package Game;

import java.io.*;
import java.util.*;

/** 2026-10-10 (sixth batch): an old world stays small. The dead let go of their lists (husks), faint ties to the dead are forgotten and the relationship list keeps to its cap, copies of one
 *  errand are cut to a few, the share of people who keep a beast is bounded, saves are compressed (and old uncompressed ones still open), and the memory check of the epoch works. */
public class SlimProbe {
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
        c.age = 30; c.gender = "Male"; c.lvl = lvl; c.majorLvl = major; c.maxAge = 100000; c.caution = 50; c.sect = null; c.born = true;
        if (c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) c.trait = Character.Traits.Friendly;
        c.relationshipList.relationships.clear();
        c.id = Map.characters.size();
        GameDriver.UID = Math.max(GameDriver.UID, c.id + 1);
        Map.characters.add(c); Map.population++;
        return c;
    }
    static void tp(Character c, Area to) { c.location.removeChar(c); c.location = to; to.addChar(c); }
    static void die(Character c) { c.dead = true; Map.characters.set(c.id, null); }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        GameDriver.frozenNpcs = true;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        p.caution = 0; p.maxAge = 100000; p.lvl = 100; p.majorLvl = 10; p.age = 30;
        GameDriver.time = Math.max(GameDriver.time, 100);

        // ---- A: husks
        Character grandpa = adult(30, 3), dad = adult(30, 3), kid = adult(10, 1), stranger = adult(30, 3);
        kid.father = dad; dad.father = grandpa; dad.children.add(kid); grandpa.children.add(dad);
        for (Character c : new Character[]{grandpa, dad, stranger}) {
            c.relationshipList.relationships.add(new Relationship(kid.id, 3));
            c.backpack.add(new Item(Item.ItemType.Herb, 1, 5)); c.materialBag.add(new Item(Item.ItemType.Metal, 1, 5));
            c.targetQuests.add(new Quest("Hunt Beast", true, 20, 1, 40, home())); c.knownLocations.add(home());
        }
        die(grandpa); die(dad); die(stranger);
        Slim.yearly();
        check("A1 a dead father still held by his child is a husk (lists let go)", dad.husked && dad.relationshipList.relationships.isEmpty() && dad.backpack.isEmpty() && dad.materialBag.isEmpty() && dad.targetQuests.isEmpty() && dad.knownLocations.isEmpty());
        check("A2 so is the grandfather behind him", grandpa.husked && grandpa.backpack.isEmpty());
        check("A3 what the family window shows is kept (names, level, kin)", dad.name != null && dad.lastName != null && dad.lvl == 30 && dad.father == grandpa && kid.father == dad && dad.children.contains(kid));
        check("A4 a dead stranger that nobody holds is left to the corpse list", !stranger.husked);
        Slim.husk(stranger);
        check("A5 Slim.husk lets go of a corpse's lists", stranger.husked && stranger.backpack.isEmpty());
        Character human = adult(30, 3); human.player = 1; human.backpack.add(new Item(Item.ItemType.Herb, 1, 5)); die(human); Slim.husk(human);
        check("A6 a person who plays is never made a husk (they may be reborn)", !human.husked && !human.backpack.isEmpty());
        human.dead = false; Map.characters.set(human.id, human);
        Area here = home();
        int cap = here.characters.size();
        Character c13 = null;
        for (int i = 0; i < 14; i++) { Character x = adult(10, 1); x.backpack.add(new Item(Item.ItemType.Herb, 1, 1)); x.dead = true; here.addDead(x); if (i == 0) c13 = x; }
        check("A7 the 13th corpse of a tile pushes the oldest out as a husk (tile keeps " + here.deadCharacters.size() + ")", c13.husked && here.deadCharacters.size() <= 12);

        // ---- B: relationships
        Character owner = adult(30, 3);
        Character live1 = adult(30, 3), deadFriend = adult(30, 3), deadKin = adult(30, 3);
        die(deadFriend); die(deadKin);
        owner.relationshipList.relationships.clear();
        owner.relationshipList.relationships.add(new Relationship(deadFriend.id, 1));
        owner.relationshipList.relationships.add(new Relationship(deadKin.id, 3));
        owner.relationshipList.relationships.add(new Relationship(live1.id, 1));
        Relationship talked = new Relationship(deadFriend.id + 0, 1); talked.talks = 2; owner.relationshipList.relationships.add(talked);
        owner.relationshipList.trim();
        boolean hasWeakDead = false, hasKinDead = false, hasLive = false;
        for (Relationship r : owner.relationshipList.relationships) { if (r.friendID == deadFriend.id && r.value == 1 && r.talks == 0) hasWeakDead = true; if (r.friendID == deadKin.id) hasKinDead = true; if (r.friendID == live1.id) hasLive = true; }
        check("B1 a faint tie to the dead is forgotten, a strong one and a living one are kept", !hasWeakDead && hasKinDead && hasLive);
        check("B2 a tie that was talked over is kept", owner.relationshipList.relationships.contains(talked));
        owner.relationshipList.relationships.clear();
        List<Character> many = new ArrayList<>();
        for (int i = 0; i < RelationshipList.CAP + 30; i++) { Character x = adult(10, 1); many.add(x); owner.relationshipList.relationships.add(new Relationship(x.id, 1)); }
        owner.relationshipList.relationships.add(new Relationship(many.get(0).id + 1000000, 3));
        owner.relationshipList.trim();
        check("B3 the list is brought down to the cap (" + owner.relationshipList.relationships.size() + " of " + RelationshipList.CAP + ")", owner.relationshipList.relationships.size() <= RelationshipList.CAP);
        check("B4 the strong tie survives the cut", owner.relationshipList.relationships.stream().anyMatch(r -> r.value == 3));
        check("B5 the cap is " + RelationshipList.CAP, RelationshipList.CAP == 40);

        // ---- C: errands
        Character q = adult(30, 3);
        q.targetQuests.clear();
        for (int i = 0; i < 8; i++) q.targetQuests.add(new Quest("Hunt Beast", true, 10 + i, 1, 40, home()));
        Quest pinned = new Quest("Hunt Beast", true, 5, 1, 40, home()); pinned.pinned = true; q.targetQuests.add(pinned);
        for (int i = 0; i < 6; i++) q.targetQuests.add(new Quest("Find Person", true, 10, 1, 34, adult(10, 1)));
        for (int i = 0; i < 3; i++) q.targetQuests.add(new Quest("Find Person", false, 14, 2, 40, adult(10, 1)));  // a lasting friendship
        for (int i = 0; i < 5; i++) q.targetQuests.add(new Quest("Rob Caravan", true, 30 + i, 2, 55, adult(10, 1)));
        Quest other = new Quest("Explore Dungeon", true, 10, 1, 40, home()); q.targetQuests.add(other);
        Slim.trim(q);
        int hunts = 0, finds = 0; boolean keepsBest = false; for (Quest x : q.targetQuests) { if ("Hunt Beast".equals(x.name)) { hunts++; if (x.priority == 17) keepsBest = true; } if ("Find Person".equals(x.name)) finds++; }
        check("C1 copies of one errand are cut to " + Slim.SAME_ERRAND + " plus the pinned one (" + hunts + ")", hunts == Slim.SAME_ERRAND + 1 && keepsBest);
        int rob = 0; for (Quest x : q.targetQuests) if ("Rob Caravan".equals(x.name)) rob++;
        check("C2 faint-tie errands are cut to " + Character.WEAK_SOCIAL_CAP + ", lasting ones stay (" + finds + " Find Person), Rob Caravan to 2 (" + rob + ")", finds == Character.WEAK_SOCIAL_CAP + 3 && rob == 2);
        check("C3 a different errand is untouched", q.targetQuests.contains(other));
        check("C4 the weak-tie cap is 2 and the queue cap 24", Character.WEAK_SOCIAL_CAP == 2 && Character.TARGET_QUEUE_CAP == 24);

        // ---- D: beasts people keep
        int k0 = Taming.keptCount;
        Taming.keptCount = 0; int pop0 = Map.population; Map.population = 1000;
        boolean empty = Taming.roomForAnother();
        Taming.keptCount = 120; boolean atShare = Taming.roomForAnother();
        Taming.keptCount = 119; boolean under = Taming.roomForAnother();
        check("D1 people may win beasts over until " + Taming.NPC_SHARE + "% of them keep one (empty " + empty + ", at the share " + atShare + ", just under " + under + ")", empty && !atShare && under);
        Taming.keptCount = k0; Map.population = pop0;

        // ---- E: compressed saves
        File dir = new File("saves");
        boolean ok = SaveManager.save("slimtest", SaveManager.Kind.MANUAL);
        File f = new File(dir, "slimtest.dat");
        byte[] head = new byte[2]; try (FileInputStream in = new FileInputStream(f)) { in.read(head); }
        check("E1 a save is written (" + ok + ") and is gzip (" + String.format("%02x%02x", head[0], head[1]) + ", " + f.length() / 1024 + " KB)", ok && (head[0] & 0xff) == 0x1f && (head[1] & 0xff) == 0x8b);
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        try (ObjectOutputStream oo = new ObjectOutputStream(raw)) { oo.writeObject(GameDriver.snapshot()); }
        check("E2 it is much smaller than the plain stream (" + f.length() / 1024 + " KB against " + raw.size() / 1024 + " KB)", f.length() * 2 < raw.size());
        SaveData back;
        try (ObjectInputStream in = new ObjectInputStream(SaveManager.openSave(f))) { back = (SaveData) in.readObject(); }
        check("E3 it reads back (" + back.characters.size() + " slots, population " + back.population + ", start " + back.startPop + ")", back.characters.size() == Map.characters.size() && back.startPop == Map.startingPopulation);
        File old = new File(dir, "oldformat.dat");
        try (ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(old))) { oo.writeObject(GameDriver.snapshot()); }
        try (ObjectInputStream in = new ObjectInputStream(SaveManager.openSave(old))) { SaveData d2 = (SaveData) in.readObject(); check("E4 a save from before the compression still opens (" + d2.characters.size() + " slots)", d2.characters.size() == Map.characters.size()); }
        old.delete();
        SaveManager.delete("slimtest");

        // ---- G: the run statistics hold the lives of people who play, not of every reborn NPC
        int lives0 = RunStats.run.lives.size(), re0 = RunStats.run.reincarnations;
        Character npcSoul = adult(31, 3);
        RunStats.endLife(npcSoul, "reincarnated at Lv.31 (Major 3)"); RunStats.onReincarnation(npcSoul);
        check("G1 the rebirth of an NPC adds no life record and is not counted (" + lives0 + " -> " + RunStats.run.lives.size() + ")", RunStats.run.lives.size() == lives0 && RunStats.run.reincarnations == re0);
        for (int i = 0; i < 60; i++) { RunStats.Life l = new RunStats.Life(); l.bodyId = 900000 + i; l.name = "Junk " + i; l.end = i % 2 == 0 ? "reincarnated at Lv.31 (Major 3)" : "still living"; RunStats.run.lives.add(l); }
        RunStats.Life died = new RunStats.Life(); died.bodyId = 800000; died.name = "A dead body of the player"; died.end = "Killed by a beast"; RunStats.run.lives.add(died);
        RunStats.dropOthersLives(Map.characters);
        boolean kept = RunStats.run.lives.contains(died) && RunStats.run.lives.stream().noneMatch(l -> l.name.startsWith("Junk "));
        check("G2 an old save's records of other people's lives are dropped, a life that ended in a death is kept (" + RunStats.run.lives.size() + " left)", kept);

        // ---- F: the memory check
        int pct0 = Settings.epochMemoryPercent;
        Settings.epochMemoryPercent = 0;
        long[] note = new long[2];
        check("F1 with the memory limit off the epoch never comes for memory", !MemWatch.full(note) && note[1] > 0);
        Settings.epochMemoryPercent = 1;
        boolean over = MemWatch.full(note); check("F2 at 1% the answer follows the live memory (" + note[0] + " of " + note[1] + " MB, over: " + over + ")", over == (note[0] > note[1] / 100));
        Settings.epochMemoryPercent = 95;
        check("F3 at 95% a small world is under it", !MemWatch.full(note));
        Settings.epochMemoryPercent = pct0;
        check("F4 the line about the memory is told", MemWatch.line().contains("MB"));
    }
}
