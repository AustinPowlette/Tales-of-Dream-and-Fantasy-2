package Game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/** Sequential decision broker. Decisions a human-controlled character faces (kill or spare, flee, forbidden technique, accept a
 *  duel, diplomacy offers, auction bids) are raised from anywhere in tick processing; all of it runs on the single game thread, so
 *  requests naturally reach the player one at a time, in the order they occur. A character that is not in MANUAL mode (or when no
 *  input can be read) gets the answer from its policy instead of a prompt. Each character may set a timeout (`policy timeout <s>`):
 *  an unanswered prompt then falls back to the policy answer so a forgotten window never freezes the world. */
public class DecisionBroker {
    /** Headless tests: pre-loaded answers (0-based option index). Non-null = answers come from here and never block. */
    static ArrayDeque<Integer> scripted = null;
    /** Headless tests: pre-loaded numeric answers (auction bids). */
    static ArrayDeque<Long> scriptedNumbers = null;
    /** Every prompt raised (kept only while scripted != null, so tests can assert on ordering). */
    static List<String> promptLog = new ArrayList<>();
    static int prompts = 0;   // total prompts shown or scripted
    static int timeouts = 0;  // prompts that fell back to the policy answer because nobody answered in time

    // True when this character's decisions should be put to a human
    static boolean promptable(Character c) {
        if (c == null || c.player != 1) return false;
        PlayerControl pc = c.pc();
        return pc != null && pc.mode == PlayerControl.Mode.MANUAL;
    }

    static int timeoutOf(Character c) {
        PlayerControl pc = c.pc();
        return pc == null ? 0 : Math.max(0, pc.decisionTimeout);
    }

    /** Yes/no question for a human-controlled character; a non-MANUAL character (or unavailable input) gets `defaultYes`. */
    static boolean askYesNo(Character c, String prompt, boolean defaultYes) {
        return ask(c, prompt, new String[]{"Yes", "No"}, defaultYes ? 0 : 1) == 0;
    }

    /** Returns the 0-based index of the chosen option; `policyIdx` is used when the character isn't promptable, input is unavailable
     *  or the character's decision timeout runs out. */
    static synchronized int ask(Character c, String prompt, String[] options, int policyIdx) {
        if (!promptable(c)) return policyIdx;
        prompts++;
        if (scripted != null) {
            promptLog.add(c.name + ": " + prompt);
            Integer a = scripted.poll();
            return (a != null && a >= 0 && a < options.length) ? a : policyIdx;
        }
        int timeout = timeoutOf(c);
        if (GameDriver.guiEnabled && GameDriver.mapViewer != null) {
            final int[] sel = {policyIdx};
            final boolean[] answered = {false};
            try {
                GameDriver.edtWait(() -> {
                    javax.swing.JOptionPane pane = new javax.swing.JOptionPane(prompt + (timeout > 0 ? "\n(default in " + timeout + "s: " + options[policyIdx] + ")" : ""),
                            javax.swing.JOptionPane.QUESTION_MESSAGE, javax.swing.JOptionPane.DEFAULT_OPTION, null, options,
                            options[Math.max(0, Math.min(options.length - 1, policyIdx))]);
                    javax.swing.JDialog d = pane.createDialog(GameDriver.mapViewer, "Decision — " + c.name);
                    javax.swing.Timer timer = null;
                    if (timeout > 0) {
                        timer = new javax.swing.Timer(timeout * 1000, e -> d.dispose());
                        timer.setRepeats(false);
                        timer.start();
                    }
                    d.setVisible(true); // modal: returns when answered, closed or timed out
                    if (timer != null) timer.stop();
                    Object v = pane.getValue();
                    for (int i = 0; i < options.length; i++) if (options[i].equals(v)) { sel[0] = i; answered[0] = true; }
                });
            } catch (Exception ignored) {}
            if (!answered[0] && timeout > 0) timeouts++;
            return sel[0];
        }
        System.out.println("*** DECISION (" + c.name + "): " + prompt);
        for (int i = 0; i < options.length; i++) System.out.println("  " + (i + 1) + ". " + options[i] + (i == policyIdx ? "  [default]" : ""));
        if (timeout > 0) System.out.println("  (no answer within " + timeout + "s picks the default)");
        boolean yesNo = options.length == 2 && options[0].equals("Yes") && options[1].equals("No");
        System.out.print(yesNo ? "  Answer y or n (any other answer = " + options[policyIdx].toLowerCase() + ") > " : "  Answer 1-" + options.length + " or the first letters of an option (any other answer = the default) > ");
        System.out.flush();
        String s;
        try {
            s = GameDriver.readInputTimed(timeout);
        } catch (Exception e) {
            System.out.println("No input available; using the default.");
            return policyIdx;
        }
        if (s == null) {
            timeouts++;
            System.out.println("No answer in " + timeout + "s — using the default: " + options[policyIdx] + ".");
            return policyIdx;
        }
        s = s.trim();
        try {
            int n = Integer.parseInt(s);
            if (n >= 1 && n <= options.length) return n - 1;
        } catch (NumberFormatException e) {
            // a word or letter: match the start of an option ("k" -> Kill, "s" -> Spare)
            for (int i = 0; i < options.length; i++) {
                if (!s.isEmpty() && options[i].toLowerCase().startsWith(s.toLowerCase())) return i;
            }
        }
        System.out.println("Not an option; using the default.");
        return policyIdx;
    }

    /** Asks a human-controlled character for a non-negative number (e.g. a maximum auction bid). A character that isn't promptable,
     *  gives no valid answer or times out gets `defaultValue`. */
    static synchronized long askNumber(Character c, String prompt, long defaultValue) {
        if (!promptable(c)) return defaultValue;
        prompts++;
        if (scriptedNumbers != null) {
            promptLog.add(c.name + ": " + prompt);
            Long a = scriptedNumbers.poll();
            return a != null ? Math.max(0, a) : defaultValue;
        }
        int timeout = timeoutOf(c);
        if (GameDriver.guiEnabled && GameDriver.mapViewer != null) {
            final String[] text = {null};
            try {
                GameDriver.edtWait(() -> {
                    javax.swing.JOptionPane pane = new javax.swing.JOptionPane(prompt, javax.swing.JOptionPane.QUESTION_MESSAGE, javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    pane.setWantsInput(true);
                    pane.setInitialSelectionValue(String.valueOf(defaultValue));
                    javax.swing.JDialog d = pane.createDialog(GameDriver.mapViewer, "Decision — " + c.name);
                    javax.swing.Timer timer = null;
                    if (timeout > 0) {
                        timer = new javax.swing.Timer(timeout * 1000, e -> d.dispose());
                        timer.setRepeats(false);
                        timer.start();
                    }
                    d.setVisible(true);
                    if (timer != null) timer.stop();
                    Object v = pane.getInputValue();
                    if (pane.getValue() instanceof Integer && (Integer) pane.getValue() == javax.swing.JOptionPane.OK_OPTION && v instanceof String) text[0] = (String) v;
                });
            } catch (Exception ignored) {}
            if (text[0] == null) { if (timeout > 0) timeouts++; return defaultValue; }
            try { return Math.max(0, Long.parseLong(text[0].trim())); } catch (NumberFormatException e) { return defaultValue; }
        }
        System.out.println("*** DECISION (" + c.name + "): " + prompt + "  [default " + defaultValue + "]");
        if (timeout > 0) System.out.println("  (no answer within " + timeout + "s picks the default)");
        String s;
        try {
            s = GameDriver.readInputTimed(timeout);
        } catch (Exception e) {
            return defaultValue;
        }
        if (s == null) {
            timeouts++;
            System.out.println("No answer in " + timeout + "s — using the default.");
            return defaultValue;
        }
        try { return Math.max(0, Long.parseLong(s.trim())); } catch (NumberFormatException e) { return defaultValue; }
    }
}
