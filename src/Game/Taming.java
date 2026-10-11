package Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Beast taming. A cultivator may keep at most (major level + 1) tamed beasts. A tamed beast is a real character in `Map.characters` (so it
 *  cultivates and levels like any beast) but it has no turn of its own: it stands in its master's tile every week (see `follow`), is listed in the
 *  master's party, and adds to the master's fighting level. When the master dies or reincarnates the beasts go back to the wild. */
final class Taming {
    private Taming() { }

    private static final Random rand = Long.getLong("tdf.seed") != null ? new Random(Long.getLong("tdf.seed") + 2) : new Random();

    /** A Beast Tamer may keep Major Lvl + 1 companions; everybody else may keep one. */
    static volatile int keptCount = 0;     // beasts people keep right now (recounted every year, +1 for each one tamed)
    static final int NPC_SHARE = 12;       // percent of the people: past it, a person who is not a Beast Tamer wins no new beast over (a world where half of all people kept one filled the memory)
    static boolean roomForAnother() { return keptCount * 100L < (long) Map.population * NPC_SHARE; }

    static int cap(Character c) { return c.trait == Character.Traits.Tamer ? c.majorLvl + 1 : 1; }

    /** charById that also sees characters born (or tamed) this tick and not yet merged into the list. */
    static Character byId(int id) {
        Character x = Character.charById(id);
        if (x != null || id < Map.characters.size()) return x;
        synchronized (Map.class) { for (Character t : new ArrayList<>(Map.tempCharacters)) if (t != null && t.id == id) return t; }
        return null;
    }

    /** The living companions of `c` without touching any state (for the GUI thread). */
    static List<Character> peek(Character c) {
        List<Character> out = new ArrayList<>();
        if (c.beastIds == null) return out;
        for (Integer id : new ArrayList<>(c.beastIds)) { Character b = Character.charById(id); if (b != null && !b.dead && b.masterId == c.id) out.add(b); }
        return out;
    }

    /** The living companions of `c` (also forgets the ones that are gone). */
    static List<Character> beasts(Character c) {
        List<Character> out = new ArrayList<>();
        if (c.beastIds == null || c.beastIds.isEmpty()) return out;
        for (int i = c.beastIds.size() - 1; i >= 0; i--) {
            Character b = byId(c.beastIds.get(i));
            if (b == null || b.dead || b.masterId != c.id) c.beastIds.remove(i); else out.add(0, b);
        }
        return out;
    }

    /** Added to the master's fighting level: each companion adds a tenth of its own level (never more than the master's). */
    static int bonus(Character c) {
        if (c.beastIds == null || c.beastIds.isEmpty()) return 0;
        int sum = 0;
        for (Character b : beasts(c)) sum += Math.min(b.lvl, Math.max(1, c.lvl)) / 10;
        return sum;
    }

    /** One attempt at taming a beast in this tile. Returns true if a beast was tamed. Prints the story for players. */
    static boolean attempt(Character c) {
        String why = tame(c);
        return why == null;
    }

    /** null = tamed; otherwise the reason (or the story of the failure). Uses up a beast of the tile either way. */
    static String tame(Character c) {
        if (c.age < 8 || c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) return "Only a cultivator can tame a beast.";
        if (c.location == null || c.location.monsters <= 0) return "There are no beasts here to tame.";
        if (beasts(c).size() >= cap(c)) return "You already keep " + beasts(c).size() + " beast" + (beasts(c).size() == 1 ? "" : "s") + ", the most you can hold (" + cap(c) + (c.trait == Character.Traits.Tamer ? ": Major Lvl + 1" : "; only a Beast Tamer keeps more") + ").";
        Area here = c.location;
        here.monsters--;
        Character beast = new Character(here, c);
        if (beast.lvl > c.lvl + 5) {
            here.monsters++; // it shrugs you off and wanders away
            return "A beast here (Lv." + beast.lvl + ") is far too strong for you (Lv." + c.lvl + ") to tame.";
        }
        boolean flute = Life.bestInstrument(c) != null;
        int chance = 35 + 3 * (c.lvl - beast.lvl) + (c.trait == Character.Traits.Tamer ? 30 : 0) + (flute ? 10 : 0) + (c.trait == Character.Traits.Friendly ? 10 : 0);
        chance = Math.max(5, Math.min(95, chance));
        if (rand.nextInt(100) < chance) {
            enlist(c, beast);
            if (c.player > 0 && c.skipping <= 0) System.out.println(beast.name + " " + beast.lastName + " (Lv." + beast.lvl + ") accepts you as its master! (" + beasts(c).size() + "/" + cap(c) + ")");
            if (c.player > 0) RunStats.onTamed();
            if (c.player > 0 || beast.lvl >= 25) Chronicle.add("world", c.name + " " + c.lastName + " tamed " + beast.name + " " + beast.lastName + " (Lv." + beast.lvl + ").", c.id);
            return null;
        }
        if (rand.nextBoolean()) { // it turns on you
            c.battle(beast);
            return "The " + beast.lastName + " refused and attacked!";
        }
        return "The " + beast.lastName + " would not be tamed and slipped away.";
    }

    /** A named companion of a story (a legendary beast of the Tamer's Daughter): made like a tamed beast of this tile, at the given level and name, and kept whatever the cap says. */
    static Character legend(Character c, String name, String species, int lvl) {
        Character beast = new Character(c.location, c);
        beast.lvl = Math.max(1, lvl); beast.majorLvl = beast.lvl / 10; beast.minorLvl = beast.lvl % 10; beast.tempLvl = beast.lvl;
        beast.potential = Math.max(beast.potential, 85);
        beast.cultivationBaseMax = Math.pow(beast.lvl, 3) * (((70. + 1. / 3.) + ((11. / 6.) * beast.lvl)) / 25.);
        beast.cultivation = (Math.pow(beast.potential, 2) * Math.sqrt(beast.lvl)) / (52. * Math.sqrt(beast.potential));
        enlist(c, beast);
        beast.name = name; beast.lastName = species;
        if (c.player > 0 && c.skipping <= 0) System.out.println(name + " the " + species + " (Lv." + beast.lvl + ") is yours.");
        return beast;
    }

    private static void enlist(Character c, Character beast) {
        beast.name = Naming.petName(beast);
        beast.id = GameDriver.nextUid();
        beast.wasTamed = true;
        beast.born = true;
        beast.time = 0;
        beast.masterId = c.id;
        beast.partyLeaderId = c.id;
        beast.affiliation = c.affiliation;
        beast.affiliationNum = c.affiliationNum;
        beast.location = c.location;
        beast.knownLocations.add(c.location);
        if (c.beastIds == null) c.beastIds = new ArrayList<>();
        c.beastIds.add(beast.id);
        keptCount++;
        Map.addPopulation(-1);     // beasts are not counted in the population (the births merge adds one back)
        Map.addTemp(beast);
    }

    /** A tamed beast joins its master's tile (it is never listed in the tile, so nobody can attack it there). */
    static void follow(Character beast) {
        Character m = beast.masterId >= 0 ? byId(beast.masterId) : null;
        if (m == null || m.dead) { returnToWild(beast); return; }
        if (m.location != null) beast.location = m.location;
        beast.partyLeaderId = m.id;
    }

    static void returnToWild(Character beast) {
        if (beast == null) return;
        Character m = beast.masterId >= 0 ? byId(beast.masterId) : null;
        if (m != null && m.beastIds != null) m.beastIds.remove(Integer.valueOf(beast.id));
        beast.masterId = -1; beast.partyLeaderId = -1;
        if (beast.location != null) beast.location.monsters++;
        remove(beast);
        beast.dead = true;
    }

    // ------------------------------------------------------------------ beasts that remember a reincarnated master
    /** The master's soul is being reborn: the companions stay where they are, with no turn of their own, and wait for it (not returned to the wild). */
    static void waitFor(Character beast, int soul) {
        beast.masterId = -1; beast.partyLeaderId = -1; beast.waitingSoul = soul;
    }

    /** A waiting beast of `c`'s soul standing in `area` (or anywhere when area is null), else null. */
    static Character lostBeastAt(Character c, Area area) {
        if (c.lostBeastIds == null) return null;
        for (Integer id : new ArrayList<>(c.lostBeastIds)) {
            Character b = byId(id);
            if (b != null && !b.dead && b.waitingSoul >= 0 && b.waitingSoul == c.soulId && (area == null || b.location == area)) return b;
        }
        return null;
    }

    /** The waiting beasts in this tile recognise their master and rejoin (as many as the cap allows). Returns how many came back. */
    static int reunite(Character c) {
        if (c.lostBeastIds == null || c.location == null) return 0;
        int back = 0;
        for (Integer id : new ArrayList<>(c.lostBeastIds)) {
            Character b = byId(id);
            if (b == null || b.dead || b.waitingSoul != c.soulId) { c.lostBeastIds.remove(id); continue; }
            if (b.location != c.location) continue;
            if (beasts(c).size() >= cap(c)) break;
            b.waitingSoul = -1; b.masterId = c.id; b.partyLeaderId = c.id;
            b.affiliation = c.affiliation; b.affiliationNum = c.affiliationNum;
            if (c.beastIds == null) c.beastIds = new ArrayList<>();
            c.beastIds.add(b.id);
            c.lostBeastIds.remove(id);
            back++;
            if (c.player > 0 && c.skipping <= 0) System.out.println(b.name + " " + b.lastName + " (Lv." + b.lvl + ") knows you by your soul and comes to your side again! (" + beasts(c).size() + "/" + cap(c) + ")");
        }
        return back;
    }

    /** Posts "Find Lost Beast" for the nearest waiting beast if there is room for it and no such quest is pending (a Tamer cares more). */
    static void postLostQuest(Character c) {
        if (c.lostBeastIds == null || c.lostBeastIds.isEmpty() || beasts(c).size() >= cap(c)) return;
        for (Quest q : c.targetQuests) if ("Find Lost Beast".equals(q.name)) return;
        if (c.currentQuest != null && "Find Lost Beast".equals(c.currentQuest.name)) return;
        Character best = null; int bd = Integer.MAX_VALUE;
        for (Integer id : new ArrayList<>(c.lostBeastIds)) {
            Character b = byId(id);
            if (b == null || b.dead || b.waitingSoul != c.soulId) { c.lostBeastIds.remove(id); continue; }
            int d = b.location == null || c.location == null ? 9999 : PlayerControl.distance(c.location, b.location);
            if (d < bd) { bd = d; best = b; }
        }
        if (best == null || best.location == null) return;
        c.targetQuests.add(new Quest("Find Lost Beast", true, c.trait == Character.Traits.Tamer ? 80 : 50, 0, c.trait == Character.Traits.Tamer ? 80 : 50, best.location));
    }

    /** The master dies (or leaves this life): the beasts go back to the wild. */
    static void onMasterGone(Character master) {
        if (master.beastIds == null || master.beastIds.isEmpty()) return;
        for (Character b : beasts(master)) returnToWild(b);
        master.beastIds.clear();
    }

    /** A tamed beast dies (old age, a fight). */
    static void onBeastDeath(Character beast) {
        Character m = beast.masterId >= 0 ? byId(beast.masterId) : null;
        if (m != null) {
            if (m.beastIds != null) m.beastIds.remove(Integer.valueOf(beast.id));
            if (m.player > 0) System.out.println("Your companion " + beast.name + " " + beast.lastName + " has died.");
        }
        remove(beast);
        beast.masterId = -1; beast.partyLeaderId = -1;
    }

    /** Takes a tamed beast out of the world's list (or out of this tick's pending list). */
    private static void remove(Character beast) {
        synchronized (Map.class) {
            if (Map.tempCharacters.contains(beast)) { Map.addRem(beast); return; } // still pending this tick: it must keep its list slot (id == index); the end-of-tick merge clears it
            if (beast.id >= 0 && beast.id < Map.characters.size() && Map.characters.get(beast.id) == beast) Map.characters.set(beast.id, null);
        }
    }

    /** Frees companion number n (1-based) of the master. */
    static String release(Character c, int n) {
        List<Character> bs = beasts(c);
        if (n < 1 || n > bs.size()) return "No companion #" + n + ". See 'companions'.";
        Character b = bs.get(n - 1);
        String nm = b.name + " " + b.lastName;
        returnToWild(b);
        return nm + " returns to the wild.";
    }

    /** Feeds companion n a portion of food: it cultivates for as many weeks as the food's level. */
    static String feed(Character c, int n) {
        List<Character> bs = beasts(c);
        if (n < 1 || n > bs.size()) return "No companion #" + n + ". See 'companions'.";
        Item food = Life.firstOf(c, Item.ItemType.Food);
        if (food == null) return "You have no food to give.";
        return give(c, bs.get(n - 1), food);
    }

    /** One portion of `food` from the master's goods goes to the companion: it cultivates for as many weeks as the food's level. */
    private static String give(Character c, Character b, Item food) {
        food.quantity--;
        if (food.quantity <= 0) Life.goods(c).remove(food);
        for (int i = 0; i < Math.max(1, food.lvl); i++) b.cultivate();
        return b.name + " " + b.lastName + " eats " + food.name + " and grows (Lv." + b.lvl + ").";
    }

    static final int KEEP_FOR_SELF = 3;   // "leftovers" feeds a companion only with the food beyond this many portions
    static final int FEED_EVERY = 4;      // weeks between two portions for one companion

    /** The cheapest food the master can spare when he keeps KEEP_FOR_SELF portions for himself (null if there is none to spare). */
    private static Item spareFood(Character c) {
        if (c.goods == null) return null;
        int held = 0; Item low = null;
        for (Item it : c.goods) if (it.itemType == Item.ItemType.Food && it.quantity > 0) { held += it.quantity; if (low == null || it.lvl < low.lvl) low = it; }
        return held > KEEP_FOR_SELF ? low : null;
    }

    /** The feed policy of a character who is not at the keys (AUTO, or a script): each companion gets a portion every FEED_EVERY weeks. Returns the number fed. */
    static int autoFeed(Character c) {
        PlayerControl pc = c.pc;
        if (pc == null || c.player < 1 || c.beastIds == null || c.beastIds.isEmpty() || pc.feedPolicy < 0 || pc.feedPolicy >= 2) return 0;
        if (GameDriver.time % FEED_EVERY != Math.floorMod(c.id, FEED_EVERY)) return 0;
        int fed = 0;
        for (Character b : beasts(c)) {
            Item food = pc.feedPolicy == 1 ? Life.firstOf(c, Item.ItemType.Food) : spareFood(c);
            if (food == null) break;
            String msg = give(c, b, food);
            fed++;
            if (c.skipping <= 0) System.out.println(msg);
        }
        return fed;
    }

    static String describe(Character c) {
        List<Character> bs = beasts(c);
        StringBuilder sb = new StringBuilder("=== Companions (" + bs.size() + "/" + cap(c) + ") ===\n");
        int i = 1;
        for (Character b : bs) sb.append(String.format("%2d. %s %s  Lv.%d  potential %d  age %d  +%d to your fighting level%n", i++, b.name, b.lastName, b.lvl, b.potential, b.age, Math.min(b.lvl, Math.max(1, c.lvl)) / 10));
        if (bs.isEmpty()) sb.append("  (none) — 'tame' tries a beast of this tile.\n");
        if (c.lostBeastIds != null) { int w = 0; for (Integer id : c.lostBeastIds) { Character b = byId(id); if (b != null && !b.dead && b.waitingSoul >= 0) { if (w++ == 0) sb.append("Waiting for you (they remember your soul):\n"); sb.append("  ").append(b.name).append(" ").append(b.lastName).append(" Lv.").append(b.lvl).append(" at ").append(b.location == null ? "?" : b.location.name).append(" ").append(PlayerControl.coord(b.location)).append("\n"); } } }
        sb.append("You may keep up to ").append(cap(c)).append(c.trait == Character.Traits.Tamer ? " (Major Lvl + 1, as a Beast Tamer)" : " (one; a Beast Tamer keeps Major Lvl + 1)").append(" beasts; they follow you everywhere and fight beside you. 'release <n>', 'feed <n>' (uses food).\n");
        return sb.toString();
    }
}
