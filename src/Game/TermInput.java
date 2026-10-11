package Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** Front door of the command interpreter (terminal and GUI): PlayerCommands.handle() calls run(), which adds
 *  - `last` / `again` / `!!` and a `cmds` list of what was typed,
 *  - aliases, unambiguous prefixes ("inv" = bag) and "did you mean ...?" for words the switch does not know,
 *  - the `hud on|off` toggle,
 *  - a safety net: an exception inside a command is reported (with its place) instead of killing the game thread.
 *  The real command switch is PlayerCommands.dispatch(); nothing here changes what a known command does. */
final class TermInput {
    private TermInput() { }

    private static final int MAX_HISTORY = 200;
    private static final HashMap<Integer, ArrayList<String>> HISTORY = new HashMap<>();
    private static int depth = 0;
    private static boolean sawUnknown = false;

    static ArrayList<String> history(Character c) {
        ArrayList<String> h = HISTORY.get(c.id);
        if (h == null) { h = new ArrayList<>(); HISTORY.put(c.id, h); }
        return h;
    }

    /** Entry point used by PlayerCommands.handle(). */
    static boolean run(Character c, String line) {
        if (line == null) return true;
        String l = line.trim();
        if (l.isEmpty()) return false;
        boolean top = depth == 0;
        if (top) {
            String low = l.toLowerCase();
            if (low.equals("!!") || low.equals("last") || low.equals("again") || low.startsWith("last ") || low.startsWith("again ")) {
                String prev = recall(c, low);
                if (prev == null) return false;
                l = prev;
                PlayerCommands.say("(repeating: " + prev + ")");
            }
        }
        sawUnknown = false;
        depth++;
        try {
            boolean used = PlayerCommands.dispatch(c, l);
            if (top && !sawUnknown && !l.startsWith("!") && !isMeta(l)) record(c, l);
            return used;
        } catch (RuntimeException | StackOverflowError ex) {
            report(l, ex);
            return false;
        } finally { depth--; }
    }

    private static boolean isMeta(String l) {
        String w = l.toLowerCase().split("\\s+")[0];
        return w.equals("last") || w.equals("again") || w.equals("!!") || w.equals("cmds") || w.equals("help") || w.equals("?") || w.equals("commands");
    }

    private static void record(Character c, String l) {
        ArrayList<String> h = history(c);
        if (!h.isEmpty() && h.get(h.size() - 1).equals(l)) return; // x x x x is one entry
        h.add(l);
        while (h.size() > MAX_HISTORY) h.remove(0);
    }

    /** `last` = the previous command, `last 3` = the third most recent. */
    private static String recall(Character c, String low) {
        ArrayList<String> h = history(c);
        if (h.isEmpty()) { PlayerCommands.say("Nothing to repeat yet: type a command first ('cmds' lists what you typed)."); return null; }
        int n = 1;
        String[] p = low.split("\\s+");
        if (p.length > 1) {
            n = TermFormat.parseInt(p[1], -1);
            if (n < 1 || n > h.size()) { PlayerCommands.say("Usage: last [n]  (1 = the previous command, up to " + h.size() + "; 'cmds' shows them)"); return null; }
        }
        return h.get(h.size() - n);
    }

    private static void report(String l, Throwable ex) {
        StringBuilder sb = new StringBuilder("[command error] '" + TermFormat.clip(l, 60) + "' failed: " + ex.getClass().getSimpleName());
        if (ex.getMessage() != null) sb.append(" (").append(TermFormat.clip(ex.getMessage(), 80)).append(')');
        StackTraceElement[] st = ex.getStackTrace();
        for (int i = 0; i < Math.min(2, st.length); i++) sb.append(i == 0 ? " at " : " < ").append(st[i].getClassName().replace("Game.", "")).append('.').append(st[i].getMethodName()).append(':').append(st[i].getLineNumber());
        sb.append(". The command was not completed; check the arguments or report this.");
        PlayerCommands.say(sb.toString());
    }

    // ------------------------------------------------------------------ words the switch does not know
    private static String dir(String w) {
        switch (w) {
            case "north": case "up": return "w";
            case "south": case "down": return "s";
            case "west": case "left": return "a";
            case "east": case "right": return "d";
            default: return null;
        }
    }

    /** Called by the `default:` branch of the switch: handles hud/cmds, aliases and prefixes, or explains. Returns what the re-dispatched command returned. */
    static boolean unknown(Character c, String line, String cmd, boolean confirmed) {
        String[] t = line.trim().split("\\s+");
        String lc = cmd.toLowerCase();
        if (lc.equals("cmds")) { cmds(c, t); return false; }
        if (lc.equals("hud")) { hud(t); return false; }
        String rest = line.trim().length() > t[0].length() ? line.trim().substring(t[0].length()) : "";
        String bang = confirmed ? "!" : "";
        if (lc.equals("notify")) return PlayerCommands.dispatch(c, bang + "settings notify" + rest);
        String d = dir(lc);
        if (d != null) { PlayerCommands.say("(" + lc + " = " + d + ")"); return PlayerCommands.dispatch(c, bang + d); }
        String full = TermHelp.expand(lc);
        if (full != null && !full.equals(lc)) {
            PlayerCommands.say("(" + lc + " = " + full + ")");
            return PlayerCommands.dispatch(c, bang + full + rest);
        }
        sawUnknown = true;
        StringBuilder sb = new StringBuilder("Unknown command '" + (cmd.length() > 40 ? cmd.substring(0, 40) + "..." : cmd) + "'.");
        List<String> hits = lc.length() >= 1 ? TermHelp.prefixHits(lc) : new ArrayList<String>();
        if (hits.size() > 1) sb.append(" Several commands start that way: ").append(TermFormat.join(hits, 8)).append(".");
        else {
            List<String> sug = TermHelp.suggest(lc, 3);
            if (!sug.isEmpty()) sb.append(" Did you mean: ").append(String.join(", ", sug)).append("?");
        }
        sb.append(" Type 'help' for the command groups, 'help <command>' for details.");
        PlayerCommands.say(sb.toString());
        return false;
    }

    private static void cmds(Character c, String[] t) {
        ArrayList<String> h = history(c);
        int n = t.length > 1 ? Math.max(1, Math.min(100, TermFormat.parseInt(t[1], 15))) : 15;
        StringBuilder sb = new StringBuilder(TermFormat.heading("Commands you typed (newest first)")).append("\n");
        if (h.isEmpty()) sb.append("  (none yet)\n");
        for (int i = 0; i < Math.min(n, h.size()); i++) sb.append(String.format("  %2d  %s%n", i + 1, h.get(h.size() - 1 - i)));
        sb.append(TermFormat.hint("'last' repeats the newest, 'last 3' the third one"));
        GameDriver.showGuiInfo("Command history", sb.toString());
    }

    private static void hud(String[] t) {
        if (t.length > 1) Settings.hud = PlayerCommands.onOff(t[1]);
        else Settings.hud = !Settings.hud;
        Settings.save();
        PlayerCommands.say("Status line " + (Settings.hud ? "on" : "off") + (Settings.hud ? "  (hide it with: hud off)" : "  (show it again with: hud on)") + (GameDriver.guiEnabled ? "  - only the terminal shows it; the window has its own status bar." : ""));
    }
}
