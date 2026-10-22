package Game;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ToolTipManager;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Window;
import java.util.HashMap;
import java.util.Map;

/** Small GUI helpers shared by every window: tooltip text for the buttons, live text-size scaling, and applying the tooltip preference. */
public class Ui {
    private Ui() { }

    private static final Map<String, String> TIPS = new HashMap<>();
    static {
        tip("↑ North", "Move one tile north (W or ↑). North is up on the map.");
        tip("↓ South", "Move one tile south (S or ↓).");
        tip("← W", "Move one tile west (A or ←).");
        tip("E →", "Move one tile east (D or →).");
        tip("Wait", "Pass one turn (Space). The world moves one week.");
        tip("Stats", "Your character sheet (I). Costs no time.");
        tip("Locations", "Places you know, with distances (L).");
        tip("Relations", "Everyone you have a relationship with (R).");
        tip("Quests", "The numbered quest list.");
        tip("Edit Self", "Change your name, trait, potential, alignment or gender.");
        tip("Cheats", "Developer menu: give wealth, set level, heal... (type the letters into the command box).");
        tip("Auto", "Let your character live like an NPC until you press Stop. Attacks and alerts interrupt it.");
        tip("Manual", "Take control back: the game waits for your command every turn.");
        tip("Stop", "Stop AUTO or a running script and return control to you.");
        tip("Alerts", "Quests waiting for you to do them yourself.");
        tip("Do Quest", "Run a quest you are standing at the target of (asks for its number).");
        tip("Duties", "Show which chores your character does by itself.");
        tip("All on", "Switch every duty on: your character does all its chores itself.");
        tip("All off", "Switch every duty off: you do everything yourself.");
        tip("Here", "Describe the tile you stand on.");
        tip("People", "List the cultivators in your tile.");
        tip("Help", "The full command list.");
        tip("Scripts", "The Script Manager: write, save and load a hybrid-mode script (T).");
        tip("Browse", "Select, filter and sort cultivators, shop stock, your backpack and places (B).");
        tip("Quests+", "The quest panel (Q): pin priorities, delete, mute, Do Quest.");
        tip("Duties+", "Duties and decision policies (U).");
        tip("Manage", "Your shops' prices, this city, your sect (M).");
        tip("Rankings", "Strongest / richest / most kills and world charts (G).");
        tip("Market", "This city's auction and library (K).");
        tip("Keys", "Keyboard shortcuts.");
        tip("Family", "Your family tree and relationship graph (F).");
        tip("Settings", "Text size, autosave, notifications, confirmations...");
        tip("Saves", "Save, load and delete named saves.");
        tip("Save", "Quick save (Ctrl+S).");
        tip("Refresh", "Reload the list now.");
        tip("Talk", "Chat with the selected cultivator (same tile only).");
        tip("Spar", "Friendly fight: nobody dies.");
        tip("Attack", "A real fight. You are warned first if the odds are bad.");
        tip("Invite to party", "Ask them to join your party.");
        tip("Seek: befriend", "Pin a quest to walk to them and befriend them, wherever they are.");
        tip("Seek: fight", "Pin a quest to walk to them and fight them.");
        tip("Seek: spar", "Pin a quest to walk to them and spar.");
        tip("Preview fight", "Win chance, lethality and escape odds against them.");
        tip("Copy ID", "Copy the character id to the clipboard.");
        tip("Follow", "Show them in the NPC tracker and on the map.");
        tip("Control", "Become this cultivator. Your current one carries on as an NPC.");
        tip("Buy selected", "Buy the selected item at the shown price.");
        tip("Sell selected", "Sell the selected backpack item in this city.");
        tip("Go there", "Walk to the selected place.");
        tip("Add waypoint quest", "Add a pinned quest that leads there.");
        tip("Pin priority", "Fix this quest's priority so it never ages.");
        tip("Unpin", "Let this quest age normally again.");
        tip("Delete", "Remove this quest (it may come back unless you mute it).");
        tip("Mute this kind", "Remove all quests of this kind and stop new ones.");
        tip("Unmute", "Allow a muted kind of quest again.");
        tip("Apply rates", "Set the selected shop's rates from the sliders.");
        tip("Learn selected", "Buy the selected technique from the library.");
        tip("Export", "Save this as a file in the exports folder.");
    }

    private static void tip(String text, String tip) { TIPS.put(text, tip); }

    /** Tooltip for a button label (the emoji prefix is ignored), or null. */
    static String tipFor(String label) {
        if (label == null) return null;
        String t = label.trim();
        String hit = TIPS.get(t);
        if (hit != null) return hit;
        for (Map.Entry<String, String> e : TIPS.entrySet()) if (t.contains(e.getKey()) && e.getKey().length() >= 4) return e.getValue();
        return null;
    }

    /** Puts the standard tooltip on a button if it has none. */
    static void tooltip(AbstractButton b) {
        if (b.getToolTipText() == null) { String t = tipFor(b.getText()); if (t != null) b.setToolTipText(t); }
    }

    /** Turns tooltips on or off everywhere according to Settings.tooltips. */
    static void applyTooltipPreference() {
        ToolTipManager.sharedInstance().setEnabled(Settings.tooltips);
        ToolTipManager.sharedInstance().setInitialDelay(350);
        ToolTipManager.sharedInstance().setDismissDelay(9000);
    }

    // ------------------------------------------------------------------ text size
    private static double applied = 1.0;

    /** Multiplies every font in a window's component tree by `ratio` (windows build with base sizes, so a new window is scaled by the setting). */
    static void scale(Container root, double ratio) {
        if (Math.abs(ratio - 1.0) < 0.001) return;
        for (Component c : root.getComponents()) {
            Font f = c.getFont();
            if (f != null) c.setFont(f.deriveFont(Math.max(6f, (float) (f.getSize2D() * ratio))));
            if (c instanceof JTable) { JTable t = (JTable) c; t.setRowHeight(Math.max(12, (int) Math.round(t.getRowHeight() * ratio))); }
            if (c instanceof JComponent && ((JComponent) c).getBorder() instanceof javax.swing.border.TitledBorder) {
                javax.swing.border.TitledBorder tb = (javax.swing.border.TitledBorder) ((JComponent) c).getBorder();
                if (tb.getTitleFont() != null) tb.setTitleFont(tb.getTitleFont().deriveFont((float) (tb.getTitleFont().getSize2D() * ratio)));
            }
            if (c instanceof Container) scale((Container) c, ratio);
        }
        root.revalidate();
    }

    /** A freshly built window gets the user's text size. */
    static void prepare(Window w) { scale(w, Settings.fontScale); }

    /** The text-size setting changed: rescale everything that is open by the change. */
    static void fontScaleChanged() {
        double ratio = Settings.fontScale / applied;
        applied = Settings.fontScale;
        for (Window w : Window.getWindows()) { scale(w, ratio); w.revalidate(); w.repaint(); }
    }

    /** MapViewer builds at base sizes; call once after construction so `applied` matches what the main window is showing. */
    static void mainWindowScaled() { applied = Settings.fontScale; }

    // ------------------------------------------------------------------ scrolling
    /** Every scroll pane that comes on screen gets a mouse-wheel step of its own (`Settings.scrollSpeed`, 1-10): the default steps of a text pane or a table were a few pixels, which made the wheel feel dead. */
    static void installScrollSpeed() {
        java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(ev -> {
            if (!(ev instanceof java.awt.event.HierarchyEvent)) return;
            java.awt.event.HierarchyEvent he = (java.awt.event.HierarchyEvent) ev;
            if ((he.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && he.getComponent() instanceof javax.swing.JScrollPane) speedUp((javax.swing.JScrollPane) he.getComponent());
        }, java.awt.AWTEvent.HIERARCHY_EVENT_MASK);
        applyScrollSpeed();
    }

    /** The pixels one step of the wheel's unit scrolls: 8 for each point of the setting (3 units go by on every notch). */
    static int scrollUnit() { return 8 * Math.max(1, Math.min(10, Settings.scrollSpeed)); }

    static void speedUp(javax.swing.JScrollPane sp) {
        int u = scrollUnit();
        javax.swing.JScrollBar v = sp.getVerticalScrollBar(), h = sp.getHorizontalScrollBar();
        if (v != null) { v.setUnitIncrement(u); v.setBlockIncrement(Math.max(u * 4, 80)); }
        if (h != null) { h.setUnitIncrement(u); h.setBlockIncrement(Math.max(u * 4, 80)); }
    }

    /** Applies the speed to every scroll pane that is open now (the Settings slider calls it). */
    static void applyScrollSpeed() {
        for (Window w : Window.getWindows()) walk(w);
    }

    private static void walk(Component c) {
        if (c instanceof javax.swing.JScrollPane) speedUp((javax.swing.JScrollPane) c);
        if (c instanceof Container) for (Component k : ((Container) c).getComponents()) walk(k);
    }
}
