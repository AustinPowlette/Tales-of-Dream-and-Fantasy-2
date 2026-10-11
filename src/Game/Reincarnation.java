package Game;

import java.util.ArrayList;
import java.util.List;

/** Reincarnation: the reward for reaching the end of your rope. Potential decides how far a cultivator can climb: each realm gate (the major level
 *  breakthroughs at 20, 37, 52, 65, 76, 85, 92, 97, 99 and 100 potential: `Character.GATE_POTENTIAL`) is nearly closed to anyone below its threshold. A cultivator of Major Lvl 3
 *  (Soul Wandering) or more who has reached the last level before a gate they cannot pass is offered the "Reincarnate" spot quest. It is the SAME
 *  character, given a second youth: age 18 again, level 1, cultivation 0, the injuries of failed breakthroughs forgotten (and the lifespan of the
 *  realms with them), potential +5. Name, family, sect, relationships, wealth, items and techniques stay. Companion beasts beyond what a Major Lvl 0
 *  cultivator may keep wait where they stand and get a quest to be found again. Death is not involved and Brutal does not change anything. */
final class Reincarnation {
    private Reincarnation() { }

    static final int[] GATES = Character.GATE_POTENTIAL;
    static final int GAIN = 5;
    static final int REBORN_AGE = GameDriver.START_AGE; // a second youth: the age everyone started at
    static final int MIN_MAJOR = 3; // Soul Wandering: only a soul that has begun to wander can be reborn

    /** The highest level a cultivator of this potential reaches without a lucky breakthrough: 9, 19, 29, ... */
    static int cap(int potential) {
        int k = 0;
        for (int g : GATES) if (potential >= g) k++;
        return 10 * k + 9;
    }

    static boolean ready(Character c) {
        return c != null && !c.dead && c.born && c.age >= 8 && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster
                && !c.isGhostForm && c.potential < 100 && c.majorLvl >= MIN_MAJOR && c.lvl >= cap(c.potential);
    }

    /** Why a cultivator cannot reincarnate yet. */
    static String notReady(Character c) {
        if (c.potential >= 100) return "Your potential is already complete: no gate can hold you.";
        if (c.majorLvl < MIN_MAJOR) return "Only a soul that has reached Major Lvl " + MIN_MAJOR + " (Soul Wandering) can be reborn. Your rope (potential " + c.potential + ") ends at level " + cap(c.potential)
                + (cap(c.potential) < 10 * MIN_MAJOR ? ", which is below it: raise your potential first (Potential Pills, insights) so that you can climb to Soul Wandering." : ".");
        return "You have not reached the end of your rope yet (level " + c.lvl + "; potential " + c.potential + " ends at level " + cap(c.potential) + ").";
    }

    /** Posts the spot quest once the rope has run out. */
    static void check(Character c) {
        if (c.lvl < 10 * MIN_MAJOR || !ready(c)) return;
        for (Quest q : c.spotQuests) if ("Reincarnate".equals(q.name)) return;
        c.spotQuests.add(new Quest("Reincarnate", true, 95, 0, 95));
    }

    /** Returns null on success (the character is young again), otherwise why not. */
    static String perform(Character c) {
        if (c.dead) return "You are dead.";
        if (!ready(c)) return notReady(c);
        String oldRealm = "Lv." + c.lvl + " (Major " + c.majorLvl + ")";
        int oldPotential = c.potential, oldAge = c.age, oldKills = c.kills;
        if (c.pastLives == null) c.pastLives = new ArrayList<>();
        c.pastLives.add("Life " + (c.lives + 1) + ": reached " + oldRealm + " at age " + oldAge + " in year " + GameDriver.time / 52 + ", potential " + oldPotential + ", " + oldKills + " kills");
        c.lives++;
        if (c.soulId < 0) c.soulId = c.id;
        RunStats.endLife(c, "reincarnated at " + oldRealm);

        // a second youth
        c.repTitle = null; // the title of the old life (Strongest, Richest ...) goes with it; the next New Year may give a new one
        c.potential = Math.min(101, c.potential + GAIN);
        c.age = REBORN_AGE;
        c.maxAge = 100; c.totalAgeGained = 100;           // the lifespan of the realms is earned again
        c.lvl = 1; c.majorLvl = 0; c.minorLvl = 1; c.tempLvl = 1;
        c.inhibitor = 1;                                   // failed breakthroughs are forgotten (1 is the default of a new cultivator)
        c.exhaustionPenalty = 0;
        c.cultivationBase = 0;
        c.cultivationBaseMax = Math.pow(c.lvl, 3) * (((70. + 1. / 3.) + ((11. / 6.) * c.lvl)) / 25.);
        c.cultivation = (Math.pow(c.potential, 2) * Math.sqrt(c.lvl) * 10.) / (52. * Math.sqrt(c.potential) * Math.pow(2, c.inhibitor));
        c.fx = null;
        c.currentQuest = null;

        // companions beyond what a Major Lvl 0 cultivator may keep wait where they stand and remember the soul
        List<Character> beasts = Taming.beasts(c);
        List<Integer> lost = new ArrayList<>(c.lostBeastIds == null ? new ArrayList<Integer>() : c.lostBeastIds);
        for (int i = Taming.cap(c); i < beasts.size(); i++) { Character b = beasts.get(i); Taming.waitFor(b, c.soulId); if (c.beastIds != null) c.beastIds.remove(Integer.valueOf(b.id)); lost.add(b.id); }
        if (!lost.isEmpty()) { c.lostBeastIds = new ArrayList<>(lost); Taming.postLostQuest(c); }

        RunStats.onReincarnation(c);
        String msg = c.name + " " + c.lastName + " has been reborn: a second youth at age " + c.age + ", level 1, potential " + oldPotential + " -> " + c.potential + " (life " + (c.lives + 1) + ").";
        boolean notable = c.player > 0 || Map.trackedCharacters.contains(c.id) || oldPotential >= 60;
        if (notable) Chronicle.add("reincarnation", msg, c.id);
        if (c.player == 1) Notifications.post("reincarnation", msg, c.id, c.id);
        if (c.player > 0) System.out.println(msg);
        return null;
    }

    /** One line for the stats screen. */
    static String status(Character c) {
        if (c.potential >= 100) return "Rope: potential " + c.potential + " — no gate can hold you.";
        return "Rope: potential " + c.potential + " carries you to level " + cap(c.potential) + (ready(c) ? " — you have reached it: 'do Reincarnate'." : c.majorLvl < MIN_MAJOR ? " (reincarnation needs Major Lvl " + MIN_MAJOR + ")." : ".")
                + (c.lives > 0 ? "  Lives lived before: " + c.lives : "");
    }

    static String lineage(Character c) {
        StringBuilder sb = new StringBuilder("=== Past lives of " + c.name + " " + c.lastName + " ===\n");
        if (c.pastLives != null) for (int i = 0; i < c.pastLives.size(); i++) sb.append("  ").append(c.pastLives.get(i)).append("\n");
        sb.append("  Life ").append(c.lives + 1).append(" (now): Lv.").append(c.lvl).append(", age ").append(c.age).append(", potential ").append(c.potentialLabel()).append("\n");
        if (c.lives == 0) sb.append("(no reincarnation yet)\n");
        sb.append(status(c)).append("\n");
        return sb.toString();
    }
}
