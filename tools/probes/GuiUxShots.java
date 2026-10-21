package Game;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

/** Renders the new dock tabs and windows to PNG files (System property "shots" = the output folder) for a visual check. */
public class GuiUxShots {
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

    static Component content(Window w) { return w instanceof JDialog ? ((JDialog) w).getRootPane() : ((JFrame) w).getRootPane(); }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "false");
        real = System.out;
        PcProbe.real = real; PcProbeGui.real = real;
        System.setIn(PcProbe.in);
        GameDriver.frozenNpcs = true; // frozen before the first tick: the NPCs act before the human prompt of a tick, and one of them could otherwise ask the character a question before the probe is ready
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        Thread w = new Thread(PcProbeGui::watcher, "dialog-watcher"); w.setDaemon(true); w.start();
        try { run(); } catch (Throwable e) { real.println("PROBE EXCEPTION " + e); e.printStackTrace(real); }
        System.exit(0);
    }

    static void run() throws Exception {
        PcProbe.in.feed("y\ns\nn\n");
        long end = System.currentTimeMillis() + 240000;
        while (System.currentTimeMillis() < end && !(GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbe.pc() != null)) Thread.sleep(200);
        Character p = PcProbe.pc();
        MapViewer mv = GameDriver.mapViewer;
        p.lvl = 40; p.majorLvl = 4; p.maxAge = 100000; p.caution = 0;
        DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        GameDriver.frozenNpcs = true;
        onEdt(() -> { mv.setSize(1700, 1000); mv.setLocation(10, 10); mv.validate(); });
        Thread.sleep(1500);
        PcProbeGui.click("Wait"); PcProbeGui.settle(); Thread.sleep(800);
        // a waypoint quest and some company so the windows have something to show
        PcProbe.cmd("qnew home " + p.location.x + " " + p.location.y + " 80");
        PcProbe.cmd("qnew far " + ((p.location.x + 3) % Map.range) + " " + p.location.y + " 60");
        Thread.sleep(800);

        final JTabbedPane[] tp = {null};
        onEdt(() -> { for (JTabbedPane x : QolGui.all(mv.getContentPane(), JTabbedPane.class)) if (x.indexOfTab("World") >= 0) tp[0] = x; });
        for (String tab : new String[]{"Move", "Me", "Auto", "World"}) {
            onEdt(() -> tp[0].setSelectedIndex(tp[0].indexOfTab(tab)));
            Thread.sleep(500);
            shot("tab_" + tab, tp[0]);
        }
        onEdt(() -> tp[0].setSelectedIndex(0));
        shot("main", mv.getRootPane());

        String[][] opens = {{"Quest Log", "Quest Log"}, {"Journal", "Journal"}, {"Here", "Here"}, {"People here", "People here"}, {"Keys", "Keyboard"}, {"Settings", "Settings"}};
        for (String[] o : opens) {
            PcProbeGui.click(o[0]); Thread.sleep(1500);
            Window wd = win(o[1]);
            if (wd == null) { real.println("NO WINDOW " + o[0]); continue; }
            shot("win_" + o[0].replace(' ', '_'), content(wd));
            QolGui.closeAll(o[1]);
        }
        PcProbeGui.click("Auto"); Thread.sleep(1200);
        onEdt(() -> tp[0].setSelectedIndex(tp[0].indexOfTab("Auto")));
        Thread.sleep(600);
        shot("tab_Auto_running", tp[0]);
        PcProbeGui.click("Take control back"); PcProbeGui.settle();
        // duties window
        onEdt(() -> GuiDuties.open(mv, "Duties")); Thread.sleep(1200);
        shot("win_Duties", content(win("Control")));
        onEdt(() -> GuiDuties.open(mv, "Policies")); Thread.sleep(800);
        shot("win_Policies", content(win("Control")));
        QolGui.closeAll("Control");
        // preview card, a generic sheet
        PcProbe.cmd("time"); Thread.sleep(1200);
        if (win("Time") != null) shot("win_Time", content(win("Time")));
        PcProbe.cmd("help"); Thread.sleep(1200);
        if (win("Commands") != null) shot("win_Commands", content(win("Commands")));
        real.println("DONE");
    }
}
