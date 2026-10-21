package Game;

import java.util.ArrayList;

/** A hybrid-mode script: an ordered list of steps a human-controlled character carries out one turn at a time
 *  (go to a place, work a profession N times, hunt, sell, do a queued quest, loops and conditions), then hands control back. */
public class PlayerScript implements java.io.Serializable {
    /** n spaces (String.repeat needs Java 11; the project builds with JDK 8). */
    static String spaces(int n) { StringBuilder sb = new StringBuilder(); for (int i = 0; i < n; i++) sb.append(' '); return sb.toString(); }

    static final long serialVersionUID = 1L;

    static class Step implements java.io.Serializable {
        static final long serialVersionUID = 1L;
        String type;       // goto wait forage mine craft meditate hunt sell quest repeat if end mode
        long a, b;         // numeric arguments (see parse())
        String text = "";  // quest name / mode / condition variable
        char op = '>';     // for `if`: '>' means >=, '<' means <=

        public String toString() {
            switch (type) {
                case "goto":     return "goto " + a + " " + b;
                case "wait":     return "wait " + a;
                case "forage": case "mine":
                                 return type + (b > 0 ? " until " + b : " " + a);
                case "craft": case "hunt":
                                 return type + " " + a;
                case "meditate": return "meditate " + (b > 0 ? "until major " + b : a);
                case "quest":    return "quest " + text;
                case "repeat":   return "repeat " + (a == 0 ? "forever" : String.valueOf(a));
                case "if":       return "if " + text + (op == '>' ? " >= " : " <= ") + a;
                case "mode":     return "mode " + text;
                case "cmd":      return "cmd " + text;
                default:         return type;
            }
        }
    }

    static final int WORKING = 0, DONE_TURN = 1, DONE_FREE = 2, BLOCKED = 3;
    static final int MAX_FREE_STEPS = 60; // steps that use no turn (loop bookkeeping, conditions) allowed per tick

    ArrayList<Step> steps = new ArrayList<>();
    int ip = 0;                  // next step
    long counter = 0;            // progress inside the current step
    int failStreak = 0;          // consecutive hunts that found nothing suitable to fight
    boolean running = false;
    ArrayList<long[]> frames = new ArrayList<>(); // {kind(0 loop, 1 if), startIp, remaining}
    String lastReason = "";

    // ---------------------------------------------------------------- authoring
    /** Parses one step from command tokens (without the leading "script add"); returns an error message or null on success. */
    String add(String[] t) {
        if (t.length == 0) return "Empty step.";
        Step s = new Step();
        s.type = t[0].toLowerCase();
        if (s.type.equals("cmd")) return t.length < 2 ? "Usage: cmd <any command>   e.g. cmd buygear, cmd make food, cmd learn 1" : addCmd(java.util.Arrays.copyOfRange(t, 1, t.length));
        boolean numeric = t.length > 1 && t[1].matches("\\d+"), until = t.length > 1 && t[1].equalsIgnoreCase("until");
        if (!NATIVE.contains(s.type)) return addCmd(t);                                                              // any other word is a command of the game
        if ((s.type.equals("craft") || s.type.equals("hunt") || s.type.equals("forage") || s.type.equals("mine") || s.type.equals("meditate")) && !numeric && !until) return addCmd(t); // "craft weapon", "hunt", "forage"
        if ((s.type.equals("sell") && t.length > 1) || (s.type.equals("wait") && !numeric)) return addCmd(t);       // "sell goods", "sell 3", a bare "wait"
        try {
            switch (s.type) {
                case "goto":
                    if (t.length < 3) return "Usage: goto <x> <y>";
                    s.a = tile(Long.parseLong(t[1])); s.b = tile(Long.parseLong(t[2]));
                    if (s.a < 0 || s.a >= Map.range || s.b < 0 || s.b >= Map.range)
                        return "Coordinates must be tiles 0-" + (Map.range - 1) + " or multiples of 100 up to " + ((Map.range - 1) * 100) + ".";
                    break;
                case "wait": case "craft": case "hunt":
                    if (t.length < 2) return "Usage: " + s.type + " <turns>";
                    s.a = Long.parseLong(t[1]);
                    if (s.a < 1) return "Count must be at least 1.";
                    break;
                case "forage": case "mine":
                    if (t.length >= 3 && t[1].equalsIgnoreCase("until")) s.b = Long.parseLong(t[2]);
                    else if (t.length >= 2) s.a = Long.parseLong(t[1]);
                    else return "Usage: " + s.type + " <count> | " + s.type + " until <quantity>";
                    if (s.a < 1 && s.b < 1) return "Count must be at least 1.";
                    break;
                case "meditate":
                    if (t.length >= 4 && t[1].equalsIgnoreCase("until") && t[2].equalsIgnoreCase("major")) s.b = Long.parseLong(t[3]);
                    else if (t.length >= 2) s.a = Long.parseLong(t[1]);
                    else return "Usage: meditate <turns> | meditate until major <level>";
                    if (s.a < 1 && s.b < 1) return "Count must be at least 1.";
                    break;
                case "sell": case "end":
                    break;
                case "quest":
                    if (t.length < 2) return "Usage: quest <quest name>";
                    s.text = String.join(" ", java.util.Arrays.copyOfRange(t, 1, t.length));
                    break;
                case "repeat":
                    if (t.length < 2) return "Usage: repeat <n|forever>";
                    s.a = t[1].equalsIgnoreCase("forever") ? 0 : Long.parseLong(t[1]);
                    if (s.a < 0) return "Repeat count must be positive.";
                    break;
                case "if": {
                    String cond = String.join("", java.util.Arrays.copyOfRange(t, 1, t.length));
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("^([a-z]+)(>=|<=)(-?\\d+)$").matcher(cond.toLowerCase());
                    if (!m.matches() || !validVar(m.group(1))) return "Usage: if <ss|major|level|age|herb|metal|items|potential> >= <n>  (or <=)";
                    s.text = m.group(1); s.op = m.group(2).equals(">=") ? '>' : '<'; s.a = Long.parseLong(m.group(3));
                    break;
                }
                case "mode":
                    if (t.length < 2 || !(t[1].equalsIgnoreCase("auto") || t[1].equalsIgnoreCase("manual")))
                        return "Usage: mode auto|manual";
                    s.text = t[1].toLowerCase();
                    break;
                default:
                    return "Unknown step '" + s.type + "'. Steps: goto wait forage mine craft meditate hunt sell quest repeat if end mode, or any command (cmd <command>)";
            }
        } catch (NumberFormatException e) {
            return "Expected a number.";
        }
        steps.add(s);
        return null;
    }

    /** The words that are steps of their own; any other first word is run as a game command. */
    static final java.util.Set<String> NATIVE = new java.util.HashSet<>(java.util.Arrays.asList("goto", "wait", "forage", "mine", "craft", "meditate", "hunt", "sell", "quest", "repeat", "if", "end", "mode"));
    /** Commands a script may not run: they would end or take over the script itself, or ask for typing. */
    static final java.util.Set<String> NOT_IN_SCRIPT = new java.util.HashSet<>(java.util.Arrays.asList("script", "quit", "exit", "save", "load", "delsave", "retire", "control", "release", "auto", "manual", "stop", "hybrid", "watch", "e", "edit", "new", "cmds", "last", "again", "!!"));
    private static final java.util.Set<String> MOVES = new java.util.HashSet<>(java.util.Arrays.asList("w", "a", "s", "d", "q", "e", "z", "c", "n", "ne", "nw", "se", "sw", "x", "north", "south", "east", "west", "stay"));

    /** A step that runs one game command (anything the command box takes), with its arguments. */
    private String addCmd(String[] t) {
        String text = String.join(" ", t).trim();
        String verb = text.toLowerCase().split("\\s+")[0];
        if (verb.startsWith("!")) { verb = verb.substring(1); text = text.substring(1); }
        if (NOT_IN_SCRIPT.contains(verb)) return "'" + verb + "' cannot be a script step (it would end or take over the script).";
        if (!MOVES.contains(verb) && TermHelp.find(verb) == null && TermHelp.expand(verb) == null) return "Unknown step or command '" + verb + "'. 'help' lists the commands.";
        Step s = new Step();
        s.type = "cmd"; s.text = text;
        steps.add(s);
        return null;
    }

    static long tile(long v) { return v >= 100 ? v / 100 : v; } // accept displayed coordinates (x*100) or tile numbers

    static boolean validVar(String v) {
        switch (v) { case "ss": case "major": case "level": case "age": case "herb": case "metal": case "items": case "potential": return true; }
        return false;
    }

    /** Checks that repeat/if blocks are balanced; returns an error message or null. */
    String validate() {
        int depth = 0;
        for (Step s : steps) {
            if (s.type.equals("repeat") || s.type.equals("if")) depth++;
            else if (s.type.equals("end")) { depth--; if (depth < 0) return "'end' without a matching repeat/if."; }
        }
        return depth > 0 ? "Missing 'end' for " + depth + " open repeat/if block(s)." : null;
    }

    String list() {
        StringBuilder sb = new StringBuilder("=== Script (" + steps.size() + " steps)" + (running ? " RUNNING" : "") + " ===\n");
        int depth = 0;
        for (int i = 0; i < steps.size(); i++) {
            Step s = steps.get(i);
            if (s.type.equals("end")) depth = Math.max(0, depth - 1);
            sb.append(String.format("%3d. %s%s%s%n", i + 1, spaces(2 * depth), s, i == ip && running ? "   <-- here" : ""));
            if (s.type.equals("repeat") || s.type.equals("if")) depth++;
        }
        return sb.toString();
    }

    void clear() { steps.clear(); reset(); }

    void reset() { ip = 0; counter = 0; running = false; frames.clear(); }

    // ---------------------------------------------------------------- execution
    void start(Character c) {
        PlayerControl pc = c.pc();
        ip = 0; counter = 0; frames.clear();
        running = true;
        pc.mode = PlayerControl.Mode.HYBRID;
        pc.interruptMajor = c.majorLvl;
        pc.attackedThisTick = false;
    }

    void resume(Character c) {
        PlayerControl pc = c.pc();
        running = true;
        pc.mode = PlayerControl.Mode.HYBRID;
        pc.interruptMajor = c.majorLvl;
        pc.attackedThisTick = false;
    }

    private void halt(Character c, String reason, boolean finished) {
        running = false;
        lastReason = reason;
        PlayerControl pc = c.pc();
        pc.mode = pc.returnToAuto ? PlayerControl.Mode.AUTO : PlayerControl.Mode.MANUAL;
        c.skipping = 0;
        System.out.println((finished ? "[Script finished] " : "[Script stopped] ") + reason + "  Control returns to " + pc.mode + ".");
        if (finished) { ip = 0; counter = 0; frames.clear(); }
    }

    void stop(Character c, String reason) { if (running) halt(c, reason, false); }

    /** Advances the script by one turn. Interrupts (attack, breakthrough, new alert) stop it and hand control back. */
    void tick(Character c) {
        if (!running) return;
        PlayerControl pc = c.pc();
        boolean freshAlert = pc.alertPending;
        pc.alertPending = false;
        if (pc.attackedThisTick) { pc.attackedThisTick = false; halt(c, "You were attacked.", false); return; }
        if (c.majorLvl > pc.interruptMajor && pc.interruptMajor >= 0) { pc.interruptMajor = c.majorLvl; halt(c, "You broke through to a new realm!", false); return; }
        if (freshAlert && pc.interruptOnAlert) { halt(c, "A new quest alert arrived.", false); return; }

        int free = 0;
        while (running) {
            if (ip >= steps.size()) { halt(c, "The script ran to its end.", true); return; }
            if (++free > MAX_FREE_STEPS) { return; } // an empty loop body etc.: yield the turn rather than freeze
            Step s = steps.get(ip);
            int r = exec(c, s);
            if (r == BLOCKED) return; // exec already halted
            if (r == WORKING) return;
            counter = 0;
            failStreak = 0;
            if (r == DONE_FREE) continue;
            return; // DONE_TURN: step finished and used the turn
        }
    }

    private int matchingEnd(int from) {
        int depth = 0;
        for (int i = from; i < steps.size(); i++) {
            String t = steps.get(i).type;
            if (t.equals("repeat") || t.equals("if")) depth++;
            else if (t.equals("end")) { depth--; if (depth == 0) return i; }
        }
        return steps.size();
    }

    // Executes the current step; on completion this method has already moved `ip` on.
    private int exec(Character c, Step s) {
        switch (s.type) {
            case "repeat": {
                if (s.a == 0 && matchingEnd(ip) == ip + 1) { ip++; return DONE_TURN; } // empty forever-loop: nothing to do
                frames.add(new long[]{0, ip, s.a == 0 ? -1 : s.a});
                ip++;
                return DONE_FREE;
            }
            case "if": {
                if (cond(c, s)) { frames.add(new long[]{1, ip, 0}); ip++; }
                else ip = matchingEnd(ip) + 1;
                return DONE_FREE;
            }
            case "end": {
                if (frames.isEmpty()) { ip++; return DONE_FREE; }
                long[] f = frames.get(frames.size() - 1);
                if (f[0] == 1) { frames.remove(frames.size() - 1); ip++; return DONE_FREE; }
                if (f[2] > 0) f[2]--;
                if (f[2] == 0) { frames.remove(frames.size() - 1); ip++; }
                else ip = (int) f[1] + 1;
                return DONE_FREE;
            }
            case "mode": {
                PlayerControl pc = c.pc();
                ip++;
                if (s.text.equals("auto")) { pc.mode = PlayerControl.Mode.AUTO; pc.autoTurns = 0; running = false; System.out.println("[Script] switching to AUTO."); return BLOCKED; }
                running = false; pc.mode = PlayerControl.Mode.MANUAL; System.out.println("[Script] switching to MANUAL."); return BLOCKED;
            }
            case "goto": {
                int tx = (int) s.a, ty = (int) s.b;
                if (c.location.x == tx && c.location.y == ty) { ip++; return DONE_FREE; }
                c.stepToward(tx, ty);
                if (c.location.x == tx && c.location.y == ty) { ip++; return DONE_TURN; }
                return WORKING;
            }
            case "wait": {
                if (++counter >= s.a) { ip++; return DONE_TURN; }
                return WORKING;
            }
            case "forage": case "mine": {
                boolean herb = s.type.equals("forage");
                if (s.b > 0 && materialQty(c, herb ? Item.ItemType.Herb : Item.ItemType.Metal) >= s.b) { ip++; return DONE_FREE; }
                if ((herb ? c.location.herbMaterials : c.location.metalMaterials) <= 0) {
                    halt(c, "This tile has no " + (herb ? "herbs" : "metal") + " left.", false);
                    return BLOCKED;
                }
                if (herb) c.forage(); else c.mine();
                if (s.b == 0 && ++counter >= s.a) { ip++; return DONE_TURN; }
                return WORKING;
            }
            case "craft": {
                if (c.hasTrade()) Profession.run(c); else c.professionPicker();
                if (++counter >= s.a) { ip++; return DONE_TURN; }
                return WORKING;
            }
            case "meditate": {
                if (s.b > 0 && c.majorLvl >= s.b) { ip++; return DONE_FREE; }
                c.meditate();
                if (s.b == 0 && ++counter >= s.a) { ip++; return DONE_TURN; }
                return WORKING;
            }
            case "hunt": {
                if (c.location.monsters <= 0 && counter == 0) { halt(c, "No beasts to hunt here.", false); return BLOCKED; }
                if (c.partyBeastHunt()) failStreak = 0;
                else if (++failStreak >= 5) { halt(c, "The beasts here are too strong (or too weak to bother with) for you.", false); return BLOCKED; }
                if (++counter >= s.a || c.location.monsters <= 0) { ip++; return DONE_TURN; }
                return WORKING;
            }
            case "sell": {
                if (!c.location.city) { halt(c, "You are not in a city, so there is nowhere to sell.", false); return BLOCKED; }
                c.sell(c.location);
                ip++;
                return DONE_TURN;
            }
            case "cmd": {
                if (counter > 0) { // a walk or a wait this step began is still going
                    if (c.skipping > 0) {
                        c.skipping--;
                        if (c.movement == 'm') c.movement();
                        else if (c.movement == 'c' && c.autoX >= 0) PlayerCommands.stepAuto(c);
                        else c.movement(c.movement);
                    }
                    if (c.skipping > 0) { counter++; return WORKING; }
                    ip++;
                    return DONE_TURN;
                }
                PlayerControl pc = c.pc();
                c.skipping = 0;
                boolean used = PlayerCommands.handle(c, "!" + s.text);
                if (c.dead) return BLOCKED;
                if (pc.mode != PlayerControl.Mode.HYBRID || !running) { ip++; running = false; return BLOCKED; } // the command handed control back
                if (c.skipping > 0) { counter = 1; return WORKING; }
                ip++;
                return used ? DONE_TURN : DONE_FREE;
            }
            case "quest": {
                Quest q = c.findQueuedQuest(s.text);
                if (q == null) { halt(c, "No quest called '" + s.text + "' is queued.", false); return BLOCKED; }
                Area dest = c.questDestination(q);
                if (dest != null && dest != c.location) { c.stepToward(dest.x, dest.y); return WORKING; }
                String why = c.doQuestNow(q);
                if (why != null) { halt(c, why, false); return BLOCKED; }
                ip++;
                return DONE_TURN;
            }
        }
        halt(c, "Unknown step " + s.type, false);
        return BLOCKED;
    }

    static long materialQty(Character c, Item.ItemType type) {
        long n = 0;
        for (Item i : c.materialBag) if (i != null && i.itemType == type) n += i.quantity;
        return n;
    }

    private boolean cond(Character c, Step s) {
        long v;
        switch (s.text) {
            case "ss": v = c.ss; break;
            case "major": v = c.majorLvl; break;
            case "level": v = c.lvl; break;
            case "age": v = c.age; break;
            case "herb": v = materialQty(c, Item.ItemType.Herb); break;
            case "metal": v = materialQty(c, Item.ItemType.Metal); break;
            case "items": v = c.backpack.size(); break;
            case "potential": v = c.potential; break;
            default: return false;
        }
        return s.op == '>' ? v >= s.a : v <= s.a;
    }
}
