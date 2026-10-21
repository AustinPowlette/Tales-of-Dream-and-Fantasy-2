package Game;

import java.io.*;
import java.util.*;

/** Balance measurement tool. Spectates a world for N game years and writes per-year statistics to CSV plus a realm-progression / lifespan
 *  summary. Run it in an EMPTY directory (a leftover saves/ folder adds a start-up menu) against a world you have shrunk for speed:
 *
 *    (copy src/Game/*.java somewhere, set Map.startingPopulation to 3000)
 *    javac -encoding UTF-8 -cp src -d out src/Game/*.java tools/BalProbe.java
 *    mkdir run && cd run && java -Xss512m -cp ../out Game.BalProbe 250
 *
 *  Output: bal_years.csv (one row per game year), bal_summary.txt (age each realm was first reached, age at death, deaths per realm).
 *  See "Balance Plan.md" for how the numbers are read. */
public class BalProbe {
    static class Snap { int age, maxAge, major, lvl; boolean beast; long wealth; int kills; }

    /** stdin for the game: "n" (no GUI) and "w" (spectate), then silence. */
    static class Feed extends InputStream {
        private final byte[] data = "n\nw\n".getBytes();
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

    public static void main(String[] a) throws Exception {
        final int years = a.length > 0 ? Integer.parseInt(a[0]) : 120;
        PrintStream real = System.out;
        System.setIn(new Feed());
        System.setOut(new PrintStream(new ByteArrayOutputStream(), true)); // the game's chatter is not needed (and would grow without bound)
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { gameError = e; e.printStackTrace(real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        Thread.sleep(3000);
        PrintWriter csv = new PrintWriter(new FileWriter("bal_years.csv"));
        csv.println("year,wallSec,pop,humansAlive,beasts,births,deathsOld,deathsOther,avgAge,avgMaxAge,maxLvl,m0,m1,m2,m3,m4,m5,m6,m7,m8,m9,m10,cities,sects,shops,dungeons,totalWealthSS,top1pctShare,medianWealth,kills");
        java.util.Map<Integer, Snap> prev = new HashMap<>();
        List<List<Integer>> firstReach = new ArrayList<>();
        for (int i = 0; i <= 10; i++) firstReach.add(new ArrayList<>());
        java.util.Map<Integer, Integer> bestMajor = new HashMap<>();
        List<Integer> deathAges = new ArrayList<>(), deathAgesOld = new ArrayList<>();
        java.util.Map<Integer, int[]> deathByMajor = new HashMap<>(); // realm -> {old age, other}
        long t0 = System.currentTimeMillis();
        int lastYear = -1;
        while (true) {
            int yr = GameDriver.time / 52;
            if (gameError != null) { real.println("GAME ERROR " + gameError); break; }
            if (yr == lastYear) { Thread.sleep(200); continue; }
            lastYear = yr;
            if (yr > years) break;
            java.util.Map<Integer, Snap> cur = new HashMap<>();
            int n = Map.characters.size();
            int beasts = 0, humans = 0; long ageSum = 0, maxAgeSum = 0; int maxLvl = 0; int[] major = new int[11]; long kills = 0;
            List<Long> wealth = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                Character c = Map.characters.get(i);
                if (c == null || c.dead || !c.born) continue;
                Snap s = new Snap();
                s.age = c.age; s.maxAge = c.maxAge; s.major = c.majorLvl; s.lvl = c.lvl; s.beast = c.trait == Character.Traits.Beast; s.wealth = PlayerLists.wealth(c); s.kills = c.kills;
                cur.put(i, s);
                if (s.beast) { beasts++; continue; }
                humans++; ageSum += c.age; maxAgeSum += c.maxAge; maxLvl = Math.max(maxLvl, c.lvl); major[Math.max(0, Math.min(10, c.majorLvl))]++; kills += c.kills; wealth.add(s.wealth);
                Integer bm = bestMajor.get(i);
                if (bm == null) bestMajor.put(i, c.majorLvl);
                else if (c.majorLvl > bm) { for (int k = bm + 1; k <= Math.min(10, c.majorLvl); k++) firstReach.get(k).add(c.age); bestMajor.put(i, c.majorLvl); }
            }
            int births = 0, dOld = 0, dOther = 0;
            for (java.util.Map.Entry<Integer, Snap> e : prev.entrySet()) if (!cur.containsKey(e.getKey()) && !e.getValue().beast) {
                Snap s = e.getValue();
                boolean old = s.age >= s.maxAge - 2;
                if (old) { dOld++; deathAgesOld.add(s.age); } else dOther++;
                deathAges.add(s.age);
                deathByMajor.computeIfAbsent(s.major, k -> new int[2])[old ? 0 : 1]++;
            }
            for (java.util.Map.Entry<Integer, Snap> e : cur.entrySet()) if (!prev.containsKey(e.getKey()) && !e.getValue().beast) births++;
            int cities = 0, sects = 0, shops = 0, dungeons = 0;
            for (Area ar : new ArrayList<>(Map.createdAreas)) { if (ar.city) cities++; if (ar.sect != null) sects++; if (ar.dungeonType != null) dungeons++; try { shops += ar.shops.size(); } catch (Exception ex) { } }
            Collections.sort(wealth);
            long total = 0; for (long w : wealth) total += w;
            long top = 0; int k1 = Math.max(1, wealth.size() / 100); for (int i = wealth.size() - k1; i < wealth.size(); i++) if (i >= 0) top += wealth.get(i);
            long median = wealth.isEmpty() ? 0 : wealth.get(wealth.size() / 2);
            csv.printf("%d,%d,%d,%d,%d,%d,%d,%d,%.1f,%.1f,%d,%s,%d,%d,%d,%d,%d,%.3f,%d,%d%n", yr, (System.currentTimeMillis() - t0) / 1000, Map.population, humans, beasts, births, dOld, dOther,
                    humans == 0 ? 0 : ageSum / (double) humans, humans == 0 ? 0 : maxAgeSum / (double) humans, maxLvl, joinInts(major), cities, sects, shops, dungeons, total, total == 0 ? 0 : top / (double) total, median, kills);
            csv.flush();
            prev = cur;
            if (yr % 10 == 0) summary(firstReach, deathAges, deathAgesOld, deathByMajor, yr);
        }
        summary(firstReach, deathAges, deathAgesOld, deathByMajor, lastYear);
        csv.close();
        System.exit(0);
    }

    static String joinInts(int[] a) { StringBuilder sb = new StringBuilder(); for (int i = 0; i < a.length; i++) { if (i > 0) sb.append(','); sb.append(a[i]); } return sb.toString(); }

    static String stats(List<Integer> l) {
        if (l.isEmpty()) return "n=0";
        List<Integer> c = new ArrayList<>(l); Collections.sort(c);
        return "n=" + c.size() + " min=" + c.get(0) + " p10=" + c.get(c.size() / 10) + " median=" + c.get(c.size() / 2) + " p90=" + c.get(c.size() * 9 / 10) + " max=" + c.get(c.size() - 1);
    }

    static void summary(List<List<Integer>> reach, List<Integer> deaths, List<Integer> oldAge, java.util.Map<Integer, int[]> byMajor, int yr) {
        try (PrintWriter w = new PrintWriter("bal_summary.txt")) {
            w.println("after game year " + yr);
            for (int k = 1; k <= 10; k++) w.println("reached major " + k + " at age: " + stats(reach.get(k)));
            w.println("age at death (all): " + stats(deaths));
            w.println("age at death (old age): " + stats(oldAge));
            for (int k = 0; k <= 10; k++) { int[] d = byMajor.get(k); if (d != null) w.println("deaths while major " + k + ": oldAge=" + d[0] + " other=" + d[1]); }
        } catch (Exception e) { }
    }
}
