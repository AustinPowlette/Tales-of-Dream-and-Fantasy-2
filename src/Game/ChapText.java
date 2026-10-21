package Game;

import java.util.ArrayList;
import java.util.List;

/** The words of a chapter: which lines show (a line may begin with ?flag| , ?!flag| or ?flag=2| , as many as it likes), the placeholders, and the framed block the terminal prints.
 *  A line that starts with "* " is narration; "@Name| words" is said by Name; anything else is said by nobody in particular and is shown as narration.
 *  Placeholders: {name} {first} {last} {year} {age}; {key} {key.first} {key.last} a person of the story (and {key.he} {key.him} {key.his} ...); {@place} a place of the story; {#flag} a flag's number; {=word} a name or phrase the story took from the world. */
final class ChapText {
    private ChapText() { }

    /** The lines of a scene that apply now, with the placeholders filled in. */
    static List<String> prepare(String[] raw, Character c, Starts.State st) {
        refresh(st);
        List<String> out = new ArrayList<>();
        if (raw == null) return out;
        for (String line : raw) {
            String l = line;
            boolean show = true;
            while (l.startsWith("?")) {
                int bar = l.indexOf('|');
                if (bar < 0) break;
                String cond = l.substring(1, bar);
                l = l.substring(bar + 1);
                boolean neg = cond.startsWith("!");
                if (neg) cond = cond.substring(1);
                boolean ok;
                int eq = cond.indexOf('=');
                if (eq >= 0) ok = Chapters.flag(st, cond.substring(0, eq)) == Integer.parseInt(cond.substring(eq + 1));
                else ok = Chapters.flag(st, cond) != 0;
                if (ok == neg) show = false;
            }
            if (show) out.add(fill(l, c, st));
        }
        return out;
    }

    /** The automatic flags: dead.<key> and gone.<key> (a person of the story who died, or is not to be found), which the text may test. */
    static void refresh(Starts.State st) {
        if (st == null || st.npcs == null) return;
        for (java.util.Map.Entry<String, Integer> e : st.npcs.entrySet()) {
            Character n = Taming.byId(e.getValue());
            boolean dead = n == null || n.dead;
            Chapters.set(st, "dead." + e.getKey(), dead ? 1 : 0);
        }
    }

    static String fill(String s, Character c, Starts.State st) {
        if (s == null || s.indexOf('{') < 0) return s == null ? "" : s;
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            int open = s.indexOf('{', i);
            if (open < 0) { out.append(s, i, s.length()); break; }
            int close = s.indexOf('}', open);
            if (close < 0) { out.append(s, i, s.length()); break; }
            out.append(s, i, open);
            String tok = s.substring(open + 1, close);
            String val = token(tok, c, st);
            out.append(val == null ? "{" + tok + "}" : val);
            i = close + 1;
        }
        return out.toString();
    }

    private static String token(String t, Character c, Starts.State st) {
        switch (t) {
            case "name": return c.name + " " + c.lastName;
            case "first": return c.name;
            case "last": return c.lastName;
            case "year": return String.valueOf(GameDriver.time / 52 + 1);
            case "age": return String.valueOf(c.age);
        }
        if (t.startsWith("@")) return Chapters.placeName(c, st, t.substring(1));
        if (t.startsWith("#")) return String.valueOf(Chapters.flag(st, t.substring(1)));
        if (t.startsWith("=")) { String w = st.words == null ? null : st.words.get(t.substring(1)); return w == null ? "someone" : w; }
        String key = t, part = "";
        int dot = t.indexOf('.');
        if (dot >= 0) { key = t.substring(0, dot); part = t.substring(dot + 1); }
        Chapters.Def d = Chapters.DEFS.get(st.story);
        Chapters.Cast sp = d == null ? null : d.cast.get(key);
        Character who = Chapters.person(st, key);   // the real person, if they exist: a companion's name was chosen by the human
        if (sp == null && who == null) return null;
        boolean fem = who != null ? "Female".equals(who.gender) : Chapters.isFemale(sp, c);
        String firstN = who != null ? who.name : Chapters.firstName(sp, c), lastN = who != null ? who.lastName : sp.last;
        switch (part) {
            case "first": return firstN;
            case "last": return lastN;
            case "he": return fem ? "she" : "he";
            case "He": return fem ? "She" : "He";
            case "him": return fem ? "her" : "him";
            case "Him": return fem ? "Her" : "Him";
            case "his": return fem ? "her" : "his";
            case "His": return fem ? "Her" : "His";
            case "hers": return fem ? "hers" : "his";
            case "man": return fem ? "woman" : "man";
            case "Man": return fem ? "Woman" : "Man";
            case "boy": return fem ? "girl" : "boy";
            case "lad": return fem ? "lass" : "lad";
            case "sib": return fem ? "sister" : "brother";
            default: return firstN + " " + lastN;
        }
    }

    // ------------------------------------------------------------------ lines
    static boolean narration(String line) { return line.startsWith("* "); }
    static boolean speaker(String line) { return line.startsWith("@") && line.indexOf('|') > 1; }
    static String who(String line) { return line.substring(1, line.indexOf('|')); }
    static String words(String line) { return line.substring(line.indexOf('|') + 1).trim(); }

    // ------------------------------------------------------------------ the terminal's block
    static String frame(String title, List<String> lines) {
        final int w = 74;
        StringBuilder sb = new StringBuilder();
        String bar = "+" + LoreText.dashes(w + 2) + "+";
        sb.append(bar).append('\n');
        sb.append("| ").append(LoreText.pad(title.toUpperCase(), w)).append(" |\n");
        sb.append("|").append(LoreText.dashes(w + 2)).append("|\n");
        for (String l : lines) {
            String first, body;
            if (speaker(l)) { first = who(l).toUpperCase() + ": "; body = words(l); }
            else { first = "  "; body = narration(l) ? l.substring(2) : l; }
            String indent = LoreText.spaces(first.length());
            boolean firstLine = true;
            for (String part : LoreText.wrap(body, w - first.length())) {
                sb.append("| ").append(LoreText.pad((firstLine ? first : indent) + part, w)).append(" |\n");
                firstLine = false;
            }
            sb.append("| ").append(LoreText.spaces(w)).append(" |\n");
        }
        sb.append(bar);
        return sb.toString();
    }
}
