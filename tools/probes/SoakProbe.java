package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-18 (long-run bug check): a soak test of a whole world, in four modes, with an audit of the world every few game years.
 *
 *    java -Xss512m -Xmx4g -Dtdf.verify=true -Dsoak.mode=monkey -Dsoak.years=150 -Dsoak.seed=1 -cp <out>;<flatlaf> Game.SoakProbe
 *
 *  soak.mode:  watch   - a spectator (no person who plays), the NPC world only
 *              auto    - a person who plays in AUTO mode (the character lives like an NPC)
 *              hybrid  - HYBRID: a small saved script is run again and again
 *              monkey  - MANUAL: a beta tester who types commands at random. Each command is built from the usage text of the game's own help (`TermHelp.CMDS`), so every documented command is tried with plausible
 *                        and with silly arguments (negative, huge, unknown ids), in whatever state the character is in; the character is also made to do the ordinary things (hunt, sell, craft, save, load).
 *  What it looks for: exceptions anywhere in the output, a "[command error]" line (a command that crashed and was caught), a thread that died, a command that spends more than a year, a world whose numbers are
 *  out of range (the audit: negative money, levels, potential, empty techniques, gear whose grade does not fit its level, items with no quantity, an item worn and carried at once, dead owners, duplicate
 *  quest ids), the heap, and (with -Dtdf.verify=true) the game's own Verify of every tick. Every kind of violation is counted with two examples. Prints [S] lines and a [T] ===== summary. */
public class SoakProbe {
    static final Random R = new Random();
    static PrintStream real;
    static String mode;
    static int years;
    static final java.util.Map<String, int[]> counts = new TreeMap<>();            // kind -> {count}
    static final java.util.Map<String, List<String>> examples = new TreeMap<>();
    static final java.util.Map<String, int[]> cmdUse = new TreeMap<>();           // command word -> {times, weeks, errors, silent}
    static int exceptions = 0, commandErrors = 0, commands = 0;
    static final Set<String> exSeen = new LinkedHashSet<>();
    static long t0 = System.currentTimeMillis();

    static void note(String kind, String example) {
        int[] c = counts.get(kind); if (c == null) counts.put(kind, c = new int[1]);
        c[0]++;
        List<String> ex = examples.get(kind); if (ex == null) examples.put(kind, ex = new ArrayList<>());
        if (ex.size() < 3) ex.add(example);
    }

    static String yr(int t) { return "Y" + (t / 52) + "." + String.format("%02d", t % 52); }

    // ---------------------------------------------------------------- scanning the output
    static void scan(String out, String after) {
        for (String line : out.split("\n")) {
            if (line.contains("[command error]")) { commandErrors++; note("command error", yr(GameDriver.time) + " after '" + after + "': " + line.trim()); }
            else if (line.contains("Exception") || line.contains("\tat Game.") || line.startsWith("at Game.") || line.contains("java.lang.")) {
                exceptions++;
                String key = line.trim();
                if (key.length() > 160) key = key.substring(0, 160);
                if (exSeen.add(key) && exSeen.size() < 40) note("exception in output", yr(GameDriver.time) + " after '" + after + "': " + key);
            }
        }
    }

    // ---------------------------------------------------------------- the audit
    static void audit() {
        final boolean[] ok = {false};
        for (int tries = 0; tries < 20 && !ok[0]; tries++) ok[0] = World.read(3000, SoakProbe::auditNow);
        if (!ok[0]) note("audit could not get the world", yr(GameDriver.time));
    }

    static int alive, beasts, monsters, cultivators, maxMajor, citiesN, sectsN;
    static long totalSS;

    static java.util.Map<Area, Character> deadOwnerBefore = new java.util.HashMap<>(), deadOwnerNow = new java.util.HashMap<>();   // city -> the dead owner it had (the same one two audits running is the violation; a new lord who also died is not)

    static void auditNow() {
        deadOwnerNow = new java.util.HashMap<>();
        alive = 0; beasts = 0; monsters = 0; cultivators = 0; maxMajor = 0; totalSS = 0; citiesN = 0; sectsN = 0;
        ArrayList<Character> all = new ArrayList<>(Map.characters);
        for (Character c : all) {
            if (c == null || c.dead || !c.born) continue;
            alive++;
            try { auditOne(c); } catch (RuntimeException e) { note("audit threw on a person", c.name + " #" + c.id + " " + e); }
        }
        for (Area a : new ArrayList<>(Map.createdAreas)) {
            if (a == null) continue;
            try {
                if (a.city) { citiesN++; if (a.cityOwner != null && a.cityOwner.dead) { if (deadOwnerBefore.get(a) == a.cityOwner) note("city whose owner has been dead for two audits", a.name); deadOwnerNow.put(a, a.cityOwner); } if (a.population < 0) note("negative city population", a.name); if (a.cityTreasury < 0) note("negative city treasury", a.name); }
                if (a.sect != null) { sectsN++; }
                if (a.shops != null) for (Shop s : new ArrayList<>(a.shops)) {
                    if (s == null) { note("null shop", a.name); continue; }
                    for (Item it : new ArrayList<>(s.inventory)) if (it == null) note("null item in a shop", a.name); else if (it.quantity < 1) note("shop item with no quantity", a.name + ": " + it.name);
                }
                for (Item it : new ArrayList<>(a.backpack)) if (it == null) note("null item on the ground", a.name);
            } catch (RuntimeException e) { note("audit threw on a tile", a.name + " " + e); }
        }
        deadOwnerBefore = deadOwnerNow;
    }

    static void auditOne(Character c) {
        String who = c.name + " " + c.lastName + " #" + c.id + " (" + c.trait + ")";
        if (c.trait == Character.Traits.Beast) { beasts++; return; }
        if (c.trait == Character.Traits.Monster) monsters++; else cultivators++;
        totalSS += c.ss;
        if (c.ss < 0) note("negative spirit stones", who + " " + c.ss);
        if (c.sc < 0 || c.dc < 0) note("negative copper/silver", who + " sc=" + c.sc + " dc=" + c.dc);
        if (c.majorLvl < 0 || c.majorLvl > 10) note("major level out of range", who + " " + c.majorLvl);
        if (c.majorLvl > maxMajor) maxMajor = c.majorLvl;
        if (c.lvl < 0 || c.lvl > 105) note("level out of range", who + " " + c.lvl);
        if (c.potential < 0 || c.potential > 300) note("potential out of range", who + " " + c.potential);
        if (c.age < 0 || c.age > 100000) note("age out of range", who + " " + c.age);
        if (c.location == null) note("living person with no location", who);
        if (c.martialTechnique == null || c.movementTechnique == null) note("no first technique", who);
        else {
            if (c.martialTechnique.type != Technique.techniqueType.Martial) note("martial slot holds a movement technique", who);
            if (c.movementTechnique.type != Technique.techniqueType.Movement) note("movement slot holds a martial technique", who);
        }
        if (c.martialTechnique2 != null && c.martialTechnique2.type != Technique.techniqueType.Martial) note("martial 2 holds a movement technique", who);
        if (c.movementTechnique2 != null && c.movementTechnique2.type != Technique.techniqueType.Movement) note("movement 2 holds a martial technique", who);
        if (c.martialTechnique != null && c.martialTechnique == c.martialTechnique2) note("same technique in both martial slots", who);
        if (c.movementTechnique != null && c.movementTechnique == c.movementTechnique2) note("same technique in both movement slots", who);
        Technique[] ts = {c.martialTechnique, c.martialTechnique2, c.movementTechnique, c.movementTechnique2};
        for (Technique t : ts) if (t != null && (t.lvl > t.potential || t.lvl < 0 || t.potential < 1)) note("technique level above its potential", who + " " + t.name + " " + t.lvl + "/" + t.potential);
        if (c.techniqueList.size() > 40) note("known-techniques list over 40 entries", who + " " + c.techniqueList.size());
        { IdentityHashMap<Technique, Boolean> seenT = new IdentityHashMap<>(); for (Technique t : new ArrayList<>(c.techniqueList)) if (t != null && seenT.put(t, Boolean.TRUE) != null) { note("the same technique twice in the known list", who + " " + t.name + " (list of " + c.techniqueList.size() + ")"); break; } }
        for (Technique t : new ArrayList<>(c.techniqueList)) if (t == null) note("null technique in the known list", who); else if (t == c.martialTechnique || t == c.martialTechnique2 || t == c.movementTechnique || t == c.movementTechnique2) note("a worn technique is also in the known list", who + " " + t.name);
        gear(c.equippedWeapon, who, "weapon"); gear(c.equippedClothing, who, "clothing");
        List<Item> bag = new ArrayList<>(c.backpack);
        for (Item it : bag) {
            if (it == null) { note("null item in a backpack", who); continue; }
            if (it == c.equippedWeapon || it == c.equippedClothing) note("an item is worn and carried", who + " " + it.name);
            if (it.quantity < 1) note("backpack item with no quantity", who + " " + it.name + " x" + it.quantity);
            gear(it, who, "carried gear");
        }
        for (Item it : new ArrayList<>(c.materialBag)) if (it == null || it.quantity < 1) note("material stack with no quantity", who + (it == null ? " null" : " " + it.name));
        for (Item it : new ArrayList<>(c.pillBag)) if (it == null || it.quantity < 1) note("pill stack with no quantity", who + (it == null ? " null" : " " + it.name));
        if (c.player >= 1) {
            Set<Integer> uids = new HashSet<>();
            List<Quest> qs = new ArrayList<>(c.targetQuests); qs.addAll(c.spotQuests); if (c.currentQuest != null) qs.add(c.currentQuest);
            for (Quest q : qs) { if (q == null) { note("null quest", who); continue; } if (q.uid != 0 && !uids.add(q.uid)) note("two quests with the same uid", who + " uid " + q.uid + " " + q.name); }
        }
    }

    static void gear(Item it, String who, String what) {
        if (it == null) return;
        boolean isGear = it.itemType == Item.ItemType.Weapon || it.itemType == Item.ItemType.Clothing || it.itemType == Item.ItemType.Array;
        if (!isGear) return;
        if (it.lvl < 1 || it.lvl > 100) note("gear level out of 1-100", who + " " + it.name + " " + it.lvl);
        if (it.name != null && it.name.contains("-Grade ")) {
            String want = Naming.grade(Naming.gearRealm(it.lvl)) + "-Grade ";
            if (!it.name.contains(want)) note("gear whose grade does not fit its level", who + " " + what + ": " + it.name + " (level " + it.lvl + ")");
        }
    }

    // ---------------------------------------------------------------- the report lines
    static long lastGc = 0;
    static String heap() {
        Runtime rt = Runtime.getRuntime();
        if (System.currentTimeMillis() - lastGc > 60000) { System.gc(); lastGc = System.currentTimeMillis(); }
        return ((rt.totalMemory() - rt.freeMemory()) >> 20) + "MB/" + (rt.maxMemory() >> 20) + "MB";
    }

    static void line(String extra) {
        int viol = 0; for (int[] c : counts.values()) viol += c[0];
        real.println("[S] " + yr(GameDriver.time) + " min=" + (System.currentTimeMillis() - t0) / 60000 + " alive=" + alive + " cult=" + cultivators + " beasts=" + beasts + " monsters=" + monsters + " cities=" + citiesN + " sects=" + sectsN + " maxMajor=" + maxMajor
                + " ss=" + totalSS + " heap=" + heap() + " cmds=" + commands + " cmdErr=" + commandErrors + " ex=" + exceptions + " violations=" + viol + (extra == null ? "" : " " + extra));
    }

    // ---------------------------------------------------------------- the monkey
    static final Set<String> EXCLUDE = new HashSet<>(Arrays.asList("quit", "exit", "bye", "load", "delsave", "control", "release", "e", "z", "cheat", "o", "music", "p", "pause", "reincarnate", "tutorial",
            "retire", "auto", "hybrid", "manual", "script", "save", "saves", "export", "resign", "disband", "leave", "lore", "story", "settings", "set", "policy", "duties", "hud", "watch", "new", "stop"));
    static final String[] BIGS = {"0", "-1", "-5", "99999999999", "2147483647", "-2147483649", "1.5", "0x10", "00012", "99999999999999999999999"};
    static final String[] WORDS = {"abc", "friend", "fight", "on", "off", "all", "weapon", "armor", "food", "wine", "poison", "city", "sect", "x", "-", "?", "*", "%s", "'", "\"", "\\", "null", "#", "#abc", "#-1", "..", "Hunt Beast", "雲"};

    static Character me() { return PcProbe.pc(); }

    static String person(Character p) {
        List<Character> here = new ArrayList<>();
        for (Character c : new ArrayList<>(p.location.characters)) if (c != null && c != p && !c.dead && c.born) here.add(c);
        if (!here.isEmpty() && R.nextInt(5) != 0) return "#" + here.get(R.nextInt(here.size())).id;
        int k = R.nextInt(8);
        if (k == 0) return "#" + R.nextInt(Math.max(1, Map.characters.size()));
        if (k == 1) return BIGS[R.nextInt(BIGS.length)];
        return String.valueOf(R.nextInt(8));
    }

    static String fill(String ph, Character p, boolean timed) {
        String l = ph.toLowerCase();
        if (R.nextInt(12) == 0) return BIGS[R.nextInt(BIGS.length)];
        if (R.nextInt(25) == 0) return WORDS[R.nextInt(WORDS.length)];
        if (l.contains("person") || l.contains("#id") || l.contains("who") || l.contains("target") || l.contains("npc") || l.contains("friend")) return person(p);
        if (l.equals("x")) return String.valueOf(Math.max(0, Math.min(Map.range - 1, p.location.x + R.nextInt(5) - 2)));
        if (l.equals("y")) return String.valueOf(Math.max(0, Math.min(Map.range - 1, p.location.y + R.nextInt(5) - 2)));
        if (l.contains("week") || l.contains("turn") || l.contains("year")) return String.valueOf(1 + R.nextInt(timed ? 6 : 30));
        if (l.contains("slot")) return String.valueOf(1 + R.nextInt(2));
        if (l.contains("n") || l.contains("num") || l.contains("#") || l.contains("level") || l.contains("amount") || l.contains("count") || l.contains("price") || l.contains("limit")) return String.valueOf(R.nextInt(timed ? 6 : 12));
        return WORDS[R.nextInt(WORDS.length)];
    }

    static boolean timedVerb(String name) {
        switch (name) { case "wait": case "walk": case "watch": case "goto": case "gq": case "goquest": case "chase": case "route": case "go": case "travel": case "x": case "stay": case "w": case "a": case "s": case "d": case "meditate": case "rest": case "hunt": case "explore": return true; default: return false; }
    }

    /** One random command built from a help entry's usage line. */
    static String generate(Character p, TermHelp.Cmd c) {
        String usage = c.usage == null ? c.name : c.usage;
        String[] forms = usage.split(" \\| (?=[a-z])");   // " | " between whole forms; "a|b" inside a word is an alternative
        String form = forms[R.nextInt(forms.length)].trim();
        List<String> toks = new ArrayList<>(Arrays.asList(form.split("\\s+")));
        if (toks.isEmpty()) return c.name;
        StringBuilder sb = new StringBuilder();
        boolean timed = timedVerb(c.name);
        String first = toks.get(0);
        sb.append(first.startsWith("<") || first.startsWith("[") ? c.name : first.split("\\|")[0]);
        for (int i = 1; i < toks.size(); i++) {
            String t = toks.get(i);
            boolean optional = t.startsWith("[");
            if (optional && R.nextInt(2) == 0) continue;
            t = t.replaceAll("^[\\[<]+|[\\]>]+$", "");
            if (t.contains("|")) { String[] alts = t.split("\\|"); if (alts.length == 0) continue; t = alts[R.nextInt(alts.length)]; if (t.startsWith("<") || t.startsWith("[")) t = t.replaceAll("^[\\[<]+|[\\]>]+$", ""); }
            if (t.matches("[a-z]+") && !t.equals("n") && !t.equals("x") && !t.equals("y") && !t.equals("id")) {
                // a literal word of the grammar (on, off, martial ...) or a placeholder word: placeholders are filled, words kept
                if (isPlaceholderWord(t)) sb.append(' ').append(fill(t, p, timed)); else sb.append(' ').append(t);
            } else sb.append(' ').append(fill(t, p, timed));
        }
        return sb.toString();
    }

    static boolean isPlaceholderWord(String t) {
        switch (t) { case "person": case "name": case "words": case "text": case "place": case "step": case "weeks": case "turns": case "level": case "amount": case "item": case "bag": case "slot": case "quest": case "type": case "kind": case "target": case "limit": return true; default: return false; }
    }

    /** The ordinary things a player does, so the character keeps living while the random commands run. */
    static String routine(Character p) {
        int k = R.nextInt(14);
        switch (k) {
            case 0: return "meditate";
            case 1: return "wait " + (1 + R.nextInt(4));
            case 2: return p.location.monsters > 0 ? "hunt" : "meditate";
            case 3: return p.location.city ? "sell" : "forage";
            case 4: return p.location.city ? "buygear" : "mine";
            case 5: return "craft";
            case 6: return "consume";
            case 7: return "equip";
            case 8: return "eat";
            case 9: return p.breakthroughReady ? "breakthrough" : "meditate";
            case 10: { Area a = randomPlace(p); return a == null ? "wait 1" : "goto " + a.x + " " + a.y; }
            case 11: return "techniques";
            case 12: return "x";
            default: return "meditate";
        }
    }

    static Area randomPlace(Character p) {
        List<Area> known = new ArrayList<>(); for (Area a : new ArrayList<>(p.knownLocations)) if (a != null && a != p.location) known.add(a);
        return known.isEmpty() ? null : known.get(R.nextInt(known.size()));
    }

    static void exec(String cmd) throws Exception {
        Character p = me();
        int t0 = GameDriver.time;
        PcProbe.cap.reset();
        PcProbe.cmd(cmd, 240);
        String out = PcProbe.out();
        PcProbe.cap.reset();
        commands++;
        scan(out, cmd);
        int dt = GameDriver.time - t0;
        String w = cmd.contains(" ") ? cmd.substring(0, cmd.indexOf(' ')) : cmd;
        int[] u = cmdUse.get(w); if (u == null) cmdUse.put(w, u = new int[4]);
        u[0]++; u[1] += dt; if (out.contains("[command error]")) u[2]++; if (out.trim().isEmpty()) u[3]++;
        if (dt > 60) note("a command that spent more than 60 weeks", yr(GameDriver.time) + " '" + cmd + "' " + dt + " weeks");
        if (dt < 0) note("time went backwards", cmd);
        if (PcProbe.gameError != null) note("game thread died", String.valueOf(PcProbe.gameError));
        Character q = me();
        if (q != null && q == p && !q.dead && q.pc() != null && q.pc().mode != PlayerControl.Mode.MANUAL) { PcProbe.cmd("stop", 60); }
    }

    static void monkey() throws Exception {
        boolean loaded = Boolean.getBoolean("soak.load"); // -Dsoak.load=true: continue the newest save in saves/ of the run folder (a copy of a real one) instead of starting a new life
        PcProbe.in.feed(loaded ? "n\n1\n" : Boolean.getBoolean("soak.pop") ? "n\nx\ns\n1\nn\n" : "n\ns\n1\nn\n"); // -Dsoak.pop=true: the build asks the size of the world first (the real 100,000 build does; the 3,000 copies do not)
        PcProbe.awaitBlocked(loaded ? 900 : 300);
        if (loaded) for (String l : PcProbe.out().split("\n")) if (l.startsWith("[Gear]") || l.startsWith("[LOAD") || l.contains("Resuming")) real.println("[S] startup: " + l.trim());
        Character p = me();
        if (p == null) {
            note("no character after the start", "");
            String o = PcProbe.out(); String[] ls = o.split("\n"); StringBuilder tail = new StringBuilder();
            for (int i = Math.max(0, ls.length - 25); i < ls.length; i++) tail.append("    | ").append(ls[i].length() > 200 ? ls[i].substring(0, 200) : ls[i]).append("\n");
            int humans = 0, deadHumans = 0, follow = 0; for (Character c : new ArrayList<>(Map.characters)) if (c != null && c.player >= 1) { if (c.player == 2) follow++; if (c.dead) deadHumans++; else humans++; }
            real.println("[S] no player found: time=" + GameDriver.time + " blocked=" + PcProbe.in.blocked + " alive humans=" + humans + " dead humans=" + deadHumans + " followed=" + follow + "\n" + tail);
            return;
        }
        real.println("[S] started " + p.name + " " + p.lastName + " " + p.getTrait() + " pot " + p.potential + " at " + p.location.name);
        PcProbe.cmd("duties all off", 60); PcProbe.cmd("policy breakthrough button", 60); PcProbe.cmd("settings hud off", 60); PcProbe.cmd("settings dialogue off", 60);
        List<TermHelp.Cmd> cmds = new ArrayList<>();
        for (TermHelp.Cmd c : TermHelp.CMDS) if (!EXCLUDE.contains(c.name)) cmds.add(c);
        real.println("[S] the monkey knows " + cmds.size() + " commands of " + TermHelp.CMDS.size());
        final int end = GameDriver.time + years * 52;
        int nextAudit = GameDriver.time + 52 * 2, nextLine = GameDriver.time + 52 * 5, saves = 0, turn = 0;
        long deadline = t0 + Long.getLong("soak.minutes", 150L) * 60000L;
        while (GameDriver.time < end && System.currentTimeMillis() < deadline) {
            p = me();
            if (p == null || p.dead) { real.println("[S] the character died at " + yr(GameDriver.time) + ": " + (p == null ? "gone" : p.deathCause)); break; }
            if (PcProbe.gameError != null) { note("game thread died", String.valueOf(PcProbe.gameError)); break; }
            turn++;
            String cmd;
            int k = R.nextInt(100);
            if (k < 45) cmd = routine(p);
            else if (k < 97) cmd = generate(p, cmds.get(R.nextInt(cmds.size())));
            else if (k < 99) { cmd = "save soak" + (saves++ % 3); }
            else { cmd = "settings " + new String[]{"hud", "dialogue", "vignettes", "lessons", "fightview", "worldnews", "toasts"}[R.nextInt(7)] + " " + (R.nextBoolean() ? "on" : "off"); }
            exec(cmd);
            if (k >= 99 && R.nextInt(4) == 0) { exec("duties " + PlayerControl.DUTY_NAMES[R.nextInt(PlayerControl.DUTY_NAMES.length)] + " " + (R.nextBoolean() ? "on" : "off")); }
            if (turn % 60 == 0) { Character q = me(); if (q != null && !q.dead) exec("policy " + new String[]{"flee", "duel", "forbidden", "feed"}[R.nextInt(4)] + " " + new String[]{"on", "off"}[R.nextInt(2)]); }
            if (turn % 400 == 0 && saves > 0) { exec("load soak" + R.nextInt(Math.min(3, saves))); Character q = me(); if (q == null) note("no character after a load", ""); else { real.println("[S] loaded; " + q.name + " at " + yr(GameDriver.time)); audit(); } }
            if (GameDriver.time >= nextAudit) { nextAudit = GameDriver.time + 52 * 2; audit(); }
            if (GameDriver.time >= nextLine) { nextLine = GameDriver.time + 52 * 5; audit(); Character q = me(); line("age=" + (q == null ? "?" : q.age) + " major=" + (q == null ? "?" : q.majorLvl) + " lvl=" + (q == null ? "?" : q.lvl)); }
        }
    }

    // ---------------------------------------------------------------- the three modes without a typing monkey
    static void watching(boolean auto, boolean hybrid) throws Exception {
        if (auto || hybrid) { PcProbe.in.feed("n\ns\n1\nn\n"); PcProbe.awaitBlocked(300); } else { PcProbe.in.feed("n\nw\n"); Thread.sleep(4000); }
        Character p = me();
        if ((auto || hybrid) && p == null) { note("no character after the start", ""); return; }
        GameDriver.tickDelayMs = 0; GameDriver.paused = false;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        if (auto) PcProbe.in.feed("auto\n");
        if (hybrid) {
            PcProbe.cmd("duties all off", 60);
            PcProbe.cmd("script add meditate", 60); PcProbe.cmd("script add wait 3", 60); PcProbe.cmd("script add meditate", 60);
            PcProbe.in.feed("script run\n");
        }
        final int end = GameDriver.time + years * 52;
        int nextLine = GameDriver.time + 52 * 5, nextAudit = GameDriver.time + 52 * 2;
        long deadline = t0 + Long.getLong("soak.minutes", 150L) * 60000L;
        while (GameDriver.time < end && System.currentTimeMillis() < deadline) {
            Thread.sleep(500);
            String out = PcProbe.out(); PcProbe.cap.reset();
            scan(out, auto ? "(auto)" : hybrid ? "(hybrid)" : "(watch)");
            if (PcProbe.gameError != null) { note("game thread died", String.valueOf(PcProbe.gameError)); break; }
            if (hybrid && PcProbe.in.blocked) { PcProbe.in.feed("script run\n"); }
            if ((auto || hybrid)) { Character q = me(); if (q == null) { real.println("[S] the character is gone at " + yr(GameDriver.time)); break; } }
            if (GameDriver.time >= nextAudit) { nextAudit = GameDriver.time + 52 * 2; audit(); }
            if (GameDriver.time >= nextLine) { nextLine = GameDriver.time + 52 * 5; audit(); line(null); }
        }
    }

    public static void main(String[] a) throws Exception {
        mode = System.getProperty("soak.mode", "monkey");
        years = Integer.getInteger("soak.years", 100);
        long seed = Long.getLong("soak.seed", 1L);
        R.setSeed(seed);
        System.setProperty("tdf.seed", String.valueOf(seed));
        real = System.out;
        PcProbe.real = real;
        System.setIn(PcProbe.in);
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        try {
            if (mode.equals("monkey")) monkey(); else watching(mode.equals("auto"), mode.equals("hybrid"));
        } catch (Throwable e) { real.println("[T] PROBE EXCEPTION " + e); e.printStackTrace(real); note("probe exception", String.valueOf(e)); }
        audit();
        line("FINAL");
        real.println("[S] ===== mode " + mode + ", years " + (GameDriver.time / 52) + ", commands " + commands + " =====");
        for (java.util.Map.Entry<String, int[]> e : counts.entrySet()) real.println("[V] " + e.getValue()[0] + "x " + e.getKey() + "  e.g. " + examples.get(e.getKey()));
        if (!cmdUse.isEmpty()) {
            StringBuilder sb = new StringBuilder("[U] commands used (times/weeks/errors/silent):");
            for (java.util.Map.Entry<String, int[]> e : cmdUse.entrySet()) { int[] u = e.getValue(); sb.append(' ').append(e.getKey()).append('=').append(u[0]).append('/').append(u[1]).append('/').append(u[2]).append('/').append(u[3]); }
            real.println(sb);
            for (java.util.Map.Entry<String, int[]> e : cmdUse.entrySet()) if (e.getValue()[2] > 0) real.println("[U] ERRORS " + e.getKey() + " " + e.getValue()[2] + " of " + e.getValue()[0]);
        }
        int fails = counts.size();
        real.println("[T] ===== " + (fails == 0 ? 1 : 0) + " passed, " + fails + " kinds of violation; commandErrors=" + commandErrors + " exceptions=" + exceptions + " gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }
}
