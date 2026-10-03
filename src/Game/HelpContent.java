package Game;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** The content model of the in-game Help: the list of sections (id, title, group), a tiny page markup compiled to Swing-friendly HTML in either of
 *  two palettes (warm paper / ink), the generated sections (terminal reference from PlayerCommands.HELP, keys from MapViewer.KEYS_HELP) and the
 *  plain-text index the search uses. The page text itself lives in HelpPages. No Swing here: GuiHelp does the window.
 *
 *  Page markup (one String per block; the first word is the tag):
 *    h2 Title | h3 Title | p text (the default) | lead text (bigger intro) | * bullet | # numbered item
 *    tip text | warn text | note text            coloured call-outs
 *    table A|B|C then row a|b|c ...              table with a header row
 *    cards then card id|Title|blurb ...          a two-column grid of links (the start page)
 *    img file.png|Caption                        a screenshot from assets/help (a dim caption when the file is missing)
 *    code line (lines of one block joined by \n) a monospace block
 *    see id id id                                "See also" links
 *  Inline: [[id]] [[id|label]] [[id#anchor|label]] (help link), [[win:name|label]] (opens that window), `code`, **bold**, ~~italic~~, {Key} (key cap), ((Button)) (button chip). */
final class HelpContent {
    private HelpContent() { }

    // ------------------------------------------------------------------ model
    static final class Section {
        final String id, title, group, summary, keywords;
        private final Supplier<String[]> source;
        private String[] cache;
        private String plain;
        Section(String id, String title, String group, String summary, String keywords, Supplier<String[]> source) {
            this.id = id; this.title = title; this.group = group; this.summary = summary; this.keywords = keywords; this.source = source;
        }
        String[] blocks() { if (cache == null) cache = source.get(); return cache; }
        /** Lower-case plain text of the whole page (title, summary, keywords and body) for the search. */
        String plain() {
            if (plain == null) plain = (title + " " + summary + " " + keywords + " " + stripHtml(body(this, paperPalette(), 700, false))).toLowerCase();
            return plain;
        }
    }

    static final String[] GROUPS = {"Getting started", "The main screen", "Move tab", "Me tab", "Auto tab", "World tab", "Game systems", "Terminal commands", "Reference"};

    private static final LinkedHashMap<String, Section> SECTIONS = new LinkedHashMap<>();
    private static boolean built = false;

    private static synchronized void ensure() {
        if (built) return;
        built = true;
        HelpPages.register();
    }

    /** Called by HelpPages for every section, in reading order. */
    static void add(String id, String title, String group, String summary, String keywords, Supplier<String[]> blocks) {
        SECTIONS.put(id, new Section(id, title, group, summary, keywords, blocks));
    }

    static void add(String id, String title, String group, String summary, String keywords, final String... blocks) {
        add(id, title, group, summary, keywords, (Supplier<String[]>) () -> blocks);
    }

    static List<Section> all() { ensure(); return new ArrayList<>(SECTIONS.values()); }
    static Section get(String id) { ensure(); return id == null ? null : SECTIONS.get(id); }
    static boolean exists(String id) { return get(id) != null; }
    static String title(String id) { Section s = get(id); return s == null ? id : s.title; }

    static List<Section> inGroup(String group) {
        List<Section> out = new ArrayList<>();
        for (Section s : all()) if (s.group.equals(group)) out.add(s);
        return out;
    }

    /** Sections matching every word of the query, best first (title hits, then keyword/summary hits, then body hits). */
    static List<Section> search(String query) {
        List<Section> out = new ArrayList<>();
        String q = query == null ? "" : query.trim().toLowerCase();
        if (q.isEmpty()) return out;
        String[] words = q.split("\\s+");
        List<int[]> scored = new ArrayList<>();
        List<Section> all = all();
        for (int i = 0; i < all.size(); i++) {
            Section s = all.get(i);
            String text = s.plain();
            int score = 0;
            boolean ok = true;
            for (String w : words) {
                int hits = countOf(text, w);
                if (hits == 0) { ok = false; break; }
                score += Math.min(hits, 12);
                if (s.title.toLowerCase().contains(w)) score += 40;
                if (s.keywords.toLowerCase().contains(w)) score += 15;
                if (s.summary.toLowerCase().contains(w)) score += 8;
            }
            if (ok) scored.add(new int[]{score, i});
        }
        scored.sort((a, b) -> a[0] != b[0] ? Integer.compare(b[0], a[0]) : Integer.compare(a[1], b[1]));
        for (int[] sc : scored) out.add(all.get(sc[1]));
        return out;
    }

    private static int countOf(String text, String w) {
        int n = 0, i = 0;
        while ((i = text.indexOf(w, i)) >= 0) { n++; i += Math.max(1, w.length()); }
        return n;
    }

    /** A short plain snippet around the first hit of the query (for the result list). */
    static String snippet(Section s, String query) {
        String t = s.plain();
        String[] words = query.trim().toLowerCase().split("\\s+");
        int at = -1;
        for (String w : words) { int i = t.indexOf(w); if (i >= 0 && (at < 0 || i < at)) at = i; }
        if (at < 0) return s.summary;
        // the plain text starts with title + summary + keywords: prefer a hit in the body when there is one
        int bodyStart = (s.title + " " + s.summary + " " + s.keywords + " ").length();
        for (String w : words) { int i = t.indexOf(w, bodyStart); if (i >= 0) { at = i; break; } }
        int from = Math.max(0, at - 40), to = Math.min(t.length(), at + 90);
        String sn = t.substring(from, to).replaceAll("\\s+", " ").trim();
        return (from > 0 ? "..." : "") + sn + (to < t.length() ? "..." : "");
    }

    /** Every `help:<id>` link target used on a page (the probe checks they all exist). */
    static List<String> linkTargets(Section s) {
        List<String> out = new ArrayList<>();
        Matcher m = Pattern.compile("href='help:([^'#]+)").matcher(body(s, paperPalette(), 700, false));
        while (m.find()) out.add(m.group(1));
        return out;
    }

    // ------------------------------------------------------------------ palettes
    static final class Palette {
        String bg, text, dim, h1, h2, link, rule, panel, code, codeBg, keyBg, keyFg, chipBg, chipFg;
        String tipBar, tipBg, warnBar, warnBg, noteBar, noteBg, thFg, thBg, rowAlt, hitBg, hitFg;
        boolean paper;
    }

    static Palette paperPalette() {
        Palette p = new Palette();
        p.paper = true;
        p.bg = "#f4eedc"; p.text = "#2e2c26"; p.dim = "#6d6757"; p.h1 = "#1b5a50"; p.h2 = "#8a6418"; p.link = "#0d6f66"; p.rule = "#cbbf9b";
        p.panel = "#ece3c8"; p.code = "#1d4a43"; p.codeBg = "#e6dec4"; p.keyBg = "#34403f"; p.keyFg = "#f6efdb"; p.chipBg = "#cfe4da"; p.chipFg = "#1b4a42";
        p.tipBar = "#2f8f7a"; p.tipBg = "#dcebe2"; p.warnBar = "#b8433a"; p.warnBg = "#f3ddd4"; p.noteBar = "#b08d3a"; p.noteBg = "#f1e7c6";
        p.thFg = "#5c4410"; p.thBg = "#e4d9b8"; p.rowAlt = "#ede5cd"; p.hitBg = "#ffe27a"; p.hitFg = "#2e2c26";
        return p;
    }

    /** Ink palette: the game's own Theme colours (so the high-contrast setting carries over), softened. */
    static Palette inkPalette() {
        Palette p = new Palette();
        p.paper = false;
        p.bg = Theme.hex(Theme.PANEL); p.text = Theme.hex(Theme.TEXT); p.dim = Theme.hex(Theme.DIM); p.h1 = Theme.hex(Theme.GOLD); p.h2 = Theme.hex(Theme.ACCENT);
        p.link = "#86d6bd"; p.rule = Theme.hex(Theme.LINE); p.panel = Theme.hex(Theme.PANEL2); p.code = "#ecd08e"; p.codeBg = Theme.hex(Theme.FIELD);
        p.keyBg = "#46544f"; p.keyFg = "#f4ecd4"; p.chipBg = "#27413c"; p.chipFg = "#c4eadb";
        p.tipBar = Theme.hex(Theme.ACCENT); p.tipBg = "#1a2a29"; p.warnBar = Theme.hex(Theme.DANGER); p.warnBg = "#2c1f1d"; p.noteBar = Theme.hex(Theme.GOLD); p.noteBg = "#2a2619";
        p.thFg = Theme.hex(Theme.GOLD); p.thBg = Theme.hex(Theme.PANEL2); p.rowAlt = "#192125"; p.hitBg = "#8a6d12"; p.hitFg = "#ffffff";
        return p;
    }

    // ------------------------------------------------------------------ rendering
    static File helpDir = null;

    /** assets/help: next to the working directory, or next to the code (so a jar or an IDE run from a sub-folder still finds it). */
    static File helpDir() {
        if (helpDir != null) return helpDir;
        List<File> cands = new ArrayList<>();
        cands.add(new File("assets/help"));
        try {
            File here = new File(HelpContent.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File p = here.isDirectory() ? here : here.getParentFile();
            for (int i = 0; i < 4 && p != null; i++, p = p.getParentFile()) cands.add(new File(p, "assets/help"));
        } catch (Exception ignored) { }
        for (File f : cands) if (f.isDirectory()) { helpDir = f; return f; }
        helpDir = cands.get(0);
        return helpDir;
    }

    private static final Map<String, int[]> IMG_SIZE = new HashMap<>();

    private static int[] imageSize(File f) {
        int[] r = IMG_SIZE.get(f.getPath());
        if (r != null) return r;
        r = new int[]{0, 0};
        try (javax.imageio.stream.ImageInputStream in = javax.imageio.ImageIO.createImageInputStream(f)) {
            java.util.Iterator<javax.imageio.ImageReader> it = javax.imageio.ImageIO.getImageReaders(in);
            if (it.hasNext()) {
                javax.imageio.ImageReader rd = it.next();
                try { rd.setInput(in); r = new int[]{rd.getWidth(0), rd.getHeight(0)}; } finally { rd.dispose(); }
            }
        } catch (Exception ignored) { }
        IMG_SIZE.put(f.getPath(), r);
        return r;
    }

    static String esc(String s) { return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"); }

    static String stripHtml(String html) {
        String s = html.replaceAll("(?s)<style.*?</style>", " ").replaceAll("<[^>]+>", " ");
        return s.replace("&nbsp;", " ").replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&").replace("&#8226;", " ").replaceAll("\\s+", " ").trim();
    }

    static String slug(String s) { return s.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", ""); }

    /** The whole HTML document of a section for a page `width` pixels wide (the text column is capped for comfortable line length). */
    static String page(Section s, Palette p, int width, double scale) {
        int fs = (int) Math.round(15 * scale);
        int colMax = (int) Math.round(760 * scale);
        int margin = Math.max(22, (width - 26 - colMax) / 2);
        StringBuilder sb = new StringBuilder("<html><head><style>");
        sb.append("body { font-family: 'Segoe UI', SansSerif; font-size: ").append(fs).append("px; color: ").append(p.text).append("; background-color: ").append(p.bg)
          .append("; margin-top: 12px; margin-bottom: 26px; margin-left: ").append(margin).append("px; margin-right: ").append(margin).append("px; }");
        sb.append(" p { margin-top: 0px; margin-bottom: 10px; }");
        sb.append(" li { margin-bottom: 4px; }");
        sb.append(" ul { margin-top: 2px; margin-bottom: 10px; margin-left: 22px; }");
        sb.append(" ol { margin-top: 2px; margin-bottom: 10px; margin-left: 26px; }");
        sb.append(" h1 { font-family: Georgia, serif; font-size: ").append((int) Math.round(30 * scale)).append("px; color: ").append(p.h1).append("; margin-top: 6px; margin-bottom: 2px; }");
        sb.append(" h2 { font-family: Georgia, serif; font-size: ").append((int) Math.round(21 * scale)).append("px; color: ").append(p.h2).append("; margin-top: 22px; margin-bottom: 5px; }");
        sb.append(" h3 { font-family: 'Segoe UI', SansSerif; font-size: ").append((int) Math.round(16 * scale)).append("px; color: ").append(p.h1).append("; margin-top: 14px; margin-bottom: 3px; }");
        sb.append(" a { color: ").append(p.link).append("; }");
        sb.append(" td { font-size: ").append(fs).append("px; }");
        sb.append(" th { font-size: ").append(Math.round(13 * scale)).append("px; color: ").append(p.thFg).append("; text-align: left; }");
        sb.append(" code { font-family: Consolas, monospace; font-size: ").append(Math.round(14 * scale)).append("px; color: ").append(p.code).append("; background-color: ").append(p.codeBg).append("; }");
        sb.append(" pre { font-family: Consolas, monospace; font-size: ").append(Math.round(13.5 * scale)).append("px; color: ").append(p.code).append("; margin: 0px; }");
        sb.append(" .dim { color: ").append(p.dim).append("; }");
        sb.append("</style></head><body>");
        sb.append(body(s, p, width, true).replace("@@FS@@", String.valueOf(fs)));
        sb.append("</body></html>");
        return sb.toString();
    }

    /** The body of a section, without the document wrapper. `withTitle` adds the big heading. */
    static String body(Section s, Palette p, int width, boolean withTitle) {
        StringBuilder sb = new StringBuilder();
        if (withTitle) {
            sb.append("<p class='dim' style='margin-bottom:0px'>").append(esc(s.group.toUpperCase())).append("</p>");
            sb.append("<h1>").append(esc(s.title)).append("</h1>");
            sb.append("<table width='100%' cellspacing='0' cellpadding='0'><tr><td bgcolor='").append(p.rule).append("' height='2'></td></tr></table>");
            sb.append("<p style='margin-bottom:6px'> </p>");
        }
        String[] blocks = s.blocks();
        int i = 0;
        int contentW = Math.max(260, width - 2 * Math.max(22, (width - 26 - 760) / 2) - 30);
        while (i < blocks.length) {
            String b = blocks[i];
            String tag = tagOf(b), rest = restOf(b);
            switch (tag) {
                case "h2": sb.append("<a name='").append(slug(rest)).append("'></a><h2>").append(inline(rest, p)).append("</h2>"); i++; break;
                case "h3": sb.append("<a name='").append(slug(rest)).append("'></a><h3>").append(inline(rest, p)).append("</h3>"); i++; break;
                case "lead": sb.append("<p style='font-size:").append("@@FS@@").append("px'>").append(inline(rest, p)).append("</p>"); i++; break;
                case "*": case "#": {
                    String list = tag.equals("*") ? "ul" : "ol";
                    sb.append("<").append(list).append(">");
                    while (i < blocks.length && tagOf(blocks[i]).equals(tag)) { sb.append("<li>").append(inline(restOf(blocks[i]), p)).append("</li>"); i++; }
                    sb.append("</").append(list).append(">");
                    break;
                }
                case "tip": sb.append(callout("Tip", rest, p.tipBar, p.tipBg, p)); i++; break;
                case "warn": sb.append(callout("Careful", rest, p.warnBar, p.warnBg, p)); i++; break;
                case "note": sb.append(callout("Note", rest, p.noteBar, p.noteBg, p)); i++; break;
                case "table": {
                    String[] head = rest.split("\\|", -1);
                    sb.append("<table width='100%' cellspacing='0' cellpadding='6' border='0'><tr bgcolor='").append(p.thBg).append("'>");
                    for (String h : head) sb.append("<th>").append(inline(h.trim().replace('¦', '|'), p)).append("</th>");
                    sb.append("</tr>");
                    i++;
                    int r = 0;
                    while (i < blocks.length && tagOf(blocks[i]).equals("row")) {
                        String[] cells = restOf(blocks[i]).split("\\|", -1);
                        sb.append("<tr valign='top'").append(r++ % 2 == 1 ? " bgcolor='" + p.rowAlt + "'" : "").append(">");
                        for (int c = 0; c < head.length; c++) sb.append("<td>").append(inline(c < cells.length ? cells[c].trim().replace('¦', '|') : "", p)).append("</td>");
                        sb.append("</tr>");
                        i++;
                    }
                    sb.append("</table><p style='margin-bottom:4px'> </p>");
                    break;
                }
                case "cards": {
                    i++;
                    List<String[]> cs = new ArrayList<>();
                    while (i < blocks.length && tagOf(blocks[i]).equals("card")) { cs.add(restOf(blocks[i]).split("\\|", 3)); i++; }
                    sb.append("<table width='100%' cellspacing='8' cellpadding='0'>");
                    for (int c = 0; c < cs.size(); c += 2) {
                        sb.append("<tr valign='top'>");
                        for (int k = c; k < c + 2; k++) {
                            if (k >= cs.size()) { sb.append("<td width='50%'></td>"); continue; }
                            String[] cd = cs.get(k);
                            sb.append("<td width='50%' bgcolor='").append(p.panel).append("'><table width='100%' cellspacing='0' cellpadding='9'><tr><td>")
                              .append("<b><a href='help:").append(cd[0]).append("'>").append(inline(cd.length > 1 ? cd[1] : title(cd[0]), p)).append("</a></b><br>")
                              .append("<span class='dim'>").append(inline(cd.length > 2 ? cd[2] : "", p)).append("</span></td></tr></table></td>");
                        }
                        sb.append("</tr>");
                    }
                    sb.append("</table><p style='margin-bottom:4px'> </p>");
                    break;
                }
                case "listkeys": {
                    sb.append("<table width='100%' cellspacing='0' cellpadding='6' border='0'><tr bgcolor='").append(p.thBg).append("'><th>List</th><th>Keys you can filter or sort by</th></tr>");
                    String[][] ks = {{"`list people` (and `people`)", String.join(" ", PlayerLists.PEOPLE_KEYS)}, {"`list monsters`", String.join(" ", PlayerLists.PEOPLE_KEYS)}, {"`list shop`", String.join(" ", PlayerLists.ITEM_KEYS)},
                            {"`list bag`", String.join(" ", PlayerLists.BAG_KEYS)}, {"`list places`", String.join(" ", PlayerLists.PLACE_KEYS)}, {"`find` (everyone in the world)", String.join(" ", PlayerLists.WORLD_KEYS)}};
                    for (int k = 0; k < ks.length; k++)
                        sb.append("<tr valign='top'").append(k % 2 == 1 ? " bgcolor='" + p.rowAlt + "'" : "").append("><td>").append(inline(ks[k][0], p)).append("</td><td><code>").append(esc(ks[k][1])).append("</code></td></tr>");
                    sb.append("</table><p style='margin-bottom:4px'> </p>");
                    i++;
                    break;
                }
                case "img": sb.append(image(rest, p, contentW)); i++; break;
                case "code": {
                    sb.append("<table width='100%' cellspacing='0' cellpadding='8'><tr><td bgcolor='").append(p.codeBg).append("'><pre>");
                    sb.append(esc(rest).replace("\\n", "\n"));
                    sb.append("</pre></td></tr></table><p style='margin-bottom:4px'> </p>");
                    i++;
                    break;
                }
                case "see": {
                    sb.append("<p class='dim'>See also: ");
                    String[] ids = rest.trim().split("\\s+");
                    for (int k = 0; k < ids.length; k++) sb.append(k > 0 ? " &nbsp;&middot;&nbsp; " : "").append("<a href='help:").append(ids[k]).append("'>").append(esc(title(ids[k]))).append("</a>");
                    sb.append("</p>");
                    i++;
                    break;
                }
                case "html": sb.append(rest); i++; break; // generated sections hand over finished HTML
                case "p": default: sb.append("<p>").append(inline(tag.equals("p") ? rest : b, p)).append("</p>"); i++; break;
            }
        }
        return sb.toString();
    }

    private static String tagOf(String b) {
        int sp = b.indexOf(' ');
        String t = sp < 0 ? b : b.substring(0, sp);
        switch (t) {
            case "h2": case "h3": case "lead": case "*": case "#": case "tip": case "warn": case "note": case "table": case "row": case "cards": case "card":
            case "img": case "code": case "see": case "html": case "listkeys": case "p": return t;
            default: return "p";
        }
    }

    private static String restOf(String b) {
        int sp = b.indexOf(' ');
        if (sp < 0) return "";
        String t = b.substring(0, sp);
        return tagOf(b).equals("p") && !t.equals("p") ? b : b.substring(sp + 1);
    }

    private static String callout(String label, String text, String bar, String bg, Palette p) {
        return "<table width='100%' cellspacing='0' cellpadding='0'><tr><td width='6' bgcolor='" + bar + "'></td><td bgcolor='" + bg + "'>"
                + "<table width='100%' cellspacing='0' cellpadding='9'><tr><td><b><font color='" + bar + "'>" + label + ".</font></b> " + inline(text, p) + "</td></tr></table></td></tr></table>"
                + "<p style='margin-bottom:2px'> </p>";
    }

    private static String image(String spec, Palette p, int contentW) {
        String[] parts = spec.split("\\|", 2);
        String file = parts[0].trim(), cap = parts.length > 1 ? parts[1].trim() : "";
        File f = new File(helpDir(), file);
        if (!f.isFile()) return "<p class='dim'><i>[Picture: " + inline(cap.isEmpty() ? file : cap, p) + " — the screenshot is not installed (assets/help/" + esc(file) + ")]</i></p>";
        int[] sz = imageSize(f);
        String dims = "";
        if (sz[0] > 0) {
            int w = Math.min(sz[0], contentW), h = Math.round(sz[1] * (w / (float) sz[0]));
            dims = " width='" + w + "' height='" + h + "'";
        }
        String url;
        try { url = f.toURI().toURL().toExternalForm(); } catch (Exception e) { url = "file:" + f.getPath(); }
        return "<table width='100%' cellspacing='0' cellpadding='0'><tr><td align='center'><table cellspacing='0' cellpadding='1' bgcolor='" + p.rule + "'><tr><td><img src='" + url + "'" + dims + "></td></tr></table></td></tr>"
                + (cap.isEmpty() ? "" : "<tr><td align='center'><span class='dim'><i>" + inline(cap, p) + "</i></span></td></tr>") + "</table><p style='margin-bottom:4px'> </p>";
    }

    private static final Pattern CODE = Pattern.compile("`([^`]+)`");
    private static final Pattern LINK = Pattern.compile("\\[\\[([^\\]|]+)(?:\\|([^\\]]*))?\\]\\]");
    private static final Pattern CHIP = Pattern.compile("\\(\\(([^)]+)\\)\\)");
    private static final Pattern KEY = Pattern.compile("\\{([^}]{1,24})\\}");
    private static final Pattern BOLD = Pattern.compile("\\*\\*(.+?)\\*\\*");
    private static final Pattern ITAL = Pattern.compile("~~(.+?)~~");

    /** Compiles inline markup (see the class comment) to HTML. */
    static String inline(String raw, Palette p) {
        String s = esc(raw).replace("\\n", "<br>");
        List<String> codes = new ArrayList<>();
        Matcher m = CODE.matcher(s);
        StringBuffer sb = new StringBuffer();
        while (m.find()) { codes.add("<code>&nbsp;" + m.group(1) + "&nbsp;</code>"); m.appendReplacement(sb, "\u0001" + (codes.size() - 1) + "\u0002"); }
        m.appendTail(sb);
        s = sb.toString();
        s = replace(LINK, s, g -> {
            String target = g[1], label = g[2];
            if (target.startsWith("win:") || target.startsWith("cmd:")) return "<a href='" + target + "'>" + (label == null ? target.substring(4) : label) + "</a>";
            String id = target.indexOf('#') >= 0 ? target.substring(0, target.indexOf('#')) : target;
            return "<a href='help:" + target + "'>" + (label == null ? esc(title(id)) : label) + "</a>";
        });
        s = replace(CHIP, s, g -> "<span style='background-color:" + p.chipBg + "; color:" + p.chipFg + "'>&nbsp;" + g[1] + "&nbsp;</span>");
        s = replace(KEY, s, g -> "<span style='font-family:Consolas; font-size:13px; background-color:" + p.keyBg + "; color:" + p.keyFg + "'>&nbsp;" + g[1] + "&nbsp;</span>");
        s = replace(BOLD, s, g -> "<b>" + g[1] + "</b>");
        s = replace(ITAL, s, g -> "<i>" + g[1] + "</i>");
        for (int i = 0; i < codes.size(); i++) s = s.replace("\u0001" + i + "\u0002", codes.get(i));
        return s;
    }

    private interface Fn { String f(String[] groups); }

    private static String replace(Pattern pat, String s, Fn fn) {
        Matcher m = pat.matcher(s);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String[] g = new String[m.groupCount() + 1];
            for (int i = 0; i < g.length; i++) g[i] = m.group(i);
            m.appendReplacement(sb, Matcher.quoteReplacement(fn.f(g)));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /** Wraps every hit of the query words in a highlight, leaving tags alone. */
    static String highlight(String html, String query, Palette p) {
        if (query == null || query.trim().isEmpty()) return html;
        List<String> words = new ArrayList<>();
        for (String w : query.trim().split("\\s+")) if (w.length() >= 2) words.add(Pattern.quote(esc(w)));
        if (words.isEmpty()) return html;
        Pattern pat = Pattern.compile("(" + String.join("|", words) + ")", Pattern.CASE_INSENSITIVE);
        StringBuilder out = new StringBuilder();
        int i = 0, n = html.length();
        boolean inStyle = false;
        while (i < n) {
            char ch = html.charAt(i);
            if (ch == '<') {
                int e = html.indexOf('>', i);
                if (e < 0) { out.append(html, i, n); break; }
                String tag = html.substring(i, e + 1);
                String tl = tag.toLowerCase();
                if (tl.startsWith("<style")) inStyle = true;
                if (tl.startsWith("</style")) inStyle = false;
                out.append(tag);
                i = e + 1;
            } else {
                int e = html.indexOf('<', i);
                if (e < 0) e = n;
                String text = html.substring(i, e);
                if (inStyle) out.append(text);
                else {
                    Matcher m = pat.matcher(text);
                    StringBuffer sb = new StringBuffer();
                    while (m.find()) m.appendReplacement(sb, Matcher.quoteReplacement("<span style='background-color:" + p.hitBg + "; color:" + p.hitFg + "'>" + m.group(1) + "</span>"));
                    m.appendTail(sb);
                    out.append(sb);
                }
                i = e;
            }
        }
        return out.toString();
    }

    // ------------------------------------------------------------------ generated sections
    /** Plain-English gloss for the groups of PlayerCommands.HELP, keyed by the label before the colon. */
    static final String[][] COMMAND_GLOSS = {
            {"Move", "Walking and waiting. Moving uses your turn."}, {"Modes", "Hand control to the game (AUTO) or back (MANUAL), or run a script."},
            {"Duties", "Which chores your character does by itself."}, {"Policy", "How your character answers fights and prompts when you are not at the keys."},
            {"Quests", "Look at, pin, delete, mute, add and perform quests."}, {"Info", "Look at things. These cost no time."},
            {"Lists", "The filter-and-sort language for cultivators, beasts, shop stock, your bag and places."}, {"Pick", "How to point at a person."},
            {"Seek", "Pin a quest to reach someone anywhere in the world."}, {"Shops", "Buy and sell with the city shops."},
            {"Work", "Everyday actions that use your turn."}, {"Roles", "Cities, sects, wars."}, {"Shop", "Set the prices of a shop you own."},
            {"Do", "Perform a named quest where you stand."}, {"Script", "Build a hybrid-mode script."}, {"Look", "Maps, routes, history, notes."},
            {"Gear", "What you wear and know."}, {"Market", "The goods market of a city."}, {"Life", "Taming, food, wine, poison, music, rebirth."},
            {"Look+", "More ways to look at the world."}, {"Run", "Statistics of your whole run."}, {"Wait", "Let time pass until something happens."},
            {"System", "The character editor, cheats, saving, pause."}};

    /** Terminal reference rendered straight from PlayerCommands.HELP, so it cannot drift from the real command list. */
    static String[] terminalReference() {
        List<String> b = new ArrayList<>();
        b.add("lead This table is generated from the game's own command list (`help` in the command box prints the same lines), so it is always up to date. A **turn** passes only for commands that act (move, work, fight); commands that only look at things are free.");
        b.add("table Group|Commands");
        Map<String, String> gloss = new HashMap<>();
        for (String[] g : COMMAND_GLOSS) gloss.put(g[0], g[1]);
        List<String> rows = new ArrayList<>();
        for (String line : PlayerCommands.HELP.split("\n")) {
            int i = line.indexOf(':');
            if (line.startsWith("===") || i < 1 || i > 12) continue;
            String label = line.substring(0, i).trim(), rest = line.substring(i + 1).trim();
            String g = gloss.get(label);
            rows.add("row **" + label + "**|" + (g != null ? "~~" + g + "~~\\n" : "") + (label.equals("Pick") ? rest.replace("`", "'") : commandCell(rest, g != null)).replace('|', '¦'));
        }
        b.addAll(rows);
        b.add("tip Anywhere the table says `<person>` you can give a row number from the last `people` list, or `#<id>` for anyone standing in your tile. Leave the person out of `talk`, `spar`, `attack`, `invite` and `recruit` and the game picks one suitable person for you.");
        b.add("p More commands that are not in that list are described in [[terminal-more|Other terminal commands]], and the detailed manual pages (`help <topic>`) in [[terminal-topics|Command manual pages]].");
        b.add("see terminal terminal-more terminal-topics keys");
        return b.toArray(new String[0]);
    }

    /** One table cell: each ' | ' separated command in a code chip on its own line, with any trailing explanation in plain text. */
    private static String commandCell(String rest, boolean glossed) {
        StringBuilder sb = new StringBuilder();
        for (String item : rest.split(" \\| ")) {
            String it = item.trim();
            if (it.isEmpty()) continue;
            String code = it, expl = "";
            int cut = indexOfAny(it, new String[]{"   ", "  — ", " — ", "  (", "   e.g.", "  e.g."});
            if (cut > 0) { code = it.substring(0, cut).trim(); expl = it.substring(cut).replaceFirst("^\\s*[—-]*\\s*", "").trim(); }
            code = code.replace("`", "'");
            sb.append("`").append(code).append("`");
            if (!expl.isEmpty()) sb.append(" ").append(expl.replace("`", "'"));
            sb.append("<br>");
        }
        return sb.toString().replaceAll("(<br>)+$", "").replace("<br>", "\\n");
    }

    private static int indexOfAny(String s, String[] needles) {
        int best = -1;
        for (String n : needles) { int i = s.indexOf(n); if (i > 0 && (best < 0 || i < best)) best = i; }
        return best;
    }

    /** Commands that exist but are not in PlayerCommands.HELP: {the command words (so the probe can check them), syntax, what it does}. */
    static final String[][] EXTRA_COMMANDS = {
            {"nw ne sw se", "`nw` `ne` `sw` `se`", "Move diagonally: one turn, like the pathfinder."},
            {"x stay", "`x` `stay`", "Wait one turn (the Wait button)."},
            {"quit exit", "`quit` `quit nosave`", "Save and quit (asks first), or quit without saving."},
            {"goquest gq", "`goquest [n]`", "Walk to the place of quest n (numbers from `q`), or of your top quest that has a place; for a quest about a person, chase them."},
            {"chase", "`chase <#id or name>`", "Follow a cultivator wherever they go, until you share a tile."},
            {"monsters", "`monsters [filters]`", "Like `people`, but for the beasts and monsters in your tile."},
            {"squad", "`squad`", "Your squad (party), in the Squad & Sect window."},
            {"find", "`find <name or #id> [limit] [filters]`", "Search everyone in the world by name or id."},
            {"control", "`control <#id>`", "Become that cultivator; your current one carries on as an NPC (asks first)."},
            {"follow unfollow track untrack", "`follow <#id>` `unfollow <#id>`", "Show or stop showing a cultivator in the NPC tracker and on the map."},
            {"preview", "`preview <#id>`", "Win chance, how lethal, escape odds and relationship before a fight."},
            {"save saves load delsave", "`save [name]` `saves` `load <name>` `delsave <name>`", "Quick or named save, list saves, load, delete."},
            {"export", "`export history|rankings|people|monsters|shop|bag|places|world|run`", "Write a CSV file to the exports folder."},
            {"story", "`story`", "Your Story objectives."},
            {"tutorial", "`tutorial`", "Open the welcome tour (or print the walkthrough in the terminal)."},
            {"guide", "`guide [section]`", "Open this Help (in the terminal it tells you where to read)."},
            {"settings", "`settings` `settings <key> <value>` `settings notify <kind> on|off`", "Show or change options (see Settings)."},
            {"notices", "`notices [n]`", "The latest notifications."},
            {"recap", "`recap`", "Preview the periodic recap."},
            {"family relations", "`family` `relations [n]`", "The family and relationship lists as text."},
            {"music", "`music on|off|new|volume <n>|fonts|font <name>|engine auto|builtin|soundfont|presets|mix|song`", "Control the generated music."},
            {"cheat", "`cheat <self|#id> ss|level|potential|kills|weapon <n>` `cheat <target> heal|reset|track`", "The commands behind the Cheats window."},
            {"unnote", "`unnote <n>`", "Delete note number n."},
            {"set", "`set limit <n>`", "Same as `limit <n>`."}};

    /** Words that are other names for commands already documented. */
    static final String ALIASES = "quests places relationships players ls beasts cook brew fame run tech chronicle date dashboard pause ? track untrack exit gq";

    /** The commands not in the main list, as a table (the probe checks every real command is either in PlayerCommands.HELP, here, or an alias). */
    static String[] terminalMore() {
        List<String> b = new ArrayList<>();
        b.add("lead These work in the command box but are not in the main list, because most of them have a button. Some also have a second name (`quests` for `q`, `places` for `l`, `relationships` for `r`, `stats` for `i`).");
        b.add("table Command|What it does");
        for (String[] e : EXTRA_COMMANDS) b.add("row " + e[1].replace('|', '¦') + "|" + e[2]);
        b.add("see terminal terminal-ref terminal-topics");
        return b.toArray(new String[0]);
    }

    /** The manual pages (`help <topic>`) from TextTools.TOPICS. */
    static String[] terminalTopics() {
        List<String> b = new ArrayList<>();
        b.add("lead Type `help <topic>` in the command box for any of these. They are shown here exactly as the game prints them.");
        for (String[] t : TextTools.TOPICS) {
            b.add("h3 " + t[0]);
            b.add("code " + t[1].replace("\n", "\\n"));
        }
        return b.toArray(new String[0]);
    }

    /** The keyboard list, parsed from MapViewer.KEYS_HELP (one source of truth with the F1 sheet and the real key handler). */
    static String[] keyTable() {
        List<String> b = new ArrayList<>();
        b.add("lead Most of the game is one key away. The keys work whenever the main window is active and you are **not typing in a text box**.");
        b.add("table Key|Does");
        List<String> notes = new ArrayList<>();
        for (String line : MapViewer.KEYS_HELP.split("\n")) {
            if (line.trim().startsWith("(")) { notes.add(line.trim()); continue; }
            for (String entry : line.trim().split("\\s{3,}")) {
                int sp = entry.indexOf("  ");
                if (sp < 0) continue;
                String keys = entry.substring(0, sp).trim(), what = entry.substring(sp).trim();
                StringBuilder k = new StringBuilder();
                for (String one : keys.split("\\s*/\\s*")) { if (k.length() > 0) k.append(" or "); k.append("{").append(one).append("}"); }
                b.add("row " + k + "|" + what);
            }
        }
        for (String n : notes) b.add("note " + n.replace("(", "").replace(")", ""));
        return b.toArray(new String[0]);
    }
}
