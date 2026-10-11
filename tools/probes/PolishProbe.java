package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-06: the amateur craft menu and the cheaper crafting, the Study Trip, the squad and friendship policies (and `avoid`), the sect mission as a Quest Log entry, info commands while
 *  paused, Cheats for Cheaters, the gender in the portrait cache, and the Wandering Immortals' craft gifts. */
public class PolishProbe {
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
    static String text(Runnable r) { int m = PcProbe.mark(); r.run(); return PcProbe.since(m); }

    static Character adult(int lvl, int major) {
        Character c = new Character(home());
        c.age = 30; c.gender = "Male"; c.lvl = lvl; c.majorLvl = major; c.maxAge = 100000; c.caution = 50; c.sect = null;
        if (c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) c.trait = Character.Traits.Friendly;
        Map.characters.add(c); Map.population++;
        return c;
    }

    static void tp(Character c, Area to) { c.location.removeChar(c); c.location = to; to.addChar(c); }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        GameDriver.frozenNpcs = true;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        p.caution = 0; p.maxAge = 100000;
        GameDriver.time = Math.max(GameDriver.time, 100);
        tp(p, home());
        Area city = home(); city.city = true; city.population = 2000;

        // ---- C: crafting
        Character am = adult(30, 3); am.trait = Character.Traits.Runner;
        check("C1 a Runner has no trade (the Craft button used to waste the turn); the Gatherer's trade is gathering (2026-10-09)", !am.hasTrade() && adultTrait(Character.Traits.Gatherer).hasTrade());
        check("C1b a Blacksmith has one", adultTrait(Character.Traits.Blacksmith).hasTrade());
        am.materialBag.add(new Item(Item.ItemType.Metal, 2, 3000));
        int before = am.amateurLvl; double exp0 = am.amateurExp;
        String why = AmateurCraft.craft(am, "weapon");
        check("C2 an amateur makes a weapon from 1,000 metal (twice a trade's cost), it is equipped, and the skill grows", why == null && am.equippedWeapon != null && am.materialBag.get(0).quantity == 2000 && (am.amateurLvl > before || am.amateurExp > exp0) && am.equippedWeapon.lvl <= AmateurCraft.cap(am));
        check("C2b the level an amateur reaches is capped low (10 + skill, and their realm)", AmateurCraft.cap(adultTrait(Character.Traits.Cheater)) == 10);
        Character poor = adultTrait(Character.Traits.Cheater);
        String refusal = AmateurCraft.craft(poor, "weapon");
        check("C3 without materials nothing is made and the reason says what is needed", refusal != null && refusal.contains("metal") && poor.equippedWeapon == null);
        am.materialBag.add(new Item(Item.ItemType.Herb, 1, 4500));
        check("C4 clothing, an array, a pill and a meal can all be made by anyone", AmateurCraft.craft(am, "clothing") == null && AmateurCraft.craft(am, "array") == null && AmateurCraft.craft(am, "pill") == null && AmateurCraft.craft(am, "food") == null
                && am.equippedClothing != null && !am.arrayBag.isEmpty() && am.pillBag.stream().anyMatch(i -> i.pillType == Item.PillType.Cultivation) && Life.firstOf(am, Item.ItemType.Food) != null);
        Character smith = adultTrait(Character.Traits.Blacksmith); smith.professionLvl = 10; smith.materialBag.add(new Item(Item.ItemType.Metal, 1, 600));
        smith.smithing();
        check("C5 a smith's weapon costs 500 metal now (it was 1,000)", smith.backpack.stream().anyMatch(i -> i.itemType == Item.ItemType.Weapon) && smith.materialBag.stream().anyMatch(i -> i.quantity == 100));
        Character cmd = p; Character.Traits keepTrait = p.trait;
        p.trait = Character.Traits.Cheater; p.materialBag.add(new Item(Item.ItemType.Metal, 1, 2000)); p.equippedWeapon = null;
        String menu = text(() -> PlayerCommands.handle(p, "craft"));
        boolean madeByCommand = PlayerCommands.handle(p, "craft weapon");
        check("C6 'craft' shows the menu for a trait without a trade (no turn), 'craft weapon' makes one (a turn)", menu.contains("Craft") && menu.contains("amateur") && madeByCommand && p.equippedWeapon != null);
        p.trait = keepTrait;
        int broke = 0;
        for (int i = 0; i < 200_000; i++) if (p.durabilityCheckUtil(20, 20)) broke++;
        double pct = broke / 2000.0;
        check("C7 gear breaks about half as often (equal realm, item Lv.20: 4.1% a fight, it was 8.2%) measured " + String.format("%.2f", pct) + "%", pct > 3.4 && pct < 4.8);

        // ---- S: Study Trip
        Library lib = new Library(city); lib.techniques.add(new Technique(Technique.techniqueType.Martial, p, 40)); city.libraries.add(0, lib);
        Character scholar = adult(30, 3); scholar.ss = 5_000_000; scholar.convertCurrency();
        Quest st = new Quest("Study Trip", true, 35, 2, 60, city);
        check("S1 a library with a better technique is worth a trip for someone who can pay", scholar.bestStudy(city) != null && scholar.questViable(st) == 1 && scholar.studyTarget() == city);
        check("S2 arriving, the character buys it", scholar.studyHere() && (scholar.martialTechnique.potential == 40 || scholar.movementTechnique.potential == 40 || scholar.techniqueList.stream().anyMatch(x -> x.potential == 40)));
        Character broke2 = adult(30, 3); broke2.ss = 0; broke2.sc = 0; broke2.dc = 0;
        check("S3 someone who cannot pay has no reason to go", broke2.bestStudy(city) == null && broke2.questViable(st) == 0);
        Character again = adult(30, 3); again.ss = 5_000_000; again.convertCurrency(); again.techniqueList.add(new Technique(lib.techniques.get(0)));
        check("S3b nobody buys a book they already know", again.bestStudy(city) == null);

        // ---- P: policies
        PlayerControl pc = p.pc();
        Character friend = adult(30, 3); friend.affiliation = p.affiliation; friend.affiliationNum = p.affiliationNum;
        pc.friendPolicy = 1; // accept
        friend.changeRelationship(p, 1);
        check("P1 friends policy 'accept': the friendship forms", p.relationshipList.contains(friend.id) != null && p.relationshipList.contains(friend.id).value == 1);
        pc.squadPolicy = 2; // never
        for (int i = 0; i < 80; i++) p.tryRallyParty(false);
        check("P2 squad policy 'never': nobody joins, however often you travel", friend.partyLeaderId != p.id);
        pc.squadPolicy = 0; DecisionBroker.scripted.clear(); DecisionBroker.scripted.addAll(Arrays.asList(1, 1, 1, 1, 1, 1, 1, 1, 1, 1)); // ask, and Decline
        for (int i = 0; i < 80 && pc.squadDeclined.isEmpty(); i++) p.tryRallyParty(false);
        check("P3 squad policy 'ask' (MANUAL): you are asked; declining turns them away and they are not asked again", friend.partyLeaderId != p.id && pc.squadDeclined.contains(friend.id));
        pc.squadPolicy = 1; pc.squadDeclined.clear();
        for (int i = 0; i < 200 && friend.partyLeaderId != p.id; i++) p.tryRallyParty(false);
        check("P4 squad policy 'auto': the friend joins", friend.partyLeaderId == p.id);
        friend.leaveParty();
        Character stranger = adult(30, 3); pc.friendPolicy = 2; // decline
        stranger.changeRelationship(p, 1);
        Relationship r = p.relationshipList.contains(stranger.id);
        check("P5 friends policy 'decline': no friendship, the refusal is kept, and it costs nothing", (r == null || r.value < 1) && r != null && r.refused);
        stranger.changeRelationship(p, 1);
        check("P5b ... and later chances do not make a friend of them either", p.relationshipList.contains(stranger.id).value < 1);
        Character asked = adult(30, 3); pc.friendPolicy = 0; DecisionBroker.scripted.clear(); DecisionBroker.scripted.add(0);
        asked.changeRelationship(p, 1);
        check("P6 friends policy 'ask': you are asked, and Accept makes the friend", p.relationshipList.contains(asked.id) != null && p.relationshipList.contains(asked.id).value == 1);
        pc.friendPolicy = 1;
        p.avoid(friend, true);
        friend.changeRelationship(p, 3);
        check("P7 'avoid' sets a friend back to nothing and blocks the friendship; 'avoid off' lifts it", p.relationshipList.contains(friend.id).value <= 0);
        p.avoid(friend, false); friend.changeRelationship(p, 1);
        check("P7b lifted: the friendship can form again", p.relationshipList.contains(friend.id).value == 1);
        String pol = text(() -> PlayerCommands.handle(p, "policy"));
        check("P8 the policy listing shows both", pol.contains("squad") && pol.contains("friends"));
        PlayerCommands.handle(p, "policy squad ask"); PlayerCommands.handle(p, "policy friends ask");
        check("P8b both can be set (ask is the default for a MANUAL player)", pc.squadPolicy == 0 && pc.friendPolicy == 0);

        Character kid = adult(5, 0); kid.age = 0; kid.mother = p; kid.trait = null; // a newborn: no trait yet, and family
        int pr0 = DecisionBroker.prompts; pc.friendPolicy = 0;
        kid.changeRelationship(p, 1);
        check("P9 a child's bond is never offered as a friendship (a newborn used to crash the game in the constructor)", DecisionBroker.prompts == pr0 && p.relationshipList.contains(kid.id) != null);
        kid.trait = Character.Traits.Friendly; pc.friendPolicy = 1; PlayerCommands.handle(p, "policy friends ask");

        // ---- M: the sect mission in the Quest Log
        Character master = adult(60, 4);
        Organization sect = new Organization(master);
        sect.questBoard.clear(); sect.questBoard.add("Forage|Herb|1|500|15"); sect.questBoard.add("Hunt|Beast|2|3|20");
        if (p.sect != null) p.sect.resign(p);
        sect.members.add(p); p.sect = sect; sect.memberRanks.put(p.id, Organization.SectRank.OUTER_DISCIPLE);
        tp(p, sect.location);
        check("M1 the mission board reads in plain words", Character.missionText("Forage|Herb|1|500|15").contains("Forage 500 herbs") && Character.missionText("Hunt|Beast|2|3|20").contains("Hunt 3"));
        boolean taken = p.acceptMission(sect.questBoard.get(0));
        Quest mq = null; for (Quest q : p.targetQuests) if ("Sect Mission".equals(q.name)) mq = q;
        check("M2 an accepted mission is a pinned quest in the Quest Log, labelled with what it asks", taken && mq != null && mq.displayName().startsWith("Sect Mission: Forage 500 herbs") && mq.pinned && mq.targetArea == sect.location);
        String board = text(() -> PlayerCommands.handle(p, "mission"));
        check("M3 'mission' lists the board and the mission you hold", board.contains("Missions of") && board.contains("Yours:"));
        p.materialBag.add(new Item(Item.ItemType.Herb, 2, 600));
        long sp0 = p.sectPoints;
        String done = p.doQuestNow(mq);
        p.syncMissionQuest();
        check("M4 handing it in at the sect's home pays the points, clears the mission and the quest", done == null && p.sectMission == null && p.sectPoints == sp0 + 15 && p.targetQuests.stream().noneMatch(q -> "Sect Mission".equals(q.name)));
        PlayerCommands.handle(p, "mission 2");
        Quest hq = null; for (Quest q : p.targetQuests) if ("Sect Mission".equals(q.name)) hq = q;
        check("M5 'mission <n>' takes the one you choose", p.sectMission != null && p.sectMission.startsWith("Hunt|") && hq != null);
        p.pc().mode = PlayerControl.Mode.AUTO; p.syncMissionQuest();
        boolean gone = p.targetQuests.stream().noneMatch(q -> "Sect Mission".equals(q.name));
        p.pc().mode = PlayerControl.Mode.MANUAL; p.syncMissionQuest();
        check("M6 a character on AUTO runs the mission by itself (no queue entry), MANUAL shows it again", gone && p.targetQuests.stream().anyMatch(q -> "Sect Mission".equals(q.name)));
        PlayerCommands.handle(p, "mission abandon");
        check("M7 'mission abandon' drops it and its quest", p.sectMission == null && p.targetQuests.stream().noneMatch(q -> "Sect Mission".equals(q.name)));
        p.sect.resign(p); tp(p, city);

        // ---- I: commands that only look are answered while paused
        check("I1 info commands are recognised (time, library, route, bag, 'craft menu', the mission board)", PlayerCommands.isInfoCommand("time") && PlayerCommands.isInfoCommand("library type=martial") && PlayerCommands.isInfoCommand("route 3 4") && PlayerCommands.isInfoCommand("craft menu") && PlayerCommands.isInfoCommand("mission"));
        check("I2 commands that act are not (goto, learn, sell, craft weapon, mission 2, policy)", !PlayerCommands.isInfoCommand("goto 3 4") && !PlayerCommands.isInfoCommand("learn 1") && !PlayerCommands.isInfoCommand("sell") && !PlayerCommands.isInfoCommand("craft weapon") && !PlayerCommands.isInfoCommand("mission 2") && !PlayerCommands.isInfoCommand("!wait 5"));

        // ---- X: cheats, the portrait, the Wandering Immortals
        p.trait = Character.Traits.Friendly;
        String refused = text(() -> PlayerCommands.handle(p, "cheat self ss 5"));
        check("X1 cheats are refused to anyone who is not a Cheater", refused.contains("Only a Cheater"));
        p.trait = Character.Traits.Cheater; long w0 = PlayerLists.wealth(p); PlayerCommands.handle(p, "cheat self ss 5000");
        check("X1b ... and work for a Cheater", PlayerLists.wealth(p) >= w0 + 5000);
        p.trait = keepTrait;
        Character model = adult(30, 3); model.gender = "Male";
        java.awt.image.BufferedImage m1 = ArtMore.portraitCached(model, 64, 64);
        model.gender = "Female";
        java.awt.image.BufferedImage m2 = ArtMore.portraitCached(model, 64, 64);
        check("X2 the portrait is painted again when the gender is edited", m1 != m2);

        int okGifts = 0, tried = 0;
        Random rnd = new Random(1);
        Character.Traits[] gifters = {Character.Traits.Blacksmith, Character.Traits.Tailor, Character.Traits.Scribe, Character.Traits.Alchemist, Character.Traits.Chef, Character.Traits.Drunken, Character.Traits.Poisoner, Character.Traits.Musician, Character.Traits.Innovative, Character.Traits.Merchant, Character.Traits.Gatherer, Character.Traits.Miner};
        StringBuilder missing = new StringBuilder();
        for (Character.Traits tr : gifters) {
            Character wi = adultTrait(tr); wi.wanderingImmortal = true; Character to = adult(30, 3);
            int items0 = to.backpack.size() + to.arrayBag.size() + to.pillBag.size() + (to.goods == null ? 0 : to.goods.size()) + to.techniqueList.size() + to.materialBag.size(); long ss1 = to.ss + to.sc;
            String g = LoreEndings.professionGift(wi, to, rnd);
            int items1 = to.backpack.size() + to.arrayBag.size() + to.pillBag.size() + (to.goods == null ? 0 : to.goods.size()) + to.techniqueList.size() + to.materialBag.size();
            tried++;
            if (g != null && (items1 > items0 || to.ss + to.sc > ss1 || to.equippedWeapon != null || to.equippedClothing != null || to.martialTechnique.potential > 3 || to.movementTechnique.potential > 3)) okGifts++; else missing.append(tr).append(' ');
        }
        check("X3 each trade's Wandering Immortal leaves something of that trade (" + okGifts + " of " + tried + (missing.length() > 0 ? "; not: " + missing : "") + ")", okGifts == tried);
        Character plain = adultTrait(Character.Traits.Cheater); plain.wanderingImmortal = true;
        check("X3b a trait with no craft leaves only the usual pills", LoreEndings.professionGift(plain, adult(30, 3), rnd) == null);
    }

    static Character adultTrait(Character.Traits tr) { Character c = adult(30, 3); c.trait = tr; return c; }
}
