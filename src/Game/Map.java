package Game;

import java.util.ArrayList;

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

    public Map(){
        tiles[range / 2][range / 2] = new Area(); // Starting Point of the World
    }
}
