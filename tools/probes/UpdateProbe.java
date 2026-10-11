package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-09: the Quest Log drops quests whose target is gone, meeting a walker, Hunt Monster, a Monster from every wild beast's kill, Meditate (1.5 ticks + the four techniques),
 *  Inspiration, the Profession button, tournament and treasure notices, scripts that run any command, and saved scripts. */
public class UpdateProbe {
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

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        GameDriver.frozenNpcs = true;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        p.caution = 0; p.maxAge = 100000; p.pc().squadPolicy = 2; p.pc().friendPolicy = 1; p.lvl = 100; p.majorLvl = 10;
        GameDriver.time = Math.max(GameDriver.time, 100);
        Map.currentCharacter = p;
        Set<Area> used = new HashSet<>();
        Area away = plain(used); used.add(away);
        Area far = plain(used); used.add(far);
        tp(p, away);

        // ---- Q: the Quest Log drops quests whose target is gone
        Area ruins = plain(used); used.add(ruins); ruins.dungeonType = Area.DungeonType.LOST_RUINS;
        Area explored = plain(used); used.add(explored); explored.dungeonType = Area.DungeonType.BEAST_LAIR;
        Area lair = plain(used); used.add(lair); lair.monsters = 50;
        Character victim = adult(20, 2);
        p.targetQuests.add(new Quest("Explore Dungeon", true, 35, 2, 60, ruins));
        p.targetQuests.add(new Quest("Explore Dungeon", true, 35, 2, 60, explored));
        p.targetQuests.add(new Quest("Hunt Beast", true, 35, 2, 60, lair));
        p.targetQuests.add(new Quest("Battle Rival", false, 18, 2, 42, victim));
        explored.dungeonType = null; // hunted out meanwhile
        int m = PcProbe.mark();
        p.quest();
        boolean keptRuins = p.targetQuests.stream().anyMatch(q -> q.targetArea == ruins), droppedExplored = p.targetQuests.stream().noneMatch(q -> q.targetArea == explored);
        check("Q1 an alert for a dungeon that is gone is dropped, one that stands stays", keptRuins && droppedExplored);
        check("Q2 the player is told why: " + PcProbe.since(m).replace("\n", " | ").substring(0, Math.min(160, PcProbe.since(m).length())), PcProbe.since(m).contains("Quest dropped: Explore Dungeon"));
        victim.dead = true;
        lair.monsters = 0;
        p.quest();
        check("Q3 a dead person's and an emptied hunting ground's quests are dropped", p.targetQuests.stream().noneMatch(q -> q.targetCharacter == victim.id && "Battle Rival".equals(q.name)) && p.targetQuests.stream().noneMatch(q -> q.targetArea == lair));
        check("Q4 the Hunt Beast quest is shown as Hunt Monster and found by that name", new Quest("Hunt Beast", true, 35, 2, 60, lair).displayName().equals("Hunt Monster") && Quest.canon("hunt monster").equals("Hunt Beast"));

        // ---- I: meeting a walker one or two tiles ahead
        Area h0 = tile(home().x, home().y);
        Character chaser = adult(30, 3), walker = adult(30, 3);
        chaser.trait = Character.Traits.Cheater; walker.trait = Character.Traits.Cheater;
        tp(chaser, h0); tp(walker, h0); walker.movement('d'); // walker stands one tile east
        Area goal = Area.ghost(walker.location.x, walker.location.y + 10);
        walker.currentQuest = new Quest("Go: far", true, 50, 0, 50, goal);
        int[] aim = chaser.interceptAim(walker);
        check("I1 one tile behind a walker the aim is the tile they step to (" + Arrays.toString(aim) + ")", aim != null && aim[0] == walker.location.x && aim[1] == walker.location.y + 1);
        Quest chase = new Quest("Battle Rival", false, 18, 2, 42, walker);
        chaser.currentQuest = chase;
        int caught = 0;
        for (int round = 0; round < 6 && caught == 0; round++) {
            chaser.targetQuests(chase); // (nothing)
            int r = chaserStep(chaser, chase);
            walker.pathfinder(goal);    // the walker takes its step after
            if (chaser.location == walker.location) caught = round + 1;
        }
        check("I2 a chaser who meets the walker where they are going catches them (round " + caught + ")", caught > 0 && caught <= 2);
        Character far1 = adult(30, 3); tp(far1, tile(home().x + 8, home().y)); 
        check("I3 beyond two tiles the plain walk to their tile is used", chaser.interceptAim(far1) == null);

        check("I4 idMismatch 0", PcProbe.idMismatch() == 0);
        // ---- W: every wild beast's kill wakes a Monster, a tamed one's does not
        Character prey = adult(20, 2), prey2 = adult(20, 2); tp(prey, away); tp(prey2, away);
        Character beast = new Character(away, prey); beast.trait = Character.Traits.Beast; beast.masterId = -1; beast.location = away;
        int temp0 = Map.tempCharacters.size();
        prey.dead(beast);
        check("W1 a wild beast that kills a cultivator awakens as a Monster", beast.trait == Character.Traits.Monster && Map.tempCharacters.size() > temp0);
        Character pet = new Character(away, prey2); pet.trait = Character.Traits.Beast; pet.masterId = p.id; pet.location = away;
        prey2.dead(pet);
        check("W2 a tamed beast's kill wakes nothing", pet.trait == Character.Traits.Beast);

        // ---- M: Meditate
        Character mc = adult(30, 3); mc.trait = Character.Traits.Cheater; mc.potential = 50; mc.fx = null;
        mc.cultivation = 100; mc.cultivationBase = 0; mc.cultivationBaseMax = 100000;
        Character cc = adult(30, 3); cc.trait = Character.Traits.Cheater; cc.cultivation = 100; cc.cultivationBase = 0; cc.cultivationBaseMax = 100000; cc.fx = null;
        mc.meditate(); cc.cultivate();
        check("M1 meditating gives one and a half cultivation ticks (" + mc.cultivationBase + " against " + cc.cultivationBase + ")", Math.abs(mc.cultivationBase - 1.5 * cc.cultivationBase) < 1e-6);
        Technique[] ts = {mc.martialTechnique, mc.movementTechnique};
        for (Technique t : ts) { t.potential = 50; t.lvl = 10; }
        mc.martialTechnique2 = new Technique(Technique.techniqueType.Martial, mc, 40); mc.martialTechnique2.lvl = 5;
        mc.movementTechnique2 = new Technique(Technique.techniqueType.Movement, mc, 40); mc.movementTechnique2.lvl = 40;
        mc.meditate();
        check("M2 every equipped technique gains exactly one level, one at its potential does not", ts[0].lvl == 11 && ts[1].lvl == 11 && mc.martialTechnique2.lvl == 6 && mc.movementTechnique2.lvl == 40);

        // ---- N: Inspiration
        Character inv = adult(30, 3); inv.trait = Character.Traits.Innovative; inv.professionLvl = 10;
        Character foe = adult(30, 3); foe.trait = Character.Traits.Cheater;
        inv.gainInspiration(foe, true); int afterWin = inv.inspiration; inv.gainInspiration(foe, false);
        check("N1 a won fight gives 5 inspiration (2.5 to 10 by the foe), a lost one 2 (1 to 4) (" + afterWin + ", then " + inv.inspiration + ")", afterWin >= 3 && afterWin <= 10 && inv.inspiration - afterWin >= 1 && inv.inspiration - afterWin <= 4 && afterWin > inv.inspiration - afterWin);
        Character nonInv = adult(30, 3); nonInv.trait = Character.Traits.Cheater; nonInv.gainInspiration(foe, true);
        check("N2 only the Innovative gain it", nonInv.inspiration == 0);
        Area wild = plain(used); used.add(wild); wild.monsters = 500; wild.lvl = 10; wild.highestLvl = 12;
        Character hunter = adult(40, 4); hunter.trait = Character.Traits.Innovative; tp(hunter, wild); hunter.tempLvlCheck();
        int fights = 0; for (int i = 0; i < 60 && hunter.inspiration == 0; i++) { hunter.beastBattleCheck(); fights++; if (hunter.dead) break; }
        check("N3 a hunt is a fight: it gave inspiration (" + hunter.inspiration + " after " + fights + " hunts)", hunter.inspiration > 0 || hunter.dead);
        inv.inspiration = 100; int techs0 = inv.techniqueList.size();
        inv.professionPicker();
        check("N4 with 100 inspiration an Innovative character makes a technique that is kept (" + techs0 + " -> " + inv.techniqueList.size() + ", inspiration " + inv.inspiration + ")", inv.techniqueList.size() == techs0 + 1 && inv.inspiration == 0);
        inv.inspiration = 30; int techs1 = inv.techniqueList.size();
        for (int i = 0; i < 200; i++) inv.professionPicker();
        check("N5 without the inspiration no technique appears by chance", inv.techniqueList.size() == techs1);

        // ---- T: tournament and treasure notices for the player
        Character foe1 = adult(20, 2), foe2 = adult(20, 2); tp(foe1, away); tp(foe2, away); tp(p, away);
        away.tournamentRegistrants.clear(); away.tournamentRegistrants.add(p); away.tournamentRegistrants.add(foe1); away.tournamentRegistrants.add(foe2);
        away.tournamentPrize = new Item(p, Item.WeaponType.Sword, 5); away.tournamentOrganizer = foe1;
        m = PcProbe.mark();
        away.runTournament();
        String tour = PcProbe.since(m);
        check("T1 the player is told how the tournament went, bout by bout", tour.contains("Your bouts:") && (tour.contains("YOU WON THE TOURNAMENT") || tour.contains("knocked out") || tour.contains("lost to")));
        away.heavenItem = new Item(Item.ItemType.Pill, Item.PillType.Cultivation, 3, 4); away.heavenItem.heavenSent = true;
        m = PcProbe.mark();
        p.skipping = 5; // on autopilot: it used to print nothing
        Treasure.claim(p, away);
        p.skipping = 0;
        check("T2 a claimed Heaven-Sent treasure is announced even on autopilot", PcProbe.since(m).contains("You claimed the Heaven-Sent treasure"));

        // ---- S: scripts
        PlayerScript sc = new PlayerScript();
        String[][] adds = {{"goto", "15", "15"}, {"buygear"}, {"repeat", "2"}, {"forage", "3"}, {"sell", "goods"}, {"end"}, {"make", "food"}, {"cmd", "learn", "1"}, {"craft", "weapon"}, {"wait", "2"}, {"meditate"}, {"hunt"}};
        StringBuilder errs = new StringBuilder();
        for (String[] ad : adds) { String e = sc.add(ad); if (e != null) errs.append(Arrays.toString(ad)).append(": ").append(e).append("; "); }
        check("S1 steps and any command can be added " + errs, errs.length() == 0 && sc.steps.size() == adds.length);
        check("S2 a command that would take over the script is refused", sc.add(new String[]{"script", "run"}) != null && sc.add(new String[]{"quit"}) != null && sc.add(new String[]{"notacommand"}) != null);
        check("S3 sell goods is a command step, a bare sell a native one", "cmd sell goods".equals(sc.steps.get(4).toString()) && "sell".equals(new PlayerScript() {{ add(new String[]{"sell"}); }}.steps.get(0).toString()));
        String saved = ScriptStore.save("probe test", sc);
        PlayerScript back = new PlayerScript();
        String loaded = ScriptStore.load("probe test", back);
        boolean same = back.steps.size() == sc.steps.size();
        for (int i = 0; same && i < sc.steps.size(); i++) same = sc.steps.get(i).toString().equals(back.steps.get(i).toString());
        check("S4 a script saves to a file and loads back the same (" + saved + ", " + loaded + ")", saved == null && loaded == null && same && Arrays.asList(ScriptStore.names()).contains("probe test"));
        check("S5 the saved script can be deleted", ScriptStore.delete("probe test") && !Arrays.asList(ScriptStore.names()).contains("probe test"));
        // a command step runs inside a live script
        p.pc().script = new PlayerScript();
        PlayerScript live = p.pc().script;
        live.add(new String[]{"meditate"}); live.add(new String[]{"wait", "3"}); live.add(new String[]{"consume"}); live.add(new String[]{"stay"});
        Technique mt = p.martialTechnique; mt.potential = 90; mt.lvl = 20;
        int t0 = GameDriver.time;
        m = PcProbe.mark();
        PcProbe.runScriptToEnd(60);
        check("S6 a script of command steps runs to its end, a wait taking its turns (" + (GameDriver.time - t0) + " turns)", GameDriver.time - t0 >= 5 && PcProbe.since(m).contains("The script ran to its end") && mt.lvl > 20);

        check("Z no exceptions", !PcProbe.out().contains("Exception"));
    }

    /** One chaser step toward a quest's person, the way Character.quest takes it. */
    static int chaserStep(Character c, Quest q) {
        try {
            java.lang.reflect.Method mth = Character.class.getDeclaredMethod("pathfinderFor", Quest.class);
            mth.setAccessible(true);
            return (Integer) mth.invoke(c, q);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
