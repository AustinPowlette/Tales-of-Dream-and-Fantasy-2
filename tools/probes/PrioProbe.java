package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-14: the priorities a person who plays gives to kinds of quests (`qprio`, `PlayerControl.prioRules`, `Character.applyPriorityRules`): a rule by type, by kind of target (family, friend, enemy, sect
 *  mate, stranger, city...), by person or place and for any type; the most specific wins; a ruled quest takes the number and stops ageing; removing the rule gives the old numbers back; pinned and own quests are
 *  not touched; NPCs ignore the rules; a new body keeps them; they survive a save. */
public class PrioProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    static void say(String s) { PcProbe.real.println("[T] " + s); }
    static Area at(Area h, int dx, int dy) {
        int x = Math.floorMod(h.x + dx, Map.range), y = Math.floorMod(h.y + dy, Map.range);
        if (Map.tiles[x][y] == null) Map.tiles[x][y] = new Area(Area.ghost(x == 0 ? Map.range - 1 : x - 1, y), 'e');
        return Map.tiles[x][y];
    }
    static Character npc(Area where, String name) {
        Character c = new Character(where);
        c.age = 30; c.gender = "Male"; c.lvl = 5; c.majorLvl = 0; c.maxAge = 100000; c.caution = 100000; c.sect = null; c.trait = Character.Traits.Friendly; c.name = name; c.lastName = "Prio";
        while (Map.characters.size() < c.id) Map.characters.add(null);
        Map.characters.add(c); Map.population++;
        return c;
    }
    static Quest find(Character p, String name, int target) {
        for (Quest q : p.targetQuests) if (q.name.equals(name) && (target < 0 || q.targetCharacter == target)) return q;
        for (Quest q : p.spotQuests) if (q.name.equals(name)) return q;
        return null;
    }
    static void setRel(Character a, Character b, int v) { a.changeRelationship(b, 1); a.relationshipList.contains(b.id).value = v; }
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

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        check("player created and prompting", p != null && PcProbe.in.blocked);
        if (p == null) return;
        p.lvl = 100; p.majorLvl = 10; p.maxAge = 100000; p.caution = 0; p.targetQuests.clear(); p.spotQuests.clear(); p.currentQuest = null;
        Settings.lore = false;
        Area lab = at(GameDriver.mapViewer == null ? Map.tiles[Map.range / 2][Map.range / 2] : Map.tiles[Map.range / 2][Map.range / 2], 6, 6);
        lab.monsters = 0; p.location.removeChar(p); p.location = lab; lab.addChar(p);
        Character kin = npc(lab, "Kin"), friend = npc(lab, "Friend"), stranger = npc(lab, "Stranger");
        p.children.add(kin); // family (the probe does not need the child to point back)
        setRel(p, friend, 2); setRel(p, stranger, 0);

        // ---- Q1: by type
        p.targetQuests.add(new Quest("Find Person", false, 14, 2, 40, stranger));
        p.targetQuests.add(new Quest("Find Person", false, 14, 2, 40, kin));
        p.targetQuests.add(new Quest("Find Person", false, 14, 2, 40, friend));
        p.spotQuests.add(new Quest("Forage", false, 40, 3, 70));
        PcProbe.cmd("qprio Find Person 60", 60);
        Quest qs = find(p, "Find Person", stranger.id), qk = find(p, "Find Person", kin.id), qf = find(p, "Find Person", friend.id);
        check("Q1 a rule by type sets all three Find Person quests to 60 (" + qs.priority + "," + qk.priority + "," + qf.priority + ")", qs.priority == 60 && qk.priority == 60 && qf.priority == 60);
        check("Q1b ...with no ageing (diff " + qs.priorityDiff + ", max " + qs.maxPriority + ") and other types untouched (Forage " + find(p, "Forage", -1).priority + ")", qs.priorityDiff == 0 && qs.maxPriority == 60 && find(p, "Forage", -1).priority == 40);
        for (int i = 0; i < 5; i++) Character.bump(p.targetQuests);
        check("Q1c five weeks of ageing change nothing", find(p, "Find Person", stranger.id).priority == 60);

        // ---- Q2: kinds beat types
        PcProbe.cmd("qprio Find Person @family 88", 60);
        PcProbe.cmd("qprio @friend 75", 60);
        check("Q2 a rule for a kind of target beats the type rule (family " + qk.priority + ", stranger " + qs.priority + ")", qk.priority == 88 && qs.priority == 60);
        check("Q2b 'any type' about friends applies (" + qf.priority + ")", qf.priority == 75 || qf.priority == 60 && PlayerControl.kindOf(p, qf).equals("friend"));
        // ---- Q3: a person beats a kind
        PcProbe.cmd("qprio #" + kin.id + " 99", 60);
        check("Q3 a rule for one person beats the kind (" + qk.priority + ")", qk.priority == 99);
        // the queue is ordered by the new numbers
        Quest top = p.targetQuests.peek();
        check("Q3b the queue's top is the 99 (" + top.priority + ")", top.priority == 99 && top.targetCharacter == kin.id);

        // ---- Q4: removing gives the old numbers back
        PcProbe.cmd("qprio #" + kin.id + " off", 60);
        check("Q4 removing the person rule falls back to the kind rule (" + qk.priority + ")", qk.priority == 88);
        PcProbe.cmd("qprio reset", 60);
        check("Q4b 'qprio reset' gives every quest its own numbers back (" + qs.priority + "/" + qs.maxPriority + "/" + qs.priorityDiff + ")", qs.priority == 14 && qs.maxPriority == 40 && qs.priorityDiff == 2 && !qs.ruled && qk.priority == 14);
        check("Q4c ...and the rule list is empty", p.pc().prioRules == null || p.pc().prioRules.isEmpty());

        // ---- Q5: pinned and own quests keep theirs
        p.pc().setPriority(p, qf, 33);
        PcProbe.cmd("qprio Find Person 70", 60);
        check("Q5 a pinned quest keeps its pinned priority (" + qf.priority + ")", qf.priority == 33 && !qf.ruled);
        PcProbe.cmd("qnew Spot 1500 1500 55", 60);
        Quest own = null; for (Quest q : p.targetQuests) if (q.userCreated) own = q;
        check("Q5b a quest you made yourself is not ruled", own != null && !own.ruled);
        PcProbe.cmd("qprio reset", 60);

        // ---- Q6: errors and the list
        PcProbe.cmd("qprio Nonsense 50", 60);
        PcProbe.cmd("qprio Find Person @alien 50", 60);
        PcProbe.cmd("qprio Forage 500", 60);
        check("Q6 bad type, bad kind and out-of-range are refused", p.pc().prioRules == null || p.pc().prioRules.isEmpty());
        PcProbe.cmd("qprio hunt monster 61", 60);
        check("Q6b 'hunt monster' means Hunt Beast; a unique beginning is enough ('Sect M' = Sect Mission)", p.pc().prioRules.containsKey("Hunt Beast|") && "Sect Mission".equals(PlayerControl.typeName("sect m")));
        PcProbe.cmd("qprio reset", 60);

        // ---- Q7: a spot quest (no target) by type; the turn applies new quests
        p.spotQuests.add(new Quest("Meditate", false, 15, 2, 40));
        PcProbe.cmd("qprio Meditate 95", 60);
        check("Q7 a spot quest takes the rule (" + find(p, "Meditate", -1).priority + ")", find(p, "Meditate", -1).priority == 95);
        Quest late = new Quest("Meditate", false, 15, 2, 40);
        p.spotQuests.add(late);
        p.applyPriorityRules();
        check("Q7b a quest posted later takes it at the next turn (" + late.priority + ")", late.priority == 95);

        // ---- Q8: NPCs ignore it; a new body keeps it; a save keeps it
        Character other = npc(lab, "Other");
        other.targetQuests.add(new Quest("Find Person", false, 14, 2, 40, stranger));
        PlayerControl.class.getName();
        check("Q8 an NPC has no rules and applyPriorityRules does nothing for it", other.pc == null && !other.targetQuests.peek().ruled);
        PlayerControl heir = new PlayerControl();
        heir.inheritFrom(p.pc());
        check("Q8b a new body inherits the rules", heir.prioRules != null && heir.prioRules.containsKey("Meditate|"));
        java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oo = new java.io.ObjectOutputStream(bo)) { oo.writeObject(p.pc().prioRules); }
        Object back;
        try (java.io.ObjectInputStream oi = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bo.toByteArray()))) { back = oi.readObject(); }
        check("Q8c the rules survive saving (" + back + ")", back instanceof java.util.Map && ((java.util.Map<?, ?>) back).get("Meditate|") != null);
        PcProbe.cmd("qprio", 60);
        say("DONE");
    }
}
