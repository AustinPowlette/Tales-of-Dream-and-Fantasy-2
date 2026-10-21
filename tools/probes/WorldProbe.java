package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-07: a place gets its name back when its city or dungeon is gone, the city's tenth of every sale, the 25-city / 25-sect protection and the new targets, the epoch by tick time (with the
 *  potential blessing), buying gear from shops first and then the city pool, crafters selling sooner, nearest-target Explore Dungeon, instruments that are not made (they mature) and the Perform quest. */
public class WorldProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; } // these checks test one trigger / a shop's exact goods: no throttle, no starter stock
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
    static Character adultTrait(Character.Traits tr) { Character c = adult(30, 3); c.trait = tr; return c; }
    static void tp(Character c, Area to) { c.location.removeChar(c); c.location = to; to.addChar(c); }

    /** An ordinary tile (no city, sect, dungeon, lore site or Originis) at least `min` and at most `max` tiles from `from`, not in `not`. */
    static Area plain(Area from, int min, int max, Set<Area> not) {
        for (Area a : new ArrayList<>(Map.createdAreas)) {
            if (a == null || a.isOriginis() || a.city || a.sect != null || a.dungeonType != null || a.loreSite != null || not.contains(a)) continue;
            int d = PlayerControl.distance(from, a);
            if (d >= min && d <= max) return a;
        }
        return null;
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        GameDriver.frozenNpcs = true;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        p.caution = 0; p.maxAge = 100000;
        p.pc().squadPolicy = 2; p.pc().friendPolicy = 1;
        GameDriver.time = Math.max(GameDriver.time, 100);
        Area city = home(); city.city = true; city.population = 2000; city.backpack = new ArrayList<>();
        Set<Area> used = new HashSet<>();
        Map.currentCharacter = p;
        for (int k = 2; k <= 12; k++) { for (int[] d : new int[][]{{k, 0}, {0, k}, {-k, 0}, {0, -k}, {k, 1}}) {  // only the nine start tiles exist so far (all cities or sects): make some plain ones
            int x = Math.floorMod(city.x + d[0], Map.range), y = Math.floorMod(city.y + d[1], Map.range);
            if (Map.tiles[x][y] == null) synchronized (Map.tileLock) { if (Map.tiles[x][y] == null) Map.tiles[x][y] = new Area(Area.ghost(x == 0 ? Map.range - 1 : x - 1, y), 'e'); }
        } }

        // ---- N: names
        Area w1 = plain(city, 1, 20, used); used.add(w1);
        check("N0 the probe found ordinary tiles to use", w1 != null);
        String old1 = w1.name; w1.geoName = old1; w1.city = true; w1.name = "Test City"; w1.population = 1000;
        adultTrait(Character.Traits.Friendly).destroyCity(w1);
        check("N1 a destroyed city gives its tile the old name back (" + old1 + ")", !w1.city && w1.name.equals(old1));
        Area w2 = plain(city, 1, 20, used); used.add(w2);
        String old2 = w2.name; w2.geoName = old2; w2.name = "Test Lair"; w2.dungeonType = Area.DungeonType.BEAST_LAIR; w2.monsters = 0; w2.lvl = 30;
        Character hunter = adult(30, 3); tp(hunter, w2);
        hunter.targetQuests(new Quest("Explore Dungeon", true, 35, 2, 60, w2));
        check("N2 a Beast Lair that has been hunted out gives its tile the old name back", w2.dungeonType == null && w2.name.equals(old2));
        Area w3 = plain(city, 1, 20, used); used.add(w3);
        String old3 = w3.name; w3.geoName = old3; w3.name = "Test Ruins"; w3.dungeonType = Area.DungeonType.LOST_RUINS; w3.dungeonCharges = 1; w3.monsters = 0; w3.lvl = 30;
        tp(hunter, w3); hunter.targetQuests(new Quest("Explore Dungeon", true, 35, 2, 60, w3));
        check("N3 Lost Ruins explored to the end give the old name back too", w3.dungeonType == null && w3.name.equals(old3));
        Area w4 = plain(city, 1, 20, used); used.add(w4);
        w4.geoName = w4.name; w4.name = "Test Site"; w4.loreSite = "test";
        w4.restoreName();
        check("N4 a story place keeps its name", w4.name.equals("Test Site")); w4.loreSite = null; w4.name = w4.geoName;
        city.city = true;
        String keepName = city.name; city.restoreName();
        check("N5 a city that still stands keeps its name", city.name.equals(keepName));

        // ---- T: a tenth of every sale
        Character lord = adultTrait(Character.Traits.Friendly); lord.ss = 0; city.cityOwner = lord; city.cityTreasury = 0;
        city.receivePayment(1000);
        check("T1 a payment to the city: 900 to the lord, 100 to the treasury (" + lord.ss + " / " + city.cityTreasury + ")", lord.ss == 900 && city.cityTreasury == 100);
        city.cityOwner = null; city.cityTreasury = 0; city.receivePayment(1000);
        check("T2 an ownerless city's treasury gets all of it", city.cityTreasury == 1000);
        Character merchant = adultTrait(Character.Traits.Merchant); merchant.ss = 0;
        city.shops.clear(); Shop shop = new Shop(merchant, city); city.shops.add(shop); merchant.ownedShops.add(shop);
        Item sw = new Item(merchant, Item.WeaponType.Sword, 10); shop.inventory.add(sw);
        long price = shop.sellPrice(sw);
        Character buyer0 = adult(30, 3); buyer0.ss = 5_000_000; city.cityTreasury = 0;
        shop.sellToBuyer(buyer0, sw);
        check("T3 a shop sale: the city takes a tenth (" + city.cityTreasury + " of " + price + "), the merchant the rest (" + merchant.ss + ")", city.cityTreasury == price / 10 && merchant.ss == price - price / 10);
        Area notCity = plain(city, 1, 20, used); used.add(notCity);
        check("T4 a tile that is not a city takes nothing", notCity.salesTax(1000) == 0);

        // ---- S: settlements
        Area scity = plain(city, 1, 20, used); used.add(scity); scity.city = true;
        Area ssect = plain(city, 1, 20, used); used.add(ssect);
        Character founder = adultTrait(Character.Traits.Leader); ssect.sect = new Organization(founder);
        Settlements.cityCount.set(20); Settlements.sectCount.set(20);
        check("S1 with 20 cities and 20 sects the Evil may not destroy either", !Settlements.mayDestroy(scity) && !Settlements.mayDestroy(ssect));
        Settlements.cityCount.set(26); Settlements.sectCount.set(20);
        check("S2 with more than 25 cities a city may be destroyed, a sect still not", Settlements.mayDestroy(scity) && !Settlements.mayDestroy(ssect));
        Settlements.sectCount.set(26);
        check("S3 with more than 25 sects a sect may be destroyed", Settlements.mayDestroy(ssect));
        scity.city = false; ssect.sect = null; founder.sect = null;
        int[] founded = Settlements.yearly();
        int cult = 0; for (Character c : new ArrayList<>(Map.characters)) if (c != null && !c.dead && c.born && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster) cult++;
        check("S4 yearly: targets are at least 25 (1 per 150 in a small world) and then 1 city per 2,000 / 1 sect per 2,500 (" + Settlements.cityTarget + " / " + Settlements.sectTarget + " for " + cult + ")",
                Settlements.cityTarget == Math.max(Math.min(25, cult / 150), cult / 2000) && Settlements.sectTarget == Math.max(Math.min(25, cult / 150), cult / 2500));
        check("S5 natural founding stops when the world has more than it supports", !(Settlements.cityCount.get() >= Settlements.cityTarget + 5) == Settlements.cityRoom());

        // ---- G: gear — shops first, then the city pool
        city.cityOwner = null; city.cityTreasury = 0; city.backpack.clear(); city.shops.clear();
        Character gm = adultTrait(Character.Traits.Merchant); gm.ss = 50_000_000; gm.convertCurrency();
        Shop gshop = new Shop(gm, city); city.shops.add(gshop); gm.ownedShops.add(gshop);
        Item shopW = new Item(gm, Item.WeaponType.Sword, 12); gshop.inventory.add(shopW);
        Item poolW = new Item(gm, Item.WeaponType.Spear, 15); city.backpack.add(poolW);
        Character b1 = adult(15, 1); b1.ss = 50_000_000; b1.convertCurrency(); b1.equippedWeapon = null; b1.backpack.clear(); tp(b1, city);
        b1.buyGear();
        check("G1 the buyer takes the shop's weapon first, though the pool has a better one", b1.equippedWeapon == shopW && city.backpack.contains(poolW));
        gshop.inventory.clear();
        Character b2 = adult(15, 1); b2.ss = 50_000_000; b2.convertCurrency(); b2.equippedWeapon = null; b2.backpack.clear(); tp(b2, city);
        long t0 = city.cityTreasury;
        b2.buyGear();
        check("G2 with no shop stock the buyer buys from the city pool at 1.25x (" + (city.cityTreasury - t0) + " ss to the city)", b2.equippedWeapon == poolW && !city.backpack.contains(poolW) && city.cityTreasury - t0 == Character.poolPrice(poolW));
        Character b3 = adult(15, 1); b3.ss = 0; b3.sc = 0; b3.equippedWeapon = null; tp(b3, city); city.backpack.add(new Item(gm, Item.WeaponType.Fan, 14));
        b3.buyGear();
        check("G3 a buyer who cannot pay buys nothing", b3.equippedWeapon == null);
        check("G4 a city with only a pool counts as a place to shop", b3.canShopHere() && b3.hasBuyableGear(city) == false);
        b3.ss = 50_000_000; b3.convertCurrency();
        check("G4b ...and hasBuyableGear sees the pool once the buyer can pay", b3.hasBuyableGear(city));
        city.backpack.clear();

        // ---- C: crafters sell sooner
        Area away = plain(city, 3, 25, used); used.add(away);
        Character smith = adultTrait(Character.Traits.Blacksmith); tp(smith, away);
        for (int i = 0; i < 4; i++) smith.backpack.add(new Item(smith, Item.WeaponType.Saber, 5 + i));
        smith.equippedWeapon = smith.backpack.get(0);
        smith.knownLocations.add(city);
        boolean goSell = false;
        for (int i = 0; i < 40 && !goSell; i++) { smith.quest(); goSell = (smith.currentQuest != null && "Go Sell".equals(smith.currentQuest.name)) || smith.targetQuests.stream().anyMatch(q -> "Go Sell".equals(q.name)) || smith.location == city; }
        check("C1 a smith holding three finished weapons heads for a city to sell (not only past eight items)", goSell);

        // ---- E: explore the nearest dungeon, rest between trips
        Area near = plain(city, 1, 3, used); used.add(near);
        Area far = plain(city, 9, 30, used); used.add(far);
        if (near != null && far != null) {
            near.dungeonType = Area.DungeonType.LOST_RUINS; near.dungeonCharges = 5; near.lvl = 30; far.dungeonType = Area.DungeonType.LOST_RUINS; far.dungeonCharges = 5; far.lvl = 30;
            Character ex = adult(30, 3); ex.knownLocations.add(far); ex.knownLocations.add(near); ex.nextExploreWeek = 0;
            Area picked = null, expected = null;
            for (int i = 0; i < 400 && picked == null; i++) {
                near.lvl = far.lvl = ex.lvl; // a character's level is recalculated as it cultivates: keep the dungeons in its range
                tp(ex, city); // always from the same place (a walker could reach the dungeon and finish the trip inside one call)
                Area before = ex.location;
                ex.quest();
                if (ex.currentQuest != null && "Explore Dungeon".equals(ex.currentQuest.name)) picked = ex.currentQuest.targetArea;
                else for (Quest q : ex.targetQuests) if ("Explore Dungeon".equals(q.name)) picked = q.targetArea;
                if (picked != null) expected = PlayerControl.distance(before, near) <= PlayerControl.distance(before, far) ? near : far;
            }
            if (picked == null && near.dungeonCharges < 5 && far.dungeonCharges == 5) { picked = near; expected = near; } // a fast mover reached the near ruins and explored them inside one call
            check("E1 the dungeon trip goes to the nearer dungeon, not the first one known (picked " + (picked == null ? "none" : picked.name) + ", expected " + (expected == null ? "-" : expected.name) + ")", picked != null && picked == expected);
            Character rest = adult(30, 3); rest.knownLocations.add(near); rest.nextExploreWeek = GameDriver.time + 100;
            boolean started = false;
            for (int i = 0; i < 200 && !started; i++) { rest.quest(); started = (rest.currentQuest != null && "Explore Dungeon".equals(rest.currentQuest.name)) || rest.targetQuests.stream().anyMatch(q -> "Explore Dungeon".equals(q.name)); }
            check("E2 a character on its rest between dungeon trips starts none", !started);
            near.dungeonType = null; far.dungeonType = null;
        } else check("E1 tiles found for the dungeon test", false);

        // ---- I: instruments
        Character mus = adultTrait(Character.Traits.Musician); mus.majorLvl = 5; mus.lvl = 52;
        check("I1 an instrument cannot be crafted", Life.craft(mus, Item.ItemType.Instrument) == -4 && Life.craft(mus, Item.ItemType.Instrument, true) == -4);
        check("I2 the amateur menu no longer lists instruments", !Arrays.asList(AmateurCraft.KINDS).contains("instrument") && AmateurCraft.craft(mus, "instrument") != null);
        String said = PcProbe.since(PcProbe.mark());
        int mk = PcProbe.mark(); boolean made = PlayerCommands.handle(p, "make instrument"); said = PcProbe.since(mk);
        check("I3 'make instrument' says instruments are not made", !made && said.contains("not made"));
        check("I4 a Musician without an instrument is given a plain one when they play", Life.bestInstrument(mus) == null && Life.play(mus) == 2 && Life.bestInstrument(mus) != null && Life.bestInstrument(mus).lvl == 1);
        Item inst = Life.bestInstrument(mus);
        for (int i = 0; i < 5000; i++) Life.practise(mus, inst, 1);
        check("I5 playing matures the instrument up to the player's realm + 1 (Lv." + inst.lvl + " of 6)", inst.lvl == 6);
        Character mus2 = adultTrait(Character.Traits.Musician); mus2.majorLvl = 1;
        Life.play(mus2); Item inst2 = Life.bestInstrument(mus2);
        for (int i = 0; i < 5000; i++) Life.practise(mus2, inst2, 1);
        check("I5b ...and no higher (a Major Lvl 1 Musician stops at Lv." + inst2.lvl + ")", inst2.lvl == 2);
        tp(mus, city); city.cityOwner = null; mus.ss = 0; mus.sc = 0;
        int cl = city.cityLevel();
        Life.concert(mus);
        long tipsMin = Math.max(1, Character.itemPrice(Life.bestInstrument(mus)) / 20) * (10 + 10L * cl);
        check("I6 a concert in a city earns the crowd's tips (" + (mus.ss + mus.sc * 1_000_000L) + " ss, at least " + tipsMin + ") and sets a rest", mus.ss + mus.sc * 1_000_000L >= tipsMin && mus.nextPerformWeek > GameDriver.time);
        Character mus3 = adultTrait(Character.Traits.Musician); mus3.majorLvl = 3; Life.play(mus3); tp(mus3, away); mus3.knownLocations.add(city);
        check("I7 the nearest, biggest known city is the stage", Life.stage(mus3) == city);
        boolean perform = false;
        for (int i = 0; i < 600 && !perform; i++) { mus3.quest(); perform = (mus3.currentQuest != null && "Perform".equals(mus3.currentQuest.name)) || mus3.targetQuests.stream().anyMatch(q -> "Perform".equals(q.name)) || mus3.location == city; }
        check("I8 a Musician takes a Perform quest to that city", perform);
        Quest pq = new Quest("Perform", true, 40, 3, 62, city);
        mus3.nextPerformWeek = 0; tp(mus3, city);
        check("I9 the Perform quest is viable in a city and a concert is given on arrival", mus3.questViable(pq) == 1 && mus3.targetQuests(pq) == 1);

        // ---- M: the map's picture cache holds every tile (it held 160 for 961 tiles and rebuilt ~800 on every redraw), and builds in the background
        PlaceImages.thumbSync = true;
        int built = 0; for (Area ar : new ArrayList<>(Map.createdAreas)) if (PlaceImages.thumb(ar, 20) != null) built++;
        PlaceImages.thumbSync = false;
        int again = 0; for (Area ar : new ArrayList<>(Map.createdAreas)) if (PlaceImages.thumb(ar, 20) != null) again++;
        check("M1 a second paint of the map's pictures finds them all in the cache (" + again + " of " + built + ")", again == built && PlaceImages.thumbsIdle());
        Area fresh = plain(city, 1, 30, new HashSet<>()); String keepFresh = fresh.name; fresh.name = fresh.name + " II";
        long mt = System.nanoTime();
        java.awt.image.BufferedImage first = PlaceImages.thumb(fresh, 20);
        long firstMs = (System.nanoTime() - mt) / 1_000_000;
        boolean later = false;
        for (int i = 0; i < 100 && !later; i++) { Thread.sleep(50); later = PlaceImages.thumbsIdle(); }
        check("M2 a new picture is not built while the map is drawn (" + firstMs + " ms, " + (first == null ? "not ready" : "ready") + ") but arrives from the background", !PlaceImages.enabled || (first == null && later));
        fresh.name = keepFresh;

        // ---- P/X: the epoch
        Character pa = adult(30, 3); pa.potential = 10; Character pb = adult(30, 3); pb.potential = 100; Character pc2 = adult(30, 3); pc2.potential = 101;
        GameDriver.epochBlessing();
        check("X1 the blessing: half a Potential Pill for a survivor of the culled realms (10 -> 12), never above 100 (" + pa.potential + ", " + pb.potential + ", " + pc2.potential + ")", pa.potential == 12 && pb.potential == 100 && pc2.potential == 101);
        GameDriver.frozenNpcs = false;
        Settings.epochTickSeconds = 1000; GameDriver.EPOCH_MIN_LIVING = 50;
        PcProbe.waitTurns(30);
        check("X2 the typical tick time is measured without the waiting (" + GameDriver.medianTickMs() + " ms)", GameDriver.tickCount >= 20 && GameDriver.medianTickMs() >= 0 && GameDriver.medianTickMs() < 60_000);
        int logMark = PcProbe.mark();
        long living0 = 0; HashMap<Integer, Integer> pot0 = new HashMap<>();
        for (Character c : new ArrayList<>(Map.characters)) if (c != null && !c.dead && c.born && c.player == 0 && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster) { living0++; pot0.put(c.id, c.potential); }
        PcProbe.waitTurns(60);
        check("X3 with the threshold far above the tick time there is no epoch", !PcProbe.since(logMark).contains("[EPOCH] The Heavens grow weary"));
        Settings.epochTickSeconds = 0; // any tick is "too slow"
        pot0.clear(); living0 = 0;
        for (Character c : new ArrayList<>(Map.characters)) if (c != null && !c.dead && c.born && c.player == 0 && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster) { living0++; pot0.put(c.id, c.potential); }
        logMark = PcProbe.mark();
        String log = "";
        for (int i = 0; i < 16 && !log.contains("[EPOCH] The Heavens grow weary"); i++) { PcProbe.waitTurns(5); log = PcProbe.since(logMark); }
        check("X4 when the ticks are too slow an epoch begins at the year's turn", log.contains("[EPOCH] The Heavens grow weary of the crowded world (a turn now takes"));
        Settings.epochTickSeconds = 1000; // no second epoch while we look
        PcProbe.waitTurns(10);
        log = PcProbe.since(logMark);
        check("X5 the purge ends and the survivors are blessed", log.contains("The purge has ended") && log.contains("the potential of half a Potential Pill"));
        long alive = 0, up = 0, could = 0;
        for (java.util.Map.Entry<Integer, Integer> e : pot0.entrySet()) { Character c = Character.charById(e.getKey()); if (c == null || c.dead) continue; alive++; if (e.getValue() < 100 && c.majorLvl <= Settings.epochMaxRealm) { could++; if (c.potential > e.getValue() || c.potentialProgress > 0) up++; } }
        check("X6 about a fifth of the realms' cultivators fell (" + (living0 - alive) + " of " + living0 + ") and nearly every survivor of the realms up to the limit gained potential (" + up + " of " + could + ")", living0 - alive >= living0 / 10 && living0 - alive <= living0 / 3 + 20 && up >= could * 0.9);
        // the cull as a number: the next epoch takes about that many in all, shared out over the realms
        Settings.epochCullNumber = 60; Settings.epochTickSeconds = 0;
        logMark = PcProbe.mark(); log = "";
        for (int i = 0; i < 40 && !log.contains("cultivators are marked for the great purge"); i++) { PcProbe.waitTurns(5); log = PcProbe.since(logMark); }
        java.util.regex.Matcher mm = java.util.regex.Pattern.compile("(\\d+) cultivators are marked for the great purge").matcher(log);
        int marked = mm.find() ? Integer.parseInt(mm.group(1)) : -1;
        Settings.epochTickSeconds = 1000; Settings.epochCullNumber = 0;
        check("X7 with the cull set to a number (60) the epoch marks about that many (" + marked + ")", marked >= 40 && marked <= 80);
    }
}
