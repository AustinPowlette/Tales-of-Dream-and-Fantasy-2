package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-11: the potential system (`Character.givePotential`: five Potential Pills to the first gate rising to thirty for the last point, ten gates ending at 99 and 100, the last breakthrough
 *  needing 100 and a 1 in 5 roll), the epoch's blessing (half a pill, only for the survivors of the realms it culled, up to `epochrealm`), and the tile limit as a multiple of the fair share (`crowd`). */
public class PotentialProbe {
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

    static Character adult(int potential, int major) {
        Character c = new Character(home());
        c.age = 30; c.gender = "Male"; c.majorLvl = major; c.minorLvl = 1; c.lvl = major * 10 + 1; c.maxAge = 100000; c.sect = null;
        c.affiliation = "Good"; c.affiliationNum = 1; c.born = true; c.potential = potential; c.potentialProgress = 0;
        if (c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) c.trait = Character.Traits.Friendly;
        c.id = Map.characters.size();
        GameDriver.UID = Math.max(GameDriver.UID, c.id + 1);
        Map.characters.add(c); Map.population++;
        return c;
    }
    static String say(Character p, String cmd) { int m = PcProbe.mark(); PlayerCommands.handle(p, cmd); return PcProbe.since(m); }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        GameDriver.frozenNpcs = true;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        Map.currentCharacter = p;

        // ---- A: the pills
        int[] gate = Character.GATE_POTENTIAL, cost = Character.GATE_PILLS;
        check("A1 ten gates, the last two at 99 and 100, and the pill costs 5, 7, 9, 11, 13, 15, 18, 22, 26, 30", gate.length == 10 && cost.length == 10 && gate[8] == 99 && gate[9] == 100 && Arrays.toString(cost).equals("[5, 7, 9, 11, 13, 15, 18, 22, 26, 30]") && Character.gatePotential(1) == 20 && Character.gatePotential(10) == 100);
        Character c = adult(0, 0);
        StringBuilder per = new StringBuilder();
        boolean exact = true; int total = 0;
        for (int k = 0; k < 10; k++) {
            int pills = 0;
            while (c.potential < gate[k] && pills < 1000) { c.givePotential(1); pills++; }
            per.append(pills).append(k < 9 ? "," : "");
            if (pills != cost[k] || c.potential != gate[k]) exact = false;
            total += pills;
        }
        check("A2 from 0 the pills needed to reach each gate are the table (" + per + ", " + total + " in all)", exact && total == 156);
        check("A3 at 100 nothing more is gained and no progress is kept (" + c.potential + ", " + c.potentialProgress + ")", c.givePotential(5) == 0 && c.potential == 100 && c.potentialProgress == 0);
        c = adult(10, 1);
        int g = c.givePotential(0.5);
        check("A4 half a pill under the first gate is 2 points (" + g + ", now " + c.potential + ")", g == 2 && c.potential == 12);
        c = adult(90, 7);
        g = c.givePotential(0.5);
        check("A5 half a pill at 90 is only part of a point (" + g + ", progress " + String.format("%.2f", c.potentialProgress) + ")", g == 0 && c.potential == 90 && c.potentialProgress > 0.1 && c.potentialProgress < 0.3);
        c = adult(98, 8);
        int pills = 0; while (c.potential < 99) { c.givePotential(1); pills++; }
        int pills2 = 0; while (c.potential < 100) { c.givePotential(1); pills2++; }
        check("A6 98 to 99 takes " + pills + " pills (13) and 99 to 100 takes " + pills2 + " (30)", pills == 13 && pills2 == 30);
        c = adult(19, 0);
        g = c.givePotential(0.5);
        check("A7 a gift that crosses a gate changes rate part-way (19 + half a pill = " + c.potential + ", progress " + String.format("%.2f", c.potentialProgress) + ")", g == 1 && c.potential == 20 && Math.abs(c.potentialProgress - 0.607) < 0.01);
        c = adult(60, 3);
        c.pillBag.add(new Item(Item.ItemType.Pill, Item.PillType.Potential, 11, 3));
        c.consume();
        check("A8 swallowing a Potential Pill gives a pill's worth (60 -> " + c.potential + ", " + String.format("%.2f", c.potentialProgress) + ", " + c.pillBag.get(0).quantity + " left)", c.potential == 61 && Math.abs(c.potentialProgress - 0.18) < 0.01 && c.pillBag.get(0).quantity == 2);

        c = adult(60, 3); c.player = 1; c.potentialHidden = false; c.givePotential(0.5);
        String shown = c.potentialShown(); c.player = 0;
        check("A9 the sheet of a person who plays shows the progress (" + shown + "), the sheet of an NPC does not (" + c.potentialShown() + ")", shown.startsWith("60 (+") && shown.endsWith("%)") && c.potentialShown().equals("60"));

        // ---- B: the last gates
        c = adult(99, 9); c.maxAge = 100000; c.totalAgeGained = 100000; c.inhibitor = 1;
        boolean stuck = true;
        for (int i = 0; i < 400; i++) { c.majorLvl = 9; c.minorLvl = 10; c.lvl = 100; c.cultivationBase = c.cultivationBaseMax = 1; c.cultivate(); if (c.majorLvl >= 10) stuck = false; }
        check("B1 the last breakthrough is closed to 99 potential (400 tries)", stuck);
        c = adult(100, 9); c.maxAge = 100000; c.totalAgeGained = 100000; int through = 0;
        for (int i = 0; i < 400; i++) { c.majorLvl = 9; c.minorLvl = 10; c.lvl = 100; c.maxAge = 100000; c.totalAgeGained = 100000; c.inhibitor = 1; c.cultivationBase = c.cultivationBaseMax = 1; c.cultivate(); if (c.majorLvl >= 10) through++; }
        check("B2 with 100 potential it is a 1 in 5 chance (" + through + " of 400)", through > 40 && through < 120);
        c = adult(98, 8); c.maxAge = 100000; c.totalAgeGained = 100000; boolean b9 = false;
        for (int i = 0; i < 400 && !b9; i++) { c.majorLvl = 8; c.minorLvl = 10; c.lvl = 90; c.maxAge = 100000; c.totalAgeGained = 100000; c.inhibitor = 1; c.cultivationBase = c.cultivationBaseMax = 1; c.cultivate(); if (c.majorLvl >= 9) b9 = true; }
        c.potential = 99; boolean at99 = false;
        for (int i = 0; i < 400 && !at99; i++) { c.majorLvl = 8; c.minorLvl = 10; c.lvl = 90; c.maxAge = 100000; c.totalAgeGained = 100000; c.inhibitor = 1; c.cultivationBase = c.cultivationBaseMax = 1; c.cultivate(); if (c.majorLvl >= 9) at99 = true; }
        check("B3 Pseudo Immortal (the 9th breakthrough) needs 99 now, not 100 (at 98 only a rare lucky try: " + b9 + "; at 99: " + at99 + ")", at99);
        c = adult(40, 2);
        check("B4 Seek Potential's target follows the table (Major 2 wants " + Character.gatePotential(3) + ")", Character.gatePotential(3) == 52 && Reincarnation.GATES == Character.GATE_POTENTIAL && Reincarnation.cap(99) == 99 && Reincarnation.cap(98) == 89);

        // ---- C: the epoch's blessing
        Arrays.fill(GameDriver.epochTierCulled, true);
        Settings.epochMaxRealm = 3;
        Character lo = adult(10, 1), mid = adult(50, 3), hi = adult(10, 4), top = adult(30, 8);
        GameDriver.epochBlessing();
        check("C1 realms up to the limit are blessed (a 10 became " + lo.potential + ", a 50 has " + String.format("%.2f", mid.potential + mid.potentialProgress) + ")", lo.potential == 12 && mid.potential + mid.potentialProgress > 50.2);
        check("C2 a realm above the limit is not (a Major 4 with 10 stays " + hi.potential + "; a Major 8 with 30 stays " + top.potential + ")", hi.potential == 10 && hi.potentialProgress == 0 && top.potential == 30);
        lo.potential = 10; lo.potentialProgress = 0; mid.potential = 50; mid.potentialProgress = 0;
        GameDriver.epochTierCulled[1] = false; // (fewer than 10 people there: the purge left that realm alone)
        GameDriver.epochBlessing();
        check("C3 a realm the purge left alone is not blessed either (" + lo.potential + ")", lo.potential == 10 && mid.potential + mid.potentialProgress > 50.2);
        Arrays.fill(GameDriver.epochTierCulled, true);
        say(p, "settings epochrealm 5");
        check("C4 `settings epochrealm` sets the limit (" + Settings.epochMaxRealm + ") and a bad value is refused", Settings.epochMaxRealm == 5 && say(p, "settings epochrealm many").contains("0 to 10") && Settings.epochMaxRealm == 5);
        hi.potential = 10; hi.potentialProgress = 0;
        GameDriver.epochBlessing();
        check("C5 with the limit at 5 a Major 4 is blessed (" + hi.potential + ")", hi.potential == 12);
        Settings.epochMaxRealm = 3;

        // ---- D: the crowd limit
        double f0 = Settings.crowdFactor;
        check("D1 the usual limit is 3.5 times a fair share (" + f0 + ")", f0 == 3.5);
        int start = 100000;
        int expected = (int) Math.round(3.5 * start / (Map.range * Map.range));
        check("D2 at 100,000 people on 31 x 31 tiles a wild tile holds " + GameDriver.crowdFor(start) + " (364)", GameDriver.crowdFor(start) == expected && expected == 364);
        String out = say(p, "settings crowd 4");
        check("D3 `settings crowd 4` changes the limit and says so (" + GameDriver.CROWD_CAPACITY + ")", Settings.crowdFactor == 4.0 && GameDriver.CROWD_CAPACITY == GameDriver.crowdFor(Map.startingPopulation) && out.contains("4.0 times") && GameDriver.CROWD_CAPACITY_CITY == 4 * GameDriver.CROWD_CAPACITY);
        out = say(p, "settings crowd lots");
        check("D4 a word is refused", Settings.crowdFactor == 4.0 && out.contains("takes a number"));
        Settings.save(); Settings.crowdFactor = 1; Settings.epochMaxRealm = 9; Settings.load();
        check("D5 the factor and the realm limit are saved and loaded (" + Settings.crowdFactor + ", " + Settings.epochMaxRealm + ")", Settings.crowdFactor == 4.0 && Settings.epochMaxRealm == 3);
        say(p, "settings crowd 3.5"); Settings.save();
        check("Z no exceptions", !PcProbe.out().contains("Exception"));
    }
}
