package Game;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/** Place art for the GUI. Place names are `[number] [adjective] NOUN [of X]`; the NOUN (and the biome) picks a hand-made image from
 *  images/places/, the adjectives / "of X" / season / level / hotspots recolour it, and cities, sects, dungeons and Originis are drawn on
 *  top from images/overlays/. Nothing here can throw at a caller: a missing or unreadable file simply yields no image.
 *  See images/places/README.txt (written on first use) for the exact file names. */
public class PlaceImages {
    static File baseDir = new File("images");   // tests point this at a temp folder
    static final int IMG_W = 512, IMG_H = 288;  // recommended art size (16:9); any size works, images are scaled
    static volatile boolean enabled = true;
    static volatile boolean generate = true;    // paint a picture (ArtGen) for places that have no hand-made one
    static volatile long artSeed = 1;           // changes every picture; Settings keeps it (or re-rolls it every launch)

    // ------------------------------------------------------------------ name parsing
    static class Parsed { String number = "", adjective = "", noun = "", of = ""; }

    private static List<String> nounsByLength = null;

    static synchronized List<String> allNouns() {
        if (nounsByLength == null) {
            Set<String> s = new TreeSet<>();
            for (String[] arr : new String[][]{Dictionary.ocean, Dictionary.grassland, Dictionary.forest, Dictionary.desert, Dictionary.tundra,
                    Dictionary.beach, Dictionary.frozenGrassland, Dictionary.frozenDesert, Dictionary.taiga, Dictionary.savanna})
                for (String n : arr) s.add(n);
            List<String> l = new ArrayList<>(s);
            l.sort((a, b) -> b.length() - a.length());
            nounsByLength = l;
        }
        return nounsByLength;
    }

    /** Splits a generated place name. Returns an empty noun for special names (Originis) and anything unrecognised. */
    /** The name a picture is chosen from: the tile's geographic name (a city or dungeon has its own display name). */
    static String geo(Area a) { return a.geoName != null ? a.geoName : a.name; }

    private static List<String> numbersLongFirst = null;
    private static synchronized List<String> numbersLongFirst() {
        if (numbersLongFirst == null) { List<String> l = new ArrayList<>(java.util.Arrays.asList(Dictionary.numbers)); l.sort((x, y) -> y.length() - x.length()); numbersLongFirst = l; }
        return numbersLongFirst;
    }

    static Parsed parse(String name) {
        Parsed p = new Parsed();
        if (name == null) return p;
        String n = name.trim();
        int io = n.indexOf(" of ");
        if (io >= 0) { p.of = n.substring(io + 4).trim(); n = n.substring(0, io).trim(); }
        for (String noun : allNouns()) {
            if (n.equals(noun) || n.endsWith(" " + noun)) {
                p.noun = noun;
                String prefix = n.substring(0, n.length() - noun.length()).trim();
                for (String num : numbersLongFirst()) {
                    if (prefix.equals(num) || prefix.startsWith(num + " ")) { p.number = num; prefix = prefix.substring(num.length()).trim(); break; }
                }
                p.adjective = prefix;
                return p;
            }
        }
        return p;
    }

    static String slug(String s) {
        return s == null ? "" : s.toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
    }

    // ------------------------------------------------------------------ moods (adjective / "of X" -> colour treatment)
    static final String DARK = "dark", FIRE = "fire", ICE = "ice", HOLY = "holy", ARID = "arid", LUSH = "lush", HAZE = "haze",
            BLOOD = "blood", TOXIC = "toxic", NIGHT = "night", VAST = "vast";

    private static final Map<String, String[]> ADJ = new HashMap<>();
    private static final String[][] OF_WORDS = {
            {"blood", BLOOD}, {"fire", FIRE}, {"flame", FIRE}, {"smoke", FIRE, HAZE}, {"fog", HAZE}, {"mist", HAZE}, {"murk", HAZE}, {"mud", HAZE},
            {"dust", ARID}, {"sand", ARID}, {"desolation", ARID}, {"ash", ARID, DARK}, {"mirage", ARID, HOLY},
            {"dread", DARK}, {"death", DARK}, {"dead", DARK}, {"corpse", DARK}, {"horror", DARK}, {"nightmare", DARK}, {"phantom", DARK},
            {"shadow", DARK}, {"spirit", HOLY, NIGHT}, {"spite", DARK}, {"sorrow", DARK}, {"burden", DARK}, {"chaos", DARK},
            {"light", HOLY}, {"miracle", HOLY}, {"wonder", HOLY}, {"wisdom", HOLY}, {"vision", HOLY}, {"contemplation", HOLY}, {"ancient", HOLY},
            {"ancestor", HOLY}, {"god", HOLY}, {"thorn", LUSH}, {"web", TOXIC}, {"spine", TOXIC}, {"dream", NIGHT}, {"delusion", NIGHT},
            {"lost", NIGHT}, {"giant", VAST}, {"storm", DARK, HAZE}};
    private static Map<String, String[]> overrides = new HashMap<>();
    private static long overridesStamp = -1;

    static {
        put(new String[]{DARK}, "Abysmal", "Baleful", "Cursed", "Dark", "Dire", "Dread", "Dreaded", "Dreadful", "Eerie", "Eldritch", "Fell", "Forbidden",
                "Forsaken", "Gloomy", "Grim", "Haunted", "Shadowy", "Treacherous", "Twisted", "Spiteful", "Chaotic", "Chaos", "Barbarous");
        put(new String[]{FIRE}, "Burning", "Fiery", "Scorching", "Sweltering", "Hot", "Smokey");
        put(new String[]{FIRE, DARK}, "Infernal");
        put(new String[]{ICE}, "Snowy", "Pale");
        put(new String[]{HOLY}, "Blessed", "Heavenly", "Glowing", "Luminescent", "Luminous", "Glittering", "Shimmering", "Sacred", "Enchanted", "Mystic",
                "Awakened", "Painted", "Sunny");
        put(new String[]{ARID}, "Arid", "Dusty", "Sandy", "Bald", "Desolate", "Rocky", "Windswept", "Jagged", "Crumbling", "Broken", "Shattered",
                "Abandoned", "Sundered", "Landlocked");
        put(new String[]{LUSH}, "Fertile", "Fragrant", "Grassy", "Bountiful", "Peaceful", "Sheltered", "New", "Tangled", "Primeval", "Primordial",
                "Jurassic", "Thorny", "Shady");
        put(new String[]{HAZE}, "Foggy", "Murky", "Musky", "Dank", "Muddy", "Flooded", "Quaking");
        put(new String[]{TOXIC}, "Toxic", "Reeking", "Fetid", "Infested", "Stinging", "Spiny");
        put(new String[]{BLOOD}, "Bloody");
        put(new String[]{NIGHT}, "Sleeping", "Sleepy", "Slumbering", "Still", "Quiet", "Lonesome", "Lone", "Hidden", "Sunless", "Delusional", "Bittersweet");
        put(new String[]{VAST}, "Deep", "Endless", "Vast", "Great", "Greater", "Grand", "Giant", "Tall", "Towering", "High", "Far", "Eternal", "Old",
                "Elder", "Last", "Uncharted", "Leaning", "Crescent", "Crooked", "Kin", "Lost", "Parlous", "Dancing");
    }

    static {
        put(new String[]{FIRE}, "Smoky", "Molten", "Boiling", "Ashen", "Ember", "Cinnabar", "Vermilion", "Scarlet", "Sun-Scorched", "Sultry");
        put(new String[]{ICE}, "Frostbitten", "Glacial", "Frozen", "Snowy", "Rime", "Pearl");
        put(new String[]{HOLY}, "Celestial", "Divine", "Hallowed", "Sacrosanct", "Immortal", "Sunlit", "Gilded", "Golden", "Heaven-Touched", "Spirit-Rich", "Qi-Dense", "Moon-Washed");
        put(new String[]{NIGHT}, "Moonlit", "Starlit", "Silent", "Whispering", "Ebon", "Obsidian", "Midnight", "Twilight", "Dusk");
        put(new String[]{DARK}, "Ghostly", "Spectral", "Nameless", "Forgotten", "Shrouded", "Veiled", "Sealed", "Demonic", "Profane", "Withered", "Weeping", "Mourning", "Drowned", "Buried", "Fallen", "Bone", "Savage");
        put(new String[]{TOXIC}, "Blighted", "Venomous", "Profane");
        put(new String[]{LUSH}, "Verdant", "Blooming", "Overgrown", "Terraced", "Bamboo", "Willow", "Lotus", "Plum", "Cherry", "Emerald", "Jade");
        put(new String[]{HAZE}, "Misty", "Cloudy", "Breezy", "Gentle", "Dawn");
        put(new String[]{ARID}, "Barren", "Parched", "Amber", "Ochre", "Sienna");
        put(new String[]{VAST}, "Highland", "Thousand-Mile", "Nine-Fold", "Outer", "Northern", "Southern", "Eastern", "Western", "Central", "Great");
    }

    private static void put(String[] moods, String... words) { for (String w : words) ADJ.put(w.toLowerCase(), moods); }

    private static void loadOverrides() {
        File f = new File(baseDir, "moods.txt");
        long stamp = f.exists() ? f.lastModified() : 0;
        if (stamp == overridesStamp) return;
        overridesStamp = stamp;
        Map<String, String[]> m = new HashMap<>();
        if (f.exists()) {
            try (java.util.Scanner sc = new java.util.Scanner(f, "UTF-8")) {
                while (sc.hasNextLine()) {
                    String line = sc.nextLine().trim();
                    int c = line.indexOf(':');
                    if (line.isEmpty() || line.startsWith("#") || c < 1) continue;
                    String[] moods = line.substring(c + 1).trim().toLowerCase().split("[,\\s]+");
                    m.put(line.substring(0, c).trim().toLowerCase(), moods.length == 1 && moods[0].isEmpty() ? new String[0] : moods);
                }
            } catch (Exception ignored) { }
        }
        overrides = m;
        cache.clear();
    }

    /** Moods for a parsed name (adjective and "of X"), honouring images/moods.txt. */
    static List<String> moods(Parsed p) {
        loadOverrides();
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (String w : p.adjective.toLowerCase().split("\\s+")) {
            if (w.isEmpty()) continue;
            String[] m = overrides.containsKey(w) ? overrides.get(w) : ADJ.get(w);
            if (m != null) for (String x : m) out.add(x);
        }
        String of = p.of.toLowerCase();
        if (!of.isEmpty()) {
            if (overrides.containsKey(of)) { for (String x : overrides.get(of)) out.add(x); }
            else for (String[] e : OF_WORDS) if (of.contains(e[0])) { for (int i = 1; i < e.length; i++) out.add(e[i]); break; }
        }
        return new ArrayList<>(out);
    }

    // ------------------------------------------------------------------ file lookup
    private static final Map<String, BufferedImage> fileCache = new LinkedHashMap<String, BufferedImage>(64, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> e) { return size() > 48; }
    };
    private static final BufferedImage NONE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    static final Set<String> missing = new TreeSet<>();          // nouns with no picture at all (required)
    static final Set<String> missingVariants = new TreeSet<>();  // <noun>_<biome> variants not made yet (optional: the plain noun picture is used)
    private static long lastMissingWrite = 0;

    private static BufferedImage load(File dir, String stem) {
        for (String ext : new String[]{".png", ".jpg", ".jpeg"}) {
            File f = new File(dir, stem + ext);
            String key = f.getPath();
            synchronized (fileCache) {
                BufferedImage c = fileCache.get(key);
                if (c != null) { if (c != NONE) return c; continue; }
            }
            if (!f.isFile()) { synchronized (fileCache) { fileCache.put(key, NONE); } continue; }
            try {
                BufferedImage img = ImageIO.read(f);
                synchronized (fileCache) { fileCache.put(key, img == null ? NONE : img); }
                if (img != null) return img;
            } catch (Exception e) {
                synchronized (fileCache) { fileCache.put(key, NONE); }
            }
        }
        return null;
    }

    /** Forget decoded files, painted pictures and missing-file bookkeeping (call after adding art or re-rolling the seed while the game runs). */
    static void reload() { synchronized (fileCache) { fileCache.clear(); } synchronized (genCache) { genCache.clear(); } synchronized (thumbCache) { thumbCache.clear(); } synchronized (smallGen) { smallGen.clear(); } cache.clear(); missing.clear(); missingVariants.clear(); overridesStamp = -1; }

    // ------------------------------------------------------------------ painted pictures (ArtGen)
    private static final Map<String, BufferedImage> genCache = new LinkedHashMap<String, BufferedImage>(64, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> e) { return size() > 96; }
    };

    /** A freshly painted picture for a place with no hand-made art. Big views are unique to the tile; small thumbnails share one picture per
     *  noun and biome (they are far too small to tell apart, and there can be hundreds of them on screen). */
    static BufferedImage painted(Area a, Parsed p, int w, int h) {
        if (!generate || (p.noun.isEmpty() && !a.isOriginis())) return null;
        boolean small = w <= 160;
        String noun = p.noun.isEmpty() ? "capital" : slug(p.noun), biome = a.biomem == null ? "" : a.biomem;
        ArtGen.Opts opts = new ArtGen.Opts(ArtGen.countOf(p.number), a.lvl, moods(p)); // the name's number ("Eight ..."), the level and the mood words shape the scene
        opts.river = a.river;
        opts.season = GameDriver.currentSeason;
        opts.weather = ArtMore.weatherAt(a);
        opts.sect = a.sect != null;
        opts.settlement = a.isOriginis() ? 3 : a.city ? (a.population >= 20000 ? 2 : 1) : 0;
        String key = (small ? "s" : String.valueOf(a.index)) + "|" + noun + "|" + biome + "|" + w + "x" + h + "|" + artSeed + "|" + opts.key();
        final Map<String, BufferedImage> gc = small ? smallGen : genCache; // the small shared pictures have a cache of their own (hundreds are on screen at once)
        synchronized (gc) { BufferedImage c = gc.get(key); if (c != null) return c; }
        long seed = small ? artSeed ^ (noun.hashCode() * 31L + biome.hashCode()) : artSeed * 1000003L + a.index;
        BufferedImage img = ArtGen.render(noun, biome, seed, w, h, small ? 0.4 : 1.0, opts);
        synchronized (gc) { gc.put(key, img); }
        return img;
    }

    /** The environment image for a place: `<noun>_<biome>` first, then `<noun>`. Null when there is none. */
    static BufferedImage base(Area a) {
        Parsed p = parse(geo(a));
        if (p.noun.isEmpty()) return null;
        File dir = new File(baseDir, "places");
        String noun = slug(p.noun), biome = slug(a.biomem);
        BufferedImage img = biome.isEmpty() ? null : load(dir, noun + "_" + biome);
        boolean variantMissing = img == null && !biome.isEmpty();
        if (img == null) img = load(dir, noun);
        boolean changed = false;
        if (img == null) changed |= missing.add(noun);
        if (variantMissing) changed |= missingVariants.add(noun + "_" + biome);
        if (changed) maybeWriteMissing();
        return img;
    }

    static BufferedImage overlay(String stem) { return load(new File(baseDir, "overlays"), stem); }

    static String dungeonStem(Area a) {
        return a.dungeonType == null ? null : "dungeon_" + a.dungeonType.name().toLowerCase();
    }

    // ------------------------------------------------------------------ rendering
    private static final Map<String, BufferedImage> cache = new LinkedHashMap<String, BufferedImage>(128, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> e) { return size() > 160; }
    };

    /** Rendered image for a place at the given size (environment + moods + season/level/hotspot tints + overlays), or null if the game has no
     *  art for it. Cheap to call repeatedly: results are cached per place, size and state. */
    static BufferedImage render(Area a, int w, int h, boolean withOverlays) {
        if (!enabled || a == null || w < 4 || h < 4) return null;
        try {
            int season = GameDriver.currentSeason;
            String key = a.index + "|" + a.name + "|" + a.biomem + "|" + w + "x" + h + "|" + season + "|" + a.hotspot + "|" + a.crowdHotspot + "|" + (a.lvl / 10)
                    + "|" + withOverlays + "|" + a.city + a.sect + a.dungeonType + "|" + (a.sect != null) + "|" + ArtMore.weatherAt(a);
            synchronized (cache) { BufferedImage c = cache.get(key); if (c != null) return c == NONE ? null : c; }
            BufferedImage out = build(a, w, h, withOverlays);
            synchronized (cache) { cache.put(key, out == null ? NONE : out); }
            return out;
        } catch (Throwable t) {
            return null;
        }
    }

    // ------------------------------------------------------------------ map thumbnails
    /** `true` while the map is drawn into an export picture (HelpShots): then every picture is built on the spot instead of in the background. */
    static volatile boolean thumbSync = false;
    /** The map sets this to repaint itself; called (at most every 150 ms, on the Swing thread) when pictures were built in the background. */
    static volatile Runnable thumbsReady = null;
    private static final java.util.concurrent.atomic.AtomicBoolean thumbNotify = new java.util.concurrent.atomic.AtomicBoolean();
    private static final java.util.Set<String> thumbPending = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static final java.util.concurrent.ExecutorService thumbPool = java.util.concurrent.Executors.newSingleThreadExecutor(r -> { Thread t = new Thread(r, "map-pictures"); t.setDaemon(true); t.setPriority(Thread.MIN_PRIORITY + 1); return t; });
    private static final Map<String, BufferedImage> thumbCache = new LinkedHashMap<String, BufferedImage>(256, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> e) { return size() > 1600; }
    };
    private static final Map<String, BufferedImage> smallGen = new LinkedHashMap<String, BufferedImage>(256, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> e) { return size() > 900; }
    };

    /** A small picture of a place for a map cell, or null while it is not ready. The map's pictures are kept for the whole map (the general cache holds only 160, so 961 tiles used to be
     *  rebuilt on every redraw, on the game's own tick) and are built one by one on a background thread: a redraw never waits for painting; the map is repainted when some are ready.
     *  The size grows with the cell (zoomed maps get sharper pictures). Null too when the place has no picture. */
    static BufferedImage thumb(Area a, int cell) {
        if (!enabled || a == null) return null;
        final int w = cell < 48 ? 96 : cell < 96 ? 160 : 224, h = w * 9 / 16;
        try {
            final String key = a.index + "|" + a.name + "|" + w + "|" + GameDriver.currentSeason + "|" + a.hotspot + a.crowdHotspot + "|" + (a.lvl / 10) + "|" + a.city + (a.sect != null) + a.dungeonType + "|" + ArtMore.weatherAt(a);
            synchronized (thumbCache) { BufferedImage c = thumbCache.get(key); if (c != null) return c == NONE ? null : c; }
            if (thumbSync) {
                BufferedImage out = build(a, w, h, true);
                synchronized (thumbCache) { thumbCache.put(key, out == null ? NONE : out); }
                return out;
            }
            if (thumbPending.add(key)) {
                final Area area = a;
                thumbPool.execute(() -> {
                    try {
                        BufferedImage out = build(area, w, h, true);
                        synchronized (thumbCache) { thumbCache.put(key, out == null ? NONE : out); }
                    } catch (Throwable t) {
                        synchronized (thumbCache) { thumbCache.put(key, NONE); }
                    } finally {
                        thumbPending.remove(key);
                        notifyThumbs();
                    }
                });
            }
            return null;
        } catch (Throwable t) {
            return null;
        }
    }

    private static void notifyThumbs() {
        if (!thumbNotify.compareAndSet(false, true)) return; // a repaint is already on its way
        javax.swing.SwingUtilities.invokeLater(() -> {
            javax.swing.Timer t = new javax.swing.Timer(150, e -> { thumbNotify.set(false); Runnable r = thumbsReady; if (r != null) r.run(); });
            t.setRepeats(false); t.start();
        });
    }

    /** Test hook: true when nothing is waiting to be built in the background. */
    static boolean thumbsIdle() { return thumbPending.isEmpty(); }

    private static BufferedImage build(Area a, int w, int h, boolean withOverlays) {
        BufferedImage base = base(a);
        boolean madeUp = false;
        if (base == null) { base = painted(a, parse(geo(a)), w, h); madeUp = base != null; }
        List<BufferedImage> overlays = new ArrayList<>();
        if (withOverlays) {
            if (a.isOriginis()) { BufferedImage o = overlay("originis"); if (o == null) o = overlay("city"); if (o != null) overlays.add(o); }
            else if (a.city) { BufferedImage o = overlay("city"); if (o != null) overlays.add(o); }
            if (a.sect != null) { BufferedImage o = overlay("sect"); if (o != null) overlays.add(o); }
            String ds = dungeonStem(a);
            if (ds != null) { BufferedImage o = overlay(ds); if (o != null) overlays.add(o); }
        }
        if (base == null && overlays.isEmpty()) return null;

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        if (base == null) {
            g.setPaint(new java.awt.GradientPaint(0, 0, new Color(38, 46, 70), 0, h, new Color(14, 16, 26)));
            g.fillRect(0, 0, w, h);
        } else if (madeUp) {
            g.drawImage(base, 0, 0, w, h, null); // already unique to this place
        } else {
            // per-place variety from a handful of pictures: mirror, crop window and a touch of brightness, all seeded by the tile
            Random r = new Random(a.index * 7919L + 17);
            boolean flip = r.nextBoolean();
            double crop = 0.86 + r.nextDouble() * 0.14;
            int sw = (int) (base.getWidth() * crop), sh = (int) (base.getHeight() * crop);
            int sx = (int) ((base.getWidth() - sw) * r.nextDouble()), sy = (int) ((base.getHeight() - sh) * r.nextDouble());
            g.drawImage(base, flip ? w : 0, 0, flip ? 0 : w, h, sx, sy, sx + sw, sy + sh, null);
            g.dispose();
            adjust(img, 0.94 + r.nextDouble() * 0.12, 1.0);
            g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }
        g.dispose();

        // colour treatment (environment only)
        Parsed p = parse(geo(a));
        for (String m : moods(p)) applyMood(img, m);
        applySeason(img, GameDriver.currentSeason);
        applyLevel(img, a.lvl);
        if (a.crowdHotspot) tint(img, new Color(225, 30, 30), 0.30);
        else if (a.hotspot) tint(img, new Color(255, 150, 20), 0.16);

        // overlays on top, untouched by the tints
        g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int n = overlays.size();
        for (int i = 0; i < n; i++) {
            BufferedImage o = overlays.get(i);
            int oh = (int) (h * (n == 1 ? 0.72 : 0.6)), ow = Math.max(1, o.getWidth() * oh / Math.max(1, o.getHeight()));
            if (ow > w * 0.9) { ow = (int) (w * 0.9); oh = Math.max(1, o.getHeight() * ow / Math.max(1, o.getWidth())); }
            int cx = n == 1 ? w / 2 : (int) (w * (i + 1) / (n + 1.0));
            g.drawImage(o, cx - ow / 2, h - oh - (int) (h * 0.04), ow, oh, null);
        }
        g.dispose();
        return img;
    }

    // ------------------------------------------------------------------ pixel treatments
    static void applyMood(BufferedImage img, String mood) {
        switch (mood) {
            case DARK:  adjust(img, 0.62, 0.7); vignette(img, 0.55); break;
            case FIRE:  tint(img, new Color(255, 90, 20), 0.28); adjust(img, 1.05, 1.1); break;
            case ICE:   tint(img, new Color(150, 200, 255), 0.30); adjust(img, 1.05, 0.8); break;
            case HOLY:  adjust(img, 1.16, 1.05); tint(img, new Color(255, 245, 200), 0.14); glow(img); break;
            case ARID:  adjust(img, 1.0, 0.6); tint(img, new Color(210, 170, 110), 0.25); break;
            case LUSH:  adjust(img, 1.0, 1.3); tint(img, new Color(60, 150, 60), 0.10); break;
            case HAZE:  softBlur(img); tint(img, new Color(205, 205, 215), 0.35); break;
            case BLOOD: tint(img, new Color(180, 10, 10), 0.30); break;
            case TOXIC: tint(img, new Color(120, 200, 60), 0.22); adjust(img, 0.95, 0.9); break;
            case NIGHT: tint(img, new Color(30, 40, 90), 0.35); adjust(img, 0.75, 0.9); break;
            case VAST:  vignette(img, 0.3); break;
            default: break;
        }
    }

    static void applySeason(BufferedImage img, int season) {
        switch (season) {
            case 0: tint(img, new Color(255, 200, 220), 0.06); adjust(img, 1.0, 1.08); break;   // spring
            case 1: tint(img, new Color(255, 230, 140), 0.10); adjust(img, 1.05, 1.0); break;   // summer
            case 2: tint(img, new Color(220, 120, 40), 0.16); break;                            // autumn
            default: tint(img, new Color(200, 220, 255), 0.22); adjust(img, 1.02, 0.75); break; // winter
        }
    }

    static void applyLevel(BufferedImage img, int lvl) {
        double a = Math.min(0.35, Math.max(0, lvl) / 300.0);
        if (a > 0.01) { tint(img, new Color(90, 10, 30), a); adjust(img, 1.0 - Math.min(0.3, lvl / 400.0), 1.0); }
    }

    /** Blend every pixel toward `c` by `alpha`. */
    static void tint(BufferedImage img, Color c, double alpha) {
        int w = img.getWidth(), h = img.getHeight();
        int[] px = img.getRGB(0, 0, w, h, null, 0, w);
        double ia = 1 - alpha;
        for (int i = 0; i < px.length; i++) {
            int p = px[i];
            int r = (int) (((p >> 16) & 255) * ia + c.getRed() * alpha), g = (int) (((p >> 8) & 255) * ia + c.getGreen() * alpha), b = (int) ((p & 255) * ia + c.getBlue() * alpha);
            px[i] = (r << 16) | (g << 8) | b;
        }
        img.setRGB(0, 0, w, h, px, 0, w);
    }

    /** Brightness and saturation multipliers. */
    static void adjust(BufferedImage img, double brightness, double saturation) {
        int w = img.getWidth(), h = img.getHeight();
        int[] px = img.getRGB(0, 0, w, h, null, 0, w);
        float[] hsb = new float[3];
        for (int i = 0; i < px.length; i++) {
            int p = px[i];
            Color.RGBtoHSB((p >> 16) & 255, (p >> 8) & 255, p & 255, hsb);
            hsb[1] = (float) Math.min(1, hsb[1] * saturation);
            hsb[2] = (float) Math.min(1, hsb[2] * brightness);
            px[i] = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]) & 0xFFFFFF;
        }
        img.setRGB(0, 0, w, h, px, 0, w);
    }

    static void vignette(BufferedImage img, double strength) {
        int w = img.getWidth(), h = img.getHeight();
        int[] px = img.getRGB(0, 0, w, h, null, 0, w);
        double cx = w / 2.0, cy = h / 2.0, max = Math.sqrt(cx * cx + cy * cy);
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            double d = Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy)) / max;
            double f = 1 - strength * d * d;
            int p = px[y * w + x];
            px[y * w + x] = ((int) (((p >> 16) & 255) * f) << 16) | ((int) (((p >> 8) & 255) * f) << 8) | (int) ((p & 255) * f);
        }
        img.setRGB(0, 0, w, h, px, 0, w);
    }

    static void glow(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        int[] px = img.getRGB(0, 0, w, h, null, 0, w);
        double cx = w / 2.0, cy = h * 0.4, max = Math.sqrt(cx * cx + cy * cy);
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            double d = Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy)) / max;
            double add = Math.max(0, 0.22 * (1 - d));
            int p = px[y * w + x];
            int r = Math.min(255, (int) (((p >> 16) & 255) + 255 * add)), g = Math.min(255, (int) (((p >> 8) & 255) + 255 * add)), b = Math.min(255, (int) ((p & 255) + 255 * add));
            px[y * w + x] = (r << 16) | (g << 8) | b;
        }
        img.setRGB(0, 0, w, h, px, 0, w);
    }

    static void softBlur(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage small = new BufferedImage(Math.max(1, w / 4), Math.max(1, h / 4), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = small.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, small.getWidth(), small.getHeight(), null);
        g.dispose();
        g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setComposite(AlphaComposite.SrcOver.derive(0.75f));
        g.drawImage(small, 0, 0, w, h, null);
        g.dispose();
    }

    // ------------------------------------------------------------------ first-use folder + checklist
    /** Creates images/places and images/overlays with a README that lists every file name the game can use. Safe to call repeatedly. */
    static void ensureFolders() {
        try {
            File places = new File(baseDir, "places"), overlays = new File(baseDir, "overlays");
            places.mkdirs(); overlays.mkdirs();
            File readme = new File(places, "README.txt");
            if (readme.exists() && new String(java.nio.file.Files.readAllBytes(readme.toPath()), java.nio.charset.StandardCharsets.UTF_8).contains("PAINTS a new picture")) return;
            try (PrintWriter w = new PrintWriter(readme, "UTF-8")) {
                w.println("PLACE IMAGES");
                w.println("Recommended size " + IMG_W + "x" + IMG_H + " (16:9), PNG or JPG, drawn in neutral daylight: the game recolours them from the place name,");
                w.println("the season, the area level and hotspots. Missing files are fine: the game PAINTS a new picture for the place (ArtGen, see Settings).");
                w.println();
                w.println("1) One environment picture per noun in this folder, lowercase with underscores, e.g. hot_springs.png");
                w.println("   Optional biome variant, which wins over the plain one: <noun>_<biome>.png, e.g. mountains_tundra.png");
                w.println("   Biomes: ocean grassland forest desert tundra beach frozen_grassland frozen_desert taiga savanna");
                w.println("2) Overlays in ../overlays (transparent PNG works best): city.png sect.png originis.png (replaces city for the first city)");
                w.println("   dungeon_lost_ruins.png dungeon_beast_lair.png dungeon_martial_trial.png dungeon_haunted_grounds.png dungeon_celestial_trial.png");
                w.println("3) Optional ../moods.txt overrides the adjective -> colour mapping, one per line: `haunted: dark, night`");
                w.println("   Moods: " + String.join(" ", DARK, FIRE, ICE, HOLY, ARID, LUSH, HAZE, BLOOD, TOXIC, NIGHT, VAST) + " (empty list = no treatment)");
                w.println("4) _missing.txt (rewritten while you play) lists every place noun the game has shown WITHOUT a hand-made picture (the game paints one),");
                w.println("   then the biome variants you have not made yet (optional: a plain noun picture is used until you do).");
                w.println();
                w.println("Nouns the game can generate (" + allNouns().size() + "):");
                List<String> sorted = new ArrayList<>(allNouns());
                java.util.Collections.sort(sorted);
                for (String n : sorted) w.println("  " + slug(n) + ".png");
            }
        } catch (Exception ignored) { }
    }

    /** Rewrites the to-do list at most every few seconds while new gaps keep being discovered. */
    private static void maybeWriteMissing() {
        long now = System.currentTimeMillis();
        if (now - lastMissingWrite < 4000) return;
        lastMissingWrite = now;
        writeMissingList();
    }

    /** Writes images/places/_missing.txt: nouns seen with no picture (required), then biome variants not made yet (optional). */
    static void writeMissingList() {
        try {
            File dir = new File(baseDir, "places");
            if (!dir.isDirectory() || (missing.isEmpty() && missingVariants.isEmpty())) return;
            try (PrintWriter w = new PrintWriter(new File(dir, "_missing.txt"), "UTF-8")) {
                w.println("Nouns seen in this world that have no hand-made picture yet (the game paints one for each place):");
                for (String m : new ArrayList<>(missing)) w.println("  " + m + ".png");
                w.println();
                w.println("Biome variants seen without their own picture (optional: the plain noun picture is used instead):");
                for (String m : new ArrayList<>(missingVariants)) w.println("  " + m + ".png");
            }
        } catch (Exception ignored) { }
    }
}
