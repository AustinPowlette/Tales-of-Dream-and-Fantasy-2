package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-15: the checks of test plan T12 (6.17) that no other probe makes: two people who play keep their own priority rules; the yearly count of the four boards costs little per cultivator; the turn cost of the new
 *  verbs (the table of 6.7); the menu's promised item levels hold over hundreds of crafts; a held breakthrough and the new fields are saved with the character (not transient). */
public class T12Probe {
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
        p.lvl = 100; p.majorLvl = 10; p.maxAge = 100000; p.caution = 0; p.targetQuests.clear(); p.spotQuests.clear(); p.currentQuest = null; p.professionLvl = 60;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        p.pc().breakthroughPolicy = PlayerControl.BT_AUTO;
        Area lab = at(6, 6); lab.monsters = 0;
        p.location.removeChar(p); p.location = lab; lab.addChar(p);

        // ---- T1: two people who play keep their own rules
        Character second = new Character(lab);
        second.age = 30; second.gender = "Male"; second.maxAge = 100000; second.trait = Character.Traits.Friendly; second.player = 1; second.pc = new PlayerControl();
        second.targetQuests.clear();
        Quest q1 = new Quest("Find Person", false, 14, 2, 40, second), q2 = new Quest("Find Person", false, 14, 2, 40, p);
        p.targetQuests.add(q1); second.targetQuests.add(q2);
        p.pc().setRule("Find Person|", 90);
        p.applyPriorityRules(); second.applyPriorityRules();
        check("T1 a rule of one person who plays does not touch another's quests (" + q1.priority + " and " + q2.priority + ")", q1.priority == 90 && q2.priority == 14 && second.pc.prioRules == null);
        second.pc.setRule("Find Person|", 20);
        second.applyPriorityRules(); p.applyPriorityRules();
        check("T1b ...and each keeps its own number (" + q1.priority + ", " + q2.priority + ")", q1.priority == 90 && q2.priority == 20);
        p.pc().setRule("Find Person|", null); p.applyPriorityRules();
        check("T1c clearing one leaves the other's (" + q1.priority + ", " + q2.priority + ")", q1.priority == 14 && q2.priority == 20);
        p.targetQuests.clear();

        // ---- T2: the yearly count of the four boards is cheap
        long t0 = System.nanoTime();
        int rounds = 5;
        for (int i = 0; i < rounds; i++) Rankings.yearly(Map.characters);
        double msPer = (System.nanoTime() - t0) / 1e6 / rounds;
        int living = 0; for (Character c : Map.characters) if (c != null && !c.dead) living++;
        double per100k = msPer * 100000.0 / Math.max(1, living);
        check("T2 the yearly count of the four boards takes " + String.format("%.1f", msPer) + " ms for " + living + " people: about " + String.format("%.0f", per100k) + " ms at 100,000", per100k < 1500);

        // ---- T3: the turn cost of the new verbs
        int t3 = GameDriver.time;
        String[] free = {"qprio", "qprio Forage 50", "qprio reset", "qadd Meditate p=40", "askgift Nobodyatall", "craft list", "breakthrough", "policy breakthrough ask", "policy breakthrough auto", "spotaction talk", "spotaction m", "music concerts", "music concert Market", "music off", "ranking", "ranking fighting", "note tile 1 1 x", "unnote tile 1 1"};
        StringBuilder spent = new StringBuilder();
        for (String cmd : free) {
            int before = GameDriver.time;
            PcProbe.cmd(cmd, 60);
            if (GameDriver.time != before) spent.append(cmd).append(" (+").append(GameDriver.time - before).append("); ");
        }
        check("T3 these verbs spend no week (" + free.length + " commands): " + (spent.length() == 0 ? "none did" : spent), spent.length() == 0 && GameDriver.time == t3);
        p.trait = Character.Traits.Blacksmith; p.materialBag.clear(); p.materialBag.add(new Item(Item.ItemType.Metal, 5, 600));
        int before = GameDriver.time;
        PcProbe.cmd("craft do 1 sword", 120);
        check("T3b 'craft do' spends one week (" + before + " -> " + GameDriver.time + ")", GameDriver.time == before + 1);

        // ---- T4: the menu's promises hold over many crafts
        p.professionLvl = 50; p.majorLvl = 10;
        Item big = new Item(Item.ItemType.Metal, 7, 200000);
        p.materialBag.clear(); p.materialBag.add(big);
        CraftMenu.Option o = CraftMenu.options(p).get(0);
        int lo = Integer.MAX_VALUE, hi = 0, bad = 0, n = 300;
        for (int i = 0; i < n; i++) {
            int before2 = p.backpack.size();
            p.smithing(null, big);
            if (p.backpack.size() == before2) { bad++; continue; }
            Item w = p.backpack.get(p.backpack.size() - 1);
            lo = Math.min(lo, w.lvl); hi = Math.max(hi, w.lvl);
            if (w.lvl < o.minLvl || w.lvl > o.maxLvl) bad++;
        }
        check("T4 300 weapons from quality-7 metal at skill 50: all within the promised Lv " + o.minLvl + "-" + o.maxLvl + " (seen " + lo + "-" + hi + ", out of range " + bad + ")", bad == 0 && lo <= o.minLvl + 5 && hi >= o.maxLvl - 5);
        p.professionLvl = 10;
        CraftMenu.Option low = CraftMenu.options(p).get(0);
        check("T4b a low skill uses the material at the level it allows (skill 10: quality " + (p.professionLvl / 10 + 2) + " -> Lv " + low.minLvl + "-" + low.maxLvl + ")", low.maxLvl == (p.professionLvl / 10 + 2) * 10 && low.minLvl == (p.professionLvl / 10 + 2) * 5 + 1);
        p.trait = Character.Traits.Alchemist; p.professionLvl = 40; p.materialBag.clear();
        for (int q = 1; q <= 6; q++) p.materialBag.add(new Item(Item.ItemType.Herb, q, 100));
        boolean pillsOk = true;
        for (CraftMenu.Option po : CraftMenu.options(p)) pillsOk &= po.kind.equals("pill") && po.minLvl == po.stack.lvl && po.maxLvl == po.stack.lvl && po.upTo == Math.min(po.stack.quantity / 50, p.professionLvl / 10 + 1);
        check("T4c a pill is exactly the quality of its herb and the count is min(stack / 50, skill / 10 + 1)", pillsOk && CraftMenu.options(p).size() == 6);

        // ---- T5: what is saved
        boolean saved = true; StringBuilder not = new StringBuilder();
        for (String f : new String[]{"breakthroughReady", "rankFight", "inhibitor"}) {
            try { if (java.lang.reflect.Modifier.isTransient(Character.class.getDeclaredField(f).getModifiers())) { saved = false; not.append(f).append(' '); } } catch (NoSuchFieldException e) { saved = false; not.append(f).append("(missing) "); }
        }
        for (String f : new String[]{"prioRules", "breakthroughPolicy", "nextGiftCheck", "giftsFrom"}) {
            try { if (java.lang.reflect.Modifier.isTransient(PlayerControl.class.getDeclaredField(f).getModifiers())) { saved = false; not.append(f).append(' '); } } catch (NoSuchFieldException e) { saved = false; not.append(f).append("(missing) "); }
        }
        for (String f : new String[]{"ruled", "origPriority", "origMax", "origDiff"}) {
            try { if (java.lang.reflect.Modifier.isTransient(Quest.class.getDeclaredField(f).getModifiers())) { saved = false; not.append(f).append(' '); } } catch (NoSuchFieldException e) { saved = false; not.append(f).append("(missing) "); }
        }
        check("T5 the held breakthrough, the fighting-board place, the priority rules, the policies, the gift clock and a ruled quest's old numbers are saved with the game (not transient: " + not + ")", saved);
        // a round trip through Java serialization of the small objects
        PlayerControl pc = new PlayerControl();
        pc.setRule("Forage|", 33); pc.breakthroughPolicy = PlayerControl.BT_BUTTON; pc.giftsFrom = new HashMap<>(); pc.giftsFrom.put(7, 120); pc.nextGiftCheck = 99;
        java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oo = new java.io.ObjectOutputStream(bo)) { oo.writeObject(pc.prioRules); oo.writeObject(pc.giftsFrom); }
        Object r1, r2;
        try (java.io.ObjectInputStream oi = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bo.toByteArray()))) { r1 = oi.readObject(); r2 = oi.readObject(); }
        check("T5b the rules and the gift clock survive a round trip (" + r1 + ", " + r2 + ")", r1 instanceof java.util.Map && ((java.util.Map<?, ?>) r1).get("Forage|").equals(33) && ((java.util.Map<?, ?>) r2).get(7).equals(120));
        System.out.println();
        PcProbe.real.println("[T] DONE");
    }
}
