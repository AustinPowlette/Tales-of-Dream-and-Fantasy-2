package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-16, change set C (beta tester's list): the five elements (`Elements`). Every creature has a stable path of one element with a name; every technique has an element and says so in its name; the two cycles are
 *  the ones the author gave (Wood parts Earth, Earth absorbs Water, Water quenches Fire, Fire melts Metal, Metal chops Wood; Wood feeds Fire, Fire makes Earth, Earth bears Metal, Metal holds Water, Water nourishes
 *  Wood); the odds of a fight move by the table in `Elements`; the fight preview and the report say so. */
public class ElementsProbe {
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

    static Character fighter(Area where, int element) {
        Character c = new Character(where);
        c.age = 30; c.gender = "Male"; c.maxAge = 100000; c.born = true; c.trait = Character.Traits.Friendly; c.targetQuests.clear(); c.lvl = 100; c.majorLvl = 5; c.caution = 0;
        c.affiliation = "Good"; c.affiliationNum = 1;
        c.equippedWeapon = null; c.equippedClothing = null;
        c.martialTechnique = new Technique(Technique.techniqueType.Martial); c.movementTechnique = new Technique(Technique.techniqueType.Movement);
        c.techniqueList.clear();
        Elements.assign(c, element, new Random(c.id));
        Map.characters.add(c); Map.population++;
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
        Area lab = at(6, 6); lab.monsters = 0;
        p.location.removeChar(p); p.location = lab; lab.addChar(p);

        // ---- E1: paths
        int[] count = new int[5]; int named = 0, bad = 0, n = 0;
        Set<String> names = new HashSet<>();
        for (Character c : Map.characters) {
            if (c == null) continue;
            n++;
            int code = Elements.code(c), e = Elements.of(c);
            if (code < 1 || code > 980 || e < 0 || e > 4) { bad++; continue; }
            count[e]++;
            String nm = Elements.pathName(c);
            if (nm.matches("(Path|Way|Dao|Art) of the [A-Za-z-]+ [A-Za-z]+")) named++;
            names.add(nm);
        }
        check("E1 all " + n + " creatures have a path in range (" + bad + " bad) with a name of the form 'Way of the Silent Mountain' (" + named + " named, " + names.size() + " different names)", bad == 0 && named == n && names.size() >= Math.min(n / 4, 100));
        int lo = Integer.MAX_VALUE, hi = 0; for (int x : count) { lo = Math.min(lo, x); hi = Math.max(hi, x); }
        check("E1b the five elements are about equally common " + Arrays.toString(count), n < 100 || (lo >= n / 5 * 0.7 && hi <= n / 5 * 1.3));
        Character c0 = Map.characters.get(0) != null ? Map.characters.get(0) : p;
        short before = (short) Elements.code(c0); c0.path = 0; short after = (short) Elements.code(c0);
        check("E1c a creature without a path (an old save) gets the same one from its id every time (" + before + " = " + after + ")", before == after);

        // ---- E2: the cycles as the author gave them
        boolean ke = Elements.overcomes(Elements.WOOD, Elements.EARTH) && Elements.overcomes(Elements.EARTH, Elements.WATER) && Elements.overcomes(Elements.WATER, Elements.FIRE)
                && Elements.overcomes(Elements.FIRE, Elements.METAL) && Elements.overcomes(Elements.METAL, Elements.WOOD);
        boolean sheng = Elements.feeds(Elements.WOOD, Elements.FIRE) && Elements.feeds(Elements.FIRE, Elements.EARTH) && Elements.feeds(Elements.EARTH, Elements.METAL)
                && Elements.feeds(Elements.METAL, Elements.WATER) && Elements.feeds(Elements.WATER, Elements.WOOD);
        check("E2 the overcoming cycle: Wood parts Earth, Earth absorbs Water, Water quenches Fire, Fire melts Metal, Metal chops Wood", ke);
        check("E2b the generating cycle: Wood feeds Fire, Fire makes Earth, Earth bears Metal, Metal holds Water, Water nourishes Wood", sheng);
        boolean one = true;
        for (int e = 0; e < 5; e++) { int beats = 0, loses = 0, feeds = 0, fed = 0; for (int f = 0; f < 5; f++) { if (Elements.overcomes(e, f)) beats++; if (Elements.overcomes(f, e)) loses++; if (Elements.feeds(e, f)) feeds++; if (Elements.feeds(f, e)) fed++; } if (beats != 1 || loses != 1 || feeds != 1 || fed != 1) one = false; }
        check("E2c each element overcomes one, is overcome by one, feeds one and is fed by one", one);
        check("E2d the words of the cycle: " + Elements.relation(Elements.FIRE, Elements.METAL) + " / " + Elements.relation(Elements.METAL, Elements.FIRE) + " / " + Elements.relation(Elements.WATER, Elements.WOOD),
                Elements.relation(Elements.FIRE, Elements.METAL).equals("Fire melts Metal") && Elements.relation(Elements.METAL, Elements.FIRE).equals("Fire melts Metal") && Elements.relation(Elements.WATER, Elements.WOOD).equals("Water nourishes Wood") && Elements.relation(Elements.EARTH, Elements.EARTH).isEmpty());

        // ---- E3: the table of the odds
        check("E3 a technique against a path: overcomes +8, is overcome -8, drains +3, feeds -3, same 0",
                Elements.against(Elements.FIRE, Elements.METAL) == 8 && Elements.against(Elements.METAL, Elements.FIRE) == -8 && Elements.against(Elements.EARTH, Elements.FIRE) == 3 && Elements.against(Elements.FIRE, Elements.EARTH) == -3 && Elements.against(Elements.WOOD, Elements.WOOD) == 0);
        check("E3b a technique in its owner's hands: same +5, feeds the path +3, the path feeds it +2, holds the path back -6, the path holds it back -4",
                Elements.suits(Elements.FIRE, Elements.FIRE) == 5 && Elements.suits(Elements.WOOD, Elements.FIRE) == 3 && Elements.suits(Elements.FIRE, Elements.WOOD) == 2 && Elements.suits(Elements.WOOD, Elements.EARTH) == -6 && Elements.suits(Elements.EARTH, Elements.WOOD) == -4);

        // ---- E4: the swing between two fighters
        Character fire = fighter(lab, Elements.FIRE), metal = fighter(lab, Elements.METAL), fire2 = fighter(lab, Elements.FIRE), wood = fighter(lab, Elements.WOOD), earth = fighter(lab, Elements.EARTH);
        check("E4 two fighters with default techniques: Fire against Metal swings +16 (+8 and -(-8)), Metal against Fire -16, the same element 0 (" + Elements.swing(fire, metal) + ", " + Elements.swing(metal, fire) + ", " + Elements.swing(fire, fire2) + ")",
                Elements.swing(fire, metal) == 16 && Elements.swing(metal, fire) == -16 && Elements.swing(fire, fire2) == 0);
        check("E4b feeding is a small push: Wood against Fire (Wood feeds Fire) " + Elements.swing(wood, fire), Elements.swing(wood, fire) == -6 && Elements.swing(fire, wood) == 6);
        // a technique of another element changes it
        Technique oddTech = new Technique(Technique.techniqueType.Martial, fire, 5);
        oddTech.elemPlus1 = (byte) (Elements.WATER + 1); // a Water technique in the hands of a Fire fighter: it quenches the Fire path (-6) and is attuned to nobody
        fire.martialTechnique = oddTech;
        double s1 = Elements.edgeValue(fire, metal);
        check("E4c a Water technique in a Fire fighter's hands against a Metal fighter: it drains Metal (+3) and holds Fire back (-6) = -3 for two thirds, the default movement technique (Fire against Metal +8, attuned +5) adds 13 for one third: edge " + String.format("%.2f", s1), Math.abs(s1 - (2 * -3 + 13) / 3.0) < 1e-9);
        fire.martialTechnique = new Technique(Technique.techniqueType.Martial);
        // the preview and the real fight
        Character.FightPreview fp = fire.previewFight(metal, false);
        check("E5 the fight preview says the same: equal power, Fire against Metal: win chance 66 (" + fp.winChance + "), swing " + fp.elementSwing + ", and names the elements",
                fp.winChance == 66 && fp.elementSwing == 16 && fp.detail().contains("Elements:") && fp.oneLine().contains("elements +16"));
        // the fight itself: friends spar-like (never lethal), 3000 times
        fire.changeRelationship(metal, 3); metal.changeRelationship(fire, 3);
        int wins = 0, losses = 0, rounds = 1500;
        for (int i = 0; i < rounds; i++) {
            fire.dead = false; metal.dead = false; fire.location = lab; metal.location = lab;
            fire.lostAt = -1; metal.lostAt = -1; fire.lostToId = -1; metal.lostToId = -1;
            fire.battle(metal);
            if (metal.lostAt == GameDriver.time && metal.lostToId == fire.id) wins++;
            else if (fire.lostAt == GameDriver.time && fire.lostToId == metal.id) losses++;
        }
        double rate = 100.0 * wins / Math.max(1, wins + losses);
        PcProbe.real.println("[T] info: Fire won " + wins + " and lost " + losses + " of " + rounds + " fights against equal Metal (" + String.format("%.1f", rate) + "%)");
        check("E5b ...and in 1500 fights Fire wins about two in three of those that were fought to the end (" + String.format("%.1f", rate) + "%)", wins + losses > 600 && rate > 58 && rate < 74);

        // ---- E6: techniques
        int match = 0, tn = 600, namesOk = 0;
        Character maker = fighter(lab, Elements.EARTH);
        for (int i = 0; i < tn; i++) {
            Technique t = new Technique(i % 2 == 0 ? Technique.techniqueType.Martial : Technique.techniqueType.Movement, maker, 5);
            int e = t.element();
            if (e == Elements.EARTH) match++;
            boolean has = false; for (String w : Elements.TECH_WORDS[e]) if (t.name.contains(w)) has = true;
            if (has && e >= 0 && e <= 4) namesOk++;
        }
        double share = 100.0 * match / tn;
        check("E6 a new technique takes its maker's element about 64% of the time (" + String.format("%.0f", share) + "%) and its name always carries a word of its element (" + namesOk + " of " + tn + ")", share > 54 && share < 76 && namesOk == tn);
        Technique def = new Technique(Technique.techniqueType.Martial);
        check("E6b the Default Technique has no element of its own and follows its owner's path", def.element() == -1 && Elements.techElem(def, fire) == Elements.FIRE && Elements.techElem(def, metal) == Elements.METAL);
        Technique cp = new Technique(new Technique(Technique.techniqueType.Martial, maker, 5));
        check("E6c a copy keeps the element", cp.element() >= 0);
        Technique old = new Technique(Technique.techniqueType.Martial); old.name = "Frost Crane Palm"; old.elemPlus1 = 0;
        int oe = old.element(); old.elemPlus1 = 0;
        check("E6d a technique of an old save gets a stable element from its name (" + oe + " = " + old.element() + ")", oe >= 0 && oe == old.element());
        check("E6e the technique text shows the element (" + cp + ")", cp.toString().contains("[" + Elements.name(cp.element()) + "]"));

        // ---- E7: children
        int same = 0, cn = 400;
        for (int i = 0; i < cn; i++) {
            Character kid = new Character(lab);
            Elements.inherit(kid, earth, fire);
            int e = Elements.of(kid);
            if (e == Elements.EARTH || e == Elements.FIRE) same++;
        }
        double ks = 100.0 * same / cn;
        check("E7 a child takes a parent's element about 60% of the time + chance (" + String.format("%.0f", ks) + "% of two parents' elements; chance alone would be 40%)", ks > 52 && ks < 86);

        // ---- E8: where the player reads it
        PcProbe.cmd("stats", 30);
        check("E8 the Stats text names the path", PcProbe.out().contains("Path: ") && PcProbe.out().contains("(" + Elements.name(Elements.of(p)) + ")"));
        Character foe = fighter(lab, (Elements.of(p) + 2) % 5);
        foe.lvl = p.lvl; foe.changeRelationship(p, 3); p.changeRelationship(foe, 3);
        int m0 = PcProbe.mark();
        p.battle(foe);
        String rep = FightReport.last == null ? "" : FightReport.last;
        check("E8b the fight report names the elements (" + (rep.contains("Elements:") ? "yes" : "no") + ")", rep.contains("Elements:"));

        // ---- E9: a fighter uses the technique that is worth more against this opponent
        Character xf = fighter(lab, Elements.FIRE), mf = fighter(lab, Elements.METAL);
        Technique t1 = new Technique(Technique.techniqueType.Martial, xf, 5), t2 = new Technique(Technique.techniqueType.Martial, xf, 5);
        t1.elemPlus1 = (byte) (Elements.WATER + 1); t1.lvl = 10; t1.potential = 10;   // Water in the hands of a Fire fighter: it holds his path back (-6) and drains Metal (+3)
        t2.elemPlus1 = (byte) (Elements.FIRE + 1); t2.lvl = 9; t2.potential = 20;     // Fire: attuned (+5) and it melts Metal (+8), one level lower
        xf.techniqueList.clear(); xf.techniqueList.add(t1); xf.techniqueList.add(t2); xf.martialTechnique = t1; xf.martialTechnique2 = null;
        xf.tempLvlCheck(); mf.tempLvlCheck();
        boolean laid = xf.martialTechnique == t1 && xf.martialTechnique2 == t2;
        int t0 = xf.tempLvl;
        int swaps0 = Elements.chosenSwaps;
        Elements.chooseTechniques(xf, mf);
        check("E9 against Metal a Fire fighter swaps his higher-level Water technique for the Fire one that melts Metal (laid out " + laid + "; now " + xf.martialTechnique.name + ")", laid && xf.martialTechnique == t2 && xf.martialTechnique2 == t1 && Elements.chosenSwaps == swaps0 + 1);
        int expect = t0 - (int) ((100 + 10 + xf.movementTechnique.lvl) / 5) + (int) ((100 + 9 + xf.movementTechnique.lvl) / 5);
        check("E9b the fighting level follows the swap (" + t0 + " -> " + xf.tempLvl + ", expected " + expect + ")", xf.tempLvl == expect);
        xf.tempLvlCheck(); // (the next equip puts the stronger one back)
        t2.lvl = 2; xf.tempLvlCheck();
        Elements.chooseTechniques(xf, mf);
        check("E9c a technique that gives up too many levels is not chosen for its element (" + t2.lvl + " levels against " + t1.lvl + ")", xf.martialTechnique == t1);
        Character hand = p; hand.martialTechnique2 = t2; Technique keep = hand.martialTechnique;
        Elements.chooseTechniques(hand, mf);
        check("E9d a person who plays by hand is never swapped", hand.martialTechnique == keep);
        System.out.println();
        PcProbe.real.println("[T] DONE");
    }
}
