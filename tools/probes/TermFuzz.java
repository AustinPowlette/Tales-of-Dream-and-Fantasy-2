package Game;

import java.io.*;
import java.util.*;

/** Fuzz probe for the command interpreter (the terminal and the GUI share it): sends several hundred random and malformed commands to a
 *  human character (unknown words, missing / negative / huge / non-numeric arguments, unknown ids, junk filters, odd characters, very long lines)
 *  and asserts that no exception reaches the output, the game thread survives and the game stays responsive (it keeps asking for the next
 *  command and a plain 'x' still advances time). Usage: java Game.TermFuzz [seed] [count]. Prints PASS/FAIL lines like the other probes. */
public class TermFuzz {
    static final Random R = new Random();
    static int checks = 0, failures = 0;

    static void check(String name, boolean ok) {
        checks++;
        if (!ok) failures++;
        PcProbe.passes += ok ? 1 : 0; PcProbe.fails += ok ? 0 : 1;
        PcProbe.real.println("[F] " + (ok ? "PASS " : "FAIL ") + name);
    }

    // commands that must not be fuzzed: they end the game, load another one, open interactive menus, flip the clock or swap the player body
    static final Set<String> EXCLUDE = new HashSet<>(Arrays.asList("quit", "exit", "bye", "load", "delsave", "control", "e", "z", "cheat", "o", "music", "p", "pause", "auto",
            "reincarnate", "script", "tutorial"));
    static final String[] BIGS = {"0", "-1", "-5", "99999999999", "2147483647", "2147483648", "-2147483649", "1e9", "1.5", "0x10", "٣", "+7", "00012", "99999999999999999999999"};
    static final String[] WORDS = {"abc", "Wei", "friend", "fight", "spar", "on", "off", "all", "weapon", "armor", "food", "wine", "poison", "city", "sect", "x", "-", "--", "?", "*", "%s", "%d",
            "'", "\"", "\\", "null", "NaN", "lvl>=5", "lvl>=", ">=5", "sort=lvl", "sort=", "sort=bogus", "bogus=1", "desc", "asc", "type=weapon", "price<=5k", "price<=99999999999999999999", "afford=1", "name=",
            "limit=abc", "#", "#abc", "#-1", "#99999999999", "#0", "#999999", "1.2.3", "a.b", "1.", ".5", "9.9", "0.", "..", "Hunt Beast", "Go: home", "雲", "\t", "&&", ";", "|", "<script>", "[1500,", "1500,1500"};

    static String num() { return R.nextInt(3) == 0 ? BIGS[R.nextInt(BIGS.length)] : String.valueOf(R.nextInt(12)); }
    static String id() { int k = R.nextInt(6); return k == 0 ? "#" + R.nextInt(3200) : k == 1 ? String.valueOf(R.nextInt(3200)) : k == 2 ? "#" + BIGS[R.nextInt(BIGS.length)] : k == 3 ? "#" + R.nextInt(5) : WORDS[R.nextInt(WORDS.length)]; }
    static String arg(boolean timed) {
        int k = R.nextInt(10);
        if (k < 3) return timed ? String.valueOf(R.nextInt(6)) : num();
        if (k < 5) return id();
        if (k < 6) return String.valueOf(R.nextInt(40) * 100) ;
        if (k < 7) { StringBuilder sb = new StringBuilder(); int n = 200 + R.nextInt(300); for (int i = 0; i < n; i++) sb.append((char) ('a' + R.nextInt(26))); return sb.toString(); }
        return WORDS[R.nextInt(WORDS.length)];
    }

    static final String[] TIMED = {"wait", "walk", "watch", "goto", "gq", "goquest", "chase", "route", "go", "travel", "x", "stay", "digest", "w", "a", "s", "d", "nw", "ne", "sw", "se", "north", "south", "east", "west"};

    // every word the interpreter's switch knows (plus aliases and typos); kept literal so this probe compiles against any copy of the sources
    static final String VOCAB = "alerts alliance attack auction beasts brew buy buygear bag chase chronicle city coat companions compare consume cook craft dash dashboard date digest disband do drink duties eat equip export family fame feed find follow forage found gift goods goquest goto gq halloffame help here history hunt i inbox install invite join journal l learn leave library limit lineage list ls make manual map market meditate mine monsters mute note notes notices party peace people places play players poison policy preview price promote q qd qdo qnew qp qu quests r recap recruit relations relationships release resign retire route run runstats s sect seek sell set settings shops spar squad stats stay stop story talk tame tech techniques time track unequip unfollow unmute unnote untrack w a d x nw ne sw se wait walk war watch "
            + "north south east west up down left right inv inventory backpack status me who todo quest quests tasks go travel again last cmds hud commands bogus qeust sh tal ? ! !! f";

    static List<String> vocabulary() {
        List<String> v = new ArrayList<>(new TreeSet<>(Arrays.asList(VOCAB.trim().split(" +"))));
        v.removeAll(EXCLUDE);
        return v;
    }

    static String gen(List<String> vocab) {
        String cmd = vocab.get(R.nextInt(vocab.size()));
        if (cmd.equals("save") || cmd.equals("f")) cmd = "export"; // saving a 3,000-person world 40 times only wastes time
        boolean timed = Arrays.asList(TIMED).contains(cmd);
        StringBuilder sb = new StringBuilder();
        if (R.nextInt(8) == 0 && !cmd.equals("!!")) sb.append('!');
        if (R.nextInt(15) == 0) cmd = cmd.toUpperCase();
        sb.append(cmd);
        int n = R.nextInt(5);
        if (cmd.equals("settings")) { // keys that are safe in a headless run (no sound, pictures or confirm flips)
            String[] keys = {"hud", "hints", "autoequip", "tooltips", "toasts", "challenges", "autosave", "autosavekeep", "recap", "font", "bogus", "notify", "maturestart", "colorblind"};
            sb.append(' ').append(keys[R.nextInt(keys.length)]);
            for (int i = 0; i < R.nextInt(3); i++) sb.append(' ').append(R.nextInt(2) == 0 ? "on" : R.nextInt(2) == 0 ? "off" : arg(false));
            return sb.toString();
        }
        for (int i = 0; i < n; i++) sb.append(R.nextInt(25) == 0 ? "   " : " ").append(arg(timed));
        return sb.toString();
    }

    static void send(String line, List<String> bad) throws Exception {
        int m = PcProbe.mark();
        PcProbe.cmd(line, 90);
        String out = PcProbe.since(m);
        boolean blocked = PcProbe.in.blocked;
        boolean err = out.contains("Exception") || out.contains("\tat Game.") || out.contains("[command error]") || out.contains("crash.log");
        if (PcProbe.gameError != null) { bad.add("GAME THREAD DIED after: " + line + "  -> " + PcProbe.gameError); return; }
        if (err) bad.add("ERROR OUTPUT after: " + shorten(line) + "\n      " + firstErr(out));
        else if (!blocked) bad.add("NOT RESPONSIVE after: " + shorten(line));
    }

    static String shorten(String s) { return s.length() > 120 ? s.substring(0, 120) + "..(" + s.length() + " chars)" : s; }
    static String firstErr(String out) { for (String l : out.split("\n")) if (l.contains("Exception") || l.contains("[command error]") || l.contains("\tat Game.")) return l.trim().substring(0, Math.min(260, l.trim().length())); return "?"; }

    public static void main(String[] a) throws Exception {
        long seed = a.length > 0 ? Long.parseLong(a[0]) : 20261002L;
        int count = a.length > 1 ? Integer.parseInt(a[1]) : 600;
        R.setSeed(seed);
        PcProbe.real = System.out;
        System.setIn(PcProbe.in);
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(PcProbe.real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        try { run(count); } catch (Throwable e) { PcProbe.real.println("[F] PROBE EXCEPTION " + e); e.printStackTrace(PcProbe.real); failures++; PcProbe.fails++; }
        PcProbe.real.println("[F] ===== " + (checks - failures) + " passed, " + failures + " failed (seed " + seed + ", gameError=" + PcProbe.gameError + ") =====");
        PcProbe.real.println("[T] ===== " + PcProbe.passes + " passed, " + PcProbe.fails + " failed; gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }

    static void run(int count) throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        check("player created and prompting", p != null && PcProbe.in.blocked);
        if (p == null) return;
        p.lvl = 100; p.majorLvl = 10; p.minorLvl = 0; p.maxAge = 100000; p.caution = 0;
        DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        DecisionBroker.scripted = new ArrayDeque<>(); // confirmations and decisions answer with their defaults and never block the fuzz
        GameDriver.frozenNpcs = true;
        int t0 = GameDriver.time;
        List<String> bad = new ArrayList<>();

        // 1. fixed malformed commands that used to throw or to do nothing silently
        String[] fixed = {"list people 99999999999", "map 99999999999", "history 99999999999", "history foo 99999999999", "find #99999999999", "note #99999999999 hi", "wait 99999999999", "wait -3", "wait abc",
                "walk d 99999999999", "walk q 3", "walk d", "goto 99999999999 5", "goto -5 5", "goto 5", "goto a b", "qdo", "qu", "qd", "qp", "qp 1", "qp x y", "qp 1 99999999999", "qdo 99999999999", "qdo #99999999999",
                "qdo -1", "release", "release 0", "release -1", "feed -1", "eat 99", "equip 99999999999", "equip -1", "compare", "compare 0", "learn 99999999999", "buy 99999999999", "buy 1.99999", "buy .",
                "buy market 99999999999 99999999999", "sell 99999999999", "sell -1", "gift", "gift 1", "gift 99999999999 1", "talk #99999999999", "talk -1", "spar 99999999999", "invite #abc", "preview #0", "seek",
                "seek 99999999999 friend", "seek 5", "seek 5 bogus", "seek -1 fight", "chase", "chase #99999999999", "chase -4", "goquest 99999999999", "goquest -1", "limit -5", "limit 0", "limit abc", "set limit",
                "set limit 99999999999", "settings", "settings autosave", "settings autosave 99999999999", "settings font abc", "settings font 99999999999", "settings recap 99999999999", "settings notify", "settings notify x on",
                "route 99999999999 99999999999", "route a b", "route", "watch 99999999999", "watch abc", "watch for", "watch until year", "watch until year abc", "digest 99999999999", "digest -4", "journal 99999999999",
                "notices 99999999999", "relations 99999999999", "unnote 99999999999", "unnote", "price", "price 99999999999 buy 1", "price 1 buy abc", "qnew", "qnew a", "qnew a b c d", "qnew x 99999999999 99999999999 1",
                "qnew x 5 5 99999999999", "mute", "unmute", "do", "do bogus", "do Hold", "do Hold Tournament 99999999999", "found", "found bogus", "join", "recruit 99999999999", "promote", "promote 99999999999", "war", "peace",
                "alliance", "make", "make bogus", "export", "export bogus", "export people 99999999999", "list", "list bogus", "list people lvl>=", "list people sort=", "list people bogus=1", "list shop 99999999999",
                "list places 0", "people 0", "people -1", "monsters 99999999999", "find", "find ", "unequip", "unequip bogus", "install", "tame", "feed", "poison", "poison 99999999999", "coat", "play", "hunt", "forage",
                "mine", "craft", "consume", "buygear", "library", "market", "auction", "shops", "city bogus", "sect bogus", "sect rules", "sect rules potential abc", "help bogus", "help", "help all", "help 99999999999",
                "!!", "last", "last 99999999999", "last abc", "cmds", "cmds -1", "cmds 99999999999", "hud", "hud maybe", "hud on", "!", "!!!", "?", "  ", "\t", "x", "bogus", "qeust", "inv", "sh", "xx", "w w w", "north 5"};
        for (String line : fixed) send(line, bad);
        check("fixed malformed commands: no exceptions, always responsive (" + fixed.length + " commands)", bad.isEmpty());
        for (String b : bad) PcProbe.real.println("[F]   " + b);
        bad.clear();

        // 2. random commands
        List<String> vocab = vocabulary();
        PcProbe.real.println("[F] vocabulary: " + vocab.size() + " words; random commands: " + count);
        List<String> sent = new ArrayList<>();
        for (int i = 0; i < count && PcProbe.gameError == null && !p.dead; i++) {
            String line = gen(vocab);
            sent.add(line);
            send(line, bad);
            if (!bad.isEmpty() && bad.size() >= 12) break;
        }
        check("random commands: no exceptions, always responsive (" + sent.size() + " commands)", bad.isEmpty());
        for (String b : bad) PcProbe.real.println("[F]   " + b);
        check("game thread alive", PcProbe.gameError == null);
        check("character ids intact", PcProbe.idMismatch() == 0);

        // 3. still playable: a plain 'x' advances time, help prints the overview, a quest list prints
        if (!p.dead && PcProbe.gameError == null) {
            PcProbe.cmd("hud on", 30);
            int tt = GameDriver.time;
            int m = PcProbe.mark();
            PcProbe.cmd("x", 60);
            check("a plain 'x' still advances time after the fuzz", GameDriver.time == tt + 1 || GameDriver.time > tt);
            m = PcProbe.mark();
            PcProbe.cmd("help", 30);
            check("help still prints the overview", PcProbe.since(m).contains("=== Commands ==="));
            m = PcProbe.mark();
            PcProbe.cmd("q", 30);
            check("quest list still prints", PcProbe.since(m).contains("=== Quests (mode: "));
        }
        PcProbe.real.println("[F] sample of the random commands: " + sent.subList(0, Math.min(12, sent.size())));
    }
}
