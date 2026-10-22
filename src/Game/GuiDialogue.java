package Game;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.*;

/** The window of a talk: the speaker's portrait and name on the left, the words so far in the middle, the replies to choose from below (keys 1-9 pick one).
 *  It is built on the Swing thread; the game thread waits for each answer with the world released (GameDriver.blocked), so the rest of the game stands still while you talk.
 *  Also asks a decision (a friendship, a squad, a challenge, a sect's offer) in the speaker's own words. */
final class GuiDialogue implements Dialogue.Io {
    private final Character me;
    private Character npc;
    private JDialog dlg;
    private JEditorPane pane;
    private JPanel buttons;
    private final StringBuilder body = new StringBuilder();
    private final LinkedBlockingQueue<Integer> answers = new LinkedBlockingQueue<>();
    private volatile boolean closed = false;
    private javax.swing.Timer timer;

    GuiDialogue(Character me) { this.me = me; }

    private static String esc(String s) { return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"); }

    private static String relWord(int v) {
        switch (v) {
            case 3: return "a close friend"; case 2: return "a friend"; case 1: return "friendly with you";
            case -1: return "dislikes you"; case -2: return "a rival"; case -3: case -4: return "an arch-enemy";
            default: return "no ties to you yet";
        }
    }

    @Override public void open(Character n, Character who) {
        this.npc = n;
        try { GameDriver.edtWait(this::build); } catch (Exception e) { closed = true; }
    }

    private void build() {
        final MapViewer mv = GameDriver.mapViewer;
        dlg = new JDialog(mv, npc.name + " " + npc.lastName, false);
        dlg.setName("dialogue");
        dlg.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dlg.addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { closed = true; answers.offer(-1); dlg.dispose(); } });
        dlg.getContentPane().setBackground(Theme.BG);
        dlg.setLayout(new BorderLayout(8, 8));

        // the speaker
        JPanel who = new JPanel();
        who.setLayout(new BoxLayout(who, BoxLayout.Y_AXIS));
        who.setBackground(Theme.PANEL);
        who.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        if (Settings.pictures) {
            JLabel pic = new JLabel(new ImageIcon(ArtMore.portraitCached(npc, 150, 188)));
            pic.setAlignmentX(Component.CENTER_ALIGNMENT);
            pic.setBorder(BorderFactory.createLineBorder(Theme.LINE));
            who.add(pic);
            who.add(Box.createVerticalStrut(8));
        }
        Relationship r = npc.relationshipList.contains(me.id);
        who.add(label(npc.name + " " + npc.lastName, Theme.bold(17), Theme.TEXT));
        who.add(label("Lv." + npc.lvl + " · Major " + npc.majorLvl + " · " + npc.getTrait(), Theme.plain(14), Theme.DIM));
        who.add(label(npc.affiliation + (npc.sect != null ? " · " + npc.sect.name : ""), Theme.plain(14), Theme.DIM));
        who.add(label(relWord(r == null ? 0 : r.value), Theme.plain(14), Theme.ACCENT));
        dlg.add(who, BorderLayout.WEST);

        // the words
        pane = GuiSheets.view(GameDriver.mapViewer);
        JScrollPane sp = new JScrollPane(pane);
        sp.setBorder(BorderFactory.createEmptyBorder());
        dlg.add(sp, BorderLayout.CENTER);

        // the replies
        buttons = new JPanel(new GridLayout(0, 1, 4, 4));
        buttons.setBackground(Theme.BG);
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        dlg.add(buttons, BorderLayout.SOUTH);
        for (int i = 1; i <= 9; i++) {
            final int n = i;
            dlg.getRootPane().registerKeyboardAction(e -> { if (buttons.getComponentCount() >= n && buttons.getComponent(n - 1) instanceof JButton) ((JButton) buttons.getComponent(n - 1)).doClick(); },
                    KeyStroke.getKeyStroke(KeyEvent.VK_0 + i, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        }
        dlg.setSize(800, 600);
        dlg.setLocationRelativeTo(mv);
        GuiHub.restoreBounds(dlg, "dialogue"); GuiHub.trackBounds(dlg, "dialogue");
        Ui.prepare(dlg);
        refresh();
        dlg.setVisible(true);
    }

    private static JLabel label(String text, Font f, Color c) {
        JLabel l = new JLabel("<html><div style='width:150px'>" + esc(text) + "</div></html>");
        l.setFont(f); l.setForeground(c); l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private void refresh() {
        if (pane == null) return;
        pane.setText(GuiSheets.page("<div style='padding:6px'>" + body + "</div>"));
        SwingUtilities.invokeLater(() -> pane.setCaretPosition(pane.getDocument().getLength()));
    }

    private void append(String htmlPiece) {
        body.append(htmlPiece);
        if (closed) return;
        SwingUtilities.invokeLater(this::refresh);
    }

    @Override public void npc(String text) {
        append("<p><b><font color='" + Theme.hex(Theme.GOLD) + "'>" + esc(npc.name) + "</font></b> &nbsp;&ldquo;" + esc(text) + "&rdquo;</p>");
    }

    @Override public void narrate(String text) {
        append("<p><i><font color='" + Theme.hex(Theme.DIM) + "'>" + esc(text) + "</font></i></p>");
    }

    /** A closed window answers with the last reply (the way out). */
    @Override public int pick(String[] labels, int[] temper) { int p = rawPick(labels, 0, labels.length - 1); return p < 0 ? labels.length - 1 : p; }

    /** The replies as buttons; waits for one. `timeoutSec` > 0: the policy's answer is taken when it runs out. -1 = the window was closed. */
    private int rawPick(String[] labels, int timeoutSec, int policyIdx) {
        if (closed) return -1;
        answers.clear();
        SwingUtilities.invokeLater(() -> {
            if (dlg == null) return;
            buttons.removeAll();
            buttons.setLayout(new GridLayout(0, labels.length > 5 ? 2 : 1, 4, 4)); // many replies go in two columns, so the speaker's card above is never squeezed
            for (int i = 0; i < labels.length; i++) {
                final int idx = i;
                JButton b = new JButton((i + 1) + ".  " + labels[i]);
                b.setHorizontalAlignment(SwingConstants.LEFT);
                b.setFont(Theme.plain(16));
                b.addActionListener(e -> { stopTimer(); answers.offer(idx); });
                buttons.add(b);
            }
            buttons.revalidate(); buttons.repaint();
            if (buttons.getComponentCount() > 0) buttons.getComponent(0).requestFocusInWindow();
            Ui.prepare(dlg);
            stopTimer();
            if (timeoutSec > 0) { timer = new javax.swing.Timer(timeoutSec * 1000, e -> { answers.offer(policyIdx); }); timer.setRepeats(false); timer.start(); }
        });
        Integer a;
        try { a = GameDriver.blocked(() -> answers.take()); } catch (RuntimeException e) { a = -1; }
        stopTimer();
        if (a == null || a < 0 || a >= labels.length) return -1;
        append("<p><font color='" + Theme.hex(Theme.ACCENT) + "'>&rsaquo; " + esc(labels[a]) + "</font></p>");
        return a;
    }

    private void stopTimer() { if (timer != null) { timer.stop(); timer = null; } }

    /** The talk is over: the last words stay up until Continue is pressed. */
    @Override public void close() {
        if (!closed) rawPick(new String[]{"Continue"}, 0, 0);
        closeNow();
    }

    void closeNow() {
        closed = true;
        SwingUtilities.invokeLater(() -> { stopTimer(); if (dlg != null) dlg.dispose(); });
    }

    // ------------------------------------------------------------------ a decision in the speaker's words
    /** A question from `speaker` to the human `me` (a friendship, a squad, a challenge...): the speaker's line, the question and the answers. Returns the chosen index. */
    static int decide(Character me, Character speaker, String line, String prompt, String[] options, int policyIdx, int timeoutSec) {
        GuiDialogue g = new GuiDialogue(me);
        g.open(speaker, me);
        if (line != null && !line.isEmpty()) g.npc(line);
        g.narrate(prompt);
        int p = g.rawPick(options, timeoutSec, policyIdx);
        g.closeNow();
        return p < 0 ? policyIdx : p; // closing the window answers as the policy would
    }
}
