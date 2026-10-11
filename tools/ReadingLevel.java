import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A rough reading-level check of the story text (no dependencies): pulls every long string literal out of the given Java files, counts words per sentence and
 *  syllables per word, and prints the Flesch-Kincaid grade, the average sentence length and the longest sentences.
 *  Usage:  javac tools/ReadingLevel.java -d /tmp/rl && java -cp /tmp/rl ReadingLevel src/Game/LoreText.java src/Game/LoreEndings.java ... */
public class ReadingLevel {
    static int syllables(String w) {
        w = w.toLowerCase().replaceAll("[^a-z]", "");
        if (w.isEmpty()) return 0;
        int n = 0; boolean prev = false;
        for (char c : w.toCharArray()) { boolean v = "aeiouy".indexOf(c) >= 0; if (v && !prev) n++; prev = v; }
        if (w.endsWith("e") && !w.endsWith("le") && n > 1) n--;
        return Math.max(1, n);
    }

    public static void main(String[] args) throws Exception {
        Pattern lit = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"");
        long totalWords = 0, totalSyl = 0, totalSent = 0, longSent = 0;
        List<String> longest = new ArrayList<>();
        for (String f : args) {
            String src = new String(Files.readAllBytes(Paths.get(f)), "UTF-8");
            long words = 0, syl = 0, sent = 0, longs = 0;
            Matcher m = lit.matcher(src);
            while (m.find()) {
                String t = m.group(1).replace("\\\"", "\"").replace("\\'", "'").replace("\\n", " ");
                t = t.replaceFirst("^\\d+\\|", "").replaceFirst("^\\* ", "").replaceFirst("^OLD MAN: ", "");
                if (t.length() < 30 || !t.contains(" ") || !t.matches(".*[.!?'\"]\\s*$")) continue;
                if (t.contains("lore ") && t.contains("`")) continue; // help text, not story
                for (String s : t.split("(?<=[.!?])\\s+")) {
                    String[] ws = s.trim().split("\\s+");
                    if (ws.length < 2) continue;
                    sent++; words += ws.length;
                    for (String w : ws) syl += syllables(w);
                    if (ws.length > 25) { longs++; longest.add(ws.length + ": " + s.trim().substring(0, Math.min(90, s.trim().length()))); }
                }
            }
            report(f, words, syl, sent, longs);
            totalWords += words; totalSyl += syl; totalSent += sent; longSent += longs;
        }
        if (args.length > 1) report("TOTAL", totalWords, totalSyl, totalSent, longSent);
        java.util.Collections.sort(longest, (a, b) -> Integer.parseInt(b.substring(0, b.indexOf(':'))) - Integer.parseInt(a.substring(0, a.indexOf(':'))));
        for (int i = 0; i < Math.min(8, longest.size()); i++) System.out.println("  long sentence " + longest.get(i));
    }

    static void report(String name, long words, long syl, long sent, long longs) {
        if (sent == 0) { System.out.println(name + ": no text"); return; }
        double wps = (double) words / sent, spw = (double) syl / words;
        double grade = 0.39 * wps + 11.8 * spw - 15.59;
        System.out.printf("%-34s sentences %5d  words/sentence %5.1f  grade %4.1f  over 25 words: %d%n", name.replaceAll(".*[\\\\/]", ""), sent, wps, grade, longs);
    }
}
