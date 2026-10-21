package Game;

import java.io.File;
import java.util.*;

/** 2026-10-13: the music engine's life cycle and its timing under load (needs an audio device and a SoundFont in soundfonts/ of the run folder; without a device it only reports).
 *  (1) Switching the music on and off, restarting it with a changed engine and clicking Next never leaves two music threads or silence (it used to leave six threads and then nothing: the old
 *  thread's last act switched the new one off). (2) The synthesiser is tuned where the Java runtime allows (256 voices, sinc). (3) With every core busy the notes still arrive on time: they are
 *  sent a quarter of a second ahead with the time stamp of their start, so a late wake-up of the loop moves no note. Headless: no game thread, a minute. */
public class MusicLifeProbe {
    static int passes = 0, fails = 0;
    static void check(String n, boolean ok) { if (ok) passes++; else fails++; System.out.println("[T] " + (ok ? "PASS " : "FAIL ") + n); }
    static void info(String s) { System.out.println("[T] info: " + s); }

    static boolean waitFor(java.util.function.BooleanSupplier c, long ms) throws Exception { long end = System.currentTimeMillis() + ms; while (System.currentTimeMillis() < end) { if (c.getAsBoolean()) return true; Thread.sleep(50); } return c.getAsBoolean(); }

    public static void main(String[] a) throws Exception {
        List<String> fonts = Ambient.fonts();
        info("soundfonts found: " + fonts);
        Settings.music = true; Settings.musicVolume = 1; Settings.musicEngine = "auto"; Settings.soundFont = "";
        Ambient.apply(); Ambient.settle();
        boolean sound = waitFor(() -> Ambient.status.equals("playing"), 20000);
        info("status after start: " + Ambient.status + " / engine " + Ambient.engine + " / last error: " + MidiOut.lastError);
        if (!sound) { info("no audio device or SoundFont here: the life-cycle checks are skipped"); System.out.println("[T] ===== " + passes + " passed, " + fails + " failed (skipped) ====="); System.exit(0); }
        check("M1 the music plays after a start and there is exactly one music thread (" + Ambient.threadsAlive() + ")", Ambient.threadsAlive() == 1);
        if (!fonts.isEmpty()) check("M2 with a SoundFont present the engine is that SoundFont (" + Ambient.engine + ")", Ambient.engine.equals(fonts.get(0)) || Ambient.engine.startsWith(fonts.get(0)));
        info("tuning: " + Ambient.tuning);
        check("M2b the synthesiser says how it was set up", !Ambient.tuning.isEmpty() || !fonts.isEmpty() == false);

        // ---- rapid on/off
        for (int i = 0; i < 12; i++) { Settings.music = i % 2 == 1; Ambient.apply(); Thread.sleep(i % 3 == 0 ? 0 : 30); }
        Settings.music = true; Ambient.apply(); Ambient.settle();
        boolean again = waitFor(() -> Ambient.status.equals("playing"), 20000);
        Thread.sleep(1500);
        check("M3 after twelve quick Off/On clicks exactly one thread is playing (threads " + Ambient.threadsAlive() + ", status " + Ambient.status + ")", again && Ambient.threadsAlive() == 1 && Ambient.status.equals("playing"));
        Settings.music = false; Ambient.apply(); Ambient.settle(); Thread.sleep(500);
        check("M4 Off ends the thread (" + Ambient.threadsAlive() + ") and says so (" + Ambient.status + ")", Ambient.threadsAlive() == 0 && Ambient.status.equals("off"));
        Settings.music = true; Ambient.apply(); Ambient.settle(); waitFor(() -> Ambient.status.equals("playing"), 20000);

        // ---- engine changes
        for (String eng : new String[]{"builtin", "soundfont", "builtin", "auto"}) {
            Settings.musicEngine = eng; Ambient.apply();
        }
        Ambient.settle(); waitFor(() -> Ambient.status.equals("playing"), 20000); Thread.sleep(1000);
        check("M5 four engine changes in a row leave one thread (" + Ambient.threadsAlive() + ") playing (" + Ambient.status + ", " + Ambient.engine + ")", Ambient.threadsAlive() == 1 && Ambient.status.equals("playing"));

        // ---- Next x40
        for (int i = 0; i < 40; i++) { Ambient.newPiece(); Thread.sleep(10); }
        Thread.sleep(1000);
        check("M6 forty clicks on Next leave one thread (" + Ambient.threadsAlive() + ")", Ambient.threadsAlive() == 1);

        // ---- timing under load
        Settings.musicEngine = "auto"; Ambient.apply(); Ambient.settle(); waitFor(() -> Ambient.status.equals("playing"), 20000);
        if (Ambient.engine.equals("built-in") || Ambient.engine.startsWith("built-in")) { info("the built-in synthesiser plays here: the timing checks are for the SoundFont engine and are skipped"); }
        else {
            Ambient.pin(null);
            Thread.sleep(4000); // let a song start
            final int cores = Runtime.getRuntime().availableProcessors();
            final boolean[] stop = {false};
            List<Thread> burners = new ArrayList<>();
            for (int i = 0; i < cores * 2; i++) { Thread t = new Thread(() -> { double x = 1; while (!stop[0]) { for (int k = 0; k < 100000; k++) x = Math.sin(x) + Math.sqrt(x + 2); } }); t.setDaemon(true); burners.add(t); t.start(); }
            long sent0 = Ambient.notesSent, late0 = Ambient.notesLate;
            Ambient.maxGapMs = 0;
            Thread.sleep(15000);
            stop[0] = true;
            long sent = Ambient.notesSent - sent0, late = Ambient.notesLate - late0;
            info("with " + cores * 2 + " busy threads: " + sent + " notes sent in 15 s, " + late + " sent too late, longest pause of the loop " + Ambient.maxGapMs + " ms, most voices " + Ambient.maxVoices);
            check("M7 under full load no note is sent after its time (late " + late + " of " + sent + ", longest pause " + Ambient.maxGapMs + " ms against a look-ahead of " + Ambient.LOOK_MS + " ms)", sent > 0 && late == 0);
            check("M8 the loop never paused as long as the look-ahead (" + Ambient.maxGapMs + " ms)", Ambient.maxGapMs < Ambient.LOOK_MS);
        }
        check("M9 `music status` has a report", Ambient.statusReport().contains("Music threads alive"));
        Settings.music = false; Ambient.apply(); Ambient.settle();
        System.out.println("[T] ===== " + passes + " passed, " + fails + " failed =====");
        System.exit(0);
    }
}
