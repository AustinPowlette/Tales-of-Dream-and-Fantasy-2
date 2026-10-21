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

/** The game's music (no audio files). The songs are text scores (`SongBook`, `Score`) played by the `Conductor`, which picks a song for the place, brings its
 *  layers in and out with the mood, fades between songs and rests between them; the old generator (`Composer`, a random pentatonic piece in four sections) now only
 *  plays when no song is available.
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
    /** The song now playing (null in a rest): `music keep` saves it when it was composed. */
    static volatile Score nowScore = null;
    /** Which engine is playing: "built-in" or the SoundFont's name. */
    static volatile String engine = "built-in";
    static volatile int season = 0;

    // instruments (also the MIDI channel order; channel 9 is skipped, it is the drum kit)
    static final int PLUCK = 0, FLUTE = 1, ERHU = 2, PAD = 3, BELL = 4, CHIME = 5, TAIKO = 6, WOOD = 7, PIPA = 8, PIANO = 9, HARP = 10, STRINGS = 11, CELLO = 12, HORN = 13, CHOIR = 14,
        KIT = 15, SUONA = 16, YANGQIN = 17, SHENG = 18, XUN = 19, XIAO = 20, MARIMBA = 21, TIMPANI = 22, UPRIGHT = 23;
    /** `kit` is a whole drum kit on the MIDI drum channel: the pitch of a note picks the drum (see `Score.DRUMS`: kick snare hat tamb shaker clap bongo conga ...). The others are melodic. */
    static final String[] INST = {"pluck", "flute", "erhu", "pad", "bell", "chime", "taiko", "wood", "pipa", "piano", "harp", "strings", "cello", "horn", "choir",
        "kit", "suona", "yangqin", "sheng", "xun", "xiao", "marimba", "timpani", "upright"};
    /** Other names the song scores may use for the instruments. */
    private static final String[][] ALIASES = {{"koto", "0"}, {"guzheng", "0"}, {"dizi", "1"}, {"fiddle", "2"}, {"bells", "4"}, {"timp", "22"}, {"shamisen", "8"}, {"voices", "14"}, {"violin", "2"}, {"drums", "15"}, {"percussion", "15"}, {"shawm", "16"}, {"dulcimer", "17"}, {"ocarina", "19"}, {"shakuhachi", "20"}, {"bass", "23"}};
    /** Per-layer loudness 0..1.5 (Settings.musicMix: "pluck=80,flute=100,..."). */
    static final double[] layer = new double[INST.length];
    static { java.util.Arrays.fill(layer, 1.0); }
    static final int[] GM = {107, 73, 110, 89, 14, 98, 116, 115, 106, 0, 46, 48, 42, 60, 53, // koto, flute, fiddle, warm pad, tubular bells, crystal, taiko, woodblock, shamisen, piano, harp, strings, cello, horn, voice oohs
        0, 111, 15, 20, 79, 77, 12, 47, 32};                                                  // (drum kit: the MIDI drum channel), shehnai (a shawm: the suona), dulcimer (the yangqin), reed organ (the sheng), ocarina (the xun), shakuhachi (the xiao), marimba, timpani, acoustic bass

    /** The index of an instrument by name or alias, or -1. */
    static int instIndex(String name) {
        String n = name.trim().toLowerCase();
        for (int i = 0; i < INST.length; i++) if (INST[i].equals(n)) return i;
        for (String[] a : ALIASES) if (a[0].equals(n)) return Integer.parseInt(a[1]);
        return -1;
    }

    // ------------------------------------------------------------------ lifecycle
    // One control thread does every start and stop in order, and a stop waits for the old music thread to end, so there is never more than one engine
    // (the old code started the new thread while the old one was still closing; the old one's last act then switched the new one off, and quick Off/On clicks left several playing).
    private static final java.util.concurrent.ExecutorService control = java.util.concurrent.Executors.newSingleThreadExecutor(r -> { Thread t = new Thread(r, "music-control"); t.setDaemon(true); return t; });
    private static volatile int generation = 0;   // a music thread of an older generation ends and never touches the shared state again

    /** Starts or stops the music to match Settings (a changed engine restarts it). Returns at once: the work is done on the control thread, in order. */
    static void apply() {
        try { control.execute(Ambient::applyNow); } catch (RuntimeException e) { applyNow(); }
    }

    /** Waits until everything asked of the music so far has been done (tests, `music status`). */
    static void settle() {
        try { control.submit(() -> { }).get(20, java.util.concurrent.TimeUnit.SECONDS); } catch (Exception ignored) { }
    }

    private static void applyNow() {
        try {
            volume = Math.max(0, Math.min(100, Settings.musicVolume)) / 100.0;
            parseMix(Settings.musicMix);
            String want = Settings.musicEngine + "|" + Settings.soundFont;
            Thread cur = thread;
            if (cur != null && cur.isAlive() && running && !want.equals(startedWith)) stopNow();
            if (Settings.music) { startedWith = want; startNow(); } else stopNow();
        } catch (Throwable ignored) { }
    }

    private static void startNow() {
        Thread old = thread;
        if (old != null && old.isAlive() && running) return;
        if (old != null && old.isAlive()) { try { old.join(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }
        final int gen = ++generation;
        running = true;
        Thread t = new Thread(() -> loop(gen), "ambient-music");
        t.setDaemon(true);
        t.setPriority(Thread.NORM_PRIORITY);
        thread = t;
        t.start();
    }

    private static void stopNow() {
        Thread t = thread;
        running = false; generation++;
        thread = null;
        status = "off";
        nowPlaying = "";
        if (t != null) { t.interrupt(); try { t.join(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }
    }

    /** Music threads alive right now (should be 0 or 1). */
    static int threadsAlive() {
        int n = 0;
        for (Thread t : Thread.getAllStackTraces().keySet()) if ("ambient-music".equals(t.getName()) && t.isAlive()) n++;
        return n;
    }

    // what the SoundFont engine reports about itself (`music status`)
    static volatile String tuning = "";
    static volatile long notesSent = 0, notesLate = 0, maxGapMs = 0;
    static volatile int maxVoices = 0;

    /** A few lines on how the music is doing: engine, threads, how the synthesiser was set up, notes sent late, the longest pause of the note loop, the most voices used. */
    static String statusReport() {
        return "Music " + (Settings.music ? "on" : "off") + ", " + status + ", engine: " + engine + (tuning.isEmpty() ? "" : " (" + tuning + ")") + ".\n"
            + "Music threads alive: " + threadsAlive() + " (must be 1 while it plays).  Notes sent: " + notesSent + ", sent too late to be on time: " + notesLate + ".  Longest pause of the note loop: " + maxGapMs + " ms (the look-ahead is " + LOOK_MS + " ms).  Most voices used at once: " + maxVoices + ".";
    }

    static final int LOOK_MS = 250; // notes are handed to the synthesiser this far ahead of their time, each with its own time stamp: a late wake-up of the loop no longer moves a note

    private static volatile String startedWith = "", fallback = "";

    private static volatile Score concertReq = null;
    private static volatile int concertLeadReq = -1, concertLevelReq = 5;
    static boolean isRunning() { return running; }
    /** A concert: plays `piece` once with the instrument number `lead` as its lead voice, at the pace and with the ornaments of a musician of grade `level`; ignored while another concert is playing or the music is off. */
    static void concert(Score piece, int lead, int level) {
        if (!running || piece == null) return;
        Score now = nowScore;
        if (now != null && now.concert) return;
        concertLeadReq = lead; concertLevelReq = level; concertReq = piece;
    }

    static void setMood(int m) { mood = m; }
    static int currentMood() { return mood; }
    /** The next song now (the Settings button, `music new`). */
    static void newPiece() { reseed = true; }
    private static volatile Score pinned = null;
    /** Plays only this song (over and over, with short rests) until `pin(null)` hands the choice back to the places; the change takes effect at once. */
    static void pin(Score s) { pinned = s; reseed = true; }
    static Score pinned() { return pinned; }

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
        /** Which track of a song wrote the note (-1: none), for the preview tool's checks. */
        int track = -1;
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

    /** A struck wooden bar (marimba): the fundamental and a bright overtone at about four times its pitch that dies faster. */
    private static final class Mallet extends Voice {
        final double f, amp; int t = 0; double ph = 0, ph2 = 0, ph3 = 0;
        Mallet(double f, double amp) { this.f = f; this.amp = amp; }
        double next() {
            double sec = t / (double) SR, tau = 0.55 * Math.pow(261.6 / Math.max(80, f), 0.35);
            ph += 2 * Math.PI * f / SR; ph2 += 2 * Math.PI * f * 3.93 / SR; ph3 += 2 * Math.PI * f * 9.2 / SR;
            double s = Math.sin(ph) * Math.exp(-sec / tau) + 0.45 * Math.sin(ph2) * Math.exp(-sec / (tau * 0.28)) + 0.12 * Math.sin(ph3) * Math.exp(-sec / (tau * 0.08));
            t++;
            return s * amp * Math.min(1, t / 12.0);
        }
        boolean done() { return t > SR * 2.0; }
    }

    /** A tuned kettle drum: a sine that settles from a little above its pitch, a soft second partial, a thump of noise at the start. */
    private static final class Timp extends Voice {
        final double f, amp; int t = 0; double ph = 0, ph2 = 0; final Random r;
        Timp(double f, double amp, Random r) { this.f = f; this.amp = amp; this.r = r; }
        double next() {
            double sec = t / (double) SR;
            double fc = f * (1 + 0.22 * Math.exp(-sec * 18));
            ph += 2 * Math.PI * fc / SR; ph2 += 2 * Math.PI * fc * 1.5 / SR;
            double s = Math.sin(ph) * Math.exp(-sec / 0.85) + 0.25 * Math.sin(ph2) * Math.exp(-sec / 0.4) + (r.nextDouble() * 2 - 1) * 0.25 * Math.exp(-sec * 90);
            t++;
            return s * amp * Math.min(1, t / 15.0);
        }
        boolean done() { return t > SR * 2.2; }
    }

    /** One piece of the drum kit, made from sine sweeps, filtered noise and a few ringing partials: `note` is the General MIDI drum number (36 kick, 38 snare, 42 hat, 54 tambourine, 70 maracas, 75 claves, ...). */
    private static final class Kit extends Voice {
        final int note; final double amp; final Random r; int t = 0; double ph = 0, ph2 = 0, lp = 0, lp2 = 0; final double len;
        /** The built-in pieces differ in loudness too: a correction in dB for each drum number 35..81 (measured one by one). */
        static final double[] GAIN_DB = {-6.6, -6.4, -1.4, -3.9, -4.2, -3.2, -2.1, 2.8, -2.2, 2.6, -3.0, -2.7, -3.3, 0.0, -4.3, -4.2, 2.2, -3.5, -1.3, 3.6, -3.5, -2.1, -4.7, 0.0, 2.4, -0.8, -1.7, -0.7, -2.5, -3.1, -3.0, -3.8, -5.5, -6.8, 6.0, 5.5, 0.0, 0.0, 2.3, 0.9, 0.8, -3.1, -2.2, 0.0, 0.0, 4.1, 2.9};
        Kit(int note, double amp, Random r) {
            this.note = note; this.amp = amp * (note >= 35 && note <= 81 ? Math.pow(10, GAIN_DB[note - 35] / 20) : 1); this.r = r;
            len = SR * (note == 49 || note == 57 ? 2.0 : note == 52 || note == 81 || note == 51 || note == 53 ? 2.4 : note == 46 ? 0.6 : note == 80 ? 0.3 : 0.9);
        }
        private double noise() { return r.nextDouble() * 2 - 1; }
        /** High-passed noise (a one-pole low-pass taken away). */
        private double hiss(double k) { double n = noise(); lp += (n - lp) * k; return n - lp; }
        double next() {
            double sec = t / (double) SR, s = 0;
            switch (note) {
                case 35: case 36: { // kick: a fast pitch drop and a click
                    double fc = 48 + 95 * Math.exp(-sec * 34); ph += 2 * Math.PI * fc / SR;
                    s = Math.sin(ph) * Math.exp(-sec / 0.16) + noise() * 0.22 * Math.exp(-sec * 220); break; }
                case 37: // rim shot
                    ph += 2 * Math.PI * 1750 / SR; ph2 += 2 * Math.PI * 480 / SR;
                    s = Math.sin(ph) * Math.exp(-sec / 0.012) * 0.7 + Math.sin(ph2) * Math.exp(-sec / 0.03) * 0.5 + hiss(0.2) * 0.35 * Math.exp(-sec / 0.01); break;
                case 38: case 40: { // snare: a body tone and a rattle of noise
                    ph += 2 * Math.PI * (185 + 60 * Math.exp(-sec * 40)) / SR;
                    s = Math.sin(ph) * Math.exp(-sec / 0.07) * 0.55 + hiss(0.12) * Math.exp(-sec / 0.13) * 0.85; break; }
                case 39: { // hand clap: three quick bursts and a tail
                    double burst = sec < 0.033 ? Math.exp(-(sec % 0.011) / 0.004) : 0, e = burst + Math.exp(-sec / 0.11) * 0.5;
                    lp2 += (noise() - lp2) * 0.35;
                    s = (lp2 - lp) * e * 2.2; lp += (lp2 - lp) * 0.05; break; }
                case 41: case 43: case 45: case 47: case 48: case 50: { // toms
                    double base = note == 41 ? 78 : note == 43 ? 98 : note == 45 ? 120 : note == 47 ? 148 : note == 48 ? 178 : 210;
                    ph += 2 * Math.PI * base * (1 + 0.55 * Math.exp(-sec * 22)) / SR;
                    s = Math.sin(ph) * Math.exp(-sec / 0.2) + noise() * 0.15 * Math.exp(-sec * 160); break; }
                case 42: case 44: s = hiss(0.35) * Math.exp(-sec / 0.03) * 0.9; break; // closed and pedal hat
                case 46: s = hiss(0.3) * Math.exp(-sec / 0.26) * 0.8; break;           // open hat
                case 49: case 57: case 55: // crash, splash
                    s = hiss(0.25) * Math.exp(-sec / (note == 55 ? 0.35 : 0.85)) * 0.8 + metal(sec, 420, 0.5) * 0.25; break;
                case 51: case 59: s = metal(sec, 560, 0.45) * 0.55 + hiss(0.5) * Math.exp(-sec / 0.05) * 0.3; break; // ride
                case 53: s = metal(sec, 760, 0.7) * 0.6; break;                                                        // ride bell
                case 52: s = metal(sec, 330, 1.0) * 0.6 + hiss(0.3) * Math.exp(-sec / 0.5) * 0.35; break;             // Chinese cymbal: here a small gong
                case 54: s = hiss(0.5) * Math.exp(-sec / 0.07) * 0.55 + metal(sec, 5200, 0.12) * 0.25; break;          // tambourine
                case 56: // cowbell
                    ph += 2 * Math.PI * 562 / SR; ph2 += 2 * Math.PI * 845 / SR;
                    s = (Math.signum(Math.sin(ph)) * 0.35 + Math.signum(Math.sin(ph2)) * 0.25) * Math.exp(-sec / 0.12); break;
                case 60: case 61: { double f = note == 60 ? 360 : 270; ph += 2 * Math.PI * f * (1 + 0.15 * Math.exp(-sec * 50)) / SR; s = Math.sin(ph) * Math.exp(-sec / 0.09) + noise() * 0.2 * Math.exp(-sec * 150); break; } // bongos
                case 62: case 63: case 64: { // congas: mute, open, low
                    double f = note == 62 ? 265 : note == 63 ? 235 : 165, tau = note == 62 ? 0.06 : note == 63 ? 0.2 : 0.24;
                    ph += 2 * Math.PI * f * (1 + 0.12 * Math.exp(-sec * 40)) / SR; s = Math.sin(ph) * Math.exp(-sec / tau) + noise() * 0.18 * Math.exp(-sec * 140); break; }
                case 65: case 66: { double f = note == 65 ? 430 : 320; ph += 2 * Math.PI * f / SR; ph2 += 2 * Math.PI * f * 2.7 / SR; s = (Math.sin(ph) + 0.4 * Math.sin(ph2)) * Math.exp(-sec / 0.14); break; } // timbales
                case 67: case 68: { double f = note == 67 ? 910 : 680; ph += 2 * Math.PI * f / SR; ph2 += 2 * Math.PI * f * 1.51 / SR; s = (Math.sin(ph) + 0.5 * Math.sin(ph2)) * Math.exp(-sec / 0.18); break; } // agogo
                case 69: s = hiss(0.6) * Math.exp(-sec / 0.055) * 0.7; break;                                           // cabasa
                case 70: s = hiss(0.45) * Math.exp(-sec / 0.04) * (1 - Math.exp(-sec / 0.006)) * 0.9; break;            // maracas: a soft attack
                case 73: case 74: { double gate = (Math.sin(2 * Math.PI * (note == 73 ? 38 : 30) * sec) > 0) ? 1 : 0.15; s = hiss(0.4) * gate * Math.exp(-sec / (note == 73 ? 0.09 : 0.3)) * 0.8; break; } // guiro
                case 75: ph += 2 * Math.PI * 2500 / SR; s = Math.sin(ph) * Math.exp(-sec / 0.022); break;               // claves
                case 76: case 77: { double f = note == 76 ? 1100 : 820; ph += 2 * Math.PI * f / SR; ph2 += 2 * Math.PI * f * 1.6 / SR; s = (Math.sin(ph) + 0.5 * Math.sin(ph2)) * Math.exp(-sec / 0.04); break; } // wood blocks
                case 80: case 81: s = metal(sec, 5600, note == 80 ? 0.2 : 1.4) * 0.5; break;                           // triangle
                default: s = hiss(0.4) * Math.exp(-sec / 0.05) * 0.6;
            }
            t++;
            return s * amp * Math.min(1, t / 6.0);
        }
        /** A few inharmonic partials that ring for about `tau` seconds. */
        private double metal(double sec, double f, double tau) {
            double s = 0, e = Math.exp(-sec / tau);
            double[] ratio = {1, 1.34, 1.9, 2.5, 3.1};
            for (int i = 0; i < ratio.length; i++) s += Math.sin(2 * Math.PI * f * ratio[i] * sec) * e / (1 + i * 0.5);
            return s * 0.5;
        }
        boolean done() { return t >= len; }
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
    private static final double[] BRIGHT_H = {1, 0.6, 0.5, 0.35, 0.25, 0.18, 0.12, 0.08}, FLUTE_H = {1, 0.22, 0.07}, PAD_H = {1, 0.16, 0.05}, DRONE_H = {1, 0.5},
        CELLO_H = {1, 0.62, 0.42, 0.26, 0.15, 0.08}, HORN_H = {1, 0.55, 0.34, 0.2, 0.1}, CHOIR_H = {1, 0.3, 0.12, 0.06}, STRING_H = {1, 0.5, 0.33, 0.22, 0.14, 0.09},
        SUONA_H = {1, 0.9, 0.75, 0.6, 0.45, 0.32, 0.2, 0.12, 0.07}, SHENG_H = {1, 0.55, 0.45, 0.3, 0.22, 0.15, 0.08}, XUN_H = {1, 0.1, 0.03}, XIAO_H = {1, 0.18, 0.06};

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
        final Conductor comp;
        final List<Voice> voices = new ArrayList<>();
        final Reverb revL = new Reverb(new int[]{1557, 1617, 1491, 1422, 1277}, new int[]{556, 441}), revR = new Reverb(new int[]{1603, 1531, 1459, 1379, 1319}, new int[]{571, 463});
        long clock = 0; final Random r = new Random(77);

        Synth(Conductor c) { comp = c; }

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
                case PIANO: v = new Pluck(f, 0.30 * n.vel, 2.4, 0.5, r); break;
                case HARP: v = new Pluck(f, 0.27 * n.vel, 2.6, 0.38, r); break;
                case STRINGS: v = new Tone(f * (1 + (r.nextDouble() - 0.5) * 0.003), 0, 0.085 * n.vel, n.dur, 0.35, 0.55, 0.0035, 0.004, STRING_H, r); break;
                case CELLO: v = new Tone(f, 0, 0.13 * n.vel, n.dur, 0.12, 0.3, 0.006, 0.003, CELLO_H, r); break;
                case HORN: v = new Tone(f, 0, 0.12 * n.vel, n.dur, 0.1, 0.35, 0.002, 0.01, HORN_H, r); break;
                case CHOIR: v = new Tone(f, 0, 0.075 * n.vel, n.dur, 0.45, 0.7, 0.003, 0.012, CHOIR_H, r); break;
                case TAIKO: v = new Drum(f, 0.42 * n.vel, false, r); break;
                case KIT: v = new Kit(n.midi, 0.36 * n.vel, r); break;
                case SUONA: v = new Tone(f, 0, 0.10 * n.vel, n.dur, 0.04, 0.12, 0.012, 0.01, SUONA_H, r); break;
                case YANGQIN: v = new Pluck(f, 0.28 * n.vel, 0.8, 0.22, r); break;
                case SHENG: v = new Tone(f, 0, 0.09 * n.vel, n.dur, 0.08, 0.15, 0.002, 0.004, SHENG_H, r); break;
                case XUN: v = new Tone(f, 0, 0.12 * n.vel, n.dur, 0.1, 0.3, 0.004, 0.035, XUN_H, r); break;
                case XIAO: v = new Tone(f, 0, 0.12 * n.vel, n.dur, 0.12, 0.35, 0.006, 0.045, XIAO_H, r); break;
                case MARIMBA: v = new Mallet(f, 0.30 * n.vel); break;
                case TIMPANI: v = new Timp(f, 0.42 * n.vel, r); break;
                case UPRIGHT: v = new Pluck(f, 0.32 * n.vel, 0.7, 0.75, r); break;
                default: v = new Drum(f * 6, 0.22 * n.vel, true, r);
            }
            v.pan = n.pan;
            v.gain = layer[n.inst];
            voices.add(v);
        }

        /** Fills `out` with n frames of 16-bit little-endian stereo. */
        void fill(byte[] out, int n, int m, double volume) {
            comp.mood = m;
            if (comp.takeCut()) voices.clear();
            for (int i = 0; i < n; i++, clock++) {
                for (Note nt = comp.poll(clock); nt != null; nt = comp.poll(clock)) spawn(nt);
                double master = comp.masterAt(clock);
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
                double k = volume * master * 1.35 * (m == 2 ? 0.8 : m == 4 ? 0.7 : 1.0);
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
        Conductor c = new Conductor(seed);
        c.testMode = true;
        c.mood = moodValue;
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

    /** For the preview tool: one whole song with every optional part, as stereo 16-bit little-endian samples (at most `maxSeconds`, plus the last notes dying away). */
    static byte[] renderScore(Score sc, int moodValue, long seed, int maxSeconds) {
        Conductor c = new Conductor(seed);
        c.testMode = true; c.mood = moodValue; c.forced = sc; c.once = true; c.forcedMode = "full";
        Synth s = new Synth(c);
        java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
        byte[] blk = new byte[2048 * 4];
        long limit = (long) (SR * maxSeconds);
        while (s.clock < limit && !c.finished()) { s.fill(blk, 2048, moodValue, 1.0); bo.write(blk, 0, blk.length); }
        return bo.toByteArray();
    }

    // ------------------------------------------------------------------ the loop
    private static void loop(int gen) {
        File sf = null;
        if ("soundfont".equalsIgnoreCase(Settings.musicEngine) || "auto".equalsIgnoreCase(Settings.musicEngine)) {
            List<String> fs = fonts();
            String want = Settings.soundFont == null ? "" : Settings.soundFont;
            if (!fs.isEmpty()) sf = new File("soundfonts", fs.contains(want) ? want : fs.get(0));
            else if (!"auto".equalsIgnoreCase(Settings.musicEngine)) status = "no soundfont found";
        }
        fallback = "";
        if (sf != null && midiLoop(sf, gen)) return;
        if (gen != generation) return;
        if (sf != null) fallback = " (SoundFont failed: " + MidiOut.lastError + ")";
        synthLoop(gen);
    }

    private static void synthLoop(int gen) {
        SourceDataLine line = null;
        try {
            AudioFormat fmt = new AudioFormat(SR, 16, 2, true, false);
            line = AudioSystem.getSourceDataLine(fmt);
            line.open(fmt, 8192 * 4);
            line.start();
        } catch (Throwable e) { // no sound card, headless, or the device is busy
            if (gen == generation) { status = "no audio device"; running = false; }
            return;
        }
        status = "playing";
        engine = "built-in" + fallback;
        tuning = "";
        Conductor comp = new Conductor(System.nanoTime() ^ 0x5DEECE66DL);
        comp.mood = mood;
        Synth syn = new Synth(comp);
        byte[] out = new byte[2048 * 4];
        try {
            while (running && gen == generation) {
                if (comp.forced != pinned) comp.forced = pinned;
                if (reseed) { reseed = false; comp.skipPiece(); }
                { Score cr = concertReq; if (cr != null) { concertReq = null; comp.concert(cr, concertLeadReq, concertLevelReq); } }
                syn.fill(out, 2048, mood, volume);
                line.write(out, 0, out.length);
            }
        } catch (Throwable e) {
            if (gen == generation) status = "stopped";
        } finally {
            try { line.stop(); line.close(); } catch (Throwable ignored) { }
            if (gen == generation) { if (!running) status = "off"; running = false; }
        }
    }

    /** The same compositions played by the Java software synthesiser with a SoundFont. Returns false when it could not start (the built-in synth takes over).
     *  The song clock is the synthesiser's own audio clock, and every note is sent LOOK_MS ahead with the time stamp of its start and its end: the synthesiser plays it sample-exactly
     *  whatever the game thread is doing at that moment (before, the notes were struck from an 8 ms sleep loop and arrived late and in clumps while the cores were busy). */
    private static boolean midiLoop(File sf, int gen) {
        MidiOut out = MidiOut.open(sf, GM);
        if (out == null) { status = "soundfont failed: " + MidiOut.lastError; return false; }
        if (gen != generation) { out.close(); return true; }
        status = "playing";
        engine = sf.getName();
        tuning = MidiOut.tuning;
        notesSent = 0; notesLate = 0; maxGapMs = 0; maxVoices = 0;
        Conductor comp = new Conductor(System.nanoTime() ^ 0x5DEECE66DL);
        comp.mood = mood;
        final long base = out.micros(); // the synthesiser's clock at sample 0 of the song clock
        List<long[]> offs = new ArrayList<>(); // {sample, channel, midi}
        double lastVol = -1, lastMaster = -1; double[] lastLayer = new double[layer.length];
        long lastWake = System.nanoTime(), lastVoiceCheck = 0;
        try {
            while (running && gen == generation) {
                long wake = System.nanoTime();
                maxGapMs = Math.max(maxGapMs, (wake - lastWake) / 1_000_000L);
                lastWake = wake;
                long nowUs = out.micros() - base;                                   // song time now, from the audio clock
                long now = (long) (nowUs / 1e6 * SR);                               // the sample being heard
                long horizon = (long) ((nowUs + LOOK_MS * 1000L) / 1e6 * SR);       // notes up to here are handed over now
                if (comp.forced != pinned) comp.forced = pinned;
                if (reseed) { reseed = false; comp.skipPiece(); }
                { Score cr = concertReq; if (cr != null) { concertReq = null; comp.concert(cr, concertLeadReq, concertLevelReq); } }
                comp.mood = mood;
                for (Note n = comp.poll(horizon); n != null; n = comp.poll(horizon)) {
                    long at = base + (long) (n.at / SR * 1e6);
                    int c = out.on(n.inst, n.midi, n.vel, at);
                    notesSent++; if (n.at < now) notesLate++;
                    for (int i = offs.size() - 1; i >= 0; i--) if (offs.get(i)[1] == c && offs.get(i)[2] == n.midi) offs.remove(i); // a struck note again: the new one's end ends both
                    offs.add(new long[]{n.at + (long) (n.dur * SR), c, n.midi});
                }
                for (int i = offs.size() - 1; i >= 0; i--) if (offs.get(i)[0] <= horizon) { long[] o = offs.remove(i); out.off((int) o[1], (int) o[2], base + (long) (o[0] / SR * 1e6)); }
                if (comp.takeCut()) { out.allOff(); offs.clear(); }
                double master = comp.masterAt(now);
                if (volume != lastVol || Math.abs(master - lastMaster) > 0.01 || !Arrays.equals(lastLayer, layer)) { lastVol = volume; lastMaster = master; lastLayer = layer.clone(); out.volumes(volume * master, layer); }
                if (wake / 1_000_000L - lastVoiceCheck >= 250) { lastVoiceCheck = wake / 1_000_000L; maxVoices = Math.max(maxVoices, out.activeVoices()); }
                Thread.sleep(15);
            }
        } catch (InterruptedException ignored) {
        } catch (Throwable e) {
            if (gen == generation) { status = "stopped"; MidiOut.lastError = "the note loop failed: " + e; if (System.getProperty("tdf.debug") != null) e.printStackTrace(); }
        } finally {
            out.close();
            if (gen == generation) { if (!running) status = "off"; running = false; }
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
