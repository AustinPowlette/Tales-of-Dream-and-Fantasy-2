package Game;

import java.io.File;
import java.util.*;

/** 2026-10-17 (headless, no world needed): the Music Manager's playlist (`Playlist`). Nothing made = every song; a song can be taken off (the rest stay) or put on; once a list is made only its songs are chosen by the
 *  conductor, a concert piece on the list is played as a song (one lead version only), and the list is kept in the settings file. */
public class PlaylistProbe {
    static int passes = 0, fails = 0;
    static void check(String n, boolean ok) { if (ok) passes++; else fails++; System.out.println("[T] " + (ok ? "PASS " : "FAIL ") + n); }

    public static void main(String[] a) throws Exception {
        Settings.file = new File("playlist-test-settings.properties");
        SongBook.dir = new File("songs-none");
        List<Score> songs = SongBook.all(), lib = Playlist.library();
        int concerts = 0; for (Score s : lib) if (s.concert) concerts++;
        check("P1 the library holds every song of the game and the " + concerts + " concert pieces (" + songs.size() + " + " + concerts + " = " + lib.size() + ")", concerts == 12 && lib.size() == songs.size() + concerts && songs.size() >= 70);
        check("P1b the kinds: a written song is a Game song, a piece for a Musician is a Musician's concert (" + Playlist.kind(songs.get(0)) + ", " + Playlist.kind(lib.get(lib.size() - 1)) + ")", Playlist.kind(songs.get(0)).equals(Playlist.GAME) && Playlist.kind(lib.get(lib.size() - 1)).equals(Playlist.MUSICIAN));
        Settings.musicPlaylist = new LinkedHashSet<>(); Settings.musicFilter = false;
        check("P2 with no list every song is on it and the conductor chooses from every song", !Playlist.filtering() && Playlist.in(songs.get(3)) && Playlist.candidates().size() == songs.size());
        Playlist.set(songs.get(3), false);
        check("P3 taking one song off leaves all the others on (" + Settings.musicPlaylist.size() + " of " + songs.size() + ")", Playlist.filtering() && !Playlist.in(songs.get(3)) && Playlist.in(songs.get(4)) && Settings.musicPlaylist.size() == songs.size() - 1 && Playlist.candidates().size() == songs.size() - 1);
        Playlist.clear();
        check("P3b 'Every song' clears the list", !Playlist.filtering() && Playlist.candidates().size() == songs.size());

        // only three songs
        Settings.musicPlaylist = new LinkedHashSet<>(); Settings.musicFilter = false;
        for (Score s : lib) Playlist.set(s, false);
        check("P3c unticking everything leaves a list that is empty but in use: the conductor still has every song (no silence): " + Playlist.candidates().size(), Playlist.filtering() && Playlist.candidates().size() == songs.size());
        Score s1 = songs.get(0), s2 = songs.get(10), s3 = songs.get(20);
        Playlist.set(s1, true); Playlist.set(s2, true); Playlist.set(s3, true);
        check("P4 a list of three songs: the conductor may choose only those (" + Playlist.candidates().size() + ")", Playlist.candidates().size() == 3 && Playlist.in(s1) && !Playlist.in(songs.get(1)));
        Set<String> heard = new LinkedHashSet<>();
        for (int seed = 1; seed <= 12; seed++) heard.addAll(titles(seed, 3 * 60));
        check("P4b played for a long while the conductor never leaves the list (heard " + heard + ")", !heard.isEmpty() && new HashSet<>(Arrays.asList(s1.title, s2.title, s3.title)).containsAll(heard));
        check("P4c ...and plays more than one of them (" + heard.size() + ")", heard.size() >= 2);

        // a concert piece on the list
        Settings.musicPlaylist = new LinkedHashSet<>(); Settings.musicFilter = true;
        Score piece = null; for (Score s : lib) if (s.concert) { piece = s; break; }
        Settings.musicPlaylist.add(piece.title);
        check("P5 a list that holds only a concert piece chooses it (" + Playlist.candidates().size() + ")", Playlist.candidates().size() == 1 && Playlist.candidates().get(0) == piece);
        Conductor c = new Conductor(5); c.testMode = true; c.mood = 1;
        Set<String> leads = new TreeSet<>(); Set<String> seen = new LinkedHashSet<>();
        long clock = 0;
        while (clock < 44100L * 150) {
            for (Ambient.Note n = c.poll(clock); n != null; n = c.poll(clock)) { if (n.track >= 0 && n.track < Playlist.candidates().get(0).tracks.size() && c.title().equals(piece.title)) { String tn = piece.tracks.get(n.track).name; if (tn.startsWith("lead")) leads.add(tn); } seen.add(c.title()); }
            clock += 256;
        }
        check("P5b it is played as a song with ONE lead version (" + leads + ") and nothing else is heard (" + seen + ")", leads.size() == 1 && seen.size() <= 2);

        // kept in the settings file
        Settings.musicPlaylist = new LinkedHashSet<>(Arrays.asList(s1.title, "A Song, with a comma", s3.title)); Settings.musicFilter = true;
        Settings.save();
        Settings.musicPlaylist = new LinkedHashSet<>(); Settings.musicFilter = false;
        Settings.load();
        check("P6 the list survives the settings file, titles with commas too (" + Settings.musicPlaylist + ")", Settings.musicFilter && Settings.musicPlaylist.size() == 3 && Settings.musicPlaylist.contains("A Song, with a comma") && Settings.musicPlaylist.contains(s1.title));
        new File("playlist-test-settings.properties").delete();
        System.out.println("[T] ===== " + passes + " passed, " + fails + " failed =====");
        System.exit(0);
    }

    static Set<String> titles(long seed, int seconds) {
        Conductor c = new Conductor(seed); c.testMode = true; c.mood = (int) (seed % 4);
        Set<String> out = new LinkedHashSet<>();
        long clock = 0;
        while (clock < 44100L * seconds) { for (Ambient.Note n = c.poll(clock); n != null; n = c.poll(clock)) { String t = c.title(); if (!t.isEmpty()) out.add(t); } clock += 256; }
        return out;
    }
}
