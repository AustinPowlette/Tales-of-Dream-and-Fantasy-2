package Game;

import java.util.List;

/** Fame: how widely a cultivator's name is known. It is earned by winning tournaments (the Garden's Variety Tournament most of all), hunting beasts and Monsters, killing cultivators who are a match for the killer,
 *  crafting, breaking through to a new realm and getting a title. It fades four percent every New Year, so the board ("Most Famous", `Rankings`) shows who is famous now and not who was long ago.
 *  A Beast earns nothing; a Monster that woke in the wilds earns it like a cultivator (its kills and hunts are the stuff of legend). Everything here is a plain number added to `Character.fame`. */
final class Fame {
    private Fame() { }

    static final int DECAY_PERCENT = 4;       // lost at every New Year
    static final int CRAFT_CAP = 300;         // items crafted in a year that count (a hundred pills in a week are not a hundred times as famous)

    /** Adds fame (never to a Beast). */
    static void add(Character c, long amount) {
        if (c == null || amount <= 0 || c.trait == Character.Traits.Beast) return;
        c.fame = (int) Math.min(Integer.MAX_VALUE / 2, (long) c.fame + amount);
    }

    /** A kill: a beast or a Monster is hunted (a Monster is worth ten beasts), a cultivator is slain (worth more the nearer the victim was to the killer in realm; the weak are no glory, and nobody under 18 is). */
    static void slain(Character killer, Character victim) {
        if (killer == null || victim == null || killer.trait == Character.Traits.Beast) return;
        if (victim.trait == Character.Traits.Beast) { if (victim.majorLvl >= killer.majorLvl - 1) add(killer, 1 + victim.majorLvl / 2); return; }
        if (victim.trait == Character.Traits.Monster) { add(killer, 10L * (victim.majorLvl + 1)); return; }
        if (victim.age < 18) return;
        int gap = killer.majorLvl - victim.majorLvl;
        if (gap >= 2) return;
        add(killer, Math.max(1, 3L * (victim.majorLvl + 1) - 3L * Math.max(0, gap)));
    }

    /** A new realm reached: 10 at Foundation, doubling with every realm (5,120 at True Immortal). */
    static void breakthrough(Character c) { if (c.majorLvl >= 1) add(c, 10L << Math.min(20, c.majorLvl - 1)); }

    /** A reputation title earned or changed. */
    static void titled(Character c) { add(c, 30); }

    /** A tournament won (or reached the final): the more rounds, and the higher the realms that may enter, the greater the name. */
    static void tournament(Character c, int rounds, int maxMajor, boolean won) {
        long f = 20L * Math.max(1, rounds) * (maxMajor + 1);
        add(c, won ? f : f / 3);
    }

    /** Once a year, before the boards are counted: the crafting of the year becomes fame, the old fame fades. */
    static void yearly(List<Character> all) {
        for (Character c : all) {
            if (c == null || c.trait == Character.Traits.Beast) continue;
            int made = c.itemsCrafted - c.fameCrafted;
            if (made > 0) { add(c, Math.min(made, CRAFT_CAP) / 5); }
            c.fameCrafted = c.itemsCrafted;
            if (c.fame > 0) c.fame = (int) ((long) c.fame * (100 - DECAY_PERCENT) / 100);
        }
    }
}
