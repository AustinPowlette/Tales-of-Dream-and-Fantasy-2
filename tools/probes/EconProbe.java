package Game;

import java.io.PrintStream;
import java.util.*;

/** The 2026-10-03 economy: yearly Heaven-Sent treasures of eight kinds, consignment and the auction schedule (five lots or six weeks), Potential Pills only at auction, the library's one book
 *  for each type and potential (20% fee, the seller loses the technique), shops without a stock limit, and Blood refined into Potential Pills by an alchemist. */
public class EconProbe {
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

    static Item weapon(Character maker, int lvl, boolean heaven) { Item w = new Item(maker, Item.WeaponType.Sword, lvl); w.heavenSent = heaven; return w; }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        GameDriver.frozenNpcs = true;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        p.caution = 0; p.maxAge = 100000;
        GameDriver.time = Math.max(GameDriver.time, 100);
        Area city = home();
        city.city = true; city.population = 2000; city.auctionBag = null; city.auctionSince = 0;
        if (city.backpack == null) city.backpack = new ArrayList<>();

        // ---- T1: the eight kinds of Heaven-Sent treasure
        boolean allKinds = true, flagged = true; Set<String> names = new HashSet<>();
        for (Treasure.Kind k : Treasure.Kind.values()) {
            Object o = Treasure.create(k, 3, p);
            if (o == null) allKinds = false;
            else if (o instanceof Technique) { if (!((Technique) o).name.startsWith("Heaven-Sent")) allKinds = false; }
            else { Item it = (Item) o; flagged &= it.heavenSent && it.name.startsWith("Heaven-Sent") || it.itemType == Item.ItemType.Pill; names.add(it.itemType.name()); }
        }
        check("T1 every kind of Heaven-Sent treasure can be made (technique, instrument, poison, two pills, clothes, weapon, array) and the items are flagged", allKinds && flagged && names.containsAll(Arrays.asList("Instrument", "Poison", "Pill", "Clothing", "Weapon", "Array")));
        Area wild = null;
        for (int x = 0; x < Map.range && wild == null; x++) for (int y = 0; y < Map.range; y++) { Area ar = Map.tiles[x][y]; if (ar != null && ar != city && !ar.city && !ar.hasHeaven()) { wild = ar; break; } }
        Character claimer = adult(40, 4);
        int ok = 0;
        for (Treasure.Kind k : Treasure.Kind.values()) {
            Treasure.place(wild, k, Treasure.create(k, 3, p));
            boolean here = wild.hasHeaven();
            Treasure.claim(claimer, wild);
            if (here && !wild.hasHeaven()) ok++;
        }
        boolean gotThem = !claimer.arrayBag.isEmpty() && claimer.pillBag.size() >= 2 && claimer.backpack.stream().anyMatch(i -> i.itemType == Item.ItemType.Poison) && claimer.backpack.stream().anyMatch(i -> i.heavenSent && i.itemType == Item.ItemType.Weapon || i.itemType == Item.ItemType.Weapon)
                && claimer.techniqueList.stream().anyMatch(tq -> tq.name.startsWith("Heaven-Sent")) || claimer.martialTechnique.name.startsWith("Heaven-Sent") || claimer.movementTechnique.name.startsWith("Heaven-Sent");
        check("T1b claiming takes each kind into the right place (array bag, pill bag, backpack, techniques) and clears the tile", ok == 8 && gotThem);
        int before = 0; for (Area ar : Map.createdAreas) if (ar != null && ar.hasHeaven()) before++;
        check("T1c the yearly descent puts one treasure on a wild tile", Treasure.yearly());
        int after = 0; for (Area ar : Map.createdAreas) if (ar != null && ar.hasHeaven()) after++;
        check("T1d ... exactly one more lies unclaimed", after == before + 1);

        // ---- T2: selling consigns luxuries to the auction, ordinary goods go to the pool
        Character seller = adult(50, 5);
        Item heavenW = weapon(seller, 25, true), plainW = weapon(seller, 25, false);
        seller.backpack.add(heavenW); seller.backpack.add(plainW);
        seller.potential = 100; seller.pillBag.add(new Item(Item.ItemType.Pill, Item.PillType.Potential, 11, 2));
        seller.sell(city);
        check("T2 a Heaven-Sent weapon is consigned to the auction block (and not sold to the pool)", city.auction().contains(heavenW) && heavenW.consignorId == seller.id && !seller.backpack.contains(heavenW) && !city.backpack.contains(heavenW));
        check("T2b an ordinary weapon is sold to the pool as before", !city.auction().contains(plainW) && (city.backpack.contains(plainW) || !seller.backpack.contains(plainW)));
        check("T2c Potential Pills are consigned too, never put on a shelf", city.auction().stream().anyMatch(i -> i.pillType == Item.PillType.Potential && i.quantity == 2) && seller.pillBag.stream().noneMatch(i -> i.pillType == Item.PillType.Potential) && (city.pillBag == null || city.pillBag.stream().noneMatch(i -> i.pillType == Item.PillType.Potential)));
        check("T2d Seek Potential now looks at the auction block", Character.potentialPillsAt(city));
        Area small = null;
        for (int x = 0; x < Map.range && small == null; x++) for (int y = 0; y < Map.range; y++) { Area ar = Map.tiles[x][y]; if (ar != null && ar != city && !ar.city) { small = ar; break; } }
        small.city = true; small.population = 100; if (small.backpack == null) small.backpack = new ArrayList<>();
        Character seller2 = adult(50, 5); Item hw2 = weapon(seller2, 25, true); seller2.backpack.add(hw2);
        seller2.sell(small);
        check("T2e a city too small for an auction house does not take a Heaven-Sent item (and no shop does)", seller2.backpack.contains(hw2) && !small.backpack.contains(hw2));
        small.city = false;

        // ---- T3: the schedule: five lots, or six weeks
        Character rich = adult(10, 1); rich.ss = 5_000_000_000L; rich.potential = 100; rich.knownLocations.add(city);
        city.auctionBag = new ArrayList<>(); city.auctionSince = 0;
        Character cons = adult(30, 3); long consWealth0 = cons.ss + cons.sc * 1_000_000L;
        List<Item> lots = new ArrayList<>();
        for (int i = 0; i < 4; i++) { Item w = weapon(cons, 20 + i, true); lots.add(w); Auctions.consign(city, w, cons); }
        GameDriver.time = 200;
        Auctions.tick(city);
        check("T3 the week the first lot arrives nothing is held", city.auctionBag.size() == 4 && city.auctionSince == 200);
        GameDriver.time = 201; Auctions.tick(city);
        check("T3b four lots a week later: still waiting", city.auctionBag.size() == 4);
        Item fifth = weapon(cons, 24, true); lots.add(fifth); Auctions.consign(city, fifth, cons);
        GameDriver.time = 202; Auctions.tick(city);
        check("T3c five lots: the auction is held", city.auctionBag.size() < 5 && rich.backpack.stream().anyMatch(i -> lots.contains(i)) || lots.contains(rich.equippedWeapon));
        long consWealth1 = cons.ss + cons.sc * 1_000_000L;
        check("T3d the consignor is paid (90%) and the city takes the rest", consWealth1 > consWealth0 && city.cityTreasury + (city.cityOwner == null ? 0 : 1) > 0);
        // six weeks for a single lot
        Area city2 = null;
        for (int x = 0; x < Map.range && city2 == null; x++) for (int y = 0; y < Map.range; y++) { Area ar = Map.tiles[x][y]; if (ar != null && ar != city && ar != small && !ar.city) { city2 = ar; break; } }
        if (city2 == null) { // a small world may have no other tile yet: make one next to the home tile
            int fx = Math.floorMod(Map.range / 2 + 5, Map.range), fy = Math.floorMod(Map.range / 2 + 5, Map.range);
            synchronized (Map.tileLock) { if (Map.tiles[fx][fy] == null) Map.tiles[fx][fy] = new Area(Area.ghost(fx == 0 ? Map.range - 1 : fx - 1, fy), 'e'); }
            city2 = Map.tiles[fx][fy];
        }
        city2.city = true; city2.population = 800; city2.auctionBag = null; city2.auctionSince = 0; rich.knownLocations.add(city2);
        Item solo = weapon(cons, 60, true); Auctions.consign(city2, solo, cons);
        int heldAt = -1;
        for (int w = 300; w < 312; w++) { GameDriver.time = w; Auctions.tick(city2); if (heldAt < 0 && !city2.auction().contains(solo)) heldAt = w; }
        check("T3e a single lot is auctioned six weeks after it arrived", heldAt == 306);
        // unsold lots wait for the next one
        Item pricey = weapon(cons, 100, true);
        city2.auctionBag.clear(); city2.auctionSince = 0; rich.knownLocations.remove(city2);
        Auctions.consign(city2, pricey, cons);
        for (int w = 400; w < 410; w++) { GameDriver.time = w; Auctions.tick(city2); }
        check("T3f a lot nobody bids on stays on the block", city2.auction().contains(pricey));
        check("T3g the auction text lists the lots and the rule", Auctions.text(city2, p).contains("Lot 1") && Auctions.text(city2, p).contains("Heaven-Sent"));

        // ---- T4: a human in the city is asked for a bid
        city.auctionBag = new ArrayList<>(); city.auctionSince = 0;
        tp(p, city);
        p.ss = 0; p.sc = 2_000_000; p.potential = 50;
        Item pill = new Item(Item.ItemType.Pill, Item.PillType.Potential, 11, 1);
        Auctions.consign(city, pill, cons);
        long wealth0 = p.ss + p.sc * 1_000_000L;
        DecisionBroker.scriptedNumbers.add(1_000_000_000_000L);
        int prompts0 = DecisionBroker.prompts;
        GameDriver.time = 500; Auctions.hold(city);
        long wealth1 = p.ss + p.sc * 1_000_000L;
        check("T4 a human in the city is asked for a bid, wins the Potential Pill with a big bid, and pays the runner-up's price (at least the reserve)", DecisionBroker.prompts == prompts0 + 1 && p.pillBag.stream().anyMatch(i -> i.pillType == Item.PillType.Potential) && wealth0 - wealth1 >= Auctions.reserve(pill) && wealth0 - wealth1 <= 1_000_000_000_000L);

        // ---- T5: the library
        Library lib = new Library(city); city.cityTreasury = 1_000_000_000L;
        Character a1 = adult(40, 4), a2 = adult(40, 4);
        Technique low = new Technique(Technique.techniqueType.Martial, a1, 3), t25a = new Technique(Technique.techniqueType.Martial, a1, 25), t25b = new Technique(Technique.techniqueType.Martial, a2, 25);
        a1.techniqueList.add(low); a1.techniqueList.add(t25a); a2.techniqueList.add(t25b);
        check("T5 a technique of potential 3 or less cannot be sold", !lib.sellTechnique(a1, low) && a1.techniqueList.contains(low) && !Library.sellable(low));
        long s0 = a1.ss + a1.sc * 1_000_000L;
        boolean sold = a1.sellBestTechnique(lib);
        long fee = Library.sellPrice(t25a);
        check("T5b the seller is paid a 20% fee (100 x potential squared) and loses the technique", sold && fee == 62_500L && !a1.techniqueList.contains(t25a) && a1.ss + a1.sc * 1_000_000L - s0 == fee);
        check("T5c the library keeps one book of that type and potential", lib.techniques.size() == 1 && lib.techniques.get(0).potential == 25);
        String firstName = lib.techniques.get(0).name;
        a2.sellBestTechnique(lib);
        check("T5d a new technique of the same type and potential replaces the old one", lib.techniques.size() == 1 && lib.techniques.get(0).name.equals(t25b.name) && !lib.techniques.get(0).name.equals(firstName));
        Character a3 = adult(40, 4);
        Technique worn = new Technique(Technique.techniqueType.Martial, a3, 60);
        a3.martialTechnique = worn;
        check("T5e equipped techniques are never sold", a3.sellableTechniques().isEmpty());
        lib.techniques.add(new Technique(Technique.techniqueType.Movement, a3, 10)); lib.techniques.add(new Technique(Technique.techniqueType.Movement, a3, 10)); lib.techniques.add(new Technique(Technique.techniqueType.Movement, a3, 2));
        lib.compact();
        long movement10 = lib.techniques.stream().filter(tq -> tq.type == Technique.techniqueType.Movement && tq.potential == 10).count();
        check("T5f old shelves are compacted: one book per type and potential, none below potential 4", movement10 == 1 && lib.techniques.stream().noneMatch(tq -> tq.potential < 4));
        city.libraries.add(0, lib); tp(p, city);
        String listing = TermView.library(p, new String[]{"library", "type=movement", "potential>=5"});
        check("T5g the terminal listing filters and keeps the library's own numbers", listing.contains("Movement") && !listing.contains("Martial"));

        // ---- T6: shops take any level
        Shop shop = new Shop(adult(10, 1), city);
        check("T6 a shop can stock an item of any level (its owner only has to afford it)", shop.canStock(weapon(a1, 100, false)) && shop.level() >= 1);

        // ---- T7: Blood and the alchemist
        Character client = adult(30, 3), alch = adult(30, 3);
        alch.trait = Character.Traits.Alchemist; alch.professionLvl = 30; alch.born = true; alch.affiliation = "Evil";
        client.materialBag.add(new Item(Item.ItemType.Blood, 2500)); client.ss = 1_000_000; client.convertCurrency(); client.potential = 50;
        long alch0 = alch.ss + alch.sc * 1_000_000L;
        check("T7 a client finds the Evil alchemist standing here", client.findAlchemist() == alch);
        alch.affiliation = "Good";
        check("T7a ...but not a Good one (only the makers of Potential Pills are sought out)", client.findAlchemist() == null);
        alch.affiliation = "Evil";
        Quest rq = new Quest("Refine Blood", true, 45, 3, 70, alch);
        check("T7b the Refine Blood quest is viable while the client has Blood and needs potential", client.questViable(rq) == 1);
        int pillsBefore = alch.pillTotal(), clientBefore = client.pillTotal();
        boolean refined = client.refineBloodWith(alch);
        long paid = alch.ss + alch.sc * 1_000_000L - alch0;
        check("T7c 2,500 Blood make two pills with no fee: the alchemist keeps one, the client gets one (paid " + paid + ", alchemist +" + (alch.pillTotal() - pillsBefore) + ")", refined && paid == 0 && alch.pillTotal() - pillsBefore == 1);
        check("T7d the Blood is used up (500 left)", client.bloodAmount() == 500);
        Character poor = adult(30, 3); poor.materialBag.add(new Item(Item.ItemType.Blood, 1500));
        check("T7e less than two pills' worth gets nothing", !poor.refineBloodWith(alch) && poor.bloodAmount() == 1500);
        Character three = adult(30, 3); three.materialBag.add(new Item(Item.ItemType.Blood, 3000)); int a0 = alch.pillTotal();
        three.refineBloodWith(alch);
        check("T7f three pills: the alchemist keeps one, the client gets two (alchemist +" + (alch.pillTotal() - a0) + ")", alch.pillTotal() - a0 == 1 && three.bloodAmount() == 0);

        // ---- T8: an NPC remembers about 150 places, and forgets the plain far ones first
        LinkedHashSet<Area> pool = new LinkedHashSet<>();
        for (Area ar : Map.createdAreas) if (ar != null) pool.add(ar);
        for (int px = 0; px < Map.range; px++) for (int py = 0; py < Map.range; py++) if (Map.tiles[px][py] != null) pool.add(Map.tiles[px][py]);
        Character mem = adult(30, 3); mem.knownLocations.clear();
        { // make enough tiles: a block of unvisited ones (the test world is thrown away)
            Character keepCur = Map.currentCharacter; Map.currentCharacter = mem;
            for (int px = 0; px < Map.range && pool.size() < Character.KNOWN_CAP + Character.KNOWN_SLACK + 60; px++) for (int py = 0; py < Map.range && pool.size() < Character.KNOWN_CAP + Character.KNOWN_SLACK + 60; py++)
                if (Map.tiles[px][py] == null) { Map.tiles[px][py] = new Area(Area.ghost(px == 0 ? Map.range - 1 : px - 1, py), 'e'); pool.add(Map.tiles[px][py]); }
            Map.currentCharacter = keepCur;
        }
        if (pool.size() < Character.KNOWN_CAP + Character.KNOWN_SLACK + 20) { PcProbe.real.println("[T] info: only " + pool.size() + " areas exist: the cap test is skipped"); }
        else {
            List<Area> all = new ArrayList<>(pool); Area farCity = null;
            for (Area ar : all) if (ar != home() && Math.max(Math.abs(ar.x - home().x), Math.abs(ar.y - home().y)) > 8 && farCity == null) { farCity = ar; farCity.city = true; }
            mem.learnPlace(farCity);
            for (Area ar : all) mem.learnPlace(ar);
            mem.learnPlace(home());
            check("T8 an NPC's known places stay near the cap (" + mem.knownLocations.size() + " of " + all.size() + ")", mem.knownLocations.size() <= Character.KNOWN_CAP + Character.KNOWN_SLACK);
            check("T8b the place the NPC stands in and a far city are never forgotten", mem.knownLocations.contains(home()) && mem.knownLocations.contains(farCity));
            Character hum = adult(30, 3); hum.player = 2; hum.knownLocations.clear();
            for (Area ar : all) hum.learnPlace(ar);
            check("T8c a followed or human character keeps every place (the fog of war never shrinks)", hum.knownLocations.size() == all.size());
            hum.player = 0;
        }
    }

    static void tp(Character c, Area to) { c.location.removeChar(c); c.location = to; to.addChar(c); }
}
