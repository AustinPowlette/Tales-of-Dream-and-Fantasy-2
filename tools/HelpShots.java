package Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/** Makes the screenshots the in-game Help shows (assets/help/*.png) from the real UI: it starts the game with a 3,000-person world and a human player,
 *  plays a little so there is something to see, opens every window, renders it into a PNG (scaled to at most ~820 px wide) and closes it again.
 *
 *    (copy src/Game/*.java somewhere, set Map.startingPopulation to 3000 there)
 *    javac -encoding UTF-8 -cp "src;lib/flatlaf-3.7.2.jar" -d out src/Game/*.java tools/HelpShots.java
 *    mkdir run && cd run && java -Xss512m -cp "../out;../lib/flatlaf-3.7.2.jar" Game.HelpShots ../assets/help
 *
 *  Run it in an EMPTY directory (a leftover saves/ folder adds a start-up menu) on a machine with a display. It takes a few minutes. The window titles and
 *  button labels are looked up by text, so when the UI is rearranged a missing shot is reported ("MISSING ...") and the others are still made. */
public class HelpShots {
    static class FeedIn extends InputStream {
        final LinkedBlockingQueue<byte[]> q = new LinkedBlockingQueue<>();
        byte[] cur = null; int pos = 0;
        @Override public int read() throws IOException { byte[] b = new byte[1]; int n = read(b, 0, 1); return n < 0 ? -1 : (b[0] & 0xff); }
        @Override public int read(byte[] b, int off, int len) throws IOException {
            if (cur == null || pos >= cur.length) { try { cur = q.take(); } catch (InterruptedException e) { return -1; } pos = 0; }
            int n = Math.min(len, cur.length - pos);
            System.arraycopy(cur, pos, b, off, n);
            pos += n;
            return n;
        }
        @Override public int available() { return cur != null ? cur.length - pos : 0; }
    }

    static File out = new File("assets/help");
    static PrintStream real;
    static MapViewer mv;
    static volatile boolean watching = true;
    static final Set<Window> seen = Collections.newSetFromMap(new WeakHashMap<>());
    static int made = 0, missing = 0;
    static long bytes = 0;
    static volatile boolean grabStart = true;

    // ------------------------------------------------------------------ helpers
    static <T extends Component> void collect(Container c, Class<T> cls, List<T> o) {
        for (Component k : c.getComponents()) { if (cls.isInstance(k)) o.add(cls.cast(k)); if (k instanceof Container) collect((Container) k, cls, o); }
    }
    static JButton button(Container root, String text) {
        List<JButton> bs = new ArrayList<>(); collect(root, JButton.class, bs);
        for (JButton b : bs) if (b.getText() != null && b.getText().trim().equals(text)) return b;
        for (JButton b : bs) if (b.getText() != null && b.getText().contains(text)) return b;
        return null;
    }
    static void edt(Runnable r) throws Exception { SwingUtilities.invokeAndWait(r); }
    static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException ignored) { } }

    static Character player() {
        int n = Map.characters.size();
        for (int i = 0; i < n; i++) { Character c = Map.characters.get(i); if (c != null && c.player == 1 && !c.dead) return c; }
        return null;
    }

    /** Waits until the game is waiting for a command and has nothing queued. */
    static void idle() {
        long end = System.currentTimeMillis() + 180000;
        int stable = 0;
        while (System.currentTimeMillis() < end) {
            if (GameDriver.waitingForInput && GameDriver.guiInputQueue.isEmpty()) { if (++stable >= 4) return; } else stable = 0;
            sleep(80);
        }
        real.println("WARN idle timeout");
    }
    static void cmd(String s) { mv.queueInput(s); sleep(300); idle(); }

    static String titleOf(Window w) { return w instanceof JDialog ? ((JDialog) w).getTitle() : w instanceof Frame ? ((Frame) w).getTitle() : ""; }

    static Window find(String prefix) {
        final Window[] r = {null};
        try { edt(() -> { for (Window w : Window.getWindows()) { String t = titleOf(w); if (w.isShowing() && t != null && t.startsWith(prefix)) r[0] = w; } }); } catch (Exception ignored) { }
        return r[0];
    }

    // ------------------------------------------------------------------ rendering
    static BufferedImage paint(Component c) {
        int w = Math.max(1, c.getWidth()), h = Math.max(1, c.getHeight());
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        try { c.paint(g); } finally { g.dispose(); }
        return img;
    }

    /** A fake window frame (title strip) above the content, so a window picture shows its title like on screen. */
    static BufferedImage framed(BufferedImage content, String title) {
        int bar = 30;
        BufferedImage img = new BufferedImage(content.getWidth(), content.getHeight() + bar, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Theme.PANEL2); g.fillRect(0, 0, img.getWidth(), bar);
        g.setColor(Theme.LINE); g.drawLine(0, bar - 1, img.getWidth(), bar - 1);
        g.setFont(Theme.bold(14)); g.setColor(Theme.TEXT); g.drawString(title, 12, 20);
        g.setFont(Theme.plain(16)); g.setColor(Theme.DIM); g.drawString("—   □   ✕", img.getWidth() - 74, 21);
        g.drawImage(content, 0, bar, null);
        g.setColor(Theme.LINE); g.drawRect(0, 0, img.getWidth() - 1, img.getHeight() - 1);
        g.dispose();
        return img;
    }

    static BufferedImage scaled(BufferedImage im, int maxW) {
        if (im.getWidth() <= maxW) return im;
        int w = maxW, h = Math.round(im.getHeight() * (maxW / (float) im.getWidth()));
        BufferedImage cur = im;
        // halve in steps for a clean result, then the final size
        while (cur.getWidth() / 2 >= w * 1.0 && cur.getWidth() / 2 > w) {
            BufferedImage t = new BufferedImage(cur.getWidth() / 2, cur.getHeight() / 2, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = t.createGraphics(); g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(cur, 0, 0, t.getWidth(), t.getHeight(), null); g.dispose(); cur = t;
        }
        BufferedImage t = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = t.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(cur, 0, 0, w, h, null); g.dispose();
        return t;
    }

    static void save(BufferedImage im, String name, int maxW) {
        try {
            BufferedImage s = scaled(im, maxW);
            File f = new File(out, name);
            f.getParentFile().mkdirs();
            javax.imageio.ImageIO.write(s, "png", f);
            made++; bytes += f.length();
            real.println("  made " + name + "  " + s.getWidth() + "x" + s.getHeight() + "  " + f.length() / 1024 + " KB");
        } catch (Exception e) { real.println("FAILED " + name + ": " + e); }
    }

    /** Opens a window with `opener` (on the Swing thread), waits for it to fill in, pictures it and closes it. */
    static void windowShot(String name, String titlePrefix, int waitMs, int maxW, Runnable opener, Runnable beforeShot) {
        try {
            edt(opener);
            sleep(waitMs);
            Window w = find(titlePrefix);
            if (w == null) { real.println("MISSING window '" + titlePrefix + "' for " + name); missing++; return; }
            if (beforeShot != null) { beforeShot.run(); sleep(700); }
            final BufferedImage[] im = {null};
            final Window ww = w;
            edt(() -> im[0] = paint(((RootPaneContainer) ww).getRootPane()));
            save(framed(im[0], titleOf(w)), name, maxW);
            edt(w::dispose);
            sleep(200);
        } catch (Exception e) { real.println("FAILED " + name + ": " + e); e.printStackTrace(real); missing++; }
    }
    static void windowShot(String name, String titlePrefix, int waitMs, Runnable opener) { windowShot(name, titlePrefix, waitMs, 820, opener, null); }

    static void selectTab(Window w, String tab) {
        try {
            edt(() -> {
                List<JTabbedPane> tps = new ArrayList<>(); collect(((RootPaneContainer) w).getContentPane(), JTabbedPane.class, tps);
                for (JTabbedPane tp : tps) { int i = tp.indexOfTab(tab); if (i < 0) for (int k = 0; k < tp.getTabCount(); k++) if (tp.getTitleAt(k).startsWith(tab)) i = k; if (i >= 0) { tp.setSelectedIndex(i); return; } }
            });
        } catch (Exception ignored) { }
    }

    static Component findComp(Container root, java.util.function.Predicate<Component> p) {
        for (Component k : root.getComponents()) { if (p.test(k)) return k; if (k instanceof Container) { Component f = findComp((Container) k, p); if (f != null) return f; } }
        return null;
    }

    // ------------------------------------------------------------------ modal dialogs (start-up questions, decisions)
    static void watcher() {
        while (watching) {
            sleep(100);
            try {
                for (Window w : Window.getWindows()) {
                    if (!(w instanceof JDialog) || !w.isShowing() || seen.contains(w)) continue;
                    final JDialog d = (JDialog) w;
                    if (!d.isModal()) continue;
                    seen.add(d);
                    final String title = d.getTitle();
                    if (title.startsWith("A new life") && grabStart) { grabStart = false; sleep(500); try { edt(() -> save(framed(paint(d.getRootPane()), d.getTitle()), "win-start.png", 820)); } catch (Exception ignored) { } }
                    SwingUtilities.invokeLater(() -> {
                        String[] prefer = title.startsWith("Tales") ? new String[]{"Single player"} : title.startsWith("A new life") ? new String[]{"Begin"} : title.equals("Your character") ? new String[]{"No"}
                                : title.startsWith("Decision") ? new String[]{"Spare", "Slip away", "Flee", "Fight", "Don't"} : new String[]{"No", "OK", "Done", "Yes"};
                        for (String p : prefer) { JButton b = button(d.getContentPane(), p); if (b != null) { b.doClick(); return; } }
                        d.dispose();
                    });
                }
            } catch (RuntimeException ignored) { }
        }
    }

    // ------------------------------------------------------------------ main
    public static void main(String[] a) throws Exception {
        if (a.length > 0) out = new File(a[0]);
        System.setProperty("java.awt.headless", "false");
        real = System.out;
        FeedIn in = new FeedIn();
        in.q.add("y\n".getBytes());
        System.setIn(in);
        System.setOut(new PrintStream(new ByteArrayOutputStream(), true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { e.printStackTrace(real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        Thread w = new Thread(HelpShots::watcher, "dialog-watcher"); w.setDaemon(true); w.start();
        try { run(); } catch (Throwable e) { real.println("HELPSHOTS EXCEPTION " + e); e.printStackTrace(real); }
        real.println("HelpShots: " + made + " pictures, " + missing + " missing, " + bytes / 1024 + " KB in " + out.getPath());
        watching = false;
        System.exit(0);
    }

    static void run() throws Exception {
        long end = System.currentTimeMillis() + 240000;
        while (System.currentTimeMillis() < end && !(GameDriver.mapViewer != null && GameDriver.waitingForInput && player() != null)) sleep(200);
        mv = GameDriver.mapViewer;
        if (mv == null || player() == null) { real.println("the game did not start"); return; }
        GameDriver.tickDelayMs = 0;
        GameDriver.paused = false;
        sleep(800);
        for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && titleOf(w).equals("Tutorial")) { edt(w::dispose); }
        edt(() -> { mv.setSize(1180, 800); mv.setLocation(20, 20); mv.validate(); });
        sleep(800);
        Settings.hints = false; Settings.lorePause = false; // a lore scene must not pause the game while the shots are taken

        // ---- a life worth looking at: some money, travel, a fight or two, quests
        Character p = player();
        cmd("cheat self ss 400000");
        cmd("cheat self weapon 3");
        for (String go : new String[]{"goto 1800 1500", "goto 1800 1800", "goto 1400 1800", "goto 1200 1400", "goto 1500 1500"}) cmd(go);
        cmd("wait 40");
        p = player();
        real.println("player: " + p.name + " " + p.lastName + " lvl " + p.lvl + " at " + p.location.name);

        // ---- the main window
        screens();
        // ---- windows
        windows(p);
        // ---- the tour
        tour();
        // ---- decision picture
        decision();
        real.println("done");
    }

    // ------------------------------------------------------------------ main window pictures
    static void screens() throws Exception {
        edt(() -> mv.refreshNow());
        sleep(1200);
        // click a tile near the player so Tile Info and the path layer show something
        final Component mp = findComp(mv.getContentPane(), c -> c.getClass().getSimpleName().equals("MapPanel"));
        if (mp != null) {
            Character p = player();
            int tx = (p.location.x + 4) % Map.range, ty = (p.location.y + 3) % Map.range;
            clickTile(mp, tx, ty);
        }
        sleep(800);
        edt(() -> { mv.refreshNow(); });
        sleep(800);
        final BufferedImage[] whole = {null};
        edt(() -> whole[0] = paint(mv.getRootPane()));
        save(framed(whole[0], mv.getTitle()), "screen-main.png", 1000);

        // map variants (just the square world)
        final String[] modes = {"map-terrain.png", "map-beasts.png", "map-population.png", "map-level.png"};
        for (int i = 0; i < modes.length; i++) {
            final int mi = i;
            edt(() -> { mv.colorMode = mi; mv.layerCrowd = false; mv.layerFog = false; mv.layerQuests = false; mv.layerPath = false; mv.refreshNow(); });
            sleep(700);
            mapShot(modes[i], mp);
        }
        edt(() -> { mv.colorMode = 0; mv.layerCrowd = true; mv.layerFog = true; mv.layerQuests = true; mv.layerPath = true; mv.refreshNow(); });
        sleep(900);
        mapShot("map-layers.png", mp);
        edt(() -> { mv.layerFog = false; mv.layerQuests = false; mv.layerPath = false; mv.layerCrowd = true; mv.refreshNow(); });
        sleep(600);

        // the layer bar on top of the map
        Component tog = findComp(mv.getContentPane(), c -> c instanceof JToggleButton && "Fog".equals(((JToggleButton) c).getText()));
        if (tog != null) { final Component bar = tog.getParent(); edt(() -> save(paint(bar), "layer-bar.png", 1000)); }
        // Tile Info + tracker
        Component ti = findComp(mv.getContentPane(), c -> c instanceof JScrollPane && ((JScrollPane) c).getBorder() instanceof javax.swing.border.TitledBorder && "Tile Info".equals(((javax.swing.border.TitledBorder) ((JScrollPane) c).getBorder()).getTitle()));
        if (ti != null) { final Component f = ti; edt(() -> save(paint(f), "tile-info.png", 600)); } else { real.println("MISSING Tile Info"); missing++; }
        Component tr = findComp(mv.getContentPane(), c -> c instanceof JScrollPane && ((JScrollPane) c).getBorder() instanceof javax.swing.border.TitledBorder && "NPC Tracker".equals(((javax.swing.border.TitledBorder) ((JScrollPane) c).getBorder()).getTitle()));
        if (tr != null) { final Component f = tr; edt(() -> save(paint(f), "tracker.png", 600)); }
        // status bar and speed bar
        Component pb = findComp(mv.getContentPane(), c -> c instanceof JProgressBar);
        if (pb != null) { final Component f = pb.getParent(); edt(() -> save(paint(f), "status-bar.png", 1000)); } else { real.println("MISSING status bar"); missing++; }
        Component pz = findComp(mv.getContentPane(), c -> c instanceof JButton && ((JButton) c).getText() != null && ((JButton) c).getText().contains("Pause"));
        if (pz != null) { final Component f = pz.getParent(); edt(() -> save(paint(f), "speed-bar.png", 800)); } else { real.println("MISSING speed bar"); missing++; }
        // event log
        Component lg = findComp(mv.getContentPane(), c -> c instanceof JTabbedPane && ((JTabbedPane) c).indexOfTab("Events") >= 0);
        if (lg != null) { final Component f = lg; edt(() -> save(paint(f), "event-log.png", 1000)); } else { real.println("MISSING event log"); missing++; }
        // the dock, one tab at a time
        final JTabbedPane dock = (JTabbedPane) findComp(mv.getContentPane(), c -> c instanceof JTabbedPane && ((JTabbedPane) c).indexOfTab("Move") >= 0);
        if (dock != null) {
            String[] tabs = {"Move", "Me", "Auto", "World"};
            for (final String tab : tabs) {
                edt(() -> dock.setSelectedIndex(dock.indexOfTab(tab)));
                sleep(500);
                edt(() -> { Component sel = dock.getSelectedComponent(); if (sel instanceof JScrollPane) ((JScrollPane) sel).getVerticalScrollBar().setValue(0); });
                edt(() -> save(paint(dock), "dock-" + tab.toLowerCase() + ".png", 700));
            }
            edt(() -> dock.setSelectedIndex(0));
        } else { real.println("MISSING dock"); missing++; }
    }

    static void clickTile(Component mp, int tx, int ty) throws Exception {
        edt(() -> {
            int ts = Math.max(4, Math.min(mp.getWidth(), mp.getHeight()) / Map.range);
            int ox = Math.max(0, (mp.getWidth() - Map.range * ts) / 2), oy = Math.max(0, (mp.getHeight() - Map.range * ts) / 2);
            int px = ox + tx * ts + ts / 2, py = oy + (Map.range - 1 - ty) * ts + ts / 2;
            for (java.awt.event.MouseListener l : mp.getMouseListeners()) l.mouseClicked(new MouseEvent(mp, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, px, py, 1, false));
        });
    }

    static void mapShot(String name, Component mp) throws Exception {
        if (mp == null) { real.println("MISSING map panel"); missing++; return; }
        edt(() -> {
            BufferedImage full = paint(mp);
            int ts = Math.max(4, Math.min(mp.getWidth(), mp.getHeight()) / Map.range), side = ts * Map.range;
            int ox = Math.max(0, (mp.getWidth() - side) / 2), oy = Math.max(0, (mp.getHeight() - side) / 2);
            BufferedImage crop = full.getSubimage(Math.max(0, ox - 3), Math.max(0, oy - 3), Math.min(full.getWidth() - Math.max(0, ox - 3), side + 6), Math.min(full.getHeight() - Math.max(0, oy - 3), side + 6));
            save(crop, name, 560);
        });
    }

    // ------------------------------------------------------------------ windows
    static void windows(Character p) throws Exception {
        windowShot("win-stats.png", "Stats", 2000, () -> GuiSheets.statsWindow(mv));
        windowShot("win-backpack-gear.png", "Backpack", 2000, 820, () -> GuiMore.backpackWindow(mv, "Gear"), null);
        windowShot("win-backpack-techniques.png", "Backpack", 2000, 820, () -> GuiMore.backpackWindow(mv, "Techniques"), () -> { Window w = find("Backpack"); if (w != null) selectTab(w, "Techniques"); });
        // companions: make the player a tamer for the picture, if a beast is around
        windowShot("win-companions.png", "Backpack", 2000, 820, () -> GuiMore.backpackWindow(mv, "Companions"), () -> { Window w = find("Backpack"); if (w != null) selectTab(w, "Companions"); });
        windowShot("win-relations.png", "Relations", 2600, () -> GuiSheets.relationsWindow(mv));
        windowShot("win-family.png", "Family", 3500, 860, () -> GuiExtras.familyWindow(mv), null);
        windowShot("win-squad.png", "Squad", 2600, () -> GuiSheets.squadWindow(mv));
        windowShot("win-locations.png", "Locations", 2800, () -> GuiSheets.locationsWindow(mv));
        windowShot("win-painted-map.png", "Painted map", 6000, 760, () -> GuiMore.paintedMapWindow(mv), null);
        windowShot("win-biography.png", "Biography", 4500, 860, () -> GuiMore.biographyWindow(mv, mv.findPlayer()), null);
        windowShot("win-story.png", "Story", 2500, 700, () -> GuiExtras.storyWindow(mv), null);
        windowShot("win-run.png", "Run statistics", 4500, 860, () -> GuiMore.runWindow(mv), null);
        windowShot("win-cheats.png", "Cheats", 1500, 600, () -> GuiSheets.cheatWindow(mv), null);
        windowShot("win-quests.png", "Quest Log", 2500, 1000, () -> GuiWindows.questPanel(mv), null);
        windowShot("win-duties.png", "Duties", 2500, 700, () -> GuiWindows.dutiesPanel(mv), null);
        windowShot("win-oldest-order.png", "Journal of the Oldest Order", 2500, 760, () -> GuiLore.journal(mv), null);
        windowShot("win-lore-scene.png", "???", 2500, 700, () -> { List<String> ln = LoreText.lines("bell", Lore.vars(p, Lore.pstate(p))); GuiLore.scene("bell", LoreText.SCENES.get("bell").title, ln); }, null);
        windowShot("win-browse.png", "Browser", 3000, 900, () -> { Component b = button(mv.getContentPane(), "Browse"); if (b != null) ((JButton) b).doClick(); }, null);
        windowShot("win-manage-shops.png", "Manage", 2500, 860, () -> GuiWindows.managePanel(mv), () -> { Window w = find("Manage"); if (w != null) selectTab(w, "My shops"); });
        windowShot("win-manage-city.png", "Manage", 2500, 860, () -> GuiWindows.managePanel(mv), () -> { Window w = find("Manage"); if (w != null) selectTab(w, "City here"); });
        windowShot("win-manage-sect.png", "Manage", 2500, 860, () -> GuiWindows.managePanel(mv), () -> { Window w = find("Manage"); if (w != null) selectTab(w, "Sect"); });
        windowShot("win-rankings.png", "Rankings", 4500, 900, () -> GuiWindows.rankingsPanel(mv), null);
        { // a stocked auction block and library for the market pictures
            Area ha = p.location; ha.city = true; if (ha.population < 500) ha.population = 800;
            if (ha.libraries.isEmpty()) ha.libraries.add(new Library(ha));
            Library hl = ha.libraries.get(0); hl.techniques.clear();
            for (int i = 0; i < 7; i++) hl.shelve(new Technique(i % 2 == 0 ? Technique.techniqueType.Martial : Technique.techniqueType.Movement, p, 6 + i * 9));
            ha.auction().clear();
            Item hw = new Item(p, Item.WeaponType.Spear, 30); hw.name = "Heaven-Sent " + hw.name; hw.heavenSent = true; Auctions.consign(ha, hw, null);
            Auctions.consign(ha, new Item(Item.ItemType.Pill, Item.PillType.Potential, 11, 2), null);
            Item hp = new Item(Item.ItemType.Poison, 6, 5, "Vintage Nightshade Draught"); Auctions.consign(ha, hp, null);
            ha.auctionSince = Math.max(1, GameDriver.time - 2);
        }
        windowShot("win-market-auction.png", "Market", 2500, 860, () -> GuiWindows.marketPanel(mv), () -> { Window w = find("Market"); if (w != null) selectTab(w, "Auction"); });
        windowShot("win-market-goods.png", "Market", 2500, 860, () -> GuiWindows.marketPanel(mv), () -> { Window w = find("Market"); if (w != null) selectTab(w, "Goods"); });
        windowShot("win-market-library.png", "Market", 2500, 860, () -> GuiWindows.marketPanel(mv), () -> { Window w = find("Market"); if (w != null) selectTab(w, "Library"); });
        windowShot("win-chronicle.png", "Chronicle", 3500, 900, () -> GuiMore.chronicleWindow(mv), null);
        windowShot("win-world.png", "World overview", 3000, 760, () -> GuiMore.worldWindow(mv), null);
        cmd("note a place worth remembering");
        windowShot("win-notes.png", "Notes", 2500, 700, () -> GuiMore.notesWindow(mv), null);
        windowShot("win-digest.png", "What happened", 2500, 860, () -> GuiMore.digestWindow(mv, 52), null);
        cmd("save helpshots");
        cmd("wait 1");
        windowShot("win-saves.png", "Saves", 3200, 900, () -> GuiExtras.savesWindow(mv), null);
        windowShot("win-settings.png", "Settings", 1800, 700, () -> GuiExtras.settingsWindow(mv), null);
        windowShot("win-notifications.png", "Notifications", 2500, 860, () -> GuiExtras.notificationsWindow(mv), null);
        windowShot("win-keys.png", "Keyboard shortcuts", 800, 700, () -> mv.showKeys(), null);
        // the character window of someone else
        Character other = null;
        for (int i = 0; i < Map.characters.size(); i++) { Character c = Map.characters.get(i); if (c != null && c != p && !c.dead && c.player == 0 && c.lvl > 15 && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster && c.born) { other = c; break; } }
        if (other != null) {
            final Character o = other;
            windowShot("win-character.png", o.name, 1500, 520, () -> mv.showCharacterDialog(o), null);
        }
        // the script editor with a small script
        cmd("script clear");
        for (String s : new String[]{"goto 1800 1500", "repeat 2", "forage 5", "goto 1500 1500", "sell", "end"}) cmd("script add " + s);
        windowShot("win-scripts.png", "Script Editor", 2000, 700, () -> { Component b = button(mv.getContentPane(), "Scripts"); if (b != null) ((JButton) b).doClick(); }, null);
        cmd("script clear");
    }

    static void tour() throws Exception {
        try {
            edt(() -> GuiHelp.tutorial(mv));
            sleep(1500);
            Window tw = find("Tutorial");
            if (tw == null) { real.println("MISSING tutorial"); missing++; return; }
            final BufferedImage[] im = {null};
            edt(() -> im[0] = paint(((RootPaneContainer) tw).getRootPane()));
            save(framed(im[0], "Tutorial"), "win-tutorial.png", 700);
            JButton next = button(((RootPaneContainer) tw).getContentPane(), "Next");
            if (next == null) next = button(((RootPaneContainer) tw).getContentPane(), "Take the tour");
            if (next != null) { final JButton nb = next; edt(nb::doClick); sleep(900); }
            final BufferedImage[] whole = {null};
            edt(() -> whole[0] = paint(mv.getRootPane()));
            save(framed(whole[0], mv.getTitle()), "tour-map.png", 1000);
            edt(tw::dispose);
        } catch (Exception e) { real.println("FAILED tour: " + e); }
    }

    static void decision() throws Exception {
        try {
            final BufferedImage[] im = {null};
            final String[] ttl = {null};
            edt(() -> {
                JOptionPane pane = new JOptionPane("You defeated Lin Wei (Lv.14, Evil). Kill or spare?", JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new String[]{"Kill", "Spare"}, "Spare");
                JDialog d = pane.createDialog(mv, "Decision — " + player().name);
                d.pack();
                Ui.prepare(d);
                d.pack();
                im[0] = paint(d.getRootPane());
                ttl[0] = d.getTitle();
                d.dispose();
            });
            save(framed(im[0], ttl[0]), "win-decision.png", 600);
        } catch (Exception e) { real.println("FAILED decision: " + e); }
    }
}
