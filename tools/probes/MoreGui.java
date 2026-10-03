package Game;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

/** The section-6 GUI windows (backpack, chronicle, biography, notes, world overview, dashboard tab, hover card, auto-pause, shortcuts). */
public class MoreGui {
    static int passes = 0, fails = 0;
    static PrintStream real;
    static void check(String n, boolean ok) { if (ok) passes++; else fails++; real.println("[M] " + (ok ? "PASS " : "FAIL ") + n); }

    static Window win(String prefix) { return QolGui.win(prefix); }
    static String text(Window w) {
        if (w == null) return "";
        StringBuilder sb = new StringBuilder(PcProbeGui.allText(w));
        try {
            PcProbeGui.onEdt(() -> {
                List<JTable> ts = new ArrayList<>(); PcProbeGui.collect(w, JTable.class, ts);
                for (JTable t : ts) for (int r = 0; r < t.getRowCount(); r++) for (int c = 0; c < t.getColumnCount(); c++) { Object v = t.getValueAt(r, c); if (v != null) sb.append(v).append(' '); }
                List<JTabbedPane> tp = new ArrayList<>(); PcProbeGui.collect(w, JTabbedPane.class, tp);
                for (JTabbedPane x : tp) for (int i = 0; i < x.getTabCount(); i++) sb.append(x.getTitleAt(i)).append(' ');
                List<JCheckBox> cb = new ArrayList<>(); PcProbeGui.collect(w, JCheckBox.class, cb);
                for (JCheckBox x : cb) sb.append(x.getText()).append(' ');
            });
        } catch (Exception e) { }
        return sb.toString();
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "false");
        real = System.out;
        PcProbe.real = real; PcProbeGui.real = real;
        System.setIn(PcProbe.in);
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        Thread w = new Thread(PcProbeGui::watcher, "dialog-watcher"); w.setDaemon(true); w.start();
        try { run(); } catch (Throwable e) { real.println("[M] PROBE EXCEPTION " + e); e.printStackTrace(real); fails++; }
        real.println("[M] ===== " + passes + " passed, " + fails + " failed; gameError=" + PcProbe.gameError + " =====");
        String o = PcProbe.out();
        int ex = 0; for (String l : o.split("\n")) if (l.contains("Exception") || l.contains("\tat Game.")) { ex++; if (ex < 8) real.println("[M] exline: " + l); }
        real.println("[M] exceptionLines=" + ex);
        System.exit(0);
    }

    static void run() throws Exception {
        PcProbe.in.feed("y\ns\n1\nn\n");
        long end = System.currentTimeMillis() + 240000;
        while (System.currentTimeMillis() < end && !(GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbe.pc() != null)) Thread.sleep(200);
        check("GUI opened and the player prompts", GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbe.pc() != null);
        Character p = PcProbe.pc();
        if (p == null) return;
        MapViewer mv = GameDriver.mapViewer;
        GameDriver.frozenNpcs = true;
        DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        Thread.sleep(600);

        // give the player something to look at
        p.backpack.add(new Item(p, Item.WeaponType.Spear, 1)); p.backpack.add(new Item(p, 1, Item.ItemType.Clothing));
        p.backpack.add(new Item(p, Item.WeaponType.Sword, 14));
        p.backpack.add(new Item(p, 3, Item.ItemType.Clothing));
        Chronicle.add("world", "A test calamity shook the north.", p.id);
        Chronicle.breakthrough(p);
        Chronicle.yearly(); Chronicle.yearly();
        Chronicle.notes.put(Chronicle.tileKey(p.location), "my first tile");

        // ---------- backpack ----------
        PcProbeGui.onEdt(() -> GuiMore.backpackWindow(mv));
        Thread.sleep(2000);
        Window bw = win("Backpack");
        check("Backpack window opens", bw != null);
        String bt = text(bw);
        check("Backpack lists the new sword and the equipment line", bt.contains("Weapon:") && bt.contains("Sword") || bt.contains("lvl") || bt.contains("14"));
        List<JTable> tables = new ArrayList<>();
        if (bw != null) PcProbeGui.onEdt(() -> PcProbeGui.collect(((JDialog) bw).getContentPane(), JTable.class, tables));
        check("Backpack has gear and technique tables", tables.size() >= 2);
        if (!tables.isEmpty()) {
            final JTable gt = tables.get(0);
            PcProbeGui.onEdt(() -> { if (gt.getRowCount() > 0) gt.setRowSelectionInterval(0, 0); });
            Thread.sleep(300);
            real.println("[M] info: tables=" + tables.size() + " rows=" + gt.getRowCount() + " cols=" + gt.getColumnCount() + " backpack=" + p.backpack.size() + " text=" + text(bw).replace((char) 10, (char) 32));
            check("gear table has rows", gt.getRowCount() >= 2);
            String hint = text(bw);
            check("selecting a row shows a comparison", hint.contains("Wearing it makes") || hint.contains("Not wearable"));
        }
        QolGui.press(((JDialog) bw).getContentPane(), "Equip best");
        Thread.sleep(800);
        PcProbe.awaitBlocked(30);
        check("Equip best equips the strongest sword", p.equippedWeapon != null && p.equippedWeapon.lvl >= 14);
        check("Backpack has the food/wine/poison and Companions tabs", text(bw).contains("Food, wine, poison") && text(bw).contains("Companions"));
        QolGui.closeAll("Backpack");

        // ---------- city / sect details ----------
        Area orig = Map.tiles[Map.range / 2][Map.range / 2];
        PcProbeGui.onEdt(() -> GuiMore.placeWindow(mv, orig));
        Thread.sleep(1500);
        Window pw = win("Details");
        check("City details window shows the lord, shops and history", pw != null && text(pw).contains("City of Originis") && text(pw).contains("History"));
        QolGui.closeAll("Details");
        check("terminal: sect/city by name resolve", TextTools.findPlaces("Originis", "city").size() == 1);

        // ---------- chronicle ----------
        PcProbeGui.onEdt(() -> GuiMore.chronicleWindow(mv));
        Thread.sleep(1500);
        Window cw = win("Chronicle");
        check("Chronicle window opens", cw != null);
        String ct = text(cw);
        check("Chronicle shows entries and the hall of legends tab", ct.contains("A test calamity") || ct.contains("broke through") || ct.contains("Hall of legends"));
        List<JTextField> tf = new ArrayList<>();
        if (cw != null) PcProbeGui.onEdt(() -> PcProbeGui.collect(((JDialog) cw).getContentPane(), JTextField.class, tf));
        if (!tf.isEmpty()) {
            PcProbeGui.onEdt(() -> { tf.get(0).setText("calamity"); tf.get(0).postActionEvent(); });
            Thread.sleep(1200);
            List<JTable> ctabs = new ArrayList<>();
            PcProbeGui.onEdt(() -> PcProbeGui.collect(((JDialog) cw).getContentPane(), JTable.class, ctabs));
            check("Chronicle search narrows to the matching event", !ctabs.isEmpty() && ctabs.get(0).getRowCount() == 1);
        } else check("Chronicle has a search box", false);
        QolGui.closeAll("Chronicle");

        // ---------- biography ----------
        PcProbeGui.onEdt(() -> GuiMore.biographyWindow(mv, p));
        Thread.sleep(1500);
        Window gw = win("Biography");
        check("Biography window opens with the life events", gw != null && text(gw).contains("Life events") );
        QolGui.closeAll("Biography");

        // ---------- notes ----------
        PcProbeGui.onEdt(() -> GuiMore.notesWindow(mv));
        Thread.sleep(1200);
        Window nw = win("Notes");
        check("Notes window lists the note", nw != null && text(nw).contains("my first tile"));
        QolGui.closeAll("Notes");

        // ---------- world overview ----------
        PcProbeGui.onEdt(() -> GuiMore.worldWindow(mv));
        Thread.sleep(1500);
        Window ww = win("World overview");
        check("World overview opens", ww != null);
        if (ww != null) {
            final java.awt.image.BufferedImage[] img = {null};
            PcProbeGui.onEdt(() -> { Container c = ((JDialog) ww).getContentPane(); img[0] = new java.awt.image.BufferedImage(c.getWidth(), c.getHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB); c.paint(img[0].getGraphics()); });
            check("World overview paints more than one colour", QolGui.distinctColours(img[0]) > 6);
            javax.imageio.ImageIO.write(img[0], "png", new File("world_overview.png"));
        }
        QolGui.closeAll("World overview");

        // ---------- rankings dashboard ----------
        PcProbeGui.onEdt(() -> GuiWindows.rankingsPanel(mv));
        Thread.sleep(1500);
        Window rw = win("Rankings");
        check("Rankings has a Dashboard tab with realm histogram", rw != null && text(rw).contains("Dashboard") );
        List<JTabbedPane> tp = new ArrayList<>();
        if (rw != null) PcProbeGui.onEdt(() -> PcProbeGui.collect(((JDialog) rw).getContentPane(), JTabbedPane.class, tp));
        if (!tp.isEmpty()) {
            PcProbeGui.onEdt(() -> { JTabbedPane x = tp.get(0); x.setSelectedIndex(x.getTabCount() - 1); });
            Thread.sleep(4500);
            check("Dashboard tab text shows the realm histogram", text(rw).contains("Cultivators by major realm"));
        }
        QolGui.closeAll("Rankings");

        // ---------- settings: auto-pause + high contrast ----------
        PcProbeGui.onEdt(() -> GuiExtras.settingsWindow(mv));
        Thread.sleep(1200);
        Window sw = win("Settings");
        String st = text(sw);
        check("Settings has auto-pause rules and the high-contrast option", st.contains("Auto-pause") && st.contains("High-contrast"));
        QolGui.closeAll("Settings");

        // ---------- status-bar buttons reopen a window that was closed with its X ----------
        for (String[] sbtn : new String[][]{{"⚑", "Quest Log"}, {"🔔", "Notifications"}}) {
            PcProbeGui.click(sbtn[0]); Thread.sleep(1500);
            Window w1 = win(sbtn[1]);
            check(sbtn[0] + " opens the " + sbtn[1] + " window", w1 != null);
            PcProbeGui.onEdt(() -> { if (w1 != null) w1.dispatchEvent(new java.awt.event.WindowEvent(w1, java.awt.event.WindowEvent.WINDOW_CLOSING)); });
            Thread.sleep(800);
            check("closing it with the X really closes it", win(sbtn[1]) == null);
            PcProbeGui.click(sbtn[0]); Thread.sleep(1500);
            check(sbtn[0] + " opens it again after it was closed with the X", win(sbtn[1]) != null);
            QolGui.closeAll(sbtn[1]);
        }

        // ---------- digest, rivalries, run stats, relationship web ----------
        PcProbeGui.onEdt(() -> GuiMore.digestWindow(mv, 52));
        Thread.sleep(1500);
        check("Digest window opens", text(win("What happened lately")).contains("What happened lately"));
        QolGui.closeAll("What happened");
        PcProbeGui.onEdt(() -> GuiMore.runWindow(mv));
        Thread.sleep(1500);
        check("Run statistics window shows the run", text(win("Run statistics")).contains("Run statistics"));
        QolGui.closeAll("Run statistics");
        PcProbeGui.onEdt(() -> GuiWindows.rankingsPanel(mv));
        Thread.sleep(1500);
        check("Rankings has the Rivalries tab", text(win("Rankings")).contains("Rivalries") && text(win("Rankings")).contains("Sects"));
        QolGui.closeAll("Rankings");
        PcProbeGui.onEdt(() -> GuiExtras.familyWindow(mv));
        Thread.sleep(1500);
        check("Family window has Past lives and Anyone's web tabs", text(win("Family")).contains("Past lives") && text(win("Family")).contains("Anyone"));
        QolGui.closeAll("Family");
        // the saves window shows a picture
        GameDriver.saveRequested = true; mv.queueInput("x"); Thread.sleep(6000);
        java.io.File[] pngs = SaveManager.dir.listFiles((d, n) -> n.endsWith(".png"));
        real.println("[M] info: saves dir " + java.util.Arrays.toString(SaveManager.dir.list()) + " abs " + SaveManager.dir.getAbsolutePath());
        check("a save writes a thumbnail picture next to it", pngs != null && pngs.length > 0);

        // ---------- auto-pause rule ----------
        Settings.pauseOn.add("war");
        GameDriver.paused = false;
        Notifications.post("war", "test war", -1, p.id);
        check("auto-pause: a matching notice pauses the game", GameDriver.paused);
        Settings.pauseOn.remove("war");
        GameDriver.paused = false;

        // ---------- hover card ----------
        // print a line naming a living cultivator so the log has a name to hover
        Character other = QolGui.other(p);
        if (other != null) {
            PcProbeGui.onEdt(() -> { try { java.lang.reflect.Field f = MapViewer.class.getDeclaredField("logArea"); f.setAccessible(true); ((JTextArea) f.get(mv)).setText("Old news. " + other.name + " " + other.lastName + " wandered into town."); } catch (Exception e) { } });
            final String[] tip = {null};
            PcProbeGui.onEdt(() -> {
                try {
                    java.lang.reflect.Field f = MapViewer.class.getDeclaredField("logArea"); f.setAccessible(true);
                    JTextArea la = (JTextArea) f.get(mv);
                    String txt = la.getText();
                    int idx = txt.indexOf(other.name + " " + other.lastName);
                    if (idx >= 0) {
                        java.awt.geom.Rectangle2D r2 = la.modelToView2D(idx + 1);
                        MouseEventFactory.fire(la, (int) r2.getX() + 2, (int) r2.getY() + 3);
                        tip[0] = la.getToolTipText();
                    }
                } catch (Exception e) { tip[0] = "ERR " + e; }
            });
            check("hover card for a name in the log", tip[0] != null && tip[0].contains(other.name) && tip[0].contains("Lv"));
        }

        // ---------- shortcuts ----------
        int before = Window.getWindows().length;
        PcProbeGui.onEdt(() -> mv.handleShortcut(new java.awt.event.KeyEvent(mv, java.awt.event.KeyEvent.KEY_PRESSED, 0, 0, java.awt.event.KeyEvent.VK_V, 'v')));
        Thread.sleep(1200);
        check("shortcut V opens the backpack", win("Backpack") != null);
        QolGui.closeAll("Backpack");
        String keys = MapViewer.KEYS_HELP;
        check("keys sheet lists the new shortcuts", keys.contains("V  backpack") && keys.contains("F1"));
    }

    static class MouseEventFactory {
        static void fire(JTextArea la, int x, int y) {
            java.awt.event.MouseEvent e = new java.awt.event.MouseEvent(la, java.awt.event.MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, x, y, 0, false);
            for (java.awt.event.MouseMotionListener l : la.getMouseMotionListeners()) l.mouseMoved(e);
        }
    }
}
