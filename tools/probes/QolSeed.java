package Game;

import java.io.PrintStream;

/** Makes a saved game with a human character (week > 52) in the current folder, for the probes that start with "Continue" (QolGui). */
public class QolSeed {
    public static void main(String[] a) throws Exception {
        PcProbe.real = System.out;
        System.setIn(PcProbe.in);
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { e.printStackTrace(PcProbe.real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        GameDriver.time = Math.max(GameDriver.time, 60);
        PcProbe.cmd("save seed");
        PcProbe.cmd("x");
        PcProbe.real.println("[T] seeded a save at week " + GameDriver.time);
        System.exit(0);
    }
}
