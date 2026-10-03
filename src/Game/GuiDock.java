package Game;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/** The four dock tabs of the main window (Move, Me, Auto, World). Every button either sends the same command the terminal has, or opens a
 *  window; none of them prints its answer only to the terminal. Buttons that cannot work where you stand (Forage without herbs, Sell outside a city ...)
 *  grey out and say why in their tooltip; the Auto tab shows what the character is doing and gives a one-click way back to manual control. */
final class GuiDock {
    private GuiDock() { }

    private static JTabbedPane tabs;
    private static final List<Cond> conds = new ArrayList<>();
    private static JLabel moveMode, autoStatus;
    private static JButton moveTake, autoTake;
    private static final java.util.Map<PlayerControl.Mode, JButton> modeBtns = new java.util.EnumMap<>(PlayerControl.Mode.class);

    /** A button that is only usable under some condition (checked every tick against the player). */
    private static final class Cond {
        final JButton b; final Predicate<Character> ok; final String tip, why;
        Cond(JButton b, Predicate<Character> ok, String tip, String why) { this.b = b; this.ok = ok; this.tip = tip; this.why = why; }
    }

    /** Scroll content that follows the viewport's width (a plain panel would keep its preferred width and clip). */
    private static final class Page extends JPanel implements Scrollable {
        Page() { setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); setBackground(Theme.PANEL); setBorder(new EmptyBorder(6, 8, 8, 8)); }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 80; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    static void showTab(String title) {
        if (tabs == null) return;
        for (int i = 0; i < tabs.getTabCount(); i++) if (tabs.getTitleAt(i).equals(title)) { tabs.setSelectedIndex(i); return; }
    }

    private static JPanel group(String title, int cols, Component... cs) {
        JPanel p = new JPanel(new GridLayout(0, cols, 5, 5));
        p.setBackground(Theme.PANEL);
        for (Component c : cs) p.add(c);
        return titled(title, p);
    }

    private static JPanel titled(String title, JComponent inner) {
        JPanel w = new JPanel(new BorderLayout()) { // as high as its buttons need, as wide as the dock (BoxLayout would otherwise stretch it)
            @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, getPreferredSize().height); }
        };
        w.setBackground(Theme.PANEL);
        w.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Theme.LINE), title, 0, 0, Theme.bold(13), Theme.ACCENT),
                new EmptyBorder(2, 6, 6, 6)));
        w.add(inner, BorderLayout.CENTER);
        w.setAlignmentX(Component.LEFT_ALIGNMENT);
        return w;
    }

    private static JScrollPane scroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(new EmptyBorder(0, 0, 0, 0));
        sp.getViewport().setBackground(Theme.PANEL);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    /** A button that greys out (with the reason as its tooltip) when `ok` is false for the player. */
    private static JButton cond(MapViewer mv, String label, String tip, String why, Predicate<Character> ok, String command) {
        JButton b = mv.makeBtn(label, tip, () -> mv.queueInput(command));
        conds.add(new Cond(b, ok, tip, why));
        return b;
    }

    private static boolean someoneHere(Character p) {
        try { for (Character x : p.location.characters) if (x != null && !x.dead && x != p && x.born && x.age >= 8 && x.trait != Character.Traits.Beast) return true; } catch (RuntimeException ignored) { return true; }
        return false;
    }

    // ================================================================== the dock
    static JComponent build(MapViewer mv) {
        conds.clear();
        tabs = new JTabbedPane();
        tabs.setFont(Theme.bold(15));
        tabs.putClientProperty("JTabbedPane.tabAreaAlignment", "fill");
        tabs.putClientProperty("JTabbedPane.tabWidthMode", "equal");
        tabs.addTab("Move", moveTab(mv));
        tabs.addTab("Me", meTab(mv));
        tabs.addTab("Auto", autoTab(mv));
        tabs.addTab("World", worldTab(mv));
        tabs.setToolTipTextAt(0, "Walk, work, fight and act on your turn. Everything here is also a typed command.");
        tabs.setToolTipTextAt(1, "Your character: sheets, people, places and the quest log.");
        tabs.setToolTipTextAt(2, "Let the character live by itself: modes, chores, scripts.");
        tabs.setToolTipTextAt(3, "The world: lists, markets, rankings, history, saving and settings.");
        return tabs;
    }

    // ------------------------------------------------------------------ Move
    private static JComponent moveTab(MapViewer mv) {
        Page page = new Page();

        // who is in charge: a one-line banner with a way back to manual control
        moveMode = new JLabel(" ");
        moveMode.setFont(Theme.bold(13));
        moveTake = mv.makeBtn("◀ Take control", "Stop AUTO or the running script and take control back: the game waits for your command every turn.", () -> mv.queueInput("stop"));
        JPanel banner = new JPanel(new BorderLayout(6, 0));
        banner.setBackground(Theme.PANEL2);
        banner.setBorder(new EmptyBorder(3, 8, 3, 4));
        banner.add(moveMode, BorderLayout.CENTER); banner.add(moveTake, BorderLayout.EAST);
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        page.add(banner);
        page.add(Box.createVerticalStrut(6));

        // walk: the d-pad plus the travel helpers
        JPanel pad = new JPanel(new GridLayout(3, 3, 5, 5));
        pad.setBackground(Theme.PANEL);
        String[][] dirs = {{"NW", "nw", "Move one tile north-west (Home)"}, {"↑", "w", "Move one tile north (W or ↑). North is up on the map."}, {"NE", "ne", "Move one tile north-east (PgUp)"},
                {"←", "a", "Move one tile west (A or ←)"}, {"Wait", "x", "Pass one turn without moving (Space). The world moves one week."}, {"→", "d", "Move one tile east (D or →)"},
                {"SW", "sw", "Move one tile south-west (End)"}, {"↓", "s", "Move one tile south (S or ↓)"}, {"SE", "se", "Move one tile south-east (PgDn)"}};
        for (String[] dd : dirs) {
            JButton db = mv.makeBtn(dd[0], dd[2], () -> mv.queueInput(dd[1]));
            db.setMargin(new Insets(4, 2, 4, 2)); // the default margin leaves no room for even one arrow in a 40 px cell
            if ("↑↓←→".contains(dd[0])) db.setFont(Theme.bold(20)); // arrows are big; the diagonal arrows are missing from the UI font, so the diagonals are written NW NE SW SE
            pad.add(db);
        }
        JButton moveHere = mv.makeBtn("▶ Move to Selected Tile", "Walk to the tile selected on the map (click a tile first). One tile a turn, diagonals included.", () -> {
            int[] s = mv.selectedTile();
            if (s != null && mv.findPlayer() != null) mv.queueInput("goto " + s[0] + " " + s[1]);
        });
        JButton goQuest = mv.makeBtn("▶ Go to top quest", "Walk to the place of your first quest, or chase the person it names (the status bar's Go does the same).", () -> mv.queueInput("goquest"));
        JButton route = mv.makeBtn("Route…", "Ask for a x y and see the walk there (distance and turns) without moving.", () -> {
            String s = JOptionPane.showInputDialog(mv, "Preview the walk to x y (e.g. 1500 900):", "Route", JOptionPane.PLAIN_MESSAGE);
            if (s != null && s.trim().matches("\\d+\\s+\\d+")) mv.queueInput("route " + s.trim());
        });
        JButton watch = mv.makeBtn("Watch ▸▸", "Let time pass until something happens to you: a notice, a quest alert or an attack (up to a year). The '.' key does the same.", () -> mv.queueInput("watch"));
        JPanel stack = new JPanel(new GridLayout(4, 1, 5, 5));
        stack.setBackground(Theme.PANEL);
        stack.add(moveHere); stack.add(goQuest); stack.add(route); stack.add(watch);
        JPanel walk = new JPanel(new BorderLayout(8, 0));
        walk.setBackground(Theme.PANEL);
        walk.add(pad, BorderLayout.CENTER); walk.add(stack, BorderLayout.EAST);
        page.add(titled("Walk", walk));
        page.add(Box.createVerticalStrut(4));

        // work: one button per verb of the 'Work:' help line; they grey out when the tile cannot offer it
        JButton meditate = mv.makeBtn("Meditate", "Cultivate for a week: your cultivation grows. Same as the 'meditate' command.", () -> mv.queueInput("meditate"));
        JButton hunt = cond(mv, "Hunt", "Fight the beasts of this tile for cultivation, materials and bounties.", "No beasts here.", p -> p.location != null && p.location.monsters > 0, "hunt");
        JButton forage = cond(mv, "Forage", "Gather herbs (needed for pills, food and wine).", "No herbs here.", p -> p.location != null && p.location.herbMaterials > 0, "forage");
        JButton mine = cond(mv, "Mine", "Dig for metal (needed for weapons and armour).", "No metal here.", p -> p.location != null && p.location.metalMaterials > 0, "mine");
        JButton craft = cond(mv, "Craft", "Work your profession with the materials you carry.", "Your nature has no craft.", p -> p.trait != Character.Traits.Ghost && p.trait != Character.Traits.Beast, "craft");
        JButton rest = mv.makeBtn("Rest ×4", "Wait four turns in a row (an alert or an attack interrupts). 'wait <n>' does any number.", () -> mv.queueInput("wait 4"));
        JButton sell = cond(mv, "Sell", "Sell everything you do not use to the best shop of this city.", "You need to be in a city to sell.", p -> p.location != null && p.location.city, "sell");
        JButton buyGear = cond(mv, "Buy gear", "Buy the best weapon and armour you can afford in this city.", "No shops here.", p -> p.location != null && p.location.city && !p.location.shops.isEmpty(), "buygear");
        JButton pills = mv.makeBtn("Use pills", "Swallow the pills you carry (they raise cultivation, potential and more).", () -> mv.queueInput("consume"));
        JButton eat = cond(mv, "Eat", "Eat food from your goods: a short boost to cultivation.", "You carry no food ('make food' from herbs, or buy at the market).", p -> Life.firstOf(p, Item.ItemType.Food) != null, "eat");
        JButton drink = cond(mv, "Drink", "Drink wine from your goods.", "You carry no wine ('make wine' from herbs, or buy at the market).", p -> Life.firstOf(p, Item.ItemType.Wine) != null, "drink");
        JButton equip = mv.makeBtn("Equip best", "Wear the strongest weapon and armour from your backpack.", () -> mv.queueInput("equip"));
        page.add(group("Do here (uses the turn)", 3, meditate, hunt, forage, mine, craft, rest, sell, buyGear, pills, eat, drink, equip));
        page.add(Box.createVerticalStrut(4));

        // quests
        JButton doTop = mv.makeBtn("Do top quest", "Do the most pressing quest that can be done right where you stand (routine work is skipped). If none can, it tells you where to go.", () -> mv.queueInput("qdo"));
        JButton nextAlert = mv.makeBtn("Go to next alert", "Walk to the first quest that waits for you (see the flag in the status bar).", () -> {
            Character p = mv.findPlayer();
            if (p != null && p.pc != null) { for (Quest q : new ArrayList<>(p.pc.inbox)) if (q != null && q.uid > 0 && q.category != Quest.QuestCategory.Spot) { mv.queueInput("goquest #" + q.uid); return; } }
            GuiQuests.open(mv, "Alerts");
        });
        JButton log = mv.makeBtn("Quest Log", "Every quest with its target and priority: go there, do it, pin it, delete it (Q).", () -> GuiQuests.open(mv, null));
        page.add(group("Quests", 3, doTop, nextAlert, log));
        page.add(Box.createVerticalStrut(4));

        // people
        JButton talk = cond(mv, "Talk", "Chat with someone in your tile (picked for you; use People here to choose).", "Nobody suitable is here.", GuiDock::someoneHere, "talk");
        JButton spar = cond(mv, "Spar", "A friendly fight with someone in your tile: nobody dies.", "Nobody suitable is here.", GuiDock::someoneHere, "spar");
        JButton people = mv.makeBtn("People here", "Everyone in your tile: talk, spar, attack, invite, preview a fight.", () -> GuiCards.peopleHere(mv, 0));
        page.add(group("People", 3, talk, spar, people));
        page.add(Box.createVerticalGlue());

        // the command box and Help stay in view under the scrolling groups
        JTextField cmd = new JTextField(14);
        cmd.setToolTipText("Type any command, then Enter (Up/Down recalls earlier ones). Help lists them all.");
        cmd.putClientProperty("JTextField.placeholderText", "command…  (e.g. list people 20, preview #12)");
        final List<String> hist = new ArrayList<>();
        final int[] hi = {0};
        cmd.addActionListener(e -> {
            String s = cmd.getText().trim();
            if (!s.isEmpty()) { mv.queueInput(s); if (hist.isEmpty() || !hist.get(hist.size() - 1).equals(s)) hist.add(s); if (hist.size() > 60) hist.remove(0); }
            hi[0] = hist.size();
            cmd.setText("");
        });
        cmd.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (hist.isEmpty()) return;
                if (e.getKeyCode() == KeyEvent.VK_UP) { hi[0] = Math.max(0, hi[0] - 1); cmd.setText(hist.get(hi[0])); e.consume(); }
                else if (e.getKeyCode() == KeyEvent.VK_DOWN) { hi[0] = Math.min(hist.size(), hi[0] + 1); cmd.setText(hi[0] >= hist.size() ? "" : hist.get(hi[0])); e.consume(); }
            }
        });
        JButton help = mv.makeBtn("Help", "Open the Help window: how to play, the commands and every window explained.", () -> GuiHelp.open(mv, "start"));
        help.setForeground(Theme.ACCENT);
        JPanel cmdRow = new JPanel(new BorderLayout(6, 0));
        cmdRow.setBackground(Theme.PANEL);
        cmdRow.setBorder(new EmptyBorder(4, 8, 6, 8));
        cmdRow.add(cmd, BorderLayout.CENTER); cmdRow.add(help, BorderLayout.EAST);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.PANEL);
        root.add(scroll(page), BorderLayout.CENTER);
        root.add(cmdRow, BorderLayout.SOUTH);
        return root;
    }

    // ------------------------------------------------------------------ Me
    private static JComponent meTab(MapViewer mv) {
        Page box = new Page();
        box.add(group("Character", 3,
                mv.makeBtn("Stats", "Your character sheet: realm, fortune, gear, home and family (I). Costs no time.", () -> GuiSheets.statsWindow(mv)),
                mv.makeBtn("Backpack", "Gear to equip or sell, materials, pills (V).", () -> GuiMore.backpackWindow(mv, "Gear")),
                mv.makeBtn("Techniques", "The techniques you know, their level and mastery.", () -> GuiMore.backpackWindow(mv, "Techniques")),
                mv.makeBtn("Biography", "Charts of your level, wealth and friends and the events of your life (J).", () -> GuiMore.biographyWindow(mv, mv.findPlayer())),
                mv.makeBtn("Journal", "Your own story: what the chronicle says about you, your notices, your life so far.", () -> GuiCards.journalWindow(mv)),
                mv.makeBtn("Story", "The objectives of your story start.", () -> GuiExtras.storyWindow(mv)),
                mv.makeBtn("Oldest Order", "The Palace of the Oldest Order at Originis: what it asks of you now, the places you have found, what you have learned ('lore').", () -> GuiLore.journal(mv)),
                mv.makeBtn("Run stats", "Statistics of this run and the hall of fame.", () -> GuiMore.runWindow(mv)),
                mv.makeBtn("Edit Self", "Change your name, trait, potential, alignment or gender.", () -> mv.queueInput("e"))));
        box.add(Box.createVerticalStrut(4));
        box.add(group("People", 3,
                mv.makeBtn("Relations", "Everyone you have a bond with (R).", () -> GuiSheets.relationsWindow(mv)),
                mv.makeBtn("Family", "Your family tree and relationship graph (F).", () -> GuiExtras.familyWindow(mv)),
                mv.makeBtn("Squad & Sect", "Your party and your sect: invite, promote, recruit.", () -> GuiSheets.squadWindow(mv)),
                mv.makeBtn("Companions", "The beasts that follow you and fight beside you.", () -> GuiMore.backpackWindow(mv, "Companions")),
                mv.makeBtn("People here", "Everyone in your tile: talk, spar, attack, invite.", () -> GuiCards.peopleHere(mv, 0)),
                mv.makeBtn("Lineage", "Your past lives.", () -> GuiHub.openWindow(mv, "pastlives"))));
        box.add(Box.createVerticalStrut(4));
        box.add(group("Places and work", 3,
                mv.makeBtn("Locations", "Places you know, with distances; go or route there (L).", () -> GuiSheets.locationsWindow(mv)),
                mv.makeBtn("Here", "The tile you stand on and what you can do in it.", () -> GuiCards.hereWindow(mv)),
                mv.makeBtn("Quest Log", "Every quest with its target and priority: go there, do it, pin it, delete it (Q).", () -> GuiQuests.open(mv, null))));
        box.add(Box.createVerticalStrut(4));
        box.add(group("System", 3, mv.makeBtn("Cheats", "Developer menu: give wealth, set level, heal...", () -> GuiSheets.cheatWindow(mv))));
        return scroll(box);
    }

    // ------------------------------------------------------------------ Auto
    private static JComponent autoTab(MapViewer mv) {
        Page box = new Page();
        autoStatus = new JLabel(" ");
        autoStatus.setVerticalAlignment(SwingConstants.TOP);
        autoStatus.setForeground(Theme.TEXT);
        autoTake = mv.makeBtn("◀ Take control back", "Stop AUTO or the running script and take control back in one click: the game waits for your command every turn.", () -> mv.queueInput("stop"));
        autoTake.setFont(Theme.bold(16)); Theme.fixSymbols(autoTake, autoTake.getText()); // setFont dropped the symbol font that makes the ◀ visible
        JPanel status = new JPanel(new BorderLayout(0, 6));
        status.setBackground(Theme.PANEL);
        status.add(autoStatus, BorderLayout.CENTER);
        status.add(autoTake, BorderLayout.SOUTH);
        box.add(titled("What is my character doing?", status));
        box.add(Box.createVerticalStrut(4));

        JSpinner turns = new JSpinner(new SpinnerNumberModel(0, 0, 5200, 13));
        turns.setToolTipText("AUTO for this many turns (weeks), then control comes back. 0 = until you stop it. 52 = a year.");
        JButton manual = mv.makeBtn("Manual", "You decide every turn: the game waits for your command. This is the default.", () -> mv.queueInput("manual"));
        JButton auto = mv.makeBtn("Auto", "Your character lives like an NPC: cultivates, travels, works, fights. Quest alerts and attacks interrupt it; duties decide what it does for you.", () -> {
            int n = (Integer) turns.getValue();
            mv.queueInput(n > 0 ? "auto " + n : "auto");
        });
        JButton hybrid = mv.makeBtn("Hybrid", "Run your script (a list of steps such as goto, forage, meditate, repeat) and fall back to manual or auto when it ends. Edit it with Scripts.", () -> {
            Character p = mv.findPlayer();
            if (p != null && p.pc != null && (p.pc.script == null || p.pc.script.steps.isEmpty())) { mv.openScriptEditor(); return; }
            mv.queueInput("script run");
        });
        modeBtns.put(PlayerControl.Mode.MANUAL, manual); modeBtns.put(PlayerControl.Mode.AUTO, auto); modeBtns.put(PlayerControl.Mode.HYBRID, hybrid);
        JPanel turnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        turnRow.setBackground(Theme.PANEL);
        JLabel tl = new JLabel("Auto for"); tl.setForeground(Theme.DIM); tl.setFont(Theme.plain(13));
        JLabel tl2 = new JLabel("turns (0 = until stopped)"); tl2.setForeground(Theme.DIM); tl2.setFont(Theme.plain(13));
        turnRow.add(tl); turnRow.add(turns); turnRow.add(tl2);
        JPanel modes = new JPanel(new BorderLayout(0, 4));
        modes.setBackground(Theme.PANEL);
        JPanel mb = new JPanel(new GridLayout(1, 3, 5, 5)); mb.setBackground(Theme.PANEL);
        mb.add(manual); mb.add(auto); mb.add(hybrid);
        modes.add(mb, BorderLayout.CENTER); modes.add(turnRow, BorderLayout.SOUTH);
        box.add(titled("Mode (the lit one is active)", modes));
        box.add(Box.createVerticalStrut(4));

        box.add(group("Chores (duties)", 2,
                mv.makeBtn("Duties", "Choose which chores your character does by itself, and how it answers prompts (U).", () -> GuiDuties.open(mv, "Duties")),
                mv.makeBtn("Policies", "How it answers fights and prompts when you are not at the keys: spare or kill, flee, accept duels.", () -> GuiDuties.open(mv, "Policies")),
                mv.makeBtn("All on", "Switch every duty on: your character does all its chores itself.", () -> mv.queueInput("duties all on")),
                mv.makeBtn("All off", "Switch every duty off: you do everything yourself (alerts tell you what waits).", () -> mv.queueInput("duties all off"))));
        box.add(Box.createVerticalStrut(4));
        box.add(group("Scripts and quests", 3,
                mv.makeBtn("Scripts", "Write the steps of a hybrid-mode script (T).", mv::openScriptEditor),
                mv.makeBtn("Run script", "Run the script now (Hybrid mode).", () -> mv.queueInput("script run")),
                mv.makeBtn("Resume", "Continue the script where it stopped.", () -> mv.queueInput("script resume")),
                mv.makeBtn("Quest Log", "Every quest with its target and priority (Q).", () -> GuiQuests.open(mv, null)),
                mv.makeBtn("Alerts", "The quests that wait for you to do them yourself.", () -> GuiQuests.open(mv, "Alerts")),
                mv.makeBtn("Notices", "Everything the game told you, newest first (N).", () -> GuiExtras.notificationsWindow(mv))));
        return scroll(box);
    }

    // ------------------------------------------------------------------ World
    private static JComponent worldTab(MapViewer mv) {
        Page box = new Page();
        box.add(group("Lists and markets", 3,
                mv.makeBtn("Browse", "Select, filter and sort people, shop stock, your backpack and places; search anyone in the world (B).", () -> GuiHub.openWindow(mv, "browse")),
                mv.makeBtn("Market", "This city's yearly auction, goods market and library (K).", () -> GuiWindows.marketPanel(mv)),
                mv.makeBtn("Manage", "Your shops' prices, the city you stand in, your sect (M).", () -> GuiWindows.managePanel(mv)),
                mv.makeBtn("Rankings", "Strongest, richest, most kills, charts and rivalries (G).", () -> GuiWindows.rankingsPanel(mv))));
        box.add(Box.createVerticalStrut(4));
        box.add(group("The world", 3,
                mv.makeBtn("Overview", "The whole world on one small map: cities, sects, dungeons and where players stand (X).", () -> GuiMore.worldWindow(mv)),
                mv.makeBtn("Painted map", "The world as an ink-and-wash painting with the named seas, ranges and rivers.", () -> GuiMore.paintedMapWindow(mv)),
                mv.makeBtn("Chronicle", "The world's recorded history: deaths of legends, cities, sects, wars (C).", () -> GuiMore.chronicleWindow(mv)),
                mv.makeBtn("Digest", "What happened lately: a season, a year, five years.", () -> GuiMore.digestWindow(mv, 52)),
                mv.makeBtn("Notes", "Places and people you bookmarked (Y).", () -> GuiMore.notesWindow(mv)),
                mv.makeBtn("Time", "The date, the season, the next auction and your birthday.", () -> mv.queueInput("time"))));
        box.add(Box.createVerticalStrut(4));
        JButton saveBtn = mv.makeBtn("💾 Save", "Quick save at the start of the next turn (Ctrl+S). Every save keeps a backup of the previous copy.", () -> { GameDriver.saveRequested = true; });
        saveBtn.setForeground(Theme.OK);
        JButton exportBtn = mv.makeBtn("Export ▾", "Save the map, a screenshot, the event log, history or rankings as a file in the exports folder.", () -> { });
        exportBtn.addActionListener(e -> mv.showExportMenu(exportBtn));
        box.add(group("Game", 3,
                saveBtn,
                mv.makeBtn("Saves", "Save as, load and delete named saves.", () -> GuiExtras.savesWindow(mv)),
                mv.makeBtn("Settings", "Text size, autosave, music, notifications, confirmations, remembered windows...", () -> GuiExtras.settingsWindow(mv)),
                exportBtn,
                mv.makeBtn("Keys", "Every keyboard shortcut. (F1 opens the Help.)", () -> GuiCards.keysWindow(mv)),
                mv.makeBtn("Tutorial", "The welcome tour, page by page.", () -> GuiExtras.tutorialWindow(mv))));
        return scroll(box);
    }

    // ================================================================== refreshed every tick (inside World.read)
    static void refresh(Character p) {
        if (moveMode == null) return;
        PlayerControl pc = p == null ? null : p.pc;
        PlayerControl.Mode mode = pc == null ? PlayerControl.Mode.MANUAL : pc.mode;
        boolean manual = mode == PlayerControl.Mode.MANUAL;
        moveMode.setText(p == null ? "no player character" : manual ? "MANUAL — you act each turn" : mode == PlayerControl.Mode.AUTO ? "AUTO — acting on its own" : "HYBRID — running your script");
        moveMode.setForeground(manual ? Theme.OK : Theme.GOLD);
        moveTake.setVisible(!manual);
        autoTake.setEnabled(!manual);
        for (java.util.Map.Entry<PlayerControl.Mode, JButton> e : modeBtns.entrySet())
            e.getValue().putClientProperty("FlatLaf.style", e.getKey() == mode ? "background:#265a52;borderColor:#68b8a0" : null);
        autoStatus.setText(p == null || pc == null ? "No player character." : describe(p, pc).replace("width:300px", "width:" + Math.max(170, (autoStatus.getParent() == null ? 340 : autoStatus.getParent().getWidth()) - 70) + "px"));
        for (Cond c : conds) {
            boolean ok;
            try { ok = p == null || c.ok.test(p); } catch (RuntimeException ex) { ok = true; }
            if (c.b.isEnabled() != ok) c.b.setEnabled(ok);
            String tip = ok ? c.tip : c.why + "  (" + c.tip + ")";
            if (!tip.equals(c.b.getToolTipText())) c.b.setToolTipText(tip);
        }
    }

    /** The Auto tab's card: the mode, what the character is busy with, and what is waiting for you. */
    static String describe(Character p, PlayerControl pc) {
        StringBuilder sb = new StringBuilder("<html><body style='width:300px'>");
        String q = p.currentQuest != null ? p.currentQuest.displayName() : null;
        switch (pc.mode) {
            case MANUAL:
                sb.append("<b>MANUAL</b> — you decide. The world waits for your command every turn.");
                break;
            case AUTO:
                sb.append("<b><font color='").append(Theme.hex(Theme.GOLD)).append("'>AUTO</font></b> — living like an NPC").append(pc.autoTurns > 0 ? ", " + pc.autoTurns + " more turns" : " until you stop it").append(".");
                sb.append("<br>Now: ").append(q != null ? esc(q) : p.movement == 'c' && p.autoX >= 0 ? "walking to [" + p.autoX * 100 + ", " + p.autoY * 100 + "]" : "cultivating and looking for something to do");
                break;
            default: {
                sb.append("<b><font color='").append(Theme.hex(Theme.GOLD)).append("'>HYBRID</font></b> — running your script");
                try {
                    PlayerScript sc = pc.script;
                    if (sc != null) {
                        sb.append(", step ").append(Math.min(sc.ip + 1, Math.max(1, sc.steps.size()))).append(" of ").append(sc.steps.size());
                        if (sc.ip >= 0 && sc.ip < sc.steps.size()) sb.append(": ").append(esc(String.valueOf(sc.steps.get(sc.ip))));
                    }
                } catch (RuntimeException ignored) { }
                sb.append(".");
            }
        }
        sb.append("<br><font color='").append(Theme.hex(Theme.DIM)).append("'>at ").append(esc(p.location == null ? "?" : p.location.name)).append(" ").append(PlayerControl.coord(p.location))
          .append(" · duties ").append(esc(pc.dutySummary())).append(" · ").append(pc.inbox.size()).append(" alerts waiting</font>");
        return sb.append("</body></html>").toString();
    }

    private static String esc(String s) { return GuiSheets.esc(s); }
}
