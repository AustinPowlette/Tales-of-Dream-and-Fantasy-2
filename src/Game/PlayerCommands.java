package Game;

import java.util.ArrayList;
import java.util.Arrays;

/** Text-command dispatcher for human-controlled characters (MANUAL mode and the mode/quest/duty/script tools that work in every mode).
 *  handle() returns true when the command uses up the character's turn, false when it was only information or setup (re-prompt).
 *  The old single-letter keys (w a s d i l r q e z f p o) keep working. See planning.md for the design. */
public class PlayerCommands {

    static final String HELP =
            "=== Commands ===\n"
          + "Move:    w a s d | goto <x> <y> | wait [n] | walk <w|a|s|d> <n> | o (auto-move menu) | stay\n"
          + "Modes:   auto [turns] | manual | script run|resume|stop | stop\n"
          + "Duties:  duties | duties all on|off | duties <city|sect|shop|craft|upkeep|bounty|tournament|social|family|loot> on|off\n"
          + "Policy:  policy | policy kill <0-3> | policy flee|forbidden on|off | policy duel accept|evade | policy script auto|manual | policy alerts on|off | policy timeout <seconds|off>\n"
          + "Quests:  q | qp <n> <priority> | qu <n> | qd <n> | qdo <n> | mute <name> | unmute <name> | qnew <label> <x> <y> <pri> [repeat] | inbox\n"
          + "Info:    i (stats) | l (places) | r (relationships) | auction | here | people [name] | bag | shops | city | sect | party | library\n"
          + "Lists:   list people|monsters|shop|bag|places [limit] [key=val key>=n ...] [sort=key desc]  e.g. 'list people 50 lvl>=20 aff=evil sort=lvl desc' | limit <n> (default rows)\n"
          + "Pick:    anywhere a <person> is wanted use a list number OR #<id> (same tile): talk #123 | spar <n> | attack <n> | invite <n> | leave | disband   (leave the person out of talk/spar/attack/invite/recruit and one suitable person here is picked at random)\n"
          + "Seek:    seek <id> friend|fight|spar [priority]  — pins a quest to reach a cultivator anywhere in the world\n"
          + "Shops:   list shop [type=weapon lvl>=30 price<=5k afford=1 sort=price] | buy <#> | buy <shop>.<item> <name> | sell <bag#>\n"
          + "Work:    forage | mine | profession | craft [kind] | meditate | hunt | sell | buygear | consume | equip | install | learn <n> | gift <person#> <item#>\n"
          + "Roles:   found city|sect | join | resign | recruit <n> | promote <n> | war | peace | alliance\n"
          + "Shop:    price <shop#> buy|sell|city <rate>\n"
          + "Do:      do <quest name> [n]   (Donate to Sect, Buy City Array, Hold Tournament, Restock Trip, Have Child, ...)\n"
          + "Script:  script add <step or any command> | list | del/edit/insert/move <n> | clear | run | save/load <name> | saved   (steps: goto wait forage mine craft meditate hunt sell quest repeat if end mode; any other word is a command)\n"
          + "Look:    map [radius] | route <x> <y> | time | journal [n] | history [text|#id] [kind=...] | note <text> | notes | dash | help <topic>\n"
          + "Gear:    bag | equip [n] | compare <n> | techniques | library | learn <n>\n"
          + "Market:  market | buy market <#> [qty] | sell goods|food|wine|poison [qty]\n"
          + "Life:    tame | companions | release <n> | feed <n> | goods | make food|wine|poison | eat [n] | drink [n] | poison <person> | coat | play | perform | reincarnate | lineage | unequip weapon|armor\n"
          + "Look+:   digest [weeks] | map level|beasts|people|sect | watch [weeks] [for <categories>] [until year <n>]\n"
          + "Run:     runstats | halloffame | retire | export run\n"
          + "Wait:    watch [weeks]  — pass time until a notice, alert or attack\n"
          + "System:  e (edit self) | z (cheats) | f (save) | p (pause) | help\n";

    // Quests that may be run on demand with 'do' (spot quests validate themselves; area quests use the current tile; character quests take a person number)
    static final java.util.Set<String> DO_SPOT = new java.util.HashSet<>(Arrays.asList(
            "Donate to Sect", "Buy from Sect", "Buy City Array", "Buy Sect Array", "Entice Merchant", "Hold Tournament",
            "Register Tournament", "Spectate Tournament", "Accept Sect Mission", "Loot Corpse", "Sell Technique", "Meditate", "Reincarnate"));
    static final java.util.Set<String> DO_AREA = new java.util.HashSet<>(Arrays.asList(
            "Buy Gear", "Restock Trip", "Sell Stock", "Seek Potential", "Join Sect", "Deliver Materials", "Donate Array",
            "Heaven Treasure", "Explore Dungeon", "Hunt Beast", "Sect Raid", "Find Lost Beast", "Festival Trade", "Perform",
            "Trade Run", "Pilgrimage", "Meditation Retreat", "Tame Beast", "Buy Pills", "Attend Auction", "Found City", "Found Sect", "Market Trip", "Gathering Trip", "Sightseeing", "Courier Run"));
    static final java.util.Set<String> DO_PERSON = new java.util.HashSet<>(Arrays.asList(
            "Propose Alliance", "Demand Tribute", "Recruit Member", "Recruit Defector", "Get Pregnant", "Gift Technique", "Visit Family", "Teach Disciple", "Courtship", "Seek Teacher"));

    static final int MAX_WAIT = 5200; // longest 'wait' / 'walk' in turns (100 years): a typo like 'wait 99999999' is cut to this with a notice

    static void say(String s) { System.out.println(s); }

    /** A person in the same tile: `#<id>` picks by character id (works for anyone in the tile, listed or not), a plain number picks a row of the last list. */
    static Character person(Character c, String arg) {
        if (arg != null && arg.startsWith("#")) {
            int id;
            try { id = Integer.parseInt(arg.substring(1)); } catch (Exception e) { say("Use #<id>, e.g. #1234."); return null; }
            Character p = Character.charById(id);
            if (p == null || p.dead) { say("No living cultivator has id " + id + "."); return null; }
            if (p == c) { say("That's you."); return null; }
            if (p.location != c.location) {
                say(p.name + " " + p.lastName + " (#" + id + ") is at " + PlayerControl.coord(p.location) + ", " + PlayerControl.distance(c.location, p.location)
                        + " tiles away; you are at " + PlayerControl.coord(c.location) + ". Make it a quest with: seek " + id + " friend|fight|spar");
                return null;
            }
            return p;
        }
        ArrayList<Character> view = c.pc().peopleView;
        int n;
        try { n = Integer.parseInt(arg); } catch (Exception e) { say("Give the person's number from 'people'."); return null; }
        if (view == null || n < 1 || n > view.size()) {
            if (n > 0 && Character.charById(n) != null) return person(c, "#" + n); // a number that is no row of the list but is somebody's id (invite 34401): take it as the id
            say("No person #" + n + (view == null || view.isEmpty() ? ". Use 'people' first (or #<id> for an id)." : " in the list of " + view.size() + ". Use 'people' to see it again (or #<id> for an id).")); return null;
        }
        Character p = view.get(n - 1);
        if (p == null || p.dead || p.location != c.location) { say("They are no longer here. Use 'people' again."); return null; }
        return p;
    }

    /** The person a command acts on: an explicit list number / #id if given, otherwise someone picked at random from the tile
     *  (only people the verb makes sense for — e.g. never a child for spar/attack, never a friend for attack). */
    static Character pick(Character c, String[] t, int argIdx, String verb) {
        if (t.length > argIdx) return person(c, t[argIdx]);
        if (verb.equals("attack")) { say("Usage: attack <person#>   (numbers from 'people'; an attack is never aimed at a stranger picked for you)"); return null; }
        ArrayList<Character> pool = new ArrayList<>();
        for (Character p : new ArrayList<>(c.location.characters)) {
            if (p == null || p.dead || p == c || p.trait == Character.Traits.Beast || !p.born) continue;
            Relationship r = c.relationshipList.contains(p.id);
            switch (verb) {
                case "spar": if (p.age < 8) continue; break;
                case "attack": if (p.age < 8 || p == c.spouse || (r != null && r.value >= 1)) continue; break;
                case "invite": if (p.player >= 1 || p.partyLeaderId >= 0 || p.age < 8) continue; break;
                case "recruit": if (c.sect == null || !c.sect.canJoin(p)) continue; break;
                default: break;
            }
            pool.add(p);
        }
        if (pool.isEmpty()) { say("Nobody here is suitable to " + verb + ". ('people' shows who is here; cities and busy tiles hold more people.)"); return null; }
        Character p = pool.get(GameDriver.rand.nextInt(pool.size()));
        say("(No target given — picked " + p.name + " " + p.lastName + " #" + p.id + ", Lv." + p.lvl + " at random.)");
        return p;
    }

    static int num(String s, int fallback) { return TermFormat.parseInt(s, fallback); } // never throws; a number too big for an int is clamped

    static boolean isBool(String s) { return s.equalsIgnoreCase("on") || s.equalsIgnoreCase("off") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("no") || s.equals("1") || s.equals("0"); }
    static boolean onOff(String s) { return s.equalsIgnoreCase("on") || s.equalsIgnoreCase("true") || s.equals("1") || s.equalsIgnoreCase("yes"); }

    /** Runs one command line for character c. Returns true if a turn passes. (TermInput adds repeat/history, aliases and an error net, then calls dispatch.) */
    static boolean handle(Character c, String line) { return TermInput.run(c, line); }

    /** Commands that only show information (no turn, no change). A paused game answers these at once, so the buttons that open pop-ups still work while paused. */
    static final java.util.Set<String> INFO_VERBS = new java.util.HashSet<>(java.util.Arrays.asList(
            "time", "date", "preview", "route", "library", "auction", "market", "goods", "shops", "shop", "bag", "stats", "i", "l", "places", "r", "relationships", "relations", "here", "people", "players",
            "monsters", "list", "ls", "city", "sect", "party", "help", "?", "map", "companions", "beasts", "techniques", "tech", "compare", "journal", "history", "chronicle", "fights", "fightlog",
            "digest", "notes", "dash", "dashboard", "find", "q", "quests", "inbox", "family", "story", "runstats", "fame", "halloffame", "lineage", "mission", "missions", "squad", "craft"));

    static boolean isInfoCommand(String line) {
        if (line == null) return false;
        String l = line.trim().toLowerCase();
        if (l.startsWith("!")) l = l.substring(1);
        int sp = l.indexOf(' ');
        String verb = sp < 0 ? l : l.substring(0, sp);
        if (verb.equals("craft")) return sp >= 0 && l.substring(sp + 1).trim().equals("menu"); // only the menu looks
        if (verb.equals("mission") || verb.equals("missions")) return sp < 0;   // listing the board looks; taking one does not
        return INFO_VERBS.contains(verb);
    }

    /** The command switch itself. */
    static boolean dispatch(Character c, String line) {
        if (line == null) return true;
        line = line.trim();
        boolean confirmed = false;
        if (line.startsWith("!")) { confirmed = true; line = line.substring(1).trim(); } // "!cmd" = the player already confirmed this (GUI dialogs / repeat)
        if (line.isEmpty()) return false;
        String[] t = line.split("\\s+");
        String cmd = t[0].toLowerCase();
        PlayerControl pc = c.pc();
        switch (cmd) {
            // ------------------------------------------------ movement
            case "w": case "a": case "s": case "d":
                c.movement(cmd.charAt(0));
                return true;
            case "nw": case "ne": case "sw": case "se":
                c.movement(cmd.equals("nw") ? 'Q' : cmd.equals("ne") ? 'E' : cmd.equals("sw") ? 'Z' : 'C'); // diagonal: one turn, like the pathfinder
                return true;
            case "x": case "stay":
                say("You stay where you are for a week.");
                c.movement('x');
                return true;
            case "wait": {
                if (t.length > 1 && (!t[1].matches("\\d+") || t[1].matches("0+"))) { say("Usage: wait [weeks]   e.g. wait 10   ('watch' waits until something happens)"); return false; }
                int n = t.length > 1 ? Math.max(1, TermFormat.cap(num(t[1], 1), 1, MAX_WAIT, "wait")) : 1;
                say("You wait " + n + (n == 1 ? " week" : " weeks") + (n > 1 ? " (an alert or an attack ends it early)." : "."));
                c.movement = 'x';
                c.skipping = n - 1;
                c.movement('x');
                return true;
            }
            case "walk": {
                if (t.length < 3 || t[1].length() != 1 || "wasd".indexOf(t[1].toLowerCase().charAt(0)) < 0 || !t[2].matches("\\d+")) { say("Usage: walk <w|a|s|d> <turns>   e.g. walk d 5  (five steps east); 'goto <x> <y>' walks to a place"); return false; }
                char dir = t[1].toLowerCase().charAt(0);
                c.movement = dir;
                c.skipping = Math.max(1, TermFormat.cap(num(t[2], 1), 1, MAX_WAIT, "walk")) - 1;
                c.movement(dir);
                return true;
            }
            case "quit": case "exit": {
                boolean save = !(t.length > 1 && t[1].equalsIgnoreCase("nosave"));
                if (!confirm(c, confirmed, save ? "Save and quit the game?" : "Quit WITHOUT saving?", line)) return false;
                GameDriver.quitSaves = save; GameDriver.quitRequested = true; GameDriver.paused = false;
                if (save) GameDriver.saveRequested = true;
                say(save ? "Saving, then leaving..." : "Leaving without saving...");
                return true; // ends the turn: the loop saves and exits
            }
            case "goquest": case "gq":
                return goQuestCommand(c, t);
            case "chase": {
                if (t.length < 2) { say("Usage: chase <#id or name>"); return false; }
                Character target = anyone(t[1]);
                if (target == null || target.dead) { say("Nobody found."); return false; }
                return chase(c, target);
            }
            case "goto": {
                if (t.length < 3) { say("Usage: goto <x> <y>   (coordinates as shown in alerts, e.g. 1500 900, or tile numbers 0-" + (Map.range - 1) + ")"); return false; }
                int tx = (int) PlayerScript.tile(num(t[1], -1)), ty = (int) PlayerScript.tile(num(t[2], -1));
                if (tx < 0 || tx >= Map.range || ty < 0 || ty >= Map.range) { say("Those coordinates are off the map."); return false; }
                if (c.location.x == tx && c.location.y == ty) { say("You are already there."); return false; }
                c.autoX = tx; c.autoY = ty; c.chaseId = -1; c.chaseTurns = 0; c.movement = 'c';
                c.skipping = 400; // walks until arrival (the driver hands control back on arrival), an alert, or an attack
                stepAuto(c);
                return true;
            }
            case "o":
                return legacyAutoMove(c);

            // ------------------------------------------------ modes
            case "auto": {
                pc.mode = PlayerControl.Mode.AUTO;
                pc.autoTurns = t.length > 1 ? Math.max(0, num(t[1], 0)) : 0;
                c.skipping = 0;
                say("AUTO mode: your character runs like an NPC" + (pc.autoTurns > 0 ? " for " + pc.autoTurns + " turns." : ". Type 'stop' to take control back."));
                return true;
            }
            case "manual": case "stop":
                if (pc.script != null && pc.script.running) { pc.script.stop(c, "You stopped the script."); return false; }
                pc.mode = PlayerControl.Mode.MANUAL;
                c.skipping = 0;
                say("MANUAL mode.");
                return false;
            case "script":
                return scriptCommand(c, pc, t, confirmed);

            // ------------------------------------------------ duties & policy
            case "duties":
                return dutiesCommand(pc, t);
            case "policy":
                return policyCommand(pc, t);

            // ------------------------------------------------ quests
            case "q": case "quests":
                GameDriver.showGuiInfo("Quests", pc.listQuests(c));
                return false;
            case "inbox": {
                if (pc.inbox.isEmpty()) { say("No quest alerts."); return false; }
                StringBuilder sb = new StringBuilder("=== Quest alerts ===\n");
                for (Quest q : pc.inbox) sb.append("  ").append(pc.describe(c, q)).append("\n");
                sb.append("Travel to the place, then use 'q' to number the quest and 'qdo <n>'.\n");
                GameDriver.showGuiInfo("Quest alerts", sb.toString());
                return false;
            }
            case "qp": {
                if (t.length < 3) { say("Usage: qp <quest#> <priority>   (a pinned priority never ages)"); return false; }
                Quest q = pc.rowRef(c, t[1]);
                if (q == null) return false;
                pc.setPriority(c, q, num(t[2], q.priority));
                say("Pinned " + q.displayName() + " at priority " + q.pinnedPriority + ".");
                return false;
            }
            case "qu": {
                if (t.length < 2) { say("Usage: qu <quest#>   (numbers from 'q')"); return false; }
                Quest q = pc.rowRef(c, t[1]);
                if (q == null) return false;
                say(pc.unpin(c, q) ? q.displayName() + " unpinned; it ages normally again." : "That quest isn't pinned.");
                return false;
            }
            case "qd": {
                if (t.length < 2) { say("Usage: qd <quest#>   (numbers from 'q'); 'mute <name>' also keeps that kind from coming back"); return false; }
                Quest q = pc.rowRef(c, t[1]);
                if (q == null) return false;
                if ((q.pinned || q.userCreated) && !confirm(c, confirmed, "Delete your " + (q.userCreated ? "own" : "pinned") + " quest '" + q.displayName() + "'?", line)) return false;
                pc.delete(c, q);
                if ("Sect Mission".equals(q.name)) c.abandonMission();
                say("Deleted " + q.displayName() + ". (Use 'mute " + q.name + "' to stop it coming back.)");
                return false;
            }
            case "qdo": {
                if (t.length < 2) return doTopQuest(c, pc); // the GUI's "Do top quest" button (plain 'qdo' = the top quest)
                Quest q = pc.rowRef(c, t[1]);
                if (q == null) return false;
                String why = c.doQuestNow(q);
                if (why != null) { say(why); return false; }
                return true;
            }
            case "mute": {
                if (t.length < 2) { say("Usage: mute <quest name>  (e.g. mute Find Person)"); return false; }
                String name = Quest.canon(String.join(" ", Arrays.copyOfRange(t, 1, t.length)));
                final String shown = Quest.shown(name);
                if (!confirm(c, confirmed, "Mute '" + shown + "'? Every queued one is removed and none will be posted again (unmute reverses it).", line)) return false;
                int n = pc.mute(c, name);
                say("Muted '" + shown + "': removed " + n + " queued and it will not be posted again.");
                return false;
            }
            case "ranking": case "rankings": case "board": case "boards": {
                GameDriver.showGuiInfo("Rankings", Rankings.text(t.length > 1 ? t[1] : null, c));
                return false;
            }
            case "spotaction": case "idle": {
                if (t.length > 1) {
                    int found = -1;
                    for (int i = 0; i < PlayerControl.SPOT_ACTIONS.length; i++) if (PlayerControl.SPOT_ACTIONS[i].toLowerCase().startsWith(t[1].toLowerCase())) found = i;
                    if (found < 0) { say("Usage: spotaction meditate|hunt|profession|random"); return false; }
                    pc.spotAction = found;
                }
                say("While your character travels or waits and its routine would do a plain spot quest, it does this: " + PlayerControl.SPOT_ACTIONS[pc.spotAction] + ". ('spotaction meditate|hunt|profession|random' changes it; Hunt needs beasts in the tile, else it meditates.)");
                return false;
            }
            case "unmute": {
                String name = Quest.canon(String.join(" ", Arrays.copyOfRange(t, 1, t.length)));
                say(pc.muted.remove(name) ? "'" + name + "' can be posted again." : "'" + name + "' was not muted. Muted: " + pc.muted);
                return false;
            }
            case "qnew": {
                if (t.length < 5) { say("Usage: qnew <label> <x> <y> <priority> [repeat]"); return false; }
                int tx = (int) PlayerScript.tile(num(t[2], -1)), ty = (int) PlayerScript.tile(num(t[3], -1));
                boolean repeat = t.length > 5 && t[5].equalsIgnoreCase("repeat");
                Quest q = pc.createWaypoint(c, t[1], tx, ty, num(t[4], 50), repeat);
                if (q != null) say("Created " + pc.describe(c, q) + (repeat ? " (repeating)" : "") + ". AUTO mode and scripts walk there; in MANUAL you'll be alerted.");
                return false;
            }

            // ------------------------------------------------ info
            case "i": case "stats":
                if (GameDriver.guiEnabled && GameDriver.mapViewer != null) javax.swing.SwingUtilities.invokeLater(() -> GuiSheets.statsWindow(GameDriver.mapViewer)); else stats(c);
                return false;
            case "l": case "places":
                if (GameDriver.guiEnabled && GameDriver.mapViewer != null) javax.swing.SwingUtilities.invokeLater(() -> GuiSheets.locationsWindow(GameDriver.mapViewer)); else places(c);
                return false;
            case "r": case "relationships":
                if (GameDriver.guiEnabled && GameDriver.mapViewer != null) javax.swing.SwingUtilities.invokeLater(() -> GuiSheets.relationsWindow(GameDriver.mapViewer)); else relationships(c);
                return false;
            case "squad":
                if (GameDriver.guiEnabled && GameDriver.mapViewer != null) javax.swing.SwingUtilities.invokeLater(() -> GuiSheets.squadWindow(GameDriver.mapViewer)); else party(c);
                return false;
            case "cheat":
                return cheatCommand(c, t);
            case "music":
                return musicCommand(t);
            case "here":
                here(c);
                return false;
            case "people": case "players":
                listCommand(c, "people", t, 1);
                return false;
            case "monsters":
                listCommand(c, "monsters", t, 1);
                return false;
            case "list": case "ls": {
                String what = t.length > 1 ? PlayerLists.kind(t[1]) : null;
                if (what == null) { say("Usage: list people|monsters|shop|bag|places [limit] [filters] [sort=key [desc]]   e.g. list people 50 lvl>=20 aff=evil sort=lvl desc"); return false; }
                listCommand(c, what, t, 2);
                return false;
            }
            case "limit": {
                int n = t.length > 1 ? num(t[t.length - 1], -1) : -1;
                if (n < 1 || n > ListQuery.MAX_LIMIT) { say("Usage: limit <1-" + ListQuery.MAX_LIMIT + ">   (currently " + pc.listLimit + " rows per list)"); return false; }
                pc.listLimit = n;
                say("Lists now show up to " + n + " rows. A number on any list command overrides it once.");
                return false;
            }
            case "buy":
                return buyCommand(c, t);
            case "seek":
                return seekCommand(c, t);
            case "bag":
                bag(c);
                return false;
            case "shops":
                shops(c);
                return false;
            case "city":
                if (t.length > 1) TextTools.showPlaces(String.join(" ", Arrays.copyOfRange(t, 1, t.length)), "city"); else city(c);
                return false;
            case "sect":
                if (t.length > 1 && t[1].equalsIgnoreCase("rules")) {
                    Organization s = c.sect != null ? c.sect : c.location.sect;
                    if (s == null) { say("You belong to no sect and none is here."); return false; }
                    if (t.length == 2) { say(s.name + " — recruitment rules: " + s.rulesText() + (s.master == c ? "\nChange them with: sect rules potential <n> level <n> gender male|female|any affiliation good|neutral|evil|any trait <name|any> family <name|any>   (or: sect rules open)" : "")); return false; }
                    if (s.master != c) { say("Only the master of " + s.name + " can change its rules."); return false; }
                    say(s.setRules(t, 2));
                    return false;
                }
                if (t.length > 1) TextTools.showPlaces(String.join(" ", Arrays.copyOfRange(t, 1, t.length)), "sect"); else sect(c);
                return false;
            case "party":
                party(c);
                return false;
            case "library":
                library(c, t);
                return false;
            case "auction":
                auction(c);
                return false;
            case "help": case "?":
                TermHelp.help(t); // overview by category / help <category> / help <command> / help all
                return false;
            case "map":
                TextTools.show(TextTools.map(c, t));
                return false;

            // ------------------------------------------------ the arts of life
            case "tame": {
                String why = Taming.tame(c);
                if (why != null) say(why);
                return why == null || why.startsWith("The ");
            }
            case "companions": case "beasts":
                GameDriver.showGuiInfo("Companions", Taming.describe(c));
                return false;
            case "release": {
                int n = t.length > 1 ? num(t[1], -1) : -1;
                if (n < 1 || n > Taming.beasts(c).size()) { say("Usage: release <n>   (numbers from 'companions'; you have " + Taming.beasts(c).size() + ")"); return false; }
                if (!confirm(c, confirmed, "Send companion #" + n + " back to the wild for good?", line)) return false;
                say(Taming.release(c, n));
                return false;
            }
            case "feed": {
                String r = Taming.feed(c, t.length > 1 ? num(t[1], -1) : -1);
                say(r);
                return r.contains(" eats ");
            }
            case "market": {
                if (!c.location.city) { say("You are not in a city: the goods market is held in cities ('l' lists the places you know)."); return false; }
                GameDriver.showGuiInfo("Market", TermView.market(c));
                return false;
            }
            case "goods": {
                StringBuilder sb = new StringBuilder("=== Food, wine and poison ===\n");
                int i = 1;
                for (Item it : Life.goods(c)) sb.append(String.format("%2d. [%s] %s  (Lv.%d)  x%d%n", i++, it.itemType, it.name, it.lvl, it.quantity));
                if (Life.goods(c).isEmpty()) sb.append(c.trait == Character.Traits.Chef || c.trait == Character.Traits.Drunken || c.trait == Character.Traits.Poisoner ? "  (nothing) — 'make food|wine|poison' from herbs (50 per unit).\n" : "  (nothing) — your trait does not make these; buy some at the 'market' in a city, or try 'craft' as an amateur.\n");
                Life.Fx f = c.fx;
                if (f != null) sb.append("Conditions: ")
                        .append(f.foodWeeks > 0 ? "nourished " + f.foodWeeks + "w  " : "").append(f.drunkWeeks > 0 ? "drunk " + f.drunkWeeks + "w  " : "")
                        .append(f.inspiredWeeks > 0 ? "inspired " + f.inspiredWeeks + "w  " : "").append(f.poisonWeeks > 0 ? "POISONED (Lv." + f.poisonLvl + ") " + f.poisonWeeks + "w  " : "")
                        .append(f.coat > 0 ? "poisoned blade x" + f.coat : "").append("\n");
                GameDriver.showGuiInfo("Goods", sb.toString());
                return false;
            }
            case "make": {
                String what = t.length > 1 ? t[1].toLowerCase() : "";
                Item.ItemType ty = what.startsWith("food") || what.equals("cook") ? Item.ItemType.Food : what.startsWith("wine") || what.equals("brew") ? Item.ItemType.Wine
                        : what.startsWith("poison") || what.equals("mix") ? Item.ItemType.Poison : what.startsWith("instr") ? Item.ItemType.Instrument : null;
                if (ty == null) { say("Usage: make food|wine|poison   (uses 50 herbs per unit; the matching trait does it best)"); return false; }
                if (ty == Item.ItemType.Instrument) { say("Instruments are not made. A Musician's instrument matures as they 'play' or 'perform'; a better one may be for sale in a city."); return false; }
                int r = Life.craft(c, ty);
                if (r == -3) say("Only a " + Life.artist(ty) + (ty == Item.ItemType.Wine ? " (the wine-maker)" : "") + " can make " + (ty == Item.ItemType.Food ? "food" : ty == Item.ItemType.Wine ? "wine" : "poison") + " (your trait is " + c.getTrait() + "). 'craft' lists what anyone can make as an amateur.");
                else if (r < 0) { int top = Math.max(10, c.professionLvl) / 10 + 2; say("You need at least 50 herbs of one kind, no higher than Lv." + top + " (your profession level " + c.professionLvl + " lets you work herbs up to Lv." + top + "; forage first)." + (c.materialBag.stream().anyMatch(h -> h.itemType == Item.ItemType.Herb && h.lvl > top) ? " The finer herbs you carry sell well, but they are beyond your craft yet." : "")); }
                return r > 0;
            }
            case "cook": return handle(c, "make food");
            case "brew": return handle(c, "make wine");
            case "eat": case "drink": {
                Item.ItemType ty = cmd.equals("eat") ? Item.ItemType.Food : Item.ItemType.Wine;
                Item it = null;
                if (t.length > 1) { int n = num(t[1], -1); if (n >= 1 && n <= Life.goods(c).size()) it = Life.goods(c).get(n - 1); }
                else it = Life.firstOf(c, ty);
                if (it == null || it.itemType != ty) { say("You have no " + (ty == Item.ItemType.Food ? "food" : "wine") + " to " + cmd + ". See 'goods'."); return false; }
                return Life.use(c, it);
            }
            case "poison": {
                Item vial = Life.firstOf(c, Item.ItemType.Poison);
                if (vial == null) { say("You carry no poison ('make poison')."); return false; }
                Character p = pick(c, t, 1, "poison");
                if (p == null) return false;
                if (!confirm(c, confirmed, "Slip poison to " + p.name + " " + p.lastName + "? If it is noticed they will not forgive you.", line)) return false;
                vial.quantity--; if (vial.quantity <= 0) Life.goods(c).remove(vial);
                boolean took = Life.poison(c, p, vial.lvl);
                if (!took) say("The poison had no effect on " + p.name + "."); // (Life.poison tells a poisoning itself)
                if (took && GameDriver.rand.nextInt(3) == 0) { p.changeRelationship(c, -3); say("They suspect you."); }
                return true;
            }
            case "coat": {
                Item vial = Life.firstOf(c, Item.ItemType.Poison);
                if (vial == null) { say("You carry no poison ('make poison')."); return false; }
                if (c.equippedWeapon == null) { say("You wield no weapon to coat (buy or make one and 'equip' it)."); return false; }
                vial.quantity--; if (vial.quantity <= 0) Life.goods(c).remove(vial);
                Life.Fx f = Life.fx(c); f.coat = 3; f.coatLvl = vial.lvl;
                say("Your weapon is coated (level " + vial.lvl + "): the next 3 fights start with a dose.");
                return true;
            }
            case "perform": {
                if (c.trait != Character.Traits.Musician) { say("Only a Musician gives concerts (your trait is " + c.getTrait() + ")."); return false; }
                if (!c.location.city) { say("A concert needs a city to pay for it ('goto' one, or use the Perform quest)."); return false; }
                return Life.concert(c) > 0;
            }
            case "play": {
                int r = Life.play(c);
                if (r == -3) say("Only a Musician can play (your trait is " + c.getTrait() + ").");
                return r > 0;
            }
            case "reincarnate": {
                String why = Reincarnation.perform(c);
                if (why != null) { say(why); return false; }
                return true;
            }
            case "runstats": case "run":
                GameDriver.showGuiInfo("Run statistics", RunStats.text());
                return false;
            case "halloffame": case "fame":
                GameDriver.showGuiInfo("Hall of fame", RunStats.hallOfFame());
                return false;
            case "retire": {
                if (!confirm(c, confirmed, "Retire this run? It is written to the hall of fame (exports/hall_of_fame.txt) and the game goes on.", line)) return false;
                RunStats.finish(c, "retired at Lv." + c.lvl + " (Major " + c.majorLvl + ")");
                say("Your run is recorded in the hall of fame. The world goes on; you can keep playing.");
                return false;
            }
            case "lineage":
                GameDriver.showGuiInfo("Lineage", Reincarnation.lineage(c));
                return false;
            case "unequip": {
                String w = t.length > 1 ? t[1].toLowerCase() : "";
                if (w.startsWith("w") && c.equippedWeapon != null) { c.backpack.add(c.equippedWeapon); say("You put away " + c.equippedWeapon + "."); c.equippedWeapon = null; return true; }
                if ((w.startsWith("a") || w.startsWith("c")) && c.equippedClothing != null) { c.backpack.add(c.equippedClothing); say("You take off " + c.equippedClothing + "."); c.equippedClothing = null; return true; }
                say("Usage: unequip weapon|armor   (something has to be worn; with 'settings autoequip off' nothing is swapped behind your back)");
                return false;
            }
            case "compare":
                TextTools.compare(c, t);
                return false;
            case "techniques": case "tech":
                TextTools.techniques(c);
                return false;
            case "journal":
                TextTools.journal(c, t);
                return false;
            case "history": case "chronicle":
                TextTools.history(c, t);
                return false;
            case "fights": case "fightlog": { // the reports of your last fights (the same text that appeared when they happened)
                int n = t.length > 1 ? TermFormat.parseInt(t[1], 3) : 3;
                say(FightReport.lastReports(Math.max(1, Math.min(FightReport.MAX_HISTORY, n))).trim());
                return false;
            }
            case "route":
                TextTools.route(c, t);
                return false;
            case "digest":
                TextTools.show(TextTools.digest(t.length > 1 ? Math.max(1, num(t[1], 52)) : 52));
                return false;
            case "time": case "date":
                TextTools.time(c);
                return false;
            case "watch":
                return TextTools.watch(c, t);
            case "note":
                TextTools.note(c, t);
                return false;
            case "notes":
                TextTools.notes();
                return false;
            case "unnote":
                TextTools.unnote(t);
                return false;
            case "dash": case "dashboard":
                TextTools.show(TextTools.dashText(TextTools.dash()));
                return false;

            // ------------------------------------------------ social
            case "mission": case "missions": return missionCommand(c, t);
            case "avoid": case "unavoid": {
                boolean on = cmd.equals("avoid") && !(t.length > 2 && t[2].equalsIgnoreCase("off"));
                Character p = pick(c, t, 1, "talk");
                if (p == null) return false;
                c.avoid(p, on);
                say(on ? p.name + " " + p.lastName + " will not become your friend (nothing is lost by this). 'unavoid <n>' lifts it." : "You are open to " + p.name + " " + p.lastName + " again.");
                return false;
            }
            case "talk": {
                Character p = pick(c, t, 1, "talk");
                if (p == null) return false;
                if (p.age >= 8 && p.trait != Character.Traits.Beast && p.trait != Character.Traits.Monster && Dialogue.converse(c, p)) { c.changeRelationship(p, 0); return true; } // a conversation (or their words in the log)
                c.interact(p);
                return true;
            }
            case "spar": {
                Character p = pick(c, t, 1, "spar");
                if (p == null) return false;
                if (p.age < 8) { say("They are a child."); return false; }
                c.sparring = true; p.sparring = true;
                c.battleCheck(p);
                c.sparring = false; p.sparring = false;
                return true;
            }
            case "attack": {
                Character p = pick(c, t, 1, "attack");
                if (p == null) return false;
                if (p == c) return false;
                Character.FightPreview fp = c.previewFight(p, false);
                if ((fp.winChance < 35 || fp.deathChance >= 50) && !confirm(c, confirmed, "Attack " + p.name + "? " + fp.oneLine(), line)) return false;
                c.battleCheck(p);
                if (!c.dead && !p.dead) say("The fight ended without a kill.");
                return true;
            }
            case "invite": {
                Character p = pick(c, t, 1, "invite");
                if (p == null) return false;
                if (p.player >= 1) { say("Another player character can't be recruited."); return false; }
                if (p.partyLeaderId >= 0) { say(p + " is already in a party."); return false; }
                Relationship r = c.relationshipList.contains(p.id);
                boolean willing = (r != null && r.value >= 1) || ((r == null || r.value >= 0) && Math.abs(p.majorLvl - c.majorLvl) <= 1 && GameDriver.rand.nextInt(2) == 0);
                if (willing) { p.joinParty(c); say(p + " joins your party."); }
                else say(p + " declines.");
                return true;
            }
            case "leave":
                if (c.partyLeaderId < 0) { say("You are not following anyone's party. ('party' shows yours; 'disband' dissolves the one you lead.)"); return false; }
                c.leaveParty();
                say("You left your party.");
                return false;
            case "disband": {
                boolean leads = false;
                for (Character m : new ArrayList<>(c.location.characters)) if (m != null && !m.dead && m.partyLeaderId == c.id) leads = true;
                if (!leads) { say("You lead no party here. ('invite <n>' asks someone to join you; 'party' shows who follows you.)"); return false; }
                if (!confirm(c, confirmed, "Disband your party?", line)) return false;
                c.dissolveParty();
                say("Your party was disbanded.");
                return false;
            }

            // ------------------------------------------------ work
            case "forage":
                if (c.location.herbMaterials <= 0) { say("There are no herbs here."); return false; }
                c.forage();
                return true;
            case "mine":
                if (c.location.metalMaterials <= 0) { say("There is no metal here."); return false; }
                c.mine();
                return true;
            case "craft": case "profession": case "prof": {
                if (c.trait == Character.Traits.Ghost || c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) { say("Your nature has no craft."); return false; }
                String kind = t.length > 1 ? t[1].toLowerCase() : "";
                if (kind.isEmpty()) {
                    if (c.hasTrade()) { // your own trade
                        if (c.trait == Character.Traits.Leader) { String[] plan = Profession.leaderPlan(c); if (plan[0] != null && !confirm(c, confirmed, plan[1], line)) return false; }
                        return Profession.run(c);
                    }
                    say(AmateurCraft.menu(c)); // no trade: the amateur menu (no turn spent)
                    return false;
                }
                if (kind.equals("menu") || kind.equals("?")) { say(AmateurCraft.menu(c)); return false; }
                return AmateurCraft.craftCommand(c, kind);
            }
            case "meditate":
                c.meditate();
                return true;
            case "explore": { // the ruins, lair or trial of this tile (the same as 'do Explore Dungeon')
                if (c.location.dungeonType == null) { say("There is nothing to explore here: no ruins, lair or trial in this tile."); return false; }
                return doCommand(c, new String[]{"do", "Explore", "Dungeon"});
            }
            case "hunt": {
                if (c.location.monsters <= 0) { say("No beasts are here."); return false; }
                if (!c.partyBeastHunt()) say("The beasts here are too strong (or too weak to bother with) for you right now.");
                return true;
            }
            case "sell":
                if (!c.location.city) { say("You need to be in a city to sell."); return false; }
                if (t.length > 1 && (t[1].equalsIgnoreCase("goods") || t[1].equalsIgnoreCase("food") || t[1].equalsIgnoreCase("wine") || t[1].equalsIgnoreCase("poison"))) {
                    Item.ItemType only = t[1].equalsIgnoreCase("food") ? Item.ItemType.Food : t[1].equalsIgnoreCase("wine") ? Item.ItemType.Wine : t[1].equalsIgnoreCase("poison") ? Item.ItemType.Poison : null;
                    long got = Life.sellGoods(c, false, only, t.length > 2 ? Math.max(1, num(t[2], 1)) : -1);
                    if (got <= 0) say("You have nothing of that kind to sell here."); // (a sale prints its own line)
                    return got > 0;
                }
                if (t.length > 1) return sellItemCommand(c, t);
                { int had = c.backpack.size() + c.pillBag.size(); long wealthBefore = PlayerLists.wealth(c);
                  c.sell(c.location);
                  if (c.backpack.size() + c.pillBag.size() == had && PlayerLists.wealth(c) == wealthBefore) { say((c.materialBag.isEmpty() ? "" : "Herbs and metals are not sold raw: they are what you craft from ('craft', 'make poison', 'make food'...); what you make sells. ") + "Nothing to sell: your equipped gear and one spare of each kind are kept. 'bag' shows your backpack, 'sell <#>' sells one item."); return false; } // no sale = no turn used
                  return true; }
            case "set":
                if (t.length > 2 && t[1].equalsIgnoreCase("limit")) return handle(c, "limit " + t[2]);
                say("Usage: set limit <n>");
                return false;
            case "buygear": {
                if (!c.canShopHere()) { say("No shops here."); return false; }
                java.util.IdentityHashMap<Item, Boolean> had = new java.util.IdentityHashMap<>();
                for (Item i : c.backpack) had.put(i, true);
                if (c.equippedWeapon != null) had.put(c.equippedWeapon, true);
                if (c.equippedClothing != null) had.put(c.equippedClothing, true);
                long wealth0 = PlayerLists.wealth(c);
                c.buyGear();
                StringBuilder got = new StringBuilder();
                java.util.ArrayList<Item> now = new java.util.ArrayList<>(c.backpack);
                if (c.equippedWeapon != null) now.add(c.equippedWeapon);
                if (c.equippedClothing != null) now.add(c.equippedClothing);
                for (Item i : now) if (!had.containsKey(i)) got.append(got.length() > 0 ? " and " : "").append(i.name).append(" (Lv.").append(i.lvl).append(")");
                if (got.length() == 0) {
                    say("Nothing bought: no weapon or armour here is better than yours, within " + 9 + " levels of yours and within your means. ('shops' lists what the shops hold, 'price' sets what you will pay.)");
                    return false;
                }
                say("You bought " + got + " for " + Math.max(0, wealth0 - PlayerLists.wealth(c)) + " ss." + (c.equippedWeapon == null || c.equippedClothing == null ? "" : " ('equip' wears the best of your gear.)"));
                return true;
            }
            case "consume": {
                int pillsBefore = 0, potBefore = c.potential;
                for (Item pill : c.pillBag) pillsBefore += Math.max(0, pill.quantity);
                c.consume();
                int pillsAfter = 0;
                for (Item pill : c.pillBag) pillsAfter += Math.max(0, pill.quantity);
                if (pillsAfter == pillsBefore) { say("No pill to take: Potential pills work while your potential is under 100, a cultivation pill must be for your next realm (Lv." + (c.majorLvl + 1) + "). 'bag' shows the pills you carry."); return false; } // nothing eaten = no turn used
                if (c.potential > potBefore) say("You took " + (c.potential - potBefore) + " Potential pill(s): potential " + potBefore + " -> " + c.potential + ".");
                return true;
            }
            case "equip":
                if (t.length > 1) return TextTools.equipItem(c, t);
                c.equipNow = true;
                try { c.equip(); } finally { c.equipNow = false; }
                say("Weapon: " + (c.equippedWeapon != null ? c.equippedWeapon : "none") + "   Armor: " + (c.equippedClothing != null ? c.equippedClothing : "none"));
                return true;
            case "gift": {
                Character p = t.length > 2 ? person(c, t[1]) : null;
                if (p == null) { if (t.length < 3) say("Usage: gift <person#> <item#>   (item numbers from 'bag')"); return false; }
                int n = num(t[2], -1);
                if (n < 1 || n > c.backpack.size()) { say("No backpack item #" + n + ". See 'bag'."); return false; }
                Item it = c.backpack.get(n - 1);
                if (it == c.equippedWeapon || it == c.equippedClothing) { say("Unequip-swap first: that item is in use."); return false; }
                c.backpack.remove(it);
                Character.addToBag(p.backpack, it);
                c.changeRelationship(p, 1);
                say("You gave " + it + " to " + p + ".");
                return true;
            }
            case "install": {
                if (c.arrayBag.isEmpty()) { say("You carry no arrays."); return false; }
                c.installArray(c.location);
                say("Installed an array at " + c.location.name + ".");
                return true;
            }
            case "learn": {
                if (!c.location.city || c.location.libraries.isEmpty()) { say("No library here."); return false; }
                Library lib = c.location.libraries.get(0);
                int n = t.length > 1 ? num(t[1], -1) : -1;
                if (lib.techniques.isEmpty()) { say("The library has nothing for sale right now."); return false; }
                if (n < 1 || n > lib.techniques.size()) { say("Usage: learn <n>   (numbers from 'library', 1-" + lib.techniques.size() + ")"); return false; }
                Technique tq = lib.techniques.get(n - 1);
                say(lib.learnTechnique(c, tq) ? "You learned " + tq + "." : "You can't afford " + tq + " (" + lib.buyPrice(tq) + " ss).");
                return true;
            }

            // ------------------------------------------------ city / sect
            case "found": {
                String kind = t.length > 1 ? t[1].toLowerCase() : "";
                String why = kind.equals("city") ? c.foundCityNow() : kind.equals("sect") ? c.foundSectNow() : "Usage: found city|sect";
                if (why != null) { say(why); return false; }
                return true;
            }
            case "join": {
                if (c.sect != null) { say("You already belong to " + c.sect.name + "."); return false; }
                if (c.location.sect == null) { say("There is no sect here. ('l' lists the places you know; sects show as [Sect: ...])"); return false; }
                if (!c.location.sect.canJoin(c)) { say(c.location.sect.name + " won't take you: " + c.location.sect.whyNot(c) + "."); return false; }
                c.location.sect.join(c);
                say("You joined " + c.sect.name + ".");
                return true;
            }
            case "resign": {
                if (c.sect == null) { say("You have no sect."); return false; }
                String old = c.sect.name;
                if (!confirm(c, confirmed, "Really leave " + old + "? You lose your rank and " + c.sectPoints + " sect points.", line)) return false;
                c.sect.resign(c);
                c.sectPoints = 0;
                say("You left " + old + ".");
                return true;
            }
            case "recruit": {
                Character p = pick(c, t, 1, "recruit");
                if (p == null) return false;
                if (c.sect == null || !c.sect.isMaster(c)) { say("Only a sect master can recruit."); return false; }
                if (!c.sect.canJoin(p)) { say(p + " can't join: " + c.sect.whyNot(p) + "."); return false; }
                c.interact(p);
                c.sect.join(p);
                say(p + " joined " + c.sect.name + ".");
                return true;
            }
            case "promote": {
                Character p = t.length > 1 ? person(c, t[1]) : null;
                if (p == null) { if (t.length < 2) say("Usage: promote <n>"); return false; }
                if (c.sect == null || !c.sect.isMaster(c) || p.sect != c.sect) { say("You can only promote members of the sect you lead."); return false; }
                c.sect.promoteRank(p);
                say(p + " is now " + c.sect.getRank(p) + ".");
                return false;
            }
            case "war": {
                Organization there = c.location.sect;
                if (c.sect == null || !c.sect.isMaster(c)) { say("Only a sect master can declare war."); return false; }
                if (there == null || there == c.sect) { say("Stand in the enemy sect's home tile to declare war on it."); return false; }
                if (c.sect.isAtWar()) { say("Your sect is already at war."); return false; }
                if (!confirm(c, confirmed, "Declare war on " + there.name + "? Wars are hard to end and your sect will be raided.", line)) return false;
                c.sect.declareWar(there);
                say(c.sect.name + " has declared war on " + there.name + "!");
                return true;
            }
            case "peace": {
                if (c.sect == null || !c.sect.isMaster(c) || !c.sect.isAtWar()) { say("Your sect is not at war (or you are not its master)."); return false; }
                Organization foe = c.sect.warTarget;
                c.sect.endWar(foe);
                say("Peace with " + foe.name + ".");
                return true;
            }
            case "alliance": {
                Organization there = c.location.sect;
                if (c.sect == null || !c.sect.isMaster(c)) { say("Only a sect master can propose an alliance."); return false; }
                if (there == null || there == c.sect || c.sect.isAlly(there) || c.sect.isAtWar()) { say("Stand in another (non-allied) sect's home tile, and not at war, to propose an alliance."); return false; }
                String no = there.allianceRefusal(c.sect);
                if (no == null && there.master != null && !there.master.dead && there.master.player > 0 && there.master != c
                        && !DecisionBroker.askYesNo(there.master, c + " of " + c.sect + " proposes an alliance with " + there + ". Accept?", true, c, "alliance")) no = there.master.name + " declined.";
                if (no != null) { say(there.name + " refuses the alliance: " + no); return true; }
                c.sect.proposeAlliance(there);
                say(c.sect.name + " and " + there.name + " are now allied.");
                return true;
            }
            case "price":
                return priceCommand(c, t);
            case "do":
                return doCommand(c, t);

            // ------------------------------------------------ saves, search, control, preview, export, settings
            case "save": {
                String name = t.length > 1 ? SaveManager.sanitize(t[1]) : SaveManager.QUICK;
                if (name.isEmpty()) { say("Give the save a name using letters, digits, - or _."); return false; }
                if (t.length > 1 && SaveManager.find(name) != null && !confirm(c, confirmed, "Overwrite the save '" + name + "'?", line)) return false;
                if (name.equals(SaveManager.QUICK)) GameDriver.saveRequested = true; else GameDriver.saveRequestName = name;
                say("Saving as '" + name + "' at the start of the next turn.");
                return false;
            }
            case "saves": {
                StringBuilder sb = new StringBuilder("=== Saved games (" + SaveManager.dir.getPath() + "/) ===\n");
                for (SaveManager.Slot s : SaveManager.list()) sb.append("  ").append(SaveManager.describe(s)).append("\n");
                if (sb.indexOf("\n") == sb.length() - 1) sb.append("  (none yet)\n");
                sb.append("save [name] | load <name> | delsave <name>\n");
                GameDriver.showGuiInfo("Saves", sb.toString());
                return false;
            }
            case "load": {
                if (t.length < 2) { say("Usage: load <name>  (see 'saves')"); return false; }
                SaveManager.Slot s = SaveManager.find(t[1]);
                if (s == null) { say("No save called '" + t[1] + "'. See 'saves'."); return false; }
                if (!confirm(c, confirmed, "Load '" + s.name + "' (year " + s.year + ")? Anything since your last save is lost.", line)) return false;
                GameDriver.loadRequest = s.name;
                say("Loading '" + s.name + "' at the start of the next turn...");
                return true;
            }
            case "delsave": {
                if (t.length < 2) { say("Usage: delsave <name>"); return false; }
                if (!confirm(c, confirmed, "Delete the save '" + t[1] + "' for good?", line)) return false;
                say(SaveManager.delete(t[1]) ? "Deleted '" + t[1] + "'." : "No such save (the legacy save.dat can't be deleted here).");
                return false;
            }
            case "find": {
                if (t.length < 2) { say("Usage: find <name|#id> [limit] [filters] [sort=key desc]   (keys: " + String.join(", ", PlayerLists.WORLD_KEYS) + ")"); return false; }
                String[] toks = t.clone();
                toks[1] = toks[1].matches("#?\\d+") ? "id=" + toks[1].replace("#", "") : toks[1];
                PlayerLists.ListView v = PlayerLists.world(c, ListQuery.parse(Arrays.copyOfRange(toks, 1, toks.length), 0), pc.listLimit);
                if (!GuiCards.listShown(c, "world", v, false)) GameDriver.showGuiInfo(v.title, v.toText());
                return false;
            }
            case "control": {
                Character n = anyone(t.length > 1 ? t[1] : null);
                if (n == null) { say("Usage: control <id|#id>  (find ids with 'find <name>')"); return false; }
                if (n == c) { say("That's you."); return false; }
                if (n.player == 1) { say("Another player already controls " + n.name + "."); return false; }
                if (n.trait == Character.Traits.Beast || n.trait == Character.Traits.Monster) { say("You can't control a beast."); return false; }
                if (!confirm(c, confirmed, "Take control of " + n.name + " " + n.lastName + " (Lv." + n.lvl + ")? " + c.name + " carries on as an NPC.", line)) return false;
                n.player = 1;
                PlayerControl np = n.pc();
                if (pc != null) np.inheritFrom(pc);
                c.player = 0;
                if (!Game.Map.trackedCharacters.contains(c.id)) Game.Map.trackedCharacters.add(c.id); // still in the tracker; 'follow'/'unfollow' decide
                say("You are now " + n.name + " " + n.lastName + " (was " + c.name + ").");
                return true;
            }
            case "follow": case "track": {
                Character n = anyone(t.length > 1 ? t[1] : null);
                if (n == null) { say("Usage: follow <id|#id>  (shows them in the GUI tracker)"); return false; }
                if (!Game.Map.trackedCharacters.contains(n.id)) Game.Map.trackedCharacters.add(n.id);
                say("Following " + n.name + " " + n.lastName + " (#" + n.id + ").");
                return false;
            }
            case "unfollow": case "untrack": {
                Character n = anyone(t.length > 1 ? t[1] : null);
                if (n == null) { say("Usage: unfollow <id|#id>"); return false; }
                Game.Map.trackedCharacters.remove(Integer.valueOf(n.id));
                say("No longer following " + n.name + ".");
                return false;
            }
            case "preview": {
                Character p;
                if (t.length > 1 && t[1].startsWith("#")) p = anyone(t[1]); else p = pick(c, t, 1, "preview");
                if (p == null) { if (t.length > 1) say("Nobody matches " + t[1] + "."); return false; }
                Character.FightPreview fp = c.previewFight(p, false);
                if (GameDriver.guiEnabled && GameDriver.mapViewer != null) GuiCards.previewWindow(fp); else GameDriver.showGuiInfo("Fight preview", fp.detail());
                return false;
            }
            case "export":
                return exportCommand(c, t);
            case "story":
                if (t.length > 1 && t[1].equalsIgnoreCase("replay") && c.story != null && c.story.v2) { say(Chapters.replay(c, c.story, t.length > 2 ? t[2] : "")); return false; }
                say(Starts.describe(c));
                return false;
            case "tutorial":
                if (GameDriver.guiEnabled && GameDriver.mapViewer != null) javax.swing.SwingUtilities.invokeLater(() -> GuiWindows.tutorialWindow(GameDriver.mapViewer));
                else say(Hints.tutorialText());
                return false;
            case "lore": return Lore.command(c, t); // the Palace of the Oldest Order: journal, clues, places, replay, register, join, cross
            case "guide": { // the in-game Help window (`guide combat` opens a section); without the window, point at the terminal help
                final String sec = t.length > 1 ? t[1].toLowerCase() : "start";
                if (GameDriver.guiEnabled && GameDriver.mapViewer != null) javax.swing.SwingUtilities.invokeLater(() -> GuiHelp.open(GameDriver.mapViewer, sec));
                else say("The illustrated Help is part of the windowed game (start it with the map viewer: answer y at the first question). Here: 'help' lists every command, 'help <topic>' explains one, 'tutorial' prints a walkthrough.");
                return false;
            }
            case "settings":
                return settingsCommand(t);
            case "notices": {
                int n = t.length > 1 ? Math.max(1, num(t[1], 15)) : 15;
                StringBuilder sb = new StringBuilder("=== Notifications (latest " + n + ") ===\n");
                synchronized (Notifications.history) {
                    for (int i = Math.max(0, Notifications.history.size() - n); i < Notifications.history.size(); i++) {
                        Notifications.Note x = Notifications.history.get(i);
                        sb.append(String.format("  wk %-6d [%s] %s%n", x.week, x.cat, x.text)); x.read = true;
                    }
                }
                GameDriver.showGuiInfo("Notifications", sb.toString());
                return false;
            }
            case "recap": {
                String[] r = Recap.preview(c);
                GameDriver.showGuiInfo(r[0], r[1]);
                return false;
            }
            case "family":
                GameDriver.showGuiInfo("Family", Family.text(c));
                return false;
            case "relations": {
                GameDriver.showGuiInfo("Relationships", Family.relationsText(c, t.length > 1 ? Math.max(1, num(t[1], 25)) : 25));
                return false;
            }

            // ------------------------------------------------ system
            case "e":
                say("=== Character Editor ===");
                GameDriver.editCharacterInteractive(c);
                return false;
            case "z":
                if (c.trait != Character.Traits.Cheater) { say("Only a Cheater can use cheats. ('e' / Edit Self can change your trait.)"); return false; }
                if (GameDriver.guiEnabled && GameDriver.mapViewer != null) javax.swing.SwingUtilities.invokeLater(() -> GuiSheets.cheatWindow(GameDriver.mapViewer)); else GameDriver.handleCheatMenu(c);
                return false;
            case "f":
                GameDriver.saveRequested = true; // saved at the start of the next tick, when the world state is consistent
                say("Game will be saved at the start of the next turn.");
                return false;
            case "p": case "pause":
                GameDriver.paused = !GameDriver.paused;
                say(GameDriver.paused ? "--- PAUSED --- (press 'p' again to resume)" : "--- RESUMED ---");
                return true;
            default:
                return TermInput.unknown(c, line, cmd, confirmed); // aliases, prefixes, 'did you mean', hud, cmds
        }
    }

    private static String moodNames(Score sc) {
        String[] names = {"the open country", "cities", "danger", "night and winter", "war", "sects", "the capital"};
        StringBuilder sb = new StringBuilder();
        for (int m = 0; m < names.length; m++) if (sc.moodWeight(m) > 0) { if (sb.length() > 0) sb.append(", "); sb.append(names[m]); }
        return sb.toString();
    }

    /** `music [on|off|next|list|play <song>|fonts|presets|engine builtin|soundfont|font <file>|mix <instrument> <0-150>|volume <n>]` */
    private static boolean musicCommand(String[] t) {
        String sub = t.length > 1 ? t[1].toLowerCase() : "";
        switch (sub) {
            case "on": case "off": Settings.music = sub.equals("on"); Settings.save(); Ambient.apply(); say("Music " + sub + "."); break;
            case "new": case "next": case "skip": Ambient.newPiece(); say("The next song begins."); break;
            case "list": {
                StringBuilder sb = new StringBuilder("The songs (music play <number or part of the name>, music play auto to let the places choose):");
                int n = 1;
                for (Score sc : SongBook.all()) { sb.append("\n  ").append(n++).append(". \"").append(sc.title).append("\"  ").append(String.format("%d:%02d", (int) sc.seconds() / 60, (int) sc.seconds() % 60)).append("  for ").append(moodNames(sc)).append(sc.kept ? "  [kept]" : ""); }
                say(sb.toString() + (Ambient.pinned() == null ? "" : "\nPlaying only: \"" + Ambient.pinned().title + "\""));
                break;
            }
            case "gen": case "compose": {
                int mood = -1; String tplKey = null; long seed = -1;
                String[] moodWords = {"open", "city", "danger", "night", "war", "sect", "capital"};
                for (int i = 2; i < t.length; i++) {
                    String w = t[i].toLowerCase();
                    if (w.matches("[0-9]+")) { seed = Long.parseLong(w); continue; }
                    for (int m = 0; m < moodWords.length; m++) if (moodWords[m].startsWith(w)) mood = m;
                    if (SongGen.template(w) != null) tplKey = w;
                }
                java.util.Random rr = new java.util.Random();
                SongGen.Tpl tpl = tplKey != null ? SongGen.template(tplKey) : SongGen.templateFor(mood >= 0 ? mood : Math.max(0, Math.min(6, Ambient.currentMood())), rr);
                if (seed < 0) seed = rr.nextInt(1000000);
                SongGen.Made made = SongGen.generate(seed, tpl);
                if (made.score == null) { say("No song came out of that seed (" + made.attempts + " attempts). Try another number."); break; }
                if (!Settings.music) { Settings.music = true; Settings.save(); Ambient.apply(); }
                Ambient.pin(made.score);
                say("[experimental] Composed \"" + made.score.title + "\" (" + tpl.key + " arrangement, seed " + seed + ", " + String.format("%d:%02d", (int) made.score.seconds() / 60, (int) made.score.seconds() % 60) + "). It plays now. 'music keep' keeps it, 'music gen' composes another, 'music play auto' goes back to the places' songs.");
                break;
            }
            case "keep": {
                Score now = Ambient.nowScore != null ? Ambient.nowScore : Ambient.pinned();
                if (now == null) { say("Nothing is playing. 'music gen' composes a song, then 'music keep' keeps it."); break; }
                if (now.kept) { say("\"" + now.title + "\" is already kept (songs/" + now.file + ")."); break; }
                if (now.source.isEmpty()) { say("\"" + now.title + "\" is one of the written songs: nothing to keep."); break; }
                Score k = SongBook.keep(now);
                say(k == null ? "Could not keep it: " + SongBook.lastError : "Kept \"" + k.title + "\" in songs/" + k.file + ". It joins the songs the places choose from.");
                if (k != null && Ambient.pinned() == now) Ambient.pin(k);
                break;
            }
            case "drop": {
                if (t.length < 3) { say("Usage: music drop <number or part of the name>   (only kept songs can be dropped; music list shows them)"); break; }
                Score sc = SongBook.find(String.join(" ", java.util.Arrays.copyOfRange(t, 2, t.length)));
                if (sc == null) { say("No such song. Try: music list"); break; }
                if (!sc.kept) { say("\"" + sc.title + "\" is a written song of the game; only kept songs can be dropped."); break; }
                if (Ambient.pinned() == sc) Ambient.pin(null);
                say(SongBook.drop(sc) ? "Dropped \"" + sc.title + "\" (its file is deleted)." : "Could not delete the file.");
                break;
            }
            case "share": {
                if (t.length > 2) { Settings.musicGenerated = Math.max(0, Math.min(100, num(t[2], Settings.musicGenerated))); Settings.save(); }
                say("[experimental] Newly composed songs take " + Settings.musicGenerated + "% of the song slots (0: only the written and kept songs). 'music share <0-100>' changes it.");
                break;
            }
            case "play": {
                if (t.length < 3) { say("Usage: music play <number or part of the name>   (music list shows them; music play auto: the places choose)"); break; }
                String key = String.join(" ", java.util.Arrays.copyOfRange(t, 2, t.length));
                if (key.equalsIgnoreCase("auto")) { Ambient.pin(null); say("The places choose the songs again."); break; }
                Score sc = SongBook.find(key);
                if (sc == null) { say("No such song. Try: music list"); break; }
                if (!Settings.music) { Settings.music = true; Settings.save(); Ambient.apply(); }
                Ambient.pin(sc);
                say("Playing \"" + sc.title + "\" (music play auto to go back).");
                break;
            }
            case "volume": if (t.length > 2) { Settings.musicVolume = Math.max(0, Math.min(100, num(t[2], Settings.musicVolume))); Settings.save(); Ambient.apply(); } say("Volume " + Settings.musicVolume + "."); break;
            case "fonts": {
                java.util.List<String> fs = Ambient.fonts();
                say(fs.isEmpty() ? "No SoundFonts yet. Put .sf2 files in the soundfonts/ folder (next to src/), then: music engine soundfont" : "SoundFonts: " + String.join(", ", fs) + "   (music font <name> to choose)");
                break;
            }
            case "font": {
                if (t.length < 3) { say("Usage: music font <file name in soundfonts/>"); break; }
                String want = t[2];
                String hit = null;
                for (String f : Ambient.fonts()) if (f.equalsIgnoreCase(want) || f.toLowerCase().startsWith(want.toLowerCase())) { hit = f; break; }
                if (hit == null) { say("No such file in soundfonts/. Try: music fonts"); break; }
                Settings.soundFont = hit; Settings.musicEngine = "soundfont"; Settings.save(); Ambient.apply(); say("Using " + hit + " (restarting the music).");
                break;
            }
            case "engine": {
                if (t.length < 3) { say("Usage: music engine auto|builtin|soundfont"); break; }
                Settings.musicEngine = t[2].toLowerCase().startsWith("s") ? "soundfont" : t[2].toLowerCase().startsWith("a") ? "auto" : "builtin"; Settings.save(); Ambient.apply();
                say("Engine: " + Settings.musicEngine + (Settings.musicEngine.equals("soundfont") && Ambient.fonts().isEmpty() ? "   (no file in soundfonts/ yet, the built-in synth plays instead)" : ""));
                break;
            }
            case "presets": {
                java.util.List<String> fs = Ambient.fonts();
                if (fs.isEmpty()) { say("No SoundFonts in soundfonts/."); break; }
                String use = Settings.soundFont.isEmpty() || !fs.contains(Settings.soundFont) ? fs.get(0) : Settings.soundFont;
                java.util.List<String> ps = MidiOut.presets(new java.io.File("soundfonts", use));
                say(use + " has " + ps.size() + " presets (bank:program name). Map instruments in soundfonts/programs.txt, e.g. pluck=0:107\n" + String.join("\n", ps.subList(0, Math.min(ps.size(), 60))) + (ps.size() > 60 ? "\n... and " + (ps.size() - 60) + " more" : ""));
                break;
            }
            case "mix": {
                if (t.length < 4) { say("Usage: music mix <" + String.join("|", Ambient.INST) + "> <0-150>   (now: " + Ambient.mixString() + ")"); break; }
                int idx = java.util.Arrays.asList(Ambient.INST).indexOf(t[2].toLowerCase());
                if (idx < 0) { say("Instruments: " + String.join(" ", Ambient.INST)); break; }
                Ambient.layer[idx] = Math.max(0, Math.min(150, num(t[3], 100))) / 100.0;
                Settings.musicMix = Ambient.mixString(); Settings.save(); Ambient.apply();
                say("Mix: " + Settings.musicMix);
                break;
            }
            case "song": case "":
                say("Music is " + (Settings.music ? "on" : "off") + " (" + Ambient.status + ", engine " + (Ambient.engine) + ", volume " + Settings.musicVolume + ").\nNow playing: " + (Ambient.nowPlaying.isEmpty() ? "-" : "\"" + Ambient.nowPlaying + "\"") + "\nCommands: music on|off|next|list|play <song>|gen [open|city|danger|night|war|sect|capital] [seed]|keep|drop <song>|share <0-100>|volume <n>|fonts|font <file>|presets|engine builtin|soundfont|mix <instrument> <0-150>");
                break;
            default: say("Unknown music command. Type 'music' for the list.");
        }
        return false;
    }

    /** `cheat <self|#id> ss|level|potential|kills|weapon <n>` or `cheat <target> heal|reset|track` (the GUI cheat panel sends these). */
    private static boolean cheatCommand(Character c, String[] t) {
        if (c.trait != Character.Traits.Cheater) { say("Only a Cheater can use cheats. ('e' / Edit Self can change your trait.)"); return false; }
        if (t.length < 3) { say("Usage: cheat <self|#id> ss|level|potential|kills|weapon <n>   |   cheat <self|#id> heal|reset|track"); return false; }
        Character target = t[1].equalsIgnoreCase("self") ? c : anyone(t[1]);
        if (target == null || target.dead) { say("Target not found."); return false; }
        String what = t[2].toLowerCase();
        long v = t.length > 3 ? (long) num(t[3], 0) : 0;
        try {
            switch (what) {
                case "ss": target.ss += v; say(target.name + " now has " + target.ss + " ss."); break;
                case "level": { int ml = (int) Math.max(0, Math.min(10, v)); target.majorLvl = ml; target.minorLvl = 1; target.lvl = ml * 10 + 1; target.tempLvl = target.lvl;
                    target.cultivationBase = 0; target.cultivationBaseMax = Math.pow(target.lvl, 3) * (((70. + 1. / 3.) + ((11. / 6.) * target.lvl)) / 25.); // (the bar and the weekly gain follow the new level)
                    target.cultivation = (Math.pow(target.potential, 2) * Math.sqrt(target.lvl) * 10.) / (52. * Math.sqrt(Math.max(1, target.potential)) * Math.pow(2, target.inhibitor));
                    say(target.name + " is now major level " + ml + "."); break; }
                case "potential": target.potential = (int) Math.max(1, Math.min(100, v)); say(target.name + " now has potential " + target.potential + "."); break;
                case "kills": target.kills = (int) v; say(target.name + " has " + target.kills + " kills."); break;
                case "weapon": { int wl = (int) Math.max(1, Math.min(10, v)); Item wp = new Item(target, Item.WeaponType.values()[GameDriver.rand.nextInt(Item.WeaponType.values().length)], wl); target.backpack.add(wp); target.equip(); say("Gave " + wp + " to " + target.name + "."); break; }
                case "heal": target.cultivation = target.cultivationBaseMax; say(target.name + " cultivation restored to max."); break;
                case "reset": target.targetQuests.clear(); target.spotQuests.clear(); target.currentQuest = null; say(target.name + " quests cleared."); break;
                case "track": if (!Game.Map.trackedCharacters.contains(target.id)) Game.Map.trackedCharacters.add(target.id); say(target.name + " " + target.lastName + " is now tracked."); break;
                default: say("Unknown cheat '" + what + "'.");
            }
        } catch (RuntimeException e) { say("That cheat failed: " + e); }
        return false;
    }

    // ------------------------------------------------------------------ list / buy / sell one / seek
    /** `list <what> [limit] [filters] [sort=key [desc]]`; tokens from index `from` are the query. Stores the numbering for talk/buy/etc. */
    static void listCommand(Character c, String what, String[] t, int from) {
        PlayerControl pc = c.pc();
        ListQuery q = ListQuery.parse(t, from);
        PlayerLists.ListView v;
        switch (what) {
            case "monsters": v = PlayerLists.people(c, q, true, pc.listLimit); break;
            case "shop": v = PlayerLists.shopItems(c, q, pc.listLimit); break;
            case "bag": v = PlayerLists.bag(c, q, pc.listLimit); break;
            case "places": v = PlayerLists.places(c, q, pc.listLimit); break;
            default: v = PlayerLists.people(c, q, false, pc.listLimit); break;
        }
        if (v.error == null) {
            switch (what) {
                case "shop": {
                    ArrayList<PlayerLists.ShopItem> iv = new ArrayList<>();
                    for (Object o : v.refs) iv.add((PlayerLists.ShopItem) o);
                    pc.itemView = iv;
                    break;
                }
                case "people": case "monsters": {
                    ArrayList<Character> pv = new ArrayList<>();
                    for (Object o : v.refs) pv.add((Character) o);
                    pc.peopleView = pv;
                    break;
                }
                default: break;
            }
            String next = what.equals("people") ? "Try: talk <#> | spar <#> | preview <#> | attack <#> | invite <#>   (# = the first column; or use #<id>)\n"
                    : what.equals("monsters") ? "Try: hunt (fights the beasts of this tile) | preview <#> | attack <#>\n" : "";
            v.note = (v.note.isEmpty() ? "" : v.note + "\n") + next + "Keys: " + String.join(", ", v.sortKeys) + " (filter key=value, key>=n; sort=key desc)";
        }
        if (GuiCards.listShown(c, what, v, t.length <= from)) return; // GUI: a table with actions (a plain 'people' opens the live People window)
        GameDriver.showGuiInfo(v.title, v.toText());
    }

    /** `buy <#>` (row of the last `list shop`) or `buy <shop>.<item> [name]` (name is checked so a changed shelf isn't bought by mistake). */
    private static boolean buyCommand(Character c, String[] t) {
        if (t.length < 2) { say("Usage: buy <#> (from 'list shop')  or  buy <shop>.<item> <item name>  or  buy market <#> [qty] (food, wine, poison)"); return false; }
        if (!c.location.city) { say("You need to be in a city to buy ('l' lists the places you know, 'goto <x> <y>' walks there)."); return false; }
        if (t[1].equalsIgnoreCase("market")) {
            int n = t.length > 2 ? num(t[2], -1) : -1, qty = t.length > 3 ? Math.max(1, num(t[3], 1)) : 1;
            java.util.List<Life.Offer> m = Life.offers(c.location);
            if (n < 1 || n > m.size()) { say("No market item #" + n + ". See 'market'."); return false; }
            String why = Life.buy(c, m.get(n - 1), qty);
            if (why != null) say(why);
            return why == null;
        }
        Shop shop = null; Item item = null;
        if (t[1].contains(".")) {
            String[] p = t[1].split("\\.");
            int si = p.length == 2 ? num(p[0], -1) : -1, ii = p.length == 2 ? num(p[1], -1) : -1;
            if (si < 1 || si > c.location.shops.size()) { say("No shop " + t[1] + " here. See 'list shop'."); return false; }
            shop = c.location.shops.get(si - 1);
            if (ii < 1 || ii > shop.inventory.size()) { say("That shelf changed. See 'list shop' again."); return false; }
            item = shop.inventory.get(ii - 1);
            if (t.length > 2) {
                String want = String.join(" ", Arrays.copyOfRange(t, 2, t.length));
                if (item.name == null || !item.name.equalsIgnoreCase(want)) { say("The shop's stock changed (slot " + t[1] + " now holds " + item.name + "). See 'list shop' again."); return false; }
            }
        } else {
            int n = num(t[1], -1);
            ArrayList<PlayerLists.ShopItem> iv = c.pc().itemView;
            if (iv == null || n < 1 || n > iv.size()) { say("No listed item #" + n + ". Use 'list shop' first."); return false; }
            shop = iv.get(n - 1).shop; item = iv.get(n - 1).item;
        }
        if (shop.location != c.location || !shop.inventory.contains(item)) { say("That item is no longer for sale here. See 'list shop' again."); return false; }
        if (shop.owner == c) { say("It's your own shop: manage its stock with 'do Restock Trip' and 'price'."); return false; }
        long price = shop.sellPrice(item);
        if (!c.canAfford(price)) { say(item.name + " costs " + price + " ss; you have " + PlayerLists.wealth(c) + " ss."); return false; }
        if (!shop.sellToBuyer(c, item)) { say("The sale failed."); return false; }
        c.equip();
        say("You bought " + item + " for " + price + " ss.");
        return true;
    }

    /** `sell <backpack #>`: sells one item to the best shop paying at least the city pool rate, else to the city pool at half price. */
    private static boolean sellItemCommand(Character c, String[] t) {
        if (!t[1].matches("#?\\d+")) { say("'sell' takes a backpack number ('bag'), or goods|food|wine|poison. Herbs and metal are not sold with 'sell': take them to a city with the Trade Run quest ('do Trade Run')."); return false; }
        int n = num(t[1].replace("#", ""), -1);
        if (n < 1 || n > c.backpack.size()) { say("No backpack item #" + n + ". See 'list bag'."); return false; }
        Item item = c.backpack.get(n - 1);
        if (item == c.equippedWeapon || item == c.equippedClothing) { say("That item is equipped."); return false; }
        ArrayList<Shop> shops = new ArrayList<>(c.location.shops);
        shops.sort((x, y) -> Double.compare(y.buyRate, x.buyRate));
        for (Shop s : shops) {
            if (s.owner == c || s.buyRate < 0.5) continue;
            long price = s.buyPrice(item);
            if (s.buyFromSeller(c, item)) { say("Sold " + item + " to " + (s.owner != null ? s.owner.name : "the shop") + " for " + price + " ss."); return true; }
        }
        long price = Character.itemPrice(item) / 2;
        c.backpack.remove(item);
        if (c.location.backpack != null) Character.addToBag(c.location.backpack, item);
        c.ss += price;
        c.convertCurrency();
        say("Sold " + item + " to the city market for " + price + " ss.");
        return true;
    }

    /** `seek <id> friend|fight|spar [priority]`: pins a quest to go to that cultivator (anywhere in the world) and befriend / fight / spar them. */
    private static boolean seekCommand(Character c, String[] t) {
        if (t.length < 3) { say("Usage: seek <id|#id> talk|court|friend|fight|spar [priority]   (ids appear in 'list people')"); return false; }
        int id = num(t[1].startsWith("#") ? t[1].substring(1) : t[1], -1);
        Character target = Character.charById(id);
        if (target == null || target.dead) { say("No living cultivator has id " + t[1] + "."); return false; }
        if (target == c) { say("That's you."); return false; }
        String kind = t[2].toLowerCase();
        String name = kind.startsWith("ta") ? "Talk To" : kind.startsWith("co") ? "Courtship" : kind.startsWith("fr") || kind.startsWith("be") ? "Befriend" : kind.startsWith("fi") || kind.startsWith("du") || kind.startsWith("ba") ? "Duel" : kind.startsWith("sp") ? "Spar" : null;
        if (name == null) { say("Choose talk, court, friend, fight or spar."); return false; }
        if (name.equals("Courtship") && (c.spouse != null || !QuestLife.suitor(c, target))) { say(target.name + " cannot be courted by you: " + (c.spouse != null ? "you are married" : "courtship needs an unmarried adult of the other gender, not kin") + "."); return false; }
        int pri = t.length > 3 ? Math.max(0, Math.min(1000, num(t[3], 60))) : 60;
        Quest q = new Quest(name, true, pri, 0, pri, target);
        q.userCreated = true;
        q.pin(pri);
        c.targetQuests.add(q);
        say("Quest added: " + c.pc().describe(c, q) + ". AUTO mode, scripts ('quest " + name + "') or walking there yourself, then 'qdo', will carry it out.");
        return false;
    }

    /** Asks before an irreversible action (unless Settings.confirmRisky is off or the command came with a leading '!'). Commands that arrive
     *  while the character is not being asked for input (a running script, AUTO) were written on purpose ahead of time, so they count as confirmed. */
    static boolean confirm(Character c, boolean confirmed, String prompt, String line) {
        if (confirmed || !Settings.confirmRisky || !DecisionBroker.promptable(c)) return true;
        if (DecisionBroker.askYesNo(c, prompt, false)) return true;
        say("Cancelled: " + (line == null ? "that" : line.trim()) + ". (Start the command with ! to skip the question.)");
        return false;
    }

    /** Any living cultivator by `#id` or plain id number (null if none). */
    static Character anyone(String arg) {
        if (arg == null || !arg.matches("#?\\d+")) return null;
        Character p = Character.charById(TermFormat.parseInt(arg.replace("#", ""), -1));
        return p == null || p.dead || !p.born ? null : p;
    }

    private static boolean exportCommand(Character c, String[] t) {
        String what = t.length > 1 ? t[1].toLowerCase() : "";
        try {
            java.io.File f;
            if (what.equals("run")) {
                java.io.File dir = new java.io.File(Settings.exportDir); dir.mkdirs();
                f = new java.io.File(dir, "run-" + System.currentTimeMillis() / 1000 + ".csv");
                java.nio.file.Files.write(f.toPath(), RunStats.csv().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
            else if (what.equals("history")) f = Exporter.history();
            else if (what.equals("rankings")) f = Exporter.rankings();
            else {
                String kind = PlayerLists.kind(what);
                if (kind == null) { say("Usage: export history | rankings | people | monsters | shop | bag | places | world  [filters]   (files go to " + Settings.exportDir + "/)"); return false; }
                ListQuery q = ListQuery.parse(t, 2);
                if (q.limit < 0) q.limit = ListQuery.MAX_LIMIT;
                PlayerLists.ListView v = PlayerLists.view(c, kind, q, ListQuery.MAX_LIMIT);
                if (v.error != null) { say(v.error); return false; }
                f = Exporter.view(kind, v);
            }
            say("Exported to " + f.getPath());
        } catch (Exception e) { say("Export failed: " + e.getMessage()); }
        return false;
    }

    private static boolean settingsCommand(String[] t) {
        if (t.length < 3) {
            say("=== Settings ===\n dialogue " + (Settings.dialogueFlavor ? "on" : "off") + " (what people say to you)   dialoguepopups " + (Settings.dialoguePopups ? "on" : "off") + " (GUI window with a portrait and replies)   dialogueauto " + (Settings.dialogueAuto ? "on" : "off") + " (talks for an AUTO character too)\n autosave " + Settings.autosaveYears + " (years, 0 = off)   autosavekeep " + Settings.autosaveKeep + "   confirm " + (Settings.confirmRisky ? "on" : "off")
                + "\n hud " + (Settings.hud ? "on" : "off") + " (terminal status block before each prompt)   hints " + (Settings.hints ? "on" : "off") + " (tips)   maturestart " + (Settings.matureStart ? "on" : "off") + "   challenges " + Settings.maxRandomChallenges + "   highcontrast " + (Settings.highContrast ? "on" : "off")
                + "\n autoequip " + (Settings.autoEquipManual ? "on" : "off") + " (swap gear for you in manual mode)   music " + (Settings.music ? "on" : "off") + "   volume " + Settings.musicVolume + "   tooltips " + (Settings.tooltips ? "on" : "off") + "   toasts " + (Settings.toasts ? "on" : "off") + "   sound " + (Settings.sound ? "on" : "off")
                + "\n pictures " + (Settings.pictures ? "on" : "off") + "   colorblind " + (Settings.colorblindMap ? "on" : "off") + "   font " + Settings.fontScale
                + "   windows " + (Settings.rememberWindows ? "remembered" : "default") + "\n art " + (Settings.paintedArt ? "on" : "off") + "   artfresh " + (Settings.artFresh ? "on" : "off") + "   (settings reroll now = repaint every picture)"
                + "\n recap " + (Settings.recapWeeks == 0 ? "off" : Settings.recapWeeks + " weeks") + "   muted notices " + Settings.mutedNotifications
                + "\n parallel " + (Settings.parallelNpcs ? "on" : "off") + " (EXPERIMENTAL: the NPCs of different tiles take their turns together on several cores; faster ticks in a big world)"
                + "\n " + MemWatch.line() + "   epochmem " + Settings.epochMemoryPercent + " (% of that memory alive that brings the epoch; 0 = off)"
                + "\n new world: startpop " + String.format("%,d", Settings.startPopulation) + " (people at the start; 10,000 to 150,000 is recommended)\n epoch: epochpop " + String.format("%,d", Settings.epochLimit()) + (Settings.epochCustom ? "" : " (automatic: twice the start; epochpop <n> to choose)") + " (most cultivators)   epochseconds " + Settings.epochTickSeconds + " (slowest typical turn)   epochcull " + (Settings.epochCullNumber > 0 ? String.format("%,d", Settings.epochCullNumber) + " (a number, in all)" : Settings.epochCullPercent + "% (of each realm)") + "   (epochcull 20% or epochcull 5000)"
                + "\n epochrealm " + Settings.epochMaxRealm + " (the epoch culls and blesses only Major Lvl 0 to this)   crowd " + Settings.crowdFactor + " (a tile is overcrowded above this many times its fair share: " + GameDriver.CROWD_CAPACITY + " people on a wild tile, " + GameDriver.CROWD_CAPACITY_CITY + " in a city)"
                + "\n spoilrefund " + Settings.spoilRefundPercent + " (% of the price that spoiled or scrapped goods pay back to their owner)"
                + "\nchange: settings <key> <value>   (settings notify <category> on|off; categories: " + String.join(" ", Notifications.CATEGORIES) + ")");
            return false;
        }
        String k = t[1].toLowerCase(), v = t[2].toLowerCase();
        if ((k.equals("epochpop") || k.equals("epochpopulation")) && (v.equals("auto") || v.equals("0"))) { Settings.epochCustom = false; say("The epoch comes when " + String.format("%,d", Settings.epochLimit()) + " cultivators live (twice the starting population)."); return false; }
        if ((k.equals("autosave") || k.equals("autosavekeep") || k.equals("challenges") || k.equals("volume") || k.equals("epochpop") || k.equals("epochpopulation") || k.equals("startpop") || k.equals("epochmem") || k.equals("spoilrefund")) && !v.matches("-?\\d+")) { say("'settings " + k + "' takes a number, e.g. settings " + k + " 5"); return false; }
        if ((k.equals("dialogue") || k.equals("dialogueflavor") || k.equals("dialoguepopups") || k.equals("dialogueauto") || k.equals("confirm") || k.equals("hints") || k.equals("hud") || k.equals("autoequip") || k.equals("tooltips") || k.equals("toasts") || k.equals("parallel") || k.equals("parallelise") || k.equals("parallelize")) && !v.matches("on|off|true|false|yes|no|1|0")) { say("'settings " + k + "' takes on or off, e.g. settings " + k + " off"); return false; }
        switch (k) {
            case "hud": Settings.hud = onOff(v); break;
            case "parallel": case "parallelise": case "parallelize": Settings.parallelNpcs = onOff(v); say(Settings.parallelNpcs ? "EXPERIMENTAL: the NPCs of different tiles now take their turns together on " + Map.tickPool.getParallelism() + " cores (people on a tile with a person who plays are still played one by one). 'settings parallel off' goes back." : "NPC turns are played one after the other."); break;
            case "lore": Settings.lore = onOff(v); if (Settings.lore && Lore.state == null) say("This world began without the lore: 'lore enable' founds the Palace at Originis."); break;
            case "lorepause": Settings.lorePause = onOff(v); break;
            case "dialogue": case "dialogueflavor": Settings.dialogueFlavor = onOff(v); break;
            case "dialoguepopups": Settings.dialoguePopups = onOff(v); break;
            case "dialogueauto": Settings.dialogueAuto = onOff(v); break;
            case "spread": Settings.startSpread = Math.max(0, Math.min(31, num(v, Settings.startSpread))); if (Settings.startSpread > 0 && Settings.startSpread % 2 == 0) Settings.startSpread++; say("A NEW world of 20,000+ people starts spread over " + (Settings.startSpread == 0 ? "the 3 x 3 tiles around Originis" : Settings.startSpread + " x " + Settings.startSpread + " tiles around Originis") + "."); break;
            case "spoilrefund": Settings.spoilRefundPercent = Math.max(0, Math.min(100, num(v, Settings.spoilRefundPercent))); say("Goods that spoil or are scrapped pay their owner " + Settings.spoilRefundPercent + "% of their price back in spirit stones."); break;
            case "epochpop": case "epochpopulation": Settings.epochPopulation = Math.max(1000, Math.min(10000000, num(v, Settings.epochPopulation))); Settings.epochCustom = true; say("The epoch comes when " + String.format("%,d", Settings.epochPopulation) + " cultivators live."); break;
            case "epochmem": case "epochmemory": Settings.epochMemoryPercent = Math.max(0, Math.min(95, num(v, Settings.epochMemoryPercent))); say(Settings.epochMemoryPercent == 0 ? "The epoch no longer comes because of memory." : "The epoch also comes when more than " + Settings.epochMemoryPercent + "% of the memory the game may use is alive (" + MemWatch.line() + ")."); break;
            case "startpop": case "startpopulation": {
                int n = Math.max(1000, Math.min(1000000, num(v, Settings.startPopulation))); Settings.startPopulation = n;
                say("A NEW world will begin with " + String.format("%,d", n) + " people (this world keeps its " + String.format("%,d", Map.startingPopulation) + ")." + StartMenu.populationWarning(n)); break; }
            case "epochsec": case "epochseconds": case "epochtick":
                try { Settings.epochTickSeconds = Math.max(0.1, Math.min(600, Double.parseDouble(v))); } catch (NumberFormatException e) { say("epochseconds: a number of seconds like 2 or 0.5"); return false; }
                say("The epoch comes when a typical turn takes more than " + Settings.epochTickSeconds + " s (in a world of " + String.format("%,d", GameDriver.EPOCH_MIN_LIVING) + "+ cultivators)."); break;
            case "epochcull":
                if (!v.matches("\\d+%?")) { say("epochcull takes a percent like 20% or a number like 5000"); return false; }
                if (v.endsWith("%")) { Settings.epochCullPercent = Math.max(1, Math.min(95, num(v.substring(0, v.length() - 1), Settings.epochCullPercent))); Settings.epochCullNumber = 0; say("The epoch takes " + Settings.epochCullPercent + "% of each realm."); }
                else { Settings.epochCullNumber = Math.max(0, Math.min(10000000, num(v, 0))); say(Settings.epochCullNumber == 0 ? "The epoch takes " + Settings.epochCullPercent + "% of each realm." : "The epoch takes " + String.format("%,d", Settings.epochCullNumber) + " cultivators in all (shared out over the realms by size)."); }
                break;
            case "epochrealm": case "epochmaxrealm": {
                if (!v.matches("\\d+")) { say("epochrealm takes a Major Lvl from 0 to 10, e.g. settings epochrealm 3"); return false; }
                Settings.epochMaxRealm = Math.max(0, Math.min(10, num(v, Settings.epochMaxRealm)));
                say("The epoch takes people only from Major Lvl 0 to " + Settings.epochMaxRealm + " (realms of fewer than 10 are spared), and blesses the survivors of those realms."); break; }
            case "crowd": case "crowdfactor": {
                double f;
                try { f = Math.max(0.5, Math.min(100, Double.parseDouble(v))); } catch (NumberFormatException e) { say("crowd takes a number such as 3.5: a tile is overcrowded above that many times its fair share of the starting population"); return false; }
                Settings.crowdFactor = f; GameDriver.rescaleCrowd();
                say("A tile is overcrowded above " + f + " times its fair share: " + GameDriver.CROWD_CAPACITY + " cultivators on a wild tile, " + GameDriver.CROWD_CAPACITY_CITY + " in a city (Originis has no limit)."); break; }
            case "autosave": Settings.autosaveYears =Math.max(0, Math.min(1000, num(v, Settings.autosaveYears))); break;
            case "autosavekeep": Settings.autosaveKeep = Math.max(1, Math.min(20, num(v, Settings.autosaveKeep))); break;
            case "confirm": Settings.confirmRisky = onOff(v); break;
            case "hints": Settings.hints = onOff(v); break;
            case "autoequip": Settings.autoEquipManual = onOff(v); break;
            case "maturestart": Settings.matureStart = onOff(v); break;
            case "challenges": Settings.maxRandomChallenges = Math.max(0, Math.min(50, num(v, Settings.maxRandomChallenges))); break;
            case "music": Settings.music = onOff(v); Ambient.apply(); break;
            case "volume": Settings.musicVolume = Math.max(0, Math.min(100, num(v, Settings.musicVolume))); Ambient.apply(); break;
            case "newpiece": Ambient.newPiece(); break;
            case "highcontrast": Settings.highContrast = onOff(v); break;
            case "windows": Settings.rememberWindows = onOff(v); if (!Settings.rememberWindows) Settings.windowBounds.clear(); break; // pop-up windows reopen where you left them
            case "tooltips": Settings.tooltips = onOff(v); break;
            case "toasts": Settings.toasts = onOff(v); break;
            case "sound": Settings.sound = onOff(v); break;
            case "pictures": Settings.pictures = onOff(v); PlaceImages.enabled = Settings.pictures; break;
            case "art": Settings.paintedArt = onOff(v); Settings.applyArt(); PlaceImages.reload(); break;
            case "artfresh": Settings.artFresh = onOff(v); Settings.applyArt(); PlaceImages.reload(); break;
            case "reroll": Settings.rerollArt(); break;
            case "colorblind": Settings.colorblindMap = onOff(v); break;
            case "font": try { Settings.fontScale = Math.max(0.7, Math.min(2.5, Double.parseDouble(v))); } catch (NumberFormatException e) { say("font: a number like 1.25"); return false; } break;
            case "recap": Settings.recapWeeks = v.equals("off") ? 0 : v.equals("season") ? 13 : v.equals("year") ? 52 : Math.max(0, num(v, Settings.recapWeeks)); break;
            case "notify": {
                if (t.length < 4) { say("Usage: settings notify <category> on|off"); return false; }
                if (java.util.Arrays.asList(Notifications.CATEGORIES).indexOf(t[2].toLowerCase()) < 0) { say("Unknown category. Use: " + String.join(" ", Notifications.CATEGORIES)); return false; }
                if (onOff(t[3])) Settings.mutedNotifications.remove(t[2].toLowerCase()); else Settings.mutedNotifications.add(t[2].toLowerCase());
                break;
            }
            default: say("Unknown setting '" + k + "'. Type 'settings' for the list."); return false;
        }
        Settings.save();
        if (GameDriver.guiEnabled && GameDriver.mapViewer != null) javax.swing.SwingUtilities.invokeLater(() -> GameDriver.mapViewer.applySettings());
        say("Set: " + String.join(" ", Arrays.copyOfRange(t, 1, t.length)) + "   ('settings' shows them all)");
        return false;
    }

    // ------------------------------------------------------------------ autopilot
    // One step of a 'goto' walk (also used by the driver on later ticks); hands control back on arrival
    /** Shortest signed distance along one axis of the wrapping world. */
    private static int wrapDelta(int d) { d = Math.floorMod(d, Map.range); return d > Map.range / 2 ? d - Map.range : d; }

    /** One step of a human's "go there": the shortest way round the wrapping world, diagonal when both axes differ (one turn per tile, like every other
     *  traveller). A chase re-aims at the target's current tile every turn and gives up after 150 turns. */
    static void stepAuto(Character c) {
        if (c.chaseId >= 0) {
            Character t = c.chaseId < Map.characters.size() ? Map.characters.get(c.chaseId) : null;
            if (t == null || t.dead || t.location == null) { stopAuto(c); say("The one you were chasing is gone."); return; }
            if (++c.chaseTurns > 150) { stopAuto(c); say("You gave up the chase after 150 turns: " + t.name + " " + t.lastName + " keeps moving. Try again or wait for them."); return; }
            int[] aim = c.interceptAim(t); // close behind a walker: go to the tile they step to
            c.autoX = aim != null ? aim[0] : t.location.x; c.autoY = aim != null ? aim[1] : t.location.y;
        }
        int dx = wrapDelta(c.autoX - c.location.x), dy = wrapDelta(c.autoY - c.location.y);
        if (dx == 0 && dy == 0) {
            boolean chasing = c.chaseId >= 0;
            Character t = chasing ? Map.characters.get(c.chaseId) : null;
            if (chasing && t != null && !t.dead && t.location != c.location) { c.autoX = t.location.x; c.autoY = t.location.y; return; } // met them at the tile they were heading for, but they stepped on: keep going
            stopAuto(c);
            say(chasing && t != null ? "You caught up with " + t.name + " " + t.lastName + " at " + PlayerControl.coord(c.location) + "." : "You arrived at " + PlayerControl.coord(c.location) + ".");
            return;
        }
        if (dx != 0 && dy != 0) c.movement(dx > 0 ? (dy > 0 ? 'E' : 'C') : (dy > 0 ? 'Q' : 'Z')); // 'w' / north is +y
        else if (dx != 0) c.movement(dx > 0 ? 'd' : 'a');
        else c.movement(dy > 0 ? 'w' : 's');
    }

    private static void stopAuto(Character c) {
        c.autoX = -1; c.autoY = -1; c.chaseId = -1; c.chaseTurns = 0; c.movement = 'x'; c.skipping = 0;
        c.movement('x');
    }

    /** `qdo` with no number: does the most pressing quest (alerts, duties, your waypoints; routine work is skipped) that can be done where you stand. */
    private static boolean doTopQuest(Character c, PlayerControl pc) {
        String firstWhy = null;
        for (Quest q : pc.buildView(c)) {
            if (q == null || PlayerControl.classify(c, q) == PlayerControl.MUNDANE) continue;
            String why = c.doQuestNow(q);
            if (why == null) return true;
            if (firstWhy == null) firstWhy = why;
        }
        say(firstWhy == null ? "No quest to do. 'q' lists them; 'goquest' walks to the first one with a place." : "Nothing can be done here. " + firstWhy + "  ('goquest' walks there.)");
        return false;
    }

    /** `goquest [n|#uid]`: walks to the quest's place, or chases its person (the first quest of the last list when no number is given). */
    private static boolean goQuestCommand(Character c, String[] t) {
        PlayerControl pc = c.pc();
        Quest q = null;
        if (t.length > 1) q = pc.rowRef(c, t[1]);
        else {
            if (pc.view.isEmpty()) pc.listQuests(c);
            for (Quest x : pc.view) if (x != null && x.category != Quest.QuestCategory.Spot) { q = x; break; }
            if (q == null) { say("No quest with a place or a person to go to. 'q' lists your quests."); return false; }
        }
        if (q == null) return false;
        if (q.category == Quest.QuestCategory.Spot) { say(q.displayName() + " is done where you stand: use 'qdo " + (t.length > 1 ? t[1] : "<n>") + "'."); return false; }
        if (q.category == Quest.QuestCategory.Character) {
            Character target = Character.charById(q.targetCharacter);
            if (target == null || target.dead || target.location == null) { say(q.displayName() + ": the target is gone."); return false; }
            return chase(c, target);
        }
        if (q.targetArea == null) { say(q.displayName() + " has no place yet."); return false; }
        if (q.targetArea == c.location) { say("You are already there: 'qdo' does it."); return false; }
        c.chaseId = -1; c.chaseTurns = 0; c.autoX = q.targetArea.x; c.autoY = q.targetArea.y; c.movement = 'c'; c.skipping = 400;
        say("Heading to " + q.targetArea.name + " " + PlayerControl.coord(q.targetArea) + " (" + Math.max(Math.abs(wrapDelta(q.targetArea.x - c.location.x)), Math.abs(wrapDelta(q.targetArea.y - c.location.y))) + " turns; you move one tile, diagonals included, per turn).");
        stepAuto(c);
        return true;
    }

    private static boolean chase(Character c, Character target) {
        if (target == c) { say("That is you."); return false; }
        if (target.location == c.location) { say(target.name + " " + target.lastName + " is right here."); return false; }
        int dist = Math.max(Math.abs(wrapDelta(target.location.x - c.location.x)), Math.abs(wrapDelta(target.location.y - c.location.y)));
        boolean purposeful = target.currentQuest != null && (target.currentQuest.targetArea != null || target.currentQuest.category == Quest.QuestCategory.Character) && target.movement != 'x';
        c.chaseId = target.id; c.chaseTurns = 0; c.autoX = target.location.x; c.autoY = target.location.y; c.movement = 'c'; c.skipping = 400;
        say("Chasing " + target.name + " " + target.lastName + " (#" + target.id + "), " + dist + " tiles away at " + PlayerControl.coord(target.location) + ". You close in one tile a turn, diagonals included; someone wandering about is usually caught, someone on a long trip may stay ahead (you stop after 150 turns, or when an alert or a fight interrupts).");
        if (purposeful) say("(" + target.name + " seems to be travelling with a purpose.)");
        stepAuto(c);
        return true;
    }

    private static boolean legacyAutoMove(Character c) {
        say("Auto-move options:");
        say("  x — Wait N turns (duty quests still run)");
        say("  w/a/s/d — Move in direction N turns");
        say("  c — Move toward coordinate N turns");
        say("  m — Random movement N turns");
        char moveOpt = GameDriver.readInput().charAt(0);
        switch (moveOpt) {
            case 'x':
                c.movement = 'x';
                say("How many turns?");
                c.skipping = GameDriver.readInt() - 1;
                c.movement('x');
                return true;
            case 'w': case 'a': case 's': case 'd':
                c.movement = moveOpt;
                say("How many turns?");
                c.skipping = GameDriver.readInt() - 1;
                c.movement(moveOpt);
                return true;
            case 'c': {
                say("Target tile X Y (0-" + (Map.range - 1) + ", e.g. '15 15'):");
                c.autoX = GameDriver.readInt();
                c.autoY = GameDriver.readInt();
                c.movement = 'c';
                say("How many turns?");
                c.skipping = GameDriver.readInt() - 1;
                if (c.autoX < 0 || c.autoX >= Map.range || c.autoY < 0 || c.autoY >= Map.range) {
                    say("Off the map."); c.autoX = -1; c.autoY = -1; c.movement = 'x'; c.skipping = 0; c.movement('x');
                    return true;
                }
                stepAuto(c);
                return true;
            }
            case 'm':
                c.movement = 'm';
                say("How many turns?");
                c.skipping = GameDriver.readInt() - 1;
                c.movement();
                return true;
            default:
                c.movement('x');
                return true;
        }
    }

    // ------------------------------------------------------------------ duties / policy / script
    private static boolean dutiesCommand(PlayerControl pc, String[] t) {
        if (t.length == 1 && GameDriver.guiEnabled && GameDriver.mapViewer != null) { // the GUI answers `duties` with its window (the terminal prints the list)
            javax.swing.SwingUtilities.invokeLater(() -> GuiDuties.open(GameDriver.mapViewer, "Duties"));
            return false;
        }
        if (t.length == 1) {
            StringBuilder sb = new StringBuilder("=== Managed duties: " + pc.dutySummary() + " ===\n");
            for (int i = 0; i < PlayerControl.DUTY_NAMES.length; i++)
                sb.append(String.format("  %-11s %s%n", PlayerControl.DUTY_NAMES[i], pc.duties[i] ? "ON  (auto)" : "off (you do it)"));
            sb.append("'duties all on|off' flips everything at once; 'duties <name> on|off' one at a time.\n");
            say(sb.toString());
            return false;
        }
        if (t[1].equalsIgnoreCase("all") && t.length > 2) {
            pc.setAllDuties(onOff(t[2]));
            say("Duties: " + pc.dutySummary());
            return false;
        }
        int idx = PlayerControl.dutyIndex(t[1]);
        if (idx < 0 || t.length < 3) { say("Usage: duties | duties all on|off | duties <" + String.join("|", PlayerControl.DUTY_NAMES) + "> on|off"); return false; }
        pc.duties[idx] = onOff(t[2]);
        say("Duty '" + PlayerControl.DUTY_NAMES[idx] + "' is " + (pc.duties[idx] ? "ON" : "off") + ".  Duties: " + pc.dutySummary());
        return false;
    }

    /** `mission` lists the board, `mission <n>` takes one (at your sect's home), `mission abandon` drops yours. */
    private static boolean missionCommand(Character c, String[] t) {
        if (c.sect == null) { say("You belong to no sect."); return false; }
        String arg = t.length > 1 ? t[1].toLowerCase() : "";
        if (arg.equals("abandon") || arg.equals("drop")) {
            if (c.sectMission == null) { say("You have no mission."); return false; }
            c.abandonMission(); say("Mission abandoned."); return false;
        }
        if (arg.matches("\\d+")) {
            int n = num(arg, 0);
            if (c.sectMission != null) { say("You already have a mission: " + c.missionLine() + ". 'mission abandon' drops it."); return false; }
            if (c.location != c.sect.location) { say("Take missions at your sect's home, " + c.sect.location.name + " " + PlayerControl.coord(c.sect.location) + " ('goto' walks there)."); return false; }
            if (n < 1 || n > c.sect.questBoard.size()) { say("The board has " + c.sect.questBoard.size() + " missions: 'mission' lists them."); return false; }
            String mm = c.sect.questBoard.get(n - 1);
            if (mm.startsWith("Deliver|") && !Life.canMake(c, mm)) { say("You cannot make or buy those goods yet."); return false; }
            c.acceptMission(mm);
            return false;
        }
        StringBuilder sb = new StringBuilder("=== Missions of " + c.sect.name + " ===\n");
        if (c.sectMission != null) sb.append("Yours: ").append(c.missionLine()).append("  ('mission abandon' drops it; it is in your Quest Log)\n");
        if (c.sect.questBoard.isEmpty()) sb.append("  (the board is empty)\n");
        for (int i = 0; i < c.sect.questBoard.size(); i++) sb.append("  ").append(i + 1).append(". ").append(Character.missionText(c.sect.questBoard.get(i))).append("\n");
        sb.append("Take one at your sect's home: mission <n>.  Hand it in with 'qdo' on the Sect Mission quest (at the home) when you carry what it asks for.\n");
        GameDriver.showGuiInfo("Sect missions", sb.toString());
        return false;
    }

    private static boolean policyCommand(PlayerControl pc, String[] t) {
        if (t.length == 1 && GameDriver.guiEnabled && GameDriver.mapViewer != null) javax.swing.SwingUtilities.invokeLater(() -> GuiDuties.open(GameDriver.mapViewer, "Policies"));
        if (t.length == 1) {
            say("=== Decision policies (used in AUTO/HYBRID; MANUAL asks you) ===\n"
                + "  kill      : " + pc.killPolicy + " = " + PlayerControl.KILL_POLICIES[pc.killPolicy] + "   (0 always spare, 1 kill evil only, 2 kill if wronged, 3 always kill)\n"
                + "  flee      : " + (pc.fleePolicy ? "on (try to flee when outmatched)" : "off") + "\n"
                + "  forbidden : " + (pc.forbiddenPolicy ? "on (use the Forbidden Technique in a deadly fight)" : "off") + "\n"
                + "  duel      : " + (pc.duelPolicy ? "accept challenges" : "evade challenges") + "\n"
                + "  squad     : " + PlayerControl.SQUAD_POLICIES[pc.squadPolicy] + "   (who may join your squad: ask each time, auto accept, never)\n"
                + "  friends   : " + PlayerControl.FRIEND_POLICIES[pc.friendPolicy] + "   (a friendship offered to you: ask, accept, decline; declining costs nothing; 'avoid <n>' refuses one person)\n"
                + "  feed      : " + PlayerControl.FEED_POLICIES[pc.feedPolicy] + "   (your companion beasts while you are AUTO or running a script: leftovers = food beyond " + Taming.KEEP_FOR_SELF + " portions, always = the best food whenever you have any, never; a portion each " + Taming.FEED_EVERY + " weeks; in MANUAL use 'feed <n>')\n"
                + "  timeout   : " + (pc.decisionTimeout > 0 ? pc.decisionTimeout + "s, then the policy answer is used" : "off (prompts wait for you)") + "\n"
                + "  script    : when a script ends or is interrupted, return to " + (pc.returnToAuto ? "AUTO" : "MANUAL") + "\n"
                + "  alerts    : a new quest alert " + (pc.interruptOnAlert ? "interrupts" : "does not interrupt") + " a script");
            return false;
        }
        if (t.length < 3) { say("Usage: policy <kill|flee|forbidden|duel|squad|friends|feed|script|alerts|timeout> <value>"); return false; }
        switch (t[1].toLowerCase()) {
            case "kill": {
                int v = num(t[2], -1);
                if (v < 0 || v >= PlayerControl.KILL_POLICIES.length) { say("kill policy 0-3."); return false; }
                pc.killPolicy = v;
                break;
            }
            case "squad": {
                int v = java.util.Arrays.asList(PlayerControl.SQUAD_POLICIES).indexOf(t[2].toLowerCase());
                if (v < 0) { say("squad policy: ask, auto or never."); return false; }
                pc.squadPolicy = v; pc.squadDeclined.clear();
                break;
            }
            case "feed": case "feeding": {
                int v = java.util.Arrays.asList(PlayerControl.FEED_POLICIES).indexOf(t[2].toLowerCase());
                if (v < 0) { say("feed policy: leftovers, always or never."); return false; }
                pc.feedPolicy = v;
                break;
            }
            case "friends": case "friend": {
                int v = java.util.Arrays.asList(PlayerControl.FRIEND_POLICIES).indexOf(t[2].toLowerCase());
                if (v < 0) { say("friends policy: ask, accept or decline."); return false; }
                pc.friendPolicy = v;
                break;
            }
            case "flee": if (!isBool(t[2])) { say("flee policy: on or off."); return false; } pc.fleePolicy = onOff(t[2]); break;
            case "forbidden": if (!isBool(t[2])) { say("forbidden policy: on or off."); return false; } pc.forbiddenPolicy = onOff(t[2]); break;
            case "duel": if (!isBool(t[2]) && !t[2].equalsIgnoreCase("accept") && !t[2].equalsIgnoreCase("evade") && !t[2].equalsIgnoreCase("decline")) { say("duel policy: accept or evade."); return false; } pc.duelPolicy = t[2].equalsIgnoreCase("accept") || onOff(t[2]); break;
            case "script": if (!t[2].equalsIgnoreCase("auto") && !t[2].equalsIgnoreCase("manual")) { say("script policy: auto or manual."); return false; } pc.returnToAuto = t[2].equalsIgnoreCase("auto"); break;
            case "alerts": if (!isBool(t[2])) { say("alerts policy: on or off."); return false; } pc.interruptOnAlert = onOff(t[2]); break;
            case "timeout": {
                int v = t[2].equalsIgnoreCase("off") ? 0 : num(t[2], -1);
                if (v < 0 || v > 3600) { say("timeout: seconds 1-3600, or 0/off."); return false; }
                pc.decisionTimeout = v;
                break;
            }
            default: say("Unknown policy '" + t[1] + "'."); return false;
        }
        return policyCommand(pc, new String[]{"policy"});
    }

    private static boolean scriptCommand(Character c, PlayerControl pc, String[] t, boolean confirmed) {
        if (pc.script == null) pc.script = new PlayerScript();
        PlayerScript s = pc.script;
        String sub = t.length > 1 ? t[1].toLowerCase() : "list";
        switch (sub) {
            case "add": {
                if (s.running) { say("Stop the script before editing it."); return false; }
                String err = s.add(Arrays.copyOfRange(t, 2, t.length));
                say(err != null ? err : "Added: " + s.steps.get(s.steps.size() - 1) + "  (" + s.steps.size() + " steps)");
                return false;
            }
            case "list": {
                String[] saved = ScriptStore.names();
                say(s.list() + (saved.length > 0 ? "Saved scripts: " + String.join(", ", saved) + "  ('script load <name>')\n" : "No saved scripts yet ('script save <name>' keeps this one).\n"));
                return false;
            }
            case "saved": {
                String[] saved = ScriptStore.names();
                say(saved.length == 0 ? "No saved scripts yet. 'script save <name>' keeps the current one." : "Saved scripts: " + String.join(", ", saved) + "  ('script load <name>', 'script delete <name>')");
                return false;
            }
            case "save": {
                String name = t.length > 2 ? String.join(" ", Arrays.copyOfRange(t, 2, t.length)) : "";
                String err = ScriptStore.save(name, s);
                say(err != null ? err : "Saved the script (" + s.steps.size() + " steps) as '" + ScriptStore.clean(name) + "'.");
                return false;
            }
            case "load": {
                if (s.running) { say("Stop the script before loading another."); return false; }
                String name = t.length > 2 ? String.join(" ", Arrays.copyOfRange(t, 2, t.length)) : "";
                if (name.isEmpty()) { say("Usage: script load <name>   (saved: " + String.join(", ", ScriptStore.names()) + ")"); return false; }
                if (!s.steps.isEmpty() && !confirm(c, confirmed, "Replace the current script (" + s.steps.size() + " steps) with '" + name + "'?", "script load")) return false;
                String err = ScriptStore.load(name, s);
                say(err != null ? err : "Loaded '" + ScriptStore.clean(name) + "' (" + s.steps.size() + " steps). 'script run' starts it.");
                return false;
            }
            case "delete": case "rm": {
                String name = t.length > 2 ? String.join(" ", Arrays.copyOfRange(t, 2, t.length)) : "";
                if (name.isEmpty()) { say("Usage: script delete <name>"); return false; }
                if (!confirm(c, confirmed, "Delete the saved script '" + name + "'?", "script delete")) return false;
                say(ScriptStore.delete(name) ? "Deleted the saved script '" + ScriptStore.clean(name) + "'." : "No saved script called '" + ScriptStore.clean(name) + "'.");
                return false;
            }
            case "edit": case "insert": {
                if (s.running) { say("Stop the script before editing it."); return false; }
                boolean edit = sub.equals("edit");
                int n = t.length > 2 ? num(t[2], -1) : -1;
                if (n < 1 || n > s.steps.size() + (edit ? 0 : 1) || t.length < 4) { say("Usage: script " + sub + " <step#> <step>   e.g. script " + sub + " 2 forage until 500"); return false; }
                PlayerScript tmp = new PlayerScript();
                String err = tmp.add(Arrays.copyOfRange(t, 3, t.length));
                if (err != null) { say(err); return false; }
                if (edit) s.steps.set(n - 1, tmp.steps.get(0)); else s.steps.add(n - 1, tmp.steps.get(0));
                s.reset();
                say((edit ? "Step " + n + " is now: " : "Inserted as step " + n + ": ") + tmp.steps.get(0));
                return false;
            }
            case "move": {
                if (s.running) { say("Stop the script before editing it."); return false; }
                int from = t.length > 2 ? num(t[2], -1) : -1, to = t.length > 3 ? num(t[3], -1) : -1;
                if (from < 1 || from > s.steps.size() || to < 1 || to > s.steps.size()) { say("Usage: script move <step#> <new place#>   (1-" + s.steps.size() + ")"); return false; }
                PlayerScript.Step st = s.steps.remove(from - 1);
                s.steps.add(to - 1, st);
                s.reset();
                say("Moved '" + st + "' to place " + to + ".");
                return false;
            }
            case "del": {
                int n = t.length > 2 ? num(t[2], -1) : -1;
                if (s.running) { say("Stop the script before editing it."); return false; }
                if (n < 1 || n > s.steps.size()) { say("Usage: script del <step#>"); return false; }
                s.steps.remove(n - 1);
                s.reset();
                say("Step removed.");
                return false;
            }
            case "clear":
                if (s.steps.size() > 3 && !confirm(c, confirmed, "Clear the whole script (" + s.steps.size() + " steps)?", "script clear")) return false;
                s.clear();
                say("Script cleared.");
                return false;
            case "run": case "start": {
                if (s.steps.isEmpty()) { say("The script is empty. Add steps with 'script add ...'."); return false; }
                String err = s.validate();
                if (err != null) { say(err); return false; }
                c.skipping = 0;
                s.start(c);
                say("HYBRID mode: running the script (" + s.steps.size() + " steps). 'stop' interrupts it.");
                return true;
            }
            case "resume": {
                if (s.steps.isEmpty() || s.ip >= s.steps.size()) { say("Nothing to resume."); return false; }
                c.skipping = 0;
                s.resume(c);
                say("HYBRID mode: resuming at step " + (s.ip + 1) + ".");
                return true;
            }
            case "stop":
                if (s.running) s.stop(c, "You stopped the script."); else say("No script is running.");
                return false;
            default:
                say("Usage: script add <step or any command> | list | del <n> | edit <n> <step> | insert <n> <step> | move <n> <place> | clear | run | resume | stop | save <name> | load <name> | saved | delete <name>");
                return false;
        }
    }

    // ------------------------------------------------------------------ shops
    private static boolean priceCommand(Character c, String[] t) {
        if (t.length < 4) { say("Usage: price <shop#> buy|sell|city <rate>   (see 'shops'; e.g. price 1 sell 1.8)"); return false; }
        int n = num(t[1], -1);
        if (n < 1 || n > c.ownedShops.size()) { say("You don't own shop #" + n + ". See 'shops'."); return false; }
        Shop shop = c.ownedShops.get(n - 1);
        double v;
        try { v = Double.parseDouble(t[3]); } catch (Exception e) { say("Rate must be a number."); return false; }
        switch (t[2].toLowerCase()) {
            case "buy": // what you pay sellers (fraction of item price)
                if (v < 0.05 || v > 1.0) { say("Buy rate must be between 0.05 and 1.0."); return false; }
                if (v + 0.05 > shop.sellRate) { say("Buy rate must stay at least 0.05 below your sell rate (" + shop.sellRate + ")."); return false; }
                shop.buyRate = v;
                break;
            case "sell": // what buyers pay you (multiple of item price)
                if (v > 5.0 || v < shop.buyRate + 0.05) { say("Sell rate must be between " + (shop.buyRate + 0.05) + " and 5.0 (above what you pay sellers)."); return false; }
                shop.sellRate = v;
                break;
            case "city": // what you pay the city pool when restocking
                if (v < 0.05 || v > 1.0) { say("City restock rate must be between 0.05 and 1.0."); return false; }
                shop.cityBuyRate = v;
                break;
            default: say("Use buy, sell or city."); return false;
        }
        say("Shop #" + n + " at " + shop.location.name + ": pays sellers " + shop.buyRate + "x, charges buyers " + shop.sellRate + "x, restocks from the city at " + shop.cityBuyRate + "x.");
        return false;
    }

    // ------------------------------------------------------------------ do <quest>
    private static boolean doCommand(Character c, String[] t) {
        if (t.length < 2) { say("Usage: do <quest name> [person#]"); return false; }
        // the name may include a trailing person number
        String last = t[t.length - 1];
        boolean hasNum = t.length > 2 && last.matches("#?\\d+");
        String name = String.join(" ", Arrays.copyOfRange(t, 1, hasNum ? t.length - 1 : t.length));
        if (name.equalsIgnoreCase("Have Child")) name = "Get Pregnant";
        String canonical = null;
        for (String s : DO_SPOT) if (s.equalsIgnoreCase(name)) canonical = s;
        for (String s : DO_AREA) if (s.equalsIgnoreCase(name)) canonical = s;
        for (String s : DO_PERSON) if (s.equalsIgnoreCase(name)) canonical = s;
        if (canonical == null) {
            java.util.TreeSet<String> all = new java.util.TreeSet<>(DO_SPOT); all.addAll(DO_AREA); all.addAll(DO_PERSON); all.add("Have Child");
            String close = null;
            for (String s : all) if (s.toLowerCase().contains(name.toLowerCase()) || TermFormat.distance(s.toLowerCase(), name.toLowerCase()) <= 2) { close = s; break; }
            say("'" + name + "' can't be done on demand." + (close != null ? " Did you mean '" + close + "'?" : "") + "\nYou can do: " + String.join(", ", all)
                + "\n(Other quests: travel to their target ('goquest <n>'), then 'q' and 'qdo <n>'. Quests that name a person need one: do Propose Alliance 3.)");
            return false;
        }
        if (canonical.equals("Get Pregnant") && !c.gender.equals("Female")) { say("Only a mother can conceive; have your wife use it."); return false; }
        Quest q;
        if (DO_SPOT.contains(canonical)) {
            q = new Quest(canonical, true, 0, 0, 0);
            int r = c.spotQuests(q);
            say(r >= 1 ? canonical + " done." : canonical + ": nothing happened (" + c.whyNot(q) + ").");
            return r >= 1; // a failed attempt costs no time
        }
        if (DO_AREA.contains(canonical)) {
            q = new Quest(canonical, true, 0, 0, 0, c.location);
        } else {
            Character p = hasNum ? person(c, last) : (canonical.equals("Get Pregnant") && c.spouse != null ? c.spouse : null);
            if (p == null) { say("Usage: do " + canonical + " <person#>  (numbers from 'people')"); return false; }
            if (canonical.equals("Gift Technique") && !c.children.contains(p)) { say("You can only teach your own children."); return false; }
            q = new Quest(canonical, true, 0, 0, 0, p);
        }
        if (c.questViable(q) != 1) { say(canonical + " can't be done right now: " + c.whyNot(q) + "."); return false; }
        int r = c.targetQuests(q);
        say(r == 1 ? canonical + " done." : canonical + ": nothing happened.");
        return true;
    }

    // ------------------------------------------------------------------ info screens
    static void stats(Character character) {
        StringBuilder _sb = new StringBuilder();
        _sb.append("=== ").append(character).append(" ===\n");
        _sb.append("Mode: ").append(character.pc().mode).append("   Duties: ").append(character.pc().dutySummary()).append("\n");
        _sb.append("Level: ").append(character.lvl).append("  (Major: ").append(character.majorLvl).append(" ").append(TermFormat.realm(character.majorLvl)).append("  Minor: ").append(character.minorLvl).append(")\n");
        _sb.append("Cultivation: ").append(TermFormat.n((long) character.cultivationBase)).append(" / ").append(TermFormat.n((long) character.cultivationBaseMax))
           .append(character.cultivationBaseMax > 0 ? "  (" + TermFormat.pct(Math.min(1.0, character.cultivationBase / character.cultivationBaseMax)) + " of the way to the next level; about +" + TermFormat.n((long) character.cultivation) + " a week)" : "").append("\n");
        _sb.append("Potential: ").append(character.potentialShown()).append("  Age: ").append(character.age).append(" / ").append(character.maxAge).append("\n");
        _sb.append("Affiliation: ").append(character.affiliation).append("  Trait: ").append(character.getTrait()).append("\n");
        _sb.append("Profession Lvl: ").append(character.professionShown()).append(character.leaderFounded() ? "  (a Leader's founding is done)" : "").append("\n");
        _sb.append(Reincarnation.status(character)).append("\n");
        if (character.beastIds != null && !character.beastIds.isEmpty()) _sb.append("Companions: ").append(Taming.beasts(character).size()).append("/").append(Taming.cap(character)).append("\n");
        if (character.fx != null) _sb.append("Conditions: ").append(character.fx.foodWeeks > 0 ? "nourished  " : "").append(character.fx.drunkWeeks > 0 ? "drunk  " : "").append(character.fx.inspiredWeeks > 0 ? "inspired  " : "").append(character.fx.poisonWeeks > 0 ? "POISONED  " : "").append(character.fx.coat > 0 ? "poisoned blade  " : "").append("\n");
        _sb.append("Wealth: ").append(TermFormat.n(character.ss)).append(" SS  ").append(TermFormat.n(character.sc)).append(" SC  ").append(TermFormat.n(character.dc)).append(" DC  ").append(TermFormat.n(character.ij)).append(" IJ   (= ").append(TermFormat.ss(PlayerLists.wealth(character))).append(")\n");
        _sb.append("Weapon: ").append(character.equippedWeapon != null ? character.equippedWeapon : "None").append("\n");
        _sb.append("Armor:  ").append(character.equippedClothing != null ? character.equippedClothing : "None").append("\n");
        _sb.append("Martial:  ").append(character.martialTechnique).append("\n");
        _sb.append("Movement: ").append(character.movementTechnique).append("\n");
        _sb.append("Kills: ").append(TermFormat.n(character.kills)).append("  Crafted: ").append(TermFormat.n(character.itemsCrafted)).append(character.trait == Character.Traits.Innovative ? "  Inspiration: " + character.inspiration + "/" + Character.INSPIRATION_NEEDED : "").append("  Sect Pts: ").append(TermFormat.n(character.sectPoints)).append("\n");
        if (character.sect != null) _sb.append("Sect: ").append(character.sect).append(" [").append(character.sect.getRank(character)).append("]  Sect Lvl: ").append(character.sect.sectLvl()).append("  Treasury: ").append(character.sect.treasury.size()).append(" items\n");
        _sb.append("Location: ").append(character.location.name).append(" ").append(PlayerControl.coord(character.location));
        if (character.location != null && character.location.sect != null) {
            Organization _ts = character.location.sect;
            _sb.append("\n  Tile Sect: ").append(_ts.name);
            if (_ts.master != null) _sb.append("  Master: ").append(_ts.master.name).append(" ").append(_ts.master.lastName).append(" Lv.").append(_ts.master.lvl); else if (_ts.isVirtual()) _sb.append("  Master: ???");
            _sb.append("  Members: ").append(_ts.members.size()).append("  Rank: ").append(_ts.sectLvl());
        }
        GameDriver.showGuiInfo("Stats — " + character.name + " " + character.lastName, _sb.toString());
    }

    static void places(Character character) {
        GameDriver.showGuiInfo("Locations", TermView.places(character));
    }

    static void relationships(Character character) {
        GameDriver.showGuiInfo("Relationships", TermView.relationships(character));
    }

    static void here(Character c) {
        Area a = c.location;
        StringBuilder sb = new StringBuilder("=== " + a.name + " " + PlayerControl.coord(a) + " ===\n");
        String regionLine = Regions.line(a);
        if (!regionLine.isEmpty()) sb.append(regionLine).append("\n");
        sb.append("Beasts: ").append(TermFormat.n(a.monsters)).append("  (area Lv ").append(a.lvl).append(", strongest ").append(a.highestLvl).append(")");
        sb.append("   Herbs: ").append(TermFormat.n(a.herbMaterials)).append("   Metal: ").append(TermFormat.n(a.metalMaterials)).append("\n");
        if (a.city) {
            sb.append("City: Lv ").append(a.cityLevel()).append("  population ").append(TermFormat.n(a.population)).append("  treasury ").append(TermFormat.ss((long) a.cityTreasury))
              .append("  lord ").append(a.cityOwner != null ? a.cityOwner : "none").append("\n");
            sb.append("  Shops ").append(a.shops.size()).append("/").append(a.maxShopSlots()).append("  Libraries ").append(a.libraries.size())
              .append("  Arrays ").append(a.arrayBag == null ? 0 : a.arrayBag.size()).append("\n");
        }
        if (a.sect != null) sb.append("Sect: ").append(a.sect.name).append(" (master ").append(a.sect.master != null ? a.sect.master : a.sect.isVirtual() ? "???" : "none").append(")\n");
        if (a.dungeonType != null) sb.append("Dungeon: ").append(a.dungeonType).append("\n");
        if (a.hasHeaven()) sb.append("A Heaven-Sent treasure lies here: ").append(a.heavenLabel()).append("!\n");
        if (a.tournamentTimer > 0) sb.append("Tournament: Realms ").append(a.tournamentMinMajor).append("-").append(a.tournamentMaxMajor)
                .append(", ").append(a.tournamentTimer).append(" weeks left, ").append(a.tournamentRegistrants.size()).append(" registered\n");
        if (a.crowdHotspot) sb.append("WARNING: overcrowded — beasts are ambushing cultivators here.\n");
        if (a.bounties != null && !a.bounties.isEmpty()) sb.append("Bounty board: ").append(a.bounties.size()).append(" posted\n");
        sb.append("Cultivators here: ").append(a.characters.size()).append("  (use 'people' to list them)\n");
        GameDriver.showGuiInfo("Here", sb.toString());
    }

    static void bag(Character c) {
        GameDriver.showGuiInfo("Inventory", TermView.bag(c));
    }

    static void shops(Character c) {
        GameDriver.showGuiInfo("Shops", TermView.shops(c));
    }

    static void city(Character c) {
        Area a = c.location;
        if (!a.city) { say("You are not in a city."); return; }
        here(c);
    }

    static void sect(Character c) {
        Organization s = c.sect != null ? c.sect : c.location.sect;
        if (s == null) { say("You belong to no sect, and none is here."); return; }
        StringBuilder sb = new StringBuilder("=== " + s.name + " ===\n");
        sb.append("Master: ").append(s.master != null ? s.master : s.isVirtual() ? "???" : "none").append("  Members: ").append(s.members.size()).append("  Sect Lvl ").append(s.sectLvl()).append("\n");
        sb.append("Home: ").append(s.location.name).append(" ").append(PlayerControl.coord(s.location)).append("\n");
        sb.append("Treasury: ").append(s.treasury.size()).append(" stacks, ").append(s.sectCash).append(" ss\n");
        sb.append("War: ").append(s.warTarget != null ? s.warTarget.name : "none").append("  Allies: ").append(s.allies.size()).append("\n");
        if (c.sect == s) sb.append("You: ").append(s.getRank(c)).append(", ").append(c.sectPoints).append(" sect points")
                .append(c.sectMission != null ? ", mission " + c.sectMission : "").append("\n");
        GameDriver.showGuiInfo("Sect", sb.toString());
    }

    static void party(Character c) {
        StringBuilder sb = new StringBuilder("=== Party ===\n");
        if (c.partyLeaderId >= 0) sb.append("You follow ").append(Character.charById(c.partyLeaderId)).append("\n");
        int n = 0;
        for (Character p : new ArrayList<>(c.location.characters)) {
            if (p != null && !p.dead && p.partyLeaderId == c.id) { sb.append("  ").append(p).append(" Lv.").append(p.lvl).append("\n"); n++; }
        }
        if (n == 0 && c.partyLeaderId < 0) sb.append("  (no party — 'invite <n>' someone from 'people')\n");
        GameDriver.showGuiInfo("Party", sb.toString());
    }

    static void auction(Character c) {
        GameDriver.showGuiInfo("Auction", "=== " + Auctions.text(c.location, c) + "===");
    }

    static void library(Character c, String[] t) {
        if (!c.location.city || c.location.libraries.isEmpty()) { say("No library here: libraries stand in cities ('l' lists the places you know, 'goto' walks there)."); return; }
        GameDriver.showGuiInfo("Library", TermView.library(c, t));
    }
}
