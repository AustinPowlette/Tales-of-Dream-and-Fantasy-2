package Game;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** The Script Manager (hybrid mode): your saved scripts on the left (load, save, save as, rename, delete, with a preview of the selected one) and the script
 *  you are editing on the right (steps with their loops indented, run / resume / stop, a menu of ready-made steps). Every change to the script is sent as
 *  a `script ...` command so the game thread stays its only writer; the lists refresh from a snapshot twice a second. The saved files are plain text in
 *  the scripts folder (`ScriptStore`), so renaming one here is a file rename. */
final class GuiScripts {
    private GuiScripts() { }

    private static JDialog dialog;
    private static String loadedName = null;     // the saved script the editor was loaded from or last saved as
    private static String savedSig = "";         // the steps as they were when it was last saved or loaded
    private static boolean awaitLoad = false;
    private static String sigBeforeLoad = "";
    private static long loadAsked = 0;

    /** Ready-made steps by kind: {menu, step}. Picking one puts it in the step box so its numbers can be changed before it is added. */
    private static final String[][] PALETTE = {
        {"Move", "goto 15 15"}, {"Move", "wait 5"},
        {"Work", "forage 10"}, {"Work", "forage until 500"}, {"Work", "mine 10"}, {"Work", "mine until 500"}, {"Work", "craft 5"}, {"Work", "profession"},
        {"Work", "meditate 10"}, {"Work", "meditate until major 3"}, {"Work", "hunt 5"},
        {"Trade and use", "sell"}, {"Trade and use", "sell goods"}, {"Trade and use", "buygear"}, {"Trade and use", "consume"}, {"Trade and use", "eat"}, {"Trade and use", "perform"},
        {"Loops and tests", "repeat 3"}, {"Loops and tests", "repeat forever"}, {"Loops and tests", "if ss >= 1000"}, {"Loops and tests", "end"},
        {"Mode and quests", "mode manual"}, {"Mode and quests", "mode auto"}, {"Mode and quests", "quest Go: home"}, {"Mode and quests", "cmd make food"},
    };

    static void open(MapViewer mv) {
        if (dialog != null && dialog.isDisplayable()) { dialog.setVisible(true); dialog.toFront(); return; }
        Color bg = Theme.PANEL;
        JDialog d = new JDialog(mv, "Script Manager (Hybrid mode)", false);
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        d.getContentPane().setBackground(bg);
        d.setLayout(new BorderLayout());

        // ================================================================ left: saved scripts
        DefaultListModel<String> savedModel = new DefaultListModel<>();
        JList<String> saved = new JList<>(savedModel);
        saved.setBackground(Theme.FIELD); saved.setForeground(Theme.TEXT); saved.setFont(Theme.plain(15));
        saved.setFixedCellHeight(28);
        saved.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        saved.setCellRenderer((l, v, i, sel, foc) -> {
            boolean cur = v.equals(loadedName);
            JLabel lb = new JLabel((cur ? "● " : "   ") + v);
            lb.setOpaque(true); lb.setBorder(new EmptyBorder(0, 8, 0, 4)); lb.setFont(Theme.plain(15));
            lb.setBackground(sel ? Theme.SELECT : Theme.FIELD); lb.setForeground(cur ? Theme.GOLD : Theme.TEXT);
            return lb;
        });
        JTextArea preview = new JTextArea(8, 18);
        preview.setEditable(false); preview.setBackground(Theme.PANEL2); preview.setForeground(Theme.DIM); preview.setFont(Theme.mono(12));
        preview.setBorder(new EmptyBorder(6, 8, 6, 8));
        saved.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            String n = saved.getSelectedValue();
            if (n == null) { preview.setText(""); return; }
            List<String> lines = ScriptStore.read(n);
            StringBuilder sb = new StringBuilder();
            int i = 1;
            for (String s : lines) sb.append(i++).append(". ").append(s).append('\n');
            preview.setText(lines.isEmpty() ? "(empty)" : sb.toString()); preview.setCaretPosition(0);
        });
        JLabel savedHead = head("SAVED SCRIPTS");
        JLabel savedNote = new JLabel("<html>Kept as plain text in the <b>scripts</b> folder, one step a line.</html>");
        savedNote.setFont(Theme.plain(12)); savedNote.setForeground(Theme.DIM);

        JButton load = mv.makeBtn("Load", "Replace the script on the right with the selected one.", () -> {
            String n = saved.getSelectedValue();
            if (n == null) return;
            if (!isRunning(mv) && curSteps(mv) > 0 && !GuiExtras.confirm(d, "Replace the script you are editing (" + curSteps(mv) + " steps) with '" + n + "'?")) return;
            sigBeforeLoad = sig(mv); awaitLoad = true; loadAsked = System.currentTimeMillis(); loadedName = n;
            mv.queueInput("!script load " + n);
        });
        load.setForeground(Theme.OK);
        JButton save = mv.makeBtn("Save", "Save the script on the right under the name it was loaded from (or ask for a name).", () -> {
            if (loadedName == null) { saveAs(mv, d); return; }
            mv.queueInput("script save " + loadedName); savedSig = sig(mv);
        });
        JButton saveAs = mv.makeBtn("Save as…", "Keep the script on the right under a new name.", () -> saveAs(mv, d));
        JButton rename = mv.makeBtn("Rename…", "Give the selected saved script another name.", () -> {
            String n = saved.getSelectedValue();
            if (n == null) return;
            Object o = JOptionPane.showInputDialog(d, "New name for '" + n + "':", "Rename script", JOptionPane.PLAIN_MESSAGE, null, null, n);
            if (o == null) return;
            String err = ScriptStore.rename(n, o.toString());
            if (err != null) JOptionPane.showMessageDialog(d, err, "Rename script", JOptionPane.WARNING_MESSAGE);
            else if (n.equals(loadedName)) loadedName = ScriptStore.clean(o.toString());
        });
        JButton del = mv.makeBtn("Delete", "Delete the selected saved script file. The script on the right is not touched.", () -> {
            String n = saved.getSelectedValue();
            if (n == null) return;
            if (GuiExtras.confirm(d, "Delete the saved script '" + n + "'?")) { mv.queueInput("!script delete " + n); if (n.equals(loadedName)) loadedName = null; }
        });
        del.setForeground(Theme.DANGER);
        JPanel savedBtns = new JPanel(new GridLayout(0, 2, 5, 5)); savedBtns.setBackground(bg);
        savedBtns.add(load); savedBtns.add(save); savedBtns.add(saveAs); savedBtns.add(rename); savedBtns.add(del);

        JPanel left = new JPanel(new BorderLayout(0, 6)); left.setBackground(bg);
        left.setBorder(new EmptyBorder(8, 8, 8, 4));
        JPanel lt = new JPanel(new BorderLayout()); lt.setBackground(bg); lt.add(savedHead, BorderLayout.NORTH); lt.add(savedNote, BorderLayout.CENTER);
        JPanel lb = new JPanel(new BorderLayout(0, 6)); lb.setBackground(bg);
        JScrollPane psp = new JScrollPane(preview); psp.setPreferredSize(new Dimension(200, 150));
        lb.add(psp, BorderLayout.CENTER); lb.add(savedBtns, BorderLayout.SOUTH);
        left.add(lt, BorderLayout.NORTH); left.add(new JScrollPane(saved), BorderLayout.CENTER); left.add(lb, BorderLayout.SOUTH);
        left.setPreferredSize(new Dimension(260, 400));

        // ================================================================ right: the script being edited
        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        list.setFont(Theme.mono(15)); list.setBackground(Theme.FIELD); list.setForeground(Theme.TEXT);
        list.setFixedCellHeight(26);
        list.setCellRenderer((l, v, i, sel, foc) -> {
            boolean here = v.contains("◀ here");
            String body = v.replace("   ◀ here", "");
            String word = body.replaceFirst("^\\d+\\.\\s*", "").trim().split(" ")[0];
            Color c = word.equals("repeat") || word.equals("if") || word.equals("end") ? Theme.GOLD : word.equals("goto") || word.equals("wait") ? Theme.ACCENT : Theme.TEXT;
            JLabel lb2 = new JLabel(v);
            lb2.setOpaque(true); lb2.setFont(Theme.mono(15)); lb2.setBorder(new EmptyBorder(0, 8, 0, 4));
            lb2.setForeground(here ? Theme.OK : c);
            lb2.setBackground(sel ? Theme.SELECT : here ? Theme.INK : Theme.FIELD);
            return lb2;
        });

        JLabel status = new JLabel(" ");
        status.setForeground(Theme.ACCENT); status.setFont(Theme.bold(14));
        JLabel which = new JLabel(" ");
        which.setFont(Theme.plain(13)); which.setForeground(Theme.DIM);

        JButton run = mv.makeBtn("▶ Run", "Start the script from its first step (Hybrid mode).", () -> mv.queueInput("script run"));
        run.setForeground(Theme.OK);
        JButton resume = mv.makeBtn("Resume", "Continue where the script stopped.", () -> mv.queueInput("script resume"));
        JButton stop = mv.makeBtn("■ Stop", "Stop the script and take control back.", () -> mv.queueInput("script stop"));
        stop.setForeground(Theme.DANGER);
        JPanel runRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2)); runRow.setBackground(bg);
        runRow.add(run); runRow.add(resume); runRow.add(stop);

        JTextField stepBox = new JTextField(22);
        stepBox.setFont(Theme.plain(15));
        stepBox.putClientProperty("JTextField.placeholderText", "a step, e.g. forage until 500");
        JButton palette = mv.makeBtn("Ready-made ▾", "Pick a step from a menu; its numbers can be changed before you add it.", () -> { });
        palette.addActionListener(e -> {
            JPopupMenu menu = new JPopupMenu();
            String kind = "";
            JMenu sub = null;
            for (String[] p : PALETTE) {
                if (!p[0].equals(kind)) { kind = p[0]; sub = new JMenu(kind); menu.add(sub); }
                final String step = p[1];
                JMenuItem mi = new JMenuItem(step);
                mi.addActionListener(ev -> { stepBox.setText(step); stepBox.requestFocusInWindow(); stepBox.selectAll(); });
                sub.add(mi);
            }
            menu.show(palette, 0, palette.getHeight());
        });
        Runnable addStep = () -> {
            String txt = stepBox.getText().trim();
            if (!txt.isEmpty()) mv.queueInput("script add " + txt);
        };
        stepBox.addActionListener(e -> addStep.run());
        JButton add = mv.makeBtn("Add step", "Add the step to the end of the script.", addStep);
        add.setForeground(Theme.OK);
        JButton insert = mv.makeBtn("Insert above", "Put the step above the selected one.", () -> {
            int i = Math.max(0, list.getSelectedIndex());
            String txt = stepBox.getText().trim();
            if (!txt.isEmpty()) mv.queueInput("script insert " + (i + 1) + " " + txt);
        });
        JPanel addRow = new JPanel(new BorderLayout(5, 0)); addRow.setBackground(bg);
        JPanel addBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0)); addBtns.setBackground(bg);
        addBtns.add(palette); addBtns.add(add); addBtns.add(insert);
        addRow.add(stepBox, BorderLayout.CENTER); addRow.add(addBtns, BorderLayout.EAST);

        JPanel edits = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2)); edits.setBackground(bg);
        edits.add(mv.makeBtn("Edit selected", () -> {
            int i = list.getSelectedIndex();
            if (i < 0) return;
            String cur = list.getSelectedValue().replaceFirst("^\\d+\\.\\s*", "").replace("   ◀ here", "").trim();
            Object o = JOptionPane.showInputDialog(d, "Step " + (i + 1) + ":", "Edit step", JOptionPane.PLAIN_MESSAGE, null, null, cur);
            if (o != null && !o.toString().trim().isEmpty()) mv.queueInput("script edit " + (i + 1) + " " + o.toString().trim());
        }));
        edits.add(mv.makeBtn("Delete step", () -> { int i = list.getSelectedIndex(); if (i >= 0) mv.queueInput("script del " + (i + 1)); }));
        edits.add(mv.makeBtn("Move up", () -> { int i = list.getSelectedIndex(); if (i > 0) { mv.queueInput("script move " + (i + 1) + " " + i); list.setSelectedIndex(i - 1); } }));
        edits.add(mv.makeBtn("Move down", () -> { int i = list.getSelectedIndex(); if (i >= 0 && i + 1 < model.size()) { mv.queueInput("script move " + (i + 1) + " " + (i + 2)); list.setSelectedIndex(i + 1); } }));
        edits.add(mv.makeBtn("Clear all", () -> { if (GuiExtras.confirm(d, "Clear the whole script?")) { mv.queueInput("!script clear"); loadedName = null; } }));

        JLabel hint = new JLabel("<html>Steps run one turn at a time; any command of the game is a step (type it as it is: buygear, make food…). 'repeat' and 'if' need a matching 'end'. Editing is refused while a script is running.</html>");
        hint.setForeground(Theme.DIM); hint.setFont(Theme.plain(12));

        JPanel top = new JPanel(new BorderLayout(0, 2)); top.setBackground(bg);
        JPanel t1 = new JPanel(new BorderLayout()); t1.setBackground(bg); t1.add(head("THE SCRIPT YOU ARE EDITING"), BorderLayout.WEST); t1.add(which, BorderLayout.EAST);
        top.add(t1, BorderLayout.NORTH); top.add(runRow, BorderLayout.CENTER); top.add(status, BorderLayout.SOUTH);
        JPanel bottom = new JPanel(new GridLayout(0, 1, 0, 3)); bottom.setBackground(bg);
        bottom.add(addRow); bottom.add(edits);
        JPanel right = new JPanel(new BorderLayout(0, 6)); right.setBackground(bg);
        right.setBorder(new EmptyBorder(8, 4, 8, 8));
        right.add(top, BorderLayout.NORTH); right.add(new JScrollPane(list), BorderLayout.CENTER);
        JPanel rs = new JPanel(new BorderLayout(0, 4)); rs.setBackground(bg); rs.add(bottom, BorderLayout.NORTH); rs.add(hint, BorderLayout.SOUTH);
        right.add(rs, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.0); split.setDividerLocation(270); split.setBorder(null); split.setBackground(bg);
        d.add(GuiHub.header(mv, "scripts", "Script Manager"), BorderLayout.NORTH);
        d.add(split, BorderLayout.CENTER);

        javax.swing.Timer refresh = new javax.swing.Timer(500, e -> {
            // the saved files (not part of the game: no lock needed)
            String[] names = ScriptStore.names();
            boolean same = names.length == savedModel.size();
            for (int i = 0; same && i < names.length; i++) if (!names[i].equals(savedModel.get(i))) same = false;
            if (!same) {
                String sel = saved.getSelectedValue();
                savedModel.clear();
                for (String n : names) savedModel.addElement(n);
                if (sel != null) saved.setSelectedValue(sel, true);
            }
            World.read(60, () -> {
                Character p = mv.findPlayer();
                if (p == null || p.pc == null) { status.setText("No player character."); return; }
                PlayerControl pc = p.pc;
                List<String> rows = new ArrayList<>();
                int ip = -1; boolean running = false;
                try {
                    PlayerScript sc = pc.script;
                    if (sc != null) {
                        ip = sc.ip; running = sc.running;
                        int depth = 0;
                        List<PlayerScript.Step> steps = new ArrayList<>(sc.steps);
                        for (int i = 0; i < steps.size(); i++) {
                            PlayerScript.Step st = steps.get(i);
                            if (st.type.equals("end")) depth = Math.max(0, depth - 1);
                            rows.add((i + 1) + ". " + PlayerScript.spaces(2 * depth) + st + (running && i == ip ? "   ◀ here" : ""));
                            if (st.type.equals("repeat") || st.type.equals("if")) depth++;
                        }
                    }
                } catch (Exception ignored) { return; } // the game thread changed the list mid-copy: try again next tick
                int sel = list.getSelectedIndex();
                if (!rows.equals(java.util.Collections.list(model.elements()))) {
                    model.clear();
                    for (String r : rows) model.addElement(r);
                    if (sel >= 0 && sel < model.size()) list.setSelectedIndex(sel);
                }
                String now = sig(pc);
                if (awaitLoad && (!now.equals(sigBeforeLoad) || System.currentTimeMillis() - loadAsked > 2500)) { savedSig = now; awaitLoad = false; }
                PlayerScript cur = pc.script;
                int nSteps = cur == null ? 0 : cur.steps.size();
                boolean dirty = !awaitLoad && !now.equals(savedSig) && nSteps > 0;
                which.setText(loadedName == null ? (nSteps == 0 ? "empty" : "not saved yet") : "from '" + loadedName + "'" + (dirty ? "  ● unsaved changes" : "  (saved)"));
                which.setForeground(dirty || loadedName == null && nSteps > 0 ? Theme.GOLD : Theme.DIM);
                status.setText(p.name + " — mode " + pc.mode + (running ? ", script step " + (ip + 1) + " of " + nSteps : "")
                        + (cur != null && !cur.lastReason.isEmpty() && !running ? " | last: " + cur.lastReason : ""));
            });
        });
        d.addWindowListener(new java.awt.event.WindowAdapter() { @Override public void windowClosed(java.awt.event.WindowEvent e) { refresh.stop(); dialog = null; } });
        refresh.setInitialDelay(0);
        refresh.start();

        d.setSize(900, 580);
        d.setMinimumSize(new Dimension(760, 460));
        d.setLocationRelativeTo(mv);
        GuiHub.restoreBounds(d, "scripts"); GuiHub.trackBounds(d, "scripts");
        Ui.prepare(d);
        dialog = d;
        d.setVisible(true);
    }

    // ------------------------------------------------------------------ helpers
    private static void saveAs(MapViewer mv, JDialog d) {
        Object o = JOptionPane.showInputDialog(d, "Name for this script (it is kept in the scripts folder):", "Save script", JOptionPane.PLAIN_MESSAGE, null, null, loadedName == null ? "" : loadedName);
        if (o == null) return;
        String n = ScriptStore.clean(o.toString());
        if (n.isEmpty()) { JOptionPane.showMessageDialog(d, "Use letters, digits, spaces, - and _.", "Save script", JOptionPane.WARNING_MESSAGE); return; }
        boolean exists = false;
        for (String s : ScriptStore.names()) if (s.equalsIgnoreCase(n)) exists = true;
        if (exists && !n.equals(loadedName) && !GuiExtras.confirm(d, "A saved script called '" + n + "' exists. Replace it?")) return;
        mv.queueInput("script save " + n);
        loadedName = n; savedSig = sig(mv);
    }

    private static JLabel head(String t) { JLabel l = new JLabel(t); l.setFont(Theme.bold(11)); l.setForeground(Theme.ACCENT); return l; }

    private static String sig(PlayerControl pc) {
        StringBuilder sb = new StringBuilder();
        try { if (pc != null && pc.script != null) for (PlayerScript.Step s : new ArrayList<>(pc.script.steps)) sb.append(s).append('\n'); } catch (RuntimeException ignored) { }
        return sb.toString();
    }

    private static String sig(MapViewer mv) {
        final String[] r = {""};
        World.read(60, () -> { Character p = mv.findPlayer(); if (p != null) r[0] = sig(p.pc); });
        return r[0];
    }

    private static int curSteps(MapViewer mv) {
        final int[] n = {0};
        World.read(60, () -> { Character p = mv.findPlayer(); if (p != null && p.pc != null && p.pc.script != null) n[0] = p.pc.script.steps.size(); });
        return n[0];
    }

    private static boolean isRunning(MapViewer mv) {
        final boolean[] r = {false};
        World.read(60, () -> { Character p = mv.findPlayer(); if (p != null && p.pc != null && p.pc.script != null) r[0] = p.pc.script.running; });
        return r[0];
    }
}
