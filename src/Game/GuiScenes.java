package Game;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/** One window for every scene of the story and the lore. A scene is cut into pages (a page is a block of narration with the lines spoken after it), the window shows one page at a time
 *  with Back / Continue, and the last page shows the choice buttons (a scene with a question) or the closing button. Scenes that arrive while a window is open wait their turn in a queue,
 *  so there is never more than one scene window, whatever the story and the Palace say in the same week. The game waits only for a scene with a question (`ask`).
 *  The pause the scene asks for (`settings lorepause`) is held until the last queued scene is closed. Closing the window with the X skips to the end of the scene: the default answer is taken.
 *  Window code runs on the Swing thread; `enqueue`, `ask`, `busy` and `supplyAfter` are safe from any thread. */
final class GuiScenes {
    private GuiScenes() { }

    static final int PAGE_MAX_LINES = 8;   // a long block of speech is cut into pages of this many lines

    static final class Scene {
        final String who, title, stats, button;
        final List<List<String>> pages;
        final List<String> credits;
        String prompt; String[] options; int dflt = -1; int timeout = 0;
        final CountDownLatch answered = new CountDownLatch(1);
        volatile int picked = -1;
        volatile boolean afterSupplied = false;      // the game thread has said what follows the chosen answer (maybe nothing)
        volatile List<List<String>> afterPages = null;
        String artKey = null;                         // the picture of the place the scene happens in (StoryArt), with the portraits of the people who speak
        List<String> faceNames = null; List<java.awt.image.BufferedImage> faceImages = null;
        @SuppressWarnings("unchecked")
        Scene(String who, String title, List<String> lines, String stats, String button, List<String> credits) {
            this.who = who; this.title = title; this.pages = paginate(lines); this.stats = stats; this.button = button; this.credits = credits;
            Object[] art = StoryArt.take();                 // set by whoever shows the scene (Chapters.play, GuiLore.scene, LoreEndings.present): nothing for a scene that has no place
            artKey = (String) art[0]; faceNames = (List<String>) art[1]; faceImages = (List<java.awt.image.BufferedImage>) art[2];
        }
    }

    private static final Object LOCK = new Object();
    private static final ArrayDeque<Scene> queue = new ArrayDeque<>();
    private static int open = 0;                      // scenes waiting or on screen, plus credits being read
    private static boolean weHoldThePause = false;
    private static JDialog window = null;             // Swing thread only
    private static volatile Scene lastAsked = null;   // the scene whose answer the game thread is just handling (see supplyAfter)
    static volatile int maxWindows = 0;               // the most scene windows seen open at once (tests)

    /** True while a scene is queued or open (counted at once, not when the window appears). */
    static boolean busy() { synchronized (LOCK) { return open > 0; } }

    // ------------------------------------------------------------------ pages
    /** Cuts a scene's lines into pages: a new page begins at narration that follows spoken lines, and a page holds at most PAGE_MAX_LINES lines. */
    static List<List<String>> paginate(List<String> lines) {
        List<List<String>> pages = new ArrayList<>();
        List<String> cur = new ArrayList<>();
        boolean lastSpoken = false;
        if (lines != null) for (String l : lines) {
            boolean narr = LoreText.narration(l);
            if ((narr && lastSpoken && !cur.isEmpty()) || cur.size() >= PAGE_MAX_LINES) { pages.add(cur); cur = new ArrayList<>(); }
            cur.add(l);
            lastSpoken = !narr;
        }
        if (!cur.isEmpty() || pages.isEmpty()) pages.add(cur);
        return pages;
    }

    // ------------------------------------------------------------------ queue
    static void enqueue(Scene s) {
        if (GameDriver.mapViewer == null) { s.answered.countDown(); return; }
        synchronized (LOCK) {
            open++; queue.add(s);
            if (Settings.lorePause && !GameDriver.paused) { GameDriver.paused = true; weHoldThePause = true; } // the game waits until the scenes are read
        }
        SwingUtilities.invokeLater(GuiScenes::pump);
    }

    /** A scene with a question: the game thread waits (the world unlocked) for the answer; closing the window or the decision timeout picks `dflt`. What follows the answer is given to `supplyAfter`. */
    static int ask(Character c, String who, String title, List<String> lines, String prompt, String[] options, int dflt) {
        if (GameDriver.mapViewer == null) return dflt;
        DecisionBroker.prompts++;
        Scene s = new Scene(who, title, lines, null, "Continue", null);
        s.prompt = prompt; s.options = options; s.dflt = dflt; s.timeout = DecisionBroker.timeoutOf(c);
        lastAsked = s;
        enqueue(s);
        try { GameDriver.blocked(() -> { s.answered.await(); return null; }); } catch (RuntimeException ignored) { }
        return s.picked < 0 ? dflt : s.picked;
    }

    /** The game thread says what follows the answer it has just handled (null or empty: nothing); the window shows it as more pages of the same scene and then closes. */
    static void supplyAfter(List<String> lines) {
        Scene s = lastAsked;
        if (s == null) return;
        s.afterPages = lines == null || lines.isEmpty() ? null : paginate(lines);
        s.afterSupplied = true;
    }

    /** Keeps the game paused and `busy()` true for something that is not a queued scene (the credits). */
    static void hold() { synchronized (LOCK) { open++; } }
    static void release() { SwingUtilities.invokeLater(() -> { finished(); }); }

    private static void finished() {
        synchronized (LOCK) {
            open = Math.max(0, open - 1);
            if (open == 0 && weHoldThePause) { weHoldThePause = false; GameDriver.paused = false; }
        }
        pump();
    }

    private static void pump() {
        if (window != null) return;
        Scene s;
        synchronized (LOCK) { s = queue.poll(); }
        if (s == null) return;
        display(s);
    }

    // ------------------------------------------------------------------ the window
    private static void display(final Scene s) {
        final MapViewer mv = GameDriver.mapViewer;
        final JDialog d = new JDialog(mv, s.who + "  ·  " + s.title, false);
        window = d;
        int windows = 0;
        for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && ((JDialog) w).getTitle() != null && ((JDialog) w).getTitle().contains("  ·  ")) windows++;
        maxWindows = Math.max(maxWindows, windows + 1);
        d.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        d.getContentPane().setBackground(Theme.BG);
        final JEditorPane ep = GuiSheets.view(mv);
        final JScrollPane sp = new JScrollPane(ep);
        sp.setBorder(BorderFactory.createEmptyBorder());
        final JPanel south = new JPanel(new BorderLayout());
        south.setBackground(Theme.BG);
        south.setBorder(BorderFactory.createEmptyBorder(6, 10, 10, 10));
        d.add(sp, BorderLayout.CENTER);
        d.add(south, BorderLayout.SOUTH);
        final boolean hasArt = s.artKey != null && Settings.pictures;
        if (hasArt) { try { d.add(artPanel(s), BorderLayout.WEST); } catch (RuntimeException ex) { } }
        final int[] page = {0};
        final boolean[] inAfter = {false, false};       // [0] showing what follows the answer, [1] the answer is in and the window waits for the game thread to say what follows
        @SuppressWarnings("unchecked") final List<List<String>>[] cur = (List<List<String>>[]) new List[]{s.pages};
        final javax.swing.Timer[] timers = new javax.swing.Timer[2]; // [0] the decision timeout, [1] the wait for the "after" pages
        final Runnable[] render = new Runnable[1];
        final Runnable close = () -> {
            for (javax.swing.Timer t : timers) if (t != null) t.stop();
            if (s.options != null && s.picked < 0) { s.picked = s.dflt; s.answered.countDown(); }  // the X (or the end of the time): the default answer
            s.answered.countDown();
            d.dispose();
            window = null;
            if (s.credits != null) { hold(); GuiLore.credits(mv, s.credits); }
            finished();
        };
        render[0] = () -> {
            List<List<String>> pages = cur[0];
            int last = pages.size() - 1;
            boolean lastPage = page[0] >= last;
            boolean finalMain = lastPage && !inAfter[0];
            ep.setText(GuiLore.sceneHtml(s.title, pages.get(Math.min(page[0], last)), lastPage && !inAfter[0] ? s.stats : null));
            ep.setCaretPosition(0);
            south.removeAll();
            JPanel buttons = new JPanel(new GridLayout(0, 1, 0, 4));
            buttons.setBackground(Theme.BG);
            JButton def = null;
            if (inAfter[1]) {
                JLabel wait = new JLabel(" ...");
                wait.setForeground(Theme.DIM);
                buttons.add(wait);
            } else if (finalMain && s.options != null && s.picked < 0) {
                JLabel ask = new JLabel("<html><b>" + GuiSheets.esc(s.prompt) + "</b></html>");
                ask.setForeground(Theme.GOLD);
                buttons.add(ask);
                for (int i = 0; i < s.options.length; i++) {
                    final int pick = i;
                    JButton b = mv.makeBtn(s.options[i], () -> { s.picked = pick; s.answered.countDown(); if (timers[0] != null) timers[0].stop(); beginAfter(s, d, inAfter, cur, page, render, timers, close); });
                    buttons.add(b);
                    if (i == s.dflt) def = b;
                }
            } else {
                JPanel row = GuiWindows.flow(FlowLayout.RIGHT);
                row.setBackground(Theme.BG);
                if (pages.size() > 1) { JLabel n = new JLabel("page " + (page[0] + 1) + " of " + pages.size() + "   "); n.setForeground(Theme.DIM); row.add(n); }
                if (page[0] > 0) row.add(mv.makeBtn("◂ Back", () -> { page[0]--; render[0].run(); }));
                JButton go;
                if (!lastPage) go = mv.makeBtn("Continue ▸", () -> { page[0]++; render[0].run(); });
                else go = mv.makeBtn(inAfter[0] ? "Continue" : s.button, close);
                row.add(go);
                def = go;
                buttons.add(row);
            }
            south.add(buttons, BorderLayout.CENTER);
            if (def != null) d.getRootPane().setDefaultButton(def);
            south.revalidate(); south.repaint();
            if (def != null) def.requestFocusInWindow();
        };
        d.addWindowListener(new java.awt.event.WindowAdapter() { @Override public void windowClosing(java.awt.event.WindowEvent e) { close.run(); } });
        render[0].run();
        d.setSize(hasArt ? 1010 : 680, 540);
        d.setLocationRelativeTo(mv);
        Ui.prepare(d);
        if (s.options != null && s.timeout > 0) { timers[0] = new javax.swing.Timer(s.timeout * 1000, e -> close.run()); timers[0].setRepeats(false); timers[0].start(); }
        d.setVisible(true);
    }

    /** The left side of a scene window: the picture of the place and a portrait for each person who speaks. */
    static JPanel artPanel(Scene s) {
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(Theme.BG);
        left.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));
        java.awt.image.BufferedImage pic = StoryArt.picture(s.artKey, 7, StoryArt.W, StoryArt.H);
        JLabel lbl = new JLabel(new ImageIcon(pic.getScaledInstance(340, 191, java.awt.Image.SCALE_SMOOTH)));
        lbl.setBorder(BorderFactory.createLineBorder(Theme.GOLD, 1));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(lbl);
        if (s.faceImages != null && !s.faceImages.isEmpty()) {
            left.add(Box.createVerticalStrut(10));
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            row.setBackground(Theme.BG);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            for (int i = 0; i < s.faceImages.size(); i++) {
                JPanel one = new JPanel(new BorderLayout());
                one.setBackground(Theme.BG);
                JLabel face = new JLabel(new ImageIcon(s.faceImages.get(i)));
                face.setBorder(BorderFactory.createLineBorder(Theme.DIM, 1));
                one.add(face, BorderLayout.CENTER);
                JLabel nm = new JLabel(i < s.faceNames.size() ? s.faceNames.get(i) : "", SwingConstants.CENTER);
                nm.setForeground(Theme.GOLD); nm.setFont(Theme.plain(12));
                one.add(nm, BorderLayout.SOUTH);
                row.add(one);
            }
            left.add(row);
        }
        left.add(Box.createVerticalGlue());
        return left;
    }

    /** The answer is in: the window waits (briefly) for the game thread to say what follows it, shows that as more pages, and closes at the end. */
    private static void beginAfter(final Scene s, final JDialog d, final boolean[] inAfter, final List<List<String>>[] cur, final int[] page, final Runnable[] render, final javax.swing.Timer[] timers, final Runnable close) {
        inAfter[1] = true;
        render[0].run();
        final long t0 = System.currentTimeMillis();
        timers[1] = new javax.swing.Timer(60, null);
        timers[1].addActionListener(e -> {
            if (!s.afterSupplied && System.currentTimeMillis() - t0 < 4000) return;
            timers[1].stop();
            inAfter[1] = false;
            if (s.afterPages != null) { inAfter[0] = true; cur[0] = s.afterPages; page[0] = 0; render[0].run(); }
            else close.run();
        });
        timers[1].start();
    }
}
