package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-16, change set E (beta tester's list): the Monsters (`Monsters`). A Monster is never stronger than the twentieth-strongest cultivator; one beaten by an NPC cultivator dies only when the fight was lethal or
 *  on 3 in 10 (it used to ALWAYS die: "road kill"); a person who plays still kills; it is wary; it hunts a ground near it (beasts three times as likely as cultivators; never a city, a sect or an array) and fights there;
 *  the strong ones stand on the boards of level, kills and fighting level (not the richest) and are spared by the epoch. */
public class MonsterProbe {
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

    static Character person(Area where, int level) {
        Character c = new Character(where);
        c.age = 30; c.gender = "Male"; c.maxAge = 100000; c.born = true; c.trait = Character.Traits.Friendly; c.targetQuests.clear(); c.lvl = level; c.majorLvl = Math.max(0, (level - 1) / 10); c.minorLvl = Math.max(1, level - 10 * c.majorLvl);
        c.caution = 0; c.affiliation = "Good"; c.affiliationNum = 1; c.name = "Cult"; c.lastName = "N" + c.id;
        Map.characters.add(c); Map.population++;
        return c;
    }

    static Character monster(Area where, int level) {
        Character c = person(where, level);
        c.trait = Character.Traits.Monster; c.affiliation = "Evil"; c.affiliationNum = -1; c.name = "Dread Wolf"; c.lastName = "M" + c.id; c.masterId = -1;
        return c;
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        check("player created and prompting", p != null && PcProbe.in.blocked);
        if (p == null) return;
        Settings.lore = false;
        p.pc().breakthroughPolicy = PlayerControl.BT_AUTO;
        Area lab = at(6, 6); lab.monsters = 0; lab.city = false; lab.sect = null; lab.lvl = 5;
        p.location.removeChar(p); p.location = lab; lab.addChar(p);

        // ---- M1: the cap is the level of the twentieth-strongest cultivator
        List<Integer> lv = new ArrayList<>();
        for (Character c : Map.characters) if (Rankings.eligible(c) && c.age >= 8) lv.add(c.lvl);
        Collections.sort(lv, Collections.reverseOrder());
        int expect = lv.size() >= 20 ? Math.max(10, lv.get(19)) : lv.isEmpty() ? 10 : Math.max(10, lv.get(lv.size() - 1));
        Monsters.refreshCap(Map.characters);
        check("M1 the cap is the level of the 20th strongest of " + lv.size() + " cultivators (" + Monsters.cap() + ", expected " + expect + ")", Monsters.cap() == expect);

        // ---- M2: a stronger Monster is brought down
        for (int i = 0; i < 25; i++) person(lab, 60); // (so the cap is a known number: 20 cultivators at level 60)
        Monsters.refreshCap(Map.characters);
        int cap = Monsters.cap();
        check("M2 with twenty cultivators at level 60 the cap is at least 60 (" + cap + ")", cap >= 60);
        Character big = monster(lab, cap + 25);
        Monsters.clamp(big);
        check("M2b a Monster above the cap comes down to it (level " + big.lvl + ", cap " + cap + "), the realm follows (major " + big.majorLvl + ", minor " + big.minorLvl + ")",
                big.lvl == cap && big.lvl == 10 * big.majorLvl + big.minorLvl && big.cultivationBase < big.cultivationBaseMax);
        Character weak = monster(lab, 15);
        int before = weak.lvl; Monsters.clamp(weak);
        check("M2c a weaker one is not touched (" + before + " -> " + weak.lvl + ")", weak.lvl == before);

        // ---- M3: it stops growing at the cap
        Character topM = monster(lab, cap); topM.potential = 100; topM.cultivationBaseMax = 10; topM.cultivationBase = 9; topM.cultivation = 100000;
        for (int i = 0; i < 50; i++) topM.cultivate();
        check("M3 a Monster at the cap does not level up however much it cultivates (" + topM.lvl + " of cap " + cap + ")", topM.lvl == cap);
        Character grow = monster(lab, cap - 5); grow.potential = 100; grow.cultivationBaseMax = 10; grow.cultivationBase = 9; grow.cultivation = 100000;
        for (int i = 0; i < 50; i++) grow.cultivate();
        check("M3b below the cap it grows and stops there (" + grow.lvl + ")", grow.lvl > cap - 5 && grow.lvl <= cap);

        // ---- M4: a beaten Monster
        Character cult = person(lab, 80);
        int died = 0, n = 600;
        for (int i = 0; i < n; i++) { Character m = monster(lab, 40); cult.winCheck(m, false); if (m.dead) died++; }
        double rate = 100.0 * died / n;
        check("M4 a Monster beaten by an NPC cultivator in a fight that was not lethal dies about 18 in 100 (" + String.format("%.0f", rate) + "% of " + n + ")", rate > 11 && rate < 26);
        died = 0;
        for (int i = 0; i < 100; i++) { Character m = monster(lab, 40); cult.winCheck(m, true); if (m.dead) died++; }
        check("M4b ...and the same when the fight was lethal: the enmity of the two does not hunt a Monster down (" + died + " of 100)", died > 5 && died < 40);
        Character hurtOne = monster(lab, 40); hurtOne.cultivation = 1000;
        for (int i = 0; i < 20 && !hurtOne.dead; i++) cult.winCheck(hurtOne, false);
        check("M4c a Monster that lived is hurt (cultivation down 30%) or died in the end", hurtOne.dead || hurtOne.cultivation < 1000);
        p.lvl = 120; p.majorLvl = 11; died = 0;
        for (int i = 0; i < 100; i++) { Character m = monster(lab, 40); p.winCheck(m, false); if (m.dead) died++; }
        check("M4d a person who plays still kills the Monster he beats (" + died + " of 100)", died == 100);
        Character beast = person(lab, 40); beast.trait = Character.Traits.Beast; beast.affiliation = "Neutral";
        Character winner2 = person(lab, 80); died = 0;
        for (int i = 0; i < 50; i++) { Character b = person(lab, 40); b.trait = Character.Traits.Beast; winner2.winCheck(b, false); if (b.dead) died++; }
        check("M4e a plain beast beaten by a cultivator still dies as before (" + died + " of 50)", died == 50);

        // ---- M5: waking
        Character cub = person(lab, cap + 30); cub.trait = Character.Traits.Beast; cub.masterId = -1; cub.maxAge = 1000;
        cub.awakenMonster();
        check("M5 a beast that wakes as a Monster is wary (caution " + cub.caution + ") and not above the cap (level " + cub.lvl + " of " + cap + ")", cub.trait == Character.Traits.Monster && cub.caution == Monsters.CAUTION && cub.lvl <= cap);

        // ---- M6: the hunting ground
        Character hunter = monster(lab, 50);
        for (int dx = -Monsters.RADIUS; dx <= Monsters.RADIUS; dx++) for (int dy = -Monsters.RADIUS; dy <= Monsters.RADIUS; dy++) { Area a = at(6 + dx, 6 + dy); if (a != lab) { a.monsters = 0; a.city = false; a.sect = null; if (a.arrayBag != null) a.arrayBag.clear(); for (Character c : new ArrayList<>(a.characters)) if (c != null && c != p) { /* leave them */ } } }
        Area beastGround = at(8, 6), peopleGround = at(4, 6), cityGround = at(6, 9), arrayGround = at(9, 6);
        for (int dx = -Monsters.RADIUS; dx <= Monsters.RADIUS; dx++) for (int dy = -Monsters.RADIUS; dy <= Monsters.RADIUS; dy++) { Area a = at(6 + dx, 6 + dy); if (a == lab) continue; for (Character c : new ArrayList<>(a.characters)) { if (c != null && c != p && !c.dead) a.removeChar(c); } }
        beastGround.monsters = 30; beastGround.lvl = 5;
        Character weakPrey = person(peopleGround, 20); peopleGround.addChar(weakPrey);
        cityGround.monsters = 30; cityGround.city = true;
        arrayGround.monsters = 30; if (arrayGround.arrayBag == null) arrayGround.arrayBag = new SafeList<>(); arrayGround.arrayBag.add(new Item(Item.ItemType.Array, 5, 1));
        int bg = 0, pg = 0, other = 0, samples = 1200;
        for (int i = 0; i < samples; i++) { Area g = Monsters.huntGround(hunter); if (g == beastGround) bg++; else if (g == peopleGround) pg++; else other++; }
        PcProbe.real.println("[T] info: hunting grounds chosen: beasts " + bg + ", people " + pg + ", other " + other);
        check("M6b it never picks a city or a tile with an array (other grounds: " + other + ")", other == 0);
        check("M6c beast ground is chosen about three times as often as the ground of a weaker cultivator (" + bg + " : " + pg + ")", pg > 0 && bg > pg * 2 && bg < pg * 4.5);

        // ---- M7: at the ground
        hunter.location.removeChar(hunter); hunter.location = beastGround; beastGround.addChar(hunter); hunter.lvl = 50; hunter.majorLvl = 4; hunter.minorLvl = 10; hunter.caution = 0;
        int monsters0 = beastGround.monsters;
        for (int i = 0; i < 5; i++) Monsters.huntAt(hunter);
        check("M7 a Monster hunting where there are beasts fights them (beasts " + monsters0 + " -> " + beastGround.monsters + ")", beastGround.monsters < monsters0);

        // ---- M8: the boards
        Character legend = monster(lab, cap - 1); legend.kills = 500; legend.rankStrong = 0;
        List<Character> world = new ArrayList<>(Map.characters);
        List<Character> top0 = Rankings.top(world, 0, 30), top1 = Rankings.top(world, 1, 100), top2 = Rankings.top(world, 2, 30), top3 = Rankings.top(world, 3, 30);
        legend.ss = 99999999;
        top1 = Rankings.top(world, 1, 100);
        check("M8 a Monster stands on the boards of kills (" + top2.contains(legend) + ") and of level or fighting level when strong enough, never on the Richest (" + top1.contains(legend) + ")", top2.contains(legend) && !top1.contains(legend));
        check("M8b a Monster's title is of the wilds (" + Rankings.titleOf(legend, 2, 1) + ", " + Rankings.titleOf(legend, 2, 5) + ", " + Rankings.titleOf(legend, 2, 50) + ")", Rankings.titleOf(legend, 2, 1).contains("Wilds") && Rankings.titleOf(legend, 2, 5).contains("Wilds") && Rankings.titleOf(legend, 2, 50).contains("Wilds"));
        Rankings.yearly(world);
        check("M8c the yearly count gives it a place and the epoch spares it (kills place " + legend.rankKills + ", epochSafe " + Rankings.epochSafe(legend) + ")", legend.rankKills > 0 && Rankings.epochSafe(legend));
        String text = Rankings.text("kills", p);
        check("M8d the board text names it a Monster", text.contains("(Monster)"));
        Character tamed = monster(lab, 30); tamed.masterId = 5; tamed.kills = 9999;
        check("M8e a tamed beast is not on a board", !Rankings.eligible(tamed, 2));
        System.out.println();
        PcProbe.real.println("[T] DONE");
    }
}
