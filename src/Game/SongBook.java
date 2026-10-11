package Game;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** The songs of the game: the written ones (each a `String[]` text score in its own class) and the kept songs of the `songs/` folder (one text score per
 *  file: composed songs the player kept with `music keep`, or songs written by hand in the same format, see `Game Info.md`). A song with a problem is left out and
 *  listed in `problems` (the probes check that the list is empty for the written ones). */
final class SongBook {
    private SongBook() { }
    private static volatile List<Score> songs;
    static final List<String> problems = new ArrayList<String>();
    /** The folder of kept songs (tests point it somewhere else). */
    static volatile File dir = new File("songs");

    static synchronized List<Score> all() {
        if (songs == null) {
            List<Score> l = new CopyOnWriteArrayList<Score>();
            String[][] sources = {SongTale.LINES, SongRoad.LINES, SongMarket.LINES, SongNight.LINES, SongDanger.LINES, SongWar.LINES, SongSect.LINES,
                SongReeds.LINES, SongTeahouse.LINES, SongMoon.LINES, SongLamps.LINES,
                SongDeep.LINES, SongGate.LINES, SongIncense.LINES,
                SongBarley.LINES, SongCranes.LINES, SongPass.LINES,
                SongFestival.LINES, SongMoneychangers.LINES, SongRain.LINES,
                SongCave.LINES, SongRavine.LINES, SongSleeper.LINES,
                SongFireflies.LINES, SongWatchman.LINES, SongSalt.LINES,
                SongTide.LINES, SongEastGate.LINES, SongCharge.LINES,
                SongBamboo.LINES, SongDawn.LINES, SongScripture.LINES,
                SongGates.LINES, SongSeat.LINES, SongNoon.LINES,
                SongCaravan.LINES, SongKites.LINES, SongNightMarket.LINES, SongGarden.LINES, SongBones.LINES, SongHollowDrums.LINES, SongLamplight.LINES,
                SongSlowMoon.LINES, SongIronRain.LINES, SongBanners.LINES, SongSutra.LINES, SongSwordDawn.LINES, SongBanquet.LINES, SongSilentCourt.LINES, SongSilkRoad.LINES, SongSteppe.LINES, SongTerraces.LINES, SongTowPath.LINES, SongSevenRoads.LINES, SongWindSleeps.LINES, SongLoneRider.LINES, SongReel.LINES, SongCourier.LINES, SongAbacus.LINES, SongLullaby.LINES, SongFugue.LINES, SongLion.LINES, SongCrane.LINES, SongFloor.LINES, SongFootsteps.LINES, SongLanternOut.LINES, SongLoom.LINES, SongNineGongs.LINES, SongElders.LINES, SongAmbassadors.LINES};
            for (String[] src : sources) {
                Score s = Score.parse(src);
                for (String p : s.problems) problems.add(s.title + ": " + p);
                if (s.problems.isEmpty()) l.add(s);
            }
            songs = l;
            reloadKept();
        }
        return songs;
    }

    /** Reads the kept songs of the folder again (a file that does not parse is skipped and noted in `keptProblems`). */
    static synchronized void reloadKept() {
        List<Score> l = all0();
        for (int i = l.size() - 1; i >= 0; i--) if (l.get(i).kept) l.remove(i);
        keptProblems.clear();
        File[] fs = dir.listFiles();
        if (fs == null) return;
        java.util.Arrays.sort(fs);
        for (File f : fs) {
            if (!f.getName().toLowerCase().endsWith(".txt") || f.getName().equalsIgnoreCase("README.txt")) continue;
            try {
                List<String> lines = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
                Score s = Score.parse(lines.toArray(new String[0]));
                if (!s.problems.isEmpty() || s.moods.isEmpty()) { keptProblems.add(f.getName() + ": " + (s.problems.isEmpty() ? "no moods line" : s.problems.get(0))); continue; }
                s.kept = true; s.file = f.getName();
                l.add(s);
            } catch (Exception e) { keptProblems.add(f.getName() + ": " + e.getMessage()); }
        }
    }

    static final List<String> keptProblems = new ArrayList<String>();

    private static List<Score> all0() { return songs; }

    /** A song by number (1-based) or by part of its title, or null. */
    static Score find(String key) {
        List<Score> all = all();
        try { int n = Integer.parseInt(key.trim()); if (n >= 1 && n <= all.size()) return all.get(n - 1); } catch (NumberFormatException ignored) { }
        for (Score s : all) if (s.title.toLowerCase().contains(key.trim().toLowerCase())) return s;
        return null;
    }

    /** Saves a composed song to the folder as a kept song (its moods weighted 3 so it plays as often as a written one) and adds it to the list. Returns it, or null on an error (`lastError`). */
    static synchronized Score keep(Score s) {
        try {
            if (s.lines == null) { lastError = "this song has no text to save"; return null; }
            all();
            dir.mkdirs();
            List<String> out = new ArrayList<String>();
            for (String l : s.lines) {
                if (l.trim().startsWith("moods:")) {
                    StringBuilder sb = new StringBuilder("moods:");
                    for (java.util.Map.Entry<Integer, Double> e : s.moods.entrySet()) sb.append(' ').append(e.getKey()).append(":3");
                    out.add(sb.toString());
                } else out.add(l);
            }
            String slug = s.title.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
            if (slug.isEmpty()) slug = "song";
            File f = new File(dir, slug + ".txt");
            for (int i = 2; f.exists(); i++) f = new File(dir, slug + "-" + i + ".txt");
            Files.write(f.toPath(), String.join("\n", out).getBytes(StandardCharsets.UTF_8));
            Score k = Score.parse(out.toArray(new String[0]));
            k.kept = true; k.file = f.getName();
            if (!k.problems.isEmpty()) { lastError = k.problems.get(0); f.delete(); return null; }
            songs.add(k);
            return k;
        } catch (Exception e) { lastError = e.getMessage(); return null; }
    }

    /** Removes a kept song (its file is deleted). False for a written song. */
    static synchronized boolean drop(Score s) {
        if (!s.kept || s.file == null) return false;
        File f = new File(dir, s.file);
        songs.remove(s);
        return !f.exists() || f.delete();
    }

    static volatile String lastError = "";
}
