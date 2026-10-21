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
    /** How far along a counted task is (the number shown as "made 12 of 25"). */
    interface Count { int value(Character c, Starts.State st); }

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
        boolean keep;                          // a companion of the story: lives long enough to see it through (+60 years of life)
        boolean opposite; String maleFirst;   // a person to love is always of the other gender than the human (see isFemale); `maleFirst` is the name when that person is a man
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
        /** A companion who must still be there at the end of a long story (three seasons): their lifespan is made 60 years longer. */
        Cast keep() { keep = true; return this; }
        Cast start() { atStart = true; home = "start"; return this; }
        /** The person's gender follows the human's: always the other one (a person who may be loved). `maleFirst` = the first name when the person is a man (null = the same name). */
        Cast opposite(String maleFirst) { this.opposite = true; this.maleFirst = maleFirst; return this; }
        Cast opposite() { return opposite(null); }
    }

    static final class Chapter {
        final String id, title;
        int gap = 0, minAge = 0, minMajor = 0, hardMajor = 0;
        Cond when, check, skip; Act setup, done;
        Count counter; int counterN;           // a counted task shows how far along it is in the Story window and the Quest Log
        String[] intro = {}, outro = {};
        Choice choice, outChoice;
        String kind = "NONE", who = "", where = "", text = "";
        int n;
        long ss; int pot, pills, ppills, gear, insight; String prize = "";
        boolean episode; int odds = 1;       // an episode is drawn at random along the way (see Def.episode); it comes up about once in `odds` weeks once it may
        int season = 1;                      // the season this chapter belongs to (Def.season)
        Cond soft; String softNeeds = "", needs = "";   // `soft`: a wish the story waits for, but not beyond PATIENCE; `needs`: what the Story window says a `when` waits for
        Chapter(String id, String title) { this.id = id; this.title = title; }
        Chapter odds(int n) { odds = n; return this; }
        Chapter gap(int weeks) { gap = weeks; return this; }
        Chapter years(int y) { gap = y * 52; return this; }
        Chapter age(int a) { minAge = a; return this; }
        Chapter major(int m) { minMajor = m; return this; }
        /** A Major level the chapter really waits for: unlike `major`, this never lapses with the patience (the Door chapters wait for Ascension, Major 7). */
        Chapter hardMajor(int m) { hardMajor = m; return this; }
        Chapter when(Cond w) { when = w; return this; }
        /** A condition the story is glad to wait for (a sect of five, a city) and gives up on after PATIENCE: the Story window shows `text` while it waits. */
        Chapter soft(Cond s, String text) { soft = s; softNeeds = text; return this; }
        /** What a hard `when` waits for, in words for the Story window. */
        Chapter needs(String text) { needs = text; return this; }
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
        /** Make `n` items **since the chapter began** (the chapter's setup stores the count in the flag "craft0"); the Quest Log and the Story window say "made 12 of 25 since this chapter began". */
        Chapter made(int n, String text) {
            this.check = (c, st) -> c.itemsCrafted - Chapters.flag(st, "craft0") >= n;
            this.counter = (c, st) -> Math.max(0, c.itemsCrafted - Chapters.flag(st, "craft0"));
            this.counterN = n;
            return task("CHECK", 0, "", "", text);
        }
        /** What it pays: spirit stones, potential, Qi/Cultivation pills, Potential Pills, gear this many Majors above you (0 = none), technique insights. */
        Chapter pay(long ss, int pot, int pills, int ppills, int gear, int insight) { this.ss = ss; this.pot = pot; this.pills = pills; this.ppills = ppills; this.gear = gear; this.insight = insight; return this; }
        Chapter prize(String p) { prize = p; return this; }
    }

    /** A season: a story with its own title, tone and interlude ("Season 2: The Silent Tonic"). The chapters of all seasons sit in one list in order; each knows its season. */
    static final class Season {
        final int n; final String title; String interludeTitle = "Some years later"; String[] interlude = {};
        Season(int n, String title) { this.n = n; this.title = title; }
    }

    /** The longest a chapter keeps the human waiting for a Major level or a wish after its gap is over: then it opens anyway (the story is not to hang on a rare thing). */
    static final int PATIENCE = 52 * 6;
    /** The longest a "reach Major Lvl N" task of a story (N of 2 or more) holds a chapter open. */
    static final int REALM_WAIVER = 52 * 8;
    /** A "make N items" task (`made`) is waived after this many weeks: a Musician, Cheater or Inventor with nothing to craft from would otherwise be stuck for ever. */
    static final int MADE_WAIVER = 52 * 4;
    /** A "hold N spirit stones" task is waived after this many weeks (a Miner who sells nothing would otherwise be stuck). */
    static final int WEALTH_WAIVER = 52 * 6;

    static final class Def {
        final String id;
        final LinkedHashMap<String, Cast> cast = new LinkedHashMap<>();
        final LinkedHashMap<String, String> places = new LinkedHashMap<>();
        final List<Chapter> chapters = new ArrayList<>();
        final List<Chapter> episodes = new ArrayList<>();
        int episodeGap = 52 * 2, episodeMax = 0;   // weeks between two episodes, and how many in all (season 1; later seasons: see seasonEpisodes)
        final java.util.HashMap<Integer, Integer> seasonEpisodes = new java.util.HashMap<>();   // season -> how many road tales it holds
        final List<Season> seasons = new ArrayList<>();
        int curSeason = 1;
        String[] epilogue = {}; Act begin, finish;
        Def(String id) { this.id = id; }
        Cast cast(String key, String first, String last) { Cast c = new Cast(key, first, last); cast.put(key, c); return c; }
        Def place(String key, String selector) { places.put(key, selector); return this; }
        final java.util.Set<String> extraFlags = new java.util.HashSet<>();
        /** Flags the story sets itself (in begin / setup / done), so the text checker knows them. */
        Def flags(String... f) { for (String x : f) extraFlags.add(x); return this; }
        Chapter chapter(String id, String title) { Chapter c = new Chapter(id, title); c.season = curSeason; chapters.add(c); return c; }
        /** The chapters declared after this call belong to season `n` (a season is a story of its own: 6-10 chapters, its own mode and tone). */
        Season season(int n, String title) { Season s = new Season(n, title); seasons.add(s); curSeason = n; return s; }
        Season seasonOf(int n) { for (Season s : seasons) if (s.n == n) return s; return null; }
        /** The scene played when season `n` ends and another follows ("some years later"); the epilogue is for the last season only. */
        Def interlude(int n, String title, String... lines) { Season s = seasonOf(n); if (s != null) { s.interludeTitle = title; s.interlude = lines; } return this; }
        int firstOfSeason(int n) { for (int i = 0; i < chapters.size(); i++) if (chapters.get(i).season == n) return i; return -1; }
        /** A short tale that is not part of the line of chapters: drawn at random (once each) when its conditions hold, at most `max` of them, `gapYears` apart. */
        Chapter episode(String id, String title) { Chapter c = new Chapter(id, title); c.episode = true; c.season = curSeason; episodes.add(c); return c; }
        Def episodes(int gapYears, int max) { episodeGap = gapYears * 52; if (curSeason == 1) episodeMax = max; else seasonEpisodes.put(curSeason, max); return this; }
        int episodeMaxFor(int season) { return season == 1 ? episodeMax : seasonEpisodes.getOrDefault(season, 0); }
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
        TaleChef.register();
        TalePoisoner.register();
        TaleTailor.register();
        TaleInventor.register();
        TaleMusician.register();
        TaleMiner.register();
        TaleCheater.register();
        TaleForager.register();
        for (Def d : new ArrayList<>(DEFS.values())) StoryDoors.add(d);   // the last season of every story, after any extra seasons
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
        int season = st.chapter < d.chapters.size() ? d.chapters.get(st.chapter).season : 1;
        String epKey = season == 1 ? "episodes" : "episodes." + season;
        if (d.episodes.isEmpty() || flag(st, epKey) >= d.episodeMaxFor(season) || GameDriver.time - st.lastEpisode < d.episodeGap) return null;
        List<Chapter> ready = new ArrayList<>();
        for (Chapter e : d.episodes) if (e.season == season && !st.seen.contains(e.id) && c.age >= e.minAge && c.majorLvl >= e.minMajor && (e.when == null || e.when.ok(c, st))) ready.add(e);
        if (ready.isEmpty()) return null;
        Chapter e = ready.get(GameDriver.rand.nextInt(ready.size()));
        return GameDriver.rand.nextInt(Math.max(1, e.odds)) == 0 ? e : null;
    }

    private static boolean eligible(Character c, Starts.State st, Chapter ch) {
        int waited = GameDriver.time - st.lastEnd;
        if (waited < ch.gap) return false;
        if (c.age < ch.minAge) return false;
        if (c.majorLvl < ch.hardMajor) return false;
        boolean patient = waited >= ch.gap + PATIENCE;   // a story does not hang on a rare Major level or a wish for ever
        if (c.majorLvl < ch.minMajor && !patient) return false;
        if (ch.soft != null && !patient && !ch.soft.ok(c, st)) return false;
        return ch.when == null || ch.when.ok(c, st);
    }

    /** What the story waits for, in words (the Story window and `story`): the years left of the gap, the age, the Major level, the wish. */
    static String waiting(Character c, Starts.State st, Chapter ch) {
        List<String> parts = new ArrayList<>();
        int waited = GameDriver.time - st.lastEnd, left = ch.gap - waited;
        if (left > 0) parts.add(left >= 52 * 2 ? "about " + (left + 26) / 52 + " more years" : left >= 52 ? "about a year" : left >= 9 ? "a few more months" : "a few more weeks");
        if (c.age < ch.minAge) parts.add("you to be " + ch.minAge);
        if (c.majorLvl < ch.hardMajor) parts.add("Major Lvl " + ch.hardMajor + " (the Door opens at Ascension)");
        boolean patient = waited >= ch.gap + PATIENCE;
        if (c.majorLvl < ch.minMajor && !patient) parts.add("Major Lvl " + ch.minMajor + " (or " + Math.max(1, (ch.gap + PATIENCE - waited + 51) / 52) + " more years)");
        if (ch.soft != null && !patient && !ch.soft.ok(c, st) && ch.softNeeds.length() > 0) parts.add(ch.softNeeds);
        if (ch.when != null && !ch.when.ok(c, st) && ch.needs.length() > 0) parts.add(ch.needs);
        return parts.isEmpty() ? "waiting for the right time" : "waiting for " + String.join(", and ", parts);
    }

    /** The text of a chapter's task for the human: with the count when the task is a counted one ("... [made 12 of 25 since this chapter began]"). */
    static String taskText(Chapter ch, Character c, Starts.State st) {
        String t = ChapText.fill(ch.text, c, st);
        if (ch.counter != null && t.length() > 0) t += "  [made " + Math.min(ch.counterN, ch.counter.value(c, st)) + " of " + ch.counterN + " since this chapter began]";
        return t;
    }

    private static void open(Character c, Starts.State st, Def d, Chapter ch) {
        st.open = true; st.openedWeek = GameDriver.time; st.cur = ch.id;
        if (ch.setup != null) ch.setup.run(c, st);
        st.seen.add(ch.id);
        stopWalking(c);
        String[] intro = ch.intro;
        Season sn = d.seasonOf(ch.season);
        if (!ch.episode && ch.season > 1 && sn != null && d.firstOfSeason(ch.season) == st.chapter) {   // the first chapter of a later season opens with its title
            intro = new String[ch.intro.length + 1]; intro[0] = "* Season " + ch.season + ": " + sn.title; System.arraycopy(ch.intro, 0, intro, 1, ch.intro.length);
            Chronicle.add("world", c.name + " " + c.lastName + " began a new season of their story: " + sn.title + ".", c.id);
        }
        play(c, st, ch.title, intro, ch.choice, artKey(st, d, ch));
        if (!ch.kind.equals("NONE") && !ch.kind.equals("WAIT") && ch.text.length() > 0) {
            String msg = "Story, chapter \"" + ch.title + "\": " + taskText(ch, c, st);
            if (c.player > 0 && c.skipping <= 0) System.out.println(msg);
            Notifications.post("alert", msg, -1, c.id);
        }
    }

    private static void close(Character c, Starts.State st, Def d, Chapter ch) {
        removeQuest(c, st);
        if (ch.outro.length > 0 || ch.outChoice != null) { st.seen.add(ch.id + ".out"); stopWalking(c); play(c, st, ch.title, ch.outro, ch.outChoice, artKey(st, d, ch)); }
        pay(c, ch);
        if (ch.done != null) ch.done.run(c, st);
        st.open = false; st.cur = null;
        if (ch.episode) { String epKey = ch.season == 1 ? "episodes" : "episodes." + ch.season; st.lastEpisode = GameDriver.time; set(st, epKey, flag(st, epKey) + 1); }
        else { st.chapter++; st.lastEnd = GameDriver.time; }
        String paid = payText(ch);
        if (paid.length() > 0 && c.player > 0 && c.skipping <= 0) System.out.println("Story: \"" + ch.title + "\" is done. " + paid);
        if (paid.length() > 0) Notifications.post("alert", "Story: \"" + ch.title + "\" is done. " + paid, -1, c.id);
        if (!ch.episode && st.chapter >= d.chapters.size()) finish(c, st, d);
        else if (!ch.episode && d.chapters.get(st.chapter).season != ch.season) interlude(c, st, d, ch.season);   // a season is over and another follows
    }

    /** The scene between two seasons, and a line in the log. */
    private static void interlude(Character c, Starts.State st, Def d, int season) {
        Season sn = d.seasonOf(season);
        if (sn != null && sn.interlude.length > 0) { st.seen.add("interlude." + season); stopWalking(c); play(c, st, sn.interludeTitle, sn.interlude, null); }
        String msg = "Season " + season + (sn == null || sn.title.isEmpty() ? "" : ", \"" + sn.title + "\",") + " of your story is over. The next one waits for its time.";
        if (c.player > 0 && c.skipping <= 0) System.out.println(msg);
        Notifications.post("breakthrough", msg, -1, c.id);
    }

    private static void finish(Character c, Starts.State st, Def d) {
        if (st.done) return;
        removeQuest(c, st);
        if (d.epilogue.length > 0) { st.seen.add("epilogue"); stopWalking(c); play(c, st, "Epilogue", d.epilogue, null); }
        if (d.finish != null) d.finish.run(c, st);
        st.done = true; st.open = false;
        for (int id : new ArrayList<>(st.npcs.values())) { Character n = Character.charById(id); if (n != null) { n.fatedTo = 0; n.noMarry = false; } } // the people of the story are ordinary people now
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
            case "CHECK": return (ch.check != null && ch.check.ok(c, st)) || (ch.counter != null && GameDriver.time - st.openedWeek >= MADE_WAIVER);   // (a counted task lapses after four years)
            case "WEALTH": if (GameDriver.time - st.openedWeek >= WEALTH_WAIVER) return true;   // (a story does not hang on money for ever)
                return Starts.complete(c, st, new Starts.Step(ch.kind, ch.n, ch.who, ch.text, "", 0, 0, 0, 0));
            case "REALM": if (ch.n >= 2 && GameDriver.time - st.openedWeek >= REALM_WAIVER) return true;   // (the story will not wait ever longer for a rare level)
                return Starts.complete(c, st, new Starts.Step(ch.kind, ch.n, ch.who, ch.text, "", 0, 0, 0, 0));
            default: return Starts.complete(c, st, new Starts.Step(ch.kind, ch.n, ch.who, ch.text, "", 0, 0, 0, 0));
        }
    }

    // ------------------------------------------------------------------ the quest in the Quest Log
    private static void ensureQuest(Character c, Starts.State st, Chapter ch) {
        if (ch.text.length() == 0 || ch.kind.equals("NONE") || ch.kind.equals("WAIT") || ch.kind.equals("FLAG")) return;
        Quest have = null;
        for (Quest q : c.targetQuests) if (q.uid == st.questUid) have = q;
        for (Quest q : c.spotQuests) if (q.uid == st.questUid) have = q;
        if (c.currentQuest != null && c.currentQuest.uid == st.questUid) have = c.currentQuest;
        if (have != null) { if (ch.counter != null) have.label = "Story: " + taskText(ch, c, st); return; } // (the count moves)
        String label = "Story: " + taskText(ch, c, st);
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

    // ------------------------------------------------------------------ gender, echoes of the world, bonds
    /** Whether this person of the story is a woman: fixed by the entry, or (a person who may be loved) always the other gender than the human's. */
    static boolean isFemale(Cast sp, Character c) { return sp.opposite ? !"Female".equals(c.gender) : sp.female; }
    static String firstName(Cast sp, Character c) { return sp.opposite && !isFemale(sp, c) && sp.maleFirst != null ? sp.maleFirst : sp.first; }

    /** What the world looks like to the story, kept as flags the text can test (?e.spouse| ?!e.sect| ?e.beasts=2| {#e.shops}): refreshed before every scene. */
    static void echo(Character c, Starts.State st) {
        if (st == null || c == null) return;
        set(st, "e.female", "Female".equals(c.gender) ? 1 : 0);
        set(st, "e.male", "Female".equals(c.gender) ? 0 : 1);
        set(st, "e.spouse", c.spouse != null && !c.spouse.dead ? 1 : 0);
        set(st, "e.kids", c.children != null && !c.children.isEmpty() ? 1 : 0);
        set(st, "e.sect", c.sect != null ? 1 : 0);
        set(st, "e.master", c.sect != null && c.sect.master == c ? 1 : 0);
        set(st, "e.city", ownsCity(c) ? 1 : 0);
        set(st, "e.shops", c.ownedShops == null ? 0 : c.ownedShops.size());
        List<Character> b = Taming.beasts(c);
        set(st, "e.beasts", b == null ? 0 : b.size());
        set(st, "e.kills", c.kills > 0 ? 1 : 0);
    }

    static boolean ownsCity(Character c) { for (Area a : new ArrayList<>(Map.createdAreas)) if (a != null && a.cityOwner == c) return true; return false; }
    static Area cityOwned(Character c) { for (Area a : new ArrayList<>(Map.createdAreas)) if (a != null && a.cityOwner == c) return a; return null; }
    static int sectSize(Character c) { return c.sect == null || c.sect.members == null ? 0 : c.sect.members.size(); }

    /** A real person of the world who is tied to the human is kept among the story's people under `key` (the text names them with {key.first}, they are met like any person of the story, and they are NOT protected: they are
     *  ordinary people). `mode`: "friend" (a bond of 2 or more), "rival" (-2 or less), "any" (any bond that matters), "kin" (a living relative). With nobody to choose, the person of the cast entry is made instead. */
    static Character adopt(Character c, Starts.State st, String key, String mode) {
        Character have = person(st, key);
        if (have != null) return have;
        List<Character> pool = new ArrayList<>();
        if (mode.equals("kin")) {
            List<Character> k = new ArrayList<>();
            k.add(c.spouse); k.add(c.mother); k.add(c.father);
            if (c.children != null) k.addAll(c.children);
            if (c.siblings != null) k.addAll(c.siblings);
            for (Character n : k) if (usable(c, n)) pool.add(n);
        } else if (c.relationshipList != null) {
            List<Relationship> rs;
            synchronized (c.relationshipList) { rs = new ArrayList<>(c.relationshipList.relationships); }
            for (Relationship r : rs) {
                boolean ok = mode.equals("friend") ? r.value >= 2 : mode.equals("rival") ? r.value <= -2 : Math.abs(r.value) >= 1;
                if (ok && usable(c, Character.charById(r.friendID))) pool.add(Character.charById(r.friendID));
            }
        }
        if (pool.isEmpty()) return spawn(c, st, key);
        Character n = pool.get(GameDriver.rand.nextInt(pool.size()));
        st.npcs.put(key, n.id);
        return n;
    }

    private static boolean usable(Character c, Character n) {
        return n != null && n != c && !n.dead && n.player == 0 && !n.storyNpc && n.age >= 18 && n.location != null && n.trait != Character.Traits.Beast && n.trait != Character.Traits.Monster && n.masterId < 0;
    }

    /** Writes a name or phrase for the text to read as {=key}. */
    static void word(Starts.State st, String key, String text) { if (st.words == null) st.words = new java.util.HashMap<>(); st.words.put(key, text); }

    /** A relative of the human who has died, if any (the references of the family tree outlive the dead): sets {=kin} to the name and {=kinrel} to "your mother", "your late husband"... Returns whether there is one. */
    static boolean deadKin(Character c, Starts.State st) {
        List<Character> k = new ArrayList<>(); List<String> rel = new ArrayList<>();
        if (c.spouse != null && c.spouse.dead) { k.add(c.spouse); rel.add("Female".equals(c.spouse.gender) ? "your late wife" : "your late husband"); }
        if (c.mother != null && c.mother.dead) { k.add(c.mother); rel.add("your mother"); }
        if (c.father != null && c.father.dead) { k.add(c.father); rel.add("your father"); }
        if (c.siblings != null) for (Character s : c.siblings) if (s != null && s.dead) { k.add(s); rel.add("Female".equals(s.gender) ? "your sister" : "your brother"); }
        if (c.children != null) for (Character s : c.children) if (s != null && s.dead && s.age >= 3) { k.add(s); rel.add("Female".equals(s.gender) ? "your daughter" : "your son"); }
        if (k.isEmpty()) return false;
        int i = GameDriver.rand.nextInt(k.size());
        word(st, "kin", k.get(i).name + " " + k.get(i).lastName); word(st, "kinrel", rel.get(i)); word(st, "kinfirst", k.get(i).name);
        return true;
    }

    /** Warms (or chills) the bond between the human and a person of the story. */
    static void bond(Character c, Starts.State st, String key, int delta) { Character n = person(st, key); if (n != null && !n.dead) n.changeRelationship(c, delta); }

    /** Marries the human and a person of the story, if both are free adults of different gender (the same rule as the world's own marriages). Returns whether it happened. */
    static boolean wed(Character c, Starts.State st, String key) {
        Character n = person(st, key);
        if (n == null || n.dead || c.dead || c.spouse != null || n.spouse != null || n.age < 18 || c.age < 18 || n.gender.equals(c.gender)) return false;
        c.spouse = n; n.spouse = c;
        if (c.player > 0 && c.skipping <= 0) System.out.println(n + " and " + c + " are now married! Congratulations!");
        Notifications.onMarriage(n, c);
        if (n.gender.equals("Female")) n.lastName = c.lastName; else c.lastName = n.lastName;
        return true;
    }

    // ------------------------------------------------------------------ the people
    static Character person(Starts.State st, String key) {
        Integer id = st.npcs.get(key);
        return id == null ? null : Taming.byId(id);   // (a companion made this tick is not in the list yet)
    }

    /** A companion beast of the story (a tamed legend, see Taming.legend) kept among the story's people, so the text can name it ({key}) and test whether it lives (?dead.key|). */
    static Character companion(Character c, Starts.State st, String key, String name, String species, int lvl, boolean female) {
        Character b = Taming.legend(c, name, species, lvl);
        if (b != null) { b.gender = female ? "Female" : "Male"; st.npcs.put(key, b.id); }
        return b;
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
        boolean fem = isFemale(sp, c); String first = firstName(sp, c);
        n.name = first; n.lastName = sp.last; n.gender = fem ? "Female" : "Male";
        Naming.claimName(first, sp.last);
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
        n.noMarry = true;   // (a friendship of 3 with a story's companion used to wed them to the human at once: the story chooses, see wed)
        if (sp.keep) n.maxAge += 60;
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
    /** The picture of the place a chapter's scene happens in: the place its task sends the human to, the place its person stands in, or the story's home (StoryArt). */
    static String artKey(Starts.State st, Def d, Chapter ch) {
        String place = null;
        if ("PLACE".equals(ch.kind)) place = ch.where;
        else if (ch.who != null && !ch.who.isEmpty() && d != null) { Cast cs = d.cast.get(ch.who); if (cs != null && cs.home != null && cs.home.startsWith("place:")) place = cs.home; }
        return StoryArt.forPlace(st.story, place);
    }

    /** Sets the picture and the portraits the next scene window takes (the people of the story who speak in the lines). */
    private static void armArt(Starts.State st, List<String> lines, String art) {
        List<String> names = new ArrayList<>();
        List<java.awt.image.BufferedImage> faces = new ArrayList<>();
        try { StoryArt.faces(st, lines, names, faces); } catch (RuntimeException e) { }
        StoryArt.next(art, names, faces);
    }

    static int play(Character c, Starts.State st, String title, String[] raw, Choice ch) { return play(c, st, title, raw, ch, StoryArt.homeOf(st.story)); }

    /** Shows a scene (a window in the GUI, a framed block in the terminal) and, if the chapter asks one, the question; keeps the answer. Returns the 0-based answer (-1 if none). `art` is the picture of the place (StoryArt). */
    static int play(Character c, Starts.State st, String title, String[] raw, Choice ch, String art) {
        echo(c, st);
        List<String> lines = ChapText.prepare(raw, c, st);
        int pick = -1;
        if (lines.isEmpty() && ch == null) return -1;
        shown.add(title);
        if (DecisionBroker.scripted != null && transcript.size() < 20000) transcript.addAll(lines);   // (tests read what was shown)
        if (c.player > 0 && c.skipping <= 0) System.out.println("[Story] " + title + "  ('story replay' reads it again)");
        boolean gui = GameDriver.guiEnabled && GameDriver.mapViewer != null && DecisionBroker.scripted == null;
        if (ch != null) {
            String prompt = ChapText.fill(ch.prompt, c, st);
            String[] opts = new String[ch.options.length];
            for (int i = 0; i < opts.length; i++) opts[i] = ChapText.fill(ch.options[i], c, st);   // (an option may name a person of the story)
            boolean asked = gui && DecisionBroker.promptable(c);
            if (asked) { armArt(st, lines, art); pick = GuiChapter.decide(c, title, lines, prompt, opts, ch.dflt); }
            else {
                if (gui) { armArt(st, lines, art); GuiChapter.scene(title, lines); } else if (DecisionBroker.scripted == null && c.player > 0) System.out.println(ChapText.frame(title, lines));
                pick = DecisionBroker.ask(c, prompt, opts, ch.dflt);
            }
            set(st, ch.flag, pick + 1);
            if (ch.after != null && pick >= 0 && pick < ch.after.length && ch.after[pick] != null && ch.after[pick].length > 0) {
                List<String> more = ChapText.prepare(ch.after[pick], c, st);
                if (asked) GuiChapter.afterChoice(more); // more pages of the window that asked
                else if (!more.isEmpty()) { if (gui) { armArt(st, more, art); GuiChapter.scene(title, more); } else if (DecisionBroker.scripted == null && c.player > 0) System.out.println(ChapText.frame(title, more)); }
            } else if (asked) GuiChapter.afterChoice(null);
        } else if (gui) { armArt(st, lines, art); GuiChapter.scene(title, lines); }
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
            String head = seasonHead(d, st, i);
            if (head.length() > 0) sb.append(i > 0 ? "\n" : "").append(head).append('\n');
            if (i < st.chapter || st.done) sb.append("  [x] ").append(ch.title).append('\n');
            else if (i == st.chapter && mainOpen) sb.append("  [>] ").append(ch.title).append(ch.text.length() > 0 ? " — " + taskText(ch, c, st) : "").append('\n');
            else if (i == st.chapter) sb.append("  [ ] Chapter ").append(i + 1).append(": ").append(waiting(c, st, ch)).append('\n');
            else sb.append("  [ ] ???\n");
        }
        if (!d.episodes.isEmpty() && episodesMet(st) > 0) sb.append("\n  Tales met on the road: ").append(episodesMet(st)).append('\n');
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
            out.add(new String[]{state, ch.title, state.equals("now") ? taskText(ch, c, st) : state.equals("wait") ? waiting(c, st, ch) : "", seasonHead(d, st, i)});
        }
        return out;
    }

    /** The heading of a season in the lists ("Season 2: The Silent Tonic"), only before its first chapter and only in a story with more than one season; "" otherwise. A season not reached yet shows no title. */
    static String seasonHead(Def d, Starts.State st, int i) {
        if (d.seasons.size() < 2) return "";
        Chapter ch = d.chapters.get(i);
        if (i > 0 && d.chapters.get(i - 1).season == ch.season) return "";
        Season sn = d.seasonOf(ch.season);
        boolean known = i <= st.chapter || st.done;
        return "Season " + ch.season + (known && sn != null && sn.title.length() > 0 ? ": " + sn.title : "");
    }

    /** The people of the story that exist in the world now (alive or dead), in the order the story made them. */
    static List<Character> people(Starts.State st) {
        List<Character> out = new ArrayList<>();
        if (st == null || st.npcs == null) return out;
        for (int id : new ArrayList<>(st.npcs.values())) { Character n = Character.charById(id); if (n != null) out.add(n); }
        return out;
    }

    static int episodesMet(Starts.State st) {
        int n = 0;
        if (st != null && st.flags != null) for (java.util.Map.Entry<String, Integer> e : st.flags.entrySet()) if (e.getKey().equals("episodes") || e.getKey().startsWith("episodes.")) n += e.getValue();
        return n;
    }

    /** The scenes the human has seen, as "id  -  Title" (for the Story window's list). */
    static List<String> sceneList(Starts.State st) {
        List<String> out = new ArrayList<>();
        if (st == null || !st.v2 || st.seen == null) return out;
        Def d = DEFS.get(st.story);
        for (String id : new ArrayList<>(st.seen)) {
            String base = id.endsWith(".out") ? id.substring(0, id.length() - 4) : id;
            Chapter ch = d == null ? null : d.find(base);
            if (id.startsWith("interlude.")) { Season sn = d == null ? null : d.seasonOf(Integer.parseInt(id.substring(10))); out.add(id + "  -  " + (sn == null ? "Interlude" : sn.interludeTitle)); continue; }
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
        boolean out = id.endsWith(".out"), inter = id.startsWith("interlude.");
        Chapter ch = id.equals("epilogue") || inter ? null : d.find(out ? id.substring(0, id.length() - 4) : id);
        if (!st.seen.contains(id)) return "You have not seen a scene called '" + id + "'.";
        Season sn = inter ? d.seasonOf(Integer.parseInt(id.substring(10))) : null;
        if (inter && sn == null) return "You have not seen a scene called '" + id + "'.";
        String[] raw = id.equals("epilogue") ? d.epilogue : inter ? sn.interlude : out ? ch.outro : ch.intro;
        String title = id.equals("epilogue") ? "Epilogue" : inter ? sn.interludeTitle : ch.title;
        List<String> lines = ChapText.prepare(raw, c, st);
        if (GameDriver.guiEnabled && GameDriver.mapViewer != null) { armArt(st, lines, ch != null ? artKey(st, d, ch) : StoryArt.homeOf(st.story)); GuiChapter.scene(title, lines); return "(the scene is open in a window)"; }
        return ChapText.frame(title, lines);
    }
}
