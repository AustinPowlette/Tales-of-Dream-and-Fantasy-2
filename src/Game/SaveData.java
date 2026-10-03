package Game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Captures all static world state for serialization.
 * Static fields are not serialized by default, so we snapshot them here,
 * write this object, then restore on load.
 */
public class SaveData implements Serializable {
    static final long serialVersionUID = 1L;

    // ── Map static state ──────────────────────────────────────────────────────
    int population;
    Area[][] tiles;
    ArrayList<Character> characters;
    ArrayList<Area> createdAreas;
    ArrayList<Area> cityAreas;
    ArrayList<Character> monstersList;
    ArrayList<Area> hotspotAreas;
    ArrayList<String> legacyTitles;
    ArrayList<Integer> legacyLvls;
    boolean heroEnsoulment;
    boolean heroAscension;
    boolean heroPseudoImmortal;
    ArrayList<Integer> trackedCharacters;

    // ── Area static counter ───────────────────────────────────────────────────
    int areaUindex;

    // ── GameDriver state ──────────────────────────────────────────────────────
    int time;
    int currentSeason;
    int uid;
    List<String> historyLog;
    int tickDelayMs;
    long savedAt;          // wall-clock time of the save (0 in old saves)
    String playerSummary;  // human characters at save time, for the load menu
    int version = 2;
    int difficulty;       // 0 Story/Standard, 1 Hard, 2 Brutal
    ArrayList<Chronicle.Entry> chronicle;                 // the world chronicle (null in older saves)
    java.util.HashMap<String, Integer> deathCauses;
    java.util.HashMap<Integer, ArrayList<int[]>> bio;      // yearly life samples of human characters
    java.util.HashMap<String, String> notes;               // bookmarks / notes
    long worldSeed;                                        // terrain seed (0 in older saves: their unexplored tiles then get a fresh one)
    RunStats.Data runStats;                                // per-run statistics (null in older saves)
    Lore.State lore;      // the Palace story's state (null in older saves and in worlds without the lore)
    boolean yearlyDone;   // the yearly block for `time` had already run when this save was written (so a load must not repeat it)
}
