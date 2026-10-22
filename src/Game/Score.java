package Game;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;

/** A song written as text (see `SongBook` for the songs and `Game Info.md` "Music" for the format), parsed into bars of note events.
 *
 *  Header:  title / bpm / beats (per bar) / shift (lowest highest, in semitones) / moods (mood:weight ...) / form (section[?][:dynamic] ...)
 *  pattern NAME: tone:duration ...   (tones 1 3 5 7 9 b = root, third, fifth, seventh, ninth, bass; ' = octave up, "," = down; a+b plays together;
 *                                     r = rest; duration * = the rest of the chord)
 *  track NAME role=lead|counter|harmony|bass|rhythm|color|perc|drive inst=a|b vel= pan= ctr=D3 pat=NAME p=percent all
 *  [Section] or [Section < Parent]; then `chords: D | Bm7 | G A` (a bar may hold several chords) and one line per track:
 *    explicit track:      `lead: A4:1.5 B4:.5 D5:2 | ...`   notes name+octave, `:beats`, r rest, [C3 E3] chord, d=.5 default length, mf/p/f dynamics,
 *                         modifiers after the pitch  > accent  ' short  _ legato  ~ tie   s strum, % repeat the bar, (..)*n repeat a group
 *    chord-driven track:  `harp: arpA`  (or `on`, `-`; `@5-8` bars, `x.8` louder/softer, `o+1` octave)
 *  A line may name an instrument for that section only:  `lead(erhu): ...`. Lines starting with `//` are comments, `|` at the start continues the line above. */
final class Score {
    static final String[] ROLES = {"lead", "counter", "harmony", "bass", "rhythm", "color", "perc", "drive"};
    static final String[] DYN_WORDS = {"pp", "p", "mp", "mf", "f", "ff"};
    static final double[] DYN_VALUES = {0.49, 0.67, 0.83, 1.0, 1.15, 1.28};

    static int roleIndex(String r) { for (int i = 0; i < ROLES.length; i++) if (ROLES[i].equalsIgnoreCase(r)) return i; return -1; }

    static double dynValue(String w) {
        for (int i = 0; i < DYN_WORDS.length; i++) if (DYN_WORDS[i].equals(w)) return DYN_VALUES[i];
        return -1;
    }

    // ------------------------------------------------------------------ the model
    String title = "Untitled";
    double bpm = 72, beats = 4;
    int shiftMin = 0, shiftMax = 0;
    /** `swing: 0.15`: every note that falls on the second half of a beat is played that fraction of a beat late (0 = straight, 0.33 = a triplet swing): the groove of a song. */
    double swing = 0;
    /** `layers: fixed`: the song is written for its own mood and plays every track whatever the mood (no thinning, no drive layer). */
    boolean fixedLayers = false;
    /** `kind: concert`: a piece for the Musicians' concerts (`Concerts`): not part of the music the places choose from; its lead voice is the musician's instrument. */
    boolean concert = false;
    /** The text this song was parsed from (to save it), where it came from ("gen road 12345" for a composed one), and whether it is a kept song of the songs/ folder. */
    String[] lines;
    String source = "";
    boolean kept = false;
    String file = null;
    final List<String> problems = new ArrayList<String>();
    final Map<Integer, Double> moods = new LinkedHashMap<Integer, Double>();
    final List<Track> tracks = new ArrayList<Track>();
    final Map<String, Track> trackByName = new LinkedHashMap<String, Track>();
    final Map<String, Pat> patterns = new LinkedHashMap<String, Pat>();
    final Map<String, Section> sections = new LinkedHashMap<String, Section>();
    final List<FormEntry> form = new ArrayList<FormEntry>();

    static final class Track {
        String name, role = "lead", pat;
        int index, roleIdx = 0, ctr = 60, chance = 100;
        int[] insts = {0};
        double vel = 1, pan = 0;
        boolean everywhere;
        boolean chordDriven() { return pat != null; }
    }

    /** A tone of a chord in a pattern: `near` (written =) puts it at the pitch nearest the track's centre instead of stacking it above the root (smooth pads). */
    static final class Tone {
        final char kind; final int oct, abs; final boolean near;
        Tone(char k, int o, boolean n) { kind = k; oct = o; near = n; abs = 0; }
        /** A fixed pitch (written @D2 in a pattern): drums keep one pitch whatever the chord. */
        Tone(int midi) { kind = 'n'; oct = 0; near = false; abs = midi; }
    }

    static final class PNote { final List<Tone> tones = new ArrayList<Tone>(); double dur; boolean rest; double vel = 1; }

    static final class Pat {
        String name; final List<PNote> notes = new ArrayList<PNote>(); boolean fixed;
    }

    static final class FormEntry { String section; boolean optional; double dyn = 1.0; }

    /** One note to play: `beat` counts from the start of its bar, `dur` in beats (it may run past the barline). */
    static final class Ev {
        double beat, dur, vel; int midi, inst = -1, art; Track track;
        Ev(Track t, double beat, double dur, int midi, double vel, int inst, int art) { track = t; this.beat = beat; this.dur = dur; this.midi = midi; this.vel = vel; this.inst = inst; this.art = art; }
    }

    static final class Chord {
        String sym; int root, bass, third = 4, fifth = 7, seventh = -1; boolean ninth; double start, dur;
        int near(int pc, int ctr) { int d = (((pc - ctr) % 12) + 12) % 12; if (d > 6) d -= 12; return ctr + d; }
        /** MIDI note of a tone around the centre `ctr` (-1 when the chord has no such tone). */
        int resolve(Tone t, int ctr) {
            if (t.kind == 'n') return t.abs;
            if (t.near) {
                int pcT;
                switch (t.kind) {
                    case '1': pcT = root; break;
                    case '3': pcT = root + third; break;
                    case '5': pcT = root + fifth; break;
                    case '7': if (seventh < 0) return -1; pcT = root + seventh; break;
                    case '9': pcT = root + 2; break;
                    default: pcT = bass;
                }
                return near(((pcT % 12) + 12) % 12, ctr + 12 * t.oct);
            }
            int rootM = near(root, ctr), m;
            switch (t.kind) {
                case '1': m = rootM; break;
                case '3': m = rootM + third; break;
                case '5': m = rootM + fifth; break;
                case '7': if (seventh < 0) return -1; m = rootM + seventh; break;
                case '9': m = rootM + 14; break;
                default: m = near(bass, ctr); break;
            }
            return m + 12 * t.oct;
        }
        /** The pitch classes of the chord (bit mask), for the checks. */
        int mask() {
            int m = 1 << root; m |= 1 << ((root + third) % 12); m |= 1 << ((root + fifth) % 12);
            if (seventh >= 0) m |= 1 << ((root + seventh) % 12);
            if (ninth) m |= 1 << ((root + 2) % 12);
            return m;
        }
    }

    static final class Line { String track, inst, text; Line(String t, String i, String x) { track = t; inst = i; text = x; } }

    static final class Section {
        String name, parent; int bars;
        final List<Line> lines = new ArrayList<Line>();
        Chord[][] chords;
        List<Ev>[] ev;
        /** The chord sounding at `beat` of bar `bar`, or null. */
        Chord chordAt(int bar, double beat) {
            if (chords == null || bar < 0 || bar >= chords.length || chords[bar] == null) return null;
            Chord found = null;
            for (Chord c : chords[bar]) if (c.start <= beat + 1e-9) found = c;
            return found;
        }
    }

    // ------------------------------------------------------------------ a performance (what the conductor plays)
    static final class PBar { Section sec; int idx; double dyn; boolean secStart; }

    static final class Perf {
        Score score; int shift; double bpm; int[] inst; boolean[] plays; final List<PBar> bars = new ArrayList<PBar>();
    }

    /** Chooses this time's key shift, tempo, instruments, optional tracks and optional sections. */
    Perf perform(Random r) {
        Perf p = new Perf();
        p.score = this;
        p.shift = shiftMin + (shiftMax > shiftMin ? r.nextInt(shiftMax - shiftMin + 1) : 0);
        p.bpm = bpm * (0.96 + 0.08 * r.nextDouble());
        p.inst = new int[tracks.size()];
        p.plays = new boolean[tracks.size()];
        for (Track t : tracks) {
            p.inst[t.index] = t.insts[r.nextInt(t.insts.length)];
            p.plays[t.index] = t.chance >= 100 || r.nextInt(100) < t.chance;
        }
        for (FormEntry f : form) {
            if (f.optional && r.nextInt(100) >= 60) continue;
            Section s = sections.get(f.section);
            if (s == null) continue;
            for (int b = 0; b < s.bars; b++) {
                PBar pb = new PBar();
                pb.sec = s; pb.idx = b; pb.dyn = f.dyn; pb.secStart = b == 0;
                p.bars.add(pb);
            }
        }
        return p;
    }

    /** Every section in the form (optional ones too), the first instrument of each track, no key shift, every track: for previews and checks. */
    Perf full() {
        Perf p = new Perf();
        p.score = this; p.shift = 0; p.bpm = bpm;
        p.inst = new int[tracks.size()]; p.plays = new boolean[tracks.size()];
        for (Track t : tracks) { p.inst[t.index] = t.insts[0]; p.plays[t.index] = true; }
        for (FormEntry f : form) {
            Section sec = sections.get(f.section);
            if (sec == null) continue;
            for (int b = 0; b < sec.bars; b++) { PBar pb = new PBar(); pb.sec = sec; pb.idx = b; pb.dyn = f.dyn; pb.secStart = b == 0; p.bars.add(pb); }
        }
        return p;
    }

    double moodWeight(int mood) { Double w = moods.get(mood); return w == null ? 0 : w; }

    double seconds() {
        double beatsTotal = 0;
        for (FormEntry f : form) { Section s = sections.get(f.section); if (s != null && !f.optional) beatsTotal += s.bars * beats; }
        return beatsTotal * 60.0 / bpm;
    }

    // ------------------------------------------------------------------ parsing
    static Score parse(String[] src) {
        Score s = new Score();
        s.lines = src;
        List<String> lines = new ArrayList<String>();
        for (String raw : src) {
            String l = raw.trim();
            if (l.isEmpty() || l.startsWith("//")) continue;
            if (l.startsWith("|") && !lines.isEmpty()) { lines.set(lines.size() - 1, lines.get(lines.size() - 1) + " " + l); continue; }
            lines.add(l);
        }
        Section cur = null;
        for (String l : lines) {
            try {
                if (l.startsWith("[")) {
                    int e = l.indexOf(']');
                    String head = l.substring(1, e).trim();
                    cur = new Section();
                    int lt = head.indexOf('<');
                    cur.name = (lt < 0 ? head : head.substring(0, lt)).trim();
                    if (lt >= 0) {
                        cur.parent = head.substring(lt + 1).trim();
                        Section par = s.sections.get(cur.parent);
                        if (par == null) s.problems.add("section " + cur.name + ": no parent " + cur.parent);
                        else cur.lines.addAll(par.lines);
                    }
                    s.sections.put(cur.name, cur);
                } else if (l.startsWith("pattern ")) {
                    int c = l.indexOf(':');
                    s.addPattern(l.substring(8, c).trim(), l.substring(c + 1).trim());
                } else if (l.startsWith("track ")) {
                    s.addTrack(l.substring(6).trim());
                } else {
                    int c = l.indexOf(':');
                    if (c < 0) { s.problems.add("cannot read: " + l); continue; }
                    String key = l.substring(0, c).trim(), val = l.substring(c + 1).trim();
                    if (cur == null) s.header(key, val);
                    else {
                        String inst = null, tname = key;
                        int p = key.indexOf('(');
                        if (p > 0) { tname = key.substring(0, p).trim(); inst = key.substring(p + 1, key.indexOf(')')).trim(); }
                        for (int i = cur.lines.size() - 1; i >= 0; i--) if (cur.lines.get(i).track.equals(tname)) cur.lines.remove(i);
                        cur.lines.add(new Line(tname, inst, val));
                    }
                }
            } catch (RuntimeException ex) {
                s.problems.add("line \"" + (l.length() > 50 ? l.substring(0, 50) + "..." : l) + "\": " + ex);
            }
        }
        for (Section sec : s.sections.values()) {
            try { s.compile(sec); } catch (RuntimeException ex) { s.problems.add("section " + sec.name + ": " + ex); }
        }
        for (FormEntry f : s.form) if (!s.sections.containsKey(f.section)) s.problems.add("form names a missing section " + f.section);
        if (s.form.isEmpty()) s.problems.add("no form");
        return s;
    }

    private void header(String key, String val) {
        switch (key) {
            case "title": title = val; break;
            case "layers": fixedLayers = val.equals("fixed"); break;
            case "kind": concert = val.equals("concert"); break;
            case "source": source = val; break;
            case "bpm": bpm = Double.parseDouble(val); break;
            case "beats": beats = Double.parseDouble(val); break;
            case "swing": swing = Math.max(0, Math.min(0.4, Double.parseDouble(val))); break;
            case "shift": { String[] p = val.split("\\s+"); shiftMin = Integer.parseInt(p[0]); shiftMax = Integer.parseInt(p[1]); break; }
            case "moods":
                for (String p : val.split("\\s+")) { String[] kv = p.split(":"); moods.put(Integer.parseInt(kv[0]), kv.length > 1 ? Double.parseDouble(kv[1]) : 1.0); }
                break;
            case "form":
                for (String p : val.split("\\s+")) {
                    FormEntry f = new FormEntry();
                    String[] kv = p.split(":");
                    f.section = kv[0];
                    if (f.section.endsWith("?")) { f.optional = true; f.section = f.section.substring(0, f.section.length() - 1); }
                    if (kv.length > 1) { double d = dynValue(kv[1]); f.dyn = d > 0 ? d : Double.parseDouble(kv[1]); }
                    form.add(f);
                }
                break;
            default: problems.add("unknown header " + key);
        }
    }

    private void addPattern(String name, String spec) {
        Pat p = new Pat();
        p.name = name;
        for (String tok : spec.split("\\s+")) {
            int c = tok.lastIndexOf(':');
            String left = c < 0 ? tok : tok.substring(0, c), dur = c < 0 ? "1" : tok.substring(c + 1);
            PNote n = new PNote();
            if (left.endsWith(">")) { n.vel = 1.3; left = left.substring(0, left.length() - 1); }        // an accent
            else if (left.endsWith("^")) { n.vel = 0.5; left = left.substring(0, left.length() - 1); }   // a ghost note
            n.dur = parseDur(dur);
            if (n.dur < 0) p.fixed = true;
            if (left.equals("r")) n.rest = true;
            else for (String t : left.split("\\+")) {
                if (t.startsWith("@")) { n.tones.add(new Tone(noteNumber(t.substring(1)))); continue; }
                boolean nearTone = t.startsWith("=");
                if (nearTone) t = t.substring(1);
                char kind = t.charAt(0);
                if ("13579b".indexOf(kind) < 0) throw new IllegalArgumentException("bad tone " + t);
                int oct = 0;
                for (int i = 1; i < t.length(); i++) { if (t.charAt(i) == '\'') oct++; else if (t.charAt(i) == ',') oct--; else throw new IllegalArgumentException("bad tone " + t); }
                n.tones.add(new Tone(kind, oct, nearTone));
            }
            p.notes.add(n);
        }
        patterns.put(name, p);
    }

    private void addTrack(String spec) {
        String[] parts = spec.split("\\s+");
        Track t = new Track();
        t.name = parts[0];
        for (int i = 1; i < parts.length; i++) {
            String[] kv = parts[i].split("=", 2);
            String k = kv[0], v = kv.length > 1 ? kv[1] : "";
            switch (k) {
                case "role": t.role = v; t.roleIdx = roleIndex(v); if (t.roleIdx < 0) throw new IllegalArgumentException("role " + v); break;
                case "inst": {
                    String[] ns = v.split("\\|");
                    t.insts = new int[ns.length];
                    for (int j = 0; j < ns.length; j++) { t.insts[j] = Ambient.instIndex(ns[j]); if (t.insts[j] < 0) throw new IllegalArgumentException("instrument " + ns[j]); }
                    break;
                }
                case "vel": t.vel = Double.parseDouble(v); break;
                case "pan": t.pan = Double.parseDouble(v); break;
                case "ctr": t.ctr = noteNumber(v); break;
                case "pat": t.pat = v; break;
                case "p": t.chance = Integer.parseInt(v); break;
                case "all": t.everywhere = true; break;
                default: throw new IllegalArgumentException("track key " + k);
            }
        }
        t.index = tracks.size();
        tracks.add(t);
        trackByName.put(t.name, t);
    }

    // ------------------------------------------------------------------ notes and chords
    /** The pieces of the drum kit by name (the General MIDI drum numbers): a note of the `kit` instrument written `@snare` in a pattern, `snare:1` or `[kick hat]` in a line of notes. */
    static final Map<String, Integer> DRUMS = new LinkedHashMap<String, Integer>();
    static {
        String[] names = {"kick", "kick2", "rim", "snare", "snare2", "clap", "tom5", "tom4", "tom3", "tom2", "tom1", "hat", "pedal", "ohat", "crash", "splash", "crash2", "ride", "ride2", "rbell", "gong", "tamb", "cow",
            "bongo", "bongo2", "mconga", "conga", "conga2", "timbale", "timbale2", "agogo", "agogo2", "cabasa", "shaker", "guiro", "guiro2", "clave", "block", "block2", "mtri", "tri"};
        int[] nums = {36, 35, 37, 38, 40, 39, 41, 43, 45, 47, 50, 42, 44, 46, 49, 55, 57, 51, 59, 53, 52, 54, 56,
            60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 73, 74, 75, 76, 77, 80, 81};
        for (int i = 0; i < names.length; i++) DRUMS.put(names[i], nums[i]);
    }

    static int noteNumber(String s) {
        Matcher m = java.util.regex.Pattern.compile("^([A-G])([#b]?)(-?\\d)$").matcher(s);
        if (m.matches()) return midi(m.group(1).charAt(0), m.group(2), Integer.parseInt(m.group(3)));
        Integer d = DRUMS.get(s);
        if (d != null) return d;
        return Integer.parseInt(s);
    }

    static int pc(char letter) { return "C D EF G A B".indexOf(letter); }

    static int midi(char letter, String acc, int oct) {
        int p = pc(letter);
        if (acc.equals("#")) p++; else if (acc.equals("b")) p--;
        return 12 * (oct + 1) + p;
    }

    static double parseDur(String s) {
        s = s.trim();
        if (s.equals("*")) return -1;
        int slash = s.indexOf('/');
        if (slash > 0) return Double.parseDouble(s.substring(0, slash)) / Double.parseDouble(s.substring(slash + 1));
        return Double.parseDouble(s);
    }

    private static final java.util.regex.Pattern CHORD = java.util.regex.Pattern.compile("^([A-G][#b]?)(maj9|maj7|m7b5|7sus4|madd9|add9|sus4|sus2|dim|m7|m|7|6|)(?:/([A-G][#b]?))?$");

    static Chord parseChord(String sym) {
        Matcher m = CHORD.matcher(sym);
        if (!m.matches()) return null;
        Chord c = new Chord();
        c.sym = sym;
        c.root = rootPc(m.group(1));
        c.bass = m.group(3) == null ? c.root : rootPc(m.group(3));
        switch (m.group(2)) {
            case "m": c.third = 3; break;
            case "7": c.seventh = 10; break;
            case "maj7": c.seventh = 11; break;
            case "maj9": c.seventh = 11; c.ninth = true; break;
            case "m7": c.third = 3; c.seventh = 10; break;
            case "sus4": c.third = 5; break;
            case "sus2": c.third = 2; break;
            case "add9": c.ninth = true; break;
            case "madd9": c.third = 3; c.ninth = true; break;
            case "6": c.seventh = 9; break;
            case "dim": c.third = 3; c.fifth = 6; break;
            case "m7b5": c.third = 3; c.fifth = 6; c.seventh = 10; break;
            case "7sus4": c.third = 5; c.seventh = 10; break;
            default:
        }
        return c;
    }

    private static int rootPc(String s) {
        int p = pc(s.charAt(0));
        if (s.length() > 1) p += s.charAt(1) == '#' ? 1 : -1;
        return ((p % 12) + 12) % 12;
    }

    // ------------------------------------------------------------------ compiling a section
    @SuppressWarnings("unchecked")
    private void compile(Section sec) {
        Line chordLine = null;
        for (Line l : sec.lines) if (l.track.equals("chords")) chordLine = l;
        int bars = 0;
        if (chordLine != null) {
            String[] bs = chordLine.text.split("\\|");
            sec.chords = new Chord[bs.length][];
            String prev = null;
            for (int b = 0; b < bs.length; b++) {
                String t = bs[b].trim();
                if (t.equals("%") && prev != null) t = prev;
                prev = t;
                String[] cs = t.split("\\s+");
                Chord[] row = new Chord[cs.length];
                double fixed = 0; int free = 0;
                for (int i = 0; i < cs.length; i++) { int c = cs[i].indexOf(':'); if (c >= 0) fixed += parseDur(cs[i].substring(c + 1)); else free++; }
                double each = free == 0 ? 0 : (beats - fixed) / free, at = 0;
                for (int i = 0; i < cs.length; i++) {
                    int c = cs[i].indexOf(':');
                    String sym = c >= 0 ? cs[i].substring(0, c) : cs[i];
                    Chord ch = parseChord(sym);
                    if (ch == null) throw new IllegalArgumentException("chord " + sym + " in bar " + (b + 1));
                    ch.start = at; ch.dur = c >= 0 ? parseDur(cs[i].substring(c + 1)) : each;
                    at += ch.dur;
                    row[i] = ch;
                }
                if (Math.abs(at - beats) > 1e-6) problems.add(sec.name + " chords bar " + (b + 1) + " add up to " + at + " beats");
                sec.chords[b] = row;
            }
            bars = bs.length;
        }
        for (Line l : sec.lines) {
            Track t = trackByName.get(l.track);
            if (t == null || t.chordDriven()) continue;
            bars = Math.max(bars, l.text.split("\\|").length);
        }
        sec.bars = bars;
        sec.ev = new List[bars];
        for (int b = 0; b < bars; b++) sec.ev[b] = new ArrayList<Ev>();
        for (Track t : tracks) {
            if (t.everywhere && t.chordDriven()) {
                boolean has = false;
                for (Line l : sec.lines) if (l.track.equals(t.name)) has = true;
                if (!has && sec.chords != null) sec.lines.add(new Line(t.name, null, "on"));
            }
        }
        for (Line l : sec.lines) {
            if (l.track.equals("chords")) continue;
            Track t = trackByName.get(l.track);
            if (t == null) { problems.add(sec.name + ": no track " + l.track); continue; }
            int inst = -1;
            if (l.inst != null) { inst = Ambient.instIndex(l.inst); if (inst < 0) problems.add(sec.name + ": instrument " + l.inst); }
            if (t.chordDriven()) chordTrack(sec, t, l, inst);
            else explicitTrack(sec, t, l, inst);
        }
    }

    private void chordTrack(Section sec, Track t, Line l, int inst) {
        if (sec.chords == null) { problems.add(sec.name + ": " + t.name + " needs chords"); return; }
        String patName = null; int from = 0, to = sec.bars - 1, octMove = 0; double mult = 1;
        for (String tok : l.text.split("\\s+")) {
            if (tok.equals("-")) return;
            if (tok.startsWith("@")) { String[] ab = tok.substring(1).split("-"); from = Integer.parseInt(ab[0]) - 1; to = ab.length > 1 ? Integer.parseInt(ab[1]) - 1 : from; }
            else if (tok.matches("x[0-9.]+")) mult = Double.parseDouble(tok.substring(1));
            else if (tok.matches("o[+-][0-9]")) octMove = Integer.parseInt(tok.substring(1).replace("+", ""));
            else patName = tok.equals("on") ? t.pat : tok;
        }
        if (patName == null) patName = t.pat;
        Pat p = patterns.get(patName);
        if (p == null) { problems.add(sec.name + ": no pattern " + patName); return; }
        for (int b = Math.max(0, from); b <= Math.min(to, sec.bars - 1); b++) {
            if (b >= sec.chords.length) break;
            for (Chord c : sec.chords[b]) {
                double at = 0;
                int guard = 0;
                while (at < c.dur - 1e-9 && guard++ < 200) {
                    for (PNote n : p.notes) {
                        if (at >= c.dur - 1e-9) break;
                        double d = n.dur < 0 ? c.dur - at : Math.min(n.dur, c.dur - at);
                        if (!n.rest) {
                            boolean onBeat = Math.abs((c.start + at) - Math.rint(c.start + at)) < 1e-6;
                            for (Tone tn : n.tones) {
                                int m = c.resolve(tn, t.ctr);
                                if (m < 0) continue;
                                m += 12 * octMove;
                                double v = mult * (onBeat ? 1.0 : 0.82) * n.vel;
                                sec.ev[b].add(new Ev(t, c.start + at, n.dur < 0 ? d * 1.03 : d, m, v, inst, n.dur < 0 ? 2 : 0));
                            }
                        }
                        at += d;
                    }
                    if (p.fixed) break;
                }
            }
        }
    }

    private static List<String> tokens(String bar) {
        List<String> out = new ArrayList<String>();
        int i = 0, n = bar.length();
        while (i < n) {
            char ch = bar.charAt(i);
            if (java.lang.Character.isWhitespace(ch)) { i++; continue; }
            int j = i;
            if (ch == '[') { j = bar.indexOf(']', i); if (j < 0) throw new IllegalArgumentException("missing ]"); j++; }
            while (j < n && !java.lang.Character.isWhitespace(bar.charAt(j))) j++;
            out.add(bar.substring(i, j));
            i = j;
        }
        return out;
    }

    private static String expandGroups(String s) {
        java.util.regex.Pattern g = java.util.regex.Pattern.compile("\\(([^()]*)\\)\\*(\\d+)");
        for (int guard = 0; guard < 20; guard++) {
            Matcher m = g.matcher(s);
            if (!m.find()) break;
            StringBuilder sb = new StringBuilder();
            int k = Integer.parseInt(m.group(2));
            for (int i = 0; i < k; i++) sb.append(m.group(1)).append(' ');
            s = s.substring(0, m.start()) + sb.toString().trim() + s.substring(m.end());
        }
        return s;
    }

    private void explicitTrack(Section sec, Track t, Line l, int inst) {
        if (l.text.trim().equals("-")) return;
        String[] bs = l.text.split("\\|");
        double defDur = 1, dyn = 1;
        Ev tied = null;
        String prev = null;
        for (int b = 0; b < bs.length && b < sec.bars; b++) {
            String text = bs[b].trim();
            if (text.equals("%") && prev != null) text = prev;
            prev = text;
            double at = 0;
            for (String tok : tokens(expandGroups(text))) {
                double dv = dynValue(tok);
                if (dv > 0) { dyn = dv; continue; }
                if (tok.startsWith("d=")) { defDur = parseDur(tok.substring(2)); continue; }
                int colon = tok.lastIndexOf(':');
                double dur = defDur;
                String body = tok;
                if (colon >= 0 && !(tok.startsWith("[") && tok.indexOf(']') > colon)) { dur = parseDur(tok.substring(colon + 1)); body = tok.substring(0, colon); }
                if (body.equals("r")) { at += dur; tied = null; continue; }
                List<Integer> pitches = new ArrayList<Integer>();
                String mods;
                if (body.startsWith("[")) {
                    int e = body.indexOf(']');
                    for (String pn : body.substring(1, e).trim().split("\\s+")) pitches.add(noteNumber(pn));
                    mods = body.substring(e + 1);
                } else {
                    Matcher dm = java.util.regex.Pattern.compile("^([a-z][a-z0-9]*?)([>'_~s^]*)$").matcher(body);
                    if (dm.matches() && DRUMS.containsKey(dm.group(1))) {
                        pitches.add(DRUMS.get(dm.group(1)));
                        mods = dm.group(2);
                    } else {
                        Matcher m = java.util.regex.Pattern.compile("^([A-G])([#b]?)(-?\\d)([>'_~s^]*)$").matcher(body);
                        if (!m.matches()) throw new IllegalArgumentException("note " + tok + " (bar " + (b + 1) + ")");
                        pitches.add(midi(m.group(1).charAt(0), m.group(2), Integer.parseInt(m.group(3))));
                        mods = m.group(4);
                    }
                }
                double vel = dyn * (mods.indexOf('>') >= 0 ? 1.18 : mods.indexOf('^') >= 0 ? 0.5 : 1.0);
                int art = mods.indexOf('\'') >= 0 ? 1 : mods.indexOf('_') >= 0 ? 2 : 0;
                boolean tie = mods.indexOf('~') >= 0;
                if (tied != null && pitches.size() == 1 && tied.midi == pitches.get(0)) {
                    // the note carries on from the one before: lengthen that one
                    tied.dur += dur;
                    if (!tie) tied = null;
                    at += dur;
                    continue;
                }
                java.util.Collections.sort(pitches);
                Ev last = null;
                for (int i = 0; i < pitches.size(); i++) {
                    double strum = mods.indexOf('s') >= 0 ? 0.06 * i : 0;
                    last = new Ev(t, at + strum, dur - strum, pitches.get(i), vel, inst, art);
                    sec.ev[b].add(last);
                }
                tied = tie && pitches.size() == 1 ? last : null;
                at += dur;
            }
            if (Math.abs(at - beats) > 1e-6) problems.add(sec.name + "/" + t.name + " bar " + (b + 1) + " adds up to " + at + " beats, not " + beats);
        }
    }
}
