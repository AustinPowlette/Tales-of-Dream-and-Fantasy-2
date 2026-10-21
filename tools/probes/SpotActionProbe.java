package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-14: what a spare moment of a turn is used for (`PlayerControl.SPOT_ACTIONS`, `Character.chosenSpot`): "Surprise me" (the old Random) never fights and never loots the dead; Talk goes to the friendliest person here
 *  and never turns into a fight; Spar is a friendly bout (nobody dies); Forage, Mine and Explore fall back to meditating when the tile has nothing for them; `spotaction` takes the new names and the old "random". */
public class SpotActionProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    static void check(String n, boolean ok) { PcProbe.check(n, ok); }
    static Area at(int dx, int dy) {
        Area h = Map.tiles[Map.range / 2][Map.range / 2];
        int x = Math.floorMod(h.x + dx, Map.range), y = Math.floorMod(h.y + dy, Map.range);
        if (Map.tiles[x][y] == null) Map.tiles[x][y] = new Area(Area.ghost(x == 0 ? Map.range - 1 : x - 1, y), 'e');
        return Map.tiles[x][y];
    }
    static Character npc(Area where, String name, String aff) {
        Character c = new Character(where);
        c.age = 30; c.gender = "Male"; c.lvl = 5; c.majorLvl = 0; c.maxAge = 100000; c.caution = 100000; c.sect = null; c.trait = Character.Traits.Friendly; c.name = name; c.lastName = "Spot"; c.affiliation = aff;
        while (Map.characters.size() < c.id) Map.characters.add(null);
        Map.characters.add(c); Map.population++;
        return c;
    }
    static void tp(Character c, Area to) { c.location.removeChar(c); c.location = to; to.addChar(c); }
    static void rel(Character a, Character b, int v) { a.changeRelationship(b, 1); a.relationshipList.contains(b.id).value = v; }

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
        p.lvl = 100; p.majorLvl = 10; p.maxAge = 100000; p.caution = 0; p.affiliation = "Good"; p.trait = Character.Traits.Friendly; p.targetQuests.clear(); p.spotQuests.clear(); p.currentQuest = null;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        PlayerControl pc = p.pc();
        Area lab = at(7, 7); lab.monsters = 0; lab.herbMaterials = 0; lab.metalMaterials = 0; lab.dungeonType = null;
        tp(p, lab);
        check("S0 the list: Meditate, Hunt, Profession, Surprise me (the old Random), then Talk, Spar, Forage, Mine, Explore", PlayerControl.SPOT_ACTIONS.length == 9 && PlayerControl.SPOT_ACTIONS[3].equals("Surprise me") && PlayerControl.SPOT_QUESTS[3].equals("Random"));

        // ---- S1: Surprise me never fights or loots
        Character thug = npc(lab, "Thug", "Evil"); // an Evil stranger: `interact` could start a fight with him
        Character dead = npc(lab, "Corpse", "Neutral"); dead.dead = true; dead.born = true;
        p.foughtAt = -1; p.foughtWith = -1;
        int results = 0, fights = 0;
        long ss0 = p.ss;
        for (int i = 0; i < 300; i++) { int r = p.event(); if (r == 2) results++; if (p.foughtAt >= 0) fights++; }
        check("S1 300 'Surprise me' turns: every one did something (" + results + "), none fought (" + fights + ")", results == 300 && fights == 0);
        check("S1b ...and a fresh corpse in the tile is not looted (nothing taken: " + (p.ss - ss0) + ")", p.ss == ss0 && !thug.dead);

        // ---- S2: Talk
        Character friend = npc(lab, "Friend", "Good"); rel(p, friend, 2);
        int before = p.relationshipList.contains(friend.id).value;
        check("S2 safeTalk goes to the friendliest person, not the Evil stranger", p.safeTalk() && p.relationshipList.contains(friend.id) != null);
        Area empty = at(8, 8); tp(p, empty); empty.herbMaterials = 0; empty.metalMaterials = 0; empty.monsters = 0;
        double cult0 = p.cultivationBase;
        check("S2b with nobody to talk to it returns false", !p.safeTalk());
        p.cultivation = 10; p.cultivationBase = 0;
        pc.spotAction = 4;
        int r = p.spotQuests(new Quest("Talk", false, 30, 3, 50));
        check("S2c the Talk spot quest with nobody here meditates instead (cultivation " + Math.round(p.cultivationBase) + ")", r == 2 && p.cultivationBase > 0);
        tp(p, lab);

        // ---- S3: Spar
        thug.dead = false;
        Character sparer = npc(lab, "Sparrer", "Neutral");
        p.foughtAt = -1; p.foughtWith = -1;
        check("S3 safeSpar fights someone who is no enemy (friend first)", p.safeSpar() && p.foughtAt >= 0 && !friend.dead && !sparer.dead && !p.dead);
        check("S3b ...and the sparring flags are off again", !p.sparring && !friend.sparring);

        // ---- S4: the choice and its fallbacks
        pc.spotAction = 6; lab.herbMaterials = 5;
        Quest picked = new Quest("Meditate", false, 15, 2, 40);
        check("S4 'Forage' replaces a Meditate pick when the tile has herbs (" + p.chosenSpot(picked).name + ")", p.chosenSpot(picked).name.equals("Forage"));
        lab.herbMaterials = 0;
        check("S4b ...and keeps Meditate when it has none", p.chosenSpot(picked) == picked);
        pc.spotAction = 7; lab.metalMaterials = 5;
        check("S4c 'Mine' with metal here (" + p.chosenSpot(picked).name + ")", p.chosenSpot(picked).name.equals("Mine"));
        lab.metalMaterials = 0;
        pc.spotAction = 8;
        check("S4d 'Explore' without a dungeon keeps Meditate", p.chosenSpot(picked) == picked);
        lab.dungeonType = Area.DungeonType.LOST_RUINS;
        check("S4e 'Explore' with ruins here picks Explore", p.chosenSpot(picked).name.equals("Explore"));
        pc.spotAction = 4;
        check("S4f 'Talk' picks Talk", p.chosenSpot(picked).name.equals("Talk"));
        pc.spotAction = 3;
        check("S4g 'Surprise me' picks the Random spot quest", p.chosenSpot(picked).name.equals("Random"));
        lab.dungeonType = null;

        // ---- S5: the command
        PcProbe.cmd("spotaction talk", 60);
        check("S5 'spotaction talk' sets it (" + pc.spotAction + ")", pc.spotAction == 4);
        PcProbe.cmd("spotaction m", 60);
        check("S5b 'spotaction m' is meditate, not mine (" + pc.spotAction + ")", pc.spotAction == 0);
        PcProbe.cmd("spotaction random", 60);
        check("S5c the old name 'random' means Surprise me (" + pc.spotAction + ")", pc.spotAction == 3);
        PcProbe.cmd("spotaction sp", 60);
        check("S5d 'sp' is spar (" + pc.spotAction + ")", pc.spotAction == 5);
        PcProbe.cmd("spotaction banana", 60);
        check("S5e an unknown name changes nothing", pc.spotAction == 5);
        System.out.println();
        PcProbe.real.println("[T] DONE");
    }
}
