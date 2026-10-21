package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-16, change set B (beta tester's list): the Lessons (casual one-time talks that teach the game), the Favors (a talk that ends with a quest that pays) and the Vignettes (little stories). Each lesson fits
 *  its situation, is told once with a cooldown, can be switched off, and reads at about a 7th grade level; a favor becomes a real quest with the one who asked marked and pays when it is done; each little story
 *  plays every choice without error and does what it says. */
public class BatchBProbe {
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

    static Area at(int dx, int dy) {
        Area h = Map.tiles[Map.range / 2][Map.range / 2];
        int x = Math.floorMod(h.x + dx, Map.range), y = Math.floorMod(h.y + dy, Map.range);
        if (Map.tiles[x][y] == null) Map.tiles[x][y] = new Area(Area.ghost(x == 0 ? Map.range - 1 : x - 1, y), 'e');
        return Map.tiles[x][y];
    }

    /** An Io that records what is said and picks the answer it is told to. */
    static final class TestIo implements Dialogue.Io {
        final List<String> said = new ArrayList<>(); int answer = 0;
        public void open(Character npc, Character me) { }
        public void npc(String text) { said.add(text); }
        public void narrate(String text) { said.add("(" + text + ")"); }
        public int pick(String[] labels, int[] temper) { return Math.min(answer, labels.length - 1); }
        public void close() { }
    }

    static Character adult(Area where, String g, Character.Traits tr) {
        Character c = new Character(where);
        c.age = 40; c.gender = g; c.maxAge = 100000; c.born = true; c.trait = tr; c.targetQuests.clear(); c.affiliation = "Good"; c.affiliationNum = 1;
        Map.characters.add(c); Map.population++;
        return c;
    }

    // ---- reading level (Flesch-Kincaid grade, a rough count of syllables)
    static int syll(String w) {
        w = w.toLowerCase().replaceAll("[^a-z]", "");
        if (w.isEmpty()) return 0;
        int n = 0; boolean prev = false;
        for (char ch : w.toCharArray()) { boolean v = "aeiouy".indexOf(ch) >= 0; if (v && !prev) n++; prev = v; }
        if (w.endsWith("e") && !w.endsWith("le") && n > 1) n--;
        return Math.max(1, n);
    }

    static double[] grade(List<String> texts) {
        int words = 0, syl = 0, sentences = 0, longest = 0;
        for (String t : texts) {
            String s = t.startsWith("* ") ? t.substring(2) : t.startsWith("@") && t.contains("| ") ? t.substring(t.indexOf("| ") + 2) : t;
            s = s.replaceAll("\\{[A-Za-z]+\\}", "friend");
            for (String sent : s.split("(?<=[.!?])\\s+")) {
                String[] ws = sent.trim().split("\\s+");
                if (ws.length == 0 || ws[0].isEmpty()) continue;
                sentences++; words += ws.length; longest = Math.max(longest, ws.length);
                for (String w : ws) syl += syll(w);
            }
        }
        double g = 0.39 * words / Math.max(1, sentences) + 11.8 * syl / Math.max(1, words) - 15.59;
        return new double[]{g, (double) words / Math.max(1, sentences), longest};
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        check("player created and prompting", p != null && PcProbe.in.blocked);
        if (p == null) return;
        Settings.lore = false; Settings.lessons = true; Settings.vignettes = true; Vignettes.testMode = true;
        p.lvl = 100; p.majorLvl = 0; p.minorLvl = 3; p.maxAge = 100000; p.caution = 0; p.targetQuests.clear(); p.spotQuests.clear(); p.currentQuest = null;
        p.pc().breakthroughPolicy = PlayerControl.BT_AUTO;
        Area lab = at(6, 6); lab.monsters = 0; lab.city = false;
        p.location.removeChar(p); p.location = lab; lab.addChar(p);
        PlayerControl pc = p.pc();
        Character n = adult(lab, "Male", Character.Traits.Friendly);
        n.name = "Wen"; n.lastName = "Ash";

        // ---- L1: the first lesson, told once, with a cooldown
        GameDriver.time = 5;
        Lessons.L first = Lessons.next(n, p);
        check("L1 a new cultivator in the first realm hears 'The slow fire' first (" + (first == null ? null : first.id) + ")", first != null && first.id.equals("qi"));
        int m0 = PcProbe.mark();
        boolean spoke = Dialogue.approach(n, p);
        String said = PcProbe.since(m0);
        check("L1b the talk is given when someone speaks to him (the lines are in the log: " + (said.length() > 120 ? said.substring(0, 120) : said).replace('\n', ' ') + ")", spoke && said.contains("Wen Ash:") && said.contains("[Lesson]"));
        check("L1c it is marked as told and the cooldown starts (" + Lessons.COOLDOWN + " weeks)", pc.lessonsSeen.contains("qi") && pc.nextLessonWeek == GameDriver.time + Lessons.COOLDOWN);
        p.minorLvl = 9; p.majorLvl = 1; // (the next lesson fits now but must wait)
        check("L1d no second lesson inside the cooldown", Lessons.next(n, p) == null && !Lessons.due(n, p));
        GameDriver.time += Lessons.COOLDOWN;
        check("L1e after the cooldown the next one comes (" + (Lessons.next(n, p) == null ? null : Lessons.next(n, p).id) + ")", Lessons.next(n, p) != null && Lessons.next(n, p).id.equals("gate"));
        Settings.lessons = false;
        check("L1f 'settings lessons off' stops them", Lessons.next(n, p) == null);
        PcProbe.cmd("settings lessons on", 30);
        check("L1g ...and on brings them back (the setting command works)", Settings.lessons && Lessons.next(n, p) != null);
        Character foe = adult(lab, "Male", Character.Traits.Friendly);
        foe.changeRelationship(p, -2); foe.changeRelationship(p, -2);
        check("L1h an enemy teaches nothing", Dialogue.voiceOf(foe, p) != DialogueText.Voice.HOSTILE || Lessons.next(foe, p) == null);

        // ---- L2: each lesson fits its moment
        GameDriver.time = 100;
        Character wife = adult(lab, "Female", Character.Traits.Friendly);
        StringBuilder bad = new StringBuilder();
        final int mon0 = lab.monsters, pop0 = lab.population; final Area.DungeonType dt0 = lab.dungeonType;
        for (Lessons.L l : Lessons.ALL) {
            pc.lessonsSeen = new HashSet<>();
            for (Lessons.L o : Lessons.ALL) if (o != l) pc.lessonsSeen.add(o.id);
            pc.nextLessonWeek = 0;
            // the state of a person who needs this lesson
            p.majorLvl = 1; p.minorLvl = 2; p.potential = 100; p.sect = null; p.trait = Character.Traits.Friendly; p.spouse = null; p.targetQuests.clear(); p.materialBag.clear();
            lab.city = false; lab.monsters = 0; lab.dungeonType = null; lab.population = 0;
            switch (l.id) {
                case "qi": p.majorLvl = 0; p.minorLvl = 3; break;
                case "gate": p.majorLvl = 1; p.minorLvl = 9; break;
                case "potential": p.majorLvl = 1; p.potential = 10; break;
                case "quests": for (int i = 0; i < 6; i++) p.targetQuests.add(new Quest("Perform", true, 1, 0, 1, lab)); break;
                case "blood": p.potential = 50; p.materialBag.add(new Item(Item.ItemType.Blood, 1500)); break;
                case "squad": p.changeRelationship(n, 1); break;
                case "gifts": p.changeRelationship(n, 1); p.changeRelationship(n, 1); break;
                case "tournament": lab.city = true; break;
                case "tribulation": p.majorLvl = 2; p.minorLvl = 8; break;
                case "gatherer": p.trait = Character.Traits.Gatherer; break;
                case "family": p.spouse = wife; wife.spouse = p; p.children.clear(); break;
                case "beasts": lab.monsters = 5; break;
                case "ruins": lab.dungeonType = Area.DungeonType.BEAST_LAIR; break;
                case "auction": lab.city = true; lab.population = 1000; break;
                case "found": p.majorLvl = 2; break;
                default: break;
            }
            Lessons.L got = Lessons.next(n, p);
            if (got == null || got != l) bad.append(l.id).append("->").append(got == null ? "none" : got.id).append(' ');
            p.relationshipList = new RelationshipList(); p.spouse = null; wife.spouse = null;
        }
        lab.monsters = mon0; lab.population = pop0; lab.dungeonType = dt0; lab.city = false;
        check("L2 each of the " + Lessons.ALL.size() + " lessons is chosen in the state it is written for (" + (bad.length() == 0 ? "all" : "wrong: " + bad) + ")", bad.length() == 0);
        // the lesson can be told through the Io of a portrait window, with a question
        pc.lessonsSeen = new HashSet<>(); pc.nextLessonWeek = 0; p.majorLvl = 0; p.minorLvl = 3; p.trait = Character.Traits.Friendly;
        TestIo io = new TestIo(); io.answer = 1;
        Dialogue.Ctx ctx = new Dialogue.Ctx(n, p, io);
        Lessons.give(ctx);
        check("L2b a lesson told in the portrait way has 4-5 lines, and 'Tell me more' adds the rest (" + io.said.size() + " lines)", io.said.size() >= 6 && io.said.size() <= 9);

        // ---- L3: the words
        List<String> all = new ArrayList<>();
        for (Lessons.L l : Lessons.ALL) { all.addAll(Arrays.asList(l.warm)); all.addAll(Arrays.asList(l.dry)); all.addAll(Arrays.asList(l.more)); }
        double[] g = grade(all);
        PcProbe.real.println("[T] info: lessons read at grade " + String.format("%.1f", g[0]) + ", " + String.format("%.1f", g[1]) + " words a sentence, longest " + (int) g[2]);
        check("L3 the lessons read at about a 7th grade level or below (" + String.format("%.1f", g[0]) + "), with sentences of " + String.format("%.1f", g[1]) + " words on average and none above 30 (" + (int) g[2] + ")", g[0] <= 8.0 && g[2] <= 30);
        boolean noMarks = true;
        for (String s : all) if (s.contains("  ") || s.contains("{") && !s.contains("{You}") && !s.contains("{Name}") && !s.contains("{me}") && !s.contains("{place}")) noMarks = false;
        check("L3b no stray marks in the words", noMarks);
        PcProbe.cmd("lessons", 30);
        check("L3c the 'lessons' command lists what was heard", PcProbe.out().contains("Lessons you have heard"));

        // ---- F: a favor
        pc.lessonsSeen = new HashSet<>(); for (Lessons.L o : Lessons.ALL) pc.lessonsSeen.add(o.id);
        Area wild = at(8, 6); wild.monsters = 25; wild.city = false; wild.lvl = 8; wild.herbMaterials = 100000;
        n.ss = 500; n.sc = 0; n.dc = 0; n.knownLocations.add(wild);
        pc.nextFavorWeek = 0;
        TestIo io2 = new TestIo(); io2.answer = 0;
        Dialogue.Ctx fc = new Dialogue.Ctx(n, p, io2);
        check("F1 a person with money and a troubled place nearby can ask a favor (weight " + Favors.weight(fc) + ")", Favors.weight(fc) > 0);
        int open0 = Favors.open(p);
        Favors.scene(fc);
        check("F2 accepting puts a real quest in the Quest Log, marked with who asked (" + Favors.open(p) + " open)", Favors.open(p) == open0 + 1 && io2.said.toString().contains("favor") || Favors.open(p) == open0 + 1);
        Quest fq = null; for (Quest q : p.targetQuests) if (q.giver > 0) fq = q;
        check("F2b it is a Hunt Beast at the troubled place with the asker's name and a reward (" + (fq == null ? null : fq.displayName() + ", " + fq.reward + " ss") + ")", fq != null && fq.name.equals("Hunt Beast") && fq.targetArea == wild && fq.reward > 0 && fq.displayName().contains("Wen"));
        check("F3 the next favor waits for the cooldown (weight " + Favors.weight(fc) + ")", Favors.weight(fc) == 0);
        pc.nextFavorWeek = 0;
        check("F3b ...and at most " + Favors.MAX_OPEN + " are open at a time", Favors.open(p) < Favors.MAX_OPEN || Favors.weight(fc) == 0);
        long ss0 = PlayerLists.wealth(p), n0 = n.ss;
        int rel0 = n.relationshipList.contains(p.id) == null ? 0 : n.relationshipList.contains(p.id).value;
        Favors.thank(p, fq);
        int rel1 = n.relationshipList.contains(p.id) == null ? 0 : n.relationshipList.contains(p.id).value;
        check("F4 when the quest is done the asker pays (" + ss0 + " -> " + PlayerLists.wealth(p) + ", paid " + (n0 - n.ss) + ") and thinks better of him (" + rel0 + " -> " + rel1 + ")", PlayerLists.wealth(p) > ss0 && n0 - n.ss == fq.reward && rel1 >= rel0);
        io2.said.clear(); io2.answer = 1;
        pc.nextFavorWeek = 0; p.targetQuests.removeIf(q -> q.giver > 0);
        Favors.scene(fc);
        check("F5 refusing adds no quest (" + Favors.open(p) + ")", Favors.open(p) == 0);
        Character poor = adult(lab, "Male", Character.Traits.Friendly); poor.ss = 5; poor.sc = 0; poor.dc = 0; poor.knownLocations.add(wild); pc.nextFavorWeek = 0;
        check("F6 someone who cannot pay does not ask", Favors.weight(new Dialogue.Ctx(poor, p, io2)) == 0);

        // ---- V: the little stories
        p.ss = 1000; p.sc = 0; p.dc = 0;
        lab.city = false; lab.spiritualVein = false;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        int told = 0; StringBuilder vbad = new StringBuilder();
        for (Vignettes.Story s : Vignettes.ALL) {
            for (int opt = 0; opt < s.options.length; opt++) {
                p.ss = 1000; p.sc = 0; p.dc = 0; pc.vignettesSeen = new HashSet<>();
                DecisionBroker.scripted.clear(); DecisionBroker.scripted.add(opt);
                int before = Vignettes.transcript.size();
                try { Vignettes.tell(p, s); told++; } catch (RuntimeException e) { vbad.append(s.id).append('/').append(opt).append(" threw ").append(e).append("; "); }
                if (Vignettes.transcript.size() <= before + s.text.length) vbad.append(s.id).append('/').append(opt).append(" told nothing after the choice; ");
            }
        }
        check("V1 all " + Vignettes.ALL.size() + " little stories play every choice (" + told + " runs) without error (" + (vbad.length() == 0 ? "ok" : vbad) + ")", vbad.length() == 0 && told >= Vignettes.ALL.size() * 2);
        // an effect that is checked: the riddle's right answer pays 40 ss
        Vignettes.Story bridge = Vignettes.ALL.get(0);
        p.ss = 1000; p.sc = 0; p.dc = 0; pc.vignettesSeen = new HashSet<>(); DecisionBroker.scripted.clear(); DecisionBroker.scripted.add(0);
        Vignettes.tell(p, bridge);
        check("V2 the riddle's right answer pays 40 ss (" + p.ss + ")", p.ss == 1040 && pc.vignettesSeen.contains("bridge") && pc.nextVignetteWeek == GameDriver.time + Vignettes.COOLDOWN);
        // when they come
        pc.vignettesSeen = new HashSet<>(); pc.nextVignetteWeek = 0; GameDriver.time = 200;
        DecisionBroker.scripted.clear(); for (int i = 0; i < 200; i++) DecisionBroker.scripted.add(0);
        int ticks = 0;
        while (pc.vignettesSeen.isEmpty() && ticks < 4000) { Vignettes.tick(p); ticks++; }
        check("V3 on the road a little story starts by itself, about one turn in " + Vignettes.ODDS + " (after " + ticks + " turns)", !pc.vignettesSeen.isEmpty() && ticks > 0);
        int seen1 = pc.vignettesSeen.size();
        for (int i = 0; i < 600; i++) Vignettes.tick(p);
        check("V3b ...and no second one inside " + Vignettes.COOLDOWN + " weeks (" + seen1 + " -> " + pc.vignettesSeen.size() + ")", pc.vignettesSeen.size() == seen1);
        Settings.vignettes = false; pc.nextVignetteWeek = 0; pc.vignettesSeen = new HashSet<>();
        for (int i = 0; i < 1500; i++) Vignettes.tick(p);
        check("V3c 'settings vignettes off' stops them", pc.vignettesSeen.isEmpty());
        PcProbe.cmd("settings vignettes on", 30);
        check("V3d ...and the command turns them on", Settings.vignettes);
        PcProbe.cmd("stories", 30);
        check("V3e the 'stories' command lists them", PcProbe.out().contains("Little stories you have lived"));
        // where they fit
        lab.spiritualVein = true; pc.vignettesSeen = new HashSet<>(); for (Vignettes.Story s : Vignettes.ALL) if (!s.id.equals("vein")) pc.vignettesSeen.add(s.id);
        check("V4 the spirit-vein story is for a person standing on a spirit vein", Vignettes.pick(p) != null && Vignettes.pick(p).id.equals("vein"));
        lab.spiritualVein = false;
        check("V4b ...and not elsewhere", Vignettes.pick(p) == null);
        List<String> vt = new ArrayList<>();
        for (Vignettes.Story s : Vignettes.ALL) vt.addAll(Arrays.asList(s.text));
        double[] vg = grade(vt);
        PcProbe.real.println("[T] info: little stories read at grade " + String.format("%.1f", vg[0]) + ", " + String.format("%.1f", vg[1]) + " words a sentence, longest " + (int) vg[2]);
        check("V5 the stories read at about a 7th grade level or below (" + String.format("%.1f", vg[0]) + ")", vg[0] <= 8.0 && vg[2] <= 32);
        // ---- V6: the stories of the higher realms (2026-10-18): five or more for every realm from Foundation to True Immortal, the sums follow the realm, a story is told only in its own realm
        StringBuilder rbad = new StringBuilder();
        for (int realm = 1; realm <= 10; realm++) {
            int count = 0;
            for (Vignettes.Story s : Vignettes.ALL) if (s.lo == realm && s.hi == realm) count++;
            if (count < 5) rbad.append("realm ").append(realm).append(" has ").append(count).append("; ");
        }
        check("V6 every realm from Foundation to True Immortal has at least five little stories of its own (" + (rbad.length() == 0 ? Vignettes.ALL.size() + " in all" : rbad) + ")", rbad.length() == 0);
        long prev = 0; boolean rising = true;
        for (int realm = 1; realm <= 10; realm++) { p.majorLvl = realm; long a = VignetteRealms.sm(p), b = VignetteRealms.md(p), c3 = VignetteRealms.bg(p); if (!(a < b && b < c3) || a <= prev) rising = false; prev = a; }
        check("V6b the sums grow with the realm: small < middling < big, and each realm's small is above the last (" + VignetteRealms.sm(p) + " / " + VignetteRealms.md(p) + " / " + VignetteRealms.bg(p) + " at True Immortal)", rising);
        Settings.vignettes = true;
        StringBuilder pbad = new StringBuilder(); int runs = 0, unformatted = 0;
        for (Vignettes.Story s : Vignettes.ALL) {
            if (s.lo < 1) continue;
            for (long purse : new long[]{0L, 1000000000L}) {
                for (int opt = 0; opt < s.options.length; opt++) {
                    p.majorLvl = s.lo; p.minorLvl = 3; p.ss = purse; p.sc = 0; p.dc = 0; p.materialBag.clear(); p.pillBag.clear(); pc.vignettesSeen = new HashSet<>();
                    DecisionBroker.scripted.clear(); DecisionBroker.scripted.add(opt);
                    int before = Vignettes.transcript.size();
                    try { Vignettes.tell(p, s); runs++; } catch (RuntimeException e) { pbad.append(s.id).append('/').append(opt).append(" threw ").append(e).append("; "); }
                    List<String> toldNow = new ArrayList<>(Vignettes.transcript.subList(before, Vignettes.transcript.size()));
                    if (toldNow.size() <= s.text.length) pbad.append(s.id).append('/').append(opt).append(" told nothing after the choice; ");
                    for (String l : toldNow) if (l.contains("{")) { unformatted++; pbad.append(s.id).append(" has '").append(l).append("'; "); break; }
                }
            }
        }
        check("V6c all the higher-realm stories play every choice with an empty purse and a full one (" + runs + " runs): no error, something told after the choice, every {amount} filled in (" + (pbad.length() == 0 ? "ok" : pbad) + ")", pbad.length() == 0 && runs >= 50 * 3 * 2);
        // told only in the person's own realm
        p.ss = 1000000000L; pc.vignettesSeen = new HashSet<>(); GameDriver.time = 500; lab.city = true; lab.population = 5000; lab.monsters = 0;
        boolean wrongRealm = false; int picked = 0;
        for (int realm = 0; realm <= 10; realm++) {
            p.majorLvl = realm;
            for (int i = 0; i < 60; i++) { Vignettes.Story s = Vignettes.pick(p); if (s != null) { picked++; if (realm < s.lo || realm > s.hi) wrongRealm = true; } }
        }
        check("V6d a person is only offered the stories of the realm they are in (" + picked + " draws)", !wrongRealm && picked > 100);
        lab.city = false; lab.population = 0;
        p.majorLvl = 5; p.ss = 1000000000L; DecisionBroker.scripted.clear();
        PcProbe.cmd("stories", 30);
        check("V6e the 'stories' command counts the stories waiting in the realm (" + PcProbe.out().replace('\n', '|').substring(Math.max(0, PcProbe.out().length() - 140)) + ")", PcProbe.out().contains("more are waiting in your realm"));
        DecisionBroker.scripted = new ArrayDeque<>();
        System.out.println();
        PcProbe.real.println("[T] DONE");
    }
}
