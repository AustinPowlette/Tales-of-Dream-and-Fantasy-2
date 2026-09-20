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
}
