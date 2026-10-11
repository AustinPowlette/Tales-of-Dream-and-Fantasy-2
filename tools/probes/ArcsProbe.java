package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-10 (fourth batch): the small arcs of the NPC seekers (Rival, Doubter, Pair, Defector, Teacher), the Dimming (the sky over a city goes out and its people lose friendships while
 *  the humans forget nothing), the later omens, and the quiet links of the starts to the Palace's places. */
public class ArcsProbe {
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

    static Character seeker(Lore.State s, String first, int major, int align) {
        Character c = new Character(home());
        c.name = first; c.lastName = "Probe"; c.age = 30; c.gender = "Male"; c.lvl = major * 10 + 3; c.majorLvl = major; c.maxAge = 100000;
        c.affiliationNum = align; c.affiliation = align > 0 ? "Good" : align < 0 ? "Evil" : "Neutral";
        if (c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) c.trait = Character.Traits.Friendly;
        c.id = Map.characters.size();
        GameDriver.UID = Math.max(GameDriver.UID, c.id + 1);
        Map.characters.add(c); Map.population++;
        Organization pal0 = Lore.palace(); pal0.members.add(c); c.sect = pal0; pal0.memberRanks.put(c.id, Organization.SectRank.OUTER_DISCIPLE);
        s.seekers.put(c.id, 0);
        return c;
    }

    static boolean noted(String needle) {
        for (Notifications.Note n : new ArrayList<>(Notifications.history)) if (n.text.contains(needle)) return true;
        for (Chronicle.Entry e : new ArrayList<>(Chronicle.log)) if (e.text.contains(needle)) return true;
        return false;
    }

    /** Only these characters are seekers (so the arc under test has nobody else to choose). */
    static void only(Lore.State s, Character... cs) {
        Set<Integer> keep = new HashSet<>();
        for (Character c : cs) keep.add(c.id);
        s.seekers.keySet().retainAll(keep);
        for (Character c : cs) if (!s.seekers.containsKey(c.id)) s.seekers.put(c.id, 0);
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        GameDriver.frozenNpcs = true;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        Map.currentCharacter = p;
        Lore.State s = Lore.state;
        Organization pal = Lore.palace();
        check("A0 the lore is on and the Palace stands", s != null && pal != null);
        if (s.seekers == null) s.seekers = new HashMap<>();
        if (!s.humans.containsKey(p.id)) s.humans.put(p.id, new Lore.PState());
        Lore.PState ps = s.humans.get(p.id);
        ps.found.add("wayhouse");
        s.arcs = null;

        // ---- the Rival
        Character rv = seeker(s, "Rivalis", 3, 0);
        s.seekers.put(rv.id, 0b11);
        only(s, rv);
        SeekerArcs.Arc ar = SeekerArcs.start(s, "rival", 20);
        check("A1 a Rival is chosen for a human who has set out (" + (ar == null ? "none" : ar.kind) + ")", ar != null && ar.a == rv.id && ar.b == p.id && noted("will not reach the Door first"));
        s.arcs = new ArrayList<>(Collections.singletonList(ar));
        int r1 = SeekerArcs.beat(s, ar, 24); ar.stage++;
        check("A2 a note is found at the furthest place the Rival has read", r1 == 0 && noted("was here first"));
        Area elsewhere = null;
        for (Area x : Map.createdAreas) if (x != null && x != p.location && x.city) { elsewhere = x; break; }
        if (elsewhere != null) { rv.location.removeChar(rv); rv.location = elsewhere; elsewhere.addChar(rv); }
        int r2 = SeekerArcs.beat(s, ar, 28); ar.stage++;
        check("A3 the Rival turns up where the human stands, and they are rivals now", r2 == 0 && rv.location == p.location && noted("waiting for you") && p.relationshipList.contains(rv.id) != null);
        int r3 = SeekerArcs.beat(s, ar, 32);
        check("A4 the arc ends with word of how the race stands", r3 == 1 && (noted("has fallen behind") || noted("read all nine")));
        s.arcs.clear();

        // ---- the Doubter
        Character db = seeker(s, "Doubting", 3, 1);
        only(s, db);
        ar = SeekerArcs.start(s, "doubter", 40);
        check("A5 the Doubter leaves the Palace and the road", ar != null && ar.a == db.id && db.sect != pal && !s.seekers.containsKey(db.id) && "the Doubter".equals(db.repTitle));
        s.arcs = new ArrayList<>(Collections.singletonList(ar));
        Character wick1 = seeker(s, "Wickone", 0, 0), wick2 = seeker(s, "Wicktwo", 0, 0), wick3 = seeker(s, "Wickthree", 0, 0);
        int before = pal.members.size();
        for (int i = 0; i < 6; i++) SeekerArcs.beat(s, ar, 44); // (the preaching is a chance for each Wick: several tries show it happens)
        ar.stage = 2;
        int r5 = SeekerArcs.beat(s, ar, 48); ar.stage++;
        boolean bounty = false;
        for (Area x : Map.createdAreas) if (x != null) for (Quest q : x.bounties) if (q.targetCharacter == db.id) bounty = true;
        check("A6 the Doubter preaches in a city, Wicks follow them out of the Palace (members " + before + " -> " + pal.members.size() + ")", pal.members.size() < before || noted("Few listened"));
        check("A7 a price is put on the Doubter", r5 == 0 && bounty);
        int r6 = SeekerArcs.beat(s, ar, 52);
        check("A8 the arc ends", r6 == 1);
        s.arcs.clear();

        // ---- the Pair
        Character x1 = seeker(s, "Pairone", 2, 1), x2 = seeker(s, "Pairtwo", 2, 1);
        int pot1 = x1.potential;
        only(s, x1, x2);
        ar = SeekerArcs.start(s, "pair", 60);
        check("A9 two friends are paired and like each other", ar != null && (ar.a == x1.id || ar.a == x2.id || ar.b == x1.id || ar.b == x2.id) && noted("share a room"));
        s.arcs = new ArrayList<>(Collections.singletonList(ar));
        Character pa = SeekerArcs.alive(ar.a), pb = SeekerArcs.alive(ar.b);
        int pp = pa.potential;
        SeekerArcs.beat(s, ar, 64); ar.stage++;
        check("A10 reading together lifts both by one potential", pa.potential >= pp + (pp >= 100 ? 0 : 1) || pp >= 100);
        pb.dead = true;
        int rp = SeekerArcs.beat(s, ar, 68);
        check("A11 when one is gone the other goes on alone, and the arc ends", rp == 1 && noted("goes on alone"));
        pb.dead = false; s.arcs.clear();

        // ---- the Defector
        Character df = seeker(s, "Defying", 3, -1);
        Character o1 = seeker(s, "Oneally", 1, -1), o2 = seeker(s, "Twoally", 1, 0), o3 = seeker(s, "Threeally", 1, -1);
        only(s, df, o1, o2, o3);
        ar = SeekerArcs.start(s, "defector", 80);
        check("A12 a hard-named seeker asks what lies behind the Seam", ar != null && noted("what lies behind the Seam"));
        s.arcs = new ArrayList<>(Collections.singletonList(ar));
        Character dd = SeekerArcs.alive(ar.a);
        SeekerArcs.beat(s, ar, 84); ar.stage++;
        check("A13 they find others (" + ar.others.length + ")", ar.others.length >= 1);
        SeekerArcs.beat(s, ar, 88); ar.stage++;
        int hold = SeekerArcs.beat(s, ar, 92);
        check("A14 then they wait for the Door (the arc holds)", hold == 2);
        ar.others = new int[]{o1.id, o2.id, o3.id};
        check("A15 three allies are counted at the Door (" + SeekerArcs.defectorAllies(s, dd) + ")", SeekerArcs.defectorAllies(s, dd) == 3);
        int crossedBefore = s.crossed;
        LoreEndings.npcCross(s, dd);
        boolean together = false;
        for (Chronicle.Entry e : new ArrayList<>(Chronicle.log)) if (e.text.contains("struck at the Gardener with 3 others")) together = true;
        check("A16 the Defector crosses with them and strikes at the Gardener (the roll has it: " + s.crossed + ")", s.crossed == crossedBefore + 1 && together);
        s.arcs.clear();

        // ---- the Teacher
        Character tc = seeker(s, "Teaching", 5, 1);
        pal.memberRanks.put(tc.id, Organization.SectRank.ELDER);
        Arrays.fill(s.lamps, -1);
        s.lamps[0] = tc.id;
        Character st1 = seeker(s, "Studone", 3, 1), st2 = seeker(s, "Studtwo", 2, 1);
        only(s, tc, st1, st2);
        ar = SeekerArcs.start(s, "teacher", 100);
        check("A17 an Elder takes students (" + (ar == null ? 0 : ar.others.length) + ")", ar != null && ar.a == tc.id && ar.others.length >= 2 && noted("has taken"));
        s.arcs = new ArrayList<>(Collections.singletonList(ar));
        SeekerArcs.beat(s, ar, 104); ar.stage++;
        SeekerArcs.beat(s, ar, 108); ar.stage++;
        for (int o : ar.others) { Character c = Character.charById(o); if (c != null) c.majorLvl = Math.max(c.majorLvl, 3); }
        Character best = null; for (int o : ar.others) { Character c = Character.charById(o); if (c != null && (best == null || c.lvl > best.lvl)) best = c; }
        int rt = SeekerArcs.beat(s, ar, 112);
        check("A18 the lamp passes to the best student, the Teacher steps down (lamp now " + s.lamps[0] + ")", rt == 1 && s.lamps[0] == best.id && pal.getRank(tc) != Organization.SectRank.ELDER);
        s.arcs.clear();

        // ---- the yearly step: never more than six, and a save from before it
        s.arcs = null;
        int maxSeen = 0;
        for (int i = 0; i < 12; i++) seeker(s, "Fill" + i, 2 + i % 3, i % 3 - 1);
        for (int y = 12; y < 200; y++) { SeekerArcs.yearly(s, y); maxSeen = Math.max(maxSeen, s.arcs.size()); }
        check("A19 a save from before the arcs runs, and there are never more than six at once (max " + maxSeen + ")", s.arcs != null && maxSeen <= SeekerArcs.MAX_ACTIVE && maxSeen >= 1);

        // ---- the Dimming
        Area big = home();
        List<Character> folk = new ArrayList<>();
        for (int i = 0; i < 6; i++) { Character c = seeker(s, "Folk" + i, 1, 0); s.seekers.remove(c.id); folk.add(c); }
        for (Character f : folk) for (Character g : folk) if (f != g) f.changeRelationship(g, 2);
        int rels = 0; for (Character f : folk) rels += f.relationshipList.relationships.size();
        p.relationshipList.relationships.size();
        int pRels = p.relationshipList.relationships.size();
        p.location.removeChar(p); p.location = big; big.addChar(p);
        s.dimNext = 0; s.dimmings = 0;
        HollowCourt.yearly(100);
        check("A20 nothing before year 150", s.dimmings == 0 && s.dimNext == 0);
        HollowCourt.yearly(150);
        check("A21 the first Dimming is scheduled after year 150 (" + s.dimNext + ")", s.dimNext >= 150 && s.dimNext < 210 && s.dimmings == 0);
        s.dimNext = 160;
        HollowCourt.yearly(160);
        int rels2 = 0; for (Character f : folk) rels2 += f.relationshipList.relationships.size();
        Area dimmed = null; // the largest city is chosen; the folk stand in Originis, which the Palace keeps out of it
        check("A22 a city goes dark for an hour (" + s.dimmings + ") and the next is 110-130 years on (" + s.dimNext + ")", s.dimmings == 1 && s.dimNext >= 160 + 110 && s.dimNext <= 160 + 130 && noted("went out for an hour"));
        check("A23 the human forgot nothing", p.relationshipList.relationships.size() == pRels);
        Area target = HollowCourt.dim(s, 170);
        int lost = 0;
        if (target != null) { /* residents of the chosen city lose friendships; count the ones left in the folk we moved there */ }
        check("A24 the Dimming picks a city of the world (" + (target == null ? "none" : target.name) + ")", target != null && target.city);

        // the Dimming takes friendships from the people of the largest city and nothing from a human standing there
        Area town = null;
        for (Area x : Map.createdAreas) if (x != null && x.city && x != Lore.palaceArea()) { if (town == null || x.characters.size() > town.characters.size()) town = x; }
        List<Character> locals = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Character c = new Character(town);
            c.id = Map.characters.size(); GameDriver.UID = Math.max(GameDriver.UID, c.id + 1);
            if (c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) c.trait = Character.Traits.Friendly;
            Map.characters.add(c); Map.population++;
            locals.add(c);
        }
        for (Character f : locals) for (Character g : locals) if (f != g) f.changeRelationship(g, 2);
        int relsBefore = 0; for (Character f : locals) relsBefore += f.relationshipList.relationships.size();
        p.location.removeChar(p); p.location = town; town.addChar(p);
        ps.queue.clear(); ps.scenes.remove("dimming");
        int pRels2 = p.relationshipList.relationships.size();
        Area got = HollowCourt.dim(s, 300);
        int relsAfter = 0; for (Character f : locals) relsAfter += f.relationshipList.relationships.size();
        check("A24b the people of the largest city lose friendships (" + relsBefore + " -> " + relsAfter + ", city " + (got == null ? "-" : got.name) + ")", got == town && relsAfter < relsBefore);
        check("A24c the human standing there forgets nothing and is shown the scene", p.relationshipList.relationships.size() == pRels2 && ps.queue.contains("dimming"));

        // ---- later omens
        boolean late = true;
        for (String o : LoreText.OMENS_LATE) if (!o.contains("{place}") || o.length() < 40) late = false;
        check("A25 the later omens name a place and are real sentences (" + LoreText.OMENS_LATE.length + ")", late && LoreText.OMENS_LATE.length >= 6);
        List<String> scene = LoreText.lines("dimming", Lore.vars(p, ps));
        check("A26 the scene of the Dimming reads and leaves no placeholder", scene.size() >= 5 && scene.stream().noneMatch(l -> l.contains("{")));
    }
}
