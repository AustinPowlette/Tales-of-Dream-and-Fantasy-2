package Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

/** The songs and their conductor (no game, no sound card): every song parses, the score format works, the songs have no harsh clashes, every mood has a song,
 *  the conductor plays, rests, changes song with the place, thins out and adds drums with the mood, performances differ, and the built-in synthesiser renders.
 *  Compile with tools/SongTool.java in the same folder (the clash check). */
public class SongProbe {
    static int pass = 0, fail = 0;

    /** Whether the song with this title is written for this mood. */
    static boolean has(String title, int mood) { for (Score sc : SongBook.all()) if (sc.title.equals(title)) return sc.moods.containsKey(mood); return false; }

    static void check(String name, boolean ok) { if (ok) pass++; else fail++; System.out.println("[T] " + (ok ? "PASS " : "FAIL ") + name); }

    /** Plays a conductor on a made-up clock: every note it writes, its master level and the title, once a second. */
    static final class Run {
        final Conductor c; long clock = 0;
        final List<Ambient.Note> notes = new ArrayList<Ambient.Note>();
        final TreeMap<Integer, Double> master = new TreeMap<Integer, Double>();
        final TreeMap<Integer, String> title = new TreeMap<Integer, String>();

        Run(Conductor c) { this.c = c; }

        void until(double seconds) {
            long end = (long) (seconds * Ambient.SR);
            while (clock < end) {
                for (Ambient.Note n = c.poll(clock); n != null; n = c.poll(clock)) notes.add(n);
                int sec = (int) (clock / Ambient.SR);
                if (!master.containsKey(sec)) { master.put(sec, c.masterAt(clock)); title.put(sec, c.title()); }
                clock += 2048;
            }
        }

        int count(double from, double to, String trackName, Score sc) {
            int n = 0;
            for (Ambient.Note x : notes) {
                double t = x.at / Ambient.SR;
                if (t >= from && t < to && x.track >= 0 && sc.tracks.get(x.track).name.equals(trackName)) n++;
            }
            return n;
        }
    }

    public static void main(String[] a) throws Exception {
        List<Score> all = SongBook.all();
        check("M1 the songs parse without problems (" + all.size() + " songs)", all.size() >= 3 && SongBook.problems.isEmpty());
        for (String p : SongBook.problems) System.out.println("[T] info: " + p);
        boolean covered = true;
        for (int m = 0; m <= 6; m++) {
            boolean any = false;
            for (Score s : all) if (s.moodWeight(m) > 0) any = true;
            if (!any) { covered = false; System.out.println("[T] info: no song for mood " + m); }
        }
        check("M2 every mood (open, city, danger, night, war, sect, capital) has a song", covered);
        boolean sane = true;
        for (Score s : all) {
            if (s.seconds() < 60 || s.seconds() > 400 || s.bpm < 30 || s.bpm > 200) sane = false;
            for (Score.Section sec : s.sections.values()) if (sec.bars < 1) sane = false;
            Score.Perf f = s.full();
            for (Score.PBar pb : f.bars) for (Score.Ev e : pb.sec.ev[pb.idx])
                if (e.midi + s.shiftMin < 21 || e.midi + s.shiftMax > 108 || e.dur <= 0 || e.vel <= 0) { sane = false; System.out.println("[T] info: " + s.title + " note out of range " + e.midi + " in " + pb.sec.name); }
        }
        check("M3 every song has a sensible length and tempo, and all notes stay in range after the key shift", sane);
        boolean clean = true;
        for (Score s : all) {
            int[] l = SongTool.lint(s, false);
            if (l[0] != 0 || l[1] != 0 || l[3] != 0) { clean = false; System.out.println("[T] info: " + s.title + " lint " + Arrays.toString(l)); }
        }
        check("M4 no harsh clashes and no stray notes against the chords in any song (SongTool lint)", clean);

        // the score format
        Score m = Score.parse(new String[]{"title: T", "bpm: 60", "beats: 4", "form: A", "track m role=lead inst=piano", "track h role=rhythm inst=harp ctr=C3 pat=p", "pattern p: 1:1 5:1 3':2",
            "[A]", "chords: C | G/B", "m: C4:1 E4:1 G4:2 | G4~:2 G4:2", "h: p"});
        check("M5 the score format: bars, ties and chord-driven tracks", m.problems.isEmpty() && m.sections.get("A").bars == 2 && m.sections.get("A").ev[1].size() >= 3);
        int tied = 0; double tiedDur = 0;
        for (Score.Ev e : m.sections.get("A").ev[1]) if (e.track.name.equals("m")) { tied++; tiedDur = e.dur; }
        check("M5b a tie lengthens the first note instead of striking again (one note of 4 beats)", tied == 1 && Math.abs(tiedDur - 4) < 1e-6);
        boolean patOk = false;
        for (Score.Ev e : m.sections.get("A").ev[0]) if (e.track.name.equals("h") && e.midi == 55 && Math.abs(e.beat - 1) < 1e-6) patOk = true;
        check("M5c a pattern puts the fifth of C on beat 2 (G3)", patOk);
        Score.Chord sl = Score.parseChord("D/F#");
        check("M5d a slash chord keeps its bass (F#2 near G2) and a seventh chord has its seventh", sl != null && sl.resolve(new Score.Tone('b', 0, false), 43) == 42 && Score.parseChord("Gmaj7").seventh == 11 && Score.parseChord("Em7").third == 3);
        Score bad = Score.parse(new String[]{"title: T", "bpm: 60", "beats: 4", "form: A", "track m role=lead inst=piano", "[A]", "chords: C", "m: C4:1 E4:1"});
        check("M5e a bar that does not add up to the beats is reported", !bad.problems.isEmpty());
        Score grp = Score.parse(new String[]{"title: T", "bpm: 60", "beats: 4", "form: A", "track m role=lead inst=piano", "[A]", "chords: C | %", "m: (C4:.5 E4:.5)*4 | %"});
        check("M5f groups (..)*n and % repeat", grp.problems.isEmpty() && grp.sections.get("A").ev[0].size() == 8 && grp.sections.get("A").ev[1].size() == 8);

        // performances differ
        Score tale = SongBook.find("tale");
        Set<String> seen = new HashSet<String>();
        for (int i = 0; i < 30; i++) { Score.Perf p = tale.perform(new Random(i)); seen.add(p.shift + Arrays.toString(p.inst) + Arrays.toString(p.plays) + p.bars.size()); }
        check("M6 a song is performed differently (key, instruments, optional parts): " + seen.size() + " of 30 performances differ", seen.size() >= 8);

        // the same song in danger, in the open country and at night
        Score road = SongBook.find("road");
        Conductor c = new Conductor(5); c.mood = 2; c.forced = road;
        Run r = new Run(c);
        r.until(40);
        int driveDanger = r.count(10, 30, "dtaiko", road) + r.count(10, 30, "dcello", road), leadDanger = r.count(10, 30, "lead", road) + r.count(10, 30, "lead2", road);
        c.mood = 0;
        r.until(110);
        int driveOpen = r.count(80, 108, "dtaiko", road) + r.count(80, 108, "dcello", road), leadOpen = r.count(80, 108, "lead", road) + r.count(80, 108, "lead2", road);
        check("M7 in danger the road song plays drums and a low pulse and no melody (" + driveDanger + " drive notes, " + leadDanger + " melody notes)", driveDanger > 10 && leadDanger == 0);
        check("M7b back in the open country the melody returns and the drums go (" + leadOpen + " melody, " + driveOpen + " drive)", leadOpen > 5 && driveOpen == 0);
        c.mood = 3;
        r.until(170);
        int padNight = r.count(145, 168, "pad", road), koto = r.count(145, 168, "koto", road);
        check("M7c at night the song goes on thinner (" + koto + " koto notes, " + padNight + " pad notes)", padNight > 0 && koto > 0);

        // fades and rests
        Conductor c2 = new Conductor(9); c2.mood = 6;
        Run r2 = new Run(c2);
        r2.until(60);
        String first = r2.title.get(30);
        c2.mood = 1;
        r2.until(220);
        boolean sawMarket = false; double minMaster = 1;
        for (int sec = 60; sec < 220; sec++) { String t = r2.title.get(sec); if (t != null && !t.equals(first) && has(t, 1)) sawMarket = true; }
        for (double v : r2.master.subMap(60, 220).values()) minMaster = Math.min(minMaster, v);
        check("M8 the capital plays one of its songs (" + first + ")", first != null && has(first, 6));
        check("M8b moving to a city fades the theme out and starts the market song (lowest master level " + String.format("%.2f", minMaster) + ")", sawMarket && minMaster < 0.2);
        Conductor c3 = new Conductor(21); c3.mood = 0;
        Run r3 = new Run(c3);
        r3.until(420);
        int emptySecs = 0, startedFromRest = 0; String prev = "";
        for (int sec = 0; sec < 420; sec++) {
            String t = r3.title.get(sec);
            if (t == null) continue;
            if (t.isEmpty()) emptySecs++;
            if (!t.isEmpty() && prev.isEmpty() && sec > 3) startedFromRest++;
            prev = t;
        }
        check("M9 the music rests between songs (" + emptySecs + " quiet seconds in 7 minutes, " + startedFromRest + " songs started after a rest)", emptySecs >= 10 && startedFromRest >= 1);
        Conductor c4 = new Conductor(33); c4.mood = 1;
        Run r4 = new Run(c4);
        r4.until(900);
        List<String> order = new ArrayList<String>();
        String last = "";
        for (int sec = 0; sec < 900; sec++) { String t = r4.title.get(sec); if (t != null && !t.isEmpty() && !t.equals(last)) order.add(t); if (t != null) last = t; }
        boolean again = false;
        for (int i = 1; i < order.size(); i++) if (order.get(i).equals(order.get(i - 1))) again = true;
        check("M9b a city plays songs one after the other (" + order.size() + " songs in 15 minutes: " + order + ")", order.size() >= 3);
        Conductor c6 = new Conductor(44); c6.mood = 0;
        Run r6 = new Run(c6);
        r6.until(1500);
        List<String> order6 = new ArrayList<String>();
        last = "";
        for (int sec = 0; sec < 1500; sec++) { String t = r6.title.get(sec); if (t != null && !t.isEmpty() && !t.equals(last)) order6.add(t); if (t != null) last = t; }
        boolean twice = false;
        for (int i = 1; i < order6.size(); i++) if (order6.get(i).equals(order6.get(i - 1))) twice = true;
        check("M9c the same song is never played twice running when the place has a choice: " + order6, order6.size() >= 4 && !twice);

        // the built-in synthesiser and the note stream
        boolean renders = true;
        for (int mood = 0; mood <= 6; mood++) {
            byte[] st = Ambient.renderStereo(8, 3, mood);
            long sum = 0; int peak = 0;
            for (int i = 0; i + 1 < st.length; i += 2) { int v = (short) ((st[i] & 0xff) | (st[i + 1] << 8)); sum += Math.abs(v); peak = Math.max(peak, Math.abs(v)); }
            if (st.length != (int) (Ambient.SR * 8) * 4 || sum < 100000 || peak >= 32767) { renders = false; System.out.println("[T] info: mood " + mood + " sum " + sum + " peak " + peak); }
        }
        check("M10 the built-in synthesiser renders every mood (not silent, not clipped)", renders);
        boolean notes = true;
        for (Score s : all) {
            List<Ambient.Note> ns = Conductor.collect(s, 0, 1);
            if (ns.size() < 200) notes = false;
            for (Ambient.Note n : ns) if (n.inst < 0 || n.inst >= Ambient.INST.length || n.midi < 12 || n.midi > 108 || n.vel <= 0 || n.vel > 1 || n.dur <= 0) notes = false;
        }
        check("M11 every note a song writes names a real instrument, pitch, loudness and length", notes);
        check("M12 the SoundFont trims and channels cover every instrument", MidiOut.TRIM_DB.length == Ambient.INST.length && MidiOut.PAN.length == Ambient.INST.length && Ambient.GM.length == Ambient.INST.length && Ambient.layer.length == Ambient.INST.length);
        Conductor c5 = new Conductor(2); c5.mood = 6; c5.forced = SongBook.find("market");
        Run r5 = new Run(c5);
        r5.until(10);
        check("M13 a pinned song plays whatever the mood (market in the capital)", r5.title.get(5) != null && r5.title.get(5).contains("Market"));
        check("M14 songs can be found by number and by part of the name", SongBook.find("1") == all.get(0) && SongBook.find("lamps") != null && SongBook.find("nonsense") == null);

        // every mood has its own song
        String[][] own = {{"2", ""}, {"3", ""}, {"4", ""}, {"5", ""}, {"6", ""}};
        boolean ownOk = true;
        for (String[] o : own) {
            Conductor cc = new Conductor(7); cc.mood = Integer.parseInt(o[0]);
            Run rr = new Run(cc); rr.until(20);
            String t = rr.title.get(10);
            if (t == null || !has(t, Integer.parseInt(o[0]))) { ownOk = false; System.out.println("[T] info: mood " + o[0] + " plays " + t); }
        }
        check("M15 danger, night, war, the sects and the capital each start their own song", ownOk);
        Score snow = SongBook.find("snow");
        Conductor cn = new Conductor(8); cn.mood = 3; cn.forced = snow; // (nine songs are written for the night now: pin the one this check is about)
        Run rn = new Run(cn); rn.until(60);
        check("M16 a song written for its mood plays every track whatever the layers (the night song's melody, " + (rn.count(0, 60, "lead", snow) + rn.count(0, 60, "lead2", snow)) + " notes)", snow.fixedLayers && rn.count(0, 60, "lead", snow) + rn.count(0, 60, "lead2", snow) > 5);
        // walking into danger from the open country: the road song gives way within about half a minute
        Conductor cd = new Conductor(12); cd.mood = 0; cd.forced = null;
        Run rd = new Run(cd);
        rd.until(15);
        String before = rd.title.get(12);
        cd.mood = 2;
        rd.until(80);
        int whenDanger = -1; double lowest = 1;
        for (int sec = 16; sec < 80; sec++) { String t = rd.title.get(sec); if (whenDanger < 0 && t != null && has(t, 2)) whenDanger = sec; }
        for (double v : rd.master.subMap(16, 80).values()) lowest = Math.min(lowest, v);
        check("M17 danger takes over from another song within about 35 seconds with a dip in volume (was " + before + "; the Beasts song at " + whenDanger + " s, lowest level " + String.format("%.2f", lowest) + ")", before != null && !before.isEmpty() && whenDanger > 0 && whenDanger < 50 && lowest < 0.3);
        boolean tracksOk = true;
        for (Score sc : all) for (Score.Track t : sc.tracks) { boolean used = false; for (Score.Section sec : sc.sections.values()) for (java.util.List<Score.Ev> bar : sec.ev) for (Score.Ev e : bar) if (e.track == t) used = true; if (!used) { tracksOk = false; System.out.println("[T] info: " + sc.title + " track " + t.name + " never plays"); } }
        check("M18 every track of every song plays somewhere", tracksOk);

        // the drum kit, the new instruments, the groove, and how alike the songs are
        boolean drumsOk = !Score.DRUMS.isEmpty();
        for (java.util.Map.Entry<String, Integer> d : Score.DRUMS.entrySet()) if (d.getValue() < 35 || d.getValue() > 81) drumsOk = false;
        Score kt = Score.parse(new String[]{"title: Kit T", "bpm: 120", "beats: 4", "shift: 0 0", "moods: 1:1", "layers: fixed", "form: A:mf",
            "pattern bd: @kick>:1 r:1 @snare^:.5 @clap+@hat:.5 r:1", "track k role=perc inst=kit vel=1 pan=0 ctr=C2 pat=bd", "track e role=perc inst=kit vel=1 pan=0", "[A]", "chords: C", "k: bd", "e: tom1:1 conga>:1 clave^:1 gong:1"});
        java.util.List<Ambient.Note> kn = Conductor.collect(kt, 1, 1);
        java.util.Map<Integer, Double> kv = new java.util.HashMap<Integer, Double>();
        for (Ambient.Note n : kn) kv.put(n.midi, Math.max(kv.containsKey(n.midi) ? kv.get(n.midi) : 0, n.vel));
        boolean kitNotes = kt.problems.isEmpty() && kv.containsKey(36) && kv.containsKey(38) && kv.containsKey(39) && kv.containsKey(42) && kv.containsKey(50) && kv.containsKey(63) && kv.containsKey(75) && kv.containsKey(52);
        check("K1 drum names parse in patterns and in lines of notes, and the pitch of a kit note is never transposed (" + kt.problems + ")", drumsOk && kitNotes);
        check("K1b an accented hit is louder than a ghost note (kick " + String.format("%.2f", kv.containsKey(36) ? kv.get(36) : 0) + ", snare " + String.format("%.2f", kv.containsKey(38) ? kv.get(38) : 0) + ")", kitNotes && kv.get(36) > kv.get(38) * 1.5);
        String[] swingLines = {"title: Sw", "bpm: 120", "beats: 4", "shift: 0 0", "moods: 1:1", "layers: fixed", "form: A:mf", "track h role=perc inst=kit vel=1 pan=0", "[A]", "chords: C", "h: hat:.5 hat:.5 hat:.5 hat:.5 hat:.5 hat:.5 hat:.5 hat:.5"};
        java.util.List<String> swl = new java.util.ArrayList<String>(java.util.Arrays.asList(swingLines)); swl.add(4, "swing: 0.2");
        Score sw0 = Score.parse(swingLines), sw1 = Score.parse(swl.toArray(new String[0]));
        java.util.List<Ambient.Note> n0 = Conductor.collect(sw0, 1, 1), n1 = Conductor.collect(sw1, 1, 1);
        java.util.Collections.sort(n0, (x, y) -> Long.compare(x.at, y.at)); java.util.Collections.sort(n1, (x, y) -> Long.compare(x.at, y.at));
        double beatSamples = Ambient.SR * 60.0 / 120, shift2 = n0.size() > 1 && n1.size() > 1 ? (n1.get(1).at - n0.get(1).at) / beatSamples : -1, shift1 = n0.size() > 1 && n1.size() > 1 ? (n1.get(0).at - n0.get(0).at) / beatSamples : -1;
        check("K2 swing: a note on the second half of a beat comes late by that fraction of a beat (" + String.format("%.2f", shift2) + " of 0.20), the first half stays (" + String.format("%.2f", shift1) + ")", Math.abs(shift2 - 0.2) < 0.08 && Math.abs(shift1) < 0.08);
        // the built-in synthesiser plays every piece of the kit, and none is far louder than the others
        java.util.List<String> kl = new java.util.ArrayList<String>(java.util.Arrays.asList("title: Pieces", "bpm: 120", "beats: 4", "shift: 0 0", "moods: 1:1", "layers: fixed", "form: A:mf", "track d role=perc inst=kit vel=1 pan=0", "[A]"));
        StringBuilder chs = new StringBuilder("chords: "), ds = new StringBuilder("d: ");
        java.util.List<String> dn = new java.util.ArrayList<String>(Score.DRUMS.keySet());
        for (int i = 0; i < dn.size(); i++) { chs.append(i > 0 ? " | C" : "C"); ds.append(i > 0 ? " | " : "").append(dn.get(i)).append(":1 r:3"); }
        kl.add(chs.toString()); kl.add(ds.toString());
        Score ps = Score.parse(kl.toArray(new String[0]));
        byte[] pcm = Ambient.renderScore(ps, 1, 1, dn.size() * 2 + 4);
        double kLow = 9, kHigh = 0;
        for (int i = 0; i < dn.size(); i++) {
            double pk = 0;
            for (int f = Math.max(0, (int) ((i * 2 - 0.06) * Ambient.SR)); f < (int) ((i * 2 + 1.0) * Ambient.SR) && 4 * f + 1 < pcm.length; f++) pk = Math.max(pk, Math.abs((short) ((pcm[4 * f] & 0xff) | (pcm[4 * f + 1] << 8))) / 32768.0);
            kLow = Math.min(kLow, pk); kHigh = Math.max(kHigh, pk);
        }
        check("K3 the built-in synthesiser plays every piece of the kit (" + dn.size() + " pieces, peaks " + String.format("%.3f", kLow) + " to " + String.format("%.3f", kHigh) + ")", kLow > 0.02 && kHigh / Math.max(1e-9, kLow) < 3.5 && kHigh < 0.99);
        java.util.Set<Integer> used = new java.util.TreeSet<Integer>(java.util.Arrays.asList(Ambient.KIT, Ambient.PLUCK, Ambient.FLUTE, Ambient.ERHU, Ambient.SUONA, Ambient.YANGQIN, Ambient.SHENG, Ambient.XUN, Ambient.XIAO, Ambient.MARIMBA, Ambient.TIMPANI, Ambient.UPRIGHT));
        int[] map = MidiOut.staticMap(used);
        boolean mapOk = map[Ambient.KIT] == 9;
        java.util.Set<Integer> chans = new java.util.HashSet<Integer>();
        for (int i : used) if (i != Ambient.KIT) { if (map[i] < 0 || map[i] > 15 || map[i] == 9 || !chans.add(map[i])) mapOk = false; }
        check("K4 a song's instruments get different channels, the kit the drum channel", mapOk);
        String fontName = Ambient.fonts().isEmpty() ? null : Ambient.fonts().get(0);
        if (fontName == null) check("K5 (no SoundFont here: the channel sharing of MidiOut is not tested)", true);
        else {
            MidiOut mo = MidiOut.open(new java.io.File("soundfonts", fontName), Ambient.GM);
            if (mo == null) check("K5 (no MIDI synthesiser here: " + MidiOut.lastError + ")", true);
            else {
                boolean ok = true; java.util.Set<Integer> used2 = new java.util.HashSet<Integer>();
                mo.volumes(0.0, Ambient.layer); // silent
                for (int i = 0; i < Ambient.INST.length; i++) { int kc = mo.on(i, i == Ambient.KIT ? 36 : 60, 0.3); if (i == Ambient.KIT ? kc != 9 : (kc < 0 || kc > 15 || kc == 9)) ok = false; if (i != Ambient.KIT) used2.add(kc); }
                int kAgain = mo.on(Ambient.PLUCK, 60, 0.3); if (kAgain == 9 || kAgain < 0) ok = false;
                int keep = mo.on(Ambient.UPRIGHT, 50, 0.3), keep2 = mo.on(Ambient.UPRIGHT, 52, 0.3); if (keep != keep2) ok = false; // an instrument that played a moment ago keeps its channel
                mo.allOff(); mo.close();
                check("K5 with more instruments than channels the quiet channels are given to the next ones (" + used2.size() + " channels for " + (Ambient.INST.length - 1) + " instruments)", ok && used2.size() == 15);
            }
        }
        double top = 0; String topPair = "";
        double[][][] feat = new double[all.size()][][];
        for (int i = 0; i < all.size(); i++) feat[i] = SongTool.features(all.get(i), new String[1]);
        for (int i = 0; i < all.size(); i++) for (int j = i + 1; j < all.size(); j++) { double sm = SongTool.similarity(feat[i], feat[j], all.get(i), all.get(j)); if (sm > top) { top = sm; topPair = all.get(i).title + " / " + all.get(j).title; } }
        check("S1 no two songs are nearly the same (the closest pair: " + String.format("%.3f", top) + " " + topPair + ")", top < 0.985);
        boolean fiveEach = true;
        for (int km = 0; km <= 6; km++) { int kn5 = 0; for (Score sc : all) if (sc.moods.containsKey(km) && sc.moods.get(km) >= 3) kn5++; if (kn5 < 5) { fiveEach = false; System.out.println("[T] info: mood " + km + " has only " + kn5 + " songs written for it"); } }
        check("S2 every kind of place has at least five songs written for it", fiveEach);

        System.out.println("[T] ===== " + pass + " passed, " + fail + " failed =====");
        System.exit(fail == 0 ? 0 : 1);
    }
}
