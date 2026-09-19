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
    private final JTextArea logArea;
    private volatile int selX = -1, selY = -1;

    private int tileSize() {
        return Math.max(8, Math.min(24, 560 / Map.range));
    }

    public MapViewer() {
        super("Tales of Dream and Fantasy 2 — World Map");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setBackground(new Color(15, 15, 25));

        mapPanel = new MapPanel();
        JScrollPane mapScroll = new JScrollPane(mapPanel);
        int ms = tileSize();
        mapScroll.setPreferredSize(new Dimension(Map.range * ms + 24, Map.range * ms + 24));
        mapScroll.getViewport().setBackground(new Color(15, 15, 25));

        // Info panel
        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(20, 20, 35));
        infoPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane infoScroll = new JScrollPane(infoPanel);
        infoScroll.setPreferredSize(new Dimension(260, 280));
        infoScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        infoScroll.getViewport().setBackground(new Color(20, 20, 35));
        infoScroll.setBorder(null);

        JPanel buttonPanel = buildButtonPanel();
        JPanel legend = buildLegend();

        JPanel rightBottom = new JPanel(new BorderLayout(0, 4));
        rightBottom.setBackground(new Color(20, 20, 35));
        rightBottom.add(buttonPanel, BorderLayout.NORTH);
        rightBottom.add(legend, BorderLayout.SOUTH);

        JPanel right = new JPanel(new BorderLayout(0, 4));
        right.setBackground(new Color(20, 20, 35));
        right.add(infoScroll, BorderLayout.CENTER);
        right.add(rightBottom, BorderLayout.SOUTH);

        // Event log
        logArea = new JTextArea(6, 70);
        logArea.setEditable(false);
        logArea.setBackground(new Color(12, 12, 22));
        logArea.setForeground(new Color(160, 200, 140));
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(50, 70, 50)),
            "Event Log", 0, 0,
            new Font("Monospaced", Font.BOLD, 10), new Color(100, 160, 80)));
        logScroll.setPreferredSize(new Dimension(0, 120));

        setLayout(new BorderLayout(4, 4));
        add(mapScroll, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
        add(logScroll, BorderLayout.SOUTH);

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

        pack();
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
        if (GameDriver.guiEnabled) GameDriver.guiInputQueue.add(s);
    }

    private JButton makeBtn(String label, Runnable action) {
        JButton btn = new JButton(label);
        btn.setBackground(new Color(35, 35, 55));
        btn.setForeground(new Color(200, 190, 160));
        btn.setFont(new Font("Monospaced", Font.BOLD, 10));
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(2, 4, 2, 4));
        btn.addActionListener(e -> action.run());
        return btn;
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
        row5.add(makeBtn("Options", () -> queueInput("o")));

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
        p.add(Box.createVerticalStrut(5));
        p.add(row4); p.add(row5);
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
        });
    }

    private void refreshLog() {
        List<String> snapshot;
        synchronized (GameDriver.eventLog) {
            snapshot = new ArrayList<>(GameDriver.eventLog);
        }
        StringBuilder sb = new StringBuilder();
        for (String line : snapshot) sb.append(line).append("\n");
        String text = sb.toString();
        if (!text.equals(logArea.getText())) {
            logArea.setText(text);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
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
        // Show player location
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

            addLabel("Top cultivators:", dim);
            try {
                area.characters.stream()
                    .filter(c -> c != null && !c.dead)
                    .sorted((a, b) -> Integer.compare(b.lvl, a.lvl))
                    .limit(5)
                    .forEach(c -> {
                        String trait = c.getTrait();
                        Color tc = c.trait == Character.Traits.Monster ? red : fg;
                        String title = c.repTitle != null ? " '" + c.repTitle + "'" : "";
                        addLabel("  " + c.name + " " + c.lastName + title + " Lv." + c.lvl
                                + " [" + trait.substring(0, Math.min(4, trait.length())) + "]", tc);
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
        dot.setFont(new Font("Monospaced", Font.PLAIN, 10));
        JLabel txt = new JLabel(label);
        txt.setForeground(new Color(155, 150, 135));
        txt.setFont(new Font("Monospaced", Font.PLAIN, 10));
        r.add(dot); r.add(txt);
        p.add(r);
    }

    private void addLabel(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(color);
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 11));
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
        MapPanel() { setBackground(new Color(15, 15, 25)); }

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
