package Game;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.PriorityQueue;

import static Game.GameDriver.*;
import static Game.Dictionary.*;
import static Game.Map.*;

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

        // Technique Inheritance
        {
            if (temp.movementTechnique2 != null) {
                movementTechnique = new Technique(temp.movementTechnique2);
            } else {
                movementTechnique = new Technique(temp.movementTechnique);
            }
            if (temp.martialTechnique2 != null) {
                martialTechnique = new Technique(temp.martialTechnique2);
            } else {
                martialTechnique = new Technique(temp.martialTechnique);
            }
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

    // Beast Constructor
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

    public void cultivate() {
        cultivationBase += cultivation;
        if (cultivationBase >= cultivationBaseMax) {
            cultivationBase -= cultivationBaseMax;
            lvl++;
            minorLvl++;
            cultivationBaseMax = Math.pow(lvl, 3) * (((70.+1./3.) + ((11./6.) * lvl)) / 25.);
            cultivation = ((Math.pow(potential,2) * Math.sqrt(lvl) * 10.) / (52. * Math.sqrt(potential) * Math.pow(2,inhibitor)));
            if ((minorLvl >= 11 && majorLvl < 10)) {
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
                        maxAge += 1900;
                        totalAgeGained += 1900;
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
                        maxAge += 3700;
                        totalAgeGained += 3700;
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
                        maxAge += 6100;
                        totalAgeGained += 6100;
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
                        maxAge += 9100;
                        totalAgeGained += 9100;
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
                        maxAge += 13700;
                        totalAgeGained += 13700;
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
                        maxAge += 16900;
                        totalAgeGained += 16900;
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
                        maxAge += 2140000000;
                        totalAgeGained += 2140000000;
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
        if (changeMc && player) {
            killer.player = true;
        }

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
            for (Relationship relationship : relationshipList.relationships) {
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

        // Buy Gear: missing slot, or equipped gear is a full major realm (10 levels) behind character level
        if (location.city && !location.shops.isEmpty()
                && (equippedWeapon == null || equippedClothing == null
                    || (equippedWeapon != null && equippedWeapon.lvl + 10 <= lvl)
                    || (equippedClothing != null && equippedClothing.lvl + 10 <= lvl))) {
            buyGear();
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

        // Restock Trip: Merchant with shops seeks best city for arbitrage restocking (10% chance/turn)
        if (trait == Traits.Merchant && !ownedShops.isEmpty() && rand.nextInt(10) == 0
                && targetQuests.stream().noneMatch(q -> "Restock Trip".equals(q.name))
                && (currentQuest == null || !"Restock Trip".equals(currentQuest.name))) {
            Area bestCity = findBestRestockCity();
            if (bestCity != null) {
                targetQuests.add(new Quest("Restock Trip", true, 30, 2, 60, bestCity));
            }
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
        // If currentQuest is null, go through queue until find viable quest or
        if (currentQuest == null) {
            while (questCode != -3 && questCode != 1) {
                if (targetQuests.peek() != null) {
                    currentQuest = targetQuests.poll();
                    if (questViable(currentQuest) == 1) {
                        questCode = 1;
                    }
                }
                else {
                    questCode = -3;
                    // Testing
                    System.out.println("Priority Queue is Null\n" + targetQuests);
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
                System.out.println("We got a problem with the spotQuests queue... It's empty");
            }
        }

        if (questCode == 1 && currentQuest.oneTime) {
            //Testing
            currentQuest.questDeconstructor();
            currentQuest = null;
            // Increase target quests if target quest completed
            targetQuests.forEach(q -> q.priority += q.priorityDiff);
            System.out.println("One Time Target Quest finished");
        }
        else if (questCode == 1 || questCode == -1) {
            //Testing
            // Increase target quests if target quest completed
            targetQuests.forEach(q -> q.priority += q.priorityDiff);
            currentQuest.priority = 0;
            targetQuests.add(currentQuest);
        }
        else if (questCode == 2 && tempQuest.oneTime) {
            //Testing
            tempQuest.questDeconstructor();
            tempQuest = null;
            System.out.println("One Time Spot Quest finished!?");
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
                if (character == null){
                    return 0;
                }
                else if (character.dead == true) {
                    return 0;
                }
                else {
                    return 1;
                }
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
                questSuccess = 2;
                break;
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
                questSuccess = 2;
                break;

            case "Mine":
                mine();
                questSuccess = 2;
                break;

            case "Test":
                //System.out.println("Test completed");
                questSuccess = 2;
                //Quest onetime = new Quest("One Time", true, 80, 5, 90);
                //spotQuests.add(onetime);
                break;

        }


        return questSuccess;
    }

    public int targetQuests (Quest quest) {
        switch (quest.name) {
            case "Find Person":
                Character person = characters.get(quest.targetCharacter);
                // Testing
                System.out.println(this + " has found " + person);
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
                    System.out.println("Weird Case?");
                    return -1;
                }

            case "Go Sell":
                if (location.city && location.cityOwner != null) {
                    sell(location);
                }
                return 1;

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
                    battleCheck(enemy);
                }
                return 1;
            }
            case "Hunt Beast":
                beastBattleCheck();
                return 1;
            case "Join Sect":
                if (location.sect != null && sect == null) {
                    location.sect.join(this);
                }
                return 1;
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

        }
        return -1;
    }

    public int pathfinder (Area destination) {

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
            System.out.println(this + " has reached their target: " + destination);
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
            //Battle A Player
            case 0:
                //battleCheck(target); // Remove this
                break;
            // Battle a Random Beast
            case 1:
                beastBattleCheck();
                result = 2;
                break;
            case 2:
                interact(target);
                result = 2;
                break;
            //Forage
            case 3:
                //forage(); // Remove this
                break;
            // Mine
            case 4:
                //mine(); // Remove this
                break;
            // Professions
            case 5:
                professionPicker();
                result = 2;
                break;
            // Loot Dead
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
                    if (myShop != null) myShop.restockFromCity(location);
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
            // Changes the Relationship Value of this Character and the targeted Player

            changeRelationship(player, 0);
            checkBirth(player);

            if ((this.player || player.player ) && skipping <= 0 && age >= 8) {
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
            System.out.println(this + " and " + player + " are now married! Congratulations!");
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
    public void convertCurrency() {
        final long RATE = 1_000_000L;
        while (ss >= RATE) { sc++; ss -= RATE; }
        while (sc >= RATE) { dc++; sc -= RATE; }
        while (dc >= RATE) { ij++; dc -= RATE; }
    }

    public boolean canAfford(long amount) { return ss >= amount; }

    public void deductSs(long amount) { ss = Math.max(0, ss - amount); }

    public int maxShops() { return Math.max(1, majorLvl); }

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

    // TODO: price formulas are placeholders — confirm before finalising economy pass
    public static long itemPrice(Item item) {
        if (item.lvl <= 0) return 0;
        long base = (long) Math.pow(100, item.lvl / 10.0);
        if (item.itemType == Item.ItemType.Pill && item.pillType == Item.PillType.Potential)
            return base * 5; // Potential Pills require deaths (blood), more valuable
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
                if (item.lvl <= majorLvl) pillsToSell.add(item);
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
        if (player && skipping <= 0 && age >= 8) {
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
            location.cityOwner = this;
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

        if (this != opponent && !opponent.dead && opponent.time >= 0) {
            if (opponent.age < 8 && opponent.mother != null && !opponent.mother.dead) {
                battle(opponent.mother);
            } else {
                battle(opponent);
            }
        }
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

        System.out.println("TempLvl of " + this + " is " + tempLvl);
        System.out.println("TempLvl of " + opponent + " is " + opponent.tempLvl);

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

        System.out.println(this + "'s chance of winning is " + winChance);

        if (winChance < caution) {
            if (fleeCheck(opponent)) {
                System.out.println("Fleed Fight");
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

            System.out.println(this + "/'s chance of winning is " + winChance);

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

        System.out.println(this + "'s chance of winning is " + winChance);

        durabilityCheck(opponent);

        if ((rand.nextInt(100) + 1) <= winChance) {
            System.out.println(this + " won the fight!");
            winCheck(opponent);
        }
        else {
            System.out.println(opponent + " won the fight!");
            opponent.winCheck(this);
        }

    }

    // Returns true if a deathmatch, returns false if not
    public boolean deathCheck(Character opponent) {
        int deathChance = 1;

        if (opponent.trait == Traits.Beast || this.trait == Traits.Beast) {
            System.out.println("Fighting Beast, No Relation");
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
        else {
            System.out.println(this + " and " + opponent + " fighting with no relation!");
        }


        if (deathChance >= (rand.nextInt(100) + 1)){
            System.out.println("DEATH MATCH");
            return true;
        }
        else {
            return false;
        }
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

        if (equippedClothing != null) {
            tempLvl += equippedClothing.lvl;
        }

        if (equippedWeapon != null) {
            tempLvl += equippedWeapon.lvl;
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

    }

    public void forbiddenCheck() {
        if (trait == Traits.Beast) {
            return;
        }
        else if (age + totalAgeGained/10 > maxAge) {
            tempLvl += Math.sqrt(potential);
            maxAge -= totalAgeGained / 10;
            dead = true;
            return;
        }
        else {
            tempLvl += Math.sqrt(potential);
            maxAge -= totalAgeGained / 10;
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

    public int placeholder() {
        return 0;
    }


}
