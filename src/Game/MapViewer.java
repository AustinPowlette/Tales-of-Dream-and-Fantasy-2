package Game;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class MapViewer extends JFrame {

    private static final int TILE = 18;

    private final MapPanel mapPanel;
    private final JPanel infoPanel;
    private volatile int selX = -1, selY = -1;

    public MapViewer() {
        super("Tales of Dream and Fantasy 2 — World Map");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setBackground(new Color(15, 15, 25));

        mapPanel = new MapPanel();
        JScrollPane scroll = new JScrollPane(mapPanel);
        scroll.setPreferredSize(new Dimension(Map.range * TILE + 4, Map.range * TILE + 4));
        scroll.getViewport().setBackground(new Color(15, 15, 25));

        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setPreferredSize(new Dimension(240, Map.range * TILE));
        infoPanel.setBackground(new Color(20, 20, 35));
        infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel legend = buildLegend();

        JPanel right = new JPanel(new BorderLayout(0, 6));
        right.setBackground(new Color(20, 20, 35));
        right.add(infoPanel, BorderLayout.CENTER);
        right.add(legend, BorderLayout.SOUTH);

        setLayout(new BorderLayout(4, 4));
        add(scroll, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);

        mapPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int tx = e.getX() / TILE;
                int ty = e.getY() / TILE;
                if (tx >= 0 && tx < Map.range && ty >= 0 && ty < Map.range) {
                    selX = tx;
                    selY = ty;
                    SwingUtilities.invokeLater(() -> {
                        mapPanel.repaint();
                        updateInfo(Map.tiles[tx][ty]);
                    });
                }
            }
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /** Called at the end of each simulation tick from GameDriver. */
    public void refresh() {
        SwingUtilities.invokeLater(() -> {
            mapPanel.repaint();
            if (selX >= 0 && selY >= 0 && Map.tiles[selX][selY] != null) {
                updateInfo(Map.tiles[selX][selY]);
            }
        });
    }

    private void updateInfo(Area area) {
        infoPanel.removeAll();
        Color gold   = new Color(255, 210, 80);
        Color purple = new Color(190, 130, 255);
        Color fg     = new Color(220, 210, 180);
        Color dim    = new Color(140, 130, 110);
        Color red    = new Color(220, 100, 80);

        if (area == null) {
            addLabel("(Empty tile)", dim);
        } else {
            addLabel(area.name, gold);
            addLabel("Level " + area.lvl + (area.hotspot ? "  ★ Hotspot" : ""), fg);
            addLabel("Tile [" + area.x + ", " + area.y + "]  Biome: " + area.biomeM, dim);
            addSep();

            if (area.city) {
                String owner = area.cityOwner != null ? area.cityOwner.name + " " + area.cityOwner.lastName : "None";
                addLabel("City  Owner: " + owner, gold);
            }
            if (area.sect != null) {
                addLabel("Sect: " + area.sect.name, purple);
                if (area.sect.master != null)
                    addLabel("  Master: " + area.sect.master.name + " " + area.sect.master.lastName + " Lv." + area.sect.master.lvl, dim);
            }
            if (area.dungeonType != null)
                addLabel("Dungeon: " + area.dungeonType.name().replace('_', ' '), red);
            if (area.arrayBag != null && !area.arrayBag.isEmpty())
                addLabel("Arrays: " + area.arrayBag.size(), new Color(100, 200, 255));
            addSep();

            try {
                long cultivatorCount = 0;
                int topLvl = 0;
                for (Character c : area.characters) {
                    if (c != null && !c.dead) {
                        cultivatorCount++;
                        if (c.lvl > topLvl) topLvl = c.lvl;
                    }
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
                        addLabel("  " + c.name + " " + c.lastName + " Lv." + c.lvl
                                + " [" + trait.substring(0, Math.min(4, trait.length())) + "]", tc);
                    });
            } catch (Exception ignored) {}

            // ── Future interactive actions panel goes here ──
            // addSep();
            // JButton moveBtn = new JButton("Move Here");
            // moveBtn.addActionListener(e -> { /* queue player move to (selX, selY) */ });
            // infoPanel.add(moveBtn);
        }

        infoPanel.revalidate();
        infoPanel.repaint();
    }

    private JPanel buildLegend() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(20, 20, 35));
        p.setBorder(new EmptyBorder(6, 10, 6, 10));
        addLegendRow(p, new Color(200, 160, 20), "City");
        addLegendRow(p, new Color(120, 40, 180),  "Sect");
        addLegendRow(p, new Color(120, 90, 20),   "Lost Ruins");
        addLegendRow(p, new Color(160, 50, 10),   "Beast Lair");
        addLegendRow(p, new Color(40, 80, 150),   "Martial Trial");
        addLegendRow(p, new Color(60, 10, 90),    "Haunted Grounds");
        addLegendRow(p, new Color(60, 100, 20),   "Wildlands (low lvl)");
        addLegendRow(p, new Color(20, 40, 10),    "Wildlands (high lvl)");
        return p;
    }

    private void addLegendRow(JPanel p, Color swatch, String label) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        row.setBackground(new Color(20, 20, 35));
        JLabel dot = new JLabel("■");
        dot.setForeground(swatch);
        dot.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JLabel txt = new JLabel(label);
        txt.setForeground(new Color(160, 155, 140));
        txt.setFont(new Font("Monospaced", Font.PLAIN, 10));
        row.add(dot);
        row.add(txt);
        p.add(row);
    }

    private void addLabel(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(color);
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 11));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(lbl);
    }

    private void addSep() {
        infoPanel.add(Box.createVerticalStrut(4));
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(50, 50, 70));
        sep.setMaximumSize(new Dimension(240, 1));
        infoPanel.add(sep);
        infoPanel.add(Box.createVerticalStrut(4));
    }

    // ── Tile rendering ────────────────────────────────────────────────────────

    private class MapPanel extends JPanel {
        MapPanel() {
            setPreferredSize(new Dimension(Map.range * TILE, Map.range * TILE));
            setBackground(new Color(15, 15, 25));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int x = 0; x < Map.range; x++) {
                for (int y = 0; y < Map.range; y++) {
                    Area area = Map.tiles[x][y];
                    g.setColor(tileColor(area));
                    g.fillRect(x * TILE, y * TILE, TILE - 1, TILE - 1);
                    if (x == selX && y == selY) {
                        g.setColor(Color.WHITE);
                        g.drawRect(x * TILE, y * TILE, TILE - 2, TILE - 2);
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
            } else if (area.dungeonType != null) {
                switch (area.dungeonType) {
                    case LOST_RUINS:      base = new Color(120, 90, 20);  break;
                    case BEAST_LAIR:      base = new Color(160, 50, 10);  break;
                    case MARTIAL_TRIAL:   base = new Color(40, 80, 150);  break;
                    case HAUNTED_GROUNDS: base = new Color(60, 10, 90);   break;
                    default:              base = new Color(60, 60, 60);
                }
            } else {
                // Biome-based color, darkened by level
                switch (area.biomeM) {
                    case "Forest":   base = new Color(20, Math.max(35, 110 - area.lvl), 15); break;
                    case "Mountain": base = new Color(Math.max(40, 90 - area.lvl / 2), 85 - area.lvl / 3, 80 - area.lvl / 3); break;
                    case "Plains":   base = new Color(Math.max(30, 90 - area.lvl), Math.max(50, 115 - area.lvl), 20); break;
                    case "Desert":   base = new Color(Math.max(80, 160 - area.lvl), Math.max(50, 110 - area.lvl), 20); break;
                    case "Swamp":    base = new Color(20, Math.max(40, 80 - area.lvl), 40);  break;
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
                Math.min(255, c.getBlue()  + amt)
            );
        }
    }
}
