package Game;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

/** Endless music made on the fly (no audio files). A `Composer` writes whole pieces as note events: a title, a key and a pentatonic mode, a tempo, a
 *  motif that comes back in four sections (statement, answer, contrast, return) over a chord progression, with a guzheng or pipa lead, a dizi
 *  answering, an erhu singing long glides, a warm pad, gongs and chimes at the section marks, and drums when the world turns dangerous.
 *  Context cues (`setMood`): 0 open country, 1 city, 2 danger, 3 night/winter, 4 war, 5 sect, 6 the capital Originis. Two engines play the notes:
 *  the built-in stereo synthesiser (Karplus-Strong strings, bowed and blown tones, a stereo reverb) or, when a `.sf2`/`.dls` file sits in
 *  `soundfonts/`, the Java software MIDI synthesiser with that SoundFont (see `MidiOut`). With no audio device it quietly turns itself off. */
final class Ambient {
    private Ambient() { }

    static final float SR = 44100f;
    private static volatile Thread thread = null;
    private static volatile boolean running = false;
    private static volatile int mood = 0;
    private static volatile double volume = 0.5;
    private static volatile boolean reseed = false;
    static volatile String status = "off";
    /** Title of the piece now playing ("" when none). */
    static volatile String nowPlaying = "";
    /** Which engine is playing: "built-in" or the SoundFont's name. */
    static volatile String engine = "built-in";
    static volatile int season = 0;

    // instruments (also the MIDI channel order; channel 9 is skipped, it is the drum kit)
    static final int PLUCK = 0, FLUTE = 1, ERHU = 2, PAD = 3, BELL = 4, CHIME = 5, TAIKO = 6, WOOD = 7, PIPA = 8;
    static final String[] INST = {"pluck", "flute", "erhu", "pad", "bell", "chime", "taiko", "wood", "pipa"};
    /** Per-layer loudness 0..1.5 (Settings.musicMix: "pluck=80,flute=100,..."). */
    static final double[] layer = {1, 1, 1, 1, 1, 1, 1, 1, 1};
    private static final int[] GM = {107, 73, 110, 89, 14, 98, 116, 115, 106}; // koto, flute, fiddle, warm pad, tubular bells, crystal, taiko, woodblock, shamisen

    static synchronized void start() {
        if (running) return;
        running = true;
        Thread t = new Thread(Ambient::loop, "ambient-music");
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY + 1);
        thread = t;
        t.start();
    }

    static synchronized void stop() {
        running = false;
        Thread t = thread;
        if (t != null) t.interrupt();
        thread = null;
        status = "off";
        nowPlaying = "";
    }

    /** Starts or stops the music to match Settings (a changed engine restarts it). */
    static void apply() {
        volume = Math.max(0, Math.min(100, Settings.musicVolume)) / 100.0;
        parseMix(Settings.musicMix);
        String want = Settings.musicEngine + "|" + Settings.soundFont;
        if (running && !want.equals(startedWith)) stop();
        if (Settings.music) { startedWith = want; start(); } else stop();
    }
    private static volatile String startedWith = "", fallback = "";

    static void setMood(int m) { mood = m; }
    static void newPiece() { reseed = true; }

    static void parseMix(String s) {
        Arrays.fill(layer, 1.0);
        if (s == null) return;
        for (String part : s.split(",")) {
            String[] kv = part.trim().split("=");
            if (kv.length != 2) continue;
            for (int i = 0; i < INST.length; i++) if (INST[i].equalsIgnoreCase(kv[0].trim())) {
                try { layer[i] = Math.max(0, Math.min(1.5, Integer.parseInt(kv[1].trim()) / 100.0)); } catch (NumberFormatException ignored) { }
            }
        }
    }

    static String mixString() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < INST.length; i++) { if (b.length() > 0) b.append(','); b.append(INST[i]).append('=').append(Math.round(layer[i] * 100)); }
        return b.toString();
    }

    /** SoundFont files found in the soundfonts/ folder. */
    static List<String> fonts() {
        List<String> out = new ArrayList<>();
        File[] fs = new File("soundfonts").listFiles();
        if (fs != null) for (File f : fs) { String n = f.getName().toLowerCase(); if (n.endsWith(".sf2") || n.endsWith(".dls") || n.endsWith(".sf3")) out.add(f.getName()); }
        java.util.Collections.sort(out);
        return out;
    }

    // ------------------------------------------------------------------ notes
    /** One note the composer wants played: `at` in samples from the start, `dur` in seconds, `glide` = semitones below the pitch the note starts at. */
    static final class Note {
        final long at; final int inst, midi; final double vel, dur, pan, glide;
        Note(long at, int inst, int midi, double vel, double dur, double pan, double glide) { this.at = at; this.inst = inst; this.midi = midi; this.vel = vel; this.dur = dur; this.pan = pan; this.glide = glide; }
    }

    // ------------------------------------------------------------------ voices
    private abstract static class Voice {
        double pan = 0, gain = 1;
        abstract double next();
        abstract boolean done();
    }

    /** A plucked string (guzheng, pipa): Karplus-Strong. */
    private static final class Pluck extends Voice {
        final double[] buf; int pos = 0; final double decay; int life; final double amp;
        Pluck(double freq, double amp, double t60, double bright, Random r) {
            double decay = Math.pow(10, -3.0 / (Math.max(0.3, t60) * freq)); // per period; t60 = seconds until the note is 60 dB down
            int n = Math.max(8, (int) (SR / freq));
            buf = new double[n];
            double prev = 0;
            for (int i = 0; i < n; i++) { double x = r.nextDouble() * 2 - 1; prev = prev * bright + x * (1 - bright); buf[i] = prev; } // a softer burst = a duller pick
            this.decay = decay; this.amp = amp; life = (int) (SR * Math.max(1.0, t60 * 1.3));
        }
        double next() {
            int nx = (pos + 1) % buf.length;
            double y = buf[pos];
            buf[pos] = (y + buf[nx]) * 0.5 * decay;
            pos = nx; life--;
            return y * amp;
        }
        boolean done() { return life <= 0 || Math.abs(buf[pos]) < 1e-4; }
    }

    /** A sustained tone: harmonics with weights, slow swell, delayed vibrato, an optional glide up into the pitch and breath noise (erhu, dizi, pad, drone). */
    private static final class Tone extends Voice {
        final double f, f0, amp, vib, vibRate, breath, glideN; final double[] h; final int total, attack, release, vibDelay; int t = 0; double ph = 0, vph = 0, lp = 0; final Random r;
        Tone(double f, double glideSemis, double amp, double seconds, double attackS, double releaseS, double vibDepth, double breath, double[] harmonics, Random r) {
            this.f = f; this.f0 = f * Math.pow(2, -glideSemis / 12.0); glideN = glideSemis > 0 ? SR * 0.22 : 0;
            this.amp = amp; total = (int) (SR * seconds); attack = Math.max(1, (int) (SR * attackS)); release = Math.max(1, (int) (SR * releaseS));
            vib = vibDepth; vibRate = 4.6 + r.nextDouble() * 1.6; vibDelay = (int) (SR * 0.5); this.breath = breath; h = harmonics; this.r = r;
        }
        double next() {
            double env = t < attack ? t / (double) attack : t > total - release ? Math.max(0, (total - t) / (double) release) : 1.0;
            vph += vibRate * 2 * Math.PI / SR;
            double fc = t < glideN ? f0 * Math.pow(f / f0, t / glideN) : f;
            double fm = 1 + Math.sin(vph) * vib * Math.min(1, Math.max(0, (t - vibDelay) / (SR * 0.6)));
            ph += 2 * Math.PI * fc * fm / SR;
            double s = 0;
            for (int k = 0; k < h.length; k++) s += h[k] * Math.sin((k + 1) * ph);
            if (breath > 0) { lp += ((r.nextDouble() * 2 - 1) - lp) * 0.35; s += lp * breath * (t < SR * 0.08 ? 3 : 1); }
            t++;
            return s * env * amp;
        }
        boolean done() { return t >= total; }
    }

    /** A gong or bell: a handful of inharmonic partials that die away slowly. */
    private static final class Bell extends Voice {
        final double[] f, a, ph; int t = 0; final double len;
        Bell(double base, double amp, double seconds) {
            double[] ratios = {1, 1.47, 2.09, 2.56, 3.37};
            f = new double[ratios.length]; a = new double[ratios.length]; ph = new double[ratios.length];
            for (int i = 0; i < ratios.length; i++) { f[i] = base * ratios[i]; a[i] = amp / (1 + i * 0.9); }
            len = seconds * SR;
        }
        double next() {
            double s = 0, e = Math.exp(-5.0 * t / len);
            for (int i = 0; i < f.length; i++) { ph[i] += 2 * Math.PI * f[i] / SR; s += Math.sin(ph[i]) * a[i] * Math.exp(-i * 0.5 * t / len); }
            t++;
            return s * e * Math.min(1, t / 80.0);
        }
        boolean done() { return t >= len; }
    }

    /** A drum: a sine that falls in pitch plus a click of noise (taiko, hand drum) or a short pair of high partials (wood block). */
    private static final class Drum extends Voice {
        final double f, amp, tau, click; final boolean wood; int t = 0; double ph = 0, ph2 = 0; final Random r;
        Drum(double f, double amp, boolean wood, Random r) { this.f = f; this.amp = amp; this.wood = wood; tau = wood ? 0.05 : f < 90 ? 0.28 : 0.15; click = wood ? 0 : 0.5; this.r = r; }
        double next() {
            double sec = t / (double) SR;
            double fc = wood ? f : f * (1 + 1.1 * Math.exp(-sec * 38));
            ph += 2 * Math.PI * fc / SR; ph2 += 2 * Math.PI * f * 1.62 / SR;
            double s = Math.sin(ph) * Math.exp(-sec / tau);
            if (wood) s += 0.5 * Math.sin(ph2) * Math.exp(-sec / (tau * 0.6));
            else s += (r.nextDouble() * 2 - 1) * click * Math.exp(-sec * 160);
            t++;
            return s * amp * Math.min(1, t / 20.0);
        }
        boolean done() { return t > SR * (wood ? 0.4 : 1.2); }
    }

    // ------------------------------------------------------------------ reverb (two of them, tuned differently, make the stereo width)
    private static final class Reverb {
        final double[][] comb; final int[] cpos; final double[][] ap; final int[] apos; final double fb = 0.8, damp = 0.35; final double[] lp;
        Reverb(int[] cl, int[] al) {
            comb = new double[cl.length][]; cpos = new int[cl.length]; lp = new double[cl.length];
            for (int i = 0; i < cl.length; i++) comb[i] = new double[cl[i]];
            ap = new double[al.length][]; apos = new int[al.length];
            for (int i = 0; i < al.length; i++) ap[i] = new double[al[i]];
        }
        double process(double x) {
            double s = 0;
            for (int i = 0; i < comb.length; i++) {
                double y = comb[i][cpos[i]];
                lp[i] = y * (1 - damp) + lp[i] * damp;
                comb[i][cpos[i]] = x + lp[i] * fb;
                cpos[i] = (cpos[i] + 1) % comb[i].length;
                s += y;
            }
            s *= 0.25;
            for (int i = 0; i < ap.length; i++) {
                double b = ap[i][apos[i]];
                double y = -s + b;
                ap[i][apos[i]] = s + b * 0.5;
                apos[i] = (apos[i] + 1) % ap[i].length;
                s = y;
            }
            return s;
        }
    }

    // ------------------------------------------------------------------ the composer
    private static final int[][] MODES = {{0, 2, 4, 7, 9}, {0, 2, 5, 7, 10}, {0, 3, 5, 7, 10}, {0, 2, 5, 7, 9}, {0, 3, 5, 8, 10}}; // gong, shang, jue, zhi, yu pentatonics
    private static final double[][] RHYTHMS = { // a two-bar phrase in beats (each sums to 8)
        {1, 1, 2, 1, 1, 2}, {2, 1, 1, 2, 2}, {1.5, 0.5, 1, 1, 2, 2}, {0.5, 0.5, 1, 2, 1, 0.5, 0.5, 2}, {3, 1, 2, 2}, {1, 1, 1, 1, 2, 2}, {2, 2, 1, 1, 1, 1}, {1, 0.5, 0.5, 1, 1, 1, 3}};
    private static final int[][] PROGS = {{0, 3, 4, 0}, {0, 2, 3, 1}, {0, 4, 3, 0}, {0, 3, 2, 4}, {0, 1, 3, 4}};
    private static final double[] BRIGHT_H = {1, 0.6, 0.5, 0.35, 0.25, 0.18, 0.12, 0.08}, FLUTE_H = {1, 0.22, 0.07}, PAD_H = {1, 0.16, 0.05}, DRONE_H = {1, 0.5};

    private static double hz(double midi) { return 440.0 * Math.pow(2, (midi - 69) / 12.0); }

    /** Names for pieces, from the seed and the mood. */
    static String title(Random r, int m) { return Naming.songTitle(r, m); }

    /** The composer: all the music's state. `poll(clock)` returns the notes that are due; bars are written a little ahead of time. */
    static final class Composer {
        Random r;
        final PriorityQueue<Note> queue = new PriorityQueue<>(64, (a, b) -> Long.compare(a.at, b.at));
        int tonic, bar, sections, pieceMood, restBars;
        int[] mode, prog, motifDeg; double[] motifDur;
        double bpm; long nextBar = 0; String title = "";
        volatile int mood = 0;
        boolean arrival = true;

        Composer(long seed) { r = new Random(seed); newPiece(false); }

        private static boolean strong(int m) { return m == 2 || m == 4 || m == 6; }

        void reseed(long seed, long now) { r = new Random(seed); queue.clear(); newPiece(false); nextBar = now; }

        /** A fresh piece (same key when `keepKey`, so a change of scene sounds like the same world). */
        void newPiece(boolean keepKey) {
            pieceMood = mood;
            if (!keepKey || mode == null) { tonic = 46 + r.nextInt(12); mode = MODES[r.nextInt(MODES.length)]; }
            else if (r.nextInt(3) == 0) tonic = 44 + ((tonic - 44 + 5 * (1 + r.nextInt(2))) % 18);
            bpm = 56 + r.nextInt(24);
            sections = 4;
            bar = 0; restBars = 2 + r.nextInt(3);
            prog = PROGS[r.nextInt(PROGS.length)];
            double[] rh = RHYTHMS[r.nextInt(RHYTHMS.length)];
            motifDur = rh;
            motifDeg = new int[rh.length];
            int d = r.nextInt(3);
            for (int i = 0; i < rh.length; i++) { motifDeg[i] = d; d += r.nextInt(5) - 2; d = Math.max(-2, Math.min(7, d)); }
            motifDeg[rh.length - 1] = new int[]{0, 3, 2}[r.nextInt(3)] + (r.nextBoolean() ? 0 : 5);  // the phrase comes home on the root, fifth or third
            title = Ambient.title(r, mood);
            Ambient.nowPlaying = title;
        }

        int pitch(int deg) {
            int p = tonic + mode[Math.floorMod(deg, mode.length)] + 12 * Math.floorDiv(deg, mode.length) + 12;
            while (p > tonic + 38) p -= 12;
            return p;
        }

        private void add(long at, int inst, int midi, double vel, double dur, double pan, double glide) {
            at += (long) ((r.nextDouble() - 0.5) * 0.03 * SR); // a human touch
            queue.add(new Note(Math.max(0, at), inst, midi, vel, dur, pan, glide));
        }

        /** Writes the notes of one bar (starting at sample `t0`) into the queue and moves on. */
        private void composeBar(long t0) {
            int m = mood;
            if (bar >= sections * 8 + restBars || (strong(m) != strong(pieceMood) || (strong(m) && m != pieceMood)) && bar > 0) { newPiece(true); }
            double tempo = m == 4 ? 1.55 : m == 1 ? 1.15 : m == 3 ? 0.8 : m == 2 ? 0.9 : 1.0;
            double beat = SR * 60.0 / (bpm * tempo), barLen = 4 * beat;
            boolean resting = bar >= sections * 8;
            int sec = Math.min(sections - 1, bar / 8), bi = bar % 8, inPhrase = bi % 2, phrase = bi / 2;
            double vol = m == 3 ? 0.7 : 1.0;
            int lead = (m == 1 || m == 4) ? PIPA : PLUCK;
            int shift = m == 2 ? -12 : 0;

            // the chord under this bar
            int ch = prog[(bar / 2) % prog.length];
            if (!resting && bar % 2 == 0) {
                for (int k = 0; k < 3; k++) {
                    int mn = tonic + mode[Math.floorMod(ch + 2 * k, mode.length)] + 12 * Math.floorDiv(ch + 2 * k, mode.length) + (m == 2 ? -12 : 0);
                    if (m != 2 || k == 0) add(t0, PAD, mn, 0.34 * vol, 1.9 * barLen / SR, -0.5 + k * 0.5, 0);
                }
                add(t0, PAD, tonic - 12 + mode[Math.floorMod(ch, mode.length)] + shift, 0.4 * vol, 1.9 * barLen / SR, 0, 0);
            }
            // section marks
            if (bi == 0 && (bar > 0 || arrival)) { add(t0, BELL, tonic - 12 + shift - (m == 2 ? 5 : 0), 0.5, 4 + r.nextInt(2), 0, 0); }
            if (m == 6 && bar % 4 == 0) add(t0 + (long) (beat * 2), BELL, tonic - 7, 0.4, 4, 0.2, 0);
            if (bi == 7 && !resting) for (int k = 0; k < 3; k++) add(t0 + (long) (barLen * 0.5 + k * beat * 0.7), CHIME, tonic + 36 + mode[r.nextInt(mode.length)], 0.3, 1.6, r.nextDouble() - 0.5, 0);
            arrival = false;

            if (!resting) {
                // the lead: the motif, varied by section; it starts on the first bar of a two-bar phrase
                if (inPhrase == 0) {
                    int dTrans = sec == 2 ? 2 : sec == 1 && phrase % 2 == 1 ? -1 : 0;
                    boolean invert = sec == 2 && phrase % 2 == 1;
                    int last = sec == 3 && phrase == 3 ? 0 : -1;
                    double t = 0;
                    for (int i = 0; i < motifDeg.length; i++) {
                        int dd = motifDeg[i];
                        if (invert) dd = 4 - dd;
                        dd += dTrans;
                        if (i == motifDeg.length - 1 && last == 0) dd = 0;
                        else if (sec == 1 && i == motifDeg.length - 1 && phrase % 2 == 0) dd += 1; // the answer ends open
                        double len = motifDur[i];
                        boolean skip = m == 3 && r.nextInt(100) < 40 || m == 2 && r.nextInt(100) < 55 || sec == 3 && phrase % 2 == 1 && i % 2 == 1 && r.nextInt(3) == 0;
                        if (!skip) {
                            long at = t0 + (long) (t * beat);
                            if (r.nextInt(100) < 14) add(at - (long) (beat * 0.12), lead, pitch(dd + 1) + shift, 0.35 * vol, 0.8, -0.3, 0);       // a grace note
                            if (i == motifDeg.length - 1 && r.nextInt(100) < 35) for (int g = 0; g < 4; g++) add(at - (long) (beat * (0.55 - g * 0.13)), lead, pitch(dd - 4 + g) + shift, 0.28 * vol, 0.8, -0.4 + g * 0.1, 0); // a guzheng run into the last note
                            add(at, lead, pitch(dd) + shift, (0.55 + r.nextDouble() * 0.3) * vol, Math.min(2.2, 0.6 + len * beat / SR), lead == PIPA ? 0.25 : -0.25, 0);
                            if (lead == PIPA && len >= 2) for (int k = 1; k < 4; k++) add(at + (long) (k * beat * 0.25), lead, pitch(dd) + shift, 0.35 * vol, 0.5, 0.25, 0); // pipa tremolo
                        }
                        t += len;
                    }
                }
                // an arpeggio underneath when the world is busy
                if (m == 1 || m == 4 || sec == 1 && m == 0 && r.nextInt(3) == 0) {
                    for (int k = 0; k < 8; k++) if (r.nextInt(100) < (m == 4 ? 85 : 55)) {
                        int dd = ch + new int[]{0, 2, 4, 2}[k % 4];
                        add(t0 + (long) (k * beat * 0.5), PLUCK, pitch(dd) - 12, 0.3 * vol, 1.6, -0.5, 0);
                    }
                }
                // the dizi answers in the second bar of a phrase
                boolean springy = season == 0 && r.nextInt(100) < 25;
                if (inPhrase == 1 && m != 2 && m != 4 && (sec >= 1 && r.nextInt(100) < 60 || springy)) {
                    int n = 2 + r.nextInt(2);
                    double t = 0;
                    for (int k = 0; k < n; k++) {
                        int dd = motifDeg[Math.max(0, motifDeg.length - 1 - k * 2)] + (sec == 2 ? 2 : 0);
                        double len = k == n - 1 ? 2.2 : 1.0;
                        add(t0 + (long) (t * beat), FLUTE, pitch(dd) + 12, (0.5 + r.nextDouble() * 0.2) * vol, Math.min(2.2, len * beat / SR * 0.9), 0.45, 0);
                        t += len;
                    }
                }
                // the erhu sings long, sliding notes in the contrasting section, in the dark, in autumn and in the capital
                if ((sec == 2 || m == 2 || m == 6 || season == 2 && sec % 2 == 1) && bar % 2 == 0 && m != 4) {
                    int dd = ch + (r.nextBoolean() ? 2 : 0) + 5;
                    add(t0 + (long) (beat * 0.5), ERHU, pitch(dd) + shift, 0.55 * vol, Math.min(2.8, barLen / SR * 0.9), -0.35, 2 + r.nextInt(3));
                }
                // drums
                switch (m) {
                    case 2:
                        add(t0, TAIKO, 38, 0.7, 0.8, 0, 0);
                        if (r.nextInt(100) < 50) add(t0 + (long) (beat * 2.5), TAIKO, 41, 0.45, 0.7, 0, 0);
                        break;
                    case 4:
                        for (int k = 0; k < 8; k++) {
                            boolean acc = k == 0 || k == 3 || k == 5;
                            if (acc || r.nextInt(100) < 40) add(t0 + (long) (k * beat * 0.5), TAIKO, acc ? 38 : 45, acc ? 0.85 : 0.5, 0.6, 0, 0);
                        }
                        for (int k = 0; k < 4; k++) add(t0 + (long) (k * beat), WOOD, 84, 0.45, 0.3, 0.3, 0);
                        break;
                    case 1:
                        if (bar % 2 == 0) { add(t0 + (long) beat, WOOD, 84, 0.35, 0.3, 0.3, 0); add(t0 + (long) (3 * beat), WOOD, 79, 0.3, 0.3, 0.3, 0); add(t0 + (long) (2 * beat), TAIKO, 55, 0.35, 0.4, 0.2, 0); }
                        break;
                    case 6:
                        if (bar % 2 == 0) add(t0, TAIKO, 36, 0.5, 1.0, 0, 0);
                        break;
                    case 5:
                        for (int k = 0; k < 4; k++) add(t0 + (long) (k * beat), WOOD, 76, 0.22, 0.3, 0.35, 0); // a temple block keeping the hour
                        break;
                    default:
                }
            }
            bar++;
            nextBar = t0 + (long) barLen;
        }

        /** The next note due at or before `clock`, or null. Writes more bars when the queue runs low (a quarter second ahead). */
        Note poll(long clock) {
            while (nextBar <= clock + (long) (SR * 0.25)) composeBar(nextBar);
            Note n = queue.peek();
            if (n != null && n.at <= clock) return queue.poll();
            return null;
        }
    }

    // ------------------------------------------------------------------ the built-in synthesiser
    private static final class Synth {
        final Composer comp;
        final List<Voice> voices = new ArrayList<>();
        final Reverb revL = new Reverb(new int[]{1557, 1617, 1491, 1422, 1277}, new int[]{556, 441}), revR = new Reverb(new int[]{1603, 1531, 1459, 1379, 1319}, new int[]{571, 463});
        long clock = 0; final Random r = new Random(77);

        Synth(Composer c) { comp = c; }

        void spawn(Note n) {
            double f = hz(n.midi);
            Voice v;
            switch (n.inst) {
                case PLUCK: v = new Pluck(f, 0.26 * n.vel, 1.5 + r.nextDouble() * 0.6, 0.55, r); break;
                case PIPA: v = new Pluck(f, 0.26 * n.vel, 1.1, 0.35, r); break;
                case FLUTE: v = new Tone(f, 0, 0.13 * n.vel, n.dur, 0.15, 0.35, 0.005, 0.02, FLUTE_H, r); break;
                case ERHU: v = new Tone(f, n.glide, 0.12 * n.vel, n.dur, 0.25, 0.6, 0.008, 0.006, BRIGHT_H, r); break;
                case PAD: v = new Tone(f * (1 + (r.nextDouble() - 0.5) * 0.004), 0, 0.075 * n.vel, n.dur, 1.2, 1.6, 0.0012, 0, n.midi < 50 ? DRONE_H : PAD_H, r); break;
                case BELL: v = new Bell(f, 0.17 * n.vel, n.dur); break;
                case CHIME: v = new Bell(f, 0.04 * n.vel, n.dur); break;
                case TAIKO: v = new Drum(f, 0.42 * n.vel, false, r); break;
                default: v = new Drum(f * 6, 0.22 * n.vel, true, r);
            }
            v.pan = n.pan;
            v.gain = layer[n.inst];
            voices.add(v);
        }

        /** Fills `out` with n frames of 16-bit little-endian stereo. */
        void fill(byte[] out, int n, int m, double volume) {
            comp.mood = m;
            for (int i = 0; i < n; i++, clock++) {
                for (Note nt = comp.poll(clock); nt != null; nt = comp.poll(clock)) spawn(nt);
                double l = 0, rr = 0;
                for (int v = voices.size() - 1; v >= 0; v--) {
                    Voice vo = voices.get(v);
                    double s = vo.next() * vo.gain;
                    double a = (vo.pan + 1) * Math.PI / 4;
                    l += s * Math.cos(a) * 1.41; rr += s * Math.sin(a) * 1.41;
                    if (vo.done()) voices.remove(v);
                }
                double mono = (l + rr) * 0.5;
                double wl = revL.process(mono * 0.8), wr = revR.process(mono * 0.8);
                double k = volume * 1.35 * (m == 2 ? 0.8 : m == 4 ? 0.7 : 1.0);
                int sl = (int) (Math.tanh((l * 0.7 + wl * 0.9) * 0.9) * k * 30000), sr = (int) (Math.tanh((rr * 0.7 + wr * 0.9) * 0.9) * k * 30000);
                out[4 * i] = (byte) (sl & 0xff); out[4 * i + 1] = (byte) ((sl >> 8) & 0xff);
                out[4 * i + 2] = (byte) (sr & 0xff); out[4 * i + 3] = (byte) ((sr >> 8) & 0xff);
            }
        }
    }

    /** For tests: `seconds` of music as 16-bit little-endian MONO samples (the two channels averaged). */
    static byte[] renderForTest(int seconds, long seed, int moodValue) {
        byte[] st = renderStereo(seconds, seed, moodValue);
        byte[] mono = new byte[st.length / 2];
        for (int i = 0; i < mono.length / 2; i++) {
            int l = (short) ((st[4 * i] & 0xff) | (st[4 * i + 1] << 8)), r = (short) ((st[4 * i + 2] & 0xff) | (st[4 * i + 3] << 8)), s = (l + r) / 2;
            mono[2 * i] = (byte) (s & 0xff); mono[2 * i + 1] = (byte) ((s >> 8) & 0xff);
        }
        return mono;
    }

    /** For tests and for exporting: stereo 16-bit little-endian samples at `SR`. */
    static byte[] renderStereo(int seconds, long seed, int moodValue) {
        Composer c = new Composer(seed);
        c.mood = moodValue;
        c.newPiece(false);
        Synth s = new Synth(c);
        int total = (int) (SR * seconds);
        byte[] all = new byte[total * 4];
        byte[] blk = new byte[2048 * 4];
        for (int done = 0; done < total; done += 2048) {
            int n = Math.min(2048, total - done);
            s.fill(blk, n, moodValue, 1.0);
            System.arraycopy(blk, 0, all, done * 4, n * 4);
        }
        return all;
    }

    // ------------------------------------------------------------------ the loop
    private static void loop() {
        File sf = null;
        if ("soundfont".equalsIgnoreCase(Settings.musicEngine) || "auto".equalsIgnoreCase(Settings.musicEngine)) {
            List<String> fs = fonts();
            String want = Settings.soundFont == null ? "" : Settings.soundFont;
            if (!fs.isEmpty()) sf = new File("soundfonts", fs.contains(want) ? want : fs.get(0));
            else if (!"auto".equalsIgnoreCase(Settings.musicEngine)) status = "no soundfont found";
        }
        fallback = "";
        if (sf != null && midiLoop(sf)) return;
        if (sf != null) fallback = " (SoundFont failed: " + MidiOut.lastError + ")";
        synthLoop();
    }

    private static void synthLoop() {
        SourceDataLine line = null;
        try {
            AudioFormat fmt = new AudioFormat(SR, 16, 2, true, false);
            line = AudioSystem.getSourceDataLine(fmt);
            line.open(fmt, 8192 * 4);
            line.start();
        } catch (Throwable e) { // no sound card, headless, or the device is busy
            status = "no audio device";
            running = false;
            return;
        }
        status = "playing";
        engine = "built-in" + fallback;
        Composer comp = new Composer(System.nanoTime() ^ 0x5DEECE66DL);
        comp.mood = mood;
        comp.newPiece(false);
        Synth syn = new Synth(comp);
        byte[] out = new byte[2048 * 4];
        try {
            while (running) {
                if (reseed) { reseed = false; comp.mood = mood; comp.reseed(System.nanoTime(), syn.clock); }
                syn.fill(out, 2048, mood, volume);
                line.write(out, 0, out.length);
            }
        } catch (Throwable e) {
            status = "stopped";
        } finally {
            try { line.stop(); line.close(); } catch (Throwable ignored) { }
            if (!running) status = "off";
            running = false;
        }
    }

    /** The same compositions played by the Java software synthesiser with a SoundFont. Returns false when it could not start (the built-in synth takes over). */
    private static boolean midiLoop(File sf) {
        MidiOut out = MidiOut.open(sf, GM);
        if (out == null) { status = "soundfont failed: " + MidiOut.lastError; return false; }
        status = "playing";
        engine = sf.getName();
        Composer comp = new Composer(System.nanoTime() ^ 0x5DEECE66DL);
        comp.mood = mood;
        comp.newPiece(false);
        long t0 = System.nanoTime();
        List<long[]> offs = new ArrayList<>(); // {sample, channel, midi}
        double lastVol = -1; double[] lastLayer = new double[layer.length];
        try {
            while (running) {
                long now = (long) ((System.nanoTime() - t0) / 1e9 * SR);
                if (reseed) { reseed = false; comp.mood = mood; comp.reseed(System.nanoTime(), now); out.allOff(); offs.clear(); }
                comp.mood = mood;
                if (volume != lastVol || !Arrays.equals(lastLayer, layer)) { lastVol = volume; lastLayer = layer.clone(); out.volumes(volume, layer); }
                for (Note n = comp.poll(now); n != null; n = comp.poll(now)) {
                    out.on(n.inst, n.midi, n.vel);
                    offs.add(new long[]{n.at + (long) (n.dur * SR), n.inst, n.midi});
                }
                for (int i = offs.size() - 1; i >= 0; i--) if (offs.get(i)[0] <= now) { long[] o = offs.remove(i); out.off((int) o[1], (int) o[2]); }
                Thread.sleep(8);
            }
        } catch (InterruptedException ignored) {
        } catch (Throwable e) {
            status = "stopped";
        } finally {
            out.close();
            if (!running) status = "off";
            running = false;
        }
        return true;
    }

    /** One call from the GUI: picks the mood from the player's tile and the season. */
    static void followWorld(Character p) {
        if (!running || p == null || p.location == null) return;
        Area a = p.location;
        season = GameDriver.currentSeason;
        int m = 0;
        if (a.crowdHotspot) m = 4;
        else if (a.hotspot || a.dungeonType != null || a.monsters > 3000) m = 2;
        else if (a.isOriginis()) m = 6;
        else if (a.sect != null) m = 5;
        else if (a.city) m = 1;
        else if (GameDriver.currentSeason == 3) m = 3;
        mood = m;
    }
}
