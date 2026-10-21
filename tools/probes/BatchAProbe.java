package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-16, change set A (beta tester's list): the Try-for-a-child command for a father who plays, the two walk settings (a fight / a new alert stops a walk: both off by default), Refine Blood without a fee
 *  (the Evil alchemist keeps half), and the gatherers' multiplier with the tile's quality cap and the floor that keeps a tile from running dry for good. */
public class BatchAProbe {
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

    static Area at(int dx, int dy) {
        Area h = Map.tiles[Map.range / 2][Map.range / 2];
        int x = Math.floorMod(h.x + dx, Map.range), y = Math.floorMod(h.y + dy, Map.range);
        if (Map.tiles[x][y] == null) Map.tiles[x][y] = new Area(Area.ghost(x == 0 ? Map.range - 1 : x - 1, y), 'e');
        return Map.tiles[x][y];
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        check("player created and prompting", p != null && PcProbe.in.blocked);
        if (p == null) return;
        Settings.lore = false;
        p.lvl = 100; p.majorLvl = 10; p.maxAge = 100000; p.caution = 0; p.targetQuests.clear(); p.spotQuests.clear(); p.currentQuest = null;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        p.pc().breakthroughPolicy = PlayerControl.BT_AUTO;
        Area lab = at(6, 6); lab.monsters = 0;
        p.location.removeChar(p); p.location = lab; lab.addChar(p);

        // ---- A1: try for a child (a father who plays asks his wife)
        p.gender = "Male"; p.age = 30;
        PcProbe.cmd("child", 60);
        check("A1 without a spouse the command says so and spends no week", PcProbe.out().contains("no living spouse"));
        Character wife = new Character(lab);
        wife.age = 25; wife.gender = "Female"; wife.maxAge = 100000; wife.born = true; wife.trait = Character.Traits.Friendly; wife.targetQuests.clear();
        p.spouse = wife; wife.spouse = p;
        Area elsewhere = at(7, 6);
        wife.location.removeChar(wife); wife.location = elsewhere; elsewhere.addChar(wife);
        int w0 = GameDriver.time;
        PcProbe.cmd("child", 60);
        check("A1b the wife must be in the tile (no week passes)", GameDriver.time == w0 && !wife.pregnant);
        wife.location.removeChar(wife); wife.location = lab; lab.addChar(wife);
        int tries = 0;
        while (!wife.pregnant && tries < 90) { PcProbe.cmd("child", 60); tries++; }
        check("A1c asking again and again, she is expecting within " + tries + " tries (one in ten each)", wife.pregnant && tries >= 1);
        check("A1d a week passes per try (" + w0 + " -> " + GameDriver.time + ")", GameDriver.time >= w0 + tries - 1);
        int w1 = GameDriver.time;
        PcProbe.cmd("child", 60);
        check("A1e once she is expecting it says so and spends nothing", GameDriver.time == w1 && PcProbe.out().contains("already expecting"));
        p.spouse = null; wife.spouse = null;

        // ---- A2: the walk settings
        check("A2 both walk settings are off by default", !p.pc().walkStopFight && !p.pc().walkStopNotice);
        PcProbe.cmd("policy walkfight on", 30);
        PcProbe.cmd("policy walknotice on", 30);
        check("A2b the policy command sets them", p.pc().walkStopFight && p.pc().walkStopNotice);
        PcProbe.cmd("policy walkfight off", 30);
        PcProbe.cmd("policy walknotice off", 30);
        check("A2c ...and clears them", !p.pc().walkStopFight && !p.pc().walkStopNotice);
        int sx = p.location.x, sy = p.location.y;
        int tx = Math.floorMod(sx + 3, Map.range);
        at(1, 6); at(2, 6); at(3, 6); at(4, 6); at(5, 6); at(8, 6); at(9, 6);
        p.pc().attackedThisTick = true; p.pc().attackedBy = "Grey Wolf";
        PcProbe.cmd("goto " + tx + " " + sy, 120);
        check("A2d a fight on the way does not stop the walk when 'walkfight' is off: you arrive (" + p.location.x + "," + p.location.y + " of " + tx + "," + sy + ")", p.location.x == tx && p.location.y == sy);
        check("A2e ...and it says so", PcProbe.out().contains("on the road; you keep walking"));
        p.pc().walkStopFight = true;
        p.pc().attackedThisTick = true; p.pc().attackedBy = "Grey Wolf";
        int ex = p.location.x;
        PcProbe.cmd("goto " + Math.floorMod(ex - 3, Map.range) + " " + sy, 120);
        check("A2f with 'walkfight' on the same fight stops the walk after one step (" + p.location.x + " from " + ex + ")", p.location.x != Math.floorMod(ex - 3, Map.range) && p.location.x != ex);
        p.pc().walkStopFight = false;
        PcProbe.cmd("settings tilecap off", 30);
        check("A2g the tile cap setting is a switch (" + Settings.tileQualityCap + ")", !Settings.tileQualityCap);
        PcProbe.cmd("settings tilecap on", 30);

        // ---- A3: gatherers
        Character g = new Character(lab);
        g.age = 30; g.gender = "Female"; g.maxAge = 100000; g.trait = Character.Traits.Gatherer; g.professionLvl = 50;
        check("A3 a skill-50 gatherer gets x4 on level-2 herbs, x1 on level-5 and above", g.gatherBonus(true, 2) == 4 && g.gatherBonus(true, 1) == 5 && g.gatherBonus(true, 5) == 1 && g.gatherBonus(true, 8) == 1);
        check("A3b ...and nothing for ore (he is not a Miner), nothing for a plain trade", g.gatherBonus(false, 2) == 1 && p.gatherBonus(true, 1) == 1);
        Character m = new Character(lab);
        m.age = 30; m.gender = "Male"; m.maxAge = 100000; m.trait = Character.Traits.Miner; m.professionLvl = 30;
        check("A3c a skill-30 miner gets x3 on level-1 ore", m.gatherBonus(false, 1) == 3 && m.gatherBonus(false, 3) == 1);
        // the tile's cap: a level-15 tile (major 1) yields up to level 3
        lab.lvl = 15; lab.herbMaterials = 100_000_000; lab.metalMaterials = 100_000_000;
        Character plain = new Character(lab);
        plain.age = 30; plain.gender = "Male"; plain.maxAge = 100000; plain.trait = Character.Traits.Friendly; plain.professionLvl = 1;
        plain.materialBag.clear();
        for (int i = 0; i < 600; i++) plain.forage();
        int maxQ = 0; long total = 0; for (Item it : plain.materialBag) { if (it.itemType == Item.ItemType.Herb) { maxQ = Math.max(maxQ, it.lvl); total += it.quantity; } }
        check("A3d on a level-15 tile 600 gatherings never find herbs above level 3 (best " + maxQ + ", " + total + " herbs)", maxQ > 0 && maxQ <= 3);
        Settings.tileQualityCap = false;
        plain.materialBag.clear();
        for (int i = 0; i < 3000; i++) plain.forage();
        maxQ = 0; for (Item it : plain.materialBag) if (it.itemType == Item.ItemType.Herb) maxQ = Math.max(maxQ, it.lvl);
        Settings.tileQualityCap = true;
        check("A3e with the cap off, rarer herbs come (best " + maxQ + " in 3000)", maxQ >= 4);
        // the multiplier and the ground: the pool loses the base amount only
        g.materialBag.clear(); g.professionLvl = 100; lab.herbMaterials = 50_000_000; lab.lvl = 100;
        int ground0 = lab.herbMaterials;
        for (int i = 0; i < 200; i++) g.forage();
        long found = 0; for (Item it : g.materialBag) if (it.itemType == Item.ItemType.Herb) found += it.quantity;
        long lost = ground0 - lab.herbMaterials;
        check("A3f a skill-100 gatherer takes much more than the ground loses (found " + found + ", the ground lost " + lost + ")", found > lost * 2 && lost > 0);
        // learning the trade
        Character apprentice = new Character(lab);
        apprentice.age = 20; apprentice.gender = "Male"; apprentice.maxAge = 100000; apprentice.trait = Character.Traits.Gatherer; apprentice.professionLvl = 1; apprentice.professionExp = 0;
        for (int i = 0; i < 400; i++) apprentice.forage();
        check("A3g gathering teaches the trade (skill 1 -> " + apprentice.professionLvl + " after 400 gatherings)", apprentice.professionLvl >= 20);
        Character other = new Character(lab);
        other.age = 20; other.gender = "Male"; other.maxAge = 100000; other.trait = Character.Traits.Alchemist; other.professionLvl = 1;
        for (int i = 0; i < 50; i++) other.forage();
        check("A3h an alchemist who forages does not gain his alchemy skill that way (" + other.professionLvl + ")", other.professionLvl == 1);
        // the floor
        lab.herbMaterials = 0; lab.metalMaterials = 0;
        for (int i = 0; i < 52; i++) lab.age();
        check("A3i a tile gathered to nothing is not empty for good (a year later: " + lab.herbMaterials + " herbs, " + lab.metalMaterials + " ore)", lab.herbMaterials > 1000 && lab.metalMaterials > 1000);
        System.out.println();
        PcProbe.real.println("[T] DONE");
    }
}
