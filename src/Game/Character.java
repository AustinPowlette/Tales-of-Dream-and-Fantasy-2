package Game;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.PriorityQueue;

import static Game.GameDriver.*;
import static Game.Dictionary.*;
import static Game.Map.*;

public class Character implements java.io.Serializable {
    static final long serialVersionUID = 1L;
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
        IronBody,
        SpiritVein,
        HeavenlyRoot,
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
    int player = 0; // 0=NPC, 1=human controlled, 2=tracked NPC (prints like player)
    boolean sparring = false;
    char movement; // 'x' = stand still  'm' = move randomly
    int skipping = 0; // How many turns a person chooses to skip
    int time = 0;
    int age = 0;
    int maxAge = 100;
    int totalAgeGained = 100;
    int potential = 0; // 101 -(Math.sqrt(rand.nextInt(10000) + 1)
    int caution = 50;
    double cultivation = 0;
    int inhibitor = 1;
    int majorLvl = 0 ; // Numbers 0-10: 10 = True Immortal, 9 = Pseudo Immortal, 8 = Half-Immortal, 7 = Ascension, 6 = Nihility, 5 = Ensoulment, 4 = Nascent Soul, 3 = Soul Wandering, 2 = Core Formation, 1 = Foundation Stage, 0 = Qi Cultivation, -1 = Crippled
    int minorLvl = 1; // Numbers 1-10: 1-3 = Low, 4-6 = Mid, 7-9 = High, 10 = Peak
    int lvl = 1;
    int tempLvl = lvl;
    int kills = 0;
    int itemsCrafted = 0;
    int sectPoints = 0;
    String sectMission = null;
    int sectMissionBonus = 0;
    boolean isGhostForm = false;
    String originalName = null; // chain of prior names for Ghost rebirths
    int bloodlineBonus = 0;
    boolean needsReincarnation = false; // set when a player character dies; resolved by GameDriver
    Character reincarnationKiller = null; // the killer, if any, for the reincarnation menu
    String repTitle = null;
    int exhaustionPenalty = 0;
    int autoX = -1;
    int autoY = -1;
    double cultivationBase = 0;
    double cultivationBaseMax = Math.pow(lvl, 3) * (((70.+1./3.) + ((11./6.) * lvl)) / 25.);
    int professionLvl = 1;
    double professionExp = 0;
    double professionExpMax = Math.pow(10, professionLvl / 10);
    long ss = 0; // Spirit Stones (Major 1-3)
    long sc = 0; // Spirit Crystals — 1 SC = 1,000,000 SS (Major 4-6)
    long dc = 0; // Divine Crystals  — 1 DC = 1,000,000 SC (Major 7-9)
    long ij = 0; // Immortal Jades   — 1 IJ = 1,000,000 DC (Major 10)
    String affiliation = ""; // Evil, Good, Neutral
    Integer affiliationNum;
    Organization sect;
    Traits trait;
    PriorityQueue<Quest> spotQuests = null;
    PriorityQueue<Quest> targetQuests = null;
    Quest currentQuest = null;
    ArrayList<Shop> ownedShops = null;
    // Relationship variables
    RelationshipList relationshipList;
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
        spotQuests = new PriorityQueue<Quest>(20);
        targetQuests = new PriorityQueue<Quest>(20);

        relationshipList = new RelationshipList();
        siblings = new ArrayList<>();
        children = new ArrayList<>();
        knownLocations = new ArrayList<>();
        backpack = new ArrayList<>();
        materialBag = new ArrayList<>();
        pillBag = new ArrayList<>();
        arrayBag = new ArrayList<>();
        ownedShops = new ArrayList<>();
        movementTechnique = new Technique(Technique.techniqueType.Movement);
        martialTechnique = new Technique(Technique.techniqueType.Martial);
        techniqueList = new ArrayList<>();

        location = area;
        location.characters.add(this);
        knownLocations.add(location);
        id = UID++;

        // Gender and Name Picker
        {
            if (rand.nextInt(2) == 0) {
            name = Dictionary.maleNames[rand.nextInt(Dictionary.maleNames.length)];
            gender = "Male";
        }
        else {
            name = Dictionary.femaleNames[rand.nextInt(Dictionary.femaleNames.length)];
            gender = "Female";
        }
        lastName = Dictionary.lastNames[rand.nextInt(Dictionary.lastNames.length)];}

        potential = (int)(101 - Math.sqrt(rand.nextInt(10000) + 1));
        cultivation = (Math.pow(potential,2)  * Math.sqrt(lvl) / (52. * Math.sqrt(potential)));

        //Caution Setter
        {
            caution = rand.nextInt(100) + 1;
            if (caution <= 8) {
                caution = 20;
            } else if (caution <= 25) {
                caution = 35;
            } else if (caution <= 75) {
                caution = 50;
            } else if (caution <= 92) {
                caution = 65;
            } else {
                caution = 80;
            }
        }

        // Affiliation Choosing
        {
            switch (rand.nextInt(3)) {
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
        }

        // Trait Picker
        {
            int traitNum = rand.nextInt(1000000);
            if (traitNum == 0) {
                trait = Traits.Cheater;
            } else if (traitNum < 10000) {
                trait = Traits.Lucky;
            } else if (traitNum < 40000) {
                trait = Traits.Ghost;
            } else if (traitNum < 70000) {
                trait = Traits.Leader;
            } else if (traitNum < 100000) {
                trait = Traits.Innovative;
            } else if (traitNum < 200000) {
                trait = Traits.Blacksmith;
            } else if (traitNum < 300000) {
                trait = Traits.Alchemist;
            } else if (traitNum < 400000) {
                trait = Traits.Scribe;
            } else if (traitNum < 500000) {
                trait = Traits.Merchant;
            } else if (traitNum < 600000) {
                trait = Traits.Gatherer;
            } else if (traitNum < 700000) {
                trait = Traits.Miner;
            } else if (traitNum < 800000) {
                trait = Traits.Friendly;
            } else if (traitNum < 900000) {
                trait = Traits.Tailor;
            } else {
                trait = Traits.Runner;
            }
        }

        // Quest Initialization
        questInitializer();
    }

    // Birth Constructor / Enemy Generator
    public Character(Character mother) {
        //Pre-Initialization Stuff
        spotQuests = new PriorityQueue<Quest>(20);
        targetQuests = new PriorityQueue<Quest>(20);

        relationshipList = new RelationshipList();
        siblings = new ArrayList<>();
        children = new ArrayList<>();
        knownLocations = new ArrayList<>();
        backpack = new ArrayList<>();
        materialBag = new ArrayList<>();
        pillBag = new ArrayList<>();
        arrayBag = new ArrayList<>();
        ownedShops = new ArrayList<>();
        movementTechnique = new Technique(Technique.techniqueType.Movement);
        martialTechnique = new Technique(Technique.techniqueType.Martial);
        techniqueList = new ArrayList<>();
        affiliationNum = 0; // default neutral; overwritten below in affiliation block

        this.mother = mother;
        this.father = mother.spouse;
        siblings.addAll(mother.children);
        for (Character child : father.children) {
            if (!siblings.contains(child)) {
                siblings.add(child);
            }
        }
        for (Character sibling : siblings) {
            sibling.siblings.add(this);
            changeRelationship(sibling, 0);
        }
        mother.children.add(this);
        father.children.add(this);
        changeRelationship(father, 3);
        changeRelationship(mother, 3);

        location = mother.location;
        location.characters.add(this);
        knownLocations.add(location);
        id = UID++;
        time = -40;
        mother.pregnant = true;
        born = false;
        Character temp = null;

        // Gender and Name Picker
        {
            if (rand.nextInt(2) == 0) {
                name = Dictionary.maleNames[rand.nextInt(Dictionary.maleNames.length)];
                gender = "Male";
                temp = father;
                if (father.sect != null) {
                    father.sect.join(this);
                }
            } else {
                name = Dictionary.femaleNames[rand.nextInt(Dictionary.femaleNames.length)];
                gender = "Female";
                temp = mother;
                if (mother.sect != null) {
                    mother.sect.join(this);
                }
            }
            lastName = father.lastName;
        }

        // Technique Inheritance — child starts at 1/4 of parent's mastery level
        {
            Technique srcMove = (temp.movementTechnique2 != null) ? temp.movementTechnique2 : temp.movementTechnique;
            movementTechnique = new Technique(srcMove);
            movementTechnique.lvl = Math.max(1, srcMove.lvl / 4);

            Technique srcMart = (temp.martialTechnique2 != null) ? temp.martialTechnique2 : temp.martialTechnique;
            martialTechnique = new Technique(srcMart);
            martialTechnique.lvl = Math.max(1, srcMart.lvl / 4);
        }

        // Limiter Inheritance
        {
            int limiter = (mother.potential + father.potential) / 2;
            potential = (int) (101 - Math.sqrt(rand.nextInt(10000) + 1));
            if (potential < limiter) {
                potential = limiter;
            }
            cultivation = (Math.pow(potential, 2) * Math.sqrt(lvl) / (52. * Math.sqrt(potential)));
        }
        // Bloodline bonus: each parent at Soul Wandering+ (majorLvl >= 3) grants +1 bonus cultivate/tick
        if (mother.majorLvl >= 3) bloodlineBonus++;
        if (father != null && father.majorLvl >= 3) bloodlineBonus++;

        //Caution Setter
        {
            caution = rand.nextInt(100) + 1;
            if (caution <= 8) {
                caution = 20;
            } else if (caution <= 25) {
                caution = 35;
            } else if (caution <= 75) {
                caution = 50;
            } else if (caution <= 92) {
                caution = 65;
            } else {
                caution = 80;
            }
        }

        // Affiliation Choosing
        {
            affiliationNum = mother.affiliationNum + father.affiliationNum;
            switch (affiliationNum) /*Affiliation Choosing */ {
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
        }

        // Trait Choosing
        {
            int traitNum = rand.nextInt(1000000);
            if (traitNum == 0) {
                trait = Traits.Cheater;
            } else if (traitNum < 5000) {
                trait = Traits.Lucky;
            } else if (traitNum < 15000) {
                trait = Traits.Ghost;         // rare physique: rebirth on death
            } else if (traitNum < 25000) {
                trait = Traits.IronBody;      // rare physique: natural armor
            } else if (traitNum < 35000) {
                trait = Traits.SpiritVein;    // rare physique: faster cultivation
            } else if (traitNum < 40000) {
                trait = Traits.HeavenlyRoot;  // rare physique: better technique learning
            } else if (traitNum < 70000) {
                trait = Traits.Leader;
            } else if (traitNum < 100000) {
                trait = Traits.Innovative;
            } else if (traitNum < 200000) {
                trait = Traits.Blacksmith;
            } else if (traitNum < 300000) {
                trait = Traits.Alchemist;
            } else if (traitNum < 400000) {
                trait = Traits.Scribe;
            } else if (traitNum < 500000) {
                trait = Traits.Merchant;
            } else if (traitNum < 600000) {
                trait = Traits.Gatherer;
            } else if (traitNum < 700000) {
                trait = Traits.Miner;
            } else if (traitNum < 800000) {
                trait = Traits.Friendly;
            } else if (traitNum < 900000) {
                trait = Traits.Tailor;
            } else {
                trait = Traits.Runner;
            }
        }

        // Quest Initialization
        questInitializer();

    }

    // Beast Constructor
    public Character (Area area, Character cultivator) {
        location = area;
        relationshipList = new RelationshipList();
        spotQuests = new PriorityQueue<>(20);
        targetQuests = new PriorityQueue<>(20);
        siblings = new ArrayList<>();
        children = new ArrayList<>();
        knownLocations = new ArrayList<>();
        backpack = new ArrayList<>();
        materialBag = new ArrayList<>();
        pillBag = new ArrayList<>();
        arrayBag = new ArrayList<>();
        ownedShops = new ArrayList<>();
        techniqueList = new ArrayList<>();
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

    public void questInitializer () {
        Quest random     = new Quest("Random", false, 35, 2, 90);
        Quest profession = new Quest("Profession", false, 30, 3, 70);
        Quest hunt       = new Quest("Hunt", false, 30, 5, 50);
        Quest test = new Quest("Test", false, 45, 5, 10);


        spotQuests.add(random);
        spotQuests.add(profession);
        spotQuests.add(hunt);
        spotQuests.add(test);
    }

    // Monster NPCs: primarily hunt, occasionally explore via Random events
    public void monsterQuestInitializer() {
        spotQuests.add(new Quest("Hunt", false, 60, 5, 80));
        spotQuests.add(new Quest("Hunt Cultivator", false, 55, 5, 75));
        spotQuests.add(new Quest("Random", false, 25, 2, 50));
    }

    // Called from winCheck() when a Beast kills a cultivator — 10% chance
    public void awakenMonster() {
        trait = Traits.Monster;
        affiliation = "Evil";
        affiliationNum = -1;
        String[] titles = {"Ancient", "Dread", "Elder", "Primordial", "Titan", "Sovereign", "Infernal"};
        name = titles[rand.nextInt(titles.length)] + " " + name;
        id = UID++;
        age = 8;
        time = 0;
        location.characters.add(this);
        if (!knownLocations.contains(location)) knownLocations.add(location);
        monsterQuestInitializer();
        characters.add(this);
        awakenedMonsters.add(this);
        System.out.println(name + " " + lastName + " has awakened as a Monster! The wilds tremble.");
    }

    public void movement() { // 0 = west, 1 = east, 2 = north, 3 = south
        int temp = rand.nextInt(5);
        char move = 'x';
        switch (temp) /* Creates New Area and/or Moves This Character*/ {
            case 0:
                move = 'a';
                break;
            case 1:
                move = 'd';
                break;
            case 2:
                move = 'w';
                break;
            case 3:
                move = 's';
                break;
            case 4:
                break;
        }
        movement(move);
    }
    public void movement(char temp) { // a = west, d = east, w = north, s = south
        location.characters.remove(this); // Removes This Character From The Character List of the "Old" Location
        switch (temp) /* Creates New Area and/or Moves This Character*/ {
            case 'd':
                if (location.x >= range - 1) {
                    if (tiles[0][location.y] == null) {
                        tiles[0][location.y] = new Area(location, 'e');
                    }
                    location = tiles[0][location.y];
                    break;
                }
                if (tiles[(location.x) + 1][location.y] == null) {
                    tiles[(location.x) + 1][location.y] = new Area(location, 'e');
                }
                location = tiles[(location.x) + 1][location.y];
                break;
            case 'a':
                if (location.x <= 0) {
                    if (tiles[(range-1)][location.y] == null) {
                        tiles[(range-1)][location.y] = new Area(location, 'w');
                    }
                    location = tiles[(range-1)][location.y];
                    break;
                }
                if (tiles[(location.x) - 1][location.y] == null) {
                    tiles[(location.x) - 1][location.y] = new Area(location, 'w');
                }
                location = tiles[(location.x) - 1][location.y];
                break;
            case 'w':
                if (location.y >= range - 1) {
                    if (tiles[(location.x)][0] == null) {
                        tiles[(location.x)][0] = new Area(location, 'n');
                    }
                    location = tiles[(location.x)][0];
                    break;
                }
                if (tiles[(location.x)][(location.y) + 1] == null) {
                    tiles[(location.x)][(location.y) + 1] = new Area(location, 'n');
                }
                location = tiles[(location.x)][(location.y) + 1];
                break;
            case 's':
                if (location.y <= 0) {
                    if (tiles[(location.x)][(range-1)] == null) {
                        tiles[(location.x)][(range-1)] = new Area(location, 's');
                    }
                    location = tiles[(location.x)][(range-1)];
                    break;
                }
                if (tiles[(location.x)][(location.y) - 1] == null) {
                    tiles[(location.x)][(location.y) - 1] = new Area(location, 's');
                }
                location = tiles[(location.x)][(location.y) - 1];
                break;
            default:
                break;
        }
        if (trait != Traits.Beast && !location.characters.contains(this)) {
            location.characters.add(this); // Adds This Character to the Character List of the New Location
        }
        if (trait != Traits.Beast && !knownLocations.contains(location)) {
            knownLocations.add(location); // Adds New Location to knownLocations
        }
    }

    public void practice() {
        cultivate();
        if (martialTechnique != null && rand.nextInt(10) == 0) martialTechnique.upgrade(this);
    }

    public void cultivate() {
        double spiritVeinBonus = (trait == Traits.SpiritVein) ? 1.5 : 1.0;
        cultivationBase += cultivation * spiritVeinBonus;
        if (cultivationBase >= cultivationBaseMax) {
            cultivationBase -= cultivationBaseMax;
            lvl++;
            minorLvl++;
            cultivationBaseMax = Math.pow(lvl, 3) * (((70.+1./3.) + ((11./6.) * lvl)) / 25.);
            cultivation = ((Math.pow(potential,2) * Math.sqrt(lvl) * 10.) / (52. * Math.sqrt(potential) * Math.pow(2,inhibitor)));
            if ((minorLvl >= 11 && majorLvl < 10)) {
                int preMajor = majorLvl;
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
                        maxAge += 100;
                        totalAgeGained += 100;
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
                        maxAge += 200;
                        totalAgeGained += 200;
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
                        maxAge += 400;
                        totalAgeGained += 400;
                        if (printBreakthroughs || (player > 0 && skipping <= 0)) System.out.println(this + " has broken through to Soul Wandering");
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
                        maxAge += 1900;
                        totalAgeGained += 1900;
                        if (printBreakthroughs || (player > 0 && skipping <= 0)) System.out.println(this + " has broken through to Nascent Soul");
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
                        maxAge += 3700;
                        totalAgeGained += 3700;
                        if (printBreakthroughs || (player > 0 && skipping <= 0)) System.out.println(this + " has broken through to Ensoulment");
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
                        maxAge += 6100;
                        totalAgeGained += 6100;
                        if (printBreakthroughs || (player > 0 && skipping <= 0)) System.out.println(this + " has broken through to Nihility");
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
                        maxAge += 9100;
                        totalAgeGained += 9100;
                        if (printBreakthroughs || (player > 0 && skipping <= 0)) System.out.println(this + " has broken through to Ascension");
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
                        maxAge += 13700;
                        totalAgeGained += 13700;
                        if (printBreakthroughs || (player > 0 && skipping <= 0)) System.out.println(this + " has broken through to Half-Immortal");
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
                        maxAge += 16900;
                        totalAgeGained += 16900;
                        if (printBreakthroughs || (player > 0 && skipping <= 0)) System.out.println(this + " has broken through to Pseudo Immortal");
                        break;
                    case 10:
                        if (rand.nextInt(5) != 0) {
                            lvl--;
                            majorLvl--;
                            minorLvl = 10;
                            inhibitor++;
                            break;
                        }
                        maxAge += 2140000000;
                        totalAgeGained += 2140000000;
                        if (printBreakthroughs || (player > 0 && skipping <= 0)) System.out.println(this + " has broken through to True Immortal");
                        break;
                }
                // Rare potential gain on successful breakthrough (1% chance)
                if (majorLvl == preMajor + 1 && potential < 101 && rand.nextInt(100) == 0) {
                    potential++;
                    if (player > 0 && skipping <= 0) System.out.println("Breakthrough insight! Potential increased to " + potential);
                }
                // Breakthrough Tribulation at key realm transitions (3=Core, 5=Nascent, 7=Spirit, 9=Immortal)
                if (majorLvl == preMajor + 1 && (majorLvl == 3 || majorLvl == 5 || majorLvl == 7 || majorLvl == 9)) {
                    tempLvlCheck();
                    int tribChance = Math.max(10, Math.min(90, potential - (majorLvl - 3) * 5));
                    if (rand.nextInt(100) < tribChance) {
                        // Pass
                        if (rand.nextInt(10) == 0 && potential < 100) potential++;
                        cultivationBase += cultivationBaseMax * 0.5;
                        if (printBreakthroughs || (player > 0 && skipping <= 0))
                            System.out.println(this + " survived the realm tribulation! (majorLvl " + majorLvl + ")");
                    } else {
                        // Fail — pushed back
                        majorLvl = preMajor;
                        minorLvl = 8;
                        lvl -= (lvl - preMajor * 10);
                        inhibitor++;
                        if (printBreakthroughs || (player > 0 && skipping <= 0))
                            System.out.println(this + " failed the realm tribulation! Cultivation regressed.");
                    }
                }
                // Hero threshold: first cultivator of any kind to reach each apex realm
                if (majorLvl == preMajor + 1) {
                    if (majorLvl == 5 && !heroEnsoulment) { heroEnsoulment = true; System.out.println("[HERO] " + this + " is the first to reach Ensoulment! The heavens take notice."); }
                    if (majorLvl == 7 && !heroAscension) { heroAscension = true; System.out.println("[HERO] " + this + " is the first to reach Ascension! An era-defining cultivator has emerged."); }
                    if (majorLvl == 9 && !heroPseudoImmortal) { heroPseudoImmortal = true; System.out.println("[HERO] " + this + " is the first to reach Pseudo Immortal! Immortality approaches."); }
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
            if (printBreakthroughs || (player > 0 && skipping <= 0)) System.out.println(this + " has been born to " + mother + " and " + father);
        }
        if (age > maxAge) {
            dead();
        }
    }

    public void dead() {
        if (dead) return;
        if (trait == Traits.Beast || trait == Traits.Monster) {
            dead = true;
            location.characters.remove(this);
            location.deadCharacters.add(this);
            remCharacters.add(this); // null out characters slot; GameDriver skips population-- for monsters
            return;
        }
        dead = true;
        if (player >= 1 && trait != Traits.Ghost) needsReincarnation = true;
        if (majorLvl >= 7) {
            String legend = name + " " + lastName;
            if (!legacyTitles.contains(legend)) { legacyTitles.add(legend); legacyLvls.add(lvl); }
        }
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
        targetQuests.clear();
        spotQuests.clear();
        currentQuest = null;
        // Recycling: remove this character from living friends' relationship lists
        for (Relationship rel : new ArrayList<>(relationshipList.relationships)) {
            if (rel.friendID < 0 || rel.friendID >= characters.size()) continue;
            Character friend = characters.get(rel.friendID);
            if (friend != null && !friend.dead) friend.relationshipList.remove(this.id);
        }
        // Notify children so they can find parent's body
        for (Character child : children) {
            if (child != null && !child.dead && child.born) {
                child.targetQuests.add(new Quest("Find Parent's Body", false, 25, 0, 45, location));
            }
        }
        remCharacters.add(this);
        location.characters.remove(this);
        location.deadCharacters.add(this);

        if (printBreakthroughs || player > 0) System.out.println(this + " has died");

    }
    public void dead(Character killer) {
        if (dead) return; // already dead this tick; prevent duplicate remCharacters entry
        if (killer == null) { dead(); return; } // array/trap death: no killer to credit
        // True Immortal rebirth — cycles through death rather than truly dying
        if (majorLvl == 10) {
            dead = false;
            time = 0;
            age = 0;
            killer.kills++;
            if (player > 0 || killer.player > 0) System.out.println(name + " completed a cycle and has rebirthed. Potential: " + potential);
            return;
        }
        // Ghost physique: enter ghost form instead of dying (only on first death, not while already a ghost)
        if (trait == Traits.Ghost && !isGhostForm && killer.trait != Traits.Beast) {
            isGhostForm = true;
            if (equippedWeapon != null)  { backpack.add(equippedWeapon);  equippedWeapon  = null; }
            if (equippedClothing != null){ backpack.add(equippedClothing); equippedClothing = null; }
            if (sect != null) sect.resign(this);
            targetQuests.clear(); spotQuests.clear(); currentQuest = null;
            // Seek a city for rebirth
            for (Area area : createdAreas) {
                if (area.city) {
                    targetQuests.add(new Quest("Ghost City Rebirth", false, 50, 2, 70, area));
                    break;
                }
            }
            killer.kills++;
            if (player > 0 || killer.player > 0) System.out.println(this + " has become a Ghost!");
            return;
        }
        dead = true;
        if (player >= 1 && trait != Traits.Ghost) {
            needsReincarnation = true;
            reincarnationKiller = killer;
        }

        if (trait != Traits.Beast && trait != Traits.Monster) {
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
            targetQuests.clear();
            spotQuests.clear();
            currentQuest = null;
            // Notify children so they can find parent's body
            for (Character child : children) {
                if (child != null && !child.dead && child.born) {
                    child.targetQuests.add(new Quest("Find Parent's Body", false, 25, 0, 45, location));
                }
            }
            remCharacters.add(this); //Removes a cultivator from the characters Array after the round is done
            location.characters.remove(this); //Removes a cultivator from the location
        }

        if (killer.trait == Traits.Beast || killer.trait == Traits.Monster) {
            if (killer.trait == Traits.Beast && !monstersList.contains(killer)) {
                monstersList.add(killer);
            }
            location.deadCharacters.add(this);
            killer.kills++;
            for (int i = 0; i < lvl; i++) {
                killer.cultivate();
            }
            return;
        }

        if (trait == Traits.Beast || trait == Traits.Monster) {
            killer.kills++;
            killer.ss += this.ss;
            this.ss = 0;
            if (monstersList.contains(this)) {
                remCharacters.add(this); //Removes monster from monstersList after round is done
            }
            return;
        }

        if (trait != Traits.Beast && trait != Traits.Monster && killer.trait != Traits.Beast && killer.trait != Traits.Monster) {
            for (Relationship relationship : relationshipList.relationships) {
                if (relationship.friendID < 0 || relationship.friendID >= characters.size()) continue;
                Character friend = characters.get(relationship.friendID);
                Integer value = relationship.value;
                if (friend != null && !friend.dead && value >= 2) {
                    value -= 1;
                    friend.changeRelationship(killer, -value);
                    if (!killer.dead && friend.majorLvl >= killer.majorLvl - 1) {
                        // Strong enough for revenge
                        final int killerId = killer.id;
                        boolean hasRevenge = friend.targetQuests.stream()
                                .anyMatch(q -> "Revenge".equals(q.name) && q.targetCharacter == killerId);
                        if (!hasRevenge)
                            friend.targetQuests.add(new Quest("Revenge", true, 60, 8, 80, killer));
                    } else if (!killer.dead) {
                        // Too weak for revenge — post a bounty at the nearest known city
                        final int killerId = killer.id;
                        for (Area area : friend.knownLocations) {
                            if (area != null && area.city) {
                                boolean alreadyPosted = area.bounties.stream()
                                        .anyMatch(b -> b.targetCharacter == killerId);
                                if (!alreadyPosted)
                                    area.bounties.add(new Quest("Bounty Hunt", true, 40, 3, 70, killer));
                                break;
                            }
                        }
                    }
                }
            }

            // Recycling: remove this character from living characters' relationship lists
            for (Relationship rel : new ArrayList<>(relationshipList.relationships)) {
                Character friend = characters.get(rel.friendID);
                if (friend != null && !friend.dead) friend.relationshipList.remove(this.id);
            }
            killer.kills++;
            // Legacy: record high-level cultivators who fall in battle
            if (majorLvl >= 7) {
                String legend = name + " " + lastName;
                if (!legacyTitles.contains(legend)) { legacyTitles.add(legend); legacyLvls.add(lvl); }
            }
            // Cultivation XP for Monster kills
            if (killer.trait == Traits.Monster) {
                for (int i = 0; i < Math.min(lvl, 20); i++) killer.cultivate();
            }
            // Legendary milestone for Monster NPCs
            if (killer.trait == Traits.Monster && killer.kills % 10 == 0) {
                String title = killer.kills >= 50 ? "Immortal Terror" : killer.kills >= 30 ? "Dread Lord" : killer.kills >= 20 ? "Tyrant" : "Legend";
                int idx = killer.lastName.indexOf(" the ");
                if (idx >= 0) killer.lastName = killer.lastName.substring(0, idx) + " the " + title;
                else killer.lastName = killer.lastName + " the " + title;
                System.out.println(killer.name + " " + killer.lastName + " has earned the title: " + title + " (" + killer.kills + " kills)");
                for (int i = 0; i < 10; i++) killer.cultivate();
                if (rand.nextInt(20) == 0 && killer.potential < 100) killer.potential++;
                for (Area a : cityAreas) {
                    if (a != null && !a.characters.isEmpty()) {
                        final int monsterId = killer.id;
                        boolean alreadyPosted = a.bounties.stream().anyMatch(b -> b.targetCharacter == monsterId);
                        if (!alreadyPosted) {
                            a.bounties.add(new Quest("Bounty Hunt", true, 65, 5, 85, killer));
                            System.out.println("Bounty posted on " + killer.name + " " + killer.lastName + " at " + a.name + "!");
                        }
                        break;
                    }
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
    public void quest () {
        consume(); // Consume pills IMPORTANT!!!

        // Dynamic quest triggers — checked each turn
        if (backpack.size() > 20 && targetQuests.stream().noneMatch(q -> "Go Sell".equals(q.name))
                && (currentQuest == null || !"Go Sell".equals(currentQuest.name))) {
            Area targetCity = null;
            for (Area area : knownLocations) {
                if (area != null && area.city && area.cityOwner != null) { targetCity = area; break; }
            }
            if (targetCity != null)
                targetQuests.add(new Quest("Go Sell", true, 50, 0, 70, targetCity));
        }

        // Buy Gear: in city with shops — buy immediately; otherwise queue travel to nearest known shop city
        // Monsters are predators and never buy gear
        boolean gearNeeded = trait != Traits.Monster && (equippedWeapon == null || equippedClothing == null
                || (equippedWeapon != null && equippedWeapon.lvl + 10 <= lvl)
                || (equippedClothing != null && equippedClothing.lvl + 10 <= lvl));
        if (gearNeeded) {
            if (location.city && !location.shops.isEmpty()) {
                buyGear();
                // If slot(s) still empty after visiting shop, can't afford — hunt beasts to earn ss
                boolean stillMissing = equippedWeapon == null || equippedClothing == null;
                if (stillMissing && targetQuests.stream().noneMatch(q -> "Hunt Beast".equals(q.name))
                        && (currentQuest == null || !"Hunt Beast".equals(currentQuest.name))) {
                    for (Area area : knownLocations) {
                        if (area != null && area.monsters > 0
                                && area.highestLvl >= lvl - 10 && area.highestLvl <= lvl + 15) {
                            targetQuests.add(new Quest("Hunt Beast", false, 40, 3, 65, area));
                            break;
                        }
                    }
                }
            } else if (targetQuests.stream().noneMatch(q -> "Buy Gear".equals(q.name))
                    && (currentQuest == null || !"Buy Gear".equals(currentQuest.name))) {
                for (Area area : knownLocations) {
                    if (area != null && area.city && !area.shops.isEmpty()) {
                        targetQuests.add(new Quest("Buy Gear", true, 30, 2, 55, area));
                        break;
                    }
                }
            }
        }

        // Seek Potential: travel to city with potential pills when potential is too low for next breakthrough
        if (trait != Traits.Beast && trait != Traits.Monster && majorLvl < 9) {
            int potThreshold = 101 - (9 - majorLvl) * (9 - majorLvl);
            if (potential < potThreshold
                    && targetQuests.stream().noneMatch(q -> "Seek Potential".equals(q.name))
                    && (currentQuest == null || !"Seek Potential".equals(currentQuest.name))) {
                for (Area area : knownLocations) {
                    if (area != null && area.city && area.pillBag != null
                            && area.pillBag.stream().anyMatch(p ->
                                    p.itemType == Item.ItemType.Pill && p.pillType == Item.PillType.Potential)) {
                        targetQuests.add(new Quest("Seek Potential", true, 35, 2, 65, area));
                        break;
                    }
                }
            }
        }

        // Buy Cultivation Pills: pick up next-realm pills when in a city (10% chance)
        if (trait != Traits.Monster && location.city && location.pillBag != null && rand.nextInt(10) == 0) {
            for (int pi = 0; pi < location.pillBag.size(); pi++) {
                Item pill = location.pillBag.get(pi);
                if (pill.itemType == Item.ItemType.Pill && pill.pillType == Item.PillType.Cultivation
                        && pill.lvl == majorLvl + 1 && pill.quantity > 0) {
                    long price = itemPrice(pill);
                    if (canAfford(price)) {
                        deductSs(price);
                        if (location.cityOwner != null) {
                            location.cityOwner.ss += price;
                            location.cityOwner.convertCurrency();
                        }
                        boolean found = false;
                        for (Item p : pillBag) {
                            if (p.name.equals(pill.name)) { p.quantity++; found = true; break; }
                        }
                        if (!found) pillBag.add(new Item(Item.ItemType.Pill, pill.pillType, pill.lvl, 1));
                        if (--pill.quantity <= 0) location.pillBag.remove(pill);
                    }
                    break;
                }
            }
        }

        // Bully: Evil characters proactively target significantly weaker characters in same location
        if (trait != Traits.Beast && "Evil".equals(affiliation) && rand.nextInt(10) == 0
                && !location.characters.isEmpty()) {
            for (Character c : location.characters) {
                if (c == null || c.dead || c == this || c.trait == Traits.Beast) continue;
                if (lvl - c.lvl >= 15
                        && targetQuests.stream().noneMatch(q -> "Battle Dislike".equals(q.name) && q.targetCharacter == c.id)) {
                    targetQuests.add(new Quest("Battle Dislike", true, 20, 3, 50, c));
                    break;
                }
            }
        }

        // Lucky: one-time high-priority loot when a fresh corpse within 30 levels is in this tile
        if (trait == Traits.Lucky && !location.deadCharacters.isEmpty()) {
            for (Character deceased : location.deadCharacters) {
                if (Math.abs(lvl - deceased.lvl) <= 30
                        && spotQuests.stream().noneMatch(q -> "Loot Corpse".equals(q.name))) {
                    spotQuests.add(new Quest("Loot Corpse", true, 75, 0, 75));
                    break;
                }
            }
        }

        // Hunt Beast: 20% chance per turn to seek combat in a known area with level-appropriate monsters
        if (rand.nextInt(5) == 0
                && targetQuests.stream().noneMatch(q -> "Hunt Beast".equals(q.name))
                && (currentQuest == null || !"Hunt Beast".equals(currentQuest.name))) {
            for (Area area : knownLocations) {
                if (area != null && area.monsters > 0
                        && area.highestLvl >= lvl - 10 && area.highestLvl <= lvl + 15) {
                    targetQuests.add(new Quest("Hunt Beast", false, 30, 2, 50, area));
                    break;
                }
            }
        }

        // Seek Territory: Monster migrates toward highest-level known area (20% chance per turn)
        if (trait == Traits.Monster && rand.nextInt(5) == 0
                && targetQuests.stream().noneMatch(q -> "Seek Territory".equals(q.name))
                && (currentQuest == null || !"Seek Territory".equals(currentQuest.name))) {
            Area bestArea = null;
            for (Area a : knownLocations) {
                if (a != null && a != location && a.lvl > location.lvl + 10) {
                    if (bestArea == null || a.lvl > bestArea.lvl) bestArea = a;
                }
            }
            if (bestArea != null) {
                targetQuests.add(new Quest("Seek Territory", true, 45, 3, 70, bestArea));
            }
        }

        // Dual Cultivation: bonus cultivation with spouse or a best friend in the same tile (5%/turn)
        if (trait != Traits.Beast && trait != Traits.Monster && rand.nextInt(20) == 0) {
            if (spouse != null && !spouse.dead && spouse.location == location) {
                for (int i = 0; i < 3; i++) cultivate();
                spouse.cultivationBase += spouse.cultivation * 3;
            } else {
                for (Relationship rel : relationshipList.relationships) {
                    if (rel.value >= 3) {
                        Character friend = characters.get(rel.friendID);
                        if (friend != null && !friend.dead && friend.location == location) {
                            cultivate();
                            friend.cultivationBase += friend.cultivation;
                            break;
                        }
                    }
                }
            }
        }

        // Legacy Discovery: rare meditation on a fallen legend grants cultivation insight (2%/turn)
        if (!legacyTitles.isEmpty() && trait != Traits.Beast && trait != Traits.Monster && rand.nextInt(50) == 0) {
            int legIdx = rand.nextInt(legacyTitles.size());
            int legLvl = legacyLvls.get(legIdx);
            for (int i = 0; i < Math.min(legLvl / 10, 10); i++) cultivate();
            if (player > 0 && skipping <= 0)
                System.out.println(this + " meditates on the legend of " + legacyTitles.get(legIdx) + "!");
        }

        // Heaven Treasure: rush to claim a known heavenly technique (high priority)
        if (trait != Traits.Beast && trait != Traits.Monster
                && targetQuests.stream().noneMatch(q -> "Heaven Treasure".equals(q.name))
                && (currentQuest == null || !"Heaven Treasure".equals(currentQuest.name))) {
            for (Area a : knownLocations) {
                if (a != null && a.heavenlyTechnique != null) {
                    targetQuests.add(new Quest("Heaven Treasure", true, 70, 5, 85, a));
                    break;
                }
            }
        }

        // Rob Caravan: Evil characters may target wealthy merchants passing through (5%/turn)
        if ("Evil".equals(affiliation) && trait != Traits.Beast && trait != Traits.Monster && rand.nextInt(20) == 0) {
            for (Character c : new ArrayList<>(location.characters)) {
                if (c == null || c.dead || c == this || c.trait == Character.Traits.Beast) continue;
                if (c.trait == Character.Traits.Merchant && (c.backpack.size() > 5 || c.ss > 100000L)
                        && targetQuests.stream().noneMatch(q -> "Rob Caravan".equals(q.name) && q.targetCharacter == c.id)) {
                    targetQuests.add(new Quest("Rob Caravan", true, 30, 2, 55, c));
                    break;
                }
            }
        }

        // Join Sect: non-Leader adults with no sect seek a known sect (5% chance per turn, low priority)
        if (sect == null && trait != Traits.Leader && age >= 8 && rand.nextInt(20) == 0
                && targetQuests.stream().noneMatch(q -> "Join Sect".equals(q.name))
                && (currentQuest == null || !"Join Sect".equals(currentQuest.name))) {
            for (Area area : knownLocations) {
                if (area != null && area.sect != null) {
                    targetQuests.add(new Quest("Join Sect", true, 20, 1, 40, area));
                    break;
                }
            }
        }

        // Return to Sect: sect members who drift away periodically head back (5% chance/turn)
        if (sect != null && !sect.isMaster(this) && location != sect.location && rand.nextInt(20) == 0
                && targetQuests.stream().noneMatch(q -> "Return to Sect".equals(q.name))
                && (currentQuest == null || !"Return to Sect".equals(currentQuest.name))) {
            targetQuests.add(new Quest("Return to Sect", false, 25, 1, 40, sect.location));
        }

        // Recruit Member: sect masters actively seek eligible candidates in known locations
        if (sect != null && sect.isMaster(this) && trait == Traits.Leader && rand.nextInt(15) == 0
                && targetQuests.stream().noneMatch(q -> "Recruit Member".equals(q.name))
                && (currentQuest == null || !"Recruit Member".equals(currentQuest.name))) {
            for (Character c : location.characters) {
                if (c != null && !c.dead && c != this && sect.canJoin(c)) {
                    targetQuests.add(new Quest("Recruit Member", true, 25, 2, 45, c));
                    break;
                }
            }
        }

        // Bounty Hunt: pick up posted bounties at cities within 10 levels of the target
        if (location.city && !location.bounties.isEmpty()) {
            for (Quest bounty : new ArrayList<>(location.bounties)) {
                Character target = characters.get(bounty.targetCharacter);
                if (target != null && !target.dead && Math.abs(lvl - target.lvl) <= 10
                        && targetQuests.stream().noneMatch(q -> "Bounty Hunt".equals(q.name)
                                && q.targetCharacter == bounty.targetCharacter)) {
                    targetQuests.add(new Quest("Bounty Hunt", true, 40, 3, 70, target));
                    location.bounties.remove(bounty);
                }
            }
        }

        // Explore Dungeon: 16% chance per turn to seek a known dungeon area at appropriate level
        if (trait != Traits.Beast && age >= 8 && rand.nextInt(6) == 0
                && targetQuests.stream().noneMatch(q -> "Explore Dungeon".equals(q.name))
                && (currentQuest == null || !"Explore Dungeon".equals(currentQuest.name))) {
            for (Area area : knownLocations) {
                if (area != null && area.dungeonType != null
                        && area.lvl >= lvl - 15 && area.lvl <= lvl + 20) {
                    targetQuests.add(new Quest("Explore Dungeon", true, 35, 2, 60, area));
                    break;
                }
            }
        }

        // Sect Raid: masters and elders attack enemy sect location when at war; also opportunistic raids on crisis sects
        if (sect != null && (sect.isMaster(this) || sect.getRank(this) == Organization.SectRank.ELDER)
                && targetQuests.stream().noneMatch(q -> "Sect Raid".equals(q.name))
                && (currentQuest == null || !"Sect Raid".equals(currentQuest.name))) {
            Area enemyLoc = null;
            if (sect.isAtWar() && sect.warTarget != null) {
                enemyLoc = sect.warTarget.location;
            } else {
                // Opportunistic: raid a known sect in succession crisis (1% chance/turn)
                if (rand.nextInt(100) == 0) {
                    for (Area area : knownLocations) {
                        if (area != null && area.sect != null && area.sect != sect && area.sect.inCrisis()) {
                            enemyLoc = area;
                            break;
                        }
                    }
                }
            }
            if (enemyLoc != null)
                targetQuests.add(new Quest("Sect Raid", true, 65, 5, 85, enemyLoc));
        }

        // Propose Alliance: sect masters seek alliances with other non-enemy sects (5% chance/turn)
        if (sect != null && sect.isMaster(this) && !sect.isAtWar() && rand.nextInt(20) == 0
                && targetQuests.stream().noneMatch(q -> "Propose Alliance".equals(q.name))) {
            for (Character c : new ArrayList<>(location.characters)) {
                if (c == null || c.dead || c.sect == null || c.sect == sect || sect.isAlly(c.sect)) continue;
                if (c.sect.isMaster(c)) {
                    targetQuests.add(new Quest("Propose Alliance", true, 30, 5, 50, c));
                    break;
                }
            }
        }

        // Demand Tribute: powerful sect masters extract tribute from weaker rival sects (2% chance/turn)
        if (sect != null && sect.isMaster(this) && !sect.isAtWar() && rand.nextInt(50) == 0
                && targetQuests.stream().noneMatch(q -> "Demand Tribute".equals(q.name))) {
            for (Character c : new ArrayList<>(location.characters)) {
                if (c == null || c.dead || c.sect == null || c.sect == sect || sect.isAlly(c.sect)) continue;
                if (c.sect.isMaster(c) && lvl - c.lvl >= 20) {
                    targetQuests.add(new Quest("Demand Tribute", true, 40, 5, 60, c));
                    break;
                }
            }
        }

        // Recruit Defector: sect masters at war poach enemy Elders (1% chance/turn)
        if (sect != null && sect.isMaster(this) && sect.isAtWar() && rand.nextInt(100) == 0
                && targetQuests.stream().noneMatch(q -> "Recruit Defector".equals(q.name))) {
            for (Character c : new ArrayList<>(location.characters)) {
                if (c == null || c.dead || c.sect == null || c.sect == sect) continue;
                if (sect.warTarget != null && c.sect == sect.warTarget
                        && sect.warTarget.getRank(c) == Organization.SectRank.ELDER) {
                    targetQuests.add(new Quest("Recruit Defector", true, 45, 5, 65, c));
                    break;
                }
            }
        }

        // Promote Disciples: sect masters check members for rank advancement (5% chance/turn)
        if (sect != null && sect.isMaster(this) && rand.nextInt(20) == 0) {
            for (Character member : new ArrayList<>(sect.members)) {
                if (member == null || member.dead) continue;
                Organization.SectRank rank = sect.getRank(member);
                if (rank == Organization.SectRank.OUTER_DISCIPLE
                        && member.majorLvl >= Math.max(1, majorLvl - 4)) {
                    sect.promoteRank(member);
                    if (player > 0 && skipping <= 0) System.out.println(member + " promoted to Inner Disciple of " + sect.name);
                } else if (rank == Organization.SectRank.INNER_DISCIPLE
                        && member.majorLvl >= Math.max(2, majorLvl - 2)) {
                    sect.promoteRank(member);
                    if (player > 0 && skipping <= 0) System.out.println(member + " promoted to Elder of " + sect.name);
                }
            }
        }

        // Gift Technique: parents give their best technique to born children (5% chance/turn)
        if (!techniqueList.isEmpty() && !children.isEmpty() && rand.nextInt(20) == 0
                && targetQuests.stream().noneMatch(q -> "Gift Technique".equals(q.name))) {
            Technique bestTech = null;
            for (Technique t : techniqueList) {
                if (bestTech == null || t.potential > bestTech.potential) bestTech = t;
            }
            if (bestTech != null) {
                final String techName = bestTech.name;
                for (Character child : new ArrayList<>(children)) {
                    if (child != null && !child.dead && child.born
                            && child.techniqueList.stream().noneMatch(t -> t.name.equals(techName))
                            && targetQuests.stream().noneMatch(q -> "Gift Technique".equals(q.name) && q.targetCharacter == child.id)) {
                        targetQuests.add(new Quest("Gift Technique", true, 30, 2, 50, child));
                        break;
                    }
                }
            }
        }

        // Restock Trip: Merchant with shops seeks best city for arbitrage restocking (10% chance/turn)
        if (trait == Traits.Merchant && !ownedShops.isEmpty() && rand.nextInt(10) == 0
                && targetQuests.stream().noneMatch(q -> "Restock Trip".equals(q.name))
                && (currentQuest == null || !"Restock Trip".equals(currentQuest.name))) {
            Area bestCity = findBestRestockCity();
            if (bestCity != null) {
                targetQuests.add(new Quest("Restock Trip", true, 30, 2, 60, bestCity));
            }
        }

        // Sell Technique: Innovative chars sell techniques to city libraries (10% chance/turn)
        if (trait == Traits.Innovative && !techniqueList.isEmpty() && rand.nextInt(10) == 0
                && spotQuests.stream().noneMatch(q -> "Sell Technique".equals(q.name))
                && targetQuests.stream().noneMatch(q -> "Sell Technique".equals(q.name))
                && (currentQuest == null || !"Sell Technique".equals(currentQuest.name))) {
            if (location.city && !location.libraries.isEmpty()) {
                spotQuests.add(new Quest("Sell Technique", true, 40, 0, 60));
            } else {
                for (Area area : knownLocations) {
                    if (area != null && area.city && !area.libraries.isEmpty()) {
                        targetQuests.add(new Quest("Sell Technique", true, 40, 0, 60, area));
                        break;
                    }
                }
            }
        }

        // Hold Tournament: city owners and sect masters hold periodic tournaments (2% chance/turn)
        if (rand.nextInt(50) == 0 && spotQuests.stream().noneMatch(q -> "Hold Tournament".equals(q.name))) {
            boolean isCityOwner = location.city && location.cityOwner == this;
            boolean isSectMasterAtHome = sect != null && sect.isMaster(this) && location == sect.location;
            if (isCityOwner || isSectMasterAtHome) {
                spotQuests.add(new Quest("Hold Tournament", true, 35, 0, 55));
            }
        }

        // Buy City Array: city owners periodically invest in stronger defensive arrays (3% chance/turn)
        if (location.city && location.cityOwner == this && rand.nextInt(33) == 0
                && spotQuests.stream().noneMatch(q -> "Buy City Array".equals(q.name))) {
            int arrayLvl = Math.min(100, (majorLvl + 1) * 10);
            Item testArray = new Item(this, arrayLvl, Item.ItemType.Array);
            if (canAfford(itemPrice(testArray))) {
                spotQuests.add(new Quest("Buy City Array", true, 40, 0, 60));
            }
        }

        // Entice Merchant: city owners recruit merchants without a local shop (5% chance/turn)
        if (location.city && location.cityOwner == this && rand.nextInt(20) == 0
                && spotQuests.stream().noneMatch(q -> "Entice Merchant".equals(q.name))) {
            boolean missingMerchant = characters.stream().anyMatch(c -> c != null && !c.dead
                    && c.trait == Traits.Merchant
                    && c.ownedShops.stream().noneMatch(s -> s.location == location));
            if (missingMerchant) spotQuests.add(new Quest("Entice Merchant", true, 45, 0, 65));
        }

        // Donate to Sect: members at sect home donate no-longer-useful items (10% chance/turn)
        if (sect != null && location == sect.location && rand.nextInt(10) == 0
                && spotQuests.stream().noneMatch(q -> "Donate to Sect".equals(q.name))) {
            boolean hasExtras = backpack.stream().anyMatch(i -> i != equippedWeapon && i != equippedClothing);
            boolean usesHerbs  = trait == Traits.Alchemist || trait == Traits.Tailor || trait == Traits.Scribe;
            boolean usesMetals = trait == Traits.Blacksmith;
            if (!hasExtras) for (Item m : materialBag) {
                if ((m.itemType == Item.ItemType.Herb  && (!usesHerbs  || m.quantity > 5000))
                 || (m.itemType == Item.ItemType.Metal && (!usesMetals || m.quantity > 5000))) { hasExtras = true; break; }
            }
            if (!hasExtras) for (Item p : pillBag) {
                boolean stale = p.lvl <= majorLvl * 10
                    || (p.pillType == Item.PillType.Potential && potential >= 100)
                    || (p.pillType == Item.PillType.Cultivation && p.quantity > 5 && p.lvl > majorLvl * 10);
                if (stale) { hasExtras = true; break; }
            }
            if (hasExtras) spotQuests.add(new Quest("Donate to Sect", true, 30, 0, 50));
        }

        // Buy from Sect: members buy needed materials or gear from treasury using sectPoints (10% chance/turn)
        if (sect != null && location == sect.location && sectPoints >= 5 && !sect.treasury.isEmpty()
                && rand.nextInt(10) == 0
                && spotQuests.stream().noneMatch(q -> "Buy from Sect".equals(q.name))) {
            boolean needsHerb  = (trait == Traits.Alchemist || trait == Traits.Tailor || trait == Traits.Scribe)
                    && materialBag.stream().noneMatch(m -> m.itemType == Item.ItemType.Herb && m.quantity >= 1000);
            boolean hasHerb    = sect.treasury.stream().anyMatch(t -> t.itemType == Item.ItemType.Herb && t.quantity > 0);
            boolean needsMetal = trait == Traits.Blacksmith
                    && materialBag.stream().noneMatch(m -> m.itemType == Item.ItemType.Metal && m.quantity >= 1000);
            boolean hasMetal   = sect.treasury.stream().anyMatch(t -> t.itemType == Item.ItemType.Metal && t.quantity > 0);
            boolean wantsGear  = sect.treasury.stream().anyMatch(t ->
                (t.itemType == Item.ItemType.Weapon   && (equippedWeapon   == null || t.lvl > equippedWeapon.lvl   + 5))
             || (t.itemType == Item.ItemType.Clothing && (equippedClothing == null || t.lvl > equippedClothing.lvl + 5)));
            if ((needsHerb && hasHerb) || (needsMetal && hasMetal) || wantsGear) {
                spotQuests.add(new Quest("Buy from Sect", true, 45, 0, 65));
            }
        }

        // Ghost form: seek pregnant women for possession (10% chance/turn)
        if (isGhostForm && rand.nextInt(10) == 0
                && targetQuests.stream().noneMatch(q -> "Possess Womb".equals(q.name))) {
            for (Area area : knownLocations) {
                if (area == null) continue;
                for (Character c : new ArrayList<>(area.characters)) {
                    if (c.pregnant && !c.dead && lvl > c.lvl) {
                        targetQuests.add(new Quest("Possess Womb", false, 80, 3, 100, c));
                        break;
                    }
                }
                if (targetQuests.stream().anyMatch(q -> "Possess Womb".equals(q.name))) break;
            }
        }

        // Accept Sect Mission: sect members pick up a board mission (15% chance/turn when at sect)
        if (sect != null && location == sect.location && sectMission == null
                && !sect.questBoard.isEmpty() && rand.nextInt(7) == 0
                && spotQuests.stream().noneMatch(q -> "Accept Sect Mission".equals(q.name))) {
            spotQuests.add(new Quest("Accept Sect Mission", true, 50, 0, 70));
        }

        // Tournament Registration: eligible cultivators sign up during registration window (5% chance)
        if (location.tournamentTimer > 0 && majorLvl >= location.tournamentMinMajor
                && majorLvl <= location.tournamentMaxMajor
                && !location.tournamentRegistrants.contains(this)
                && rand.nextInt(20) == 0
                && spotQuests.stream().noneMatch(q -> "Register Tournament".equals(q.name))) {
            spotQuests.add(new Quest("Register Tournament", true, 60, 0, 80));
        }

        // Spectate Tournament: any character nearby can watch (10% chance during registration)
        if (location.tournamentTimer > 0 && !location.tournamentRegistrants.contains(this)
                && rand.nextInt(10) == 0
                && spotQuests.stream().noneMatch(q -> "Spectate Tournament".equals(q.name))) {
            spotQuests.add(new Quest("Spectate Tournament", true, 20, 0, 40));
        }

        // Get Pregnant: married females with a spouse present (2% chance/turn)
        if (gender.equals("Female") && spouse != null && !pregnant && rand.nextInt(50) == 0
                && targetQuests.stream().noneMatch(q -> "Get Pregnant".equals(q.name))) {
            targetQuests.add(new Quest("Get Pregnant", true, 25, 0, 45, spouse));
        }

        Quest tempQuest = this.spotQuests.peek();
        int questCode = -2;

        // If currentQuest exists, make sure it's viable. If not, make it null
        if (currentQuest != null) {
            if (questViable(currentQuest) == 0) {
                currentQuest.questDeconstructor();
                currentQuest = null;
            }
        }
        // If currentQuest is null, go through queue until find viable quest or empty
        if (currentQuest == null) {
            while (questCode != -3 && questCode != 1) {
                if (targetQuests.peek() != null) {
                    currentQuest = targetQuests.poll();
                    if (questViable(currentQuest) == 1) {
                        questCode = 1;
                    } else {
                        currentQuest.questDeconstructor();
                        currentQuest = null;
                    }
                }
                else {
                    questCode = -3;
                }
            }
        }

        // If currentQuest exists and is viable, do pathfinding
        if (questCode != -3) {
            if (currentQuest.category == Quest.QuestCategory.Character) {
                Character target = characters.get(currentQuest.targetCharacter);
                if (target != null && !target.dead) {
                    currentQuest.targetArea = target.location;
                }
            }
            questCode = pathfinder(currentQuest.targetArea);
            if (questCode == 1) {
                // Do Target Actions
                questCode = targetQuests(currentQuest);
            }
        }

        // If currentQuest wasn't activated, do Spot
        if (questCode != 1) {
            // Do Spot
            if (tempQuest != null) {
                tempQuest = spotQuests.poll();
                questCode = spotQuests(tempQuest);
            }
            else {
                // spotQuests queue exhausted
            }
        }

        if (dead) return; // died during quest execution; dead() already cleaned up quests

        if (questCode == 1 && currentQuest != null && currentQuest.oneTime) {
            currentQuest.questDeconstructor();
            currentQuest = null;
            targetQuests.forEach(q -> q.priority += q.priorityDiff);
        }
        else if (questCode == 1 || questCode == -1) {
            targetQuests.forEach(q -> q.priority += q.priorityDiff);
            if (currentQuest != null) {
                currentQuest.priority = 0;
                targetQuests.add(currentQuest);
            }
        }
        else if (questCode == 2 && tempQuest.oneTime) {
            tempQuest.questDeconstructor();
            tempQuest = null;
        }
        else if (questCode == 2 || questCode == -2) {
            tempQuest.priority = 0;
            spotQuests.add(tempQuest);
        }

        // Increase priority of all spotQuests
        spotQuests.forEach(q -> q.priority += q.priorityDiff);
    }



    public int questViable(Quest quest) {
        switch (quest.category) {
            case Spot:
                System.out.println("Error in questViable Spot");
                return 0;
            case Character:
                Character character = characters.get(quest.targetCharacter);
                if (character == null || character.dead) return 0;
                if ("Propose Alliance".equals(quest.name)) {
                    return (sect != null && !sect.isAtWar() && character.sect != null && !sect.isAlly(character.sect)) ? 1 : 0;
                }
                if ("Demand Tribute".equals(quest.name)) {
                    return (sect != null && !sect.isAtWar() && character.sect != null && lvl - character.lvl >= 20) ? 1 : 0;
                }
                if ("Recruit Defector".equals(quest.name)) {
                    return (sect != null && sect.isAtWar() && character.sect != null && character.sect != sect
                            && sect.warTarget != null && character.sect == sect.warTarget
                            && sect.warTarget.getRank(character) == Organization.SectRank.ELDER) ? 1 : 0;
                }
                return 1;
            case Area:
                if ("Go Sell".equals(quest.name)) {
                    return (quest.targetArea != null && quest.targetArea.city && quest.targetArea.cityOwner != null) ? 1 : 0;
                }
                if ("Hunt Beast".equals(quest.name)) {
                    return (quest.targetArea != null && quest.targetArea.monsters > 0) ? 1 : 0;
                }
                if ("Join Sect".equals(quest.name)) {
                    return (quest.targetArea != null && quest.targetArea.sect != null && sect == null) ? 1 : 0;
                }
                if ("Restock Trip".equals(quest.name)) {
                    return (quest.targetArea != null && quest.targetArea.city && quest.targetArea.cityOwner != null
                            && quest.targetArea.backpack != null && !quest.targetArea.backpack.isEmpty()) ? 1 : 0;
                }
                if ("Sell Stock".equals(quest.name)) {
                    // Cancel if we have nothing left to sell or destination city is gone
                    boolean hasCargoToSell = backpack.stream().anyMatch(
                        i -> i.itemType != Item.ItemType.Weapon && i.itemType != Item.ItemType.Clothing
                             || (i != equippedWeapon && i != equippedClothing));
                    return (quest.targetArea != null && quest.targetArea.city
                            && quest.targetArea.cityOwner != null && hasCargoToSell) ? 1 : 0;
                }
                if ("Buy Gear".equals(quest.name)) {
                    return (quest.targetArea != null && quest.targetArea.city && !quest.targetArea.shops.isEmpty()) ? 1 : 0;
                }
                if ("Explore Dungeon".equals(quest.name)) {
                    return (quest.targetArea != null && quest.targetArea.dungeonType != null) ? 1 : 0;
                }
                if ("Sect Raid".equals(quest.name)) {
                    if (sect == null || quest.targetArea == null || quest.targetArea.sect == null) return 0;
                    // During a target sect's succession crisis, any sect can raid (no war required)
                    if (quest.targetArea.sect.inCrisis()) return 1;
                    return (sect.isAtWar() && quest.targetArea.sect == sect.warTarget) ? 1 : 0;
                }
                if ("Sell Technique".equals(quest.name)) {
                    return (quest.targetArea != null && quest.targetArea.city && !quest.targetArea.libraries.isEmpty()) ? 1 : 0;
                }
                if ("Seek Potential".equals(quest.name)) {
                    return (quest.targetArea != null && quest.targetArea.city
                            && quest.targetArea.pillBag != null
                            && quest.targetArea.pillBag.stream().anyMatch(p ->
                                    p.itemType == Item.ItemType.Pill && p.pillType == Item.PillType.Potential)) ? 1 : 0;
                }
                if ("Return to Sect".equals(quest.name)) {
                    return (sect != null && quest.targetArea != null && quest.targetArea == sect.location) ? 1 : 0;
                }
                if ("Deliver Materials".equals(quest.name)) {
                    return (sect != null && sectMission != null && quest.targetArea != null && quest.targetArea == sect.location) ? 1 : 0;
                }
                return 1;

        }
        return 0;
    }

    public int spotQuests (Quest quest) {
        int questSuccess = -2;
        switch (quest.name) {
            //Write Spot Case Here
            case "Random":
                questSuccess = event();
                //Testing
                //System.out.println("Random completed");
                break;
            case "Profession":
                questSuccess = professionPicker();
                break;
            case "Hunt":
                beastBattleCheck();
                if (sect != null) sectPoints += 2;
                if (sectMission != null) {
                    String[] mp = sectMission.split("\\|");
                    if (mp.length >= 4 && "Hunt".equals(mp[0]) && "Beast".equals(mp[1])) {
                        int remaining = Integer.parseInt(mp[3]) - 1;
                        if (remaining <= 0) {
                            sectPoints += sectMissionBonus;
                            if (player > 0 && skipping <= 0) System.out.println("Hunt mission complete! +" + sectMissionBonus + " SP");
                            sectMission = null; sectMissionBonus = 0;
                        } else {
                            sectMission = mp[0] + "|" + mp[1] + "|" + mp[2] + "|" + remaining + (mp.length > 4 ? "|" + mp[4] : "");
                        }
                    }
                }
                questSuccess = 2;
                break;
            case "Hunt Cultivator": {
                for (Character c : new ArrayList<>(location.characters)) {
                    if (c == null || c.dead || c == this || c.trait == Traits.Beast || c.trait == Traits.Monster) continue;
                    battleCheck(c);
                    break;
                }
                questSuccess = 2;
                break;
            }
            case "Interact":
                Character target = location.characters.get(rand.nextInt(location.characters.size()));
                if (target != null) {
                    questSuccess = 2; // Don't forget to change this
                    interact(target);
                }
                else {

                }
                break;

            case "Forage":
                forage();
                if (sect != null) sectPoints += 2;
                if (sectMission != null) {
                    String[] mp = sectMission.split("\\|");
                    if (mp.length >= 4 && "Forage".equals(mp[0]) && "Herb".equals(mp[1])) {
                        int missionLv = Integer.parseInt(mp[2]);
                        int missionQty = Integer.parseInt(mp[3]);
                        boolean hasEnough = materialBag.stream().anyMatch(m ->
                            m.itemType == Item.ItemType.Herb && m.lvl >= missionLv && m.quantity >= missionQty);
                        if (hasEnough && sect != null) {
                            if (location == sect.location) {
                                String itemName = "Herb Lvl: " + missionLv;
                                int removed = removeItems(materialBag, itemName, missionQty);
                                if (removed > 0) {
                                    boolean merged = false;
                                    for (Item t : sect.treasury) { if (t.name.equals(itemName)) { t.quantity += removed; merged = true; break; } }
                                    if (!merged) sect.treasury.add(new Item(Item.ItemType.Herb, missionLv, removed));
                                }
                                sectPoints += sectMissionBonus;
                                if (player > 0 && skipping <= 0) System.out.println("Sect mission complete! +" + sectMissionBonus + " SP");
                                sectMission = null; sectMissionBonus = 0;
                            } else if (targetQuests.stream().noneMatch(q -> "Deliver Materials".equals(q.name))) {
                                targetQuests.add(new Quest("Deliver Materials", false, 65, 3, 80, sect.location));
                            }
                        }
                    }
                }
                questSuccess = 2;
                break;

            case "Mine":
                mine();
                if (sect != null) sectPoints += 2;
                if (sectMission != null) {
                    String[] mp = sectMission.split("\\|");
                    if (mp.length >= 4 && "Mine".equals(mp[0]) && "Metal".equals(mp[1])) {
                        int missionLv = Integer.parseInt(mp[2]);
                        int missionQty = Integer.parseInt(mp[3]);
                        boolean hasEnough = materialBag.stream().anyMatch(m ->
                            m.itemType == Item.ItemType.Metal && m.lvl >= missionLv && m.quantity >= missionQty);
                        if (hasEnough && sect != null) {
                            if (location == sect.location) {
                                String itemName = "Metal Lvl: " + missionLv;
                                int removed = removeItems(materialBag, itemName, missionQty);
                                if (removed > 0) {
                                    boolean merged = false;
                                    for (Item t : sect.treasury) { if (t.name.equals(itemName)) { t.quantity += removed; merged = true; break; } }
                                    if (!merged) sect.treasury.add(new Item(Item.ItemType.Metal, missionLv, removed));
                                }
                                sectPoints += sectMissionBonus;
                                if (player > 0 && skipping <= 0) System.out.println("Sect mission complete! +" + sectMissionBonus + " SP");
                                sectMission = null; sectMissionBonus = 0;
                            } else if (targetQuests.stream().noneMatch(q -> "Deliver Materials".equals(q.name))) {
                                targetQuests.add(new Quest("Deliver Materials", false, 65, 3, 80, sect.location));
                            }
                        }
                    }
                }
                questSuccess = 2;
                break;

            case "Test":
                //System.out.println("Test completed");
                questSuccess = 2;
                //Quest onetime = new Quest("One Time", true, 80, 5, 90);
                //spotQuests.add(onetime);
                break;

            case "Sell Technique": {
                if (location.city && !location.libraries.isEmpty()) {
                    Library lib = location.libraries.get(0);
                    Technique best = null;
                    for (Technique t : techniqueList) {
                        final String tn = t.name;
                        boolean alreadyIn = lib.techniques.stream().anyMatch(lt -> lt.name.equals(tn));
                        if (!alreadyIn && (best == null || t.potential > best.potential)) best = t;
                    }
                    if (best != null) {
                        lib.sellTechnique(this, new Technique(best));
                        if (player > 0 && skipping <= 0) System.out.println("Sold " + best.name + " to city library");
                    }
                }
                questSuccess = 2;
                break;
            }

            case "Hold Tournament": {
                if (location.tournamentTimer > 0) { questSuccess = -2; break; }
                int minMajor = Math.max(0, (majorLvl / 2) * 2);
                int maxMajor = Math.min(10, minMajor + 1);
                int prizeLvl = Math.min(100, (maxMajor + 1) * 10 + rand.nextInt(11));
                Item prize;
                if (rand.nextInt(2) == 0) {
                    Item.WeaponType[] wts = Item.WeaponType.values();
                    prize = new Item(this, wts[rand.nextInt(wts.length)], prizeLvl);
                } else {
                    prize = new Item(this, prizeLvl, Item.ItemType.Clothing);
                }
                long prizeCost = itemPrice(prize);
                if (!canAfford(prizeCost)) { questSuccess = -2; break; }
                deductSs(prizeCost);
                location.tournamentTimer = 8;
                location.tournamentMinMajor = minMajor;
                location.tournamentMaxMajor = maxMajor;
                location.tournamentPrize = prize;
                location.tournamentOrganizer = this;
                location.tournamentRegistrants.clear();
                if (player > 0 && skipping <= 0) System.out.println("=== Tournament announced at " + location.name + "! Realm " + minMajor + "-" + maxMajor + ", 8 weeks registration. Prize: " + prize + " ===");
                questSuccess = 2;
                break;
            }

            case "Register Tournament": {
                if (location.tournamentTimer <= 0 || location.tournamentRegistrants.contains(this)) { questSuccess = -2; break; }
                if (majorLvl < location.tournamentMinMajor || majorLvl > location.tournamentMaxMajor) { questSuccess = -2; break; }
                location.tournamentRegistrants.add(this);
                if (player > 0 && skipping <= 0) System.out.println("Registered for tournament at " + location.name);
                questSuccess = 2;
                break;
            }

            case "Spectate Tournament": {
                if (location.tournamentTimer <= 0) { questSuccess = -2; break; }
                if (player > 0 && skipping <= 0) System.out.println("Watching tournament registration at " + location.name + " — " + location.tournamentRegistrants.size() + " signed up so far.");
                questSuccess = 2;
                break;
            }

            case "Accept Sect Mission": {
                if (sect == null || location != sect.location || sect.questBoard.isEmpty() || sectMission != null) { questSuccess = -2; break; }
                sectMission = sect.questBoard.get(rand.nextInt(sect.questBoard.size()));
                String[] parts = sectMission.split("\\|");
                sectMissionBonus = parts.length >= 5 ? Integer.parseInt(parts[4]) : 10;
                if (player > 0 && skipping <= 0) System.out.println("Accepted sect mission: " + parts[0] + " " + parts[1] + " (Lv." + parts[2] + "x" + parts[3] + ") for " + sectMissionBonus + " SP bonus");
                questSuccess = 2;
                break;
            }

            case "Loot Corpse":
                eventLootDead();
                questSuccess = 2;
                break;

            case "Buy City Array": {
                if (!location.city || location.cityOwner != this) { questSuccess = -2; break; }
                int arrayLvl = Math.min(100, (majorLvl + 1) * 10);
                Item array = new Item(this, arrayLvl, Item.ItemType.Array);
                long arrayCost = itemPrice(array);
                if (!canAfford(arrayCost)) { questSuccess = -2; break; }
                deductSs(arrayCost);
                location.arrayBag.add(array);
                if (player > 0 && skipping <= 0) System.out.println("Installed " + array + " at " + location.name);
                questSuccess = 2;
                break;
            }

            case "Entice Merchant": {
                if (!location.city || location.cityOwner != this) { questSuccess = -2; break; }
                Character merchant = null;
                for (Character c : new ArrayList<>(characters)) {
                    if (c == null || c.dead || c.trait != Traits.Merchant) continue;
                    if (c.ownedShops.stream().noneMatch(s -> s.location == location)) { merchant = c; break; }
                }
                if (merchant == null) { questSuccess = -2; break; }
                long incentive = Math.max(10000L, (long)(majorLvl + 1) * 50000);
                if (!canAfford(incentive)) { questSuccess = -2; break; }
                deductSs(incentive);
                merchant.ss += incentive;
                merchant.targetQuests.add(new Quest("Restock Trip", true, 80, 5, 100, location));
                if (player > 0 && skipping <= 0) System.out.println("Enticed " + merchant + " to open a shop in " + location.name);
                questSuccess = 2;
                break;
            }

            case "Donate to Sect": {
                if (sect == null || location != sect.location) { questSuccess = -2; break; }
                int earnedPoints = 0;
                boolean usesHerbs2  = trait == Traits.Alchemist || trait == Traits.Tailor || trait == Traits.Scribe;
                boolean usesMetals2 = trait == Traits.Blacksmith;
                // Donate all non-equipped backpack items (spare combat loot)
                for (Item item : new ArrayList<>(backpack)) {
                    if (item == equippedWeapon || item == equippedClothing) continue;
                    sect.treasury.add(item);
                    backpack.remove(item);
                    earnedPoints += sect.donationPoints(item, 1);
                }
                // Herbs/metals: non-users donate ALL; users keep 5000, donate surplus
                for (Item item : new ArrayList<>(materialBag)) {
                    boolean isHerb  = item.itemType == Item.ItemType.Herb;
                    boolean isMetal = item.itemType == Item.ItemType.Metal;
                    if (!isHerb && !isMetal) continue;
                    int keep   = (isHerb && usesHerbs2) || (isMetal && usesMetals2) ? 5000 : 0;
                    int donate = Math.max(0, item.quantity - keep);
                    if (donate <= 0) continue;
                    item.quantity -= donate;
                    if (item.quantity <= 0) materialBag.remove(item);
                    boolean merged = false;
                    for (Item t : sect.treasury) {
                        if (t.name.equals(item.name)) { t.quantity += donate; merged = true; break; }
                    }
                    if (!merged) {
                        Item copy = new Item(item.itemType, item.lvl, donate - 1); // qty = 1+(donate-1) = donate
                        copy.name = item.name;
                        sect.treasury.add(copy);
                    }
                    earnedPoints += sect.donationPoints(item, donate);
                }
                // Pills: donate what's no longer useful
                for (Item pill : new ArrayList<>(pillBag)) {
                    int donate = 0;
                    if (pill.lvl <= majorLvl * 10) {
                        donate = pill.quantity; // realm already surpassed — all useless
                    } else if (pill.pillType == Item.PillType.Potential && potential >= 100) {
                        donate = pill.quantity; // potential maxed — potential pills useless
                    } else if (pill.pillType == Item.PillType.Cultivation) {
                        donate = Math.max(0, pill.quantity - 5); // keep reserve of 5 at current+ level
                    }
                    if (donate <= 0) continue;
                    pill.quantity -= donate;
                    if (pill.quantity <= 0) pillBag.remove(pill);
                    boolean merged = false;
                    for (Item t : sect.treasury) {
                        if (t.name.equals(pill.name)) { t.quantity += donate; merged = true; break; }
                    }
                    if (!merged) {
                        Item copy = new Item(Item.ItemType.Pill, pill.pillType, pill.lvl, donate);
                        sect.treasury.add(copy);
                    }
                    earnedPoints += sect.donationPoints(pill, donate);
                }
                sectPoints += earnedPoints;
                if (player > 0 && skipping <= 0) System.out.println("Donated to sect treasury — earned " + earnedPoints + " sect points.");
                questSuccess = 2;
                break;
            }

            case "Buy from Sect": {
                if (sect == null || location != sect.location || sect.treasury.isEmpty()) { questSuccess = -2; break; }
                for (Item item : new ArrayList<>(sect.treasury)) {
                    if (item.quantity <= 0) { sect.treasury.remove(item); continue; }
                    boolean wantHerb  = item.itemType == Item.ItemType.Herb
                            && (trait == Traits.Alchemist || trait == Traits.Tailor || trait == Traits.Scribe)
                            && materialBag.stream().noneMatch(m -> m.itemType == Item.ItemType.Herb && m.quantity >= 1000);
                    boolean wantMetal = item.itemType == Item.ItemType.Metal && trait == Traits.Blacksmith
                            && materialBag.stream().noneMatch(m -> m.itemType == Item.ItemType.Metal && m.quantity >= 1000);
                    if (wantHerb || wantMetal) {
                        int buyQty = Math.min(1000, item.quantity);
                        int cost = sect.buyingCost(item, buyQty);
                        if (sectPoints >= cost && cost > 0) {
                            sectPoints -= cost;
                            item.quantity -= buyQty;
                            if (item.quantity <= 0) sect.treasury.remove(item);
                            boolean merged = false;
                            for (Item m : materialBag) {
                                if (m.name.equals(item.name)) { m.quantity += buyQty; merged = true; break; }
                            }
                            if (!merged) {
                                Item copy = new Item(item.itemType, item.lvl, buyQty - 1);
                                copy.name = item.name;
                                materialBag.add(copy);
                            }
                            if (player > 0 && skipping <= 0) System.out.println("Bought " + buyQty + "x " + item.name + " from sect treasury for " + cost + " sect points.");
                            break;
                        }
                    }
                    if (item.itemType == Item.ItemType.Weapon && (equippedWeapon == null || item.lvl > equippedWeapon.lvl + 5)) {
                        int cost = sect.buyingCost(item, 1);
                        if (sectPoints >= cost) {
                            sectPoints -= cost;
                            sect.treasury.remove(item);
                            backpack.add(item);
                            equip();
                            if (player > 0 && skipping <= 0) System.out.println("Bought " + item + " from sect treasury for " + cost + " sect points.");
                            break;
                        }
                    }
                    if (item.itemType == Item.ItemType.Clothing && (equippedClothing == null || item.lvl > equippedClothing.lvl + 5)) {
                        int cost = sect.buyingCost(item, 1);
                        if (sectPoints >= cost) {
                            sectPoints -= cost;
                            sect.treasury.remove(item);
                            backpack.add(item);
                            equip();
                            if (player > 0 && skipping <= 0) System.out.println("Bought " + item + " from sect treasury for " + cost + " sect points.");
                            break;
                        }
                    }
                }
                questSuccess = 2;
                break;
            }

        }


        return questSuccess;
    }

    public int targetQuests (Quest quest) {
        switch (quest.name) {
            case "Find Person":
                Character person = characters.get(quest.targetCharacter);
                if (relationshipList.contains(person.id) != null) {
                    if (relationshipList.contains(person.id).value > 0) {
                        interact(person);
                        // Make a gifting function when possible / sparring
                    } else if (relationshipList.contains(person.id).value < 0) {
                        battleCheck(person);
                    } else {
                        interact(person);
                    }
                    return 1;
                }
                else {
                    return -1;
                }

            case "Go Sell":
                if (location.city && location.cityOwner != null) {
                    sell(location);
                    location.population += Math.max(1, (int)(ss / 1000000));
                }
                return 1;

            case "Sell Technique": {
                if (location.city && !location.libraries.isEmpty()) {
                    Library lib = location.libraries.get(0);
                    Technique best = null;
                    for (Technique t : techniqueList) {
                        final String tn = t.name;
                        boolean alreadyIn = lib.techniques.stream().anyMatch(lt -> lt.name.equals(tn));
                        if (!alreadyIn && (best == null || t.potential > best.potential)) best = t;
                    }
                    if (best != null) {
                        lib.sellTechnique(this, new Technique(best));
                        if (player > 0 && skipping <= 0) System.out.println("Sold " + best.name + " to city library");
                    }
                }
                return 1;
            }

            case "Battle Dislike": {
                Character bully = characters.get(quest.targetCharacter);
                if (bully != null && !bully.dead) {
                    bullyCheck(bully);
                }
                return 1;
            }
            case "Battle Rival":
            case "Battle Arch-Enemy":
            case "Revenge":
            case "Bounty Hunt": {
                Character enemy = characters.get(quest.targetCharacter);
                if (enemy != null && !enemy.dead) {
                    partyBattleCheck(enemy);
                }
                return 1;
            }
            case "Propose Alliance": {
                Character target = characters.get(quest.targetCharacter);
                if (target != null && !target.dead && target.sect != null && sect != null
                        && !sect.isAlly(target.sect) && !sect.isAtWar()) {
                    sect.proposeAlliance(target.sect);
                    if (player > 0 || target.player > 0)
                        System.out.println(sect + " and " + target.sect + " have formed an alliance!");
                }
                return 1;
            }
            case "Demand Tribute": {
                Character target = characters.get(quest.targetCharacter);
                if (target != null && !target.dead && target.sect != null && sect != null
                        && lvl - target.lvl >= 20) {
                    long tribute = 10000000L;
                    if (target.canAfford(tribute)) {
                        target.deductSs(tribute);
                        ss += tribute;
                        convertCurrency();
                        if (player > 0 || target.player > 0)
                            System.out.println(this + " demands tribute from " + target.sect);
                    } else {
                        if (!sect.isAtWar()) {
                            sect.declareWar(target.sect);
                            if (player > 0 || target.player > 0)
                                System.out.println(sect + " declared war on " + target.sect + " over unpaid tribute!");
                        }
                    }
                }
                return 1;
            }
            case "Recruit Defector": {
                Character target = characters.get(quest.targetCharacter);
                if (target != null && !target.dead && target.sect != null && sect != null && target.sect != sect) {
                    Organization oldSect = target.sect;
                    boolean accepts = rand.nextInt(100) < 40; // 40% base defection chance
                    if (accepts) {
                        oldSect.resign(target);
                        target.sectPoints = 0;
                        sect.join(target);
                        if (oldSect.warTarget == null) oldSect.declareWar(sect);
                        if (player > 0 || target.player > 0)
                            System.out.println(target + " has defected from " + oldSect + " to " + sect + "!");
                    }
                }
                return 1;
            }
            case "Hunt Beast":
                beastBattleCheck();
                if (sect != null) sectPoints += 5;
                return 1;
            case "Seek Territory":
                beastBattleCheck();
                return 1;
            case "Heaven Treasure": {
                if (location.heavenlyTechnique != null) {
                    techniqueList.add(location.heavenlyTechnique);
                    if (player > 0 && skipping <= 0) System.out.println(this + " claimed the heavenly technique: " + location.heavenlyTechnique.name + "!");
                    location.heavenlyTechnique = null;
                }
                return 1;
            }
            case "Rob Caravan": {
                Character merchant = characters.get(quest.targetCharacter);
                if (merchant != null && !merchant.dead) partyBattleCheck(merchant);
                return 1;
            }
            case "Join Sect":
                if (location.sect != null && sect == null && location.sect.canJoin(this)) {
                    location.sect.join(this);
                }
                return 1;
            case "Return to Sect":
                if (sect != null) sectPoints += 2;
                return 1;
            case "Deliver Materials": {
                if (sect != null && sectMission != null) {
                    String[] mp = sectMission.split("\\|");
                    if (mp.length >= 4) {
                        int mLv = Integer.parseInt(mp[2]);
                        int mQty = Integer.parseInt(mp[3]);
                        boolean isHerb = "Herb".equals(mp[1]);
                        Item.ItemType mType = isHerb ? Item.ItemType.Herb : Item.ItemType.Metal;
                        String itemName = (isHerb ? "Herb Lvl: " : "Metal Lvl: ") + mLv;
                        int removed = removeItems(materialBag, itemName, mQty);
                        if (removed > 0) {
                            boolean merged = false;
                            for (Item t : sect.treasury) { if (t.name.equals(itemName)) { t.quantity += removed; merged = true; break; } }
                            if (!merged) sect.treasury.add(new Item(mType, mLv, removed));
                            sectPoints += sectMissionBonus;
                            if (player > 0 && skipping <= 0) System.out.println("Materials delivered to sect! +" + sectMissionBonus + " SP");
                        }
                        sectMission = null; sectMissionBonus = 0;
                    }
                }
                return 1;
            }
            case "Restock Trip": {
                if (location.city && location.cityOwner != null) {
                    boolean hasShopHere = ownedShops.stream().anyMatch(s -> s.location == location);
                    if (!hasShopHere && ownedShops.size() < maxShops()) {
                        Shop shop = new Shop(this, location);
                        ownedShops.add(shop);
                        location.shops.add(shop);
                    }
                    for (Shop shop : ownedShops) {
                        if (shop.location == location) shop.restockFromCity(location);
                    }
                }
                return 1;
            }
            case "Sell Stock": {
                if (!location.city || location.cityOwner == null) return 0;
                // Sell all non-equipped, non-spare backpack items into this city's pool at full price
                Item spareW = null, spareA = null;
                for (Item i : backpack) {
                    if (i.itemType == Item.ItemType.Weapon && i != equippedWeapon)
                        if (spareW == null || i.lvl > spareW.lvl) spareW = i;
                    if (i.itemType == Item.ItemType.Clothing && i != equippedClothing)
                        if (spareA == null || i.lvl > spareA.lvl) spareA = i;
                }
                ArrayList<Item> cargo = new ArrayList<>();
                for (Item i : backpack) {
                    if (i == equippedWeapon || i == equippedClothing || i == spareW || i == spareA) continue;
                    cargo.add(i);
                }
                long earned = 0;
                for (Item item : cargo) {
                    long price = itemPrice(item);
                    earned += price;
                    boolean merged = false;
                    for (Item ci : location.backpack) {
                        if (ci.name.equals(item.name) && ci.lvl == item.lvl) { ci.quantity += item.quantity; merged = true; break; }
                    }
                    if (!merged) location.backpack.add(item);
                }
                backpack.removeAll(cargo);
                ss += earned;
                convertCurrency();
                location.population += Math.max(1, (int)(earned / 1000000L));
                if (player > 0 && skipping <= 0) System.out.println(this + " sold cargo at " + location.name + " for " + earned + " ss");
                return 1;
            }
            case "Recruit Member": {
                Character recruit = characters.get(quest.targetCharacter);
                if (recruit != null && !recruit.dead && sect != null && sect.canJoin(recruit)) {
                    interact(recruit);
                    sect.join(recruit);
                }
                return 1;
            }
            case "Explore Dungeon": {
                Area dungeon = quest.targetArea;
                if (dungeon == null || dungeon.dungeonType == null) return 1;
                switch (dungeon.dungeonType) {
                    case LOST_RUINS:
                        if (dungeon.monsters > 0) beastBattleCheck();
                        long treasure = (long)(dungeon.lvl) * dungeon.lvl * 100L;
                        ss += treasure; convertCurrency();
                        if (rand.nextInt(15) == 0) {
                            Technique.techniqueType tt = rand.nextInt(2) == 0 ? Technique.techniqueType.Movement : Technique.techniqueType.Martial;
                            techniqueList.add(new Technique(tt, this, Math.max(1, rand.nextInt(dungeon.lvl / 2 + 1))));
                            if (player > 0 && skipping <= 0) System.out.println(this + " found an ancient technique in the Lost Ruins!");
                        }
                        break;
                    case BEAST_LAIR:
                        for (int i = 0; i < 3 && dungeon.monsters > 0; i++) beastBattleCheck();
                        break;
                    case MARTIAL_TRIAL:
                        tempLvlCheck();
                        int trialPower = Math.max(1, dungeon.lvl / 5);
                        int trialWin = Math.max(5, Math.min(95, 50 + 5 * (tempLvl - trialPower)));
                        if (rand.nextInt(100) < trialWin) {
                            if (martialTechnique != null) martialTechnique.upgrade(this);
                            for (int i = 0; i < 3; i++) cultivate();
                            if (player > 0 && skipping <= 0) System.out.println(this + " passed the Martial Trial!");
                        }
                        break;
                    case HAUNTED_GROUNDS:
                        for (int i = 0; i < 5; i++) cultivate();
                        if (rand.nextInt(25) == 0 && potential < 100) {
                            potential++;
                            if (player > 0 && skipping <= 0) System.out.println(this + " gained potential in the Haunted Grounds!");
                        }
                        break;
                    case CELESTIAL_TRIAL:
                        for (int i = 0; i < 3 && dungeon.monsters > 0; i++) beastBattleCheck();
                        for (int i = 0; i < 20; i++) cultivate();
                        if (rand.nextInt(4) == 0 && potential < 100) {
                            potential++;
                            if (player > 0 && skipping <= 0) System.out.println(this + " gained potential at the Celestial Trial!");
                        }
                        if (rand.nextInt(10) == 0) {
                            techniqueList.add(new Technique(Technique.techniqueType.Martial, this, Math.max(1, majorLvl)));
                            if (player > 0 && skipping <= 0) System.out.println(this + " comprehended a celestial martial technique!");
                        }
                        break;
                }
                if (sect != null) sectPoints += 10;
                return 1;
            }
            case "Sect Raid": {
                boolean raidSuccess = false;
                if (location.sect != null && sect != null && location.sect != sect) {
                    for (Character c : new ArrayList<>(location.characters)) {
                        if (c == null || c.dead || c == this || c.sect != location.sect) continue;
                        battleCheck(c);
                        raidSuccess = true;
                        // War ends if enemy sect master is dead or sect dissolved
                        if (sect.warTarget != null && (sect.warTarget.master == null || sect.warTarget.master.dead
                                || location.sect == null)) {
                            sect.endWar(sect.warTarget);
                        }
                        break;
                    }
                }
                if (raidSuccess && sect != null) sectPoints += 20;
                return 1;
            }
            case "Buy Gear":
                if (location.city && !location.shops.isEmpty()) {
                    buyGear();
                }
                return 1;
            case "Seek Potential": {
                if (location.city && location.pillBag != null) {
                    Item pill = null;
                    for (Item p : location.pillBag) {
                        if (p.itemType == Item.ItemType.Pill && p.pillType == Item.PillType.Potential) {
                            pill = p; break;
                        }
                    }
                    if (pill != null && canAfford(itemPrice(pill))) {
                        deductSs(itemPrice(pill));
                        location.pillBag.remove(pill);
                        pillBag.add(pill);
                        consume();
                    }
                }
                return 1;
            }

            case "Get Pregnant": {
                Character partner = characters.get(quest.targetCharacter);
                if (partner == null || partner.dead || partner != spouse) return -1;
                if (location == partner.location) {
                    if (!pregnant && rand.nextInt(10) == 0) {
                        pregnant = true;
                        tempCharacters.add(new Character(this));
                        if (player > 0 && skipping <= 0) System.out.println("You are now pregnant with " + partner + "'s child!");
                    }
                    return 1;
                }
                return 0;
            }

            case "Find Parent's Body": {
                if (location == quest.targetArea) {
                    if (player > 0 && skipping <= 0) System.out.println("You found your parent's resting place.");
                    if (rand.nextInt(5) == 0) {
                        potential = Math.min(100, potential + 1);
                        if (player > 0 && skipping <= 0) System.out.println("Your grief has strengthened your resolve. (+1 Potential)");
                    }
                    return 1;
                }
                return 0;
            }

            case "Possess Womb": {
                Character mother = characters.get(quest.targetCharacter);
                if (mother == null || mother.dead || !mother.pregnant || lvl <= mother.lvl) return -1;
                if (location == mother.location) {
                    Character unborn = null;
                    for (Character child : mother.children) {
                        if (child != null && !child.born && !child.dead) { unborn = child; break; }
                    }
                    if (unborn == null) return -1;
                    unborn.trait = Traits.Ghost;
                    unborn.originalName = (originalName != null ? originalName + " → " : "") + name + " " + lastName;
                    if (player > 0 || mother.player > 0) System.out.println(this + " possesses " + mother + "'s unborn child! (was: " + unborn.originalName + ")");
                    // Ghost dies permanently
                    dead = true;
                    remCharacters.add(this);
                    location.characters.remove(this);
                    return 1;
                }
                return 0;
            }

            case "Ghost City Rebirth": {
                if (!location.city) return 0;
                Character rebirth = new Character(location);
                rebirth.trait = Traits.Ghost;
                rebirth.originalName = (originalName != null ? originalName + " → " : "") + name + " " + lastName;
                if (player == 1) { rebirth.player = 1; player = 0; }
                else if (player == 2) { rebirth.player = 2; player = 0; }
                tempCharacters.add(rebirth);
                if (player > 0 || rebirth.player > 0) System.out.println(this + " is reborn as " + rebirth + " in " + location.name + "! (was: " + rebirth.originalName + ")");
                dead = true;
                remCharacters.add(this);
                location.characters.remove(this);
                return 1;
            }

            case "Gift Technique": {
                Character recipient = characters.get(quest.targetCharacter);
                if (recipient == null || recipient.dead) return -1;
                if (location == recipient.location) {
                    Technique bestTq = null;
                    for (Technique t : techniqueList) {
                        if (t.potential > (bestTq == null ? 0 : bestTq.potential)) bestTq = t;
                    }
                    if (bestTq != null) {
                        final String tqName = bestTq.name;
                        if (recipient.techniqueList.stream().noneMatch(t -> t.name.equals(tqName))) {
                            Technique copy = new Technique(bestTq);
                            copy.lvl = Math.max(1, bestTq.lvl / 2); // parent teaches directly — better start
                            recipient.techniqueList.add(copy);
                            if (player > 0 && skipping <= 0) System.out.println("Gave technique " + bestTq.name + " (potential " + bestTq.potential + ") to " + recipient);
                        }
                    }
                    return 1;
                }
                return 0;
            }

        }
        return -1;
    }

    public int pathfinder (Area destination) {
        // Human player in manual control provides their own movement; don't override it
        if (player >= 1 && skipping <= 0) return (location == destination) ? 1 : 0;

        int xDiff = location.x - destination.x;
        int yDiff = location.y - destination.y;
        int divider = range / 2;

        if (xDiff == 0) {
            // Don't move left or right
        }
        else if (xDiff < -divider || (xDiff >= 1 && xDiff <= divider)) {
            // Move Left
            this.movement('a');
        }
        else {
            // Move right
            this.movement('d');
        }

        if (yDiff == 0) {
            // Don't move left or right
        }
        else if (yDiff < -divider || (yDiff >= 1 && yDiff <= divider)) {
            // Move Left
            this.movement('s');
        }
        else {
            // Move right
            this.movement('w');
        }

        if (location == destination) {
            return 1;
        }
        else {
            return 0;
        }

    }

    public int event() {
        Character target = location.characters.get(rand.nextInt(location.characters.size()));
        int result = -2;
        switch (eventChoosing()) {
            case 1:
                beastBattleCheck();
                result = 2;
                break;
            case 2:
                interact(target);
                result = 2;
                break;
            case 5:
                professionPicker();
                result = 2;
                break;
            case 6:
                eventLootDead();
                result = 2;
                break;
        }
        return result;
    }

    public int professionPicker () {
        int success = -2;
        switch (trait) {
            case Alchemist:
                success = alchemy();
                if (success == -2) {
                    forage();
                    if (spotQuests.stream().noneMatch(q -> "Forage".equals(q.name)))
                        spotQuests.add(new Quest("Forage", false, 40, 3, 70));
                }
                return success;
            case Blacksmith:
                if (materialBag.stream().noneMatch(i -> i.itemType == Item.ItemType.Metal && i.quantity >= 1000))
                    if (spotQuests.stream().noneMatch(q -> "Mine".equals(q.name)))
                        spotQuests.add(new Quest("Mine", false, 40, 3, 70));
                smithing();
                break;
            case Friendly:
                Character target = location.characters.get(rand.nextInt(location.characters.size()));
                if (target != null) {
                    interact(target);
                    success = 2;
                }
                else {
                    success = -2;
                }
                break;
            case Tailor:
                if (materialBag.stream().noneMatch(i -> i.itemType == Item.ItemType.Herb && i.quantity >= 1000))
                    if (location.herbMaterials > 0 && spotQuests.stream().noneMatch(q -> "Forage".equals(q.name)))
                        spotQuests.add(new Quest("Forage", false, 40, 3, 70));
                if (materialBag.stream().noneMatch(i -> i.itemType == Item.ItemType.Metal && i.quantity >= 1000))
                    if (location.metalMaterials > 0 && spotQuests.stream().noneMatch(q -> "Mine".equals(q.name)))
                        spotQuests.add(new Quest("Mine", false, 40, 3, 70));
                tailoring();
                break;
            case Scribe:
                if (materialBag.stream().noneMatch(i -> i.quantity >= 10000))
                    if (spotQuests.stream().noneMatch(q -> "Forage".equals(q.name)))
                        spotQuests.add(new Quest("Forage", false, 40, 3, 70));
                array();
                if (!arrayBag.isEmpty()) installArray(location);
                break;
            case Leader:
                if (professionLvl == 1) {
                    if (rand.nextInt(5) == 0) {
                        cityLeadership(); // 20%: city path (sets professionLvl to 50 on success)
                    } else {
                        professionLvl = 50; // 80%: skip straight to sect track
                    }
                }
                if (professionLvl == 50) {
                    sectLeadership();
                }
                break;
            case Innovative:
                if (rand.nextInt(100) == 0) {
                    technique();
                }
                if (equippedWeapon != null && equippedWeapon.enchantment == null && rand.nextInt(20) == 0) {
                    String[] enchants = {"Sharp", "Spiritual", "Swift", "Heavy", "Fierce", "Wrathful", "Sundering"};
                    equippedWeapon.enchantment = enchants[rand.nextInt(enchants.length)];
                    equippedWeapon.enchantBonus = rand.nextInt(5) + 1;
                    if (player > 0 && skipping <= 0) System.out.println(this + " enchanted their weapon: " + equippedWeapon);
                }
                break;
            case Merchant:
                if (location.city && location.cityOwner != null) {
                    boolean hasShopHere = ownedShops.stream().anyMatch(s -> s.location == location);
                    if (!hasShopHere && ownedShops.size() < maxShops()) {
                        Shop shop = new Shop(this, location);
                        ownedShops.add(shop);
                        location.shops.add(shop);
                    }
                    Shop myShop = ownedShops.stream().filter(s -> s.location == location).findFirst().orElse(null);
                    if (myShop != null) {
                        myShop.restockFromCity(location);
                        // Cargo pickup: buy from city pool into backpack for inter-city transport
                        if (backpack.size() < 15 && location.backpack != null && !location.backpack.isEmpty()
                                && targetQuests.stream().noneMatch(q -> "Sell Stock".equals(q.name))
                                && (currentQuest == null || !"Sell Stock".equals(currentQuest.name))) {
                            boolean pickedUp = false;
                            for (Item item : new ArrayList<>(location.backpack)) {
                                if (backpack.size() >= 15) break;
                                long cost = Math.max(1, (long)(itemPrice(item) * 0.30));
                                if (canAfford(cost)) {
                                    deductSs(cost);
                                    if (location.cityOwner != null) location.cityOwner.ss += cost;
                                    location.backpack.remove(item);
                                    backpack.add(item);
                                    pickedUp = true;
                                }
                            }
                            if (pickedUp) {
                                Area sellCity = findBestSellCity();
                                if (sellCity != null)
                                    targetQuests.add(new Quest("Sell Stock", true, 40, 3, 70, sellCity));
                            }
                        }
                        // Equip better gear from own stock instead of leaving it on a shelf
                        for (Item item : new ArrayList<>(myShop.inventory)) {
                            if (item.itemType == Item.ItemType.Weapon
                                    && (equippedWeapon == null || item.lvl > equippedWeapon.lvl)) {
                                if (equippedWeapon != null) myShop.inventory.add(equippedWeapon);
                                myShop.inventory.remove(item);
                                backpack.add(item);
                                equip();
                            } else if (item.itemType == Item.ItemType.Clothing
                                    && (equippedClothing == null || item.lvl > equippedClothing.lvl)) {
                                if (equippedClothing != null) myShop.inventory.add(equippedClothing);
                                myShop.inventory.remove(item);
                                backpack.add(item);
                                equip();
                            }
                        }
                    }
                }
                break;
            case Lucky:
                eventLootDead();
                break;
            default:
                break;
        }
        return -2;
    }
    public void eventLootDead() {
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
        // Returns: 1=beast battle, 2=interact, 5=profession, 6=loot dead
        // Evil: slightly more beast fighting; Good/Neutral: balanced
        int eventNum = rand.nextInt(10000);
        switch (affiliationNum) {
            case -1: // Evil
                if (eventNum < 1500) return 1;       // 15% beast battle
                else if (eventNum < 4500) return 2;  // 30% interact
                else if (eventNum < 8000) return 5;  // 35% profession
                else return 6;                        // 20% loot dead
            case 0:  // Neutral
                if (eventNum < 1000) return 1;        // 10% beast battle
                else if (eventNum < 4500) return 2;   // 35% interact
                else if (eventNum < 8500) return 5;   // 40% profession
                else return 6;                         // 15% loot dead
            case 1:  // Good
                if (eventNum < 700) return 1;          // 7% beast battle
                else if (eventNum < 4700) return 2;    // 40% interact
                else if (eventNum < 8700) return 5;    // 40% profession
                else return 6;                          // 13% loot dead
            default:
                return 5;
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
            if (player > 0 && skipping <= 0 && age >= 8) {
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
            if (player > 0 && skipping <= 0 && age >= 8) {
                System.out.println("Discovered " + dropAmount + " metal. Quality: " + rewardLvl);
            }
        }
    }
    public int alchemy() {
        int success = -2;
        for (int i = 0; i <= professionLvl/10; i++) {
            boolean hasBlood = false;
            boolean hasItem = false;
            Item material = null;
            // Selects the herb to be used
            for (Item item : materialBag) {
                if (item.itemType == Item.ItemType.Blood && item.quantity >= 1000) {
                    hasBlood = true;
                    material = item;
                    success = 2;
                    break;
                }
                else if (item.itemType == Item.ItemType.Herb && item.quantity >= 50) {
                    if (material == null) {
                        if (item.lvl <= professionLvl/10 + 2) {
                            material = item;
                            success = 2;
                        }
                    }
                    else if (material.lvl < item.lvl && item.lvl <= professionLvl/10 + 2) {
                        material = item;
                        success = 2;
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
                    i += professionLvl/10 + 1;
                }
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
                itemsCrafted++;
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
        if (player > 0 && skipping <= 0 && age >= 8) {
            System.out.println("Created a few pills");
        }
        return success;
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
                    if (player > 0 && skipping <= 0 && age >= 8) {
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

            //Checks to see if Affiliation is the same or Good vs Evil
            if (this.affiliation.equals("Evil")) {
                if (rand.nextInt(100) == 0) {
                    battleCheck(player);
                }
            }
            else if ((this.affiliation.equals("Good") && player.affiliation.equals("Evil")) ) {
                if (rand.nextInt(100) == 0) {
                    battleCheck(player);
                }
            }
            // Sparring: close friends spar together (10% chance when rel >= 1)
            Relationship relVal = relationshipList.contains(player.id);
            if (relVal != null && relVal.value >= 1 && !sparring && !player.sparring && rand.nextInt(10) == 0) {
                this.sparring = true;
                player.sparring = true;
                battleCheck(player);
            }

            // Changes the Relationship Value of this Character and the targeted Player

            changeRelationship(player, 0);
            checkBirth(player);

            if ((this.player > 0 || player.player > 0) && skipping <= 0) {
                System.out.println(this + " talked to " + player);
            }
        }
    }
    public void changeRelationship (Character player, int change) {
        // Checks to see if the relationship doesn't exist. If it doesn't, it creates the relationship
        if (relationshipList.contains(player.id) == null) {
            relationshipList.add(new Relationship(player.id, 0));
            this.targetQuests.add(new Quest("Find Person", false, 0, 0, 20, player));
        }
        if (player.relationshipList.contains(this.id) == null) {
            player.relationshipList.add(new Relationship(this.id, 0));
            player.targetQuests.add(new Quest("Find Person", false, 0, 0, 20, this));
        }

        // Ensure both sides have an active Find Person quest (in targetQuests or currentQuest)
        boolean hasQuest1 = this.targetQuests.stream()
                .anyMatch(q -> q.targetCharacter == player.id && "Find Person".equals(q.name))
                || (this.currentQuest != null && this.currentQuest.targetCharacter == player.id
                    && "Find Person".equals(this.currentQuest.name));
        boolean hasQuest2 = player.targetQuests.stream()
                .anyMatch(q -> q.targetCharacter == this.id && "Find Person".equals(q.name))
                || (player.currentQuest != null && player.currentQuest.targetCharacter == this.id
                    && "Find Person".equals(player.currentQuest.name));

        if (!hasQuest1) {
            this.targetQuests.add(new Quest("Find Person", false, 0, 0, 20, player));
        }
        if (!hasQuest2) {
            player.targetQuests.add(new Quest("Find Person", false, 0, 0, 20, this));
        }

        // Changes the value of the relationship in the hash table
        Relationship temp = relationshipList.contains(player.id);
        Relationship temp2 = player.relationshipList.contains(this.id);
        int value = temp.value;
        if (change == 0) {
            change = changeRelationshipUtil(player);
        }
        if (value + change > 3) {
            change = 3;
        }
        else if (value + change < -3) {
            change = -3;
        }
        else {
            change += value;
        }
        temp.changeValue(change);
        temp2.changeValue(change);

        if (change < 0) {
            final String bName;
            final int bPriority;
            final int bPriorityDiff;
            final int bMaxPriority;
            if (change == -1) {
                bName = "Battle Dislike";   bPriority = 20; bPriorityDiff = 3; bMaxPriority = 40;
            } else if (change == -2) {
                bName = "Battle Rival";     bPriority = 35; bPriorityDiff = 5; bMaxPriority = 60;
            } else {
                bName = "Battle Arch-Enemy"; bPriority = 50; bPriorityDiff = 8; bMaxPriority = 80;
            }
            if (this.targetQuests.stream().noneMatch(q -> q.targetCharacter == player.id && bName.equals(q.name)))
                this.targetQuests.add(new Quest(bName, false, bPriority, bPriorityDiff, bMaxPriority, player));
            if (player.targetQuests.stream().noneMatch(q -> q.targetCharacter == this.id && bName.equals(q.name)))
                player.targetQuests.add(new Quest(bName, false, bPriority, bPriorityDiff, bMaxPriority, this));
        }

        checkRelationship(player);

        if (change == 0){
            relationshipList.remove(temp);
            player.relationshipList.remove(temp2);
            temp = null;
            temp2 = null;
        }
    }
    public int changeRelationshipUtil(Character player) {
        int random = rand.nextInt(1000000) + 1;
        int positive = 0;
        int negative = 0;

        // Same Affiliation
        if (this.affiliationNum == player.affiliationNum) {
            positive = 250000;
            negative = 50000;
        }
        // Good vs Evil
        else if (Math.abs(this.affiliationNum) == Math.abs(player.affiliationNum)) {
            positive = 50000;
            negative = 250000;
        }
        // Neutral Good / Neutral Evil
        else {
            positive = 100000;
            negative = 100000;
        }

        if (this.mother == player.mother || this.father == player.father) {
            positive *= 2;
            negative /= 2;
        }

        int gap = (this.lvl - player.lvl) / 10;
        int relationshipScore = this.relationshipList.contains(player.id).value;
        positive /= Math.pow(2, gap + relationshipScore);
        negative /= Math.pow(2, gap + relationshipScore);

        if (random <= positive) {
            return 1;
        }
        else if (random < (1000000 - negative)) {
            return 0;
        }
        else {
            return -1;
        }
    }

    public void checkRelationship(Character player) {
        int relationship = relationshipList.contains(player.id).value;

        if (this.age >= 18 && player.age >= 18 && spouse == null && player.spouse == null && relationship >= 3 && this.gender != player.gender && this.mother != player && player.mother != this && this.father != player && player.father != this && !siblings.contains(player) && !this.dead && !player.dead) {
            spouse = player;
            player.spouse = this;
            if (printBreakthroughs || this.player > 0 || player.player > 0) System.out.println(this + " and " + player + " are now married! Congratulations!");
            if (gender.equals("Female")) {
                this.lastName = player.lastName;
            }
            else {
                player.lastName = this.lastName;
            }
        }
        int value = Math.abs(relationship);

        if (value == 0) {
            // Remove Find Person quests when relationship dissolves to neutral
            targetQuests.removeIf(q -> q.targetCharacter == player.id && "Find Person".equals(q.name));
            player.targetQuests.removeIf(q -> q.targetCharacter == this.id && "Find Person".equals(q.name));
        }
        else {
            // Ensure both sides have an active Find Person quest (in targetQuests or currentQuest)
            boolean hasQuest1 = this.targetQuests.stream()
                    .anyMatch(q -> q.targetCharacter == player.id && "Find Person".equals(q.name))
                    || (this.currentQuest != null && this.currentQuest.targetCharacter == player.id
                        && "Find Person".equals(this.currentQuest.name));
            boolean hasQuest2 = player.targetQuests.stream()
                    .anyMatch(q -> q.targetCharacter == this.id && "Find Person".equals(q.name))
                    || (player.currentQuest != null && player.currentQuest.targetCharacter == this.id
                        && "Find Person".equals(player.currentQuest.name));

            if (!hasQuest1) {
                this.targetQuests.add(new Quest("Find Person", false, 0, 0, 20, player));
            }
            if (!hasQuest2) {
                player.targetQuests.add(new Quest("Find Person", false, 0, 0, 20, this));
            }

            targetQuests.forEach(q -> {
                if (q.targetCharacter == player.id && "Find Person".equals(q.name)) {
                    q.priorityDiff = value;
                    q.priority = value;
                    q.maxPriority = 20 * value;
                }
            });

            player.targetQuests.forEach(q -> {
                if (q.targetCharacter == this.id && "Find Person".equals(q.name)) {
                    q.priorityDiff = value;
                    q.priority = value;
                    q.maxPriority = 20 * value;
                }
            });
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
            if (!temp.pregnant && rand.nextInt(10) == 0) {
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
            int rawLvl = rand.nextInt(material.lvl * 5) + (1 + ((material.lvl * 10) / 2));
            weapon = new Item(this, temp, Math.min(rawLvl, Math.min((majorLvl + 1) * 10, 100)));
            material.quantity -= 1000;
        }

        if (weapon != null) {
            backpack.add(weapon);
            itemsCrafted++;

            //Leveling up your profession skill
            if (professionLvl < 100) {
                professionExp += weapon.lvl * 3;
                while (professionExp > professionExpMax && professionLvl < 100) {
                    professionLvl++;
                    professionExp -= professionExpMax;
                    professionExpMax = Math.pow(10, professionLvl / 10.);
                    if (professionLvl == 100 && (player > 0 && skipping <= 0)) {
                        System.out.println(this + " reached max blacksmithingLvl");
                    }
                }
            }
            if (player > 0 && skipping <= 0 && age >= 8) {
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
            int rawLvl = rand.nextInt(material.lvl * 5) + (1 + ((material.lvl * 10) / 2));
            clothing = new Item(this, Math.min(rawLvl, Math.min((majorLvl + 1) * 10, 100)), Item.ItemType.Clothing);
            material.quantity -= 1000;
        }

        if (clothing != null) {
            backpack.add(clothing);
            itemsCrafted++;

            //Leveling up your profession skill
            if (professionLvl < 100) {
                professionExp += clothing.lvl * 3;
                while (professionExp > professionExpMax && professionLvl < 100) {
                    professionLvl++;
                    professionExp -= professionExpMax;
                    professionExpMax = Math.pow(10, professionLvl / 10.);
                }
            }
            if (player > 0 && skipping <= 0 && age >= 8) {
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
            int rawLvl = rand.nextInt(material.lvl * 5) + (1 + ((material.lvl * 10) / 2));
            array = new Item(this, Math.min(rawLvl, Math.min((majorLvl + 1) * 10, 100)), Item.ItemType.Array);
            material.quantity -= 1000;
        }

        if (array != null) {
            arrayBag.add(array);
            itemsCrafted++;

            //Leveling up your profession skill
            if (professionLvl < 100) {
                professionExp += array.lvl * 3;
                while (professionExp > professionExpMax && professionLvl < 100) {
                    professionLvl++;
                    professionExp -= professionExpMax;
                    professionExpMax = Math.pow(10, professionLvl / 10.);
                }
            }
            if (player > 0 && skipping <= 0 && age >= 8) {
                System.out.println("Created " + array);
            }
        }
    }
    public void convertCurrency() {
        final long RATE = 1_000_000L;
        while (ss >= RATE) { sc++; ss -= RATE; }
        while (sc >= RATE) { dc++; sc -= RATE; }
        while (dc >= RATE) { ij++; dc -= RATE; }
    }

    public boolean canAfford(long amount) {
        if (amount <= 0) return true;
        if (ss >= amount) return true;
        final long RATE = 1_000_000L;
        long remaining = amount - ss;
        if (sc >= (remaining + RATE - 1) / RATE) return true;
        remaining -= sc * RATE;
        if (remaining <= 0) return true;
        if (dc > 0 && remaining / RATE / RATE <= dc) return true;
        return ij > 0;
    }

    public void deductSs(long amount) {
        if (amount <= 0) return;
        final long RATE = 1_000_000L;
        if (ss >= amount) { ss -= amount; return; }
        long needed = amount - ss;
        ss = 0;
        if (sc > 0) {
            long scDraw = Math.min(sc, (needed + RATE - 1) / RATE);
            sc -= scDraw;
            ss += scDraw * RATE;
        }
        if (ss >= needed) { ss -= needed; return; }
        needed -= ss; ss = 0;
        if (dc > 0) {
            long dcDraw = Math.min(dc, (needed + RATE - 1) / RATE);
            dc -= dcDraw;
            sc += dcDraw * RATE;
            long scDraw = Math.min(sc, (needed + RATE - 1) / RATE);
            sc -= scDraw;
            ss += scDraw * RATE;
        }
        if (ss >= needed) { ss -= needed; return; }
        needed -= ss; ss = 0;
        if (ij > 0) {
            ij--;
            dc += RATE;
            long dcDraw = Math.min(dc, (needed + RATE - 1) / RATE);
            dc -= dcDraw;
            sc += dcDraw * RATE;
            long scDraw = Math.min(sc, (needed + RATE - 1) / RATE);
            sc -= scDraw;
            ss += scDraw * RATE;
        }
        ss = Math.max(0, ss - needed);
    }

    public int maxShops() { return Math.max(1, majorLvl); }

    // Scores known cities by inverse pool size (scarcity = demand) and proximity; excludes current city.
    private Area findBestSellCity() {
        Area best = null;
        int bestScore = -1;
        for (Area area : knownLocations) {
            if (area == null || !area.city || area.cityOwner == null || area == location) continue;
            int poolSize = (area.backpack != null) ? area.backpack.size() : 0;
            int score = Math.max(0, 50 - poolSize);  // empty pool = max demand
            int dist = Math.abs(area.x - location.x) + Math.abs(area.y - location.y);
            score += Math.max(0, 20 - dist);          // closer = easier to reach
            if (score > bestScore) { bestScore = score; best = area; }
        }
        return best;
    }

    // Scores known cities by pool size (A), own shop presence (B), and proximity (C).
    private Area findBestRestockCity() {
        Area best = null;
        int bestScore = -1;
        for (Area area : knownLocations) {
            if (area == null || !area.city || area.cityOwner == null
                    || area.backpack == null || area.backpack.isEmpty()) continue;
            int score = area.backpack.size() * 10;
            for (Shop s : ownedShops) { if (s.location == area) { score += 50; break; } }
            int dist = Math.abs(area.x - location.x) + Math.abs(area.y - location.y);
            score += Math.max(0, 30 - dist);
            if (score > bestScore) { bestScore = score; best = area; }
        }
        return best;
    }

    public void buyGear() {
        if (!location.city || location.shops.isEmpty()) return;
        int currentWeaponLvl = equippedWeapon != null ? equippedWeapon.lvl : -1;
        int currentArmorLvl  = equippedClothing != null ? equippedClothing.lvl : -1;
        boolean boughtWeapon = false;
        boolean boughtArmor = false;
        for (Shop shop : location.shops) {
            if (!boughtWeapon) {
                for (Item item : new ArrayList<>(shop.inventory)) {
                    long price = shop.sellPrice(item);
                    if (item.itemType == Item.ItemType.Weapon
                            && item.lvl >= lvl - 9             // within one major realm of current level
                            && item.lvl > currentWeaponLvl    // strictly better than equipped
                            && canAfford(price)
                            && ss <= price * 4) {             // spend at most 25% of ss
                        if (shop.sellToBuyer(this, item)) { equip(); boughtWeapon = true; break; }
                    }
                }
            }
            if (!boughtArmor) {
                for (Item item : new ArrayList<>(shop.inventory)) {
                    long price = shop.sellPrice(item);
                    if (item.itemType == Item.ItemType.Clothing
                            && item.lvl >= lvl - 9
                            && item.lvl > currentArmorLvl
                            && canAfford(price)
                            && ss <= price * 4) {
                        if (shop.sellToBuyer(this, item)) { equip(); boughtArmor = true; break; }
                    }
                }
            }
            if (boughtWeapon && boughtArmor) break;
        }
    }

    public static long itemPrice(Item item) {
        if (item.lvl <= 0) return 0;
        long base = Math.max(1, (long)(Math.pow(item.lvl, 2.5) * 10));
        if (item.itemType == Item.ItemType.Pill && item.pillType == Item.PillType.Potential)
            return base > Long.MAX_VALUE / 5 ? Long.MAX_VALUE : base * 5;
        return base;
    }

    public void sell(Area city) {
        // Identify spare weapon/clothing to keep as backup in case equipped item breaks
        Item spareWeapon = null;
        Item spareClothing = null;
        for (Item item : backpack) {
            if (item.itemType == Item.ItemType.Weapon && item != equippedWeapon) {
                if (spareWeapon == null || item.lvl > spareWeapon.lvl) spareWeapon = item;
            }
            if (item.itemType == Item.ItemType.Clothing && item != equippedClothing) {
                if (spareClothing == null || item.lvl > spareClothing.lvl) spareClothing = item;
            }
        }
        ArrayList<Item> toSell = new ArrayList<>();
        for (Item item : backpack) {
            if (item == equippedWeapon || item == equippedClothing) continue;
            if (item == spareWeapon   || item == spareClothing)    continue;
            toSell.add(item);
        }
        // Find best-paying merchant shop in this city (beats city pool 0.50 rate)
        Shop bestShop = null;
        for (Shop shop : city.shops) {
            if (bestShop == null || shop.buyRate > bestShop.buyRate) bestShop = shop;
        }

        // buyFromSeller() handles backpack removal and ss payment directly
        ArrayList<Item> soldToMerchant = new ArrayList<>();
        long merchantEarned = 0;
        if (bestShop != null) {
            for (Item item : new ArrayList<>(toSell)) {
                long price = bestShop.buyPrice(item);
                if (bestShop.buyFromSeller(this, item)) {
                    soldToMerchant.add(item);
                    merchantEarned += price;
                }
            }
        }

        // Remaining items go to city pool at base 50% rate
        long poolEarned = 0;
        ArrayList<Item> remaining = new ArrayList<>(toSell);
        remaining.removeAll(soldToMerchant);
        for (Item item : remaining) {
            long price = itemPrice(item) / 2;
            poolEarned += price;
            if (city.backpack != null) {
                boolean merged = false;
                for (Item cityItem : city.backpack) {
                    if (cityItem.name.equals(item.name) && cityItem.lvl == item.lvl) {
                        cityItem.quantity += item.quantity;
                        merged = true;
                        break;
                    }
                }
                if (!merged) city.backpack.add(item);
            }
        }
        backpack.removeAll(remaining);
        ss += poolEarned;
        convertCurrency();
        // Sell pills: cultivation pills at or below current majorLvl are past-realm useless
        // Never sell Potential Pills if potential < 100 (still need them)
        long pillEarned = 0;
        ArrayList<Item> pillsToSell = new ArrayList<>();
        for (Item item : pillBag) {
            if (item.pillType == Item.PillType.Potential) {
                if (potential >= 100) pillsToSell.add(item);
            } else if (item.pillType == Item.PillType.Cultivation) {
                if (item.lvl <= majorLvl * 10) pillsToSell.add(item);
            }
        }
        for (Item pill : pillsToSell) {
            pillEarned += itemPrice(pill) * pill.quantity;
            if (city.pillBag != null) {
                boolean merged = false;
                for (Item cityPill : city.pillBag) {
                    if (cityPill.name.equals(pill.name) && cityPill.lvl == pill.lvl) {
                        cityPill.quantity += pill.quantity;
                        merged = true;
                        break;
                    }
                }
                if (!merged) city.pillBag.add(pill);
            }
        }
        pillBag.removeAll(pillsToSell);
        ss += pillEarned;
        convertCurrency();
        if (player > 0 && skipping <= 0 && age >= 8) {
            long totalEarned = merchantEarned + poolEarned + pillEarned;
            System.out.println("Sold " + (toSell.size() + pillsToSell.size()) + " items for " + totalEarned + " ss at " + city.name);
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
        if (technique != null) itemsCrafted++;

        //Leveling up your profession skill
        if (professionLvl < 100) {
            professionExp += (technique.lvl + 1) * 3;
            while (professionExp > professionExpMax && professionLvl < 100) {
                professionLvl++;
                professionExp -= professionExpMax;
                professionExpMax = Math.pow(10, professionLvl / 10.);
            }
        }
        if (player > 0 && skipping <= 0 && age >= 8) {
            System.out.println("Created " + technique);
        }
        // Donate a copy to sect library (earns sectPoints)
        if (technique != null && sect != null && sect.sectLibrary != null) {
            final String tname = technique.name;
            boolean notYetIn = sect.sectLibrary.techniques.stream().noneMatch(t -> t.name.equals(tname));
            if (notYetIn && rand.nextInt(5) == 0) {
                sect.sectLibrary.techniques.add(new Technique(technique));
                sectPoints += 10;
            }
        }
    }
    public void cityLeadership() {
        if (ss >= 1000000 && !location.city && rand.nextInt(100) == 0) {
            location.city = true;
            location.cityOwner = this;
            location.population = rand.nextInt((startingPopulation * 100000) - startingPopulation + 1) + startingPopulation;
            location.backpack = new ArrayList<>();
            location.materialBag = new ArrayList<>();
            location.pillBag = new ArrayList<>();
            location.arrayBag = new ArrayList<>();
            location.libraries.add(new Library(location));
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
        if (movementTechnique2 != null && !techniqueList.contains(movementTechnique2)) {
            techniqueList.add(movementTechnique2);
        }
        if (martialTechnique2 != null && !techniqueList.contains(martialTechnique2)) {
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
    /** Remove up to `qty` of a named stackable item from a bag. Returns the amount actually removed. */
    public static int removeItems(ArrayList<Item> bag, String name, int qty) {
        for (int i = 0; i < bag.size(); i++) {
            Item item = bag.get(i);
            if (item.name.equals(name)) {
                int removed = Math.min(item.quantity, qty);
                item.quantity -= removed;
                if (item.quantity <= 0) bag.remove(i);
                return removed;
            }
        }
        return 0;
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

    public void bullyCheck(Character target) {
        if (target == null || target.dead) return;
        if (lvl > target.lvl + 15) {
            // Much stronger: intimidate and steal 25% of their ss
            long stolen = target.ss / 4;
            if (stolen > 0) {
                target.deductSs(stolen);
                ss += stolen;
                convertCurrency();
            }
            target.changeRelationship(this, -1);
        } else {
            // Close enough in level: escalates to a fight
            battleCheck(target);
        }
    }

    public void battleCheck(Character opponent) {
        if (trait == Traits.Beast && !opponent.dead && opponent.time >= 0) {
            // Beasts are blocked by arrays
            if (opponent.location.arrayBag != null && !opponent.location.arrayBag.isEmpty()) {
                return;
            }
            if (opponent.age < 8 && opponent.mother != null && !opponent.mother.dead) {
                battle(opponent.mother);
                return;
            } else {
                battle(opponent);
                return;
            }
        }
        if (age < 8 && !sparring) {
            return;
        }
        if (relationshipList.contains(opponent.id) != null) {
            if (!sparring && relationshipList.contains(opponent.id).value >= 1) {
                return;
            }
        }

        // Affiliation rules: Good won't attack non-Evil unless arch-enemy; Neutral won't attack non-Evil unless rival or worse
        if (!sparring && !"Evil".equals(opponent.affiliation)) {
            Relationship rel = relationshipList.contains(opponent.id);
            int relValue = rel != null ? rel.value : 0;
            if ("Good".equals(affiliation) && relValue > -3) return;
            if ("Neutral".equals(affiliation) && relValue > -2) return;
        }

        // Array protection: attacker must break the strongest array before engaging
        if (opponent.location.arrayBag != null && !opponent.location.arrayBag.isEmpty()) {
            Item strongestArray = null;
            for (Item a : opponent.location.arrayBag) {
                if (strongestArray == null || a.lvl > strongestArray.lvl) strongestArray = a;
            }
            if (strongestArray != null) {
                this.tempLvlCheck();
                opponent.tempLvlCheck();
                int arrayPower = strongestArray.lvl / 5;
                if (arrayPower > this.tempLvl) {
                    // Array stronger than attacker — defender won't interfere; just fight the array
                    arrayBreakAttempt(opponent.location);
                    return;
                }
                // Array weak/equal — must break it, then fight defender
                if (!arrayBreakAttempt(opponent.location)) return;
            }
        }

        // Decreases Relationship
        changeRelationship(opponent, 0);

        // Sect squabble escalation: repeated cross-sect fights lead to war
        if (sect != null && opponent.sect != null && sect != opponent.sect && !sect.isAtWar()) {
            sect.squabbles++;
            if (sect.squabbles >= 5) {
                sect.declareWar(opponent.sect);
                if (player > 0 || opponent.player > 0) System.out.println(sect + " has declared war on " + opponent.sect + "!");
            }
        }

        if (this != opponent && !opponent.dead && opponent.time >= 0) {
            if (opponent.age < 8 && opponent.mother != null && !opponent.mother.dead) {
                battle(opponent.mother);
            } else {
                battle(opponent);
            }
        }
    }

    // Sequential tag-team: this character leads; positive-relationship allies step in if the previous fighter loses.
    // Enemy accumulates exhaustion after each fight.
    public void partyBattleCheck(Character opponent) {
        if (opponent == null || opponent.dead || this == opponent) return;
        if (age < 8 && !sparring) return;

        // Build party: lead attacker + nearby allies with positive relationship
        ArrayList<Character> party = new ArrayList<>();
        party.add(this);
        for (Character c : new ArrayList<>(location.characters)) {
            if (c == null || c.dead || c == this || c == opponent || c.trait == Traits.Beast) continue;
            Relationship r = relationshipList.contains(c.id);
            if (r != null && r.value >= 1) party.add(c);
        }
        // Strongest ally leads
        for (Character p : party) p.tempLvlCheck();
        party.sort((a, b) -> Integer.compare(b.tempLvl, a.tempLvl));

        for (int i = 0; i < party.size(); i++) {
            if (opponent.dead) break;
            Character fighter = party.get(i);
            if (fighter.dead || (fighter.age < 8 && !fighter.sparring)) continue;
            opponent.exhaustionPenalty = i * 2; // enemy tires from consecutive fights
            fighter.battleCheck(opponent);
        }
        opponent.exhaustionPenalty = 0;
    }

    public boolean beastBattleCheck() {
        if (age < 8 || location.monsters <= 0 || lvl < (location.lvl + location.highestLvl) / 2) {
            return false;
        }
        if (lvl < location.lvl) {
            if ( rand.nextInt(4) != 0) {
                return false;
            }
        }
        location.monsters--;
        battle(new Character(location, this));
        return true;
    }

    // Returns true if the attacker breaks the strongest array; removes it regardless of outcome.
    // On failure: 5% chance to die.
    public boolean arrayBreakAttempt(Area area) {
        if (area.arrayBag == null || area.arrayBag.isEmpty()) return true;
        Item strongest = null;
        for (Item a : area.arrayBag) {
            if (strongest == null || a.lvl > strongest.lvl) strongest = a;
        }
        if (strongest == null) return true;
        tempLvlCheck();
        int arrayPower = strongest.lvl / 5;
        int winChance = Math.max(0, Math.min(100, 50 - 15 * (arrayPower - tempLvl)));
        area.arrayBag.remove(strongest);
        if (rand.nextInt(100) < winChance) {
            return true;
        }
        if (rand.nextInt(20) == 0) {
            dead(null);
        }
        return false;
    }

    public void installArray(Area area) {
        if (!arrayBag.isEmpty()) {
            area.arrayBag.add(arrayBag.remove(0));
        }
    }

    public void battle(Character opponent) {
        // (MESSAGE) System.out.println(this + " has challenged " + opponent + " to a fight!");


        // (NOTE) Optimizes Equipment and Techniques and Updates Corresponding TempLvl
        this.tempLvlCheck();
        opponent.tempLvlCheck();

        // Defender gets a boost from arrays protecting their area
        if (opponent.location.arrayBag != null && !opponent.location.arrayBag.isEmpty()) {
            int bestArrayLvl = 0;
            for (Item a : opponent.location.arrayBag) {
                if (a.lvl > bestArrayLvl) bestArrayLvl = a.lvl;
            }
            opponent.tempLvl += bestArrayLvl / 10 + 1;
        }

        // Death Chance and Forbidden Init
        boolean deathChance = deathCheck(opponent);
        boolean forbiddenUsed = false;
        boolean oppForbiddenUsed = false;

        //winChanceCheck(opponent);
        int winChance = 50 - (15 * (opponent.tempLvl - this.tempLvl));
        if (this.trait == Traits.Lucky) {
            winChance += 45;
        }
        if (opponent.trait == Traits.Lucky) {
            winChance -= 45;
        }

        if (winChance < caution) {
            if (fleeCheck(opponent)) {
                return;
            }
            else if (deathChance) {
                forbiddenCheck();
                if (this.dead) { this.dead(opponent); return; }
                forbiddenUsed = true;
            }
        }
        else if (winChance > opponent.caution) {
            if(opponent.fleeCheck(this)) {
                return;
            }
            else if (deathChance) {
                opponent.forbiddenCheck();
                if (opponent.dead) { opponent.dead(this); return; }
                oppForbiddenUsed = true;
            }
        }

        if (deathChance) {
            // Second Win Chance Calculation
            winChance = 50 - (15 * (opponent.tempLvl - this.tempLvl));
            if (this.trait == Traits.Lucky) {
                winChance += 45;
            }
            if (opponent.trait == Traits.Lucky) {
                winChance -= 45;
            }

            if (winChance < caution && oppForbiddenUsed) {
                if (fleeCheck(opponent)) {
                    return;
                } else if (deathChance) {
                    forbiddenCheck();
                    if (this.dead) { this.dead(opponent); return; }
                }
            } else if (winChance > opponent.caution && forbiddenUsed) {
                if (opponent.fleeCheck(this)) {
                    return;
                } else if (deathChance) {
                    opponent.forbiddenCheck();
                    if (opponent.dead) { opponent.dead(this); return; }
                }
            }
        }

        winChance = 50 - (15 * (opponent.tempLvl - this.tempLvl));
        if (this.trait == Traits.Lucky) {
            winChance += 45;
        }
        if (opponent.trait == Traits.Lucky) {
            winChance -= 45;
        }

        durabilityCheck(opponent);

        if ((rand.nextInt(100) + 1) <= winChance) {
            if (player > 0 || opponent.player > 0) System.out.println(this + " won the fight!");
            winCheck(opponent);
        }
        else {
            if (player > 0 || opponent.player > 0) System.out.println(opponent + " won the fight!");
            opponent.winCheck(this);
        }

    }

    // Returns true if a deathmatch, returns false if not
    public boolean deathCheck(Character opponent) {
        int deathChance = 1;

        if (opponent.trait == Traits.Beast || this.trait == Traits.Beast) {
            return true;
        }

        if (relationshipList.contains(opponent.id) != null) {

            switch (relationshipList.contains(opponent.id).value) {
                case -4: //System.out.println("Wanted");
                    deathChance = 100;
                    break;
                case -3: //System.out.println("Arch enemy");
                    deathChance = 75;
                    break;
                case -2: //System.out.println("Rival");
                    deathChance = 25;
                    break;
                case -1: //System.out.println("Dislike");
                    deathChance = 5;
                    break;
                case 0: //System.out.println("Neutral");
                    deathChance = 1;
                    break;
                default: //System.out.println("Friendly");
                    deathChance = 0;
                    break;
            }
        }
        if (deathChance >= (rand.nextInt(100) + 1)){
            return true;
        }
        else {
            return false;
        }
    }

    public void winCheck(Character loser) {
        if (sparring) {
            changeRelationship(loser, 1);
            loser.changeRelationship(this, 1);
            practice();
            loser.practice();
            this.sparring = false;
            loser.sparring = false;
        } else {
            int upgradeChance = (trait == Traits.HeavenlyRoot) ? 2 : 5;
            if (martialTechnique != null && rand.nextInt(upgradeChance) == 0) martialTechnique.upgrade(this);
            Area lostAt = loser.location;
            loser.dead(this);
            // Beast awakening: killing a cultivator may trigger Monster NPC awakening (10% chance)
            if (trait == Traits.Beast && loser.trait != Traits.Beast && loser.dead && rand.nextInt(10) == 0) {
                awakenMonster();
            }
            // Evil or Beast destroys undefended city/sect (20% chance per eligible kill)
            if ((affiliationNum == -1 || trait == Traits.Beast)
                    && lostAt != null && !"Originis".equals(lostAt.name)
                    && (lostAt.city || lostAt.sect != null)
                    && (lostAt.arrayBag == null || lostAt.arrayBag.isEmpty())
                    && rand.nextInt(5) == 0) {
                destroyCity(lostAt);
            }
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

    public void destroyCity(Area area) {
        // Plunder all area bags
        if (area.backpack != null)    { backpack.addAll(area.backpack);       area.backpack.clear();    }
        if (area.materialBag != null) { materialBag.addAll(area.materialBag); area.materialBag.clear(); }
        if (area.pillBag != null)     { pillBag.addAll(area.pillBag);         area.pillBag.clear();     }
        if (area.arrayBag != null)    { arrayBag.addAll(area.arrayBag);       area.arrayBag.clear();    }
        // Blood from population massacre — proportional to city size
        int bloodQty = Math.max(1, (int)(area.population / 100));
        materialBag.add(new Item(Item.ItemType.Blood, bloodQty));
        // Cultivation bonus — 10 extra cultivate() calls from the battle aura
        for (int i = 0; i < 10; i++) cultivate();
        // Potential gain
        if (potential < 101 && rand.nextInt(10) == 0) {
            potential++;
            if (player > 0 && skipping <= 0) System.out.println("The carnage expanded your limits! Potential → " + potential);
        }
        // Plunder coin from city coffers
        ss += area.population;
        convertCurrency();
        // Population devastated — 90% wiped out
        area.population = Math.max(1, (int)(area.population * 0.1));
        // Dissolve sect if present
        if (area.sect != null) {
            Organization destroyedSect = area.sect;
            for (Character member : new ArrayList<>(destroyedSect.members)) {
                if (member != null && !member.dead) {
                    member.sect = null;
                    member.sectMission = null;
                    member.sectMissionBonus = 0;
                }
            }
            destroyedSect.members.clear();
            destroyedSect.memberRanks.clear();
            if (destroyedSect.warTarget != null) destroyedSect.endWar(destroyedSect.warTarget);
            area.sect = null;
        }
        // Destroy city — remove infrastructure
        if (area.city) {
            area.city = false;
            area.cityOwner = null;
            area.shops.clear();
            area.libraries.clear();
        }
        if (player > 0 && skipping <= 0) System.out.println(this + " has destroyed " + area.name + "!");
    }

    public void tempLvlCheck(){
        equip();
        tempLvl = lvl;

        if (equippedClothing != null) {
            tempLvl += equippedClothing.lvl;
        }

        if (equippedWeapon != null) {
            tempLvl += equippedWeapon.lvl + equippedWeapon.enchantBonus;
        }

        if (martialTechnique != null) {
            tempLvl += martialTechnique.lvl;
        }

        if (movementTechnique != null) {
            tempLvl += movementTechnique.lvl;
        }

        tempLvl /= 5;

        if (this.trait == Traits.Cheater) {
            tempLvl += 5;
        }

        if (this.trait == Traits.IronBody) {
            tempLvl += majorLvl / 2 + 1;
        }

        if (exhaustionPenalty > 0) {
            tempLvl = Math.max(1, tempLvl - exhaustionPenalty);
        }

    }

    public void forbiddenCheck() {
        if (trait == Traits.Beast) {
            return;
        }
        else if (age + totalAgeGained/10 > maxAge) {
            tempLvl += Math.sqrt(potential);
            maxAge -= totalAgeGained / 10;
            dead();
            return;
        }
        else {
            tempLvl += Math.sqrt(potential);
            maxAge -= totalAgeGained / 10;
            // Rare potential gain for surviving a forbidden technique (2% chance)
            if (potential < 101 && rand.nextInt(50) == 0) {
                potential++;
                if (player > 0 && skipping <= 0) System.out.println("Surviving the forbidden technique expanded your potential to " + potential + "!");
            }
        }
    }

    public void durabilityCheck(Character opponent) {
        int weaponLvl = 0;
        int armorLvl = 0;
        int oppWeaponLvl = 0;
        int oppArmorLvl = 0;
        int breaker = 0;
        boolean breakWeapon = false;
        boolean breakArmor = false;
        boolean oppBreakWeapon = false;
        boolean oppBreakArmor = false;


        if (equippedWeapon != null) {
            weaponLvl = equippedWeapon.lvl;
        }
        if (equippedClothing != null) {
            armorLvl = equippedClothing.lvl;
        }
        if (opponent.equippedWeapon != null) {
            oppWeaponLvl = opponent.equippedWeapon.lvl;
        }
        if (opponent.equippedClothing != null) {
            oppArmorLvl = opponent.equippedClothing.lvl;
        }

        if (equippedWeapon != null) {
            breaker = Math.max(Math.max(oppWeaponLvl, opponent.lvl), oppArmorLvl);
            breakWeapon = opponent.durabilityCheckUtil(equippedWeapon.lvl, breaker);
        }

        if (equippedClothing != null) {
            breaker = Math.max(oppWeaponLvl, opponent.lvl);
            breakArmor = opponent.durabilityCheckUtil(equippedClothing.lvl, breaker);
        }

        if (opponent.equippedWeapon != null) {
            breaker = Math.max(Math.max(weaponLvl, this.lvl), armorLvl);
            oppBreakWeapon = opponent.durabilityCheckUtil(opponent.equippedWeapon.lvl, breaker);
        }

        if (opponent.equippedClothing != null) {
            breaker =  Math.max(weaponLvl, this.lvl);
            oppBreakArmor = opponent.durabilityCheckUtil(opponent.equippedClothing.lvl, breaker);
        }

        if (breakWeapon) {
            System.out.println("" + equippedWeapon + " Weapon has broken!");
            equippedWeapon.deconstructor();
            this.equippedWeapon = null;
        }
        if (breakArmor) {
            System.out.println("" + equippedClothing + " Armor has broken!");
            equippedClothing.deconstructor();
            this.equippedClothing = null;
        }
        if (oppBreakWeapon) {
            System.out.println("" + opponent.equippedWeapon + " Weapon has broken!");
            opponent.equippedWeapon.deconstructor();
            opponent.equippedWeapon = null;
        }
        if (oppBreakArmor) {
            System.out.println("" + opponent.equippedClothing + " Armor has broken!");
            opponent.equippedClothing.deconstructor();
            opponent.equippedClothing = null;
        }

    }

    public boolean durabilityCheckUtil(int toBreak, int breaker) {
        int intBreaker = (int)(breaker/10);
        int intToBreak = (int)(toBreak/10);
        int breakChance = (int)(Math.pow(2, (intBreaker - intToBreak)) * 10000);

        if (rand.nextInt(100000) + 1 <= breakChance - (900 * intToBreak)) {
            return true;
        }
        return false;
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
                return "Ghost" + (isGhostForm ? " (Ghost Form)" : "") + (originalName != null ? " [was: " + originalName + "]" : "");
            case IronBody:
                return "Iron Body";
            case SpiritVein:
                return "Spirit Vein";
            case HeavenlyRoot:
                return "Heavenly Root";
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
    public void updateRepTitle() {
        if (majorLvl >= 10)  { repTitle = "True Immortal"; return; }
        if (kills >= 500)    { repTitle = "God Slayer"; return; }
        if (majorLvl >= 9)   { repTitle = "Pseudo Immortal Venerable"; return; }
        if (kills >= 100)    { repTitle = gender.equals("Female") ? "Demoness Butcher" : "Demon Butcher"; return; }
        if (majorLvl >= 7)   { repTitle = "Ascension Expert"; return; }
        if (kills >= 50)     { repTitle = gender.equals("Female") ? "Slaughter Maiden" : "Slaughter King"; return; }
        if (professionLvl >= 100 && trait != null) {
            switch (trait) {
                case Alchemist:  repTitle = gender.equals("Female") ? "Pill Queen" : "Pill King"; return;
                case Blacksmith: repTitle = gender.equals("Female") ? "Forge Lady" : "Forge Saint"; return;
                case Tailor:     repTitle = "Robe Saint"; return;
                case Scribe:     repTitle = "Array Sage"; return;
                case Innovative: repTitle = "Technique Ancestor"; return;
                case Merchant:   repTitle = gender.equals("Female") ? "Trade Queen" : "Trade King"; return;
                default: break;
            }
        }
    }

    public String toString() {
        if (dead && age > maxAge) {
            return (name + " " + lastName + " with potential: " + potential + " and cultivation: " + cultivation + " and major level: " + majorLvl +  " and minor level: " + minorLvl + " and inhibitor: " + inhibitor);
        }
        String title = (repTitle != null ? " '" + repTitle + "'" : "");
        return (name + " " + lastName + title);
    }

    public int placeholder() {
        return 0;
    }


}
