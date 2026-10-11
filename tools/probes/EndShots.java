package Game;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

/** Screenshots of an ending scene and its scrolling credits (-Dshots=<dir>). */
public class EndShots {
    static PrintStream real;
    static void onEdt(Runnable r) throws Exception { SwingUtilities.invokeAndWait(r); }

    static void shot(String name, Component c) throws Exception {
        final BufferedImage[] im = {null};
        onEdt(() -> { int w = Math.max(1, c.getWidth()), h = Math.max(1, c.getHeight()); im[0] = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB); Graphics2D g = im[0].createGraphics(); c.paint(g); g.dispose(); });
        File dir = new File(System.getProperty("shots", "shots")); dir.mkdirs();
        javax.imageio.ImageIO.write(im[0], "png", new File(dir, name + ".png"));
        real.println("SHOT " + name + " " + im[0].getWidth() + "x" + im[0].getHeight());
    }

    static Window win(String prefix) { return QolGui.win(prefix); }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "false");
        real = System.out;
        PcProbe.real = real; PcProbeGui.real = real;
        System.setIn(PcProbe.in);
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        Thread w = new Thread(PcProbeGui::watcher, "dialog-watcher"); w.setDaemon(true); w.start();
        try { run(); } catch (Throwable e) { real.println("PROBE EXCEPTION " + e); e.printStackTrace(real); }
        System.exit(0);
    }

    static void run() throws Exception {
        Settings.lore = false;
        PcProbe.in.feed("y\ns\nn\n");
        long end = System.currentTimeMillis() + 240000;
        while (System.currentTimeMillis() < end && !(GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbe.pc() != null)) Thread.sleep(200);
        Character p = PcProbe.pc();
        MapViewer mv = GameDriver.mapViewer;
        PcProbeGui.watch = false; // the dialog watcher would close the scene after half a second
        Settings.lore = true; Settings.lorePause = false;
        Lore.PState ps = new Lore.PState();
        List<String> credits = LoreEndings.credits(p, ps, "The True Ending: a soldier of the Outside", 7421, Arrays.asList("TALES OF DREAM AND FANTASY 3: THE OUTSIDE", p.name + " and ??? continue the war. Coming soon.", "(The Gardener has said soon for ten thousand years. Do not hold your breath. Do hold your spear.)"));
        List<String> lines = LoreText.lines("end_soldier", Lore.vars(p, ps));
        GuiLore.ending("The True Ending: a soldier of the Outside", lines, RunStats.text(), credits);
        Thread.sleep(2500);
        Window sc = null;
        for (Window x : Window.getWindows()) if (x instanceof JDialog && x.isShowing() && ((JDialog) x).getTitle().startsWith("???")) sc = x;
        if (sc != null) shot("end_scene", ((JDialog) sc).getRootPane());
        final Window scene = sc;
        if (scene != null) onEdt(() -> { for (JButton b : QolGui.all(((JDialog) scene).getContentPane(), JButton.class)) if (b.getText().startsWith("Credits")) b.doClick(); });
        Thread.sleep(9000);
        Window cw = win("Credits");
        if (cw != null) shot("end_credits", ((JDialog) cw).getRootPane());
        real.println("DONE");
    }
}
