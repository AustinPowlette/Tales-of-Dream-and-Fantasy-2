package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-13: a walk (goto, goquest, chase) hands the prompt back in the turn it ends, and a chase ends with the target in the player's tile.
 *  Before the fix "You caught up" was printed and the turn then passed with no prompt, so the walker stepped away (17 of 21 times); and a goto of N tiles cost N+1 weeks. */
public class ArrivalProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    static void say(String s) { PcProbe.real.println("[T] " + s); }
    static Area home() { return Map.tiles[Map.range / 2][Map.range / 2]; }
    static Character adult() {
        Character c = new Character(home());
        c.age = 30; c.gender = "Male"; c.lvl = 30; c.majorLvl = 3; c.maxAge = 100000; c.caution = 0; c.sect = null; c.trait = Character.Traits.Friendly;
        while (Map.characters.size() < c.id) Map.characters.add(null);
        Map.characters.add(c); Map.population++;
        return c;
    }
    static void tp(Character c, Area to) { c.location.removeChar(c); c.location = to; to.addChar(c); }
    static Area at(Area h, int dx, int dy) {
        int x = Math.floorMod(h.x + dx, Map.range), y = Math.floorMod(h.y + dy, Map.range);
        if (Map.tiles[x][y] == null) Map.tiles[x][y] = new Area(Area.ghost(x == 0 ? Map.range - 1 : x - 1, y), 'e');
        return Map.tiles[x][y];
    }

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

    /** One chase against a walking NPC; returns {caught messages, of those with the target in the tile at the prompt, 1 if the player ended co-located}. */
    static int[] chase(int k) throws Exception {
        Character p = PcProbe.pc(); Area h = home();
        Character w = adult(); w.targetQuests.clear(); w.spotQuests.clear(); w.currentQuest = null; w.exploreTicks = 0;
        for (int i = 0; i < 8; i++) w.backpack.add(new Item(w, Item.WeaponType.Sword, 10));
        tp(w, h);
        Area far = at(h, 12, -2 + (k % 5)); far.city = true;
        w.targetQuests.add(new Quest("Go Sell", true, 95, 1, 100, far));
        tp(p, at(h, 5 - (k % 3), 3 - (k % 4)));
        p.pc().mode = PlayerControl.Mode.MANUAL; p.targetQuests.clear(); p.currentQuest = null;
        GameDriver.frozenNpcs = false;
        int t0 = GameDriver.time, caught = 0, together = 0; boolean ok = false;
        for (int r = 0; r < 60 && GameDriver.time - t0 < 30; r++) {
            if (p.location == w.location) { ok = true; break; }
            int m1 = PcProbe.mark(); PcProbe.cmd("chase #" + w.id, 90); String o = PcProbe.since(m1);
            if (o.contains("caught up")) { caught++; if (p.location == w.location) together++; }
        }
        GameDriver.frozenNpcs = true;
        say(String.format("chase k=%d %s after %d weeks; \"You caught up\" %d times, %d of them with the target in my tile at the prompt", k, ok ? "CAUGHT" : "missed", GameDriver.time - t0, caught, together));
        w.targetQuests.clear(); w.currentQuest = null; w.dead = true; w.location.removeChar(w);
        tp(p, h); p.targetQuests.clear(); p.currentQuest = null; p.pc().mode = PlayerControl.Mode.MANUAL;
        return new int[]{caught, together};
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        PcProbe.check("player created and prompting", p != null && PcProbe.in.blocked);
        if (p == null) return;
        p.lvl = 100; p.majorLvl = 10; p.maxAge = 100000; p.caution = 0;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        p.pc().squadPolicy = 2; p.pc().friendPolicy = 1;
        Settings.lore = false;
        GameDriver.time = Math.max(GameDriver.time, 100);

        // A1: goto N tiles costs N weeks in all (it used to be N+1: an empty week at the end). Quest alerts cut a walk short by design, so the walk is renewed until it lands.
        for (int n : new int[]{3, 5, 8}) {
            Area h = home(); tp(p, h);
            Area dest = at(h, n, 0);
            int t0 = GameDriver.time; int m = PcProbe.mark(); int cmds = 0;
            while (p.location != dest && cmds++ < 20) PcProbe.cmd("goto " + dest.x + " " + dest.y, 120);
            PcProbe.check("A1 goto " + n + " tiles: at the destination after " + n + " weeks in all (took " + (GameDriver.time - t0) + ", " + cmds + " command(s))", p.location == dest && GameDriver.time - t0 == n);
        }
        // A2: the arrival of a goquest walk also hands back the prompt in the same week
        { Area h = home(); tp(p, h); Area dest = at(h, 0, 4);
          p.targetQuests.clear(); p.currentQuest = null;
          Quest q = new Quest("Go: probe", true, 50, 0, 50, dest); q.userCreated = true; q.pin(50); p.targetQuests.add(q);
          int t0 = GameDriver.time; int cmds = 0;
          while (p.location != dest && cmds++ < 20) { PcProbe.cmd("q", 30); PcProbe.cmd("goto " + dest.x + " " + dest.y, 120); }
          PcProbe.check("A2 a 4-tile walk to a quest's tile: there after 4 weeks (took " + (GameDriver.time - t0) + ")", p.location == dest && GameDriver.time - t0 == 4);
          p.targetQuests.clear(); p.currentQuest = null; }
        // A3: a chase against a walker ends with the target in my tile
        int caught = 0, together = 0, landed = 0, trials = 12;
        for (int k = 0; k < trials; k++) { int[] r = chase(k); caught += r[0]; together += r[1]; if (r[0] > 0 && r[0] == r[1]) landed++; }
        say("TOTAL: \"You caught up\" " + caught + " times, " + together + " with the target in my tile at the prompt (before the fix: 4 of 21)");
        PcProbe.check("A3 at least 95% of the caught-up messages leave the target in my tile (" + together + " of " + caught + ")", caught > 0 && together * 100 >= caught * 95);
        // A4: AUTO is untouched: it still walks like an NPC and ends the run on its count
        { Area h = home(); tp(p, h); int t0 = GameDriver.time; PcProbe.cmd("auto 3", 120);
          PcProbe.check("A4 'auto 3' runs three weeks and hands control back", GameDriver.time - t0 == 3 && p.pc().mode == PlayerControl.Mode.MANUAL); }
        say("DONE");
    }
}
