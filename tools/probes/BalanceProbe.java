package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-09 balance and speed batch: auction bids are a multiple of the reserve (not the bidder's purse), a new shop has starter stock, a kind of title is held by its ten best only,
 *  the Epoch population target, the start spread, and the trigger throttle keeps the chance per week (the dice are scaled). */
public class BalanceProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; } // (the shop check switches the seed on for itself)
    static void check(String name, boolean ok) { PcProbe.passes += ok ? 1 : 0; PcProbe.fails += ok ? 0 : 1; PcProbe.real.println("[T] " + (ok ? "PASS " : "FAIL ") + name); }

    public static void main(String[] a) throws Exception {
        PcProbe.real = System.out;
        System.setIn(PcProbe.in);
        GameDriver.frozenNpcs = true; // frozen before the first tick: the NPCs act before the human prompt of a tick, and one of them could otherwise ask the character a question before the probe is ready
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(PcProbe.real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        try { run(); } catch (Throwable e) { PcProbe.real.println("[T] PROBE EXCEPTION " + e); e.printStackTrace(PcProbe.real); PcProbe.fails++; }
        PcProbe.real.println("[T] ===== " + PcProbe.passes + " passed, " + PcProbe.fails + " failed; gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }

    static Area home() { return Map.tiles[Map.range / 2][Map.range / 2]; }
    static Character adult(int lvl, int major) {
        Character c = new Character(home());
        c.age = 30; c.gender = "Male"; c.lvl = lvl; c.majorLvl = major; c.maxAge = 100000; c.caution = 50; c.sect = null;
        if (c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) c.trait = Character.Traits.Friendly;
        Map.characters.add(c); Map.population++;
        return c;
    }
    static void tp(Character c, Area to) { c.location.removeChar(c); c.location = to; to.addChar(c); }
    static long wealth(Character c) { return c.ss + c.sc * 1_000_000L + c.dc * 1_000_000_000_000L; }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        GameDriver.frozenNpcs = true;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        p.pc().squadPolicy = 2; p.pc().friendPolicy = 1;
        GameDriver.time = Math.max(GameDriver.time, 100);
        Area city = home(); city.city = true; city.population = 2000; city.backpack = new ArrayList<>();

        // ---- auctions: a rich bidder offers a few reserves, not the purse
        city.auction().clear(); if (city.goodsBag != null) city.goodsBag.clear();
        Item lot = new Item(p, Item.WeaponType.Sword, 12);
        Auctions.consign(city, lot, null);
        long reserve = Auctions.reserve(lot);
        ArrayList<Character> rich = new ArrayList<>();
        for (int i = 0; i < 3; i++) { Character b = adult(30, 3); b.equippedWeapon = null; b.backpack.clear(); b.ss = 0; b.sc = 5_000_000_000L; tp(b, city); b.knownLocations.add(city); rich.add(b); }
        long before = 0; for (Character b : rich) before += wealth(b);
        Auctions.hold(city);
        long after = 0; for (Character b : rich) after += wealth(b);
        long paid = before - after;
        boolean sold = false; for (Character b : rich) if (b.equippedWeapon == lot || b.backpack.contains(lot)) sold = true;
        check("A1 the lot was sold to one of three billionaires", sold || paid > 0);
        check("A2 the winner pays at least the reserve and at most three times it (paid " + paid + ", reserve " + reserve + ")", paid >= reserve && paid <= reserve * 3 + 1);
        int need = 0; for (int i = 0; i < 200; i++) need += (int) (Auctions.npcBid(Auctions.NEED, Long.MAX_VALUE / 4, 1000) > 3000 ? 1 : 0);
        int coll = 0; for (int i = 0; i < 200; i++) coll += (int) (Auctions.npcBid(Auctions.COLLECTOR, Long.MAX_VALUE / 4, 1000) > 1500 ? 1 : 0);
        check("A3 a need never bids above three reserves and a collector above one and a half", need == 0 && coll == 0);
        check("A4 a poor bidder never bids more than the purse", Auctions.npcBid(Auctions.NEED, 1200, 1000) <= 1200 && Auctions.npcBid(Auctions.NEED, 1200, 1000) >= 1000);

        // ---- shops
        Shop.SEED = true;
        Character owner = adult(30, 3);
        Shop s = new Shop(owner, city);
        Shop.SEED = false;
        int top = Math.min(40, 6 + 5 * Math.max(1, city.cityLevel()));
        boolean lvlOk = true; for (Item it : s.inventory) if (it.lvl < 1 || it.lvl > top) lvlOk = false;
        check("B1 a new shop opens with 4 to 8 plain weapons and coats (" + s.inventory.size() + ")", s.inventory.size() >= 4 && s.inventory.size() <= 8 && lvlOk);
        s.inventory.clear(); Shop.SEED = true; s.seedStock(); Shop.SEED = false;
        check("B2 a shop that ran dry is stocked again", s.inventory.size() >= 4);

        // ---- titles: the boards (2026-10-10: the hundred Strongest, Richest and with Most Kills replace the ten-of-a-kind killer titles)
        ArrayList<Character> killers = new ArrayList<>();
        for (int i = 0; i < 30; i++) { Character k = adult(20, 1); k.trait = Character.Traits.Friendly; k.kills = 120 + i; k.relationshipList = new RelationshipList(); killers.add(k); }
        Naming.assignTitles(Map.characters);
        check("C1 the three best killers are ranks 1-3 of Most Kills (" + killers.get(29).rankKills + ", " + killers.get(28).rankKills + ", " + killers.get(27).rankKills + ")", killers.get(29).rankKills == 1 && killers.get(28).rankKills == 2 && killers.get(27).rankKills == 3);
        check("C2 the thirtieth killer is still on the board (rank " + killers.get(0).rankKills + ") and has a title (" + killers.get(0).repTitle + ")", killers.get(0).rankKills == 30 && killers.get(0).repTitle != null);
        for (Character k : killers) { k.dead = true; }

        // ---- the Epoch population target, the start spread
        check("D1 the Epoch population target is 200,000, 2 s, 20%", Settings.epochPopulation == 200000 && Settings.epochTickSeconds == 2.0 && Settings.epochCullPercent == 20 && Settings.epochCullNumber == 0);
        check("D2 the default start spread is 21 tiles", Settings.startSpread == 21);

        // ---- the trigger throttle keeps the chance per week: the dice are scaled by trigK
        Character d = adult(20, 1); d.trait = Character.Traits.Friendly;
        int hits1 = 0, hits2 = 0; d.trigK = 1; for (int i = 0; i < 40000; i++) if (QuestLife.roll(d, "Hunt Beast", 20)) hits1++;
        d.trigK = 2; for (int i = 0; i < 40000; i++) if (QuestLife.roll(d, "Hunt Beast", 20)) hits2++;
        d.trigK = 1;
        check("E1 on a looked-at turn the odds double when the triggers are looked at every second turn (" + hits1 + " -> " + hits2 + ")", hits2 > hits1 * 1.6 && hits2 < hits1 * 2.5);
    }
}
