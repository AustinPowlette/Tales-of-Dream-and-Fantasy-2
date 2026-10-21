package Game;

import java.io.PrintStream;
import java.util.*;
import java.util.function.Predicate;

/** 2026-10-18 (test T4 of the control audit): one Story start played for N game years by a diligent person in MANUAL mode, with the commands and windows a person has (no cheats, no state edits): the bot reads the
 *  story's task (the Story window / the pinned quest), walks, chases, fights, gathers, crafts, hunts and cultivates, and the report says which chapters opened and closed and when, where it got stuck, which
 *  results were silent, which prompts appeared, whether it died and any exception. Run: `java -Xss512m -cp <out> Game.StoryJourney <story 1-10 or id> <years> [seed]` from an empty folder, in a 3,000-person
 *  copy of the sources. The log goes to `journey-<id>.log`, the summary to the console lines starting `[T]`. */
public class StoryJourney {
    static { Character.TRIGGER_EVERY = 1; }
    static PrintStream log;
    static String storyId;
    static int years = 60;

    static void say(String s) { PcProbe.real.println("[T] " + s); if (log != null) log.println(s); }

    // ---------------------------------------------------------------- report
    static final List<String> chapterLog = new ArrayList<>();
    static final List<String> problems = new ArrayList<>();
    static final List<String> silent = new ArrayList<>();
    static final LinkedHashMap<String, Integer> promptCounts = new LinkedHashMap<>();
    static final LinkedHashMap<String, Integer> cmdCounts = new LinkedHashMap<>(), cmdWeeks = new LinkedHashMap<>();
    static final List<String> scenes = new ArrayList<>();
    static final List<String> events = new ArrayList<>();
    static int exceptions = 0;
    static final LinkedHashMap<String, Integer> extras = new LinkedHashMap<>();   // lessons, little stories and favors that came to the person (only counted when -Djourney.extras=1)

    static String yr(int t) { return String.format("Y%d.%02d", t / 52, t % 52); }

    /** The answers of a careful person: fight a challenger only when the odds are good, try to get away from a stronger one; everything else gets the game's own default (the policy answer). */
    static volatile boolean committed = false;   // the bot itself began this fight (a story foe): it does not flee from it
    static final class Careful extends ArrayDeque<Integer> {
        @Override public Integer poll() {
            List<String> log = DecisionBroker.promptLog;
            if (log.isEmpty()) return null;
            String last = log.get(log.size() - 1);
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("Win chance ~(\\d+)%").matcher(last);
            int chance = m.find() ? Integer.parseInt(m.group(1)) : -1;
            if (last.contains("challenges you!") && chance >= 0) return chance >= 60 ? 0 : 1;   // Fight / Slip away
            if (last.contains("Try to flee?") && chance >= 0) return committed || chance >= 65 ? 1 : 0;     // Flee / Fight (a person who chose the fight himself does not run from it)
            return null;
        }
    }

    // ---------------------------------------------------------------- the game
    static Character me() { return PcProbe.pc(); }

    static String snap(Character p) {
        int pills = 0; for (Item i : p.pillBag) pills += i.quantity;
        int mats = 0; for (Item i : p.materialBag) mats += i.quantity;
        return "ss=" + PlayerLists.wealth(p) + " bag=" + p.backpack.size() + " pills=" + pills + " mats=" + mats + " lvl=" + p.lvl + " tech=" + p.techniqueList.size() + " pot=" + p.potential + " kills=" + p.kills
                + " rels=" + p.relationshipList.copy().size() + " sect=" + (p.sect == null ? "-" : p.sect.name) + " crafted=" + p.itemsCrafted + " loc=" + p.location.x + "," + p.location.y + " beasts=" + Taming.beasts(p).size();
    }

    /** One command; returns the lines it printed without the status block. */
    static String exec(String cmd, int secs) throws Exception {
        PcProbe.cap.reset();
        Character p = me();
        String before = p == null ? "" : snap(p);
        int t0 = GameDriver.time;
        PcProbe.cmd(cmd, secs);
        int dt = GameDriver.time - t0;
        String raw = PcProbe.out();
        PcProbe.cap.reset();
        cmdCounts.merge(label(cmd), 1, Integer::sum);
        cmdWeeks.merge(label(cmd), dt, Integer::sum);
        StringBuilder sb = new StringBuilder();
        String nm = p == null ? "@@" : p.name + " " + p.lastName + " - ";
        for (String line : raw.split("\n")) {
            String t = line.trim();
            if (t.contains("Exception") || t.startsWith("at Game.") || t.contains("java.lang.")) { exceptions++; problems.add("EXCEPTION after '" + cmd + "' at " + yr(GameDriver.time) + ": " + t); }
            if (t.startsWith("Cause:") || t.contains("has died!")) events.add(yr(GameDriver.time) + " " + t);
            if (t.startsWith("[Lesson]")) { extras.merge("lessons told", 1, Integer::sum); events.add(yr(GameDriver.time) + " " + t); }
            if (t.startsWith("[A little story]")) { extras.merge("little stories told", 1, Integer::sum); events.add(yr(GameDriver.time) + " " + t); }
            if (t.contains("A new quest is in your Quest Log: Thin out the beasts")) { extras.merge("favors accepted", 1, Integer::sum); events.add(yr(GameDriver.time) + " " + t); }
            if (t.startsWith("*** DECISION")) { promptCounts.merge(norm(t), 1, Integer::sum); }
            if (t.isEmpty() || t.equals(">") || t.contains("| MANUAL") || t.startsWith("next:") || t.startsWith("try:") || t.startsWith(nm) || t.startsWith("[Quest Alert]") || t.startsWith("[Notice]") || t.startsWith("=== Spring") || t.startsWith("=== Summer") || t.startsWith("=== Autumn") || t.startsWith("=== Winter")) continue;
            sb.append(t).append("\n");
        }
        for (String s : DecisionBroker.promptLog) promptCounts.merge("prompt: " + norm(s), 1, Integer::sum);
        DecisionBroker.promptLog.clear();
        for (String s : Chapters.shown) { scenes.add(yr(GameDriver.time) + " " + s); }
        Chapters.shown.clear();
        Character after = me();
        String out = sb.toString().trim();
        if (p != null && after == p && !p.dead) {
            String now = snap(p);
            if (!now.equals(before) && out.isEmpty() && !cmd.startsWith("wait") && !cmd.startsWith("meditate") && !cmd.startsWith("goto") && !cmd.startsWith("chase") && !cmd.startsWith("goquest"))
                silent.add(yr(GameDriver.time) + " '" + cmd + "' changed " + diff(before, now) + " and printed nothing");
        }
        if (log != null) log.println(yr(GameDriver.time) + " > " + cmd + (out.isEmpty() ? "" : "  | " + out.replace('\n', '|')));
        return out;
    }

    /** A prompt without the names and numbers that change from one time to the next. */
    static String norm(String s) { return s.replaceAll("[A-Z][A-Za-z' -]+ \\(Lv\\.[0-9]+[^)]*\\)", "<someone>").replaceAll("[0-9,]+", "#"); }

    static String label(String cmd) { int i = cmd.indexOf(' '); return i < 0 ? cmd : cmd.substring(0, i); }

    static String diff(String a, String b) {
        String[] x = a.split(" "), y = b.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < x.length && i < y.length; i++) if (!x[i].equals(y[i])) sb.append(x[i]).append("->").append(y[i]).append(' ');
        return sb.toString().trim();
    }

    // ---------------------------------------------------------------- the map
    static Area nearest(Character p, Predicate<Area> ok) {
        Area best = null; int bd = Integer.MAX_VALUE;
        for (Area a : new ArrayList<>(Map.createdAreas)) {
            if (a == null || !ok.test(a)) continue;
            int d = PlayerControl.distance(p.location, a);
            if (d < bd) { bd = d; best = a; }
        }
        return best;
    }

    static String go(Character p, Area a) { return a == null || a == p.location ? null : "goto " + a.x + " " + a.y; }

    static List<Character> adultsHere(Character p) {
        List<Character> out = new ArrayList<>();
        for (Character c : new ArrayList<>(p.location.characters)) if (c != null && c != p && !c.dead && c.born && c.age >= 14 && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster) out.add(c);
        return out;
    }

    // ---------------------------------------------------------------- what the bot does
    static int weeksOnChapter = 0, readyWeeks = 0, lastMajor = -1;
    static String lastChapter = "";
    static int trainWeeks = 0;
    static int turn = 0;
    static Character lastP;

    static String decide(Character p) throws Exception {
        Starts.State st = p.story;
        if (p.breakthroughReady) {
            readyWeeks++;
            int gate = Character.gatePotential(Math.min(10, p.majorLvl + 1));
            if (p.potential >= gate || readyWeeks >= 104) { readyWeeks = 0; return "breakthrough"; }
        } else readyWeeks = 0;
        String care = poisonCare(p);
        if (care != null) return care;
        if (turn % 8 == 0 && !p.pillBag.isEmpty()) return "consume";
        if (turn % 6 == 3) return "equip"; // a person in MANUAL mode puts on his own gear
        if (p.location.city && turn % 7 == 0 && PlayerLists.wealth(p) > 2000) return "buygear"; // and buys better when he can
        if (p.location.city && p.backpack.size() > 6 && turn % 5 == 0) return "sell";
        if (st == null || st.done) return grow(p);
        Chapters.Def d = Chapters.DEFS.get(st.story);
        if (d == null) return grow(p);
        Chapters.Chapter ch = st.open && st.cur != null ? d.find(st.cur) : (st.chapter < d.chapters.size() ? d.chapters.get(st.chapter) : null);
        if (ch == null) return grow(p);
        if (!st.open) {
            // waiting for the gap, the age or the realm of the next chapter
            return grow(p);
        }
        Character who = ch.who.length() > 0 ? Chapters.person(st, ch.who) : null;
        switch (ch.kind) {
            case "MEET": {
                if (who == null || who.dead || who.location == p.location) return grow(p);
                return "chase #" + who.id;
            }
            case "DEFEAT": {
                if (who == null || who.dead) return grow(p);
                if (who.location != p.location) return "chase #" + who.id;
                // on the Story difficulty the foe of a hero leaves him alive: a person simply fights, as often as it takes
                return "!attack #" + who.id;
            }
            case "PLACE": {
                Area a = Chapters.place(p, st, ch.where);
                String g = go(p, a);
                return g != null ? g : grow(p);
            }
            case "WAIT": return "wait 2";
            case "REALM": return grow(p);
            case "AGE": return grow(p);
            case "FRIENDS": return friends(p, ch.n);
            case "KILLS": return kills(p);
            case "WEALTH": return wealth(p);
            case "SHOPS": return shops(p, ch.n);
            case "FOUND_SECT": return foundSect(p);
            case "FOUND_CITY": return foundCity(p);
            case "TAME": return tame(p);
            case "CHECK": return made(p);
            case "NONE": case "FLAG": default: return grow(p);
        }
    }

    static boolean wantsSect(Character p) {
        String id = storyId == null ? "" : storyId;
        return !(id.equals("heir") || id.equals("clan"));   // those two stories ask the person to found a sect of their own
    }

    /** "You feel sick: you have been poisoned ... Eat food of level L+ to purge it": a person eats, and buys food at the market of a city first if he has none. */
    static String poisonCare(Character p) throws Exception {
        Life.Fx fx = p.fx;
        if (fx == null || fx.poisonWeeks <= 0) return null;
        Item food = Life.firstOf(p, Item.ItemType.Food);
        if (food != null && food.lvl >= fx.poisonLvl) return "eat";
        if (!p.location.city) { Area c = nearest(p, a -> a.city); String g = go(p, c); return g; }
        String market = exec("market", 60);
        int bestIdx = -1, bestPrice = Integer.MAX_VALUE;
        for (String line : market.split("\n")) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("^\\s*(\\d+)\\s+Food\\s+(.+?)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+").matcher(line);
            if (m.find() && Integer.parseInt(m.group(3)) >= fx.poisonLvl && Integer.parseInt(m.group(5)) < bestPrice) { bestIdx = Integer.parseInt(m.group(1)); bestPrice = Integer.parseInt(m.group(5)); }
        }
        if (bestIdx > 0) return "buy market " + bestIdx + " 2";
        Area other = nearest(p, a -> a.city && a != p.location);
        return go(p, other);
    }

    static String grow(Character p) {
        // a sect: arrays, members and extra cultivation (the person looks for one that takes him)
        if (wantsSect(p) && p.sect == null && p.majorLvl >= 1 && turn % 30 == 0) {
            Area sectTile = nearest(p, a -> a.sect != null && !a.sect.virtual && a.sect.canJoin(p));
            if (sectTile != null) { if (sectTile == p.location) return "join"; String g = go(p, sectTile); if (g != null) return g; }
        }
        if (p.sect != null && p.sect.location != null && p.location != p.sect.location && !(p.sect.location.siegeUntil > GameDriver.time)) {
            String g = go(p, p.sect.location); if (g != null) return g; // home to the sect to cultivate
        }
        // growth is the cultivation bar: meditate (1.5 ticks and the techniques) is the best use of a week, in a quiet place (crowds challenge and ambush a weak cultivator)
        if (p.sect == null && (p.location.characters.size() > 5 || p.location.crowdHotspot || p.location.monsters > 50)) {
            Area q = nearest(p, a -> a != p.location && !a.city && a.sect == null && a.characters.size() <= 2 && a.monsters < 20 && PlayerControl.distance(p.location, a) <= 8);
            String g = go(p, q);
            if (g != null) return g;
        }
        return "meditate";
    }

    static String friends(Character p, int n) {
        List<Character> here = adultsHere(p);
        Character best = null; int bv = 99;
        for (Character c : here) {
            Relationship r = p.relationshipList.contains(c.id);
            int v = r == null ? 0 : r.value;
            if (v < 2 && v < bv) { bv = v; best = c; }
        }
        if (best != null) return "talk #" + best.id;
        Area crowd = nearest(p, a -> a != p.location && a.characters.size() >= 6);
        String g = go(p, crowd);
        return g != null ? g : grow(p);
    }

    static String kills(Character p) {
        if (p.location.monsters > 0 && !p.location.city) return "hunt";
        Area a = nearest(p, x -> x.monsters > 0 && !x.city && x.lvl <= p.lvl + 8);
        String g = go(p, a);
        return g != null ? g : grow(p);
    }

    static String wealth(Character p) {
        // trade: work the trade, sell the goods, hunt for the loot, gather and sell
        if (p.location.city && p.backpack.size() > 2) return "sell";
        Character.Traits tr = p.trait;
        boolean crafter = tr == Character.Traits.Alchemist || tr == Character.Traits.Blacksmith || tr == Character.Traits.Tailor || tr == Character.Traits.Scribe || tr == Character.Traits.Gatherer || tr == Character.Traits.Miner || tr == Character.Traits.Merchant;
        if (crafter) return made(p); // the others (an Innovative, a Tamer ...) earn by the loot of their hunts
        if (p.location.monsters > 0 && !p.location.city) return "hunt";
        return kills(p);
    }

    static String shops(Character p, int n) {
        if (p.location.city && p.ownedShops.size() < n) {
            boolean here = false;
            for (Shop s : p.ownedShops) if (s.location == p.location) here = true;
            if (!here) return "profession";
        }
        Area city = nearest(p, a -> a.city && a != p.location && a.shops.size() < a.maxShopSlots() && ownsNoShopIn(p, a));
        String g = go(p, city);
        return g != null ? g : "profession";
    }

    static boolean ownsNoShopIn(Character p, Area a) { for (Shop s : p.ownedShops) if (s.location == a) return false; return true; }

    static String foundSect(Character p) {
        if (p.majorLvl < 2) return grow(p);
        if (p.sect != null) return "!resign";
        if (p.location.sect != null) { Area a = nearest(p, x -> x.sect == null && !x.city); String g = go(p, a); return g != null ? g : grow(p); }
        return "found sect";
    }

    static String foundCity(Character p) {
        if (p.majorLvl < 2) return grow(p);
        if (p.location.city && p.location.cityOwner != null) { Area a = nearest(p, x -> !x.city && x.sect == null); String g = go(p, a); return g != null ? g : grow(p); }
        return "found city";
    }

    static String tame(Character p) {
        if (p.location.monsters > 0 && !p.location.city) return "tame";
        Area a = nearest(p, x -> x.monsters > 0 && !x.city && x.lvl <= p.lvl + 4);
        String g = go(p, a);
        return g != null ? g : grow(p);
    }

    static int noMaterialTurns = 0;

    /** Counted crafts ("make ten items"): a turn of the trade, which gathers, buys or crafts as it needs; when the tile has nothing to gather, a tile that has some. */
    static String made(Character p) {
        boolean herb = p.trait == Character.Traits.Alchemist || p.trait == Character.Traits.Tailor || p.trait == Character.Traits.Scribe;
        int here = herb ? p.location.herbMaterials : p.location.metalMaterials;
        int stack = Profession.bestStack(p, herb ? Item.ItemType.Herb : Item.ItemType.Metal, 100);
        if (stack < Character.CRAFT_COST && here <= 0 && !p.location.city) {
            Area a = nearest(p, x -> (herb ? x.herbMaterials : x.metalMaterials) > 1000 && !x.city);
            String g = go(p, a);
            if (g != null) return g;
        }
        return "profession";
    }

    // ---------------------------------------------------------------- the run
    public static void main(String[] a) throws Exception {
        String which = a.length > 0 ? a[0] : "1";
        years = a.length > 1 ? Integer.parseInt(a[1]) : 60;
        long seed = a.length > 2 ? Long.parseLong(a[2]) : 1L;
        List<String> ids = new ArrayList<>(Starts.STORIES.keySet());
        int idx = which.matches("\\d+") ? Integer.parseInt(which) : ids.indexOf(which) + 1;
        storyId = ids.get(Math.max(0, idx - 1));
        System.setProperty("tdf.seed", String.valueOf(seed));
        log = new PrintStream(new java.io.FileOutputStream("journey-" + storyId + ".log"), true, "UTF-8");
        PcProbe.real = System.out;
        System.setIn(PcProbe.in);
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(PcProbe.real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        try { run(idx); } catch (Throwable e) { PcProbe.real.println("[T] JOURNEY EXCEPTION " + e); e.printStackTrace(PcProbe.real); problems.add("harness exception " + e); }
        report();
        System.exit(0);
    }

    static void run(int idx) throws Exception {
        DecisionBroker.scripted = new Careful(); DecisionBroker.scriptedNumbers = new ArrayDeque<>(); // every prompt is answered (the careful answers above, else the default) and logged
        if (System.getProperty("journey.extras") != null) { Vignettes.testMode = true; Settings.lessons = true; Settings.vignettes = true; } // the lessons, little stories and favors take part too
        PcProbe.in.feed("n\ns\n4\n" + idx + "\n1\n");
        PcProbe.awaitBlocked(240);
        Character p = me();
        if (p == null) { problems.add("no character after the start menu"); return; }
        if (p.story == null) { problems.add("the Story start did not give a story"); return; }
        say("started " + storyId + ": " + p.name + " " + p.lastName + " " + p.getTrait() + " potential " + p.potential + " age " + p.age + " ss " + PlayerLists.wealth(p) + " at " + p.location.name);
        // the person plays by hand: no duties, breakthroughs on a button (the bot decides when)
        exec("duties all off", 60);
        exec("policy breakthrough button", 60);
        exec("policy flee on", 60); // an outmatched person tries to get away (the game's default is to stand and fight)
        exec("settings hud off", 60);
        int end = GameDriver.time + years * 52;
        int lastReport = GameDriver.time, stuckGuard = 0;
        String lastCmd = "";
        int startMajor = p.majorLvl;
        while (GameDriver.time < end) {
            Character q = me();
            if (q == null || q.dead) { events.add(yr(GameDriver.time) + " the character is dead or gone: " + (lastP == null ? "?" : lastP.name + " " + lastP.lastName + ", age " + lastP.age + ", Major " + lastP.majorLvl)); break; }
            lastP = q;
            if (PcProbe.gameError != null) { problems.add("game thread died: " + PcProbe.gameError); break; }
            p = q;
            Starts.State st = p.story;
            trackChapters(p, st);
            turn++;
            String cmd = decide(p);
            committed = cmd.startsWith("!attack");
            int t0 = GameDriver.time;
            String out = exec(cmd, 180);
            int dt = GameDriver.time - t0;
            if (p.dead) { events.add(yr(GameDriver.time) + " the character is dead: " + p.name + " " + p.lastName + ", age " + p.age + ", Major " + p.majorLvl + " (see the Cause line above)"); break; }
            // a command that did nothing at all is retried as meditation (a person would do something else)
            if (dt == 0 && !isFree(cmd) && out.length() > 0) {
                stuckGuard++;
                if (stuckGuard % 10 == 1) problems.add(yr(GameDriver.time) + " no time passed for '" + cmd + "': " + out.replace('\n', '|'));
                exec("meditate", 120);
            } else stuckGuard = 0;
            if (p.majorLvl != lastMajor) { events.add(yr(GameDriver.time) + " reached major level " + p.majorLvl + " (" + TermFormat.realm(p.majorLvl) + ") lvl " + p.lvl + " potential " + p.potential); lastMajor = p.majorLvl; }
            if (GameDriver.time - lastReport >= 52 * 5) { lastReport = GameDriver.time; say(yr(GameDriver.time) + " " + p.name + " age " + p.age + " Major " + p.majorLvl + " lv " + p.lvl + " pot " + p.potential + " ss " + PlayerLists.wealth(p) + " crafted " + p.itemsCrafted + " chapter " + chapterNow(p)); }
            lastCmd = cmd;
        }
        Character f = me();
        if (f != null) say("end " + yr(GameDriver.time) + " " + f.name + " age " + f.age + " Major " + f.majorLvl + " lv " + f.lvl + " chapter " + chapterNow(f) + " done=" + (f.story != null && f.story.done));
    }

    static boolean isFree(String cmd) { String l = label(cmd); return l.equals("consume") || l.equals("sell") || l.equals("breakthrough") || l.equals("equip") || l.equals("buygear") || l.equals("duties") || l.equals("policy") || l.equals("settings") || l.equals("goto") || l.equals("chase") || l.equals("talk"); }

    static String chapterNow(Character p) {
        Starts.State st = p.story;
        if (st == null) return "-";
        Chapters.Def d = Chapters.DEFS.get(st.story);
        if (d == null) return "?";
        if (st.done) return "(done)";
        if (st.open && st.cur != null) return st.cur + "(open)";
        return st.chapter < d.chapters.size() ? d.chapters.get(st.chapter).id + "(waiting)" : "(end)";
    }

    static String prevState = "";
    static int openedAt = 0;
    static String openId = "";

    static void trackChapters(Character p, Starts.State st) {
        if (st == null) return;
        String now = st.done ? "done" : (st.open && st.cur != null ? "open:" + st.cur : "wait:" + st.chapter);
        if (!now.equals(prevState)) {
            if (now.startsWith("open:")) { openId = st.cur; openedAt = GameDriver.time; chapterLog.add(yr(GameDriver.time) + " opened " + st.cur + " (age " + p.age + ", Major " + p.majorLvl + ", crafted " + p.itemsCrafted + ")"); }
            else if (prevState.startsWith("open:")) chapterLog.add(yr(GameDriver.time) + " closed " + prevState.substring(5) + " after " + String.format("%.1f", (GameDriver.time - openedAt) / 52.0) + " years");
            if (now.equals("done")) chapterLog.add(yr(GameDriver.time) + " THE STORY IS COMPLETE (flags " + st.flags + ")");
            prevState = now;
        }
        if (st.open && GameDriver.time - openedAt > 52 * 12 && (GameDriver.time - openedAt) % (52 * 12) == 0) problems.add(yr(GameDriver.time) + " chapter " + openId + " has been open for " + (GameDriver.time - openedAt) / 52 + " years (" + chapterNow(p) + ", Major " + p.majorLvl + ", crafted " + p.itemsCrafted + ")");
    }

    static void report() {
        say("=== REPORT for " + storyId + " ===");
        Character p = me();
        Starts.State st = p == null ? null : p.story;
        say("chapters (" + chapterLog.size() + " events):");
        for (String s : chapterLog) say("  " + s);
        if (st != null) say("final: chapter index " + st.chapter + ", open=" + st.open + ", done=" + st.done + ", flags=" + st.flags + ", episodes seen " + st.seen);
        say("events:"); for (String s : events) say("  " + s);
        say("commands (count/weeks): " + cmdCounts.size() + " kinds"); for (java.util.Map.Entry<String, Integer> e : cmdCounts.entrySet()) say("  " + e.getKey() + " x" + e.getValue() + " / " + cmdWeeks.getOrDefault(e.getKey(), 0) + " weeks");
        say("prompts that appeared (" + promptCounts.size() + " kinds):"); for (java.util.Map.Entry<String, Integer> e : promptCounts.entrySet()) say("  x" + e.getValue() + " " + e.getKey());
        say("scenes shown (" + scenes.size() + "):"); for (String s : scenes) say("  " + s);
        say("silent results (" + silent.size() + "):"); int n = 0; for (String s : silent) { say("  " + s); if (++n >= 40) { say("  ..."); break; } }
        LinkedHashMap<String, int[]> grouped = new LinkedHashMap<>(); LinkedHashMap<String, String> firstSeen = new LinkedHashMap<>();
        for (String s : problems) { String key = s.replaceFirst("^Y[0-9.]+ ", "").replaceAll("[0-9,]+", "#"); int[] c = grouped.get(key); if (c == null) { grouped.put(key, new int[]{1}); firstSeen.put(key, s); } else c[0]++; }
        say("problems (" + problems.size() + " in " + grouped.size() + " kinds):"); n = 0; for (java.util.Map.Entry<String, int[]> e : grouped.entrySet()) { say("  x" + e.getValue()[0] + " first: " + firstSeen.get(e.getKey())); if (++n >= 40) { say("  ..."); break; } }
        say("lessons, little stories, favors: " + extras);
        say("exceptions seen in the output: " + exceptions + "; gameError=" + PcProbe.gameError + "; crash.log=" + new java.io.File("crash.log").exists());
        say("===== journey " + storyId + " finished at " + yr(GameDriver.time) + " =====");
    }
}
