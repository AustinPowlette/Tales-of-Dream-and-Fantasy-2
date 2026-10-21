package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-13: paused menus and saving. (1) Which commands run while the game is paused (`PlayerCommands.runsWhilePaused`) and which wait in the queue; `GameDriver.servePaused` runs the free ones and
 *  leaves the others in order. (2) A `save` typed at the prompt is written at once, without a move (the week does not change), and the save loads again into a consistent world (ids equal list positions,
 *  nobody dead left in a tile, the same living people). */
public class PausedProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    static void say(String s) { PcProbe.real.println("[T] " + s); }

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

    static int alive() { int n = 0; for (Character c : new ArrayList<>(Map.characters)) if (c != null && !c.dead && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster) n++; return n; }
    static int deadInTiles() {
        int n = 0;
        for (int x = 0; x < Map.range; x++) for (int y = 0; y < Map.range; y++) { Area ar = Map.tiles[x][y]; if (ar == null) continue; for (Character c : new ArrayList<>(ar.characters)) if (c != null && c.dead) n++; }
        return n;
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

        // ---- P1: the table
        String[] free = {"ranking", "rankings", "board", "e", "save", "save x", "settings hud off", "set limit 30", "music off", "policy", "duties all on", "note hello", "price 1 buy 1", "invite 1", "buy 1", "sell 1", "buygear", "learn 1",
                "gift 1 1", "feed 1", "eat", "drink", "coat", "consume", "install", "equip", "equip 1", "unequip weapon", "join", "resign", "recruit 1", "war", "peace", "alliance", "seek 5 talk", "qnew A 1 1 50", "mute X", "limit 10",
                "i", "l", "bag", "q", "here", "people", "script list", "script add x", "lore journal", "export run", "follow 3", "avoid 3", "leave", "disband", "promote 1", "release 1", "f", "craft menu", "mission", "!sell 1", "time", "preview 1"};
        String[] wait = {"w", "a", "s", "d", "nw", "x", "stay", "wait 4", "watch", "goto 3 3", "goquest", "chase #5", "meditate", "forage", "mine", "hunt", "explore", "do Meditate", "qdo", "craft weapon", "profession", "make food", "cook", "perform", "play",
                "tame", "talk 1", "spar 1", "attack 1", "poison 1", "script run", "script resume", "auto", "auto 5", "found city", "reincarnate", "control 5", "load x", "lore register", "walk", "o"};
        ArrayList<String> badFree = new ArrayList<>(), badWait = new ArrayList<>();
        for (String c : free) if (!PlayerCommands.runsWhilePaused(c)) badFree.add(c);
        for (String c : wait) if (PlayerCommands.runsWhilePaused(c)) badWait.add(c);
        PcProbe.check("P1 these run while paused: " + free.length + " commands, wrongly queued: " + badFree, badFree.isEmpty());
        PcProbe.check("P1b these wait for the game to resume: " + wait.length + " commands, wrongly run: " + badWait, badWait.isEmpty());

        // ---- P2: servePaused runs the free ones now and keeps the others in order
        Queue<String> q = new ArrayDeque<>(Arrays.asList("meditate", "ranking", "set limit 33", "wait 4", "time", "hunt"));
        int t0 = GameDriver.time; int m = PcProbe.mark();
        List<String> kept = GameDriver.servePaused(p, q);
        String o = PcProbe.since(m);
        PcProbe.check("P2 the free commands ran (the ranking and 'time' answered, the limit set: " + p.pc().listLimit + ")", o.toLowerCase().contains("highest level") && p.pc().listLimit == 33 && (o.contains("Year") || o.contains("year") || o.contains("Week") || o.contains("week")));
        PcProbe.check("P2b the others kept their order: " + kept, kept.equals(Arrays.asList("meditate", "wait 4", "hunt")) && new ArrayList<>(q).equals(kept));
        PcProbe.check("P2c no time passed while paused", GameDriver.time == t0);

        // ---- P3: save at the prompt costs no move
        int tSave = GameDriver.time; int aliveBefore = alive();
        PcProbe.cmd("save probe1", 120);
        boolean found = SaveManager.find("probe1") != null;
        PcProbe.check("P3 'save probe1' wrote the slot at once, with no move (time " + tSave + " -> " + GameDriver.time + ")", found && GameDriver.time == tSave);
        PcProbe.check("P3b a bare 'save' (quick) is also written at once", saveQuick());
        SaveManager.Slot s1 = SaveManager.find("probe1");
        PcProbe.check("P3c a save made at the prompt is marked mid-week and says so in the list", s1 != null && s1.midWeek && SaveManager.describe(s1).contains("[mid-week]"));
        GameDriver.saveRequestName = "probe3"; GameDriver.servePendingSaves(); // what the pause loop and the top of a tick do: between two weeks
        SaveManager.Slot s3 = SaveManager.find("probe3");
        PcProbe.check("P3d a save made between two weeks (paused, top of a tick) is not marked", s3 != null && !s3.midWeek && !SaveManager.describe(s3).contains("[mid-week]"));
        // ---- P4: kill someone this week (pending removal), save, and the saved world has nobody dead in the lists
        Character victim = new Character(Map.tiles[Map.range / 2][Map.range / 2]);
        victim.age = 30; victim.lvl = 5; victim.trait = Character.Traits.Friendly;
        while (Map.characters.size() < victim.id) Map.characters.add(null);
        Map.characters.add(victim); Map.population++;
        victim.location.addChar(victim);
        victim.dead(); // a death this week: pending removal until the end of the tick
        int pendingBefore = Map.remCharacters.size();
        PcProbe.cmd("save probe2", 120);
        PcProbe.check("P4 the death pending at the prompt was flushed by the save (was " + pendingBefore + ", now " + Map.remCharacters.size() + ")", Map.remCharacters.isEmpty() && SaveManager.find("probe2") != null);
        PcProbe.check("P4b the dead person's slot in the character list was emptied, as the end of a tick does", Map.characters.size() <= victim.id || Map.characters.get(victim.id) == null);
        // ---- P5: load it back and play on
        int aliveSaved = alive();
        PcProbe.cmd("!load probe2", 180);
        PcProbe.cmd("x", 120);
        PcProbe.check("P5 the save loads and the world is consistent (id mismatches " + PcProbe.idMismatch() + ", living people " + aliveSaved + " -> " + alive() + ")", PcProbe.idMismatch() == 0 && Math.abs(alive() - aliveSaved) <= 5);
        PcProbe.check("P5b the player is still there and prompting", PcProbe.pc() != null && PcProbe.in.blocked);
        for (int i = 0; i < 4; i++) PcProbe.cmd("x", 120);
        PcProbe.check("P5c four more weeks run after the load without errors", PcProbe.gameError == null && !PcProbe.out().contains("Exception"));
        say("DONE");
    }

    static boolean saveQuick() throws Exception {
        long before = new java.io.File(SaveManager.dir, SaveManager.QUICK + ".dat").lastModified();
        PcProbe.cmd("save", 120);
        java.io.File f = new java.io.File(SaveManager.dir, SaveManager.QUICK + ".dat");
        return f.isFile() && f.lastModified() >= before && SaveManager.find(SaveManager.QUICK) != null;
    }
}
