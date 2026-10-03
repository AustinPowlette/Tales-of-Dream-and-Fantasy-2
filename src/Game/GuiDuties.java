package Game;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** The Duties & Policies window (Auto tab "Duties", the U key, the `duties` command): which chores the character does by itself, and how it answers
 *  prompts when nobody is at the keys. Two tabs so that each fits without scrolling; every control sends the same command the terminal has. */
final class GuiDuties {
    private GuiDuties() { }

    static final String[] DESC = {
            "Buy City Array, Entice Merchant, Hold Tournament (as lord), claim or found cities",
            "Sect missions, donations, sect arrays, recruiting, diplomacy, raids, promotions, founding sects",
            "Restock trips and selling stock for your shops",
            "Gather materials and craft or work your profession",
            "Use pills, buy gear, sell surplus, study at libraries, seek potential",
            "City beast bounties and Bounty Hunt",
            "Register for tournaments",
            "Grudge fights (Battle Dislike)",
            "Have a child",
            "Loot a fresh corpse"};

    static void open(MapViewer mv, String tab) {
        JDialog d = GuiWindows.window(mv, "duties", "Duties & Policies", 700, 600, 1200, (dlg, refreshers) -> build(mv, refreshers));
        if (d != null && tab != null) GuiHub.selectTab(d.getContentPane(), tab);
    }

    private static JLabel dim(String s) { JLabel l = GuiWindows.label(s); l.setForeground(Theme.DIM); l.setFont(Theme.plain(13)); return l; }

    private static JComponent build(MapViewer mv, List<Runnable> refreshers) {
        final boolean[] updating = {false};
        final Color BG = GuiWindows.BG, FG = GuiWindows.FG;

        // ---- mode banner (both tabs share it)
        JLabel modeLbl = new JLabel(" ");
        modeLbl.setFont(Theme.serif(17));
        JLabel modeNote = dim(" ");
        JButton back = mv.makeBtn("◀ Take control back", "Switch to MANUAL (also stops a running script): the game waits for your command every turn.", () -> mv.queueInput("stop"));
        JPanel banner = new JPanel(new BorderLayout(8, 0));
        banner.setBackground(Theme.PANEL2);
        banner.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        JPanel bt = new JPanel(new GridLayout(2, 1)); bt.setBackground(Theme.PANEL2); bt.add(modeLbl); bt.add(modeNote);
        banner.add(bt, BorderLayout.CENTER); banner.add(back, BorderLayout.EAST);

        // ---- duties tab
        JPanel duties = new JPanel(new GridBagLayout());
        duties.setBackground(BG);
        duties.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0; gc.anchor = GridBagConstraints.WEST; gc.insets = new Insets(3, 2, 3, 12);
        JLabel intro = new JLabel("<html>A <b>duty</b> is a kind of chore. <b>ON</b>: your character does it by itself, like an NPC would. <b>Off</b>: you do it (or you get an alert in the Quest Log).</html>");
        intro.setForeground(FG); intro.setFont(Theme.plain(14));
        gc.gridx = 0; gc.gridwidth = 2; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1; duties.add(intro, gc); gc.gridy++;
        gc.gridwidth = 1; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
        JCheckBox[] boxes = new JCheckBox[PlayerControl.DUTY_NAMES.length];
        for (int i = 0; i < boxes.length; i++) {
            final int idx = i;
            boxes[i] = new JCheckBox(PlayerControl.DUTY_NAMES[i]);
            boxes[i].setBackground(BG); boxes[i].setForeground(FG); boxes[i].setFont(Theme.bold(15));
            boxes[i].setToolTipText(DESC[i]);
            boxes[i].addActionListener(e -> { if (!updating[0]) mv.queueInput("duties " + PlayerControl.DUTY_NAMES[idx] + (boxes[idx].isSelected() ? " on" : " off")); });
            gc.gridx = 0; gc.weightx = 0; duties.add(boxes[i], gc);
            JLabel desc = dim(DESC[i]);
            gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL; duties.add(desc, gc); gc.fill = GridBagConstraints.NONE;
            gc.gridy++;
        }
        JLabel summary = GuiWindows.label(" ");
        summary.setForeground(GuiWindows.ACCENT);
        JPanel master = GuiWindows.flow(FlowLayout.LEFT);
        master.add(mv.makeBtn("All on", "Your character does every chore itself.", () -> mv.queueInput("duties all on")));
        master.add(mv.makeBtn("All off", "You do every chore yourself (alerts tell you what is waiting).", () -> mv.queueInput("duties all off")));
        master.add(summary);
        gc.gridx = 0; gc.gridwidth = 2; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(10, 0, 3, 0); duties.add(master, gc);
        gc.gridy++; gc.weighty = 1; gc.fill = GridBagConstraints.BOTH; duties.add(Box.createVerticalGlue(), gc);

        // ---- policies tab
        JPanel pol = new JPanel(new GridBagLayout());
        pol.setBackground(BG);
        pol.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        GridBagConstraints pg = new GridBagConstraints();
        pg.gridx = 0; pg.gridy = 0; pg.anchor = GridBagConstraints.WEST; pg.insets = new Insets(4, 2, 4, 2); pg.fill = GridBagConstraints.HORIZONTAL; pg.weightx = 1;
        JLabel pintro = new JLabel("<html>Decision policies answer the questions fights and events ask <b>when you are not at the keys</b> (AUTO or a script). In MANUAL you are asked, unless the timeout below runs out.</html>");
        pintro.setForeground(FG); pintro.setFont(Theme.plain(14));
        pol.add(pintro, pg); pg.gridy++;
        JComboBox<String> kill = new JComboBox<>(PlayerControl.KILL_POLICIES);
        kill.setToolTipText("What to do with a defeated foe");
        JCheckBox flee = new JCheckBox("try to flee when outmatched"), forb = new JCheckBox("use the Forbidden Technique in a deadly fight"),
                duel = new JCheckBox("accept challenges (off = try to slip away)"), toAuto = new JCheckBox("after a script ends, return to AUTO (off = MANUAL)"),
                alerts = new JCheckBox("a new quest alert interrupts a running script");
        flee.setToolTipText("When the fight looks lost, try to run instead of fighting on");
        forb.setToolTipText("A last resort that costs lifespan");
        duel.setToolTipText("Strangers, rivals and dislikers may challenge you; off means you try to slip away");
        toAuto.setToolTipText("What happens when a hybrid script finishes or is interrupted");
        alerts.setToolTipText("Stops a script the moment a new alert arrives so you can look at it");
        for (JCheckBox b : new JCheckBox[]{flee, forb, duel, toAuto, alerts}) { b.setBackground(BG); b.setForeground(FG); b.setFont(Theme.plain(15)); }
        JPanel k = GuiWindows.flow(FlowLayout.LEFT);
        k.add(GuiWindows.label("after winning a fight:")); k.add(kill);
        pol.add(k, pg); pg.gridy++;
        for (JCheckBox b : new JCheckBox[]{flee, forb, duel, toAuto, alerts}) { pol.add(b, pg); pg.gridy++; }
        JSpinner timeout = new JSpinner(new SpinnerNumberModel(0, 0, 3600, 5));
        timeout.setToolTipText("In MANUAL a prompt waits this long for you, then the policy answers (0 = wait forever)");
        JSpinner limit = new JSpinner(new SpinnerNumberModel(20, 1, ListQuery.MAX_LIMIT, 5));
        limit.setToolTipText("How many rows a list shows by default (the 'limit' command)");
        JPanel t = GuiWindows.flow(FlowLayout.LEFT);
        t.add(GuiWindows.label("decision timeout (s, 0 = wait forever):")); t.add(timeout);
        t.add(GuiWindows.label("list rows:")); t.add(limit);
        pol.add(t, pg); pg.gridy++;
        pg.weighty = 1; pg.fill = GridBagConstraints.BOTH; pol.add(Box.createVerticalGlue(), pg);
        kill.addActionListener(e -> { if (!updating[0]) mv.queueInput("policy kill " + kill.getSelectedIndex()); });
        flee.addActionListener(e -> { if (!updating[0]) mv.queueInput("policy flee " + (flee.isSelected() ? "on" : "off")); });
        forb.addActionListener(e -> { if (!updating[0]) mv.queueInput("policy forbidden " + (forb.isSelected() ? "on" : "off")); });
        duel.addActionListener(e -> { if (!updating[0]) mv.queueInput("policy duel " + (duel.isSelected() ? "accept" : "evade")); });
        toAuto.addActionListener(e -> { if (!updating[0]) mv.queueInput("policy script " + (toAuto.isSelected() ? "auto" : "manual")); });
        alerts.addActionListener(e -> { if (!updating[0]) mv.queueInput("policy alerts " + (alerts.isSelected() ? "on" : "off")); });
        timeout.addChangeListener(e -> { if (!updating[0]) mv.queueInput("policy timeout " + timeout.getValue()); });
        limit.addChangeListener(e -> { if (!updating[0]) mv.queueInput("limit " + limit.getValue()); });

        refreshers.add(() -> {
            Character p = GuiWindows.player(mv);
            if (p == null) return;
            PlayerControl pc = p.pc;
            updating[0] = true;
            try {
                for (int i = 0; i < boxes.length; i++) boxes[i].setSelected(pc.duties[i]);
                summary.setText("duties: " + pc.dutySummary() + "   mode: " + pc.mode);
                modeLbl.setText(pc.mode == PlayerControl.Mode.MANUAL ? "MANUAL — you decide every turn" : pc.mode == PlayerControl.Mode.AUTO ? "AUTO — your character lives like an NPC" : "HYBRID — a script is running");
                modeLbl.setForeground(pc.mode == PlayerControl.Mode.MANUAL ? Theme.OK : Theme.GOLD);
                modeNote.setText("duties " + pc.dutySummary() + (p.currentQuest != null ? " · quest: " + p.currentQuest.displayName() : ""));
                back.setEnabled(pc.mode != PlayerControl.Mode.MANUAL);
                kill.setSelectedIndex(pc.killPolicy);
                flee.setSelected(pc.fleePolicy); forb.setSelected(pc.forbiddenPolicy); duel.setSelected(pc.duelPolicy);
                toAuto.setSelected(pc.returnToAuto); alerts.setSelected(pc.interruptOnAlert);
                if (!timeout.getValue().equals(pc.decisionTimeout)) timeout.setValue(pc.decisionTimeout);
                if (!limit.getValue().equals(pc.listLimit)) limit.setValue(pc.listLimit);
            } finally { updating[0] = false; }
        });

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(GuiWindows.MONO_B);
        tabs.addTab("Duties", duties);
        tabs.addTab("Policies", pol);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(banner, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        return root;
    }
}
