package Game;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;
import java.util.*;
import java.util.List;

/** 2026-10-13 (GUI, needs a display): the after-death window. The X (and Escape) cannot close it; it offers Spectate, Save and quit, Quit; Spectate slows the world to 2 seconds a week.
 *  `-Ddeath.choice=Spectate` (default) checks the first and last; `-Ddeath.choice=Save` presses "Save and quit" (the program ends; look for saves/quick.dat afterwards);
 *  `-Ddeath.choice=Quit` presses "Quit the game" (the program ends, nothing is saved). */
public class DeathMenuProbe {
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
        PcProbeGui.SHEETS.clear(); PcProbeGui.IGNORE.add("After death");
        try { run(); } catch (Throwable e) { PcProbeGui.real.println("[G] PROBE EXCEPTION " + e); e.printStackTrace(PcProbeGui.real); PcProbeGui.fails++; }
        PcProbeGui.real.println("[G] ===== " + PcProbeGui.passes + " passed, " + PcProbeGui.fails + " failed; gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }

    static JDialog deathWindow() {
        for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && "After death".equals(((JDialog) w).getTitle())) return (JDialog) w;
        return null;
    }
    static boolean until(java.util.function.BooleanSupplier c, long ms) throws Exception { long end = System.currentTimeMillis() + ms; while (System.currentTimeMillis() < end) { if (c.getAsBoolean()) return true; Thread.sleep(40); } return c.getAsBoolean(); }

    static void run() throws Exception {
        String choice = System.getProperty("death.choice", "Spectate");
        Settings.lore = false;
        PcProbe.in.feed("y\ns\nn\n");
        long end = System.currentTimeMillis() + Long.getLong("gui.waitMs", 240000L);
        while (System.currentTimeMillis() < end && !(GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null)) Thread.sleep(200);
        check("GUI opened and the player is prompting", GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null);
        Character p = PcProbeGui.pc();
        if (p == null) return;
        p.maxAge = 100000;
        GameDriver.tickDelayMs = 0;
        // the character dies; the end of the tick asks what happens next
        p.deathCause = "the probe"; p.dead();
        GameDriver.guiInputQueue.add("x");
        check("D1 the after-death window opens", until(() -> deathWindow() != null, 30000));
        JDialog d = deathWindow();
        if (d == null) return;
        final String[] labels = {""};
        SwingUtilities.invokeAndWait(() -> { java.util.List<JButton> bs = new ArrayList<>(); PcProbeGui.collect(d.getContentPane(), JButton.class, bs); StringBuilder sb = new StringBuilder(); for (JButton b : bs) sb.append(b.getText()).append(" | "); labels[0] = sb.toString(); });
        PcProbeGui.real.println("[G] info: buttons: " + labels[0]);
        check("D2 it offers Spectate, Save and quit, and Quit", labels[0].contains("Spectate") && labels[0].contains("Save and quit") && labels[0].contains("Quit the game"));
        // the X and Escape do nothing
        SwingUtilities.invokeAndWait(() -> d.dispatchEvent(new java.awt.event.WindowEvent(d, java.awt.event.WindowEvent.WINDOW_CLOSING)));
        Thread.sleep(500);
        check("D3 the X cannot close it", deathWindow() == d && d.isShowing());
        SwingUtilities.invokeAndWait(() -> { javax.swing.KeyStroke esc = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0); Object k = d.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(esc); if (k != null) d.getRootPane().getActionMap().get(k).actionPerformed(null); });
        Thread.sleep(300);
        check("D3b Escape cannot close it either", deathWindow() == d && d.isShowing());
        check("D3c the game is waiting for the answer (the speed is still " + GameDriver.tickDelayMs + ")", GameDriver.tickDelayMs == 0);
        final String want = choice.equals("Save") ? "Save and quit" : choice.equals("Quit") ? "Quit the game" : "Spectate";
        SwingUtilities.invokeAndWait(() -> { JButton b = PcProbeGui.findButton(d.getContentPane(), want); if (b != null) b.doClick(); });
        if (choice.equals("Spectate")) {
            check("D4 Spectate closes the window", until(() -> deathWindow() == null, 10000));
            check("D5 Spectate slows the world to 2 seconds a week (" + GameDriver.tickDelayMs + " ms)", until(() -> GameDriver.tickDelayMs == 2000, 5000));
            check("D6 nobody plays and the world goes on (a spectator follows someone)", until(() -> PcProbeGui.pc() == null, 5000));
        } else {
            PcProbeGui.real.println("[G] info: pressed '" + want + "': the program should end by itself");
            Thread.sleep(20000);
            PcProbeGui.real.println("[G] FAIL the program did not end after '" + want + "'");
            PcProbeGui.fails++;
        }
    }
}
