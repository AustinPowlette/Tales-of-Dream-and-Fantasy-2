package Game;

import java.io.PrintStream;
import java.util.*;

/** Reproduces "learning a technique / buying from the sect / Move Here moves me extra" for a MANUAL human: logs the position tick by tick. */
public class MoveProbe {
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

    /** Samples (tick, tile) while a command runs: every change is one entry. */
    static String trace(String cmd) throws Exception {
        final Character p = PcProbe.pc(); final StringBuilder sb = new StringBuilder(); final boolean[] go = {true};
        Thread t = new Thread(() -> { String last = ""; while (go[0]) { String k = "t" + GameDriver.time + "@" + p.location.x + "," + p.location.y; if (!k.equals(last)) { sb.append(k).append(' '); last = k; } try { Thread.sleep(1); } catch (InterruptedException e) { return; } } });
        t.start(); PcProbe.cmd(cmd); go[0] = false; t.join(); return sb.toString();
    }

    static String at(Character c) { return c.location.x + "," + c.location.y; }
    static void tp(Character c, Area to) { c.location.removeChar(c); c.location = to; to.addChar(c); }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        Settings.lore = false; // (the Convocation's scenes stop a walk: duties on register for the tournament)
        p.pc().breakthroughPolicy = PlayerControl.BT_AUTO; // (a held breakthrough asks at the prompt and would take the next command line as its answer)
        p.caution = 0; p.ss += 1_000_000; p.convertCurrency();
        PcProbe.real.println("[T] info: mode=" + p.pc().mode + " duties on=" + p.pc().dutiesOn() + " age=" + p.age + " sect=" + (p.sect == null ? "none" : p.sect.name) + " at " + at(p));
        Area city = Map.tiles[Map.range / 2][Map.range / 2]; if (!city.city) city.city = true; if (city.libraries.isEmpty()) city.libraries.add(new Library(city));

        if (city == null) { check("MV0 a city with a library exists", false); return; }
        tp(p, city);
        Library lib = city.libraries.get(0);
        for (int i = 0; i < 6; i++) lib.techniques.add(new Technique(i % 2 == 0 ? Technique.techniqueType.Martial : Technique.techniqueType.Movement, p, 20 + i * 10));
        PcProbe.real.println("[T] info: library techniques=" + lib.techniques.size() + " city=" + city.name + " at " + at(p));
        // 1: learn
        for (int i = 0; i < 4; i++) {
            String before = at(p); int tk = GameDriver.time;
            String tr = trace("learn 1"); PcProbe.real.println("[T] info: learn trace " + tr);
            PcProbe.real.println("[T] info: learn #" + (i + 1) + " " + before + " -> " + at(p) + " ticks " + (GameDriver.time - tk));
        }
        check("MV1 learning a technique does not move a MANUAL player", at(p).equals(city.x + "," + city.y));
        // 1b: the same with every duty ON and a few waypoint quests (what a GUI player usually has)
        p.pc().setAllDuties(true);
        PcProbe.cmd("qnew Far_place " + ((city.x + 6) % Map.range) + " " + city.y + " 70");
        for (int i = 0; i < 4; i++) { String tr = trace("learn 1"); PcProbe.real.println("[T] info: duties-on learn trace " + tr); }
        check("MV1b learning with duties on does not walk a MANUAL player away", at(p).equals(city.x + "," + city.y));
        p.pc().setAllDuties(false);
        // 2: stay as a control
        String b2 = at(p); PcProbe.cmd("stay"); check("MV2 staying does not move", at(p).equals(b2));
        // 3: goto three tiles east, tick by tick
        int tx = (p.location.x + 3) % Map.range, ty = p.location.y;
        tp(p, p.location); // no-op
        String start = at(p);
        PcProbe.real.println("[T] info: goto trace " + trace("goto " + tx + " " + ty));
        StringBuilder sb = new StringBuilder(start + " ->");
        sb.append(" " + at(p));
        for (int i = 0; i < 5; i++) { PcProbe.cmd("stay"); sb.append(" " + at(p)); }
        PcProbe.real.println("[T] info: goto path: " + sb);
        // 3b: the same walk with every duty ON: it stops exactly on the tile, and `stay` / `wait` never let a duty walk the player away
        p.pc().setAllDuties(true);
        tp(p, city); PcProbe.cmd("stay");
        int gx = (city.x + 5) % Map.range;
        String gt = trace("goto " + gx + " " + city.y);
        PcProbe.real.println("[T] info: duties-on goto trace " + gt);
        for (int i = 0; i < 12 && !(p.location.x == gx && p.location.y == city.y); i++) PcProbe.cmd("stay"); // alerts may have stopped the walk: finish by hand
        check("MV3b a walk with duties on arrives and stops on the tile", p.location.x == gx && p.location.y == city.y);
        String before3 = at(p);
        for (int i = 0; i < 4; i++) PcProbe.cmd("stay");
        PcProbe.cmd("wait 3");
        check("MV3c with every duty on, `stay` and `wait` do not move the player", at(p).equals(before3));
        p.pc().setAllDuties(false);
        // 4: buy from sect: give the player a sect and a spot quest
        Organization sect = city.sect;
        if (sect == null) for (Area ar : Map.createdAreas) if (ar != null && ar.sect != null && !ar.sect.virtual) { sect = ar.sect; city = ar; break; }
        if (sect != null) {
            tp(p, sect.location != null ? sect.location : city);
            if (p.sect != sect) { if (p.sect != null) p.sect.resign(p); sect.members.add(p); p.sect = sect; sect.memberRanks.put(p.id, Organization.SectRank.OUTER_DISCIPLE); }
            p.sectPoints = 5000;
            p.spotQuests.add(new Quest("Buy from Sect", true, 45, 0, 65));
            for (int i = 0; i < 3; i++) {
                String before = at(p); int mark = PcProbe.mark();
                PcProbe.real.println("[T] info: buy trace " + trace("do Buy from Sect"));
                PcProbe.real.println("[T] info: buy from sect #" + (i + 1) + " " + before + " -> " + at(p) + " output: " + PcProbe.since(mark).replace("\n", " | ").substring(0, Math.min(160, PcProbe.since(mark).length())));
            }
        } else PcProbe.real.println("[T] info: no sect found");
    }
}
