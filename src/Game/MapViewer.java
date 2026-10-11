package Game;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class MapViewer extends JFrame {

    private final MapPanel mapPanel;
    private final JPanel infoPanel;
    private final JPanel trackerPanel;
    private final JTextArea logArea;
    private final JTextArea historyArea;
    private volatile int selX = -1, selY = -1;

    private volatile int exportTile = 0; // > 0 while the map is being drawn into an export image
    /** The map is square and centred in its panel; these are the pixel offsets of tile (0,0)'s corner. */
    private int offX() { if (exportTile > 0 || mapPanel == null) return 0; return Math.max(0, (mapPanel.getWidth() - vis() * tileSize()) / 2); }
    private int offY() { if (exportTile > 0 || mapPanel == null) return 0; return Math.max(0, (mapPanel.getHeight() - vis() * tileSize()) / 2); }

    // ---- zoom: 1 = the whole world; higher shows fewer, bigger tiles around (viewX, viewY) (the world wraps round)
    volatile int zoom = 1, viewX = 15, viewY = 15;
    volatile boolean followMe = true;           // while zoomed, keep the player's tile in the middle
    private JToggleButton followBtn;
    private JLabel zoomLabel;
    private boolean panDragged = false;
    /** Tiles shown along one side. */
    private int vis() { return (exportTile > 0 || zoom <= 1) ? Map.range : Math.max(5, (Map.range + zoom - 1) / zoom); }
    private int originX() { return (exportTile > 0 || zoom <= 1) ? 0 : Math.floorMod(viewX - vis() / 2, Map.range); }
    private int originY() { return (exportTile > 0 || zoom <= 1) ? 0 : Math.floorMod(viewY - vis() / 2, Map.range); }
    /** Column / row (0 = left / top) of world tile x / y on screen, or -1 when it is out of view. */
    private int colOf(int x) { int c = Math.floorMod(x - originX(), Map.range); return c < vis() ? c : -1; }
    private int rowOf(int y) { int d = Math.floorMod(y - originY(), Map.range); return d < vis() ? vis() - 1 - d : -1; }
    /** World tile under a screen position, or -1 outside the map. */
    private int tileX(int px, int ts) { int c = Math.floorDiv(px - offX(), ts); return c < 0 || c >= vis() ? -1 : Math.floorMod(originX() + c, Map.range); }
    private int tileY(int py, int ts) { int r = Math.floorDiv(py - offY(), ts); return r < 0 || r >= vis() ? -1 : Math.floorMod(originY() + vis() - 1 - r, Map.range); }

    void setZoom(int z, int tx, int ty) {
        z = Math.max(1, Math.min(5, z));
        if (z == zoom) return;
        if (zoom <= 1 && !followMe) { if (tx < 0) { tx = selX; ty = selY; } if (tx >= 0) { viewX = tx; viewY = ty; } }
        else if (!followMe && tx >= 0) { viewX = tx; viewY = ty; }
        zoom = z;
        SwingUtilities.invokeLater(() -> { if (zoomLabel != null) zoomLabel.setText(zoom + "x"); if (mapPanel != null) { mapPanel.revalidate(); mapPanel.repaint(); } });
    }

    private int tileSize() {
        if (exportTile > 0) return exportTile;
        int panelW = mapPanel != null ? mapPanel.getWidth()  : 0;
        int panelH = mapPanel != null ? mapPanel.getHeight() : 0;
        if (panelW > 10 && panelH > 10) {
            return Math.max(4, Math.min(panelW, panelH) / vis());
        }
        return Math.max(8, Math.min(32, 700 / vis()));
    }

    public MapViewer() {
        super("Tales of Dream and Fantasy 2 — World Map");
        try { setIconImages(ArtMore.logoImages()); } catch (RuntimeException ignored) { /* no icon is not worth a crash */ } // the game's emblem in the title bar and the taskbar
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // the window's X asks whether to save and quit
        addWindowListener(new java.awt.event.WindowAdapter() { @Override public void windowClosing(java.awt.event.WindowEvent e) { confirmQuit(); } });
        setBackground(Theme.BG);

        mapPanel = new MapPanel();
        PlaceImages.thumbsReady = () -> mapPanel.repaint();
        JScrollPane mapScroll = new JScrollPane(mapPanel);
        mapScroll.getViewport().setBackground(Theme.BG);
        PlaceImages.ensureFolders();
        JPanel mapWrap = new JPanel(new BorderLayout());
        mapWrap.add(buildLayerBar(), BorderLayout.NORTH);
        mapWrap.add(mapScroll, BorderLayout.CENTER);

        // Info panel (tile details)
        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Theme.PANEL);
        infoPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        { JLabel hint = new JLabel("Click a tile on the map to inspect it."); hint.setForeground(Theme.DIM); infoPanel.add(hint); }

        JScrollPane infoScroll = new JScrollPane(infoPanel);
        infoScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        infoScroll.getViewport().setBackground(Theme.PANEL);
        infoScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.LINE),
                "Tile Info", 0, 0, Theme.bold(16), Theme.ACCENT));

        // NPC tracker panel
        trackerPanel = new JPanel();
        trackerPanel.setLayout(new BoxLayout(trackerPanel, BoxLayout.Y_AXIS));
        trackerPanel.setBackground(Theme.BG);
        trackerPanel.setBorder(new EmptyBorder(4, 8, 4, 8));

        JScrollPane trackerScroll = new JScrollPane(trackerPanel);
        trackerScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        trackerScroll.getViewport().setBackground(Theme.BG);
        trackerScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.LINE),
                "NPC Tracker", 0, 0, Theme.bold(16), Theme.OK));

        // Info + tracker: vertically resizable split
        JSplitPane infoTrackerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, infoScroll, trackerScroll);
        infoTrackerSplit.setResizeWeight(0.6);
        infoTrackerSplit.setContinuousLayout(true);
        infoTrackerSplit.setDividerSize(5);
        infoTrackerSplit.setBackground(Theme.PANEL);

        JComponent dock = buildDock();
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, infoTrackerSplit, dock);
        rightSplit.setResizeWeight(0.4);
        rightSplit.setContinuousLayout(true);
        rightSplit.setDividerSize(7);
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(Theme.PANEL);
        right.setPreferredSize(new Dimension(400, 0));
        right.add(rightSplit, BorderLayout.CENTER);
        // the dividers can only be placed once the window has a size: tile info gets the room (about 40% of the column), the tracker a strip, the action dock the rest
        rightSplit.addComponentListener(new java.awt.event.ComponentAdapter() {
            boolean placed = false;
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                if (placed || rightSplit.getHeight() < 300) return;
                placed = true;
                rightSplit.setDividerLocation(0.40); // the dock (Move / Me / Auto / World) is what the player clicks: it gets the larger share
                infoTrackerSplit.setDividerLocation(0.80);
            }
        });

        // Event log (tabbed: Events + World History)
        logArea = new JTextArea(5, 70);
        logArea.setEditable(false);
        logArea.setBackground(Theme.FIELD);
        logArea.setForeground(new Color(160, 200, 140));
        logArea.setFont(Theme.mono(14));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(null);

        historyArea = new JTextArea(5, 70);
        historyArea.setEditable(false);
        historyArea.setBackground(new Color(12, 18, 12));
        historyArea.setForeground(new Color(200, 220, 160));
        historyArea.setFont(Theme.mono(14));
        historyArea.setLineWrap(true);
        historyArea.setWrapStyleWord(true);
        JScrollPane historyScroll = new JScrollPane(historyArea);
        historyScroll.setBorder(null);

        JTabbedPane logTabs = new JTabbedPane(JTabbedPane.BOTTOM);
        logTabs.setBackground(Theme.BG);
        logTabs.setForeground(Theme.TEXT);
        logTabs.setFont(Theme.plain(16));
        logTabs.addTab("Events", buildLogPanel(logScroll));
        logTabs.addTab("World History", historyScroll);

        // Speed controls panel (top)
        JPanel speedPanel = buildSpeedPanel();

        // Horizontal split: map | right panel
        JSplitPane horizSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mapWrap, right);
        horizSplit.setResizeWeight(1.0);
        horizSplit.setContinuousLayout(true);
        horizSplit.setDividerSize(5);

        // Vertical split: main content | event log
        JSplitPane vertSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, horizSplit, logTabs);
        vertSplit.setResizeWeight(0.78);
        vertSplit.setContinuousLayout(true);
        vertSplit.setDividerSize(6);

        setLayout(new BorderLayout(4, 4));
        JPanel topBox = new JPanel();
        topBox.setLayout(new BoxLayout(topBox, BoxLayout.Y_AXIS));
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(9, 12, 15));
        header.add(Theme.banner(), BorderLayout.WEST);
        header.add(speedPanel, BorderLayout.CENTER);
        topBox.add(header);
        topBox.add(buildStatusBar());
        add(topBox, BorderLayout.NORTH);
        add(vertSplit, BorderLayout.CENTER);

        mapPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (panDragged) { panDragged = false; return; }
                int ts = tileSize();
                int tx = tileX(e.getX(), ts);
                int ty = tileY(e.getY(), ts); // the map is drawn with north (+y) at the top
                if (tx >= 0 && tx < Map.range && ty >= 0 && ty < Map.range) {
                    selX = tx;
                    selY = ty;
                    SwingUtilities.invokeLater(() -> {
                        mapPanel.repaint();
                        World.readWait(250, () -> updateInfo(Map.tiles[tx][ty]));
                    });
                    // a click only selects the tile and shows its information; the "Move Here" button in that panel walks there
                }
            }
        });

        MouseAdapter panAndZoom = new MouseAdapter() {
            private int lastX, lastY;
            @Override public void mousePressed(MouseEvent e) { lastX = e.getX(); lastY = e.getY(); panDragged = false; }
            @Override public void mouseDragged(MouseEvent e) {
                if (zoom <= 1 || !SwingUtilities.isLeftMouseButton(e)) return;
                int ts = tileSize(), dx = (e.getX() - lastX) / ts, dy = (e.getY() - lastY) / ts;
                if (dx == 0 && dy == 0) return;
                viewX = Math.floorMod(viewX - dx, Map.range); viewY = Math.floorMod(viewY + dy, Map.range); // the map follows the hand; north is up
                lastX += dx * ts; lastY += dy * ts; panDragged = true;
                followMe = false; if (followBtn != null) followBtn.setSelected(false);
                mapPanel.repaint();
            }
            @Override public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
                int ts = tileSize();
                setZoom(zoom + (e.getWheelRotation() < 0 ? 1 : -1), tileX(e.getX(), ts), tileY(e.getY(), ts));
            }
        };
        mapPanel.addMouseListener(panAndZoom); mapPanel.addMouseMotionListener(panAndZoom); mapPanel.addMouseWheelListener(panAndZoom);

        mapPanel.addMouseListener(new MouseAdapter() {
            private void popup(MouseEvent e) {
                if (!e.isPopupTrigger()) return;
                int ts = tileSize();
                int tx = tileX(e.getX(), ts), ty = tileY(e.getY(), ts);
                if (tx >= 0 && tx < Map.range && ty >= 0 && ty < Map.range) showTileMenu(tx, ty, e.getComponent(), e.getX(), e.getY());
            }
            @Override public void mousePressed(MouseEvent e) { popup(e); }
            @Override public void mouseReleased(MouseEvent e) { popup(e); }
        });

        // Size to 85% of screen, let layouts breathe
        java.awt.Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int)(screen.width * 0.85), (int)(screen.height * 0.85));
        setLocationRelativeTo(null);
        GuiHub.restoreBounds(this, "main");
        GuiHub.trackBounds(this, "main");
        setVisible(true);
        installShortcuts();
        Ui.applyTooltipPreference();
        Ui.scale(this, Settings.fontScale);
        Ui.mainWindowScaled();
        Notifications.listener = n -> SwingUtilities.invokeLater(() -> GuiExtras.toast(this, n));
        SwingUtilities.invokeLater(() -> GuiExtras.welcome(this));
    }

    /** Right-click on a map tile: select it, go there, route, note it, add a waypoint quest, open its details, meet the people standing there. */
    private void showTileMenu(int tx, int ty, Component where, int mx, int my) {
        selectTile(tx, ty);
        final Area a = Map.tiles[tx][ty];
        JPopupMenu m = new JPopupMenu();
        JMenuItem head = new JMenuItem((a == null ? "Unexplored tile" : a.name) + "  [" + tx * 100 + ", " + ty * 100 + "]");
        head.setEnabled(false);
        m.add(head);
        m.addSeparator();
        JMenuItem go = new JMenuItem("Go here");
        go.setToolTipText("Walk to this tile, one tile a turn");
        go.addActionListener(e -> queueInput("goto " + tx + " " + ty));
        m.add(go);
        JMenuItem route = new JMenuItem("Route preview");
        route.addActionListener(e -> queueInput("route " + tx + " " + ty));
        m.add(route);
        if (a != null) {
            JMenuItem wp = new JMenuItem("Add waypoint quest here");
            wp.setToolTipText("Pin a quest that leads to this tile");
            wp.addActionListener(e -> queueInput("qnew " + a.name.replace(' ', '_') + " " + tx + " " + ty + " 70"));
            m.add(wp);
            JMenuItem big = new JMenuItem("Large picture");
            big.addActionListener(e -> showLargeView(a));
            m.add(big);
            if (a.city || a.sect != null) {
                JMenuItem det = new JMenuItem((a.city ? "City" : "Sect") + " details…");
                det.addActionListener(e -> GuiMore.placeWindow(this, a));
                m.add(det);
            }
            final List<Character> top = new ArrayList<>();
            try {
                for (Character c : a.characters) if (c != null && !c.dead && c.trait != Character.Traits.Beast && c.born && c.player != 1) top.add(c);
                top.sort((x, y) -> Integer.compare(y.lvl, x.lvl));
            } catch (RuntimeException ignored) { }
            if (!top.isEmpty()) {
                m.addSeparator();
                for (int i = 0; i < Math.min(5, top.size()); i++) {
                    final Character c = top.get(i);
                    JMenu who = new JMenu(c.name + " " + c.lastName + "  Lv." + c.lvl);
                    JMenuItem open = new JMenuItem("Open");
                    open.addActionListener(e -> showCharacterDialog(c));
                    JMenuItem chase = new JMenuItem("Chase");
                    chase.addActionListener(e -> queueInput("chase #" + c.id));
                    JMenuItem seek = new JMenuItem("Pin a quest to befriend");
                    seek.addActionListener(e -> queueInput("seek " + c.id + " friend"));
                    JMenuItem seekTalk = new JMenuItem("Pin a quest to walk there and talk");
                    seekTalk.addActionListener(e -> queueInput("seek " + c.id + " talk"));
                    JMenuItem seekCourt = new JMenuItem("Pin a quest to walk there and court");
                    seekCourt.addActionListener(e -> queueInput("seek " + c.id + " court"));
                    who.add(open); who.add(chase); who.add(seekTalk); who.add(seekCourt); who.add(seek);
                    m.add(who);
                }
            }
        }
        m.show(where, mx, my);
    }

    /** Settings changed (window, `settings` command): re-apply everything that is not read live. */
    void applySettings() {
        Ambient.apply();
        Settings.applyArt();
        Ui.applyTooltipPreference();
        mapPanel.repaint();
        if (selX >= 0 && selY >= 0 && Map.tiles[selX][selY] != null) World.readWait(250, () -> updateInfo(Map.tiles[selX][selY]));
    }

    Character findPlayer() {
        // Index loop: the game thread appends to Map.characters while this runs on the EDT (an iterator throws ConcurrentModificationException;
        // the list only ever grows, so indexing up to the size read here is safe)
        int n = Map.characters.size();
        for (int i = 0; i < n; i++) {
            Character c = Map.characters.get(i);
            if (c != null && c.player == 1 && !c.dead) return c;
        }
        return null;
    }

    void queueInput(String s) {
        if (GameDriver.guiEnabled) {
            System.out.println("[GUI] queued: " + s);
            GameDriver.guiInputQueue.add(s);
        }
    }

    JButton makeBtn(String label, Runnable action) {
        JButton btn = new JButton(label);
        btn.setFont(Theme.bold(15));
        Theme.fixSymbols(btn, label);
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(3, 10, 3, 10));
        btn.addActionListener(e -> action.run());
        Ui.tooltip(btn);
        return btn;
    }

    /** A button with its own tooltip (shown only while Settings.tooltips is on, see Ui.applyTooltipPreference). */
    JButton makeBtn(String label, String tip, Runnable action) {
        JButton btn = makeBtn(label, action);
        if (tip != null && !tip.isEmpty()) btn.setToolTipText(tip);
        return btn;
    }

    /** The tile selected on the map as {x, y} (tile numbers), or null before the first selection. */
    int[] selectedTile() { return selX >= 0 && selY >= 0 ? new int[]{selX, selY} : null; }

    /** Selects a tile as a click on the map would: highlights it and shows its Tile Info. */
    void selectTile(int tx, int ty) {
        if (tx < 0 || ty < 0 || tx >= Map.range || ty >= Map.range) return;
        selX = tx; selY = ty;
        SwingUtilities.invokeLater(() -> {
            mapPanel.repaint();
            World.readWait(250, () -> updateInfo(Map.tiles[tx][ty]));
        });
    }

    void openScriptEditor() { showScriptEditor(); }

    /** Opens the Browse window, on a tab by its title ("Cultivators", "Monsters", "Shop items", "Backpack", "Places", "Search anyone"). */
    void openBrowser(String tab) {
        showBrowser();
        if (browserDialog != null && tab != null) GuiHub.selectTab(browserDialog.getContentPane(), tab);
    }

    private JPanel buildSpeedPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 6));
        p.setBackground(new Color(9, 12, 15));

        JLabel lbl = new JLabel("Speed");
        lbl.setForeground(Theme.DIM);
        lbl.setFont(Theme.bold(15));
        p.add(lbl);

        JButton pauseBtn = makeBtn("⏸ Pause", () -> { GameDriver.paused = !GameDriver.paused; });
        pauseBtn.setForeground(Theme.GOLD);
        pauseBtn.setToolTipText("Pause or resume the world (P). While paused nothing moves, but every window still works.");
        p.add(pauseBtn);

        final int[] delays = {0, 100, 500, 2000, 10000};
        final String[] names = {"0ms", "0.1s", "0.5s", "2s", "10s"};
        final JButton[] speeds = new JButton[delays.length];
        for (int i = 0; i < delays.length; i++) {
            final int d = delays[i];
            speeds[i] = makeBtn(names[i], () -> { GameDriver.paused = false; GameDriver.tickDelayMs = d; });
            speeds[i].setToolTipText(d == 0 ? "As fast as the computer can go: no pause between turns." : "Wait " + names[i] + " between turns (one turn = one game week).");
            p.add(speeds[i]);
        }

        JLabel statusLbl = new JLabel("  ");
        statusLbl.setFont(Theme.symbol(14));
        p.add(statusLbl);

        // the state text and the highlighted speed follow the game
        new javax.swing.Timer(300, e2 -> {
            if (GameDriver.paused) {
                statusLbl.setForeground(Theme.GOLD);
                statusLbl.setText("  ⏸ PAUSED");
            } else if (GameDriver.waitingForInput) {
                statusLbl.setForeground(Theme.ACCENT);
                statusLbl.setText("  ◆ Waiting for input — press a button");
            } else {
                statusLbl.setForeground(Theme.OK);
                statusLbl.setText("  ▶ Simulation running...");
            }
            pauseBtn.putClientProperty("FlatLaf.style", GameDriver.paused ? "background:#3a3120;borderColor:#d0ac5c" : null);
            for (int i = 0; i < delays.length; i++)
                speeds[i].putClientProperty("FlatLaf.style", !GameDriver.paused && GameDriver.tickDelayMs == delays[i] ? "background:#265a52;borderColor:#68b8a0" : null);
        }).start();

        return p;
    }

    /** A titled group of buttons for a dock page. */
    private JPanel group(String title, int cols, Component... cs) {
        JPanel p = grid(cols, cs);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Theme.LINE), title, 0, 0, Theme.bold(13), Theme.ACCENT),
                new EmptyBorder(2, 6, 6, 6)));
        return p;
    }

    private JPanel grid(int cols, Component... cs) {
        JPanel p = new JPanel(new GridLayout(0, cols, 6, 6));
        p.setBackground(Theme.PANEL);
        for (Component c : cs) p.add(c);
        return p;
    }

    private JPanel page(Component content) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.PANEL);
        p.setBorder(new EmptyBorder(10, 10, 8, 10));
        p.add(content, BorderLayout.NORTH);
        return p;
    }

    /** The Quit button / window close: save and quit, quit without saving, or carry on. */
    void confirmQuit() {
        int a = JOptionPane.showOptionDialog(this, "Leave the game?", "Quit", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                new String[]{"Save and quit", "Quit without saving", "Keep playing"}, "Save and quit");
        if (a != 0 && a != 1) return;
        GuiHub.noteBounds(this, "main"); try { Settings.save(); } catch (RuntimeException ignored) { } // the window comes back where it was
        GameDriver.quitSaves = a == 0; GameDriver.quitRequested = true; GameDriver.paused = false;
        if (a == 0) GameDriver.saveRequested = true;
        queueInput("x"); // wakes a game that is waiting for your command: the turn ends, the save happens, the program exits
        if (a == 1) { try { Settings.save(); } catch (RuntimeException ignored) { } System.exit(0); }
    }

    private JComponent buildDock() { return GuiDock.build(this); } // the Move / Me / Auto / World tabs live in GuiDock

    // ------------------------------------------------------------------ map layers, status bar, log filters, banner, shortcuts
    // Map layers (all toggleable): colour mode + overlays. Read by MapPanel on every repaint.
    volatile int colorMode = 0;               // 0 terrain, 1 beast density, 2 population, 3 level
    volatile boolean layerThumbs = false;     // tile pictures inside the map cells
    volatile boolean layerCrowd = true;       // red / amber outline on over-crowded tiles
    volatile boolean layerFog = false;        // dim what the player has never visited
    volatile boolean layerQuests = false;     // pins on quest targets
    volatile boolean layerPath = false;       // line from the player to the selected tile
    volatile boolean showPictures = true;     // place picture in the Tile Info panel

    private JPanel buildLayerBar() {
        Color bg = Theme.PANEL, fg = Theme.TEXT;
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        p.setBackground(bg);
        JLabel l = new JLabel("Map:");
        l.setForeground(fg); l.setFont(Theme.bold(14));
        p.add(l);
        JComboBox<String> mode = new JComboBox<>(new String[]{"Terrain", "Beasts", "Population", "Level"});
        mode.setFont(Theme.plain(13));
        mode.setToolTipText("What the tile colours show");
        mode.addActionListener(e -> { colorMode = mode.getSelectedIndex(); mapPanel.repaint(); });
        p.add(mode);
        String[][] toggles = {{"Pictures", "pictures", "Show the place picture in Tile Info (needs art in images/)"},
                {"Tile pics", "thumbs", "Draw small pictures inside map tiles (best when the map is zoomed)"},
                {"Crowd", "crowd", "Outline over-crowded tiles: red = hotspot, amber = above 80% of capacity"},
                {"Fog", "fog", "Dim tiles the player has never visited"},
                {"Quests", "quests", "Pins on the places your quests point to (orange alert, cyan duty, grey routine)"},
                {"Path", "path", "Line from the player to the selected tile"}};
        for (String[] t : toggles) {
            JToggleButton b = new JToggleButton(t[0]);
            b.setFont(Theme.bold(12));
            b.setToolTipText(t[2]);
            b.setFocusable(false);
            Theme.chip(b);
            boolean on = t[1].equals("pictures") ? showPictures : t[1].equals("crowd") ? layerCrowd : false;
            b.setSelected(on);
            b.addActionListener(e -> {
                boolean v = b.isSelected();
                switch (t[1]) {
                    case "pictures": showPictures = v; if (selX >= 0 && Map.tiles[selX][selY] != null) World.readWait(250, () -> updateInfo(Map.tiles[selX][selY])); break;
                    case "thumbs": layerThumbs = v; break;
                    case "crowd": layerCrowd = v; break;
                    case "fog": layerFog = v; break;
                    case "quests": layerQuests = v; break;
                    default: layerPath = v; break;
                }
                mapPanel.repaint();
            });
            p.add(b);
        }
        JLabel zl = new JLabel("Zoom:"); zl.setForeground(fg); zl.setFont(Theme.bold(12)); p.add(zl);
        JButton zout = makeBtn("−", () -> setZoom(zoom - 1, -1, -1)); zout.setToolTipText("Show more of the world (mouse wheel down)");
        JButton zin = makeBtn("+", () -> setZoom(zoom + 1, -1, -1)); zin.setToolTipText("Bigger tiles, bigger pictures (mouse wheel up). Drag the map to move around.");
        zoomLabel = new JLabel(zoom + "x"); zoomLabel.setForeground(fg); zoomLabel.setFont(Theme.bold(12));
        followBtn = new JToggleButton("Follow me", followMe); followBtn.setFont(Theme.bold(12)); followBtn.setFocusable(false); Theme.chip(followBtn);
        followBtn.setToolTipText("While zoomed in, keep your character in the middle of the map (dragging the map turns this off)");
        followBtn.addActionListener(e -> { followMe = followBtn.isSelected(); mapPanel.repaint(); });
        p.add(zout); p.add(zoomLabel); p.add(zin); p.add(followBtn);
        JButton legendBtn = makeBtn("Legend ▾", () -> { });
        legendBtn.setFont(Theme.bold(12)); Theme.fixSymbols(legendBtn, legendBtn.getText());
        legendBtn.addActionListener(e -> { JPopupMenu pm = new JPopupMenu(); pm.add(buildLegend()); pm.show(legendBtn, 0, legendBtn.getHeight()); });
        p.add(legendBtn);
        return p;
    }

    // --- status bar -------------------------------------------------------------------------------------------------------------
    private JLabel sbName, sbRealm, sbAge, sbWealth, sbMode, sbQuest, sbPlace;
    private JProgressBar sbCult;
    private JButton sbAlerts, sbBell, sbMusic;
    private JLabel sbSave;

    private JPanel buildStatusBar() {
        Color bg = Theme.PANEL2, fg = new Color(215, 205, 175);
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        p.setBackground(bg);
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.LINE));
        Font f = Theme.bold(14);
        sbName = new JLabel("(no player)"); sbName.setForeground(Theme.GOLD);
        sbRealm = new JLabel(" "); sbAge = new JLabel(" "); sbWealth = new JLabel(" "); sbMode = new JLabel(" "); sbQuest = new JLabel(" "); sbPlace = new JLabel(" ");
        for (JLabel l : new JLabel[]{sbName, sbRealm, sbAge, sbWealth, sbMode, sbPlace, sbQuest}) { l.setFont(f); if (l != sbName) l.setForeground(fg); }
        sbCult = new JProgressBar(0, 1000);
        sbCult.setPreferredSize(new Dimension(170, 16));
        sbCult.setStringPainted(true);
        sbCult.setFont(Theme.plain(11));
        sbCult.setToolTipText("Cultivation progress toward the next level");
        sbName.setFont(Theme.serif(17));
        sbName.setToolTipText("Your cultivator — click for the character sheet");
        sbName.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sbName.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { GuiSheets.statsWindow(MapViewer.this); } });
        sbRealm.setToolTipText("Major realm . minor stage, level and your craft (trait)");
        sbAge.setToolTipText("Your age / the age at which your lifespan runs out");
        sbWealth.setToolTipText("Your wealth in spirit stones (ss)");
        sbMode.setToolTipText("Who is in charge — click to open the Auto tab. MANUAL: you; AUTO: the character; HYBRID: your script. 'duties' shows which chores it does itself.");
        sbMode.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sbMode.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { GuiDock.showTab("Auto"); } });
        sbPlace.setToolTipText("Where you stand — click for the tile's card");
        sbPlace.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sbPlace.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { GuiCards.hereWindow(MapViewer.this); } });
        sbQuest.setToolTipText("Your current quest — click to open the Quest Log");
        sbQuest.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sbQuest.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { GuiQuests.open(MapViewer.this, null); } });
        sbAlerts = makeBtn("⚑ 0", () -> GuiQuests.open(this, sbAlerts.getText().trim().endsWith(" 0") ? null : "Alerts"));
        sbAlerts.setToolTipText("Quest alerts waiting — click to open the Quest Log on them (Q opens it)");
        sbBell = makeBtn("🔔 0", () -> GuiExtras.notificationsWindow(this));
        sbBell.setToolTipText("Notifications you have not read yet — click for the history");
        sbMusic = makeBtn("♪ off", () -> { Settings.music = !Settings.music; Settings.save(); Ambient.apply(); });
        sbMusic.setToolTipText("The game's music (volume and the next song in Settings, or: music list)");
        JButton sbGo = makeBtn("▶ Go", () -> queueInput("goquest"));
        sbGo.setToolTipText("Walk to the place of your top quest (or chase the person it names)");
        JButton sbQuit = makeBtn("⏻ Quit", this::confirmQuit);
        sbQuit.setToolTipText("Save and quit (or quit without saving)");
        sbSave = new JLabel(" ");
        sbSave.setFont(Theme.symbol(13));
        sbSave.setToolTipText("Autosave / save status. Saves happen between turns and keep a backup of the previous copy.");
        JButton sbHelp = GuiHelp.helpButton(this, "start"); sbHelp.setToolTipText("Help (F1): every button and feature explained");
        p.add(sbName); p.add(sbRealm); p.add(sbCult); p.add(sbAge); p.add(sbWealth); p.add(sbMode); p.add(sbAlerts); p.add(sbBell); p.add(sbMusic); p.add(sbPlace); p.add(sbQuest); p.add(sbGo); p.add(sbSave); p.add(sbHelp); p.add(sbQuit);
        new javax.swing.Timer(500, e -> updateSaveIndicator()).start();
        return p;
    }

    private void updateSaveIndicator() {
        String st = SaveManager.state;
        long age = SaveManager.lastAt == 0 ? -1 : (System.currentTimeMillis() - SaveManager.lastAt) / 1000;
        int unread = Notifications.unread();
        sbMusic.setText(Settings.music ? (Ambient.status.equals("no audio device") ? "♪ no sound" : "♪ on") : "♪ off");
        sbMusic.setForeground(Settings.music ? Theme.ACCENT : Theme.DIM);
        sbMusic.setToolTipText(Settings.music && !Ambient.nowPlaying.isEmpty() ? "Now playing: \"" + Ambient.nowPlaying + "\" (" + Ambient.engine + ") - click to switch off" : "The game's music (volume, sound source and the next song in Settings)");
        sbBell.setText("🔔 " + unread);
        sbBell.setForeground(unread > 0 ? Theme.GOLD : Theme.TEXT);
        if (st.equals("saving") || st.equals("loading")) { sbSave.setForeground(new Color(255, 190, 70)); sbSave.setText("💾 " + st + "..."); }
        else if (st.equals("error")) { sbSave.setForeground(new Color(255, 100, 90)); sbSave.setText("💾 save FAILED"); sbSave.setToolTipText(SaveManager.lastError); }
        else if (age >= 0) {
            sbSave.setForeground(age < 8 ? new Color(120, 230, 130) : new Color(130, 150, 130));
            sbSave.setText("💾 " + SaveManager.lastName + " · yr " + SaveManager.lastYear + " · " + (age < 60 ? age + "s" : age < 3600 ? age / 60 + "m" : age / 3600 + "h") + " ago");
        } else { sbSave.setForeground(new Color(130, 130, 130)); sbSave.setText("💾 not saved yet"); }
    }

    private void updateStatusBar() {
        Character p = findPlayer();
        GuiDock.refresh(p); // the dock's buttons and the Auto tab follow the character
        if (p == null) { sbName.setText("(no player — spectating)"); return; }
        try {
            sbName.setText(p.name + " " + p.lastName);
            sbRealm.setText("Major " + p.majorLvl + "." + p.minorLvl + " Lv" + p.lvl + (p.trait != null ? " " + p.getTrait() : ""));
            double max = Math.max(1, p.cultivationBaseMax);
            sbCult.setValue((int) Math.min(1000, 1000 * p.cultivationBase / max));
            sbCult.setString("cult " + (int) Math.min(100, 100 * p.cultivationBase / max) + "%   pot " + p.potentialLabel());
            sbAge.setText("age " + p.age + "/" + p.maxAge);
            sbWealth.setText(PlayerLists.wealth(p) + " ss");
            PlayerControl pc = p.pc;
            sbMode.setText(pc == null ? "" : pc.mode + " · duties " + pc.dutySummary());
            sbAlerts.setText("⚑ " + (pc == null ? 0 : pc.inbox.size()));
            sbAlerts.setForeground(pc != null && !pc.inbox.isEmpty() ? new Color(255, 170, 60) : Theme.TEXT);
            sbPlace.setText(p.location == null ? "" : p.location.name + " " + PlayerControl.coord(p.location));
            Ambient.followWorld(p);
            Quest cq = p.currentQuest;
            sbQuest.setText(cq == null ? "" : "▶ " + cq.displayName());
        } catch (RuntimeException ignored) { /* the game thread changed something mid-read: the next tick refreshes */ }
    }

    // --- event log filters + clickable names ------------------------------------------------------------------------------------
    private JCheckBox[] logChecks;
    private JTextField logSearch;
    private static final String[] LOG_CATS = {"Fights", "Economy", "World", "Me", "Other"};

    private JPanel buildLogPanel(JScrollPane scroll) {
        Color bg = Theme.BG, fg = Theme.TEXT;
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bg);
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 1));
        bar.setBackground(bg);
        logChecks = new JCheckBox[LOG_CATS.length];
        for (int i = 0; i < LOG_CATS.length; i++) {
            logChecks[i] = new JCheckBox(LOG_CATS[i], true);
            logChecks[i].setBackground(bg); logChecks[i].setForeground(fg);
            logChecks[i].setFont(Theme.plain(13));
            logChecks[i].addActionListener(e -> refreshLog());
            bar.add(logChecks[i]);
        }
        logSearch = new JTextField(14);
        logSearch.setFont(Theme.plain(13));
        logSearch.setToolTipText("Only lines containing this text");
        logSearch.addActionListener(e -> refreshLog());
        JLabel l = new JLabel("search:"); l.setForeground(fg);
        bar.add(l); bar.add(logSearch);
        JLabel hint = new JLabel("(click a line to open the first cultivator named in it)");
        hint.setForeground(new Color(120, 120, 100)); hint.setFont(Theme.plain(12));
        bar.add(hint);
        panel.add(bar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        logArea.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { openCultivatorFromLogClick(e); }
        });
        logArea.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) { logArea.setToolTipText(hoverCard(e)); }
        });
        ToolTipManager.sharedInstance().registerComponent(logArea);
        return panel;
    }

    static String logCategory(String line, String myName) {
        if (line.startsWith("[Notice]")) return "Me";
        String l = line.toLowerCase();
        if (myName != null && !myName.isEmpty() && line.contains(myName)) return "Me";
        if (l.contains("won the fight") || l.contains("spared") || l.contains("defeated") || l.contains("challenges you") || l.contains("has died")
                || l.contains("war") || l.contains("raid") || l.contains("ambush") || l.contains("killed") || l.contains("slain")) return "Fights";
        if (l.contains("auction") || l.contains("sold ") || l.contains("bought") || l.contains("tax") || l.contains("shop") || l.contains("treasury")
                || l.contains("installed") || l.contains("enticed") || l.contains(" ss")) return "Economy";
        if (line.startsWith("[") || line.startsWith("===") || l.contains("beast tide") || l.contains("celestial") || l.contains("heaven") || l.contains("plague")
                || l.contains("storm") || l.contains("overcrowding") || l.contains("it's year") || l.contains("succession") || l.contains("epoch")) return "World";
        return "Other";
    }

    private boolean logLineVisible(String line) {
        if (logChecks == null) return true;
        Character p = findPlayer();
        String me = p == null ? null : p.name;
        String cat = logCategory(line, me);
        for (int i = 0; i < LOG_CATS.length; i++) if (LOG_CATS[i].equals(cat) && !logChecks[i].isSelected()) return false;
        String q = logSearch == null ? "" : logSearch.getText().trim().toLowerCase();
        return q.isEmpty() || line.toLowerCase().contains(q);
    }

    private java.util.Map<String, Integer> nameIndex = null;
    private long nameIndexAt = 0;

    /** The cultivator named under the mouse in the log (first two-word match on the line), or null. */
    private Character cultivatorAt(MouseEvent e) {
        try {
            int off = logArea.viewToModel(e.getPoint());
            int line = logArea.getLineOfOffset(off);
            String text = logArea.getText(logArea.getLineStartOffset(line), logArea.getLineEndOffset(line) - logArea.getLineStartOffset(line));
            if (nameIndex == null || System.currentTimeMillis() - nameIndexAt > 15000) {
                java.util.Map<String, Integer> m = new java.util.HashMap<>();
                int n = Map.characters.size();
                for (int i = 0; i < n; i++) {
                    Character c = Map.characters.get(i);
                    if (c != null && !c.dead && c.trait != Character.Traits.Beast) m.putIfAbsent((c.name + " " + c.lastName).toLowerCase(), i);
                }
                nameIndex = m; nameIndexAt = System.currentTimeMillis();
            }
            String[] w = text.replaceAll("[^A-Za-z' ]", " ").trim().split("\\s+");
            for (int i = 0; i + 1 < w.length; i++) {
                Integer id = nameIndex.get((w[i] + " " + w[i + 1]).toLowerCase());
                if (id != null) { Character c = Character.charById(id); if (c != null) return c; }
            }
        } catch (Exception ignored) { }
        return null;
    }

    /** A small card for the cultivator under the mouse: realm, age, sect, place (what is public knowledge; potential only when it is yours). */
    private String hoverCard(MouseEvent e) {
        final String[] out = {null};
        World.read(25, () -> {
            Character c = cultivatorAt(e);
            if (c == null || c.dead) return;
            Character me = findPlayer();
            out[0] = "<html><b>" + c.name + " " + c.lastName + "</b>" + (c.repTitle != null ? " '" + c.repTitle + "'" : "") + "<br>Lv " + c.lvl + " (major " + c.majorLvl + "), age " + c.age
                    + "<br>" + c.affiliation + (c.sect != null ? ", " + c.sect.name : "") + "<br>at " + (c.location == null ? "?" : c.location.name)
                    + (me != null && me != c && c.location != null && me.location != null ? " — " + PlayerControl.distance(me.location, c.location) + " tiles away" : "")
                    + "<br><i>click to open</i></html>";
        });
        return out[0];
    }

    private void openCultivatorFromLogClick(MouseEvent e) {
        try {
            int off = logArea.viewToModel(e.getPoint());
            int line = logArea.getLineOfOffset(off);
            String text = logArea.getText(logArea.getLineStartOffset(line), logArea.getLineEndOffset(line) - logArea.getLineStartOffset(line));
            if (nameIndex == null || System.currentTimeMillis() - nameIndexAt > 15000) {
                java.util.Map<String, Integer> m = new java.util.HashMap<>();
                int n = Map.characters.size();
                for (int i = 0; i < n; i++) {
                    Character c = Map.characters.get(i);
                    if (c != null && !c.dead && c.trait != Character.Traits.Beast) m.putIfAbsent((c.name + " " + c.lastName).toLowerCase(), i);
                }
                nameIndex = m; nameIndexAt = System.currentTimeMillis();
            }
            String[] w = text.replaceAll("[^A-Za-z' ]", " ").trim().split("\\s+");
            for (int i = 0; i + 1 < w.length; i++) {
                Integer id = nameIndex.get((w[i] + " " + w[i + 1]).toLowerCase());
                if (id != null) { Character c = Character.charById(id); if (c != null) { showCharacterDialog(c); return; } }
            }
        } catch (Exception ignored) { }
    }

    // --- place picture (Tile Info banner + large view) ---------------------------------------------------------------------------
    private void addBanner(Area area) {
        BufferedImage img = PlaceImages.render(area, 320, 180, true);
        if (img == null) return;
        JLabel pic = new JLabel(new ImageIcon(img));
        pic.setAlignmentX(Component.LEFT_ALIGNMENT);
        pic.setBorder(BorderFactory.createLineBorder(Theme.LINE));
        pic.setToolTipText("Click for a larger view");
        pic.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pic.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { showLargeView(area); } });
        infoPanel.add(pic);
        infoPanel.add(Box.createVerticalStrut(4));
    }

    void showLargeView(Area area) {
        BufferedImage img = PlaceImages.render(area, 768, 432, true);
        JDialog d = new JDialog(this, area.name, false);
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        d.getContentPane().setBackground(Theme.BG);
        d.setLayout(new BorderLayout());
        d.add(img != null ? new JLabel(new ImageIcon(img)) : new JLabel("  No picture for this place yet (see images/places/README.txt)  "), BorderLayout.CENTER);
        JLabel cap = new JLabel(" " + area.name + "  " + PlayerControl.coord(area) + "  Lv." + area.lvl + (area.city ? "  City Lv." + area.cityLevel() : "") + (area.sect != null ? "  Sect: " + area.sect.name : "")
                + (area.crowdHotspot ? "  ⚠ overcrowded" : "") + "  " + new String[]{"Spring", "Summer", "Autumn", "Winter"}[GameDriver.currentSeason]);
        cap.setForeground(Theme.TEXT); cap.setFont(Theme.bold(15));
        JPanel capRow = new JPanel(new BorderLayout());
        capRow.setBackground(Theme.PANEL);
        capRow.add(cap, BorderLayout.CENTER);
        JPanel hb = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4)); hb.setBackground(Theme.PANEL); hb.add(GuiHub.helpButton(this, "map"));
        capRow.add(hb, BorderLayout.EAST);
        d.add(capRow, BorderLayout.SOUTH);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    // --- exports -----------------------------------------------------------------------------------------------------------
    void showExportMenu(Component anchor) {
        JPopupMenu menu = new JPopupMenu();
        int cap = Math.max(2, 8000 / Math.max(1, Map.range));
        int[] sizes = {Math.min(6, cap), Math.min(12, cap), Math.min(24, cap)};
        String[] names = {"small", "medium", "large"};
        for (int i = 0; i < 3; i++) {
            final int ts = sizes[i];
            JMenuItem it = new JMenuItem("Map image (" + names[i] + ", " + ts * Map.range + " px)");
            it.addActionListener(e -> exportMap(ts));
            menu.add(it);
        }
        JMenuItem shot = new JMenuItem("Window screenshot");
        shot.addActionListener(e -> {
            try { notice("Saved " + Exporter.png("screenshot", GuiExtras.snapshot(getRootPane())).getPath()); } catch (Exception ex) { notice("Export failed: " + ex.getMessage()); }
        });
        menu.add(shot);
        menu.addSeparator();
        JMenuItem log = new JMenuItem("Event log (text)");
        log.addActionListener(e -> {
            try {
                java.io.File f = new java.io.File(Exporter.dir(), "log-" + Exporter.stamp() + ".txt");
                List<String> snap; synchronized (GameDriver.eventLog) { snap = new ArrayList<>(GameDriver.eventLog); }
                java.nio.file.Files.write(f.toPath(), snap, java.nio.charset.StandardCharsets.UTF_8);
                notice("Saved " + f.getPath());
            } catch (Exception ex) { notice("Export failed: " + ex.getMessage()); }
        });
        menu.add(log);
        JMenuItem hist = new JMenuItem("Yearly history (CSV)");
        hist.addActionListener(e -> { try { notice("Saved " + Exporter.history().getPath()); } catch (Exception ex) { notice("Export failed: " + ex.getMessage()); } });
        menu.add(hist);
        JMenuItem rank = new JMenuItem("Rankings (CSV)");
        rank.addActionListener(e -> World.readWait(600, () -> { try { notice("Saved " + Exporter.rankings().getPath()); } catch (Exception ex) { notice("Export failed: " + ex.getMessage()); } }));
        menu.add(rank);
        menu.show(anchor, 0, anchor.getHeight());
    }

    private void notice(String msg) {
        System.out.println("[Export] " + msg);
        Notifications.post("recap", msg, -1, -1);
    }

    /** Draws the whole map (with the layers currently switched on) into a PNG. */
    void exportMap(int ts) {
        int n = Map.range;
        BufferedImage img = new BufferedImage(n * ts, n * ts, BufferedImage.TYPE_INT_RGB);
        boolean ok = World.readWait(1500, () -> {
            Graphics2D g = img.createGraphics();
            try { mapPanel.paintExport(g, ts, n * ts); } finally { g.dispose(); }
        });
        if (!ok) { notice("The world is busy: try the export again in a moment."); return; }
        try { notice("Saved " + Exporter.png("map", img).getPath()); } catch (Exception e) { notice("Export failed: " + e.getMessage()); }
    }

    // --- keyboard shortcuts -----------------------------------------------------------------------------------------------------
    static final String KEYS_HELP =
            "W / ↑  north     S / ↓  south     A / ←  west     D / →  east     Space  wait a turn\n"
          + "I  stats    L  places    R  relationships    H  help    P  pause\n"
          + "Q  quest panel    U  duties & policies    B  browse lists    M  manage (shops/city/sect)\n"
          + "K  market (auction & library)    G  rankings & charts    T  scripts    O  toggle map pictures\n"
          + "F  family & relationships    N  notifications    Ctrl+S  save\n"
          + "V  backpack & equipment    C  chronicle    J  biography charts    X  world overview    Y  notes\n"
          + ".  watch (fast-forward until something happens)    F1  Help\n"
          + "(shortcuts are ignored while you are typing in a text box)";

    void showKeys() { GuiCards.keysWindow(this); } // the key-cap sheet; KEYS_HELP stays the plain-text form for the terminal and the tests

    private void installShortcuts() {
        java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() != KeyEvent.KEY_PRESSED || !isActive()) return false;
            Component fo = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            if (fo instanceof javax.swing.text.JTextComponent || fo instanceof JComboBox || fo instanceof JSpinner.DefaultEditor) return false;
            return handleShortcut(e);
        });
    }

    /** Runs the shortcut for a key press (see KEYS_HELP); true if the key was one of ours. */
    boolean handleShortcut(KeyEvent e) {
        if (e.isControlDown()) {
            if (e.getKeyCode() == KeyEvent.VK_S) { GameDriver.saveRequested = true; return true; }
            return false;
        }
        if (e.isAltDown() || e.isMetaDown()) return false;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W: case KeyEvent.VK_UP: queueInput("w"); return true;
            case KeyEvent.VK_S: case KeyEvent.VK_DOWN: queueInput("s"); return true;
            case KeyEvent.VK_A: case KeyEvent.VK_LEFT: queueInput("a"); return true;
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: queueInput("d"); return true;
            case KeyEvent.VK_SPACE: case KeyEvent.VK_NUMPAD5: queueInput("x"); return true;
            case KeyEvent.VK_NUMPAD8: queueInput("w"); return true;
            case KeyEvent.VK_NUMPAD2: queueInput("s"); return true;
            case KeyEvent.VK_NUMPAD4: queueInput("a"); return true;
            case KeyEvent.VK_NUMPAD6: queueInput("d"); return true;
            case KeyEvent.VK_NUMPAD7: case KeyEvent.VK_HOME: queueInput("nw"); return true;
            case KeyEvent.VK_NUMPAD9: case KeyEvent.VK_PAGE_UP: queueInput("ne"); return true;
            case KeyEvent.VK_NUMPAD1: case KeyEvent.VK_END: queueInput("sw"); return true;
            case KeyEvent.VK_NUMPAD3: case KeyEvent.VK_PAGE_DOWN: queueInput("se"); return true;
            case KeyEvent.VK_I: queueInput("i"); return true;
            case KeyEvent.VK_L: queueInput("l"); return true;
            case KeyEvent.VK_R: queueInput("r"); return true;
            case KeyEvent.VK_H: GuiHelp.open(this, "start"); return true;
            case KeyEvent.VK_P: GameDriver.paused = !GameDriver.paused; return true;
            case KeyEvent.VK_Q: GuiQuests.open(this, null); return true;
            case KeyEvent.VK_U: GuiDuties.open(this, "Duties"); return true;
            case KeyEvent.VK_B: showBrowser(); return true;
            case KeyEvent.VK_M: GuiWindows.managePanel(this); return true;
            case KeyEvent.VK_K: GuiWindows.marketPanel(this); return true;
            case KeyEvent.VK_G: GuiWindows.rankingsPanel(this); return true;
            case KeyEvent.VK_T: showScriptEditor(); return true;
            case KeyEvent.VK_F: GuiExtras.familyWindow(this); return true;
            case KeyEvent.VK_N: GuiExtras.notificationsWindow(this); return true;
            case KeyEvent.VK_V: GuiMore.backpackWindow(this); return true;
            case KeyEvent.VK_C: GuiMore.chronicleWindow(this); return true;
            case KeyEvent.VK_J: GuiMore.biographyWindow(this, findPlayer()); return true;
            case KeyEvent.VK_X: GuiMore.worldWindow(this); return true;
            case KeyEvent.VK_Y: GuiMore.notesWindow(this); return true;
            case KeyEvent.VK_PERIOD: queueInput("watch"); return true;
            case KeyEvent.VK_F1: GuiHelp.open(this, "start"); return true; // the old key sheet is the Help page "keys"
            case KeyEvent.VK_O: showPictures = !showPictures; layerThumbs = showPictures; mapPanel.repaint(); return true;
            default: return false;
        }
    }

    // ------------------------------------------------------------------ browser window (select / filter / sort lists)
    private JDialog browserDialog = null;

    /** Non-modal window over the same lists as the terminal `list` command (PlayerLists): pick a row, then act on it. Every action is sent
     *  as a normal command using #id / shop refs, so nothing here touches game state directly. */
    private void showBrowser() {
        if (browserDialog != null && browserDialog.isDisplayable()) { browserDialog.setVisible(true); browserDialog.toFront(); return; }
        Color bg = Theme.PANEL;
        JDialog d = new JDialog(this, "Browser — select, filter, sort", false);
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        d.getContentPane().setBackground(bg);
        d.setLayout(new BorderLayout());
        d.add(GuiHub.header(this, "browser", "Browser"), BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.bold(15));
        List<Runnable> refreshers = new ArrayList<>();
        tabs.addTab("Cultivators", buildBrowserTab("people", refreshers));
        tabs.addTab("Monsters", buildBrowserTab("monsters", refreshers));
        tabs.addTab("Shop items", buildBrowserTab("shop", refreshers));
        tabs.addTab("Backpack", buildBrowserTab("bag", refreshers));
        tabs.addTab("Places", buildBrowserTab("places", refreshers));
        tabs.addTab("Search anyone", GuiExtras.searchTab(this, refreshers));
        d.add(tabs, BorderLayout.CENTER);
        javax.swing.Timer t = new javax.swing.Timer(1500, e -> { int i = tabs.getSelectedIndex(); if (i >= 0 && i < refreshers.size()) refreshers.get(i).run(); });
        tabs.addChangeListener(e -> { int i = tabs.getSelectedIndex(); if (i >= 0 && i < refreshers.size()) refreshers.get(i).run(); });
        d.addWindowListener(new WindowAdapter() { @Override public void windowClosed(WindowEvent e) { t.stop(); } });
        t.start();
        d.setSize(980, 560);
        d.setLocationRelativeTo(this);
        GuiHub.restoreBounds(d, "browser"); GuiHub.trackBounds(d, "browser");
        browserDialog = d;
        d.setVisible(true);
        refreshers.get(0).run();
    }

    private JPanel buildBrowserTab(final String kind, List<Runnable> refreshers) {
        Color bg = Theme.PANEL, fg = Theme.TEXT;
        Font mono = Theme.plain(14);
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBackground(bg);

        final java.util.Set<String> keyset = kind.equals("shop") ? PlayerLists.ITEM_KEYS : kind.equals("bag") ? PlayerLists.BAG_KEYS
                : kind.equals("places") ? PlayerLists.PLACE_KEYS : PlayerLists.PEOPLE_KEYS;
        JComboBox<String> sort = new JComboBox<>();
        sort.addItem("(default)");
        for (String k : keyset) sort.addItem(k);
        JCheckBox desc = new JCheckBox("desc");
        desc.setBackground(bg); desc.setForeground(fg);
        JSpinner limit = new JSpinner(new SpinnerNumberModel(20, 1, ListQuery.MAX_LIMIT, 5));
        JTextField name = new JTextField(9);
        JTextField lvlMin = new JTextField(3), lvlMax = new JTextField(3), priceMax = new JTextField(7), distMax = new JTextField(3), extra = new JTextField(14);
        JComboBox<String> type = new JComboBox<>();
        JCheckBox afford = new JCheckBox("affordable");
        afford.setBackground(bg); afford.setForeground(fg);
        JComboBox<String> aff = new JComboBox<>(new String[]{"any", "good", "neutral", "evil"});
        if (kind.equals("shop") || kind.equals("bag")) for (String s : new String[]{"any", "weapon", "clothing", "pill", "array", "herb", "metal", "blood"}) type.addItem(s);
        if (kind.equals("places")) for (String s : new String[]{"any", "city", "sect", "dungeon", "wild"}) type.addItem(s);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        filters.setBackground(bg);
        java.util.function.BiConsumer<String, Component> add = (label, comp) -> {
            JLabel l = new JLabel(label); l.setForeground(fg); l.setFont(mono);
            filters.add(l); filters.add(comp);
        };
        add.accept("name", name);
        if (!kind.equals("places")) { add.accept("lvl ≥", lvlMin); add.accept("≤", lvlMax); }
        if (kind.equals("people") || kind.equals("monsters")) add.accept("aff", aff);
        if (kind.equals("shop") || kind.equals("bag") || kind.equals("places")) add.accept("type", type);
        if (kind.equals("shop") || kind.equals("bag")) add.accept("price ≤", priceMax);
        if (kind.equals("shop")) filters.add(afford);
        if (kind.equals("places")) add.accept("dist ≤", distMax);
        add.accept("extra", extra);
        add.accept("sort", sort);
        filters.add(desc);
        add.accept("limit", limit);

        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel() { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = new JTable(model);
        table.setFont(mono);
        table.setRowHeight(20);
        table.setBackground(Theme.FIELD);
        table.setForeground(fg);
        table.setSelectionBackground(Theme.SELECT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(Theme.bold(14));
        JLabel status = new JLabel(" ");
        status.setForeground(Theme.ACCENT);
        status.setFont(mono);

        final PlayerLists.ListView[] current = {null};
        final Object[] keepRef = {null};
        Runnable refreshInner = () -> {
            Character p = findPlayer();
            if (p == null || p.pc == null || p.location == null) { status.setText("No player character."); return; }
            List<String> tok = new ArrayList<>();
            tok.add(String.valueOf(limit.getValue()));
            if (sort.getSelectedIndex() > 0) tok.add("sort=" + sort.getSelectedItem() + (desc.isSelected() ? ":desc" : ""));
            else if (desc.isSelected()) tok.add("desc");
            for (String w : name.getText().trim().split("\\s+")) if (!w.isEmpty()) tok.add("name=" + w);
            if (!lvlMin.getText().trim().isEmpty()) tok.add("lvl>=" + lvlMin.getText().trim());
            if (!lvlMax.getText().trim().isEmpty()) tok.add("lvl<=" + lvlMax.getText().trim());
            if (kind.equals("people") || kind.equals("monsters")) { if (aff.getSelectedIndex() > 0) tok.add("aff=" + aff.getSelectedItem()); }
            if (type.getItemCount() > 0 && type.getSelectedIndex() > 0) tok.add("type=" + type.getSelectedItem());
            if (!priceMax.getText().trim().isEmpty()) tok.add("price<=" + priceMax.getText().trim());
            if (afford.isSelected() && kind.equals("shop")) tok.add("afford=1");
            if (!distMax.getText().trim().isEmpty()) tok.add("dist<=" + distMax.getText().trim());
            for (String w : extra.getText().trim().split("\\s+")) if (!w.isEmpty()) tok.add(w);
            PlayerLists.ListView v;
            try {
                ListQuery q = ListQuery.parse(tok.toArray(new String[0]), 0);
                int dl = p.pc.listLimit;
                switch (kind) {
                    case "monsters": v = PlayerLists.people(p, q, true, dl); break;
                    case "shop": v = PlayerLists.shopItems(p, q, dl); break;
                    case "bag": v = PlayerLists.bag(p, q, dl); break;
                    case "places": v = PlayerLists.places(p, q, dl); break;
                    default: v = PlayerLists.people(p, q, false, dl); break;
                }
            } catch (RuntimeException ex) { return; } // the game thread changed a list mid-copy: the next refresh will succeed
            if (v.error != null) { status.setText(v.error); return; }
            Object sel = keepRef[0];
            int row = table.getSelectedRow();
            if (row >= 0 && current[0] != null && row < current[0].refs.size()) sel = current[0].refs.get(row);
            current[0] = v;
            model.setDataVector(v.rows.toArray(new Object[0][]), v.headers);
            if (sel != null) for (int i = 0; i < v.refs.size(); i++) if (v.refs.get(i) == sel) { table.setRowSelectionInterval(i, i); break; }
            status.setText("showing " + v.rows.size() + " of " + v.total + (v.note.isEmpty() ? "" : "   —   " + v.note));
        };
        Runnable refresh = () -> World.read(80, refreshInner);
        refreshers.add(refresh);

        // clicking a header sorts by that column (click again to flip the direction)
        java.util.Map<String, String> headerKey = new java.util.HashMap<>();
        headerKey.put("ID", "id"); headerKey.put("Name", "name"); headerKey.put("Lv", "lvl"); headerKey.put("Aff", "aff"); headerKey.put("Trait", "trait");
        headerKey.put("Sect", "sect"); headerKey.put("Rel", "rel"); headerKey.put("Type", "type"); headerKey.put("Price", "price"); headerKey.put("Value", "price");
        headerKey.put("Qty", "qty"); headerKey.put("Shop owner", "owner"); headerKey.put("Afford", "afford"); headerKey.put("Dist", "dist"); headerKey.put("Beasts", "beasts");
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                if (col < 0) return;
                String key = headerKey.get(String.valueOf(table.getColumnName(col)));
                if (key == null || !keyset.contains(key)) return;
                if (key.equals(sort.getSelectedItem())) desc.setSelected(!desc.isSelected()); else { sort.setSelectedItem(key); desc.setSelected(false); }
                refresh.run();
            }
        });
        sort.addActionListener(e -> refresh.run());
        desc.addActionListener(e -> refresh.run());
        type.addActionListener(e -> refresh.run());
        aff.addActionListener(e -> refresh.run());
        afford.addActionListener(e -> refresh.run());
        limit.addChangeListener(e -> refresh.run());
        java.awt.event.ActionListener enter = e -> refresh.run();
        for (JTextField f : new JTextField[]{name, lvlMin, lvlMax, priceMax, distMax, extra}) f.addActionListener(enter);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        actions.setBackground(bg);
        actions.add(makeBtn("Refresh", refresh));
        actions.add(makeBtn("Set as default limit", () -> queueInput("limit " + limit.getValue())));
        java.util.function.Function<String, Object> selected = msg -> {
            int r = table.getSelectedRow();
            if (r < 0 || current[0] == null || r >= current[0].refs.size()) { status.setText("Select a row first."); return null; }
            keepRef[0] = current[0].refs.get(r);
            return current[0].refs.get(r);
        };
        if (kind.equals("people") || kind.equals("monsters")) {
            String[][] acts = {{"Talk", "talk #%d"}, {"Spar", "spar #%d"}, {"Attack", "attack #%d"}, {"Invite to party", "invite #%d"},
                    {"Seek: befriend", "seek %d friend"}, {"Seek: talk", "seek %d talk"}, {"Seek: court", "seek %d court"}, {"Seek: fight", "seek %d fight"}, {"Seek: spar", "seek %d spar"}};
            for (String[] a : acts) actions.add(makeBtn(a[0], () -> {
                Object o = selected.apply(a[0]);
                if (o instanceof Character) queueInput(String.format(a[1], ((Character) o).id));
            }));
            actions.add(makeBtn("Copy ID", () -> {
                Object o = selected.apply("id");
                if (o instanceof Character) java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(String.valueOf(((Character) o).id)), null);
            }));
        } else if (kind.equals("shop")) {
            JButton buy = makeBtn("Buy selected", () -> {
                Object o = selected.apply("buy");
                if (o instanceof PlayerLists.ShopItem) { PlayerLists.ShopItem si = (PlayerLists.ShopItem) o; queueInput("buy " + si.shopNo + "." + si.itemNo + " " + si.item.name); }
            });
            buy.setForeground(Theme.OK);
            actions.add(buy);
        } else if (kind.equals("bag")) {
            actions.add(makeBtn("Sell selected", () -> {
                Object o = selected.apply("sell");
                Character p = findPlayer();
                if (o instanceof Item && p != null) queueInput("sell " + (p.backpack.indexOf((Item) o) + 1));
            }));
        } else {
            actions.add(makeBtn("Go there", () -> {
                Object o = selected.apply("go");
                if (o instanceof Area) queueInput("goto " + ((Area) o).x + " " + ((Area) o).y);
            }));
            actions.add(makeBtn("Add waypoint quest", () -> {
                Object o = selected.apply("wp");
                if (o instanceof Area) queueInput("qnew " + ((Area) o).name.replace(' ', '_') + " " + ((Area) o).x + " " + ((Area) o).y + " 70");
            }));
        }

        JPanel north = new JPanel(new BorderLayout());
        north.setBackground(bg);
        north.add(filters, BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(bg);
        south.add(actions, BorderLayout.NORTH);
        south.add(status, BorderLayout.SOUTH);
        panel.add(north, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    // ------------------------------------------------------------------ script editor (hybrid mode)
    private JDialog scriptDialog = null;

    /** Non-modal editor for the controlled character's hybrid script. Every edit is sent as a `script ...` command so the game thread
     *  stays the only writer; the list refreshes from a snapshot twice a second. */
    private void showScriptEditor() { GuiScripts.open(this); }

    private JPanel row(Color bg) {
        JPanel r = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 1));
        r.setBackground(bg);
        return r;
    }

    /** Called at the end of each simulation tick from GameDriver. */
    public void refresh() { SwingUtilities.invokeLater(this::refreshNow); }

    /** Runs on the Swing thread. The game hands the world over at the end of every tick (see World), so this normally reads a stable state;
     *  if the lock can't be had quickly the refresh is skipped and the previous picture stays until the next one. */
    void refreshNow() {
        mapPanel.renderBuffer(200); // draw the finished tick now, while the game has handed the world over
        mapPanel.repaint();
        World.read(300, () -> {
            if (selX < 0) { // nothing selected yet (start-up or a fresh load): show the player's own tile
                Character me = findPlayer();
                if (me != null && me.location != null) { selX = me.location.x; selY = me.location.y; }
            }
            if (selX >= 0 && selY >= 0 && Map.tiles[selX][selY] != null) updateInfo(Map.tiles[selX][selY]);
            updateTracker();
            updateStatusBar();
        });
        refreshLog();
    }

    private void refreshLog() {
        List<String> evSnap;
        synchronized (GameDriver.eventLog) { evSnap = new ArrayList<>(GameDriver.eventLog); }
        StringBuilder sb = new StringBuilder();
        for (String line : evSnap) if (logLineVisible(line)) sb.append(line).append("\n");
        String evText = sb.toString();
        if (!evText.equals(logArea.getText())) {
            logArea.setText(evText);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }

        List<String> histSnap;
        synchronized (GameDriver.historyLog) { histSnap = new ArrayList<>(GameDriver.historyLog); }
        StringBuilder hsb = new StringBuilder();
        for (String line : histSnap) hsb.append(line).append("\n");
        String hText = hsb.toString();
        if (!hText.equals(historyArea.getText())) {
            historyArea.setText(hText);
            historyArea.setCaretPosition(historyArea.getDocument().getLength());
        }
    }

    private void updateTracker() {
        trackerPanel.removeAll();
        Color gold = Theme.GOLD;
        Color fg   = Theme.TEXT;
        Color dim  = Theme.DIM;
        Color red  = Theme.DANGER;

        if (Map.trackedCharacters.isEmpty()) {
            JLabel empty = new JLabel("(none tracked — click a cultivator name to track)");
            empty.setForeground(dim);
            empty.setFont(Theme.plain(16));
            trackerPanel.add(empty);
        } else {
            boolean anyDead = false;
            for (int i = 0; i < Map.trackedCharacters.size(); i++) {
                int cid = Map.trackedCharacters.get(i);
                if (cid < 0 || cid >= Map.characters.size()) continue;
                Character c = Map.characters.get(cid);
                if (c == null || c.dead) {
                    JPanel drow = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
                    drow.setBackground(Theme.BG);
                    drow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
                    drow.setAlignmentX(Component.LEFT_ALIGNMENT);
                    JLabel dead = new JLabel("† " + (c != null ? c.name + " " + c.lastName : "ID:" + cid) + " (deceased)");
                    dead.setForeground(dim);
                    dead.setFont(Theme.plain(16));
                    final int goneCid = cid;
                    JButton goneBtn = new JButton("×");
                    goneBtn.setFont(Theme.bold(16));
                    goneBtn.setForeground(red);
                    goneBtn.setToolTipText("Take them off the tracker.");
                    goneBtn.putClientProperty("JButton.buttonType", "toolBarButton");
                    goneBtn.setBorderPainted(false);
                    goneBtn.setFocusPainted(false);
                    goneBtn.setMargin(new Insets(0, 2, 0, 2));
                    goneBtn.addActionListener(e -> { Map.trackedCharacters.remove(Integer.valueOf(goneCid)); updateTracker(); });
                    drow.add(dead);
                    drow.add(goneBtn);
                    trackerPanel.add(drow);
                    anyDead = true;
                    continue;
                }
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
                row.setBackground(Theme.BG);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
                row.setAlignmentX(Component.LEFT_ALIGNMENT);

                JButton nameBtn = new JButton(c.name + " " + c.lastName);
                nameBtn.setFont(Theme.bold(16));
                nameBtn.setForeground(c.player == 1 ? gold : fg);
                nameBtn.putClientProperty("JButton.buttonType", "toolBarButton");
                nameBtn.setBorderPainted(false);
                nameBtn.setFocusPainted(false);
                nameBtn.setMargin(new Insets(0, 2, 0, 2));
                final Character tracked = c;
                nameBtn.addActionListener(e -> showCharacterDialog(tracked));

                JLabel info = new JLabel(" Lv." + c.lvl + " Age" + c.age
                        + " [" + (c.location != null ? c.location.x + "," + c.location.y : "?") + "]");
                info.setForeground(dim);
                info.setFont(Theme.plain(16));

                final int removeCid = cid;
                JButton removeBtn = new JButton("×");
                removeBtn.setFont(Theme.bold(16));
                removeBtn.setForeground(red);
                removeBtn.putClientProperty("JButton.buttonType", "toolBarButton");
                removeBtn.setBorderPainted(false);
                removeBtn.setFocusPainted(false);
                removeBtn.setMargin(new Insets(0, 2, 0, 2));
                removeBtn.addActionListener(e -> {
                    Map.trackedCharacters.remove(Integer.valueOf(removeCid));
                    updateTracker();
                });

                row.add(nameBtn);
                row.add(info);
                row.add(removeBtn);
                trackerPanel.add(row);
            }
            if (anyDead) {
                JButton clear = makeBtn("Clear deceased", "Take every dead character off the tracker.", () -> {
                    for (int tid : new ArrayList<>(Map.trackedCharacters)) {
                        Character tc = tid >= 0 && tid < Map.characters.size() ? Map.characters.get(tid) : null;
                        if (tc == null || tc.dead) Map.trackedCharacters.remove(Integer.valueOf(tid));
                    }
                    updateTracker();
                });
                trackerPanel.add(clear);
            }
        }
        Ui.scale(trackerPanel, Settings.fontScale);
        trackerPanel.revalidate();
        trackerPanel.repaint();
    }

    /** Shows a detailed character dialog with Track and Control buttons. */
    void showCharacterDialog(Character c) {
        if (!World.readWait(500, () -> buildCharacterDialog(c))) buildCharacterDialog(c); // busy tick: build anyway (a stale field is harmless)
    }

    private void buildCharacterDialog(Character c) {
        JDialog dialog = new JDialog(this, c.name + " " + c.lastName, false);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setBackground(Theme.BG);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.PANEL);
        panel.setBorder(new EmptyBorder(10, 14, 10, 14));

        Color gold   = Theme.GOLD;
        Color fg     = Theme.TEXT;
        Color dim    = Theme.DIM;
        Color cyan   = new Color(80, 210, 210);
        Color purple = new Color(190, 130, 255);

        Runnable addLbl = null; // placeholder — use helper inline
        java.util.function.BiConsumer<String, Color> dlgLabel = (text, color) -> {
            JLabel lbl = new JLabel(text);
            lbl.setForeground(color);
            lbl.setFont(Theme.plain(17));
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(lbl);
        };

        if (Settings.pictures) {
            JLabel pic = new JLabel(new ImageIcon(ArtMore.portraitCached(c, 160, 200)));
            pic.setAlignmentX(Component.LEFT_ALIGNMENT);
            pic.setBorder(new EmptyBorder(0, 0, 8, 0));
            panel.add(pic);
        }
        dlgLabel.accept(c.name + " " + c.lastName + (c.repTitle != null ? " '" + c.repTitle + "'" : ""), gold);
        dlgLabel.accept("Realm: Major " + c.majorLvl + "  Minor " + c.minorLvl + "  Lv." + c.lvl, fg);
        dlgLabel.accept("Cultivation: " + (int)c.cultivation + " / " + (int)c.cultivationBaseMax, dim);
        dlgLabel.accept("Age: " + c.age + " / " + c.maxAge + "  Potential: " + c.potentialLabel(), fg);
        dlgLabel.accept("Trait: " + c.getTrait() + "  Affiliation: " + c.affiliation + "  Gender: " + c.gender, fg);
        dlgLabel.accept("Profession Lv." + c.professionShown(), dim);
        dlgLabel.accept("Wealth: " + c.ss + " SS  " + c.sc + " SC  " + c.dc + " DC", fg);
        dlgLabel.accept("Weapon: " + (c.equippedWeapon != null ? c.equippedWeapon.toString() : "None"), fg);
        dlgLabel.accept("Armor:  " + (c.equippedClothing != null ? c.equippedClothing.toString() : "None"), fg);
        dlgLabel.accept("Kills: " + c.kills + "  Items crafted: " + c.itemsCrafted + (c.trait == Character.Traits.Innovative ? "  Inspiration: " + c.inspiration + "/" + Character.INSPIRATION_NEEDED : ""), dim);
        if (c.sect != null) dlgLabel.accept("Sect: " + c.sect.name + " [" + c.sect.getRank(c) + "]", purple);
        if (c.spouse != null) dlgLabel.accept("Spouse: " + c.spouse.name + " " + c.spouse.lastName, new Color(255, 160, 200));
        dlgLabel.accept("Location: " + (c.location != null ? c.location.name + " [" + c.location.x + "," + c.location.y + "]" : "?"), dim);
        dlgLabel.accept("ID: " + c.id + "  Player: " + c.player, dim);

        panel.add(Box.createVerticalStrut(8));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        btnRow.setBackground(Theme.PANEL);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton trackBtn = makeBtn(Map.trackedCharacters.contains(c.id) ? "✓ Tracked" : "Track", () -> {
            if (!Map.trackedCharacters.contains(c.id)) Map.trackedCharacters.add(c.id);
            updateTracker();
            dialog.dispose();
        });

        JButton controlBtn = makeBtn("Control", () -> {
            if (findPlayer() != null) { // the game thread does the switch (and keeps duties/policies) when it next asks for a command
                if (!GuiExtras.confirm(dialog, "Take control of " + c.name + " " + c.lastName + " (Lv." + c.lvl + ")? Your current cultivator carries on as an NPC.")) return;
                queueInput("!control #" + c.id);
            } else { // spectating: nobody to hand over from
                c.player = 1;
                System.out.println("[GUI] Player now controls: " + c.name + " " + c.lastName);
            }
            dialog.dispose();
        });
        controlBtn.setForeground(Theme.GOLD);

        JButton closeBtn = makeBtn("Close", dialog::dispose);
        JButton bioBtn = makeBtn("Biography", () -> GuiMore.biographyWindow(this, c));

        Character me = findPlayer();
        if (me != null && me != c && c.player != 1 && c.age >= 8 && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster) { // from anywhere in the world: a quest that walks to them
            final int cid = c.id;
            btnRow.add(makeBtn("Go & talk", "Pin a quest that walks to them and talks (it follows them if they move).", () -> queueInput("seek " + cid + " talk")));
            if (QuestLife.suitor(me, c)) btnRow.add(makeBtn("Go & court", "Pin a quest that walks to them and courts them.", () -> queueInput("seek " + cid + " court")));
        }
        btnRow.add(bioBtn);
        btnRow.add(trackBtn);
        btnRow.add(controlBtn);
        btnRow.add(closeBtn);
        panel.add(btnRow);

        JScrollPane scroll = new JScrollPane(panel);
        scroll.getViewport().setBackground(Theme.PANEL);
        scroll.setBorder(null);
        dialog.setLayout(new BorderLayout());
        dialog.add(GuiHub.header(this, "character", c.name), BorderLayout.NORTH);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.setSize(420, 460);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void updateInfo(Area area) {
        infoPanel.removeAll();
        Color gold   = Theme.GOLD;
        Color purple = new Color(190, 130, 255);
        Color fg     = Theme.TEXT;
        Color dim    = Theme.DIM;
        Color red    = Theme.DANGER;
        Color cyan   = new Color(80, 210, 210);

        String[] SEASON_NAMES = {"Spring", "Summer", "Autumn", "Winter"};
        addLabel("Season: " + SEASON_NAMES[GameDriver.currentSeason] + (area != null ? "   Weather here: " + ArtMore.WEATHER_NAMES[ArtMore.weatherAt(area)] : ""), dim);
        Character pl = findPlayer();
        if (pl != null && pl.location != null)
            addLabel("Player: [" + pl.location.x + "," + pl.location.y + "] Lv." + pl.lvl, new Color(255, 255, 100));
        addSep();

        if (area == null) {
            addLabel("(Empty tile)", dim);
        } else {
            if (showPictures) addBanner(area);
            addLabel(area.name, gold);
            if (area.sect != null && showPictures) {
                JLabel sealLbl = new JLabel(area.sect.name, new ImageIcon(ArtMore.seal(area.sect.name, 40)), SwingConstants.LEFT);
                sealLbl.setForeground(Theme.DIM); sealLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                infoPanel.add(sealLbl);
            }
            if (area.geoName != null && !area.geoName.equals(area.name)) addLabel("(the land here was called " + area.geoName + " before the settlement)", dim); // a founded city or a dungeon renames the tile
            String regionText = Regions.line(area);
            if (!regionText.isEmpty()) addLabel(regionText, dim);
            String lvlLine = (area.city ? "City level " + area.cityLevel() + " (grows with the treasury)  ·  tile level " : "Level ") + area.lvl + (area.hotspot ? "  ★ Hotspot" : "");
            if (area.spiritualVein) lvlLine += "  ✦ Spirit Vein";
            addLabel(lvlLine, area.spiritualVein ? cyan : fg);
            addLabel("Tile [" + area.x + ", " + area.y + "]  Biome: " + area.biomeM, dim);
            addSep();

            if (area.city) {
                String owner = area.cityOwner != null ? area.cityOwner.name + " " + area.cityOwner.lastName : "None";
                addLabel("City  Owner: " + owner, gold);
                addLabel("Population: " + area.population, dim);
            }
            if (area.sect != null) {
                addLabel("Sect: " + area.sect.name, purple);
                if (area.sect.master == null && area.sect.isVirtual()) addLabel("  Master: ???", dim);
                else if (area.sect.master != null)
                    addLabel("  Master: " + area.sect.master.name + " " + area.sect.master.lastName + " Lv." + area.sect.master.lvl, dim);
                if (area.sect.territoryBonus > 0)
                    addLabel("  Territory Bonus: +" + area.sect.territoryBonus, cyan);
            }
            if (area.dungeonType != null)
                addLabel("Dungeon: " + area.dungeonType.name().replace('_', ' '),
                    area.dungeonType == Area.DungeonType.CELESTIAL_TRIAL ? cyan : red);
            if (area.celestialTrialTimer > 0)
                addLabel("  Closes in: " + area.celestialTrialTimer + " ticks", dim);
            if (area.hasHeaven())
                addLabel("Heaven Treasure: " + area.heavenLabel(), gold);
            if (area.arrayBag != null && !area.arrayBag.isEmpty())
                addLabel("Arrays: " + area.arrayBag.size(), Theme.ACCENT);
            addSep();

            try {
                long cultivatorCount = 0;
                int topLvl = 0;
                for (Character c : area.characters) {
                    if (c != null && !c.dead) { cultivatorCount++; if (c.lvl > topLvl) topLvl = c.lvl; }
                }
                addLabel("Cultivators: " + cultivatorCount + (topLvl > 0 ? "  (top Lv." + topLvl + ")" : "")
                        + (area.isOriginis() ? "" : "  / cap " + area.crowdCapacity()), fg);
                if (area.crowdHotspot) addLabel("⚠ Overcrowded: beasts are ambushing here", red);
            } catch (Exception ignored) {}
            addLabel("Beasts: " + area.monsters, fg);
            addLabel("Bounties: " + area.bounties.size(), area.bounties.isEmpty() ? dim : new Color(255, 140, 60));
            addSep();

            addLabel("Top cultivators (click to inspect):", dim);
            try {
                area.characters.stream()
                    .filter(c -> c != null && !c.dead)
                    .sorted((a, b) -> Integer.compare(b.lvl, a.lvl))
                    .limit(5)
                    .forEach(c -> {
                        String trait = c.getTrait();
                        Color tc = c.trait == Character.Traits.Monster ? red : (c.player >= 1 ? gold : fg);
                        String title = c.repTitle != null ? " '" + c.repTitle + "'" : "";
                        String labelText = "  " + c.name + " " + c.lastName + title + " Lv." + c.lvl
                                + " [" + trait.substring(0, Math.min(4, trait.length())) + "]";
                        // Make each cultivator a clickable button
                        JButton btn = new JButton(labelText);
                        btn.setFont(Theme.plain(17));
                        btn.setForeground(tc);
                        btn.putClientProperty("JButton.buttonType", "toolBarButton");
                        btn.setBorderPainted(false);
                        btn.setFocusPainted(false);
                        btn.setHorizontalAlignment(SwingConstants.LEFT);
                        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
                        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
                        btn.setMargin(new Insets(0, 0, 0, 0));
                        btn.addActionListener(e -> showCharacterDialog(c));
                        infoPanel.add(btn);
                    });
            } catch (Exception ignored) {}

            if (GameDriver.guiEnabled) {
                addSep();
                final int tx = area.x, ty = area.y;
                JButton moveBtn = makeBtn("▶ Move Here [" + tx + "," + ty + "]", () -> {
                    Character p2 = findPlayer();
                    if (p2 != null) queueInput("goto " + tx + " " + ty);
                });
                moveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                moveBtn.setToolTipText("Walk to this tile, one tile a turn (diagonals included). Right-click the map for more.");
                JButton routeBtn = makeBtn("Route", "Show the walk to this tile (distance and turns) without moving.", () -> queueInput("route " + tx + " " + ty));
                JButton wpBtn = makeBtn("Waypoint", "Pin a quest that leads to this tile; AUTO and scripts walk there, in MANUAL you are alerted.", () -> queueInput("qnew " + area.name.replace(' ', '_') + " " + tx + " " + ty + " 70"));
                infoPanel.add(moveBtn);
                JPanel mvRow = new JPanel(new GridLayout(1, 2, 4, 0));
                mvRow.setOpaque(false);
                mvRow.setAlignmentX(Component.LEFT_ALIGNMENT);
                mvRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                mvRow.add(routeBtn); mvRow.add(wpBtn);
                infoPanel.add(Box.createVerticalStrut(3));
                infoPanel.add(mvRow);
                if (area.city || area.sect != null) {
                    final Area detailArea = area;
                    JButton detailBtn = makeBtn("City / sect details…", () -> GuiMore.placeWindow(this, detailArea));
                    detailBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                    infoPanel.add(detailBtn);
                }
            }
        }
        Ui.scale(infoPanel, Settings.fontScale);
        infoPanel.revalidate();
        infoPanel.repaint();
    }

    private JPanel buildLegend() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Theme.PANEL);
        p.setBorder(new EmptyBorder(4, 8, 6, 8));
        addLegendRow(p, new Color(204, 164, 86), "City");
        addLegendRow(p, new Color(132, 94, 154),  "Sect");
        addLegendRow(p, new Color(120, 90, 20),   "Lost Ruins");
        addLegendRow(p, new Color(160, 50, 10),   "Beast Lair");
        addLegendRow(p, new Color(40, 80, 150),   "Martial Trial");
        addLegendRow(p, new Color(60, 10, 90),    "Haunted Grounds");
        addLegendRow(p, new Color(60, 160, 160),  "Celestial Trial");
        addLegendRow(p, new Color(30, 160, 140),  "Spiritual Vein");
        addLegendRow(p, new Color(255, 255, 100), "● Player position");
        addLegendRow(p, new Color(60, 100, 20),   "Wildlands (low lvl)");
        addLegendRow(p, new Color(20, 40, 10),    "Wildlands (high lvl)");
        return p;
    }

    private void addLegendRow(JPanel p, Color swatch, String label) {
        JPanel r = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        r.setBackground(Theme.PANEL);
        JLabel dot = new JLabel("■");
        dot.setForeground(label.startsWith("●") || label.equals("City") || label.equals("Sect") ? swatch : Theme.tile(swatch, null));
        dot.setFont(Theme.plain(16));
        JLabel txt = new JLabel(label);
        txt.setForeground(Theme.DIM);
        txt.setFont(Theme.plain(16));
        r.add(dot); r.add(txt);
        p.add(r);
    }

    private void addLabel(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(color);
        lbl.setFont(Theme.plain(17));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(lbl);
    }

    private void addSep() {
        infoPanel.add(Box.createVerticalStrut(3));
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(50, 50, 70));
        sep.setMaximumSize(new Dimension(260, 1));
        infoPanel.add(sep);
        infoPanel.add(Box.createVerticalStrut(3));
    }

    // ── Tile rendering ────────────────────────────────────────────────────────

    private class MapPanel extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() { return new Dimension(520, 520); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 64; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return true; }

        MapPanel() {
            setBackground(Theme.BG);
            setToolTipText("");
            addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override public void componentResized(java.awt.event.ComponentEvent e) { repaint(); }
            });
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() != 2) return;
                    int ts = tileSize();
                    int tx = tileX(e.getX(), ts), ty = tileY(e.getY(), ts);
                    if (tx >= 0 && tx < Map.range && ty >= 0 && ty < Map.range && Map.tiles[tx][ty] != null) showLargeView(Map.tiles[tx][ty]);
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            int ts = tileSize();
            return new Dimension(vis() * ts, vis() * ts);
        }

        @Override public String getToolTipText(MouseEvent e) {
            int ts = tileSize();
            final int tx = tileX(e.getX(), ts), ty = tileY(e.getY(), ts);
            if (tx < 0 || ty < 0 || tx >= Map.range || ty >= Map.range) return null;
            final String[] out = {null};
            World.read(40, () -> {
                Area a = Map.tiles[tx][ty];
                if (a == null) return;
                int pop = 0;
                try { pop = a.characters.size(); } catch (RuntimeException ignored) { }
                String nm = a.name == null ? "" : a.name.replace("&", "&amp;").replace("<", "&lt;");
                if (a.geoName != null && !a.geoName.equals(a.name)) nm += " <i>(" + a.geoName.replace("&", "&amp;").replace("<", "&lt;") + ")</i>";
                out[0] = "<html><b>" + nm + "</b><br>[" + a.x + ", " + a.y + "]  Lv." + a.lvl + (a.city ? "  City Lv." + a.cityLevel() : "") + (a.sect != null ? "  Sect" : "")
                        + "<br>" + pop + " cultivators · " + a.monsters + " beasts" + (a.crowdHotspot ? "<br>overcrowded" : "") + "</html>";
            });
            return out[0];
        }

        private Point center(Area a, int ts) {
            int c = colOf(a.x), r = rowOf(a.y);
            if (c < 0 || r < 0) return new Point(-9999, -9999); // out of view
            return new Point(c * ts + ts / 2, r * ts + ts / 2);
        }

        private BufferedImage buf;

        void paintExport(Graphics2D g, int ts, int size) {
            exportTile = ts;
            try { g.setColor(getBackground()); g.fillRect(0, 0, size, size); paintWorld(g); } finally { exportTile = 0; }
        }

        /** Paints the map into an off-screen picture while the world is stable; if the game is mid-tick the last finished picture is shown instead. */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;
            renderBuffer(30);
            if (buf != null) g.drawImage(buf, 0, 0, null);
        }

        /** Renders the world into the off-screen picture when it can read it within `ms`. The game calls this at the end of every tick (see refreshNow) while it has
         *  handed the world over: a repaint scheduled for later would find the game running again and keep showing the old picture (the map froze during scripts and walks). */
        void renderBuffer(long ms) {
            int w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;
            final BufferedImage target = (buf != null && buf.getWidth() == w && buf.getHeight() == h) ? buf : new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            boolean ok = World.read(ms, () -> {
                Graphics2D bg = target.createGraphics();
                try {
                    bg.setColor(getBackground()); bg.fillRect(0, 0, w, h);
                    int ox = offX(), oy = offY(), side = vis() * tileSize();
                    bg.translate(ox, oy);
                    paintWorld(bg);
                    bg.translate(-ox, -oy);
                    bg.setColor(Theme.alpha(Theme.GOLD, 110)); bg.drawRect(ox - 2, oy - 2, side + 2, side + 2); // thin gold frame round the world
                } finally { bg.dispose(); }
            });
            if (ok) buf = target;
        }

        private void paintWorld(Graphics g) {
            int ts = tileSize();
            int playerX = -1, playerY = -1;
            Character pl = findPlayer();
            if (pl != null && pl.location != null) { playerX = pl.location.x; playerY = pl.location.y; }
            if (zoom > 1 && followMe && pl != null && pl.location != null) { viewX = pl.location.x; viewY = pl.location.y; } // zoomed: the player stays in the middle
            java.util.Set<Area> known = null;
            if (layerFog && pl != null) { try { known = new java.util.HashSet<>(pl.knownLocations); } catch (RuntimeException ignored) { known = null; } }
            final int mode = colorMode;
            PlaceImages.thumbSync = exportTile > 0; // an export waits for every picture; the live map draws what is ready and gets the rest from a background thread

            for (int x = 0; x < Map.range; x++) {
                int col = colOf(x);
                if (col < 0) continue; // not in view (zoomed)
                int px = col * ts;
                for (int y = 0; y < Map.range; y++) {
                    int row = rowOf(y);
                    if (row < 0) continue;
                    Area area = Map.tiles[x][y];
                    int sy = row * ts; // screen row: +y (north, the 'w' key) is drawn upward
                    boolean fogged = known != null && (area == null || !known.contains(area));
                    g.setColor(fogged ? new Color(6, 6, 10) : tileColor(area, mode));
                    g.fillRect(px, sy, ts - 1, ts - 1);
                    if (!fogged && layerThumbs && area != null && ts >= 12) {
                        BufferedImage im = PlaceImages.thumb(area, ts);
                        if (im != null) {
                            int side = Math.min(im.getWidth(), im.getHeight()), sx0 = (im.getWidth() - side) / 2;
                            g.drawImage(im, px, sy, px + ts - 1, sy + ts - 1, sx0, 0, sx0 + side, side, null);
                        }
                    }
                    if (fogged) {
                        // nothing else is drawn for tiles the player has not seen (the selection ring and player marker below still are)
                    }
                    if (layerCrowd && area != null && !fogged) {
                        if (area.crowdHotspot) { g.setColor(new Color(255, 40, 40)); g.drawRect(px, sy, ts - 2, ts - 2); g.drawRect(px + 1, sy + 1, ts - 4, ts - 4); }
                        else if (!area.isOriginis() && area.characters.size() > area.crowdCapacity() * 0.8) { g.setColor(new Color(255, 190, 40)); g.drawRect(px, sy, ts - 2, ts - 2); }
                    }
                    if (x == selX && y == selY) {
                        g.setColor(Color.WHITE);
                        g.drawRect(px, sy, ts - 2, ts - 2);
                    }
                    if (x == playerX && y == playerY) {
                        g.setColor(new Color(255, 255, 100));
                        int cx = px + ts / 2, cy = sy + ts / 2, r = Math.max(2, ts / 4);
                        g.fillOval(cx - r, cy - r, r * 2, r * 2);
                    }
                    // Tracked NPC dots (cyan)
                    for (int tid : new ArrayList<>(Map.trackedCharacters)) { // snapshot: a cheat/GUI action can add ids while painting
                        if (tid < 0 || tid >= Map.characters.size()) continue;
                        Character tc = Map.characters.get(tid);
                        if (tc != null && !tc.dead && tc.location != null
                                && tc.location.x == x && tc.location.y == y
                                && !(x == playerX && y == playerY)) {
                            g.setColor(new Color(0, 220, 220));
                            int cx2 = px + ts / 2, cy2 = sy + ts / 2, r2 = Math.max(2, ts / 5);
                            g.fillOval(cx2 - r2, cy2 - r2, r2 * 2, r2 * 2);
                            break;
                        }
                    }
                }
            }

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (layerQuests && pl != null && pl.pc != null) paintQuestPins(g2, pl, ts);
            if (layerPath && pl != null && pl.location != null && selX >= 0 && selY >= 0 && Map.tiles[selX][selY] != null
                    && !(selX == pl.location.x && selY == pl.location.y)) {
                Point a = center(pl.location, ts), b = center(Map.tiles[selX][selY], ts);
                g2.setColor(new Color(255, 255, 255, 200));
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{6f, 6f}, 0f));
                g2.drawLine(a.x, a.y, b.x, b.y);
                g2.setStroke(new BasicStroke(1f));
                g2.setFont(new Font("Monospaced", Font.BOLD, Math.max(11, ts / 2)));
                g2.drawString(PlayerControl.distance(pl.location, Map.tiles[selX][selY]) + " tiles", (a.x + b.x) / 2 + 4, (a.y + b.y) / 2 - 4);
            }
            g.setColor(new Color(230, 230, 200));
            g.setFont(new Font("Monospaced", Font.BOLD, Math.max(12, ts)));
            g.drawString("N ↑", 4, Math.max(12, ts)); // compass: north (the 'w' / North button) is up
        }

        private void paintQuestPins(Graphics2D g2, Character pl, int ts) {
            List<Quest> qs = new ArrayList<>();
            try {
                qs.addAll(pl.targetQuests);
                if (pl.currentQuest != null) qs.add(pl.currentQuest);
            } catch (RuntimeException ignored) { return; }
            int n = 0;
            for (Quest q : qs) {
                Area a = q.targetArea;
                if (q.category == Quest.QuestCategory.Character) { Character t = Character.charById(q.targetCharacter); a = t != null && !t.dead ? t.location : null; }
                if (a == null || q.category == Quest.QuestCategory.Spot || ++n > 80) continue;
                int cls = PlayerControl.classify(pl, q);
                Color c = cls == PlayerControl.ALERT ? new Color(255, 150, 30) : cls >= 0 ? new Color(60, 210, 230) : new Color(150, 150, 150);
                Point p = center(a, ts);
                int r = Math.max(3, ts / 3);
                Polygon d = new Polygon(new int[]{p.x, p.x + r, p.x, p.x - r}, new int[]{p.y - r, p.y, p.y + r, p.y}, 4);
                g2.setColor(c);
                g2.fillPolygon(d);
                g2.setColor(Color.BLACK);
                g2.drawPolygon(d);
            }
        }

        private Color tileColor(Area area, int mode) {
            if (area == null) return Theme.BG;
            if (mode == 1) { // beast density (log scale)
                double v = Math.min(1, Math.log10(area.monsters + 1) / 7.5);
                return new Color((int) (25 + 200 * v), (int) (30 + 40 * (1 - v)), 30);
            }
            if (mode == 2) { // population: cultivators standing in the tile
                int pop = 0;
                try { for (Character c : area.characters) if (c != null && !c.dead) pop++; } catch (Exception ignored) { }
                double v = Math.min(1, Math.log10(pop + 1) / 3.5);
                return new Color((int) (20 + 30 * v), (int) (40 + 150 * v), (int) (60 + 190 * v));
            }
            if (mode == 3) { // area level: green (weak) to red (strong)
                double v = Math.min(1, area.lvl / 100.0);
                return new Color((int) (40 + 200 * v), (int) (200 - 170 * v), 40);
            }
            return tileColor(area);
        }

        /** Okabe-Ito based palette: places differ in hue AND lightness, terrain stays muted grey-blue/olive so it never competes with them. */
        private Color tileColorBlind(Area area) {
            Color base;
            if (area.city) base = new Color(230, 159, 0);
            else if (area.sect != null) base = new Color(0, 114, 178);
            else if (area.dungeonType == Area.DungeonType.CELESTIAL_TRIAL) base = new Color(240, 240, 240);
            else if (area.dungeonType != null) {
                switch (area.dungeonType) {
                    case LOST_RUINS: base = new Color(240, 228, 66); break;
                    case BEAST_LAIR: base = new Color(213, 94, 0); break;
                    case MARTIAL_TRIAL: base = new Color(86, 180, 233); break;
                    case HAUNTED_GROUNDS: base = new Color(204, 121, 167); break;
                    default: base = new Color(120, 120, 120);
                }
            } else if (area.spiritualVein) base = new Color(0, 158, 115);
            else {
                int v = Math.max(20, 70 - area.lvl / 2);
                switch (area.biomeM) {
                    case "Forest": base = new Color(v - 10, v + 10, v - 10); break;
                    case "Mountain": base = new Color(v + 20, v + 20, v + 20); break;
                    case "Plains": base = new Color(v + 10, v + 15, v - 5); break;
                    case "Desert": base = new Color(v + 30, v + 20, v - 10); break;
                    case "Swamp": base = new Color(v - 10, v, v + 10); break;
                    case "Tundra": base = new Color(v + 25, v + 35, v + 55); break;
                    case "Ocean": base = new Color(v - 15, v + 5, v + 45); break;
                    case "Grassland": base = new Color(v + 8, v + 22, v - 8); break;
                    default: base = new Color(v, v + 5, v);
                }
            }
            if (area.hotspot) base = brighten(base, 55);
            return base;
        }

        private Color tileColor(Area area) {
            if (area == null) return Theme.BG;
            if (Settings.colorblindMap) return tileColorBlind(area);
            Color base;
            if (area.city) {
                base = new Color(200, 160, 20);
            } else if (area.sect != null) {
                base = new Color(120, 40, 180);
            } else if (area.dungeonType == Area.DungeonType.CELESTIAL_TRIAL) {
                base = new Color(60, 160, 160);
            } else if (area.dungeonType != null) {
                switch (area.dungeonType) {
                    case LOST_RUINS:      base = new Color(120, 90, 20);  break;
                    case BEAST_LAIR:      base = new Color(160, 50, 10);  break;
                    case MARTIAL_TRIAL:   base = new Color(40, 80, 150);  break;
                    case HAUNTED_GROUNDS: base = new Color(60, 10, 90);   break;
                    default:              base = new Color(60, 60, 60);
                }
            } else if (area.spiritualVein) {
                base = new Color(30, 160, 140);
            } else {
                // terrain colours from the biome and the height (BiomeGen), a little darker where the wildlands are more dangerous
                Color tc = new Color(BiomeGen.rgb(area.biomem, area.elev, area.river));
                double dark = Math.max(0.55, 1.0 - area.lvl / 220.0);
                base = new Color((int) (tc.getRed() * dark * 0.8), (int) (tc.getGreen() * dark * 0.8), (int) (tc.getBlue() * dark * 0.8));
            }
            if (area.hotspot) base = brighten(base, 55);
            try {
                int pop = 0;
                for (Character c : area.characters) { if (c != null && !c.dead) pop++; }
                if (pop > 0) base = brighten(base, Math.min(45, pop * 3));
            } catch (Exception ignored) {}
            return Theme.tile(base, area);
        }

        private Color brighten(Color c, int amt) {
            return new Color(
                Math.min(255, c.getRed()   + amt),
                Math.min(255, c.getGreen() + amt),
                Math.min(255, c.getBlue()  + amt));
        }
    }
}
