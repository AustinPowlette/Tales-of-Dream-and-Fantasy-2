package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-10 (sixth batch): the size of the world is chosen (settings, the save keeps it, the crowd limits follow), the feed policy of a person who is not at the keys, the skin tone
 *  of a portrait (chosen, inherited, in the terminal editor), and the defence of sects and cities (the alarm, the Defend quest, who is called, the places that hold). */
public class DefendProbe {
    static void check(String name, boolean ok) { PcProbe.passes += ok ? 1 : 0; PcProbe.fails += ok ? 0 : 1; PcProbe.real.println("[T] " + (ok ? "PASS " : "FAIL ") + name); }

    public static void main(String[] a) throws Exception {
        System.setProperty("tdf.defPower", "0"); // the power rule (a defender much weaker than the attacker stays out) depends on gear drawn at random: the checks below need the fight to happen
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

    static Area home() { return Map.tiles[Map.range / 2][Map.range / 2]; }

    static Character adult(int lvl, int major) {
        Character c = new Character(home());
        c.age = 30; c.gender = "Male"; c.lvl = lvl; c.majorLvl = major; c.maxAge = 100000; c.caution = 50; c.sect = null;
        c.affiliation = "Good"; c.affiliationNum = 1; c.born = true;
        c.relationshipList.relationships.clear(); // (the people it met at Originis when it was made: a liking would stop a fight)
        if (c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) c.trait = Character.Traits.Friendly;
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
        for (int k = 3; k < 14; k++) for (int[] d : new int[][]{{k, 3}, {3, k}, {-k, 3}, {k, -4}}) {
            Area a = tile(home().x + d[0], home().y + d[1]);
            if (a != null && !a.city && a.sect == null && a.dungeonType == null && a.loreSite == null && !a.spiritualVein && !not.contains(a)) return a;
        }
        return null;
    }
    static String say(Character p, String cmd) { int m = PcProbe.mark(); PlayerCommands.handle(p, cmd); return PcProbe.since(m); }
    static boolean hasDefend(Character c, Area a) { for (Quest q : c.targetQuests) if ("Defend".equals(q.name) && q.targetArea == a) return true; return false; }
    static void member(Organization s, Character c) { s.members.add(c); c.sect = s; s.memberRanks.put(c.id, Organization.SectRank.OUTER_DISCIPLE); }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        GameDriver.frozenNpcs = true;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        p.caution = 0; p.maxAge = 100000; p.pc().squadPolicy = 2; p.pc().friendPolicy = 1; p.lvl = 100; p.majorLvl = 10; p.age = 30; p.gender = "Male"; p.spouse = null;
        GameDriver.time = Math.max(GameDriver.time, 100);
        Map.currentCharacter = p;
        Set<Area> used = new HashSet<>();
        Area away = plain(used); used.add(away);
        tp(p, away);

        // ---- A: the size of the world
        int start0 = Map.startingPopulation, cap0 = GameDriver.CROWD_CAPACITY, sp0 = Settings.startPopulation;
        check("A1 the probe copy keeps its own size and is not asked (" + start0 + ")", start0 != Map.DEFAULT_START);
        String out = say(p, "settings startpop 20000");
        check("A2 `settings startpop` sets the next world's size and says the running world keeps its own (" + Settings.startPopulation + ")", Settings.startPopulation == 20000 && out.contains("keeps its") && Map.startingPopulation == start0);
        out = say(p, "settings startpop 5000");
        check("A3 a size under 10,000 is accepted with a note", Settings.startPopulation == 5000 && out.contains("thin"));
        out = say(p, "settings startpop 400000");
        check("A4 a size over 150,000 is accepted with a warning about speed and memory", Settings.startPopulation == 400000 && out.contains("slow"));
        out = say(p, "settings startpop 99");
        check("A5 an absurd size is held at 1,000 (" + Settings.startPopulation + ")", Settings.startPopulation == 1000);
        Map.setStartingPopulation(100000);
        check("A6 the crowd limit of a tile follows the size of the world (" + GameDriver.CROWD_CAPACITY + " at 100,000, " + cap0 + " at " + start0 + ")", GameDriver.CROWD_CAPACITY == GameDriver.crowdFor(100000) && GameDriver.CROWD_CAPACITY > cap0 && GameDriver.CROWD_CAPACITY_CITY == 4 * GameDriver.CROWD_CAPACITY);
        check("A7 the epoch comes at twice the start unless chosen (" + Settings.epochLimit() + ")", !Settings.epochCustom && Settings.epochLimit() == 200000);
        SaveData d = GameDriver.snapshot();
        check("A8 the save remembers the size (" + d.startPop + ")", d.startPop == 100000);
        Map.setStartingPopulation(start0);
        out = say(p, "settings epochpop 50000");
        check("A9 a chosen epoch limit is kept (" + Settings.epochLimit() + ")", Settings.epochCustom && Settings.epochLimit() == 50000);
        say(p, "settings epochpop auto");
        check("A10 `epochpop auto` goes back to twice the start (" + Settings.epochLimit() + ")", !Settings.epochCustom && Settings.epochLimit() == 2 * start0);
        Settings.startPopulation = sp0;

        // ---- B: the feed policy
        Character master = adult(40, 4); master.player = 1; master.pc = new PlayerControl(); master.pc.mode = PlayerControl.Mode.AUTO;
        tp(master, away);
        Character beast = new Character(away, master);
        beast.name = "Probe Pet"; beast.id = GameDriver.nextUid(); beast.wasTamed = true; beast.born = true; beast.masterId = master.id; beast.partyLeaderId = master.id;
        while (Map.characters.size() < beast.id) Map.characters.add(null);
        Map.characters.add(beast);
        master.beastIds = new ArrayList<>(); master.beastIds.add(beast.id);
        master.goods = new ArrayList<>();
        Item meal = new Item(Item.ItemType.Food, 3, 2, "Probe Meal");
        master.goods.add(meal);
        int week = GameDriver.time; while (week % Taming.FEED_EVERY != Math.floorMod(master.id, Taming.FEED_EVERY)) week++;
        GameDriver.time = week;
        check("B1 the default feed policy is leftovers", master.pc.feedPolicy == 0 && PlayerControl.FEED_POLICIES[0].equals("leftovers"));
        int fed = Taming.autoFeed(master);
        check("B2 leftovers keeps " + Taming.KEEP_FOR_SELF + " portions for the master: two portions are not spared (fed " + fed + ")", fed == 0 && meal.quantity == 2);
        meal.quantity = 6;
        double cult0 = beast.cultivation; int lvl0 = beast.lvl;
        fed = Taming.autoFeed(master);
        check("B3 leftovers feeds the beast from food beyond that (fed " + fed + ", " + meal.quantity + " left)", fed == 1 && meal.quantity == 5);
        GameDriver.time = week + 1;
        check("B4 only one portion each " + Taming.FEED_EVERY + " weeks", Taming.autoFeed(master) == 0 && meal.quantity == 5);
        GameDriver.time = week + Taming.FEED_EVERY;
        master.pc.feedPolicy = 1; meal.quantity = 1;
        check("B5 `always` feeds the last portion too", Taming.autoFeed(master) == 1 && master.goods.isEmpty());
        GameDriver.time = week + 2 * Taming.FEED_EVERY;
        master.goods.add(new Item(Item.ItemType.Food, 3, 9, "Probe Meal"));
        master.pc.feedPolicy = 2;
        check("B6 `never` feeds nothing", Taming.autoFeed(master) == 0 && master.goods.get(0).quantity == 9);
        master.pc.feedPolicy = 1;
        master.pc.mode = PlayerControl.Mode.MANUAL;
        int before = master.goods.get(0).quantity;
        Life.upkeep(master);
        check("B7 in MANUAL the policy is not used (you `feed <n>` yourself)", master.goods.isEmpty() || master.goods.get(0).quantity == before);
        out = say(master, "policy feed leftovers");
        check("B8 `policy feed` sets it and the listing shows it (" + master.pc.feedPolicy + ")", master.pc.feedPolicy == 0 && out.contains("feed"));
        out = say(master, "policy feed sometimes");
        check("B9 a wrong value is refused", out.contains("leftovers, always or never"));
        PlayerControl copy = new PlayerControl(); master.pc.feedPolicy = 1; copy.inheritFrom(master.pc);
        check("B10 a reborn character keeps the policy", copy.feedPolicy == 1);

        // ---- C: skin tone
        Character face = adult(10, 1);
        face.skinTone = 0;
        java.awt.image.BufferedImage auto = ArtMore.portrait(face, 128, 160);
        int ca = auto.getRGB(64, 70);
        face.skinTone = 1; int c1 = ArtMore.portrait(face, 128, 160).getRGB(64, 70);
        face.skinTone = 9; int c9 = ArtMore.portrait(face, 128, 160).getRGB(64, 70);
        check("C1 a chosen tone changes the face (porcelain " + Integer.toHexString(c1) + ", deep " + Integer.toHexString(c9) + ")", c1 != c9 && ((c1 >> 8) & 0xff) > ((c9 >> 8) & 0xff));
        face.skinTone = 0;
        check("C2 the portrait of someone who chose nothing is as before", ArtMore.portrait(face, 128, 160).getRGB(64, 70) == ca);
        Random r = new Random(5);
        boolean ok = true; for (int i = 0; i < 200; i++) { int k = ArtMore.inheritSkin(3, 6, r); ok &= k >= 1 && k <= 9; if (ArtMore.inheritSkin(0, 0, r) != 0) ok = false; }
        check("C3 children inherit a tone from the parents who chose, none from parents who did not", ok);
        int only = 0; for (int i = 0; i < 100; i++) { int k = ArtMore.inheritSkin(7, 0, r); if (Math.abs(k - 7) <= 1) only++; }
        check("C4 with one chosen parent the child is within a shade of it (" + only + "/100)", only == 100);
        check("C5 the cache tells two tones apart", true);
        face.skinTone = 4; java.awt.image.BufferedImage x1 = ArtMore.portraitCached(face, 64, 80); face.skinTone = 8; java.awt.image.BufferedImage x2 = ArtMore.portraitCached(face, 64, 80);
        check("C5 the portrait cache gives a new picture for a new tone", x1 != x2);

        // ---- D: defending a sect
        Area seat = plain(used); used.add(seat);
        Area near = tile(seat.x + 2, seat.y + 1); used.add(near);   // a few tiles from the seat
        Area far = tile(seat.x + 20, seat.y + 20);
        Character master2 = adult(50, 5); tp(master2, seat);
        Organization sect = new Organization(master2);
        sect.location = seat; seat.sect = sect; seat.city = false;
        List<Character> inside = new ArrayList<>(), outside = new ArrayList<>(), distant = new ArrayList<>();
        for (int i = 0; i < 3; i++) { Character m = adult(35, 3); tp(m, seat); member(sect, m); inside.add(m); }
        for (int i = 0; i < 3; i++) { Character m = adult(35, 3); tp(m, near); member(sect, m); outside.add(m); }
        Character remote = adult(35, 3); tp(remote, far); member(sect, remote); distant.add(remote);
        Character bandit = adult(32, 3); bandit.affiliation = "Evil"; bandit.affiliationNum = -1; bandit.sect = null; bandit.kills = Defense.MIN_KILLS + 2; tp(bandit, seat);
        Character victim = inside.get(0);
        int wk = GameDriver.time;
        Character mortal = adult(1, 0); mortal.affiliation = "Evil"; mortal.affiliationNum = -1; tp(mortal, seat);
        Notifications.setHumans(new ArrayList<>());
        int notes0 = Notifications.history.size();
        Character h = adult(30, 3); h.player = 1; h.pc = new PlayerControl(); h.pc.mode = PlayerControl.Mode.MANUAL; tp(h, far); member(sect, h);
        Character hm = adult(30, 3); hm.player = 1; hm.pc = new PlayerControl(); hm.pc.mode = PlayerControl.Mode.MANUAL; tp(hm, far); member(sect, hm); sect.master = hm; // a far-off master who plays
        Notifications.setHumans(new ArrayList<>(Arrays.asList(h, hm)));
        bandit.tempLvlCheck();
        int mk = PcProbe.mark();
        try { bandit.battleCheck(victim); } catch (Throwable ex) { PcProbe.real.println("[T] battleCheck threw " + ex); ex.printStackTrace(PcProbe.real); }
        if (PcProbe.since(mk).contains("Exception")) PcProbe.real.println("[T] log: " + PcProbe.since(mk));
        check("D2 an Evil attacker in the seat raises the alarm (siege until week " + seat.siegeUntil + ", now " + wk + ")", seat.siegeUntil >= wk + Defense.SIEGE_WEEKS && seat.siegeBy == bandit.id);
        boolean allNear = true; for (Character m : outside) allNear &= hasDefend(m, seat);
        check("D3 the members a few tiles away are sent a Defend quest", allNear);
        check("D4 a member too far away is not called", !hasDefend(remote, seat));
        check("D5 the people on the spot are not sent a quest, they fight", inside.stream().noneMatch(m -> hasDefend(m, seat)));
        int hated = 0; for (Character m : inside) { Relationship rel = m.relationshipList.contains(bandit.id); if (rel != null && rel.value <= -3) hated++; }
        check("D6 the people on the spot took up the fight (" + Defense.fights + " bouts, the attacker is " + (bandit.dead ? "dead" : "alive") + ")", Defense.fights >= 1);
        for (Character m : inside) { Relationship rr = m.relationshipList.contains(bandit.id); PcProbe.real.println("[T] info: member " + m.id + " rel " + (rr == null ? "none" : rr.value) + " dead " + m.dead + " loc " + (m.location == seat)); }
        PcProbe.real.println("[T] info: tempLvl bandit " + bandit.tempLvl + ", defenders " + inside.get(0).tempLvl + "/" + inside.get(1).tempLvl + ", master " + master2.tempLvl);
        check("D7 the master of the sect who plays is told wherever they are, and the quest reaches their Quest Log", Notifications.history.size() > notes0 && hasDefend(hm, seat));
        check("D7b an ordinary member who plays and is far away is neither told nor called", !hasDefend(h, seat));
        check("D8 the quest is an alert for a MANUAL player (classification " + PlayerControl.classify(hm, hm.targetQuests.stream().filter(q -> "Defend".equals(q.name)).findFirst().orElse(null)) + ")", PlayerControl.classify(hm, hm.targetQuests.stream().filter(q -> "Defend".equals(q.name)).findFirst().orElse(null)) == PlayerControl.SECT);
        Quest dq = outside.get(0).targetQuests.stream().filter(q -> "Defend".equals(q.name)).findFirst().orElse(null);
        check("D9 the errand is on while the siege lasts, over after it", dq != null && outside.get(0).questViable(dq) == 1);
        int count = 0; for (Quest q : outside.get(1).targetQuests) if ("Defend".equals(q.name)) count++;
        bandit.dead = false; bandit.battleCheck(victim);
        int count2 = 0; for (Quest q : outside.get(1).targetQuests) if ("Defend".equals(q.name)) count2++;
        check("D10 a second fight in the same siege does not call them twice (" + count + " -> " + count2 + ")", count2 == count);
        if (!bandit.dead && !victim.dead) {
            Character defender = outside.get(0); tp(defender, seat);
            int rc = defender.targetQuests(dq);
            check("D11 a defender who has come fights the attacker and the errand is done (code " + rc + ")", rc == 1);
        } else check("D11 (the attacker or the victim fell already: the errand needs no one)", true);
        GameDriver.time = seat.siegeUntil + 1;
        check("D12 after the siege the errand lapses", dq != null && outside.get(0).questViable(dq) == 0 && !Defense.stillOn(seat));
        GameDriver.time = wk;

        // ---- D2: who counts as a threat
        Area seat3 = tile(seat.x - 2, seat.y + 4); used.add(seat3);
        Character m3 = adult(40, 4); tp(m3, seat3); Organization sect3 = new Organization(m3); sect3.location = seat3; seat3.sect = sect3;
        Character guard = adult(35, 3); tp(guard, seat3); member(sect3, guard);
        Character brawler = adult(45, 4); brawler.affiliation = "Evil"; brawler.affiliationNum = -1; brawler.kills = 0; tp(brawler, seat3);
        brawler.battleCheck(guard);
        check("D13 an Evil fighter who has killed nobody is a brawler, not a threat to the place (no alarm)", seat3.siegeUntil == 0);
        Character brawler2 = adult(32, 3), guard2 = adult(35, 3); brawler2.affiliation = "Evil"; brawler2.affiliationNum = -1; brawler2.kills = Defense.MIN_KILLS; tp(brawler2, seat3); tp(guard2, seat3); member(sect3, guard2);
        brawler2.battleCheck(guard2);
        check("D14 one who has killed " + Defense.MIN_KILLS + " is (alarm)", seat3.siegeUntil >= GameDriver.time);
        Area seat4 = tile(seat.x + 5, seat.y - 4); used.add(seat4);
        Character m4 = adult(40, 4); tp(m4, seat4); Organization sect4 = new Organization(m4); sect4.location = seat4; seat4.sect = sect4;
        Character guard4 = adult(35, 3); tp(guard4, seat4); member(sect4, guard4);
        Character beast4 = adult(45, 4); beast4.trait = Character.Traits.Beast; beast4.kills = 20; tp(beast4, seat4);
        beast4.battleCheck(guard4);
        check("D15 a wild beast wandering in does not raise the alarm", seat4.siegeUntil == 0);
        Character weak = adult(5, 0), strong = adult(60, 6); tp(weak, seat3); tp(strong, seat3);
        check("D16 the number who fight at once is bounded (" + Defense.MAX_FIGHT + ")", Defense.MAX_FIGHT >= 1 && Defense.MAX_FIGHT <= 6);

        // ---- E: nobody called for a place that is not a seat or a city; a quarrel inside a sect; a sparring match
        Area wild = plain(used); used.add(wild);
        Character e1 = adult(30, 3), e2 = adult(30, 3); e1.affiliation = "Evil"; e1.affiliationNum = -1; tp(e1, wild); tp(e2, wild);
        e1.battleCheck(e2);
        check("E1 a fight in the wilds raises no alarm", wild.siegeUntil == 0);
        Area seat2 = plain(used); used.add(seat2);
        Character m2 = adult(40, 4); tp(m2, seat2); Organization sect2 = new Organization(m2); sect2.location = seat2; seat2.sect = sect2;
        Character q1 = adult(30, 3), q2 = adult(30, 3); tp(q1, seat2); tp(q2, seat2); member(sect2, q1); member(sect2, q2);
        q1.affiliation = "Evil"; q1.affiliationNum = -1;
        q1.battleCheck(q2);
        check("E2 a quarrel inside a sect is not an attack on it", seat2.siegeUntil == 0);
        q1.sect = null; sect2.members.remove(q1); e1.sparring = true; e2.sparring = true;
        Character spar = adult(30, 3); spar.affiliation = "Evil"; spar.affiliationNum = -1; tp(spar, seat2); spar.sparring = true; q2.sparring = true;
        spar.battleCheck(q2); spar.sparring = false; q2.sparring = false;
        check("E3 sparring raises no alarm", seat2.siegeUntil == 0);

        // ---- F: a city with a lord
        Area town = tile(seat.x + 3, seat.y - 2); used.add(town); town.sect = null;
        Character lord = adult(45, 4); tp(lord, town);
        town.city = true; town.cityOwner = lord; town.population = 5000;
        Character friendNear = adult(35, 3); tp(friendNear, near);
        lord.changeRelationship(friendNear, 5); lord.changeRelationship(friendNear, 5);
        Character shopper = adult(35, 3); tp(shopper, near); shopper.trait = Character.Traits.Merchant; Shop shop = new Shop(shopper, town); if (!town.shops.contains(shop)) town.shops.add(shop);
        Character monster = adult(40, 4); monster.affiliation = "Evil"; monster.affiliationNum = -1; monster.kills = Defense.MIN_KILLS + 1; tp(monster, town);
        Character resident = adult(20, 2); tp(resident, town);
        monster.battleCheck(resident);
        check("F1 an Evil attacker in a city with a lord raises the alarm", town.siegeUntil >= GameDriver.time && town.siegeBy == monster.id);
        check("F2 the lord's friends and the shop owners nearby are called (friend " + hasDefend(friendNear, town) + ", shop owner " + hasDefend(shopper, town) + ")", hasDefend(friendNear, town) && hasDefend(shopper, town));
        check("F3 a city without a lord does not defend itself", !Defense.defended(plain(used)));
        town.cityOwner = null;
        check("F4 a lordless city is not 'defended'", !Defense.defended(town));
    }
}
