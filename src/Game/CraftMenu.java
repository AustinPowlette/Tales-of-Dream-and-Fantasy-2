package Game;

import java.util.ArrayList;
import java.util.List;

/** The crafting menu of a trade (Blacksmith, Tailor, Scribe, Alchemist): the person who plays chooses WHAT to make from WHICH stack instead of leaving it to the game. The level of a thing follows its
 *  material (an item is made in the upper half of the quality's realm, limited by the skill and by the maker's own realm; a pill has exactly the quality of its herb), so the choice that matters is the stack,
 *  and for a weapon its kind. One craft is one turn: one weapon, one piece of clothing or one array (the turn's skill allows several pills). `craft list` shows the options, `craft do <n> [kind | count]`
 *  makes one; the Craft window (`GuiCraft`) is the same menu. NPCs and AUTO keep the game's choice (`Character.smithing()` and friends). */
final class CraftMenu {
    private CraftMenu() { }

    static final class Option {
        final String kind;        // weapon, clothing, array, pill, potential
        final Item stack;
        final String what;        // "Weapon", "Clothing", "Array", "Pill", "Potential Pill"
        final int minLvl, maxLvl; // the level of what comes out (a pill: its quality, twice)
        final int upTo;           // how many the stack allows (a turn makes one gear item, or up to this many pills)
        final String warn;        // a warning to show with the row (null = none)
        Option(String kind, Item stack, String what, int minLvl, int maxLvl, int upTo, String warn) {
            this.kind = kind; this.stack = stack; this.what = what; this.minLvl = minLvl; this.maxLvl = maxLvl; this.upTo = upTo; this.warn = warn;
        }
        String material() { return (stack.itemType == Item.ItemType.Blood ? "Blood" : stack.itemType == Item.ItemType.Metal ? "metal" : "herbs") + (stack.itemType == Item.ItemType.Blood ? "" : " quality " + stack.lvl); }
        String makes() { return minLvl == maxLvl ? "Lv " + minLvl : "Lv " + minLvl + "-" + maxLvl; }
    }

    static boolean hasMenu(Character c) {
        switch (c.trait) { case Blacksmith: case Tailor: case Scribe: case Alchemist: return true; default: return false; }
    }

    /** The cap on an item's level: the maker's realm and 100. */
    private static int realmCap(Character c) { return Math.min((c.majorLvl + 1) * 10, 100); }

    /** What this person can make right now, stack by stack (best material first). */
    static List<Option> options(Character c) {
        List<Option> out = new ArrayList<>();
        if (!hasMenu(c)) return out;
        final int skillCap = c.professionLvl / 10 + 2;
        List<Item> stacks = new ArrayList<>(c.materialBag);
        stacks.sort((a, b) -> Integer.compare(b.lvl, a.lvl));
        for (Item s : stacks) {
            if (s == null || s.quantity <= 0) continue;
            switch (c.trait) {
                case Blacksmith:
                    if (s.itemType == Item.ItemType.Metal && s.lvl > 0 && s.quantity >= Character.CRAFT_COST) out.add(gear("weapon", "Weapon", c, s, skillCap, s.quantity / Character.CRAFT_COST));
                    break;
                case Tailor:
                    if ((s.itemType == Item.ItemType.Metal || s.itemType == Item.ItemType.Herb) && s.lvl > 0 && s.quantity >= Character.CRAFT_COST) out.add(gear("clothing", "Clothing", c, s, skillCap, s.quantity / Character.CRAFT_COST));
                    break;
                case Scribe:
                    if ((s.itemType == Item.ItemType.Metal || s.itemType == Item.ItemType.Herb) && s.lvl > 0 && s.lvl <= skillCap && s.quantity >= Character.ARRAY_MIN)
                        out.add(gear("array", "Array", c, s, s.lvl, (s.quantity - Character.ARRAY_MIN) / Character.CRAFT_COST + 1));
                    break;
                case Alchemist:
                    if (s.itemType == Item.ItemType.Herb && s.lvl > 0 && s.lvl <= skillCap && s.quantity >= 50)
                        out.add(new Option("pill", s, "Pill", s.lvl, s.lvl, Math.min(s.quantity / 50, c.professionLvl / 10 + 1), null));
                    else if (s.itemType == Item.ItemType.Blood && s.quantity >= 1000)
                        out.add(new Option("potential", s, "Potential Pill", 0, 0, Math.min(s.quantity / 1000, c.professionLvl / 10 + 1), "Potential Pills are made of the blood of the fallen: making them makes you Evil."));
                    break;
                default: break;
            }
        }
        return out;
    }

    private static Option gear(String kind, String what, Character c, Item s, int skillCap, int upTo) {
        int ml = Math.min(s.lvl, skillCap);
        int lo = Math.min(1 + (ml * 10) / 2, realmCap(c)), hi = Math.min(ml * 10, realmCap(c));
        String warn = s.lvl > skillCap ? "your skill uses this material as quality " + skillCap + " (the rest is wasted)" : null;
        return new Option(kind, s, what, lo, Math.max(lo, hi), upTo, warn);
    }

    static String listText(Character c, List<Option> opts) {
        if (!hasMenu(c)) return "Your trade (" + c.getTrait() + ") makes things without a choice: 'craft' does a turn of it. 'craft menu' lists what anybody can make as an amateur.";
        StringBuilder sb = new StringBuilder("=== What your trade can make now (profession Lv." + c.professionLvl + ", realm cap Lv." + realmCap(c) + ") ===\n");
        if (opts.isEmpty()) sb.append("  Nothing: you hold no stack big enough (" + (c.trait == Character.Traits.Alchemist ? "50 herbs of one quality, or 1,000 Blood" : c.trait == Character.Traits.Scribe ? Character.ARRAY_MIN + " of one stack" : Character.CRAFT_COST + " of one stack") + "). Gather (forage / mine) or buy at a market.\n");
        for (int i = 0; i < opts.size(); i++) {
            Option o = opts.get(i);
            sb.append(String.format("%2d. %-14s from %-18s (%d units)  makes %-10s up to %d%s%n", i + 1, o.what, o.material(), o.stack.quantity, o.kind.equals("potential") ? "potential" : o.makes(), o.upTo, o.warn == null ? "" : "   ! " + o.warn));
        }
        sb.append(c.trait == Character.Traits.Blacksmith ? "'craft do <n> [sword|saber|staff|spear|fan]' makes one weapon (a turn; no kind = a random one).\n" : c.trait == Character.Traits.Alchemist ? "'craft do <n> [count]' brews that many pills (a turn; the most you can by default).\n" : "'craft do <n>' makes one (a turn).\n");
        return sb.toString();
    }

    static Item.WeaponType weaponType(String s) {
        if (s == null) return null;
        for (Item.WeaponType w : Item.WeaponType.values()) if (w.name().equalsIgnoreCase(s)) return w;
        return null;
    }

    /** `craft do <n> [arg]`. True = a turn was spent. */
    static boolean craft(Character c, String[] t) {
        List<Option> opts = options(c);
        if (!hasMenu(c)) { System.out.println(listText(c, opts)); return false; }
        int n = t.length > 2 ? PlayerCommands.num(t[2], -1) : -1;
        if (n < 1 || n > opts.size()) { System.out.println(opts.isEmpty() ? listText(c, opts) : "Usage: craft do <number from 'craft list'> [kind | count]"); return false; }
        Option o = opts.get(n - 1);
        String arg = t.length > 3 ? t[3] : null;
        switch (o.kind) {
            case "weapon": {
                Item.WeaponType type = weaponType(arg);
                if (arg != null && type == null) { System.out.println("Weapon kinds: sword, saber, staff, spear, fan."); return false; }
                int before = c.backpack.size();
                c.smithing(type, o.stack);
                return done(c, c.backpack.size() > before);
            }
            case "clothing": {
                int before = c.backpack.size();
                c.tailoring(o.stack);
                return done(c, c.backpack.size() > before);
            }
            case "array": {
                int before = c.arrayBag.size();
                c.array(o.stack);
                if (c.arrayBag.size() > before) System.out.println("You carry " + c.arrayBag.size() + " array(s). 'install' sets one in this place.");
                return done(c, c.arrayBag.size() > before);
            }
            default: {
                int want = arg == null ? o.upTo : PlayerCommands.num(arg, -1);
                if (want < 1) { System.out.println("How many pills? 1 to " + o.upTo + "."); return false; }
                int made = c.alchemyOf(o.stack, want);
                if (made > 0) System.out.println("You brew " + made + " " + (o.kind.equals("potential") ? "Potential Pill(s)" : "pill(s) of quality " + o.minLvl) + " ('bag' shows them)." + (made < want ? " (A turn's skill and the stack allow " + made + ".)" : ""));
                return done(c, made > 0);
            }
        }
    }

    private static boolean done(Character c, boolean made) {
        if (!made) System.out.println("Nothing came of it: that stack cannot be used (any more).");
        return made;
    }
}
