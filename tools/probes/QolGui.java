package Game;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

/** Drives the real Swing GUI through the quality-of-life windows: start-up menu, tutorial, saves, settings, family, notifications, search, exports. */
public class QolGui {
    static int passes = 0, fails = 0;
    static PrintStream real;
    static void check(String n, boolean ok) { if (ok) passes++; else fails++; real.println("[Q] " + (ok ? "PASS " : "FAIL ") + n); }

    static Window win(String prefix) {
        final Window[] r = {null};
        try { SwingUtilities.invokeAndWait(() -> { for (Window w : Window.getWindows()) {
            String t = w instanceof JDialog ? ((JDialog) w).getTitle() : w instanceof Frame ? ((Frame) w).getTitle() : "";
            if (w.isShowing() && t != null && t.startsWith(prefix)) r[0] = w; } }); } catch (Exception e) { }
        return r[0];
    }
    static Container content(Window w) { return w instanceof JDialog ? ((JDialog) w).getContentPane() : ((JFrame) w).getContentPane(); }
    static void press(Container c, String label) throws Exception {
        final JButton[] b = {null};
        PcProbeGui.onEdt(() -> b[0] = PcProbeGui.findButton(c, label));
        if (b[0] == null) { check("button '" + label + "' exists", false); return; }
        SwingUtilities.invokeLater(() -> b[0].doClick());
    }
    static <T extends Component> List<T> all(Container c, Class<T> cls) { List<T> l = new ArrayList<>(); PcProbeGui.collect(c, cls, l); return l; }
    static void closeAll(String prefix) throws Exception { PcProbeGui.onEdt(() -> { for (Window w : Window.getWindows()) { String t = w instanceof JDialog ? ((JDialog) w).getTitle() : ""; if (w.isShowing() && t != null && t.startsWith(prefix)) w.dispose(); } }); }
    static Character other(Character me) {
        for (int i = 0; i < Map.characters.size(); i++) {
            Character c = Map.characters.get(i);
            if (c != null && c != me && !c.dead && c.born && c.player == 0 && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster && c.location != null) return c;
        }
        return null;
    }
    static long imgHash(java.awt.image.BufferedImage im) { long h = 7; for (int y = 0; y < im.getHeight(); y += 3) for (int x = 0; x < im.getWidth(); x += 3) h = h * 31 + im.getRGB(x, y); return h; }
    static int distinctColours(java.awt.image.BufferedImage im) { Set<Integer> s = new HashSet<>(); for (int y = 0; y < im.getHeight(); y += 2) for (int x = 0; x < im.getWidth(); x += 2) s.add(im.getRGB(x, y)); return s.size(); }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "false");
        real = System.out;
        PcProbe.real = real; PcProbeGui.real = real;
        System.setIn(PcProbe.in);
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        Thread w = new Thread(PcProbeGui::watcher, "dialog-watcher"); w.setDaemon(true); w.start();
        try { run(); } catch (Throwable e) { real.println("[Q] PROBE EXCEPTION " + e); e.printStackTrace(real); fails++; }
        real.println("[Q] ===== " + passes + " passed, " + fails + " failed; gameError=" + PcProbe.gameError + " =====");
        String o = PcProbe.out();
        int ex = 0; for (String l : o.split("\n")) if (l.contains("Exception") || l.contains("\tat Game.")) { ex++; if (ex < 8) real.println("[Q] exline: " + l); }
        real.println("[Q] exceptionLines=" + ex);
        System.exit(0);
    }

    static void run() throws Exception {
        PcProbeGui.startupChoice = "Continue selected";
        PcProbe.in.feed("y\n");
        long end = System.currentTimeMillis() + 240000;
        while (System.currentTimeMillis() < end && !(GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbe.pc() != null)) Thread.sleep(200);
        check("start-up menu: Continue selected loaded the newest save", GameDriver.mapViewer != null && PcProbe.pc() != null && GameDriver.time > 52);
        check("start-up menu was shown", PcProbeGui.dialogSince(0, "Saved games"));
        Character p = PcProbe.pc();
        if (p == null) return;
        MapViewer mv = GameDriver.mapViewer;
        p.lvl = 100; p.majorLvl = 10; p.minorLvl = 0; p.maxAge = 100000; p.caution = 0;
        DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        Thread.sleep(600);

        // ---------- first run tutorial ----------
        Window tw = win("Tutorial");
        check("first run opened the tutorial", tw != null && Settings.firstRunDone);
        if (tw != null) {
            String first = PcProbeGui.allText(content(tw));
            check("tutorial shows page 1", first.contains("Welcome to the cultivation world") || first.contains("Time passes in weeks"));
            press(content(tw), "Take the tour"); Thread.sleep(600);
            check("tutorial Next turns the page", !PcProbeGui.allText(content(tw)).contains("Time passes in weeks"));
            closeAll("Tutorial");
        }

        // ---------- tooltips ----------
        final String[] tip = {null};
        PcProbeGui.onEdt(() -> { JButton b = PcProbeGui.findButton(mv.getContentPane(), "Stats"); tip[0] = b == null ? null : b.getToolTipText(); });
        check("main buttons have tooltips", tip[0] != null && tip[0].length() > 5);

        // ---------- save indicator + bell ----------
        final String[] bar = {""};
        final boolean[] bellBtn = {false};
        PcProbeGui.onEdt(() -> { bar[0] = PcProbeGui.allText(mv.getContentPane()); bellBtn[0] = PcProbeGui.findButton(mv.getContentPane(), "🔔") != null; });
        check("status bar shows the save indicator and the bell", bar[0].contains("💾") && bellBtn[0]);

        // ---------- theme, layout and map ----------
        check("FlatLaf theme is active (" + UIManager.getLookAndFeel().getName() + ")", Theme.flat && UIManager.getLookAndFeel().getName().startsWith("Flat"));
        final String[] hover = {null};
        final int[] geom = new int[4];
        PcProbeGui.onEdt(() -> {
            for (Component c : all(mv.getContentPane(), JPanel.class)) if (c.getClass().getSimpleName().equals("MapPanel")) {
                JComponent mp = (JComponent) c;
                int ts = Math.max(4, Math.min(mp.getWidth(), mp.getHeight()) / Map.range);
                int ox = Math.max(0, (mp.getWidth() - Map.range * ts) / 2), oy = Math.max(0, (mp.getHeight() - Map.range * ts) / 2);
                geom[0] = ox; geom[1] = mp.getWidth(); geom[2] = Map.range * ts;
                Area pl = PcProbe.pc().location;
                hover[0] = mp.getToolTipText(new java.awt.event.MouseEvent(mp, java.awt.event.MouseEvent.MOUSE_MOVED, 0, 0, ox + pl.x * ts + ts / 2, oy + (Map.range - 1 - pl.y) * ts + ts / 2, 0, false));
            }
        });
        check("the map is centred in its panel (left gutter " + geom[0] + ", width " + geom[1] + ", map " + geom[2] + ")", geom[2] > 0 && Math.abs(geom[0] - (geom[1] - geom[2]) / 2) <= 1);
        check("hovering a tile explains it", hover[0] != null && hover[0].contains("cultivators") && hover[0].contains("[" + PcProbe.pc().location.x + ", " + PcProbe.pc().location.y + "]"));
        final int[] tabsInDock = {0};
        PcProbeGui.onEdt(() -> { for (JTabbedPane tp : all(mv.getContentPane(), JTabbedPane.class)) if (tp.indexOfTab("World") >= 0) tabsInDock[0] = tp.getTabCount(); });
        check("the action buttons are grouped in a dock with four tabs", tabsInDock[0] == 4);

        // ---------- saves window ----------
        PcProbeGui.click("Saves"); Thread.sleep(2600);
        Window sw = win("Saves");
        check("Saves window opens and lists the slots", sw != null && all(content(sw), JTable.class).get(0).getRowCount() >= 1);
        if (sw != null) {
            PcProbeGui.dialogInput = "guitest";
            press(content(sw), "Save as"); Thread.sleep(700);
            PcProbeGui.click("Wait"); PcProbeGui.settle(); Thread.sleep(1500);
            check("Save as… writes the named slot", new File("saves/guitest.dat").isFile() && new File("saves/guitest.meta").isFile());
            Thread.sleep(2600);
            JTable tb = all(content(sw), JTable.class).get(0);
            int row = -1;
            for (int i = 0; i < tb.getRowCount(); i++) if ("guitest".equals(String.valueOf(tb.getValueAt(i, 0)))) row = i;
            check("the new slot shows up in the table", row >= 0);
            if (row >= 0) {
                final int rr = row;
                PcProbeGui.onEdt(() -> tb.setRowSelectionInterval(rr, rr));
                int tNow = GameDriver.time;
                press(content(sw), "Load selected"); Thread.sleep(800);
                PcProbeGui.click("Wait"); PcProbeGui.settle();
                check("Load selected (confirmed) loads it", PcProbe.pc() != null && GameDriver.time <= tNow + 3 && PcProbe.idMismatch() == 0);
                p = PcProbe.pc(); p.lvl = 100; p.majorLvl = 10; p.maxAge = 100000; p.caution = 0;
                PcProbeGui.confirmAnswer = "No";
                Thread.sleep(2600);
                PcProbeGui.onEdt(() -> { for (int i = 0; i < tb.getRowCount(); i++) if ("guitest".equals(String.valueOf(tb.getValueAt(i, 0)))) tb.setRowSelectionInterval(i, i); });
                press(content(sw), "Delete selected"); Thread.sleep(800);
                PcProbeGui.click("Wait"); PcProbeGui.settle();
                check("declining the delete confirmation keeps the save", new File("saves/guitest.dat").isFile());
                PcProbeGui.confirmAnswer = "Yes";
                press(content(sw), "Delete selected"); Thread.sleep(800);
                PcProbeGui.click("Wait"); PcProbeGui.settle(); Thread.sleep(500);
                check("confirming deletes every file of the slot", !new File("saves/guitest.dat").exists() && !new File("saves/guitest.meta").exists());
            }
            closeAll("Saves");
        }

        // ---------- settings window ----------
        PcProbeGui.click("Settings"); Thread.sleep(700);
        Window st = win("Settings");
        check("Settings opens", st != null);
        if (st != null) {
            Container sc = content(st);
            List<JCheckBox> boxes = all(sc, JCheckBox.class);
            JCheckBox tips = null, cb = null, toasts = null, deaths = null;
            for (JCheckBox b : boxes) { String s = b.getText(); if (s.startsWith("Tooltips")) tips = b; if (s.startsWith("Colour-blind")) cb = b; if (s.startsWith("Pop-up")) toasts = b; if (s.trim().startsWith("Deaths")) deaths = b; }
            check("settings has its checkboxes", tips != null && cb != null && toasts != null && deaths != null && boxes.size() >= 12);
            final JCheckBox ft = tips, fc = cb, fd = deaths;
            PcProbeGui.onEdt(() -> ft.doClick());
            check("Tooltips checkbox switches tooltips off", !Settings.tooltips && !ToolTipManager.sharedInstance().isEnabled());
            PcProbeGui.onEdt(() -> ft.doClick());
            check("…and back on", Settings.tooltips && ToolTipManager.sharedInstance().isEnabled());
            PcProbeGui.onEdt(() -> fd.doClick());
            check("category checkbox mutes death notices", Settings.mutedNotifications.contains("death"));
            PcProbeGui.onEdt(() -> fd.doClick());
            check("…and unmutes them", !Settings.mutedNotifications.contains("death"));
            // colour-blind palette changes the rendered map
            final java.awt.image.BufferedImage[] shots = new java.awt.image.BufferedImage[2];
            List<Component> panels = new ArrayList<>();
            PcProbeGui.onEdt(() -> { for (Component c : all(mv.getContentPane(), JPanel.class)) if (c.getClass().getSimpleName().equals("MapPanel")) panels.add(c); });
            check("found the map panel", panels.size() == 1);
            if (panels.size() == 1) {
                Component mp = panels.get(0);
                PcProbeGui.onEdt(() -> { mp.setSize(Math.max(200, mp.getWidth()), Math.max(200, mp.getHeight())); shots[0] = GuiExtras.snapshot(mp); });
                PcProbeGui.onEdt(() -> fc.doClick());
                PcProbeGui.onEdt(() -> shots[1] = GuiExtras.snapshot(mp));
                check("colour-blind palette redraws the map differently", Settings.colorblindMap && imgHash(shots[0]) != imgHash(shots[1]));
                PcProbeGui.onEdt(() -> fc.doClick());
                check("…and restores", !Settings.colorblindMap);
            }
            // text size
            final int[] before = {0, 0};
            PcProbeGui.onEdt(() -> { JButton b = PcProbeGui.findButton(mv.getContentPane(), "Stats"); before[0] = b.getFont().getSize(); });
            List<JSlider> sl = all(sc, JSlider.class);
            PcProbeGui.onEdt(() -> sl.get(0).setValue(150));
            Thread.sleep(400);
            PcProbeGui.onEdt(() -> { JButton b = PcProbeGui.findButton(mv.getContentPane(), "Stats"); before[1] = b.getFont().getSize(); });
            check("text size slider rescales the main window (" + before[0] + " -> " + before[1] + ")", Settings.fontScale > 1.4 && before[1] > before[0] * 1.3);
            PcProbeGui.onEdt(() -> sl.get(0).setValue(100));
            Thread.sleep(400);
            PcProbeGui.onEdt(() -> { JButton b = PcProbeGui.findButton(mv.getContentPane(), "Stats"); before[1] = b.getFont().getSize(); });
            check("…and back to normal", Math.abs(Settings.fontScale - 1.0) < 0.01 && Math.abs(before[1] - before[0]) <= 1);
            closeAll("Settings");
        }

        // ---------- family window ----------
        Character rel = other(p), par = null;
        for (int i = 0; i < Map.characters.size() && par == null; i++) { Character c = Map.characters.get(i); if (c != null && c != p && c != rel && !c.dead && c.born && c.player == 0 && c.trait != Character.Traits.Beast) par = c; }
        p.mother = par; if (par != null) par.children.add(p);
        p.spouse = rel; if (rel != null) rel.spouse = p;
        PcProbeGui.click("Family"); Thread.sleep(3600);
        Window fw = win("Family");
        check("Family window opens", fw != null);
        if (fw != null) {
            List<GuiExtras.TreePanel> tps = all(content(fw), GuiExtras.TreePanel.class);
            List<GuiExtras.GraphPanel> gps = all(content(fw), GuiExtras.GraphPanel.class);
            check("tree and graph panels exist", tps.size() == 1 && gps.size() == 2);
            if (tps.size() == 1 && gps.size() == 2) {
                Family.Tree ft = tps.get(0).tree;
                check("tree has parent row + own row (mother and spouse known)", ft != null && ft.rows.size() >= 2 && ft.rows.get(ft.selfGen).size() >= 2);
                final java.awt.image.BufferedImage[] im = new java.awt.image.BufferedImage[2];
                PcProbeGui.onEdt(() -> { tps.get(0).setSize(800, 400); gps.get(0).setSize(800, 500); im[0] = GuiExtras.snapshot(tps.get(0)); im[1] = GuiExtras.snapshot(gps.get(0)); });
                check("tree draws boxes (many colours)", distinctColours(im[0]) > 6);
                check("graph draws the spouse tie", gps.get(0).ties.size() >= 1 && distinctColours(im[1]) > 6);
            }
            closeAll("Family");
        }

        // ---------- notifications: toast, bell, history window ----------
        Character nx = other(p);
        int unread0 = Notifications.unread();
        Notifications.post("death", "Your friend Probe Person has died.", nx.id, p.id);
        Thread.sleep(900);
        final boolean[] toast = {false};
        PcProbeGui.onEdt(() -> { for (Window w : Window.getWindows()) if (w instanceof JWindow && w.isShowing()) for (JLabel l : all(((JWindow) w).getContentPane(), JLabel.class)) if (l.getText() != null && l.getText().contains("Probe Person")) toast[0] = true; });
        check("a notice pops up a toast", toast[0]);
        final String[] bell = {""};
        Thread.sleep(700);
        PcProbeGui.onEdt(() -> { JButton b = PcProbeGui.findButton(mv.getContentPane(), "🔔"); bell[0] = b == null ? "" : b.getText(); });
        check("bell shows the unread count (" + bell[0] + ")", bell[0].contains(String.valueOf(unread0 + 1)) || bell[0].matches(".*[1-9].*"));
        PcProbeGui.click("🔔"); Thread.sleep(2500);
        Window nw = win("Notifications");
        check("Notifications window lists the notice", nw != null && all(content(nw), JTable.class).get(0).getRowCount() >= 1 && String.valueOf(all(content(nw), JTable.class).get(0).getValueAt(0, 2)).contains("Probe Person"));
        Thread.sleep(2600);
        PcProbeGui.onEdt(() -> { JButton b = PcProbeGui.findButton(mv.getContentPane(), "🔔"); bell[0] = b == null ? "" : b.getText(); });
        check("opening the history marks them read (bell " + bell[0] + ")", bell[0].contains("0"));
        closeAll("Notifications");
        Settings.toasts = false;
        int tw0 = 0; for (Window w : Window.getWindows()) if (w instanceof JWindow && w.isShowing()) tw0++;
        Notifications.post("auction", "silent one", -1, p.id);
        Thread.sleep(500);
        int tw1 = 0; for (Window w : Window.getWindows()) if (w instanceof JWindow && w.isShowing()) tw1++;
        check("toasts can be switched off", tw1 <= tw0);
        Settings.toasts = true;

        // ---------- recap popup ----------
        PcProbeGui.onEdt(() -> GuiExtras.recapWindow(mv, "Year 9 recap — test", "=== Year 9 recap ===\nYou: level 5 → 6\n"));
        Thread.sleep(400);
        Window rw = win("Year 9 recap");
        check("recap popup opens", rw != null && PcProbeGui.allText(content(rw)).contains("level 5"));
        if (rw != null) {
            final Container rc = content(rw);
            PcProbeGui.onEdt(() -> { for (JCheckBox b : all(rc, JCheckBox.class)) if (b.getText().startsWith("Stop")) b.doClick(); });
            press(rc, "OK"); Thread.sleep(400);
            check("'Stop showing recaps' turns the recap off", Settings.recapWeeks == 0);
            Settings.recapWeeks = 52; Settings.save();
        }

        // ---------- browse: search anyone ----------
        PcProbeGui.click("Browse"); Thread.sleep(800);
        Window bw = win("Browser");
        check("Browse opens with a Search tab", bw != null && all(content(bw), JTabbedPane.class).get(0).indexOfTab("Search anyone") >= 0);
        if (bw != null) {
            JTabbedPane tabs = all(content(bw), JTabbedPane.class).get(0);
            PcProbeGui.onEdt(() -> tabs.setSelectedIndex(tabs.indexOfTab("Search anyone")));
            Character target = other(p);
            Component tab = tabs.getComponentAt(tabs.indexOfTab("Search anyone"));
            final JTextField q = all((Container) tab, JTextField.class).get(0);
            PcProbeGui.onEdt(() -> { q.setText("#" + target.id); q.postActionEvent(); });
            Thread.sleep(700);
            JTable rt = all((Container) tab, JTable.class).get(0);
            check("search by #id finds exactly that cultivator", rt.getRowCount() == 1 && String.valueOf(rt.getValueAt(0, 1)).equals(String.valueOf(target.id)));
            final String frag = target.name.substring(0, Math.min(3, target.name.length()));
            PcProbeGui.onEdt(() -> { q.setText(frag); q.postActionEvent(); });
            Thread.sleep(700);
            check("search by name fragment finds several", rt.getRowCount() >= 1);
            PcProbeGui.onEdt(() -> { q.setText("#" + target.id); q.postActionEvent(); });
            Thread.sleep(600);
            PcProbeGui.onEdt(() -> rt.setRowSelectionInterval(0, 0));
            press((Container) tab, "Follow"); Thread.sleep(900);
            check("Follow adds them to the tracker", Map.trackedCharacters.contains(target.id));
            press((Container) tab, "Unfollow"); Thread.sleep(600);
            press((Container) tab, "Export CSV"); Thread.sleep(500);
            File[] csv = new File("exports").listFiles((d, n) -> n.startsWith("search"));
            check("Search results export to CSV", csv != null && csv.length >= 1);
            int tMark = PcProbe.mark();
            press((Container) tab, "Preview fight"); Thread.sleep(900);
            check("Preview fight prints the odds", PcProbe.since(tMark).length() > 10);
            press((Container) tab, "Control"); Thread.sleep(1200);
            PcProbeGui.settle();
            check("Control (confirmed) switches to that cultivator", PcProbe.pc() != null && PcProbe.pc().id == target.id);
            closeAll("Browser");
        }

        // ---------- exports ----------
        File[] before = new File("exports").listFiles((d, n) -> n.endsWith(".png"));
        int nb = before == null ? 0 : before.length;
        mv.exportMap(4);
        File[] maps = new File("exports").listFiles((d, n) -> n.startsWith("map-") && n.endsWith(".png"));
        check("Export map image writes a PNG", maps != null && maps.length >= 1);
        if (maps != null && maps.length >= 1) {
            java.awt.image.BufferedImage im = javax.imageio.ImageIO.read(maps[maps.length - 1]);
            check("the map image has the right size (" + im.getWidth() + "px) and real content", im.getWidth() == Map.range * 4 && im.getHeight() == Map.range * 4 && distinctColours(im) > 20);
        }
        PcProbeGui.click("Rankings"); Thread.sleep(2500);
        Window rk = win("Rankings");
        if (rk != null) {
            press(content(rk), "Save charts as PNG"); Thread.sleep(700);
            File[] ch = new File("exports").listFiles((d, n) -> n.startsWith("charts"));
            check("Rankings window saves the charts as PNG", ch != null && ch.length >= 1);
            press(content(rk), "Export rankings CSV"); Thread.sleep(700);
            File[] rc = new File("exports").listFiles((d, n) -> n.startsWith("rankings"));
            check("…and exports rankings CSV", rc != null && rc.length >= 1);
            closeAll("Rankings");
        } else check("Rankings opens for export", false);

        // ---------- the game is still alive ----------
        PcProbeGui.click("Wait"); PcProbeGui.settle();
        check("the game keeps running", GameDriver.waitingForInput && PcProbe.pc() != null && PcProbe.idMismatch() == 0);
        check("no crash log", !new File("crash.log").exists());
    }
}
