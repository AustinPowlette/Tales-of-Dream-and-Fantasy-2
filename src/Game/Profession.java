package Game;

import java.util.ArrayList;

import static Game.GameDriver.rand;

/** The Profession button / `profession` command: what each trait that makes or does something does when the player asks for a turn of their trade.
 *  Every branch says what happened (or why nothing could), and spends the turn only when something was done. NPCs and AUTO characters use `Character.professionPicker`. */
final class Profession {
    private Profession() { }

    /** Whether a trait has a trade of its own (the others craft as amateurs, see AmateurCraft). */
    static boolean hasTrade(Character.Traits t) {
        switch (t) {
            case Alchemist: case Blacksmith: case Tailor: case Scribe: case Leader: case Innovative: case Merchant: case Friendly: case Lucky:
            case Tamer: case Poisoner: case Drunken: case Chef: case Musician: case Gatherer: case Miner: return true;
            default: return false;
        }
    }

    private static void say(String s) { System.out.println(s); }

    /** The largest single stack of a material (a craft uses one stack). */
    static int bestStack(Character c, Item.ItemType t, int maxLvl) {
        int best = 0;
        for (Item i : c.materialBag) if (i != null && i.itemType == t && i.lvl > 0 && i.lvl <= maxLvl && i.quantity > best) best = i.quantity;
        return best;
    }

    private static long wealth(Character c) { return PlayerLists.wealth(c); }

    /** One turn of the trade. True = a turn was spent. */
    static boolean run(Character c) {
        if (c == null || c.location == null) return false;
        switch (c.trait) {
            case Alchemist: return alchemist(c);
            case Blacksmith: return smith(c);
            case Tailor: return tailor(c);
            case Scribe: return scribe(c);
            case Leader: return leader(c);
            case Innovative: return innovator(c);
            case Merchant: return merchant(c);
            case Friendly: return friendly(c);
            case Lucky: return lucky(c);
            case Chef: return art(c, Item.ItemType.Food, "cook");
            case Drunken: return art(c, Item.ItemType.Wine, "brew");
            case Poisoner: return art(c, Item.ItemType.Poison, "mix");
            case Musician: return Life.play(c) == 2;
            case Tamer: return tamer(c);
            case Gatherer: return gatherer(c, true);
            case Miner: return gatherer(c, false);
            default:
                say("Your trait (" + c.getTrait() + ") has no trade. 'craft' lists what you can make as an amateur.");
                return false;
        }
    }

    // ------------------------------------------------------------------ gathering for a trade
    /** The trade is short of a material: gather it here, or buy some at the city market. True = a turn was spent. */
    private static boolean fetch(Character c, Item.ItemType type, String forWhat) {
        boolean herb = type == Item.ItemType.Herb;
        Area a = c.location;
        int stack = bestStack(c, type, 100);
        int here = herb ? a.herbMaterials : a.metalMaterials;
        String what = herb ? "herbs" : "metal";
        if (here > 0) {
            say("You are short of " + what + " to " + forWhat + " (your biggest stack holds " + stack + "); you gather instead.");
            if (herb) c.forage(); else c.mine();
            return true;
        }
        if (a.city) {
            long w0 = wealth(c);
            if (QuestLife.buyMaterials(c, type)) {
                say("You are short of " + what + " to " + forWhat + "; you buy some at the market for " + (w0 - wealth(c)) + " ss (your biggest stack now holds " + bestStack(c, type, 100) + ").");
                return true;
            }
        }
        say("You cannot " + forWhat + ": your biggest stack of " + what + " holds " + stack + " and there is none to gather here" + (a.city ? " (and the market has none you can afford)" : "") + ". Find a tile that has some ('l' lists places).");
        return false;
    }

    // ------------------------------------------------------------------ the crafts
    private static boolean alchemist(Character c) {
        int cap = c.professionLvl / 10 + 2;
        boolean blood = false;
        for (Item i : c.materialBag) if (i != null && i.itemType == Item.ItemType.Blood && i.quantity >= 1000) blood = true;
        if (!blood && bestStack(c, Item.ItemType.Herb, cap) < 50) {
            if (bestStack(c, Item.ItemType.Herb, 100) >= 50) say("Your herbs are too rich for your skill (Lv." + c.professionLvl + " can use herbs up to quality " + cap + ").");
            return fetch(c, Item.ItemType.Herb, "brew pills");
        }
        c.alchemy();
        return true;
    }

    private static boolean smith(Character c) {
        if (bestStack(c, Item.ItemType.Metal, 100) < Character.CRAFT_COST) return fetch(c, Item.ItemType.Metal, "forge a weapon");
        c.smithing();
        return true;
    }

    private static boolean tailor(Character c) {
        if (bestStack(c, Item.ItemType.Herb, 100) < Character.CRAFT_COST && bestStack(c, Item.ItemType.Metal, 100) < Character.CRAFT_COST)
            return fetch(c, c.location.herbMaterials > 0 || !(c.location.metalMaterials > 0) ? Item.ItemType.Herb : Item.ItemType.Metal, "sew clothes");
        c.tailoring();
        return true;
    }

    private static boolean scribe(Character c) {
        int cap = c.professionLvl / 10 + 2;
        if (bestStack(c, Item.ItemType.Herb, cap) < Character.ARRAY_MIN && bestStack(c, Item.ItemType.Metal, cap) < Character.ARRAY_MIN)
            return fetch(c, Item.ItemType.Herb, "draw an array (it takes " + Character.ARRAY_MIN + " of one stack)");
        int before = c.arrayBag.size();
        c.array();
        if (c.arrayBag.size() > before) say("You carry " + c.arrayBag.size() + " array(s). 'install' sets one in this place.");
        return true;
    }

    private static boolean art(Character c, Item.ItemType type, String verb) {
        int skill = Math.max(10, c.professionLvl);
        Item herb = Life.bestHerb(c, skill / 10 + 2, 50);
        if (herb == null) return fetch(c, Item.ItemType.Herb, verb);
        return Life.craft(c, type) == 2;
    }

    private static boolean gatherer(Character c, boolean herb) {
        if ((herb ? c.location.herbMaterials : c.location.metalMaterials) <= 0) {
            say("There " + (herb ? "are no herbs" : "is no metal") + " here to gather. Find a richer tile ('l' lists places).");
            return false;
        }
        if (herb) c.forage(); else c.mine();
        return true;
    }

    // ------------------------------------------------------------------ the other trades
    private static boolean innovator(Character c) {
        if (c.inspiration >= Character.INSPIRATION_NEEDED) {
            Technique t = c.inventTechnique();
            say("The ideas come together: you set down " + t.name + " (potential " + t.potential + ", " + t.getType() + "). It is yours to use, teach or sell to a library.");
            return true;
        }
        Item w = c.equippedWeapon;
        if (w != null && w.enchantment == null) {
            c.enchantWeapon();
            say("You work an enchantment into " + w.name + ": " + w.enchantment + " (+" + w.enchantBonus + ").");
            return true;
        }
        say("Inspiration " + c.inspiration + "/" + Character.INSPIRATION_NEEDED + ": a new technique comes from fighting (a hunt counts), not from sitting still. "
                + (w == null ? "Equip a weapon and you can enchant it meanwhile." : "Your weapon already carries an enchantment."));
        return false;
    }

    private static boolean merchant(Character c) {
        Area a = c.location;
        if (!a.city) { say("Merchants trade in cities. Go to one and use Profession to open a shop or restock yours."); return false; }
        int shops0 = c.ownedShops.size();
        Shop mine = null;
        for (Shop s : c.ownedShops) if (s.location == a) mine = s;
        if (mine == null && (shops0 >= c.maxShops() || a.shops.size() >= a.maxShopSlots())) {
            say(shops0 >= c.maxShops() ? "You already keep the most shops you can (" + shops0 + ")." : a.name + " has no free shop slot.");
            return false;
        }
        int backpack0 = c.backpack.size();
        c.professionPicker();
        for (Shop s : c.ownedShops) if (s.location == a) mine = s;
        if (mine == null) { say("You could not set up a shop here."); return false; }
        if (c.ownedShops.size() > shops0) say("You open a shop in " + a.name + " (" + c.ownedShops.size() + "/" + c.maxShops() + " shops). The goods you carry go on its shelves.");
        else say("You restock your shop in " + a.name + " from the city (" + mine.inventory.size() + " goods on the shelves)"
                + (c.backpack.size() > backpack0 ? " and load " + (c.backpack.size() - backpack0) + " item(s) to sell in another city." : "."));
        return true;
    }

    private static boolean friendly(Character c) {
        ArrayList<Character> others = new ArrayList<>();
        for (Character p : c.location.characters) if (p != null && p != c && !p.dead && p.born && p.age >= 8 && p.trait != Character.Traits.Beast && p.trait != Character.Traits.Monster) others.add(p);
        if (others.isEmpty()) { say("Nobody else is here to talk to."); return false; }
        Character t = others.get(rand.nextInt(others.size()));
        c.interact(t);
        Relationship r = c.relationshipList.contains(t.id);
        say("You spend the week talking with " + t.name + " " + t.lastName + (r != null ? " (how you stand: " + r.value + ")." : "."));
        return true;
    }

    private static boolean lucky(Character c) {
        if (c.location.deadCharacters.isEmpty()) { say("Fortune finds nothing to pick up here: no one has fallen in this tile."); return false; }
        long w0 = wealth(c); int items0 = c.backpack.size() + c.pillBag.size() + c.materialBag.size();
        c.eventLootDead();
        long gain = wealth(c) - w0; int items = c.backpack.size() + c.pillBag.size() + c.materialBag.size() - items0;
        if (gain <= 0 && items <= 0) { say("Luck leads you to the fallen here, but they are too strong for you to take anything from (their realm is two or more above yours)."); return true; }
        say("Luck leads you to the fallen: you take " + (gain > 0 ? gain + " ss" : "") + (gain > 0 && items > 0 ? " and " : "") + (items > 0 ? items + " item stack(s)" : "") + ".");
        return true;
    }

    private static boolean tamer(Character c) {
        String why = Taming.tame(c);
        if (why == null) return true; // the story was told
        say(why);
        return why.startsWith("The "); // refused or slipped away: the attempt used the turn
    }

    // ------------------------------------------------------------------ the Leader
    /** What the Leader's trade would do here: {"claim"|"city"|"sect", text} or {null, reason}. */
    static String[] leaderPlan(Character c) {
        Area a = c.location;
        if (c.majorLvl < 2) return new String[]{null, "A Leader founds or claims a city at Major Lvl 2 (you are Major Lvl " + c.majorLvl + ")."};
        if (a.city && a.cityOwner == null) return new String[]{"claim", "Claim lordship of " + a.name + "?"};
        if (!a.city) return new String[]{"city", "Found a city here? (A spirit vein site is best; the choice cannot be undone.)"};
        if (a.sect == null && c.sect == null) return new String[]{"sect", "Found a sect in " + a.name + "?"};
        return new String[]{null, a.name + " is already " + (a.cityOwner == c ? "yours" : "ruled by " + a.cityOwner) + (a.sect != null ? " and holds " + a.sect.name : "") + ": go to another tile to found something."};
    }

    private static boolean leader(Character c) {
        String[] plan = leaderPlan(c);
        if (plan[0] == null) { say(plan[1]); return false; }
        String why = "sect".equals(plan[0]) ? c.foundSectNow() : c.foundCityNow();
        if (why != null) { say(why); return false; }
        return true;
    }
}
