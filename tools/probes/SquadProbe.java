package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-13: squads of a person who plays. Members stay while the leader plays MANUAL even with errands waiting (before: 0 of 4 stayed a week), ride along tile by tile so that all stand in the
 *  leader's tile at every prompt, obey Hold / Follow / Fights first / Reserve, can be kicked (plain removal), and join `hunt` and `attack` unless the player goes alone
 *  (`alone` on the line, `policy squadfight`, the question). */
public class SquadProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    static void say(String s) { PcProbe.real.println("[T] " + s); }
    static Area home() { return Map.tiles[Map.range / 2][Map.range / 2]; }
    static Character adult(Area where, int lvl, String affiliation) {
        Character c = new Character(where);
        c.age = 30; c.gender = "Male"; c.lvl = lvl; c.majorLvl = Math.min(10, lvl / 10); c.maxAge = 100000; c.caution = 0; c.sect = null; c.trait = Character.Traits.Friendly; c.affiliation = affiliation;
        while (Map.characters.size() < c.id) Map.characters.add(null);
        Map.characters.add(c); Map.population++;
        return c;
    }
    static void tp(Character c, Area to) { c.location.removeChar(c); c.location = to; to.addChar(c); }
    static Area at(Area h, int dx, int dy) {
        int x = Math.floorMod(h.x + dx, Map.range), y = Math.floorMod(h.y + dy, Map.range);
        if (Map.tiles[x][y] == null) Map.tiles[x][y] = new Area(Area.ghost(x == 0 ? Map.range - 1 : x - 1, y), 'e');
        return Map.tiles[x][y];
    }
    static boolean allHere(Character p, List<Character> ms) { for (Character m : ms) if (m.location != p.location || m.dead) return false; return true; }

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
        PcProbe.check("player created and prompting", p != null && PcProbe.in.blocked);
        if (p == null) return;
        p.lvl = 100; p.majorLvl = 10; p.maxAge = 100000; p.caution = 0; p.targetQuests.clear(); p.spotQuests.clear(); p.currentQuest = null;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        p.pc().squadPolicy = 1; p.pc().friendPolicy = 1;
        Settings.lore = false;
        GameDriver.time = Math.max(GameDriver.time, 100);
        Area lab = at(home(), 5, 5); lab.monsters = 0; lab.city = false;
        tp(p, lab);
        Area far = at(home(), -6, 4); far.city = true;
        final List<Character> ms = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Character m = adult(lab, 45 + i, "Neutral");
            for (int k = 0; k < 6; k++) m.backpack.add(new Item(m, Item.WeaponType.Sword, 10));
            m.targetQuests.add(new Quest("Go Sell", true, 95, 1, 100, far)); // an errand that used to make every member leave within a week
            m.joinParty(p);
            ms.add(m);
        }
        check(p.squadMembers().size() == 4, "Q0 four members joined");
        GameDriver.frozenNpcs = false;

        // ---- Q1: they stay, with errands waiting
        boolean stayed = true; int offTile = 0;
        for (int i = 0; i < 10; i++) {
            PcProbe.cmd("x", 120);
            for (Character m : ms) if (m.partyLeaderId != p.id) stayed = false;
            if (!allHere(p, ms)) offTile++;
        }
        check(stayed, "Q1 all four are still in the squad after ten weeks with an errand each (before: none after a week)");
        check(offTile == 0, "Q1b and all four stood in the leader's tile at each of the ten prompts (off-tile prompts: " + offTile + ")");
        int withErrand = 0; for (Character m : ms) if (!m.targetQuests.isEmpty() || m.currentQuest != null) withErrand++;
        check(withErrand >= 1, "Q1c their errands are not all thrown away by the squad rule (" + withErrand + " of 4 still have one; the world's own quest upkeep may drop an unreachable one)");

        // ---- Q2: tile by tile
        int bad = 0;
        String[] steps = {"d", "d", "w", "w", "a", "s", "nw", "ne", "sw", "se"};
        for (String s : steps) { PcProbe.cmd(s, 120); if (!allHere(p, ms)) bad++; }
        check(bad == 0, "Q2 after each of ten steps (including diagonals) the squad stands in my tile (misses " + bad + ")");
        Area dest = at(p.location, 5, -2);
        int cmds = 0;
        while (p.location != dest && cmds++ < 20) PcProbe.cmd("goto " + dest.x + " " + dest.y, 120);
        check(p.location == dest && allHere(p, ms), "Q2b after a goto of several tiles the squad is with me on arrival");

        // ---- Q3: Hold
        Area start = p.location;
        PcProbe.cmd("squad", 60);
        PcProbe.cmd("squad hold 1", 60);
        check(ms.get(0).squadOrder == Character.ORDER_HOLD, "Q3 'squad hold 1' puts the first member on Hold");
        for (int i = 0; i < 3; i++) { PcProbe.cmd("d", 120); say("info: after step " + (i + 1) + ": me " + p.location.x + "," + p.location.y + ", the held member " + ms.get(0).location.x + "," + ms.get(0).location.y + " (start " + start.x + "," + start.y + "), order " + ms.get(0).squadOrder + ", leader " + ms.get(0).partyLeaderId + " (me " + p.id + ")"); }
        check(ms.get(0).location == start && ms.get(0).partyLeaderId == p.id, "Q3b the held member stays where it was and is still in the squad");
        check(ms.get(1).location == p.location && ms.get(2).location == p.location && ms.get(3).location == p.location, "Q3c the others came along");
        PcProbe.cmd("squad follow 1", 60);
        int waited = 0;
        while (ms.get(0).location != p.location && waited++ < 15) PcProbe.cmd("x", 120);
        check(ms.get(0).location == p.location, "Q3d on Follow it walks to me and joins me when I stand still (" + waited + " weeks)");

        // ---- Q4: kick
        PcProbe.cmd("squad kick 2", 60);
        check(ms.get(1).partyLeaderId < 0 && p.squadMembers().size() == 3 && !p.squadMembers().contains(ms.get(1)), "Q4 'squad kick 2' removes the second member and only that one");
        check(ms.get(1).squadOrder == Character.ORDER_FOLLOW && ms.get(1).squadRole == Character.ROLE_NORMAL, "Q4b a kicked member loses its orders");
        PcProbe.cmd("squad kick 9", 60);
        check(p.squadMembers().size() == 3, "Q4c a number that is nobody changes nothing");
        ms.remove(1);

        // ---- Q5: hunts
        Area wild = at(p.location, 2, 3); wild.monsters = 40; wild.lvl = 1; wild.highestLvl = 1;
        tp(p, wild); for (Character m : ms) tp(m, wild);
        GameDriver.frozenNpcs = true; // (the squad is placed by hand: nobody wanders off meanwhile)
        PcProbe.cmd("squad front 2", 60);
        check(ms.get(1).squadRole == Character.ROLE_FRONT, "Q5 'squad front 2' sets the role");
        int k0 = p.kills, f0 = ms.get(1).kills, t0 = GameDriver.time;
        PcProbe.cmd("hunt", 120);
        check(GameDriver.time == t0 + 1 && ms.get(1).kills == f0 + 1 && p.kills == k0, "Q5b 'hunt': the member told to fight first took the beast, I did not (member kills " + f0 + " -> " + ms.get(1).kills + ", mine " + k0 + " -> " + p.kills + ")");
        k0 = p.kills; f0 = ms.get(1).kills;
        PcProbe.cmd("hunt alone", 120);
        check(p.kills == k0 + 1 && ms.get(1).kills == f0, "Q5c 'hunt alone': I took the beast myself");
        PcProbe.cmd("policy squadfight alone", 60);
        k0 = p.kills; f0 = ms.get(1).kills;
        PcProbe.cmd("hunt", 120);
        check(p.kills == k0 + 1 && ms.get(1).kills == f0, "Q5d with 'policy squadfight alone' a plain 'hunt' goes alone");
        PcProbe.cmd("hunt squad", 120);
        check(ms.get(1).kills == f0 + 1, "Q5e ...and 'hunt squad' calls the squad for that one");
        PcProbe.cmd("squad fight ask", 60);
        DecisionBroker.scripted.clear(); DecisionBroker.scripted.add(1); // "Go alone"
        int pr0 = DecisionBroker.prompts; k0 = p.kills; f0 = ms.get(1).kills;
        PcProbe.cmd("hunt", 120);
        check(DecisionBroker.prompts == pr0 + 1 && p.kills == k0 + 1 && ms.get(1).kills == f0, "Q5f with 'ask' the question is put (Call the squad / Go alone) and 'Go alone' is respected");
        DecisionBroker.scripted.clear(); DecisionBroker.scripted.add(0); // "Call the squad"
        f0 = ms.get(1).kills;
        PcProbe.cmd("hunt", 120);
        check(ms.get(1).kills == f0 + 1, "Q5g ...and 'Call the squad' brings it");
        PcProbe.cmd("squad fight join", 60);
        PcProbe.cmd("squad hold all", 60);
        k0 = p.kills; f0 = ms.get(1).kills;
        PcProbe.cmd("hunt", 120);
        check(p.kills == k0 + 1 && ms.get(1).kills == f0, "Q5h members on Hold sit the hunt out");
        PcProbe.cmd("squad follow all", 60);

        // ---- Q6: attack
        Character foe = adult(wild, 12, "Evil"); foe.caution = 100000; // (a foe that never tries to flee: it flees when the fight is lost by a margin above its caution)
        PcProbe.cmd("squad front 2", 60);
        int tt = GameDriver.time;
        PcProbe.cmd("attack #" + foe.id, 120);
        check(ms.get(1).foughtWith == foe.id && ms.get(1).foughtAt == tt, "Q6 'attack': the member told to fight first fought the foe");
        boolean beatenByMember = foe.dead || (foe.lostAt == tt && foe.lostToId == ms.get(1).id);
        check(!beatenByMember || !(p.foughtAt == tt && p.foughtWith == foe.id), "Q6b ...and when it beat the foe I was not needed (the squad stands down once the foe is beaten; it " + (beatenByMember ? "did" : "fled, so I stepped in") + ")");
        Character foe2 = adult(wild, 12, "Evil"); foe2.caution = 100000;
        tt = GameDriver.time;
        PcProbe.cmd("attack #" + foe2.id + " alone", 120);
        check(p.foughtWith == foe2.id && p.foughtAt == tt && ms.get(1).foughtWith != foe2.id, "Q6c 'attack ... alone': I fought alone");
        Character friendly = adult(wild, 12, "Evil");
        ms.get(0).changeRelationship(friendly, 3); friendly.changeRelationship(ms.get(0), 3);
        PcProbe.cmd("squad reserve 1", 60);
        check(ms.get(0).squadRole == Character.ROLE_RESERVE, "Q6d 'squad reserve 1' sets the reserve role");
        tt = GameDriver.time;
        PcProbe.cmd("attack #" + friendly.id, 120);
        check(!(ms.get(0).foughtWith == friendly.id && ms.get(0).foughtAt == tt), "Q6e a member does not fight a friend of its own");

        // ---- Q8: Work: a member goes about its errands and stays in the squad; Follow calls it back
        PcProbe.cmd("squad follow all", 60);
        for (Character m : ms) tp(m, p.location);
        GameDriver.frozenNpcs = false;
        Character worker = ms.get(0);
        if (worker.targetQuests.isEmpty()) worker.targetQuests.add(new Quest("Go Sell", true, 95, 1, 100, far));
        PcProbe.cmd("squad work 1", 60);
        check(worker.squadOrder == Character.ORDER_WORK, "Q8 'squad work 1' sets the Work order");
        int away = 0;
        for (int i = 0; i < 12; i++) { PcProbe.cmd("x", 120); if (worker.location != p.location) away++; }
        check(away >= 6 && worker.partyLeaderId == p.id, "Q8b a member on Work walked off on its errand and is still in the squad (away " + away + " of 12 weeks)");
        check(ms.get(1).location == p.location && ms.get(2).location == p.location, "Q8c the others stayed with me");
        PcProbe.cmd("squad follow 1", 60);
        int back = 0;
        while (worker.location != p.location && back++ < 40) PcProbe.cmd("x", 120);
        check(worker.location == p.location, "Q8d Follow calls it back (" + back + " weeks)");
        GameDriver.frozenNpcs = true;
        for (Character m : ms) tp(m, p.location);

        // ---- Q9: the squad defends the leader
        PcProbe.cmd("squad normal all", 60); PcProbe.cmd("squad follow all", 60);
        PcProbe.cmd("squad front 2", 60); PcProbe.cmd("squad hold 3", 60);
        p.pc().duelPolicy = true;
        Character raider = adult(p.location, 12, "Evil"); raider.caution = 100000; raider.name = "Raider";
        int t9 = GameDriver.time;
        raider.battleCheck(p);
        check(ms.get(1).foughtWith == raider.id && ms.get(1).foughtAt == t9, "Q9 an NPC attacks me: the member told to fight first met it");
        check(!(ms.get(2).foughtWith == raider.id && ms.get(2).foughtAt == t9), "Q9b a member on Hold sat it out");
        boolean beaten9 = raider.dead || (raider.lostAt == t9 && raider.lostToId == ms.get(1).id);
        check(!beaten9 || !(p.foughtAt == t9 && p.foughtWith == raider.id), "Q9c when the member beat the raider I was not fought (" + (beaten9 ? "beaten" : "it fled, so it went on against me") + ")");
        PcProbe.cmd("squad hold all", 60);
        Character raider2 = adult(p.location, 12, "Evil"); raider2.caution = 100000;
        int t9b = GameDriver.time;
        raider2.battleCheck(p);
        check(p.foughtWith == raider2.id && p.foughtAt == t9b, "Q9d with every member on Hold nobody stepped in and the raider fought me");
        PcProbe.cmd("squad follow all", 60);

        // ---- Q7: the party list
        int m0 = PcProbe.mark();
        PcProbe.cmd("squad", 60);
        String list = PcProbe.since(m0);
        check(list.contains("Follow") && list.contains("Hold") && list.contains("fights first") && list.contains("reserve") && list.contains("squad kick"), "Q7 'squad' lists the members with their orders and says how to command them");
        say("DONE");
    }

    static void check(boolean ok, String name) { PcProbe.check(name, ok); }
}
