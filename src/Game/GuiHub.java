package Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** The plumbing every window shares: the title strip with its "?" help button, remembered window positions, the router that turns the text a terminal
 *  command prints (GameDriver.showGuiInfo) into the matching interactive window, and a themed fallback that renders any other text as a sheet with
 *  headings, tables and clickable people and places. Windows themselves live in GuiWindows / GuiSheets / GuiMore / GuiExtras / GuiQuests / GuiDuties / GuiCards. */
final class GuiHub {
    private GuiHub() { }

    // ------------------------------------------------------------------ help section of a window
    /** The Help section a window belongs to (see GuiHelp.open); `key` is the GuiWindows.window key, `title` its title. */
    static String helpId(String key, String title) {
        String k = key == null ? "" : key;
        if (k.startsWith("info:")) {
            String t = (title == null ? "" : title).toLowerCase();
            if (t.startsWith("commands")) return "terminal";
            if (t.contains("quest")) return "quests";
            if (t.contains("market") || t.contains("auction") || t.contains("library")) return "market";
            if (t.contains("chronicle") || t.contains("journal") || t.contains("history") || t.contains("digest")) return "chronicle";
            if (t.contains("technique")) return "techniques";
            if (t.contains("route") || t.contains("map") || t.contains("time")) return "map";
            return "terminal";
        }
        if (k.startsWith("bio")) return "stats";
        if (k.startsWith("place")) return "locations";
        if (k.startsWith("list:")) return "terminal";
        switch (k) {
            case "quests": return "quests";
            case "duties": case "control": return "duties";
            case "stats": case "run": return "stats";
            case "backpack": return "backpack";
            case "locations": return "locations";
            case "relations": case "people": return "relations";
            case "squad": return "squad";
            case "manage": return "manage";
            case "rankings": return "rankings";
            case "market": return "market";
            case "chronicle": case "notes": case "digest": return "chronicle";
            case "world": return "world";
            case "paintedmap": return "painted-map";
            case "settings": return "settings";
            case "saves": return "saves";
            case "notices": return "notifications";
            case "family": return "family";
            case "story": case "tutorial": return "start";
            case "cheats": return "cheats";
            case "keys": return "keys";
            case "here": return "map";
            case "journal": return "me";
            case "lorejournal": return "lore";
            case "concerts": return "music";
            case "preview": return "combat";
            case "browser": return "world";
            case "scripts": return "scripts";
            case "character": return "relations";
            case "main": return "start";
            default: return "start";
        }
    }

    /** One line under the title bar that says what the window is for. */
    static String hint(String key) {
        String k = key == null ? "" : key;
        if (k.startsWith("bio")) return "Charts and life events of this cultivator.";
        if (k.startsWith("place")) return "Everything known about this city or sect.";
        switch (k) {
            case "quests": return "Everything you could do, in the order of its pressing priority. Double-click a row to go or do it.";
            case "duties": case "control": return "Which chores your character does by itself, and how it answers prompts when you are not at the keys.";
            case "lorejournal": return "The Palace of the Oldest Order: what it asks of you now, the road so far, the places you have read and what they taught you.";
            case "stats": return "Your character sheet. Names, places and gear are links.";
            case "backpack": return "Gear, techniques, materials, pills, food and companions.";
            case "locations": return "Places you know. 'go' walks there, 'route' only shows the way.";
            case "relations": return "Everyone you have a bond with.";
            case "people": return "Who is in your tile. Select a row, then act on it.";
            case "squad": return "Your party and your sect.";
            case "manage": return "Your shops, the city you stand in and your sect.";
            case "rankings": return "The strongest, richest and most dangerous, and the world's charts.";
            case "market": return "The auction house, the goods market and the library of the city you stand in.";
            case "chronicle": return "The world's recorded history.";
            case "notes": return "Places and people you bookmarked.";
            case "world": return "The whole world at a glance. Click a tile.";
            case "digest": return "What happened in the world lately.";
            case "paintedmap": return "The world as a painting.";
            case "settings": return "Everything is saved the moment you change it.";
            case "saves": return "Save, load and delete named saves.";
            case "notices": return "Everything the game told you, newest first.";
            case "family": return "Family tree, relationship graph and past lives.";
            case "story": return "Your story: its chapters, the people in it and the scenes you can read again.";
            case "run": return "This run: every life, the peaks and the totals.";
            case "cheats": return "Developer tools. They change the world for good.";
            case "here": return "The tile you stand on and what you can do in it.";
            case "journal": return "Your own story so far.";
            case "concerts": return "Performances going on near you, and every piece of the musicians' repertoire.";
            case "keys": return "Every keyboard shortcut. Shortcuts are ignored while you type in a text box.";
            case "browser": return "Select, filter and sort lists of people, goods and places.";
            case "scripts": return "Hybrid mode: write, save and load the list of steps your character follows by itself.";
            case "character": return "What is public knowledge about this cultivator.";
            default: return "";
        }
    }

    /** The strip across the top of every game window: what it is for, and a "?" that opens its Help section. */
    static JComponent header(MapViewer mv, String key, String title) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setBackground(Theme.PANEL2);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.LINE), BorderFactory.createEmptyBorder(3, 10, 3, 6)));
        JLabel l = new JLabel(hint(key));
        l.setForeground(Theme.DIM);
        l.setFont(Theme.plain(12));
        p.add(l, BorderLayout.CENTER);
        p.add(helpButton(mv, helpId(key, title)), BorderLayout.EAST);
        return p;
    }

    static JButton helpButton(MapViewer mv, String id) {
        JButton b = GuiHelp.helpButton(mv, id);
        b.setName("helpButton");
        return b;
    }

    // ------------------------------------------------------------------ remembered window positions
    static String boundsKey(String key) { return key.replaceAll("[0-9_]+$", "").replace(':', '.').replace(';', ',').replace('=', '-'); }

    /** Puts a window back where the player left it last time (clamped onto a screen), when Settings.rememberWindows is on. */
    static void restoreBounds(Window w, String key) {
        if (!Settings.rememberWindows || key == null) return;
        String s = Settings.windowBounds.get(boundsKey(key));
        if (s == null) return;
        try {
            String[] p = s.split(",");
            Rectangle r = new Rectangle(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]));
            if (r.width < 200 || r.height < 120) return;
            Rectangle screens = new Rectangle();
            for (GraphicsDevice g : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) screens = screens.union(g.getDefaultConfiguration().getBounds());
            if (screens.isEmpty()) return;
            r.width = Math.min(r.width, screens.width); r.height = Math.min(r.height, screens.height);
            r.x = Math.max(screens.x, Math.min(r.x, screens.x + screens.width - Math.min(r.width, 160)));
            r.y = Math.max(screens.y, Math.min(r.y, screens.y + screens.height - 60));
            w.setBounds(r);
        } catch (RuntimeException ignored) { /* a damaged entry: keep the default placement */ }
    }

    static void noteBounds(Window w, String key) {
        if (key != null && Settings.rememberWindows && w.isShowing()) { Rectangle r = w.getBounds(); Settings.windowBounds.put(boundsKey(key), r.x + "," + r.y + "," + r.width + "," + r.height); }
    }

    /** Remembers where a window is (kept in memory while it moves, written to settings.properties when it closes). */
    static void trackBounds(Window w, String key) {
        if (key == null) return;
        Runnable note = () -> noteBounds(w, key);
        w.addComponentListener(new ComponentAdapter() {
            @Override public void componentMoved(ComponentEvent e) { note.run(); }
            @Override public void componentResized(ComponentEvent e) { note.run(); }
        });
        w.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { note.run(); }
            @Override public void windowClosed(java.awt.event.WindowEvent e) { if (Settings.rememberWindows) Settings.save(); }
        });
    }

    /** Selects the tab of the first tabbed pane in a window whose title starts with `prefix`. */
    static void selectTab(Container root, String prefix) {
        if (root == null || prefix == null) return;
        for (Component c : root.getComponents()) {
            if (c instanceof JTabbedPane) {
                JTabbedPane tp = (JTabbedPane) c;
                for (int i = 0; i < tp.getTabCount(); i++) if (tp.getTitleAt(i).startsWith(prefix)) { tp.setSelectedIndex(i); return; }
                return;
            }
            if (c instanceof Container) selectTab((Container) c, prefix);
        }
    }

    // ------------------------------------------------------------------ open a window by name (links, buttons, shortcuts)
    static void openWindow(MapViewer mv, String name) {
        if (mv == null || name == null) return;
        switch (name) {
            case "quests": GuiQuests.open(mv, null); break;
            case "alerts": GuiQuests.open(mv, "Alerts"); break;
            case "duties": GuiDuties.open(mv, "Duties"); break;
            case "policies": GuiDuties.open(mv, "Policies"); break;
            case "people": GuiCards.peopleHere(mv, 0); break;
            case "monsters": GuiCards.peopleHere(mv, 1); break;
            case "here": GuiCards.hereWindow(mv); break;
            case "journal": GuiCards.journalWindow(mv); break;
            case "keys": GuiCards.keysWindow(mv); break;
            case "manage": GuiWindows.managePanel(mv); break;
            case "shops": GuiWindows.managePanel(mv); GuiHub.selectTab(dialog("manage"), "My shops"); break;
            case "city": GuiWindows.managePanel(mv); GuiHub.selectTab(dialog("manage"), "City"); break;
            case "sect": GuiWindows.managePanel(mv); GuiHub.selectTab(dialog("manage"), "Sect"); break;
            case "market": GuiWindows.marketPanel(mv); break;
            case "auction": GuiWindows.marketPanel(mv); GuiHub.selectTab(dialog("market"), "Auction"); break;
            case "goods": GuiWindows.marketPanel(mv); GuiHub.selectTab(dialog("market"), "Goods"); break;
            case "library": GuiWindows.marketPanel(mv); GuiHub.selectTab(dialog("market"), "Library"); break;
            case "browse": mv.openBrowser(null); break;
            case "scripts": mv.openScriptEditor(); break;
            case "settings": GuiExtras.settingsWindow(mv); break;
            case "notices": GuiExtras.notificationsWindow(mv); break;
            case "pastlives": GuiExtras.familyWindow(mv); GuiHub.selectTab(dialog("family"), "Past lives"); break;
            case "help": GuiHelp.open(mv, "start"); break;
            default: GuiSheets.window(mv, name); break;
        }
    }

    static Container dialog(String key) { JDialog d = GuiWindows.find(key); return d == null ? null : d.getContentPane(); }

    // ------------------------------------------------------------------ router: terminal output -> interactive window
    /** Called on the Swing thread with the title and text a command wanted to show. True when an interactive window took it over. */
    static boolean route(MapViewer mv, String title, String content) {
        if (title == null) return false;
        if (title.startsWith("Stats")) { GuiSheets.statsWindow(mv); return true; }
        switch (title) {
            case "Quests": GuiQuests.open(mv, null); return true;
            case "Quest alerts": GuiQuests.open(mv, "Alerts"); return true;
            case "Companions": GuiMore.backpackWindow(mv, "Companions"); return true;
            case "Inventory": GuiMore.backpackWindow(mv, "Gear"); return true;
            case "Goods": GuiMore.backpackWindow(mv, "Food"); return true;
            case "Market": openWindow(mv, "goods"); return true;
            case "Auction": openWindow(mv, "auction"); return true;
            case "Library": openWindow(mv, "library"); return true;
            case "Shops": openWindow(mv, "shops"); return true;
            case "Sect": openWindow(mv, "sect"); return true;
            case "Party": GuiSheets.squadWindow(mv); return true;
            case "Here": GuiCards.hereWindow(mv); return true;
            case "Run statistics": GuiMore.runWindow(mv); return true;
            case "Lineage": openWindow(mv, "pastlives"); return true;
            case "Saves": GuiExtras.savesWindow(mv); return true;
            case "Notifications": GuiExtras.notificationsWindow(mv); return true;
            case "Family": GuiExtras.familyWindow(mv); return true;
            case "Relationships": GuiSheets.relationsWindow(mv); return true;
            case "Locations": GuiSheets.locationsWindow(mv); return true;
            case "Journal": case "Journal of": GuiCards.journalWindow(mv); return true;
            default: break;
        }
        if (title.startsWith("Journal of")) { GuiCards.journalWindow(mv); return true; }
        if (title.startsWith("Fight preview")) return false; // rendered as a card by PlayerCommands' preview hook; the text form stays the fallback
        return false;
    }

    // ------------------------------------------------------------------ themed fallback for every other piece of text
    private static final Pattern HEADING = Pattern.compile("^=+\\s*(.+?)\\s*=+\\s*$");
    private static final Pattern KEYVAL = Pattern.compile("^(\\s*)([A-Za-z][A-Za-z0-9 '/&()+-]{0,30}?):\\s+(\\S.*)$");
    private static final Pattern NUMBERED = Pattern.compile("^\\s*(\\d+)\\.\\s+(.*)$");
    private static final Pattern COORD = Pattern.compile("\\[(\\d+), (\\d+)\\]");
    private static final Pattern IDREF = Pattern.compile("#(\\d{1,7})\\b");

    /** Adds links to the places "[x, y]" and the people "#id" a text mentions. Input is already HTML-escaped. */
    static String linkify(String s) {
        Matcher m = COORD.matcher(s);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            int x = Integer.parseInt(m.group(1)), y = Integer.parseInt(m.group(2));
            m.appendReplacement(sb, "<a href='goto:" + x / 100 + ":" + y / 100 + "'>" + m.group() + "</a>");
        }
        m.appendTail(sb);
        m = IDREF.matcher(sb.toString());
        StringBuffer sb2 = new StringBuffer();
        while (m.find()) {
            int id = Integer.parseInt(m.group(1));
            Character c = id < Game.Map.characters.size() ? Game.Map.characters.get(id) : null;
            m.appendReplacement(sb2, c != null && !c.dead ? "<a href='char:" + id + "'>#" + id + "</a>" : "#" + id);
        }
        m.appendTail(sb2);
        return sb2.toString();
    }

    /** Turns the plain text of a command (=== headings ===, "Label: value" lines, "1. row" lists, indented bullets) into a themed sheet. */
    static String textHtml(String content) {
        StringBuilder sb = new StringBuilder();
        boolean table = false, first = true;
        for (String raw : (content == null ? "" : content).split("\n", -1)) {
            String line = raw.replace("\r", "");
            String t = line.trim();
            Matcher h = HEADING.matcher(t);
            if (t.isEmpty()) { if (table) { sb.append("</table>"); table = false; } sb.append("<div style='font-size:6px'>&nbsp;</div>"); continue; }
            if (h.matches()) {
                if (table) { sb.append("</table>"); table = false; }
                String title = GuiSheets.esc(h.group(1));
                sb.append(first ? "<h1>" + title + "</h1>" : "<h2>" + title.toUpperCase() + "</h2>");
                first = false;
                continue;
            }
            first = false;
            Matcher n = NUMBERED.matcher(line), kv = KEYVAL.matcher(line);
            String cells;
            if (n.matches()) cells = "<td width='6%' class='dim' align='right'>" + n.group(1) + "</td><td>" + linkify(GuiSheets.esc(n.group(2))) + "</td>";
            else if (kv.matches() && !t.startsWith("[")) cells = "<td width='26%' class='dim'>" + GuiSheets.esc(kv.group(2)) + "</td><td>" + linkify(GuiSheets.esc(kv.group(3))) + "</td>";
            else if (t.endsWith(":") && t.length() < 40) { if (table) { sb.append("</table>"); table = false; } sb.append("<h2>").append(GuiSheets.esc(t.substring(0, t.length() - 1)).toUpperCase()).append("</h2>"); continue; }
            else if (line.startsWith("  ") || line.startsWith("\t")) cells = "<td width='3%'></td><td>" + linkify(GuiSheets.esc(t)) + "</td>";
            else { if (table) { sb.append("</table>"); table = false; } sb.append("<p>").append(linkify(GuiSheets.esc(t))).append("</p>"); continue; }
            if (!table) { sb.append("<table width='100%' cellspacing='0'>"); table = true; }
            sb.append("<tr>").append(cells).append("</tr>");
        }
        if (table) sb.append("</table>");
        return GuiSheets.page(sb.toString());
    }
}
