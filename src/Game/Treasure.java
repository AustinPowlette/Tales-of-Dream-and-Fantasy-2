package Game;

import java.util.ArrayList;

import static Game.GameDriver.rand;

/** Heaven-Sent Treasure: once a year the Heavens drop one treasure on a wild tile, in one of eight kinds (a technique, an instrument, a poison, a Potential Pill, a regular pill,
 *  clothes, a weapon or an array). It is claimed by the "Heaven Treasure" quest; everything but the technique is flagged `heavenSent`, so it can be consigned to a city auction. */
final class Treasure {
    private Treasure() { }

    enum Kind { TECHNIQUE, INSTRUMENT, POISON, POTENTIAL_PILL, PILL, CLOTHES, WEAPON, ARRAY }

    static final int MAX_UNCLAIMED = 12;
    /** Counters for the playtest probes (not saved). */
    static volatile int stSpawned, stClaimed;

    /** Once a year: one treasure descends on a random wild tile (never a city), unless a dozen already wait unclaimed. Returns false when nobody is alive. */
    static boolean yearly() {
        ArrayList<Area> eligible = new ArrayList<>();
        int unclaimed = 0;
        for (Area area : Map.createdAreas) {
            if (area == null) continue;
            if (area.hasHeaven()) unclaimed++;
            else if (!area.city) eligible.add(area);
        }
        if (eligible.isEmpty() || unclaimed >= MAX_UNCLAIMED) return true;
        Character phantom = null;
        int top = 1;
        for (Character c : Map.characters) {
            if (c == null || c.dead || c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) continue;
            if (phantom == null) phantom = c;
            if (c.majorLvl > top) top = c.majorLvl;
        }
        if (phantom == null) return false;
        stSpawned++;
        Area at = eligible.get(rand.nextInt(eligible.size()));
        Kind kind = Kind.values()[rand.nextInt(Kind.values().length)];
        place(at, kind, create(kind, top, phantom));
        for (Character cc : Map.characters) {
            if (cc == null || cc.dead || !cc.born || cc.location == null) continue;
            if (Math.abs(cc.location.x - at.x) + Math.abs(cc.location.y - at.y) <= 20 ) cc.learnPlace(at);
        }
        System.out.println("Heaven's Will descends! A Heaven-Sent " + label(kind) + " has appeared at " + at.name + " [" + at.x + "," + at.y + "]!");
        Chronicle.add("world", "Heaven's Will: a Heaven-Sent " + label(kind) + " descended at " + at.name + ".");
        return true;
    }

    static String label(Kind k) {
        switch (k) {
            case TECHNIQUE: return "technique"; case INSTRUMENT: return "instrument"; case POISON: return "poison"; case POTENTIAL_PILL: return "Potential Pill";
            case PILL: return "pill"; case CLOTHES: return "set of clothes"; case WEAPON: return "weapon"; default: return "array";
        }
    }

    /** The treasure: a Technique or an Item, whichever the kind makes. */
    static Object create(Kind kind, int top, Character phantom) {
        int lvl = Math.min(100, (top + 1) * 10 + rand.nextInt(11)); // gear levels run 1-100, one realm is ten levels
        switch (kind) {
            case TECHNIQUE: {
                Technique t = new Technique(rand.nextInt(2) == 0 ? Technique.techniqueType.Martial : Technique.techniqueType.Movement, phantom, Math.max(5 + rand.nextInt(10), Math.min(100, top * 10 + rand.nextInt(11))));
                t.name = "Heaven-Sent " + t.name;
                return t;
            }
            case WEAPON: return gear(new Item(phantom, Item.WeaponType.values()[rand.nextInt(Item.WeaponType.values().length)], lvl));
            case CLOTHES: return gear(new Item(phantom, lvl, Item.ItemType.Clothing));
            case ARRAY: return gear(new Item(phantom, lvl, Item.ItemType.Array));
            case INSTRUMENT: return gear(new Item(phantom, lvl, Item.ItemType.Instrument));
            case POISON: {
                int pl = Math.min(10, top + 2);
                Item p = new Item(Item.ItemType.Poison, pl, 5, "Heaven-Sent " + Naming.poison(pl));
                p.heavenSent = true;
                return p;
            }
            case POTENTIAL_PILL: {
                Item p = new Item(Item.ItemType.Pill, Item.PillType.Potential, 11, 1 + rand.nextInt(3));
                p.heavenSent = true;
                return p;
            }
            default: {
                Item p = new Item(Item.ItemType.Pill, Item.PillType.Cultivation, Math.min(10, top + 1), 3 + rand.nextInt(3));
                p.heavenSent = true;
                return p;
            }
        }
    }

    private static Item gear(Item it) {
        it.name = "Heaven-Sent " + it.name;
        it.enchantment = "the Heavens' Blessing"; it.enchantBonus = 5;
        it.heavenSent = true;
        return it;
    }

    static void place(Area at, Kind kind, Object treasure) {
        if (treasure instanceof Technique) at.heavenlyTechnique = (Technique) treasure;
        else at.heavenItem = (Item) treasure;
    }

    /** What the claimer got, said however they got there (a walk, a script, AUTO): the log, a notice and, for a human who plays, a window. */
    private static void announce(Character c, String text) {
        System.out.println(text);
        if (c.player == 1) {
            Notifications.postQuiet("fortune", text, -1, c.id); // the text is already in the log
            if (c.isManualPc()) GameDriver.showGuiInfo("Heaven's Treasure", text);
        }
    }

    /** The claimer takes what lies here. Returns true if there was something to take. */
    static boolean claim(Character c, Area a) {
        if (a.hasHeaven()) stClaimed++;
        if (a.heavenlyTechnique != null) {
            Technique t = a.heavenlyTechnique;
            c.techniqueList.add(t);
            a.heavenlyTechnique = null;
            c.equip();
            if (c.player > 0) announce(c, "You claimed the heavenly technique " + t.name + " (potential " + t.potential + ", " + t.getType() + ")! It is in your technique list; 'techniques' shows it.");
            Chronicle.add("world", c.name + " " + c.lastName + " claimed " + t.name + ".", c.id);
            return true;
        }
        if (a.heavenItem == null) return false;
        Item it = a.heavenItem;
        a.heavenItem = null;
        switch (it.itemType) {
            case Pill: Character.addToBag(c.pillBag, it); break;
            case Array: if (c.arrayBag == null) c.arrayBag = new ArrayList<>(); c.arrayBag.add(it); break;
            case Poison: c.backpack.add(it); break; // kept in the backpack so it can be consigned to an auction (the goods bag is for eating and drinking stock)
            default: c.backpack.add(it); c.equip(); break;
        }
        if (c.player > 0) announce(c, "You claimed the Heaven-Sent treasure: " + it.name + (it.quantity > 1 ? " x" + it.quantity : "") + "! It is in your " + (it.itemType == Item.ItemType.Pill ? "pill bag" : it.itemType == Item.ItemType.Array ? "array bag" : "backpack") + ". A strong one can be put up at a city auction ('sell' in a city).");
        Chronicle.add("world", c.name + " " + c.lastName + " claimed " + it.name + ".", c.id);
        return true;
    }
}
