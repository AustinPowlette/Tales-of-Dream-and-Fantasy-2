package Game;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/** The one list of places. It replaces Me > Locations (an HTML page) and Browser > Places (a table): the places you know, with the filters of the terminal `list places` (name, type, distance, level,
 *  extra filters, sort, limit), a "Noted only" box, sort by clicking a header, and the buttons for a place: Go there, Route, Add waypoint quest, Details (city or sect sheet, or select the tile on the map),
 *  Note / Remove note (the bookmarks of `note tile <x> <y> <text>`). A noted place shows its note and its name in gold. Every action is a command; nothing here touches game state. */
final class GuiPlaceList {
    private GuiPlaceList() { }

    private static final String[][] HEADER_KEYS = {{"Name", "name"}, {"Lv", "lvl"}, {"Type", "type"}, {"Dist", "dist"}, {"Beasts", "beasts"}, {"Note", "noted"}};

    /** `inBrowser`: the list sits in the Browser window (no "Open in Browser" button). One refresher is added to `refreshers`. */
    static JComponent create(final MapViewer mv, final List<Runnable> refreshers, final boolean inBrowser) {
        final JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBackground(GuiWindows.BG);

        final JTextField name = new JTextField(10), lvlMin = new JTextField(3), lvlMax = new JTextField(3), distMax = new JTextField(3), extra = new JTextField(12);
        name.setToolTipText("Part of a name; press Enter");
        extra.setToolTipText("More filters in the list language, e.g. beasts<=0 pop>=500 (see 'help list')");
        final JComboBox<String> type = new JComboBox<>(new String[]{"any", "city", "sect", "dungeon", "wild"});
        final JCheckBox vein = new JCheckBox("spirit veins"), noted = new JCheckBox("noted only");
        for (JCheckBox b : new JCheckBox[]{vein, noted}) { b.setBackground(GuiWindows.BG); b.setForeground(GuiWindows.FG); }
        noted.setToolTipText("Only the places you marked with a note");
        final java.util.Set<String> keys = PlayerLists.PLACE_KEYS;
        final JComboBox<String> sort = new JComboBox<>();
        sort.addItem("(default: nearest)");
        for (String k : keys) sort.addItem(k);
        final JCheckBox desc = new JCheckBox("desc");
        desc.setBackground(GuiWindows.BG); desc.setForeground(GuiWindows.FG);
        Character p0 = mv.findPlayer();
        final JSpinner limit = new JSpinner(new SpinnerNumberModel(Math.max(1, Math.min(ListQuery.MAX_LIMIT, p0 != null && p0.pc != null ? p0.pc.listLimit : 20)), 1, ListQuery.MAX_LIMIT, 5));
        final JLabel status = GuiWindows.label(" ");
        status.setForeground(GuiWindows.ACCENT);

        JPanel filters = GuiWindows.flow(FlowLayout.LEFT);
        filters.add(GuiWindows.label("name:")); filters.add(name);
        filters.add(GuiWindows.label("type")); filters.add(type);
        filters.add(GuiWindows.label("dist ≤")); filters.add(distMax);
        filters.add(GuiWindows.label("lvl ≥")); filters.add(lvlMin); filters.add(GuiWindows.label("≤")); filters.add(lvlMax);
        filters.add(vein); filters.add(noted);
        filters.add(GuiWindows.label("extra")); filters.add(extra);
        filters.add(GuiWindows.label("sort")); filters.add(sort); filters.add(desc);
        filters.add(GuiWindows.label("limit")); filters.add(limit);

        final DefaultTableModel model = GuiWindows.model();
        final JTable table = GuiWindows.table(model);
        final PlayerLists.ListView[] current = {null};
        final boolean[] busy = {false};

        final java.util.function.Supplier<Area> selected = () -> {
            int r = table.getSelectedRow();
            if (r < 0 || current[0] == null || r >= current[0].refs.size()) { status.setText("Select a row first."); return null; }
            return (Area) current[0].refs.get(r);
        };

        final Runnable body = () -> {
            Character p = mv.findPlayer();
            if (p == null || p.location == null || p.pc == null) { status.setText("No player character."); return; }
            List<String> tok = new ArrayList<>();
            tok.add(String.valueOf(limit.getValue()));
            if (sort.getSelectedIndex() > 0) tok.add("sort=" + sort.getSelectedItem() + (desc.isSelected() ? ":desc" : ""));
            else if (desc.isSelected()) tok.add("desc");
            for (String w : name.getText().trim().split("\\s+")) if (!w.isEmpty()) tok.add("name=" + w);
            if (type.getSelectedIndex() > 0) tok.add("type=" + type.getSelectedItem());
            if (!distMax.getText().trim().isEmpty()) tok.add("dist<=" + distMax.getText().trim());
            if (!lvlMin.getText().trim().isEmpty()) tok.add("lvl>=" + lvlMin.getText().trim());
            if (!lvlMax.getText().trim().isEmpty()) tok.add("lvl<=" + lvlMax.getText().trim());
            if (vein.isSelected()) tok.add("vein=1");
            if (noted.isSelected()) tok.add("noted=1");
            for (String w : extra.getText().trim().split("\\s+")) if (!w.isEmpty()) tok.add(w);
            PlayerLists.ListView v;
            try {
                v = PlayerLists.places(p, ListQuery.parse(tok.toArray(new String[0]), 0), p.pc.listLimit);
            } catch (RuntimeException ex) { return; } // the game thread changed a list mid-copy: the next refresh will succeed
            if (v.error != null) { status.setText(v.error); return; }
            Object sel = null;
            int row = table.getSelectedRow();
            if (row >= 0 && current[0] != null && row < current[0].refs.size()) sel = current[0].refs.get(row);
            current[0] = v;
            model.setDataVector(v.rows.toArray(new Object[0][]), v.headers);
            if (table.getColumnCount() > 1) table.getColumnModel().getColumn(0).setMaxWidth(36);
            if (sel != null) for (int i = 0; i < v.refs.size(); i++) if (v.refs.get(i) == sel) { table.setRowSelectionInterval(i, i); break; }
            status.setText("showing " + v.rows.size() + " of " + v.total + " places you know   —   you are at " + p.location.name + " " + PlayerControl.coord(p.location));
        };
        final Runnable run = () -> { if (busy[0]) return; busy[0] = true; try { body.run(); } finally { busy[0] = false; } };
        final Runnable search = () -> World.readSoon(200, run);
        refreshers.add(() -> World.read(80, run));

        java.awt.event.ActionListener enter = e -> search.run();
        for (JTextField f : new JTextField[]{name, lvlMin, lvlMax, distMax, extra}) f.addActionListener(enter);
        type.addActionListener(enter); sort.addActionListener(enter); desc.addActionListener(enter); vein.addActionListener(enter); noted.addActionListener(enter);
        limit.addChangeListener(e -> search.run());

        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                if (col < 0) return;
                String head = String.valueOf(table.getColumnName(col)), key = null;
                for (String[] hk : HEADER_KEYS) if (hk[0].equals(head)) key = hk[1];
                if (key == null || !keys.contains(key)) return;
                if (key.equals(sort.getSelectedItem())) desc.setSelected(!desc.isSelected()); else { sort.setSelectedItem(key); desc.setSelected(false); }
                search.run();
            }
        });
        // a place with a note: its name and note in gold; cities and sects stand out by type
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isSel, foc, r, c);
                if (!isSel) {
                    comp.setBackground(Theme.FIELD);
                    String head = String.valueOf(t.getColumnName(c));
                    int noteCol = idx(t, "Note"), typeCol = idx(t, "Type");
                    boolean hasNote = noteCol >= 0 && !String.valueOf(t.getValueAt(r, noteCol)).isEmpty();
                    String ty = typeCol >= 0 ? String.valueOf(t.getValueAt(r, typeCol)) : "";
                    Color fg = Theme.TEXT;
                    if (head.equals("Name")) fg = hasNote ? Theme.GOLD : ty.equals("city") ? Theme.OK : ty.equals("dungeon") ? Theme.DANGER : Theme.TEXT;
                    if (head.equals("Note")) fg = Theme.GOLD;
                    if (head.equals("Coords") || head.equals("What")) fg = Theme.DIM;
                    comp.setForeground(fg);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                return comp;
            }
        });

        final Runnable details = () -> {
            Area a = selected.get();
            if (a == null) return;
            if (a.city || a.sect != null) GuiMore.placeWindow(mv, a); else mv.selectTile(a.x, a.y);
        };
        table.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) details.run(); } });
        table.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "openPlace");
        table.getActionMap().put("openPlace", new AbstractAction() { @Override public void actionPerformed(java.awt.event.ActionEvent e) { details.run(); } });

        JPanel row1 = GuiWindows.flow(FlowLayout.LEFT), row2 = GuiWindows.flow(FlowLayout.LEFT);
        row1.add(mv.makeBtn("Go there", "Walk to this place (a walk ends when you arrive, an alert comes, or something attacks).", () -> { Area a = selected.get(); if (a != null) mv.queueInput("goto " + a.x * 100 + " " + a.y * 100); }));
        row1.add(mv.makeBtn("Route", "Show the way and the number of turns, without walking.", () -> { Area a = selected.get(); if (a != null) mv.queueInput("route " + a.x * 100 + " " + a.y * 100); }));
        row1.add(mv.makeBtn("Add waypoint quest", "Pin a quest that walks to this place.", () -> { Area a = selected.get(); if (a != null) mv.queueInput("qnew " + a.name.replace(' ', '_') + " " + a.x * 100 + " " + a.y * 100 + " 70"); }));
        row1.add(mv.makeBtn("Details", "A city or sect: its sheet. Any other place: select its tile on the map (double-click a row does the same).", details));
        row1.add(mv.makeBtn("Note…", "Mark this place with a short note (it stays in your notes and shows in gold here).", () -> {
            Area a = selected.get();
            if (a == null) return;
            String text = JOptionPane.showInputDialog(mv, "Note for " + a.name + " " + PlayerControl.coord(a) + ":", "Note", JOptionPane.PLAIN_MESSAGE);
            if (text == null || text.trim().isEmpty()) return;
            mv.queueInput("note tile " + a.x + " " + a.y + " " + text.trim());
            status.setText("Noted " + a.name + ".");
            search.run();
        }));
        row1.add(mv.makeBtn("Remove note", "Delete the note of this place.", () -> {
            Area a = selected.get();
            if (a == null) return;
            mv.queueInput("unnote tile " + a.x + " " + a.y);
            status.setText("Note removed from " + a.name + ".");
            search.run();
        }));

        row2.add(mv.makeBtn("Refresh", "Reload the list now.", search));
        row2.add(mv.makeBtn("Set as default limit", "Make this limit the number of rows every list shows (the 'limit' command).", () -> mv.queueInput("limit " + limit.getValue())));
        row2.add(mv.makeBtn("Export CSV", "Write the rows shown to a CSV file in the exports folder.", () -> {
            if (current[0] == null) { status.setText("Nothing to export yet."); return; }
            try { status.setText("Exported to " + Exporter.view("places", current[0]).getPath()); } catch (Exception ex) { status.setText("Export failed: " + ex.getMessage()); }
        }));
        if (!inBrowser) row2.add(mv.makeBtn("Open in Browser", "The same list in the Browser window, with its other tabs.", () -> mv.openBrowser("Places")));

        JPanel actions = new JPanel(new GridLayout(0, 1));
        actions.setBackground(GuiWindows.BG);
        actions.add(row1); actions.add(row2);
        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(GuiWindows.BG);
        south.add(actions, BorderLayout.CENTER);
        south.add(status, BorderLayout.SOUTH);

        panel.add(filters, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(Theme.FIELD);
        panel.add(sp, BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private static int idx(JTable t, String col) {
        for (int i = 0; i < t.getColumnCount(); i++) if (col.equals(t.getColumnName(i))) return i;
        return -1;
    }
}
