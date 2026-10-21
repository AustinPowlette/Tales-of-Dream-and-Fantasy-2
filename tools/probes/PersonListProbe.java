package Game;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;
import java.util.*;
import java.util.List;

/** 2026-10-13 (GUI, needs a display): the one list of people (`GuiPersonList`) in all its places: People here > Cultivators / Monsters, Browser > Cultivators / Monsters / Search anyone.
 *  The same button bar everywhere, a double-click opens the sheet, Follow / Unfollow is one button, Seek is a menu, an action that needs the same tile says so when the person is elsewhere,
 *  People here links to the Browser, the card and portrait appear for the people in the tile. */
public class PersonListProbe {
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
        PcProbeGui.IGNORE.addAll(Arrays.asList("People here", "Browser — select, filter, sort"));
        try { run(); } catch (Throwable e) { PcProbeGui.real.println("[G] PROBE EXCEPTION " + e); e.printStackTrace(PcProbeGui.real); PcProbeGui.fails++; }
        PcProbeGui.real.println("[G] ===== " + PcProbeGui.passes + " passed, " + PcProbeGui.fails + " failed; gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }

    static boolean until(java.util.function.BooleanSupplier c, long ms) throws Exception { long end = System.currentTimeMillis() + ms; while (System.currentTimeMillis() < end) { if (c.getAsBoolean()) return true; Thread.sleep(50); } return c.getAsBoolean(); }
    static JDialog find(String title) { for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && ((JDialog) w).getTitle() != null && ((JDialog) w).getTitle().startsWith(title)) return (JDialog) w; return null; }
    static Character npc(Area where, String first, int lvl) {
        Character c = new Character(where);
        c.age = 30; c.gender = "Male"; c.lvl = lvl; c.majorLvl = lvl / 10; c.maxAge = 100000; c.trait = Character.Traits.Friendly; c.name = first; c.lastName = "Probeson"; c.caution = 0;
        while (Map.characters.size() < c.id) Map.characters.add(null);
        Map.characters.add(c); Map.population++;
        return c;
    }
    static JPanel visibleTab(JDialog d) {
        final JPanel[] r = {null};
        try { SwingUtilities.invokeAndWait(() -> { List<JTabbedPane> ts = new ArrayList<>(); PcProbeGui.collect(d.getContentPane(), JTabbedPane.class, ts); if (!ts.isEmpty() && ts.get(0).getSelectedComponent() instanceof JPanel) r[0] = (JPanel) ts.get(0).getSelectedComponent(); }); } catch (Exception e) { }
        return r[0];
    }
    static void selectTab(JDialog d, String title) throws Exception {
        SwingUtilities.invokeAndWait(() -> { List<JTabbedPane> ts = new ArrayList<>(); PcProbeGui.collect(d.getContentPane(), JTabbedPane.class, ts); if (!ts.isEmpty()) for (int i = 0; i < ts.get(0).getTabCount(); i++) if (ts.get(0).getTitleAt(i).startsWith(title)) ts.get(0).setSelectedIndex(i); });
        Thread.sleep(500);
    }
    static String buttons(Container c) {
        final String[] r = {""};
        try { SwingUtilities.invokeAndWait(() -> { List<JButton> bs = new ArrayList<>(); PcProbeGui.collect(c, JButton.class, bs); StringBuilder sb = new StringBuilder(); for (JButton b : bs) sb.append("[").append(b.getText()).append("]"); r[0] = sb.toString(); }); } catch (Exception e) { }
        return r[0];
    }
    static JTable table(Container c) {
        final JTable[] r = {null};
        try { SwingUtilities.invokeAndWait(() -> { List<JTable> ts = new ArrayList<>(); PcProbeGui.collect(c, JTable.class, ts); if (!ts.isEmpty()) r[0] = ts.get(0); }); } catch (Exception e) { }
        return r[0];
    }
    static void press(Container c, String label) throws Exception { SwingUtilities.invokeAndWait(() -> { JButton b = PcProbeGui.findButton(c, label); if (b != null) b.doClick(); }); }
    static String statusText(Container c) { final String[] r = {""}; try { SwingUtilities.invokeAndWait(() -> r[0] = PcProbeGui.allText(c)); } catch (Exception e) { } return r[0]; }

    static final String BAR = "[Open][Talk][Spar][Attack][Invite to party][Preview fight][Chase][Follow][Control][Seek ▾][Go there][Copy ID]";

    static void run() throws Exception {
        Settings.lore = false;
        PcProbe.in.feed("y\ns\nn\n");
        long end = System.currentTimeMillis() + Long.getLong("gui.waitMs", 240000L);
        while (System.currentTimeMillis() < end && !(GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null)) Thread.sleep(200);
        check("GUI opened and the player is prompting", GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null);
        final Character p = PcProbeGui.pc();
        if (p == null) return;
        final MapViewer mv = GameDriver.mapViewer;
        p.lvl = 100; p.majorLvl = 10; p.maxAge = 100000;
        // a quiet tile with a few people, a monster, and a far-away cultivator
        Area lab = Map.tiles[(p.location.x + 4) % Map.range][(p.location.y + 4) % Map.range];
        if (lab == null) lab = p.location;
        final Area home = p.location;
        final Character ann = npc(home, "Ann", 30), bob = npc(home, "Bob", 20);
        final Character mon = npc(home, "Grendel", 40); mon.trait = Character.Traits.Monster;
        final int fx = (home.x + 7) % Map.range, fy = (home.y + 7) % Map.range;
        if (Map.tiles[fx][fy] == null) Map.tiles[fx][fy] = new Area(Area.ghost(fx == 0 ? Map.range - 1 : fx - 1, fy), 'e');
        final Area farTile = Map.tiles[fx][fy];
        final Character far = npc(farTile, "Zed", 25);
        Thread.sleep(300);

        // ---- People here
        SwingUtilities.invokeLater(() -> GuiCards.peopleHere(mv, 0));
        check("L1 People here opens", until(() -> find("People here") != null, 8000));
        JDialog ph = find("People here");
        if (ph == null) return;
        Thread.sleep(2200);
        String bar = buttons(ph.getContentPane());
        PcProbeGui.real.println("[G] info: People here buttons: " + bar);
        boolean barOk = true; for (String b : BAR.substring(1, BAR.length() - 1).split("\\]\\[")) if (!bar.contains("[" + b + "]")) barOk = false;
        check("L2 People here has the whole button bar (Open, Talk, Spar, Attack, Invite, Preview, Chase, Follow, Control, Seek, Go there, Copy ID)", barOk);
        check("L2b ...plus Refresh, Set as default limit, Export CSV and Open in Browser", bar.contains("[Refresh]") && bar.contains("[Set as default limit]") && bar.contains("[Export CSV]") && bar.contains("[Open in Browser]"));
        JTable tb = table(ph.getContentPane());
        check("L3 the list shows the people of the tile (" + (tb == null ? 0 : tb.getRowCount()) + " rows)", tb != null && tb.getRowCount() >= 2);
        // select Ann
        int annRow = -1;
        for (int i = 0; tb != null && i < tb.getRowCount(); i++) if (String.valueOf(tb.getValueAt(i, 2)).contains("Ann")) annRow = i;
        final int ar = annRow;
        check("L3b Ann is in the list", ar >= 0);
        SwingUtilities.invokeAndWait(() -> tb.setRowSelectionInterval(ar, ar));
        Thread.sleep(700);
        check("L4 the card of the selected person is shown beside the list", statusText(ph.getContentPane()).contains("Ann") && statusText(ph.getContentPane()).contains("Realm"));
        // Follow / Unfollow is one button
        press(ph.getContentPane(), "Follow");
        check("L5 'Follow' tracks the person and the same button now says Unfollow", until(() -> Map.trackedCharacters.contains(ann.id), 5000) && buttons(visibleTab(ph)).contains("[Unfollow]") && !buttons(visibleTab(ph)).contains("[Follow]"));
        press(ph.getContentPane(), "Unfollow");
        check("L5b 'Unfollow' stops it and the button says Follow again", until(() -> !Map.trackedCharacters.contains(ann.id), 5000) && buttons(visibleTab(ph)).contains("[Follow]") && !buttons(visibleTab(ph)).contains("[Unfollow]"));
        // double-click opens the sheet
        final JTable tbf = tb;
        SwingUtilities.invokeAndWait(() -> { java.awt.event.MouseEvent e = new java.awt.event.MouseEvent(tbf, java.awt.event.MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 5, 5, 2, false); for (java.awt.event.MouseListener ml : tbf.getMouseListeners()) ml.mouseClicked(e); });
        check("L6 a double-click on a row opens the person's sheet", until(() -> find("Ann Probeson") != null, 5000));
        JDialog sheet = find("Ann Probeson");
        if (sheet != null) {
            check("L6b the sheet has the same one Follow button", buttons(sheet.getContentPane()).contains("[Follow]") || buttons(sheet.getContentPane()).contains("[Unfollow]"));
            SwingUtilities.invokeAndWait(sheet::dispose);
        }
        // talk uses the command (a turn is not needed to check: it is queued and the game answers)
        int t0 = GameDriver.time;
        press(ph.getContentPane(), "Invite to party");
        check("L7 'Invite to party' sends the command (Ann answers, no week passes)", until(() -> ann.partyLeaderId == p.id || PcProbe.out().contains("declines"), 6000) && GameDriver.time == t0);
        // Monsters tab
        selectTab(ph, "Monsters");
        JPanel mt = visibleTab(ph);
        String mbar = mt == null ? "" : buttons(mt);
        check("L8 the Monsters tab has Open, Attack, Hunt here, Preview, Chase, Follow, Go there, Copy ID", mbar.contains("[Open]") && mbar.contains("[Attack]") && mbar.contains("[Hunt here]") && mbar.contains("[Preview fight]") && mbar.contains("[Chase]") && mbar.contains("[Follow]") && mbar.contains("[Copy ID]") && !mbar.contains("[Talk]"));
        SwingUtilities.invokeAndWait(ph::dispose);

        // ---- Browser
        SwingUtilities.invokeLater(() -> mv.openBrowser("Cultivators"));
        check("L9 the Browser opens on Cultivators", until(() -> find("Browser") != null, 8000));
        JDialog br = find("Browser");
        if (br == null) return;
        Thread.sleep(2000);
        JPanel ct = visibleTab(br);
        String cbar = ct == null ? "" : buttons(ct);
        boolean sameBar = true; for (String b : BAR.substring(1, BAR.length() - 1).split("\\]\\[")) if (!cbar.contains("[" + b + "]")) sameBar = false;
        check("L10 Browser > Cultivators has the same button bar and the filters of the other lists", sameBar && cbar.contains("[Set as default limit]") && !cbar.contains("[Open in Browser]"));
        JTable bt = ct == null ? null : table(ct);
        check("L10b it lists the people of the tile too (" + (bt == null ? 0 : bt.getRowCount()) + " rows)", bt != null && bt.getRowCount() >= 2);
        // Search anyone
        selectTab(br, "Search anyone");
        JPanel st = visibleTab(br);
        String sbar = st == null ? "" : buttons(st);
        boolean worldBar = true; for (String b : BAR.substring(1, BAR.length() - 1).split("\\]\\[")) if (!sbar.contains("[" + b + "]")) worldBar = false;
        check("L11 Search anyone has the same button bar (it used to lack Seek: talk and court, Chase, Talk, Spar, Attack, Invite)", worldBar && sbar.contains("[Search]"));
        final JPanel stf = st;
        SwingUtilities.invokeAndWait(() -> { List<JTextField> fs = new ArrayList<>(); PcProbeGui.collect(stf, JTextField.class, fs); if (!fs.isEmpty()) { fs.get(0).setText("Zed"); fs.get(0).postActionEvent(); } });
        Thread.sleep(1500);
        JTable wt = table(st);
        check("L12 searching the whole world finds the far cultivator (" + (wt == null ? 0 : wt.getRowCount()) + " rows)", wt != null && wt.getRowCount() >= 1 && String.valueOf(wt.getValueAt(0, 2)).contains("Zed"));
        SwingUtilities.invokeAndWait(() -> wt.setRowSelectionInterval(0, 0));
        Thread.sleep(300);
        press(st, "Talk");
        Thread.sleep(500);
        check("L13 'Talk' on someone who is elsewhere says so instead of failing silently", statusText(st).contains("not in your tile"));
        // the Seek menu
        press(st, "Seek ▾");
        Thread.sleep(500);
        final List<JMenuItem> items = new ArrayList<>();
        SwingUtilities.invokeAndWait(() -> { for (Window w : Window.getWindows()) PcProbeGui.collect(w, JMenuItem.class, items); });
        StringBuilder names = new StringBuilder(); for (JMenuItem mi : items) names.append(mi.getText()).append(",");
        PcProbeGui.real.println("[G] info: menu items: " + names);
        check("L14 the Seek button opens a menu with Befriend, Talk, Court, Spar, Fight", names.toString().contains("Befriend") && names.toString().contains("Talk") && names.toString().contains("Court") && names.toString().contains("Spar") && names.toString().contains("Fight"));
        for (JMenuItem mi : items) if ("Talk".equals(mi.getText())) { final JMenuItem m = mi; SwingUtilities.invokeAndWait(m::doClick); break; }
        boolean quest = until(() -> { for (Quest q : new ArrayList<>(p.targetQuests)) if (q.name.equals("Talk To") && q.targetCharacter == far.id) return true; return false; }, 6000);
        check("L15 Seek > Talk pins a 'Talk To' quest for that person", quest);
        check("L16 the Seek menu also offers Kill (hunt down)", names.toString().contains("Kill"));

        // ---- Relations and Family on the same list
        SwingUtilities.invokeAndWait(br::dispose);
        ann.changeRelationship(p, 0); p.changeRelationship(ann, 0); bob.changeRelationship(p, 0); p.changeRelationship(bob, 0); // (makes the entries)
        p.relationshipList.contains(ann.id).value = 2; p.relationshipList.contains(bob.id).value = -3;
        p.siblings.add(far);
        SwingUtilities.invokeLater(() -> GuiSheets.relationsWindow(mv));
        check("L17 the Relations window opens", until(() -> find("Relations") != null, 8000));
        JDialog rl = find("Relations");
        if (rl != null) {
            Thread.sleep(2800);
            JPanel kt = visibleTab(rl);
            String kbar = kt == null ? "" : buttons(kt);
            boolean kbarOk = true; for (String b : BAR.substring(1, BAR.length() - 1).split("\\]\\[")) if (!kbar.contains("[" + b + "]")) kbarOk = false;
            check("L18 'People you know' has the same button bar", kbarOk);
            JTable kt2 = kt == null ? null : table(kt);
            StringBuilder rows = new StringBuilder();
            for (int i = 0; kt2 != null && i < kt2.getRowCount(); i++) { rows.append(kt2.getValueAt(i, 2)).append("=").append(kt2.getValueAt(i, 8)).append(";"); }
            PcProbeGui.real.println("[G] info: known: " + rows);
            check("L19 it lists Ann as a close friend, Bob as an arch-enemy and the far sibling as a sibling", rows.toString().contains("Ann Probeson=close friend") && rows.toString().contains("Bob Probeson=arch-enemy") && rows.toString().contains("Zed Probeson=sibling"));
            // the 'show' filter
            final JPanel ktf = kt;
            SwingUtilities.invokeAndWait(() -> { List<JComboBox> cs = new ArrayList<>(); PcProbeGui.collect(ktf, JComboBox.class, cs); for (JComboBox cb : cs) if (cb.getItemCount() == 3 && "everyone".equals(String.valueOf(cb.getItemAt(0)))) cb.setSelectedIndex(2); });
            Thread.sleep(1200);
            JTable k3 = table(kt);
            boolean onlyBob = k3 != null && k3.getRowCount() >= 1; for (int i = 0; k3 != null && i < k3.getRowCount(); i++) if (!String.valueOf(k3.getValueAt(i, 2)).contains("Bob")) onlyBob = false;
            check("L20 'rivals and enemies' keeps only the people you are on bad terms with (" + (k3 == null ? 0 : k3.getRowCount()) + " rows)", onlyBob);
            selectTab(rl, "Family");
            JPanel ft = visibleTab(rl);
            Thread.sleep(2200);
            JTable f2 = ft == null ? null : table(ft);
            StringBuilder frows = new StringBuilder();
            for (int i = 0; f2 != null && i < f2.getRowCount(); i++) frows.append(f2.getValueAt(i, 2)).append("=").append(f2.getValueAt(i, 8)).append(";");
            PcProbeGui.real.println("[G] info: family: " + frows);
            check("L21 the Family tab lists the sibling and not the friend", frows.toString().contains("Zed Probeson=sibling") && !frows.toString().contains("Ann Probeson"));
            SwingUtilities.invokeAndWait(rl::dispose);
        }
        PcProbeGui.real.println("[G] DONE");
    }
}
