package Game;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;
import java.util.*;
import java.util.List;

/** 2026-10-17 (GUI, needs a display): the Music Manager window (the list of songs with a tick for each, Play now, Every song), the tabbed Cheats window (its buttons send `cheat ...` commands), the fight window's
 *  check box that turns the live window off (the Settings box follows it), the animal and the place picture in the fight window, and the faster mouse wheel. */
public class ManagerGuiProbe {
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

    static Character foe(Area where, int level, int element, String nm, Character.Traits tr) {
        Character c = new Character(where);
        c.age = 30; c.gender = "Male"; c.maxAge = 100000; c.born = true; c.trait = tr; c.targetQuests.clear(); c.lvl = level; c.majorLvl = level / 10; c.caution = 0;
        c.affiliation = "Good"; c.affiliationNum = 1; c.name = nm; c.lastName = "Foe";
        while (Map.characters.size() < c.id) Map.characters.add(null);
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
        Thread.sleep(800);
        final MapViewer mv = GameDriver.mapViewer;
        PcProbeGui.IGNORE.add("Music Manager"); PcProbeGui.IGNORE.add("Cheats");

        // ---- M: the Music Manager
        Playlist.clear();
        SwingUtilities.invokeLater(() -> GuiMusic.open(mv));
        check("M1 the Music Manager opens", until(() -> dialogWith("Music Manager") != null, 6000));
        final JDialog md = dialogWith("Music Manager");
        final List<JTable> tables = new ArrayList<>();
        SwingUtilities.invokeAndWait(() -> PcProbeGui.collect(md.getContentPane(), JTable.class, tables));
        JTable tbl = tables.isEmpty() ? null : tables.get(0);
        check("M2 it lists every song and every concert piece (" + (tbl == null ? 0 : tbl.getRowCount()) + ")", tbl != null && tbl.getRowCount() == Playlist.library().size() && tbl.getRowCount() >= 80);
        final JTable table = tbl;
        final Boolean[] first = {null};
        SwingUtilities.invokeAndWait(() -> first[0] = (Boolean) table.getValueAt(0, 0));
        check("M2b with no playlist every song is ticked", Boolean.TRUE.equals(first[0]));
        SwingUtilities.invokeAndWait(() -> table.setValueAt(Boolean.FALSE, 0, 0)); // untick the first
        Thread.sleep(400);
        check("M3 unticking a song makes a playlist of all the others (" + Settings.musicPlaylist.size() + ")", Settings.musicFilter && Settings.musicPlaylist.size() == SongBook.all().size() - 1 && !Playlist.in(SongBook.all().get(0)));
        JButton every = button(md, "Every song");
        click(every); Thread.sleep(400);
        check("M3b 'Every song' clears it", !Settings.musicFilter);
        SwingUtilities.invokeAndWait(() -> table.setRowSelectionInterval(2, 2));
        Score third = null;
        final String title = (String) table.getValueAt(2, 1);
        for (Score s : Playlist.library()) if (s.title.equals(title)) third = s;
        click(button(md, "Play now")); Thread.sleep(300);
        check("M4 Play now holds the selected song (" + (Ambient.pinned() == null ? null : Ambient.pinned().title) + ")", third != null && Ambient.pinned() == third && Settings.music);
        click(button(md, "Back to the playlist")); Thread.sleep(300);
        check("M4b Back to the playlist lets the places choose again", Ambient.pinned() == null);
        SwingUtilities.invokeAndWait(md::dispose);

        // ---- C: the Cheats window
        p.trait = Character.Traits.Cheater;
        SwingUtilities.invokeLater(() -> GuiSheets.cheatWindow(mv));
        check("C1 the Cheats window opens with tabs", until(() -> dialogWith("Cheats") != null, 6000));
        final JDialog cd = dialogWith("Cheats");
        final List<JTabbedPane> tps = new ArrayList<>();
        SwingUtilities.invokeAndWait(() -> PcProbeGui.collect(cd.getContentPane(), JTabbedPane.class, tps));
        StringBuilder names = new StringBuilder();
        if (!tps.isEmpty()) for (int i = 0; i < tps.get(0).getTabCount(); i++) names.append(tps.get(0).getTitleAt(i)).append(' ');
        check("C1b its tabs: " + names, names.toString().contains("People") && names.toString().contains("Items") && names.toString().contains("Policies") && names.toString().contains("World"));
        check("C1c ...and the third set's tabs: Growth, This tile, Time", names.toString().contains("Growth") && names.toString().contains("This tile") && names.toString().contains("Time"));
        p.martialTechnique = new Technique(Technique.techniqueType.Martial, p, 60); p.martialTechnique.lvl = 2;
        final JButton[] mastery = {null};
        SwingUtilities.invokeAndWait(() -> { List<JButton> bs = new ArrayList<>(); PcProbeGui.collect(cd.getContentPane(), JButton.class, bs); for (JButton b : bs) if ("Master every technique".equals(b.getText())) mastery[0] = b; });
        check("C1d the Growth tab has the 'Master every technique' button", mastery[0] != null);
        if (mastery[0] != null) { SwingUtilities.invokeAndWait(() -> mastery[0].doClick()); Thread.sleep(1500); check("C1e pressing it sends 'cheat mastery' (technique " + p.martialTechnique.lvl + "/" + p.martialTechnique.potential + ")", p.martialTechnique.lvl == p.martialTechnique.potential); }
        final JCheckBox[] evade = {null};
        SwingUtilities.invokeAndWait(() -> { List<JCheckBox> cs = new ArrayList<>(); PcProbeGui.collect(cd.getContentPane(), JCheckBox.class, cs); for (JCheckBox c : cs) if (c.getText().startsWith("Evade death")) evade[0] = c; });
        check("C2 the Policies tab has the Evade death box", evade[0] != null);
        if (evade[0] != null) { SwingUtilities.invokeAndWait(() -> evade[0].doClick()); Thread.sleep(1500); check("C2b ticking it sends the cheat (evade death is on: " + p.pc().evadeDeath + ")", p.pc().evadeDeath); }
        SwingUtilities.invokeAndWait(cd::dispose);

        // ---- F: the fight window: an animal, a place, the check box
        Area here = p.location;
        Character wolf = foe(here, 30, Elements.WOOD, "Grey Wolf", Character.Traits.Beast); wolf.masterId = -1;
        FightReport.replays.clear();
        for (int i = 0; i < 20 && FightReport.replays.isEmpty(); i++) { p.dead = false; wolf.dead = false; p.lvl = 120; p.battle(wolf); }
        check("F1 a hunt of a beast left a picture whose opponent is a beast", !FightReport.replays.isEmpty() && FightReport.replays.get(FightReport.replays.size() - 1).kindB == 1);
        FightReplay pic = FightReport.replays.get(FightReport.replays.size() - 1);
        GuiFightView.replay(mv, pic);
        check("F2 the fight window opens", until(() -> dialogWith("Fight  ·") != null, 6000));
        final JDialog fd = dialogWith("Fight  ·");
        Thread.sleep(2500);
        String shot = System.getProperty("fight.shot2");
        if (shot != null && fd != null) { final java.awt.image.BufferedImage[] im = {null}; SwingUtilities.invokeAndWait(() -> im[0] = GuiExtras.snapshot(fd.getRootPane())); javax.imageio.ImageIO.write(im[0], "png", new java.io.File(shot)); }
        final JCheckBox[] noAuto = {null};
        SwingUtilities.invokeAndWait(() -> { List<JCheckBox> cs = new ArrayList<>(); PcProbeGui.collect(fd.getContentPane(), JCheckBox.class, cs); for (JCheckBox c : cs) if (c.getText().startsWith("Do not open")) noAuto[0] = c; });
        check("F3 the fight window has the check box 'Do not open this window by itself' (not ticked while the live window is on: " + (noAuto[0] != null && !noAuto[0].isSelected()) + ")", noAuto[0] != null && Settings.fightView == !noAuto[0].isSelected());
        Settings.fightView = true;
        SwingUtilities.invokeAndWait(() -> noAuto[0].setSelected(false));
        final JCheckBox[] settingsBox = {null};
        SwingUtilities.invokeLater(() -> GuiExtras.settingsWindow(mv));
        Thread.sleep(1500);
        JDialog sd = dialogWith("Settings");
        if (sd != null) { final JDialog sdd = sd; SwingUtilities.invokeAndWait(() -> { List<JCheckBox> cs = new ArrayList<>(); PcProbeGui.collect(sdd.getContentPane(), JCheckBox.class, cs); for (JCheckBox c : cs) if (c.getText().startsWith("Show each fight live")) settingsBox[0] = c; }); }
        SwingUtilities.invokeAndWait(() -> noAuto[0].doClick());
        Thread.sleep(500);
        check("F4 ticking it turns the live window off (" + Settings.fightView + ")", !Settings.fightView);
        check("F4b ...and the Settings box follows (found " + (settingsBox[0] != null) + ", ticked " + (settingsBox[0] != null && settingsBox[0].isSelected()) + ")", settingsBox[0] == null || !settingsBox[0].isSelected());
        SwingUtilities.invokeAndWait(() -> noAuto[0].doClick());
        Thread.sleep(300);
        check("F4c unticking it turns the live window on again and the Settings box follows", Settings.fightView && (settingsBox[0] == null || settingsBox[0].isSelected()));
        SwingUtilities.invokeAndWait(() -> { for (Window w2 : Window.getWindows()) if (w2 instanceof JDialog && w2.isShowing()) { String tt = ((JDialog) w2).getTitle(); if (tt != null && (tt.contains("Fight  ·") || tt.startsWith("Settings"))) w2.dispose(); } });

        // ---- S: the mouse wheel
        int unit = Ui.scrollUnit();
        final boolean[] fast = {true};
        SwingUtilities.invokeAndWait(() -> { for (Window w2 : Window.getWindows()) walk(w2, unit, fast); });
        check("S1 every scroll pane that is open has the wheel step of the setting (" + unit + " px per unit; default setting " + Settings.scrollSpeed + ")", fast[0] && unit >= 16);
        PcProbeGui.real.println("[G] DONE");
    }

    static void walk(Component c, int unit, boolean[] ok) {
        if (c instanceof JScrollPane && ((JScrollPane) c).isShowing()) { JScrollPane sp = (JScrollPane) c; if (sp.getVerticalScrollBar() != null && sp.getVerticalScrollBar().getUnitIncrement() < unit) ok[0] = false; }
        if (c instanceof Container) for (Component k : ((Container) c).getComponents()) walk(k, unit, ok);
    }
}
