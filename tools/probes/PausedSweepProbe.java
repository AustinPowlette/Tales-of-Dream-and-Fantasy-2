package Game;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;
import java.util.*;
import java.util.List;

/** 2026-10-15 (GUI, needs a display), the paused sweep of the test plan (T2, contract test 12): the game stands paused in the middle of a run, every enabled button of the dock (Move, Me, Auto, World) is pressed one
 *  by one and classified: it acted now (a window opened, a menu opened, a line was printed), it waits behind the pause (its command stays in the queue; the "Paused" window says so), or it did nothing. No button may do
 *  nothing, and no time may pass while paused. */
public class PausedSweepProbe {
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
        try { run(); } catch (Throwable e) { PcProbeGui.real.println("[G] PROBE EXCEPTION " + e); e.printStackTrace(PcProbeGui.real); PcProbeGui.fails++; }
        PcProbeGui.real.println("[G] ===== " + PcProbeGui.passes + " passed, " + PcProbeGui.fails + " failed; gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }

    static boolean until(java.util.function.BooleanSupplier c, long ms) throws Exception {
        long end = System.currentTimeMillis() + ms;
        while (System.currentTimeMillis() < end) { if (c.getAsBoolean()) return true; Thread.sleep(50); }
        return c.getAsBoolean();
    }

    static boolean pauseMidRun() throws Exception {
        for (int tries = 0; tries < 8; tries++) {
            GameDriver.paused = false;
            GameDriver.tickDelayMs = 150;
            GameDriver.guiInputQueue.add("wait 400");
            Thread.sleep(500);
            GameDriver.paused = true;
            if (until(() -> GameDriver.pauseWaiting, 6000)) return true;
            GameDriver.paused = false;
            PcProbeGui.settle();
        }
        return false;
    }

    /** The enabled buttons of the dock tabs, in tab order. */
    static List<JButton> dockButtons() {
        final List<JButton> out = new ArrayList<>();
        try {
            SwingUtilities.invokeAndWait(() -> {
                List<JTabbedPane> ts = new ArrayList<>(); PcProbeGui.collect(GameDriver.mapViewer.getContentPane(), JTabbedPane.class, ts);
                for (JTabbedPane tp : ts) {
                    boolean dock = false;
                    for (int i = 0; i < tp.getTabCount(); i++) if (tp.getTitleAt(i).equals("Move")) dock = true;
                    if (!dock) continue;
                    for (int i = 0; i < tp.getTabCount(); i++) {
                        tp.setSelectedIndex(i);
                        List<JButton> bs = new ArrayList<>(); PcProbeGui.collect((Container) tp.getComponentAt(i), JButton.class, bs);
                        for (JButton b : bs) if (b.isEnabled() && b.isShowing() || b.isEnabled() && b.isDisplayable()) out.add(b);
                    }
                }
            });
        } catch (Exception e) { }
        return out;
    }

    static int dialogCount() { int n = 0; for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing()) n++; return n; }

    static void closeExtraDialogs(Set<Window> keep) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MenuSelectionManager.defaultManager().clearSelectedPath();
            for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && !keep.contains(w)) w.dispose();
        });
    }

    static void run() throws Exception {
        Settings.lore = false;
        PcProbe.in.feed("y\ns\nn\n");
        long end = System.currentTimeMillis() + Long.getLong("gui.waitMs", 240000L);
        while (System.currentTimeMillis() < end && !(GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null)) Thread.sleep(200);
        check("GUI opened and the player is prompting", GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null);
        final Character p = PcProbeGui.pc();
        if (p == null) return;
        p.lvl = 100; p.majorLvl = 10; p.maxAge = 100000; p.caution = 0;
        p.pc().breakthroughPolicy = PlayerControl.BT_AUTO;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        PcProbeGui.SHEETS.clear();
        // the dialogs the watcher must leave alone while we look at them are closed by us
        Thread.sleep(800);
        List<JButton> buttons = dockButtons();
        check("S0 the dock has buttons to press (" + buttons.size() + ")", buttons.size() > 40);
        check("S1 the game stands in the pause loop in the middle of a run", pauseMidRun());
        final int tp = GameDriver.time;
        Set<Window> base = new HashSet<>(); for (Window w : Window.getWindows()) base.add(w);

        List<String> acted = new ArrayList<>(), waited = new ArrayList<>(), nothing = new ArrayList<>(), skipped = new ArrayList<>();
        Set<String> skip = new HashSet<>(Arrays.asList("Edit Self", "Cheats", "Retire run…", "Save", "💾 Save", "◀ Take control", "◀ Take control back"));
        for (JButton b : buttons) {
            final String label = b.getText() == null ? "?" : b.getText().replaceAll("<[^>]*>", "");
            if (skip.contains(label) || label.isEmpty() || label.equals("?")) { skipped.add(label); continue; }
            GameDriver.guiInputQueue.clear();
            int d0 = dialogCount(), m0 = PcProbe.mark();
            try { SwingUtilities.invokeAndWait(() -> { JTabbedPane tabs = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, b); if (tabs != null) { Component c = b; while (c != null && c.getParent() != tabs) c = c.getParent(); if (c != null) tabs.setSelectedComponent(c); } if (b.isEnabled()) b.doClick(); }); } catch (Exception e) { Throwable c = e.getCause() == null ? e : e.getCause(); StackTraceElement[] st = c.getStackTrace(); nothing.add(label + " (threw " + c + " at " + (st.length > 0 ? st[0] : "?") + " / " + (st.length > 1 ? st[1] : "") + ")"); continue; }
            Thread.sleep(550);
            boolean popup = MenuSelectionManager.defaultManager().getSelectedPath().length > 0;
            boolean window = dialogCount() > d0;
            boolean printed = PcProbe.since(m0).trim().length() > 0;
            boolean queued = !GameDriver.guiInputQueue.isEmpty();
            String said = PcProbe.since(m0).toLowerCase();
            if (window || popup || (printed && !said.contains("pause"))) acted.add(label); else if (queued || said.contains("pause")) waited.add(label); else nothing.add(label);
            GameDriver.guiInputQueue.clear();
            closeExtraDialogs(base);
        }
        PcProbeGui.real.println("[G] info: acted now (" + acted.size() + "): " + acted);
        PcProbeGui.real.println("[G] info: wait behind the pause (" + waited.size() + "): " + waited);
        PcProbeGui.real.println("[G] info: did nothing (" + nothing.size() + "): " + nothing);
        PcProbeGui.real.println("[G] info: skipped (" + skipped.size() + "): " + skipped);
        // buttons whose whole job is to change what an unpaused game does next (the mode, the quick duty switches, the script runner) may take effect without a visible sign
        Set<String> quiet = new HashSet<>(Arrays.asList("Manual", "Auto", "Hybrid", "All duties on", "All duties off", "Resume", "Run script", "Route…", "Tutorial")); // (Route… asks in a small window the watcher closes at once; the Tutorial window was already open)
        List<String> bad = new ArrayList<>(); for (String s : nothing) if (!quiet.contains(s)) bad.add(s);
        check("S2 no button of the dock does nothing while the game is paused (" + bad + ")", bad.isEmpty());
        check("S3 pressing them all spent no time (week " + tp + " -> " + GameDriver.time + ")", GameDriver.time == tp);
        check("S4 many of them act at once (" + acted.size() + ") and the rest wait in line (" + waited.size() + ")", acted.size() >= 25);
        GameDriver.paused = false; GameDriver.tickDelayMs = 0;
        PcProbeGui.real.println("[G] DONE");
    }
}
