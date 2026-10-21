package Game;

import java.io.PrintStream;
import java.util.*;
import java.util.regex.*;

/** 2026-10-10 (third batch): the chapters of the Story starts (Chapters, ChapText, Tale*): every story's text is well formed (placeholders resolve, flags are known, no line too long),
 *  and the Last Heir plays through on each road: its people appear when needed and are protected (only the human can kill them), choices are kept and change later lines, chapters
 *  that belong to another road are skipped, the quest follows the task, replays work and the story ends with its epilogue. */
public class TalesProbe {
    static void check(String name, boolean ok) { PcProbe.passes += ok ? 1 : 0; PcProbe.fails += ok ? 0 : 1; PcProbe.real.println("[T] " + (ok ? "PASS " : "FAIL ") + name); }

    public static void main(String[] a) throws Exception {
        PcProbe.real = System.out;
        System.setIn(PcProbe.in);
        GameDriver.frozenNpcs = true;
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(PcProbe.real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        try { run(); } catch (Throwable e) { PcProbe.real.println("[T] PROBE EXCEPTION " + e); e.printStackTrace(PcProbe.real); PcProbe.fails++; }
        PcProbe.real.println("[T] ===== " + PcProbe.passes + " passed, " + PcProbe.fails + " failed; gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }

    static Area home() { return Map.tiles[Map.range / 2][Map.range / 2]; }
    static void tp(Character c, Area to) { c.location.removeChar(c); c.location = to; to.addChar(c); }
    static Starts.Choice story(String id, int diff) { Starts.Choice ch = new Starts.Choice(); ch.kind = Starts.Kind.STORY; ch.story = id; ch.difficulty = diff; return ch; }
    static void weeks(int w) { GameDriver.time += w; }
    static void years(int y) { GameDriver.time += y * 52; }
    static void answers(int... a) { DecisionBroker.scripted.clear(); for (int x : a) DecisionBroker.scripted.add(x); }

    /** Plays the Heir to the end. `kill` = what to do with Cinder (true = kill), `orrinRoad` = 0 expose, 1 duel (then kill), 2 duel (then spare), 3 deal. Returns the character. */
    static Character play(Character p, String tag, boolean killCinder, int orrinRoad, boolean killPell) throws Exception {
        Starts.apply(p, story("heir", 0));
        Starts.State st = p.story;
        p.lvl = 5; p.majorLvl = 0; p.itemsCrafted = 0; p.ss = 0; p.age = 18;
        Chapters.shown.clear();
        Starts.tick(p);
        check(tag + " 1 the first chapter opens at once: a scene and a quest (" + Chapters.shown + ")", st.open && Chapters.shown.contains("Ash") && questCount(p, st) == 1);
        check(tag + " 2 nobody of the story exists yet", st.npcs.isEmpty());
        p.majorLvl = 1; p.lvl = 12;
        Starts.tick(p);
        check(tag + " 3 the first task done: the chapter closed, 3 pills paid, the next one waits a year (open=" + st.open + ", chapter=" + st.chapter + ")", !st.open && st.chapter == 1 && !p.pillBag.isEmpty());
        Starts.tick(p);
        check(tag + " 4 nothing opens before the year is over", !st.open && st.chapter == 1);

        // II. Wen, the cauldron, the letter
        years(1); weeks(2);
        Starts.tick(p);
        Character wen = Chapters.person(st, "wen");
        check(tag + " 5 'The Blue Cloud' opens: Wen exists, is fated to the player, in a city (" + (wen == null ? "none" : wen.name + " " + wen.lastName + " fated=" + wen.fatedTo) + ")", st.open && wen != null && wen.fatedTo == p.id + 1 && Map.characters.get(wen.id) == wen && wen.location.city);
        check(tag + " 6 the quest points at her", hasQuestFor(p, wen));
        tp(p, wen.location);
        Starts.tick(p);
        check(tag + " 7 meeting her closes the chapter and pays 300", !st.open && st.chapter == 2 && p.ss >= 300);
        years(1); weeks(2);
        Starts.tick(p);
        check(tag + " 8 'The Cracked Cauldron' waits for ten brews (open=" + st.open + ")", st.open && Chapters.flag(st, "craft0") == p.itemsCrafted);
        Starts.tick(p);
        check(tag + " 9 still open with nine brews", p.itemsCrafted - Chapters.flag(st, "craft0") < 10 && st.open);
        p.itemsCrafted += 10;
        Starts.tick(p);
        check(tag + " 10 ten brews close it and give gear", !st.open && st.chapter == 3 && p.equippedWeapon != null);

        years(2); weeks(2); p.majorLvl = 2; p.lvl = 22;
        answers(0);
        long ss0 = p.ss;
        Starts.tick(p);
        Character orrin = Chapters.person(st, "orrin");
        check(tag + " 11 the letter: Orrin appears in a city other than Wen's", orrin != null && orrin.location.city && orrin.location != wen.location);
        tp(p, orrin.location);
        Starts.tick(p);
        check(tag + " 12 meeting him asks the question; 'accept' pays 3,000 (patron=" + Chapters.flag(st, "patron") + ", +" + (p.ss - ss0) + ")", !st.open && Chapters.flag(st, "patron") == 1 && p.ss - ss0 >= 3000);

        // III. Marrow Reach, the runner
        years(3); weeks(2); p.majorLvl = 3; p.lvl = 32;
        Starts.tick(p);
        Area marrow = Chapters.place(p, st, "marrow");
        check(tag + " 13 'Marrow Reach' picks a wild tile and sends the player there (" + (marrow == null ? "none" : marrow.name) + ")", st.open && marrow != null && !marrow.city && marrow.sect == null && hasQuestFor(p, marrow));
        check(tag + " 14 the places of the story are different tiles", marrow != Chapters.place(p, st, "wen") && marrow != Chapters.place(p, st, "vale"));
        answers(1);
        tp(p, marrow);
        Starts.tick(p);
        check(tag + " 15 arriving shows the village and keeps the answer (mercy=" + Chapters.flag(st, "mercy") + ")", !st.open && Chapters.flag(st, "mercy") == 2);

        years(4); weeks(2);
        Starts.tick(p);
        Character cinder = Chapters.person(st, "cinder");
        check(tag + " 16 'The Runner': Cinder exists, a Major above, fated, hostile (lvl " + (cinder == null ? "-" : cinder.lvl + " vs " + p.lvl + ", rel " + (cinder.relationshipList.contains(p.id) == null ? "?" : cinder.relationshipList.contains(p.id).value)) + ")", cinder != null && cinder.lvl >= p.lvl + 8 && cinder.fatedTo == p.id + 1 && cinder.relationshipList.contains(p.id) != null && cinder.relationshipList.contains(p.id).value <= -2);
        tp(p, cinder.location);
        Starts.tick(p);
        Starts.tick(p);
        check(tag + " 17 meeting him closes 'The Runner'; the next tick 'The Torch' opens and asks for a win", st.open && "torch".equals(chapterId(st)));

        // the protection: nobody but the human ends him
        Character stranger = adult(60, 6);
        cinder.dead(); check(tag + " 18 age, plague and the epoch cannot end him", !cinder.dead);
        cinder.dead(stranger); check(tag + " 19 a stranger cannot kill him", !cinder.dead);
        boolean safe = Rankings.epochSafe(cinder);
        check(tag + " 20 the epoch spares him", safe);
        stranger.winCheck(cinder);
        check(tag + " 21 a stranger who beats him leaves him alive", !cinder.dead);
        Starts.tick(p);
        check(tag + " 22 the task is not done by a stranger's win", st.open && "torch".equals(chapterId(st)));
        // the foe beats the human on the Story difficulty: left alive
        p.dead = false;
        cinder.winCheck(p);
        check(tag + " 23 on the Story difficulty the foe leaves the hero alive", !p.dead);
        answers(killCinder ? 0 : 1);
        p.winCheck(cinder);
        check(tag + " 24 the player's win is recorded (beaten=" + Chapters.flag(st, "beaten.cinder") + ", dead=" + cinder.dead + ")", Chapters.flag(st, "beaten.cinder") == 1 && cinder.dead == killCinder);
        Starts.tick(p);
        check(tag + " 25 the chapter closes (chapter " + st.chapter + ")", !st.open && st.chapter == 7);

        // IV. the ledger, the last cup
        years(2); weeks(2);
        Starts.tick(p);
        Area vale = Chapters.place(p, st, "vale");
        check(tag + " 26 'The Ledger' sends the player to Orrin's city", st.open && vale == orrin.location && hasQuestFor(p, vale));
        tp(p, vale);
        Starts.tick(p);
        check(tag + " 27 reading the records closes the Ledger (chapter " + st.chapter + ")", st.chapter == 8 && !st.open);
        answers(orrinRoad == 0 ? 0 : orrinRoad == 3 ? 2 : 1);
        Starts.tick(p); Starts.tick(p); // the Last Cup opens next tick (it asks, and its task is none, so it closes at once)
        check(tag + " 28 the road is kept (orrin=" + Chapters.flag(st, "orrin") + ")", Chapters.flag(st, "orrin") == (orrinRoad == 0 ? 1 : orrinRoad == 3 ? 3 : 2));
        if (orrinRoad == 0) check(tag + " 29 exposed: he is ruined and no longer fated", Chapters.flag(st, "exposed") == 1 && orrin.fatedTo == 0 && orrin.ss == 0 && "avenger".equals(chapterIdOr(st, "avenger", "debt")) || st.chapter >= 11);
        if (orrinRoad == 1 || orrinRoad == 2) {
            check(tag + " 29 the duel: the Reckoning is the chapter now (" + chapterId(st) + ")", st.open && "reckoning".equals(chapterId(st)));
            answers(orrinRoad == 1 ? 0 : 1);
            tp(p, orrin.location);
            p.winCheck(orrin);
            check(tag + " 30 beating Orrin " + (orrinRoad == 1 ? "and killing him" : "and sparing him"), Chapters.flag(st, "beaten.orrin") == 1 && orrin.dead == (orrinRoad == 1));
            Starts.tick(p);
        }
        if (orrinRoad == 3) check(tag + " 29 the deal skips the Reckoning (chapter " + st.chapter + ")", !"reckoning".equals(chapterId(st)));

        // V. those who are left
        years(26);
        Starts.tick(p);
        if (killCinder) {
            Character pell = Chapters.person(st, "pell");
            check(tag + " 31 'Those Who Loved Him' brings Pell, fated and hostile; Wen may have fallen (wen dead=" + wen.dead + ")", pell != null && pell.fatedTo == p.id + 1 && st.open && "avenger".equals(chapterId(st)));
            answers(killPell ? 0 : 1);
            tp(p, pell.location);
            p.winCheck(pell);
            Starts.tick(p);
            check(tag + " 32 Pell is beaten, " + (killPell ? "killed" : "spared") + "; the chapter closes", !st.open && pell.dead == killPell);
        } else {
            check(tag + " 31 Cinder lived: the avenger is skipped and the letter from the south comes (" + chapterId(st) + ")", Chapters.person(st, "pell") == null && st.open == false || "debt".equals(chapterId(st)));
            years(16);
            Starts.tick(p);
            check(tag + " 32 the letter was read and closed", st.seen.contains("debt") && p.potential <= 100);
        }

        // VI. the sect
        years(9); p.majorLvl = 4; p.lvl = 42;
        answers(2);
        Starts.tick(p);
        check(tag + " 33 'What a Sect Is For' waits for the sect (" + chapterId(st) + ")", st.open && "sect".equals(chapterId(st)));
        Organization sect = new Organization("Azure Cloud", p.location);
        p.sect = sect; sect.master = p;
        Starts.tick(p);
        check(tag + " 34 founding it ends season 1: the interlude plays, the story goes on (done=" + st.done + ", seen=" + st.seen.contains("interlude.1") + ")", !st.done && st.seen.contains("interlude.1") && !st.open && st.chapter == 13);
        years(4); weeks(2); p.majorLvl = 4; p.lvl = 45;
        int rounds = 0; Chapters.Def hd = Chapters.DEFS.get("heir");
        while (!st.done && rounds++ < 400) { weeks(40); p.majorLvl = 8; p.lvl = 85; DecisionBroker.scripted.clear(); for (int i = 0; i < 6; i++) DecisionBroker.scripted.add(1); Starts.tick(p); if (st.open && st.cur != null) { Chapters.Chapter cc = hd.find(st.cur); if (cc != null) { satisfy(p, st, cc); Starts.tick(p); } } }
        check(tag + " 34b seasons 2 and 3 play through to the epilogue (rounds " + rounds + ", done=" + st.done + ")", st.done && st.seen.contains("epilogue") && st.seen.contains("interlude.2") && st.seen.contains("beds") && st.seen.contains("wick"));
        check(tag + " 34c the people are ordinary now (fatedTo " + cinder.fatedTo + ")", cinder.fatedTo == 0);
        check(tag + " 35 the choice about the cauldron is kept (cauldron=" + Chapters.flag(st, "cauldron") + ")", Chapters.flag(st, "cauldron") == 3);
        String rp = Chapters.replay(p, st, "");
        check(tag + " 36 'story replay' lists the scenes seen (" + st.seen.size() + ")", rp.contains("ash") && rp.contains("epilogue") && st.seen.size() >= 10);
        String sc = Chapters.replay(p, st, "ash");
        check(tag + " 37 a scene can be read again, in the terminal's frame", sc.contains("ASH") && sc.contains("Cauldron") || sc.contains("cauldron"));
        String desc = Starts.describe(p);
        check(tag + " 38 'story' lists the chapters as done", desc.contains("[x] Ash") && desc.contains("Story complete"));
        return p;
    }

    static int questCount(Character p, Starts.State st) {
        int n = 0;
        for (Quest q : p.targetQuests) if (q.uid == st.questUid) n++;
        for (Quest q : p.spotQuests) if (q.uid == st.questUid) n++;
        return n;
    }
    static boolean hasQuestFor(Character p, Character who) { for (Quest q : p.targetQuests) if (q.uid == p.story.questUid && q.targetCharacter == who.id) return true; return false; }
    static boolean hasQuestFor(Character p, Area at) { for (Quest q : p.targetQuests) if (q.uid == p.story.questUid && q.targetArea == at) return true; return false; }
    static String chapterId(Starts.State st) { Chapters.Def d = Chapters.DEFS.get(st.story); return st.chapter < d.chapters.size() ? d.chapters.get(st.chapter).id : "(end)"; }
    static String chapterIdOr(Starts.State st, String x, String y) { String c = chapterId(st); return c.equals(x) || c.equals(y) ? c : x; }

    static Character adult(int lvl, int major) {
        Character c = new Character(home());
        c.age = 30; c.gender = "Male"; c.lvl = lvl; c.majorLvl = major; c.maxAge = 100000; c.caution = 50; c.sect = null;
        if (c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) c.trait = Character.Traits.Friendly;
        c.id = Map.characters.size();
        GameDriver.UID = Math.max(GameDriver.UID, c.id + 1);
        Map.characters.add(c); Map.population++;
        return c;
    }

    // ------------------------------------------------------------------ the lint of every story's text
    static void lint() {
        Pattern tok = Pattern.compile("\\{([^}]*)\\}");
        for (Chapters.Def d : Chapters.DEFS.values()) {
            Set<String> flagsSet = new HashSet<>(d.extraFlags);
            List<Chapters.Chapter> every = new ArrayList<>(d.chapters); every.addAll(d.episodes);
            for (Chapters.Chapter ch : every) {
                if (ch.choice != null) flagsSet.add(ch.choice.flag);
                if (ch.outChoice != null) flagsSet.add(ch.outChoice.flag);
            }
            int lines = 0, words = 0, sentences = 0, longest = 0; List<String> bad = new ArrayList<>();
            List<String[]> all = new ArrayList<>();
            for (Chapters.Chapter ch : every) {
                all.add(ch.intro); all.add(ch.outro);
                for (Chapters.Choice c : new Chapters.Choice[]{ch.choice, ch.outChoice}) if (c != null && c.after != null) for (String[] x : c.after) if (x != null) all.add(x);
            }
            all.add(d.epilogue);
            for (Chapters.Season sn : d.seasons) all.add(sn.interlude);
            for (String[] block : all) for (String raw : block) {
                lines++;
                String l = raw;
                while (l.startsWith("?")) {
                    int bar = l.indexOf('|');
                    if (bar < 0) { bad.add("bad condition: " + raw); break; }
                    String cond = l.substring(1, bar).replace("!", "");
                    int eq = cond.indexOf('='); String key = eq >= 0 ? cond.substring(0, eq) : cond;
                    boolean known = flagsSet.contains(key) || key.startsWith("e.") || (key.startsWith("dead.") || key.startsWith("beaten.")) && d.cast.containsKey(key.substring(key.indexOf('.') + 1)) || key.equals("exposed") || key.equals("aged." + key);
                    if (!known) bad.add("unknown flag '" + key + "' in: " + raw);
                    l = l.substring(bar + 1);
                }
                boolean narr = l.startsWith("* "), spk = ChapText.speaker(l);
                if (!narr && !spk) bad.add("not narration or speech: " + raw);
                Matcher m = tok.matcher(l);
                while (m.find()) {
                    String x = m.group(1); boolean ok;
                    if (x.equals("name") || x.equals("first") || x.equals("last") || x.equals("year") || x.equals("age") || x.startsWith("=") || x.startsWith("#")) ok = true;
                    else if (x.startsWith("@")) ok = d.places.containsKey(x.substring(1));
                    else { String k = x.contains(".") ? x.substring(0, x.indexOf('.')) : x; ok = d.cast.containsKey(k); }
                    if (!ok) bad.add("unknown placeholder {" + x + "} in: " + raw);
                }
                String body = spk ? ChapText.words(l) : narr ? l.substring(2) : l;
                String[] ws = body.split("\\s+");
                words += ws.length;
                for (String s : body.split("(?<=[.!?])\\s+")) { sentences++; int n = s.split("\\s+").length; longest = Math.max(longest, n); if (n > 40) bad.add("sentence over 40 words (" + n + "): " + s); }
                if (body.length() > 700) bad.add("line over 700 characters: " + raw.substring(0, 60));
            }
            double perSentence = sentences == 0 ? 0 : (double) words / sentences;
            PcProbe.real.println("[T] info: " + d.id + ": " + d.chapters.size() + " chapters + " + d.episodes.size() + " episodes, " + lines + " lines, " + words + " words, " + String.format("%.1f", perSentence) + " words per sentence, longest " + longest);
            for (String b : bad) PcProbe.real.println("[T]   lint: " + b);
            check("L " + d.id + " text is well formed (" + bad.size() + " problems)", bad.isEmpty());
            check("L " + d.id + " reads plainly (" + String.format("%.1f", perSentence) + " words per sentence, under 17)", perSentence < 17);
            Set<String> ids = new HashSet<>(); boolean uniq = true;
            for (Chapters.Chapter ch : every) if (!ids.add(ch.id)) uniq = false;
            check("L " + d.id + " chapter ids are unique", uniq);
        }
    }


    // ------------------------------------------------------------------ a generic player: does what each task asks, answers at random, until the story ends
    static void satisfy(Character p, Starts.State st, Chapters.Chapter ch) {
        switch (ch.kind) {
            case "PLACE": { Area a = Chapters.place(p, st, ch.where); if (a != null) tp(p, a); break; }
            case "MEET": { Character n = Chapters.person(st, ch.who); if (n != null && n.location != null) tp(p, n.location); break; }
            case "DEFEAT": {
                Character n = Chapters.person(st, ch.who);
                if (n != null && !n.dead && n.location != null) { tp(p, n.location); p.winCheck(n); }
                break;
            }
            case "WEALTH": p.ss += ch.n; p.convertCurrency(); break;
            case "CRAFT": p.itemsCrafted += ch.n + 5; break;
            case "KILLS": p.kills += ch.n; break;
            case "FRIENDS": for (int i = 0; i < ch.n; i++) { Character f = adult(10, 1); p.changeRelationship(f, 3); } break;
            case "SHOPS": for (Area ar : new ArrayList<>(Map.createdAreas)) if (ar.city && p.ownedShops.size() < ch.n) { Shop sh = new Shop(p, ar); p.ownedShops.add(sh); } break;
            case "FOUND_SECT": { Organization o = new Organization("Probe Sect " + GameDriver.time, p.location); p.sect = o; o.master = p; break; }
            case "FOUND_CITY": for (Area ar : new ArrayList<>(Map.createdAreas)) if (ar.city) { ar.cityOwner = p; break; } break;
            case "TAME": while (Taming.beasts(p).size() < ch.n) Taming.legend(p, "Probe", "Beast", 5); break;
            case "FLAG": Chapters.set(st, ch.who, 1); break;
            case "CHECK": p.itemsCrafted += 50; break;
            default: break;
        }
    }

    static int generic(Character p, String id, int diff, long seed, boolean verbose) {
        Random rnd = new Random(seed);
        Starts.apply(p, story(id, diff));
        Starts.State st = p.story;
        p.age = 30; p.lvl = 85; p.majorLvl = 8; p.dead = false; p.sect = null; p.itemsCrafted = 0; p.kills = 0; p.ownedShops.clear(); p.fatedTo = 0;
        Chapters.transcript.clear();
        int guard = 0, scenes0 = Chapters.shown.size();
        Chapters.Def d = Chapters.DEFS.get(id);
        while (!st.done && guard++ < 700) {
            weeks(70);
            Area[] spots = {home(), Chapters.pick(p, st, "wild"), Chapters.pick(p, st, "far")};
            Area spot = spots[rnd.nextInt(3)]; if (spot != null) tp(p, spot);
            p.age = Math.min(p.age + 1, 90); p.maxAge = 100000; p.dead = false;
            DecisionBroker.scripted.clear(); for (int i = 0; i < 6; i++) DecisionBroker.scripted.add(rnd.nextInt(3));
            Starts.tick(p);
            if (st.open && st.cur != null) {
                Chapters.Chapter ch = d.find(st.cur);
                if (ch != null) { satisfy(p, st, ch); DecisionBroker.scripted.clear(); for (int i = 0; i < 6; i++) DecisionBroker.scripted.add(rnd.nextInt(3)); Starts.tick(p); }
            }
        }
        return guard;
    }

    static void generics(Character p) {
        for (String id : Chapters.DEFS.keySet()) {
            int ok = 0, bad = 0, maxGuard = 0; List<String> problems = new ArrayList<>(); Set<String> titles = new HashSet<>();
            for (long seed = 1; seed <= 12; seed++) {
                int g;
                try { g = generic(p, id, (int) (seed % 2), seed * 7919, false); } catch (Throwable e) { problems.add("seed " + seed + ": " + e); bad++; continue; }
                Starts.State st = p.story;
                maxGuard = Math.max(maxGuard, g);
                boolean finished = st.done && st.seen.contains("epilogue");
                for (String l : Chapters.transcript) {
                    if (l.contains("{") || l.contains("}")) problems.add("seed " + seed + " unresolved placeholder: " + l);
                    else if (l.startsWith("?")) problems.add("seed " + seed + " unparsed condition: " + l);
                    else if (!l.startsWith("* ") && !ChapText.speaker(l)) problems.add("seed " + seed + " odd line: " + l);
                }
                titles.addAll(Chapters.shown.subList(0, Chapters.shown.size()));
                if (finished) ok++; else { bad++; problems.add("seed " + seed + " did not finish (chapter " + st.chapter + ", open=" + st.open + ", cur=" + st.cur + ", guard " + g + ")"); }
            }
            for (String pr : problems) PcProbe.real.println("[T]   " + id + ": " + pr);
            check("G " + id + " plays to its epilogue on 12 random roads, no placeholder left over (" + ok + "/12 finished, " + problems.size() + " problems, up to " + maxGuard + " rounds)", bad == 0 && problems.isEmpty());
        }
    }

    static void areaInfo(String when) {
        int city = 0, sect = 0, dungeon = 0, ocean = 0, wild = 0, total = 0;
        for (Area a : new ArrayList<>(Map.createdAreas)) { total++; if (a.city) city++; else if (a.sect != null) sect++; else if (a.dungeonType != null) dungeon++; else if ("Ocean".equals(a.biomeM)) ocean++; else wild++; }
        PcProbe.real.println("[T] info: " + when + ": createdAreas " + total + " (cities " + city + ", sects " + sect + ", dungeons " + dungeon + ", ocean " + ocean + ", wild " + wild + "), time " + GameDriver.time);
    }


    // ------------------------------------------------------------------ the endings written for each start
    static void codas(Character p) {
        String[] fams = {"stay", "soldier", "leave", "fall", "world"};
        String[] ids = {"steward", "soldier", "leave_neutral_x", "leave_evil_dead", "ascendant_x"};
        check("E0 ending ids map to the five families", "stay".equals(StoryCodas.family("usurper1")) && "stay".equals(StoryCodas.family("idler0")) && "soldier".equals(StoryCodas.family("soldier")) && "leave".equals(StoryCodas.family("leave_evil_spared_x")) && "fall".equals(StoryCodas.family("leave_evil_dead")) && "world".equals(StoryCodas.family("last_lamp")) && StoryCodas.family("nonsense") == null);
        for (String id : Chapters.DEFS.keySet()) {
            int missing = 0, lines = 0; List<String> bad = new ArrayList<>();
            for (String f : fams) {
                String[] raw = StoryCodas.TABLE.get(id + ":" + f);
                if (raw == null) { missing++; continue; }
                for (String l : raw) {
                    lines++;
                    String x = l; while (x.startsWith("?")) { int bar = x.indexOf('|'); if (bar < 0) break; x = x.substring(bar + 1); }
                    if (!x.startsWith("* ")) bad.add(f + ": " + l);
                    if (x.contains("{") && !x.matches(".*\\{[a-z]+\\.[A-Za-z]+\\}.*")) bad.add(f + " placeholder: " + l);
                }
            }
            check("E " + id + " has all five endings (" + (5 - missing) + "/5, " + lines + " lines, " + bad.size() + " problems)", missing == 0 && bad.isEmpty());
        }
        // a life that finished its story reads the flags in the coda; one that did not says so
        Starts.apply(p, story("heir", 0));
        p.story.done = true; Chapters.set(p.story, "dead.wen", 1);
        List<String> done = StoryCodas.lines(p, "steward");
        check("E1 a finished story's coda has its title and no 'unfinished' line (" + done.size() + " lines)", done.size() >= 2 && done.get(0).contains("Azure Cloud") && done.stream().noneMatch(l -> l.contains("not quite finished")) && done.stream().noneMatch(l -> l.contains("Wen visits")));
        Chapters.set(p.story, "dead.wen", 0); p.story.done = false;
        List<String> open = StoryCodas.lines(p, "steward");
        check("E2 an unfinished story's coda says so, and reads the flags (Wen visits)", open.stream().anyMatch(l -> l.contains("not quite finished")) && open.stream().anyMatch(l -> l.contains("Wen visits")));
        p.story = null;
        check("E3 a life with no story has no coda", StoryCodas.lines(p, "soldier").isEmpty() && StoryCodas.credit(p) == null);
    }


    // ------------------------------------------------------------------ the Palace's own text reads plainly
    static void loreText() {
        int lines = 0, words = 0, sentences = 0, longest = 0; String longestAt = ""; List<String> bad = new ArrayList<>();
        for (LoreText.Scene sc : LoreText.SCENES.values()) {
            for (String l : sc.lines) {
                lines++;
                String body = l.startsWith("* ") ? l.substring(2) : l.startsWith("OLD MAN: ") ? l.substring(9) : l;
                if (body.contains("  ")) bad.add(sc.id + ": double space");
                words += body.split("\\s+").length;
                for (String s : body.split("(?<=[.!?])\\s+")) {
                    sentences++; int n = s.split("\\s+").length;
                    if (n > longest) { longest = n; longestAt = sc.id; }
                    if (n > 45) bad.add(sc.id + ": sentence of " + n + " words");
                }
            }
        }
        double avg = sentences == 0 ? 0 : (double) words / sentences;
        PcProbe.real.println("[T] info: Palace text: " + LoreText.SCENES.size() + " scenes, " + lines + " lines, " + words + " words, " + String.format("%.1f", avg) + " words per sentence, longest " + longest + " (" + longestAt + ")");
        for (String b : bad) PcProbe.real.println("[T]   lore: " + b);
        check("L lore the Palace's scenes read plainly (" + String.format("%.1f", avg) + " words per sentence, under 17; " + bad.size() + " problems)", avg < 17 && bad.isEmpty());
        for (String w : LoreText.WHISPERS) { String body = w.substring(w.indexOf('|') + 1); if (body.split("\\s+").length > 35) bad.add("whisper too long: " + body); }
        check("L lore whispers are short", bad.isEmpty());
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        GameDriver.frozenNpcs = true;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        p.caution = 0; p.maxAge = 100000; p.pc().squadPolicy = 2; p.pc().friendPolicy = 1; p.age = 30; p.gender = "Male"; p.spouse = null;
        GameDriver.time = Math.max(GameDriver.time, 100);
        Map.currentCharacter = p;
        lint();
        loreText();
        areaInfo("start");
        generics(p);
        areaInfo("after the generic runs");
        codas(p);

        Starts.State s0 = null;
        play(p, "[A kill/kill/expose]", true, 0, true);
        // a second life on the other roads (the same body, a new story)
        p.dead = false; p.sect = null;
        play(p, "[B spare/duel+kill/-]", false, 1, false);
        p.dead = false; p.sect = null;
        play(p, "[C kill/duel+spare/spare Pell]", true, 2, false);
        p.dead = false; p.sect = null;
        play(p, "[D spare/deal]", false, 3, false);
    }
}
