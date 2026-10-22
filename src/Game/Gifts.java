package Game;

import java.util.ArrayList;
import java.util.List;

import static Game.GameDriver.rand;

/** Gifts from the people who love a person who plays. Every few weeks a best friend (bond 3) or a relative of a human may decide to bring a gift: they get the quest "Give Gift" with the human as its target,
 *  walk to where he is, and hand it over when they stand in his tile (no messengers, no gifts from afar). What they give comes out of their OWN holdings by their trait (a Gatherer gives herbs, a Blacksmith a weapon,
 *  an Alchemist a pill ...), at a level the receiver can use, so nothing is made from nothing and the economy stays closed. The same quest can be made by hand ("Give Gift" in the New quest window): you, or your
 *  character on AUTO, walk to someone and give them a gift from your holdings. NPCs never give each other gifts (it would change the economy unseen). */
final class Gifts {
    private Gifts() { }

    static final int CHECK_MIN = 4, CHECK_SPAN = 5;  // a person who plays is looked at every 4-8 weeks
    static final int GIVER_COOLDOWN = 52;            // one gift a year from the same giver to the same person

    /** Game thread, once per quest turn of a person who plays: maybe someone decides to bring a gift. */
    static void weekly(Character h) {
        PlayerControl pc = h.pc;
        if (pc == null || h.player != 1 || h.dead || GameDriver.time < pc.nextGiftCheck) return;
        pc.nextGiftCheck = GameDriver.time + CHECK_MIN + rand.nextInt(CHECK_SPAN);
        List<Character> cands = new ArrayList<>();
        for (Relationship r : new ArrayList<>(h.relationshipList.relationships)) {
            Character g = Character.charById(r.friendID);
            if (eligibleGiver(h, g, r.value)) cands.add(g);
        }
        for (Character g : kin(h)) if (!cands.contains(g) && eligibleGiver(h, g, 3)) cands.add(g);
        if (cands.isEmpty()) return;
        send(h, cands.get(rand.nextInt(cands.size())));
    }

    private static List<Character> kin(Character h) {
        List<Character> out = new ArrayList<>();
        if (h.spouse != null) out.add(h.spouse);
        if (h.mother != null) out.add(h.mother);
        if (h.father != null) out.add(h.father);
        out.addAll(h.children); out.addAll(h.siblings);
        return out;
    }

    /** A giver is alive, grown, not a person who plays, a best friend or a relative, not already on the way, and has not given lately. */
    static boolean eligibleGiver(Character h, Character g, int bond) {
        if (g == null || g == h || g.dead || !g.born || g.age < 12 || g.player != 0 || g.trait == Character.Traits.Beast || g.trait == Character.Traits.Monster || g.location == null) return false;
        if (bond < 3 && !PlayerLists.isKin(h, g)) return false;
        if (onTheWay(g, h)) return false;
        Integer last = h.pc.giftsFrom == null ? null : h.pc.giftsFrom.get(g.id);
        return last == null || GameDriver.time - last >= GIVER_COOLDOWN;
    }

    static boolean onTheWay(Character g, Character h) {
        if (g.currentQuest != null && "Give Gift".equals(g.currentQuest.name) && g.currentQuest.targetCharacter == h.id) return true;
        for (Quest q : g.targetQuests) if ("Give Gift".equals(q.name) && q.targetCharacter == h.id) return true;
        return false;
    }

    /** The giver gets the quest (and the cooldown starts now). */
    static void send(Character h, Character g) {
        if (h.pc.giftsFrom == null) h.pc.giftsFrom = new java.util.HashMap<>();
        h.pc.giftsFrom.put(g.id, GameDriver.time);
        g.addTarget(new Quest("Give Gift", true, 38, 2, 58, h));
    }

    /** `askgift`: the person who plays asks a best friend or relative to bring a gift (the same quest). Returns what to say. */
    static String ask(Character h, Character g) {
        if (g == null) return "Who? 'askgift #<id>' or part of a name.";
        if (g.player != 0) return g.name + " is another player.";
        Relationship r = h.relationshipList.contains(g.id);
        int bond = r == null ? 0 : r.value;
        if (g.dead || !g.born) return g.name + " is gone.";
        if (bond < 3 && !PlayerLists.isKin(h, g)) return "Only a best friend or a relative will bring you a gift (" + g.name + " is " + PlayerLists.relationOf(h, g, bond) + ").";
        if (onTheWay(g, h)) return g.name + " is already on the way with a gift.";
        Integer last = h.pc.giftsFrom == null ? null : h.pc.giftsFrom.get(g.id);
        if (last != null && GameDriver.time - last < GIVER_COOLDOWN) return g.name + " gave or promised you a gift " + (GameDriver.time - last) + " weeks ago; wait " + (GIVER_COOLDOWN - (GameDriver.time - last)) + " more.";
        send(h, g);
        return g.name + " " + g.lastName + " will bring you a gift: they walk to wherever you are (" + PlayerControl.distance(h.location, g.location) + " tiles away) and give it when they stand in your tile.";
    }

    // ------------------------------------------------------------------ what is given
    private static final long SILVER_SHARE = 20;                   // at most a twentieth of the giver's wealth

    /** The giver hands the receiver a gift from the giver's own holdings. Returns a line for the receiver ("brings you ...") or null when the giver had nothing to give. */
    static String deliver(Character giver, Character rec) {
        String[] order = preference(giver.trait);
        for (String kind : order) {
            String what = give(kind, giver, rec);
            if (what != null) {
                giver.changeRelationship(rec, 1);
                String line = giver.player == 1 ? "You give " + rec.name + " " + rec.lastName + " " + what + "." : giver.name + " " + giver.lastName + " " + (rec.player == 1 ? "brings you " : "gives " + rec.name + " ") + what + ".";
                if (rec.player == 1) { rec.tell(line); Notifications.onGift(rec, giver, line); } else giver.tell(line);
                return line;
            }
        }
        return null;
    }

    /** What a trait gives first, then what any giver falls back to. */
    static String[] preference(Character.Traits t) {
        switch (t) {
            case Gatherer: return new String[]{"herbs", "pill", "silver"};
            case Miner: return new String[]{"metal", "pill", "silver"};
            case Alchemist: return new String[]{"pill", "herbs", "silver"};
            case Blacksmith: return new String[]{"weapon", "metal", "silver"};
            case Tailor: return new String[]{"clothing", "herbs", "silver"};
            case Scribe: return new String[]{"array", "silver", "pill"};
            case Chef: return new String[]{"food", "herbs", "silver"};
            case Drunken: return new String[]{"wine", "silver", "pill"};
            case Poisoner: return new String[]{"poison", "silver", "pill"};
            case Merchant: return new String[]{"silver", "pill"};
            case Leader: return new String[]{"favour", "silver", "pill"};
            case Runner: return new String[]{"news", "silver"};
            case Innovative: return new String[]{"enchant", "weapon", "silver"};
            default: return new String[]{"silver", "pill", "herbs", "metal"};
        }
    }

    private static String give(String kind, Character g, Character r) {
        switch (kind) {
            case "herbs": return stack(g, r, Item.ItemType.Herb);
            case "metal": return stack(g, r, Item.ItemType.Metal);
            case "pill": return pill(g, r);
            case "food": return goods(g, r, Item.ItemType.Food);
            case "wine": return goods(g, r, Item.ItemType.Wine);
            case "poison": return goods(g, r, Item.ItemType.Poison);
            case "weapon": return gear(g, r, true);
            case "clothing": return gear(g, r, false);
            case "array": {
                if (g.arrayBag.isEmpty()) return null;
                Item a = g.arrayBag.remove(0);
                r.arrayBag.add(a);
                return "an array (Lv." + a.lvl + ")";
            }
            case "silver": {
                long wealth = PlayerLists.wealth(g);
                long amount = wealth / SILVER_SHARE;
                if (amount < 100) return null;
                g.deductSs(amount); r.credit(amount);
                return TermFormat.ss(amount) + " in silver";
            }
            case "favour": {
                if (g.sect == null || r.sect != g.sect) return null;
                r.sectPoints += 20;
                return "a good word to the sect (+20 sect points)";
            }
            case "news": {
                int told = 0;
                for (Area a : new ArrayList<>(g.knownLocations)) { if (a != null && r.knownLocations != null && !r.knownLocations.contains(a) && told < 3) { r.learnPlace(a); told++; } }
                return told == 0 ? null : "news of " + told + " place" + (told == 1 ? "" : "s");
            }
            case "enchant": {
                if (r.equippedWeapon == null || r.equippedWeapon.enchantBonus >= 5) return null;
                r.equippedWeapon.enchantBonus++;
                return "an enchantment for your " + r.equippedWeapon.name + " (+1)";
            }
            default: return null;
        }
    }

    /** Food, wine or poison: up to five portions of the giver's best stack of that kind (from the goods of both: nothing is made from nothing). */
    private static String goods(Character g, Character r, Item.ItemType type) {
        Item best = Life.firstOf(g, type);
        if (best == null) return null;
        int n = Math.min(5, best.quantity);
        best.quantity -= n;
        if (best.quantity <= 0) Life.goods(g).remove(best);
        Character.addToBag(Life.goods(r), new Item(type, best.lvl, n, best.name));
        return n + " portion" + (n == 1 ? "" : "s") + " of " + best.name;
    }

    /** Herbs or metal: the giver's richest stack of a quality the receiver can use (up to one above his skill), at most a hundred units. */
    private static String stack(Character g, Character r, Item.ItemType type) {
        int usable = Math.max(3, r.professionLvl / 10 + 2);
        Item best = null;
        for (Item i : g.materialBag) if (i != null && i.itemType == type && i.lvl > 0 && i.lvl <= usable + 1 && i.quantity >= 10 && (best == null || i.lvl > best.lvl)) best = i;
        if (best == null) return null;
        int n = Math.min(100, best.quantity);
        best.quantity -= n;
        if (best.quantity <= 0) g.materialBag.remove(best);
        Character.addToBag(r.materialBag, new Item(type, best.lvl, n));
        return n + " " + (type == Item.ItemType.Herb ? "herbs" : "units of metal") + " (quality " + best.lvl + ")";
    }

    /** A cultivation pill the receiver can use (never above his next realm); a Potential Pill only rarely, from an alchemist who holds one. */
    private static String pill(Character g, Character r) {
        Item pick = null;
        for (Item i : g.pillBag) {
            if (i == null || i.quantity <= 0) continue;
            if (i.pillType == Item.PillType.Potential) { if (g.trait == Character.Traits.Alchemist && rand.nextInt(10) == 0) { pick = i; break; } continue; }
            if (i.lvl <= (r.majorLvl + 1) * 2 + 2 && (pick == null || i.lvl > pick.lvl)) pick = i;
        }
        if (pick == null) return null;
        pick.quantity--;
        Item gift = new Item(Item.ItemType.Pill, pick.pillType, pick.lvl, 1);
        gift.maker = pick.maker; gift.makerID = pick.makerID; gift.brand = pick.brand;
        if (pick.quantity <= 0) g.pillBag.remove(pick);
        Character.addToBag(r.pillBag, gift);
        return "a " + (pick.pillType == Item.PillType.Potential ? "Potential Pill" : "cultivation pill (quality " + pick.lvl + ")");
    }

    /** A weapon or a piece of clothing from the giver's pack that the receiver can wear (his realm up to the next), not the one in use; a smith or a tailor may make one from his own material first. */
    private static String gear(Character g, Character r, boolean weapon) {
        int lo = r.majorLvl * 10 + 1, hi = (r.majorLvl + 1) * 10 + 10;
        Item best = pickGear(g, weapon, lo, hi);
        if (best == null && ((weapon && g.trait == Character.Traits.Blacksmith) || (!weapon && g.trait == Character.Traits.Tailor))) {
            if (weapon) g.smithing(); else g.tailoring();
            best = pickGear(g, weapon, lo, hi);
        }
        if (best == null) return null;
        g.backpack.remove(best);
        Character.addToBag(r.backpack, best);
        return (weapon ? "a weapon: " : "clothing: ") + best.name + " (Lv." + best.lvl + ")";
    }

    private static Item pickGear(Character g, boolean weapon, int lo, int hi) {
        Item best = null;
        for (Item i : g.backpack) {
            if (i == null || i == g.equippedWeapon || i == g.equippedClothing) continue;
            if (weapon ? i.itemType != Item.ItemType.Weapon : i.itemType != Item.ItemType.Clothing) continue;
            if (i.lvl < lo || i.lvl > hi) continue;
            if (best == null || i.lvl > best.lvl) best = i;
        }
        return best;
    }
}
