package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-13: Seek: kill (the quest "Hunt Down"). `seek <id> kill` pins it for anyone (a child and a monster included, not another player or the foe of another person's story); when you stand with the
 *  target `qdo` asks Kill / Spar / Spare / Not now; Kill is a fight the hunter has chosen (no second question, a child is not spared); Spar and Not now keep the hunt; Spare ends it;
 *  a hunt's chase has no give-up after 150 turns, a plain chase has. */
public class HuntDownProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    static void say(String s) { PcProbe.real.println("[T] " + s); }
    static Area at(Area h, int dx, int dy) {
        int x = Math.floorMod(h.x + dx, Map.range), y = Math.floorMod(h.y + dy, Map.range);
        if (Map.tiles[x][y] == null) Map.tiles[x][y] = new Area(Area.ghost(x == 0 ? Map.range - 1 : x - 1, y), 'e');
        return Map.tiles[x][y];
    }
    static void tp(Character c, Area to) { c.location.removeChar(c); c.location = to; to.addChar(c); }
    static Character npc(Area where, String name, int age, int lvl) {
        Character c = new Character(where);
        c.age = age; c.gender = "Male"; c.lvl = lvl; c.majorLvl = lvl / 10; c.maxAge = 100000; c.caution = 100000; c.sect = null; c.trait = Character.Traits.Friendly; c.name = name; c.lastName = "Quarry";
        while (Map.characters.size() < c.id) Map.characters.add(null);
        Map.characters.add(c); Map.population++;
        return c;
    }
    static Quest quest(Character p, Character t) { for (Quest q : p.targetQuests) if (q.name.equals("Hunt Down") && q.targetCharacter == t.id) return q; return null; }
    static void qdo(Character p, Quest q) throws Exception {
        PcProbe.cmd("q", 60);
        int i = p.pc().view.indexOf(q);
        if (i < 0) { PcProbe.fails++; say("FAIL the quest is not in the list"); return; }
        PcProbe.cmd("qdo " + (i + 1), 120);
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
        p.lvl = 100; p.majorLvl = 10; p.maxAge = 100000; p.caution = 0; p.targetQuests.clear(); p.spotQuests.clear(); p.currentQuest = null;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        Settings.lore = false;
        GameDriver.time = Math.max(GameDriver.time, 100);
        Area lab = at(GameDriver.mapViewer == null ? Map.tiles[Map.range / 2][Map.range / 2] : Map.tiles[Map.range / 2][Map.range / 2], 6, 6);
        lab.monsters = 0; tp(p, lab);

        // ---- H1: the quest is made once, for a child as for anyone
        Character kid = npc(lab, "Kid", 12, 5);
        PcProbe.cmd("seek " + kid.id + " kill", 60);
        Quest qk = quest(p, kid);
        PcProbe.check("H1 'seek <id> kill' pins a Hunt Down quest for a child of twelve", qk != null && qk.pinned && qk.userCreated);
        PcProbe.cmd("seek " + kid.id + " kill", 60);
        int n = 0; for (Quest q : p.targetQuests) if (q.name.equals("Hunt Down") && q.targetCharacter == kid.id) n++;
        PcProbe.check("H1b asking again makes no second copy", n == 1);
        Character other = npc(lab, "Hero", 30, 30); other.fatedTo = 77; // the foe of another person's story
        PcProbe.cmd("seek " + other.id + " kill", 60);
        PcProbe.check("H1c the foe of another person's story cannot be hunted", quest(p, other) == null);
        Character child2 = npc(lab, "Safe", 12, 5);
        child2.dead(p);
        PcProbe.check("H1d without the hunt a child is still protected (dead(killer) does nothing)", !child2.dead);

        // ---- H2: Not now keeps the hunt, Spar keeps it, Spare ends it
        DecisionBroker.scripted.clear(); DecisionBroker.scripted.add(3);
        qdo(p, qk);
        PcProbe.check("H2 'Not now': the child lives and the hunt goes on", !kid.dead && quest(p, kid) != null);
        DecisionBroker.scripted.clear(); DecisionBroker.scripted.add(1);
        qdo(p, qk);
        PcProbe.check("H2b 'Spar': nobody dies and the hunt goes on", !kid.dead && quest(p, kid) != null);
        // ---- H3: Kill takes a child too
        DecisionBroker.scripted.clear(); DecisionBroker.scripted.add(0);
        int prompts0 = DecisionBroker.prompts;
        qdo(p, qk);
        PcProbe.check("H3 'Kill': the child is dead (the hunter chose it)", kid.dead);
        PcProbe.check("H3b ...with only the one question (no 'kill or spare?' after the fight: " + (DecisionBroker.prompts - prompts0) + " prompt)", DecisionBroker.prompts - prompts0 == 1);
        PcProbe.check("H3c ...and the quest is over", quest(p, kid) == null);

        // ---- H4: Spare ends it
        Character adult = npc(lab, "Adult", 40, 20);
        PcProbe.cmd("seek " + adult.id + " kill", 60);
        Quest qa = quest(p, adult);
        DecisionBroker.scripted.clear(); DecisionBroker.scripted.add(2);
        qdo(p, qa);
        PcProbe.check("H4 'Spare': the adult lives and the quest is over", !adult.dead && quest(p, adult) == null);

        // ---- H5: a monster
        Character mon = npc(lab, "Grendel", 60, 20); mon.trait = Character.Traits.Monster;
        PcProbe.cmd("seek " + mon.id + " kill", 60);
        Quest qm = quest(p, mon);
        PcProbe.check("H5 a monster can be hunted", qm != null);
        DecisionBroker.scripted.clear(); DecisionBroker.scripted.add(0);
        qdo(p, qm);
        PcProbe.check("H5b ...and is killed", mon.dead);

        // ---- H6: the chase of a hunt has no give-up
        Character far = npc(at(lab, 5, 5), "Runner", 30, 20);
        PcProbe.cmd("seek " + far.id + " kill", 60);
        Quest qf = quest(p, far);
        PcProbe.cmd("q", 60);
        int idx = p.pc().view.indexOf(qf);
        PcProbe.cmd("goquest " + (idx + 1), 120);
        PcProbe.check("H6 'goquest' on a Hunt Down starts a chase with no give-up (chaseHunt " + p.chaseHunt + ")", p.chaseHunt || p.location == far.location);
        if (p.chaseId >= 0) { p.chaseTurns = 149; PlayerCommands.stepAuto(p); PcProbe.check("H6b the 150-turn limit does not count for a hunt (turns " + p.chaseTurns + ")", p.chaseTurns == 149 && p.chaseId >= 0); }
        PcProbe.cmd("stay", 60);
        tp(p, lab);
        PcProbe.cmd("chase #" + far.id, 120);
        PcProbe.check("H6c a plain 'chase' keeps its limit (chaseHunt " + p.chaseHunt + ")", !p.chaseHunt);
        // ---- H7: a plain 'attack' ignores the scruples of alignment (a Good hunter strikes a Neutral stranger) but not the friend rule
        PcProbe.cmd("stay", 60); p.chaseId = -1; p.autoX = -1; tp(p, lab);
        p.affiliation = "Good"; p.targetQuests.clear();
        Character stranger = npc(lab, "Stranger", 30, 20); stranger.affiliation = "Neutral";
        DecisionBroker.scripted.clear(); DecisionBroker.scripted.add(1); DecisionBroker.scripted.add(1);
        PcProbe.cmd("attack #" + stranger.id, 120);
        PcProbe.check("H7 'attack' fights a Neutral stranger although the attacker is Good (fought " + (p.foughtWith == stranger.id) + ")", p.foughtWith == stranger.id || stranger.dead);
        Character buddy = npc(lab, "Buddy", 30, 20); buddy.affiliation = "Neutral";
        p.changeRelationship(buddy, 1); p.relationshipList.contains(buddy.id).value = 2; // (a friend)
        p.foughtWith = -1; p.foughtAt = -1;
        DecisionBroker.scripted.clear();
        PcProbe.cmd("attack #" + buddy.id, 120);
        PcProbe.check("H7b ...but never a friend (the friend rule stays)", p.foughtWith != buddy.id && !buddy.dead);
        PcProbe.check("H7c the flag is off again after the command", !p.attackChosen);
        say("DONE");
    }
}
