package Game;

import java.io.*;
import java.util.*;

/** Save compatibility: loads the old save.dat found in the run directory (copied from the repo), runs a few turns, saves in the new format, loads that,
 *  runs a few more, and checks the invariants and the fields added since. Must be run against the REAL population (no shrinking) with the old save. */
public class CompatProbe {
    static void check(String name, boolean ok) { PcProbe.passes += ok ? 1 : 0; PcProbe.fails += ok ? 0 : 1; PcProbe.real.println("[T] " + (ok ? "PASS " : "FAIL ") + name); }

    public static void main(String[] a) throws Exception {
        PcProbe.real = System.out;
        System.setIn(PcProbe.in);
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(PcProbe.real); } }, "game", 1024L << 20);
        t.setDaemon(true); t.start();
        try { run(); } catch (Throwable e) { PcProbe.real.println("[T] PROBE EXCEPTION " + e); e.printStackTrace(PcProbe.real); PcProbe.fails++; }
        PcProbe.real.println("[T] ===== " + PcProbe.passes + " passed, " + PcProbe.fails + " failed; gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }

    static int baseline = -1;
    static int humans() { int n = 0; for (int i = 0; i < Map.characters.size(); i++) { Character c; try { c = Map.characters.get(i); } catch (RuntimeException e) { continue; } if (c != null && !c.dead && c.player == 1) n++; } return n; }

    static void invariants(String when) {
        int bad = 0, nullGoods = 0, nullName = 0;
        for (int i = 0; i < Map.characters.size(); i++) { Character c = Map.characters.get(i); if (c != null && c.id != i) bad++; if (c != null && !c.dead && (c.name == null || c.lastName == null)) nullName++; }
        for (int i = 0; i < Map.createdAreas.size(); i++) { Area ar = Map.createdAreas.get(i); if (ar != null && ar.goodsBag == null) nullGoods++; }
        PcProbe.real.println("[T] info: " + when + ": time " + GameDriver.time + " characters " + Map.characters.size() + " areas " + Map.createdAreas.size() + " humans " + humans() + " id mismatches " + bad);
        if (baseline < 0) baseline = bad;
        check(when + ": ids equal list indices (the old save carried " + baseline + " mismatches of its own; they must not grow)", bad <= baseline);
        check(when + ": every area has a goods market and every cultivator a name", nullGoods == 0 && nullName == 0);
        int sects = 0, warsUndated = 0, over = 0;
        for (int i = 0; i < Map.createdAreas.size(); i++) { Area ar = Map.createdAreas.get(i); if (ar != null && ar.sect != null) { sects++; if (ar.sect.warTarget != null && ar.sect.warSince < 0) warsUndated++; try { ar.sect.memberCap(); } catch (RuntimeException e) { over++; } } }
        check(when + ": sect fields are usable (memberCap, warSince)", warsUndated == 0 && over == 0);
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\n1\n");          // no map viewer, load the first (newest) saved game
        { long until = System.currentTimeMillis() + 600_000; while (Map.characters.size() < 1000 && System.currentTimeMillis() < until && PcProbe.gameError == null) Thread.sleep(500); Thread.sleep(3000); } // an old save may have no human: the game then never waits for input
        PcProbe.real.println("[T] info: loaded; humans " + humans());
        invariants("after loading the old save");
        // run a few turns: with a human, through the command line; without, the game is running on its own
        int t0 = GameDriver.time;
        if (humans() > 0) { PcProbe.cmd("wait 5", 900); for (String c : new String[]{"q", "market", "squad", "companions", "lineage", "stats", "here", "music", "help"}) { String out = NewProbe.run(c); check("old save: '" + c + "' works", !out.contains("Exception")); } }
        else { long end = System.currentTimeMillis() + 15 * 60_000L; while (GameDriver.time < t0 + 5 && System.currentTimeMillis() < end && PcProbe.gameError == null) Thread.sleep(1000); }
        check("old save: the world runs on after loading (time advanced)", GameDriver.time > t0);
        invariants("after running the old save");
        // a save in the new format and a load of it
        GameDriver.saveRequestName = "compat"; // the game thread writes it between ticks
        { long until = System.currentTimeMillis() + 300_000; while (!new File("saves/compat.dat").isFile() && System.currentTimeMillis() < until) Thread.sleep(500); Thread.sleep(1500); }
        boolean saved = new File("saves/compat.dat").isFile() && new File("saves/compat.dat").length() > 1_000_000;
        check("a new-format save is written", saved);
        int pop0 = Map.population, chars0 = Map.characters.size(), areas0 = Map.createdAreas.size();
        GameDriver.loadRequest = "compat";
        int tl = GameDriver.time;
        long end = System.currentTimeMillis() + 15 * 60_000L;
        if (humans() > 0) PcProbe.cmd("wait 1", 900);
        while (GameDriver.loadRequest != null && System.currentTimeMillis() < end) Thread.sleep(500);
        Thread.sleep(3000);
        PcProbe.real.println("[T] info: reloaded: characters " + Map.characters.size() + " (were " + chars0 + "), areas " + Map.createdAreas.size() + " (were " + areas0 + ")");
        check("the new save loads: the same world comes back", Math.abs(Map.characters.size() - chars0) < chars0 / 20 + 50 && Map.createdAreas.size() >= areas0);
        invariants("after reloading the new save");
        end = System.currentTimeMillis() + 10 * 60_000L; int tr = GameDriver.time;
        if (humans() > 0) PcProbe.cmd("wait 3", 900); else while (GameDriver.time < tr + 2 && System.currentTimeMillis() < end) Thread.sleep(1000);
        check("the reloaded world runs", GameDriver.time > tr || humans() > 0);
        invariants("after running the reloaded world");
        String chatter = PcProbe.cap.toString();
        int ex = chatter.split("Exception", -1).length - 1;
        for (String line : chatter.split("\n")) if (line.contains("Exception")) { PcProbe.real.println("[T] info: exception line: " + line.trim()); break; }
        check("no exceptions in the game's output", ex == 0 && PcProbe.gameError == null);
    }
}
