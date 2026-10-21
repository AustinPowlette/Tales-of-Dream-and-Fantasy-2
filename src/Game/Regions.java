package Game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Names for the big shapes of the world: seas and oceans, mountain ranges, forests, plains, deserts, rivers. They come from the terrain fields
 *  (`BiomeGen.cell`, a pure function of position and the world seed), so a region has the same name whoever explores it first and nothing is saved:
 *  the labels are rebuilt when the world seed changes. The world wraps around, so a region can cross the edge of the map. Each region also has a
 *  culture (1 = pinyin-like, 2 = western-fantasy) that colours the names of the cities founded in it. */
final class Regions {
    private Regions() { }

    private static long builtFor = Long.MIN_VALUE;
    private static int n;
    private static int[][] land, river;          // label per tile (-1 = none)
    private static String[] landName, riverName; // by label
    private static int[] landCulture;
    private static List<Object[]> labelList;              // {name, centre x, centre y, tiles}

    private static synchronized void build() {
        if (builtFor == Map.worldSeed && land != null) return;
        n = Map.range;
        BiomeGen.Cell[][] cells = new BiomeGen.Cell[n][n];
        String[][] key = new String[n][n];
        for (int x = 0; x < n; x++) for (int y = 0; y < n; y++) {
            BiomeGen.Cell c = BiomeGen.cell(Map.worldSeed, x, y, n);
            cells[x][y] = c;
            key[x][y] = c.minor.equals("Ocean") ? "Ocean" : c.minor.equals("Beach") ? null : c.elev >= 0.78 ? "Mountain" : c.minor;
        }
        land = new int[n][n];
        river = new int[n][n];
        for (int[] row : land) java.util.Arrays.fill(row, -1);
        for (int[] row : river) java.util.Arrays.fill(row, -1);
        List<String> names = new ArrayList<>(), rnames = new ArrayList<>();
        List<Object[]> labels = new ArrayList<>();
        List<Integer> cult = new ArrayList<>();
        // land, sea and mountains: 4-connected components of one key
        for (int x = 0; x < n; x++) for (int y = 0; y < n; y++) {
            if (key[x][y] == null || land[x][y] >= 0) continue;
            List<int[]> comp = flood(key, land, x, y, names.size(), false);
            String name = comp.size() >= minSize(key[x][y]) ? landName(key[x][y], comp) : null;
            names.add(name);
            if (name != null) { // the centre of the region, counted round the wrap from its first tile
                double sx = 0, sy = 0;
                int[] rep0 = comp.get(0);
                for (int[] pt : comp) { sx += wrapD(pt[0] - rep0[0]); sy += wrapD(pt[1] - rep0[1]); }
                labels.add(new Object[]{name, Math.floorMod((int) Math.round(rep0[0] + sx / comp.size()), n) + 0.0, Math.floorMod((int) Math.round(rep0[1] + sy / comp.size()), n) + 0.0, comp.size()});
            }
            cult.add(1 + (int) Math.floorMod(Map.worldSeed ^ (comp.get(0)[0] * 7919L + comp.get(0)[1] * 104729L), 2L));
        }
        // rivers: 8-connected chains of river tiles
        boolean[][] isRiver = new boolean[n][n];
        for (int x = 0; x < n; x++) for (int y = 0; y < n; y++) isRiver[x][y] = cells[x][y].river;
        for (int x = 0; x < n; x++) for (int y = 0; y < n; y++) {
            if (!isRiver[x][y] || river[x][y] >= 0) continue;
            int id = rnames.size();
            ArrayDeque<int[]> q = new ArrayDeque<>();
            List<int[]> comp = new ArrayList<>();
            q.add(new int[]{x, y});
            river[x][y] = id;
            while (!q.isEmpty()) {
                int[] p = q.poll();
                comp.add(p);
                for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) {
                    int nx = Math.floorMod(p[0] + dx, n), ny = Math.floorMod(p[1] + dy, n);
                    if (isRiver[nx][ny] && river[nx][ny] < 0) { river[nx][ny] = id; q.add(new int[]{nx, ny}); }
                }
            }
            rnames.add(comp.size() >= 3 ? riverName(comp) : null);
        }
        landName = names.toArray(new String[0]);
        riverName = rnames.toArray(new String[0]);
        landCulture = new int[cult.size()];
        for (int i = 0; i < landCulture.length; i++) landCulture[i] = cult.get(i);
        labelList = labels;
                builtFor = Map.worldSeed;
    }

    private static List<int[]> flood(String[][] key, int[][] label, int sx, int sy, int id, boolean unused) {
        ArrayDeque<int[]> q = new ArrayDeque<>();
        List<int[]> comp = new ArrayList<>();
        q.add(new int[]{sx, sy});
        label[sx][sy] = id;
        String k = key[sx][sy];
        int[][] d = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!q.isEmpty()) {
            int[] p = q.poll();
            comp.add(p);
            for (int[] dd : d) {
                int nx = Math.floorMod(p[0] + dd[0], n), ny = Math.floorMod(p[1] + dd[1], n);
                if (label[nx][ny] < 0 && k.equals(key[nx][ny])) { label[nx][ny] = id; q.add(new int[]{nx, ny}); }
            }
        }
        // the representative tile (smallest x, then y) seeds the name, so it is stable
        int[] best = comp.get(0);
        for (int[] p : comp) if (p[0] < best[0] || p[0] == best[0] && p[1] < best[1]) best = p;
        comp.set(0, best);
        return comp;
    }

    private static int minSize(String key) { return key.equals("Mountain") ? 4 : key.equals("Ocean") ? 6 : 8; }

    private static String landName(String key, List<int[]> comp) {
        int[] rep = comp.get(0);
        Random r = new Random(Naming.mix64(Naming.mix64(Map.worldSeed ^ key.hashCode()) + rep[0] * 1_000_003L + rep[1]));
        String adj = Naming.REG_ADJ[r.nextInt(Naming.REG_ADJ.length)], thing = Naming.REG_THING[r.nextInt(Naming.REG_THING.length)];
        int size = comp.size();
        int pick = r.nextInt(3);
        switch (key) {
            case "Ocean":
                if (size >= 60) return pick == 0 ? "the " + adj + " Ocean" : pick == 1 ? "the Ocean of " + thing : "the Boundless " + adj + " Sea";
                if (size >= 18) return pick == 0 ? "the " + adj + " Sea" : pick == 1 ? "the Sea of " + thing : "the " + Naming.syllables(r, 0) + " Sea";
                return pick == 0 ? "the " + adj + " Gulf" : pick == 1 ? "the Bay of " + thing : "the " + Naming.syllables(r, 0) + " Bay";
            case "Mountain":
                if (size >= 12) return pick == 0 ? "the " + adj + " Range" : pick == 1 ? "the Mountains of " + thing : "the " + Naming.syllables(r, 0) + " Range";
                return pick == 0 ? "the " + adj + " Massif" : pick == 1 ? "the Peaks of " + thing : "the " + Naming.syllables(r, 0) + " Highlands";
            case "Forest": return pick == 0 ? "the " + adj + " Forest" : pick == 1 ? "the Forest of " + thing : "the " + Naming.syllables(r, 0) + " Woods";
            case "Taiga": return pick == 0 ? "the " + adj + " Taiga" : pick == 1 ? "the " + adj + " Pinewoods" : "the " + Naming.syllables(r, 0) + " Pine Sea";
            case "Grassland": case "Frozen Grassland": return pick == 0 ? "the " + adj + " Plains" : pick == 1 ? "the Steppe of " + thing : "the " + Naming.syllables(r, 0) + " Meadows";
            case "Savanna": return pick == 0 ? "the " + adj + " Savanna" : pick == 1 ? "the Golden Veldt of " + thing : "the " + Naming.syllables(r, 0) + " Veldt";
            case "Desert": case "Frozen Desert": return pick == 0 ? "the " + adj + " Desert" : pick == 1 ? "the Sea of Sand" : "the Wastes of " + thing;
            case "Tundra": return pick == 0 ? "the " + adj + " Tundra" : pick == 1 ? "the Frozen Reach" : "the Barrens of " + thing;
            default: return null;
        }
    }

    private static String riverName(List<int[]> comp) {
        int[] rep = comp.get(0);
        for (int[] p : comp) if (p[0] < rep[0] || p[0] == rep[0] && p[1] < rep[1]) rep = p;
        Random r = new Random(Naming.mix64(Naming.mix64(Map.worldSeed ^ 0x5151) + rep[0] * 1_000_003L + rep[1]));
        String adj = Naming.REG_ADJ[r.nextInt(Naming.REG_ADJ.length)], thing = Naming.REG_THING[r.nextInt(Naming.REG_THING.length)];
        boolean great = comp.size() >= 7;
        String core = r.nextInt(3) == 0 ? "the River of " + thing : r.nextInt(2) == 0 ? "the " + Naming.syllables(r, 0) + " River" : "the " + adj + " River";
        return great && !core.contains(" of ") ? core.replace("the ", "the Great ") : core;
    }

    private static int wrapD(int d) { d = Math.floorMod(d, n); return d > n / 2 ? d - n : d; }

    /** {name, x, y, tiles} for every named land or sea region (for the painted world map). */
    static synchronized List<Object[]> labels() { build(); return new ArrayList<>(labelList); }

    // ------------------------------------------------------------------ queries
    /** "the Misty Range" for a tile (null where the region is too small to have a name). */
    static String regionOf(Area a) { return a == null ? null : regionAt(a.x, a.y); }

    static String regionAt(int x, int y) {
        if (x < 0 || y < 0) return null;
        build();
        int id = land[Math.floorMod(x, n)][Math.floorMod(y, n)];
        return id < 0 ? null : landName[id];
    }

    /** "the Jade River" when a river flows through the tile. */
    static String riverOf(Area a) { return a == null ? null : riverAt(a.x, a.y); }

    static String riverAt(int x, int y) {
        if (x < 0 || y < 0) return null;
        build();
        int id = river[Math.floorMod(x, n)][Math.floorMod(y, n)];
        return id < 0 ? null : riverName[id];
    }

    /** 1 pinyin-like, 2 western-fantasy; 0 when unknown. */
    static int culture(Area a) {
        if (a == null || a.x < 0) return 0;
        build();
        int id = land[Math.floorMod(a.x, n)][Math.floorMod(a.y, n)];
        return id < 0 ? 0 : landCulture[id];
    }

    /** One line for the place sheets: "In the Misty Range, on the Jade River", or "" when the tile has neither. */
    static String line(Area a) {
        String r = regionOf(a), v = riverOf(a);
        if (r == null && v == null) return "";
        return (r != null ? "In " + r : "") + (r != null && v != null ? ", on " : v != null ? "On " : "") + (v != null ? v : "");
    }
}
