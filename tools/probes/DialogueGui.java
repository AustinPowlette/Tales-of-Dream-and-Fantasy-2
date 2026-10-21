package Game;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

/** Opens the real talk window (GuiDialogue): a conversation through the Talk command, a scene when someone speaks to the player, and a decision in someone's words. Needs a display.
 *  Screenshots go to -Dshots=<dir>. */
public class DialogueGui {
    static int passes = 0, fails = 0;
    static PrintStream real;
    static void check(String n, boolean ok) { if (ok) passes++; else fails++; real.println("[G] " + (ok ? "PASS " : "FAIL ") + n); }

    static JDialog dialogue() {
        for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && "dialogue".equals(w.getName())) return (JDialog) w;
        return null;
    }

    static JDialog awaitDialogue(int ms) throws Exception {
        long end = System.currentTimeMillis() + ms;
        while (System.currentTimeMillis() < end) { JDialog d = dialogue(); if (d != null) { Thread.sleep(300); return dialogue(); } Thread.sleep(60); }
        return null;
    }

    static List<JButton> buttons(JDialog d) {
        List<JButton> bs = new ArrayList<>();
        PcProbeGui.collect(d.getContentPane(), JButton.class, bs);
        bs.removeIf(b -> b.getText() == null || !b.getText().matches("^\\d+\\..*")); // the replies (scroll bars have buttons too)
        return bs;
    }

    static String text(JDialog d) { return PcProbeGui.allText(d.getContentPane()); }

    static void shot(JDialog d, String name) throws Exception {
        String dir = System.getProperty("shots");
        if (dir == null || d == null) return;
        new File(dir).mkdirs();
        final BufferedImage[] im = {null};
        SwingUtilities.invokeAndWait(() -> { im[0] = new BufferedImage(d.getWidth(), d.getHeight(), BufferedImage.TYPE_INT_RGB); Graphics2D g = im[0].createGraphics(); d.paint(g); g.dispose(); });
        javax.imageio.ImageIO.write(im[0], "png", new File(dir, name));
    }

    static void clickButton(JDialog d, int i) throws Exception {
        SwingUtilities.invokeAndWait(() -> { List<JButton> bs = buttons(d); if (i < bs.size()) bs.get(i).doClick(); });
        Thread.sleep(350);
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "false");
        real = System.out;
        PcProbe.real = real;
        System.setIn(PcProbe.in);
        GameDriver.frozenNpcs = true; // frozen before the first tick: the NPCs act before the human prompt of a tick, and one of them could otherwise ask the character a question before the probe is ready
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        PcProbeGui.real = real; PcProbeGui.answerDialogues = false;
        Thread w = new Thread(PcProbeGui::watcher, "dialog-watcher"); w.setDaemon(true); w.start();
        try { run(); } catch (Throwable e) { real.println("[G] PROBE EXCEPTION " + e); e.printStackTrace(real); fails++; }
        real.println("[G] ===== " + passes + " passed, " + fails + " failed; gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }

    static void run() throws Exception {
        Settings.lore = false;
        PcProbe.in.feed("y\ns\nn\n");
        long end = System.currentTimeMillis() + 240000;
        while (System.currentTimeMillis() < end && !(GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbe.pc() != null)) Thread.sleep(200);
        Character p = PcProbe.pc();
        real.println("[G] info: viewer=" + (GameDriver.mapViewer != null) + " waiting=" + GameDriver.waitingForInput + " pc=" + (p != null) + " tail=" + PcProbe.out().substring(Math.max(0, PcProbe.out().length() - 300)).replace((char) 10, (char) 32));
        check("GUI opened and the player is prompting", p != null && GameDriver.mapViewer != null);
        if (p == null) return;
        p.lvl = 60; p.majorLvl = 6; p.maxAge = 100000; p.caution = 0; p.ss = 5000;
        p.pc().squadPolicy = 2; p.pc().friendPolicy = 1;
        DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        GameDriver.frozenNpcs = true;
        Settings.lessons = false; Settings.vignettes = false; Settings.dialoguePopups = true; Settings.dialogueFlavor = true; Settings.dialogueAuto = false;
        // a chef who is a friend, in the player's tile
        Character chef = new Character(p.location);
        chef.age = 40; chef.gender = "Female"; chef.lvl = 50; chef.majorLvl = 5; chef.maxAge = 100000; chef.trait = Character.Traits.Chef; chef.affiliation = "Good"; chef.affiliationNum = 1; chef.player = 0; chef.professionLvl = 40;
        chef.id = Map.characters.size(); GameDriver.UID = Math.max(GameDriver.UID, chef.id + 1); Map.characters.add(chef); Map.population++;
        chef.location.removeChar(chef); chef.location = p.location; p.location.addChar(chef);
        DialogueProbe.setRel(chef, p, 2);

        // ---- a conversation through the Talk command
        GameDriver.guiInputQueue.add("people"); PcProbeGui.settle();
        int idx = p.pc().peopleView.indexOf(chef) + 1;
        check("the chef is listed among the people here", idx > 0);
        GameDriver.guiInputQueue.add("talk " + idx);
        JDialog d = awaitDialogue(15000);
        check("the Talk command opens the talk window", d != null);
        if (d == null) return;
        String first = text(d);
        check("the window shows the speaker, her words and replies (" + first.replace('\n', ' ').substring(0, Math.min(120, first.length())) + ")", first.contains(chef.name) && buttons(d).size() >= 4);
        Thread.sleep(500);
        shot(d, "dialogue-talk-1.png");
        List<JButton> bs = buttons(d);
        check("the first reply offered is a topic (' 1. ' numbering)", bs.get(0).getText().startsWith("1."));
        int before = text(d).length();
        clickButton(d, 0); // how have you been?
        d = dialogue();
        check("choosing a topic adds her answer to the window", d != null && text(d).length() > before);
        shot(d, "dialogue-talk-2.png");
        // keyboard: the digit 1 picks the first reply again
        int len2 = text(d).length();
        final JDialog dd = d;
        SwingUtilities.invokeAndWait(() -> { dd.getRootPane().requestFocusInWindow(); });
        clickButton(d, 0);
        d = dialogue();
        check("a second topic goes on in the same window", d != null && text(d).length() > len2);
        // say goodbye: the last reply, then Continue closes the window and the game takes the turn
        for (int i = 0; i < 12 && dialogue() != null; i++) {
            JDialog cur = dialogue();
            List<JButton> b2 = buttons(cur);
            if (b2.isEmpty()) break;
            clickButton(cur, b2.size() - 1);
        }
        long wait = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < wait && dialogue() != null) Thread.sleep(100);
        check("saying goodbye and Continue closes the window", dialogue() == null);
        PcProbeGui.settle();
        check("the game is waiting for the player's next command again", GameDriver.waitingForInput);

        // ---- a decision in someone's words
        Character suitor = new Character(p.location);
        suitor.age = 25; suitor.gender = "Male"; suitor.lvl = 40; suitor.majorLvl = 4; suitor.maxAge = 100000; suitor.trait = Character.Traits.Musician; suitor.affiliation = "Good"; suitor.affiliationNum = 1; suitor.player = 0;
        suitor.id = Map.characters.size(); GameDriver.UID = Math.max(GameDriver.UID, suitor.id + 1); Map.characters.add(suitor); Map.population++;
        suitor.location.removeChar(suitor); suitor.location = p.location; p.location.addChar(suitor);
        p.pc().friendPolicy = 0; // ask
        final boolean[] answered = {false}; final int[] ans = {-1};
        Thread q = new Thread(() -> {
            try { ans[0] = DecisionBroker.ask(p, "Suitor Name (Lv.40, Musician) would like to be your friend. Accept?", new String[]{"Accept", "Decline"}, 0, suitor, "friend"); answered[0] = true; } catch (Throwable e) { e.printStackTrace(real); }
        }, "asker");
        q.setDaemon(true); q.start();
        d = awaitDialogue(15000);
        check("a friendship offered by someone opens the window with their words", d != null && text(d).contains(suitor.name));
        if (d != null) {
            shot(d, "dialogue-decision.png");
            check("the window offers Accept and Decline", text(d).contains("Accept") || buttons(d).stream().anyMatch(b -> b.getText().contains("Accept")));
            clickButton(d, 1);
        }
        long w2 = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < w2 && !answered[0]) Thread.sleep(100);
        check("the answer reaches the game (Decline = " + ans[0] + ")", answered[0] && ans[0] == 1);
        check("no exceptions", !PcProbe.out().contains("Exception"));
    }
}
