package Game;

import java.util.ArrayList;
import java.util.List;

/** The third set of cheats (only a Cheater may use them; `cheat help` lists them, the Cheats window has a button for each): the person (an instant breakthrough, a breakthrough that never fails, age and lifespan, the
 *  inhibitor, full technique mastery, the element of a path, marriage and pregnancy), the world (the epoch at once, a Heaven-Sent treasure, a spirit vein, a dungeon, an array, a tile wiped of beasts, a beast woken,
 *  a tournament opened, held or closed), time (jump weeks, go to a season), the purse (free crafting, wealth) and sight (reveal the map, the people of a tile, one person). Every one runs through the command line. */
final class CheatsMore {
    private CheatsMore() { }

    static final String HELP =
            "=== Even more cheats (only for a Cheater) ===\n"
            + "  cheat breakthrough [self|#id]             at once to the next realm (the last level of this one is filled and the gate always opens)\n"
            + "  cheat pass on|off [self|#id]              every breakthrough and every tribulation succeeds\n"
            + "  cheat age <years> [self|#id]  |  cheat lifespan <years> [self|#id]\n"
            + "  cheat inhibitor <n> [self|#id]            the slowing left by failed breakthroughs (1 = none, 0 = twice as fast)\n"
            + "  cheat mastery [self|#id]                  every technique known is at its full level\n"
            + "  cheat element wood|fire|earth|metal|water [self|#id] [techniques]     the element of the path (and of the techniques)\n"
            + "  cheat marry <#a> <#b>  |  cheat pregnant <#id>          a marriage; a child on the way (the woman needs a husband)\n"
            + "  cheat epoch                               the Heavens grow weary at the next tick: the great purge begins\n"
            + "  cheat treasure [technique|instrument|poison|potential|pill|clothes|weapon|array]     a Heaven-Sent treasure at your tile\n"
            + "  cheat vein on|off  |  cheat dungeon ruins|lair|trial|haunted|off [level]  |  cheat array install <level>|break     this tile\n"
            + "  cheat wipe                                every wild beast and Monster of this tile falls   |   cheat awaken      a wild beast of this tile wakes as a Monster\n"
            + "  cheat garden                              the Garden's Variety Tournament is held now (everyone fights, nobody dies; the next one comes at its own year all the same)\n"
            + "  cheat tournament open [min max] | hold | close      at your tile (hold: everyone here who may enter is signed up and it is fought next week)\n"
            + "  cheat jump <weeks>  |  cheat season spring|summer|autumn|winter      the weeks pass (an alert ends them early)\n"
            + "  cheat freecraft on|off [self|#id]         crafting never runs out of herbs, metal or Blood   |   cheat wealth <spirit stones> [self|#id]\n"
            + "  cheat reveal map | here | <#id>           every place made so far; everyone in this tile with their path and element; one person in full\n"
            + "  cheat teleport <x> <y> squad              takes the squad along\n";

    /** A cheat that waits spends the week; the caller reads this once. */
    private static boolean turnSpent;
    static boolean takeTurnFlag() { boolean b = turnSpent; turnSpent = false; return b; }

    private static void say(String s) { Cheats.say(s); }
    private static String who(Character x) { return Cheats.who(x); }
    private static boolean has(String[] t, int n) { return t.length > n; }

    /** Returns true when `t[1]` was one of these. */
    static boolean run(Character c, String[] t) {
        String verb = t[1].toLowerCase();
        switch (verb) {
            case "help": say(HELP); return true;
            case "breakthrough": breakthrough(c, t); return true;
            case "pass": case "freecraft": flag(c, t, verb); return true;
            case "age": case "lifespan": ageCheat(c, t, verb); return true;
            case "inhibitor": inhibitor(c, t); return true;
            case "mastery": mastery(c, t); return true;
            case "element": element(c, t); return true;
            case "marry": marry(t); return true;
            case "pregnant": pregnant(t); return true;
            case "epoch": GameDriver.epochNow = true; say("The Heavens grow weary of the world: the great purge begins at the next tick."); return true;
            case "treasure": treasure(c, t); return true;
            case "vein": vein(c, t); return true;
            case "dungeon": dungeon(c, t); return true;
            case "array": array(c, t); return true;
            case "wipe": wipe(c); return true;
            case "awaken": awaken(c); return true;
            case "garden": say("The sky opens..."); { Character champ = GardenTournament.run(GameDriver.time / 52 + 1); say(champ == null ? "Nobody fought: the world is too small." : "The champion is " + champ.name + " " + champ.lastName + "."); } return true;
            case "tournament": tournament(c, t); return true;
            case "jump": jump(c, has(t, 2) ? PlayerCommands.num(t[2], 1) : 1, "The weeks pass"); return true;
            case "season": season(c, t); return true;
            case "wealth": wealth(c, t); return true;
            case "reveal": reveal(c, t); return true;
            default: return false;
        }
    }

    // ------------------------------------------------------------------ helpers
    /** The target named by a `#id` or `self` token anywhere after position `from` (the person who plays when there is none). */
    private static Character targetFrom(Character c, String[] t, int from) {
        for (int i = from; i < t.length; i++) if (t[i].startsWith("#") || t[i].equalsIgnoreCase("self")) return Cheats.target(c, t[i]);
        return c;
    }

    /** The bar, the weekly gain and the level fields after a change of level or inhibitor. */
    private static void recalc(Character x) {
        x.cultivationBaseMax = Math.pow(x.lvl, 3) * (((70. + 1. / 3.) + ((11. / 6.) * x.lvl)) / 25.);
        x.cultivation = (Math.pow(x.potential, 2) * Math.sqrt(x.lvl) * 10.) / (52. * Math.sqrt(Math.max(1, x.potential)) * Math.pow(2, x.inhibitor));
    }

    // ------------------------------------------------------------------ the person
    private static void breakthrough(Character c, String[] t) {
        Character x = targetFrom(c, t, 2); if (x == null) return;
        if (x.majorLvl >= 10) { say(who(x) + " is a True Immortal already: there is nothing above."); return; }
        final int before = x.majorLvl;
        final boolean was = x.cheatBreak;
        x.cheatBreak = true;
        try {
            x.minorLvl = 10; x.lvl = x.majorLvl * 10 + 10; x.tempLvl = x.lvl;
            recalc(x);
            x.cultivationBase = x.cultivationBaseMax;
            x.breakthroughReady = true;
            x.breakthroughNow();
        } finally { x.cheatBreak = was; }
        x.tempLvlCheck();
        say(who(x) + (x.majorLvl > before ? " breaks through to " + TermFormat.REALMS[x.majorLvl] + "." : " stands at the last level of " + TermFormat.REALMS[x.majorLvl] + " (the gate did not open: see the log)."));
    }

    private static void flag(Character c, String[] t, String which) {
        if (!has(t, 2)) { say("Usage: cheat " + which + " on|off [self|#id]"); return; }
        boolean on = PlayerCommands.onOff(t[2]);
        Character x = targetFrom(c, t, 3); if (x == null) return;
        if (which.equals("pass")) { x.cheatBreak = on; say(who(x) + (on ? ": every breakthrough and tribulation will succeed." : ": breakthroughs are fair again.")); }
        else { x.cheatFreeCraft = on; if (on) refill(x); say(who(x) + (on ? ": crafting never runs out of herbs, metal or Blood." : ": crafting costs materials again.")); }
    }

    /** Free crafting: the bag is kept full of every quality of herb and metal, and of Blood. Called every turn of a person who plays and when the cheat is switched on. */
    static void refill(Character x) {
        if (x == null || !x.cheatFreeCraft || x.materialBag == null) return;
        for (Item.ItemType ty : new Item.ItemType[]{Item.ItemType.Herb, Item.ItemType.Metal}) {
            for (int q = 1; q <= 10; q++) {
                Item stack = null;
                for (Item m : x.materialBag) if (m != null && m.itemType == ty && m.lvl == q) { stack = m; break; }
                if (stack == null) x.materialBag.add(new Item(ty, q, 5000));
                else if (stack.quantity < 5000) stack.quantity = 5000;
            }
        }
        Item blood = null;
        for (Item m : x.materialBag) if (m != null && m.itemType == Item.ItemType.Blood) { blood = m; break; }
        if (blood == null) x.materialBag.add(new Item(Item.ItemType.Blood, 5000));
        else if (blood.quantity < 5000) blood.quantity = 5000;
    }

    private static void ageCheat(Character c, String[] t, String which) {
        if (!has(t, 2)) { say("Usage: cheat " + which + " <years> [self|#id]"); return; }
        Character x = targetFrom(c, t, 3); if (x == null) return;
        int v = Math.max(0, PlayerCommands.num(t[2], 0));
        if (which.equals("age")) { x.age = v; say(who(x) + " is " + v + " years old now" + (v >= x.maxAge ? " (past the lifespan of " + x.maxAge + ": death will come)." : " (the lifespan is " + x.maxAge + ").")); }
        else { x.maxAge = Math.max(1, v); say(who(x) + " will live to " + x.maxAge + " (now " + x.age + ")."); }
    }

    private static void inhibitor(Character c, String[] t) {
        if (!has(t, 2)) { say("Usage: cheat inhibitor <n> [self|#id]   (1 = no slowing, 0 = twice as fast, each failure adds 1)"); return; }
        Character x = targetFrom(c, t, 3); if (x == null) return;
        x.inhibitor = Math.max(0, Math.min(20, PlayerCommands.num(t[2], 1)));
        recalc(x);
        say(who(x) + ": inhibitor " + x.inhibitor + " (" + String.format("%.1f", x.cultivation) + " cultivation a week).");
    }

    private static void mastery(Character c, String[] t) {
        Character x = targetFrom(c, t, 2); if (x == null) return;
        int n = 0;
        List<Technique> all = new ArrayList<>();
        all.add(x.martialTechnique); all.add(x.martialTechnique2); all.add(x.movementTechnique); all.add(x.movementTechnique2);
        if (x.techniqueList != null) all.addAll(x.techniqueList);
        for (Technique q : all) if (q != null && q.potential > 0 && q.lvl < q.potential) { q.lvl = q.potential; n++; }
        x.tempLvlCheck();
        say(who(x) + ": " + n + " technique(s) are at their full level now.");
    }

    private static void element(Character c, String[] t) {
        if (!has(t, 2)) { say("Usage: cheat element wood|fire|earth|metal|water [self|#id] [techniques]"); return; }
        int e = -1;
        for (int i = 0; i < 5; i++) if (Elements.NAMES[i].equalsIgnoreCase(t[2])) e = i;
        if (e < 0) { say("The elements are wood, fire, earth, metal and water."); return; }
        Character x = targetFrom(c, t, 3); if (x == null) return;
        boolean tech = false;
        for (String w : t) if (w.equalsIgnoreCase("techniques")) tech = true;
        Elements.assign(x, e, new java.util.Random());
        int n = 0;
        if (tech) {
            List<Technique> all = new ArrayList<>();
            all.add(x.martialTechnique); all.add(x.martialTechnique2); all.add(x.movementTechnique); all.add(x.movementTechnique2);
            if (x.techniqueList != null) all.addAll(x.techniqueList);
            for (Technique q : all) if (q != null && q.potential > 0) { q.elemPlus1 = (byte) (e + 1); q.name = Naming.technique(q.type, x, e); n++; }
            x.equip();
        }
        say(who(x) + " now follows " + Elements.describe(x) + (tech ? "; " + n + " technique(s) took the element too." : "."));
    }

    private static void marry(String[] t) {
        if (t.length < 4) { say("Usage: cheat marry <#a> <#b>"); return; }
        Character a = PlayerCommands.anyone(t[2]), b = PlayerCommands.anyone(t[3]);
        if (a == null || b == null || a == b) { say("Two different living people are needed (#id #id)."); return; }
        if (a.spouse != null && a.spouse != b) { a.spouse.spouse = null; }
        if (b.spouse != null && b.spouse != a) { b.spouse.spouse = null; }
        a.spouse = b; b.spouse = a;
        Cheats.setBoth(a, b, 3);
        if (a.gender != null && !a.gender.equals(b.gender)) { if (a.gender.equals("Female")) a.lastName = b.lastName; else b.lastName = a.lastName; }
        Notifications.onMarriage(a, b);
        say(who(a) + " and " + who(b) + " are married.");
    }

    private static void pregnant(String[] t) {
        if (!has(t, 2)) { say("Usage: cheat pregnant <#id>   (a woman with a living husband)"); return; }
        Character m = PlayerCommands.anyone(t[2]);
        if (m == null) { say("Nobody found for '" + t[2] + "'."); return; }
        if (!"Female".equals(m.gender)) { say(who(m) + " cannot carry a child."); return; }
        if (m.spouse == null || m.spouse.dead) { say(who(m) + " needs a living husband first (cheat marry)."); return; }
        if (m.pregnant) { say(who(m) + " is with child already."); return; }
        Map.addTemp(new Character(m));
        say(who(m) + " is with child (" + who(m.spouse) + " is the father). The child is born in a while.");
    }

    // ------------------------------------------------------------------ the world
    private static void treasure(Character c, String[] t) {
        Area here = c.location;
        if (here == null) return;
        if (here.hasHeaven()) { say("A Heaven-Sent treasure lies here already: " + here.heavenLabel() + "."); return; }
        Treasure.Kind kind = Treasure.Kind.values()[GameDriver.rand.nextInt(Treasure.Kind.values().length)];
        if (has(t, 2)) {
            String w = t[2].toLowerCase();
            Treasure.Kind found = null;
            for (Treasure.Kind k : Treasure.Kind.values()) if (k.name().replace("_", "").toLowerCase().startsWith(w)) { found = k; break; }
            if (found == null) { say("technique, instrument, poison, potential, pill, clothes, weapon or array."); return; }
            kind = found;
        }
        Treasure.place(here, kind, Treasure.create(kind, Math.min(9, c.majorLvl + 1), c));
        Map.heavenExists = true;
        say("A Heaven-Sent " + Treasure.label(kind) + " descends here: " + here.heavenLabel() + ". Take it with the usual quest.");
    }

    private static void vein(Character c, String[] t) {
        if (!has(t, 2)) { say("Usage: cheat vein on|off"); return; }
        c.location.spiritualVein = PlayerCommands.onOff(t[2]);
        say(c.location.name + (c.location.spiritualVein ? " now holds a spirit vein." : " holds no spirit vein now."));
    }

    private static void dungeon(Character c, String[] t) {
        Area a = c.location;
        if (!has(t, 2)) { say("Usage: cheat dungeon ruins|lair|trial|haunted|off [level]"); return; }
        if (a.city || a.sect != null || a.loreSite != null || a.isOriginis()) { say("Not here: a city, a sect, Originis and the places of the story cannot become a dungeon."); return; }
        String w = t[2].toLowerCase();
        if (w.equals("off")) {
            if (a.dungeonType == null) { say("There is no dungeon here."); return; }
            a.dungeonType = null; a.dungeonCharges = 0; a.celestialTrialTimer = 0; a.restoreName();
            say("The dungeon is gone: this is " + a.name + " again.");
            return;
        }
        Area.DungeonType ty = w.startsWith("r") ? Area.DungeonType.LOST_RUINS : w.startsWith("l") || w.startsWith("b") ? Area.DungeonType.BEAST_LAIR : w.startsWith("t") || w.startsWith("m") ? Area.DungeonType.MARTIAL_TRIAL : w.startsWith("h") ? Area.DungeonType.HAUNTED_GROUNDS : null;
        if (ty == null) { say("ruins, lair, trial, haunted or off."); return; }
        if (has(t, 3)) { a.lvl = Math.max(1, Math.min(100, PlayerCommands.num(t[3], a.lvl))); a.baseLvl = a.lvl; }
        if (a.geoName == null) a.geoName = a.name;
        a.dungeonType = ty;
        a.dungeonCharges = ty == Area.DungeonType.LOST_RUINS ? Math.max(3, a.lvl / 2 + GameDriver.rand.nextInt(Math.max(1, a.lvl / 2 + 1))) : 0;
        if (ty == Area.DungeonType.BEAST_LAIR) a.monsters = Math.max(a.monsters, 1000);
        a.name = Naming.dungeon(ty, a.lvl);
        c.learnPlace(a);
        say("The land here is " + a.name + " now (a dungeon of level " + a.lvl + ").");
    }

    private static void array(Character c, String[] t) {
        Area a = c.location;
        if (!has(t, 2)) { say("Usage: cheat array install <level 1-100> | break"); return; }
        if (t[2].equalsIgnoreCase("break")) { int n = a.arrayBag.size(); a.arrayBag.clear(); say(n + " array(s) of " + a.name + " are broken."); return; }
        if (!t[2].equalsIgnoreCase("install")) { say("install <level> or break."); return; }
        int lvl = Math.max(1, Math.min(100, has(t, 3) ? PlayerCommands.num(t[3], 10) : 10));
        a.arrayBag.add(new Item(c, lvl, Item.ItemType.Array));
        say("An array of level " + lvl + " guards " + a.name + " now (" + a.arrayBag.size() + " there).");
    }

    private static void wipe(Character c) {
        Area a = c.location;
        int beasts = a.monsters;
        a.monsters = 0;
        int n = 0;
        for (Character m : new ArrayList<>(a.characters)) {
            if (m == null || m.dead || m.trait != Character.Traits.Monster) continue;
            m.deathCause = "struck down by divine lightning"; m.smitten = true; m.dead(); m.smitten = false;
            if (m.dead) n++;
        }
        say(a.name + " is cleared: " + beasts + " wild beast(s) and " + n + " Monster(s) fall.");
    }

    private static void awaken(Character c) {
        Area a = c.location;
        if (a.monsters <= 0) { say("No wild beast lives here to wake."); return; }
        a.monsters--;
        Character b = new Character(a, c);
        Cheats.setLevel(b, Math.max(1, a.lvl));
        b.awakenMonster();
        say("A wild beast wakes as a Monster: " + b.name + " " + b.lastName + " (Lv." + b.lvl + ", " + Elements.name(Elements.of(b)) + ").");
    }

    private static void tournament(Character c, String[] t) {
        Area a = c.location;
        if (!has(t, 2)) { say("Usage: cheat tournament open [min max] | hold | close"); return; }
        switch (t[2].toLowerCase()) {
            case "open": {
                if (a.tournamentTimer > 0) { say("A tournament is open here already."); return; }
                int lo = has(t, 3) ? Math.max(0, Math.min(10, PlayerCommands.num(t[3], 0))) : Math.max(0, (c.majorLvl / 2) * 2);
                int hi = has(t, 4) ? Math.max(lo, Math.min(10, PlayerCommands.num(t[4], lo + 1))) : Math.min(10, lo + 1);
                int prizeLvl = Math.min(100, (hi + 1) * 10 + GameDriver.rand.nextInt(11));
                Item prize = GameDriver.rand.nextInt(2) == 0 ? new Item(c, Item.WeaponType.values()[GameDriver.rand.nextInt(Item.WeaponType.values().length)], prizeLvl) : new Item(c, prizeLvl, Item.ItemType.Clothing);
                a.tournamentTimer = 8; a.tournamentMinMajor = lo; a.tournamentMaxMajor = hi; a.tournamentPrize = prize; a.tournamentOrganizer = c;
                a.tournamentRegistrants.clear();
                a.tournamentName = Naming.tournament(a.name, GameDriver.time / 52);
                say("=== The " + a.tournamentName + " is announced at " + a.name + "! Realm " + lo + "-" + hi + ", 8 weeks registration. Prize: " + prize + " ===");
                return;
            }
            case "hold": {
                if (a.tournamentTimer <= 0) { say("No tournament is open here (cheat tournament open)."); return; }
                int n = 0;
                for (Character x : new ArrayList<>(a.characters)) {
                    if (x == null || x.dead || !x.born || x.age < 8 || x == c) continue; // (you enter by the quest, or watch by it)
                    if (x.player == 1 && x.isManualPc() && x != c) continue; // other people who play choose for themselves
                    if (x.majorLvl < a.tournamentMinMajor || x.majorLvl > a.tournamentMaxMajor || a.tournamentRegistrants.contains(x)) continue;
                    if (x.trait == Character.Traits.Beast || x.trait == Character.Traits.Monster) continue;
                    a.tournamentRegistrants.add(x); n++;
                }
                a.tournamentTimer = 1;
                say("The tournament is held next week: " + n + " more fighter(s) are signed up (" + a.tournamentRegistrants.size() + " in all).");
                return;
            }
            case "close": {
                if (a.tournamentTimer <= 0) { say("No tournament is open here."); return; }
                if (a.tournamentPrize != null && a.tournamentOrganizer != null) a.tournamentOrganizer.backpack.add(a.tournamentPrize);
                a.tournamentTimer = 0; a.tournamentPrize = null; a.tournamentOrganizer = null; a.tournamentRegistrants.clear();
                Tournaments.clearSeats(a);
                say("The tournament of " + a.name + " is called off.");
                return;
            }
            default: say("open, hold or close.");
        }
    }

    // ------------------------------------------------------------------ time
    private static void jump(Character c, int weeks, String text) {
        int n = Math.max(1, Math.min(520, weeks));
        say(text + ": " + n + (n == 1 ? " week" : " weeks") + " (an alert or an attack ends it early).");
        c.movement = 'x';
        c.skipping = n - 1;
        c.movement('x');
        turnSpent = true;
    }

    private static void season(Character c, String[] t) {
        String[] names = {"spring", "summer", "autumn", "winter"};
        int target = -1;
        if (has(t, 2)) for (int i = 0; i < 4; i++) if (names[i].startsWith(t[2].toLowerCase())) target = i;
        if (target < 0) { say("Usage: cheat season spring|summer|autumn|winter"); return; }
        int now = (GameDriver.time % 52) / 13;
        if (now == target) { say("It is " + names[target] + " already."); return; }
        int weeks = ((target * 13 - GameDriver.time % 52) + 52) % 52;
        jump(c, weeks, "You wait for " + names[target]);
    }

    // ------------------------------------------------------------------ the purse
    private static void wealth(Character c, String[] t) {
        if (!has(t, 2)) { say("Usage: cheat wealth <spirit stones> [self|#id]"); return; }
        Character x = targetFrom(c, t, 3); if (x == null) return;
        x.ss = Math.max(0, PlayerCommands.num(t[2], 0));
        say(who(x) + " now holds " + x.ss + " ss.");
    }

    // ------------------------------------------------------------------ sight
    private static void reveal(Character c, String[] t) {
        if (!has(t, 2)) { say("Usage: cheat reveal map | here | <#id>"); return; }
        String w = t[2].toLowerCase();
        if (w.equals("map")) {
            int n = 0;
            for (Area a : new ArrayList<>(Map.createdAreas)) if (a != null) { c.learnPlace(a); n++; }
            say("Every place made so far is on your map now (" + n + ").");
        } else if (w.equals("here")) {
            StringBuilder sb = new StringBuilder("Everyone in " + c.location.name + " (path and element):\n");
            int shown = 0;
            for (Character x : new ArrayList<>(c.location.characters)) {
                if (x == null || x.dead || x == c) continue;
                if (shown++ >= 60) { sb.append("  ... and more\n"); break; }
                sb.append("  #").append(x.id).append(' ').append(x.name).append(' ').append(x.lastName).append("  Lv.").append(x.lvl).append(' ').append(TermFormat.realm(x.majorLvl)).append("  ").append(Elements.describe(x)).append("  ").append(x.getTrait()).append('\n');
            }
            if (shown == 0) sb.append("  (nobody else)\n");
            say(sb.toString());
        } else {
            Character x = Cheats.target(c, t[2]);
            if (x == null) return;
            StringBuilder sb = new StringBuilder(who(x) + "  Lv." + x.lvl + " " + TermFormat.realm(x.majorLvl) + ", " + x.getTrait() + ", age " + x.age + "/" + x.maxAge + "\n");
            sb.append("  Path: ").append(Elements.describe(x)).append("   potential ").append(x.potential).append(", inhibitor ").append(x.inhibitor).append(", ").append(x.ss).append(" ss\n");
            Technique[] ts = {x.martialTechnique, x.martialTechnique2, x.movementTechnique, x.movementTechnique2};
            for (Technique q : ts) if (q != null && q.potential > 0) sb.append("  ").append(q.type).append(": ").append(q.name).append(" [").append(Elements.name(q.element())).append("] level ").append(q.lvl).append("/").append(q.potential).append('\n');
            sb.append("  Sect: ").append(x.sect == null ? "none" : x.sect.name).append("   spouse: ").append(x.spouse == null ? "none" : who(x.spouse)).append("   at ").append(x.location == null ? "?" : x.location.name).append('\n');
            say(sb.toString());
        }
    }
}
