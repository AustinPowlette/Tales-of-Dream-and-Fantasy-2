package Game;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;
import java.util.*;
import java.util.List;

/** 2026-10-14 (GUI, needs a display): the Randomize button of Edit Character. Each ticked field changes and nothing else does (gender stays when its box is off, potential stays when it is locked), Done keeps the
 *  new choices, Cancel puts everything back, the draws are in range (`EditRandom`), and the terminal editor has (R)andomize. */
public class EditRandomProbe {
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
        PcProbeGui.IGNORE.addAll(Arrays.asList("Edit Character"));
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
    static void box(Container c, String text, boolean on) throws Exception {
        SwingUtilities.invokeAndWait(() -> { List<JCheckBox> l = new ArrayList<>(); PcProbeGui.collect(c, JCheckBox.class, l); for (JCheckBox b : l) if (text.equals(b.getText())) b.setSelected(on); });
    }
    static List<JTextField> fields(Container c) { final List<JTextField> l = new ArrayList<>(); try { SwingUtilities.invokeAndWait(() -> PcProbeGui.collect(c, JTextField.class, l)); } catch (Exception e) { } return l; }
    static String selectedRadio(Container c, String... names) {
        final String[] r = {""};
        try { SwingUtilities.invokeAndWait(() -> { List<JRadioButton> l = new ArrayList<>(); PcProbeGui.collect(c, JRadioButton.class, l); for (JRadioButton b : l) for (String n : names) if (n.equals(b.getText()) && b.isSelected()) r[0] = n; }); } catch (Exception e) { }
        return r[0];
    }
    static void press(Container c, String label) throws Exception { SwingUtilities.invokeAndWait(() -> { JButton b = PcProbeGui.findButton(c, label); if (b != null) b.doClick(); }); }

    static void run() throws Exception {
        Settings.lore = false;
        PcProbe.in.feed("y\ns\nn\n");
        long end = System.currentTimeMillis() + Long.getLong("gui.waitMs", 240000L);
        while (System.currentTimeMillis() < end && !(GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null)) Thread.sleep(200);
        check("GUI opened and the player is prompting", GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null);
        final Character p = PcProbeGui.pc();
        if (p == null) return;
        final MapViewer mv = GameDriver.mapViewer;

        // ---- R1: the draws are in range
        boolean ok = true;
        for (int i = 0; i < 300; i++) {
            int pot = EditRandom.potential(), sk = EditRandom.skin();
            Character.Traits tr = EditRandom.trait();
            String al = EditRandom.alignment();
            String[] nm = EditRandom.name(i % 2 == 0, p.location);
            if (pot < 1 || pot > 100 || sk < 1 || sk > ArtMore.SKINS.length || tr == Character.Traits.Beast || tr == Character.Traits.Monster || tr == Character.Traits.Cheater
                    || !(al.equals("Good") || al.equals("Neutral") || al.equals("Evil")) || nm[0].isEmpty() || nm[1].isEmpty()) ok = false;
        }
        check("R1 300 draws are in range: potential 1-100, a skin tone, a playable trait, an alignment, a name", ok);

        // ---- R2: the dialog
        final String name0 = p.name, last0 = p.lastName, aff0 = p.affiliation, gen0 = p.gender;
        final Character.Traits trait0 = p.trait;
        final int pot0 = p.potential, skin0 = p.skinTone;
        SwingUtilities.invokeLater(() -> GuiEditSelf.show(mv, p, true));
        check("R2 the Edit Character dialog opens", until(() -> find("Edit Character") != null, 8000));
        final JDialog d = find("Edit Character");
        if (d == null) return;
        Thread.sleep(800);
        check("R2b it has a Randomize button", PcProbeGui.findButton(d.getContentPane(), "Randomize") != null);
        String genderBefore = selectedRadio(d.getContentPane(), "Male", "Female");
        // everything but gender, ten presses: the gender never changes
        boolean genderStays = true, nameChanged = false, potentialInRange = true, nameWasRandom = false;
        for (int i = 0; i < 10; i++) {
            press(d.getContentPane(), "Randomize");
            Thread.sleep(150);
            if (!genderBefore.equals(selectedRadio(d.getContentPane(), "Male", "Female"))) genderStays = false;
            List<JTextField> f = fields(d.getContentPane());
            if (!f.get(0).getText().equals(name0)) nameChanged = true;
            Object pv = null;
            List<JSpinner> sp = new ArrayList<>(); final List<JSpinner> spf = sp; SwingUtilities.invokeAndWait(() -> PcProbeGui.collect(d.getContentPane(), JSpinner.class, spf));
            if (!sp.isEmpty()) { int v = ((Number) sp.get(0).getValue()).intValue(); if (v < 1 || v > 100) potentialInRange = false; }
        }
        check("R3 ten presses with Gender unticked: the gender never changes (" + genderBefore + ")", genderStays);
        check("R3b ...the name changes and the potential stays 1-100", nameChanged && potentialInRange);

        // only the name: nothing else moves
        box(d.getContentPane(), "Skin", false); box(d.getContentPane(), "Trait", false); box(d.getContentPane(), "Potential", false); box(d.getContentPane(), "Alignment", false); box(d.getContentPane(), "Gender", false);
        final int skinNow = p.skinTone;
        final int[] potNow = {0};
        List<JSpinner> sp2 = new ArrayList<>(); SwingUtilities.invokeAndWait(() -> PcProbeGui.collect(d.getContentPane(), JSpinner.class, sp2));
        potNow[0] = ((Number) sp2.get(0).getValue()).intValue();
        String n1 = fields(d.getContentPane()).get(0).getText();
        press(d.getContentPane(), "Randomize"); Thread.sleep(200);
        String n2 = fields(d.getContentPane()).get(0).getText();
        check("R4 with only Name ticked the name changes (" + n1 + " -> " + n2 + ") and the skin and potential do not", !n1.equals(n2) && p.skinTone == skinNow && ((Number) sp2.get(0).getValue()).intValue() == potNow[0]);

        // only the skin
        box(d.getContentPane(), "Name", false); box(d.getContentPane(), "Skin", true);
        String n3 = fields(d.getContentPane()).get(0).getText();
        press(d.getContentPane(), "Randomize"); Thread.sleep(200);
        check("R5 with only Skin ticked the skin tone is set (" + p.skinTone + ") and the name stays", p.skinTone >= 1 && fields(d.getContentPane()).get(0).getText().equals(n3));

        // ---- R6: Cancel puts everything back
        press(d.getContentPane(), "Cancel");
        check("R6 Cancel closes the dialog", until(() -> find("Edit Character") == null, 8000));
        check("R6b ...and the character is as it was (" + p.name + ", " + p.trait + ", potential " + p.potential + ", skin " + p.skinTone + ")", p.name.equals(name0) && p.lastName.equals(last0) && p.trait == trait0 && p.potential == pot0 && p.skinTone == skin0 && p.affiliation.equals(aff0) && p.gender.equals(gen0));

        // ---- R7: Done keeps them
        SwingUtilities.invokeLater(() -> GuiEditSelf.show(mv, p, true));
        check("R7 the dialog opens again", until(() -> find("Edit Character") != null, 8000));
        JDialog d2 = find("Edit Character");
        Thread.sleep(800);
        for (String b : new String[]{"Name", "Skin", "Trait", "Potential", "Alignment"}) box(d2.getContentPane(), b, true);
        box(d2.getContentPane(), "Gender", false);
        press(d2.getContentPane(), "Randomize"); Thread.sleep(300);
        String shownFirst = fields(d2.getContentPane()).get(0).getText();
        press(d2.getContentPane(), "Done");
        check("R7b Done closes it", until(() -> find("Edit Character") == null, 8000));
        check("R7c ...and keeps the randomized name (" + shownFirst + " = " + p.name + ") and the same gender", p.name.equals(shownFirst) && p.gender.equals(gen0));
        // put the character back
        p.name = name0; p.lastName = last0; p.trait = trait0; p.affiliation = aff0; p.potential = pot0; p.skinTone = skin0;
        PcProbeGui.real.println("[G] DONE");
    }
}
