package Game;

import java.io.*;
import java.util.*;

/** 2026-10-18, a measurement and not a test: how many people stand at Ascension, Half-Immortal, Pseudo Immortal and True Immortal every ten years, and how many went through the Seam on their own
 *  (`Settings.leavePct`, `GameInfo/Garden at Ascension Design.md`). Spectates a world for N game years. Arguments: years (default 150), `off` (all chances 0, for the comparison) or four numbers
 *  `a h p t` (the chances). Run in an empty directory against a copy of the sources whose Map.startingPopulation was changed (20,000 is a good size):
 *    java -Xss512m -cp ../out Game.LeaveSim 150 off   and   java -Xss512m -cp ../out Game.LeaveSim 150
 *  -Dleave.seed=N starts N NPCs at the top of Nihility with 100 potential (the realms above are otherwise reached by almost nobody in 150 years). Prints one line per ten years: year, living cultivators, those at Major 7 / 8 / 9 / 10, the leavers per realm (ascension, half, pseudo, lamp), lamps carried, seekers, the Roll of the Crossed. */
public class LeaveSim {
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
        final int years = a.length > 0 ? Integer.parseInt(a[0]) : 150;
        if (a.length > 1 && a[1].equals("off")) Settings.leavePct = new int[]{0, 0, 0, 0};
        else if (a.length > 4) Settings.leavePct = new int[]{Integer.parseInt(a[1]), Integer.parseInt(a[2]), Integer.parseInt(a[3]), Integer.parseInt(a[4])};
        PrintStream real = System.out;
        System.setIn(new Feed());
        System.setOut(new PrintStream(new ByteArrayOutputStream(), true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { gameError = e; e.printStackTrace(real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        Thread.sleep(3000);
        int seed = Integer.getInteger("leave.seed", 0);
        if (seed > 0) { // a hurry: this many NPCs start at the top of Nihility with 100 potential (so that the realms above are visited within the run)
            int n = 0;
            for (Character c : new ArrayList<>(Map.characters)) {
                if (c == null || c.dead || c.player != 0 || c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster || c.age < 18 || n >= seed) continue;
                c.potential = 100; c.majorLvl = 6; c.minorLvl = 9; c.lvl = 69; c.maxAge = Math.max(c.maxAge, 1_000_000); n++;
            }
            real.println("seeded " + n + " cultivators at Major 6, level 69, potential 100");
        }
        real.println("leavePct " + Arrays.toString(Settings.leavePct) + ", start population " + Map.startingPopulation);
        real.println("year  living   M6   M7   M8   M9  M10 | left: asc half pseu lamp | lamps seekers roll  wallSec");
        long t0 = System.currentTimeMillis();
        int lastDecade = -1;
        while (true) {
            int yr = GameDriver.time / 52;
            if (gameError != null) { real.println("GAME ERROR " + gameError); break; }
            if (yr / 10 == lastDecade) { Thread.sleep(300); continue; }
            lastDecade = yr / 10;
            int[] major = new int[11]; int living = 0;
            for (Character c : new ArrayList<>(Map.characters)) {
                if (c == null || c.dead || c.trait == Character.Traits.Beast) continue;
                living++; if (c.majorLvl >= 0 && c.majorLvl <= 10) major[c.majorLvl]++;
            }
            Lore.State s = Lore.state;
            int[] left = s == null || s.leftAt == null ? new int[4] : s.leftAt;
            real.println(String.format("%4d %7d %4d %4d %4d %4d %4d | %9d %4d %4d %4d | %5d %7d %4d  %d", yr, living, major[6], major[7], major[8], major[9], major[10], left[0], left[1], left[2], left[3],
                    s == null ? 0 : LoreEndings.lampsCarried(s), s == null || s.seekers == null ? 0 : s.seekers.size(), s == null ? 0 : s.crossed, (System.currentTimeMillis() - t0) / 1000));
            if (yr >= years) break;
        }
        System.exit(0);
    }
}
