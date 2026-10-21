package Game;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/** Writes data out of the game: CSV tables (history, rankings, any list) and PNG pictures (map, charts) into Settings.exportDir (default exports/). */
public class Exporter {
    private Exporter() { }

    static File dir() { File d = new File(Settings.exportDir); d.mkdirs(); return d; }

    static String stamp() { return new java.text.SimpleDateFormat("yyyyMMdd-HHmmss").format(new java.util.Date()); }

    static String cell(Object o) {
        String s = String.valueOf(o);
        return s.contains(",") || s.contains("\"") || s.contains("\n") ? "\"" + s.replace("\"", "\"\"") + "\"" : s;
    }

    static File csv(String base, String[] headers, List<Object[]> rows) throws Exception {
        File f = new File(dir(), base + "-" + stamp() + ".csv");
        try (PrintWriter w = new PrintWriter(f, "UTF-8")) {
            StringBuilder h = new StringBuilder();
            for (int i = 0; i < headers.length; i++) { if (i > 0) h.append(','); h.append(cell(headers[i])); }
            w.println(h);
            for (Object[] r : rows) {
                StringBuilder l = new StringBuilder();
                for (int i = 0; i < r.length; i++) { if (i > 0) l.append(','); l.append(cell(r[i])); }
                w.println(l);
            }
        }
        return f;
    }

    static File png(String base, BufferedImage img) throws Exception {
        File f = new File(dir(), base + "-" + stamp() + ".png");
        ImageIO.write(img, "png", f);
        return f;
    }

    /** One row per game year: year, population, cities, sects, shops. */
    static File history() throws Exception {
        List<Object[]> rows = new ArrayList<>();
        synchronized (GameDriver.statHistory) { for (int[] r : GameDriver.statHistory) rows.add(new Object[]{r[0], r[1], r[2], r[3], r[4]}); }
        return csv("history", new String[]{"year", "population", "cities", "sects", "shops"}, rows);
    }

    /** The 500 strongest living cultivators. */
    static File rankings() throws Exception {
        List<Character> alive = new ArrayList<>();
        int n = Game.Map.characters.size();
        for (int i = 0; i < n; i++) {
            Character c = Game.Map.characters.get(i);
            if (c != null && !c.dead && c.born && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster) alive.add(c);
        }
        alive.sort((a, b) -> Integer.compare(b.lvl, a.lvl));
        List<Object[]> rows = new ArrayList<>();
        for (Character c : alive.subList(0, Math.min(500, alive.size())))
            rows.add(new Object[]{c.id, c.name + " " + c.lastName, c.lvl, c.majorLvl, c.age, c.kills, PlayerLists.wealth(c), c.sect != null ? c.sect.name : "", c.location != null ? c.location.name : ""});
        return csv("rankings", new String[]{"id", "name", "level", "major", "age", "kills", "wealth_ss", "sect", "location"}, rows);
    }

    /** Any list view (people, shop, bag, places...) as CSV. */
    static File view(String base, PlayerLists.ListView v) throws Exception {
        return csv(base, v.headers, v.rows);
    }
}
