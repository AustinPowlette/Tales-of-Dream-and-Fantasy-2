package Game;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.PriorityQueue;

import static Game.GameDriver.*;

public class Character {
    enum Traits {
        Leader,
        Blacksmith,
        Alchemist,
        Scribe,
        Merchant,
        Ghost,
        Innovative,
        Gatherer,
        Miner,
        Friendly,
        Tailor,
        Runner,
        Cheater,
        Lucky,
        Monster,
        Beast
    }
    int id;
    String name = "";
    String lastName = "";
    String gender = "";
    boolean pregnant = false;
    boolean born = true;
    boolean dead = false;
    boolean player = false;
    boolean sparring = false;
    boolean forbidden = false;
    char movement; // 'x' = stand still  'm' = move randomly
    int skipping = 0; // How many turns a person chooses to skip
    int time = 0;
    int age = 0;
    int maxAge = 100;
    int potential = 0; // 101 -(Math.sqrt(rand.nextInt(10000) + 1)
    double cultivation = 0;
    int inhibitor = 1;
    int majorLvl = 0 ; // Numbers 0-10: 10 = True Immortal, 9 = Pseudo Immortal, 8 = Half-Immortal, 7 = Ascension, 6 = Nihility, 5 = Ensoulment, 4 = Nascent Soul, 3 = Soul Wandering, 2 = Core Formation, 1 = Foundation Stage, 0 = Qi Cultivation, -1 = Crippled
    int minorLvl = 1; // Numbers 1-10: 1-3 = Low, 4-6 = Mid, 7-9 = High, 10 = Peak
    int lvl = 1;
    int tempLvl = lvl;
    double cultivationBase = 0;
    double cultivationBaseMax = Math.pow(lvl, 3) * (((70.+1./3.) + ((11./6.) * lvl)) / 25.);
    int professionLvl = 1;
    double professionExp = 0;
    double professionExpMax = Math.pow(10, professionLvl / 10);
    long ss = 0; // Spirit Stones
    int travellingSpeed = 1;
    String affiliation = ""; // Evil, Good, Neutral
    Integer affiliationNum;
    Organization sect;
    Traits trait;
    PriorityQueue<Quest> quests;
    // Relationship variables
    Hashtable<Character, Integer> relationships;
    ArrayList<Character> relationshipsList;
    Character spouse = null;
    Character father = null;
    Character mother = null;
    ArrayList<Character> siblings;
    ArrayList<Character> children;
    Area location = null;
    ArrayList<Area> knownLocations;
    // Make a Place Class and a City Class Maybe and then make a known Place/City variable
    ArrayList<Item> backpack;
    ArrayList<Item> materialBag;
    ArrayList<Item> pillBag;
    ArrayList<Item> arrayBag;
    Item equippedWeapon = null;
    Item equippedClothing = null;
    // Technique Variables
    Technique movementTechnique;
    Technique martialTechnique;
    Technique movementTechnique2;
    Technique martialTechnique2;
    ArrayList<Technique> techniqueList;

    // Game Initialization Constructor + TownNPC Birth Constructor
    public Character(Area area) {
        //Pre-Initialization Stuff
        quests = new PriorityQueue<Quest>();
        relationships = new Hashtable();
        relationshipsList = new ArrayList<>();
        siblings = new ArrayList<>();
        children = new ArrayList<>();
        knownLocations = new ArrayList<>();
        backpack = new ArrayList<>();
        materialBag = new ArrayList<>();
        pillBag = new ArrayList<>();
        arrayBag = new ArrayList<>();
        movementTechnique = new Technique(Technique.techniqueType.Movement);
        martialTechnique = new Technique(Technique.techniqueType.Martial);
        techniqueList = new ArrayList<>();

        location = area;
        location.characters.add(this);
        knownLocations.add(location);
        id = ++UID;
        if (rand.nextInt(2) == 0) {
            name = GameDriver.maleNames[rand.nextInt(GameDriver.maleNames.length)];
            gender = "Male";
        }
        else {
            name = GameDriver.femaleNames[rand.nextInt(GameDriver.femaleNames.length)];
            gender = "Female";
        }
        lastName = GameDriver.lastNames[rand.nextInt(GameDriver.lastNames.length)];

        potential = (int)(101 - Math.sqrt(rand.nextInt(10000) + 1));
        cultivation = (Math.pow(potential,2)  * Math.sqrt(lvl) / (52. * Math.sqrt(potential)));

        switch (rand.nextInt(3)) /*Affiliation Choosing */{
            case 0:
                affiliation = "Good";
                affiliationNum = 1;
                break;
            case 1:
                affiliation = "Evil";
                affiliationNum = -1;
                break;
            case 2:
                affiliation = "Neutral";
                affiliationNum = 0;
                break;
        }
        int traitNum = rand.nextInt(1000000);
        if (traitNum == 0) {
            trait = Traits.Cheater;
        }
        else if (traitNum < 10000) {
            trait = Traits.Lucky;
        }
        else if (traitNum < 40000) {
            trait = Traits.Ghost;
        }
        else if (traitNum < 70000) {
            trait = Traits.Leader;
        }
        else if (traitNum < 100000) {
            trait = Traits.Innovative;
        }
        else if (traitNum < 200000) {
            trait = Traits.Blacksmith;
        }
        else if (traitNum < 300000) {
            trait = Traits.Alchemist;
        }
        else if (traitNum < 400000) {
            trait = Traits.Scribe;
        }
        else if (traitNum < 500000) {
            trait = Traits.Merchant;
        }
        else if (traitNum < 600000) {
            trait = Traits.Gatherer;
        }
        else if (traitNum < 700000) {
            trait = Traits.Miner;
        }
        else if (traitNum < 800000) {
            trait = Traits.Friendly;
        }
        else if (traitNum < 900000) {
            trait = Traits.Tailor;
        }
        else {
            trait = Traits.Runner;
        }
    }
    // Birth Constructor / Enemy Generator
    // Make sure to add technique inheritance later
    public Character(Character mother) {
        //Pre-Initialization Stuff
        quests = new PriorityQueue<Quest>();
        relationships = new Hashtable();
        relationshipsList = new ArrayList<>();
        siblings = new ArrayList<>();
        children = new ArrayList<>();
        knownLocations = new ArrayList<>();
        backpack = new ArrayList<>();
        materialBag = new ArrayList<>();
        pillBag = new ArrayList<>();
        arrayBag = new ArrayList<>();
        movementTechnique = new Technique(Technique.techniqueType.Movement);
        martialTechnique = new Technique(Technique.techniqueType.Martial);
        techniqueList = new ArrayList<>();

        this.mother = mother;
        this.father = mother.spouse;
        siblings.addAll(mother.children);
        siblings.addAll(father.children);
        for (Character sibling : siblings) {
            sibling.siblings.add(this);
            changeRelationship(sibling, 20);
        }
        mother.children.add(this);
        father.children.add(this);
        changeRelationship(father, 50);
        changeRelationship(mother, 50);

        location = mother.location;
        location.characters.add(this);
        knownLocations.add(location);
        id = ++UID;
        time = -40;
        mother.pregnant = true;
        born = false;
        Character temp = null;
        if (rand.nextInt(2) == 0) {
            name = GameDriver.maleNames[rand.nextInt(GameDriver.maleNames.length)];
            gender = "Male";
            temp = father;
            if (father.sect != null) {
                father.sect.join(this);
            }
        }
        else {
            name = GameDriver.femaleNames[rand.nextInt(GameDriver.femaleNames.length)];
            gender = "Female";
            temp = mother;
            if (mother.sect != null) {
                mother.sect.join(this);
            }
        }
        lastName = father.lastName;

        // Technique Inheritance
        if (temp.movementTechnique2 != null) {
            movementTechnique = new Technique(temp.movementTechnique2);
        }
        else {
            movementTechnique = new Technique(temp.movementTechnique);
        }
        if (temp.martialTechnique2 != null) {
            martialTechnique = new Technique(temp.martialTechnique2);
        }
        else {
            martialTechnique = new Technique(temp.martialTechnique);
        }

        int limiter = (mother.potential + father.potential) / 2;
        potential = (int)(101 - Math.sqrt(rand.nextInt(10000) + 1));
        if (potential < limiter) {
            potential = limiter;
        }
        cultivation = (Math.pow(potential,2)  * Math.sqrt(lvl) / (52. * Math.sqrt(potential)));

        affiliationNum = mother.affiliationNum + father.affiliationNum;
        switch (affiliationNum) /*Affiliation Choosing */{
            case -2:
                affiliation = "Evil";
                affiliationNum = -1;
                break;
            case -1:
                affiliation = "Evil";
                affiliationNum = -1;
                break;
            case 0:
                affiliation = "Neutral";
                affiliationNum = 0;
                break;
            case 1:
                affiliation = "Good";
                affiliationNum = 1;
                break;
            case 2:
                affiliation = "Good";
                affiliationNum = 1;
                break;
        }
        int traitNum = rand.nextInt(1000000);
        if (traitNum == 0) {
            trait = Traits.Cheater;
        }
        else if (traitNum < 10000) {
            trait = Traits.Lucky;
        }
        else if (traitNum < 40000) {
            trait = Traits.Ghost;
        }
        else if (traitNum < 70000) {
            trait = Traits.Leader;
        }
        else if (traitNum < 100000) {
            trait = Traits.Innovative;
        }
        else if (traitNum < 200000) {
            trait = Traits.Blacksmith;
        }
        else if (traitNum < 300000) {
            trait = Traits.Alchemist;
        }
        else if (traitNum < 400000) {
            trait = Traits.Scribe;
        }
        else if (traitNum < 500000) {
            trait = Traits.Merchant;
        }
        else if (traitNum < 600000) {
            trait = Traits.Gatherer;
        }
        else if (traitNum < 700000) {
            trait = Traits.Miner;
        }
        else if (traitNum < 800000) {
            trait = Traits.Friendly;
        }
        else if (traitNum < 900000) {
            trait = Traits.Tailor;
        }
        else {
            trait = Traits.Runner;
        }
    }
    public Character (Area area, Character cultivator) {
        location = area;
        if (rand.nextInt(2) == 0) {
            gender = "Male";
        }
        else {
            gender = "Female";
        }
        switch (rand.nextInt(3)) /*Affiliation Choosing */{
            case 0:
                affiliation = "Good";
                affiliationNum = 1;
                break;
            case 1:
                affiliation = "Evil";
                affiliationNum = -1;
                break;
            case 2:
                affiliation = "Neutral";
                affiliationNum = 0;
                break;
        }

        name = adjectivesEntities[rand.nextInt(adjectivesEntities.length)];
        lastName = monsters[rand.nextInt(monsters.length)];
        lvl = rand.nextInt(location.lvl) + 1;
        tempLvl = lvl;
        majorLvl = lvl/10;
        minorLvl = lvl%10;
        potential = (int)(101 - Math.sqrt(rand.nextInt(10000) + 1));
        if (potential > location.highestLvl) {
            potential = location.highestLvl;
        }
        movementTechnique = new Technique(Technique.techniqueType.Movement, this, rand.nextInt(potential)+1);
        martialTechnique = new Technique(Technique.techniqueType.Martial, this, rand.nextInt(potential)+1);
        cultivation = (Math.pow(potential,2)  * Math.sqrt(lvl) / (52. * Math.sqrt(potential)));
        cultivationBaseMax = Math.pow(lvl, 3) * (((70.+1./3.) + ((11./6.) * lvl)) / 25.);
        trait = Traits.Beast;
        ss = (long)Math.pow(100, (lvl+potential)/20.);
    }

    public void movement() { // 0 = west, 1 = east, 2 = north, 3 = south
        location.characters.remove(this); // Removes This Character From The Character List of the "Old" Location
        int temp = GameDriver.rand.nextInt(5);
        switch (temp) /* Creates New Area and/or Moves This Character*/ {
            case 0:
                if (location.x >= GameDriver.range - 100) {
                    if (GameDriver.tiles[0][location.y/100] == null) {
                        GameDriver.tiles[0][location.y/100] = new Area(location, 'e');
                    }
                    location = GameDriver.tiles[0][location.y/100];
                    break;
                }
                if (GameDriver.tiles[(location.x/100) + 1][location.y/100] == null) {
                    GameDriver.tiles[(location.x/100) + 1][location.y/100] = new Area(location, 'e');
                }
                location = GameDriver.tiles[(location.x/100) + 1][location.y/100];
                break;
            case 1:
                if (location.x <= 0) {
                    if (GameDriver.tiles[(GameDriver.range-100)/100][location.y/100] == null) {
                        GameDriver.tiles[(GameDriver.range-100)/100][location.y/100] = new Area(location, 'w');
                    }
                    location = GameDriver.tiles[(GameDriver.range-100)/100][location.y/100];
                    break;
                }
                if (GameDriver.tiles[(location.x/100) - 1][location.y/100] == null) {
                    GameDriver.tiles[(location.x/100) - 1][location.y/100] = new Area(location, 'w');
                }
                location = GameDriver.tiles[(location.x/100) - 1][location.y/100];
                break;
            case 2:
                if (location.y >= GameDriver.range - 100) {
                    if (GameDriver.tiles[(location.x/100)][0] == null) {
                        GameDriver.tiles[(location.x/100)][0] = new Area(location, 'n');
                    }
                    location = GameDriver.tiles[(location.x/100)][0];
                    break;
                }
                if (GameDriver.tiles[(location.x/100)][(location.y/100) + 1] == null) {
                    GameDriver.tiles[(location.x/100)][(location.y/100) + 1] = new Area(location, 'n');
                }
                location = GameDriver.tiles[(location.x/100)][(location.y/100) + 1];
                break;
            case 3:
                if (location.y <= 0) {
                    if (GameDriver.tiles[(location.x/100)][(GameDriver.range-100)/100] == null) {
                        GameDriver.tiles[(location.x/100)][(GameDriver.range-100)/100] = new Area(location, 's');
                    }
                    location = GameDriver.tiles[(location.x/100)][(GameDriver.range-100)/100];
                    break;
                }
                if (GameDriver.tiles[(location.x/100)][(location.y/100) - 1] == null) {
                    GameDriver.tiles[(location.x/100)][(location.y/100) - 1] = new Area(location, 's');
                }
                location = GameDriver.tiles[(location.x/100)][(location.y/100) - 1];
                break;
            case 4:
                break;
        }
        if (trait != Traits.Beast && !location.characters.contains(this)) {
            location.characters.add(this); // Adds This Character to the Character List of the New Location
        }
        if (trait != Traits.Beast && !knownLocations.contains(location)) {
            knownLocations.add(location); // Adds New Location to knownLocations
        }
    }
    public void movement(char temp) { // a = west, d = east, w = north, s = south
        location.characters.remove(this); // Removes This Character From The Character List of the "Old" Location
        switch (temp) /* Creates New Area and/or Moves This Character*/ {
            case 'd':
                if (location.x >= GameDriver.range - 100) {
                    if (GameDriver.tiles[0][location.y/100] == null) {
                        GameDriver.tiles[0][location.y/100] = new Area(location, 'e');
                    }
                    location = GameDriver.tiles[0][location.y/100];
                    break;
                }
                if (GameDriver.tiles[(location.x/100) + 1][location.y/100] == null) {
                    GameDriver.tiles[(location.x/100) + 1][location.y/100] = new Area(location, 'e');
                }
                location = GameDriver.tiles[(location.x/100) + 1][location.y/100];
                break;
            case 'a':
                if (location.x <= 0) {
                    if (GameDriver.tiles[(GameDriver.range-100)/100][location.y/100] == null) {
                        GameDriver.tiles[(GameDriver.range-100)/100][location.y/100] = new Area(location, 'w');
                    }
                    location = GameDriver.tiles[(GameDriver.range-100)/100][location.y/100];
                    break;
                }
                if (GameDriver.tiles[(location.x/100) - 1][location.y/100] == null) {
                    GameDriver.tiles[(location.x/100) - 1][location.y/100] = new Area(location, 'w');
                }
                location = GameDriver.tiles[(location.x/100) - 1][location.y/100];
                break;
            case 'w':
                if (location.y >= GameDriver.range - 100) {
                    if (GameDriver.tiles[(location.x/100)][0] == null) {
                        GameDriver.tiles[(location.x/100)][0] = new Area(location, 'n');
                    }
                    location = GameDriver.tiles[(location.x/100)][0];
                    break;
                }
                if (GameDriver.tiles[(location.x/100)][(location.y/100) + 1] == null) {
                    GameDriver.tiles[(location.x/100)][(location.y/100) + 1] = new Area(location, 'n');
                }
                location = GameDriver.tiles[(location.x/100)][(location.y/100) + 1];
                break;
            case 's':
                if (location.y <= 0) {
                    if (GameDriver.tiles[(location.x/100)][(GameDriver.range-100)/100] == null) {
                        GameDriver.tiles[(location.x/100)][(GameDriver.range-100)/100] = new Area(location, 's');
                    }
                    location = GameDriver.tiles[(location.x/100)][(GameDriver.range-100)/100];
                    break;
                }
                if (GameDriver.tiles[(location.x/100)][(location.y/100) - 1] == null) {
                    GameDriver.tiles[(location.x/100)][(location.y/100) - 1] = new Area(location, 's');
                }
                location = GameDriver.tiles[(location.x/100)][(location.y/100) - 1];
                break;
            default:
                break;
        }
        if (!location.characters.contains(this)) {
            location.characters.add(this); // Adds This Character to the Character List of the New Location
        }
        if (!knownLocations.contains(location)) {
            knownLocations.add(location); // Adds New Location to knownLocations
        }
    }

    public void cultivate() {
        cultivationBase += cultivation;
        if (cultivationBase >= cultivationBaseMax) {
            cultivationBase -= cultivationBaseMax;
            lvl++;
            minorLvl++;
            cultivationBaseMax = Math.pow(lvl, 3) * (((70.+1./3.) + ((11./6.) * lvl)) / 25.);
            cultivation = ((Math.pow(potential,2) * Math.sqrt(lvl) * 10.) / (52. * Math.sqrt(potential) * Math.pow(2,inhibitor)));
            if (minorLvl >= 11 && majorLvl < 10) {
                majorLvl++;
                minorLvl = 1;

                switch (majorLvl) {
                    case 1:
                        if (potential < 20) {
                            if (rand.nextInt(15) != 0) {
                                lvl--;
                                majorLvl--;
                                minorLvl = 10;
                                inhibitor++;
                                break;
                            }
                        }
                        travellingSpeed += 7;
                        maxAge += 100;
                        if (printBreakthroughs) {
                            System.out.println(this + " has broken through to the Foundation Stage");
                        }
                        break;
                    case 2:
                        if (potential < 37) {
                            if (rand.nextInt(15) != 0) {
                                lvl--;
                                majorLvl--;
                                minorLvl = 10;
                                inhibitor++;
                                break;
                            }
                        }
                        travellingSpeed += 8;
                        maxAge += 200;
                        if (printBreakthroughs) {
                            System.out.println(this + " has broken through to the Core Formation Stage");
                        }
                        break;
                    case 3:
                        if (potential < 52) {
                            if (rand.nextInt(15) != 0) {
                                lvl--;
                                majorLvl--;
                                minorLvl = 10;
                                inhibitor++;
                                break;
                            }
                        }
                        travellingSpeed += 8;
                        maxAge += 400;
                        System.out.println(this + " has broken through to Soul Wandering");
                        break;
                    case 4:
                        if (potential < 65) {
                            if (rand.nextInt(15) != 0) {
                                lvl--;
                                majorLvl--;
                                minorLvl = 10;
                                inhibitor++;
                                break;
                            }
                        }
                        travellingSpeed += 8;
                        maxAge += 1900;
                        System.out.println(this + " has broken through to Nascent Soul");
                        break;
                    case 5:
                        if (potential < 76) {
                            if (rand.nextInt(15) != 0) {
                                lvl--;
                                majorLvl--;
                                minorLvl = 10;
                                inhibitor++;
                                break;
                            }
                        }
                        travellingSpeed += 8;
                        maxAge += 3700;
                        System.out.println(this + " has broken through to Ensoulment");
                        break;
                    case 6:
                        if (potential < 85) {
                            if (rand.nextInt(10) != 0) {
                                lvl--;
                                majorLvl--;
                                minorLvl = 10;
                                inhibitor++;
                                break;
                            }
                        }
                        travellingSpeed += 8;
                        maxAge += 6100;
                        System.out.println(this + " has broken through to Nihility");
                        break;
                    case 7:
                        if (potential < 92) {
                            if (rand.nextInt(10) != 0) {
                                lvl--;
                                majorLvl--;
                                minorLvl = 10;
                                inhibitor++;
                                break;
                            }
                        }
                        travellingSpeed += 8;
                        maxAge += 9100;
                        System.out.println(this + " has broken through to Ascension");
                        break;
                    case 8:
                        if (potential < 97) {
                            if (rand.nextInt(10) != 0) {
                                lvl--;
                                majorLvl--;
                                minorLvl = 10;
                                inhibitor++;
                                break;
                            }
                        }
                        travellingSpeed += 8;
                        maxAge += 13700;
                        System.out.println(this + " has broken through to Half-Immortal");
                        break;
                    case 9:
                        if (potential < 100) {
                            if (rand.nextInt(10) != 0) {
                                lvl--;
                                majorLvl--;
                                minorLvl = 10;
                                inhibitor++;
                                break;
                            }
                        }
                        travellingSpeed += 8;
                        maxAge += 16900;
                        System.out.println(this + " has broken through to Pseudo Immortal");
                        break;
                    case 10:
                        if (rand.nextInt(5) != 0) {
                            lvl--;
                            majorLvl--;
                            minorLvl = 10;
                            inhibitor++;
                            break;
                        }
                        travellingSpeed += 8;
                        maxAge += 2140000000;
                        System.out.println(this + " has broken through to True Immortal");
                        break;
                }
            }
        }
    }

    public void time() {
        time++;
        if (time >= 52) {
            age++;
            time -= 52;
        }
        // Births a Child and Sets the Mother to Not Pregnant Status
        if (time >= 0 && !born) {
            born = true;
            mother.pregnant = false;
            System.out.println(this + " has been born to " + mother + " and " + father);
        }
        if (age > maxAge) {
            dead();
        }
    }
    public void dead() {
        dead = true;
        if (spouse != null) {
            spouse.spouse = null;
            spouse = null;
        }
        if (pregnant) {
            for (Character child : children) {
                if (!born) {
                    child.dead = true;
                }
            }
        }
        if (sect != null) {
            sect.resign(this);
        }
        if (equippedWeapon != null) {
            backpack.add(equippedWeapon);
            equippedWeapon = null;
        }
        if (equippedClothing != null) {
            backpack.add(equippedClothing);
            equippedClothing = null;
        }
        remCharacters.add(this);
        location.characters.remove(this);
        location.deadCharacters.add(this);

        System.out.println(this + "has died"); // Print Statement That Can Be Removed Later

    }
    public void dead(Character killer) {
        dead = true;

        if (trait != Traits.Beast) {
            if (spouse != null) {
                spouse.spouse = null;
                spouse = null;
            }
            if (pregnant) {
                for (Character child : children) {
                    if (!born) {
                        child.dead = true;
                    }
                }
            }
            if (sect != null) {
                sect.resign(this);
            }
            if (equippedWeapon != null) {
                backpack.add(equippedWeapon);
                equippedWeapon = null;
            }
            if (equippedClothing != null) {
                backpack.add(equippedClothing);
                equippedClothing = null;
            }
            remCharacters.add(this); //Removes a cultivator from the characters Array after the round is done
            location.characters.remove(this); //Removes a cultivator from the location
        }

        if (killer.trait == Traits.Beast) {
            if (!monstersList.contains(killer)) {
                monstersList.add(killer);
            }
            location.deadCharacters.add(this);
            for (int i = 0; i < lvl; i++) {
                killer.cultivate();
            }
            return;
        }

        if (trait == Traits.Beast) {
            killer.ss += this.ss;
            this.ss = 0;
            if (monstersList.contains(this)) {
                remCharacters.add(this); //Removes monster from monstersList after round is done
            }
            return;
        }

        if (trait != Traits.Beast && killer.trait != Traits.Beast) {
            for (Character friend : relationshipsList) {
                Integer value = friend.relationships.get(this);
                if (!friend.dead && value > 50) {
                    value /= -2;
                    friend.changeRelationship(killer, value);
                }
            }

            // Transers all items to the killer
            transferAllItems(killer);

            // Creates Blood to give to Killer
            Item item = new Item(Item.ItemType.Blood, potential + lvl);
            boolean hasItem = false;
            for (Item test : killer.materialBag) {
                if (item.name.equals(test.name)) {
                    test.quantity += item.quantity;
                    hasItem = true;
                    break;
                }
            }
            if (!hasItem) {
                killer.materialBag.add(item);
            }
        }
    }

    public void event() {
        consume();
        Character target = location.characters.get(rand.nextInt(location.characters.size()));
        switch (eventChoosing()) {
            //Battle A Player
            case 0:
                battleCheck(target);
                break;
            // Battle a Random Beast
            case 1:
                beastBattleCheck();
            //Interact With A Player
            case 2:
                interact(target);
                break;
            //Forage
            case 3:
                forage();
                break;
            // Mine
            case 4:
                mine();
                break;
            // Professions
            case 5:
                switch (trait) {
                    case Alchemist:
                        alchemy();
                        break;
                    case Blacksmith:
                        smithing();
                        break;
                    case Gatherer:
                        forage();
                        break;
                    case Miner:
                        mine();
                        break;
                    case Friendly:
                        interact(target);
                    case Tailor:
                        tailoring();
                    case Scribe:
                        array();
                        break;
                    case Leader:
                        if (professionLvl == 1) {
                            cityLeadership();
                        }
                        if (professionLvl == 50) {
                            sectLeadership();
                        }
                        break;
                    case Innovative:
                        if (rand.nextInt(100) == 0) {
                            technique();
                        }
                        break;
                    case Merchant:
                        break;
                    case Lucky:
                        event2();
                        break;
                    default:
                        break;
                }
                break;
            case 6:
                event2();
                break;
        }
    }
    public void event2() {
        // Prototype code
        if (!location.deadCharacters.isEmpty()) {
            Character temp = null;
            for (Character deceased : location.deadCharacters) {
                if (deceased.majorLvl < majorLvl + 2) {
                    deceased.transferAllItems(this);
                    temp = deceased;
                    break;
                }
            }
            location.deadCharacters.remove(temp);
        }
    }
    public int eventChoosing()  {
        int eventNum = rand.nextInt(10000); // Change back to 10000
        switch (affiliationNum) {
            case -1:
                if (eventNum < 125) {
                    return 0;
                } else if (eventNum < 1000) {
                    return 1;
                } else if (eventNum < 4000) {
                    return 2;
                } else if (eventNum < 6000){
                    return 3;
                } else if (eventNum < 8000) {
                    return 4;
                } else if (eventNum < 9000) {
                    return 5;
                } else {
                    return 6;
                }
            case 0:
                if (eventNum < 50) {
                    return 0;
                } else if (eventNum < 1000) {
                    return 1;
                } else if (eventNum < 4000) {
                    return 2;
                } else if (eventNum < 6000){
                    return 3;
                } else if (eventNum < 8000) {
                    return 4;
                } else if (eventNum < 9000) {
                    return 5;
                } else {
                    return 6;
                }
            case 1:
                if (eventNum < 125) {
                    return 0;
                } else if (eventNum < 1000) {
                    return 1;
                } else if (eventNum < 4000) {
                    return 2;
                } else if (eventNum < 6000){
                    return 3;
                } else if (eventNum < 8000) {
                    return 4;
                } else if (eventNum < 9000) {
                    return 5;
                } else {
                    return 6;
                }
            default:
                System.out.println("We got a problem in the event choosing method!!!");
                return 1; // Change this to
        }
    }
    public void forage(){
        if (location.herbMaterials > 0) {
            boolean hasItem = false;
            int rewardLvl = 11 - (int)Math.sqrt(Math.sqrt(rand.nextInt(10000) + 1)); // Formula for calculating a herb's level randomly
            int dropAmount = rand.nextInt(10) + 1;
            if (dropAmount > location.herbMaterials) {
                dropAmount = location.herbMaterials;
            }
            // Cycling through the materialBag to see if they have the item. If they do, it adds to the quantity of the item. If not, the item is created and added to their inventory
            for (Item item : materialBag) {
                if (item.itemType == Item.ItemType.Herb && item.lvl == rewardLvl) {
                    hasItem = true;
                    item.quantity += dropAmount;
                }
            }
            if (!hasItem) {
                Item item = new Item(Item.ItemType.Herb, rewardLvl, dropAmount); // Creates the item to add it to the player's inventory
                materialBag.add(item);
            }
            location.herbMaterials -= dropAmount; // Removes herbs from the location's herb count
            if (player && skipping <= 0 && age >= 8) {
                System.out.println("Discovered " + dropAmount + " herbs. Quality: " + rewardLvl);
            }
        }
    }
    public void mine(){
        if (location.metalMaterials > 0) {
            boolean hasItem = false;
            int rewardLvl = 11 - (int)Math.sqrt(Math.sqrt(rand.nextInt(10000) + 1)); // Formula for calculating a herb's level randomly
            int dropAmount = rand.nextInt(10) + 1;
            if (dropAmount > location.metalMaterials) {
                dropAmount = location.metalMaterials;
            }
            // Cycling through the materialBag to see if they have the item. If they do, it adds to the quantity of the item. If not, the item is created and added to their inventory
            for (Item item : materialBag) {
                if (item.itemType == Item.ItemType.Metal && item.lvl == rewardLvl) {
                    hasItem = true;
                    item.quantity += dropAmount;
                }
            }
            if (!hasItem) {
                Item item = new Item(Item.ItemType.Metal, rewardLvl, dropAmount); // Creates the item to add it to the player's inventory
                materialBag.add(item);
            }
            location.metalMaterials -= dropAmount; // Removes herbs from the location's herb count
            if (player && skipping <= 0 && age >= 8) {
                System.out.println("Discovered " + dropAmount + " metal. Quality: " + rewardLvl);
            }
        }
    }
    public void alchemy() {
        for (int i = 0; i <= professionLvl/10; i++) {
            boolean hasBlood = false;
            boolean hasItem = false;
            Item material = null;
            // Selects the herb to be used
            for (Item item : materialBag) {
                if (item.itemType == Item.ItemType.Blood && item.quantity >= 1000) {
                    hasBlood = true;
                    material = item;
                    break;
                }
                else if (item.itemType == Item.ItemType.Herb && item.quantity >= 50) {
                    if (material == null) {
                        if (item.lvl <= professionLvl/10 + 2) {
                            material = item;
                        }
                    }
                    else if (material.lvl < item.lvl && item.lvl <= professionLvl/10 + 2) {
                        material = item;
                    }
                }
            }
            Item pill = null;
            // Creates Pill Item
            if (hasBlood) {
                int quantity = material.quantity / 50;
                if (quantity > (professionLvl/10) - i + 1) {
                    quantity = (professionLvl/10) - i + 1;
                }
                i += quantity;

                pill = new Item(Item.ItemType.Pill, Item.PillType.Potential, 11, quantity);
                material.quantity -= 1000 * quantity;
            }
            else if (material != null){
                int quantity = material.quantity / 50;
                if (quantity > (professionLvl/10) - i + 1) {
                    quantity = (professionLvl/10) - i + 1;
                }
                i += quantity;

                pill = new Item(Item.ItemType.Pill, Item.PillType.Cultivation, material.lvl, quantity);
                material.quantity -= 50 * quantity;
            }
            else if (material == null) {
                if (rand.nextInt(10) == 0) {
                    pill = new Item(Item.ItemType.Pill, Item.PillType.Cultivation, 1, 1);
                }
                i += professionLvl/10 + 1;
            }


            if (pill != null) {
                // Cycling through the pillBag to see if they have the pill. If they do, it adds to the quantity of the item. If not, the item is created and added to their inventory
                for (Item item : pillBag) {
                    if (item.itemType == Item.ItemType.Pill && item.lvl == pill.lvl) {
                        hasItem = true;
                        item.quantity += pill.quantity;
                    }
                }
                if (!hasItem) {
                    pillBag.add(pill);
                }
                //Leveling up your alchemy skill
                if (professionLvl < 100) {
                    professionExp += pill.lvl * 10 * pill.quantity;
                    while (professionExp > professionExpMax && professionLvl < 100) {
                        professionLvl++;
                        professionExp -= professionExpMax;
                        professionExpMax = Math.pow(10, professionLvl / 10.);
                    }
                }
            }
        }
        if (player && skipping <= 0 && age >= 8) {
            System.out.println("Created a few pills");
        }
    }
    public void consume(){
        for (Item item : pillBag) {
            if (item.itemType == Item.ItemType.Pill) {
                if (item.pillType == Item.PillType.Potential && item.quantity > 0 && potential < 100) {
                    potential++;
                    item.quantity--;
                    continue;
                }
                else if (item.pillType == Item.PillType.Cultivation && item.quantity > 0 && item.lvl == majorLvl + 1) {
                    if (player && skipping <= 0 && age >= 8) {
                        System.out.println(this + " has consumed a(n) " + item);
                    }
                    cultivate();
                    item.quantity--;
                    continue;
                }
            }
        }
    }

    public void interact(Character player) {
        if (this != player && !player.dead && player.time >= 0) {
            Integer increase = rand.nextInt(10000);
            if (increase > 1000) {
                increase = 5;
            } else if (increase > 100) {
                increase = 10;
            } else if (increase > 10) {
                increase = 20;
            } else {
                increase = 50;
            }

            //Checks to see if Affiliation is the same or Good vs Evil
            if (this.affiliation.equals(player.affiliation)) {
                increase *= 2;
            } else if ((this.affiliation.equals("Good") && player.affiliation.equals("Evil")) || (this.affiliation.equals("Evil") && player.affiliation.equals("Good"))) {
                increase /= 2;
                if (rand.nextInt(100) == 0) {
                    battleCheck(player);
                }
            }
            // Changes the Relationship Value of this Character and the targeted Player

            changeRelationship(player, increase);
            checkBirth(player);

            if (this.player && skipping <= 0 && age >= 8) {
                System.out.println(this + " talked to " + player);
            }
        }
    }
    public void changeRelationship (Character player, Integer change) {
        // Checks to see if the relationship doesn't exist. If it doesn't, it creates the relationship
        if (!relationships.containsKey(player)) {
            relationships.put(player, 0);
            player.relationships.put(this, 0);
            relationshipsList.add(player);
            player.relationshipsList.add(this);
        }
        // Changes the value of the relationship in the hash table
        Integer value = relationships.get(player);
        relationships.replace(player, value + change);
        player.relationships.replace(this, value + change);
        checkRelationship(player);
    }
    public void checkRelationship(Character player) {
        if (this.age >= 18 && player.age >= 18 && spouse == null && player.spouse == null && relationships.get(player) >= 100 && this.gender != player.gender && this.mother != player && player.mother != this && this.father != player && player.father != this && !siblings.contains(player) && !this.dead && !player.dead) {
            spouse = player;
            player.spouse = this;
            System.out.println(this + " and " + player + " are now married! Congratulations!");
            if (gender.equals("Female")) {
                this.lastName = player.lastName;
            }
            else {
                player.lastName = this.lastName;
            }

        }
    }
    public void checkBirth(Character player) {
        if (spouse == player) {
            Character temp;
            if (gender.equals("Female")) {
                temp = this;
            }
            else {
                temp = player;
            }
            if (!temp.pregnant) {
                temp.pregnant = true;
                tempCharacters.add(new Character(temp));
            }
        }
    }
    public void smithing() {
        Item material = null;
        // Selects the metal to be used
        for (Item item : materialBag) {
            if (item.itemType == Item.ItemType.Metal && item.quantity >= 1000) {
                if (material == null) {
                    if (item.lvl <= professionLvl/10 + 2) {
                        material = item;
                    }
                }
                else if (material.lvl < item.lvl && item.lvl <= professionLvl/10 + 2) {
                    material = item;
                }
            }
        }
        Item weapon = null;
        // Chooses the weapon type
        Item.WeaponType temp = null;
        switch (rand.nextInt(5)) {
            case 0:
                temp = Item.WeaponType.Sword;
                break;
            case 1:
                temp = Item.WeaponType.Saber;
                break;
            case 2:
                temp = Item.WeaponType.Staff;
                break;
            case 3:
                temp = Item.WeaponType.Spear;
                break;
            case 4:
                temp = Item.WeaponType.Fan;
                break;
        }
        // Creates Item
        if (material == null) {
            if (rand.nextInt(300) == 0) {
                weapon = new Item(this, temp, rand.nextInt(10) + 1);
            }
        }
        else {
            weapon = new Item(this, temp, rand.nextInt(material.lvl * 5) + (1 + ((material.lvl * 10) / 2)));
            material.quantity -= 1000;
        }

        if (weapon != null) {
            backpack.add(weapon);

            //Leveling up your profession skill
            if (professionLvl < 100) {
                professionExp += weapon.lvl * 3;
                while (professionExp > professionExpMax && professionLvl < 100) {
                    professionLvl++;
                    professionExp -= professionExpMax;
                    professionExpMax = Math.pow(10, professionLvl / 10.);
                    if (professionLvl == 100) {
                        System.out.println(this + " reached max blacksmithingLvl");
                    }
                }
            }
            if (player && skipping <= 0 && age >= 8) {
                System.out.println("Created " + weapon);
            }
        }
    }
    public void tailoring() {
        Item material = null;
        // Selects the material to be used
        for (Item item : materialBag) {
            if (item.quantity >= 1000) {
                if (material == null) {
                    if (item.lvl <= professionLvl/10 + 2 && item.lvl > 0) {
                        material = item;
                    }
                }
                else if (material.lvl < item.lvl && item.lvl <= professionLvl/10 + 2 && item.lvl > 0) {
                    material = item;
                }
            }
        }
        Item clothing = null;

        // Creates Item
        if (material == null) {
            if (rand.nextInt(300) == 0) {
                clothing = new Item(this, rand.nextInt(10) + 1, Item.ItemType.Clothing);
            }
        }
        else {
            clothing = new Item(this, rand.nextInt(material.lvl * 5) + (1 + ((material.lvl * 10) / 2)), Item.ItemType.Clothing);
            material.quantity -= 1000;
        }

        if (clothing != null) {
            backpack.add(clothing);

            //Leveling up your profession skill
            if (professionLvl < 100) {
                professionExp += clothing.lvl * 3;
                while (professionExp > professionExpMax && professionLvl < 100) {
                    professionLvl++;
                    professionExp -= professionExpMax;
                    professionExpMax = Math.pow(10, professionLvl / 10.);
                }
            }
            if (player && skipping <= 0 && age >= 8) {
                System.out.println("Created " + clothing);
            }
        }
    }
    public void array() {
        Item material = null;
        // Selects the material to be used
        for (Item item : materialBag) {
            if (item.quantity >= 10000) {
                if (material == null) {
                    if (item.lvl <= professionLvl/10 + 2 && item.lvl > 0) {
                        material = item;
                    }
                }
                else if (material.lvl < item.lvl && item.lvl <= professionLvl/10 + 2 && item.lvl > 0) {
                    material = item;
                }
            }
        }
        Item array = null;

        // Creates Item
        if (material == null) {
            if (rand.nextInt(300) == 0) {
                array = new Item(this, rand.nextInt(10) + 1, Item.ItemType.Array);
            }
        }
        else {
            array = new Item(this, rand.nextInt(material.lvl * 5) + (1 + ((material.lvl * 10) / 2)), Item.ItemType.Array);
            material.quantity -= 1000;
        }

        if (array != null) {
            arrayBag.add(array);

            //Leveling up your profession skill
            if (professionLvl < 100) {
                professionExp += array.lvl * 3;
                while (professionExp > professionExpMax && professionLvl < 100) {
                    professionLvl++;
                    professionExp -= professionExpMax;
                    professionExpMax = Math.pow(10, professionLvl / 10.);
                }
            }
            if (player && skipping <= 0 && age >= 8) {
                System.out.println("Created " + array);
            }
        }
    }
    public void technique() {
        Technique technique = null;
        switch (rand.nextInt(2)) {
            case 0:
                technique = new Technique(Technique.techniqueType.Martial, this, ((rand.nextInt(professionLvl) + 1 + professionLvl) / 2));
                if (technique.lvl > martialTechnique.lvl) {
                    techniqueList.add(technique);
                }
                break;
            case 1:
                technique = new Technique(Technique.techniqueType.Movement, this, ((rand.nextInt(professionLvl) + 1 + professionLvl) / 2));
                if (technique.lvl > movementTechnique.lvl) {
                    techniqueList.add(technique);
                }
                break;
        }
        equip();

        //Leveling up your profession skill
        if (professionLvl < 100) {
            professionExp += (technique.lvl + 1) * 3;
            while (professionExp > professionExpMax && professionLvl < 100) {
                professionLvl++;
                professionExp -= professionExpMax;
                professionExpMax = Math.pow(10, professionLvl / 10.);
            }
        }
        if (player && skipping <= 0 && age >= 8) {
            System.out.println("Created " + technique);
        }
    }
    public void cityLeadership() {
        if (ss >= 1000000 && !location.city && rand.nextInt(100) == 0) {
            location.city = true;
            location.population = rand.nextInt((startingPopulation * 100000) - startingPopulation + 1) + startingPopulation;
            location.backpack = new ArrayList<>();
            location.materialBag = new ArrayList<>();
            location.pillBag = new ArrayList<>();
            location.arrayBag = new ArrayList<>();
            professionLvl = 50;
        }
    }
    public void sectLeadership(){
        if (location.sect == null && ss >= 1000000000) {
            if (location.city || rand.nextInt(100) == 0) {
                location.sect = new Organization(this);
                professionLvl = 100;
            }
        }
    }
    public void equip () {
        Item tempAddWeapon = null;
        Item tempAddClothing = null;
        boolean freshWeapon = false;
        boolean freshClothing = false;
        if (backpack != null && !backpack.isEmpty()) {
            for (Item item : backpack) {
                if (equippedWeapon == null) {
                    if (item.itemType == Item.ItemType.Weapon) {
                        equippedWeapon = item;
                        freshWeapon = true;
                    }
                }
                else if (item.itemType == Item.ItemType.Weapon && equippedWeapon.lvl < item.lvl) {
                    if (tempAddWeapon == null) {
                        tempAddWeapon = equippedWeapon;
                    }
                    equippedWeapon = item;
                }

                if (equippedClothing == null) {
                    if (item.itemType == Item.ItemType.Clothing) {
                        equippedClothing = item;
                        freshClothing = true;
                    }
                } else if (item.itemType == Item.ItemType.Clothing && equippedClothing.lvl < item.lvl) {
                    if (tempAddClothing == null) {
                        tempAddClothing = equippedClothing;
                    }
                    equippedClothing = item;
                }
            }

            if (freshWeapon) {
                backpack.remove(equippedWeapon);
            }
            else if (tempAddWeapon != null) {
                backpack.remove(equippedWeapon);
                backpack.add(tempAddWeapon);
            }

            if (freshClothing) {
                backpack.remove(equippedClothing);
            }
            else if (tempAddClothing != null) {
                backpack.remove(equippedClothing);
                backpack.add(tempAddClothing);
            }

        }

        Technique tempAddMartial = null;
        Technique tempAddMovement = null;
        Technique tempAddMartial2 = null;
        Technique tempAddMovement2 = null;
        boolean freshMartial = false;
        boolean freshMovement = false;
        //System.out.println(this + " | " + movementTechnique + " | " + movementTechnique2 + " | " + techniqueList);
        if (movementTechnique2 != null) {
            techniqueList.add(movementTechnique2);
        }
        if (martialTechnique2 != null) {
            techniqueList.add(martialTechnique2);
        }
        movementTechnique2 = null;
        martialTechnique2 = null;
        //System.out.println(this + " | " + movementTechnique + " | " + movementTechnique2 + " | " + techniqueList);
        if (techniqueList != null && !techniqueList.isEmpty()) {
            for (Technique technique : techniqueList) {
                if (technique.type == Technique.techniqueType.Martial && martialTechnique.lvl < technique.lvl) {
                    if (tempAddMartial == null) {
                        tempAddMartial = martialTechnique;
                    }
                    martialTechnique = technique;
                }
                else if (technique.type == Technique.techniqueType.Martial && martialTechnique.potential < technique.potential) {
                    if (martialTechnique2 == null) {
                        martialTechnique2 = technique;
                        freshMartial = true;
                    }
                    else if (martialTechnique2 != null && martialTechnique2.potential < technique.potential) {
                        if (tempAddMartial2 == null) {
                            tempAddMartial2 = martialTechnique2;
                        }
                        martialTechnique2 = technique;
                    }
                }
                if (technique.type == Technique.techniqueType.Movement && movementTechnique.lvl < technique.lvl) {
                    if (tempAddMovement == null) {
                        tempAddMovement = movementTechnique;
                    }
                    movementTechnique = technique;
                }
                else if (technique.type == Technique.techniqueType.Movement && movementTechnique.potential < technique.potential) {
                    if (movementTechnique2 == null) {
                        movementTechnique2 = technique;
                        freshMovement = true;
                    }
                    else if (movementTechnique2 != null && movementTechnique2.potential < technique.potential) {
                        if (tempAddMovement2 == null) {
                            tempAddMovement2 = movementTechnique2;
                        }
                        movementTechnique2 = technique;
                    }
                }
            }
            if (tempAddMartial != null) {
                techniqueList.remove(martialTechnique);
                techniqueList.add(tempAddMartial);
            }
            if (tempAddMovement != null) {
                techniqueList.remove(movementTechnique);
                techniqueList.add(tempAddMovement);
            }
            if (tempAddMartial2 != null) {
                techniqueList.remove(martialTechnique2);
                techniqueList.add(tempAddMartial2);
            }
            else if (freshMartial) {
                techniqueList.remove(martialTechnique2);
            }
            if (tempAddMovement2 != null) {
                techniqueList.remove(movementTechnique2);
                techniqueList.add(tempAddMovement2);
            }
            else if (freshMovement) {
                techniqueList.remove(movementTechnique2);
            }
        }
        if (trait != Traits.Innovative && trait != Traits.Beast) {
            techniqueList.clear();
        }
        //System.out.println(this + " | " + movementTechnique + " | " + movementTechnique2 + " | " + techniqueList);

    }
    public void transferAllItems(Character receiver) {
        // Transfers losers items to the receiver
        for (Item item : backpack) {
            receiver.backpack.add(item);
        }
        for (Item item : materialBag) {
            boolean hasItem = false;
            for (Item test : receiver.materialBag) {
                if (item.name.equals(test.name)) {
                    test.quantity += item.quantity;
                    hasItem = true;
                    break;
                }
            }
            if (!hasItem) {
                receiver.materialBag.add(item);
            }
        }
        for (Item item : pillBag) {
            boolean hasItem = false;
            for (Item test : receiver.pillBag) {
                if (item.name.equals(test.name)) {
                    test.quantity += item.quantity;
                    hasItem = true;
                    break;
                }
            }
            if (!hasItem) {
                receiver.pillBag.add(item);
            }
        }
        for (Item item : arrayBag) {
            receiver.arrayBag.add(item);
        }

        backpack.clear();
        pillBag.clear();
        materialBag.clear();
        arrayBag.clear();
        receiver.ss += this.ss;
        this.ss = 0;
    }

    public void battleCheck(Character opponent) {
        if (trait == Traits.Beast && !opponent.dead && opponent.time >= 0) {
            if (opponent.age < 8 && opponent.mother != null && !opponent.mother.dead) {
                battle(opponent.mother);
                return;
            } else {
                battle(opponent);
                return;
            }
        }

        if (affiliationNum == 1 && opponent.affiliationNum != -1 && !sparring) {
            return;
        }
        if (age < 18 && !sparring) {
            return;
        }
        if (relationships.containsKey(opponent)) {
            if (!sparring && relationships.get(opponent) >= 50) {
                return;
            }
        }

        // Decreases Relationship
        if (!sparring) {
            Integer increase = rand.nextInt(10000);
            if (increase > 1000) {
                increase = 5;
            } else if (increase > 100) {
                increase = 10;
            } else if (increase > 10) {
                increase = 20;
            } else {
                increase = 50;
            }
            changeRelationship(opponent, -1 * increase);
        }

        if (!sparring && location.city) {
            return;
        }
        if (this != opponent && !opponent.dead && opponent.time >= 0) {
            if (opponent.age < 8 && opponent.mother != null && !opponent.mother.dead) {
                battle(opponent.mother);
            } else {
                battle(opponent);
            }
        }
    }
    public void beastBattleCheck() {
        if (age < 13 || location.monsters <= 0 || lvl < (location.lvl + location.highestLvl) / 2) {
            return;
        }
        if (lvl < location.lvl) {
            if ( rand.nextInt(4) != 0) {
                return;
            }
        }
        location.monsters--;
        battle(new Character(location, this));
    }
    public void battle(Character opponent) {
        //System.out.println(this + " has challenged " + opponent + " to a fight!");
        int index = 50;
        int counter = 0;


        // Optimizes Equipment and Techniques and Updates Corresponding TempLvl
        equip();
        opponent.equip();

        boolean fighting = true;

        int speedMeter1 = (tempLvl + movementTechnique.lvl) / 2;
        int dmgMeter1 = (((tempLvl * 2) + martialTechnique.lvl) / 3) - opponent.tempLvl;
        if (equippedWeapon != null && equippedWeapon.lvl > lvl) {
            dmgMeter1 = ((tempLvl + martialTechnique.lvl + equippedWeapon.lvl) / 3) - opponent.tempLvl;
        }
        int speedMeter2 = (opponent.tempLvl + opponent.movementTechnique.lvl) / 2;
        int dmgMeter2 = (((opponent.tempLvl * 2) + opponent.martialTechnique.lvl) / 3) - tempLvl;
        if (opponent.equippedWeapon != null && opponent.equippedWeapon.lvl > opponent.lvl) {
            dmgMeter2 = ((opponent.tempLvl + opponent.martialTechnique.lvl + opponent.equippedWeapon.lvl) / 3) - tempLvl;
        }
        int actionBar1 = 0; // Player's Action Bar
        int actionBar2 = 0; // Opponent's Action Bar
        if (dmgMeter1 < 1) {
            dmgMeter1 = 1;
        }
        if (dmgMeter2 < 1) {
            dmgMeter2 = 1;
        }

        // Actual Fight Sequence
        while (fighting) {
            //Forbidden Check
            if (forbidden || opponent.forbidden) {
                forbiddenCheck(opponent);
                opponent.forbiddenCheck(this);
                speedMeter1 = (tempLvl + movementTechnique.lvl) / 2;
                dmgMeter1 = (((tempLvl * 2) + martialTechnique.lvl) / 3) - opponent.tempLvl;
                if (equippedWeapon != null && equippedWeapon.lvl > lvl) {
                    dmgMeter1 = ((tempLvl + martialTechnique.lvl + equippedWeapon.lvl) / 3) - opponent.tempLvl;
                }
                speedMeter2 = (opponent.tempLvl + opponent.movementTechnique.lvl) / 2;
                dmgMeter2 = (((opponent.tempLvl * 2) + opponent.martialTechnique.lvl) / 3) - tempLvl;
                if (opponent.equippedWeapon != null && opponent.equippedWeapon.lvl > opponent.lvl) {
                    dmgMeter2 = ((opponent.tempLvl + opponent.martialTechnique.lvl + opponent.equippedWeapon.lvl) / 3) - tempLvl;
                }
                if (dmgMeter1 < 1) {
                    dmgMeter1 = 1;
                }
                if (dmgMeter2 < 1) {
                    dmgMeter2 = 1;
                }
            }

            if (dead || opponent.dead) {
                fighting = false;
                sparring = false;
                opponent.sparring = false;
                forbidden = false;
                opponent.forbidden = false;
                break;
            }

            actionBar1 += speedMeter1;
            actionBar2 += speedMeter2;
            if (actionBar1 >= 100 && actionBar2 >= 100) {
                if (speedMeter1 >= speedMeter2) {
                    index += dmgMeter1;
                    durabilityCheck(opponent);
                    //System.out.println(this + " has pushed back " + opponent + " by " + dmgMeter1 + " to tile " + index + "!");
                    if (!indexCheck(opponent, index)) {
                        fighting = false;
                        sparring = false;
                        opponent.sparring = false;
                        forbidden = false;
                        opponent.forbidden = false;
                        break;
                    }
                    index -= dmgMeter2;
                    opponent.durabilityCheck(this);
                    //System.out.println(opponent + " has pushed back " + this + " by " + dmgMeter2 + " to tile " + index + "!");
                    actionBar1 -= 100;
                    actionBar2 -= 100;
                }
                else{
                    index -= dmgMeter2;
                    opponent.durabilityCheck(this);
                    //System.out.println(opponent + " has pushed back " + this + " by " + dmgMeter2 + " to tile " + index + "!");
                    if (!indexCheck(opponent, index)) {
                        fighting = false;
                        sparring = false;
                        opponent.sparring = false;
                        forbidden = false;
                        opponent.forbidden = false;
                        break;
                    }
                    index += dmgMeter1;
                    durabilityCheck(opponent);
                    //System.out.println(this + " has pushed back " + opponent + " by " + dmgMeter1 + " to tile " + index + "!");
                    actionBar1 -= 100;
                    actionBar2 -= 100;
                }
            }
            else if (actionBar1 >= 100) {
                index += dmgMeter1;
                durabilityCheck(opponent);
                //System.out.println(this + " has pushed back " + opponent + " by " + dmgMeter1 + " to tile " + index + "!");
                actionBar1 -= 100;
            }
            else if (actionBar2 >= 100) {
                index -= dmgMeter2;
                opponent.durabilityCheck(this);
                //System.out.println(opponent + " has pushed back " + this + " by " + dmgMeter2 + " to tile " + index + "!");
                actionBar2 -= 100;
            }
            if (!indexCheck(opponent, index)) {
                fighting = false;
                sparring = false;
                opponent.sparring = false;
                forbidden = false;
                opponent.forbidden = false;
                break;
            }
            counter++;
            if (counter >= 200) {
                fighting = false;
                sparring = false;
                opponent.sparring = false;
                forbidden = false;
                opponent.forbidden = false;
                break;
                //System.out.println(this + " and " + opponent + " have fought to a tie!");
            }
        }
    }
    // If returns false the fight will end
    public boolean indexCheck(Character opponent, int index) {
        if (index <= 0) {
            if (this.trait != Traits.Beast) {
                System.out.println("" + this + ": " + this.lvl + " " + potential + " has lost to " + opponent + ": " + opponent.lvl + " " + opponent.potential);
            }
            opponent.winCheck(this);
            return false;
        }
        else if (index >= 100) {
            if (opponent.trait != Traits.Beast) {
                System.out.println("" + opponent + ": " + opponent.lvl + " " + opponent.potential + " has lost to " + this + ": " + this.lvl + " " + this.potential);
            }
            this.winCheck(opponent);
            return false;
        }
        else if (index <= 10 && !sparring) {
            this.forbidden = true;
        }
        else if (index >= 90 && !sparring) {
            opponent.forbidden = true;
        }
        else if (index <= 24) {
            if (fleeCheck(opponent)) {
                //System.out.println("" + this + " has fleed from " + opponent);
                return false;
            }
        }
        else if (index >= 76) {
            if (opponent.fleeCheck(this)) {
                //System.out.println("" + opponent + " has fleed from " + this);
                return false;
            }
        }
        return true;
    }
    public void winCheck(Character loser) {
        if (!sparring) {
            loser.dead(this);
        }
    }
    //If true, player will flee
    public boolean fleeCheck(Character opponent) {
        if (this.trait == Traits.Beast) {
            return false;
        }
        if (this.trait == Traits.Runner && lvl >= opponent.lvl - 10) {
            return true;
        }
        if (lvl > opponent.lvl && movementTechnique.lvl > opponent.movementTechnique.lvl) {
            return true;
        }
        else if (lvl + movementTechnique.lvl > opponent.lvl + opponent.movementTechnique.lvl) {
            if (rand.nextInt(5) != 0) {
                return true;
            }
        }
        else if (lvl + movementTechnique.lvl == opponent.lvl + opponent.movementTechnique.lvl) {
            if (rand.nextInt(2) == 0) {
                return true;
            }
        }
        else {
            if (rand.nextInt(lvl + movementTechnique.lvl) > (opponent.lvl + opponent.movementTechnique.lvl)/4) {
                return true;
            }
        }
        return false;
    }
    public void tempLvlCheck(){
        equip();
        tempLvl = lvl;
        if (equippedClothing != null && equippedClothing.lvl > lvl) {
            tempLvl = (lvl + equippedClothing.lvl) / 2;
        }

        if (forbidden) {
            if (lvl < potential) {
                tempLvl = potential;
                if (equippedClothing != null && equippedClothing.lvl > tempLvl) {
                    tempLvl = (tempLvl + equippedClothing.lvl) / 2;
                }
                potential -= (int) Math.sqrt(potential);
            }
            else if (lvl >= potential) {
                tempLvl = lvl + (2 * majorLvl);
                if (equippedClothing != null && equippedClothing.lvl > tempLvl) {
                    tempLvl = (tempLvl + equippedClothing.lvl) / 2;
                }
                maxAge -= (int)Math.pow(2, majorLvl);
            }
        }

        if (this.trait == Traits.Cheater) {
            tempLvl += 5;
        }

    }
    public void forbiddenCheck(Character opponent) {
        tempLvlCheck();
        if (age > maxAge) {
            opponent.winCheck(this);
        }
    }
    public void durabilityCheck(Character opponent) {
        if (equippedWeapon != null) {
            if (opponent.equippedClothing != null) {
                opponent.equippedClothing.durability -= equippedWeapon.lvl;
                equippedWeapon.durability -= opponent.equippedClothing.lvl;
            }
            else {
                equippedWeapon.durability -= opponent.lvl;
            }
        }
        else {
            if (opponent.equippedClothing != null) {
                opponent.equippedClothing.durability -= lvl;
            }
        }
        if (equippedWeapon != null && equippedWeapon.durability <= 0) {
            equippedWeapon = null;
        }
        if (opponent.equippedClothing != null && opponent.equippedClothing.durability <= 0) {
            opponent.equippedClothing = null;
        }
        if (equippedWeapon == null) {
            equip();
        }
        if (opponent.equippedClothing == null) {
            opponent.equip();
        }
    }
    public String getTrait() {
        switch (trait) {
            case Alchemist:
                return "Alchemist";
            case Blacksmith:
                return "Blacksmith";
            case Gatherer:
                return "Gatherer";
            case Miner:
                return "Miner";
            case Friendly:
                return "Friendly";
            case Scribe:
                return "Scribe";
            case Leader:
                return "Leader";
            case Innovative:
                return "Innovative";
            case Merchant:
                return "Merchant";
            case Lucky:
                return "Lucky";
            case Ghost:
                return "Ghost";
            case Tailor:
                return "Tailor";
            case Runner:
                return "Runner";
            case Cheater:
                return "Cheater";
            default:
                return "Error in trait";
        }
    }
    public String toString() {

        if (dead && majorLvl == 10) {
            dead = false;
            time = 0;
            age = 0;
            return (name + " completed a cycle and has rebirthed. Potential: " + potential);
        }
        else if (dead  && age > maxAge) {
            return (name + " " + lastName + " with potential: " + potential + " and cultivation: " + cultivation + " and major level: " + majorLvl +  " and minor level: " + minorLvl + " and inhibitor: " + inhibitor);
        }
        else {
            return (name + " " + lastName);
        }
    }


}
