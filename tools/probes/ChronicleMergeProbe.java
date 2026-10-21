package Game;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;
import java.util.*;
import java.util.List;

/** 2026-10-14 (GUI, needs a display), change set 7e-1: the what-happened views are one window. The Chronicle window has the tabs World, Digest, Journal, Recap and Hall of legends; `digestWindow` and
 *  `journalWindow` open it on their tab instead of making windows of their own; the Recap tab is a card (the period so far, then the last recap); the recap popup is the same card with links and a
 *  Chronicle button that opens the Recap tab. */
public class ChronicleMergeProbe {
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
        PcProbeGui.IGNORE.addAll(Arrays.asList("Chronicle", "Journal", "What happened", "Year 3 recap"));
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
    static String text(Container c) { final String[] r = {""}; try { SwingUtilities.invokeAndWait(() -> r[0] = PcProbeGui.allText(c)); } catch (Exception e) { } return r[0]; }
    static String tabs(Container c) {
        final String[] r = {""};
        try { SwingUtilities.invokeAndWait(() -> { List<JTabbedPane> ts = new ArrayList<>(); PcProbeGui.collect(c, JTabbedPane.class, ts); if (!ts.isEmpty()) { StringBuilder sb = new StringBuilder(); for (int i = 0; i < ts.get(0).getTabCount(); i++) sb.append(i > 0 ? "|" : "").append(ts.get(0).getTitleAt(i)); r[0] = sb.toString(); } }); } catch (Exception e) { }
        return r[0];
    }
    static String selected(Container c) {
        final String[] r = {""};
        try { SwingUtilities.invokeAndWait(() -> { List<JTabbedPane> ts = new ArrayList<>(); PcProbeGui.collect(c, JTabbedPane.class, ts); if (!ts.isEmpty() && ts.get(0).getSelectedIndex() >= 0) r[0] = ts.get(0).getTitleAt(ts.get(0).getSelectedIndex()); }); } catch (Exception e) { }
        return r[0];
    }
    static int count(String title) { int n = 0; for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && ((JDialog) w).getTitle() != null && ((JDialog) w).getTitle().startsWith(title)) n++; return n; }

    static void run() throws Exception {
        Settings.lore = false;
        PcProbe.in.feed("y\ns\nn\n");
        long end = System.currentTimeMillis() + Long.getLong("gui.waitMs", 240000L);
        while (System.currentTimeMillis() < end && !(GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null)) Thread.sleep(200);
        check("GUI opened and the player is prompting", GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null);
        final Character p = PcProbeGui.pc();
        if (p == null) return;
        final MapViewer mv = GameDriver.mapViewer;

        SwingUtilities.invokeLater(() -> GuiMore.chronicleWindow(mv));
        check("C1 the Chronicle window opens", until(() -> find("Chronicle") != null, 8000));
        JDialog cw = find("Chronicle");
        if (cw == null) return;
        Thread.sleep(1500);
        check("C1b its tabs are World, Digest, Journal, Recap (" + tabs(cw.getContentPane()) + ")", tabs(cw.getContentPane()).equals("World|Digest|Journal|Recap"));
        check("C1c it opens on World", selected(cw.getContentPane()).equals("World"));

        SwingUtilities.invokeLater(() -> GuiMore.digestWindow(mv, 13));
        Thread.sleep(1500);
        check("C2 digestWindow shows the Digest tab of the same window (" + selected(cw.getContentPane()) + ", " + count("Chronicle") + " window)", selected(cw.getContentPane()).equals("Digest") && count("Chronicle") == 1);
        check("C2b ...with the digest text", text(cw.getContentPane()).contains("What happened lately") || text(cw.getContentPane()).contains("DIGEST") || text(cw.getContentPane()).toLowerCase().contains("population"));

        SwingUtilities.invokeLater(() -> GuiCards.journalWindow(mv));
        Thread.sleep(1500);
        check("C3 journalWindow shows the Journal tab and makes no window of its own (" + selected(cw.getContentPane()) + ", Journal windows " + count("Journal") + ")", selected(cw.getContentPane()).equals("Journal") && count("Journal") == 0);
        check("C3b ...with the journal of the player", text(cw.getContentPane()).contains("Journal of " + p.name));

        SwingUtilities.invokeLater(() -> GuiMore.chronicleWindow(mv, "Recap"));
        Thread.sleep(1500);
        String rt = text(cw.getContentPane());
        check("C4 the Recap tab shows the period so far as a card (" + selected(cw.getContentPane()) + ")", selected(cw.getContentPane()).equals("Recap") && rt.contains("WHAT HAPPENED TO YOU") && rt.contains("recap"));
        check("C4b ...and says the first recap is still to come", rt.contains("first recap arrives"));

        // the recap popup is the same card
        SwingUtilities.invokeLater(() -> GuiExtras.recapWindow(mv, "Year 3 recap — test", Recap.cardHtml(p)));
        check("C5 the recap popup opens", until(() -> find("Year 3 recap") != null, 8000));
        JDialog rp = find("Year 3 recap");
        Thread.sleep(500);
        check("C5b ...as a page (not plain text) with the card's sections", rp != null && text(rp.getContentPane()).contains("WHAT HAPPENED TO YOU"));
        if (rp != null) {
            SwingUtilities.invokeAndWait(() -> { JDialog c = find("Chronicle"); if (c != null) GuiHub.selectTab(c.getContentPane(), "World"); });
            JButton[] chb = {null};
            SwingUtilities.invokeAndWait(() -> chb[0] = PcProbeGui.findButton(rp.getContentPane(), "Chronicle"));
            check("C5c the popup has a Chronicle button", chb[0] != null);
            if (chb[0] != null) { SwingUtilities.invokeAndWait(() -> chb[0].doClick()); Thread.sleep(1200); }
            check("C5d ...which opens the Recap tab (" + selected(cw.getContentPane()) + ")", selected(cw.getContentPane()).equals("Recap"));
            SwingUtilities.invokeAndWait(() -> { JDialog d = find("Year 3 recap"); if (d != null) d.dispose(); });
        }
        // a real recap, delivered by the game at the end of a period, is kept as the last recap
        Recap.ensureBase(p);
        Settings.recapWeeks = 1;
        mv.queueInput("meditate");
        check("C6 a recap delivered by the game opens as a card", until(() -> find("1 weeks recap") != null || find("Season recap") != null, 30000));
        SwingUtilities.invokeAndWait(() -> { for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && ((JDialog) w).getTitle() != null && ((JDialog) w).getTitle().contains("recap")) w.dispose(); });
        Settings.recapWeeks = 0;
        SwingUtilities.invokeLater(() -> GuiMore.chronicleWindow(mv, "Recap"));
        Thread.sleep(1500);
        check("C6b ...and the Recap tab then shows it as the last recap", text(cw.getContentPane()).contains("THE LAST RECAP"));
        PcProbeGui.real.println("[G] DONE");
    }
}
