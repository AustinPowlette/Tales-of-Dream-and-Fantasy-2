package Game;

import java.io.*;
import java.util.*;

/** Samples what the characters are doing: every 150 ms it counts, over all living adult NPCs, the name of the target quest each one is on (or "(none)"),
 *  the share with an empty weapon / clothing slot, and how many Musicians hold an instrument. Prints one block per N years: the quest names ranked by share.
 *  java -Xss512m -cp <out>;lib/flatlaf-3.7.2.jar Game.QuestSim 20 5 */
public class QuestSim {
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
    // per trait: how many samples, how many on each quest family, and the sum of the characters' target shares (2026-10-08, see QuestLife)
    static final int NT = Character.Traits.values().length;
    static long[][] famCount = new long[NT][QuestLife.N];
    static long[] traitN = new long[NT];
    static double[][] tgtSum = new double[NT][QuestLife.N];

    public static void main(String[] a) throws Exception {
        final int years = a.length > 0 ? Integer.parseInt(a[0]) : 20;
        final int block = a.length > 1 ? Integer.parseInt(a[1]) : 5;
        PrintStream real = System.out;
        System.setIn(new Feed());
        System.setOut(new PrintStream(new ByteArrayOutputStream(), true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { gameError = e; e.printStackTrace(real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        Thread.sleep(3000);
        TreeMap<String, Long> counts = new TreeMap<>();
        long samples = 0, noW = 0, noC = 0, adults = 0, musicians = 0, withInst = 0, idle = 0;
        int lastTime = -1, blockStart = 0;
        while (true) {
            int time = GameDriver.time;
            if (gameError != null) { real.println("GAME ERROR " + gameError); break; }
            int yr = time / 52;
            if (yr > years) break;
            if (time == lastTime) { Thread.sleep(40); continue; }
            lastTime = time;
            if (time % 3 != 0) continue; // every third tick is plenty
            for (Character c : new ArrayList<>(Map.characters)) {
                if (c == null || c.dead || !c.born || c.player != 0 || c.age < 16 || c.majorLvl < 1 || c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) continue;
                try {
                    adults++;
                    if (c.equippedWeapon == null) noW++;
                    if (c.equippedClothing == null) noC++;
                    if (c.trait == Character.Traits.Musician) { musicians++; if (Life.bestInstrument(c) != null) withInst++; }
                    Quest q = c.currentQuest;
                    int ti = c.trait.ordinal(), fi = q == null ? -1 : QuestLife.family(q.name);
                    traitN[ti]++; if (fi >= 0) famCount[ti][fi]++;
                    double[] tg = c.qTarget; if (tg != null) for (int i = 0; i < QuestLife.N; i++) tgtSum[ti][i] += tg[i];
                    String nm = q == null ? "(none)" : q.name;
                    if (q == null) idle++;
                    counts.merge(nm, 1L, Long::sum);
                    samples++;
                } catch (RuntimeException e) { }
            }
            if (yr - blockStart >= block) {
                report(real, blockStart, yr, counts, samples, noW, noC, adults, musicians, withInst);
                counts.clear(); samples = noW = noC = adults = musicians = withInst = idle = 0; blockStart = yr;
                famCount = new long[NT][QuestLife.N]; traitN = new long[NT]; tgtSum = new double[NT][QuestLife.N];
            }
        }
        report(real, blockStart, years, counts, samples, noW, noC, adults, musicians, withInst);
        long shelf = 0, pool = 0, all = 0, carried = 0;
        for (Area ar : new ArrayList<>(Map.createdAreas)) {
            if (ar == null) continue;
            try {
                for (Shop s : ar.shops) for (Item it : s.inventory) { all++; if (it.itemType == Item.ItemType.Instrument) shelf++; }
                if (ar.backpack != null) for (Item it : ar.backpack) { all++; if (it.itemType == Item.ItemType.Instrument) pool++; }
            } catch (RuntimeException e) { }
        }
        for (Character c : new ArrayList<>(Map.characters)) if (c != null && !c.dead) for (Item it : new ArrayList<>(c.backpack)) if (it.itemType == Item.ItemType.Instrument) carried++;
        real.println("instruments: on shop shelves " + shelf + ", in city pools " + pool + ", carried " + carried + "  (items in shops and pools: " + all + ")");
        System.exit(0);
    }

    static void report(PrintStream out, int from, int to, TreeMap<String, Long> counts, long samples, long noW, long noC, long adults, long musicians, long withInst) {
        if (samples == 0) return;
        List<java.util.Map.Entry<String, Long>> es = new ArrayList<>(counts.entrySet());
        es.sort((x, y) -> Long.compare(y.getValue(), x.getValue()));
        StringBuilder sb = new StringBuilder("years " + from + "-" + to + "  samples=" + samples + "  noWeapon=" + (100 * noW / Math.max(1, adults)) + "%  noClothing=" + (100 * noC / Math.max(1, adults)) + "%  musicians=" + musicians + " withInstrument=" + withInst + "\n   ");
        int n = 0;
        for (java.util.Map.Entry<String, Long> e : es) { if (n++ >= 80) break; sb.append(String.format("%s %.1f%% | ", e.getKey(), 100.0 * e.getValue() / samples)); }
        out.println(sb);
        // the families: what they did against what they meant to do, overall and for each trait with enough samples (percent of the time spent on a family; target in brackets)
        long[] all = new long[QuestLife.N]; double[] allT = new double[QuestLife.N]; long allN = 0;
        for (int t = 0; t < NT; t++) { for (int f = 0; f < QuestLife.N; f++) { all[f] += famCount[t][f]; allT[f] += tgtSum[t][f]; } allN += traitN[t]; }
        out.println("   families (actual% [target%]): " + famLine(all, allT, allN));
        for (int t = 0; t < NT; t++) if (traitN[t] > 4000) out.println("     " + String.format("%-12s", Character.Traits.values()[t]) + famLine(famCount[t], tgtSum[t], traitN[t]));
    }

    static String famLine(long[] fam, double[] tgt, long n) {
        long fs = 0; for (long v : fam) fs += v;
        StringBuilder sb = new StringBuilder();
        for (int f = 0; f < QuestLife.N; f++) sb.append(QuestLife.FAMILY[f].split(" ")[0]).append(' ').append(fs == 0 ? 0 : Math.round(100.0 * fam[f] / fs)).append('[').append(n == 0 ? 0 : Math.round(100.0 * tgt[f] / n)).append("]  ");
        sb.append("(on a quest ").append(n == 0 ? 0 : 100 * fs / n).append("%)");
        return sb.toString();
    }
}
