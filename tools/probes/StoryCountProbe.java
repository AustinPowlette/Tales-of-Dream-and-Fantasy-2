package Game;

import java.io.PrintStream;

/** 2026-10-14: a counted story task ("make twenty-five items") counts the items made SINCE THE CHAPTER BEGAN (the chapter's setup stores the number already made), says so, and shows "made 12 of 25 since this
 *  chapter began" in its text and in the Quest Log label (the author took Profession Lv 26 for the moment the task fired: the total counts from the start of the chapter, not of the game). */
public class StoryCountProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    static void check(String n, boolean ok) { PcProbe.check(n, ok); }

    public static void main(String[] a) throws Exception {
        PcProbe.real = System.out;
        System.setIn(PcProbe.in);
        GameDriver.frozenNpcs = true;
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(PcProbe.real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        try { run(); } catch (Throwable e) { PcProbe.real.println("[T] PROBE EXCEPTION " + e); e.printStackTrace(PcProbe.real); PcProbe.fails++; }
        PcProbe.real.println("[T] ===== " + PcProbe.passes + " passed, " + PcProbe.fails + " failed; gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        check("player created and prompting", p != null && PcProbe.in.blocked);
        if (p == null) return;
        Settings.lore = false;
        Starts.State st = new Starts.State();
        st.story = "smith";
        Chapters.Chapter ch = new Chapters.Chapter("probe", "A Probe Chapter").made(25, "Learn the trade: make twenty-five items.");
        ch.setup = (c, s) -> Chapters.set(s, "craft0", c.itemsCrafted);
        p.itemsCrafted = 40; // forty items already made before the chapter began
        ch.setup.run(p, st);
        check("C1 the chapter begins with the count of items already made stored (" + Chapters.flag(st, "craft0") + ")", Chapters.flag(st, "craft0") == 40);
        check("C1b the task text says 0 of 25 and 'since this chapter began' (" + Chapters.taskText(ch, p, st) + ")", Chapters.taskText(ch, p, st).contains("made 0 of 25 since this chapter began"));
        check("C1c forty items made before do not finish it", !Chapters.done(p, st, ch));
        p.itemsCrafted = 52;
        check("C2 after 12 more the text says 12 of 25 (" + Chapters.taskText(ch, p, st) + ")", Chapters.taskText(ch, p, st).contains("made 12 of 25"));
        p.itemsCrafted = 64;
        check("C3 24 more is not 25: not done", !Chapters.done(p, st, ch));
        p.itemsCrafted = 65;
        check("C3b the twenty-fifth item since the chapter began finishes it (total " + p.itemsCrafted + ")", Chapters.done(p, st, ch) && Chapters.taskText(ch, p, st).contains("made 25 of 25"));
        p.itemsCrafted = 90;
        check("C3c the count never shows more than the task asks (" + Chapters.taskText(ch, p, st) + ")", Chapters.taskText(ch, p, st).contains("made 25 of 25"));
        // the stories that use it
        boolean smithOk = false, heirOk = false;
        for (Chapters.Chapter c : Chapters.DEFS.get("smith").chapters) if (c.counter != null) smithOk = true;
        for (Chapters.Chapter c : Chapters.DEFS.get("heir").chapters) if (c.counter != null) heirOk = true;
        check("C4 the Smith's and the Heir's counted chapters carry the counter", smithOk && heirOk);
        int counted = 0, sum = 0; for (Chapters.Chapter c : Chapters.DEFS.get("smith").chapters) if (c.season == 1 && c.counter != null) { counted++; sum += c.counterN; }
        check("C4b the Smith's first season asks for six small counts of pieces (3, 4, 6, 3, 3, 5 = 24, not 215): " + counted + " counts, " + sum + " pieces", counted == 6 && sum == 24);
        System.out.println();
        PcProbe.real.println("[T] DONE");
    }
}
