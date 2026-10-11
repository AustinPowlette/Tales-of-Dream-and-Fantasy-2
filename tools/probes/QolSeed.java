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
        PcProbe.pc().pc().squadPolicy = 2; // a squad request would ask a question and swallow the next command
        GameDriver.time = Math.max(GameDriver.time, 60);
        PcProbe.cmd("save seed");
        PcProbe.cmd("x");
        java.io.File meta = new java.io.File("saves/seed.meta"), dat = new java.io.File("saves/seed.dat");
        for (int i = 0; i < 600 && !(meta.exists() && dat.exists()); i++) Thread.sleep(100); // the save is written at the start of the next turn: wait for it before exiting
        Thread.sleep(1500);
        PcProbe.real.println("[T] seeded a save at week " + GameDriver.time);
        System.exit(0);
    }
}
