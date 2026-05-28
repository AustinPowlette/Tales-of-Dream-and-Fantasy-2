package Game;

import java.util.ArrayList;

import static Game.GameDriver.*;
import static Game.Dictionary.*;
import static Game.Map.*;

public class Area {

    static int uindex = 1;
    int index;
    String name = "";
    int x;
    int y;
    boolean city = false;
    Character cityOwner;
    ArrayList<Item> backpack;
    ArrayList<Item> materialBag;
    ArrayList<Item> pillBag;
    ArrayList<Item> arrayBag;
    Organization sect;
    String biomeM = "";
    String biomem = "";
    int lvl;
    int highestLvl = 1;
    int population;
    int monsters;
    int herbMaterials;
    int metalMaterials;
    // Create an ArrayList of Structures and Create a Structure Object

    ArrayList<Character> characters = new ArrayList<Character>();
    ArrayList<Character> deadCharacters = new ArrayList<>();

    public Area() {
        name = "Originis";
        index = uindex++;
        x = range / 2;
        y = range / 2;
        city = true;
        biomeM = "Forest";
        biomem = "Forest";
        lvl = 1;
        monsters = rand.nextInt(10000000);
        herbMaterials = rand.nextInt(1000000000);
        metalMaterials = rand.nextInt(1000000000);
        population = rand.nextInt(100000000);
        backpack = new ArrayList<>();
        materialBag = new ArrayList<>();
        pillBag = new ArrayList<>();
        arrayBag = new ArrayList<>();
        createdAreas.add(this);
    }
    public Area(Area area, char dir) { // dir stands for direction. Ex: East, West, South, North
        switch (dir) {
            case 'w':
                y = area.y;
                if (area.x <= 0) {
                    x = range - 1;
                    break;
                }
                x = area.x - 1;
                break;
            case 'e':
                y = area.y;
                if (area.x >= range - 1) {
                    x = 0;
                    break;
                }
                x = area.x + 1;
                break;
            case 'n':
                x = area.x;
                if (area.y >= range - 1) {
                    y = 0;
                    break;
                }
                y = area.y + 1;
                break;
            case 's':
                x = area.x;
                if (area.y <= 0) {
                    y = range - 1;
                    break;
                }
                y = area.y - 1;
                break;
        } // Sets the Coordinate

        // Sets the Biome
        if (area.biomem.equals("Beach") || area.biomem.equals("Frozen Grassland") || area.biomem.equals("Savanna") || area.biomem.equals("Taiga") || area.biomem.equals("Frozen Desert")) {
            biomeM = area.biomeM;
            if (rand.nextInt(3) == 0) {
                biomem = area.biomeM;
            }
            else {
                biomem = area.biomem;
            }
        }
        else if (rand.nextInt(10) == 0) {
            int temp = rand.nextInt(5);
            switch (temp) {
                case 0:
                    biomeM = "Ocean";
                    if (area.biomeM.equals("Desert") || area.biomeM.equals("Grassland") || area.biomeM.equals("Forest")) {
                        biomem = "Beach";
                        break;
                    }
                    biomem = "Ocean";
                    break;
                case 1:
                    biomeM = "Grassland";
                    if (area.biomeM.equals("Desert")) {
                        biomem = "Savanna";
                        break;
                    }
                    if (area.biomeM.equals("Ocean")) {
                        biomem = "Beach";
                        break;
                    }
                    if (area.biomeM.equals("Tundra")) {
                        biomem = "Frozen Grassland";
                        break;
                    }
                    biomem = "Grassland";
                    break;
                case 2:
                    biomeM = "Forest";
                    if (area.biomeM.equals("Desert")) {
                        biomem = "Savanna";
                        break;
                    }
                    if (area.biomeM.equals("Ocean")) {
                        biomem = "Beach";
                        break;
                    }
                    if (area.biomeM.equals("Tundra")) {
                        biomem = "Taiga";
                        break;
                    }
                    biomem = "Forest";
                    break;
                case 3:
                    biomeM = "Desert";
                    if (area.biomeM.equals("Ocean")) {
                        biomem = "Beach";
                        break;
                    }
                    if (area.biomeM.equals("Grassland")) {
                    biomem = "Savanna";
                    break;
                    }
                    if (area.biomeM.equals("Forest")) {
                    biomem = "Savanna";
                    break;
                    }
                    if (area.biomeM.equals("Tundra")) {
                    biomem = "Frozen Desert";
                    break;
                    }
                    biomem = "Desert";
                    break;
                case 4:
                    biomeM = "Tundra";
                    if (area.biomeM.equals("Grassland")) {
                        biomem = "Frozen Grassland";
                        break;
                    }
                    if (area.biomeM.equals("Forest")) {
                        biomem = "Taiga";
                        break;
                    }
                    if (area.biomeM.equals("Desert")) {
                        biomem = "Frozen Desert";
                        break;
                    }
                    biomem = "Tundra";
                    break;
            }
        }
        else {
            biomeM = area.biomeM;
            biomem = area.biomem;
        }

        // Indexing the Area
        index = uindex++;

        // Naming The Area
        if (rand.nextInt(5) == 0) {
            name = name.concat(numbers[rand.nextInt(numbers.length)] + " ");
        }
        if (rand.nextInt(1) == 0) {
            name = name.concat(adjectivesPlaces[rand.nextInt(adjectivesPlaces.length)] + " ");
        }

        switch (biomem) {
            case "Ocean":
                name = name.concat(ocean[rand.nextInt(ocean.length)]);
                break;
            case "Grassland":
                name = name.concat(grassland[rand.nextInt(grassland.length)]);
                break;
            case "Forest":
                name = name.concat(forest[rand.nextInt(forest.length)]);
                break;
            case "Desert":
                name = name.concat(desert[rand.nextInt(desert.length)]);
                break;
            case "Tundra":
                name = name.concat(tundra[rand.nextInt(tundra.length)]);
                break;
            case "Beach":
                name = name.concat(beach[rand.nextInt(beach.length)]);
                break;
            case "Frozen Grassland":
                name = name.concat(frozenGrassland[rand.nextInt(frozenGrassland.length)]);
                break;
            case "Frozen Desert":
                name = name.concat(frozenDesert[rand.nextInt(frozenDesert.length)]);
                break;
            case "Taiga":
                name = name.concat(taiga[rand.nextInt(taiga.length)]);
                break;
            case "Savanna":
                name = name.concat(savanna[rand.nextInt(savanna.length)]);
                break;
        }

        if (rand.nextInt(5) == 0) {
            name = name.concat(" of " + adjectiveOf[rand.nextInt(adjectiveOf.length)]);
        }
        lvl = currentCharacter.lvl;
        monsters = rand.nextInt((startingPopulation * 10) - startingPopulation + 1) + startingPopulation;
        herbMaterials = rand.nextInt((startingPopulation * 10000) - startingPopulation + 1) + startingPopulation;
        metalMaterials = rand.nextInt((startingPopulation * 1000) - startingPopulation + 1) + startingPopulation;
        createdAreas.add(this);
    }

    public void age() {
        monsters *= 1.01;
        herbMaterials *= 1.01;
        metalMaterials *= 1.01;
        population *= 1.01;
        if (population > 0 && rand.nextInt(10) == 0) {
            Character temp = new Character(this);
            if (rand.nextInt(2) == 0 && sect != null) {
                sect.join(temp);
            }
            tempCharacters.add(temp);
        }
        if (population > 0 && cityOwner != null) {
            cityOwner.ss += population;
        }
    }

    public String toString() {
        return (" is in the " + name + " at x: " + x * 100 + ", y: " + y * 100);
        //return (currentCharacter + " is in the " + name + " at x: " + x * 100 + ", y: " + y * 100 + " Lvl: " + lvl + " animalCount: " + animals + " monsterCount: " + monsters + " spiritStoneCount: " + spiritStoneMaterials + " herbMaterialCount: " + herbMaterials + " metalMaterialCount: " + metalMaterials);
    }
}

