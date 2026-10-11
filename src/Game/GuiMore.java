package Game;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/** More player windows: Backpack & equipment, Chronicle (with the hall of legends), Biography charts, Notes & bookmarks, the World overview,
 *  and the Dashboard panel used by Rankings. Like every GUI window they only read the world (inside World.read through GuiWindows.window)
 *  and send actions as ordinary commands. */
final class GuiMore {
    private GuiMore() { }

    static final Color BG = Theme.PANEL, FG = Theme.TEXT, DIM = Theme.DIM;

    private static JTextArea area(String s) {
        JTextArea a = new JTextArea(s);
        a.setEditable(false); a.setLineWrap(true); a.setWrapStyleWord(true);
        a.setBackground(Theme.FIELD); a.setForeground(FG); a.setFont(Theme.mono(13));
        a.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        return a;
    }

    private static void setRows(DefaultTableModel m, List<Object[]> rows) {
        m.setRowCount(0);
        for (Object[] r : rows) m.addRow(r);
    }

    // ================================================================== backpack & equipment
    static void backpackWindow(MapViewer mv) { backpackWindow(mv, "Gear"); }

    /** Opens the backpack window on a tab ("Gear", "Techniques", "Companions", ...), also when it is already open. */
    static void backpackWindow(MapViewer mv, String tab) {
        JDialog bw0 = GuiWindows.window(mv, "backpack", "Backpack & equipment", 860, 640, 1500, (d, refreshers) -> {
            JTabbedPane tabs = new JTabbedPane();
            tabs.setFont(GuiWindows.MONO_B);
            d.getRootPane().putClientProperty("tabs", tabs);

            // ---- gear: what you wear (two cards) above what you carry (rows with the change in fighting level)
            GuiItems.WornCard wWeapon = new GuiItems.WornCard("WEAPON"), wArmor = new GuiItems.WornCard("ARMOR");
            JLabel fight = new JLabel(" ");
            fight.setFont(Theme.plain(13)); fight.setForeground(Theme.DIM);
            JPanel cards = new JPanel(new GridLayout(1, 2, 8, 0)); cards.setBackground(BG);
            cards.add(wWeapon); cards.add(wArmor);
            JPanel wornBox = new JPanel(new BorderLayout(0, 4)); wornBox.setBackground(BG);
            wornBox.setBorder(BorderFactory.createEmptyBorder(6, 6, 2, 6));
            wornBox.add(cards, BorderLayout.CENTER); wornBox.add(fight, BorderLayout.SOUTH);
            DefaultListModel<GuiItems.Entry> gm = new DefaultListModel<>();
            JList<GuiItems.Entry> gl = GuiItems.list(gm);
            JLabel hint = GuiWindows.label("Select an item to compare it with what you wear.");
            final int[] fightNow = {0};
            JPanel btns = GuiWindows.flow(FlowLayout.LEFT);
            btns.add(mv.makeBtn("Equip selected", () -> { int r = GuiItems.selected(gl); if (r >= 0) mv.queueInput("equip " + (r + 1)); }));
            btns.add(mv.makeBtn("Equip best", () -> mv.queueInput("equip")));
            btns.add(mv.makeBtn("Sell selected", () -> { int r = GuiItems.selected(gl); if (r >= 0) mv.queueInput("sell " + (r + 1)); }));
            btns.add(mv.makeBtn("Sell all (city)", () -> mv.queueInput("sell")));
            btns.add(mv.makeBtn("Consume pills", () -> mv.queueInput("consume")));
            JPanel south = new JPanel(new BorderLayout()); south.setBackground(BG);
            south.add(hint, BorderLayout.NORTH); south.add(btns, BorderLayout.CENTER);
            JPanel gear = new JPanel(new BorderLayout(4, 4)); gear.setBackground(BG);
            gear.add(wornBox, BorderLayout.NORTH);
            gear.add(new JScrollPane(gl), BorderLayout.CENTER);
            gear.add(south, BorderLayout.SOUTH);
            tabs.addTab("Gear", gear);

            // ---- techniques
            DefaultTableModel tm = GuiWindows.model();
            tm.setColumnIdentifiers(new String[]{"Slot", "Technique", "Kind", "Level", "Potential", "Mastery", "By"});
            JTable tt = GuiWindows.table(tm);
            tt.getColumnModel().getColumn(5).setCellRenderer((tbl, v, sel, foc, r, c) -> {
                JProgressBar b = new JProgressBar(0, 100);
                b.setValue(v instanceof Integer ? (Integer) v : 0); b.setStringPainted(true); b.setString(v + "%");
                return b;
            });
            JPanel tech = new JPanel(new BorderLayout(4, 4)); tech.setBackground(BG);
            tech.add(new JScrollPane(tt), BorderLayout.CENTER);
            JPanel tb = GuiWindows.flow(FlowLayout.LEFT);
            tb.add(mv.makeBtn("Library", () -> mv.queueInput("library")));
            tb.add(GuiWindows.label("The strongest technique of each kind is used automatically; mastery = level / potential."));
            tech.add(tb, BorderLayout.SOUTH);
            tabs.addTab("Techniques", tech);

            // ---- stores: herbs, metals, blood, pills, arrays
            DefaultListModel<GuiItems.Entry> sm = new DefaultListModel<>();
            JList<GuiItems.Entry> sl = GuiItems.list(sm);
            JLabel storeSum = GuiWindows.label(" ");
            JPanel sbtns = GuiWindows.flow(FlowLayout.LEFT);
            sbtns.add(mv.makeBtn("Consume pills", () -> mv.queueInput("consume")));
            sbtns.add(mv.makeBtn("Sell all (city)", () -> mv.queueInput("sell")));
            sbtns.add(GuiWindows.label("Herbs and metal feed a trade; pills raise cultivation or potential."));
            JPanel stores = new JPanel(new BorderLayout(4, 4)); stores.setBackground(BG);
            stores.add(storeSum, BorderLayout.NORTH); stores.add(new JScrollPane(sl), BorderLayout.CENTER); stores.add(sbtns, BorderLayout.SOUTH);
            tabs.addTab("Materials, pills, arrays", stores);

            // ---- goods: food, wine, poison, music
            DefaultListModel<GuiItems.Entry> om = new DefaultListModel<>();
            JList<GuiItems.Entry> ol = GuiItems.list(om);
            JLabel cond = GuiWindows.label(" ");
            JPanel ob = GuiWindows.flow(FlowLayout.LEFT);
            ob.add(mv.makeBtn("Eat", () -> { int r = GuiItems.selected(ol); mv.queueInput(r >= 0 ? "eat " + (r + 1) : "eat"); }));
            ob.add(mv.makeBtn("Drink", () -> { int r = GuiItems.selected(ol); mv.queueInput(r >= 0 ? "drink " + (r + 1) : "drink"); }));
            ob.add(mv.makeBtn("Make food", () -> mv.queueInput("make food")));
            ob.add(mv.makeBtn("Make wine", () -> mv.queueInput("make wine")));
            ob.add(mv.makeBtn("Make poison", () -> mv.queueInput("make poison")));
            ob.add(mv.makeBtn("Play", () -> mv.queueInput("play")));
            ob.add(mv.makeBtn("Perform", () -> mv.queueInput("perform")));
            ob.add(mv.makeBtn("Coat blade", () -> mv.queueInput("coat")));
            JPanel ogoods = new JPanel(new BorderLayout(4, 4)); ogoods.setBackground(BG);
            ogoods.add(new JScrollPane(ol), BorderLayout.CENTER);
            JPanel osouth = new JPanel(new BorderLayout()); osouth.setBackground(BG);
            osouth.add(ob, BorderLayout.CENTER);
            ogoods.add(cond, BorderLayout.NORTH); ogoods.add(osouth, BorderLayout.SOUTH);
            tabs.addTab("Food, wine, poison", ogoods);

            // ---- companions
            DefaultTableModel cm = GuiWindows.model();
            cm.setColumnIdentifiers(new String[]{"#", "Beast", "Lvl", "Potential", "Age", "Adds to your fight"});
            JTable ct = GuiWindows.table(cm);
            ct.getColumnModel().getColumn(0).setMaxWidth(40);
            JLabel ccap = GuiWindows.label(" ");
            JPanel cb = GuiWindows.flow(FlowLayout.LEFT);
            cb.add(mv.makeBtn("Tame a beast here", () -> mv.queueInput("tame")));
            cb.add(mv.makeBtn("Feed selected", () -> { int r = ct.getSelectedRow(); if (r >= 0) mv.queueInput("feed " + (r + 1)); }));
            cb.add(mv.makeBtn("Release selected", () -> { int r = ct.getSelectedRow(); if (r >= 0 && GuiExtras.confirm(d, "Send this companion back to the wild?")) mv.queueInput("!release " + (r + 1)); }));
            JPanel comp = new JPanel(new BorderLayout(4, 4)); comp.setBackground(BG);
            comp.add(ccap, BorderLayout.NORTH); comp.add(new JScrollPane(ct), BorderLayout.CENTER); comp.add(cb, BorderLayout.SOUTH);
            tabs.addTab("Companions", comp);

            refreshers.add(() -> {
                Character p = GuiWindows.player(mv);
                if (p == null) { fight.setText("No player character."); return; }
                Item w = p.equippedWeapon, a = p.equippedClothing;
                wWeapon.set(w); wArmor.set(a);
                int base = TextTools.power(p, w, a);
                fightNow[0] = base;
                fight.setText("<html>Fighting level <b><font color='" + Theme.hex(Theme.GOLD) + "'>" + base + "</font></b> — your level " + p.lvl + " plus gear and techniques, divided by 5.   Wealth <b>" + p.ss + "</b> ss</html>");
                List<GuiItems.Entry> rows = new ArrayList<>();
                List<Item> bag = new ArrayList<>(p.backpack);
                for (int i = 0; i < bag.size(); i++) {
                    Item it = bag.get(i);
                    GuiItems.Entry e = new GuiItems.Entry(it);
                    e.index = i; e.makerShown = true;
                    boolean wear = it.itemType == Item.ItemType.Weapon || it.itemType == Item.ItemType.Clothing;
                    if (wear) {
                        int pw = it.itemType == Item.ItemType.Weapon ? TextTools.power(p, it, a) : TextTools.power(p, w, it);
                        e.after = pw;
                        e.badge = pw > base ? "▲ " + pw : pw < base ? "▼ " + pw : "= " + pw;
                        e.badgeColor = pw > base ? Theme.OK : pw < base ? Theme.DANGER : Theme.DIM;
                    } else if (it.stackable || it.quantity > 1) { e.badge = "×" + it.quantity; e.badgeColor = Theme.TEXT; }
                    rows.add(e);
                }
                if (rows.isEmpty()) rows.add(GuiItems.Entry.header("Your backpack is empty — hunt, craft or buy gear (Buy gear in a city)"));
                GuiItems.fill(gl, gm, rows);

                List<Object[]> trows = new ArrayList<>();
                Object[][] slots = {{"Martial", p.martialTechnique}, {"Martial 2", p.martialTechnique2}, {"Movement", p.movementTechnique}, {"Movement 2", p.movementTechnique2}};
                for (Object[] s : slots) { Technique q = (Technique) s[1]; if (q != null) trows.add(new Object[]{s[0], q.name, q.getType(), q.lvl, q.potential, mastery(q), q.maker}); }
                for (Technique q : new ArrayList<>(p.techniqueList)) trows.add(new Object[]{"known", q.name, q.getType(), q.lvl, q.potential, mastery(q), q.maker});
                setRows(tm, trows);

                List<GuiItems.Entry> srows = new ArrayList<>();
                List<Item> herbs = new ArrayList<>(), metals = new ArrayList<>(), bloods = new ArrayList<>(), potPills = new ArrayList<>(), cultPills = new ArrayList<>();
                for (Item it : new ArrayList<>(p.materialBag)) {
                    if (it == null || it.itemType == null) continue;
                    if (it.itemType == Item.ItemType.Herb) herbs.add(it); else if (it.itemType == Item.ItemType.Metal) metals.add(it); else bloods.add(it);
                }
                for (Item it : new ArrayList<>(p.pillBag)) { if (it == null) continue; if (it.pillType == Item.PillType.Potential) potPills.add(it); else cultPills.add(it); }
                GuiItems.group(srows, "Herbs", herbs); GuiItems.group(srows, "Metals", metals); GuiItems.group(srows, "Blood", bloods);
                GuiItems.group(srows, "Pills that raise potential", potPills); GuiItems.group(srows, "Pills that raise cultivation", cultPills);
                GuiItems.group(srows, "Arrays", new ArrayList<>(p.arrayBag));
                int stacks = herbs.size() + metals.size() + bloods.size() + potPills.size() + cultPills.size() + p.arrayBag.size();
                if (srows.isEmpty()) srows.add(GuiItems.Entry.header("Nothing here yet — forage herbs, mine metal, craft pills and arrays"));
                storeSum.setText(stacks == 0 ? "You carry no materials, pills or arrays." : stacks + " stacks: " + herbs.size() + " herbs, " + metals.size() + " metals, " + (potPills.size() + cultPills.size()) + " pills, " + p.arrayBag.size() + " arrays.");
                GuiItems.fill(sl, sm, srows);

                List<GuiItems.Entry> orows = new ArrayList<>();
                List<Item> goodsNow = p.goods == null ? new ArrayList<Item>() : new ArrayList<>(p.goods);
                for (int i = 0; i < goodsNow.size(); i++) {
                    Item it = goodsNow.get(i);
                    if (it == null) continue;
                    GuiItems.Entry e = new GuiItems.Entry(it);
                    e.index = i; e.badge = "×" + it.quantity; e.badgeColor = Theme.TEXT;
                    orows.add(e);
                }
                if (orows.isEmpty()) orows.add(GuiItems.Entry.header("No food, wine or poison — Make food / Make wine from herbs, or buy at the market"));
                GuiItems.fill(ol, om, orows);
                Life.Fx fx = p.fx;
                String cs = fx == null ? "No special conditions." : "Conditions: " + (fx.foodWeeks > 0 ? "nourished " + fx.foodWeeks + "w   " : "") + (fx.drunkWeeks > 0 ? "drunk " + fx.drunkWeeks + "w   " : "")
                        + (fx.inspiredWeeks > 0 ? "inspired " + fx.inspiredWeeks + "w   " : "") + (fx.poisonWeeks > 0 ? "POISONED Lv." + fx.poisonLvl + " " + fx.poisonWeeks + "w   " : "") + (fx.coat > 0 ? "poisoned blade x" + fx.coat : "");
                cond.setText(cs + "   Instrument: " + (Life.bestInstrument(p) == null ? "none" : Life.bestInstrument(p).name));
                int csel = ct.getSelectedRow();
                List<Object[]> crows = new ArrayList<>();
                List<Character> bs = Taming.peek(p);
                for (int i = 0; i < bs.size(); i++) { Character b = bs.get(i); crows.add(new Object[]{i + 1, b.name + " " + b.lastName, b.lvl, b.potential, b.age, "+" + Math.min(b.lvl, Math.max(1, p.lvl)) / 10}); }
                setRows(cm, crows);
                if (csel >= 0 && csel < cm.getRowCount()) ct.setRowSelectionInterval(csel, csel);
                ccap.setText("Companions " + bs.size() + " / " + Taming.cap(p) + "  (a cultivator may keep Major Lvl + 1; they follow you and fight beside you)");
            });
            gl.addListSelectionListener(e -> {
                if (e.getValueIsAdjusting()) return;
                GuiItems.Entry en = gl.getSelectedValue();
                if (en == null || en.item == null) { hint.setText("Select an item to compare it with what you wear."); return; }
                hint.setText(en.after == Integer.MIN_VALUE ? "Not wearable gear." : "Wearing it makes your fighting level " + en.after + "  (now " + fightNow[0] + ").");
            });
            return tabs;
        });
        Object t = bw0 == null ? null : bw0.getRootPane().getClientProperty("tabs");
        if (t instanceof JTabbedPane && tab != null) {
            JTabbedPane tp = (JTabbedPane) t;
            for (int i = 0; i < tp.getTabCount(); i++) if (tp.getTitleAt(i).startsWith(tab)) { tp.setSelectedIndex(i); break; }
        }
    }

    private static int mastery(Technique q) { return q.potential <= 0 ? 100 : Math.max(0, Math.min(100, 100 * q.lvl / q.potential)); }

    // ================================================================== chronicle
    static void chronicleWindow(MapViewer mv) {
        GuiWindows.window(mv, "chronicle", "Chronicle", 900, 620, 3000, (d, refreshers) -> {
            JTabbedPane tabs = new JTabbedPane();
            tabs.setFont(GuiWindows.MONO_B);

            JTextField query = new JTextField(22);
            JComboBox<String> kind = new JComboBox<>();
            kind.addItem("all");
            for (String k : Chronicle.KINDS) kind.addItem(k);
            JCheckBox mine = new JCheckBox("only me");
            mine.setBackground(BG); mine.setForeground(FG);
            JButton refresh = new JButton("Search");
            JPanel top = GuiWindows.flow(FlowLayout.LEFT);
            top.add(GuiWindows.label("Text or #id:")); top.add(query); top.add(GuiWindows.label("Kind:")); top.add(kind); top.add(mine); top.add(refresh);
            DefaultTableModel m = GuiWindows.model();
            m.setColumnIdentifiers(new String[]{"When", "Kind", "Event"});
            JTable t = GuiWindows.table(m);
            t.getColumnModel().getColumn(0).setPreferredWidth(110); t.getColumnModel().getColumn(0).setMaxWidth(150);
            t.getColumnModel().getColumn(1).setPreferredWidth(90); t.getColumnModel().getColumn(1).setMaxWidth(110);
            List<Chronicle.Entry>[] shown = new List[]{new ArrayList<>()};
            int[] signature = {-1};
            Runnable load = () -> {
                String q = query.getText();
                int me = -1;
                if (mine.isSelected()) { Character p = GuiWindows.player(mv); if (p != null) me = p.id; }
                List<Chronicle.Entry> list = Chronicle.search(me >= 0 ? "#" + me : q, (String) kind.getSelectedItem(), 400);
                if (me >= 0 && !q.trim().isEmpty()) { String lq = q.trim().toLowerCase(); list.removeIf(e -> !e.text.toLowerCase().contains(lq)); }
                shown[0] = list;
                List<Object[]> rows = new ArrayList<>();
                for (Chronicle.Entry e : list) rows.add(new Object[]{e.when(), e.kind, e.text});
                setRows(m, rows);
                signature[0] = Chronicle.log.size();
            };
            refresh.addActionListener(e -> World.readSoon(200, load));
            query.addActionListener(e -> World.readSoon(200, load));
            kind.addActionListener(e -> World.readSoon(200, load));
            mine.addActionListener(e -> World.readSoon(200, load));
            t.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() != 2) return;
                    int r = t.getSelectedRow();
                    if (r < 0 || r >= shown[0].size()) return;
                    for (int id : shown[0].get(r).ids) { Character c = Character.charById(id); if (c != null && !c.dead) { mv.showCharacterDialog(c); return; } }
                }
            });
            JPanel chron = new JPanel(new BorderLayout(4, 4)); chron.setBackground(BG);
            JLabel evPic = new JLabel("", SwingConstants.CENTER);
            evPic.setPreferredSize(new Dimension(300, 170));
            evPic.setForeground(Theme.DIM);
            t.getSelectionModel().addListSelectionListener(e -> { // an illustration for the selected event
                if (e.getValueIsAdjusting()) return;
                int r = t.getSelectedRow();
                if (r < 0 || r >= shown[0].size() || !Settings.pictures) { evPic.setIcon(null); return; }
                Chronicle.Entry en = shown[0].get(r);
                evPic.setIcon(new ImageIcon(ArtMore.eventArt(eventKind(en), en.week * 31L + en.text.hashCode(), 300, 169)));
            });
            JPanel east = new JPanel(new BorderLayout()); east.setBackground(BG); east.add(evPic, BorderLayout.NORTH);
            chron.add(top, BorderLayout.NORTH); chron.add(new JScrollPane(t), BorderLayout.CENTER); chron.add(east, BorderLayout.EAST);
            chron.add(GuiWindows.label("Newest first. Double-click a row to open the first cultivator in it who is still alive."), BorderLayout.SOUTH);
            tabs.addTab("Chronicle", chron);

            tabs.addTab("Hall of legends", GuiPages.pane(mv, refreshers, GuiPages::legendsHtml));
            refreshers.add(() -> { if (signature[0] != Chronicle.log.size()) load.run(); });
            return tabs;
        });
    }

    // ================================================================== biography
    static void biographyWindow(MapViewer mv, Character who) {
        if (who == null) return;
        final int id = who.id;
        GuiWindows.window(mv, "bio" + id, "Biography — " + who.name + " " + who.lastName, 980, 640, 4000, (d, refreshers) -> {
            GuiWindows.ChartPanel lvl = new GuiWindows.ChartPanel("Level by year");
            GuiWindows.ChartPanel wealth = new GuiWindows.ChartPanel("Wealth (log10 spirit stones x 100) by year");
            GuiWindows.ChartPanel social = new GuiWindows.ChartPanel("Friends and kills by year");
            JPanel charts = new JPanel(new GridLayout(3, 1, 4, 4)); charts.setBackground(BG);
            charts.add(lvl); charts.add(wealth); charts.add(social);
            JLabel head = new JLabel(" ");
            head.setOpaque(true); head.setBackground(Theme.PANEL2); head.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            head.setIconTextGap(12);
            final String[] headKey = {""};
            JPanel right = new JPanel(new BorderLayout()); right.setBackground(BG);
            right.add(head, BorderLayout.NORTH);
            right.add(GuiPages.pane(mv, refreshers, () -> GuiPages.bioHtml(Character.charById(id))), BorderLayout.CENTER);
            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, charts, right);
            split.setResizeWeight(0.45);
            SwingUtilities.invokeLater(() -> split.setDividerLocation(0.42));
            refreshers.add(() -> {
                Character c = Character.charById(id);
                List<int[]> life = Chronicle.bioOf(id);
                lvl.setSeries(new String[]{"Level"}, life, new int[]{1}, new Color[]{Theme.ACCENT}, false);
                wealth.setSeries(new String[]{"Wealth"}, life, new int[]{2}, new Color[]{Theme.GOLD}, false);
                social.setSeries(new String[]{"Friends", "Kills"}, life, new int[]{3, 4}, new Color[]{Theme.OK, Theme.DANGER}, false);
                if (c == null) { head.setText("<html><b>Unknown cultivator</b></html>"); return; }
                String key = c.name + c.lastName + c.dead + c.trait + c.gender + c.affiliation + c.repTitle;
                if (!key.equals(headKey[0])) {
                    headKey[0] = key;
                    head.setIcon(Settings.pictures ? new ImageIcon(ArtMore.portrait(c, 64, 80)) : null);
                    head.setText("<html><span style='font-size:" + Math.round(18 * Settings.fontScale) + "px;color:" + Theme.hex(Theme.GOLD) + "'><b>" + GuiSheets.esc(c.name + " " + c.lastName) + (c.dead ? " (dead)" : "") + "</b></span>"
                            + (c.repTitle != null ? "<br><span style='color:" + Theme.hex(Theme.DIM) + "'>'" + GuiSheets.esc(c.repTitle) + "'</span>" : "")
                            + "<br><span style='color:" + Theme.hex(Theme.DIM) + "'>" + GuiSheets.esc(c.getTrait() + " · " + c.affiliation + " · " + c.gender) + "</span></html>");
                }
            });
            return split;
        });
    }

    // ================================================================== notes
    static void notesWindow(MapViewer mv) {
        GuiWindows.window(mv, "notes", "Notes & bookmarks", 640, 460, 2500, (d, refreshers) -> {
            DefaultTableModel m = GuiWindows.model();
            m.setColumnIdentifiers(new String[]{"#", "Where / who", "Note"});
            JTable t = GuiWindows.table(m);
            t.getColumnModel().getColumn(0).setMaxWidth(40);
            JPanel btns = GuiWindows.flow(FlowLayout.LEFT);
            btns.add(mv.makeBtn("Note this tile", () -> {
                String s = JOptionPane.showInputDialog(d, "Note for the tile you stand on:", "Note", JOptionPane.PLAIN_MESSAGE);
                if (s != null && !s.trim().isEmpty()) mv.queueInput("note " + s.trim());
            }));
            btns.add(mv.makeBtn("Go there", () -> {
                int r = t.getSelectedRow(); List<String> keys = TextTools.noteKeys();
                if (r < 0 || r >= keys.size()) return;
                String k = keys.get(r);
                if (k.startsWith("t:")) { String[] p = k.substring(2).split(","); mv.queueInput("goto " + Integer.parseInt(p[0]) * 100 + " " + Integer.parseInt(p[1]) * 100); }
                else { Character c = Character.charById(Integer.parseInt(k.substring(2))); if (c != null) mv.showCharacterDialog(c); }
            }));
            btns.add(mv.makeBtn("Delete", () -> { int r = t.getSelectedRow(); if (r >= 0) mv.queueInput("unnote " + (r + 1)); }));
            JPanel p = new JPanel(new BorderLayout(4, 4)); p.setBackground(BG);
            p.add(new JScrollPane(t), BorderLayout.CENTER); p.add(btns, BorderLayout.SOUTH);
            refreshers.add(() -> {
                int sel = t.getSelectedRow();
                List<Object[]> rows = new ArrayList<>();
                List<String> keys = TextTools.noteKeys();
                for (int i = 0; i < keys.size(); i++) rows.add(new Object[]{i + 1, Chronicle.describeKey(keys.get(i)), Chronicle.notes.get(keys.get(i))});
                setRows(m, rows);
                if (sel >= 0 && sel < m.getRowCount()) t.setRowSelectionInterval(sel, sel);
            });
            return p;
        });
    }

    // ================================================================== world overview
    private static final class WorldView extends JPanel {
        int n = 0;
        int[][] rgb = new int[0][0];
        String[][] label = new String[0][0];
        char[][] mark = new char[0][0];
        List<int[]> people = new ArrayList<>();
        int hx = -1, hy = -1;

        WorldView() { setBackground(Theme.BG); }

        void snapshot(Character me) {
            int r = Game.Map.range;
            int[][] c = new int[r][r]; String[][] lb = new String[r][r]; char[][] mk = new char[r][r];
            for (int x = 0; x < r; x++) for (int y = 0; y < r; y++) {
                Area a = Game.Map.tiles[x][y];
                if (a == null) { c[x][y] = 0x0b0e12; continue; }
                Color base = base(a);
                c[x][y] = base.getRGB();
                if (a.city) { mk[x][y] = 'C'; lb[x][y] = a.name; }
                else if (a.sect != null) { mk[x][y] = 'S'; lb[x][y] = a.sect.name; }
                else if (a.dungeonType != null) mk[x][y] = 'D';
            }
            List<int[]> ppl = new ArrayList<>();
            for (Character h : Notifications.humans()) if (h != null && !h.dead && h.location != null) ppl.add(new int[]{h.location.x, h.location.y, h == me ? 1 : 0});
            n = r; rgb = c; label = lb; mark = mk; people = ppl;
            repaint();
        }

        static Color base(Area a) { return new Color(BiomeGen.rgb(a.biomem, a.elev, a.river)); }

        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (n == 0) return;
            int ts = Math.max(4, Math.min(getWidth(), getHeight()) / n);
            for (int x = 0; x < n; x++) for (int y = 0; y < n; y++) {
                int px = x * ts, py = (n - 1 - y) * ts;
                g.setColor(new Color(rgb[x][y]));
                g.fillRect(px, py, ts, ts);
                char m = mark[x][y];
                if (m == 'C') { g.setColor(new Color(240, 200, 60)); g.fillRect(px + 1, py + 1, ts - 2, ts - 2); }
                else if (m == 'S') { g.setColor(new Color(170, 90, 210)); g.fillPolygon(new int[]{px + ts / 2, px + ts - 1, px + ts / 2, px + 1}, new int[]{py + 1, py + ts / 2, py + ts - 1, py + ts / 2}, 4); }
                else if (m == 'D') { g.setColor(new Color(220, 80, 50)); g.fillOval(px + ts / 3, py + ts / 3, Math.max(2, ts / 3), Math.max(2, ts / 3)); }
            }
            g.setFont(Theme.plain(Math.max(9, ts / 2)));
            for (int x = 0; x < n; x++) for (int y = 0; y < n; y++) {
                String s = label[x][y];
                if (s == null) continue;
                int px = x * ts, py = (n - 1 - y) * ts;
                g.setColor(new Color(0, 0, 0, 150)); g.drawString(s, px + ts + 1, py + ts / 2 + 5);
                g.setColor(Color.WHITE); g.drawString(s, px + ts, py + ts / 2 + 4);
            }
            for (int[] p : people) {
                int px = p[0] * ts, py = (n - 1 - p[1]) * ts;
                g.setColor(p[2] == 1 ? Color.YELLOW : Color.CYAN);
                g.setStroke(new BasicStroke(2));
                g.drawOval(px - 1, py - 1, ts + 2, ts + 2);
            }
            if (hx >= 0) { g.setColor(Color.WHITE); g.setStroke(new BasicStroke(1)); g.drawRect(hx * ts, (n - 1 - hy) * ts, ts, ts); }
        }

        int[] tileAt(Point p) {
            if (n == 0) return null;
            int ts = Math.max(4, Math.min(getWidth(), getHeight()) / n);
            int x = p.x / ts, y = n - 1 - p.y / ts;
            return x >= 0 && x < n && y >= 0 && y < n ? new int[]{x, y} : null;
        }
    }

    /** Which illustration fits a chronicle entry. */
    static String eventKind(Chronicle.Entry e) {
        String t = e.text == null ? "" : e.text;
        if (t.contains("Tide")) return "tide";
        if (t.contains("Tournament") || t.contains("Contest") || t.contains("Games") || t.contains("Match") || t.contains("Trial of Arms")) return "tournament";
        if (t.contains("Festival") || t.contains("Fair")) return "festival";
        if (t.contains("Trial")) return "trial";
        if ("war".equals(e.kind) || t.contains("war ")) return "war";
        if ("city".equals(e.kind)) return "city";
        if ("sect".equals(e.kind)) return "sect";
        if ("death".equals(e.kind)) return "death";
        if ("reincarnation".equals(e.kind)) return "reincarnation";
        return "legend";
    }

    /** The whole world as an ink-and-wash painting, with its named regions and the cities and sects you know. */
    static void paintedMapWindow(MapViewer mv) {
        GuiWindows.window(mv, "paintedmap", "Painted map of the world", 860, 900, 60000, (d, refreshers) -> {
            JLabel pic = new JLabel("Painting the map...", SwingConstants.CENTER);
            pic.setForeground(Theme.DIM);
            JPanel p = new JPanel(new BorderLayout(4, 4)); p.setBackground(BG);
            JPanel btns = GuiWindows.flow(FlowLayout.LEFT);
            final BufferedImage[] last = {null};
            btns.add(mv.makeBtn("Save as PNG", () -> {
                if (last[0] == null) return;
                try { java.io.File dir = new java.io.File(Settings.exportDir); dir.mkdirs(); java.io.File f = new java.io.File(dir, "world_map.png"); javax.imageio.ImageIO.write(last[0], "png", f); System.out.println("Saved " + f.getPath()); } catch (Exception ex) { /* nothing to do */ }
            }));
            btns.add(GuiWindows.label("Cities and sects appear once you have seen them; the names of seas, ranges, forests and plains come from the land itself."));
            p.add(btns, BorderLayout.NORTH); p.add(pic, BorderLayout.CENTER);
            Runnable paint = () -> {
                final java.util.Set<Area>[] known = new java.util.Set[]{null};
                World.read(200, () -> { Character pl = GuiWindows.player(mv); if (pl != null) known[0] = new java.util.HashSet<>(pl.knownLocations); });
                int side = Math.max(300, Math.min(d.getContentPane().getWidth(), d.getContentPane().getHeight() - 40));
                new Thread(() -> { // painting takes a moment: off the Swing thread
                    BufferedImage im = ArtMore.worldMap(side, side, known[0]);
                    SwingUtilities.invokeLater(() -> { last[0] = im; pic.setText(""); pic.setIcon(new ImageIcon(im)); });
                }, "map-paint").start();
            };
            refreshers.add(paint);
            SwingUtilities.invokeLater(paint);
            return p;
        });
    }

    static void worldWindow(MapViewer mv) {
        GuiWindows.window(mv, "world", "World overview", 760, 800, 2500, (d, refreshers) -> {
            WorldView v = new WorldView();
            JLabel info = GuiWindows.label("Click a tile for details.");
            int[] sel = {-1, -1};
            v.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    int[] t = v.tileAt(e.getPoint());
                    if (t == null) return;
                    sel[0] = t[0]; sel[1] = t[1]; v.hx = t[0]; v.hy = t[1];
                    World.read(100, () -> {
                        Area a = Game.Map.tiles[t[0]][t[1]];
                        info.setText(a == null ? "[" + t[0] * 100 + ", " + t[1] * 100 + "] — unexplored" : "[" + t[0] * 100 + ", " + t[1] * 100 + "] " + a.name + " (" + a.biomeM + ", level " + a.lvl + ")"
                                + (a.city ? "  CITY pop " + a.population : "") + (a.sect != null ? "  SECT " + a.sect.name : "") + (a.dungeonType != null ? "  " + a.dungeonType : "") + (a.hotspot ? "  hotspot" : ""));
                    });
                    v.repaint();
                }
            });
            JPanel btns = GuiWindows.flow(FlowLayout.LEFT);
            btns.add(mv.makeBtn("Go to selected", () -> { if (sel[0] >= 0) mv.queueInput("goto " + sel[0] * 100 + " " + sel[1] * 100); }));
            btns.add(mv.makeBtn("Route preview", () -> { if (sel[0] >= 0) mv.queueInput("route " + sel[0] * 100 + " " + sel[1] * 100); }));
            btns.add(mv.makeBtn("City / sect details", () -> { if (sel[0] >= 0) placeWindow(mv, Game.Map.tiles[sel[0]][sel[1]]); }));
            btns.add(GuiWindows.label("gold square = city, purple diamond = sect, red dot = dungeon, ring = players (yellow: you). North is up."));
            JPanel south = new JPanel(new BorderLayout()); south.setBackground(BG);
            south.add(info, BorderLayout.NORTH); south.add(btns, BorderLayout.CENTER);
            JPanel p = new JPanel(new BorderLayout()); p.setBackground(BG);
            p.add(v, BorderLayout.CENTER); p.add(south, BorderLayout.SOUTH);
            refreshers.add(() -> v.snapshot(GuiWindows.player(mv)));
            return p;
        });
    }

    // ================================================================== digest
    static void digestWindow(MapViewer mv, int weeks) {
        GuiWindows.window(mv, "digest", "What happened lately", 860, 640, 5000, (d, refreshers) -> {
            range[0] = weeks;
            final int[] spans = {13, 52, 260};
            final String[] names = {"Season", "Year", "5 years"};
            final JButton[] bs = new JButton[3];
            JPanel btns = GuiWindows.flow(FlowLayout.LEFT);
            Runnable style = () -> { for (int i = 0; i < 3; i++) bs[i].putClientProperty("FlatLaf.style", spans[i] == range[0] ? "background:#265a52;borderColor:#68b8a0" : null); };
            for (int i = 0; i < 3; i++) {
                final int w = spans[i];
                bs[i] = mv.makeBtn(names[i], "Show the last " + names[i].toLowerCase() + (w == 13 ? " (13 weeks)" : w == 52 ? " (52 weeks)" : " (260 weeks)") + ".", () -> {
                    range[0] = w; style.run();
                    World.readSoon(100, () -> { for (Runnable r : refreshers) { try { r.run(); } catch (RuntimeException ignored) { } } });
                });
                btns.add(bs[i]);
            }
            style.run();
            JCheckBox auto = new JCheckBox("Show when a saved game opens", Settings.digestOnLoad);
            auto.setBackground(BG); auto.setForeground(FG);
            auto.addActionListener(e -> { Settings.digestOnLoad = auto.isSelected(); Settings.save(); });
            btns.add(auto);
            JPanel p = new JPanel(new BorderLayout(4, 4)); p.setBackground(BG);
            p.add(btns, BorderLayout.NORTH);
            p.add(GuiPages.pane(mv, refreshers, () -> GuiPages.digestHtml(range[0])), BorderLayout.CENTER);
            return p;
        });
    }
    private static final int[] range = {52};

    // ================================================================== rivalries (sects and cities)
    static JComponent rivalriesPanel(List<Runnable> refreshers) {
        DefaultTableModel sm = GuiWindows.model(), cm = GuiWindows.model();
        JTable st = GuiWindows.table(sm), ct = GuiWindows.table(cm);
        for (JTable t : new JTable[]{st, ct}) t.setAutoCreateRowSorter(true);
        JPanel p = new JPanel(new GridLayout(2, 1, 4, 4)); p.setBackground(BG);
        JPanel sp = new JPanel(new BorderLayout()); sp.setBackground(BG); sp.add(GuiWindows.label("Sects (power = sum of members' level^1.5 / 100)"), BorderLayout.NORTH); sp.add(new JScrollPane(st), BorderLayout.CENTER);
        JPanel cp = new JPanel(new BorderLayout()); cp.setBackground(BG); cp.add(GuiWindows.label("Cities"), BorderLayout.NORTH); cp.add(new JScrollPane(ct), BorderLayout.CENTER);
        p.add(sp); p.add(cp);
        refreshers.add(() -> {
            List<Object[]> srows = new ArrayList<>(), crows = new ArrayList<>();
            for (Area a : new ArrayList<>(Game.Map.createdAreas)) {
                if (a == null) continue;
                if (a.sect != null && a.sect.members != null && !a.sect.members.isEmpty()) {
                    Organization s = a.sect; double power = 0; int n = 0, top = 0;
                    for (Character m : new ArrayList<>(s.members)) if (m != null && !m.dead) { power += Math.pow(m.lvl, 1.5) / 100.0; n++; top = Math.max(top, m.lvl); }
                    srows.add(new Object[]{s.name, a.name, n, top, (int) power, s.warTarget != null ? s.warTarget.name : "—", s.allies.size(), s.sectCash, s.rulesText()});
                }
                if (a.city) crows.add(new Object[]{a.name, a.population, a.cityLevel(), a.cityOwner != null ? a.cityOwner.name + " " + a.cityOwner.lastName : "—", a.cityTreasury, a.shops.size(), a.sect != null ? a.sect.name : "—"});
            }
            sm.setDataVector(srows.toArray(new Object[0][]), new String[]{"Sect", "Home", "Members", "Top Lv", "Power", "At war with", "Allies", "Cash", "Rules"});
            cm.setDataVector(crows.toArray(new Object[0][]), new String[]{"City", "Population", "Level", "Lord", "Treasury", "Shops", "Sect"});
        });
        return p;
    }

    // ================================================================== run statistics
    static void runWindow(MapViewer mv) {
        GuiWindows.window(mv, "run", "Run statistics", 920, 620, 4000, (d, refreshers) -> {
            JTabbedPane tabs = new JTabbedPane();
            tabs.setFont(GuiWindows.MONO_B);
            tabs.addTab("This run", GuiPages.pane(mv, refreshers, GuiPages::runHtml));
            tabs.addTab("Hall of fame", GuiPages.pane(mv, refreshers, GuiPages::hallHtml));
            JPanel btns = GuiWindows.flow(FlowLayout.LEFT);
            btns.add(mv.makeBtn("Export CSV", "Save every life of this run as a spreadsheet file in the exports folder.", () -> mv.queueInput("export run")));
            btns.add(mv.makeBtn("Retire run…", "Write this run into the hall of fame now. The game goes on.", () -> { if (GuiExtras.confirm(d, "Record this run in the hall of fame? The game goes on.")) mv.queueInput("!retire"); }));
            JPanel p = new JPanel(new BorderLayout(4, 4)); p.setBackground(BG);
            p.add(tabs, BorderLayout.CENTER); p.add(btns, BorderLayout.SOUTH);
            return p;
        });
    }

    // ================================================================== city / sect details
    static void placeWindow(MapViewer mv, Area a) {
        if (a == null || (!a.city && a.sect == null)) return;
        final int ax = a.x, ay = a.y;
        GuiWindows.window(mv, "place" + ax + "_" + ay, "Details — " + (a.city ? a.name : a.sect.name), 780, 640, 3000, (d, refreshers) -> {
            JPanel btns = GuiWindows.flow(FlowLayout.LEFT);
            btns.add(mv.makeBtn("Go there", () -> mv.queueInput("goto " + ax * 100 + " " + ay * 100)));
            btns.add(mv.makeBtn("Route preview", () -> mv.queueInput("route " + ax * 100 + " " + ay * 100)));
            btns.add(mv.makeBtn("Open lord / master", () -> {
                Area t = Game.Map.tiles[ax][ay];
                Character who = t == null ? null : t.cityOwner != null ? t.cityOwner : t.sect != null ? t.sect.master : null;
                if (who != null) mv.showCharacterDialog(who);
            }));
            JPanel p = new JPanel(new BorderLayout(4, 4)); p.setBackground(BG);
            p.add(GuiPages.pane(mv, refreshers, () -> GuiPages.placeHtml(Game.Map.tiles[ax][ay], GuiWindows.player(mv))), BorderLayout.CENTER); p.add(btns, BorderLayout.SOUTH);
            return p;
        });
    }

    // ================================================================== dashboard tab (Rankings)
    static JComponent dashboardPanel(MapViewer mv, List<Runnable> refreshers) {
        return GuiPages.pane(mv, refreshers, GuiPages::dashHtml);
    }
}
