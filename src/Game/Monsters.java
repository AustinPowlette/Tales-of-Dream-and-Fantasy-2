package Game;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/** The life of the Monsters (a wild beast that killed a cultivator wakes as a Monster). Measured before this change (a 3,000-person world, 40 years): about as many Monsters were killed by cultivators every year as woke
 *  (sixty or so), because a Monster beaten by a cultivator ALWAYS died (no death roll, unlike a cultivator), and a few dozen at a time stood above the twentieth-strongest cultivator. Now:
 *  - a Monster beaten by an NPC cultivator dies on 18 in 100 (`KILL_ODDS`); otherwise it is hurt (cultivation down 30%) and lives on. A person who plays still chooses to attack and kills.
 *  - a Monster is never stronger than the twentieth-strongest cultivator: its level stops at that level (`cap()`, recounted every New Year and clamped then; a stronger one is brought down to it when it wakes).
 *  - it hunts: its "Seek Territory" walk now goes to a hunting ground near it (a wild tile with beasts, or with cultivators it can beat; never a city, a sect or an array), beasts three times as often as cultivators,
 *    and there it fights (`huntAt`): beasts mostly, cultivators weaker than itself otherwise.
 *  - it is wary (`CAUTION`): it tries to get away from a fight it would probably lose.
 *  - the strong ones appear on the boards of the world (Rankings: Highest Level, Most Kills, Highest Fighting Level) and are spared by the epoch. */
final class Monsters {
    private Monsters() { }

    static final int RANK = 20;          // a Monster is never stronger than the cultivator of this rank (by level)
    static final int KILL_ODDS = 18;     // percent: an NPC cultivator who beat a Monster finishes it (the fight's own death roll, which follows the bad blood between the two, is not used: a Monster is hurt, not hunted down)
    static final int CAUTION = 55;       // a Monster tries to flee when its chance to win is under this
    static final int RADIUS = 6;         // how far from itself a Monster looks for a hunting ground
    static volatile int stSurvived, stFinished, stHunts;   // counters (tests, the playtest tools; not saved)
    static final java.util.Map<String, Integer> stBy = new java.util.concurrent.ConcurrentHashMap<>(); // who finished Monsters, by the quest the winner was on (the playtest tools)

    private static volatile int cap = -1;

    /** The level a Monster cannot pass: the level of the twentieth-strongest living cultivator (counted at the New Year; the first time it is asked it is counted at once). */
    static int cap() {
        if (cap < 0) refreshCap(Map.characters);
        return cap;
    }

    static void refreshCap(List<Character> all) {
        PriorityQueue<Integer> heap = new PriorityQueue<>();   // the levels of the strongest RANK cultivators, the lowest at the head
        for (Character c : all) {
            if (!Rankings.eligible(c) || c.age < 8) continue;
            if (heap.size() < RANK) heap.add(c.lvl);
            else if (c.lvl > heap.peek()) { heap.poll(); heap.add(c.lvl); }
        }
        cap = heap.isEmpty() ? 10 : Math.max(10, heap.peek());
    }

    /** A Monster stronger than the cap is brought down to it (the bar starts again at half). */
    static void clamp(Character m) {
        int c = cap();
        if (m == null || m.trait != Character.Traits.Monster || m.lvl <= c) return;
        m.lvl = c;
        m.majorLvl = Math.max(0, (c - 1) / 10);
        m.minorLvl = Math.max(1, c - 10 * m.majorLvl);
        m.cultivationBaseMax = Math.pow(m.lvl, 3) * (((70. + 1. / 3.) + ((11. / 6.) * m.lvl)) / 25.);
        m.cultivation = ((Math.pow(m.potential, 2) * Math.sqrt(m.lvl) * 10.) / (52. * Math.sqrt(Math.max(1, m.potential)) * Math.pow(2, m.inhibitor)));
        m.cultivationBase = m.cultivationBaseMax / 2;
    }

    /** Once a year (and when a Monster wakes): recount the cap and bring down any Monster above it. */
    static void yearly(List<Character> all) {
        refreshCap(all);
        for (Character c : all) if (c != null && !c.dead && c.trait == Character.Traits.Monster) clamp(c);
    }

    /** The Monster that has just woken: wary, and not above the cap. */
    static void woke(Character m) {
        m.caution = CAUTION;
        clamp(m);
    }

    /** A Monster has been beaten by `winner` (an NPC): does it die? Not by the fight's own death roll (that follows the enmity between the two, and a Monster that has fought is everybody's enemy): 18 in 100. */
    static boolean dies(Character monster, Character winner, boolean lethal) {
        boolean dead = GameDriver.rand.nextInt(100) < KILL_ODDS;
        if (dead) stFinished++; else stSurvived++;
        String q = winner == null ? "?" : (dead ? "killed:" : "spared:") + (winner.currentQuest != null ? winner.currentQuest.name : "spot") + (winner.trait == Character.Traits.Monster || winner.trait == Character.Traits.Beast ? "(wild)" : "");
        stBy.merge(q, 1, Integer::sum);
        return dead;
    }

    // ---------------------------------------------------------------- hunting
    /** A hunting ground within RADIUS tiles: a wild tile with beasts (weight 3), or with cultivators the Monster can beat (weight 1); never a city, a sect or a tile with an array. null: none. */
    static Area huntGround(Character m) {
        Area here = m.location;
        if (here == null) return null;
        List<Area> pool = new ArrayList<>(); List<Integer> weights = new ArrayList<>(); int total = 0;
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                if (dx == 0 && dy == 0) continue;
                int x = Math.floorMod(here.x + dx, Map.range), y = Math.floorMod(here.y + dy, Map.range);
                Area a = Map.tiles[x][y];
                if (a == null || a.city || a.sect != null || (a.arrayBag != null && !a.arrayBag.isEmpty())) continue;
                int w = 0;
                if (a.monsters > 0 && a.lvl <= m.lvl + 6) w += 3;
                if (weakerCultivator(m, a) != null) w += 1;
                if (w == 0) continue;
                pool.add(a); weights.add(w); total += w;
            }
        }
        if (pool.isEmpty()) return null;
        int r = GameDriver.rand.nextInt(total);
        for (int i = 0; i < pool.size(); i++) { r -= weights.get(i); if (r < 0) return pool.get(i); }
        return pool.get(pool.size() - 1);
    }

    /** A cultivator on the tile that the Monster can beat (not much above its level), the weakest first; null if none. */
    static Character weakerCultivator(Character m, Area a) {
        Character best = null;
        try {
            int seen = 0;
            for (Character c : a.characters) {
                if (c == null || c.dead || c == m || !c.born || c.age < 8 || c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) continue;
                if (++seen > 12) break;
                if (c.lvl > m.lvl + 5) continue;
                if (best == null || c.lvl < best.lvl) best = c;
            }
        } catch (RuntimeException e) { }
        return best;
    }

    /** At the hunting ground: beasts mostly (they are three times the prey), cultivators weaker than the Monster otherwise. The fight is the usual one; a kill feeds the Monster as it always did. */
    static void huntAt(Character m) {
        stHunts++;
        Area a = m.location;
        if (a == null) return;
        boolean beastsHere = a.monsters > 0;
        Character prey = weakerCultivator(m, a);
        if (beastsHere && (prey == null || GameDriver.rand.nextInt(100) < 70)) { if (m.beastBattleCheck()) return; }
        if (prey != null) m.battleCheck(prey);
    }
}
