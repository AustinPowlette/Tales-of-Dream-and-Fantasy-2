package Game;

import static Game.GameDriver.rand;

/** Everyone can craft. A character with a trade (Blacksmith, Tailor, Scribe, Alchemist, Chef, Drunken, Poisoner, Musician) crafts its own kind professionally; anybody can also craft any of the
 *  eight kinds as an amateur: it takes twice the material of a trade, uses only plain quality (the best level an amateur reaches is 10 + amateur skill, up to 60, and never above the
 *  character's own realm) and the skill rises slowly with use (capped at 50, a trade goes to 100). An NPC without a weapon or clothes uses it too (`gearUp`). */
final class AmateurCraft {
    private AmateurCraft() { }

    static final String[] KINDS = {"weapon", "clothing", "pill", "array", "food", "wine", "poison"};
    static final int COST = 2 * Character.CRAFT_COST, ARRAY_COST = 2 * Character.ARRAY_MIN, PILL_HERBS = 100, MAX_SKILL = 50;

    /** The best item level this amateur can reach. */
    static int cap(Character c) { return Math.min((c.majorLvl + 1) * 10, 10 + c.amateurLvl); }

    /** The material level an amateur can make use of (higher materials still work, they just do not make better things). */
    static int usable(Character c, int materialLvl) { return Math.max(1, Math.min(materialLvl, c.amateurLvl / 10 + 1)); }

    /** The best stack of `type` (null = any herb or metal) with at least `min` units. */
    static Item pick(Character c, Item.ItemType type, int min) {
        Item best = null;
        for (Item it : c.materialBag) {
            if (it == null || it.quantity < min || it.lvl <= 0) continue;
            if (type == null ? !(it.itemType == Item.ItemType.Herb || it.itemType == Item.ItemType.Metal) : it.itemType != type) continue;
            if (best == null || it.lvl > best.lvl) best = it;
        }
        return best;
    }

    private static int units(Character c, Item.ItemType type) {
        int n = 0;
        for (Item it : c.materialBag) if (it != null && (type == null ? (it.itemType == Item.ItemType.Herb || it.itemType == Item.ItemType.Metal) : it.itemType == type)) n += Math.max(0, it.quantity);
        return n;
    }

    private static void use(Character c, Item stack, int n) {
        stack.quantity -= n;
        if (stack.quantity <= 0) c.materialBag.remove(stack);
    }

    /** The amateur skill grows a little with every item made. */
    static void gain(Character c, int itemLvl) {
        if (c.amateurLvl >= MAX_SKILL) return;
        if (c.amateurExpMax < 1) c.amateurExpMax = 1;
        c.amateurExp += itemLvl * 3;
        while (c.amateurExp > c.amateurExpMax && c.amateurLvl < MAX_SKILL) {
            c.amateurLvl++;
            c.amateurExp -= c.amateurExpMax;
            c.amateurExpMax = Math.pow(10, c.amateurLvl / 10.);
        }
    }

    private static int itemLevel(Character c, Item material) {
        int ml = usable(c, material.lvl);
        int raw = rand.nextInt(ml * 5) + 1 + (ml * 10) / 2;
        return Math.max(1, Math.min(cap(c), raw * 7 / 10));
    }

    private static void say(Character c, String text) { if (c.player > 0 && c.skipping <= 0) System.out.println(text); }

    /** Makes one thing of `kind` as an amateur. Returns null when it was made, otherwise the reason it was not (nothing is used up). */
    static String craft(Character c, String kind) {
        switch (kind) {
            case "weapon": {
                Item m = pick(c, Item.ItemType.Metal, COST);
                if (m == null) return "A weapon takes " + COST + " metal (you carry " + units(c, Item.ItemType.Metal) + "): mine some first.";
                Item w = new Item(c, Item.WeaponType.values()[rand.nextInt(Item.WeaponType.values().length)], itemLevel(c, m));
                use(c, m, COST);
                c.backpack.add(w); c.itemsCrafted++; gain(c, w.lvl); c.equip();
                say(c, "You made " + w + " (amateur work, Lv." + w.lvl + ").");
                return null;
            }
            case "clothing": {
                Item m = pick(c, null, COST);
                if (m == null) return "Clothes take " + COST + " herbs or metal (you carry " + units(c, null) + "): forage or mine some first.";
                Item cl = new Item(c, itemLevel(c, m), Item.ItemType.Clothing);
                use(c, m, COST);
                c.backpack.add(cl); c.itemsCrafted++; gain(c, cl.lvl); c.equip();
                say(c, "You made " + cl + " (amateur work, Lv." + cl.lvl + ").");
                return null;
            }
            case "array": {
                Item m = pick(c, null, ARRAY_COST);
                if (m == null) return "An array takes " + ARRAY_COST + " herbs or metal in one stack (you carry " + units(c, null) + " in all).";
                Item ar = new Item(c, itemLevel(c, m), Item.ItemType.Array);
                use(c, m, COST);
                c.arrayBag.add(ar); c.itemsCrafted++; gain(c, ar.lvl);
                say(c, "You made " + ar + " (amateur work, Lv." + ar.lvl + "). 'install' sets it where you stand.");
                return null;
            }
            case "pill": {
                Item m = pick(c, Item.ItemType.Herb, PILL_HERBS);
                if (m == null) return "A pill takes " + PILL_HERBS + " herbs (you carry " + units(c, Item.ItemType.Herb) + "): forage some first.";
                int lvl = Math.min(10, usable(c, m.lvl));
                use(c, m, PILL_HERBS);
                Character.addToBag(c.pillBag, new Item(Item.ItemType.Pill, Item.PillType.Cultivation, lvl, 1));
                c.itemsCrafted++; gain(c, lvl);
                say(c, "You made a Lv." + lvl + " Cultivation Pill (amateur work: an amateur's pills only reach the realm the skill allows). 'consume' takes the ones you can use.");
                return null;
            }
            case "instrument":
                return "Instruments are not made: a Musician's instrument matures as they play, and a better one may be for sale in a city.";
            case "food": case "wine": case "poison": {
                Item.ItemType t = kind.equals("food") ? Item.ItemType.Food : kind.equals("wine") ? Item.ItemType.Wine : kind.equals("poison") ? Item.ItemType.Poison : Item.ItemType.Instrument;
                int r = Life.craft(c, t, true);
                return r > 0 ? null : "A " + kind + " takes 50 herbs (you carry " + units(c, Item.ItemType.Herb) + "): forage some first.";
            }
            default:
                return "You cannot craft '" + kind + "'. Choose: " + String.join(", ", KINDS) + ".";
        }
    }

    /** The trade this character works for `kind`, or null when it is an amateur at it. */
    private static boolean tradeCovers(Character c, String kind) {
        switch (c.trait) {
            case Blacksmith: return kind.equals("weapon");
            case Tailor: return kind.equals("clothing");
            case Scribe: return kind.equals("array");
            case Alchemist: return kind.equals("pill");
            case Chef: return kind.equals("food");
            case Drunken: return kind.equals("wine");
            case Poisoner: return kind.equals("poison");
            default: return false;
        }
    }

    /** `craft <kind>`: true if something was made (it takes the turn), false with the reason said. */
    static boolean craftCommand(Character c, String kind) {
        if (tradeCovers(c, kind)) { // your own trade: the professional work
            int before = c.itemsCrafted;
            switch (c.trait) {
                case Blacksmith: c.smithing(); break;
                case Tailor: c.tailoring(); break;
                case Scribe: c.array(); break;
                case Alchemist: c.alchemy(); break;
                default: Life.profession(c); break;
            }
            if (c.itemsCrafted == before) { PlayerCommands.say("Your trade has nothing to work with: " + needs(kind) + "."); return false; }
            return true;
        }
        String why = craft(c, kind);
        if (why != null) { PlayerCommands.say(why); return false; }
        return true;
    }

    private static String needs(String kind) {
        switch (kind) {
            case "weapon": return "metal (" + Character.CRAFT_COST + " per weapon)";
            case "clothing": return "herbs or metal (" + Character.CRAFT_COST + " per set)";
            case "array": return "herbs or metal (" + Character.ARRAY_MIN + " in the bag)";
            case "pill": return "herbs (50 per pill) or Blood (1,000 per Potential Pill)";
            default: return "herbs (50 per portion)";
        }
    }

    /** The craft menu for the terminal (the GUI button lists the same kinds). */
    static String menu(Character c) {
        StringBuilder sb = new StringBuilder("=== Craft ===\n");
        sb.append(c.hasTrade() ? "Your trade (" + c.getTrait() + "): 'craft' with nothing after it works it.\n" : "Your trait (" + c.getTrait() + ") has no trade: craft as an amateur.\n");
        sb.append("Amateur skill ").append(c.amateurLvl).append(" of ").append(MAX_SKILL).append(": the best thing you can make is Lv.").append(cap(c)).append(" (it grows slowly with every item).\n");
        sb.append("  craft weapon     ").append(COST).append(" metal        (you carry ").append(units(c, Item.ItemType.Metal)).append(")\n");
        sb.append("  craft clothing   ").append(COST).append(" herbs or metal (you carry ").append(units(c, null)).append(")\n");
        sb.append("  craft array      ").append(ARRAY_COST).append(" herbs or metal in one stack, uses ").append(COST).append("\n");
        sb.append("  craft pill       ").append(PILL_HERBS).append(" herbs        (you carry ").append(units(c, Item.ItemType.Herb)).append("; Potential Pills need an alchemist: 'Refine Blood')\n");
        sb.append("  craft food|wine|poison   50 herbs per portion\n");
        sb.append("Mine and forage first ('mine', 'forage'). Professionals use half the material and reach far better quality.\n");
        return sb.toString();
    }

    /** An NPC (or AUTO character) without a weapon or clothes: make a plain one from what it carries, or go and get the materials where it stands. */
    static void gearUp(Character c) {
        if (c.location == null) return;
        if (c.equippedWeapon == null) {
            if (pick(c, Item.ItemType.Metal, COST) != null) { craft(c, "weapon"); return; }
            if (c.location.metalMaterials > 0) { c.mine(); return; }
        }
        if (c.equippedClothing == null) {
            if (pick(c, null, COST) != null) { craft(c, "clothing"); return; }
            if (c.location.herbMaterials > 0) { c.forage(); return; }
            if (c.location.metalMaterials > 0) c.mine();
        }
    }
}
