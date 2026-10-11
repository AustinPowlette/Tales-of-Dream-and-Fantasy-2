package Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Composes new songs in the style of the written ones. A song is the arrangement of one of the seven written songs (its instruments, patterns, balance,
 *  sections and layers, all kept) with a new key, new chords and a new melody:
 *  chords come from a bank of eight-bar progressions (degrees of the scale, in major or minor); the melody is built the way the written ones are: a one-bar
 *  hook (a rhythm cell and a contour of steps), the same hook again on the next chord (a sequence), a contrast bar, a half cadence, the hook returning,
 *  a climbing bar and a cadence; the answer section keeps the hook and rewrites the rest; the middle section is a long-note line that rises to a peak; the
 *  last statement climbs in arpeggios. Notes on long strong beats are snapped to the chord, passing notes keep a semitone away from it. Every attempt goes
 *  through `SongCheck` (no stray notes, no clashes, no big leaps, mostly stepwise) and is thrown away if it fails; the same seed always gives the same song. */
final class SongGen {
    private SongGen() { }

    // ------------------------------------------------------------------ templates
    /** The arrangement of a written song: `mel` rows are {section, track, kind, centre pitch, octave shift}; kinds A A2 B A3 INTRO OUTRO CTR<firstBar>. */
    static final class Tpl {
        final String key; final String[] src; final boolean minor, a2Variant, busy; final int mood; final String[][] mel; final String[] bells, chimes;
        Tpl(String key, String[] src, boolean minor, int mood, boolean a2Variant, boolean busy, String[][] mel, String[] bells, String[] chimes) {
            this.key = key; this.src = src; this.minor = minor; this.mood = mood; this.a2Variant = a2Variant; this.busy = busy; this.mel = mel; this.bells = bells; this.chimes = chimes;
        }
    }

    static final List<Tpl> TEMPLATES = new ArrayList<Tpl>();
    static {
        TEMPLATES.add(new Tpl("tale", SongTale.LINES, false, 6, false, false, new String[][]{
            {"intro", "lead2", "INTRO", "74", "0"}, {"A", "lead", "A", "70", "0"}, {"A2", "lead2", "A2", "72", "0"}, {"A2", "flute", "CTR4", "82", "0"}, {"B", "lead2", "B", "74", "0"},
            {"A3", "lead", "A3", "70", "0"}, {"A3", "low", "A3", "70", "-1"}, {"A3", "lead3", "A3", "70", "0"}, {"outro", "lead", "OUTRO", "70", "0"}}, new String[]{"bell"}, new String[]{"chime"}));
        TEMPLATES.add(new Tpl("road", SongRoad.LINES, true, 0, true, false, new String[][]{
            {"intro", "lead", "INTRO", "70", "0"}, {"A", "lead", "A", "72", "0"}, {"A2", "lead2", "A2", "74", "0"}, {"A2", "cello", "CTR0", "55", "0"}, {"B", "lead2", "B", "74", "0"},
            {"B", "cello", "CTR0", "57", "0"}, {"A3", "lead", "A3", "72", "0"}, {"A3", "cello", "CTR0", "55", "0"}, {"outro", "lead", "OUTRO", "72", "0"}}, new String[]{"bell"}, new String[]{"chime"}));
        TEMPLATES.add(new Tpl("market", SongMarket.LINES, false, 1, false, true, new String[][]{
            {"A", "lead", "A", "68", "0"}, {"A2", "lead2", "A2", "70", "0"}, {"B", "lead2", "B", "72", "0"}, {"A3", "lead", "A", "68", "0"}, {"A3", "lead2", "A", "68", "-1"},
            {"outro", "lead", "OUTRO", "68", "0"}}, new String[]{"bell"}, new String[]{"chime"}));
        TEMPLATES.add(new Tpl("night", SongNight.LINES, true, 3, false, false, new String[][]{
            {"intro", "lead2", "INTRO", "74", "0"}, {"A", "lead", "A", "72", "0"}, {"B", "lead2", "B", "76", "0"}, {"A2", "lead2", "A", "74", "0"}, {"outro", "lead", "OUTRO", "72", "0"}},
            new String[]{"bell"}, new String[]{"chime"}));
        TEMPLATES.add(new Tpl("danger", SongDanger.LINES, true, 2, false, false, new String[][]{
            {"intro", "horn", "INTRO", "56", "0"}, {"A", "horn", "A", "57", "0"}, {"B", "erhu", "B", "78", "0"}, {"A2", "erhu", "CTR4", "80", "0"}, {"outro", "horn", "OUTRO", "57", "0"}},
            new String[]{"bell"}, new String[]{"chime"}));
        TEMPLATES.add(new Tpl("war", SongWar.LINES, true, 4, false, false, new String[][]{
            {"intro", "horn", "INTRO", "68", "0"}, {"A", "horn", "A", "70", "0"}, {"B", "erhu", "B", "80", "0"}, {"A2", "erhu", "CTR0", "82", "0"}, {"outro", "horn", "OUTRO", "70", "0"}},
            new String[]{"bell"}, new String[]{}));
        TEMPLATES.add(new Tpl("sect", SongSect.LINES, false, 5, false, false, new String[][]{
            {"A", "lead", "A", "72", "0"}, {"B", "lead2", "B", "76", "0"}, {"A2", "lead", "A", "72", "0"}, {"A2", "lead2", "A", "72", "-1"}, {"outro", "lead", "OUTRO", "72", "0"}},
            new String[]{"bell"}, new String[]{"chime"}));
    }

    static Tpl template(String key) { for (Tpl t : TEMPLATES) if (t.key.equalsIgnoreCase(key)) return t; return null; }

    /** Which arrangement suits a mood (random among the ones that do). */
    static Tpl templateFor(int mood, Random r) {
        String[][] pick = {{"road", "road", "road", "tale", "sect"}, {"market", "market", "market", "market", "road"}, {"danger"}, {"night", "night", "night", "night", "road"}, {"war"}, {"sect", "sect", "sect", "tale"}, {"tale", "tale", "tale", "market"}};
        String[] opts = pick[Math.max(0, Math.min(pick.length - 1, mood))];
        return template(opts[r.nextInt(opts.length)]);
    }

    // ------------------------------------------------------------------ banks
    private static final int[] MAJOR = {0, 2, 4, 5, 7, 9, 11}, AEOLIAN = {0, 2, 3, 5, 7, 8, 10}, DORIAN = {0, 2, 3, 5, 7, 9, 10};
    private static final String[] NAMES = {"C", "Db", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B"};

    private static final String[] MAJOR_A = {"1 6 4 5 1 6 4 5", "1 6 4 5 1 3 4 5", "1 5 6 3 4 1 4 5", "1 4 1 5 6 4 2 5", "1 3 4 5 1 3 4 2+5", "6 4 1 5 6 4 1 5", "1 5 6 4 1 5 4 5", "1 4 1 5 1 4 2 5"};
    private static final String[] MAJOR_B = {"6 5 4 1 6 5 4 5", "2 5 1 6 2 5 1 5", "4 1 5 6 4 1 5 5", "6 3 4 1 2 5 6 5", "3 6 2 5 3 6 4 5", "4 5 6 3 4 5 1 5"};
    private static final String[] MINOR_A = {"1 6 3 7 1 6 3 7", "1 7 6 7 1 7 6 5", "1 4 1 5 1 4 5 5", "1 3 4 5 1 3 6 5", "1 6 4 5 1 6 4 5", "1 4 7 3 6 4 7 5", "6 7 1 5 6 7 1 5", "1 3 6 7 1 3 4 5"};
    private static final String[] MINOR_B = {"4 1 7 3 4 1 5 5", "3 7 6 4 3 7 5 5", "6 3 7 1 4 1 5 5", "4 6 7 1 4 6 5 5", "6 7 1 3 4 6 7 5"};

    /** Rhythm cells (durations in beats, `r` before one = a rest) by meter: hook (the one-bar idea), busy (more notes), long, cadence, arpeggio. */
    private static String[] cells(double beats, String kind) {
        if (beats == 4) {
            switch (kind) {
                case "hook": return new String[]{"1.5 .5 2", "1.5 .5 1 1", "1 .5 .5 1 1", "1.5 .5 1 .5 .5", ".5 .5 1 1 .5 .5", "1 1 2", "2 1 1", ".5 .5 1 2", "1 .5 .5 2"};
                case "busy": return new String[]{".5 .5 1 1 .5 .5", ".5 .5 1 1.5 .5", "1 .5 .5 1 .5 .5", ".5 .5 .5 .5 1 1", "1 .5 .5 .5 .5 1"};
                case "long": return new String[]{"2 2", "3 1", "2 1 1", "1.5 1.5 1", "2 1.5 .5", "1 3"};
                case "cad": return new String[]{"3 r1", "2 2", "4", "3 1"};
                default: return new String[]{"1 1 1 1"};
            }
        }
        if (beats == 3) {
            switch (kind) {
                case "hook": case "busy": return new String[]{"1.5 .5 1", "2 1", "1 1 1", "1.5 1.5", ".5 .5 1 1", "1 .5 .5 1", "1.5 .5 .5 .5"};
                case "long": return new String[]{"3", "2 1", "1 2", "1.5 1.5"};
                case "cad": return new String[]{"3", "2 r1", "2 1"};
                default: return new String[]{"1 1 1"};
            }
        }
        switch (kind) { // 6/8: a beat is a dotted quarter
            case "hook": case "busy": return new String[]{"1 2/3 1/3", "2/3 1/3 1", "1 1", "1 1/3 1/3 1/3", "2/3 1/3 2/3 1/3"};
            case "long": return new String[]{"2", "1 1", "1 2/3 1/3"};
            case "cad": return new String[]{"2", "1 r1", "1 1"};
            default: return new String[]{"1/3 1/3 1/3 1/3 1/3 1/3"};
        }
    }

    /** Contours of a bar's notes as steps along the scale, by number of notes. */
    private static final int[][][] SHAPES = {
        {{}},
        {{}},
        {{1}, {2}, {-1}, {-2}, {1}},
        {{1, 1}, {2, -1}, {-1, 2}, {2, 1}, {-1, -1}, {3, -1}, {1, 2}, {-2, 1}},
        {{1, 1, -1}, {2, -1, -1}, {1, 2, -2}, {-1, 2, 1}, {1, -1, 2}, {2, -1, 1}, {-1, -1, 3}},
        {{1, 1, 1, -1}, {2, -1, 1, -1}, {1, -1, 1, 1}, {-1, 2, -1, 1}, {1, 1, -2, 1}},
        {{1, 1, -1, 2, -1}, {2, -1, 1, -1, -1}, {1, -1, 1, 1, -1}, {-1, 1, 1, 1, -2}}};

    // ------------------------------------------------------------------ pitch machinery
    private static final class Nt {
        final String d; final double dv; final int midi; // midi -1: a rest
        Nt(String d, double dv, int midi) { this.d = d; this.dv = dv; this.midi = midi; }
    }
    private static final class Bar { final List<Nt> notes = new ArrayList<Nt>(); }

    private static final class Ctx {
        final Random r; final int[] scale; final int tonic; final boolean pent, minor; final double beats; final boolean busy;
        final boolean[] passing = new boolean[12];
        Ctx(Random r, int[] scale, int tonic, boolean pent, boolean minor, double beats, boolean busy) {
            this.r = r; this.scale = scale; this.tonic = tonic; this.pent = pent; this.minor = minor; this.beats = beats; this.busy = busy;
            int[] penta = minor ? new int[]{0, 2, 3, 4, 6} : new int[]{0, 1, 2, 4, 5};
            if (pent) for (int i : penta) passing[(tonic + scale[i]) % 12] = true;
            else for (int s : scale) passing[(tonic + s) % 12] = true;
        }

        static int triMask(Score.Chord c) { return (1 << c.root) | (1 << ((c.root + c.third) % 12)) | (1 << ((c.root + c.fifth) % 12)); }

        /** The pitches a note over this chord may have, ascending, inside [lo, hi]: chord tones and scale tones that are not a semitone from one. */
        List<Integer> allowed(Score.Chord ch, int lo, int hi) {
            int mask = ch.mask();
            List<Integer> out = new ArrayList<Integer>();
            for (int m = lo; m <= hi; m++) {
                int pc = m % 12;
                boolean chordTone = (mask & (1 << pc)) != 0;
                boolean close = (mask & (1 << ((pc + 1) % 12))) != 0 || (mask & (1 << ((pc + 11) % 12))) != 0;
                if (chordTone || (passing[pc] && !close)) out.add(m);
            }
            return out;
        }

        int step(int from, int k, Score.Chord ch, int lo, int hi) {
            List<Integer> l = allowed(ch, lo, hi);
            if (l.isEmpty() || k == 0) return from;
            int idx = 0, best = 999;
            for (int i = 0; i < l.size(); i++) { int d = Math.abs(l.get(i) - from); if (d < best) { best = d; idx = i; } }
            int t = idx + k;
            if (t < 0 || t >= l.size()) t = idx - k;
            return l.get(Math.max(0, Math.min(l.size() - 1, t)));
        }

        int nearestTriad(int p, Score.Chord ch, int lo, int hi) {
            int mask = triMask(ch), best = p, bd = 999;
            for (int m = lo; m <= hi; m++) if ((mask & (1 << (m % 12))) != 0) { int d = Math.abs(m - p); if (d < bd || (d == bd && m < best)) { bd = d; best = m; } }
            return best;
        }

        /** The chord tone (root, third or fifth) nearest `p` that is a given tone: 0 root, 1 third, 2 fifth. */
        int toneNear(int p, Score.Chord ch, int which, int lo, int hi) {
            int pc = (ch.root + (which == 0 ? 0 : which == 1 ? ch.third : ch.fifth)) % 12, best = -1, bd = 999;
            for (int m = lo; m <= hi; m++) if (m % 12 == pc) { int d = Math.abs(m - p); if (d < bd) { bd = d; best = m; } }
            return best < 0 ? p : best;
        }
    }

    private static Score.Chord chordAt(Score.Chord[] bar, double pos) {
        Score.Chord found = bar[0];
        for (Score.Chord c : bar) if (c.start <= pos + 1e-9) found = c;
        return found;
    }

    private static int[] shapeFor(Random r, int n) {
        if (n <= 1) return new int[0];
        if (n > 6) { int[] s = new int[n - 1]; for (int i = 0; i < s.length; i++) s[i] = r.nextBoolean() ? 1 : -1; return s; }
        int[][] opts = SHAPES[n];
        return opts[r.nextInt(opts.length)].clone();
    }

    private static Bar genBar(Ctx c, Score.Chord[] chords, String cell, int first, int[] shape, int lo, int hi) {
        Bar b = new Bar();
        double pos = 0; int prev = -1, idx = 0;
        for (String tok : cell.split(" ")) {
            boolean rest = tok.startsWith("r");
            String ds = rest ? tok.substring(1) : tok;
            double dv = Score.parseDur(ds);
            if (rest) { b.notes.add(new Nt(ds, dv, -1)); pos += dv; continue; }
            Score.Chord ch = chordAt(chords, pos);
            int p;
            if (prev < 0) p = first;
            else p = c.step(prev, shape[Math.min(idx - 1, shape.length - 1)], ch, lo, hi);
            boolean strong = Math.abs(pos - Math.rint(pos)) < 1e-6 && dv >= 0.99;
            if (strong) {
                int q = c.nearestTriad(p, ch, lo, hi);
                if (prev >= 0 && q == prev) {
                    boolean up = idx - 1 < shape.length && shape[idx - 1] > 0;
                    int mask = Ctx.triMask(ch), alt = -1;
                    for (int m = prev + (up ? 1 : -1); m >= lo && m <= hi && Math.abs(m - prev) <= 6; m += up ? 1 : -1) if ((mask & (1 << (m % 12))) != 0) { alt = m; break; }
                    if (alt >= 0) q = alt;
                }
                p = q;
            }
            b.notes.add(new Nt(ds, dv, p));
            prev = p; pos += dv; idx++;
        }
        return b;
    }

    private static int pitched(String cell) { int n = 0; for (String t : cell.split(" ")) if (!t.startsWith("r")) n++; return n; }

    private static int lastPitch(Bar b) { for (int i = b.notes.size() - 1; i >= 0; i--) if (b.notes.get(i).midi >= 0) return b.notes.get(i).midi; return 60; }

    private static int firstPitch(Bar b) { for (Nt n : b.notes) if (n.midi >= 0) return n.midi; return 60; }

    // ------------------------------------------------------------------ the plan of one song
    private static final class Plan {
        Score.Chord[][] chordsA, chordsA2, chordsB;
        Score.Chord[][] intro, outro;
        final Map<String, List<Bar>> kinds = new HashMap<String, List<Bar>>();
    }

    private static String fmt(double d) {
        if (Math.abs(d - Math.rint(d)) < 1e-9) return String.valueOf((long) Math.rint(d));
        if (Math.abs(d * 2 - Math.rint(d * 2)) < 1e-9) { String s = String.valueOf(d); return s.startsWith("0.") ? s.substring(1) : s; }
        for (int den = 3; den <= 6; den += 3) for (int num = 1; num < den * 8; num++) if (Math.abs(d - num / (double) den) < 1e-9) return num + "/" + den;
        return String.valueOf(d);
    }

    private static String name(int midi) { return NAMES[((midi % 12) + 12) % 12] + (midi / 12 - 1); }

    private static String barText(Bar b, int octave) {
        StringBuilder sb = new StringBuilder();
        for (Nt n : b.notes) { if (sb.length() > 0) sb.append(' '); sb.append(n.midi < 0 ? "r" : name(n.midi + 12 * octave)).append(':').append(n.d); }
        return sb.toString();
    }

    private static String restBar(double beats) { return "r:" + fmt(beats); }

    private static Score.Chord[][] chordBars(String prog, Ctx c, boolean harmonicV, Random r, double beats) {
        String[] bars = prog.split(" ");
        Score.Chord[][] out = new Score.Chord[bars.length][];
        for (int i = 0; i < bars.length; i++) {
            String[] parts = bars[i].split("\\+");
            out[i] = new Score.Chord[parts.length];
            for (int k = 0; k < parts.length; k++) {
                String sym = symbol(Integer.parseInt(parts[k]), c, harmonicV, r);
                if (sym == null) return null;
                Score.Chord ch = Score.parseChord(sym);
                if (ch == null) return null;
                ch.start = k * beats / parts.length; ch.dur = beats / parts.length;
                out[i][k] = ch;
            }
        }
        return out;
    }

    private static String symbol(int deg, Ctx c, boolean harmonicV, Random r) {
        int i = deg - 1;
        int root = (c.tonic + c.scale[i]) % 12;
        int third = ((c.scale[(i + 2) % 7] - c.scale[i]) % 12 + 12) % 12, fifth = ((c.scale[(i + 4) % 7] - c.scale[i]) % 12 + 12) % 12;
        if (c.minor && deg == 5 && harmonicV) { third = 4; fifth = 7; }
        String n = NAMES[root];
        if (third == 4 && fifth == 7) {
            if (deg == 5 && r.nextInt(100) < 25) return n + "7";
            if ((deg == 1 || deg == 4) && r.nextInt(100) < 30) return n + "maj7";
            if (r.nextInt(100) < 12) return n + "add9";
            return n;
        }
        if (third == 3 && fifth == 7) return n + (r.nextInt(100) < 35 ? "m7" : "m");
        return null; // a diminished or odd chord: this progression does not fit the mode
    }

    private static Score.Chord[] flat(Score.Chord[][] bars) {
        List<Score.Chord> l = new ArrayList<Score.Chord>();
        for (Score.Chord[] b : bars) for (Score.Chord c : b) l.add(c);
        return l.toArray(new Score.Chord[0]);
    }

    private static String chordLine(Score.Chord[][] bars) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bars.length; i++) {
            if (i > 0) sb.append(" | ");
            for (int k = 0; k < bars[i].length; k++) { if (k > 0) sb.append(' '); sb.append(bars[i][k].sym); }
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ the melodies
    private static String pickCell(Random r, double beats, String kind, boolean busy) {
        String[] cs = cells(beats, kind.equals("hook") && busy ? "busy" : kind);
        return cs[r.nextInt(cs.length)];
    }

    /** One eight-bar statement. `hook` is bar 1's rhythm and shape, kept across the answer and the climax; `variant` rewrites bars 3, 4, 7 and 8. */
    private static List<Bar> statement(Ctx c, Score.Chord[][] chords, int center, String hookCell, int[] hookShape, List<Bar> reuse, String[] reuseFrom) {
        int lo = center - 9, hi = center + 11;
        Random r = c.r;
        double beats = c.beats;
        Bar[] b = new Bar[8];
        Score.Chord first = chords[0][0];
        int a1 = c.nearestTriad(center + r.nextInt(5) - 2, first, lo, hi);
        // a hook that begins on the fifth or third reads best: try those when near
        int t5 = c.toneNear(center, first, 2, lo, hi), t3 = c.toneNear(center, first, 1, lo, hi);
        if (reuse == null) a1 = r.nextBoolean() ? t5 : t3;
        if (reuse != null && sameChord(chords[0], reuseFrom, 0)) b[0] = reuse.get(0); else b[0] = genBar(c, chords[0], hookCell, a1, hookShape, lo, hi);
        // bar 2: the hook again on the next chord (a sequence)
        if (reuse != null && sameChord(chords[1], reuseFrom, 1)) b[1] = reuse.get(1);
        else {
            int root1 = chords[0][0].root, root2 = chords[1][0].root;
            int move = ((root2 - root1 + 18) % 12) - 6;
            int a2 = c.nearestTriad((firstPitch(b[0]) + move + lastPitch(b[0])) / 2, chords[1][0], lo, hi);
            b[1] = genBar(c, chords[1], hookCell, a2, hookShape, lo, hi);
            if (same(b[0], b[1])) b[1] = genBar(c, chords[1], hookCell, c.nearestTriad(a2 + 3, chords[1][0], lo, hi), hookShape, lo, hi);
        }
        // bar 3: a contrast, longer notes, the other way round
        {
            String cell = pickCell(r, beats, r.nextBoolean() ? "long" : "hook", false);
            int n = pitched(cell);
            int[] sh = shapeFor(r, n);
            if (hookShape.length > 0 && sh.length > 0 && Integer.signum(sh[0]) == Integer.signum(hookShape[0])) for (int i = 0; i < sh.length; i++) sh[i] = -sh[i];
            int anchor = c.nearestTriad(lastPitch(b[1]) + (r.nextBoolean() ? 2 : -2), chords[2][0], lo, hi);
            b[2] = genBar(c, chords[2], cell, anchor, sh, lo, hi);
        }
        // bar 4: the half cadence, an open chord tone held
        b[3] = cadenceBar(c, chords[3], lastPitch(b[2]), false, lo, hi, firstPitch(b[0]));
        // bar 5: the hook returns
        if (reuse != null && sameChord(chords[4], reuseFrom, 4)) b[4] = reuse.get(4);
        else if (sameChord(chords[4], chords, 0)) b[4] = b[0];
        else b[4] = genBar(c, chords[4], hookCell, c.nearestTriad(firstPitch(b[0]), chords[4][0], lo, hi), hookShape, lo, hi);
        // bar 6: the sequence again, its last step turned upward
        if (reuse != null && sameChord(chords[5], reuseFrom, 5)) b[5] = reuse.get(5);
        else {
            int[] sh2 = hookShape.clone();
            if (sh2.length > 0) sh2[sh2.length - 1] = sh2[sh2.length - 1] > 0 ? sh2[sh2.length - 1] : 1;
            int root5 = chords[4][0].root, root6 = chords[5][0].root;
            int move = ((root6 - root5 + 18) % 12) - 6;
            int a6 = c.nearestTriad((firstPitch(b[4]) + move + lastPitch(b[4])) / 2, chords[5][0], lo, hi);
            b[5] = genBar(c, chords[5], hookCell, a6, sh2, lo, hi);
            if (same(b[4], b[5])) b[5] = genBar(c, chords[5], hookCell, c.nearestTriad(a6 + 3, chords[5][0], lo, hi), sh2, lo, hi);
        }
        // bar 7: the climb, a rising line to the top of the song
        {
            String cell = pickCell(r, beats, c.busy ? "hook" : "hook", c.busy);
            int n = pitched(cell);
            int[] sh = new int[Math.max(0, n - 1)];
            for (int i = 0; i < sh.length; i++) sh[i] = i == sh.length - 1 && n > 3 && r.nextBoolean() ? -1 : 1;
            int anchor = c.nearestTriad(Math.min(hi - 4, lastPitch(b[5]) + 1), chords[6][0], lo, hi);
            b[6] = genBar(c, chords[6], cell, anchor, sh, lo, hi);
        }
        // bar 8: the cadence into the next statement, resting on the third of the last chord (the leading note when it is a dominant)
        b[7] = cadenceBar(c, chords[7], lastPitch(b[6]), true, lo, hi, firstPitch(b[0]));
        List<Bar> out = new ArrayList<Bar>();
        for (Bar x : b) out.add(x);
        return out;
    }

    /** True when two bars have the same pitches. */
    private static boolean same(Bar a, Bar b) {
        if (a.notes.size() != b.notes.size()) return false;
        for (int i = 0; i < a.notes.size(); i++) if (a.notes.get(i).midi != b.notes.get(i).midi) return false;
        return true;
    }

    private static boolean sameChord(Score.Chord[] bar, Score.Chord[][] other, int idx) {
        return other != null && idx < other.length && other[idx][0].sym.equals(bar[0].sym);
    }

    private static boolean sameChord(Score.Chord[] bar, String[] other, int idx) { return other != null && idx < other.length && other[idx].equals(bar[0].sym); }

    /** A bar that holds one chord tone (the third or the fifth of the last chord), chosen so the leap from `prev` and the leap to `target` (where the next bar starts) are both small. */
    private static Bar cadenceBar(Ctx c, Score.Chord[] chord, int prev, boolean closing, int lo, int hi, int target) {
        String cell = pickCell(c.r, c.beats, "cad", false);
        Score.Chord ch = chord[chord.length - 1];
        int best = -1, bestCost = 999;
        for (int which = 1; which <= 2; which++) for (int m = lo; m <= hi; m++) {
            int pc = (ch.root + (which == 1 ? ch.third : ch.fifth)) % 12;
            if (m % 12 != pc) continue;
            int cost = Math.max(Math.abs(m - prev), Math.abs(m - target)) * 10 + (closing && which == 2 ? 3 : 0) + c.r.nextInt(4);
            if (cost < bestCost) { bestCost = cost; best = m; }
        }
        int p = best < 0 ? c.toneNear(prev, ch, 1, lo, hi) : best;
        Bar bar = new Bar();
        boolean first = true;
        for (String tok : cell.split(" ")) {
            boolean rest = tok.startsWith("r");
            String ds = rest ? tok.substring(1) : tok;
            double dv = Score.parseDur(ds);
            if (rest) { bar.notes.add(new Nt(ds, dv, -1)); continue; }
            if (!first) {
                int prevP = p, q = -1;
                int mask = Ctx.triMask(ch);
                for (int m = prevP - 1; m >= prevP - 5 && m >= lo; m--) if ((mask & (1 << (m % 12))) != 0) { q = m; break; }
                if (q < 0) for (int m = prevP + 1; m <= prevP + 5 && m <= hi; m++) if ((mask & (1 << (m % 12))) != 0) { q = m; break; }
                if (q < 0) q = prevP;
                bar.notes.add(new Nt(ds, dv, q)); p = q; continue;
            }
            bar.notes.add(new Nt(ds, dv, p)); first = false;
        }
        return bar;
    }

    /** The answer: the hook and its sequence kept, the contrast, cadence and climb written again. */
    private static List<Bar> answer(Ctx c, Score.Chord[][] chords, int center, String hookCell, int[] hookShape, List<Bar> a, Score.Chord[][] chordsA) {
        int lo = center - 9, hi = center + 11;
        String[] syms = new String[chordsA.length];
        for (int i = 0; i < syms.length; i++) syms[i] = chordsA[i][0].sym;
        // bars 1, 2, 5 and 6 as in the statement where the chord is the same; the rest written again
        return statement(c, chords, center, hookCell, hookShape, a, syms);
    }

    /** The last statement: bars 1-4 as the first, then rising arpeggios (bars 5 and 7) around the sequence (bar 6), and a falling arpeggio to close (bar 8). */
    private static List<Bar> climax(Ctx c, Score.Chord[][] chords, int center, List<Bar> a) {
        int lo = center - 9, hi = center + 14;
        List<Bar> out = new ArrayList<Bar>();
        for (int i = 0; i < 4; i++) out.add(a.get(i));
        out.add(fitArp(c, chords[4], true, lastPitch(out.get(3)), firstPitch(a.get(5)), lo, hi));
        out.add(a.get(5));
        out.add(fitArp(c, chords[6], true, lastPitch(a.get(5)), lastPitch(a.get(5)) + 7, lo, hi));
        out.add(fitArp(c, chords[7], false, lastPitch(out.get(6)), firstPitch(a.get(0)), lo, hi));
        return out;
    }

    /** A bar of chord tones one after another, up or down, starting where the leap from `from` and the leap from the end to `to` are both smallest. */
    private static Bar fitArp(Ctx c, Score.Chord[] chord, boolean up, int from, int to, int lo, int hi) {
        String cell = cells(c.beats, "arp")[0];
        String[] toks = cell.split(" ");
        Bar best = null; int bestCost = 99999;
        for (int start = lo; start <= hi; start++) {
            if ((Ctx.triMask(chord[0]) & (1 << (start % 12))) == 0) continue;
            Bar b = new Bar();
            int p = start; boolean ok = true;
            for (int i = 0; i < toks.length; i++) {
                double dv = Score.parseDur(toks[i]);
                Score.Chord ch = chordAt(chord, i * dv);
                if (i > 0) {
                    boolean dir = toks.length > 4 && (i == 3) ? !up : up;
                    int mask = Ctx.triMask(ch), q = -1;
                    for (int m = p + (dir ? 1 : -1); m >= lo && m <= hi; m += dir ? 1 : -1) if ((mask & (1 << (m % 12))) != 0) { q = m; break; }
                    if (q < 0) { ok = false; break; }
                    p = q;
                }
                b.notes.add(new Nt(toks[i], dv, p));
            }
            if (!ok) continue;
            int cost = Math.max(Math.abs(start - from), Math.abs(p - to)) * 10 + c.r.nextInt(5);
            if (cost < bestCost) { bestCost = cost; best = b; }
        }
        if (best == null) return cadenceBar(c, chord, from, false, lo, hi, to);
        return best;
    }

    /** The middle section: long notes in a line that rises to a peak in bars 6-7 and settles on a chord tone of the last chord. */
    private static List<Bar> middle(Ctx c, Score.Chord[][] chords, int center, int hookStart) {
        int lo = center - 9, hi = center + 12;
        Random r = c.r;
        int[][] contours = {{0, 2, 0, 3, 5, 7, 9, 5}, {2, 0, 4, 2, 7, 5, 9, 7}, {0, 3, 2, 5, 4, 8, 10, 5}};
        int[] ct = contours[r.nextInt(contours.length)];
        List<Bar> out = new ArrayList<Bar>();
        for (int i = 0; i < 8; i++) {
            String cell = pickCell(r, c.beats, "long", false);
            int n = pitched(cell);
            int[] sh = shapeFor(r, n);
            int want = center - 3 + ct[i];
            int from = i == 0 ? want : lastPitch(out.get(i - 1));
            int anchor = c.nearestTriad(from + Math.max(-5, Math.min(5, want - from)), chords[i][0], lo, hi);
            if (i == 7) {
                Bar b = cadenceBar(c, chords[i], lastPitch(out.get(i - 1)), false, lo, hi, hookStart);
                out.add(b);
            } else out.add(genBar(c, chords[i], cell, anchor, sh, lo, hi));
        }
        return out;
    }

    private static List<Bar> intro(Ctx c, Score.Chord[][] chords, int center, int bars) {
        int lo = center - 9, hi = center + 11;
        List<Bar> out = new ArrayList<Bar>();
        for (int i = 0; i < bars; i++) {
            Bar b = new Bar();
            if (i < bars - 2) b.notes.add(new Nt(fmt(c.beats), c.beats, -1));
            else if (i == bars - 2) {
                int p1 = c.toneNear(center, chords[i][0], 2, lo, hi), p2 = c.toneNear(p1 + 3, chords[i][0], 0, lo, hi);
                b.notes.add(new Nt(fmt(c.beats / 2), c.beats / 2, p1)); b.notes.add(new Nt(fmt(c.beats / 2), c.beats / 2, p2));
            } else b.notes.add(new Nt(fmt(c.beats), c.beats, c.toneNear(out.get(i - 1).notes.get(1).midi, chords[i][0], 1, lo, hi)));
            out.add(b);
        }
        return out;
    }

    private static List<Bar> outro(Ctx c, Score.Chord[][] chords, int center, List<Bar> a, int bars) {
        int lo = center - 9, hi = center + 11;
        List<Bar> out = new ArrayList<Bar>();
        for (int i = 0; i < bars - 2; i++) out.add(a.get(i));
        Score.Chord[] pair = chords[bars - 2];
        int prev = out.isEmpty() ? center : lastPitch(out.get(out.size() - 1));
        Bar b = new Bar();
        int p1 = c.toneNear(prev, pair[0], 1, lo, hi);
        b.notes.add(new Nt(fmt(c.beats / 2), c.beats / 2, p1));
        int p2 = c.toneNear(p1, pair[pair.length - 1], 1, lo, hi);
        b.notes.add(new Nt(fmt(c.beats / 2), c.beats / 2, p2));
        out.add(b);
        Bar fin = new Bar();
        fin.notes.add(new Nt(fmt(c.beats), c.beats, c.toneNear(p2, chords[bars - 1][0], 0, lo, hi)));
        out.add(fin);
        return out;
    }

    private static List<Bar> counter(Ctx c, Score.Chord[][] chords, int center, int from) {
        int lo = center - 8, hi = center + 8;
        List<Bar> out = new ArrayList<Bar>();
        int prev = center;
        for (int i = 0; i < chords.length; i++) {
            Bar b = new Bar();
            if (i < from) { b.notes.add(new Nt(fmt(c.beats), c.beats, -1)); out.add(b); continue; }
            Score.Chord[] bar = chords[i];
            double each = c.beats / bar.length;
            for (Score.Chord ch : bar) {
                int p = c.nearestTriad(prev + (c.r.nextInt(3) - 1) * 2, ch, lo, hi);
                if (p == prev && c.r.nextBoolean()) p = c.step(prev, 1, ch, lo, hi);
                p = c.nearestTriad(p, ch, lo, hi);
                b.notes.add(new Nt(fmt(each), each, p)); prev = p;
            }
            out.add(b);
        }
        return out;
    }

    // ------------------------------------------------------------------ the song
    /** Builds the text of a song from a seed and an arrangement; null when this progression does not fit the mode. */
    static String[] build(long seed, Tpl t) {
        Random r = new Random(seed);
        Score ts = Score.parse(t.src);
        double beats = ts.beats;
        int tonic = r.nextInt(12);
        int[] scale = !t.minor ? MAJOR : r.nextInt(100) < 60 ? AEOLIAN : DORIAN;
        boolean pent = r.nextInt(100) < 65, harmonicV = r.nextInt(100) < 60;
        Ctx c = new Ctx(r, scale, tonic, pent, t.minor, beats, t.busy);
        String[] bankA = t.minor ? MINOR_A : MAJOR_A, bankB = t.minor ? MINOR_B : MAJOR_B;
        Plan p = new Plan();
        p.chordsA = chordBars(bankA[r.nextInt(bankA.length)], c, harmonicV, r, beats);
        p.chordsB = chordBars(bankB[r.nextInt(bankB.length)], c, harmonicV, r, beats);
        if (p.chordsA == null || p.chordsB == null) return null;
        p.chordsA2 = p.chordsA;
        if (t.a2Variant) {
            Score.Chord[][] v = new Score.Chord[8][];
            for (int i = 0; i < 8; i++) v[i] = p.chordsA[i];
            Score.Chord[][] iv = chordBars(t.minor ? "4" : "4", c, harmonicV, r, beats), dom = chordBars("5", c, harmonicV, r, beats);
            if (iv == null || dom == null) return null;
            v[5] = iv[0]; v[6] = dom[0]; v[7] = p.chordsA[7];
            p.chordsA2 = v;
        }
        Score.Chord[][] ton = chordBars("1", c, harmonicV, r, beats);
        if (ton == null) return null;
        Map<String, Integer> sectionBars = new HashMap<String, Integer>();
        for (Score.Section s : ts.sections.values()) sectionBars.put(s.name, s.bars);
        int introBars = sectionBars.containsKey("intro") ? sectionBars.get("intro") : 4, outroBars = sectionBars.containsKey("outro") ? sectionBars.get("outro") : 4;
        p.intro = new Score.Chord[introBars][];
        for (int i = 0; i < introBars; i++) p.intro[i] = p.chordsA[i];
        p.outro = new Score.Chord[outroBars][];
        {
            Score.Chord[][] dom = chordBars("5", c, harmonicV, r, beats), sub = chordBars("4", c, harmonicV, r, beats);
            if (dom == null || sub == null) return null;
            if (outroBars >= 4) {
                for (int i = 0; i < outroBars - 2; i++) p.outro[i] = p.chordsA[i];
                Score.Chord[] pair = new Score.Chord[2];
                pair[0] = copyChord(p.chordsA[outroBars - 2][0], 0, beats / 2); pair[1] = copyChord(dom[0][0], beats / 2, beats / 2);
                p.outro[outroBars - 2] = pair;
            } else {
                Score.Chord[] pair = new Score.Chord[2];
                pair[0] = copyChord(sub[0][0], 0, beats / 2); pair[1] = copyChord(dom[0][0], beats / 2, beats / 2);
                for (int i = 0; i < outroBars - 1; i++) p.outro[i] = pair;
            }
            p.outro[outroBars - 1] = new Score.Chord[]{copyChord(ton[0][0], 0, beats)};
        }
        // melodies
        String hookCell = pickCell(r, beats, "hook", t.busy);
        int[] hookShape = shapeFor(r, pitched(hookCell));
        int centerA = 0, centerB = 0, centerI = 0;
        for (String[] m : t.mel) {
            int cen = Integer.parseInt(m[3]);
            if (m[2].equals("A") && centerA == 0) centerA = cen;
            if (m[2].equals("B") && centerB == 0) centerB = cen;
            if (m[2].equals("INTRO") && centerI == 0) centerI = cen;
        }
        if (centerA == 0) centerA = 70;
        List<Bar> mA = statement(c, p.chordsA, centerA, hookCell, hookShape, null, null);
        p.kinds.put("A", mA);
        p.kinds.put("A2", answer(c, p.chordsA2, centerA + 2, hookCell, hookShape, mA, p.chordsA));
        p.kinds.put("A3", climax(c, p.chordsA, centerA, mA));
        if (centerB != 0) p.kinds.put("B", middle(c, p.chordsB, centerB, firstPitch(mA.get(0))));
        // text
        List<String> out = new ArrayList<String>();
        Set<String> explicit = new HashSet<String>();
        for (Score.Track tr : ts.tracks) if (!tr.chordDriven()) explicit.add(tr.name);
        Set<String> bellSet = new HashSet<String>(java.util.Arrays.asList(t.bells)), chimeSet = new HashSet<String>(java.util.Arrays.asList(t.chimes));
        String cur = null;
        for (String raw : t.src) {
            String l = raw.trim();
            if (l.isEmpty() || l.startsWith("//")) { out.add(raw); continue; }
            if (l.startsWith("[")) {
                String head = l.substring(1, l.indexOf(']')).trim();
                int lt = head.indexOf('<');
                cur = (lt < 0 ? head : head.substring(0, lt)).trim();
                out.add(raw);
                Score.Chord[][] ch = chordsFor(cur, p);
                if (ch != null) out.add("chords: " + chordLine(ch));
                continue;
            }
            int colon = l.indexOf(':');
            if (cur == null) {
                String key = colon < 0 ? "" : l.substring(0, colon).trim();
                if (key.equals("title")) { out.add("title: " + title(new Random(seed ^ 0x5bd1e995L), t.mood)); continue; }
                if (key.equals("bpm")) { double bpm = Double.parseDouble(l.substring(colon + 1).trim()); out.add("bpm: " + Math.round(bpm * (0.94 + 0.12 * r.nextDouble()))); continue; }
                if (key.equals("moods")) { out.add("moods: " + scaleMoods(l.substring(colon + 1).trim())); continue; }
                out.add(raw);
                continue;
            }
            if (colon < 0) { out.add(raw); continue; }
            String key = l.substring(0, colon).trim(), val = l.substring(colon + 1).trim();
            int par = key.indexOf('(');
            String track = par > 0 ? key.substring(0, par).trim() : key;
            if (track.equals("chords")) continue;
            if (!explicit.contains(track)) { out.add(raw); continue; }
            if (val.equals("-") || isRestsOnly(val)) { out.add(raw); continue; }
            String line = null;
            for (String[] m : t.mel) if (m[0].equals(cur) && m[1].equals(track)) { line = melodyLine(c, p, m, ts, cur, sectionBars.get(cur), t); break; }
            if (line == null && bellSet.contains(track)) line = bellLine(c, chordsFor(cur, p), cur, beats);
            if (line == null && chimeSet.contains(track)) line = chimeLine(c, chordsFor(cur, p), beats);
            out.add(key + ": " + (line == null ? "-" : line));
        }
        String[] lines = out.toArray(new String[0]);
        // the source of the song, for `music keep` and for finding it again
        List<String> withSource = new ArrayList<String>();
        for (String s : lines) { withSource.add(s); if (s.startsWith("title:")) withSource.add("source: gen " + t.key + " " + seed); }
        return withSource.toArray(new String[0]);
    }

    private static final String[][] TITLE_ADJ = {
        {"Distant", "Moonlit", "Quiet", "Jade", "Autumn", "Misty", "Wandering", "Drifting", "Frosted", "Golden", "Hidden", "Evening", "Windswept", "Green", "Silver", "Long"},
        {"Bright", "Crowded", "Red", "Lantern-Lit", "Noisy", "Festival", "Busy", "Sunlit", "Painted", "Merry", "Late", "Thousand-Voiced"},
        {"Black", "Cold", "Broken", "Hungry", "Hollow", "Silent", "Ashen", "Pale", "Sunless", "Restless", "Iron", "Bone-White"},
        {"Sleeping", "Snowbound", "Pale", "Frozen", "Moonless", "Falling", "Winter", "Still", "Soft", "Lonely", "Last", "Silver"},
        {"Broken", "Crimson", "Iron", "Burning", "Fallen", "Marching", "Thunderous", "Torn", "Ten Thousand", "Black", "Last", "Steel"},
        {"Morning", "Quiet", "Mountain", "Cloud-Capped", "Incense", "Bell-Tolled", "Ninefold", "Cedar", "Old", "Pure", "White", "Sacred"},
        {"Eternal", "First", "Imperial", "Radiant", "Ancient", "Golden", "Nine-Tiered", "Boundless", "Waking", "Great", "Jade", "Heavenly"}};
    private static final String[][] TITLE_NOUN = {
        {"Cranes", "Pine Wind", "River", "Bamboo", "Clouds", "Orchids", "Boats", "Rain", "Ferry", "Plum Road", "Mountain Pass", "Lotus Pond", "Fields", "Fireflies", "Path"},
        {"Lanterns", "Teahouse", "Rooftops", "Bridges", "Fireworks", "Lamps", "Drums", "Kites", "Gongs", "Paper Boats", "Gate", "Street", "Canal", "Festival"},
        {"Ridge", "Fog", "Pass", "Lair", "Hollow", "Thicket", "Cave", "Beasts", "Wolves", "Stairs", "Pit", "Road", "Marsh"},
        {"Lantern Road", "Plum Blossoms", "Moon", "Courtyard", "Pines", "Hearth", "Tea House", "Bridge", "Pond", "Stars", "Embers", "Window"},
        {"Banners", "Swords", "Drums", "Gate", "Vow", "Spears", "Army", "Horses", "Fires", "Oath", "Wall", "Dawn"},
        {"Bell", "Hall", "Garden", "Stair", "Incense", "Cloister", "Peak", "Pines", "Sword Garden", "Pilgrim", "Courtyard", "Lotus"},
        {"Roads", "Dawn", "Gates", "Towers", "Hall", "Pagoda", "Throne", "Sky", "Years", "Lamps", "Bells", "City"}};
    private static final String[] TITLE_PLACE = {"the Jade River", "the Nine Peaks", "the Old Gate", "the Cloud Sea", "the Sleeping Valley", "Heaven's Ladder", "the Lotus Pond", "the Mirror Lake", "the Western Pass", "the Bamboo Sea", "Originis", "the Last Village"};

    /** A name for a composed song: an adjective and a noun of its mood in one of six shapes (several hundred different names). */
    static String title(Random r, int mood) {
        int m = Math.max(0, Math.min(6, mood));
        String adj = TITLE_ADJ[m][r.nextInt(TITLE_ADJ[m].length)], noun = TITLE_NOUN[m][r.nextInt(TITLE_NOUN[m].length)], place = TITLE_PLACE[r.nextInt(TITLE_PLACE.length)];
        String adj2 = TITLE_ADJ[m][r.nextInt(TITLE_ADJ[m].length)];
        switch (r.nextInt(6)) {
            case 0: return adj + " " + noun;
            case 1: return noun + " over " + place;
            case 2: return "Song of the " + adj + " " + noun;
            case 3: return "The " + adj + " " + noun + " of " + place;
            case 4: return noun + " and " + adj2 + " " + TITLE_NOUN[m][r.nextInt(TITLE_NOUN[m].length)];
            default: return adj + " " + noun + " at " + (m == 3 ? "Midnight" : m == 4 ? "Daybreak" : "Dusk");
        }
    }

    private static Score.Chord copyChord(Score.Chord c, double start, double dur) {
        Score.Chord n = Score.parseChord(c.sym);
        n.start = start; n.dur = dur;
        return n;
    }

    private static boolean isRestsOnly(String v) {
        for (String bar : v.split("\\|")) for (String tok : bar.trim().split("\\s+")) if (!tok.startsWith("r")) return false;
        return true;
    }

    private static String scaleMoods(String moods) {
        StringBuilder sb = new StringBuilder();
        for (String part : moods.split("\\s+")) {
            String[] kv = part.split(":");
            double w = kv.length > 1 ? Double.parseDouble(kv[1]) : 1;
            if (sb.length() > 0) sb.append(' ');
            sb.append(kv[0]).append(':').append(Math.max(1, Math.round(w / 4)));
        }
        return sb.toString();
    }

    private static Score.Chord[][] chordsFor(String section, Plan p) {
        switch (section) {
            case "intro": return p.intro;
            case "outro": return p.outro;
            case "B": return p.chordsB;
            case "A2": return p.chordsA2;
            default: return p.chordsA;
        }
    }

    private static String melodyLine(Ctx c, Plan p, String[] m, Score ts, String section, int bars, Tpl t) {
        String kind = m[2];
        int center = Integer.parseInt(m[3]), oct = Integer.parseInt(m[4]);
        List<Bar> list;
        if (kind.startsWith("CTR")) list = counter(c, chordsFor(section, p), center, Integer.parseInt(kind.substring(3)));
        else if (kind.equals("INTRO")) list = intro(c, p.intro, center, bars);
        else if (kind.equals("OUTRO")) list = outro(c, p.outro, center, p.kinds.get("A"), bars);
        else list = p.kinds.get(kind);
        if (list == null) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(bars, list.size()); i++) { if (i > 0) sb.append(" | "); sb.append(barText(list.get(i), oct)); }
        return sb.toString();
    }

    private static String bellLine(Ctx c, Score.Chord[][] chords, String section, double beats) {
        int n = chords.length;
        List<Integer> strikes = new ArrayList<Integer>();
        if (section.equals("outro")) strikes.add(n - 1);
        else { strikes.add(0); if (n >= 8) strikes.add(4); }
        int last = strikes.get(strikes.size() - 1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= last; i++) {
            if (i > 0) sb.append(" | ");
            if (strikes.contains(i)) {
                Score.Chord ch = chords[i][0];
                int low = ch.near(ch.root, 45);
                sb.append('[').append(name(low)).append(' ').append(name(low + ch.fifth)).append("]:").append(fmt(beats));
            } else sb.append(restBar(beats));
        }
        return sb.toString();
    }

    private static String chimeLine(Ctx c, Score.Chord[][] chords, double beats) {
        int n = chords.length;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n - 1; i++) sb.append(restBar(beats)).append(" | ");
        Score.Chord ch = chords[n - 1][chords[n - 1].length - 1];
        int p = ch.near((ch.root + ch.third) % 12, 90);
        sb.append("r:").append(fmt(beats - 1)).append(' ').append(name(p)).append(":1");
        return sb.toString();
    }

    // ------------------------------------------------------------------ the front door
    /** Result of `generate`: the song, the number of attempts and the reasons attempts were thrown away. */
    static final class Made { Score score; int attempts; String why = ""; }

    static final Map<String, Integer> REJECTED = new HashMap<String, Integer>();

    /** Composes a song: the same (seed, arrangement) always gives the same song. Up to 80 attempts, each checked; null if none passes. */
    static Made generate(long seed, Tpl t) {
        Made made = new Made();
        for (int a = 0; a < 80; a++) {
            made.attempts = a + 1;
            String[] lines;
            try { lines = build(seed * 1000003L + a * 7919L + t.key.hashCode() * 104729L, t); } catch (RuntimeException e) { note("exception " + e.getClass().getSimpleName()); continue; }
            if (lines == null) { note("progression does not fit the mode"); continue; }
            Score s = Score.parse(lines);
            if (!s.problems.isEmpty()) { note("parse: " + s.problems.get(0)); continue; }
            SongCheck.Result res = SongCheck.check(s);
            if (res.nonChordTones > 0) { note("stray note"); continue; }
            if (res.clashes > 0) { note("clash"); continue; }
            if (res.leaps > 0) { String w = res.notes.isEmpty() ? "" : res.notes.get(0); int a1 = w.indexOf('('), a2 = w.indexOf(')'); note("leap " + (a1 >= 0 && a2 > a1 ? w.substring(a1, a2 + 1).replaceAll("[0-9]+", "#") + (w.contains("lead2") ? " lead2" : w.contains(" lead ") ? " lead" : "") : "")); continue; }
            if (res.stepwise < 0.5) { note("too jumpy"); continue; }
            if (res.widestRange > 28) { note("range"); continue; }
            s.source = "gen " + t.key + " " + seed;
            made.score = s;
            return made;
        }
        return made;
    }

    private static synchronized void note(String why) { Integer n = REJECTED.get(why); REJECTED.put(why, n == null ? 1 : n + 1); }
}
