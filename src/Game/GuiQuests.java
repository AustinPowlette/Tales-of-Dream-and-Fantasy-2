package Game;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/** The Quest Log: the ONE quest window of the game (the Me tab, the Move tab, the status bar's flag, the Q key and the `q`/`inbox` commands all open it).
 *  Left: every quest with its kind, target, distance and priority; right: a card for the selected one; below: what you can do with it. The numbered
 *  terminal list stays as it was; the window addresses quests by their stable id (`#uid`), so list numbers shifting never matters. */
final class GuiQuests {
    private GuiQuests() { }

    static final String[] FILTERS = {"All", "Alerts", "Duties", "Routine", "Pinned"};
    private static final Color ALERT = new Color(255, 170, 60);

    /** Opens (or raises) the Quest Log, optionally on a filter ("Alerts" ...). */
    static void open(MapViewer mv, String filter) {
        JDialog d = GuiWindows.window(mv, "quests", "Quest Log", 1120, 620, 1000, (dlg, refreshers) -> build(mv, dlg, refreshers));
        if (d == null || filter == null) return;
        Object f = d.getRootPane().getClientProperty("filter");
        if (f instanceof JComboBox) ((JComboBox<?>) f).setSelectedItem(filter);
    }

    /** True when the quest can be done where the player stands right now. */
    static boolean doableHere(Character p, Quest q) {
        if (q.category == Quest.QuestCategory.Spot) return true;
        if (q.category == Quest.QuestCategory.Character) { Character t = Character.charById(q.targetCharacter); return t != null && !t.dead && t.location == p.location; }
        return q.targetArea != null && q.targetArea == p.location;
    }

    static boolean hasPlace(Quest q) { return q.category != Quest.QuestCategory.Spot; }

    private static JComponent build(MapViewer mv, JDialog d, List<Runnable> refreshers) {
        JPanel root = new JPanel(new BorderLayout(6, 6));
        root.setBackground(GuiWindows.BG);
        root.setBorder(BorderFactory.createEmptyBorder(4, 6, 6, 6));

        DefaultTableModel model = GuiWindows.model();
        JTable table = GuiWindows.table(model);
        final List<Quest> shown = new ArrayList<>();
        JComboBox<String> filter = new JComboBox<>(FILTERS);
        d.getRootPane().putClientProperty("filter", filter);
        filter.setToolTipText("Alerts: quests that wait for you. Duties: chores you can hand to the character (Duties window). Routine: everyday work the game does for you.");
        JLabel status = GuiWindows.label(" ");
        status.setForeground(GuiWindows.ACCENT);
        JSpinner pri = new JSpinner(new SpinnerNumberModel(80, 0, 1000, 5));
        pri.setToolTipText("The priority a pinned quest keeps for good (higher = earlier). Normal quests drift with time.");
        JComboBox<String> muted = new JComboBox<>();
        JEditorPane card = GuiSheets.view(mv);
        final String[] lastCard = {""};
        JButton goBtn = mv.makeBtn("Go there", "Walk to the quest's place, or chase the person it names. One tile a turn, diagonals included; an alert or a fight stops the walk.", () -> { });
        JButton doBtn = mv.makeBtn("Do Quest", "Do it now: works when you stand at its place (or it needs no place). Uses the turn.", () -> { });
        JButton openBtn = mv.makeBtn("Open target", "Open the person it names, or the place's details.", () -> { });
        doBtn.setForeground(Theme.OK);

        final boolean[] busy = {false}; // refreshing re-selects the row, which fires the selection listener, which refreshes...: ignore that echo
        Runnable refreshBody = () -> {
            Character p = GuiWindows.player(mv);
            if (p == null) { status.setText("No player character."); return; }
            PlayerControl pc = p.pc;
            List<Quest> rows = new ArrayList<>();
            if (p.currentQuest != null) rows.add(p.currentQuest);
            List<Quest> t = new ArrayList<>(p.targetQuests); t.sort((a, b) -> Integer.compare(b.priority, a.priority)); rows.addAll(t);
            List<Quest> s = new ArrayList<>(p.spotQuests); s.sort((a, b) -> Integer.compare(b.priority, a.priority)); rows.addAll(s);
            String f = String.valueOf(filter.getSelectedItem());
            Quest keep = null;
            int sel = table.getSelectedRow();
            if (sel >= 0 && sel < shown.size()) keep = shown.get(sel);
            shown.clear();
            List<Object[]> data = new ArrayList<>();
            int alerts = 0;
            for (Quest q : rows) {
                int cls = PlayerControl.classify(p, q);
                boolean isAlert = cls == PlayerControl.ALERT || (cls >= 0 && !pc.duties[cls]);
                if (pc.inbox.contains(q)) alerts++;
                boolean ok = f.equals("All") || (f.equals("Alerts") && isAlert) || (f.equals("Duties") && cls >= 0) || (f.equals("Routine") && cls == PlayerControl.MUNDANE) || (f.equals("Pinned") && q.pinned);
                if (!ok) continue;
                String target = "here", where = "", tiles = "";
                if (q.category == Quest.QuestCategory.Character) {
                    Character t2 = Character.charById(q.targetCharacter);
                    if (t2 != null) { target = t2.name + " " + t2.lastName + " (#" + t2.id + ")"; where = PlayerControl.coord(t2.location); tiles = String.valueOf(PlayerControl.distance(p.location, t2.location)); }
                } else if (q.category == Quest.QuestCategory.Area && q.targetArea != null) {
                    target = q.targetArea.name; where = PlayerControl.coord(q.targetArea); tiles = String.valueOf(PlayerControl.distance(p.location, q.targetArea));
                }
                String state = (q.pinned ? "pinned " : "") + (pc.inbox.contains(q) ? "NEW" : "");
                shown.add(q);
                data.add(new Object[]{q == p.currentQuest ? "ACTIVE" : PlayerControl.classText(cls), q.displayName(), target, where, tiles, q.priority, state.trim()});
            }
            model.setDataVector(data.toArray(new Object[0][]), new String[]{"Kind", "Quest", "Target", "Where", "Tiles", "Pri", "State"});
            int[] w = {80, 220, 190, 110, 48, 48, 80};
            for (int i = 0; i < w.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
            if (keep != null) { int i = shown.indexOf(keep); if (i >= 0) table.setRowSelectionInterval(i, i); }
            status.setText(rows.size() + " quests · " + alerts + " new alerts · mode " + pc.mode + (shown.size() != rows.size() ? "  (" + shown.size() + " shown)" : "")); status.revalidate();
            Object cur = muted.getSelectedItem();
            muted.removeAllItems();
            for (String m : new ArrayList<>(pc.muted)) muted.addItem(m);
            if (cur != null) muted.setSelectedItem(cur);
            // the card and the buttons follow the selected row
            int r = table.getSelectedRow();
            Quest q = r >= 0 && r < shown.size() ? shown.get(r) : null;
            String html = q == null ? GuiSheets.page("<h1>Quest Log</h1><p class='dim'>Select a quest to see what it asks of you.</p><p class='dim'>Double-click a row to go there (or do it if you are already there).</p>") : cardHtml(p, q);
            if (!html.equals(lastCard[0])) { lastCard[0] = html; card.setText(html); card.setCaretPosition(0); }
            boolean sel1 = q != null;
            doBtn.setEnabled(sel1 && doableHere(p, q));
            goBtn.setEnabled(sel1 && hasPlace(q) && !doableHere(p, q));
            openBtn.setEnabled(sel1 && hasPlace(q));
        };
        Runnable refresh = () -> { if (busy[0]) return; busy[0] = true; try { refreshBody.run(); } finally { busy[0] = false; } };
        refreshers.add(refresh);
        filter.addActionListener(e -> { World.read(150, refresh); });
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) World.read(150, refresh); });

        // alerts in orange, duties cyan, routine dim, the active quest gold
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isSel, foc, r, c);
                String kind = String.valueOf(t.getValueAt(r, 0));
                Color col = kind.equals("ACTIVE") ? Theme.GOLD : kind.equals("alert") ? ALERT : kind.startsWith("duty") ? Theme.ACCENT : kind.equals("routine") ? Theme.DIM : Theme.TEXT;
                if (!isSel) { comp.setBackground(Theme.FIELD); comp.setForeground(c == 0 || c == 1 ? col : (c == 6 && String.valueOf(v).contains("NEW") ? ALERT : Theme.TEXT)); }
                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                return comp;
            }
        });

        java.util.function.Function<Integer, Quest> selected = unused -> { int r = table.getSelectedRow(); return r >= 0 && r < shown.size() ? shown.get(r) : null; };
        java.util.function.Consumer<String> act = fmt -> {
            Quest q = selected.apply(0);
            if (q == null) { status.setText("Select a quest first."); return; }
            mv.queueInput(String.format(fmt, q.uid, pri.getValue()));
        };
        goBtn.addActionListener(e -> act.accept("goquest #%d"));
        doBtn.addActionListener(e -> act.accept("qdo #%d"));
        openBtn.addActionListener(e -> {
            Quest q = selected.apply(0);
            if (q == null) return;
            if (q.category == Quest.QuestCategory.Character) { Character t = Character.charById(q.targetCharacter); if (t != null && !t.dead) mv.showCharacterDialog(t); }
            else if (q.targetArea != null) { if (q.targetArea.city || q.targetArea.sect != null) GuiMore.placeWindow(mv, q.targetArea); else mv.selectTile(q.targetArea.x, q.targetArea.y); }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) return;
                Character p = GuiWindows.player(mv);
                Quest q = selected.apply(0);
                if (p == null || q == null) return;
                if (doableHere(p, q)) mv.queueInput("qdo #" + q.uid); else if (hasPlace(q)) mv.queueInput("goquest #" + q.uid);
            }
        });

        JPanel actions = GuiWindows.flow(FlowLayout.LEFT);
        actions.add(goBtn); actions.add(doBtn); actions.add(openBtn);
        actions.add(GuiWindows.label("   priority")); actions.add(pri);
        actions.add(mv.makeBtn("Pin priority", "Fix this quest's priority so it never ages (and is never shed).", () -> act.accept("qp #%d %d")));
        actions.add(mv.makeBtn("Unpin", "Let this quest age normally again.", () -> act.accept("qu #%d")));
        JButton del = mv.makeBtn("Delete", "Remove this quest. It may come back unless you mute its kind.", () -> {
            Quest q = selected.apply(0);
            if (q == null) { status.setText("Select a quest first."); return; }
            if ((q.pinned || q.userCreated) && !GuiExtras.confirm(d, "Delete " + (q.userCreated ? "your own" : "the pinned") + " quest '" + q.displayName() + "'?")) return;
            mv.queueInput("!qd #" + q.uid);
        });
        del.setForeground(Theme.DANGER);
        actions.add(del);
        JPanel mutes = GuiWindows.flow(FlowLayout.LEFT);
        mutes.add(mv.makeBtn("Mute this kind", "Remove every quest of this kind and never post it again (Unmute reverses it).", () -> {
            Quest q = selected.apply(0);
            if (q != null && GuiExtras.confirm(d, "Mute '" + q.name + "'? Every queued one is removed and none is posted again (Unmute reverses it).")) mv.queueInput("!mute " + q.name);
        }));
        mutes.add(GuiWindows.label("muted:")); mutes.add(muted);
        mutes.add(mv.makeBtn("Unmute", "Allow the chosen kind of quest again.", () -> { Object m = muted.getSelectedItem(); if (m != null) mv.queueInput("unmute " + m); }));

        JTextField wpLabel = new JTextField("waypoint", 9), wpX = new JTextField(3), wpY = new JTextField(3);
        wpLabel.setToolTipText("A name for your own quest; it becomes 'Go: <name>' in the list");
        JSpinner wpPri = new JSpinner(new SpinnerNumberModel(70, 0, 1000, 5));
        JPanel wp = GuiWindows.flow(FlowLayout.LEFT);
        wp.add(GuiWindows.label("new waypoint:")); wp.add(wpLabel); wp.add(GuiWindows.label("x")); wp.add(wpX); wp.add(GuiWindows.label("y")); wp.add(wpY); wp.add(GuiWindows.label("pri")); wp.add(wpPri);
        wp.add(mv.makeBtn("Use map tile", "Fill x and y with the tile selected on the map.", () -> { int[] s = mv.selectedTile(); if (s != null) { wpX.setText(String.valueOf(s[0])); wpY.setText(String.valueOf(s[1])); } }));
        wp.add(mv.makeBtn("Add", "Pin a quest that leads to this tile (blank x/y = where you stand). AUTO and scripts walk there; in MANUAL you are alerted.", () -> {
            Character p = GuiWindows.player(mv);
            String x = wpX.getText().trim(), y = wpY.getText().trim();
            if (x.isEmpty() && p != null) x = String.valueOf(p.location.x);
            if (y.isEmpty() && p != null) y = String.valueOf(p.location.y);
            mv.queueInput("qnew " + wpLabel.getText().trim().replace(' ', '_') + " " + x + " " + y + " " + wpPri.getValue());
        }));
        wp.add(GuiWindows.label("(blank = where you stand)"));

        JPanel top = GuiWindows.flow(FlowLayout.LEFT);
        top.add(GuiWindows.label("show:")); top.add(filter);
        top.add(mv.makeBtn("Next alert", "Select the next quest that waits for you.", () -> {
            int n = shown.size(), from = table.getSelectedRow();
            Character p = GuiWindows.player(mv);
            if (p == null) return;
            for (int k = 1; k <= n; k++) { int i = (from + k + n) % Math.max(1, n); if (i < n && p.pc.inbox.contains(shown.get(i))) { table.setRowSelectionInterval(i, i); table.scrollRectToVisible(table.getCellRect(i, 0, true)); return; } }
            status.setText("No new alerts waiting.");
        }));
        top.add(mv.makeBtn("▶ Go to top quest", "Walk to the place of your first quest (or chase the person it names). Same as the Go button in the status bar.", () -> mv.queueInput("goquest")));
        top.add(status);

        JPanel south = new JPanel(new GridLayout(3, 1));
        south.setBackground(GuiWindows.BG);
        south.add(actions); south.add(mutes); south.add(wp);
        JScrollPane cardScroll = new JScrollPane(card);
        cardScroll.setPreferredSize(new Dimension(330, 100));
        cardScroll.getViewport().setBackground(Theme.BG);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(table), cardScroll);
        split.setResizeWeight(0.72);
        split.setContinuousLayout(true);
        split.setBackground(GuiWindows.BG);
        root.add(top, BorderLayout.NORTH);
        root.add(split, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
        return root;
    }

    /** The card of one quest: what it is, why it was posted, who/where it points at and what to do next. */
    static String cardHtml(Character p, Quest q) {
        PlayerControl pc = p.pc;
        int cls = PlayerControl.classify(p, q);
        StringBuilder sb = new StringBuilder("<h1>" + GuiSheets.esc(q.displayName()) + "</h1>");
        String kind = q == p.currentQuest ? "the quest your character is on" : cls == PlayerControl.ALERT ? "an alert: it waits for you" : cls == PlayerControl.MUNDANE ? "everyday work" : "a duty (" + PlayerControl.DUTY_NAMES[cls] + ")" + (pc.duties[cls] ? ", done for you" : ", yours to do");
        sb.append("<span class='dim'>").append(kind).append(" · priority ").append(q.priority).append(q.pinned ? " (pinned)" : "").append(q.userCreated ? " · your own waypoint" : "").append("</span>");
        String why = PlayerControl.REASONS.get(q.name);
        sb.append("<table width='100%'>");
        if (why != null) sb.append("<tr><td width='24%' class='dim'>Why</td><td>").append(GuiSheets.esc(why)).append("</td></tr>");
        boolean here = doableHere(p, q);
        if (q.category == Quest.QuestCategory.Character) {
            Character t = Character.charById(q.targetCharacter);
            if (t != null && !t.dead) {
                sb.append("<tr><td class='dim'>Person</td><td><a href='char:").append(t.id).append("'>").append(GuiSheets.esc(t.name + " " + t.lastName)).append("</a> · Lv ").append(t.lvl).append(" · ").append(GuiSheets.esc(t.affiliation)).append("</td></tr>");
                if (t.location != null) sb.append("<tr><td class='dim'>Where</td><td><a href='goto:").append(t.location.x).append(":").append(t.location.y).append("'>").append(GuiSheets.esc(t.location.name)).append(" ").append(PlayerControl.coord(t.location)).append("</a> · ").append(PlayerControl.distance(p.location, t.location)).append(" tiles</td></tr>");
            } else sb.append("<tr><td class='dim'>Person</td><td>gone</td></tr>");
        } else if (q.category == Quest.QuestCategory.Area && q.targetArea != null) {
            Area a = q.targetArea;
            String nm = (a.city || a.sect != null) ? "<a href='place:" + a.x + ":" + a.y + "'>" + GuiSheets.esc(a.name) + "</a>" : GuiSheets.esc(a.name);
            sb.append("<tr><td class='dim'>Place</td><td>").append(nm).append(" <a href='goto:").append(a.x).append(":").append(a.y).append("'>").append(PlayerControl.coord(a)).append("</a></td></tr>");
            sb.append("<tr><td class='dim'>Distance</td><td>").append(PlayerControl.distance(p.location, a)).append(" tiles").append(here ? " — you are there" : "").append("</td></tr>");
        } else sb.append("<tr><td class='dim'>Place</td><td>done where you stand, no travel</td></tr>");
        sb.append("</table>");
        sb.append("<h2>NEXT STEP</h2><p>");
        if (q.category == Quest.QuestCategory.Spot) sb.append("Press <b>Do Quest</b>: it is done in your tile and uses the turn. If its conditions are not met here nothing happens and it stays.");
        else if (here) sb.append("You are in the right place. Press <b>Do Quest</b>.");
        else sb.append("Press <b>Go there</b> (or double-click the row): you walk one tile a turn. When you arrive, press <b>Do Quest</b>.");
        sb.append("</p>");
        if (pc.mode != PlayerControl.Mode.MANUAL) sb.append("<p class='dim'>Your character is in ").append(pc.mode).append(" mode: it takes quests by itself; these buttons act on your command first.</p>");
        return GuiSheets.page(sb.toString());
    }
}
