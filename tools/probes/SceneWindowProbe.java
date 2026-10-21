package Game;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;
import java.util.*;
import java.util.List;

/** 2026-10-13 (GUI, needs a display): the one scene window. Scenes queued in a burst open one after the other in a single window (never two at once), a scene is cut into pages of narration and
 *  speech with Continue / Back, a scene with a question shows its buttons on the last page, what follows the answer appears as more pages of the same window, the X takes the default answer,
 *  the game stays paused while scenes are queued and runs on when the last is closed. */
public class SceneWindowProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    static void check(String n, boolean ok) { PcProbeGui.check(n, ok); }
    static void info(String s) { PcProbeGui.real.println("[G] info: " + s); }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "false");
        PcProbeGui.real = System.out;
        PcProbe.real = PcProbeGui.real;
        System.setIn(PcProbe.in);
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(PcProbeGui.real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        Thread w = new Thread(PcProbeGui::watcher, "dialog-watcher"); w.setDaemon(true); w.start(); // answers the start-up dialogs (it leaves scene windows alone)
        PcProbeGui.SHEETS.clear();
        try { run(); } catch (Throwable e) { PcProbeGui.real.println("[G] PROBE EXCEPTION " + e); e.printStackTrace(PcProbeGui.real); PcProbeGui.fails++; }
        PcProbeGui.real.println("[G] ===== " + PcProbeGui.passes + " passed, " + PcProbeGui.fails + " failed; gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }

    static List<JDialog> sceneWindows() {
        List<JDialog> out = new ArrayList<>();
        for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && ((JDialog) w).getTitle() != null && ((JDialog) w).getTitle().contains("  ·  ")) out.add((JDialog) w);
        return out;
    }
    static boolean until(java.util.function.BooleanSupplier c, long ms) throws Exception { long end = System.currentTimeMillis() + ms; while (System.currentTimeMillis() < end) { if (c.getAsBoolean()) return true; Thread.sleep(40); } return c.getAsBoolean(); }
    static String text(JDialog d) { final String[] r = {""}; try { SwingUtilities.invokeAndWait(() -> r[0] = PcProbeGui.allText(d.getContentPane())); } catch (Exception e) { } return r[0]; }
    static boolean press(JDialog d, String label) throws Exception {
        final boolean[] ok = {false};
        SwingUtilities.invokeAndWait(() -> { JButton b = PcProbeGui.findButton(d.getContentPane(), label); if (b != null) { b.doClick(); ok[0] = true; } });
        return ok[0];
    }
    static void closeWithX(JDialog d) throws Exception {
        SwingUtilities.invokeAndWait(() -> d.dispatchEvent(new java.awt.event.WindowEvent(d, java.awt.event.WindowEvent.WINDOW_CLOSING)));
    }

    static void run() throws Exception {
        Settings.lore = false; Settings.lorePause = true;
        PcProbe.in.feed("y\ns\nn\n");
        long end = System.currentTimeMillis() + Long.getLong("gui.waitMs", 240000L);
        while (System.currentTimeMillis() < end && !(GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null)) Thread.sleep(200);
        check("GUI opened and the player is prompting", GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null);
        if (PcProbeGui.pc() == null) return;
        Thread.sleep(800);
        // close the welcome tour if it is up (it would count as a window)
        for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && !((JDialog) w).getTitle().contains("  ·  ")) { final Window ww = w; SwingUtilities.invokeAndWait(ww::dispose); }

        // ---- pages
        List<String> speech = Arrays.asList("* The door opens.", "OLD MAN: Come in.", "OLD MAN: Sit.", "* He pours tea.", "OLD MAN: Drink.", "* Night falls.");
        check("W1 a scene is cut into pages at each block of narration that follows speech (" + GuiScenes.paginate(speech).size() + " pages)", GuiScenes.paginate(speech).size() == 3);
        List<String> longSpeech = new ArrayList<>(); for (int i = 0; i < 20; i++) longSpeech.add("OLD MAN: line " + i);
        check("W1b a long speech is cut into pages of at most " + GuiScenes.PAGE_MAX_LINES + " lines (" + GuiScenes.paginate(longSpeech).size() + " pages)", GuiScenes.paginate(longSpeech).size() == 3);

        // ---- a burst of scenes: one window at a time, in order, the game paused meanwhile
        GameDriver.paused = false;
        GuiScenes.maxWindows = 0;
        GuiLore.scene("a", "Alpha", speech);
        GuiLore.story("Beta", Arrays.asList("* A quiet morning.", "@Mei| Hello."));
        GuiLore.scene("c", "Gamma", Arrays.asList("* Wind.", "The Garden listens."));
        GuiLore.story("Delta", Arrays.asList("* Last."));
        check("W2 busy() is true at once, before any window exists", GuiScenes.busy() && GuiLore.busy());
        check("W2b the game is held paused while scenes wait", GameDriver.paused);
        List<String> seen = new ArrayList<>();
        int mostAtOnce = 0, pagesClicked = 0;
        long deadline = System.currentTimeMillis() + 30000;
        while (GuiScenes.busy() && System.currentTimeMillis() < deadline) {
            List<JDialog> ws = sceneWindows();
            mostAtOnce = Math.max(mostAtOnce, ws.size());
            if (ws.size() == 1) {
                JDialog d = ws.get(0);
                String title = d.getTitle();
                if (seen.isEmpty() || !seen.get(seen.size() - 1).equals(title)) seen.add(title);
                if (press(d, "Continue ▸")) pagesClicked++;
                else if (!press(d, "Continue")) press(d, "Close");
            }
            Thread.sleep(60);
        }
        info("windows in order: " + seen + "; most at once " + mostAtOnce + ", pages clicked through " + pagesClicked);
        check("W3 never more than one scene window at a time (most seen " + mostAtOnce + ", counted in the code " + GuiScenes.maxWindows + ")", mostAtOnce <= 1 && GuiScenes.maxWindows <= 1);
        check("W3b all four scenes were shown, in the order they came", seen.size() == 4 && seen.get(0).contains("Alpha") && seen.get(1).contains("Beta") && seen.get(2).contains("Gamma") && seen.get(3).contains("Delta"));
        check("W3c the first scene took its three pages (Continue clicked " + pagesClicked + " times on pages)", pagesClicked >= 2);
        check("W3d when the last scene is closed the game runs on (paused " + GameDriver.paused + ", busy " + GuiScenes.busy() + ")", !GameDriver.paused && !GuiScenes.busy());

        // ---- a scene with a question
        final int[] answer = {-9};
        Thread asker = new Thread(() -> answer[0] = GuiScenes.ask(PcProbeGui.pc(), "Story", "The Fork", speech, "Which road?", new String[]{"Left", "Middle", "Right"}, 1));
        asker.start();
        check("W4 a scene with a question opens a window", until(() -> sceneWindows().size() == 1, 5000));
        JDialog d = sceneWindows().isEmpty() ? null : sceneWindows().get(0);
        if (d != null) {
            check("W4b the answers are not shown on the first page", !text(d).contains("Which road?") && PcProbeGui.findButton(d.getContentPane(), "Middle") == null);
            press(d, "Continue ▸"); Thread.sleep(150); press(d, "Continue ▸"); Thread.sleep(200);
            check("W4c the question and its buttons are on the last page", text(d).contains("Which road?") && PcProbeGui.findButton(d.getContentPane(), "Right") != null);
            int windowsBefore = sceneWindows().size();
            press(d, "Right");
            asker.join(5000);
            check("W4d the answer reaches the game thread (" + answer[0] + ")", answer[0] == 2);
            GuiScenes.supplyAfter(Arrays.asList("* You take the right road.", "@Mei| Good choice."));
            check("W4e what follows the answer appears in the same window", until(() -> text(d).contains("You take the right road") || text(d).contains("right road"), 4000) && sceneWindows().size() == windowsBefore);
            press(d, "Continue");
            check("W4f the window closes at the end and the game is released", until(() -> sceneWindows().isEmpty() && !GuiScenes.busy() && !GameDriver.paused, 5000));
        }

        // ---- the X takes the default answer
        answer[0] = -9;
        Thread asker2 = new Thread(() -> answer[0] = GuiScenes.ask(PcProbeGui.pc(), "Story", "Another Fork", speech, "Which road?", new String[]{"Left", "Middle", "Right"}, 1));
        asker2.start();
        check("W5 the second question opens", until(() -> sceneWindows().size() == 1, 5000));
        if (!sceneWindows().isEmpty()) {
            closeWithX(sceneWindows().get(0));
            asker2.join(5000);
            check("W5b closing with the X takes the default answer (" + answer[0] + ")", answer[0] == 1);
            check("W5c the window is gone and the game runs on", until(() -> sceneWindows().isEmpty() && !GuiScenes.busy() && !GameDriver.paused, 5000));
        }
        // ---- the ending with credits: the credits keep the scene system busy until they are closed
        GuiLore.ending("The End", Arrays.asList("* It is over."), "stats here", Arrays.asList("CREDITS", "", "Nobody was harmed"));
        check("W6 the ending opens", until(() -> sceneWindows().size() == 1, 5000));
        if (!sceneWindows().isEmpty()) {
            press(sceneWindows().get(0), "Credits ▸");
            Thread.sleep(500);
            check("W6b while the credits run the scene system is still busy", GuiScenes.busy());
            for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && "Credits".equals(((JDialog) w).getTitle())) { final Window ww = w; SwingUtilities.invokeAndWait(ww::dispose); }
            check("W6c closing the credits releases it", until(() -> !GuiScenes.busy() && !GameDriver.paused, 5000));
        }
        PcProbeGui.real.println("[G] DONE");
    }
}
