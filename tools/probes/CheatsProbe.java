package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-17 (headless): the second set of cheats (`Cheats`, `cheat help`). Each one is tried through the command line as a Cheater, and refused to everybody else: divine lightning (a child, a person of a story, a
 *  marked person too), the bond between two NPCs, unbreakable gear, technique level and potential, teleport, every kind of item, a companion of any level and changing it, evade death (a policy), the No Death mark
 *  (only a person who plays can kill), resurrecting the dead, 100% win and 100% no escape, creating a person, a Monster and wild beasts, and changing a city and a sect. */
public class CheatsProbe {
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

    static Character person(Area where, int lvl, int age) {
        Character c = new Character(where);
        c.age = age; c.gender = "Male"; c.maxAge = 100000; c.born = true; c.trait = Character.Traits.Friendly; c.targetQuests.clear(); c.lvl = lvl; c.majorLvl = Math.max(0, (lvl - 1) / 10); c.minorLvl = Math.max(1, lvl - 10 * c.majorLvl);
        c.caution = 0; c.affiliation = "Good"; c.affiliationNum = 1; c.name = "Cheat"; c.lastName = "N" + c.id;
        while (Map.characters.size() < c.id) Map.characters.add(null); // (ids are the places in the list)
        Map.characters.add(c); Map.population++;
        return c;
    }

    /** 2026-10-18: the third set (`CheatsMore`): growth, the tile, time, the purse, sight, the squad. */
    static void more(Character p, Area lab) throws Exception {
        p.location.removeChar(p); p.location = lab; lab.addChar(p);
        PcProbe.cmd("cheat help", 30);
        check("N0 'cheat help' lists the third set too", PcProbe.out().contains("breakthrough") && PcProbe.out().contains("freecraft") && PcProbe.out().contains("reveal"));

        // ---- B: breakthrough
        Character b1 = person(lab, 15, 30); b1.potential = 5; b1.inhibitor = 1;
        PcProbe.cmd("cheat breakthrough #" + b1.id, 30);
        check("N1 an instant breakthrough, whatever the gate (major " + b1.majorLvl + ", lv " + b1.lvl + ")", b1.majorLvl == 2 && b1.lvl == 21);
        int passed = 0;
        for (int i = 0; i < 20; i++) {
            Character b2 = person(lab, 25, 30); b2.potential = 1; b2.inhibitor = 1;
            PcProbe.cmd("cheat pass on #" + b2.id, 30);
            PcProbe.cmd("cheat breakthrough #" + b2.id, 30);
            if (b2.majorLvl == 3 && b2.cheatBreak) passed++;
        }
        check("N2 with 'pass' on, the breakthrough and the tribulation to Core Formation never fail (" + passed + " of 20 with potential 1)", passed == 20);
        Character b3 = person(lab, 100, 30); b3.majorLvl = 10; b3.lvl = 101;
        PcProbe.cmd("cheat breakthrough #" + b3.id, 30);
        check("N2b a True Immortal has nothing above", b3.majorLvl == 10 && PcProbe.out().contains("nothing above"));

        // ---- A: age, lifespan, inhibitor, mastery, element
        Character g = person(lab, 40, 30); g.potential = 50;
        PcProbe.cmd("cheat age 77 #" + g.id, 30); PcProbe.cmd("cheat lifespan 999 #" + g.id, 30);
        check("N3 age and lifespan are set (" + g.age + "/" + g.maxAge + ")", g.age == 77 && g.maxAge == 999);
        PcProbe.cmd("cheat inhibitor 1 #" + g.id, 30); double slow = g.cultivation;
        PcProbe.cmd("cheat inhibitor 0 #" + g.id, 30); double fast = g.cultivation;
        PcProbe.cmd("cheat inhibitor 4 #" + g.id, 30); double slower = g.cultivation;
        check("N4 the inhibitor sets the weekly gain (" + String.format("%.1f %.1f %.1f", fast, slow, slower) + ")", g.inhibitor == 4 && Math.abs(fast / slow - 2) < 0.01 && Math.abs(slow / slower - 8) < 0.05);
        g.martialTechnique = new Technique(Technique.techniqueType.Martial, g, 60); g.martialTechnique.lvl = 3;
        g.movementTechnique = new Technique(Technique.techniqueType.Movement, g, 40); g.movementTechnique.lvl = 2;
        PcProbe.cmd("cheat mastery #" + g.id, 30);
        check("N5 every technique known is at its full level (" + g.martialTechnique.lvl + "/" + g.martialTechnique.potential + ", " + g.movementTechnique.lvl + "/" + g.movementTechnique.potential + ")", g.martialTechnique.lvl == g.martialTechnique.potential && g.movementTechnique.lvl == g.movementTechnique.potential);
        PcProbe.cmd("cheat element water #" + g.id, 30);
        check("N6 the path takes the element (" + Elements.name(Elements.of(g)) + ")", Elements.of(g) == 4);
        PcProbe.cmd("cheat element fire #" + g.id + " techniques", 30);
        check("N6b ...and the techniques with it (" + Elements.name(Elements.of(g)) + ", " + Elements.name(g.martialTechnique.element()) + ")", Elements.of(g) == 1 && g.martialTechnique.element() == 1 && g.movementTechnique.element() == 1);

        // ---- M: marriage, a child
        Character hus = person(lab, 30, 30), wife = person(lab, 30, 30); wife.gender = "Female";
        PcProbe.cmd("cheat pregnant #" + wife.id, 30);
        check("N7 a woman without a husband cannot be made pregnant", !wife.pregnant);
        PcProbe.cmd("cheat marry #" + hus.id + " #" + wife.id, 30);
        check("N7b a marriage is made at once", hus.spouse == wife && wife.spouse == hus);
        int born0 = Map.tempCharacters.size();
        PcProbe.cmd("cheat pregnant #" + wife.id, 30);
        check("N7c a child is on the way (" + (Map.tempCharacters.size() - born0) + ", pregnant=" + wife.pregnant + ")", wife.pregnant && Map.tempCharacters.size() == born0 + 1);

        // ---- T: treasure, vein, dungeon, array, wipe, awaken
        Area ta = at(7, 5); ta.monsters = 0; ta.city = false; ta.heavenlyTechnique = null; ta.heavenItem = null; ta.dungeonType = null; ta.spiritualVein = false; ta.arrayBag.clear();
        p.location.removeChar(p); p.location = ta; ta.addChar(p);
        PcProbe.cmd("cheat treasure technique", 30);
        check("N8 a Heaven-Sent treasure lies in the tile (" + ta.heavenLabel() + ")", ta.hasHeaven() && ta.heavenlyTechnique != null);
        PcProbe.cmd("cheat vein on", 30);
        check("N9 a spirit vein", ta.spiritualVein);
        PcProbe.cmd("cheat vein off", 30);
        check("N9b ...and gone", !ta.spiritualVein);
        PcProbe.cmd("cheat dungeon lair 30", 30);
        check("N10 a Beast Lair of level 30 (" + ta.dungeonType + ", " + ta.lvl + ", " + ta.name + ")", ta.dungeonType == Area.DungeonType.BEAST_LAIR && ta.lvl == 30 && ta.monsters >= 1000);
        PcProbe.cmd("cheat dungeon off", 30);
        check("N10b ...and the land is itself again (" + ta.name + ")", ta.dungeonType == null);
        PcProbe.cmd("cheat dungeon ruins 25", 30);
        check("N10c ruins have charges (" + ta.dungeonCharges + ")", ta.dungeonType == Area.DungeonType.LOST_RUINS && ta.dungeonCharges >= 3);
        PcProbe.cmd("cheat dungeon off", 30);
        PcProbe.cmd("cheat array install 40", 30); PcProbe.cmd("cheat array install 12", 30);
        check("N11 two arrays guard the tile (" + ta.arrayBag.size() + ")", ta.arrayBag.size() == 2);
        PcProbe.cmd("cheat array break", 30);
        check("N11b ...and are broken", ta.arrayBag.isEmpty());
        ta.monsters = 500;
        Character mon = new Character(ta, p); Cheats.setLevel(mon, 20); mon.awakenMonster();
        PcProbe.cmd("cheat wipe", 30);
        check("N12 the beasts and the Monster of the tile fall (" + ta.monsters + ", dead=" + mon.dead + ")", ta.monsters == 0 && mon.dead);
        ta.monsters = 5; int aw0 = Character.stMonAwake;
        PcProbe.cmd("cheat awaken", 30);
        check("N13 a wild beast wakes as a Monster (" + ta.monsters + ", awake " + (Character.stMonAwake - aw0) + ")", ta.monsters == 4 && Character.stMonAwake == aw0 + 1);

        // ---- O: tournaments
        Area to = at(5, 7); to.tournamentTimer = 0; to.tournamentRegistrants.clear(); to.city = false;
        p.location.removeChar(p); p.location = to; to.addChar(p);
        Character f1 = person(to, 12, 30), f2 = person(to, 14, 30), f3 = person(to, 16, 30);
        PcProbe.cmd("cheat tournament open 0 1", 30);
        check("N14 a tournament is opened (" + to.tournamentTimer + ", realms " + to.tournamentMinMajor + "-" + to.tournamentMaxMajor + ", prize " + to.tournamentPrize + ")", to.tournamentTimer == 8 && to.tournamentMaxMajor == 1 && to.tournamentPrize != null);
        PcProbe.cmd("cheat tournament hold", 30);
        check("N14b it is held next week with the people here signed up (" + to.tournamentTimer + ", " + to.tournamentRegistrants.size() + ")", to.tournamentTimer == 1 && to.tournamentRegistrants.size() >= 3 && to.tournamentRegistrants.contains(f1));
        PcProbe.cmd("cheat tournament close", 30);
        check("N14c ...or called off", to.tournamentTimer == 0 && to.tournamentRegistrants.isEmpty() && to.tournamentPrize == null);

        // ---- S: the spectator needs a seat (the Spectate Tournament quest)
        PcProbe.cmd("cheat tournament open 0 1", 30);
        check("N15 nobody is a spectator before taking a seat", Tournaments.spectators(to, new ArrayList<Character>()).isEmpty());
        Tournaments.takeSeat(p, to);
        check("N15b ...and the person who took one is", Tournaments.spectators(to, new ArrayList<Character>()).contains(p));
        Tournaments.clearSeats(to);
        check("N15c the seats are free when it is over", Tournaments.spectators(to, new ArrayList<Character>()).isEmpty());
        PcProbe.cmd("cheat tournament close", 30);

        // ---- P: the purse and free crafting
        PcProbe.cmd("cheat wealth 123456 #" + g.id, 30);
        check("N16 wealth is set (" + g.ss + ")", g.ss == 123456);
        PcProbe.cmd("cheat freecraft on", 30);
        int herb10 = 0; for (Item m : p.materialBag) if (m.itemType == Item.ItemType.Herb && m.lvl == 10) herb10 = m.quantity;
        check("N17 free crafting fills the bag (" + herb10 + " herbs of quality 10)", herb10 >= 5000 && p.cheatFreeCraft);
        for (Item m : p.materialBag) if (m.itemType == Item.ItemType.Herb && m.lvl == 10) m.quantity = 3;
        CheatsMore.refill(p);
        herb10 = 0; for (Item m : p.materialBag) if (m.itemType == Item.ItemType.Herb && m.lvl == 10) herb10 = m.quantity;
        check("N17b ...and again each turn (" + herb10 + ")", herb10 >= 5000);
        PcProbe.cmd("cheat freecraft off", 30);
        check("N17c ...until switched off", !p.cheatFreeCraft);

        // ---- V: reveal
        Character shown = person(to, 33, 44);
        PcProbe.cmd("cheat reveal here", 30);
        check("N18 'reveal here' lists the people with their paths (" + (PcProbe.out().contains("#" + shown.id) && PcProbe.out().contains("(" + Elements.name(Elements.of(shown)) + ")")) + ")", PcProbe.out().contains("#" + shown.id) && PcProbe.out().contains("(" + Elements.name(Elements.of(shown)) + ")"));
        PcProbe.cmd("cheat reveal #" + shown.id, 30);
        check("N18b ...and one person in full", PcProbe.out().contains("Path:") && PcProbe.out().contains("potential " + shown.potential));
        PcProbe.cmd("cheat reveal map", 30);
        check("N18c ...and the map is revealed", PcProbe.out().contains("on your map"));

        // ---- Q: teleport with the squad
        Character m1 = person(to, 30, 30), m2 = person(to, 30, 30);
        m1.joinParty(p); m2.joinParty(p);
        Area dest = at(9, 9); if (dest.city) dest.city = false;
        PcProbe.cmd("cheat teleport " + dest.x + " " + dest.y + " self squad", 30);
        check("N19 the squad comes along (" + (p.location == dest) + ", " + (m1.location == dest) + ", " + (m2.location == dest) + ")", p.location == dest && m1.location == dest && m2.location == dest);
        PcProbe.cmd("cheat teleport " + to.x + " " + to.y, 30);
        check("N19b without the word 'squad' it is only you (" + (p.location == to) + ", " + (m1.location == dest) + ")", p.location == to && m1.location == dest);

        // ---- J: time (the cheats that wait spend the week)
        int t0 = GameDriver.time;
        PcProbe.cmd("cheat jump 3", 60);
        boolean said = PcProbe.out().contains("The weeks pass");
        while (GameDriver.time < t0 + 3 && p.skipping > 0) PcProbe.cmd("wait", 60); // (an alert ends a wait early: the probe waits it out)
        check("N20 'jump' spends the week and lets the weeks pass (said " + said + ", " + (GameDriver.time - t0) + " weeks, skipping " + p.skipping + ")", said && GameDriver.time - t0 >= 1 && GameDriver.time - t0 <= 3);
        int seasonNow = (GameDriver.time % 52) / 13, want = (seasonNow + 1) % 4;
        PcProbe.cmd("cheat season " + new String[]{"spring", "summer", "autumn", "winter"}[want], 60);
        for (int i = 0; i < 20 && (GameDriver.time % 52) / 13 != want; i++) PcProbe.cmd("wait 5", 60);
        check("N21 'season' waits for the season (" + (GameDriver.time % 52) / 13 + " wanted " + want + ")", (GameDriver.time % 52) / 13 == want);

        // ---- E: the epoch at once
        PcProbe.cmd("cheat epoch", 30);
        check("N22 the epoch is called (flag " + GameDriver.epochNow + ")", GameDriver.epochNow || !GameDriver.epochVictims.isEmpty());
        for (int i = 0; i < 3 && GameDriver.epochNow; i++) PcProbe.cmd("wait", 60);
        PcProbe.real.println("[T] info: epoch victims " + GameDriver.epochVictims.size() + " flag " + GameDriver.epochNow);
        check("N22b ...and it begins at the next tick", !GameDriver.epochNow);
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        check("player created and prompting", p != null && PcProbe.in.blocked);
        if (p == null) return;
        Settings.lore = false; Settings.fightView = false;
        p.pc().breakthroughPolicy = PlayerControl.BT_AUTO;
        p.lvl = 100; p.majorLvl = 9; p.maxAge = 100000; p.caution = 0; p.targetQuests.clear(); p.spotQuests.clear(); p.currentQuest = null;
        Area lab = at(6, 6); lab.monsters = 0; lab.city = false;
        p.location.removeChar(p); p.location = lab; lab.addChar(p);

        // ---- X0: only a Cheater
        p.trait = Character.Traits.Friendly;
        Character v0 = person(lab, 30, 30);
        PcProbe.cmd("cheat kill #" + v0.id, 30);
        check("X0 a person who is not a Cheater cannot use them", !v0.dead && PcProbe.out().contains("Only a Cheater"));
        p.trait = Character.Traits.Cheater;
        PcProbe.cmd("cheat help", 30);
        check("X0b 'cheat help' lists them", PcProbe.out().contains("More cheats") && PcProbe.out().contains("resurrect"));

        // ---- K: divine lightning
        Character adult = person(lab, 40, 30), kid = person(lab, 5, 10), fated = person(lab, 40, 30), marked = person(lab, 40, 30), wander = person(lab, 40, 30);
        fated.fatedTo = p.id + 1; marked.noDeath = true; wander.wanderingImmortal = true;
        for (Character x : new Character[]{adult, kid, fated, marked, wander}) PcProbe.cmd("cheat kill #" + x.id, 30);
        check("K1 divine lightning kills an adult, a child, a person of a story, a marked one and a Wandering Immortal (" + adult.dead + kid.dead + fated.dead + marked.dead + wander.dead + ")", adult.dead && kid.dead && fated.dead && marked.dead && wander.dead);
        check("K1b the cause is lightning (" + adult.deathCause + ")", "struck down by divine lightning".equals(adult.deathCause));
        PcProbe.cmd("cheat kill #" + p.id, 30);
        check("K1c you cannot strike yourself down", !p.dead);

        // ---- R: bonds
        Character r1 = person(lab, 30, 30), r2 = person(lab, 30, 30);
        PcProbe.cmd("cheat relation #" + r1.id + " #" + r2.id + " -3", 30);
        boolean bad = r1.relationshipList.contains(r2.id) == null || r2.relationshipList.contains(r1.id) == null || r1.relationshipList.contains(r2.id).value != -3 || r2.relationshipList.contains(r1.id).value != -3;
        if (bad) PcProbe.real.println("[T] info: relation said: " + PcProbe.out().replace((char) 10, (char) 124) + " ab=" + (r1.relationshipList.contains(r2.id) == null ? "none" : "" + r1.relationshipList.contains(r2.id).value) + " ba=" + (r2.relationshipList.contains(r1.id) == null ? "none" : "" + r2.relationshipList.contains(r1.id).value));
        check("R1 the bond between two NPCs is set both ways (-3)", !bad);
        PcProbe.cmd("cheat relation #" + r1.id + " #" + r2.id + " 3", 30);
        check("R1b ...and back to best friends (3)", r1.relationshipList.contains(r2.id).value == 3 && r2.relationshipList.contains(r1.id).value == 3);

        // ---- U: unbreakable
        Character smith = person(lab, 50, 30), big = person(lab, 100, 30);
        smith.equippedWeapon = new Item(smith, Item.WeaponType.Sword, 1); smith.equippedClothing = new Item(smith, 1, Item.ItemType.Clothing);
        big.equippedWeapon = new Item(big, Item.WeaponType.Sword, 10);
        int broke = 0;
        for (int i = 0; i < 4000 && broke < 1; i++) { smith.equippedWeapon = smith.equippedWeapon != null ? smith.equippedWeapon : new Item(smith, Item.WeaponType.Sword, 1); smith.durabilityCheck(big); if (smith.equippedWeapon == null) broke++; }
        check("U0 without the cheat a weak weapon breaks against a strong one (" + broke + ")", broke >= 1);
        PcProbe.cmd("cheat unbreakable both #" + smith.id, 30);
        smith.equippedWeapon = new Item(smith, Item.WeaponType.Sword, 1); smith.equippedClothing = new Item(smith, 1, Item.ItemType.Clothing);
        int lost = 0;
        for (int i = 0; i < 6000; i++) { smith.durabilityCheck(big); if (smith.equippedWeapon == null || smith.equippedClothing == null) { lost++; smith.equippedWeapon = new Item(smith, Item.WeaponType.Sword, 1); smith.equippedClothing = new Item(smith, 1, Item.ItemType.Clothing); } }
        check("U1 with the cheat 6,000 clashes break nothing (" + lost + ")", lost == 0);

        // ---- T: techniques
        Character tt = person(lab, 50, 30);
        tt.martialTechnique = new Technique(Technique.techniqueType.Martial, tt, 20); tt.martialTechnique.lvl = 5;
        PcProbe.cmd("cheat technique martial level 33 #" + tt.id, 30);
        check("T1 the martial technique's level is set (" + tt.martialTechnique.lvl + ", potential " + tt.martialTechnique.potential + ")", tt.martialTechnique.lvl == 33 && tt.martialTechnique.potential >= 33);
        PcProbe.cmd("cheat technique martial potential 80 #" + tt.id, 30);
        check("T1b ...and the potential (" + tt.martialTechnique.potential + ")", tt.martialTechnique.potential == 80);

        // ---- P: teleport
        Area far = at(12, 12);
        Character tp = person(lab, 30, 30);
        PcProbe.cmd("cheat teleport " + far.x + " " + far.y + " #" + tp.id, 30);
        check("P1 an NPC is teleported to the tile (" + tp.location.x + "," + tp.location.y + ")", tp.location == far && far.characters.contains(tp) && !lab.characters.contains(tp));
        PcProbe.cmd("cheat teleport " + lab.x + " " + lab.y, 30);
        check("P1b ...and you with no target", p.location == lab);

        // ---- G: items
        int bp = p.backpack.size(), ab = p.arrayBag.size();
        p.equippedWeapon = null; p.equippedClothing = null; p.backpack.removeIf(i -> i.itemType == Item.ItemType.Weapon || i.itemType == Item.ItemType.Clothing);
        PcProbe.cmd("cheat give weapon 6", 30); PcProbe.cmd("cheat give armor 6", 30); PcProbe.cmd("cheat give array 4", 30); PcProbe.cmd("cheat give instrument 3", 30);
        check("G1 a weapon and armor are worn, an instrument and an array arrive (weapon Lv." + (p.equippedWeapon == null ? 0 : p.equippedWeapon.lvl) + ", armor Lv." + (p.equippedClothing == null ? 0 : p.equippedClothing.lvl) + ", backpack " + bp + " -> " + p.backpack.size() + ", arrays " + ab + " -> " + p.arrayBag.size() + ")", p.equippedWeapon != null && p.equippedWeapon.lvl >= 6 && p.equippedClothing != null && p.equippedClothing.lvl >= 6 && p.backpack.size() >= bp + 1 && p.arrayBag.size() == ab + 1);
        int pills0 = p.pillTotal();
        PcProbe.cmd("cheat give pill potential 11 7", 30); PcProbe.cmd("cheat give pill cultivation 4 5", 30);
        check("G2 pills (" + pills0 + " -> " + p.pillTotal() + ")", p.pillTotal() == pills0 + 12);
        PcProbe.cmd("cheat give herb 5 300", 30); PcProbe.cmd("cheat give metal 6 400", 30); PcProbe.cmd("cheat give blood 2500", 30);
        int herb = 0, metal = 0; for (Item m : p.materialBag) { if (m.itemType == Item.ItemType.Herb && m.lvl == 5) herb += m.quantity; if (m.itemType == Item.ItemType.Metal && m.lvl == 6) metal += m.quantity; }
        check("G3 herbs, metal and Blood (" + herb + ", " + metal + ", " + p.bloodAmount() + ")", herb >= 300 && metal >= 400 && p.bloodAmount() >= 2500);
        int known = p.techniqueList.size();
        PcProbe.cmd("cheat give technique martial 70 fire", 30);
        Technique got = p.techniqueList.size() > known ? p.techniqueList.get(p.techniqueList.size() - 1) : null;
        boolean inSlot = false; for (Technique q : new Technique[]{p.martialTechnique, p.martialTechnique2}) if (q != null && q.potential == 70 && q.element() == Elements.FIRE) inSlot = true;
        check("G4 a Fire technique of potential 70 is known (" + (got == null ? "equipped: " + inSlot : got.name) + ")", (got != null && got.element() == Elements.FIRE && got.potential == 70) || inSlot);

        // ---- C: companions
        int comp0 = Taming.beasts(p).size();
        PcProbe.cmd("cheat tame 60 Moon Wolf", 30);
        List<Character> comps = Taming.beasts(p);
        check("C1 a companion of level 60 joins whatever the cap says (" + comp0 + " -> " + comps.size() + ")", comps.size() == comp0 + 1 && comps.get(comps.size() - 1).lvl == 60);
        PcProbe.cmd("cheat companion " + comps.size() + " level 90", 30);
        check("C1b its level is changed (" + comps.get(comps.size() - 1).lvl + ")", comps.get(comps.size() - 1).lvl == 90 && comps.get(comps.size() - 1).majorLvl == 8);
        PcProbe.cmd("cheat companion " + comps.size() + " name Silver Fang", 30);
        check("C1c ...and its name (" + comps.get(comps.size() - 1).name + ")", comps.get(comps.size() - 1).name.startsWith("Silver Fang"));

        // ---- E, N: evade death, the No Death mark
        PcProbe.cmd("cheat evade on", 30);
        p.dead();
        check("E1 with 'evade' on, death passes you by", !p.dead && p.pc().evadeDeath);
        PcProbe.cmd("cheat evade off", 30);
        check("E1b ...and off again", !p.pc().evadeDeath);
        Character mk = person(lab, 40, 30), killerNpc = person(lab, 90, 30);
        PcProbe.cmd("cheat nodeath on #" + mk.id, 30);
        PcProbe.real.println("[T] info: nodeath said: " + PcProbe.out().substring(Math.max(0, PcProbe.out().length() - 160)).replace((char) 10, (char) 124) + " noDeath=" + mk.noDeath);
        mk.dead(); mk.dead(killerNpc);
        check("N1 a marked person cannot die of age, a trap or an NPC's blade", !mk.dead && mk.noDeath);
        mk.dead(p);
        check("N1b ...but a person who plays can kill them", mk.dead);
        Character mk2 = person(lab, 40, 30); mk2.noDeath = true;
        check("N1c the epoch spares the marked (" + Rankings.epochSafe(mk2) + ")", Rankings.epochSafe(mk2));

        // ---- RES: the dead come back
        check("S1 the dead are remembered (" + Cheats.graveyard.size() + ")", Cheats.graveyard.containsKey(adult.id));
        PcProbe.cmd("cheat resurrect", 30);
        check("S1b 'cheat resurrect' lists them", PcProbe.out().contains("recently dead") && PcProbe.out().contains("#" + adult.id));
        PcProbe.cmd("cheat resurrect #" + adult.id, 30);
        check("S2 a dead NPC rises at your tile (dead=" + adult.dead + ", in the world " + (Map.characters.get(adult.id) == adult) + ")", !adult.dead && Map.characters.get(adult.id) == adult && adult.location == lab && lab.characters.contains(adult));
        PcProbe.cmd("cheat resurrect #" + adult.id, 30);
        check("S2b a living one cannot be called back again", PcProbe.out().contains("Nobody of that name") || PcProbe.out().contains("is alive"));

        // ---- W: win and escape
        Character foe = person(lab, 140, 30);
        p.lvl = 20; p.majorLvl = 1; p.equippedWeapon = null; p.equippedClothing = null; p.tempLvlCheck(); foe.tempLvlCheck();
        int before = p.previewFight(foe, false).winChance;
        PcProbe.cmd("cheat win on", 30);
        int after = p.previewFight(foe, false).winChance;
        check("W1 a person who always wins shows 100% against a stronger one (" + before + "% -> " + after + "%)", after == 100 && before < 100);
        PcProbe.cmd("cheat win off", 30);
        PcProbe.cmd("cheat win on #" + foe.id, 30);
        check("W1b ...and a marked enemy always loses (" + p.previewFight(foe, false).winChance + "%)", p.previewFight(foe, false).winChance == 0);
        PcProbe.cmd("cheat win off #" + foe.id, 30);
        Character runner = person(lab, 100, 30), chaser = person(lab, 30, 30); runner.trait = Character.Traits.Runner;
        int fled = 0; for (int i = 0; i < 300; i++) if (runner.fleeCheck(chaser)) fled++;
        PcProbe.cmd("cheat noescape on #" + chaser.id, 30);
        PcProbe.real.println("[T] info: noescape said: " + PcProbe.out().substring(Math.max(0, PcProbe.out().length() - 160)).replace((char) 10, (char) 124) + " flag=" + chaser.cheatNoEscape);
        int fled2 = 0; for (int i = 0; i < 300; i++) if (runner.fleeCheck(chaser)) fled2++;
        check("W2 nobody can flee from a person marked 'no escape' (fled " + fled + " of 300 before, " + fled2 + " after)", fled > 100 && fled2 == 0);

        // ---- CR: create
        int temp0 = Map.tempCharacters.size();
        PcProbe.cmd("cheat create npc 4 Alchemist", 30);
        check("CR1 a new person is made at the tile, in the realm and the trade asked (" + (Map.tempCharacters.size() - temp0) + " born)", Map.tempCharacters.size() == temp0 + 1 && Map.tempCharacters.get(Map.tempCharacters.size() - 1).majorLvl == 4 && Map.tempCharacters.get(Map.tempCharacters.size() - 1).trait == Character.Traits.Alchemist);
        int beasts0 = lab.monsters;
        PcProbe.cmd("cheat create beast 12", 30);
        check("CR2 wild beasts are added (" + beasts0 + " -> " + lab.monsters + ")", lab.monsters == beasts0 + 12);
        int aw0 = Character.stMonAwake, tc = Map.tempCharacters.size();
        PcProbe.cmd("cheat create monster 40 Grey Terror", 30);
        check("CR3 a Monster wakes (" + (Character.stMonAwake - aw0) + ")", Character.stMonAwake == aw0 + 1 && Map.tempCharacters.size() == tc + 1);

        // ---- CT, SE: city and sect
        lab.city = true; lab.population = 100; lab.cityTreasury = 5;
        PcProbe.cmd("cheat city population 54321", 30); PcProbe.cmd("cheat city treasury 777000", 30); PcProbe.cmd("cheat city owner self", 30); PcProbe.cmd("cheat city rename Probe Haven", 30);
        check("CT1 the city's people, treasury, lord and name are changed (" + lab.population + ", " + lab.cityTreasury + ", " + (lab.cityOwner == p) + ", " + lab.name + ")", lab.population == 54321 && lab.cityTreasury == 777000 && lab.cityOwner == p && lab.name.equals("Probe Haven"));
        lab.city = false;
        Area sa = null; for (Area x : Map.createdAreas) if (x != null && x.sect != null && x.sect.members.size() >= 1) { sa = x; break; }
        if (sa == null) { sa = lab; Organization made = new Organization("Probe Sect", lab); lab.sect = made; Character boss = person(lab, 50, 40); made.join(boss); made.master = boss; }
        if (false) { }
        else {
            Organization so = sa.sect;
            p.location.removeChar(p); p.location = sa; sa.addChar(p);
            Character cand = person(sa, 60, 30);
            PcProbe.cmd("cheat sect join #" + cand.id, 30);
            check("SE1 a person is forced into the sect (" + (cand.sect == so) + ")", cand.sect == so);
            PcProbe.cmd("cheat sect master #" + cand.id, 30);
            check("SE2 and made its master (" + (so.master == cand) + ")", so.master == cand);
            PcProbe.cmd("cheat sect expel #" + cand.id, 30);
            check("SE3 and cast out (" + (cand.sect == null) + ")", cand.sect == null);
            PcProbe.cmd("cheat sect rename The Probe Gate", 30);
            check("SE4 the sect is renamed (" + so.name + ")", so.name.equals("The Probe Gate"));
        }
        more(p, lab);
        System.out.println();
        PcProbe.real.println("[T] DONE");
    }
}
