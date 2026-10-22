package Game;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** The Edit Character dialog: one window for the name, the trait (a searchable list with what each trait does), the potential, the alignment and the gender,
 *  with the portrait following the choices. It replaces the chain of option pop-ups whose trait row of 24 buttons ran off the side of the screen.
 *  Called on the Swing thread (GameDriver.editCharacterInteractive waits for it); OK writes the choices into the character, Cancel leaves it as it was. */
final class GuiEditSelf {
    private GuiEditSelf() { }

    /** {trait, group, what it does}. The two beast traits are not playable and are not listed. */
    static final String[][] INFO = {
        {"Leader", "society", "Founds cities and sects and leads larger squads. Choose it if you want to build something that outlives you."},
        {"Merchant", "society", "Runs shops and trades between cities, and earns from the gap between what a city pays and what it charges."},
        {"Friendly", "society", "Makes friends easily: bonds grow faster and strangers are less likely to turn on you."},
        {"Alchemist", "craft", "Refines pills from herbs: pills for cultivation and pills for potential. Needs herbs, and a Gatherer finds more of them."},
        {"Blacksmith", "craft", "Forges weapons from metal. A Miner finds more metal."},
        {"Tailor", "craft", "Sews armour and clothing."},
        {"Scribe", "craft", "Draws arrays, the formations that guard cities, sects and shops."},
        {"Innovative", "craft", "Gains Inspiration from fights. A hundred of it makes a new technique of your own. Can enchant weapons and sell techniques to a library."},
        {"Gatherer", "gathering", "Finds more herbs when foraging. Feeds Alchemists, Chefs and brewers."},
        {"Miner", "gathering", "Digs more metal out of a tile. Feeds Blacksmiths."},
        {"Runner", "combat", "Escapes fights: very hard to catch, and good at crossing the world fast."},
        {"IronBody", "combat", "Natural armour. Adds half your major level plus one to your fighting level."},
        {"HeavenlyRoot", "combat", "Techniques level up faster, and a kill teaches you something twice as often."},
        {"SpiritVein", "combat", "Cultivates one and a half times as fast, in everything you do."},
        {"Lucky", "fortune", "Plus 45 to the chance of winning a fight, extra cultivation and more loot from the dead."},
        {"Ghost", "fortune", "Death does not end you once. You become a ghost, walk to a city and are born again at 18. Your things are left behind and you leave your sect."},
        {"Cheater", "fortune", "A small bonus to fighting level, the Cheats window, and the only trait that may change its potential in a running game. Almost never born."},
        {"Tamer", "life arts", "Tames wild beasts, and keeps as many as your major level plus one. They follow you and fight beside you."},
        {"Chef", "life arts", "Cooks food that boosts cultivation for twice as long, and cures poison."},
        {"Drunken", "life arts", "Brews wine. Drink makes a Drunken stronger instead of weaker, and gives a free turn of cultivation now and then."},
        {"Poisoner", "life arts", "Mixes poison, coats blades with it and poisons people. Resists poison well."},
        {"Musician", "life arts", "Makes and plays instruments: extra cultivation, everyone listening is inspired, and a city pays tips."},
    };

    static String[] info(Character.Traits t) {
        for (String[] r : INFO) if (r[0].equals(t.name())) return r;
        return new String[]{t.name(), "", "A gift of the world."};
    }

    private static final class Row {
        final Character.Traits trait; final String group, text, label;
        Row(Character.Traits t) { trait = t; String[] r = info(t); group = r[1]; text = r[2]; label = Character.traitName(t); }
        boolean matches(String q) { return q.isEmpty() || (label + " " + group + " " + text).toLowerCase().contains(q); }
    }

    /** @param creation the editor offered while the character is being made: the potential may be set then; in a running game only a Cheater may change it. */
    static void show(MapViewer owner, Character c, boolean creation) {
        final String name0 = c.name, last0 = c.lastName, aff0 = c.affiliation, gen0 = c.gender;
        final Character.Traits trait0 = c.trait;
        final int pot0 = c.potential, skin0 = c.skinTone;

        JDialog d = new JDialog(owner, "Edit Character", true);
        d.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        d.getContentPane().setBackground(Theme.PANEL);
        d.setLayout(new BorderLayout(0, 0));

        // ---- left: who you are
        JTextField first = new JTextField(c.name, 14), last = new JTextField(c.lastName, 14);
        JRadioButton[] aff = new JRadioButton[3];
        String[] affNames = {"Good", "Neutral", "Evil"};
        ButtonGroup ag = new ButtonGroup();
        for (int i = 0; i < 3; i++) { aff[i] = radio(affNames[i], affNames[i].equalsIgnoreCase(c.affiliation)); ag.add(aff[i]); }
        JRadioButton male = radio("Male", "Male".equalsIgnoreCase(c.gender)), female = radio("Female", "Female".equalsIgnoreCase(c.gender));
        ButtonGroup gg = new ButtonGroup(); gg.add(male); gg.add(female);
        JSpinner pot = new JSpinner(new SpinnerNumberModel(Math.max(1, Math.min(100, c.potential)), 1, 100, 1));
        JLabel potNote = small("");
        final JComboBox<String> pathBox = new JComboBox<>(Elements.NAMES);
        pathBox.setSelectedIndex(Elements.of(c));
        pathBox.setEnabled(creation);
        final short[] pendingPath = {c.path};
        final JLabel pathNote = small(creation ? Elements.pathName(c) : Elements.pathName(c) + " (fixed for life)");
        final JCheckBox rName = check("Name", true), rSkin = check("Skin", true), rTrait = check("Trait", true), rPot = check("Potential", true), rGender = check("Gender", false), rAlign = check("Alignment", true), rPath = check("Path", true);
        rPath.setEnabled(creation); if (!creation) rPath.setSelected(false);
        final JButton randomize = new JButton("Randomize");
        randomize.setToolTipText("Fill the ticked fields with random choices (the name from the world's name engine, the potential from the NPC curve). Done keeps them, Cancel puts everything back.");
        JLabel portrait = new JLabel();
        portrait.setPreferredSize(new Dimension(150, 188));
        portrait.setHorizontalAlignment(SwingConstants.CENTER);
        portrait.setBorder(BorderFactory.createLineBorder(Theme.LINE));

        JPanel left = new JPanel(); left.setBackground(Theme.PANEL);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(new EmptyBorder(10, 12, 10, 8));
        left.add(head("NAME")); left.add(labelled("First", first)); left.add(labelled("Last", last));
        left.add(Box.createVerticalStrut(8));
        left.add(head("ALIGNMENT")); left.add(row(aff[0], aff[1], aff[2]));
        left.add(Box.createVerticalStrut(4));
        left.add(head("GENDER")); left.add(row(male, female));
        left.add(Box.createVerticalStrut(4));
        final JPanel skinPanel = skinRow(c, () -> repaintPortrait(portrait, c, male.isSelected() ? "Male" : "Female"));
        left.add(head("SKIN")); left.add(skinPanel);
        left.add(Box.createVerticalStrut(4));
        left.add(portrait); // directly below the skin tones: the portrait follows them
        left.add(Box.createVerticalStrut(8));
        left.add(head("POTENTIAL (1-100)")); left.add(row(pot)); left.add(potNote);
        left.add(Box.createVerticalStrut(4));
        left.add(head("PATH (THE ELEMENT YOU CULTIVATE)")); left.add(row(pathBox)); left.add(pathNote);
        left.add(Box.createVerticalStrut(6));
        left.add(head("RANDOMIZE")); left.add(row(rName, rSkin, rTrait)); left.add(row(rPot, rGender, rAlign, rPath)); left.add(row(randomize));
        for (Component k : left.getComponents()) if (k instanceof JComponent) ((JComponent) k).setAlignmentX(Component.LEFT_ALIGNMENT);

        // ---- centre: the trait list and what the chosen trait does
        List<Row> all = new ArrayList<>();
        for (Character.Traits t : Character.Traits.values()) if (t != Character.Traits.Monster && t != Character.Traits.Beast) all.add(new Row(t));
        DefaultListModel<Row> model = new DefaultListModel<>();
        JList<Row> list = new JList<>(model);
        list.setBackground(Theme.FIELD);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(30);
        list.setCellRenderer((l, r, i, sel, foc) -> {
            JLabel lb = new JLabel("<html><b>" + r.label + "</b> <font color='" + Theme.hex(Theme.DIM) + "'>&nbsp;" + r.group + "</font></html>");
            lb.setOpaque(true); lb.setBorder(new EmptyBorder(0, 10, 0, 6));
            lb.setBackground(sel ? Theme.SELECT : Theme.FIELD); lb.setForeground(Theme.TEXT); lb.setFont(Theme.plain(14));
            return lb;
        });
        JTextField search = new JTextField(12);
        search.putClientProperty("JTextField.placeholderText", "search the traits…");
        JEditorPane desc = new JEditorPane();
        desc.setContentType("text/html");
        desc.setEditable(false); desc.setOpaque(false); desc.setFocusable(false);
        JPanel descBox = new JPanel(new BorderLayout()); descBox.setBackground(Theme.PANEL2);
        descBox.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.LINE), new EmptyBorder(12, 14, 12, 14)));
        descBox.add(desc, BorderLayout.CENTER);
        descBox.setPreferredSize(new Dimension(300, 200));

        final Character.Traits[] chosen = {c.trait};
        Runnable refreshDesc = () -> {
            Character.Traits t = chosen[0];
            String[] r = info(t);
            desc.setText("<html><body style='font-family:" + Theme.UI_FAMILY + ";font-size:14px;color:" + Theme.hex(Theme.TEXT) + "'><span style='font-size:20px'><b>" + GuiSheets.esc(Character.traitName(t)) + "</b></span><br>"
                    + "<font color='" + Theme.hex(Theme.ACCENT) + "'>" + GuiSheets.esc(r[1].toUpperCase()) + "</font><br><br>" + GuiSheets.esc(r[2]) + "</body></html>");
            boolean canPot = creation || t == Character.Traits.Cheater;
            pot.setEnabled(canPot);
            rPot.setEnabled(canPot); if (!canPot) rPot.setSelected(false);
            potNote.setText(canPot ? (creation ? "Free while you are making the character." : "A Cheater may change it.") : "Locked: only a Cheater can change it in a running game.");
            c.trait = t; // the portrait follows the choices (restored on Cancel)
            repaintPortrait(portrait, c, male.isSelected() ? "Male" : "Female");
        };
        Runnable refill = () -> {
            String q = search.getText().trim().toLowerCase();
            model.clear();
            for (Row r : all) if (r.matches(q)) model.addElement(r);
            for (int i = 0; i < model.size(); i++) if (model.get(i).trait == chosen[0]) { list.setSelectedIndex(i); break; }
        };
        list.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            Row r = list.getSelectedValue();
            if (r != null) { chosen[0] = r.trait; refreshDesc.run(); }
        });
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { refill.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { refill.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { refill.run(); }
        });
        randomize.addActionListener(e -> {
            if (rGender.isSelected()) (EditRandom.female() ? female : male).setSelected(true);
            if (rName.isSelected()) { String[] n = EditRandom.name(female.isSelected(), c.location); first.setText(n[0]); last.setText(n[1]); }
            if (rSkin.isSelected()) { int tone = EditRandom.skin(); c.skinTone = tone; if (tone < skinPanel.getComponentCount()) ((JToggleButton) skinPanel.getComponent(tone)).setSelected(true); }
            if (rAlign.isSelected()) { String a = EditRandom.alignment(); for (int i = 0; i < 3; i++) aff[i].setSelected(affNames[i].equals(a)); }
            if (rTrait.isSelected()) { chosen[0] = EditRandom.trait(); search.setText(""); refill.run(); }
            if (rPot.isSelected() && pot.isEnabled()) pot.setValue(EditRandom.potential());
            if (rPath.isSelected() && pathBox.isEnabled()) { pendingPath[0] = Elements.roll(-1, GameDriver.rand); pathBox.setSelectedIndex((pendingPath[0] - 1) / 196); pathNote.setText(pathWords(pendingPath[0])); }
            refreshDesc.run();
            repaintPortrait(portrait, c, male.isSelected() ? "Male" : "Female");
        });
        pathBox.addActionListener(e -> { if (pathBox.isEnabled() && (pendingPath[0] - 1) / 196 != pathBox.getSelectedIndex()) { pendingPath[0] = Elements.roll(pathBox.getSelectedIndex(), GameDriver.rand); pathNote.setText(pathWords(pendingPath[0])); } });
        male.addActionListener(e -> repaintPortrait(portrait, c, "Male"));
        female.addActionListener(e -> repaintPortrait(portrait, c, "Female"));

        JPanel listBox = new JPanel(new BorderLayout(0, 4)); listBox.setBackground(Theme.PANEL);
        JPanel top = new JPanel(new BorderLayout(6, 0)); top.setBackground(Theme.PANEL);
        top.add(head("TRAIT"), BorderLayout.WEST); top.add(search, BorderLayout.CENTER);
        listBox.add(top, BorderLayout.NORTH);
        JScrollPane lsp = new JScrollPane(list); lsp.setPreferredSize(new Dimension(230, 300));
        listBox.add(lsp, BorderLayout.CENTER);
        JPanel centre = new JPanel(new BorderLayout(10, 0)); centre.setBackground(Theme.PANEL);
        centre.setBorder(new EmptyBorder(10, 8, 10, 12));
        centre.add(listBox, BorderLayout.WEST); centre.add(descBox, BorderLayout.CENTER);

        // ---- bottom: the buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8)); bottom.setBackground(Theme.PANEL2);
        JButton ok = new JButton("Done"), cancel = new JButton("Cancel");
        ok.setFont(Theme.bold(14)); cancel.setFont(Theme.plain(14));
        bottom.add(cancel); bottom.add(ok);
        final boolean[] saved = {false};
        ok.addActionListener(e -> {
            String fn = first.getText().trim(), ln = last.getText().trim();
            c.name = fn.isEmpty() ? name0 : fn; c.lastName = ln.isEmpty() ? last0 : ln;
            c.trait = chosen[0];
            c.affiliation = aff[0].isSelected() ? "Good" : aff[2].isSelected() ? "Evil" : "Neutral";
            c.gender = male.isSelected() ? "Male" : "Female";
            if (pot.isEnabled()) c.potential = Math.min(100, Math.max(1, ((Number) pot.getValue()).intValue()));
            if (creation && pathBox.isEnabled()) c.path = pendingPath[0];
            saved[0] = true;
            d.dispose();
        });
        cancel.addActionListener(e -> d.dispose());
        d.addWindowListener(new java.awt.event.WindowAdapter() { @Override public void windowClosing(java.awt.event.WindowEvent e) { d.dispose(); } });
        d.getRootPane().setDefaultButton(ok);
        d.getRootPane().registerKeyboardAction(e -> d.dispose(), KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);

        JLabel title = new JLabel("  Edit " + name0 + " " + last0);
        title.setFont(Theme.serif(20)); title.setForeground(Theme.GOLD); title.setBorder(new EmptyBorder(8, 8, 4, 8));
        d.add(title, BorderLayout.NORTH);
        d.add(left, BorderLayout.WEST);
        d.add(centre, BorderLayout.CENTER);
        d.add(bottom, BorderLayout.SOUTH);
        refill.run();
        refreshDesc.run();
        d.pack(); // as tall as the left column needs (name to Randomize), at least 960 x 700, never bigger than the screen
        Rectangle scr = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        d.setSize(Math.min(scr.width - 40, Math.max(960, d.getWidth())), Math.min(scr.height - 40, Math.max(700, d.getHeight())));
        d.setLocationRelativeTo(owner);
        Ui.prepare(d);
        d.setVisible(true); // modal: returns when the dialog is closed
        if (!saved[0]) { c.name = name0; c.lastName = last0; c.trait = trait0; c.affiliation = aff0; c.gender = gen0; c.potential = pot0; c.skinTone = skin0; }
    }

    /** The words of a path number (for the note under the element box). */
    private static String pathWords(short code) {

        int k = code - 1, noun = k % 7, adj = (k / 7) % 7, prefix = (k / 49) % 4, e = k / 196;
        return Elements.words(e, prefix, adj, noun);
    }

    // ------------------------------------------------------------------ small pieces
    /** Swatches for the skin tone: the first (a seed mark) leaves it to the portrait, then nine tones from light to dark; the portrait follows at once (restored on Cancel). */
    private static JPanel skinRow(Character c, Runnable repaint) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2)); p.setBackground(Theme.PANEL);
        ButtonGroup g = new ButtonGroup();
        for (int tone = 0; tone <= ArtMore.SKINS.length; tone++) {
            final int t = tone;
            JToggleButton b = new JToggleButton();
            b.setPreferredSize(new Dimension(22, 22)); b.setMargin(new Insets(0, 0, 0, 0)); b.setFocusable(false);
            b.setIcon(swatch(t == 0 ? Theme.PANEL2 : ArtMore.SKINS[t - 1], t == 0 ? "?" : null, false));
            b.setSelectedIcon(swatch(t == 0 ? Theme.PANEL2 : ArtMore.SKINS[t - 1], t == 0 ? "?" : null, true));
            b.setToolTipText(t == 0 ? "Let the portrait draw its own skin" : ArtMore.SKIN_NAMES[t - 1]);
            b.setSelected(c.skinTone == t);
            b.addActionListener(e -> { c.skinTone = t; repaint.run(); });
            g.add(b); p.add(b);
        }
        return p;
    }

    private static Icon swatch(Color col, String mark, boolean on) {
        java.awt.image.BufferedImage im = new java.awt.image.BufferedImage(20, 20, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = im.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(col); g.fillRoundRect(1, 1, 18, 18, 6, 6);
        g.setColor(on ? Theme.GOLD : Theme.LINE); g.setStroke(new BasicStroke(on ? 2.5f : 1f)); g.drawRoundRect(1, 1, 18, 18, 6, 6);
        if (mark != null) { g.setColor(Theme.DIM); g.setFont(Theme.bold(12)); g.drawString(mark, 6, 15); }
        g.dispose();
        return new ImageIcon(im);
    }
    private static void repaintPortrait(JLabel l, Character c, String gender) {
        String g0 = c.gender;
        c.gender = gender;
        try { l.setIcon(Settings.pictures ? new ImageIcon(ArtMore.portrait(c, 140, 180)) : null); } catch (RuntimeException ignored) { l.setIcon(null); }
        c.gender = g0;
    }

    private static JCheckBox check(String t, boolean on) {
        JCheckBox c = new JCheckBox(t, on);
        c.setOpaque(false); c.setForeground(Theme.TEXT); c.setFont(Theme.plain(13));
        return c;
    }

    private static JRadioButton radio(String t, boolean on) {
        JRadioButton r = new JRadioButton(t, on);
        r.setOpaque(false); r.setForeground(Theme.TEXT); r.setFont(Theme.plain(14));
        return r;
    }

    private static JLabel head(String t) { JLabel l = new JLabel(t); l.setFont(Theme.bold(11)); l.setForeground(Theme.ACCENT); return l; }

    private static JLabel small(String t) { JLabel l = new JLabel(t); l.setFont(Theme.plain(12)); l.setForeground(Theme.DIM); return l; }

    private static JPanel row(Component... cs) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0)); p.setOpaque(false);
        for (Component c : cs) p.add(c);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        return p;
    }

    private static JPanel labelled(String l, JComponent f) {
        JPanel p = new JPanel(new BorderLayout(6, 0)); p.setOpaque(false);
        JLabel k = new JLabel(l); k.setForeground(Theme.DIM); k.setFont(Theme.plain(13)); k.setPreferredSize(new Dimension(40, 24));
        p.add(k, BorderLayout.WEST); p.add(f, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return p;
    }
}
