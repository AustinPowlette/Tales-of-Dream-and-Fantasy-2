package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-10 (fourth batch): the Erased. The Hollow Court puts out one seeker of the Palace in a century: the year before, a warning (the lanterns dim, a notice for the humans who knew them);
 *  the year after, they are gone with no death, no notice and no name in the chronicle, and only the humans who knew them remember (Journal text, scene). Never twice in a century. */
public class ErasedProbe {
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

    static Character seeker(String first, String last) {
        Character c = new Character(home());
        c.name = first; c.lastName = last; c.age = 30; c.gender = "Male"; c.lvl = 30; c.majorLvl = 3; c.maxAge = 100000;
        if (c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) c.trait = Character.Traits.Friendly;
        c.id = Map.characters.size();
        GameDriver.UID = Math.max(GameDriver.UID, c.id + 1);
        Map.characters.add(c); Map.population++;
        Lore.state.seekers.put(c.id, 0);
        return c;
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        GameDriver.frozenNpcs = true;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        Map.currentCharacter = p;
        check("H0 the lore is on and the Palace stands", Lore.state != null && Lore.palace() != null);
        Lore.State s = Lore.state;
        if (s.seekers == null) s.seekers = new HashMap<>();
        if (!s.humans.containsKey(p.id)) s.humans.put(p.id, new Lore.PState());
        Lore.PState ps = s.humans.get(p.id);

        Character friend = seeker("Quillon", "Ashgrove"), stranger = seeker("Marrek", "Dunmoor");
        p.changeRelationship(friend, 3);
        Chronicle.add("world", "Quillon Ashgrove won a small tournament at Originis.", friend.id);
        Chronicle.add("world", "Marrek Dunmoor founded a shop.", stranger.id);
        int logBefore = Chronicle.log.size();

        // ---- the schedule
        s.erasedNext = 0; s.erased = null; s.erasedWarn = -1;
        Lore.erasedYearly(10);
        check("H1 nothing happens before year 30", s.erasedWarn < 0 && s.erasedNext == 0);
        Lore.erasedYearly(40);
        check("H2 the first one is scheduled between year 50 and 89 (" + s.erasedNext + ")", s.erasedNext >= 50 && s.erasedNext < 90 && s.erasedWarn < 0);
        s.erasedNext = 60;
        Lore.erasedYearly(60);
        check("H3 in its year the warning is given for a seeker a human knows (warn=" + s.erasedWarn + ", friend=" + friend.id + ")", s.erasedWarn == friend.id);
        check("H4 the seeker is still here after the warning", !friend.dead && s.seekers.containsKey(friend.id));
        boolean warned = false;
        for (Notifications.Note n : new ArrayList<>(Notifications.history)) if (n.text.contains("dimmed tonight") && n.text.contains("Quillon")) warned = true;
        check("H5 the human who knows them is told the lanterns dimmed", warned);

        // ---- the year after
        Lore.erasedYearly(61);
        check("H6 the year after, they are gone (dead=" + friend.dead + ")", friend.dead && !s.seekers.containsKey(friend.id));
        check("H7 the stranger was left alone", !stranger.dead && s.seekers.containsKey(stranger.id));
        boolean nameLeft = false;
        for (Chronicle.Entry e : new ArrayList<>(Chronicle.log)) if (e.text.contains("Quillon Ashgrove")) nameLeft = true;
        check("H8 their name is gone from the chronicle (and no death line was written)", !nameLeft);
        check("H9 the other entries stay", Chronicle.log.stream().anyMatch(e -> e.text.contains("Marrek Dunmoor")));
        check("H10 the human alone remembers: Journal text and the scene (" + (ps.remembered == null ? "-" : ps.remembered) + ")", ps.remembered != null && ps.remembered.size() == 1 && ps.remembered.get(0).contains("Quillon Ashgrove") && Lore.cluesText(ps).contains("Remembered by you alone") && ps.queue.contains("erased"));
        List<String> lines = LoreText.lines("erased", Lore.vars(p, ps));
        check("H11 the scene names them and leaves no placeholder", lines.stream().anyMatch(l -> l.contains("Quillon Ashgrove")) && lines.stream().noneMatch(l -> l.contains("{")));
        check("H12 the list of everyone put out has them (" + s.erased + ")", s.erased != null && s.erased.size() == 1 && s.erased.get(0).startsWith("Quillon Ashgrove|"));
        check("H13 the next is a century away (year " + s.erasedNext + ")", s.erasedNext >= 61 + 95 && s.erasedNext <= 61 + 105);

        // ---- one in a century
        int before = s.erased.size();
        for (int y = 62; y < 61 + 95; y++) { seeker("Fill" + y, "Seeker"); Lore.erasedYearly(y); }
        check("H14 none in the next 94 years (" + (s.erased.size() - before) + ")", s.erased.size() == before && s.erasedWarn < 0);
        for (int y = 61 + 95; y <= 61 + 110; y++) Lore.erasedYearly(y);
        check("H15 one more by year " + (61 + 110) + " (" + (s.erased.size() - before) + ")", s.erased.size() == before + 1);
        for (int y = 61 + 111; y <= 61 + 190; y++) { seeker("More" + y, "Seeker"); Lore.erasedYearly(y); }
        check("H16 still just one per century over the next 80 years (" + (s.erased.size() - before) + ")", s.erased.size() == before + 1);

        // ---- edge cases
        s.erasedWarn = -1; s.erasedNext = 1000;
        Character gone = seeker("Soon", "Gone");
        s.erasedNext = 400; s.erasedWarn = gone.id; gone.dead = true;
        Lore.erasedYearly(400);
        check("H17 a seeker who died before their year is not put out; another is chosen next year", s.erasedWarn < 0 && s.erasedNext == 401);
        s.erased = null; s.erasedWarn = -1; s.erasedNext = 0;
        Lore.erasedYearly(500);
        check("H18 a save from before the Erased (no list) runs fine", s.erased != null);
    }
}
