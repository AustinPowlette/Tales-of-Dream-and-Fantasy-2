package Game;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** The "how does this life begin" choice: a dialog in GUI mode, numbered prompts in the console. */
public final class StartMenu {
    private StartMenu() { }

    static final String[] KIND_TITLES = {"Protagonist", "Standard (fate rolls you)", "Standard (custom: choose everything)", "Story"};
    static final String[] KIND_BLURBS = {
            "The perfect cultivator: potential 100, an Alchemist. First realms fly by; the top realm is still thousands of years away.",
            "A body drawn from the same lottery as everyone else (most people are mediocre). If the draw is a dud (potential under 20), fate rerolls once. Your talent is only a rumour (a word) until your first breakthrough.",
            "A sandbox: pick your exact potential, trait, alignment, gender and name. Nothing is hidden.",
            "One of ten characters with a past and a chain of objectives. Choose a difficulty: Story, Hard or Brutal."};

    static final int REC_MIN = 10000, REC_MAX = 150000, HARD_MIN = 1000, HARD_MAX = 1000000;

    /** A sentence about a size outside the recommended range ("" when it is inside). */
    static String populationWarning(int n) {
        if (n < REC_MIN) return " Note: under " + String.format("%,d", REC_MIN) + " people the world is thin (few cities, sects and rivals) and the story may feel empty.";
        if (n > REC_MAX) return " Warning: over " + String.format("%,d", REC_MAX) + " people the turns slow down and the world needs a lot of memory (several GB after a hundred years).";
        return "";
    }

    /** "How many people at the start?" for a NEW world (default 100,000; 10,000 to 150,000 recommended; outside it the choice is warned about and still accepted).
     *  Not asked in a test copy that edits Map.startingPopulation, or with -Dtdf.startpop=N (which sets it). */
    static void askPopulation() {
        Integer fixed = Integer.getInteger("tdf.startpop");
        if (fixed != null) { Map.setStartingPopulation(Math.max(HARD_MIN, Math.min(HARD_MAX, fixed))); return; }
        if (Map.startingPopulation != Map.DEFAULT_START) return; // a probe copy with its own size
        final int def = Math.max(HARD_MIN, Math.min(HARD_MAX, Settings.startPopulation));
        int n = def;
        if (GameDriver.guiEnabled && GameDriver.mapViewer != null) {
            final int[] out = {def};
            try {
                GameDriver.edtWait(() -> {
                    JSpinner sp = new JSpinner(new SpinnerNumberModel(def, HARD_MIN, HARD_MAX, 5000));
                    sp.setPreferredSize(new Dimension(160, 32));
                    JLabel warn = new JLabel(" ");
                    warn.setPreferredSize(new Dimension(360, 56)); // room for the warning from the start: the dialog does not have to grow when it appears
                    Runnable upd = () -> {
                        int v = ((Number) sp.getValue()).intValue();
                        String w = populationWarning(v).trim();
                        warn.setText("<html><div style='width:340px;color:#b04a3a'>" + (w.isEmpty() ? "&nbsp;" : w) + "</div></html>");
                    };
                    sp.addChangeListener(e -> upd.run());
                    upd.run();
                    JPanel spin = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)); spin.add(sp);
                    JPanel p = new JPanel(new BorderLayout(0, 8));
                    p.add(new JLabel("<html><div style='width:340px'>How many people live in the world at the start?<br>Recommended: " + String.format("%,d", REC_MIN) + " to " + String.format("%,d", REC_MAX)
                            + ". A smaller world runs faster on a weaker computer and needs less memory; " + String.format("%,d", Map.DEFAULT_START) + " is the usual world.</div></html>"), BorderLayout.NORTH);
                    p.add(spin, BorderLayout.CENTER);
                    p.add(warn, BorderLayout.SOUTH);
                    JOptionPane.showOptionDialog(GameDriver.mapViewer, p, "Size of the world", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"Start"}, "Start");
                    try { sp.commitEdit(); } catch (java.text.ParseException ignored) { }
                    out[0] = ((Number) sp.getValue()).intValue();
                });
            } catch (Exception ignored) { }
            n = out[0];
        } else {
            System.out.println("Starting population: how many people live in the world at the start? (" + String.format("%,d", REC_MIN) + " to " + String.format("%,d", REC_MAX) + " recommended; a smaller world suits a weaker computer; default "
                    + String.format("%,d", def) + ") - type a number, or any other word for the default:");
            try { n = Integer.parseInt(GameDriver.scan.next().replace(",", "").replace("_", "").trim()); } catch (Exception e) { n = def; }
        }
        n = Math.max(HARD_MIN, Math.min(HARD_MAX, n));
        String w = populationWarning(n);
        if (!w.isEmpty()) System.out.println("[World size]" + w);
        System.out.println("The world begins with " + String.format("%,d", n) + " people.");
        Settings.startPopulation = n;
        try { Settings.save(); } catch (RuntimeException ignored) { }
        Map.setStartingPopulation(n);
    }

    /** "How many players?": 'S' single, 'M' multiplayer, 'W' watch. A dialog in GUI mode, a console prompt otherwise. */
    static char askMode() {
        if (GameDriver.guiEnabled && GameDriver.mapViewer != null) {
            final int[] sel = {0};
            try {
                GameDriver.edtWait(() -> sel[0] = JOptionPane.showOptionDialog(GameDriver.mapViewer, "How do you want to play?", "Tales of Dream and Fantasy", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                        new String[]{"Single player", "Multiplayer (2-4 players)", "Watch the world"}, "Single player"));
            } catch (Exception ignored) { }
            return sel[0] == 1 ? 'M' : sel[0] == 2 ? 'W' : 'S';
        }
        System.out.println("Mode: (S)ingle Player | (M)ultiplayer | (W)atch / Spectate");
        return GameDriver.scan.next().charAt(0);
    }

    /** "How many players? (2-4)". */
    static int askPlayerCount() {
        if (GameDriver.guiEnabled && GameDriver.mapViewer != null) {
            final int[] sel = {0};
            try { GameDriver.edtWait(() -> sel[0] = JOptionPane.showOptionDialog(GameDriver.mapViewer, "How many players?", "Multiplayer", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"2", "3", "4"}, "2")); } catch (Exception ignored) { }
            return 2 + Math.max(0, sel[0]);
        }
        System.out.println("How many players? (2-4)");
        try { return Math.min(4, Math.max(2, Integer.parseInt(GameDriver.scan.next()))); } catch (Exception e) { return 2; }
    }

    /** A yes/no question (GUI: dialog, console: Y/n). */
    static boolean askYesNo(String title, String text, boolean def) {
        if (GameDriver.guiEnabled && GameDriver.mapViewer != null) {
            final int[] sel = {def ? 0 : 1};
            try { GameDriver.edtWait(() -> sel[0] = JOptionPane.showOptionDialog(GameDriver.mapViewer, text, title, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"Yes", "No"}, def ? "Yes" : "No")); } catch (Exception ignored) { }
            return sel[0] == 0;
        }
        System.out.println(text + " Y/n");
        char c = GameDriver.scan.next().charAt(0);
        return c == 'Y' || c == 'y';
    }

    static Starts.Choice choose(int playerIndex, int playerCount) {
        if (GameDriver.guiEnabled && GameDriver.mapViewer != null) {
            final Starts.Choice[] out = {new Starts.Choice()};
            try { GameDriver.edtWait(() -> out[0] = dialog(playerIndex, playerCount)); } catch (Exception e) { return new Starts.Choice(); }
            return out[0];
        }
        return console(playerIndex, playerCount);
    }

    // ------------------------------------------------------------------ console
    private static int ask(String prompt, int lo, int hi, int def) {
        System.out.println(prompt);
        try { int n = Integer.parseInt(GameDriver.scan.next().trim()); if (n >= lo && n <= hi) return n; } catch (Exception ignored) { }
        return def;
    }

    private static Starts.Choice console(int playerIndex, int playerCount) {
        Starts.Choice ch = new Starts.Choice();
        System.out.println("\nHow does " + (playerCount > 1 ? "player " + (playerIndex + 1) + "'s" : "your") + " life begin?");
        for (int i = 0; i < KIND_TITLES.length; i++) System.out.println("  " + (i + 1) + ") " + KIND_TITLES[i] + "\n       " + KIND_BLURBS[i]);
        int k = ask("Choose 1-4 (default 1):", 1, 4, 1);
        ch.kind = Starts.Kind.values()[k - 1];
        if (ch.kind == Starts.Kind.STORY) {
            List<Starts.Story> list = new ArrayList<>(Starts.STORIES.values());
            for (int i = 0; i < list.size(); i++) System.out.println("  " + (i + 1) + ") " + list.get(i).title + " (potential " + list.get(i).potential + ")\n       " + list.get(i).blurb);
            int s = ask("Choose a story 1-" + list.size() + " (default 1):", 1, list.size(), 1);
            ch.story = list.get(s - 1).id;
            for (int i = 0; i < 3; i++) System.out.println("  " + (i + 1) + ") " + Starts.DIFFICULTY_NAMES[i] + ": " + Starts.DIFFICULTY_BLURB[i]);
            ch.difficulty = ask("Difficulty 1-3 (default 1):", 1, 3, 1) - 1;
        }
        return ch;
    }

    // ------------------------------------------------------------------ GUI
    private static Starts.Choice dialog(int playerIndex, int playerCount) {
        final Starts.Choice ch = new Starts.Choice();
        JDialog d = new JDialog(GameDriver.mapViewer, "A new life" + (playerCount > 1 ? " — player " + (playerIndex + 1) : ""), true);
        d.setLayout(new BorderLayout(8, 8));
        ((JComponent) d.getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JLabel head = new JLabel("How does this life begin?");
        head.setFont(Theme.serif(20)); head.setForeground(Theme.GOLD);
        d.add(head, BorderLayout.NORTH);

        JRadioButton[] kinds = new JRadioButton[KIND_TITLES.length];
        ButtonGroup bg = new ButtonGroup();
        JPanel left = new JPanel(new GridLayout(0, 1, 4, 4));
        for (int i = 0; i < kinds.length; i++) { kinds[i] = new JRadioButton(KIND_TITLES[i]); kinds[i].setFont(Theme.bold(16)); bg.add(kinds[i]); left.add(kinds[i]); }
        kinds[0].setSelected(true);

        List<Starts.Story> stories = new ArrayList<>(Starts.STORIES.values());
        DefaultListModel<String> model = new DefaultListModel<>();
        for (Starts.Story s : stories) model.addElement(s.title + "   (potential " + s.potential + ")");
        JList<String> list = new JList<>(model);
        list.setSelectedIndex(0);
        list.setFont(Theme.plain(15));
        JScrollPane listScroll = new JScrollPane(list);
        listScroll.setPreferredSize(new Dimension(380, 220));
        JComboBox<String> diff = new JComboBox<>(Starts.DIFFICULTY_NAMES);
        JTextArea blurb = new JTextArea(6, 46);
        blurb.setEditable(false); blurb.setLineWrap(true); blurb.setWrapStyleWord(true); blurb.setFont(Theme.plain(15));
        JTextArea diffBlurb = new JTextArea(3, 46);
        diffBlurb.setEditable(false); diffBlurb.setLineWrap(true); diffBlurb.setWrapStyleWord(true); diffBlurb.setFont(Theme.plain(14)); diffBlurb.setForeground(Theme.DIM);

        JPanel storyBox = new JPanel(new BorderLayout(6, 6));
        storyBox.add(listScroll, BorderLayout.CENTER);
        JPanel dpan = new JPanel(new BorderLayout(6, 4));
        JPanel drow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        drow.add(new JLabel("Difficulty")); drow.add(diff);
        dpan.add(drow, BorderLayout.NORTH); dpan.add(diffBlurb, BorderLayout.CENTER);
        storyBox.add(dpan, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(left, BorderLayout.WEST);
        JPanel right = new JPanel(new BorderLayout(6, 6));
        right.add(new JScrollPane(blurb), BorderLayout.NORTH);
        right.add(storyBox, BorderLayout.CENTER);
        center.add(right, BorderLayout.CENTER);
        d.add(center, BorderLayout.CENTER);

        Runnable refresh = () -> {
            int k = 0; for (int i = 0; i < kinds.length; i++) if (kinds[i].isSelected()) k = i;
            boolean story = k == 3;
            list.setEnabled(story); diff.setEnabled(story);
            storyBox.setVisible(story);
            int si = Math.max(0, list.getSelectedIndex());
            blurb.setText(story ? stories.get(si).title + "\n\n" + stories.get(si).blurb + "\n\nPotential " + stories.get(si).potential + (stories.get(si).potentialPills > 0 ? ", with " + stories.get(si).potentialPills + " Potential Pill(s)" : "")
                    + ", " + stories.get(si).steps.size() + " objectives." : KIND_BLURBS[k]);
            diffBlurb.setText(Starts.DIFFICULTY_BLURB[diff.getSelectedIndex()]);
            blurb.setCaretPosition(0);
            d.pack();
        };
        for (JRadioButton b : kinds) b.addActionListener(e -> refresh.run());
        list.addListSelectionListener(e -> refresh.run());
        diff.addActionListener(e -> refresh.run());

        JButton ok = new JButton("Begin");
        ok.addActionListener(e -> {
            int k = 0; for (int i = 0; i < kinds.length; i++) if (kinds[i].isSelected()) k = i;
            ch.kind = Starts.Kind.values()[k];
            if (ch.kind == Starts.Kind.STORY) { ch.story = stories.get(Math.max(0, list.getSelectedIndex())).id; ch.difficulty = diff.getSelectedIndex(); }
            d.dispose();
        });
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(ok);
        d.add(south, BorderLayout.SOUTH);
        d.getRootPane().setDefaultButton(ok);
        refresh.run();
        d.setLocationRelativeTo(GameDriver.mapViewer);
        Ui.prepare(d);
        d.setVisible(true);
        return ch;
    }
}
