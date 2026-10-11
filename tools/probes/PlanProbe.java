package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-09 (second batch): planned moves (an NPC acts where it stands and only plans one step; the steps are taken together), a chaser's aim at a walker's planned step,
 *  a party member riding with his leader, Join Sect cleared when a sect is joined, `invite <id>`, the tournament sign-up for a person who plays, the epoch's messages and settings. */
public class PlanProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    static void check(String name, boolean ok) { PcProbe.passes += ok ? 1 : 0; PcProbe.fails += ok ? 0 : 1; PcProbe.real.println("[T] " + (ok ? "PASS " : "FAIL ") + name); }

    public static void main(String[] a) throws Exception {
        PcProbe.real = System.out;
        System.setIn(PcProbe.in);
        GameDriver.frozenNpcs = true; // frozen before the first tick: the NPCs act before the human prompt of a tick, and one of them could otherwise ask the character a question before the probe is ready
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(PcProbe.real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        try { run(); } catch (Throwable e) { PcProbe.real.println("[T] PROBE EXCEPTION " + e); e.printStackTrace(PcProbe.real); PcProbe.fails++; }
        PcProbe.real.println("[T] ===== " + PcProbe.passes + " passed, " + PcProbe.fails + " failed; gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }

    static Area home() { return Map.tiles[Map.range / 2][Map.range / 2]; }
    static Character adult(int lvl, int major) {
        Character c = new Character(home());
        c.age = 30; c.gender = "Male"; c.lvl = lvl; c.majorLvl = major; c.maxAge = 100000; c.caution = 50; c.sect = null;
        if (c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) c.trait = Character.Traits.Friendly;
        while (Map.characters.size() < c.id) Map.characters.add(null); // (the id is the index, as in a running game)
        Map.characters.add(c); Map.population++;
        return c;
    }
    static void tp(Character c, Area to) { c.location.removeChar(c); c.location = to; to.addChar(c); }
    /** One planning turn of c: the step is planned, nothing moves until applyMove. */
    static void planTurn(Character c, Runnable turn) { c.planning = true; turn.run(); c.planning = false; }
    static boolean listed(Character c, Area at) { return at.characters.contains(c); }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        GameDriver.frozenNpcs = true;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        p.pc().squadPolicy = 2; p.pc().friendPolicy = 1;
        GameDriver.time = Math.max(GameDriver.time, 100);
        Area h = home();

        // ---- planned steps
        Character w = adult(30, 3);
        planTurn(w, () -> w.movement('d'));
        Area east = w.pendTo;
        check("P1 a planned step moves nobody: still on the old tile until it is taken", w.location == h && listed(w, h) && east != null && east.x == Math.floorMod(h.x + 1, Map.range) && east.y == h.y && !listed(w, east));
        w.applyMove();
        check("P2 taking the step moves him to the new tile's list and out of the old one", w.location == east && listed(w, east) && !listed(w, h) && w.pendTo == null);
        planTurn(w, () -> { w.movement('Q'); w.movement('d'); w.movement('w'); });
        Area nw = w.pendTo;
        check("P3 one step a turn: a diagonal is one step, a second request the same turn is ignored", nw != null && nw.x == h.x && nw.y == Math.floorMod(h.y + 1, Map.range));
        w.applyMove();
        tp(w, h);

        // ---- walking to a place three tiles east: arrival is found at the start of the turn after the last step
        Area far = h;
        for (int i = 0; i < 3; i++) { Area cur = w.location; planTurn(w, () -> w.movement('d')); w.applyMove(); far = w.location; }
        tp(w, h);
        final Area dest = far;
        int[] results = new int[5]; int turns = 0;
        for (int i = 0; i < 5; i++) { final int ii = i; planTurn(w, () -> results[ii] = w.pathfinder(dest)); w.applyMove(); turns++; if (results[i] == 1) break; }
        check("P4 a walk of three tiles reports arrival only when he stands on the place (" + Arrays.toString(results) + ", at " + w.location.x + "," + w.location.y + ")",
                results[0] == 0 && results[1] == 0 && results[2] == 0 && results[3] == 1 && w.location == dest);

        // ---- an NPC acts first and wanders last: with no quest to walk, one wander step, planned
        Character n = adult(30, 3); n.trait = Character.Traits.Friendly; n.targetQuests.clear(); n.currentQuest = null;
        int moves = 0, still = 0;
        for (int i = 0; i < 60; i++) {
            n.pendTo = null; n.movedTick = -1; n.exploreTicks = 0;
            Area before = n.location;
            planTurn(n, () -> GameDriver.actThenWander(n));
            boolean stayedPut = n.location == before; // (nothing is taken yet)
            if (n.pendTo != null) moves++; else still++;
            if (!stayedPut) { check("P5 a turn never moves him before the step is taken", false); break; }
            n.applyMove();
            if (n.location != before) tp(n, before);
        }
        check("P5 a turn plans at most one step and moves nobody before the steps are taken (" + moves + " steps, " + still + " stands in 60 turns)", moves > 20 && moves + still == 60);

        // ---- a party member who stands with his leader rides with him
        Character lead = adult(30, 3), fol = adult(30, 3);
        fol.partyLeaderId = lead.id; lead.followerIds = new ArrayList<>(); lead.followerIds.add(fol.id);
        List<Character> moved = new ArrayList<>();
        planTurn(lead, () -> lead.movement('d')); moved.add(lead);
        planTurn(fol, () -> fol.ride(lead)); moved.add(fol);
        GameDriver.applyMoves(moved, moved);
        check("P6 the follower goes where the leader's step took him", lead.location == fol.location && lead.location != h && listed(fol, fol.location));
        tp(lead, h); tp(fol, h); fol.partyLeaderId = -1; lead.followerIds.clear();

        // ---- the chaser's aim: a walker whose step is planned is met where he goes (or on the way, when he is too far ahead)
        Character walker = adult(30, 3);
        Area e1 = null, e4 = null; { Character mk = adult(30, 3); Area cur = h; for (int i = 1; i <= 8; i++) { final Character fm = mk; planTurn(mk, () -> fm.movement('d')); mk.applyMove(); if (i == 1) e1 = mk.location; if (i == 4) e4 = mk.location; if (i == 8) cityOf(mk.location); } tp(mk, h); mk.dead = true; mk.location.removeChar(mk); }
        Area far8 = Map.tiles[Math.floorMod(h.x + 8, Map.range)][h.y];
        tp(walker, h);
        walker.currentQuest = new Quest("Go Sell", true, 90, 1, 100, far8);
        Character chaser = p;
        GameDriver.movesPlanned = true;
        walker.turnT = GameDriver.time;
        planTurn(walker, () -> walker.pathfinder(far8));
        Area stepped = walker.pendTo;
        // the chaser stands two tiles behind (west), the walker goes east at the same speed: it cannot be caught from behind, so it is met where the walker is going
        Area behind = Map.tiles[Math.floorMod(h.x - 2, Map.range)][h.y];
        if (behind == null) { Character mk = adult(30, 3); planTurn(mk, () -> mk.movement('a')); mk.applyMove(); planTurn(mk, () -> mk.movement('a')); mk.applyMove(); behind = mk.location; mk.dead = true; mk.location.removeChar(mk); }
        tp(chaser, behind);
        int[] aim = chaser.interceptAim(walker);
        check("P7 a walker too far ahead to be caught from behind is met at the place he is heading for (aim " + (aim == null ? "null" : aim[0] + "," + aim[1]) + ", place " + far8.x + "," + far8.y + ")", aim != null && aim[0] == far8.x && aim[1] == far8.y);
        // the chaser stands off to the side and ahead: he is met on the walker's way
        Area side = Map.tiles[Math.floorMod(h.x + 5, Map.range)][Math.floorMod(h.y + 3, Map.range)];
        if (side == null) { Character mk = adult(30, 3); tp(mk, Map.tiles[Math.floorMod(h.x + 5, Map.range)][h.y]); planTurn(mk, () -> mk.movement('w')); mk.applyMove(); planTurn(mk, () -> mk.movement('w')); mk.applyMove(); planTurn(mk, () -> mk.movement('w')); mk.applyMove(); side = mk.location; mk.dead = true; mk.location.removeChar(mk); }
        tp(chaser, side);
        aim = chaser.interceptAim(walker);
        boolean onWay = aim != null && aim[1] == h.y && Character.chebXY(side.x, side.y, aim[0], aim[1]) <= Math.floorMod(aim[0] - h.x, Map.range);
        check("P8 a chaser off to the side meets him on his way, a tile he can reach no later than the walker does (aim " + (aim == null ? "null" : aim[0] + "," + aim[1]) + ")", onWay);
        // adjacent: go to the tile he steps to
        Area adjacent = Map.tiles[Math.floorMod(h.x + 1, Map.range)][Math.floorMod(h.y + 1, Map.range)];
        if (adjacent != null) {
            tp(chaser, adjacent);
            aim = chaser.interceptAim(walker);
            check("P9 next to the walker, the aim is the tile he steps to", aim != null && walker.pendTo != null && aim[0] == walker.pendTo.x && aim[1] == walker.pendTo.y);
        }
        // a walker who plans no step is gone to where he stands
        walker.pendTo = null;
        tp(chaser, behind);
        check("P10 a walker who stays gets no lead: go to him (null aim)", chaser.interceptAim(walker) == null);
        GameDriver.movesPlanned = false; walker.currentQuest = null; walker.pendTo = null; walker.pendFrom = null;
        // without planned steps the old guess is used (UpdateProbe covers it)
        tp(chaser, h);

        // ---- Go There in a live game: a chaser who starts off to the side of a walker meets him on his way
        GameDriver.frozenNpcs = false;
        Settings.lore = false;
        far8.city = true; if (far8.backpack == null) far8.backpack = new ArrayList<>();
        int caught = 0, trials = 5;
        for (int k = 0; k < trials; k++) {
            Character w2 = adult(30, 3); w2.trait = Character.Traits.Friendly; w2.caution = 0; w2.targetQuests.clear(); w2.spotQuests.clear(); w2.currentQuest = null;
            for (int i = 0; i < 8; i++) w2.backpack.add(new Item(w2, Item.WeaponType.Sword, 10));
            tp(w2, h);
            w2.targetQuests.add(new Quest("Go Sell", true, 95, 1, 100, far8));
            tp(p, Map.tiles[Math.floorMod(h.x + 5, Map.range)][Math.floorMod(h.y + 3, Map.range)] != null ? Map.tiles[Math.floorMod(h.x + 5, Map.range)][Math.floorMod(h.y + 3, Map.range)] : side);
            int t0c = GameDriver.time; boolean ok = false; String last = "";
            for (int r = 0; r < 25 && !ok && GameDriver.time - t0c < 40; r++) { // (a new alert wakes the autopilot: ask again, like a player would)
                if (p.location == w2.location) { ok = true; break; }
                int mk2 = PcProbe.mark(); PcProbe.cmd("chase #" + w2.id, 60); last = PcProbe.since(mk2);
                if (last.contains("caught up") || p.location == w2.location) ok = true;
            }
            PcProbe.real.println("[T] info: live chase " + k + ": " + (ok ? "caught" : "not caught") + " after " + (GameDriver.time - t0c) + " weeks (walker at " + w2.location.x + "," + w2.location.y + ", chaser at " + p.location.x + "," + p.location.y + ", the walker's quest: " + (w2.currentQuest == null ? "none" : w2.currentQuest.name) + ")");
            if (ok) caught++;
            w2.targetQuests.clear(); w2.currentQuest = null; tp(w2, h); tp(p, h);
        }
        check("P11 a person who plays catches a walker with Go There from the side in a live game (" + caught + " of " + trials + ")", caught >= trials - 2);
        GameDriver.frozenNpcs = true;

        // ---- Join Sect is finished when a sect is joined, whichever door
        Character founder = adult(30, 3); founder.majorLvl = 3;
        Organization o = new Organization(founder); Area sa = h; o.location = sa; o.setRules(new String[]{"open"}, 0);
        Character joiner = adult(30, 3); joiner.sect = null; joiner.targetQuests.clear();
        joiner.targetQuests.add(new Quest("Join Sect", true, 20, 1, 40, sa));
        joiner.currentQuest = new Quest("Join Sect", true, 20, 1, 40, sa);
        o.join(joiner);
        check("J1 joining a sect clears the Join Sect errands (queue " + joiner.targetQuests.size() + ")", joiner.sect == o && joiner.targetQuests.stream().noneMatch(q -> "Join Sect".equals(q.name)) && joiner.currentQuest == null);
        Quest stale = new Quest("Join Sect", true, 20, 1, 40, sa);
        joiner.targetQuests.add(stale);
        sa.sect = o;
        check("J2 a Join Sect that was queued anyway is dropped by the Quest Log's pruning as gone", gone(joiner, stale));

        // ---- invite by id (a number that is no row of the list)
        Character other = adult(30, 3); tp(p, h); tp(other, h);
        p.pc().peopleView = new ArrayList<>(); p.pc().peopleView.add(other); // a list of one row
        Character viaId = PlayerCommands.person(p, String.valueOf(other.id));
        int m = PcProbe.mark();
        Character viaMissing = PlayerCommands.person(p, "99999999");
        PcProbe.real.println("[T] info: other.id=" + other.id + " size=" + Map.characters.size() + " charById=" + Character.charById(other.id) + " same=" + (Character.charById(other.id) == other) + " loc=" + other.location.x + "," + other.location.y + " p=" + p.location.x + "," + p.location.y + " viaId=" + viaId);
        check("I1 a number that is not a row of the list but is the id of someone in the tile picks him", viaId == other);
        check("I2 a number that is neither says so and how (" + PcProbe.since(m).replace('\n', '/') + ")", viaMissing == null && PcProbe.since(m).contains("No person #99999999"));
        check("I3 a row number still picks the row", PlayerCommands.person(p, "1") == other);

        // ---- the tournament sign-up is offered at once to a person who plays
        h.tournamentTimer = 5; h.tournamentMinMajor = 0; h.tournamentMaxMajor = 9; h.tournamentRegistrants.remove(p);
        p.spotQuests.removeIf(q -> "Register Tournament".equals(q.name));
        int got = 0; for (int i = 0; i < 20; i++) { p.spotQuests.removeIf(q -> "Register Tournament".equals(q.name)); p.quest(); if (p.spotQuests.stream().anyMatch(q -> "Register Tournament".equals(q.name))) got++; }
        check("T1 a tournament open for you puts the sign-up in your quests every turn it is looked at (" + got + " of 20)", got == 20);
        h.tournamentTimer = 0; p.spotQuests.removeIf(q -> "Register Tournament".equals(q.name));

        // ---- the epoch: the message, the three limits
        Character victim = adult(60, 4); victim.deathCause = GameDriver.PURGE_CAUSE; victim.player = 2; // (a followed cultivator: the one kind of NPC whose death is printed)
        m = PcProbe.mark();
        victim.dead();
        String said = PcProbe.since(m);
        victim.needsReincarnation = false;
        check("E1 an epoch death reads 'Heaven has consumed' (" + said.replace('\n', '/') + ")", said.contains("Heaven has consumed") && !said.contains("has died"));
        check("E2 and the Chronicle says so", Chronicle.search("#" + victim.id, "death", 5).toString().contains("consumed by Heaven"));
        Settings.epochPopulation = 200000; Settings.epochTickSeconds = 2.0; Settings.epochCullPercent = 20; Settings.epochCullNumber = 0;
        PlayerCommands.handle(p, "settings epochpop 150000");
        PlayerCommands.handle(p, "settings epochseconds 3.5");
        boolean s1 = Settings.epochPopulation == 150000 && Settings.epochTickSeconds == 3.5;
        PlayerCommands.handle(p, "settings epochcull 30%");
        boolean s2 = Settings.epochCullPercent == 30 && Settings.epochCullNumber == 0;
        PlayerCommands.handle(p, "settings epochcull 5000");
        boolean s3 = Settings.epochCullNumber == 5000 && Settings.epochCullPercent == 30;
        m = PcProbe.mark();
        PlayerCommands.handle(p, "settings epochcull banana");
        boolean s4 = Settings.epochCullNumber == 5000 && PcProbe.since(m).contains("percent like 20%");
        PlayerCommands.handle(p, "settings epochcull 0");
        boolean s5 = Settings.epochCullNumber == 0;
        check("E3 the epoch's three limits can be set and a wrong value is refused (" + s1 + s2 + s3 + s4 + s5 + ")", s1 && s2 && s3 && s4 && s5);
        Settings.save();
        Settings.epochPopulation = 0; Settings.epochTickSeconds = 0; Settings.epochCullPercent = 0; Settings.epochCullNumber = 7;
        Settings.load();
        check("E4 they are saved and loaded again (" + Settings.epochPopulation + ", " + Settings.epochTickSeconds + ", " + Settings.epochCullPercent + "%, " + Settings.epochCullNumber + ")",
                Settings.epochPopulation == 150000 && Settings.epochTickSeconds == 3.5 && Settings.epochCullPercent == 30 && Settings.epochCullNumber == 0);
        Settings.epochPopulation = 200000; Settings.epochTickSeconds = 2.0; Settings.epochCullPercent = 20; Settings.epochCullNumber = 0; Settings.save();
        check("Z no exceptions", !PcProbe.out().contains("Exception"));
    }

    static void cityOf(Area a) { }
    /** Character.targetGone is private: the Quest Log's pruning is its public face (pruneGoneQuests). */
    static boolean gone(Character c, Quest q) {
        try {
            java.lang.reflect.Method mth = Character.class.getDeclaredMethod("targetGone", Quest.class);
            mth.setAccessible(true);
            return (Boolean) mth.invoke(c, q);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
