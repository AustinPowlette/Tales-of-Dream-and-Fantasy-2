package Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The periodic "what happened" summary (every season / year / N years, per Settings.recapWeeks): what changed for each human character,
 *  the notices they received, and what changed in the world. Console: printed; GUI: a window. */
public class Recap {
    private Recap() { }

    static class Base {
        int week, lvl, major, age, kills, crafted, sectPoints, children, relations, population, cities, sects, notes, history;
        long wealth;
    }

    private static final Map<Integer, Base> base = new HashMap<>();
    private static int lastRecapWeek = -1;

    static void reset() { synchronized (base) { base.clear(); } lastRecapWeek = -1; }

    private static Base take(Character c) {
        Base b = new Base();
        b.week = GameDriver.time; b.lvl = c.lvl; b.major = c.majorLvl; b.age = c.age; b.kills = c.kills; b.crafted = c.itemsCrafted; b.sectPoints = c.sectPoints;
        b.children = c.children.size(); b.relations = c.relationshipList.relationships.size(); b.wealth = PlayerLists.wealth(c);
        b.population = Game.Map.population;
        int ci = 0, se = 0;
        for (Area a : new ArrayList<>(Game.Map.createdAreas)) { if (a.city) ci++; if (a.sect != null) se++; }
        b.cities = ci; b.sects = se;
        b.notes = Notifications.history.size();
        b.history = GameDriver.historyLog.size();
        return b;
    }

    /** Game thread, once per tick (cheap unless a recap is due). */
    static void check() {
        int w = Settings.recapWeeks;
        if (w <= 0 || GameDriver.time <= 0 || GameDriver.time % w != 0 || GameDriver.time == lastRecapWeek) return;
        lastRecapWeek = GameDriver.time;
        for (Character h : new ArrayList<>(Notifications.humans())) {
            if (h == null || h.dead || h.player != 1) continue;
            Base b;
            synchronized (base) { b = base.get(h.id); }
            if (b != null) deliver(h, build(h, b), b);
            synchronized (base) { base.put(h.id, take(h)); }
        }
    }

    /** Called when a human is first seen so their first period has a starting point. */
    static void ensureBase(Character h) { synchronized (base) { base.computeIfAbsent(h.id, k -> take(h)); } }

    private static volatile String lastTitle = null, lastBody = null; // the last recap that was delivered, as a card (the Recap tab of the Chronicle window shows it)

    private static void deliver(Character h, String[] titleAndText, Base b) {
        System.out.println(titleAndText[1]);
        if (GameDriver.guiEnabled && GameDriver.mapViewer != null) {
            final String t = titleAndText[0], body = html(h, b);
            lastTitle = t; lastBody = body;
            javax.swing.SwingUtilities.invokeLater(() -> GuiWindows.recapWindow(GameDriver.mapViewer, t, GuiSheets.page(body)));
        }
    }

    /** The recap as it would read right now (the period so far), without resetting anything. */
    static String[] preview(Character h) {
        Base b;
        synchronized (base) { b = base.get(h.id); if (b == null) { b = take(h); base.put(h.id, b); } }
        return build(h, b);
    }

    /** The Recap tab of the Chronicle window: the period so far, and the last recap that was delivered. */
    static String cardHtml(Character h) {
        if (h == null) return GuiSheets.page("<h1>No player character</h1>");
        Base b;
        synchronized (base) { b = base.get(h.id); if (b == null) { b = take(h); base.put(h.id, b); } }
        StringBuilder sb = new StringBuilder(html(h, b));
        if (lastBody != null) sb.append("<hr><span class='dim'>THE LAST RECAP</span>").append(lastBody);
        else sb.append("<p class='dim'>The first recap arrives when the period ends (Settings > recaps).</p>");
        return GuiSheets.page(sb.toString());
    }

    /** The recap as a card: the same facts as `build`, laid out as a page with links to the people (no page wrapper). */
    static String html(Character h, Base b) {
        int weeks = GameDriver.time - b.week;
        String period = weeks == 52 ? "Year " + (GameDriver.time / 52) : weeks == 13 ? "Season" : weeks + " weeks";
        StringBuilder sb = new StringBuilder("<h1>").append(GuiSheets.esc(period)).append(" recap — ").append(GuiSheets.esc(h.name + " " + h.lastName)).append("</h1>");
        long w = PlayerLists.wealth(h);
        sb.append("<table width='100%'>");
        sb.append("<tr><td class='dim' width='22%'>You</td><td>level ").append(b.lvl).append(" → ").append(h.lvl).append(h.majorLvl != b.major ? " (Major " + b.major + " → " + h.majorLvl + ")" : " (Major " + h.majorLvl + ")")
          .append(", age ").append(h.age).append("/").append(h.maxAge).append("</td></tr>");
        sb.append("<tr><td class='dim'>Wealth</td><td>").append(w).append(" ss (").append(delta(w - b.wealth)).append("), kills ").append(delta(h.kills - b.kills)).append(", crafted ")
          .append(delta(h.itemsCrafted - b.crafted)).append(", sect points ").append(delta(h.sectPoints - b.sectPoints)).append("</td></tr>");
        sb.append("<tr><td class='dim'>Family and friends</td><td>children ").append(delta(h.children.size() - b.children)).append(", relationships ").append(delta(h.relationshipList.relationships.size() - b.relations)).append("</td></tr>");
        int ci = 0, se = 0;
        for (Area a : new ArrayList<>(Game.Map.createdAreas)) { if (a.city) ci++; if (a.sect != null) se++; }
        sb.append("<tr><td class='dim'>The world</td><td>population ").append(b.population).append(" → ").append(Game.Map.population).append(", cities ").append(b.cities).append(" → ").append(ci)
          .append(", sects ").append(b.sects).append(" → ").append(se).append("</td></tr></table>");
        List<Notifications.Note> mine = new ArrayList<>();
        synchronized (Notifications.history) {
            for (int i = Math.max(0, b.notes); i < Notifications.history.size(); i++) {
                Notifications.Note n = Notifications.history.get(i);
                if ((n.forId == h.id || n.forId < 0) && !n.cat.equals("hint")) mine.add(n);
            }
        }
        sb.append("<h2>WHAT HAPPENED TO YOU</h2>");
        if (mine.isEmpty()) sb.append("<p class='dim'>A quiet stretch: nothing needed your attention.</p>");
        else {
            sb.append("<table width='100%'>");
            int shown = 0;
            for (Notifications.Note n : mine) {
                if (++shown > 12) { sb.append("<tr><td colspan='2' class='dim'>... and ").append(mine.size() - 12).append(" more (see the notices)</td></tr>"); break; }
                sb.append("<tr><td width='14%' class='dim'>wk ").append(n.week).append("</td><td>").append(n.charId >= 0 ? "<a href='char:" + n.charId + "'>" + GuiSheets.esc(n.text) + "</a>" : GuiSheets.esc(n.text)).append("</td></tr>");
            }
            sb.append("</table>");
        }
        List<String> hist;
        synchronized (GameDriver.historyLog) { hist = new ArrayList<>(GameDriver.historyLog); }
        int from = Math.min(Math.max(0, b.history), hist.size());
        List<String> news = hist.subList(from, hist.size());
        if (!news.isEmpty()) {
            sb.append("<h2>NOTABLE EVENTS</h2><ul>");
            for (int i = Math.max(0, news.size() - 6); i < news.size(); i++) sb.append("<li>").append(GuiSheets.esc(news.get(i))).append("</li>");
            sb.append("</ul>");
        }
        return sb.toString();
    }

    static String delta(long v) { return (v >= 0 ? "+" : "") + v; }

    static String[] build(Character h, Base b) {
        int weeks = GameDriver.time - b.week;
        String period = weeks == 52 ? "Year " + (GameDriver.time / 52) : weeks == 13 ? "Season" : weeks + " weeks";
        StringBuilder sb = new StringBuilder();
        String title = period + " recap — " + h.name + " " + h.lastName;
        sb.append("=== ").append(title).append(" ===\n");
        sb.append("You:  level ").append(b.lvl).append(" → ").append(h.lvl).append(h.majorLvl != b.major ? "  (Major " + b.major + " → " + h.majorLvl + ")" : "  (Major " + h.majorLvl + ")")
          .append(",  age ").append(h.age).append("/").append(h.maxAge).append("\n");
        long w = PlayerLists.wealth(h);
        sb.append("Wealth: ").append(w).append(" ss (").append(delta(w - b.wealth)).append(")   Kills ").append(delta(h.kills - b.kills)).append("   Crafted ")
          .append(delta(h.itemsCrafted - b.crafted)).append("   Sect points ").append(delta(h.sectPoints - b.sectPoints)).append("\n");
        sb.append("Family & friends: children ").append(delta(h.children.size() - b.children)).append(",  relationships ").append(delta(h.relationshipList.relationships.size() - b.relations)).append("\n");
        List<Notifications.Note> mine = new ArrayList<>();
        synchronized (Notifications.history) {
            for (int i = Math.max(0, b.notes); i < Notifications.history.size(); i++) {
                Notifications.Note n = Notifications.history.get(i);
                if ((n.forId == h.id || n.forId < 0) && !n.cat.equals("hint")) mine.add(n);
            }
        }
        sb.append("\nWhat happened to you:\n");
        if (mine.isEmpty()) sb.append("  A quiet stretch: nothing needed your attention.\n");
        int shown = 0;
        for (Notifications.Note n : mine) { if (++shown > 12) { sb.append("  ... and ").append(mine.size() - 12).append(" more (see the notification history)\n"); break; } sb.append("  • ").append(n.text).append("\n"); }
        int ci = 0, se = 0;
        for (Area a : new ArrayList<>(Game.Map.createdAreas)) { if (a.city) ci++; if (a.sect != null) se++; }
        sb.append("\nThe world: population ").append(b.population).append(" → ").append(Game.Map.population).append(",  cities ").append(b.cities).append(" → ").append(ci)
          .append(",  sects ").append(b.sects).append(" → ").append(se).append("\n");
        List<String> hist;
        synchronized (GameDriver.historyLog) { hist = new ArrayList<>(GameDriver.historyLog); }
        int from = Math.min(Math.max(0, b.history), hist.size());
        List<String> news = hist.subList(from, hist.size());
        if (!news.isEmpty()) {
            sb.append("Notable events:\n");
            for (int i = Math.max(0, news.size() - 6); i < news.size(); i++) sb.append("  • ").append(news.get(i)).append("\n");
        }
        return new String[]{title, sb.toString()};
    }
}
