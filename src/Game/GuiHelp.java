package Game;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/** The in-game Help: one window with a contents tree, a search box and the page (HTML on warm paper or ink), links between pages
 *  (`help:<id>`), links that open the real windows (`win:<name>`), back / forward / home, and the welcome tutorial with its highlighted tour.
 *  The text is in HelpPages (markup) / HelpContent (model + compiler). Other windows call `helpButton(mv, "section-id")` for their "?" button. */
final class GuiHelp {
    private GuiHelp() { }

    private static Controller current;
    private static final Preferences PREFS = Preferences.userRoot().node("tales-of-dream-and-fantasy-2/help");

    // ------------------------------------------------------------------ public API
    /** Opens (or raises) the Help window at a section; null or an unknown id shows the start page. Safe to call from any thread. */
    static void open(final MapViewer mv, final String sectionId) {
        if (mv == null) return;
        if (!SwingUtilities.isEventDispatchThread()) { SwingUtilities.invokeLater(() -> open(mv, sectionId)); return; }
        final String target = sectionId == null || sectionId.isEmpty() ? "start" : sectionId;
        String bare = target.indexOf('#') >= 0 ? target.substring(0, target.indexOf('#')) : target;
        final String go = HelpContent.exists(bare) ? target : "start";
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int w = Math.min(1080, (int) (screen.width * 0.92)), h = Math.min(760, (int) (screen.height * 0.9));
        JDialog d = GuiWindows.window(mv, "help", "Help", w, h, 3_600_000, (dlg, refreshers) -> {
            Controller c = new Controller(mv, dlg);
            current = c;
            dlg.addWindowListener(new java.awt.event.WindowAdapter() { @Override public void windowClosed(java.awt.event.WindowEvent e) { if (current == c) current = null; c.stop(); } });
            return c.root;
        });
        if (current != null && current.dialog == d) current.go(go, true);
    }

    /** A small round "?" button for a window's title row: tooltip "Help: <section title>", opens the Help at that section. */
    static JButton helpButton(final MapViewer mv, final String sectionId) {
        JButton b = new JButton("?") {
            @Override public Dimension getPreferredSize() { int s = Math.round(getFont().getSize() * 1.95f); return new Dimension(s, s); }
            @Override public Dimension getMinimumSize() { return getPreferredSize(); }
            @Override public Dimension getMaximumSize() { return getPreferredSize(); }
        };
        b.setFont(Theme.bold(15));
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setFocusable(false);
        b.setForeground(Theme.GOLD);
        Theme.style(b, "arc:999; background:#27413c; borderColor:#68b8a0; hoverBackground:#2f5a52; pressedBackground:#1d3a35");
        String t = "Help: " + HelpContent.title(sectionId);
        b.setToolTipText(t);
        b.getAccessibleContext().setAccessibleName(t);
        b.putClientProperty("helpId", sectionId);
        b.addActionListener(e -> open(mv, sectionId));
        return b;
    }

    /** The section a "?" button points at, or null (used by the probe). */
    static String helpIdOf(Component c) { return c instanceof JComponent ? (String) ((JComponent) c).getClientProperty("helpId") : null; }

    /** Opens one of the game's windows by name (what a `win:<name>` link in the Help does). */
    static void openWindow(MapViewer mv, String name) {
        try {
            switch (name) {
                case "stats": GuiSheets.statsWindow(mv); break;
                case "backpack": GuiMore.backpackWindow(mv, "Gear"); break;
                case "techniques": GuiMore.backpackWindow(mv, "Techniques"); break;
                case "companions": GuiMore.backpackWindow(mv, "Companions"); break;
                case "relations": GuiSheets.relationsWindow(mv); break;
                case "family": GuiExtras.familyWindow(mv); break;
                case "squad": GuiSheets.squadWindow(mv); break;
                case "locations": GuiSheets.locationsWindow(mv); break;
                case "quests": GuiWindows.questPanel(mv); break;
                case "duties": GuiWindows.dutiesPanel(mv); break;
                case "manage": GuiWindows.managePanel(mv); break;
                case "rankings": GuiWindows.rankingsPanel(mv); break;
                case "market": GuiWindows.marketPanel(mv); break;
                case "chronicle": GuiMore.chronicleWindow(mv); break;
                case "biography": GuiMore.biographyWindow(mv, mv.findPlayer()); break;
                case "notes": GuiMore.notesWindow(mv); break;
                case "world": GuiMore.worldWindow(mv); break;
                case "painted-map": GuiMore.paintedMapWindow(mv); break;
                case "digest": GuiMore.digestWindow(mv, 52); break;
                case "run": GuiMore.runWindow(mv); break;
                case "settings": GuiExtras.settingsWindow(mv); break;
                case "saves": GuiExtras.savesWindow(mv); break;
                case "notifications": GuiExtras.notificationsWindow(mv); break;
                case "story": GuiExtras.storyWindow(mv); break;
                case "cheats": GuiSheets.cheatWindow(mv); break;
                case "keys": mv.showKeys(); break;
                case "browse": mv.handleShortcut(new KeyEvent(mv, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_B, 'b')); break;
                case "scripts": mv.handleShortcut(new KeyEvent(mv, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_T, 't')); break;
                case "tour": tutorial(mv); break;
                default: break;
            }
        } catch (RuntimeException ignored) { /* a window that needs a player when there is none: it says so itself */ }
    }

    // ------------------------------------------------------------------ the window
    private static final class Controller {
        final MapViewer mv;
        final JDialog dialog;
        final JPanel root = new JPanel(new BorderLayout());
        final DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode("Help");
        final JTree tree;
        final DefaultListModel<HelpContent.Section> resultModel = new DefaultListModel<>();
        final JList<HelpContent.Section> results = new JList<>(resultModel);
        final CardLayout cards = new CardLayout();
        final JPanel left = new JPanel(cards);
        final JTextField search = new JTextField(18);
        final JEditorPane page = new JEditorPane();
        final JScrollPane pageScroll;
        final JButton back, fwd, home, theme, prev, next;
        final JLabel crumb = new JLabel(" "), status = new JLabel(" ");
        final List<String> history = new ArrayList<>();
        int hpos = -1;
        HelpContent.Section cur;
        String anchor = null, query = "";
        boolean paper = PREFS.getBoolean("paper", !Settings.highContrast);
        int lastWidth = 0, noSelect = 0;
        double lastScale = Settings.fontScale;
        final javax.swing.Timer watcher, searchTimer;

        Controller(MapViewer mv, JDialog dialog) {
            this.mv = mv;
            this.dialog = dialog;
            root.setBackground(Theme.PANEL);

            // ---- contents tree
            for (String g : HelpContent.GROUPS) {
                DefaultMutableTreeNode gn = new DefaultMutableTreeNode(g);
                for (HelpContent.Section s : HelpContent.inGroup(g)) gn.add(new DefaultMutableTreeNode(s));
                if (gn.getChildCount() > 0) treeRoot.add(gn);
            }
            tree = new JTree(new DefaultTreeModel(treeRoot));
            tree.setRootVisible(false);
            tree.setShowsRootHandles(true);
            tree.setBackground(Theme.FIELD);
            tree.setFont(Theme.plain(15));
            tree.setRowHeight(0);
            tree.setBorder(new EmptyBorder(4, 4, 4, 4));
            tree.setCellRenderer(new TreeRenderer());
            for (int i = 0; i < tree.getRowCount(); i++) tree.expandRow(i);
            tree.addTreeSelectionListener(e -> {
                if (noSelect > 0) return;
                TreePath tp = e.getNewLeadSelectionPath();
                if (tp == null) return;
                Object o = ((DefaultMutableTreeNode) tp.getLastPathComponent()).getUserObject();
                if (o instanceof HelpContent.Section) go(((HelpContent.Section) o).id, true);
            });
            JScrollPane treeScroll = new JScrollPane(tree);
            treeScroll.setBorder(null);
            treeScroll.getViewport().setBackground(Theme.FIELD);
            treeScroll.getVerticalScrollBar().setUnitIncrement(16);

            // ---- search results
            results.setBackground(Theme.FIELD);
            results.setCellRenderer(new ResultRenderer());
            results.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            results.addListSelectionListener(e -> { if (!e.getValueIsAdjusting() && noSelect == 0 && results.getSelectedValue() != null) go(results.getSelectedValue().id, true); });
            JScrollPane resScroll = new JScrollPane(results);
            resScroll.setBorder(null);
            resScroll.getViewport().setBackground(Theme.FIELD);
            left.add(treeScroll, "tree");
            left.add(resScroll, "results");
            left.setPreferredSize(new Dimension(280, 100));

            // ---- toolbar
            back = mv.makeBtn("◀", () -> step(-1));
            fwd = mv.makeBtn("▶", () -> step(1));
            home = mv.makeBtn("⌂ Home", () -> go("start", true));
            back.setToolTipText("Back"); fwd.setToolTipText("Forward"); home.setToolTipText("The start page");
            theme = mv.makeBtn(paper ? "☾ Ink" : "☀ Paper", () -> { });
            theme.setToolTipText("Switch the page between warm paper and dark ink");
            theme.addActionListener(e -> { paper = !paper; PREFS.putBoolean("paper", paper); theme.setText(paper ? "☾ Ink" : "☀ Paper"); Theme.fixSymbols(theme, theme.getText()); render(true); });
            search.setFont(Theme.plain(15));
            search.putClientProperty("JTextField.placeholderText", "Search the Help…  (e.g. flee, auction, script)");
            search.putClientProperty("JTextField.showClearButton", Boolean.TRUE);
            search.setToolTipText("Type to find pages. Every word must appear. Enter opens the best match, Esc clears.");
            searchTimer = new javax.swing.Timer(220, e -> runSearch());
            searchTimer.setRepeats(false);
            search.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { searchTimer.restart(); }
                public void removeUpdate(DocumentEvent e) { searchTimer.restart(); }
                public void changedUpdate(DocumentEvent e) { searchTimer.restart(); }
            });
            search.addActionListener(e -> { searchTimer.stop(); runSearch(); if (resultModel.size() > 0) { noSelect++; results.setSelectedIndex(0); noSelect--; go(resultModel.get(0).id, true); } });
            search.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ESCAPE) search.setText(""); if (e.getKeyCode() == KeyEvent.VK_DOWN && resultModel.size() > 0) { results.requestFocusInWindow(); if (results.getSelectedIndex() < 0) results.setSelectedIndex(0); } }
            });
            JPanel tools = new JPanel(new BorderLayout(8, 0));
            tools.setBackground(Theme.PANEL2);
            tools.setBorder(new EmptyBorder(6, 8, 6, 8));
            JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            nav.setOpaque(false);
            nav.add(back); nav.add(fwd); nav.add(home);
            JPanel mid = new JPanel(new BorderLayout());
            mid.setOpaque(false);
            mid.add(search, BorderLayout.CENTER);
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
            right.setOpaque(false);
            right.add(theme);
            tools.add(nav, BorderLayout.WEST); tools.add(mid, BorderLayout.CENTER); tools.add(right, BorderLayout.EAST);

            // ---- page
            page.setEditable(false);
            HTMLEditorKit kit = new HTMLEditorKit();
            page.setEditorKit(kit);
            page.setOpaque(true);
            page.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.FALSE);
            page.addHyperlinkListener(e -> {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) link(e.getDescription());
                else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) status.setText(describeLink(e.getDescription()));
                else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) status.setText(" ");
            });
            pageScroll = new JScrollPane(page);
            pageScroll.setBorder(null);
            pageScroll.getVerticalScrollBar().setUnitIncrement(22);
            pageScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            crumb.setForeground(Theme.DIM);
            crumb.setFont(Theme.plain(13));
            status.setForeground(Theme.DIM);
            status.setFont(Theme.plain(12));
            prev = mv.makeBtn("◀ Previous page", () -> neighbour(-1));
            next = mv.makeBtn("Next page ▶", () -> neighbour(1));
            JPanel foot = new JPanel(new BorderLayout(8, 0));
            foot.setBackground(Theme.PANEL2);
            foot.setBorder(new EmptyBorder(5, 8, 5, 8));
            JPanel footL = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); footL.setOpaque(false); footL.add(prev);
            JPanel footR = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); footR.setOpaque(false); footR.add(next);
            foot.add(footL, BorderLayout.WEST); foot.add(status, BorderLayout.CENTER); foot.add(footR, BorderLayout.EAST);
            JPanel pageBox = new JPanel(new BorderLayout());
            pageBox.add(pageScroll, BorderLayout.CENTER);
            pageBox.add(foot, BorderLayout.SOUTH);

            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, pageBox);
            split.setDividerLocation(280);
            split.setContinuousLayout(true);
            split.setDividerSize(5);
            root.add(tools, BorderLayout.NORTH);
            root.add(split, BorderLayout.CENTER);

            watcher = new javax.swing.Timer(600, e -> {
                int w = pageScroll.getViewport().getWidth();
                if (Math.abs(w - lastWidth) > 24 || Math.abs(Settings.fontScale - lastScale) > 0.001) render(true);
            });
            watcher.start();
            go("start", true);
        }

        void stop() { watcher.stop(); searchTimer.stop(); }

        // ---- navigation
        void go(String target, boolean push) {
            String id = target, anc = null;
            int hash = target.indexOf('#');
            if (hash >= 0) { id = target.substring(0, hash); anc = target.substring(hash + 1); }
            HelpContent.Section s = HelpContent.get(id);
            if (s == null) { s = HelpContent.get("start"); anc = null; }
            if (push && (cur == null || !cur.id.equals(s.id) || anc != null)) {
                while (history.size() > hpos + 1) history.remove(history.size() - 1);
                history.add(s.id + (anc != null ? "#" + anc : ""));
                hpos = history.size() - 1;
            }
            cur = s;
            anchor = anc;
            render(false);
            selectInTree(s);
            updateButtons();
        }

        void step(int d) {
            int np = hpos + d;
            if (np < 0 || np >= history.size()) return;
            hpos = np;
            go(history.get(hpos), false);
        }

        void neighbour(int d) {
            List<HelpContent.Section> all = HelpContent.all();
            int i = all.indexOf(cur) + d;
            if (i >= 0 && i < all.size()) go(all.get(i).id, true);
        }

        void updateButtons() {
            back.setEnabled(hpos > 0);
            fwd.setEnabled(hpos < history.size() - 1);
            List<HelpContent.Section> all = HelpContent.all();
            int i = all.indexOf(cur);
            prev.setEnabled(i > 0); next.setEnabled(i >= 0 && i < all.size() - 1);
            prev.setToolTipText(i > 0 ? all.get(i - 1).title : null); next.setToolTipText(i >= 0 && i < all.size() - 1 ? all.get(i + 1).title : null);
            crumb.setText(cur == null ? " " : cur.group + "  ›  " + cur.title);
            dialog.setTitle("Help — " + (cur == null ? "" : cur.title));
        }

        void selectInTree(HelpContent.Section s) {
            noSelect++;
            try {
                for (int i = 0; i < treeRoot.getChildCount(); i++) {
                    DefaultMutableTreeNode gn = (DefaultMutableTreeNode) treeRoot.getChildAt(i);
                    for (int k = 0; k < gn.getChildCount(); k++) {
                        DefaultMutableTreeNode n = (DefaultMutableTreeNode) gn.getChildAt(k);
                        if (n.getUserObject() == s) { TreePath tp = new TreePath(n.getPath()); tree.setSelectionPath(tp); tree.scrollPathToVisible(tp); return; }
                    }
                }
                tree.clearSelection();
            } finally { noSelect--; }
        }

        // ---- rendering
        void render(boolean keepScroll) {
            if (cur == null) return;
            int w = Math.max(420, pageScroll.getViewport().getWidth() > 0 ? pageScroll.getViewport().getWidth() : dialog.getWidth() - 320);
            lastWidth = pageScroll.getViewport().getWidth();
            lastScale = Settings.fontScale;
            HelpContent.Palette pal = paper ? HelpContent.paperPalette() : HelpContent.inkPalette();
            String html = HelpContent.page(cur, pal, w, Settings.fontScale);
            if (!query.isEmpty()) html = HelpContent.highlight(html, query, pal);
            final int pos = keepScroll ? pageScroll.getVerticalScrollBar().getValue() : 0;
            page.setBackground(Color.decode(pal.bg));
            pageScroll.getViewport().setBackground(Color.decode(pal.bg));
            page.setText(html);
            final String anc = keepScroll ? null : anchor;
            SwingUtilities.invokeLater(() -> {
                if (anc != null) page.scrollToReference(anc);
                else { page.setCaretPosition(0); pageScroll.getVerticalScrollBar().setValue(pos); }
            });
        }

        // ---- search
        void runSearch() {
            String q = search.getText().trim();
            query = q;
            if (q.isEmpty()) { cards.show(left, "tree"); status.setText(" "); render(true); return; }
            List<HelpContent.Section> hits = HelpContent.search(q);
            noSelect++;
            resultModel.clear();
            for (HelpContent.Section s : hits) resultModel.addElement(s);
            noSelect--;
            cards.show(left, "results");
            status.setText(hits.isEmpty() ? "No page mentions all of: " + q : hits.size() + " page" + (hits.size() == 1 ? "" : "s") + " match");
            ResultRenderer.query = q;
            results.repaint();
            render(true);
        }

        // ---- links
        void link(String d) {
            if (d == null) return;
            if (d.startsWith("help:")) go(d.substring(5), true);
            else if (d.startsWith("win:")) openWindow(mv, d.substring(4));
            else if (d.startsWith("cmd:")) mv.queueInput(d.substring(4));
        }

        String describeLink(String d) {
            if (d == null) return " ";
            if (d.startsWith("help:")) { String id = d.substring(5); int h = id.indexOf('#'); return "Go to: " + HelpContent.title(h >= 0 ? id.substring(0, h) : id); }
            if (d.startsWith("win:")) return d.equals("win:tour") ? "Start the guided tour" : "Opens the " + d.substring(4).replace('-', ' ') + " window";
            if (d.startsWith("cmd:")) return "Runs the command: " + d.substring(4);
            return d;
        }
    }

    // ------------------------------------------------------------------ renderers
    private static final class TreeRenderer extends DefaultTreeCellRenderer {
        TreeRenderer() { setOpenIcon(null); setClosedIcon(null); setLeafIcon(null); setBackgroundSelectionColor(Theme.SELECT); setBackgroundNonSelectionColor(Theme.FIELD); setBorderSelectionColor(null); }
        @Override public Component getTreeCellRendererComponent(JTree t, Object v, boolean sel, boolean exp, boolean leaf, int row, boolean focus) {
            super.getTreeCellRendererComponent(t, v, sel, exp, leaf, row, focus);
            Object o = ((DefaultMutableTreeNode) v).getUserObject();
            setOpaque(true);
            setBorder(new EmptyBorder(3, 4, 3, 4));
            if (o instanceof HelpContent.Section) {
                setText(((HelpContent.Section) o).title);
                setFont(t.getFont());
                setForeground(sel ? Color.WHITE : Theme.TEXT);
                setBackground(sel ? Theme.SELECT : Theme.FIELD);
            } else {
                setText(String.valueOf(o).toUpperCase());
                setFont(t.getFont().deriveFont(Font.BOLD, t.getFont().getSize2D() * 0.84f));
                setForeground(Theme.GOLD);
                setBackground(Theme.FIELD);
                setBorder(new EmptyBorder(8, 2, 3, 4));
            }
            return this;
        }
    }

    private static final class ResultRenderer extends JLabel implements ListCellRenderer<HelpContent.Section> {
        static String query = "";
        ResultRenderer() { setOpaque(true); setBorder(new EmptyBorder(5, 8, 5, 8)); }
        @Override public Component getListCellRendererComponent(JList<? extends HelpContent.Section> l, HelpContent.Section s, int i, boolean sel, boolean focus) {
            String sn = HelpContent.esc(HelpContent.snippet(s, query));
            setText("<html><div style='width:215px'><b>" + HelpContent.esc(s.title) + "</b> <font color='" + Theme.hex(Theme.GOLD) + "' size='-1'>" + HelpContent.esc(s.group) + "</font><br><font color='" + Theme.hex(Theme.DIM) + "' size='-1'>" + sn + "</font></div></html>");
            setFont(l.getFont());
            setForeground(sel ? Color.WHITE : Theme.TEXT);
            setBackground(sel ? Theme.SELECT : Theme.FIELD);
            return this;
        }
    }

    // ------------------------------------------------------------------ welcome tutorial with the highlighted tour
    /** The first-run welcome / tour window (title "Tutorial"): a few pages, each one lighting up the part of the screen it talks about. */
    static void tutorial(final MapViewer mv) {
        if (!SwingUtilities.isEventDispatchThread()) { SwingUtilities.invokeLater(() -> tutorial(mv)); return; }
        GuiWindows.window(mv, "tutorial", "Tutorial", 600, 460, 3_600_000, (d, refreshers) -> {
            final List<Hints.Page> pages = Hints.tutorial();
            final int[] at = {0};
            final Spotlight spot = new Spotlight();
            final Component oldGlass = mv.getGlassPane();
            final boolean oldVisible = oldGlass.isVisible();
            mv.setGlassPane(spot);
            final JEditorPane body = new JEditorPane();
            body.setEditable(false);
            body.setEditorKit(new HTMLEditorKit());
            body.addHyperlinkListener(e -> {
                if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED || e.getDescription() == null) return;
                String ds = e.getDescription();
                if (ds.startsWith("help:")) open(mv, ds.substring(5));
                else if (ds.startsWith("win:")) openWindow(mv, ds.substring(4));
            });
            JLabel step = new JLabel(" "), title = new JLabel(" ");
            step.setFont(Theme.plain(13)); step.setForeground(Theme.DIM);
            title.setFont(Theme.serif(22)); title.setForeground(Theme.GOLD);
            JPanel head = new JPanel(new BorderLayout());
            head.setBackground(Theme.PANEL2);
            head.setBorder(new EmptyBorder(10, 16, 8, 16));
            head.add(step, BorderLayout.NORTH); head.add(title, BorderLayout.CENTER);
            final JButton prev = mv.makeBtn("◀ Back", () -> { }), nextBtn = mv.makeBtn("Next ▶", () -> { });
            final JButton helpBtn = mv.makeBtn("Open the Help", () -> { });
            final JLabel pos = new JLabel(" ");
            pos.setForeground(Theme.DIM);
            final Runnable show = () -> {
                Hints.Page pg = pages.get(at[0]);
                HelpContent.Palette pal = HelpContent.paperPalette();
                StringBuilder sb = new StringBuilder("<html><head><style>body { font-family: 'Segoe UI', SansSerif; font-size: " + Math.round(15 * Settings.fontScale) + "px; color: " + pal.text + "; background-color: " + pal.bg
                        + "; margin: 12px 18px; } p { margin-top: 0px; margin-bottom: 10px; } a { color: " + pal.link + "; } code { font-family: Consolas; color: " + pal.code + "; }</style></head><body>");
                for (String para : pg.body.split("\n\n")) sb.append("<p>").append(HelpContent.inline(para.replace("\n", " "), pal)).append("</p>");
                if (pg.helpId != null) sb.append("<p>More: <a href='help:").append(pg.helpId).append("'>").append(HelpContent.esc(HelpContent.title(pg.helpId))).append("</a></p>");
                sb.append("</body></html>");
                body.setBackground(Color.decode(pal.bg));
                body.setText(sb.toString());
                body.setCaretPosition(0);
                title.setText(pg.title);
                step.setText("Step " + (at[0] + 1) + " of " + pages.size() + (pg.region != null ? "   —   look at the highlighted part of the window" : ""));
                pos.setText("  " + (at[0] + 1) + " / " + pages.size());
                prev.setEnabled(at[0] > 0);
                nextBtn.setText(at[0] == pages.size() - 1 ? "Done" : at[0] == 0 ? "Take the tour ▶" : "Next ▶");
                Theme.fixSymbols(nextBtn, nextBtn.getText());
                Rectangle r = pg.region == null ? null : regionRect(mv, pg.region, spot);
                spot.setTarget(r);
                if (r != null) placeAway(d, mv, r, spot); else if (at[0] == 0 || at[0] == pages.size() - 1) d.setLocationRelativeTo(mv);
            };
            for (java.awt.event.ActionListener l : prev.getActionListeners()) prev.removeActionListener(l);
            for (java.awt.event.ActionListener l : nextBtn.getActionListeners()) nextBtn.removeActionListener(l);
            for (java.awt.event.ActionListener l : helpBtn.getActionListeners()) helpBtn.removeActionListener(l);
            prev.addActionListener(e -> { if (at[0] > 0) { at[0]--; show.run(); } });
            nextBtn.addActionListener(e -> { if (at[0] < pages.size() - 1) { at[0]++; show.run(); } else d.dispose(); });
            helpBtn.addActionListener(e -> { String id = pages.get(at[0]).helpId; open(mv, id == null ? "start" : id); });
            JCheckBox hints = new JCheckBox("Show one-time hints during play", Settings.hints);
            hints.setForeground(Theme.TEXT); hints.setOpaque(false); hints.setFont(Theme.plain(13));
            hints.addActionListener(e -> { Settings.hints = hints.isSelected(); Settings.save(); });
            JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
            south.setBackground(Theme.PANEL2);
            south.add(prev); south.add(nextBtn); south.add(helpBtn); south.add(pos); south.add(hints);
            d.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosed(java.awt.event.WindowEvent e) { spot.setVisible(false); mv.setGlassPane(oldGlass); oldGlass.setVisible(oldVisible); }
            });
            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(Theme.PANEL);
            JScrollPane bs = new JScrollPane(body);
            bs.setBorder(null);
            root.add(head, BorderLayout.NORTH); root.add(bs, BorderLayout.CENTER); root.add(south, BorderLayout.SOUTH);
            SwingUtilities.invokeLater(show);
            return root;
        });
    }

    /** Moves the tutorial window to the corner of the main window that overlaps the lit region the least. */
    private static void placeAway(JDialog d, MapViewer mv, Rectangle region, Component glass) {
        if (!mv.isShowing() || !glass.isShowing()) return;
        Point gp = glass.getLocationOnScreen();
        Rectangle lit = new Rectangle(gp.x + region.x, gp.y + region.y, region.width, region.height);
        Rectangle win = mv.getBounds();
        Dimension ds = d.getSize();
        int m = 20;
        Point[] cands = {new Point(win.x + m, win.y + win.height - ds.height - m), new Point(win.x + win.width - ds.width - m, win.y + win.height - ds.height - m),
                new Point(win.x + win.width - ds.width - m, win.y + 120), new Point(win.x + m, win.y + 120),
                new Point(win.x + (win.width - ds.width) / 2, win.y + win.height - ds.height - m)};
        Point best = cands[0];
        long bestOverlap = Long.MAX_VALUE;
        for (Point c : cands) {
            Rectangle r = new Rectangle(c.x, c.y, ds.width, ds.height).intersection(lit);
            long ov = r.isEmpty() ? 0 : (long) r.width * r.height;
            if (ov < bestOverlap) { bestOverlap = ov; best = c; }
        }
        d.setLocation(best);
    }

    /** The glass-pane rectangle of a named part of the main window, or null when it cannot be found. */
    static Rectangle regionRect(MapViewer mv, String region, Component glass) {
        Component c = null;
        Container root = mv.getContentPane();
        switch (region) {
            case "map": c = find(root, x -> x.getClass().getSimpleName().equals("MapPanel")); break;
            case "layers": { Component t = find(root, x -> x instanceof JToggleButton && "Fog".equals(((JToggleButton) x).getText())); c = t == null ? null : t.getParent(); break; }
            case "tile": c = find(root, x -> x instanceof JScrollPane && ((JScrollPane) x).getBorder() instanceof javax.swing.border.TitledBorder && "Tile Info".equals(((javax.swing.border.TitledBorder) ((JScrollPane) x).getBorder()).getTitle())); break;
            case "dock": c = find(root, x -> x instanceof JTabbedPane && ((JTabbedPane) x).indexOfTab("Move") >= 0); break;
            case "status": { Component t = find(root, x -> x instanceof JProgressBar); c = t == null ? null : t.getParent(); break; }
            case "speed": { Component t = find(root, x -> x instanceof JButton && ((JButton) x).getText() != null && ((JButton) x).getText().contains("Pause")); c = t == null ? null : t.getParent(); break; }
            case "log": c = find(root, x -> x instanceof JTabbedPane && ((JTabbedPane) x).indexOfTab("Events") >= 0); break;
            default: break;
        }
        if (c == null || !c.isShowing()) return null;
        Rectangle r = new Rectangle(0, 0, c.getWidth(), c.getHeight());
        if (region.equals("map")) { // the world is a centred square inside its panel
            int ts = Math.max(4, Math.min(c.getWidth(), c.getHeight()) / Map.range);
            int side = ts * Map.range;
            r = new Rectangle(Math.max(0, (c.getWidth() - side) / 2), Math.max(0, (c.getHeight() - side) / 2), side, side);
        }
        Rectangle out = SwingUtilities.convertRectangle(c, r, glass);
        return out;
    }

    private interface Test { boolean ok(Component c); }

    private static Component find(Container root, Test t) {
        for (Component k : root.getComponents()) {
            if (t.ok(k)) return k;
            if (k instanceof Container) { Component f = find((Container) k, t); if (f != null) return f; }
        }
        return null;
    }

    /** A dimmed overlay with a bright hole around the part of the window being explained; it never takes mouse events. */
    private static final class Spotlight extends JComponent {
        private Rectangle target;
        Spotlight() { setOpaque(false); }
        void setTarget(Rectangle r) { target = r; setVisible(r != null); repaint(); }
        @Override public boolean contains(int x, int y) { return false; } // clicks go through to the game
        @Override protected void paintComponent(Graphics g0) {
            if (target == null) return;
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle t = new Rectangle(target.x - 4, target.y - 4, target.width + 8, target.height + 8);
            Area shade = new Area(new Rectangle(0, 0, getWidth(), getHeight()));
            shade.subtract(new Area(new RoundRectangle2D.Double(t.x, t.y, t.width, t.height, 14, 14)));
            g.setColor(new Color(0, 0, 0, 125));
            g.fill(shade);
            g.setColor(Theme.alpha(Theme.GOLD, 70)); g.setStroke(new BasicStroke(9f));
            g.draw(new RoundRectangle2D.Double(t.x, t.y, t.width, t.height, 14, 14));
            g.setColor(Theme.GOLD); g.setStroke(new BasicStroke(3f));
            g.draw(new RoundRectangle2D.Double(t.x, t.y, t.width, t.height, 14, 14));
            g.dispose();
        }
    }
}
