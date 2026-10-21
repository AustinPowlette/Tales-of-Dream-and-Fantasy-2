package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-14: the quests a person makes by hand (`qadd`, `PlayerControl.createQuest`, the New quest window): every type in the list can be made (a person, a place or nothing, as the type is about), the target can be
 *  an id, a name or coordinates, a quest is pinned and its own (no priority rule changes it), a second copy is refused, nonsense is refused with a reason, "go" starts the walk. */
public class QuestAddProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    static void check(String n, boolean ok) { PcProbe.check(n, ok); }
    static Area at(int dx, int dy) {
        Area h = Map.tiles[Map.range / 2][Map.range / 2];
        int x = Math.floorMod(h.x + dx, Map.range), y = Math.floorMod(h.y + dy, Map.range);
        if (Map.tiles[x][y] == null) Map.tiles[x][y] = new Area(Area.ghost(x == 0 ? Map.range - 1 : x - 1, y), 'e');
        return Map.tiles[x][y];
    }
    static Character npc(Area where, String name, String last) {
        Character c = new Character(where);
        c.age = 30; c.gender = "Male"; c.lvl = 5; c.majorLvl = 0; c.maxAge = 100000; c.caution = 100000; c.sect = null; c.trait = Character.Traits.Friendly; c.name = name; c.lastName = last;
        while (Map.characters.size() < c.id) Map.characters.add(null);
        Map.characters.add(c); Map.population++;
        return c;
    }
    static Quest find(Character p, String name) {
        for (Quest q : p.targetQuests) if (q.name.equals(name)) return q;
        for (Quest q : p.spotQuests) if (q.name.equals(name)) return q;
        return null;
    }
    static int count(Character p, String name) { int n = 0; for (Quest q : p.targetQuests) if (q.name.equals(name)) n++; for (Quest q : p.spotQuests) if (q.name.equals(name)) n++; return n; }

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
        Settings.lore = false;
        p.lvl = 100; p.majorLvl = 10; p.maxAge = 100000; p.caution = 0; p.targetQuests.clear(); p.spotQuests.clear(); p.currentQuest = null;
        PlayerControl pc = p.pc();
        Area lab = at(6, 6); lab.monsters = 0;
        p.location.removeChar(p); p.location = lab; lab.addChar(p);
        Character mei = npc(at(9, 9), "Meiling", "Quxy"), kid = npc(at(9, 9), "Smallone", "Quxy"); kid.age = 5;
        PlayerControl.class.getName();

        // ---- Q1: by id
        PcProbe.cmd("qadd Find Person #" + mei.id + " p=60", 60);
        Quest q = find(p, "Find Person");
        check("Q1 'qadd Find Person #id p=60' makes a pinned quest of mine about her at priority 60", q != null && q.pinned && q.userCreated && q.priority == 60 && q.targetCharacter == mei.id && q.category == Quest.QuestCategory.Character);
        PcProbe.cmd("qadd Find Person #" + mei.id + " p=60", 60);
        check("Q1b asking again makes no second copy (" + count(p, "Find Person") + ")", count(p, "Find Person") == 1);

        // ---- Q2: by name, by unique beginning
        PcProbe.cmd("qadd Courtship Meiling p=45", 60);
        Quest court = find(p, "Courtship");
        check("Q2 a person by part of a name (qadd Courtship Meiling): found her (" + (court == null ? "none" : court.targetCharacter) + ")", court != null && court.targetCharacter == mei.id && court.priority == 45);
        PcProbe.cmd("qadd Visit F #" + mei.id, 60);
        check("Q2b a unique beginning of the type is enough ('Visit F' = Visit Family)", find(p, "Visit Family") != null);

        // ---- Q3: places
        Area far = at(11, 7);
        PcProbe.cmd("qadd Hunt Beast at " + far.x * 100 + " " + far.y * 100 + " p=55", 60);
        Quest hb = find(p, "Hunt Beast");
        check("Q3 'qadd Hunt Beast at x y' targets that tile (" + (hb == null ? "none" : hb.targetArea.name) + ")", hb != null && hb.targetArea == far && hb.category == Quest.QuestCategory.Area);
        PcProbe.cmd("qadd Explore Dungeon here", 60);
        Quest ex = find(p, "Explore Dungeon");
        check("Q3b 'here' is the tile you stand on", ex != null && ex.targetArea == lab);
        PcProbe.cmd("qadd Sightseeing", 60);
        check("Q3c a place quest with no place means where you stand", find(p, "Sightseeing") != null && find(p, "Sightseeing").targetArea == lab);

        // ---- Q4: spot
        PcProbe.cmd("qadd Meditate repeat p=30", 60);
        Quest med = find(p, "Meditate");
        check("Q4 a spot quest waits in the spot queue and repeats (oneTime " + (med == null ? "?" : med.oneTime) + ")", med != null && p.spotQuests.contains(med) && !med.oneTime && med.pinned && med.priority == 30);
        PcProbe.cmd("qadd Meditate p=70", 60);
        check("Q4b the same errand again raises the old one to 70 instead of making a second (" + count(p, "Meditate") + ", " + med.priority + ")", count(p, "Meditate") == 1 && med.priority == 70);

        // ---- Q5: refusals
        int before = p.targetQuests.size() + p.spotQuests.size();
        PcProbe.cmd("qadd Banana", 60);
        PcProbe.cmd("qadd Sect Mission", 60);
        PcProbe.cmd("qadd Courtship", 60);
        PcProbe.cmd("qadd Courtship #" + p.id, 60);
        PcProbe.cmd("qadd Courtship Nobodyatall", 60);
        PcProbe.cmd("qadd Battle Rival #" + kid.id, 60);
        PcProbe.cmd("qadd Hunt Beast at 99999 99999", 60);
        PcProbe.cmd("qadd Find #" + mei.id, 60);
        check("Q5 nonsense makes nothing: an unknown type, a game's quest, a missing person, yourself, nobody, a child, off the map, an ambiguous type (" + before + " -> " + (p.targetQuests.size() + p.spotQuests.size()) + ")", p.targetQuests.size() + p.spotQuests.size() == before);
        check("Q5b 'Sect Mission' is a managed type and the typed word 'Find' alone is ambiguous", PlayerControl.MANAGED_TYPES.contains("Sect Mission") && PlayerControl.typeName("Find") == null);

        // ---- Q6: a priority rule does not touch a quest of mine
        PcProbe.cmd("qprio Find Person 5", 60);
        check("Q6 a priority rule leaves my own quest at 60 (" + find(p, "Find Person").priority + ")", find(p, "Find Person").priority == 60);
        PcProbe.cmd("qprio reset", 60);

        // ---- Q7: go
        p.targetQuests.removeIf(x -> x.name.equals("Courtship"));
        PcProbe.cmd("qadd Courtship #" + mei.id + " p=50 go", 120);
        check("Q7 'go' starts the chase after her (chase " + p.chaseId + ")", p.chaseId == mei.id || p.location == mei.location);

        // ---- Q8: every type can be made
        p.targetQuests.clear(); p.spotQuests.clear(); p.chaseId = -1;
        int made = 0, refused = 0; StringBuilder bad = new StringBuilder();
        for (String[] ty : PlayerControl.QUEST_TYPES) {
            String r = pc.createQuest(p, ty[0], mei, lab, 40, false);
            if (r.startsWith("!")) { refused++; bad.append(ty[0]).append(": ").append(r).append("; "); } else made++;
        }
        check("Q8 every type but the game's own (" + PlayerControl.QUEST_TYPES.length + " listed) can be made by hand (" + made + " made, " + refused + " refused " + (bad.length() > 300 ? bad.substring(0, 300) : bad) + ")", refused == PlayerControl.MANAGED_TYPES.size() && made == PlayerControl.QUEST_TYPES.length - PlayerControl.MANAGED_TYPES.size());
        int persons = 0, spots = 0, places = 0;
        for (Quest x : p.targetQuests) { if (x.category == Quest.QuestCategory.Character) persons++; else places++; }
        for (Quest x : p.spotQuests) spots++;
        check("Q8b the categories follow the type: " + persons + " about a person, " + places + " about a place, " + spots + " spot", persons == PlayerControl.PERSON_TYPES.size() && spots > 0 && places > 0);
        PcProbe.cmd("qadd", 60);
        System.out.println();
        PcProbe.real.println("[T] DONE");
    }
}
