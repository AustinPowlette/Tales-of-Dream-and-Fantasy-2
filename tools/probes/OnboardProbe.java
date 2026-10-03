package Game;

import java.io.*;
import java.util.*;

/** First-hour measurement: a new Standard character on MANUAL plays N turns by simply waiting (the most passive player there is) while every
 *  prompt is answered with its default. Reports: level at the start and the end, alerts, decision prompts, fights, whether and when they died.
 *  Usage: java Game.OnboardProbe <mature|level1> <turns> */
public class OnboardProbe {
    public static void main(String[] a) throws Exception {
        boolean mature = a.length < 1 || a[0].equals("mature");
        int turns = a.length > 1 ? Integer.parseInt(a[1]) : 156;
        Settings.matureStart = mature;
        PcProbe.real = System.out;
        System.setIn(PcProbe.in);
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(PcProbe.real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        PcProbe.in.feed("n\ns\n2\nn\n"); // no viewer, single player, Standard (fate rolls you), no edit
        PcProbe.awaitBlocked(300);
        Character p = PcProbe.pc();
        DecisionBroker.scripted = null;
        int lvl0 = p.lvl, major0 = p.majorLvl, pot = p.potential;
        int alerts = 0, prompts0 = DecisionBroker.prompts, fights = 0, challenges = 0, diedAt = -1, wins = 0;
        int mark = PcProbe.mark();
        long ss0 = p.ss;
        for (int i = 0; i < turns && !p.dead; i++) {
            int before = PcProbe.mark();
            PcProbe.in.feed("x\n");
            PcProbe.awaitBlocked(120);
            String out = PcProbe.since(before);
            for (String line : out.split("\n")) {
                if (line.contains("[Quest Alert]")) alerts++;
                if (line.contains("has challenged you") || line.contains("challenges you") || line.contains("attacks you")) challenges++;
                if (line.contains("won the fight")) { fights++; if (line.contains(p.name)) wins++; }
            }
            if (p.dead) { diedAt = i + 1; break; }
        }
        System.out.flush();
        PcProbe.real.println("[ONBOARD] mode=" + (mature ? "mature" : "level1") + " potential=" + pot + " start level " + lvl0 + " (major " + major0 + ") end level " + p.lvl + " (major " + p.majorLvl + ") turns " + (diedAt < 0 ? turns : diedAt)
                + " dead=" + p.dead + " alerts=" + alerts + " (" + String.format("%.2f", alerts / (double) Math.max(1, diedAt < 0 ? turns : diedAt)) + "/turn) prompts=" + (DecisionBroker.prompts - prompts0) + " fights=" + fights + " challengesSeen=" + challenges + " wealthDelta=" + (p.ss - ss0) + " gameError=" + PcProbe.gameError);
        System.exit(0);
    }
}
