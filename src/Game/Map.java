package Game;

import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;

public class Map {
    static final int DEFAULT_START = 100000;      // the size of a world when nobody chooses (the start prompt offers 10,000 to 150,000; see StartMenu.askPopulation)
    static int startingPopulation = 100000; // Starting Population (Cultivators): chosen at the start of a new game, saved with the world (a probe copy edits this line)
    static int population = startingPopulation;
    /** Sets the size of the world (its crowd limits follow). */
    static void setStartingPopulation(int n) { startingPopulation = Math.max(500, n); GameDriver.rescaleCrowd(); }
    //static final int range = 10 + 1; //Small
    static final int range = 30 + 1; //Medium
    //static final int range = 100 + 1; //Large

    public static Area[][] tiles = new Area[range][range];
    public static long worldSeed = Long.getLong("tdf.seed") != null ? Long.getLong("tdf.seed") * 31 + 7 : new java.util.Random().nextLong() | 1L; // seeds the terrain fields (BiomeGen); saved with the world
    public static ArrayList<Area> createdAreas = new ArrayList<>();
    public static ArrayList<Area> cityAreas = new ArrayList<>();
    public static ArrayList<Character> characters = new ArrayList<>();
    public static ArrayList<Character> tempCharacters = new ArrayList<>();
    public static ArrayList<Character> remCharacters = new ArrayList<>();
    public static ArrayList<Character> monstersList = new ArrayList<>();
    public static ArrayList<Character> awakenedMonsters = new ArrayList<>();
    public static ArrayList<Area> hotspotAreas = new ArrayList<>();
    public static Character currentCharacter;
    public static volatile boolean heavenExists = false; // a Heaven-Sent Treasure lies unclaimed somewhere (refreshed at the start of every tick; gates the per-character quest scan)
    public static ArrayList<String> legacyTitles = new ArrayList<>();
    public static ArrayList<Integer> legacyLvls = new ArrayList<>();

    // Hero threshold flags — volatile so parallel cultivate() threads see updates immediately
    public static volatile boolean heroEnsoulment = false;
    public static volatile boolean heroAscension = false;
    public static volatile boolean heroPseudoImmortal = false;

    // Player-pinned NPC IDs shown in the tracker panel
    public static ArrayList<Integer> trackedCharacters = new ArrayList<>();

    // Lock for double-checked tile creation during parallel movement
    public static final Object tileLock = new Object();

    // Shared thread pool for parallel tick phases; sized to all available cores
    public static final ForkJoinPool tickPool =
            new ForkJoinPool(Integer.getInteger("tdf.threads", Runtime.getRuntime().availableProcessors()));

    // Synchronized add helpers — called from parallel threads in Character/Area code
    public static synchronized void addTemp(Character c)               { tempCharacters.add(c); }
    public static synchronized void addRem(Character c)                { remCharacters.add(c); }
    public static synchronized void addMonster(Character c)            { monstersList.add(c); }
    public static synchronized void addCreatedArea(Area a)             { createdAreas.add(a); }
    public static synchronized void addLegacy(String title, int lvl)   { legacyTitles.add(title); legacyLvls.add(lvl); }
    public static synchronized void addPopulation(int d)               { population += d; }
    public static synchronized void addAwakened(Character c)           { awakenedMonsters.add(c); }
    private static final ArrayList<Character> reincarnations = new ArrayList<>();
    /** A person who plays has died and waits for the reincarnation screen (resolved at the end of the tick; no need to look at every character for it). */
    public static synchronized void needReincarnation(Character c)     { reincarnations.add(c); }
    public static synchronized ArrayList<Character> takeReincarnations() { ArrayList<Character> l = new ArrayList<>(reincarnations); reincarnations.clear(); return l; }
    public static synchronized void addMonsterOnce(Character c)        { if (!monstersList.contains(c)) monstersList.add(c); }

    // ---- parallel NPC turns: what reaches beyond the tile of the turn (another person's lists, a sect, a distant city's board) waits until the turns are over
    public static volatile boolean parallelPhase = false;                // true while NPC turns of different tiles run together
    private static final java.util.concurrent.ConcurrentLinkedQueue<Runnable> deferred = new java.util.concurrent.ConcurrentLinkedQueue<>();
    /** Runs r now; during a parallel NPC phase it is kept and run on the game thread once the phase is over, in the order asked (so a one-thread game behaves exactly as before). */
    public static void later(Runnable r) { if (parallelPhase) deferred.add(r); else r.run(); }
    public static void runDeferred() {
        Runnable r;
        while ((r = deferred.poll()) != null) {
            try { r.run(); } catch (RuntimeException e) { GameDriver.parallelError(e); }
        }
    }

    /** The births (and awakened monsters) of the tick join the list: in id order, so ids stay equal to list positions however the turns that made them were interleaved. */
    public static synchronized void mergeBirths() {
        if (tempCharacters.isEmpty()) return;
        if (tempCharacters.size() > 1) tempCharacters.sort((a, b) -> Integer.compare(a.id, b.id));
        for (Character c : tempCharacters) { while (characters.size() < c.id) characters.add(null); characters.add(c); }
        population += tempCharacters.size();
        tempCharacters.clear();
    }

    public Map(){
        if (Long.getLong("tdf.seed") == null) worldSeed = new java.util.Random().nextLong() | 1L; // a new world gets its own terrain
        tiles[range / 2][range / 2] = new Area(); // Starting Point of the World
    }
}
