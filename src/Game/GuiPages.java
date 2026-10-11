package Game;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** The HTML pages of the sheet windows (the same ink-and-jade sheets as Stats, Relations and Journal): family list, past lives, biography, run statistics and
 *  hall of fame, story, hall of legends, digest, auction, city, sect, place details and the world dashboard. Each builder only READS the world (it runs inside
 *  GuiWindows.window's World.read) and returns a page for GuiSheets.view; links use the grammar of GuiSheets.link (char:, goto:, place:, win:, cmd:). */
final class GuiPages {
    private GuiPages() { }

    // ------------------------------------------------------------------ building blocks
    static String esc(String s) { return GuiSheets.esc(s); }

    static String hex(Color c) { return Theme.hex(c); }

    static String px(int n) { return Math.round(n * Settings.fontScale) + "px"; }

    static String tag(String text, Color c) { return "<font color='" + hex(c) + "'><b>" + esc(text) + "</b></font>"; }

    static String dim(String text) { return "<span class='dim'>" + esc(text) + "</span>"; }

    static String name(Character c) { return c == null ? "?" : esc(c.name + " " + c.lastName); }

    static String link(Character c) { return c == null ? "?" : "<a href='char:" + c.id + "'>" + name(c) + "</a>"; }

    static String placeLink(Area a) { return a == null ? "?" : (a.city || a.sect != null) ? "<a href='place:" + a.x + ":" + a.y + "'>" + esc(a.name) + "</a>" : esc(a.name); }

    /** A row of tiles: {label, value} or {label, value, colour}. perRow tiles to a line. */
    static String tiles(int perRow, String[]... items) {
        StringBuilder sb = new StringBuilder("<table width='100%' cellspacing='6' cellpadding='8'>");
        int pct = 100 / perRow;
        for (int i = 0; i < items.length; i += perRow) {
            sb.append("<tr>");
            for (int j = 0; j < perRow; j++) {
                if (i + j >= items.length) { sb.append("<td width='" + pct + "%'></td>"); continue; }
                String[] t = items[i + j];
                String color = t.length > 2 && t[2] != null ? t[2] : hex(Theme.TEXT);
                sb.append("<td width='").append(pct).append("%' bgcolor='").append(hex(Theme.PANEL2)).append("'><span class='dim' style='font-size:").append(px(11)).append("'>").append(esc(t[0].toUpperCase())).append("</span><br><span style='font-size:")
                  .append(px(19)).append(";color:").append(color).append("'><b>").append(t[1]).append("</b></span></td>");
            }
            sb.append("</tr>");
        }
        return sb.append("</table>").toString();
    }

    static String[] tile(String label, Object value) { return new String[]{label, esc(String.valueOf(value))}; }

    static String[] tile(String label, Object value, Color c) { return new String[]{label, esc(String.valueOf(value)), hex(c)}; }

    static String year(int week) { return "year " + week / 52; }

    static Color kindColor(String kind) {
        if (kind == null) return Theme.DIM;
        switch (kind) {
            case "death": case "war": case "fight": return Theme.DANGER;
            case "breakthrough": case "level": return Theme.OK;
            case "city": case "sect": case "legend": return Theme.GOLD;
            case "reincarnation": case "family": return Theme.ACCENT;
            default: return Theme.DIM;
        }
    }

    /** A scrolling sheet pane that re-reads its page on every refresh (the scroll position is kept). */
    static JComponent pane(MapViewer mv, List<Runnable> refreshers, Supplier<String> html) {
        JEditorPane ep = GuiSheets.view(mv);
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
        return sp;
    }

    private static String noPlayer() { return GuiSheets.page("<h1>No player character</h1><p class='dim'>You are spectating. Use Control on a cultivator to play.</p>"); }

    // ------------------------------------------------------------------ family list
    static String familyListHtml(Character p) {
        if (p == null) return noPlayer();
        Family.Tree t = Family.build(p, 3, 3);
        int living = 0, gone = 0;
        for (List<Family.Person> r : t.rows) for (Family.Person x : r) if (x != t.self) { if (x.dead) gone++; else living++; }
        StringBuilder sb = new StringBuilder("<h1>Family of ").append(name(p)).append("</h1>");
        if (living + gone == 0) return GuiSheets.page(sb.append("<p class='dim'>No known family yet. Marry, have children, or find your parents: they will all appear here, with a link to each.</p>").toString());
        sb.append(dim(living + " living, " + gone + " gone"));
        for (int r = 0; r < t.rows.size(); r++) {
            int g = r - t.selfGen;
            String title = g <= -3 ? "Ancestors" : g == -2 ? "Grandparents" : g == -1 ? "Parents" : g == 0 ? "You, your spouse and siblings" : g == 1 ? "Children" : g == 2 ? "Grandchildren" : "Descendants";
            List<Family.Person> row = t.rows.get(r);
            if (row.isEmpty()) continue;
            sb.append("<h2>").append(title.toUpperCase()).append("</h2><table width='100%' cellspacing='6' cellpadding='8'>");
            for (int i = 0; i < row.size(); i += 3) {
                sb.append("<tr>");
                for (int j = 0; j < 3; j++) {
                    if (i + j >= row.size()) { sb.append("<td width='33%'></td>"); continue; }
                    Family.Person x = row.get(i + j);
                    boolean me = x == t.self;
                    sb.append("<td width='33%' bgcolor='").append(hex(me ? Theme.INK : x.dead ? Theme.FIELD : Theme.PANEL2)).append("'>");
                    sb.append(me ? "<b>" + esc(x.name) + "</b>" : "<a href='char:" + x.id + "'><b>" + esc(x.name) + "</b></a>");
                    sb.append("<br><span class='dim'>").append(esc(x.label)).append(" · Lv ").append(x.lvl).append(" · age ").append(x.age).append("</span><br>");
                    sb.append(me ? tag("you", Theme.GOLD) : x.dead ? tag("gone", Theme.DIM) : tag("alive", Theme.OK)).append("</td>");
                }
                sb.append("</tr>");
            }
            sb.append("</table>");
        }
        return GuiSheets.page(sb.toString());
    }

    // ------------------------------------------------------------------ past lives
    private static final java.util.regex.Pattern LIFE = java.util.regex.Pattern.compile("Life (\\d+): reached (.*) at age (\\d+) in year (\\d+), potential (\\d+), (\\d+) kills");

    static String pastLivesHtml(Character c) {
        if (c == null) return noPlayer();
        StringBuilder sb = new StringBuilder("<h1>Past lives of ").append(name(c)).append("</h1>");
        sb.append(dim(c.lives == 0 ? "This is your first life." : "You have lived " + c.lives + (c.lives == 1 ? " life" : " lives") + " before this one."));
        // the Rope
        sb.append("<h2>THE ROPE</h2>");
        if (c.potential >= 100) sb.append("<p>Potential ").append(c.potential).append(": ").append(tag("no gate can hold you", Theme.GOLD)).append("</p>");
        else {
            int cap = Reincarnation.cap(c.potential);
            sb.append("<table width='100%'><tr><td>Potential ").append(c.potential).append(" carries you to level <b>").append(cap).append("</b></td><td align='right'>you are at level ").append(c.lvl).append("</td></tr></table>");
            sb.append(GuiSheets.bar(cap <= 0 ? 0 : Math.min(1, c.lvl / (double) cap), Reincarnation.ready(c) ? Theme.GOLD : Theme.ACCENT));
            sb.append("<p class='dim'>").append(esc(Reincarnation.ready(c) ? "You have reached the end of the rope. 'do Reincarnate' begins a new life with +5 potential."
                    : c.majorLvl < Reincarnation.MIN_MAJOR ? "Reincarnation needs Major Lvl " + Reincarnation.MIN_MAJOR + "." : "Keep climbing: at the end of the rope you may reincarnate.")).append("</p>");
        }
        sb.append("<h2>LIVES</h2><table width='100%' cellspacing='6' cellpadding='8'>");
        if (c.pastLives != null) for (Object o : c.pastLives) {
            String line = String.valueOf(o);
            java.util.regex.Matcher m = LIFE.matcher(line);
            sb.append("<tr><td width='14%' bgcolor='").append(hex(Theme.PANEL2)).append("'>");
            if (m.matches()) sb.append(tag("Life " + m.group(1), Theme.GOLD)).append("</td><td bgcolor='").append(hex(Theme.PANEL2)).append("'><b>").append(esc(m.group(2))).append("</b><br><span class='dim'>to age ").append(m.group(3))
                    .append(", ended in year ").append(m.group(4)).append(" · potential ").append(m.group(5)).append(" · ").append(m.group(6)).append(" kills</span></td></tr>");
            else sb.append("</td><td bgcolor='").append(hex(Theme.PANEL2)).append("'>").append(esc(line)).append("</td></tr>");
        }
        sb.append("<tr><td width='14%' bgcolor='").append(hex(Theme.INK)).append("'>").append(tag("Life " + (c.lives + 1), Theme.OK)).append("<br>").append(dim("now")).append("</td><td bgcolor='").append(hex(Theme.INK)).append("'><b>")
          .append(esc(GuiSheets.REALMS[Math.max(0, Math.min(10, c.majorLvl))])).append("</b>, level ").append(c.lvl).append("<br><span class='dim'>age ").append(c.age).append(" · potential ").append(esc(c.potentialLabel())).append(" · ").append(c.kills).append(" kills</span></td></tr></table>");
        return GuiSheets.page(sb.toString());
    }

    // ------------------------------------------------------------------ biography
    static String bioHtml(Character c) {
        StringBuilder sb = new StringBuilder();
        if (c != null) {
            sb.append(tiles(3, tile("Realm", GuiSheets.REALMS[Math.max(0, Math.min(10, c.majorLvl))], Theme.GOLD), tile("Level", c.lvl), tile("Age", c.age + (c.dead ? " (dead)" : "")),
                    tile("Kills", c.kills), tile("Potential", c.player == 1 ? c.potentialLabel() : String.valueOf(c.potential)), tile("Sect", c.sect == null ? "none" : c.sect.name)));
        }
        List<Chronicle.Entry> mine = c == null ? new ArrayList<Chronicle.Entry>() : Chronicle.about(c);
        sb.append("<h2>LIFE EVENTS</h2>");
        if (mine.isEmpty()) return GuiSheets.page(sb.append("<p class='dim'>Nothing notable yet. Breakthroughs, fights, marriages and deaths of people you know are written here as they happen.</p>").toString());
        int lastYear = -1, shown = 0;
        for (int i = mine.size() - 1; i >= 0 && shown < 120; i--, shown++) {
            Chronicle.Entry e = mine.get(i);
            if (e.year() != lastYear) {
                if (lastYear >= 0) sb.append("</table>");
                sb.append("<p>").append(tag("YEAR " + e.year(), Theme.GOLD)).append("</p><table width='100%'>");
                lastYear = e.year();
            }
            StringBuilder who = new StringBuilder();
            for (int k = 1; k < e.ids.length && k < 4; k++) { Character x = Character.charById(e.ids[k]); if (x != null && !x.dead) who.append(" <a href='char:").append(x.id).append("'>").append(esc(x.name)).append("</a>"); }
            sb.append("<tr><td width='17%'>").append(tag(e.kind.toUpperCase(), kindColor(e.kind))).append("</td><td>").append(esc(e.text)).append(who).append("</td></tr>");
        }
        sb.append("</table>");
        return GuiSheets.page(sb.toString());
    }

    // ------------------------------------------------------------------ run statistics
    static String runHtml() {
        RunStats.Data d = RunStats.run;
        String[] dn = Starts.DIFFICULTY_NAMES;
        StringBuilder sb = new StringBuilder("<h1>Run statistics</h1>");
        sb.append(dim("Began as " + d.firstName + " (" + d.startKind + ", " + dn[Math.max(0, Math.min(dn.length - 1, d.difficulty))] + ") in year " + d.startWeek / 52 + "; "
                + (d.ended ? "ended in year " + d.endWeek / 52 + " (" + d.endNote + ")" : "still running")));
        int peakLvl = 0, peakMajor = 0, kills = 0, bestLife = 0;
        for (RunStats.Life l : d.lives) { peakLvl = Math.max(peakLvl, l.peakLvl); peakMajor = Math.max(peakMajor, l.peakMajor); kills += l.kills; bestLife = Math.max(bestLife, (l.endWeek < 0 ? GameDriver.time : l.endWeek) - l.startWeek); }
        sb.append(tiles(4, tile("Years played", RunStats.years(), Theme.GOLD), tile("Lives lived", d.lives.size()), tile("Reincarnations", d.reincarnations), tile("Longest life", bestLife / 52 + " years"),
                tile("Peak level", peakLvl + " (Major " + peakMajor + ")", Theme.OK), tile("Kills", kills), tile("Peak wealth", TextTools.si(d.peakWealth) + " ss"), tile("Deepest dungeon", d.deepestDungeon),
                tile("Companions tamed", d.tamed), tile("Sects / cities founded", d.founded), tile("Children", d.children), tile("Loved ones outlived", d.loved)));
        sb.append("<h2>LIFE BY LIFE</h2>");
        if (d.lives.isEmpty()) return GuiSheets.page(sb.append("<p class='dim'>No lives recorded yet.</p>").toString());
        sb.append("<table width='100%'><tr><th>#</th><th>Who</th><th>Years</th><th>Age</th><th>Peak</th><th>Potential</th><th>Kills</th><th>How it ended</th></tr>");
        int i = 1;
        for (RunStats.Life l : d.lives) {
            int endW = l.endWeek < 0 ? GameDriver.time : l.endWeek;
            sb.append("<tr><td class='dim'>").append(i++).append("</td><td><b>").append(esc(l.name)).append("</b></td><td>").append(l.startWeek / 52).append("–").append(endW / 52).append("</td><td>").append(l.startAge).append("→")
              .append(l.startAge + (endW - l.startWeek) / 52).append("</td><td>Lv ").append(l.peakLvl).append(" <span class='dim'>(M").append(l.peakMajor).append(")</span></td><td>").append(l.potentialStart).append("→").append(l.potentialEnd)
              .append("</td><td>").append(l.kills).append("</td><td>").append(l.open() ? tag(l.end, Theme.OK) : esc(l.end)).append("</td></tr>");
        }
        return GuiSheets.page(sb.append("</table>").toString());
    }

    static String hallHtml() {
        java.io.File f = RunStats.hallFile();
        StringBuilder sb = new StringBuilder("<h1>Hall of fame</h1>");
        if (!f.isFile()) return GuiSheets.page(sb.append("<p class='dim'>Empty. A run that ends (a Brutal death, a story ending, or Retire) is recorded here and in <b>").append(esc(f.getPath())).append("</b>.</p>").toString());
        List<String> lines = new ArrayList<>();
        try { for (String s : java.nio.file.Files.readAllLines(f.toPath(), java.nio.charset.StandardCharsets.UTF_8)) if (!s.trim().isEmpty()) lines.add(s.trim()); } catch (java.io.IOException e) { return GuiSheets.page(sb.append("<p>Could not read ").append(esc(f.getPath())).append("</p>").toString()); }
        sb.append(dim(lines.size() + " runs · " + f.getPath()));
        sb.append("<table width='100%' cellspacing='6' cellpadding='8'>");
        for (int i = lines.size() - 1; i >= 0; i--) {
            String[] parts = lines.get(i).split(" \\| ");
            boolean ending = parts.length > 0 && parts[0].equals("ENDING");
            sb.append("<tr><td bgcolor='").append(hex(Theme.PANEL2)).append("'>");
            if (ending && parts.length >= 5) sb.append(tag(parts[3], Theme.GOLD)).append("&nbsp;&nbsp;").append(dim(parts[1])).append("<br><b>").append(esc(parts[2])).append("</b><br><span class='dim'>").append(esc(joinFrom(parts, 4))).append("</span>");
            else if (parts.length >= 3) sb.append(tag(parts[0], Theme.GOLD)).append("&nbsp;&nbsp;<b>").append(esc(parts[1])).append("</b><br><span class='dim'>").append(esc(joinFrom(parts, 2))).append("</span>");
            else sb.append(esc(lines.get(i)));
            sb.append("</td></tr>");
        }
        return GuiSheets.page(sb.append("</table>").toString());
    }

    private static String joinFrom(String[] parts, int from) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < parts.length; i++) { if (sb.length() > 0) sb.append("  ·  "); sb.append(parts[i]); }
        return sb.toString();
    }

    // ------------------------------------------------------------------ hall of legends
    static String legendsHtml() {
        StringBuilder sb = new StringBuilder("<h1>Hall of legends</h1>");
        sb.append(dim("Cultivators who reached Ascension or beyond are remembered here, and the world's firsts."));
        List<String> titles = new ArrayList<>(Game.Map.legacyTitles);
        List<Integer> lvls = new ArrayList<>(Game.Map.legacyLvls);
        sb.append("<h2>THE LEGENDS</h2>");
        if (titles.isEmpty()) sb.append("<p class='dim'>None yet. The first to pass Ascension will be written here for good.</p>");
        else {
            sb.append("<table width='100%' cellspacing='6' cellpadding='9'>");
            for (int i = 0; i < titles.size(); i += 2) {
                sb.append("<tr>");
                for (int j = 0; j < 2; j++) {
                    if (i + j >= titles.size()) { sb.append("<td width='50%'></td>"); continue; }
                    int lv = i + j < lvls.size() ? lvls.get(i + j) : 0;
                    sb.append("<td width='50%' bgcolor='").append(hex(Theme.INK)).append("'>").append(dim("No. " + (i + j + 1))).append("<br><span style='font-size:").append(px(17)).append(";color:").append(hex(Theme.GOLD)).append("'><b>")
                      .append(esc(titles.get(i + j))).append("</b></span><br>").append(dim("level " + lv)).append("</td>");
                }
                sb.append("</tr>");
            }
            sb.append("</table>");
        }
        List<Chronicle.Entry> lg = Chronicle.search("", "legend", 100);
        sb.append("<h2>FIRSTS AND LEGENDS FROM THE CHRONICLE</h2>");
        if (lg.isEmpty()) sb.append("<p class='dim'>Nothing recorded yet.</p>");
        else {
            sb.append("<table width='100%'>");
            for (Chronicle.Entry e : lg) {
                StringBuilder who = new StringBuilder();
                for (int k = 0; k < e.ids.length && k < 2; k++) { Character x = Character.charById(e.ids[k]); if (x != null && !x.dead) { who.append(" <a href='char:").append(x.id).append("'>").append(esc(x.name)).append("</a>"); break; } }
                sb.append("<tr><td width='15%'>").append(tag("YEAR " + e.year(), Theme.GOLD)).append("</td><td>").append(esc(e.text)).append(who).append("</td></tr>");
            }
            sb.append("</table>");
        }
        return GuiSheets.page(sb.toString());
    }

    // ------------------------------------------------------------------ digest
    private static String delta(int then, int now) {
        int dd = now - then;
        return dd == 0 ? "<span class='dim'>no change</span>" : dd > 0 ? "<font color='" + hex(Theme.OK) + "'>▲ +" + dd + "</font>" : "<font color='" + hex(Theme.DANGER) + "'>▼ " + dd + "</font>";
    }

    private static String[] change(String label, int then, int now) {
        return new String[]{label, now + "<br><span style='font-size:" + px(13) + "'>" + delta(then, now) + "</span>"};
    }

    static String digestHtml(int weeks) {
        int from = Math.max(0, GameDriver.time - weeks);
        List<Chronicle.Entry> recent = new ArrayList<>();
        synchronized (Chronicle.log) { for (Chronicle.Entry e : Chronicle.log) if (e.week >= from) recent.add(e); }
        java.util.Map<String, Integer> counts = new java.util.TreeMap<>();
        for (Chronicle.Entry e : recent) counts.merge(e.kind, 1, Integer::sum);
        StringBuilder sb = new StringBuilder("<h1>What happened lately</h1>").append(dim("The last " + Math.max(1, weeks / 52) + " year(s), up to year " + GameDriver.time / 52));
        int[] then = null, now = null;
        synchronized (GameDriver.statHistory) { if (!GameDriver.statHistory.isEmpty()) { now = GameDriver.statHistory.get(GameDriver.statHistory.size() - 1); for (int[] h : GameDriver.statHistory) if (h[0] <= from / 52 + 1) then = h; } }
        if (now != null && then != null && then != now) {
            sb.append("<h2>HOW THE WORLD CHANGED</h2>");
            sb.append(tiles(4, change("Population", then[1], now[1]), change("Cities", then[2], now[2]), change("Sects", then[3], now[3]), change("Shops", then[4], now[4])));
        }
        sb.append("<h2>IN THE CHRONICLE</h2><p>");
        if (counts.isEmpty()) sb.append(dim("nothing was written"));
        else for (java.util.Map.Entry<String, Integer> e : counts.entrySet()) sb.append(tag(e.getValue() + " " + e.getKey(), kindColor(e.getKey()))).append("&nbsp;&nbsp;&nbsp;");
        sb.append("</p><h2>NOTABLE EVENTS</h2>");
        int shown = 0;
        StringBuilder ev = new StringBuilder();
        for (int i = recent.size() - 1; i >= 0 && shown < 30; i--) {
            Chronicle.Entry e = recent.get(i);
            if (e.kind.equals("breakthrough") && !e.text.contains("Major Lvl 5") && !e.text.contains("Major Lvl 6") && !e.text.contains("Major Lvl 7")) continue;
            ev.append("<tr><td width='13%'>").append(dim(year(e.week))).append("</td><td width='17%'>").append(tag(e.kind.toUpperCase(), kindColor(e.kind))).append("</td><td>").append(esc(e.text)).append("</td></tr>");
            shown++;
        }
        sb.append(shown == 0 ? "<p class='dim'>A quiet time.</p>" : "<table width='100%'>" + ev + "</table>");
        sb.append("<h2>YOUR NOTICES</h2>");
        int k = 0;
        StringBuilder nt = new StringBuilder();
        synchronized (Notifications.history) {
            for (int i = Notifications.history.size() - 1; i >= 0 && k < 12; i--) {
                Notifications.Note x = Notifications.history.get(i);
                if (x.week >= from) { nt.append("<tr><td width='13%'>").append(dim(year(x.week))).append("</td><td width='17%'>").append(tag(String.valueOf(x.cat).toUpperCase(), Theme.ACCENT)).append("</td><td>").append(esc(x.text)).append("</td></tr>"); k++; }
            }
        }
        sb.append(k == 0 ? "<p class='dim'>None in this time.</p>" : "<table width='100%'>" + nt + "</table>");
        return GuiSheets.page(sb.toString());
    }

    // ------------------------------------------------------------------ auction
    static String auctionHtml(Character c) {
        if (c == null) return noPlayer();
        Area a = c.location;
        StringBuilder sb = new StringBuilder("<h1>Auction at ").append(esc(a.name)).append("</h1>");
        if (!a.city) return GuiSheets.page(sb.append("<p>You are not in a city. Walk to one to see its auction house.</p>").toString());
        if (a.population < 500) return GuiSheets.page(sb.append("<p>This city is too small for an auction house: it needs 500 residents (it has " + a.population + ").</p>").toString());
        List<Item> lots = new ArrayList<>();
        if (a.auctionBag != null) for (Item i : new ArrayList<>(a.auctionBag)) if (i != null && i.itemType != null) lots.add(i);
        lots.sort((x, y) -> Long.compare(Auctions.reserve(y), Auctions.reserve(x)));
        int w = Auctions.weeksToNext(a);
        long wealth = PlayerLists.wealth(c);
        sb.append(tiles(3, tile("Lots on the block", lots.size() + " of " + Auctions.LOTS_TO_HOLD, Theme.GOLD), tile("Next auction", w < 0 ? "when lots arrive" : "in about " + w + " week" + (w == 1 ? "" : "s")), tile("Your wealth", wealth + " ss", Theme.OK)));
        sb.append(GuiSheets.bar(Math.min(1.0, lots.size() / (double) Auctions.LOTS_TO_HOLD), Theme.GOLD));
        sb.append("<p class='dim'>").append(esc(w < 0 ? "An auction is held when " + Auctions.LOTS_TO_HOLD + " lots wait, or " + Auctions.MAX_WAIT + " weeks after the first arrives." : "Sooner if " + Auctions.LOTS_TO_HOLD + " lots wait.")).append("</p>");
        sb.append("<h2>THE LOTS</h2>");
        if (lots.isEmpty()) sb.append("<p class='dim'>No lots on the block right now. Sell a luxury in a city and it is put up for auction.</p>");
        else {
            sb.append("<table width='100%' cellspacing='5' cellpadding='8'>");
            int n = 1;
            for (Item it : lots) {
                long res = Auctions.reserve(it);
                sb.append("<tr><td width='9%' bgcolor='").append(hex(Theme.PANEL2)).append("' align='center'>").append(dim("LOT")).append("<br><span style='font-size:").append(px(18)).append("'><b>").append(n++).append("</b></span></td><td bgcolor='").append(hex(Theme.PANEL2)).append("'><b>")
                  .append(esc(it.name + (it.quantity > 1 ? " ×" + it.quantity : ""))).append("</b>").append(it.heavenSent ? "&nbsp;&nbsp;" + tag("HEAVEN-SENT", Theme.GOLD) : "").append("<br><span class='dim'>").append(esc(GuiItems.kind(it) + (it.lvl > 0 ? " · " + (GuiItems.graded(it) ? GuiItems.grade(it.lvl) : "level " + it.lvl) : "")))
                  .append("</span></td><td width='24%' bgcolor='").append(hex(Theme.PANEL2)).append("' align='right'>").append(dim("reserve")).append("<br><b><font color='").append(hex(Theme.GOLD)).append("'>").append(res).append(" ss</font></b><br>")
                  .append(wealth >= res ? tag("you can afford it", Theme.OK) : dim("beyond your purse")).append("</td></tr>");
            }
            sb.append("</table>");
        }
        sb.append("<h2>HOW IT WORKS</h2><p class='dim'>Only luxuries are auctioned: Heaven-Sent treasures, Potential Pills and vintage food, wine and poison. Sell them in a city (<b>Sell</b>) to put them on the block. "
                + "Be standing in the city when the auction is held: you are asked for a maximum bid for each lot (0 passes) and the winner pays the runner-up's bid. The consignor gets 90%.</p>");
        return GuiSheets.page(sb.toString());
    }

    // ------------------------------------------------------------------ sect
    static String rankName(Organization.SectRank r) {
        String s = r.name().toLowerCase().replace('_', ' ');
        return java.lang.Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    static Color rankColor(Organization.SectRank r) {
        switch (r) { case FOUNDER: case MASTER: return Theme.GOLD; case ELDER: return Theme.ACCENT; case INNER_DISCIPLE: return Theme.TEXT; default: return Theme.DIM; }
    }

    /** A sect as a sheet. `viewer` (may be null) gets "yours / not yours" and their own points; `detail` adds the founder and the strongest members. */
    static String sectHtml(Organization s, Character viewer, boolean detail) {
        if (s == null) return GuiSheets.page("<h1>No sect</h1><p class='dim'>You belong to no sect and none is here. A Major Lvl 2+ character can found one (<b>Found sect</b>); otherwise stand in a sect's tile and press <b>Join</b>.</p>");
        boolean mine = viewer != null && viewer.sect == s;
        StringBuilder sb = new StringBuilder("<h1>").append(esc(s.name)).append("</h1>");
        sb.append(dim((viewer == null ? "" : mine ? "Your sect · you are " + rankName(s.getRank(viewer)) + " · " : "Not your sect · ") + "home " + (s.location == null ? "?" : s.location.name + " " + PlayerControl.coord(s.location))));
        String master = s.master != null ? link(s.master) : s.isVirtual() ? "???" : "none";
        if (detail) sb.append(tiles(4, new String[]{"Master", master}, tile("Members", s.members.size()), tile("Sect level", s.sectLvl()), tile("Cash", s.sectCash + " ss", Theme.GOLD),
                tile("Treasury", s.treasury.size() + " stacks"), tile("At war with", s.warTarget != null ? s.warTarget.name : "nobody", s.warTarget != null ? Theme.DANGER : Theme.DIM), tile("Allies", s.allies.size()),
                mine ? tile("Your points", viewer.sectPoints, Theme.OK) : tile("Your points", "—")));
        else { // the compact banner above a members table
            sb.append(tiles(4, new String[]{"Master", master}, tile("Members", s.members.size()), tile("Sect level", s.sectLvl()), tile("Cash", s.sectCash + " ss", Theme.GOLD)));
            sb.append("<p class='dim'>Treasury ").append(s.treasury.size()).append(" stacks · ").append(s.warTarget != null ? "<font color='" + hex(Theme.DANGER) + "'>at war with " + esc(s.warTarget.name) + "</font>" : "at peace").append(" · ").append(s.allies.size()).append(" allies")
              .append(mine ? " · your points " + viewer.sectPoints : "").append("</p>");
        }
        sb.append("<p>").append(tag("JOINING", Theme.ACCENT)).append("&nbsp;&nbsp;").append(esc(s.rulesText())).append("</p>");
        if (mine && viewer.sectMission != null) sb.append("<p>").append(tag("YOUR MISSION", Theme.GOLD)).append("&nbsp;&nbsp;").append(esc(viewer.missionLine())).append("</p>");
        if (detail) {
            sb.append("<p>").append(tag("FOUNDER", Theme.ACCENT)).append("&nbsp;&nbsp;").append(s.founder != null ? (s.founder.dead ? esc(s.founder.name + " " + s.founder.lastName) + dim(" (dead)") : link(s.founder) + dim("  Lv " + s.founder.lvl)) : s.isVirtual() ? "???" : "?").append("</p>");
            List<Character> mem = new ArrayList<>(s.members);
            mem.removeIf(x -> x == null || x.dead);
            mem.sort((x, y) -> Integer.compare(y.lvl, x.lvl));
            sb.append("<h2>STRONGEST MEMBERS</h2><table width='100%'><tr><th>Name</th><th>Rank</th><th>Lv</th></tr>");
            for (int i = 0; i < Math.min(10, mem.size()); i++) { Character x = mem.get(i); Organization.SectRank r = s.getRank(x); sb.append("<tr><td>").append(link(x)).append("</td><td>").append(tag(rankName(r), rankColor(r))).append("</td><td>").append(x.lvl).append("</td></tr>"); }
            sb.append("</table>");
        }
        return GuiSheets.page(sb.toString());
    }

    // ------------------------------------------------------------------ city
    /** A city as a sheet; `viewer` may be null. `detail` adds the libraries, who is here and the sect that sits in it. */
    static String cityHtml(Character viewer, Area a, boolean detail) {
        StringBuilder sb = new StringBuilder("<h1>").append(esc(a.name)).append("</h1>");
        String region = Regions.line(a);
        if (!a.city) {
            sb.append(dim(PlayerControl.coord(a) + " · " + a.biomeM));
            return GuiSheets.page(sb.append("<p>This place is not a city.</p><p class='dim'>A character of Major Lvl 2 or more can found one here (<b>Found / claim city</b>). Cities grow with trade, shops and tournaments.</p>").toString());
        }
        sb.append(dim(PlayerControl.coord(a) + " · " + (a.geoName != null && !a.geoName.equals(a.name) ? a.geoName + ", " : "") + a.biomeM + (region.isEmpty() ? "" : " · " + region)));
        String lord = a.cityOwner == null ? "nobody" : (viewer != null && a.cityOwner == viewer ? "You" : null);
        sb.append(tiles(3, tile("City level", a.cityLevel(), Theme.GOLD), tile("Population", a.population), tile("Treasury", a.cityTreasury + " ss"),
                new String[]{"Lord", lord != null ? esc(lord) : link(a.cityOwner) + dim("  Lv " + a.cityOwner.lvl)}, tile("Shops", a.shops.size() + " of " + a.maxShopSlots()), tile("Libraries / arrays", a.libraries.size() + " / " + (a.arrayBag == null ? 0 : a.arrayBag.size()))));
        if (a.isOriginis()) sb.append("<p class='dim'>Crowd: ").append(a.characters.size()).append(" people. Originis is exempt from crowd hotspots.</p>");
        else {
            int cap = Math.max(1, a.crowdCapacity());
            sb.append("<p class='dim'>Crowd: ").append(a.characters.size()).append(" of ").append(cap).append(" the city can hold").append(a.crowdHotspot ? " &nbsp;" + tag("OVERCROWDED", Theme.DANGER) : "").append("</p>")
              .append(GuiSheets.bar(Math.min(1.0, a.characters.size() / (double) cap), a.crowdHotspot ? Theme.DANGER : Theme.ACCENT));
        }
        if (a.tournamentTimer > 0) sb.append("<p>").append(tag("TOURNAMENT", Theme.GOLD)).append("&nbsp;&nbsp;realms ").append(a.tournamentMinMajor).append("–").append(a.tournamentMaxMajor).append(" · ").append(a.tournamentTimer).append(" weeks left · ")
                .append(a.tournamentRegistrants.size()).append(" registered</p>");
        sb.append("<h2>SHOPS HERE</h2>");
        if (a.shops.isEmpty()) sb.append("<p class='dim'>No shops. A merchant can open one (<b>Open / restock here</b>&nbsp;on the My shops tab).</p>");
        else {
            sb.append("<table width='100%'><tr><th>Shop</th><th>Owner</th><th>Lv</th><th>Stock</th></tr>");
            for (Shop s : new ArrayList<>(a.shops)) sb.append("<tr><td>").append(esc(s.name == null || s.name.isEmpty() ? "shop" : s.name)).append("</td><td>").append(s.owner == null ? "?" : link(s.owner)).append("</td><td>").append(s.level()).append("</td><td>").append(s.inventory.size()).append("</td></tr>");
            sb.append("</table>");
        }
        if (detail) {
            if (!a.libraries.isEmpty()) {
                sb.append("<h2>LIBRARIES</h2><p>");
                for (Library l : new ArrayList<>(a.libraries)) sb.append(esc(l.name == null || l.name.isEmpty() ? "library" : l.name)).append(dim(" (" + l.techniques.size() + " techniques)")).append("&nbsp;&nbsp;&nbsp;");
                sb.append("</p>");
            }
            if (a.sect != null) sb.append("<p>").append(tag("SECT HERE", Theme.ACCENT)).append("&nbsp;&nbsp;").append(esc(a.sect.name)).append(dim(" (" + a.sect.members.size() + " members)")).append("</p>");
            List<Character> here = new ArrayList<>();
            for (Character c : new ArrayList<>(a.characters)) if (c != null && !c.dead && c.trait != Character.Traits.Beast) here.add(c);
            here.sort((x, y) -> Integer.compare(y.lvl, x.lvl));
            sb.append("<h2>CULTIVATORS HERE — ").append(here.size()).append("</h2><table width='100%'><tr><th>Strongest</th><th>Lv</th></tr>");
            for (int i = 0; i < Math.min(8, here.size()); i++) sb.append("<tr><td>").append(link(here.get(i))).append("</td><td>").append(here.get(i).lvl).append("</td></tr>");
            sb.append("</table>");
        }
        return GuiSheets.page(sb.toString());
    }

    // ------------------------------------------------------------------ place details (city and/or sect with its history)
    static String placeHtml(Area a, Character viewer) {
        if (a == null) return GuiSheets.page("<h1>Unknown place</h1>");
        String city = a.city ? cityHtml(viewer, a, true).replace("</body></html>", "").replace("<html><body>", "") : "";
        String sect = a.sect != null ? sectHtml(a.sect, viewer, true).replace("</body></html>", "").replace("<html><body>", "") : "";
        StringBuilder sb = new StringBuilder(city);
        if (!city.isEmpty() && !sect.isEmpty()) sb.append("<hr>");
        sb.append(sect);
        if (sb.length() == 0) sb.append("<h1>Nothing of note</h1><p class='dim'>This tile holds no city or sect.</p>");
        List<Chronicle.Entry> h = Chronicle.search(a.city ? a.name : a.sect.name, "all", 40);
        if (a.sect != null && a.city && !a.sect.name.equals(a.name)) for (Chronicle.Entry e : Chronicle.search(a.sect.name, "all", 20)) if (!h.contains(e)) h.add(e);
        if (a.geoName != null && !a.geoName.equals(a.name)) for (Chronicle.Entry e : Chronicle.search(a.geoName, "all", 10)) if (!h.contains(e)) h.add(e);
        java.util.Collections.reverse(h);
        sb.append("<h2>HISTORY</h2>");
        if (h.isEmpty()) sb.append("<p class='dim'>Nothing recorded yet.</p>");
        else {
            sb.append("<table width='100%'>");
            for (Chronicle.Entry e : h) sb.append("<tr><td width='14%'>").append(tag("YEAR " + e.year(), kindColor(e.kind))).append("</td><td>").append(esc(e.text)).append("</td></tr>");
            sb.append("</table>");
        }
        return GuiSheets.page(sb.toString());
    }

    // ------------------------------------------------------------------ world dashboard
    static String dashHtml() {
        TextTools.Dash d = TextTools.dash();
        StringBuilder sb = new StringBuilder("<h1>World dashboard</h1>").append(dim("Year " + d.week / 52));
        sb.append(tiles(4, tile("Living", d.living, Theme.GOLD), tile("Cultivators", d.cultivators), tile("Children", d.children), tile("Mean age", d.cultivators == 0 ? 0 : d.totalAge / d.cultivators),
                tile("Beasts", d.beasts), tile("Awakened monsters", d.monsters), tile("Cities", d.cities), tile("Sects", d.sects)));
        sb.append("<h2>CULTIVATORS BY REALM</h2><table width='100%'>");
        int max = 1;
        for (int v : d.realm) max = Math.max(max, v);
        for (int i = 0; i < 11; i++) sb.append("<tr><td width='20%'>").append(esc(GuiSheets.REALMS[i])).append("</td><td>").append(GuiSheets.bar(Math.max(0.01, d.realm[i] / (double) max), Theme.ACCENT)).append("</td><td width='12%' align='right'>").append(d.realm[i]).append("</td></tr>");
        sb.append("</table><h2>WEALTH AMONG CULTIVATORS</h2>");
        sb.append(tiles(4, tile("Median", TextTools.si(d.p50) + " ss"), tile("Top tenth starts at", TextTools.si(d.p90) + " ss"), tile("Top hundredth starts at", TextTools.si(d.p99) + " ss"), tile("Richest", TextTools.si(d.max) + " ss", Theme.GOLD)));
        sb.append("<p class='dim'>The top 1% hold ").append(Math.round(d.top1Share * 100)).append("% of all the wealth.</p>");
        sb.append("<h2>DEATHS SO FAR, BY CAUSE</h2>");
        int total = 0;
        for (int v : d.deaths.values()) total += v;
        if (d.deaths.isEmpty()) sb.append("<p class='dim'>None recorded yet.</p>");
        else {
            sb.append("<table width='100%'>");
            for (java.util.Map.Entry<String, Integer> e : d.deaths.entrySet())
                sb.append("<tr><td width='28%'>").append(esc(e.getKey())).append("</td><td>").append(GuiSheets.bar(Math.max(0.01, e.getValue() / (double) Math.max(1, total)), Theme.DANGER)).append("</td><td width='18%' align='right'>").append(e.getValue()).append(dim("  " + Math.round(100.0 * e.getValue() / Math.max(1, total)) + "%")).append("</td></tr>");
            sb.append("</table>");
        }
        return GuiSheets.page(sb.toString());
    }

    // ------------------------------------------------------------------ story
    static String storyHtml(Character p) {
        if (p == null) return noPlayer();
        Starts.State st = p.story;
        if (st == null) return GuiSheets.page("<h1>No story</h1><p>You are not living a story: this life began as " + (GameDriver.difficulty > 0 ? "a hard Standard" : "a Standard life") + " (no chain of objectives).</p>"
                + "<p class='dim'>Story starts are chosen when a new game begins: ten of them, each a few chapters with named people, choices and an ending of its own.</p>");
        Starts.Story s = Starts.STORIES.get(st.story);
        if (s == null) return GuiSheets.page("<h1>Your story</h1><p class='dim'>(unknown story)</p>");
        StringBuilder sb = new StringBuilder("<h1>").append(esc(s.title)).append("</h1>").append(dim(Starts.DIFFICULTY_NAMES[st.difficulty]));
        sb.append("<p>").append(esc(s.blurb)).append("</p>");
        if (!st.hooked) return GuiSheets.page(sb.append("<p class='dim'>A pure challenge: this Brutal start has no hook and no chapters.</p>").toString());
        if (!st.v2) { // a save from before the chapters: the flat objectives
            sb.append("<h2>OBJECTIVES</h2><table width='100%'>");
            for (int i = 0; i < s.steps.size(); i++) {
                Starts.Step step = s.steps.get(i);
                boolean done = i < st.step || st.done, now = i == st.step && !st.done;
                sb.append("<tr><td width='11%'>").append(done ? tag("DONE", Theme.OK) : now ? tag("NOW", Theme.GOLD) : tag("LATER", Theme.DIM)).append("</td><td>").append(esc(step.text)).append("<br>").append(dim("reward: " + step.reward)).append("</td></tr>");
            }
            if (st.done) sb.append("<tr><td></td><td>").append(tag("Story complete.", Theme.GOLD)).append("</td></tr>");
            return GuiSheets.page(sb.append("</table>").toString());
        }
        List<String[]> rows = Chapters.rows(p, st);
        int done = 0;
        for (String[] r : rows) if (r[0].equals("done")) done++;
        sb.append("<h2>").append(st.done ? "THE STORY IS COMPLETE" : "CHAPTER " + Math.min(rows.size(), done + 1) + " OF " + rows.size()).append("</h2>");
        sb.append(GuiSheets.bar(rows.isEmpty() ? 0 : done / (double) rows.size(), st.done ? Theme.GOLD : Theme.ACCENT));
        sb.append("<table width='100%' cellspacing='5' cellpadding='7'>");
        int n = 1;
        for (String[] r : rows) {
            boolean isDone = r[0].equals("done"), now = r[0].equals("now"), wait = r[0].equals("wait");
            sb.append("<tr><td width='7%' bgcolor='").append(hex(now ? Theme.INK : Theme.PANEL2)).append("' align='center'>").append(isDone ? tag("" + n, Theme.OK) : now ? tag("" + n, Theme.GOLD) : dim("" + n)).append("</td><td bgcolor='").append(hex(now ? Theme.INK : Theme.PANEL2)).append("'>");
            if (isDone) sb.append("<b>").append(esc(r[1])).append("</b>");
            else if (now) sb.append(tag("NOW", Theme.GOLD)).append(" <b>").append(esc(r[1])).append("</b><br>").append(esc(r[2]));
            else if (wait) sb.append("<b>Chapter ").append(n).append("</b><br>").append(dim("waiting for the right time"));
            else sb.append(dim("???"));
            sb.append("</td></tr>");
            n++;
        }
        sb.append("</table>");
        List<Character> people = Chapters.people(st);
        if (!people.isEmpty()) {
            sb.append("<h2>PEOPLE OF YOUR STORY</h2><table width='100%'><tr><th>Name</th><th>Lv</th><th>Where</th><th></th></tr>");
            for (Character x : people)
                sb.append("<tr><td>").append(x.dead ? esc(x.name + " " + x.lastName) : link(x)).append("</td><td>").append(x.lvl).append("</td><td>").append(x.dead ? dim("—") : x.location == null ? "?" : placeLink(x.location) + " " + dim(PlayerControl.coord(x.location)))
                  .append("</td><td>").append(x.dead ? tag("gone", Theme.DIM) : x.fatedTo != 0 ? tag("yours to face", Theme.GOLD) : tag("alive", Theme.OK)).append("</td></tr>");
            sb.append("</table>");
        }
        int tales = Chapters.episodesMet(st);
        if (tales > 0) sb.append("<p>").append(dim("Tales met on the road: " + tales)).append("</p>");
        return GuiSheets.page(sb.toString());
    }
}
