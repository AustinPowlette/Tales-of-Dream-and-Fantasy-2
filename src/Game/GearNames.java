package Game;

import java.util.ArrayList;
import java.util.List;

/** One-time repair of gear names in an old world. Until 2026-10-18 the namers read a gear level (1-100, ten levels to a realm) as a realm number, so a level 11 weapon was called "Divine-Grade Void ..." and every
 *  piece of gear above level 8 was top grade. `fixAll` runs when a world is loaded (it changes nothing that is already right, so it can run every time): a weapon, a piece of clothing or an array whose grade or
 *  band of adjectives does not match its level gets a new name made by the current namer (the type word, a "Heaven-Sent " or "Relic of the Footprint: " prefix and everything that is not the name stay as they are). */
final class GearNames {
    private GearNames() { }

    /** Renames the gear of the whole world. Returns how many items were renamed. */
    static int fixAll() {
        int n = 0;
        try {
            for (Character c : new ArrayList<>(Map.characters)) n += fixCharacter(c);
            for (Character c : new ArrayList<>(Map.tempCharacters)) n += fixCharacter(c);
            for (Area a : new ArrayList<>(Map.createdAreas)) {
                if (a == null) continue;
                n += fixAll(a.backpack); n += fixAll(a.arrayBag); n += fixAll(a.auctionBag);
                if (fix(a.heavenItem)) n++;
                if (a.shops != null) for (Shop s : new ArrayList<>(a.shops)) if (s != null) n += fixAll(s.inventory);
                if (a.sect != null) n += fixAll(a.sect.treasury);
            }
        } catch (RuntimeException ignored) { /* a name is a courtesy: never stop a load for it */ }
        if (n > 0) System.out.println("[Gear] " + n + " weapons, garments and arrays were renamed to the grade of their real level.");
        return n;
    }

    private static int fixCharacter(Character c) {
        if (c == null) return 0;
        int n = 0;
        if (fix(c.equippedWeapon)) n++;
        if (fix(c.equippedClothing)) n++;
        n += fixAll(c.backpack); n += fixAll(c.arrayBag);
        return n;
    }

    private static int fixAll(List<Item> bag) {
        int n = 0;
        if (bag != null) for (Item it : new ArrayList<>(bag)) if (fix(it)) n++;
        return n;
    }

    /** True if the item was renamed. */
    static boolean fix(Item it) {
        if (it == null || it.name == null || it.lvl <= 0) return false;
        if (it.itemType != Item.ItemType.Weapon && it.itemType != Item.ItemType.Clothing && it.itemType != Item.ItemType.Array) return false;
        int realm = Naming.gearRealm(it.lvl);
        String oldGrade = Naming.grade(it.lvl), newGrade = Naming.grade(realm);
        if (oldGrade.equals(newGrade) && Naming.bandOf(it.lvl) == Naming.bandOf(realm)) return false; // already right
        String n = it.name;
        String lead = oldGrade + "-Grade ";
        int at = n.indexOf(lead);
        if (at < 0) return false; // not named by the old rule: leave it
        String head = n.substring(0, at); // "Heaven-Sent ", "Relic of the Footprint: " ... stays as it is
        String rest = n.substring(at + lead.length());
        String type = null;
        if (it.itemType == Item.ItemType.Weapon) { String w = it.getWeaponType(); if (w != null && rest.endsWith(w)) type = w; }
        else {
            String[] types = it.itemType == Item.ItemType.Clothing ? Dictionary.clothingNames : Dictionary.arrayTypes;
            for (String t : types) if (t != null && rest.endsWith(t) && (type == null || t.length() > type.length())) type = t;
        }
        if (type == null) return false;
        String fresh = it.itemType == Item.ItemType.Weapon ? Naming.weapon(it.lvl, type) : it.itemType == Item.ItemType.Clothing ? Naming.clothing(it.lvl, type) : Naming.array(it.lvl, type);
        it.name = (head + fresh).intern();
        return true;
    }
}
