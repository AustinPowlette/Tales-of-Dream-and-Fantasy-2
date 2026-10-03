package Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Terminal-side text formatting shared by the command outputs: numbers with separators, fixed-width tables, headings, hint lines.
 *  ASCII only (the Windows console and the GUI log both have to show it). Pure functions: no game state is touched. */
final class TermFormat {
    private TermFormat() { }

    // ------------------------------------------------------------------ numbers
    /** 1234567 -> "1,234,567". */
    static String n(long v) {
        String s = Long.toString(v), sign = "";
        if (v < 0) { sign = "-"; s = s.substring(1); }
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) { if (i > 0 && (s.length() - i) % 3 == 0) b.append(','); b.append(s.charAt(i)); }
        return sign + b;
    }

    static String ss(long v) { return n(v) + " ss"; }

    /** Parses an integer without ever throwing: junk gives `fallback`, a number too big for an int is clamped (so "wait 99999999999" is a huge
     *  number, not the fallback; commands apply their own caps). */
    static int parseInt(String s, int fallback) {
        if (s == null) return fallback;
        String t = s.trim().replace(",", "").replace("_", "");
        if (t.startsWith("+")) t = t.substring(1);
        if (t.isEmpty()) return fallback;
        try { return Integer.parseInt(t); } catch (NumberFormatException e) { /* maybe just too big */ }
        if (t.matches("-?\\d{10,}")) return t.startsWith("-") ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        return fallback;
    }

    /** `v` limited to lo..hi; when it had to be cut a one-line notice is printed (so a typo like 'wait 9999999' is not silent). */
    static int cap(int v, int lo, int hi, String what) {
        if (v > hi) { PlayerCommands.say("(" + what + " limited to " + n(hi) + ".)"); return hi; }
        return Math.max(lo, v);
    }

    static String pct(double frac) { return Math.round(Math.max(0, Math.min(1, frac)) * 100) + "%"; }

    // ------------------------------------------------------------------ strings
    static String pad(String s, int w) {
        if (s == null) s = "";
        StringBuilder b = new StringBuilder(s);
        while (b.length() < w) b.append(' ');
        return b.toString();
    }

    static String padL(String s, int w) {
        if (s == null) s = "";
        StringBuilder b = new StringBuilder();
        for (int i = s.length(); i < w; i++) b.append(' ');
        return b.append(s).toString();
    }

    /** Cuts a long cell to `max` characters, ending in "..". */
    static String clip(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, Math.max(1, max - 2)) + "..";
    }

    static String repeat(char ch, int n) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < n; i++) b.append(ch);
        return b.toString();
    }

    static String heading(String title) { return "=== " + title + " ==="; }

    /** The closing hint line of a sheet: tells what to type next. */
    static String hint(String s) { return "  Try: " + s + "\n"; }

    static final String[] REALMS = {"Qi Cultivation", "Foundation", "Core Formation", "Soul Wandering", "Nascent Soul", "Ensoulment", "Nihility", "Ascension", "Half-Immortal", "Pseudo Immortal", "True Immortal"};

    static String realm(int major) { return major >= 0 && major < REALMS.length ? REALMS[major] : "Crippled"; }

    static String season(int week) { return TextTools.SEASONS[(week % 52) / 13 % 4]; }

    // ------------------------------------------------------------------ tables
    /** A fixed-width text table. Numbers (Integer/Long) are right-aligned and get thousands separators unless their column is marked raw
     *  (ids, list numbers, coordinates); text is left-aligned. Long text cells are cut to the column's max width when one is set. */
    static final class Table {
        private final String[] headers;
        private final List<Object[]> rows = new ArrayList<>();
        private final Set<Integer> raw = new HashSet<>();
        private final int[] maxW;
        private String indent = "";

        Table(String... headers) { this.headers = headers; this.maxW = new int[headers.length]; }

        Table indent(String s) { indent = s; return this; }
        /** Columns whose numbers are shown as they are (no separators). */
        Table raw(int... cols) { for (int c : cols) raw.add(c); return this; }
        Table max(int col, int w) { if (col >= 0 && col < maxW.length) maxW[col] = w; return this; }
        Table row(Object... cells) { rows.add(cells); return this; }
        int size() { return rows.size(); }

        private String cell(Object o, int col) {
            if (o == null) return "";
            String s = (o instanceof Integer || o instanceof Long) && !raw.contains(col) ? n(((Number) o).longValue()) : String.valueOf(o);
            return maxW[col] > 0 ? clip(s, maxW[col]) : s;
        }

        private static boolean isNum(Object o) { return o instanceof Integer || o instanceof Long || o instanceof Double; }

        String render() {
            int cols = headers.length;
            int[] w = new int[cols];
            boolean[] right = new boolean[cols];
            for (int i = 0; i < cols; i++) { w[i] = headers[i].length(); right[i] = !rows.isEmpty(); }
            for (Object[] r : rows) for (int i = 0; i < cols; i++) {
                Object o = i < r.length ? r[i] : null;
                w[i] = Math.max(w[i], cell(o, i).length());
                if (!isNum(o)) right[i] = false;
            }
            StringBuilder sb = new StringBuilder();
            StringBuilder h = new StringBuilder(indent);
            for (int i = 0; i < cols; i++) h.append(right[i] ? padL(headers[i], w[i]) : pad(headers[i], w[i])).append("  ");
            sb.append(rtrim(h.toString())).append("\n");
            for (Object[] r : rows) {
                StringBuilder l = new StringBuilder(indent);
                for (int i = 0; i < cols; i++) { String c = cell(i < r.length ? r[i] : null, i); l.append(right[i] ? padL(c, w[i]) : pad(c, w[i])).append("  "); }
                sb.append(rtrim(l.toString())).append("\n");
            }
            return sb.toString();
        }
    }

    static String rtrim(String s) {
        int e = s.length();
        while (e > 0 && s.charAt(e - 1) == ' ') e--;
        return s.substring(0, e);
    }

    // ------------------------------------------------------------------ fuzzy matching
    /** Levenshtein distance (small strings only). */
    static int distance(String a, String b) {
        int[] prev = new int[b.length() + 1], cur = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) prev[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            cur[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
                if (i > 1 && j > 1 && a.charAt(i - 1) == b.charAt(j - 2) && a.charAt(i - 2) == b.charAt(j - 1)) cur[j] = Math.min(cur[j], prev[j - 2] + 1); // a swap counts once
            }
            int[] t = prev; prev = cur; cur = t;
        }
        return prev[b.length()];
    }

    static List<String> sortedUnique(java.util.Collection<String> c) {
        List<String> l = new ArrayList<>(new java.util.TreeSet<>(c));
        return l;
    }

    static String join(List<String> l, int max) {
        if (l.size() <= max) return String.join(", ", l);
        return String.join(", ", l.subList(0, max)) + ", ... (" + (l.size() - max) + " more)";
    }

    static List<String> list(String... s) { return new ArrayList<>(Arrays.asList(s)); }
}
