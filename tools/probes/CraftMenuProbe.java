package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-14: the crafting menu of a trade (`CraftMenu`, `craft list`, `craft do <n> [kind|count]`): the level follows the chosen stack and the maker's skill and realm, a weapon can be a chosen kind, pills are
 *  counted, one craft is one turn, a trade without a menu refuses, and making Potential Pills (by the menu, by the game's own `alchemy()` or by refining another person's Blood) makes the maker Evil. */
public class CraftMenuProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    static void say(String s) { PcProbe.real.println("[T] " + s); }
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

    static Item stack(Item.ItemType type, int quality, int qty) {
        return type == Item.ItemType.Blood ? new Item(type, qty) : new Item(type, quality, qty);
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        check("player created and prompting", p != null && PcProbe.in.blocked);
        if (p == null) return;
        p.maxAge = 100000; p.caution = 0; p.targetQuests.clear(); p.spotQuests.clear(); p.currentQuest = null;
        p.lvl = 100; p.majorLvl = 10; p.professionLvl = 50; p.professionExp = 0; p.professionExpMax = Math.pow(10, 5);
        Settings.lore = false;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();

        // ---- C1: a smith, two metals
        p.trait = Character.Traits.Blacksmith;
        p.materialBag.clear(); p.backpack.clear();
        Item low = stack(Item.ItemType.Metal, 3, 1000), high = stack(Item.ItemType.Metal, 7, 600);
        p.materialBag.add(low); p.materialBag.add(high);
        List<CraftMenu.Option> o = CraftMenu.options(p);
        check("C1 two options, the better metal first (" + o.size() + ")", o.size() == 2 && o.get(0).stack == high && o.get(1).stack == low);
        check("C1b quality 7 at skill 50 makes Lv 36-70, quality 3 makes Lv 16-30 (" + o.get(0).makes() + ", " + o.get(1).makes() + ")", o.get(0).minLvl == 36 && o.get(0).maxLvl == 70 && o.get(1).minLvl == 16 && o.get(1).maxLvl == 30);
        check("C1c the stack allows 1 and 2 items (" + o.get(0).upTo + ", " + o.get(1).upTo + ")", o.get(0).upTo == 1 && o.get(1).upTo == 2);
        p.majorLvl = 2; // the realm caps the level: (2 + 1) * 10 = 30
        List<CraftMenu.Option> capped = CraftMenu.options(p);
        check("C1d the maker's realm caps what comes out (Major 2: Lv " + capped.get(0).makes() + ")", capped.get(0).maxLvl == 30 && capped.get(0).minLvl == 30);
        p.majorLvl = 10;
        PcProbe.cmd("craft list", 60);

        // ---- C2: a chosen stack and kind, one turn
        int t0 = GameDriver.time, crafted0 = p.itemsCrafted;
        PcProbe.cmd("craft do 2 sword", 120);
        Item made = p.backpack.isEmpty() ? null : p.backpack.get(p.backpack.size() - 1);
        check("C2 'craft do 2 sword' made a sword from the quality-3 stack (" + (made == null ? "nothing" : made.name + " Lv." + made.lvl) + ")", made != null && made.weaponType == Item.WeaponType.Sword && made.lvl >= 16 && made.lvl <= 30);
        check("C2b ...it cost 500 of that stack only (" + low.quantity + " and " + high.quantity + ") and one turn (" + t0 + " -> " + GameDriver.time + ")", low.quantity == 500 && high.quantity == 600 && GameDriver.time == t0 + 1);
        check("C2c ...and counted as crafted with profession experience (" + p.itemsCrafted + ", exp " + p.professionExp + ")", p.itemsCrafted == crafted0 + 1 && p.professionExp > 0);
        PcProbe.cmd("craft do 1 fan", 120);
        Item made2 = p.backpack.get(p.backpack.size() - 1);
        check("C2d 'craft do 1 fan' from the quality-7 stack made a fan of Lv 36-70 (" + made2.weaponType + " Lv." + made2.lvl + "), 100 of the stack are left", made2.weaponType == Item.WeaponType.Fan && made2.lvl >= 36 && made2.lvl <= 70 && high.quantity == 100);
        int tb = GameDriver.time;
        PcProbe.cmd("craft do 9", 60);
        PcProbe.cmd("craft do 1 banana", 60);
        check("C2e a number that is not on the list, or an unknown kind, spends no turn", GameDriver.time == tb);
        PcProbe.cmd("craft do 1", 120);
        check("C2f a stack of 500 can be used up (a random kind): it is gone from the bag, the leftover 100 stays (" + p.materialBag.size() + ")", !p.materialBag.contains(low) && p.materialBag.size() == 1 && p.backpack.size() == 3);
        PcProbe.cmd("craft do 1", 60);
        check("C2g with nothing left it says so and spends no turn", GameDriver.time == tb + 1);

        // ---- C3: tailor and scribe
        p.trait = Character.Traits.Tailor; p.materialBag.clear(); p.backpack.clear();
        Item herb = stack(Item.ItemType.Herb, 5, 800);
        p.materialBag.add(herb);
        PcProbe.cmd("craft do 1", 120);
        check("C3 a tailor sews clothing from the chosen herbs (" + (p.backpack.isEmpty() ? "nothing" : p.backpack.get(0).name) + ") using 500", p.backpack.size() == 1 && p.backpack.get(0).itemType == Item.ItemType.Clothing && herb.quantity == 300);
        p.trait = Character.Traits.Scribe; p.materialBag.clear();
        Item ink = stack(Item.ItemType.Metal, 4, 1200);
        p.materialBag.add(ink);
        o = CraftMenu.options(p);
        check("C3b a scribe's stack of 1,200 allows (1200 - 600) / 500 + 1 = 2 arrays (" + (o.isEmpty() ? 0 : o.get(0).upTo) + ")", o.size() == 1 && o.get(0).upTo == 2);
        int arrays0 = p.arrayBag.size();
        PcProbe.cmd("craft do 1", 120);
        check("C3c 'craft do 1' draws an array (" + p.arrayBag.size() + ") from it (" + ink.quantity + " left)", p.arrayBag.size() == arrays0 + 1 && ink.quantity == 700);

        // ---- C4: an alchemist
        p.trait = Character.Traits.Alchemist; p.materialBag.clear(); p.pillBag.clear(); p.affiliation = "Good"; p.affiliationNum = 1;
        Item herbs = stack(Item.ItemType.Herb, 4, 500), blood = stack(Item.ItemType.Blood, 10, 2500);
        p.materialBag.add(herbs);
        o = CraftMenu.options(p);
        check("C4 one pill option, quality 4, up to 6 (skill 50 = 5 + 1; the stack holds 10) (" + o.size() + ", up to " + (o.isEmpty() ? 0 : o.get(0).upTo) + ")", o.size() == 1 && o.get(0).kind.equals("pill") && o.get(0).upTo == 6);
        int pillsBase = p.itemsCrafted;
        PcProbe.cmd("craft do 1 3", 120);
        check("C4b 'craft do 1 3' brewed 3 pills of quality 4 and used 150 herbs (" + p.pillTotal() + " pills, " + herbs.quantity + " herbs)", p.pillTotal() == 3 && herbs.quantity == 350 && p.pillBag.get(0).lvl == 4);
        check("C4b2 each pill counts as an item crafted (+" + (p.itemsCrafted - pillsBase) + " for 3 pills)", p.itemsCrafted - pillsBase == 3);
        PcProbe.cmd("craft do 1 99", 120);
        check("C4c more than a turn allows is cut to the limit (" + p.pillTotal() + " pills, 3 + 6 = 9... the stack has 7 left)", p.pillTotal() == 3 + 6 && herbs.quantity == 50);
        check("C4d brewing ordinary pills does not change alignment (" + p.affiliation + ")", "Good".equals(p.affiliation));

        // ---- C5: Potential Pills make the maker Evil
        p.materialBag.add(blood);
        o = CraftMenu.options(p);
        CraftMenu.Option po = null; int pn = 0;
        for (int i = 0; i < o.size(); i++) if (o.get(i).kind.equals("potential")) { po = o.get(i); pn = i + 1; }
        check("C5 the Blood stack is an option with the warning (" + (po == null ? "none" : po.warn) + ")", po != null && po.warn != null && po.warn.contains("Evil") && po.upTo == 2);
        PcProbe.cmd("craft do " + pn, 120);
        int potion = 0; for (Item pi : p.pillBag) if (pi.pillType == Item.PillType.Potential) potion += pi.quantity;
        check("C5b 2 Potential Pills were made from 2,000 Blood (" + potion + ", Blood left " + blood.quantity + ")", potion == 2 && blood.quantity == 500);
        check("C5c ...and the maker is Evil now (" + p.affiliation + ", " + p.affiliationNum + ")", "Evil".equals(p.affiliation) && p.affiliationNum == -1);

        // ---- C6: the game's own alchemy() and refining for another person do it too
        Character npcAlch = new Character(p.location);
        npcAlch.age = 40; npcAlch.gender = "Female"; npcAlch.maxAge = 100000; npcAlch.trait = Character.Traits.Alchemist; npcAlch.professionLvl = 30; npcAlch.affiliation = "Good"; npcAlch.affiliationNum = 1;
        npcAlch.materialBag.add(stack(Item.ItemType.Blood, 10, 1000));
        npcAlch.alchemy();
        check("C6 an alchemist's own alchemy() with 1,000 Blood makes her Evil (" + npcAlch.affiliation + ")", "Evil".equals(npcAlch.affiliation));
        Character client = new Character(p.location);
        client.age = 30; client.gender = "Male"; client.maxAge = 100000; client.trait = Character.Traits.Friendly; client.ss = 100000000L; client.affiliation = "Good";
        client.materialBag.add(stack(Item.ItemType.Blood, 10, 2000));
        Character refiner = new Character(p.location);
        refiner.age = 40; refiner.gender = "Male"; refiner.maxAge = 100000; refiner.trait = Character.Traits.Alchemist; refiner.professionLvl = 30; refiner.affiliation = "Neutral"; refiner.affiliationNum = 0;
        client.refineBloodWith(refiner);
        check("C6b refining someone's Blood makes the REFINER Evil (" + refiner.affiliation + "), not the client (" + client.affiliation + ")", "Evil".equals(refiner.affiliation) && "Good".equals(client.affiliation));

        // ---- C7: a trade without a menu
        p.trait = Character.Traits.Chef; p.materialBag.clear();
        int tc = GameDriver.time;
        PcProbe.cmd("craft list", 60);
        PcProbe.cmd("craft do 1", 60);
        check("C7 a Chef's trade has no menu: it says so and spends no turn", CraftMenu.options(p).isEmpty() && GameDriver.time == tc);
        say("DONE");
    }
}
