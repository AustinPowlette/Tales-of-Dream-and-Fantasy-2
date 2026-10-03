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
 *  Instruments are General MIDI programs (koto, flute, fiddle, warm pad, tubular bells, crystal, taiko, wood block, shamisen); a font that
 *  numbers its presets differently can be mapped in `soundfonts/programs.txt`, one `instrument=program` or `instrument=bank:program` per line
 *  (instruments: pluck flute erhu pad bell chime taiko wood pipa). */
final class MidiOut {
    static volatile String lastError = "";
    private Synthesizer syn;
    private final MidiChannel[] ch = new MidiChannel[16];
    private final int[] chan = new int[Ambient.INST.length];

    private MidiOut() { }

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
            int[] bankOf = new int[gm.length], prog = gm.clone();
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
                            if (val.contains(":")) { bankOf[i] = Integer.parseInt(val.substring(0, val.indexOf(':')).trim()); prog[i] = Integer.parseInt(val.substring(val.indexOf(':') + 1).trim()); }
                            else prog[i] = Integer.parseInt(val);
                        } catch (NumberFormatException ignored) { }
                    }
                }
            }
            for (int i = 0; i < gm.length; i++) {
                int c = i < 9 ? i : i + 1;
                m.chan[i] = c;
                m.ch[c] = cs[c];
                if (bankOf[i] != 0) { m.ch[c].controlChange(0, bankOf[i] >> 7); m.ch[c].controlChange(32, bankOf[i] & 127); }
                m.ch[c].programChange(prog[i]);
                m.ch[c].controlChange(91, 70);  // a little hall from the font's own reverb
                m.ch[c].controlChange(10, new int[]{50, 80, 45, 64, 64, 70, 64, 80, 78}[i % 9]);
            }
            return m;
        } catch (Throwable e) {
            lastError = e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage());
            m.close();
            return null;
        }
    }

    void volumes(double volume, double[] layer) {
        for (int i = 0; i < chan.length; i++) {
            MidiChannel c = ch[chan[i]];
            if (c != null) c.controlChange(7, (int) Math.max(0, Math.min(127, 127 * Math.sqrt(volume) * layer[i] * 0.9)));
        }
    }

    void on(int inst, int midi, double vel) {
        MidiChannel c = ch[chan[inst]];
        if (c != null) c.noteOn(Math.max(0, Math.min(127, midi)), (int) Math.max(1, Math.min(127, vel * 127)));
    }

    void off(int inst, int midi) {
        MidiChannel c = ch[chan[inst]];
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
