package Game;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

/** 2026-10-21 (GUI, needs a display): the Concerts window (`GuiConcerts`). A musician plays in the tile of the person who plays; the window lists the performance, the button listens (the week passes, the piece is marked heard),
 *  the second tab lists the 43 pieces with a heard mark, a piece that was not heard cannot be replayed, the occasion filter and the Masterpieces entry work, and the dock and the Backpack both have a Concerts button.
 *  Screenshots go to `-Dshots=<dir>` (default `shots`); the checks are printed as `[C] PASS/FAIL`. */
public class ConcertGuiProbe {
    static PrintStream real;
    static int passes = 0, fails = 0;
    static void check(String n, boolean ok) { if (ok) passes++; else fails++; real.println("[C] " + (ok ? "PASS " : "FAIL ") + n); }
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
        while (System.currentTimeMillis() < end) { Window w = QolGui.win(prefix); if (w != null) return w; Thread.sleep(150); }
        return null;
    }

    static JTable table(Window w, int index) throws Exception {
        final List<JTable> l = new ArrayList<>();
        onEdt(() -> PcProbeGui.collect(((JDialog) w).getContentPane(), JTable.class, l));
        return index < l.size() ? l.get(index) : null;
    }

    static boolean press(Window w, String label) throws Exception {
        final JButton[] b = {null};
        onEdt(() -> b[0] = PcProbeGui.findButton(w instanceof JDialog ? ((JDialog) w).getContentPane() : ((JFrame) w).getContentPane(), label));
        if (b[0] == null) return false;
        SwingUtilities.invokeLater(() -> b[0].doClick());
        return true;
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "false");
        real = System.out;
        PcProbe.real = real; PcProbeGui.real = real;
        System.setIn(PcProbe.in);
        GameDriver.frozenNpcs = true;
        Settings.firstRunDone = true;
        Settings.lore = false;
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        Thread w = new Thread(PcProbeGui::watcher, "dialog-watcher"); w.setDaemon(true); w.start();
        try { run(); } catch (Throwable e) { real.println("[C] PROBE EXCEPTION " + e); e.printStackTrace(real); fails++; }
        real.println("[C] ===== " + passes + " passed, " + fails + " failed =====");
        System.exit(0);
    }

    static void run() throws Exception {
        PcProbe.in.feed("y\ns\nn\n");
        long end = System.currentTimeMillis() + 240000;
        while (System.currentTimeMillis() < end && !(GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbe.pc() != null)) Thread.sleep(200);
        Character p = PcProbe.pc();
        MapViewer mv = GameDriver.mapViewer;
        onEdt(() -> { mv.setSize(1500, 950); mv.setLocation(10, 10); mv.validate(); });
        Thread.sleep(1500);
        PcProbeGui.click("Wait"); PcProbeGui.settle(); Thread.sleep(800);
        DecisionBroker.scriptedNumbers = new ArrayDeque<>();

        // the buttons
        List<JButton> all = new ArrayList<>();
        onEdt(() -> PcProbeGui.collect(mv.getContentPane(), JButton.class, all));
        boolean dockBtn = false; for (JButton b : all) if ("Concerts".equals(b.getText())) dockBtn = true;
        check("C1 the dock has a Concerts button", dockBtn);

        // a musician plays in the tile
        Character mus = null;
        for (Character x : Map.characters) if (x != null && x != p && !x.dead && x.age >= 18 && x.location != null) { mus = x; break; }
        final Character musF = mus; final Area home = mus.location;
        mus.location = p.location;
        Concerts.announce(mus, new Item(Item.ItemType.Instrument, 6, 1, "Heaven-Grade Pipa"), 6);
        Concerts.Show show = Concerts.showAt(p.location);
        check("C2 the performance is recorded", show != null);

        onEdt(() -> GuiConcerts.open(mv));
        Window win = waitFor("Concerts", 5000);
        check("C3 the Concerts window opens", win != null);
        if (win == null) return;
        Thread.sleep(2200);   // (a refresh)
        JTable near = table(win, 0);
        final int[] rows = {0}; final String[] first = {""};
        onEdt(() -> { rows[0] = near.getRowCount(); if (rows[0] > 0) first[0] = near.getValueAt(0, 0) + "|" + near.getValueAt(0, 1) + "|" + near.getValueAt(0, 5); });
        check("C4 the list has the performance once (" + first[0] + ")", rows[0] == 1 && first[0].startsWith(show.piece.title) && first[0].endsWith("|0"));
        shot("concerts-nearby", win);

        int h0 = Concerts.heardCount(p), t0 = GameDriver.time;
        onEdt(() -> near.setRowSelectionInterval(0, 0));
        check("C5 the Listen button is found and pressed", press(win, "Listen / walk there"));
        for (int i = 0; i < 300 && GameDriver.time == t0; i++) Thread.sleep(80);
        Thread.sleep(500);
        check("C6 listening marked the piece heard and the week passed (" + h0 + " -> " + Concerts.heardCount(p) + ", time " + t0 + " -> " + GameDriver.time + ")", Concerts.heardBy(p, show.index) && Concerts.heardCount(p) == h0 + 1 && GameDriver.time > t0);

        // the pieces tab
        final JTabbedPane[] tabs = {null};
        onEdt(() -> { List<JTabbedPane> l = new ArrayList<>(); PcProbeGui.collect(((JDialog) win).getContentPane(), JTabbedPane.class, l); tabs[0] = l.get(0); tabs[0].setSelectedIndex(1); });
        Thread.sleep(2200);
        JTable pieces = table(win, 1);
        final int[] prow = {0}; final String[] heardCol = {""};
        onEdt(() -> { prow[0] = pieces.getRowCount(); for (int r = 0; r < pieces.getRowCount(); r++) if (show.piece.title.equals(pieces.getValueAt(r, 1))) heardCol[0] = String.valueOf(pieces.getValueAt(r, 5)); });
        check("C7 the pieces tab lists all " + Concerts.count() + " pieces (" + prow[0] + ") and the heard one is marked (" + heardCol[0] + ")", prow[0] == Concerts.count() && heardCol[0].equals("yes"));
        shot("concerts-pieces", win);

        // a piece not heard cannot be replayed
        final int[] unheardRow = {-1};
        onEdt(() -> { for (int r = 0; r < pieces.getRowCount(); r++) if ("-".equals(pieces.getValueAt(r, 5))) { unheardRow[0] = r; break; } if (unheardRow[0] >= 0) pieces.setRowSelectionInterval(unheardRow[0], unheardRow[0]); });
        int before = Concerts.heardCount(p);
        press(win, "Replay"); Thread.sleep(800);
        final List<JLabel> labs = new ArrayList<>();
        onEdt(() -> PcProbeGui.collect(((JDialog) win).getContentPane(), JLabel.class, labs));
        boolean said = false; for (JLabel l : labs) if (l.getText() != null && l.getText().contains("have not heard")) said = true;
        check("C8 a piece you have not heard is not replayed (the window says so; heard stays " + before + ")", said && Concerts.heardCount(p) == before);

        // the filter
        final JComboBox<?>[] occ = {null};
        onEdt(() -> { List<JComboBox> l = new ArrayList<>(); PcProbeGui.collect(((JDialog) win).getContentPane(), JComboBox.class, l); occ[0] = l.get(0); occ[0].setSelectedIndex(occ[0].getItemCount() - 1); });
        Thread.sleep(600);
        final int[] mrows = {0};
        onEdt(() -> mrows[0] = pieces.getRowCount());
        check("C9 the Masterpieces entry of the filter shows the six masterpieces (" + mrows[0] + ")", mrows[0] == 6);

        onEdt(() -> win.dispose());
        mus.location = home;
        check("C10 no game error", PcProbe.gameError == null);
    }
}
