package Game;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Terminal-usability driver: starts a 3,000-person world with one human, sends the commands listed (one per line) in the file given as
 *  args[0] (blank lines and lines starting with // are skipped; "@wait N" = pass N turns with 'wait', "@frozen" freezes NPCs, "@strong" makes
 *  the player unkillable-ish) and writes everything the game printed, command by command, to args[1] (default termdriver.out). */
public class TermDriver {
    public static void main(String[] a) throws Exception {
        PcProbe.real = System.out;
        System.setIn(PcProbe.in);
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(PcProbe.real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        List<String> cmds = Files.readAllLines(Paths.get(a[0]), java.nio.charset.StandardCharsets.UTF_8);
        String outFile = a.length > 1 ? a[1] : "termdriver.out";
        StringBuilder log = new StringBuilder();
        try {
            PcProbe.in.feed("n\ns\n1\nn\n");
            PcProbe.awaitBlocked(180);
            Character p = PcProbe.pc();
            log.append("### START (player ").append(p == null ? "none" : p.name + " " + p.lastName).append(")\n").append(PcProbe.out()).append("\n");
            DecisionBroker.scriptedNumbers = new ArrayDeque<>();
            for (String line : cmds) {
                String l = line.trim();
                if (l.isEmpty() || l.startsWith("//")) continue;
                int m = PcProbe.mark();
                if (l.equals("@frozen")) { GameDriver.frozenNpcs = true; continue; }
                if (l.equals("@strong")) { p.lvl = Math.max(p.lvl, 100); p.majorLvl = 10; p.minorLvl = 0; p.maxAge = 100000; p.caution = 0; continue; }
                if (l.startsWith("@wait ")) { PcProbe.waitTurns(Integer.parseInt(l.substring(6).trim())); log.append("### [wait ").append(l.substring(6)).append("]\n").append(PcProbe.since(m)).append("\n"); continue; }
                if (l.startsWith("@eval ")) { continue; }
                PcProbe.cmd(l, 120);
                log.append("### ").append(l).append("\n").append(PcProbe.since(m));
                if (!PcProbe.since(m).endsWith("\n")) log.append("\n");
            }
        } catch (Throwable e) { log.append("### DRIVER EXCEPTION ").append(e).append("\n"); e.printStackTrace(PcProbe.real); }
        try (PrintWriter w = new PrintWriter(outFile, "UTF-8")) { w.print(log); }
        PcProbe.real.println("[TermDriver] wrote " + outFile + " gameError=" + PcProbe.gameError);
        System.exit(0);
    }
}
