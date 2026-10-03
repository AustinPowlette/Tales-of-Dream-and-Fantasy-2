package Game;

import java.util.ArrayList;
import java.util.List;

/** Text sheets of the info commands (places, relationships, bag, shops, market, library): headings, aligned tables, closing hint line.
 *  Each returns the finished text; PlayerCommands sends it to the console or the GUI info window. */
final class TermView {
    private TermView() { }

    private static final int PLACES_SHOWN = 30, RELATIONS_SHOWN = 30;

    static String places(Character c) {
        List<Area> known = new ArrayList<>();
        for (Area a : new ArrayList<>(c.knownLocations)) if (a != null) known.add(a);
        known.sort((x, y) -> Integer.compare(PlayerControl.distance(c.location, x), PlayerControl.distance(c.location, y)));
        StringBuilder sb = new StringBuilder("=== Known Locations (").append(known.size()).append(") ===\n");
        TermFormat.Table tb = new TermFormat.Table("Place", "Coords", "Dist", "Type", "Notes").raw(2).max(0, 30).max(4, 70);
        for (Area a : known.subList(0, Math.min(PLACES_SHOWN, known.size()))) {
            StringBuilder notes = new StringBuilder();
            if (a.sect != null) notes.append("sect ").append(a.sect.name).append("  ");
            if (a.cityOwner != null) notes.append("lord ").append(a.cityOwner.name).append("  ");
            if (a.dungeonType != null) notes.append(a.dungeonType.toString().toLowerCase().replace('_', ' ')).append("  ");
            if (a == c.location) notes.append("(you are here)");
            tb.row(a.name, PlayerControl.coord(a), PlayerControl.distance(c.location, a), PlayerLists.placeType(a), notes.toString().trim());
        }
        sb.append(tb.render());
        if (known.size() > PLACES_SHOWN) sb.append("... ").append(known.size() - PLACES_SHOWN).append(" more (farther away): 'list places 100' or 'list places type=city' shows them.\n");
        sb.append(TermFormat.hint("goto <x> <y> walks there ('route <x> <y>' previews), 'map' draws them, 'list places type=city sort=dist' filters"));
        return sb.toString();
    }

    private static String bondLabel(int v) {
        return v >= 3 ? "Best Friend" : v == 2 ? "Close Friend" : v == 1 ? "Friend" : v == 0 ? "Acquaintance" : v == -1 ? "Dislike" : v == -2 ? "Rival" : "Arch-Enemy";
    }

    static String relationships(Character c) {
        List<Relationship> rels = new ArrayList<>(c.relationshipList.relationships);
        rels.sort((x, y) -> Integer.compare(Math.abs(y.value), Math.abs(x.value)));
        StringBuilder sb = new StringBuilder("=== Relationships ===\n");
        TermFormat.Table tb = new TermFormat.Table("ID", "Name", "Bond", "Value", "Lv", "Where", "Dist").raw(0, 3, 4, 6).max(1, 30).max(5, 40);
        int shown = 0, total = 0;
        for (Relationship rel : rels) {
            Character f = Character.charById(rel.friendID);
            if (f == null || f.dead) continue;
            total++;
            if (shown >= RELATIONS_SHOWN) continue;
            shown++;
            tb.row(f.id, f.name + " " + f.lastName, bondLabel(rel.value), rel.value, f.lvl, f.location == null ? "?" : f.location.name + " " + PlayerControl.coord(f.location), f.location == null ? 0 : PlayerControl.distance(c.location, f.location));
        }
        if (total == 0) sb.append("  (nobody yet: talk to people with 'people' then 'talk 1'; friendships and feuds are listed here)\n");
        else {
            sb.append(tb.render());
            if (total > shown) sb.append("... ").append(total - shown).append(" more: 'relations 100' shows the strongest bonds in detail.\n");
        }
        if (c.spouse != null) sb.append("Spouse: ").append(c.spouse.name).append(" ").append(c.spouse.lastName).append("\n");
        if (total > 0) sb.append(TermFormat.hint("seek <id> friend|fight|spar sets a quest to reach one of them, 'chase #<id>' follows, 'family' shows your kin"));
        return sb.toString();
    }

    static String bag(Character c) {
        StringBuilder sb = new StringBuilder("=== Inventory ===\nBackpack:");
        if (c.backpack.isEmpty()) sb.append(" (empty)\n");
        else {
            sb.append("\n");
            TermFormat.Table tb = new TermFormat.Table("#", "Item", "Lv", "Value", "Note").indent("  ").raw(0, 2).max(1, 44);
            for (int i = 0; i < c.backpack.size(); i++) {
                Item it = c.backpack.get(i);
                if (it == null) continue;
                tb.row(i + 1, it.name == null ? "?" : it.name + (it.quantity > 1 ? " x" + it.quantity : ""), it.lvl, Character.itemPrice(it),
                        it == c.equippedWeapon || it == c.equippedClothing ? "equipped" : (it.enchantment != null ? "[" + it.enchantment + " +" + it.enchantBonus + "]" : ""));
            }
            sb.append(tb.render());
        }
        sb.append("Materials: ").append(c.materialBag.isEmpty() ? "(none)" : c.materialBag.toString())
                .append("\nPills: ").append(c.pillBag.isEmpty() ? "(none)" : c.pillBag.toString())
                .append("\nArrays: ").append(c.arrayBag.isEmpty() ? "(none)" : c.arrayBag.toString())
                .append("\nTechniques: ").append(c.techniqueList.isEmpty() ? "(none)" : c.techniqueList.toString()).append("\n");
        sb.append(TermFormat.hint("equip <#> wears an item, compare <#> shows the difference, sell <#> sells it in a city, gift <person> <#> gives it away, consume takes pills"));
        return sb.toString();
    }

    static String shops(Character c) {
        StringBuilder sb = new StringBuilder("=== Your shops ===\n");
        if (c.ownedShops.isEmpty()) sb.append("  (none: 'do Restock Trip' in a city opens one if you are a merchant; 'list shop' shows what the city's shops sell)\n");
        else {
            TermFormat.Table tb = new TermFormat.Table("#", "Shop", "Lv", "Stock", "Pays sellers", "Charges", "Restocks at").raw(0, 2, 3).max(1, 40);
            for (int i = 0; i < c.ownedShops.size(); i++) {
                Shop s = c.ownedShops.get(i);
                tb.row(i + 1, (s.name == null || s.name.isEmpty() ? "" : s.name + " @ ") + s.location.name, s.level(), s.inventory.size(),
                        String.format("%.2fx", s.buyRate), String.format("%.2fx", s.sellRate), String.format("%.2fx", s.cityBuyRate));
            }
            sb.append(tb.render());
        }
        sb.append("Set rates with: price <shop#> buy|sell|city <rate>\n");
        return sb.toString();
    }

    static String market(Character c) {
        Area a = c.location;
        StringBuilder sb = new StringBuilder("=== Goods market of " + a.name + " ===\n");
        List<Life.Offer> offers = Life.offers(a);
        if (offers.isEmpty()) sb.append("  (the shelves are empty: Chefs, brewers and Poisoners sell here)\n");
        else {
            TermFormat.Table tb = new TermFormat.Table("#", "Type", "Item", "Lv", "Qty", "ss each", "Seller").raw(0, 3).max(2, 30).max(6, 30);
            int i = 1;
            for (Life.Offer o : offers) tb.row(i++, o.stack.itemType, o.stack.name, o.stack.lvl, o.stack.quantity, o.price, o.shop == null ? "city market" : o.shop.name);
            sb.append(tb.render());
        }
        sb.append("Buy with: buy market <#> [qty]   Sell yours with: sell goods|food|wine|poison [qty]   Your wealth: ").append(TermFormat.ss(PlayerLists.wealth(c))).append("\n");
        return sb.toString();
    }

    static final java.util.Set<String> LIB_KEYS = new java.util.HashSet<>(java.util.Arrays.asList("name", "type", "lvl", "potential", "price", "idx"));

    /** `library [limit] [words] [type=martial|movement] [potential>=30] [price<=5k] [sort=potential:desc]`: the numbers are the library's own, so `learn <#>` works on a filtered list. */
    static String library(Character c, String[] args) {
        Library lib = c.location.libraries.get(0);
        StringBuilder sb = new StringBuilder("=== " + (lib.name == null || lib.name.isEmpty() ? "Library" : lib.name) + " (" + lib.techniques.size() + " techniques, one for each type and potential) ===\n");
        if (lib.techniques.isEmpty()) { sb.append("  (the shelves are empty for now)\n"); return sb.toString(); }
        ListQuery q = ListQuery.parse(args, 1);
        String err = q.validate(LIB_KEYS);
        if (err != null) return err + "\n";
        java.util.List<Integer> idx = new java.util.ArrayList<>();
        for (int i = 0; i < lib.techniques.size(); i++) idx.add(i);
        final Library L = lib;
        int[] total = new int[1];
        java.util.List<Integer> shown = q.apply(idx, i -> { Technique t = L.techniques.get(i); java.util.Map<String, Object> f = new java.util.HashMap<>(); f.put("name", t.name); f.put("type", t.getType()); f.put("lvl", t.lvl); f.put("potential", t.potential); f.put("price", L.buyPrice(t)); f.put("idx", i + 1); return f; },
                (a, b) -> Integer.compare(L.techniques.get(a).potential, L.techniques.get(b).potential), Math.max(10, c.pc().listLimit), total);
        TermFormat.Table tb = new TermFormat.Table("#", "Technique", "Type", "Lv", "Potential", "Price (ss)").raw(0, 3, 4).max(1, 40);
        for (int i : shown) { Technique t = lib.techniques.get(i); tb.row(i + 1, t.name, t.getType(), t.lvl, t.potential, (long) lib.buyPrice(t)); }
        sb.append(tb.render());
        if (total[0] > shown.size()) sb.append("  (").append(total[0] - shown.size()).append(" more: narrow it down, e.g.  library type=martial potential>=40 sort=price)\n");
        sb.append("Your wealth: ").append(TermFormat.ss(PlayerLists.wealth(c))).append("\n");
        sb.append(TermFormat.hint("learn <#> buys a copy.  Filters: library [limit] [words] type=martial|movement potential>=30 price<=5k sort=potential:desc"));
        return sb.toString();
    }
}
