package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-14: the breakthrough a person who plays chooses (`Character.heldBreakthrough`, `breakthroughNow`, `offerBreakthrough`, `policy breakthrough`, the `breakthrough` command). A full bar at the last level of a
 *  realm waits for him (MANUAL, policy ask or button); the prompt says the potential, the odds, the cost of a failure and the tribulation; Break through now tries it (no week is spent); "Not yet, and do not ask
 *  again" switches the question off; policy auto, AUTO, HYBRID and NPCs break through by themselves as before. */
public class BreakthroughProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    static void say(String s) { PcProbe.real.println("[T] " + s); }
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

    /** At the last level of Major 0 with a full bar. */
    static void ready(Character c, int potential) {
        c.majorLvl = 0; c.minorLvl = 10; c.lvl = 10; c.potential = potential; c.inhibitor = 1; c.maxAge = 1000; c.totalAgeGained = 1000;
        c.cultivationBaseMax = Math.pow(c.lvl, 3) * (((70. + 1. / 3.) + ((11. / 6.) * c.lvl)) / 25.);
        c.cultivationBase = c.cultivationBaseMax; c.cultivation = 10;
        c.breakthroughReady = false; c.breakthroughAsked = false;
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        check("player created and prompting", p != null && PcProbe.in.blocked);
        if (p == null) return;
        p.maxAge = 100000; p.caution = 0; p.targetQuests.clear(); p.spotQuests.clear(); p.currentQuest = null; p.trait = Character.Traits.Friendly;
        Settings.lore = false;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        PlayerControl pc = p.pc();

        // ---- B1: a full bar waits
        ready(p, 10);
        p.cultivate(1.0);
        check("B1 a full bar at the last level is held: no level gained (Lv." + p.lvl + "), ready " + p.breakthroughReady, p.lvl == 10 && p.majorLvl == 0 && p.breakthroughReady && p.minorLvl == 10);
        double bar = p.cultivationBase;
        for (int i = 0; i < 5; i++) p.cultivate(1.0);
        check("B1b more weeks change nothing: no failure, the bar stays at its cap (inhibitor " + p.inhibitor + ")", p.inhibitor == 1 && p.cultivationBase == p.cultivationBaseMax && p.cultivationBase == bar && p.majorLvl == 0);

        // ---- B2: the note
        String low = p.breakthroughNote();
        check("B2 the note below the gate warns (1 in 15, the inhibitor)", low.contains("Foundation") && low.contains("WARNING") && low.contains("1 try in 15") && low.contains("inhibitor 1 -> 2"));
        p.potential = 25;
        check("B2b above the gate it says the breakthrough will succeed", p.breakthroughNote().contains("will succeed") && !p.breakthroughNote().contains("WARNING"));
        p.majorLvl = 2; p.minorLvl = 10; p.potential = 60;
        check("B2c before a tribulation realm (Soul Wandering) it gives the odds of the tribulation", p.breakthroughNote().contains("tribulation") && p.breakthroughNote().contains("60%"));
        p.majorLvl = 9; p.potential = 99;
        check("B2d the last realm says 1 try in 5", p.breakthroughNote().contains("1 try in 5"));

        // ---- B3: the prompt, once per full bar
        ready(p, 25);
        p.cultivate(1.0);
        DecisionBroker.scripted.clear(); DecisionBroker.scripted.add(1);
        int prompts0 = DecisionBroker.prompts;
        p.offerBreakthrough();
        check("B3 the prompt is put once (" + (DecisionBroker.prompts - prompts0) + ") and 'Not yet' leaves the bar full", DecisionBroker.prompts - prompts0 == 1 && p.breakthroughReady && p.majorLvl == 0);
        p.offerBreakthrough();
        check("B3b ...and it is not asked again for the same bar", DecisionBroker.prompts - prompts0 == 1);

        // ---- B4: the command succeeds above the gate and costs no week
        int t0 = GameDriver.time;
        PcProbe.cmd("breakthrough", 120);
        check("B4 'breakthrough' with enough potential reaches Foundation (Major " + p.majorLvl + ", minor " + p.minorLvl + ", ready " + p.breakthroughReady + ")", p.majorLvl == 1 && p.minorLvl == 1 && !p.breakthroughReady);
        check("B4b ...and spends no week (" + t0 + " -> " + GameDriver.time + ") and adds the lifespan", GameDriver.time == t0 && p.maxAge == 1100);

        // ---- B5: the prompt's answers
        ready(p, 25);
        p.cultivate(1.0);
        DecisionBroker.scripted.clear(); DecisionBroker.scripted.add(0);
        p.offerBreakthrough();
        check("B5 'Break through now' does it", p.majorLvl == 1 && !p.breakthroughReady);
        ready(p, 25);
        p.cultivate(1.0);
        DecisionBroker.scripted.clear(); DecisionBroker.scripted.add(2);
        p.offerBreakthrough();
        check("B5b 'Not yet, and do not ask again' switches the question off (policy " + PlayerControl.BREAKTHROUGH_POLICIES[pc.breakthroughPolicy] + ") and keeps the bar full", pc.breakthroughPolicy == PlayerControl.BT_BUTTON && p.breakthroughReady && p.majorLvl == 0);
        int pr = DecisionBroker.prompts;
        p.breakthroughAsked = false;
        p.offerBreakthrough();
        check("B5c with the button policy nothing is asked", DecisionBroker.prompts == pr);
        PcProbe.cmd("policy breakthrough ask", 60);
        check("B5d 'policy breakthrough ask' turns the question back on", pc.breakthroughPolicy == PlayerControl.BT_ASK);
        PcProbe.cmd("policy breakthrough sometimes", 60);
        check("B5e a bad value is refused", pc.breakthroughPolicy == PlayerControl.BT_ASK);

        // ---- B6: a failure costs the inhibitor
        boolean failed = false;
        for (int i = 0; i < 40 && !failed; i++) {
            ready(p, 5);
            p.cultivate(1.0);
            p.breakthroughNow();
            failed = p.majorLvl == 0 && p.inhibitor == 2 && p.lvl == 10;
        }
        check("B6 below the gate a failed try costs a level back and an inhibitor (Lv." + p.lvl + ", inhibitor " + p.inhibitor + ")", failed);

        // ---- B7: not ready
        p.majorLvl = 0; p.minorLvl = 4; p.lvl = 4; p.cultivationBase = 0; p.breakthroughReady = false;
        int lv = p.lvl;
        PcProbe.cmd("breakthrough", 60);
        check("B7 'breakthrough' with nothing to break through says so and changes nothing", p.lvl == lv && p.majorLvl == 0);

        // ---- B8: the others break through by themselves
        pc.breakthroughPolicy = PlayerControl.BT_AUTO;
        ready(p, 25);
        p.cultivate(1.0);
        check("B8 policy auto: no hold (Major " + p.majorLvl + ")", p.majorLvl == 1 && !p.breakthroughReady);
        pc.breakthroughPolicy = PlayerControl.BT_ASK;
        pc.mode = PlayerControl.Mode.AUTO;
        ready(p, 25);
        p.cultivate(1.0);
        check("B8b AUTO mode: no hold (Major " + p.majorLvl + ")", p.majorLvl == 1 && !p.breakthroughReady);
        pc.mode = PlayerControl.Mode.MANUAL;
        Character npc = new Character(p.location);
        npc.age = 30; npc.gender = "Male"; npc.maxAge = 100000; npc.trait = Character.Traits.Friendly;
        ready(npc, 25);
        npc.cultivate(1.0);
        check("B8c an NPC breaks through by itself (Major " + npc.majorLvl + ")", npc.majorLvl == 1 && !npc.breakthroughReady);

        // ---- B9: the meditate line and the heir
        ready(p, 25);
        String line = p.meditate(false);
        check("B9 meditating with a full bar says a breakthrough waits", line.contains("breakthrough waits"));
        PlayerControl heir = new PlayerControl();
        pc.breakthroughPolicy = PlayerControl.BT_BUTTON;
        heir.inheritFrom(pc);
        check("B9b a new body keeps the policy", heir.breakthroughPolicy == PlayerControl.BT_BUTTON);
        say("DONE");
    }
}
