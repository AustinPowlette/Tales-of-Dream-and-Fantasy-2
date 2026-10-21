package Game;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** The one place for items you carry or can buy, on the item cards of `GuiItems`: three sub-tabs with the same filters and sort as the terminal `list` command.
 *  *Mine*: your backpack, with the change in your fighting level that wearing a piece would make; Equip, Sell, Gift, Consume.
 *  *For sale here*: the stock of the shops of the city you stand in; Buy.
 *  *Sell here*: your backpack priced by the shops of this city against what the city pool pays; Sell, and a toggle that keeps only the items a shop pays more for than the pool.
 *  Replaces the Browser's two text tables (Shop items, Backpack) and gives the Market the shops. Every action is a command; nothing here touches game state. */
final class GuiItemList {
    private GuiItemList() { }

    enum Mode { MINE, SHOP, SELL }

    /** One component, one refresher (it refreshes the sub-tab that is showing). */
    static JComponent create(final MapViewer mv, final List<Runnable> refreshers) {
        final JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(GuiWindows.MONO_B);
        final List<Runnable> inner = new ArrayList<>();
        tabs.addTab("Mine", pane(mv, Mode.MINE, inner));
        tabs.addTab("For sale here", pane(mv, Mode.SHOP, inner));
        tabs.addTab("Sell here", pane(mv, Mode.SELL, inner));
        final Runnable showing = () -> { int i = tabs.getSelectedIndex(); if (i >= 0 && i < inner.size()) inner.get(i).run(); };
        tabs.addChangeListener(e -> showing.run());
        refreshers.add(showing);
        return tabs;
    }

    private static JComponent pane(final MapViewer mv, final Mode mode, final List<Runnable> inner) {
        final JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBackground(GuiWindows.BG);
        final JTextField name = new JTextField(9), lvlMin = new JTextField(3), lvlMax = new JTextField(3), priceMax = new JTextField(7);
        final JComboBox<String> type = new JComboBox<>(new String[]{"any", "weapon", "clothing", "pill", "array", "herb", "metal", "blood"});
        final JCheckBox afford = new JCheckBox("affordable");
        afford.setBackground(GuiWindows.BG); afford.setForeground(GuiWindows.FG);
        final java.util.Set<String> keys = mode == Mode.SHOP ? PlayerLists.ITEM_KEYS : PlayerLists.BAG_KEYS;
        final JComboBox<String> sort = new JComboBox<>();
        sort.addItem("(default)");
        for (String k : keys) sort.addItem(k);
        final JCheckBox desc = new JCheckBox("desc");
        desc.setBackground(GuiWindows.BG); desc.setForeground(GuiWindows.FG);
        Character p0 = mv.findPlayer();
        final JSpinner limit = new JSpinner(new SpinnerNumberModel(Math.max(20, Math.min(ListQuery.MAX_LIMIT, p0 != null && p0.pc != null ? p0.pc.listLimit : 20)), 1, ListQuery.MAX_LIMIT, 5));
        final JToggleButton good = new JToggleButton("Only good prices");
        good.setToolTipText("Show only the items that a shop of this city pays more for than the city pool does.");
        final JLabel status = GuiWindows.label(" ");
        status.setForeground(GuiWindows.ACCENT);

        JPanel filters = GuiWindows.flow(FlowLayout.LEFT);
        filters.add(GuiWindows.label("name")); filters.add(name);
        filters.add(GuiWindows.label("type")); filters.add(type);
        filters.add(GuiWindows.label("lvl ≥")); filters.add(lvlMin); filters.add(GuiWindows.label("≤")); filters.add(lvlMax);
        filters.add(GuiWindows.label("price ≤")); filters.add(priceMax);
        if (mode == Mode.SHOP) filters.add(afford);
        filters.add(GuiWindows.label("sort")); filters.add(sort); filters.add(desc);
        filters.add(GuiWindows.label("limit")); filters.add(limit);
        if (mode == Mode.SELL) filters.add(good);

        final DefaultListModel<GuiItems.Entry> model = new DefaultListModel<>();
        final JList<GuiItems.Entry> list = GuiItems.list(model);
        final List<Object> refs = new ArrayList<>();   // parallel to the model: the Item (Mine, Sell) or ShopItem (For sale), null for a header
        final boolean[] busy = {false};

        final Runnable body = () -> {
            Character p = mv.findPlayer();
            if (p == null || p.location == null || p.pc == null) { status.setText("No player character."); return; }
            List<String> tok = new ArrayList<>();
            tok.add(String.valueOf(limit.getValue()));
            if (sort.getSelectedIndex() > 0) tok.add("sort=" + sort.getSelectedItem() + (desc.isSelected() ? ":desc" : ""));
            else if (desc.isSelected()) tok.add("desc");
            for (String w : name.getText().trim().split("\\s+")) if (!w.isEmpty()) tok.add("name=" + w);
            if (!lvlMin.getText().trim().isEmpty()) tok.add("lvl>=" + lvlMin.getText().trim());
            if (!lvlMax.getText().trim().isEmpty()) tok.add("lvl<=" + lvlMax.getText().trim());
            if (type.getSelectedIndex() > 0) tok.add("type=" + type.getSelectedItem());
            if (!priceMax.getText().trim().isEmpty()) tok.add("price<=" + priceMax.getText().trim());
            if (mode == Mode.SHOP && afford.isSelected()) tok.add("afford=1");
            PlayerLists.ListView v;
            try {
                ListQuery q = ListQuery.parse(tok.toArray(new String[0]), 0);
                v = mode == Mode.SHOP ? PlayerLists.shopItems(p, q, p.pc.listLimit) : PlayerLists.bag(p, q, p.pc.listLimit);
            } catch (RuntimeException ex) { return; } // the game thread changed a list mid-copy: the next refresh will succeed
            if (v.error != null) { status.setText(v.error); return; }
            List<GuiItems.Entry> rows = new ArrayList<>();
            List<Object> newRefs = new ArrayList<>();
            boolean inCity = p.location.city;
            if (mode != Mode.MINE && !inCity) {
                rows.add(GuiItems.Entry.header("You are not in a city: shops stand in cities ('Places' lists the ones you know)")); newRefs.add(null);
            } else if (mode == Mode.SHOP && p.location.shops.isEmpty()) {
                rows.add(GuiItems.Entry.header("This city has no shops")); newRefs.add(null);
            } else {
                int base = TextTools.power(p, p.equippedWeapon, p.equippedClothing);
                java.util.ArrayList<Shop> buyers = new java.util.ArrayList<>(p.location.shops);
                buyers.sort((x, y) -> Double.compare(y.buyRate, x.buyRate));
                int shown = 0;
                for (int i = 0; i < v.refs.size(); i++) {
                    Object ref = v.refs.get(i);
                    if (mode == Mode.SHOP) {
                        PlayerLists.ShopItem si = (PlayerLists.ShopItem) ref;
                        GuiItems.Entry e = new GuiItems.Entry(si.item);
                        long price = si.shop.sellPrice(si.item);
                        e.badge = TermFormat.n(price) + " ss"; e.badgeColor = p.canAfford(price) ? Theme.OK : Theme.DANGER;
                        e.note = String.valueOf(v.rows.get(i)[6]) + (si.item.quantity > 1 ? "   ×" + si.item.quantity + " in stock" : "");
                        rows.add(e); newRefs.add(si); shown++;
                    } else {
                        Item it = (Item) ref;
                        GuiItems.Entry e = new GuiItems.Entry(it);
                        e.index = p.backpack.indexOf(it); e.makerShown = true;
                        if (mode == Mode.MINE) {
                            boolean wear = it.itemType == Item.ItemType.Weapon || it.itemType == Item.ItemType.Clothing;
                            if (wear) {
                                int pw = it.itemType == Item.ItemType.Weapon ? TextTools.power(p, it, p.equippedClothing) : TextTools.power(p, p.equippedWeapon, it);
                                e.badge = pw > base ? "▲ " + pw : pw < base ? "▼ " + pw : "= " + pw;
                                e.badgeColor = pw > base ? Theme.OK : pw < base ? Theme.DANGER : Theme.DIM;
                                e.note = "wearing it makes your fighting level " + pw + " (now " + base + ")";
                            } else if (it.stackable || it.quantity > 1) { e.badge = "×" + it.quantity; e.badgeColor = Theme.TEXT; }
                            if (e.note.isEmpty()) e.note = "worth " + TermFormat.n(Character.itemPrice(it)) + " ss";
                        } else {
                            long pool = Character.itemPrice(it) / 2, best = pool, shopPays = 0; Shop bestShop = null;
                            for (Shop s : buyers) if (s.owner != p && s.buyRate >= 0.5) { shopPays = s.buyPrice(it); bestShop = s; break; }
                            boolean better = bestShop != null && shopPays > pool;
                            if (good.isSelected() && !better) continue;
                            if (bestShop != null) best = Math.max(pool, shopPays);
                            e.badge = TermFormat.n(best) + " ss"; e.badgeColor = better ? Theme.OK : Theme.DIM;
                            e.note = (bestShop != null ? (bestShop.owner != null ? bestShop.owner.name + "'s shop pays " : "A shop pays ") + TermFormat.n(shopPays) + "   ·   " : "") + "the city pool pays " + TermFormat.n(pool);
                        }
                        rows.add(e); newRefs.add(it); shown++;
                    }
                }
                if (rows.isEmpty()) {
                    rows.add(GuiItems.Entry.header(mode == Mode.MINE ? "Nothing in your backpack matches (materials and pills are in the Backpack window's other tabs)" : mode == Mode.SHOP ? "No shop item matches" : good.isSelected() ? "No item of yours gets a better price from a shop than from the pool" : "Nothing to sell"));
                    newRefs.add(null);
                }
                status.setText((mode == Mode.SHOP ? "showing " + shown + " of " + v.total + " items for sale" : mode == Mode.SELL ? shown + " of your items" : shown + " of " + v.total + " items") + "   —   your wealth " + TermFormat.n(PlayerLists.wealth(p)) + " ss");
            }
            if (rows.size() == 1 && newRefs.get(0) == null && mode != Mode.MINE && !inCity) status.setText("You are not in a city.");
            // keep the selection through the periodic refresh
            Object sel = list.getSelectedIndex() >= 0 && list.getSelectedIndex() < refs.size() ? refs.get(list.getSelectedIndex()) : null;
            refs.clear(); refs.addAll(newRefs);
            GuiItems.fill(list, model, rows);
            if (sel != null) for (int i = 0; i < refs.size(); i++) if (refs.get(i) == sel && list.getSelectedIndex() != i) { list.setSelectedIndex(i); break; }
        };
        final Runnable run = () -> { if (busy[0]) return; busy[0] = true; try { body.run(); } finally { busy[0] = false; } };
        final Runnable refresh = () -> World.read(80, run);
        inner.add(refresh);
        java.awt.event.ActionListener again = e -> refresh.run();
        for (JTextField f : new JTextField[]{name, lvlMin, lvlMax, priceMax}) f.addActionListener(again);
        type.addActionListener(again); sort.addActionListener(again); desc.addActionListener(again); afford.addActionListener(again); good.addActionListener(again);
        limit.addChangeListener(e -> refresh.run());

        final java.util.function.Supplier<Object> selected = () -> {
            int r = list.getSelectedIndex();
            if (r < 0 || r >= refs.size() || refs.get(r) == null) { status.setText("Select an item first."); return null; }
            return refs.get(r);
        };
        final java.util.function.Function<Item, Integer> slot = it -> { Character p = mv.findPlayer(); return p == null ? -1 : p.backpack.indexOf(it) + 1; };

        JPanel actions = GuiWindows.flow(FlowLayout.LEFT);
        actions.add(mv.makeBtn("Refresh", "Reload the list now.", refresh));
        if (mode == Mode.MINE) {
            actions.add(mv.makeBtn("Equip selected", "Wear the selected weapon or clothes (the old ones go back to the bag).", () -> { Object o = selected.get(); if (o instanceof Item && slot.apply((Item) o) > 0) mv.queueInput("equip " + slot.apply((Item) o)); }));
            actions.add(mv.makeBtn("Equip best", "Wear the strongest weapon and clothes you carry.", () -> mv.queueInput("equip")));
            actions.add(mv.makeBtn("Sell selected", "Sell the selected item in this city (to the shop that pays most, else the city pool).", () -> { Object o = selected.get(); if (o instanceof Item && slot.apply((Item) o) > 0) mv.queueInput("sell " + slot.apply((Item) o)); }));
            actions.add(mv.makeBtn("Gift selected…", "Give the selected item to someone in your tile (friendship grows once a week per person).", () -> {
                Object o = selected.get();
                if (!(o instanceof Item) || slot.apply((Item) o) <= 0) return;
                String s = JOptionPane.showInputDialog(panel, "Give it to which person in your tile? (#id, or the number from People here)", "Gift", JOptionPane.PLAIN_MESSAGE);
                if (s == null || s.trim().isEmpty()) return;
                s = s.trim();
                mv.queueInput("gift " + (s.matches("\\d{3,}") ? "#" + s : s) + " " + slot.apply((Item) o));
            }));
            actions.add(mv.makeBtn("Consume pills", "Swallow the pills you carry that can be used now.", () -> mv.queueInput("consume")));
        } else if (mode == Mode.SHOP) {
            JButton buy = mv.makeBtn("Buy selected", "Buy the selected item at the shown price.", () -> {
                Object o = selected.get();
                if (o instanceof PlayerLists.ShopItem) { PlayerLists.ShopItem si = (PlayerLists.ShopItem) o; mv.queueInput("buy " + si.shopNo + "." + si.itemNo + " " + si.item.name); }
            });
            buy.setForeground(Theme.OK);
            actions.add(buy);
            actions.add(mv.makeBtn("Buy gear", "Buy the best weapon and armour the shops here can sell you for what you can afford.", () -> mv.queueInput("buygear")));
        } else {
            JButton sell = mv.makeBtn("Sell selected", "Sell the selected item here: the shop that pays most buys it, else the city pool.", () -> { Object o = selected.get(); if (o instanceof Item && slot.apply((Item) o) > 0) mv.queueInput("sell " + slot.apply((Item) o)); });
            sell.setForeground(Theme.OK);
            actions.add(sell);
            actions.add(mv.makeBtn("Sell all (city)", "Sell everything that is not equipped and not your one spare of each kind.", () -> mv.queueInput("sell")));
        }
        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(GuiWindows.BG);
        south.add(actions, BorderLayout.NORTH);
        south.add(status, BorderLayout.SOUTH);
        panel.add(filters, BorderLayout.NORTH);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }
}
