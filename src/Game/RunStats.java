package Game;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/** Per-run statistics. A "run" is one playthrough of one world: every body the human has lived in (reincarnations, takeovers after death), what
 *  they reached and how each life ended, plus run-wide totals. It is saved with the world, shown by `runstats` and in the Run window, exported
 *  with `export run`, and written to the hall of fame (`exports/hall_of_fame.txt`) when the run ends (a Brutal death, or `retire`). */
final class RunStats {
    private RunStats() { }

    static class Life implements Serializable {
        static final long serialVersionUID = 1L;
        int bodyId; String name; int startWeek, startAge, endWeek = -1; int peakLvl, peakMajor, kills, potentialStart, potentialEnd; String end = "still living";
        boolean open() { return endWeek < 0; }
    }

    static class Data implements Serializable {
        static final long serialVersionUID = 1L;
        int startWeek, difficulty; String startKind = "?"; String firstName = "?";
        ArrayList<Life> lives = new ArrayList<>();
        double peakWealth; int deepestDungeon, loved, tamed, founded, reincarnations, children;
        boolean ended; int endWeek; String endNote = "";
    }

    static volatile Data run = new Data();

    static void reset() { run = new Data(); }

    /** A new run starts with this character. */
    static void begin(Character c, String kind, int difficulty) {
        run = new Data();
        run.startWeek = GameDriver.time; run.difficulty = difficulty; run.startKind = kind == null ? "?" : kind; run.firstName = c.name + " " + c.lastName;
        open(c);
    }

    /** Starts a record for a body the human has just taken over (a second player, or a body chosen after death). */
    static void track(Character c) { if (!run.ended) open(c); }

    private static synchronized Life open(Character c) {
        for (Life l : run.lives) if (l.bodyId == c.id && l.open()) return l;
        Life l = new Life();
        l.bodyId = c.id; l.name = c.name + " " + c.lastName; l.startWeek = GameDriver.time; l.startAge = c.age; l.potentialStart = c.potential; l.potentialEnd = c.potential;
        run.lives.add(l);
        return l;
    }

    private static void update(Life l, Character c) {
        l.peakLvl = Math.max(l.peakLvl, c.lvl); l.peakMajor = Math.max(l.peakMajor, c.majorLvl); l.kills = Math.max(l.kills, c.kills); l.potentialEnd = c.potential;
        double w = c.ss + c.sc * 1e6 + c.dc * 1e12 + c.ij * 1e18;
        if (w > run.peakWealth) run.peakWealth = w;
        if (c.location != null && c.location.dungeonType != null) run.deepestDungeon = Math.max(run.deepestDungeon, c.location.lvl);
    }

    /** Once a year: every living human's life is brought up to date (and a body the human just took over gets its own record). */
    static void yearly() {
        if (run.ended) return;
        for (Character h : Notifications.humans()) { if (h == null || h.dead) continue; update(open(h), h); }
    }

    /** The body is left behind: how it ended ("reincarnated", "killed by ...", "old age ..."). */
    static void endLife(Character c, String how) {
        if (c == null || c.player < 1) return; // the life of a person who plays (an NPC that reincarnates adds nothing: it used to add a record for every rebirth in the world)
        Life l = open(c);
        update(l, c);
        l.endWeek = GameDriver.time; l.end = how;
    }

    /** The character was reborn (the old life was closed by `endLife`): a new life record starts. */
    static void onReincarnation(Character c) {
        if (c == null || c.player < 1) return;
        run.reincarnations++;
        open(c);
    }

    static void onLoved() { run.loved++; }
    static void onTamed() { run.tamed++; }
    static void onFounded() { run.founded++; }
    static void onChild() { run.children++; }

    /** The run is over (Brutal death) or retired: written to the hall of fame once. */
    static void finish(Character c, String note) {
        if (run.ended) return;
        if (c != null) endLife(c, note);
        run.ended = true; run.endWeek = GameDriver.time; run.endNote = note == null ? "" : note;
        writeHallOfFame();
    }

    /** The run ends without a hall-of-fame line of its own (the ending already wrote one). */
    static void finishQuiet(Character c, String note) {
        if (run.ended) return;
        if (c != null) endLife(c, note);
        run.ended = true; run.endWeek = GameDriver.time; run.endNote = note == null ? "" : note;
    }

    /** One line in the hall of fame for an ending of the story (the run may go on). */
    static void hallEnding(Character c, String ending, int score) {
        Data d = run;
        int peakLvl = c.lvl, peakMajor = c.majorLvl, kills = c.kills;
        for (Life l : d.lives) { peakLvl = Math.max(peakLvl, l.peakLvl); peakMajor = Math.max(peakMajor, l.peakMajor); kills = Math.max(kills, l.kills); }
        String line = "ENDING | year " + GameDriver.time / 52 + " | " + c.name + " " + c.lastName + " (" + d.startKind + ") | " + ending + " | SCORE " + score + " | " + years() + " years, " + d.reincarnations + " reincarnations | peak Lv." + peakLvl + " Major " + peakMajor + " | " + kills + " kills | potential " + c.potential;
        try (PrintWriter w = new PrintWriter(new FileWriter(hallFile(), true))) { w.println(line); } catch (IOException ignored) { }
    }

    // ------------------------------------------------------------------ report
    static int years() { return (run.ended ? run.endWeek : GameDriver.time) / 52 - run.startWeek / 52; }

    static String text() {
        Data d = run;
        StringBuilder sb = new StringBuilder("=== Run statistics ===\n");
        String[] dn = Starts.DIFFICULTY_NAMES;
        sb.append("Began as ").append(d.firstName).append(" (").append(d.startKind).append(", ").append(dn[Math.max(0, Math.min(dn.length - 1, d.difficulty))]).append(") in year ").append(d.startWeek / 52)
          .append("; ").append(d.ended ? "ended in year " + d.endWeek / 52 + " (" + d.endNote + ")" : "still running").append(" — ").append(years()).append(" years played.\n");
        int peakLvl = 0, peakMajor = 0, kills = 0, bestLife = 0;
        for (Life l : d.lives) { peakLvl = Math.max(peakLvl, l.peakLvl); peakMajor = Math.max(peakMajor, l.peakMajor); kills += l.kills; bestLife = Math.max(bestLife, (l.endWeek < 0 ? GameDriver.time : l.endWeek) - l.startWeek); }
        sb.append(String.format("Lives lived: %d   reincarnations: %d   peak level %d (Major %d)   kills %d   longest life %d years%n", d.lives.size(), d.reincarnations, peakLvl, peakMajor, kills, bestLife / 52));
        sb.append(String.format("Peak wealth: %s ss   deepest dungeon level: %d   companions tamed: %d   sects/cities founded: %d   children: %d   loved ones outlived: %d%n",
                TextTools.si(d.peakWealth), d.deepestDungeon, d.tamed, d.founded, d.children, d.loved));
        sb.append("\nLife by life:\n");
        int i = 1;
        for (Life l : d.lives) {
            sb.append(String.format("%3d. %-26s years %d-%d  age %d->%d  peak Lv.%d (Major %d)  potential %d->%d  kills %d  — %s%n", i++, l.name, l.startWeek / 52, (l.endWeek < 0 ? GameDriver.time : l.endWeek) / 52,
                    l.startAge, l.startAge + ((l.endWeek < 0 ? GameDriver.time : l.endWeek) - l.startWeek) / 52, l.peakLvl, l.peakMajor, l.potentialStart, l.potentialEnd, l.kills, l.end));
        }
        if (d.lives.isEmpty()) sb.append("  (no lives recorded yet)\n");
        return sb.toString();
    }

    /** One line per life, as CSV, for spreadsheets. */
    static String csv() {
        StringBuilder sb = new StringBuilder("life,name,start_year,end_year,start_age,peak_level,peak_major,potential_start,potential_end,kills,end\n");
        int i = 1;
        for (Life l : run.lives) sb.append(i++).append(',').append(q(l.name)).append(',').append(l.startWeek / 52).append(',').append((l.endWeek < 0 ? GameDriver.time : l.endWeek) / 52).append(',').append(l.startAge).append(',')
                .append(l.peakLvl).append(',').append(l.peakMajor).append(',').append(l.potentialStart).append(',').append(l.potentialEnd).append(',').append(l.kills).append(',').append(q(l.end)).append('\n');
        return sb.toString();
    }

    private static String q(String s) { return '"' + (s == null ? "" : s.replace("\"", "'")) + '"'; }

    // ------------------------------------------------------------------ hall of fame
    static File hallFile() { File dir = new File(Settings.exportDir); dir.mkdirs(); return new File(dir, "hall_of_fame.txt"); }

    static void writeHallOfFame() {
        Data d = run;
        int peakLvl = 0, peakMajor = 0, kills = 0;
        for (Life l : d.lives) { peakLvl = Math.max(peakLvl, l.peakLvl); peakMajor = Math.max(peakMajor, l.peakMajor); kills += l.kills; }
        String line = "year " + d.endWeek / 52 + " | " + d.firstName + " (" + d.startKind + ", " + Starts.DIFFICULTY_NAMES[Math.max(0, Math.min(2, d.difficulty))] + ") | " + years() + " years, " + d.lives.size() + " lives, "
                + d.reincarnations + " reincarnations | peak Lv." + peakLvl + " Major " + peakMajor + " | " + kills + " kills | wealth " + TextTools.si(d.peakWealth) + " | " + d.endNote;
        try (PrintWriter w = new PrintWriter(new FileWriter(hallFile(), true))) { w.println(line); } catch (IOException ignored) { }
    }

    static String hallOfFame() {
        File f = hallFile();
        if (!f.isFile()) return "=== Hall of fame ===\n  (empty: runs that end — a Brutal death, or `retire` — are recorded in " + f.getPath() + ")\n";
        try { return "=== Hall of fame (" + f.getPath() + ") ===\n" + new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8); } catch (IOException e) { return "Could not read " + f.getPath(); }
    }

    // ------------------------------------------------------------------ save / load
    static void snapshotInto(SaveData d) { d.runStats = run; }
    static void restoreFrom(SaveData d) {
        run = d.runStats != null ? d.runStats : new Data();
        dropOthersLives(d.characters);
    }

    /** Saves written before 2026-10-10 hold a record for every rebirth of every NPC in the world (34,536 in a world of 560 years, and "reincarnations" counted 17,873): only the lives of people who
     *  play are kept - those of a body a human plays now, and the ones that ended in a death (an NPC's life is never closed by a death here). */
    static void dropOthersLives(java.util.List<Character> list) {
        if (run == null || run.lives == null || run.lives.size() < 50 || list == null) return; // a run of a few lives is the real thing
        java.util.HashSet<Integer> keep = new java.util.HashSet<>();
        for (Character c : list) if (c != null && c.player > 0) keep.add(c.id);
        for (Life l : run.lives) if (l.end != null && !l.end.startsWith("reincarnated") && !"still living".equals(l.end)) keep.add(l.bodyId);
        int before = run.lives.size();
        run.lives.removeIf(l -> !keep.contains(l.bodyId));
        if (run.lives.size() < before) {
            int reinc = 0;
            for (Life l : run.lives) if (l.end != null && l.end.startsWith("reincarnated")) reinc++;
            run.reincarnations = reinc;
        }
    }
}
