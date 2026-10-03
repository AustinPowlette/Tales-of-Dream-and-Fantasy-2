package Game;

import java.io.File;
import java.util.*;

/** Headless check of the in-game Help: every page renders, every [[link]] points at a page that exists, every `img` file exists in assets/help,
 *  no page mentions a button name that the dock no longer has. Run from the repo root so assets/help is found. */
public class HelpCheck {
    static int bad = 0, pages = 0, images = 0, links = 0;
    static void fail(String s) { bad++; System.out.println("[H] FAIL " + s); }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        List<HelpContent.Section> all = HelpContent.all();
        Set<String> ids = new HashSet<>();
        for (HelpContent.Section s : all) ids.add(s.id);
        File dir = HelpContent.helpDir();
        System.out.println("[H] info: " + all.size() + " pages, help dir " + dir.getAbsolutePath() + " exists=" + dir.isDirectory());
        // button names that were removed or renamed in the 2026-10-06 window overhaul
        String[] stale = {"Quests+", "Duties+", "((Do Quest))|Asks for a quest number", "↑ North", "E →"};
        for (HelpContent.Section s : all) {
            pages++;
            try {
                String html = HelpContent.body(s, HelpContent.paperPalette(), 700, true);
                if (html == null || html.length() < 50) fail("page " + s.id + " renders empty");
                HelpContent.body(s, HelpContent.inkPalette(), 500, false);
                for (String t : HelpContent.linkTargets(s)) { links++; if (!ids.contains(t)) fail("page " + s.id + " links to unknown page '" + t + "'"); }
                for (String b : s.blocks()) {
                    if (b.startsWith("img ")) {
                        images++;
                        String name = b.substring(4).split("\\|")[0].trim();
                        if (!new File(dir, name).isFile()) fail("page " + s.id + " needs image " + name);
                    }
                    if (b.startsWith("see ")) for (String t : b.substring(4).trim().split("\\s+")) { links++; if (!ids.contains(t)) fail("page " + s.id + " 'see' unknown page '" + t + "'"); }
                    for (String st : stale) if (b.contains(st)) fail("page " + s.id + " mentions the removed button text '" + st + "'");
                }
            } catch (RuntimeException e) { fail("page " + s.id + " threw " + e); e.printStackTrace(System.out); }
        }
        // search finds things
        if (HelpContent.search("quest").isEmpty()) fail("search 'quest' finds nothing");
        if (HelpContent.search("palace").isEmpty()) fail("search 'palace' finds nothing");
        if (HelpContent.search("oldest order").isEmpty()) fail("search 'oldest order' finds nothing");
        for (String must : new String[]{"start", "move", "me", "auto", "world", "quests", "duties", "keys", "lore", "combat", "sect", "economy", "terminal", "stats", "backpack", "browse", "saves", "settings"})
            if (!ids.contains(must)) fail("missing page '" + must + "'");
        // every window key must lead to an existing page
        for (String k : new String[]{"quests", "duties", "stats", "backpack", "locations", "relations", "people", "squad", "manage", "rankings", "market", "chronicle", "notes", "digest", "world", "paintedmap", "settings", "saves", "notices", "family", "story", "tutorial", "cheats", "keys", "here", "journal", "lorejournal", "preview", "browser", "scripts", "character", "main", "info:Quest list", "bio:1", "place:1:1", "list:people"}) {
            String id = GuiHub.helpId(k, k);
            if (!ids.contains(id)) fail("window key '" + k + "' maps to missing page '" + id + "'");
        }
        System.out.println("[H] ===== " + (bad == 0 ? "PASS" : "FAIL") + ": " + pages + " pages, " + links + " links, " + images + " images, " + bad + " problems =====");
        System.exit(bad == 0 ? 0 : 1);
    }
}
