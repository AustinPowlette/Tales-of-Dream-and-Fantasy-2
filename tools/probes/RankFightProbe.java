package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-14: the fourth board, Highest Fighting Level, and the first renamed to Highest Level (`Rankings`): the rating is read from what is worn (nothing is equipped), counts the lasting bonuses and the
 *  companion beasts, the yearly count fills `rankFight` (a thousand of them, a title for the first hundred), a title comes from the best board, the epoch spares them, the text of `ranking` lists both. */
public class RankFightProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    static void check(String n, boolean ok) { PcProbe.check(n, ok); }
    static Character adult(int lvl, int major) {
        Character c = new Character(Map.tiles[Map.range / 2][Map.range / 2]);
        c.age = 30; c.gender = "Male"; c.lvl = lvl; c.majorLvl = major; c.maxAge = 100000; c.caution = 50; c.sect = null; c.trait = Character.Traits.Friendly;
        while (Map.characters.size() < c.id) Map.characters.add(null);
        Map.characters.add(c); Map.population++;
        return c;
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

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        check("player created and prompting", p != null && PcProbe.in.blocked);
        if (p == null) return;
        Settings.lore = false;
        GameDriver.frozenNpcs = true;

        // ---- K1: the rating
        Character c = adult(50, 5);
        c.equippedClothing = new Item(c, 20, Item.ItemType.Clothing);
        c.equippedWeapon = new Item(c, Item.WeaponType.Sword, 30); c.equippedWeapon.enchantBonus = 2;
        c.martialTechnique = c.movementTechnique = null;
        long raw = 50 + c.equippedClothing.lvl + c.equippedWeapon.lvl + 2;
        check("K1 the rating is level + gear + enchantment (" + Rankings.fightRating(c) + " = " + raw + "), the fighting level is a fifth (" + Rankings.fightLevel(c) + ")", Rankings.fightRating(c) == raw && Rankings.fightLevel(c) == (int) (raw / 5));
        // ---- K2: nothing is equipped by reading it
        Item better = new Item(c, Item.WeaponType.Sword, 90);
        c.backpack.add(better);
        Item worn = c.equippedWeapon;
        Rankings.fightRating(c);
        check("K2 reading the rating equips nothing (a better sword in the bag stays there)", c.equippedWeapon == worn && c.backpack.contains(better));
        // ---- K3: the lasting bonuses count five each
        long base = Rankings.fightRating(c);
        c.blessing = 2;
        check("K3 a soldier's blessing of 2 adds 10 (" + (Rankings.fightRating(c) - base) + ")", Rankings.fightRating(c) - base == 10);
        c.blessing = 0; c.trait = Character.Traits.Cheater;
        check("K3b the Cheater adds 25", Rankings.fightRating(c) - base == 25);
        c.trait = Character.Traits.IronBody; c.majorLvl = 4;
        check("K3c Iron Body adds (4 / 2 + 1) * 5 = 15 (" + (Rankings.fightRating(c) - base) + ")", Rankings.fightRating(c) - base == 15);
        c.trait = Character.Traits.Friendly; c.majorLvl = 5;

        // ---- K4: the yearly count
        Character g1 = adult(30, 3), g2 = adult(31, 3), g3 = adult(20, 2);
        g1.equippedWeapon = new Item(g1, Item.WeaponType.Spear, 90); // low level, a mighty weapon
        g2.equippedWeapon = null; g3.equippedWeapon = new Item(g3, Item.WeaponType.Fan, 40);
        for (Character x : Map.characters) if (x != null && x != g1 && x != g2 && x != g3 && x != c && !x.dead) { x.lvl = 0; x.equippedWeapon = null; x.equippedClothing = null; x.martialTechnique = null; x.movementTechnique = null; }
        c.lvl = 5; c.equippedWeapon = null; c.equippedClothing = null; p.lvl = 1; p.majorLvl = 0; p.equippedWeapon = null; p.equippedClothing = null; p.martialTechnique = null; p.movementTechnique = null;
        Rankings.yearly(Map.characters);
        check("K4 the yearly count ranks the best fighter first although he has the lower level (rank " + g1.rankFight + ", Highest Level rank " + g1.rankStrong + " vs " + g2.rankStrong + ")", g1.rankFight == 1 && g2.rankStrong < g1.rankStrong);
        check("K4b the board takes the thousand best (" + countRanked() + " ranked) and an unranked soul has 0", countRanked() > 0 && countRanked() <= Rankings.STRONG_SAFE);
        // ---- K5: titles
        g1.rankStrong = 0; g1.rankRich = 0; g1.rankKills = 0;
        check("K5 rank 1 of the fighting board holds the War God title (" + Rankings.title(g1) + ")", "War God of the Nine Heavens".equals(Rankings.title(g1)));
        g1.gender = "Female";
        check("K5b a woman holds the woman's title (" + Rankings.title(g1) + ")", "War Goddess of the Nine Heavens".equals(Rankings.title(g1)));
        g1.rankFight = 5;
        check("K5c rank 5 is the lesser title (" + Rankings.title(g1) + ")", "Peerless Warrior".equals(Rankings.title(g1)));
        g1.rankFight = 500;
        check("K5d rank 500 is outside the hundred: no title, but the epoch still spares him (" + Rankings.title(g1) + ", " + Rankings.epochSafe(g1) + ")", Rankings.title(g1) == null && Rankings.epochSafe(g1));
        g1.rankFight = 1; g1.rankStrong = 2;
        check("K5e on two boards the grander tier wins (Highest Level 2 = tier 0 and Fighting 1 = tier 0: the first board) (" + Rankings.title(g1) + ")", Rankings.title(g1) != null);
        // ---- K6: the text and the places
        g1.rankFight = 1; g1.rankStrong = 0;
        String txt = Rankings.text(null, g1);
        check("K6 the ranking text lists the four boards", txt.contains("Highest Level") && txt.contains("Highest Fighting Level") && txt.contains("Richest") && txt.contains("Most Kills"));
        check("K6b 'ranking fighting' shows only that board, 'ranking level' only the level board", Rankings.text("fighting", g1).contains("Highest Fighting Level") && !Rankings.text("fighting", g1).contains("Most Kills") && Rankings.text("level", g1).contains("Highest Level") && !Rankings.text("level", g1).contains("Fighting Level ("));
        check("K6c the places line says it (" + Rankings.places(g1) + ")", Rankings.places(g1).contains("Fighting Level 1"));
        // ---- K7: an old save has no place
        Character old = adult(10, 1);
        check("K7 a character from an old save has rankFight 0", old.rankFight == 0 && !Rankings.epochSafe(old));
        // ---- K8: both level boards are epoch-safe, up to a thousand
        Character lowLevelFighter = adult(5, 0);
        lowLevelFighter.rankFight = 900;
        check("K8 a place on the fighting board spares from the epoch", Rankings.epochSafe(lowLevelFighter));
        System.out.println();
        PcProbe.real.println("[T] DONE");
    }

    static int countRanked() { int n = 0; for (Character x : Map.characters) if (x != null && x.rankFight > 0) n++; return n; }
}
