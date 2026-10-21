package Game;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;
import java.util.*;
import java.util.List;

/** 2026-10-16 (GUI, needs a display), change set B: a little story opens in the scene window headed "A little story" with its choices on the last page, the game waits for the answer and the answer is the button
 *  pressed; a lesson opens in the portrait window (the speaker's card, the words, the reply "Thank you. That helps.", then Continue). */
public class VignetteGuiProbe {
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

    static boolean until(java.util.function.BooleanSupplier c, long ms) throws Exception {
        long end = System.currentTimeMillis() + ms;
        while (System.currentTimeMillis() < end) { if (c.getAsBoolean()) return true; Thread.sleep(50); }
        return c.getAsBoolean();
    }

    static JDialog dialogWith(String part) {
        for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && ((JDialog) w).getTitle() != null && ((JDialog) w).getTitle().contains(part)) return (JDialog) w;
        return null;
    }

    static JDialog dialogNamed(String name) {
        for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && name.equals(w.getName())) return (JDialog) w;
        return null;
    }

    static JButton button(JDialog d, String startsWith) {
        final List<JButton> bs = new ArrayList<>();
        try { SwingUtilities.invokeAndWait(() -> PcProbeGui.collect(d.getContentPane(), JButton.class, bs)); } catch (Exception e) { return null; }
        for (JButton b : bs) if (b.getText() != null && b.getText().replaceAll("<[^>]*>", "").trim().startsWith(startsWith)) return b;
        return null;
    }

    static void click(final JButton b) throws Exception { SwingUtilities.invokeAndWait(b::doClick); }

    static void run() throws Exception {
        Settings.lore = false; Settings.lessons = true; Settings.vignettes = true;
        PcProbe.in.feed("y\ns\nn\n");
        long end = System.currentTimeMillis() + Long.getLong("gui.waitMs", 240000L);
        while (System.currentTimeMillis() < end && !(GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null)) Thread.sleep(200);
        check("GUI opened and the player is prompting", GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null);
        final Character p = PcProbeGui.pc();
        if (p == null) return;
        p.pc().breakthroughPolicy = PlayerControl.BT_AUTO;
        Thread.sleep(800);
        PcProbeGui.IGNORE.add("A little story  ·  The riddle at the bridge");

        // ---- a little story with a question
        final Vignettes.Story bridge = Vignettes.ALL.get(0);
        final int[] picked = {-9};
        Thread t1 = new Thread(() -> { picked[0] = GuiScenes.ask(p, "A little story", bridge.title, new ArrayList<>(Arrays.asList(bridge.text)), bridge.prompt, bridge.options, bridge.dflt); }, "story");
        t1.start();
        check("V1 a little story opens in the scene window headed 'A little story'", until(() -> dialogWith("A little story") != null, 6000));
        JDialog d = dialogWith("A little story");
        JButton b = d == null ? null : button(d, "\"A dream");
        check("V2 its choices are buttons on the last page (found: " + (b == null ? null : b.getText()) + ")", b != null);
        if (b != null) {
            click(b);
            t1.join(8000);
            check("V3 the game thread got the answer that was pressed (" + picked[0] + ")", picked[0] == 1);
            GuiScenes.supplyAfter(Arrays.asList("* He waves you across.", "@Bridge keeper| Go on."));
            Thread.sleep(600);
            JDialog d2 = dialogWith("A little story");
            check("V4 what follows the answer is shown in the same window", d2 != null);
            if (d2 != null) { JButton cont = button(d2, "Continue"); if (cont != null) click(cont); Thread.sleep(300); }
            check("V5 the window closes after the last page", until(() -> dialogWith("A little story") == null, 4000));
        }

        // ---- a lesson in the portrait window
        final Character npc = new Character(p.location);
        npc.age = 40; npc.gender = "Female"; npc.maxAge = 100000; npc.born = true; npc.trait = Character.Traits.Friendly; npc.name = "Mei"; npc.lastName = "Ash"; npc.affiliation = "Good"; npc.affiliationNum = 1;
        Map.characters.add(npc); Map.population++;
        PlayerControl pc = p.pc();
        pc.lessonsSeen = new HashSet<>(); pc.nextLessonWeek = 0; p.majorLvl = 0; p.minorLvl = 3; GameDriver.time = Math.max(GameDriver.time, 5);
        final boolean[] done = {false};
        Thread t2 = new Thread(() -> {
            GuiDialogue g = new GuiDialogue(p);
            Dialogue.Ctx c = new Dialogue.Ctx(npc, p, g);
            g.open(npc, p);
            try { Lessons.give(c); } finally { g.close(); }
            done[0] = true;
        }, "lesson");
        t2.start();
        check("L1 a lesson opens the portrait window of the speaker", until(() -> dialogNamed("dialogue") != null, 6000));
        JDialog ld = dialogNamed("dialogue");
        JButton thanks = null;
        for (int i = 0; i < 80 && thanks == null && ld != null; i++) { thanks = button(ld, "1."); if (thanks != null && !thanks.getText().contains("Thank")) thanks = null; Thread.sleep(100); }
        check("L2 the reply 'Thank you. That helps.' is a button (" + (thanks == null ? null : thanks.getText()) + ")", thanks != null);
        if (thanks != null) {
            String text = null;
            if (ld != null) { final String[] tx = {""}; final JDialog fl = ld; SwingUtilities.invokeAndWait(() -> tx[0] = PcProbeGui.allText(fl)); text = tx[0]; }
            check("L2b the window shows the speaker's name and the lesson's words", text != null && text.contains("Mei Ash") && text.contains("qi"));
            click(thanks);
            JButton cont = null;
            for (int i = 0; i < 80 && cont == null; i++) { cont = button(ld, "1."); if (cont != null && !cont.getText().contains("Continue")) cont = null; Thread.sleep(100); }
            check("L3 then Continue closes the talk", cont != null);
            if (cont != null) click(cont);
            t2.join(8000);
            check("L4 the talk ends and the lesson is marked as told (" + pc.lessonsSeen + ")", done[0] && pc.lessonsSeen.contains("qi"));
        }
        PcProbeGui.real.println("[G] DONE");
    }
}
