package Game;

import java.io.*;
import java.util.*;

/** Two human players (multiplayer): prompts must arrive sequentially, and mixed modes must coexist. */
public class PcProbe2 {
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

    static ArrayList<Character> humans() {
        ArrayList<Character> r = new ArrayList<>();
        for (int i = 0; i < Math.min(10, Map.characters.size()); i++) { Character c = Map.characters.get(i); if (c != null && c.player == 1 && !c.dead) r.add(c); }
        return r;
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\nm\n2\n1\nn\n1\nn\n");
        PcProbe.awaitBlocked(180);
        ArrayList<Character> hs = humans();
        check("M two human characters created", hs.size() == 2);
        Character p1 = hs.get(0), p2 = hs.get(1); p1.pc().squadPolicy = 2; p2.pc().squadPolicy = 2; p1.pc().friendPolicy = 1; p2.pc().friendPolicy = 1; // a squad request would ask a question and swallow a command
        int t0 = GameDriver.time;
        // each tick prompts player 1 then player 2, in order
        for (int i = 0; i < 6; i++) { PcProbe.cmd("stay"); PcProbe.cmd("stay"); }
        check("M six ticks passed with two prompts each", GameDriver.time == t0 + 6);
        // player 2 goes AUTO, player 1 stays manual
        int m = PcProbe.mark();
        PcProbe.cmd("stay");          // p1 acts
        PcProbe.cmd("auto 40");       // p2 switches to AUTO for 40 turns
        for (int i = 0; i < 40 && p2.pc().mode == PlayerControl.Mode.AUTO; i++) PcProbe.cmd("stay"); // only p1 is prompted while p2 runs
        check("M p2 ran on its own while p1 kept playing", p2.pc().mode == PlayerControl.Mode.MANUAL && GameDriver.time >= t0 + 40);
        if (!(p2.pc().mode == PlayerControl.Mode.MANUAL && GameDriver.time >= t0 + 40)) { String tail = PcProbe.since(m); PcProbe.real.println("[T] debug: p2 mode=" + p2.pc().mode + " dead=" + p2.dead + " autoTurns=" + p2.pc().autoTurns + " time=" + GameDriver.time + " t0=" + t0 + " p1dead=" + p1.dead + " | " + tail.substring(Math.max(0, tail.length() - 400))); }
        check("M sequential decisions: no exceptions", !PcProbe.since(m).contains("Exception"));
        // both settle in different modes with different duty sets
        PcProbe.cmd("duties city on");
        PcProbe.cmd("stay");
        PcProbe.cmd("duties sect on");
        check("M duty toggles are per character", p1.pc().duties[PlayerControl.CITY] && !p1.pc().duties[PlayerControl.SECT] && p2.pc().duties[PlayerControl.SECT] && !p2.pc().duties[PlayerControl.CITY]);
        check("M idMismatch 0", PcProbe.idMismatch() == 0);
    }
}
