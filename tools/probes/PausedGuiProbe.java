package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-13 (GUI, needs a display): the real buttons while the game is paused in the middle of a run (`wait`). The Titled hundred, Edit Self and Save work at once; a button that spends the week
 *  (Meditate) opens the "Paused" window, waits in the queue and runs when the game resumes. A Save clicked while the game waits for the player's command is written without a move. */
public class PausedGuiProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    static void check(String n, boolean ok) { PcProbeGui.check(n, ok); }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "false");
        PcProbeGui.real = System.out;
        PcProbe.real = PcProbeGui.real;
        System.setIn(PcProbe.in);
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(PcProbeGui.real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        Thread w = new Thread(PcProbeGui::watcher, "dialog-watcher"); w.setDaemon(true); w.start();
        try { run(); } catch (Throwable e) { PcProbeGui.real.println("[G] PROBE EXCEPTION " + e); e.printStackTrace(PcProbeGui.real); PcProbeGui.fails++; }
        PcProbeGui.real.println("[G] ===== " + PcProbeGui.passes + " passed, " + PcProbeGui.fails + " failed; gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }

    static boolean until(java.util.function.BooleanSupplier c, long ms) throws Exception { long end = System.currentTimeMillis() + ms; while (System.currentTimeMillis() < end) { if (c.getAsBoolean()) return true; Thread.sleep(50); } return c.getAsBoolean(); }

    /** Starts a long wait and pauses in the middle of it; true when the game stands in the pause loop. */
    static boolean pauseMidRun() throws Exception {
        for (int tries = 0; tries < 8; tries++) {
            GameDriver.paused = false;
            GameDriver.tickDelayMs = 150;
            GameDriver.guiInputQueue.add("wait 400");
            Thread.sleep(500);
            GameDriver.paused = true;
            if (until(() -> GameDriver.pauseWaiting, 6000)) return true;
            GameDriver.paused = false; // an alert cut the wait short and the game is at the prompt again: try once more
            PcProbeGui.settle();
        }
        return false;
    }

    static void run() throws Exception {
        Settings.lore = false;
        PcProbe.in.feed("y\ns\nn\n");
        long end = System.currentTimeMillis() + Long.getLong("gui.waitMs", 240000L);
        while (System.currentTimeMillis() < end && !(GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null)) Thread.sleep(200);
        check("GUI opened and the player is prompting", GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null);
        Character p = PcProbeGui.pc();
        if (p == null) return;
        p.lvl = 100; p.majorLvl = 10; p.maxAge = 100000; p.caution = 0;
        DecisionBroker.scriptedNumbers = new ArrayDeque<>(); DecisionBroker.scripted = new ArrayDeque<>();
        PcProbeGui.SHEETS.clear();
        Thread.sleep(500);

        // ---- S1: Save while the game waits for a command: written at once, no move
        java.io.File quick = new java.io.File(SaveManager.dir, SaveManager.QUICK + ".dat");
        long q0 = quick.isFile() ? quick.lastModified() : 0; int t0 = GameDriver.time;
        PcProbeGui.click("Save");
        check("S1 Save at the prompt is written within 5 seconds without a move (time " + t0 + " -> " + GameDriver.time + ")", until(() -> quick.isFile() && quick.lastModified() > q0, 5000) && GameDriver.time == t0);
        PcProbeGui.settle();

        // ---- S2: paused in the middle of a run
        check("S2 the game stands in the pause loop in the middle of a wait", pauseMidRun());
        int tp = GameDriver.time;
        // the Titled hundred
        int d0 = PcProbeGui.dialogCount();
        PcProbeGui.click("Rankings");
        check("S3 Rankings opens while paused", PcProbeGui.dialogSince(d0, "Rankings") && GameDriver.time == tp);
        // Edit Self (a modal window: the watcher records it and answers it)
        d0 = PcProbeGui.dialogCount();
        PcProbeGui.click("Edit Self");
        check("S4 Edit Self opens while paused", PcProbeGui.dialogSince(d0, "Edit") || PcProbeGui.dialogSince(d0, "Name"));
        // Save while paused
        long q1 = quick.lastModified();
        PcProbeGui.click("Save");
        check("S5 Save while paused is written within 5 seconds (the week stays " + tp + ")", until(() -> quick.lastModified() > q1, 5000) && GameDriver.time == tp);
        // a button that spends the week: notice + queued
        d0 = PcProbeGui.dialogCount();
        int med0 = PcProbe.mark();
        PcProbeGui.click("Meditate");
        check("S6 Meditate while paused opens the Paused window", PcProbeGui.dialogSince(d0, "The game is paused"));
        Thread.sleep(600);
        check("S6b ...and nothing happened yet (still week " + tp + ", no meditation)", GameDriver.time == tp && !PcProbe.since(med0).contains("You meditate"));
        // resume: the queued command runs when the walk ends (the wait is cut by an alert, or runs out) - make it end now
        GameDriver.paused = false; GameDriver.tickDelayMs = 0;
        boolean ran = until(() -> PcProbe.since(med0).contains("You meditate"), 120000);
        check("S7 after resuming, the queued Meditate runs", ran);
        PcProbeGui.settle();
        PcProbeGui.real.println("[G] DONE");
    }
}
