package Game;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;

/** Save slots, rotating autosaves and crash-safe writing.
 *
 *  Files live in saves/: `<slot>.dat` (the world), `<slot>.meta` (year, population, players, time, size, CRC-32) and `<slot>.bak` (the previous
 *  good copy of that slot). A save is written to a temp file, checksummed, then moved into place, so an interrupted or failed save can never
 *  destroy the previous one; a load whose file fails its checksum (or cannot be read) falls back to the `.bak`. Autosaves rotate through
 *  auto-1 (newest) .. auto-N. A crash writes a `crash-<time>` slot and crash.log. A legacy save.dat next to the game is offered too. */
public class SaveManager {
    static File dir = new File("saves");
    static final File LEGACY = new File("save.dat");
    static final String QUICK = "quick";

    enum Kind { MANUAL, QUICK, AUTO, CRASH, LEGACY }

    static class Slot {
        String name; Kind kind = Kind.MANUAL; File file; long savedAt; int year; int population; String players = ""; long bytes; long crc; boolean hasBackup; boolean damaged;
    }

    // status for the GUI indicator (read from any thread)
    static volatile String state = "idle";      // idle | saving | loading | error
    static volatile String lastName = "";
    static volatile Kind lastKind = Kind.MANUAL;
    static volatile long lastAt = 0;
    static volatile int lastYear = 0;
    static volatile String lastError = "";

    static String sanitize(String name) {
        String n = name == null ? "" : name.trim().replaceAll("[^A-Za-z0-9_-]+", "_").replaceAll("^_+|_+$", "");
        if (n.length() > 40) n = n.substring(0, 40);
        return n;
    }

    static File dat(String n) { return new File(dir, n + ".dat"); }
    static File meta(String n) { return new File(dir, n + ".meta"); }
    static File bak(String n) { return new File(dir, n + ".bak"); }

    // ------------------------------------------------------------------ listing
    static List<Slot> list() {
        List<Slot> out = new ArrayList<>();
        File[] files = dir.listFiles((d, n) -> n.endsWith(".meta") || n.endsWith(".dat"));
        java.util.Set<String> seen = new java.util.HashSet<>();
        if (files != null) for (File f : files) {
            String n = f.getName().replaceAll("\\.(meta|dat)$", "");
            if (!seen.add(n)) continue;
            Slot s = new Slot();
            s.name = n; s.file = dat(n);
            s.hasBackup = bak(n).isFile();
            Properties p = new Properties();
            if (meta(n).isFile()) {
                try (FileInputStream in = new FileInputStream(meta(n))) { p.load(in); } catch (Exception ignored) { }
                try { s.kind = Kind.valueOf(p.getProperty("kind", "MANUAL")); } catch (Exception e) { s.kind = Kind.MANUAL; }
                s.savedAt = l(p, "savedAt"); s.year = (int) l(p, "year"); s.population = (int) l(p, "population"); s.players = p.getProperty("players", "");
                s.bytes = l(p, "bytes"); s.crc = l(p, "crc");
            } else if (s.file.isFile()) { s.savedAt = s.file.lastModified(); s.bytes = s.file.length(); }
            if (!s.file.isFile()) { if (!s.hasBackup) continue; s.damaged = true; }
            else if (s.bytes > 0 && s.file.length() != s.bytes) s.damaged = true;
            out.add(s);
        }
        if (LEGACY.isFile()) {
            Slot s = new Slot();
            s.name = "legacy"; s.kind = Kind.LEGACY; s.file = LEGACY; s.savedAt = LEGACY.lastModified(); s.bytes = LEGACY.length(); s.players = "(old save.dat)";
            out.add(s);
        }
        out.sort(Comparator.comparingLong((Slot s) -> s.savedAt).reversed());
        return out;
    }

    private static long l(Properties p, String k) { try { return Long.parseLong(p.getProperty(k, "0").trim()); } catch (Exception e) { return 0; } }

    static Slot find(String name) {
        for (Slot s : list()) if (s.name.equalsIgnoreCase(name)) return s;
        return null;
    }

    static String describe(Slot s) {
        return String.format("%-14s %-6s year %-5d pop %-8d %s  %s%s", s.name, s.kind.name().toLowerCase(), s.year, s.population,
                new java.text.SimpleDateFormat("MM-dd HH:mm").format(new java.util.Date(s.savedAt)), s.players, s.damaged ? "  [DAMAGED" + (s.hasBackup ? ", backup available]" : "]") : "");
    }

    // ------------------------------------------------------------------ writing
    /** Saves the current world (game thread, between ticks). Returns true on success. */
    static boolean save(String name, Kind kind) {
        String n = kind == Kind.AUTO ? "auto-0" : sanitize(name);
        if (n.isEmpty()) { lastError = "invalid slot name"; state = "error"; return false; }
        state = "saving"; lastError = "";
        final SaveData d = GameDriver.snapshot();
        final String slot = n;
        final boolean[] ok = {false};
        final String[] err = {""};
        Thread t = new Thread(null, () -> {
            try { writeSlot(slot, kind, d); ok[0] = true; }
            catch (Throwable e) { err[0] = String.valueOf(e); }
        }, "save", GameDriver.BIG_STACK);
        t.start();
        try { GameDriver.blocked(() -> { t.join(); return null; }); } catch (RuntimeException ignored) { }
        if (ok[0] && kind == Kind.AUTO) {
            try { rotateAutos(); ok[0] = true; n = "auto-1"; } catch (Throwable e) { ok[0] = false; err[0] = String.valueOf(e); }
        }
        if (ok[0]) {
            state = "idle"; lastName = kind == Kind.AUTO ? "auto-1" : slot; lastKind = kind; lastAt = System.currentTimeMillis(); lastYear = GameDriver.time / 52 + 1;
            System.out.println("[SAVE] Saved '" + lastName + "' (year " + lastYear + ").");
            SaveThumbs.capture(lastName);
        } else {
            state = "error"; lastError = err[0];
            System.out.println("[SAVE ERROR] " + err[0]);
        }
        return ok[0];
    }

    private static void writeSlot(String slot, Kind kind, SaveData d) throws Exception {
        dir.mkdirs();
        File tmp = new File(dir, slot + ".dat.tmp"), dat = dat(slot), bak = bak(slot);
        CRC32 crc = new CRC32();
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new CheckedOutputStream(new FileOutputStream(tmp), crc), 1 << 16))) {
            oos.writeObject(d);
        } catch (Throwable e) { tmp.delete(); throw new Exception("could not write " + tmp.getName() + ": " + e, e); }
        if (tmp.length() < 1024) { tmp.delete(); throw new Exception("written file is implausibly small"); }
        Properties p = new Properties();
        p.setProperty("kind", kind.name()); p.setProperty("savedAt", String.valueOf(d.savedAt)); p.setProperty("year", String.valueOf(d.time / 52 + 1));
        p.setProperty("population", String.valueOf(d.population)); p.setProperty("players", d.playerSummary == null ? "" : d.playerSummary);
        p.setProperty("bytes", String.valueOf(tmp.length())); p.setProperty("crc", String.valueOf(crc.getValue())); p.setProperty("name", slot);
        File mtmp = new File(dir, slot + ".meta.tmp");
        try (FileOutputStream mo = new FileOutputStream(mtmp)) { p.store(mo, "save slot"); }
        // keep the previous good copy, then swap the new one in; undo the backup move if the swap fails
        boolean movedOld = false;
        if (dat.isFile()) { Files.move(dat.toPath(), bak.toPath(), StandardCopyOption.REPLACE_EXISTING); movedOld = true; }
        try {
            Files.move(tmp.toPath(), dat.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Throwable e) {
            if (movedOld && !dat.isFile()) { try { Files.move(bak.toPath(), dat.toPath(), StandardCopyOption.REPLACE_EXISTING); } catch (Throwable ignored) { } }
            tmp.delete(); mtmp.delete();
            throw new Exception("could not move the new save into place: " + e, e);
        }
        Files.move(mtmp.toPath(), meta(slot).toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /** auto-0 (just written) becomes auto-1; older ones shift up; anything beyond the keep count is deleted. */
    private static void rotateAutos() throws Exception {
        int keep = Math.max(1, Settings.autosaveKeep);
        for (int i = keep; i >= 1; i--) {
            String from = i == 1 ? "auto-0" : "auto-" + (i - 1), to = "auto-" + i;
            if (i == 1) { deleteSlotFiles(to); }
            else if (i == keep) { deleteSlotFiles(to); }
            if (dat(from).isFile()) moveSlot(from, to);
        }
        for (int i = keep + 1; i < keep + 20; i++) deleteSlotFiles("auto-" + i);
    }

    private static void moveSlot(String from, String to) throws Exception {
        for (String ext : new String[]{".dat", ".meta", ".bak", ".png"}) {
            File f = new File(dir, from + ext), t = new File(dir, to + ext);
            if (f.isFile()) Files.move(f.toPath(), t.toPath(), StandardCopyOption.REPLACE_EXISTING);
            else t.delete();
        }
    }

    static void deleteSlotFiles(String n) { for (String ext : new String[]{".dat", ".meta", ".bak", ".dat.tmp", ".meta.tmp", ".png"}) new File(dir, n + ext).delete(); }

    static boolean delete(String name) {
        Slot s = find(name);
        if (s == null || s.kind == Kind.LEGACY) return false;
        deleteSlotFiles(s.name);
        return true;
    }

    /** Called by the game loop once a game year (per the autosave setting). */
    static void autosave() { save("auto", Kind.AUTO); }

    // ------------------------------------------------------------------ loading
    /** Loads a slot into the running game (game thread, between ticks). Falls back to the slot's backup if the main file is damaged. */
    static boolean load(String name) {
        Slot s = find(name);
        if (s == null) { System.out.println("[LOAD] No save called '" + name + "'."); return false; }
        state = "loading";
        boolean ok = false;
        String how = "";
        File main = s.file;
        if (main.isFile() && checksumOk(s)) ok = readInto(main);
        if (!ok && s.kind != Kind.LEGACY && bak(s.name).isFile()) {
            System.out.println("[LOAD] '" + s.name + "' could not be read intact; trying its backup.");
            ok = readInto(bak(s.name));
            if (ok) how = " (restored from the backup copy)";
        }
        state = ok ? "idle" : "error";
        if (ok) { lastName = s.name; lastAt = System.currentTimeMillis(); lastYear = GameDriver.time / 52 + 1; System.out.println("[LOAD] Loaded '" + s.name + "'" + how + ". Year " + lastYear + ", population " + Game.Map.population + "."); }
        else lastError = "could not load " + s.name;
        return ok;
    }

    static boolean checksumOk(Slot s) {
        if (s.crc == 0) return true; // legacy / no metadata: nothing to compare with
        try (FileInputStream in = new FileInputStream(s.file)) {
            CRC32 c = new CRC32();
            byte[] buf = new byte[1 << 16];
            int n;
            while ((n = in.read(buf)) > 0) c.update(buf, 0, n);
            return c.getValue() == s.crc;
        } catch (Exception e) { return false; }
    }

    private static boolean readInto(File f) {
        final boolean[] ok = {false};
        Thread t = new Thread(null, () -> {
            try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f), 1 << 16))) {
                SaveData d = (SaveData) ois.readObject();
                GameDriver.applySnapshot(d);
                ok[0] = true;
            } catch (Throwable e) { System.out.println("[LOAD ERROR] " + f.getName() + ": " + e); }
        }, "load", GameDriver.BIG_STACK);
        t.start();
        try { t.join(); } catch (InterruptedException ignored) { }
        return ok[0];
    }

    // ------------------------------------------------------------------ start-up menu
    /** Asks which save to continue (or a new game). Returns the slot name, or null for a new game. Console or GUI, like the rest of the start-up. */
    static String chooseAtStartup() {
        List<Slot> slots = list();
        if (slots.isEmpty()) return null;
        if (GameDriver.guiEnabled && GameDriver.mapViewer != null) return GuiWindows.startupSaveMenu(GameDriver.mapViewer, slots);
        System.out.println("Saved games:");
        for (int i = 0; i < slots.size(); i++) System.out.println("  " + (i + 1) + ") " + describe(slots.get(i)));
        System.out.println("Load which? (number, or N for a new game)");
        String a;
        try { a = GameDriver.scan.next().trim(); } catch (Exception e) { return null; }
        if (a.equalsIgnoreCase("y")) return slots.get(0).name; // old prompt style: yes = the newest
        try { int n = Integer.parseInt(a); if (n >= 1 && n <= slots.size()) return slots.get(n - 1).name; } catch (NumberFormatException ignored) { }
        return null;
    }

    // ------------------------------------------------------------------ crash safety
    private static volatile boolean crashing = false;

    /** Anything that kills the game thread leaves a crash.log and, if the world can still be written, a crash-<time> save. */
    static void installCrashHandler() {
        Thread.setDefaultUncaughtExceptionHandler((th, e) -> {
            if (crashing) return;
            crashing = true;
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            try (PrintWriter w = new PrintWriter(new java.io.FileWriter("crash.log", true))) {
                w.println("=== " + new java.util.Date() + " thread " + th.getName() + " ===");
                w.println(sw);
            } catch (Exception ignored) { }
            System.out.println("[CRASH] " + e + " (details in crash.log)");
            String saved = "";
            try {
                if (th.getName().equals("game") || th.getName().equals("main")) {
                    String n = "crash-" + new java.text.SimpleDateFormat("yyyyMMdd-HHmmss").format(new java.util.Date());
                    if (save(n, Kind.CRASH)) saved = "\nThe world was saved as '" + n + "' (Load it from the Saves window).";
                }
            } catch (Throwable ignored) { }
            final String msg = "The game hit an error and stopped:\n" + e + saved + "\nSee crash.log.";
            if (GameDriver.guiEnabled) javax.swing.SwingUtilities.invokeLater(() -> javax.swing.JOptionPane.showMessageDialog(GameDriver.mapViewer, msg, "Game error", javax.swing.JOptionPane.ERROR_MESSAGE));
        });
    }
}
