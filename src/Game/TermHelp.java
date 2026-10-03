package Game;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/** The terminal's command registry: every command grouped into categories with a one-line summary, usage, details and aliases.
 *  Drives `help` (overview / `help <category>` / `help <command>` / `help all`), the 'did you mean...?' for unknown words and the
 *  unambiguous-prefix shortcuts (`inv` = inventory = bag). The switch in PlayerCommands stays the single source of what a command DOES;
 *  this file only describes and finds them, so an entry missing here never breaks a command. */
final class TermHelp {
    private TermHelp() { }

    static final class Cmd {
        final String name, cat, usage, summary, details;
        final String[] aliases;
        Cmd(String name, String aliases, String cat, String usage, String summary, String details) {
            this.name = name; this.cat = cat; this.usage = usage; this.summary = summary; this.details = details;
            this.aliases = aliases.isEmpty() ? new String[0] : aliases.split(" ");
        }
    }

    /** Category key -> {label, one-line description}. Order = order in the overview. */
    static final String[][] CATS = {
        {"move", "Move", "walk, wait, travel, look at the map"},
        {"quests", "Quests", "what the world asks of you, and how to do it"},
        {"look", "Look", "your sheet, this place, the world's story"},
        {"people", "People", "who is here: talk, spar, fight, party, lists and search"},
        {"gear", "Gear", "backpack, equipment, techniques, the library"},
        {"work", "Work", "gather, craft, meditate, hunt, sell"},
        {"trade", "Trade", "cities, shops, the goods market, auctions"},
        {"life", "Life arts", "beasts, food and wine, poison, music, rebirth"},
        {"sect", "Sect and rank", "join, lead, found, war and peace"},
        {"auto", "Autopilot", "let the character run itself: auto, scripts, duties, policies, watch"},
        {"system", "System", "saves, settings, exports, help and shortcuts"},
    };

    static final List<Cmd> CMDS = new ArrayList<>();
    private static void e(String name, String aliases, String cat, String usage, String summary, String details) { CMDS.add(new Cmd(name, aliases, cat, usage, summary, details)); }

    static {
        // ---- move
        e("w", "a s d nw ne sw se north south east west up down left right n", "move", "w | a | s | d      nw | ne | sw | se",
            "Move one tile (w north, a west, s south, d east; two letters = diagonal)",
            "Each step takes one week. North is up on the map and y grows to the north. Use 'goto <x> <y>' for a long walk.");
        e("x", "stay rest pass skip", "move", "x", "Stay where you are for one week (same as 'wait 1')", "Duties you switched on, quest alerts and notices still happen.");
        e("wait", "", "move", "wait [weeks]", "Stand still for several weeks (default 1, at most 5,200)",
            "An attack or a new quest alert cuts a long wait short. Use 'watch' to wait until something happens.\ne.g.  wait 10");
        e("walk", "", "move", "walk <w|a|s|d> <weeks>", "Keep walking one way for some weeks", "e.g.  walk d 5   (five steps east)");
        e("goto", "go travel", "move", "goto <x> <y>", "Walk to a place until you arrive (or something interrupts you)",
            "Coordinates are the ones printed everywhere, like [1500, 1500] (type 1500 1500), or plain tile numbers. 'route <x> <y>' previews the trip first.\ne.g.  goto 1500 1400");
        e("goquest", "gq", "move", "goquest [n|#uid]", "Walk to the place (or person) of a quest", "With no number it takes the first quest of 'q' that has somewhere to go.\ne.g.  goquest 2");
        e("chase", "", "move", "chase <#id>", "Follow a cultivator anywhere in the world until you catch up", "Gives up after 150 turns. Find ids with 'people' or 'find <name>'.\ne.g.  chase #1234");
        e("o", "", "move", "o", "Old auto-move menu (asks step by step; 'goto' and 'wait' are faster)", "");
        e("route", "", "move", "route <x> <y>", "Preview a walk: distance, arrival date, danger on the way", "");
        e("map", "", "move", "map [radius] [biome|level|beasts|people|sect]", "ASCII map of the world around you", "");
        // ---- quests
        e("q", "quests quest todo tasks jobs", "quests", "q", "List your quests, numbered: where to go, how far, how pressing",
            "[alert] quests are the ones MANUAL mode leaves to you; [routine] ones only run in AUTO or when a matching duty is on. The numbers are what qdo, qp, qu, qd and goquest take.\ne.g.  q  then  qdo 1");
        e("inbox", "alerts", "quests", "inbox", "Every quest alert still waiting", "Alerts also print as '[Quest Alert]' lines the moment they arrive.");
        e("qdo", "", "quests", "qdo <n>", "Do quest n of the last 'q' list (place quests: stand at the place first)", "A quest that is not here says how far away it is. 'goquest <n>' walks there.\ne.g.  qdo 2");
        e("qp", "", "quests", "qp <n> <priority>", "Pin a quest's priority (0-1000); a pinned quest never ages", "e.g.  qp 3 90");
        e("qu", "", "quests", "qu <n>", "Unpin a quest again", "");
        e("qd", "", "quests", "qd <n>", "Delete a quest from your list (it may come back; 'mute' stops that)", "");
        e("mute", "", "quests", "mute <quest name>", "Never post this kind of quest again (and remove the queued ones)", "e.g.  mute Find Person   ('unmute <name>' undoes it)");
        e("unmute", "", "quests", "unmute <quest name>", "Allow a muted quest kind again", "");
        e("qnew", "", "quests", "qnew <label> <x> <y> <priority> [repeat]", "Create your own waypoint quest", "The tile must have been visited. e.g.  qnew home 1500 1500 70");
        e("seek", "", "quests", "seek <id> friend|fight|spar [priority]", "Pin a quest to reach a cultivator anywhere in the world", "e.g.  seek 1234 spar");
        e("do", "", "quests", "do <quest name> [person#]", "Do a quest kind on demand (Donate to Sect, Hold Tournament, Restock Trip, Have Child ...)", "e.g.  do Buy City Array   or   do Have Child");
        // ---- look
        e("i", "stats status stat me self", "look", "i", "Your character sheet: level, cultivation, wealth, gear, sect", "");
        e("here", "", "look", "here", "This place: beasts, herbs, metal, city, sect, who is around", "'people' lists the cultivators here, 'monsters' the awakened beasts.");
        e("l", "places locations", "look", "l", "Places you know, nearest first, with coordinates", "'list places' filters and sorts them (e.g. list places type=city).");
        e("r", "relationships rels", "look", "r", "Your friends, rivals and enemies", "'relations [n]' is the sheet of your strongest bonds; 'family' your kin.");
        e("time", "date", "look", "time", "Year, week, season, your birthday, the next auction", "");
        e("journal", "", "look", "journal [n]", "Your own story so far", "");
        e("fights", "fightlog", "look", "fights [n]", "The reports of your last fights (default 3)",
            "Each report says who fought, the odds, whether anyone fled or used a forbidden technique, who won, whether the loser was killed, spared or left injured, and any loot or spirit stones paid. The same text is printed when the fight happens.\ne.g.  fights 5");
        e("lore", "story", "look", "lore [journal|clues|where|palace|roll|hall|scenes|replay <id>|register|join|cross|leave|on|off|enable]", "The story of the Palace of the Oldest Order: your journal and next objective",
            "Plain 'lore' shows the road so far and what the Order asks now. 'lore clues' lists what you have learned, 'lore where' the places, 'lore palace' the Order itself, 'lore replay <id>' reads a scene again, 'lore cross' (Major Lvl 10, level 101) opens the Seam to the Gardener and asks how the story ends ('lore leave' goes back through it after a Stay); 'lore roll' and 'lore hall' show the Roll of the Crossed and the Hall of Fame.");
        e("history", "chronicle", "look", "history [text|#id] [kind=...] [n]", "Search the world's chronicle", "e.g.  history Wei   or   history kind=war 50");
        e("dash", "dashboard", "look", "dash", "World statistics: realms, wealth, deaths", "");
        e("digest", "", "look", "digest [weeks]", "What happened in the world lately", "");
        e("family", "", "look", "family", "Your spouse, parents, children, siblings", "");
        e("relations", "", "look", "relations [n]", "Your n strongest bonds with details (default 25)", "");
        e("lineage", "", "look", "lineage", "Your past lives", "");
        e("story", "", "look", "story", "Your start and the objectives of a Story run", "");
        e("recap", "", "look", "recap", "Summary of the last stretch of your life", "");
        e("notices", "", "look", "notices [n]", "The latest notifications", "");
        e("note", "notes unnote", "look", "note <text> | note #<id> <text> | notes | unnote <n>", "Bookmarks for tiles and people", "");
        e("runstats", "run", "look", "runstats", "Statistics of the whole run", "");
        e("halloffame", "fame", "look", "halloffame", "Past runs", "");
        e("find", "", "look", "find <name|#id> [limit] [filters]", "Search every cultivator in the world", "e.g.  find Wei lvl>=20 sort=lvl desc");
        // ---- people
        e("people", "players who", "people", "people [name] [limit] [filters]", "Cultivators in this tile, numbered for talk/spar/attack ...", "Same as 'list people'. e.g.  people 30 lvl>=20 aff=evil sort=lvl desc");
        e("monsters", "", "people", "monsters", "Awakened beasts (monsters) in this tile", "");
        e("list", "ls", "people", "list people|monsters|shop|bag|places [limit] [key=val key>=n ...] [sort=key desc]", "Filtered, sorted lists that the other commands number from",
            "Filters: key=text (contains), key>=n, key<=n ...; numbers accept 5k, 2m. The keys are printed under each list.\ne.g.  list shop type=weapon price<=5k afford=1 sort=price");
        e("limit", "", "people", "limit <1-500>", "Default number of rows a list shows", "");
        e("talk", "chat", "people", "talk [person]", "Talk to someone here (raises friendship)", "A person is a list number from 'people', or #<id> for anyone in the tile; leave it out and a suitable one is picked.\ne.g.  talk 3   talk #1234");
        e("spar", "", "people", "spar [person]", "Friendly fight (nobody dies)", "");
        e("attack", "fight", "people", "attack [person]", "A real fight; asks first when the odds are bad", "'preview <person>' shows your chances.");
        e("invite", "", "people", "invite [person]", "Ask someone to join your party", "");
        e("leave", "", "people", "leave", "Leave the party you are in", "");
        e("disband", "", "people", "disband", "Dissolve your own party", "");
        e("party", "squad", "people", "party", "Who is in your party", "");
        e("preview", "", "people", "preview <person|#id>", "Chances of a fight before you start it", "");
        e("follow", "track", "people", "follow <id>", "Track a cultivator (GUI tracker)", "'unfollow <id>' stops.");
        e("unfollow", "untrack", "people", "unfollow <id>", "Stop tracking a cultivator", "");
        e("control", "", "people", "control <id>", "Take over another cultivator (yours carries on as an NPC)", "");
        e("gift", "", "people", "gift <person> <bag#>", "Give a backpack item to someone here", "");
        // ---- gear
        e("bag", "inventory inv backpack pack items", "gear", "bag", "Your backpack, materials, pills, arrays and techniques", "'list bag' filters and sorts the backpack. 'equip <n>' wears an item, 'sell <n>' sells one.");
        e("equip", "wear", "gear", "equip [bag#]", "Put on the best gear (or the numbered item)", "");
        e("unequip", "", "gear", "unequip weapon|armor", "Take it off", "");
        e("compare", "", "gear", "compare <bag#>", "What an item would change in your fighting level", "");
        e("consume", "", "gear", "consume", "Take your pills", "");
        e("install", "", "gear", "install", "Install a carried array in this place", "");
        e("techniques", "tech", "gear", "techniques", "Your technique slots and every technique known", "");
        e("library", "", "gear", "library [limit] [words] [type=martial|movement] [potential>=n] [price<=n] [sort=potential:desc]", "Techniques for sale in this city's library", "'learn <n>' buys one (the numbers stay the library's own on a filtered list). The library keeps one book for each type and potential.");
        e("learn", "", "gear", "learn <n>", "Buy technique n from the library", "");
        // ---- work
        e("forage", "gather herbs", "work", "forage", "Gather herbs (a turn)", "");
        e("mine", "dig", "work", "mine", "Dig for metal (a turn)", "");
        e("craft", "", "work", "craft", "Practise your profession (a turn)", "");
        e("meditate", "cultivate study", "work", "meditate", "Cultivate: the main way to grow stronger", "");
        e("hunt", "", "work", "hunt", "Hunt the beasts of this tile", "");
        e("sell", "", "work", "sell [bag#] | sell goods|food|wine|poison [qty]", "Sell your loot in a city", "Without a number it sells everything sellable.");
        e("buygear", "", "work", "buygear", "Buy the best gear you can afford here", "");
        // ---- trade
        e("city", "", "trade", "city [name]", "The city you stand in, or any city by name", "");
        e("shops", "", "trade", "shops", "Shops you own", "'price <shop#> buy|sell|city <rate>' sets their rates.");
        e("market", "", "trade", "market", "The city's goods market (food, wine, poison)", "'buy market <#> [qty]' buys.");
        e("goods", "", "trade", "goods", "The food, wine and poison you carry", "");
        e("auction", "", "trade", "auction", "The lots on this city's auction block and when the next auction is", "Only luxuries: Heaven-Sent treasures, Potential Pills, vintage goods. 'sell' puts yours on the block.");
        e("buy", "purchase", "trade", "buy <#> | buy <shop>.<item> <name> | buy market <#> [qty]", "Buy from the last 'list shop' (or the market)", "e.g.  list shop  then  buy 3");
        e("price", "", "trade", "price <shop#> buy|sell|city <rate>", "Set your shop's rates", "e.g.  price 1 sell 1.8");
        // ---- life
        e("tame", "", "life", "tame", "Tame a beast here as a companion", "");
        e("companions", "beasts", "life", "companions", "Your tamed beasts", "'feed <n>', 'release <n>'.");
        e("release", "", "life", "release <n>", "Send a companion back to the wild", "");
        e("feed", "", "life", "feed <n>", "Feed a companion", "");
        e("make", "", "life", "make food|wine|poison|instrument", "Craft goods from herbs (the right trait does it best)", "'cook' = make food, 'brew' = make wine.");
        e("cook", "", "life", "cook", "Make food", "");
        e("brew", "", "life", "brew", "Make wine", "");
        e("eat", "", "life", "eat [n]", "Eat some food", "");
        e("drink", "", "life", "drink [n]", "Drink some wine", "");
        e("poison", "", "life", "poison [person]", "Slip poison to someone (they will not forgive you if noticed)", "");
        e("coat", "", "life", "coat", "Coat your weapon with poison", "");
        e("play", "", "life", "play", "Play an instrument (inspires you)", "");
        e("reincarnate", "", "life", "reincarnate", "Be reborn when your rope ends", "");
        // ---- sect
        e("sect", "", "sect", "sect [name] | sect rules ...", "Your sect (or the one here), or any by name", "");
        e("join", "", "sect", "join", "Join the sect in this place", "");
        e("resign", "", "sect", "resign", "Leave your sect", "");
        e("recruit", "", "sect", "recruit [person]", "Recruit someone into the sect you lead", "");
        e("promote", "", "sect", "promote <person>", "Raise a member's rank", "");
        e("found", "", "sect", "found city|sect", "Found a city (Major Lvl 3) or a sect (Major Lvl 2)", "");
        e("war", "", "sect", "war", "Declare war on the sect whose home you stand in", "");
        e("peace", "", "sect", "peace", "End your sect's war", "");
        e("alliance", "", "sect", "alliance", "Propose an alliance to the sect here", "");
        // ---- auto
        e("auto", "", "auto", "auto [turns]", "Let the character run itself like an NPC", "'stop' takes control back.");
        e("manual", "stop", "auto", "manual | stop", "Take control back (also stops a running script)", "");
        e("script", "", "auto", "script add <step> | list | del <n> | clear | run | resume | stop", "A list of steps the character carries out by itself", "Steps: goto wait forage mine craft meditate hunt sell quest repeat if end mode.");
        e("duties", "", "auto", "duties | duties all on|off | duties <name> on|off", "Choose which kinds of work the game does for you", "");
        e("policy", "", "auto", "policy [kill|flee|forbidden|duel|script|alerts|timeout <value>]", "How the game answers prompts when you are not at the keyboard", "");
        e("watch", "", "auto", "watch [weeks] [for <categories>] [until year <n>]", "Let time pass until something happens to you", "e.g.  watch 100");
        e("pause", "p", "auto", "p", "Pause or resume the clock (GUI)", "");
        // ---- system
        e("save", "f", "system", "save [name]", "Save the game (quick slot when no name)", "'saves' lists, 'load <name>' loads, 'delsave <name>' deletes.");
        e("saves", "", "system", "saves", "List saved games", "");
        e("load", "", "system", "load <name>", "Load a save", "");
        e("delsave", "", "system", "delsave <name>", "Delete a save", "");
        e("quit", "exit bye", "system", "quit [nosave]", "Save and leave the game", "");
        e("export", "", "system", "export history|rankings|people|monsters|shop|bag|places|world|run", "Write a table to a CSV file", "");
        e("settings", "options config prefs", "system", "settings [key value]", "Show or change settings",
            "Keys: autosave autosavekeep confirm hints hud autoequip maturestart challenges music volume highcontrast tooltips toasts sound pictures art artfresh colorblind font recap notify.\ne.g.  settings hud off");
        e("music", "", "system", "music [on|off|new|volume <n>|...]", "Ambient music", "");
        e("tutorial", "", "system", "tutorial", "The new-player tutorial", "");
        e("help", "? commands", "system", "help [command|category|all]", "This help", "e.g.  help quests   help qdo   help all");
        e("last", "again !!", "system", "last   (also: again, !!)", "Repeat the previous command", "");
        e("cmds", "", "system", "cmds [n]", "The commands you typed lately (type 'last' to repeat one)", "");
        e("hud", "", "system", "hud [on|off]", "Show or hide the status line before each prompt", "Same as 'settings hud on|off'.");
        e("e", "", "system", "e", "Edit your character (name, trait, potential ...)", "");
        e("z", "cheat", "system", "z | cheat <self|#id> <what> ...", "Cheat menu", "");
    }

    // ------------------------------------------------------------------ lookup
    /** Names and aliases that must never be run by a mere prefix or a typo: the user has to type them in full. */
    static final java.util.Set<String> NO_AUTO = new java.util.HashSet<>(java.util.Arrays.asList(
            "quit", "exit", "resign", "retire", "delsave", "disband", "war", "peace", "alliance", "release", "load", "cheat", "z", "e", "attack", "poison",
            "found", "reincarnate", "control", "mute", "qd", "fight", "bye", "install", "coat", "sell", "gift", "consume"));

    private static Map<String, Cmd> index;

    /** word -> command (names and aliases). */
    static synchronized Map<String, Cmd> index() {
        if (index == null) {
            index = new LinkedHashMap<>();
            for (Cmd c : CMDS) index.put(c.name, c);
            for (Cmd c : CMDS) for (String a : c.aliases) if (!index.containsKey(a)) index.put(a, c);
        }
        return index;
    }

    static Cmd find(String word) { return word == null ? null : index().get(word.toLowerCase()); }

    static String[] category(String key) {
        String k = key.toLowerCase();
        if (k.equals("info")) k = "look";
        if (k.equals("items") || k.equals("equipment")) k = "gear";
        if (k.equals("shopping") || k.equals("shop") || k.equals("economy")) k = "trade";
        if (k.equals("social") || k.equals("lists") || k.equals("pick")) k = "people";
        if (k.equals("modes") || k.equals("automation") || k.equals("autopilot")) k = "auto";
        if (k.equals("roles") || k.equals("sects")) k = "sect";
        if (k.equals("life arts") || k.equals("arts")) k = "life";
        if (k.equals("basics") || k.equals("movement") || k.equals("moving")) k = "move";
        for (String[] c : CATS) if (c[0].equals(k) || c[1].equalsIgnoreCase(key)) return c;
        return null;
    }

    private static String shortNames(String cat) {
        StringBuilder sb = new StringBuilder();
        for (Cmd c : CMDS) if (c.cat.equals(cat)) { if (sb.length() > 0) sb.append(' '); sb.append(c.name.equals("w") ? "w a s d nw ne sw se" : c.name); }
        return sb.toString();
    }

    // ------------------------------------------------------------------ the help screens
    /** `help` with no argument: categories with a one-line description each. */
    static String overview() {
        StringBuilder sb = new StringBuilder("=== Commands ===\n");
        sb.append("Just starting?  q (what to do)   people (who is here)   here   i (your sheet)   x (let a week pass)   help <command>\n\n");
        for (String[] c : CATS) {
            sb.append(String.format(" %-10s %s%n", c[1], c[2]));
            sb.append("            ").append(shortNames(c[0])).append("\n");
        }
        sb.append("\n  help <category>   e.g. help quests / help trade        help <command>   e.g. help qdo        help all   the full reference\n");
        sb.append("  Shortcuts: a prefix is enough when it is unambiguous (inv = bag); 'last' or '!!' repeats the previous command; 'cmds' lists them;\n");
        sb.append("  'settings hud off' hides the status line, 'settings hints off' the tips. Mistyped a command? You are told what it might have been.\n");
        return sb.toString();
    }

    static String categoryText(String[] cat) {
        StringBuilder sb = new StringBuilder("=== help " + cat[0] + " ===\n");
        sb.append(cat[1]).append(": ").append(cat[2]).append("\n");
        int w = 0;
        for (Cmd c : CMDS) if (c.cat.equals(cat[0])) w = Math.max(w, firstLine(c.usage).length());
        w = Math.min(w, 46);
        for (Cmd c : CMDS) if (c.cat.equals(cat[0])) sb.append("  ").append(TermFormat.pad(firstLine(c.usage), w)).append("  ").append(c.summary).append("\n");
        sb.append("  (help <command> shows details, aliases and examples)\n");
        return sb.toString();
    }

    private static String firstLine(String s) { int i = s.indexOf('\n'); return i < 0 ? s : s.substring(0, i); }

    static String commandText(Cmd c) {
        StringBuilder sb = new StringBuilder("=== help " + c.name + " ===\n");
        sb.append(c.usage).append("\n  ").append(c.summary).append("\n");
        if (!c.details.isEmpty()) for (String l : c.details.split("\n")) sb.append("  ").append(l).append("\n");
        if (c.aliases.length > 0) sb.append("  Also: ").append(String.join(", ", c.aliases)).append("\n");
        sb.append("  Group: ").append(category(c.cat)[1]).append("  (help ").append(c.cat).append(")\n");
        return sb.toString();
    }

    /** `help all`: the complete reference (the original one-line-per-topic list) plus the shortcut list. */
    static String all() {
        StringBuilder sb = new StringBuilder(PlayerCommands.HELP);
        sb.append("\nShort forms (aliases) work everywhere: ");
        boolean first = true;
        for (Cmd c : CMDS) if (c.aliases.length > 0) { sb.append(first ? "" : "; ").append(c.name).append(" = ").append(String.join("/", c.aliases)); first = false; }
        sb.append("\nAlso: last / again / !! repeat the previous command, cmds lists what you typed, hud on|off, any unambiguous prefix works (inv = bag).\n");
        return sb.toString();
    }

    /** `help ...`: prints the matching screen (in the GUI an info window). */
    static void help(String[] t) {
        if (t.length < 2) { GameDriver.showGuiInfo("Commands", overview()); return; }
        String word = String.join(" ", java.util.Arrays.copyOfRange(t, 1, t.length)).trim();
        String w = word.toLowerCase();
        if (w.equals("all") || w.equals("full") || w.equals("everything")) { GameDriver.showGuiInfo("Commands (all)", all()); return; }
        for (String[] topic : TextTools.TOPICS) if (topic[0].equals(w)) { TextTools.show(TextTools.helpTopic(w)); return; } // the long hand-written topics (map, route, watch ...)
        String[] cat = category(w);
        if (cat != null) { GameDriver.showGuiInfo("Help: " + cat[1], categoryText(cat)); return; }
        Cmd c = find(w.split("\\s+")[0]);
        if (c != null) { GameDriver.showGuiInfo("Help: " + c.name, commandText(c)); return; }
        String old = TextTools.helpTopic(w); // section labels of the full list (duties, policy, seek ...)
        if (!old.startsWith("No help topic")) { TextTools.show(old); return; }
        StringBuilder sb = new StringBuilder("No help topic '" + word + "'.");
        List<String> sug = suggest(w.split("\\s+")[0], 4);
        if (!sug.isEmpty()) sb.append(" Did you mean: ").append(String.join(", ", sug)).append("?");
        sb.append("\nCategories: ");
        for (int i = 0; i < CATS.length; i++) sb.append(i > 0 ? ", " : "").append(CATS[i][0]);
        sb.append(".  Try 'help' (overview) or 'help all'.");
        PlayerCommands.say(sb.toString());
    }

    // ------------------------------------------------------------------ unknown words
    /** Likely meanings of an unknown word, best first: prefix matches, then small edit distance (typos, swapped letters), then substrings.
     *  Each entry is the command name, or "alias (name)" when the user nearly typed an alias. */
    static List<String> suggest(String word, int max) {
        String w = word.toLowerCase();
        List<String> out = new ArrayList<>();
        if (w.isEmpty()) return out;
        int allowed = w.length() <= 2 ? 0 : w.length() <= 4 ? 1 : 2;
        java.util.HashMap<String, Integer> score = new java.util.HashMap<>();
        java.util.HashMap<String, String> shown = new java.util.HashMap<>();
        for (Map.Entry<String, Cmd> en : index().entrySet()) {
            String k = en.getKey(), name = en.getValue().name;
            int sc = -1;
            if (k.startsWith(w)) sc = 0;
            else if (w.length() >= 3 && k.length() >= 3 && TermFormat.distance(w, k) <= allowed) sc = TermFormat.distance(w, k);
            else if (w.length() >= 3 && k.contains(w)) sc = 3;
            if (sc < 0) continue;
            Integer old = score.get(name);
            if (old == null || sc < old || (sc == old && k.equals(name))) { score.put(name, sc); shown.put(name, k.equals(name) ? name : k + " (" + name + ")"); }
        }
        List<String> names = new ArrayList<>(score.keySet());
        names.sort((a, b) -> score.get(a).equals(score.get(b)) ? a.compareTo(b) : Integer.compare(score.get(a), score.get(b)));
        for (String n : names) out.add(shown.get(n));
        return out.size() > max ? new ArrayList<>(out.subList(0, max)) : out;
    }

    /** The command a word stands for when it is not a command the switch knows: an alias, or a prefix that only one command starts with
     *  (null if none, ambiguous, or too dangerous to guess). */
    static String expand(String word) {
        String w = word.toLowerCase();
        Cmd exact = index().get(w);
        if (exact != null && !exact.name.equals(w)) return NO_AUTO.contains(w) || NO_AUTO.contains(exact.name) ? null : exact.name; // alias
        if (w.length() < 2) return null;
        TreeSet<String> hits = new TreeSet<>();
        for (Map.Entry<String, Cmd> en : index().entrySet()) if (en.getKey().startsWith(w)) hits.add(en.getValue().name);
        if (hits.size() == 1 && !NO_AUTO.contains(hits.first())) return hits.first();
        return null;
    }

    /** True for the words that name more than one command by prefix (so we can say so). */
    static List<String> prefixHits(String word) {
        String w = word.toLowerCase();
        TreeSet<String> hits = new TreeSet<>();
        for (Map.Entry<String, Cmd> en : index().entrySet()) if (en.getKey().startsWith(w)) hits.add(en.getValue().name);
        return new ArrayList<>(hits);
    }
}
