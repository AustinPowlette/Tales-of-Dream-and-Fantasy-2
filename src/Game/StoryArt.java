package Game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static Game.ArtGen.DH;
import static Game.ArtGen.DW;
import static Game.ArtGen.alpha;
import static Game.ArtGen.c;
import static Game.ArtGen.mix;
import static Game.ArtGen.poly;

/** The pictures of the story and the lore. Every place a scene happens in has a painted picture, in the watercolour style of `ArtGen` (Originis has had one from the start): the nine places of the Palace's story, the
 *  Empty Seat, the Door, the Under-Kitchen, the Hollow Court, the Giant Footprints and the Garden, and a picture for every kind of place the ten-and-eight stories send the human to (a harbour, a village, a lantern street,
 *  an inn, a mine, a forge, a library, a ferry, a marsh, a pass, a shrine, a camp, a tower, a hall, a castle ...). The scene window shows the picture of its place at the top, with a portrait of each of the people who speak in it
 *  (the same portraits as the people's cards: `ArtMore.portraitCached`). A story's places have names of their own ("harbour", "quarter", "kessel"); `forPlace` says which picture each name gets, and a chapter without a place
 *  shows its story's home. Pictures are painted from a seed (the same place always looks the same) and kept in a small cache; nothing needs a file. */
final class StoryArt {
    private StoryArt() { }

    static final int W = 640, H = 360;

    /** Every picture there is. */
    static final String[] KEYS = {
        "palace", "wayhouse", "weaver", "cistern", "assayer", "shed", "thinsky", "lanternroad", "menagerie", "garrison", "seat", "door", "undercroft", "court", "footprint", "garden",
        "harbour", "village", "town", "quarter", "inn", "hall", "castle", "pavilion", "library", "forge", "kitchen", "mine", "cave", "forest", "meadow", "water", "marsh", "cliff", "wastes", "shrine", "tower", "camp", "road", "sky",
        "platforms"};

    private static final Map<String, String> PLACE = new HashMap<>();
    private static void alias(String art, String... places) { for (String p : places) PLACE.put(p, art); }
    static {
        alias("palace", "palace", "palace3");
        alias("library", "archive", "library");
        alias("hall", "academy", "conserv", "guild", "table", "hall", "manor", "court");
        alias("cave", "vault", "deep", "cellar", "hide", "corridor");
        alias("road", "crossing", "crossroads");
        alias("meadow", "valley", "plain", "field", "farm", "country", "meadow", "vale");
        alias("cliff", "pass", "north", "cliff", "marrow");
        alias("village", "village", "village1", "village2", "hamlet", "granny", "home");
        alias("water", "ferry", "bridge", "weir", "lake", "wreck");
        alias("marsh", "marsh");
        alias("town", "mei", "town", "concord", "kessel", "fair", "ostrow", "pellmoor", "lowstreet", "wen", "cinder", "deliver");
        alias("quarter", "quarter", "lane");
        alias("inn", "inn");
        alias("weaver", "wall");
        alias("castle", "castle");
        alias("sky", "sky");
        alias("court", "hollow");
        alias("kitchen", "kitchen");
        alias("shed", "shed");
        alias("harbour", "harbour");
        alias("mine", "mine");
        alias("shrine", "shrine", "grave", "well");
        alias("tower", "tower", "light");
        alias("camp", "fordcamp", "camp", "site");
        alias("wastes", "wastes");
        alias("forest", "wood", "oak", "glade");
        alias("pavilion", "pavilion", "gala");
        for (String k : KEYS) if (!PLACE.containsKey(k)) PLACE.put(k, k);
    }

    /** The home of each story: what its scenes show when they are not in a place of their own. */
    private static final Map<String, String> HOME = new HashMap<>();
    static {
        String[][] h = {{"heir", "castle"}, {"scion", "town"}, {"orphan", "quarter"}, {"disgraced", "library"}, {"smith", "forge"}, {"tamer", "meadow"}, {"ghost", "water"}, {"lucky", "harbour"}, {"crane", "pavilion"},
            {"disciple", "cliff"}, {"chef", "kitchen"}, {"poisoner", "marsh"}, {"tailor", "quarter"}, {"inventor", "library"}, {"musician", "inn"}, {"miner", "mine"}, {"cheater", "hall"}, {"forager", "forest"}};
        for (String[] x : h) HOME.put(x[0], x[1]);
    }

    /** The picture a story's place name gets (a chapter's `.at("harbour")`, a cast member's `.at("place:harbour")`); the story's home when the name is unknown or empty. */
    static String forPlace(String story, String place) {
        if (place != null) { String p = place.startsWith("place:") ? place.substring(6) : place; String a = PLACE.get(p); if (a != null) return a; }
        return HOME.getOrDefault(story, "road");
    }

    static String homeOf(String story) { return HOME.getOrDefault(story, "road"); }

    /** The picture of a scene of the Palace's story, by the id of the scene (`LoreText`, `LoreEndings`). */
    static String forLore(String id) {
        if (id == null) return "palace";
        if (id.startsWith("site_")) return id.substring(5);
        switch (id) {
            case "undercroft": return "undercroft";
            case "court_hands": case "erased": case "dimming": case "omen": case "out_hollow": case "end_dark": return "court";
            case "seat": case "early_oldman": case "end_oldman": case "end_oldman_full": case "end_oldman_stay": case "out_throne": return "seat";
            case "door7": case "early_door": case "end_threshold": case "end_leave": case "early_leave": case "end_attack": case "end_attack_whole": case "end_attack_dead": case "end_attack_spared": case "early_strike": case "early_strike_dead": case "early_strike_spared": case "early_rearguard": return "door";
            case "end_soldier": case "end_soldier_front": case "end_soldier_guard": case "end_soldier_keep": case "end_soldier_war": case "out_deserter": return "garrison";
            case "end_ascendant": case "out_peak": case "out_fall": return "thinsky";
            case "end_idler": case "out_garden": return "garden";
            case "out_road": return "road";
            case "out_teahouse": return "inn";
            case "out_mirror": return "cistern";
            case "out_ferry": return "water";
            case "out_farm": return "meadow";
            case "out_dust": return "wastes";
            case "rope": return "assayer";
            case "idler": return "garden";
            case "soldier": case "last_lamp": return "garrison";
            case "steward": case "usurper": return "palace";
            default:
                if (id.startsWith("ascendant")) return "thinsky";
                if (id.startsWith("early_") || id.startsWith("leave")) return "door";
                return "palace";
        }
    }

    // ------------------------------------------------------------------ the cache and the painting
    private static final LinkedHashMap<String, BufferedImage> cache = new LinkedHashMap<String, BufferedImage>(48, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> e) { return size() > 40; }
    };

    /** The picture (painted once, then kept). `seed` varies the weather and the small things; the same key and seed give the same picture. */
    static BufferedImage picture(String key, long seed, int w, int h) {
        if (key == null) key = "road";
        String ck = key + "|" + seed + "|" + w + "x" + h;
        synchronized (cache) {
            BufferedImage im = cache.get(ck);
            if (im == null) { im = paint(key, seed, w, h); cache.put(ck, im); }
            return im;
        }
    }
    static BufferedImage picture(String key) { return picture(key, 7, W, H); }

    private static BufferedImage paint(String key, long seed, int w, int h) {
        Random r = new Random(Naming.mix64(seed + key.hashCode()));
        BufferedImage im = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = im.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setColor(c(238, 231, 214)); g.fillRect(0, 0, w, h);
        g.scale(w / (double) DW, h / (double) DH);
        ArtGen.Painter p = new ArtGen.Painter(g, r, 1.0, ArtGen.palette("", r), "", null);
        try { draw(p, key); }
        catch (RuntimeException e) { p.sky(200); p.mountainLayers(190, 2, 70, false); }
        g.dispose();
        if (w >= 64) { im = ArtGen.kuwahara(im, 2); ArtGen.paper(im, r); }
        return im;
    }

    // ------------------------------------------------------------------ moods of the sky
    private static void dusk(ArtGen.Painter p) { p.pal.skyTop = c(74, 78, 120); p.pal.skyBot = c(238, 170, 130); p.pal.sun = c(252, 196, 140); p.pal.far = c(120, 120, 150); p.pal.near = c(52, 56, 78); p.pal.mist = c(236, 200, 190); }
    private static void night(ArtGen.Painter p) { p.pal.skyTop = c(18, 22, 50); p.pal.skyBot = c(62, 70, 110); p.pal.sun = c(236, 230, 200); p.pal.far = c(70, 78, 112); p.pal.near = c(26, 32, 52); p.pal.ground = c(52, 64, 70); p.pal.mist = c(120, 130, 160); p.pal.leafDark = c(20, 44, 44); p.pal.leafLight = c(50, 80, 74); }
    private static void dawn(ArtGen.Painter p) { p.pal.skyTop = c(176, 196, 224); p.pal.skyBot = c(250, 222, 196); p.pal.sun = c(255, 224, 170); }
    private static void grey(ArtGen.Painter p) { p.pal.skyTop = c(168, 174, 180); p.pal.skyBot = c(214, 214, 208); p.pal.far = c(142, 150, 154); p.pal.near = c(84, 94, 96); p.pal.ground = c(120, 130, 112); }
    private static void dim(ArtGen.Painter p) { p.pal.skyTop = c(36, 34, 40); p.pal.skyBot = c(84, 70, 66); p.pal.ground = c(58, 52, 50); p.pal.far = c(70, 66, 74); p.pal.near = c(36, 34, 40); }

    private static void lantern(ArtGen.Painter p, double x, double y, double s, boolean lit) {
        p.stroke(x, y - 12 * s, x, y - 7 * s, 0.9 * s, c(60, 50, 44), 0.9);
        p.washCircle(x, y - 3 * s, 3.2 * s, 4.2 * s, lit ? c(240, 150, 60) : c(70, 62, 60), 3, 0.95, 0.08);
        if (lit) { p.g.setColor(alpha(c(255, 190, 90), 0.12)); p.g.fill(new java.awt.geom.Ellipse2D.Double(x - 14 * s, y - 17 * s, 28 * s, 28 * s)); }
    }

    private static void house(ArtGen.Painter p, double x, double y, double w, double h, Color wall, Color roof, boolean lit) {
        p.washRect(x - w / 2, y - h, w, h, wall, 3, 0.95, 0.04);
        p.wash(poly(x - w * 0.64, y - h + 2, x - w * 0.3, y - h - h * 0.5, x + w * 0.3, y - h - h * 0.5, x + w * 0.64, y - h + 2), roof, 3, 0.96, 0.05);
        if (lit) p.washRect(x - w * 0.12, y - h * 0.62, w * 0.24, h * 0.3, c(252, 214, 130), 2, 0.95, 0.05);
        p.washRect(x - w * 0.09, y - h * 0.34, w * 0.18, h * 0.34, mix(roof, c(30, 24, 22), 0.5), 2, 0.9, 0.04);
    }

    private static void smoke(ArtGen.Painter p, double x, double y) {
        for (int k = 0; k < 5; k++) p.stroke(x + Math.sin(k + x) * 3, y - k * 7, x + Math.sin(k + 1 + x) * 3, y - (k + 1) * 7, 2.2, c(214, 210, 204), 0.4);
    }

    private static void ghost(ArtGen.Painter p, double x, double y, double s, double a) {
        p.wash(poly(x - 4 * s, y, x - 3 * s, y - 14 * s, x, y - 18 * s, x + 3 * s, y - 14 * s, x + 4 * s, y, x + 1 * s, y - 2 * s, x - 1 * s, y), alpha(c(236, 240, 246), 1), 3, a, 0.1);
        p.washCircle(x, y - 17 * s, 2.6 * s, 2.8 * s, c(240, 244, 250), 2, a, 0.1);
    }

    // ------------------------------------------------------------------ the pictures
    private static void draw(ArtGen.Painter p, String key) {
        Random r = p.r;
        switch (key) {
            case "palace": ArtCity.capital(p); break;

            // ---------------------------------------------------------------- the Palace's story
            case "wayhouse": {                       // an inn by a river that nobody lights
                dusk(p);
                p.sky(170); p.moon(400, 60, 20, c(236, 232, 214));
                p.mountainLayers(168, 2, 70, false);
                int[] back = p.ridge(190, 6, 0.8, 3); p.land(back, DH + 10, mix(p.pal.far, p.pal.ground, 0.5), 0.92, false, p.pal.far);
                p.waterBody(232, DH + 4);
                for (int i = 0; i < 8; i++) p.stroke(40 + r.nextInt(430), 236 + r.nextInt(40), 90 + r.nextInt(430), 236 + r.nextInt(40), 1.5, c(230, 220, 210), 0.2);
                p.washRect(196, 196, 110, 38, c(110, 92, 80), 4, 0.95, 0.04);
                p.wash(poly(184, 200, 236, 166, 270, 166, 322, 200), c(46, 46, 52), 4, 0.97, 0.05);
                for (double wx : new double[]{218, 256, 286}) p.washRect(wx - 6, 208, 12, 16, c(40, 38, 44), 2, 0.95, 0.04); // dark windows
                lantern(p, 316, 226, 1.4, false);                       // the lantern that is not lit
                for (int i = 0; i < 3; i++) p.broad(60 + i * 40, 236, 34 + r.nextInt(14), p.pal.leafDark);
                p.deadTree(440, 238, 46);
                p.washCircle(470, 150, 28, 10, c(252, 210, 140), 4, 0.35, 0.2); // the far glow of Originis
                p.mist(232, 20, 0.35);
                break;
            }
            case "weaver": {                         // a wall of threads at the edge of the world
                dawn(p); p.sky(150);
                for (int i = 0; i < 150; i++) {
                    double x = i * 3.4 + r.nextInt(3), top = 20 + r.nextInt(40), bot = 190 + r.nextInt(30);
                    Color tc = mix(c(150 + r.nextInt(60), 130 + r.nextInt(60), 120 + r.nextInt(70)), c(236, 226, 206), r.nextDouble() * 0.5);
                    p.stroke(x, top, x + r.nextGaussian() * 2, bot, 2.4, tc, 0.55);
                }
                for (int y = 40; y < 200; y += 9) p.stroke(0, y + r.nextInt(3), DW, y + r.nextInt(3), 1.6, c(236, 224, 200), 0.22);
                p.washRect(248, 10, 16, 200, c(255, 246, 200), 6, 0.8, 0.1);   // the seam: a line of light where the world joins itself
                p.lightRays(256, 20, 5, 0.22);
                p.land(p.ridge(206, 6, 0.8, 3), DH + 10, mix(p.pal.ground, p.pal.sand, 0.4), 0.95, false, p.pal.far);
                p.grass(210, 280, 120, p.pal.leafDark, p.pal.leafLight);
                p.figure(256, 252, 1.4);
                p.mist(205, 18, 0.35);
                break;
            }
            case "cistern": {                        // a flooded vault of stone basins
                p.pal.skyTop = c(30, 54, 62); p.pal.skyBot = c(72, 110, 112);
                p.gradientRect(0, DH, c(24, 40, 48), c(70, 104, 108));
                for (int i = 0; i < 6; i++) {
                    double x = 40 + i * 86;
                    p.washRect(x - 14, 40, 28, 170, c(120, 130, 124), 4, 0.9, 0.04);
                    p.stroke(x - 14, 60, x + 72, 40, 7, c(104, 114, 110), 0.7);
                }
                p.lightRays(256, 0, 4, 0.35);
                p.waterBody(200, DH + 4);
                for (int i = 0; i < 4; i++) { double x = 80 + i * 110, y = 236 + (i % 2) * 14; p.washCircle(x, y, 40, 10, c(120, 128, 124), 5, 0.9, 0.05); p.washCircle(x, y, 32, 7, p.pal.waterDeep, 3, 0.8, 0.05); }
                for (int i = 0; i < 18; i++) { double x = 20 + r.nextInt(470), y = 206 + r.nextInt(70); p.stroke(x, y, x + 14, y, 1.2, c(250, 252, 250), 0.35); }
                p.figure(420, 214, 1.1);
                p.mist(200, 26, 0.3);
                break;
            }
            case "assayer": {                        // a hall on high ground with a great balance and nine locks
                p.sky(150); p.mountainLayers(170, 3, 80, true);
                p.washRect(100, 150, 312, 70, c(226, 214, 190), 4, 0.96, 0.03);
                p.wash(poly(86, 156, 150, 118, 362, 118, 426, 156), c(70, 74, 74), 4, 0.97, 0.04);
                for (int i = 0; i < 9; i++) { double x = 126 + i * 32; p.washCircle(x, 196, 5, 6, c(190, 160, 70), 3, 0.95, 0.06); p.washRect(x - 1, 198, 2, 8, c(110, 86, 40), 2, 0.9, 0.03); }
                p.stroke(256, 60, 256, 150, 5, c(60, 52, 46), 0.95);               // the balance: a post, a beam, two pans on chains
                p.stroke(176, 70, 336, 70, 4, c(60, 52, 46), 0.95);
                for (double px : new double[]{176, 336}) {
                    p.stroke(px, 70, px - 14, 112, 1.2, c(70, 60, 50), 0.8); p.stroke(px, 70, px + 14, 112, 1.2, c(70, 60, 50), 0.8);
                    p.washCircle(px, 116, 22, 6, c(190, 160, 70), 4, 0.95, 0.05);
                }
                p.land(p.ridge(222, 5, 0.8, 3), DH + 10, mix(p.pal.ground, p.pal.rock, 0.5), 0.95, false, p.pal.far);
                for (int i = 0; i < 10; i++) p.stroke(40 + i * 44, 236, 56 + i * 44, 264, 2, c(190, 184, 170), 0.4);
                p.figure(256, 260, 1.3);
                break;
            }
            case "shed": {                           // a shed of strange tools in a forest
                p.pal.leafDark = c(26, 62, 44); p.sky(130);
                for (int row = 0; row < 3; row++) for (int i = 0; i < 12; i++) p.pine(10 + i * 44 + r.nextInt(16), 170 + row * 34, 70 + row * 18 + r.nextInt(20), mix(p.pal.leafDark, p.pal.leafLight, row * 0.2));
                p.land(p.ridge(232, 4, 0.8, 3), DH + 10, mix(p.pal.ground, p.pal.leafDark, 0.4), 0.96, false, p.pal.far);
                p.washRect(180, 170, 150, 64, c(124, 98, 72), 4, 0.96, 0.04);
                p.wash(poly(168, 174, 210, 140, 300, 140, 342, 174), c(70, 58, 48), 4, 0.97, 0.05);
                String[] tools = {"hoe", "shears", "rake", "saw", "lantern", "scales"};
                for (int i = 0; i < 6; i++) { double x = 196 + i * 22; p.stroke(x, 180, x, 214, 1.8, c(58, 48, 42), 0.9); p.washCircle(x, 216 + (i % 2) * 4, 5, 3, c(150, 150, 140), 2, 0.9, 0.08); }
                p.washRect(240, 204, 24, 30, c(252, 214, 128), 3, 0.9, 0.04);      // the door, a little open, and warm
                p.lightRays(252, 205, 2, 0.15);
                p.spores(30);
                break;
            }
            case "thinsky": {                        // a peak under a cracked sky
                p.pal.skyTop = c(150, 168, 200); p.pal.skyBot = c(238, 228, 214);
                p.sky(180);
                for (int k = 0; k < 4; k++) {
                    double x = 120 + r.nextInt(280), y = 0;
                    for (int s = 0; s < 9; s++) { double nx = x + r.nextGaussian() * 18, ny = y + 18 + r.nextInt(14); p.stroke(x, y, nx, ny, 3.4 - k * 0.5, c(255, 244, 190), 0.9); p.stroke(x, y, nx, ny, 9, c(255, 240, 180), 0.18); x = nx; y = ny; }
                }
                p.lightRays(256, 0, 5, 0.3);
                p.mountainLayers(196, 3, 130, true, 2);
                p.figure(250, 168, 1.2);
                p.mist(206, 30, 0.35);
                break;
            }
            case "lanternroad": {                    // a road of lanterns the dead still walk
                night(p); p.sky(170); p.moon(120, 56, 22, c(236, 232, 214));
                p.mountainLayers(176, 2, 50, false);
                p.dunes(190, 3, c(96, 100, 112), c(46, 50, 66));
                for (int i = 0; i < 8; i++) { double t = i / 7.0, x = 256 - 190 * (1 - t) * (i % 2 == 0 ? 1 : -1) * 0 + (i % 2 == 0 ? -1 : 1) * (14 + 30 * t), y = 196 + t * 78, s = 0.7 + t * 1.2; lantern(p, x, y, s, true); }
                p.wash(poly(236, 200, 276, 200, 330, 290, 190, 290), c(70, 74, 86), 4, 0.8, 0.05);   // the road widening toward us
                for (int i = 0; i < 6; i++) ghost(p, 236 + r.nextInt(46), 204 + i * 11, 0.5 + i * 0.12, 0.55);
                p.mist(230, 30, 0.3);
                break;
            }
            case "menagerie": {                      // a round hall of numbered cells
                p.sky(160); p.mountainLayers(176, 2, 60, false);
                p.washCircle(256, 188, 190, 62, c(210, 196, 170), 5, 0.96, 0.04);
                p.washCircle(256, 170, 170, 52, c(228, 216, 192), 4, 0.9, 0.04);
                for (int i = 0; i < 16; i++) {
                    double a = Math.PI + Math.PI * (i + 0.5) / 16, cx = 256 + Math.cos(a) * 150, cy = 196 - Math.sin(a) * 48 * -1;
                    double ax = 256 + Math.cos(a) * 150, ay = 196 + Math.sin(a) * 26 * -1 + 14;
                    p.washRect(ax - 7, ay - 18, 14, 24, c(60, 52, 48), 2, 0.9, 0.04);
                    p.stroke(ax - 4, ay - 24, ax + 4, ay - 24, 1.4, c(150, 60, 50), 0.9);
                }
                p.wash(poly(66, 170, 256, 116, 446, 170), c(70, 66, 66), 4, 0.97, 0.04);
                p.land(p.ridge(246, 4, 0.8, 3), DH + 10, mix(p.pal.ground, p.pal.sand, 0.5), 0.96, false, p.pal.far);
                p.figure(256, 262, 1.2);
                break;
            }
            case "garrison": {                       // a fortress as if the army had just left
                dusk(p); p.sky(150);
                p.mountainLayers(160, 2, 50, false);
                p.washRect(70, 120, 372, 100, c(176, 166, 150), 5, 0.97, 0.03);
                for (double x = 74; x < 440; x += 12) p.washRect(x, 112, 7, 10, c(176, 166, 150), 2, 0.95, 0.04);
                ArtCity.tower(p, 100, 120, 5, 1.5); ArtCity.tower(p, 412, 120, 5, 1.5);
                p.washRect(228, 156, 56, 64, c(46, 40, 38), 3, 0.97, 0.03);      // the open gate, dark
                p.wash(poly(222, 160, 256, 140, 290, 160), c(70, 60, 56), 3, 0.95, 0.04);
                for (double bx : new double[]{140, 372}) { p.stroke(bx, 124, bx, 90, 1.4, c(70, 56, 44), 0.9); p.wash(poly(bx, 92, bx + 18, 100, bx, 110), c(120, 60, 54), 3, 0.9, 0.06); }
                p.land(p.ridge(236, 5, 0.8, 3), DH + 10, mix(p.pal.ground, p.pal.sand, 0.5), 0.96, false, p.pal.far);
                for (int i = 0; i < 12; i++) { double x = 150 + r.nextInt(220), y = 240 + r.nextInt(30); p.stroke(x, y, x + 6, y - 1, 1.6, c(120, 110, 100), 0.5); }  // what the army left: a few bootprints
                p.mist(234, 22, 0.4);
                break;
            }
            case "seat": {                           // the Empty Seat
                p.pal.skyTop = c(20, 18, 28); p.pal.skyBot = c(56, 46, 54);
                p.gradientRect(0, DH, c(16, 14, 22), c(62, 52, 58));
                for (int i = 0; i < 5; i++) { double x = 40 + i * 56; double s = 1 - i * 0.05; p.washRect(x - 11, 30, 22, 210, c(70, 64, 70), 4, 0.92, 0.04); p.washRect(DW - x - 11, 30, 22, 210, c(70, 64, 70), 4, 0.92, 0.04); }
                p.lightRays(256, 0, 3, 0.4);
                p.washRect(220, 176, 72, 8, c(150, 140, 130), 3, 0.9, 0.03);       // the dais
                p.washRect(232, 136, 48, 40, c(96, 70, 56), 4, 0.96, 0.04);        // the seat
                p.washRect(228, 90, 56, 48, c(110, 80, 64), 4, 0.96, 0.04);        // the back of the chair
                p.stroke(256, 88, 256, 70, 3, c(190, 160, 80), 0.9);
                p.washRect(244, 246, 24, 40, c(252, 240, 190), 5, 0.95, 0.08);    // the Door's line of light under the floor
                p.stroke(240, 246, 272, 246, 2, c(255, 248, 210), 0.9);
                p.mist(236, 20, 0.2);
                break;
            }
            case "door": {                           // one door standing alone in a white field
                p.pal.skyTop = c(236, 238, 242); p.pal.skyBot = c(250, 248, 242); p.sky(180);
                p.gradientRect(150, DH, c(244, 244, 240), c(222, 226, 226));
                p.washRect(214, 54, 84, 150, c(120, 94, 70), 5, 0.96, 0.03);
                p.washRect(224, 64, 64, 140, c(255, 250, 220), 6, 0.96, 0.03);        // light through it
                p.stroke(256, 64, 256, 204, 1.4, c(150, 120, 90), 0.8);
                p.lightRays(256, 130, 6, 0.28);
                p.washCircle(279, 136, 3, 3, c(190, 160, 80), 2, 0.95, 0.04);
                p.figure(256, 262, 1.5);
                p.mist(206, 26, 0.5);
                break;
            }
            case "undercroft": case "kitchen": {     // a vaulted kitchen below the Palace (or an honest one)
                boolean low = key.equals("undercroft");
                p.pal.skyTop = c(40, 30, 28); p.pal.skyBot = c(120, 80, 56);
                p.gradientRect(0, DH, c(34, 26, 24), c(112, 76, 54));
                for (int i = 0; i < 7; i++) { double x = i * 80 - 10; p.stroke(x, 0, x + 40, 70, 8, c(96, 84, 76), 0.8); p.stroke(x + 80, 0, x + 40, 70, 8, c(96, 84, 76), 0.8); }
                p.washRect(70, 190, 372, 24, c(120, 88, 62), 4, 0.96, 0.03);         // a long table
                for (int i = 0; i < 4; i++) { double x = 120 + i * 90; p.washCircle(x, 184, 20, 12, c(60, 56, 56), 4, 0.96, 0.04); smoke(p, x, 176); p.washRect(x - 14, 176, 28, 4, c(210, 170, 90), 2, 0.7, 0.04); }
                for (int i = 0; i < 9; i++) { double x = 50 + i * 52; p.stroke(x, 30, x, 56 + r.nextInt(14), 1.2, c(70, 60, 54), 0.8); p.washCircle(x, 62 + r.nextInt(14), 8, 8, c(150, 120, 80), 3, 0.9, 0.06); }
                p.washCircle(256, 250, 70, 18, c(250, 150, 60), 5, 0.5, 0.2);        // a hearth
                p.embers(40);
                if (low) { for (int i = 0; i < 7; i++) p.washCircle(86 + i * 56, 232, 6, 3, c(224, 206, 190), 2, 0.7, 0.1); }   // the pale hands of the cooks, resting
                break;
            }
            case "court": {                          // the Hollow Court: robes with nobody in them
                p.pal.skyTop = c(18, 30, 30); p.pal.skyBot = c(70, 92, 84);
                p.gradientRect(0, DH, c(14, 26, 26), c(60, 80, 74));
                p.washRect(0, 220, DW, 80, c(44, 62, 58), 4, 0.95, 0.04);
                for (int i = 0; i < 9; i++) {
                    double a = Math.PI * (i + 0.5) / 9, x = 256 - Math.cos(a) * 190, y = 214 + Math.sin(a) * 40 * -1 + 40 - 30 * Math.sin(a), s = 1.4 + 0.5 * Math.sin(a);
                    p.wash(poly(x - 9 * s, y, x - 6 * s, y - 28 * s, x - 3 * s, y - 36 * s, x + 3 * s, y - 36 * s, x + 6 * s, y - 28 * s, x + 9 * s, y), c(70 + r.nextInt(16), 96 + r.nextInt(20), 90), 4, 0.95, 0.05);
                    p.washCircle(x, y - 38 * s, 3.6 * s, 4 * s, c(10, 18, 18), 3, 0.96, 0.04);       // a hood with no face in it
                }
                p.stroke(256, 0, 256, 64, 1.2, c(120, 130, 120), 0.8);
                p.washCircle(256, 76, 11, 14, c(84, 100, 96), 3, 0.9, 0.05);                         // the lamp that is not lit
                p.mist(220, 40, 0.3);
                break;
            }
            case "footprint": {                      // a footprint the size of a lake
                p.sky(150); p.mountainLayers(160, 3, 90, true);
                p.land(p.ridge(206, 5, 0.8, 3), DH + 10, mix(p.pal.ground, p.pal.sand, 0.4), 0.96, false, p.pal.far);
                p.washCircle(256, 244, 150, 30, c(96, 100, 92), 6, 0.9, 0.05);
                p.washCircle(256, 246, 134, 24, p.pal.water, 4, 0.9, 0.05);
                for (int i = 0; i < 5; i++) { double x = 150 + i * 53, y = 206 - Math.sin(i / 4.0 * Math.PI) * 16; p.washCircle(x, y, 20 - Math.abs(i - 2) * 2, 8, c(96, 100, 92), 5, 0.9, 0.05); p.washCircle(x, y + 1, 15 - Math.abs(i - 2) * 2, 5, p.pal.water, 3, 0.9, 0.05); }
                p.figure(420, 252, 1.3);
                break;
            }
            case "platforms": {                      // the Garden's Variety Tournament: the platforms fall out of the opened sky
                night(p); p.sky(200); p.moon(410, 60, 26, c(244, 238, 214));
                p.lightRays(256, -8, 6, 0.32);
                for (int i = 0; i < 28; i++) {
                    double x = 10 + r.nextInt(DW - 20), y = 20 + r.nextInt(190), w = 22 + (y / 190.0) * 46;
                    p.wash(poly(x - w / 2, y, x + w / 2, y, x + w / 2 - 5, y + w * 0.12, x - w / 2 + 5, y + w * 0.12), c(196, 196, 214), 3, 0.95, 0.05);
                    p.stroke(x, y + w * 0.12, x + r.nextGaussian() * 2, y + w * 0.12 + 14 + r.nextInt(30), 1.4, c(240, 236, 255), 0.25);
                }
                p.mountainLayers(230, 2, 50, false);
                p.washCircle(256, 262, 120, 14, c(196, 196, 214), 5, 0.9, 0.05);    // the platform that is yours
                p.figure(256, 262, 1.8);
                p.flakes(30);
                break;
            }
            case "garden": {                         // the Garden itself: ponds, a bridge, blossoms
                p.sky(150); p.mountainLayers(160, 2, 60, false);
                p.land(p.ridge(176, 5, 0.8, 3), DH + 10, p.pal.ground, 0.96, false, p.pal.far);
                p.waterBody(214, DH + 4);
                for (int i = 0; i < 40; i++) { double a = Math.PI * i / 39; p.stroke(150 + Math.cos(a + Math.PI) * 110, 240 - Math.sin(a) * 26, 150 + Math.cos(a + Math.PI + 0.08) * 110, 240 - Math.sin(a + 0.08) * 26, 3, c(210, 196, 176), 0.8); }
                p.pavilion(380, 214, 2.4);
                for (int i = 0; i < 6; i++) p.broad(50 + i * 70 + r.nextInt(20), 214, 46 + r.nextInt(14), mix(p.pal.leafLight, c(250, 196, 210), 0.4));
                p.flowers(60); p.crane(190, 112, 1.6); p.crane(220, 128, 1.2);
                break;
            }

            // ---------------------------------------------------------------- places of the stories
            case "harbour": {
                p.sky(150); p.sun(380, 100, 26); p.mountainLayers(150, 2, 50, false);
                p.waterBody(170, DH + 4);
                for (int i = 0; i < 5; i++) p.boat(60 + i * 90 + r.nextInt(20), 190 + r.nextInt(60), 0.9 + r.nextDouble() * 0.5);
                p.washRect(0, 168, DW, 10, c(120, 98, 76), 4, 0.95, 0.03);                // the quay
                for (int i = 0; i < 6; i++) house(p, 40 + i * 84, 168, 56, 34, c(224, 210, 186), c(70, 66, 66), i % 3 == 0);
                p.stroke(420, 168, 420, 96, 3, c(70, 56, 44), 0.9); p.stroke(420, 100, 470, 160, 1.4, c(70, 56, 44), 0.8);        // a crane for cargo
                p.birds(120, 60, 5, 1.2);
                break;
            }
            case "village": {
                p.sky(150); p.mountainLayers(160, 2, 70, false);
                p.land(p.ridge(184, 8, 0.8, 3), DH + 10, p.pal.ground, 0.96, false, p.pal.far);
                for (int i = 0; i < 7; i++) { double x = 50 + i * 70 + r.nextInt(16), y = 214 + r.nextInt(46), s = 0.8 + (y - 214) / 120.0; house(p, x, y, 46 * s, 28 * s, c(228, 214, 188), c(150, 124, 80), i % 3 == 1); if (i % 2 == 0) smoke(p, x + 6, y - 40 * s); }
                p.wash(poly(236, 200, 276, 200, 340, 290, 180, 290), c(196, 178, 140), 4, 0.8, 0.05);
                for (int i = 0; i < 4; i++) p.broad(30 + i * 130 + r.nextInt(30), 206 + r.nextInt(20), 40, p.pal.leafLight);
                p.grass(230, 285, 80, p.pal.leafDark, p.pal.leafLight);
                break;
            }
            case "town": {
                p.sky(150); p.sun(110, 90, 26); p.mountainLayers(150, 2, 60, false);
                ArtCity.skyline(p, 2);
                p.waterBody(236, DH + 4);
                for (int i = 0; i < 30; i++) { double a = Math.PI * i / 29; p.stroke(190 + Math.cos(a + Math.PI) * 60, 250 - Math.sin(a) * 16, 190 + Math.cos(a + Math.PI + 0.1) * 60, 250 - Math.sin(a + 0.1) * 16, 3, c(180, 160, 140), 0.8); }
                p.boat(360, 264, 1.2); p.boat(90, 270, 1.0);
                break;
            }
            case "quarter": {                        // a lantern street at dusk
                dusk(p); p.sky(120);
                for (int row = 0; row < 2; row++) for (int side = 0; side < 2; side++) {
                    for (int i = 0; i < 5; i++) {
                        double t = i / 4.0, s = 1.2 - t * 0.7, x = side == 0 ? 30 + t * 150 : 482 - t * 150, y = 250 - t * 56 - row * 0;
                        house(p, x, y, 70 * s, 70 * s, c(214, 196, 170), c(60, 56, 60), i % 2 == 0);
                    }
                }
                p.wash(poly(226, 190, 286, 190, 400, 290, 112, 290), c(168, 150, 130), 4, 0.9, 0.05);
                for (int i = 0; i < 8; i++) { double y = 60 + r.nextInt(50); lantern(p, 60 + i * 56, y, 1.5, true); p.stroke(60 + i * 56, y - 14, 60 + (i + 1) * 56, y - 18, 0.8, c(70, 56, 44), 0.6); }
                p.figure(256, 262, 1.2);
                break;
            }
            case "inn": {
                dusk(p); p.sky(170); p.mountainLayers(170, 2, 60, false);
                p.land(p.ridge(212, 6, 0.8, 3), DH + 10, mix(p.pal.ground, c(80, 70, 64), 0.4), 0.96, false, p.pal.far);
                house(p, 256, 244, 170, 60, c(214, 192, 160), c(60, 52, 52), true);
                for (double x : new double[]{200, 312}) p.washRect(x - 8, 214, 16, 16, c(252, 214, 130), 2, 0.95, 0.04);
                lantern(p, 340, 238, 1.6, true);
                p.wash(poly(380, 250, 440, 250, 436, 268, 384, 268), c(110, 82, 58), 3, 0.95, 0.05);      // a cart
                p.washCircle(396, 272, 8, 8, c(70, 56, 44), 3, 0.9, 0.05); p.washCircle(426, 272, 8, 8, c(70, 56, 44), 3, 0.9, 0.05);
                for (int i = 0; i < 4; i++) p.pine(30 + i * 40, 228, 60 + r.nextInt(20), p.pal.leafDark);
                break;
            }
            case "hall": {                           // a great hall behind a court of steps
                p.sky(150); p.mountainLayers(160, 2, 60, false);
                p.washRect(110, 150, 292, 70, c(226, 206, 176), 4, 0.96, 0.03);
                p.wash(poly(86, 160, 150, 108, 362, 108, 426, 160), c(150, 66, 54), 4, 0.97, 0.04);
                p.wash(poly(120, 160, 170, 126, 342, 126, 392, 160), c(60, 62, 60), 4, 0.9, 0.04);
                for (int i = 0; i < 6; i++) p.stroke(130 + i * 50, 160, 130 + i * 50, 218, 4, c(150, 66, 54), 0.9);
                for (int i = 0; i < 7; i++) p.stroke(120 + i * 2, 220 + i * 6, 392 - i * 2, 220 + i * 6, 3, c(206, 196, 178), 0.7);
                p.pavilion(60, 234, 1.6); p.pavilion(452, 234, 1.6);
                p.figure(256, 266, 1.3);
                break;
            }
            case "castle": {
                p.sky(150); p.mountainLayers(170, 3, 100, true);
                p.washRect(120, 120, 272, 94, c(172, 164, 152), 5, 0.97, 0.03);
                for (double x = 124; x < 388; x += 12) p.washRect(x, 112, 7, 10, c(172, 164, 152), 2, 0.95, 0.04);
                ArtCity.tower(p, 120, 120, 6, 1.5); ArtCity.tower(p, 392, 120, 6, 1.5); ArtCity.tower(p, 256, 112, 8, 1.7);
                p.washRect(236, 162, 40, 52, c(46, 40, 38), 3, 0.97, 0.03);
                for (double bx : new double[]{170, 342}) { p.stroke(bx, 124, bx, 92, 1.4, c(70, 56, 44), 0.9); p.wash(poly(bx, 94, bx + 18, 102, bx, 112), c(190, 52, 44), 3, 0.95, 0.06); }
                p.land(p.ridge(226, 6, 0.8, 3), DH + 10, mix(p.pal.rock, p.pal.ground, 0.4), 0.97, true, p.pal.rockDark);
                p.mist(220, 24, 0.4);
                break;
            }
            case "pavilion": {
                p.sky(150); p.mountainLayers(160, 2, 70, false);
                p.land(p.ridge(190, 6, 0.8, 3), DH + 10, p.pal.ground, 0.96, false, p.pal.far);
                p.waterBody(222, DH + 4);
                p.washRect(176, 214, 160, 10, c(190, 176, 150), 4, 0.95, 0.03);
                p.pavilion(256, 214, 3.4);
                for (int i = 0; i < 5; i++) p.broad(30 + i * 100, 214, 44 + r.nextInt(14), mix(p.pal.leafLight, c(250, 196, 210), 0.3));
                p.crane(120, 160, 1.6); p.reeds(60, 240, 20); p.reeds(440, 250, 24);
                break;
            }
            case "library": {                        // shelves, a lamp, a ladder
                p.pal.skyTop = c(46, 38, 32); p.pal.skyBot = c(130, 98, 70);
                p.gradientRect(0, DH, c(40, 32, 28), c(124, 90, 62));
                for (int row = 0; row < 5; row++) for (int col = 0; col < 2; col++) {
                    double y = 40 + row * 38; p.washRect(30 + col * 330, y, 150, 4, c(84, 62, 44), 3, 0.95, 0.03);
                    for (int b = 0; b < 14; b++) { double x = 34 + col * 330 + b * 10.4; p.washRect(x, y - 24 + r.nextInt(5), 8, 24, c(90 + r.nextInt(110), 50 + r.nextInt(60), 40 + r.nextInt(50)), 2, 0.9, 0.04); }
                }
                p.washRect(210, 200, 92, 8, c(120, 88, 60), 4, 0.96, 0.03);       // a reading table
                p.stroke(236, 200, 236, 250, 5, c(90, 66, 46), 0.9); p.stroke(276, 200, 276, 250, 5, c(90, 66, 46), 0.9);
                p.washCircle(256, 190, 24, 14, c(252, 214, 130), 5, 0.55, 0.15);   // a lamp
                p.washRect(250, 190, 12, 10, c(252, 232, 170), 3, 0.95, 0.05);
                p.stroke(410, 60, 380, 250, 4, c(90, 66, 46), 0.9);                 // the ladder
                break;
            }
            case "forge": {                          // a forge glowing in the dark
                p.pal.skyTop = c(28, 24, 26); p.pal.skyBot = c(100, 52, 36);
                p.gradientRect(0, DH, c(24, 20, 22), c(70, 44, 34));
                p.washRect(120, 120, 130, 100, c(70, 60, 58), 5, 0.96, 0.04);
                p.washRect(150, 160, 70, 40, c(252, 140, 50), 6, 0.95, 0.08);       // the fire
                p.lightRays(185, 180, 4, 0.3);
                p.washRect(310, 196, 80, 18, c(80, 82, 86), 5, 0.97, 0.03);           // the anvil
                p.washRect(334, 214, 32, 30, c(70, 60, 54), 4, 0.96, 0.03);
                p.wash(poly(300, 196, 396, 196, 380, 184, 320, 184), c(96, 98, 104), 4, 0.97, 0.03);
                for (int i = 0; i < 6; i++) { double x = 40 + i * 24; p.stroke(x, 40, x, 90 + r.nextInt(20), 2, c(110, 100, 94), 0.85); }
                p.embers(90);
                p.figure(280, 250, 1.4);
                break;
            }
            case "mine": case "cave": {              // timber props, a lamp, a vein of ore
                boolean mine = key.equals("mine");
                p.pal.skyTop = c(30, 28, 34); p.pal.skyBot = c(76, 68, 70);
                p.gradientRect(0, DH, c(26, 24, 30), c(84, 76, 78));
                p.wash(poly(-10, -10, 522, -10, 522, 40, 460, 70, 400, 50, 330, 84, 256, 60, 170, 90, 100, 64, 40, 80, -10, 50), c(52, 46, 48), 6, 0.97, 0.06);
                p.wash(poly(-10, 298, 522, 298, 522, 250, 450, 236, 360, 258, 260, 244, 160, 262, 70, 240, -10, 256), c(50, 44, 46), 6, 0.97, 0.06);
                if (mine) { for (double x : new double[]{110, 256, 402}) { p.stroke(x - 40, 240, x - 40, 70, 8, c(110, 84, 60), 0.95); p.stroke(x + 40, 240, x + 40, 70, 8, c(110, 84, 60), 0.95); p.stroke(x - 48, 74, x + 48, 74, 9, c(110, 84, 60), 0.95); } p.stroke(0, 252, DW, 252, 2, c(150, 150, 150), 0.6); }
                for (int i = 0; i < 10; i++) { double x = 30 + r.nextInt(450), y = 110 + r.nextInt(120); p.washCircle(x, y, 4 + r.nextInt(5), 3 + r.nextInt(4), i % 2 == 0 ? c(120, 200, 210) : c(230, 190, 90), 3, 0.9, 0.08); }
                lantern(p, 256, 160, 2.0, true);
                p.lightRays(256, 150, 3, 0.18);
                p.figure(330, 246, 1.2);
                break;
            }
            case "forest": {
                p.pal.leafDark = c(28, 66, 46); p.sky(120);
                for (int row = 0; row < 3; row++) for (int i = 0; i < 14; i++) p.pine(8 + i * 38 + r.nextInt(14), 160 + row * 40, 60 + row * 22 + r.nextInt(22), mix(p.pal.leafDark, p.pal.leafLight, row * 0.25));
                p.land(p.ridge(250, 4, 0.8, 3), DH + 10, mix(p.pal.ground, p.pal.leafDark, 0.3), 0.96, false, p.pal.far);
                p.lightRays(300, 0, 4, 0.2); p.grass(240, 286, 100, p.pal.leafDark, p.pal.leafLight); p.flowers(24); p.spores(20);
                p.figure(220, 266, 1.4);
                break;
            }
            case "meadow": {
                p.sky(150); p.sun(400, 96, 24); p.mountainLayers(160, 2, 70, false);
                p.land(p.ridge(190, 8, 0.8, 3), DH + 10, p.pal.ground, 0.96, false, p.pal.far);
                p.land(p.ridge(226, 7, 0.9, 3), DH + 10, mix(p.pal.ground, p.pal.leafLight, 0.5), 0.96, false, p.pal.far);
                p.broad(150, 214, 70, p.pal.leafLight);
                p.grass(200, 286, 160, p.pal.leafDark, p.pal.leafLight); p.flowers(70); p.birds(330, 70, 4, 1.2);
                break;
            }
            case "water": {                          // a river, a ferry or a bridge
                p.sky(150); p.sun(110, 96, 24); p.mountainLayers(160, 2, 70, false);
                p.waterBody(196, DH + 4);
                p.washRect(0, 192, DW, 8, c(150, 128, 100), 4, 0.9, 0.04);
                for (int i = 0; i < 30; i++) { double a = Math.PI * i / 29; p.stroke(90 + Math.cos(a + Math.PI) * 190, 218 - Math.sin(a) * 40, 90 + Math.cos(a + Math.PI + 0.1) * 190, 218 - Math.sin(a + 0.1) * 40, 4, c(160, 140, 120), 0.8); }
                p.boat(370, 252, 1.4);
                p.reeds(40, 274, 28); p.reeds(470, 280, 30); p.birds(200, 70, 5, 1.2);
                break;
            }
            case "marsh": {
                grey(p); p.sky(150); p.mountainLayers(170, 2, 40, false);
                p.waterBody(196, DH + 4);
                for (int i = 0; i < 12; i++) p.reeds(20 + r.nextInt(470), 210 + r.nextInt(74), 20 + r.nextInt(22));
                for (int i = 0; i < 3; i++) p.deadTree(60 + i * 180 + r.nextInt(40), 220 + r.nextInt(20), 44 + r.nextInt(24));
                p.boat(300, 262, 1.1);
                p.mist(210, 40, 0.5); p.spores(18);
                break;
            }
            case "cliff": {                          // a narrow pass between cliffs
                p.sky(130); p.mountainLayers(150, 2, 100, true);
                p.wash(poly(-10, 40, 190, 70, 230, 290, -10, 290), p.pal.rock, 8, 0.97, 0.06); p.wash(poly(522, 20, 330, 74, 290, 290, 522, 290), mix(p.pal.rock, p.pal.rockDark, 0.3), 8, 0.97, 0.06);
                for (int i = 0; i < 24; i++) { double x = r.nextInt(190), y = 60 + r.nextInt(220); p.stroke(x, y, x + r.nextGaussian() * 6, y + 20 + r.nextInt(30), 3, p.pal.rockDark, 0.4); double x2 = 330 + r.nextInt(190); p.stroke(x2, y, x2 + r.nextGaussian() * 6, y + 20 + r.nextInt(30), 3, p.pal.rockDark, 0.4); }
                p.wash(poly(236, 130, 276, 130, 360, 290, 150, 290), mix(p.pal.sand, p.pal.rock, 0.4), 4, 0.9, 0.05);
                p.figure(256, 230, 1.5); p.deadTree(190, 250, 40);
                p.mist(180, 40, 0.35);
                break;
            }
            case "wastes": {
                grey(p); p.pal.skyBot = c(222, 198, 160); p.sky(150);
                p.dunes(172, 4, c(226, 204, 160), c(150, 120, 86));
                p.deadTree(90, 230, 50); p.deadTree(380, 244, 40); p.deadTree(440, 226, 30);
                for (int i = 0; i < 40; i++) { double x = r.nextInt(DW), y = 190 + r.nextInt(90); p.stroke(x, y, x + 20 + r.nextInt(26), y + r.nextGaussian(), 1.4, c(255, 244, 214), 0.25); }
                p.figure(250, 258, 1.4);
                break;
            }
            case "shrine": {                         // a small shrine, incense, old trees (also a grave)
                p.sky(150); p.mountainLayers(160, 2, 60, false);
                p.land(p.ridge(196, 6, 0.8, 3), DH + 10, p.pal.ground, 0.96, false, p.pal.far);
                for (int i = 0; i < 4; i++) p.pine(30 + i * 130 + r.nextInt(30), 224, 80 + r.nextInt(30), p.pal.leafDark);
                p.washRect(220, 196, 72, 44, c(208, 194, 168), 4, 0.96, 0.03);
                p.wash(poly(206, 200, 240, 172, 272, 172, 306, 200), c(70, 62, 60), 4, 0.97, 0.04);
                p.washRect(244, 210, 24, 30, c(40, 34, 32), 3, 0.97, 0.03); p.washCircle(256, 206, 3, 3, c(240, 150, 60), 3, 0.95, 0.05);
                smoke(p, 256, 190); p.washRect(250, 244, 12, 10, c(150, 144, 136), 3, 0.95, 0.03);
                for (int i = 0; i < 5; i++) p.washRect(60 + i * 24, 254 + (i % 2) * 6, 8, 16, c(150, 148, 140), 3, 0.95, 0.04);       // stones of the dead
                p.figure(330, 262, 1.2); p.grass(240, 286, 80, p.pal.leafDark, p.pal.leafLight);
                break;
            }
            case "tower": {                          // a tall tower on a headland, with a light
                dusk(p); p.sky(170); p.waterBody(210, DH + 4); p.mountainLayers(186, 1, 40, false);
                p.wash(poly(120, 214, 200, 190, 300, 190, 420, 216), c(96, 92, 88), 5, 0.96, 0.05);
                p.wash(poly(226, 190, 232, 60, 280, 60, 286, 190), c(208, 196, 174), 5, 0.97, 0.03);
                p.washRect(224, 40, 64, 22, c(70, 66, 66), 4, 0.97, 0.03); p.washRect(238, 22, 36, 20, c(254, 230, 150), 5, 0.95, 0.05);
                p.lightRays(256, 32, 4, 0.35); p.birds(110, 90, 5, 1.2);
                for (int i = 0; i < 6; i++) p.stroke(40 + r.nextInt(430), 224 + r.nextInt(50), 90 + r.nextInt(430), 224 + r.nextInt(50), 1.5, c(250, 250, 248), 0.3);
                break;
            }
            case "camp": {                           // tents, a fire, banners
                dusk(p); p.sky(170); p.mountainLayers(176, 2, 60, false);
                p.land(p.ridge(212, 6, 0.8, 3), DH + 10, mix(p.pal.ground, c(80, 70, 64), 0.4), 0.96, false, p.pal.far);
                for (int i = 0; i < 5; i++) { double x = 60 + i * 96, y = 232 + (i % 2) * 22; p.wash(poly(x - 34, y, x, y - 40, x + 34, y), i % 2 == 0 ? c(210, 190, 150) : c(180, 120, 90), 4, 0.96, 0.05); p.washRect(x - 6, y - 14, 12, 14, c(40, 34, 32), 2, 0.9, 0.03); }
                p.washCircle(256, 262, 18, 8, c(252, 150, 60), 5, 0.8, 0.15); p.embers(40);
                for (double bx : new double[]{150, 360}) { p.stroke(bx, 220, bx, 150, 1.6, c(70, 56, 44), 0.9); p.wash(poly(bx, 152, bx + 22, 160, bx, 170), c(190, 52, 44), 3, 0.95, 0.06); }
                p.figure(290, 264, 1.2);
                break;
            }
            case "sky": {                            // above the clouds
                dawn(p); p.sky(260); p.gradientRect(120, DH, alpha(c(255, 255, 255), 0), c(236, 238, 244));
                for (int i = 0; i < 9; i++) p.washCircle(20 + i * 62, 220 + r.nextInt(50), 70, 14, c(255, 255, 255), 4, 0.8, 0.2);
                for (int i = 0; i < 4; i++) p.floatingIslet(60 + r.nextInt(380), 70 + r.nextInt(100), 40 + r.nextInt(50));
                p.crane(250, 150, 2.2); p.crane(300, 170, 1.6); p.crane(210, 176, 1.4);
                p.lightRays(380, 0, 4, 0.25);
                break;
            }
            default: {                               // road
                p.sky(150); p.sun(392, 100, 24); p.mountainLayers(160, 3, 80, false);
                p.land(p.ridge(196, 8, 0.8, 3), DH + 10, p.pal.ground, 0.96, false, p.pal.far);
                p.wash(poly(236, 196, 276, 196, 340, 290, 160, 290), mix(p.pal.sand, p.pal.ground, 0.3), 4, 0.9, 0.05);
                p.grass(210, 286, 90, p.pal.leafDark, p.pal.leafLight);
                p.washRect(330, 236, 8, 18, c(150, 144, 136), 3, 0.95, 0.04);   // a milestone
                p.figure(262, 252, 1.4); p.birds(120, 70, 4, 1.2);
            }
        }
    }

    // ------------------------------------------------------------------ the faces
    /** Names of the people who speak in these lines, in order, without "You" and without repeats (at most `max`). */
    static List<String> speakers(List<String> lines, int max) {
        List<String> out = new ArrayList<>();
        if (lines == null) return out;
        for (String l : lines) {
            if (!ChapText.speaker(l)) continue;
            String who = ChapText.who(l);
            if (who == null || who.equals("You") || who.isEmpty() || out.contains(who)) continue;
            out.add(who);
            if (out.size() >= max) break;
        }
        return out;
    }

    /** The portraits of the people of a story who speak in the scene (painted on the calling thread; the same portrait cache as the people's cards). Parallel lists: names and images. */
    static void faces(Starts.State st, List<String> lines, List<String> namesOut, List<BufferedImage> imagesOut) {
        if (st == null || st.npcs == null) return;
        for (String who : speakers(lines, 3)) {
            Character found = null;
            for (int id : new ArrayList<>(st.npcs.values())) {
                Character n = Character.charById(id);
                if (n != null && n.name != null && (n.name.equalsIgnoreCase(who) || (n.name + " " + n.lastName).equalsIgnoreCase(who))) { found = n; break; }
            }
            if (found == null) continue;
            try { imagesOut.add(ArtMore.portraitCached(found, 84, 84)); namesOut.add(found.name); } catch (RuntimeException e) { }
        }
    }

    // ------------------------------------------------------------------ handing a picture to the next scene window
    private static String pendingKey = null;
    private static List<String> pendingNames = null;
    private static List<BufferedImage> pendingFaces = null;

    /** Set just before a scene is shown (on the game thread): the picture's key and the faces. The scene window takes them. */
    static synchronized void next(String key, List<String> names, List<BufferedImage> faces) { pendingKey = key; pendingNames = names; pendingFaces = faces; }

    /** Takes what `next` set (once): {key, names, faces}; nulls when nothing was set. */
    static synchronized Object[] take() {
        Object[] o = {pendingKey, pendingNames, pendingFaces};
        pendingKey = null; pendingNames = null; pendingFaces = null;
        return o;
    }
}
