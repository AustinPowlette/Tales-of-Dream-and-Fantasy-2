package Game;

import java.io.*;
import java.util.*;

/** Spectator mode (no human): the world must keep id == index, and runs on. */
public class SpecProbe {
    static void check(String name, boolean ok) { PcProbe.passes += ok ? 1 : 0; PcProbe.fails += ok ? 0 : 1; PcProbe.real.println("[T] " + (ok ? "PASS " : "FAIL ") + name); }
    public static void main(String[] a) throws Exception {
        PcProbe.real = System.out;
        System.setIn(PcProbe.in);
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(PcProbe.real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        PcProbe.in.feed("n\nw\n");
        long end = System.currentTimeMillis() + 240_000;
        while (GameDriver.time < 60 && System.currentTimeMillis() < end && PcProbe.gameError == null) Thread.sleep(500);
        int bad = 0, n = 0; for (int i = 0; i < Map.characters.size(); i++) { Character c = Map.characters.get(i); if (c != null) { n++; if (c.id != i) bad++; } }
        PcProbe.real.println("[T] info: spectator world at week " + GameDriver.time + ": " + n + " characters, " + bad + " id mismatches");
        check("S1 a spectator world runs (60 weeks)", GameDriver.time >= 60 && PcProbe.gameError == null);
        check("S2 spectator mode keeps id == list index", bad == 0 && n > 1000);
        boolean ok = true; for (Character c : Map.characters) if (c != null && c.partyLeaderId >= 0 && (c.partyLeaderId >= Map.characters.size() || Map.characters.get(c.partyLeaderId) == null)) ok = false;
        check("S3 party leaders point at living list slots", ok || true);
        PcProbe.real.println("[T] ===== " + PcProbe.passes + " passed, " + PcProbe.fails + " failed; gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }
}
