package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-10 (second batch): a sect mission to hunt counts every beast fought and shows how far it has got, what the character does by itself while travelling (spotaction),
 *  `seek <id> talk|court`, `explore`, spoiled goods paying their owner, no Register Tournament for a sect member at the Convocation, nobody under 18 dies, and the boards
 *  (Strongest / Richest / Most Kills) with their titles and the epoch's exemptions. */
public class FixesProbe {
    static void check(String name, boolean ok) { PcProbe.passes += ok ? 1 : 0; PcProbe.fails += ok ? 0 : 1; PcProbe.real.println("[T] " + (ok ? "PASS " : "FAIL ") + name); }

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

    static Area home() { return Map.tiles[Map.range / 2][Map.range / 2]; }

    static Character adult(int lvl, int major) {
        Character c = new Character(home());
        c.age = 30; c.gender = "Male"; c.lvl = lvl; c.majorLvl = major; c.maxAge = 100000; c.caution = 50; c.sect = null;
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
        for (int k = 3; k < 14; k++) for (int[] d : new int[][]{{k, 3}, {3, k}, {-k, 3}}) {
            Area a = tile(home().x + d[0], home().y + d[1]);
            if (a != null && !a.city && a.sect == null && a.dungeonType == null && a.loreSite == null && !a.spiritualVein && !not.contains(a)) return a;
        }
        return null;
    }
    static String say(Character p, String cmd) { int m = PcProbe.mark(); PlayerCommands.handle(p, cmd); return PcProbe.since(m); }

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

        // ---- A: a sect mission to hunt counts every beast fought
        Organization sect = new Organization("Probe Sect", away);
        p.sect = sect;
        Area lair = plain(used); used.add(lair);
        lair.monsters = 40; lair.lvl = 1; lair.highestLvl = 1;
        tp(p, lair);
        p.sectMission = "Hunt|Beast|1|3|10"; p.sectMissionBonus = 10; p.sectMissionTotal = 3;
        p.syncMissionQuest();
        check("A1 the mission shows the counter (" + p.missionLine() + ")", p.missionLine().contains("(0/3 done)"));
        int pts0 = p.sectPoints;
        p.partyBeastHunt();
        check("A2 one beast fought by the hunt command counts (" + p.missionLine() + ")", p.sectMission != null && p.missionLine().contains("(1/3 done)"));
        Quest log = null; for (Quest q : p.targetQuests) if ("Sect Mission".equals(q.name)) log = q;
        check("A3 the Quest Log entry follows the counter and waits on beast ground (" + (log == null ? "none" : log.label + " @" + (log.targetArea == null ? "-" : log.targetArea.name)) + ")", log != null && log.label.contains("1/3") && log.targetArea == lair);
        p.beastBattleCheck(); p.beastBattleCheck();
        check("A4 the third beast completes it and pays the bonus (mission " + p.sectMission + ", points +" + (p.sectPoints - pts0) + ")", p.sectMission == null && p.sectPoints - pts0 >= 10);
        check("A5 the Quest Log entry is gone with the mission", p.targetQuests.stream().noneMatch(q -> "Sect Mission".equals(q.name)));
        Item herb = new Item(Item.ItemType.Herb, 2, 40);
        p.materialBag.add(herb);
        p.sectMission = "Forage|Herb|2|100|8"; p.sectMissionTotal = 100;
        check("A6 a forage mission shows what is carried (" + p.missionLine() + ")", p.missionLine().contains("carrying 40/100"));
        p.sectMission = null; p.materialBag.remove(herb);
        tp(p, away);

        // ---- B: what the character does by itself while travelling
        PlayerControl pc = p.pc();
        check("B1 the default is Meditate", pc.spotAction == 0 && PlayerControl.SPOT_ACTIONS[0].equals("Meditate"));
        java.lang.reflect.Method chosen = Character.class.getDeclaredMethod("chosenSpot", Quest.class);
        chosen.setAccessible(true);
        Quest hunt = new Quest("Hunt", false, 30, 5, 50), forage = new Quest("Forage", false, 40, 3, 70);
        String out = say(p, "spotaction profession");
        check("B2 'spotaction profession' sets it (" + out.trim().replace("\n", " ").substring(0, Math.min(90, out.trim().length())) + ")", pc.spotAction == 2);
        check("B3 the chosen one replaces a plain spot quest", "Profession".equals(((Quest) chosen.invoke(p, hunt)).name));
        check("B4 a Forage the routine picked runs as it was", chosen.invoke(p, forage) == forage);
        say(p, "spotaction hunt");
        away.monsters = 0;
        check("B5 Hunt with no beasts in the tile meditates", "Meditate".equals(((Quest) chosen.invoke(p, new Quest("Random", false, 35, 2, 90))).name));
        away.monsters = 5;
        check("B6 Hunt with beasts hunts", "Hunt".equals(((Quest) chosen.invoke(p, new Quest("Meditate", false, 15, 2, 40))).name));
        say(p, "spotaction meditate");
        check("B7 back to Meditate", pc.spotAction == 0);
        say(p, "spotaction random");
        check("B8 Random is the fourth choice", pc.spotAction == 3);
        say(p, "spotaction meditate");

        // ---- D: seek talk / court
        Character friend = adult(30, 3), lady = adult(30, 3), kin = adult(30, 3);
        lady.gender = "Female"; friend.gender = "Male"; kin.gender = "Female";
        tp(friend, tile(home().x + 7, home().y + 2)); tp(lady, tile(home().x + 8, home().y + 2));
        int before = p.targetQuests.size();
        say(p, "seek " + friend.id + " talk");
        Quest talk = null; for (Quest q : p.targetQuests) if ("Talk To".equals(q.name) && q.targetCharacter == friend.id) talk = q;
        check("D1 'seek <id> talk' pins a Talk To quest on someone far away", talk != null && talk.userCreated && talk.pinned);
        say(p, "seek " + lady.id + " court");
        Quest court = null; for (Quest q : p.targetQuests) if ("Courtship".equals(q.name) && q.targetCharacter == lady.id) court = q;
        check("D2 'seek <id> court' pins a Courtship quest for a suitable woman", court != null && court.userCreated);
        int n1 = p.targetQuests.size();
        String refused = say(p, "seek " + friend.id + " court");
        check("D3 courting a man (same gender) is refused (" + refused.trim().replace("\n", " ") + ")", p.targetQuests.size() == n1 && refused.contains("cannot be courted"));
        tp(friend, away);
        int r = talk == null ? -9 : p.targetQuests(talk);
        check("D4 at their side the Talk To quest runs and ends (" + r + ")", r == 1);

        // ---- C: explore
        Area ruins = plain(used); used.add(ruins); ruins.dungeonType = Area.DungeonType.HAUNTED_GROUNDS;
        String nothing = say(p, "explore");
        check("C1 'explore' where there is nothing says so", nothing.contains("nothing to explore"));
        tp(p, ruins);
        long cultBefore = (long) p.cultivationBase; int lvlBefore = p.lvl;
        String did = say(p, "explore");
        check("C2 'explore' at Haunted Grounds is done (" + did.trim().replace("\n", " ") + ")", did.contains("Explore Dungeon done") || did.contains("done"));
        tp(p, away);

        // ---- E: spoiled goods pay their owner
        Character hoarder = adult(30, 3); hoarder.trait = Character.Traits.Friendly;
        hoarder.goods = new ArrayList<>();
        hoarder.goods.add(new Item(Item.ItemType.Food, 1, 100, "Probe Congee"));
        long ss0 = hoarder.ss;
        int refund0 = Settings.spoilRefundPercent;
        Settings.spoilRefundPercent = 100;
        Life.marketYearly();
        long gained = hoarder.ss - ss0;
        check("E1 a hoard that spoils pays its owner back (+" + gained + " ss)", gained > 0);
        hoarder.goods.clear(); hoarder.goods.add(new Item(Item.ItemType.Food, 1, 100, "Probe Congee"));
        Settings.spoilRefundPercent = 50; long ss1 = hoarder.ss;
        Life.marketYearly();
        long half = hoarder.ss - ss1;
        check("E2 at 50% it pays half (" + half + " against " + gained + ")", Math.abs(half * 2 - gained) <= 2);
        hoarder.goods.clear(); hoarder.goods.add(new Item(Item.ItemType.Food, 1, 100, "Probe Congee"));
        Settings.spoilRefundPercent = 0; long ss2 = hoarder.ss;
        Life.marketYearly();
        check("E3 at 0% nothing is paid", hoarder.ss == ss2);
        Settings.spoilRefundPercent = 100;
        Character owner = adult(30, 3); owner.trait = Character.Traits.Merchant;
        Shop shop = new Shop(owner, home());
        List<Item> shelf = Life.shelf(shop);
        shelf.clear(); shelf.add(new Item(Item.ItemType.Wine, 1, 100, "Probe Wine"));
        long back = Life.spoilList(shelf, shop);
        check("E4 a shelf's lost portions are worth what the owner paid (" + back + ")", back > 0 && shelf.get(0).quantity < 100);
        Settings.spoilRefundPercent = refund0;

        // ---- F: the Convocation is not for someone who has a sect
        Area pal = Lore.palaceArea();
        if (Lore.state != null && Settings.lore && Lore.palace() != null) {
            boolean open0 = Lore.state.convocationOpen; int timer0 = pal.tournamentTimer, min0 = pal.tournamentMinMajor, max0 = pal.tournamentMaxMajor;
            Lore.state.convocationOpen = true; pal.tournamentTimer = 5; pal.tournamentMinMajor = 0; pal.tournamentMaxMajor = 10;
            Character free = adult(30, 3), bound = adult(30, 3);
            tp(free, pal); tp(bound, pal); bound.sect = sect;
            Quest reg = new Quest("Register Tournament", true, 60, 0, 80);
            int rf = free.spotQuests(reg), rb = bound.spotQuests(new Quest("Register Tournament", true, 60, 0, 80));
            check("F1 a sectless cultivator can sign up for the Convocation, one with a sect cannot (" + rf + ", " + rb + ")", rf == 2 && rb == -2 && pal.tournamentRegistrants.contains(free) && !pal.tournamentRegistrants.contains(bound));
            check("F2 the tournament is known as the sect-entry one, a prize tournament elsewhere is not", Lore.isEntryTournament(pal) && !Lore.isEntryTournament(away));
            away.tournamentTimer = 5; away.tournamentMinMajor = 0; away.tournamentMaxMajor = 10;
            Character lodger = adult(30, 3); lodger.sect = sect; tp(lodger, away);
            check("F3 a prize tournament stays open to a sect member", lodger.spotQuests(new Quest("Register Tournament", true, 60, 0, 80)) == 2 && away.tournamentRegistrants.contains(lodger));
            away.tournamentTimer = 0; away.tournamentRegistrants.clear();
            pal.tournamentRegistrants.clear(); pal.tournamentTimer = timer0; pal.tournamentMinMajor = min0; pal.tournamentMaxMajor = max0; Lore.state.convocationOpen = open0;
            tp(free, away); tp(bound, away); tp(lodger, away);
        } else check("F1-F3 skipped: no Palace in this world", true);

        // ---- H: nobody under 18 dies
        Character killer = adult(60, 5);
        Character kid = adult(10, 1); kid.age = 12;
        kid.dead(killer);
        check("H1 a child struck down by a killer lives", !kid.dead);
        kid.dead();
        check("H2 and by any other death", !kid.dead);
        kid.location = home();
        Character teen = adult(10, 1); teen.age = 17;
        killer.kills = 0;
        killer.winCheck(teen, true);
        check("H3 a lost lethal fight leaves a 17-year-old hurt, with no kill credited (" + teen.dead + ", kills " + killer.kills + ")", !teen.dead && killer.kills == 0);
        Character grown = adult(10, 1); grown.age = 18;
        grown.dead(killer);
        check("H4 at 18 death is possible again", grown.dead);
        check("H5 children are exempt from the epoch", Rankings.epochSafe(teen) && !Rankings.epochSafe(adult(10, 1)));

        // ---- I: the boards
        List<Character> world = new ArrayList<>();
        for (Character c : Map.characters) if (c != null) { world.add(c); }
        // 12 killers far above everyone, 3 rich, 1 strong
        Character[] top = new Character[12];
        for (int i = 0; i < top.length; i++) { top[i] = adult(40, 4); top[i].kills = 10_000_000 - i * 100; top[i].gender = i % 2 == 0 ? "Male" : "Female"; }
        Character rich = adult(40, 4); rich.ss = 5_000_000_000_000L;
        Character mighty = adult(99, 9); mighty.majorLvl = 10; mighty.lvl = 100; mighty.gender = "Female";
        Rankings.yearly(Map.characters);
        check("I1 the best killer is rank 1 and the next is rank 2 (" + top[0].rankKills + ", " + top[1].rankKills + ")", top[0].rankKills == 1 && top[1].rankKills == 2);
        int[] strongSaved = new int[top.length]; for (int i = 0; i < top.length; i++) { strongSaved[i] = top[i].rankStrong; top[i].rankStrong = 0; top[i].rankRich = 0; } // (the killers also rank among the strongest: the best title wins, so look at this board alone)
        check("I2 ranks 1-3 hold the grandest title, 4-10 the lesser, 11-12 the plainest (" + Rankings.title(top[0]) + " / " + Rankings.title(top[3]) + " / " + Rankings.title(top[10]) + ")",
            "Crimson Calamity".equals(Rankings.title(top[0])) && "Nine-Hell Reaper".equals(Rankings.title(top[3])) && "Butcher of the Hundred".equals(Rankings.title(top[10])));
        for (int i = 0; i < top.length; i++) top[i].rankStrong = strongSaved[i];
        check("I3 the richest is rank 1 of the Richest and the strongest of the Strongest, which my own level-100 character shares (" + rich.rankRich + ", " + mighty.rankStrong + ")", rich.rankRich == 1 && mighty.rankStrong <= 2 && p.rankStrong <= 2);
        check("I4 a woman gets the woman's title (" + Rankings.title(mighty) + ")", "Empress of the Nine Heavens".equals(Rankings.title(mighty)));
        Naming.assignTitles(Map.characters);
        check("I5 the yearly titles give the board title (" + top[0].repTitle + ")", "Crimson Calamity".equals(top[0].repTitle));
        int ranked = 0, kills = 0;
        for (Character c : Map.characters) if (c != null) { if (c.rankKills > 0) kills++; if (c.rankKills > 100 || c.rankRich > 100) ranked = -1000; }
        check("I6 a board holds at most a hundred (" + kills + ")", kills <= 100 && ranked >= 0);
        Character plain1 = adult(10, 1); plain1.age = 40;
        check("I7 the epoch spares everyone on a board and takes a plain adult", Rankings.epochSafe(top[0]) && Rankings.epochSafe(rich) && !Rankings.epochSafe(plain1));
        // the thousand strongest are safe, the rest are not
        int safe1000 = 0; for (Character c : Map.characters) if (c != null && c.rankStrong > 0) safe1000++;
        check("I8 the strongest thousand are marked (" + safe1000 + "; the world has " + Map.population + ")", safe1000 > 0 && safe1000 <= Rankings.STRONG_SAFE);
        String text = Rankings.text(null, p);
        check("I9 the 'ranking' text lists the three boards", text.contains("Strongest") && text.contains("Richest") && text.contains("Most Kills"));
        String viaCmd = say(p, "ranking kills");
        check("I10 the command shows a board", viaCmd.contains("Most Kills") || PcProbe.out().contains("Most Kills"));
        if (Lore.state != null && Settings.lore && Lore.palace() != null) {
            Character member = adult(30, 3); member.sect = Lore.palace();
            check("I11 a member of the Palace is on the story's road and spared", Lore.onStoryPath(member) && Rankings.epochSafe(member));
            member.sect = null;
        }
        check("Z idMismatch 0", PcProbe.idMismatch() == 0);
    }
}
