package Game;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Procedural painterly place pictures. Every call paints a NEW picture from a seed: layered watercolour washes (polygons whose edges are
 *  recursively deformed and stacked at low opacity, so pigment pools at the edges), dry-brush strokes, atmospheric perspective, a touch of
 *  blur, then paper grain and a small red seal. The noun picks the scene (mountains, forest, lake, cave...), the biome picks the palette, the
 *  seed picks the composition, so two "forests" never look the same. Thread-safe (all state is per-call). `quality` 1.0 = full, lower = fewer
 *  washes for small thumbnails. PlaceImages uses this whenever there is no hand-made picture for a place.
 *  Command line:  java Game.ArtGen <outDir> [noun biome seed]...  (or just <outDir>: a sample of every scene) */
public class ArtGen {
    static final int DW = 512, DH = 288; // design coordinates; the result is scaled to the requested size

    // ------------------------------------------------------------------ noun -> scene
    static final Map<String, String> KIND = new LinkedHashMap<>();
    private static void k(String kind, String... nouns) { for (String n : nouns) KIND.put(n, kind); }
    static {
        k("mountains", "mountains", "mounts", "crags", "cliffs", "plateaus");
        k("hills", "hills", "badlands", "barrens", "mesas", "peninsulas", "point", "points");
        k("forest", "forests", "woods", "groves", "rainforests", "jungles", "glades");
        k("meadow", "meadows", "grasslands", "fields", "pastures", "shrublands", "beach");
        k("lake", "lakes", "ponds", "lagoons", "lake_beds", "bays", "coves", "basins");
        k("river", "rivers", "streams", "brooks", "creeks", "bayous");
        k("falls", "rapids", "gulches", "ravines", "canyons", "crevasses");
        k("cave", "caves", "caverns", "grottoes", "burrows", "pits", "underwater_caves", "underwater_caverns", "underwater_trenches");
        k("arch", "stone_arches", "sea_arches", "ice_arches", "bridges", "giant_footprints");
        k("desert", "deserts", "dust_bowls", "oases");
        k("islands", "islands", "archipelagos", "atolls", "barrier_islands", "reefs", "coral_reefs", "coral_forests");
        k("sea", "oceans", "coasts", "shorelines", "tide_pools", "fjords");
        k("ice", "glaciers");
        k("marsh", "marshes", "bogs", "mangroves");
        k("volcano", "volcanoes", "calderas");
        k("spring", "hot_springs", "springs", "geysers", "hydrothermal_vents");
        k("falls", "waterfalls", "gorges", "slot_canyons", "frozen_waterfalls", "ice_falls");
        k("river", "valleys", "dry_riverbeds", "deltas");
        k("mountains", "ridges", "peaks", "summits", "passes");
        k("pillars", "stone_forests", "hoodoos", "kopjes");
        k("terraces", "terraces");
        k("mounds", "termite_mounds");
        k("hills", "highlands", "rock_gardens", "headlands");
        k("cave", "sinkholes", "ice_caves", "sea_caves", "frost_hollows", "trenches");
        k("forest", "bamboo_forests", "pine_forests", "cherry_blossom_groves", "willow_groves", "orchards", "acacia_groves", "snow_groves", "rime_forests");
        k("meadow", "prairies", "steppes", "beaches");
        k("marsh", "moors", "wetlands", "salt_marshes", "muskegs");
        k("lake", "lotus_ponds", "frozen_lakes", "watering_holes", "oxbow_lakes", "sunken_gardens");
        k("arch", "natural_bridges", "stone_bridges");
        k("menhirs", "standing_stones");
        k("desert", "dunes", "salt_flats", "sand_seas", "mirage_wastes", "buried_ruins", "salt_pans");
        k("sea", "kelp_forests", "cloud_seas", "tidal_flats", "driftwood_shores");
        k("whirlpool", "whirlpools");
        k("islands", "sea_stacks", "shoals");
        k("ice", "snowfields", "ice_fields", "permafrost_plains", "aurora_plains");
    }

    static String slug(String noun) { return noun.toLowerCase().replace(' ', '_'); }

    /** What the place's NAME says besides its noun: a number ("Eight Giant Footprints" paints eight), the area level (spirit glow, floating
     *  islets), and mood words from the adjectives (dark/night: moon and dead trees; fire: embers; ice: snow; holy: light rays and cranes; ...). */
    public static class Opts {
        public int count = 0;          // 0 = the scene's own choice
        public int level = 1;          // area level 1-100
        public java.util.Set<String> moods = java.util.Collections.emptySet();
        public boolean river = false;  // a river runs across the tile
        public int season = -1;        // 0-3 = spring..winter finishing touches, -1 = none
        public int weather = 0;        // 0 clear, 1 rain, 2 fog
        public int settlement = 0;     // 1 town, 2 city, 3 the capital Originis
        public boolean sect = false;   // a sect hall stands here
        public Opts() { }
        public Opts(int count, int level, java.util.Collection<String> moods) { this.count = count; this.level = level; this.moods = new java.util.HashSet<>(moods); }
        public String key() { return count + "|" + (level / 20) + "|" + new java.util.TreeSet<>(moods) + "|" + river + season + weather + settlement + sect; }
    }

    /** "Eight" -> 8. Unknown words give 0 (the scene decides). */
    public static int countOf(String word) {
        if (word == null) return 0;
        switch (word.trim().toLowerCase().replace(' ', '-')) {
            case "two": return 2; case "three": return 3; case "four": return 4; case "five": return 5; case "six": return 6; case "seven": return 7;
            case "eight": return 8; case "nine": return 9; case "ten": return 10; case "eleven": return 11; case "twelve": return 12; case "thirteen": return 13;
            case "sixteen": return 16; case "eighteen": return 18; case "twenty-one": return 21; case "twenty-four": return 24; case "thirty-three": return 33;
            case "thirty-six": return 36; case "forty-nine": return 49; case "sixty-nine": return 69; case "seventy-two": return 72; case "eighty-one": return 81;
            case "ninety-nine": return 99; case "hundred": case "one-hundred": return 100; case "thousand": case "one-thousand": return 1000;
            case "three-thousand": return 3000; case "ten-thousand": return 10000;
            default: return 0;
        }
    }

    // ------------------------------------------------------------------ palette
    static class Pal {
        Color skyTop, skyBot, sun, far, near, ground, leafLight, leafDark, trunk, water, waterDeep, rock, rockDark, mist, sand;
    }

    static Color c(int r, int g, int b) { return new Color(r, g, b); }
    static Color mix(Color a, Color b, double t) { t = Math.max(0, Math.min(1, t)); return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * t), (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t), (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t)); }
    static Color alpha(Color a, double al) { return new Color(a.getRed(), a.getGreen(), a.getBlue(), (int) Math.max(0, Math.min(255, al * 255))); }

    static Pal palette(String biome, Random r) {
        String b = biome == null ? "" : biome.toLowerCase();
        Pal p = new Pal();
        // neutral daylight base: misty blue-green ink landscape
        p.skyTop = c(196, 214, 222); p.skyBot = c(240, 232, 212); p.sun = c(250, 222, 176); p.far = c(164, 186, 190); p.near = c(52, 82, 82);
        p.ground = c(118, 146, 100); p.leafLight = c(128, 168, 110); p.leafDark = c(40, 84, 62); p.trunk = c(58, 50, 44);
        p.water = c(120, 168, 178); p.waterDeep = c(56, 100, 118); p.rock = c(112, 120, 116); p.rockDark = c(52, 60, 62); p.mist = c(244, 240, 228); p.sand = c(226, 208, 164);
        if (b.contains("desert") && !b.contains("frozen")) { p.skyTop = c(226, 214, 190); p.skyBot = c(250, 232, 196); p.far = c(206, 176, 136); p.near = c(150, 104, 72); p.ground = c(214, 178, 120); p.leafLight = c(156, 158, 90); p.leafDark = c(92, 104, 60); p.rock = c(160, 120, 92); p.rockDark = c(96, 66, 52); p.sand = c(232, 204, 150); }
        else if (b.contains("tundra") || b.contains("frozen")) { p.skyTop = c(196, 212, 230); p.skyBot = c(238, 242, 248); p.far = c(176, 196, 216); p.near = c(96, 122, 150); p.ground = c(222, 232, 240); p.leafLight = c(150, 176, 168); p.leafDark = c(56, 88, 88); p.water = c(150, 190, 214); p.rock = c(130, 142, 154); p.sand = c(228, 234, 238); }
        else if (b.contains("taiga")) { p.skyTop = c(190, 204, 210); p.skyBot = c(226, 228, 220); p.far = c(150, 170, 172); p.near = c(34, 66, 62); p.ground = c(96, 122, 96); p.leafLight = c(92, 130, 100); p.leafDark = c(26, 62, 56); }
        else if (b.contains("savanna")) { p.skyTop = c(214, 222, 210); p.skyBot = c(250, 234, 198); p.far = c(190, 184, 140); p.near = c(108, 110, 70); p.ground = c(196, 176, 98); p.leafLight = c(164, 160, 80); p.leafDark = c(84, 98, 52); p.sand = c(222, 196, 136); }
        else if (b.contains("forest")) { p.ground = c(92, 132, 84); p.leafLight = c(110, 156, 92); p.leafDark = c(30, 74, 52); p.near = c(40, 74, 66); }
        else if (b.contains("grass")) { p.ground = c(132, 168, 100); p.leafLight = c(150, 184, 104); p.leafDark = c(56, 104, 66); }
        else if (b.contains("beach")) { p.ground = c(228, 210, 166); p.leafLight = c(130, 170, 104); p.water = c(132, 192, 198); p.sand = c(234, 218, 176); }
        else if (b.contains("ocean")) { p.water = c(104, 158, 184); p.waterDeep = c(40, 88, 122); p.skyTop = c(186, 208, 226); }
        // every picture gets its own small shift so no two are the same colour
        double dh = (r.nextDouble() - 0.5) * 0.04, ds = 1 + (r.nextDouble() - 0.5) * 0.22, dv = 1 + (r.nextDouble() - 0.5) * 0.10;
        p.skyTop = shift(p.skyTop, dh, ds, dv); p.skyBot = shift(p.skyBot, dh, ds, dv); p.far = shift(p.far, dh, ds, dv); p.near = shift(p.near, dh, ds, dv); p.ground = shift(p.ground, dh, ds, dv);
        p.leafLight = shift(p.leafLight, dh, ds, dv); p.leafDark = shift(p.leafDark, dh, ds, dv); p.rock = shift(p.rock, dh * 0.5, ds, dv); p.sand = shift(p.sand, dh * 0.5, ds, dv); p.sun = shift(p.sun, dh, ds, dv);
        return p;
    }

    static Color shift(Color col, double dh, double ds, double dv) {
        float[] h = Color.RGBtoHSB(col.getRed(), col.getGreen(), col.getBlue(), null);
        return Color.getHSBColor((float) ((h[0] + dh + 1) % 1.0), (float) Math.max(0, Math.min(1, h[1] * ds)), (float) Math.max(0, Math.min(1, h[2] * dv)));
    }

    // ------------------------------------------------------------------ painter: watercolour primitives
    static class Painter {
        final Graphics2D g; final Random r; final double q; final Pal pal; final String noun; final Opts o;
        final List<int[]> ridges = new ArrayList<>(); // every ridge line painted so far (far to near): props stand on them
        Painter(Graphics2D g, Random r, double q, Pal pal, String noun, Opts o) { this.g = g; this.r = r; this.q = q; this.pal = pal; this.noun = noun; this.o = o == null ? new Opts() : o; }

        List<double[]> deform(List<double[]> p, int depth, double var) {
            List<double[]> cur = p;
            for (int d = 0; d < depth; d++) {
                List<double[]> next = new ArrayList<>(cur.size() * 2);
                for (int i = 0; i < cur.size(); i++) {
                    double[] a = cur.get(i), b = cur.get((i + 1) % cur.size());
                    double len = Math.hypot(b[0] - a[0], b[1] - a[1]);
                    next.add(a);
                    next.add(new double[]{(a[0] + b[0]) / 2 + r.nextGaussian() * len * var, (a[1] + b[1]) / 2 + r.nextGaussian() * len * var});
                }
                cur = next;
            }
            return cur;
        }

        Path2D path(List<double[]> p) {
            Path2D.Double s = new Path2D.Double();
            for (int i = 0; i < p.size(); i++) { double[] v = p.get(i); if (i == 0) s.moveTo(v[0], v[1]); else s.lineTo(v[0], v[1]); }
            s.closePath();
            return s;
        }

        /** Stacks `layers` slightly different deformations of the polygon at low opacity: soft interior, darker pooled edges. */
        void wash(List<double[]> base, Color col, int layers, double total, double var) {
            int n = Math.max(2, (int) Math.round(layers * q));
            List<double[]> master = deform(base, 2, var);
            for (int i = 0; i < n; i++) {
                List<double[]> p = deform(master, q < 0.6 ? 1 : 2, var * 0.7);
                double a = (1 - Math.pow(1 - Math.min(0.97, total), 1.0 / n)) * (0.7 + r.nextDouble() * 0.6); // `total` = the coverage the stack should reach
                g.setColor(alpha(col, Math.min(0.95, a)));
                g.fill(path(p));
            }
        }

        void washCircle(double cx, double cy, double rx, double ry, Color col, int layers, double total, double var) {
            List<double[]> p = new ArrayList<>();
            int pts = 9;
            for (int i = 0; i < pts; i++) { double a = Math.PI * 2 * i / pts; p.add(new double[]{cx + Math.cos(a) * rx * (0.88 + r.nextDouble() * 0.24), cy + Math.sin(a) * ry * (0.88 + r.nextDouble() * 0.24)}); }
            wash(p, col, layers, total, var);
        }

        void washRect(double x, double y, double w, double h, Color col, int layers, double total, double var) {
            List<double[]> p = new ArrayList<>();
            p.add(new double[]{x, y}); p.add(new double[]{x + w / 2, y}); p.add(new double[]{x + w, y}); p.add(new double[]{x + w, y + h / 2});
            p.add(new double[]{x + w, y + h}); p.add(new double[]{x + w / 2, y + h}); p.add(new double[]{x, y + h}); p.add(new double[]{x, y + h / 2});
            wash(p, col, layers, total, var);
        }

        /** A dry-brush stroke: several thin bristle lines with their own opacity, tapering towards the end. */
        void stroke(double x1, double y1, double x2, double y2, double w, Color col, double a) {
            int bristles = Math.max(2, (int) (w / 1.6 * (0.5 + q * 0.5)));
            double nx = -(y2 - y1), ny = x2 - x1, len = Math.hypot(nx, ny);
            if (len < 1e-6) return;
            nx /= len; ny /= len;
            for (int i = 0; i < bristles; i++) {
                double off = (i - bristles / 2.0) * (w / bristles);
                double jx = r.nextGaussian() * 0.6, jy = r.nextGaussian() * 0.6;
                g.setColor(alpha(col, a * (0.35 + r.nextDouble() * 0.65)));
                g.setStroke(new BasicStroke((float) Math.max(0.6, w / bristles * 1.6), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Line2D.Double(x1 + nx * off + jx, y1 + ny * off + jy, x2 + nx * off * 0.4 + jx, y2 + ny * off * 0.4 + jy));
            }
        }

        void gradientRect(double y0, double y1, Color top, Color bot) { g.setPaint(new GradientPaint(0, (float) y0, top, 0, (float) y1, bot)); g.fill(new Rectangle2D.Double(-4, y0, DW + 8, y1 - y0)); }

        // -------------------------------------------------------------- composite pieces
        void sky(double horizon) {
            gradientRect(0, horizon, pal.skyTop, pal.skyBot);
            int clouds = 4 + r.nextInt(4);
            for (int i = 0; i < clouds; i++) { double x = r.nextDouble() * DW, y = 20 + r.nextDouble() * horizon * 0.55, w = 60 + r.nextDouble() * 140; washCircle(x, y, w, 7 + r.nextDouble() * 9, alpha(pal.mist, 1), 5, 0.42, 0.22); }
        }

        void sun(double x, double y, double rad) {
            for (int i = 5; i >= 1; i--) { g.setColor(alpha(pal.sun, 0.05)); g.fill(new Ellipse2D.Double(x - rad - i * 9, y - rad - i * 9, 2 * (rad + i * 9), 2 * (rad + i * 9))); }
            washCircle(x, y, rad, rad, pal.sun, 6, 0.95, 0.06);
        }

        int[] ridge(double base, double amp, double rough, int octaves) {
            int[] out = new int[DW + 1];
            double[] ph = new double[octaves], fr = new double[octaves];
            for (int o = 0; o < octaves; o++) { ph[o] = r.nextDouble() * 6.28; fr[o] = (1 + o * 1.8 + r.nextDouble()) * rough; }
            for (int x = 0; x <= DW; x++) { double y = 0, a = amp; for (int o = 0; o < octaves; o++) { y += Math.sin(x / (double) DW * 6.283 * fr[o] + ph[o]) * a; a *= 0.5; } out[x] = (int) (base + y); }
            return out;
        }

        int[] peaks(double base, double height, int n) {
            int[] out = new int[DW + 1];
            Arrays.fill(out, (int) base);
            for (int i = 0; i < n; i++) {
                double cx = r.nextDouble() * DW, hw = 50 + r.nextDouble() * 100, hh = height * (0.45 + r.nextDouble() * 0.55);
                for (int x = 0; x <= DW; x++) { double d = Math.abs(x - cx) / hw; if (d < 1) out[x] = Math.min(out[x], (int) (base - hh * Math.pow(1 - d, 1.3) * (0.93 + 0.07 * Math.sin(x * 0.6 + i)))); }
            }
            return out;
        }

        /** Washes the land under a ridge line and dry-brushes crags along it. */
        void land(int[] ridge, double bottom, Color col, double total, boolean crags, Color cragCol) {
            ridges.add(ridge);
            List<double[]> p = new ArrayList<>();
            p.add(new double[]{-6, bottom});
            for (int x = -6; x <= DW + 6; x += 14) p.add(new double[]{x, ridge[Math.max(0, Math.min(DW, x))]});
            p.add(new double[]{DW + 6, bottom});
            wash(p, col, 12, total, 0.05);
            if (crags) for (int i = 0; i < (int) (26 * (0.4 + q * 0.6)); i++) {
                int x = 8 + r.nextInt(DW - 16); int y = ridge[x];
                stroke(x, y + 2, x + r.nextGaussian() * 6, y + 10 + r.nextDouble() * 34, 2 + r.nextDouble() * 2.5, cragCol, 0.35);
            }
        }

        void mist(double y, double h, double a) {
            for (int i = 0; i < 3; i++) washRect(-10, y - h / 2 + r.nextDouble() * h * 0.3, DW + 20, h * (0.4 + r.nextDouble() * 0.4), pal.mist, 4, a, 0.18);
        }

        void mountainLayers(double base, int layers, double height, boolean snow) { mountainLayers(base, layers, height, snow, 0); }

        void mountainLayers(double base, int layers, double height, boolean snow, int peaksN) {
            for (int l = 0; l < layers; l++) {
                double t = layers == 1 ? 1 : l / (double) (layers - 1);
                int[] rg = peaks(base + l * 15, height * (1 - t * 0.35), peaksN > 0 ? peaksN : 3 + r.nextInt(3));
                for (int x = 1; x < DW; x++) rg[x] += (int) (Math.sin(x * 0.29 + l * 2) * 2 + r.nextGaussian() * 1.2);
                Color col = mix(pal.far, pal.near, Math.pow(t, 0.9));
                land(rg, DH + 12, col, 0.92, true, mix(col, pal.rockDark, 0.6));
                for (int x = 4; x < DW - 6; x += 7) if (r.nextInt(3) != 0) stroke(x, rg[x] + 1, x + 7, rg[Math.min(DW, x + 7)] + 1, 2.2, alpha(mix(pal.sun, c(255, 255, 255), 0.5), 1), 0.28); // rim light
                if (snow) for (int i = 0; i < 26; i++) { int x = 6 + r.nextInt(DW - 12); int y = rg[x]; if (y < base - height * 0.45) stroke(x - 5, y + 4, x + 6, y + 10, 4, alpha(c(250, 250, 252), 1), 0.6); }
                mist(base + l * 15 + 8, 26, 0.3 - t * 0.12);
            }
        }

        void pine(double x, double y, double h, Color col) {
            stroke(x, y, x + r.nextGaussian() * 1.5, y - h, Math.max(1.6, h / 14), pal.trunk, 0.8);
            int tiers = 4 + (h > 50 ? 1 : 0);
            for (int i = 0; i < tiers; i++) {
                double t = (i + 0.4) / tiers, ty = y - h * (0.15 + t * 0.78), wd = h * (0.42 - t * 0.3);
                List<double[]> tri = new ArrayList<>();
                tri.add(new double[]{x - wd, ty + h * 0.13}); tri.add(new double[]{x, ty - h * 0.14}); tri.add(new double[]{x + wd, ty + h * 0.13}); tri.add(new double[]{x, ty + h * 0.07});
                wash(tri, jit(col, 0.06), 3, 0.92, 0.12);
            }
        }

        void broad(double x, double y, double h, Color leaf) {
            stroke(x, y, x + r.nextGaussian() * 2, y - h * 0.55, Math.max(2, h / 11), pal.trunk, 0.85);
            int blobs = 5 + r.nextInt(3);
            for (int i = 0; i < blobs; i++) {
                double bx = x + (r.nextDouble() - 0.5) * h * 0.72, by = y - h * (0.55 + r.nextDouble() * 0.42), rr = h * (0.15 + r.nextDouble() * 0.1);
                washCircle(bx, by, rr, rr * 0.82, jit(leaf, 0.07), 3, 0.92, 0.14);
            }
            washCircle(x - h * 0.1, y - h * 0.86, h * 0.1, h * 0.08, pal.leafLight, 2, 0.6, 0.15);
        }

        Color jit(Color col, double amt) { return shift(col, (r.nextDouble() - 0.5) * amt * 0.4, 1 + (r.nextDouble() - 0.5) * amt * 2, 1 + (r.nextDouble() - 0.5) * amt * 2); }

        void waterBody(double y0, double y1) {
            gradientRect(y0, y1, mix(pal.water, pal.skyBot, 0.35), pal.waterDeep);
            for (int i = 0; i < (int) (40 + 60 * q); i++) {
                double t = r.nextDouble(), y = y0 + (y1 - y0) * t * t, len = 10 + t * 80, x = r.nextDouble() * DW;
                stroke(x, y, x + len, y + r.nextGaussian(), 1.5 + t * 2, r.nextInt(3) == 0 ? pal.waterDeep : c(252, 252, 250), 0.22 + t * 0.2);
            }
        }

        void reeds(double x, double y, double h) {
            for (int i = 0; i < 3; i++) { double lean = (r.nextDouble() - 0.5) * 16; stroke(x + i * 3, y, x + i * 3 + lean, y - h, 2, mix(pal.leafDark, pal.trunk, 0.4), 0.8); }
            washCircle(x + 3, y - h, 2.5, 7, pal.trunk, 2, 0.8, 0.1);
        }

        void rock(List<double[]> poly, Color col) {
            wash(poly, col, 10, 0.95, 0.06);
            Rectangle2D b = path(poly).getBounds2D();
            Shape old = g.getClip(); g.setClip(path(poly));
            for (int i = 0; i < (int) (b.getWidth() / 6 * (0.4 + q)); i++) { double x = b.getX() + r.nextDouble() * b.getWidth(), y = b.getY() + r.nextDouble() * b.getHeight(); stroke(x, y, x + r.nextGaussian() * 6, y + 10 + r.nextDouble() * 26, 2.5, pal.rockDark, 0.33); }
            g.setClip(old);
        }

        void grass(double y0, double y1, int count, Color a, Color b) {
            for (int i = 0; i < (int) (count * (0.35 + q * 0.65)); i++) {
                double x = r.nextDouble() * DW, y = y0 + r.nextDouble() * (y1 - y0), h = 5 + (y - y0) / (y1 - y0) * 14;
                stroke(x, y, x + r.nextGaussian() * 3, y - h, 1.8, mix(a, b, r.nextDouble()), 0.6);
            }
        }

        void dunes(double y0, int bands, Color light, Color dark) {
            for (int l = 0; l < bands; l++) {
                double t = l / (double) Math.max(1, bands - 1);
                int[] rg = ridge(y0 + l * 30, 18 - l * 2, 0.7 + l * 0.25, 3);
                land(rg, DH + 10, mix(light, dark, t), 0.92, false, dark);
                for (int i = 0; i < 14; i++) { int x = r.nextInt(DW); stroke(x, rg[x] + 3, x + 26 + r.nextInt(30), rg[Math.min(DW, x + 30)] + 6, 2.5, alpha(c(255, 244, 214), 1), 0.3); }
            }
        }

        // -------------------------------------------------------------- props and motifs
        void pagoda(double x, double y, double s) {
            Color wall = c(168, 74, 58), roof = c(58, 62, 62);
            int tiers = 3 + r.nextInt(2);
            double w = 15 * s, h = 9 * s;
            for (int i = 0; i < tiers; i++) {
                double ww = w * (1 - i * 0.16), by = y - i * h * 1.35;
                washRect(x - ww * 0.6, by - h, ww * 1.2, h, wall, 3, 0.9, 0.05);
                wash(poly(x - ww * 1.25, by - h + 1 * s, x - ww * 0.7, by - h - 3.2 * s, x + ww * 0.7, by - h - 3.2 * s, x + ww * 1.25, by - h + 1 * s, x, by - h - 1.2 * s), roof, 3, 0.95, 0.04);
            }
            stroke(x, y - tiers * h * 1.35 - 2 * s, x, y - tiers * h * 1.35 - 9 * s, 1.4 * s, roof, 0.9);
        }

        void pavilion(double x, double y, double s) {
            Color roof = c(60, 64, 62), post = c(150, 70, 56);
            washRect(x - 9 * s, y - 9 * s, 18 * s, 9 * s, c(236, 224, 200), 2, 0.5, 0.05);
            stroke(x - 8 * s, y, x - 8 * s, y - 9 * s, 1.6 * s, post, 0.9); stroke(x + 8 * s, y, x + 8 * s, y - 9 * s, 1.6 * s, post, 0.9);
            wash(poly(x - 13 * s, y - 8 * s, x - 6 * s, y - 14 * s, x + 6 * s, y - 14 * s, x + 13 * s, y - 8 * s, x, y - 10 * s), roof, 3, 0.95, 0.05);
        }

        void crane(double x, double y, double s) {
            Color body = c(250, 250, 246), ink = c(40, 44, 44);
            wash(poly(x - 5 * s, y, x, y - 2 * s, x + 6 * s, y - 1 * s, x + 2 * s, y + 2 * s), body, 3, 0.95, 0.08);
            stroke(x + 5 * s, y - 1 * s, x + 9 * s, y - 4 * s, 1.1 * s, ink, 0.8); // neck and head
            wash(poly(x - 2 * s, y - 1 * s, x - 9 * s, y - 8 * s, x - 3 * s, y - 3 * s), body, 3, 0.9, 0.08);
            wash(poly(x - 1 * s, y - 1 * s, x + 6 * s, y - 8 * s, x + 2 * s, y - 2 * s), body, 3, 0.9, 0.08);
            stroke(x - 5 * s, y + 1 * s, x - 9 * s, y + 3 * s, 0.9 * s, ink, 0.7); // legs trailing
        }

        void birds(double x, double y, int n, double s) {
            for (int i = 0; i < n; i++) {
                double bx = x + i * 14 * s + r.nextGaussian() * 4, by = y + Math.abs(i - n / 2.0) * 3 * s + r.nextGaussian() * 3;
                stroke(bx - 4 * s, by - 2 * s, bx, by, 1.4 * s, c(48, 52, 54), 0.75); stroke(bx, by, bx + 4 * s, by - 2 * s, 1.4 * s, c(48, 52, 54), 0.75);
            }
        }

        void boat(double x, double y, double s) {
            wash(poly(x - 14 * s, y - 2 * s, x - 9 * s, y + 2 * s, x + 9 * s, y + 2 * s, x + 15 * s, y - 3 * s, x, y - 1 * s), c(70, 56, 46), 4, 0.95, 0.06);
            stroke(x - 2 * s, y - 2 * s, x - 2 * s, y - 9 * s, 1.4 * s, c(56, 48, 42), 0.85);
            wash(poly(x - 5 * s, y - 9 * s, x + 1 * s, y - 9 * s, x + 4 * s, y - 14 * s), c(54, 60, 58), 3, 0.9, 0.06); // the hat of the boatman
            stroke(x - 16 * s, y + 3 * s, x - 6 * s, y + 3 * s, 1.3 * s, c(255, 255, 255), 0.5); stroke(x + 8 * s, y + 3.5 * s, x + 20 * s, y + 3.5 * s, 1.3 * s, c(255, 255, 255), 0.5);
        }

        void figure(double x, double y, double s) { // a hermit with a hat and a staff
            wash(poly(x - 2.5 * s, y, x - 2 * s, y - 8 * s, x + 2 * s, y - 8 * s, x + 2.5 * s, y), c(88, 74, 64), 3, 0.9, 0.06);
            wash(poly(x - 4 * s, y - 8 * s, x, y - 13 * s, x + 4 * s, y - 8 * s), c(60, 56, 50), 3, 0.95, 0.06);
            stroke(x + 4 * s, y, x + 3 * s, y - 12 * s, 1.0 * s, c(70, 56, 44), 0.85);
        }

        void moon(double x, double y, double rad, Color col) {
            for (int i = 4; i >= 1; i--) { g.setColor(alpha(col, 0.05)); g.fill(new Ellipse2D.Double(x - rad - i * 7, y - rad - i * 7, 2 * (rad + i * 7), 2 * (rad + i * 7))); }
            washCircle(x, y, rad, rad, col, 6, 0.96, 0.05);
            g.setColor(alpha(c(90, 104, 130), 0.55)); g.setStroke(new BasicStroke(1.4f)); g.draw(new Ellipse2D.Double(x - rad, y - rad, 2 * rad, 2 * rad));
            washCircle(x + rad * 0.3, y - rad * 0.1, rad * 0.55, rad * 0.5, c(150, 160, 175), 3, 0.25, 0.2); // a few maria
        }

        void deadTree(double x, double y, double h) {
            stroke(x, y, x + r.nextGaussian() * 2, y - h, Math.max(2, h / 12), c(46, 40, 38), 0.9);
            for (int i = 0; i < 5; i++) {
                double t = 0.35 + i * 0.13, bx = x, by = y - h * t, dir = i % 2 == 0 ? -1 : 1, len = h * (0.34 - t * 0.2);
                stroke(bx, by, bx + dir * len, by - len * 0.7, Math.max(1.2, h / 22), c(46, 40, 38), 0.85);
                stroke(bx + dir * len, by - len * 0.7, bx + dir * len * 1.4, by - len * 1.3, 1.0, c(46, 40, 38), 0.8);
            }
        }

        void floatingIslet(double x, double y, double w) {
            wash(poly(x - w / 2, y, x - w * 0.3, y + w * 0.35, x - w * 0.08, y + w * 0.7, x + w * 0.05, y + w * 0.4, x + w * 0.3, y + w * 0.3, x + w / 2, y), mix(pal.rock, pal.rockDark, 0.3), 5, 0.95, 0.07);
            wash(poly(x - w / 2, y, x - w * 0.3, y - w * 0.12, x + w * 0.3, y - w * 0.12, x + w / 2, y), pal.leafLight, 4, 0.95, 0.06);
            pine(x + w * 0.1, y - w * 0.08, w * 0.4, pal.leafDark);
            for (int i = 0; i < 3; i++) stroke(x - w * 0.3 + i * w * 0.3, y + w * 0.35, x - w * 0.3 + i * w * 0.3 + r.nextGaussian() * 3, y + w * 0.7 + 12 + r.nextInt(14), 1.2, c(255, 255, 255), 0.35); // waterfalls off the islet
        }

        void lightRays(double x0, double y0, int n, double strength) {
            for (int i = 0; i < n; i++) {
                double ang = Math.PI * (0.62 + 0.30 * i / Math.max(1, n - 1)) + r.nextGaussian() * 0.03, len = 260 + r.nextInt(80), wid = 14 + r.nextInt(20);
                double x1 = x0 + Math.cos(ang) * len, y1 = y0 + Math.sin(ang) * len;
                Path2D.Double ray = new Path2D.Double(); ray.moveTo(x0 - 2, y0); ray.lineTo(x1 - wid, y1); ray.lineTo(x1 + wid, y1); ray.lineTo(x0 + 2, y0); ray.closePath();
                g.setPaint(new GradientPaint((float) x0, (float) y0, alpha(pal.sun, strength), (float) x1, (float) y1, alpha(pal.sun, 0)));
                g.fill(ray);
            }
        }

        void flakes(int n) { for (int i = 0; i < n; i++) { double x = r.nextDouble() * DW, y = r.nextDouble() * DH, s = 0.8 + r.nextDouble() * 1.8; g.setColor(alpha(c(255, 255, 255), 0.5 + r.nextDouble() * 0.4)); g.fill(new Ellipse2D.Double(x, y, s, s)); } }
        void embers(int n) { for (int i = 0; i < n; i++) { double x = r.nextDouble() * DW, y = 60 + r.nextDouble() * (DH - 60), s = 1 + r.nextDouble() * 2.2; g.setColor(alpha(c(255, 150 + r.nextInt(80), 40), 0.5 + r.nextDouble() * 0.45)); g.fill(new Ellipse2D.Double(x, y, s, s)); stroke(x, y, x + r.nextGaussian() * 2, y + 6, 1.0, c(255, 120, 40), 0.25); } }
        void spores(int n) { for (int i = 0; i < n; i++) washCircle(r.nextDouble() * DW, 150 + r.nextDouble() * 140, 4 + r.nextDouble() * 12, 3 + r.nextDouble() * 8, c(150, 210, 90), 2, 0.22, 0.2); }
        void flowers(int n) { Color[] cs = {c(244, 170, 190), c(250, 226, 130), c(240, 240, 250), c(190, 150, 230)}; for (int i = 0; i < n; i++) { double x = r.nextDouble() * DW, y = 215 + r.nextDouble() * 70; g.setColor(alpha(cs[r.nextInt(cs.length)], 0.85)); g.fill(new Ellipse2D.Double(x, y, 2.6, 2.6)); } }
    }

    // ------------------------------------------------------------------ scenes
    static void scene(Painter p, String kind, String noun) {
        Random r = p.r; Pal pal = p.pal;
        switch (kind) {
            case "capital": ArtCity.capital(p); break;
            case "mountains": {
                p.sky(170); p.sun(90 + r.nextInt(330), 56 + r.nextInt(34), 24 + r.nextInt(10));
                boolean cold = pal.ground.getBlue() > 200;
                p.mountainLayers(146 + r.nextInt(14), 4, 118 + r.nextInt(24), cold || r.nextInt(3) == 0, p.o.count > 0 ? Math.min(p.o.count, 9) : 0);
                for (int i = 0; i < 7; i++) p.pine(16 + r.nextInt(480), 262 + r.nextInt(22), 26 + r.nextInt(28), pal.leafDark);
                if (noun.equals("cliffs") || noun.equals("crags")) p.rock(poly(330, 290, 346, 118, 396, 92, 444, 130, 476, 290), pal.rock);
                break;
            }
            case "hills": {
                p.sky(180); p.sun(300 + r.nextInt(150), 66, 28);
                for (int l = 0; l < 4; l++) { int[] rg = p.ridge(150 + l * 28, 22 - l * 3, 0.9 + l * 0.2, 3); Color col = mix(mix(pal.far, pal.ground, 0.5), pal.leafDark, l / 3.0 * 0.85); p.land(rg, DH + 10, col, 0.93, false, col); p.mist(150 + l * 28 + 6, 22, 0.28 - l * 0.05); }
                if (noun.equals("mesas") || noun.equals("badlands")) for (int i = 0; i < 3; i++) { double x = 50 + i * 165 + r.nextInt(40), w = 70 + r.nextInt(44), y = 128 + r.nextInt(30); p.rock(poly(x, y + 200, x, y, x + w, y + 4, x + w, y + 200), mix(pal.rock, c(170, 110, 84), 0.55)); p.washRect(x, y, w, 8, pal.sand, 4, 0.7, 0.1); }
                for (int i = 0; i < 4; i++) p.broad(30 + r.nextInt(450), 268 + r.nextInt(16), 30, pal.leafDark);
                break;
            }
            case "forest": {
                p.sky(160); p.mountainLayers(118, 2, 62, false);
                boolean jungle = noun.equals("jungles") || noun.equals("rainforests");
                if (noun.equals("groves")) { // separate clumps of trees on grass
                    int ng = p.o.count > 0 ? Math.min(p.o.count, 8) : 3 + r.nextInt(2);
                    p.washRect(-10, 190, DW + 20, 120, mix(pal.ground, pal.leafLight, 0.35), 8, 0.95, 0.05);
                    p.grass(200, 288, 200, pal.leafLight, pal.leafDark);
                    for (int k = 0; k < ng; k++) {
                        double cx = 40 + (k + 0.5) * (DW - 80) / ng + r.nextGaussian() * 10, base = 224 + r.nextInt(50), s = ng > 5 ? 0.7 : 1.0;
                        for (int i = 0; i < 8; i++) { double x = cx + r.nextGaussian() * 22 * s, y = base + r.nextInt(14), hh = (34 + r.nextInt(26)) * s; if (r.nextInt(3) == 0) p.pine(x, y, hh, pal.leafDark); else p.broad(x, y, hh, mix(pal.leafLight, pal.leafDark, r.nextDouble())); }
                    }
                    break;
                }
                for (int row = 0; row < 4; row++) {
                    double t = row / 3.0; int n = 7 + row * 4;
                    for (int i = 0; i < n; i++) {
                        double x = r.nextDouble() * DW, y = 172 + row * 33 + r.nextInt(10), h = 40 + row * 26 + r.nextInt(18);
                        Color col = mix(mix(pal.far, pal.leafLight, 0.5), pal.leafDark, t);
                        if ((noun.equals("forests") || noun.equals("woods") || jungle) && r.nextInt(3) == 0) p.broad(x, y, h, col); else p.pine(x, y, h, col);
                    }
                    p.mist(172 + row * 33, 20, 0.22);
                }
                if (noun.equals("glades")) { p.washCircle(256, 250, 150, 38, alpha(c(255, 246, 200), 1), 8, 0.6, 0.12); }
                break;
            }
            case "meadow": {
                p.sky(170); p.sun(120 + r.nextInt(280), 66, 24);
                for (int l = 0; l < 3; l++) { int[] rg = p.ridge(165 + l * 32, 10, 0.8 + l * 0.4, 3); Color col = mix(mix(pal.far, pal.ground, 0.6), pal.ground, l / 2.0); p.land(rg, DH + 10, col, 0.94, false, col); p.mist(165 + l * 32, 16, 0.2); }
                if (noun.equals("beach") || noun.equals("beaches")) { p.waterBody(170, 212); p.washRect(-10, 208, DW + 20, 90, pal.sand, 8, 0.95, 0.05); p.washRect(-10, 204, DW + 20, 6, c(255, 255, 255), 4, 0.6, 0.2); }
                else { p.grass(200, 288, 420, pal.leafLight, pal.leafDark); for (int i = 0; i < 3; i++) p.broad(40 + r.nextInt(430), 218 + r.nextInt(12), 40, pal.leafDark); }
                break;
            }
            case "lake": {
                int npool = p.o.count > 0 ? Math.min(p.o.count, 10) : 1;
                if (npool > 1) { // several ponds in a meadow
                    p.sky(160); p.mountainLayers(140, 2, 60, false);
                    p.washRect(-10, 170, DW + 20, 140, mix(pal.ground, pal.leafLight, 0.3), 8, 0.95, 0.05);
                    p.grass(180, 288, 220, pal.leafLight, pal.leafDark);
                    for (int k = 0; k < npool; k++) {
                        double cx = 40 + r.nextInt(432), cy = 190 + (k % 3) * 28 + r.nextInt(20), rx = (npool > 5 ? 28 : 54) + r.nextInt(28), ry = rx * 0.3;
                        p.washCircle(cx, cy + 2, rx + 4, ry + 3, mix(pal.sand, pal.ground, 0.5), 4, 0.8, 0.1);
                        p.washCircle(cx, cy, rx, ry, mix(pal.water, c(255, 255, 255), 0.2), 6, 0.95, 0.08);
                        if (r.nextBoolean()) p.reeds(cx - rx * 0.8 + r.nextDouble() * rx * 1.6, cy + ry * 0.4, 8 + r.nextInt(6));
                    }
                    p.mist(176, 18, 0.3);
                    break;
                }
                p.sky(150); p.mountainLayers(128, 3, 92, false);
                p.waterBody(165, DH + 4);
                p.washRect(-10, 160, DW + 20, 34, alpha(pal.near, 1), 5, 0.3, 0.1);
                p.mist(166, 14, 0.4);
                if (noun.equals("lagoons") || noun.equals("coves") || noun.equals("bays")) p.land(p.ridge(252, 12, 1.1, 2), DH + 10, pal.sand, 0.95, false, pal.sand);
                for (int i = 0; i < 4; i++) p.reeds(16 + r.nextInt(480), 262 + r.nextInt(22), 26 + r.nextInt(18));
                break;
            }
            case "river": {
                p.sky(140); p.mountainLayers(118, 3, 100, false);
                p.washRect(-10, 160, DW + 20, 140, mix(pal.ground, pal.leafDark, 0.25), 8, 0.95, 0.05);
                List<double[]> riv = poly(250, 165, 226, 195, 318, 212, 280, 244, 236, 262, 160, 300, 400, 300, 420, 262, 396, 238, 330, 224, 290, 210, 268, 190, 262, 165);
                p.g.setClip(p.path(riv)); p.waterBody(160, 300); p.g.setClip(null);
                p.wash(riv, alpha(pal.waterDeep, 1), 6, 0.25, 0.04);
                for (int i = 0; i < 11; i++) { double x = r.nextBoolean() ? 50 + r.nextInt(130) : 390 + r.nextInt(112), y = 232 + r.nextInt(52); if (r.nextBoolean()) p.pine(x, y, 34, pal.leafDark); else p.broad(x, y, 36, pal.leafDark); }
                p.mist(172, 18, 0.3);
                break;
            }
            case "falls": {
                p.sky(140);
                int nfall = p.o.count > 0 ? Math.min(p.o.count, 9) : 1;
                if (nfall > 1) { // a cliff wall with several falls
                    p.rock(poly(-6, 294, -6, 40, 120, 28, 260, 36, 400, 24, DW + 6, 38, DW + 6, 294), pal.rock);
                    for (int k = 0; k < nfall; k++) {
                        double cx = 30 + (k + 0.5) * (DW - 60) / nfall, wd = Math.max(8, (DW - 60) / nfall * 0.32), top = 40 + r.nextInt(20);
                        p.g.setPaint(new GradientPaint(0, (float) top, c(252, 253, 254), 0, 290, c(206, 226, 232))); p.g.fill(new Rectangle2D.Double(cx - wd / 2, top, wd, 250 - top));
                        for (int i = 0; i < 10; i++) { double x = cx - wd / 2 + r.nextDouble() * wd; p.stroke(x, top + 4, x, 246, 1.6, c(255, 255, 255), 0.5); }
                        p.washCircle(cx, 254, wd * 1.2, 8, c(255, 255, 255), 3, 0.7, 0.2);
                    }
                    p.mist(258, 34, 0.45);
                    break;
                }
                p.rock(poly(-6, 294, -6, 26, 172, 16, 216, 120, 226, 294), pal.rock); p.rock(poly(DW + 6, 294, DW + 6, 10, 332, 28, 292, 130, 282, 294), mix(pal.rock, pal.rockDark, 0.15));
                p.g.setPaint(new GradientPaint(0, 60, c(252, 253, 254), 0, 290, c(206, 226, 232))); p.g.fill(new Rectangle2D.Double(208, 56, 90, 210));
                for (int i = 0; i < 34; i++) { double x = 211 + r.nextInt(84); p.stroke(x, 62, x + r.nextGaussian() * 1.5, 250, 2.2, i % 3 == 0 ? pal.water : c(255, 255, 255), 0.5); }
                for (int i = 0; i < 9; i++) p.washCircle(196 + r.nextInt(120), 252 + r.nextInt(26), 20 + r.nextInt(24), 9, c(255, 255, 255), 3, 0.7, 0.2);
                p.mist(258, 34, 0.45);
                for (int i = 0; i < 7; i++) p.pine(r.nextBoolean() ? 16 + r.nextInt(170) : 336 + r.nextInt(160), 40 + r.nextInt(70), 28, pal.leafDark); // both banks, not only the left one
                break;
            }
            case "cave": {
                // Seen from inside: layered rock walls close in from both sides, stalactites hang overhead, daylight shows at the far mouth(s),
                // and the floor holds a glowing pool or crystals depending on the kind of cave.
                boolean pit = noun.equals("pits") || noun.equals("sinkholes"), burrow = noun.equals("burrows"), under = noun.startsWith("underwater") || noun.equals("trenches");
                boolean grotto = noun.equals("grottoes"), cavern = noun.equals("caverns") || noun.equals("underwater_caverns");
                int mouths = p.o.count > 0 ? Math.min(p.o.count, 7) : 1;
                Color deep = c(12, 14, 20), wallLo = mix(pal.rock, deep, 0.62), wallHi = mix(pal.rock, deep, 0.35), glow = under ? c(120, 200, 220) : pit ? c(250, 240, 200) : c(255, 226, 160);
                p.gradientRect(0, DH, mix(deep, pal.rock, 0.25), deep);
                // far wall and the way out
                p.washRect(-10, 40, DW + 20, 190, mix(pal.rock, deep, 0.5), 8, 0.95, 0.06);
                if (pit) { // looking straight up a shaft: a bright disc far above
                    p.washCircle(256, 60, 70, 40, glow, 8, 0.95, 0.06);
                    p.g.setPaint(new GradientPaint(0, 60, alpha(glow, 0.6), 0, 290, alpha(glow, 0)));
                    p.g.fill(new Polygon(new int[]{200, 312, 380, 130}, new int[]{70, 70, 290, 290}, 4));
                } else {
                    for (int k = 0; k < mouths; k++) {
                        double cx = mouths == 1 ? 256 + p.r.nextGaussian() * 30 : 60 + (k + 0.5) * (DW - 120) / mouths, w = mouths == 1 ? 58 + p.r.nextInt(30) : 30 + p.r.nextInt(18), hh = w * (burrow ? 0.9 : 1.15);
                        for (int s = 4; s >= 1; s--) { p.g.setColor(alpha(glow, 0.06)); p.g.fill(new Ellipse2D.Double(cx - w - s * 10, 168 - hh - s * 10, 2 * (w + s * 10), 2 * (hh + s * 10))); }
                        List<double[]> mouth = new ArrayList<>(); mouth.add(new double[]{cx - w, 200});
                        for (int q = 0; q <= 12; q++) { double an = Math.PI * q / 12; mouth.add(new double[]{cx - Math.cos(an) * w, 200 - Math.sin(an) * hh}); }
                        mouth.add(new double[]{cx + w, 200});
                        p.wash(mouth, glow, 10, 0.97, 0.04);
                        Shape keep = p.g.getClip(); p.g.setClip(p.path(mouth)); // a glimpse of the land outside: sky above, green below
                        p.g.setPaint(new GradientPaint(0, (float) (200 - hh), alpha(c(200, 226, 236), 0.8), 0, 200, alpha(pal.leafLight, 0.9))); p.g.fill(new Rectangle2D.Double(cx - w, 200 - hh, 2 * w, hh));
                        p.g.setClip(keep);
                        p.g.setPaint(new GradientPaint(0, 150, alpha(glow, 0.5), 0, 290, alpha(glow, 0)));
                        p.g.fill(new Polygon(new int[]{(int) (cx - w * 0.8), (int) (cx + w * 0.8), (int) (cx + w * 2.4), (int) (cx - w * 2.4)}, new int[]{200, 200, 290, 290}, 4)); // light spilling across the floor
                    }
                }
                // the rock walls that close in from the left and right (two layers: far then near)
                for (int layer = 0; layer < 2; layer++) {
                    double inset = layer == 0 ? 70 : 20, reach = layer == 0 ? 170 : 120;
                    Color col = layer == 0 ? wallHi : wallLo;
                    p.rock(poly(-8, -8, inset + reach * 0.35 + p.r.nextInt(30), -8, inset + reach * 0.6, 70 + p.r.nextInt(30), inset + reach * 0.35, 150, inset + reach * (layer == 0 ? 0.55 : 0.3), 230, inset + reach * 0.5, 298, -8, 298), col);
                    p.rock(poly(DW + 8, -8, DW - inset - reach * 0.35 - p.r.nextInt(30), -8, DW - inset - reach * 0.6, 70 + p.r.nextInt(30), DW - inset - reach * 0.35, 150, DW - inset - reach * (layer == 0 ? 0.55 : 0.3), 230, DW - inset - reach * 0.5, 298, DW + 8, 298), col);
                }
                // the ceiling and its stalactites
                p.rock(poly(-8, -8, DW + 8, -8, DW + 8, 36 + p.r.nextInt(14), 380, 52 + p.r.nextInt(18), 256, 40 + p.r.nextInt(12), 130, 52 + p.r.nextInt(18), -8, 38), wallLo);
                for (int i = 0; i < 22; i++) { double x = 40 + p.r.nextInt(DW - 80), top = 40 + Math.abs(x - 256) * 0.06, hh = 16 + p.r.nextInt(46), wd = 5 + p.r.nextInt(7); p.wash(poly(x - wd, top, x + wd, top, x + p.r.nextGaussian() * 2, top + hh), mix(wallLo, deep, 0.2), 3, 0.95, 0.08); }
                // the floor and what lies on it
                p.washRect(-10, 240, DW + 20, 60, mix(pal.rock, deep, 0.45), 8, 0.95, 0.05);
                if (grotto || cavern || under) { // a still pool that mirrors the glow
                    Color pool = under ? c(60, 140, 170) : c(70, 130, 160);
                    p.washCircle(256, 262, 150, 22, pool, 8, 0.95, 0.06);
                    p.washCircle(256, 262, 90, 12, alpha(glow, 1), 6, 0.45, 0.1);
                    for (int i = 0; i < 14; i++) p.stroke(110 + p.r.nextInt(290), 252 + p.r.nextInt(22), 130 + p.r.nextInt(290), 253 + p.r.nextInt(22), 1.6, glow, 0.3);
                }
                if (cavern || under) { // glowing crystals
                    for (int i = 0; i < 7; i++) {
                        double x = 40 + p.r.nextInt(DW - 80), y = 262 + p.r.nextInt(30), hh = 18 + p.r.nextInt(28);
                        Color cr = under ? c(150, 240, 230) : (i % 2 == 0 ? c(170, 210, 255) : c(220, 180, 255));
                        for (int s = 3; s >= 1; s--) { p.g.setColor(alpha(cr, 0.07)); p.g.fill(new Ellipse2D.Double(x - hh * s * 0.5, y - hh - hh * s * 0.4, hh * s, hh * s * 1.2)); }
                        p.wash(poly(x - 5, y, x - 3, y - hh * 0.8, x, y - hh, x + 3, y - hh * 0.7, x + 5, y), cr, 4, 0.9, 0.05);
                    }
                }
                for (int i = 0; i < 9; i++) { double x = 30 + p.r.nextInt(DW - 60), hh = 10 + p.r.nextInt(30), wd = 6 + p.r.nextInt(8); p.wash(poly(x - wd, 298, x + wd, 298, x + p.r.nextGaussian() * 2, 298 - hh), wallLo, 3, 0.95, 0.08); }
                if (burrow) for (int i = 0; i < 16; i++) { double x = 60 + p.r.nextInt(DW - 120); p.stroke(x, 42, x + p.r.nextGaussian() * 6, 60 + p.r.nextInt(60), 1.8, c(86, 66, 48), 0.8); } // roots
                if (under) { for (int i = 0; i < 26; i++) p.washCircle(p.r.nextInt(DW), 60 + p.r.nextInt(220), 1.5 + p.r.nextDouble() * 4, 1.5 + p.r.nextDouble() * 4, c(220, 245, 255), 2, 0.5, 0.2); p.washRect(-10, 0, DW + 20, DH, c(70, 150, 180), 5, 0.22, 0.05); } // bubbles and a blue cast
                p.mist(256, 22, 0.08);
                break;
            }
            case "arch": {
                p.sky(170);
                boolean ice = noun.equals("ice_arches"), sea = noun.equals("sea_arches");
                if (sea) p.waterBody(186, DH + 4);
                if (noun.contains("bridges") && !noun.equals("natural_bridges")) {
                    p.waterBody(210, DH + 4);
                    int nb = p.o.count > 0 ? Math.min(p.o.count, 9) : 1;
                    if (nb <= 1) {
                        p.stroke(62, 212, 450, 212, 7, c(96, 70, 60), 0.95);
                        for (int i = 0; i < 40; i++) { double a = Math.PI * i / 39; p.stroke(255 - Math.cos(a) * 195, 212 - Math.sin(a) * 92, 255 - Math.cos(a + 0.08) * 195, 212 - Math.sin(a + 0.08) * 92, 12, c(100, 72, 62), 0.9); }
                        for (int i = 0; i < 9; i++) p.stroke(80 + i * 43, 150 + Math.abs(4 - i) * 14, 80 + i * 43, 212, 3, c(92, 66, 58), 0.85);
                    } else { // a long bridge with `nb` arched spans
                        double span = 400.0 / nb, deck = 168, water = 214;
                        for (int k = 0; k < nb; k++) {
                            double cx = 56 + (k + 0.5) * span, rad = span * 0.46, hgt = Math.min(rad * 0.95, water - deck - 4);
                            for (int i = 0; i < 26; i++) { double a1 = Math.PI * i / 26, a2 = Math.PI * (i + 1) / 26; p.stroke(cx - Math.cos(a1) * rad, water - Math.sin(a1) * hgt, cx - Math.cos(a2) * rad, water - Math.sin(a2) * hgt, 7, c(104, 78, 66), 0.92); }
                            p.washRect(cx - rad - 3, deck - 2, rad * 2 + 6, water - deck - hgt * 0.15, c(120, 94, 80), 3, 0.35, 0.05);
                        }
                        p.stroke(48, deck, 464, deck, 8, c(92, 68, 58), 0.95);
                        for (int i = 0; i <= nb * 4; i++) p.stroke(56 + i * span / 4, deck - 14, 56 + i * span / 4, deck, 2, c(92, 66, 58), 0.8);
                        p.stroke(48, deck - 14, 464, deck - 14, 2.4, c(92, 66, 58), 0.85);
                    }
                } else if (noun.equals("giant_footprints")) {
                    p.washRect(-10, 170, DW + 20, 130, mix(pal.sand, pal.ground, 0.4), 8, 0.95, 0.05);
                    int nf = p.o.count > 0 ? Math.min(p.o.count, 14) : 4;
                    double sc = nf <= 4 ? 1.0 : nf <= 8 ? 0.72 : 0.5, step = nf <= 1 ? 0 : (nf <= 4 ? 100 : 360.0 / (nf - 1));
                    for (int i = 0; i < nf; i++) { double x = (nf == 1 ? 220 : 60 + i * (nf <= 4 ? 100 : step)), y = 200 + (i % 2) * 40 * sc - i * 0; Color pr = c(96, 82, 62); p.washCircle(x + 35 * sc, y + 18 * sc, 38 * sc, 20 * sc, pr, 6, 0.6, 0.1); for (int t = 0; t < 4; t++) p.washCircle(x + (14 + t * 15) * sc, y - 6 * sc, 7 * sc, 8 * sc, pr, 4, 0.6, 0.1); p.washCircle(x + 35 * sc, y + 16 * sc, 28 * sc, 12 * sc, alpha(c(236, 226, 196), 1), 4, 0.45, 0.1); }
                } else {
                    int na = p.o.count > 0 ? Math.min(p.o.count, 6) : 1;
                    if (na <= 1) {
                        List<double[]> a = poly(64, 290, 66, 130, 100, 44, 250, 30, 400, 44, 444, 130, 446, 290, 382, 290, 380, 154, 350, 108, 160, 108, 130, 154, 128, 290);
                        p.rock(a, ice ? c(170, 204, 218) : mix(pal.rock, c(112, 96, 88), 0.4));
                    } else for (int k = 0; k < na; k++) {
                        double cx = 40 + (k + 0.5) * (DW - 80) / na, hw = (DW - 80) / na * 0.46, hh = 120 + r.nextInt(70), th = Math.max(10, hw * 0.22);
                        p.rock(poly(cx - hw, 290, cx - hw + 2, 290 - hh * 0.55, cx - hw * 0.7, 290 - hh * 0.9, cx, 290 - hh, cx + hw * 0.7, 290 - hh * 0.9, cx + hw - 2, 290 - hh * 0.55, cx + hw, 290,
                                cx + hw - th, 290, cx + hw - th, 290 - hh * 0.5, cx + hw * 0.55, 290 - hh * 0.75, cx, 290 - hh + th, cx - hw * 0.55, 290 - hh * 0.75, cx - hw + th, 290 - hh * 0.5, cx - hw + th, 290),
                                ice ? c(170, 204, 218) : mix(pal.rock, c(112, 96, 88), 0.4));
                    }
                    p.washCircle(256, 260, 140, 50, alpha(c(252, 240, 206), 1), 8, 0.4, 0.12);
                }
                p.mist(270, 22, 0.35);
                break;
            }
            case "desert": {
                p.sky(160); p.sun(330 + r.nextInt(100), 78, 34);
                p.dunes(160, 4, mix(pal.sand, c(250, 226, 180), 0.4), mix(pal.near, pal.rock, 0.5));
                if (noun.equals("mirage_wastes")) {
                    p.washCircle(256, 168, 150, 10, mix(pal.water, c(230, 240, 245), 0.4), 7, 0.55, 0.12); // the lake that is not there
                    p.washCircle(256, 150, 120, 8, mix(pal.water, c(255, 255, 255), 0.5), 4, 0.3, 0.15);     // ...and its reflection in the sky
                    for (int i = 0; i < 40; i++) { double x = r.nextInt(DW), y = 160 + r.nextInt(30); p.stroke(x, y, x + 20 + r.nextInt(40), y + r.nextGaussian() * 2.2, 1.3, c(255, 244, 220), 0.3); }
                }
                if (noun.equals("oases")) { p.washCircle(256, 240, 88, 15, pal.water, 6, 0.95, 0.08); for (int i = 0; i < 5; i++) { double x = 186 + i * 36; p.stroke(x, 242, x + 10, 184, 3, p.pal.trunk, 0.9); for (int f = 0; f < 6; f++) p.stroke(x + 10, 184, x + 10 + (f - 2.5) * 17, 180 + Math.abs(f - 2.5) * 9, 3.5, pal.leafDark, 0.85); } }
                p.mist(176, 20, 0.22);
                break;
            }
            case "islands": {
                p.sky(166); p.waterBody(166, DH + 4);
                boolean reef = noun.contains("reef") || noun.contains("coral");
                int n = p.o.count > 0 ? Math.min(p.o.count, 18) : 4 + r.nextInt(3);
                double isc = n > 8 ? 0.55 : 1.0;
                for (int i = 0; i < n; i++) {
                    double x = 30 + r.nextInt(430), y = 172 + r.nextInt(92), w = (44 + r.nextInt(96)) * isc, h = reef ? 7 : (14 + r.nextInt(44)) * isc;
                    p.wash(poly(x, y, x + w * 0.2, y - h, x + w * 0.5, y - h * 1.1, x + w * 0.8, y - h * 0.9, x + w, y, x + w * 0.5, y + 4), reef ? c(200, 150, 130) : mix(pal.near, pal.leafDark, 0.5), 8, 0.95, 0.08);
                    if (!reef && h > 22) p.pine(x + w * 0.5, y - h * 0.55, 20, pal.leafDark);
                    p.washCircle(x + w / 2, y + 1, w * 0.6, 4, c(255, 255, 255), 3, 0.65, 0.2);
                }
                p.mist(172, 20, 0.28);
                break;
            }
            case "whirlpool": {
                p.sky(150); p.sun(290 + r.nextInt(120), 96, 26); p.waterBody(150, DH + 4);
                for (int i = 0; i < 20; i++) { double y = 158 + i * 6, len = 30 + i * 5; for (double x = -r.nextDouble() * 60; x < DW; x += len * 1.7) p.stroke(x, y, x + len, y - 1.2, 1.2 + i * 0.1, c(255, 255, 255), 0.15 + i * 0.01); }
                int nw = p.o.count > 0 ? Math.min(p.o.count, 5) : 1;
                for (int k2 = 0; k2 < nw; k2++) {
                    double cx = nw == 1 ? 256 : 70 + (k2 + 0.5) * 380.0 / nw, cy = nw == 1 ? 232 : 214 + r.nextInt(50), sc = nw == 1 ? 1.0 : 0.62;
                    p.washCircle(cx, cy, 74 * sc, 24 * sc, mix(pal.water, c(10, 30, 60), 0.75), 7, 0.9, 0.1);
                    double turns = 2.6, prevx = cx, prevy = cy;
                    for (int s = 0; s <= 90; s++) {
                        double t = s / 90.0, ang = t * turns * Math.PI * 2, rad = (6 + t * 78) * sc;
                        double x = cx + Math.cos(ang) * rad, y = cy + Math.sin(ang) * rad * 0.34;
                        if (s > 0) p.stroke(prevx, prevy, x, y, 1.6 + t * 2.4 * sc, c(255, 255, 255), 0.25 + 0.4 * t);
                        prevx = x; prevy = y;
                    }
                    p.washCircle(cx, cy, 9 * sc, 3.2 * sc, c(6, 20, 40), 4, 0.95, 0.1);
                }
                p.mist(160, 20, 0.2);
                break;
            }
            case "pillars": {
                boolean forest = noun.equals("stone_forests");
                p.sky(170); p.sun(330 + r.nextInt(100), 70, 26);
                p.mountainLayers(150, 2, 50, false);
                p.washRect(-10, 196, DW + 20, 110, mix(pal.sand, pal.ground, 0.35), 8, 0.95, 0.05);
                int n = p.o.count > 0 ? Math.min(p.o.count, 16) : (forest ? 15 : 8);
                for (int i = 0; i < n; i++) {
                    double x = 20 + (i + 0.2 + r.nextDouble() * 0.6) * (DW - 40) / n, base = 226 + r.nextInt(46), s = 0.6 + (base - 226) / 46.0 * 0.5;
                    double h = (forest ? 110 + r.nextInt(70) : 60 + r.nextInt(44)) * s, w = (forest ? 12 + r.nextInt(8) : 16 + r.nextInt(8)) * s;
                    Color col = mix(pal.rock, pal.sand, 0.25 + r.nextDouble() * 0.3);
                    if (forest) {
                        p.rock(poly(x - w, base, x - w * 0.8, base - h * 0.5, x - w * 0.55, base - h, x + w * 0.5, base - h - 2, x + w * 0.8, base - h * 0.5, x + w, base), col);
                        if (r.nextInt(2) == 0) p.pine(x, base - h, 16 * s, pal.leafDark);
                    } else { // a hoodoo: a slender shaft with a heavy cap
                        p.rock(poly(x - w * 0.6, base, x - w * 0.4, base - h * 0.6, x - w * 0.5, base - h * 0.9, x + w * 0.5, base - h * 0.9, x + w * 0.4, base - h * 0.6, x + w * 0.6, base), col);
                        p.rock(poly(x - w * 1.1, base - h * 0.88, x - w * 0.7, base - h * 1.02, x + w * 0.8, base - h * 1.0, x + w * 1.15, base - h * 0.86, x, base - h * 0.8), mix(col, pal.rockDark, 0.4));
                    }
                }
                p.mist(210, 18, 0.2);
                break;
            }
            case "menhirs": {
                p.sky(170); p.sun(300 + r.nextInt(120), 64, 26);
                p.mountainLayers(150, 2, 46, false);
                p.washRect(-10, 190, DW + 20, 120, mix(pal.ground, pal.leafLight, 0.35), 8, 0.95, 0.05);
                p.grass(196, 290, 220, pal.leafLight, pal.leafDark);
                int n = p.o.count > 0 ? Math.min(p.o.count, 14) : 9;
                for (int i = 0; i < n; i++) {
                    double ang = i * Math.PI * 2 / n + 0.2, x = 256 + Math.cos(ang) * 150, y = 238 + Math.sin(ang) * 36, s = 0.75 + (y - 202) / 72.0 * 0.5;
                    double w = 17 * s, h = (50 + r.nextInt(26)) * s;
                    p.rock(poly(x - w, y, x - w * 0.8, y - h * 0.8, x - w * 0.3, y - h, x + w * 0.5, y - h * 0.96, x + w * 0.9, y - h * 0.6, x + w, y), mix(pal.rock, pal.rockDark, 0.2 + r.nextDouble() * 0.4));
                }
                if (n >= 6) for (int i = 0; i < 2; i++) { double x = 256 + (i - 0.5) * 60, y = 236; p.rock(poly(x - 34, y - 78, x + 34, y - 78, x + 36, y - 66, x - 36, y - 66), pal.rockDark); } // a trilithon at the heart
                p.mist(200, 18, 0.24);
                break;
            }
            case "terraces": {
                p.sky(150); p.sun(380 + r.nextInt(60), 66, 24);
                for (int l = 0; l < 2; l++) { int[] rg = p.ridge(110 + l * 24, 28, 0.9, 3); Color col = mix(pal.far, pal.leafDark, 0.3 + l * 0.2); p.land(rg, DH + 10, col, 0.9, false, col); p.mist(110 + l * 24 + 6, 22, 0.28); }
                int bands = 8;
                for (int k = 0; k < bands; k++) {
                    double y = 150 + k * 17, amp = 5 + k * 0.4, x0 = -10, inset = (bands - k) * 16;
                    List<double[]> top = new ArrayList<>(), bot = new ArrayList<>();
                    for (int s = 0; s <= 24; s++) { double x = x0 + s * (DW + 20) / 24.0; double w1 = Math.sin(s * 0.5 + k * 1.7) * amp; top.add(new double[]{x, y + w1}); bot.add(new double[]{x, y + 15 + w1 * 0.9}); }
                    List<double[]> ribbon = new ArrayList<>(top); for (int s = bot.size() - 1; s >= 0; s--) ribbon.add(bot.get(s));
                    boolean wet = k % 2 == 1;
                    p.wash(ribbon, wet ? mix(pal.water, c(220, 235, 240), 0.35) : mix(pal.leafLight, pal.leafDark, 0.25 + 0.05 * k), 6, 0.9, 0.02);
                    p.stroke(top.get(0)[0], top.get(0)[1], top.get(24)[0], top.get(24)[1], 2.2, pal.rockDark, 0.4);
                }
                p.pagoda(150 + r.nextInt(200), 160, 0.9);
                p.mist(150, 20, 0.2);
                break;
            }
            case "mounds": {
                p.sky(170); p.sun(120 + r.nextInt(280), 74, 38);
                p.washRect(-10, 190, DW + 20, 120, mix(pal.sand, c(212, 150, 96), 0.4), 8, 0.95, 0.05);
                p.dunes(196, 2, mix(pal.sand, c(250, 226, 180), 0.4), mix(pal.near, pal.rock, 0.5));
                int n = p.o.count > 0 ? Math.min(p.o.count, 12) : 6;
                for (int i = 0; i < n; i++) {
                    double x = 30 + r.nextInt(450), y = 226 + r.nextInt(52), s = 0.6 + (y - 226) / 52.0 * 0.6, h = (44 + r.nextInt(50)) * s, w = (20 + r.nextInt(14)) * s;
                    Color col = mix(c(176, 108, 70), pal.rock, 0.25 + r.nextDouble() * 0.3);
                    p.rock(poly(x - w, y, x - w * 0.6, y - h * 0.5, x - w * 0.25, y - h * 0.9, x, y - h, x + w * 0.3, y - h * 0.85, x + w * 0.65, y - h * 0.45, x + w, y), col);
                    p.stroke(x - w * 0.3, y - h * 0.8, x - w * 0.5, y - 2, 1.4, mix(col, pal.rockDark, 0.5), 0.5);
                    p.stroke(x + w * 0.2, y - h * 0.8, x + w * 0.4, y - 2, 1.4, mix(col, pal.rockDark, 0.5), 0.5);
                }
                for (int i = 0; i < 3; i++) { double x = 60 + r.nextInt(400); p.stroke(x, 232, x + 2, 196, 3, pal.trunk, 0.9); p.washCircle(x + 2, 190, 34, 8, pal.leafDark, 5, 0.85, 0.12); } // acacias
                p.mist(200, 16, 0.2);
                break;
            }
            case "sea": {
                p.sky(150); p.sun(290 + r.nextInt(120), 100, 28); p.waterBody(150, DH + 4);
                for (int i = 0; i < 26; i++) { double y = 160 + i * 4.8, len = 28 + i * 4; for (double x = -r.nextDouble() * 60; x < DW; x += len * 1.6) p.stroke(x, y, x + len, y - 1.5 - i * 0.08, 1.2 + i * 0.12, c(255, 255, 255), 0.18 + i * 0.012); }
                if (noun.equals("fjords")) { int[] rg = p.ridge(130, 50, 1.0, 3); p.land(rg, 170, mix(pal.far, pal.near, 0.6), 0.95, true, pal.rockDark); p.mist(150, 26, 0.35); }
                else if (noun.equals("coasts") || noun.equals("shorelines") || noun.equals("tide_pools")) { p.land(p.ridge(236, 9, 1.0, 3), DH + 10, pal.sand, 0.95, false, pal.sand); p.washRect(-10, 232, DW + 20, 4, c(255, 255, 255), 3, 0.8, 0.2); }
                break;
            }
            case "ice": {
                p.sky(160);
                for (int l = 0; l < 3; l++) { int[] rg = p.peaks(172 + l * 30, 112 - l * 20, 4); Color col = mix(c(236, 244, 250), c(132, 168, 204), l / 2.0); p.land(rg, DH + 10, col, 0.95, true, c(96, 136, 176)); p.mist(172 + l * 30, 22, 0.3); }
                for (int i = 0; i < 34; i++) { double x = r.nextInt(DW), y = 196 + r.nextInt(84); p.stroke(x, y, x + 14 + r.nextInt(40), y + r.nextGaussian() * 3, 2, c(90, 130, 170), 0.22); }
                break;
            }
            case "marsh": {
                p.sky(150); p.mountainLayers(136, 2, 50, false);
                p.washRect(-10, 168, DW + 20, 130, mix(pal.ground, pal.water, 0.4), 8, 0.95, 0.05);
                p.waterBody(174, DH + 4);
                p.washRect(-10, 172, DW + 20, 120, alpha(pal.leafDark, 1), 6, 0.22, 0.1);
                for (int i = 0; i < 28; i++) p.reeds(r.nextInt(DW), 204 + r.nextInt(84), 22 + r.nextInt(34));
                if (noun.equals("mangroves")) for (int i = 0; i < 6; i++) { double x = 40 + r.nextInt(430), y = 240 + r.nextInt(30); p.broad(x, y, 42, pal.leafDark); for (int t = 0; t < 4; t++) p.stroke(x, y - 14, x + (t - 1.5) * 14, y + 8, 2, pal.trunk, 0.8); }
                p.mist(186, 26, 0.35);
                break;
            }
            case "volcano": {
                p.sky(160); p.sun(120, 70, 24);
                List<double[]> v = poly(30, 296, 200, 112, 234, 96, 282, 96, 318, 112, 486, 296);
                p.rock(v, c(90, 76, 76));
                p.washCircle(258, 102, 34, 8, c(244, 136, 64), 6, 0.95, 0.1);
                for (int i = 0; i < 3; i++) p.stroke(256, 104, 238 + i * 16, 152, 3, c(244, 120, 56), 0.85);
                for (int i = 0; i < 10; i++) p.washCircle(244 - i * 9 + r.nextInt(26), 84 - i * 8, 26 + i * 6, 16 + i * 3, c(96, 86, 92), 3, 0.3, 0.2);
                p.mist(246, 28, 0.35);
                break;
            }
            default: { // spring
                p.sky(150); p.mountainLayers(128, 3, 92, false);
                p.washRect(-10, 188, DW + 20, 120, mix(pal.ground, pal.rock, 0.3), 8, 0.95, 0.05);
                int np = p.o.count > 0 ? Math.min(p.o.count, 10) : 5;
                double psc = np > 5 ? 0.62 : 1.0;
                for (int i = 0; i < np; i++) { double x = 40 + i * (np <= 5 ? 96 : 400.0 / np) + r.nextInt(24), y = (np > 5 ? 214 + (i % 3) * 22 : 236) + r.nextInt(30); p.washCircle(x + 60 * psc, y + 6, 70 * psc, 20 * psc, c(92, 84, 78), 6, 0.95, 0.08); p.washCircle(x + 60 * psc, y + 2, 60 * psc, 15 * psc, mix(pal.water, c(180, 226, 222), 0.4), 6, 0.95, 0.06); }
                for (int i = 0; i < 22; i++) p.washCircle(60 + r.nextInt(400), 170 + r.nextInt(90), 26 + r.nextInt(44), 10 + r.nextInt(16), c(255, 255, 255), 3, 0.22, 0.22);
                for (int i = 0; i < 4; i++) p.pine(16 + r.nextInt(480), 218 + r.nextInt(14), 34, pal.leafDark);
                break;
            }
        }
    }

    /** Props and motifs drawn over the finished scene, chosen by seed (so places differ) and by the place's name and level. */
    static void decorate(Painter p, String kind, String noun) {
        Random r = p.r; Opts o = p.o;
        boolean hasSky = !kind.equals("cave");
        boolean ridged = !p.ridges.isEmpty() && (kind.equals("mountains") || kind.equals("hills") || kind.equals("lake") || kind.equals("river") || kind.equals("marsh") || kind.equals("spring") || kind.equals("forest") || kind.equals("meadow"));
        boolean water = kind.equals("lake") || kind.equals("river") || kind.equals("sea") || kind.equals("islands");
        // --- features of particular nouns
        if (noun.equals("bamboo_forests")) { for (int i = 0; i < 34; i++) { double x = r.nextInt(DW), w = 4 + r.nextInt(4), top = 10 + r.nextInt(60); Color bc = mix(p.pal.leafLight, p.pal.leafDark, r.nextDouble()); p.stroke(x, 300, x + r.nextGaussian() * 3, top, w, bc, 0.85); for (int nd = 1; nd < 7; nd++) p.stroke(x - w / 2, top + nd * 38, x + w / 2, top + nd * 38, 1.6, p.pal.leafDark, 0.7); for (int lf = 0; lf < 4; lf++) p.stroke(x, top + lf * 26, x + (lf % 2 == 0 ? 1 : -1) * (14 + r.nextInt(10)), top + lf * 26 + 8, 1.8, bc, 0.75); } }
        if (noun.equals("cherry_blossom_groves") || noun.equals("orchards")) { for (int i = 0; i < 160; i++) p.washCircle(r.nextInt(DW), 150 + r.nextInt(120), 4 + r.nextInt(8), 3 + r.nextInt(6), noun.equals("orchards") ? c(236, 150, 90) : c(244, 176, 196), 2, 0.6, 0.2); }
        if (noun.equals("lotus_ponds")) { for (int i = 0; i < 16; i++) { double x = 40 + r.nextInt(432), y = 190 + r.nextInt(80); p.washCircle(x, y, 10 + r.nextInt(8), 3 + r.nextInt(2), p.pal.leafDark, 3, 0.85, 0.1); if (i % 3 == 0) p.washCircle(x + 2, y - 3, 4, 3.5, c(248, 190, 210), 3, 0.95, 0.1); } }
        if (noun.equals("geysers")) { for (int i = 0; i < 3; i++) { double x = 90 + i * 150 + r.nextInt(40); for (int k = 0; k < 9; k++) p.washCircle(x + r.nextGaussian() * 8, 250 - k * 22, 10 + k * 3, 14 + k * 2, c(250, 250, 250), 3, 0.5, 0.2); } }
        if (noun.equals("aurora_plains")) { for (int b = 0; b < 3; b++) { Color ac = b == 1 ? c(150, 120, 230) : c(110, 230, 170); for (int s = 0; s < 40; s++) { double x = s * 13, y = 40 + b * 18 + Math.sin(s * 0.4 + b) * 16; p.stroke(x, y, x + 14, y + Math.sin((s + 1) * 0.4 + b) * 16 - Math.sin(s * 0.4 + b) * 16, 14, ac, 0.16); } } }
        if (noun.equals("kelp_forests")) { for (int i = 0; i < 40; i++) { double x = r.nextInt(DW), hgt = 80 + r.nextInt(130); for (int s = 0; s < 8; s++) p.stroke(x + Math.sin(s * 0.9 + i) * 6, 296 - s * hgt / 8, x + Math.sin((s + 1) * 0.9 + i) * 6, 296 - (s + 1) * hgt / 8, 4, mix(c(70, 110, 60), c(120, 100, 50), r.nextDouble()), 0.7); } }
        // --- mood words from the name
        java.util.Set<String> m = o.moods;
        if (hasSky && (m.contains("dark") || m.contains("night"))) { p.moon(60 + r.nextInt(390), 40 + r.nextInt(30), 15 + r.nextInt(8), c(238, 238, 222)); }
        if (hasSky && m.contains("blood")) p.moon(300 + r.nextInt(150), 52, 22, c(190, 50, 46));
        if (m.contains("dark") || m.contains("night") || m.contains("blood")) { for (int i = 0; i < 2 + r.nextInt(3); i++) p.deadTree(20 + r.nextInt(470), 262 + r.nextInt(22), 40 + r.nextInt(22)); }
        if (m.contains("fire")) { p.washRect(-10, 120, DW + 20, 130, c(255, 120, 50), 6, 0.2, 0.15); p.embers(40); }
        if (m.contains("ice")) p.flakes(90);
        if (hasSky && m.contains("holy")) { p.lightRays(60 + r.nextInt(390), 6, 5, 0.22); }
        if (m.contains("toxic")) p.spores(14);
        if (m.contains("lush")) p.flowers(110);
        if (m.contains("haze")) { p.mist(170, 40, 0.4); p.mist(230, 40, 0.35); }
        if (m.contains("vast") && ridged) { int[] rg = p.ridges.get(0); int x = 40 + r.nextInt(430); p.figure(x, rg[x] + 1, 0.45); }
        // --- level: spirit glow and floating islets
        if (hasSky && o.level >= 40) {
            double st = Math.min(0.28, 0.06 + (o.level - 40) / 300.0);
            p.lightRays(240 + r.nextInt(60), -10, 3, st);
            if (o.level >= 55) for (int i = 0; i < 1 + (o.level >= 75 ? 2 : 0) + r.nextInt(2); i++) p.floatingIslet(40 + r.nextInt(430), 40 + r.nextInt(60), 36 + r.nextInt(40));
        }
        // --- a building on a ridge
        if (ridged && r.nextInt(100) < 38) {
            int idx = Math.max(0, p.ridges.size() - 2 + r.nextInt(2)); idx = Math.min(idx, p.ridges.size() - 1);
            int[] rg = p.ridges.get(idx);
            int x = 50 + r.nextInt(DW - 100);
            double sc = 0.55 + 0.35 * idx / Math.max(1, p.ridges.size() - 1);
            if (r.nextBoolean()) p.pagoda(x, rg[x] + 3, sc); else p.pavilion(x, rg[x] + 3, sc * 1.1);
        }
        // --- sky and water life
        if (hasSky && r.nextInt(100) < 42) { if (r.nextInt(3) == 0) { int n = 2 + r.nextInt(3); for (int i = 0; i < n; i++) p.crane(120 + r.nextInt(280) + i * 26, 40 + r.nextInt(50) + i * 6, 1.0 + r.nextDouble() * 0.5); } else p.birds(120 + r.nextInt(280), 40 + r.nextInt(60), 3 + r.nextInt(5), 1.0); }
        if (water && r.nextInt(100) < 45) p.boat(60 + r.nextInt(380), kind.equals("river") ? 240 + r.nextInt(18) : 215 + r.nextInt(50), 0.9 + r.nextDouble() * 0.5);
        if ((kind.equals("meadow") || kind.equals("hills")) && r.nextInt(100) < 30) p.figure(60 + r.nextInt(380), 252 + r.nextInt(24), 0.9);
    }

    static List<double[]> poly(double... xy) { List<double[]> l = new ArrayList<>(); for (int i = 0; i + 1 < xy.length; i += 2) l.add(new double[]{xy[i], xy[i + 1]}); return l; }

    // ------------------------------------------------------------------ public API
    /** A new picture for `noun` in `biome`. Same seed = same picture. `w`/`h` are the output size; `quality` 1.0 = full detail. */
    public static BufferedImage render(String noun, String biome, long seed, int w, int h, double quality) { return render(noun, biome, seed, w, h, quality, null); }

    /** Same, with what the place's name says (number, level, mood words). */
    public static BufferedImage render(String noun, String biome, long seed, int w, int h, double quality, Opts opts) {
        String n = slug(noun == null ? "" : noun);
        String kind = KIND.getOrDefault(n, "hills");
        Random r = new Random(seed * 0x9E3779B97F4A7C15L + n.hashCode());
        double q = Math.max(0.3, Math.min(1.0, quality));
        int ss = q >= 0.8 ? 2 : 1; // paint at double size, filter, then shrink: finer strokes and softer edges
        BufferedImage im = new BufferedImage(w * ss, h * ss, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = im.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setColor(c(238, 231, 214)); g.fillRect(0, 0, w * ss, h * ss);
        g.scale(w * ss / (double) DW, h * ss / (double) DH);
        Pal pal = palette(biome, r);
        Painter painter = new Painter(g, r, q, pal, n, opts);
        int st = opts == null ? 0 : opts.settlement;
        if (st == 3) kind = "capital";
        scene(painter, kind, n);
        if (st != 3) decorate(painter, kind, n);
        if (st == 1 || st == 2) ArtCity.skyline(painter, st);
        if (opts != null && opts.sect && st != 3) ArtCity.sectHall(painter);
        ArtCity.finish(painter, kind);
        g.dispose();
        im = ss == 2 ? shrink(kuwahara(im, 3), w, h) : blur(im);
        paper(im, r);
        return im;
    }

    /** Kuwahara oil-paint filter (summed-area tables, so O(pixels)): each pixel takes the mean colour of the calmest of the four windows around it,
     *  which flattens texture into brush-like patches while keeping the edges between them sharp. */
    static BufferedImage kuwahara(BufferedImage src, int rad) {
        int w = src.getWidth(), h = src.getHeight(), W = w + 1;
        int[] px = src.getRGB(0, 0, w, h, null, 0, w);
        long[] sr = new long[W * (h + 1)], sg = new long[W * (h + 1)], sb = new long[W * (h + 1)], sl = new long[W * (h + 1)], sq = new long[W * (h + 1)];
        for (int y = 0; y < h; y++) {
            long rr = 0, gg = 0, bb = 0, ll = 0, qq = 0;
            for (int x = 0; x < w; x++) {
                int c = px[y * w + x], r = c >> 16 & 255, g = c >> 8 & 255, b = c & 255, l = (r * 3 + g * 6 + b) / 10;
                rr += r; gg += g; bb += b; ll += l; qq += (long) l * l;
                int i = (y + 1) * W + x + 1, up = y * W + x + 1;
                sr[i] = sr[up] + rr; sg[i] = sg[up] + gg; sb[i] = sb[up] + bb; sl[i] = sl[up] + ll; sq[i] = sq[up] + qq;
            }
        }
        int[] out = new int[w * h];
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            double best = Double.MAX_VALUE; int bc = px[y * w + x];
            for (int q = 0; q < 4; q++) {
                int x0 = (q & 1) == 0 ? x - rad : x, x1 = (q & 1) == 0 ? x : x + rad, y0 = (q & 2) == 0 ? y - rad : y, y1 = (q & 2) == 0 ? y : y + rad;
                x0 = Math.max(0, x0); y0 = Math.max(0, y0); x1 = Math.min(w - 1, x1); y1 = Math.min(h - 1, y1);
                int a = (y1 + 1) * W + x1 + 1, b = y0 * W + x1 + 1, c2 = (y1 + 1) * W + x0, d = y0 * W + x0;
                double n = (double) (x1 - x0 + 1) * (y1 - y0 + 1);
                double mean = (sl[a] - sl[b] - sl[c2] + sl[d]) / n, var = (sq[a] - sq[b] - sq[c2] + sq[d]) / n - mean * mean;
                if (var < best) { best = var; bc = (int) ((sr[a] - sr[b] - sr[c2] + sr[d]) / n) << 16 | (int) ((sg[a] - sg[b] - sg[c2] + sg[d]) / n) << 8 | (int) ((sb[a] - sb[b] - sb[c2] + sb[d]) / n); }
            }
            out[y * w + x] = bc;
        }
        BufferedImage res = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        res.setRGB(0, 0, w, h, out, 0, w);
        return res;
    }

    private static BufferedImage shrink(BufferedImage im, int w, int h) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(im, 0, 0, w, h, null);
        g.dispose();
        return out;
    }

    private static BufferedImage blur(BufferedImage im) {
        float[] k = {1 / 16f, 2 / 16f, 1 / 16f, 2 / 16f, 4 / 16f, 2 / 16f, 1 / 16f, 2 / 16f, 1 / 16f};
        return new ConvolveOp(new Kernel(3, 3, k), ConvolveOp.EDGE_NO_OP, null).filter(im, null);
    }

    /** Paper fibre + grain, a soft vignette and a small red seal in the corner. */
    static void paper(BufferedImage im, Random r) {
        int w = im.getWidth(), h = im.getHeight();
        int[] px = im.getRGB(0, 0, w, h, null, 0, w);
        double fx = r.nextDouble() * 10;
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int c = px[y * w + x];
            double fiber = Math.sin(x * 0.35 + Math.sin(y * 0.11 + fx) * 3) * 1.6 + Math.sin(y * 0.6 + x * 0.05) * 1.1;
            double n = (r.nextDouble() - 0.5) * 11 + fiber;
            double dx = (x - w / 2.0) / (w / 2.0), dy = (y - h / 2.0) / (h / 2.0), vig = 1 - 0.16 * (dx * dx + dy * dy);
            int rr = cl(((c >> 16 & 255) + n) * vig), gg = cl(((c >> 8 & 255) + n) * vig), bb = cl(((c & 255) + n * 0.9) * vig);
            px[y * w + x] = rr << 16 | gg << 8 | bb;
        }
        im.setRGB(0, 0, w, h, px, 0, w);
        Graphics2D g = im.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double s = w / (double) DW;
        int sw = Math.max(6, (int) (18 * s)), sh = Math.max(7, (int) (22 * s)), sx = w - (int) (38 * s), sy = h - (int) (40 * s);
        g.setColor(new Color(178, 52, 44, 205)); g.fillRoundRect(sx, sy, sw, sh, 3, 3);
        g.setColor(new Color(240, 226, 210, 225)); g.setStroke(new BasicStroke((float) Math.max(1, 1.5 * s)));
        g.drawLine(sx + sw / 5, sy + sh / 3, sx + sw * 4 / 5, sy + sh / 3); g.drawLine(sx + sw / 2, sy + sh / 3, sx + sw / 2, sy + sh * 4 / 5); g.drawLine(sx + sw / 5, sy + sh * 3 / 5, sx + sw * 4 / 5, sy + sh * 3 / 5);
        g.dispose();
    }

    private static int cl(double v) { return (int) Math.max(0, Math.min(255, v)); }

    // ------------------------------------------------------------------ command line (contact sheets)
    public static void main(String[] a) throws Exception {
        java.io.File out = new java.io.File(a.length > 0 ? a[0] : "artgen-out");
        out.mkdirs();
        List<String[]> jobs = new ArrayList<>();
        if (a.length >= 4) for (int i = 1; i + 2 < a.length; i += 3) jobs.add(new String[]{a[i], a[i + 1], a[i + 2]});
        else for (String n : KIND.keySet()) jobs.add(new String[]{n, "Grassland", "1"});
        long t0 = System.currentTimeMillis();
        for (String[] j : jobs) javax.imageio.ImageIO.write(render(j[0], j[1], Long.parseLong(j[2]), 512, 288, 1.0), "png", new java.io.File(out, slug(j[0]) + "_" + j[1].toLowerCase() + "_" + j[2] + ".png"));
        System.err.println("wrote " + jobs.size() + " images in " + (System.currentTimeMillis() - t0) + " ms to " + out);
    }
}
