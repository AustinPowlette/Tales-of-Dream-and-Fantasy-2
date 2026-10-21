package Game;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** The New quest window: every type of quest a person can have (`PlayerControl.QUEST_TYPES`), made by hand. Left: a searchable table of the types (what each is about, where the game starts its priority, your rule
 *  for it); right: the target (a person by id, name or from the people you know; a place by x y, from the places you know, the map tile or here; nothing for a quest done where you stand), the priority (the game's
 *  number or your own rule), repeat, and Create / Create and go. It only sends `qadd ...`, so the window and the command line are one path (`PlayerControl.createQuest`). */
final class GuiNewQuest {
    private GuiNewQuest() { }

    static void open(MapViewer mv, String preselect) {
        JDialog d = GuiWindows.window(mv, "newquest", "New quest — make one by hand", 920, 560, 3000, (dlg, refreshers) -> build(mv, dlg, refreshers));
        if (d == null || preselect == null) return;
        Object sel = d.getRootPane().getClientProperty("select");
        if (sel instanceof java.util.function.Consumer) { @SuppressWarnings("unchecked") java.util.function.Consumer<String> c = (java.util.function.Consumer<String>) sel; c.accept(preselect); }
    }

    private static JComponent build(final MapViewer mv, final JDialog dlg, final List<Runnable> refreshers) {
        final Color BG = GuiWindows.BG;
        final DefaultTableModel model = GuiWindows.model();
        final JTable table = GuiWindows.table(model);
        final JTextField search = new JTextField(14);
        final List<String> shown = new ArrayList<>();
        final String[] rules = {""};

        // ---- the target cards
        final CardLayout cards = new CardLayout();
        final JPanel target = new JPanel(cards);
        target.setBackground(BG);
        final JTextField who = new JTextField(14);
        who.setToolTipText("A person: #<id>, an id number, or part of a name (the people you know first)");
        final JComboBox<String> known = new JComboBox<>();
        final List<Integer> knownIds = new ArrayList<>();
        JPanel person = new JPanel(new GridLayout(0, 1, 4, 4)); person.setBackground(BG);
        person.add(GuiWindows.label("Who (id or name):")); person.add(who);
        person.add(GuiWindows.label("or one you know:")); person.add(known);
        final JTextField px = new JTextField(5), py = new JTextField(5);
        final JComboBox<String> places = new JComboBox<>();
        final List<Area> placeRefs = new ArrayList<>();
        JPanel place = new JPanel(new GridLayout(0, 1, 4, 4)); place.setBackground(BG);
        JPanel xy = GuiWindows.flow(FlowLayout.LEFT); xy.add(GuiWindows.label("x")); xy.add(px); xy.add(GuiWindows.label("y")); xy.add(py);
        xy.add(mv.makeBtn("Map tile", "Fill x and y with the tile selected on the map.", () -> { int[] s = mv.selectedTile(); if (s != null) { px.setText(String.valueOf(s[0])); py.setText(String.valueOf(s[1])); } }));
        xy.add(mv.makeBtn("Here", "Where you stand.", () -> { px.setText(""); py.setText(""); places.setSelectedIndex(0); }));
        place.add(GuiWindows.label("Where (blank = here):")); place.add(xy);
        place.add(GuiWindows.label("or a place you know:")); place.add(places);
        JPanel spot = new JPanel(new BorderLayout()); spot.setBackground(BG);
        spot.add(GuiWindows.label("<html>It is done where you stand:<br>no target.</html>"), BorderLayout.NORTH);
        target.add(person, "person"); target.add(place, "place"); target.add(spot, "spot");

        final JSpinner pri = new JSpinner(new SpinnerNumberModel(50, 0, 100, 5));
        final JCheckBox repeat = new JCheckBox("repeat");
        repeat.setBackground(BG); repeat.setForeground(GuiWindows.FG);
        repeat.setToolTipText("The quest stays after it is done (it goes back to its priority) instead of ending");
        final JLabel info = GuiWindows.label(" "), status = GuiWindows.label(" ");
        info.setForeground(GuiWindows.ACCENT); status.setForeground(Theme.OK);

        final java.util.function.Supplier<String> typeOf = () -> { int r = table.getSelectedRow(); return r >= 0 && r < shown.size() ? shown.get(r) : null; };
        final Runnable onSelect = () -> {
            String t = typeOf.get();
            if (t == null) { info.setText(" "); return; }
            String about = PlayerControl.aboutOf(t);
            cards.show(target, about);
            boolean managed = PlayerControl.MANAGED_TYPES.contains(t);
            info.setText(managed ? t + " is given by the game; it cannot be made by hand." : t + " — " + (about.equals("person") ? "about a person" : about.equals("place") ? "about a place" : "done where you stand"));
            int def = PlayerCommands.defaultPriority(t);
            try { Character p = mv.findPlayer(); Integer rule = p != null && p.pc != null && p.pc.prioRules != null ? p.pc.prioRules.get(t + "|") : null; pri.setValue(rule != null ? rule : def); } catch (RuntimeException e) { pri.setValue(def); }
        };
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) onSelect.run(); });

        final java.util.function.Consumer<Boolean> create = go -> {
            String t = typeOf.get();
            if (t == null) { status.setForeground(Theme.DANGER); status.setText("Choose a type first."); return; }
            if (PlayerControl.MANAGED_TYPES.contains(t)) { status.setForeground(Theme.DANGER); status.setText(t + " is given by the game."); return; }
            String about = PlayerControl.aboutOf(t), tgt = "";
            if (about.equals("person")) {
                String w = who.getText().trim();
                if (!w.isEmpty()) tgt = w.matches("\\d+") ? " #" + w : w.startsWith("#") ? " " + w : " " + w;
                else if (known.getSelectedIndex() > 0 && known.getSelectedIndex() - 1 < knownIds.size()) tgt = " #" + knownIds.get(known.getSelectedIndex() - 1);
                else { status.setForeground(Theme.DANGER); status.setText("Say who: an id, a name, or pick one you know."); return; }
            } else if (about.equals("place")) {
                if (!px.getText().trim().isEmpty() && !py.getText().trim().isEmpty()) tgt = " at " + px.getText().trim() + " " + py.getText().trim();
                else if (places.getSelectedIndex() > 0 && places.getSelectedIndex() - 1 < placeRefs.size()) { Area a = placeRefs.get(places.getSelectedIndex() - 1); tgt = " at " + a.x + " " + a.y; }
                else tgt = " here";
            }
            mv.queueInput("qadd " + t + tgt + " p=" + pri.getValue() + (repeat.isSelected() ? " repeat" : "") + (go ? " go" : ""));
            status.setForeground(Theme.OK); status.setText("Sent: " + t + tgt + " (the Quest Log shows it)");
        };

        JPanel buttons = GuiWindows.flow(FlowLayout.LEFT);
        buttons.add(GuiWindows.label("priority")); buttons.add(pri); buttons.add(repeat);
        buttons.add(mv.makeBtn("Create", "Add the quest to your Quest Log, pinned at this priority.", () -> create.accept(false)));
        buttons.add(mv.makeBtn("Create and go", "Add it and walk to its place (or chase its person) at once.", () -> create.accept(true)));

        JPanel right = new JPanel(new BorderLayout(6, 6));
        right.setBackground(BG);
        right.setPreferredSize(new Dimension(330, 100));
        right.add(info, BorderLayout.NORTH);
        right.add(target, BorderLayout.CENTER);
        JPanel rs = new JPanel(new BorderLayout()); rs.setBackground(BG);
        rs.add(buttons, BorderLayout.NORTH); rs.add(status, BorderLayout.SOUTH);
        right.add(rs, BorderLayout.SOUTH);

        final Runnable fill = () -> {
            String q = search.getText().trim().toLowerCase();
            List<Object[]> rows = new ArrayList<>();
            shown.clear();
            Character p = mv.findPlayer();
            for (String[] t : PlayerControl.QUEST_TYPES) {
                if (!q.isEmpty() && !t[0].toLowerCase().contains(q)) continue;
                shown.add(t[0]);
                Integer rule = p != null && p.pc != null && p.pc.prioRules != null ? p.pc.prioRules.get(t[0] + "|") : null;
                String about = PlayerControl.MANAGED_TYPES.contains(t[0]) ? "the game's" : PlayerControl.aboutOf(t[0]);
                rows.add(new Object[]{t[0], about, Integer.parseInt(t[1]), rule == null ? "" : String.valueOf(rule)});
            }
            String sel = typeOf.get();
            model.setDataVector(rows.toArray(new Object[0][]), new String[]{"Quest type", "About", "Starts at", "Your rule"});
            if (sel != null) for (int i = 0; i < shown.size(); i++) if (shown.get(i).equals(sel)) { table.setRowSelectionInterval(i, i); break; }
        };
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { fill.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { fill.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { fill.run(); }
        });
        dlg.getRootPane().putClientProperty("select", (java.util.function.Consumer<String>) name -> { search.setText(""); fill.run(); for (int i = 0; i < shown.size(); i++) if (shown.get(i).equalsIgnoreCase(name)) { table.setRowSelectionInterval(i, i); break; } });

        refreshers.add(() -> {
            Character p = GuiWindows.player(mv);
            if (p == null) return;
            if (known.getItemCount() == 0) { // the people you know and the places you know, once (they change slowly)
                known.addItem("(choose)");
                List<Relationship> rel = new ArrayList<>(p.relationshipList.relationships);
                rel.sort((a, b) -> Integer.compare(Math.abs(b.value), Math.abs(a.value)));
                for (Relationship r : rel) {
                    Character o = Character.charById(r.friendID);
                    if (o == null || o.dead || o == p) continue;
                    known.addItem(o.name + " " + o.lastName + " (" + PlayerLists.relationOf(p, o, r.value) + ", #" + o.id + ")"); knownIds.add(o.id);
                    if (knownIds.size() >= 150) break;
                }
                places.addItem("(here)");
                List<Area> ps = new ArrayList<>(p.knownLocations);
                ps.sort((a, b) -> Integer.compare(PlayerControl.distance(p.location, a), PlayerControl.distance(p.location, b)));
                for (Area a : ps) { if (a == null || a == p.location) continue; places.addItem(a.name + " " + PlayerControl.coord(a) + " (" + PlayerLists.placeType(a) + ")"); placeRefs.add(a); if (placeRefs.size() >= 200) break; }
            }
            fill.run();
        });

        JPanel left = new JPanel(new BorderLayout(4, 4));
        left.setBackground(BG);
        JPanel top = GuiWindows.flow(FlowLayout.LEFT);
        top.add(GuiWindows.label("find:")); top.add(search);
        left.add(top, BorderLayout.NORTH);
        left.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(BG);
        root.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        root.add(left, BorderLayout.CENTER);
        root.add(right, BorderLayout.EAST);
        return root;
    }
}
