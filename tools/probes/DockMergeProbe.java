package Game;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;
import java.util.*;
import java.util.List;

/** 2026-10-14 (GUI, needs a display), change set 7e-2..4: one home for each window. The dock's Move tab holds People here, the Quest Log and Alerts (the Me and Auto tabs only link to them); the
 *  Digest and Journal buttons and the Titled hundred button are gone; Rankings has the Hall of legends and Hall of fame tabs (the Chronicle and Run statistics windows no longer have them); the
 *  `ranking` command opens the Rankings window; the Control window (one "Control…" button replaces Duties and Policies) has the chores, the answers and the spot action on its first tab. */
public class DockMergeProbe {
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
        PcProbeGui.IGNORE.addAll(Arrays.asList("Rankings", "Control", "Chronicle", "Run statistics"));
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
    static String tabsOf(Container c) {
        final String[] r = {""};
        try { SwingUtilities.invokeAndWait(() -> { List<JTabbedPane> ts = new ArrayList<>(); PcProbeGui.collect(c, JTabbedPane.class, ts); if (!ts.isEmpty()) { StringBuilder sb = new StringBuilder(); for (int i = 0; i < ts.get(0).getTabCount(); i++) sb.append(i > 0 ? "|" : "").append(ts.get(0).getTitleAt(i)); r[0] = sb.toString(); } }); } catch (Exception e) { }
        return r[0];
    }
    static int tabCount(Container c) {
        final int[] r = {-1};
        try { SwingUtilities.invokeAndWait(() -> { List<JTabbedPane> ts = new ArrayList<>(); PcProbeGui.collect(c, JTabbedPane.class, ts); r[0] = ts.size(); }); } catch (Exception e) { }
        return r[0];
    }
    /** The buttons and links on one tab of the dock ("Move", "Me", ...). */
    static String dockTab(String title, boolean links) {
        final String[] r = {""};
        try {
            SwingUtilities.invokeAndWait(() -> {
                List<JTabbedPane> ts = new ArrayList<>(); PcProbeGui.collect(GameDriver.mapViewer.getContentPane(), JTabbedPane.class, ts);
                for (JTabbedPane tp : ts) for (int i = 0; i < tp.getTabCount(); i++) if (tp.getTitleAt(i).equals(title)) {
                    Component pane = tp.getComponentAt(i);
                    StringBuilder sb = new StringBuilder();
                    if (pane instanceof Container) {
                        List<JButton> bs = new ArrayList<>(); PcProbeGui.collect((Container) pane, JButton.class, bs);
                        for (JButton b : bs) sb.append("[").append(b.getText()).append("]");
                        if (links) { List<JLabel> ls = new ArrayList<>(); PcProbeGui.collect((Container) pane, JLabel.class, ls); for (JLabel l : ls) if (l.getText() != null && l.getText().startsWith("<html><u>")) sb.append("{").append(l.getText().replaceAll("<[^>]*>", "")).append("}"); }
                    }
                    r[0] = sb.toString();
                    return;
                }
            });
        } catch (Exception e) { }
        return r[0];
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
        Thread.sleep(1500);

        // ---- the dock
        String move = dockTab("Move", true), me = dockTab("Me", true), auto = dockTab("Auto", true), world = dockTab("World", true);
        PcProbeGui.real.println("[G] info: Move " + move.length() + " chars, Me: " + me + " | Auto: " + auto);
        check("D1 the Move tab has People here, Quest Log and Alerts", move.contains("[People here]") && move.contains("[Quest Log]") && move.contains("[Alerts]"));
        check("D2 the Me tab has no People here / Quest Log / Journal button, only links", !me.contains("[People here]") && !me.contains("[Quest Log]") && !me.contains("[Journal]") && me.contains("{People here}") && me.contains("{Quest Log}") && me.contains("{Journal}"));
        check("D3 the Auto tab has no Quest Log / Alerts button, only links", !auto.contains("[Quest Log]") && !auto.contains("[Alerts]") && auto.contains("{Quest Log}") && auto.contains("{Alerts}"));
        check("D4 the Auto tab has one [Control…] button and no Duties / Policies buttons", auto.contains("[Control…]") && !auto.contains("[Duties]") && !auto.contains("[Policies]"));
        check("D5 the World tab has no Digest or Titled hundred button, and still has Rankings and Chronicle", !world.contains("[Digest]") && !world.contains("[Titled hundred]") && world.contains("[Rankings]") && world.contains("[Chronicle]"));

        // ---- Rankings
        SwingUtilities.invokeLater(() -> GuiWindows.rankingsPanel(mv));
        check("D6 Rankings opens", until(() -> find("Rankings") != null, 8000));
        JDialog rk = find("Rankings");
        if (rk == null) return;
        Thread.sleep(1500);
        String rt = tabsOf(rk.getContentPane());
        check("D6b its tabs include Hall of legends and Hall of fame (" + rt + ")", rt.contains("Hall of legends") && rt.contains("Hall of fame") && rt.startsWith("Highest level|Fighting level|Richest|Most kills|Most famous|Oldest"));
        SwingUtilities.invokeAndWait(rk::dispose);
        // the Chronicle and Run statistics no longer carry them
        SwingUtilities.invokeLater(() -> GuiMore.chronicleWindow(mv));
        check("D7 the Chronicle opens", until(() -> find("Chronicle") != null, 8000));
        JDialog cw = find("Chronicle");
        Thread.sleep(1200);
        check("D7b ...without the Hall of legends (" + (cw == null ? "" : tabsOf(cw.getContentPane())) + ")", cw != null && !tabsOf(cw.getContentPane()).contains("Hall of legends"));
        if (cw != null) SwingUtilities.invokeAndWait(cw::dispose);
        SwingUtilities.invokeLater(() -> GuiMore.runWindow(mv));
        check("D8 Run statistics opens", until(() -> find("Run statistics") != null, 8000));
        JDialog rw = find("Run statistics");
        Thread.sleep(1200);
        check("D8b ...as one page (no tabs: the hall of fame moved to Rankings)", rw != null && tabCount(rw.getContentPane()) == 0);
        if (rw != null) SwingUtilities.invokeAndWait(rw::dispose);
        // the typed command opens the window
        mv.queueInput("ranking");
        check("D9 the `ranking` command opens the Rankings window", until(() -> find("Rankings") != null, 15000));
        JDialog rk2 = find("Rankings");
        if (rk2 != null) SwingUtilities.invokeAndWait(rk2::dispose);

        // ---- the Control window
        SwingUtilities.invokeLater(() -> GuiDuties.open(mv, null));
        check("D10 the Control window opens", until(() -> find("Control") != null, 8000));
        JDialog cd = find("Control");
        if (cd == null) return;
        Thread.sleep(1800);
        String ct = tabsOf(cd.getContentPane());
        check("D10b its first tab is Chores and answers (" + ct + ")", ct.startsWith("Chores and answers"));
        final int[] boxes = {0}, combos = {0};
        final JComboBox<?>[] spot = {null};
        SwingUtilities.invokeAndWait(() -> {
            List<JCheckBox> bs = new ArrayList<>(); PcProbeGui.collect(cd.getContentPane(), JCheckBox.class, bs);
            for (JCheckBox b : bs) if (PlayerControl.dutyIndex(b.getText()) >= 0) boxes[0]++;
            List<JComboBox> cs = new ArrayList<>(); PcProbeGui.collect(cd.getContentPane(), JComboBox.class, cs);
            combos[0] = cs.size();
            for (JComboBox c : cs) if (c.getItemCount() == PlayerControl.SPOT_ACTIONS.length && String.valueOf(c.getItemAt(0)).equals(PlayerControl.SPOT_ACTIONS[0])) spot[0] = c;
        });
        check("D11 the first tab has the ten duties (" + boxes[0] + "), the policy lists (" + combos[0] + " drop-downs) and the spot action", boxes[0] == 10 && combos[0] >= 4 && spot[0] != null);
        if (spot[0] != null) {
            final int to = (p.pc().spotAction + 1) % PlayerControl.SPOT_ACTIONS.length;
            SwingUtilities.invokeAndWait(() -> spot[0].setSelectedIndex(to));
            check("D11b choosing a spot action sets it", until(() -> p.pc().spotAction == to, 15000));
        }
        SwingUtilities.invokeAndWait(cd::dispose);
        PcProbeGui.real.println("[G] DONE");
    }
}
