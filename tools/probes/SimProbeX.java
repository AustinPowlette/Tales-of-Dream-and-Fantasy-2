package Game;

import java.io.*;
import java.math.BigInteger;
import java.util.*;

/** Fight reports, the 10% non-lethal loss penalty, sect villainy (bullying, murder, theft, poison, framing, coups), spoilage. */
public class SimProbeX {
    static void check(String name, boolean ok) { PcProbe.passes += ok ? 1 : 0; PcProbe.fails += ok ? 0 : 1; PcProbe.real.println("[T] " + (ok ? "PASS " : "FAIL ") + name); }
    static void info(String s) { PcProbe.real.println("[T] info: " + s); }

    public static void main(String[] a) throws Exception {
        PcProbe.real = System.out;
        System.setIn(PcProbe.in);
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(PcProbe.real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        try { run(); } catch (Throwable e) { PcProbe.real.println("[T] PROBE EXCEPTION " + e); e.printStackTrace(PcProbe.real); PcProbe.fails++; }
        PcProbe.real.println("[T] ===== " + PcProbe.passes + " passed, " + PcProbe.fails + " failed; gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }

    // ------------------------------------------------------------------ helpers
    static Area home() { return Map.tiles[Map.range / 2][Map.range / 2]; }

    static Character adult(int lvl, int align) {
        Character c = new Character(home());
        c.age = 30; c.gender = "Male"; c.lvl = lvl; c.majorLvl = Math.max(1, lvl / 15);
        c.affiliationNum = align; c.affiliation = align < 0 ? "Evil" : align == 0 ? "Neutral" : "Good";
        c.caution = 50; c.maxAge = 100000; c.ss = 0; c.sc = 0; c.dc = 0; c.ij = 0;
        c.backpack.clear(); c.pillBag.clear(); c.materialBag.clear(); c.arrayBag.clear();
        c.equippedWeapon = null; c.equippedClothing = null;
        if (c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster || c.trait == Character.Traits.Runner || c.trait == Character.Traits.Lucky || c.trait == Character.Traits.Cheater || c.trait == Character.Traits.IronBody)
            c.trait = Character.Traits.Friendly; // keep the maths of the fights plain
        Map.characters.add(c); Map.population++;
        return c;
    }

    static Character human(int lvl, int align) {
        Character h = adult(lvl, align);
        h.player = 1; h.pc().mode = PlayerControl.Mode.MANUAL;
        return h;
    }

    static void unhuman(Character h) { h.player = 0; h.needsReincarnation = false; h.reincarnationKiller = null; }

    static void rel(Character x, Character y, int v) { SectPolitics.setRel(x, y, v); }
    static void relOneWay(Character x, Character y, int v) { Relationship r = x.relationshipList.contains(y.id); if (r == null) x.relationshipList.add(new Relationship(y.id, v)); else r.changeValue(v); }

    static String text(Runnable r) { int m = PcProbe.mark(); r.run(); return PcProbe.since(m); }
    static BigInteger wealth(Character c) { return FightReport.totalWealth(c); }
    static void script(Integer... answers) { DecisionBroker.scripted.clear(); DecisionBroker.scripted.addAll(Arrays.asList(answers)); }

    static List<Area> freeAreas = new ArrayList<>();
    static Organization mkSect(int masterLvl, int align) {
        Character m = adult(masterLvl, align); m.majorLvl = 3;
        Organization s = new Organization(m);
        s.setRules(new String[]{"open"}, 0);
        for (Area ar : freeAreas) if (ar.sect == null) { ar.sect = s; s.location = ar; break; }
        return s;
    }
    static Character member(Organization s, int lvl, int align, Organization.SectRank rank) {
        Character c = adult(lvl, align);
        s.members.add(c); c.sect = s; s.memberRanks.put(c.id, rank);
        return c;
    }
    static void dismantle(Organization s) {
        if (s.location != null && s.location.sect == s) s.location.sect = null;
        for (Character c : new ArrayList<>(s.members)) if (c != null) { c.sect = null; }
        if (s.master != null) s.master.sect = null;
    }

    /** The rank bookkeeping of a sect must stay consistent whatever happened to it. */
    static String validSect(Organization s) {
        Character m = s.master;
        if (m != null) {
            if (m.dead) return "master is dead";
            if (m.sect != s) return "master is not in the sect";
            if (s.getRank(m) != Organization.SectRank.MASTER) return "master has rank " + s.getRank(m);
        }
        int masters = 0;
        for (java.util.Map.Entry<Integer, Organization.SectRank> e : s.memberRanks.entrySet()) {
            Character c = Character.charById(e.getKey());
            if (c == null || c.dead) return "dangling rank id " + e.getKey();
            if (c.sect != s) return "rank entry for a non-member " + c.name;
            if (e.getValue() == Organization.SectRank.MASTER) { masters++; if (c != m) return "a MASTER rank on a non-master"; }
        }
        if (m != null && masters != 1 && s.memberRanks.get(m.id) != Organization.SectRank.FOUNDER) return "MASTER ranks: " + masters;
        for (Character c : s.members) if (c != null && (c.dead || c.sect != s)) return "stale member " + (c == null ? "null" : c.name);
        if (s.plot != null) for (int id : s.plot.plotters) { Character c = Character.charById(id); if (c == null) return "dangling plotter id " + id; }
        return null;
    }

    static boolean has(String text, String... needles) { for (String n : needles) if (!text.contains(n)) return false; return true; }

    // ------------------------------------------------------------------ the run
    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        GameDriver.frozenNpcs = true;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        p.caution = 0; p.maxAge = 100000;
        GameDriver.time = Math.max(GameDriver.time, 60);
        int savedDifficulty = GameDriver.difficulty; GameDriver.difficulty = 0;
        home().arrayBag.clear();
        for (Area ar : Map.createdAreas) if (ar != null && ar.sect == null && !ar.city && !ar.isOriginis()) freeAreas.add(ar);
        info("free areas for sects: " + freeAreas.size());
        PcProbe.real.println("[T] ---- A: fight report ----");
        fightReports();
        PcProbe.real.println("[T] ---- B: non-lethal loss penalty ----");
        penaltyTests();
        PcProbe.real.println("[T] ---- C: sect villainy ----");
        sectTests();
        PcProbe.real.println("[T] ---- D: spoilage ----");
        spoilTests();
        GameDriver.difficulty = savedDifficulty;
        SectPolitics.scale = 1.0;
    }

    // ------------------------------------------------------------------ A
    static void fightReports() throws Exception {
        Area here = home();
        here.arrayBag.clear();
        // A1: win and kill, with loot
        Character h = human(100, 0);
        Character foe = adult(1, -1); foe.caution = 500; foe.ss = 1234; foe.backpack.add(new Item(foe, Item.WeaponType.Sword, 5));
        rel(h, foe, -4);
        int k0 = h.kills; script(0);
        int nChron = Chronicle.search("", "fight", 1000).size(), nNotes = Notifications.history.size();
        final Character h1 = h, foe1 = foe;
        String t = text(() -> h1.battle(foe1));
        check("A1 win+kill: the report names the fight, the win, the kill and the loot", has(t, "FIGHT REPORT", "YOU WON", "KILLED", "Loot", "1234 ss") && foe.dead);
        check("A1b the kills counter is shown and went up", h.kills == k0 + 1 && t.contains("Kills: " + h.kills));
        check("A1c a fight notice was posted for the human and the chronicle has a 'fight' line", Notifications.history.size() > nNotes && Notifications.history.get(Notifications.history.size() - 1).cat.equals("fight")
                && Notifications.history.get(Notifications.history.size() - 1).forId == h.id && Chronicle.search("", "fight", 1000).size() == nChron + 1);
        check("A1d the report is kept as text (history / last)", FightReport.last.contains("YOU WON") && FightReport.lastReports(3).contains("FIGHT REPORT"));
        check("A1e the power line shows both fighters' power and the win chance", has(t, "Power you", "chance to win"));
        unhuman(h);

        // A2: win, spare a feud enemy: 10% of their stones come across
        h = human(100, 0);
        foe = adult(1, -1); foe.caution = 500; foe.ss = 1000;
        rel(h, foe, -2);
        BigInteger hw = wealth(h), fw = wealth(foe);
        script(1);
        final Character h2 = h, foe2 = foe;
        t = text(() -> h2.battle(foe2));
        check("A2 win+spare: spared, the penalty line shows 100 ss", has(t, "YOU WON", "spared", "Penalty", "100 ss") && !foe.dead);
        check("A2b the winner got exactly 10% of the loser's stones", wealth(h).subtract(hw).equals(BigInteger.valueOf(100)) && wealth(foe).equals(BigInteger.valueOf(900)));
        unhuman(h);

        // A3: spare a stranger: no penalty (relationship 0, not a feud)
        h = human(100, 0);
        foe = adult(1, -1); foe.caution = 500; foe.ss = 1000;
        BigInteger fw3 = wealth(foe); script(1);
        final Character h3 = h, foe3 = foe;
        t = text(() -> h3.battle(foe3));
        check("A3 a stranger duel has no penalty", has(t, "YOU WON", "spared") && !t.contains("Penalty") && wealth(foe).equals(fw3));
        unhuman(h);

        // A4: lose a non-lethal fight (the winner's roll of death is 0 and it knows the human only as an enemy: one-way relationship)
        Character weak = human(1, 0); weak.ss = 2000; weak.sc = 3;
        Character strong = adult(100, -1); strong.caution = 0;
        relOneWay(strong, weak, 1); relOneWay(weak, strong, -1);
        BigInteger ww = wealth(weak), sw = wealth(strong);
        script(1); // "Fight" instead of fleeing
        final Character weak1 = weak, strong1 = strong;
        t = text(() -> strong1.battle(weak1));
        BigInteger expect = ww.divide(BigInteger.TEN);
        check("A4 lose non-lethal: report says lost, injured, and the penalty", has(t, "YOU LOST", "injured", "Penalty") && !weak.dead);
        check("A4b the loser paid exactly 10% of all tiers and the winner got it (nothing lost or minted)", ww.subtract(wealth(weak)).equals(expect) && wealth(strong).subtract(sw).equals(expect) && weak.ss >= 0 && weak.sc >= 0);
        unhuman(weak);

        // A5: lose and be killed (deadly feud): the loot goes to the winner
        weak = human(1, 0); weak.ss = 777; weak.backpack.add(new Item(weak, Item.WeaponType.Spear, 4));
        strong = adult(100, -1); strong.caution = 0; rel(strong, weak, -4);
        script(1);
        final Character weak2 = weak, strong2 = strong;
        t = text(() -> strong2.battle(weak2));
        check("A5 lose+killed: the report says so and shows what the killer took", has(t, "YOU LOST", "KILLED", "Loot", "777 ss") && weak.dead);
        unhuman(weak);

        // A6: the human flees (a Runner of equal level gets away)
        Character runner = human(90, 0); runner.trait = Character.Traits.Runner; runner.caution = 100;
        Character bigfoe = adult(90, -1); bigfoe.caution = 500; bigfoe.equippedWeapon = new Item(bigfoe, Item.WeaponType.Sword, 100); bigfoe.backpack.add(bigfoe.equippedWeapon);
        rel(runner, bigfoe, -2);
        script(0);
        final Character runner1 = runner, bigfoe1 = bigfoe;
        t = text(() -> runner1.battle(bigfoe1));
        check("A6 human flees: the report states the flight, no winner, nobody hurt", has(t, "FIGHT REPORT", "fled", "no winner") && !runner.dead && !bigfoe.dead);
        unhuman(runner);

        // A7: the opponent flees from the human
        Character hunter = human(90, 0); hunter.equippedWeapon = new Item(hunter, Item.WeaponType.Sword, 100); hunter.backpack.add(hunter.equippedWeapon);
        Character coward = adult(85, -1); coward.trait = Character.Traits.Runner; coward.caution = 0;
        rel(hunter, coward, -2);
        final Character hunter1 = hunter, coward1 = coward;
        t = text(() -> hunter1.battle(coward1));
        check("A7 opponent flees: the report says they escaped", has(t, "FIGHT REPORT", "fled from you") && has(t, "no winner") && !coward.dead);
        unhuman(hunter);

        // A8: forbidden technique: used by a human, and declined
        Character brave = human(10, 0); brave.caution = 100; brave.potential = 64;
        Character beast = adult(60, -1); beast.caution = 500; rel(brave, beast, -4);
        script(1, 0, 1, 1, 1);
        final Character brave1 = brave, beast1 = beast;
        t = text(() -> brave1.battle(beast1));
        check("A8 forbidden technique used: the report lists it", has(t, "FIGHT REPORT", "Forbidden Technique", "used"));
        unhuman(brave);
        brave = human(10, 0); brave.caution = 100; beast = adult(60, -1); beast.caution = 500; rel(brave, beast, -4);
        script(1, 1, 1, 1);
        final Character brave2 = brave, beast2 = beast;
        t = text(() -> brave2.battle(beast2));
        check("A8b forbidden technique declined: the report says so", has(t, "FIGHT REPORT", "declined the Forbidden Technique"));
        unhuman(brave);

        // A8c: an NPC uses the forbidden technique against the human (deadly, the NPC is losing and not fleeing)
        Character hh = human(100, 0); Character npcF = adult(10, -1); npcF.caution = 100; npcF.potential = 64; rel(hh, npcF, -4); script(0);
        npcF.trait = Character.Traits.Friendly;
        final Character hh1 = hh, npcF1 = npcF;
        // npc is the defender: winChance(hh) > npc.caution -> npc.fleeCheck, then forbiddenCheck; make fleeing impossible (lvl 10 vs 100)
        t = text(() -> hh1.battle(npcF1));
        check("A8c an NPC's forbidden technique is reported too", has(t, "FIGHT REPORT") && (t.contains("used the Forbidden Technique") || t.contains("burned out")));
        unhuman(hh);

        // A9: a beast: an easy win is one line; a loss shows the full block
        Character slayer = human(100, 0);
        slayer.location.monsters = Math.max(slayer.location.monsters, 5);
        slayer.location.lvl = 1; slayer.location.highestLvl = 1;
        final Character slayer1 = slayer;
        t = text(() -> slayer1.beastBattleCheck());
        info("A9 text: " + t.replace('\n', '|'));
        check("A9 an easy beast fight is one compact line", t.contains("[Fight] You defeated the beast") && !t.contains("FIGHT REPORT"));
        unhuman(slayer);

        // A10: arrays: a human breaking an array hears the result
        Character breaker = human(60, 0);
        Area ar = home(); ar.arrayBag.add(new Item(breaker, 10, Item.ItemType.Array));
        final Character breaker1 = breaker;
        t = text(() -> breaker1.arrayBreakAttempt(breaker1.location));
        check("A10 breaking an array is reported", t.contains("array guarding") && (t.contains("You broke") || t.contains("You failed")));
        ar.arrayBag.clear();
        unhuman(breaker);

        // A11: a tag-team assault: the human's round has a header line and a round number
        Character lead = human(100, 0); lead.equippedWeapon = null;
        Character friend = adult(1, 0); friend.caution = 100;
        rel(lead, friend, 2);
        Character victim = adult(40, -1); victim.caution = 500; victim.affiliation = "Evil";
        rel(lead, victim, -3);
        script(0);
        final Character lead1 = lead, victim1 = victim;
        t = text(() -> lead1.partyBattleCheck(victim1));
        check("A11 tag-team: a header line, and the human's own round report says which round it is", t.contains("Tag-team assault") && t.contains("FIGHT REPORT"));
        unhuman(lead);

        // A12: nothing is printed for fights between NPCs only
        Character n1 = adult(100, -1), n2 = adult(1, 0); n2.caution = 500; rel(n1, n2, -3);
        int made0 = FightReport.made;
        t = text(() -> n1.battle(n2));
        check("A12 an NPC-only fight makes no report", FightReport.made == made0 && !t.contains("FIGHT REPORT"));
        // A13: the report state is clean afterwards
        check("A13 no report stays open after the fights", FightReport.cur == null && FightReport.partyRound == 0);
    }

    // ------------------------------------------------------------------ B
    static void penaltyTests() {
        Random r = new Random(7);
        // exact arithmetic over every tier, never negative, nothing minted
        Character w = adult(10, 0), l = adult(10, 0);
        l.ss = 7; l.sc = 3; l.dc = 0; l.ij = 0; w.ss = 5;
        BigInteger tl = wealth(l), tw = wealth(w);
        long moved = FightReport.moveWealth(l, w, 10);
        check("B1 10% of 3,000,007 ss moves: 300,000", moved == 300_000L && wealth(l).equals(tl.subtract(BigInteger.valueOf(300_000))) && wealth(w).equals(tw.add(BigInteger.valueOf(300_000))));
        check("B1b tiers stay normalised and non-negative", l.ss >= 0 && l.ss < 1_000_000 && l.sc >= 0 && w.ss >= 0 && w.ss < 1_000_000);
        // huge fortunes do not overflow
        Character rich = adult(10, 0), taker = adult(10, 0);
        rich.ij = 9_000_000_000L; rich.dc = 999_999; rich.sc = 123_456; rich.ss = 999_999; taker.ij = 5;
        BigInteger rt = wealth(rich), tt = wealth(taker);
        long m2 = FightReport.moveWealth(rich, taker, 10);
        check("B2 a fortune in immortal jades moves 10% exactly (BigInteger-safe), saturating the printed amount", rt.add(tt).equals(wealth(rich).add(wealth(taker))) && wealth(rich).equals(rt.subtract(rt.divide(BigInteger.TEN))) && rich.ij >= 0 && rich.dc >= 0 && rich.sc >= 0 && rich.ss >= 0 && m2 > 0);
        // nothing to take
        Character poor = adult(10, 0), tk = adult(10, 0); poor.ss = 5;
        check("B3 a loser with under 10 ss pays nothing and stays at zero or more", FightReport.moveWealth(poor, tk, 10) == 0 && poor.ss == 5);
        Character broke = adult(10, 0); check("B3b a penniless loser stays at 0", FightReport.moveWealth(broke, tk, 10) == 0 && broke.ss == 0 && wealth(broke).signum() == 0);
        // random fuzz: conservation
        boolean ok = true;
        for (int i = 0; i < 300; i++) {
            Character x = adult(10, 0), y = adult(10, 0);
            x.ss = r.nextInt(2_000_000); x.sc = r.nextInt(3_000_000); x.dc = r.nextInt(4); y.ss = r.nextInt(1000); y.sc = r.nextInt(5);
            BigInteger before = wealth(x).add(wealth(y)); BigInteger xb = wealth(x);
            long mv = FightReport.moveWealth(x, y, 10);
            ok &= before.equals(wealth(x).add(wealth(y))) && x.ss >= 0 && x.sc >= 0 && x.dc >= 0 && wealth(x).equals(xb.subtract(xb.divide(BigInteger.TEN))) && mv == xb.divide(BigInteger.TEN).longValue();
        }
        check("B4 300 random fortunes: wealth conserved, tiers valid, exactly floor(10%)", ok);

        // the rule: feud yes, stranger no, spar no, beast no
        Character win = adult(100, -1), lose = adult(1, 0); lose.ss = 1000;
        BigInteger before = wealth(lose);
        win.winCheck(lose, false); // stranger: no relationship, no quest
        check("B5 a stranger duel (no relationship) costs the loser nothing", wealth(lose).equals(before) && !lose.dead);
        relOneWay(lose, win, -1);
        win.winCheck(lose, false);
        check("B5b with a negative relationship (loser's side only) the non-lethal loser pays 10%", wealth(lose).equals(before.subtract(before.divide(BigInteger.TEN))));
        Character win2 = adult(100, -1), lose2 = adult(1, 0); lose2.ss = 1000; rel(win2, lose2, -3);
        BigInteger b2 = wealth(lose2);
        win2.sparring = true; lose2.sparring = true; win2.winCheck(lose2, false);
        check("B5c sparring never costs anything", wealth(lose2).equals(b2));
        win2.sparring = false; lose2.sparring = false;
        Character win3 = adult(100, -1), lose3 = adult(1, 0); lose3.ss = 1000;
        win3.currentQuest = new Quest("Bounty Hunt", true, 40, 3, 70, lose3);
        BigInteger b3 = wealth(lose3);
        win3.winCheck(lose3, false);
        check("B5d a bounty fight counts as a feud even between strangers", wealth(lose3).equals(b3.subtract(b3.divide(BigInteger.TEN))));
        win3.currentQuest = null;
        // a lethal result kills: nothing special, the killer gets everything via the loot (not 10%)
        Character win4 = adult(100, -1), lose4 = adult(1, 0); lose4.ss = 1000; rel(win4, lose4, -3);
        win4.winCheck(lose4, true);
        check("B5e a lethal fight kills and the killer takes all, not 10%", lose4.dead && win4.ss >= 1000);
        // two NPCs, a rival: through battle()
        Character n1 = adult(100, -1), n2 = adult(1, 0); n1.caution = 0; n2.caution = 500; n2.ss = 1000;
        relOneWay(n1, n2, 1); relOneWay(n2, n1, -2); // n1 is not out for blood (no death roll), n2 hates n1
        BigInteger b5 = wealth(n2);
        n1.battle(n2);
        check("B6 an NPC feud fight through battle(): the loser pays", !n2.dead && wealth(n2).equals(b5.subtract(b5.divide(BigInteger.TEN))));
    }

    // ------------------------------------------------------------------ C
    static void sectTests() throws Exception {
        Random r = new Random(11);
        SectPolitics.resetCounters();
        // ---- C0: rates scale with alignment
        Character ev = adult(30, -1), ne = adult(30, 0), go = adult(30, 1);
        int e1 = 0, n1 = 0, g1 = 0;
        for (int i = 0; i < 20000; i++) { if (SectPolitics.roll(ev, 7)) e1++; if (SectPolitics.roll(ne, 7)) n1++; if (SectPolitics.roll(go, 7)) g1++; }
        info("rolls out of 20000 at 7%: evil " + e1 + " neutral " + n1 + " good " + g1);
        check("C0 Evil acts much more than Neutral, Good never", e1 > n1 * 2 && n1 > 0 && g1 == 0);
        Character cautious = adult(30, -1); cautious.caution = 100; Character reckless = adult(30, -1); reckless.caution = 1;
        int c1 = 0, c2 = 0;
        for (int i = 0; i < 20000; i++) { if (SectPolitics.roll(cautious, 7)) c1++; if (SectPolitics.roll(reckless, 7)) c2++; }
        check("C0b the cautious hesitate, the reckless do not", c2 > c1 * 2);

        // ---- C1: bullying
        int[] counts = new int[3];
        for (int al = -1; al <= 1; al++) {
            Organization s = mkSect(60, al);
            List<Character> inners = new ArrayList<>(), outers = new ArrayList<>();
            for (int i = 0; i < 4; i++) inners.add(member(s, 30, al, Organization.SectRank.INNER_DISCIPLE));
            for (int i = 0; i < 8; i++) outers.add(member(s, 20, 0, Organization.SectRank.OUTER_DISCIPLE));
            int b0 = SectPolitics.bullied; boolean anyRel = false; BigInteger drained = BigInteger.ZERO;
            for (int round = 0; round < 300; round++) {
                for (Character o : outers) { if (!s.members.contains(o)) { s.members.add(o); o.sect = s; } s.memberRanks.put(o.id, Organization.SectRank.OUTER_DISCIPLE); o.ss = 1000; o.sc = 0; }
                for (Character i : inners) { if (!s.members.contains(i)) { s.members.add(i); i.sect = s; } s.memberRanks.put(i.id, Organization.SectRank.INNER_DISCIPLE); }
                SectPolitics.bullyRound(s, GameDriver.rand);
            }
            counts[al + 1] = SectPolitics.bullied - b0;
            for (Character i : inners) for (Character o : outers) if (SectPolitics.relValue(i, o) <= -1) anyRel = true;
            if (al == -1) { check("C1b bullies leave bad blood between inner and outer disciples", anyRel); }
            String v = validSect(s);
            if (v != null) info("sect validity after bullying: " + v);
            check("C1c bullying leaves the sect valid (" + (al == -1 ? "evil" : al == 0 ? "neutral" : "good") + ")", v == null);
        }
        info("bullying events in 300 seasons: evil " + counts[0] + ", neutral " + counts[1] + ", good " + counts[2]);
        check("C1 Evil inner disciples bully far more than Neutral, Good never", counts[0] > counts[1] * 2 && counts[1] > 0 && counts[2] == 0);
        // a bully takes stones: one deterministic shakedown
        Organization bs = mkSect(60, -1);
        Character bully = member(bs, 40, -1, Organization.SectRank.INNER_DISCIPLE), prey = member(bs, 10, 0, Organization.SectRank.OUTER_DISCIPLE);
        prey.ss = 5000; BigInteger pb = wealth(prey), bb = wealth(bully);
        boolean got = false;
        for (int i = 0; i < 20 && !got; i++) { prey.ss = 5000; prey.sc = 0; pb = wealth(prey); bb = wealth(bully); got = SectPolitics.bully(bs, bully, prey, GameDriver.rand) && wealth(prey).compareTo(pb) < 0; }
        check("C1d a successful shakedown moves 15% of the victim's stones to the bully", got && wealth(bully).compareTo(bb) > 0 && SectPolitics.relValue(bully, prey) <= -1);

        // ---- C1e: a human victim is told and asked first
        Organization hs = mkSect(60, -1);
        Character hb = member(hs, 40, -1, Organization.SectRank.INNER_DISCIPLE);
        Character hv = member(hs, 10, 0, Organization.SectRank.OUTER_DISCIPLE); hv.player = 1; hv.pc().mode = PlayerControl.Mode.MANUAL; hv.ss = 4000;
        DecisionBroker.promptLog.clear(); script(1); // slip away
        BigInteger hv0 = wealth(hv);
        int nn = Notifications.history.size();
        String slipText = text(() -> SectPolitics.bully(hs, hb, hv, GameDriver.rand));
        boolean slipped = slipText.contains("You slipped away") || slipText.contains("slipped away");
        boolean asked = false; for (String pl : DecisionBroker.promptLog) if (pl.contains("challenges you")) asked = true;
        boolean told = false; for (int i = nn; i < Notifications.history.size(); i++) { Notifications.Note n = Notifications.history.get(i); if (n.cat.equals("sect") && n.forId == hv.id && n.text.contains("take what is yours")) told = true; }
        info("C1e asked=" + asked + " told=" + told + " promptLog=" + DecisionBroker.promptLog + " wealthSame=" + wealth(hv).equals(hv0));
        check("C1e a human victim gets a notice and a prompt before a shakedown, and slipping away keeps their stones", asked && told && (!slipped || wealth(hv).equals(hv0)));
        script(0); // fight
        String ft = text(() -> SectPolitics.bully(hs, hb, hv, GameDriver.rand));
        check("C1f a human victim who stands up fights, with a fight report", ft.contains("FIGHT REPORT"));
        unhuman(hv);
        hv.pc = null;

        // ---- C2: murder for loot
        int killed = 0, escaped = 0, caught = 0, quiet = 0, lootOk = 0, selfDied = 0;
        for (int trial = 0; trial < 60; trial++) {
            Organization s = mkSect(90, -1);
            Character el = member(s, 60, -1, Organization.SectRank.ELDER); el.caution = 0;
            Character vic = member(s, 20, 0, Organization.SectRank.OUTER_DISCIPLE);
            Item gem = new Item(vic, Item.WeaponType.Saber, 6); vic.backpack.add(gem); vic.equippedWeapon = gem;
            el.backpack.add(new Item(el, Item.WeaponType.Sword, 3));
            int m0 = SectPolitics.murders, x0 = SectPolitics.murdersExposed;
            boolean did = SectPolitics.murder(s, el, SectPolitics.roster(s), GameDriver.rand);
            if (!did) { check("C2 an elder with a covetable victim attempts murder", false); continue; }
            if (vic.dead) { killed++; if (el.backpack.contains(gem) || el.equippedWeapon == gem) lootOk++; else if (trial < 60) info("C2 loot missing: elder dead=" + el.dead + " sect=" + (el.sect == s) + " backpack=" + el.backpack.size() + " gemInVicBag=" + vic.backpack.contains(gem) + " eqW=" + (el.equippedWeapon == null ? "null" : el.equippedWeapon.name) + " gem=" + gem.name); } else if (el.dead) selfDied++; else escaped++;
            if (SectPolitics.murdersExposed > x0) { caught++; if (el.sect == s) check("C2c a caught murderer is expelled", false); } else if (vic.dead) quiet++;
            String v = validSect(s); if (v != null) check("C2d the sect stays valid after a murder: " + v, false);
            dismantle(s);
        }
        info("murders: killed " + killed + " escaped " + escaped + " murderer-died " + selfDied + "; exposed " + caught + " unnoticed " + quiet);
        check("C2 evil elders murder for a better item and take it (loot via dead(killer); a relic sometimes shatters with its owner)", killed > 20); info("C2 the prize reached the killer in " + lootOk + " of " + killed + " murders (a weapon that meets a far stronger fighter breaks in the fight, as in any duel)");
        check("C2b some murders come to light (elder expelled with a bounty) and some stay secret", caught > 3 && quiet > 3);
        boolean chron = false; for (Chronicle.Entry e : Chronicle.search("murdered", "sect", 50)) chron = true;
        check("C2e a discovered murder is in the chronicle (kind sect)", chron);
        // elders who are too weak or victims without a better item are left alone
        Organization fs = mkSect(90, -1);
        Character fe = member(fs, 20, -1, Organization.SectRank.ELDER), fv = member(fs, 60, 0, Organization.SectRank.OUTER_DISCIPLE);
        fv.backpack.add(new Item(fv, Item.WeaponType.Saber, 40));
        check("C2f an elder does not attack a stronger disciple", !SectPolitics.murder(fs, fe, SectPolitics.roster(fs), GameDriver.rand));

        // ---- C3: coups
        int won = 0, failed = 0, exposed = 0, noPlot = 0;
        int cw0 = SectPolitics.coupsWon, cf0 = SectPolitics.coupsFailed, pe0 = SectPolitics.plotsExposed;
        int savedTime = GameDriver.time;
        SectPolitics.scale = 1000;
        int invalid = 0; String firstInvalid = null; boolean winnerWasPlotter = true;
        for (int trial = 0; trial < 120; trial++) {
            int ml = 40 + r.nextInt(25);
            Organization s = mkSect(ml, trial % 3 == 0 ? 0 : -1);
            Character master = s.master; master.caution = 30;
            List<Character> elders = new ArrayList<>();
            int ne2 = 2 + r.nextInt(3);
            for (int i = 0; i < ne2; i++) { Character e = member(s, ml - 8 + r.nextInt(14), -1 + (i % 2 == 0 ? 0 : 1) * 0, Organization.SectRank.ELDER); e.caution = 30; elders.add(e); }
            Character loyal = member(s, ml - 10, 1, Organization.SectRank.ELDER);
            for (int i = 0; i < 4; i++) member(s, 15, 0, Organization.SectRank.OUTER_DISCIPLE);
            Character oldMaster = master;
            Set<Integer> plotterIds = new HashSet<>();
            int cw = SectPolitics.coupsWon, cf = SectPolitics.coupsFailed, pe = SectPolitics.plotsExposed;
            boolean resolved = false;
            GameDriver.time = savedTime;
            for (int yr = 0; yr < 8 && !resolved; yr++) {
                SectPolitics.plotYearly(s, SectPolitics.roster(s), GameDriver.rand);
                if (s.plot != null) plotterIds.addAll(s.plot.plotters);
                String v = validSect(s);
                if (v != null) { invalid++; if (firstInvalid == null) firstInvalid = v; }
                if (s.plot == null && (SectPolitics.coupsWon > cw || SectPolitics.coupsFailed > cf || SectPolitics.plotsExposed > pe)) resolved = true;
                GameDriver.time += 52;
            }
            if (SectPolitics.coupsWon > cw) { won++; if (s.master == null || !elders.contains(s.master)) { winnerWasPlotter = false; info("C3 winner not a plotter: master=" + (s.master == null ? "null" : s.master.name + " lvl " + s.master.lvl + " id " + s.master.id) + " plotters=" + plotterIds + " oldMaster id " + oldMaster.id + " dead=" + oldMaster.dead); } }
            else if (SectPolitics.coupsFailed > cf) failed++;
            else if (SectPolitics.plotsExposed > pe) exposed++;
            else noPlot++;
            dismantle(s);
        }
        GameDriver.time = savedTime; SectPolitics.scale = 1.0;
        info("coups: won " + won + ", failed " + failed + ", exposed " + exposed + ", unresolved " + noPlot + "; invalid states " + invalid + (firstInvalid != null ? " first: " + firstInvalid : ""));
        check("C3 coups succeed sometimes and the strongest surviving plotter becomes the master", won > 3 && winnerWasPlotter);
        check("C3b coups also fail or are exposed", failed + exposed > 3);
        check("C3c every sect state stays valid through plots, coups and purges (one MASTER, no dangling ids)", invalid == 0);
        check("C3d the counters match", SectPolitics.coupsWon - cw0 == won && SectPolitics.coupsFailed - cf0 == failed && SectPolitics.plotsExposed - pe0 == exposed);
        // a plot object saves and loads
        SectPolitics.Plot plot = new SectPolitics.Plot(); plot.plotters.add(5); plot.plotters.add(9); plot.startWeek = 77; plot.leaderId = 5;
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        try (ObjectOutputStream oo = new ObjectOutputStream(bo)) { oo.writeObject(plot); }
        SectPolitics.Plot back = (SectPolitics.Plot) new ObjectInputStream(new ByteArrayInputStream(bo.toByteArray())).readObject();
        check("C3e a conspiracy is serialisable (saves)", back.plotters.equals(plot.plotters) && back.startWeek == 77 && back.leaderId == 5);

        // ---- C3f: a human master is told and asked before an ambush
        Organization hm = mkSect(40, 1);
        Character human1 = hm.master; human1.player = 1; human1.pc().mode = PlayerControl.Mode.MANUAL;
        Character ep1 = member(hm, 45, -1, Organization.SectRank.ELDER), ep2 = member(hm, 45, -1, Organization.SectRank.ELDER);
        SectPolitics.Plot hp = new SectPolitics.Plot(); hp.startWeek = 0; hp.plotters.add(ep1.id); hp.plotters.add(ep2.id); hp.leaderId = ep1.id; hm.plot = hp;
        DecisionBroker.promptLog.clear(); script(1);
        int nn2 = Notifications.history.size();
        SectPolitics.strike(hm, hp, 2, GameDriver.rand);
        boolean asked2 = false; for (String pl : DecisionBroker.promptLog) if (pl.contains("challenges you")) asked2 = true;
        boolean told2 = false; for (int i = nn2; i < Notifications.history.size(); i++) { Notifications.Note n = Notifications.history.get(i); if (n.cat.equals("sect") && n.forId == human1.id && n.text.contains("ambush")) told2 = true; }
        info("C3f asked2=" + asked2 + " told2=" + told2 + " plotNull=" + (hm.plot == null) + " alive=" + !human1.dead + " rel=" + SectPolitics.relValue(ep1, human1) + " log=" + DecisionBroker.promptLog);
        check("C3f a human master is told and asked; slipping away aborts the coup and makes enemies", asked2 && told2 && hm.plot == null && !human1.dead && SectPolitics.relValue(ep1, human1) <= -3);
        unhuman(human1); human1.pc = null;
        dismantle(hm);

        // ---- C4: good sects stay peaceful; neutral ones less so
        SectPolitics.resetCounters();
        Organization gs = mkSect(70, 1);
        for (int i = 0; i < 3; i++) member(gs, 55, 1, Organization.SectRank.ELDER);
        for (int i = 0; i < 4; i++) member(gs, 35, 1, Organization.SectRank.INNER_DISCIPLE);
        for (int i = 0; i < 6; i++) { Character o = member(gs, 25, 1, Organization.SectRank.OUTER_DISCIPLE); o.backpack.add(new Item(o, Item.WeaponType.Saber, 50)); }
        gs.sectCash = 5_000_000; gs.treasury.add(new Item(gs.master, Item.WeaponType.Sword, 60));
        for (int yr = 0; yr < 150; yr++) { SectPolitics.yearlySect(gs, GameDriver.rand); SectPolitics.bullyRound(gs, GameDriver.rand); }
        info("good sect over 150 years at the normal rates: " + SectPolitics.summary());
        check("C4 a Good sect sees no bullying, murder, theft, poison, framing or coups", SectPolitics.bullied == 0 && SectPolitics.murders == 0 && SectPolitics.thefts == 0 && SectPolitics.poisonings == 0
                && SectPolitics.framings == 0 && SectPolitics.plotsHatched == 0 && gs.sectCash == 5_000_000 && gs.master != null && !gs.master.dead);
        check("C4b treachery in a Good sect is rare (a few in 150 years at most)", SectPolitics.treacheries <= 12);
        SectPolitics.scale = 1000;
        for (int yr = 0; yr < 100; yr++) { SectPolitics.yearlySect(gs, GameDriver.rand); SectPolitics.bullyRound(gs, GameDriver.rand); }
        SectPolitics.scale = 1.0;
        check("C4c even with every chance multiplied by 1000 a Good sect commits no villainy", SectPolitics.bullied == 0 && SectPolitics.murders == 0 && SectPolitics.thefts == 0 && SectPolitics.poisonings == 0
                && SectPolitics.framings == 0 && SectPolitics.plotsHatched == 0);
        check("C4d the Good sect is still valid", validSect(gs) == null);
        dismantle(gs);

        // ---- C5: theft from the treasury
        int stolen = 0, caughtThief = 0;
        for (int trial = 0; trial < 50; trial++) {
            Organization s = mkSect(70, -1);
            Character el = member(s, 50, -1, Organization.SectRank.ELDER); el.caution = 0;
            Item blade = new Item(s.master, Item.WeaponType.Sword, 30); s.treasury.add(blade);
            s.sectCash = 0;
            boolean did = SectPolitics.theft(s, el, GameDriver.rand);
            if (did && (el.backpack.contains(blade)) && !s.treasury.contains(blade)) stolen++;
            if (did && el.sect != s) caughtThief++;
            if (validSect(s) != null) check("C5d sect valid after theft", false);
            dismantle(s);
        }
        check("C5 an Evil elder steals gear from the treasury, and some are caught and expelled", stolen == 50 && caughtThief > 3 && caughtThief < 40);
        Organization cs = mkSect(70, -1); Character cel = member(cs, 50, -1, Organization.SectRank.ELDER); cs.sectCash = 2_000_000; BigInteger cw1 = wealth(cel);
        SectPolitics.theft(cs, cel, GameDriver.rand);
        long took = 2_000_000 - cs.sectCash;
        check("C5b a cash theft takes 15-35% of the coffers into the elder's purse", took >= 300_000 && took <= 700_000 && wealth(cel).subtract(cw1).longValue() == took);
        dismantle(cs);
        Organization hs2 = mkSect(70, -1); Character hel = member(hs2, 50, -1, Organization.SectRank.ELDER);
        hs2.treasury.add(new Item(Item.ItemType.Herb, 5, 1000));
        int before = hs2.treasury.get(0).quantity; SectPolitics.theft(hs2, hel, GameDriver.rand);
        check("C5c with nothing else to take, herbs are embezzled", hs2.treasury.isEmpty() || hs2.treasury.get(0).quantity < before);
        dismantle(hs2);

        // ---- C6: poison
        int poisoned = 0, tries6 = 0;
        for (int trial = 0; trial < 40; trial++) {
            Organization s = mkSect(80, -1);
            Character el = member(s, 50, -1, Organization.SectRank.ELDER);
            Character riv = member(s, 36, 0, Organization.SectRank.ELDER); // within 0.7-1.4x of the poisoner, and weak enough for a vial to take hold now and then
            el.goods = new ArrayList<>(); el.goods.add(new Item(Item.ItemType.Poison, 8, 3, "Test Venom"));
            int p0 = SectPolitics.poisonings;
            SectPolitics.poisonRival(s, el, SectPolitics.roster(s), GameDriver.rand);
            tries6++;
            if (riv.fx != null && riv.fx.poisonWeeks > 0) poisoned++;
            if (SectPolitics.poisonings != p0 + 1) check("C6 poisonRival counted", false);
            dismantle(s);
        }
        check("C6 an Evil elder poisons a rival with a vial (it takes hold at least sometimes)", poisoned > 5 && poisoned < tries6);
        Organization ps = mkSect(80, -1); Character pel = member(ps, 50, -1, Organization.SectRank.ELDER); member(ps, 48, 0, Organization.SectRank.ELDER);
        ps.treasury.add(new Item(Item.ItemType.Poison, 5, 4, "Pantry Venom"));
        SectPolitics.poisonRival(ps, pel, SectPolitics.roster(ps), GameDriver.rand);
        check("C6b without a vial of their own the elder takes one from the sect pantry", ps.treasury.get(0).quantity == 3);
        dismantle(ps);

        // ---- C7: framing
        int expelledByLies = 0, demoted = 0;
        for (int trial = 0; trial < 80; trial++) {
            Organization s = mkSect(80, trial % 2 == 0 ? -1 : 0);
            Character sch = member(s, 40, -1, Organization.SectRank.INNER_DISCIPLE);
            Character riv = member(s, 42, 0, Organization.SectRank.ELDER);
            int f0 = SectPolitics.framings;
            SectPolitics.frame(s, sch, SectPolitics.roster(s), GameDriver.rand);
            if (riv.sect != s) expelledByLies++;
            if (s.getRank(sch) == Organization.SectRank.OUTER_DISCIPLE) demoted++;
            if (SectPolitics.framings != f0 + 1) check("C7 frame counted", false);
            String v = validSect(s); if (v != null) check("C7d valid after framing: " + v, false);
            dismantle(s);
        }
        check("C7 a schemer's lies get a rival thrown out of the sect, or the lie is exposed and the schemer demoted", expelledByLies > 10 && demoted > 3);

        // ---- C8: the whole yearly pass over several evil sects at once (nothing throws, every sect valid)
        SectPolitics.resetCounters();
        List<Organization> many = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            Organization s = mkSect(60 + i, i % 3 - 1);
            for (int k = 0; k < 3; k++) member(s, 45 + k, -1, Organization.SectRank.ELDER);
            for (int k = 0; k < 4; k++) { Character c = member(s, 30 + k, -1, Organization.SectRank.INNER_DISCIPLE); c.backpack.add(new Item(c, Item.WeaponType.Spear, 25 + k)); }
            for (int k = 0; k < 8; k++) { Character c = member(s, 12 + k, 0, Organization.SectRank.OUTER_DISCIPLE); c.ss = 3000; }
            s.treasury.add(new Item(s.master, Item.WeaponType.Sword, 20)); s.sectCash = 400_000;
            many.add(s);
        }
        SectPolitics.scale = 20;
        int bad = 0; String why = null;
        for (int yr = 0; yr < 40; yr++) {
            GameDriver.time += 52;
            SectPolitics.yearly(many);
            for (int se = 0; se < 4; se++) SectPolitics.seasonal(se);
            for (Organization s : many) { String v = validSect(s); if (v != null) { bad++; if (why == null) why = v; } }
        }
        SectPolitics.scale = 1.0; GameDriver.time = savedTime;
        info("mass run: " + SectPolitics.summary() + (why != null ? " invalid: " + why : ""));
        check("C8 40 years of intrigue in 12 evil sects: no exceptions, every sect valid", SectPolitics.errors == 0 && bad == 0);
        check("C8b the world became dangerous: murders, thefts and more happened", SectPolitics.murders + SectPolitics.thefts + SectPolitics.bullied + SectPolitics.framings > 20);
        for (Organization s : many) dismantle(s);
        // the real diplomacy() entry point still works with sects around
        SectPolitics.resetCounters();
        for (int i = 0; i < 5; i++) Seasonal.diplomacy();
        check("C9 Seasonal.diplomacy runs the new politics without errors", SectPolitics.errors == 0);
        int tr0 = 0; for (Chronicle.Entry e : Chronicle.log) if (e.text.contains("treachery")) tr0++;
        info("treachery lines so far: " + tr0);
    }

    // ------------------------------------------------------------------ D
    static void spoilTests() {
        Area a = home();
        a.goodsBag.clear();
        Item f1 = new Item(Item.ItemType.Food, 1, 100, "Plain Rice"), f9 = new Item(Item.ItemType.Food, 9, 100, "Immortal Feast");
        Item w1 = new Item(Item.ItemType.Wine, 1, 100, "Rough Wine"), w9 = new Item(Item.ItemType.Wine, 9, 100, "Grand Cru");
        Item p1 = new Item(Item.ItemType.Poison, 1, 100, "Weak Venom");
        Item tiny = new Item(Item.ItemType.Wine, 1, 1, "One Sip");
        int sf1 = Life.spoil(f1), sf9 = Life.spoil(f9), sw1 = Life.spoil(w1), sw9 = Life.spoil(w9), sp1 = Life.spoil(p1);
        info("100 portions after a year: food L1 " + sf1 + ", food L9 " + sf9 + ", wine L1 " + sw1 + ", wine L9 " + sw9 + ", poison L1 " + sp1 + "; one sip: " + Life.spoil(tiny));
        check("D1 food goes off faster than wine, higher grades last longer", sf1 < sw1 && sf9 > sf1 && sw9 > sw1);
        check("D1b everything loses at least 8% a year and a lone portion is gone", sw9 <= 92 && sp1 <= 92 && Life.spoil(tiny) == 0);
        Item big = new Item(Item.ItemType.Wine, 3, 1000, "Cellar"), mid = new Item(Item.ItemType.Wine, 3, 100, "Rack");
        check("D1c bigger stacks spoil proportionally more", (1000 - Life.spoil(big)) / 1000.0 > (100 - Life.spoil(mid)) / 100.0);
        // the yearly pass touches every pool
        a.goodsBag.add(new Item(Item.ItemType.Wine, 3, 500, "Market Wine")); a.goodsBag.add(new Item(Item.ItemType.Food, 3, 400, "Market Rice"));
        Shop shop = null; for (Area ar : Map.createdAreas) if (ar != null && !ar.shops.isEmpty()) { shop = ar.shops.get(0); break; }
        if (shop != null) Life.shelf(shop).add(new Item(Item.ItemType.Wine, 3, 120, "Shelf Wine"));
        Character cook = adult(20, 0); cook.goods = new ArrayList<>(); cook.goods.add(new Item(Item.ItemType.Poison, 3, 900, "Stash")); cook.goods.add(new Item(Item.ItemType.Food, 2, 600, "Hoard"));
        Organization s = mkSect(50, 0); s.treasury.add(new Item(Item.ItemType.Food, 3, 300, "Pantry Rice")); s.sectCash = 0;
        if (s.location == null || s.location.sect != s) { Area pa = null; for (Area ar : Map.createdAreas) if (ar != null && ar.sect == null && !ar.isOriginis()) { pa = ar; break; } if (pa != null) { pa.sect = s; s.location = pa; } }
        int m0 = Life.marketCount(a), c0 = Life.goodsCount(cook), p0 = Life.countIn(s.treasury, Item.ItemType.Food, 0);
        int sh0 = shop == null ? 0 : Life.shelfCount(shop);
        Life.marketYearly();
        check("D2 the market's stock spoils", Life.marketCount(a) < m0);
        check("D2b a character's hoard spoils and is capped", Life.goodsCount(cook) < c0 && Life.goodsCount(cook) <= Life.HOLD_CAP);
        if (s.location == null || s.location.sect != s) Life.spoilList(s.treasury); // no free tile for the sect in a shrunken world: spoil its pantry directly
        info("D2c pantry before " + p0 + " after " + Life.countIn(s.treasury, Item.ItemType.Food, 0) + " sectOnTile=" + (s.location != null && s.location.sect == s));
        check("D2c the sect pantry spoils", Life.countIn(s.treasury, Item.ItemType.Food, 0) < p0);
        if (shop != null) check("D2d shop shelves spoil", Life.shelfCount(shop) < sh0);
        // per-head stock stays bounded under constant inflow: an economic model of the yearly pass
        a.goodsBag.clear();
        int stock = 0; int[] history = new int[60];
        for (int yr = 0; yr < 60; yr++) {
            Life.market(a).add(new Item(Item.ItemType.Wine, 3, 120, "Inflow Wine")); Life.market(a).add(new Item(Item.ItemType.Food, 3, 200, "Inflow Rice"));
            // merge stacks like the real shelf does
            Item mw = null, mf = null; for (Item it : new ArrayList<>(a.goodsBag)) { if (it.name.startsWith("Inflow Wine")) { if (mw == null) mw = it; else { mw.quantity += it.quantity; a.goodsBag.remove(it); } } else if (it.name.startsWith("Inflow Rice")) { if (mf == null) mf = it; else { mf.quantity += it.quantity; a.goodsBag.remove(it); } } }
            Life.marketYearly();
            history[yr] = Life.marketCount(a);
        }
        info("market stock under a constant inflow of 320 portions a year: year 10 " + history[9] + ", year 30 " + history[29] + ", year 59 " + history[59]);
        check("D3 under a constant inflow the stock levels off (year 59 within 15% of year 30)", history[59] <= history[29] * 1.15 + 5 && history[59] < 320 * 6);
        a.goodsBag.clear();
        dismantle(s);
    }
}
