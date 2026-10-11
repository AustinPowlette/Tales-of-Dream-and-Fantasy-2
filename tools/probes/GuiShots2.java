package Game;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

/** Renders the windows of the GUI overhaul (2026-10-10, fifth batch) to PNG files for a visual check: System property "shots" = output folder,
 *  "only" = comma list of scene names to run (default all). Needs a display. Scenes: backpack, editself, scripts, family, bio, run, story, chronicle, digest, market, manage, squad, place, rankings. */
public class GuiShots2 {
    static PrintStream real;
    static MapViewer mv;
    static Character p;

    static void onEdt(Runnable r) throws Exception { SwingUtilities.invokeAndWait(r); }

    static void shot(String name, Component c) throws Exception {
        final BufferedImage[] im = {null};
        onEdt(() -> { int w = Math.max(1, c.getWidth()), h = Math.max(1, c.getHeight()); im[0] = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB); Graphics2D g = im[0].createGraphics(); c.paint(g); g.dispose(); });
        File dir = new File(System.getProperty("shots", "shots")); dir.mkdirs();
        javax.imageio.ImageIO.write(im[0], "png", new File(dir, name + ".png"));
        real.println("SHOT " + name + " " + im[0].getWidth() + "x" + im[0].getHeight());
    }

    static Window win(String prefix) { return QolGui.win(prefix); }
    static Component content(Window w) { return w instanceof JDialog ? ((JDialog) w).getRootPane() : ((JFrame) w).getRootPane(); }

    /** Opens a window with `open`, waits, shoots it as `name`, closes it. */
    static void window(String name, String title, Runnable open) throws Exception {
        try { onEdt(open); } catch (Exception e) { real.println("OPEN FAILED " + name + " " + e); return; }
        Thread.sleep(1600);
        Window w = win(title);
        if (w == null) { real.println("NO WINDOW " + name + " (" + title + ")"); return; }
        shot(name, content(w));
        onEdt(w::dispose);
        Thread.sleep(200);
    }

    /** Sends a command through the GUI queue (the way the buttons do) and waits until the game asks for the next one. */
    static void cmd(String s) throws Exception {
        mv.queueInput(s);
        Thread.sleep(250);
        long end = System.currentTimeMillis() + 30000;
        while (System.currentTimeMillis() < end && !GameDriver.waitingForInput) Thread.sleep(50);
        Thread.sleep(150);
    }

    static boolean wanted(String s) {
        String only = System.getProperty("only", "");
        return only.isEmpty() || Arrays.asList(only.split(",")).contains(s);
    }

    static void tab(String windowTitle, String tabPrefix) throws Exception {
        Window w = win(windowTitle);
        if (w != null) onEdt(() -> GuiHub.selectTab(((JDialog) w).getContentPane(), tabPrefix));
        Thread.sleep(700);
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "false");
        real = System.out;
        PcProbe.real = real; PcProbeGui.real = real;
        System.setIn(PcProbe.in);
        GameDriver.frozenNpcs = true;
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        Thread w = new Thread(PcProbeGui::watcher, "dialog-watcher"); w.setDaemon(true); w.start();
        try { run(); } catch (Throwable e) { real.println("PROBE EXCEPTION " + e); e.printStackTrace(real); }
        System.exit(0);
    }

    /** Gives the character something to show: gear, materials, pills, arrays, food and wine. */
    static void dress(Character p) {
        Item sword = new Item(p, Item.WeaponType.Sword, 4); p.backpack.add(sword); p.equippedWeapon = sword;
        Item spear = new Item(p, Item.WeaponType.Spear, 6); spear.enchantment = "Flame"; spear.enchantBonus = 3; p.backpack.add(spear);
        Item fan = new Item(p, Item.WeaponType.Fan, 2); p.backpack.add(fan);
        Item robe = new Item(p, 5, Item.ItemType.Clothing); p.backpack.add(robe);
        Item robe2 = new Item(p, 3, Item.ItemType.Clothing); p.backpack.add(robe2); p.equippedClothing = robe2;
        p.materialBag.add(new Item(Item.ItemType.Herb, 3, 14)); p.materialBag.add(new Item(Item.ItemType.Herb, 5, 6));
        p.materialBag.add(new Item(Item.ItemType.Metal, 4, 9)); p.materialBag.add(new Item(Item.ItemType.Blood, 1500));
        Item pill = new Item(Item.ItemType.Pill, Item.PillType.Cultivation, 4, 5); pill.brand = "Hopkins Pill House"; p.pillBag.add(pill);
        p.pillBag.add(new Item(Item.ItemType.Pill, Item.PillType.Potential, 3, 2));
        p.arrayBag.add(new Item(p, 3, Item.ItemType.Array));
        if (p.goods == null) p.goods = new ArrayList<>();
        p.goods.add(new Item(Item.ItemType.Food, 3, 4, "Jade Noodles")); p.goods.add(new Item(Item.ItemType.Wine, 2, 6, "Plum Wine"));
    }

    static Character person(String first, String last, int lvl, int age) {
        Character c = new Character(p.location);
        c.id = Map.characters.size(); GameDriver.UID = Math.max(GameDriver.UID, c.id + 1);
        if (c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) c.trait = Character.Traits.Friendly;
        c.name = first; c.lastName = last; c.lvl = lvl; c.age = age;
        Map.characters.add(c); Map.population++;
        return c;
    }

    /** Family, friends, a past life, chronicle lines and a story for the character. */
    static void populate() {
        Character mother = person("Wen", "Lian", 45, 88), father = person("Hao", "Lian", 38, 91), spouse = person("Yue", "Shan", 36, 30), kid = person("Mei", "Lian", 5, 6), kid2 = person("Jun", "Lian", 12, 14), rival = person("Dao", "Feng", 41, 55), friend = person("Lan", "Ming", 33, 40);
        p.mother = mother; p.father = father; mother.children.add(p); father.children.add(p);
        p.spouse = spouse; spouse.spouse = p;
        p.children.add(kid); p.children.add(kid2); kid.mother = p; kid2.mother = p;
        father.dead = true;
        p.changeRelationship(rival, -3); p.changeRelationship(friend, 3); p.changeRelationship(spouse, 3); p.changeRelationship(mother, 2);
        p.lives = 2; p.pastLives = new ArrayList<>();
        p.pastLives.add("Life 1: reached Foundation at age 61 in year 80, potential 31, 12 kills");
        p.pastLives.add("Life 2: reached Soul Wandering at age 140 in year 190, potential 44, 203 kills");
        p.potential = 58;
        int w = GameDriver.time;
        Chronicle.add("breakthrough", p.name + " reached Foundation Stage.", p.id);
        Chronicle.add("fight", p.name + " defeated " + rival.name + " in a duel.", p.id, rival.id);
        Chronicle.add("city", p.name + " founded the city of Azure Gate.", p.id);
        Chronicle.add("death", father.name + " died at 91.", father.id, p.id);
        Chronicle.add("legend", p.name + " was named a legend of the east.", p.id);
        RunStats.run.reincarnations = 2; RunStats.run.peakWealth = 3.4e7; RunStats.run.deepestDungeon = 6; RunStats.run.tamed = 2; RunStats.run.children = 2;
        Starts.State st = new Starts.State(); st.story = "heir"; st.difficulty = 0; st.hooked = true; p.story = st;
        Chapters.begin(p, st);
        st.chapter = 2; st.open = true; st.cur = Chapters.DEFS.get("heir").chapters.get(2).id;
        try { java.io.File f = RunStats.hallFile(); try (PrintWriter pw = new PrintWriter(new FileWriter(f, true))) { pw.println("year 311 | Wei Ran (Heir, Story) | 311 years, 3 lives, 2 reincarnations | peak Lv.101 Major 9 | 340 kills | wealth 12M | Crossed the Door"); pw.println("ENDING | year 402 | Lan Shu (Orphan) | The Lamp Kept | SCORE 880 | 402 years, 1 reincarnations | peak Lv.88 Major 8 | 90 kills | potential 61"); } } catch (IOException ignored) { }
    }

    static void later() throws Exception {
        populate();
        if (wanted("family")) {
            window("family_tree", "Family", () -> GuiExtras.familyWindow(mv));
            for (String t : new String[]{"Family list", "Relationship list", "Past lives"}) {
                onEdt(() -> GuiExtras.familyWindow(mv)); Thread.sleep(1200);
                tab("Family", t);
                shot("family_" + t.replace(' ', '_'), content(win("Family")));
                Window fw = win("Family"); onEdt(fw::dispose);
            }
        }
        if (wanted("bio")) window("bio", "Biography", () -> GuiMore.biographyWindow(mv, p));
        if (wanted("run")) {
            window("run", "Run statistics", () -> GuiMore.runWindow(mv));
            onEdt(() -> GuiMore.runWindow(mv)); Thread.sleep(1200); tab("Run statistics", "Hall"); shot("run_hall", content(win("Run statistics")));
            Window rw = win("Run statistics"); onEdt(rw::dispose);
        }
        if (wanted("story")) window("story", "Story", () -> GuiExtras.storyWindow(mv));
        if (wanted("chronicle")) {
            Map.addLegacy("Sovereign of the Nine Heavens", 103); Map.addLegacy("The Hollow Blade", 98); Map.addLegacy("Mother of Lanterns", 101);
            Chronicle.add("legend", "The first cultivator crossed Ascension: Su Wen.", p.id);
            onEdt(() -> GuiMore.chronicleWindow(mv)); Thread.sleep(1200); tab("Chronicle", "Hall"); shot("chronicle_legends", content(win("Chronicle")));
            Window cw = win("Chronicle"); onEdt(cw::dispose);
        }
        if (wanted("digest")) window("digest", "What happened lately", () -> GuiMore.digestWindow(mv, 52));
        Area sectArea = null;
        for (Area x : Map.createdAreas) if (x != null && x.sect != null && !x.sect.isVirtual() && x.sect.master != null) { sectArea = x; break; }
        if (sectArea != null) { sectArea.sect.members.add(p); p.sect = sectArea.sect; sectArea.sect.memberRanks.put(p.id, Organization.SectRank.INNER_DISCIPLE); p.sectPoints = 240; }
        final Area sa = sectArea;
        if (wanted("manage")) {
            onEdt(() -> GuiWindows.managePanel(mv)); Thread.sleep(1200);
            for (String t : new String[]{"City", "Sect"}) { tab("Manage", t); Thread.sleep(500); shot("manage_" + t, content(win("Manage"))); }
            Window mw = win("Manage"); onEdt(mw::dispose);
        }
        if (wanted("squad")) window("squad", "Squad", () -> GuiSheets.squadWindow(mv));
        if (wanted("place")) {
            window("place_city", "Details", () -> GuiMore.placeWindow(mv, p.location));
            if (sa != null) window("place_sect", "Details", () -> GuiMore.placeWindow(mv, sa));
        }
        if (wanted("rankings")) {
            Rankings.yearly(Map.characters);
            onEdt(() -> GuiWindows.rankingsPanel(mv)); Thread.sleep(1500);
            shot("rankings_strongest", content(win("Rankings")));
            tab("Rankings", "Dashboard"); shot("rankings_dashboard", content(win("Rankings")));
            Window rkw = win("Rankings"); onEdt(rkw::dispose);
        }
        if (wanted("market")) {
            Area a = p.location;
            if (a.auctionBag == null) a.auctionBag = new ArrayList<>();
            Item t = new Item(p, Item.WeaponType.Sword, 8); t.heavenSent = true; t.name = "Heavenfall Sword"; a.auctionBag.add(t);
            a.auctionBag.add(new Item(Item.ItemType.Pill, Item.PillType.Potential, 5, 2));
            a.auctionBag.add(new Item(Item.ItemType.Wine, 4, 6, "Vintage Plum Wine"));
            p.ss = 4000;
            window("market_auction", "Market", () -> GuiWindows.marketPanel(mv));
        }
    }

    static void run() throws Exception {
        PcProbe.in.feed("y\ns\nn\n");
        long end = System.currentTimeMillis() + 240000;
        while (System.currentTimeMillis() < end && !(GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbe.pc() != null)) Thread.sleep(200);
        p = PcProbe.pc();
        mv = GameDriver.mapViewer;
        p.lvl = 40; p.majorLvl = 4; p.maxAge = 100000; p.caution = 0;
        DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        GameDriver.frozenNpcs = true;
        onEdt(() -> { mv.setSize(1500, 950); mv.setLocation(10, 10); mv.validate(); });
        Thread.sleep(1500);
        PcProbeGui.click("Wait"); PcProbeGui.settle(); Thread.sleep(800);

        PcProbeGui.SHEETS.clear(); // the watcher closes sheet windows it knows; these shots want them open
        dress(p);
        if (wanted("backpack")) {
            window("backpack_gear", "Backpack", () -> GuiMore.backpackWindow(mv, "Gear"));
            onEdt(() -> GuiMore.backpackWindow(mv, "Materials")); Thread.sleep(1400);
            shot("backpack_stores", content(win("Backpack")));
            onEdt(() -> GuiMore.backpackWindow(mv, "Food")); Thread.sleep(900);
            shot("backpack_goods", content(win("Backpack")));
            Window bpw = win("Backpack"); onEdt(bpw::dispose);
        }
        if (wanted("editself")) {
            PcProbeGui.watch = false; // the watcher would answer a modal dialog by itself
            SwingUtilities.invokeLater(() -> GuiEditSelf.show(mv, p, false));
            Thread.sleep(1500);
            Window ew = win("Edit Character");
            if (ew == null) real.println("NO WINDOW editself"); else { shot("editself", content(ew)); onEdt(ew::dispose); }
            PcProbeGui.watch = true; new Thread(PcProbeGui::watcher, "dialog-watcher2").start();
            Thread.sleep(300);
        }
        if (wanted("scripts")) {
            new File("scripts").mkdirs();
            try (PrintWriter pw = new PrintWriter("scripts/Herb run.txt")) { pw.println("goto 15 15"); pw.println("repeat 3"); pw.println("forage 10"); pw.println("sell"); pw.println("end"); }
            try (PrintWriter pw = new PrintWriter("scripts/Mine and smith.txt")) { pw.println("mine until 500"); pw.println("craft 5"); }
            cmd("!script clear");
            cmd("script add goto 15 15"); cmd("script add repeat 3"); cmd("script add forage 10"); cmd("script add sell"); cmd("script add end");
            Thread.sleep(500);
            window("scripts", "Script Manager", () -> mv.openScriptEditor());
        }
        later();
        real.println("DONE");
    }
}
