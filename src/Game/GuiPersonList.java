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

/** The one list of people. It replaces three that had grown apart (People here, Browser > Cultivators / Monsters, Browser > Search anyone): the same filters, sort by clicking a header, colours,
 *  double-click opens the person's sheet, the same button bar, one Follow / Unfollow toggle, a Seek menu, and (for the people in your tile) the card and portrait of the selected row.
 *  Three uses: HERE (the cultivators in your tile), MONSTERS (the monsters in your tile) and WORLD (anyone, searched on demand). Every action is sent as a command, so nothing here touches game state. */
final class GuiPersonList {
    private GuiPersonList() { }

    enum Kind { HERE, MONSTERS, WORLD, KNOWN, FAMILY }

    private static final String[][] HEADER_KEYS = {{"ID", "id"}, {"Name", "name"}, {"Lv", "lvl"}, {"Major", "major"}, {"Age", "age"}, {"Aff", "aff"}, {"Trait", "trait"}, {"Sect", "sect"}, {"Rel", "rel"}, {"Where", "where"}, {"Dist", "dist"}, {"Wealth", "wealth"}, {"Relation", "relation"}};

    /** `inBrowser`: the list sits in the Browser window (no "Open in Browser" button). One refresher is added to `refreshers`. */
    static JComponent create(final MapViewer mv, final Kind kind, final List<Runnable> refreshers, final boolean inBrowser) {
        final boolean monsters = kind == Kind.MONSTERS, world = kind == Kind.WORLD, known = kind == Kind.KNOWN || kind == Kind.FAMILY, here = !world && !known; // here: the people of your tile (with the card); world: a search on demand; known: the people you have a bond with, near or far
        final JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBackground(GuiWindows.BG);

        // ---- filters
        final JTextField name = new JTextField(world ? 12 : 9);
        name.setToolTipText(world ? "Part of a name, or an id number; press Enter to search the whole world" : "Part of a name; press Enter");
        final JTextField lvlMin = new JTextField(3), lvlMax = new JTextField(3), extra = new JTextField(12);
        extra.setToolTipText("More filters in the list language, e.g. trait=Alchemist sect=none (see 'help list')");
        final JComboBox<String> aff = new JComboBox<>(new String[]{"any", "good", "neutral", "evil"});
        final java.util.Set<String> keys = world ? PlayerLists.WORLD_KEYS : known ? PlayerLists.KNOWN_KEYS : PlayerLists.PEOPLE_KEYS;
        final JComboBox<String> sort = new JComboBox<>();
        sort.addItem("(default)");
        for (String k : keys) sort.addItem(k);
        final JCheckBox desc = new JCheckBox("desc");
        desc.setBackground(GuiWindows.BG); desc.setForeground(GuiWindows.FG);
        Character p0 = mv.findPlayer();
        final JSpinner limit = new JSpinner(new SpinnerNumberModel(Math.max(1, Math.min(ListQuery.MAX_LIMIT, world ? 25 : p0 != null && p0.pc != null ? p0.pc.listLimit : 20)), 1, ListQuery.MAX_LIMIT, 5)); // (the default limit of every list: 'Set as default limit' changes it)
        final JLabel status = GuiWindows.label(" ");
        status.setForeground(GuiWindows.ACCENT);
        final JComboBox<String> show = new JComboBox<>(new String[]{"everyone", "friends", "rivals and enemies"});
        JPanel filters = GuiWindows.flow(FlowLayout.LEFT);
        if (kind == Kind.KNOWN) { filters.add(GuiWindows.label("show")); filters.add(show); }
        filters.add(GuiWindows.label(world ? "name or #id:" : "name:")); filters.add(name);
        filters.add(GuiWindows.label("lvl ≥")); filters.add(lvlMin); filters.add(GuiWindows.label("≤")); filters.add(lvlMax);
        if (!monsters) { filters.add(GuiWindows.label("aff")); filters.add(aff); }
        filters.add(GuiWindows.label("extra")); filters.add(extra);
        filters.add(GuiWindows.label("sort")); filters.add(sort); filters.add(desc);
        filters.add(GuiWindows.label("limit")); filters.add(limit);

        // ---- table, card
        final DefaultTableModel model = GuiWindows.model();
        final JTable table = GuiWindows.table(model);
        final JEditorPane card = here ? GuiSheets.view(mv) : null;
        final JLabel portrait = new JLabel();
        portrait.setHorizontalAlignment(SwingConstants.CENTER);
        final PlayerLists.ListView[] current = {null};
        final String[] lastCard = {""};
        final boolean[] busy = {false};

        final java.util.function.Supplier<Character> selected = () -> {
            int r = table.getSelectedRow();
            if (r < 0 || current[0] == null || r >= current[0].refs.size()) { status.setText("Select a row first."); return null; }
            return (Character) current[0].refs.get(r);
        };
        final JButton[] followBtn = {null};
        final Runnable followLabel = () -> {
            int r = table.getSelectedRow();
            Character c = r >= 0 && current[0] != null && r < current[0].refs.size() ? (Character) current[0].refs.get(r) : null;
            if (followBtn[0] != null) followBtn[0].setText(c != null && Map.trackedCharacters.contains(c.id) ? "Unfollow" : "Follow");
        };

        final Runnable body = () -> {
            Character p = mv.findPlayer();
            if (p == null || p.location == null || p.pc == null) { status.setText("No player character."); return; }
            List<String> tok = new ArrayList<>();
            tok.add(String.valueOf(limit.getValue()));
            if (sort.getSelectedIndex() > 0) tok.add("sort=" + sort.getSelectedItem() + (desc.isSelected() ? ":desc" : ""));
            else if (desc.isSelected()) tok.add("desc");
            String s = name.getText().trim();
            if (world && s.matches("#?\\d+")) tok.add("id=" + s.replace("#", ""));
            else for (String w : s.split("\\s+")) if (!w.isEmpty()) tok.add("name=" + w);
            if (!lvlMin.getText().trim().isEmpty()) tok.add("lvl>=" + lvlMin.getText().trim());
            if (!lvlMax.getText().trim().isEmpty()) tok.add("lvl<=" + lvlMax.getText().trim());
            if (!monsters && aff.getSelectedIndex() > 0) tok.add("aff=" + aff.getSelectedItem());
            if (kind == Kind.KNOWN && show.getSelectedIndex() == 1) tok.add("rel>=1");
            if (kind == Kind.KNOWN && show.getSelectedIndex() == 2) tok.add("rel<=-1");
            for (String w : extra.getText().trim().split("\\s+")) if (!w.isEmpty()) tok.add(w);
            PlayerLists.ListView v;
            try {
                ListQuery q = ListQuery.parse(tok.toArray(new String[0]), 0);
                v = world ? PlayerLists.world(p, q, p.pc.listLimit) : known ? PlayerLists.known(p, q, kind == Kind.FAMILY, p.pc.listLimit) : PlayerLists.people(p, q, monsters, p.pc.listLimit);
            } catch (RuntimeException ex) { return; } // the game thread changed a list mid-copy: the next refresh will succeed
            if (v.error != null) { status.setText(v.error); return; }
            Object sel = null;
            int row = table.getSelectedRow();
            if (row >= 0 && current[0] != null && row < current[0].refs.size()) sel = current[0].refs.get(row);
            current[0] = v;
            model.setDataVector(v.rows.toArray(new Object[0][]), v.headers);
            if (table.getColumnCount() > 1) { table.getColumnModel().getColumn(0).setMaxWidth(36); }
            if (sel != null) for (int i = 0; i < v.refs.size(); i++) if (v.refs.get(i) == sel) { table.setRowSelectionInterval(i, i); break; }
            status.setText(known ? "showing " + v.rows.size() + " of " + v.total + (kind == Kind.FAMILY ? " relatives" : " people you know") + "   —   " + v.note : world ? "found " + v.total + (v.total > v.rows.size() ? " (showing " + v.rows.size() + ")" : "") + "   —   searches the whole world; press Enter to search"
                    : "showing " + v.rows.size() + " of " + v.total + (monsters ? " monsters" : " cultivators") + " in " + p.location.name + (monsters ? "   (wild beasts here: " + GuiCards.num(p.location.monsters) + ")" : ""));
            followLabel.run();
            if (here) {
                row = table.getSelectedRow();
                Character who = row >= 0 && row < v.refs.size() ? (Character) v.refs.get(row) : null;
                String html = who == null ? GuiSheets.page("<h1>" + (monsters ? "Monsters" : "People here") + "</h1><p class='dim'>Select a row to see who it is, then act on it below.</p>") : GuiCards.personCard(p, who);
                if (!html.equals(lastCard[0])) {
                    lastCard[0] = html; card.setText(html); card.setCaretPosition(0);
                    portrait.setIcon(who != null && Settings.pictures ? new ImageIcon(ArtMore.portraitCached(who, 130, 160)) : null);
                }
            }
        };
        final Runnable run = () -> { if (busy[0]) return; busy[0] = true; try { body.run(); } finally { busy[0] = false; } };
        final Runnable search = world ? () -> {
            Character p = mv.findPlayer();
            if (p == null || p.location == null) { status.setText("No player character."); return; }
            if (!World.readWait(600, run)) status.setText("The world is busy: try again in a moment.");
        } : () -> World.readSoon(200, run);
        refreshers.add(world ? () -> { /* a search of the whole world is only run on demand */ } : () -> World.read(80, run));

        java.awt.event.ActionListener enter = e -> search.run();
        for (JTextField f : new JTextField[]{name, lvlMin, lvlMax, extra}) f.addActionListener(enter);
        aff.addActionListener(enter); sort.addActionListener(enter); desc.addActionListener(enter); show.addActionListener(enter);
        limit.addChangeListener(e -> { if (!world) search.run(); });
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) { if (here) World.readSoon(200, run); else followLabel.run(); } });

        // click a header: sort by that column (again: the other way)
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

        // colours by column name: affiliation tints the name, quest targets are gold, the bond is green or red
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isSel, foc, r, c);
                if (!isSel) {
                    comp.setBackground(Theme.FIELD);
                    String head = String.valueOf(t.getColumnName(c));
                    int affCol = idx(t, "Aff"), notesCol = idx(t, "Notes");
                    String a = affCol >= 0 ? String.valueOf(t.getValueAt(r, affCol)) : "", notes = notesCol >= 0 ? String.valueOf(t.getValueAt(r, notesCol)) : "";
                    Color fg = Theme.TEXT;
                    if (head.equals("Name")) fg = notes.contains("quest target") ? Theme.GOLD : a.equalsIgnoreCase("evil") ? Theme.DANGER : a.equalsIgnoreCase("good") ? Theme.OK : Theme.TEXT;
                    if (head.equals("Rel")) { try { int rel = Integer.parseInt(String.valueOf(v)); fg = rel > 0 ? Theme.OK : rel < 0 ? Theme.DANGER : Theme.DIM; } catch (NumberFormatException ignored) { } }
                    comp.setForeground(fg);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                return comp;
            }
        });
        final Runnable open = () -> { Character c = selected.get(); if (c != null) mv.showCharacterDialog(c); };
        table.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) open.run(); } });
        table.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "openPerson");
        table.getActionMap().put("openPerson", new AbstractAction() { @Override public void actionPerformed(java.awt.event.ActionEvent e) { open.run(); } });

        // ---- the button bar (one for all three lists)
        final java.util.function.BiConsumer<Character, String> sameTile = (c, cmd) -> {
            Character me = mv.findPlayer();
            if (me == null) return;
            if (c.location != me.location) { status.setText(c.name + " is not in your tile (" + (c.location == null ? "?" : c.location.name) + "): use Seek to go to them, or Chase."); return; }
            mv.queueInput(cmd + " #" + c.id);
        };
        JPanel row1 = GuiWindows.flow(FlowLayout.LEFT), row2 = GuiWindows.flow(FlowLayout.LEFT);
        row1.add(mv.makeBtn("Open", "Open this person's sheet (double-click a row or press Enter does the same).", open));
        if (!monsters) {
            row1.add(mv.makeBtn("Talk", "Chat: may improve how they see you. Uses the turn. They must be in your tile.", () -> { Character c = selected.get(); if (c != null) sameTile.accept(c, "talk"); }));
            row1.add(mv.makeBtn("Spar", "A friendly fight: nobody dies. Uses the turn. They must be in your tile.", () -> { Character c = selected.get(); if (c != null) sameTile.accept(c, "spar"); }));
        }
        JButton atk = mv.makeBtn("Attack", "A real fight (your squad joins unless you set it otherwise). You are warned first if the odds are bad. They must be in your tile.", () -> { Character c = selected.get(); if (c != null) sameTile.accept(c, "attack"); });
        atk.setForeground(Theme.DANGER);
        row1.add(atk);
        if (monsters) row1.add(mv.makeBtn("Hunt here", "Fight the beasts of this tile one after another.", () -> mv.queueInput("hunt")));
        if (monsters) row1.add(mv.makeBtn("Seek: kill", "Pin a quest that hunts this monster down wherever it goes and asks what to do when you find it.", () -> { Character c = selected.get(); if (c != null) mv.queueInput("seek " + c.id + " kill"); }));
        else row1.add(mv.makeBtn("Invite to party", "Ask them to join your party (they may decline; asking costs no time). They must be in your tile.", () -> { Character c = selected.get(); if (c != null) sameTile.accept(c, "invite"); }));
        row1.add(mv.makeBtn("Preview fight", "Win chance, lethality and escape odds against them.", () -> { Character c = selected.get(); if (c != null) mv.queueInput("preview #" + c.id); }));
        row1.add(mv.makeBtn("Chase", "Walk after them, wherever they are, until you catch up (150 turns at most).", () -> { Character c = selected.get(); if (c != null) mv.queueInput("chase #" + c.id); }));
        followBtn[0] = mv.makeBtn("Follow", "Show them in the NPC tracker and on the map; the same button stops following.", () -> {
            Character c = selected.get();
            if (c == null) return;
            boolean tracked = Map.trackedCharacters.contains(c.id);
            mv.queueInput((tracked ? "unfollow #" : "follow #") + c.id);
            followBtn[0].setText(tracked ? "Follow" : "Unfollow");
            mv.trackerSoon();
            status.setText((tracked ? "No longer following " : "Following ") + c.name + (tracked ? "." : " (see the NPC Tracker)."));
        });
        row1.add(followBtn[0]);
        if (!monsters) {
            JButton ctl = mv.makeBtn("Control", "Take control of this person; your current cultivator carries on as an NPC.", () -> {
                Character c = selected.get();
                if (c == null) return;
                if (!GuiExtras.confirm(mv, "Take control of " + c.name + " " + c.lastName + " (Lv." + c.lvl + ")? Your current cultivator carries on as an NPC.")) return;
                mv.queueInput("!control #" + c.id);
            });
            ctl.setForeground(Theme.GOLD);
            row1.add(ctl);
            final JButton seek = mv.makeBtn("Seek ▾", "Pin a quest that goes to them wherever they are and does this when you meet.", () -> { });
            seek.addActionListener(e -> {
                Character c = selected.get(), me = mv.findPlayer();
                if (c == null) return;
                JPopupMenu menu = new JPopupMenu();
                String[][] kinds = {{"Befriend", "friend"}, {"Talk", "talk"}, {"Court", "court"}, {"Spar", "spar"}, {"Fight", "fight"}, {"Kill (hunt down)", "kill"}};
                for (String[] k : kinds) {
                    JMenuItem mi = new JMenuItem(k[0]);
                    if (k[1].equals("court") && (me == null || !QuestLife.suitor(me, c))) { mi.setEnabled(false); mi.setToolTipText("Courtship needs an unmarried adult of the other gender, not kin."); }
                    mi.addActionListener(ev -> mv.queueInput("seek " + c.id + " " + k[1]));
                    menu.add(mi);
                }
                menu.addSeparator();
                JMenuItem gift = new JMenuItem("Ask for a gift (a best friend or a relative)");
                gift.setToolTipText("They bring something from their own holdings when they stand in your tile.");
                gift.addActionListener(ev -> mv.queueInput("askgift #" + c.id));
                menu.add(gift);
                menu.show(seek, 0, seek.getHeight());
            });
            row1.add(seek);
        }
        row1.add(mv.makeBtn("Go there", "Walk to the tile where they stand now.", () -> { Character c = selected.get(); if (c != null && c.location != null) mv.queueInput("goto " + c.location.x + " " + c.location.y); }));
        row1.add(mv.makeBtn("Copy ID", "Copy the id number to the clipboard (for #id commands).", () -> { Character c = selected.get(); if (c != null) Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(String.valueOf(c.id)), null); }));

        JButton refreshBtn = mv.makeBtn(world ? "Search" : "Refresh", world ? "Search the whole world with these filters." : "Reload the list now.", search);
        if (world) refreshBtn.setForeground(Theme.OK);
        row2.add(refreshBtn);
        row2.add(mv.makeBtn("Set as default limit", "Make this limit the number of rows every list shows (the 'limit' command).", () -> mv.queueInput("limit " + limit.getValue())));
        row2.add(mv.makeBtn("Export CSV", "Write the rows shown to a CSV file in the exports folder.", () -> {
            if (current[0] == null) { status.setText("Nothing to export yet."); return; }
            try { status.setText("Exported to " + Exporter.view(kind == Kind.WORLD ? "search" : kind.name().toLowerCase(), current[0]).getPath()); } catch (Exception ex) { status.setText("Export failed: " + ex.getMessage()); }
        }));
        if (here && !inBrowser) row2.add(mv.makeBtn("Open in Browser", "The same list in the Browser window, with all its other tabs.", () -> mv.openBrowser(monsters ? "Monsters" : "Cultivators")));

        JPanel actions = new JPanel(new GridLayout(0, 1));
        actions.setBackground(GuiWindows.BG);
        actions.add(row1); actions.add(row2);
        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(GuiWindows.BG);
        south.add(actions, BorderLayout.CENTER);
        south.add(status, BorderLayout.SOUTH);

        panel.add(filters, BorderLayout.NORTH);
        if (here) {
            JPanel east = new JPanel(new BorderLayout());
            east.setBackground(GuiWindows.BG);
            east.add(portrait, BorderLayout.NORTH);
            JScrollPane cs = new JScrollPane(card);
            cs.getViewport().setBackground(Theme.BG);
            east.add(cs, BorderLayout.CENTER);
            east.setPreferredSize(new Dimension(300, 100));
            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(table), east);
            split.setResizeWeight(0.7); split.setContinuousLayout(true); split.setBackground(GuiWindows.BG);
            panel.add(split, BorderLayout.CENTER);
        } else panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private static int idx(JTable t, String head) {
        for (int i = 0; i < t.getColumnCount(); i++) if (head.equals(String.valueOf(t.getColumnName(i)))) return i;
        return -1;
    }
}
