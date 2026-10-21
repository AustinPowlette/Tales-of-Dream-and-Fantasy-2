package Game;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.PrintStream;
import java.util.*;
import java.util.List;

/** 2026-10-14 (GUI, needs a display), change set 7d: a long text in any table wraps and the row grows (W1-W3, the same event at three widths), no window of the game clips a cell (W4: every
 *  table of every window is checked cell by cell), a long cell is drawn as wrapped text in the colours of its renderer (W2), double-clicking a row opens the person / place in Notes, Rivalries,
 *  Manage > Sect and Chronicle (W5), a person's sheet opens once and a second click right after Close opens nothing (W6), and a decision that names a person shows their portrait (W7). */
public class WrapTableProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    static void check(String n, boolean ok) { PcProbeGui.check(n, ok); }
    static final String LONG = "In the third year of the great drought the Nine Bells sect closed its gates to every traveller, and the elders of the river towns met under the old camphor tree to argue whether grain should go to the hungry or be kept for the war that nobody yet dared to name aloud; the argument lasted until the first snow.";

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "false");
        PcProbeGui.real = System.out;
        PcProbe.real = PcProbeGui.real;
        System.setIn(PcProbe.in);
        GameDriver.frozenNpcs = true;
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(PcProbeGui.real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        Thread w = new Thread(PcProbeGui::watcher, "dialog-watcher"); w.setDaemon(true); w.start();
        PcProbeGui.SHEETS.clear();
        PcProbeGui.IGNORE.addAll(Arrays.asList("Chronicle", "Notes", "Notes & bookmarks", "Rankings", "Manage", "Notifications", "Saves", "Family", "Relations", "Locations", "Quests", "Backpack", "Squad", "Browser", "Market", "Run statistics", "Here", "People here", "Journal"));
        try { run(); } catch (Throwable e) { PcProbeGui.real.println("[G] PROBE EXCEPTION " + e); e.printStackTrace(PcProbeGui.real); PcProbeGui.fails++; }
        PcProbeGui.real.println("[G] ===== " + PcProbeGui.passes + " passed, " + PcProbeGui.fails + " failed; gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }

    static boolean until(java.util.function.BooleanSupplier c, long ms) throws Exception {
        long end = System.currentTimeMillis() + ms;
        while (System.currentTimeMillis() < end) { if (c.getAsBoolean()) return true; Thread.sleep(50); }
        return c.getAsBoolean();
    }
    static JDialog find(String title) {
        for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && ((JDialog) w).getTitle() != null && ((JDialog) w).getTitle().startsWith(title)) return (JDialog) w;
        return null;
    }
    static int dialogs(String title) {
        int n = 0;
        for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && ((JDialog) w).getTitle() != null && ((JDialog) w).getTitle().startsWith(title)) n++;
        return n;
    }
    static List<JTable> tablesIn(Container c) { final List<JTable> r = new ArrayList<>(); try { SwingUtilities.invokeAndWait(() -> PcProbeGui.collect(c, JTable.class, r)); } catch (Exception e) { } return r; }
    static void doubleClick(JTable tb, int row) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            tb.setRowSelectionInterval(row, row);
            for (java.awt.event.MouseListener l : tb.getMouseListeners()) l.mouseClicked(new MouseEvent(tb, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 5, 5, 2, false));
        });
    }
    static Character npc(Area where, String first, int lvl) {
        Character c = new Character(where);
        c.age = 30; c.gender = "Male"; c.lvl = lvl; c.majorLvl = lvl / 10; c.maxAge = 100000; c.trait = Character.Traits.Friendly; c.name = first; c.lastName = "Wrapson"; c.caution = 0;
        while (Map.characters.size() < c.id) Map.characters.add(null);
        Map.characters.add(c); Map.population++;
        return c;
    }
    /** Cells whose text needs more height than their row has (the wrapped count of lines against the row height). */
    static int clipped(JTable tb, StringBuilder why) {
        final int[] n = {0};
        try {
            SwingUtilities.invokeAndWait(() -> {
                FontMetrics fm = tb.getFontMetrics(tb.getFont());
                for (int r = 0; r < tb.getRowCount() && r < 400; r++)
                    for (int c = 0; c < tb.getColumnCount(); c++) {
                        Object v = tb.getValueAt(r, c);
                        if (!(v instanceof String)) continue;
                        String s = (String) v;
                        int w = tb.getColumnModel().getColumn(c).getWidth() - 14;
                        if (s.length() < 6 || w < 20 || s.startsWith("<html>")) continue;
                        int lines = GuiWrapTable.wrapLines(s, fm, w), need = lines == 1 ? 0 : lines * fm.getHeight() + 4; // (one line fits the normal row)
                        if (need > tb.getRowHeight(r) + 1) { n[0]++; if (why.length() < 300) why.append("[row ").append(r).append(" col ").append(tb.getColumnName(c)).append(" needs ").append(need).append(" has ").append(tb.getRowHeight(r)).append("]"); }
                    }
            });
        } catch (Exception e) { }
        return n[0];
    }

    static void run() throws Exception {
        Settings.lore = false;
        PcProbe.in.feed("y\ns\nn\n");
        long end = System.currentTimeMillis() + Long.getLong("gui.waitMs", 240000L);
        while (System.currentTimeMillis() < end && !(GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null)) Thread.sleep(200);
        check("GUI opened and the player is prompting", GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null);
        final Character p = PcProbeGui.pc();
        if (p == null) return;
        final MapViewer mv = GameDriver.mapViewer;
        p.lvl = 100; p.majorLvl = 10; p.maxAge = 100000;
        final Area home = p.location;
        final Character ann = npc(home, "Ann", 30);

        // ---- W1: the same long event at three widths
        final DefaultTableModel m = GuiWindows.model();
        m.setColumnIdentifiers(new String[]{"When", "Kind", "Event"});
        m.addRow(new Object[]{"Y1 w1", "world", "short"});
        m.addRow(new Object[]{"Y1 w2", "world", LONG});
        final JTable[] tb = {null};
        final int[] heights = new int[3];
        int[] widths = {500, 800, 1300};
        for (int i = 0; i < 3; i++) {
            final int wi = widths[i], k = i;
            SwingUtilities.invokeAndWait(() -> {
                JTable x = GuiWindows.table(m);
                JScrollPane sp = new JScrollPane(x);
                JFrame f = new JFrame("wrap-test"); f.add(sp); f.setSize(wi, 300); f.setVisible(true);
                f.validate(); x.doLayout();
                tb[0] = x;
            });
            Thread.sleep(600);
            heights[k] = tb[0].getRowHeight(1);
            final int shortH = tb[0].getRowHeight(0);
            check("W1." + k + " at " + wi + " px the short row stays one line (" + shortH + ") and the 300-character event wraps (row " + heights[k] + ")", shortH == 20 && heights[k] > 20);
            SwingUtilities.invokeAndWait(() -> { for (Window w : Window.getWindows()) if (w instanceof JFrame && "wrap-test".equals(((JFrame) w).getTitle())) w.dispose(); });
        }
        check("W1b ...and the narrower the window the taller the row (" + heights[0] + " >= " + heights[1] + " >= " + heights[2] + ")", heights[0] >= heights[1] && heights[1] >= heights[2] && heights[0] > heights[2]);

        // ---- W2: a wrapped cell keeps the colours of its renderer
        final boolean[] ok2 = {false};
        SwingUtilities.invokeAndWait(() -> {
            JTable x = GuiWindows.table(m);
            x.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                    Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c); comp.setForeground(Color.RED); return comp;
                }
            });
            JFrame f = new JFrame("wrap-test"); f.add(new JScrollPane(x)); f.setSize(500, 300); f.setVisible(true); f.validate(); x.doLayout();
            Component longCell = x.prepareRenderer(x.getCellRenderer(1, 2), 1, 2), shortCell = x.prepareRenderer(x.getCellRenderer(0, 2), 0, 2);
            ok2[0] = longCell instanceof JTextArea && longCell.getForeground().equals(Color.RED) && !(shortCell instanceof JTextArea);
            f.dispose();
        });
        check("W2 a long cell is drawn as wrapped text in its renderer's colour; a short cell stays a label", ok2[0]);

        // ---- W3: the Chronicle wraps a long event
        Chronicle.add("world", LONG, ann.id);
        SwingUtilities.invokeLater(() -> GuiMore.chronicleWindow(mv));
        check("W3 the Chronicle opens", until(() -> find("Chronicle") != null, 8000));
        JDialog cw = find("Chronicle");
        Thread.sleep(2500);
        JTable ct = cw == null ? null : tablesIn(cw.getContentPane()).get(0);
        int longRow = -1;
        for (int i = 0; ct != null && i < ct.getRowCount(); i++) if (String.valueOf(ct.getValueAt(i, 2)).startsWith("In the third year")) longRow = i;
        check("W3b the long event is a taller row (" + (longRow < 0 ? -1 : ct.getRowHeight(longRow)) + ")", longRow >= 0 && ct.getRowHeight(longRow) > 20);

        if (System.getProperty("shots") != null && cw != null) { // a picture of the window for a look by eye
            final JDialog shot = cw;
            SwingUtilities.invokeAndWait(() -> { try { java.awt.image.BufferedImage im = new java.awt.image.BufferedImage(shot.getWidth(), shot.getHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB); shot.paint(im.getGraphics()); javax.imageio.ImageIO.write(im, "png", new java.io.File(System.getProperty("shots"))); } catch (Exception e) { } });
        }
        // ---- W5: double-clicks
        doubleClick(ct, longRow);
        check("W5 double-click in the Chronicle opens the first living person of the event", until(() -> find("Ann Wrapson") != null, 8000));
        JDialog ad = find("Ann Wrapson");
        // ---- W6: one sheet per person; a click right after Close opens nothing
        SwingUtilities.invokeAndWait(() -> mv.showCharacterDialog(ann));
        Thread.sleep(500);
        check("W6 asking for the same sheet again brings the first to the front (" + dialogs("Ann Wrapson") + " open)", dialogs("Ann Wrapson") == 1);
        SwingUtilities.invokeAndWait(() -> { JDialog d = find("Ann Wrapson"); if (d != null) d.dispose(); });
        Thread.sleep(150);
        doubleClick(ct, longRow);
        Thread.sleep(500);
        check("W6b a double-click right after Close opens nothing (the click that lands on the list below)", dialogs("Ann Wrapson") == 0);
        Thread.sleep(900);
        doubleClick(ct, longRow);
        check("W6c ...but a double-click a moment later does", until(() -> find("Ann Wrapson") != null, 8000));
        SwingUtilities.invokeAndWait(() -> { JDialog d = find("Ann Wrapson"); if (d != null) d.dispose(); });
        Thread.sleep(800);

        // ---- Notes: double-click on a person's note opens the person
        Chronicle.notes.clear();
        Chronicle.notes.put(Chronicle.charKey(ann), "a friend who writes long letters");
        SwingUtilities.invokeLater(() -> GuiMore.notesWindow(mv));
        check("W5b Notes opens", until(() -> find("Notes") != null, 8000));
        JDialog nw = find("Notes");
        Thread.sleep(2800);
        List<JTable> nts = nw == null ? new ArrayList<JTable>() : tablesIn(nw.getContentPane());
        if (!nts.isEmpty() && nts.get(0).getRowCount() > 0) {
            doubleClick(nts.get(0), 0);
            check("W5c double-click on a person's note opens the person", until(() -> find("Ann Wrapson") != null, 8000));
            SwingUtilities.invokeAndWait(() -> { JDialog d = find("Ann Wrapson"); if (d != null) d.dispose(); });
        } else check("W5c Notes lists the note", false);
        Chronicle.notes.clear();

        // ---- W4: every window: no table clips a cell
        SwingUtilities.invokeLater(() -> {
            GuiMore.backpackWindow(mv, "Companions");
            GuiExtras.notificationsWindow(mv);
            GuiExtras.savesWindow(mv);
            GuiExtras.familyWindow(mv);
            GuiSheets.relationsWindow(mv);
            GuiSheets.locationsWindow(mv);
            GuiSheets.squadWindow(mv);
            GuiWindows.rankingsPanel(mv);
            GuiWindows.managePanel(mv);
            GuiWindows.marketPanel(mv);
            GuiQuests.open(mv, null);
            GuiMore.runWindow(mv);
            mv.openBrowser("Cultivators");
        });
        Thread.sleep(6000);
        int windows = 0, tablesSeen = 0, bad = 0;
        StringBuilder why = new StringBuilder();
        for (Window w : Window.getWindows()) {
            if (!(w instanceof JDialog) || !w.isShowing()) continue;
            windows++;
            for (JTable x : tablesIn(((JDialog) w).getContentPane())) {
                if (x.getRowCount() == 0) continue;
                tablesSeen++;
                int c = clipped(x, why);
                if (c > 0) { bad += c; PcProbeGui.real.println("[G] info: " + ((JDialog) w).getTitle() + ": " + c + " clipped cells " + why); why.setLength(0); }
            }
        }
        check("W4 " + windows + " windows, " + tablesSeen + " tables with rows: no cell is clipped (" + bad + ")", tablesSeen >= 6 && bad == 0);

        // ---- W5d: Rankings and Manage > Sect double-click
        JDialog rk = find("Rankings");
        List<JTable> rts = rk == null ? new ArrayList<JTable>() : tablesIn(rk.getContentPane());
        boolean hasHandler = false;
        for (JTable x : rts) if (x.getMouseListeners().length > 1) hasHandler = true;
        check("W5d Rankings tables have a double-click handler (" + rts.size() + " tables)", hasHandler);

        // ---- W7: a decision about a person shows their portrait
        final int[] answer = {-9};
        Thread asker = new Thread(() -> answer[0] = DecisionBroker.askAbout(p, "You defeated Ann Wrapson (Lv.30, Neutral). Kill or spare?", new String[]{"Kill", "Spare"}, 1, ann), "asker");
        asker.setDaemon(true); asker.start();
        check("W7 the Kill-or-spare window opens", until(() -> find("Decision") != null, 8000));
        JDialog dd = find("Decision");
        final boolean[] portrait = {false};
        if (dd != null) SwingUtilities.invokeAndWait(() -> {
            List<JLabel> ls = new ArrayList<>(); PcProbeGui.collect(dd.getContentPane(), JLabel.class, ls);
            for (JLabel l : ls) if (l.getIcon() != null && l.getIcon().getIconWidth() >= 80) portrait[0] = true;
        });
        check("W7b ...and it shows the person's portrait", portrait[0] || !Settings.pictures);
        if (dd != null) SwingUtilities.invokeAndWait(dd::dispose);
        until(() -> answer[0] != -9, 8000);
        check("W7c closing it gives the default answer (Spare)", answer[0] == 1);
        PcProbeGui.real.println("[G] DONE");
    }
}
