package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-13: no duplicate quests. `seek` and `qnew` used to queue a copy on every click (ten Find Person quests from a beta tester). */
public class DupQuestProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    static Area home() { return Map.tiles[Map.range / 2][Map.range / 2]; }
    static Character adult(Area where) {
        Character c = new Character(where);
        c.age = 30; c.gender = "Male"; c.lvl = 30; c.majorLvl = 3; c.maxAge = 100000; c.caution = 0; c.sect = null; c.trait = Character.Traits.Friendly;
        while (Map.characters.size() < c.id) Map.characters.add(null);
        Map.characters.add(c); Map.population++;
        return c;
    }
    static int count(Character c, String name, int targetId) {
        int n = 0;
        ArrayList<Quest> all = new ArrayList<>(c.targetQuests); all.addAll(c.spotQuests); if (c.currentQuest != null) all.add(c.currentQuest);
        for (Quest q : all) if (q.name.equalsIgnoreCase(name) && (targetId < 0 || q.targetCharacter == targetId)) n++;
        return n;
    }

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
        p.lvl = 100; p.majorLvl = 10; p.maxAge = 100000; p.caution = 0;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        Settings.lore = false;
        p.targetQuests.clear(); p.spotQuests.clear(); p.currentQuest = null;
        Character w = adult(home()), v = adult(home());
        w.gender = "Male"; p.pc().muted.clear();

        // D1: ten identical seeks = one quest (each of the kinds)
        for (String kind : new String[]{"talk", "friend", "fight", "spar"}) {
            for (int i = 0; i < 10; i++) PcProbe.cmd("seek " + w.id + " " + kind);
        }
        PcProbe.check("D1 talk/friend/fight/spar x10 each = one quest of each kind (" + count(p, "Talk To", w.id) + "," + count(p, "Befriend", w.id) + "," + count(p, "Duel", w.id) + "," + count(p, "Spar", w.id) + ")",
                count(p, "Talk To", w.id) == 1 && count(p, "Befriend", w.id) == 1 && count(p, "Duel", w.id) == 1 && count(p, "Spar", w.id) == 1);
        // D2: another person is another quest
        PcProbe.cmd("seek " + v.id + " talk");
        PcProbe.check("D2 the same quest for another person is a separate quest", count(p, "Talk To", -1) == 2 && count(p, "Talk To", v.id) == 1);
        // D3: asking again with a higher priority raises the one quest; a lower one leaves it
        PcProbe.cmd("seek " + w.id + " talk 90");
        Quest talk = null; for (Quest q : p.targetQuests) if (q.name.equals("Talk To") && q.targetCharacter == w.id) talk = q;
        PcProbe.check("D3 a higher priority raises the existing quest to 90 (" + (talk == null ? "gone" : talk.priority) + ")", talk != null && talk.priority == 90 && count(p, "Talk To", w.id) == 1);
        PcProbe.cmd("seek " + w.id + " talk 10");
        PcProbe.check("D3b a lower priority does not lower it", talk != null && talk.priority == 90);
        // D4: waypoints: ten identical qnew = one; the queue is still a valid heap (the highest priority comes out first)
        Area h = home();
        for (int i = 0; i < 10; i++) PcProbe.cmd("qnew Home " + h.x + " " + h.y + " 50");
        PcProbe.check("D4 qnew Home x10 = one waypoint (" + count(p, "Go: Home", -1) + ")", count(p, "Go: Home", -1) == 1);
        PcProbe.cmd("qnew Home " + h.x + " " + h.y + " 99");
        Quest top = p.targetQuests.peek();
        PcProbe.check("D4b a repeated qnew with priority 99 raises it, and the queue's head is the highest priority (" + (top == null ? "none" : top.name + " " + top.priority) + ")", top != null && top.priority == 99 && count(p, "Go: Home", -1) == 1);
        // D5: a finished (removed) quest can be made again
        p.targetQuests.remove(talk);
        PcProbe.cmd("seek " + w.id + " talk 40");
        PcProbe.check("D5 after the quest is gone the same seek makes it again", count(p, "Talk To", w.id) == 1);
        // D6: old saves: copies already in the queue are cleaned once, the survivor keeps the best priority
        p.targetQuests.clear();
        for (int pr : new int[]{20, 70, 40}) { Quest q = new Quest("Find Person", true, pr, 0, pr, w); p.targetQuests.add(q); }
        p.pc().dedupedAtLoad = false;
        PcProbe.cmd("x");
        PcProbe.check("D6 three Find Person copies become one with priority 70 (" + count(p, "Find Person", w.id) + ")", count(p, "Find Person", w.id) == 1 && p.targetQuests.peek() != null && p.targetQuests.peek().priority >= 70);
        // D7: two characters are independent
        Character other = adult(home());
        Quest a1 = new Quest("Talk To", true, 50, 0, 50, w); Quest a2 = new Quest("Talk To", true, 50, 0, 50, w);
        other.addPlayerQuest(a1); p.addPlayerQuest(a2);
        PcProbe.check("D7 another character's queue does not block mine", other.targetQuests.contains(a1) && !other.questAddWasDuplicate && count(other, "Talk To", w.id) == 1);
        PcProbe.cmd("x");
        PcProbe.real.println("[T] DONE");
    }
}
