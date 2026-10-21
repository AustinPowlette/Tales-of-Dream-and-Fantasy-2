package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-20: how a city or a sect is destroyed (`Sieges`). S1 the standing guards (a sect keeps 2 + one per six members, to 10; a city 2 + one per 20,000 people, to 10); S2 a strong attacker beats the guards of a thin sect and razes it;
 *  S3 real defenders in the seat fight it in a row and a weak attacker is turned back (the sect holds, the attacker is hurt); S4 the people who stand for a place (members in a seat, the lord's circle in a city, not strangers);
 *  S5 only a beaten array comes down: a failed attempt leaves it; S6 the whole chain through `winCheck`: kills in a place with arrays never raze it, in a defended place without them the storms turn back a weak raider,
 *  S7 one storm at a time (no recursion), S8 a person far weaker than the attacker keeps out, S9 the world's 25-place floor still holds. */
public class SiegeProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    static void check(String n, boolean ok) { PcProbe.check(n, ok); }
    static Area home() { return Map.tiles[Map.range / 2][Map.range / 2]; }

    static Character adult(int lvl, int major) {
        Character c = new Character(home());
        c.age = 30; c.gender = "Male"; c.lvl = lvl; c.majorLvl = major; c.maxAge = 100000; c.caution = 50; c.sect = null;
        c.affiliation = "Good"; c.affiliationNum = 1; c.born = true; c.trait = Character.Traits.Friendly;
        c.relationshipList.relationships.clear();
        c.equippedWeapon = null; c.equippedClothing = null; c.martialTechnique = new Technique(Technique.techniqueType.Martial); c.movementTechnique = new Technique(Technique.techniqueType.Movement);
        c.id = Map.characters.size();
        GameDriver.UID = Math.max(GameDriver.UID, c.id + 1);
        Map.characters.add(c); Map.population++;
        return c;
    }
    static void tp(Character c, Area to) { c.location.removeChar(c); c.location = to; to.addChar(c); }
    static Area tile(int x, int y) {
        x = Math.floorMod(x, Map.range); y = Math.floorMod(y, Map.range);
        if (Map.tiles[x][y] == null) synchronized (Map.tileLock) { if (Map.tiles[x][y] == null) Map.tiles[x][y] = new Area(Area.ghost(x == 0 ? Map.range - 1 : x - 1, y), 'e'); }
        return Map.tiles[x][y];
    }
    static Area plain(Set<Area> not) {
        for (int k = 3; k < 24; k++) for (int[] d : new int[][]{{k, 3}, {3, k}, {-k, 3}, {k, -4}}) {
            Area a = tile(home().x + d[0], home().y + d[1]);
            if (a != null && !a.city && a.sect == null && a.dungeonType == null && a.loreSite == null && !a.spiritualVein && !not.contains(a)) return a;
        }
        return null;
    }
    static void member(Organization s, Character c) { s.members.add(c); c.sect = s; s.memberRanks.put(c.id, Organization.SectRank.OUTER_DISCIPLE); }
    static Organization sectAt(Area seat, int members, int lvl, Set<Area> used) {
        Character master = adult(lvl, lvl / 10); tp(master, tile(seat.x + 30, seat.y + 30));
        Organization s = new Organization(master);
        s.location = seat; seat.sect = s; seat.city = false;
        for (int i = 0; i < members; i++) { Character m = adult(lvl, lvl / 10); tp(m, tile(seat.x + 30, seat.y + 31)); member(s, m); }
        Settlements.sectCount.set(100);
        return s;
    }
    static Character evil(int lvl, Area at) { Character e = adult(lvl, lvl / 10); e.affiliation = "Evil"; e.affiliationNum = -1; e.kills = 10; tp(e, at); return e; }

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
        GameDriver.time = Math.max(GameDriver.time, 200);
        p.player = 0; // (the human of the probe stays out of it: nobody is told)
        Set<Area> used = new HashSet<>();

        // ---- S1: the guards
        Area s1 = plain(used); used.add(s1);
        Organization small = sectAt(s1, 4, 30, used), big = sectAt(plain(used), 35, 30, used);
        check("S1 a sect of 4 keeps 2 guards, a sect of 35 keeps 2 + 35 / 6 = 7 (" + Sieges.guards(s1) + ", " + Sieges.guards(big.location) + ")", Sieges.guards(s1) == 2 && Sieges.guards(big.location) == 7);
        Organization huge = sectAt(plain(used), 120, 30, used);
        check("S1b guards never exceed " + Sieges.MAX_GUARDS + " (" + Sieges.guards(huge.location) + ")", Sieges.guards(huge.location) == Sieges.MAX_GUARDS);
        Area cityA = plain(used); used.add(cityA);
        cityA.city = true; cityA.population = 60000; cityA.cityOwner = adult(40, 4); tp(cityA.cityOwner, cityA);
        check("S1c a city of 60,000 keeps 2 + 3 = 5 guards (" + Sieges.guards(cityA) + ") as strong as its lord (" + Sieges.guardPower(cityA) + " = 40 / 5 + 1)", Sieges.guards(cityA) == 5 && Sieges.guardPower(cityA) == 9);
        check("S1d a sect's guards are as strong as its five best members (" + Sieges.guardPower(s1) + " = 30 / 5 + 1)", Sieges.guardPower(s1) == 7);

        // ---- S2: a strong attacker razes a thin sect (two guards, 95% a bout at best: a few tries, each against a fresh sect)
        Area seat2 = null; Organization thin = null; boolean fell = false; int tries2 = 0;
        int razed0 = Sieges.razed;
        while (!fell && tries2 < 12) {
            tries2++;
            seat2 = plain(used); used.add(seat2);
            thin = sectAt(seat2, 3, 20, used);
            Character mighty = evil(100, seat2); mighty.tempLvlCheck();
            fell = Sieges.storm(mighty, seat2);
        }
        check("S2 an attacker of level 100 fights the two guards of a thin sect of level 20 and razes it (" + fell + " after " + tries2 + " tries, sect " + seat2.sect + ")", fell && seat2.sect == null && Sieges.razed == razed0 + 1);
        check("S2b the members are free of the sect", thin.members.isEmpty());

        // ---- S3: real defenders in a row turn a weak raider back
        Area seat3 = plain(used); used.add(seat3);
        Organization held = sectAt(seat3, 12, 60, used);
        List<Character> inside = new ArrayList<>();
        for (int i = 0; i < 4; i++) { Character m = adult(60, 6); m.affiliation = "Good"; tp(m, seat3); member(held, m); inside.add(m); }
        Character raider = evil(40, seat3);
        for (Character m : inside) { Relationship r = m.relationshipList.contains(raider.id); if (r != null) r.value = -3; }
        int repelled0 = Sieges.repelled, real0 = Sieges.realFights;
        boolean f3 = Sieges.storm(raider, seat3);
        check("S3 a raider of level 40 against four members of level 60 and a guard: the sect holds (" + f3 + ")", !f3 && seat3.sect == held && Sieges.repelled == repelled0 + 1);
        check("S3b real fights were fought (" + (Sieges.realFights - real0) + ") and the raider is dead or hurt", Sieges.realFights > real0 && (raider.dead || raider.cultivation < 1e9));
        check("S3c the place is under attack for a season (no second storm at once)", seat3.siegeUntil >= GameDriver.time + Defense.SIEGE_WEEKS - 1);

        // ---- S4: who stands for a place
        Area seat4 = plain(used); used.add(seat4);
        Organization s4 = sectAt(seat4, 2, 30, used);
        Character m1 = adult(30, 3), m2 = adult(30, 3), stranger = adult(30, 3), child = adult(30, 3); child.age = 9;
        tp(m1, seat4); member(s4, m1); tp(m2, seat4); member(s4, m2); tp(stranger, seat4); tp(child, seat4); member(s4, child);
        Character att4 = evil(50, seat4);
        List<Character> d4 = Sieges.defenders(seat4, att4);
        check("S4 the members in the seat stand for it, a stranger and a child do not (" + d4.size() + ")", d4.contains(m1) && d4.contains(m2) && !d4.contains(stranger) && !d4.contains(child) && d4.size() == 2);
        Area city4 = plain(used); used.add(city4);
        Character lord = adult(40, 4); lord.affiliation = "Good"; tp(lord, city4);
        city4.city = true; city4.cityOwner = lord; city4.population = 40000;
        Character shopper = adult(25, 2), friend = adult(30, 3), passer = adult(30, 3);
        tp(shopper, city4); tp(friend, city4); tp(passer, city4);
        Shop shop = new Shop(shopper, city4); shopper.ownedShops.add(shop); city4.shops.add(shop);
        lord.relationshipList.relationships.add(new Relationship(friend.id, 3));
        Character att4b = evil(60, city4);
        List<Character> c4 = Sieges.defenders(city4, att4b);
        check("S4b a city's lord, a shop owner and the lord's close friend stand for it; a passer-by does not (" + c4.size() + ")", c4.contains(lord) && c4.contains(shopper) && c4.contains(friend) && !c4.contains(passer));

        // ---- S5: arrays
        Area seat5 = plain(used); used.add(seat5);
        Character weakBreaker = adult(20, 2); tp(weakBreaker, seat5);
        seat5.arrayBag.add(new Item(weakBreaker, 100, Item.ItemType.Array)); // a level 100 array against a level 20 attacker: 0% to break it
        int tries = 0, down = 0;
        for (int i = 0; i < 12 && !weakBreaker.dead; i++) { tries++; if (weakBreaker.arrayBreakAttempt(seat5)) down++; }
        check("S5 an array far above the attacker is not worn down by failed attempts (" + tries + " tries, " + down + " broken, " + seat5.arrayBag.size() + " standing)", down == 0 && seat5.arrayBag.size() == 1);
        seat5.arrayBag.clear(); seat5.arrayBag.add(new Item(weakBreaker, 40, Item.ItemType.Array)); // a level 40 array (power 8) against a level 100 attacker (power 20): a sure break
        Character strongBreaker = adult(100, 10); tp(strongBreaker, seat5);
        boolean broke = strongBreaker.arrayBreakAttempt(seat5);
        check("S5b a strong attacker breaks it and it comes down (" + broke + ")", broke && seat5.arrayBag.isEmpty());

        // ---- S6: the chain through winCheck
        Area seat6 = plain(used); used.add(seat6);
        Organization s6 = sectAt(seat6, 20, 70, used);
        seat6.arrayBag.add(new Item(p, 50, Item.ItemType.Array));
        Character killer = evil(30, seat6);
        int storms0 = Sieges.storms;
        for (int i = 0; i < 700; i++) { Character v = adult(5, 0); tp(v, seat6); killer.winCheck(v, true); if (killer.dead) break; }
        check("S6 700 kills in a seat with an array standing never reach a storm (" + (Sieges.storms - storms0) + " storms)", Sieges.storms == storms0 && seat6.sect == s6);
        seat6.arrayBag.clear();
        for (int i = 0; i < 4; i++) { Character m = adult(70, 7); tp(m, seat6); member(s6, m); }
        Character killer2 = evil(30, seat6);
        storms0 = Sieges.storms; razed0 = Sieges.razed;
        int kills = 0;
        for (int i = 0; i < 900 && !killer2.dead; i++) { Character v = adult(5, 0); tp(v, seat6); killer2.winCheck(v, true); kills++; if (Sieges.storms > storms0) break; }
        check("S6b with no array a weak raider's kills wish the place down about 1 time in " + Settlements.RAZE_SECT_ODDS + " (a storm after " + kills + " kills)", Sieges.storms > storms0 || killer2.dead);
        check("S6c but the storm is turned back by the members in the seat: the sect stands (" + (seat6.sect == s6) + ", razed " + (Sieges.razed - razed0) + ")", seat6.sect == s6 && Sieges.razed == razed0);

        // ---- S7: one storm at a time
        Area seat7 = plain(used); used.add(seat7);
        sectAt(seat7, 2, 10, used);
        seat7.storming = true;
        int st0 = Sieges.storms;
        check("S7 a storm that is already going on is not started again", !Sieges.storm(evil(100, seat7), seat7) && Sieges.storms == st0);
        seat7.storming = false;

        // ---- S8: the weak keep out
        Area seat8 = plain(used); used.add(seat8);
        Organization s8 = sectAt(seat8, 1, 1, used);
        Character tiny = adult(2, 0); tp(tiny, seat8); member(s8, tiny);
        Character att8 = evil(100, seat8);
        int real1 = Sieges.realFights;
        Sieges.storm(att8, seat8);
        check("S8 a member far weaker than the attacker keeps out (" + (Sieges.realFights - real1) + " real fights) and does not die for nothing", Sieges.realFights == real1 && !tiny.dead);

        // ---- S9: the floor
        Settlements.sectCount.set(10); Settlements.cityCount.set(10);
        Area seat9 = plain(used); used.add(seat9);
        sectAt(seat9, 1, 5, used);
        Settlements.sectCount.set(10);
        check("S9 below 25 sects the world protects them (mayDestroy " + Settlements.mayDestroy(seat9) + ")", !Settlements.mayDestroy(seat9));
        Settlements.sectCount.set(100); Settlements.cityCount.set(100);

        System.out.println();
        PcProbe.real.println("[T] " + Sieges.counts());
        PcProbe.real.println("[T] DONE");
    }
}
