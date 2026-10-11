package Game;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** The windows of the Story chapters: a scene (the Lore window headed "Story") and a scene with a question, where the game waits for the answer. */
final class GuiChapter {
    private GuiChapter() { }

    static void scene(String title, List<String> lines) { GuiLore.story(title, lines); }

    /** The scene with one button for each answer; closing the window (or the decision timeout) picks `dflt`. Runs on the game thread, which waits with the world unlocked. */
    static int decide(final Character c, final String title, final List<String> lines, final String prompt, final String[] options, final int dflt) {
        final int[] sel = {dflt};
        final int timeout = DecisionBroker.timeoutOf(c);
        DecisionBroker.prompts++;
        try {
            GameDriver.edtWait(() -> {
                final MapViewer mv = GameDriver.mapViewer;
                final JDialog d = new JDialog(mv, "Story  ·  " + title, true);
                d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                d.getContentPane().setBackground(Theme.BG);
                JEditorPane ep = GuiSheets.view(mv);
                ep.setText(GuiLore.sceneHtml(title, lines, null));
                ep.setCaretPosition(0);
                JScrollPane sp = new JScrollPane(ep);
                sp.setBorder(BorderFactory.createEmptyBorder());
                JPanel south = new JPanel(new GridLayout(0, 1, 0, 4));
                south.setBackground(Theme.BG);
                south.setBorder(BorderFactory.createEmptyBorder(6, 10, 10, 10));
                JLabel ask = new JLabel("<html><b>" + GuiSheets.esc(prompt) + "</b></html>");
                ask.setForeground(Theme.GOLD);
                south.add(ask);
                for (int i = 0; i < options.length; i++) {
                    final int pick = i;
                    south.add(mv.makeBtn(options[i], () -> { sel[0] = pick; d.dispose(); }));
                }
                d.add(sp, BorderLayout.CENTER);
                d.add(south, BorderLayout.SOUTH);
                d.setSize(700, 580);
                d.setLocationRelativeTo(mv);
                Ui.prepare(d);
                javax.swing.Timer timer = null;
                if (timeout > 0) { timer = new javax.swing.Timer(timeout * 1000, e -> d.dispose()); timer.setRepeats(false); timer.start(); }
                d.setVisible(true); // modal: returns when a button is pressed, the window is closed or the time is up
                if (timer != null) timer.stop();
            });
        } catch (Exception ignored) { }
        return sel[0];
    }
}
