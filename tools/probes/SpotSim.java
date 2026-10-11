package Game;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/** How often the four default spot quests (Random, Profession, Hunt, Meditate) run, and what the character was doing at the time. The game itself does not call SpotSim:
 *  the measuring copy adds `SpotSim.count(this, tempQuest, targetResult);` after the spot quest of Character.quest (see spotrun.sh in the scratchpad).
 *  java -Xss512m -cp <out>;lib/flatlaf-3.7.2.jar Game.SpotSim 10 */
public class SpotSim {
    static final ConcurrentHashMap<String, LongAdder> counts = new ConcurrentHashMap<>();
    static final LongAdder awakened = new LongAdder(), beastKills = new LongAdder(), monsterKills = new LongAdder(), huntFought = new LongAdder(), huntNone = new LongAdder(), huntOther = new LongAdder(), profDid = new LongAdder(), profNothing = new LongAdder(), beastWins = new LongAdder();
    static final LongAdder turns = new LongAdder(), targetDone = new LongAdder(), noSpot = new LongAdder();

    /** Called once per character turn (NPC adults) after the spot quest was picked. */
    public static void count(Character c, Quest spot, int targetResult) {
        if (c.player != 0 || c.age < 16 || c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) return;
        turns.increment();
        if (targetResult == 1) { targetDone.increment(); return; }
        if (spot == null) { noSpot.increment(); return; }
        String where = c.currentQuest == null ? "no quest" : targetResult == 0 ? "travelling" : "other";
        counts.computeIfAbsent(spot.name + " | " + where, k -> new LongAdder()).increment();
        counts.computeIfAbsent(spot.name + " | all", k -> new LongAdder()).increment();
    }

    /** Patched into the measuring copy: a Hunt spot quest and whether it found a fight. */
    public static void hunt(Character c, boolean fought) {
        if (c.player != 0) return;
        if (fought) huntFought.increment(); else if (c.location.monsters <= 0) huntNone.increment(); else huntOther.increment();
    }
    public static void profession(Character c, int result) { if (c.player != 0) return; if (result == -2) profNothing.increment(); else profDid.increment(); }
    public static void killed(Character killer, Character victim) { if (victim.trait == Character.Traits.Beast || victim.trait == Character.Traits.Monster) return; if (killer.trait == Character.Traits.Beast) beastKills.increment(); else if (killer.trait == Character.Traits.Monster) monsterKills.increment(); }

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
        final int years = a.length > 0 ? Integer.parseInt(a[0]) : 10;
        PrintStream real = System.out;
        System.setIn(new Feed());
        System.setOut(new PrintStream(new ByteArrayOutputStream(), true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { gameError = e; e.printStackTrace(real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        Thread.sleep(3000);
        int lastYear = -1;
        while (true) {
            if (gameError != null) { real.println("GAME ERROR " + gameError); break; }
            int yr = GameDriver.time / 52;
            if (yr >= years) break;
            if (yr != lastYear) {
                lastYear = yr;
                int mon = 0, alive = 0; for (Character c : new ArrayList<>(Map.characters)) if (c != null && !c.dead) { alive++; if (c.trait == Character.Traits.Monster) mon++; }
                real.println("year " + yr + ": population " + Map.population + " (list alive " + alive + "), Monsters alive " + mon + ", awakened so far " + awakened.sum() + ", cultivators killed by wild beasts " + beastKills.sum() + ", by Monsters " + monsterKills.sum() + ", beast wins that spared " + beastWins.sum());
            }
            Thread.sleep(200);
        }
        long total = turns.sum();
        real.println("adult NPC turns: " + total + " | target quest finished that turn (no spot quest): " + pct(targetDone.sum(), total) + " | no spot quest queued: " + pct(noSpot.sum(), total));
        TreeMap<String, Long> sorted = new TreeMap<>();
        for (java.util.Map.Entry<String, LongAdder> e : counts.entrySet()) sorted.put(e.getKey(), e.getValue().sum());
        for (java.util.Map.Entry<String, Long> e : sorted.entrySet()) real.println(String.format("  %-34s %10d  %s", e.getKey(), e.getValue(), pct(e.getValue(), total)));
        real.println("Hunt spot quests: fought " + huntFought.sum() + ", no monsters in the tile " + huntNone.sum() + ", monsters too strong/weak " + huntOther.sum()
                + " | Profession spot quests: did something " + profDid.sum() + ", nothing " + profNothing.sum() + " | cultivators killed by wild beasts " + beastKills.sum() + ", by Monsters " + monsterKills.sum() + " | Monsters awakened " + awakened.sum() + " | fights a wild beast won but the cultivator escaped " + beastWins.sum());
        long n = 0, kills = 0, crafted = 0, major = 0, lvl = 0, wealth = 0, geared = 0; int top = 0;
        for (Character c : new ArrayList<>(Map.characters)) {
            if (c == null || c.dead || !c.born || c.player != 0 || c.age < 16 || c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) continue;
            n++; kills += c.kills; crafted += c.itemsCrafted; major += c.majorLvl; lvl += c.lvl; wealth += Math.min(PlayerLists.wealth(c), 1_000_000_000L); if (c.equippedWeapon != null && c.equippedClothing != null) geared++; top = Math.max(top, c.majorLvl);
        }
        int monsters = 0; for (Character c : new ArrayList<>(Map.characters)) if (c != null && !c.dead && c.trait == Character.Traits.Monster) monsters++;
        real.println(String.format("living adult NPCs %d | mean major %.2f (top %d) | mean level %.1f | kills per head %.1f | items crafted per head %.1f | wealth per head %.0f ss | fully geared %.0f%% | Monsters alive %d",
                n, major / (double) Math.max(1, n), top, lvl / (double) Math.max(1, n), kills / (double) Math.max(1, n), crafted / (double) Math.max(1, n), wealth / (double) Math.max(1, n), 100.0 * geared / Math.max(1, n), monsters));
        int bad = 0, dups = 0; java.util.IdentityHashMap<Character, Boolean> seen = new java.util.IdentityHashMap<>();
        { ArrayList<Character> all = new ArrayList<>(Map.characters); for (int i = 0; i < all.size(); i++) { Character c = all.get(i); if (c == null) continue; if (c.id != i) bad++; if (seen.put(c, true) != null) dups++; } }
        real.println("id mismatches " + bad + ", the same character in two slots " + dups + ", list size " + Map.characters.size() + ", year " + GameDriver.time / 52);
        real.println("[T] ===== SpotSim done =====");
        System.exit(0);
    }

    static String pct(long n, long total) { return total == 0 ? "-" : String.format("%.1f%%", 100.0 * n / total); }
}
