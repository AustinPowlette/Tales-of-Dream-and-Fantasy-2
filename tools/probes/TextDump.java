package Game;

import java.io.*;
import java.util.*;

/** Writes every word of the lessons, the little stories and the favors to a markdown file (`java -cp <out> Game.TextDump <file>`), so the author can read them in one piece. No world is needed. */
public class TextDump {
    static String file = "Lesson and Story Text.md";

    public static void main(String[] a) throws Exception {
        if (a.length > 0) file = a[0];
        PcProbe.real = System.out;
        System.setIn(PcProbe.in);
        GameDriver.frozenNpcs = true;
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(PcProbe.real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        try { run(); } catch (Throwable e) { PcProbe.real.println("DUMP FAILED " + e); e.printStackTrace(PcProbe.real); }
        System.exit(0);
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        Settings.lore = false;
        StringBuilder sb = new StringBuilder();
        sb.append("# Lessons, favors and little stories: all the text\n\n");
        sb.append("Placeholders: `{You}` is how the speaker addresses you (your name, \"friend\", \"young one\"...), `{Name}` your name, `{me}` the speaker's name. A lesson is told in the **warm** voice by cheerful, warm and scholarly speakers and in the **dry** voice by the others; after it you may ask \"Tell me more\".\n\n");
        sb.append("## Lessons (").append(Lessons.ALL.size()).append(")\n\n");
        sb.append("Each is told once per person, when someone stops to talk to you (or you talk first) and the situation is true; at least ").append(Lessons.COOLDOWN).append(" weeks apart.\n\n");
        int n = 1;
        for (Lessons.L l : Lessons.ALL) {
            sb.append("### ").append(n++).append(". ").append(l.title).append("\n\n*Teaches:* ").append(l.gist).append("\n\n");
            sb.append("**Warm voice**\n\n"); for (String s : l.warm) sb.append("> ").append(s).append("\n>\n"); sb.append("\n");
            sb.append("**Dry voice**\n\n"); for (String s : l.dry) sb.append("> ").append(s).append("\n>\n"); sb.append("\n");
            sb.append("**\"Tell me more\"**\n\n"); for (String s : l.more) sb.append("> ").append(s).append("\n>\n"); sb.append("\n");
        }
        sb.append("## Favors\n\nA conversation can end with a request (a place within 14 tiles with wild beasts, not too far above your strength). The speaker opens with one of three lines, then names the pay; you answer.\n\n");
        sb.append("**Openings** (one of three, `where` is \"here\" or \"at <place> (<coordinates>)\"; sometimes a short greeting from the speaker's voice comes first)\n\n");
        sb.append("> Could I ask a favor? Beasts have been getting bold <where>. They've scared off every honest worker in the area.\n>\n");
        sb.append("> You look like someone who can handle a fight. There are beasts <where> that need thinning out. Could you do it?\n>\n");
        sb.append("> Forgive me for asking a stranger's help. Beasts are ruining the work <where>. I'd feel safer if someone strong cleared them.\n>\n");
        sb.append("> I can pay <N> ss when it's done.\n\n");
        sb.append("**Your answers:** *I'll do it.* / *I can't, sorry.* / *Not now.*\n\n");
        sb.append("**If you accept** (one of): \"Thank you. Truly. Come find me when it's done.\" / \"You have my thanks. Be careful out there.\" / \"Heaven watch over you. And thank you.\" Then: *A new quest is in your Quest Log: Thin out the beasts for <name>.* When it is done: *<name> thanks you for the favor and pays <N> ss.*\n\n");
        sb.append("**If you refuse** (one of): \"I understand. It was a lot to ask.\" / \"No harm in asking. Another time, perhaps.\"  **If you say not now:** \"Of course. If you change your mind, I'll still be here.\"\n\n");
        sb.append("## Little stories (").append(Vignettes.ALL.size()).append(")\n\n");
        sb.append("Lines starting with \"*\" are narration; `Name:` is somebody speaking. The text after each choice is what you read once you have chosen. Each story is told once per person, about one turn in ").append(Vignettes.ODDS).append(" when one fits, ").append(Vignettes.COOLDOWN).append(" weeks apart.\n\n");
        n = 1;
        for (Vignettes.Story s : Vignettes.ALL) {
            p.majorLvl = s.lo; p.minorLvl = 3; // (the sums of a story follow the realm it is told in)
            sb.append("### ").append(n++).append(". ").append(s.title).append("\n\n");
            sb.append("*Told in: ").append(s.lo == s.hi ? TermFormat.realm(s.lo) : TermFormat.realm(s.lo) + " to " + TermFormat.realm(s.hi)).append(s.lo == s.hi && s.lo > 0 ? " (small sum " + String.format("%,d", VignetteRealms.sm(p)) + ", middling " + String.format("%,d", VignetteRealms.md(p)) + ", big " + String.format("%,d", VignetteRealms.bg(p)) + " spirit stones)" : "").append("*\n\n");
            for (String l : s.text) sb.append(format(Vignettes.fmt(p, l))).append("\n\n");
            sb.append("**").append(Vignettes.fmt(p, s.prompt)).append("**\n\n");
            for (int i = 0; i < s.options.length; i++) sb.append(i + 1).append(". ").append(Vignettes.fmt(p, s.options[i])).append(i == s.dflt ? "  *(the default if you close the window)*" : "").append("\n");
            sb.append("\n");
            sb.append("*What follows each choice* (text in brackets is what the game changes):\n\n");
            for (int i = 0; i < s.options.length; i++) {
                sb.append("- **").append(Vignettes.fmt(p, s.options[i])).append("**\n\n");
                // a choice can end in more than one way (dice, a purse too thin): every ending that turns up in forty tries is shown, the rich purse first
                LinkedHashMap<String, List<String>> variants = new LinkedHashMap<>();
                for (long purse : new long[]{100000000000L, 0L}) {
                    for (int tries = 0; tries < 40; tries++) {
                        p.ss = purse; p.sc = 0; p.dc = 0; p.materialBag.clear(); p.pillBag.clear(); p.backpack.clear(); p.majorLvl = s.lo; p.minorLvl = 3;
                        List<String> after;
                        try { after = s.resolve.go(p, i); } catch (RuntimeException e) { after = Arrays.asList("(needs a place that is not at hand in this dump)"); }
                        List<String> shown = new ArrayList<>();
                        for (String l : after) shown.add(format(Vignettes.fmt(p, l)));
                        String key = String.join("\n", shown).replaceAll("\\([^)]*is in your[^)]*\\)", "(item)").replaceAll("[0-9,]+", "#").replaceAll("it at [A-Z][^.]*\\.", "it at a place.").replaceAll("a place on your map[^.]*\\.", "a place.");
                        if (!variants.containsKey(key) && variants.size() < 6) variants.put(key, shown);
                    }
                }
                int v = 0;
                for (List<String> shown : variants.values()) {
                    if (variants.size() > 1) sb.append("  *Ending ").append((char) ('a' + v++)).append(":*\n\n");
                    for (String l : shown) sb.append("  ").append(l).append("\n\n");
                }
            }
        }
        PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        out.print(sb);
        out.close();
        PcProbe.real.println("wrote " + file);
    }

    static String format(String l) {
        if (l.startsWith("* ")) return "*" + l.substring(2) + "*";
        if (l.startsWith("@") && l.contains("| ")) return "**" + l.substring(1, l.indexOf("| ")) + ":** " + l.substring(l.indexOf("| ") + 2);
        return l;
    }
}
