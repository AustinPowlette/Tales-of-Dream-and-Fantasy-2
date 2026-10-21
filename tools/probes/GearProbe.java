package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-18 (headless): the scale of gear. Weapons, clothing and arrays run 1-100 (ten levels to a realm); the namers and the Items window used to read the level as a realm number, so a level 11 weapon was
 *  "Divine-Grade" and "True Immortal grade". Checks the realm of a level, the names and grades, the level of gear handed out (it was on the 1-10 scale), Heaven-Sent instruments (1-10) and `GearNames.fixAll`
 *  (the rename of the gear of an old world at load: grade and adjectives follow the level, type words and prefixes stay, nothing right is touched, a second run changes nothing). */
public class GearProbe {
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

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        check("player created and prompting", p != null && PcProbe.in.blocked);
        if (p == null) return;

        // ---- A: the realm of a gear level
        check("A1 levels 1-10 are realm 1, 11-20 realm 2, 91-100 realm 10, more stays 10", Naming.gearRealm(1) == 1 && Naming.gearRealm(10) == 1 && Naming.gearRealm(11) == 2 && Naming.gearRealm(20) == 2 && Naming.gearRealm(91) == 10 && Naming.gearRealm(100) == 10 && Naming.gearRealm(150) == 10);
        boolean inside = true;
        for (int r = 1; r <= 10; r++) for (int i = 0; i < 200; i++) { int l = Item.levelInRealm(r); if (Naming.gearRealm(l) != r) inside = false; }
        check("A2 Item.levelInRealm(r) always lands inside realm r", inside);

        // ---- B: names and grades follow the realm
        Item w15 = new Item(p, Item.WeaponType.Sword, 15), w25 = new Item(p, Item.WeaponType.Spear, 25), w55 = new Item(p, Item.WeaponType.Fan, 55), w95 = new Item(p, Item.WeaponType.Staff, 95);
        check("B1 a level 15 weapon is Mortal grade (" + w15.name + ")", w15.name.startsWith("Mortal-Grade "));
        check("B2 level 25 is Spirit (" + w25.name + "), 55 Earth (" + w55.name + "), 95 Divine (" + w95.name + ")", w25.name.startsWith("Spirit-Grade ") && w55.name.startsWith("Earth-Grade ") && w95.name.startsWith("Divine-Grade "));
        Item c35 = new Item(p, 35, Item.ItemType.Clothing), a75 = new Item(p, 75, Item.ItemType.Array);
        check("B3 clothing and arrays too (" + c35.name + " | " + a75.name + ")", c35.name.startsWith("Spirit-Grade ") && a75.name.startsWith("Heaven-Grade "));
        check("B4 the Items window grades by the realm: level 15 -> Foundation, level 95 -> Pseudo Immortal, a level 3 herb -> Core Formation",
                GuiItems.grade(GuiItems.realmOf(w15)).startsWith("Foundation") && GuiItems.grade(GuiItems.realmOf(w95)).startsWith("Pseudo Immortal") && GuiItems.grade(GuiItems.realmOf(new Item(Item.ItemType.Herb, 3, 5))).startsWith("Core Formation"));
        Item inst = new Item(p, 4, Item.ItemType.Instrument);
        check("B5 an instrument keeps its realm-number scale (realmOf " + GuiItems.realmOf(inst) + ")", GuiItems.realmOf(inst) == 4);

        // ---- C: gear handed out lands in its realm; Heaven-Sent instruments are 1-10
        p.backpack.clear(); p.equippedWeapon = null; p.equippedClothing = null;
        Starts.giveGear(p, 3);
        int wl = -1, cl = -1; ArrayList<Item> all = new ArrayList<>(p.backpack); all.add(p.equippedWeapon); all.add(p.equippedClothing); // (equip() moves the gear it likes out of the bag)
        for (Item it : all) { if (it == null) continue; if (it.itemType == Item.ItemType.Weapon) wl = it.lvl; if (it.itemType == Item.ItemType.Clothing) cl = it.lvl; }
        check("C1 Starts.giveGear(c, 3) gives gear of realm 3: levels 21-30 (" + wl + ", " + cl + ")", wl >= 21 && wl <= 30 && cl >= 21 && cl <= 30);
        boolean tOk = true; String tSample = "";
        for (int top = 0; top <= 9; top++) { Object o = Treasure.create(Treasure.Kind.INSTRUMENT, top, p); Item ti = (Item) o; tSample += ti.lvl + " "; if (ti.lvl < 1 || ti.lvl > 10) tOk = false; }
        check("C2 a Heaven-Sent instrument is level 1-10 (" + tSample.trim() + ")", tOk);
        Object tw = Treasure.create(Treasure.Kind.WEAPON, 2, p);
        check("C3 a Heaven-Sent weapon is on the 1-100 scale (" + ((Item) tw).lvl + ", realm 3-4 gear)", ((Item) tw).lvl >= 30 && ((Item) tw).name.startsWith("Heaven-Sent "));

        // ---- D: the rename of an old world
        Item old1 = new Item(p, Item.WeaponType.Sword, 25); old1.name = "Divine-Grade Void Sword";
        Item old2 = new Item(p, Item.WeaponType.Saber, 25); old2.name = "Heaven-Sent Divine-Grade Wrathful Saber"; old2.enchantment = "the Heavens' Blessing"; old2.enchantBonus = 5;
        Item old3 = new Item(p, 35, Item.ItemType.Clothing); old3.name = "Divine-Grade Celestial Robe";
        Item old4 = new Item(p, 15, Item.ItemType.Array); old4.name = "Divine-Grade Heaven-Sealing Pavilion Seal";
        Item old5 = new Item(p, Item.WeaponType.Spear, 12); old5.name = "Relic of the Footprint: Divine-Grade Primordial Spear";
        Item right = new Item(p, Item.WeaponType.Fan, 25); String rightName = right.name; // named by the new rule
        Item odd = new Item(p, Item.WeaponType.Staff, 25); odd.name = "Divine-Grade Staff of Kindness"; // a name with an unknown ending
        Item herbo = new Item(Item.ItemType.Herb, 9, 3); String herbName = herbo.name;
        Item big = new Item(p, Item.WeaponType.Staff, 98); big.name = "Divine-Grade Eternal Staff"; // already right
        p.backpack.addAll(Arrays.asList(old1, old2, old3, old4, old5, right, odd, herbo, big));
        Area where = p.location; Item onFloor = new Item(p, Item.WeaponType.Sword, 45); onFloor.name = "Divine-Grade Immortal Sword"; where.backpack.add(onFloor);
        int n = GearNames.fixAll();
        check("D1 fixAll renamed the old names (" + n + ")", n >= 6);
        check("D2 a level 25 weapon is Spirit grade now (" + old1.name + ") and still a Sword", old1.name.startsWith("Spirit-Grade ") && old1.name.endsWith(" Sword"));
        check("D3 the Heaven-Sent prefix stays (" + old2.name + ") and so does the enchantment", old2.name.startsWith("Heaven-Sent Spirit-Grade ") && old2.name.endsWith(" Saber") && old2.enchantBonus == 5);
        check("D4 clothing (" + old3.name + ") and a two-word array type (" + old4.name + ")", old3.name.startsWith("Spirit-Grade ") && old3.name.endsWith(" Robe") && old4.name.startsWith("Mortal-Grade ") && old4.name.endsWith(" Pavilion Seal"));
        check("D5 a relic keeps its prefix (" + old5.name + ")", old5.name.startsWith("Relic of the Footprint: Mortal-Grade ") && old5.name.endsWith(" Spear"));
        check("D6 gear lying in a tile is renamed too (" + onFloor.name + ")", onFloor.name.startsWith("Earth-Grade "));
        check("D7 what was right, unknown or not gear is left alone", right.name.equals(rightName) && odd.name.equals("Divine-Grade Staff of Kindness") && herbo.name.equals(herbName) && big.name.equals("Divine-Grade Eternal Staff"));
        String snap = old1.name + old2.name + old3.name + old4.name + old5.name + onFloor.name;
        int n2 = GearNames.fixAll();
        check("D8 a second run changes nothing (" + n2 + ")", n2 == 0 && snap.equals(old1.name + old2.name + old3.name + old4.name + old5.name + onFloor.name));
        check("D9 no exceptions in the output", !PcProbe.out().contains("Exception"));
        PcProbe.real.println("[T] DONE");
    }
}
