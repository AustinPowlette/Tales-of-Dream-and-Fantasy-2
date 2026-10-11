package Game;

import java.util.ArrayList;
import java.util.List;

/** Keeps an old world small (2026-10-10). A world of a hundred years or more used to hold, per living cultivator, a long list of faint acquaintances (93% were +1 / -1 ties, many of them to the
 *  dead), twenty queued errands (most of them copies of a few kinds), and the whole bodies of its dead kin; a world of five hundred years took 1.4 GB in a file and gigabytes in memory.
 *  - `husk`: a dead cultivator keeps only what the family window and the chronicle show (name, level, cause, kin); the lists it carried are dropped.
 *  - `yearly`: finds the dead who are still held by living kin and makes husks of them (also in an old save), and counts the beasts that people keep.
 *  - `trim`: a living cultivator forgets weak ties to the dead and past the cap, drops copies of the same errand past a few, and forgets far places past a cap (once a year, with `maintainQuests`).
 *  Nothing here changes what a person can do now: only what an old, unused record keeps. */
final class Slim {
    private Slim() { }

    /** Errands of one kind a person keeps queued (the best ones; the social errands follow the relationships and have their own cap). */
    static final int SAME_ERRAND = 3;
    /** Names of the errands that tie to a person and follow the relationship list. */
    private static boolean social(String n) { return "Find Person".equals(n) || "Battle Dislike".equals(n) || "Battle Rival".equals(n) || "Battle Arch-Enemy".equals(n); }

    /** A dead cultivator's lists are let go. Never for a person who plays (they may reincarnate) or the Palace's people of a story. */
    static void husk(Character c) {
        if (c == null || !c.dead || c.husked || c.player > 0 || c.fatedTo > 0) return;
        c.husked = true;
        c.relationshipList = new RelationshipList();
        c.targetQuests = new java.util.PriorityQueue<>(1);
        c.spotQuests = new java.util.PriorityQueue<>(1);
        c.currentQuest = null;
        c.backpack = new ArrayList<>(0); c.materialBag = new ArrayList<>(0); c.pillBag = new ArrayList<>(0); c.arrayBag = new ArrayList<>(0);
        c.techniqueList = new ArrayList<>(0); c.knownLocations = new ArrayList<>(0); c.ownedShops = new ArrayList<>(0);
        c.goods = null; c.fx = null; c.beastIds = null; c.lostBeastIds = null; c.followerIds = null;
        c.equippedWeapon = null; c.equippedClothing = null;
        c.qTarget = null; c.qEma = null;
    }

    /** Once a year (after the year's block): the dead still held by living kin become husks. */
    static void yearly() {
        int tamed = 0;
        ArrayList<Character> stack = new ArrayList<>();
        for (Character c : Map.characters) {
            if (c == null) continue;
            if (c.wasTamed && c.masterId >= 0 && !c.dead) tamed++;
            if (c.dead) continue;
            kin(c, stack);
            while (!stack.isEmpty()) {
                Character d = stack.remove(stack.size() - 1);
                if (d == null || !d.dead || d.husked) continue;
                if (d.player > 0 || d.fatedTo > 0) continue;
                husk(d);
                kin(d, stack);
            }
        }
        Taming.keptCount = tamed;
    }

    private static void kin(Character c, List<Character> out) {
        if (c.father != null && c.father.dead && !c.father.husked) out.add(c.father);
        if (c.mother != null && c.mother.dead && !c.mother.husked) out.add(c.mother);
        if (c.spouse != null && c.spouse.dead && !c.spouse.husked) out.add(c.spouse);
        if (c.reincarnationKiller != null && c.reincarnationKiller.dead && !c.reincarnationKiller.husked) out.add(c.reincarnationKiller);
        if (c.siblings != null) for (Character x : c.siblings) if (x != null && x.dead && !x.husked) out.add(x);
        if (c.children != null) for (Character x : c.children) if (x != null && x.dead && !x.husked) out.add(x);
    }

    /** Once a year for each living cultivator (from `maintainQuests`). */
    static void trim(Character c) {
        if (c.player > 0 || c.dead) return;
        if (c.relationshipList != null) c.relationshipList.trim();
        // copies of the same errand past a few: the best are kept
        if (c.targetQuests != null && c.targetQuests.size() > SAME_ERRAND) {
            java.util.HashMap<String, Integer> seen = null;
            ArrayList<Quest> all = new ArrayList<>(c.targetQuests);
            all.sort((a, b) -> Integer.compare(b.priority, a.priority));
            ArrayList<Quest> drop = null;
            for (Quest q : all) {
                if (q.pinned || q.userCreated || q.progress > 0 || social(q.name)) continue;
                if (seen == null) seen = new java.util.HashMap<>();
                int n = seen.merge(q.name, 1, Integer::sum);
                if (n > SAME_ERRAND) { if (drop == null) drop = new ArrayList<>(); drop.add(q); }
            }
            if (drop != null) { for (Quest q : drop) { c.targetQuests.remove(q); q.questDeconstructor(); } }
        }
        if (c.targetQuests != null && c.targetQuests.size() > 2) {
            capNamed(c, "Find Person", true, Character.WEAK_SOCIAL_CAP);     // the faint-tie errands (the cap used to be 4)
            capNamed(c, "Battle Dislike", true, Character.WEAK_SOCIAL_CAP);
            capNamed(c, "Rob Caravan", false, 2);                            // they piled up, one for each wealthy merchant ever seen
        }
        // an NPC's heap of raw materials: past a few stacks the least valuable ones (under SMALL_STACK stones in all) are let go
        if (c.materialBag != null && c.materialBag.size() > MATERIAL_STACKS) {
            ArrayList<Item> bag = new ArrayList<>(c.materialBag);
            bag.sort((x, y) -> Long.compare(Character.itemPrice(y) * y.quantity, Character.itemPrice(x) * x.quantity));
            for (int i = MATERIAL_STACKS; i < bag.size(); i++) if (Character.itemPrice(bag.get(i)) * (long) bag.get(i).quantity < SMALL_STACK) c.materialBag.remove(bag.get(i));
        }
        // stacks that were used up and left at 0
        if (c.materialBag != null) c.materialBag.removeIf(m -> m.quantity <= 0);
        if (c.pillBag != null) c.pillBag.removeIf(m -> m.quantity <= 0);
        if (c.goods != null) c.goods.removeIf(m -> m.quantity <= 0);
        // the same maker (or pill house) written out again and again on the items: one string for all
        if (c.backpack != null) for (Item it : c.backpack) internNames(it);
        if (c.pillBag != null) for (Item it : c.pillBag) internNames(it);
        if (c.goods != null) for (Item it : c.goods) internNames(it);
        if (c.equippedWeapon != null) internNames(c.equippedWeapon);
        if (c.equippedClothing != null) internNames(c.equippedClothing);
        c.trimKnown();
    }

    private static void internNames(Item it) {
        if (it.maker != null) it.maker = it.maker.intern();
        if (it.brand != null) it.brand = it.brand.intern();
    }

    static final int MATERIAL_STACKS = 8;     // kinds of herb and metal an NPC keeps without question
    static final long SMALL_STACK = 20000;    // spirit stones: a stack worth less than this is dropped past the eighth

    /** At most `limit` queued errands of this name (the best by priority stay); `oneTimeOnly`: only the one-time ones count (a lasting friendship's errand is not a faint tie). */
    private static void capNamed(Character c, String name, boolean oneTimeOnly, int limit) {
        ArrayList<Quest> mine = null;
        for (Quest q : c.targetQuests) if (name.equals(q.name) && (!oneTimeOnly || q.oneTime) && !q.pinned && !q.userCreated && q.progress == 0) { if (mine == null) mine = new ArrayList<>(); mine.add(q); }
        if (mine == null || mine.size() <= limit) return;
        mine.sort((a, b) -> Integer.compare(b.priority, a.priority));
        for (int i = limit; i < mine.size(); i++) { c.targetQuests.remove(mine.get(i)); mine.get(i).questDeconstructor(); }
    }
}
