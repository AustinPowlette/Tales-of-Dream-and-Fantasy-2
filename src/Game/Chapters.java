package Game;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** The chapters of a Story start (see "Story Plan.md"): a story is a list of chapters, each a pop-up scene (with an optional choice), a task, and a second
 *  scene when the task is done. The story's people are real characters ("fated" to the human: only that human can kill them, see Character.fatedTo)
 *  spawned when a chapter needs them, living in the world; the story's places are real tiles picked when first needed. What the human chose is kept as
 *  flags (Starts.State.flags) and changes later lines (a line may start with ?flag| or ?!flag| or ?flag=2| to show only then, see ChapText).
 *  The text of each story is data in its own file (TaleHeir ...); the engine is here. */
final class Chapters {
    private Chapters() { }

    // ------------------------------------------------------------------ the data model
    interface Cond { boolean ok(Character c, Starts.State st); }
    interface Act { void run(Character c, Starts.State st); }

    /** A question put after a scene: the answer is kept as flag = option index + 1 (0 = not asked yet); `after[i]` is read after option i. */
    static final class Choice {
        final String flag, prompt; final String[] options; String[][] after; int dflt = 0;
        Choice(String flag, String prompt, String... options) { this.flag = flag; this.prompt = prompt; this.options = options; }
        Choice after(String[]... lines) { this.after = lines; return this; }
        Choice dflt(int i) { this.dflt = i; return this; }
    }

    /** A person of the story. `home`: far (a city far from the human), near (a city a few tiles away), sect (a sect far away), wild (empty land far away),
     *  here (the human's tile), start (Originis), place:<key> (a place of the story). `offset` = levels above the human's when the person appears. */
    static final class Cast {
        final String key, first, last; boolean female; Character.Traits trait = Character.Traits.Friendly; String affil = "Neutral";
        int offset = 0, potBonus = 0, rel = 0, age = 0; String home = "far"; boolean fated = true, atStart = false;
        Cast(String key, String first, String last) { this.key = key; this.first = first; this.last = last; }
        Cast she() { female = true; return this; }
        Cast he() { female = false; return this; }
        Cast trait(Character.Traits t) { trait = t; return this; }
        Cast side(String a) { affil = a; return this; }
        Cast lvl(int offset) { this.offset = offset; return this; }
        Cast pot(int bonus) { potBonus = bonus; return this; }
        Cast rel(int r) { rel = r; return this; }
        Cast age(int a) { age = a; return this; }
        Cast at(String home) { this.home = home; return this; }
        Cast free() { fated = false; return this; }
        Cast start() { atStart = true; home = "start"; return this; }
    }

    static final class Chapter {
        final String id, title;
        int gap = 0, minAge = 0, minMajor = 0;
        Cond when, check, skip; Act setup, done;
        String[] intro = {}, outro = {};
        Choice choice, outChoice;
        String kind = "NONE", who = "", where = "", text = "";
        int n;
        long ss; int pot, pills, ppills, gear, insight; String prize = "";
        boolean episode; int odds = 1;       // an episode is drawn at random along the way (see Def.episode); it comes up about once in `odds` weeks once it may
        Chapter(String id, String title) { this.id = id; this.title = title; }
        Chapter odds(int n) { odds = n; return this; }
        Chapter gap(int weeks) { gap = weeks; return this; }
        Chapter years(int y) { gap = y * 52; return this; }
        Chapter age(int a) { minAge = a; return this; }
        Chapter major(int m) { minMajor = m; return this; }
        Chapter when(Cond w) { when = w; return this; }
        /** The chapter is left out altogether when this holds (the story took another road), checked when its turn comes. */
        Chapter skipIf(Cond s) { skip = s; return this; }
        Chapter setup(Act a) { setup = a; return this; }
        Chapter done(Act a) { done = a; return this; }
        Chapter intro(String... l) { intro = l; return this; }
        Chapter outro(String... l) { outro = l; return this; }
        Chapter choice(Choice c) { choice = c; return this; }
        Chapter outChoice(Choice c) { outChoice = c; return this; }
        Chapter task(String kind, int n, String who, String where, String text) { this.kind = kind; this.n = n; this.who = who; this.where = where; this.text = text; return this; }
        Chapter meet(String who, String text) { return task("MEET", 0, who, "", text); }
        Chapter defeat(String who, String text) { return task("DEFEAT", 0, who, "", text); }
        Chapter at(String place, String text) { return task("PLACE", 0, "", place, text); }
        Chapter realm(int n, String text) { return task("REALM", n, "", "", text); }
        Chapter wait(int weeks) { return task("WAIT", weeks, "", "", ""); }
        Chapter craft(int n, String text) { return task("CRAFT", n, "", "", text); }
        Chapter wealth(int n, String text) { return task("WEALTH", n, "", "", text); }
        Chapter friends(int n, String text) { return task("FRIENDS", n, "", "", text); }
        Chapter shops(int n, String text) { return task("SHOPS", n, "", "", text); }
        Chapter flag(String flag, String text) { return task("FLAG", 0, flag, "", text); }
        Chapter check(Cond c, String text) { this.check = c; return task("CHECK", 0, "", "", text); }
        /** What it pays: spirit stones, potential, Qi/Cultivation pills, Potential Pills, gear this many Majors above you (0 = none), technique insights. */
        Chapter pay(long ss, int pot, int pills, int ppills, int gear, int insight) { this.ss = ss; this.pot = pot; this.pills = pills; this.ppills = ppills; this.gear = gear; this.insight = insight; return this; }
        Chapter prize(String p) { prize = p; return this; }
    }

    static final class Def {
        final String id;
        final LinkedHashMap<String, Cast> cast = new LinkedHashMap<>();
        final LinkedHashMap<String, String> places = new LinkedHashMap<>();
        final List<Chapter> chapters = new ArrayList<>();
        final List<Chapter> episodes = new ArrayList<>();
        int episodeGap = 52 * 2, episodeMax = 0;   // weeks between two episodes, and how many in all
        String[] epilogue = {}; Act begin, finish;
        Def(String id) { this.id = id; }
        Cast cast(String key, String first, String last) { Cast c = new Cast(key, first, last); cast.put(key, c); return c; }
        Def place(String key, String selector) { places.put(key, selector); return this; }
        final java.util.Set<String> extraFlags = new java.util.HashSet<>();
        /** Flags the story sets itself (in begin / setup / done), so the text checker knows them. */
        Def flags(String... f) { for (String x : f) extraFlags.add(x); return this; }
        Chapter chapter(String id, String title) { Chapter c = new Chapter(id, title); chapters.add(c); return c; }
        /** A short tale that is not part of the line of chapters: drawn at random (once each) when its conditions hold, at most `max` of them, `gapYears` apart. */
        Chapter episode(String id, String title) { Chapter c = new Chapter(id, title); c.episode = true; episodes.add(c); return c; }
        Def episodes(int gapYears, int max) { episodeGap = gapYears * 52; episodeMax = max; return this; }
        Def epilogue(String... l) { epilogue = l; return this; }
        Def begin(Act a) { begin = a; return this; }
        Def finish(Act a) { finish = a; return this; }
        Chapter find(String chapterId) { for (Chapter c : chapters) if (c.id.equals(chapterId)) return c; for (Chapter c : episodes) if (c.id.equals(chapterId)) return c; return null; }
    }

    static final LinkedHashMap<String, Def> DEFS = new LinkedHashMap<>();
    static Def def(String id) { Def d = new Def(id); DEFS.put(id, d); return d; }
    /** The scenes shown, by title, newest last (tests read it). */
    static final List<String> shown = new ArrayList<>();
    /** Every line shown while a test drives the decisions (DecisionBroker.scripted set), for the checks on the words. */
    static final List<String> transcript = new ArrayList<>();

    static {
        TaleHeir.register();
        TaleScion.register();
        TaleOrphan.register();
        TaleDisgraced.register();
        TaleSmith.register();
        TaleTamer.register();
        TaleGhost.register();
        TaleLucky.register();
        TaleCrane.register();
        TaleDisciple.register();
    }

    static boolean has(String story) { return DEFS.containsKey(story); }

    // ------------------------------------------------------------------ flags
    static int flag(Starts.State st, String k) { Integer v = st.flags == null ? null : st.flags.get(k); return v == null ? 0 : v; }
    static void set(Starts.State st, String k, int v) { if (st.flags == null) st.flags = new java.util.HashMap<>(); st.flags.put(k, v); }

    // ------------------------------------------------------------------ starting
    /** A new life with this story: the flags, the places are picked when first needed, the people who stand in Originis from the first day appear. */
    static void begin(Character c, Starts.State st) {
        Def d = DEFS.get(st.story);
        st.v2 = true; st.chapter = 0; st.open = false; st.lastEnd = GameDriver.time;
        st.flags = new java.util.HashMap<>(); st.seen = new java.util.LinkedHashSet<>(); st.places = new java.util.HashMap<>();
        if (d == null) return;
        if (d.begin != null) d.begin.run(c, st);
        for (Cast sp : d.cast.values()) if (sp.atStart) spawn(c, st, sp.key);
    }

    // ------------------------------------------------------------------ the tick
    static void tick(Character c, Starts.State st) {
        Def d = DEFS.get(st.story);
        if (d == null) { st.done = true; return; }
        watch(c, st, d);
        ChapText.refresh(st);
        while (!st.open && st.chapter < d.chapters.size() && d.chapters.get(st.chapter).skip != null && d.chapters.get(st.chapter).skip.ok(c, st)) st.chapter++; // (the road the story did not take)
        if (st.chapter >= d.chapters.size()) { finish(c, st, d); return; }
        Chapter ch = st.open && st.cur != null ? d.find(st.cur) : d.chapters.get(st.chapter);
        if (ch == null) { st.open = false; return; }
        if (!st.open) {
            if (GuiLore.busy()) return;
            if (eligible(c, st, ch)) open(c, st, d, ch);
            else { Chapter ep = drawEpisode(c, st, d); if (ep == null) return; ch = ep; open(c, st, d, ep); }
        }
        if (st.open && done(c, st, ch)) close(c, st, d, ch);
        else if (st.open) ensureQuest(c, st, ch);
    }

    /** An episode that may come up now: its conditions hold, the gap since the last one is over, the quota is not met, and the dice fall. */
    private static Chapter drawEpisode(Character c, Starts.State st, Def d) {
        if (d.episodes.isEmpty() || flag(st, "episodes") >= d.episodeMax || GameDriver.time - st.lastEpisode < d.episodeGap) return null;
        List<Chapter> ready = new ArrayList<>();
        for (Chapter e : d.episodes) if (!st.seen.contains(e.id) && c.age >= e.minAge && c.majorLvl >= e.minMajor && (e.when == null || e.when.ok(c, st))) ready.add(e);
        if (ready.isEmpty()) return null;
        Chapter e = ready.get(GameDriver.rand.nextInt(ready.size()));
        return GameDriver.rand.nextInt(Math.max(1, e.odds)) == 0 ? e : null;
    }

    private static boolean eligible(Character c, Starts.State st, Chapter ch) {
        if (GameDriver.time - st.lastEnd < ch.gap) return false;
        if (c.age < ch.minAge || c.majorLvl < ch.minMajor) return false;
        return ch.when == null || ch.when.ok(c, st);
    }

    private static void open(Character c, Starts.State st, Def d, Chapter ch) {
        st.open = true; st.openedWeek = GameDriver.time; st.cur = ch.id;
        if (ch.setup != null) ch.setup.run(c, st);
        st.seen.add(ch.id);
        stopWalking(c);
        play(c, st, ch.title, ch.intro, ch.choice);
        if (!ch.kind.equals("NONE") && !ch.kind.equals("WAIT") && ch.text.length() > 0) {
            String msg = "Story, chapter \"" + ch.title + "\": " + ChapText.fill(ch.text, c, st);
            if (c.player > 0 && c.skipping <= 0) System.out.println(msg);
            Notifications.post("alert", msg, -1, c.id);
        }
    }

    private static void close(Character c, Starts.State st, Def d, Chapter ch) {
        removeQuest(c, st);
        if (ch.outro.length > 0 || ch.outChoice != null) { st.seen.add(ch.id + ".out"); stopWalking(c); play(c, st, ch.title, ch.outro, ch.outChoice); }
        pay(c, ch);
        if (ch.done != null) ch.done.run(c, st);
        st.open = false; st.cur = null;
        if (ch.episode) { st.lastEpisode = GameDriver.time; set(st, "episodes", flag(st, "episodes") + 1); }
        else { st.chapter++; st.lastEnd = GameDriver.time; }
        String paid = payText(ch);
        if (paid.length() > 0 && c.player > 0 && c.skipping <= 0) System.out.println("Story: \"" + ch.title + "\" is done. " + paid);
        if (paid.length() > 0) Notifications.post("alert", "Story: \"" + ch.title + "\" is done. " + paid, -1, c.id);
        if (!ch.episode && st.chapter >= d.chapters.size()) finish(c, st, d);
    }

    private static void finish(Character c, Starts.State st, Def d) {
        if (st.done) return;
        removeQuest(c, st);
        if (d.epilogue.length > 0) { st.seen.add("epilogue"); stopWalking(c); play(c, st, "Epilogue", d.epilogue, null); }
        if (d.finish != null) d.finish.run(c, st);
        st.done = true; st.open = false;
        for (int id : new ArrayList<>(st.npcs.values())) { Character n = Character.charById(id); if (n != null) n.fatedTo = 0; } // the people of the story are ordinary people now
        Starts.Story s = Starts.STORIES.get(st.story);
        String msg = "Your story \"" + (s == null ? st.story : s.title) + "\" is complete. The rest of your life is yours to write.";
        if (c.player > 0 && c.skipping <= 0) System.out.println(msg);
        Notifications.post("breakthrough", msg, -1, c.id);
        Chronicle.add("world", c.name + " " + c.lastName + " came to the end of a long story: " + (s == null ? st.story : s.title) + ".", c.id);
    }

    private static void stopWalking(Character c) {
        if (c.player > 0) { c.skipping = 0; PlayerControl pc = c.pc(); if (pc != null) pc.watching = false; }
    }

    // ------------------------------------------------------------------ the task
    static boolean done(Character c, Starts.State st, Chapter ch) {
        switch (ch.kind) {
            case "NONE": return true;
            case "WAIT": return GameDriver.time - st.openedWeek >= ch.n;
            case "MEET": { Character n = person(st, ch.who); return n == null || n.dead || (c.location != null && n.location == c.location); }
            case "DEFEAT": { Character n = person(st, ch.who); return n == null || n.dead || flag(st, "beaten." + ch.who) != 0; }
            case "PLACE": { Area a = place(c, st, ch.where); return a == null || c.location == a; }
            case "FLAG": return flag(st, ch.who) != 0;
            case "SHOPS": return c.ownedShops != null && c.ownedShops.size() >= ch.n;
            case "CHECK": return ch.check != null && ch.check.ok(c, st);
            default: return Starts.complete(c, st, new Starts.Step(ch.kind, ch.n, ch.who, ch.text, "", 0, 0, 0, 0));
        }
    }

    // ------------------------------------------------------------------ the quest in the Quest Log
    private static void ensureQuest(Character c, Starts.State st, Chapter ch) {
        if (ch.text.length() == 0 || ch.kind.equals("NONE") || ch.kind.equals("WAIT") || ch.kind.equals("FLAG")) return;
        for (Quest q : c.targetQuests) if (q.uid == st.questUid) return;
        for (Quest q : c.spotQuests) if (q.uid == st.questUid) return;
        if (c.currentQuest != null && c.currentQuest.uid == st.questUid) return;
        String label = "Story: " + ChapText.fill(ch.text, c, st);
        Character npc = ch.kind.equals("MEET") || ch.kind.equals("DEFEAT") ? person(st, ch.who) : null;
        Area at = ch.kind.equals("PLACE") ? place(c, st, ch.where) : null;
        Quest q;
        if (npc != null && !npc.dead) q = new Quest(label, true, 90, 0, 90, npc);
        else if (at != null) q = new Quest(label, true, 90, 0, 90, at);
        else q = new Quest(label, true, 90, 0, 90);
        q.userCreated = true;
        q.pin(90);
        if (q.category == Quest.QuestCategory.Spot) c.spotQuests.add(q); else c.targetQuests.add(q);
        st.questUid = q.uid;
    }

    private static void removeQuest(Character c, Starts.State st) {
        c.targetQuests.removeIf(q -> q.uid == st.questUid);
        c.spotQuests.removeIf(q -> q.uid == st.questUid);
        if (c.currentQuest != null && c.currentQuest.uid == st.questUid) c.currentQuest = null;
        st.questUid = -1;
    }

    // ------------------------------------------------------------------ rewards
    static void pay(Character c, Chapter ch) {
        if (ch.pot > 0) c.potential = Math.min(100, c.potential + ch.pot);
        if (ch.ss > 0) { c.ss += ch.ss; c.convertCurrency(); }
        if (ch.pills > 0) Starts.givePills(c, Item.PillType.Cultivation, Math.max(1, Math.min(10, c.majorLvl + 1)), ch.pills);
        if (ch.ppills > 0) Starts.givePills(c, Item.PillType.Potential, 1, ch.ppills);
        if (ch.gear > 0) Starts.giveGear(c, Math.max(1, Math.min(10, c.majorLvl + ch.gear)));
        for (int i = 0; i < ch.insight && c.martialTechnique != null; i++) c.martialTechnique.upgrade(c);
    }

    static String payText(Chapter ch) {
        List<String> p = new ArrayList<>();
        if (ch.prize.length() > 0) p.add(ch.prize);
        if (ch.ss > 0) p.add(String.format("%,d spirit stones", ch.ss));
        if (ch.pot > 0) p.add("+" + ch.pot + " potential");
        if (ch.pills > 0) p.add(ch.pills + " Cultivation Pill" + (ch.pills > 1 ? "s" : ""));
        if (ch.ppills > 0) p.add(ch.ppills + " Potential Pill" + (ch.ppills > 1 ? "s" : ""));
        if (ch.gear > 0) p.add("gear a Major above you");
        if (ch.insight > 0) p.add("a technique insight");
        return p.isEmpty() ? "" : "Reward: " + String.join(", ", p) + ".";
    }

    // ------------------------------------------------------------------ the people
    static Character person(Starts.State st, String key) {
        Integer id = st.npcs.get(key);
        return id == null ? null : Character.charById(id);
    }

    /** The person of the story with this key: found if they live, created (in the place their entry says, a level or more above the human) otherwise. */
    static Character spawn(Character c, Starts.State st, String key) {
        Character have = person(st, key);
        if (have != null) return have;
        Def d = DEFS.get(st.story);
        Cast sp = d == null ? null : d.cast.get(key);
        if (sp == null) return null;
        Area home = homeFor(c, st, sp);
        if (home == null) return null;
        Character n = new Character(home);
        Naming.releaseName(n.name, n.lastName);
        n.name = sp.first; n.lastName = sp.last; n.gender = sp.female ? "Female" : "Male";
        Naming.claimName(sp.first, sp.last);
        n.trait = sp.trait;
        n.affiliation = sp.affil; n.affiliationNum = sp.affil.equals("Good") ? 1 : sp.affil.equals("Evil") ? -1 : 0;
        int hard = st.difficulty;
        n.potential = Math.max(30, Math.min(98, c.potential + sp.potBonus + hard * 4));
        n.cultivationBaseMax = Math.pow(Math.max(1, n.lvl), 3) * (((70. + 1. / 3.) + ((11. / 6.) * Math.max(1, n.lvl))) / 25.);
        n.cultivation = (Math.pow(n.potential, 2) * Math.sqrt(Math.max(1, n.lvl)) * 10.) / (52. * Math.sqrt(n.potential) * Math.pow(2, n.inhibitor));
        raise(n, Math.max(3, c.lvl + sp.offset + hard * 3));
        n.age = Math.max(Character.ADULT_AGE + 2, sp.age > 0 ? sp.age : GameDriver.START_AGE + 6 + GameDriver.rand.nextInt(14) + n.majorLvl * 4);
        n.age = Math.min(n.age, Math.max(Character.ADULT_AGE + 2, n.maxAge / 2));
        n.time = GameDriver.rand.nextInt(52);
        Starts.giveGear(n, Math.max(1, n.majorLvl));
        n.storyNpc = true;
        if (sp.fated) n.fatedTo = c.id + 1;
        while (Map.characters.size() < n.id) Map.characters.add(null);
        Map.characters.add(n);
        Map.population++;
        if (sp.rel != 0) n.changeRelationship(c, sp.rel);
        st.npcs.put(key, n.id);
        return n;
    }

    /** Cultivates a made person until they stand at `target` (they are made a little above the human, with the potential to stay there). */
    static void raise(Character n, int target) {
        int pl = n.player; n.player = 0;
        try { for (int i = 0; i < 150000 && n.lvl < target; i++) n.cultivate(); } finally { n.player = pl; }
    }

    /** Called once a tick: a person of the story who is about to die of old age is let go (the story goes on with their heirs; see the chapter text). */
    private static void watch(Character c, Starts.State st, Def d) {
        for (java.util.Map.Entry<String, Integer> e : st.npcs.entrySet()) {
            Character n = Character.charById(e.getValue());
            if (n == null || n.dead || n.fatedTo == 0) continue;
            if (n.age >= n.maxAge - 2) { n.fatedTo = 0; set(st, "aged." + e.getKey(), 1); }
        }
    }

    /** A fight the human won against a person of their story: the foe is "beaten" whether they are then killed or spared. */
    static void beaten(Character winner, Character loser) {
        Starts.State st = winner.story;
        if (st == null || !st.v2) return;
        for (java.util.Map.Entry<String, Integer> e : st.npcs.entrySet()) if (e.getValue() == loser.id) set(st, "beaten." + e.getKey(), 1);
    }

    // ------------------------------------------------------------------ the places
    static Area place(Character c, Starts.State st, String key) {
        Def d = DEFS.get(st.story);
        if (key == null || key.isEmpty()) return null;
        int[] xy = st.places == null ? null : st.places.get(key);
        if (xy == null) {
            String sel = d == null ? "far" : d.places.get(key);
            Area a = pick(c, st, sel == null ? "far" : sel);
            if (a == null) return null;
            if (st.places == null) st.places = new java.util.HashMap<>();
            st.places.put(key, new int[]{a.x, a.y});
            return a;
        }
        return Map.tiles[xy[0]][xy[1]];
    }

    static String placeName(Character c, Starts.State st, String key) {
        Area a = place(c, st, key);
        return a == null ? "a place you will hear of" : (a.name == null || a.name.isEmpty() ? "the land at (" + a.x + "," + a.y + ")" : a.name);
    }

    private static Area homeFor(Character c, Starts.State st, Cast sp) {
        String h = sp.home;
        if (h.equals("start")) return Map.tiles[Map.range / 2][Map.range / 2];
        if (h.equals("here")) return c.location;
        if (h.startsWith("place:")) return place(c, st, h.substring(6));
        Area a = pick(c, st, h);
        return a != null ? a : c.location;
    }

    /** A young world has few explored tiles: makes sure there are some empty land tiles to pick from (creating a few the way exploring would). */
    private static void ensureWild(Character c) {
        int have = 0;
        for (Area a : new ArrayList<>(Map.createdAreas)) if (a != null && !a.city && a.sect == null && a.dungeonType == null && !"Ocean".equals(a.biomeM)) have++;
        Character saved = Map.currentCharacter;
        if (saved == null) Map.currentCharacter = c;
        try {
            for (int tries = 0; have < 8 && tries < 120; tries++) {
                int x = GameDriver.rand.nextInt(Map.range), y = GameDriver.rand.nextInt(Map.range);
                synchronized (Map.tileLock) {
                    if (Map.tiles[x][y] != null) continue;
                    Map.tiles[x][y] = new Area(Area.ghost(x == 0 ? Map.range - 1 : x - 1, y), 'e');
                    Area a = Map.tiles[x][y];
                    if (!a.city && a.sect == null && a.dungeonType == null && !"Ocean".equals(a.biomeM)) have++;
                }
            }
        } finally { if (saved == null) Map.currentCharacter = null; }
    }

    /** A tile: far / near (cities), sect (a sect far away), wild (empty land far away), dungeon (a delve far away). */
    static Area pick(Character c, Starts.State st, String sel) {
        List<Area> pool = new ArrayList<>();
        Area mid = Map.tiles[Map.range / 2][Map.range / 2];
        if (sel.equals("start")) return mid; // Originis
        if (sel.startsWith("wild")) ensureWild(c);
        for (Area a : new ArrayList<>(Map.createdAreas)) {
            if (a == null || a == mid || a == c.location) continue;
            boolean used = false; // a place of the story is not another place of the story
            if (st != null && st.places != null) for (int[] p : st.places.values()) if (p[0] == a.x && p[1] == a.y) used = true;
            if (used) continue;
            boolean ocean = "Ocean".equals(a.biomeM);
            switch (sel) {
                case "far": case "near": if (a.city) pool.add(a); break;
                case "sect": if (a.sect != null && !a.sect.palace && !a.sect.virtual) pool.add(a); break;
                case "dungeon": if (a.dungeonType != null) pool.add(a); break;
                default: if (!a.city && a.sect == null && !ocean && a.dungeonType == null) pool.add(a); break;
            }
        }
        if (pool.isEmpty()) return null;
        int best = 0;
        for (Area a : pool) best = Math.max(best, PlayerControl.distance(c.location, a));
        List<Area> pick = new ArrayList<>();
        boolean nearish = sel.equals("near") || sel.equals("wildnear");
        int lo = nearish ? 2 : (int) (best * 0.55), hi = nearish ? 7 : best + 1;
        for (Area a : pool) { int dd = PlayerControl.distance(c.location, a); if (dd >= lo && dd <= hi) pick.add(a); }
        if (pick.isEmpty()) pick = pool;
        return pick.get(GameDriver.rand.nextInt(pick.size()));
    }

    // ------------------------------------------------------------------ showing a scene
    /** Shows a scene (a window in the GUI, a framed block in the terminal) and, if the chapter asks one, the question; keeps the answer. Returns the 0-based answer (-1 if none). */
    static int play(Character c, Starts.State st, String title, String[] raw, Choice ch) {
        List<String> lines = ChapText.prepare(raw, c, st);
        int pick = -1;
        if (lines.isEmpty() && ch == null) return -1;
        shown.add(title);
        if (DecisionBroker.scripted != null && transcript.size() < 20000) transcript.addAll(lines);   // (tests read what was shown)
        if (c.player > 0 && c.skipping <= 0) System.out.println("[Story] " + title + "  ('story replay' reads it again)");
        boolean gui = GameDriver.guiEnabled && GameDriver.mapViewer != null && DecisionBroker.scripted == null;
        if (ch != null) {
            String prompt = ChapText.fill(ch.prompt, c, st);
            if (gui && DecisionBroker.promptable(c)) pick = GuiChapter.decide(c, title, lines, prompt, ch.options, ch.dflt);
            else {
                if (gui) GuiChapter.scene(title, lines); else if (DecisionBroker.scripted == null && c.player > 0) System.out.println(ChapText.frame(title, lines));
                pick = DecisionBroker.ask(c, prompt, ch.options, ch.dflt);
            }
            set(st, ch.flag, pick + 1);
            if (ch.after != null && pick >= 0 && pick < ch.after.length && ch.after[pick] != null && ch.after[pick].length > 0) {
                List<String> more = ChapText.prepare(ch.after[pick], c, st);
                if (!more.isEmpty()) { if (gui) GuiChapter.scene(title, more); else if (DecisionBroker.scripted == null && c.player > 0) System.out.println(ChapText.frame(title, more)); }
            }
        } else if (gui) GuiChapter.scene(title, lines);
        else if (DecisionBroker.scripted == null && c.player > 0) System.out.println(ChapText.frame(title, lines));
        return pick;
    }

    // ------------------------------------------------------------------ the `story` command
    static String describe(Character c, Starts.State st, Starts.Story s) {
        Def d = DEFS.get(st.story);
        StringBuilder sb = new StringBuilder("=== " + s.title + " — " + Starts.DIFFICULTY_NAMES[st.difficulty] + " ===\n" + s.blurb + "\n\n");
        if (!st.hooked) { sb.append("A pure challenge: this Brutal start has no hook and no chapters.\n"); return sb.toString(); }
        if (d == null) return sb.toString();
        Chapter openCh = st.open && st.cur != null ? d.find(st.cur) : null;
        boolean mainOpen = openCh != null && !openCh.episode;
        for (int i = 0; i < d.chapters.size(); i++) {
            Chapter ch = d.chapters.get(i);
            if (i < st.chapter || st.done) sb.append("  [x] ").append(ch.title).append('\n');
            else if (i == st.chapter && mainOpen) sb.append("  [>] ").append(ch.title).append(ch.text.length() > 0 ? " — " + ChapText.fill(ch.text, c, st) : "").append('\n');
            else if (i == st.chapter) sb.append("  [ ] Chapter ").append(i + 1).append(": waiting for the right time").append('\n');
            else sb.append("  [ ] ???\n");
        }
        if (!d.episodes.isEmpty() && flag(st, "episodes") > 0) sb.append("\n  Tales met on the road: ").append(flag(st, "episodes")).append('\n');
        if (st.done) sb.append("\nStory complete.\n");
        sb.append("\n'story replay' lists the scenes you can read again; 'story replay <id>' reads one.\n");
        return sb.toString();
    }

    /** The chapter list for the Story window: {state, title, task text}, state = done / now / wait / later. */
    static List<String[]> rows(Character c, Starts.State st) {
        List<String[]> out = new ArrayList<>();
        Def d = DEFS.get(st.story);
        if (d == null) return out;
        Chapter openCh = st.open && st.cur != null ? d.find(st.cur) : null;
        boolean mainOpen = openCh != null && !openCh.episode;
        for (int i = 0; i < d.chapters.size(); i++) {
            Chapter ch = d.chapters.get(i);
            String state = i < st.chapter || st.done ? "done" : i == st.chapter && mainOpen ? "now" : i == st.chapter ? "wait" : "later";
            out.add(new String[]{state, ch.title, state.equals("now") ? ChapText.fill(ch.text, c, st) : ""});
        }
        return out;
    }

    /** The people of the story that exist in the world now (alive or dead), in the order the story made them. */
    static List<Character> people(Starts.State st) {
        List<Character> out = new ArrayList<>();
        if (st == null || st.npcs == null) return out;
        for (int id : new ArrayList<>(st.npcs.values())) { Character n = Character.charById(id); if (n != null) out.add(n); }
        return out;
    }

    static int episodesMet(Starts.State st) { return flag(st, "episodes"); }

    /** The scenes the human has seen, as "id  -  Title" (for the Story window's list). */
    static List<String> sceneList(Starts.State st) {
        List<String> out = new ArrayList<>();
        if (st == null || !st.v2 || st.seen == null) return out;
        Def d = DEFS.get(st.story);
        for (String id : new ArrayList<>(st.seen)) {
            String base = id.endsWith(".out") ? id.substring(0, id.length() - 4) : id;
            Chapter ch = d == null ? null : d.find(base);
            out.add(id + "  -  " + (id.equals("epilogue") ? "Epilogue" : ch == null ? base : ch.title + (id.endsWith(".out") ? " (afterwards)" : "")));
        }
        return out;
    }

    /** `story replay [id]`: the scenes seen (chapter ids), or one of them read again. */
    static String replay(Character c, Starts.State st, String id) {
        Def d = DEFS.get(st.story);
        if (d == null || st.seen == null) return "There is nothing to read again yet.";
        if (id == null || id.isEmpty()) {
            StringBuilder sb = new StringBuilder("Scenes you can read again ('story replay <id>'):\n");
            for (String s : st.seen) sb.append("  ").append(s).append('\n');
            return sb.toString();
        }
        boolean out = id.endsWith(".out");
        Chapter ch = id.equals("epilogue") ? null : d.find(out ? id.substring(0, id.length() - 4) : id);
        if (!st.seen.contains(id)) return "You have not seen a scene called '" + id + "'.";
        String[] raw = id.equals("epilogue") ? d.epilogue : out ? ch.outro : ch.intro;
        String title = id.equals("epilogue") ? "Epilogue" : ch.title;
        List<String> lines = ChapText.prepare(raw, c, st);
        if (GameDriver.guiEnabled && GameDriver.mapViewer != null) { GuiChapter.scene(title, lines); return "(the scene is open in a window)"; }
        return ChapText.frame(title, lines);
    }
}
