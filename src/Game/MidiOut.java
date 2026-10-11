package Game;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Patch;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/** Plays `Ambient`'s notes through Java's software MIDI synthesiser with a SoundFont (`.sf2` / `.dls`) from the soundfonts/ folder.
 *  Instruments are General MIDI programs (koto, flute, fiddle, warm pad, tubular bells, crystal, taiko, wood block, shamisen, ... see `Ambient.GM`); a font that
 *  numbers its presets differently can be mapped in `soundfonts/programs.txt`, one `instrument=program` or `instrument=bank:program` per line (the names are
 *  `Ambient.INST`). A synthesiser has sixteen channels and the game has more instruments than that, so an instrument gets a channel when its first note comes
 *  and the channel that has been quiet the longest is given to another instrument when none is free (a song uses ten or twelve at most, so this does not
 *  happen within a song). The drum kit is the MIDI drum channel (9): the pitch of a note is the drum. */
final class MidiOut {
    static volatile String lastError = "";
    /** Stereo position (0-127) of each instrument's channel. */
    static final int[] PAN = {50, 80, 45, 64, 64, 70, 64, 80, 78, 58, 84, 64, 46, 70, 64, 64, 62, 90, 40, 74, 76, 36, 64, 56};
    private static final int DRUM_CHANNEL = 9;
    private Synthesizer syn;
    private final MidiChannel[] ch = new MidiChannel[16];
    private final int[] chan = new int[Ambient.INST.length];      // channel of each instrument, -1 = none yet
    private final int[] owner = new int[16];                       // instrument on each channel, -1 = free
    private final long[] used = new long[16];                      // when the channel last played a note
    private long tick = 0;
    private int[] prog, bankOf;
    private double curVol = 0;
    private double[] curLayer = new double[Ambient.INST.length];

    private MidiOut() { java.util.Arrays.fill(chan, -1); java.util.Arrays.fill(owner, -1); java.util.Arrays.fill(curLayer, 1.0); }

    static MidiOut open(File sf, int[] gm) {
        MidiOut m = new MidiOut();
        try {
            m.syn = MidiSystem.getSynthesizer();
            m.syn.open();
            Soundbank bank = MidiSystem.getSoundbank(sf);
            if (!m.syn.isSoundbankSupported(bank)) { lastError = "format not supported"; m.syn.close(); return null; }
            m.syn.unloadAllInstruments(m.syn.getDefaultSoundbank());
            if (!m.syn.loadAllInstruments(bank)) { lastError = "could not load the instruments"; m.syn.close(); return null; }
            MidiChannel[] cs = m.syn.getChannels();
            for (int k = 0; k < 16 && k < cs.length; k++) m.ch[k] = cs[k];
            m.bankOf = new int[gm.length]; m.prog = gm.clone();
            File map = new File(sf.getParentFile(), "programs.txt");
            if (map.isFile()) try (BufferedReader br = new BufferedReader(new FileReader(map))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    int eq = line.indexOf('=');
                    if (line.startsWith("#") || eq < 0) continue;
                    String name = line.substring(0, eq).trim(), val = line.substring(eq + 1).trim();
                    for (int i = 0; i < Ambient.INST.length; i++) if (Ambient.INST[i].equalsIgnoreCase(name)) {
                        try {
                            if (val.contains(":")) { m.bankOf[i] = Integer.parseInt(val.substring(0, val.indexOf(':')).trim()); m.prog[i] = Integer.parseInt(val.substring(val.indexOf(':') + 1).trim()); }
                            else m.prog[i] = Integer.parseInt(val);
                        } catch (NumberFormatException ignored) { }
                    }
                }
            }
            MidiChannel d = m.ch[DRUM_CHANNEL];
            if (d != null) { d.programChange(m.prog[Ambient.KIT]); d.controlChange(91, 45); d.controlChange(10, PAN[Ambient.KIT]); }
            return m;
        } catch (Throwable e) {
            lastError = e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage());
            m.close();
            return null;
        }
    }

    /** Which channel each of these instruments plays on when a whole song is rendered at once (the preview tool): the kit on the drum channel, the others in turn. */
    static int[] staticMap(java.util.Set<Integer> insts) {
        int[] map = new int[Ambient.INST.length];
        java.util.Arrays.fill(map, -1);
        map[Ambient.KIT] = DRUM_CHANNEL;
        int c = 0;
        for (int i : new java.util.TreeSet<Integer>(insts)) {
            if (i == Ambient.KIT) continue;
            if (c == DRUM_CHANNEL) c++;
            map[i] = c++ % 16;
        }
        return map;
    }

    /** Loudness of each instrument in the bundled SoundFont (GeneralUser GS) relative to the others, in dB to add: measured with `SongTool trim`, so a song's own levels mean the same for every instrument. */
    static final double[] TRIM_DB = {5.2, -5.3, -5.3, 5.3, -3.0, -0.9, 0.5, 9.7, 11.4, -5.9, -1.4, -1.1, -4.6, -7.3, -0.3, 3.0, -5.3, 2.2, 5.7, -4.9, -0.7, 0.9, 3.0, -0.8};

    /** The channel volume (controller 7) of an instrument for a music volume 0..1 and a mix layer 0..1.5. */
    static int cc7(int inst, double volume, double layer) {
        return (int) Math.max(0, Math.min(127, 127 * Math.sqrt(volume) * layer * 0.9 * Math.pow(10, TRIM_DB[inst] / 40)));
    }

    void volumes(double volume, double[] layer) {
        curVol = volume; curLayer = layer.clone();
        for (int i = 0; i < chan.length; i++) {
            int c = i == Ambient.KIT ? DRUM_CHANNEL : chan[i];
            if (c >= 0 && ch[c] != null) ch[c].controlChange(7, cc7(i, volume, layer[i]));
        }
    }

    /** The channel for an instrument: its own, or a free one, or the one that has been quiet the longest (given the instrument's program, pan and volume at once). */
    private int channelFor(int inst) {
        if (inst == Ambient.KIT) return DRUM_CHANNEL;
        int c = chan[inst];
        if (c >= 0) { used[c] = ++tick; return c; }
        int best = -1;
        for (int k = 0; k < 16; k++) {
            if (k == DRUM_CHANNEL || ch[k] == null) continue;
            if (owner[k] < 0) { best = k; break; }
            if (best < 0 || used[k] < used[best]) best = k;
        }
        if (best < 0) return DRUM_CHANNEL;
        if (owner[best] >= 0) chan[owner[best]] = -1;
        owner[best] = inst; chan[inst] = best; used[best] = ++tick;
        MidiChannel m = ch[best];
        m.controlChange(0, bankOf[inst] >> 7); m.controlChange(32, bankOf[inst] & 127);
        m.programChange(prog[inst]);
        m.controlChange(91, 70);  // a little hall from the font's own reverb
        m.controlChange(10, PAN[inst % PAN.length]);
        m.controlChange(7, cc7(inst, curVol, curLayer[inst]));
        return best;
    }

    /** Starts a note; returns the channel it was played on (for the note-off). */
    int on(int inst, int midi, double vel) {
        int c = channelFor(inst);
        MidiChannel m = ch[c];
        if (m != null) m.noteOn(Math.max(0, Math.min(127, midi)), (int) Math.max(1, Math.min(127, vel * 127)));
        return c;
    }

    void off(int channel, int midi) {
        if (channel == DRUM_CHANNEL) return; // a drum is struck, not held: its note-off could only cut the next hit of the same drum short
        MidiChannel c = channel >= 0 && channel < 16 ? ch[channel] : null;
        if (c != null) c.noteOff(Math.max(0, Math.min(127, midi)));
    }

    void allOff() { for (MidiChannel c : ch) if (c != null) c.allNotesOff(); }

    void close() { try { if (syn != null) syn.close(); } catch (Throwable ignored) { } }

    /** The names of the presets inside a SoundFont file (for `music presets`). */
    static List<String> presets(File sf) {
        List<String> out = new ArrayList<>();
        try {
            Soundbank b = MidiSystem.getSoundbank(sf);
            for (javax.sound.midi.Instrument in : b.getInstruments()) {
                Patch p = in.getPatch();
                out.add(p.getBank() + ":" + p.getProgram() + "  " + in.getName());
            }
        } catch (Throwable e) { out.add("(could not read: " + e.getMessage() + ")"); }
        return out;
    }
}
