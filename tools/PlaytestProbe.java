package Game;

import java.io.*;
import java.util.*;

/** Long-run playtest measurement for the late-2026 systems (reincarnation pace, the goods economy, 18-year-old city births, sect caps, wars, squads, beasts).
 *  Spectates a shrunken world for N game years and writes play_years.csv (one row per year) plus play_summary.txt. Same set-up as BalProbe:
 *
 *    (copy src/Game/*.java somewhere, set Map.startingPopulation to 3000)
 *    javac -encoding UTF-8 -cp src -d out src/Game/*.java tools/PlaytestProbe.java
 *    mkdir run && cd run && java -Xss512m -cp ../out Game.PlaytestProbe 150
 *
 *  At the end it writes a save into slot "playtest" (so a second run of Game.LoadProbe, or the real game, can load a long-lived world). See "Playtest Plan.md". */
public class PlaytestProbe {
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
        final int years = a.length > 0 ? Integer.parseInt(a[0]) : 100;
        PrintStream real = System.out;
        System.setIn(new Feed());
        final ByteArrayOutputStream sink = new ByteArrayOutputStream();
        System.setOut(new PrintStream(sink, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { gameError = e; e.printStackTrace(real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        Thread.sleep(3000);
        PrintWriter csv = new PrintWriter(new FileWriter("play_years.csv"));
        csv.println("year,wallSec,heapMB,pop,cultivators,beasts,tamed,reincarnationsLogged,cities,sects,largestSect,avgSect,sectsOverCap,warsActive,warsStartedLogged,truces,treachery,"
                + "warsDeclaredTotal,warsEndedTotal,largestSquad,squadMembers,leaderSquadMembers,marketGoods,goodsHeld,totalWealthSS,medianWealth,top1pctShare,maxMajor,major3plus,major5plus,age18inflow,exceptions,idMismatch,"
                + "spBullied,spMurders,spMurdersExposed,spThefts,spPoisonings,spFramings,spPlots,spCoupsWon,spCoupsFailed,spTreacheries,fightReports,palaceMembers,convocations,crossedNpc,omens");
        long t0 = System.currentTimeMillis();
        int lastYear = -1, prevLogged = 0;
        Set<Integer> seen = new HashSet<>();
        int exceptions = 0;
        while (true) {
            int yr = GameDriver.time / 52;
            if (gameError != null) { real.println("GAME ERROR " + gameError); break; }
            if (yr == lastYear) { Thread.sleep(200); continue; }
            lastYear = yr;
            if (yr > years) break;
            int n = Map.characters.size(), beasts = 0, cult = 0, tamed = 0, maxMajor = 0, m3 = 0, m5 = 0, age18 = 0, squadMembers = 0, leaderSquad = 0, goodsHeld = 0;
            List<Long> wealth = new ArrayList<>();
            java.util.Map<Integer, Integer> squads = new HashMap<>();
            Set<Integer> now = new HashSet<>();
            for (int i = 0; i < n; i++) {
                Character c = Map.characters.get(i);
                if (c == null || c.dead || !c.born) continue;
                if (c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) { beasts++; if (c.wasTamed) tamed++; continue; }
                cult++; now.add(i);
                if (c.age == 18 && !seen.contains(i)) age18++;
                maxMajor = Math.max(maxMajor, c.majorLvl); if (c.majorLvl >= 3) m3++; if (c.majorLvl >= 5) m5++;
                wealth.add(PlayerLists.wealth(c));
                final int pl = c.partyLeaderId; /* read once: the game thread changes it while we count */ if (pl >= 0 && pl != c.id) { squadMembers++; squads.merge(pl, 1, Integer::sum); Character l = pl < n ? Map.characters.get(pl) : null; if (l != null && l.trait == Character.Traits.Leader) leaderSquad++; }
                try { if (c.goods != null) for (Item it : c.goods) goodsHeld += it.quantity; } catch (RuntimeException e) { exceptions++; }
            }
            seen = now;
            int largestSquad = 0; for (int v : squads.values()) largestSquad = Math.max(largestSquad, v + 1);
            int cities = 0, sects = 0, largest = 0, over = 0, wars = 0; long sectSum = 0; int marketGoods = 0;
            for (Area ar : new ArrayList<>(Map.createdAreas)) {
                if (ar.city) { cities++; try { marketGoods += Life.marketCount(ar); } catch (RuntimeException e) { exceptions++; } }
                if (ar.sect != null) {
                    sects++; int sz = ar.sect.members == null ? 0 : ar.sect.members.size(); sectSum += sz; largest = Math.max(largest, sz);
                    if (sz > ar.sect.memberCap()) over++;
                    if (ar.sect.warTarget != null) wars++;
                }
            }
            int reinc = 0, warsLogged = 0, truces = 0, treach = 0;
            try {
                for (Chronicle.Entry e : new ArrayList<>(Chronicle.log)) {
                    if ("reincarnation".equals(e.kind)) reinc++;
                    if ("war".equals(e.kind)) { if (e.text.contains("declared war")) warsLogged++; if (e.text.contains("truce")) truces++; if (e.text.contains("treachery")) treach++; }
                }
            } catch (RuntimeException e) { exceptions++; }
            Collections.sort(wealth);
            long total = 0; for (long w : wealth) total += w;
            long top = 0; int k1 = Math.max(1, wealth.size() / 100); for (int i = wealth.size() - k1; i < wealth.size(); i++) if (i >= 0) top += wealth.get(i);
            long median = wealth.isEmpty() ? 0 : wealth.get(wealth.size() / 2);
            long heap = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) >> 20;
            int idBad = 0;
            for (int i = 0; i < Map.characters.size(); i++) { Character c = Map.characters.get(i); if (c != null && c.id != i) idBad++; } // the load-bearing rule: id == index
            String chatter = sink.toString();
            int ex = chatter.split("Exception", -1).length - 1;
            if (sink.size() > 40_000_000) sink.reset();
            csv.print(String.format("%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%.1f,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%.3f,%d,%d,%d,%d,%d,%d", yr, (System.currentTimeMillis() - t0) / 1000, heap, Map.population, cult, beasts, tamed, reinc, cities, sects, largest,
                    sects == 0 ? 0 : sectSum / (double) sects, over, wars, warsLogged, truces, treach, Organization.warsDeclared, Organization.warsEnded, largestSquad, squadMembers, leaderSquad, marketGoods, goodsHeld, total, median, total == 0 ? 0 : top / (double) total, maxMajor, m3, m5, age18, exceptions + ex, idBad));
            Organization pal = Lore.state != null ? Lore.palace() : null;
            csv.println("," + SectPolitics.bullied + "," + SectPolitics.murders + "," + SectPolitics.murdersExposed + "," + SectPolitics.thefts + "," + SectPolitics.poisonings + "," + SectPolitics.framings + "," + SectPolitics.plotsHatched + "," + SectPolitics.coupsWon + "," + SectPolitics.coupsFailed + "," + SectPolitics.treacheries + "," + FightReport.made + "," + (pal == null ? 0 : pal.members.size()) + "," + (Lore.state == null ? 0 : Lore.state.held) + "," + (Lore.state == null ? 0 : Lore.state.crossed) + "," + (Lore.state == null ? 0 : Lore.state.omens));
            csv.flush();
            if (yr == years) { GameDriver.saveRequestName = "playtest"; try { Thread.sleep(20000); } catch (InterruptedException e) { } }
        }
        try (PrintWriter w = new PrintWriter("play_summary.txt")) { w.println("finished year " + lastYear + " in " + (System.currentTimeMillis() - t0) / 1000 + " s; gameError=" + gameError); }
        csv.close();
        System.exit(0);
    }
}
