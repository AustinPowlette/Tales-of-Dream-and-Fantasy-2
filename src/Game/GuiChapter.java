package Game;

import java.util.List;

/** The windows of the Story chapters: a scene (the Lore window headed "Story") and a scene with a question, where the game waits for the answer. Both are pages of the one scene window (GuiScenes). */
final class GuiChapter {
    private GuiChapter() { }

    static void scene(String title, List<String> lines) { GuiLore.story(title, lines); }

    /** The scene with one button for each answer, on its last page; closing the window (or the decision timeout) picks `dflt`. Runs on the game thread, which waits with the world unlocked.
     *  What follows the answer is given to `afterChoice` and appears as more pages of the same window. */
    static int decide(final Character c, final String title, final List<String> lines, final String prompt, final String[] options, final int dflt) {
        return GuiScenes.ask(c, "Story", title, lines, prompt, options, dflt);
    }

    /** The text that follows the answer just given (null or empty: none): more pages of the window that asked. */
    static void afterChoice(List<String> lines) { GuiScenes.supplyAfter(lines); }
}
