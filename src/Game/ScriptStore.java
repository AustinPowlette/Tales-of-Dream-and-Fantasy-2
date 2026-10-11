package Game;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

/** Named scripts kept on disk (the `scripts` folder next to `saves`), one step per line in the same words `script list` shows, so they can also be edited by hand. */
final class ScriptStore {
    private ScriptStore() { }

    static final File DIR = new File("scripts");
    static final int MAX_NAME = 40;

    static String clean(String name) {
        String n = name == null ? "" : name.trim().replaceAll("[^A-Za-z0-9 _-]", "").trim();
        return n.length() > MAX_NAME ? n.substring(0, MAX_NAME).trim() : n;
    }

    private static File file(String name) { return new File(DIR, clean(name) + ".txt"); }

    /** The saved scripts, alphabetical. */
    static String[] names() {
        File[] fs = DIR.listFiles((d, n) -> n.endsWith(".txt"));
        if (fs == null) return new String[0];
        String[] r = new String[fs.length];
        for (int i = 0; i < fs.length; i++) r[i] = fs[i].getName().substring(0, fs[i].getName().length() - 4);
        Arrays.sort(r, String.CASE_INSENSITIVE_ORDER);
        return r;
    }

    /** Saves the script under a name. Returns null, or why not. */
    static String save(String name, PlayerScript s) {
        String n = clean(name);
        if (n.isEmpty()) return "Give the script a name (letters, digits, spaces, - and _).";
        if (s.steps.isEmpty()) return "The script is empty: nothing to save.";
        try {
            if (!DIR.exists() && !DIR.mkdirs()) return "Could not create the scripts folder.";
            try (PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file(n)), StandardCharsets.UTF_8))) {
                w.println("# Tales of Dream and Fantasy 2 script: one step per line (lines starting with # are notes)");
                for (PlayerScript.Step st : s.steps) w.println(st);
            }
        } catch (IOException e) { return "Could not save: " + e.getMessage(); }
        return null;
    }

    /** Loads a saved script into `into` (its steps are replaced). Returns null, or why not; lines that do not parse are skipped and counted in the message. */
    static String load(String name, PlayerScript into) {
        File f = file(name);
        if (!f.isFile()) return "No saved script called '" + clean(name) + "'. 'script saved' lists them.";
        PlayerScript fresh = new PlayerScript();
        ArrayList<String> bad = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String err = fresh.add(line.split("\\s+"));
                if (err != null) bad.add(line + " (" + err + ")");
            }
        } catch (IOException e) { return "Could not read it: " + e.getMessage(); }
        into.steps.clear();
        into.steps.addAll(fresh.steps);
        into.reset();
        return bad.isEmpty() ? null : "Loaded " + fresh.steps.size() + " steps; skipped " + bad.size() + ": " + bad;
    }

    static boolean delete(String name) { return file(name).delete(); }

    /** The steps of a saved script as written in its file (comments and blank lines left out), for a preview. */
    static java.util.List<String> read(String name) {
        java.util.List<String> out = new ArrayList<>();
        File f = file(name);
        if (!f.isFile()) return out;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) { line = line.trim(); if (!line.isEmpty() && !line.startsWith("#")) out.add(line); }
        } catch (IOException ignored) { }
        return out;
    }

    /** Renames a saved script. Returns null, or why not. */
    static String rename(String from, String to) {
        String n = clean(to);
        if (n.isEmpty()) return "Give the script a name (letters, digits, spaces, - and _).";
        File a = file(from), b = file(n);
        if (!a.isFile()) return "No saved script called '" + clean(from) + "'.";
        if (clean(from).equals(n)) return null;
        if (b.exists() && !clean(from).equalsIgnoreCase(n)) return "A saved script called '" + n + "' exists already.";
        return a.renameTo(b) ? null : "Could not rename the file.";
    }
}
