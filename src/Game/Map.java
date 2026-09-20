package Game;

import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;

public class Map {
    static final int startingPopulation = 100000; // Starting Population (Cultivators)
    static int population = startingPopulation;
    //static final int range = 10 + 1; //Small
    static final int range = 30 + 1; //Medium
    //static final int range = 100 + 1; //Large

    public static Area[][] tiles = new Area[range][range];
    public static ArrayList<Area> createdAreas = new ArrayList<>();
    public static ArrayList<Area> cityAreas = new ArrayList<>();
    public static ArrayList<Character> characters = new ArrayList<>();
    public static ArrayList<Character> tempCharacters = new ArrayList<>();
    public static ArrayList<Character> remCharacters = new ArrayList<>();
    public static ArrayList<Character> monstersList = new ArrayList<>();
    public static ArrayList<Character> awakenedMonsters = new ArrayList<>();
    public static ArrayList<Area> hotspotAreas = new ArrayList<>();
    public static Character currentCharacter;
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
            new ForkJoinPool(Runtime.getRuntime().availableProcessors());

    // Synchronized add helpers — called from parallel threads in Character/Area code
    public static synchronized void addTemp(Character c)               { tempCharacters.add(c); }
    public static synchronized void addRem(Character c)                { remCharacters.add(c); }
    public static synchronized void addMonster(Character c)            { monstersList.add(c); }
    public static synchronized void addCreatedArea(Area a)             { createdAreas.add(a); }
    public static synchronized void addLegacy(String title, int lvl)   { legacyTitles.add(title); legacyLvls.add(lvl); }

    public Map(){
        tiles[range / 2][range / 2] = new Area(); // Starting Point of the World
    }
}
