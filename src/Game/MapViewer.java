package Game;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class MapViewer extends JFrame {

    private final MapPanel mapPanel;
    private final JPanel infoPanel;
    private final JPanel trackerPanel;
    private final JTextArea logArea;
    private final JTextArea historyArea;
    private volatile int selX = -1, selY = -1;

    private int tileSize() {
        int panelW = mapPanel != null ? mapPanel.getWidth()  : 0;
        int panelH = mapPanel != null ? mapPanel.getHeight() : 0;
        if (panelW > 10 && panelH > 10) {
            return Math.max(4, Math.min(panelW, panelH) / Map.range);
        }
        return Math.max(8, Math.min(32, 700 / Map.range));
    }

    public MapViewer() {
        super("Tales of Dream and Fantasy 2 — World Map");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setBackground(new Color(15, 15, 25));

        mapPanel = new MapPanel();
        JScrollPane mapScroll = new JScrollPane(mapPanel);
        mapScroll.getViewport().setBackground(new Color(15, 15, 25));

        // Info panel (tile details)
        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(20, 20, 35));
        infoPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane infoScroll = new JScrollPane(infoPanel);
        infoScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        infoScroll.getViewport().setBackground(new Color(20, 20, 35));
        infoScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 80)),
                "Tile Info", 0, 0, new Font("Monospaced", Font.BOLD, 16), new Color(100, 120, 200)));

        // NPC tracker panel
        trackerPanel = new JPanel();
        trackerPanel.setLayout(new BoxLayout(trackerPanel, BoxLayout.Y_AXIS));
        trackerPanel.setBackground(new Color(15, 20, 30));
        trackerPanel.setBorder(new EmptyBorder(4, 8, 4, 8));

        JScrollPane trackerScroll = new JScrollPane(trackerPanel);
        trackerScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        trackerScroll.getViewport().setBackground(new Color(15, 20, 30));
        trackerScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(40, 80, 60)),
                "NPC Tracker", 0, 0, new Font("Monospaced", Font.BOLD, 16), new Color(80, 180, 100)));

        // Info + tracker: vertically resizable split
        JSplitPane infoTrackerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, infoScroll, trackerScroll);
        infoTrackerSplit.setResizeWeight(0.6);
        infoTrackerSplit.setContinuousLayout(true);
        infoTrackerSplit.setDividerSize(5);
        infoTrackerSplit.setBackground(new Color(20, 20, 35));

        JPanel buttonPanel = buildButtonPanel();
        JPanel legend = buildLegend();

        JPanel rightBottom = new JPanel(new BorderLayout(0, 4));
        rightBottom.setBackground(new Color(20, 20, 35));
        rightBottom.add(buttonPanel, BorderLayout.NORTH);
        rightBottom.add(legend, BorderLayout.SOUTH);

        JPanel right = new JPanel(new BorderLayout(0, 4));
        right.setBackground(new Color(20, 20, 35));
        right.setPreferredSize(new Dimension(300, 0));
        right.add(infoTrackerSplit, BorderLayout.CENTER);
        right.add(rightBottom, BorderLayout.SOUTH);

        // Event log (tabbed: Events + World History)
        logArea = new JTextArea(5, 70);
        logArea.setEditable(false);
        logArea.setBackground(new Color(12, 12, 22));
        logArea.setForeground(new Color(160, 200, 140));
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(null);

        historyArea = new JTextArea(5, 70);
        historyArea.setEditable(false);
        historyArea.setBackground(new Color(12, 18, 12));
        historyArea.setForeground(new Color(200, 220, 160));
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        historyArea.setLineWrap(true);
        historyArea.setWrapStyleWord(true);
        JScrollPane historyScroll = new JScrollPane(historyArea);
        historyScroll.setBorder(null);

        JTabbedPane logTabs = new JTabbedPane(JTabbedPane.BOTTOM);
        logTabs.setBackground(new Color(15, 15, 25));
        logTabs.setForeground(new Color(180, 180, 140));
        logTabs.setFont(new Font("Monospaced", Font.PLAIN, 16));
        logTabs.addTab("Events", logScroll);
        logTabs.addTab("World History", historyScroll);

        // Speed controls panel (top)
        JPanel speedPanel = buildSpeedPanel();

        // Horizontal split: map | right panel
        JSplitPane horizSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mapScroll, right);
        horizSplit.setResizeWeight(1.0);
        horizSplit.setContinuousLayout(true);
        horizSplit.setDividerSize(5);

        // Vertical split: main content | event log
        JSplitPane vertSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, horizSplit, logTabs);
        vertSplit.setResizeWeight(0.72);
        vertSplit.setContinuousLayout(true);
        vertSplit.setDividerSize(6);

        setLayout(new BorderLayout(4, 4));
        add(speedPanel, BorderLayout.NORTH);
        add(vertSplit, BorderLayout.CENTER);

        mapPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int ts = tileSize();
                int tx = e.getX() / ts;
                int ty = e.getY() / ts;
                if (tx >= 0 && tx < Map.range && ty >= 0 && ty < Map.range) {
                    selX = tx;
                    selY = ty;
                    SwingUtilities.invokeLater(() -> {
                        mapPanel.repaint();
                        updateInfo(Map.tiles[tx][ty]);
                    });
                    // Click-to-move: queue coordinate navigation for the player
                    if (GameDriver.guiEnabled) {
                        Character player = findPlayer();
                        if (player != null && (tx != player.location.x || ty != player.location.y)) {
                            System.out.println("[GUI] click-move → " + tx + "," + ty);
                            GameDriver.guiInputQueue.add("o");
                            GameDriver.guiInputQueue.add("c");
                            GameDriver.guiInputQueue.add(String.valueOf(tx));
                            GameDriver.guiInputQueue.add(String.valueOf(ty));
                            GameDriver.guiInputQueue.add("999");
                        }
                    }
                }
            }
        });

        // Size to 85% of screen, let layouts breathe
        java.awt.Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int)(screen.width * 0.85), (int)(screen.height * 0.85));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private Character findPlayer() {
        for (Character c : Map.characters) {
            if (c != null && c.player == 1 && !c.dead) return c;
        }
        return null;
    }

    private void queueInput(String s) {
        if (GameDriver.guiEnabled) {
            System.out.println("[GUI] queued: " + s);
            GameDriver.guiInputQueue.add(s);
        }
    }

    private JButton makeBtn(String label, Runnable action) {
        JButton btn = new JButton(label);
        btn.setBackground(new Color(35, 35, 55));
        btn.setForeground(new Color(200, 190, 160));
        btn.setFont(new Font("Monospaced", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(2, 4, 2, 4));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private JPanel buildSpeedPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));
        p.setBackground(new Color(15, 15, 30));
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 80)));

        JLabel lbl = new JLabel("Speed:");
        lbl.setForeground(new Color(160, 160, 200));
        lbl.setFont(new Font("Monospaced", Font.BOLD, 17));
        p.add(lbl);

        JButton pauseBtn = makeBtn("⏸ Pause", () -> {
            GameDriver.paused = !GameDriver.paused;
        });
        pauseBtn.setForeground(new Color(255, 180, 80));
        p.add(pauseBtn);

        p.add(makeBtn("0ms",  () -> { GameDriver.paused = false; GameDriver.tickDelayMs = 0; }));
        p.add(makeBtn("0.1s", () -> { GameDriver.paused = false; GameDriver.tickDelayMs = 100; }));
        p.add(makeBtn("0.5s", () -> { GameDriver.paused = false; GameDriver.tickDelayMs = 500; }));
        p.add(makeBtn("2s",   () -> { GameDriver.paused = false; GameDriver.tickDelayMs = 2000; }));
        p.add(makeBtn("10s",  () -> { GameDriver.paused = false; GameDriver.tickDelayMs = 10000; }));

        JLabel statusLbl = new JLabel("  ");
        statusLbl.setFont(new Font("Monospaced", Font.BOLD, 16));
        p.add(statusLbl);

        // Update status label in a timer
        new javax.swing.Timer(300, e2 -> {
            if (GameDriver.paused) {
                statusLbl.setForeground(new Color(255, 180, 60));
                statusLbl.setText("  ⏸ PAUSED");
            } else {
                statusLbl.setForeground(new Color(80, 220, 80));
                statusLbl.setText("  ▶ Simulation running — no input needed");
            }
        }).start();

        return p;
    }

    private JPanel buildButtonPanel() {
        JPanel p = new JPanel();
        p.setBackground(new Color(20, 20, 35));
        p.setBorder(new EmptyBorder(6, 6, 6, 6));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JPanel row1 = row(new Color(20, 20, 35));
        row1.add(makeBtn("   ↑ North   ", () -> queueInput("w")));

        JPanel row2 = row(new Color(20, 20, 35));
        row2.add(makeBtn("← W", () -> queueInput("a")));
        row2.add(makeBtn("Wait", () -> queueInput("x")));
        row2.add(makeBtn("E →", () -> queueInput("d")));

        JPanel row3 = row(new Color(20, 20, 35));
        row3.add(makeBtn("   ↓ South   ", () -> queueInput("s")));

        JPanel row4 = row(new Color(20, 20, 35));
        row4.add(makeBtn("Stats", () -> queueInput("i")));
        row4.add(makeBtn("Locations", () -> queueInput("l")));

        JPanel row5 = row(new Color(20, 20, 35));
        row5.add(makeBtn("Relations", () -> queueInput("r")));
        row5.add(makeBtn("Quests", () -> queueInput("q")));

        JPanel row6 = row(new Color(20, 20, 35));
        row6.add(makeBtn("Edit Self", () -> queueInput("e")));
        row6.add(makeBtn("Cheats", () -> queueInput("z")));

        JPanel row7 = row(new Color(20, 20, 35));
        JButton saveBtn = makeBtn("💾 Save", () -> {
            new Thread(GameDriver::save).start();
        });
        saveBtn.setForeground(new Color(100, 220, 100));
        row7.add(saveBtn);

        JButton moveHere = makeBtn("▶ Move to Selected Tile", () -> {
            if (selX >= 0 && selY >= 0) {
                Character pl = findPlayer();
                if (pl != null) {
                    queueInput("o"); queueInput("c");
                    queueInput(String.valueOf(selX)); queueInput(String.valueOf(selY));
                    queueInput("999");
                }
            }
        });
        moveHere.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(row1); p.add(row2); p.add(row3);
        p.add(Box.createVerticalStrut(3));
        p.add(row4); p.add(row5); p.add(row6);
        p.add(Box.createVerticalStrut(3));
        p.add(row7);
        p.add(Box.createVerticalStrut(5));
        p.add(moveHere);
        return p;
    }

    private JPanel row(Color bg) {
        JPanel r = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 1));
        r.setBackground(bg);
        return r;
    }

    /** Called at the end of each simulation tick from GameDriver. */
    public void refresh() {
        SwingUtilities.invokeLater(() -> {
            mapPanel.repaint();
            if (selX >= 0 && selY >= 0 && Map.tiles[selX][selY] != null) {
                updateInfo(Map.tiles[selX][selY]);
            }
            refreshLog();
            updateTracker();
        });
    }

    private void refreshLog() {
        List<String> evSnap;
        synchronized (GameDriver.eventLog) { evSnap = new ArrayList<>(GameDriver.eventLog); }
        StringBuilder sb = new StringBuilder();
        for (String line : evSnap) sb.append(line).append("\n");
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
        Color gold = new Color(255, 210, 80);
        Color fg   = new Color(200, 200, 170);
        Color dim  = new Color(120, 130, 110);
        Color red  = new Color(220, 100, 80);

        if (Map.trackedCharacters.isEmpty()) {
            JLabel empty = new JLabel("(none tracked — click a cultivator name to track)");
            empty.setForeground(dim);
            empty.setFont(new Font("Monospaced", Font.ITALIC, 16));
            trackerPanel.add(empty);
        } else {
            for (int i = 0; i < Map.trackedCharacters.size(); i++) {
                int cid = Map.trackedCharacters.get(i);
                if (cid < 0 || cid >= Map.characters.size()) continue;
                Character c = Map.characters.get(cid);
                if (c == null || c.dead) {
                    JLabel dead = new JLabel("† ID:" + cid + " (deceased)");
                    dead.setForeground(dim);
                    dead.setFont(new Font("Monospaced", Font.PLAIN, 16));
                    trackerPanel.add(dead);
                    continue;
                }
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
                row.setBackground(new Color(15, 20, 30));
                row.setMaximumSize(new Dimension(260, 20));

                JButton nameBtn = new JButton(c.name + " " + c.lastName);
                nameBtn.setFont(new Font("Monospaced", Font.BOLD, 16));
                nameBtn.setForeground(c.player == 1 ? gold : fg);
                nameBtn.setBackground(new Color(20, 30, 40));
                nameBtn.setBorderPainted(false);
                nameBtn.setFocusPainted(false);
                nameBtn.setMargin(new Insets(0, 2, 0, 2));
                final Character tracked = c;
                nameBtn.addActionListener(e -> showCharacterDialog(tracked));

                JLabel info = new JLabel(" Lv." + c.lvl + " Age" + c.age
                        + " [" + (c.location != null ? c.location.x + "," + c.location.y : "?") + "]");
                info.setForeground(dim);
                info.setFont(new Font("Monospaced", Font.PLAIN, 16));

                final int removeCid = cid;
                JButton removeBtn = new JButton("×");
                removeBtn.setFont(new Font("Monospaced", Font.BOLD, 16));
                removeBtn.setForeground(red);
                removeBtn.setBackground(new Color(30, 20, 20));
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
        }
        trackerPanel.revalidate();
        trackerPanel.repaint();
    }

    /** Shows a detailed character dialog with Track and Control buttons. */
    private void showCharacterDialog(Character c) {
        JDialog dialog = new JDialog(this, c.name + " " + c.lastName, false);
        dialog.setBackground(new Color(15, 15, 25));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(20, 20, 35));
        panel.setBorder(new EmptyBorder(10, 14, 10, 14));

        Color gold   = new Color(255, 210, 80);
        Color fg     = new Color(220, 210, 180);
        Color dim    = new Color(140, 130, 110);
        Color cyan   = new Color(80, 210, 210);
        Color purple = new Color(190, 130, 255);

        Runnable addLbl = null; // placeholder — use helper inline
        java.util.function.BiConsumer<String, Color> dlgLabel = (text, color) -> {
            JLabel lbl = new JLabel(text);
            lbl.setForeground(color);
            lbl.setFont(new Font("Monospaced", Font.PLAIN, 17));
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(lbl);
        };

        dlgLabel.accept(c.name + " " + c.lastName + (c.repTitle != null ? " '" + c.repTitle + "'" : ""), gold);
        dlgLabel.accept("Realm: Major " + c.majorLvl + "  Minor " + c.minorLvl + "  Lv." + c.lvl, fg);
        dlgLabel.accept("Cultivation: " + (int)c.cultivation + " / " + (int)c.cultivationBaseMax, dim);
        dlgLabel.accept("Age: " + c.age + " / " + c.maxAge + "  Potential: " + c.potential, fg);
        dlgLabel.accept("Trait: " + c.getTrait() + "  Affiliation: " + c.affiliation + "  Gender: " + c.gender, fg);
        dlgLabel.accept("Profession Lv." + c.professionLvl, dim);
        dlgLabel.accept("Wealth: " + c.ss + " SS  " + c.sc + " SC  " + c.dc + " DC", fg);
        dlgLabel.accept("Weapon: " + (c.equippedWeapon != null ? c.equippedWeapon.toString() : "None"), fg);
        dlgLabel.accept("Armor:  " + (c.equippedClothing != null ? c.equippedClothing.toString() : "None"), fg);
        dlgLabel.accept("Kills: " + c.kills + "  Items crafted: " + c.itemsCrafted, dim);
        if (c.sect != null) dlgLabel.accept("Sect: " + c.sect.name + " [" + c.sect.getRank(c) + "]", purple);
        if (c.spouse != null) dlgLabel.accept("Spouse: " + c.spouse.name + " " + c.spouse.lastName, new Color(255, 160, 200));
        dlgLabel.accept("Location: " + (c.location != null ? c.location.name + " [" + c.location.x + "," + c.location.y + "]" : "?"), dim);
        dlgLabel.accept("ID: " + c.id + "  Player: " + c.player, dim);

        panel.add(Box.createVerticalStrut(8));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        btnRow.setBackground(new Color(20, 20, 35));
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton trackBtn = makeBtn(Map.trackedCharacters.contains(c.id) ? "✓ Tracked" : "Track", () -> {
            if (!Map.trackedCharacters.contains(c.id)) Map.trackedCharacters.add(c.id);
            updateTracker();
            dialog.dispose();
        });

        JButton controlBtn = makeBtn("Control", () -> {
            // Set this NPC as the player; demote previous player to follow mode
            for (Character pc : Map.characters) {
                if (pc != null && pc.player == 1 && pc != c) pc.player = 2;
            }
            c.player = 1;
            System.out.println("[GUI] Player now controls: " + c.name + " " + c.lastName);
            dialog.dispose();
        });
        controlBtn.setForeground(new Color(255, 220, 80));

        JButton closeBtn = makeBtn("Close", dialog::dispose);

        btnRow.add(trackBtn);
        btnRow.add(controlBtn);
        btnRow.add(closeBtn);
        panel.add(btnRow);

        JScrollPane scroll = new JScrollPane(panel);
        scroll.getViewport().setBackground(new Color(20, 20, 35));
        scroll.setBorder(null);
        dialog.add(scroll);
        dialog.setSize(340, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void updateInfo(Area area) {
        infoPanel.removeAll();
        Color gold   = new Color(255, 210, 80);
        Color purple = new Color(190, 130, 255);
        Color fg     = new Color(220, 210, 180);
        Color dim    = new Color(140, 130, 110);
        Color red    = new Color(220, 100, 80);
        Color cyan   = new Color(80, 210, 210);

        String[] SEASON_NAMES = {"Spring", "Summer", "Autumn", "Winter"};
        addLabel("Season: " + SEASON_NAMES[GameDriver.currentSeason], dim);
        Character pl = findPlayer();
        if (pl != null && pl.location != null)
            addLabel("Player: [" + pl.location.x + "," + pl.location.y + "] Lv." + pl.lvl, new Color(255, 255, 100));
        addSep();

        if (area == null) {
            addLabel("(Empty tile)", dim);
        } else {
            addLabel(area.name, gold);
            String lvlLine = "Level " + area.lvl + (area.hotspot ? "  ★ Hotspot" : "");
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
                if (area.sect.master != null)
                    addLabel("  Master: " + area.sect.master.name + " " + area.sect.master.lastName + " Lv." + area.sect.master.lvl, dim);
                if (area.sect.territoryBonus > 0)
                    addLabel("  Territory Bonus: +" + area.sect.territoryBonus, cyan);
            }
            if (area.dungeonType != null)
                addLabel("Dungeon: " + area.dungeonType.name().replace('_', ' '),
                    area.dungeonType == Area.DungeonType.CELESTIAL_TRIAL ? cyan : red);
            if (area.celestialTrialTimer > 0)
                addLabel("  Closes in: " + area.celestialTrialTimer + " ticks", dim);
            if (area.heavenlyTechnique != null)
                addLabel("Heaven Treasure: " + area.heavenlyTechnique.name, gold);
            if (area.arrayBag != null && !area.arrayBag.isEmpty())
                addLabel("Arrays: " + area.arrayBag.size(), new Color(100, 200, 255));
            addSep();

            try {
                long cultivatorCount = 0;
                int topLvl = 0;
                for (Character c : area.characters) {
                    if (c != null && !c.dead) { cultivatorCount++; if (c.lvl > topLvl) topLvl = c.lvl; }
                }
                addLabel("Cultivators: " + cultivatorCount + (topLvl > 0 ? "  (top Lv." + topLvl + ")" : ""), fg);
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
                        btn.setFont(new Font("Monospaced", Font.PLAIN, 17));
                        btn.setForeground(tc);
                        btn.setBackground(new Color(25, 25, 40));
                        btn.setBorderPainted(false);
                        btn.setFocusPainted(false);
                        btn.setHorizontalAlignment(SwingConstants.LEFT);
                        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
                        btn.setMaximumSize(new Dimension(260, 18));
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
                    if (p2 != null) {
                        queueInput("o"); queueInput("c");
                        queueInput(String.valueOf(tx)); queueInput(String.valueOf(ty));
                        queueInput("999");
                    }
                });
                moveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                infoPanel.add(moveBtn);
            }
        }
        infoPanel.revalidate();
        infoPanel.repaint();
    }

    private JPanel buildLegend() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(20, 20, 35));
        p.setBorder(new EmptyBorder(4, 8, 6, 8));
        addLegendRow(p, new Color(200, 160, 20), "City");
        addLegendRow(p, new Color(120, 40, 180),  "Sect");
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
        r.setBackground(new Color(20, 20, 35));
        JLabel dot = new JLabel("■");
        dot.setForeground(swatch);
        dot.setFont(new Font("Monospaced", Font.PLAIN, 16));
        JLabel txt = new JLabel(label);
        txt.setForeground(new Color(155, 150, 135));
        txt.setFont(new Font("Monospaced", Font.PLAIN, 16));
        r.add(dot); r.add(txt);
        p.add(r);
    }

    private void addLabel(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(color);
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 17));
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

    private class MapPanel extends JPanel {
        MapPanel() {
            setBackground(new Color(15, 15, 25));
            addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override public void componentResized(java.awt.event.ComponentEvent e) { repaint(); }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            int ts = tileSize();
            return new Dimension(Map.range * ts, Map.range * ts);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int ts = tileSize();
            int playerX = -1, playerY = -1;
            Character pl = findPlayer();
            if (pl != null && pl.location != null) { playerX = pl.location.x; playerY = pl.location.y; }

            for (int x = 0; x < Map.range; x++) {
                for (int y = 0; y < Map.range; y++) {
                    Area area = Map.tiles[x][y];
                    g.setColor(tileColor(area));
                    g.fillRect(x * ts, y * ts, ts - 1, ts - 1);
                    if (x == selX && y == selY) {
                        g.setColor(Color.WHITE);
                        g.drawRect(x * ts, y * ts, ts - 2, ts - 2);
                    }
                    if (x == playerX && y == playerY) {
                        g.setColor(new Color(255, 255, 100));
                        int cx = x * ts + ts / 2, cy = y * ts + ts / 2, r = Math.max(2, ts / 4);
                        g.fillOval(cx - r, cy - r, r * 2, r * 2);
                    }
                    // Tracked NPC dots (cyan)
                    for (int tid : Map.trackedCharacters) {
                        if (tid < 0 || tid >= Map.characters.size()) continue;
                        Character tc = Map.characters.get(tid);
                        if (tc != null && !tc.dead && tc.location != null
                                && tc.location.x == x && tc.location.y == y
                                && !(x == playerX && y == playerY)) {
                            g.setColor(new Color(0, 220, 220));
                            int cx2 = x * ts + ts / 2, cy2 = y * ts + ts / 2, r2 = Math.max(2, ts / 5);
                            g.fillOval(cx2 - r2, cy2 - r2, r2 * 2, r2 * 2);
                            break;
                        }
                    }
                }
            }
        }

        private Color tileColor(Area area) {
            if (area == null) return new Color(15, 15, 25);
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
                switch (area.biomeM) {
                    case "Forest":   base = new Color(20, Math.max(35, 110 - area.lvl), 15); break;
                    case "Mountain": base = new Color(Math.max(40, 90 - area.lvl/2), Math.max(5, 85 - area.lvl/3), Math.max(5, 80 - area.lvl/3)); break;
                    case "Plains":   base = new Color(Math.max(30, 90 - area.lvl), Math.max(50, 115 - area.lvl), 20); break;
                    case "Desert":   base = new Color(Math.max(80, 160 - area.lvl), Math.max(50, 110 - area.lvl), 20); break;
                    case "Swamp":    base = new Color(20, Math.max(40, 80 - area.lvl), 40); break;
                    case "Tundra":   base = new Color(Math.max(60, 140 - area.lvl), Math.max(60, 140 - area.lvl), Math.max(80, 160 - area.lvl)); break;
                    default:         base = new Color(20, Math.max(35, 80 - area.lvl), 15);
                }
            }
            if (area.hotspot) base = brighten(base, 55);
            try {
                int pop = 0;
                for (Character c : area.characters) { if (c != null && !c.dead) pop++; }
                if (pop > 0) base = brighten(base, Math.min(45, pop * 3));
            } catch (Exception ignored) {}
            return base;
        }

        private Color brighten(Color c, int amt) {
            return new Color(
                Math.min(255, c.getRed()   + amt),
                Math.min(255, c.getGreen() + amt),
                Math.min(255, c.getBlue()  + amt));
        }
    }
}
