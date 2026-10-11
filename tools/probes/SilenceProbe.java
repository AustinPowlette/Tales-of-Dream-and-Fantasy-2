package Game;

import java.io.*;
import java.util.*;

/** Plays the real game (stdin fed like PcProbe) and runs every command a button sends in a prepared state, reporting the ones that print nothing:
 *  a button the player presses should always say what happened. Also runs the Profession button for every trait and prints what each one says. */
public class SilenceProbe {
    static void check(String name, boolean ok) { PcProbe.check(name, ok); }

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

    static String run1(String c) throws Exception {
        int m = PcProbe.mark();
        PcProbe.cmd(c, 120);
        StringBuilder sb = new StringBuilder();
        Character pl = PcProbe.pc();
        String nm = pl == null ? "@@" : pl.name + " " + pl.lastName + " - ";
        for (String line : PcProbe.since(m).split("\n")) { // the status block that follows every command is not the command's answer
            String t = line.trim();
            if (t.isEmpty() || t.equals(">") || t.contains("| MANUAL") || t.startsWith("next:") || t.startsWith("try:") || t.startsWith(nm)) continue;
            sb.append(t).append("\n");
        }
        return sb.toString().trim();
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        if (p == null) { check("player created", false); return; }
        p.lvl = 100; p.majorLvl = 10; p.minorLvl = 0; p.maxAge = 100000; p.caution = 0;
        DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        DecisionBroker.scripted = new ArrayDeque<>();
        p.pc().squadPolicy = 2; p.pc().friendPolicy = 1;
        p.ss = 50_000_000L; p.convertCurrency();
        p.materialBag.add(new Item(Item.ItemType.Herb, 3, 6000)); p.materialBag.add(new Item(Item.ItemType.Metal, 3, 6000));
        // ---- the Profession button for every trait
        PcProbe.real.println("[T] ----- Profession per trait (at " + p.location.name + ", a " + (p.location.city ? "city" : "tile") + ") -----");
        for (Character.Traits tr : Character.Traits.values()) {
            p.trait = tr;
            String o = run1("profession");
            PcProbe.real.println("[P] " + tr + " -> " + (o.isEmpty() ? "(NOTHING PRINTED)" : o.replace("\n", " | ")));
            if (o.isEmpty() && p.hasTrade()) check("P " + tr + " says something", false);
        }
        p.trait = Character.Traits.Innovative; p.inspiration = 100; if (p.equippedWeapon != null) p.equippedWeapon.enchantment = "Sharp";
        String o = run1("profession");
        check("P Innovative with 100 inspiration invents a technique (" + o + ")", o.contains("set down") && p.inspiration == 0);
        if (p.equippedWeapon != null) p.equippedWeapon.enchantment = "Sharp"; // the weapon is enchanted: only the missing inspiration is left to explain
        o = run1("profession");
        check("P Innovative without inspiration says how to get it (" + o + ")", o.contains("Inspiration"));
        // ---- every other button
        p.trait = Character.Traits.Alchemist;
        String[] cmds = {"meditate", "hunt", "forage", "mine", "craft", "craft menu", "craft weapon", "craft pill", "craft clothing", "craft array", "craft food", "craft wine", "craft poison",
                "wait 1", "sell", "sell goods", "buygear", "consume", "eat", "drink", "equip", "make food", "make wine", "make poison", "play", "perform", "tame", "learn 1", "library",
                "found city", "found sect", "join", "recruit", "war", "peace", "alliance", "mission", "invite 1", "feed 1", "promote 1", "attack 1", "talk 1", "spar 1", "follow 1", "preview 1",
                "install", "leave", "coat", "time", "duties all on", "duties all off", "policy", "script list", "script run", "script stop", "script clear", "seek x", "price 1 buy 1",
                "do Sell Technique", "do Sell Stock", "do Restock Trip", "do Register Tournament", "do Hold Tournament", "do Entice Merchant", "do Donate to Sect", "do Buy from Sect",
                "do Buy Sect Array", "do Buy City Array", "do Accept Sect Mission", "qdo", "goquest", "watch", "stay", "stats", "bag", "here", "people", "shops", "city", "sect"};
        ArrayList<String> silent = new ArrayList<>();
        for (String c : cmds) {
            o = run1(c);
            if (o.isEmpty()) silent.add(c);
        }
        PcProbe.real.println("[T] SILENT commands (printed nothing): " + silent);
        // ---- a second pass with something to act on: a wild tile with beasts, herbs and ore, and a pack with pills, food, wine, an array and spare gear
        Area wild = null;
        for (int k = 3; k < 14 && wild == null; k++) for (int[] d : new int[][]{{k, 3}, {3, k}, {-k, 3}}) {
            int x = Math.floorMod(p.location.x + d[0], Map.range), y = Math.floorMod(p.location.y + d[1], Map.range);
            if (Map.tiles[x][y] == null) synchronized (Map.tileLock) { if (Map.tiles[x][y] == null) Map.tiles[x][y] = new Area(Area.ghost(x == 0 ? Map.range - 1 : x - 1, y), 'e'); }
            Area a2 = Map.tiles[x][y];
            if (a2 != null && !a2.city && a2.sect == null && a2.dungeonType == null && a2.loreSite == null) { wild = a2; break; }
        }
        Area city = p.location;
        p.location.removeChar(p); p.location = wild; wild.addChar(p);
        wild.monsters = 400; wild.herbMaterials = 50000; wild.metalMaterials = 50000; wild.lvl = 8; wild.highestLvl = 10;
        p.lvl = 90; p.majorLvl = 9;
        p.pillBag.add(new Item(Item.ItemType.Pill, Item.PillType.Cultivation, 10, 2)); p.pillBag.add(new Item(Item.ItemType.Pill, Item.PillType.Potential, 11, 2));
        Life.goods(p).add(new Item(Item.ItemType.Food, 3, 3, "Plain Noodles")); Life.goods(p).add(new Item(Item.ItemType.Wine, 3, 3, "Plain Wine"));
        p.arrayBag.add(new Item(p, 5, Item.ItemType.Array));
        p.backpack.add(new Item(p, Item.WeaponType.Sword, 30)); p.backpack.add(new Item(p, 30, Item.ItemType.Clothing));
        p.techniqueList.add(new Technique(Technique.techniqueType.Martial, p, 70));
        String[] cmds2 = {"hunt", "forage", "mine", "eat", "drink", "consume", "equip", "install", "sell", "sell goods", "craft weapon", "craft food", "make wine", "meditate", "bag", "techniques", "equip 1", "gift 1 1", "feed 1", "release 1", "tame", "play", "learn 1", "library", "buygear", "stay"};
        ArrayList<String> silent2 = new ArrayList<>();
        for (String c : cmds2) { String o2 = run1(c); if (o2.isEmpty()) silent2.add(c); }
        // and in a city with a library, a shop and an auction house
        p.location.removeChar(p); p.location = city; city.addChar(p);
        String[] cmds3 = {"sell", "sell goods", "sell 1", "learn 1", "learn", "library", "buygear", "shops", "auction", "market", "sell technique", "do Sell Technique", "do Hold Tournament", "do Register Tournament", "found sect", "found city", "join", "sect", "city", "price 1 buy 0.5"};
        for (String c : cmds3) { String o3 = run1(c); if (o3.isEmpty()) silent2.add(c + " (city)"); }
        PcProbe.real.println("[T] SILENT commands, second pass: " + silent2);
        check("second pass: every button said something", silent2.isEmpty());
        check("no exceptions", !PcProbe.out().contains("Exception"));
    }
}
