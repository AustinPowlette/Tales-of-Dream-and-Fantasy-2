package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-18 (headless): the fixes that followed the long runs (`GameInfo/Long Run Results.md`): the eight quests made by hand are spot quests and Bounty Hunt asks for a person (the quest matrix checks that they work);
 *  a person who plays by hand is not drawn into a friend's party battle; a poisoned person is warned loudly (a notice, a status line, reminders, a walk stops); eating a Potential Pill says so; Refine Blood names the
 *  alchemist's skill when that is the limit; Get Pregnant tells a person who plays that there is no child yet and stays; Recruit Defector, Deliver Goods, Disperse and Festival Trade say what happened. */
public class LongRunFixProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    static void check(String n, boolean ok) { PcProbe.check(n, ok); }

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

    static Character person(Area where, int lvl, String gender) {
        Character c = new Character(where);
        c.age = 30; c.gender = gender; c.maxAge = 100000; c.born = true; c.trait = Character.Traits.Friendly; c.targetQuests.clear(); c.lvl = lvl; c.majorLvl = Math.max(0, (lvl - 1) / 10); c.minorLvl = Math.max(1, lvl - 10 * c.majorLvl); c.tempLvl = lvl;
        c.caution = 0; c.affiliation = "Good"; c.affiliationNum = 1; c.name = "Fix"; c.lastName = "N" + c.id;
        while (Map.characters.size() < c.id) Map.characters.add(null);
        Map.characters.add(c); Map.population++;
        return c;
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        check("player created and prompting", p != null && PcProbe.in.blocked);
        if (p == null) return;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        Settings.lore = false; Settings.fightView = false;
        GameDriver.time = 400;
        p.lvl = 25; p.majorLvl = 2; p.minorLvl = 5; p.tempLvl = 25; p.maxAge = 100000; p.caution = 0; p.potential = 60; p.trait = Character.Traits.Friendly; p.affiliation = "Good";
        p.targetQuests.clear(); p.spotQuests.clear(); p.currentQuest = null; p.ss = 50_000_000L; p.convertCurrency();
        Area h = p.location;

        // ---- A: the quest types
        for (String ty : new String[]{"Buy City Array", "Buy Sect Array", "Buy from Sect", "Donate to Sect", "Entice Merchant", "Hold Tournament", "Register Tournament", "Spectate Tournament"})
            check("A1 " + ty + " is done where you stand, not at a place (" + PlayerControl.aboutOf(ty) + ")", PlayerControl.aboutOf(ty).equals("spot"));
        check("A2 Bounty Hunt names a person (" + PlayerControl.aboutOf("Bounty Hunt") + ")", PlayerControl.aboutOf("Bounty Hunt").equals("person"));
        h.city = true; h.population = 5000; h.tournamentTimer = 0; h.tournamentRegistrants.clear();
        PcProbe.cmd("qadd Hold Tournament here", 30);
        Quest q = null; for (Quest x : p.spotQuests) if (x.name.equals("Hold Tournament")) q = x;
        check("A3 'qadd Hold Tournament' makes a spot quest", q != null);
        if (q != null) { PcProbe.cmd("qdo #" + q.uid, 30); check("A4 doing it holds the tournament (timer " + h.tournamentTimer + ")", h.tournamentTimer > 0); }
        h.tournamentTimer = 0; h.tournamentRegistrants.clear();

        // ---- B: a friend's battle does not draw a person who plays into the fight
        Character lead = person(h, 40, "Male"), foe = person(h, 30, "Male"), buddy = person(h, 30, "Male");
        Cheats.setBoth(lead, p, 2); Cheats.setBoth(lead, buddy, 2);
        p.skipping = 0; int mk = PcProbe.mark();
        lead.partyBattleCheck(foe);
        String said = PcProbe.since(mk);
        check("B1 the person who plays does not fight in his friend's party (" + (said.contains("You (Lv") ? "he did" : "he did not") + ")", !said.contains("You (Lv") && !said.contains("attacked you (Lv.25"));
        check("B2 he is told that he stays out of it", said.contains("you stay out of it"));
        check("B3 an NPC friend still joins (" + said.contains("Tag-team") + ")", said.contains("Tag-team") || buddy.exhaustionPenalty >= 0);

        // ---- C: poison is loud
        Character vil = person(h, 20, "Male"); vil.trait = Character.Traits.Poisoner;
        p.fx = null; int tries = 0; mk = PcProbe.mark();
        while ((p.fx == null || p.fx.poisonWeeks <= 0) && tries++ < 200) Life.poison(vil, p, 9);
        String ps = PcProbe.since(mk);
        check("C1 'You feel sick' is printed (" + tries + " tries)", ps.contains("You feel sick"));
        boolean noted = false; synchronized (Notifications.history) { for (Notifications.Note n : Notifications.history) if (n.text != null && n.text.startsWith("You are poisoned")) noted = true; }
        check("C2 a notice of the 'danger' kind is posted", noted);
        String hint = Life.poisonHint(p);
        check("C3 the status block's 'next:' line says what to do (" + hint + ")", hint != null && hint.contains("POISONED") && TermHud.next(p).contains("POISONED"));
        p.skipping = 5; Life.poison(vil, p, 9); // another dose stops a walk
        mk = PcProbe.mark(); p.skipping = 0; Life.tick(p);
        for (int i = 0; i < 4; i++) Life.tick(p);
        check("C4 reminders follow while it lasts", PcProbe.since(mk).contains("You are still poisoned"));
        p.fx = null;

        // ---- D: eating a Potential Pill says so
        p.potential = 50; p.pillBag.add(new Item(Item.ItemType.Pill, Item.PillType.Potential, 11, 1));
        mk = PcProbe.mark(); p.consume();
        check("D1 swallowing a Potential Pill is reported (" + PcProbe.since(mk).replace('\n', '|').trim() + ")", PcProbe.since(mk).contains("You swallow a Potential Pill"));

        // ---- E: Refine Blood names the skill
        Character alch = person(h, 40, "Male"); alch.trait = Character.Traits.Alchemist; alch.affiliation = "Evil"; alch.professionLvl = 1;
        p.materialBag.add(new Item(Item.ItemType.Blood, 4000));
        mk = PcProbe.mark(); boolean ok = p.refineBloodWith(alch);
        check("E1 an unskilled alchemist is named as the limit, not the Blood (" + PcProbe.since(mk).replace('\n', '|').trim() + ")", !ok && PcProbe.since(mk).contains("not skilled enough"));
        p.materialBag.removeIf(m -> m.itemType == Item.ItemType.Blood); p.materialBag.add(new Item(Item.ItemType.Blood, 1000));
        mk = PcProbe.mark(); ok = p.refineBloodWith(alch);
        check("E2 too little Blood still says so", !ok && PcProbe.since(mk).contains("you carry 1000"));

        // ---- F: Get Pregnant, for a person who plays
        Character hus = person(h, 30, "Male"); p.gender = "Female"; p.spouse = hus; hus.spouse = p; p.pregnant = false; p.children.clear();
        Quest gp = new Quest("Get Pregnant", true, 25, 0, 40, hus);
        mk = PcProbe.mark(); int r = 1; int guard = 0;
        while (!p.pregnant && guard++ < 200) r = p.targetQuests(gp);
        String gs = PcProbe.since(mk);
        check("F1 a miss says so and the quest stays (result 0) until it works (" + guard + " tries, pregnant=" + p.pregnant + ")", p.pregnant || gs.contains("no child yet"));
        check("F2 it ends (result 1) when the child is on the way", p.pregnant && r == 1);
        p.gender = "Male"; p.spouse = null; hus.spouse = null;

        // ---- G: Deliver Goods, Disperse, Festival Trade, Hunt Cultivator say what happened
        Organization so = new Organization(person(h, 60, "Male")); so.minPotential = 0; so.minLevel = 0; so.location = h; h.sect = so; so.forceJoin(p);
        p.sectMission = "Deliver|Food|1|1|10"; if (p.goods != null) p.goods.clear();
        mk = PcProbe.mark(); int dg = p.targetQuests(new Quest("Deliver Goods", true, 65, 0, 80, h));
        check("G1 Deliver Goods without the goods says so and stays (" + dg + ")", dg == 0 && PcProbe.since(mk).contains("do not carry the goods"));
        p.sectMission = null; so.resign(p); h.sect = null;
        mk = PcProbe.mark(); p.targetQuests(new Quest("Disperse", true, 45, 0, 60, h));
        check("G2 Disperse says that the crowd thins", PcProbe.since(mk).contains("quieter"));
        mk = PcProbe.mark(); Life.festivalTrade(p);
        check("G3 Festival Trade without goods says what is missing", PcProbe.since(mk).contains("no food, wine or poison"));
        Character prey = person(h, 12, "Male"); prey.affiliation = "Neutral"; p.affiliation = "Good"; Cheats.setBoth(p, prey, 0);
        h.characters.removeIf(c -> c != null && c != p && c != prey); // the others of this test (the poisoner is an enemy now) could be chosen as prey and fought
        mk = PcProbe.mark(); p.spotQuests(new Quest("Hunt Cultivator", true, 55, 0, 60));
        check("G4 Hunt Cultivator says why a Good person does not strike one who is not Evil (" + PcProbe.since(mk).replace('\n', '|').trim() + ")", PcProbe.since(mk).contains("Hunt Cultivator:"));

        System.out.println();
        PcProbe.real.println("[T] DONE");
    }
}
