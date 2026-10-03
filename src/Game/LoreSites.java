package Game;

import java.util.ArrayList;
import java.util.List;

/** The places of the lore: nine tiles scattered over the world (plus the Empty Seat, which is Originis itself), each holding one piece of the truth.
 *  Where they are is decided by the world seed alone (`place`), so a world always has the same nine places in the same spots, and every tile of the
 *  right kind gets its lore name the moment it is created (`Lore.onTileCreated`) or is forced into being when a clue points at it (`ensureTile`).
 *  A human who stands on one with the right realm and has read the clue before it learns what it holds (`arrive`). NPCs walk past them. */
final class LoreSites {
    private LoreSites() { }

    /** tier: the order of understanding (a place of tier t needs `needN` places of tier t-1 read first); rule: how the terrain picks its tile;
     *  ring: wrapped Chebyshev distance from Originis; maxLvl: the most dangerous tile level (see Area's quadrant levels) it may be put on. */
    static class Site {
        final String id, title, rule, kind, where; final int tier, ringMin, ringMax, maxLvl, minMajor, needN;
        Site(String id, String title, String kind, String where, int tier, int needN, int ringMin, int ringMax, int maxLvl, int minMajor, String rule) {
            this.id = id; this.title = title; this.kind = kind; this.where = where; this.tier = tier; this.needN = needN; this.ringMin = ringMin; this.ringMax = ringMax; this.maxLvl = maxLvl; this.minMajor = minMajor; this.rule = rule; }
    }

    static final Site[] ALL = {
            new Site("wayhouse", "The Unlit Wayhouse", "a roadside inn nobody lights", "on the low ground by a river, a short walk from Originis", 1, 0, 2, 3, 12, 0, "river"),
            new Site("weaver", "The Weaver's Wall", "a wall that wraps around the land", "at the edge of the map, where it wraps back on itself", 2, 1, 6, 15, 40, 1, "seam"),
            new Site("cistern", "The Cistern of Returning", "a flooded vault of stone basins", "in low, wet ground", 2, 1, 4, 8, 30, 2, "low"),
            new Site("assayer", "The Assayer's Hall", "a hall with a giant balance and nine locks", "on high ground", 2, 1, 5, 9, 35, 2, "high"),
            new Site("shed", "The Gardener's Shed", "a shed of strange tools", "deep in a forest", 3, 2, 7, 11, 45, 3, "forest"),
            new Site("thinsky", "The Thin Sky", "a mountain peak under a cracked sky", "on the highest ground of the far country", 3, 2, 8, 13, 60, 4, "high"),
            new Site("lanternroad", "The Road of Unreturned Lanterns", "a road the dead still walk", "in the barren wastes", 3, 2, 8, 13, 60, 4, "barren"),
            new Site("menagerie", "The Menagerie of Practice", "a round hall of numbered cells", "in the far country", 4, 2, 10, 14, 75, 5, "any"),
            new Site("garrison", "The Garrison of the Crossed", "an empty fortress, as if an army just left", "at the edge of the world", 4, 2, 12, 15, 95, 6, "any"),
    };
    static final String[] FOOT_NAMES = {"The Giant's Heel", "The Glass Hand", "The Toothed Print", "The Muster Print", "Where the Giant Knelt", "The Last Print"};
    static final String[] FOOT_CLUES = {
            "A footprint wider than a courtyard, pressed into stone that never wears away. Something taller than the sky stood here and was shoved backward. A trench shows where its heel dragged. At the bottom lies a flake of cold white metal.",
            "The next print is shallower. The giant was already tired. The ground around it has melted into glass, in the shape of a hand that came down from the sky.",
            "Something with too many teeth bit the rim of this print. The bite marks are shaped like lamps. The sky above is a little paler than the sky around it.",
            "Thousands of soldiers' bootprints run toward a giant's footprint and stop at its edge. None of them goes around.",
            "A kneeling print: two knees and a palm, pressed deep. A spear stands upright in the stone beside it, and the shaft is warm.",
            "The last footprint points away from the Garrison, toward the Door. It is the only one filled with ordinary rain."};
    static final String SEAT = "seat";   // the Empty Seat: a place of the Palace itself (Originis), read by an Elder who has read the other nine

    static Site byId(String id) { for (Site s : ALL) if (s.id.equals(id)) return s; return null; }
    static int index(String id) { for (int i = 0; i < ALL.length; i++) if (ALL[i].id.equals(id)) return i; return -1; }
    static String title(String id) { Site s = byId(id); return s != null ? s.title : SEAT.equals(id) ? "The Empty Seat" : id; }

    // ------------------------------------------------------------------ where they are (deterministic from the world seed)
    static int ring(int x, int y) {
        int cx = Map.range / 2;
        int dx = Math.abs(x - cx), dy = Math.abs(y - cx);
        dx = Math.min(dx, Map.range - dx); dy = Math.min(dy, Map.range - dy);
        return Math.max(dx, dy);
    }

    /** The level Area gives a tile at (x, y) before its random 0-4: the same quadrant formula as the Area constructor. */
    static int estLvl(int x, int y) {
        int c = Map.range / 2, dx = x - c, dy = y - c;
        float q = (dx < 0 && dy < 0) ? 20f : (dx >= 0 && dy < 0) ? 40f : (dx < 0 && dy >= 0) ? 60f : 80f;
        return 1 + (int) (q * (Math.abs(dx) / (float) c + Math.abs(dy) / (float) c) / 2f);
    }

    private static double noise(long seed, int i, int x, int y) {
        long h = Naming.mix64(Naming.mix64(seed ^ (i * 0x9E3779B97F4A7C15L)) + x * 1000003L + y);
        return (h >>> 11) * (1.0 / (1L << 53));
    }

    private static double score(Site s, BiomeGen.Cell c, int x, int y) {
        switch (s.rule) {
            case "river": return (c.river ? 1.0 : 0.0) + 0.5 * c.moist + 0.3 * (1 - c.elev);
            case "low": return 1 - c.elev + 0.4 * c.moist;
            case "high": return c.elev;
            case "seam": return ((x == 0 || y == 0) ? 2.0 : 0.0) + 0.3 * c.elev;
            case "forest": return ("Forest".equals(c.major) ? 1.0 : 0.0) + 0.4 * c.moist;
            case "barren": return ("Desert".equals(c.major) || "Tundra".equals(c.major) ? 1.0 : 0.0) + 0.6 * (1 - c.moist);
            default: return 0;
        }
    }

    /** {x, y} of every site in ALL (the same order), for this world seed. Pure: nothing is created. */
    static int[][] place(long seed) {
        int R = Map.range, mid = R / 2;
        BiomeGen.Cell[][] cells = new BiomeGen.Cell[R][R];
        for (int x = 0; x < R; x++) for (int y = 0; y < R; y++) cells[x][y] = BiomeGen.cell(seed, x, y, R);
        int[][] out = new int[ALL.length][2];
        List<int[]> taken = new ArrayList<>();
        for (int i = 0; i < ALL.length; i++) {
            Site s = ALL[i];
            int[] best = null;
            for (int relax = 0; relax < 4 && best == null; relax++) {
                double bs = -1e9;
                for (int x = 0; x < R; x++) for (int y = 0; y < R; y++) {
                    int r = ring(x, y);
                    if (r < s.ringMin - relax || r > s.ringMax + relax || r < 2) continue;
                    if (estLvl(x, y) + 4 > s.maxLvl + relax * 15) continue;
                    BiomeGen.Cell c = cells[x][y];
                    if (relax < 3 && "Ocean".equals(c.major)) continue; // not under the sea (a beach is fine)
                    boolean crowded = false;
                    for (int[] t : taken) if (ring2(t[0], t[1], x, y) < 2) crowded = true;
                    if (crowded) continue;
                    double sc = score(s, c, x, y) + 0.35 * noise(seed, i, x, y);
                    if (sc > bs) { bs = sc; best = new int[]{x, y}; }
                }
            }
            if (best == null) best = new int[]{(mid + 2 + i) % R, (mid + 3) % R}; // cannot happen on a 31x31 world; keeps the table whole anyway
            out[i] = best; taken.add(best);
        }
        return out;
    }

    /** The Giant Footprints: three near the Thin Sky and three near the Garrison (battles whose remnants the Garden kept). Pure in the seed. */
    static int[][] placeFeet(long seed, int[] sx, int[] sy) {
        int R = Map.range;
        int ti = index("thinsky"), gi = index("garrison");
        int[][] out = new int[FOOT_NAMES.length][2];
        List<int[]> taken = new ArrayList<>();
        for (int i = 0; i < out.length; i++) {
            int bi = i < 3 ? ti : gi;
            int bx = sx[bi], by = sy[bi];
            double bs = -1e9; int[] best = null;
            for (int dx = -5; dx <= 5; dx++) for (int dy = -5; dy <= 5; dy++) {
                if (Math.max(Math.abs(dx), Math.abs(dy)) < 2) continue;
                int x = Math.floorMod(bx + dx, R), y = Math.floorMod(by + dy, R);
                if (ring(x, y) < 2) continue;
                boolean clash = false;
                for (int j = 0; j < sx.length; j++) if (ring2(x, y, sx[j], sy[j]) < 1) clash = true;
                for (int[] t : taken) if (ring2(t[0], t[1], x, y) < 2) clash = true;
                if (clash) continue;
                double sc = noise(seed, 60 + i, x, y);
                if (sc > bs) { bs = sc; best = new int[]{x, y}; }
            }
            if (best == null) best = new int[]{Math.floorMod(bx + 3 + i, R), Math.floorMod(by + 2, R)};
            out[i] = best; taken.add(best);
        }
        return out;
    }

    /** A tile that is one of the footprints takes its name. */
    static void applyFoot(Lore.State st, Area a) {
        if (st == null || st.footX == null || a == null || a.loreSite != null || a.city || a.isOriginis()) return;
        for (int i = 0; i < st.footX.length; i++) if (a.x == st.footX[i] && a.y == st.footY[i]) {
            a.loreSite = "foot" + i;
            if (a.geoName == null) a.geoName = a.name;
            a.name = FOOT_NAMES[i];
            a.dungeonType = null; a.dungeonCharges = 0;
            Naming.claimPlace(a.name);
            return;
        }
    }

    private static int ring2(int ax, int ay, int bx, int by) {
        int dx = Math.abs(ax - bx), dy = Math.abs(ay - by);
        return Math.max(Math.min(dx, Map.range - dx), Math.min(dy, Map.range - dy));
    }

    // ------------------------------------------------------------------ the tiles
    /** The site (index into ALL) that belongs on tile (x, y) in this world, or -1. */
    static int siteAt(Lore.State st, int x, int y) {
        if (st == null || st.siteX == null) return -1;
        for (int i = 0; i < ALL.length && i < st.siteX.length; i++) if (st.siteX[i] == x && st.siteY[i] == y) return i;
        return -1;
    }

    /** Gives a tile its lore name and marks it (called when a tile is created, and for tiles that already exist when the lore starts). */
    static void apply(Area a, Site s) {
        if (a == null || s == null || a.loreSite != null || a.city || a.isOriginis()) return;
        a.loreSite = s.id;
        if (a.geoName == null) a.geoName = a.name;
        a.name = s.title;
        a.dungeonType = null; a.dungeonCharges = 0; // a site is a place of its own, not an ordinary dungeon
        Naming.claimPlace(s.title);
    }

    /** The tile of site `i`, created now if nobody has walked there yet (the clue that points at it needs a place to point to). */
    static Area ensureTile(Lore.State st, int i) {
        int x = st.siteX[i], y = st.siteY[i];
        Area a = Map.tiles[x][y];
        if (a == null) {
            synchronized (Map.tileLock) {
                if (Map.tiles[x][y] == null) {
                    if (Map.currentCharacter == null) return null;
                    Map.tiles[x][y] = new Area(Area.ghost(x == 0 ? Map.range - 1 : x - 1, y), 'e'); // its constructor applies the site (Lore.onTileCreated)
                }
                a = Map.tiles[x][y];
            }
        }
        if (a != null && a.loreSite == null) apply(a, ALL[i]);
        return a;
    }

    // ------------------------------------------------------------------ a human arrives
    /** Called every turn a human stands on a lore tile: reads the place if they may. */
    static void arrive(Character c, Lore.PState p, Area a) {
        Site s = byId(a.loreSite);
        if (s == null || p.found.contains(s.id)) return;
        int week = GameDriver.time;
        Integer last = p.glimpsed.get(s.id);
        boolean tell = last == null || week - last >= 26;
        if (c.majorLvl < s.minMajor) {
            if (tell) { p.glimpsed.put(s.id, week); Lore.say(c, "Something is here. " + s.title + " keeps its secret from you for now: come back when you have crossed Major Lvl " + s.minMajor + "."); }
            return;
        }
        if (s.tier > 1 && Lore.foundInTier(p, s.tier - 1) < s.needN) {
            if (tell) { p.glimpsed.put(s.id, week); Lore.say(c, "You sense " + s.title + " has something to say, but you do not yet know what to ask it. Follow the Order's errands first (you need " + s.needN + " of the places before it)."); }
            return;
        }
        discover(c, p, s);
    }

    static void discover(Character c, Lore.PState p, Site s) {
        p.found.add(s.id);
        p.revealed.add(s.id);
        String reward = reward(c, s.id);
        Lore.queueScene(p, "site_" + s.id, true);
        LoreText.Clue cl = LoreText.CLUES.get(s.id);
        Notifications.post("lore", "You read " + s.title + ": " + (cl == null ? "" : cl.truth) + (reward.isEmpty() ? "" : "  (" + reward + ")"), -1, c.id);
        Chronicle.add("world", c.name + " " + c.lastName + " read " + s.title + ".", c.id);
    }

    /** What a place gives besides the truth. Returns a short description. */
    static String reward(Character c, String id) {
        switch (id) {
            case "wayhouse": Starts.givePills(c, Item.PillType.Cultivation, 1, 2); c.ss += 150; c.convertCurrency(); return "2 Qi Cultivation Pills and 150 spirit stones from the keeper's strongbox";
            case "weaver": if (c.movementTechnique != null) { c.movementTechnique.upgrade(c); c.movementTechnique.upgrade(c); } return "the footprints teach your stride: movement technique improved";
            case "cistern": Starts.givePills(c, Item.PillType.Potential, 1, 1); return "a Potential Pill, from the largest basin";
            case "assayer": {
                boolean was = c.potentialHidden; c.potentialHidden = false;
                if (c.inhibitor > 1) { c.inhibitor--; c.cultivation = (Math.pow(c.potential, 2) * Math.sqrt(c.lvl) * 10.) / (52. * Math.sqrt(c.potential) * Math.pow(2, c.inhibitor)); }
                return "the balance re-weighs you" + (was ? ": your potential is " + c.potential : "") + " and eases the weight of failed breakthroughs";
            }
            case "shed": if (c.martialTechnique != null) { c.martialTechnique.upgrade(c); c.martialTechnique.upgrade(c); } return "a seed of understanding: martial technique improved";
            case "thinsky": for (int i = 0; i < 10; i++) c.cultivate(); return "ten weeks of insight under the crack";
            case "lanternroad": Starts.givePills(c, Item.PillType.Potential, 1, 1); return "a Potential Pill, pressed into your hand by a ghost";
            case "menagerie": Starts.giveGear(c, Math.max(1, Math.min(10, c.majorLvl + 1))); return "gear from an unnumbered cell";
            case "garrison": if (c.potential < 100) c.potential++; return "the Crossed leave you their lamp oil: potential +1";
            case "seat": if (c.potential < 100) c.potential++; return "potential +1";
            default: return "";
        }
    }
}
