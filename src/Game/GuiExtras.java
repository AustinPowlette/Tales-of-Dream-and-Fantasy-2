package Game;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/** GUI for the quality-of-life features: saves, settings, family tree / relationship graph, notifications (toasts + history), tutorial,
 *  recap popup, start-up save menu and the global cultivator search. Like GuiWindows, every window only reads game state and sends
 *  actions as ordinary commands (a leading '!' means the GUI already asked the player to confirm). */
public class GuiExtras {
    private GuiExtras() { }

    static final Color BG = GuiWindows.BG, FG = GuiWindows.FG, DIM = GuiWindows.DIM, ACCENT = GuiWindows.ACCENT;
    static final Font MONO = GuiWindows.MONO, MONO_B = GuiWindows.MONO_B;

    /** GUI confirmation for an irreversible action. */
    static boolean confirm(Component parent, String message) {
        if (!Settings.confirmRisky) return true;
        return JOptionPane.showConfirmDialog(parent, message, "Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private static JTextArea text(String s) {
        JTextArea a = new JTextArea(s);
        a.setEditable(false); a.setLineWrap(true); a.setWrapStyleWord(true);
        a.setFont(MONO); a.setBackground(Theme.FIELD); a.setForeground(FG);
        a.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        return a;
    }

    private static JCheckBox check(String label, boolean on, java.util.function.Consumer<Boolean> set, String tip) {
        JCheckBox c = new JCheckBox(label, on);
        c.setBackground(BG); c.setForeground(FG); c.setFont(MONO); c.setToolTipText(tip);
        c.addActionListener(e -> set.accept(c.isSelected()));
        return c;
    }

    // ================================================================== start-up save menu
    /** Blocks the game thread (world lock released) until the player picks a slot; null = new game. */
    static String startupSaveMenu(MapViewer mv, List<SaveManager.Slot> slots) {
        final String[] result = {null};
        try {
            GameDriver.edtWait(() -> {
                DefaultListModel<String> m = new DefaultListModel<>();
                for (SaveManager.Slot s : slots) m.addElement(SaveManager.describe(s));
                JList<String> list = new JList<>(m);
                list.setFont(MONO); list.setSelectedIndex(0);
                JScrollPane sp = new JScrollPane(list);
                sp.setPreferredSize(new Dimension(760, Math.min(360, 40 + 22 * slots.size())));
                JPanel p = new JPanel(new BorderLayout(4, 4));
                p.add(new JLabel("Saved games found. Pick one to continue, or start a new world:"), BorderLayout.NORTH);
                p.add(sp, BorderLayout.CENTER);
                String[] options = {"Continue selected", "New game"};
                int r = JOptionPane.showOptionDialog(mv, p, "Saved games", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
                int i = list.getSelectedIndex();
                if (r == 0 && i >= 0) result[0] = slots.get(i).name;
            });
        } catch (Exception ignored) { }
        return result[0];
    }

    // ================================================================== saves window
    static void savesWindow(MapViewer mv) {
        GuiWindows.window(mv, "saves", "Saves", 900, 460, 2500, (d, refreshers) -> {
            JPanel root = new JPanel(new BorderLayout(4, 4));
            root.setBackground(BG);
            DefaultTableModel model = GuiWindows.model();
            JTable table = GuiWindows.table(model);
            final List<SaveManager.Slot> shown = new ArrayList<>();
            JLabel status = GuiWindows.label(" ");
            status.setForeground(ACCENT);
            refreshers.add(() -> {
                int sel = table.getSelectedRow();
                String keep = sel >= 0 && sel < shown.size() ? shown.get(sel).name : null;
                shown.clear();
                List<Object[]> rows = new ArrayList<>();
                for (SaveManager.Slot s : SaveManager.list()) {
                    shown.add(s);
                    rows.add(new Object[]{s.name, s.kind, s.year, s.population, s.players, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date(s.savedAt)),
                            (s.bytes / 1024) + " KB", s.damaged ? "DAMAGED" : s.hasBackup ? "ok + backup" : "ok"});
                }
                model.setDataVector(rows.toArray(new Object[0][]), new String[]{"Name", "Kind", "Year", "Pop.", "Players", "Saved", "Size", "State"});
                if (keep != null) for (int i = 0; i < shown.size(); i++) if (shown.get(i).name.equals(keep)) table.setRowSelectionInterval(i, i);
                status.setText(SaveManager.state.equals("idle") ? (SaveManager.lastName.isEmpty() ? "No save yet this session." : "Last: " + SaveManager.lastName + ", year " + SaveManager.lastYear)
                        : SaveManager.state.equals("error") ? "Error: " + SaveManager.lastError : SaveManager.state + "...");
            });
            JPanel bar = GuiWindows.flow(FlowLayout.LEFT);
            bar.add(mv.makeBtn("Save as…", () -> {
                String n = JOptionPane.showInputDialog(d, "Name for the save (letters, digits, - or _):", "Save game", JOptionPane.PLAIN_MESSAGE);
                if (n == null) return;
                n = SaveManager.sanitize(n);
                if (n.isEmpty()) { status.setText("That name has no usable characters."); return; }
                if (SaveManager.find(n) != null && !confirm(d, "Overwrite the save '" + n + "'?")) return;
                mv.queueInput("!save " + n);
                status.setText("Saving '" + n + "' at the start of the next turn...");
            }));
            bar.add(mv.makeBtn("Quick save", () -> { GameDriver.saveRequested = true; status.setText("Quick save at the start of the next turn..."); }));
            JButton load = mv.makeBtn("Load selected", () -> {
                int r = table.getSelectedRow();
                if (r < 0 || r >= shown.size()) { status.setText("Select a save first."); return; }
                SaveManager.Slot s = shown.get(r);
                if (!confirm(d, "Load '" + s.name + "' (year " + s.year + ")?\nAnything since your last save is lost.")) return;
                mv.queueInput("!load " + s.name);
                status.setText("Loading '" + s.name + "' at the start of the next turn...");
            });
            load.setForeground(Theme.OK);
            bar.add(load);
            JButton del = mv.makeBtn("Delete selected", () -> {
                int r = table.getSelectedRow();
                if (r < 0 || r >= shown.size()) { status.setText("Select a save first."); return; }
                SaveManager.Slot s = shown.get(r);
                if (s.kind == SaveManager.Kind.LEGACY) { status.setText("The old save.dat can't be deleted here."); return; }
                if (!confirm(d, "Delete the save '" + s.name + "' for good?")) return;
                mv.queueInput("!delsave " + s.name);
                status.setText("Deleted '" + s.name + "'.");
            });
            del.setForeground(Theme.DANGER);
            bar.add(del);
            bar.add(mv.makeBtn("Open folder", () -> { try { SaveManager.dir.mkdirs(); Desktop.getDesktop().open(SaveManager.dir); } catch (Exception e) { status.setText("Can't open the folder here."); } }));
            JLabel hint = GuiWindows.label(" Autosaves rotate (auto-1 is the newest, then auto-2...). Every save keeps a backup of the previous good copy; a crash leaves a crash-… slot.");
            hint.setForeground(DIM); hint.setFont(Theme.plain(12));
            JPanel south = new JPanel(new BorderLayout());
            south.setBackground(BG);
            south.add(bar, BorderLayout.NORTH); south.add(status, BorderLayout.CENTER); south.add(hint, BorderLayout.SOUTH);
            JLabel thumb = new JLabel("(select a save)", SwingConstants.CENTER);
            thumb.setPreferredSize(new Dimension(300, 190)); thumb.setForeground(DIM); thumb.setBorder(BorderFactory.createLineBorder(Theme.LINE));
            table.getSelectionModel().addListSelectionListener(e -> {
                int r = table.getSelectedRow();
                if (e.getValueIsAdjusting() || r < 0 || r >= shown.size()) return;
                java.io.File png = new java.io.File(SaveManager.dir, shown.get(r).name + ".png");
                try { if (png.isFile()) { thumb.setText(null); thumb.setIcon(new ImageIcon(javax.imageio.ImageIO.read(png))); } else { thumb.setIcon(null); thumb.setText("(no picture for this save)"); } } catch (Exception ex) { thumb.setIcon(null); thumb.setText("(no picture)"); }
            });
            root.add(new JScrollPane(table), BorderLayout.CENTER);
            root.add(thumb, BorderLayout.EAST);
            root.add(south, BorderLayout.SOUTH);
            return root;
        });
    }

    // ================================================================== settings window
    static void settingsWindow(MapViewer mv) {
        GuiWindows.window(mv, "settings", "Settings", 640, 640, 60000, (d, refreshers) -> {
            Runnable applyAll = () -> { Settings.save(); mv.applySettings(); };
            JPanel form = new JPanel();
            form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
            form.setBackground(BG);
            form.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

            form.add(section("Saving"));
            JSpinner auto = new JSpinner(new SpinnerNumberModel(Settings.autosaveYears, 0, 1000, 1));
            auto.addChangeListener(e -> { Settings.autosaveYears = (Integer) auto.getValue(); applyAll.run(); });
            form.add(line("Autosave every N years (0 = off)", auto));
            JSpinner keep = new JSpinner(new SpinnerNumberModel(Settings.autosaveKeep, 1, 20, 1));
            keep.addChangeListener(e -> { Settings.autosaveKeep = (Integer) keep.getValue(); applyAll.run(); });
            form.add(line("Autosaves kept (rotating)", keep));
            form.add(check("Ask before risky or irreversible actions", Settings.confirmRisky, v -> { Settings.confirmRisky = v; applyAll.run(); },
                    "Leaving a sect, declaring war, deleting quests, attacking with bad odds, overwriting or loading saves..."));

            form.add(section("Speed"));
            form.add(check("EXPERIMENTAL: NPCs of different tiles take their turns on several cores", Settings.parallelNpcs, v -> { Settings.parallelNpcs = v; applyAll.run(); },
                    "Off (default): the NPCs take their turns one after the other. On: the turns of NPCs on different tiles run together, 2-4 times faster in a big world; people on a tile with a person who plays are still played one by one. Experimental: if anything looks wrong, switch it off."));

            form.add(section("A new world"));
            JSpinner stPop = new JSpinner(new SpinnerNumberModel(Settings.startPopulation, 1000, 1000000, 5000));
            stPop.addChangeListener(e -> { Settings.startPopulation = (Integer) stPop.getValue(); applyAll.run(); });
            stPop.setToolTipText("How many people the NEXT new world begins with (you are asked at the start too). 10,000 to 150,000 is recommended: a weaker computer plays a smaller world faster. The world you are in keeps its size.");
            form.add(line("People at the start of a new world", stPop));

            form.add(section("The epoch (the great purge of a crowded world)"));
            JSpinner epPop = new JSpinner(new SpinnerNumberModel(Math.max(1000, Math.min(10000000, Settings.epochLimit())), 1000, 10000000, 10000));
            epPop.addChangeListener(e -> { Settings.epochPopulation = (Integer) epPop.getValue(); Settings.epochCustom = true; applyAll.run(); });
            epPop.setToolTipText("The epoch comes when this many cultivators live, whatever the speed of the computer");
            form.add(line("Most cultivators before an epoch", epPop));
            JSpinner epMem = new JSpinner(new SpinnerNumberModel(Settings.epochMemoryPercent, 0, 95, 5));
            epMem.addChangeListener(e -> { Settings.epochMemoryPercent = (Integer) epMem.getValue(); applyAll.run(); });
            epMem.setToolTipText("The epoch also comes when more than this percent of the memory the game may use (-Xmx) is alive, so an old world is thinned before it runs out of memory. 0 = never for that reason. Checked once a year. " + MemWatch.line() + ". The task manager shows the heap the Java machine grew to, not what the game needs: start the game with -Xmx3g to keep it near 3 GB.");
            form.add(line("Memory alive that brings an epoch (%)", epMem));
            JSpinner epSec = new JSpinner(new SpinnerNumberModel(Settings.epochTickSeconds, 0.1, 600.0, 0.5));
            epSec.addChangeListener(e -> { Settings.epochTickSeconds = ((Number) epSec.getValue()).doubleValue(); applyAll.run(); });
            epSec.setToolTipText("The epoch also comes when a typical turn takes longer than this many seconds (in a world of 10,000+): a slower computer is purged sooner, so every computer plays at a good speed");
            form.add(line("Slowest typical turn, in seconds", epSec));
            JSpinner epPct = new JSpinner(new SpinnerNumberModel(Settings.epochCullPercent, 1, 95, 1));
            epPct.addChangeListener(e -> { Settings.epochCullPercent = (Integer) epPct.getValue(); applyAll.run(); });
            epPct.setToolTipText("Percent of each realm's cultivators the epoch takes (realms with fewer than 10 are spared)");
            form.add(line("Cull: percent of each realm", epPct));
            JSpinner epNum = new JSpinner(new SpinnerNumberModel(Settings.epochCullNumber, 0, 10000000, 1000));
            epNum.addChangeListener(e -> { Settings.epochCullNumber = (Integer) epNum.getValue(); applyAll.run(); });
            epNum.setToolTipText("0 = use the percent. Above 0: the epoch takes this many cultivators in all, shared out over the realms by their size");
            form.add(line("Cull: a number in all (0 = use the percent)", epNum));
            JSpinner epRealm = new JSpinner(new SpinnerNumberModel(Settings.epochMaxRealm, 0, 10, 1));
            epRealm.addChangeListener(e -> { Settings.epochMaxRealm = (Integer) epRealm.getValue(); applyAll.run(); });
            epRealm.setToolTipText("The epoch takes people only from Major Lvl 0 up to this realm; the realms above are left alone. Only the survivors of the realms it thinned get the blessing (the potential of half a Potential Pill).");
            form.add(line("Highest realm the epoch culls (Major Lvl)", epRealm));
            JSpinner crowd = new JSpinner(new SpinnerNumberModel(Settings.crowdFactor, 0.5, 100.0, 0.5));
            crowd.addChangeListener(e -> { Settings.crowdFactor = ((Number) crowd.getValue()).doubleValue(); GameDriver.rescaleCrowd(); applyAll.run(); });
            crowd.setToolTipText("A tile is overcrowded (beasts ambush everyone there) above this many times its fair share of the starting population, an even spread over the map. A city holds four times as much; Originis has no limit.");
            form.add(line("Overcrowded above (times a tile's fair share)", crowd));

            form.add(section("Display"));
            JSlider font = new JSlider(70, 250, (int) Math.round(Settings.fontScale * 100));
            font.setBackground(BG); font.setForeground(FG); font.setMajorTickSpacing(45); font.setPaintTicks(true);
            JLabel fontLbl = GuiWindows.label(font.getValue() + "%");
            font.addChangeListener(e -> {
                fontLbl.setText(font.getValue() + "%");
                if (!font.getValueIsAdjusting()) { Settings.fontScale = font.getValue() / 100.0; Settings.save(); Ui.fontScaleChanged(); }
            });
            JPanel fl = new JPanel(new BorderLayout(6, 0)); fl.setBackground(BG); fl.add(font, BorderLayout.CENTER); fl.add(fontLbl, BorderLayout.EAST);
            form.add(line("Text size", fl));
            form.add(check("Place pictures (Tile Info and map thumbnails)", Settings.pictures, v -> { Settings.pictures = v; PlaceImages.enabled = v; applyAll.run(); }, "Hand-made art in images/places, or painted by the game"));
            form.add(check("Paint a picture for places without hand-made art", Settings.paintedArt, v -> { Settings.paintedArt = v; Settings.applyArt(); PlaceImages.reload(); applyAll.run(); }, "Every place gets its own watercolour landscape"));
            form.add(check("New pictures every launch", Settings.artFresh, v -> { Settings.artFresh = v; Settings.applyArt(); PlaceImages.reload(); applyAll.run(); }, "Off: the same places look the same every time you play"));
            JPanel artBtns = GuiWindows.flow(FlowLayout.LEFT);
            artBtns.add(mv.makeBtn("Repaint all pictures", () -> { Settings.rerollArt(); applyAll.run(); }));
            artBtns.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(artBtns);
            form.add(check("Colour-blind-safe map colours", Settings.colorblindMap, v -> { Settings.colorblindMap = v; applyAll.run(); }, "A palette that keeps cities, sects, dungeons and terrain apart without relying on red/green"));
            form.add(check("Swap my gear for me in manual mode", Settings.autoEquipManual, v -> { Settings.autoEquipManual = v; applyAll.run(); }, "Off: in MANUAL mode you wear what you choose (empty slots are still filled). In AUTO the game always picks the strongest."));
            form.add(check("High-contrast theme (restart to apply)", Settings.highContrast, v -> { Settings.highContrast = v; applyAll.run(); }, "Black background, white text, bright accents"));
            form.add(check("Tooltips on buttons", Settings.tooltips, v -> { Settings.tooltips = v; applyAll.run(); }, "Hover text explaining every button"));
            form.add(check("Remember window positions and sizes", Settings.rememberWindows, v -> { Settings.rememberWindows = v; if (!v) Settings.windowBounds.clear(); applyAll.run(); }, "Pop-up windows reopen where and how big you left them"));
            JSpinner chal = new JSpinner(new SpinnerNumberModel(Settings.maxRandomChallenges, 0, 50, 1));
            chal.addChangeListener(ev -> { Settings.maxRandomChallenges = (Integer) chal.getValue(); applyAll.run(); });
            form.add(line("Challenges per turn from strangers", chal));
            form.add(check("New characters start with their first ten years of cultivation", Settings.matureStart, v -> { Settings.matureStart = v; applyAll.run(); }, "On: a new human begins at the level an 18-year-old of their potential would have (like everyone else in the world). Off: level 1. Story starts always begin at level 1."));
            form.add(check("First-time hints", Settings.hints, v -> { Settings.hints = v; applyAll.run(); }, "One-time tips the first time something happens"));
            form.add(check("Story: the Palace of the Oldest Order", Settings.lore, v -> { Settings.lore = v; applyAll.run(); }, "On: the story that runs through every life (the Palace at Originis, ???'s scenes). Off: none of it runs or shows. A world that began without it: type 'lore enable'."));
            form.add(check("Hear what people say to you", Settings.dialogueFlavor, v -> { Settings.dialogueFlavor = v; applyAll.run(); }, "On: when someone speaks to you, offers friendship, asks to join your squad or challenges you, their words appear in the log and the terminal."));
            form.add(check("Conversation windows with a portrait", Settings.dialoguePopups, v -> { Settings.dialoguePopups = v; applyAll.run(); }, "On: people who stop to talk open a window with their portrait and replies to choose from, and the Talk button opens a full conversation. Off: only their words in the log."));
            form.add(check("Talks for an AUTO character too", Settings.dialogueAuto, v -> { Settings.dialogueAuto = v; applyAll.run(); }, "Off (default): an AUTO or script-run character only hears the people's words. On: it also has the branching talks and conversations, answering by its own temper; the talk is written to the log."));
            form.add(check("Pause the game while a story scene is open", Settings.lorePause, v -> { Settings.lorePause = v; applyAll.run(); }, "The game waits until you press Continue on a scene of ???."));

            form.add(section("Music"));
            form.add(check("Music", Settings.music, v -> { Settings.music = v; applyAll.run(); }, "Seven songs that follow where you are (the capital, open country, cities, night and winter, danger, war, sects), fade into each other, rest between songs and are played a little differently each time."));
            JSlider vol = new JSlider(0, 100, Settings.musicVolume);
            vol.setBackground(BG); vol.setForeground(FG);
            vol.addChangeListener(e -> { Settings.musicVolume = vol.getValue(); if (!vol.getValueIsAdjusting()) { Settings.save(); } Ambient.apply(); });
            form.add(line("Music volume", vol));
            JSlider gen = new JSlider(0, 100, Settings.musicGenerated);
            gen.setBackground(BG); gen.setForeground(FG);
            gen.setToolTipText("EXPERIMENTAL. How often a song newly composed by the game (not kept yet) takes a slot; the game's songs are written by hand and this is an extra for people who want endless variety, so the tunes are not as good. 0: only the written and the kept songs. Type 'music gen' to hear one and 'music keep' to keep it.");
            gen.addChangeListener(e -> { Settings.musicGenerated = gen.getValue(); if (!gen.getValueIsAdjusting()) Settings.save(); });
            form.add(line("Newly composed songs (%, experimental)", gen));
            JPanel musicBtns = GuiWindows.flow(FlowLayout.LEFT);
            musicBtns.add(mv.makeBtn("Next song", Ambient::newPiece));
            musicBtns.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(musicBtns);
            java.util.List<String> fonts = Ambient.fonts();
            JComboBox<String> source = new JComboBox<>();
            source.addItem("Automatic (SoundFont if there is one)");
            source.addItem("Built-in instruments");
            for (String f : fonts) source.addItem("SoundFont: " + f);
            int srcIdx = Settings.musicEngine.equals("builtin") ? 1 : Settings.musicEngine.equals("soundfont") && fonts.contains(Settings.soundFont) ? 2 + fonts.indexOf(Settings.soundFont) : 0;
            source.setSelectedIndex(Math.min(srcIdx, source.getItemCount() - 1));
            source.setToolTipText("Put .sf2 SoundFont files in the soundfonts/ folder (next to src/) and restart the window to see them here");
            source.addActionListener(ev -> {
                int i = source.getSelectedIndex();
                Settings.musicEngine = i <= 0 ? "auto" : i == 1 ? "builtin" : "soundfont";
                Settings.soundFont = i <= 1 ? "" : fonts.get(i - 2);
                Settings.save(); Ambient.apply();
            });
            form.add(line("Sound source", source));
            JLabel playing = new JLabel(Ambient.nowPlaying.isEmpty() ? " " : "Now playing: " + Ambient.nowPlaying);
            playing.setForeground(Theme.DIM); playing.setFont(Theme.plain(12)); playing.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(playing);

            form.add(section("Notifications"));
            form.add(check("Pop-up toasts", Settings.toasts, v -> { Settings.toasts = v; applyAll.run(); }, "Small pop-ups in the corner of the window"));
            form.add(check("Sound (system beep)", Settings.sound, v -> { Settings.sound = v; applyAll.run(); }, "Beep when a notice arrives"));
            for (int i = 0; i < Notifications.CATEGORIES.length; i++) {
                final String cat = Notifications.CATEGORIES[i];
                form.add(check("   " + Notifications.LABELS[i], Notifications.enabled(cat), v -> { if (v) Settings.mutedNotifications.remove(cat); else Settings.mutedNotifications.add(cat); applyAll.run(); },
                        "Tell me about this (unchecked = silent)"));
            }
            form.add(section("Auto-pause (the game pauses when this happens to you)"));
            String[][] pauseKinds = {{"attack", "Attacked or challenged"}, {"breakthrough", "Breakthrough"}, {"death", "A relation dies"}, {"war", "War or peace"},
                    {"family", "Birth or marriage"}, {"alert", "New quest alert"}, {"danger", "Danger (crowd, lifespan)"}, {"auction", "Auction soon"}, {"fortune", "Good fortune"}};
            for (String[] pk : pauseKinds) {
                form.add(check("   " + pk[1], Settings.pauseOn.contains(pk[0]), v -> { if (v) Settings.pauseOn.add(pk[0]); else Settings.pauseOn.remove(pk[0]); applyAll.run(); },
                        "Pause the game (GUI) when this happens, so a fast-forward never runs past it"));
            }
            JComboBox<String> recap = new JComboBox<>(new String[]{"Off", "Each season (13 weeks)", "Each year (52 weeks)", "Every 5 years"});
            int[] weeks = {0, 13, 52, 260};
            int cur = 2;
            for (int i = 0; i < weeks.length; i++) if (weeks[i] == Settings.recapWeeks) cur = i;
            recap.setSelectedIndex(cur);
            recap.addActionListener(e -> { Settings.recapWeeks = weeks[recap.getSelectedIndex()]; applyAll.run(); });
            form.add(line("Summary pop-up (recap)", recap));

            form.add(section("Help"));
            JPanel helpBtns = GuiWindows.flow(FlowLayout.LEFT);
            helpBtns.add(mv.makeBtn("Open tutorial", () -> tutorialWindow(mv)));
            helpBtns.add(mv.makeBtn("Reset hints", () -> { Settings.shownHints.clear(); Settings.save(); }));
            helpBtns.add(mv.makeBtn("Open exports folder", () -> { try { Exporter.dir(); Desktop.getDesktop().open(Exporter.dir()); } catch (Exception e) { /* headless */ } }));
            helpBtns.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(helpBtns);
            JScrollPane sp = new JScrollPane(form);
            sp.getViewport().setBackground(BG);
            return sp;
        });
    }

    private static JLabel section(String t) {
        JLabel l = GuiWindows.label("— " + t + " —");
        l.setForeground(ACCENT); l.setFont(MONO_B);
        l.setBorder(BorderFactory.createEmptyBorder(10, 0, 4, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static JPanel line(String label, JComponent c) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBackground(BG);
        p.add(GuiWindows.label(label), BorderLayout.WEST);
        if (c instanceof JSpinner || c instanceof JComboBox) { // keep these at their natural width instead of stretching across the window
            JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); wrap.setBackground(BG); wrap.add(c); p.add(wrap, BorderLayout.CENTER);
        } else p.add(c, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    // ================================================================== tutorial / welcome / recap
    static void tutorialWindow(MapViewer mv) { GuiHelp.tutorial(mv); } // the welcome tour lives in GuiHelp (with the Help window)

    /** First launch: show the tutorial once (Settings.firstRunDone), then never again unless asked. */
    static void welcome(MapViewer mv) {
        if (Settings.firstRunDone) return;
        Settings.firstRunDone = true;
        Settings.save();
        tutorialWindow(mv);
    }

    private static JDialog lastRecap = null;

    static void recapWindow(MapViewer mv, String title, String body) {
        if (lastRecap != null && lastRecap.isDisplayable()) lastRecap.dispose(); // one recap at a time: unread ones used to pile up year after year
        JDialog d = new JDialog(mv, title, false);
        lastRecap = d;
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        d.getContentPane().setBackground(BG);
        d.setLayout(new BorderLayout(4, 4));
        JTextArea t = text(body);
        t.setFont(Theme.plain(14));
        d.add(new JScrollPane(t), BorderLayout.CENTER);
        JPanel south = GuiWindows.flow(FlowLayout.LEFT);
        JCheckBox stop = check("Stop showing recaps", false, v -> { }, "Recaps can be switched back on in Settings");
        south.add(stop);
        south.add(mv.makeBtn("Notification history", () -> notificationsWindow(mv)));
        south.add(mv.makeBtn("OK", () -> { if (stop.isSelected()) { Settings.recapWeeks = 0; Settings.save(); } d.dispose(); }));
        d.add(south, BorderLayout.SOUTH);
        d.setSize(620, 460);
        d.setLocationRelativeTo(mv);
        Ui.prepare(d);
        d.setVisible(true);
    }

    // ================================================================== notifications: toasts + history window
    static Color catColor(String cat) {
        switch (cat) {
            case "breakthrough": return new Color(255, 215, 80);
            case "level": return new Color(120, 220, 120);
            case "death": return new Color(240, 100, 100);
            case "war": return new Color(255, 150, 60);
            case "family": return new Color(255, 150, 200);
            case "alert": return new Color(255, 170, 60);
            case "danger": return new Color(255, 90, 90);
            case "auction": return new Color(90, 220, 230);
            case "fortune": return new Color(240, 200, 90);
            case "hint": return new Color(140, 180, 255);
            default: return new Color(190, 190, 190);
        }
    }

    private static final List<JWindow> toasts = new ArrayList<>();

    static void toast(MapViewer mv, Notifications.Note n) {
        if (!mv.isShowing()) return;
        while (toasts.size() >= 4) closeToast(toasts.get(0));
        JWindow w = new JWindow(mv);
        w.setFocusableWindowState(false);
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(30, 30, 52));
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(catColor(n.cat), 2), BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        String html = n.text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        JLabel l = new JLabel("<html><body style='width:" + (int) (290 * Settings.fontScale) + "px'>" + html + "</body></html>");
        l.setForeground(FG); l.setFont(new Font("Monospaced", Font.PLAIN, (int) Math.round(13 * Settings.fontScale)));
        p.add(l, BorderLayout.CENTER);
        w.setContentPane(p);
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        p.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                closeToast(w);
                Character c = n.charId >= 0 ? Character.charById(n.charId) : null;
                if (c != null && !c.dead) mv.showCharacterDialog(c); else notificationsWindow(mv);
            }
        });
        w.pack();
        toasts.add(w);
        placeToasts(mv);
        w.setVisible(true);
        javax.swing.Timer t = new javax.swing.Timer(6500, e -> closeToast(w));
        t.setRepeats(false);
        t.start();
        // the toast is going to be seen: the history entry stays "unread" until the history window is opened
    }

    private static void closeToast(JWindow w) {
        if (!toasts.remove(w)) return;
        w.dispose();
        Window owner = w.getOwner();
        if (owner instanceof MapViewer) placeToasts((MapViewer) owner);
    }

    private static void placeToasts(MapViewer mv) {
        int y = mv.getY() + mv.getHeight() - 60;
        for (int i = toasts.size() - 1; i >= 0; i--) {
            JWindow w = toasts.get(i);
            y -= w.getHeight() + 6;
            w.setLocation(mv.getX() + mv.getWidth() - w.getWidth() - 24, y);
        }
    }

    static void notificationsWindow(MapViewer mv) {
        GuiWindows.window(mv, "notices", "Notifications", 820, 480, 2000, (d, refreshers) -> {
            DefaultTableModel model = GuiWindows.model();
            JTable table = GuiWindows.table(model);
            table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                    Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                    if (!sel) { comp.setBackground(Theme.FIELD); comp.setForeground(c == 1 ? catColor(String.valueOf(t.getValueAt(r, 1))) : FG); }
                    return comp;
                }
            });
            final List<Notifications.Note> shown = new ArrayList<>();
            JComboBox<String> filter = new JComboBox<>();
            filter.addItem("All");
            for (String c : Notifications.CATEGORIES) filter.addItem(c);
            refreshers.add(() -> {
                List<Notifications.Note> all;
                synchronized (Notifications.history) { all = new ArrayList<>(Notifications.history); }
                String f = String.valueOf(filter.getSelectedItem());
                shown.clear();
                List<Object[]> rows = new ArrayList<>();
                for (int i = all.size() - 1; i >= 0; i--) {
                    Notifications.Note n = all.get(i);
                    if (!f.equals("All") && !n.cat.equals(f)) continue;
                    shown.add(n); n.read = true;
                    rows.add(new Object[]{"wk " + n.week, n.cat, n.text});
                }
                int sel = table.getSelectedRow();
                model.setDataVector(rows.toArray(new Object[0][]), new String[]{"When", "Kind", "Notice"});
                table.getColumnModel().getColumn(0).setPreferredWidth(70);
                table.getColumnModel().getColumn(1).setPreferredWidth(110);
                table.getColumnModel().getColumn(2).setPreferredWidth(600);
                if (sel >= 0 && sel < rows.size()) table.setRowSelectionInterval(sel, sel);
            });
            table.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() != 2) return;
                    int r = table.getSelectedRow();
                    if (r < 0 || r >= shown.size()) return;
                    Character c = shown.get(r).charId >= 0 ? Character.charById(shown.get(r).charId) : null;
                    if (c != null && !c.dead) mv.showCharacterDialog(c);
                }
            });
            JPanel bar = GuiWindows.flow(FlowLayout.LEFT);
            bar.add(GuiWindows.label("show")); bar.add(filter);
            bar.add(mv.makeBtn("Settings", () -> settingsWindow(mv)));
            bar.add(GuiWindows.label(" double-click a notice to open the cultivator it is about"));
            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(BG);
            root.add(bar, BorderLayout.NORTH);
            root.add(new JScrollPane(table), BorderLayout.CENTER);
            return root;
        });
    }

    // ================================================================== story objectives
    static void storyWindow(MapViewer mv) {
        GuiWindows.window(mv, "story", "Story", 760, 640, 2000, (d, refreshers) -> {
            final JComboBox<String> scenes = new JComboBox<>();
            final JButton again = mv.makeBtn("Read again", () -> {
                Object o = scenes.getSelectedItem();
                if (o != null) { String id = o.toString(); int dash = id.indexOf("  -  "); mv.queueInput("story replay " + (dash > 0 ? id.substring(0, dash) : id)); }
            });
            refreshers.add(() -> {
                Character p = GuiWindows.player(mv);
                java.util.List<String> items = p == null || p.story == null ? new ArrayList<String>() : Chapters.sceneList(p.story);
                boolean same = items.size() == scenes.getItemCount();
                for (int i = 0; same && i < items.size(); i++) if (!items.get(i).equals(scenes.getItemAt(i))) same = false;
                if (!same) { scenes.removeAllItems(); for (String it : items) scenes.addItem(it); }
                again.setEnabled(!items.isEmpty());
                scenes.setEnabled(!items.isEmpty());
            });
            JPanel south = GuiWindows.flow(FlowLayout.LEFT);
            south.add(GuiWindows.label("Scenes you have seen: "));
            south.add(scenes);
            south.add(again);
            JPanel root = new JPanel(new BorderLayout());
            root.add(GuiPages.pane(mv, refreshers, () -> GuiPages.storyHtml(GuiWindows.player(mv))), BorderLayout.CENTER);
            root.add(south, BorderLayout.SOUTH);
            return root;
        });
    }

    // ================================================================== family tree + relationship graph
    private static void openPerson(MapViewer mv, int id) {
        Character c = Character.charById(id);
        if (c != null && !c.dead) mv.showCharacterDialog(c);
        else if (c != null) JOptionPane.showMessageDialog(mv, c.name + " " + c.lastName + " died at age " + c.age + ".", "Deceased", JOptionPane.INFORMATION_MESSAGE);
    }

    static class TreePanel extends JPanel {
        volatile Family.Tree tree;
        private final List<Object[]> hits = new ArrayList<>(); // {Rectangle, Integer id}
        TreePanel(MapViewer mv) {
            setBackground(Theme.FIELD);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    for (Object[] h : hits) if (((Rectangle) h[0]).contains(e.getPoint())) { openPerson(mv, (Integer) h[1]); return; }
                }
            });
            setToolTipText("Click a person to open them");
        }
        @Override public Dimension getPreferredSize() {
            Family.Tree t = tree;
            int n = 1;
            if (t != null) for (List<Family.Person> r : t.rows) n = Math.max(n, r.size());
            return new Dimension(Math.max(600, n * 160), Math.max(360, (t == null ? 1 : t.rows.size()) * 110 + 40));
        }
        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            hits.clear();
            Family.Tree t = tree;
            if (t == null) { g.setColor(DIM); g.setFont(MONO); g.drawString("No player character.", 20, 30); return; }
            int w = getWidth(), h = getHeight(), rows = t.rows.size();
            int boxH = 46;
            for (int i = 0; i < rows; i++) {
                List<Family.Person> r = t.rows.get(i);
                int n = r.size(), boxW = Math.max(70, Math.min(150, w / Math.max(1, n) - 10));
                int y = rows == 1 ? h / 2 - boxH / 2 : 24 + i * (h - 48 - boxH) / (rows - 1);
                for (int j = 0; j < n; j++) { Family.Person p = r.get(j); p.x = (int) ((long) w * (2 * j + 1) / (2 * n)); p.y = y + boxH / 2; }
                g.setFont(Theme.plain(11)); g.setColor(DIM);
                int gen = i - t.selfGen;
                g.drawString(gen <= -3 ? "ancestors" : gen == -2 ? "grandparents" : gen == -1 ? "parents" : gen == 0 ? "your generation" : gen == 1 ? "children" : gen == 2 ? "grandchildren" : "descendants", 4, y + 10);
            }
            g.setStroke(new BasicStroke(1.6f));
            for (Family.Person[] e : t.edges) {
                g.setColor(new Color(110, 130, 190));
                g.draw(new Line2D.Double(e[0].x, e[0].y + boxH / 2, e[1].x, e[1].y - boxH / 2));
            }
            if (t.self != null) for (Family.Person p : t.rows.get(t.selfGen)) if (p.label.equals("spouse")) {
                g.setColor(Theme.GOLD); g.setStroke(new BasicStroke(2.4f));
                g.draw(new Line2D.Double(t.self.x, t.self.y, p.x, p.y));
            }
            g.setStroke(new BasicStroke(1f));
            for (List<Family.Person> r : t.rows) for (Family.Person p : r) {
                int boxW = Math.max(70, Math.min(150, w / Math.max(1, r.size()) - 10));
                Rectangle b = new Rectangle(p.x - boxW / 2, p.y - boxH / 2, boxW, boxH);
                boolean me = p == t.self;
                g.setColor(me ? new Color(70, 60, 20) : p.dead ? new Color(30, 30, 34) : new Color(28, 34, 62));
                g.fillRoundRect(b.x, b.y, b.width, b.height, 10, 10);
                g.setColor(me ? Theme.GOLD : p.dead ? new Color(90, 90, 90) : new Color(100, 130, 220));
                g.drawRoundRect(b.x, b.y, b.width, b.height, 10, 10);
                g.setFont(Theme.bold(12));
                g.setColor(p.dead ? DIM : FG);
                g.drawString(clip(g, p.name, boxW - 8), b.x + 5, b.y + 15);
                g.setFont(Theme.plain(11));
                g.setColor(DIM);
                g.drawString(p.label + (p.dead ? " †" : ""), b.x + 5, b.y + 29);
                g.drawString("Lv" + p.lvl + " age " + p.age, b.x + 5, b.y + 42);
                hits.add(new Object[]{b, p.id});
            }
        }
    }

    private static String clip(Graphics2D g, String s, int w) {
        FontMetrics m = g.getFontMetrics();
        if (m.stringWidth(s) <= w) return s;
        while (s.length() > 3 && m.stringWidth(s + "…") > w) s = s.substring(0, s.length() - 1);
        return s + "…";
    }

    static class GraphPanel extends JPanel {
        volatile List<Family.Tie> ties = new ArrayList<>();
        volatile String me = "";
        private final List<Object[]> hits = new ArrayList<>();
        GraphPanel(MapViewer mv) {
            setBackground(Theme.FIELD);
            setToolTipText("Click a person to open them. Green = friends, red = rivals, gold = spouse, blue = family; thicker = stronger.");
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    for (Object[] h : hits) if (((Rectangle) h[0]).contains(e.getPoint())) { openPerson(mv, (Integer) h[1]); return; }
                }
            });
        }
        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            hits.clear();
            int w = getWidth(), h = getHeight(), cx = w / 2, cy = h / 2;
            List<Family.Tie> l = ties;
            if (l.isEmpty()) { g.setColor(DIM); g.setFont(MONO); g.drawString("No relationships yet: talk, spar and travel to make some.", 20, 30); }
            int n = l.size();
            double rx = Math.max(80, w / 2 - 110), ry = Math.max(70, h / 2 - 50);
            for (int i = 0; i < n; i++) {
                Family.Tie t = l.get(i);
                double a = -Math.PI / 2 + 2 * Math.PI * i / Math.max(1, n);
                int x = (int) (cx + rx * Math.cos(a)), y = (int) (cy + ry * Math.sin(a));
                boolean fam = t.label.equals("mother") || t.label.equals("father") || t.label.equals("child") || t.label.equals("sibling");
                Color c = t.label.equals("spouse") ? Theme.GOLD : fam ? new Color(110, 150, 240) : t.value > 0 ? Theme.OK : t.value < 0 ? new Color(230, 80, 80) : new Color(140, 140, 140);
                g.setColor(c);
                g.setStroke(new BasicStroke(1.5f + 1.5f * Math.min(3, Math.abs(t.value))));
                g.draw(new Line2D.Double(cx, cy, x, y));
                Rectangle b = new Rectangle(x - 62, y - 20, 124, 40);
                g.setStroke(new BasicStroke(1f));
                g.setColor(new Color(24, 26, 44)); g.fillRoundRect(b.x, b.y, b.width, b.height, 10, 10);
                g.setColor(c); g.drawRoundRect(b.x, b.y, b.width, b.height, 10, 10);
                g.setFont(Theme.bold(11)); g.setColor(FG);
                g.drawString(clip(g, t.other.name + " " + t.other.lastName, 114), b.x + 5, b.y + 15);
                g.setFont(Theme.plain(10)); g.setColor(DIM);
                g.drawString(t.label + " (" + (t.value > 0 ? "+" : "") + t.value + ")  Lv" + t.other.lvl, b.x + 5, b.y + 31);
                hits.add(new Object[]{b, t.other.id});
            }
            g.setColor(new Color(70, 60, 20)); g.fillOval(cx - 42, cy - 24, 84, 48);
            g.setColor(Theme.GOLD); g.drawOval(cx - 42, cy - 24, 84, 48);
            g.setFont(Theme.bold(12));
            g.drawString(clip(g, me, 76), cx - 36, cy + 4);
        }
    }

    static void familyWindow(MapViewer mv) {
        GuiWindows.window(mv, "family", "Family & Relationships", 940, 640, 3000, (d, refreshers) -> {
            TreePanel tree = new TreePanel(mv);
            GraphPanel graph = new GraphPanel(mv);
            refreshers.add(() -> {
                Character p = GuiWindows.player(mv);
                if (p == null) { tree.tree = null; graph.ties = new ArrayList<>(); tree.repaint(); graph.repaint(); return; }
                tree.tree = Family.build(p, 3, 3);
                graph.ties = Family.ties(p, 16);
                graph.me = p.name;
                tree.revalidate(); tree.repaint(); graph.repaint();
            });
            JTabbedPane tabs = new JTabbedPane();
            tabs.setFont(MONO_B);
            tabs.addTab("Family tree", new JScrollPane(tree));
            tabs.addTab("Relationship graph", graph);
            tabs.addTab("Family list", GuiPages.pane(mv, refreshers, () -> GuiPages.familyListHtml(GuiWindows.player(mv))));
            tabs.addTab("Relationship list", GuiPages.pane(mv, refreshers, () -> GuiSheets.relationsHtml(GuiWindows.player(mv), "", 0, 80)));
            tabs.addTab("Past lives", GuiPages.pane(mv, refreshers, () -> GuiPages.pastLivesHtml(GuiWindows.player(mv))));
            // the relationship web of anyone: type a name or #id
            GraphPanel web = new GraphPanel(mv);
            JTextField who = new JTextField(18);
            JLabel whoStatus = GuiWindows.label("Type a name or #id and press Show.");
            Runnable show = () -> {
                String q = who.getText().trim();
                World.read(300, () -> {
                    Character c = TextTools.findCharacter(q);
                    if (c == null) { whoStatus.setText("Nobody found for '" + q + "'."); return; }
                    web.ties = Family.ties(c, 16); web.me = c.name + " " + c.lastName;
                    whoStatus.setText(c.name + " " + c.lastName + " (#" + c.id + ", Lv." + c.lvl + ") — click a person in the web to open them.");
                });
                web.repaint();
            };
            who.addActionListener(e -> show.run());
            JPanel webTop = GuiWindows.flow(FlowLayout.LEFT);
            webTop.add(GuiWindows.label("Whose web:")); webTop.add(who); webTop.add(mv.makeBtn("Show", show)); webTop.add(whoStatus);
            JPanel webTab = new JPanel(new BorderLayout()); webTab.setBackground(BG);
            webTab.add(webTop, BorderLayout.NORTH); webTab.add(web, BorderLayout.CENTER);
            tabs.addTab("Anyone's web", webTab);
            return tabs;
        });
    }

    // ================================================================== search anyone (Browse window tab)
    static JPanel searchTab(MapViewer mv, List<Runnable> refreshers) {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBackground(BG);
        JTextField q = new JTextField(14);
        JTextField lvlMin = new JTextField(3), lvlMax = new JTextField(3), extra = new JTextField(14);
        JComboBox<String> aff = new JComboBox<>(new String[]{"any", "good", "neutral", "evil"});
        JComboBox<String> sort = new JComboBox<>();
        sort.addItem("(level)");
        for (String k : PlayerLists.WORLD_KEYS) sort.addItem(k);
        JCheckBox desc = new JCheckBox("desc");
        desc.setBackground(BG); desc.setForeground(FG);
        JSpinner limit = new JSpinner(new SpinnerNumberModel(25, 1, ListQuery.MAX_LIMIT, 5));
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        filters.setBackground(BG);
        java.util.function.BiConsumer<String, Component> add = (label, comp) -> { filters.add(GuiWindows.label(label)); filters.add(comp); };
        add.accept("name or #id", q); add.accept("lvl ≥", lvlMin); add.accept("≤", lvlMax); add.accept("aff", aff); add.accept("extra", extra); add.accept("sort", sort);
        filters.add(desc); add.accept("limit", limit);
        DefaultTableModel model = GuiWindows.model();
        JTable table = GuiWindows.table(model);
        JLabel status = GuiWindows.label(" ");
        status.setForeground(ACCENT);
        final PlayerLists.ListView[] current = {null};

        Runnable search = () -> {
            Character p = mv.findPlayer();
            if (p == null || p.location == null) { status.setText("No player character."); return; }
            List<String> tok = new ArrayList<>();
            tok.add(String.valueOf(limit.getValue()));
            String s = q.getText().trim();
            if (s.matches("#?\\d+")) tok.add("id=" + s.replace("#", ""));
            else for (String w : s.split("\\s+")) if (!w.isEmpty()) tok.add("name=" + w);
            if (!lvlMin.getText().trim().isEmpty()) tok.add("lvl>=" + lvlMin.getText().trim());
            if (!lvlMax.getText().trim().isEmpty()) tok.add("lvl<=" + lvlMax.getText().trim());
            if (aff.getSelectedIndex() > 0) tok.add("aff=" + aff.getSelectedItem());
            if (sort.getSelectedIndex() > 0) tok.add("sort=" + sort.getSelectedItem() + (desc.isSelected() ? ":desc" : ""));
            else if (desc.isSelected()) tok.add("desc");
            for (String w : extra.getText().trim().split("\\s+")) if (!w.isEmpty()) tok.add(w);
            final PlayerLists.ListView[] out = {null};
            boolean ok = World.readWait(600, () -> {
                try { out[0] = PlayerLists.world(p, ListQuery.parse(tok.toArray(new String[0]), 0), p.pc != null ? p.pc.listLimit : 25); }
                catch (RuntimeException ex) { out[0] = null; }
            });
            if (!ok || out[0] == null) { status.setText("The world is busy: try again in a moment."); return; }
            PlayerLists.ListView v = out[0];
            if (v.error != null) { status.setText(v.error); return; }
            current[0] = v;
            model.setDataVector(v.rows.toArray(new Object[0][]), v.headers);
            status.setText("found " + v.total + (v.total > v.rows.size() ? " (showing " + v.rows.size() + ")" : "") + "   —   searches the whole world; press Enter to search");
        };
        refreshers.add(() -> { /* global scans are only run on demand */ });
        java.awt.event.ActionListener enter = e -> search.run();
        for (JTextField f : new JTextField[]{q, lvlMin, lvlMax, extra}) f.addActionListener(enter);
        aff.addActionListener(enter); sort.addActionListener(enter); desc.addActionListener(enter);

        java.util.function.Supplier<Character> sel = () -> {
            int r = table.getSelectedRow();
            if (r < 0 || current[0] == null || r >= current[0].refs.size()) { status.setText("Select a row first."); return null; }
            return (Character) current[0].refs.get(r);
        };
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        actions.setBackground(BG);
        JButton go = mv.makeBtn("Search", search);
        go.setForeground(Theme.OK);
        actions.add(go);
        actions.add(mv.makeBtn("Open", () -> { Character c = sel.get(); if (c != null) mv.showCharacterDialog(c); }));
        actions.add(mv.makeBtn("Follow", () -> { Character c = sel.get(); if (c != null) { mv.queueInput("follow #" + c.id); status.setText("Following " + c.name + " (see the NPC Tracker)."); } }));
        actions.add(mv.makeBtn("Unfollow", () -> { Character c = sel.get(); if (c != null) mv.queueInput("unfollow #" + c.id); }));
        JButton ctl = mv.makeBtn("Control", () -> {
            Character c = sel.get();
            if (c == null) return;
            if (!confirm(mv, "Take control of " + c.name + " " + c.lastName + " (Lv." + c.lvl + ")? Your current cultivator carries on as an NPC.")) return;
            mv.queueInput("!control #" + c.id);
        });
        ctl.setForeground(Theme.GOLD);
        actions.add(ctl);
        actions.add(mv.makeBtn("Preview fight", () -> { Character c = sel.get(); if (c != null) mv.queueInput("preview #" + c.id); }));
        for (String[] a : new String[][]{{"Seek: befriend", "friend"}, {"Seek: fight", "fight"}, {"Seek: spar", "spar"}})
            actions.add(mv.makeBtn(a[0], () -> { Character c = sel.get(); if (c != null) mv.queueInput("seek " + c.id + " " + a[1]); }));
        actions.add(mv.makeBtn("Go there", () -> { Character c = sel.get(); if (c != null && c.location != null) mv.queueInput("goto " + c.location.x + " " + c.location.y); }));
        actions.add(mv.makeBtn("Copy ID", () -> { Character c = sel.get(); if (c != null) Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(String.valueOf(c.id)), null); }));
        actions.add(mv.makeBtn("Export CSV", () -> {
            if (current[0] == null) { status.setText("Search first."); return; }
            try { status.setText("Exported to " + Exporter.view("search", current[0]).getPath()); } catch (Exception ex) { status.setText("Export failed: " + ex.getMessage()); }
        }));
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) { Character c = sel.get(); if (c != null) mv.showCharacterDialog(c); } }
        });
        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(BG);
        south.add(actions, BorderLayout.NORTH); south.add(status, BorderLayout.SOUTH);
        panel.add(filters, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    // ================================================================== images
    /** Renders any component (a chart panel, the whole window) into an image. */
    static BufferedImage snapshot(Component c) {
        int w = Math.max(1, c.getWidth()), h = Math.max(1, c.getHeight());
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        try { c.paint(g); } finally { g.dispose(); }
        return img;
    }
}
