package Game;

/** Coherent world generation. The world is a torus (it wraps on both axes), so biomes come from a few smooth, seamlessly tiling noise fields instead of a
 *  random walk from whichever tile was explored last: elevation (continents, coasts, highlands), moisture, temperature (warm and cold bands around the
 *  world plus cooling with height) and a ridged field for rivers. A tile's biome depends only on its position and the world seed, so the same world
 *  comes out whoever explores it first and in whatever order; neighbouring tiles agree (a beach between ocean and land, tundra before desert is
 *  impossible, rivers wind), and the place names follow the terrain (peaks on high ground, streams on rivers). Originis is always a temperate green
 *  forest. */
final class BiomeGen {
    private BiomeGen() { }

    static final double SEA = 0.22, HIGH = 0.84; // about 17% sea and 13% highland

    static class Cell { double elev, moist, temp; boolean river, shore; String major, minor; }

    private static long mix(long z) { z += 0x9E3779B97F4A7C15L; z = (z ^ (z >>> 30)) * 0xBF58476D1CE5E5B9L; z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL; return z ^ (z >>> 31); }

    private static double lattice(long seed, int ch, int ix, int iy, int cells) {
        ix = Math.floorMod(ix, cells); iy = Math.floorMod(iy, cells);
        long h = mix(seed ^ mix(ch * 1_000_003L + ix * 7919L + iy * 104729L + cells));
        return (h >>> 11) / (double) (1L << 53);
    }

    private static double smooth(double t) { return t * t * (3 - 2 * t); }

    /** Value noise on a `cells` x `cells` lattice that tiles over the world: (u, v) in [0, 1). */
    private static double noise(long seed, int ch, double u, double v, int cells) {
        double x = u * cells, y = v * cells;
        int x0 = (int) Math.floor(x), y0 = (int) Math.floor(y);
        double fx = smooth(x - x0), fy = smooth(y - y0);
        double a = lattice(seed, ch, x0, y0, cells), b = lattice(seed, ch, x0 + 1, y0, cells), c = lattice(seed, ch, x0, y0 + 1, cells), d = lattice(seed, ch, x0 + 1, y0 + 1, cells);
        return a + (b - a) * fx + (c - a) * fy + (a - b - c + d) * fx * fy;
    }

    private static double fbm(long seed, int ch, double u, double v) {
        return (noise(seed, ch, u, v, 3) * 0.50 + noise(seed, ch, u, v, 6) * 0.30 + noise(seed, ch, u, v, 12) * 0.20);
    }

    /** Stretches the narrow range of fractal noise (about 0.25-0.75) over 0-1. */
    private static double stretch(double v) { return Math.max(0, Math.min(1, (v - 0.27) / 0.46)); }

    /** Elevation of a tile (also used to find out whether a neighbour is sea). */
    private static double elevation(long seed, int x, int y, int range) {
        x = Math.floorMod(x, range); y = Math.floorMod(y, range);
        double e = stretch(fbm(seed, 0, (x + 0.5) / range, (y + 0.5) / range));
        double w = Math.max(0, 1 - Math.hypot(x - range / 2, y - range / 2) / 4.5);
        return e + (0.56 - e) * w;
    }

    static Cell cell(long seed, int x, int y, int range) {
        Cell c = new Cell();
        double u = (x + 0.5) / range, v = (y + 0.5) / range;
        double e = elevation(seed, x, y, range), m = stretch(fbm(seed, 1, u, v));
        double phase = lattice(seed, 9, 1, 1, 7);
        double t = 0.5 + 0.30 * Math.cos(2 * Math.PI * (v + phase)) + 0.28 * (stretch(fbm(seed, 2, u, v)) - 0.5);
        // Originis (the middle of the map) is always a green, temperate, low-lying forest
        int cx = range / 2, cy = range / 2;
        double dist = Math.hypot(x - cx, y - cy);
        double w = Math.max(0, 1 - dist / 4.5);
        m = m + (0.74 - m) * w; t = t + (0.50 - t) * w;
        t -= 0.30 * Math.max(0, e - 0.72); // high ground is colder
        c.elev = e; c.moist = m; c.temp = Math.max(0, Math.min(1, t));
        double rv = Math.abs(fbm(seed, 3, u, v) - 0.5);
        c.river = e > SEA + 0.07 && e < 0.80 && rv < 0.022 && dist > 1.5;
        if (c.river) c.moist = Math.min(1, c.moist + 0.15);
        if (e < SEA) { c.major = "Ocean"; c.minor = "Ocean"; return c; }
        for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) // land next to the sea is a beach
            if (elevation(seed, x + d[0], y + d[1], range) < SEA) { c.major = "Ocean"; c.minor = "Beach"; c.shore = true; c.river = false; return c; }
        double mm = c.moist, tt = c.temp;
        if (tt < 0.28) {
            if (mm < 0.35) { c.major = "Desert"; c.minor = "Frozen Desert"; }
            else if (mm < 0.55) { c.major = "Tundra"; c.minor = "Tundra"; }
            else if (mm < 0.70) { c.major = "Grassland"; c.minor = "Frozen Grassland"; }
            else { c.major = "Forest"; c.minor = "Taiga"; }
        } else if (tt < 0.62) {
            if (mm < 0.28 && tt > 0.5) { c.major = "Desert"; c.minor = "Desert"; }
            else if (mm < 0.56) { c.major = "Grassland"; c.minor = "Grassland"; }
            else { c.major = "Forest"; c.minor = "Forest"; }
        } else {
            if (mm < 0.38) { c.major = "Desert"; c.minor = "Desert"; }
            else if (mm < 0.58) { c.major = "Grassland"; c.minor = "Savanna"; }
            else { c.major = "Forest"; c.minor = "Forest"; }
        }
        return c;
    }

    /** Fills the tile's biome fields from its position (x, y must already be set). */
    static void apply(Area a) {
        Cell c = cell(Map.worldSeed, a.x, a.y, Map.range);
        a.biomeM = c.major; a.biomem = c.minor;
        a.elev = (float) c.elev; a.moist = (float) c.moist; a.temp = (float) c.temp; a.river = c.river;
    }

    // ------------------------------------------------------------------ names that follow the terrain
    private static final java.util.Set<String> HIGH_NOUNS = new java.util.HashSet<>(java.util.Arrays.asList(
            "Mountains", "Peaks", "Summits", "Ridges", "Passes", "Highlands", "Cliffs", "Plateaus", "Crags", "Gorges", "Glaciers", "Volcanoes", "Calderas", "Ravines", "Canyons", "Mesas", "Hoodoos", "Crevasses", "Ice Falls", "Waterfalls"));
    private static final java.util.Set<String> RIVER_NOUNS = new java.util.HashSet<>(java.util.Arrays.asList(
            "Rivers", "Streams", "Brooks", "Creeks", "Rapids", "Waterfalls", "Deltas", "Bayous", "Oxbow Lakes", "Dry Riverbeds", "Gorges", "Springs", "Gulches", "Valleys"));
    private static final java.util.Set<String> WET_NOUNS = new java.util.HashSet<>(java.util.Arrays.asList(
            "Marshes", "Bogs", "Ponds", "Lakes", "Wetlands", "Mangroves", "Bayous", "Lotus Ponds", "Rainforests", "Jungles", "Springs", "Oxbow Lakes", "Muskegs", "Salt Marshes", "Frozen Lakes"));
    private static final java.util.Set<String> LOW_NOUNS = new java.util.HashSet<>(java.util.Arrays.asList(
            "Pastures", "Meadows", "Fields", "Grasslands", "Prairies", "Steppes", "Plains", "Orchards", "Basins", "Hills", "Terraces", "Shrublands", "Dunes", "Salt Flats", "Lake Beds", "Dust Bowls", "Barrens", "Snowfields", "Permafrost Plains", "Tidal Flats"));

    /** A noun from `nouns` that fits the terrain: highland names on high ground, water names on rivers and wet land, flat names on lowland. */
    static String nounFor(String[] nouns, double elev, double moist, boolean river, java.util.Random r) {
        java.util.Set<String> want = null;
        double chance = 0;
        if (river) { want = RIVER_NOUNS; chance = 0.8; }
        else if (elev > HIGH) { want = HIGH_NOUNS; chance = 0.7; }
        else if (moist > 0.78) { want = WET_NOUNS; chance = 0.45; }
        else if (elev < 0.52 && elev > 0) { want = LOW_NOUNS; chance = 0.4; }
        if (want != null && r.nextDouble() < chance) {
            java.util.List<String> fit = new java.util.ArrayList<>();
            for (String n : nouns) if (want.contains(n)) fit.add(n);
            if (!fit.isEmpty()) return fit.get(r.nextInt(fit.size()));
        }
        return nouns[r.nextInt(nouns.length)];
    }

    // ------------------------------------------------------------------ a picture of the whole world (tests and the world overview)
    static int rgb(Cell c) { return rgb(c.minor, c.elev, c.river); }

    static int rgb(String minor, double elev, boolean river) {
        Cell c = new Cell(); c.minor = minor == null ? "Forest" : minor; c.elev = elev; c.river = river;
        int r, g, b;
        switch (c.minor) {
            case "Ocean": { double d = Math.max(0, Math.min(1, (SEA - c.elev) / 0.3)); r = (int) (40 - 30 * d); g = (int) (100 - 50 * d); b = (int) (170 - 60 * d); break; }
            case "Beach": r = 224; g = 208; b = 150; break;
            case "Desert": r = 214; g = 184; b = 110; break;
            case "Frozen Desert": r = 196; g = 196; b = 186; break;
            case "Tundra": r = 176; g = 188; b = 190; break;
            case "Frozen Grassland": r = 168; g = 196; b = 188; break;
            case "Taiga": r = 52; g = 98; b = 80; break;
            case "Savanna": r = 176; g = 168; b = 78; break;
            case "Grassland": r = 122; g = 168; b = 74; break;
            default: r = 40; g = 112; b = 52;
        }
        if (c.minor.equals("Ocean")) return (r << 16) | (g << 8) | b;
        double hi = Math.max(0, (c.elev - 0.58) / 0.42);
        r = (int) (r + (150 - r) * hi * 0.8); g = (int) (g + (146 - g) * hi * 0.8); b = (int) (b + (140 - b) * hi * 0.8);
        if (c.elev > 0.84) { r = (int) (r + (245 - r) * 0.6); g = (int) (g + (245 - g) * 0.6); b = (int) (b + (250 - b) * 0.6); }
        if (c.river) { r = 70; g = 130; b = 200; }
        return (Math.max(0, Math.min(255, r)) << 16) | (Math.max(0, Math.min(255, g)) << 8) | Math.max(0, Math.min(255, b));
    }
}
