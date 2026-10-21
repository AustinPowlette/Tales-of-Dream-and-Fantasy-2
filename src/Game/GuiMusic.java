package Game;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** The Music Manager: every option of the music in one window (it replaces the Music part of Settings). The list of songs (the game's written songs, the songs you composed and kept, and the pieces of the musicians'
 *  concerts) with a tick for each: only the ticked songs are played (`Playlist`; nothing made = every song). Play now picks the song that is heard until you go back to the playlist. Also: music on / off, volume, the
 *  sound source (the built-in synthesiser or a SoundFont), newly composed songs and Next song. Everything here changes settings or the music; nothing writes the world. */
final class GuiMusic {
    private GuiMusic() { }

    static void open(final MapViewer mv) {
        GuiWindows.window(mv, "music", "Music Manager", 940, 700, 1200, (d, refreshers) -> {
            final Color BG = GuiWindows.BG, FG = GuiWindows.FG;
            JPanel root = new JPanel(new BorderLayout(6, 6));
            root.setBackground(BG);
            root.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

            // ---- the controls
            final boolean[] setting = {false};
            final JCheckBox on = new JCheckBox("Music on", Settings.music);
            on.setBackground(BG); on.setForeground(FG);
            on.addActionListener(e -> { if (setting[0]) return; Settings.music = on.isSelected(); Settings.save(); Ambient.apply(); });
            final JSlider vol = new JSlider(0, 100, Settings.musicVolume);
            vol.setBackground(BG); vol.setForeground(FG); vol.setPreferredSize(new Dimension(160, 26));
            vol.addChangeListener(e -> { if (setting[0]) return; Settings.musicVolume = vol.getValue(); if (!vol.getValueIsAdjusting()) Settings.save(); Ambient.apply(); });
            java.util.List<String> fonts = Ambient.fonts();
            final JComboBox<String> source = new JComboBox<>();
            source.addItem("Automatic (SoundFont if there is one)");
            source.addItem("Built-in instruments");
            for (String f : fonts) source.addItem("SoundFont: " + f);
            int srcIdx = Settings.musicEngine.equals("builtin") ? 1 : Settings.musicEngine.equals("soundfont") && fonts.contains(Settings.soundFont) ? 2 + fonts.indexOf(Settings.soundFont) : 0;
            source.setSelectedIndex(Math.min(srcIdx, source.getItemCount() - 1));
            source.setToolTipText("Put .sf2 SoundFont files in the soundfonts/ folder (next to src/) and reopen this window to see them here");
            source.addActionListener(ev -> {
                if (setting[0]) return;
                int i = source.getSelectedIndex();
                Settings.musicEngine = i <= 0 ? "auto" : i == 1 ? "builtin" : "soundfont";
                Settings.soundFont = i <= 1 ? "" : fonts.get(i - 2);
                Settings.save(); Ambient.apply();
            });
            final JSlider gen = new JSlider(0, 100, Settings.musicGenerated);
            gen.setBackground(BG); gen.setForeground(FG); gen.setPreferredSize(new Dimension(140, 26));
            gen.setToolTipText("EXPERIMENTAL. How often a song newly composed by the game (not kept yet) takes a slot; the game's songs are written by hand and this is an extra for people who want endless variety.");
            gen.addChangeListener(e -> { if (setting[0]) return; Settings.musicGenerated = gen.getValue(); if (!gen.getValueIsAdjusting()) Settings.save(); });
            JPanel row1 = GuiWindows.flow(FlowLayout.LEFT);
            row1.add(on); row1.add(GuiWindows.label("Volume")); row1.add(vol);
            row1.add(mv.makeBtn("Next song", "Skip to another song of the playlist.", Ambient::newPiece));
            JPanel row2 = GuiWindows.flow(FlowLayout.LEFT);
            row2.add(GuiWindows.label("Sound source")); row2.add(source);
            row2.add(GuiWindows.label("   Newly composed songs (%, experimental)")); row2.add(gen);
            final JLabel playing = GuiWindows.label(" ");
            playing.setForeground(Theme.ACCENT);
            JPanel row3 = GuiWindows.flow(FlowLayout.LEFT);
            row3.add(playing);
            row3.add(mv.makeBtn("Back to the playlist", "Stop holding one song: the places choose from the playlist again.", () -> Ambient.pin(null)));
            JPanel top = new JPanel(new GridLayout(0, 1, 0, 0)); top.setBackground(BG);
            top.add(row1); top.add(row2); top.add(row3);

            // ---- the list
            final List<Score> shown = new ArrayList<>();
            final DefaultTableModel model = new DefaultTableModel(new Object[]{"On the playlist", "Song", "Kind", "Heard in", "Length"}, 0) {
                @Override public Class<?> getColumnClass(int c) { return c == 0 ? Boolean.class : String.class; }
                @Override public boolean isCellEditable(int r, int c) { return c == 0; }
            };
            final JTable table = GuiWindows.table(model);
            table.getColumnModel().getColumn(0).setPreferredWidth(110); table.getColumnModel().getColumn(0).setMaxWidth(130);
            table.getColumnModel().getColumn(1).setPreferredWidth(280);
            table.getColumnModel().getColumn(2).setPreferredWidth(140);
            table.getColumnModel().getColumn(3).setPreferredWidth(190);
            table.getColumnModel().getColumn(4).setPreferredWidth(60);
            final JComboBox<String> kind = new JComboBox<>(new String[]{"All songs", Playlist.GAME + "s", Playlist.COMPOSED, Playlist.MUSICIAN + "s", "On the playlist"});
            final JTextField search = new JTextField(14);
            search.putClientProperty("JTextField.placeholderText", "search the titles…");
            final JLabel count = GuiWindows.label(" ");
            final boolean[] filling = {false};
            final Runnable fill = () -> {
                filling[0] = true;
                try {
                    shown.clear();
                    List<Object[]> rows = new ArrayList<>();
                    String q = search.getText().trim().toLowerCase();
                    int k = kind.getSelectedIndex();
                    for (Score s : Playlist.library()) {
                        String kd = Playlist.kind(s);
                        if (k == 1 && !kd.equals(Playlist.GAME) || k == 2 && !kd.equals(Playlist.COMPOSED) || k == 3 && !kd.equals(Playlist.MUSICIAN) || k == 4 && !Playlist.in(s)) continue;
                        if (!q.isEmpty() && !s.title.toLowerCase().contains(q)) continue;
                        shown.add(s);
                        rows.add(new Object[]{Playlist.in(s), s.title, kd, Playlist.moodNames(s), String.format("%d:%02d", (int) s.seconds() / 60, (int) s.seconds() % 60)});
                    }
                    int sel = table.getSelectedRow();
                    model.setRowCount(0);
                    for (Object[] r : rows) model.addRow(r);
                    if (sel >= 0 && sel < model.getRowCount()) table.setRowSelectionInterval(sel, sel);
                    int on2 = 0, all = Playlist.library().size();
                    for (Score s : Playlist.library()) if (Playlist.in(s)) on2++;
                    count.setText(Playlist.filtering() ? on2 + " of " + all + " songs are on the playlist." : "No playlist is made: every song of the game is played (" + SongBook.all().size() + " songs). Untick the ones you do not want, or tick only the ones you like.");
                } finally { filling[0] = false; }
            };
            model.addTableModelListener(e -> {
                if (filling[0] || e.getColumn() != 0 && e.getColumn() != javax.swing.event.TableModelEvent.ALL_COLUMNS) return;
                int r = e.getFirstRow();
                if (r < 0 || r >= shown.size() || r >= model.getRowCount()) return;
                Boolean v = (Boolean) model.getValueAt(r, 0);
                Playlist.set(shown.get(r), v != null && v);
                SwingUtilities.invokeLater(fill);
            });
            kind.addActionListener(e -> fill.run());
            search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { fill.run(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { fill.run(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { fill.run(); }
            });
            JPanel filters = GuiWindows.flow(FlowLayout.LEFT);
            filters.add(GuiWindows.label("Show")); filters.add(kind); filters.add(search);
            JPanel north = new JPanel(new BorderLayout()); north.setBackground(BG);
            north.add(top, BorderLayout.NORTH); north.add(filters, BorderLayout.CENTER);
            root.add(north, BorderLayout.NORTH);
            root.add(new JScrollPane(table), BorderLayout.CENTER);

            // ---- the buttons
            JPanel btns = GuiWindows.flow(FlowLayout.LEFT);
            btns.add(mv.makeBtn("Play now", "Hear the selected song now, and keep hearing it (it repeats after a rest) until you press Back to the playlist.", () -> {
                int r = table.getSelectedRow();
                if (r < 0 || r >= shown.size()) return;
                if (!Settings.music) { Settings.music = true; Settings.save(); Ambient.apply(); }
                final Score sc = shown.get(r);
                Ambient.pin(sc);
            }));
            btns.add(mv.makeBtn("Add the shown songs", "Tick every song that is shown in the list.", () -> { for (Score s : new ArrayList<>(shown)) Playlist.set(s, true); fill.run(); }));
            btns.add(mv.makeBtn("Remove the shown songs", "Untick every song that is shown in the list.", () -> { for (Score s : new ArrayList<>(shown)) Playlist.set(s, false); fill.run(); }));
            btns.add(mv.makeBtn("Every song", "Make no playlist: every song of the game is played.", () -> { Playlist.clear(); fill.run(); }));
            JPanel south = new JPanel(new BorderLayout()); south.setBackground(BG);
            south.add(count, BorderLayout.NORTH); south.add(btns, BorderLayout.CENTER);
            JLabel hint = GuiWindows.label("The musicians' concerts are pieces for a Musician's instrument; on the playlist they are played as songs with their own voices. A composed song (music gen / music keep) is kept in songs/.");
            hint.setForeground(Theme.DIM); hint.setFont(Theme.plain(12));
            south.add(hint, BorderLayout.SOUTH);
            root.add(south, BorderLayout.SOUTH);

            fill.run();
            refreshers.add(() -> {
                setting[0] = true;
                try { on.setSelected(Settings.music); } finally { setting[0] = false; }
                Score pin = Ambient.pinned();
                String now = Ambient.nowPlaying;
                playing.setText(pin != null ? "Holding \"" + pin.title + "\" (Back to the playlist lets the places choose)" : now == null || now.isEmpty() ? "Nothing is playing." : "Now playing: " + now);
            });
            return root;
        });
    }
}
