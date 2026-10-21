package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-19 (story seasons): the season engine and what a player sees of it, headless. The Heir is taken to the end of season 1: the interlude plays, `story` lists the seasons and says what the next chapter waits for, the first
 *  scene of season 2 opens with its title, the Story window's page has the season headings, an interlude can be read again, a person to love takes the other gender than the human's (and the pronoun tokens follow), a Major-level gate
 *  and a wish lapse after the patience, a "reach Major N" task is waived, a real friend is taken into a story (`adopt`), a dead relative is named (`deadKin`), a companion is kept and noticed when it dies, the echoes are kept. */
public class SeasonsProbe {
    static void check(String name, boolean ok) { PcProbe.passes += ok ? 1 : 0; PcProbe.fails += ok ? 0 : 1; PcProbe.real.println("[S] " + (ok ? "PASS " : "FAIL ") + name); }

    public static void main(String[] a) throws Exception {
        PcProbe.real = System.out;
        System.setIn(PcProbe.in);
        GameDriver.frozenNpcs = true;
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(PcProbe.real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        try { run(); } catch (Throwable e) { PcProbe.real.println("[S] PROBE EXCEPTION " + e); e.printStackTrace(PcProbe.real); PcProbe.fails++; }
        PcProbe.real.println("[S] ===== " + PcProbe.passes + " passed, " + PcProbe.fails + " failed; gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }

    static void tp(Character c, Area to) { c.location.removeChar(c); c.location = to; to.addChar(c); }
    static void weeks(int w) { GameDriver.time += w; }
    static void years(int y) { GameDriver.time += y * 52; }
    static void answers(int... a) { DecisionBroker.scripted.clear(); for (int x : a) DecisionBroker.scripted.add(x); }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        GameDriver.frozenNpcs = true;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        p.caution = 0; p.maxAge = 100000; p.pc().squadPolicy = 2; p.pc().friendPolicy = 1; p.age = 30; p.spouse = null;
        GameDriver.time = Math.max(GameDriver.time, 100);
        Map.currentCharacter = p;

        // ---- the gender of a person to love, and the pronouns
        p.gender = "Male";
        Starts.Choice ch = new Starts.Choice(); ch.kind = Starts.Kind.STORY; ch.story = "heir"; ch.difficulty = 0;
        Starts.apply(p, ch);
        Starts.State st = p.story;
        Character wen = Chapters.spawn(p, st, "wen");
        check("S1 a man's Wen is a woman, named Wen (" + wen.gender + ")", "Female".equals(wen.gender) && wen.name.equals("Wen"));
        check("S1b the world's own marriage rule did not wed them on the spot (spouse=" + p.spouse + ")", p.spouse == null);
        check("S2 the tokens follow: {wen.He} {wen.his} {wen.man} -> " + ChapText.fill("{wen.He} {wen.his} {wen.man}", p, st), ChapText.fill("{wen.He} {wen.his} {wen.man}", p, st).equals("She her woman"));
        p.gender = "Female"; st.npcs.remove("wen");
        Character wen2 = Chapters.spawn(p, st, "wen");
        check("S3 a woman's Wen is a man (" + wen2.gender + ", " + ChapText.fill("{wen.he} {wen.him}", p, st) + ")", "Male".equals(wen2.gender) && ChapText.fill("{wen.he} {wen.him}", p, st).equals("he him"));
        p.gender = "Male";

        // ---- season 1 to its end, by the generic player
        Starts.apply(p, ch); st = p.story;
        p.lvl = 45; p.majorLvl = 4; p.age = 30; p.dead = false; p.itemsCrafted = 0;
        Chapters.Def d = Chapters.DEFS.get("heir");
        check("S4 the Heir has five seasons: three, the Green Seal and the Door (" + d.seasons.size() + ") and season 2 starts at chapter " + d.firstOfSeason(2), d.seasons.size() == 5 && d.firstOfSeason(2) == 13);
        int guard = 0;
        while (st.chapter < d.firstOfSeason(2) && guard++ < 400) {
            weeks(70); DecisionBroker.scripted.clear(); for (int i = 0; i < 6; i++) DecisionBroker.scripted.add(1);
            Starts.tick(p);
            if (st.open && st.cur != null) { Chapters.Chapter c = d.find(st.cur); if (c != null) { TalesProbe.satisfy(p, st, c); Starts.tick(p); } }
        }
        check("S5 season 1 ended: the interlude was played and kept (" + st.seen.contains("interlude.1") + "), the story is not done (" + st.done + ")", st.seen.contains("interlude.1") && !st.done && !st.open);
        String rep = Chapters.replay(p, st, "interlude.1");
        check("S6 the interlude can be read again", rep.contains("Chimney") || rep.contains("INTERLUDE") || rep.toUpperCase().contains("THE CHIMNEY"));

        // ---- what `story` says while season 2 waits
        String desc = Starts.describe(p);
        check("S7 `story` lists the seasons and the wait", desc.contains("Season 1") && desc.contains("Season 2: The Silent Tonic") && desc.contains("waiting for"));
        List<String[]> rows = Chapters.rows(p, st);
        boolean head = false; for (String[] r : rows) if (r.length > 3 && r[3].startsWith("Season 2")) head = true;
        check("S8 the Story window's rows carry the season headings", head);
        String html = GuiPages.storyHtml(p);
        check("S9 the Story window's page shows Season 2 and the wait", html.contains("Season 2") && html.toLowerCase().contains("waiting"));

        // ---- the patience rule: a Major-level gate and a wish lapse after six more years
        Chapters.Chapter first = d.chapters.get(st.chapter);
        p.majorLvl = 0; p.lvl = 5;
        Chapters.Chapter g = new Chapters.Chapter("probe_gate", "Gate").years(1).major(3).soft((c, s) -> false, "a wish that will not come").wait(5);
        d.chapters.add(st.chapter, g); // (the probe puts a chapter in front of the line and takes it out again)
        st.lastEnd = GameDriver.time;
        weeks(52 + 2);
        Starts.tick(p);
        check("S10 a Major-level gate and a wish hold a chapter back while the patience lasts (open=" + st.open + ")", !st.open);
        weeks(Chapters.PATIENCE + 2);
        Starts.tick(p);
        String why = ""; try { java.lang.reflect.Method m = Chapters.class.getDeclaredMethod("eligible", Character.class, Starts.State.class, Chapters.Chapter.class); m.setAccessible(true); why = " eligible=" + m.invoke(null, p, st, g) + " waited=" + (GameDriver.time - st.lastEnd) + " chapter=" + st.chapter + "/" + d.chapters.size() + " at=" + d.chapters.get(Math.min(st.chapter, d.chapters.size() - 1)).id + " busy=" + GuiLore.busy() + " done=" + st.done; } catch (Throwable e) { why = " " + e; }
        check("S11 after the patience they lapse and the chapter opens (open=" + st.open + ", cur=" + st.cur + why + ")", st.open && "probe_gate".equals(st.cur));
        st.open = false; st.cur = null; d.chapters.remove(g); st.seen.remove("probe_gate");
        p.majorLvl = 4; p.lvl = 45;

        // ---- season 2 opens with its title
        Chapters.shown.clear(); Chapters.transcript.clear();
        weeks(300);
        answers(1, 1, 1);
        Starts.tick(p);
        check("S12 the first scene of season 2 opened (" + st.cur + ")", st.open && "beds".equals(st.cur));
        check("S13 it begins with `Season 2: The Silent Tonic`", Chapters.transcript.stream().anyMatch(l -> l.contains("Season 2: The Silent Tonic")));
        check("S14 the Quest Log holds its task", TalesProbe.questCount(p, st) == 1);

        // ---- echoes
        Chapters.echo(p, st);
        check("S15 the echoes are kept (male=" + Chapters.flag(st, "e.male") + ", sect=" + Chapters.flag(st, "e.sect") + ", beasts=" + Chapters.flag(st, "e.beasts") + ")", Chapters.flag(st, "e.male") == 1 && Chapters.flag(st, "e.female") == 0 && Chapters.flag(st, "e.sect") == (p.sect != null ? 1 : 0));

        // ---- a companion beast of a story: named, noticed when it dies
        Starts.apply(p, TalesProbe.story("tamer", 0)); st = p.story;
        Character fox = Chapters.companion(p, st, "fox", "Burdock", "Grey Fox", 6, true);
        check("S16 the fox is a companion kept among the story's people, named Burdock, and a she (" + ChapText.fill("{fox.first} {fox.he}", p, st) + ")", fox != null && Chapters.person(st, "fox") == fox && ChapText.fill("{fox.first} {fox.he}", p, st).equals("Burdock she"));
        fox.dead = true; ChapText.refresh(st);
        check("S17 when the fox dies the text can tell (dead.fox=" + Chapters.flag(st, "dead.fox") + ")", Chapters.flag(st, "dead.fox") == 1);

        // ---- a real friend is taken into a story; a dead relative is named
        Starts.apply(p, TalesProbe.story("ghost", 0)); st = p.story;
        Character friend = TalesProbe.adult(8, 1); p.changeRelationship(friend, 3);
        friend.changeRelationship(p, 3);
        Character got = Chapters.adopt(p, st, "friend", "friend");
        check("S18 a friend of the human is taken into the story, not made up (" + (got == null ? "none" : got.name + (got == friend ? " = the friend" : " (another)")) + ")", got != null && got == friend && !got.storyNpc);
        Character mother = new Character(p.location); mother.name = "Hana"; mother.lastName = "Quill"; mother.gender = "Female"; mother.dead = true;
        p.mother = mother;
        boolean kin = Chapters.deadKin(p, st);
        check("S19 a dead mother is named in the text (" + kin + ", " + ChapText.fill("{=kinrel}: {=kin}", p, st) + ")", kin && ChapText.fill("{=kinrel}|{=kin}", p, st).equals("your mother|Hana Quill"));
        p.mother = null;

        // ---- a person who may be loved can be wed; the rule is the same as the world's
        Starts.apply(p, TalesProbe.story("smith", 0)); st = p.story; p.gender = "Male"; p.age = 30;
        Character marl = Chapters.spawn(p, st, "marl"); marl.age = 30;
        check("S20 Marl, for a man, is a woman (" + marl.gender + ")", "Female".equals(marl.gender));
        boolean wed = Chapters.wed(p, st, "marl");
        Chapters.Chapter mc = new Chapters.Chapter("probe_made", "Made").made(100000, "make a great many things");
        st.openedWeek = GameDriver.time;
        boolean early = Chapters.done(p, st, mc);
        st.openedWeek = GameDriver.time - Chapters.MADE_WAIVER - 1;
        check("S22 a counted 'make N' task lapses after four years (early=" + early + ", late=" + Chapters.done(p, st, mc) + ")", !early && Chapters.done(p, st, mc));
        check("S21 they can wed (" + wed + ", spouse=" + (p.spouse == marl) + "; p.spouse=" + p.spouse + " dead=" + p.dead + " age=" + p.age + " marl.age=" + marl.age + " gender=" + marl.gender + "/" + p.gender + ")", wed && p.spouse == marl && marl.spouse == p);
    }
}
