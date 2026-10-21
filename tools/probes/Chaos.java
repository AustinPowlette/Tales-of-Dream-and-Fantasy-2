package Game;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.management.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Harsher race/deadlock test: 0 ms ticks, every window open, AND a chaos thread firing saves, loads, settings flips, notices, exports,
 *  window open/close and text-size changes at random while the EDT is being hammered. Stderr (uncaught Swing exceptions) is scanned too. */
public class Chaos {
    static int passes = 0, fails = 0;
    static PrintStream real;
    static void check(String n, boolean ok) { if (ok) passes++; else fails++; real.println("[C] " + (ok ? "PASS " : "FAIL ") + n); }
    static final AtomicInteger saves = new AtomicInteger(), loads = new AtomicInteger(), flips = new AtomicInteger(), exports = new AtomicInteger(), notes = new AtomicInteger(), windows = new AtomicInteger();
    static final List<String> edtErrors = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "false");
        real = System.out;
        PrintStream err = new PrintStream(new OutputStream() {
            StringBuilder sb = new StringBuilder();
            @Override public void write(int b) { if (b == '\n') { String s = sb.toString(); sb.setLength(0); if (s.contains("Exception") || s.contains("Error")) edtErrors.add(s); real.println("[stderr] " + s); } else sb.append((char) b); }
        }, true);
        System.setErr(err);
        PcProbe.real = real; PcProbeGui.real = real;
        System.setIn(PcProbe.in);
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        Thread w = new Thread(PcProbeGui::watcher, "dialog-watcher"); w.setDaemon(true); w.start();
        try { run(Integer.parseInt(a.length > 0 ? a[0] : "120")); } catch (Throwable e) { real.println("[C] PROBE EXCEPTION " + e); e.printStackTrace(real); fails++; }
        real.println("[C] ===== " + passes + " passed, " + fails + " failed; gameError=" + PcProbe.gameError + " =====");
        String o = PcProbe.out();
        int ex = 0; for (String l : o.split("\n")) if (l.contains("Exception") || l.contains("\tat Game.")) { ex++; if (ex < 10) real.println("[C] exline: " + l); }
        real.println("[C] exceptionLines(stdout)=" + ex + " edtErrors(stderr)=" + edtErrors.size());
        System.exit(0);
    }

    static void run(int seconds) throws Exception {
        PcProbe.in.feed("y\ns\nn\n");
        long end = System.currentTimeMillis() + 240000;
        while (System.currentTimeMillis() < end && !(GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbe.pc() != null)) Thread.sleep(200);
        Character p = PcProbe.pc();
        check("started", p != null);
        if (p == null) return;
        MapViewer mv = GameDriver.mapViewer;
        p.lvl = 100; p.majorLvl = 10; p.minorLvl = 0; p.maxAge = 100000; p.caution = 0;
        DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        DecisionBroker.scripted = new ArrayDeque<>();
        Settings.autosaveYears = 1; Settings.firstRunDone = true;
        QolGui.closeAll("Tutorial");
        String[] wins = {"Quests+", "Duties+", "Manage", "Rankings", "Market", "Browse", "Family", "Saves", "Settings"};
        for (String b : wins) { PcProbeGui.click(b); Thread.sleep(250); }
        SaveManager.save("chaos0", SaveManager.Kind.MANUAL); SaveManager.save("chaos1", SaveManager.Kind.MANUAL);
        GameDriver.paused = false; GameDriver.tickDelayMs = 0;
        int time0 = GameDriver.time;
        final boolean[] stop = {false};

        // EDT latency probe
        final long[] maxLat = {0}; final int[] n = {0}; final long[] lats = new long[400000];
        Thread lat = new Thread(() -> {
            while (!stop[0]) {
                final long s = System.nanoTime();
                try { SwingUtilities.invokeAndWait(() -> { }); } catch (Exception e) { return; }
                long d = (System.nanoTime() - s) / 1_000_000;
                synchronized (lats) { if (n[0] < lats.length) lats[n[0]++] = d; }
                maxLat[0] = Math.max(maxLat[0], d);
                try { Thread.sleep(30); } catch (InterruptedException e) { return; }
            }
        }, "edt-latency"); lat.setDaemon(true); lat.start();

        // chaos thread
        Random rnd = new Random(Long.getLong("seed", 11));
        Thread chaos = new Thread(() -> {
            while (!stop[0]) {
                try {
                    int k = rnd.nextInt(12);
                    switch (k) {
                        case 0: GameDriver.saveRequestName = "chaos" + rnd.nextInt(3); saves.incrementAndGet(); break;
                        case 1: GameDriver.saveRequested = true; saves.incrementAndGet(); break;
                        case 2: if (SaveManager.find("chaos0") != null && GameDriver.loadRequest == null) { GameDriver.loadRequest = "chaos" + rnd.nextInt(2); loads.incrementAndGet(); } break;
                        case 3: Settings.colorblindMap = !Settings.colorblindMap; SwingUtilities.invokeLater(mv::applySettings); flips.incrementAndGet(); break;
                        case 4: Settings.pictures = !Settings.pictures; SwingUtilities.invokeLater(mv::applySettings); flips.incrementAndGet(); break;
                        case 5: Notifications.post("death", "chaos notice " + notes.incrementAndGet(), -1, -1); break;
                        case 6: SwingUtilities.invokeLater(() -> mv.exportMap(3)); exports.incrementAndGet(); break;
                        case 7: { String b = wins[rnd.nextInt(wins.length)]; PcProbeGui.click(b); windows.incrementAndGet(); break; }
                        case 8: QolGui.closeAll(rnd.nextBoolean() ? "Family" : "Saves"); break;
                        case 9: if (rnd.nextInt(6) == 0) { Settings.fontScale = 1.0 + rnd.nextInt(4) * 0.15; SwingUtilities.invokeLater(Ui::fontScaleChanged); } break;
                        case 10: GameDriver.paused = true; Thread.sleep(150); GameDriver.paused = false; break;
                        default: GameDriver.tickDelayMs = rnd.nextBoolean() ? 0 : 20; break;
                    }
                    Thread.sleep(40 + rnd.nextInt(160));
                } catch (Throwable e) { real.println("[C] chaos thread: " + e); }
            }
        }, "chaos"); chaos.setDaemon(true); chaos.start();

        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        long stopAt = System.currentTimeMillis() + seconds * 1000L;
        int lastTime = GameDriver.time, autoSent = 0, resets = 0; long lastProgress = System.currentTimeMillis(), longestStall = 0;
        boolean deadlock = false;
        while (System.currentTimeMillis() < stopAt) {
            if (GameDriver.waitingForInput && GameDriver.guiInputQueue.isEmpty()) { GameDriver.guiInputQueue.add("auto 500"); autoSent++; }
            PcProbeGui.onEdt(() -> {
                for (Window win : Window.getWindows()) {
                    if (!win.isShowing() || win == mv || !(win instanceof JDialog)) continue;
                    Container cp = ((JDialog) win).getContentPane();
                    List<JTabbedPane> tp = new ArrayList<>(); PcProbeGui.collect(cp, JTabbedPane.class, tp);
                    for (JTabbedPane tabs : tp) if (tabs.getTabCount() > 1 && rnd.nextInt(3) == 0) tabs.setSelectedIndex(rnd.nextInt(tabs.getTabCount()));
                }
                mv.refreshNow();
            });
            int tm = GameDriver.time;
            if (tm != lastTime) { if (tm < lastTime) resets++; lastTime = tm; lastProgress = System.currentTimeMillis(); }
            else longestStall = Math.max(longestStall, System.currentTimeMillis() - lastProgress);
            long[] dead = mx.findDeadlockedThreads();
            if (dead != null) { deadlock = true; for (ThreadInfo ti : mx.getThreadInfo(dead, 30)) real.println("[C] " + ti); break; }
            if (System.currentTimeMillis() - lastProgress > 15000) { // stall: dump every thread once
                real.println("[C] STALL > 15 s, thread dump:");
                for (ThreadInfo ti : mx.dumpAllThreads(true, true)) if (ti.getThreadName().matches("game|AWT-EventQueue.*|save|load|chaos|main")) real.println("[C] " + ti);
                lastProgress = System.currentTimeMillis();
            }
            Thread.sleep(50);
        }
        stop[0] = true;
        Thread.sleep(800);
        int ticks = GameDriver.time - time0;
        long[] copy; synchronized (lats) { copy = Arrays.copyOf(lats, n[0]); }
        Arrays.sort(copy);
        long p50 = copy.length == 0 ? -1 : copy[copy.length / 2], p99 = copy.length == 0 ? -1 : copy[(int) (copy.length * 0.99)];
        real.println("[C] info: net ticks=" + ticks + " (time went backwards " + resets + "x = loads applied), saves requested=" + saves + " loads requested=" + loads + " setting flips=" + flips + " exports=" + exports + " notices=" + notes + " window clicks=" + windows
                + "; EDT p50=" + p50 + "ms p99=" + p99 + "ms max=" + maxLat[0] + "ms (" + copy.length + " samples); longest tick stall=" + longestStall + "ms");
        String out = PcProbe.out();
        int loaded = out.split("\\[LOAD\\] Loaded", -1).length - 1, saved = out.split("\\[SAVE\\] Saved", -1).length - 1;
        real.println("[C] info: saves completed=" + saved + " loads completed=" + loaded);
        check("no deadlock", !deadlock && mx.findDeadlockedThreads() == null);
        check("the game kept moving (longest stall " + longestStall + " ms)", longestStall < 15000 && (GameDriver.time != time0 || resets > 0));
        check("saves and loads actually happened under load (" + saved + " saves, " + loaded + " loads)", saved >= 10 && loaded >= 3);
        check("the Swing thread stayed responsive (p99 " + p99 + " ms, max " + maxLat[0] + " ms)", p99 >= 0 && p99 < 1500 && maxLat[0] < 8000);
        check("no exceptions on the Swing thread or anywhere (stderr: " + edtErrors.size() + ")", edtErrors.isEmpty());
        check("no crash log", !new File("crash.log").exists());
        check("ids intact", PcProbe.idMismatch() == 0);
        check("a player still exists", PcProbe.pc() != null);
        GameDriver.tickDelayMs = 100;
    }
}
