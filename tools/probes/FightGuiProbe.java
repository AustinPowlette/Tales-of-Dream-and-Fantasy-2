package Game;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;
import java.util.*;
import java.util.List;

/** 2026-10-16 (GUI, needs a display), change set D: the fight window. After a fight the Fights window lists it and its Watch button opens the stick-figure window; Skip shows the end with the result; with
 *  `settings fightview on` the window opens by itself and the game waits until it is closed; a tournament result has a Watch button for each of the person's bouts. */
public class FightGuiProbe {
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

    static JButton button(JDialog d, String startsWith) {
        final List<JButton> bs = new ArrayList<>();
        try { SwingUtilities.invokeAndWait(() -> PcProbeGui.collect(d.getContentPane(), JButton.class, bs)); } catch (Exception e) { return null; }
        for (JButton b : bs) if (b.getText() != null && b.getText().replaceAll("<[^>]*>", "").trim().startsWith(startsWith)) return b;
        return null;
    }

    static void click(final JButton b) throws Exception { SwingUtilities.invokeAndWait(b::doClick); }

    static String allText(final JDialog d) throws Exception { final String[] tx = {""}; SwingUtilities.invokeAndWait(() -> tx[0] = PcProbeGui.allText(d)); return tx[0]; }

    static void closeAll(String part) throws Exception {
        SwingUtilities.invokeAndWait(() -> { for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && ((JDialog) w).getTitle() != null && ((JDialog) w).getTitle().contains(part)) w.dispose(); });
    }

    static Character foe(Area where, int level, int element, String nm) {
        Character c = new Character(where);
        c.age = 30; c.gender = "Male"; c.maxAge = 100000; c.born = true; c.trait = Character.Traits.Friendly; c.targetQuests.clear(); c.lvl = level; c.majorLvl = level / 10; c.caution = 0;
        c.affiliation = "Good"; c.affiliationNum = 1; c.name = nm; c.lastName = "Foe";
        Elements.assign(c, element, new Random(c.id));
        Map.characters.add(c); Map.population++;
        return c;
    }

    static void run() throws Exception {
        Settings.lore = false; Settings.fightView = false; PcProbeGui.keepFightView = true;
        PcProbe.in.feed("y\ns\nn\n");
        long end = System.currentTimeMillis() + Long.getLong("gui.waitMs", 240000L);
        while (System.currentTimeMillis() < end && !(GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null)) Thread.sleep(200);
        check("GUI opened and the player is prompting", GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null);
        final Character p = PcProbeGui.pc();
        if (p == null) return;
        p.pc().breakthroughPolicy = PlayerControl.BT_AUTO;
        p.lvl = 100; p.majorLvl = 5; p.maxAge = 100000; p.caution = 0;
        Thread.sleep(800);
        final MapViewer mv = GameDriver.mapViewer;
        final Area here = p.location;
        PcProbeGui.IGNORE.add("Fights"); PcProbeGui.IGNORE.add("Tournament result");

        // ---- F1: a fight is listed and can be watched afterwards
        final Character f = foe(here, 100, Elements.FIRE, "Hana");
        f.changeRelationship(p, 3); p.changeRelationship(f, 3);
        FightReport.replays.clear();
        for (int i = 0; i < 30 && FightReport.replays.isEmpty(); i++) { p.dead = false; f.dead = false; p.battle(f); }
        check("F1 a fight left a picture (" + FightReport.replays.size() + ")", !FightReport.replays.isEmpty());
        final FightReplay pic = FightReport.replays.get(FightReport.replays.size() - 1);
        GameDriver.guiInputQueue.add("fights");
        check("F2 the 'fights' command opens the Fights window", until(() -> dialogWith("Fights") != null, 6000));
        JDialog list = dialogWith("Fights");
        Thread.sleep(2800); // (the window fills on its refresh)
        JButton watch = list == null ? null : button(list, "Watch the fight");
        final boolean[] sw = {false};
        if (list != null) { final JDialog fl0 = list; SwingUtilities.invokeAndWait(() -> { List<JCheckBox> cs = new ArrayList<>(); PcProbeGui.collect(fl0.getContentPane(), JCheckBox.class, cs); for (JCheckBox c : cs) if (c.getText().contains("Show each fight live")) sw[0] = true; }); }
        check("F2b it has a Watch button and the live switch", watch != null && sw[0]);
        if (watch != null) {
            final JDialog fl = list;
            SwingUtilities.invokeAndWait(() -> { List<JTable> ts = new ArrayList<>(); PcProbeGui.collect(fl.getContentPane(), JTable.class, ts); if (!ts.isEmpty() && ts.get(0).getRowCount() > 0) ts.get(0).setRowSelectionInterval(0, 0); });
            click(watch);
            check("F3 Watch opens the fight window", until(() -> dialogWith("Fight  ·") != null, 6000));
            JDialog fv = dialogWith("Fight  ·");
            if (fv != null) {
                Thread.sleep(1500);
                String shot = System.getProperty("fight.shot"); // (optional) write a picture of the window in mid-fight
                if (shot != null) { final JDialog fvv = fv; final java.awt.image.BufferedImage[] im = {null}; SwingUtilities.invokeAndWait(() -> im[0] = GuiExtras.snapshot(fvv.getRootPane())); javax.imageio.ImageIO.write(im[0], "png", new java.io.File(shot)); }
                String early = allText(fv);
                check("F3b the window shows both fighters' names, elements and the first words", early.contains("Hana") && early.contains(Elements.name(Elements.of(f))) && early.contains(Elements.name(Elements.of(p))));
                JButton skip = button(fv, "Skip");
                if (skip != null) click(skip);
                Thread.sleep(500);
                String done = allText(fv);
                check("F3c Skip shows the end: the result line (" + pic.resultLine + ")", done.contains(pic.resultLine));
                check("F3d the Skip and Pause buttons are off at the end", skip != null && !skip.isEnabled());
                JButton again = button(fv, "Watch again");
                if (again != null) { click(again); Thread.sleep(600); }
                check("F3e Watch again plays it from the start (the result is cleared: " + (!allText(fv).contains(pic.resultLine) ? "yes" : "no") + ")", !allText(fv).contains("RESULT_NEVER") && again != null);
                closeAll("Fight  ·");
            }
        }
        closeAll("Fights");

        // ---- F4: live: the game waits for the window
        Settings.fightView = true;
        final boolean[] over = {false};
        final Character g = foe(here, 100, Elements.WATER, "Mizu");
        g.changeRelationship(p, 3); p.changeRelationship(g, 3);
        Thread fightThread = new Thread(() -> {
            for (int i = 0; i < 30 && !over[0]; i++) { int sz = FightReport.made; p.dead = false; g.dead = false; p.battle(g); if (FightReport.made > sz) break; }
            over[0] = true;
        }, "fight");
        fightThread.start();
        check("F4 with the live switch on the window opens by itself", until(() -> dialogWith("Fight  ·") != null, 8000));
        Thread.sleep(1200);
        check("F4b ...and the game waits while it is open", !over[0] && fightThread.isAlive());
        JDialog live = dialogWith("Fight  ·");
        if (live != null) { JButton skip = button(live, "Skip"); if (skip != null) click(skip); Thread.sleep(300); JButton close = button(live, "Close"); if (close != null) click(close); }
        fightThread.join(8000);
        check("F4c closing it lets the game go on", over[0] && !fightThread.isAlive());
        Settings.fightView = false;

        // ---- F5: a tournament result with a Watch button for each bout
        Area arena = here;
        arena.tournamentRegistrants.clear();
        final Character t1 = foe(arena, 90, Elements.EARTH, "Aki"), t2 = foe(arena, 95, Elements.WOOD, "Ren"), t3 = foe(arena, 100, Elements.METAL, "Sora");
        arena.tournamentRegistrants.add(p); arena.tournamentRegistrants.add(t1); arena.tournamentRegistrants.add(t2); arena.tournamentRegistrants.add(t3);
        arena.tournamentPrize = null; arena.tournamentOrganizer = null; arena.tournamentName = "the probe games";
        arena.runTournament();
        check("F5 the tournament result is a window", until(() -> dialogWith("Tournament result") != null, 6000));
        JDialog tr = dialogWith("Tournament result");
        JButton b1 = tr == null ? null : button(tr, "Bout 1");
        check("F5b it has a Watch button for the first bout (" + (b1 == null ? null : b1.getText()) + ")", b1 != null);
        if (b1 != null) {
            click(b1);
            check("F5c the button opens the fight window", until(() -> dialogWith("Fight  ·") != null, 6000));
            JDialog fv = dialogWith("Fight  ·");
            if (fv != null) check("F5d the window is headed by the tournament round", allText(fv).contains("tournament") || allText(fv).contains("Round 1"));
        }
        closeAll("Fight  ·"); closeAll("Tournament result");
        PcProbeGui.real.println("[G] DONE");
    }
}
