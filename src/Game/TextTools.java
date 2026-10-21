package Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

/** Terminal-side tools that the GUI shares: ASCII world map, gear comparison, technique sheet, chronicle search, route preview, time sheet,
 *  notes/bookmarks, the balance dashboard and per-topic help. All run on the game thread (command handlers), so they read the world freely. */
final class TextTools {
    private TextTools() { }

    static void say(String s) { System.out.println(s); }
    /** A multi-line sheet: printed in the console, a dialog in the GUI (title = its first line). */
    static void show(String s) { GameDriver.showGuiInfo(s.split("\n", 2)[0].replace("=", "").trim(), s); }

    static final String[] SEASONS = {"Spring", "Summer", "Autumn", "Winter"};

    // ------------------------------------------------------------------ map
    static char terrain(Area a) {
        if (a.river) return '=';
        if (a.elev > BiomeGen.HIGH && !"Ocean".equals(a.biomem)) return '^';
        switch (a.biomem) {
            case "Ocean": return '~'; case "Beach": return ':'; case "Forest": case "Taiga": return 'f'; case "Grassland": case "Frozen Grassland": return 'p'; case "Savanna": return 's';
            case "Desert": return 'd'; case "Tundra": case "Frozen Desert": return 't'; default: return '.';
        }
    }

    static char marker(Area a, Character viewer) {
        if (a.city) return 'C';
        if (a.sect != null) return 'S';
        if (a.dungeonType != null) {
            switch (a.dungeonType) {
                case LOST_RUINS: return 'R'; case BEAST_LAIR: return 'B'; case MARTIAL_TRIAL: return 'M';
                case HAUNTED_GROUNDS: return 'H'; case CELESTIAL_TRIAL: return 'T'; default: return '?';
            }
        }
        if (a.spiritualVein) return 'v';
        return 0;
    }

    private static char layer(Area a, String mode) {
        switch (mode) {
            case "level": return (char) ('0' + Math.min(9, a.lvl / 10));
            case "beasts": return (char) ('0' + Math.min(9, (int) Math.log10(a.monsters + 1)));
            case "people": return (char) ('0' + Math.min(9, (int) Math.log10(a.characters.size() + 1) * 3));
            case "sect": return a.sect != null ? 'S' : a.city ? 'C' : '.';
            default: return terrain(a);
        }
    }

    /** `map` (whole world) or `map <radius>` (a square around you). North is up; x grows to the right. Only tiles you know are drawn. */
    static String map(Character c, String[] t) {
        int r = Map.range;
        int radius = r;
        String mode = "biome";
        for (int i = 1; i < t.length; i++) {
            if (t[i].matches("\\d+")) radius = Math.max(2, Math.min(r, TermFormat.parseInt(t[i], r)));
            else if (java.util.Arrays.asList("biome", "level", "beasts", "people", "sect").contains(t[i].toLowerCase())) mode = t[i].toLowerCase();
        }
        int x0 = 0, x1 = r - 1, y0 = 0, y1 = r - 1;
        if (radius < r) { x0 = Math.max(0, c.location.x - radius); x1 = Math.min(r - 1, c.location.x + radius); y0 = Math.max(0, c.location.y - radius); y1 = Math.min(r - 1, c.location.y + radius); }
        java.util.Set<Area> known = new java.util.HashSet<>(c.knownLocations);
        java.util.Set<String> noted = new java.util.HashSet<>(Chronicle.notes.keySet());
        List<Character> humans = Notifications.humans();
        StringBuilder sb = new StringBuilder("=== World map (x " + x0 * 100 + "-" + x1 * 100 + ", y " + y0 * 100 + "-" + y1 * 100 + "; north is up, one tile = one week's walk) ===\n");
        sb.append("     ");
        for (int x = x0; x <= x1; x++) sb.append(x % 5 == 0 ? String.format("%-2d", x) : "  ");
        sb.append("  (x tile)\n");
        for (int y = y1; y >= y0; y--) {
            sb.append(String.format("%3d  ", y));
            for (int x = x0; x <= x1; x++) {
                Area a = Map.tiles[x][y];
                char ch;
                boolean here = c.location.x == x && c.location.y == y;
                boolean other = false;
                for (Character h : humans) if (h != null && h != c && !h.dead && h.location != null && h.location.x == x && h.location.y == y) other = true;
                if (here) ch = '@';
                else if (other) ch = 'P';
                else if (a == null) ch = ' ';
                else if (!known.contains(a)) ch = '·';
                else { char m = marker(a, c); ch = m != 0 ? m : noted.contains(Chronicle.tileKey(a)) ? '*' : layer(a, mode); }
                sb.append(ch).append(' ');
            }
            sb.append('\n');
        }
        if (!mode.equals("biome")) sb.append("  layer '").append(mode).append("': ").append(mode.equals("level") ? "digit = tile level / 10" : mode.equals("beasts") ? "digit = log10 of the beasts there (0 = few, 9 = swarms)" : mode.equals("people") ? "digit = how crowded (0 empty ... 9 packed)" : "S sects, C cities").append("   (map biome|level|beasts|people|sect [radius])\n");
        sb.append("  @ you   P other player   C city   S sect   R ruins   B beast lair   M martial trial   H haunted   T celestial trial   v spirit vein   * your note\n");
        sb.append("  terrain: f forest/taiga  p grass  s savanna  d desert  t tundra/ice  ^ highland  = river  ~ sea  : beach   · seen by someone but not by you   (blank: nobody has been there)\n");
        sb.append("  Cities / sects you know: ");
        int n = 0;
        for (Area a : c.knownLocations) {
            if (a == null || (!a.city && a.sect == null)) continue;
            if (n++ > 0) sb.append(", ");
            sb.append(a.city ? a.name : a.sect.name).append(" ").append(PlayerControl.coord(a));
            if (n >= 12) { sb.append(", ..."); break; }
        }
        if (n == 0) sb.append("none yet");
        sb.append("\n  Use 'route <x> <y>' to preview a walk, 'goto <x> <y>' to take it.\n");
        return sb.toString();
    }

    // ------------------------------------------------------------------ gear
    static int power(Character c, Item weapon, Item cloth) {
        int sum = c.lvl + (cloth != null ? cloth.lvl : 0) + (weapon != null ? weapon.lvl + weapon.enchantBonus : 0)
                + (c.martialTechnique != null ? c.martialTechnique.lvl : 0) + (c.movementTechnique != null ? c.movementTechnique.lvl : 0);
        return sum / 5;
    }

    static Item bagItem(Character c, String arg) {
        int n = PlayerCommands.num(arg, -1);
        if (n < 1 || n > c.backpack.size()) { say("No backpack item #" + arg + ". See 'bag'."); return null; }
        return c.backpack.get(n - 1);
    }

    /** `compare <bag#>`: what swapping that item in would do to your fighting level. */
    static void compare(Character c, String[] t) {
        if (t.length < 2) { say("Usage: compare <bag#>   (numbers from 'bag')"); return; }
        Item it = bagItem(c, t[1]);
        if (it == null) return;
        if (it.itemType != Item.ItemType.Weapon && it.itemType != Item.ItemType.Clothing) { say(it.name + " is not wearable gear."); return; }
        boolean weapon = it.itemType == Item.ItemType.Weapon;
        Item cur = weapon ? c.equippedWeapon : c.equippedClothing;
        int before = power(c, c.equippedWeapon, c.equippedClothing);
        int after = weapon ? power(c, it, c.equippedClothing) : power(c, c.equippedWeapon, it);
        StringBuilder sb = new StringBuilder("=== Compare ===\n");
        sb.append("Equipped: ").append(cur == null ? "(nothing)" : cur).append("\n");
        sb.append("Candidate: ").append(it).append("\n");
        sb.append("Item level ").append(cur == null ? 0 : cur.lvl + (weapon ? cur.enchantBonus : 0)).append(" -> ").append(it.lvl + (weapon ? it.enchantBonus : 0))
          .append("   fighting level ").append(before).append(" -> ").append(after)
          .append(after > before ? "  (better)" : after < before ? "  (worse)" : "  (no change)").append("\n");
        show(sb.toString());
    }

    /** `equip <bag#>`: put that item on, the old one goes to the backpack. (The game re-optimises gear on its own, so a lower-level pick is swapped back.) */
    static boolean equipItem(Character c, String[] t) {
        Item it = bagItem(c, t[1]);
        if (it == null) return false;
        boolean weapon = it.itemType == Item.ItemType.Weapon;
        if (!weapon && it.itemType != Item.ItemType.Clothing) { say(it.name + " is not wearable gear."); return false; }
        Item cur = weapon ? c.equippedWeapon : c.equippedClothing;
        if (cur != null && cur.lvl + (weapon ? cur.enchantBonus : 0) > it.lvl + (weapon ? it.enchantBonus : 0))
            say("Note: that is weaker than " + cur.name + "; the game keeps the strongest gear on, so it may be swapped back on a later turn.");
        c.backpack.remove(it);
        if (cur != null) c.backpack.add(cur);
        if (weapon) c.equippedWeapon = it; else c.equippedClothing = it;
        say("You equip " + it + ".");
        return false; // changing clothes takes no time
    }

    static void techniques(Character c) {
        StringBuilder sb = new StringBuilder("=== Techniques ===\n");
        sb.append("Martial:   ").append(slot(c.martialTechnique)).append("\n");
        sb.append("  second:  ").append(slot(c.martialTechnique2)).append("\n");
        sb.append("Movement:  ").append(slot(c.movementTechnique)).append("\n");
        sb.append("  second:  ").append(slot(c.movementTechnique2)).append("\n");
        sb.append("Known (").append(c.techniqueList.size()).append("):\n");
        int kn = 0;
        for (Technique q : c.techniqueList) sb.append("  ").append(++kn).append(") ").append(q.getType()).append("  ").append(q.name).append(q.element() >= 0 ? " [" + Elements.name(q.element()) + "]" : "").append("  lvl ").append(q.lvl).append(" / potential ").append(q.potential)
                .append(q.maker == null || q.maker.isEmpty() ? "" : "  (by " + q.maker + ")").append("\n");
        if (c.techniqueList.isEmpty()) sb.append("  (none learned: libraries sell copies, dungeons and tombs hold others — 'library', 'learn <n>')\n");
        if (c.player == 1 && c.pc != null && c.pc.mode == PlayerControl.Mode.MANUAL && !Settings.autoEquipManual) sb.append("Your slots stay as you put them: 'swaptech martial|movement', 'usetech <n>', 'forget ...'; 'equip' sorts them for you. A technique levels up as you fight and meditate.\n");
        else sb.append("The strongest technique of each kind is used automatically; a technique levels up as you fight and meditate.\n");
        show(sb.toString());
    }

    private static String slot(Technique q) { return q == null ? "(empty)" : q.name + (q.element() >= 0 ? " [" + Elements.name(q.element()) + "]" : " [follows your path]") + "  lvl " + q.lvl + " / potential " + q.potential; }

    // ------------------------------------------------------------------ chronicle
    /** `history [text|#id] [kind=...] [n]`. Without text: the newest world events. */
    static void history(Character c, String[] t) {
        String kind = "all";
        int limit = 25;
        List<String> words = new ArrayList<>();
        for (int i = 1; i < t.length; i++) {
            if (t[i].startsWith("kind=")) kind = t[i].substring(5).toLowerCase();
            else if (t[i].matches("\\d+") && i == t.length - 1 && words.size() > 0) limit = Math.max(1, Math.min(200, TermFormat.parseInt(t[i], 200)));
            else if (t[i].matches("\\d+") && words.isEmpty() && t.length == 2) limit = Math.max(1, Math.min(200, TermFormat.parseInt(t[i], 200)));
            else words.add(t[i]);
        }
        String q = String.join(" ", words);
        List<Chronicle.Entry> list = Chronicle.search(q, kind, limit);
        java.util.Collections.reverse(list); // oldest first reads like a story
        StringBuilder sb = new StringBuilder("=== Chronicle" + (q.isEmpty() ? "" : " — " + q) + (kind.equals("all") ? "" : " [" + kind + "]") + " (" + list.size() + " entries, kinds: " + String.join("/", Chronicle.KINDS) + ") ===\n");
        sb.append(Chronicle.render(list));
        if (list.isEmpty()) sb.append("(nothing recorded yet — deaths of Major Lvl 3+, breakthroughs to Core Formation and beyond, new cities and sects, wars, calamities and legends are kept)\n");
        show(sb.toString());
    }

    /** `journal [n]`: your own story: what the chronicle says about you, then your latest notices. */
    static void journal(Character c, String[] t) {
        int n = t.length > 1 ? Math.max(1, Math.min(100, PlayerCommands.num(t[1], 20))) : 20;
        StringBuilder sb = new StringBuilder("=== Journal of " + c.name + " " + c.lastName + " ===\n");
        sb.append("Age ").append(c.age).append(", Lvl ").append(c.lvl).append(", ").append(c.kills).append(" kills");
        if (c.sect != null) sb.append(", ").append(c.sect.getRank(c)).append(" of ").append(c.sect.name);
        sb.append("\n\nIn the chronicle:\n");
        List<Chronicle.Entry> mine = Chronicle.about(c);
        if (mine.isEmpty()) sb.append("  (nothing yet)\n");
        for (int i = Math.max(0, mine.size() - n); i < mine.size(); i++) sb.append("  ").append(mine.get(i)).append("\n");
        sb.append("\nRecent notices:\n");
        synchronized (Notifications.history) {
            int shown = 0;
            for (int i = Notifications.history.size() - 1; i >= 0 && shown < n; i--) {
                Notifications.Note x = Notifications.history.get(i);
                if (x.forId >= 0 && x.forId != c.id) continue;
                sb.append(String.format("  wk %-6d [%s] %s%n", x.week, x.cat, x.text)); shown++;
            }
            if (shown == 0) sb.append("  (none)\n");
        }
        List<int[]> life = Chronicle.bioOf(c.id);
        if (!life.isEmpty()) {
            sb.append("\nLife so far (year: level, wealth, friends, kills):\n");
            int step = Math.max(1, life.size() / 12);
            for (int i = 0; i < life.size(); i += step) { int[] s = life.get(i); sb.append(String.format("  year %-5d lvl %-4d wealth 10^%.1f ss  friends %-3d kills %d%n", s[0], s[1], s[2] / 100.0, s[3], s[4])); }
        }
        show(sb.toString());
    }

    // ------------------------------------------------------------------ route / time / watch
    static void route(Character c, String[] t) {
        if (t.length < 3) { say("Usage: route <x> <y>   (same coordinates as goto)"); return; }
        int tx = (int) PlayerScript.tile(PlayerCommands.num(t[1], -1)), ty = (int) PlayerScript.tile(PlayerCommands.num(t[2], -1));
        if (tx < 0 || tx >= Map.range || ty < 0 || ty >= Map.range) { say("Those coordinates are off the map."); return; }
        int x = c.location.x, y = c.location.y;
        int steps = 0, unknown = 0, hot = 0, maxLvl = 0, worst = 0;
        List<String> via = new ArrayList<>();
        c.tempLvlCheck();
        int power = c.tempLvl;
        while ((x != tx || y != ty) && steps < 500) {
            int dx = tx - x, dy = ty - y;
            if (Math.abs(dx) >= Math.abs(dy)) x += dx > 0 ? 1 : -1; else y += dy > 0 ? 1 : -1;
            steps++;
            Area a = Map.tiles[x][y];
            if (a == null) { unknown++; continue; }
            if (a.hotspot || a.crowdHotspot) hot++;
            if (a.lvl > maxLvl) maxLvl = a.lvl;
            if (a.highestLvl > worst) worst = a.highestLvl;
            if (a.city || a.sect != null) via.add((a.city ? a.name : a.sect.name) + " " + PlayerControl.coord(a));
        }
        int now = GameDriver.time;
        int arrive = now + steps;
        StringBuilder sb = new StringBuilder("=== Route " + PlayerControl.coord(c.location) + " -> [" + tx * 100 + ", " + ty * 100 + "] ===\n");
        sb.append("Distance ").append(steps).append(" tiles = ").append(steps).append(" weeks (the walk moves along x first when it is the longer leg, then y).\n");
        sb.append("Arrive in year ").append(arrive / 52).append(", week ").append(arrive % 52 + 1).append(" (").append(SEASONS[(arrive % 52) / 13 % 4]).append(").\n");
        sb.append("Along the way: ").append(unknown).append(" unexplored tiles, ").append(hot).append(" hotspot/crowded tiles; tile level up to ").append(maxLvl)
          .append(", strongest beast on the road ").append(worst).append(" (your fighting level ").append(power).append(")").append(worst > power * 2 ? "  — dangerous!" : "").append(".\n");
        if (!via.isEmpty()) sb.append("Passing: ").append(String.join(", ", via.subList(0, Math.min(8, via.size())))).append(via.size() > 8 ? ", ..." : "").append("\n");
        if (steps == 0) sb.append("You are already there.\n");
        show(sb.toString());
    }

    static void time(Character c) {
        int wk = GameDriver.time;
        int inYear = wk % 52;
        StringBuilder sb = new StringBuilder("=== Time ===\n");
        sb.append("Year ").append(wk / 52).append(", week ").append(inYear + 1).append(" of 52   (").append(SEASONS[inYear / 13 % 4]).append(", ").append(13 - inYear % 13).append(" weeks left of the season)\n");
        sb.append("Total weeks: ").append(wk).append("\n");
        sb.append("Next yearly tax and settlement round: in ").append(inYear == 0 ? 0 : 52 - inYear).append(" weeks.\n");
        if (Auctions.hasHouse(c.location)) { int aw = Auctions.weeksToNext(c.location); sb.append("Next auction at ").append(c.location.name).append(": ").append(aw < 0 ? "nothing on the block" : "in about " + aw + " weeks").append(".\n"); }
        else sb.append("Cities of 500 or more hold an auction when ").append(Auctions.LOTS_TO_HOLD).append(" lots wait, or ").append(Auctions.MAX_WAIT).append(" weeks after the first lot arrives.\n");
        sb.append("Your birthday: in ").append(Math.max(0, 52 - c.time)).append(" weeks (you are ").append(c.age).append(" of ").append(c.maxAge).append(").\n");
        if (c.location.tournamentTimer > 0) sb.append("A tournament at ").append(c.location.name).append(" ends in ").append(c.location.tournamentTimer).append(" weeks.\n");
        show(sb.toString());
    }

    /** `watch [weeks] [kind...]`: let time pass until something happens to you (a notice, an alert, an attack) or the weeks run out. */
    static boolean watch(Character c, String[] t) {
        int weeks = t.length > 1 ? Math.max(1, Math.min(2600, PlayerCommands.num(t[1], 52))) : 52;
        PlayerControl pc = c.pc();
        pc.watchCats = null;
        for (int i = 1; i < t.length; i++) {
            if (t[i].equalsIgnoreCase("for") && i + 1 < t.length) {
                java.util.Set<String> cats = new java.util.HashSet<>();
                for (String s : t[i + 1].toLowerCase().split(",")) if (!s.trim().isEmpty()) cats.add(s.trim());
                for (String s : cats) { boolean known = false; for (String k : Notifications.CATEGORIES) if (k.equals(s)) known = true; if (!known) { say("Unknown notice category '" + s + "'. Categories: " + String.join(", ", Notifications.CATEGORIES)); return false; } }
                pc.watchCats = cats;
            } else if (t[i].equalsIgnoreCase("until") && i + 2 < t.length && t[i + 1].equalsIgnoreCase("year")) {
                int y = PlayerCommands.num(t[i + 2], -1);
                if (y * 52 > GameDriver.time) weeks = Math.min(2600, y * 52 - GameDriver.time); else { say("That year has already passed."); return false; }
            }
        }
        pc.watching = true;
        c.movement = 'x';
        c.skipping = weeks - 1;
        say("Watching for up to " + weeks + " weeks: " + (pc.watchCats == null ? "any notice, quest alert or attack" : "an attack or a notice of kind " + String.join("/", pc.watchCats)) + " stops it.");
        c.movement('x');
        return true;
    }

    /** A person by part of a name: first among the people you have a bond with, then anyone alive in the world (null when none or when several living people of your bonds match too loosely). */
    static Character findKnownPerson(Character me, String fragment) {
        if (fragment == null || fragment.trim().isEmpty()) return null;
        String f = fragment.trim().toLowerCase();
        for (Relationship r : new ArrayList<>(me.relationshipList.relationships)) {
            Character o = Character.charById(r.friendID);
            if (o != null && !o.dead && o != me && (o.name + " " + o.lastName).toLowerCase().contains(f)) return o;
        }
        for (Character o : Map.characters) if (o != null && !o.dead && o.born && o != me && o.trait != Character.Traits.Beast && o.trait != Character.Traits.Monster && (o.name + " " + o.lastName).toLowerCase().contains(f)) return o;
        return null;
    }

    // ------------------------------------------------------------------ notes
    static void note(Character c, String[] t) {
        if (t.length < 2) { say("Usage: note <text>  (marks this tile) | note #<id> <text>  (marks a cultivator) | notes | unnote <n>"); return; }
        String key, text;
        if (t[1].equalsIgnoreCase("tile") && t.length >= 4) { // note tile <x> <y> [text]: a place anywhere (the Places window uses this)
            int x = TermFormat.parseInt(t[2], -1), y = TermFormat.parseInt(t[3], -1);
            if (x < 0 || y < 0 || x >= Map.range || y >= Map.range || Map.tiles[x][y] == null) { say("No such tile: " + t[2] + " " + t[3] + " (tile numbers, not the [x, y] coordinates)."); return; }
            key = Chronicle.tileKey(Map.tiles[x][y]);
            text = t.length > 4 ? String.join(" ", Arrays.copyOfRange(t, 4, t.length)) : "marked";
        } else if (t[1].matches("#\\d+")) {
            Character p = Character.charById(TermFormat.parseInt(t[1].substring(1), -1));
            if (p == null) { say("No such cultivator: " + t[1] + " (find ids with 'people' or 'find <name>')."); return; }
            key = Chronicle.charKey(p);
            text = t.length > 2 ? String.join(" ", Arrays.copyOfRange(t, 2, t.length)) : "bookmarked";
        } else {
            key = Chronicle.tileKey(c.location);
            text = String.join(" ", Arrays.copyOfRange(t, 1, t.length));
        }
        Chronicle.notes.put(key, text);
        say("Noted: " + Chronicle.describeKey(key) + " — " + text);
    }

    static List<String> noteKeys() { synchronized (Chronicle.notes) { return new ArrayList<>(Chronicle.notes.keySet()); } }

    static void notes() {
        StringBuilder sb = new StringBuilder("=== Notes & bookmarks ===\n");
        List<String> keys = noteKeys();
        for (int i = 0; i < keys.size(); i++) sb.append(String.format("%2d. %s — %s%n", i + 1, Chronicle.describeKey(keys.get(i)), Chronicle.notes.get(keys.get(i))));
        if (keys.isEmpty()) sb.append("(none) — 'note <text>' marks the tile you stand on; 'note #<id> <text>' a cultivator.\n");
        show(sb.toString());
    }

    static void unnote(String[] t) {
        if (t.length >= 4 && t[1].equalsIgnoreCase("tile")) { // unnote tile <x> <y>
            int x = TermFormat.parseInt(t[2], -1), y = TermFormat.parseInt(t[3], -1);
            if (x < 0 || y < 0 || x >= Map.range || y >= Map.range || Map.tiles[x][y] == null) { say("No such tile."); return; }
            say(Chronicle.notes.remove(Chronicle.tileKey(Map.tiles[x][y])) != null ? "Removed." : "That place has no note.");
            return;
        }
        List<String> keys = noteKeys();
        int n = t.length > 1 ? PlayerCommands.num(t[1], -1) : -1;
        if (n < 1 || n > keys.size()) { say("Usage: unnote <n>  (see 'notes')"); return; }
        Chronicle.notes.remove(keys.get(n - 1));
        say("Removed.");
    }

    // ------------------------------------------------------------------ balance dashboard
    static class Dash {
        int living, cultivators, children, beasts, monsters, cities, sects;
        int[] realm = new int[11];
        double p10, p50, p90, p99, max, top1Share;
        long totalAge;
        java.util.Map<String, Integer> deaths = new TreeMap<>();
        int week;
    }

    static Dash dash() {
        Dash d = new Dash();
        d.week = GameDriver.time;
        List<Double> wealth = new ArrayList<>();
        for (int i = 0; i < Map.characters.size(); i++) {
            Character c = Map.characters.get(i);
            if (c == null || c.dead) continue;
            d.living++;
            if (c.trait == Character.Traits.Beast) { d.beasts++; continue; }
            if (c.trait == Character.Traits.Monster) { d.monsters++; continue; }
            if (!c.born || c.age < 8) { d.children++; continue; }
            d.cultivators++;
            d.realm[Math.max(0, Math.min(10, c.majorLvl))]++;
            d.totalAge += c.age;
            wealth.add(c.ss + c.sc * 1e6 + c.dc * 1e12 + c.ij * 1e18);
        }
        for (Area a : Map.createdAreas) { if (a == null) continue; if (a.city) d.cities++; if (a.sect != null) d.sects++; }
        if (!wealth.isEmpty()) {
            java.util.Collections.sort(wealth);
            int n = wealth.size();
            d.p10 = wealth.get(n / 10); d.p50 = wealth.get(n / 2); d.p90 = wealth.get(Math.min(n - 1, n * 9 / 10)); d.p99 = wealth.get(Math.min(n - 1, n * 99 / 100)); d.max = wealth.get(n - 1);
            double total = 0, top = 0;
            for (double w : wealth) total += w;
            for (int i = n - Math.max(1, n / 100); i < n; i++) top += wealth.get(i);
            d.top1Share = total > 0 ? top / total : 0;
        }
        synchronized (Chronicle.deathCauses) { d.deaths.putAll(Chronicle.deathCauses); }
        return d;
    }

    static String bar(double frac, int width) {
        int n = (int) Math.round(Math.max(0, Math.min(1, frac)) * width);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < width; i++) sb.append(i < n ? '#' : '.');
        return sb.toString();
    }

    static String si(double v) {
        if (v < 1000) return String.valueOf((long) v);
        String[] u = {"k", "M", "B", "T", "Q"};
        int i = -1;
        while (v >= 1000 && i < u.length - 1) { v /= 1000; i++; }
        return String.format("%.1f%s", v, u[i]);
    }

    static String dashText(Dash d) {
        StringBuilder sb = new StringBuilder("=== World dashboard — year " + d.week / 52 + " ===\n");
        sb.append(String.format("Living %d  (cultivators %d, children %d, beasts %d, awakened monsters %d)   cities %d   sects %d   mean age %.0f%n",
                d.living, d.cultivators, d.children, d.beasts, d.monsters, d.cities, d.sects, d.cultivators == 0 ? 0 : (double) d.totalAge / d.cultivators));
        sb.append("\nCultivators by major realm:\n");
        int max = 1;
        for (int v : d.realm) max = Math.max(max, v);
        String[] names = {"Qi", "Foundation", "Core", "Soul Wander", "Nascent Soul", "Ensoulment", "Nihility", "Ascension", "Half-Immortal", "Pseudo Imm.", "True Imm."};
        for (int i = 0; i < 11; i++) sb.append(String.format("  M%-2d %-12s %s %d%n", i, names[i], bar((double) d.realm[i] / max, 30), d.realm[i]));
        sb.append("\nWealth (spirit stones) among cultivators:\n");
        sb.append(String.format("  p10 %s   median %s   p90 %s   p99 %s   richest %s   top 1%% hold %.0f%% of it%n", si(d.p10), si(d.p50), si(d.p90), si(d.p99), si(d.max), d.top1Share * 100));
        sb.append("\nDeaths of cultivators so far, by cause:\n");
        int total = 0;
        for (int v : d.deaths.values()) total += v;
        for (java.util.Map.Entry<String, Integer> e : d.deaths.entrySet()) sb.append(String.format("  %-20s %s %d (%.0f%%)%n", e.getKey(), bar(total == 0 ? 0 : (double) e.getValue() / total, 20), e.getValue(), total == 0 ? 0 : 100.0 * e.getValue() / total));
        if (d.deaths.isEmpty()) sb.append("  (none recorded yet)\n");
        return sb.toString();
    }

    // ------------------------------------------------------------------ city and sect details
    /** Cities and sects whose name contains `q` (case-insensitive); `kind` = "city", "sect" or "any". */
    static List<Area> findPlaces(String q, String kind) {
        String needle = q == null ? "" : q.trim().toLowerCase();
        List<Area> out = new ArrayList<>();
        for (Area a : new ArrayList<>(Map.createdAreas)) {
            if (a == null) continue;
            boolean cityHit = a.city && !kind.equals("sect") && (needle.isEmpty() || a.name.toLowerCase().contains(needle));
            boolean sectHit = a.sect != null && !kind.equals("city") && (needle.isEmpty() || a.sect.name.toLowerCase().contains(needle) || (a.city && false));
            if (cityHit || sectHit) out.add(a);
        }
        return out;
    }

    private static String ago(int week) { return "year " + week / 52; }

    /** The full sheet for a city and/or the sect that sits in this tile, with its history from the chronicle. */
    static String placeDetails(Area a, String focus) {
        StringBuilder sb = new StringBuilder();
        if (a.city && !"sect".equals(focus)) {
            sb.append("=== City of ").append(a.name).append(" ===\n");
            if (!Regions.line(a).isEmpty()) sb.append(Regions.line(a)).append("\n");
            sb.append("Tile ").append(PlayerControl.coord(a)).append("  ").append(a.geoName != null && !a.geoName.equals(a.name) ? "(" + a.geoName + ", " + a.biomeM + ")" : a.biomeM)
              .append("   level ").append(a.cityLevel()).append("   population ").append(a.population).append("   treasury ").append(a.cityTreasury).append(" ss\n");
            sb.append("Lord: ").append(a.cityOwner != null ? a.cityOwner.name + " " + a.cityOwner.lastName + " (Lv." + a.cityOwner.lvl + ", #" + a.cityOwner.id + ")" : "nobody — the city has no ruler").append("\n");
            sb.append("Shops ").append(a.shops.size()).append("/").append(a.maxShopSlots()).append(":");
            for (Shop s : new ArrayList<>(a.shops)) sb.append("  ").append(s.name == null || s.name.isEmpty() ? "shop" : s.name).append(" (").append(s.owner != null ? s.owner.name : "?").append(")");
            sb.append("\nLibraries:");
            for (Library l : new ArrayList<>(a.libraries)) sb.append("  ").append(l.name == null || l.name.isEmpty() ? "library" : l.name).append(" (").append(l.techniques.size()).append(" techniques)");
            sb.append("\nArrays protecting the city: ").append(a.arrayBag == null ? 0 : a.arrayBag.size()).append("\n");
            if (a.sect != null) sb.append("Sect here: ").append(a.sect.name).append(" (").append(a.sect.members.size()).append(" members)\n");
            if (a.tournamentTimer > 0) sb.append("Tournament under way: ").append(a.tournamentTimer).append(" weeks left\n");
            List<Character> here = new ArrayList<>();
            for (Character c : new ArrayList<>(a.characters)) if (c != null && !c.dead && c.trait != Character.Traits.Beast) here.add(c);
            here.sort((x, y) -> Integer.compare(y.lvl, x.lvl));
            sb.append("Cultivators here: ").append(here.size()).append("  strongest:");
            for (int i = 0; i < Math.min(8, here.size()); i++) sb.append("  ").append(here.get(i).name).append(" ").append(here.get(i).lastName).append(" Lv.").append(here.get(i).lvl);
            sb.append("\n");
            appendHistory(sb, a.name, a.geoName);
        }
        if (a.sect != null && !"city".equals(focus)) {
            Organization s = a.sect;
            if (sb.length() > 0) sb.append("\n");
            sb.append("=== Sect: ").append(s.name).append(" ===\n");
            if (!Regions.line(a).isEmpty()) sb.append(Regions.line(a)).append("\n");
            sb.append("Home ").append(a.name).append(" ").append(PlayerControl.coord(a)).append("   level ").append(s.sectLvl()).append("   members ").append(s.members.size()).append("   cash ").append(s.sectCash).append(" ss   treasury ").append(s.treasury.size()).append(" stacks\n");
            sb.append("Founder: ").append(s.founder != null ? s.founder.name + " " + s.founder.lastName + (s.founder.dead ? " (dead)" : " Lv." + s.founder.lvl) : s.isVirtual() ? "???" : "?").append("   Master: ").append(s.master != null ? s.master.name + " " + s.master.lastName + " Lv." + s.master.lvl : s.isVirtual() ? "???" : "none").append("\n");
            sb.append("War: ").append(s.warTarget != null ? s.warTarget.name : "none").append("   Allies: ");
            if (s.allies.isEmpty()) sb.append("none"); else for (Organization o : s.allies) sb.append(o.name).append("; ");
            sb.append("\nJoining: ").append(s.rulesText()).append("\n");
            List<Character> mem = new ArrayList<>(s.members);
            mem.removeIf(c -> c == null || c.dead);
            mem.sort((x, y) -> Integer.compare(y.lvl, x.lvl));
            sb.append("Strongest members:");
            for (int i = 0; i < Math.min(10, mem.size()); i++) sb.append("\n  ").append(String.format("%-28s Lv.%-4d %s", mem.get(i).name + " " + mem.get(i).lastName, mem.get(i).lvl, s.getRank(mem.get(i))));
            sb.append("\n");
            appendHistory(sb, s.name, null);
        }
        if (sb.length() == 0) sb.append("Nothing of note: this tile holds no city or sect.\n");
        return sb.toString();
    }

    private static void appendHistory(StringBuilder sb, String name, String alt) {
        List<Chronicle.Entry> h = Chronicle.search(name, "all", 40);
        if (alt != null && !alt.equals(name)) for (Chronicle.Entry e : Chronicle.search(alt, "all", 10)) if (!h.contains(e)) h.add(e);
        java.util.Collections.reverse(h);
        sb.append("History:\n");
        for (Chronicle.Entry e : h) sb.append("  ").append(e.when()).append("  ").append(e.text).append("\n");
        if (h.isEmpty()) sb.append("  (nothing recorded)\n");
    }

    /** `city <name>` / `sect <name>`: find by name and show the details. */
    static boolean showPlaces(String query, String kind) {
        List<Area> hits = findPlaces(query, kind);
        if (hits.isEmpty()) { say("No " + (kind.equals("any") ? "city or sect" : kind) + " matches '" + query + "'."); return false; }
        if (hits.size() > 1) {
            StringBuilder sb = new StringBuilder("Several match '" + query + "':\n");
            for (Area a : hits.subList(0, Math.min(15, hits.size()))) sb.append("  ").append(a.city ? a.name : a.sect.name).append(" ").append(PlayerControl.coord(a)).append(a.city ? "  (city)" : "  (sect)").append("\n");
            sb.append("Be more specific.\n");
            show(sb.toString());
            return false;
        }
        show(placeDetails(hits.get(0), kind.equals("any") ? null : kind));
        return true;
    }

    /** A living cultivator by `#id` or part of the name (the strongest match). */
    static Character findCharacter(String q) {
        if (q == null || q.trim().isEmpty()) return null;
        String s = q.trim();
        if (s.matches("#?\\d+")) return Character.charById(TermFormat.parseInt(s.replace("#", ""), -1));
        String needle = s.toLowerCase();
        Character best = null;
        int n = Map.characters.size();
        for (int i = 0; i < n; i++) {
            Character c = Map.characters.get(i);
            if (c == null || c.dead || c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) continue;
            if ((c.name + " " + c.lastName).toLowerCase().contains(needle) && (best == null || c.lvl > best.lvl)) best = c;
        }
        return best;
    }

    /** What happened in the last `weeks` weeks: counts, the notable events, and how the world changed. */
    static String digest(int weeks) {
        int from = Math.max(0, GameDriver.time - weeks);
        List<Chronicle.Entry> recent = new ArrayList<>();
        synchronized (Chronicle.log) { for (Chronicle.Entry e : Chronicle.log) if (e.week >= from) recent.add(e); }
        java.util.Map<String, Integer> counts = new TreeMap<>();
        for (Chronicle.Entry e : recent) counts.merge(e.kind, 1, Integer::sum);
        StringBuilder sb = new StringBuilder("=== What happened lately (the last " + Math.max(1, weeks / 52) + " year(s), up to year " + GameDriver.time / 52 + ") ===\n");
        int[] then = null, now = null;
        synchronized (GameDriver.statHistory) { if (!GameDriver.statHistory.isEmpty()) { now = GameDriver.statHistory.get(GameDriver.statHistory.size() - 1); for (int[] h : GameDriver.statHistory) if (h[0] <= from / 52 + 1) then = h; } }
        if (now != null && then != null && then != now) sb.append(String.format("Population %d -> %d, cities %d -> %d, sects %d -> %d, shops %d -> %d%n", then[1], now[1], then[2], now[2], then[3], now[3], then[4], now[4]));
        sb.append("Chronicle entries: ");
        if (counts.isEmpty()) sb.append("none"); else for (java.util.Map.Entry<String, Integer> e : counts.entrySet()) sb.append(e.getValue()).append(" ").append(e.getKey()).append("   ");
        sb.append("\n\nNotable events:\n");
        int shown = 0;
        for (int i = recent.size() - 1; i >= 0 && shown < 30; i--) { Chronicle.Entry e = recent.get(i); if (e.kind.equals("breakthrough") && !e.text.contains("Major Lvl 5") && !e.text.contains("Major Lvl 6") && !e.text.contains("Major Lvl 7")) continue; sb.append("  ").append(e).append("\n"); shown++; }
        if (shown == 0) sb.append("  (a quiet time)\n");
        sb.append("\nYour notices:\n");
        int k = 0;
        synchronized (Notifications.history) { for (int i = Notifications.history.size() - 1; i >= 0 && k < 12; i--) { Notifications.Note x = Notifications.history.get(i); if (x.week >= from) { sb.append(String.format("  wk %-6d [%s] %s%n", x.week, x.cat, x.text)); k++; } } }
        if (k == 0) sb.append("  (none)\n");
        return sb.toString();
    }

    // ------------------------------------------------------------------ help topics
    static final String[][] TOPICS = {
        {"map", "map [biome|level|beasts|people|sect] [radius]\n  Layers like the GUI map: terrain (default), tile level, beast density, crowds, sects.\n  (the plain form follows)\n  map | map <radius>\n  ASCII map of the world (or a square around you). North is up, one tile = one week's walk. Shows cities, sects, dungeons, spirit veins, other players and your notes; tiles you have never seen stay dark."},
        {"route", "route <x> <y>\n  Previews the walk 'goto' would take: distance, arrival date, danger along the road (unexplored tiles, hotspots, the strongest beast) and the cities you pass."},
        {"time", "time\n  Year, week and season, weeks to the yearly auction/tax round, to your birthday and to the end of a tournament here."},
        {"watch", "watch [weeks] [for <category,...>] [until year <n>]\n  e.g. 'watch 200 for breakthrough,war' or 'watch until year 120'. Categories: breakthrough, level, death, war, family, alert, danger, auction, fortune, reincarnation. An attack always stops it.\n  (the plain form follows)\n  watch [weeks]\n  Lets time run (default 52 weeks) until something happens to you: a notice (breakthrough, death of a relation, war, danger...), a new quest alert, or an attack."},
        {"journal", "journal [n]\n  Your own story: the chronicle entries about you, your latest notices and a sample of your life (level, wealth, friends, kills by year)."},
        {"history", "history [text|#id] [kind=death|breakthrough|city|sect|war|world|legend] [n]\n  Searches the world chronicle: 'history Wei', 'history #1234', 'history kind=war 50'. Deaths of Major Lvl 3+, breakthroughs to Core Formation and beyond, new cities and sects, wars, calamities, legends."},
        {"equip", "equip | equip <bag#>\n  Plain 'equip' puts on the best gear you carry. 'equip <n>' wears that item (the game keeps the strongest gear on, so a weaker pick is swapped back later)."},
        {"compare", "compare <bag#>\n  Shows how wearing that item would change your fighting level, next to what you wear now."},
        {"techniques", "techniques\n  Your martial and movement slots (main and second) and every technique you know. The strongest of each kind is used automatically."},
        {"notes", "note <text> | note #<id> <text> | notes | unnote <n>\n  Bookmarks the tile you stand on (or a cultivator) with a note. They show as * on 'map' and in the GUI Notes window."},
        {"city", "city | city <name>\n  The city you stand in, or any city found by (part of) its name: lord, shops, libraries, arrays, who is here, and its history from the chronicle."},
        {"rules", "sect rules [potential <n>] [level <n>] [gender male|female|any] [affiliation good|neutral|evil|any] [trait <name|any>] [family <name|any>] | sect rules open\n  What a sect asks of its recruits: a craft (profession), a potential floor, a level floor, an alignment, a gender, a family name. The master sets them; NPC-founded sects roll up to two."},
        {"sect", "sect | sect <name>\n  Your sect (or the one here), or any sect by name: founder, master, strongest members, war and allies, how to join, and its history."},
        {"runstats", "runstats | halloffame | retire | export run\n  Statistics of the whole run (every body you have lived in, how each life ended, peaks and totals). `retire` writes it to the hall of fame (also written automatically when a Brutal run ends); `export run` saves a CSV."},
        {"digest", "digest [weeks]\n  A summary of what happened lately (default 52 weeks): population and settlement changes, notable chronicle events and your notices. A saved game opens with one."},
        {"market", "market | buy market <#> [qty] | sell goods|food|wine|poison [qty]\n  The goods market of a city: portions of food, wine and poison that Chefs, brewers and Poisoners sold (a quarter of a pill's price each; poison three times that). Food spoils fastest. NPCs buy meals and wine here too."},
        {"dash", "dash\n  World dashboard: population by kind, cultivators per realm, wealth spread and deaths by cause. Also in the GUI Rankings window."},
    };

    /** `help <topic>`: a command's detail, or the section of the general help whose label starts with the word. */
    static String helpTopic(String word) {
        String w = word.toLowerCase();
        StringBuilder sb = new StringBuilder();
        for (String[] t : TOPICS) if (t[0].equals(w)) return "=== help " + t[0] + " ===\n" + t[1] + "\n";
        for (String line : PlayerCommands.HELP.split("\n")) {
            int i = line.indexOf(':');
            if (i > 0 && line.substring(0, i).trim().toLowerCase().startsWith(w)) sb.append(line).append("\n");
        }
        if (sb.length() > 0) return "=== help " + w + " ===\n" + sb;
        StringBuilder names = new StringBuilder();
        for (String[] t : TOPICS) names.append(t[0]).append(' ');
        return "No help topic '" + word + "'. Topics: " + names + "and the section labels (move, modes, duties, policy, quests, info, lists, pick, seek, shops, work, roles, do, script, system).";
    }
}
