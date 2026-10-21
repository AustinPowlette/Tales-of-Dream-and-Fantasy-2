package Game;

import java.util.ArrayList;
import java.util.List;

/** The music of a tournament. While a tournament bout is on screen (the fight window of an ordinary tournament, or of the Garden's Variety Tournament, and its result window) the places' music gives way to a piece of its own:
 *  "Sand and Drums" (`SongArena`) or "The Platforms Fall" (`SongPlatforms`). The pieces are pinned with `Ambient.pin`, so they play whatever the mood, and the music that was playing (or the song the person pinned
 *  with `music play`) comes back two seconds after the last window closes. Windows ask with `hold` and let go with `release` (a count, so a bout that opens as the last one closes does not blink). Nothing happens when the
 *  music is off. The pieces are not in `SongBook`: the places never choose them, and `music list` does not show them. */
final class EventMusic {
    private EventMusic() { }

    static final int ARENA = 1, GARDEN = 2;
    private static Score arena, garden;
    static final List<String> problems = new ArrayList<>();
    private static int holds = 0, current = 0;
    private static Score saved = null;
    private static java.util.Timer timer = null;
    private static java.util.TimerTask pending = null;

    static synchronized Score score(int kind) {
        if (arena == null) {
            arena = Score.parse(SongArena.LINES);
            garden = Score.parse(SongPlatforms.LINES);
            for (String p : arena.problems) problems.add(arena.title + ": " + p);
            for (String p : garden.problems) problems.add(garden.title + ": " + p);
        }
        return kind == GARDEN ? garden : arena;
    }

    /** A tournament window opens: the piece plays (the Garden's piece wins over the ordinary one). */
    static synchronized void hold(int kind) {
        if (kind <= 0) return;
        if (pending != null) { pending.cancel(); pending = null; }
        boolean playing;
        try { playing = Ambient.isRunning(); } catch (RuntimeException e) { playing = false; }
        if (holds == 0 && current == 0) saved = Ambient.pinned();
        holds++;
        if (!playing) return;
        int want = Math.max(current, kind);
        if (want != current) {
            Score s = score(want);
            if (s != null && s.problems.isEmpty()) { current = want; Ambient.pin(s); }
        }
    }

    /** A tournament window closes. */
    static synchronized void release() {
        if (holds > 0) holds--;
        if (holds > 0) return;
        if (current == 0) return;
        if (timer == null) timer = new java.util.Timer("event-music", true);
        pending = new java.util.TimerTask() { @Override public void run() { restore(); } };
        timer.schedule(pending, 2000);
    }

    private static synchronized void restore() {
        pending = null;
        if (holds > 0 || current == 0) return;
        current = 0;
        Ambient.pin(saved);
        saved = null;
    }

    /** Tests: the pieces play now (they are parsed even when the music is off). */
    static int holds() { return holds; }
    static int playing() { return current; }
}
