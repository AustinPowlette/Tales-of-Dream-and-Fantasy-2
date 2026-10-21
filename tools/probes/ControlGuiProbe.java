package Game;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;
import java.util.*;
import java.util.List;

/** 2026-10-14 (GUI, needs a display): the new windows of change sets 8-10 used by their real controls. The Control window's Priorities tab sets and removes a priority rule and the breakthrough policy; the Quest Log's
 *  Always ▾ and New quest… buttons exist; the New quest window makes a quest by hand (a person by id, a place, a spot quest) through `qadd`; the Craft window crafts from a chosen stack. */
public class ControlGuiProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    static void check(String n, boolean ok) { PcProbeGui.check(n, ok); }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "false");
        PcProbeGui.real = System.out;
        PcProbe.real = PcProbeGui.real;
        System.setIn(PcProbe.in);
        GameDriver.frozenNpcs = true;
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(PcProbeGui.real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        Thread w = new Thread(PcProbeGui::watcher, "dialog-watcher"); w.setDaemon(true); w.start();
        PcProbeGui.SHEETS.clear();
        PcProbeGui.IGNORE.addAll(Arrays.asList("Control", "Quest Log", "New quest", "Craft"));
        try { run(); } catch (Throwable e) { PcProbeGui.real.println("[G] PROBE EXCEPTION " + e); e.printStackTrace(PcProbeGui.real); PcProbeGui.fails++; }
        PcProbeGui.real.println("[G] ===== " + PcProbeGui.passes + " passed, " + PcProbeGui.fails + " failed; gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }

    static boolean until(java.util.function.BooleanSupplier c, long ms) throws Exception {
        long end = System.currentTimeMillis() + ms;
        while (System.currentTimeMillis() < end) { if (c.getAsBoolean()) return true; Thread.sleep(50); }
        return c.getAsBoolean();
    }
    static JDialog find(String title) {
        for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && ((JDialog) w).getTitle() != null && ((JDialog) w).getTitle().startsWith(title)) return (JDialog) w;
        return null;
    }
    static JTable table(Container c) { final JTable[] r = {null}; try { SwingUtilities.invokeAndWait(() -> { List<JTable> l = new ArrayList<>(); PcProbeGui.collect(c, JTable.class, l); if (!l.isEmpty()) r[0] = l.get(0); }); } catch (Exception e) { } return r[0]; }
    static void press(Container c, String label) throws Exception { SwingUtilities.invokeAndWait(() -> { JButton b = PcProbeGui.findButton(c, label); if (b != null) b.doClick(); }); }
    static String buttons(Container c) {
        final String[] r = {""};
        try { SwingUtilities.invokeAndWait(() -> { List<JButton> bs = new ArrayList<>(); PcProbeGui.collect(c, JButton.class, bs); StringBuilder sb = new StringBuilder(); for (JButton b : bs) sb.append("[").append(b.getText()).append("]"); r[0] = sb.toString(); }); } catch (Exception e) { }
        return r[0];
    }
    static int rowOf(JTable t, String name) { for (int i = 0; t != null && i < t.getRowCount(); i++) if (name.equals(String.valueOf(t.getValueAt(i, 0))) || String.valueOf(t.getValueAt(i, 1)).equals(name)) return i; return -1; }
    static Character npc(Area where, String name) {
        Character c = new Character(where);
        c.age = 30; c.gender = "Male"; c.lvl = 5; c.majorLvl = 0; c.maxAge = 100000; c.caution = 100000; c.sect = null; c.trait = Character.Traits.Friendly; c.name = name; c.lastName = "Guiprobe";
        while (Map.characters.size() < c.id) Map.characters.add(null);
        Map.characters.add(c); Map.population++;
        return c;
    }

    static void run() throws Exception {
        Settings.lore = false;
        PcProbe.in.feed("y\ns\nn\n");
        long end = System.currentTimeMillis() + Long.getLong("gui.waitMs", 240000L);
        while (System.currentTimeMillis() < end && !(GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null)) Thread.sleep(200);
        check("GUI opened and the player is prompting", GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null);
        final Character p = PcProbeGui.pc();
        if (p == null) return;
        final MapViewer mv = GameDriver.mapViewer;
        p.maxAge = 100000;
        final PlayerControl pc = p.pc();
        final Character friend = npc(p.location, "Guifriend");

        // ---- G1: the Control window's Priorities tab
        SwingUtilities.invokeLater(() -> GuiDuties.open(mv, "Priorities"));
        check("G1 the Control window opens on the Priorities tab", until(() -> find("Control") != null, 8000));
        final JDialog cw = find("Control");
        if (cw == null) return;
        Thread.sleep(2200);
        JTable types = table(cw.getContentPane());
        int fr = rowOf(types, "Forage");
        check("G1b the table lists the quest types (" + (types == null ? 0 : types.getRowCount()) + ") with Forage among them", types != null && types.getRowCount() > 60 && fr >= 0);
        final int frow = fr;
        SwingUtilities.invokeAndWait(() -> types.setRowSelectionInterval(frow, frow));
        press(cw.getContentPane(), "Set rule");
        check("G1c 'Set rule' for the selected type makes a rule", until(() -> pc.prioRules != null && pc.prioRules.containsKey("Forage|"), 15000));
        Thread.sleep(1800);
        check("G1d the rule shows in the table's Your rule column", String.valueOf(types.getValueAt(frow, 2)).matches("\\d+"));
        press(cw.getContentPane(), "Clear rule");
        check("G1e 'Clear rule' removes it", until(() -> pc.prioRules == null || !pc.prioRules.containsKey("Forage|"), 15000));
        // the breakthrough policy combo
        final JComboBox<?>[] bt = {null};
        SwingUtilities.invokeAndWait(() -> { List<JComboBox> l = new ArrayList<>(); PcProbeGui.collect(cw.getContentPane(), JComboBox.class, l); for (JComboBox c : l) if (c.getItemCount() == 3 && String.valueOf(c.getItemAt(0)).startsWith("ask me")) bt[0] = c; });
        check("G1f the Priorities tab has the breakthrough policy", bt[0] != null);
        if (bt[0] != null) {
            SwingUtilities.invokeAndWait(() -> bt[0].setSelectedIndex(2));
            check("G1g choosing 'wait for the Breakthrough button' sets the policy", until(() -> pc.breakthroughPolicy == PlayerControl.BT_BUTTON, 15000));
            SwingUtilities.invokeAndWait(() -> bt[0].setSelectedIndex(0));
            until(() -> pc.breakthroughPolicy == PlayerControl.BT_ASK, 15000);
        }
        SwingUtilities.invokeAndWait(cw::dispose);

        // ---- G2: the Quest Log buttons
        SwingUtilities.invokeLater(() -> GuiQuests.open(mv, null));
        check("G2 the Quest Log opens", until(() -> find("Quest Log") != null, 8000));
        JDialog ql = find("Quest Log");
        Thread.sleep(1500);
        String qb = ql == null ? "" : buttons(ql.getContentPane());
        check("G2b it has the Always ▾ and New quest… buttons", qb.contains("[Always ▾]") && qb.contains("[New quest…]"));
        if (ql != null) SwingUtilities.invokeAndWait(ql::dispose);

        // ---- G3: the New quest window
        SwingUtilities.invokeLater(() -> GuiNewQuest.open(mv, "Find Person"));
        check("G3 the New quest window opens", until(() -> find("New quest") != null, 8000));
        final JDialog nq = find("New quest");
        if (nq == null) return;
        Thread.sleep(2200);
        JTable nt = table(nq.getContentPane());
        check("G3b it lists the types (" + (nt == null ? 0 : nt.getRowCount()) + ") and has the type preselected", nt != null && nt.getRowCount() > 60 && nt.getSelectedRow() >= 0 && String.valueOf(nt.getValueAt(nt.getSelectedRow(), 0)).equals("Find Person"));
        // a person by id
        SwingUtilities.invokeAndWait(() -> { List<JTextField> l = new ArrayList<>(); PcProbeGui.collect(nq.getContentPane(), JTextField.class, l); for (JTextField f : l) if (f.getToolTipText() != null && f.getToolTipText().startsWith("A person")) f.setText("#" + friend.id); });
        press(nq.getContentPane(), "Create");
        check("G3c Create makes a pinned Find Person quest about the friend", until(() -> { for (Quest q : p.targetQuests) if (q.name.equals("Find Person") && q.targetCharacter == friend.id && q.userCreated && q.pinned) return true; return false; }, 15000));
        // a spot quest
        final int mrow = rowOf(nt, "Meditate");
        SwingUtilities.invokeAndWait(() -> nt.setRowSelectionInterval(mrow, mrow));
        press(nq.getContentPane(), "Create");
        check("G3d a spot quest (Meditate) is made too", until(() -> { for (Quest q : p.spotQuests) if (q.name.equals("Meditate") && q.userCreated) return true; return false; }, 15000));
        // the game's own type is refused
        final int srow = rowOf(nt, "Sect Mission");
        int before = p.targetQuests.size() + p.spotQuests.size();
        SwingUtilities.invokeAndWait(() -> nt.setRowSelectionInterval(srow, srow));
        press(nq.getContentPane(), "Create");
        Thread.sleep(800);
        check("G3e a quest the game makes itself (Sect Mission) is refused", p.targetQuests.size() + p.spotQuests.size() == before);
        SwingUtilities.invokeAndWait(nq::dispose);

        // ---- G4: the Craft window
        p.trait = Character.Traits.Blacksmith; p.professionLvl = 40; p.majorLvl = 5; p.materialBag.clear();
        p.materialBag.add(new Item(Item.ItemType.Metal, 5, 1200));
        SwingUtilities.invokeLater(() -> GuiCraft.open(mv));
        check("G4 the Craft window opens", until(() -> find("Craft") != null, 8000));
        final JDialog cr = find("Craft");
        if (cr == null) return;
        Thread.sleep(2200);
        JTable ct = table(cr.getContentPane());
        check("G4b it lists the metal stack (" + (ct == null ? 0 : ct.getRowCount()) + " rows)", ct != null && ct.getRowCount() == 1);
        SwingUtilities.invokeAndWait(() -> ct.setRowSelectionInterval(0, 0));
        final int weapons0 = (int) p.backpack.stream().filter(i -> i.itemType == Item.ItemType.Weapon).count();
        press(cr.getContentPane(), "Craft");
        check("G4c Craft makes a weapon from that stack (500 units used)", until(() -> p.backpack.stream().filter(i -> i.itemType == Item.ItemType.Weapon).count() == weapons0 + 1 && p.materialBag.get(0).quantity == 700, 20000));
        SwingUtilities.invokeAndWait(cr::dispose);
        PcProbeGui.real.println("[G] DONE");
    }
}
