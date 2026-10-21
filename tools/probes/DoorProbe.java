package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-18: the Door at Ascension (`GameInfo/Garden at Ascension Design.md`). NPCs: an ordinary person who first reaches Ascension, Half-Immortal or Pseudo Immortal may go through the Seam on their own
 *  (Settings.leavePct, once per realm), a True Immortal who is no seeker takes a lamp while one is free; seekers, lamp-bearers, people of a story and beasts are never rolled; descendants of a leaver below
 *  True Immortal carry +2/+2 (a lamp-bearer +5). A person who plays: the Door is open from Ascension (Major Lvl 7, level 71), from anywhere, at any time; the Early Door has its own endings (the Rear Guard, the Early Strike,
 *  a walk out alone) and a Stay that can be left later. The ending of the human part is chosen with the first argument: rear|strike|strikefull|alone|stay|back (default rear): one ending per run, because each ends the body. */
public class DoorProbe {
    static void check(String n, boolean ok) { PcProbe.check(n, ok); }

    static String VARIANT = "rear";

    public static void main(String[] a) throws Exception {
        if (a.length > 0) VARIANT = a[0];
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

    static Character npc(Area where, int major) {
        Character c = LongRunFixProbe.person(where, major * 10 + 5, "Male");
        c.majorLvl = major; c.lvl = major * 10 + 5; c.minorLvl = 5; c.potential = 90;
        return c;
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        check("player created and prompting", p != null && PcProbe.in.blocked);
        if (p == null) return;
        Lore.State s = Lore.state;
        Settings.lore = true; Settings.fightView = false;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        p.caution = 0; p.maxAge = 100000;
        String variant = VARIANT;
        Area h = p.location;
        int[] saved = Settings.leavePct.clone();

        // ---- A: settings
        check("A1 default chances are 20 / 15 / 10 / 25 (" + Arrays.toString(saved) + ")", Arrays.equals(saved, new int[]{20, 15, 10, 25}));
        check("A2 the blessing of a leaver below True Immortal is +2", Settings.leaveBless == 2);

        PcProbe.cmd("settings leavepct 5 6 7 8", 20); boolean setOk = Arrays.equals(Settings.leavePct, new int[]{5, 6, 7, 8});
        PcProbe.cmd("settings leavebless 3", 20); setOk &= Settings.leaveBless == 3;
        check("A3 'settings leavepct' and 'settings leavebless' set the numbers", setOk);
        Settings.leavePct = saved.clone(); Settings.leaveBless = 2;

        // ---- B: ordinary people (NPCs)
        Settings.leavePct = new int[]{100, 100, 100, 100};
        s.wanderers = new ArrayList<>(); if (s.seekers == null) s.seekers = new HashMap<>(); s.leftAt = null;
        Character n7 = npc(h, 7), kid = npc(h, 0);
        n7.children.add(kid); kid.mother = n7; kid.father = n7; kid.age = 5; kid.potential = 50;
        LoreEndings.realmReached(n7); LoreEndings.realmReached(n7);
        LoreEndings.drainLeavers();
        check("B1 an ordinary person who reaches Ascension may leave (chance 100%): gone, with a line in the counts", n7.dead && s.leftAt != null && s.leftAt[0] == 1);
        check("B2 the roll happens once per realm (the second call did nothing)", s.leftAt[0] == 1 && n7.leaveRolled == 7);
        check("B3 a leaver is not a lamp-bearer", s.wanderers.isEmpty() && !n7.wanderingImmortal);
        check("B4 the child carries +2 / +2 (blessing " + kid.blessing + ", potential " + kid.potential + ")", kid.blessing == 2 && kid.potential == 52);
        check("B5 a newborn inherits it", LoreEndings.inheritedBlessing(n7, null) == 2);

        Character n8 = npc(h, 8), n9 = npc(h, 9);
        LoreEndings.realmReached(n8); LoreEndings.realmReached(n9); LoreEndings.drainLeavers();
        check("B6 Half-Immortal and Pseudo Immortal roll their own chance", n8.dead && n9.dead && s.leftAt[1] == 1 && s.leftAt[2] == 1);

        Settings.leavePct = new int[]{0, 0, 0, 0};
        Character stay7 = npc(h, 7); LoreEndings.realmReached(stay7); LoreEndings.drainLeavers();
        check("B7 a chance of 0 keeps everybody", !stay7.dead);

        Settings.leavePct = new int[]{100, 100, 100, 100};
        Character seeker = npc(h, 7), wi = npc(h, 7), fated = npc(h, 7), beast = npc(h, 7), human = npc(h, 7), seeker10 = npc(h, 10);
        s.seekers.put(seeker.id, 0); s.seekers.put(seeker10.id, 0xFF);
        wi.wanderingImmortal = true; fated.fatedTo = 5; beast.trait = Character.Traits.Beast; human.player = 1;
        for (Character c : new Character[]{seeker, wi, fated, beast, human, seeker10}) LoreEndings.realmReached(c);
        LoreEndings.drainLeavers();
        check("B8 seekers (also at True Immortal), lamp-bearers, people of a story, beasts and humans are never rolled", !seeker.dead && !wi.dead && !fated.dead && !beast.dead && !human.dead && !seeker10.dead);
        human.player = 0;

        Character t10 = npc(h, 10);
        LoreEndings.realmReached(t10); LoreEndings.drainLeavers();
        check("B9 a True Immortal who is no seeker takes a lamp (chance 100%): a Wandering Immortal, counted in the lamps", t10.wanderingImmortal && !t10.dead && s.wanderers.contains(t10.id) && s.leftAt[3] == 1);
        check("B10 their line carries +5 (a lamp-bearer gives 5, " + LoreEndings.childBlessing(t10) + ")", LoreEndings.childBlessing(t10) == 5);
        while (s.wanderers.size() < LoreEndings.LAMP_MAX) s.wanderers.add(-1 - s.wanderers.size());
        Character t10b = npc(h, 10);
        LoreEndings.realmReached(t10b); LoreEndings.drainLeavers();
        check("B11 when all nine lamps are carried nobody else takes one", !t10b.wanderingImmortal && !t10b.dead);
        s.wanderers.removeIf(i -> i < 0);

        check("B12 the roll text lists the others who went through", Lore.rollText().contains("without a lamp"));
        Settings.leavePct = saved;

        // ---- C: texts and tables
        for (String id : new String[]{"door7", "early_door", "early_oldman", "early_rearguard", "early_strike", "early_strike_dead", "early_strike_spared", "early_leave"})
            check("C1 scene " + id + " exists and has lines", !LoreText.lines(id, Lore.vars(p, Lore.pstate(p))).isEmpty());
        check("C2 the array's odds against a weak striker: 85 / 75 / 65 / 55", LoreEndings.earlyStrikeDeath(7) == 85 && LoreEndings.earlyStrikeDeath(8) == 75 && LoreEndings.earlyStrikeDeath(9) == 65 && LoreEndings.earlyStrikeDeath(10) == 55);
        Random r = new Random(7); int bad7 = 0, bad10 = 0;
        for (int i = 0; i < 4000; i++) { if (LoreEndings.OUT_TAG[LoreEndings.pickOutsideFor(r, 7)].equals("bad")) bad7++; if (LoreEndings.OUT_TAG[LoreEndings.pickOutsideFor(r, 10)].equals("bad")) bad10++; }
        check("C3 the Outside is harsher at lower realms (bad branches " + bad7 + " vs " + bad10 + " of 4000)", bad7 > bad10 * 1.5);
        check("C4 the codas know the early endings", "leave".equals(StoryCodas.family("early_rearguard")) && "fall".equals(StoryCodas.family("early_strike_dead")) && "leave".equals(StoryCodas.family("early_alone_out_road")));

        // ---- D: the Door for a person who plays
        Lore.PState ps = Lore.pstate(p);
        if (ps == null) { Lore.tick(p); ps = Lore.pstate(p); }
        p.majorLvl = 6; p.lvl = 65; p.minorLvl = 5;
        int mk = PcProbe.mark();
        PcProbe.cmd("lore door", 20);
        check("D1 below Ascension the Door is shut and says where it opens (" + PcProbe.since(mk).replace('\n', ' ').trim() + ")", PcProbe.since(mk).contains("Ascension") && !ps.ended && ps.scenes.stream().noneMatch(x -> x.startsWith("early_")));

        p.majorLvl = 7; p.lvl = 71; p.minorLvl = 1; p.tempLvl = 71;
        Lore.tick(p);
        boolean told = false; synchronized (Notifications.history) { for (Notifications.Note nt : Notifications.history) if (nt.text != null && nt.text.contains("the Door of the Garden is open to you")) told = true; }
        check("D2 on reaching Ascension the person is told the Door is open (notice + the scene door7 waiting or shown)", told && ps.doorTold && (ps.queue.contains("door7") || ps.scenes.contains("door7")));
        check("D3 the journal says so", Lore.objective(p, ps).contains("Early Door"));
        check("D4 the Full Door is not open (not a True Immortal): Lore.whyNotCross refuses", Lore.whyNotCross(p, ps) != null);

        // not yet
        DecisionBroker.scripted.add(1);
        mk = PcProbe.mark();
        PcProbe.cmd("lore door", 20);
        check("D5 'Not yet' at the first question changes nothing and the Door stays", !ps.ended && ps.scenes.contains("early_door") && !ps.metEarly && PcProbe.since(mk).contains("waits"));
        // step through, then Not yet / Back
        DecisionBroker.scripted.add(0); DecisionBroker.scripted.add(2);
        PcProbe.cmd("lore door", 20);
        check("D6 stepping through meets the Old Man once and 'Not yet' goes back (" + ps.metEarly + ")", ps.metEarly && !ps.ended && ps.scenes.contains("early_oldman"));
        DecisionBroker.scripted.add(0); DecisionBroker.scripted.add(1); DecisionBroker.scripted.add(3); DecisionBroker.scripted.add(2); // step, leave, Back, Not yet
        PcProbe.cmd("lore door", 20);
        check("D7 Back from the leave menu ends nothing", !ps.ended);
        check("D8 the person has no lamp in any of this", s.wanderers == null || !s.wanderers.contains(p.id));

        if (variant.equals("back")) { done(); return; }

        // ---- E: one ending
        Character child = npc(h, 0); p.children.add(child); child.mother = p; child.father = p; child.age = 3; child.potential = 40;
        p.potential = 80;
        if (variant.equals("strikefull")) while (s.wanderers.size() < LoreEndings.LAMP_MAX) s.wanderers.add(-1 - s.wanderers.size());
        int blood0 = LoreEndings.bloodBonus(), lamps0 = LoreEndings.lampsCarried(s);
        switch (variant) {
            case "rear": DecisionBroker.scripted.add(0); DecisionBroker.scripted.add(1); DecisionBroker.scripted.add(0); break; // step through, Leave, the rear guard
            case "strike": case "strikefull": DecisionBroker.scripted.add(0); DecisionBroker.scripted.add(1); DecisionBroker.scripted.add(1); break;
            case "alone": DecisionBroker.scripted.add(0); DecisionBroker.scripted.add(1); DecisionBroker.scripted.add(2); break;
            case "stay": DecisionBroker.scripted.add(0); DecisionBroker.scripted.add(0); DecisionBroker.scripted.add(0); break; // step, Stay, Steward
            default: throw new IllegalStateException("unknown door variant " + variant);
        }
        mk = PcProbe.mark();
        PcProbe.cmd("lore door", 30);
        String o = PcProbe.since(mk);
        check("E1 an ending was reached (" + ps.endingId + ", score " + ps.score + ")", ps.ended && !ps.endingId.isEmpty());
        check("E2 the Hall of Fame and the Roll of the Crossed have it", variant.equals("stay") || (Lore.rollText().contains("year") && RunStats.hallOfFame().contains("ENDING")));
        check("E3 the credits are shown", variant.equals("stay") || o.contains("CREDITS"));
        switch (variant) {
            case "rear":
                check("E4 the Rear Guard: id " + ps.endingId, ps.endingId.equals("early_rearguard") && !p.wanderingImmortal && LoreEndings.lampsCarried(s) == lamps0);
                check("E5 the child carries +2 / +2 (" + child.blessing + ")", child.blessing == 2);
                break;
            case "strike": case "strikefull":
                boolean dead = ps.endingId.equals("early_strike_dead");
                check("E4 the Early Strike ends in " + (dead ? "death" : "being spared") + " (" + ps.endingId + ")", dead || ps.endingId.startsWith("early_evil_spared_"));
                if (variant.equals("strikefull")) check("E5 with every lamp carried the array always kills, and the blood goes home (+5)", dead && LoreEndings.bloodBonus() == blood0 + 5);
                else if (dead) check("E5 a death sends the blood home (+5)", LoreEndings.bloodBonus() == blood0 + 5);
                else check("E5 spared, the person walks out and the child carries +2", child.blessing == 2);
                break;
            case "alone":
                check("E4 the walk out alone: id " + ps.endingId, ps.endingId.startsWith("early_alone_") && ps.score > 0);
                check("E5 the child carries +2 / +2 (" + child.blessing + ")", child.blessing == 2);
                break;
            case "stay":
                check("E4 a Stay at the Early Door is the existing Steward (" + ps.endingId + ")", ps.endingId.equals("steward") && LoreEndings.stayedOf(ps));
                // the Seam opens again: this time leave through the Early Door's menu (Rear Guard)
                DecisionBroker.scripted.clear(); DecisionBroker.scripted.add(0); // the Early leave menu is asked directly (no 'step through'): the rear guard
                p.majorLvl = 8; p.lvl = 81;
                PcProbe.cmd("lore leave", 30);
                check("E5 'lore leave' after an early Stay offers the Early menu (no lamp) and ends with the Rear Guard (" + ps.endingId + ")", ps.endingId.equals("early_rearguard") && LoreEndings.lampsCarried(s) == lamps0);
                break;
            default:
        }
        done();
    }

    static void done() throws Exception {
        PcProbe.in.feed("2\n"); // the succession dialog, if any: Spectate
        Thread.sleep(3000);
        check("Z no exceptions in the output", !PcProbe.out().contains("Exception"));
        PcProbe.real.println("[T] DONE");
    }
}
