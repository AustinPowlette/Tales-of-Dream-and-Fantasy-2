package Game;

import java.util.Random;

/** The five elements of cultivation. Every cultivator (and every beast) follows a Path, a named way of cultivating that belongs to one element: Wood, Fire, Earth, Metal or Water. Every technique has an element too.
 *  In a fight the elements move the odds, by the old cycles:
 *  - Generating (Sheng): Wood feeds Fire, Fire makes Earth (ash), Earth bears Metal, Metal holds Water, Water nourishes Wood.
 *  - Overcoming (Ke): Wood parts Earth, Earth absorbs Water, Water quenches Fire, Fire melts Metal, Metal chops Wood.
 *  What counts is a fighter's martial technique (two thirds) and movement technique (one third) against the other's path, and how well each technique suits its own owner's path. A technique without an element of its own
 *  (the Default Technique everyone starts with) follows its owner's path, and so does the natural attack of a beast. All numbers are percentage points on the chance to win (one point of fighting power is 15).
 *  A path is stored as one small number (`Character.path`: the element, a word for "Path/Way/Dao/Art" and two words that name it); old saves and every creature without one get a path from their id (never from the
 *  game's random numbers, so adding paths changed nothing else in a world). */
final class Elements {
    private Elements() { }

    static final int WOOD = 0, FIRE = 1, EARTH = 2, METAL = 3, WATER = 4;
    static final String[] NAMES = {"Wood", "Fire", "Earth", "Metal", "Water"};

    // ---- the cycles: element i feeds (i+1) and overcomes (i+2)
    static boolean feeds(int a, int b) { return a >= 0 && b >= 0 && (a + 1) % 5 == b; }
    static boolean overcomes(int a, int b) { return a >= 0 && b >= 0 && (a + 2) % 5 == b; }

    private static final String[] SHENG = {"Wood feeds Fire", "Fire makes Earth", "Earth bears Metal", "Metal holds Water", "Water nourishes Wood"};  // by the element that feeds
    private static final String[] KE_BY_ELEM = {"Wood parts Earth", "Fire melts Metal", "Earth absorbs Water", "Metal chops Wood", "Water quenches Fire"};

    /** The words of the cycle between two elements, from a's side ("Fire melts Metal", "Metal chops Wood", "Wood feeds Fire", ...), or "" for the same element. */
    static String relation(int a, int b) {
        if (a < 0 || b < 0 || a == b) return "";
        if (overcomes(a, b)) return KE_BY_ELEM[a];
        if (overcomes(b, a)) return KE_BY_ELEM[b];
        if (feeds(a, b)) return SHENG[a];
        return SHENG[b];
    }

    // ---- paths
    private static final String[] PREFIX = {"Path", "Way", "Dao", "Art"};
    private static final String[][] ADJ = {
            {"Verdant", "Quiet", "Ancient", "Whispering", "Thorned", "Sleeping", "Rising"},
            {"Crimson", "Burning", "Restless", "Dawn", "Ashen", "Roaring", "Golden"},
            {"Silent", "Deep", "Unmoving", "Ochre", "Patient", "Heavy", "Old"},
            {"Tempered", "Silver", "Iron", "Bright", "Cold", "Unbroken", "Gilded"},
            {"Flowing", "Still", "Drifting", "Moonlit", "Deep-Running", "Veiled", "Winter"}};
    private static final String[][] NOUN = {
            {"Willow", "Bamboo", "Pine", "Vine", "Orchid", "Root", "Forest"},
            {"Ember", "Flame", "Lantern", "Phoenix", "Torch", "Pyre", "Sun"},
            {"Mountain", "Stone", "Clay", "Valley", "Mesa", "Cavern", "Boulder"},
            {"Blade", "Needle", "Bell", "Mirror", "Anvil", "Coin", "Spear"},
            {"River", "Tide", "Mist", "Lake", "Ripple", "Spring", "Snow"}};
    /** Words for the names of techniques, by element. */
    static final String[][] TECH_WORDS = {
            {"Verdant", "Bamboo", "Willow", "Pine", "Thorn", "Vine", "Orchid", "Cedar"},
            {"Flame", "Ember", "Crimson", "Blaze", "Cinder", "Scarlet", "Ash", "Sun"},
            {"Stone", "Mountain", "Clay", "Granite", "Dust", "Boulder", "Ochre", "Cavern"},
            {"Iron", "Steel", "Silver", "Bronze", "Golden", "Razor", "Copper", "Bell"},
            {"River", "Tide", "Mist", "Rain", "Frost", "Lake", "Ripple", "Moon-Water"}};

    private static int pack(int element, int prefix, int adj, int noun) { return 1 + ((element * 4 + prefix) * 7 + adj) * 7 + noun; }

    /** A path number out of a Random: the element (or -1 for any), and the words. */
    static short roll(int element, Random r) {
        int e = element >= 0 ? element : r.nextInt(5);
        return (short) pack(e, r.nextInt(4), r.nextInt(7), r.nextInt(7));
    }

    /** Gives a creature a path out of a Random: the element (or -1 for any), and the words. */
    static void assign(Character c, int element, Random r) { c.path = roll(element, r); }

    private static Random seeded(Character c, long salt) { return new Random(c.id * 2654435761L + salt); }

    /** The path number, given from the id for anyone who has none yet (old saves, creatures made without one). */
    static int code(Character c) {
        if (c.path <= 0) assign(c, -1, seeded(c, 17));
        return c.path;
    }

    /** The element of a creature's path, 0-4. */
    static int of(Character c) {
        if (c == null) return 0;
        return (code(c) - 1) / 196;
    }

    static String name(int element) { return element >= 0 && element < 5 ? NAMES[element] : "?"; }

    /** The words of a path from its parts. */
    static String words(int e, int prefix, int adj, int noun) { return PREFIX[prefix] + " of the " + ADJ[e][adj] + " " + NOUN[e][noun]; }

    /** "Way of the Silent Mountain". */
    static String pathName(Character c) {
        int k = code(c) - 1;
        int noun = k % 7, adj = (k / 7) % 7, prefix = (k / 49) % 4, e = k / 196;
        return words(e, prefix, adj, noun);
    }

    /** "Way of the Silent Mountain (Earth)". */
    static String describe(Character c) { return pathName(c) + " (" + name(of(c)) + ")"; }

    /** A child follows a parent's element half the time (the words are its own). */
    static void inherit(Character child, Character mother, Character father) {
        Random r = seeded(child, 31);
        Character parent = father != null && r.nextBoolean() ? father : mother;
        int e = r.nextInt(100) < 50 ? of(parent) : -1;
        assign(child, e, r);
    }

    // ---- techniques
    /** The element a new technique takes: its maker's own half the time, else any. */
    static int forNewTechnique(Character maker) {
        Random r = GameDriver.rand;
        return maker != null && r.nextInt(100) < 55 ? of(maker) : r.nextInt(5);
    }

    /** The element a technique counts as in this owner's hands: its own, or the owner's path for a technique that has none (the Default Technique). */
    static int techElem(Technique t, Character owner) {
        int e = t == null ? -1 : t.element();
        return e >= 0 ? e : of(owner);
    }

    // ---- the odds
    /** A technique of element te against a creature whose path is pe (percentage points for the technique's user). */
    static int against(int te, int pe) {
        if (overcomes(te, pe)) return 8;
        if (overcomes(pe, te)) return -8;
        if (feeds(te, pe)) return -3;   // the technique feeds the enemy
        if (feeds(pe, te)) return 3;    // the technique drains the enemy
        return 0;
    }

    /** A technique of element te in the hands of an owner whose path is se. */
    static int suits(int te, int se) {
        if (te == se) return 5;
        if (feeds(te, se)) return 3;
        if (feeds(se, te)) return 2;
        if (overcomes(te, se)) return -6;
        if (overcomes(se, te)) return -4;
        return 0;
    }

    /** How much the elements help `a` against `b`, in points of the chance to win: the martial technique counts twice as much as the movement technique (two thirds and one third). A beast has no technique of its
     *  own: its natural attack is its path's element. */
    static double edgeValue(Character a, Character b) {
        int pb = of(b), pa = of(a);
        if (a.trait == Character.Traits.Beast || a.trait == Character.Traits.Monster) {
            Technique t = a.martialTechnique;
            int te = techElem(t, a);
            return against(te, pb) + (t != null && t.element() >= 0 ? suits(te, pa) : 0);
        }
        int me = techElem(a.martialTechnique, a), ve = techElem(a.movementTechnique, a);
        return (2.0 * (against(me, pb) + suits(me, pa)) + (against(ve, pb) + suits(ve, pa))) / 3.0;
    }

    static int edge(Character a, Character b) { return (int) Math.round(edgeValue(a, b)); }

    /** The elements' push on `a`'s chance to win against `b` (positive: in a's favour), in percentage points. */
    static int swing(Character a, Character b) { return a == null || b == null ? 0 : (int) Math.round(edgeValue(a, b) - edgeValue(b, a)); }

    /** The worth, in points of the chance to win, of using technique `t` as one of the two it fights with against `b`: its level (a point of technique level is a fifth of a point of power, 3 points of the chance) plus what its
     *  element gives (`weight`: two thirds for the martial technique, one third for the movement technique). */
    private static double worth(Technique t, Character a, Character b, double weight) {
        int te = techElem(t, a);
        return 3.0 * t.lvl + weight * (against(te, of(b)) + suits(te, of(a)));
    }

    /** Before a fight, a fighter who is not steered by a person's hand fights with whichever of his two martial techniques (and two movement techniques) is worth more against this opponent: the one with the higher level, or the
     *  one whose element beats the opponent's path by more than the level it gives up. The slot that is used is swapped with the second one for this fight (the next `equip` puts the strongest back) and the fighting level follows. */
    static void chooseTechniques(Character a, Character b) {
        if (a == null || b == null || (a.player == 1 && a.isManualPc()) || a.trait == Character.Traits.Beast || a.trait == Character.Traits.Monster) return;
        boolean swapped = false;
        Technique m1 = a.martialTechnique, m2 = a.martialTechnique2;
        if (m1 != null && m2 != null && worth(m2, a, b, 2.0 / 3.0) > worth(m1, a, b, 2.0 / 3.0) + 3.0) { a.martialTechnique = m2; a.martialTechnique2 = m1; swapped = true; }
        Technique v1 = a.movementTechnique, v2 = a.movementTechnique2;
        if (v1 != null && v2 != null && worth(v2, a, b, 1.0 / 3.0) > worth(v1, a, b, 1.0 / 3.0) + 3.0) { a.movementTechnique = v2; a.movementTechnique2 = v1; swapped = true; }
        if (swapped) a.tempLvl = a.tempLvl - (int) (rawFight(a, m1, v1) / 5) + (int) (rawFight(a, a.martialTechnique, a.movementTechnique) / 5);
        if (swapped) chosenSwaps++;
    }

    static volatile int chosenSwaps = 0;   // fights in which a technique was swapped for its element (tests, the playtest tools)

    /** The fighting rating before the division by five, with the given techniques in place of the ones in use. */
    private static long rawFight(Character c, Technique martial, Technique movement) {
        long raw = c.lvl;
        if (c.equippedClothing != null) raw += c.equippedClothing.lvl;
        if (c.equippedWeapon != null) raw += c.equippedWeapon.lvl + c.equippedWeapon.enchantBonus;
        if (martial != null) raw += martial.lvl;
        if (movement != null) raw += movement.lvl;
        return raw;
    }

    /** One line for the fight report and the preview, from a's side. */
    static String note(Character a, Character b, boolean youAreA) {
        int sw = swing(a, b);
        int ea = of(a), eb = of(b);
        String who = youAreA ? "your" : a.name + "'s";
        String them = youAreA ? b.name + "'s" : "their";
        String rel = relation(ea, eb);
        return "Elements: " + who + " " + name(ea) + " path against " + them + " " + name(eb) + (rel.isEmpty() ? " (the same element)" : " (" + rel + ")") + ": "
                + (sw == 0 ? "no change" : (sw > 0 ? "+" : "") + sw + " points on " + (youAreA ? "your" : "the first fighter's") + " chance to win") + ".";
    }

    /** Colours for the interface (RGB). */
    static final int[] COLOR = {0x4CAF6A, 0xE0623A, 0xC29A4A, 0xB8C4CE, 0x4E8FD6};
}
