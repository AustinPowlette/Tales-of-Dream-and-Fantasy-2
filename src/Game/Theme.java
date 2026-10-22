package Game;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/** The look of the game: an "ink & jade" xianxia palette on top of FlatLaf. FlatLaf is found at run time (lib/flatlaf-*.jar is added to the class
 *  path here, through reflection) so the game still compiles with a plain `javac -cp src`, and falls back to a themed Nimbus when the jar is
 *  missing. Every window reads its colours and fonts from here. */
public final class Theme {
    private Theme() { }

    // ------------------------------------------------------------------ palette
    static Color BG = new Color(12, 16, 20), PANEL = new Color(20, 26, 30), PANEL2 = new Color(27, 35, 38), FIELD = new Color(10, 14, 17);
    static Color LINE = new Color(64, 60, 44), TEXT = new Color(228, 220, 198), DIM = new Color(146, 146, 128);
    static Color ACCENT = new Color(104, 184, 160), GOLD = new Color(208, 172, 92), DANGER = new Color(200, 76, 62), OK = new Color(118, 200, 150);
    static Color SELECT = new Color(38, 78, 72), SEAL = new Color(178, 52, 44), INK = new Color(46, 66, 64);

    /** Settings.highContrast: black ground, white text, saturated accents (read once, before the first window is built). */
    private static void highContrast() {
        BG = new Color(0, 0, 0); PANEL = new Color(8, 8, 8); PANEL2 = new Color(22, 22, 22); FIELD = new Color(0, 0, 0);
        LINE = new Color(190, 190, 190); TEXT = new Color(255, 255, 255); DIM = new Color(215, 215, 215);
        ACCENT = new Color(0, 255, 225); GOLD = new Color(255, 230, 0); DANGER = new Color(255, 96, 96); OK = new Color(110, 255, 120);
        SELECT = new Color(0, 74, 130); SEAL = new Color(255, 70, 70); INK = new Color(40, 40, 40);
    }

    static final String UI_FAMILY = "Segoe UI", SERIF = "Georgia", CJK = "Microsoft YaHei", SYMBOL = "Segoe UI Symbol";

    static boolean flat = false;   // true when FlatLaf is active

    static String hex(Color c) { return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue()); }

    // ------------------------------------------------------------------ fonts
    static Font plain(int n) { return new Font(UI_FAMILY, Font.PLAIN, Math.round(n * 0.93f)); }
    static Font bold(int n) { return new Font(UI_FAMILY, Font.BOLD, Math.round(n * 0.93f)); }
    static Font mono(int n) { return new Font("Consolas", Font.PLAIN, n); }
    static Font serif(int n) { return new Font(SERIF, Font.BOLD, n); }
    static Font symbol(int n) { return new Font(SYMBOL, Font.PLAIN, n); }
    static boolean hasSymbol(String s) { if (s == null) return false; for (int i = 0; i < s.length(); i++) { char ch = s.charAt(i); if ((ch >= 0x25A0 && ch <= 0x2BFF) || ch == 0x23F8 || java.lang.Character.isSurrogate(ch)) return true; } return false; }

    /** Text containing symbol glyphs (⚑ 🔔 💾 ▶) needs a font that has them; Segoe UI does not. */
    static void fixSymbols(JComponent c, String text) { if (hasSymbol(text)) c.setFont(symbol(c.getFont().getSize())); }

    // ------------------------------------------------------------------ installation
    /** Sets the look and feel. Safe to call more than once; must run before the first window is built. */
    static synchronized void install() {
        if (flat) return;
        Settings.ensureLoaded();
        if (Settings.highContrast) highContrast();
        if (!installFlat()) installNimbus();
        UIManager.put("defaultFont", plain(15));
        UIManager.put("ToolTip.background", PANEL2); UIManager.put("ToolTip.foreground", TEXT);
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(LINE));
        UIManager.put("Table.background", FIELD); UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.selectionBackground", SELECT); UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("Table.alternateRowColor", new Color(15, 21, 24)); UIManager.put("Table.showGrid", false); UIManager.put("Table.intercellSpacing", new Dimension(0, 0));
        UIManager.put("Table.rowHeight", 26);
        UIManager.put("TableHeader.background", PANEL2); UIManager.put("TableHeader.foreground", GOLD);
        UIManager.put("TextArea.background", FIELD); UIManager.put("TextField.background", FIELD); UIManager.put("List.background", FIELD);
        UIManager.put("ScrollPane.background", BG); UIManager.put("Viewport.background", BG);
        UIManager.put("SplitPane.background", BG);
        UIManager.put("SplitPaneDivider.background", BG);
        UIManager.put("OptionPane.background", PANEL); UIManager.put("Panel.background", PANEL);
        UIManager.put("ProgressBar.foreground", ACCENT); UIManager.put("ProgressBar.background", FIELD);
        if (flat) {
            UIManager.put("Button.arc", 10); UIManager.put("Component.arc", 10); UIManager.put("TextComponent.arc", 8); UIManager.put("CheckBox.arc", 5); UIManager.put("ProgressBar.arc", 999);
            UIManager.put("ScrollBar.thumbArc", 999); UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2)); UIManager.put("ScrollBar.showButtons", false); UIManager.put("ScrollBar.width", 12);
            UIManager.put("ScrollBar.thumb", new Color(70, 84, 82)); UIManager.put("ScrollBar.hoverThumbColor", new Color(96, 120, 114)); UIManager.put("ScrollBar.track", BG);
            UIManager.put("Button.background", new Color(31, 40, 42)); UIManager.put("Button.hoverBackground", new Color(40, 54, 54)); UIManager.put("Button.pressedBackground", new Color(30, 62, 58));
            UIManager.put("Button.borderColor", new Color(84, 74, 46)); UIManager.put("Button.hoverBorderColor", ACCENT); UIManager.put("Button.foreground", TEXT);
            UIManager.put("Button.toolbar.hoverBackground", new Color(40, 54, 54)); UIManager.put("Button.toolbar.selectedBackground", SELECT);
            UIManager.put("ToggleButton.selectedBackground", SELECT); UIManager.put("ToggleButton.selectedForeground", Color.WHITE);
            UIManager.put("Component.focusColor", ACCENT); UIManager.put("Component.borderColor", LINE); UIManager.put("Component.focusedBorderColor", ACCENT);
            UIManager.put("TabbedPane.background", PANEL); UIManager.put("TabbedPane.underlineColor", GOLD); UIManager.put("TabbedPane.selectedForeground", GOLD);
            UIManager.put("TabbedPane.hoverColor", PANEL2); UIManager.put("TabbedPane.contentAreaColor", LINE); UIManager.put("TabbedPane.tabSelectionHeight", 3);
            UIManager.put("TabbedPane.tabInsets", new Insets(6, 14, 6, 14)); UIManager.put("TabbedPane.showTabSeparators", false); UIManager.put("TabbedPane.tabSeparatorsFullHeight", false);
            UIManager.put("SplitPaneDivider.gripColor", LINE); UIManager.put("SplitPane.dividerSize", 7);
            UIManager.put("ComboBox.background", FIELD); UIManager.put("Spinner.background", FIELD); UIManager.put("TextField.background", FIELD);
            UIManager.put("CheckBox.icon.selectedBackground", ACCENT.darker()); UIManager.put("CheckBox.icon.checkmarkColor", Color.WHITE);
            UIManager.put("ProgressBar.selectionForeground", TEXT); UIManager.put("ProgressBar.selectionBackground", TEXT);
            UIManager.put("Slider.trackColor", LINE); UIManager.put("Slider.thumbColor", ACCENT); UIManager.put("Slider.foreground", ACCENT);
            UIManager.put("OptionPane.buttonMinimumWidth", 90);
        }
        applyTooltipDefaults();
    }

    private static void applyTooltipDefaults() { ToolTipManager.sharedInstance().setInitialDelay(350); ToolTipManager.sharedInstance().setDismissDelay(9000); }

    private static boolean installFlat() {
        try {
            ClassLoader cl = Theme.class.getClassLoader();
            File[] jars = null;
            java.util.List<File> dirs = new java.util.ArrayList<>();
            dirs.add(new File("lib"));
            try { File here = new File(Theme.class.getProtectionDomain().getCodeSource().getLocation().toURI()); dirs.add(new File(here.getParentFile(), "lib")); dirs.add(new File(here.getParentFile().getParentFile(), "lib")); } catch (Exception ignored) { }
            for (File dir : dirs) { File[] f = dir.listFiles((d, n) -> n.startsWith("flatlaf") && n.endsWith(".jar")); if (f != null && f.length > 0) { jars = f; break; } }
            try { Class.forName("com.formdev.flatlaf.FlatDarkLaf", false, cl); }
            catch (ClassNotFoundException notOnPath) {
                if (jars == null || jars.length == 0) return false;
                URL[] urls = new URL[jars.length];
                for (int i = 0; i < jars.length; i++) urls[i] = jars[i].toURI().toURL();
                cl = new URLClassLoader(urls, cl);
            }
            Class<?> laf = Class.forName("com.formdev.flatlaf.FlatLaf", true, cl), dark = Class.forName("com.formdev.flatlaf.FlatDarkLaf", true, cl);
            Map<String, String> d = new HashMap<>();
            d.put("@background", hex(PANEL)); d.put("@foreground", hex(TEXT)); d.put("@accentColor", hex(ACCENT)); d.put("@selectionBackground", hex(SELECT));
            d.put("@selectionForeground", "#ffffff"); d.put("@componentBackground", hex(FIELD)); d.put("@disabledForeground", hex(DIM));
            d.put("@buttonBackground", "#1f282a"); d.put("@menuBackground", hex(PANEL2));
            laf.getMethod("setGlobalExtraDefaults", Map.class).invoke(null, d);
            boolean ok = (Boolean) dark.getMethod("setup").invoke(null);
            if (!ok) return false;
            UIManager.put("ClassLoader", cl);
            flat = true;
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static void installNimbus() {
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); } catch (Exception e) { return; }
        UIManager.put("control", PANEL); UIManager.put("info", PANEL2); UIManager.put("nimbusBase", new Color(40, 62, 58)); UIManager.put("nimbusLightBackground", FIELD);
        UIManager.put("text", TEXT); UIManager.put("nimbusFocus", ACCENT); UIManager.put("nimbusSelectionBackground", SELECT); UIManager.put("nimbusSelectedText", Color.WHITE);
        UIManager.put("nimbusDisabledText", DIM); UIManager.put("nimbusBlueGrey", new Color(46, 60, 58));
    }

    // ------------------------------------------------------------------ component helpers
    /** FlatLaf per-component styling (a plain client property, so it is harmless when FlatLaf is not active). */
    static <T extends JComponent> T style(T c, String css) { c.putClientProperty("FlatLaf.style", css); return c; }

    static <T extends AbstractButton> T chip(T b) { b.putClientProperty("JButton.buttonType", "roundRect"); return b; }

    /** Terrain tile colour filter: muted ink-wash versions of the old colours, bright gold for cities and violet for sects. */
    static Color tile(Color c, Area a) {
        boolean special = a != null && (a.city || a.sect != null || a.dungeonType != null || a.spiritualVein);
        if (a != null && a.city) return new Color(204, 164, 86);
        if (a != null && a.sect != null) return new Color(132, 94, 154);
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        Color d = Color.getHSBColor(hsb[0], hsb[1] * (special ? 0.85f : 0.52f), Math.min(1f, hsb[2] * 1.05f + 0.04f));
        double t = special ? 0.12 : 0.30;
        return new Color((int) (d.getRed() * (1 - t) + INK.getRed() * t), (int) (d.getGreen() * (1 - t) + INK.getGreen() * t), (int) (d.getBlue() * (1 - t) + INK.getBlue() * t));
    }

    /** Title strip: red seal, serif title, subtitle, a fading gold rule underneath. */
    static JComponent banner() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4)) {
            @Override protected void paintComponent(Graphics g0) {
                super.paintComponent(g0);
                Graphics2D g = (Graphics2D) g0;
                g.setPaint(new GradientPaint(0, 0, alpha(GOLD, 0), getWidth() * 0.6f, 0, alpha(GOLD, 150), false));
                g.fillRect(0, getHeight() - 1, (int) (getWidth() * 0.6), 1);
                g.setPaint(new GradientPaint(getWidth() * 0.6f, 0, alpha(GOLD, 150), getWidth(), 0, alpha(GOLD, 0), false));
                g.fillRect((int) (getWidth() * 0.6), getHeight() - 1, getWidth(), 1);
            }
        };
        p.setBackground(new Color(9, 12, 15));
        JLabel seal = new JLabel("仙", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0; g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(SEAL); g.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6); super.paintComponent(g0);
            }
        };
        seal.setFont(new Font(CJK, Font.BOLD, 20)); seal.setForeground(new Color(248, 232, 214)); seal.setPreferredSize(new Dimension(30, 30));
        JLabel title = new JLabel("Tales of Dream and Fantasy");
        title.setFont(serif(20)); title.setForeground(GOLD);
        JLabel sub = new JLabel("夢幻傳說");
        sub.setFont(new Font(CJK, Font.PLAIN, 14)); sub.setForeground(DIM);
        p.add(seal); p.add(title); p.add(sub);
        return p;
    }

    static Color alpha(Color c, int a) { return new Color(c.getRed(), c.getGreen(), c.getBlue(), a); }
}
