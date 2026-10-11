package Game;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** The small interactive windows that used to be plain terminal text: Here (the tile you stand on), People here (cultivators and beasts of the tile),
 *  Journal, the fight-preview card and the keyboard-shortcut sheet. Like every GUI window they only read the world (inside GuiWindows.window's World.read)
 *  and act by queueing ordinary commands. */
final class GuiCards {
    private GuiCards() { }

    private static String col(Color c) { return Theme.hex(c); }
    private static String esc(String s) { return GuiSheets.esc(s); }
    private static String num(long v) { return String.format("%,d", v); }

    // ================================================================== Here
    static void hereWindow(MapViewer mv) {
        GuiSheets.sheet(mv, "here", "Here", 580, 660, 1500, () -> hereHtml(mv), null);
    }

    static String hereHtml(MapViewer mv) {
        Character c = mv.findPlayer();
        if (c == null || c.location == null) return GuiSheets.page("<h1>No player character</h1>");
        Area a = c.location;
        StringBuilder sb = new StringBuilder("<h1>" + esc(a.name) + "</h1>");
        String region = Regions.line(a);
        sb.append("<span class='dim'>").append(PlayerControl.coord(a)).append(a.city ? " · tile level " : " · level ").append(a.lvl).append(" · ").append(esc(String.valueOf(a.biomem)))
          .append(" · ").append(new String[]{"spring", "summer", "autumn", "winter"}[GameDriver.currentSeason]).append(", ").append(ArtMore.WEATHER_NAMES[ArtMore.weatherAt(a)])
          .append(region.isEmpty() ? "" : " · " + esc(region)).append("</span>");
        if (a.crowdHotspot) sb.append("<p><font color='").append(col(Theme.DANGER)).append("'><b>Overcrowded:</b> beasts are ambushing cultivators here. Move on.</font></p>");

        sb.append("<h2>THIS TILE</h2><table width='100%'>");
        sb.append(row("Beasts", num(a.monsters) + " (strongest Lv " + a.highestLvl + ")" + (a.monsters > 0 ? " — <a href='cmd:hunt'>Hunt</a> · <a href='cmd:monsters'>who</a>" : "")));
        sb.append(row("Herbs", num(a.herbMaterials) + (a.herbMaterials > 0 ? " — <a href='cmd:forage'>Forage</a>" : "")));
        sb.append(row("Metal", num(a.metalMaterials) + (a.metalMaterials > 0 ? " — <a href='cmd:mine'>Mine</a>" : "")));
        int here = 0;
        try { for (Character x : a.characters) if (x != null && !x.dead && x != c && x.trait != Character.Traits.Beast && x.trait != Character.Traits.Monster) here++; } catch (RuntimeException ignored) { }
        sb.append(row("Cultivators here", here + (here > 0 ? " — <a href='win:people'>People here</a>" : "")));
        if (a.bounties != null && !a.bounties.isEmpty()) sb.append(row("Bounty board", a.bounties.size() + " posted"));
        if (a.dungeonType != null) sb.append(row("Dungeon", esc(a.dungeonType.name().replace('_', ' ').toLowerCase())));
        if (a.hasHeaven()) sb.append(row("Heaven treasure", "a Heaven-Sent treasure lies here: " + a.heavenLabel()));
        if (a.spiritualVein) sb.append(row("Spirit vein", "your cultivation is faster here"));
        if (a.arrayBag != null && !a.arrayBag.isEmpty()) sb.append(row("Arrays", String.valueOf(a.arrayBag.size())));
        sb.append("</table>");

        if (a.city) {
            sb.append("<h2>CITY</h2><table width='100%'>");
            sb.append(row("Level / people", a.cityLevel() + " · population " + num(a.population)));
            sb.append(row("Lord", a.cityOwner != null ? "<a href='char:" + a.cityOwner.id + "'>" + esc(a.cityOwner.name + " " + a.cityOwner.lastName) + "</a>" + (a.cityOwner == c ? " (you)" : "") : "none — a Major Lvl 2+ cultivator can <a href='cmd:found city'>found</a> or claim it"));
            sb.append(row("Treasury", num(a.cityTreasury) + " ss"));
            sb.append(row("Shops / libraries", a.shops.size() + " of " + a.maxShopSlots() + " · " + a.libraries.size() + " libraries"));
            if (a.tournamentTimer > 0) sb.append(row("Tournament", "realms " + a.tournamentMinMajor + "-" + a.tournamentMaxMajor + ", " + a.tournamentTimer + " weeks left, " + a.tournamentRegistrants.size() + " registered"));
            sb.append(row("Do here", "<a href='cmd:sell'>Sell</a> · <a href='cmd:buygear'>Buy gear</a> · <a href='win:goods'>Goods market</a> · <a href='win:library'>Library</a> · <a href='win:auction'>Auction</a> · <a href='win:city'>Manage city</a>"));
            sb.append("</table>");
        }
        if (a.sect != null) {
            Organization s = a.sect;
            sb.append("<h2>SECT</h2><table width='100%'>");
            sb.append(row("Sect", "<a href='place:" + a.x + ":" + a.y + "'>" + esc(s.name) + "</a> · level " + s.sectLvl() + " · " + s.members.size() + " members"));
            if (s.master == null && s.isVirtual()) sb.append(row("Master", "???"));
            else if (s.master != null) sb.append(row("Master", "<a href='char:" + s.master.id + "'>" + esc(s.master.name + " " + s.master.lastName) + "</a> Lv " + s.master.lvl));
            sb.append(row("Joining", esc(s.rulesText())));
            sb.append(row("You", c.sect == s ? "a member (" + s.getRank(c) + ")" : c.sect != null ? "a member of " + esc(c.sect.name) : "not a member — <a href='cmd:join'>Join</a>"));
            sb.append("</table>");
        }

        sb.append("<h2>PEOPLE HERE</h2>");
        List<Character> who = new ArrayList<>();
        try { for (Character x : new ArrayList<>(a.characters)) if (x != null && !x.dead && x != c && x.trait != Character.Traits.Beast && x.born) who.add(x); } catch (RuntimeException ignored) { }
        who.sort((x, y) -> Integer.compare(y.lvl, x.lvl));
        if (who.isEmpty()) sb.append("<p class='dim'>Nobody else is here.</p>");
        else {
            sb.append("<table width='100%'>");
            for (int i = 0; i < Math.min(10, who.size()); i++) {
                Character x = who.get(i);
                String tag = x.trait == Character.Traits.Monster ? "<font color='" + col(Theme.DANGER) + "'>monster</font>" : "<span class='dim'>" + esc(x.affiliation) + (x.sect != null ? " · " + esc(x.sect.name) : "") + "</span>";
                sb.append("<tr><td><a href='char:").append(x.id).append("'>").append(esc(x.name + " " + x.lastName)).append("</a></td><td>Lv ").append(x.lvl).append("</td><td>").append(tag).append("</td><td align='right'><a href='cmd:talk #").append(x.id).append("'>talk</a> · <a href='cmd:spar #").append(x.id).append("'>spar</a></td></tr>");
            }
            sb.append("</table>");
            if (who.size() > 10) sb.append("<p class='dim'>… and ").append(who.size() - 10).append(" more — <a href='win:people'>People here</a></p>");
        }
        sb.append("<p><a href='win:locations'>Known places</a> · <a href='place:").append(a.x).append(":").append(a.y).append("'>Details of this place</a></p>");
        return GuiSheets.page(sb.toString());
    }

    private static String row(String k, String v) { return "<tr><td width='26%' class='dim'>" + k + "</td><td>" + v + "</td></tr>"; }

    // ================================================================== People here
    static void peopleHere(MapViewer mv, int tab) {
        JDialog d = GuiWindows.window(mv, "people", "People here", 980, 560, 1500, (dlg, refreshers) -> {
            JTabbedPane tabs = new JTabbedPane();
            tabs.setFont(GuiWindows.MONO_B);
            tabs.addTab("Cultivators", peopleTab(mv, false, refreshers));
            tabs.addTab("Monsters", peopleTab(mv, true, refreshers));
            return tabs;
        });
        if (d != null) GuiHub.selectTab(d.getContentPane(), tab == 1 ? "Monsters" : "Cultivators");
    }

    private static JComponent peopleTab(MapViewer mv, boolean monsters, List<Runnable> refreshers) {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBackground(GuiWindows.BG);
        DefaultTableModel model = GuiWindows.model();
        JTable table = GuiWindows.table(model);
        JTextField filter = new JTextField(12);
        filter.setToolTipText("Part of a name; press Enter");
        JLabel status = GuiWindows.label(" ");
        status.setForeground(GuiWindows.ACCENT);
        JEditorPane card = GuiSheets.view(mv);
        JLabel portrait = new JLabel();
        portrait.setHorizontalAlignment(SwingConstants.CENTER);
        final PlayerLists.ListView[] current = {null};
        final String[] lastCard = {""};

        final boolean[] busy = {false}; // refreshing re-selects the row, which fires the selection listener, which refreshes...: ignore that echo
        Runnable refreshBody = () -> {
            Character p = GuiWindows.player(mv);
            if (p == null || p.location == null) { status.setText("No player character."); return; }
            List<String> tok = new ArrayList<>();
            tok.add(String.valueOf(Math.max(50, p.pc.listLimit)));
            for (String w : filter.getText().trim().split("\\s+")) if (!w.isEmpty()) tok.add("name=" + w);
            PlayerLists.ListView v;
            try { v = PlayerLists.people(p, ListQuery.parse(tok.toArray(new String[0]), 0), monsters, 500); } catch (RuntimeException ex) { return; }
            if (v.error != null) { status.setText(v.error); return; }
            Object sel = null;
            int row = table.getSelectedRow();
            if (row >= 0 && current[0] != null && row < current[0].refs.size()) sel = current[0].refs.get(row);
            current[0] = v;
            model.setDataVector(v.rows.toArray(new Object[0][]), v.headers);
            if (table.getColumnCount() > 1) { table.getColumnModel().getColumn(0).setMaxWidth(36); table.getColumnModel().getColumn(1).setPreferredWidth(56); table.getColumnModel().getColumn(2).setPreferredWidth(190); }
            if (sel != null) for (int i = 0; i < v.refs.size(); i++) if (v.refs.get(i) == sel) { table.setRowSelectionInterval(i, i); break; }
            status.setText(v.rows.size() + (monsters ? " monsters" : " cultivators") + " in " + p.location.name + (monsters ? "   (wild beasts here: " + num(p.location.monsters) + ")" : ""));
            row = table.getSelectedRow();
            Character who = row >= 0 && row < v.refs.size() ? (Character) v.refs.get(row) : null;
            String html = who == null ? GuiSheets.page("<h1>" + (monsters ? "Monsters" : "People here") + "</h1><p class='dim'>Select a row to see who it is, then act on it below.</p>") : personCard(p, who);
            if (!html.equals(lastCard[0])) {
                lastCard[0] = html; card.setText(html); card.setCaretPosition(0);
                portrait.setIcon(who != null && Settings.pictures ? new ImageIcon(ArtMore.portraitCached(who, 130, 160)) : null);
            }
        };
        Runnable refresh = () -> { if (busy[0]) return; busy[0] = true; try { refreshBody.run(); } finally { busy[0] = false; } };
        refreshers.add(refresh);
        filter.addActionListener(e -> World.readSoon(200, refresh));
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) World.readSoon(200, refresh); });
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isSel, foc, r, c);
                if (!isSel) {
                    comp.setBackground(Theme.FIELD);
                    String aff = String.valueOf(t.getValueAt(r, 4)), notes = String.valueOf(t.getValueAt(r, t.getColumnCount() - 1));
                    Color fg = Theme.TEXT;
                    if (c == 2) fg = notes.contains("quest target") ? Theme.GOLD : aff.equalsIgnoreCase("evil") ? Theme.DANGER : aff.equalsIgnoreCase("good") ? Theme.OK : Theme.TEXT;
                    if (c == 7) { try { int rel = Integer.parseInt(String.valueOf(v)); fg = rel > 0 ? Theme.OK : rel < 0 ? Theme.DANGER : Theme.DIM; } catch (NumberFormatException ignored) { } }
                    comp.setForeground(fg);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                return comp;
            }
        });
        java.util.function.Supplier<Character> sel = () -> {
            int r = table.getSelectedRow();
            if (r < 0 || current[0] == null || r >= current[0].refs.size()) { status.setText("Select a row first."); return null; }
            return (Character) current[0].refs.get(r);
        };
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) { Character c = sel.get(); if (c != null) mv.showCharacterDialog(c); } }
        });
        JPanel actions = GuiWindows.flow(FlowLayout.LEFT);
        actions.add(mv.makeBtn("Open", "Open this cultivator's sheet (double-click a row does the same).", () -> { Character c = sel.get(); if (c != null) mv.showCharacterDialog(c); }));
        if (!monsters) {
            actions.add(mv.makeBtn("Talk", "Chat: may improve how they see you. Uses the turn.", () -> { Character c = sel.get(); if (c != null) mv.queueInput("talk #" + c.id); }));
            actions.add(mv.makeBtn("Spar", "A friendly fight: nobody dies. Uses the turn.", () -> { Character c = sel.get(); if (c != null) mv.queueInput("spar #" + c.id); }));
            JButton atk = mv.makeBtn("Attack", "A real fight. You are warned first if the odds are bad.", () -> { Character c = sel.get(); if (c != null) mv.queueInput("attack #" + c.id); });
            atk.setForeground(Theme.DANGER);
            actions.add(atk);
            actions.add(mv.makeBtn("Invite to party", "Ask them to join your party (they may decline).", () -> { Character c = sel.get(); if (c != null) mv.queueInput("invite #" + c.id); }));
        } else {
            JButton atk = mv.makeBtn("Attack", "Fight this one. You are warned first if the odds are bad.", () -> { Character c = sel.get(); if (c != null) mv.queueInput("attack #" + c.id); });
            atk.setForeground(Theme.DANGER);
            actions.add(atk);
            actions.add(mv.makeBtn("Hunt here", "Fight the beasts of this tile one after another.", () -> mv.queueInput("hunt")));
        }
        actions.add(mv.makeBtn("Preview fight", "Win chance, lethality and escape odds against them.", () -> { Character c = sel.get(); if (c != null) mv.queueInput("preview #" + c.id); }));
        actions.add(mv.makeBtn("Follow", "Show them in the NPC tracker and on the map.", () -> { Character c = sel.get(); if (c != null) mv.queueInput("follow #" + c.id); }));
        JPanel north = GuiWindows.flow(FlowLayout.LEFT);
        north.add(GuiWindows.label("name:")); north.add(filter);
        north.add(mv.makeBtn("Refresh", "Reload the list now.", () -> World.readSoon(300, refresh)));
        north.add(status);
        JPanel east = new JPanel(new BorderLayout());
        east.setBackground(GuiWindows.BG);
        east.add(portrait, BorderLayout.NORTH);
        JScrollPane cs = new JScrollPane(card);
        cs.getViewport().setBackground(Theme.BG);
        east.add(cs, BorderLayout.CENTER);
        east.setPreferredSize(new Dimension(300, 100));
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(table), east);
        split.setResizeWeight(0.7); split.setContinuousLayout(true); split.setBackground(GuiWindows.BG);
        panel.add(north, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    /** What the player may know about someone standing in the same tile. */
    static String personCard(Character me, Character c) {
        StringBuilder sb = new StringBuilder("<h1>" + esc(c.name + " " + c.lastName) + "</h1>");
        sb.append("<span class='dim'>").append(esc(c.getTrait())).append(" · ").append(esc(c.affiliation)).append(" · ").append(esc(c.gender)).append(" · age ").append(c.age).append("</span>");
        int mine = TextTools.power(me, me.equippedWeapon, me.equippedClothing), theirs = TextTools.power(c, c.equippedWeapon, c.equippedClothing);
        Relationship r = me.relationshipList.contains(c.id);
        sb.append("<table width='100%'>");
        sb.append(row("Realm", "Major " + c.majorLvl + " · " + GuiSheets.REALMS[Math.max(0, Math.min(10, c.majorLvl))] + " · Lv " + c.lvl));
        sb.append(row("Power", "<font color='" + col(theirs > mine ? Theme.DANGER : theirs < mine ? Theme.OK : Theme.TEXT) + "'>" + theirs + "</font> against your " + mine + (theirs > mine + 1 ? " — stronger than you" : theirs < mine - 1 ? " — weaker than you" : " — an even match")));
        if (c.sect != null) sb.append(row("Sect", esc(c.sect.name) + " · " + c.sect.getRank(c)));
        sb.append(row("Bond", r == null ? "<span class='dim'>none yet</span>" : "<font color='" + col(r.value >= 1 ? Theme.OK : r.value <= -1 ? Theme.DANGER : Theme.DIM) + "'>" + GuiSheets.relLabel(r.value) + " (" + r.value + ")</font>"));
        if (c == me.spouse) sb.append(row("Family", "your spouse"));
        if (c.repTitle != null) sb.append(row("Known as", "'" + esc(c.repTitle) + "'"));
        if (Rankings.places(c).length() > 0) sb.append(row("Ranked", esc(Rankings.places(c))));
        if (c.trait == Character.Traits.Monster) sb.append(row("Danger", "<font color='" + col(Theme.DANGER) + "'>a monster: it kills</font>"));
        sb.append("</table>");
        sb.append("<p class='dim'>#").append(c.id).append(" · weapon ").append(esc(c.equippedWeapon != null ? c.equippedWeapon.toString() : "none")).append("</p>");
        return GuiSheets.page(sb.toString());
    }

    // ================================================================== a list a command produced (list people / shop / bag / places, find)
    private static final java.util.Map<String, Object[]> listHolders = new HashMap<>();

    /** The GUI side of `list ...` / `find ...`: shows the result as a table with actions on the selected row instead of a text dump.
     *  Returns false when there is no GUI (the caller prints the text). Called on the game thread. */
    static boolean listShown(Character c, String kind, PlayerLists.ListView v, boolean plain) {
        final MapViewer mv = GameDriver.mapViewer;
        if (!GameDriver.guiEnabled || mv == null || v == null || v.error != null) return false;
        if (plain && (kind.equals("people") || kind.equals("monsters"))) { SwingUtilities.invokeLater(() -> peopleHere(mv, kind.equals("monsters") ? 1 : 0)); return true; }
        final String note = v.note;
        SwingUtilities.invokeLater(() -> showList(mv, kind, v, note));
        return true;
    }

    private static void showList(MapViewer mv, String kind, PlayerLists.ListView v, String note) {
        final String key = "list:" + kind;
        JDialog d = GuiWindows.window(mv, key, v.title, 960, 520, 3_600_000, (dlg, refreshers) -> {
            DefaultTableModel model = GuiWindows.model();
            JTable table = GuiWindows.table(model);
            JLabel status = GuiWindows.label(" ");
            status.setForeground(GuiWindows.ACCENT);
            final List<Object> refs = new ArrayList<>();
            listHolders.put(key, new Object[]{model, refs, status, table});
            java.util.function.Supplier<Object> sel = () -> {
                int r = table.getSelectedRow();
                if (r < 0 || r >= refs.size()) { status.setText("Select a row first."); return null; }
                return refs.get(r);
            };
            JPanel actions = GuiWindows.flow(FlowLayout.LEFT);
            actions.add(mv.makeBtn("Open", "Open the selected cultivator (or the place's details). Double-click a row does the same.", () -> openRef(mv, sel.get())));
            if (kind.equals("people") || kind.equals("monsters") || kind.equals("world")) {
                for (String[] a : new String[][]{{"Talk", "talk #%d", "Chat with them (same tile only). Uses the turn."}, {"Spar", "spar #%d", "A friendly fight (same tile only)."}, {"Attack", "attack #%d", "A real fight (same tile only)."},
                        {"Invite to party", "invite #%d", "Ask them to join your party (same tile only)."}, {"Preview fight", "preview #%d", "Win chance, lethality and escape odds."}, {"Seek: befriend", "seek %d friend", "Pin a quest to walk to them and befriend them, wherever they are."},
                        {"Seek: talk", "seek %d talk", "Pin a quest to walk to them and talk, wherever they are."}, {"Seek: court", "seek %d court", "Pin a quest to walk to them and court them (an unmarried adult of the other gender, not kin)."}, {"Seek: fight", "seek %d fight", "Pin a quest to walk to them and fight them."}, {"Chase", "chase #%d", "Walk after them."}, {"Follow", "follow #%d", "Show them in the NPC tracker and on the map."}})
                    actions.add(mv.makeBtn(a[0], a[2], () -> { Object o = sel.get(); if (o instanceof Character) mv.queueInput(String.format(a[1], ((Character) o).id)); }));
            } else if (kind.equals("shop")) {
                JButton buy = mv.makeBtn("Buy selected", "Buy the selected item at the shown price.", () -> { Object o = sel.get(); if (o instanceof PlayerLists.ShopItem) { PlayerLists.ShopItem si = (PlayerLists.ShopItem) o; mv.queueInput("buy " + si.shopNo + "." + si.itemNo + " " + si.item.name); } });
                buy.setForeground(Theme.OK);
                actions.add(buy);
            } else if (kind.equals("bag")) {
                actions.add(mv.makeBtn("Sell selected", "Sell the selected backpack item in this city.", () -> { Object o = sel.get(); Character p = mv.findPlayer(); if (o instanceof Item && p != null) mv.queueInput("sell " + (p.backpack.indexOf((Item) o) + 1)); }));
                actions.add(mv.makeBtn("Equip", "Wear the selected weapon or armour.", () -> { Object o = sel.get(); Character p = mv.findPlayer(); if (o instanceof Item && p != null) mv.queueInput("equip " + (p.backpack.indexOf((Item) o) + 1)); }));
            } else if (kind.equals("places")) {
                actions.add(mv.makeBtn("Go there", "Walk to the selected place.", () -> { Object o = sel.get(); if (o instanceof Area) mv.queueInput("goto " + ((Area) o).x + " " + ((Area) o).y); }));
                actions.add(mv.makeBtn("Route", "Show the walk there without moving.", () -> { Object o = sel.get(); if (o instanceof Area) mv.queueInput("route " + ((Area) o).x * 100 + " " + ((Area) o).y * 100); }));
                actions.add(mv.makeBtn("Add waypoint quest", "Pin a quest that leads there.", () -> { Object o = sel.get(); if (o instanceof Area) mv.queueInput("qnew " + ((Area) o).name.replace(' ', '_') + " " + ((Area) o).x + " " + ((Area) o).y + " 70"); }));
            }
            actions.add(mv.makeBtn("Copy ID", "Copy the character id to the clipboard.", () -> { Object o = sel.get(); if (o instanceof Character) Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(String.valueOf(((Character) o).id)), null); }));
            table.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) openRef(mv, sel.get()); } });
            JPanel p = new JPanel(new BorderLayout(4, 4));
            p.setBackground(GuiWindows.BG);
            p.add(new JScrollPane(table), BorderLayout.CENTER);
            JPanel south = new JPanel(new BorderLayout());
            south.setBackground(GuiWindows.BG);
            south.add(actions, BorderLayout.NORTH); south.add(status, BorderLayout.SOUTH);
            p.add(south, BorderLayout.SOUTH);
            return p;
        });
        d.setTitle(v.title);
        Object[] h = listHolders.get(key);
        if (h == null) return;
        DefaultTableModel model = (DefaultTableModel) h[0];
        @SuppressWarnings("unchecked") List<Object> refs = (List<Object>) h[1];
        refs.clear(); refs.addAll(v.refs);
        model.setDataVector(v.rows.toArray(new Object[0][]), v.headers);
        ((JLabel) h[2]).setText("showing " + v.rows.size() + " of " + v.total + (note == null || note.isEmpty() ? "" : "   —   " + note.replace('\n', ' ')));
        d.toFront();
    }

    private static void openRef(MapViewer mv, Object o) {
        if (o instanceof Character) mv.showCharacterDialog((Character) o);
        else if (o instanceof Area) { Area a = (Area) o; if (a.city || a.sect != null) GuiMore.placeWindow(mv, a); else mv.selectTile(a.x, a.y); }
    }

    // ================================================================== Journal
    static void journalWindow(MapViewer mv) {
        GuiSheets.sheet(mv, "journal", "Journal", 700, 700, 3000, () -> journalHtml(mv), null);
    }

    static String journalHtml(MapViewer mv) {
        Character c = mv.findPlayer();
        if (c == null) return GuiSheets.page("<h1>No player character</h1>");
        StringBuilder sb = new StringBuilder("<h1>Journal of " + esc(c.name + " " + c.lastName) + "</h1>");
        sb.append("<span class='dim'>age ").append(c.age).append(" · Lv ").append(c.lvl).append(" · ").append(c.kills).append(" kills");
        if (c.sect != null) sb.append(" · ").append(c.sect.getRank(c)).append(" of ").append(esc(c.sect.name));
        sb.append("</span>");
        sb.append("<h2>IN THE CHRONICLE</h2>");
        List<Chronicle.Entry> mine = Chronicle.about(c);
        if (mine.isEmpty()) sb.append("<p class='dim'>Nothing yet.</p>");
        else {
            sb.append("<table width='100%'>");
            for (int i = mine.size() - 1, shown = 0; i >= 0 && shown < 30; i--, shown++) {
                Chronicle.Entry e = mine.get(i);
                StringBuilder who = new StringBuilder();
                for (int k = 1; k < e.ids.length && k < 4; k++) { Character x = Character.charById(e.ids[k]); if (x != null && !x.dead) who.append(" <a href='char:").append(x.id).append("'>").append(esc(x.name)).append("</a>"); }
                sb.append("<tr><td width='22%' class='dim'>").append(e.when()).append("</td><td>").append(esc(e.text)).append(who).append("</td></tr>");
            }
            sb.append("</table>");
        }
        sb.append("<h2>RECENT NOTICES</h2>");
        List<Notifications.Note> notes = new ArrayList<>();
        synchronized (Notifications.history) {
            for (int i = Notifications.history.size() - 1; i >= 0 && notes.size() < 25; i--) { Notifications.Note x = Notifications.history.get(i); if (x.forId < 0 || x.forId == c.id) notes.add(x); }
        }
        if (notes.isEmpty()) sb.append("<p class='dim'>None.</p>");
        else {
            sb.append("<table width='100%'>");
            for (Notifications.Note x : notes) sb.append("<tr><td width='14%' class='dim'>wk ").append(x.week).append("</td><td width='16%'><font color='").append(col(GuiExtras.catColor(x.cat))).append("'>").append(esc(x.cat)).append("</font></td><td>")
                    .append(x.charId >= 0 ? "<a href='char:" + x.charId + "'>" + esc(x.text) + "</a>" : esc(x.text)).append("</td></tr>");
            sb.append("</table>");
        }
        List<int[]> life = Chronicle.bioOf(c.id);
        if (!life.isEmpty()) {
            sb.append("<h2>LIFE SO FAR</h2><table width='100%'><tr><th>Year</th><th>Level</th><th>Wealth</th><th>Friends</th><th>Kills</th></tr>");
            int step = Math.max(1, life.size() / 12);
            for (int i = 0; i < life.size(); i += step) { int[] s = life.get(i); sb.append("<tr><td>").append(s[0]).append("</td><td>").append(s[1]).append("</td><td>10^").append(String.format("%.1f", s[2] / 100.0)).append(" ss</td><td>").append(s[3]).append("</td><td>").append(s[4]).append("</td></tr>"); }
            sb.append("</table>");
        }
        sb.append("<p><a href='win:biography'>Charts and the full biography</a> · <a href='win:chronicle'>The world's chronicle</a></p>");
        return GuiSheets.page(sb.toString());
    }

    // ================================================================== fight preview card
    private static final java.util.Map<String, JEditorPane> panes = new HashMap<>();

    /** Called on the game thread by the `preview` command in the GUI: the card is built here (the numbers are stable) and shown on the Swing thread. */
    static void previewWindow(Character.FightPreview fp) {
        final MapViewer mv = GameDriver.mapViewer;
        if (mv == null || fp == null) return;
        final Character me = fp.me, opp = fp.opp;
        final String html = previewHtml(fp);
        final boolean same = me.location == opp.location;
        final int id = opp.id;
        SwingUtilities.invokeLater(() -> {
            JDialog d = GuiWindows.window(mv, "preview", "Fight preview", 520, 520, 3_600_000, (dlg, refreshers) -> {
                JEditorPane ep = GuiSheets.view(mv);
                panes.put("preview", ep);
                JPanel p = new JPanel(new BorderLayout());
                p.setBackground(Theme.PANEL);
                JScrollPane sp = new JScrollPane(ep);
                sp.getViewport().setBackground(Theme.BG);
                JPanel b = GuiWindows.flow(FlowLayout.LEFT);
                b.setName("previewButtons");
                p.add(sp, BorderLayout.CENTER);
                p.add(b, BorderLayout.SOUTH);
                dlg.getRootPane().putClientProperty("buttons", b);
                return p;
            });
            JEditorPane ep = panes.get("preview");
            if (ep != null) { ep.setText(html); ep.setCaretPosition(0); }
            Object bp = d.getRootPane().getClientProperty("buttons");
            if (bp instanceof JPanel) {
                JPanel b = (JPanel) bp;
                b.removeAll();
                if (same) {
                    b.add(mv.makeBtn("Spar", "A friendly fight with them: nobody dies.", () -> mv.queueInput("spar #" + id)));
                    JButton atk = mv.makeBtn("Attack", "A real fight.", () -> mv.queueInput("attack #" + id));
                    atk.setForeground(Theme.DANGER);
                    b.add(atk);
                    b.add(mv.makeBtn("Talk", "Chat with them.", () -> mv.queueInput("talk #" + id)));
                } else {
                    b.add(mv.makeBtn("Chase", "Walk after them (the quest log's Go does the same for quest targets).", () -> mv.queueInput("chase #" + id)));
                    b.add(mv.makeBtn("Seek: talk", "Pin a quest that walks to them and talks.", () -> mv.queueInput("seek " + id + " talk")));
                    b.add(mv.makeBtn("Seek: court", "Pin a quest that walks to them and courts them.", () -> mv.queueInput("seek " + id + " court")));
                    b.add(mv.makeBtn("Seek: fight", "Pin a quest that walks to them and fights.", () -> mv.queueInput("seek " + id + " fight")));
                    b.add(mv.makeBtn("Seek: spar", "Pin a quest that walks to them and spars.", () -> mv.queueInput("seek " + id + " spar")));
                }
                b.add(mv.makeBtn("Open", "Open their sheet.", () -> { Character c = Character.charById(id); if (c != null) mv.showCharacterDialog(c); }));
                Ui.scale(b, Settings.fontScale);
                b.revalidate(); b.repaint();
            }
            d.toFront();
        });
    }

    static String previewHtml(Character.FightPreview fp) {
        Character me = fp.me, opp = fp.opp;
        int max = Math.max(1, Math.max(fp.myPower, fp.oppPower));
        StringBuilder sb = new StringBuilder("<h1>You against " + esc(opp.name + " " + opp.lastName) + "</h1>");
        sb.append("<span class='dim'>Lv ").append(opp.lvl).append(" (Major ").append(opp.majorLvl).append(") · ").append(esc(opp.affiliation)).append(" · ").append(esc(opp.getTrait())).append("</span>");
        sb.append("<table width='100%'><tr><td width='16%'>You</td><td width='12%'>").append(fp.myPower).append("</td><td>").append(GuiSheets.bar(fp.myPower / (double) max, Theme.ACCENT)).append("</td></tr>");
        sb.append("<tr><td>").append(esc(opp.name)).append("</td><td>").append(fp.oppPower).append("</td><td>").append(GuiSheets.bar(fp.oppPower / (double) max, fp.oppPower > fp.myPower ? Theme.DANGER : Theme.GOLD)).append("</td></tr></table>");
        sb.append("<span class='dim'>Power = (level + armour + weapon + both techniques) / 5, plus trait bonuses").append(fp.arrowBoost ? "; the defender's array adds a bonus here" : "").append(".</span>");
        sb.append("<h2>THE ODDS</h2><table width='100%'>");
        sb.append("<tr><td width='34%'>Your chance to win</td><td width='12%'><b>~").append(fp.winChance).append("%</b></td><td>").append(GuiSheets.bar(fp.winChance / 100.0, fp.winChance >= 60 ? Theme.OK : fp.winChance >= 35 ? Theme.GOLD : Theme.DANGER)).append("</td></tr>");
        sb.append("<tr><td>The fight is lethal</td><td><b>").append(fp.deathChance >= 100 ? "certain" : "~" + fp.deathChance + "%").append("</b></td><td>").append(GuiSheets.bar(Math.min(100, fp.deathChance) / 100.0, Theme.DANGER)).append("</td></tr>");
        sb.append("<tr><td>Your chance to escape</td><td><b>~").append(fp.myFlee).append("%</b></td><td>").append(GuiSheets.bar(fp.myFlee / 100.0, Theme.ACCENT)).append("</td></tr>");
        sb.append("<tr><td>Their escape if losing</td><td><b>~").append(fp.oppFlee).append("%</b></td><td>").append(GuiSheets.bar(fp.oppFlee / 100.0, Theme.DIM)).append("</td></tr></table>");
        Relationship r = me.relationshipList.contains(opp.id);
        sb.append("<p>Bond: ").append(r == null ? "none" : GuiSheets.relLabel(r.value) + " (" + r.value + ")").append(" · their caution ").append(opp.caution);
        if (fp.deathmatch) sb.append("<br><font color='").append(col(Theme.DANGER)).append("'>A beast this strong kills.</font>");
        sb.append("</p><p class='dim'>If you win you choose to kill or spare.</p>");
        return GuiSheets.page(sb.toString());
    }

    // ================================================================== keyboard shortcuts
    /** A key drawn as a key cap. */
    static final class KeyCap extends JComponent {
        private final String text;
        KeyCap(String text) { this.text = text; setFont(Theme.mono(14).deriveFont(Font.BOLD)); }
        @Override public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(Math.max(26, fm.stringWidth(text) + 16), fm.getHeight() + 10);
        }
        @Override public Dimension getMaximumSize() { return getPreferredSize(); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g.setColor(Theme.LINE); g.fillRoundRect(0, 2, w, h - 2, 8, 8);
            g.setColor(Theme.PANEL2); g.fillRoundRect(0, 0, w, h - 3, 8, 8);
            g.setColor(Theme.GOLD); g.drawRoundRect(0, 0, w - 1, h - 4, 8, 8);
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            g.setColor(Theme.TEXT);
            g.drawString(text, (w - fm.stringWidth(text)) / 2, (h - 3 - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

    /** {section, keys joined by "|", what it does}. One source for the sheet; MapViewer.handleShortcut is the implementation. */
    static final String[][] KEYS = {
            {"MOVE AND TIME", "W|↑", "one tile north (north is up on the map)"}, {"MOVE AND TIME", "S|↓", "one tile south"}, {"MOVE AND TIME", "A|←", "one tile west"}, {"MOVE AND TIME", "D|→", "one tile east"},
            {"MOVE AND TIME", "Home|PgUp|End|PgDn", "diagonals (also the numpad 7 9 1 3)"}, {"MOVE AND TIME", "Space", "wait one turn (numpad 5)"}, {"MOVE AND TIME", ".", "watch: fast-forward until something happens"},
            {"MOVE AND TIME", "P", "pause or resume the world"}, {"MOVE AND TIME", "Ctrl|S", "save the game"}, {"MOVE AND TIME", "O", "toggle map pictures"},
            {"YOU", "I", "stats"}, {"YOU", "V", "backpack and equipment"}, {"YOU", "J", "biography and charts"}, {"YOU", "R", "relations"}, {"YOU", "F", "family and relationships"}, {"YOU", "L", "known places"}, {"YOU", "Q", "quest log"}, {"YOU", "U", "duties and policies"}, {"YOU", "T", "script editor"},
            {"WORLD", "B", "browse lists (people, goods, places)"}, {"WORLD", "M", "manage shops, city and sect"}, {"WORLD", "K", "market: auction, goods, library"}, {"WORLD", "G", "rankings and charts"}, {"WORLD", "C", "chronicle"}, {"WORLD", "X", "world overview"}, {"WORLD", "Y", "notes and bookmarks"}, {"WORLD", "N", "notifications"},
            {"HELP", "H", "the Help window"}, {"HELP", "F1", "the Help window (this sheet: the Keys button)"}};
    static final String[] MOUSE = {"Click a tile: select it and read its Tile Info.", "Double-click a tile: a large picture of the place.", "Right-click a tile: go there, route, details, note, who is there.", "Click a line of the event log: opens the first cultivator it names."};

    static void keysWindow(MapViewer mv) {
        GuiWindows.window(mv, "keys", "Keyboard shortcuts", 1020, 660, 3_600_000, (d, refreshers) -> {
            java.util.Map<String, JPanel> cards = new java.util.LinkedHashMap<>();
            for (String[] k : KEYS) {
                JPanel card = cards.get(k[0]);
                if (card == null) {
                    card = new JPanel();
                    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                    card.setBackground(Theme.PANEL);
                    card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.LINE), BorderFactory.createEmptyBorder(6, 10, 8, 10)));
                    JLabel t = new JLabel(k[0]);
                    t.setForeground(Theme.ACCENT); t.setFont(Theme.bold(13)); t.setAlignmentX(Component.LEFT_ALIGNMENT);
                    card.add(t); card.add(Box.createVerticalStrut(4));
                    cards.put(k[0], card);
                }
                JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
                line.setBackground(Theme.PANEL);
                line.setAlignmentX(Component.LEFT_ALIGNMENT);
                String[] caps = k[1].split("\\|");
                boolean combo = k[1].startsWith("Ctrl");
                for (int i = 0; i < caps.length; i++) {
                    if (i > 0) { JLabel sep = new JLabel(combo ? "+" : "/"); sep.setForeground(Theme.DIM); line.add(sep); }
                    line.add(new KeyCap(caps[i]));
                }
                JLabel desc = new JLabel("  " + k[2]);
                desc.setForeground(Theme.TEXT); desc.setFont(Theme.plain(14));
                line.add(desc);
                card.add(line);
            }
            JPanel left = new JPanel(), right = new JPanel();
            for (JPanel col : new JPanel[]{left, right}) { col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS)); col.setBackground(Theme.BG); }
            int i = 0;
            for (JPanel card : cards.values()) {
                JPanel col = i++ % 2 == 0 ? left : right;
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                col.add(card); col.add(Box.createVerticalStrut(8));
            }
            JPanel mouse = new JPanel();
            mouse.setLayout(new BoxLayout(mouse, BoxLayout.Y_AXIS));
            mouse.setBackground(Theme.PANEL);
            mouse.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.LINE), BorderFactory.createEmptyBorder(6, 10, 8, 10)));
            JLabel mt = new JLabel("MOUSE"); mt.setForeground(Theme.ACCENT); mt.setFont(Theme.bold(13)); mt.setAlignmentX(Component.LEFT_ALIGNMENT);
            mouse.add(mt);
            for (String m : MOUSE) { JLabel l = new JLabel("<html>• " + m + "</html>"); l.setForeground(Theme.TEXT); l.setFont(Theme.plain(14)); l.setAlignmentX(Component.LEFT_ALIGNMENT); mouse.add(l); }
            mouse.setAlignmentX(Component.LEFT_ALIGNMENT);
            right.add(mouse);
            JPanel grid = new JPanel(new GridLayout(1, 2, 10, 0));
            grid.setBackground(Theme.BG);
            JPanel lw = new JPanel(new BorderLayout()), rw = new JPanel(new BorderLayout());
            lw.setBackground(Theme.BG); rw.setBackground(Theme.BG);
            lw.add(left, BorderLayout.NORTH); rw.add(right, BorderLayout.NORTH);
            grid.add(lw); grid.add(rw);
            JLabel foot = new JLabel("  Shortcuts are ignored while you type in a text box.");
            foot.setForeground(Theme.DIM); foot.setFont(Theme.plain(13));
            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(Theme.BG);
            root.setBorder(BorderFactory.createEmptyBorder(8, 10, 6, 10));
            JScrollPane sp = new JScrollPane(grid);
            sp.setBorder(null); sp.getViewport().setBackground(Theme.BG); sp.getVerticalScrollBar().setUnitIncrement(16);
            root.add(sp, BorderLayout.CENTER); root.add(foot, BorderLayout.SOUTH);
            return root;
        });
    }
}
