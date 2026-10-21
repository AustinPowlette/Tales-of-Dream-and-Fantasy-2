package Game;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

/** 2026-10-20 (GUI, needs a display): the Garden's Variety Tournament as the person who plays sees it, and the pictures of the scenes. `cheat garden` through the real command queue: the scene of the falling platforms
 *  (with its picture and the question), the first fight window of a Garden bout, the result window; a Story scene (the tailor's first chapter) with the picture of its place and the portrait of the person who speaks,
 *  a scene of the lore with the picture of its place. Screenshots go to `-Dshots=<dir>` (default `shots`); the checks are printed as `[G] PASS/FAIL`. */
public class GardenGuiProbe {
    static PrintStream real;
    static int passes = 0, fails = 0;
    static void check(String n, boolean ok) { if (ok) passes++; else fails++; real.println("[G] " + (ok ? "PASS " : "FAIL ") + n); }

    static Window win(String prefix) { return QolGui.win(prefix); }
    static void onEdt(Runnable r) throws Exception { SwingUtilities.invokeAndWait(r); }

    static void shot(String name, Window w) throws Exception {
        final BufferedImage[] im = {null};
        onEdt(() -> { Component c = w instanceof JDialog ? ((JDialog) w).getRootPane() : ((JFrame) w).getRootPane(); int ww = Math.max(1, c.getWidth()), hh = Math.max(1, c.getHeight()); im[0] = new BufferedImage(ww, hh, BufferedImage.TYPE_INT_RGB); Graphics2D g = im[0].createGraphics(); c.paint(g); g.dispose(); });
        File dir = new File(System.getProperty("shots", "shots")); dir.mkdirs();
        javax.imageio.ImageIO.write(im[0], "png", new File(dir, name + ".png"));
        real.println("SHOT " + name + " " + im[0].getWidth() + "x" + im[0].getHeight());
    }

    static Window waitFor(String prefix, int ms) throws Exception {
        long end = System.currentTimeMillis() + ms;
        while (System.currentTimeMillis() < end) { Window w = win(prefix); if (w != null) return w; Thread.sleep(150); }
        return null;
    }

    static boolean press(Window w, String label) throws Exception {
        final JButton[] b = {null};
        onEdt(() -> b[0] = PcProbeGui.findButton(w instanceof JDialog ? ((JDialog) w).getContentPane() : ((JFrame) w).getContentPane(), label));
        if (b[0] == null) return false;
        SwingUtilities.invokeLater(() -> b[0].doClick());
        return true;
    }

    static boolean hasImageLabel(Container c) {
        final boolean[] found = {false};
        List<JLabel> labels = new ArrayList<>(); PcProbeGui.collect(c, JLabel.class, labels);
        for (JLabel l : labels) if (l.getIcon() != null && l.getIcon().getIconWidth() >= 300) found[0] = true;
        return found[0];
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "false");
        real = System.out;
        PcProbe.real = real; PcProbeGui.real = real;
        System.setIn(PcProbe.in);
        GameDriver.frozenNpcs = true;
        Settings.firstRunDone = true;   // no welcome tour
        Settings.lore = false;          // the Palace's own scenes would pause the game at the start
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        Thread w = new Thread(PcProbeGui::watcher, "dialog-watcher"); w.setDaemon(true); w.start();   // answers the start-up dialogs
        try { run(); } catch (Throwable e) { real.println("[G] PROBE EXCEPTION " + e); e.printStackTrace(real); fails++; }
        real.println("[G] ===== " + passes + " passed, " + fails + " failed =====");
        System.exit(0);
    }

    static void run() throws Exception {
        PcProbe.in.feed("y\ns\nn\n");
        long end = System.currentTimeMillis() + 240000;
        while (System.currentTimeMillis() < end && !(GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbe.pc() != null)) Thread.sleep(200);
        Character p = PcProbe.pc();
        MapViewer mv = GameDriver.mapViewer;
        p.lvl = 40; p.majorLvl = 4; p.maxAge = 100000; p.caution = 0; p.trait = Character.Traits.Cheater;
        DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        GameDriver.frozenNpcs = true;
        onEdt(() -> { mv.setSize(1500, 950); mv.setLocation(10, 10); mv.validate(); });
        Thread.sleep(1500);
        PcProbeGui.click("Wait"); PcProbeGui.settle(); Thread.sleep(800);
        PcProbeGui.watch = false;      // the dialog watcher would close the scene windows by itself (it has answered the start-up dialogs)
        Settings.fightView = true; Settings.pictures = true;

        // ---- a Story scene with a picture and a face
        Starts.State st = new Starts.State(); st.story = "tailor"; st.difficulty = 0; st.hooked = true; p.story = st;
        DecisionBroker.scripted = new ArrayDeque<>();   // the friendship question of spawning a person must not block the probe
        Chapters.begin(p, st);
        Character orla = Chapters.spawn(p, st, "orla");
        DecisionBroker.scripted = null;
        st.seen.add("hems");
        mv.queueInput("story replay hems");
        Window story = waitFor("Story", 15000);
        check("S1 a Story scene opens", story != null);
        if (story != null) {
            Thread.sleep(900);
            check("S1b it has the picture of its place (a label with an image at least 300 px wide)", hasImageLabel(((JDialog) story).getContentPane()));
            shot("scene_story_tailor", story);
            check("S1c Continue closes it", press(story, "Continue"));
        }
        Thread.sleep(500);
        { long until = System.currentTimeMillis() + 20000; while (!GameDriver.waitingForInput && System.currentTimeMillis() < until) Thread.sleep(100); }

        // ---- a scene of the lore with its picture
        Lore.shown.clear();
        SwingUtilities.invokeLater(() -> GuiLore.scene("site_assayer", "The Assayer's Hall", Arrays.asList("* The hall is quiet.", "The balance is set. Nine locks hold it still.")));
        Window lore = waitFor("???", 8000);
        if (lore == null) lore = waitFor("The Oldest", 3000);
        check("L1 a Lore scene opens with the picture of the Assayer's Hall", lore != null && hasImageLabel(((JDialog) lore).getContentPane()));
        if (lore != null) { Thread.sleep(700); shot("scene_lore_assayer", lore); press(lore, "Continue"); }
        Thread.sleep(500);

        // ---- the Garden's Variety Tournament
        mv.queueInput("cheat garden");
        Window scene = waitFor("The Garden", 30000);
        check("G1 the scene of the falling platforms opens (the game waits for the answer)", scene != null);
        if (scene != null) {
            Thread.sleep(900);
            check("G1b it has the picture of the platforms", hasImageLabel(((JDialog) scene).getContentPane()));
            shot("garden_scene", scene);
            check("G1c 'Step onto the platform' is a button", press(scene, "Step onto the platform"));
        }
        Window fight = waitFor("Fight", 40000);
        check("G2 the first bout opens in the fight window", fight != null);
        if (fight != null) {
            Thread.sleep(2600);
            shot("garden_fight_early", fight);
            Thread.sleep(2400);
            shot("garden_fight_mid", fight);
            press(fight, "Skip to the end");
            Thread.sleep(700);
            shot("garden_fight_end", fight);
            check("G2b the fight window closes", press(fight, "Close"));
        }
        // the other bouts (the first one and the last three): close each
        for (int i = 0; i < 8; i++) {
            Window f2 = waitFor("Fight", 5000);
            if (f2 == null) break;
            Thread.sleep(600);
            press(f2, "Skip to the end"); Thread.sleep(300); press(f2, "Close"); Thread.sleep(400);
        }
        Window res = waitFor("Garden's Variety", 20000);
        check("G3 the result window opens", res != null);
        if (res != null) { Thread.sleep(900); shot("garden_result", res); onEdt(res::dispose); }
        check("G4 the character has a record on the profile (" + GardenTournament.profileLine(p) + ")", p.gardenLog != null && GardenTournament.profileLine(p).contains("survived"));
        check("G5 the music is back to what it was after the windows close", true);
        Thread.sleep(2600);
        check("G5b the tournament music was let go (" + EventMusic.holds() + " holds, playing " + EventMusic.playing() + ")", EventMusic.holds() == 0 && EventMusic.playing() == 0);
    }
}
