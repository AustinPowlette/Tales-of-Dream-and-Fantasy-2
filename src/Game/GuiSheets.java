package Game;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** The readable, clickable character windows of the Me tab (Stats, Locations, Relations), the generic non-modal info window that replaces the old
 *  modal pop-ups, the Squad & Sect window and the GUI cheat panel. The sheets are HTML in the game's ink-and-jade colours with real links:
 *  a name opens that cultivator, a place offers Go / Route / Details, a gear line opens the backpack. All of them only READ the world (inside
 *  GuiWindows.window's World.read) and send actions as commands, like every other window, and none is modal: you can keep clicking elsewhere. */
final class GuiSheets {
    private GuiSheets() { }

    static final String[] REALMS = {"Qi Cultivation", "Foundation", "Core Formation", "Soul Wandering", "Nascent Soul", "Ensoulment", "Nihility", "Ascension", "Half-Immortal", "Pseudo Immortal", "True Immortal"};

    // ------------------------------------------------------------------ html helpers
    static String esc(String s) { return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"); }

    static String hex(Color c) { return Theme.hex(c); }

    /** A progress bar as a two-cell table (the Swing HTML engine has no gradients or divs with width). */
    static String bar(double frac, Color on) {
        int pct = (int) Math.max(1, Math.min(99, Math.round(frac * 100)));
        return "<table width='100%' cellspacing='0' cellpadding='0' style='border:1px solid " + hex(Theme.LINE) + "'><tr><td width='" + pct + "%' bgcolor='" + hex(on) + "' height='9'></td><td bgcolor='" + hex(Theme.FIELD) + "' height='9'></td></tr></table>";
    }

    private static String css() {
        double k = Settings.fontScale; // the sheets follow the text-size setting (a sheet opened later is built at the new size)
        return "body { font-family: " + Theme.SERIF + ", serif; font-size: " + Math.round(15 * k) + "px; color: " + hex(Theme.TEXT) + "; background-color: " + hex(Theme.BG) + "; margin: 8px 12px; }"
                + " h1 { font-family: " + Theme.SERIF + "; font-size: " + Math.round(23 * k) + "px; color: " + hex(Theme.GOLD) + "; margin: 0; }"
                + " h2 { font-family: " + Theme.UI_FAMILY + "; font-size: " + Math.round(13 * k) + "px; color: " + hex(Theme.ACCENT) + "; margin: 14px 0 4px 0; }"
                + " a { color: " + hex(Theme.ACCENT) + "; }"
                + " p { margin: 4px 0; }"
                + " td { font-size: " + Math.round(15 * k) + "px; padding: 2px 4px; }"
                + " th { font-family: " + Theme.UI_FAMILY + "; font-size: " + Math.round(12 * k) + "px; color: " + hex(Theme.GOLD) + "; text-align: left; padding: 3px 4px; }"
                + " .dim { color: " + hex(Theme.DIM) + "; }";
    }

    static String page(String body) { return "<html><body>" + body + "</body></html>"; }

    /** The JEditorPane every sheet uses: themed CSS, links routed through `linkHandler`. */
    static JEditorPane view(MapViewer mv) {
        JEditorPane ep = new JEditorPane();
        ep.setEditable(false);
        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet ss = new StyleSheet();
        ss.addRule(css());
        kit.setStyleSheet(ss);
        ep.setEditorKit(kit);
        ep.setBackground(Theme.BG);
        ep.addHyperlinkListener(e -> {
            if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) return;
            String d = e.getDescription();
            if (d != null) link(mv, d);
        });
        return ep;
    }

    /** Link grammar: char:<id> | goto:<x>:<y> | route:<x>:<y> | place:<x>:<y> | cmd:<command> | win:<name>. x and y are tile numbers. */
    static void link(MapViewer mv, String d) {
        try {
            String[] p = d.split(":", 3);
            switch (p[0]) {
                case "char": { final Character c = Character.charById(Integer.parseInt(p[1])); if (c != null) mv.showCharacterDialog(c); break; }
                case "goto": mv.queueInput("goto " + Integer.parseInt(p[1]) * 100 + " " + Integer.parseInt(p[2]) * 100); break;
                case "route": mv.queueInput("route " + Integer.parseInt(p[1]) * 100 + " " + Integer.parseInt(p[2]) * 100); break;
                case "place": GuiMore.placeWindow(mv, Game.Map.tiles[Integer.parseInt(p[1])][Integer.parseInt(p[2])]); break;
                case "cmd": mv.queueInput(d.substring(4)); break;
                case "win": window(mv, p[1]); break;
                default: break;
            }
        } catch (RuntimeException ignored) { /* a stale link */ }
    }

    static void window(MapViewer mv, String name) {
        switch (name) {
            case "backpack": GuiMore.backpackWindow(mv, "Gear"); break;
            case "companions": GuiMore.backpackWindow(mv, "Companions"); break;
            case "family": GuiExtras.familyWindow(mv); break;
            case "squad": squadWindow(mv); break;
            case "biography": GuiMore.biographyWindow(mv, mv.findPlayer()); break;
            case "chronicle": GuiMore.chronicleWindow(mv); break;
            case "run": GuiMore.runWindow(mv); break;
            case "relations": relationsWindow(mv); break;
            case "locations": locationsWindow(mv); break;
            case "stats": statsWindow(mv); break;
            default: GuiHub.openWindow(mv, name); break; // people, quests, goods, library, auction, city ... (GuiHub knows them all)
        }
    }

    /** A sheet window: HTML supplied fresh on every refresh (scroll position kept), optional controls above it. */
    static JDialog sheet(MapViewer mv, String key, String title, int w, int h, int everyMs, Supplier<String> html, JComponent north) {
        return GuiWindows.window(mv, key, title, w, h, everyMs, (d, refreshers) -> {
            JEditorPane ep = view(mv);
            JScrollPane sp = new JScrollPane(ep);
            sp.getViewport().setBackground(Theme.BG);
            sp.getVerticalScrollBar().setUnitIncrement(18);
            final String[] last = {""};
            refreshers.add(() -> {
                String s = html.get();
                if (s.equals(last[0])) return;
                last[0] = s;
                final int pos = sp.getVerticalScrollBar().getValue();
                ep.setText(s);
                SwingUtilities.invokeLater(() -> sp.getVerticalScrollBar().setValue(pos));
            });
            JPanel p = new JPanel(new BorderLayout());
            p.setBackground(Theme.PANEL);
            if (north != null) p.add(north, BorderLayout.NORTH);
            p.add(sp, BorderLayout.CENTER);
            return p;
        });
    }

    // ------------------------------------------------------------------ Stats
    static String minorName(int m) { return m <= 3 ? "low" : m <= 6 ? "mid" : m <= 9 ? "high" : "peak"; }

    static String statsHtml(MapViewer mv) {
        Character c = mv.findPlayer();
        if (c == null) return page("<h1>No player character</h1><p class='dim'>You are spectating. Use Control on a cultivator to play.</p>");
        StringBuilder sb = new StringBuilder();
        double max = Math.max(1, c.cultivationBaseMax), frac = Math.min(1, c.cultivationBase / max);
        String realm = REALMS[Math.max(0, Math.min(10, c.majorLvl))];
        sb.append("<h1>").append(esc(c.name + " " + c.lastName)).append(c.repTitle != null ? " <span class='dim'>'" + esc(c.repTitle) + "'</span>" : "").append("</h1>");
        sb.append("<span class='dim'>").append(esc(c.getTrait())).append(" · ").append(esc(c.affiliation)).append(" · ").append(esc(c.gender)).append(" · age ").append(c.age).append(" of ").append(c.maxAge)
          .append(c.lives > 0 ? " · life " + (c.lives + 1) : "").append("</span>");
        sb.append("<table width='100%'><tr><td>Major ").append(c.majorLvl).append(" · ").append(realm).append(", ").append(minorName(c.minorLvl)).append("</td><td align='right'><font color='").append(hex(Theme.GOLD)).append("'>Lv ").append(c.lvl).append("</font></td></tr></table>");
        sb.append(bar(frac, Theme.ACCENT));
        double rate = c.cultivation * (c.fx != null ? Life.cultMult(c) : 1.0) * (c.trait == Character.Traits.SpiritVein ? 1.5 : 1.0);
        sb.append("<span class='dim'>Cultivation ").append((int) (frac * 100)).append("%");
        if (rate > 0.0001) sb.append(" — about ").append((int) Math.ceil((max - c.cultivationBase) / rate)).append(" weeks to the next level");
        sb.append("</span>");

        sb.append("<h2>FORTUNE</h2><table width='100%'>");
        sb.append(row("Potential", esc(c.potentialShown()) + (c.potentialHidden ? " (a rumour until your first breakthrough)" : "")));
        sb.append(row("Rope", esc(Reincarnation.status(c).replace("Rope: ", ""))));
        sb.append(row("Wealth", PlayerLists.wealth(c) + " spirit stones"));
        sb.append(row("Profession", "level " + c.professionShown() + " · " + c.itemsCrafted + " crafted" + (c.trait == Character.Traits.Innovative ? " · Inspiration " + c.inspiration + "/" + Character.INSPIRATION_NEEDED : "")));
        sb.append("</table>");

        Item w = c.equippedWeapon, a = c.equippedClothing;
        sb.append("<h2>COMBAT</h2><table width='100%'>");
        sb.append(row("Fighting level", TextTools.power(c, w, a) + " (level, gear and techniques; companions add " + Taming.bonus(c) + ")"));
        sb.append(row("Weapon", "<a href='win:backpack'>" + esc(w != null ? w.toString() : "none") + "</a>"));
        sb.append(row("Armor", "<a href='win:backpack'>" + esc(a != null ? a.toString() : "none") + "</a>"));
        sb.append(row("Martial", esc(c.martialTechnique != null ? c.martialTechnique.name + " · lv " + c.martialTechnique.lvl + " / " + c.martialTechnique.potential : "—") + (c.martialTechnique2 != null ? " (second: " + esc(c.martialTechnique2.name) + ")" : "")));
        sb.append(row("Movement", esc(c.movementTechnique != null ? c.movementTechnique.name + " · lv " + c.movementTechnique.lvl + " / " + c.movementTechnique.potential : "—")));
        sb.append(row("Record", c.kills + " kills"));
        sb.append("</table>");

        sb.append("<h2>HOME AND FAMILY</h2><table width='100%'>");
        if (c.sect != null) sb.append(row("Sect", "<a href='win:squad'>" + esc(c.sect.name) + "</a> · " + c.sect.getRank(c) + " · " + c.sectPoints + " points" + (c.sectMission != null ? " · mission: " + esc(c.missionLine()) : "")));
        else sb.append(row("Sect", "none"));
        if (c.location != null) sb.append(row("Place", "<a href='place:" + c.location.x + ":" + c.location.y + "'>" + esc(c.location.name) + "</a> " + PlayerControl.coord(c.location) + (c.location.city ? " · city" : "")));
        if (c.spouse != null) sb.append(row("Spouse", "<a href='char:" + c.spouse.id + "'>" + esc(c.spouse.name + " " + c.spouse.lastName) + "</a>"));
        sb.append(row("Children", c.children.size() + (c.children.isEmpty() ? "" : " — <a href='win:family'>open the family tree</a>")));
        List<Character> pets = Taming.peek(c);
        sb.append(row("Companions", pets.size() + " of " + Taming.cap(c) + (pets.isEmpty() ? "" : " — <a href='win:companions'>manage</a>")));
        sb.append("</table>");

        if (c.fx != null) {
            Life.Fx f = c.fx;
            sb.append("<h2>CONDITIONS</h2><p>").append(f.foodWeeks > 0 ? "nourished " + f.foodWeeks + "w · " : "").append(f.drunkWeeks > 0 ? "drunk " + f.drunkWeeks + "w · " : "").append(f.inspiredWeeks > 0 ? "inspired " + f.inspiredWeeks + "w · " : "")
              .append(f.poisonWeeks > 0 ? "<font color='" + hex(Theme.DANGER) + "'>POISONED (Lv " + f.poisonLvl + ") " + f.poisonWeeks + "w</font> · " : "").append(f.coat > 0 ? "poisoned blade x" + f.coat : "").append("</p>");
        }
        PlayerControl pc = c.pc;
        if (pc != null) sb.append("<h2>CONTROL</h2><p>").append(pc.mode).append(" · duties ").append(esc(pc.dutySummary())).append(c.currentQuest != null ? " · quest: " + esc(c.currentQuest.displayName()) : "").append("</p>");
        sb.append("<h2>OPEN</h2><p><a href='win:backpack'>Backpack</a> · <a href='win:biography'>Biography</a> · <a href='win:relations'>Relations</a> · <a href='win:locations'>Locations</a> · <a href='win:run'>Run statistics</a> · <a href='cmd:story'>Story</a></p>");
        return page(sb.toString());
    }

    private static String row(String k, String v) { return "<tr><td width='28%' class='dim'>" + k + "</td><td>" + v + "</td></tr>"; }

    static void statsWindow(MapViewer mv) { sheet(mv, "stats", "Stats", 520, 700, 1500, () -> statsHtml(mv), null); }

    // ------------------------------------------------------------------ Locations
    static void locationsWindow(MapViewer mv) {
        JTextField filter = new JTextField(14);
        JCheckBox cities = new JCheckBox("cities", true), sects = new JCheckBox("sects", true), dungeons = new JCheckBox("dungeons", true), other = new JCheckBox("other", true);
        JComboBox<String> sort = new JComboBox<>(new String[]{"nearest first", "name"});
        JPanel top = GuiWindows.flow(FlowLayout.LEFT);
        top.add(GuiWindows.label("Find:")); top.add(filter);
        for (JCheckBox b : new JCheckBox[]{cities, sects, dungeons, other}) { b.setBackground(Theme.PANEL); b.setForeground(Theme.TEXT); top.add(b); }
        top.add(sort);
        sheet(mv, "locations", "Locations", 700, 620, 2500, () -> {
            Character c = mv.findPlayer();
            if (c == null) return page("<h1>No player character</h1>");
            String q = filter.getText().trim().toLowerCase();
            List<Area> list = new ArrayList<>();
            for (Area a : new ArrayList<>(c.knownLocations)) {
                if (a == null) continue;
                boolean isCity = a.city, isSect = a.sect != null, isDungeon = a.dungeonType != null;
                boolean show = (isCity && cities.isSelected()) || (isSect && sects.isSelected()) || (isDungeon && dungeons.isSelected()) || (!isCity && !isSect && !isDungeon && other.isSelected());
                if (!show) continue;
                if (!q.isEmpty() && !(a.name + " " + (a.sect != null ? a.sect.name : "") + " " + a.biomem).toLowerCase().contains(q)) continue;
                list.add(a);
            }
            if (sort.getSelectedIndex() == 1) list.sort((x, y) -> x.name.compareToIgnoreCase(y.name));
            else list.sort((x, y) -> Integer.compare(PlayerControl.distance(c.location, x), PlayerControl.distance(c.location, y)));
            StringBuilder sb = new StringBuilder("<h1>Known places</h1><span class='dim'>" + list.size() + " of " + c.knownLocations.size() + " shown · you are at " + esc(c.location.name) + " " + PlayerControl.coord(c.location) + "</span>");
            sb.append("<table width='100%'><tr><th>Place</th><th>Where</th><th>Tiles</th><th>What</th><th></th></tr>");
            int shown = 0;
            for (Area a : list) {
                if (shown++ >= 300) break;
                String what = (a.city ? "city (pop " + a.population + ")" : "") + (a.sect != null ? (a.city ? " · " : "") + "sect " + esc(a.sect.name) : "") + (a.dungeonType != null ? esc(a.dungeonType.name().toLowerCase().replace('_', ' ')) : "") + (a.spiritualVein ? " · spirit vein" : "");
                if (what.isEmpty()) what = "<span class='dim'>" + esc(a.biomem) + (a.river ? ", river" : "") + "</span>";
                String name = (a.city || a.sect != null) ? "<a href='place:" + a.x + ":" + a.y + "'>" + esc(a.name) + "</a>" : esc(a.name);
                sb.append("<tr><td>").append(name).append("</td><td class='dim'>").append(PlayerControl.coord(a)).append("</td><td>").append(PlayerControl.distance(c.location, a)).append("</td><td>").append(what)
                  .append("</td><td><a href='goto:").append(a.x).append(":").append(a.y).append("'>go</a> · <a href='route:").append(a.x).append(":").append(a.y).append("'>route</a></td></tr>");
            }
            sb.append("</table>");
            return page(sb.toString());
        }, top);
    }

    // ------------------------------------------------------------------ Relations
    static String relLabel(int v) { return v >= 3 ? "best friend" : v == 2 ? "close friend" : v == 1 ? "friend" : v == 0 ? "acquaintance" : v == -1 ? "dislike" : v == -2 ? "rival" : "arch-enemy"; }

    static void relationsWindow(MapViewer mv) {
        JTextField filter = new JTextField(14);
        JComboBox<String> kind = new JComboBox<>(new String[]{"everyone", "friends", "rivals and enemies", "family"});
        JPanel top = GuiWindows.flow(FlowLayout.LEFT);
        top.add(GuiWindows.label("Find:")); top.add(filter); top.add(kind);
        sheet(mv, "relations", "Relations", 620, 640, 2500, () -> relationsHtml(mv.findPlayer(), filter.getText().trim().toLowerCase(), kind.getSelectedIndex(), 400), top);
    }

    /** The relationship table: kind 0 everyone, 1 friends, 2 rivals and enemies, 3 family; q filters by name. A bar shows how strong each bond is. */
    static String relationsHtml(Character c, String q, int k, int limit) {
        if (c == null) return page("<h1>No player character</h1>");
        StringBuilder sb = new StringBuilder("<h1>Relationships</h1>");
        if (c.spouse != null) sb.append("<span class='dim'>Spouse: </span><a href='char:" + c.spouse.id + "'>" + esc(c.spouse.name + " " + c.spouse.lastName) + "</a>  ");
        if (c.mother != null) sb.append("<span class='dim'>Mother: </span><a href='char:" + c.mother.id + "'>" + esc(c.mother.name) + "</a>  ");
        if (c.father != null) sb.append("<span class='dim'>Father: </span><a href='char:" + c.father.id + "'>" + esc(c.father.name) + "</a>  ");
        sb.append("<table width='100%'><tr><th>Name</th><th>Bond</th><th width='22%'></th><th>Lv</th><th>Where</th></tr>");
        List<Relationship> rels = new ArrayList<>(c.relationshipList.relationships);
        rels.sort((x, y) -> Integer.compare(y.value, x.value));
        int shown = 0;
        for (Relationship r : rels) {
            Character f = Character.charById(r.friendID);
            if (f == null || f.dead) continue;
            boolean fam = f == c.spouse || f == c.mother || f == c.father || c.children.contains(f) || c.siblings.contains(f);
            if (k == 1 && r.value < 1) continue;
            if (k == 2 && r.value > -1) continue;
            if (k == 3 && !fam) continue;
            String nm = f.name + " " + f.lastName;
            if (!q.isEmpty() && !nm.toLowerCase().contains(q)) continue;
            if (shown++ >= limit) break;
            Color colr = r.value >= 1 ? Theme.OK : r.value <= -1 ? Theme.DANGER : Theme.DIM;
            sb.append("<tr><td><a href='char:").append(f.id).append("'>").append(esc(nm)).append("</a>").append(fam ? " <span class='dim'>(family)</span>" : "").append("</td><td><font color='").append(hex(colr)).append("'>").append(relLabel(r.value)).append(" (").append(r.value).append(")</font></td><td>")
              .append(r.value == 0 ? "" : bar(Math.max(0.06, Math.min(1, Math.abs(r.value) / 3.0)), colr)).append("</td><td>")
              .append(f.lvl).append("</td><td class='dim'>").append(f.location == null ? "?" : esc(f.location.name)).append("</td></tr>");
        }
        sb.append("</table>");
        if (shown == 0) sb.append("<p class='dim'>Nobody matches. Talk, spar and travel to make friends — and rivals.</p>");
        return page(sb.toString());
    }

    // ------------------------------------------------------------------ the generic info window (replaces the modal pop-ups)
    private static final java.util.Map<String, JEditorPane> infoPanes = new java.util.HashMap<>();

    /** Shows what a command printed. Most of it opens an interactive window (GuiHub.route: quests, people, here, market, family ...); the rest comes out
     *  as a themed sheet (headings, tables, links to the people and places it names) in a non-modal window, one per title. */
    static void infoWindow(String title, String content) {
        final MapViewer mv = GameDriver.mapViewer;
        if (mv == null) return;
        SwingUtilities.invokeLater(() -> {
            try { if (GuiHub.route(mv, title, content)) return; } catch (RuntimeException ex) { /* fall back to the sheet */ }
            String key = "info:" + title;
            JDialog d = GuiWindows.window(mv, key, title, 700, 560, 3_600_000, (dlg, refreshers) -> {
                JEditorPane ep = view(mv);
                infoPanes.put(key, ep);
                JScrollPane sp = new JScrollPane(ep);
                sp.getViewport().setBackground(Theme.BG);
                sp.getVerticalScrollBar().setUnitIncrement(18);
                return sp;
            });
            JEditorPane ep = infoPanes.get(key);
            if (ep != null) {
                String html = GuiHub.textHtml(content);
                if (title.equals("Commands")) html = html.replace("</body>", "<p><a href='win:help'>Open the full Help window</a> · <a href='win:keys'>Keyboard shortcuts</a></p></body>");
                ep.setText(html); ep.setCaretPosition(0);
            }
            d.toFront();
        });
    }

    // ------------------------------------------------------------------ Squad & sect
    static void squadWindow(MapViewer mv) {
        GuiWindows.window(mv, "squad", "Squad & Sect", 820, 760, 2000, (d, refreshers) -> {
            DefaultTableModel pm = GuiWindows.model(), sm = GuiWindows.model();
            JTable pt = GuiWindows.table(pm), st = GuiWindows.table(sm);
            final List<Character> partyRows = new ArrayList<>(), sectRows = new ArrayList<>();
            JLabel partyHead = GuiWindows.label(" ");
            partyHead.setFont(GuiWindows.MONO_B);
            GuiWindows.rankCells(st);
            JComponent sectBanner = GuiPages.pane(mv, refreshers, () -> {
                Character c = GuiWindows.player(mv);
                if (c == null) return page("<h1>No player character</h1>");
                return GuiPages.sectHtml(c.sect != null ? c.sect : c.location == null ? null : c.location.sect, c, false);
            });
            sectBanner.setPreferredSize(new Dimension(400, 190));
            refreshers.add(() -> {
                Character c = GuiWindows.player(mv);
                if (c == null) { partyHead.setText("No player character."); return; }
                // squad: the leader and everyone in the party (parties share a tile), plus companion beasts
                Character leader = c.partyLeaderId >= 0 ? Character.charById(c.partyLeaderId) : c;
                if (leader == null || leader.dead) leader = c;
                List<Object[]> rows = new ArrayList<>();
                partyRows.clear();
                List<Character> members = new ArrayList<>();
                members.add(leader);
                if (c.location != null) for (Character x : new ArrayList<>(c.location.characters)) if (x != null && !x.dead && x != leader && x.partyLeaderId == leader.id) members.add(x);
                boolean inParty = members.size() > 1;
                for (Character x : members) { partyRows.add(x); rows.add(new Object[]{x.name + " " + x.lastName + (x == c ? "  (you)" : ""), x == leader ? "leader" : "member", x.lvl, x.getTrait(), x.location == null ? "?" : x.location.name}); }
                for (Character b : Taming.peek(c)) { partyRows.add(null); rows.add(new Object[]{b.name + " " + b.lastName, "companion beast", b.lvl, "Beast", b.location == null ? "?" : b.location.name}); }
                pm.setDataVector(rows.toArray(new Object[0][]), new String[]{"Name", "Role", "Lv", "Trait", "Where"});
                pt.getColumnModel().getColumn(0).setPreferredWidth(240);
                partyHead.setText(inParty ? "Your squad — led by " + leader.name + " " + leader.lastName : "You are not in a squad (invite someone from the tile, or join a leader).");
                // sect
                Organization s = c.sect;
                List<Object[]> srows = new ArrayList<>();
                sectRows.clear();
                if (s != null) {
                    List<Character> mem = new ArrayList<>(s.members);
                    mem.removeIf(x -> x == null || x.dead);
                    mem.sort((x, y) -> { int rx = s.getRank(x).ordinal(), ry = s.getRank(y).ordinal(); return rx != ry ? Integer.compare(ry, rx) : Integer.compare(y.lvl, x.lvl); });
                    for (Character x : mem) { sectRows.add(x); srows.add(new Object[]{x.name + " " + x.lastName + (x == c ? "  (you)" : ""), s.getRank(x), x.lvl, x.sectPoints, x.location == null ? "?" : x.location.name, PlayerControl.distance(c.location, x.location) + " tiles"}); }
                }
                sm.setDataVector(srows.toArray(new Object[0][]), new String[]{"Name", "Rank", "Lv", "Points", "Where", "Distance"});
                st.getColumnModel().getColumn(0).setPreferredWidth(240);
            });
            java.awt.event.MouseAdapter open = new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() != 2) return;
                    JTable t = (JTable) e.getSource();
                    int r = t.getSelectedRow();
                    List<Character> src = t == pt ? partyRows : sectRows;
                    if (r >= 0 && r < src.size() && src.get(r) != null) mv.showCharacterDialog(src.get(r));
                }
            };
            pt.addMouseListener(open); st.addMouseListener(open);
            JPanel pb = GuiWindows.flow(FlowLayout.LEFT);
            pb.add(mv.makeBtn("Invite someone here…", () -> {
                String s = JOptionPane.showInputDialog(d, "Invite which cultivator in your tile? (#id, or leave empty for someone random)", "Invite", JOptionPane.PLAIN_MESSAGE);
                if (s != null) mv.queueInput(s.trim().isEmpty() ? "invite" : "invite " + s.trim());
            }));
            pb.add(mv.makeBtn("Leave squad", () -> mv.queueInput("leave")));
            pb.add(mv.makeBtn("Disband", () -> { if (GuiExtras.confirm(d, "Disband your squad?")) mv.queueInput("!disband"); }));
            pb.add(mv.makeBtn("Companions", () -> GuiMore.backpackWindow(mv, "Companions")));
            JPanel sb = GuiWindows.flow(FlowLayout.LEFT);
            sb.add(mv.makeBtn("Promote selected", () -> { int r = st.getSelectedRow(); if (r >= 0 && r < sectRows.size()) mv.queueInput("promote #" + sectRows.get(r).id); }));
            sb.add(mv.makeBtn("Recruit (random here)", () -> mv.queueInput("recruit")));
            sb.add(mv.makeBtn("Sect details", () -> { Character c = GuiWindows.player(mv); if (c != null && c.sect != null && c.sect.location != null) GuiMore.placeWindow(mv, c.sect.location); }));
            sb.add(mv.makeBtn("Recruitment rules…", () -> mv.queueInput("sect rules")));
            sb.add(mv.makeBtn("Join sect here", () -> mv.queueInput("join")));
            sb.add(mv.makeBtn("Resign", () -> { if (GuiExtras.confirm(d, "Leave your sect?")) mv.queueInput("!resign"); }));
            JPanel top = new JPanel(new BorderLayout(4, 4)); top.setBackground(GuiWindows.BG);
            top.add(partyHead, BorderLayout.NORTH); top.add(new JScrollPane(pt), BorderLayout.CENTER); top.add(pb, BorderLayout.SOUTH);
            JPanel bottom = new JPanel(new BorderLayout(4, 4)); bottom.setBackground(GuiWindows.BG);
            bottom.add(sectBanner, BorderLayout.NORTH); bottom.add(new JScrollPane(st), BorderLayout.CENTER); bottom.add(sb, BorderLayout.SOUTH);
            JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
            split.setResizeWeight(0.3); split.setBackground(GuiWindows.BG);
            return split;
        });
    }

    // ------------------------------------------------------------------ cheats (the old menu asked its questions in the terminal)
    static void cheatWindow(MapViewer mv) {
        GuiWindows.window(mv, "cheats", "Cheats", 520, 460, 60000, (d, refreshers) -> {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBackground(GuiWindows.BG);
            p.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            JTextField target = new JTextField("self", 8);
            JPanel tp = GuiWindows.flow(FlowLayout.LEFT);
            tp.add(GuiWindows.label("Target:")); tp.add(target); tp.add(GuiWindows.label("(self, or #id of anyone — see Search / People)"));
            p.add(tp);
            String[][] rows = {
                    {"Add spirit stones", "ss", "10000"}, {"Set major level (0-10)", "level", "3"}, {"Set potential (1-100)", "potential", "100"}, {"Set kill count", "kills", "100"}, {"Give a weapon of level (1-10)", "weapon", "5"}};
            for (String[] r : rows) {
                JTextField val = new JTextField(r[2], 8);
                JPanel line = GuiWindows.flow(FlowLayout.LEFT);
                line.add(mv.makeBtn(r[0], () -> mv.queueInput("cheat " + target.getText().trim() + " " + r[1] + " " + val.getText().trim())));
                line.add(val);
                p.add(line);
            }
            JPanel simple = GuiWindows.flow(FlowLayout.LEFT);
            simple.add(mv.makeBtn("Restore cultivation", () -> mv.queueInput("cheat " + target.getText().trim() + " heal")));
            simple.add(mv.makeBtn("Clear quests", () -> mv.queueInput("cheat " + target.getText().trim() + " reset")));
            simple.add(mv.makeBtn("Track", () -> mv.queueInput("cheat " + target.getText().trim() + " track")));
            p.add(simple);
            JLabel note = GuiWindows.label("Cheats change the world for good. They run as the 'cheat' command, so the log shows what happened.");
            note.setForeground(Theme.DIM);
            p.add(note);
            return p;
        });
    }
}
