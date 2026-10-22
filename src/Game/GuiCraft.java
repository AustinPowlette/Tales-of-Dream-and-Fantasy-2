package Game;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** The Craft window: the crafting menu of a trade (`CraftMenu`). One row per stack the maker can use, with what it makes at his skill; choose a row (and the kind of weapon, or how many pills), press Craft.
 *  Every press sends `craft do <n> ...`, one turn each; "Craft x5" queues five. */
final class GuiCraft {
    private GuiCraft() { }

    static void open(MapViewer mv) {
        GuiWindows.window(mv, "craft", "Craft — what to make", 860, 500, 1500, (d, refreshers) -> build(mv, refreshers));
    }

    private static JComponent build(final MapViewer mv, final List<Runnable> refreshers) {
        final Color BG = GuiWindows.BG;
        final DefaultTableModel model = GuiWindows.model();
        final JTable table = GuiWindows.table(model);
        final JLabel head = GuiWindows.label(" "), note = GuiWindows.label(" ");
        head.setForeground(GuiWindows.ACCENT); note.setForeground(Theme.DANGER);
        final JComboBox<String> weapon = new JComboBox<>(new String[]{"random kind", "sword", "saber", "staff", "spear", "fan"});
        final JSpinner count = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        final JLabel weaponLbl = GuiWindows.label("weapon:"), countLbl = GuiWindows.label("pills:");
        final List<CraftMenu.Option> shown = new ArrayList<>();
        final String[] trade = {""};

        final java.util.function.IntConsumer craft = times -> {
            int r = table.getSelectedRow();
            if (r < 0 || r >= shown.size()) { note.setText("Select a row first."); return; }
            CraftMenu.Option o = shown.get(r);
            String arg = "";
            if (o.kind.equals("weapon") && weapon.getSelectedIndex() > 0) arg = " " + weapon.getSelectedItem();
            if (o.kind.equals("pill") || o.kind.equals("potential")) arg = " " + count.getValue();
            for (int i = 0; i < times; i++) mv.queueInput("craft do " + (r + 1) + arg);
        };
        JPanel actions = GuiWindows.flow(FlowLayout.LEFT);
        actions.add(weaponLbl); actions.add(weapon); actions.add(countLbl); actions.add(count);
        actions.add(mv.makeBtn("Craft", "Make it (one turn).", () -> craft.accept(1)));
        actions.add(mv.makeBtn("Craft ×5", "Make it five times (five turns, one after the other; an alert or an attack interrupts).", () -> craft.accept(5)));
        actions.add(mv.makeBtn("Profession turn", "Let the game choose (the Profession button): the best stack your skill can use.", () -> mv.queueInput("profession")));
        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            note.setText(r >= 0 && r < shown.size() && shown.get(r).warn != null ? shown.get(r).warn : " ");
        });

        refreshers.add(() -> {
            Character p = GuiWindows.player(mv);
            if (p == null) return;
            List<CraftMenu.Option> opts = CraftMenu.options(p);
            shown.clear(); shown.addAll(opts);
            List<Object[]> rows = new ArrayList<>();
            for (int i = 0; i < opts.size(); i++) {
                CraftMenu.Option o = opts.get(i);
                rows.add(new Object[]{i + 1, o.what, o.material(), o.stack.quantity, o.kind.equals("potential") ? "potential" : o.makes(), o.upTo, o.warn == null ? "" : o.warn});
            }
            int sel = table.getSelectedRow();
            model.setDataVector(rows.toArray(new Object[0][]), new String[]{"#", "Makes", "From", "Units", "Level", "Up to", "Note"});
            if (table.getColumnCount() > 0) table.getColumnModel().getColumn(0).setMaxWidth(36);
            if (sel >= 0 && sel < rows.size()) table.setRowSelectionInterval(sel, sel);
            boolean smith = p.trait == Character.Traits.Blacksmith, alch = p.trait == Character.Traits.Alchemist;
            weapon.setVisible(smith); weaponLbl.setVisible(smith); count.setVisible(alch); countLbl.setVisible(alch);
            trade[0] = p.getTrait();
            head.setText(CraftMenu.hasMenu(p) ? trade[0] + " — profession Lv." + p.professionLvl + ", items up to Lv." + Math.min((p.majorLvl + 1) * 10, 100) + " (your realm). The level follows the material: pick the best stack."
                    : "Your trade (" + trade[0] + ") makes things without a choice; the Profession button does a turn of it. The Profession menu lists what anybody can make as an amateur.");
        });
        JPanel root = new JPanel(new BorderLayout(6, 6));
        root.setBackground(BG);
        root.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        root.add(head, BorderLayout.NORTH);
        root.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(BG);
        south.add(actions, BorderLayout.NORTH);
        south.add(note, BorderLayout.SOUTH);
        root.add(south, BorderLayout.SOUTH);
        return root;
    }
}
