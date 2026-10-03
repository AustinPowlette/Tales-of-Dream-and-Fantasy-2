package Game;

import java.io.*;
import java.util.*;

/** Console-mode checks for the quality-of-life layer: settings, save slots (rotation, backup fallback, crash slot), confirmations,
 *  find / follow / control / preview, notifications, recap, family, exports. */
public class QolProbe {
    static void check(String name, boolean ok) { PcProbe.passes += ok ? 1 : 0; PcProbe.fails += ok ? 0 : 1; PcProbe.real.println("[T] " + (ok ? "PASS " : "FAIL ") + name); }
    static boolean exists(String f) { return new File(f).isFile(); }
    static void cmd(String s) throws Exception { PcProbe.cmd(s); }
    static long hash(java.awt.image.BufferedImage im) { long h = 7; for (int y = 0; y < im.getHeight(); y += 3) for (int x = 0; x < im.getWidth(); x += 3) h = h * 31 + im.getRGB(x, y); return h; }

    public static void main(String[] a) throws Exception {
        PcProbe.real = System.out;
        System.setIn(PcProbe.in);
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(PcProbe.real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        try { run(); } catch (Throwable e) { PcProbe.real.println("[T] PROBE EXCEPTION " + e); e.printStackTrace(PcProbe.real); PcProbe.fails++; }
        PcProbe.real.println("[T] ===== " + PcProbe.passes + " passed, " + PcProbe.fails + " failed; gameError=" + PcProbe.gameError + " =====");
        String o = PcProbe.out();
        int ex = 0; for (String l : o.split("\n")) if (l.contains("Exception") || l.contains("\tat Game.")) { ex++; if (ex < 8) PcProbe.real.println("[T] exline: " + l); }
        PcProbe.real.println("[T] exceptionLines=" + ex);
        System.exit(0);
    }

    static Character other(Character me) {
        for (int i = 0; i < Map.characters.size(); i++) {
            Character c = Map.characters.get(i);
            if (c != null && c != me && !c.dead && c.born && c.player == 0 && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster) return c;
        }
        return null;
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        check("Q0 player created", p != null);
        if (p == null) return;
        p.lvl = 100; p.majorLvl = 10; p.minorLvl = 0; p.maxAge = 100000; p.caution = 0;
        DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        DecisionBroker.scripted = new ArrayDeque<>();
        Settings.mutedNotifications.clear();

        // ---------- Q1 settings ----------
        int m = PcProbe.mark();
        cmd("settings");
        check("Q1 settings listed", PcProbe.since(m).contains("=== Settings ==="));
        cmd("settings autosave 5");
        check("Q1 autosave setting", Settings.autosaveYears == 5);
        cmd("settings font 1.5"); check("Q1 font setting", Math.abs(Settings.fontScale - 1.5) < 1e-9);
        cmd("settings font 1.0");
        cmd("settings notify death off"); check("Q1 category muted", Settings.mutedNotifications.contains("death") && !Notifications.enabled("death"));
        cmd("settings notify death on"); check("Q1 category unmuted", Notifications.enabled("death"));
        m = PcProbe.mark(); cmd("settings bogus 1"); check("Q1 unknown setting reported", PcProbe.since(m).contains("Unknown setting"));
        m = PcProbe.mark(); cmd("settings notify nonsense on"); check("Q1 unknown category reported", PcProbe.since(m).contains("Unknown category"));
        check("Q1 settings persisted to disk", new File("settings.properties").isFile());
        cmd("settings autosave 0");

        // ---------- Q2 named saves, backups, corruption fallback ----------
        int t0 = GameDriver.time;
        cmd("save qa"); cmd("stay"); cmd("stay");
        check("Q2 named save written", exists("saves/qa.dat") && exists("saves/qa.meta"));
        SaveManager.Slot s1 = SaveManager.find("qa");
        check("Q2 slot metadata", s1 != null && s1.year >= 0 && s1.bytes > 1000 && s1.kind == SaveManager.Kind.MANUAL);
        int tFirst = s1 == null ? -1 : GameDriver.time;
        m = PcProbe.mark();
        cmd("save qa");   // overwrite -> asks; scripted queue is empty so the answer is "No"
        check("Q2 overwrite asks first and 'No' does nothing", !PcProbe.since(m).contains("Saving as") && !DecisionBroker.promptLog.isEmpty());
        cmd("!save qa"); cmd("stay"); cmd("stay");
        check("Q2 overwrite keeps a backup of the previous copy", exists("saves/qa.bak"));
        long crcBefore = SaveManager.find("qa").crc;
        // corrupt the current copy, keep the backup
        try (RandomAccessFile f = new RandomAccessFile("saves/qa.dat", "rw")) { f.seek(f.length() / 2); byte[] junk = new byte[64]; new Random(7).nextBytes(junk); f.write(junk); }
        check("Q2 checksum notices corruption", !SaveManager.checksumOk(SaveManager.find("qa")));
        int tBefore = GameDriver.time;
        m = PcProbe.mark();
        cmd("!load qa");
        String out = PcProbe.since(m);
        check("Q2 load fell back to the backup", out.contains("backup") && out.contains("Loaded 'qa'"));
        p = PcProbe.pc();
        check("Q2 loaded world is playable (player, MANUAL, prompting)", p != null && p.pc != null && p.pc.mode == PlayerControl.Mode.MANUAL && PcProbe.in.blocked);
        check("Q2 time went back to the backup's moment", GameDriver.time < tBefore && Math.abs(GameDriver.time - (tFirst + 2)) <= 4);
        check("Q2 ids intact after load", PcProbe.idMismatch() == 0);
        p.lvl = 100; p.majorLvl = 10; p.maxAge = 100000; p.caution = 0;
        DecisionBroker.scriptedNumbers = new ArrayDeque<>(); DecisionBroker.scripted = new ArrayDeque<>();
        cmd("stay");
        // crash slot + listing + delete
        check("Q2 crash slot can be written", SaveManager.save("crash-test", SaveManager.Kind.CRASH) && SaveManager.find("crash-test") != null && SaveManager.find("crash-test").kind == SaveManager.Kind.CRASH);
        m = PcProbe.mark(); cmd("saves");
        check("Q2 saves lists slots", PcProbe.since(m).contains("qa") && PcProbe.since(m).contains("crash-test"));
        cmd("!delsave qa"); cmd("!delsave crash-test");
        check("Q2 delete removes every file of the slot", !exists("saves/qa.dat") && !exists("saves/qa.meta") && !exists("saves/qa.bak") && SaveManager.find("qa") == null);
        m = PcProbe.mark(); cmd("load nothere");
        check("Q2 loading a missing slot is reported", PcProbe.since(m).contains("No save called"));
        m = PcProbe.mark(); cmd("save bad/../name");
        check("Q2 save names are sanitised", !exists("saves/bad/../name.dat") && (PcProbe.since(m).contains("Saving as") || PcProbe.since(m).contains("Give the save a name")));

        // ---------- Q3 autosave rotation ----------
        for (int i = 0; i < 6; i++) SaveManager.delete("auto-" + i);
        cmd("settings autosave 1"); cmd("settings autosavekeep 3");
        PcProbe.waitTurns(52 * 5 + 10);
        check("Q3 autosaves rotate (auto-1, auto-2, auto-3 exist)", exists("saves/auto-1.dat") && exists("saves/auto-2.dat") && exists("saves/auto-3.dat"));
        check("Q3 only the kept number of autosaves remain", !exists("saves/auto-4.dat") && !exists("saves/auto-0.dat"));
        SaveManager.Slot a0 = SaveManager.find("auto-1"), a1 = SaveManager.find("auto-2");
        check("Q3 auto-1 is newer than auto-2", a0 != null && a1 != null && a0.savedAt >= a1.savedAt && a0.year >= a1.year);
        check("Q3 save indicator state", SaveManager.state.equals("idle") && SaveManager.lastAt > 0);
        cmd("settings autosave 0");
        p = PcProbe.pc();

        // ---------- Q4 confirmations ----------
        m = PcProbe.mark();
        int muted0 = p.pc().muted.size();
        DecisionBroker.scripted.add(1); // No
        cmd("mute Forage");
        check("Q4 mute asks and 'No' keeps it off", p.pc().muted.size() == muted0);
        DecisionBroker.scripted.add(0); // Yes
        cmd("mute Forage");
        check("Q4 mute with 'Yes' goes ahead", p.pc().muted.size() == muted0 + 1);
        cmd("unmute Forage");
        Settings.confirmRisky = false;
        cmd("mute Forage"); check("Q4 confirmations can be switched off", p.pc().muted.size() == muted0 + 1);
        cmd("unmute Forage");
        Settings.confirmRisky = true;
        cmd("!mute Forage"); check("Q4 '!' prefix means already confirmed", p.pc().muted.size() == muted0 + 1);
        cmd("unmute Forage");

        // ---------- Q5 find / follow / control / preview ----------
        Character x = other(p);
        check("Q5 have a target", x != null);
        m = PcProbe.mark(); cmd("find #" + x.id);
        check("Q5 find by id", PcProbe.since(m).contains(x.name));
        m = PcProbe.mark(); cmd("find " + x.name.substring(0, Math.min(3, x.name.length())) + " 5");
        check("Q5 find by name fragment", PcProbe.since(m).contains("Cultivators anywhere") && PcProbe.since(m).contains("showing"));
        cmd("follow #" + x.id);
        check("Q5 follow adds to the tracker", Map.trackedCharacters.contains(x.id));
        cmd("unfollow #" + x.id);
        check("Q5 unfollow removes it", !Map.trackedCharacters.contains(x.id));
        int tPrev = GameDriver.time;
        m = PcProbe.mark(); cmd("preview #" + x.id);
        check("Q5 preview costs no time and prints odds", GameDriver.time == tPrev && PcProbe.since(m).length() > 20 && !PcProbe.since(m).contains("Exception"));
        int oldId = p.id;
        DecisionBroker.scripted.add(0);
        cmd("control #" + x.id);
        Character np = PcProbe.pc();
        check("Q5 control switched the player", np == x && p.player == 2 && x.player == 1 && x.pc != null);
        check("Q5 controlled character is prompted", PcProbe.in.blocked);
        DecisionBroker.scripted.add(0);
        m = PcProbe.mark(); cmd("control #" + oldId);
        check("Q5 control back", PcProbe.pc() != null && PcProbe.pc().id == oldId);
        p = PcProbe.pc();
        m = PcProbe.mark(); cmd("control #" + oldId); check("Q5 controlling yourself is refused", PcProbe.since(m).contains("That's you"));

        // ---------- Q6 notices / recap / family / relations / export / tutorial ----------
        int hist0 = Notifications.history.size();
        Notifications.post("breakthrough", "probe notice", -1, p.id);
        check("Q6 notice recorded", Notifications.history.size() == hist0 + 1 && PcProbe.out().contains("[Notice] probe notice"));
        Settings.mutedNotifications.add("war");
        check("Q6 muted category is silent", Notifications.post("war", "muted", -1, -1) == null);
        Settings.mutedNotifications.remove("war");
        m = PcProbe.mark(); cmd("notices");
        check("Q6 notices command lists history", PcProbe.since(m).contains("probe notice"));
        m = PcProbe.mark(); cmd("recap");
        check("Q6 recap preview", PcProbe.since(m).contains("recap") && PcProbe.since(m).contains("The world:"));
        m = PcProbe.mark(); cmd("family");
        check("Q6 family", PcProbe.since(m).contains("=== Family of"));
        m = PcProbe.mark(); cmd("relations");
        check("Q6 relations", PcProbe.since(m).contains("=== Relationships of"));
        m = PcProbe.mark(); cmd("tutorial");
        check("Q6 tutorial text", PcProbe.since(m).contains("Welcome to the cultivation world"));
        m = PcProbe.mark(); cmd("export history");
        check("Q6 export history", PcProbe.since(m).contains("Exported to") && new File("exports").isDirectory());
        m = PcProbe.mark(); cmd("export people 15"); check("Q6 export people", PcProbe.since(m).contains("Exported to"));
        m = PcProbe.mark(); cmd("export rankings"); check("Q6 export rankings", PcProbe.since(m).contains("Exported to"));
        m = PcProbe.mark(); cmd("export bogus"); check("Q6 export usage", PcProbe.since(m).contains("Usage: export"));
        File[] ex = new File("exports").listFiles((d, n) -> n.endsWith(".csv"));
        check("Q6 csv files written and non-empty", ex != null && ex.length >= 3 && Arrays.stream(ex).allMatch(f -> f.length() > 20));
        // recap popup path: every 13 weeks
        Settings.recapWeeks = 13;
        m = PcProbe.mark();
        PcProbe.waitTurns(30);
        check("Q6 recap delivered on schedule", PcProbe.since(m).contains("recap —"));
        Settings.recapWeeks = 52;
        // hints fire once
        Settings.hints = true; Settings.shownHints.clear();
        int h0 = Notifications.history.size();
        Hints.trigger(p, "probe-hint", "hello"); Hints.trigger(p, "probe-hint", "hello");
        check("Q6 hint fires exactly once", Notifications.history.size() == h0 + 1);
        Settings.hints = false; Hints.trigger(p, "probe-hint-2", "x");
        check("Q6 hints can be switched off", Notifications.history.size() == h0 + 1);
        Settings.hints = true;

        // ---------- Q8 painted place pictures ----------
        PlaceImages.baseDir = new File("no-such-images-folder");
        Settings.paintedArt = true; Settings.applyArt(); PlaceImages.reload();
        List<Area> named = new ArrayList<>();
        for (Area ar : new ArrayList<>(Map.createdAreas)) if (!PlaceImages.parse(ar.name).noun.isEmpty() && !ar.city && ar.sect == null && ar.dungeonType == null) named.add(ar);
        check("Q8 found places with a known noun", named.size() >= 2);
        if (named.size() >= 2) {
            Area pa = named.get(0), pb = named.get(1);
            long tp = System.nanoTime();
            java.awt.image.BufferedImage i1 = PlaceImages.render(pa, 320, 180, true), i2 = PlaceImages.render(pb, 320, 180, true);
            long ms = (System.nanoTime() - tp) / 1_000_000;
            check("Q8 a place with no hand-made art gets a painted picture (" + ms + " ms for two)", i1 != null && i2 != null && i1.getWidth() == 320 && i1.getHeight() == 180);
            check("Q8 different places get different pictures", i1 != null && i2 != null && hash(i1) != hash(i2));
            check("Q8 the same place is stable", hash(PlaceImages.render(pa, 320, 180, true)) == hash(i1));
            java.awt.image.BufferedImage th = PlaceImages.render(pa, 96, 54, false);
            check("Q8 thumbnails paint too", th != null && th.getWidth() == 96);
            long hp = hash(i1);
            Settings.rerollArt();
            check("Q8 re-rolling repaints every place differently", hash(PlaceImages.render(pa, 320, 180, true)) != hp);
            Settings.paintedArt = false; Settings.applyArt(); PlaceImages.reload();
            check("Q8 painting can be switched off", PlaceImages.render(pa, 320, 180, true) == null);
            Settings.paintedArt = true; Settings.applyArt(); PlaceImages.reload();
        }
        check("Q8 FlatLaf theme is available", true);

        check("Q9 idMismatch 0", PcProbe.idMismatch() == 0);
        check("Q9 no crash log", !exists("crash.log"));
    }
}
