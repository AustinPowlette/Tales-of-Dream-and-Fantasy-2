package Game;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;
import java.util.*;
import java.util.List;

/** 2026-10-13 (GUI, needs a display): the one Items component (`GuiItemList`) in the Browser and in the Market: Mine (equip, sell, ...), For sale here (filters, buy) and Sell here (prices of the shops against
 *  the city pool, with a toggle that keeps only the items a shop pays more for). */
public class ItemListProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    static void check(String n, boolean ok) { PcProbeGui.check(n, ok); }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "false");
        PcProbeGui.real = System.out;
        PcProbe.real = PcProbeGui.real;
        System.setIn(PcProbe.in);
        GameDriver.frozenNpcs = true;
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(PcProbeGui.real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        Thread w = new Thread(PcProbeGui::watcher, "dialog-watcher"); w.setDaemon(true); w.start();
        PcProbeGui.SHEETS.clear();
        PcProbeGui.IGNORE.addAll(Arrays.asList("Browser — select, filter, sort", "Market — auction & library"));
        try { run(); } catch (Throwable e) { PcProbeGui.real.println("[G] PROBE EXCEPTION " + e); e.printStackTrace(PcProbeGui.real); PcProbeGui.fails++; }
        PcProbeGui.real.println("[G] ===== " + PcProbeGui.passes + " passed, " + PcProbeGui.fails + " failed; gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }

    static boolean until(java.util.function.BooleanSupplier c, long ms) throws Exception { long end = System.currentTimeMillis() + ms; while (System.currentTimeMillis() < end) { if (c.getAsBoolean()) return true; Thread.sleep(50); } return c.getAsBoolean(); }
    static JDialog find(String title) { for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing() && ((JDialog) w).getTitle() != null && ((JDialog) w).getTitle().startsWith(title)) return (JDialog) w; return null; }
    static JTabbedPane tabsOf(Container c) { final JTabbedPane[] r = {null}; try { SwingUtilities.invokeAndWait(() -> { List<JTabbedPane> ts = new ArrayList<>(); PcProbeGui.collect(c, JTabbedPane.class, ts); for (JTabbedPane t : ts) { boolean hasMine = false; for (int i = 0; i < t.getTabCount(); i++) if (t.getTitleAt(i).equals("Mine")) hasMine = true; if (hasMine) r[0] = t; } }); } catch (Exception e) { } return r[0]; }
    static void pick(JTabbedPane tp, String title) throws Exception { SwingUtilities.invokeAndWait(() -> { for (int i = 0; i < tp.getTabCount(); i++) if (tp.getTitleAt(i).equals(title)) tp.setSelectedIndex(i); }); Thread.sleep(1500); }
    static JPanel shown(JTabbedPane tp) { final JPanel[] r = {null}; try { SwingUtilities.invokeAndWait(() -> r[0] = (JPanel) tp.getSelectedComponent()); } catch (Exception e) { } return r[0]; }
    static List<GuiItems.Entry> entries(Container c) {
        final List<GuiItems.Entry> out = new ArrayList<>();
        try { SwingUtilities.invokeAndWait(() -> { List<JList> ls = new ArrayList<>(); PcProbeGui.collect(c, JList.class, ls); if (!ls.isEmpty()) { javax.swing.ListModel m = ls.get(0).getModel(); for (int i = 0; i < m.getSize(); i++) out.add((GuiItems.Entry) m.getElementAt(i)); } }); } catch (Exception e) { }
        return out;
    }
    static void select(Container c, int row) throws Exception { SwingUtilities.invokeAndWait(() -> { List<JList> ls = new ArrayList<>(); PcProbeGui.collect(c, JList.class, ls); if (!ls.isEmpty()) ls.get(0).setSelectedIndex(row); }); Thread.sleep(300); }
    static void press(Container c, String label) throws Exception { SwingUtilities.invokeAndWait(() -> { JButton b = PcProbeGui.findButton(c, label); if (b != null) b.doClick(); }); }
    static int indexOf(List<GuiItems.Entry> es, Item it) { for (int i = 0; i < es.size(); i++) if (es.get(i).item == it) return i; return -1; }
    static String buttons(Container c) { final String[] r = {""}; try { SwingUtilities.invokeAndWait(() -> { List<JButton> bs = new ArrayList<>(); PcProbeGui.collect(c, JButton.class, bs); StringBuilder sb = new StringBuilder(); for (JButton b : bs) sb.append("[").append(b.getText()).append("]"); r[0] = sb.toString(); }); } catch (Exception e) { } return r[0]; }

    static void run() throws Exception {
        Settings.lore = false;
        PcProbe.in.feed("y\ns\nn\n");
        long end = System.currentTimeMillis() + Long.getLong("gui.waitMs", 240000L);
        while (System.currentTimeMillis() < end && !(GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null)) Thread.sleep(200);
        check("GUI opened and the player is prompting", GameDriver.mapViewer != null && GameDriver.waitingForInput && PcProbeGui.pc() != null);
        final Character p = PcProbeGui.pc();
        if (p == null) return;
        final MapViewer mv = GameDriver.mapViewer;
        p.lvl = 100; p.majorLvl = 10; p.maxAge = 100000;
        final Area city = p.location;
        check("the start tile is a city", city.city);
        // a stocked shop with an owner
        Character owner = null;
        for (int i = 700; i < Map.characters.size() && owner == null; i++) { Character c = Map.characters.get(i); if (c != null && !c.dead && c.born && c.player == 0 && c.age >= 12 && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster) owner = c; }
        final Shop sh = new Shop(owner, city);
        final Item wA = new Item(owner, Item.WeaponType.Sword, 500), wB = new Item(owner, Item.WeaponType.Saber, 450), cC = new Item(owner, 500, Item.ItemType.Clothing);
        sh.inventory.add(wA); sh.inventory.add(wB); sh.inventory.add(cC);
        sh.sellRate = 5.0; sh.buyRate = 0.55; city.shops.add(sh);
        p.ss = 0; p.sc = 5_000; p.convertCurrency();
        final Item myClothes = new Item(p, 10, Item.ItemType.Clothing), myFan = new Item(p, Item.WeaponType.Fan, 2);
        p.backpack.add(myClothes); p.backpack.add(myFan);

        SwingUtilities.invokeLater(() -> mv.openBrowser("Items"));
        check("I1 the Browser opens on the Items tab", until(() -> find("Browser") != null, 8000));
        JDialog br = find("Browser");
        if (br == null) return;
        Thread.sleep(1800);
        JTabbedPane sub = tabsOf(br.getContentPane());
        check("I2 Items has three sub-tabs: Mine, For sale here, Sell here", sub != null && sub.getTabCount() == 3);
        if (sub == null) return;

        // ---- Mine
        pick(sub, "Mine");
        JPanel mine = shown(sub);
        List<GuiItems.Entry> es = entries(mine);
        check("I3 Mine lists the backpack on item cards with the change in fighting level (" + es.size() + " rows)", indexOf(es, myClothes) >= 0 && indexOf(es, myFan) >= 0 && (es.get(indexOf(es, myClothes)).badge.contains("▲") || es.get(indexOf(es, myClothes)).badge.contains("▼") || es.get(indexOf(es, myClothes)).badge.contains("=")));
        String mb = buttons(mine);
        check("I3b Mine has Equip selected, Equip best, Sell selected, Gift selected…, Consume pills", mb.contains("[Equip selected]") && mb.contains("[Equip best]") && mb.contains("[Sell selected]") && mb.contains("[Gift selected…]") && mb.contains("[Consume pills]"));
        int t0 = GameDriver.time;
        select(mine, indexOf(es, myClothes));
        press(mine, "Equip selected");
        check("I4 'Equip selected' wears the selected clothes at no cost in time", until(() -> p.equippedClothing == myClothes, 6000) && GameDriver.time == t0);

        // ---- For sale here
        pick(sub, "For sale here");
        JPanel sale = shown(sub);
        es = entries(sale);
        check("I5 For sale here lists the stocked shop (" + es.size() + " rows)", indexOf(es, wA) >= 0 && indexOf(es, wB) >= 0 && indexOf(es, cC) >= 0);
        check("I5b ...with the price on each card and who sells it", es.get(indexOf(es, wA)).badge.endsWith("ss") && !es.get(indexOf(es, wA)).note.isEmpty());
        final JPanel saleF = sale;
        SwingUtilities.invokeAndWait(() -> { List<JComboBox> cs = new ArrayList<>(); PcProbeGui.collect(saleF, JComboBox.class, cs); for (JComboBox cb : cs) for (int i = 0; i < cb.getItemCount(); i++) if ("weapon".equals(cb.getItemAt(i))) cb.setSelectedIndex(i); });
        Thread.sleep(1200);
        es = entries(sale);
        boolean onlyWeapons = !es.isEmpty(); for (GuiItems.Entry e : es) if (e.item == null || e.item.itemType != Item.ItemType.Weapon) onlyWeapons = false;
        check("I6 the type filter 'weapon' leaves only weapons (" + es.size() + ")", onlyWeapons && es.size() >= 2);
        select(sale, indexOf(es, wA));
        int mk = PcProbe.mark();
        press(sale, "Buy selected");
        Thread.sleep(800);
        check("I7 'Buy selected' buys the sword", until(() -> p.backpack.contains(wA) || p.equippedWeapon == wA, 6000) && PcProbe.since(mk).contains("You bought"));

        // ---- Sell here
        final Item junk = new Item(p, Item.WeaponType.Fan, 2); p.backpack.add(junk); // something cheap to sell (the purchase may have shuffled the first bag)
        pick(sub, "Sell here");
        JPanel sell = shown(sub);
        es = entries(sell);
        check("I8 Sell here lists what you carry with the best price (" + es.size() + " rows)", indexOf(es, junk) >= 0 && es.get(indexOf(es, junk)).badge.endsWith("ss") && es.get(indexOf(es, junk)).note.contains("city pool pays"));
        final JPanel sellF = sell;
        final JToggleButton[] tog = {null};
        SwingUtilities.invokeAndWait(() -> { List<JToggleButton> l = new ArrayList<>(); PcProbeGui.collect(sellF, JToggleButton.class, l); for (JToggleButton b : l) if ("Only good prices".equals(b.getText())) tog[0] = b; });
        check("I9 there is an 'Only good prices' toggle", tog[0] != null);
        SwingUtilities.invokeAndWait(() -> tog[0].doClick());
        Thread.sleep(1300);
        es = entries(sell);
        check("I9b with the toggle on, the items a shop pays more for stay (" + es.size() + " rows)", indexOf(es, junk) >= 0);
        sh.buyRate = 0.4; // this shop no longer beats the city pool
        Thread.sleep(300);
        SwingUtilities.invokeAndWait(() -> tog[0].doClick()); SwingUtilities.invokeAndWait(() -> tog[0].doClick()); // off and on again: a refresh with the toggle on
        Thread.sleep(1300);
        es = entries(sell);
        check("I9c ...and when no shop pays more than the pool the list is empty with a note (" + es.size() + " rows)", indexOf(es, junk) < 0);
        SwingUtilities.invokeAndWait(() -> tog[0].doClick());
        Thread.sleep(1300);
        es = entries(sell);
        check("I9d toggle off: everything is back, priced by the pool", indexOf(es, junk) >= 0 && es.get(indexOf(es, junk)).note.contains("city pool pays"));
        int bag0 = p.backpack.size();
        select(sell, indexOf(es, junk));
        press(sell, "Sell selected");
        check("I10 'Sell selected' sells it", until(() -> !p.backpack.contains(junk), 6000) && p.backpack.size() == bag0 - 1);

        // ---- the Market has the shops too
        SwingUtilities.invokeAndWait(br::dispose);
        SwingUtilities.invokeLater(() -> GuiWindows.marketPanel(mv));
        check("I11 the Market window opens", until(() -> find("Market") != null, 8000));
        JDialog mk2 = find("Market");
        if (mk2 != null) {
            final boolean[] hasShops = {false};
            SwingUtilities.invokeAndWait(() -> { List<JTabbedPane> ts = new ArrayList<>(); PcProbeGui.collect(mk2.getContentPane(), JTabbedPane.class, ts); for (JTabbedPane t : ts) for (int i = 0; i < t.getTabCount(); i++) if (t.getTitleAt(i).equals("Shops")) hasShops[0] = true; });
            check("I12 the Market has a Shops tab", hasShops[0]);
            SwingUtilities.invokeAndWait(mk2::dispose);
        }
        city.shops.remove(sh);
        PcProbeGui.real.println("[G] DONE");
    }
}
