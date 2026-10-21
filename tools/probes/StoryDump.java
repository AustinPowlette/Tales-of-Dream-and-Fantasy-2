package Game;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Writes "Start Stories Script.md": every scene of the ten Story starts in one file to read (opening, task, closing scene, choices and their answers, epilogue, codas), with what starts each chapter, what it asks of the
 *  player, what it pays, and how long each story is. Run from the repo root: `java -cp ... Game.StoryDump <repo>/src/Game <out.md> [<repo>/GameInfo/Story Plan.md]`. The conditions that are code (`when`, `skipIf`, `setup`, `done`)
 *  are copied from the source of the Tale* file, because they cannot be printed from the running data. */
public class StoryDump {
    static final StringBuilder out = new StringBuilder();
    static void p(String s) { out.append(s).append('\n'); }

    /** One top-level call of a builder chain: name and the text between its parentheses. */
    static final class Call { String name, args; }

    /** The chain that starts at index `from` (the "(" of chapter(...) is the first one) cut into its top-level calls. */
    static List<Call> chain(String src, int from) {
        List<Call> calls = new ArrayList<>();
        int depth = 0, i = from;
        boolean inStr = false, inChr = false;
        int callStart = -1; String name = null;
        for (; i < src.length(); i++) {
            char ch = src.charAt(i);
            if (inStr) { if (ch == '\\') i++; else if (ch == '"') inStr = false; continue; }
            if (inChr) { if (ch == '\\') i++; else if (ch == '\'') inChr = false; continue; }
            if (ch == '"') { inStr = true; continue; }
            if (ch == '\'') { inChr = true; continue; }
            if (ch == '(') {
                if (depth == 0) {
                    int j = i - 1; while (j >= 0 && (java.lang.Character.isJavaIdentifierPart(src.charAt(j)))) j--;
                    name = src.substring(j + 1, i); callStart = i + 1;
                }
                depth++;
            } else if (ch == ')') {
                depth--;
                if (depth == 0 && name != null) { Call c = new Call(); c.name = name; c.args = src.substring(callStart, i); calls.add(c); name = null; }
            } else if (ch == ';' && depth == 0) break;
        }
        return calls;
    }

    static String squash(String s, int max) {
        String t = s.replaceAll("\\s+", " ").trim();
        return t.length() > max ? t.substring(0, max - 3) + "..." : t;
    }

    /** All the source of the Tale* files, by story id. */
    static java.util.Map<String, String> sources(File dir) throws IOException {
        java.util.Map<String, String> m = new HashMap<>(), byBase = new HashMap<>(), idBase = new HashMap<>();
        File[] fs = dir.listFiles((d, n) -> n.startsWith("Tale") && n.endsWith(".java"));
        if (fs == null) return m;
        for (File f : fs) {
            String s = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            String base = f.getName().substring(0, f.getName().length() - 5);
            byBase.put(base, s);
            int i = s.indexOf("Chapters.def(\"");
            if (i < 0) continue;
            int a = i + "Chapters.def(\"".length(), b = s.indexOf('"', a);
            idBase.put(s.substring(a, b), base);
        }
        for (java.util.Map.Entry<String, String> e : idBase.entrySet()) { String more = byBase.get(e.getValue() + "Seasons"), extra = byBase.get(e.getValue() + "More"); m.put(e.getKey(), byBase.get(e.getValue()) + (more == null ? "" : "\n" + more) + (extra == null ? "" : "\n" + extra)); }   // (season 1 and the later seasons of one story)
        return m;
    }

    static int words(String[] lines) {
        int n = 0;
        if (lines != null) for (String l : lines) { String t = l.replaceAll("^(\\?!?[A-Za-z0-9_.=]+\\|)+", ""); if (t.startsWith("@")) t = t.substring(t.indexOf('|') + 1); n += t.trim().isEmpty() ? 0 : t.trim().split("\\s+").length; }
        return n;
    }

    static String line(String raw) {
        String l = raw, cond = "";
        while (l.startsWith("?") && l.indexOf('|') > 0) {
            int bar = l.indexOf('|'); String c = l.substring(1, bar); l = l.substring(bar + 1);
            cond += (cond.isEmpty() ? "" : " and ") + (c.startsWith("!") ? "not " + c.substring(1) : c.replace("=", " is "));
        }
        String body;
        if (l.startsWith("@") && l.indexOf('|') > 1) body = "**" + l.substring(1, l.indexOf('|')) + ":** " + l.substring(l.indexOf('|') + 1).trim();
        else if (l.startsWith("* ")) body = l.substring(2);
        else body = l;
        return (cond.isEmpty() ? "" : "*[only if " + cond + "]* ") + body;
    }

    static void scene(String heading, String[] lines) {
        if (lines == null || lines.length == 0) return;
        p("**" + heading + "**");
        p("");
        for (String l : lines) { p(line(l)); p(""); }
    }

    static void choice(String heading, Chapters.Choice c) {
        if (c == null) return;
        p("**" + heading + "** (kept as the flag `" + c.flag + "`; default: option " + (c.dflt + 1) + ")");
        p("");
        p("> " + c.prompt);
        p("");
        for (int i = 0; i < c.options.length; i++) {
            p((i + 1) + ". " + c.options[i]);
            if (c.after != null && i < c.after.length && c.after[i] != null && c.after[i].length > 0) {
                for (String l : c.after[i]) p("   - " + line(l));
            }
        }
        p("");
    }

    static String task(Chapters.Chapter ch) {
        String k = ch.kind;
        String txt = ch.text.isEmpty() ? "" : "  \"" + ch.text + "\"";
        switch (k) {
            case "NONE": return "No task: the closing scene follows at once." ;
            case "WAIT": return "Wait: nothing to do, the closing scene comes " + ch.n + " weeks (" + (ch.n / 52) + " years" + (ch.n % 52 != 0 ? " and " + (ch.n % 52) + " weeks" : "") + ") after the opening scene.";
            case "REALM": return "Reach Major Lvl " + ch.n + "." + txt;
            case "CRAFT": return "Make " + ch.n + " items in total (everything you have ever made counts)." + txt;
            case "CHECK": return ch.counter != null ? "Make " + ch.counterN + " items **since this chapter began**." + txt : "A special condition (see the code below)." + txt;
            case "MEET": return "Meet " + ch.who + " (walk to their tile)." + txt;
            case "DEFEAT": return "Defeat " + ch.who + " (only you can kill them)." + txt;
            case "PLACE": return "Go to the place `" + ch.where + "`." + txt;
            case "WEALTH": return "Hold " + String.format("%,d", ch.n) + " spirit stones." + txt;
            case "FRIENDS": return "Have " + ch.n + " friends." + txt;
            case "SHOPS": return "Own " + ch.n + " shops." + txt;
            case "FLAG": return "Wait for the story flag `" + ch.who + "` (set by something you do in the story)." + txt;
            default: return k + " " + ch.n + (ch.who.isEmpty() ? "" : " " + ch.who) + "." + txt;
        }
    }

    static String trigger(Chapters.Chapter ch, List<Call> calls, boolean first) {
        List<String> t = new ArrayList<>();
        if (ch.episode) t.add("a road tale: drawn at random (about 1 in " + ch.odds + " turns once it may come), only one in " + "every few years");
        if (ch.gap > 0) t.add(ch.gap % 52 == 0 ? (ch.gap / 52) + " year(s) after the previous chapter ended" : ch.gap + " weeks after the previous chapter ended");
        else if (!ch.episode) t.add(first ? "the first week of the story" : "at once when the previous chapter ends");
        if (ch.minAge > 0) t.add("age " + ch.minAge + " or more");
        if (ch.minMajor > 0) t.add("Major Lvl " + ch.minMajor + " or more (given up after " + (Chapters.PATIENCE / 52) + " more years of waiting)");
        if (ch.soft != null) t.add("a wish: " + ch.softNeeds + " (given up after " + (Chapters.PATIENCE / 52) + " more years of waiting)");
        for (Call c : calls) {
            if (c.name.equals("when")) t.add("and this condition holds: `" + squash(c.args, 260) + "`");
            if (c.name.equals("skipIf")) t.add("(left out altogether if `" + squash(c.args, 200) + "`)");
        }
        return String.join("; ", t);
    }

    static String pays(Chapters.Chapter ch) {
        String s = Chapters.payText(ch);
        return s.isEmpty() ? "nothing" : s.replace("Reward: ", "").replaceAll("\\.$", "");
    }

    public static void main(String[] a) throws Exception {
        File src = new File(a.length > 0 ? a[0] : "src/Game");
        File dest = new File(a.length > 1 ? a[1] : "Start Stories Script.md");
        File plan = new File(a.length > 2 ? a[2] : "GameInfo/Story Plan.md");
        java.util.Map<String, String> srcs = sources(src);
        java.util.Map<String, String> planNotes = new HashMap<>();
        if (plan.isFile()) {
            List<String> pl = Files.readAllLines(plan.toPath(), StandardCharsets.UTF_8);
            for (String l : pl) { java.util.regex.Matcher m = java.util.regex.Pattern.compile("^(\\d+)\\. \\*\\*(.+?)\\*\\*(.*)$").matcher(l); if (m.find()) planNotes.put(m.group(2).trim(), (m.group(2) + m.group(3)).trim()); }
        }
        p("# Start Stories Script: every scene of the ten character starts");
        p("");
        p("Generated by `tools/probes/StoryDump.java` from the source. Read it from top to bottom; mark what to change. The words are exactly the game's, with the placeholders left in: `{name}` / `{first}` / `{last}` the player, `{key}` / `{key.first}` a person of the story, `{@place}` a place of the story, `{#flag}` a number. A line marked *[only if ...]* shows only when you made that choice (or that person is dead).");
        p("");
        p("How a chapter runs: a window with the **opening scene** (and sometimes a question), then a **task** (it sits in the Quest Log as `Story: ...`), then the **closing scene** (sometimes another question), then the reward. The next chapter starts once its *Starts when* is true. **\"Make N items\" is a real task** (a count of what you craft); **\"Wait\"** and **\"No task\"** chapters ask nothing of you but time.");
        p("");

        // overview table
        p("## The ten starts at a glance");
        p("");
        p("| # | Story | Chapters | Road tales | Words (scenes) | Reading time | Years of waiting built in |");
        p("|---|---|---|---|---|---|---|");
        int n = 0;
        for (Chapters.Def d : Chapters.DEFS.values()) {
            n++;
            int w = 0, gapSum = 0, waitSum = 0;
            for (Chapters.Chapter ch : d.chapters) { w += words(ch.intro) + words(ch.outro); if (ch.choice != null && ch.choice.after != null) for (String[] x : ch.choice.after) w += words(x); if (ch.outChoice != null && ch.outChoice.after != null) for (String[] x : ch.outChoice.after) w += words(x); gapSum += ch.gap; if (ch.kind.equals("WAIT")) waitSum += ch.n; }
            for (Chapters.Chapter ch : d.episodes) w += words(ch.intro) + words(ch.outro);
            w += words(d.epilogue); for (Chapters.Season sn : d.seasons) w += words(sn.interlude);
            Starts.Story s = Starts.STORIES.get(d.id);
            p("| " + n + " | " + (s == null ? d.id : s.title) + " | " + d.chapters.size() + " | " + d.episodes.size() + " | " + String.format("%,d", w) + " | about " + Math.max(1, w / 200) + " min | " + String.format("%.1f", (gapSum + waitSum) / 52.0) + " |");
        }
        p("");
        p("(Reading time at 200 words a minute, for one path through the choices only roughly: the choice answers are counted all together. \"Years of waiting built in\" counts the gaps between chapters and the Wait tasks; the real length also depends on how fast you grow, since many chapters wait for a realm or a count of items.)");
        p("");

        n = 0;
        for (Chapters.Def d : Chapters.DEFS.values()) {
            n++;
            Starts.Story s = Starts.STORIES.get(d.id);
            String title = s == null ? d.id : s.title;
            String code = srcs.get(d.id);
            p("---");
            p("");
            p("# " + n + ". " + title + "  (`" + d.id + "`)");
            p("");
            if (s != null) {
                p("*" + s.blurb + "*");
                p("");
                p("Starts as: " + (s.gender == null ? "any gender" : s.gender) + ", trait " + s.trait + ", " + s.affiliation + ", potential " + s.potential + ", " + String.format("%,d", s.money) + " spirit stones" + (s.hook != null && !s.hook.isEmpty() ? ". Hook: " + s.hook : "") + ".");
                p("");
            }
            String pn = planNotes.get(title);
            if (pn != null) { p("**What the plan says is the truth of this story:** " + pn); p(""); }
            if (!d.cast.isEmpty()) {
                p("**People of the story** (real characters in the world; \"fated\" means only you can kill them):");
                p("");
                p("| Key | Name | Trait | Side | Level vs yours | Lives | Appears |");
                p("|---|---|---|---|---|---|---|");
                for (Chapters.Cast c : d.cast.values()) p("| " + c.key + " | " + c.first + " " + c.last + " | " + c.trait + " | " + c.affil + " | " + (c.offset >= 0 ? "+" : "") + c.offset + " | " + c.home + (c.fated ? "" : " (not fated)") + " | " + (c.atStart ? "from the first day" : "when a chapter needs them") + " |");
                p("");
            }
            if (!d.places.isEmpty()) {
                StringBuilder sb = new StringBuilder("**Places:** ");
                for (java.util.Map.Entry<String, String> e : d.places.entrySet()) sb.append("`").append(e.getKey()).append("` = ").append(e.getValue()).append("; ");
                p(sb.toString()); p("");
            }
            int ci = 0;
            for (Chapters.Chapter ch : d.chapters) {
                ci++;
                if (d.seasons.size() > 1 && (ci == 1 || d.chapters.get(ci - 2).season != ch.season)) {
                    Chapters.Season sn = d.seasonOf(ch.season);
                    if (ci > 1) { Chapters.Season prev = d.seasonOf(d.chapters.get(ci - 2).season); if (prev != null && prev.interlude.length > 0) { p("## " + n + ".I" + prev.n + "  Interlude: " + prev.interludeTitle + "  (between season " + prev.n + " and season " + ch.season + ")"); p(""); scene("Interlude", prev.interlude); } }
                    p("# " + n + ".S" + ch.season + "  Season " + ch.season + (sn == null ? "" : ": " + sn.title)); p("");
                }
                List<Call> calls = new ArrayList<>();
                if (code != null) { int i = code.indexOf("chapter(\"" + ch.id + "\""); if (i >= 0) calls = chain(code, i + "chapter".length()); }
                p("## " + n + "." + ci + "  \"" + ch.title + "\"  (`" + ch.id + "`)");
                p("");
                p("- **Starts when:** " + trigger(ch, calls, ci == 1));
                p("- **Task:** " + task(ch));
                p("- **Pays:** " + pays(ch));
                for (Call c : calls) if (c.name.equals("setup") || c.name.equals("done")) p("- **" + (c.name.equals("setup") ? "When it opens, the code" : "When it closes, the code") + ":** `" + squash(c.args, 320) + "`");
                p("- **Words:** " + (words(ch.intro) + words(ch.outro)) + " in the two scenes");
                p("");
                scene("Opening scene", ch.intro);
                choice("Question after the opening scene", ch.choice);
                scene("Closing scene", ch.outro);
                choice("Question after the closing scene", ch.outChoice);
            }
            if (!d.episodes.isEmpty()) {
                p("## " + n + ".R  Road tales of " + title + "  (at most " + d.episodeMax + ", at least " + (d.episodeGap / 52) + " years apart; each comes up once)");
                p("");
                for (Chapters.Chapter ch : d.episodes) {
                    List<Call> calls = new ArrayList<>();
                    if (code != null) { int i = code.indexOf("episode(\"" + ch.id + "\""); if (i >= 0) calls = chain(code, i + "episode".length()); }
                    p("### \"" + ch.title + "\"  (`" + ch.id + "`)");
                    p("");
                    p("- **Comes up when:** " + trigger(ch, calls, false));
                    p("- **Task:** " + task(ch));
                    p("- **Pays:** " + pays(ch));
                    for (Call c : calls) if (c.name.equals("setup") || c.name.equals("done")) p("- **" + (c.name.equals("setup") ? "When it opens, the code" : "When it closes, the code") + ":** `" + squash(c.args, 320) + "`");
                    p("");
                    scene("Opening scene", ch.intro);
                    choice("Question", ch.choice);
                    scene("Closing scene", ch.outro);
                    choice("Question after", ch.outChoice);
                }
            }
            if (d.epilogue.length > 0) { p("## " + n + ".E  Epilogue (when the last chapter is done)"); p(""); scene("Epilogue", d.epilogue); }
            p("## " + n + ".C  The last scene, after the Palace's ending (by how the Garden ends for you)");
            p("");
            String[][] fams = {{"stay", "You stay in the Garden (Steward, Usurper, Idler)"}, {"soldier", "You become a lamp-bearer, a soldier of the Outside"}, {"leave", "You walk out of the Garden alone, or are spared"}, {"fall", "You are struck down by the array"}, {"world", "The Garden itself ends"}};
            for (String[] f : fams) { String[] raw = StoryCodas.TABLE.get(d.id + ":" + f[0]); if (raw != null) scene(f[1], raw); }
        }
        Files.write(dest.toPath(), out.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("[StoryDump] wrote " + dest + " (" + out.length() + " characters)");
    }
}
