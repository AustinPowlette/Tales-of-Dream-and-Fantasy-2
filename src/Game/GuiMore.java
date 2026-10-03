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
        JDialog bw0 = GuiWindows.window(mv, "backpack", "Backpack & equipment", 820, 600, 1500, (d, refreshers) -> {
            JTabbedPane tabs = new JTabbedPane();
            tabs.setFont(GuiWindows.MONO_B);
            d.getRootPane().putClientProperty("tabs", tabs);

            // ---- gear
            JTextArea worn = area("");
            worn.setRows(5);
            DefaultTableModel gm = GuiWindows.model();
            gm.setColumnIdentifiers(new String[]{"#", "Item", "Kind", "Lvl", "Enchant", "Fight lvl if worn"});
            JTable gt = GuiWindows.table(gm);
            gt.getColumnModel().getColumn(0).setMaxWidth(40);
            JLabel hint = GuiWindows.label("Select an item to compare it with what you wear.");
            JPanel btns = GuiWindows.flow(FlowLayout.LEFT);
            btns.add(mv.makeBtn("Equip selected", () -> { int r = gt.getSelectedRow(); if (r >= 0) mv.queueInput("equip " + (r + 1)); }));
            btns.add(mv.makeBtn("Equip best", () -> mv.queueInput("equip")));
            btns.add(mv.makeBtn("Sell selected", () -> { int r = gt.getSelectedRow(); if (r >= 0) mv.queueInput("sell " + (r + 1)); }));
            btns.add(mv.makeBtn("Sell all (city)", () -> mv.queueInput("sell")));
            btns.add(mv.makeBtn("Consume pills", () -> mv.queueInput("consume")));
            JPanel south = new JPanel(new BorderLayout()); south.setBackground(BG);
            south.add(hint, BorderLayout.NORTH); south.add(btns, BorderLayout.CENTER);
            JPanel gear = new JPanel(new BorderLayout(4, 4)); gear.setBackground(BG);
            gear.add(worn, BorderLayout.NORTH);
            gear.add(new JScrollPane(gt), BorderLayout.CENTER);
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

            // ---- stores
            JTextArea stores = area("");
            tabs.addTab("Materials, pills, arrays", new JScrollPane(stores));

            // ---- goods: food, wine, poison, music
            DefaultTableModel om = GuiWindows.model();
            om.setColumnIdentifiers(new String[]{"#", "Goods", "Kind", "Lvl", "Qty"});
            JTable ot = GuiWindows.table(om);
            ot.getColumnModel().getColumn(0).setMaxWidth(40);
            JLabel cond = GuiWindows.label(" ");
            JPanel ob = GuiWindows.flow(FlowLayout.LEFT);
            ob.add(mv.makeBtn("Eat", () -> { int r = ot.getSelectedRow(); mv.queueInput(r >= 0 ? "eat " + (r + 1) : "eat"); }));
            ob.add(mv.makeBtn("Drink", () -> { int r = ot.getSelectedRow(); mv.queueInput(r >= 0 ? "drink " + (r + 1) : "drink"); }));
            ob.add(mv.makeBtn("Make food", () -> mv.queueInput("make food")));
            ob.add(mv.makeBtn("Make wine", () -> mv.queueInput("make wine")));
            ob.add(mv.makeBtn("Make poison", () -> mv.queueInput("make poison")));
            ob.add(mv.makeBtn("Make instrument", () -> mv.queueInput("make instrument")));
            ob.add(mv.makeBtn("Play", () -> mv.queueInput("play")));
            ob.add(mv.makeBtn("Coat blade", () -> mv.queueInput("coat")));
            JPanel ogoods = new JPanel(new BorderLayout(4, 4)); ogoods.setBackground(BG);
            ogoods.add(new JScrollPane(ot), BorderLayout.CENTER);
            JPanel osouth = new JPanel(new BorderLayout()); osouth.setBackground(BG);
            osouth.add(cond, BorderLayout.NORTH); osouth.add(ob, BorderLayout.CENTER);
            ogoods.add(osouth, BorderLayout.SOUTH);
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
                if (p == null) { worn.setText("No player character."); return; }
                Item w = p.equippedWeapon, a = p.equippedClothing;
                String t = "Weapon: " + (w == null ? "none" : w) + "\nArmor:  " + (a == null ? "none" : a)
                        + "\nFighting level " + TextTools.power(p, w, a) + "  (level " + p.lvl + " + gear + techniques, divided by 5)   Wealth " + p.ss + " ss";
                if (!worn.getText().equals(t)) worn.setText(t);
                int sel = gt.getSelectedRow();
                List<Object[]> rows = new ArrayList<>();
                List<Item> bag = new ArrayList<>(p.backpack);
                int base = TextTools.power(p, w, a);
                for (int i = 0; i < bag.size(); i++) {
                    Item it = bag.get(i);
                    boolean wear = it.itemType == Item.ItemType.Weapon || it.itemType == Item.ItemType.Clothing;
                    String after = "";
                    if (wear) { int pw = it.itemType == Item.ItemType.Weapon ? TextTools.power(p, it, a) : TextTools.power(p, w, it); after = pw + (pw > base ? "  ▲" : pw < base ? "  ▼" : "  ="); }
                    rows.add(new Object[]{i + 1, it.name, it.itemType == null ? "?" : it.itemType.name(), it.lvl, it.enchantment == null ? "" : it.enchantment + " +" + it.enchantBonus, after});
                }
                setRows(gm, rows);
                if (sel >= 0 && sel < gm.getRowCount()) gt.setRowSelectionInterval(sel, sel);
                List<Object[]> trows = new ArrayList<>();
                Object[][] slots = {{"Martial", p.martialTechnique}, {"Martial 2", p.martialTechnique2}, {"Movement", p.movementTechnique}, {"Movement 2", p.movementTechnique2}};
                for (Object[] s : slots) { Technique q = (Technique) s[1]; if (q != null) trows.add(new Object[]{s[0], q.name, q.getType(), q.lvl, q.potential, mastery(q), q.maker}); }
                for (Technique q : new ArrayList<>(p.techniqueList)) trows.add(new Object[]{"known", q.name, q.getType(), q.lvl, q.potential, mastery(q), q.maker});
                setRows(tm, trows);
                StringBuilder sb = new StringBuilder();
                sb.append("Materials: ").append(p.materialBag).append("\n\nPills: ").append(p.pillBag).append("\n\nArrays: ").append(p.arrayBag).append("\n");
                if (!stores.getText().equals(sb.toString())) stores.setText(sb.toString());
                int osel = ot.getSelectedRow();
                List<Object[]> orows = new ArrayList<>();
                List<Item> goodsNow = p.goods == null ? new ArrayList<Item>() : new ArrayList<>(p.goods);
                for (int i = 0; i < goodsNow.size(); i++) { Item it = goodsNow.get(i); orows.add(new Object[]{i + 1, it.name, it.itemType == null ? "?" : it.itemType.name(), it.lvl, it.quantity}); }
                setRows(om, orows);
                if (osel >= 0 && osel < om.getRowCount()) ot.setRowSelectionInterval(osel, osel);
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
            gt.getSelectionModel().addListSelectionListener(e -> {
                int r = gt.getSelectedRow();
                if (r < 0 || e.getValueIsAdjusting()) return;
                Object after = gm.getValueAt(r, 5);
                Character pl0 = GuiWindows.player(mv);
                if (pl0 != null && r < pl0.backpack.size() && Settings.pictures) hint.setIcon(new ImageIcon(ArtMore.itemIcon(pl0.backpack.get(r), 48)));
                hint.setText(after == null || after.toString().isEmpty() ? "Not wearable gear." : "Wearing it makes your fighting level " + after + "  (now " + (worn.getText().replaceAll("(?s).*Fighting level (\\d+).*", "$1")) + ").");
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
            refresh.addActionListener(e -> World.read(200, load));
            query.addActionListener(e -> World.read(200, load));
            kind.addActionListener(e -> World.read(200, load));
            mine.addActionListener(e -> World.read(200, load));
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

            JTextArea legends = area("");
            tabs.addTab("Hall of legends", new JScrollPane(legends));

            refreshers.add(() -> {
                if (signature[0] != Chronicle.log.size()) load.run();
                StringBuilder sb = new StringBuilder("Cultivators who reached Ascension or beyond are remembered here.\n\n");
                List<String> titles = new ArrayList<>(Game.Map.legacyTitles);
                List<Integer> lvls = new ArrayList<>(Game.Map.legacyLvls);
                for (int i = 0; i < titles.size(); i++) sb.append(String.format("  %-34s level %d%n", titles.get(i), i < lvls.size() ? lvls.get(i) : 0));
                if (titles.isEmpty()) sb.append("  (none yet)\n");
                sb.append("\nFirsts and legends from the chronicle:\n");
                List<Chronicle.Entry> lg = Chronicle.search("", "legend", 100);
                java.util.Collections.reverse(lg);
                for (Chronicle.Entry e : lg) sb.append("  ").append(e).append("\n");
                if (lg.isEmpty()) sb.append("  (none yet)\n");
                String s = sb.toString();
                if (!legends.getText().equals(s)) legends.setText(s);
            });
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
            JTextArea story = area("");
            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, charts, new JScrollPane(story));
            split.setResizeWeight(0.58);
            refreshers.add(() -> {
                Character c = Character.charById(id);
                List<int[]> life = Chronicle.bioOf(id);
                lvl.setSeries(new String[]{"Level"}, life, new int[]{1}, new Color[]{Theme.ACCENT}, false);
                wealth.setSeries(new String[]{"Wealth"}, life, new int[]{2}, new Color[]{Theme.GOLD}, false);
                social.setSeries(new String[]{"Friends", "Kills"}, life, new int[]{3, 4}, new Color[]{Theme.OK, Theme.DANGER}, false);
                StringBuilder sb = new StringBuilder();
                if (c != null) {
                    sb.append(c.name).append(" ").append(c.lastName).append(c.dead ? "  (dead)" : "").append("\nAge ").append(c.age).append(", level ").append(c.lvl).append(", ").append(c.kills).append(" kills");
                    if (c.sect != null) sb.append(", ").append(c.sect.name);
                    sb.append("\nPotential ").append(c.player == 1 ? c.potentialLabel() : String.valueOf(c.potential)).append("\n\n");
                }
                if (life.size() < 2) sb.append("(the charts fill in as game years pass; they are recorded for player characters and followed cultivators)\n\n");
                List<Chronicle.Entry> mine = c == null ? new ArrayList<>() : Chronicle.about(c);
                sb.append("Life events:\n");
                for (Chronicle.Entry e : mine) sb.append("  ").append(e).append("\n");
                if (mine.isEmpty()) sb.append("  (nothing notable yet)\n");
                String s = sb.toString();
                if (!story.getText().equals(s)) story.setText(s);
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
        GuiWindows.window(mv, "digest", "What happened lately", 820, 560, 5000, (d, refreshers) -> {
            JTextArea ta = area("");
            JPanel btns = GuiWindows.flow(FlowLayout.LEFT);
            for (int w : new int[]{13, 52, 260}) btns.add(mv.makeBtn(w == 13 ? "Season" : w == 52 ? "Year" : "5 years", () -> { range[0] = w; }));
            JCheckBox auto = new JCheckBox("Show when a saved game opens", Settings.digestOnLoad);
            auto.setBackground(BG); auto.setForeground(FG);
            auto.addActionListener(e -> { Settings.digestOnLoad = auto.isSelected(); Settings.save(); });
            btns.add(auto);
            JPanel p = new JPanel(new BorderLayout(4, 4)); p.setBackground(BG);
            p.add(new JScrollPane(ta), BorderLayout.CENTER); p.add(btns, BorderLayout.SOUTH);
            range[0] = weeks;
            refreshers.add(() -> { String s = TextTools.digest(range[0]); if (!ta.getText().equals(s)) { ta.setText(s); ta.setCaretPosition(0); } });
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
        GuiWindows.window(mv, "run", "Run statistics", 920, 560, 4000, (d, refreshers) -> {
            JTextArea ta = area("");
            JPanel btns = GuiWindows.flow(FlowLayout.LEFT);
            btns.add(mv.makeBtn("Export CSV", () -> mv.queueInput("export run")));
            btns.add(mv.makeBtn("Hall of fame", () -> mv.queueInput("halloffame")));
            btns.add(mv.makeBtn("Retire run…", () -> { if (GuiExtras.confirm(d, "Record this run in the hall of fame? The game goes on.")) mv.queueInput("!retire"); }));
            JPanel p = new JPanel(new BorderLayout(4, 4)); p.setBackground(BG);
            p.add(new JScrollPane(ta), BorderLayout.CENTER); p.add(btns, BorderLayout.SOUTH);
            refreshers.add(() -> { String s = RunStats.text(); if (!ta.getText().equals(s)) { int pos = ta.getCaretPosition(); ta.setText(s); ta.setCaretPosition(Math.min(pos, s.length())); } });
            return p;
        });
    }

    // ================================================================== city / sect details
    static void placeWindow(MapViewer mv, Area a) {
        if (a == null || (!a.city && a.sect == null)) return;
        final int ax = a.x, ay = a.y;
        GuiWindows.window(mv, "place" + ax + "_" + ay, "Details — " + (a.city ? a.name : a.sect.name), 780, 640, 3000, (d, refreshers) -> {
            JTextArea ta = area("");
            JPanel btns = GuiWindows.flow(FlowLayout.LEFT);
            btns.add(mv.makeBtn("Go there", () -> mv.queueInput("goto " + ax * 100 + " " + ay * 100)));
            btns.add(mv.makeBtn("Route preview", () -> mv.queueInput("route " + ax * 100 + " " + ay * 100)));
            btns.add(mv.makeBtn("Open lord / master", () -> {
                Area t = Game.Map.tiles[ax][ay];
                Character who = t == null ? null : t.cityOwner != null ? t.cityOwner : t.sect != null ? t.sect.master : null;
                if (who != null) mv.showCharacterDialog(who);
            }));
            JPanel p = new JPanel(new BorderLayout(4, 4)); p.setBackground(BG);
            p.add(new JScrollPane(ta), BorderLayout.CENTER); p.add(btns, BorderLayout.SOUTH);
            refreshers.add(() -> {
                Area t = Game.Map.tiles[ax][ay];
                String s = t == null ? "Unknown place." : TextTools.placeDetails(t, null);
                if (!ta.getText().equals(s)) { int pos = ta.getCaretPosition(); ta.setText(s); ta.setCaretPosition(Math.min(pos, s.length())); }
            });
            return p;
        });
    }

    // ================================================================== dashboard tab (Rankings)
    static JComponent dashboardPanel(List<Runnable> refreshers) {
        JTextArea a = area("");
        a.setFont(Theme.mono(13));
        refreshers.add(() -> a.setText(TextTools.dashText(TextTools.dash())));
        return new JScrollPane(a);
    }
}
