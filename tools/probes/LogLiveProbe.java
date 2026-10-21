package Game;

import javax.swing.*;
import java.io.PrintStream;
import java.util.*;

/** 2026-10-16 (GUI, needs a display), change set A: the answer of a command that costs no turn (Invite, a setting, 'time') appears in the GUI's log pane at once, not after the next week passes. The beta tester's
 *  complaint: "Invite doesn't print to the terminal immediately, requires a move". The log pane used to be redrawn only at the end of a tick. */
public class LogLiveProbe {
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
        try { run(); } catch (Throwable e) { PcProbeGui.real.println("[G] PROBE EXCEPTION " + e); e.printStackTrace(PcProbeGui.real); PcProbeGui.fails++; }
        PcProbeGui.real.println("[G] ===== " + PcProbeGui.passes + " passed, " + PcProbeGui.fails + " failed; gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }

    static String logText() throws Exception {
        final java.lang.reflect.Field f = MapViewer.class.getDeclaredField("logArea");
        f.setAccessible(true);
        final String[] out = {""};
        SwingUtilities.invokeAndWait(() -> { try { out[0] = ((JTextArea) f.get(GameDriver.mapViewer)).getText(); } catch (Exception e) { out[0] = "?" + e; } });
        return out[0];
    }

    static boolean until(java.util.function.BooleanSupplier c, long ms) throws Exception {
        long end = System.currentTimeMillis() + ms;
        while (System.currentTimeMillis() < end) { if (c.getAsBoolean()) return true; Thread.sleep(50); }
        return c.getAsBoolean();
    }

    static void run() throws Exception {
        Settings.lore = false;
        PcProbe.in.feed("y\ns\nn\n");
        long end = System.currentTimeMillis() + Long.getLong("gui.waitMs", 240000L);
        while (System.currentTimeMillis() < end && !(GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null)) Thread.sleep(200);
        check("GUI opened and the player is prompting", GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null);
        final Character p = PcProbeGui.pc();
        if (p == null) return;
        p.pc().breakthroughPolicy = PlayerControl.BT_AUTO;
        Thread.sleep(800);
        final int week = GameDriver.time;
        final int mark = GameDriver.logVersion;
        GameDriver.guiInputQueue.add("policy walkfight on");
        GameDriver.guiInputQueue.add("invite #99999999");
        boolean shown = until(() -> { try { return !logText().isEmpty() && GameDriver.logVersion > mark && until0(); } catch (Exception e) { return false; } }, 4000);
        String text = logText();
        String tail = text.length() > 300 ? text.substring(text.length() - 300) : text;
        check("L1 the log pane shows the answer of a free command while the game waits (log version " + mark + " -> " + GameDriver.logVersion + ")", shown);
        check("L2 ...and no week passed meanwhile (week " + week + " -> " + GameDriver.time + ")", GameDriver.time == week);
        check("L3 the last line of the pane is the latest line of the log", !GameDriver.eventLog.isEmpty() && tail.contains(lastLine()));
        PcProbeGui.real.println("[G] info: the pane ends: " + tail.replace('\n', '|'));
        PcProbeGui.real.println("[G] DONE");
    }

    /** True when the pane's text already ends with the log's last line. */
    static boolean until0() throws Exception { String tx = logText(); return tx.contains(lastLine()); }

    static String lastLine() {
        synchronized (GameDriver.eventLog) { for (int i = GameDriver.eventLog.size() - 1; i >= 0; i--) { String s = GameDriver.eventLog.get(i); if (s != null && !s.trim().isEmpty() && s.indexOf('\n') < 0) return s; } }
        return "";
    }
}
