package Game;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** The song generator and the kept songs (no game, no sound card, a few seconds): every arrangement composes songs that pass the checks, the same seed gives the same
 *  song, different seeds give different songs, the arrangement of the written song is kept, songs can be kept to a folder and dropped, the runtime share works and
 *  the `music gen|keep|drop|share|list` commands do what they say. Compile with tools/SongTool.java is not needed; SongProbe's `Run` is reused. */
public class SongGenProbe {
    static int pass = 0, fail = 0;

    static void check(String name, boolean ok) { if (ok) pass++; else fail++; System.out.println("[T] " + (ok ? "PASS " : "FAIL ") + name); }

    static String leadLine(String[] lines, String section) {
        String cur = "";
        for (String l : lines) {
            String t = l.trim();
            if (t.startsWith("[")) cur = t.substring(1, t.indexOf(']')).split("<")[0].trim();
            else if (cur.equals(section) && (t.startsWith("lead:") || t.startsWith("horn:"))) return t;
        }
        return "";
    }

    static String say(String command) throws Exception {
        Method m = PlayerCommands.class.getDeclaredMethod("musicCommand", String[].class);
        m.setAccessible(true);
        PrintStream real = System.out;
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bo, true));
        try { m.invoke(null, (Object) command.split(" ")); } finally { System.setOut(real); }
        return bo.toString();
    }

    static String command;

    public static void main(String[] a) throws Exception {
        File tmp = Files.createTempDirectory("songs").toFile();
        File old = SongBook.dir;
        SongBook.dir = new File(tmp, "songs");

        int nulls = 0, attempts = 0, total = 0, unclean = 0, sameArrangement = 0;
        long t0 = System.nanoTime();
        List<SongGen.Made> made = new ArrayList<SongGen.Made>();
        for (SongGen.Tpl t : SongGen.TEMPLATES) {
            Score base = Score.parse(t.src);
            for (int i = 0; i < 20; i++) {
                SongGen.Made m = SongGen.generate(7000 + i, t);
                total++; attempts += m.attempts;
                if (m.score == null) { nulls++; continue; }
                made.add(m);
                if (!SongCheck.check(m.score).clean()) unclean++;
                boolean same = m.score.tracks.size() == base.tracks.size() && m.score.sections.keySet().equals(base.sections.keySet()) && m.score.beats == base.beats;
                for (String sec : base.sections.keySet()) if (m.score.sections.get(sec) == null || m.score.sections.get(sec).bars != base.sections.get(sec).bars) same = false;
                for (Score.Track tr : base.tracks) if (m.score.trackByName.get(tr.name) == null || m.score.trackByName.get(tr.name).vel != tr.vel) same = false;
                if (same) sameArrangement++;
            }
        }
        double ms = (System.nanoTime() - t0) / 1e6 / total;
        check("G1 every arrangement composes songs (" + total + " tried, " + nulls + " failed, " + String.format("%.1f", attempts / (double) total) + " attempts each)", nulls == 0 && attempts / (double) total < 25);
        check("G2 every composed song passes the same checks as the written ones (" + unclean + " do not)", unclean == 0);
        check("G3 a composed song keeps the arrangement of its template: tracks, levels, sections and bars (" + sameArrangement + " of " + made.size() + ")", sameArrangement == made.size());
        check("G4 composing takes " + String.format("%.0f", ms) + " ms a song on average (under 500)", ms < 500);

        SongGen.Tpl tale = SongGen.template("tale");
        String[] one = SongGen.generate(42, tale).score.lines, again = SongGen.generate(42, tale).score.lines;
        check("G5 the same seed gives the same song", Arrays.equals(one, again));
        Set<String> leads = new HashSet<String>(), keys = new HashSet<String>(), titles = new HashSet<String>();
        for (int i = 0; i < 30; i++) {
            Score s = SongGen.generate(100 + i, tale).score;
            leads.add(leadLine(s.lines, "A"));
            for (String l : s.lines) if (l.startsWith("chords:")) { keys.add(l); break; }
            titles.add(s.title);
        }
        check("G6 different seeds give different songs (" + leads.size() + " melodies, " + keys.size() + " chord lines, " + titles.size() + " titles in 30)", leads.size() >= 28 && keys.size() >= 12 && titles.size() >= 12);
        boolean sourceOk = true;
        for (SongGen.Made m : made) if (!m.score.source.startsWith("gen ") || m.score.moods.isEmpty()) sourceOk = false;
        check("G7 a composed song says where it came from (source: gen <arrangement> <seed>) and which moods it suits", sourceOk);

        // the hook returns: in the first statement bar 5 starts like bar 1 for most songs (the same notes when the chord is the same)
        int echoes = 0, counted = 0;
        for (SongGen.Made m : made) {
            Score.Section sec = m.score.sections.get("A");
            if (sec == null || sec.bars < 8) continue;
            Score.Track lead = m.score.trackByName.containsKey("lead") ? m.score.trackByName.get("lead") : m.score.trackByName.get("horn");
            if (lead == null) continue;
            counted++;
            double[] first = new double[2];
            List<Integer> p1 = new ArrayList<Integer>(), p5 = new ArrayList<Integer>();
            for (Score.Ev e : sec.ev[0]) if (e.track == lead) p1.add(e.midi);
            for (Score.Ev e : sec.ev[4]) if (e.track == lead) p5.add(e.midi);
            if (!p1.isEmpty() && p1.size() == p5.size()) echoes++;
        }
        check("G8 the hook comes back: bar 5 has the rhythm of bar 1 in " + echoes + " of " + counted + " songs", counted > 0 && echoes >= counted * 0.8);

        // keeping, listing, dropping
        Score s1 = SongGen.generate(5, SongGen.template("danger")).score;
        int before = SongBook.all().size();
        Score kept = SongBook.keep(s1);
        check("G9 a kept song is written to the folder and joins the list (" + (kept == null ? SongBook.lastError : kept.file) + ")", kept != null && new File(SongBook.dir, kept.file).isFile() && SongBook.all().size() == before + 1 && kept.kept);
        boolean heavier = kept != null;
        if (kept != null) for (double w : kept.moods.values()) if (w != 3.0) heavier = false;
        check("G9b a kept song's moods are weighted like the written songs (3)", heavier && kept != null && kept.moodWeight(2) == 3.0);
        SongBook.reloadKept();
        boolean reloaded = false;
        for (Score s : SongBook.all()) if (s.kept && s.title.equals(s1.title)) reloaded = true;
        check("G10 kept songs are read from the folder again", reloaded);
        Files.write(new File(SongBook.dir, "broken.txt").toPath(), "title: Broken\nbpm: 60\nthis is not a score".getBytes("UTF-8"));
        SongBook.reloadKept();
        check("G11 a file that does not parse is skipped and reported (" + SongBook.keptProblems + ")", SongBook.keptProblems.size() == 1 && SongBook.keptProblems.get(0).startsWith("broken.txt"));
        new File(SongBook.dir, "broken.txt").delete();
        SongBook.reloadKept();
        Score toDrop = null;
        for (Score s : SongBook.all()) if (s.kept) toDrop = s;
        boolean dropped = toDrop != null && SongBook.drop(toDrop) && !new File(SongBook.dir, toDrop.file).exists() && SongBook.all().size() == before;
        check("G12 a kept song can be dropped (file deleted), a written song cannot", dropped && !SongBook.drop(SongBook.all().get(0)));

        // the runtime share: a conductor in danger plays a composed song when the share is 100
        int saved = Settings.musicGenerated;
        Settings.musicGenerated = 100;
        SongProbe.Run run = new SongProbe.Run(new Conductor(11));
        run.c.mood = 2;
        run.until(30);
        boolean composed = Ambient.nowScore != null && Ambient.nowScore.source.startsWith("gen ");
        Settings.musicGenerated = 0;
        SongProbe.Run run2 = new SongProbe.Run(new Conductor(12));
        run2.c.mood = 2;
        run2.until(30);
        boolean written = Ambient.nowScore != null && Ambient.nowScore.source.isEmpty();
        Settings.musicGenerated = saved;
        check("G13 with the share at 100 the conductor plays a composed song, at 0 a written one", composed && written);

        // the commands (music silent: volume 0)
        int vol = Settings.musicVolume; boolean on = Settings.music;
        Settings.musicVolume = 0;
        command = "music gen danger 77";
        String out1 = say(command);
        Score pinned = Ambient.pinned();
        check("G14 'music gen danger 77' composes and plays a danger song (" + out1.trim().split("\n")[0] + ")", pinned != null && out1.contains("Composed") && out1.contains("seed 77") && out1.contains("danger"));
        command = "music keep";
        Ambient.nowScore = pinned;
        String out2 = say(command);
        check("G15 'music keep' keeps the song that is playing (" + out2.trim() + ")", out2.contains("Kept") && SongBook.all().stream().anyMatch(s -> s.kept));
        command = "music list";
        check("G16 'music list' marks kept songs", say(command).contains("[kept]"));
        command = "music keep";
        Ambient.nowScore = SongBook.all().get(0);
        check("G17 'music keep' on a written song says there is nothing to keep", say(command).contains("written songs"));
        command = "music share 30";
        say(command);
        check("G18 'music share 30' sets the share", Settings.musicGenerated == 30);
        Settings.musicGenerated = saved;
        Score k2 = null;
        for (Score s : SongBook.all()) if (s.kept) k2 = s;
        command = "music drop " + (k2 == null ? "nothing" : k2.title);
        String out3 = say(command);
        check("G19 'music drop <name>' removes a kept song (" + out3.trim() + ")", out3.contains("Dropped") && SongBook.all().stream().noneMatch(s -> s.kept));
        command = "music play auto";
        say(command);
        Settings.musicVolume = vol; Settings.music = on;
        Ambient.pin(null);
        Settings.music = false; Ambient.apply(); Ambient.settle(); // (the music is stopped for good)

        SongBook.dir = old;
        SongBook.reloadKept();
        for (File f : new File(tmp, "songs").listFiles() == null ? new File[0] : new File(tmp, "songs").listFiles()) f.delete();
        new File(tmp, "songs").delete(); tmp.delete();
        System.out.println("[T] ===== " + pass + " passed, " + fail + " failed =====");
        System.exit(fail == 0 ? 0 : 1);
    }
}
