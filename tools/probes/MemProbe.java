package Game;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.*;

/** Long spectator run of the real 100,000 world that measures memory: live heap after a full GC every N game years, the size of Map.characters,
 *  how many Character objects are reachable from the living through family links (dead ancestors), and class histograms at chosen years.
 *  java -Xmx16g -Xss512m -cp out Game.MemProbe <lastYear> <everyYears> <histogram years, comma separated>
 *  -Dmem.input="n\n1\nauto\n" continues the newest save of the run folder (the viewer question, the save to load, the AUTO command); the default starts a new world and watches it. */
public class MemProbe {
    static class Feed extends InputStream {
        private final byte[] data = System.getProperty("mem.input", "n\n100000\nw\n").replace("\\n", "\n").getBytes();
        private int pos = 0;
        @Override public int read() throws IOException { byte[] b = new byte[1]; int n = read(b, 0, 1); return n < 0 ? -1 : (b[0] & 0xff); }
        @Override public int read(byte[] b, int off, int len) throws IOException {
            if (pos >= data.length) { try { Thread.sleep(Long.MAX_VALUE); } catch (InterruptedException e) { return -1; } }
            int n = Math.min(len, data.length - pos);
            System.arraycopy(data, pos, b, off, n);
            pos += n;
            return n;
        }
    }
    static volatile Throwable gameError = null;

    static String histogram(int top) {
        try {
            String s = (String) ManagementFactory.getPlatformMBeanServer().invoke(new javax.management.ObjectName("com.sun.management:type=DiagnosticCommand"),
                    "gcClassHistogram", new Object[]{null}, new String[]{String[].class.getName()});
            String[] lines = s.split("\n");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(lines.length, top + 3); i++) sb.append(lines[i]).append('\n');
            return sb.toString();
        } catch (Throwable e) { return "histogram failed: " + e; }
    }

    static long deathTotal() { try { long n = 0; synchronized (Chronicle.deathCauses) { for (int v : Chronicle.deathCauses.values()) n += v; } return n; } catch (Throwable e) { return -1; } }

    public static void main(String[] a) throws Exception {
        final int lastYear = a.length > 0 ? Integer.parseInt(a[0]) : 100;
        final int every = a.length > 1 ? Integer.parseInt(a[1]) : 5;
        Set<Integer> histYears = new HashSet<>();
        if (a.length > 2) for (String s : a[2].split(",")) if (!s.isEmpty()) histYears.add(Integer.parseInt(s.trim()));
        PrintStream real = System.out;
        System.setIn(new Feed());
        final PrintStream log = new PrintStream(new FileOutputStream("game_out.txt"), true);
        System.setOut(log);
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { gameError = e; e.printStackTrace(real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        PrintWriter csv = new PrintWriter(new FileWriter("mem.csv", true), true);
        csv.println("year,wallSec,tickMs,pop,listSize,nonNull,cultivators,beasts,tamed,reachableDead,liveHeapMB,committedMB,saveMB,deaths");
        long t0 = System.currentTimeMillis();
        int lastLogged = -1;
        long lastWall = t0; int lastTime = 0;
        while (true) {
            Thread.sleep(500);
            if (gameError != null) { real.println("GAME ERROR " + gameError); break; }
            int yr = GameDriver.time / 52;
            if (yr > lastYear) break;
            if (yr == lastLogged || yr % every != 0) continue;
            lastLogged = yr;
            long now = System.currentTimeMillis();
            long tickMs = GameDriver.time > lastTime ? (now - lastWall) / (GameDriver.time - lastTime) : 0;
            lastWall = now; lastTime = GameDriver.time;
            int listSize = Map.characters.size(), nonNull = 0, cult = 0, beasts = 0, tamed = 0;
            int reachableDead = -1;
            try {
                ArrayList<Character> snap = new ArrayList<>(Map.characters);
                IdentityHashMap<Character, Boolean> seen = new IdentityHashMap<>();
                ArrayDeque<Character> q = new ArrayDeque<>();
                for (Character c : snap) {
                    if (c == null) continue;
                    nonNull++;
                    if (c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) { beasts++; if (c.wasTamed) tamed++; } else cult++;
                    seen.put(c, Boolean.TRUE); q.add(c);
                }
                int dead = 0;
                while (!q.isEmpty()) {
                    Character c = q.poll();
                    ArrayList<Character> next = new ArrayList<>();
                    if (c.father != null) next.add(c.father);
                    if (c.mother != null) next.add(c.mother);
                    if (c.spouse != null) next.add(c.spouse);
                    if (c.reincarnationKiller != null) next.add(c.reincarnationKiller);
                    if (c.siblings != null) next.addAll(c.siblings);
                    if (c.children != null) next.addAll(c.children);
                    for (Character n : next) if (n != null && seen.put(n, Boolean.TRUE) == null) { dead++; q.add(n); }
                }
                reachableDead = dead;
            } catch (Throwable e) { reachableDead = -2; }
            System.gc();
            Runtime rt = Runtime.getRuntime();
            long live = (rt.totalMemory() - rt.freeMemory()) >> 20, committed = rt.totalMemory() >> 20;
            long saveMB = -1;
            try { File[] fs = new File("saves").listFiles((d, n) -> n.endsWith(".dat")); long mx = 0; if (fs != null) for (File f : fs) mx = Math.max(mx, f.length()); saveMB = mx >> 20; } catch (Throwable e) { }
            csv.println(yr + "," + (now - t0) / 1000 + "," + tickMs + "," + Map.population + "," + listSize + "," + nonNull + "," + cult + "," + beasts + "," + tamed + "," + reachableDead + "," + live + "," + committed + "," + saveMB + "," + deathTotal());
            if (histYears.contains(yr)) {
                try (PrintWriter h = new PrintWriter(new FileWriter("hist_year" + yr + ".txt"))) { h.print(histogram(45)); }
            }
        }
        try { csv.println("causes " + new TreeMap<>(Chronicle.deathCauses)); } catch (Throwable e) { }
        try { csv.println("defense " + Defense.counts()); } catch (Throwable e) { }
        csv.println("done");
        csv.close();
        System.exit(0);
    }
}
