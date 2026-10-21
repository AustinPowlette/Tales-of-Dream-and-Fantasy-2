package Game;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** The second set of cheats (only a Cheater may use them; `cheat help` lists them, the Cheats window has a button for each). Where the first set changes a number (stones, level, potential, kills, a weapon), these change the
 *  world: strike somebody down, set a bond between any two people, make gear unbreakable, set a technique, teleport, give any item, tame a companion of any level, refuse death (a policy that also keeps an AUTO character
 *  alive), mark a person so that nothing kills them, call the dead back, win every fight, let nobody escape you, create people and Monsters, and change a city or a sect. Everything runs through the command line, so the log
 *  shows what happened. */
final class Cheats {
    private Cheats() { }

    static final String HELP =
            "=== More cheats (only for a Cheater) ===\n"
            + "  cheat kill <#id>                          struck down by divine lightning (nothing protects against it)\n"
            + "  cheat relation <#a> <#b> <-4..3>          the bond between any two people, both ways (-4 wanted, 0 strangers, 3 best friends)\n"
            + "  cheat unbreakable weapon|armor|both|off [self|#id]\n"
            + "  cheat technique <martial|martial2|movement|movement2|known N> level|potential <n> [self|#id]\n"
            + "  cheat teleport <x> <y> [self|#id]         tile numbers, or the coordinates shown (x100)\n"
            + "  cheat give weapon|armor|array <level 1-100> | instrument|pill|herb|metal <level 1-10> [self|#id]\n"
            + "  cheat give pill cultivation|potential <level 1-10> <how many> | herb|metal <level> <how many> | blood <how many> | technique martial|movement <potential> [fire|wood|earth|metal|water]\n"
            + "  cheat tame <level> [name]                 a companion beast of any level (the cap does not count); cheat companion <n> level|potential|name <value>\n"
            + "  cheat evade on|off [self|#id]             when death comes the person simply goes on living (a policy: it also saves an AUTO character)\n"
            + "  cheat nodeath on|off <#id|self>           the No Death mark: only a person who plays can kill the marked one (nothing else, the epoch included)\n"
            + "  cheat resurrect [<#id|part of a name>]    call back one of the recently dead (no argument: the list)\n"
            + "  cheat win on|off [self|#id]               every fight is won (and a marked enemy always loses)\n"
            + "  cheat noescape on|off [self|#id]          nobody can flee from the marked person\n"
            + "  cheat create npc [major 0-10] [trait] | monster <level> [name] | beast <how many>      at your tile\n"
            + "  cheat city population|treasury <n> | owner <#id|self> | rename <name> | raze        the city you stand in\n"
            + "  cheat sect master|join|expel <#id> | rename <name>       the sect of this tile, or yours\n";

    // ---- the graveyard: the recent dead, so that they can be called back
    static final int GRAVEYARD = 500;
    static final LinkedHashMap<Integer, Character> graveyard = new LinkedHashMap<>();

    /** Called when somebody dies (not an untamed beast): kept for `cheat resurrect`. */
    static void bury(Character c) {
        if (c == null || c.trait == Character.Traits.Beast) return;
        synchronized (graveyard) {
            graveyard.remove(c.id);
            graveyard.put(c.id, c);
            while (graveyard.size() > GRAVEYARD) { Integer first = graveyard.keySet().iterator().next(); graveyard.remove(first); }
        }
    }

    /** True when a person who plays refuses death by policy (`cheat evade`): he is healed and nothing happens. */
    static boolean evades(Character c) {
        if (c == null || c.player != 1 || c.pc == null || !c.pc.evadeDeath) return false;
        c.cultivation = Math.max(c.cultivation, 1);
        c.dead = false;
        c.tell("Death comes for you and passes you by: you go on living.");
        return true;
    }

    /** Added to the chance to win of `a` fighting `b`: a person who always wins gets +1000, a marked enemy -1000. */
    static int bias(Character a, Character b) {
        int v = 0;
        if (a != null && a.cheatWin) v += 1000;
        if (b != null && b.cheatWin) v -= 1000;
        return v;
    }

    static void say(String s) { PlayerCommands.say(s); }

    /** The new cheats. Returns true when `t[1]` was one of them (the old set is handled by the caller). */
    static boolean run(Character c, String[] t) {
        String verb = t[1].toLowerCase();
        try {
            switch (verb) {
                case "help": say(HELP + CheatsMore.HELP); return true;
                case "kill": kill(c, t); return true;
                case "relation": case "relationship": relation(t); return true;
                case "unbreakable": unbreakable(c, t); return true;
                case "technique": technique(c, t); return true;
                case "teleport": teleport(c, t); return true;
                case "give": give(c, t); return true;
                case "tame": tame(c, t); return true;
                case "companion": companion(c, t); return true;
                case "evade": flag(c, t, "evade"); return true;
                case "nodeath": flag(c, t, "nodeath"); return true;
                case "win": flag(c, t, "win"); return true;
                case "noescape": flag(c, t, "noescape"); return true;
                case "resurrect": resurrect(c, t); return true;
                case "create": create(c, t); return true;
                case "city": city(c, t); return true;
                case "sect": sect(c, t); return true;
                default: return CheatsMore.run(c, t);
            }
        } catch (RuntimeException e) { say("That cheat failed: " + e); return true; }
    }

    // ------------------------------------------------------------------ targets
    static Character target(Character c, String s) {
        if (s == null || s.equalsIgnoreCase("self")) return c;
        Character x = PlayerCommands.anyone(s);
        if (x == null) say("Nobody found for '" + s + "' (use #id).");
        return x;
    }

    static Character lastTarget(Character c, String[] t, int from) {
        if (t.length > from) return target(c, t[t.length - 1].startsWith("#") || t[t.length - 1].equalsIgnoreCase("self") ? t[t.length - 1] : null);
        return c;
    }

    static boolean has(String[] t, int n) { return t.length > n; }

    static String who(Character x) { return x.name + " " + x.lastName + " #" + x.id; }

    // ------------------------------------------------------------------ kill
    private static void kill(Character c, String[] t) {
        if (!has(t, 2)) { say("Usage: cheat kill <#id>"); return; }
        Character x = PlayerCommands.anyone(t[2]);
        if (x == null) { say("Nobody found for '" + t[2] + "' (use #id; a person who lives)."); return; }
        if (x == c) { say("That would be you. (Use a policy if you want to refuse death: cheat evade.)"); return; }
        x.deathCause = "struck down by divine lightning";
        x.smitten = true;
        String name = who(x);
        x.dead();
        x.smitten = false;
        if (x.dead) { say(name + " is struck down by divine lightning."); Chronicle.add("world", x.name + " " + x.lastName + " was struck down by divine lightning.", x.id); }
        else say(name + " would not die (they may be a True Immortal in a cycle).");
    }

    // ------------------------------------------------------------------ relation
    private static void relation(String[] t) {
        if (t.length < 5) { say("Usage: cheat relation <#a> <#b> <-4..3>"); return; }
        Character a = PlayerCommands.anyone(t[2]), b = PlayerCommands.anyone(t[3]);
        if (a == null || b == null || a == b) { say("Two different living people are needed (#id #id)."); return; }
        int v;
        try { v = Math.max(-4, Math.min(3, Integer.parseInt(t[4].trim()))); } catch (NumberFormatException e) { say("The bond is a number from -4 to 3."); return; }
        setBoth(a, b, v);
        say(who(a) + " and " + who(b) + " now stand at " + v + " (" + describeBond(v) + ").");
    }

    /** Both entries are made first (making one can nudge the other), then both are set. */
    static void setBoth(Character a, Character b, int v) {
        if (a.relationshipList.contains(b.id) == null) a.changeRelationship(b, 0);
        if (b.relationshipList.contains(a.id) == null) b.changeRelationship(a, 0);
        setBond(a, b, v); setBond(b, a, v);
    }

    static void setBond(Character a, Character b, int v) {
        Relationship r = a.relationshipList.contains(b.id);
        if (r == null) { a.changeRelationship(b, 0); r = a.relationshipList.contains(b.id); }
        if (r != null) r.value = v;
    }

    private static String describeBond(int v) {
        switch (v) { case 3: return "best friends"; case 2: return "close friends"; case 1: return "friends"; case 0: return "strangers"; case -1: return "dislike each other"; case -2: return "rivals"; case -3: return "arch-enemies"; default: return "wanted"; }
    }

    // ------------------------------------------------------------------ gear, techniques
    private static void unbreakable(Character c, String[] t) {
        if (!has(t, 2)) { say("Usage: cheat unbreakable weapon|armor|both|off [self|#id]"); return; }
        Character x = lastTarget(c, t, 3); if (x == null) return;
        String w = t[2].toLowerCase();
        x.unbreakable = w.startsWith("w") ? 1 : w.startsWith("a") ? 2 : w.startsWith("b") ? 3 : 0;
        say(who(x) + ": " + (x.unbreakable == 0 ? "gear can break again." : "his " + (x.unbreakable == 1 ? "weapon" : x.unbreakable == 2 ? "armor" : "weapon and armor") + " will never break."));
    }

    private static void technique(Character c, String[] t) {
        if (t.length < 5) { say("Usage: cheat technique <martial|martial2|movement|movement2|known N> level|potential <n> [self|#id]"); return; }
        int ai = t[2].equalsIgnoreCase("known") ? 4 : 3;
        Character x = lastTarget(c, t, ai + 2); if (x == null) return;
        Technique q;
        String slot = t[2].toLowerCase();
        String what; int v;
        if (slot.equals("known")) {
            int n = PlayerCommands.num(t[3], 0);
            if (x.techniqueList == null || n < 1 || n > x.techniqueList.size()) { say("No known technique number " + n + " (" + (x.techniqueList == null ? 0 : x.techniqueList.size()) + " known)."); return; }
            q = x.techniqueList.get(n - 1); what = t[4].toLowerCase(); v = PlayerCommands.num(t[5 > t.length - 1 ? 4 : 5], 0);
        } else {
            q = slot.equals("martial") ? x.martialTechnique : slot.equals("martial2") ? x.martialTechnique2 : slot.equals("movement") ? x.movementTechnique : slot.equals("movement2") ? x.movementTechnique2 : null;
            what = t[3].toLowerCase(); v = PlayerCommands.num(t[4], 0);
        }
        if (q == null) { say("That slot is empty."); return; }
        if (what.startsWith("l")) { q.lvl = Math.max(1, v); if (q.potential < q.lvl) q.potential = q.lvl; }
        else if (what.startsWith("p")) { q.potential = Math.max(1, v); if (q.lvl > q.potential) q.lvl = q.potential; }
        else { say("level or potential."); return; }
        x.tempLvlCheck();
        say(who(x) + ": " + q.name + " is now level " + q.lvl + ", potential " + q.potential + ".");
    }

    private static void teleport(Character c, String[] t) {
        if (t.length < 4) { say("Usage: cheat teleport <x> <y> [self|#id] [squad]   (tile numbers, or the coordinates shown, which are 100 times the tile)"); return; }
        int x = (int) PlayerScript.tile(PlayerCommands.num(t[2], -1)), y = (int) PlayerScript.tile(PlayerCommands.num(t[3], -1));
        Character who = lastTarget(c, t, 4); if (who == null) return;
        if (x < 0 || x >= Map.range || y < 0 || y >= Map.range) { say("That is off the map (0-" + (Map.range - 1) + ")."); return; }
        Area a = Map.tiles[x][y];
        if (a == null) { say("That tile has not been made yet: walk near it first."); return; }
        moveTo(who, a);
        say(who(who) + " is now at " + PlayerControl.coord(a) + " (" + a.name + ").");
        boolean squad = false;
        for (String w : t) if (w.equalsIgnoreCase("squad")) squad = true;
        if (squad) { int n = 0; for (Character m : who.squadMembers()) { moveTo(m, a); n++; } say(n + " member(s) of the squad come along."); }
    }

    private static void moveTo(Character who, Area a) {
        if (who.location != null) who.location.removeChar(who);
        who.location = a; a.addChar(who); who.learnPlace(a);
        who.autoX = -1; who.autoY = -1; who.chaseId = -1; who.movement = 'x'; who.skipping = 0;
    }

    private static void give(Character c, String[] t) {
        if (t.length < 4) { say("Usage: cheat give weapon|armor|array|instrument <level> | pill cultivation|potential <level> <n> | herb|metal <level> <n> | blood <n> | technique martial|movement <potential>  [self|#id]"); return; }
        Character x = lastTarget(c, t, 5); if (x == null) return;
        String k = t[2].toLowerCase();
        int lvl = Math.max(1, Math.min(10, PlayerCommands.num(t[3], 1)));
        int gearLvl = Math.max(1, Math.min(100, PlayerCommands.num(t[3], 1))); // weapons, clothing and arrays run 1-100
        Item it = null; String said = null;
        switch (k) {
            case "weapon": it = new Item(x, Item.WeaponType.values()[GameDriver.rand.nextInt(Item.WeaponType.values().length)], gearLvl); x.backpack.add(it); x.equip(); said = it.toString(); break;
            case "armor": case "clothing": it = new Item(x, gearLvl, Item.ItemType.Clothing); x.backpack.add(it); x.equip(); said = it.toString(); break;
            case "array": it = new Item(x, gearLvl, Item.ItemType.Array); Character.addToBag(x.arrayBag, it); said = it.toString(); break;
            case "instrument": it = new Item(x, lvl, Item.ItemType.Instrument); x.backpack.add(it); said = it.toString(); break;
            case "pill": {
                if (t.length < 6) { say("Usage: cheat give pill cultivation|potential <level> <n>"); return; }
                boolean pot = t[3].toLowerCase().startsWith("p");
                int pl = Math.max(1, Math.min(11, PlayerCommands.num(t[4], 1))), n = Math.max(1, PlayerCommands.num(t[5], 1));
                it = new Item(Item.ItemType.Pill, pot ? Item.PillType.Potential : Item.PillType.Cultivation, pot ? 11 : pl, n);
                Character.addToBag(x.pillBag, it); said = n + " x " + it.name; break;
            }
            case "herb": case "metal": {
                if (t.length < 5) { say("Usage: cheat give herb|metal <level> <n>"); return; }
                int n = Math.max(1, PlayerCommands.num(t[4], 1));
                Item.ItemType ty = k.equals("herb") ? Item.ItemType.Herb : Item.ItemType.Metal;
                boolean merged = false;
                for (Item m : x.materialBag) if (m.itemType == ty && m.lvl == lvl) { m.quantity += n; merged = true; break; }
                if (!merged) x.materialBag.add(new Item(ty, lvl, n));
                said = n + " " + k + " of quality " + lvl; break;
            }
            case "blood": {
                int n = Math.max(1, PlayerCommands.num(t[3], 1000));
                boolean merged = false;
                for (Item m : x.materialBag) if (m.itemType == Item.ItemType.Blood) { m.quantity += n; merged = true; break; }
                if (!merged) x.materialBag.add(new Item(Item.ItemType.Blood, n));
                said = n + " Blood"; break;
            }
            case "technique": {
                int pot = Math.max(1, Math.min(100, PlayerCommands.num(t[4 > t.length - 1 ? 3 : 4], 10)));
                Technique.techniqueType ty = t[3].toLowerCase().startsWith("mo") && t[3].toLowerCase().startsWith("mov") ? Technique.techniqueType.Movement : Technique.techniqueType.Martial;
                Technique q = new Technique(ty, x, pot);
                for (String w : t) { for (int e = 0; e < 5; e++) if (w.equalsIgnoreCase(Elements.NAMES[e])) { q.elemPlus1 = (byte) (e + 1); q.name = Naming.technique(ty, x, e); } }
                q.lvl = 1;
                x.techniqueList.add(q); x.equip();
                said = q.name + " [" + Elements.name(q.element()) + "] (potential " + q.potential + ")"; break;
            }
            default: say("Unknown item kind '" + k + "'."); return;
        }
        say(who(x) + " receives " + said + ".");
    }

    // ------------------------------------------------------------------ companions
    private static void tame(Character c, String[] t) {
        int lvl = has(t, 2) ? Math.max(1, Math.min(120, PlayerCommands.num(t[2], c.lvl))) : Math.max(1, c.lvl);
        String name = has(t, 3) ? String.join(" ", java.util.Arrays.copyOfRange(t, 3, t.length)) : null;
        Character b = Taming.legend(c, name == null ? "Companion" : name, "Beast", lvl);
        if (name == null) { b.name = Naming.petName(b); b.lastName = "Beast"; }
        say(b.name + " " + b.lastName + " (Lv." + b.lvl + ") is your companion now (" + Taming.beasts(c).size() + " kept; the cap does not count for a cheat).");
    }

    private static void companion(Character c, String[] t) {
        if (t.length < 5) { say("Usage: cheat companion <n> level|potential|name <value>   (n = the companion's place in the Companions list)"); return; }
        List<Character> bs = Taming.beasts(c);
        int n = PlayerCommands.num(t[2], 0);
        if (n < 1 || n > bs.size()) { say("You keep " + bs.size() + " companion(s)."); return; }
        Character b = bs.get(n - 1);
        String what = t[3].toLowerCase();
        if (what.startsWith("n")) { b.name = String.join(" ", java.util.Arrays.copyOfRange(t, 4, t.length)); say("Renamed: " + b.name + " " + b.lastName + "."); return; }
        int v = PlayerCommands.num(t[4], 1);
        if (what.startsWith("l")) { setLevel(b, Math.max(1, v)); say(b.name + " is now level " + b.lvl + "."); }
        else if (what.startsWith("p")) { b.potential = Math.max(1, Math.min(100, v)); say(b.name + " now has potential " + b.potential + "."); }
        else say("level, potential or name.");
    }

    /** Sets a creature's level the way the first cheats do: realm, bar and weekly gain follow. */
    static void setLevel(Character x, int lvl) {
        x.lvl = lvl; x.majorLvl = Math.max(0, (lvl - 1) / 10); x.minorLvl = Math.max(1, lvl - 10 * x.majorLvl); x.tempLvl = x.lvl;
        x.cultivationBase = 0;
        x.cultivationBaseMax = Math.pow(x.lvl, 3) * (((70. + 1. / 3.) + ((11. / 6.) * x.lvl)) / 25.);
        x.cultivation = (Math.pow(x.potential, 2) * Math.sqrt(x.lvl) * 10.) / (52. * Math.sqrt(Math.max(1, x.potential)) * Math.pow(2, x.inhibitor));
    }

    // ------------------------------------------------------------------ marks and policies
    private static void flag(Character c, String[] t, String which) {
        if (!has(t, 2)) { say("Usage: cheat " + which + " on|off [self|#id]"); return; }
        boolean on = PlayerCommands.onOff(t[2]);
        Character x = lastTarget(c, t, 3); if (x == null) return;
        switch (which) {
            case "evade": if (x.pc == null) { say("Only a person who plays can refuse death by policy (use cheat nodeath for an NPC)."); return; } x.pc.evadeDeath = on; say(who(x) + ": when death comes " + (on ? "he simply goes on living (also while AUTO)." : "it takes him again.")); break;
            case "nodeath": x.noDeath = on; say(who(x) + (on ? " carries the No Death mark: only a person who plays can kill them." : " is mortal again.")); break;
            case "win": x.cheatWin = on; say(who(x) + (on ? " wins every fight now (and a fight against them is always lost)." : " fights fair again.")); break;
            default: x.cheatNoEscape = on; say("Nobody can flee from " + who(x) + (on ? " now." : " any more: escapes work again.")); break;
        }
    }

    // ------------------------------------------------------------------ the dead
    private static void resurrect(Character c, String[] t) {
        synchronized (graveyard) {
            if (!has(t, 2)) {
                StringBuilder sb = new StringBuilder("The recently dead (the last " + GRAVEYARD + "): cheat resurrect <#id|part of a name>\n");
                int shown = 0;
                List<Character> l = new ArrayList<>(graveyard.values());
                for (int i = l.size() - 1; i >= 0 && shown < 25; i--, shown++) { Character d = l.get(i); sb.append("  #").append(d.id).append(' ').append(d.name).append(' ').append(d.lastName).append("  Lv.").append(d.lvl).append(d.deathCause == null ? "" : "  (" + d.deathCause + ")").append('\n'); }
                if (l.isEmpty()) sb.append("  (nobody has died yet)\n");
                say(sb.toString());
                return;
            }
            Character d = null;
            String key = String.join(" ", java.util.Arrays.copyOfRange(t, 2, t.length)).trim();
            if (key.matches("#?\\d+")) d = graveyard.get(Integer.parseInt(key.replace("#", "")));
            else for (Character g : graveyard.values()) if ((g.name + " " + g.lastName).toLowerCase().contains(key.toLowerCase())) d = g;
            if (d == null) { say("Nobody of that name is among the recent dead ('cheat resurrect' lists them)."); return; }
            if (d.player >= 1) { say(who(d) + " plays: their life goes on through reincarnation, not this cheat."); return; }
            if (!d.dead) { say(who(d) + " is alive."); graveyard.remove(d.id); return; }
            boolean pending = Map.remCharacters.contains(d); // died this very week: still in the list, waiting to be removed at the end of the tick
            if (d.id >= Map.characters.size() || (Map.characters.get(d.id) != null && Map.characters.get(d.id) != d)) { say("Their place in the world is taken: they cannot be called back."); return; }
            graveyard.remove(d.id);
            Map.remCharacters.remove(d);
            Map.characters.set(d.id, d);
            d.dead = false; d.deathCause = null; d.reincarnating = false; d.smitten = false;
            d.age = Math.min(d.age, Math.max(0, d.maxAge - 5));
            d.cultivation = Math.max(1, d.cultivation);
            if (d.location != null) d.location.deadCharacters.remove(d);
            d.location = c.location; c.location.addChar(d); d.learnPlace(c.location);
            d.targetQuests.clear(); d.spotQuests.clear(); d.currentQuest = null;
            if (d.trait == Character.Traits.Monster) { d.monsterQuestInitializer(); Map.addMonsterOnce(d); } else d.questInitializer();
            if (!pending) Map.addPopulation(1);
            say(who(d) + " rises and stands before you at " + PlayerControl.coord(c.location) + ".");
            Chronicle.add("world", d.name + " " + d.lastName + " was called back from the dead.", d.id);
        }
    }

    // ------------------------------------------------------------------ creating
    private static void create(Character c, String[] t) {
        if (!has(t, 2)) { say("Usage: cheat create npc [major 0-10] [trait] | monster <level> [name] | beast <how many>"); return; }
        Area here = c.location;
        switch (t[2].toLowerCase()) {
            case "beast": { int n = Math.max(1, Math.min(1000, has(t, 3) ? PlayerCommands.num(t[3], 1) : 1)); here.monsters += n; say(n + " wild beast(s) now live here (" + here.monsters + ")."); return; }
            case "monster": {
                int lvl = Math.max(1, Math.min(150, has(t, 3) ? PlayerCommands.num(t[3], c.lvl) : c.lvl));
                Character b = new Character(here, c);
                setLevel(b, lvl);
                b.awakenMonster(); // (it wakes below the cap: a Monster is never above the 20th strongest cultivator)
                if (has(t, 4)) b.name = String.join(" ", java.util.Arrays.copyOfRange(t, 4, t.length));
                say("A Monster wakes here: " + b.name + " " + b.lastName + " (Lv." + b.lvl + (b.lvl < lvl ? ", brought down to the cap " + Monsters.cap() : "") + ", " + Elements.name(Elements.of(b)) + ").");
                return;
            }
            case "npc": {
                Character n = new Character(here);
                Starts.prehistoryOne(n);
                if (has(t, 3) && t[3].matches("\\d+")) { int major = Math.max(0, Math.min(10, PlayerCommands.num(t[3], 0))); setLevel(n, major * 10 + 1); }
                for (String w : t) for (Character.Traits tr : Character.Traits.values()) if (tr != Character.Traits.Beast && tr != Character.Traits.Monster && tr.name().equalsIgnoreCase(w)) n.trait = tr;
                n.born = true;
                Map.addTemp(n);
                say("A new person stands here: " + n.name + " " + n.lastName + " #" + n.id + " (Lv." + n.lvl + ", " + n.getTrait() + "). They join the world at the end of this week.");
                return;
            }
            default: say("npc, monster or beast.");
        }
    }

    // ------------------------------------------------------------------ cities and sects
    private static void city(Character c, String[] t) {
        Area a = c.location;
        if (a == null || !a.city) { say("You are not standing in a city."); return; }
        if (!has(t, 2)) { say("Usage: cheat city population|treasury <n> | owner <#id|self> | rename <name> | raze"); return; }
        switch (t[2].toLowerCase()) {
            case "population": a.population = (int) Math.max(1, Math.min(2_000_000_000L, PlayerCommands.num(t[3 > t.length - 1 ? 2 : 3], 1000))); say(a.name + " now holds " + a.population + " people."); break;
            case "treasury": a.cityTreasury = Math.max(0, PlayerCommands.num(t[3 > t.length - 1 ? 2 : 3], 0)); say(a.name + "'s treasury holds " + a.cityTreasury + " ss (city level " + a.cityLevel() + ")."); break;
            case "owner": { Character x = target(c, has(t, 3) ? t[3] : "self"); if (x == null) return; a.cityOwner = x; say(who(x) + " is the lord of " + a.name + "."); break; }
            case "rename": if (!has(t, 3)) { say("Usage: cheat city rename <name>"); return; } a.name = String.join(" ", java.util.Arrays.copyOfRange(t, 3, t.length)); say("The city is now called " + a.name + "."); break;
            case "raze": c.destroyCity(a); say(a.name + " is razed."); break;
            default: say("population, treasury, owner, rename or raze.");
        }
    }

    private static void sect(Character c, String[] t) {
        Organization s = c.sect != null ? c.sect : c.location != null ? c.location.sect : null;
        if (s == null) { say("There is no sect here and you belong to none."); return; }
        if (!has(t, 2)) { say("Usage: cheat sect master|join|expel <#id> | rename <name>"); return; }
        switch (t[2].toLowerCase()) {
            case "rename": if (!has(t, 3)) { say("Usage: cheat sect rename <name>"); return; } s.name = String.join(" ", java.util.Arrays.copyOfRange(t, 3, t.length)); say("The sect is now called " + s.name + "."); break;
            case "join": case "master": case "expel": {
                Character x = target(c, has(t, 3) ? t[3] : "self"); if (x == null) return;
                if (t[2].equalsIgnoreCase("expel")) { s.resign(x); say(who(x) + " is cast out of " + s.name + "."); break; }
                if (x.sect != null && x.sect != s) x.sect.resign(x);
                if (x.sect != s) s.forceJoin(x);
                if (t[2].equalsIgnoreCase("master")) { s.master = x; say(who(x) + " is the master of " + s.name + "."); } else say(who(x) + " joins " + s.name + ".");
                break;
            }
            default: say("master, join, expel or rename.");
        }
    }
}
