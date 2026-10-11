package Game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** How a life begins: Protagonist (potential 100), Standard (rolled from the NPC curve with one re-roll, or fully custom) or one of ten Story
 *  characters with a chain of objectives, each playable at Story / Hard / Brutal difficulty. Difficulty changes the starting potential, gear and
 *  money, how hostile the world is to the player, whether potential is shown as a number or a word, and (Brutal) whether death offers a way back. */
public final class Starts {
    private Starts() { }

    enum Kind { PROTAGONIST, STANDARD_ROLL, STANDARD_CUSTOM, STORY }

    static final String[] DIFFICULTY_NAMES = {"Story", "Hard", "Brutal"};
    static final String[] DIFFICULTY_BLURB = {
            "The character as written: their potential, gear and money, a normal world.",
            "Potential at 60%, no starting gear, half the money, evil strangers pick on you more and lost fights are deadlier. Potential is a rumour (words, not a number) until your first breakthrough.",
            "Potential rolled between 1 and 20, no gear, no money, a hostile world, hidden potential, and when you die that is the end: no rebirth, no taking over a body."};

    // ------------------------------------------------------------------ story data
    static class Npc { final String key, first, last; final Character.Traits trait; final int lvl; final String affil; final int rel;
        Npc(String key, String first, String last, Character.Traits trait, int lvl, String affil, int rel) { this.key = key; this.first = first; this.last = last; this.trait = trait; this.lvl = lvl; this.affil = affil; this.rel = rel; } }

    /** kinds: REALM n, LEVEL n, KILLS n, WEALTH n, CRAFT n, FRIENDS n, MEET key, DEFEAT key, BEFRIEND key, SECT (belong to one), FOUND_SECT, FOUND_CITY, DUNGEON, AGE n */
    static class Step { final String kind, text, npc; final int n; final String reward; final int potential; final long ss; final int pills, gear;
        Step(String kind, int n, String npc, String text, String reward, int potential, long ss, int pills, int gear) { this.kind = kind; this.n = n; this.npc = npc; this.text = text; this.reward = reward; this.potential = potential; this.ss = ss; this.pills = pills; this.gear = gear; } }

    static class Story {
        String id, title, blurb, hook; int potential; Character.Traits trait; String affiliation; String gender; long money; int gearMajor; int potentialPills; int bloodline; boolean brutalHook;
        List<Npc> npcs = new ArrayList<>(); List<Step> steps = new ArrayList<>(); String technique; // a named starting technique, or null
        Story(String id, String title, String blurb, int potential, Character.Traits trait, String affiliation, String gender, long money, int gearMajor, int potentialPills, boolean brutalHook) {
            this.id = id; this.title = title; this.blurb = blurb; this.potential = potential; this.trait = trait; this.affiliation = affiliation; this.gender = gender; this.money = money;
            this.gearMajor = gearMajor; this.potentialPills = potentialPills; this.brutalHook = brutalHook; }
        Story npc(String key, String f, String l, Character.Traits t, int lvl, String aff, int rel) { npcs.add(new Npc(key, f, l, t, lvl, aff, rel)); return this; }
        Story step(String kind, int n, String npc, String text, String reward, int potential, long ss, int pills, int gear) { steps.add(new Step(kind, n, npc, text, reward, potential, ss, pills, gear)); return this; }
    }

    static final java.util.Map<String, Story> STORIES = new LinkedHashMap<>();
    private static void add(Story s) { STORIES.put(s.id, s); }
    static {
        Character.Traits T_ALCH = Character.Traits.Alchemist, T_MERCH = Character.Traits.Merchant, T_RUN = Character.Traits.Runner, T_SCRIBE = Character.Traits.Scribe, T_SMITH = Character.Traits.Blacksmith,
                T_GATH = Character.Traits.Gatherer, T_GHOST = Character.Traits.Ghost, T_LUCKY = Character.Traits.Lucky, T_LEAD = Character.Traits.Leader, T_INNO = Character.Traits.Innovative, T_FRIEND = Character.Traits.Friendly;
        add(new Story("heir", "The Last Heir of the Azure Cloud Sect",
                "Your sect burned, your master's last gift was a cracked cauldron and a name to find. You are the best talent in a generation, and everyone who ruined your home knows it.",
                100, T_ALCH, "Neutral", "any", 500, 1, 0, true).npc("wraith", "Cinder", "Wraith", Character.Traits.Runner, 14, "Evil", -3)
                .step("REALM", 1, null, "Awaken your cultivation: reach Major Lvl 1.", "three Qi Cultivation Pills", 0, 0, 3, 0)
                .step("CRAFT", 10, null, "Prove your craft: make ten items.", "a Spirit-grade weapon", 0, 0, 0, 2)
                .step("FOUND_SECT", 0, null, "Rebuild the Azure Cloud Sect: found a sect of your own.", "a technique insight", 0, 2000, 0, 0)
                .step("DEFEAT", 0, "wraith", "Avenge your sect: Cinder Wraith, who burned it, must fall.", "a Potential Pill", 0, 5000, 0, 0));
        add(new Story("scion", "The Rich Scion",
                "Born in a merchant house with more spirit stones than sense. The caravans, the bodyguards and the best gear money can buy are yours, and so are the rivals who want them.",
                45, T_MERCH, "Neutral", "any", 50_000, 2, 3, false).npc("guard", "Gareth", "Ironwall", Character.Traits.Blacksmith, 16, "Good", 3)
                .step("WEALTH", 200_000, null, "Double your fortune: hold 200,000 spirit stones.", "a Potential Pill", 0, 0, 0, 0)
                .step("REALM", 2, null, "Money is not enough: reach Major Lvl 2.", "gear one Major above you", 0, 0, 0, 3)
                .step("FRIENDS", 3, null, "Buy friends the honest way: three friendships (relationship 2+).", "5,000 spirit stones", 0, 5000, 0, 0)
                .step("FOUND_CITY", 0, null, "Found your own city.", "a Potential Pill", 0, 0, 0, 0));
        add(new Story("orphan", "The Orphan of the Ash Road",
                "Raised by travellers, taught to survive. You never stay anywhere long enough to be hurt.",
                62, T_RUN, "Good", "any", 0, 0, 0, true).npc("elder", "Old", "Brannoch", Character.Traits.Friendly, 10, "Good", 3)
                .step("MEET", 0, "elder", "Find Old Brannoch, who raised you, in Originis.", "a keepsake: Qi Cultivation Pills", 0, 100, 2, 0)
                .step("FRIENDS", 2, null, "Make two friends who are not travellers.", "300 spirit stones", 0, 300, 0, 0)
                .step("REALM", 1, null, "Cross the first gate: reach Major Lvl 1.", "a Potential Pill", 0, 0, 0, 0)
                .step("DUNGEON", 0, null, "Walk into a place of danger: stand on a dungeon tile.", "a Spirit-grade garment", 0, 0, 0, 2));
        add(new Story("disgraced", "The Disgraced Disciple",
                "Expelled for reading what you should not have. You still carry half a forbidden scripture and a grudge the size of a sect.",
                78, T_SCRIBE, "Evil", "any", 100, 0, 0, false).npc("elder", "Elder", "Zhao", Character.Traits.Leader, 18, "Good", -3).npc("ally", "Mei", "Lingfeng", Character.Traits.Friendly, 8, "Evil", 2)
                .step("AGE", 21, null, "Stay alive until your 21st birthday.", "200 spirit stones", 0, 200, 0, 0)
                .step("BEFRIEND", 0, "ally", "Find the one friend who did not turn on you: Mei Lingfeng.", "a Potential Pill", 0, 0, 0, 0)
                .step("REALM", 2, null, "Become strong enough that expulsion means nothing: Major Lvl 2.", "a technique insight", 0, 0, 0, 0)
                .step("DEFEAT", 0, "elder", "Settle with Elder Zhao, who cast you out.", "Spirit-grade gear", 0, 3000, 0, 2));
        add(new Story("smith", "The Blacksmith's Child",
                "Your father forged weapons for heroes and died holding a hammer. You will never out-cultivate them, but you will arm the ones who do.",
                30, T_SMITH, "Good", "any", 300, 1, 0, false)
                .step("CRAFT", 25, null, "Learn the trade: make 25 items.", "a Potential Pill", 0, 0, 0, 0)
                .step("WEALTH", 10_000, null, "Earn your keep: hold 10,000 spirit stones.", "a Spirit-grade weapon", 0, 0, 0, 2)
                .step("CRAFT", 100, null, "A smith's name is made by volume: 100 items.", "a Potential Pill", 0, 2000, 0, 0)
                .step("REALM", 2, null, "Hold your own: reach Major Lvl 2.", "gear one Major above you", 0, 0, 0, 3));
        add(new Story("tamer", "The Beast-Tamer's Daughter",
                "The wild remembers your mother's voice. Beasts hesitate before they attack you.",
                55, Character.Traits.Tamer, "Neutral", "any", 100, 0, 0, false)
                .step("TAME", 1, null, "Win a beast's trust: tame one ('tame' where beasts roam).", "a Qi Cultivation Pill", 0, 0, 1, 0)
                .step("REALM", 1, null, "Reach Major Lvl 1.", "200 spirit stones", 0, 200, 0, 0)
                .step("WEALTH", 5_000, null, "Sell what the wild gives: hold 5,000 spirit stones.", "a Potential Pill", 0, 0, 0, 0)
                .step("DUNGEON", 0, null, "Find a beast lair or ruin: stand on a dungeon tile.", "a technique insight", 0, 0, 0, 0));
        add(new Story("ghost", "The Ghost-Touched",
                "You died once and did not stay dead. People shy away; the dead do not.",
                70, T_GHOST, "Neutral", "any", 50, 0, 0, true).npc("seer", "Old", "Meng", Character.Traits.Scribe, 12, "Neutral", 2)
                .step("MEET", 0, "seer", "Find Old Meng in Originis: she can tell you what you are.", "a Qi Cultivation Pill", 0, 0, 1, 0)
                .step("FRIENDS", 1, null, "Be known by the living: one real friend.", "100 spirit stones", 0, 100, 0, 0)
                .step("REALM", 1, null, "Reach Major Lvl 1.", "a Potential Pill", 0, 0, 0, 0)
                .step("REALM", 3, null, "Outlive the whispers: reach Major Lvl 3.", "a technique insight", 0, 0, 0, 0));
        add(new Story("lucky", "The Lucky Fool",
                "Everyone agrees you have no talent. Everyone also agrees that things keep going your way.",
                15, T_LUCKY, "Good", "any", 20, 0, 1, true).npc("friend", "Pip", "Tallow", Character.Traits.Friendly, 6, "Good", 3)
                .step("FRIENDS", 3, null, "Collect friends the way you collect luck: three of them.", "a Potential Pill", 0, 0, 0, 0)
                .step("KILLS", 5, null, "Prove you can hold your own: five kills.", "200 spirit stones", 0, 200, 0, 0)
                .step("REALM", 1, null, "Pass the first gate against all odds: reach Major Lvl 1.", "two Potential Pills", 0, 0, 0, 0)
                .step("REALM", 2, null, "Keep getting lucky: reach Major Lvl 2.", "a Spirit-grade weapon", 0, 0, 0, 2));
        add(new Story("clan", "The Last of the Silver Crane Clan",
                "Your clan is gone; the survivors look to you.",
                85, T_LEAD, "Good", "any", 400, 1, 0, false).npc("a", "Tamsin", "Crane", Character.Traits.Gatherer, 6, "Good", 3).npc("b", "Joren", "Crane", Character.Traits.Miner, 7, "Good", 3).npc("c", "Isla", "Crane", Character.Traits.Tailor, 5, "Good", 3)
                .npc("rival", "Baron", "Hexley", Character.Traits.Leader, 16, "Evil", -3)
                .step("FOUND_SECT", 0, null, "Raise the clan again: found a sect.", "a Potential Pill", 0, 1000, 0, 0)
                .step("FRIENDS", 5, null, "A clan needs allies: five friends.", "gear for the clan's heir", 0, 0, 0, 2)
                .step("REALM", 2, null, "Lead from the front: reach Major Lvl 2.", "a technique insight", 0, 0, 0, 0)
                .step("DEFEAT", 0, "rival", "Baron Hexley hunted your clan. End it.", "a Potential Pill", 0, 5000, 0, 0));
        add(new Story("disciple", "The Wandering Immortal's Disciple",
                "A drunk old man taught you three moves and vanished.",
                90, T_INNO, "Neutral", "any", 100, 0, 0, false)
                .step("REALM", 1, null, "Master the three moves: reach Major Lvl 1.", "a technique insight", 0, 0, 0, 0)
                .step("KILLS", 15, null, "Test the moves: fifteen kills.", "300 spirit stones", 0, 300, 0, 0)
                .step("REALM", 3, null, "Rise far enough that he might return: reach Major Lvl 3.", "a Potential Pill", 0, 0, 0, 0)
                .step("WEALTH", 20_000, null, "Even immortals drink: hold 20,000 spirit stones.", "Heaven-grade gear", 0, 0, 0, 3));
        STORIES.get("disciple").technique = "Three Drunken Moves";
    }

    // ------------------------------------------------------------------ per-character story state (saved with the character)
    static class State implements Serializable {
        static final long serialVersionUID = 1L;
        String story;
        int step = 0;
        boolean done = false;
        int difficulty = 0;
        boolean hooked = true;
        final java.util.HashMap<String, Integer> npcs = new java.util.HashMap<>();
        int questUid = -1;
        // the chapters of a story that has them (see Chapters): null / 0 / false in a save made before they existed, which keeps its flat list of objectives
        boolean v2, open;                                   // chapters are on; the current chapter's task is under way
        int chapter, openedWeek, lastEnd;                   // the index of the current chapter, the week it opened, the week the last one ended
        String cur;                                         // the id of the chapter or episode that is open
        int lastEpisode;                                    // the week the last episode ended
        java.util.HashMap<String, Integer> flags;           // what was chosen and what happened (see ChapText for how the text reads them)
        java.util.LinkedHashSet<String> seen;               // the scenes seen (chapter ids, "<id>.out", "epilogue"): `story replay`
        java.util.HashMap<String, int[]> places;            // the places of the story, once picked: key -> {x, y}
    }

    // ------------------------------------------------------------------ menu result
    static class Choice { Kind kind = Kind.PROTAGONIST; String story; int difficulty = 0; }

    // ------------------------------------------------------------------ applying a start
    /** Sets up `c` (a freshly created human character) according to `ch`. Called once per player at the start of a new world. */
    static void apply(Character c, Choice ch) {
        GameDriver.difficulty = Math.max(GameDriver.difficulty, ch.kind == Kind.STORY ? ch.difficulty : 0);
        switch (ch.kind) {
            case PROTAGONIST:
                c.potential = 100; c.trait = Character.Traits.Alchemist;
                break;
            case STANDARD_ROLL: {
                int p = c.potential; // the constructor already rolled it from the NPC curve
                if (p < 20) p = (int) (101 - Math.sqrt(GameDriver.rand.nextInt(10000) + 1)); // fate rerolls once
                c.potential = Math.max(1, Math.min(100, p));
                c.potentialHidden = true;
                break;
            }
            case STANDARD_CUSTOM:
                break; // the caller opens the character editor
            case STORY: {
                Story s = STORIES.get(ch.story);
                if (s == null) break;
                applyStory(c, s, ch.difficulty);
                break;
            }
        }
        c.cultivationBaseMax = Math.pow(c.lvl, 3) * (((70. + 1. / 3.) + ((11. / 6.) * c.lvl)) / 25.);
        c.cultivation = (Math.pow(c.potential, 2) * Math.sqrt(c.lvl) * 10.) / (52. * Math.sqrt(c.potential) * Math.pow(2, c.inhibitor));
    }

    /** After the character editor: keeps the derived cultivation numbers in step with whatever the player typed. */
    static void apply2(Character c) {
        c.cultivationBaseMax = Math.pow(c.lvl, 3) * (((70. + 1. / 3.) + ((11. / 6.) * c.lvl)) / 25.);
        c.cultivation = (Math.pow(c.potential, 2) * Math.sqrt(c.lvl) * 10.) / (52. * Math.sqrt(c.potential) * Math.pow(2, c.inhibitor));
    }

    private static void applyStory(Character c, Story s, int diff) {
        c.trait = s.trait;
        if (s.affiliation != null) {
            c.affiliation = s.affiliation;
            c.affiliationNum = s.affiliation.equals("Good") ? 1 : s.affiliation.equals("Evil") ? -1 : 0;
        }
        int pot = s.potential;
        if (diff == 1) pot = Math.max(1, (int) Math.round(s.potential * 0.6));
        if (diff == 2) pot = 1 + GameDriver.rand.nextInt(20);
        c.potential = pot;
        c.potentialHidden = diff >= 1;
        long money = diff == 0 ? s.money : diff == 1 ? s.money / 2 : 0;
        c.ss = money; c.convertCurrency();
        if (s.bloodline > 0) c.bloodlineBonus += s.bloodline;
        if (diff == 0) {
            if (s.gearMajor > 0) giveGear(c, s.gearMajor);
            if (s.potentialPills > 0) givePills(c, Item.PillType.Potential, 1, s.potentialPills);
            if (s.technique != null) { c.martialTechnique = new Technique(Technique.techniqueType.Martial, c, 40); c.martialTechnique.name = s.technique; c.martialTechnique.lvl = 10; }
        }
        State st = new State();
        st.story = s.id; st.difficulty = diff;
        st.hooked = diff < 2 || s.brutalHook;
        c.story = st;
        if (st.hooked && Chapters.has(s.id)) Chapters.begin(c, st);   // a story with chapters: its people appear when the chapters need them
        else if (st.hooked) {
            for (Npc n : s.npcs) {
                Character npc = spawnNpc(n, c);
                if (npc != null) st.npcs.put(n.key, npc.id);
            }
        }
        if (!st.hooked) st.done = true; // a pure challenge: no hook, no chain
        c.story = st;
    }

    static void giveGear(Character c, int major) {
        Item w = new Item(c, Item.WeaponType.values()[GameDriver.rand.nextInt(Item.WeaponType.values().length)], major);
        Item cl = new Item(c, major, Item.ItemType.Clothing);
        c.backpack.add(w); c.backpack.add(cl);
        c.equip();
    }

    static void givePills(Character c, Item.PillType type, int lvl, int qty) {
        Item pill = new Item(Item.ItemType.Pill, type, lvl, qty);
        for (Item p : c.pillBag) if (p.name.equals(pill.name) && p.pillType == type && p.lvl == lvl) { p.quantity += qty; return; }
        c.pillBag.add(pill);
    }

    /** A story character: a normal NPC with a fixed name, trait and level who stays in Originis. */
    static Character spawnNpc(Npc n, Character player) {
        Area home = Map.tiles[Map.range / 2][Map.range / 2];
        if (home == null) return null;
        Character npc = new Character(home);
        npc.age = GameDriver.START_AGE;
        npc.name = n.first; npc.lastName = n.last; npc.trait = n.trait;
        npc.affiliation = n.affil; npc.affiliationNum = n.affil.equals("Good") ? 1 : n.affil.equals("Evil") ? -1 : 0;
        npc.potential = Math.max(npc.potential, 60);
        int guard = 0;
        while (npc.lvl < n.lvl && guard++ < 200000) npc.cultivate();
        npc.storyNpc = true;
        while (Map.characters.size() < npc.id) Map.characters.add(null); // (the id is the position, as everywhere)
        Map.characters.add(npc); // (the births of the NPC phase are merged before the people who play act, so nothing is pending)
        Map.population++;
        if (n.rel != 0) { npc.changeRelationship(player, n.rel); }
        return npc;
    }

    /** Called once for every NPC of the first generation: the years they cultivated before the game began (the player starts fresh at level 1). */
    static void prehistory(List<Character> starters) {
        final int weeks = (GameDriver.START_AGE - 8) * 52;
        starters.parallelStream().forEach(c -> { for (int i = 0; i < weeks; i++) c.cultivate(); });
    }

    /** The years a human protagonist cultivated between 8 and 18, so they start where everybody else of their potential starts instead of at level 1
     *  (stops short of Major Lvl 3: the first tribulation is for the game itself to face). */
    static void prehistoryPlayer(Character c) {
        final int weeks = (GameDriver.START_AGE - 8) * 52;
        int pl = c.player;
        c.player = 0; // silent: no breakthrough messages for years nobody watched
        try { for (int i = 0; i < weeks && !c.dead && c.lvl < 29; i++) c.cultivate(); } finally { c.player = pl; }
    }

    /** A cultivator spawned by a city: they arrive at the starting age with the cultivation of the years since 8 (so their level follows their potential). */
    static void prehistoryOne(Character c) {
        c.age = GameDriver.START_AGE;
        c.time = GameDriver.rand.nextInt(52);
        final int weeks = (GameDriver.START_AGE - 8) * 52;
        for (int i = 0; i < weeks; i++) c.cultivate();
    }

    // ------------------------------------------------------------------ potential shown as a word
    static String potentialWord(int p) { return p < 20 ? "dull" : p < 37 ? "ordinary" : p < 52 ? "talented" : p < 65 ? "gifted" : p < 85 ? "prodigious" : "heaven-chosen"; }

    // ------------------------------------------------------------------ the engine: runs every tick for a human character with a story
    static void tick(Character c) {
        State st = c.story;
        if (st == null || st.done) return;
        Story s = STORIES.get(st.story);
        if (s == null) { st.done = true; return; }
        if (st.v2) { Chapters.tick(c, st); return; }
        hold(st);
        if (st.step >= s.steps.size()) { finish(c, s, st); return; }
        Step step = s.steps.get(st.step);
        if (complete(c, st, step)) {
            reward(c, step);
            removeQuest(c, st);
            st.step++;
            String msg = "Story step complete: " + step.text + " Reward: " + step.reward + ".";
            System.out.println(msg);
            Notifications.post("alert", msg, -1, c.id);
            if (st.step >= s.steps.size()) { finish(c, s, st); return; }
            announce(c, s, st);
        }
        ensureQuest(c, st, s.steps.get(Math.min(st.step, s.steps.size() - 1)));
    }

    static void announce(Character c, Story s, State st) {
        Step n = s.steps.get(st.step);
        String msg = "Story (" + s.title + "), next: " + n.text;
        System.out.println(msg);
        Notifications.post("alert", msg, -1, c.id);
    }

    private static void finish(Character c, Story s, State st) {
        st.done = true;
        removeQuest(c, st);
        String msg = "Your story \"" + s.title + "\" is complete. The rest of your life is yours to write.";
        System.out.println(msg);
        Notifications.post("breakthrough", msg, -1, c.id);
    }

    static boolean complete(Character c, State st, Step s) {
        switch (s.kind) {
            case "REALM": return c.majorLvl >= s.n;
            case "LEVEL": return c.lvl >= s.n;
            case "KILLS": return c.kills >= s.n;
            case "WEALTH": return PlayerLists.wealth(c) >= s.n;
            case "CRAFT": return c.itemsCrafted >= s.n;
            case "FRIENDS": { int n = 0; for (Relationship r : new ArrayList<>(c.relationshipList.relationships)) if (r.value >= 2) n++; return n >= s.n; }
            case "MEET": { Character npc = npcOf(st, s.npc); return npc == null || (c.location != null && npc.location == c.location); }
            case "DEFEAT": { Character npc = npcOf(st, s.npc); return npc == null || npc.dead; }
            case "BEFRIEND": { Character npc = npcOf(st, s.npc); if (npc == null || npc.dead) return true; Relationship r = c.relationshipList.contains(npc.id); return r != null && r.value >= 2; }
            case "SECT": return c.sect != null;
            case "FOUND_SECT": return c.sect != null && c.sect.master == c;
            case "FOUND_CITY": for (Area a : new ArrayList<>(Map.createdAreas)) if (a.city && a.cityOwner == c) return true; return false;
            case "TAME": return Taming.beasts(c).size() >= s.n;
            case "DUNGEON": return c.location != null && c.location.dungeonType != null;
            case "AGE": return c.age >= s.n;
            default: return true;
        }
    }

    private static Character npcOf(State st, String key) {
        Integer id = st.npcs.get(key);
        return id == null ? null : Character.charById(id);
    }

    /** Story characters do not wander: each tick they are put back where they belong and their own errands are cleared. */
    private static void hold(State st) {
        Area home = Map.tiles[Map.range / 2][Map.range / 2];
        for (Integer id : st.npcs.values()) {
            Character n = Character.charById(id);
            if (n == null || n.dead || home == null) continue;
            n.targetQuests.clear(); n.spotQuests.clear(); n.currentQuest = null;
            if (n.location != home) { if (n.location != null) n.location.removeChar(n); n.location = home; home.addChar(n); }
        }
    }

    private static void reward(Character c, Step s) {
        if (s.potential > 0) c.potential = Math.min(100, c.potential + s.potential);
        if (s.ss > 0) { c.ss += s.ss; c.convertCurrency(); }
        if (s.pills > 0) givePills(c, Item.PillType.Cultivation, 1, s.pills);
        if (s.gear > 0) giveGear(c, s.gear);
        if (s.reward.contains("Potential Pill")) givePills(c, Item.PillType.Potential, 1, s.reward.startsWith("two") ? 2 : 1);
        if (s.reward.contains("technique insight") && c.martialTechnique != null) { c.martialTechnique.upgrade(c); c.martialTechnique.upgrade(c); }
    }

    // ------------------------------------------------------------------ the story quest in the player's quest list
    private static void ensureQuest(Character c, State st, Step step) {
        for (Quest q : c.targetQuests) if (q.uid == st.questUid) return;
        for (Quest q : c.spotQuests) if (q.uid == st.questUid) return;
        if (c.currentQuest != null && c.currentQuest.uid == st.questUid) return;
        Quest q;
        String label = "Story: " + step.text;
        Character npc = step.npc == null ? null : npcOf(st, step.npc);
        if (npc != null && !npc.dead) q = new Quest(label, true, 90, 0, 90, npc);
        else q = new Quest(label, true, 90, 0, 90);
        q.userCreated = true;
        q.pin(90);
        if (q.category == Quest.QuestCategory.Spot) c.spotQuests.add(q); else c.targetQuests.add(q);
        st.questUid = q.uid;
    }

    private static void removeQuest(Character c, State st) {
        c.targetQuests.removeIf(q -> q.uid == st.questUid);
        c.spotQuests.removeIf(q -> q.uid == st.questUid);
        if (c.currentQuest != null && c.currentQuest.uid == st.questUid) c.currentQuest = null;
        st.questUid = -1;
    }

    /** Text for the `story` command and the Story window. */
    static String describe(Character c) {
        State st = c.story;
        if (st == null) return "You are not living a story: this life began as " + (GameDriver.difficulty > 0 ? "a hard Standard" : "a Standard life") + " (no chain of objectives).";
        Story s = STORIES.get(st.story);
        if (s == null) return "(unknown story)";
        StringBuilder sb = new StringBuilder("=== " + s.title + " — " + DIFFICULTY_NAMES[st.difficulty] + " ===\n" + s.blurb + "\n\n");
        if (st.v2) return Chapters.describe(c, st, s);
        if (!st.hooked) { sb.append("A pure challenge: this Brutal start has no hook and no objectives.\n"); return sb.toString(); }
        for (int i = 0; i < s.steps.size(); i++) {
            Step step = s.steps.get(i);
            sb.append(i < st.step || st.done ? "  [x] " : i == st.step ? "  [>] " : "  [ ] ").append(step.text).append("   (reward: ").append(step.reward).append(")\n");
        }
        if (st.done) sb.append("\nStory complete.\n");
        return sb.toString();
    }
}
