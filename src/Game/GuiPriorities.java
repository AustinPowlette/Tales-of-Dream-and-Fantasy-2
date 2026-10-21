package Game;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** The Priorities tab of the Control window: how this person ranks the kinds of quest. A rule replaces the game's own number for the quests it matches (`PlayerControl.prioRules`, `Character.applyPriorityRules`).
 *  Left: every quest type with the priority it starts at and your rule; the buttons set or clear the rule of the selected type. Below: a rule for a kind of target (family, friends, enemies, sect mates, strangers, cities,
 *  sects, dungeons, wild places) and the list of every rule with a Remove button. Every control sends a `qprio` command, so nothing here writes game state. */
final class GuiPriorities {
    private GuiPriorities() { }

    static JComponent create(final MapViewer mv, final List<Runnable> refreshers) {
        final Color BG = GuiWindows.BG, FG = GuiWindows.FG;
        JPanel root = new JPanel(new BorderLayout(6, 6));
        root.setBackground(BG);
        root.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        JLabel intro = new JLabel("<html>A quest with a <b>higher priority</b> is done first. A rule replaces the game's own number for the quests it matches and stops them ageing. "
                + "The most specific rule wins: <b>this person or place</b> over a <b>kind of target</b> over a <b>type</b>. Pinned quests keep their own priority.</html>");
        intro.setForeground(FG); intro.setFont(Theme.plain(13));
        // ---- the breakthrough prompt
        final boolean[] setting = {false};
        final JComboBox<String> bt = new JComboBox<>(new String[]{"ask me (a prompt, with the odds)", "break through by itself, as an NPC does", "wait for the Breakthrough button"});
        bt.setToolTipText("What happens when your cultivation is full at the last level of a realm and a breakthrough is due");
        bt.addActionListener(e -> { if (!setting[0]) mv.queueInput("policy breakthrough " + PlayerControl.BREAKTHROUGH_POLICIES[bt.getSelectedIndex()]); });
        JPanel btRow = GuiWindows.flow(FlowLayout.LEFT);
        btRow.add(GuiWindows.label("when a breakthrough is due:")); btRow.add(bt);
        // ---- what stops a walk (Go There, goto, chase)
        final JCheckBox stopFight = new JCheckBox("a fight stops my walk"), stopNotice = new JCheckBox("a new quest alert stops my walk");
        for (JCheckBox cb : new JCheckBox[]{stopFight, stopNotice}) { cb.setBackground(BG); cb.setForeground(FG); cb.setFont(Theme.plain(13)); }
        stopFight.setToolTipText("Off: a fight that finds you on a walk is fought and you keep walking. On: the walk stops so you can decide.");
        stopNotice.setToolTipText("Off: a new quest alert waits until your walk ends. On: the walk stops when an alert comes.");
        stopFight.addActionListener(e -> { if (!setting[0]) mv.queueInput("policy walkfight " + (stopFight.isSelected() ? "on" : "off")); });
        stopNotice.addActionListener(e -> { if (!setting[0]) mv.queueInput("policy walknotice " + (stopNotice.isSelected() ? "on" : "off")); });
        btRow.add(Box.createHorizontalStrut(14)); btRow.add(stopFight); btRow.add(stopNotice);
        JPanel north = new JPanel(new BorderLayout(4, 4));
        north.setBackground(BG);
        north.add(intro, BorderLayout.NORTH);
        north.add(btRow, BorderLayout.CENTER);
        root.add(north, BorderLayout.NORTH);

        // ---- the types
        final DefaultTableModel types = GuiWindows.model();
        final JTable table = GuiWindows.table(types);
        final List<String> typeNames = new ArrayList<>();
        final JSpinner value = new JSpinner(new SpinnerNumberModel(60, 0, 100, 5));
        JPanel setRow = GuiWindows.flow(FlowLayout.LEFT);
        setRow.add(GuiWindows.label("priority for the selected type:")); setRow.add(value);
        setRow.add(mv.makeBtn("Set rule", "Every quest of the selected type gets this priority.", () -> {
            int r = table.getSelectedRow();
            if (r >= 0 && r < typeNames.size()) mv.queueInput("qprio " + typeNames.get(r) + " " + value.getValue());
        }));
        setRow.add(mv.makeBtn("Clear rule", "Back to the game's own priority for the selected type.", () -> {
            int r = table.getSelectedRow();
            if (r >= 0 && r < typeNames.size()) mv.queueInput("qprio " + typeNames.get(r) + " off");
        }));
        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            if (e.getValueIsAdjusting() || r < 0 || r >= typeNames.size()) return;
            Object rule = types.getValueAt(r, 2);
            String s = String.valueOf(rule);
            if (s.matches("\\d+")) value.setValue(Integer.parseInt(s)); else value.setValue(Integer.parseInt(String.valueOf(types.getValueAt(r, 1))));
        });
        JPanel left = new JPanel(new BorderLayout(4, 4));
        left.setBackground(BG);
        left.add(new JScrollPane(table), BorderLayout.CENTER);
        left.add(setRow, BorderLayout.SOUTH);

        // ---- kinds of target, and the rules
        final JComboBox<String> kindType = new JComboBox<>();
        kindType.addItem("any type");
        for (String[] q : PlayerControl.QUEST_TYPES) kindType.addItem(q[0]);
        final JComboBox<String> kind = new JComboBox<>();
        for (String k : PlayerControl.KINDS_OF_PERSON) kind.addItem(k);
        for (String k : PlayerControl.KINDS_OF_PLACE) kind.addItem(k.equals("sect") ? "sect (place)" : k);
        final JSpinner kindValue = new JSpinner(new SpinnerNumberModel(70, 0, 100, 5));
        JPanel kindRow = GuiWindows.flow(FlowLayout.LEFT);
        kindRow.add(GuiWindows.label("quests of")); kindRow.add(kindType); kindRow.add(GuiWindows.label("about")); kindRow.add(kind); kindRow.add(GuiWindows.label("=")); kindRow.add(kindValue);
        kindRow.add(mv.makeBtn("Set", "Every such quest about that kind of person or place gets this priority.", () -> {
            String t = kindType.getSelectedIndex() <= 0 ? "" : kindType.getSelectedItem() + " ";
            String k = String.valueOf(kind.getSelectedItem()).replace(" (place)", "");
            mv.queueInput("qprio " + t + "@" + k + " " + kindValue.getValue());
        }));
        final DefaultListModel<String> ruleModel = new DefaultListModel<>();
        final JList<String> ruleList = new JList<>(ruleModel);
        ruleList.setBackground(Theme.FIELD); ruleList.setForeground(FG); ruleList.setFont(GuiWindows.MONO);
        final List<String> ruleKeys = new ArrayList<>();
        JPanel ruleBtns = GuiWindows.flow(FlowLayout.LEFT);
        ruleBtns.add(mv.makeBtn("Remove rule", "Remove the selected rule.", () -> {
            int r = ruleList.getSelectedIndex();
            if (r < 0 || r >= ruleKeys.size()) return;
            String key = ruleKeys.get(r);
            int bar = key.indexOf('|');
            String type = key.substring(0, bar), scope = key.substring(bar + 1);
            String sc = scope.isEmpty() ? "" : scope.startsWith("kind:") ? " @" + scope.substring(5) : scope.startsWith("id:") ? " #" + scope.substring(3) : " at:" + scope.substring(5);
            mv.queueInput("qprio " + (type.equals("*") ? "" : type) + sc + " off");
        }));
        ruleBtns.add(mv.makeBtn("Remove all", "Quests rank as the game ranks them again.", () -> mv.queueInput("qprio reset")));
        JPanel right = new JPanel(new BorderLayout(4, 4));
        right.setBackground(BG);
        JPanel rt = new JPanel(new BorderLayout(4, 4));
        rt.setBackground(BG);
        rt.add(GuiWindows.label("By kind of target"), BorderLayout.NORTH);
        rt.add(kindRow, BorderLayout.CENTER);
        right.add(rt, BorderLayout.NORTH);
        JPanel rl = new JPanel(new BorderLayout(4, 4));
        rl.setBackground(BG);
        rl.add(GuiWindows.label("Your rules"), BorderLayout.NORTH);
        rl.add(new JScrollPane(ruleList), BorderLayout.CENTER);
        rl.add(ruleBtns, BorderLayout.SOUTH);
        right.add(rl, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.5); split.setBorder(null); split.setBackground(BG);
        SwingUtilities.invokeLater(() -> split.setDividerLocation(0.5));
        root.add(split, BorderLayout.CENTER);

        refreshers.add(() -> {
            Character p = GuiWindows.player(mv);
            if (p == null) return;
            setting[0] = true;
            try { bt.setSelectedIndex(Math.max(0, Math.min(2, p.pc.breakthroughPolicy))); stopFight.setSelected(p.pc.walkStopFight); stopNotice.setSelected(p.pc.walkStopNotice); } finally { setting[0] = false; }
            LinkedHashMap<String, Integer> rules = p.pc.prioRules == null ? new LinkedHashMap<String, Integer>() : new LinkedHashMap<>(p.pc.prioRules);
            List<Object[]> rows = new ArrayList<>();
            typeNames.clear();
            for (String[] q : PlayerControl.QUEST_TYPES) {
                Integer rule = rules.get(q[0] + "|");
                typeNames.add(q[0]);
                rows.add(new Object[]{q[0], Integer.parseInt(q[1]), rule == null ? "" : String.valueOf(rule)});
            }
            int sel = table.getSelectedRow();
            types.setDataVector(rows.toArray(new Object[0][]), new String[]{"Quest type", "Starts at", "Your rule"});
            if (sel >= 0 && sel < rows.size()) table.setRowSelectionInterval(sel, sel);
            int rs = ruleList.getSelectedIndex();
            ruleModel.clear(); ruleKeys.clear();
            for (java.util.Map.Entry<String, Integer> e : rules.entrySet()) { ruleKeys.add(e.getKey()); ruleModel.addElement(PlayerControl.ruleText(e.getKey(), e.getValue())); }
            if (rs >= 0 && rs < ruleModel.size()) ruleList.setSelectedIndex(rs);
        });
        return root;
    }
}
