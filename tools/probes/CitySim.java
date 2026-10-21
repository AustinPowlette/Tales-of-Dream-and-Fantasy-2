package Game;

import java.io.*;
import java.util.*;

/** Long-run measurement of settlements: how many cities and sects exist each year, how they are founded and destroyed, city levels, treasuries and populations,
 *  tick times (run with -Dtdf.perf=true for the per-phase numbers) and how many "[EPOCH]" purges happened. Spectates a shrunken world for N game years
 *  and prints one CSV row per year (also written to city_years.csv).  java -Xss512m -Dtdf.perf=true -cp <out>;lib/flatlaf-3.7.2.jar Game.CitySim 40 */
public class CitySim {
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
        final int years = a.length > 0 ? Integer.parseInt(a[0]) : 40;
        PrintStream real = System.out;
        System.setIn(new Feed());
        final ByteArrayOutputStream sink = new ByteArrayOutputStream();
        System.setOut(new PrintStream(sink, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { gameError = e; e.printStackTrace(real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        Thread.sleep(3000);
        String head = "year,cultivators,cities,sects,citiesNew,citiesLost,sectsNew,sectsLost,lvl1,lvl2,lvl3,lvl4plus,maxLevel,maxTreasury,avgCityPop,maxCityPop,ownerless,epochs,wallSec,perf";
        PrintWriter csv = new PrintWriter(new FileWriter("city_years.csv"));
        csv.println(head); real.println(head);
        Set<Area> prevCities = new HashSet<>(), prevSects = new HashSet<>();
        int lastYear = -1, exceptions = 0;
        long wall0 = System.currentTimeMillis();
        while (true) {
            int yr = GameDriver.time / 52;
            if (gameError != null) { real.println("GAME ERROR " + gameError); break; }
            if (yr == lastYear) { Thread.sleep(200); continue; }
            lastYear = yr;
            if (yr > years) break;
            int cult = 0;
            for (Character c : new ArrayList<>(Map.characters)) {
                if (c == null || c.dead || !c.born || c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) continue;
                cult++;
            }
            Set<Area> cities = new HashSet<>(), sects = new HashSet<>();
            int l1 = 0, l2 = 0, l3 = 0, l4 = 0, maxL = 0, ownerless = 0; long maxT = 0, popSum = 0, popMax = 0;
            for (Area ar : new ArrayList<>(Map.createdAreas)) {
                if (ar == null) continue;
                try {
                    if (ar.sect != null) sects.add(ar);
                    if (ar.city) {
                        cities.add(ar);
                        int lv = ar.cityLevel(); maxL = Math.max(maxL, lv);
                        if (lv <= 1) l1++; else if (lv == 2) l2++; else if (lv == 3) l3++; else l4++;
                        maxT = Math.max(maxT, ar.cityTreasury); popSum += ar.population; popMax = Math.max(popMax, ar.population);
                        if (ar.cityOwner == null) ownerless++;
                    }
                } catch (RuntimeException e) { exceptions++; }
            }
            int cNew = 0, cLost = 0, sNew = 0, sLost = 0;
            for (Area x : cities) if (!prevCities.contains(x)) cNew++;
            for (Area x : prevCities) if (!cities.contains(x)) cLost++;
            for (Area x : sects) if (!prevSects.contains(x)) sNew++;
            for (Area x : prevSects) if (!sects.contains(x)) sLost++;
            prevCities = cities; prevSects = sects;
            int epochs = 0, from = 0; String out = sink.toString();
            while ((from = out.indexOf("[EPOCH] The Heavens", from)) >= 0) { epochs++; from++; }
            String perf = Perf.ON ? Perf.window().replace(' ', ';') : "";
            String row = String.join(",", String.valueOf(yr), String.valueOf(cult), String.valueOf(cities.size()), String.valueOf(sects.size()), String.valueOf(cNew), String.valueOf(cLost),
                    String.valueOf(sNew), String.valueOf(sLost), String.valueOf(l1), String.valueOf(l2), String.valueOf(l3), String.valueOf(l4), String.valueOf(maxL), String.valueOf(maxT),
                    String.valueOf(cities.isEmpty() ? 0 : popSum / cities.size()), String.valueOf(popMax), String.valueOf(ownerless), String.valueOf(epochs),
                    String.valueOf((System.currentTimeMillis() - wall0) / 1000), perf);
            csv.println(row); csv.flush(); real.println(row);
        }
        csv.close();
        String out = sink.toString();
        int ex = 0; for (String line : out.split("\n")) if (line.contains("Exception") || line.contains("\tat Game.")) ex++;
        real.println("[C] " + Sieges.counts()); // how cities and sects were razed (Sieges)
        real.println("[C] exceptionLines=" + ex + " sampleExceptions=" + exceptions + " gameError=" + gameError);
        System.exit(0);
    }
}
