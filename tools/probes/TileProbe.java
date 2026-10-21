package Game;

import java.io.*;
import java.util.*;

/** Long spectator run of the real 100,000 world that measures how full the tiles get and how potential spreads: every N game years one line in tiles.csv with the occupancy of the wild tiles
 *  (median, 90th and 99th percentile, the fullest) and how many tiles are over 50%, 80%, 100%, 150%, 250% and 400% of the limit, the hotspots, the cities against their own limit, Originis, and
 *  the potential and realms of the living (mean, how many have 52+, 85+, 99+, 100; people in each realm).
 *  java -Xmx4g -Xss512m -cp out Game.TileProbe <lastYear> <everyYears>      (-Dmem.input="n\n100000\nw\n" is the default: a new world watched from outside) */
public class TileProbe {
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

    static int pct(int[] sorted, double q) { return sorted.length == 0 ? 0 : sorted[Math.min(sorted.length - 1, (int) Math.floor(q * sorted.length))]; }

    public static void main(String[] a) throws Exception {
        final int lastYear = a.length > 0 ? Integer.parseInt(a[0]) : 70;
        final int every = a.length > 1 ? Integer.parseInt(a[1]) : 5;
        PrintStream real = System.out;
        System.setIn(new Feed());
        final PrintStream log = new PrintStream(new FileOutputStream("game_out.txt"), true);
        System.setOut(log);
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { gameError = e; e.printStackTrace(real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        PrintWriter csv = new PrintWriter(new FileWriter("tiles.csv", true), true);
        csv.println("year,wallSec,tickMs,pop,wildCap,cityCap,wildTiles,p50,p90,p99,max,over50,over80,over100,over150,over250,over400,hotspots,cityTiles,cityMax,cityOverCap,originis,top5,potMean,pot52,pot85,pot99,pot100,realms0to10");
        long t0 = System.currentTimeMillis();
        int lastLogged = -1; long lastWall = t0; int lastTime = 0;
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
            try {
                int cap = GameDriver.CROWD_CAPACITY, capCity = GameDriver.CROWD_CAPACITY_CITY;
                ArrayList<Integer> wild = new ArrayList<>(), cities = new ArrayList<>();
                int hot = 0, origin = 0;
                for (int x = 0; x < Map.range; x++) for (int y = 0; y < Map.range; y++) {
                    Area ar = Map.tiles[x][y];
                    if (ar == null) continue;
                    int n = ar.characters.size();
                    if (ar.crowdHotspot) hot++;
                    if (ar.isOriginis()) { origin = n; continue; }
                    if (ar.city) cities.add(n); else wild.add(n);
                }
                int[] w = new int[wild.size()]; for (int i = 0; i < w.length; i++) w[i] = wild.get(i);
                Arrays.sort(w);
                int[] ct = new int[cities.size()]; for (int i = 0; i < ct.length; i++) ct[i] = cities.get(i);
                Arrays.sort(ct);
                int[] over = new int[6]; double[] lim = {0.5, 0.8, 1.0, 1.5, 2.5, 4.0};
                for (int n : w) for (int k = 0; k < 6; k++) if (n > cap * lim[k]) over[k]++;
                int cityOver = 0; for (int n : ct) if (n > capCity) cityOver++;
                StringBuilder top = new StringBuilder();
                for (int k = 0; k < Math.min(5, w.length); k++) top.append(k > 0 ? "/" : "").append(w[w.length - 1 - k]);
                double sum = 0; int pop = 0, p52 = 0, p85 = 0, p99 = 0, p100 = 0; int[] realm = new int[11];
                for (Character c : new ArrayList<>(Map.characters)) {
                    if (c == null || c.dead || !c.born || c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) continue;
                    pop++; sum += c.potential;
                    if (c.potential >= 52) p52++;
                    if (c.potential >= 85) p85++;
                    if (c.potential >= 99) p99++;
                    if (c.potential >= 100) p100++;
                    realm[Math.max(0, Math.min(10, c.majorLvl))]++;
                }
                StringBuilder rs = new StringBuilder();
                for (int k = 0; k < 11; k++) rs.append(k > 0 ? "/" : "").append(realm[k]);
                csv.println(yr + "," + (now - t0) / 1000 + "," + tickMs + "," + Map.population + "," + cap + "," + capCity + "," + w.length + "," + pct(w, 0.5) + "," + pct(w, 0.9) + "," + pct(w, 0.99) + "," + (w.length == 0 ? 0 : w[w.length - 1])
                        + "," + over[0] + "," + over[1] + "," + over[2] + "," + over[3] + "," + over[4] + "," + over[5] + "," + hot + "," + ct.length + "," + (ct.length == 0 ? 0 : ct[ct.length - 1]) + "," + cityOver + "," + origin + "," + top
                        + "," + String.format(Locale.ROOT, "%.2f", pop == 0 ? 0 : sum / pop) + "," + p52 + "," + p85 + "," + p99 + "," + p100 + "," + rs);
            } catch (Throwable e) { csv.println(yr + ",measure failed " + e); }
        }
        csv.println("done");
        csv.close();
        System.exit(0);
    }
}
