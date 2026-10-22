package Game;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** The Concerts window: two tabs. "Playing nearby" lists the performances going on within 25 tiles (the musician, the piece, the instrument, the place, the distance); the button walks there (`goto`) or listens when
 *  the performance is in your tile (`listen`: it costs the week, warms the friendship once per musician and leaves a calm that lasts four weeks). "The pieces" lists every piece of the musicians' repertoire with the
 *  occasion it belongs to, whether you have heard it, and a Replay button for the ones you have heard. Everything here is a command in the queue (nothing writes the world from the window). */
final class GuiConcerts {
    private GuiConcerts() { }

    static String occasionName(String o) {
        if (o == null || o.isEmpty()) return "any";
        return o.substring(0, 1).toUpperCase() + o.substring(1);
    }

    static void open(final MapViewer mv) {
        GuiWindows.window(mv, "concerts", "Concerts", 940, 600, 1500, (d, refreshers) -> {
            final Color BG = GuiWindows.BG, FG = GuiWindows.FG;
            JPanel root = new JPanel(new BorderLayout(6, 6));
            root.setBackground(BG);
            root.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

            final JLabel summary = GuiWindows.label(" ");
            summary.setForeground(Theme.ACCENT);
            final boolean[] setting = {false};
            final JCheckBox notices = new JCheckBox("Tell me when someone starts to play in my tile", Settings.concertNotices);
            notices.setBackground(BG); notices.setForeground(FG);
            notices.addActionListener(e -> { if (setting[0]) return; Settings.concertNotices = notices.isSelected(); Settings.save(); });
            JPanel top = new JPanel(new GridLayout(0, 1, 0, 0)); top.setBackground(BG);
            top.add(summary); top.add(notices);

            // ---- playing nearby
            final DefaultTableModel nm = GuiWindows.model();
            nm.setColumnIdentifiers(new String[]{"Piece", "Musician", "Instrument", "Grade", "Where", "Tiles away", "Weeks left", "You"});
            final JTable nt = GuiWindows.table(nm);
            nt.getColumnModel().getColumn(0).setPreferredWidth(200);
            nt.getColumnModel().getColumn(1).setPreferredWidth(150);
            nt.getColumnModel().getColumn(2).setPreferredWidth(130);
            nt.getColumnModel().getColumn(3).setMaxWidth(50);
            nt.getColumnModel().getColumn(5).setMaxWidth(80);
            nt.getColumnModel().getColumn(6).setMaxWidth(80);
            final List<Concerts.Show> shows = new ArrayList<Concerts.Show>();
            final JLabel nstatus = GuiWindows.label(" ");
            final Runnable go = () -> {
                int r = nt.getSelectedRow();
                if (r < 0 && shows.size() == 1) r = 0;
                if (r < 0 || r >= shows.size()) { nstatus.setText("Select a performance first."); return; }
                Concerts.Show s = shows.get(r);
                if (s.distance == 0) mv.queueInput("listen");
                else mv.queueInput("goto " + s.at.x + " " + s.at.y);
            };
            nt.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) go.run(); }
            });
            JPanel nb = GuiWindows.flow(FlowLayout.LEFT);
            nb.add(mv.makeBtn("Listen / walk there", "Listen to the selected performance when it is in your tile (it takes the week), or walk to it. Double-click a row does the same.", go));
            nb.add(mv.makeBtn("Perform myself", "Give a concert of your own with your best instrument.", () -> mv.queueInput("perform")));
            nb.add(nstatus);
            JPanel nearby = new JPanel(new BorderLayout(4, 4)); nearby.setBackground(BG);
            nearby.add(new JScrollPane(nt), BorderLayout.CENTER); nearby.add(nb, BorderLayout.SOUTH);

            // ---- the pieces
            final DefaultTableModel pm = GuiWindows.model();
            pm.setColumnIdentifiers(new String[]{"#", "Piece", "Occasion", "Length", "Kind", "Heard"});
            final JTable pt = GuiWindows.table(pm);
            pt.getColumnModel().getColumn(0).setMaxWidth(40);
            pt.getColumnModel().getColumn(1).setPreferredWidth(260);
            pt.getColumnModel().getColumn(3).setMaxWidth(70);
            pt.getColumnModel().getColumn(5).setMaxWidth(70);
            final JLabel pstatus = GuiWindows.label(" ");
            final long[] lastMask = {-1};
            final JComboBox<String> occ = new JComboBox<String>();
            occ.addItem("All occasions");
            for (String o : Concerts.OCCASIONS) occ.addItem(occasionName(o));
            occ.addItem("Masterpieces");
            final JCheckBox heardOnly = new JCheckBox("heard only");
            heardOnly.setBackground(BG); heardOnly.setForeground(FG);
            final int[] shownIdx = new int[Concerts.count() + 1];
            final int[] nShown = {0};
            final Runnable fillPieces = () -> {
                Character p = mv.findPlayer();
                long mask = p == null ? 0 : p.heardConcerts;
                int sel = pt.getSelectedRow();
                pm.setRowCount(0);
                nShown[0] = 0;
                List<Score> all = Concerts.all();
                int oi = occ.getSelectedIndex();
                String want = oi <= 0 ? null : oi > Concerts.OCCASIONS.length ? "master" : Concerts.OCCASIONS[oi - 1];
                for (int i = 0; i < all.size(); i++) {
                    String o = Concerts.occasionOf(i);
                    if (want != null && !want.equals(o)) continue;
                    boolean heard = Concerts.heardBy(p, i);
                    if (heardOnly.isSelected() && !heard) continue;
                    Score s = all.get(i);
                    int sec = (int) s.seconds();
                    shownIdx[nShown[0]++] = i;
                    pm.addRow(new Object[]{i + 1, s.title, o.equals("master") ? "any" : occasionName(o), String.format("%d:%02d", sec / 60, sec % 60), Concerts.isMaster(i) ? "masterpiece" : "piece", heard ? "yes" : "-"});
                }
                if (sel >= 0 && sel < pm.getRowCount()) pt.setRowSelectionInterval(sel, sel);
                lastMask[0] = mask;
            };
            occ.addActionListener(e -> World.read(300, fillPieces));
            heardOnly.addActionListener(e -> World.read(300, fillPieces));
            final Runnable replay = () -> {
                int r = pt.getSelectedRow();
                if (r < 0 || r >= nShown[0]) { pstatus.setText("Select a piece first."); return; }
                int i = shownIdx[r];
                Character p = mv.findPlayer();
                if (!Concerts.heardBy(p, i)) { pstatus.setText("You have not heard this one yet: find a musician who plays it (the Playing nearby tab), or play it yourself with a high grade instrument."); return; }
                pstatus.setText(" ");
                mv.queueInput("music concert " + Concerts.all().get(i).title);
            };
            pt.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) replay.run(); }
            });
            JPanel pf = GuiWindows.flow(FlowLayout.LEFT);
            pf.add(GuiWindows.label("Show")); pf.add(occ); pf.add(heardOnly);
            JPanel pb = GuiWindows.flow(FlowLayout.LEFT);
            pb.add(mv.makeBtn("Replay", "Hear a piece you have already heard again, played on your best instrument (the music must be on).", replay));
            pb.add(pstatus);
            JPanel pieces = new JPanel(new BorderLayout(4, 4)); pieces.setBackground(BG);
            pieces.add(pf, BorderLayout.NORTH); pieces.add(new JScrollPane(pt), BorderLayout.CENTER); pieces.add(pb, BorderLayout.SOUTH);

            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("Playing nearby", nearby);
            tabs.addTab("The pieces", pieces);
            root.add(top, BorderLayout.NORTH);
            root.add(tabs, BorderLayout.CENTER);
            JLabel hint = GuiWindows.label("Musicians play in cities, at the seats of sects and on the road. A masterpiece is played by a master (grade 8 or more) or on a Heaven-Sent instrument, about one concert in five.");
            hint.setForeground(Theme.DIM); hint.setFont(Theme.plain(12));
            root.add(hint, BorderLayout.SOUTH);

            refreshers.add(() -> {
                Character p = mv.findPlayer();
                setting[0] = true;
                try { notices.setSelected(Settings.concertNotices); } finally { setting[0] = false; }
                if (p == null || p.location == null) { summary.setText("No player character."); return; }
                summary.setText("You have heard " + Concerts.heardCount(p) + " of the " + Concerts.count() + " pieces.");
                shows.clear();
                shows.addAll(Concerts.near(p, 25));
                int sel = nt.getSelectedRow();
                nm.setRowCount(0);
                for (Concerts.Show s : shows)
                    nm.addRow(new Object[]{s.piece.title, s.who.name + " " + s.who.lastName, s.instName == null ? "an old erhu" : s.instName, s.grade, s.at.name + " " + PlayerControl.coord(s.at), s.distance,
                        Math.max(0, s.until - GameDriver.time), (Concerts.heardBy(p, s.index) ? "heard" : "new") + (s.distance == 0 ? ", here" : "")});
                if (sel >= 0 && sel < nm.getRowCount()) nt.setRowSelectionInterval(sel, sel);
                if (shows.isEmpty()) nstatus.setText("Nobody is playing within 25 tiles just now.");
                else if (nstatus.getText().startsWith("Nobody") || nstatus.getText().startsWith("Select")) nstatus.setText(" ");
                if (lastMask[0] != p.heardConcerts) fillPieces.run();
            });
            return root;
        });
    }
}
