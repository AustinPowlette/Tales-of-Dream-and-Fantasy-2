package Game;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** The playlist of the Music Manager: which songs the places may choose from. Nothing chosen (the default) means every song of the game, the composed ones that were kept and, if you like, the pieces of the musicians'
 *  concerts; once you choose, only the chosen songs are played (a concert piece is then played as an ordinary song, with its own voices at a middle grade). The choice is kept by title in `Settings.musicPlaylist`. */
final class Playlist {
    private Playlist() { }

    static final String[] MOODS = {"open country", "city", "danger", "night", "war", "sect", "capital"};
    static final String GAME = "Game song", COMPOSED = "Composed (kept)", MUSICIAN = "Musician's concert";

    /** Every song a person can put on the list: the written and kept songs, then the concert pieces. */
    static List<Score> library() {
        List<Score> all = new ArrayList<>(SongBook.all());
        try { all.addAll(Concerts.all()); } catch (RuntimeException e) { }
        return all;
    }

    static String kind(Score s) { return s.concert ? MUSICIAN : s.kept ? COMPOSED : GAME; }

    static String moodNames(Score s) {
        if (s.concert) return "any";
        StringBuilder sb = new StringBuilder();
        for (java.util.Map.Entry<Integer, Double> e : s.moods.entrySet()) { if (e.getValue() <= 0) continue; int m = e.getKey(); sb.append(sb.length() > 0 ? ", " : "").append(m >= 0 && m < MOODS.length ? MOODS[m] : String.valueOf(m)); }
        return sb.length() == 0 ? "-" : sb.toString();
    }

    static boolean filtering() { return Settings.musicFilter; }

    /** True when the song may be played (every song when no list is made). */
    static boolean in(Score s) { return !filtering() || Settings.musicPlaylist.contains(s.title); }

    /** Puts a song on or takes it off the list. The first change starts a list of everything else, so unticking one song leaves all the others. */
    static synchronized void set(Score s, boolean on) {
        Set<String> list = new LinkedHashSet<>(Settings.musicPlaylist);
        if (!Settings.musicFilter) { list.clear(); for (Score x : SongBook.all()) list.add(x.title); Settings.musicFilter = true; }
        if (on) list.add(s.title); else list.remove(s.title);
        Settings.musicPlaylist = list;
        Settings.save();
    }

    static synchronized void clear() { Settings.musicPlaylist = new LinkedHashSet<>(); Settings.musicFilter = false; Settings.save(); }

    /** The songs the places choose from now: the list (the written, kept and concert songs on it), or every written and kept song. Never empty. */
    static List<Score> candidates() {
        List<Score> game = SongBook.all();
        if (!filtering()) return game;
        List<Score> out = new ArrayList<>();
        for (Score s : library()) if (Settings.musicPlaylist.contains(s.title)) out.add(s);
        return out.isEmpty() ? game : out;
    }
}
