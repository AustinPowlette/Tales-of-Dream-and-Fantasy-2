package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-16, change set D (beta tester's list): the picture of a fight (`FightReplay`). A fight is settled by one roll; its picture must agree with the result: the winner wins, the loser ends where the real result puts
 *  them (dead, spared, injured, fled), health only goes down, a favourite wins with more health left than an underdog, the elements are named where they mattered, and the same fight always looks the same. */
public class FightReplayProbe {
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

    static Character foe(Area where, int level, int element) {
        Character c = new Character(where);
        c.age = 30; c.gender = "Male"; c.maxAge = 100000; c.born = true; c.trait = Character.Traits.Friendly; c.targetQuests.clear(); c.lvl = level; c.majorLvl = level / 10; c.caution = 0;
        c.affiliation = "Good"; c.affiliationNum = 1; c.name = "Foe"; c.lastName = "N" + c.id;
        Elements.assign(c, element, new Random(c.id));
        Map.characters.add(c); Map.population++;
        return c;
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        check("player created and prompting", p != null && PcProbe.in.blocked);
        if (p == null) return;
        Settings.lore = false;
        p.lvl = 100; p.majorLvl = 5; p.maxAge = 100000; p.caution = 0; p.targetQuests.clear(); p.spotQuests.clear(); p.currentQuest = null;
        p.pc().breakthroughPolicy = PlayerControl.BT_AUTO;
        Area lab = at(6, 6); lab.monsters = 0;
        p.location.removeChar(p); p.location = lab; lab.addChar(p);

        // ---- R1: bouts between random people
        List<Character> people = new ArrayList<>();
        for (Character c : Map.characters) if (c != null && !c.dead && c.name != null && c.lastName != null) people.add(c);
        Random rnd = new Random(5);
        int bad = 0, n = 3000; StringBuilder why = new StringBuilder();
        int favWins = 0, favTotal = 0; double favEnd = 0, dogEnd = 0; int favCount = 0, dogCount = 0;
        for (int i = 0; i < n; i++) {
            Character a = people.get(rnd.nextInt(people.size())), b = people.get(rnd.nextInt(people.size()));
            if (a == b) continue;
            int wc = 5 + rnd.nextInt(91);
            boolean aw = rnd.nextInt(100) < wc;
            Character w = aw ? a : b;
            FightReplay f;
            try { f = FightReplay.bout(a, b, wc, w, "Round 1", "Somewhere"); } catch (RuntimeException e) { bad++; why.append("threw ").append(e).append("; "); continue; }
            if (f.beats.size() < 4) { bad++; why.append("short; "); continue; }
            int prevA = 100, prevB = 100; boolean mono = true;
            for (FightReplay.Beat bt : f.beats) { if (bt.hpA > prevA || bt.hpB > prevB || bt.hpA < 0 || bt.hpB < 0 || bt.hpA > 100 || bt.hpB > 100) mono = false; prevA = bt.hpA; prevB = bt.hpB; }
            int winnerEnd = aw ? f.endA() : f.endB(), loserEnd = aw ? f.endB() : f.endA();
            if (!mono) { bad++; why.append("health rose; "); }
            else if (winnerEnd <= loserEnd) { bad++; why.append("the winner ended lower; "); }
            else if (loserEnd > 14) { bad++; why.append("the loser ended high (" + loserEnd + "); "); }
            double pw = aw ? wc / 100.0 : 1 - wc / 100.0;
            if (pw >= 0.7) { favEnd += winnerEnd; favCount++; } else if (pw <= 0.3) { dogEnd += winnerEnd; dogCount++; }
        }
        check("R1 " + n + " random bouts: the winner wins, the loser ends low, health only falls (" + bad + " bad: " + (why.length() > 120 ? why.substring(0, 120) : why) + ")", bad == 0);
        double favAvg = favEnd / Math.max(1, favCount), dogAvg = dogEnd / Math.max(1, dogCount);
        check("R1b a favourite who wins ends with more health (" + String.format("%.0f", favAvg) + ") than an underdog who wins (" + String.format("%.0f", dogAvg) + ")", favAvg > dogAvg + 15);

        // ---- R2: determinism and the words
        Character x = foe(lab, 60, Elements.FIRE), y = foe(lab, 60, Elements.METAL);
        FightReplay f1 = FightReplay.bout(x, y, 66, x, "Round 1", "Lab"), f2 = FightReplay.bout(x, y, 66, x, "Round 1", "Lab");
        boolean same = f1.beats.size() == f2.beats.size();
        for (int i = 0; same && i < f1.beats.size(); i++) same = f1.beats.get(i).text.equals(f2.beats.get(i).text) && f1.beats.get(i).hpA == f2.beats.get(i).hpA;
        check("R2 the same fight always looks the same", same);
        String all = ""; for (FightReplay.Beat bt : f1.beats) all += bt.text + " ";
        check("R2b the elements are named when they change the odds (Fire against Metal: " + f1.swing + "): " + (all.length() > 140 ? all.substring(0, 140) : all), f1.swing != 0 && all.contains("The elements: Fire against Metal (Fire melts Metal)") && all.contains(x.name));
        check("R2c the first beat gives the odds and the levels", f1.beats.get(0).text.contains("Power") && f1.beats.get(0).text.contains("66%"));
        check("R2d a technique is called out with its name or the path's (" + f1.techA + ")", all.contains(f1.techA) || all.contains(f1.techB));

        // ---- R3: real fights of the person who plays
        int before = FightReport.replays.size();
        Character f = foe(lab, 100, Elements.of(p));
        f.changeRelationship(p, 3); p.changeRelationship(f, 3);
        int ok = 0, mismatch = 0, fights = 200, fled = 0;
        for (int i = 0; i < fights; i++) {
            p.dead = false; f.dead = false; p.lostAt = -1; f.lostAt = -1; p.lostToId = -1; f.lostToId = -1;
            FightReplay was = FightReport.replays.isEmpty() ? null : FightReport.replays.get(FightReport.replays.size() - 1);
            p.battle(f);
            if (FightReport.replays.isEmpty() || FightReport.replays.get(FightReport.replays.size() - 1) == was) { fled++; continue; }
            FightReplay fr = FightReport.replays.get(FightReport.replays.size() - 1);
            boolean pWon = f.lostAt == GameDriver.time && f.lostToId == p.id, fWon = p.lostAt == GameDriver.time && p.lostToId == f.id;
            boolean agrees = (pWon && fr.aWon) || (fWon && fr.bWon) || (!pWon && !fWon);
            if (agrees) ok++; else mismatch++;
        }
        check("R3 " + (ok + mismatch) + " real fights left a picture; the picture's winner is the real winner (" + ok + " agree, " + mismatch + " do not, " + fled + " did not come to blows)", mismatch == 0 && ok > 100);
        check("R3b the pictures are kept to " + FightReport.MAX_REPLAYS + " (" + FightReport.replays.size() + ")", FightReport.replays.size() <= FightReport.MAX_REPLAYS && FightReport.replays.size() > 0);
        FightReplay lastPic = FightReport.replays.get(FightReport.replays.size() - 1);
        check("R3c the picture carries the written report and a result line", lastPic.reportText != null && lastPic.reportText.contains("FIGHT REPORT") && !lastPic.resultLine.isEmpty());

        // ---- R4: a deadly fight and a flight
        Character enemy = foe(lab, 140, Elements.WOOD);
        enemy.changeRelationship(p, -4); p.changeRelationship(enemy, -4);
        int killed = 0, fights2 = 120;
        for (int i = 0; i < fights2 && killed < 5; i++) {
            p.dead = false; enemy.dead = false;
            FightReplay was = FightReport.replays.isEmpty() ? null : FightReport.replays.get(FightReport.replays.size() - 1);
            DecisionBroker.scripted = new ArrayDeque<>(); for (int k = 0; k < 6; k++) DecisionBroker.scripted.add(0);
            p.battle(enemy);
            if (!FightReport.replays.isEmpty() && FightReport.replays.get(FightReport.replays.size() - 1) != was) { FightReplay fr = FightReport.replays.get(FightReport.replays.size() - 1); if (fr.killed) { killed++; if ((fr.aWon ? fr.endB() : fr.endA()) != 0) { killed = -1000; } } }
            p.dead = false; enemy.dead = false;
        }
        DecisionBroker.scripted = new ArrayDeque<>();
        check("R4 when someone is killed the picture ends with them at zero health (" + killed + " deadly fights seen)", killed >= 1);

        // ---- R4b: every fight goes through the window, an easy beast too; the picture knows what it fought
        Character cub = foe(lab, 5, Elements.WOOD); cub.trait = Character.Traits.Beast; cub.masterId = -1; cub.affiliation = "Neutral";
        FightReplay wasB = FightReport.replays.isEmpty() ? null : FightReport.replays.get(FightReport.replays.size() - 1);
        p.dead = false; p.lvl = 140; p.battle(cub);
        FightReplay pb = FightReport.replays.isEmpty() ? null : FightReport.replays.get(FightReport.replays.size() - 1);
        check("R4b an easy fight against a beast (a one-line report) still leaves a picture of a stick animal (kind " + (pb == null ? -1 : pb.kindB) + ")", pb != null && pb != wasB && pb.kindB == 1 && pb.kindA == 0 && pb.area == lab);
        Character fiend = foe(lab, 5, Elements.FIRE); fiend.trait = Character.Traits.Monster; fiend.masterId = -1; fiend.affiliation = "Evil";
        FightReplay wasM = FightReport.replays.get(FightReport.replays.size() - 1);
        p.dead = false; p.battle(fiend);
        FightReplay pm = FightReport.replays.get(FightReport.replays.size() - 1);
        check("R4c ...and a Monster is drawn as a bigger one (kind " + pm.kindB + ")", pm != wasM && pm.kindB == 2);

        // ---- R6: betting on a spectated tournament
        check("R6 the odds: 50% pays x1.90, 10% pays x9.50, 1% is capped at x15, 99% pays at least x1.05 (" + Tournaments.oddsFor(50) + ", " + Tournaments.oddsFor(10) + ", " + Tournaments.oddsFor(1) + ", " + Tournaments.oddsFor(99) + ")",
                Math.abs(Tournaments.oddsFor(50) - 1.9) < 1e-9 && Math.abs(Tournaments.oddsFor(10) - 9.5) < 1e-9 && Tournaments.oddsFor(1) == 15.0 && Tournaments.oddsFor(99) == 1.05);
        Area arena = at(9, 9); arena.city = false;
        p.location.removeChar(p); p.location = arena; arena.addChar(p);
        p.dead = false; p.ss = 100000; p.sc = 0; p.dc = 0;
        List<Character> field = new ArrayList<>();
        for (int i = 0; i < 4; i++) { Character e = foe(arena, 60 + 5 * i, i % 5); arena.tournamentRegistrants.add(e); field.add(e); }
        arena.tournamentPrize = null; arena.tournamentOrganizer = null; arena.tournamentName = "the probe games";
        long wealth0 = PlayerLists.wealth(p);
        check("R6a nobody who has not taken a seat (the Spectate Tournament quest) counts as a spectator", Tournaments.spectators(arena, field).isEmpty());
        Tournaments.takeSeat(p, arena);
        check("R6a2 ...and a person who took a seat does", Tournaments.spectators(arena, field).contains(p));
        DecisionBroker.scripted = new ArrayDeque<>(); for (int i = 0; i < 3; i++) { DecisionBroker.scripted.add(0); DecisionBroker.scripted.add(1); } // a medium bet on the first fighter of every bout
        int mk = PcProbe.mark();
        arena.runTournament();
        String said = PcProbe.since(mk);
        long wealth1 = PlayerLists.wealth(p);
        DecisionBroker.scripted = new ArrayDeque<>();
        check("R6b a person who only stands in the tile is told how the whole tournament went (3 bouts shown)", said.contains("You watched the tournament") && said.contains("Bout 1") && said.contains("Bout 3"));
        java.util.regex.Matcher mt = java.util.regex.Pattern.compile("Your bets: ([+-]?\\d+) ss").matcher(said);
        long net = mt.find() ? Long.parseLong(mt.group(1).replace("+", "")) : Long.MIN_VALUE;
        check("R6c the bets are settled: the wealth changed by what the report says (" + (wealth1 - wealth0) + " against " + net + ")", net != Long.MIN_VALUE && wealth1 - wealth0 == net);
        check("R6d each bout was bet on once at a tenth of the wealth (the report names three bets)", said.split("You bet ", -1).length - 1 == 3);

        // ---- R5: the setting
        PcProbe.cmd("settings fightview on", 30);
        check("R5 'settings fightview on' turns the live window on", Settings.fightView);
        PcProbe.cmd("settings fightview off", 30);
        check("R5b ...and off", !Settings.fightView);
        // ---- R7: the natural way to a seat: a tournament is open in the tile, the game offers the Spectate Tournament quest, the person does it (qdo)
        arena.tournamentRegistrants.clear(); arena.tournamentTimer = 8; arena.tournamentMinMajor = 0; arena.tournamentMaxMajor = 10; arena.tournamentPrize = null;
        Tournaments.clearSeats(arena);
        // (the staged deadly fights above may have queued the person's removal from the world: if the game has already dropped him it carries on as a spectator world and 'wait 1' never gets an answer; put him back)
        Map.remCharacters.remove(p); Map.tempCharacters.remove(p);
        if (p.id >= 0 && p.id < Map.characters.size() && Map.characters.get(p.id) != p) Map.characters.set(p.id, p);
        p.dead = false; p.sect = null; p.targetQuests.clear(); p.spotQuests.clear(); p.currentQuest = null;
        Quest offered = null;
        for (int i = 0; i < 6 && offered == null; i++) {
            PcProbe.cmd("wait 1", 60);
            for (Quest q : p.spotQuests) if ("Spectate Tournament".equals(q.name)) offered = q;
        }
        check("R7 a person who plays is offered the Spectate Tournament quest while a tournament is open in the tile", offered != null);
        if (offered != null) {
            arena.tournamentTimer = 8; p.skipping = 0; // (the quest is done the way `qdo` does it, in the week the tournament is still open)
            String whyNot = p.doQuestNow(offered);
            check("R7b doing it (what 'qdo' runs) takes the seat (" + whyNot + ")", whyNot == null && Tournaments.spectators(arena, new ArrayList<Character>()).contains(p));
        }
        arena.tournamentTimer = 0; Tournaments.clearSeats(arena);

        Settings.fightView = true;
        System.out.println();
        PcProbe.real.println("[T] DONE");
    }
}
