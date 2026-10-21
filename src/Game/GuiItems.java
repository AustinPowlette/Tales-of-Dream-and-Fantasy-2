package Game;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** Item cards for the Backpack: a painted icon, the name, a line that says what it is and how good (the grade is the realm the item belongs to), and a badge
 *  (quantity, or what wearing it would do). Used by the Gear, Materials / pills / arrays and Food / wine / poison tabs, and by the "wearing now" cards. */
final class GuiItems {
    private GuiItems() { }

    // ------------------------------------------------------------------ words and colours
    /** Items of level n belong to the n-th realm: 1 = Qi Cultivation ... 10 = Pseudo Immortal. */
    /** The realm number of an item's level: gear (weapons, clothing, arrays) runs 1-100 with ten levels to a realm; everything else already uses 1-10. */
    static int realmOf(Item it) {
        if (it == null) return 0;
        boolean gear = it.itemType == Item.ItemType.Weapon || it.itemType == Item.ItemType.Clothing || it.itemType == Item.ItemType.Array;
        return gear && it.lvl > 0 ? Naming.gearRealm(it.lvl) : it.lvl;
    }
    static String grade(int lvl) { return lvl <= 0 ? "ungraded" : GuiSheets.REALMS[Math.min(10, lvl - 1)] + " grade"; }

    static Color gradeColor(int lvl) { return ArtMore.gradeColor(lvl); }

    static String kind(Item it) {
        if (it.itemType == null) return "item";
        switch (it.itemType) {
            case Weapon: return it.weaponType == null ? "Weapon" : it.getWeaponType();
            case Clothing: return "Armor";
            case Pill: return (it.pillType == Item.PillType.Potential ? "Potential" : "Cultivation") + " pill";
            case Herb: return "Herb";
            case Metal: return "Metal";
            case Blood: return "Blood";
            case Array: return "Array";
            case Food: return "Food";
            case Wine: return "Wine";
            case Poison: return "Poison";
            case Instrument: return "Instrument";
            default: return it.itemType.name();
        }
    }

    static boolean graded(Item it) {
        return it.itemType != null && it.itemType != Item.ItemType.Food && it.itemType != Item.ItemType.Wine && it.itemType != Item.ItemType.Poison && it.itemType != Item.ItemType.Blood;
    }

    /** "Sword · Core Formation grade · Flame +3 · made by Wei Lan" as HTML (the grade in its colour). */
    static String subHtml(Item it, boolean maker) {
        StringBuilder sb = new StringBuilder("<html>").append(GuiSheets.esc(kind(it)));
        if (it.lvl > 0 && graded(it)) sb.append(" · <font color='").append(Theme.hex(gradeColor(realmOf(it)))).append("'>").append(GuiSheets.esc(grade(realmOf(it)))).append("</font>");
        else if (it.lvl > 0) sb.append(" · level ").append(it.lvl);
        if (it.enchantment != null) sb.append(" · <font color='").append(Theme.hex(Theme.GOLD)).append("'>").append(GuiSheets.esc(it.enchantment)).append(" +").append(it.enchantBonus).append("</font>");
        if (it.heavenSent) sb.append(" · <font color='").append(Theme.hex(Theme.GOLD)).append("'>Heaven-Sent</font>");
        if (it.brand != null && it.itemType == Item.ItemType.Pill) sb.append(" · ").append(GuiSheets.esc(it.brand));
        if (maker && it.maker != null && !it.maker.isEmpty()) sb.append(" · made by ").append(GuiSheets.esc(it.maker));
        return sb.append("</html>").toString();
    }

    // ------------------------------------------------------------------ icons
    private static final java.util.Map<String, ImageIcon> icons = new HashMap<>();

    static Icon icon(Item it, int size) {
        if (!Settings.pictures || it == null || it.itemType == null) return new ImageIcon(new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB));
        String k = it.itemType + "|" + it.weaponType + "|" + it.lvl + "|" + size;
        ImageIcon ic = icons.get(k);
        if (ic == null) { ic = new ImageIcon(ArtMore.itemIcon(it, size)); if (icons.size() > 300) icons.clear(); icons.put(k, ic); }
        return ic;
    }

    // ------------------------------------------------------------------ list entries
    /** A row of an item list: an item (with its place in the bag, a badge on the right) or a section header. */
    static final class Entry {
        final Item item;
        final String head;
        int index = -1;           // place in the bag (0-based) for the commands that take a number
        String badge = "";
        Color badgeColor = Theme.DIM;
        String note = "";         // an extra dim line
        boolean makerShown = false;
        int after = Integer.MIN_VALUE; // fighting level if worn (gear only)

        Entry(Item it) { item = it; head = null; }
        private Entry(String h) { item = null; head = h; }
        static Entry header(String text) { return new Entry(text); }
        String key() { return head != null ? "h:" + head : item.name + "|" + item.lvl + "|" + item.quantity + "|" + item.enchantBonus + "|" + badge + "|" + note + "|" + index; }
    }

    /** Replaces the rows of the list when something changed (so the selection and the scroll position survive the periodic refresh). */
    static void fill(JList<Entry> list, DefaultListModel<Entry> model, List<Entry> rows) {
        boolean same = rows.size() == model.size();
        for (int i = 0; same && i < rows.size(); i++) if (!rows.get(i).key().equals(model.get(i).key())) same = false;
        if (same) return;
        int sel = list.getSelectedIndex();
        model.clear();
        for (Entry e : rows) model.addElement(e);
        if (sel >= 0 && sel < model.size()) list.setSelectedIndex(sel);
    }

    /** The place in the bag (0-based) of the selected item, or -1 (nothing selected, or a header). */
    static int selected(JList<Entry> list) {
        Entry e = list.getSelectedValue();
        return e == null || e.item == null ? -1 : e.index;
    }

    static JList<Entry> list(DefaultListModel<Entry> model) {
        JList<Entry> l = new JList<>(model);
        l.setBackground(Theme.FIELD);
        l.setCellRenderer(new Renderer());
        l.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return l;
    }

    private static final class Renderer implements ListCellRenderer<Entry> {
        private final JPanel row = new JPanel(new BorderLayout(12, 0)), hdr = new JPanel(new BorderLayout());
        private final JLabel ic = new JLabel(), title = new JLabel(), sub = new JLabel(), note = new JLabel(), badge = new JLabel(), head = new JLabel();

        Renderer() {
            title.setFont(Theme.bold(15)); sub.setFont(Theme.plain(12)); note.setFont(Theme.plain(12)); note.setForeground(Theme.DIM); badge.setFont(Theme.bold(15));
            JPanel mid = new JPanel(new BorderLayout()); mid.setOpaque(false); // BorderLayout: the title takes the full width instead of clipping at its first measured size
            mid.add(title, BorderLayout.NORTH); mid.add(sub, BorderLayout.CENTER); mid.add(note, BorderLayout.SOUTH);
            ic.setPreferredSize(new Dimension(46, 46));
            row.add(ic, BorderLayout.WEST); row.add(mid, BorderLayout.CENTER); row.add(badge, BorderLayout.EAST);
            row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.LINE), new EmptyBorder(4, 8, 4, 10)));
            head.setFont(Theme.bold(12)); head.setForeground(Theme.ACCENT);
            hdr.setBackground(Theme.PANEL2); hdr.setBorder(new EmptyBorder(6, 10, 4, 10)); hdr.add(head, BorderLayout.CENTER);
        }

        @Override public Component getListCellRendererComponent(JList<? extends Entry> l, Entry e, int i, boolean sel, boolean foc) {
            if (e.head != null) { head.setText(e.head.toUpperCase()); return hdr; }
            Item it = e.item;
            row.setBackground(sel ? Theme.SELECT : Theme.FIELD);
            ic.setIcon(icon(it, 40));
            title.setText(it.name == null ? "?" : it.name); title.setForeground(Theme.TEXT);
            sub.setText(subHtml(it, e.makerShown)); sub.setForeground(Theme.DIM);
            note.setText(e.note); note.setVisible(!e.note.isEmpty());
            badge.setText(e.badge); badge.setForeground(e.badgeColor);
            return row;
        }
    }

    // ------------------------------------------------------------------ the "wearing now" card
    static final class WornCard extends JPanel {
        private final JLabel ic = new JLabel(), slot = new JLabel(), name = new JLabel(), sub = new JLabel();

        WornCard(String slotName) {
            super(new BorderLayout(12, 0));
            setBackground(Theme.PANEL2);
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.LINE), new EmptyBorder(8, 10, 8, 10)));
            slot.setText(slotName); slot.setFont(Theme.bold(11)); slot.setForeground(Theme.ACCENT);
            name.setFont(Theme.bold(16)); name.setForeground(Theme.TEXT);
            sub.setFont(Theme.plain(12)); sub.setForeground(Theme.DIM);
            JPanel mid = new JPanel(new BorderLayout(0, 1)); mid.setOpaque(false);
            JPanel two = new JPanel(new BorderLayout(0, 1)); two.setOpaque(false);
            two.add(name, BorderLayout.NORTH); two.add(sub, BorderLayout.CENTER);
            mid.add(slot, BorderLayout.NORTH); mid.add(two, BorderLayout.CENTER);
            ic.setPreferredSize(new Dimension(58, 58));
            add(ic, BorderLayout.WEST); add(mid, BorderLayout.CENTER);
        }

        void set(Item it) {
            if (it == null) { ic.setIcon(null); name.setText("nothing"); sub.setText("Equip something from the list below."); return; }
            ic.setIcon(icon(it, 54));
            name.setText(it.name);
            sub.setText(subHtml(it, true));
        }
    }

    // ------------------------------------------------------------------ small helpers for the stores
    /** Section headers and rows for a bag whose items are grouped by a label, biggest grade first. */
    static void group(List<Entry> out, String title, List<Item> items) {
        if (items.isEmpty()) return;
        long total = 0;
        for (Item it : items) total += Math.max(1, it.quantity);
        out.add(Entry.header(title + "  ·  " + total));
        List<Item> sorted = new ArrayList<>(items);
        sorted.sort((a, b) -> b.lvl != a.lvl ? Integer.compare(b.lvl, a.lvl) : String.valueOf(a.name).compareTo(String.valueOf(b.name)));
        for (Item it : sorted) {
            Entry e = new Entry(it);
            if (it.stackable || it.quantity > 1) { e.badge = "×" + it.quantity; e.badgeColor = Theme.TEXT; }
            if (it.lvl > 0 && it.stackable && it.itemType != Item.ItemType.Food && it.itemType != Item.ItemType.Wine && it.itemType != Item.ItemType.Poison)
                e.note = "base price " + Character.priceAtLevel(it.lvl) + " ss each";
            out.add(e);
        }
    }
}
