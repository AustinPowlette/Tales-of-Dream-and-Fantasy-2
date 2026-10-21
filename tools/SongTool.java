package Game;

import javax.imageio.ImageIO;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Checks, pictures and recordings of the songs in `SongBook`, with the same notes the game plays (Conductor.collect).
 *  Compile with the sources, run from the repo root (the SoundFont is read from soundfonts/; the WAV with a SoundFont needs
 *  --add-exports java.desktop/com.sun.media.sound=ALL-UNNAMED on Java 9+):
 *    lint [song]                  problems of the text, melody against chords, harsh clashes, leaps, ranges
 *    info                         the songs with their sections and lengths
 *    roll <song> <png> [mood]     a piano roll (one colour per instrument)
 *    midi <song> <mid> [mood]     a Standard MIDI File
 *    wav <song> <wav> [mood] [builtin|font]   a recording (default: with the first SoundFont); prints peak and loudness
 *    similar [limit]              the key, metre and tempo of every song and the pairs that sound most alike (instruments, rhythm, melodic steps, key colour)
 *    all <folder>                 lint + roll + midi + wav (mood 0, SoundFont) of every song */
public class SongTool {
    public static void main(String[] a) throws Exception {
        String cmd = a.length > 0 ? a[0] : "info";
        SongBook.all(); // (loads the songs, so that the problems of the ones left out are known)
        for (String p : SongBook.problems) System.out.println("PROBLEM " + p);
        switch (cmd) {
            case "info": info(); break;
            case "concerts": for (Score s : Concerts.all()) lint(s, true); break; // lint of the Musicians' concert pieces
            case "lint": for (Score s : pick(a.length > 1 ? a[1] : null)) lint(s, true); break;
            case "roll": roll(one(a[1]), new File(a[2]), a.length > 3 ? Integer.parseInt(a[3]) : 0); break;
            case "midi": midi(one(a[1]), new File(a[2]), a.length > 3 ? Integer.parseInt(a[3]) : 0); break;
            case "wav": wav(one(a[1]), new File(a[2]), a.length > 3 ? Integer.parseInt(a[3]) : 0, a.length > 4 ? a[4] : "font"); break;
            case "similar": similar(a.length > 1 ? Double.parseDouble(a[1]) : 0.9); break;
            case "balance": balance(one(a[1]), a.length > 2 ? Integer.parseInt(a[2]) : 0); break;
            case "trim": trim(); break;
            case "dump": dump(one(a[1]), a.length > 2 ? Integer.parseInt(a[2]) : 1, a.length > 3 ? Integer.parseInt(a[3]) : 9999); break;
            case "gen": gen(a.length > 1 ? a[1] : "all", a.length > 2 ? Integer.parseInt(a[2]) : 5, a.length > 3 ? Long.parseLong(a[3]) : 1); break;
            case "genfiles": {
                SongGen.Tpl t = SongGen.template(a[1]);
                SongGen.Made m = SongGen.generate(Long.parseLong(a[2]), t);
                if (m.score == null) { System.out.println("no song passed in " + m.attempts + " attempts"); break; }
                String base = a[3];
                new File(base).getParentFile().mkdirs();
                roll(m.score, new File(base + ".png"), 0);
                midi(m.score, new File(base + ".mid"), 0);
                if (a.length > 4 && a[4].equals("wav")) wav(m.score, new File(base + ".wav"), 0, "font");
                java.nio.file.Files.write(java.nio.file.Paths.get(base + ".txt"), String.join("\n", m.score.lines).getBytes("UTF-8"));
                break;
            }
            case "all": {
                File dir = new File(a[1]);
                dir.mkdirs();
                int n = 1;
                for (Score s : SongBook.all()) {
                    String base = String.format("%02d-%s", n++, s.title.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", ""));
                    lint(s, true);
                    roll(s, new File(dir, base + ".png"), 0);
                    midi(s, new File(dir, base + ".mid"), 0);
                    wav(s, new File(dir, base + ".wav"), 0, "font");
                }
                break;
            }
            default: System.out.println("commands: info | lint [song] | roll song png [mood] | midi song mid [mood] | wav song wav [mood] [builtin|font] | all folder");
        }
    }

    /** Composes `count` songs from seed `seed0` on and prints what came out (the numbers the generator itself checks). */
    static void gen(String tplKey, int count, long seed0) {
        int made = 0, tries = 0;
        for (SongGen.Tpl t : SongGen.TEMPLATES) {
            if (!tplKey.equals("all") && !t.key.equals(tplKey)) continue;
            for (int i = 0; i < count; i++) {
                long seed = seed0 + i;
                long t0 = System.nanoTime();
                SongGen.Made m = SongGen.generate(seed, t);
                tries += m.attempts;
                if (m.score == null) { System.out.printf("%-7s %6d  no song passed in %d attempts%n", t.key, seed, m.attempts); continue; }
                made++;
                SongCheck.Result r = SongCheck.check(m.score);
                System.out.printf("%-7s %6d  %-34s %d attempts, %s, %.0f%% steps, widest %d, %d ms%n", t.key, seed, m.score.title, m.attempts, clock(m.score.seconds()), r.stepwise * 100, r.widestRange, (System.nanoTime() - t0) / 1000000);
            }
        }
        System.out.println(made + " songs, " + tries + " attempts; thrown away because: " + SongGen.REJECTED);
    }

    static Score one(String key) { Score s = SongBook.find(key); if (s == null) s = Concerts.find(key); if (s == null) throw new IllegalArgumentException("no song " + key); return s; }

    static List<Score> pick(String key) { List<Score> l = new ArrayList<Score>(); if (key == null) l.addAll(SongBook.all()); else l.add(one(key)); return l; }

    static String clock(double sec) { int s = (int) Math.round(sec); return (s / 60) + ":" + (s % 60 < 10 ? "0" : "") + (s % 60); }

    static String name(int midi) { return new String[]{"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"}[midi % 12] + (midi / 12 - 1); }

    // ------------------------------------------------------------------ info
    static void info() {
        int n = 1;
        for (Score s : SongBook.all()) {
            Score.Perf p = s.full();
            System.out.printf("%d. %s  (%s, %.0f bpm, %d bars, %s with every optional part)  moods %s%n", n++, s.title, s.beats == 4 ? "4/4" : s.beats + " beats", s.bpm, p.bars.size(), clock(p.bars.size() * s.beats * 60.0 / s.bpm), s.moods);
            double t = 0;
            StringBuilder sb = new StringBuilder("   ");
            int start = 0;
            for (int i = 0; i < p.bars.size(); i++) {
                if (p.bars.get(i).secStart) { sb.append(p.bars.get(i).sec.name).append(' ').append(clock(i * s.beats * 60.0 / s.bpm)).append("   "); }
            }
            System.out.println(sb);
        }
    }

    // ------------------------------------------------------------------ lint
    static final class N { double start, end; int midi, track, role; String where; }

    /** Prints the checks (when `print`) and returns {problems of the text, non-chord tones on long strong beats, leaps over a sixth, clashes}. */
    static int[] lint(Score s, boolean print) {
        java.io.PrintStream real = System.out;
        if (!print) System.setOut(new java.io.PrintStream(new java.io.OutputStream() { public void write(int b) { } }));
        try { return lint(s); } finally { System.setOut(real); }
    }

    private static int[] lint(Score s) {
        System.out.println("=== " + s.title);
        SongCheck.Result r = SongCheck.check(s);
        for (String l : r.trackLines) System.out.println(l);
        System.out.println("  melody: " + r.nonChordTones + " non-chord tones on long strong beats, " + r.leaps + " leaps over a sixth, " + String.format("%.0f", r.stepwise * 100) + "% of moves are steps, widest line " + r.widestRange + " semitones");
        for (String x : r.notes) System.out.println("    " + x);
        System.out.println("  clashes (semitone / minor ninth between two tracks for three quarters of a beat or more): " + r.clashes);
        for (String x : r.clashList) System.out.println("    " + x);
        for (String x : s.problems) System.out.println("  PROBLEM " + x);
        return new int[]{r.problems, r.nonChordTones, r.leaps, r.clashes};
    }

    /** The notes of each track bar by bar (every optional part, first instruments): `dump <song> [from [to]]`. */
    static void dump(Score s, int from, int to) {
        Score.Perf p = s.full();
        for (int k = from - 1; k < Math.min(to, p.bars.size()); k++) {
            Score.PBar pb = p.bars.get(k);
            StringBuilder ch = new StringBuilder();
            if (pb.sec.chords != null && pb.idx < pb.sec.chords.length) for (Score.Chord c : pb.sec.chords[pb.idx]) ch.append(c.sym).append(' ');
            System.out.println("bar " + (k + 1) + " [" + pb.sec.name + " " + (pb.idx + 1) + "]  " + ch);
            for (Score.Track t : s.tracks) {
                List<Score.Ev> es = new ArrayList<Score.Ev>();
                for (Score.Ev e : pb.sec.ev[pb.idx]) if (e.track == t) es.add(e);
                if (es.isEmpty()) continue;
                es.sort((u, v) -> Double.compare(u.beat, v.beat));
                StringBuilder sb = new StringBuilder();
                for (Score.Ev e : es) sb.append(name(e.midi)).append('@').append(String.format("%.2f", e.beat + 1)).append('/').append(String.format("%.2f", e.dur)).append(' ');
                System.out.println("    " + String.format("%-8s", t.name) + sb);
            }
        }
    }

    // ------------------------------------------------------------------ piano roll
    static void roll(Score s, File out, int mood) throws Exception {
        List<Ambient.Note> notes = Conductor.collect(s, mood, 1);
        Score.Perf p = s.full();
        double ppb = 7, bars = p.bars.size();
        int w = (int) (bars * s.beats * ppb) + 40, lo = 30, hi = 100, rowH = 8, h = (hi - lo) * rowH + 70;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(20, 22, 28)); g.fillRect(0, 0, w, h);
        for (int m = lo; m < hi; m++) { if (m % 12 == 0) { g.setColor(new Color(60, 64, 76)); g.drawLine(30, (hi - m) * rowH, w, (hi - m) * rowH); g.setColor(new Color(150, 150, 160)); g.setFont(new Font("SansSerif", Font.PLAIN, 9)); g.drawString("C" + (m / 12 - 1), 2, (hi - m) * rowH - 1); } }
        double bpmSec = s.beats * 60.0 / s.bpm;
        for (int k = 0; k <= bars; k++) {
            int x = 30 + (int) (k * s.beats * ppb);
            boolean sec = k < p.bars.size() ? p.bars.get(k).secStart : true;
            g.setColor(sec ? new Color(200, 180, 90) : new Color(45, 48, 58));
            g.drawLine(x, 0, x, h - 60);
            if (sec && k < p.bars.size()) { g.setColor(new Color(230, 210, 120)); g.setFont(new Font("SansSerif", Font.BOLD, 11)); g.drawString(p.bars.get(k).sec.name, x + 3, h - 46); }
            if (k < p.bars.size()) {
                Score.PBar pb = p.bars.get(k);
                if (pb.sec.chords != null && pb.idx < pb.sec.chords.length) {
                    g.setFont(new Font("SansSerif", Font.PLAIN, 9));
                    for (Score.Chord c : pb.sec.chords[pb.idx]) { g.setColor(new Color(150, 200, 255)); g.drawString(c.sym, 30 + (int) ((k * s.beats + c.start) * ppb) + 2, h - 32); }
                }
                if (k % 4 == 0) { g.setColor(new Color(120, 120, 130)); g.setFont(new Font("SansSerif", Font.PLAIN, 9)); g.drawString(String.valueOf(k + 1), x + 2, h - 20); }
            }
        }
        double secPerBeat = 60.0 / s.bpm;
        for (Ambient.Note n : notes) {
            double beat = n.at / Ambient.SR / secPerBeat, len = n.dur / secPerBeat;
            int x = 30 + (int) (beat * ppb), wd = Math.max(2, (int) (len * ppb)), y = (hi - n.midi) * rowH;
            if (n.midi < lo || n.midi >= hi) continue;
            float hue = (n.inst * 0.13f) % 1f;
            g.setColor(Color.getHSBColor(hue, 0.65f, 0.35f + (float) Math.min(0.65, n.vel * 0.65)));
            g.fillRect(x, y, wd, rowH - 1);
        }
        g.setColor(Color.WHITE); g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString(s.title + "  -  mood " + mood + "  -  " + clock(bars * bpmSec) + "  (" + (int) s.bpm + " bpm)", 34, h - 6);
        StringBuilder key = new StringBuilder();
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        int kx = w - 400;
        for (int i = 0; i < Ambient.INST.length; i++) { g.setColor(Color.getHSBColor((i * 0.13f) % 1f, 0.65f, 0.9f)); g.drawString(Ambient.INST[i], Math.max(300, kx) + (i % 8) * 48 - 60 + 60, h - 6 - (i / 8) * 11 + 0); }
        g.dispose();
        out.getParentFile().mkdirs();
        ImageIO.write(img, "png", out);
        System.out.println("wrote " + out);
    }

    // ------------------------------------------------------------------ midi file
    /** The instruments a list of notes uses. */
    static java.util.Set<Integer> usedInstruments(List<Ambient.Note> notes) {
        java.util.Set<Integer> used = new java.util.TreeSet<Integer>();
        for (Ambient.Note n : notes) used.add(n.inst);
        return used;
    }

    static void midi(Score s, File out, int mood) throws Exception {
        List<Ambient.Note> notes = Conductor.collect(s, mood, 1);
        Sequence seq = new Sequence(Sequence.PPQ, 480);
        Track tr = seq.createTrack();
        // 120 bpm: a quarter is half a second, so a tick is 1/960 s
        byte[] tempo = {0x07, (byte) 0xA1, 0x20};
        tr.add(new MidiEvent(new javax.sound.midi.MetaMessage(0x51, tempo, 3), 0));
        int[] map = MidiOut.staticMap(usedInstruments(notes));
        for (int i : usedInstruments(notes)) {
            if (i != Ambient.KIT) tr.add(new MidiEvent(new ShortMessage(ShortMessage.PROGRAM_CHANGE, map[i], Ambient.GM[i], 0), 0));
            tr.add(new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, map[i], 10, MidiOut.PAN[i]), 0));
        }
        for (Ambient.Note n : notes) {
            long on = (long) (n.at / Ambient.SR * 960), off = on + Math.max(1, (long) (n.dur * 960));
            tr.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, map[n.inst], n.midi, (int) Math.max(1, Math.min(127, n.vel * 127))), on));
            if (n.inst != Ambient.KIT) tr.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, map[n.inst], n.midi, 0), off));
        }
        MidiSystem.write(seq, 1, out);
        System.out.println("wrote " + out + " (" + notes.size() + " notes)");
    }

    /** A-weighted loudness (dB) of the loudest tenth of the 100 ms windows of 16-bit stereo samples (silence below -70 dB is left out): what the ear hears, not plain RMS. */
    static double loudness(byte[] pcm) {
        int frames = pcm.length / 4;
        double[] mono = new double[frames];
        for (int i = 0; i < frames; i++) {
            double l = (short) ((pcm[4 * i] & 0xff) | (pcm[4 * i + 1] << 8)) / 32768.0, r = (short) ((pcm[4 * i + 2] & 0xff) | (pcm[4 * i + 3] << 8)) / 32768.0;
            mono[i] = (l + r) / 2;
        }
        double[] w = aWeight(mono, 44100);
        int win = 4410;
        List<Double> levels = new ArrayList<Double>();
        for (int f = 0; f + win <= frames; f += win) {
            double sq = 0;
            for (int i = f; i < f + win; i++) sq += w[i] * w[i];
            double db = 10 * Math.log10(Math.max(1e-12, sq / win));
            if (db > -70) levels.add(db);
        }
        java.util.Collections.sort(levels);
        return levels.isEmpty() ? -99 : levels.get((int) (levels.size() * 0.9));
    }

    /** The A-weighting filter (IEC 61672 poles) by the bilinear transform, scaled to 0 dB at 1 kHz. */
    static double[] aWeight(double[] in, double fs) {
        double[] f = {20.598997, 20.598997, 107.65265, 737.86223, 12194.217, 12194.217};
        boolean[] highPass = {true, true, true, true, false, false};
        double c = 2 * fs;
        double[] y = in.clone();
        for (int k = 0; k < f.length; k++) {
            double wp = 2 * Math.PI * f[k];
            double a0 = c + wp, a1 = wp - c;
            double b0 = highPass[k] ? c : wp, b1 = highPass[k] ? -c : wp;
            double xPrev = 0, yPrev = 0;
            for (int i = 0; i < y.length; i++) {
                double xi = y[i];
                double yi = (b0 * xi + b1 * xPrev - a1 * yPrev) / a0;
                xPrev = xi; yPrev = yi; y[i] = yi;
            }
        }
        // gain at 1 kHz: measured with a sine through the same chain would be long; use the analytic value instead
        double wv = 2 * Math.PI * 1000, mag = 1;
        for (int k = 0; k < f.length; k++) {
            double wp = 2 * Math.PI * f[k];
            mag *= highPass[k] ? wv / Math.sqrt(wv * wv + wp * wp) : wp / Math.sqrt(wv * wv + wp * wp);
        }
        for (int i = 0; i < y.length; i++) y[i] /= mag;
        return y;
    }

    /** Plays one test phrase on each instrument (the same notes, velocity 0.65) and prints its loudness in the middle and the low register: the numbers behind `MidiOut.TRIM_DB`. */
    static void trim() throws Exception {
        System.out.println("=== instrument loudness in the SoundFont: test phrase at velocity 0.65 (dBFS, loudest tenth of 100 ms windows)");
        for (int inst = 0; inst < Ambient.INST.length; inst++) {
            double[] res = new double[2];
            int[][] phrases = inst == Ambient.KIT ? new int[][]{{38, 42, 36, 42, 38, 54}, {36, 36, 41, 36}} : new int[][]{{60, 64, 67, 72, 67, 64}, {48, 55, 48, 55}}; // (a kit: snare, hat, kick, tambourine / kick and floor tom)
            for (int ph = 0; ph < 2; ph++) {
                List<Ambient.Note> ns = new ArrayList<Ambient.Note>();
                long at = 0;
                for (int rep = 0; rep < 3; rep++) for (int m : phrases[ph]) { ns.add(new Ambient.Note(at, inst, m, 0.65, 0.9, 0, 0)); at += (long) (Ambient.SR * 1.0); }
                res[ph] = loudness(renderFont(ns));
            }
            System.out.printf("  %-8s middle %6.1f dB   low %6.1f dB%n", Ambient.INST[inst], res[0], res[1]);
        }
    }

    /** Role -> the loudness (dBFS, loudest tenth of the time a track plays, the SoundFont, one track alone) the mix is aiming at. */
    static final double[] TARGET_DB = {-28, -31, -34, -34, -33, -33, -38, -36};

    /** Renders every track alone and prints how loud it is, the target for its role and the track velocity that would reach it. */
    static void balance(Score s, int mood) throws Exception {
        List<Ambient.Note> notes = Conductor.collect(s, mood, 1);
        System.out.println("=== balance of " + s.title + " (dBFS, SoundFont, each track alone; roles aim at " + java.util.Arrays.toString(TARGET_DB) + " for " + java.util.Arrays.toString(Score.ROLES) + ")");
        for (Score.Track t : s.tracks) {
            List<Ambient.Note> mine = new ArrayList<Ambient.Note>();
            for (Ambient.Note n : notes) if (n.track == t.index) mine.add(n);
            if (mine.isEmpty()) continue;
            double p90 = loudness(renderFont(mine));
            double target = TARGET_DB[t.roleIdx], newVel = t.vel * Math.pow(10, (target - p90) / 40);
            System.out.printf("  %-8s %-8s %-8s %6.1f dB  target %5.0f  vel %.2f -> %.2f%n", t.name, t.role, Ambient.INST[mine.get(0).inst], p90, target, t.vel, newVel);
        }
    }

    // ------------------------------------------------------------------ how alike the songs are
    private static final double[] MAJOR_PROFILE = {6.35, 2.23, 3.48, 2.33, 4.38, 4.09, 2.52, 5.19, 2.39, 3.66, 2.29, 2.88}, MINOR_PROFILE = {6.33, 2.68, 3.52, 5.38, 2.60, 3.53, 2.54, 4.75, 3.98, 2.69, 3.34, 3.17};
    private static final String[] PC_NAMES = {"C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B"};

    /** What a song sounds like, in four blocks of numbers (each scaled to length 1): the instruments by number of notes, where the notes of the pitched instruments fall inside a beat (twelfths),
     *  the steps of the lead lines (-12..+12 semitones), and the pitch classes of the lead lines turned so that the key is C. `label[0]` receives "key, metre, tempo". */
    static double[][] features(Score s, String[] label) {
        Score.Perf p = s.full();
        double[] inst = new double[Ambient.INST.length], onset = new double[12], step = new double[25], pc = new double[12];
        java.util.Map<Integer, List<double[]>> lead = new java.util.HashMap<Integer, List<double[]>>();
        for (int k = 0; k < p.bars.size(); k++) {
            Score.PBar pb = p.bars.get(k);
            for (Score.Ev e : pb.sec.ev[pb.idx]) {
                int in = e.inst >= 0 ? e.inst : e.track.insts[0];
                inst[in]++;
                boolean drum = in == Ambient.KIT || in == Ambient.TAIKO || in == Ambient.WOOD;
                if (!drum) onset[(int) Math.round((e.beat - Math.floor(e.beat + 1e-9)) * 12) % 12]++;
                if (e.track.roleIdx == 0 && !drum) {
                    pc[e.midi % 12]++;
                    List<double[]> l = lead.get(e.track.index);
                    if (l == null) lead.put(e.track.index, l = new ArrayList<double[]>());
                    l.add(new double[]{k * s.beats + e.beat, e.midi});
                }
            }
        }
        for (List<double[]> l : lead.values()) {
            java.util.Collections.sort(l, new java.util.Comparator<double[]>() { public int compare(double[] a, double[] b) { return Double.compare(a[0], b[0]); } });
            for (int i = 1; i < l.size(); i++) { int d = (int) (l.get(i)[1] - l.get(i - 1)[1]); if (l.get(i)[0] - l.get(i - 1)[0] > 4 * s.beats) continue; step[Math.max(-12, Math.min(12, d)) + 12]++; }
        }
        // the key: the turning of the pitch classes that fits a major or minor profile best
        double best = -9; int bestRot = 0; boolean minor = false;
        for (int rot = 0; rot < 12; rot++) for (int mode = 0; mode < 2; mode++) {
            double[] prof = mode == 0 ? MAJOR_PROFILE : MINOR_PROFILE;
            double sx = 0, sy = 0, sxx = 0, syy = 0, sxy = 0;
            for (int i = 0; i < 12; i++) { double x = pc[(i + rot) % 12], y = prof[i]; sx += x; sy += y; sxx += x * x; syy += y * y; sxy += x * y; }
            double c = (12 * sxy - sx * sy) / Math.sqrt(Math.max(1e-9, (12 * sxx - sx * sx) * (12 * syy - sy * sy)));
            if (c > best) { best = c; bestRot = rot; minor = mode == 1; }
        }
        double[] turned = new double[12];
        for (int i = 0; i < 12; i++) turned[i] = pc[(i + bestRot) % 12];
        label[0] = PC_NAMES[bestRot] + (minor ? " minor" : " major") + ", " + (s.beats == Math.rint(s.beats) ? String.valueOf((int) s.beats) : String.valueOf(s.beats)) + " beats, " + (int) s.bpm + " bpm";
        return new double[][]{unit(inst), unit(onset), unit(step), unit(turned)};
    }

    private static double[] unit(double[] v) {
        double n = 0;
        for (double x : v) n += x * x;
        n = Math.sqrt(n);
        double[] o = new double[v.length];
        for (int i = 0; i < v.length; i++) o[i] = n == 0 ? 0 : v[i] / n;
        return o;
    }

    private static double cos(double[] a, double[] b) { double s = 0; for (int i = 0; i < a.length; i++) s += a[i] * b[i]; return s; }

    /** How alike two songs are, 0..1: the mean of the four blocks (instruments, rhythm, melodic steps, key colour), lowered when the metre or the tempo differs. */
    static double similarity(double[][] a, double[][] b, Score sa, Score sb) {
        double m = (cos(a[0], b[0]) + cos(a[1], b[1]) + cos(a[2], b[2]) + cos(a[3], b[3])) / 4;
        if (sa.beats != sb.beats) m *= 0.9;
        double tempo = Math.min(sa.bpm, sb.bpm) / Math.max(sa.bpm, sb.bpm);
        if (tempo < 0.85) m *= 0.95;
        return m;
    }

    /** `similar [limit]`: every song with its key, metre and tempo, and the pairs that sound most alike (similarity above `limit`, default 0.9). */
    static void similar(double limit) {
        List<Score> all = SongBook.all();
        int n = all.size();
        double[][][] f = new double[n][][];
        String[] label = new String[n];
        for (int i = 0; i < n; i++) { String[] l = new String[1]; f[i] = features(all.get(i), l); label[i] = l[0]; System.out.printf("%2d. %-44s %s%n", i + 1, all.get(i).title, l[0]); }
        List<double[]> pairs = new ArrayList<double[]>();
        double[] nearest = new double[n]; int[] near = new int[n];
        for (int i = 0; i < n; i++) for (int j = i + 1; j < n; j++) {
            double sm = similarity(f[i], f[j], all.get(i), all.get(j));
            pairs.add(new double[]{sm, i, j});
            if (sm > nearest[i]) { nearest[i] = sm; near[i] = j; }
            if (sm > nearest[j]) { nearest[j] = sm; near[j] = i; }
        }
        java.util.Collections.sort(pairs, new java.util.Comparator<double[]>() { public int compare(double[] a, double[] b) { return Double.compare(b[0], a[0]); } });
        System.out.println("=== the most alike pairs (limit " + limit + ")");
        int shown = 0;
        for (double[] p : pairs) { if (p[0] < limit && shown >= 10) break; System.out.printf("  %.3f  %s  /  %s%n", p[0], all.get((int) p[1]).title, all.get((int) p[2]).title); if (++shown >= 25) break; }
        double mean = 0; for (double[] p : pairs) mean += p[0]; mean /= Math.max(1, pairs.size());
        System.out.printf("=== %d songs, %d pairs, mean similarity %.3f, the closest pair %.3f%n", n, pairs.size(), mean, pairs.isEmpty() ? 0 : pairs.get(0)[0]);
    }

    // ------------------------------------------------------------------ recording
    static void wav(Score s, File out, int mood, String engine) throws Exception {
        byte[] pcm;
        if (engine.startsWith("b")) pcm = Ambient.renderScore(s, mood, 1, 900);
        else pcm = renderFont(Conductor.collect(s, mood, 1));
        double peak = 0, sq = 0;
        int n = pcm.length / 2;
        for (int i = 0; i < n; i++) { double v = (short) ((pcm[2 * i] & 0xff) | (pcm[2 * i + 1] << 8)) / 32768.0; peak = Math.max(peak, Math.abs(v)); sq += v * v; }
        System.out.printf("  %s: %s, peak %.1f dBFS, loudness %.1f dBFS rms%n", s.title, clock(n / 2 / 44100.0), 20 * Math.log10(Math.max(1e-9, peak)), 10 * Math.log10(Math.max(1e-12, sq / n)));
        AudioFormat fmt = new AudioFormat(44100f, 16, 2, true, false);
        out.getParentFile().mkdirs();
        AudioSystem.write(new AudioInputStream(new ByteArrayInputStream(pcm), fmt, pcm.length / 4), AudioFileFormat.Type.WAVE, out);
        System.out.println("wrote " + out);
    }

    /** Renders notes through the Java software synthesiser with the first SoundFont of soundfonts/, as fast as it can (not in real time). */
    static byte[] renderFont(List<Ambient.Note> notes) throws Exception {
        List<String> fonts = Ambient.fonts();
        if (fonts.isEmpty()) throw new IllegalStateException("no SoundFont in soundfonts/");
        Class<?> cls = Class.forName("com.sun.media.sound.SoftSynthesizer");
        Object synth = cls.getDeclaredConstructor().newInstance();
        AudioFormat fmt = new AudioFormat(44100f, 16, 2, true, false);
        Map<String, Object> info = new HashMap<String, Object>();
        info.put("interpolation", "sinc");
        info.put("max polyphony", 256);
        AudioInputStream stream = (AudioInputStream) cls.getMethod("openStream", AudioFormat.class, Map.class).invoke(synth, fmt, info);
        Synthesizer sy = (Synthesizer) synth;
        Soundbank bank = MidiSystem.getSoundbank(new File("soundfonts", fonts.get(0)));
        sy.unloadAllInstruments(sy.getDefaultSoundbank());
        sy.loadAllInstruments(bank);
        Receiver rc = sy.getReceiver();
        int[] map = MidiOut.staticMap(usedInstruments(notes));
        for (int i : usedInstruments(notes)) {
            int c = map[i];
            if (i != Ambient.KIT) rc.send(new ShortMessage(ShortMessage.PROGRAM_CHANGE, c, Ambient.GM[i], 0), 0);
            rc.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, c, 7, MidiOut.cc7(i, 1.0, 1.0)), 0);
            rc.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, c, 10, MidiOut.PAN[i]), 0);
            rc.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, c, 91, i == Ambient.KIT ? 45 : 70), 0);
        }
        double endSec = 0;
        for (Ambient.Note n : notes) {
            long on = (long) (n.at / Ambient.SR * 1e6), off = on + (long) (n.dur * 1e6);
            rc.send(new ShortMessage(ShortMessage.NOTE_ON, map[n.inst], n.midi, (int) Math.max(1, Math.min(127, n.vel * 127))), on);
            if (n.inst != Ambient.KIT) rc.send(new ShortMessage(ShortMessage.NOTE_OFF, map[n.inst], n.midi, 0), off);
            endSec = Math.max(endSec, off / 1e6);
        }
        int total = (int) ((endSec + 4) * 44100) * 4;
        byte[] pcm = new byte[total];
        int got = 0;
        while (got < total) { int r = stream.read(pcm, got, Math.min(65536, total - got)); if (r <= 0) break; got += r; }
        stream.close();
        sy.close();
        return pcm;
    }
}
