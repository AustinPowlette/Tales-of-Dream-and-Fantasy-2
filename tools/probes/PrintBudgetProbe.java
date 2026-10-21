package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-13: the console print budget. What the GUI prints at every prompt (`settings hud off`) used to be the whole backpack, the herb and metal bag, the pill bag, the spouse and the place:
 *  7 lines and 550 characters a week for a person who only meditates. Now it is one line. Checks: no bag dump in the output, a MANUAL meditating week costs at most 4 lines on average,
 *  an AUTO year at most 2 lines a week, and `bag` still shows the bags. */
public class PrintBudgetProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    public static void main(String[] a) throws Exception {
        PcProbe.real = System.out;
        System.setIn(PcProbe.in);
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(PcProbe.real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        try { run(); } catch (Throwable e) { PcProbe.real.println("[T] PROBE EXCEPTION " + e); e.printStackTrace(PcProbe.real); PcProbe.fails++; }
        PcProbe.real.println("[T] ===== " + PcProbe.passes + " passed, " + PcProbe.fails + " failed; gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }

    static int lines(String s) { int n = 0; for (String l : s.split("\n")) if (!l.trim().isEmpty()) n++; return n; }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        PcProbe.check("player created and prompting", p != null && PcProbe.in.blocked);
        if (p == null) return;
        Settings.lore = false; Settings.lessons = false; Settings.vignettes = false; Settings.hud = false; // "hud off" = what the GUI prints at every prompt
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        p.pc().squadPolicy = 1; p.pc().friendPolicy = 1;
        p.materialBag.add(new Item(Item.ItemType.Herb, 3, 400)); p.materialBag.add(new Item(Item.ItemType.Herb, 5, 90)); p.materialBag.add(new Item(Item.ItemType.Metal, 2, 700));
        int mk = PcProbe.mark();
        for (int i = 0; i < 52; i++) PcProbe.cmd(i % 4 == 0 ? "profession" : "meditate", 60);
        String manual = PcProbe.since(mk);
        int n = lines(manual);
        PcProbe.real.println("[T] info: MANUAL 52 weeks = " + n + " lines, " + manual.length() + " characters");
        PcProbe.check("B1 the prompt prints no bag dump (lines with 'Quantity:': " + count(manual, "Quantity:") + ")", count(manual, "Quantity:") == 0);
        PcProbe.check("B2 a manual week costs at most 4 lines on average (" + n + " in 52 weeks)", n <= 4 * 52);
        PcProbe.check("B3 the prompt still names the character and the place each week", count(manual, " is in the ") >= 40);
        mk = PcProbe.mark();
        PcProbe.cmd("bag", 30);
        PcProbe.check("B4 'bag' still lists the bags", PcProbe.since(mk).toLowerCase().contains("herb") || PcProbe.since(mk).contains("Quantity") || PcProbe.since(mk).toLowerCase().contains("backpack"));
        p.pc().mode = PlayerControl.Mode.MANUAL;
        mk = PcProbe.mark();
        PcProbe.cmd("auto 52", 300);
        String auto = PcProbe.since(mk);
        PcProbe.real.println("[T] info: AUTO 52 weeks = " + lines(auto) + " lines");
        PcProbe.check("B5 an AUTO year costs at most 2 lines a week (" + lines(auto) + " in 52 weeks)", lines(auto) <= 2 * 52);
        PcProbe.real.println("[T] DONE");
    }

    static int count(String s, String what) { int n = 0, i = 0; while ((i = s.indexOf(what, i)) >= 0) { n++; i += what.length(); } return n; }
}
