package Game;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** The windows of the lore: the scenes ??? speaks (a themed pop-up with Continue; the game waits while one is open unless `settings lorepause off`),
 *  the ending window, and the Journal (act, objective, the road, the places, what you have learned, the Palace, the scenes you can read again).
 *  Everything here runs on the Swing thread and only reads Lore state inside World.read; actions go out as commands (`lore replay x`, `goto`). */
final class GuiLore {
    private GuiLore() { }

    private static int openScenes = 0;
    private static volatile boolean weHoldThePause = false;

    /** True while a scene is open or the welcome tour is on screen (the next scene waits its turn; the terminal never waits). */
    static boolean busy() {
        if (!GameDriver.guiEnabled || GameDriver.mapViewer == null) return false;
        try { return openScenes > 0 || GuiWindows.find("tutorial") != null; } catch (RuntimeException e) { return false; }
    }

    private static String esc(String s) { return GuiSheets.esc(s); }

    /** The scene as an HTML page: the title, narration in dim italics, spoken lines with the speaker's name in gold. */
    static String sceneHtml(String title, List<String> lines, String stats) {
        StringBuilder sb = new StringBuilder("<h1>" + esc(title) + "</h1><hr>");
        String gold = Theme.hex(Theme.GOLD), dim = Theme.hex(Theme.DIM);
        for (String l : lines) {
            if (LoreText.narration(l)) sb.append("<p><font color='").append(dim).append("'><i>").append(esc(l.substring(2))).append("</i></font></p>");
            else if (LoreText.other(l)) sb.append("<p><b><font color='").append(Theme.hex(Theme.ACCENT)).append("'>OLD MAN</font></b>&nbsp; ").append(esc(l.substring(9))).append("</p>");
            else sb.append("<p><b><font color='").append(gold).append("'>").append(LoreText.VOICE).append("</font></b>&nbsp; ").append(esc(l)).append("</p>");
        }
        if (stats != null) sb.append("<hr><p><font color='").append(gold).append("'><b>THE END</b></font> &nbsp;You transcended the Garden. The world goes on, and so can you: nothing more is asked of you.</p><pre>").append(esc(stats)).append("</pre>");
        return GuiSheets.page(sb.toString());
    }

    static void scene(String id, String title, List<String> lines) { show(title, lines, null, "Continue", null); }
    static void ending(String title, List<String> lines, String stats) { show(title, lines, stats, "Close", null); }
    /** An ending: the scene, then (Credits button) the funny credits as a slow scroll. */
    static void ending(String title, List<String> lines, String stats, List<String> credits) { show(title, lines, stats, credits == null ? "Close" : "Credits ▸", credits); }

    /** The credits as a slow scroll (a Skip button stops it); safe from any thread. */
    static void credits(final MapViewer mv, final List<String> lines) {
        SwingUtilities.invokeLater(() -> {
            JDialog d = new JDialog(mv, "Credits", false);
            d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            d.getContentPane().setBackground(Theme.BG);
            StringBuilder sb = new StringBuilder("<div align='center'>");
            for (int i = 0; i < 14; i++) sb.append("<br>");
            String gold = Theme.hex(Theme.GOLD), dim = Theme.hex(Theme.DIM);
            for (int i = 0; i < lines.size(); i++) {
                String l = lines.get(i);
                if (l.isEmpty()) sb.append("<br>");
                else if (i == 0) sb.append("<h1><font color='").append(gold).append("'>").append(esc(l)).append("</font></h1>");
                else if (l.equals("Starring") || l.equals("The Roll of the Crossed") || l.startsWith("THE END") || l.startsWith("Ending:") || l.startsWith("SECRET")) sb.append("<h2><font color='").append(gold).append("'>").append(esc(l)).append("</font></h2>");
                else sb.append("<p><font color='").append(dim).append("'>").append(esc(l)).append("</font></p>");
            }
            for (int i = 0; i < 16; i++) sb.append("<br>");
            sb.append("</div>");
            JEditorPane ep = GuiSheets.view(mv);
            ep.setText(GuiSheets.page(sb.toString()));
            ep.setCaretPosition(0);
            JScrollPane sp = new JScrollPane(ep, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            sp.setBorder(BorderFactory.createEmptyBorder());
            final Timer t = new Timer(40, null);
            t.addActionListener(e -> { JScrollBar b = sp.getVerticalScrollBar(); if (b.getValue() + b.getVisibleAmount() >= b.getMaximum() - 1) t.stop(); else b.setValue(b.getValue() + 1); });
            JButton skip = mv.makeBtn("Close", () -> { t.stop(); d.dispose(); });
            JPanel south = GuiWindows.flow(FlowLayout.RIGHT);
            south.add(skip);
            d.add(sp, BorderLayout.CENTER);
            d.add(south, BorderLayout.SOUTH);
            d.addWindowListener(new java.awt.event.WindowAdapter() { @Override public void windowClosed(java.awt.event.WindowEvent e) { t.stop(); openScenes = Math.max(0, openScenes - 1); if (openScenes == 0 && weHoldThePause) { weHoldThePause = false; GameDriver.paused = false; } } });
            d.setSize(620, 560);
            d.setLocationRelativeTo(mv);
            Ui.prepare(d);
            d.setVisible(true);
            Timer start = new Timer(1200, e -> t.start());
            start.setRepeats(false);
            start.start();
        });
    }

    private static void show(final String title, final List<String> lines, final String stats, final String button, final List<String> creditLines) {
        final MapViewer mv = GameDriver.mapViewer;
        if (mv == null) return;
        if (Settings.lorePause && !GameDriver.paused) { GameDriver.paused = true; weHoldThePause = true; } // the game waits until the scene is closed
        final String html = sceneHtml(title, lines, stats);
        SwingUtilities.invokeLater(() -> {
            JDialog d = new JDialog(mv, LoreText.VOICE + "  ·  " + title, false);
            d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            d.getContentPane().setBackground(Theme.BG);
            JEditorPane ep = GuiSheets.view(mv);
            ep.setText(html);
            ep.setCaretPosition(0);
            JScrollPane sp = new JScrollPane(ep);
            sp.setBorder(BorderFactory.createEmptyBorder());
            JButton go = mv.makeBtn(button, () -> { if (creditLines != null) openScenes++; d.dispose(); if (creditLines != null) credits(mv, creditLines); }); // the credits count as an open scene, so the game does not quit under them
            JPanel south = GuiWindows.flow(FlowLayout.RIGHT);
            south.add(go);
            d.add(sp, BorderLayout.CENTER);
            d.add(south, BorderLayout.SOUTH);
            d.getRootPane().setDefaultButton(go);
            d.setSize(660, 520);
            d.setLocationRelativeTo(mv);
            Ui.prepare(d);
            openScenes++;
            d.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosed(java.awt.event.WindowEvent e) {
                    openScenes = Math.max(0, openScenes - 1);
                    if (openScenes == 0 && weHoldThePause) { weHoldThePause = false; GameDriver.paused = false; }
                }
            });
            d.setVisible(true);
            go.requestFocusInWindow();
        });
    }

    // ------------------------------------------------------------------ the Journal
    /** Opens the Journal (safe from any thread: windows are built on the Swing thread). */
    static void journal(MapViewer mv) {
        SwingUtilities.invokeLater(() -> GuiSheets.sheet(mv, "lorejournal", "Journal of the Oldest Order", 720, 640, 3000, () -> journalHtml(mv), null));
    }

    private static String journalHtml(MapViewer mv) {
        Character c = GuiWindows.player(mv);
        Lore.State s = Lore.state;
        if (c == null) return GuiSheets.page("<h1>The Oldest Order</h1><p class='dim'>No human character yet.</p>");
        if (s == null || !Settings.lore) return GuiSheets.page("<h1>The Oldest Order</h1><p class='dim'>" + (s == null ? "This world began without the lore. Type <b>lore enable</b> in the command box to found the Palace at Originis." : "The lore is switched off (Settings, or <b>settings lore on</b>).") + "</p>");
        Lore.PState p = s.humans.get(c.id);
        if (p == null) {
            return GuiSheets.page("<h1>The Oldest Order</h1><p>The Palace of the Oldest Order stands at Originis. Its bell rings for those who come near it; go there and listen.</p>"
                    + "<p><a href='cmd:goto " + (Map.range / 2) * 100 + " " + (Map.range / 2) * 100 + "'>Go to Originis</a></p><p class='dim'>" + esc(Lore.palaceText(c, new Lore.PState())).replace("\n", "<br>") + "</p>");
        }
        String gold = Theme.hex(Theme.GOLD), ok = Theme.hex(Theme.ACCENT), dim = Theme.hex(Theme.DIM);
        Organization pal = Lore.palace();
        StringBuilder sb = new StringBuilder("<h1>The Oldest Order</h1><span class='dim'>" + esc(Lore.actName(p)) + " &middot; " + esc(Lore.rankText(c, pal)) + " &middot; merit " + c.sectPoints + "</span>");
        sb.append("<h2>What the Order asks now</h2><p>").append(esc(Lore.objective(c, p))).append("</p>");
        sb.append("<h2>The road</h2><table cellspacing='0'>");
        for (int i = 0; i < Lore.CHAIN.length; i++) {
            Lore.Step st = Lore.CHAIN[i];
            if (i > p.step + 1 && i > 1) continue; // the far future stays unwritten
            String mark = i < p.step ? "<font color='" + ok + "'>&#10003;</font>" : i == p.step ? "<font color='" + gold + "'>&#9654;</font>" : "<font color='" + dim + "'>&middot;</font>";
            String txt = i > p.step ? "<font color='" + dim + "'>...</font>" : esc(st.text);
            sb.append("<tr><td valign='top'>").append(mark).append("</td><td>").append(txt).append("</td></tr>");
        }
        sb.append("</table>");
        sb.append("<h2>The places</h2><table cellspacing='0'>");
        for (int i = 0; i < LoreSites.ALL.length; i++) {
            LoreSites.Site site = LoreSites.ALL[i];
            boolean found = p.found.contains(site.id), known = p.revealed.contains(site.id);
            sb.append("<tr><td>").append(found ? "<font color='" + ok + "'>&#10003;</font>" : known ? "&#9675;" : "<font color='" + dim + "'>?</font>").append("</td><td>");
            if (known || found) {
                sb.append(esc(site.title)).append(" <span class='dim'>[").append(s.siteX[i] * 100).append(", ").append(s.siteY[i] * 100).append("] Major Lvl ").append(site.minMajor).append("+</span>");
                if (!found) sb.append(" &nbsp;<a href='goto:").append(s.siteX[i]).append(':').append(s.siteY[i]).append("'>Go</a>");
            } else sb.append("<font color='").append(dim).append("'>(not yet known)</font>");
            sb.append("</td></tr>");
        }
        sb.append("<tr><td>").append(p.found.contains(LoreSites.SEAT) ? "<font color='" + ok + "'>&#10003;</font>" : "<font color='" + dim + "'>?</font>").append("</td><td>The Empty Seat <span class='dim'>(Originis)</span></td></tr></table>");
        sb.append("<p class='dim'>Giant Footprints read: ").append(p.prints == null ? 0 : p.prints.size()).append(" of ").append(LoreSites.FOOT_NAMES.length).append(" (they lie near the Thin Sky and the Garrison)</p>");
        if (p.ended) sb.append("<p><b><font color='").append(gold).append("'>Ending: ").append(esc(p.ending)).append(" &middot; score ").append(p.score).append("</font></b> &nbsp;<a href='cmd:lore roll'>Roll of the Crossed</a> &nbsp;<a href='cmd:lore hall'>Hall of fame</a></p>");
        sb.append("<h2>What you have learned</h2>");
        int n = 0;
        for (String id : p.found) {
            LoreText.Clue cl = LoreText.CLUES.get(id);
            if (cl == null) continue;
            sb.append("<p><b>").append(++n).append(". ").append(esc(cl.title)).append("</b><br>").append(esc(cl.truth)).append("</p>");
        }
        if (n == 0) sb.append("<p class='dim'>Nothing yet. The Palace's roads lead to nine places; the Wayhouse is the first.</p>");
        sb.append("<h2>The Palace</h2><p>").append(esc(Lore.palaceText(c, p)).replace("\n", "<br>")).append("</p>");
        sb.append("<h2>Scenes you have seen</h2><p>");
        for (String id : p.scenes) { LoreText.Scene sc = LoreText.SCENES.get(id); if (sc != null) sb.append("<a href='cmd:lore replay ").append(id).append("'>").append(esc(sc.title)).append("</a> &nbsp; "); }
        if (p.scenes.isEmpty()) sb.append("<span class='dim'>none yet</span>");
        sb.append("</p>");
        if (!p.ended && Lore.whyNotCross(c, p) == null) sb.append("<p><b><a href='cmd:lore cross'>Cross the Seam</a></b></p>");
        if (LoreEndings.stayedOf(p)) sb.append("<p><b><a href='cmd:lore leave'>Leave the Garden</a></b> &nbsp; <span class='dim'>(the Seam opens for you again; leaving is final)</span></p>");
        return GuiSheets.page(sb.toString());
    }
}
