package Game;

import java.io.*;
import java.util.*;

/** Long-run measurement of the Monsters (beasts that woke after killing a cultivator): how many there are, how they die (a cultivator, another wild creature, time), how long they live, how strong the strongest get
 *  against the strongest cultivators, how much they hunt (cultivators, beasts). Spectates a shrunken world for N game years and prints one CSV row per year (also monster_years.csv).
 *  Same set-up as EconSim:  java -Xss512m -cp <out>;lib/flatlaf-3.7.2.jar Game.MonsterSim 40 */
public class MonsterSim {
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
        String head = "year,cultivators,beasts,monsters,awakened,dieByCultivator,dieByWild,dieNature,avgAgeAtDeath,killsOfCultivators,killsOfBeasts,maxMonsterLvl,monsterP90Lvl,top20CultLvl,monstersAboveTop20,monstersMajor3,monstersMajor5,monstersMajor7,maxCultLvl,births";
        PrintWriter csv = new PrintWriter(new FileWriter("monster_years.csv"));
        csv.println(head); real.println(head);
        int lastYear = -1;
        int pBirths = 0, pAw = 0, pCult = 0, pWild = 0, pNat = 0, pKC = 0, pKB = 0; long pLife = 0;
        while (true) {
            int yr = GameDriver.time / 52;
            if (gameError != null) { real.println("GAME ERROR " + gameError); break; }
            if (yr == lastYear) { Thread.sleep(200); continue; }
            lastYear = yr;
            if (yr > years) break;
            int n = Map.characters.size(), cult = 0, beasts = 0, mons = 0, m3 = 0, m5 = 0, m7 = 0; int maxM = 0, maxC = 0;
            List<Integer> cl = new ArrayList<>(), ml = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                Character c = Map.characters.get(i);
                if (c == null || c.dead || !c.born) continue;
                try {
                    if (c.trait == Character.Traits.Beast) beasts++;
                    else if (c.trait == Character.Traits.Monster) { mons++; maxM = Math.max(maxM, c.lvl); ml.add(c.lvl); if (c.majorLvl >= 3) m3++; if (c.majorLvl >= 5) m5++; if (c.majorLvl >= 7) m7++; }
                    else if (c.age >= 8) { cult++; cl.add(c.lvl); maxC = Math.max(maxC, c.lvl); }
                } catch (RuntimeException e) { }
            }
            Collections.sort(cl, Collections.reverseOrder()); Collections.sort(ml);
            int top20 = cl.size() >= 20 ? cl.get(19) : (cl.isEmpty() ? 0 : cl.get(cl.size() - 1));
            int above = 0; for (int l : ml) if (l >= top20) above++;
            int p90 = ml.isEmpty() ? 0 : ml.get(Math.min(ml.size() - 1, (int) (ml.size() * 0.9)));
            int aw = Character.stMonAwake, dc = Character.stMonDieByCult, dw = Character.stMonDieByWild, dn = Character.stMonDieNature, kc = Character.stMonKillCult, kb = Character.stMonKillBeast; long life = Character.stMonLifeSum;
            int deaths = (dc - pCult) + (dw - pWild) + (dn - pNat);
            String row = String.join(",", String.valueOf(yr), String.valueOf(cult), String.valueOf(beasts), String.valueOf(mons), String.valueOf(aw - pAw), String.valueOf(dc - pCult), String.valueOf(dw - pWild), String.valueOf(dn - pNat),
                    String.format("%.1f", deaths == 0 ? 0 : (double) (life - pLife) / deaths), String.valueOf(kc - pKC), String.valueOf(kb - pKB), String.valueOf(maxM), String.valueOf(p90), String.valueOf(top20), String.valueOf(above), String.valueOf(m3), String.valueOf(m5), String.valueOf(m7), String.valueOf(maxC), String.valueOf(Map.stBirths - pBirths));
            pBirths = Map.stBirths;
            pAw = aw; pCult = dc; pWild = dw; pNat = dn; pKC = kc; pKB = kb; pLife = life;
            csv.println(row); csv.flush(); real.println(row);
        }
        csv.close();
        real.println("[E] finished-by: " + Monsters.stBy + "  survived=" + Monsters.stSurvived + " finished=" + Monsters.stFinished + " hunts=" + Monsters.stHunts);
        real.println("[E] gameError=" + gameError);
        System.exit(0);
    }
}
