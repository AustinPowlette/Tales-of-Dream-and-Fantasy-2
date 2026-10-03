package Game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

/** The lore: the Palace of the Oldest Order and the story that runs through every human life (see "Lore Design.md").
 *
 *  The world is the inner garden of a far stronger cultivator; the Palace at Originis is where his image, ???, runs a school for an army. This class is
 *  the engine: the Palace (a sect with a virtual master: no Character, so every master duty is done here), the yearly Convocation of the First Gate
 *  (the entrance tournament), the chain of objectives every human follows in seven acts, the places that hold the clues (LoreSites), the scenes ??? speaks
 *  (LoreText, shown by GuiLore or as a framed block in the terminal), the whispers, the omens and the crossing that ends the story.
 *
 *  Everything is switched by `Settings.lore`; with it off nothing here runs or appears. State is `Lore.state` (saved in SaveData.lore, null in worlds
 *  that began without it: `lore enable` creates the Palace there). All of it runs on the game thread. */
final class Lore {
    private Lore() { }

    static volatile State state = null;
    /** Tests: the ids of the scenes shown, in order (not saved). */
    static final List<String> shown = Collections.synchronizedList(new ArrayList<String>());

    // ------------------------------------------------------------------ saved state
    static class State implements Serializable {
        static final long serialVersionUID = 1L;
        long seed;                       // the world seed the places were computed for
        int[] siteX, siteY;              // where each of LoreSites.ALL is (same order)
        boolean convocationOpen;         // the Convocation of the First Gate is under way at Originis
        boolean pending;                 // a new one is waiting for the courtyard to be free
        int convocationYear;             // the year of the open (or last) Convocation
        int held;                        // Convocations that have been judged
        HashSet<Integer> passes = new HashSet<>();            // cultivators the Palace has let in (they may join, now or later)
        HashMap<Integer, PState> humans = new HashMap<>();    // per human body
        int[] lamps = newLamps();        // the nine lamps: the id of the Elder who holds each, or -1
        int crossed;                     // cultivators who crossed the Seam
        ArrayList<String> roll = new ArrayList<>();           // their names (the Roll of the Crossed)
        int omens;                       // omens the Hollow has sent
        String lastResult = "";          // one line about the last Convocation
        boolean silenced;                // ??? is gone (the Last Lamp, or the hidden ending): no more Convocations, whispers or omens
        boolean palaceGone;              // a human razed the Palace
        HashMap<Integer, Integer> seekers;   // NPC seekers (admitted by the Convocation): id -> bit mask of the places read (null in older saves)
        ArrayList<Integer> wanderers;        // the Wandering Immortals (soldiers who came back)
        int[] footX, footY;                  // the Giant Footprints (null in older saves: computed on demand)
        HashSet<String> firstRead;           // the places somebody has already read (the Chronicle names the first reader)
        int blood;                           // potential every cultivator born from now on gets: 5 for each who fell on the Gardener's array (stacks)
        HashMap<Integer, Integer> stayers;   // NPC seekers who crossed and stayed: id -> -1 Evil, 0 Neutral, 1 Good (null in older saves)
        boolean gardenDestroyed;             // the Gardener is dead: the Garden went out
    }

    static int[] newLamps() { int[] l = new int[9]; Arrays.fill(l, -1); return l; }

    static class PState implements Serializable {
        static final long serialVersionUID = 1L;
        int bodyId = -1;
        int step;                        // index into CHAIN
        int peakMajor, peakPoints;       // best realm / merit reached in any life (the story is about the soul, not the body)
        int registeredYear;              // Convocation year this human registered for (0 = never)
        int tries;                       // Convocations entered
        String result;                   // "PASS" / "FAIL" / "INVITE" while the scene is still to be shown
        int resultRound, resultRounds, resultYear;
        boolean everMember, declined;
        int lives;                       // reincarnations seen
        int lastWhisper = -1000;
        boolean ended; String ending = ""; int endedWeek;
        int questUid = -1; String questLabel = "";
        LinkedHashSet<String> scenes = new LinkedHashSet<>();     // scenes seen, in order
        LinkedHashSet<String> found = new LinkedHashSet<>();      // places read
        HashSet<String> revealed = new HashSet<>();               // places the human has been told about
        HashMap<String, Integer> glimpsed = new HashMap<>();      // place -> week the human last stood there unable to read it
        ArrayList<String> queue = new ArrayList<>();              // scenes waiting to be shown (one per turn)
        int lampIdx = -1;
        String endingId = ""; int score;                 // the ending reached and its score
        boolean darkPath, secretDone, stewardActive;   // the Usurper's path, the hidden ending reached, a Steward in office
        int idleUntil;                                 // the week the Idler's Garden goes out (0 = never)
        HashSet<String> prints;                        // Giant Footprints read (null until the first)
        boolean metOldMan, stayed, counted;            // the Old Man has spoken; the ending is a Stay (it can be undone with `lore leave`); the crossing is counted in the Roll
    }

    // ------------------------------------------------------------------ the chain: seven acts of objectives, the same for every human
    static class Step {
        final String kind; final int n; final String[] ids; final int act; final String text; final String promote;
        Step(int act, String kind, int n, String[] ids, String text, String promote) { this.act = act; this.kind = kind; this.n = n; this.ids = ids; this.text = text; this.promote = promote; }
    }
    static String[] ids(String... s) { return s; }
    static final Step[] CHAIN = {
            new Step(1, "REGISTER", 0, null, "Present yourself at Originis and register for the Convocation of the First Gate ('do Register Tournament' there).", null),
            new Step(1, "ADMIT", 0, null, "Win a place in the Palace of the Oldest Order.", null),
            new Step(2, "SITE", 0, ids("wayhouse"), "Find the Unlit Wayhouse the Order spoke of.", null),
            new Step(2, "POINTS", 30, null, "Earn 30 merit for the Order: sect missions, donations, delving ruins.", null),
            new Step(2, "MAJOR", 2, null, "Cross the second gate: reach Major Lvl 2 (Core Formation). The Order will take you into the Inner Court.", "INNER"),
            new Step(3, "SITES", 2, ids("weaver", "cistern", "assayer"), "Read two of these three places: the Weaver's Wall, the Cistern of Returning, the Assayer's Hall.", null),
            new Step(3, "MAJOR", 4, null, "Reach Major Lvl 4 (Nascent Soul).", null),
            new Step(4, "SITES", 2, ids("shed", "thinsky", "lanternroad"), "Read two of these three places: the Gardener's Shed, the Thin Sky, the Road of Unreturned Lanterns.", null),
            new Step(4, "ELDER", 5, null, "Take a lamp among the Nine: reach Major Lvl 5 (Ensoulment) with 200 merit.", "ELDER"),
            new Step(5, "SITES", 2, ids("menagerie", "garrison"), "Read both of the far places: the Menagerie of Practice and the Garrison of the Crossed.", null),
            new Step(5, "MAJOR", 8, null, "Reach Major Lvl 8 (Half-Immortal).", null),
            new Step(6, "SEAT", 0, null, "Go to the Empty Seat at Originis (an Elder who has read the nine places).", null),
            new Step(6, "MAJOR", 9, null, "Reach Major Lvl 9 (Pseudo Immortal).", null),
            new Step(7, "MAJOR", 10, null, "Open the Door: reach Major Lvl 10, level 101 (True Immortal).", null),
            new Step(7, "CROSS", 0, null, "Go through the Seam under the Empty Seat ('lore cross' at Originis) and meet the Gardener.", null),
    };
    static final int STEP_INNER = 4, STEP_ELDER = 8, STEP_SEAT = 11;
    static final int[] LEVEL_SCENE_MAJOR = {1, 3, 4, 6, 7, 8, 9, 10};
    static final String[] LEVEL_SCENE_ID = {"m1", "m3", "m4", "m6", "m7", "m8", "m9", "door"};
    static final String[] REALM = {"Qi Cultivation", "Foundation", "Core Formation", "Soul Wandering", "Nascent Soul", "Ensoulment", "Nihility", "Ascension", "Half-Immortal", "Pseudo Immortal", "True Immortal"};

    // ------------------------------------------------------------------ the Palace
    static Area palaceArea() { return Map.tiles[Map.range / 2][Map.range / 2]; }
    static Organization palace() { Area a = palaceArea(); return a != null && a.sect != null && (a.sect.palace || a.sect.virtual) ? a.sect : null; }
    static boolean isMember(Character c) { return c != null && c.sect != null && (c.sect.palace || c.sect.virtual); }
    static boolean active() { return state != null && Settings.lore; }

    /** A new world: the Palace stands at Originis (named the same every time, no master, no members until the first Convocation) and the places are set out. */
    static void newWorld() {
        if (!Settings.lore || state != null) return;
        Area home = palaceArea();
        if (home == null || home.sect != null) return;
        State s = new State();
        s.seed = Map.worldSeed;
        int[][] at = LoreSites.place(s.seed);
        s.siteX = new int[at.length]; s.siteY = new int[at.length];
        for (int i = 0; i < at.length; i++) { s.siteX[i] = at[i][0]; s.siteY[i] = at[i][1]; }
        home.sect = new Organization(LoreText.PALACE, home);
        state = s;
        LoreEndings.ensureFeet(s);
        for (Area a : new ArrayList<>(Map.createdAreas)) onTileCreated(a);
        Chronicle.add("world", "The " + LoreText.PALACE + " stands open at Originis. Its master's seat is empty.");
    }

    /** `lore enable` in a world that began without the lore: the Palace is founded at Originis if the tile has no sect. Returns null on success, else why not. */
    static String enable() {
        if (state != null) { Settings.lore = true; Settings.save(); return null; }
        Area home = palaceArea();
        if (home == null) return "There is no Originis here.";
        if (home.sect != null) return "Originis already hosts a sect (" + home.sect.name + "): the Palace cannot stand there.";
        Settings.lore = true; Settings.save();
        newWorld();
        if (state == null) return "The Palace could not be founded.";
        state.pending = true; state.convocationYear = GameDriver.time / 52 + 1;
        return null;
    }

    /** Called from Area's constructor: a tile that belongs to a lore place takes its name. */
    static void onTileCreated(Area a) {
        State s = state;
        if (s == null || !Settings.lore || a == null) return;
        int i = LoreSites.siteAt(s, a.x, a.y);
        if (i >= 0) LoreSites.apply(a, LoreSites.ALL[i]);
        else LoreSites.applyFoot(s, a);
    }

    /** Organization.whyNot for the Palace: it admits only those it has let in. */
    static String whyNotEnter(Character c) {
        State s = state;
        if (s == null || s.passes.contains(c.id)) return null;
        return "the Palace admits only those who win a place at the Convocation of the First Gate (every New Year) or whom it has noticed";
    }

    // ------------------------------------------------------------------ the world's side: Convocation, ranks, omens
    /** Every tick (after the tournament timers ran): opens a waiting Convocation as soon as the courtyard is free. */
    static void worldTick() {
        State s = state;
        if (s == null || !Settings.lore) return;
        Area home = palaceArea();
        if (home == null || palace() == null || s.silenced) return;
        if (s.pending && !s.convocationOpen && home.tournamentTimer <= 0) {
            s.pending = false; s.convocationOpen = true;
            home.tournamentTimer = 8; home.tournamentMinMajor = 0; home.tournamentMaxMajor = 4;
            home.tournamentPrize = null; home.tournamentOrganizer = null; home.tournamentRegistrants.clear();
            home.tournamentName = "Convocation of the First Gate, Year " + s.convocationYear;
            String msg = "The " + home.tournamentName + " is proclaimed at Originis: anyone below the Fifth Gate (Major Lvl 0-4) may register for a place in the " + LoreText.PALACE + ". Eight weeks.";
            System.out.println("=== " + msg + " ===");
            Chronicle.add("world", msg);
        }
    }

    /** Once a year: the next Convocation, ranks and stipends of the members, the dead leave the rolls, omens, and those who reach the Door cross. */
    static void yearly() {
        State s = state;
        if (s == null || !Settings.lore) return;
        Organization pal = palace();
        if (pal == null) return;
        int year = GameDriver.time / 52 + 1;
        if (s.silenced) { LoreEndings.yearly(s, pal); return; } // ??? is gone: the Palace runs on without its yearly rites
        if (!s.convocationOpen) { s.pending = true; s.convocationYear = year; }
        // the dead leave the passes
        for (java.util.Iterator<Integer> it = s.passes.iterator(); it.hasNext(); ) {
            Integer id = it.next(); Character b = Character.charById(id);
            if (b == null || b.dead) it.remove();
        }
        // the Nine Lamps: seats whose holder died or left are free
        for (int i = 0; i < s.lamps.length; i++) {
            if (s.lamps[i] < 0) continue;
            Character h = Character.charById(s.lamps[i]);
            if (h == null || h.dead || h.sect != pal || pal.getRank(h) != Organization.SectRank.ELDER) s.lamps[i] = -1;
        }
        // merit promotions of the NPC members (a human is promoted by the chain), stipends, and the crossing of those who reached the Door
        List<Character> members = new ArrayList<>(pal.members);
        List<Character> candidates = new ArrayList<>();
        for (Character m : members) {
            if (m == null || m.dead) continue;
            Organization.SectRank r = pal.getRank(m);
            if (m.player == 0) {
                if (r == Organization.SectRank.OUTER_DISCIPLE && m.majorLvl >= 2) { pal.promoteRank(m); r = Organization.SectRank.INNER_DISCIPLE; }
                if (r == Organization.SectRank.INNER_DISCIPLE && m.majorLvl >= 4) candidates.add(m);
            }
            long base = r == Organization.SectRank.ELDER ? 200 : r == Organization.SectRank.INNER_DISCIPLE ? 60 : 20;
            long stipend = base * (m.majorLvl + 1) * (m.majorLvl + 1);
            m.ss += stipend; m.convertCurrency();
        }
        Collections.sort(candidates, (a, b) -> Integer.compare(b.lvl, a.lvl));
        for (Character cand : candidates) { int free = freeLamp(s); if (free < 0) break; seatLamp(s, pal, cand, free); }
        omens(year);
        LoreEndings.yearly(s, pal); // seekers walk the road, a Steward keeps their promise
    }

    static int freeLamp(State s) { for (int i = 0; i < s.lamps.length; i++) if (s.lamps[i] < 0) return i; return -1; }

    /** Makes `c` the holder of lamp `i` (an Elder of the Nine). */
    static void seatLamp(State s, Organization pal, Character c, int i) {
        if (pal.getRank(c) == Organization.SectRank.OUTER_DISCIPLE) pal.promoteRank(c);
        if (pal.getRank(c) == Organization.SectRank.INNER_DISCIPLE) pal.promoteRank(c);
        s.lamps[i] = c.id;
        if (c.player == 0) Chronicle.add("world", c.name + " " + c.lastName + " was named Elder: the Lamp of " + LoreText.LAMPS[i] + " is lit in the Palace of the Oldest Order.", c.id);
    }

    /** A human becomes an Elder: a free lamp, or the weakest non-human Elder yields theirs. Returns the lamp index, or -1 if nobody can yield. */
    static int lampFor(State s, Organization pal, Character c) {
        int free = freeLamp(s);
        if (free < 0) {
            int weakest = -1;
            for (int i = 0; i < s.lamps.length; i++) {
                Character h = Character.charById(s.lamps[i]);
                if (h != null && h.player == 0 && (weakest < 0 || h.lvl < Character.charById(s.lamps[weakest]).lvl)) weakest = i;
            }
            if (weakest < 0) return -1;
            Character yielder = Character.charById(s.lamps[weakest]);
            if (yielder != null) pal.memberRanks.put(yielder.id, Organization.SectRank.INNER_DISCIPLE);
            s.lamps[weakest] = -1; free = weakest;
        }
        seatLamp(s, pal, c, free);
        return free;
    }

    /** An NPC who reached level 101 in the Palace crosses the Seam: gone from the world, remembered in the Roll of the Crossed. */
    static void crossNpc(Character m) {
        State s = state;
        s.crossed++;
        String who = m.name + " " + m.lastName;
        s.roll.add(who + " (year " + GameDriver.time / 52 + ")");
        Chronicle.add("legend", who + " opened the Door and crossed the Seam beneath the Palace. They are not seen again.", m.id);
        System.out.println("[Lore] " + who + " has crossed the Seam and left the Garden.");
        m.deathCause = "crossed the Seam";
        m.dead();
    }

    /** The Hollow brushes the Garden now and then: nothing happens but a strange report, more often as the years pass. */
    static void omens(int year) {
        State s = state;
        if (year < 20) return;
        int chance = Math.min(30, 3 + year / 40);
        if (GameDriver.rand.nextInt(100) >= chance) return;
        ArrayList<Area> cities = new ArrayList<>();
        for (Area a : Map.createdAreas) if (a != null && a.city) cities.add(a);
        Area at = cities.isEmpty() || GameDriver.rand.nextInt(3) == 0 ? palaceArea() : cities.get(GameDriver.rand.nextInt(cities.size()));
        String text = LoreText.OMENS[GameDriver.rand.nextInt(LoreText.OMENS.length)].replace("{place}", at == null ? "Originis" : at.name);
        s.omens++;
        Chronicle.add("world", "Omen: " + text);
        System.out.println("[Omen] " + text);
        for (Character h : Notifications.humans()) {
            if (h == null || h.dead || h.player != 1 || !isMember(h)) continue;
            PState p = s.humans.get(h.id);
            if (p != null && p.found.contains("thinsky")) Notifications.post("lore", "An omen: " + text, -1, h.id);
            if (p != null && !p.ended) queueScene(p, "omen", true);
        }
    }

    // ------------------------------------------------------------------ the Convocation (the entrance tournament)
    /** Area.runTournament calls this first: the Convocation at Originis is judged here instead (returns true when it was). */
    static boolean runConvocation(Area a) {
        State s = state;
        if (s == null || !Settings.lore || !s.convocationOpen || a != palaceArea()) return false;
        Organization pal = palace();
        if (pal == null) return false;
        s.convocationOpen = false;
        List<Character> pool = new ArrayList<>();
        for (Character c : new ArrayList<>(a.tournamentRegistrants)) if (c != null && !c.dead && c.born && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster) pool.add(c);
        a.tournamentRegistrants.clear(); a.tournamentPrize = null; a.tournamentOrganizer = null;
        // The call carries to every land: unaffiliated cultivators below the Fifth Gate answer it from where they live (they are judged here and walk to
        // Originis afterwards). Without this only the few who happened to stand in Originis competed: 37 of 100,000 in a spread world.
        {
            java.util.HashSet<Character> in = new java.util.HashSet<>(pool);
            int total = Map.characters.size(), want = Math.min(1500, Map.population / 60);
            for (int tries = 0, got = 0; total > 0 && tries < want * 4 && got < want; tries++) {
                Character c = Map.characters.get(GameDriver.rand.nextInt(total));
                if (c == null || c.dead || !c.born || c.player != 0 || c.sect != null || c.age < 14 || c.majorLvl > 4 || c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster || !in.add(c)) continue;
                pool.add(c); got++;
            }
        }
        s.held++;
        int n = pool.size();
        java.util.Random rand = GameDriver.rand;
        Collections.shuffle(pool, rand);
        java.util.IdentityHashMap<Character, Integer> reached = new java.util.IdentityHashMap<>();
        java.util.IdentityHashMap<Character, StringBuilder> story = new java.util.IdentityHashMap<>();
        int round = 1;
        List<Character> cur = new ArrayList<>(pool);
        while (cur.size() > 1) {
            List<Character> next = new ArrayList<>();
            for (int i = 0; i + 1 < cur.size(); i += 2) {
                Character x = cur.get(i), y = cur.get(i + 1);
                x.tempLvlCheck(); y.tempLvlCheck();
                int wc = Math.max(5, Math.min(95, 50 - 15 * (y.tempLvl - x.tempLvl)));
                if (x.trait == Character.Traits.Lucky) wc = Math.min(95, wc + 45);
                if (y.trait == Character.Traits.Lucky) wc = Math.max(5, wc - 45);
                boolean xWins = rand.nextInt(100) < wc;
                Character w = xWins ? x : y, l = xWins ? y : x;
                reached.put(l, round);
                next.add(w);
                note(story, w, "round " + round + ": you beat " + l.name + " " + l.lastName + " (Lv." + l.lvl + ")");
                note(story, l, "round " + round + ": you fell to " + w.name + " " + w.lastName + " (Lv." + w.lvl + ")");
            }
            if (cur.size() % 2 == 1) { Character bye = cur.get(cur.size() - 1); next.add(bye); note(story, bye, "round " + round + ": a bye"); }
            cur = next; round++;
        }
        int rounds = round - 1;
        Character champion = cur.isEmpty() ? null : cur.get(0);
        if (champion != null) reached.put(champion, round);
        // the best of them are let in: a third of the field, at most a few hundred in a big world
        int quota = Math.max(1, Math.min(Math.max(4, Map.startingPopulation / 400), (n + 2) / 3));
        List<Character> order = new ArrayList<>(pool);
        Collections.sort(order, (x, y) -> { int r = Integer.compare(reached.get(y), reached.get(x)); return r != 0 ? r : Integer.compare(y.tempLvl, x.tempLvl); });
        int admitted = 0;
        for (Character c : order) {
            boolean in = admitted < quota && (c.sect == null || c.player == 1) && (pal.members.size() < pal.memberCap() || c.player == 1);
            if (in) {
                admitted++; s.passes.add(c.id);
                if (c.player == 0) { pal.join(c); if (c.sect == pal) { if (s.seekers == null) s.seekers = new HashMap<>(); s.seekers.put(c.id, 0); } } // those who win a place become seekers
            }
            if (c.player == 1) {
                PState p = s.humans.get(c.id);
                if (p != null) {
                    p.result = in ? "PASS" : "FAIL"; p.resultRound = reached.get(c); p.resultRounds = rounds; p.resultYear = s.convocationYear; p.tries++;
                    System.out.println("Convocation of the First Gate, Year " + s.convocationYear + ": " + (story.get(c) == null ? "no bouts" : story.get(c).toString()));
                }
            }
        }
        if (champion != null && !champion.dead) {
            try {
                Item prize = new Item(champion, Item.WeaponType.values()[rand.nextInt(Item.WeaponType.values().length)], Math.max(1, Math.min(10, champion.majorLvl + 2)));
                champion.backpack.add(prize); champion.equip();
                Starts.givePills(champion, Item.PillType.Cultivation, Math.max(1, Math.min(10, champion.majorLvl + 1)), 2);
            } catch (RuntimeException ignored) { /* a prize is a courtesy: never let it stop the Gate */ }
        }
        String line = "The Convocation of the First Gate (Year " + s.convocationYear + "): " + n + " came, " + admitted + " were let in" + (champion == null ? "." : "; " + champion.name + " " + champion.lastName + " won the final (" + rounds + " rounds).");
        s.lastResult = line;
        System.out.println("=== " + line + " ===");
        Chronicle.add("world", line, champion == null ? new int[0] : new int[]{champion.id});
        return true;
    }

    private static void note(java.util.IdentityHashMap<Character, StringBuilder> story, Character c, String what) {
        if (c.player != 1) return;
        StringBuilder sb = story.get(c);
        if (sb == null) { sb = new StringBuilder(); story.put(c, sb); } else sb.append("; ");
        sb.append(what);
    }

    /** A human leaves the Palace of their own will: the pass stays, but nothing joins them again until they say so (`lore join`). */
    static void onResign(Character c) { PState p = pstate(c); if (p != null) p.declined = true; }

    // ------------------------------------------------------------------ a human's turn
    static PState pstate(Character c) { State s = state; return s == null || c == null ? null : s.humans.get(c.id); }

    private static PState pstateFor(Character c, State s) {
        PState p = s.humans.get(c.id);
        if (p != null) return p;
        Integer orphan = null;
        for (java.util.Map.Entry<Integer, PState> e : s.humans.entrySet()) {
            Character b = Character.charById(e.getKey());
            if (b == null || b.dead || b.player != 1) { orphan = e.getKey(); break; }
        }
        if (orphan != null) { p = s.humans.remove(orphan); s.humans.put(c.id, p); p.bodyId = c.id; p.questUid = -1; p.questLabel = ""; queueScene(p, "another_face", true); return p; }
        p = new PState(); p.bodyId = c.id; p.lives = c.lives;
        s.humans.put(c.id, p);
        return p;
    }

    /** Every turn of every human adult (called from GameDriver.playerTurn): the story advances, ??? speaks. */
    static void tick(Character c) {
        State s = state;
        if (s == null || !Settings.lore || c == null || c.dead || c.player != 1) return;
        Organization pal = palace(); Area home = palaceArea();
        if (pal == null || home == null || c.location == null) { PState q = s.humans.get(c.id); if (q != null) LoreEndings.tick(c, q); return; } // the Palace is gone: the footprints and the endings still run
        PState p = pstateFor(c, s);
        if (c.majorLvl > p.peakMajor) p.peakMajor = c.majorLvl;
        if (c.sectPoints > p.peakPoints) p.peakPoints = c.sectPoints;
        if (p.ended) { LoreEndings.tick(c, p); whisper(c, p); flush(c, p); return; }
        boolean member = c.sect == pal;
        if (member && !p.everMember) firstJoined(c, p);
        if (c.lives > p.lives) { p.lives = c.lives; if (p.everMember) queueScene(p, "rope", true); }
        if (!p.everMember && c.location == home && p.scenes.isEmpty() && p.queue.isEmpty()) queueScene(p, "bell", false);
        if (c.location.loreSite != null && !c.location.isOriginis()) { LoreSites.arrive(c, p, c.location); LoreEndings.tick(c, p); }
        convocation(c, p, s, home, pal);
        invite(c, p, s, pal);
        join(c, p, s, pal);
        if (c.sect == pal && c.location == home && p.step >= STEP_SEAT && !p.found.contains(LoreSites.SEAT) && p.found.size() >= LoreSites.ALL.length
                && pal.getRank(c) == Organization.SectRank.ELDER) readSeat(c, p);
        advance(c, p, s, pal);
        enforceRank(c, p, s, pal);
        questFor(c, p, s, home, pal);
        levelScenes(c, p);
        whisper(c, p);
        flush(c, p);
    }

    private static void firstJoined(Character c, PState p) {
        p.everMember = true; p.declined = false;
        for (int i = 0; i < LEVEL_SCENE_MAJOR.length; i++) if (LEVEL_SCENE_MAJOR[i] < p.peakMajor) p.scenes.add(LEVEL_SCENE_ID[i]); // an old hand is not told about the realms already behind them
        Hints.trigger(c, "lore-intro", "The Palace's voice speaks now and then. 'lore' opens your journal of what you have learned, 'lore palace' shows the Order.");
    }

    private static boolean inBand(Character c, Area home) { return c.majorLvl >= home.tournamentMinMajor && c.majorLvl <= home.tournamentMaxMajor; }

    private static void convocation(Character c, PState p, State s, Area home, Organization pal) {
        boolean member = c.sect == pal;
        if (s.convocationOpen && c.location == home && !member && p.registeredYear != s.convocationYear && inBand(c, home)
                && !home.tournamentRegistrants.contains(c) && c.pc() != null && c.pc().mode != PlayerControl.Mode.MANUAL) {
            home.tournamentRegistrants.add(c); // a human on autopilot does what the quest asks
            say(c, "You registered for the " + home.tournamentName + ".");
        }
        if (s.convocationOpen && home.tournamentRegistrants.contains(c) && p.registeredYear != s.convocationYear) {
            p.registeredYear = s.convocationYear;
            queueScene(p, "register", false);
        }
        if (p.result != null && !p.result.equals("INVITE")) {
            String r = p.result;
            if (r.equals("PASS")) queueScene(p, "pass", true); else queueScene(p, "fail", true);
        }
    }

    /** A human who crossed the second gate after a Convocation has been judged is noticed: the Palace sends for them (so the way in never closes). */
    private static void invite(Character c, PState p, State s, Organization pal) {
        if (c.sect == pal || s.passes.contains(c.id) || c.majorLvl < 2 || s.held < 1) return;
        if (s.convocationOpen && palaceArea().tournamentRegistrants.contains(c)) return; // they entered this year: let the Convocation speak first
        s.passes.add(c.id);
        p.result = "INVITE";
        queueScene(p, "invite", true);
    }

    private static void join(Character c, PState p, State s, Organization pal) {
        if (c.sect == pal || !s.passes.contains(c.id) || p.declined) return;
        if (p.result == null && !p.queue.isEmpty()) return;
        if (c.sect != null) {
            boolean yes = DecisionBroker.askYesNo(c, "The " + LoreText.PALACE + " has let you in. Leave " + c.sect.name + " and join it? (You keep your pass: 'lore join' later.)", false);
            if (!yes) { p.declined = true; p.result = null; say(c, "You keep your place in " + c.sect.name + ". The Palace's door stays open for you: 'lore join' at Originis."); return; }
            c.sect.resign(c); c.sectPoints = 0;
        }
        pal.join(c);
        if (c.sect == pal) {
            p.result = null;
            say(c, "You joined the " + LoreText.PALACE + " as " + LoreText.RANK_TITLE[0] + ".");
            Notifications.post("lore", "You joined the " + LoreText.PALACE + ".", -1, c.id);
            Chronicle.add("world", c.name + " " + c.lastName + " joined the " + LoreText.PALACE + ".", c.id);
        } else if (p.result != null) {
            say(c, "The Palace's rolls are full this year; your pass keeps. Try 'lore join' later.");
            p.result = null;
        }
    }

    private static boolean done(Character c, PState p, Step st, Organization pal) {
        boolean member = c.sect == pal;
        switch (st.kind) {
            case "REGISTER": return p.registeredYear > 0 || p.everMember;
            case "ADMIT": return p.everMember;
            case "SITE": return p.found.contains(st.ids[0]);
            case "SITES": return foundOf(p, st.ids) >= st.n;
            case "POINTS": return p.peakPoints >= st.n;
            case "MAJOR": return p.peakMajor >= st.n && (st.promote == null || member);
            case "ELDER": return p.peakMajor >= st.n && p.peakPoints >= 200 && member;
            case "SEAT": return p.found.contains(LoreSites.SEAT);
            case "CROSS": return p.ended;
            default: return false;
        }
    }

    static int foundOf(PState p, String[] ids) { int n = 0; for (String i : ids) if (p.found.contains(i)) n++; return n; }
    static int foundInTier(PState p, int tier) { int n = 0; for (LoreSites.Site s : LoreSites.ALL) if (s.tier == tier && p.found.contains(s.id)) n++; return n; }

    private static void advance(Character c, PState p, State s, Organization pal) {
        if (p.step == 0 && p.revealed.isEmpty()) enter(c, p, s, CHAIN[0]);
        for (int guard = 0; guard < 8 && p.step < CHAIN.length; guard++) {
            Step st = CHAIN[p.step];
            if (!done(c, p, st, pal)) break;
            complete(c, p, s, pal, st);
            p.step++;
            if (p.step < CHAIN.length) enter(c, p, s, CHAIN[p.step]);
        }
    }

    /** A step becomes the current one: the places it names are revealed (their tiles come into being and are known to the human). */
    private static void enter(Character c, PState p, State s, Step st) {
        if (st.ids != null) for (String id : st.ids) reveal(c, p, s, id);
        if (p.step >= 1) System.out.println("[Lore] " + LoreText.ACT_NAMES[st.act] + ": " + st.text);
        if (p.step >= 1) Notifications.post("lore", "The Order asks: " + st.text, -1, c.id);
    }

    private static void reveal(Character c, PState p, State s, String id) {
        int i = LoreSites.index(id);
        if (i < 0 || p.revealed.contains(id)) return;
        Character keep = Map.currentCharacter;
        Map.currentCharacter = c;
        Area a = LoreSites.ensureTile(s, i);
        Map.currentCharacter = keep;
        if (a == null) return;
        p.revealed.add(id);
        if (c.knownLocations != null && !c.knownLocations.contains(a)) c.knownLocations.add(a);
    }

    private static void complete(Character c, PState p, State s, Organization pal, Step st) {
        int idx = p.step;
        String note = "Step done: " + st.text;
        switch (idx) {
            case 3: Starts.givePills(c, Item.PillType.Cultivation, 1, 3); note += "  (3 Qi Cultivation Pills)"; break;
            case STEP_INNER:
                if (pal.getRank(c) == Organization.SectRank.OUTER_DISCIPLE) pal.promoteRank(c);
                Starts.giveGear(c, Math.max(1, Math.min(10, c.majorLvl + 1))); queueScene(p, "inner", true);
                note += "  (Inner Court; gear of the Order)"; break;
            case 5: Starts.givePills(c, Item.PillType.Potential, 1, 1); note += "  (a Potential Pill)"; break;
            case 6: Starts.givePills(c, Item.PillType.Cultivation, Math.max(1, Math.min(10, c.majorLvl + 1)), 3); note += "  (3 Cultivation Pills)"; break;
            case 7: Starts.givePills(c, Item.PillType.Potential, 1, 1); note += "  (a Potential Pill)"; break;
            case STEP_ELDER: {
                int lamp = lampFor(s, pal, c);
                if (lamp >= 0) { p.lampIdx = lamp; pal.memberRanks.put(c.id, Organization.SectRank.ELDER); Starts.givePills(c, Item.PillType.Potential, 1, 2); queueScene(p, "elder", true); note += "  (the Lamp of " + LoreText.LAMPS[lamp] + "; 2 Potential Pills)"; }
                else note += "  (the Nine Lamps are all held by humans: you wait for one to be free)";
                break;
            }
            case 9: Starts.giveGear(c, Math.max(1, Math.min(10, c.majorLvl + 1))); note += "  (gear of the Order)"; break;
            case 10: Starts.givePills(c, Item.PillType.Cultivation, Math.max(1, Math.min(10, c.majorLvl + 1)), 3); note += "  (3 Cultivation Pills)"; break;
            default: break;
        }
        if (idx >= 2) { System.out.println("[Lore] " + note); Notifications.post("lore", note, -1, c.id); }
    }

    /** A member's rank follows the chain (a human who left and came back is not a Wick again). */
    private static void enforceRank(Character c, PState p, State s, Organization pal) {
        if (c.sect != pal) return;
        Organization.SectRank r = pal.getRank(c);
        if (p.step > STEP_INNER && r == Organization.SectRank.OUTER_DISCIPLE) pal.promoteRank(c);
        if (p.step > STEP_ELDER && r != Organization.SectRank.ELDER && p.lampIdx >= 0 && s.lamps[p.lampIdx] == c.id) pal.memberRanks.put(c.id, Organization.SectRank.ELDER);
    }

    // ------------------------------------------------------------------ the quest in the human's list
    private static Quest findQuest(Character c, int uid) {
        if (uid < 0) return null;
        for (Quest q : c.targetQuests) if (q.uid == uid) return q;
        for (Quest q : c.spotQuests) if (q.uid == uid) return q;
        if (c.currentQuest != null && c.currentQuest.uid == uid) return c.currentQuest;
        return null;
    }

    static void dropQuest(Character c, PState p) {
        Quest cur = findQuest(c, p.questUid);
        if (cur != null) { c.targetQuests.remove(cur); c.spotQuests.remove(cur); if (c.currentQuest == cur) c.currentQuest = null; }
        p.questUid = -1; p.questLabel = "";
    }

    private static void questFor(Character c, PState p, State s, Area home, Organization pal) {
        if (p.step >= CHAIN.length) { dropQuest(c, p); return; }
        Step st = CHAIN[p.step];
        String label; Area target = null;
        boolean member = c.sect == pal;
        switch (st.kind) {
            case "REGISTER": case "ADMIT":
                if (s.convocationOpen && p.registeredYear != s.convocationYear && !member) { label = "Palace: Register for the Convocation of the First Gate (do Register Tournament)"; target = home; }
                else if (s.convocationOpen && !member) label = "Palace: Wait at Originis for the Convocation's verdict";
                else if (!member) label = "Palace: Return to the Convocation (Year " + (s.pending || s.held == 0 ? s.convocationYear : s.convocationYear + 1) + ") or cross the second gate (Major Lvl 2)";
                else label = "Palace: Present yourself to the Order";
                break;
            case "SITE": case "SITES": {
                Area best = null; String bestId = null; int bd = Integer.MAX_VALUE;
                for (String id : st.ids) {
                    if (p.found.contains(id) || !p.revealed.contains(id)) continue;
                    LoreSites.Site site = LoreSites.byId(id); if (site != null && c.majorLvl < site.minMajor) continue; // a place that would still keep its secret is no errand yet
                    int i = LoreSites.index(id); if (i < 0) continue;
                    Area a = Map.tiles[s.siteX[i]][s.siteY[i]];
                    if (a == null) continue;
                    int d = PlayerControl.distance(c.location, a);
                    if (d < bd) { bd = d; best = a; bestId = id; }
                }
                if (best != null) { target = best; label = "Palace: Seek " + LoreSites.title(bestId); }
                else label = "Palace: " + st.text;
                break;
            }
            case "SEAT": label = "Palace: Go to the Empty Seat at Originis"; target = home; break;
            case "CROSS": label = "Palace: Cross the Seam beneath the Empty Seat (lore cross, at Originis)"; target = home; break;
            case "MAJOR": label = "Palace: Reach Major Lvl " + st.n + " (" + REALM[Math.min(10, st.n)] + ")"; break;
            case "ELDER": label = "Palace: Reach Major Lvl 5 and 200 merit to take a lamp"; break;
            case "POINTS": label = "Palace: Earn " + st.n + " merit for the Order"; break;
            default: label = "Palace: " + st.text;
        }
        ensureQuest(c, p, label, target);
    }

    private static void ensureQuest(Character c, PState p, String label, Area target) {
        // Only a WALK is a quest: a waypoint to where the Order points (arriving completes it, AUTO walks there, MANUAL gets one alert). The objectives that
        // have nowhere to go (a realm to reach, merit to earn) live in the Journal, `lore` and the notices: as quests they were re-created every time the
        // player crossed the target tile, which spammed alerts and interrupted every `goto`.
        boolean wantArea = target != null && target != c.location;
        if (!wantArea) { if (p.questUid >= 0) dropQuest(c, p); return; }
        Quest cur = findQuest(c, p.questUid);
        if (cur != null && label.equals(p.questLabel) && cur.targetArea == target) return;
        dropQuest(c, p);
        Quest q = new Quest(label, true, 85, 0, 85, target);
        q.userCreated = true;
        q.pin(85);
        c.targetQuests.add(q);
        p.questUid = q.uid; p.questLabel = label;
    }

    // ------------------------------------------------------------------ ??? speaks
    static void queueScene(PState p, String id, boolean again) {
        if (p == null || p.scenes.contains(id) || p.queue.contains(id)) return;
        p.queue.add(id);
    }

    private static void levelScenes(Character c, PState p) {
        if (!p.everMember) return;
        for (int i = 0; i < LEVEL_SCENE_MAJOR.length; i++) if (p.peakMajor >= LEVEL_SCENE_MAJOR[i]) queueScene(p, LEVEL_SCENE_ID[i], true);
    }

    private static void flush(Character c, PState p) {
        if (p.queue.isEmpty() || GuiLore.busy()) return; // one scene at a time, and not on top of the welcome tour
        String id = p.queue.remove(0);
        if (p.scenes.contains(id)) return;
        show(c, p, id);
    }

    static java.util.Map<String, String> vars(Character c, PState p) {
        java.util.Map<String, String> v = new LinkedHashMap<>();
        v.put("name", c.name + " " + c.lastName); v.put("first", c.name);
        Organization pal = palace();
        Organization.SectRank r = pal != null && c.sect == pal ? pal.getRank(c) : Organization.SectRank.OUTER_DISCIPLE;
        v.put("rank", LoreText.RANK_TITLE[Math.min(2, r.ordinal())]);
        v.put("year", String.valueOf(GameDriver.time / 52 + 1));
        v.put("round", p == null ? "?" : String.valueOf(p.resultRound));
        v.put("lamp", p != null && p.lampIdx >= 0 ? LoreText.LAMPS[p.lampIdx] : "Dawn");
        v.put("convo", state == null ? "?" : "Year " + (state.convocationOpen ? state.convocationYear : state.convocationYear + (state.pending ? 0 : 1)));
        v.put("n", "");
        v.put("lamps", String.valueOf(LoreEndings.lampsCarried(state)));
        v.put("lampNo", String.valueOf(LoreEndings.lampsCarried(state) + 1));
        return v;
    }

    /** Shows a scene now: a window in the GUI (non-modal; the game waits for Continue unless `settings lorepause off`), a framed block in the terminal. */
    static void show(Character c, PState p, String id) {
        LoreText.Scene sc = LoreText.SCENES.get(id);
        if (sc == null) return;
        if (p != null) p.scenes.add(id);
        List<String> lines = LoreText.lines(id, vars(c, p));
        shown.add(id);
        if (GameDriver.guiEnabled && GameDriver.mapViewer != null) {
            System.out.println("[Lore] ??? speaks: " + sc.title + "  ('lore replay " + id + "' reads it again)");
            GuiLore.scene(id, sc.title, lines);
        } else System.out.println(LoreText.frame(sc.title, lines));
    }

    /** One of ???'s rare one-liners, now and then, once the human has met the Palace. */
    private static void whisper(Character c, PState p) {
        if ((!p.everMember && !p.ended) || (state != null && state.silenced)) return;
        int week = GameDriver.time;
        if (week - p.lastWhisper < 104 || GameDriver.rand.nextInt(600) != 0) return;
        int act = p.ended ? 99 : CHAIN[Math.min(p.step, CHAIN.length - 1)].act;
        List<String> pool = new ArrayList<>();
        for (String w : LoreText.WHISPERS) {
            int bar = w.indexOf('|'); int need = Integer.parseInt(w.substring(0, bar));
            if (p.ended ? (need == 99 || (need == 98 && p.darkPath)) : need <= act) pool.add(w.substring(bar + 1));
        }
        if (pool.isEmpty()) return;
        p.lastWhisper = week;
        String text = pool.get(GameDriver.rand.nextInt(pool.size()));
        System.out.println("[Lore] ??? whispers: \"" + text + "\"");
        Notifications.post("lore", "??? whispers: " + text, -1, c.id);
    }

    static void say(Character c, String text) { if (c != null && c.player > 0 && c.skipping <= 0) System.out.println("[Lore] " + text); }

    // ------------------------------------------------------------------ the Empty Seat and the crossing
    private static void readSeat(Character c, PState p) {
        p.found.add(LoreSites.SEAT); p.revealed.add(LoreSites.SEAT);
        String reward = LoreSites.reward(c, LoreSites.SEAT);
        show(c, p, "seat");
        Notifications.post("lore", "You sat beside the Empty Seat and learned the truth (" + reward + ").", -1, c.id);
        Chronicle.add("world", c.name + " " + c.lastName + " sat beside the Empty Seat of the Palace.", c.id);
    }

    /** Why a human cannot cross yet (null when they can). */
    static String whyNotCross(Character c, PState p) {
        Area home = palaceArea();
        if (c.location != home) return "The Seam lies beneath the Empty Seat at Originis.";
        if (c.majorLvl < 10 || c.lvl < 101) return "The Door has not opened for you yet. You are Major Lvl " + c.majorLvl + " (level " + c.lvl + "). The Seam asks for level 101, True Immortal.";
        if (p.found.size() < LoreSites.ALL.length + 1) return "You do not understand enough yet. You have read " + p.found.size() + " of the " + (LoreSites.ALL.length + 1) + " places (nine on the Roads, and the Empty Seat).";
        return null;
    }

    /** `lore cross`: asks the last question and, if answered, ends the story. Returns true if the character crossed. */
    static boolean cross(Character c) {
        State s = state;
        if (s == null) { PlayerCommands.say("The lore is not active in this world."); return false; }
        PState p = s.humans.get(c.id);
        if (p == null) { PlayerCommands.say("The Palace has not met you yet."); return false; }
        if (LoreEndings.stayedOf(p)) return LoreEndings.leave(c, p); // a human who stayed may leave at any time, from anywhere
        if (p.ended) { PlayerCommands.say("You have already chosen: " + p.ending + " (score " + p.score + ")."); return false; }
        String why = whyNotCross(c, p);
        if (why != null) { PlayerCommands.say(why); return false; }
        return LoreEndings.choose(c, p); // the six endings
    }

    // ------------------------------------------------------------------ the `lore` command
    static boolean command(Character c, String[] t) {
        String sub = t.length > 1 ? t[1].toLowerCase() : "";
        State s = state;
        if (sub.equals("enable") || sub.equals("on")) {
            String why = enable();
            PlayerCommands.say(why == null ? "The lore is on. The Palace of the Oldest Order stands at Originis; its gates open at the next New Year." : why);
            return false;
        }
        if (sub.equals("off") || sub.equals("disable")) { Settings.lore = false; Settings.save(); PlayerCommands.say("The lore is off: nothing of it runs or appears (the Palace stays where it is). 'lore on' brings it back."); return false; }
        if (s == null || !Settings.lore) { PlayerCommands.say(s == null ? "This world began without the lore. 'lore enable' founds the Palace of the Oldest Order at Originis (if Originis has no sect)." : "The lore is off ('lore on' or 'settings lore on')."); return false; }
        PState p = pstateFor(c, s);
        switch (sub) {
            case "": case "journal": case "status":
                if (GameDriver.guiEnabled && GameDriver.mapViewer != null && !sub.equals("status")) { GuiLore.journal(GameDriver.mapViewer); return false; }
                GameDriver.showGuiInfo("Lore", describe(c, p));
                return false;
            case "clues": GameDriver.showGuiInfo("Lore: what you have learned", cluesText(p)); return false;
            case "where": GameDriver.showGuiInfo("Lore: places", whereText(c, p)); return false;
            case "palace": GameDriver.showGuiInfo("The " + LoreText.PALACE, palaceText(c, p)); return false;
            case "scenes": {
                StringBuilder sb = new StringBuilder("=== Scenes you have seen (lore replay <id>) ===\n");
                for (String id : p.scenes) { LoreText.Scene sc = LoreText.SCENES.get(id); sb.append("  ").append(id).append("  -  ").append(sc == null ? "" : sc.title).append('\n'); }
                if (p.scenes.isEmpty()) sb.append("  (none yet)\n");
                GameDriver.showGuiInfo("Lore: scenes", sb.toString());
                return false;
            }
            case "replay": {
                String id = t.length > 2 ? t[2].toLowerCase() : "";
                if (!p.scenes.contains(id)) { PlayerCommands.say("You have not seen '" + id + "'. 'lore scenes' lists what you have."); return false; }
                List<String> lines = LoreText.lines(id, vars(c, p));
                if (GameDriver.guiEnabled && GameDriver.mapViewer != null) GuiLore.scene(id, LoreText.SCENES.get(id).title, lines);
                else PlayerCommands.say(LoreText.frame(LoreText.SCENES.get(id).title, lines));
                return false;
            }
            case "register": {
                Area home = palaceArea();
                if (c.location != home) { PlayerCommands.say("Register at Originis."); return false; }
                if (c.sect == palace()) { PlayerCommands.say("You are already in the Palace."); return false; }
                return PlayerCommands.handle(c, "!do Register Tournament");
            }
            case "join": {
                Organization pal = palace();
                if (c.location != palaceArea()) { PlayerCommands.say("Go to Originis first."); return false; }
                if (c.sect == pal) { PlayerCommands.say("You are already in the Palace."); return false; }
                if (!s.passes.contains(c.id)) { PlayerCommands.say("The Palace has not let you in yet: " + whyNotEnter(c) + "."); return false; }
                p.declined = false;
                join(c, p, s, pal);
                return c.sect == pal;
            }
            case "cross": return cross(c);
            case "leave": return LoreEndings.leave(c, pstate(c));
            case "usurp": return LoreEndings.usurp(c);
            case "roll": GameDriver.showGuiInfo("The Roll of the Crossed", rollText()); return false;
            case "hall": GameDriver.showGuiInfo("Hall of fame", RunStats.hallOfFame()); return false;
            default:
                PlayerCommands.say("Usage: lore [journal|clues|where|palace|roll|hall|scenes|replay <id>|register|join|cross|leave|on|off|enable]");
                return false;
        }
    }

    // ------------------------------------------------------------------ the journal (plain text; the GUI journal shows the same facts)
    static String actName(PState p) { return p.ended ? "The Story Is Told" : LoreText.ACT_NAMES[CHAIN[Math.min(p.step, CHAIN.length - 1)].act]; }

    static String rankText(Character c, Organization pal) {
        if (pal == null || c.sect != pal) return "not a member";
        Organization.SectRank r = pal.getRank(c);
        return LoreText.RANK_TITLE[Math.min(2, r.ordinal())] + " (" + r + ")";
    }

    static String objective(Character c, PState p) {
        if (p.ended) return "None. You chose: " + p.ending + " (score " + p.score + ")." + (LoreEndings.stayedOf(p) ? " The Seam will open for you again: 'lore leave'." : "") + (p.darkPath && !p.secretDone ? " The Seat is warm." : "");
        Step st = CHAIN[Math.min(p.step, CHAIN.length - 1)];
        return st.text;
    }

    static String whereText(Character c, PState p) {
        State s = state;
        StringBuilder sb = new StringBuilder("=== The places of the Oldest Order ===\n");
        for (int i = 0; i < LoreSites.ALL.length; i++) {
            LoreSites.Site site = LoreSites.ALL[i];
            boolean found = p.found.contains(site.id), known = p.revealed.contains(site.id);
            sb.append(found ? "  [x] " : known ? "  [ ] " : "  [?] ");
            if (known || found) sb.append(site.title).append(" [").append(s.siteX[i] * 100).append(", ").append(s.siteY[i] * 100).append("]").append(found ? "" : "  (" + c.location.x + "," + c.location.y + " -> " + PlayerControl.distance(c.location, Map.tiles[s.siteX[i]][s.siteY[i]]) + " tiles; Major Lvl " + site.minMajor + "+)");
            else sb.append("(not yet known)");
            sb.append('\n');
        }
        sb.append(p.found.contains(LoreSites.SEAT) ? "  [x] The Empty Seat (Originis)\n" : "  [?] The Empty Seat (open to an Elder who has read the nine)\n");
        return sb.toString();
    }

    static String rollText() {
        State s = state;
        StringBuilder sb = new StringBuilder("=== The Roll of the Crossed ===\n");
        if (s == null || s.roll.isEmpty()) sb.append("  Nobody has opened the Door yet.\n");
        else for (int i = 0; i < s.roll.size(); i++) sb.append(String.format("%3d. %s%n", i + 1, s.roll.get(i)));
        if (s != null && s.wanderers != null && !s.wanderers.isEmpty()) sb.append("Wandering Immortals walking the Garden: ").append(s.wanderers.size()).append('\n');
        return sb.toString();
    }

    static String cluesText(PState p) {
        StringBuilder sb = new StringBuilder("=== What you have learned ===\n");
        int n = 0;
        for (String id : p.found) {
            LoreText.Clue cl = LoreText.CLUES.get(id);
            if (cl == null) continue;
            sb.append(++n).append(". ").append(cl.title).append("\n     ").append(cl.truth).append('\n');
        }
        if (n == 0) sb.append("  Nothing yet. The Palace's roads lead to nine places; the Wayhouse is the first.\n");
        if (p.prints != null && !p.prints.isEmpty()) {
            sb.append("Giant Footprints (").append(p.prints.size()).append(" of ").append(LoreSites.FOOT_NAMES.length).append("):\n");
            for (int k = 0; k < LoreSites.FOOT_NAMES.length; k++) if (p.prints.contains("foot" + k)) sb.append("  ").append(LoreSites.FOOT_NAMES[k]).append(": ").append(LoreSites.FOOT_CLUES[k]).append('\n');
        }
        return sb.toString();
    }

    static String palaceText(Character c, PState p) {
        State s = state; Organization pal = palace();
        StringBuilder sb = new StringBuilder("=== The " + LoreText.PALACE + " ===\n");
        sb.append("Master: ???   Members: ").append(pal.members.size()).append('/').append(pal.memberCap()).append("   Rank: ").append(rankText(c, pal)).append('\n');
        sb.append("Convocation of the First Gate: ");
        if (s.convocationOpen) sb.append("open now (Year ").append(s.convocationYear).append("), ").append(palaceArea().tournamentTimer).append(" weeks left, ").append(palaceArea().tournamentRegistrants.size()).append(" registered\n");
        else sb.append("next at the New Year (Year ").append(s.pending ? s.convocationYear : s.convocationYear + 1).append(")\n");
        if (!s.lastResult.isEmpty()) sb.append("Last: ").append(s.lastResult).append('\n');
        sb.append("The Nine Lamps:\n");
        for (int i = 0; i < 9; i++) {
            Character h = s.lamps[i] >= 0 ? Character.charById(s.lamps[i]) : null;
            sb.append("  Lamp of ").append(LoreText.LAMPS[i]).append(": ").append(h == null ? "dark" : h.name + " " + h.lastName + " (Major " + h.majorLvl + (h.player == 1 ? ", you" : "") + ")").append('\n');
        }
        sb.append("The Empty Seat: ???\n");
        if (s.crossed > 0) sb.append("Crossed the Seam: ").append(s.crossed).append("  (").append(String.join("; ", s.roll.subList(Math.max(0, s.roll.size() - 3), s.roll.size()))).append(")\n");
        return sb.toString();
    }

    static String describe(Character c, PState p) {
        State s = state; Organization pal = palace();
        StringBuilder sb = new StringBuilder("=== The Oldest Order ===\n");
        sb.append("Act ").append(actName(p)).append("\nRank: ").append(rankText(c, pal)).append("   merit ").append(c.sectPoints).append("\n\n");
        sb.append("The road so far:\n");
        for (int i = 0; i < CHAIN.length; i++) {
            Step st = CHAIN[i];
            if (i > p.step && i > 1) { if (i == p.step + 1) sb.append("  [ ] ...\n"); continue; }
            sb.append(i < p.step ? "  [x] " : "  [>] ").append(st.text).append('\n');
        }
        if (p.step == 0 || p.step == 1) {
            sb.append("\nConvocation: ").append(s.convocationOpen ? "open now (Year " + s.convocationYear + ")" : "next at the New Year").append(". ").append(p.result != null ? "" : "Anyone who crosses Major Lvl 2 after the first one is judged is welcome without a trial.").append('\n');
        }
        sb.append('\n').append(cluesText(p));
        sb.append('\n').append("Commands: lore clues | where | palace | scenes | replay <id> | register | join | cross | leave\n");
        return sb.toString();
    }

    // ------------------------------------------------------------------ saves
    static void snapshotInto(SaveData d) { d.lore = state; }
    static void restoreFrom(SaveData d) { state = d.lore; shown.clear(); }
}
