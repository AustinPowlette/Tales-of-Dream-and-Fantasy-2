package Game;

import java.util.ArrayList;
import java.util.List;

/** Checks a song's notes against its chords (the same checks `SongTool lint` prints and the song generator uses to throw bad attempts away):
 *  melody notes on long strong beats must be chord tones (or the ninth / sixth), no leap over a sixth inside a phrase, no semitone or minor-ninth clash
 *  between two tracks for three quarters of a beat, and the text must parse. Also some measures of the melody (stepwise motion, range). */
final class SongCheck {
    private SongCheck() { }

    static final class N { double start, end; int midi, track, role; String where; }

    static final class Result {
        int problems, nonChordTones, leaps, clashes;
        /** Share of melodic moves (lead and counter tracks) that are steps of three semitones or less (a step of the pentatonic scale), and the widest range of one lead track in semitones. */
        double stepwise = 1; int widestRange = 0, melodyNotes = 0;
        final List<String> notes = new ArrayList<String>(), clashList = new ArrayList<String>(), trackLines = new ArrayList<String>();
        boolean clean() { return problems == 0 && nonChordTones == 0 && leaps == 0 && clashes == 0; }
    }

    static String name(int midi) { return new String[]{"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"}[midi % 12] + (midi / 12 - 1); }

    static Result check(Score s) {
        Result res = new Result();
        res.problems = s.problems.size();
        Score.Perf p = s.full();
        List<N> all = new ArrayList<N>();
        for (int k = 0; k < p.bars.size(); k++) {
            Score.PBar pb = p.bars.get(k);
            for (Score.Ev e : pb.sec.ev[pb.idx]) {
                N n = new N();
                n.start = k * s.beats + e.beat; n.end = n.start + e.dur; n.midi = e.midi; n.track = e.track.index; n.role = e.track.roleIdx;
                n.where = "bar " + (k + 1) + " (" + pb.sec.name + " " + (pb.idx + 1) + ") beat " + String.format("%.2f", e.beat + 1) + " " + e.track.name + " " + name(e.midi);
                int inst = e.inst >= 0 ? e.inst : e.track.insts[0];
                if (inst == Ambient.TAIKO || inst == Ambient.WOOD || inst == Ambient.KIT || e.track.roleIdx == 6) continue; // (drums: the pitch is not a note of the tune)
                all.add(n);
            }
        }
        for (Score.Track t : s.tracks) {
            int cnt = 0, lo = 999, hi = -1; long sum = 0;
            for (N n : all) if (n.track == t.index) { cnt++; lo = Math.min(lo, n.midi); hi = Math.max(hi, n.midi); sum += n.midi; }
            res.trackLines.add(String.format("  %-8s %-8s %4d notes  %s..%s  avg %s", t.name, t.role, cnt, cnt == 0 ? "-" : name(lo), cnt == 0 ? "-" : name(hi), cnt == 0 ? "-" : name((int) (sum / cnt))));
        }
        int moves = 0, steps = 0;
        for (Score.Track t : s.tracks) {
            if (t.chordDriven() || (t.roleIdx != 0 && t.roleIdx != 1)) continue;
            List<N> line = new ArrayList<N>();
            for (N n : all) if (n.track == t.index) line.add(n);
            line.sort((x, y) -> Double.compare(x.start, y.start));
            int run = 1, lo = 999, hi = -1;
            for (int i = 0; i < line.size(); i++) {
                N n = line.get(i);
                lo = Math.min(lo, n.midi); hi = Math.max(hi, n.midi);
                res.melodyNotes++;
                int bar = (int) (n.start / s.beats);
                Score.PBar pb = p.bars.get(bar);
                Score.Chord ch = pb.sec.chordAt(pb.idx, n.start - bar * s.beats);
                boolean strong = Math.abs(n.start - Math.rint(n.start)) < 1e-6 && (n.end - n.start) >= 0.99;
                if (ch != null && strong) {
                    int pc = n.midi % 12;
                    boolean inChord = (ch.mask() & (1 << pc)) != 0, color = pc == (ch.root + 2) % 12 || pc == (ch.root + 9) % 12;
                    if (!inChord && !color) { res.nonChordTones++; if (res.notes.size() < 14) res.notes.add("not a chord tone on a strong beat: " + n.where + " over " + ch.sym); }
                }
                if (i > 0) {
                    N q = line.get(i - 1);
                    int iv = Math.abs(n.midi - q.midi);
                    if (Math.abs(n.start - q.end) < 0.3) {
                        moves++;
                        if (iv <= 3) steps++;
                        if (iv > 9) { res.leaps++; if (res.notes.size() < 14) res.notes.add("leap of " + iv + ": " + n.where); }
                    }
                    run = n.midi == q.midi ? run + 1 : 1;
                    if (run == 5) res.notes.add("five repeated notes: " + n.where);
                }
            }
            if (hi >= 0) res.widestRange = Math.max(res.widestRange, hi - lo);
        }
        res.stepwise = moves == 0 ? 1 : steps / (double) moves;
        all.sort((x, y) -> Double.compare(x.start, y.start));
        for (int i = 0; i < all.size(); i++) {
            N x = all.get(i);
            for (int j = i + 1; j < all.size() && all.get(j).start < x.end - 0.5; j++) {
                N y = all.get(j);
                if (x.track == y.track) continue;
                if (Math.min(x.end, y.end) - y.start < 0.75) continue;
                int d = Math.abs(x.midi - y.midi);
                if (d == 1 || d == 11 || d == 13) {
                    res.clashes++;
                    if (res.clashList.size() < 14) res.clashList.add(x.where + "  vs  " + y.where.substring(y.where.indexOf("beat")));
                }
            }
        }
        return res;
    }
}
