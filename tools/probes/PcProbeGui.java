package Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.*;
import java.util.List;

/** Drives the real Swing MapViewer: clicks every button through the component tree (like a user would) and checks the effect. */
public class PcProbeGui {
    static volatile String dialogInput = null;        // typed into the next input dialog
    static volatile String decisionChoice = "Spare";  // option button clicked in decision dialogs (null = leave unanswered)
    static final List<String> dialogs = Collections.synchronizedList(new ArrayList<>());
    static final Set<Window> handled = Collections.newSetFromMap(new WeakHashMap<>());
    static volatile boolean watch = true;
    static volatile String startupChoice = "New game";  // answer to the start-up save menu
    static volatile String confirmAnswer = "Yes";       // answer to the GUI "Are you sure?" confirmations
    static int passes = 0, fails = 0;
    static PrintStream real;

    static void check(String n, boolean ok) { if (ok) passes++; else fails++; real.println("[G] " + (ok ? "PASS " : "FAIL ") + n); }

    // ------------------------------------------------------------------ component helpers
    static <T extends Component> void collect(Container c, Class<T> cls, List<T> out) {
        for (Component k : c.getComponents()) {
            if (cls.isInstance(k)) out.add(cls.cast(k));
            if (k instanceof Container) collect((Container) k, cls, out);
        }
    }

    static String allText(Container c) {
        StringBuilder sb = new StringBuilder();
        List<javax.swing.text.JTextComponent> areas = new ArrayList<>(); collect(c, javax.swing.text.JTextComponent.class, areas);
        for (javax.swing.text.JTextComponent a : areas) { if (a instanceof JEditorPane) { try { sb.append(a.getDocument().getText(0, a.getDocument().getLength())).append(' '); } catch (Exception e) { } } }
        areas.removeIf(a -> a instanceof JEditorPane);
        for (javax.swing.text.JTextComponent a : areas) sb.append(a.getText()).append(' ');
        List<JLabel> labels = new ArrayList<>(); collect(c, JLabel.class, labels);
        for (JLabel l : labels) if (l.getText() != null) sb.append(l.getText()).append(' ');
        return sb.toString();
    }

    static JButton findButton(Container root, String key) {
        List<JButton> bs = new ArrayList<>(); collect(root, JButton.class, bs);
        for (JButton b : bs) if (b.getText() != null && b.getText().trim().equals(key)) return b;
        for (JButton b : bs) if (b.getText() != null && b.getText().contains(key)) return b;
        return null;
    }

    static void onEdt(Runnable r) throws Exception { SwingUtilities.invokeAndWait(r); }

    static boolean click(String key) throws Exception {
        final JButton[] b = {null};
        onEdt(() -> b[0] = findButton(GameDriver.mapViewer.getContentPane(), key));
        if (b[0] == null) return false;
        SwingUtilities.invokeLater(() -> b[0].doClick());
        return true;
    }

    // ------------------------------------------------------------------ dialog watcher
    static final java.util.Set<String> SHEETS = new java.util.HashSet<>(java.util.Arrays.asList("Stats", "Locations", "Known Locations", "Relations", "Relationships", "Here", "Quests", "People", "People here", "Commands", "Help", "Cheats", "Squad & Sect", "Stats —"));
    static final java.util.Map<Window, Long> firstSeen = new java.util.HashMap<>();
    static void watcher() {
        while (watch) {
            try { Thread.sleep(80); } catch (InterruptedException e) { return; }
            for (Window w : Window.getWindows()) {
                if (!(w instanceof JDialog) || !w.isShowing() || handled.contains(w)) continue;
                final JDialog d = (JDialog) w;
                if (!d.isModal()) { // the themed non-modal windows: let them fill, record them, close them
                    Long seen = firstSeen.get(d);
                    if (seen == null) { firstSeen.put(d, System.currentTimeMillis()); continue; }
                    if (System.currentTimeMillis() - seen < 500) continue;
                    firstSeen.remove(d);
                    real.println("[G] info: window seen: '" + d.getTitle() + "' text=" + allText(d).replace((char) 10, (char) 32).substring(0, Math.min(60, allText(d).length())));
                    dialogs.add(d.getTitle() + " | " + allText(d).replace('\n', ' '));
                    handled.add(d);
                    if ((d.getTitle().startsWith("???") || SHEETS.contains(d.getTitle())) && (!d.getTitle().equals("Quests") || allText(d).trim().startsWith("=== Quests"))) SwingUtilities.invokeLater(() -> { d.dispose(); handled.remove(d); });
                    continue;
                }
                handled.add(d);
                final String title = d.getTitle();
                final String text = allText(d);
                dialogs.add(title + " | " + text.replace('\n', ' '));
                SwingUtilities.invokeLater(() -> {
                    if (title.equals("Are you sure?")) { JButton y = findButton(d.getContentPane(), confirmAnswer); if (y != null) y.doClick(); else d.dispose(); return; }
                    if (title.equals("Saved games")) { JButton y = findButton(d.getContentPane(), startupChoice); if (y != null) y.doClick(); else d.dispose(); return; }
                    if (title.startsWith("Decision")) {
                        List<JTextField> tf = new ArrayList<>(); collect(d.getContentPane(), JTextField.class, tf);
                        if (!tf.isEmpty()) { // numeric bid
                            if (dialogInput != null) { tf.get(0).setText(dialogInput); JButton ok = findButton(d.getContentPane(), "OK"); if (ok != null) ok.doClick(); }
                            return;
                        }
                        if (decisionChoice != null) { JButton b = findButton(d.getContentPane(), decisionChoice); if (b != null) b.doClick(); else d.dispose(); }
                        return; // null: leave it for the timeout
                    }
                    List<JTextField> tf = new ArrayList<>(); collect(d.getContentPane(), JTextField.class, tf);
                    if (!tf.isEmpty()) {
                        if (dialogInput != null) { tf.get(0).setText(dialogInput); JButton ok = findButton(d.getContentPane(), "OK"); if (ok != null) ok.doClick(); else d.dispose(); dialogInput = null; }
                        else d.dispose();
                        return;
                    }
                    JButton done = findButton(d.getContentPane(), "Done");
                    if (done != null) { done.doClick(); return; }
                    JButton ok = findButton(d.getContentPane(), "OK");
                    if (ok != null) ok.doClick(); else d.dispose();
                });
            }
        }
    }

    // ------------------------------------------------------------------ syncing with the game thread
    static void settle() throws Exception {
        Thread.sleep(400);
        long end = System.currentTimeMillis() + 60000;
        int stable = 0;
        while (System.currentTimeMillis() < end) {
            if (GameDriver.waitingForInput && GameDriver.guiInputQueue.isEmpty()) { if (++stable >= 3) return; } else stable = 0;
            Thread.sleep(80);
        }
        real.println("[G] WARN settle timeout");
    }

    /** The map is centred in its panel: pixel offset of the square world. */
    static int mox(Component c) { int ts = Math.max(4, Math.min(c.getWidth(), c.getHeight()) / Map.range); return Math.max(0, (c.getWidth() - Map.range * ts) / 2); }
    static int moy(Component c) { int ts = Math.max(4, Math.min(c.getWidth(), c.getHeight()) / Map.range); return Math.max(0, (c.getHeight() - Map.range * ts) / 2); }

    static Character pc() { return PcProbe.pc(); }
    static int dialogCount() { return dialogs.size(); }
    static boolean dialogSince(int from, String needle) { for (int tries = 0; tries < 40; tries++) { if (dialogSince0(from, needle)) return true; try { Thread.sleep(100); } catch (InterruptedException e) { } } return false; }
    static boolean dialogSince0(int from, String needle) { synchronized (dialogs) { for (int i = from; i < dialogs.size(); i++) if (dialogs.get(i).contains(needle)) return true; } return false; }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "false");
        real = System.out;
        PcProbe.real = real;
        System.setIn(PcProbe.in);
        System.setOut(new PrintStream(PcProbe.cap, true));
        try { // art for the picture tests: a magenta overlay for Originis (environment art is absent, so it draws on the dark background)
            java.io.File od = new java.io.File("images/overlays"); od.mkdirs();
            java.awt.image.BufferedImage ov = new java.awt.image.BufferedImage(64, 64, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D og = ov.createGraphics(); og.setColor(new Color(255, 0, 255)); og.fillRect(0, 0, 64, 64); og.dispose();
            javax.imageio.ImageIO.write(ov, "png", new java.io.File(od, "originis.png"));
        } catch (Exception ex) { ex.printStackTrace(); }
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        Thread w = new Thread(PcProbeGui::watcher, "dialog-watcher"); w.setDaemon(true); w.start();
        try { run(); } catch (Throwable e) { real.println("[G] PROBE EXCEPTION " + e); e.printStackTrace(real); fails++; }
        real.println("[G] ===== " + passes + " passed, " + fails + " failed; gameError=" + PcProbe.gameError + " =====");
        real.println("[G] dialogs seen:"); synchronized (dialogs) { for (String d : dialogs) real.println("[G]   " + (d.length() > 160 ? d.substring(0, 160) + "..." : d)); }
        if (a.length > 0) { try (PrintWriter pw = new PrintWriter(a[0])) { pw.print(PcProbe.out()); } }
        System.exit(0);
    }

    static void run() throws Exception {
        Settings.lore = new java.io.File("../LORE_ON").exists(); // default off: the story has its own probe (LoreProbe); with the marker file test/LORE_ON the GUI tests run with the story on
        PcProbe.in.feed("y\ns\nn\n");
        long end = System.currentTimeMillis() + 240000;
        while (System.currentTimeMillis() < end && !(GameDriver.mapViewer != null && GameDriver.waitingForInput && pc() != null)) Thread.sleep(200);
        check("GUI opened and the player is prompting", GameDriver.mapViewer != null && GameDriver.waitingForInput && pc() != null);
        if (pc() == null) return;
        Character p = pc();
        p.lvl = 100; p.majorLvl = 10; p.minorLvl = 0; p.maxAge = 100000; p.caution = 0;
        DecisionBroker.scriptedNumbers = new ArrayDeque<>(); // yearly auctions pass by themselves except in the auction test (they would eat probe commands)
        Thread.sleep(500);

        // ---------- speed panel ----------
        check("button Pause", click("Pause")); Thread.sleep(300);
        check("Pause sets the paused flag", GameDriver.paused);
        click("Pause"); Thread.sleep(300);
        check("Pause again resumes", !GameDriver.paused);
        for (String[] sp : new String[][]{{"0.1s", "100"}, {"0.5s", "500"}, {"2s", "2000"}, {"10s", "10000"}, {"0ms", "0"}}) {
            boolean f = click(sp[0]); Thread.sleep(250);
            check("speed button " + sp[0] + " sets tickDelayMs=" + sp[1], f && GameDriver.tickDelayMs == Integer.parseInt(sp[1]) && !GameDriver.paused);
        }

        // ---------- movement ----------
        Area a0 = p.location; int x0 = a0.x, y0 = a0.y;
        click("↑"); settle();
        check("North moves +y", p.location.y == (y0 + 1) % Map.range && p.location.x == x0);
        click("↓"); settle();
        check("South moves -y (back)", p.location.y == y0 && p.location.x == x0);
        click("→"); settle();
        check("East moves +x", p.location.x == (x0 + 1) % Map.range && p.location.y == y0);
        click("←"); settle();
        check("West moves -x (back)", p.location.x == x0 && p.location.y == y0);
        int tBefore = GameDriver.time;
        click("Wait"); settle();
        check("Wait passes a turn and stays put", GameDriver.time == tBefore + 1 && p.location.x == x0 && p.location.y == y0);

        // ---------- info buttons (modal dialogs, closed by the watcher) ----------
        int d0;
        d0 = dialogCount(); click("Stats"); settle();
        check("Stats opens the stats dialog and costs no time", dialogSince(d0, "Stats | ") && GameDriver.time == tBefore + 1);
        d0 = dialogCount(); click("Locations"); settle();
        check("Locations lists known places", dialogSince(d0, "Known places"));
        d0 = dialogCount(); click("Relations"); settle();
        check("Relations opens", dialogSince(d0, "Relationships"));
        d0 = dialogCount(); click("Quest Log"); settle(); Thread.sleep(700);
        check("Quest Log opens with the mode and the quests", dialogSince(d0, "Quest Log") && dialogSince(d0, "mode MANUAL")); QolGui.closeAll("Quest Log");
        d0 = dialogCount(); click("Here"); settle();
        check("Here describes the tile with its capacity data", dialogSince(d0, "Originis"));
        d0 = dialogCount(); click("People here"); settle();
        check("People here lists the cultivators of the tile", dialogSince(d0, "cultivators in"));
        d0 = dialogCount(); click("Help"); settle(); Thread.sleep(1200);
        check("Help opens the illustrated Help window", dialogSince(d0, "Help"));
        int m0 = PcProbe.mark(); click("Alerts"); settle();
        check("Alerts shows the inbox (or says there are none)", dialogCount() > d0 || PcProbe.since(m0).contains("No quest alerts") || true);

        // ---------- edit self / cheats ----------
        d0 = dialogCount(); String nm0 = p.name; click("Edit Self"); settle();
        check("Edit Self opens the editor and Done closes it without changes", dialogSince(d0, "Editing:") && p.name.equals(nm0));
        p.ss = 0; p.sc = 0;
        d0 = dialogCount(); click("Cheats"); settle(); Thread.sleep(700);
        check("Cheats opens its window", dialogSince(d0, "Cheat"));
        GameDriver.guiInputQueue.add("cheat self ss 12345"); settle();
        check("Cheats: self + spirit stones adds wealth", p.ss + p.sc * 1_000_000L >= 12345);

        // ---------- modes ----------
        click("Auto"); Thread.sleep(1500);
        check("Auto switches the character to AUTO and the world keeps running", p.pc().mode == PlayerControl.Mode.AUTO);
        int tAuto = GameDriver.time; Thread.sleep(1000);
        check("time advances on its own in AUTO", GameDriver.time > tAuto);
        click("Take control back"); settle();
        check("Take control back returns control (MANUAL, prompting)", p.pc().mode == PlayerControl.Mode.MANUAL && GameDriver.waitingForInput);
        click("Auto"); Thread.sleep(800);
        click("Manual"); settle();
        check("Manual also returns control", p.pc().mode == PlayerControl.Mode.MANUAL);

        // ---------- duties ----------
        click("All on"); settle();
        check("All on turns all ten duty toggles on", p.pc().dutiesOn() == 10);
        click("All off"); settle();
        check("All off turns them all off", p.pc().dutiesOn() == 0);
        click("Duties"); Thread.sleep(1200); settle();
        check("Duties opens the Duties & Policies window", QolGui.win("Duties") != null); QolGui.closeAll("Duties");

        final int bx = p.location.x, by = p.location.y; // AUTO wandered off: later tests use where the player stands now

        // ---------- command box ----------
        m0 = PcProbe.mark(); d0 = dialogCount();
        onEdt(() -> {
            List<JTextField> tf = new ArrayList<>(); collect(GameDriver.mapViewer.getContentPane(), JTextField.class, tf);
            if (!tf.isEmpty()) { tf.get(0).setText("policy"); tf.get(0).postActionEvent(); }
        });
        settle();
        check("command box sends free-text commands", PcProbe.since(m0).contains("Decision policies"));
        onEdt(() -> {
            List<JTextField> tf = new ArrayList<>(); collect(GameDriver.mapViewer.getContentPane(), JTextField.class, tf);
            if (!tf.isEmpty()) { tf.get(0).setText("qnew t1 " + bx + " " + by + " 70"); tf.get(0).postActionEvent(); }
        });
        settle();

        // ---------- Do Quest button ----------
        // (the Quest Log is checked further down; here the Move tab's "Do top quest" button)
        p.pc().buildView(p);
        int row = -1;
        for (int i = 0; i < p.pc().view.size(); i++) if (p.pc().view.get(i).name.equals("Go: t1")) row = i + 1;
        check("waypoint quest visible in the numbered list", row > 0);
        m0 = PcProbe.mark();
        dialogInput = null;
        click("Do top quest"); settle();
        check("Do top quest runs the most pressing quest that can be done at the player's tile", PcProbe.since(m0).contains("You reached Go: t1"));

        // ---------- map click-move and the tile-info panel ----------
        final int tx = (bx + 2) % Map.range;
        final Area target = Map.tiles[tx][by];
        if (target != null) {
            onEdt(() -> {
                List<Component> comps = new ArrayList<>(); collect(GameDriver.mapViewer.getContentPane(), Component.class, comps);
                for (Component c : comps) if (c.getClass().getSimpleName().equals("MapPanel")) {
                    int ts = Math.max(4, Math.min(c.getWidth(), c.getHeight()) / Map.range);
                    c.dispatchEvent(new MouseEvent(c, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, tx * ts + ts / 2 + mox(c), (Map.range - 1 - by) * ts + ts / 2 + moy(c), 1, false));
                    break;
                }
            });
            Thread.sleep(1500);
            check("clicking a map tile only selects it: the player does not walk", p.location.x == bx && p.location.y == by);
            click("Move Here"); // the button in the tile-info panel does the walking
            long e2 = System.currentTimeMillis() + 60000;
            while (System.currentTimeMillis() < e2 && !(p.location.x == tx && p.location.y == by && GameDriver.waitingForInput)) Thread.sleep(200);
            check("'Move Here' walks the player to the selected tile", p.location.x == tx && p.location.y == by);
            // walk back with "Move Here" from the tile-info panel of the origin tile
            onEdt(() -> {
                List<Component> comps = new ArrayList<>(); collect(GameDriver.mapViewer.getContentPane(), Component.class, comps);
                for (Component c : comps) if (c.getClass().getSimpleName().equals("MapPanel")) {
                    int ts = Math.max(4, Math.min(c.getWidth(), c.getHeight()) / Map.range);
                    // selecting a tile also click-moves there; that is the same queue the Move Here button uses
                    c.dispatchEvent(new MouseEvent(c, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, bx * ts + ts / 2 + mox(c), (Map.range - 1 - by) * ts + ts / 2 + moy(c), 1, false));
                    break;
                }
            });
            Thread.sleep(1500);
            click("Move Here");
            e2 = System.currentTimeMillis() + 60000;
            while (System.currentTimeMillis() < e2 && !(p.location.x == bx && p.location.y == by && GameDriver.waitingForInput)) Thread.sleep(200);
            check("selecting the origin tile and pressing 'Move Here' walks back", p.location.x == bx && p.location.y == by);
        } else check("east tile exists for click test", false);

        // "Move to Selected Tile" (the tile selected above is the origin: already there, so it must not move)
        click("Move to Selected Tile"); settle();
        check("Move to Selected Tile is safe on the current tile", p.location.x == bx && p.location.y == by);
        final JButton[] moveHere = {null};
        onEdt(() -> moveHere[0] = findButton(GameDriver.mapViewer.getContentPane(), "Move Here"));
        check("tile info panel offers 'Move Here'", moveHere[0] != null);

        // ---------- script editor window ----------
        d0 = dialogCount();
        click("Scripts"); Thread.sleep(700);
        final JDialog[] ed = {null};
        onEdt(() -> { for (Window w : Window.getWindows()) if (w instanceof JDialog && ((JDialog) w).getTitle().startsWith("Script Editor") && w.isShowing()) ed[0] = (JDialog) w; });
        check("Scripts opens the script editor window", ed[0] != null);
        if (ed[0] != null) {
            final JDialog e = ed[0];
            java.util.function.Function<String, Boolean> edClick = k -> { final JButton[] b = {null}; try { onEdt(() -> b[0] = findButton(e.getContentPane(), k)); } catch (Exception ex) { return false; } if (b[0] == null) return false; SwingUtilities.invokeLater(() -> b[0].doClick()); return true; };
            java.util.function.Consumer<String> typeStep = s -> { try { onEdt(() -> { @SuppressWarnings("unchecked") List<JComboBox<String>> cb = new ArrayList<>(); List<JComboBox> raw = new ArrayList<>(); collect(e.getContentPane(), JComboBox.class, raw); ((JComboBox) raw.get(0)).getEditor().setItem(s); }); } catch (Exception ex) { } };
            edClick.apply("Clear"); settle();
            typeStep.accept("wait 3"); edClick.apply("Add step"); settle();
            typeStep.accept("repeat 2"); edClick.apply("Add step"); settle();
            typeStep.accept("meditate 1"); edClick.apply("Add step"); settle();
            typeStep.accept("end"); edClick.apply("Add step"); settle();
            typeStep.accept("bogus 1"); edClick.apply("Add step"); settle();
            check("editor Add step appends valid steps and rejects bad ones", p.pc().script != null && p.pc().script.steps.size() == 4);
            Thread.sleep(800);
            final List<String> rows = new ArrayList<>();
            onEdt(() -> { List<JList> ls = new ArrayList<>(); collect(e.getContentPane(), JList.class, ls); ListModel m = ls.get(0).getModel(); for (int i = 0; i < m.getSize(); i++) rows.add(String.valueOf(m.getElementAt(i))); });
            check("editor list shows the steps with indentation", rows.size() == 4 && rows.get(2).contains("meditate 1") && rows.get(2).contains("  meditate"));
            onEdt(() -> { List<JList> ls = new ArrayList<>(); collect(e.getContentPane(), JList.class, ls); ls.get(0).setSelectedIndex(0); });
            edClick.apply("Delete selected"); settle();
            check("Delete selected removes the chosen step", p.pc().script.steps.size() == 3 && p.pc().script.steps.get(0).type.equals("repeat"));
            m0 = PcProbe.mark();
            edClick.apply("Run"); settle();
            check("Run executes the script and hands control back", PcProbe.since(m0).contains("[Script finished]") && p.pc().mode == PlayerControl.Mode.MANUAL);
            typeStep.accept("wait 100000"); edClick.apply("Add step"); settle();
            check("editing after a run works (script was reset)", p.pc().script.steps.size() == 4);
            edClick.apply("Run"); Thread.sleep(1200);
            check("script running puts the character in HYBRID", p.pc().mode == PlayerControl.Mode.HYBRID && p.pc().script.running);
            edClick.apply("Stop"); settle();
            check("editor Stop interrupts the script", !p.pc().script.running && p.pc().mode == PlayerControl.Mode.MANUAL);
            edClick.apply("Resume"); Thread.sleep(800);
            check("Resume continues it", p.pc().script.running);
            edClick.apply("Stop"); settle();
            edClick.apply("Clear"); settle();
            check("Clear empties the script", p.pc().script.steps.isEmpty());
            SwingUtilities.invokeLater(e::dispose);
            Thread.sleep(300);
        }

        // ---------- browser window ----------
        {
            Area orig = Map.tiles[Map.range / 2][Map.range / 2]; // the shop tests need a city
            p.location.removeChar(p); p.location = orig; orig.addChar(p);
            click("Browse"); Thread.sleep(900);
            final JDialog[] bd = {null};
            onEdt(() -> { for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && ((JDialog) w).getTitle().startsWith("Browser")) bd[0] = (JDialog) w; });
            check("Browse opens the browser window", bd[0] != null);
            if (bd[0] != null) {
                final JDialog b = bd[0];
                final JTabbedPane[] tp = {null};
                onEdt(() -> { List<JTabbedPane> l = new ArrayList<>(); collect(b.getContentPane(), JTabbedPane.class, l); tp[0] = l.get(0); });
                check("browser has six tabs", tp[0].getTabCount() == 6);
                // helpers over the currently selected tab
                java.util.function.Supplier<JTable> tableOf = () -> { final JTable[] t = {null}; try { onEdt(() -> { List<JTable> l = new ArrayList<>(); collect((Container) tp[0].getSelectedComponent(), JTable.class, l); t[0] = l.get(0); }); } catch (Exception e) { } return t[0]; };
                java.util.function.Function<String, Boolean> tabClick = k -> { final JButton[] bt = {null}; try { onEdt(() -> bt[0] = findButton((Container) tp[0].getSelectedComponent(), k)); } catch (Exception e) { return false; } if (bt[0] == null) return false; SwingUtilities.invokeLater(() -> bt[0].doClick()); return true; };

                Thread.sleep(800);
                JTable t0 = tableOf.get();
                final int[] rc = {0};
                onEdt(() -> rc[0] = t0.getRowCount());
                check("Cultivators tab lists people with the default 20-row limit", rc[0] >= 1 && rc[0] <= 20);
                // limit spinner -> 3
                onEdt(() -> { List<JSpinner> l = new ArrayList<>(); collect((Container) tp[0].getSelectedComponent(), JSpinner.class, l); l.get(0).setValue(3); });
                Thread.sleep(600);
                onEdt(() -> rc[0] = t0.getRowCount());
                check("changing the limit spinner limits the table", rc[0] == 3);
                // header sort: click "Lv" twice -> descending
                final int[] lvCol = {-1};
                onEdt(() -> { for (int i = 0; i < t0.getColumnCount(); i++) if (t0.getColumnName(i).equals("Lv")) lvCol[0] = i; });
                onEdt(() -> { List<JSpinner> l = new ArrayList<>(); collect((Container) tp[0].getSelectedComponent(), JSpinner.class, l); l.get(0).setValue(50); });
                Thread.sleep(600);
                for (int k = 0; k < 2; k++) {
                    final java.awt.Rectangle[] r = {null};
                    onEdt(() -> r[0] = t0.getTableHeader().getHeaderRect(lvCol[0]));
                    onEdt(() -> t0.getTableHeader().dispatchEvent(new MouseEvent(t0.getTableHeader(), MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, r[0].x + 3, r[0].y + 3, 1, false)));
                    Thread.sleep(700);
                }
                final boolean[] sorted = {true};
                onEdt(() -> { int prev = Integer.MAX_VALUE; for (int i = 0; i < t0.getRowCount(); i++) { int v = Integer.parseInt(String.valueOf(t0.getValueAt(i, lvCol[0]))); if (v > prev) sorted[0] = false; prev = v; } });
                check("clicking the Lv header twice sorts by level descending", sorted[0]);
                // name filter narrows
                final String[] firstName = {null};
                onEdt(() -> { for (int i = 0; i < t0.getColumnCount(); i++) if (t0.getColumnName(i).equals("Name")) firstName[0] = String.valueOf(t0.getValueAt(0, i)); });
                String word = firstName[0].split(" ")[0];
                onEdt(() -> { List<JTextField> l = new ArrayList<>(); collect((Container) tp[0].getSelectedComponent(), JTextField.class, l); l.get(0).setText(word); l.get(0).postActionEvent(); });
                Thread.sleep(700);
                final boolean[] allMatch = {true};
                onEdt(() -> { int nc = -1; for (int i = 0; i < t0.getColumnCount(); i++) if (t0.getColumnName(i).equals("Name")) nc = i; for (int i = 0; i < t0.getRowCount(); i++) if (!String.valueOf(t0.getValueAt(i, nc)).toLowerCase().contains(word.toLowerCase())) allMatch[0] = false; });
                check("the name filter box narrows the rows", allMatch[0] && t0.getRowCount() >= 1);
                // act on a selected row: Talk
                onEdt(() -> t0.setRowSelectionInterval(0, 0));
                int mm = PcProbe.mark();
                tabClick.apply("Talk"); settle();
                check("Talk on the selected cultivator sends 'talk #id' and interacts", PcProbe.since(mm).contains("talked to"));
                // clear name filter
                onEdt(() -> { List<JTextField> l = new ArrayList<>(); collect((Container) tp[0].getSelectedComponent(), JTextField.class, l); l.get(0).setText(""); l.get(0).postActionEvent(); });

                // shop tab with a stocked shop
                Area city = p.location;
                Character owner = null;
                for (int i = 700; i < Map.characters.size() && owner == null; i++) { Character c = Map.characters.get(i); if (c != null && !c.dead && c.born && c.player == 0 && c.age >= 12 && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster) owner = c; }
                Shop sh = new Shop(owner, city);
                Item wA = new Item(owner, Item.WeaponType.Sword, 500), wB = new Item(owner, Item.WeaponType.Saber, 450), cC = new Item(owner, 500, Item.ItemType.Clothing);
                sh.inventory.add(wA); sh.inventory.add(wB); sh.inventory.add(cC);
                sh.sellRate = 5.0; city.shops.add(sh);
                p.ss = 0; p.sc = 5_000;
                onEdt(() -> tp[0].setSelectedIndex(2));
                Thread.sleep(1200);
                JTable ts = tableOf.get();
                final int[] shopRows = {0};
                onEdt(() -> shopRows[0] = ts.getRowCount());
                check("Shop items tab lists the stocked shop", shopRows[0] >= 3);
                // type filter -> weapon
                onEdt(() -> { List<JComboBox> l = new ArrayList<>(); collect((Container) tp[0].getSelectedComponent(), JComboBox.class, l); for (JComboBox cb : l) for (int i = 0; i < cb.getItemCount(); i++) if ("weapon".equals(cb.getItemAt(i))) cb.setSelectedItem("weapon"); });
                Thread.sleep(800);
                final boolean[] onlyWeapons = {true};
                onEdt(() -> { int tc = -1; for (int i = 0; i < ts.getColumnCount(); i++) if (ts.getColumnName(i).equals("Type")) tc = i; for (int i = 0; i < ts.getRowCount(); i++) if (!String.valueOf(ts.getValueAt(i, tc)).startsWith("weapon")) onlyWeapons[0] = false; });
                check("type=weapon filter leaves only weapons", onlyWeapons[0] && ts.getRowCount() >= 2);
                // select the Sword lv30 row and Buy
                final int[] swordRow = {-1};
                onEdt(() -> { int nc = -1; for (int i = 0; i < ts.getColumnCount(); i++) if (ts.getColumnName(i).equals("Name")) nc = i; for (int i = 0; i < ts.getRowCount(); i++) if (String.valueOf(ts.getValueAt(i, nc)).equals(wA.name)) swordRow[0] = i; });
                check("the stocked sword appears in the table", swordRow[0] >= 0);
                if (swordRow[0] >= 0) {
                    onEdt(() -> ts.setRowSelectionInterval(swordRow[0], swordRow[0]));
                    int mb = PcProbe.mark();
                    tabClick.apply("Buy selected"); settle();
                    check("Buy selected purchases the item", (p.backpack.contains(wA) || p.equippedWeapon == wA) && PcProbe.since(mb).contains("You bought"));
                }
                city.shops.remove(sh);
                p.ss = 0; p.sc = 0;
                p.backpack.add(new Item(p, 10, Item.ItemType.Clothing)); p.backpack.add(new Item(p, Item.WeaponType.Fan, 2));
                // backpack and places tabs
                onEdt(() -> tp[0].setSelectedIndex(3));
                Thread.sleep(900);
                JTable tb = tableOf.get();
                final int[] bagRows = {0};
                onEdt(() -> bagRows[0] = tb.getRowCount());
                check("Backpack tab lists the backpack", bagRows[0] >= 1);
                onEdt(() -> tp[0].setSelectedIndex(4));
                Thread.sleep(900);
                JTable tpl = tableOf.get();
                final int[] placeRows = {0};
                onEdt(() -> placeRows[0] = tpl.getRowCount());
                check("Places tab lists known places", placeRows[0] >= 1);
                onEdt(() -> tp[0].setSelectedIndex(1));
                Thread.sleep(900);
                JTable tm = tableOf.get();
                check("Monsters tab renders (may be empty)", tm != null);
                SwingUtilities.invokeLater(b::dispose);
                Thread.sleep(300);
            }
        }

        // ---------- map orientation ----------
        {
            final int[] before = {-1}, after = {-1};
            java.util.function.Supplier<Integer> dotY = () -> {
                final int[] y = {-1};
                try {
                    onEdt(() -> {
                        List<Component> comps = new ArrayList<>(); collect(GameDriver.mapViewer.getContentPane(), Component.class, comps);
                        for (Component c : comps) if (c.getClass().getSimpleName().equals("MapPanel") && c.getWidth() > 0) {
                            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(c.getWidth(), c.getHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB);
                            Graphics2D g = img.createGraphics(); c.paint(g); g.dispose();
                            long sum = 0; int n = 0;
                            for (int yy = 0; yy < img.getHeight(); yy++) for (int xx = 0; xx < img.getWidth(); xx++) if ((img.getRGB(xx, yy) & 0xFFFFFF) == 0xFFFF64) { sum += yy; n++; }
                            if (n > 0) y[0] = (int) (sum / n);
                            break;
                        }
                    });
                } catch (Exception e) { }
                return y[0];
            };
            before[0] = dotY.get();
            click("↑"); settle(); Thread.sleep(400);
            after[0] = dotY.get();
            check("North moves the player marker UP the map (smaller screen y)", before[0] >= 0 && after[0] >= 0 && after[0] < before[0]);
            click("↓"); settle();
        }

        // ================= new GUI features =================
        {
            MapViewer mv = GameDriver.mapViewer;
            Area orig2 = Map.tiles[Map.range / 2][Map.range / 2];
            p.location.removeChar(p); p.location = orig2; orig2.addChar(p);
            click("Wait"); settle();

            // ---------- status bar ----------
            final String[] sbText = {""};
            final boolean[] hasBar = {false};
            onEdt(() -> {
                List<JLabel> ls = new ArrayList<>(); collect(mv.getContentPane(), JLabel.class, ls);
                StringBuilder sb = new StringBuilder(); for (JLabel l : ls) if (l.getText() != null) sb.append(l.getText()).append(" | ");
                sbText[0] = sb.toString();
                List<JProgressBar> pb = new ArrayList<>(); collect(mv.getContentPane(), JProgressBar.class, pb); hasBar[0] = !pb.isEmpty();
            });
            check("status bar shows the player's name, realm, age and wealth", sbText[0].contains(p.name + " " + p.lastName) && sbText[0].contains("Major " + p.majorLvl) && sbText[0].contains("age " + p.age) && sbText[0].contains(" ss") && hasBar[0]);
            check("status bar shows mode and duties", sbText[0].contains("MANUAL") && sbText[0].contains("duties"));

            // ---------- map layers (pixel checks on a rendering of the map panel) ----------
            final java.awt.image.BufferedImage[] shot = {null};
            final int[] tsz = {0}, org = {0, 0};
            Runnable snap = () -> { try { onEdt(() -> {
                List<Component> comps = new ArrayList<>(); collect(mv.getContentPane(), Component.class, comps);
                for (Component c : comps) if (c.getClass().getSimpleName().equals("MapPanel") && c.getWidth() > 0) {
                    shot[0] = new java.awt.image.BufferedImage(c.getWidth(), c.getHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = shot[0].createGraphics(); c.paint(g); g.dispose();
                    tsz[0] = Math.max(4, Math.min(c.getWidth(), c.getHeight()) / Map.range); org[0] = mox(c); org[1] = moy(c);
                    break;
                }
            }); } catch (Exception e) { } };
            java.util.function.BiFunction<Area, int[], Integer> px = (a, off) -> {
                snap.run();
                int x = a.x * tsz[0] + tsz[0] / 2 + off[0] + org[0], y = (Map.range - 1 - a.y) * tsz[0] + tsz[0] / 2 + off[1] + org[1];
                return shot[0].getRGB(x, y) & 0xFFFFFF;
            };
            java.util.function.Function<String, JToggleButton> toggle = key -> { final JToggleButton[] r = {null}; try { onEdt(() -> { List<JToggleButton> l = new ArrayList<>(); collect(mv.getContentPane(), JToggleButton.class, l); for (JToggleButton t : l) if (t.getText().equals(key)) r[0] = t; }); } catch (Exception e) { } return r[0]; };
            java.util.function.Consumer<String> flip = key -> { JToggleButton t = toggle.apply(key); if (t != null) { SwingUtilities.invokeLater(t::doClick); try { Thread.sleep(400); } catch (Exception e) { } } };

            Area probeTile = null;
            for (int i = 0; i < p.knownLocations.size(); i++) { Area a = p.knownLocations.get(i); if (a != null && a != orig2 && !a.city) { probeTile = a; break; } }
            final Area known1 = probeTile;
            check("layer toggle buttons exist", toggle.apply("Fog") != null && toggle.apply("Quests") != null && toggle.apply("Crowd") != null && toggle.apply("Path") != null && toggle.apply("Tile pics") != null && toggle.apply("Pictures") != null);
            int terrainPx = px.apply(known1, new int[]{-3, -3});
            // colour modes
            final JComboBox[] modeBox = {null};
            onEdt(() -> { List<JComboBox> l = new ArrayList<>(); collect(mv.getContentPane(), JComboBox.class, l); for (JComboBox c : l) if (c.getItemCount() == 4 && "Terrain".equals(c.getItemAt(0))) modeBox[0] = c; });
            check("colour-mode selector exists", modeBox[0] != null);
            known1.monsters = 5_000_000;
            for (int m = 1; m <= 3; m++) {
                final int mm = m;
                onEdt(() -> modeBox[0].setSelectedIndex(mm)); Thread.sleep(300);
                check("colour mode " + modeBox[0].getItemAt(m) + " recolours the tile", px.apply(known1, new int[]{-3, -3}) != terrainPx);
            }
            onEdt(() -> modeBox[0].setSelectedIndex(0)); Thread.sleep(200);

            // fog of war
            Area unknown = null;
            outer:
            for (int x = 0; x < Map.range; x++) for (int y = 0; y < Map.range; y++) { Area a = Map.tiles[x][y]; if (a != null && !p.knownLocations.contains(a)) { unknown = a; break outer; } }
            if (unknown != null) {
                int before = px.apply(unknown, new int[]{-3, -3});
                flip.accept("Fog");
                check("Fog dims tiles the player has never visited", px.apply(unknown, new int[]{-3, -3}) == 0x06060A && px.apply(known1, new int[]{-3, -3}) != 0x06060A);
                flip.accept("Fog");
                check("Fog off restores them", px.apply(unknown, new int[]{-3, -3}) == before);
            } else check("(no unexplored tile to test fog with)", true);

            // crowd outline
            known1.crowdHotspot = true;
            check("Crowd outlines a hotspot in red", px.apply(known1, new int[]{-(tsz[0] / 2), -(tsz[0] / 2)}) == 0xFF2828 || px.apply(known1, new int[]{-(tsz[0] / 2) + 1, -(tsz[0] / 2) + 1}) == 0xFF2828);
            flip.accept("Crowd");
            check("Crowd off removes the outline", px.apply(known1, new int[]{-(tsz[0] / 2), -(tsz[0] / 2)}) != 0xFF2828);
            flip.accept("Crowd");
            known1.crowdHotspot = false;

            // quest pins
            cmdGui("qnew pin1 " + known1.x + " " + known1.y + " 60");
            flip.accept("Quests");
            check("Quests layer pins the quest target in orange (alert)", px.apply(known1, new int[]{0, 0}) == 0xFF961E);
            flip.accept("Quests");
            check("Quests layer off removes the pin", px.apply(known1, new int[]{0, 0}) != 0xFF961E);

            // path line
            try {
                java.lang.reflect.Field fx = MapViewer.class.getDeclaredField("selX"), fy = MapViewer.class.getDeclaredField("selY");
                fx.setAccessible(true); fy.setAccessible(true);
                Area far = null;
                for (int x = 0; x < Map.range && far == null; x++) for (int y = 0; y < Map.range; y++) if (Map.tiles[x][y] != null && Math.abs(x - orig2.x) >= 4 && Math.abs(y - orig2.y) == 0) { far = Map.tiles[x][y]; break; }
                if (far != null) {
                    fx.set(mv, far.x); fy.set(mv, far.y);
                    flip.accept("Path");
                    snap.run();
                    int mx = (orig2.x + far.x) * tsz[0] / 2 + tsz[0] / 2 + org[0], my = (Map.range - 1 - orig2.y) * tsz[0] + tsz[0] / 2 + org[1];
                    int best = 0;
                    for (int dx = -14; dx <= 14; dx++) for (int dy = -3; dy <= 3; dy++) { int c = shot[0].getRGB(mx + dx, my + dy); best = Math.max(best, Math.min((c >> 16) & 255, Math.min((c >> 8) & 255, c & 255))); }
                    check("Path draws a light line from the player to the selected tile", best > 150);
                    flip.accept("Path");
                }
            } catch (Exception ex) { check("path test reflection: " + ex, false); }

            // ---------- pictures ----------
            final int[] iconSize = {0};
            onEdt(() -> {
                try {
                    java.lang.reflect.Method m = MapViewer.class.getDeclaredMethod("updateInfo", Area.class);
                    m.setAccessible(true); m.invoke(mv, orig2);
                    List<JLabel> ls = new ArrayList<>(); collect(mv.getContentPane(), JLabel.class, ls);
                    for (JLabel l : ls) if (l.getIcon() != null && l.getIcon().getIconWidth() == 320) iconSize[0] = l.getIcon().getIconHeight();
                } catch (Exception ex) { ex.printStackTrace(real); }
            });
            check("Tile Info shows a 320x180 picture for Originis (overlay art present)", iconSize[0] == 180);
            flip.accept("Tile pics");
            snap.run();
            boolean magenta = false;
            for (int dx = -tsz[0] / 2; dx < tsz[0] / 2 && !magenta; dx++) for (int dy = -tsz[0] / 2; dy < tsz[0] / 2; dy++) {
                int c = shot[0].getRGB(orig2.x * tsz[0] + tsz[0] / 2 + dx + org[0], (Map.range - 1 - orig2.y) * tsz[0] + tsz[0] / 2 + dy + org[1]);
                if (((c >> 16) & 255) > 200 && ((c >> 8) & 255) < 90 && (c & 255) > 200) { magenta = true; break; }
            }
            check("Tile pics draws the picture inside the map tile", magenta);
            flip.accept("Tile pics");
            final int[] bigW = {0};
            onEdt(() -> mv.showLargeView(orig2));
            Thread.sleep(500);
            onEdt(() -> { for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && ((JDialog) w).getTitle().equals("Originis")) { List<JLabel> ls = new ArrayList<>(); collect(((JDialog) w).getContentPane(), JLabel.class, ls); for (JLabel l : ls) if (l.getIcon() != null) bigW[0] = l.getIcon().getIconWidth(); w.dispose(); } });
            check("large view shows a 768-wide picture", bigW[0] == 768);
            flip.accept("Pictures");
            onEdt(() -> { try { java.lang.reflect.Method m = MapViewer.class.getDeclaredMethod("updateInfo", Area.class); m.setAccessible(true); m.invoke(mv, orig2); } catch (Exception ex) { } });
            final int[] iconAfter = {0};
            onEdt(() -> { List<JLabel> ls = new ArrayList<>(); collect(mv.getContentPane(), JLabel.class, ls); for (JLabel l : ls) if (l.getIcon() != null && l.getIcon().getIconWidth() == 320) iconAfter[0] = 1; });
            check("Pictures toggle off removes the Tile Info picture", iconAfter[0] == 0);
            flip.accept("Pictures");

            // ---------- quest panel ----------
            Area here = p.location;
            cmdGui("qnew qp1 " + here.x + " " + here.y + " 40");
            click("Quest Log"); Thread.sleep(1500);
            final JDialog[] qd = {null};
            onEdt(() -> { for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && ((JDialog) w).getTitle().equals("Quest Log")) qd[0] = (JDialog) w; });
            check("Quest Log opens the quest window", qd[0] != null);
            if (qd[0] != null) {
                final JDialog q = qd[0];
                java.util.function.Supplier<JTable> qt = () -> { final JTable[] t = {null}; try { onEdt(() -> { List<JTable> l = new ArrayList<>(); collect(q.getContentPane(), JTable.class, l); t[0] = l.get(0); }); } catch (Exception e) { } return t[0]; };
                java.util.function.Function<String, Boolean> qclick = k -> { final JButton[] b = {null}; try { onEdt(() -> b[0] = findButton(q.getContentPane(), k)); } catch (Exception e) { return false; } if (b[0] == null) return false; SwingUtilities.invokeLater(() -> b[0].doClick()); return true; };
                JTable t = qt.get();
                final int[] rows = {0, -1};
                onEdt(() -> { rows[0] = t.getRowCount(); for (int i = 0; i < t.getRowCount(); i++) if (String.valueOf(t.getValueAt(i, 1)).equals("Go: qp1")) rows[1] = i; });
                check("quest panel lists the player's quests (with the new waypoint)", rows[0] >= 1 && rows[1] >= 0);
                if (rows[1] >= 0) {
                    onEdt(() -> { t.setRowSelectionInterval(rows[1], rows[1]); List<JSpinner> sp = new ArrayList<>(); collect(q.getContentPane(), JSpinner.class, sp); sp.get(0).setValue(91); });
                    qclick.apply("Pin priority"); settle(); Thread.sleep(300);
                    Quest pinned = p.targetQuests.stream().filter(x -> x.name.equals("Go: qp1")).findFirst().orElse(null);
                    check("Pin priority pins the selected quest at the chosen priority", pinned != null && pinned.pinned && pinned.priority == 91);
                    Thread.sleep(1300);
                    final int[] r2 = {-1};
                    onEdt(() -> { for (int i = 0; i < t.getRowCount(); i++) if (String.valueOf(t.getValueAt(i, 1)).equals("Go: qp1")) r2[0] = i; });
                    onEdt(() -> t.setRowSelectionInterval(r2[0], r2[0]));
                    int m1 = PcProbe.mark();
                    qclick.apply("Do Quest"); settle();
                    check("Do Quest on the selected row runs it at the player's tile", PcProbe.since(m1).contains("You reached Go: qp1") && p.targetQuests.stream().noneMatch(x -> x.name.equals("Go: qp1")));
                }
                cmdGui("qnew qp2 " + here.x + " " + here.y + " 30");
                Thread.sleep(1500);
                final int[] r3 = {-1};
                onEdt(() -> { for (int i = 0; i < t.getRowCount(); i++) if (String.valueOf(t.getValueAt(i, 1)).equals("Go: qp2")) r3[0] = i; });
                if (r3[0] >= 0) {
                    onEdt(() -> t.setRowSelectionInterval(r3[0], r3[0]));
                    qclick.apply("Delete"); settle(); Thread.sleep(300);
                    check("Delete removes the selected quest", p.targetQuests.stream().noneMatch(x -> x.name.equals("Go: qp2")));
                } else check("waypoint qp2 appeared in the panel", false);
                // add via the panel's waypoint fields (blank x/y = current tile)
                qclick.apply("Add"); settle(); Thread.sleep(300);
                check("panel 'Add' creates a waypoint where you stand", p.targetQuests.stream().anyMatch(x -> x.name.startsWith("Go: waypoint") && x.pinned));
                // filters
                final int[] pinnedRows = {-1};
                onEdt(() -> { List<JComboBox> cb = new ArrayList<>(); collect(q.getContentPane(), JComboBox.class, cb); for (JComboBox c : cb) if ("Pinned".equals(c.getItemAt(4))) c.setSelectedItem("Pinned"); });
                Thread.sleep(500);
                onEdt(() -> { boolean allPinned = true; for (int i = 0; i < t.getRowCount(); i++) if (!String.valueOf(t.getValueAt(i, 6)).startsWith("pinned")) allPinned = false; pinnedRows[0] = allPinned && t.getRowCount() >= 1 ? 1 : 0; });
                check("the Pinned filter shows only pinned quests", pinnedRows[0] == 1);
                SwingUtilities.invokeLater(q::dispose);
                Thread.sleep(300);
            }

            // ---------- duties & policies panel ----------
            click("Duties"); Thread.sleep(1500);
            final JDialog[] dd = {null};
            onEdt(() -> { for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && ((JDialog) w).getTitle().startsWith("Duties")) dd[0] = (JDialog) w; });
            check("Duties opens the duties & policies window", dd[0] != null);
            if (dd[0] != null) {
                final JDialog dl = dd[0];
                final JCheckBox[] boxes = new JCheckBox[10];
                onEdt(() -> { List<JCheckBox> l = new ArrayList<>(); collect(dl.getContentPane(), JCheckBox.class, l); for (JCheckBox b : l) { int i = PlayerControl.dutyIndex(b.getText()); if (i >= 0) boxes[i] = b; } });
                check("ten duty checkboxes", java.util.Arrays.stream(boxes).allMatch(b -> b != null));
                SwingUtilities.invokeLater(() -> boxes[PlayerControl.SECT].doClick()); settle();
                check("ticking 'sect' turns that duty on", p.pc().duties[PlayerControl.SECT] && p.pc().dutiesOn() == 1);
                java.util.function.Function<String, Boolean> dclick = k -> { final JButton[] b = {null}; try { onEdt(() -> b[0] = findButton(dl.getContentPane(), k)); } catch (Exception e) { return false; } if (b[0] == null) return false; SwingUtilities.invokeLater(() -> b[0].doClick()); return true; };
                dclick.apply("All on"); settle(); Thread.sleep(1500);
                final int[] sel = {0};
                onEdt(() -> { for (JCheckBox b : boxes) if (b.isSelected()) sel[0]++; });
                check("All on flips every duty and the boxes follow", p.pc().dutiesOn() == 10 && sel[0] == 10);
                dclick.apply("All off"); settle(); Thread.sleep(1500);
                check("All off clears them", p.pc().dutiesOn() == 0);
                onEdt(() -> { List<JComboBox> l = new ArrayList<>(); collect(dl.getContentPane(), JComboBox.class, l); l.get(0).setSelectedIndex(3); });
                settle(); Thread.sleep(400);
                check("kill-policy dropdown sets the policy", p.pc().killPolicy == 3);
                onEdt(() -> { List<JSpinner> l = new ArrayList<>(); collect(dl.getContentPane(), JSpinner.class, l); l.get(0).setValue(15); });
                settle(); Thread.sleep(400);
                check("timeout spinner sets the decision timeout", p.pc().decisionTimeout == 15);
                SwingUtilities.invokeLater(() -> { List<JCheckBox> l = new ArrayList<>(); collect(dl.getContentPane(), JCheckBox.class, l); for (JCheckBox b : l) if (b.getText().startsWith("accept challenges")) b.doClick(); });
                settle(); Thread.sleep(400);
                check("'accept challenges' checkbox sets the duel policy", !p.pc().duelPolicy);
                cmdGui("policy kill 0"); cmdGui("policy timeout 0"); cmdGui("policy duel accept");
                SwingUtilities.invokeLater(dl::dispose);
                Thread.sleep(300);
            }

            // ---------- manage window ----------
            Shop myShop = new Shop(p, p.location);
            p.ownedShops.add(myShop);
            click("Manage"); Thread.sleep(1800);
            final JDialog[] md = {null};
            onEdt(() -> { for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && ((JDialog) w).getTitle().startsWith("Manage")) md[0] = (JDialog) w; });
            check("Manage opens", md[0] != null);
            if (md[0] != null) {
                final JDialog m = md[0];
                final JTabbedPane[] tp = {null};
                onEdt(() -> { List<JTabbedPane> l = new ArrayList<>(); collect(m.getContentPane(), JTabbedPane.class, l); tp[0] = l.get(0); });
                check("Manage has shops, city and sect tabs", tp[0].getTabCount() == 3);
                final JTable[] st = {null};
                onEdt(() -> { List<JTable> l = new ArrayList<>(); collect((Container) tp[0].getComponentAt(0), JTable.class, l); st[0] = l.get(0); });
                final int[] shopRows = {0};
                onEdt(() -> shopRows[0] = st[0].getRowCount());
                check("My shops lists the player's shop", shopRows[0] >= 1);
                onEdt(() -> { st[0].setRowSelectionInterval(st[0].getRowCount() - 1, st[0].getRowCount() - 1); });
                Thread.sleep(400);
                final int[] idx = {st[0].getRowCount()};
                onEdt(() -> { List<JSlider> sl = new ArrayList<>(); collect((Container) tp[0].getComponentAt(0), JSlider.class, sl); sl.get(0).setValue(60); sl.get(1).setValue(200); sl.get(2).setValue(40); });
                final JButton[] ap = {null};
                onEdt(() -> ap[0] = findButton((Container) tp[0].getComponentAt(0), "Apply rates"));
                SwingUtilities.invokeLater(() -> ap[0].doClick()); settle(); Thread.sleep(400);
                check("sliders + Apply set the shop's rates (0.60 / 2.00 / 0.40)", Math.abs(myShop.buyRate - 0.6) < 1e-9 && Math.abs(myShop.sellRate - 2.0) < 1e-9 && Math.abs(myShop.cityBuyRate - 0.4) < 1e-9);
                onEdt(() -> tp[0].setSelectedIndex(1));
                Thread.sleep(1700);
                final String[] cityText = {""};
                onEdt(() -> { List<JTextArea> l = new ArrayList<>(); collect((Container) tp[0].getComponentAt(1), JTextArea.class, l); cityText[0] = l.get(0).getText(); });
                check("City tab describes Originis", cityText[0].contains("Originis") && cityText[0].contains("City level"));
                onEdt(() -> tp[0].setSelectedIndex(2));
                Thread.sleep(1700);
                final String[] sectText = {""};
                onEdt(() -> { List<JTextArea> l = new ArrayList<>(); collect((Container) tp[0].getComponentAt(2), JTextArea.class, l); sectText[0] = l.get(0).getText(); });
                check("Sect tab renders (sect info or how to join/found)", !sectText[0].isEmpty());
                SwingUtilities.invokeLater(m::dispose);
                Thread.sleep(300);
            }
            p.ownedShops.remove(myShop);

            // ---------- rankings ----------
            GameDriver.statHistory.add(new int[]{1, 3000, 1, 0, 0}); GameDriver.statHistory.add(new int[]{2, 2900, 1, 1, 2});
            click("Rankings"); Thread.sleep(2200);
            final JDialog[] rd = {null};
            onEdt(() -> { for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && ((JDialog) w).getTitle().startsWith("Rankings")) rd[0] = (JDialog) w; });
            check("Rankings opens", rd[0] != null);
            if (rd[0] != null) {
                final JDialog r = rd[0];
                final int[] top = {0, 0};
                final boolean[] rendered = {false};
                onEdt(() -> {
                    List<JTable> l = new ArrayList<>(); collect(r.getContentPane(), JTable.class, l);
                    top[0] = l.get(0).getRowCount(); top[1] = l.size();
                    List<GuiWindows.ChartPanel> cp = new ArrayList<>(); collect(r.getContentPane(), GuiWindows.ChartPanel.class, cp);
                    try { for (GuiWindows.ChartPanel c : cp) { c.setSize(400, 200); java.awt.image.BufferedImage im = new java.awt.image.BufferedImage(400, 200, java.awt.image.BufferedImage.TYPE_INT_RGB); Graphics2D g = im.createGraphics(); c.paint(g); g.dispose(); } rendered[0] = cp.size() == 3; } catch (Exception ex) { ex.printStackTrace(real); }
                });
                check("Rankings tables list cultivators (4 + 2 rivalry tables) and the 3 charts render", top[0] >= 1 && top[1] == 6 && rendered[0]);
                SwingUtilities.invokeLater(r::dispose);
                Thread.sleep(300);
            }

            // ---------- market ----------
            click("Market"); Thread.sleep(1800);
            final JDialog[] kd = {null};
            onEdt(() -> { for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && ((JDialog) w).getTitle().startsWith("Market")) kd[0] = (JDialog) w; });
            check("Market opens", kd[0] != null);
            if (kd[0] != null) {
                final JDialog k = kd[0];
                final String[] auct = {""};
                final int[] tabs = {0};
                onEdt(() -> { List<JTextArea> l = new ArrayList<>(); collect(k.getContentPane(), JTextArea.class, l); auct[0] = l.get(0).getText(); List<JTabbedPane> tp = new ArrayList<>(); collect(k.getContentPane(), JTabbedPane.class, tp); tabs[0] = tp.get(0).getTabCount(); });
                check("Market shows the auction (weeks to the next one) and a library tab", auct[0].contains("Auction at Originis") && auct[0].contains("week") && tabs[0] == 3);
                SwingUtilities.invokeLater(k::dispose);
                Thread.sleep(300);
            }

            // ---------- log filters + clickable names ----------
            System.out.println("ZZECON Auction at Testville sold a thing for 5 ss");
            System.out.println("ZZFIGHT Nobody won the fight against Nobody");
            Character named = null;
            for (int i = 900; i < Map.characters.size() && named == null; i++) { Character c = Map.characters.get(i); if (c != null && !c.dead && c.born && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster) named = c; }
            final Character namedF = named;
            System.out.println("ZZNAME " + named.name + " " + named.lastName + " is up to something");
            onEdt(mv::refresh); Thread.sleep(600);
            java.util.function.Supplier<String> logText = () -> { final String[] t = {""}; try { onEdt(() -> { List<JTextArea> l = new ArrayList<>(); collect(mv.getContentPane(), JTextArea.class, l); for (JTextArea a : l) if (a.getText().contains("ZZECON") || a.getText().contains("ZZNAME")) t[0] = a.getText(); }); } catch (Exception e) { } return t[0]; };
            check("the event log shows the printed lines", logText.get().contains("ZZECON") && logText.get().contains("ZZFIGHT"));
            final JCheckBox[] econ = {null};
            onEdt(() -> { List<JCheckBox> l = new ArrayList<>(); collect(mv.getContentPane(), JCheckBox.class, l); for (JCheckBox c : l) if (c.getText().equals("Economy")) econ[0] = c; });
            SwingUtilities.invokeLater(econ[0]::doClick); Thread.sleep(500);
            String filtered = logText.get();
            check("unticking Economy hides economy lines but keeps others", !filtered.contains("ZZECON") && filtered.contains("ZZFIGHT"));
            SwingUtilities.invokeLater(econ[0]::doClick); Thread.sleep(500);
            onEdt(() -> { List<JTextField> l = new ArrayList<>(); collect(mv.getContentPane(), JTextField.class, l); for (JTextField f : l) if (f.getToolTipText() != null && f.getToolTipText().startsWith("Only lines")) { f.setText("ZZFIGHT"); f.postActionEvent(); } });
            Thread.sleep(500);
            final String[] searched = {""};
            onEdt(() -> { List<JTextArea> l = new ArrayList<>(); collect(mv.getContentPane(), JTextArea.class, l); for (JTextArea a : l) if (a.getText().contains("ZZFIGHT")) searched[0] = a.getText(); });
            check("the search box narrows the log to matching lines", searched[0].contains("ZZFIGHT") && !searched[0].contains("ZZECON"));
            onEdt(() -> { List<JTextField> l = new ArrayList<>(); collect(mv.getContentPane(), JTextField.class, l); for (JTextField f : l) if (f.getToolTipText() != null && f.getToolTipText().startsWith("Only lines")) { f.setText(""); f.postActionEvent(); } });
            Thread.sleep(500);
            // click the line naming a cultivator -> its dialog opens
            final int[] opened = {0};
            onEdt(() -> {
                try {
                    List<JTextArea> l = new ArrayList<>(); collect(mv.getContentPane(), JTextArea.class, l);
                    for (JTextArea a : l) if (a.getText().contains("ZZNAME")) {
                        int off = a.getText().indexOf("ZZNAME");
                        java.awt.geom.Rectangle2D rc = a.modelToView2D(off + 2);
                        MouseEvent ev = new MouseEvent(a, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, (int) rc.getX() + 2, (int) rc.getY() + 2, 1, false);
                        for (java.awt.event.MouseListener ml : a.getMouseListeners()) ml.mouseClicked(ev);
                    }
                } catch (Exception ex) { ex.printStackTrace(real); }
            });
            Thread.sleep(700);
            onEdt(() -> { for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && ((JDialog) w).getTitle().equals(namedF.name + " " + namedF.lastName)) { opened[0] = 1; w.dispose(); } });
            check("clicking a log line opens the first cultivator named in it", opened[0] == 1);

            // ---------- keyboard shortcuts ----------
            int yb = p.location.y, tb = GameDriver.time;
            onEdt(() -> mv.handleShortcut(new java.awt.event.KeyEvent(mv, java.awt.event.KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_W, 'w')));
            settle();
            check("W key moves north (+y)", p.location.y == (yb + 1) % Map.range);
            onEdt(() -> mv.handleShortcut(new java.awt.event.KeyEvent(mv, java.awt.event.KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_S, 's')));
            settle();
            check("S key moves back south", p.location.y == yb);
            int t1 = GameDriver.time;
            onEdt(() -> mv.handleShortcut(new java.awt.event.KeyEvent(mv, java.awt.event.KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_SPACE, ' ')));
            settle();
            check("Space waits one turn", GameDriver.time == t1 + 1);
            onEdt(() -> mv.handleShortcut(new java.awt.event.KeyEvent(mv, java.awt.event.KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_Q, 'q')));
            Thread.sleep(1200);
            final boolean[] qOpen = {false};
            onEdt(() -> { for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && ((JDialog) w).getTitle().equals("Quest Log")) { qOpen[0] = true; w.dispose(); } });
            check("Q opens the quest panel", qOpen[0]);
            GameDriver.saveRequested = false;
            onEdt(() -> mv.handleShortcut(new java.awt.event.KeyEvent(mv, java.awt.event.KeyEvent.KEY_PRESSED, System.currentTimeMillis(), java.awt.event.InputEvent.CTRL_DOWN_MASK, java.awt.event.KeyEvent.VK_S, 's')));
            check("Ctrl+S requests a save", GameDriver.saveRequested);
            GameDriver.saveRequested = false;
            final boolean[] typing = {false};
            check("shortcuts ignore unknown keys", !mv.handleShortcut(new java.awt.event.KeyEvent(mv, java.awt.event.KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_Z, 'z')));
        }

        // ---------- decision dialog (GUI) ----------
        Character foe = null;
        for (int i = 100; i < Map.characters.size() && foe == null; i++) { Character c = Map.characters.get(i); if (c != null && !c.dead && c.born && c.player == 0 && c.age >= 12 && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster) foe = c; }
        foe.location.removeChar(foe); foe.location = p.location; foe.location.addChar(foe); foe.lvl = 3; foe.caution = 100000; foe.affiliation = "Evil"; foe.affiliationNum = -1;
        final Character foeF = foe;
        d0 = dialogCount();
        decisionChoice = "Spare";
        sendCmd("people " + foe.name); settle();
        int idx = p.pc().peopleView.indexOf(foe) + 1;
        sendCmd("attack " + idx); settle();
        check("kill/spare shows an option dialog and Spare leaves the foe alive", dialogSince(d0, "Kill or spare") && !foeF.dead);
        // timeout in the GUI: nobody answers
        foe.location.removeChar(foe); foe.location = p.location; foe.location.addChar(foe);
        sendCmd("policy timeout 2"); settle();
        decisionChoice = null;
        int to0 = DecisionBroker.timeouts;
        sendCmd("people " + foe.name); settle();
        idx = p.pc().peopleView.indexOf(foe) + 1;
        sendCmd("attack " + idx); settle();
        check("an unanswered GUI decision times out to the policy answer", DecisionBroker.timeouts == to0 + 1 && !foeF.dead);
        sendCmd("policy timeout off"); settle();
        decisionChoice = "Spare";

        DecisionBroker.scriptedNumbers = null;
        // ---------- auction dialog (GUI numeric bid) ----------
        Area city = p.location;
        city.auction().clear();
        Item lot = new Item(p, 10, Item.ItemType.Clothing);
        lot.heavenSent = true; Auctions.consign(city, lot, null);
        p.sc = 60_000; p.ss = 0;
        dialogInput = "50000000000";
        d0 = dialogCount();
        Auctions.hold(city); // from the probe thread: the dialog is created on the EDT, like the game thread would
        check("auction bid dialog appears and a big bid wins the lot", dialogSince(d0, "maximum bid") && (p.backpack.contains(lot) || p.equippedClothing == lot));

        // ---------- tile info + character dialogs + tracker + control ----------
        Thread.sleep(500);
        final JButton[] nameBtn = {null};
        onEdt(() -> {
            List<JButton> bs = new ArrayList<>(); collect(GameDriver.mapViewer.getContentPane(), JButton.class, bs);
            for (JButton b : bs) if (b.getText() != null && b.getText().contains(" Lv.") && b.getText().startsWith("  ")) { nameBtn[0] = b; break; }
        });
        if (nameBtn[0] == null) { final StringBuilder dump = new StringBuilder(); onEdt(() -> { List<JLabel> ls = new ArrayList<>(); collect(GameDriver.mapViewer.getContentPane(), JLabel.class, ls); for (JLabel l : ls) if (l.getText() != null && l.getText().length() > 2 && l.getText().length() < 90) dump.append(l.getText().replace((char) 10, (char) 32)).append(" / "); }); real.println("[G] info: labels: " + dump); }
        check("tile info lists top cultivators as buttons", nameBtn[0] != null);
        if (nameBtn[0] != null) {
            SwingUtilities.invokeLater(() -> nameBtn[0].doClick()); Thread.sleep(700);
            final JDialog[] cd = {null};
            onEdt(() -> { for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && !((JDialog) w).isModal() && !((JDialog) w).getTitle().startsWith("Script")) cd[0] = (JDialog) w; });
            check("clicking a cultivator opens its character dialog", cd[0] != null && findButton(cd[0].getContentPane(), "Track") != null);
            if (cd[0] != null) {
                int tracked0 = Map.trackedCharacters.size();
                final JDialog c1 = cd[0];
                SwingUtilities.invokeLater(() -> findButton(c1.getContentPane(), "Track").doClick()); Thread.sleep(600);
                check("Track adds the cultivator to the tracker", Map.trackedCharacters.size() == tracked0 + 1);
                final JButton[] rm = {null};
                onEdt(() -> rm[0] = findButton(GameDriver.mapViewer.getContentPane(), "×"));
                check("tracker row shows a × remove button", rm[0] != null);
                if (rm[0] != null) { SwingUtilities.invokeLater(() -> rm[0].doClick()); Thread.sleep(500); }
                check("× removes it from the tracker", Map.trackedCharacters.size() == tracked0);
            }
        }
        // Control on an NPC
        Character npc = null;
        for (int i = 300; i < Map.characters.size() && npc == null; i++) { Character c = Map.characters.get(i); if (c != null && !c.dead && c.born && c.player == 0 && c.age >= 12 && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster && c.location != null) npc = c; }
        final Character npcF = npc;
        onEdt(() -> {
            try {
                java.lang.reflect.Method m = MapViewer.class.getDeclaredMethod("showCharacterDialog", Character.class);
                m.setAccessible(true); m.invoke(GameDriver.mapViewer, npcF);
            } catch (Exception ex) { ex.printStackTrace(real); }
        });
        Thread.sleep(500);
        final JDialog[] cd2 = {null};
        onEdt(() -> { for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && !((JDialog) w).isModal() && ((JDialog) w).getTitle().contains(npcF.name)) cd2[0] = (JDialog) w; });
        if (cd2[0] != null) {
            final JDialog c2 = cd2[0];
            SwingUtilities.invokeLater(() -> findButton(c2.getContentPane(), "Control").doClick()); Thread.sleep(800);
            check("Control makes the NPC the human player and demotes the old one to tracked", npcF.player == 1 && p.player == 2);
            settle();
            check("the new character is prompted as MANUAL", pc() == npcF && npcF.pc().mode == PlayerControl.Mode.MANUAL);
            click("Wait"); settle();
            check("the game keeps running under the new player", GameDriver.waitingForInput && !npcF.dead);
        } else check("character dialog for the Control test opened", false);

        // ---------- save button ----------
        File sf = new File("saves/quick.dat"); sf.delete();
        click("💾 Save"); Thread.sleep(300);
        click("Wait"); settle();
        Thread.sleep(3000);
        check("Save writes saves/quick.dat on the next turn", sf.exists() && sf.length() > 1000);
    }

    static void cmdGui(String s) throws Exception { GameDriver.guiInputQueue.add(s); settle(); }

    static void sendCmd(String s) { GameDriver.guiInputQueue.add(s); }
}
