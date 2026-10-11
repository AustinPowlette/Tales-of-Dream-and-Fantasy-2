package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-08: the quest profiles and fatigue (QuestLife), the eleven new quests (Visit Family, Teach Disciple, Courtship, Trade Run, Pilgrimage, Meditation Retreat, Tame Beast, Buy Pills, Attend Auction,
 *  Found City, Found Sect), the Gatherer's and Miner's trade, the craftsmen's materials market, and the preference for spirit veins. */
public class QuestLifeProbe {
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
        c.id = Map.characters.size(); // the list index is the id (the game keeps it so; a probe that adds by hand must too, or charById finds someone else)
        GameDriver.UID = Math.max(GameDriver.UID, c.id + 1);
        Map.characters.add(c); Map.population++;
        return c;
    }
    static Character adultTrait(Character.Traits tr) { Character c = adult(30, 3); c.trait = tr; return c; }
    static void tp(Character c, Area to) { c.location.removeChar(c); c.location = to; to.addChar(c); }

    static Area plain(Set<Area> not) {
        for (Area a : new ArrayList<>(Map.createdAreas)) if (a != null && !a.isOriginis() && !a.city && a.sect == null && a.dungeonType == null && a.loreSite == null && !a.spiritualVein && !"Ocean".equals(a.biomem) && !not.contains(a)) return a;
        return null;
    }

    static boolean queued(Character c, String name) {
        if (c.currentQuest != null && name.equals(c.currentQuest.name)) return true;
        for (Quest q : c.targetQuests) if (name.equals(q.name)) return true;
        return false;
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        GameDriver.frozenNpcs = true;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        p.caution = 0; p.maxAge = 100000; p.pc().squadPolicy = 2; p.pc().friendPolicy = 1;
        GameDriver.time = Math.max(GameDriver.time, 100);
        Map.currentCharacter = p;
        Area city = home(); city.city = true; city.population = 2000;
        if (city.backpack == null) city.backpack = new ArrayList<>();
        Settlements.cityTarget = 1000; Settlements.sectTarget = 1000; // plenty of room unless a test closes it
        // tiles: the world has only the nine start tiles (all cities or sects) until someone walks: make plain ones
        for (int k = 2; k <= 12; k++) for (int[] d : new int[][]{{k, 0}, {0, k}, {-k, 0}, {0, -k}, {k, 1}}) {
            int x = Math.floorMod(city.x + d[0], Map.range), y = Math.floorMod(city.y + d[1], Map.range);
            if (Map.tiles[x][y] == null) synchronized (Map.tileLock) { if (Map.tiles[x][y] == null) Map.tiles[x][y] = new Area(Area.ghost(x == 0 ? Map.range - 1 : x - 1, y), 'e'); }
        }
        Set<Area> used = new HashSet<>();
        Area away = plain(used); used.add(away);
        tp(p, away); // the player is out of the way of the auction tests

        // ---- P: families and profiles
        boolean allFam = true; StringBuilder nofam = new StringBuilder();
        for (String n : QuestLife.OWN) if (QuestLife.family(n) < 0) { allFam = false; nofam.append(n).append(' '); }
        check("P1 every new quest belongs to a family " + nofam, allFam && QuestLife.family("Hunt Beast") == QuestLife.ADV && QuestLife.family("Study Trip") == QuestLife.GROW && QuestLife.family("Disperse") == -1);
        boolean sums = true;
        for (Character.Traits tr : Character.Traits.values()) { Character c = adultTrait(tr); double s = 0; for (double v : QuestLife.target(c)) s += v; if (Math.abs(s - 1) > 1e-9) sums = false; }
        check("P2 every trait's target shares add up to 1", sums);
        Character baseline = adultTrait(Character.Traits.Cheater); baseline.affiliation = "Neutral";
        Character mer = adultTrait(Character.Traits.Merchant), fri = adultTrait(Character.Traits.Friendly), mus = adultTrait(Character.Traits.Musician), spv = adultTrait(Character.Traits.SpiritVein), lea = adultTrait(Character.Traits.Leader);
        for (Character x : new Character[]{mer, fri, mus, spv, lea}) x.affiliation = "Neutral"; // the alignment bends the shares too: compare like with like
        double[] tm = QuestLife.target(mer), tf = QuestLife.target(fri), tmu = QuestLife.target(mus), ts = QuestLife.target(spv), tl = QuestLife.target(lea), tb = QuestLife.target(baseline);
        check("P3 a Merchant trades most, a Friendly keeps company, a Musician travels, a Spirit Vein meditates, a Leader keeps people round them",
                tm[QuestLife.TRADE] > 2 * tb[QuestLife.TRADE] && tf[QuestLife.SOC] > tb[QuestLife.SOC] * 1.5 && tmu[QuestLife.JOURN] > 2 * tb[QuestLife.JOURN] && ts[QuestLife.CULT] > 1.5 * tb[QuestLife.CULT] && tl[QuestLife.SOC] > 1.2 * tb[QuestLife.SOC] && tl[QuestLife.ADV] < 0.9 * tb[QuestLife.ADV]);
        Character young = adultTrait(Character.Traits.Cheater); young.age = 20; Character old = adultTrait(Character.Traits.Cheater); old.age = 90; old.maxAge = 100; Character evil = adultTrait(Character.Traits.Cheater); evil.affiliation = "Evil"; Character good = adultTrait(Character.Traits.Cheater); good.affiliation = "Good";
        check("P4 the young adventure more and meditate less than the old; the Evil fight more than the Good",
                QuestLife.target(young)[QuestLife.ADV] > QuestLife.target(old)[QuestLife.ADV] && QuestLife.target(old)[QuestLife.CULT] > QuestLife.target(young)[QuestLife.CULT] && QuestLife.target(evil)[QuestLife.FIGHT] > QuestLife.target(good)[QuestLife.FIGHT]);

        // ---- F: fatigue
        Character f = adult(30, 3); f.trait = Character.Traits.Cheater; f.age = 40;
        QuestLife.tick(f);
        double fresh = QuestLife.interest(f, QuestLife.ADV);
        check("F1 a character at its target is neither tired nor hungry (interest " + String.format("%.2f", fresh) + ")", fresh > 0.8 && fresh < 1.25);
        f.currentQuest = new Quest("Hunt Beast", true, 50, 3, 70, home());
        for (int i = 0; i < 300; i++) QuestLife.tick(f);
        double tired = QuestLife.interest(f, QuestLife.ADV), hungry = QuestLife.interest(f, QuestLife.SOC);
        check("F2 300 weeks of hunting tire it of adventure (" + String.format("%.2f", tired) + ") and make it hungry for company (" + String.format("%.2f", hungry) + ")", tired < 0.5 && hungry > 1.5);
        int tiredHits = 0, hungryHits = 0;
        for (int i = 0; i < 20000; i++) { if (QuestLife.roll(f, "Hunt Beast", 20)) tiredHits++; if (QuestLife.roll(f, "Find Person", 20)) hungryHits++; }
        check("F3 the tired one thinks of hunting far less often than the hungry one of company (" + tiredHits + " against " + hungryHits + " in 20,000)", tiredHits * 5 < hungryHits);
        java.util.PriorityQueue<Quest> q = new java.util.PriorityQueue<>();
        q.add(new Quest("Hunt Beast", true, 60, 3, 70, home())); q.add(new Quest("Find Person", true, 20, 1, 34, adult(10, 1)));
        Quest got = QuestLife.pick(f, q);
        check("F4 the tired character takes the person to visit before the hunt, though the hunt has the higher priority", got != null && "Find Person".equals(got.name));
        java.util.PriorityQueue<Quest> q2 = new java.util.PriorityQueue<>();
        q2.add(new Quest("Hunt Beast", true, 60, 3, 70, home()));
        check("F5 ...and when only the hunt waits it is not taken (the character rests instead)", QuestLife.pick(f, q2) == null && q2.size() == 1);
        java.util.PriorityQueue<Quest> q3 = new java.util.PriorityQueue<>();
        q3.add(new Quest("Possess Womb", false, 80, 3, 100, adult(10, 1))); q3.add(new Quest("Heaven Treasure", true, 70, 5, 85, home()));
        check("F6 an urgent quest (maximum priority 80 or more) is taken however tired the character is", QuestLife.pick(f, q3) != null);
        Character calm = adult(30, 3); calm.trait = Character.Traits.Cheater; QuestLife.tick(calm);
        java.util.PriorityQueue<Quest> q4 = new java.util.PriorityQueue<>();
        q4.add(new Quest("Hunt Beast", true, 60, 3, 70, home())); q4.add(new Quest("Find Person", true, 20, 1, 34, adult(10, 1)));
        check("F7 a rested character takes the hunt (the higher priority)", "Hunt Beast".equals(QuestLife.pick(calm, q4).name));
        Character beast = adult(30, 3); beast.trait = Character.Traits.Beast;
        check("F8 beasts and monsters are not steered", QuestLife.interest(beast, QuestLife.ADV) == 1);

        // ---- V: Visit Family, Teach Disciple, Courtship
        Area t1 = plain(used); used.add(t1);
        Character parent = adult(40, 3), kid = adult(10, 1); kid.age = 20; parent.children.add(kid); kid.mother = parent; kid.father = null; parent.gender = "Female";
        tp(kid, t1);
        boolean thought = false;
        for (int i = 0; i < 4000 && !thought; i++) { parent.nextRoutineWeek = 0; parent.currentQuest = null; QuestLife.tick(parent); QuestLife.consider(parent); thought = queued(parent, "Visit Family"); }
        check("V1 a parent with a child far away thinks of visiting (the quest appears)", thought);
        Quest vf = new Quest("Visit Family", true, 28, 2, 50, kid);
        check("V2 the visit is viable while the relative lives", parent.questViable(vf) == 1);
        check("V3 the visit does nothing until the parent is there (still going)", parent.targetQuests(vf) == 0 && vf.progress == 0);
        tp(parent, t1);
        Relationship before = parent.relationshipList.contains(kid.id); int v0 = before == null ? 0 : before.value;
        int vr = 0; for (int i = 0; i < 4; i++) vr = parent.targetQuests(vf);
        check("V4 at the relative's side the visit lasts four turns, then the bond has grown", vr == 1 && vf.progress == 4 && parent.relationshipList.contains(kid.id).value >= Math.min(3, v0 + 1) && parent.nextRoutineWeek > GameDriver.time);
        // teach
        Character master = adult(60, 4); master.age = 60; Character pupil = adult(10, 1); pupil.age = 20; tp(pupil, t1); tp(master, t1);
        master.techniqueList.add(new Technique(Technique.techniqueType.Martial, master, 40));
        master.techniqueList.get(master.techniqueList.size() - 1).potential = 90;
        Organization sect = new Organization(master); pupil.sect = sect; sect.members.add(pupil);
        long sp0 = master.sectPoints;
        Quest tq0 = new Quest("Teach Disciple", true, 30, 2, 52, pupil); int tr0 = 0; for (int i = 0; i < 4; i++) tr0 = master.targetQuests(tq0);
        boolean taught = tr0 == 1;
        boolean learned = false; for (Technique tq : pupil.techniqueList) if (tq.name.equals(master.techniqueList.get(master.techniqueList.size() - 1).name)) learned = true;
        check("V5 teaching: the disciple learns the master's best technique, the master earns sect points, they grow closer", taught && learned && master.sectPoints >= sp0 + 5 && master.relationshipList.contains(pupil.id) != null);
        boolean teachThought = false;
        for (int i = 0; i < 6000 && !teachThought; i++) { master.nextRoutineWeek = 0; master.currentQuest = null; QuestLife.consider(master); teachThought = queued(master, "Teach Disciple"); }
        check("V6 a master with a weaker disciple thinks of teaching", teachThought);
        pupil.sect = null; master.sect = null; sect.members.clear();
        // courtship
        Area t2 = plain(used); used.add(t2);
        Character man = adult(30, 3); man.gender = "Male"; man.age = 25; Character woman = adult(30, 3); woman.gender = "Female"; woman.age = 24; tp(man, t2); tp(woman, t2);
        man.mother = null; man.father = null; woman.mother = null; woman.father = null; man.siblings.clear(); woman.siblings.clear();
        Character other = adult(20, 1); other.gender = "Male"; other.age = 25;
        check("V7 two unmarried adults of the other gender and no kinship are suitors; two men are not", QuestLife.suitor(man, woman) && QuestLife.suitor(woman, man) && !QuestLife.suitor(man, other));
        Quest court = new Quest("Courtship", true, 32, 2, 52, woman);
        int steps = 0;
        for (int i = 0; i < 20 && man.spouse == null; i++) { if (man.questViable(court) == 1 && man.targetQuests(court) == 1) { steps++; court = new Quest("Courtship", true, 32, 2, 52, woman); } }
        check("V8 a few courtships (" + steps + ") end in a marriage", man.spouse == woman && woman.spouse == man);
        check("V9 a married man is no suitor, and a courtship of someone's spouse is not viable", !QuestLife.suitor(adult(20, 1), woman) && man.questViable(court) == 0);
        boolean courtThought = false;
        Character single = adult(30, 3); single.gender = "Female"; single.age = 25; single.mother = null; single.father = null; tp(single, t2);
        Character bachelor = adult(30, 3); bachelor.gender = "Male"; bachelor.age = 27; bachelor.mother = null; bachelor.father = null; tp(bachelor, t2);
        for (int i = 0; i < 6000 && !courtThought; i++) { single.nextRoutineWeek = 0; single.currentQuest = null; QuestLife.consider(single); courtThought = queued(single, "Courtship"); }
        check("V10 a single adult thinks of courting someone who stands there", courtThought);

        // ---- T: trade run, the craftsmen's market, gatherers and miners
        Character gath = adultTrait(Character.Traits.Gatherer); gath.materialBag.clear(); gath.materialBag.add(new Item(Item.ItemType.Herb, 3, 5000)); gath.ss = 0; gath.sc = 0;
        Area mkt = city; mkt.cityTreasury = 500_000; mkt.materialBag = new ArrayList<>(); tp(gath, mkt);
        check("T1 a Gatherer with 5,000 herbs has " + QuestLife.surplus(gath) + " to spare", QuestLife.surplus(gath) == 5000 - 300);
        Quest tr = new Quest("Trade Run", true, 36, 2, 58, mkt);
        check("T2 the trade run is viable while the city can pay", gath.questViable(tr) == 1);
        long tre0 = mkt.cityTreasury;
        gath.targetQuests(tr); boolean ran = gath.targetQuests(tr) == 1; // the first turn is haggling
        long paid = gath.ss + gath.sc * 1_000_000L;
        check("T3 the run sells the spare herbs (kept 300), the treasury pays (" + paid + " ss, a fifth of it at most), the city keeps the herbs", ran && gath.materialBag.get(0).quantity == 300 && paid > 0 && tre0 - mkt.cityTreasury == paid && paid <= tre0 / 5 && !mkt.materialBag.isEmpty());
        Character smith = adultTrait(Character.Traits.Blacksmith); smith.materialBag.clear(); smith.ss = 1_000_000; smith.sc = 0; smith.convertCurrency(); tp(smith, mkt);
        mkt.materialBag.clear(); mkt.materialBag.add(new Item(Item.ItemType.Metal, 2, 3000));
        boolean bought = QuestLife.buyMaterials(smith, Item.ItemType.Metal);
        long metal = 0; for (Item it : smith.materialBag) if (it.itemType == Item.ItemType.Metal) metal += it.quantity;
        check("T4 a craftsman short of metal buys it from the city's stock (1,500 units at a time)", bought && metal == 1500 && mkt.materialBag.get(0).quantity == 1500);
        Character none = adultTrait(Character.Traits.Blacksmith); none.ss = 0; none.sc = 0; tp(none, mkt);
        check("T5 ...and one who cannot pay buys nothing", !QuestLife.buyMaterials(none, Item.ItemType.Metal));
        Area wild = plain(used); used.add(wild); wild.herbMaterials = 1_000_000; wild.metalMaterials = 1_000_000;
        Character g2 = adultTrait(Character.Traits.Gatherer); g2.materialBag.clear(); tp(g2, wild); Character m2 = adultTrait(Character.Traits.Miner); m2.materialBag.clear(); tp(m2, wild);
        for (int i = 0; i < 12; i++) { g2.quest(); m2.quest(); }
        boolean gotHerb = g2.materialBag.stream().anyMatch(i -> i.itemType == Item.ItemType.Herb), gotMetal = m2.materialBag.stream().anyMatch(i -> i.itemType == Item.ItemType.Metal);
        check("T6 a Gatherer gathers herbs and a Miner mines metal as their trade (it used to be nothing)", gotHerb && gotMetal);

        // ---- R: pilgrimage and meditation retreat, and the spirit vein
        Area vein = plain(used); used.add(vein); vein.spiritualVein = true;
        Character pil = adult(30, 3); tp(pil, vein);
        Quest pq = new Quest("Pilgrimage", true, 30, 2, 52, vein);
        int[] rets = new int[6]; for (int i = 0; i < 6; i++) rets[i] = pil.targetQuests(pq);
        check("T7 a pilgrimage stays six turns (0 0 0 0 0 1): " + Arrays.toString(rets), rets[0] == 0 && rets[4] == 0 && rets[5] == 1);
        Character med = adult(30, 3); med.spouse = null; Character mate = adult(30, 3); med.spouse = mate; mate.spouse = med; med.gender = "Male"; mate.gender = "Female";
        Area far = plain(used); used.add(far); tp(mate, far); tp(med, vein);
        Quest rq = new Quest("Meditation Retreat", true, 40, 3, 60, vein);
        int r1 = med.targetQuests(rq);
        check("R1 the retreat starts and calls the spouse, who gets the quest too", r1 == 0 && queued(mate, "Meditation Retreat"));
        int last = 0; for (int i = 0; i < 7; i++) last = med.targetQuests(rq);
        check("R2 it ends after eight turns (progress " + rq.progress + ")", last == 1 && rq.progress == 8);
        Character sitter = adult(30, 3); tp(sitter, vein); boolean sat = false;
        for (int i = 0; i < 3000 && !sat; i++) { sitter.nextRoutineWeek = 0; sitter.currentQuest = null; QuestLife.consider(sitter); sat = queued(sitter, "Meditation Retreat"); }
        check("R3 standing on a spirit vein, a character sits down to meditate", sat);
        Character walker = adult(30, 3); tp(walker, away); walker.knownLocations.add(vein); boolean goes = false;
        for (int i = 0; i < 6000 && !goes; i++) { walker.nextRoutineWeek = 0; walker.currentQuest = null; walker.targetQuests.clear(); QuestLife.consider(walker); goes = queued(walker, "Meditation Retreat"); }
        check("R4 a character who knows of a vein sets out for it", goes);

        // ---- S: founding sites and the vein's pull
        Area vein2 = plain(used); used.add(vein2); vein2.spiritualVein = true;
        Character founder = adult(60, 4); founder.age = 40; tp(founder, vein2);
        Area siteC = QuestLife.foundingSite(founder, true);
        check("S1 a city is founded on the spirit vein under the founder's feet rather than on a nearer plain tile", siteC == vein2);
        Quest fc = new Quest("Found City", true, 38, 2, 58, vein2);
        check("S2 the founding quest is viable and, on arrival, founds the city", founder.questViable(fc) == 1 && founder.targetQuests(fc) == 1 && vein2.city);
        Area vein3 = plain(used); used.add(vein3); vein3.spiritualVein = true; tp(founder, vein3); founder.sect = null;
        Quest fs = new Quest("Found Sect", true, 38, 2, 58, vein3);
        check("S3 ...and a sect the same way", founder.questViable(fs) == 1 && founder.targetQuests(fs) == 1 && vein3.sect != null);
        Settlements.cityTarget = 0; Settlements.sectTarget = 0; Settlements.cityCount.set(100); Settlements.sectCount.set(100);
        check("S4 with no room in the world the quests are not viable and are not thought of", founder.questViable(new Quest("Found City", true, 38, 2, 58, plain(used))) == 0 && !Settlements.cityRoom());
        Settlements.cityTarget = 100000; Settlements.sectTarget = 100000; Settlements.cityCount.set(0); Settlements.sectCount.set(0);
        int onVein = 0, offVein = 0;
        Area plainA = plain(used); used.add(plainA); Area veinA = plain(used); used.add(veinA); veinA.spiritualVein = true;
        for (int i = 0; i < 6000; i++) {
            Character cc = adult(30, 3); cc.professionLvl = 1;
            tp(cc, veinA); veinA.city = false; cc.cityLeadership(); if (veinA.city) { onVein++; veinA.city = false; veinA.cityOwner = null; veinA.libraries.clear(); }
            tp(cc, plainA); plainA.city = false; cc.cityLeadership(); if (plainA.city) { offVein++; plainA.city = false; plainA.cityOwner = null; plainA.libraries.clear(); }
            Map.characters.remove(cc);
        }
        check("S5 a Major Lvl 2 character founds a city on a spirit vein about five times as often (" + onVein + " against " + offVein + " in 6,000)", onVein > offVein * 3 && onVein > 100);

        // ---- B: pills and the auction
        Area shop = city; shop.pillBag = new ArrayList<>();
        Character buyer = adult(30, 3); buyer.majorLvl = 3; buyer.ss = 10_000_000; buyer.convertCurrency(); tp(buyer, shop);
        shop.pillBag.add(new Item(Item.ItemType.Pill, Item.PillType.Cultivation, 4, 3));
        Quest bp = new Quest("Buy Pills", true, 38, 3, 60, shop);
        check("B1 the next-realm pill is on sale: the Buy Pills trip is viable", QuestLife.pillFor(buyer, shop) != null && buyer.questViable(bp) == 1);
        boolean got1 = buyer.targetQuests(bp) == 1 && buyer.pillBag.stream().anyMatch(i -> i.pillType == Item.PillType.Cultivation && i.lvl == 4);
        check("B2 on arrival the pill is bought (and paid to the city)", got1);
        Character broke = adult(30, 3); broke.majorLvl = 3; broke.ss = 0; broke.sc = 0;
        check("B3 someone who cannot pay has no trip", QuestLife.pillFor(broke, shop) == null);
        // auction: only bidders who are near (or have come) take part
        Area ac = city; ac.auctionBag = new ArrayList<>();
        Item lot = new Item(buyer, Item.WeaponType.Spear, 12); ac.auctionBag.add(lot); ac.auctionSince = GameDriver.time - 1;
        Character bidder = adult(30, 3); bidder.equippedWeapon = null; bidder.backpack.clear(); bidder.ss = 0; bidder.sc = 5_000_000_000L; tp(bidder, ac); bidder.knownLocations.add(ac);
        Quest att = new Quest("Attend Auction", true, 42, 3, 62, ac);
        check("B4 an auction is coming: the Attend Auction trip is viable and the bidder wants the lot", bidder.questViable(att) == 1 && Auctions.wants(bidder, lot, bidder.sc * 1_000_000L));
        int[] ar = new int[3]; for (int i = 0; i < 3; i++) ar[i] = bidder.targetQuests(att);
        check("B5 the bidder waits in the city for the auction (0 0 0): " + Arrays.toString(ar) + " progress " + att.progress, ar[0] == 0 && ar[2] == 0 && att.progress == 3);
        Area farTile = null;
        for (Area a2 : new ArrayList<>(Map.createdAreas)) if (a2 != null && a2 != ac && !a2.city && a2.sect == null && PlayerControl.distance(a2, ac) > 8) { farTile = a2; break; }
        far = farTile != null ? farTile : far;
        Character distant = adult(30, 3); distant.equippedWeapon = null; distant.backpack.clear(); distant.ss = 0; distant.sc = 9_000_000_000L; tp(distant, far); distant.knownLocations.add(ac);
        boolean farEnough = PlayerControl.distance(far, ac) > 8;
        for (Character o : new ArrayList<>(Map.characters)) if (o != null && o != bidder && o != distant && o.player == 0) { o.ss = 0; o.sc = 0; o.dc = 0; } // no other bidder: the bids are random now, a poorer neighbour could beat the test's bidder
        Auctions.hold(ac);
        check("B6 the auction goes to the bidder who is there, not to the richer one who is far away (distance " + PlayerControl.distance(far, ac) + ")", farEnough && distant.equippedWeapon != lot && !distant.backpack.contains(lot) && (bidder.equippedWeapon == lot || bidder.backpack.contains(lot)));

        // ---- N: more supply (2026-10-08): market trips, gathering trips, a teacher, sightseeing, couriers, pills to sell
        Character nomeal = adult(30, 3); nomeal.ss = 1_000_000; nomeal.convertCurrency(); nomeal.fx = null; nomeal.goods = null; tp(nomeal, away); nomeal.knownLocations.add(city);
        Life.market(city).add(new Item(Item.ItemType.Food, 1, 6, "Plain Bread"));
        boolean mt = false;
        for (int i = 0; i < 6000 && !mt; i++) { nomeal.nextRoutineWeek = 0; nomeal.currentQuest = null; nomeal.targetQuests.clear(); QuestLife.consider(nomeal); mt = queued(nomeal, "Market Trip"); }
        check("N1 someone with no meal thinks of a trip to a city market that sells one", mt);
        tp(nomeal, city);
        Quest mq = new Quest("Market Trip", true, 30, 2, 52, city); nomeal.targetQuests(mq);
        boolean mtDone = nomeal.targetQuests(mq) == 1 && Life.firstOf(nomeal, Item.ItemType.Food) != null;
        check("N2 at the market the meal is bought", mtDone);
        Area rich = plain(used); used.add(rich); rich.herbMaterials = 5_000_000;
        Area poorTile = plain(used); used.add(poorTile); poorTile.herbMaterials = 0;
        Character picker = adultTrait(Character.Traits.Gatherer); picker.materialBag.clear(); tp(picker, poorTile); picker.knownLocations.add(rich);
        boolean gt = false;
        for (int i = 0; i < 6000 && !gt; i++) { picker.nextRoutineWeek = 0; picker.currentQuest = null; picker.targetQuests.clear(); QuestLife.consider(picker); gt = queued(picker, "Gathering Trip"); }
        check("N3 a Gatherer on poor ground sets out for a rich one", gt);
        Quest gq = new Quest("Gathering Trip", true, 32, 2, 54, rich); tp(picker, rich);
        int[] gr = new int[8]; for (int i = 0; i < 8; i++) gr[i] = picker.targetQuests(gq);
        long herbs = 0; for (Item it : picker.materialBag) if (it.itemType == Item.ItemType.Herb) herbs += it.quantity;
        check("N4 the trip lasts eight turns of gathering (" + herbs + " herbs, last result " + gr[7] + ")", gr[6] == 0 && gr[7] == 1 && herbs > 0);
        Character pupil2 = adult(20, 2); Character sage = adult(60, 5); sage.age = 70; tp(sage, away); tp(pupil2, away);
        Technique tech = new Technique(Technique.techniqueType.Martial, sage, 50); tech.potential = 95; sage.techniqueList.add(tech);
        pupil2.changeRelationship(sage, 1);
        boolean st = false;
        for (int i = 0; i < 6000 && !st; i++) { pupil2.lvl = 20; sage.lvl = 60; pupil2.nextRoutineWeek = 0; pupil2.currentQuest = null; pupil2.targetQuests.clear(); QuestLife.consider(pupil2); st = queued(pupil2, "Seek Teacher"); }
        check("N5 a character with a friend far above thinks of seeking them as a teacher", st);
        int rel0 = pupil2.relationshipList.contains(sage.id).value;
        Quest skq = new Quest("Seek Teacher", true, 33, 2, 54, sage); int skr = 0; for (int i = 0; i < 5; i++) skr = pupil2.targetQuests(skq);
        boolean taughtMe = skr == 1;
        boolean copied = pupil2.techniqueList.stream().anyMatch(x -> x.name.equals(tech.name));
        check("N6 at the teacher's side the student learns the best technique", taughtMe && copied && pupil2.relationshipList.contains(sage.id).value >= Math.min(3, rel0 + 1));
        Area sight = plain(used); used.add(sight); sight.spiritualVein = true;
        Character tourist = adult(30, 3); tp(tourist, home()); tourist.knownLocations.add(sight);
        int dSight = PlayerControl.distance(home(), sight);
        boolean sg = dSight < 5 || dSight > 30;
        for (int i = 0; i < 6000 && !sg; i++) { tourist.nextRoutineWeek = 0; tourist.currentQuest = null; tourist.targetQuests.clear(); QuestLife.consider(tourist); sg = queued(tourist, "Sightseeing"); }
        check("N7 a notable place a few days away draws a tourist (distance " + dSight + ")", sg);
        Quest sq = new Quest("Sightseeing", true, 26, 2, 48, sight); tp(tourist, sight);
        int[] sr = new int[3]; for (int i = 0; i < 3; i++) sr[i] = tourist.targetQuests(sq);
        check("N8 the visit lasts three turns (0 0 1): " + Arrays.toString(sr), sr[0] == 0 && sr[1] == 0 && sr[2] == 1);
        Area bigCity = null;
        for (Area a2 : new ArrayList<>(Map.createdAreas)) if (a2 != null && !a2.isOriginis() && !a2.city && a2.sect == null && a2.loreSite == null && PlayerControl.distance(home(), a2) >= 8 && !used.contains(a2)) { bigCity = a2; break; }
        if (bigCity != null) {
            bigCity.city = true; bigCity.population = 5000; bigCity.cityTreasury = 200_000; used.add(bigCity);
            Character runner = adultTrait(Character.Traits.Runner); runner.ss = 0; runner.sc = 0; tp(runner, home()); runner.knownLocations.add(bigCity);
            boolean cr = false;
            for (int i = 0; i < 6000 && !cr; i++) { runner.nextRoutineWeek = 0; runner.currentQuest = null; runner.targetQuests.clear(); QuestLife.consider(runner); cr = queued(runner, "Courier Run"); }
            check("N9 a Runner takes word to a far city", cr);
            tp(runner, bigCity); long t0c = bigCity.cityTreasury;
            boolean crDone = runner.targetQuests(new Quest("Courier Run", true, 34, 2, 55, bigCity)) == 1;
            check("N10 the city pays a courier from its treasury (" + (t0c - bigCity.cityTreasury) + " ss)", crDone && runner.ss + runner.sc * 1_000_000L > 0 && bigCity.cityTreasury < t0c);
            Character nonRunner = adultTrait(Character.Traits.Merchant); tp(nonRunner, home()); nonRunner.knownLocations.add(bigCity);
            boolean nr = false;
            for (int i = 0; i < 3000 && !nr; i++) { nonRunner.nextRoutineWeek = 0; nonRunner.currentQuest = null; nonRunner.targetQuests.clear(); QuestLife.consider(nonRunner); nr = queued(nonRunner, "Courier Run"); }
            check("N11 only a Runner is a courier", !nr);
        } else check("N9 a far tile was found for the courier test", false);
        Character apothecary = adultTrait(Character.Traits.Alchemist); apothecary.majorLvl = 4; tp(apothecary, away); apothecary.knownLocations.add(city);
        for (int i = 0; i < 6; i++) apothecary.pillBag.add(new Item(Item.ItemType.Pill, Item.PillType.Cultivation, 2, 1));
        check("N12 pills of a realm already passed count as stock to sell", QuestLife.pillStock(apothecary) >= 4);
        boolean gs = false;
        for (int i = 0; i < 40 && !gs; i++) { apothecary.quest(); gs = queued(apothecary, "Go Sell") || apothecary.location == city; }
        check("N13 an Alchemist with pills to spare heads for a city to sell them", gs);

    }
}
