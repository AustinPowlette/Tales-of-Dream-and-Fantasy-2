package Game;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;
import java.util.*;
import java.util.List;

/** 2026-10-13 (GUI, needs a display): the Squad & Sect window. It lists every member with its order and role and where it is, and its buttons give the orders of the selected member
 *  (Follow, Hold here, Fights first, Strongest first, Reserve, Kick) and choose what the squad does in your attacks and hunts. */
public class SquadGuiProbe {
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
        try { run(); } catch (Throwable e) { PcProbeGui.real.println("[G] PROBE EXCEPTION " + e); e.printStackTrace(PcProbeGui.real); PcProbeGui.fails++; }
        PcProbeGui.real.println("[G] ===== " + PcProbeGui.passes + " passed, " + PcProbeGui.fails + " failed; gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }

    static boolean until(java.util.function.BooleanSupplier c, long ms) throws Exception { long end = System.currentTimeMillis() + ms; while (System.currentTimeMillis() < end) { if (c.getAsBoolean()) return true; Thread.sleep(50); } return c.getAsBoolean(); }
    static JDialog squadWindow() { for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && "Squad & Sect".equals(((JDialog) w).getTitle())) return (JDialog) w; return null; }
    static JFrame frameOf(Window w) { return null; }

    static void run() throws Exception {
        Settings.lore = false;
        PcProbe.in.feed("y\ns\nn\n");
        long end = System.currentTimeMillis() + Long.getLong("gui.waitMs", 240000L);
        while (System.currentTimeMillis() < end && !(GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null)) Thread.sleep(200);
        check("GUI opened and the player is prompting", GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null);
        final Character p = PcProbeGui.pc();
        if (p == null) return;
        final MapViewer mv = GameDriver.mapViewer;
        p.lvl = 100; p.majorLvl = 10; p.maxAge = 100000; p.caution = 0;
        final List<Character> ms = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Character m = new Character(p.location);
            m.age = 30; m.gender = "Male"; m.lvl = 40 + i; m.majorLvl = 4; m.maxAge = 100000; m.trait = Character.Traits.Friendly; m.name = "Zed" + i; m.lastName = "Squadman";
            while (Map.characters.size() < m.id) Map.characters.add(null);
            Map.characters.add(m); Map.population++;
            m.joinParty(p); ms.add(m);
        }
        SwingUtilities.invokeLater(() -> GuiSheets.squadWindow(mv));
        check("G1 the Squad & Sect window opens", until(() -> squadWindow() != null, 8000));
        final JDialog d = squadWindow();
        if (d == null) return;
        Thread.sleep(2200); // the window refreshes on a timer
        final String[] txt = {""};
        SwingUtilities.invokeAndWait(() -> txt[0] = PcProbeGui.allText(d.getContentPane()));
        final JTable[] pt = {null};
        SwingUtilities.invokeAndWait(() -> { List<JTable> ts = new ArrayList<>(); PcProbeGui.collect(d.getContentPane(), JTable.class, ts); if (!ts.isEmpty()) pt[0] = ts.get(0); });
        check("G2 the squad table lists both members with order and fight role", pt[0] != null && pt[0].getRowCount() >= 3 && String.valueOf(pt[0].getValueAt(1, 2)).equals("Follow") && String.valueOf(pt[0].getValueAt(1, 3)).contains("strongest"));
        // Hold the first member
        SwingUtilities.invokeAndWait(() -> pt[0].setRowSelectionInterval(1, 1));
        SwingUtilities.invokeAndWait(() -> { JButton b = PcProbeGui.findButton(d.getContentPane(), "Hold here"); if (b != null) b.doClick(); });
        check("G3 'Hold here' puts the selected member on Hold", until(() -> ms.get(0).squadOrder == Character.ORDER_HOLD, 5000));
        SwingUtilities.invokeAndWait(() -> { JButton b = PcProbeGui.findButton(d.getContentPane(), "Fights first"); if (b != null) b.doClick(); });
        check("G4 'Fights first' sets the role", until(() -> ms.get(0).squadRole == Character.ROLE_FRONT, 5000));
        SwingUtilities.invokeAndWait(() -> { JButton b = PcProbeGui.findButton(d.getContentPane(), "Reserve"); if (b != null) b.doClick(); });
        check("G5 'Reserve' sets the role", until(() -> ms.get(0).squadRole == Character.ROLE_RESERVE, 5000));
        SwingUtilities.invokeAndWait(() -> { JButton b = PcProbeGui.findButton(d.getContentPane(), "Follow"); if (b != null) b.doClick(); });
        check("G6 'Follow' takes it off Hold", until(() -> ms.get(0).squadOrder == Character.ORDER_FOLLOW, 5000));
        // the fight policy combo
        final JComboBox<?>[] combo = {null};
        SwingUtilities.invokeAndWait(() -> { List<JComboBox> cs = new ArrayList<>(); PcProbeGui.collect(d.getContentPane(), JComboBox.class, cs); if (!cs.isEmpty()) combo[0] = cs.get(0); });
        check("G7 the attacks-and-hunts choice is there", combo[0] != null);
        if (combo[0] != null) {
            SwingUtilities.invokeAndWait(() -> combo[0].setSelectedIndex(2));
            check("G8 choosing 'I go alone' sets the policy", until(() -> p.pc().squadFight == 2, 5000));
            SwingUtilities.invokeAndWait(() -> combo[0].setSelectedIndex(0));
            check("G8b ...and 'the squad joins me' sets it back", until(() -> p.pc().squadFight == 0, 5000));
        }
        // kick the second member
        SwingUtilities.invokeAndWait(() -> pt[0].setRowSelectionInterval(2, 2));
        SwingUtilities.invokeAndWait(() -> { JButton b = PcProbeGui.findButton(d.getContentPane(), "Kick"); if (b != null) b.doClick(); });
        check("G9 'Kick' removes the selected member only", until(() -> ms.get(1).partyLeaderId < 0 && ms.get(0).partyLeaderId == p.id, 5000));
        // the leader row cannot be ordered
        SwingUtilities.invokeAndWait(() -> pt[0].setRowSelectionInterval(0, 0));
        SwingUtilities.invokeAndWait(() -> { JButton b = PcProbeGui.findButton(d.getContentPane(), "Kick"); if (b != null) b.doClick(); });
        Thread.sleep(500);
        check("G10 the leader's own row takes no orders (the squad is intact)", ms.get(0).partyLeaderId == p.id);
        PcProbeGui.real.println("[G] DONE");
    }
}
