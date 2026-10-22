package Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import static Game.GameDriver.rand;

/** What each character feels like doing (2026-10-08). Every quest belongs to one of eight families (adventure, conflict, growth, trade, company, sect and city, meditation, journeys).
 *  A character has a TARGET SHARE of its time for each family that follows from its trait, its stage of life and its affiliation (a Merchant trades, a Friendly one keeps company, a Musician travels,
 *  the young adventure and study, the old meditate and teach). A running average of the time it has really spent on each family (`qEma`) is compared with the target:
 *  a family it has had too much of tires it (its interest falls, down to a tenth, and below four fifths it is put off unless urgent), one it has neglected draws it (up to three and a half times). The interest bends both
 *  how often a quest is thought of (`roll`) and which of the waiting quests is taken next (`pick`). Beasts and monsters, and a human who plays MANUAL, are not steered.
 *  This class also holds the eleven quests that fill the gaps: Visit Family, Teach Disciple, Courtship, Trade Run, Pilgrimage, Meditation Retreat, Tame Beast, Buy Pills, Attend Auction, Found City, Found Sect. */
final class QuestLife {
    private QuestLife() { }

    static final int ADV = 0, FIGHT = 1, GROW = 2, TRADE = 3, SOC = 4, SECT = 5, CULT = 6, JOURN = 7, N = 8;
    static final String[] FAMILY = {"adventure", "conflict", "growth", "trade", "company", "sect and city", "meditation", "journeys"};
    /** The share of its quest time an ordinary adult gives to each family. */
    static final double[] BASE = {0.22, 0.09, 0.13, 0.06, 0.20, 0.09, 0.12, 0.07};

    private static final HashMap<String, Integer> FAM = new HashMap<>();
    private static void fam(int f, String... names) { for (String n : names) FAM.put(n, f); }
    static {
        fam(ADV, "Hunt Beast", "Explore Dungeon", "Heaven Treasure", "Bounty Hunt", "Poison Contract", "Seek Territory", "Hunt Cultivator");
        fam(FIGHT, "Battle Dislike", "Battle Rival", "Battle Arch-Enemy", "Revenge", "Rob Caravan", "Sect Raid", "Demand Tribute", "Recruit Defector");
        fam(GROW, "Buy Gear", "Study Trip", "Seek Potential", "Refine Blood", "Buy Pills", "Sell Technique", "Attend Auction", "Seek Teacher");
        fam(TRADE, "Go Sell", "Restock Trip", "Sell Stock", "Festival Trade", "Trade Run", "Deliver Materials", "Deliver Goods", "Market Trip", "Gathering Trip");
        fam(SOC, "Find Person", "Visit Family", "Courtship", "Teach Disciple", "Gift Technique", "Give Gift", "Get Pregnant", "Find Parent's Body");
        fam(SECT, "Join Sect", "Return to Sect", "Recruit Member", "Propose Alliance", "Donate Array", "Sect Mission", "Found City", "Found Sect");
        fam(CULT, "Meditation Retreat");
        fam(JOURN, "Pilgrimage", "Perform", "Tame Beast", "Find Lost Beast", "Sightseeing", "Courier Run");
    }

    /** The quests this class runs. */
    static final Set<String> OWN = new HashSet<>(Arrays.asList("Give Gift", "Visit Family", "Teach Disciple", "Courtship", "Trade Run", "Pilgrimage", "Meditation Retreat",
            "Tame Beast", "Buy Pills", "Attend Auction", "Found City", "Found Sect", "Market Trip", "Gathering Trip", "Seek Teacher", "Sightseeing", "Courier Run"));
    static boolean owns(String name) { return OWN.contains(name); }

    /** The family of a quest, or -1 for the ones that belong to none (fleeing, ghosts, errands). */
    static int family(String name) {
        if (name == null) return -1;
        int slot = name.hashCode() & (FAM_CACHE.length - 1); // (a String keeps its hash; the quest names in the code are the same objects every time)
        FamEntry e = FAM_CACHE[slot];
        if (e != null && e.name == name) return e.fam;
        Integer f = FAM.get(name);
        int v = f == null ? -1 : f;
        FAM_CACHE[slot] = new FamEntry(name, v);
        return v;
    }
    private static final class FamEntry { final String name; final int fam; FamEntry(String n, int f) { name = n; fam = f; } } // immutable: a racing reader sees a whole entry or none
    private static final FamEntry[] FAM_CACHE = new FamEntry[128];

    private static boolean steered(Character c) { return c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster; }

    // ------------------------------------------------------------------ profiles
    private static void m(double[] t, double... fm) { for (int i = 0; i + 1 < fm.length; i += 2) t[(int) fm[i]] *= fm[i + 1]; }

    /** The target shares of this character (they add up to 1); recomputed once a year and when the trait changes. */
    static double[] target(Character c) {
        int key = (GameDriver.time / 52) * 64 + c.trait.ordinal();
        double[] t = c.qTarget;
        if (t != null && c.qTargetKey == key) return t;
        t = BASE.clone();
        switch (c.trait) {
            case Leader: m(t, SECT, 1.0, SOC, 1.3, ADV, 0.7); break;
            case Blacksmith: case Tailor: m(t, TRADE, 1.6, GROW, 1.2, ADV, 0.6); break;
            case Scribe: case Alchemist: m(t, GROW, 1.2, ADV, 0.7); break;
            case Merchant: m(t, TRADE, 3.0, JOURN, 1.3, FIGHT, 0.5, ADV, 0.5); break;
            case Ghost: m(t, CULT, 1.3, SOC, 0.7); break;
            case Innovative: m(t, GROW, 1.5, CULT, 1.3); break;
            case Gatherer: case Miner: m(t, TRADE, 1.4, ADV, 0.8); break;
            case Friendly: m(t, SOC, 2.2, SECT, 1.2, FIGHT, 0.4); break;
            case Runner: m(t, JOURN, 2.0, ADV, 1.2); break;
            case Cheater: m(t, ADV, 1.5, GROW, 1.2); break;
            case Lucky: m(t, ADV, 1.2, FIGHT, 1.2); break;
            case IronBody: m(t, ADV, 1.4, FIGHT, 1.3); break;
            case SpiritVein: m(t, CULT, 2.2); break;
            case HeavenlyRoot: m(t, CULT, 1.8, GROW, 1.2); break;
            case Tamer: m(t, JOURN, 1.8, ADV, 1.2); break;
            case Poisoner: m(t, FIGHT, 1.6, TRADE, 1.2); break;
            case Drunken: m(t, SOC, 1.6, JOURN, 1.5, GROW, 0.6); break;
            case Chef: m(t, SOC, 1.6, TRADE, 1.5); break;
            case Musician: m(t, JOURN, 2.5, SOC, 1.4); break;
            default: break;
        }
        boolean young = c.age < 30, elder = c.age >= 0.6 * Math.max(c.maxAge, 60);
        if (young) m(t, ADV, 1.4, GROW, 1.5, FIGHT, 1.2, SECT, 0.7, CULT, 0.8, TRADE, 0.6);
        else if (elder) m(t, ADV, 0.5, FIGHT, 0.4, SOC, 1.5, CULT, 1.8, JOURN, 1.4, SECT, 1.3, GROW, 0.7);
        if ("Evil".equals(c.affiliation)) m(t, FIGHT, 1.6, SOC, 0.7);
        else if ("Good".equals(c.affiliation)) m(t, SOC, 1.2, SECT, 1.2);
        double sum = 0;
        for (double v : t) sum += v;
        for (int i = 0; i < N; i++) t[i] /= sum;
        c.qTarget = t; c.qTargetKey = key;
        return t;
    }

    /** Once a turn, from `Character.quest()`: the time on the current quest's family goes into the running average (it forgets with a memory of about a year and a quarter). */
    static void tick(Character c) {
        if (!steered(c)) return;
        double[] t = target(c);
        if (c.qEma == null) { c.qEma = t.clone(); for (int i = 0; i < N; i++) c.qEma[i] *= 0.9; } // a new character starts as it is meant to be
        int f = c.currentQuest == null ? -1 : family(c.currentQuest.name);
        for (int i = 0; i < N; i++) c.qEma[i] = c.qEma[i] * 0.985 + (i == f ? 0.015 : 0);
    }

    /** How much this character feels like a quest of family `f` right now: 1 = as much as it should have, below 1 = tired of it, above 1 = it has been neglected. */
    static double interest(Character c, int f) {
        if (f < 0 || !steered(c) || c.qEma == null) return 1;
        double[] t = target(c), e = c.qEma;
        double sum = 0;
        for (double v : e) sum += v;
        double share = e[f] / Math.max(0.4, sum);
        double ratio = t[f] / Math.max(0.03, share);
        return Math.max(0.1, Math.min(3.5, ratio * Math.sqrt(ratio))); // ratio to the power 1.5: a family that has had a fifth too much is already unwelcome
    }

    /** `rand.nextInt(odds) == 0`, with the odds bent by the interest in the quest's family. */
    static boolean roll(Character c, String quest, int odds) {
        double in = interest(c, family(quest));
        double eff = in < 1 ? in * in : in; // tiredness bites harder than hunger helps
        return rand.nextInt((int) Math.max(1, Math.round(odds / eff / c.trigK))) == 0; // (trigK: see Character.quest)
    }

    /** A gate that can only close: an urgent quest is still thought of, unless the character is tired of that family (then now and then it is not). */
    static boolean pass(Character c, String quest) {
        double in = interest(c, family(quest));
        return in >= 1 || rand.nextDouble() < in;
    }

    /** The next quest from a queue: the highest priority, bent by the interest in each quest's family (30 points per e-fold), so a tired family waits and a neglected one jumps the queue. */
    static Quest pick(Character c, PriorityQueue<Quest> queue) {
        if (queue.isEmpty()) return null;
        if (!steered(c) || c.qEma == null) return queue.poll();
        Quest best = null;
        double bestScore = -1e18;
        for (Quest q : queue) {
            double in = interest(c, family(q.name));
            if (in < 0.8 && q.maxPriority < 80) continue; // tired of that kind of thing and it is not a matter of life and death: it waits (the character does something else, or nothing)
            double s = q.priority + 30 * Math.log(in);
            if (s > bestScore) { bestScore = s; best = q; }
        }
        if (best != null) queue.remove(best);
        return best;
    }

    // ------------------------------------------------------------------ new quests: what makes a character think of them
    private static void say(Character c, String text) { if (c.player > 0 && c.skipping <= 0) System.out.println(text); }

    private static boolean ready(Character c, Character t) { return t != null && !t.dead && t.born && t.location != null; }

    /** The routine quests a character may think of, in the order of this table (the order the turn starts from is random, so the later ones are not starved). */
    private static final String[] ROUTINE = {"Meditation Retreat", "Visit Family", "Courtship", "Teach Disciple", "Trade Run", "Buy Pills", "Attend Auction", "Pilgrimage", "Tame Beast",
            "Market Trip", "Gathering Trip", "Seek Teacher", "Sightseeing", "Courier Run", "Found City"};
    private static final int[] ODDS = {50, 35, 50, 30, 18, 20, 30, 40, 40, 25, 30, 30, 25, 15, 90};

    /** Once a turn (not for a human who plays MANUAL): maybe a character thinks of one of the routine quests. At most one a turn, and a short rest after each. */
    static void consider(Character c) {
        if (c.age < 12 || !steered(c) || c.isGhostForm || c.location == null || c.isManualPc()) return;
        if (GameDriver.time < c.nextRoutineWeek || c.targetQuests.size() >= 14) return;
        if (c.currentQuest != null && c.currentQuest.progress > 0) return; // in the middle of a retreat, a pilgrimage or an auction
        boolean added = false;
        if (c.location.spiritualVein && roll(c, "Meditation Retreat", 14) && c.findQueuedQuest("Meditation Retreat") == null) { // standing on a spirit vein, it is hard not to sit down
            c.addTarget(new Quest("Meditation Retreat", true, 48, 3, 62, c.location));
            added = true;
        }
        int n = ROUTINE.length, off = rand.nextInt(n);
        for (int k = 0; k < n && !added; k++) {
            int i = (k + off) % n;
            String name = ROUTINE[i];
            if (!roll(c, name, ODDS[i])) continue;
            if (c.findQueuedQuest(name) != null || ("Found City".equals(name) && c.findQueuedQuest("Found Sect") != null)) continue;
            switch (name) {
                case "Meditation Retreat": added = retreat(c); break;
                case "Visit Family": added = visitFamily(c); break;
                case "Courtship": added = courtship(c); break;
                case "Teach Disciple": added = teach(c); break;
                case "Trade Run": added = tradeRun(c); break;
                case "Buy Pills": added = buyPills(c); break;
                case "Attend Auction": added = attendAuction(c); break;
                case "Pilgrimage": added = pilgrimage(c); break;
                case "Tame Beast": added = tameBeast(c); break;
                case "Market Trip": added = marketTrip(c); break;
                case "Gathering Trip": added = gatheringTrip(c); break;
                case "Seek Teacher": added = seekTeacher(c); break;
                case "Sightseeing": added = sightseeing(c); break;
                case "Courier Run": added = courier(c); break;
                default: added = found(c); break;
            }
        }
        if (added) c.nextRoutineWeek = GameDriver.time + 1;
    }

    private static boolean retreat(Character c) {
        Area best = null;
        int bestD = Integer.MAX_VALUE;
        for (Area a : c.knownLocations) {
            if (a == null || !a.spiritualVein) continue;
            int d = PlayerControl.distance(c.location, a);
            if (d <= 30 && d < bestD) { bestD = d; best = a; }
        }
        if (best == null) return false; // no vein known: the old one-turn Meditate does for now
        c.addTarget(new Quest("Meditation Retreat", true, 40, 3, 60, best));
        return true;
    }

    private static void addKin(List<Character> l, Character k) { if (k != null && !k.dead && k.born && k.location != null && !l.contains(k)) l.add(k); }

    private static boolean visitFamily(Character c) {
        ArrayList<Character> kin = new ArrayList<>();
        addKin(kin, c.spouse); addKin(kin, c.father); addKin(kin, c.mother);
        if (c.children != null) for (Character k : new ArrayList<>(c.children)) addKin(kin, k);
        if (c.siblings != null) for (Character k : new ArrayList<>(c.siblings)) addKin(kin, k);
        kin.removeIf(k -> k.age < 8 || k.location == c.location || PlayerControl.distance(c.location, k.location) > 30 || k.trait == Character.Traits.Beast || k.trait == Character.Traits.Monster);
        if (kin.isEmpty()) return false;
        c.addTarget(new Quest("Visit Family", true, 28, 2, 50, kin.get(rand.nextInt(kin.size()))));
        return true;
    }

    private static boolean teach(Character c) {
        if (c.age < 25 || c.majorLvl < 1) return false;
        ArrayList<Character> pupils = new ArrayList<>();
        if (c.sect != null && c.sect.members != null && !c.sect.members.isEmpty()) {
            int n = c.sect.members.size();
            for (int i = 0; i < 8; i++) { try { pupils.add(c.sect.members.get(rand.nextInt(n))); } catch (RuntimeException e) { break; } }
        }
        if (c.children != null) pupils.addAll(c.children);
        for (Relationship r : new ArrayList<>(c.relationshipList.relationships)) if (r.value >= 1 && !r.refused) pupils.add(Character.charById(r.friendID)); // a younger friend asks to be taught
        pupils.removeIf(p -> p == null || p == c || p.player == 1 || !ready(c, p) || p.age < 8 || p.lvl + 3 >= c.lvl || PlayerControl.distance(c.location, p.location) > 30
                || p.trait == Character.Traits.Beast || p.trait == Character.Traits.Monster);
        if (pupils.isEmpty()) return false;
        c.addTarget(new Quest("Teach Disciple", true, 30, 2, 52, pupils.get(rand.nextInt(pupils.size()))));
        return true;
    }

    /** Someone `c` could court: an unmarried adult of the other gender, not kin, not far in age. */
    static boolean suitor(Character c, Character t) {
        if (t == null || t == c || t.dead || !t.born || t.age < 18 || t.spouse != null || t.gender == null || t.gender.equals(c.gender)) return false;
        if (t.player == 1 && c.player != 1) return false; // nobody courts a human on their own account (the human is never asked out of the blue)
        if (t.trait == Character.Traits.Beast || t.trait == Character.Traits.Monster) return false;
        if (c.mother == t || c.father == t || t.mother == c || t.father == c || (c.siblings != null && c.siblings.contains(t)) || (c.children != null && c.children.contains(t))) return false;
        return Math.abs(t.age - c.age) <= 40 && t.location != null;
    }

    private static boolean courtship(Character c) {
        if (c.spouse != null || c.age < 18 || c.age > 0.75 * Math.max(c.maxAge, 60)) return false;
        Character best = null;
        int bv = -1;
        for (Relationship r : new ArrayList<>(c.relationshipList.relationships)) {
            Character t = Character.charById(r.friendID);
            if (r.refused || r.value < 0 || !suitor(c, t) || PlayerControl.distance(c.location, t.location) > 30) continue;
            if (r.value > bv) { bv = r.value; best = t; }
        }
        if (best == null && c.location.characters != null && !c.location.characters.isEmpty()) { // nobody they know: someone who stands where they stand
            for (int i = 0; i < 12 && best == null; i++) {
                Character t = null;
                try { t = c.location.characters.get(rand.nextInt(c.location.characters.size())); } catch (RuntimeException e) { break; }
                if (suitor(c, t) && t.location == c.location) best = t;
            }
        }
        if (best == null) return false;
        c.addTarget(new Quest("Courtship", true, 32, 2, 52, best));
        return true;
    }

    // ---- materials a character does not need
    private static long keepOf(Character c, Item it) {
        boolean herb = it.itemType == Item.ItemType.Herb;
        switch (c.trait) {
            case Blacksmith: return herb ? 300 : 2000;
            case Tailor: return 2000;
            case Scribe: case Alchemist: case Chef: case Drunken: case Poisoner: case Musician: return herb ? 2000 : 300;
            default: return 300;
        }
    }

    static long surplus(Character c) {
        long s = 0;
        for (Item it : c.materialBag) if (it != null && (it.itemType == Item.ItemType.Herb || it.itemType == Item.ItemType.Metal) && it.lvl > 0) s += Math.max(0, it.quantity - keepOf(c, it));
        return s;
    }

    private static boolean tradeRun(Character c) {
        if (surplus(c) < 1000) return false;
        Area best = null;
        double bs = -1e18;
        for (Area a : c.knownLocations) {
            if (a == null || !a.city || a.cityTreasury < 2000) continue;
            int d = PlayerControl.distance(c.location, a);
            if (d > 30) continue;
            double s = Math.min(a.cityTreasury, 200000) / 5000.0 - d;
            if (s > bs) { bs = s; best = a; }
        }
        if (best == null) return false;
        c.addTarget(new Quest("Trade Run", true, 36, 2, 58, best));
        return true;
    }

    private static boolean pilgrimage(Character c) {
        if (c.age < 20 || c.majorLvl < 1) return false;
        Area pick = null;
        int n = 0;
        for (Area a : c.knownLocations) {
            if (a == null || a == c.location) continue;
            boolean famous = a.loreSite != null || a.spiritualVein || a.isOriginis() || (a.city && a.cityLevel() >= 6) || a.dungeonType == Area.DungeonType.CELESTIAL_TRIAL;
            if (!famous || PlayerControl.distance(c.location, a) > 30) continue;
            if (rand.nextInt(++n) == 0) pick = a;
        }
        if (pick == null) return false;
        c.addTarget(new Quest("Pilgrimage", true, 30, 2, 52, pick));
        return true;
    }

    private static boolean tameBeast(Character c) {
        if (c.majorLvl < 1 || Taming.beasts(c).size() >= Taming.cap(c)) return false;
        Area best = null;
        int bestD = Integer.MAX_VALUE;
        for (Area a : c.knownLocations) {
            if (a == null || a.monsters <= 0 || a.city || a.lvl > c.lvl + 3 || a.lvl < c.lvl - 25) continue;
            int d = PlayerControl.distance(c.location, a);
            if (d <= 30 && d < bestD) { bestD = d; best = a; }
        }
        if (best == null) return false;
        c.addTarget(new Quest("Tame Beast", true, 33, 2, 55, best));
        return true;
    }

    /** The next-realm Cultivation Pill this character could buy in the city, or null. */
    static Item pillFor(Character c, Area a) {
        if (a == null || !a.city || a.pillBag == null) return null;
        for (Item p : a.pillBag) {
            if (p != null && p.itemType == Item.ItemType.Pill && p.pillType == Item.PillType.Cultivation && p.lvl == c.majorLvl + 1 && p.quantity > 0 && c.canAfford(Character.itemPrice(p))) return p;
        }
        return null;
    }

    private static boolean buyPills(Character c) {
        if (c.majorLvl >= 10) return false;
        Area best = null;
        int bestD = Integer.MAX_VALUE;
        for (Area a : c.knownLocations) {
            if (a == null || a == c.location || pillFor(c, a) == null) continue;
            int d = PlayerControl.distance(c.location, a);
            if (d <= 30 && d < bestD) { bestD = d; best = a; }
        }
        if (best == null) return false;
        c.addTarget(new Quest("Buy Pills", true, 38, 3, 60, best));
        return true;
    }

    private static boolean attendAuction(Character c) {
        Area best = null;
        int bestD = Integer.MAX_VALUE;
        long wealth = c.ss + c.sc * 1_000_000L;
        for (Area a : c.knownLocations) {
            if (a == null || !Auctions.hasHouse(a) || a.auctionBag == null || a.auctionBag.isEmpty()) continue;
            int w = Auctions.weeksToNext(a), d = PlayerControl.distance(c.location, a);
            if (w < 1 || w > 14 || d > 20 || d >= bestD) continue;
            boolean want = false;
            for (Item lot : new ArrayList<>(a.auctionBag)) if (lot != null && lot.itemType != null && wealth >= Auctions.reserve(lot) && Auctions.wants(c, lot, wealth)) { want = true; break; }
            if (want) { bestD = d; best = a; }
        }
        if (best == null) return false;
        c.addTarget(new Quest("Attend Auction", true, 42, 3, 62, best));
        return true;
    }

    /** Cultivation Pills of a realm already passed: what a trip to market sells (the same rule as `Character.sell`). */
    static int pillStock(Character c) {
        int n = 0;
        for (Item it : c.pillBag) if (it != null && it.pillType == Item.PillType.Cultivation && it.lvl <= c.majorLvl) n += Math.max(0, it.quantity);
        return n;
    }

    private static boolean marketHasFood(Character c, Area a) {
        if (a == null || !a.city) return false;
        for (Item it : Life.market(a)) if (it != null && it.itemType == Item.ItemType.Food && it.quantity > 0 && it.lvl <= Math.max(1, c.majorLvl + 1)) return true;
        return false;
    }

    /** Someone with no meal in the pack and none working goes to the nearest city market. */
    private static boolean marketTrip(Character c) {
        if (c.age < 12 || (c.fx != null && c.fx.foodWeeks > 0)) return false;
        int meals = 0;
        if (c.goods != null) for (Item it : c.goods) if (it != null && it.itemType == Item.ItemType.Food) meals += Math.max(0, it.quantity);
        if (meals >= 2) return false; // running low is enough
        Area best = null;
        int bestD = Integer.MAX_VALUE;
        for (Area a : c.knownLocations) {
            if (a == null || !marketHasFood(c, a)) continue;
            int d = PlayerControl.distance(c.location, a);
            if (d <= 30 && d < bestD) { bestD = d; best = a; }
        }
        if (best == null) return false;
        c.addTarget(new Quest("Market Trip", true, 30, 2, 52, best));
        return true;
    }

    private static boolean herbWorker(Character c) {
        switch (c.trait) { case Gatherer: case Alchemist: case Scribe: case Tailor: return true; default: return false; }
    }

    private static boolean metalWorker(Character c) {
        switch (c.trait) { case Miner: case Blacksmith: case Tailor: return true; default: return false; }
    }

    private static long yieldOf(Character c, Area a) {
        return Math.max(herbWorker(c) ? a.herbMaterials : 0, metalWorker(c) ? a.metalMaterials : 0);
    }

    /** A Gatherer, Miner or craftsman goes where the herbs or ore are rich (at least three times as much as here). */
    private static boolean gatheringTrip(Character c) {
        if (!herbWorker(c) && !metalWorker(c)) return false;
        long here = yieldOf(c, c.location);
        Area best = null;
        long bestY = Math.max(20000, here * 3);
        for (Area a : c.knownLocations) {
            if (a == null || a == c.location || a.city) continue;
            long y = yieldOf(c, a);
            if (y > bestY && PlayerControl.distance(c.location, a) <= 25) { bestY = y; best = a; }
        }
        if (best == null) return false;
        c.addTarget(new Quest("Gathering Trip", true, 32, 2, 54, best));
        return true;
    }

    /** Someone stronger who may teach: the sect's master, or a friend ten levels above. */
    private static boolean seekTeacher(Character c) {
        if (c.age < 14) return false;
        ArrayList<Character> cand = new ArrayList<>();
        if (c.sect != null && c.sect.master != null) cand.add(c.sect.master);
        for (Relationship r : new ArrayList<>(c.relationshipList.relationships)) if (r.value >= 1 && !r.refused) cand.add(Character.charById(r.friendID));
        cand.removeIf(t -> t == null || t == c || t.player == 1 || !ready(c, t) || t.lvl < c.lvl + 10 || PlayerControl.distance(c.location, t.location) > 30
                || t.trait == Character.Traits.Beast || t.trait == Character.Traits.Monster);
        if (cand.isEmpty()) return false;
        c.addTarget(new Quest("Seek Teacher", true, 33, 2, 54, cand.get(rand.nextInt(cand.size()))));
        return true;
    }

    /** A famous or notable place to see: a city, a sect's home, a dungeon, a spirit vein, a story place. */
    private static boolean sightseeing(Character c) {
        Area pick = null;
        int n = 0;
        for (Area a : c.knownLocations) {
            if (a == null || a == c.location) continue;
            if (!(a.city || a.sect != null || a.dungeonType != null || a.spiritualVein || a.loreSite != null)) continue;
            int d = PlayerControl.distance(c.location, a);
            if (d < 5 || d > 30) continue;
            if (rand.nextInt(++n) == 0) pick = a;
        }
        if (pick == null) return false;
        c.addTarget(new Quest("Sightseeing", true, 26, 2, 48, pick));
        return true;
    }

    /** A Runner carries word between the cities for a fee. */
    private static boolean courier(Character c) {
        if (c.trait != Character.Traits.Runner || c.age < 14) return false;
        Area pick = null;
        int n = 0;
        for (Area a : c.knownLocations) {
            if (a == null || !a.city || a.cityTreasury < 2000) continue;
            int d = PlayerControl.distance(c.location, a);
            if (d < 8 || d > 30) continue;
            if (rand.nextInt(++n) == 0) pick = a;
        }
        if (pick == null) return false;
        c.addTarget(new Quest("Courier Run", true, 34, 2, 55, pick));
        return true;
    }

    /** A place to found a city or sect: spirit veins first (a hundred points), then closeness; sects like cities a little. Null when nothing fits. */
    static Area foundingSite(Character c, boolean city) {
        Area best = null;
        double bs = -1e18;
        int R = 7;
        for (int dx = -R; dx <= R; dx++) for (int dy = -R; dy <= R; dy++) {
            int x = Math.floorMod(c.location.x + dx, Map.range), y = Math.floorMod(c.location.y + dy, Map.range);
            Area a = Map.tiles[x][y];
            if (a == null || "Ocean".equals(a.biomem) || a.isOriginis() || a.loreSite != null) continue;
            if (city ? a.city : a.sect != null) continue;
            double s = (a.spiritualVein ? 100 : 0) - 3 * Math.max(Math.abs(dx), Math.abs(dy)) + rand.nextInt(10) + (!city && a.city ? 15 : 0);
            if (s > bs) { bs = s; best = a; }
        }
        return best;
    }

    private static boolean found(Character c) {
        if (c.majorLvl < 2 || c.age < 25) return false;
        boolean sect = c.sect == null && Settlements.sectRoom() && (rand.nextInt(3) != 0 || !Settlements.cityRoom());
        boolean city = !sect && Settlements.cityRoom();
        if (!sect && !city) return false;
        Area site = foundingSite(c, city);
        if (site == null) return false;
        c.addTarget(new Quest(city ? "Found City" : "Found Sect", true, 38, 2, 58, site));
        return true;
    }

    // ------------------------------------------------------------------ viability and what happens on arrival
    static int viable(Character c, Quest q) {
        Area a = q.targetArea;
        switch (q.name) {
            case "Courtship": {
                Character t = Character.charById(q.targetCharacter);
                return c.spouse == null && suitor(c, t) ? 1 : 0;
            }
            case "Give Gift": case "Visit Family": case "Teach Disciple": {
                Character t = Character.charById(q.targetCharacter);
                return t != null && !t.dead && t.born ? 1 : 0;
            }
            case "Trade Run": return a != null && a.city && a.cityTreasury >= 1000 && surplus(c) >= 300 ? 1 : 0;
            case "Pilgrimage": case "Meditation Retreat": return a != null ? 1 : 0;
            case "Tame Beast": return a != null && a.monsters > 0 && Taming.beasts(c).size() < Taming.cap(c) ? 1 : 0;
            case "Buy Pills": return pillFor(c, a) != null ? 1 : 0;
            case "Attend Auction": return a != null && Auctions.hasHouse(a) && Auctions.weeksToNext(a) > 0 ? 1 : 0;
            case "Market Trip": return marketHasFood(c, a) ? 1 : 0;
            case "Gathering Trip": case "Sightseeing": return a != null ? 1 : 0;
            case "Courier Run": return a != null && a.city ? 1 : 0;
            case "Seek Teacher": {
                Character t = Character.charById(q.targetCharacter);
                return t != null && !t.dead && t.born ? 1 : 0;
            }
            case "Found City": return a != null && !a.city && c.majorLvl >= 2 && Settlements.cityRoom() ? 1 : 0;
            case "Found Sect": return a != null && a.sect == null && c.sect == null && c.majorLvl >= 2 && Settlements.sectRoom() ? 1 : 0;
            default: return 1;
        }
    }

    private static void rest(Character c) { c.nextRoutineWeek = GameDriver.time + 2; }

    /** Runs when the character is at the quest's place (or, for a person, has to be where that person is). 1 = done, 0 = still going (the quest stays), -1 = failed. */
    static int run(Character c, Quest q) {
        switch (q.name) {
            case "Give Gift": { // walk to them; when you stand in their tile, hand over a gift from your own holdings (Gifts)
                Character t = Character.charById(q.targetCharacter);
                if (t == null || t.dead) return -1;
                if (t.location != c.location) return 0;
                if (Gifts.deliver(c, t) == null) { c.changeRelationship(t, 1); say(c, "You came to see " + t.name + " " + t.lastName + " with nothing to give: you only visit."); }
                rest(c);
                return 1;
            }
            case "Visit Family": { // a few days under the same roof
                Character t = Character.charById(q.targetCharacter);
                if (t == null || t.dead) return -1;
                if (t.location != c.location) return 0;
                if (q.progress == 0) { c.changeRelationship(t, 1); say(c, "You spend a few days with " + t + "."); }
                q.progress++;
                c.cultivate(); t.cultivate();
                if (q.progress < 4) return 0;
                rest(c);
                return 1;
            }
            case "Teach Disciple": { // four days of lessons
                Character s = Character.charById(q.targetCharacter);
                if (s == null || s.dead) return -1;
                if (s.location != c.location) return 0;
                q.progress++;
                s.cultivate(); s.cultivate();
                if (q.progress == 1) {
                    c.changeRelationship(s, 1);
                    if (c.sect != null && c.sect == s.sect) c.sectPoints += 5;
                    Technique best = null;
                    for (Technique t : c.techniqueList) if (best == null || t.potential > best.potential) best = t;
                    if (best != null) {
                        final String nm = best.name;
                        if (s.techniqueList.stream().noneMatch(t -> t.name.equals(nm))) {
                            Technique copy = new Technique(best);
                            copy.lvl = Math.max(1, best.lvl / 2);
                            s.techniqueList.add(copy);
                            say(c, "You teach " + s + " the " + best.name + ".");
                        }
                    }
                    say(c, "You spend the days teaching " + s + ".");
                }
                if (q.progress < 4) return 0;
                rest(c);
                return 1;
            }
            case "Courtship": { // three days in each other's company
                Character t = Character.charById(q.targetCharacter);
                if (t == null || t.dead) return -1;
                if (t.location != c.location) return 0;
                if (!suitor(c, t) || c.spouse != null) return -1;
                q.progress++;
                if (q.progress < 3) return 0;
                c.changeRelationship(t, 1);
                say(c, "You spend the days with " + t + ".");
                rest(c);
                return 1;
            }
            case "Trade Run": {
                if (q.progress++ < 1) return 0; // a turn of haggling first
                Area city = c.location;
                if (city == null || !city.city) return -1;
                long total = 0;
                long room = (long) (city.cityTreasury * 0.2);
                for (Item it : new ArrayList<>(c.materialBag)) {
                    if (it == null || (it.itemType != Item.ItemType.Herb && it.itemType != Item.ItemType.Metal) || it.lvl <= 0) continue;
                    int spare = (int) Math.min(Integer.MAX_VALUE, it.quantity - keepOf(c, it));
                    if (spare <= 0) continue;
                    long unit = Math.max(1, (long) (Math.pow(it.lvl, 2.5) / 10));
                    long left = room - total;
                    if (left < unit) break;
                    if (unit * spare > left) spare = (int) (left / unit);
                    it.quantity -= spare;
                    if (it.quantity <= 0) c.materialBag.remove(it);
                    total += unit * spare;
                    if (city.materialBag == null) city.materialBag = new SafeList<>();
                    Character.addToBag(city.materialBag, new Item(it.itemType, it.lvl, spare));
                }
                if (total > 0) {
                    city.cityTreasury -= total;
                    c.ss += total; c.convertCurrency();
                    say(c, "You sell your spare herbs and metal in " + city.name + " for " + total + " ss.");
                }
                rest(c);
                return 1;
            }
            case "Pilgrimage": {
                q.progress++;
                c.cultivate(); c.cultivate();
                if (q.progress == 1) say(c, "You reach " + c.location.name + " on pilgrimage and stay to meditate.");
                if (q.progress < 6) return 0;
                if (c.potential < 100 && rand.nextInt(25) == 0) { int g = c.givePotential(1); say(c, g > 0 ? "The pilgrimage brings an insight: your potential rises to " + c.potential + "." : "The pilgrimage brings an insight: your potential stirs (" + Math.round(c.potentialProgress * 100) + "% of the next point)."); }
                rest(c);
                return 1;
            }
            case "Meditation Retreat": {
                if (q.progress == 0) invitePartner(c, c.location);
                q.progress++;
                int n = c.location.spiritualVein ? 2 : 1;
                for (int i = 0; i < n; i++) c.cultivate();
                Character p = partnerHere(c);
                if (p != null) { c.cultivate(); p.cultivate(); } // dual cultivation
                if (q.progress == 1) say(c, "You begin a meditation retreat at " + c.location.name + (c.location.spiritualVein ? " (a spirit vein)." : "."));
                if (q.progress < 8) return 0;
                say(c, "The retreat is over.");
                rest(c);
                return 1;
            }
            case "Tame Beast": { // up to three attempts, one a turn
                q.progress++;
                boolean tamed = c.location != null && c.location.monsters > 0 && Taming.beasts(c).size() < Taming.cap(c) && Taming.attempt(c);
                if (!tamed && q.progress < 3 && c.location != null && c.location.monsters > 0 && Taming.beasts(c).size() < Taming.cap(c)) return 0;
                rest(c);
                return 1;
            }
            case "Buy Pills": {
                c.buyNextRealmPill();
                rest(c);
                return 1;
            }
            case "Attend Auction": {
                int wait = Auctions.weeksToNext(c.location);
                if (wait > 0 && q.progress < 8) {
                    if (q.progress == 0) say(c, "You wait in " + c.location.name + " for the auction (about " + wait + " week" + (wait == 1 ? "" : "s") + "; you are asked for your bids when it is held).");
                    q.progress++; return 0;
                }
                say(c, wait > 0 ? "The auction at " + c.location.name + " has not been held after " + q.progress + " weeks; you give up waiting." : "The auction at " + c.location.name + " is over" + (q.progress == 0 ? ": nothing is on the block." : "."));
                rest(c);
                return 1;
            }
            case "Market Trip": { // a turn to look round the stalls
                if (q.progress++ < 1) return 0;
                Life.marketTrip(c);
                rest(c);
                return 1;
            }
            case "Gathering Trip": {
                q.progress++;
                boolean herb = herbWorker(c) && (!metalWorker(c) || q.progress % 2 == 1);
                for (int i = 0; i < 2; i++) { if (herb) c.forage(); else c.mine(); }
                if (q.progress == 1) say(c, "You set to work gathering at " + c.location.name + ".");
                if (q.progress < 8) return 0;
                rest(c);
                return 1;
            }
            case "Seek Teacher": { // five days at the teacher's side
                Character t = Character.charById(q.targetCharacter);
                if (t == null || t.dead) return -1;
                if (t.location != c.location) return 0;
                q.progress++;
                c.cultivate(); c.cultivate();
                if (q.progress == 1) {
                    c.changeRelationship(t, 1);
                    Technique best = null, mine = null;
                    for (Technique x : t.techniqueList) if (best == null || x.potential > best.potential) best = x;
                    for (Technique x : c.techniqueList) if (mine == null || x.potential > mine.potential) mine = x;
                    if (best != null && (mine == null || best.potential > mine.potential)) {
                        final String nm = best.name;
                        if (c.techniqueList.stream().noneMatch(x -> x.name.equals(nm))) {
                            Technique copy = new Technique(best);
                            copy.lvl = Math.max(1, best.lvl / 2);
                            c.techniqueList.add(copy);
                            say(c, t + " teaches you the " + best.name + ".");
                        }
                    }
                    say(c, "You spend some days at the side of " + t + ".");
                }
                if (q.progress < 5) return 0;
                rest(c);
                return 1;
            }
            case "Sightseeing": {
                q.progress++;
                c.cultivate();
                if (q.progress == 1) {
                    for (int dx = -2; dx <= 2; dx++) for (int dy = -2; dy <= 2; dy++) {
                        Area near = Map.tiles[Math.floorMod(c.location.x + dx, Map.range)][Math.floorMod(c.location.y + dy, Map.range)];
                        if (near != null) c.learnPlace(near);
                    }
                    say(c, "You look around " + c.location.name + ".");
                }
                if (q.progress < 3) return 0;
                rest(c);
                return 1;
            }
            case "Courier Run": {
                Area a = c.location;
                if (a == null || !a.city) return -1;
                long pay = Math.min(a.cityTreasury / 50, 3000);
                if (pay > 0) { a.cityTreasury -= pay; c.ss += pay; c.convertCurrency(); }
                if (a.cityOwner != null && !a.cityOwner.dead && a.cityOwner != c) c.changeRelationship(a.cityOwner, 1);
                c.cultivate(); c.cultivate();
                say(c, "You deliver your word to " + a.name + (pay > 0 ? " and are paid " + pay + " ss." : "."));
                rest(c);
                return 1;
            }
            case "Found City": {
                String why = c.foundCityNow();
                rest(c);
                return why == null ? 1 : -1;
            }
            case "Found Sect": {
                String why = c.foundSectNow();
                rest(c);
                return why == null ? 1 : -1;
            }
            default: return 1;
        }
    }

    /** A craftsman short of herbs or metal buys some from the city's stock (what Gatherers and Miners bring on their Trade Runs) at 1.5 times what the city paid for it. */
    static boolean buyMaterials(Character c, Item.ItemType type) {
        Area city = c.location;
        if (city == null || !city.city || city.materialBag == null) return false;
        Item best = null;
        for (Item it : city.materialBag) if (it != null && it.itemType == type && it.lvl > 0 && it.quantity >= 500 && (best == null || it.lvl > best.lvl)) best = it;
        if (best == null) return false;
        int n = Math.min(best.quantity, 1500);
        long price = Math.max(1, (long) (Math.pow(best.lvl, 2.5) / 10 * 1.5)) * n;
        if (!c.canAfford(price)) return false;
        c.deductSs(price);
        city.receivePayment(price);
        best.quantity -= n;
        if (best.quantity <= 0) city.materialBag.remove(best);
        Character.addToBag(c.materialBag, new Item(type, best.lvl, n));
        return true;
    }

    /** The warmest person who stands here: a spouse, else a best friend. */
    static Character partnerHere(Character c) {
        if (c.spouse != null && !c.spouse.dead && c.spouse.location == c.location) return c.spouse;
        for (Relationship r : new ArrayList<>(c.relationshipList.relationships)) {
            if (r.value < 3) continue;
            Character f = Character.charById(r.friendID);
            if (f != null && !f.dead && f.born && f.location == c.location) return f;
        }
        return null;
    }

    /** The spouse (else a best friend) who is not here hears of the retreat and comes too (an NPC or an AUTO character; a person who plays MANUAL is not sent anywhere). */
    private static void invitePartner(Character c, Area where) {
        Character p = c.spouse;
        if (p == null || p.dead || !p.born || p.location == where) {
            p = null;
            for (Relationship r : new ArrayList<>(c.relationshipList.relationships)) {
                if (r.value < 3) continue;
                Character f = Character.charById(r.friendID);
                if (f != null && !f.dead && f.born && f.location != where && f.age >= 12) { p = f; break; }
            }
        }
        if (p == null || p.dead || !p.born || p.location == where || p.isManualPc()) return;
        if (PlayerControl.distance(p.location, where) > 30) return;
        final Character friend = p; final Area at = where;
        Map.later(() -> { if (!friend.dead && friend.findQueuedQuest("Meditation Retreat") == null) friend.addTarget(new Quest("Meditation Retreat", true, 46, 2, 62, at)); }); // (the friend's own queue: touched after a parallel phase)
    }
}
