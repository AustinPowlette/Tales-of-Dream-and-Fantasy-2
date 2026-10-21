package Game;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;
import java.util.*;
import java.util.List;

/** 2026-10-14 (GUI, needs a display): the one list of places (`GuiPlaceList`) in Me > Locations and Browser > Places: the same filters and button bar, type / distance / spirit vein / noted filters,
 *  sort by header, Note and Remove note (`note tile x y text`), a noted place shows its note, Go there walks, Details on a wild tile selects it. */
public class PlaceListProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    static void check(String n, boolean ok) { PcProbeGui.check(n, ok); }

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
        PcProbeGui.IGNORE.addAll(Arrays.asList("Locations", "Browser — select, filter, sort"));
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
    static String buttons(Container c) {
        final String[] r = {""};
        try { SwingUtilities.invokeAndWait(() -> { List<JButton> bs = new ArrayList<>(); PcProbeGui.collect(c, JButton.class, bs); StringBuilder sb = new StringBuilder(); for (JButton b : bs) sb.append("[").append(b.getText()).append("]"); r[0] = sb.toString(); }); } catch (Exception e) { }
        return r[0];
    }
    static JTable table(Container c) {
        final JTable[] r = {null};
        try { SwingUtilities.invokeAndWait(() -> { List<JTable> ts = new ArrayList<>(); PcProbeGui.collect(c, JTable.class, ts); if (!ts.isEmpty()) r[0] = ts.get(0); }); } catch (Exception e) { }
        return r[0];
    }
    static void press(Container c, String label) throws Exception { SwingUtilities.invokeAndWait(() -> { JButton b = PcProbeGui.findButton(c, label); if (b != null) b.doClick(); }); }
    static int rowOf(JTable t, String name) {
        for (int i = 0; t != null && i < t.getRowCount(); i++) if (String.valueOf(t.getValueAt(i, 1)).contains(name)) return i;
        return -1;
    }
    static void toggle(Container c, String text) throws Exception {
        SwingUtilities.invokeAndWait(() -> { List<JCheckBox> l = new ArrayList<>(); PcProbeGui.collect(c, JCheckBox.class, l); for (JCheckBox b : l) if (text.equals(b.getText())) b.doClick(); });
    }
    static void setName(JDialog d, String text) throws Exception {
        SwingUtilities.invokeAndWait(() -> { List<JTextField> fs = new ArrayList<>(); PcProbeGui.collect(d.getContentPane(), JTextField.class, fs); if (!fs.isEmpty()) { fs.get(0).setText(text); fs.get(0).postActionEvent(); } });
    }
    static void clickHeader(JTable tb, int col) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            java.awt.event.MouseEvent e = new java.awt.event.MouseEvent(tb.getTableHeader(), java.awt.event.MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, tb.getTableHeader().getHeaderRect(col).x + 4, 4, 1, false);
            for (java.awt.event.MouseListener l : tb.getTableHeader().getMouseListeners()) l.mouseClicked(e);
        });
    }
    static void select(JTable tb, int row) throws Exception { SwingUtilities.invokeAndWait(() -> tb.setRowSelectionInterval(row, row)); }

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
        final int fx = (home.x + 7) % Map.range, fy = (home.y + 7) % Map.range, nx = (home.x + 2) % Map.range, ny = home.y;
        if (Map.tiles[fx][fy] == null) Map.tiles[fx][fy] = new Area(Area.ghost(fx == 0 ? Map.range - 1 : fx - 1, fy), 'e');
        if (Map.tiles[nx][ny] == null) Map.tiles[nx][ny] = new Area(Area.ghost(nx == 0 ? Map.range - 1 : nx - 1, ny), 'e');
        final Area far = Map.tiles[fx][fy], near = Map.tiles[nx][ny];
        far.name = "Probe Hollow"; far.spiritualVein = true; far.city = false; far.sect = null; far.dungeonType = null;
        near.name = "Probe Meadow"; near.spiritualVein = false; near.city = false; near.sect = null; near.dungeonType = null;
        p.knownLocations.add(far); p.knownLocations.add(near);
        Chronicle.notes.clear();
        Thread.sleep(300);

        // ---- Locations
        SwingUtilities.invokeLater(() -> GuiSheets.locationsWindow(mv));
        check("P1 the Locations window opens", until(() -> find("Locations") != null, 8000));
        final JDialog lw = find("Locations");
        if (lw == null) return;
        Thread.sleep(2200);
        String bar = buttons(lw.getContentPane());
        PcProbeGui.real.println("[G] info: Locations buttons: " + bar);
        check("P2 the button bar: Go there, Route, Add waypoint quest, Details, Note, Remove note, Refresh, Set as default limit, Export CSV, Open in Browser",
                bar.contains("[Go there]") && bar.contains("[Route]") && bar.contains("[Add waypoint quest]") && bar.contains("[Details]") && bar.contains("[Note…]") && bar.contains("[Remove note]")
                        && bar.contains("[Refresh]") && bar.contains("[Set as default limit]") && bar.contains("[Export CSV]") && bar.contains("[Open in Browser]"));
        final JTable tb = table(lw.getContentPane());
        check("P3 the table lists the known places (" + (tb == null ? 0 : tb.getRowCount()) + " rows) with a What and a Note column", tb != null && tb.getRowCount() >= 3 && tb.getColumnName(6).equals("What") && tb.getColumnName(8).equals("Note"));
        int hr = rowOf(tb, "Probe Hollow");
        check("P3b 'Probe Hollow' is listed and says spirit vein", hr >= 0 && String.valueOf(tb.getValueAt(hr, 6)).contains("spirit vein"));

        // ---- filters
        setName(lw, "Hollow");
        Thread.sleep(900);
        check("P4 the name filter keeps only matching places", tb.getRowCount() == 1 && String.valueOf(tb.getValueAt(0, 1)).contains("Probe Hollow"));
        setName(lw, "");
        toggle(lw.getContentPane(), "spirit veins");
        Thread.sleep(900);
        check("P4b 'spirit veins' keeps only veins (" + tb.getRowCount() + " rows)", tb.getRowCount() >= 1 && rowOf(tb, "Probe Meadow") < 0 && rowOf(tb, "Probe Hollow") >= 0);
        toggle(lw.getContentPane(), "spirit veins");
        Thread.sleep(900);
        check("P4c ...and unticking brings the rest back", rowOf(tb, "Probe Meadow") >= 0);

        // ---- note / remove note
        select(tb, rowOf(tb, "Probe Meadow"));
        PcProbeGui.dialogInput = "good herbs here";
        SwingUtilities.invokeLater(() -> { JButton b = PcProbeGui.findButton(lw.getContentPane(), "Note…"); if (b != null) b.doClick(); });
        check("P5 Note… stores the note on that tile", until(() -> Chronicle.notes.containsKey(Chronicle.tileKey(near)), 15000));
        PcProbeGui.dialogInput = null;
        check("P5b the table shows the note", until(() -> { int r = rowOf(tb, "Probe Meadow"); return r >= 0 && String.valueOf(tb.getValueAt(r, 8)).contains("good herbs"); }, 8000));
        toggle(lw.getContentPane(), "noted only");
        Thread.sleep(900);
        check("P5c 'noted only' keeps just the noted place (" + tb.getRowCount() + " rows)", tb.getRowCount() == 1 && rowOf(tb, "Probe Meadow") == 0);
        select(tb, 0);
        press(lw.getContentPane(), "Remove note");
        check("P6 Remove note deletes it", until(() -> !Chronicle.notes.containsKey(Chronicle.tileKey(near)), 15000));
        toggle(lw.getContentPane(), "noted only");
        Thread.sleep(900);

        // ---- sort by header
        clickHeader(tb, 3);
        Thread.sleep(900);
        int d0 = (Integer) tb.getValueAt(0, 3), dn = (Integer) tb.getValueAt(tb.getRowCount() - 1, 3);
        check("P7 clicking the Dist header sorts by distance (" + d0 + " .. " + dn + ")", d0 <= dn);
        clickHeader(tb, 3);
        Thread.sleep(900);
        d0 = (Integer) tb.getValueAt(0, 3); dn = (Integer) tb.getValueAt(tb.getRowCount() - 1, 3);
        check("P7b ...and again reverses it (" + d0 + " .. " + dn + ")", d0 >= dn);

        // ---- Details on a wild tile
        select(tb, rowOf(tb, "Probe Hollow"));
        press(lw.getContentPane(), "Details");
        Thread.sleep(800);
        check("P8 Details on a wild place selects its tile on the map", mv.selX == fx && mv.selY == fy);

        // ---- the Browser has the same list
        SwingUtilities.invokeLater(() -> mv.openBrowser("Places"));
        check("P9 Browser > Places opens", until(() -> find("Browser") != null, 8000));
        JDialog br = find("Browser");
        Thread.sleep(2200);
        String bb = br == null ? "" : buttons(br.getContentPane());
        check("P9b ...with the same buttons", bb.contains("[Go there]") && bb.contains("[Note…]") && bb.contains("[Remove note]"));

        // ---- the terminal command
        mv.queueInput("note tile " + fx + " " + fy + " far hollow");
        check("P10 'note tile x y text' marks a place anywhere", until(() -> Chronicle.notes.containsKey(Chronicle.tileKey(far)), 15000));
        mv.queueInput("unnote tile " + fx + " " + fy);
        check("P10b 'unnote tile x y' removes it", until(() -> !Chronicle.notes.containsKey(Chronicle.tileKey(far)), 15000));

        // ---- Go there walks to the place
        select(tb, rowOf(tb, "Probe Hollow"));
        press(lw.getContentPane(), "Go there");
        check("P11 Go there starts the walk to that tile", until(() -> (p.autoX == fx && p.autoY == fy) || (p.location.x == fx && p.location.y == fy), 20000));
        PcProbeGui.real.println("[G] DONE");
    }
}
