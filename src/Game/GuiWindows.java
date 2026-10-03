package Game;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The player-facing windows of the GUI (quest panel, duties & policies, manage shops/city/sect, rankings & charts, market).
 *  Every window only READS game state (snapshots, tolerant of the game thread changing things) and sends actions as ordinary commands
 *  through MapViewer.queueInput, so the game thread stays the only writer. Each is a singleton: opening it again raises it. */
public class GuiWindows {
    static final Color BG = Theme.PANEL, FG = Theme.TEXT, DIM = Theme.DIM, ACCENT = Theme.ACCENT;
    static final Font MONO = Theme.plain(14), MONO_B = Theme.bold(14);
    private static final Map<String, JDialog> open = new HashMap<>();

    interface Builder { JComponent build(JDialog d, List<Runnable> refreshers); }

    /** Opens (or raises) a singleton window; `refreshers` run once at open and then every `everyMs`. */
    static JDialog window(MapViewer mv, String key, String title, int w, int h, int everyMs, Builder b) {
        JDialog old = open.get(key);
        if (old != null && old.isDisplayable()) { old.setVisible(true); old.toFront(); old.requestFocus(); return old; }
        JDialog d = new JDialog(mv, title, false);
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); // closing with the X must really close it (a hidden window could not be reopened by its button)
        d.getContentPane().setBackground(BG);
        List<Runnable> refreshers = new ArrayList<>();
        JComponent c = b.build(d, refreshers);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BG);
        wrap.add(GuiHub.header(mv, key, title), BorderLayout.NORTH); // what the window is for + the "?" that opens its Help section
        wrap.add(c, BorderLayout.CENTER);
        d.add(wrap);
        Runnable all = () -> World.read(80, () -> { for (Runnable r : refreshers) { try { r.run(); } catch (RuntimeException ignored) { /* mid-tick read: next refresh fixes it */ } } });
        Timer t = new Timer(everyMs, e -> all.run());
        d.addWindowListener(new java.awt.event.WindowAdapter() { @Override public void windowClosed(java.awt.event.WindowEvent e) { t.stop(); open.remove(key); } });
        d.setSize(w, h);
        d.setLocationRelativeTo(mv);
        GuiHub.restoreBounds(d, key);
        GuiHub.trackBounds(d, key);
        Ui.prepare(d);
        open.put(key, d);
        d.setVisible(true);
        all.run();
        t.start();
        return d;
    }

    /** The open window with this key, or null. */
    static JDialog find(String key) { JDialog d = open.get(key); return d != null && d.isDisplayable() ? d : null; }

    static JLabel label(String s) { JLabel l = new JLabel(s); l.setForeground(FG); l.setFont(MONO); return l; }
    static JPanel flow(int align) { JPanel p = new JPanel(new FlowLayout(align, 5, 3)); p.setBackground(BG); return p; }

    static JTable table(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setFont(MONO); t.setRowHeight(20);
        t.setBackground(Theme.FIELD); t.setForeground(FG);
        t.setSelectionBackground(Theme.SELECT);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setFont(MONO_B);
        return t;
    }

    /** A read-only model; the column class comes from the first value, so a sortable table orders numbers as numbers. */
    static DefaultTableModel model() {
        return new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { for (int r = 0; r < getRowCount(); r++) { Object v = getValueAt(r, c); if (v != null) return v.getClass(); } return Object.class; }
        };
    }

    /** Replaces the rows without a structure change, so a sorted or filtered table keeps its sort and filter across refreshes. */
    static void fillRows(DefaultTableModel m, String[] cols, List<Object[]> rows) {
        if (m.getColumnCount() != cols.length) m.setColumnIdentifiers(cols);
        m.setRowCount(0);
        for (Object[] r : rows) m.addRow(r);
    }

    private static final class OnChange implements javax.swing.event.DocumentListener {
        final Runnable r; OnChange(Runnable r) { this.r = r; }
        @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
        @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
    }

    private static long lnum(String t, long def) { try { return Long.parseLong(t.trim()); } catch (RuntimeException e) { return def; } }

    // the quality-of-life windows live in GuiExtras; these are the entry points the game code calls
    static void tutorialWindow(MapViewer mv) { GuiExtras.tutorialWindow(mv); }
    static void recapWindow(MapViewer mv, String title, String body) { GuiExtras.recapWindow(mv, title, body); }
    static String startupSaveMenu(MapViewer mv, List<SaveManager.Slot> slots) { return GuiExtras.startupSaveMenu(mv, slots); }

    static Character player(MapViewer mv) { Character p = mv.findPlayer(); return p != null && p.pc != null ? p : null; }

    // ================================================================== quest log
    /** One quest window for the whole GUI: see GuiQuests (this entry point is kept for the callers that still name questPanel). */
    static void questPanel(MapViewer mv) { GuiQuests.open(mv, null); }

    // ================================================================== duties & policies
    static void dutiesPanel(MapViewer mv) { GuiDuties.open(mv, null); }

    // ================================================================== manage: shops / city / sect
    static void managePanel(MapViewer mv) {
        window(mv, "manage", "Manage — shops, city, sect", 900, 600, 1500, (d, refreshers) -> {
            JTabbedPane tabs = new JTabbedPane();
            tabs.setFont(MONO_B);
            tabs.addTab("My shops", shopsTab(mv, refreshers));
            tabs.addTab("City here", cityTab(mv, refreshers));
            tabs.addTab("Sect", sectTab(mv, refreshers));
            return tabs;
        });
    }

    private static JComponent shopsTab(MapViewer mv, List<Runnable> refreshers) {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBackground(BG);
        DefaultTableModel model = model();
        JTable table = table(model);
        JSlider buy = new JSlider(5, 100, 55), sell = new JSlider(105, 500, 150), city = new JSlider(5, 100, 30);
        JLabel buyL = label(" "), sellL = label(" "), cityL = label(" "), status = label(" ");
        status.setForeground(ACCENT);
        for (JSlider s : new JSlider[]{buy, sell, city}) { s.setBackground(BG); s.setForeground(FG); s.setPreferredSize(new Dimension(260, 30)); }
        buy.addChangeListener(e -> buyL.setText(String.format("pay sellers %.2fx", buy.getValue() / 100.0)));
        sell.addChangeListener(e -> sellL.setText(String.format("charge buyers %.2fx", sell.getValue() / 100.0)));
        city.addChangeListener(e -> cityL.setText(String.format("restock from city %.2fx", city.getValue() / 100.0)));
        final boolean[] loading = {false};
        final Shop[] selShop = {null};
        final List<Shop> shopRows = new ArrayList<>();
        Runnable load = () -> {
            int r = table.getSelectedRow();
            if (r < 0 || r >= shopRows.size()) return;
            Shop s = shopRows.get(r);
            selShop[0] = s;
            buy.setValue((int) Math.round(s.buyRate * 100)); sell.setValue((int) Math.round(s.sellRate * 100)); city.setValue((int) Math.round(s.cityBuyRate * 100));
        };
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) load.run(); });
        refreshers.add(() -> {
            Character c = player(mv);
            if (c == null) return;
            List<Shop> shops = new ArrayList<>(c.ownedShops);
            Shop keep = selShop[0];
            shopRows.clear(); shopRows.addAll(shops);
            List<Object[]> rows = new ArrayList<>();
            for (int i = 0; i < shops.size(); i++) { Shop s = shops.get(i); rows.add(new Object[]{i + 1, s.location.name, s.level(), s.inventory.size(), String.format("%.2f", s.buyRate), String.format("%.2f", s.sellRate), String.format("%.2f", s.cityBuyRate)}); }
            model.setDataVector(rows.toArray(new Object[0][]), new String[]{"#", "City", "Lv", "Stock", "Pays", "Charges", "Restock"});
            int i = keep == null ? -1 : shops.indexOf(keep);
            if (i >= 0) table.setRowSelectionInterval(i, i);
            status.setText(shops.isEmpty() ? "You own no shops. Stand in a city and use 'Open / restock here' (merchants)." : shops.size() + " shop(s). Select one, move the sliders, press Apply.");
        });
        JPanel sliders = new JPanel(new GridLayout(3, 2, 4, 2));
        sliders.setBackground(BG);
        sliders.add(buy); sliders.add(buyL); sliders.add(sell); sliders.add(sellL); sliders.add(city); sliders.add(cityL);
        buy.setValue(55); sell.setValue(150); city.setValue(30);
        JPanel btns = flow(FlowLayout.LEFT);
        btns.add(mv.makeBtn("Apply rates", () -> {
            Shop s = selShop[0];
            int r = table.getSelectedRow();
            if (s == null || r < 0) { status.setText("Select a shop first."); return; }
            double nb = buy.getValue() / 100.0, ns = sell.getValue() / 100.0, nc = city.getValue() / 100.0;
            // order matters: the game keeps buy rate at least 0.05 below the sell rate
            if (ns >= s.sellRate) { mv.queueInput("price " + (r + 1) + " sell " + ns); mv.queueInput("price " + (r + 1) + " buy " + nb); }
            else { mv.queueInput("price " + (r + 1) + " buy " + nb); mv.queueInput("price " + (r + 1) + " sell " + ns); }
            mv.queueInput("price " + (r + 1) + " city " + nc);
        }));
        btns.add(mv.makeBtn("Open / restock here", () -> mv.queueInput("do Restock Trip")));
        btns.add(mv.makeBtn("Sell stock here", () -> mv.queueInput("do Sell Stock")));
        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(BG);
        south.add(sliders, BorderLayout.NORTH); south.add(btns, BorderLayout.CENTER); south.add(status, BorderLayout.SOUTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private static JComponent cityTab(MapViewer mv, List<Runnable> refreshers) {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBackground(BG);
        JTextArea info = new JTextArea();
        info.setEditable(false); info.setFont(MONO); info.setBackground(Theme.FIELD); info.setForeground(FG);
        refreshers.add(() -> {
            Character c = player(mv);
            if (c == null) return;
            Area a = c.location;
            StringBuilder sb = new StringBuilder();
            sb.append(a.name).append(' ').append(PlayerControl.coord(a)).append("\n");
            if (!a.city) sb.append("Not a city. A Major Lvl 2+ character can found one here ('Found / claim city').\n");
            else {
                sb.append("City level ").append(a.cityLevel()).append("   population ").append(a.population).append("\n");
                sb.append("Lord: ").append(a.cityOwner != null ? a.cityOwner.name + " " + a.cityOwner.lastName + (a.cityOwner == c ? "  (you)" : "") : "none").append("\n");
                sb.append("Treasury: ").append(a.cityTreasury).append(" ss   Shops ").append(a.shops.size()).append("/").append(a.maxShopSlots())
                  .append("   Libraries ").append(a.libraries.size()).append("   Arrays ").append(a.arrayBag == null ? 0 : a.arrayBag.size()).append("\n");
                sb.append("Crowd: ").append(a.characters.size()).append(a.isOriginis() ? " (Originis is exempt from crowd hotspots)" : " / " + a.crowdCapacity()).append(a.crowdHotspot ? "  — OVERCROWDED" : "").append("\n");
                if (a.tournamentTimer > 0) sb.append("Tournament: realms ").append(a.tournamentMinMajor).append("-").append(a.tournamentMaxMajor).append(", ").append(a.tournamentTimer).append(" weeks left, ").append(a.tournamentRegistrants.size()).append(" registered\n");
                sb.append("\nShops here:\n");
                for (Shop s : new ArrayList<>(a.shops)) sb.append("  ").append(s.name == null || s.name.isEmpty() ? (s.owner != null ? s.owner.name : "?") : s.name + " (" + (s.owner != null ? s.owner.name : "?") + ")").append("  Lv").append(s.level()).append("  stock ").append(s.inventory.size()).append("\n");
            }
            if (!info.getText().equals(sb.toString())) info.setText(sb.toString());
        });
        JPanel btns = flow(FlowLayout.LEFT);
        btns.add(mv.makeBtn("Found / claim city", () -> mv.queueInput("found city")));
        btns.add(mv.makeBtn("Buy city array", () -> mv.queueInput("do Buy City Array")));
        btns.add(mv.makeBtn("Hold tournament", () -> mv.queueInput("do Hold Tournament")));
        btns.add(mv.makeBtn("Entice merchant", () -> mv.queueInput("do Entice Merchant")));
        btns.add(mv.makeBtn("Register for tournament", () -> mv.queueInput("do Register Tournament")));
        p.add(new JScrollPane(info), BorderLayout.CENTER);
        p.add(btns, BorderLayout.SOUTH);
        return p;
    }

    private static JComponent sectTab(MapViewer mv, List<Runnable> refreshers) {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBackground(BG);
        JTextArea info = new JTextArea(6, 40);
        info.setEditable(false); info.setFont(MONO); info.setBackground(Theme.FIELD); info.setForeground(FG);
        DefaultTableModel model = model();
        JTable table = table(model);
        final List<Character> members = new ArrayList<>();
        refreshers.add(() -> {
            Character c = player(mv);
            if (c == null) return;
            Organization s = c.sect != null ? c.sect : c.location.sect;
            StringBuilder sb = new StringBuilder();
            List<Object[]> rows = new ArrayList<>();
            members.clear();
            if (s == null) sb.append("You belong to no sect and none is here. Major Lvl 2+ can found one ('Found sect'); otherwise stand in a sect's tile and press Join.\n");
            else {
                sb.append(s.name).append(c.sect == s ? "  (yours, " + s.getRank(c) + ")" : "  (not yours)").append("\n");
                sb.append("Master: ").append(s.master != null ? s.master.name + " " + s.master.lastName : s.isVirtual() ? "???" : "none").append("   members ").append(s.members.size()).append("   Sect Lvl ").append(s.sectLvl()).append("\n");
                sb.append("Home ").append(s.location.name).append(' ').append(PlayerControl.coord(s.location)).append("   treasury ").append(s.treasury.size()).append(" stacks, ").append(s.sectCash).append(" ss\n");
                sb.append("War: ").append(s.warTarget != null ? s.warTarget.name : "none").append("   allies ").append(s.allies.size()).append("\n");
                sb.append("Joining: ").append(s.rulesText()).append("\n");
                if (c.sect == s) sb.append("Your sect points: ").append(c.sectPoints).append(c.sectMission != null ? "   mission: " + c.sectMission : "").append("\n");
                for (Character m : new ArrayList<>(s.members)) {
                    if (m == null || m.dead) continue;
                    members.add(m);
                    rows.add(new Object[]{m.id, m.name + " " + m.lastName, m.lvl, s.getRank(m), m.sectPoints, PlayerControl.coord(m.location)});
                }
            }
            if (!info.getText().equals(sb.toString())) info.setText(sb.toString());
            model.setDataVector(rows.toArray(new Object[0][]), new String[]{"ID", "Member", "Lv", "Rank", "Points", "Where"});
        });
        JPanel btns = flow(FlowLayout.LEFT);
        JButton promote = mv.makeBtn("Promote selected", () -> { int r = table.getSelectedRow(); if (r >= 0 && r < members.size()) mv.queueInput("promote #" + members.get(r).id); });
        btns.add(promote);
        btns.add(mv.makeBtn("Recruit (random here)", () -> mv.queueInput("recruit")));
        btns.add(mv.makeBtn("Found sect", () -> mv.queueInput("found sect")));
        btns.add(mv.makeBtn("Join", () -> mv.queueInput("join")));
        btns.add(mv.makeBtn("Resign", () -> mv.queueInput("resign")));
        btns.add(mv.makeBtn("Recruitment rules…", () -> {
            JTextField pot = new JTextField("0", 4), lvl = new JTextField("0", 4), fam = new JTextField("any", 10);
            JComboBox<String> gen = new JComboBox<>(new String[]{"any", "male", "female"}), aff = new JComboBox<>(new String[]{"any", "good", "neutral", "evil"}), tr = new JComboBox<>();
            tr.addItem("any");
            for (Character.Traits x : Character.Traits.values()) if (x != Character.Traits.Beast && x != Character.Traits.Monster) tr.addItem(Character.traitName(x));
            JPanel form = new JPanel(new GridLayout(0, 2, 4, 4));
            form.add(new JLabel("Minimum potential")); form.add(pot); form.add(new JLabel("Minimum level")); form.add(lvl);
            form.add(new JLabel("Gender")); form.add(gen); form.add(new JLabel("Alignment")); form.add(aff);
            form.add(new JLabel("Craft (trait)")); form.add(tr); form.add(new JLabel("Family name")); form.add(fam);
            if (JOptionPane.showConfirmDialog(p, form, "Recruitment rules of your sect (master only)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
                mv.queueInput("sect rules potential " + pot.getText().trim() + " level " + lvl.getText().trim() + " gender " + gen.getSelectedItem() + " affiliation " + aff.getSelectedItem()
                        + " trait " + String.valueOf(tr.getSelectedItem()).replace(' ', '_') + " family " + (fam.getText().trim().isEmpty() ? "any" : fam.getText().trim()));
        }));
        JPanel btns2 = flow(FlowLayout.LEFT);
        btns2.add(mv.makeBtn("Donate to sect", () -> mv.queueInput("do Donate to Sect")));
        btns2.add(mv.makeBtn("Buy from sect", () -> mv.queueInput("do Buy from Sect")));
        btns2.add(mv.makeBtn("Take sect mission", () -> mv.queueInput("do Accept Sect Mission")));
        btns2.add(mv.makeBtn("Buy sect array", () -> mv.queueInput("do Buy Sect Array")));
        btns2.add(mv.makeBtn("Declare war here", () -> mv.queueInput("war")));
        btns2.add(mv.makeBtn("Peace", () -> mv.queueInput("peace")));
        btns2.add(mv.makeBtn("Propose alliance here", () -> mv.queueInput("alliance")));
        JPanel south = new JPanel(new GridLayout(2, 1));
        south.setBackground(BG);
        south.add(btns); south.add(btns2);
        p.add(new JScrollPane(info), BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    // ================================================================== rankings & charts
    static void rankingsPanel(MapViewer mv) {
        window(mv, "rankings", "Rankings & Charts", 1000, 640, 4000, (d, refreshers) -> {
            JTabbedPane tabs = new JTabbedPane();
            tabs.setFont(MONO_B);
            String[] names = {"Strongest", "Richest", "Most kills", "Oldest"};
            List<Character>[] lists = new List[names.length];
            DefaultTableModel[] models = new DefaultTableModel[names.length];
            JTable[] tables = new JTable[names.length];
            for (int i = 0; i < names.length; i++) {
                models[i] = model();
                tables[i] = table(models[i]);
                final int idx = i;
                tables[i].addMouseListener(new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2 && lists[idx] != null) {
                            int r = tables[idx].getSelectedRow();
                            if (r >= 0 && r < lists[idx].size()) mv.showCharacterDialog(lists[idx].get(r));
                        }
                    }
                });
                tabs.addTab(names[i], new JScrollPane(tables[i]));
            }
            ChartPanel pop = new ChartPanel("Population by year"), eco = new ChartPanel("Cities / sects / shops by year"), realms = new ChartPanel("Living cultivators by major realm");
            JPanel charts = new JPanel(new GridLayout(3, 1, 4, 4));
            charts.setBackground(BG);
            charts.add(pop); charts.add(eco); charts.add(realms);
            tabs.addTab("Charts", charts);
            tabs.addTab("Dashboard", GuiMore.dashboardPanel(refreshers));
            tabs.addTab("Rivalries", GuiMore.rivalriesPanel(refreshers));
            refreshers.add(() -> {
                List<Character> alive = new ArrayList<>();
                int n = Game.Map.characters.size();
                int[] realmCount = new int[11];
                for (int i = 0; i < n; i++) {
                    Character c = Game.Map.characters.get(i);
                    if (c == null || c.dead || !c.born || c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) continue;
                    alive.add(c);
                    realmCount[Math.max(0, Math.min(10, c.majorLvl))]++;
                }
                Comparator2 cmp = new Comparator2();
                List<List<Character>> tops = new ArrayList<>();
                tops.add(cmp.top(alive, (a, b) -> Integer.compare(b.lvl, a.lvl)));
                tops.add(cmp.top(alive, (a, b) -> Long.compare(PlayerLists.wealth(b), PlayerLists.wealth(a))));
                tops.add(cmp.top(alive, (a, b) -> Integer.compare(b.kills, a.kills)));
                tops.add(cmp.top(alive, (a, b) -> Integer.compare(b.age, a.age)));
                for (int i = 0; i < names.length; i++) {
                    lists[i] = tops.get(i);
                    List<Object[]> rows = new ArrayList<>();
                    int rank = 1;
                    for (Character c : lists[i]) rows.add(new Object[]{rank++, c.name + " " + c.lastName + (c.player >= 1 ? "  ★" : ""), c.lvl, c.majorLvl, c.age, c.kills, PlayerLists.wealth(c), c.sect != null ? c.sect.name : "-", c.location != null ? c.location.name : "?"});
                    int sel = tables[i].getSelectedRow();
                    models[i].setDataVector(rows.toArray(new Object[0][]), new String[]{"#", "Name", "Lv", "Major", "Age", "Kills", "Wealth (ss)", "Sect", "Where"});
                    if (sel >= 0 && sel < rows.size()) tables[i].setRowSelectionInterval(sel, sel);
                }
                List<int[]> hist;
                synchronized (GameDriver.statHistory) { hist = new ArrayList<>(GameDriver.statHistory); }
                pop.setSeries(new String[]{"population"}, hist, new int[]{1}, new Color[]{Theme.ACCENT}, false);
                eco.setSeries(new String[]{"cities", "sects", "shops"}, hist, new int[]{2, 3, 4}, new Color[]{new Color(230, 190, 40), new Color(180, 100, 240), new Color(100, 220, 120)}, false);
                List<int[]> bars = new ArrayList<>();
                for (int i = 0; i <= 10; i++) bars.add(new int[]{i, realmCount[i]});
                realms.setSeries(new String[]{"cultivators"}, bars, new int[]{1}, new Color[]{new Color(255, 150, 80)}, true);
            });
            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(BG);
            JLabel hint = label(" Double-click a row to open that cultivator. Charts fill in as game years pass (history restarts when a save is loaded).");
            hint.setForeground(DIM); hint.setFont(Theme.plain(12));
            root.add(hint, BorderLayout.NORTH);
            root.add(tabs, BorderLayout.CENTER);
            JLabel exportMsg = label(" ");
            exportMsg.setForeground(ACCENT);
            JPanel exp = flow(FlowLayout.LEFT);
            exp.add(mv.makeBtn("Save charts as PNG", () -> {
                try { exportMsg.setText("Saved " + Exporter.png("charts", GuiExtras.snapshot(charts)).getPath()); } catch (Exception e) { exportMsg.setText("Export failed: " + e.getMessage()); }
            }));
            exp.add(mv.makeBtn("Export rankings CSV", () -> {
                try { exportMsg.setText("Saved " + Exporter.rankings().getPath()); } catch (Exception e) { exportMsg.setText("Export failed: " + e.getMessage()); }
            }));
            exp.add(mv.makeBtn("Export history CSV", () -> {
                try { exportMsg.setText("Saved " + Exporter.history().getPath()); } catch (Exception e) { exportMsg.setText("Export failed: " + e.getMessage()); }
            }));
            exp.add(exportMsg);
            root.add(exp, BorderLayout.SOUTH);
            return root;
        });
    }

    private static class Comparator2 {
        List<Character> top(List<Character> all, java.util.Comparator<Character> c) {
            List<Character> l = new ArrayList<>(all);
            l.sort(c);
            return new ArrayList<>(l.subList(0, Math.min(100, l.size())));
        }
    }

    /** Minimal line/bar chart (no libraries): x = first column of each row, one line per series column. */
    static class ChartPanel extends JPanel {
        private final String title;
        private String[] names = new String[0];
        private List<int[]> rows = new ArrayList<>();
        private int[] cols = new int[0];
        private Color[] colors = new Color[0];
        private boolean bars = false;

        ChartPanel(String title) { this.title = title; setBackground(Theme.FIELD); setBorder(BorderFactory.createLineBorder(Theme.LINE)); }

        void setSeries(String[] names, List<int[]> rows, int[] cols, Color[] colors, boolean bars) {
            this.names = names; this.rows = rows; this.cols = cols; this.colors = colors; this.bars = bars;
            repaint();
        }

        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), l = 60, r = 14, t = 24, b = 24;
            g.setColor(FG); g.setFont(MONO_B); g.drawString(title, 8, 16);
            if (rows == null || rows.size() < (bars ? 1 : 2)) { g.setFont(MONO); g.setColor(DIM); g.drawString(bars ? "no data" : "waiting for at least two game years...", l, h / 2); return; }
            long max = 1;
            for (int[] row : rows) for (int c : cols) max = Math.max(max, row[c]);
            int x0 = rows.get(0)[0], x1 = rows.get(rows.size() - 1)[0];
            g.setColor(Theme.LINE);
            g.drawLine(l, t, l, h - b); g.drawLine(l, h - b, w - r, h - b);
            g.setFont(Theme.plain(11)); g.setColor(DIM);
            g.drawString(String.valueOf(max), 4, t + 8); g.drawString("0", l - 12, h - b);
            g.drawString(bars ? "major realm 0..10" : "year " + x0 + " .. " + x1, l, h - 6);
            int pw = w - l - r, ph = h - t - b;
            for (int s = 0; s < cols.length; s++) {
                g.setColor(colors[s]);
                if (bars) {
                    int bw = Math.max(4, pw / Math.max(1, rows.size()) - 6);
                    for (int i = 0; i < rows.size(); i++) {
                        int bh = (int) (ph * (double) rows.get(i)[cols[s]] / max);
                        int bx = l + 4 + i * pw / rows.size();
                        g.fillRect(bx, h - b - bh, bw, bh);
                        g.setColor(FG); g.drawString(String.valueOf(rows.get(i)[0]), bx + bw / 2 - 3, h - b + 12); g.setColor(colors[s]);
                    }
                } else {
                    int px = -1, py = -1;
                    for (int[] row : rows) {
                        int x = l + (x1 == x0 ? 0 : (int) ((long) pw * (row[0] - x0) / (x1 - x0)));
                        int y = h - b - (int) (ph * (double) row[cols[s]] / max);
                        if (px >= 0) g.drawLine(px, py, x, y);
                        px = x; py = y;
                    }
                    g.drawString(names[s] + " " + rows.get(rows.size() - 1)[cols[s]], w - r - 130, t + 12 + 13 * s);
                }
            }
        }
    }

    // ================================================================== market: auction + library
    static void marketPanel(MapViewer mv) {
        window(mv, "market", "Market — auction & library", 860, 560, 1500, (d, refreshers) -> {
            JTabbedPane tabs = new JTabbedPane();
            tabs.setFont(MONO_B);

            JTextArea auction = new JTextArea();
            auction.setEditable(false); auction.setLineWrap(true); auction.setWrapStyleWord(true); auction.setFont(MONO); auction.setBackground(Theme.FIELD); auction.setForeground(FG);
            refreshers.add(() -> {
                Character c = player(mv);
                if (c == null) return;
                StringBuilder sb = new StringBuilder(Auctions.text(c.location, c));
                if (!auction.getText().equals(sb.toString())) auction.setText(sb.toString());
            });
            tabs.addTab("Auction", new JScrollPane(auction));

            DefaultTableModel gmModel = model();
            JTable gmTable = table(gmModel);
            JLabel gmStatus = label(" "); gmStatus.setForeground(ACCENT);
            refreshers.add(() -> {
                Character c = player(mv);
                if (c == null) return;
                Area a = c.location;
                List<Object[]> rows = new ArrayList<>();
                if (a.city) { int i = 1; for (Life.Offer o : Life.offers(a)) rows.add(new Object[]{i++, o.stack.name + (o.shop == null ? "" : "  @ " + o.shop.name), o.stack.itemType == null ? "?" : o.stack.itemType.name(), o.stack.lvl, o.stack.quantity, o.price}); }
                int sel = gmTable.getSelectedRow();
                gmModel.setDataVector(rows.toArray(new Object[0][]), new String[]{"#", "Goods", "Kind", "Lvl", "In stock", "Price each (ss)"});
                if (sel >= 0 && sel < rows.size()) gmTable.setRowSelectionInterval(sel, sel);
                gmStatus.setText(a.city ? "Goods market of " + a.name + ".  Your wealth: " + PlayerLists.wealth(c) + " ss." : "You are not in a city.");
            });
            JPanel gmBtns = flow(FlowLayout.LEFT);
            gmBtns.add(mv.makeBtn("Buy 1", () -> { int r = gmTable.getSelectedRow(); if (r >= 0) mv.queueInput("buy market " + (r + 1)); }));
            gmBtns.add(mv.makeBtn("Buy 5", () -> { int r = gmTable.getSelectedRow(); if (r >= 0) mv.queueInput("buy market " + (r + 1) + " 5"); }));
            gmBtns.add(mv.makeBtn("Sell all my goods", () -> mv.queueInput("sell goods")));
            JPanel gmPanel = new JPanel(new BorderLayout(4, 4)); gmPanel.setBackground(BG);
            gmPanel.add(gmStatus, BorderLayout.NORTH); gmPanel.add(new JScrollPane(gmTable), BorderLayout.CENTER); gmPanel.add(gmBtns, BorderLayout.SOUTH);
            tabs.addTab("Goods", gmPanel);

            DefaultTableModel libModel = model(), mineModel = model();
            JTable libTable = table(libModel), mineTable = table(mineModel);
            libTable.setAutoCreateRowSorter(true); mineTable.setAutoCreateRowSorter(true); // click a column header to sort
            final String[] libCols = {"#", "Technique", "Type", "Lv", "Potential", "Price (ss)"};
            final String[] mineCols = {"My techniques", "Type", "Lv", "Potential", "Equipped", "Sells for (ss)"};
            JLabel libStatus = label(" "); libStatus.setForeground(ACCENT);
            refreshers.add(() -> {
                Character c = player(mv);
                if (c == null) return;
                List<Object[]> rows = new ArrayList<>();
                if (c.location.city && !c.location.libraries.isEmpty()) {
                    Library lib = c.location.libraries.get(0);
                    List<Technique> ts = new ArrayList<>(lib.techniques);
                    for (int i = 0; i < ts.size(); i++) rows.add(new Object[]{i + 1, ts.get(i).name, ts.get(i).type, ts.get(i).lvl, ts.get(i).potential, lib.buyPrice(ts.get(i))});
                    libStatus.setText(ts.size() + " techniques in " + (lib.name == null || lib.name.isEmpty() ? "the " + c.location.name + " library" : lib.name + " (" + c.location.name + ")") + " (one for each type and potential).  Your wealth: " + PlayerLists.wealth(c) + " ss");
                } else libStatus.setText("No library here.");
                int keep = -1; int sr = libTable.getSelectedRow(); if (sr >= 0) keep = ((Number) libModel.getValueAt(libTable.convertRowIndexToModel(sr), 0)).intValue();
                fillRows(libModel, libCols, rows);
                for (int r = 0; r < libModel.getRowCount() && keep >= 0; r++) if (((Number) libModel.getValueAt(r, 0)).intValue() == keep) { int vr = libTable.convertRowIndexToView(r); if (vr >= 0) libTable.setRowSelectionInterval(vr, vr); break; }
                List<Object[]> mine = new ArrayList<>();
                java.util.Set<Technique> worn = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<Technique, Boolean>());
                for (Technique w : new Technique[]{c.martialTechnique, c.martialTechnique2, c.movementTechnique, c.movementTechnique2}) if (w != null) { worn.add(w); mine.add(new Object[]{w.name, w.type, w.lvl, w.potential, "equipped", ""}); }
                for (Technique t : new ArrayList<>(c.techniqueList)) if (!worn.contains(t)) mine.add(new Object[]{t.name, t.type, t.lvl, t.potential, "", Library.sellable(t) ? (Object) Library.sellPrice(t) : (Object) 0L});
                fillRows(mineModel, mineCols, mine);
            });
            // filters for the library table: name, type, potential range, highest price
            final JTextField fName = new JTextField(10), fPotMin = new JTextField(3), fPotMax = new JTextField(3), fPriceMax = new JTextField(8);
            final JComboBox<String> fType = new JComboBox<>(new String[]{"All types", "Martial", "Movement"});
            fName.setToolTipText("Part of a technique's name"); fPotMin.setToolTipText("Lowest potential"); fPotMax.setToolTipText("Highest potential"); fPriceMax.setToolTipText("Highest price in ss");
            Runnable applyFilter = () -> {
                final String nm = fName.getText().trim().toLowerCase(), ty = (String) fType.getSelectedItem();
                final long pmin = lnum(fPotMin.getText(), 0), pmax = lnum(fPotMax.getText(), Long.MAX_VALUE), prmax = lnum(fPriceMax.getText(), Long.MAX_VALUE);
                @SuppressWarnings("unchecked") TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) libTable.getRowSorter();
                sorter.setRowFilter(new RowFilter<Object, Object>() {
                    @Override public boolean include(RowFilter.Entry<? extends Object, ? extends Object> e) {
                        if (!nm.isEmpty() && !String.valueOf(e.getValue(1)).toLowerCase().contains(nm)) return false;
                        if (!"All types".equals(ty) && !String.valueOf(e.getValue(2)).equals(ty)) return false;
                        long pot = ((Number) e.getValue(4)).longValue(), price = ((Number) e.getValue(5)).longValue();
                        return pot >= pmin && pot <= pmax && price <= prmax;
                    }
                });
            };
            fName.getDocument().addDocumentListener(new OnChange(applyFilter)); fPotMin.getDocument().addDocumentListener(new OnChange(applyFilter));
            fPotMax.getDocument().addDocumentListener(new OnChange(applyFilter)); fPriceMax.getDocument().addDocumentListener(new OnChange(applyFilter));
            fType.addActionListener(e -> applyFilter.run());
            JPanel filterBar = flow(FlowLayout.LEFT);
            filterBar.add(label("Filter:")); filterBar.add(fName); filterBar.add(fType); filterBar.add(label("potential")); filterBar.add(fPotMin); filterBar.add(label("to")); filterBar.add(fPotMax);
            filterBar.add(label("price up to")); filterBar.add(fPriceMax);
            filterBar.add(mv.makeBtn("Clear", () -> { fName.setText(""); fPotMin.setText(""); fPotMax.setText(""); fPriceMax.setText(""); fType.setSelectedIndex(0); }));
            JPanel libTop = new JPanel(new BorderLayout(4, 4)); libTop.setBackground(BG);
            libTop.add(filterBar, BorderLayout.NORTH); libTop.add(new JScrollPane(libTable), BorderLayout.CENTER);
            JPanel lib = new JPanel(new BorderLayout(4, 4));
            lib.setBackground(BG);
            JPanel split = new JPanel(new GridLayout(2, 1, 4, 4));
            split.setBackground(BG);
            split.add(libTop); split.add(new JScrollPane(mineTable));
            JPanel lb = flow(FlowLayout.LEFT);
            JButton learn = mv.makeBtn("Learn selected", () -> { int r = libTable.getSelectedRow(); if (r >= 0) mv.queueInput("learn " + ((Number) libModel.getValueAt(libTable.convertRowIndexToModel(r), 0)).intValue()); });
            learn.setForeground(Theme.OK);
            lb.add(learn);
            lb.add(mv.makeBtn("Sell my best technique here", "The library pays 20% of its price and keeps the book (one for each type and potential); you lose the technique. Techniques of potential 3 or less and the ones you wear are not bought.", () -> mv.queueInput("do Sell Technique")));
            lb.add(libStatus);
            lib.add(split, BorderLayout.CENTER); lib.add(lb, BorderLayout.SOUTH);
            tabs.addTab("Library", lib);
            return tabs;
        });
    }
}
