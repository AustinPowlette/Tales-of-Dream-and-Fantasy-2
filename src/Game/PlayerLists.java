package Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Selectable, filterable, sortable lists for human characters: cultivators/monsters in the tile, shop stock, the backpack, known places.
 *  Used by the terminal `list` command and by the GUI browser window (both call the same code, so they always agree). */
public class PlayerLists {

    /** One rendered list: table headers + rows, the object each row stands for, and how many matched before the cap. */
    static class ListView {
        String title = "";
        String[] headers = new String[0];
        ArrayList<Object[]> rows = new ArrayList<>();
        ArrayList<Object> refs = new ArrayList<>();
        int total = 0;
        String note = "";
        String error = null;
        String[] sortKeys = new String[0]; // keys usable in sort= for this list

        /** Terminal text: heading, an aligned table (numbers right-aligned, thousands separators on money/quantity columns), then the note/hints. */
        String toText() {
            if (error != null) return error;
            StringBuilder sb = new StringBuilder("=== " + title + " (showing " + rows.size() + " of " + total + ") ===\n");
            if (rows.isEmpty()) { if (!(headers.length <= 1 && !note.isEmpty())) sb.append("  (nothing matches)\n"); } // a note alone explains an empty shop list (no city / no shops)
            else {
                TermFormat.Table tb = new TermFormat.Table(headers);
                for (int i = 0; i < headers.length; i++) if (!SEPARATED.contains(headers[i])) tb.raw(i); // ids, levels, ages and list numbers stay plain
                for (Object[] r : rows) tb.row(r);
                sb.append(tb.render());
            }
            if (total > rows.size()) sb.append("... ").append(total - rows.size()).append(" more — raise the limit (e.g. add 100, or 'limit 100') or narrow the filter.\n");
            if (!note.isEmpty()) sb.append(note).append("\n");
            return sb.toString();
        }

        private static final Set<String> SEPARATED = new HashSet<>(Arrays.asList("Price", "Value", "Wealth", "Qty", "Beasts", "Pop"));
    }

    /** Shop stock entry: which shop, which item, its position for `buy S.I`. */
    static class ShopItem {
        final Shop shop; final Item item; final int shopNo, itemNo;
        ShopItem(Shop shop, Item item, int shopNo, int itemNo) { this.shop = shop; this.item = item; this.shopNo = shopNo; this.itemNo = itemNo; }
    }

    static final Set<String> PEOPLE_KEYS = keys("id", "name", "lvl", "major", "age", "aff", "trait", "sect", "rel", "gender", "kills", "target");
    static final Set<String> ITEM_KEYS = keys("shop", "type", "sub", "name", "lvl", "price", "qty", "owner", "afford");
    static final Set<String> KNOWN_KEYS = keys("id", "name", "lvl", "major", "age", "aff", "trait", "sect", "rel", "gender", "kills", "dist", "where", "relation");
    static final Set<String> WORLD_KEYS = keys("id", "name", "lvl", "major", "age", "aff", "trait", "sect", "gender", "kills", "wealth", "dist", "where");
    static final Set<String> BAG_KEYS = keys("type", "sub", "name", "lvl", "price", "qty", "equipped");
    static final Set<String> PLACE_KEYS = keys("name", "dist", "lvl", "type", "pop", "beasts", "x", "y", "noted", "vein");

    private static Set<String> keys(String... k) { return new LinkedHashSet<>(Arrays.asList(k)); }

    static String itemType(Item i) { return i.itemType == null ? "?" : i.itemType.name().toLowerCase(); }
    static String itemSub(Item i) {
        if (i.weaponType != null) return i.weaponType.name().toLowerCase();
        if (i.pillType != null) return i.pillType.name().toLowerCase();
        return "";
    }

    // ------------------------------------------------------------------ people / monsters in the tile
    static ListView people(Character c, ListQuery q, boolean monsters, int defaultLimit) {
        ListView v = new ListView();
        v.title = monsters ? "Monsters here" : "Cultivators here";
        v.sortKeys = PEOPLE_KEYS.toArray(new String[0]);
        String err = q.validate(PEOPLE_KEYS);
        if (err != null) { v.error = err; return v; }

        List<Character> src = new ArrayList<>();
        for (int attempt = 0; attempt < 3; attempt++) { // the game thread may change the tile while the GUI reads it
            try { src = new ArrayList<>(c.location.characters); break; } catch (java.util.ConcurrentModificationException e) { src = new ArrayList<>(); }
        }
        List<Character> here = new ArrayList<>();
        for (Character p : src) {
            if (p == null || p.dead || p == c || p.trait == Character.Traits.Beast) continue;
            boolean isMonster = p.trait == Character.Traits.Monster;
            if (isMonster != monsters) continue;
            here.add(p);
        }
        final HashSet<Integer> targets = new HashSet<>();
        for (Quest qu : new ArrayList<>(c.targetQuests)) if (qu.category == Quest.QuestCategory.Character) targets.add(qu.targetCharacter);
        if (c.currentQuest != null && c.currentQuest.category == Quest.QuestCategory.Character) targets.add(c.currentQuest.targetCharacter);

        java.util.function.Function<Character, Map<String, Object>> fields = p -> {
            Map<String, Object> f = new HashMap<>();
            Relationship r = c.relationshipList.contains(p.id);
            f.put("id", p.id);
            f.put("name", p.name + " " + p.lastName);
            f.put("lvl", p.lvl);
            f.put("major", p.majorLvl);
            f.put("age", p.age);
            f.put("aff", p.affiliation);
            f.put("trait", p.getTrait());
            f.put("sect", p.sect != null ? p.sect.name : "none");
            f.put("rel", r != null ? r.value : 0);
            f.put("gender", p.gender);
            f.put("kills", p.kills);
            f.put("target", targets.contains(p.id) ? 1 : 0);
            return f;
        };
        // default: quest targets first, then people you know, then by level
        Comparator<Character> order = Comparator.<Character>comparingInt(p -> targets.contains(p.id) ? 0 : 1)
                .thenComparingInt(p -> c.relationshipList.contains(p.id) != null ? 0 : 1)
                .thenComparingInt(p -> -p.lvl);
        int[] total = {0};
        List<Character> shown = q.apply(here, fields, order, defaultLimit, total);
        v.total = total[0];
        v.headers = new String[]{"#", "ID", "Name", "Lv", "Aff", "Trait", "Sect", "Rel", "Notes"};
        for (int i = 0; i < shown.size(); i++) {
            Character p = shown.get(i);
            Relationship r = c.relationshipList.contains(p.id);
            String notes = (targets.contains(p.id) ? "quest target " : "") + (p.partyLeaderId == c.id ? "your party " : "") + (p.spouse == c ? "spouse" : "");
            v.rows.add(new Object[]{i + 1, p.id, p.name + " " + p.lastName + (p.repTitle != null ? " '" + p.repTitle + "'" : ""), p.lvl, p.affiliation, p.getTrait(),
                    p.sect != null ? p.sect.name : "-", r != null ? r.value : 0, notes.trim()});
            v.refs.add(p);
        }
        if (monsters) v.note = "Wild beasts in this tile: " + c.location.monsters + " (area Lv " + c.location.lvl + ", strongest " + c.location.highestLvl + ") — fight them with 'hunt'.";
        return v;
    }

    // ------------------------------------------------------------------ shop stock in the current city
    static ListView shopItems(Character c, ListQuery q, int defaultLimit) {
        ListView v = new ListView();
        v.title = "Shop stock in " + c.location.name;
        v.sortKeys = ITEM_KEYS.toArray(new String[0]);
        String err = q.validate(ITEM_KEYS);
        if (err != null) { v.error = err; return v; }
        if (!c.location.city || c.location.shops.isEmpty()) { v.note = c.location.city ? "This city has no shops." : "You are not in a city."; v.headers = new String[]{"#"}; return v; }
        List<ShopItem> all = new ArrayList<>();
        try {
            for (int s = 0; s < c.location.shops.size(); s++) {
                Shop shop = c.location.shops.get(s);
                List<Item> inv = new ArrayList<>(shop.inventory);
                for (int i = 0; i < inv.size(); i++) if (inv.get(i) != null && inv.get(i).itemType != null) all.add(new ShopItem(shop, inv.get(i), s + 1, shop.inventory.indexOf(inv.get(i)) + 1));
            }
        } catch (java.util.ConcurrentModificationException ignored) { /* partial list: the GUI refreshes again */ }
        java.util.function.Function<ShopItem, Map<String, Object>> fields = si -> {
            Map<String, Object> f = new HashMap<>();
            long price = si.shop.sellPrice(si.item);
            f.put("shop", si.shopNo);
            f.put("type", itemType(si.item));
            f.put("sub", itemSub(si.item));
            f.put("name", si.item.name == null ? "" : si.item.name);
            f.put("lvl", si.item.lvl);
            f.put("price", price);
            f.put("qty", si.item.quantity);
            f.put("owner", si.shop.owner != null ? si.shop.owner.name + " " + si.shop.owner.lastName : "none");
            f.put("afford", c.canAfford(price) ? 1 : 0);
            return f;
        };
        Comparator<ShopItem> order = Comparator.<ShopItem>comparingInt(si -> -si.item.lvl).thenComparingLong(si -> si.shop.sellPrice(si.item));
        int[] total = {0};
        List<ShopItem> shown = q.apply(all, fields, order, defaultLimit, total);
        v.total = total[0];
        v.headers = new String[]{"#", "Ref", "Type", "Name", "Lv", "Price", "Shop (owner)", "Afford"};
        for (int i = 0; i < shown.size(); i++) {
            ShopItem si = shown.get(i);
            long price = si.shop.sellPrice(si.item);
            v.rows.add(new Object[]{i + 1, si.shopNo + "." + si.itemNo, itemType(si.item) + (itemSub(si.item).isEmpty() ? "" : "/" + itemSub(si.item)), si.item.name, si.item.lvl, price,
                    shopLabel(si.shop), c.canAfford(price) ? "yes" : "no"});
            v.refs.add(si);
        }
        v.note = "Buy with 'buy <#>' (this listing's number) or 'buy <Ref> <name>'. Your wealth: " + wealth(c) + " ss.";
        return v;
    }

    // ------------------------------------------------------------------ the people you have a bond with, or only your family
    /** What this person is to `c`: spouse, mother, father, child, sibling, or the bond ("best friend" ... "arch-enemy"). */
    static String relationOf(Character c, Character o, int bond) {
        if (c.spouse == o) return "spouse";
        if (c.mother == o) return "mother";
        if (c.father == o) return "father";
        if (c.children.contains(o)) return "child";
        if (c.siblings.contains(o)) return "sibling";
        return Family.label(bond);
    }

    static boolean isKin(Character c, Character o) { return c.spouse == o || c.mother == o || c.father == o || c.children.contains(o) || c.siblings.contains(o); }

    /** Everyone alive with whom `c` has a bond (and the family, bond or not); `family` keeps the family only. */
    static ListView known(Character c, ListQuery q, boolean family, int defaultLimit) {
        ListView v = new ListView();
        v.title = family ? "Family" : "People you know";
        v.sortKeys = KNOWN_KEYS.toArray(new String[0]);
        String err = q.validate(KNOWN_KEYS);
        if (err != null) { v.error = err; return v; }
        final Map<Integer, Integer> bond = new HashMap<>();
        List<Character> all = new ArrayList<>();
        for (Relationship r : new ArrayList<>(c.relationshipList.relationships)) {
            Character o = Character.charById(r.friendID);
            if (o == null || o.dead || o == c || bond.containsKey(o.id)) continue;
            bond.put(o.id, r.value);
            if (!family || isKin(c, o)) all.add(o);
        }
        List<Character> kin = new ArrayList<>();
        if (c.spouse != null) kin.add(c.spouse);
        if (c.mother != null) kin.add(c.mother);
        if (c.father != null) kin.add(c.father);
        kin.addAll(c.children); kin.addAll(c.siblings);
        for (Character o : kin) if (o != null && !o.dead && o != c && !bond.containsKey(o.id)) { bond.put(o.id, 0); all.add(o); }
        java.util.function.Function<Character, Map<String, Object>> fields = p -> {
            Map<String, Object> f = new HashMap<>();
            int b = bond.containsKey(p.id) ? bond.get(p.id) : 0;
            f.put("id", p.id); f.put("name", p.name + " " + p.lastName); f.put("lvl", p.lvl); f.put("major", p.majorLvl); f.put("age", p.age);
            f.put("aff", p.affiliation); f.put("trait", p.getTrait()); f.put("sect", p.sect != null ? p.sect.name : "none"); f.put("rel", b); f.put("gender", p.gender);
            f.put("kills", p.kills); f.put("dist", PlayerControl.distance(c.location, p.location)); f.put("where", p.location != null ? p.location.name : "?");
            f.put("relation", relationOf(c, p, b));
            return f;
        };
        // default: family first, then the strongest bonds (friends and enemies alike), then by level
        Comparator<Character> order = Comparator.<Character>comparingInt(p -> isKin(c, p) ? 0 : 1)
                .thenComparingInt(p -> -Math.abs(bond.containsKey(p.id) ? bond.get(p.id) : 0)).thenComparingInt(p -> -p.lvl);
        int[] total = {0};
        List<Character> shown = q.apply(all, fields, order, defaultLimit, total);
        v.total = total[0];
        v.headers = new String[]{"#", "ID", "Name", "Lv", "Aff", "Trait", "Sect", "Rel", "Relation", "Where", "Dist"};
        for (int i = 0; i < shown.size(); i++) {
            Character p = shown.get(i);
            int b = bond.containsKey(p.id) ? bond.get(p.id) : 0;
            v.rows.add(new Object[]{i + 1, p.id, p.name + " " + p.lastName + (p.repTitle != null ? " '" + p.repTitle + "'" : ""), p.lvl, p.affiliation, p.getTrait(), p.sect != null ? p.sect.name : "-", b, relationOf(c, p, b),
                    p.location != null ? p.location.name + " " + PlayerControl.coord(p.location) : "?", PlayerControl.distance(c.location, p.location)});
            v.refs.add(p);
        }
        v.note = family ? "Your spouse, parents, children and siblings." : "Everyone you have a bond with, family included.";
        return v;
    }

    // ------------------------------------------------------------------ anyone in the world (search)
    static ListView world(Character c, ListQuery q, int defaultLimit) {
        ListView v = new ListView();
        v.title = "Cultivators anywhere";
        v.sortKeys = WORLD_KEYS.toArray(new String[0]);
        String err = q.validate(WORLD_KEYS);
        if (err != null) { v.error = err; return v; }
        List<Character> all = new ArrayList<>();
        int n = Game.Map.characters.size();
        for (int i = 0; i < n; i++) { // index loop: the list only grows while the GUI reads it
            Character p = Game.Map.characters.get(i);
            if (p == null || p.dead || !p.born || p == c || p.trait == Character.Traits.Beast) continue;
            all.add(p);
        }
        java.util.function.Function<Character, Map<String, Object>> fields = p -> {
            Map<String, Object> f = new HashMap<>();
            f.put("id", p.id); f.put("name", p.name + " " + p.lastName); f.put("lvl", p.lvl); f.put("major", p.majorLvl); f.put("age", p.age);
            f.put("aff", p.affiliation); f.put("trait", p.getTrait()); f.put("sect", p.sect != null ? p.sect.name : "none"); f.put("gender", p.gender);
            f.put("kills", p.kills); f.put("wealth", wealth(p)); f.put("dist", PlayerControl.distance(c.location, p.location));
            f.put("where", p.location != null ? p.location.name : "?");
            return f;
        };
        int[] total = {0};
        List<Character> shown = q.apply(all, fields, Comparator.<Character>comparingInt(p -> -p.lvl), defaultLimit, total);
        v.total = total[0];
        v.headers = new String[]{"#", "ID", "Name", "Lv", "Major", "Age", "Sect", "Where", "Dist", "Wealth"};
        for (int i = 0; i < shown.size(); i++) {
            Character p = shown.get(i);
            v.rows.add(new Object[]{i + 1, p.id, p.name + " " + p.lastName + (p.repTitle != null ? " '" + p.repTitle + "'" : ""), p.lvl, p.majorLvl, p.age,
                    p.sect != null ? p.sect.name : "-", p.location != null ? p.location.name + " " + PlayerControl.coord(p.location) : "?", PlayerControl.distance(c.location, p.location), wealth(p)});
            v.refs.add(p);
        }
        v.note = "Use the ID with #id commands (talk #id needs the same tile), 'seek <id> friend|fight|spar', 'preview #id', 'follow #id', 'control #id'.";
        return v;
    }

    // ------------------------------------------------------------------ backpack
    static ListView bag(Character c, ListQuery q, int defaultLimit) {
        ListView v = new ListView();
        v.title = "Backpack";
        v.sortKeys = BAG_KEYS.toArray(new String[0]);
        String err = q.validate(BAG_KEYS);
        if (err != null) { v.error = err; return v; }
        List<Item> all = new ArrayList<>();
        for (Item i : c.backpack) if (i != null && i.itemType != null) all.add(i);
        java.util.function.Function<Item, Map<String, Object>> fields = i -> {
            Map<String, Object> f = new HashMap<>();
            f.put("type", itemType(i));
            f.put("sub", itemSub(i));
            f.put("name", i.name == null ? "" : i.name);
            f.put("lvl", i.lvl);
            f.put("price", Character.itemPrice(i));
            f.put("qty", i.quantity);
            f.put("equipped", i == c.equippedWeapon || i == c.equippedClothing ? 1 : 0);
            return f;
        };
        int[] total = {0};
        List<Item> shown = q.apply(all, fields, Comparator.<Item>comparingInt(i -> -i.lvl), defaultLimit, total);
        v.total = total[0];
        v.headers = new String[]{"#", "Type", "Name", "Lv", "Value", "Qty", "Note"};
        for (Item i : shown) {
            v.rows.add(new Object[]{c.backpack.indexOf(i) + 1, itemType(i) + (itemSub(i).isEmpty() ? "" : "/" + itemSub(i)), i.name, i.lvl, Character.itemPrice(i), i.quantity,
                    i == c.equippedWeapon || i == c.equippedClothing ? "equipped" : ""});
            v.refs.add(i);
        }
        v.note = "'#' is the backpack slot: 'sell <#>' sells that item in a city, 'gift <person> <#>' gives it away. Materials and pills: 'bag'.";
        return v;
    }

    // ------------------------------------------------------------------ known places
    static ListView places(Character c, ListQuery q, int defaultLimit) {
        ListView v = new ListView();
        v.title = "Known places";
        v.sortKeys = PLACE_KEYS.toArray(new String[0]);
        String err = q.validate(PLACE_KEYS);
        if (err != null) { v.error = err; return v; }
        List<Area> all = new ArrayList<>();
        for (Area a : new ArrayList<>(c.knownLocations)) if (a != null) all.add(a);
        java.util.function.Function<Area, Map<String, Object>> fields = a -> {
            Map<String, Object> f = new HashMap<>();
            f.put("name", a.name);
            f.put("dist", PlayerControl.distance(c.location, a));
            f.put("lvl", a.lvl);
            f.put("type", placeType(a));
            f.put("pop", a.city ? a.population : 0);
            f.put("beasts", a.monsters);
            f.put("noted", Chronicle.notes.containsKey(Chronicle.tileKey(a)) ? 1 : 0);
            f.put("vein", a.spiritualVein ? 1 : 0);
            f.put("x", a.x * 100);
            f.put("y", a.y * 100);
            return f;
        };
        int[] total = {0};
        List<Area> shown = q.apply(all, fields, Comparator.<Area>comparingInt(a -> PlayerControl.distance(c.location, a)), defaultLimit, total);
        v.total = total[0];
        v.headers = new String[]{"#", "Name", "Coords", "Dist", "Lv", "Type", "What", "Beasts", "Note"};
        for (int i = 0; i < shown.size(); i++) {
            Area a = shown.get(i);
            String note = Chronicle.notes.get(Chronicle.tileKey(a));
            v.rows.add(new Object[]{i + 1, a.name, PlayerControl.coord(a), PlayerControl.distance(c.location, a), a.lvl, placeType(a), placeWhat(a), a.monsters, note == null ? "" : note});
            v.refs.add(a);
        }
        v.note = "Go with 'goto <x> <y>' using the coordinates shown; 'note tile <x> <y> <text>' marks a place, 'list places noted=1' lists the marked ones.";
        return v;
    }

    /** What is at a place, in a few words: city and population, sect, kind of dungeon, spirit vein, or the biome of a wild tile. */
    static String placeWhat(Area a) {
        StringBuilder sb = new StringBuilder();
        if (a.city) sb.append("city, pop ").append(a.population);
        if (a.sect != null) sb.append(sb.length() > 0 ? " / " : "").append("sect ").append(a.sect.name);
        if (a.dungeonType != null) sb.append(sb.length() > 0 ? " / " : "").append(a.dungeonType.name().toLowerCase().replace('_', ' '));
        if (a.spiritualVein) sb.append(sb.length() > 0 ? " / " : "").append("spirit vein");
        if (sb.length() == 0) sb.append(a.biomem).append(a.river ? ", river" : "");
        return sb.toString();
    }

    static String placeType(Area a) {
        if (a.city) return "city";
        if (a.sect != null) return "sect";
        if (a.dungeonType != null) return "dungeon";
        return "wild";
    }

    static String shopLabel(Shop s) {
        String owner = s.owner != null ? s.owner.name + " " + s.owner.lastName : "none";
        return s.name == null || s.name.isEmpty() ? owner : s.name + " (" + owner + ")";
    }

    static long wealth(Character c) { return c.ss + c.sc * 1_000_000L + c.dc * 1_000_000_000_000L; }

    /** The list for a `list <kind>` word (people, monsters, shop, bag, places, world). */
    static ListView view(Character c, String kind, ListQuery q, int defaultLimit) {
        switch (kind) {
            case "monsters": return people(c, q, true, defaultLimit);
            case "shop": return shopItems(c, q, defaultLimit);
            case "bag": return bag(c, q, defaultLimit);
            case "places": return places(c, q, defaultLimit);
            case "world": return world(c, q, defaultLimit);
            default: return people(c, q, false, defaultLimit);
        }
    }

    /** Which list a `list <what>` word means (null if none). */
    static String kind(String w) {
        switch (w.toLowerCase()) {
            case "people": case "players": case "player": case "characters": case "cultivators": case "npcs": return "people";
            case "monsters": case "monster": case "beasts": return "monsters";
            case "shop": case "shops": case "items": case "stock": case "market": return "shop";
            case "bag": case "inventory": case "backpack": return "bag";
            case "places": case "place": case "known": case "locations": return "places";
            case "world": case "everyone": case "anyone": case "all": return "world";
            default: return null;
        }
    }
}
