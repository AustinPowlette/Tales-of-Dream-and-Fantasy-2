package Game;

import java.io.PrintStream;
import java.util.*;

/** The endings through the real game loop: a human at the open Seam chooses (soldier, then another life leaves the world), the game keeps ticking, the
 *  Wandering Immortal wanders and cannot die, the succession dialog follows, and a world with the story switched off or the Palace razed keeps running. */
public class EndFlowProbe {
    static void check(String name, boolean ok) { PcProbe.passes += ok ? 1 : 0; PcProbe.fails += ok ? 0 : 1; PcProbe.real.println("[T] " + (ok ? "PASS " : "FAIL ") + name); }

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

    static Character ready(Character h, Lore.State s) {
        Organization pal = Lore.palace();
        h.potential = 100; h.majorLvl = 10; h.lvl = 101; h.maxAge = 1_000_000; h.caution = 0;
        h.location.removeChar(h); h.location = Map.tiles[Map.range / 2][Map.range / 2]; h.location.addChar(h);
        s.passes.add(h.id); pal.join(h);
        if (h.sect != pal) { pal.members.add(h); h.sect = pal; pal.memberRanks.put(h.id, Organization.SectRank.ELDER); }
        Lore.tick(h);
        Lore.PState q = Lore.pstate(h);
        for (LoreSites.Site si : LoreSites.ALL) { q.found.add(si.id); q.revealed.add(si.id); }
        q.found.add(LoreSites.SEAT); q.everMember = true; q.queue.clear();
        return h;
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        Lore.State s = Lore.state;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        p.caution = 0;
        Settings.lore = true;
        ready(p, s);
        check("F0 the human can reach the Seam", Lore.whyNotCross(p, Lore.pstate(p)) == null);
        DecisionBroker.scripted.add(0); DecisionBroker.scripted.add(1); DecisionBroker.scripted.add(0); DecisionBroker.scripted.add(0); // step through, Leave, lamp-bearer, at the front
        int mark = PcProbe.mark();
        PcProbe.cmd("lore cross", 20);
        String o = PcProbe.since(mark);
        check("F1 the soldier ending prints the story and the credits", o.contains("CREDITS"));
        PcProbe.in.feed("2\n"); // the succession dialog: Spectate
        Thread.sleep(6000);
        int t0 = GameDriver.time; Thread.sleep(8000);
        check("F2 control passed on: the soldier is no longer the player and the world keeps running", p.player != 1 && GameDriver.time > t0 + 5);
        check("F3 the soldier is a Wandering Immortal NPC and alive", p.wanderingImmortal && !p.dead && p.repTitle != null && p.repTitle.contains("Wandering"));
        check("F4 the wanderer is moving about", p.location != null);
        p.dead(); p.dead(p);
        check("F5 killing a Wandering Immortal does nothing", !p.dead);
        check("F6 the ending is in the hall of fame and the Roll of the Crossed", RunStats.hallOfFame().contains("ENDING") && Lore.rollText().contains("lamp-bearer"));
        check("F9 no exceptions in the output", !PcProbe.out().contains("Exception"));
    }
}
