package Game;

import java.util.PriorityQueue;
import java.util.Random;

/** Plays the songs of `SongBook`: picks a song for the mood (never the one just played when there is a choice), performs it a little differently each
 *  time (key, tempo, instruments, optional sections and tracks), brings its layers in and out with the mood bar by bar (the same song gets thinner at
 *  night and grows drums in danger), fades to another song when the place calls for one, rests between songs, and hands the engines one note at a
 *  time (`poll`) with a master level for the fades (`masterAt`). With no song at all the old generator (`Ambient.Composer`) plays instead. */
final class Conductor {
    private static final int GAP = 0, PLAY = 1, TAIL = 2, FADE = 3;
    private static final float SR = Ambient.SR;
    /** Mood -> target level of each role (lead counter harmony bass rhythm color perc drive). */
    static final double[][] MASK = {
        /* 0 open country */ {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0},
        /* 1 city         */ {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0},
        /* 2 danger       */ {0.0, 0.0, 1.0, 1.0, 0.5, 0.4, 1.0, 1.0},
        /* 3 night/winter */ {0.7, 0.0, 0.9, 0.6, 0.45, 1.0, 0.0, 0.0},
        /* 4 war          */ {0.6, 0.0, 1.0, 1.0, 1.0, 0.2, 1.0, 1.0},
        /* 5 sect         */ {0.85, 0.6, 1.0, 0.5, 0.6, 1.0, 0.4, 0.0},
        /* 6 the capital  */ {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0}};

    /** Struck and plucked instruments keep ringing after the written length, like a harp or a pedalled piano (seconds; 0 = as written). */
    private static final double[] RING = new double[Ambient.INST.length];
    static {
        RING[Ambient.PIANO] = 1.8; RING[Ambient.HARP] = 2.2; RING[Ambient.PLUCK] = 1.6; RING[Ambient.PIPA] = 1.0; RING[Ambient.BELL] = 3.0; RING[Ambient.CHIME] = 2.0;
        RING[Ambient.YANGQIN] = 1.4; RING[Ambient.MARIMBA] = 0.9; RING[Ambient.TIMPANI] = 1.2; RING[Ambient.UPRIGHT] = 0.35;
    }

    volatile int mood = 0;
    volatile boolean skip = false;
    /** Tests and previews: start at once, no fade-in. */
    boolean testMode = false;
    /** When set, only this song plays (the `music play` command and the preview tool). */
    volatile Score forced = null;
    volatile String forcedMode = null;

    /** A concert (`Concerts`): the piece, the voice of the musician's instrument and its grade; the piece cuts in at once, plays once with the instrument as its lead and the music of the places goes on after it. */
    private volatile Score concertPiece;
    private volatile int concertLead = -1, concertLevel = 5;
    private boolean concertPending = false, concertNow = false;

    void concert(Score s, int lead, int level) { concertLead = lead; concertLevel = level; concertPiece = s; }

    private final Random r;
    private Ambient.Composer legacy;
    private final PriorityQueue<Ambient.Note> queue = new PriorityQueue<Ambient.Note>(64, (a, b) -> Long.compare(a.at, b.at));
    private final double[] level = new double[Score.ROLES.length];
    private int state = GAP, barNo, wantBar = -1, seenMood = -1, beganMood = 0;
    private boolean fresh = true, cut = false;
    private Score score, last;
    private Score.Perf perf;
    private long songStart, gapUntil, nextBar, tailEnd, fadeStart, fadeEnd, fadeInStart, fadeInEnd, moodSince;
    private double beatS = SR;
    /** Plays the song `forced` once through and then stops (the preview tool): no more songs after it. */
    boolean once = false;
    private boolean finished = false;

    Conductor(long seed) { r = new Random(seed); }

    boolean finished() { return finished; }

    /** Every note of one whole performance of `sc` in the given mood, in time order (every optional part, first instruments): what the preview tool writes out. */
    static java.util.List<Ambient.Note> collect(Score sc, int moodValue, long seed) {
        Conductor c = new Conductor(seed);
        c.testMode = true; c.mood = moodValue; c.forced = sc; c.once = true; c.forcedMode = "full";
        java.util.List<Ambient.Note> out = new java.util.ArrayList<Ambient.Note>();
        long clock = 0;
        while (!c.finished() && clock < (long) (SR * 3600)) {
            for (Ambient.Note n = c.poll(clock); n != null; n = c.poll(clock)) out.add(n);
            clock += 256;
        }
        return out;
    }

    /** The title of the piece now playing ("" in a rest). */
    String title() { return state == GAP || score == null ? "" : score.title; }

    /** True once after the music was cut (the engines stop the ringing notes). */
    boolean takeCut() { boolean c = cut; cut = false; return c; }

    void skipPiece() { skip = true; }

    // ------------------------------------------------------------------ the engines' side
    Ambient.Note poll(long clock) {
        if (SongBook.all().isEmpty()) {
            if (legacy == null) legacy = new Ambient.Composer(r.nextLong());
            legacy.mood = mood;
            return legacy.poll(clock);
        }
        advance(clock);
        Ambient.Note n = queue.peek();
        if (n != null && n.at <= clock) return queue.poll();
        return null;
    }

    /** 0..1: the fade-in at the start of a song and the fade-out when another song takes over. */
    double masterAt(long clock) {
        double m = 1;
        if (state == PLAY || state == FADE || state == TAIL) {
            if (fadeInEnd > fadeInStart) m *= smooth((clock - fadeInStart) / (double) (fadeInEnd - fadeInStart));
            if (state == FADE) m *= 1 - smooth((clock - fadeStart) / (double) Math.max(1, fadeEnd - fadeStart));
        }
        return m;
    }

    private static double smooth(double x) { x = Math.max(0, Math.min(1, x)); return x * x * (3 - 2 * x); }

    // ------------------------------------------------------------------ the state machine
    private void advance(long clock) {
        if (mood != seenMood) { seenMood = mood; moodSince = clock; }
        if (fresh) { fresh = false; gapUntil = testMode ? clock : clock + (long) (SR * 1.2); }
        if (concertPiece != null && !concertPending && !concertNow) { // a concert begins: what plays is cut at once
            concertPending = true;
            if (state == PLAY || state == TAIL || state == FADE) { cut = true; queue.clear(); }
            state = GAP; gapUntil = clock;
        }
        if (skip) {
            skip = false;
            if (state == PLAY || state == TAIL) startFade(Math.max(clock, nextBar), 1.0);
            else if (state == GAP) gapUntil = clock;
        }
        switch (state) {
            case GAP:
                if (finished) break;
                if (clock >= gapUntil) begin(clock);
                break;
            case PLAY:
                while (state == PLAY && nextBar <= clock + (long) (SR * 0.3)) bar();
                break;
            case TAIL:
                if (clock >= tailEnd) rest(clock, false);
                break;
            default: // FADE
                if (clock >= fadeEnd) { cut = true; queue.clear(); rest(clock, true); }
        }
    }

    private void begin(long clock) {
        if (concertPending) { score = concertPiece; concertPiece = null; concertPending = false; concertNow = score != null; }
        else score = pick();
        if (score == null) { finished = true; return; }
        perf = score.perform(r);
        if (forcedMode != null) applyForcedMode();
        if (concertNow || score.concert) { // the musician's instrument leads, at his tempo, with the ornaments his grade allows (a concert piece on the playlist: its own voices, a middle grade)
            if (!concertNow) { concertLead = -1; concertLevel = 5; }
            perf.bpm *= Concerts.tempoFor(concertLevel);
            for (Score.Track tk : score.tracks) {
                if (!tk.name.startsWith("lead")) continue;
                if (concertLead >= 0) perf.inst[tk.index] = concertLead;
                perf.plays[tk.index] = tk.name.equals("lead") ? concertLevel < 5 : tk.name.equals("leadf") ? concertLevel >= 5 && concertLevel < 8 : concertLevel >= 8;
            }
        }
        barNo = 0; wantBar = -1; beganMood = mood;
        nextBar = testMode ? clock : clock + (long) (SR * 0.12); // the fade-in's volume reaches the synthesiser before the first note
        double[] target = targetLevels();
        for (int i = 0; i < level.length; i++) level[i] = target[i];
        songStart = nextBar;
        fadeInStart = clock; fadeInEnd = testMode ? clock : clock + (long) (SR * 4);
        state = PLAY;
        Ambient.nowPlaying = score.title;
        Ambient.nowScore = score;
    }

    /** `forcedMode`: "full" plays every optional section and track with the first instrument of each (for previews). */
    private void applyForcedMode() {
        if ("full".equals(forcedMode)) perf = score.full();
    }

    private void rest(long clock, boolean quick) {
        boolean wasConcert = concertNow;
        concertNow = false;
        if (!wasConcert) last = score;
        perf = null; state = GAP;
        Ambient.nowPlaying = "";
        Ambient.nowScore = null;
        if (once) { finished = true; return; }
        gapUntil = clock + (long) (SR * (wasConcert ? 6 : quick ? 1.2 : testMode ? 0 : forced != null ? 4 : gapSeconds()));
    }

    private double gapSeconds() {
        switch (mood) {
            case 2: case 4: return 6 + 14 * r.nextDouble();
            case 3: return 35 + 45 * r.nextDouble();
            case 1: return 15 + 25 * r.nextDouble();
            default: return 20 + 30 * r.nextDouble();
        }
    }

    private Score pick() {
        if (forced != null) return forced;
        int share = Settings.musicGenerated;
        if (share > 0 && !testMode && r.nextInt(100) < share) {
            SongGen.Made made = SongGen.generate(r.nextLong() & 0xffffffL, SongGen.templateFor(mood, r));
            if (made.score != null) return made.score;
        }
        java.util.List<Score> all = Playlist.candidates(); // the Music Manager's playlist, or every song
        double total = 0;
        double[] w = new double[all.size()];
        for (int pass = 0; pass < 2; pass++) {
            total = 0;
            for (int i = 0; i < w.length; i++) {
                Score s = all.get(i);
                w[i] = pass == 0 ? s.moodWeight(mood) : 1;
                if (s == last && all.size() > 1 && pass == 0 && countWith(mood) > 1) w[i] = 0;
                total += w[i];
            }
            if (total > 0) break;
        }
        if (total <= 0) return all.get(0);
        double x = r.nextDouble() * total;
        for (int i = 0; i < w.length; i++) { x -= w[i]; if (x <= 0 && w[i] > 0) return all.get(i); }
        return all.get(all.size() - 1);
    }

    private int countWith(int m) { int n = 0; for (Score s : Playlist.candidates()) if (s.moodWeight(m) > 0) n++; return n; }

    private boolean wantsOther() {
        double mine = score.moodWeight(mood), best = 0;
        for (Score s : Playlist.candidates()) best = Math.max(best, s.moodWeight(mood));
        return forced == null && best > 0 && mine * 2 < best;
    }

    private static final double[] ALL_ON = {1, 1, 1, 1, 1, 1, 1, 1};

    /** What each role should sound like now: the mood's mask, or everything for a song written for its own mood (`layers: fixed`). */
    private double[] targetLevels() { return score.fixedLayers ? ALL_ON : MASK[Math.max(0, Math.min(MASK.length - 1, mood))]; }

    /** 0: stay; 1: fade out over two bars at a section start; 2: danger and war call for their own music at once (one bar). */
    private int shouldSwitch(Score.PBar pb, long at) {
        if (concertNow) return 0; // a concert is played to its end
        if (mood == beganMood || !wantsOther()) { wantBar = -1; return 0; }
        if (wantBar < 0) wantBar = barNo;
        if (mood == 2 || mood == 4) return at - moodSince >= SR * 3 && at - songStart >= SR * 8 ? 2 : 0;
        if (at - moodSince < SR * 6 || barNo < 6) return 0;
        return pb.secStart || barNo - wantBar >= 8 ? 1 : 0;
    }

    private void startFade(long at, double bars) {
        fadeStart = at;
        fadeEnd = at + (long) (bars * (perf != null ? perf.score.beats : 4) * beatS);
        state = FADE;
    }

    /** Writes the notes of the next bar into the queue. */
    private void bar() {
        if (barNo >= perf.bars.size()) { state = TAIL; tailEnd = nextBar + (long) (SR * 4); return; }
        Score.PBar pb = perf.bars.get(barNo);
        int sw = shouldSwitch(pb, nextBar);
        if (sw > 0) { startFade(nextBar, sw == 2 ? 1.0 : 2.0); return; }
        double[] target = targetLevels();
        for (int i = 0; i < level.length; i++) {
            double d = target[i] - level[i];
            level[i] += Math.max(-0.34, Math.min(0.34, d));
        }
        int left = perf.bars.size() - barNo;
        double tempo = left == 1 ? 0.9 : left == 2 ? 0.95 : 1.0;
        beatS = SR * 60.0 / (perf.bpm * tempo);
        long t0 = nextBar;
        for (Score.Ev e : pb.sec.ev[pb.idx]) {
            Score.Track tk = e.track;
            if (!perf.plays[tk.index]) continue;
            double lv = level[tk.roleIdx];
            if (lv < 0.05) continue;
            int inst = e.inst >= 0 ? e.inst : perf.inst[tk.index];
            boolean drum = inst == Ambient.TAIKO || inst == Ambient.WOOD || inst == Ambient.KIT; // (their pitch is the drum, never transposed)
            double vel = 0.78 * tk.vel * e.vel * pb.dyn * Math.pow(lv, 0.7) * (0.94 + 0.12 * r.nextDouble());
            vel = Math.max(0.05, Math.min(1.0, vel));
            double swingBeats = perf.score.swing > 0 && Math.abs(e.beat - Math.floor(e.beat + 1e-6) - 0.5) < 1e-6 ? perf.score.swing : 0; // the second half of a beat comes late: the swing of a groove
            long at = t0 + (long) ((e.beat + swingBeats) * beatS) + (long) ((r.nextDouble() - 0.5) * (inst == Ambient.KIT ? 0.012 : 0.02) * SR);
            double secs = e.dur * 60.0 / (perf.bpm * tempo) * (e.art == 1 ? 0.45 : e.art == 2 ? 1.02 : 0.96);
            if (e.art != 1) secs = Math.max(secs, RING[inst]);
            int midi = e.midi + (drum ? 0 : perf.shift);
            Ambient.Note nn = new Ambient.Note(Math.max(0, at), inst, Math.max(12, Math.min(108, midi)), vel, Math.max(0.05, secs), tk.pan, 0);
            nn.track = tk.index;
            queue.add(nn);
        }
        barNo++;
        nextBar = t0 + (long) (perf.score.beats * beatS);
    }
}
