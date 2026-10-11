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
        Beast,
        Tamer,
        Poisoner,
        Drunken,
        Chef,
        Musician
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
    double potentialProgress = 0;     // the part of the next point of potential already earned (0..1; see givePotential)
    /** The potential each breakthrough needs: the breakthrough to Major Lvl k (1..10) needs GATE_POTENTIAL[k - 1]. Below it only 1 try in 15 (1 in 10 from Major 6) gets through; the last gate also needs a 1 in 5 roll. */
    static final int[] GATE_POTENTIAL = {20, 37, 52, 65, 76, 85, 92, 97, 99, 100};
    /** How many Potential Pills it takes to climb from the last gate to this one: the cost of a gate rises steadily, so the highest points of potential are the dearest. */
    static final int[] GATE_PILLS = {5, 7, 9, 11, 13, 15, 18, 22, 26, 30};
    /** The potential the breakthrough to this Major Lvl needs (1..10). */
    static int gatePotential(int toMajor) { return GATE_POTENTIAL[Math.max(1, Math.min(10, toMajor)) - 1]; }
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
    int sectMissionTotal = 0;  // how many the mission asked for at the start (the mission text counts down what is left): for the "2/5" counter
    boolean isGhostForm = false;
    String originalName = null; // chain of prior names for Ghost rebirths
    int bloodlineBonus = 0;
    Life.Fx fx = null;                  // food / wine / music / poison conditions (null for most characters)
    ArrayList<Item> goods = null;       // food, wine and poison (see Life)
    ArrayList<Integer> beastIds = null; // tamed beasts (see Taming)
    int skinTone = 0;                   // portrait skin: 0 = whatever the portrait's seed gives, 1-9 = a chosen tone (ArtMore.SKINS; a child of a chosen tone inherits one)
    int masterId = -1;                  // for a tamed beast: its master
    boolean husked = false;             // a dead cultivator whose lists were let go (see Slim.husk)
    boolean wasTamed = false;           // this beast was tamed once (its list slot is cleared by hand when it is freed or dies)
    ArrayList<String> pastLives = null; // earlier lives of a reincarnated soul (see Reincarnation)
    int lives = 0;                      // how many times this soul has been reincarnated
    int soulId = -1;                    // id of this soul's first body (shared by every life of a reincarnated soul)
    ArrayList<Integer> lostBeastIds = null; // companions that stayed behind when this soul was reborn and wait for it (see Taming.waitFor)
    int waitingSoul = -1;               // for a beast: the soul it is waiting for (it has no turn and stays where its master left it)
    int reincarnTry = -1000;            // week of the last reincarnation attempt that found no family (retried yearly)
    transient boolean equipNow = false;  // the player asked for the best gear right now (the `equip` command / Equip best button)
    boolean reincarnating = false;      // the body is being left behind by a reincarnation (not a death)
    Starts.State story = null;        // the chain of objectives of a Story start (null otherwise)
    boolean potentialHidden = false;  // shown to its owner as a word until the first breakthrough
    boolean storyNpc = false;         // a character that belongs to somebody's story and stays in Originis
    int fatedTo = 0;                  // a person of somebody's story: the id of that human + 1 (0 = nobody's). Only that human can kill them; the epoch, age and everyone else leave them alone (see Story.release)
    /** Potential as the owner sees it: the number, or a word while it is still a rumour. */
    /** The potential for the character sheet of someone who plays: the number and, when it has gone a good way toward the next point, how far ("41 (+60%)"). */
    String potentialShown() { String s = potentialLabel(); return potentialHidden || potential >= 100 || player < 1 || potentialProgress < 0.05 ? s : s + " (+" + Math.round(potentialProgress * 100) + "%)"; }
    String potentialLabel() { return potentialHidden ? Starts.potentialWord(potential) : String.valueOf(potential); }
    boolean needsReincarnation = false; // set when a player character dies; resolved by GameDriver
    int blessing = 0;                    // fighting level (tempLvl) and potential a soldier's descendants carry: +5 per soldier in the line (see LoreEndings)
    boolean wanderingImmortal = false;   // a soldier who came back: cannot die, gives hints and gifts (see LoreEndings.wanderTurn)
    Character reincarnationKiller = null; // the killer, if any, for the reincarnation menu
    String deathCause = "natural causes"; // shown in the reincarnation screen
    String repTitle = null;
    int rankStrong = 0, rankRich = 0, rankKills = 0;   // places on the yearly boards (Rankings): 0 = not ranked; Strongest counts to 1000, the others to 100
    int exhaustionPenalty = 0;
    int partyLeaderId = -1;
    int exploreTicks = 0;
    char exploreDir = 'x';
    boolean forbiddenDeclined = false; // the last forbiddenCheck() was declined by a human (so the technique was not used)
    boolean ambusher = false; // a crowd-hotspot beast: kills any cultivator it defeats and never triggers awakening/city destruction
    int autoX = -1;
    int chaseId = -1;    // a human's "go there" is chasing this cultivator (see PlayerCommands.stepAuto); -1 = walking to fixed coordinates
    int chaseTurns = 0;
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
    PlayerControl pc = null; // player-control state (human characters only; created on first use, see pc())
    ArrayList<Shop> ownedShops = null;
    // Relationship variables
    RelationshipList relationshipList;
    Character spouse = null;
    Character father = null;
    Character mother = null;
    ArrayList<Character> siblings;
    ArrayList<Character> children;
    Area location = null;
    int areaIdx = -1; // index in location.characters for O(1) swap-remove
    // Planned moves (see GameDriver's tick): an NPC acts on the tile it stands in and only PLANS its one step; the steps of the whole phase are applied together afterwards,
    // so while the turns run nobody leaves or enters a tile (a mover can still be met, fought or robbed where he stood) and a person who plays can read where each walker is going.
    transient boolean planning;      // true during a turn whose steps are only planned
    transient Area pendTo, pendFrom; // the tile the step leads to, and the tile it was planned from (a step is dropped if the character was moved some other way meanwhile)
    transient int rideId = -1;       // a party member standing with the leader: moves wherever the leader's step has taken him
    transient int movedTick = -1;    // the week of the last step taken or planned
    transient int turnT = -1;        // the week this NPC last had its turn (its plan, or the lack of one, is then known)
    ArrayList<Area> knownLocations;
    static final int POOL_CAP = 400;                       // items a city's pool keeps (the overflow of what is sold to the city is scrapped)
    static final int CRAFT_COST = 500, ARRAY_MIN = 600;  // material units one weapon / piece of clothing / array uses (arrays wanted 10,000 in the bag, then 2,000: a forage gives 10 to 100, so a Scribe never had enough; now 6 to 60 forages)
    int nextRoutineWeek = 0;                          // a rest between the routine quests of QuestLife (visits, retreats, trade runs...)
    transient double[] qTarget, qEma;                // QuestLife: the share of its time this character means to give each family of quests, and what it has really given lately
    transient int qTargetKey = -1;
    int nextPerformWeek = 0;                          // a Musician's next concert (a rest between performances)
    int nextExploreWeek = 0, nextHuntWeek = 0;       // the earliest week the next self-chosen dungeon trip / hunt may start (a rest between trips)
    int amateurLvl = 0; double amateurExp = 0, amateurExpMax = 1;  // everyone's hand at crafting (AmateurCraft): capped at 50
    static final int KNOWN_CAP = 80, KNOWN_SLACK = 16;   // an NPC remembers about 150 places (a human's fog of war never shrinks)
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
        spotQuests = new PriorityQueue<Quest>(4);
        targetQuests = new PriorityQueue<Quest>(4);

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
        location.addChar(this);
        knownLocations.add(location);
        id = GameDriver.nextUid();

        // Gender and Name Picker
        {
            boolean female = rand.nextInt(2) != 0;
            gender = female ? "Female" : "Male";
            String[] full = Naming.person(female, location); // avoids a namesake among the living where it can; the region's culture favours its own names
            name = full[0];
            lastName = full[1];
        }

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

        age = 8; // the constructor's default: the world's starting population and city births set the real starting age (Starts.prehistoryOne)

        trait = Life.rollTrait(trait);
        // Quest Initialization
        questInitializer();
    }

    // Birth Constructor / Enemy Generator
    public Character(Character mother) {
        //Pre-Initialization Stuff
        spotQuests = new PriorityQueue<Quest>(4);
        targetQuests = new PriorityQueue<Quest>(4);

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

        id = GameDriver.nextUid(); // must be assigned before changeRelationship() below, which builds the reciprocal entries from this.id
        this.mother = mother;
        this.father = mother.spouse;
        skinTone = ArtMore.inheritSkin(mother.skinTone, father == null ? 0 : father.skinTone, rand); // a family keeps its colouring (0 = nobody chose: the portrait draws its own)
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
        location.addChar(this);
        knownLocations.add(location);
        time = -40;
        mother.pregnant = true;
        born = false;
        Character temp = null;

        // Gender and Name Picker
        {
            if (rand.nextInt(2) == 0) {
                name = Naming.firstFor(false, father.lastName);
                gender = "Male";
                temp = father;
                if (father.sect != null) {
                    father.sect.join(this);
                }
            } else {
                name = Naming.firstFor(true, father.lastName);
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
            blessing = LoreEndings.inheritedBlessing(mother, father); // descendants of soldiers are born stronger
            if (blessing > 0) potential = Math.min(100, potential + blessing);
            if (LoreEndings.bloodBonus() > 0) potential = Math.min(100, potential + LoreEndings.bloodBonus()); // the blood that went back to the Garden
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

        trait = Life.rollTrait(trait);
        // Quest Initialization
        questInitializer();

    }

    // Beast Constructor
    public Character (Area area, Character cultivator) {
        location = area;
        relationshipList = new RelationshipList();
        spotQuests = new PriorityQueue<>(4);
        targetQuests = new PriorityQueue<>(4);
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

        name = Naming.beastAdjective();
        lvl = rand.nextInt(location.lvl) + 1;
        lastName = Naming.beastSpecies(lvl); // the species follows the beast's level tier
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
        // Loot scales with item prices (lvl^2.5 * 10) so hunting funds gear at the same tier instead of exploding exponentially
        ss = (long)(Math.pow(lvl, 2.5) * 10 * (1 + potential / 100.));
        age = rand.nextInt(maxAge * 3 / 4);
    }

    public void questInitializer () {
        Quest random     = new Quest("Random", false, 35, 2, 90);
        Quest profession = new Quest("Profession", false, 30, 3, 70);
        Quest hunt       = new Quest("Hunt", false, 30, 5, 50);
        Quest meditate   = new Quest("Meditate", false, 15, 2, 40); // low-priority rest: an extra cultivation tick


        spotQuests.add(random);
        spotQuests.add(profession);
        spotQuests.add(hunt);
        spotQuests.add(meditate);
    }

    // Monster NPCs: primarily hunt, occasionally explore via Random events
    public void monsterQuestInitializer() {
        spotQuests.add(new Quest("Hunt", false, 60, 5, 80));
        spotQuests.add(new Quest("Hunt Cultivator", false, 55, 5, 75));
        spotQuests.add(new Quest("Random", false, 25, 2, 50));
    }

    // Called from dead() every time a wild (untamed) Beast kills a cultivator
    public void awakenMonster() {
        trait = Traits.Monster;
        affiliation = "Evil";
        affiliationNum = -1;
        String[] titles = {"Ancient", "Dread", "Elder", "Primordial", "Titan", "Sovereign", "Infernal"};
        name = titles[rand.nextInt(titles.length)] + " " + name;
        id = GameDriver.nextUid();
        age = rand.nextInt(Math.max(1, maxAge * 3 / 4));
        time = 0;
        Map.later(() -> location.addChar(this)); // (the tile's list of people changes once the parallel turns are over)
        learnPlace(location);
        monsterQuestInitializer();
        // Queue through tempCharacters so ids stay equal to list indices even when births are already pending this tick
        // (the merge counts every temp entry as a birth, so pre-subtract this one from the population)
        addPopulation(-1);
        addTemp(this);
        addAwakened(this);
        // a 100,000-person world wakes about eleven a week: the log hears of those near a human, the Chronicle of the strong ones
        boolean near = false;
        for (Character h : Notifications.humans()) if (h != null && !h.dead && h.location != null && location != null && PlayerControl.distance(h.location, location) <= 3) near = true;
        if (near) {
            if (awakeWeek != GameDriver.time) { if (awakeHidden > 0) System.out.println("(" + awakeHidden + " more beasts awakened as Monsters nearby in the last weeks.)"); awakeWeek = GameDriver.time; awakeShown = 0; awakeHidden = 0; }
            if (++awakeShown <= 3) System.out.println(name + " " + lastName + " has awakened as a Monster! The wilds tremble."); else awakeHidden++;
        }
        if (lvl >= 25) Chronicle.add("world", name + " " + lastName + " (Lv." + lvl + ") has awakened as a Monster near " + (location == null ? "the wilds" : location.name) + ".");
    }

    private static int awakeWeek = -1, awakeShown, awakeHidden; // the awakening lines printed this week (a crowded start woke dozens a week)
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
    /** Thread-safe tile creation: double-checked lock prevents two threads creating the same tile.
     *  Sets currentCharacter inside the lock so Area constructor seeds level correctly. */
    private Area ensureTile(int x, int y, char dir) { return ensureTile(x, y, dir, location); }
    private Area ensureTile(int x, int y, char dir, Area from) {
        if (tiles[x][y] == null) {
            synchronized (tileLock) {
                if (tiles[x][y] == null) {
                    currentCharacter = this;
                    tiles[x][y] = new Area(from, dir);
                }
            }
        }
        return tiles[x][y];
    }

    /** The tile one step from `from` (the world wraps); a tile that does not exist yet is made. */
    private Area neighbour(Area from, char dir) {
        switch (dir) {
            case 'd': return from.x >= range - 1 ? ensureTile(0, from.y, 'e', from) : ensureTile(from.x + 1, from.y, 'e', from);
            case 'a': return from.x <= 0 ? ensureTile(range - 1, from.y, 'w', from) : ensureTile(from.x - 1, from.y, 'w', from);
            case 'w': return from.y >= range - 1 ? ensureTile(from.x, 0, 'n', from) : ensureTile(from.x, from.y + 1, 'n', from);
            case 's': return from.y <= 0 ? ensureTile(from.x, range - 1, 's', from) : ensureTile(from.x, from.y - 1, 's', from);
            default: return from;
        }
    }

    /** A planning turn: one step a turn (a diagonal is one step), taken later by applyMove(). The tiles on the way are made now, like a real step. */
    private void planMove(char temp) {
        if (pendTo != null || rideId >= 0) return;
        Area to;
        switch (temp) {
            case 'Q': to = neighbour(neighbour(location, 'a'), 'w'); break;
            case 'E': to = neighbour(neighbour(location, 'd'), 'w'); break;
            case 'Z': to = neighbour(neighbour(location, 'a'), 's'); break;
            case 'C': to = neighbour(neighbour(location, 'd'), 's'); break;
            case 'a': case 'd': case 'w': case 's': to = neighbour(location, temp); break;
            default: return;
        }
        pendFrom = location; pendTo = to; movedTick = GameDriver.time;
    }

    /** A party member who stands with the leader (and plans no step of his own) will move wherever the leader's step takes him. */
    void ride(Character leader) {
        if (planning && pendTo == null && leader != null) { rideId = leader.id; pendFrom = location; }
    }

    /** Takes the planned step (the end of the NPC phase, or at once for a character that plays on its own). */
    void applyMove() {
        Area to = pendTo, from = pendFrom;
        pendTo = null;
        if (rideId < 0) pendFrom = null; // (a rider still needs the tile he stood on: applyRide)
        if (to != null && !dead && location == from) moveTo(to);
    }

    /** A party member who stood with the leader goes where the leader now stands (after every other step has been taken). */
    void applyRide() {
        int lid = rideId; Area from = pendFrom;
        rideId = -1; pendFrom = null;
        if (lid < 0 || dead) return;
        Character l = charById(lid);
        if (l != null && !l.dead && l.location != null && location == from && l.location != location) moveTo(l.location);
    }

    /** The two halves of a step, for taking the steps of a crowd on several cores (see GameDriver.applyStepsInParallel). */
    void leaveTile() { location.removeChar(this); }
    void arriveTile() {
        Area to = pendTo; pendTo = null; if (rideId < 0) pendFrom = null;
        location = to;
        if (trait != Traits.Beast && areaIdx < 0) location.addChar(this);
        if (trait != Traits.Beast) learnPlace(location);
    }

    private void moveTo(Area to) {
        location.removeChar(this);
        location = to;
        if (trait != Traits.Beast && areaIdx < 0) location.addChar(this);
        if (trait != Traits.Beast) learnPlace(location);
    }

    public void movement(char temp) { // a = west, d = east, w = north, s = south; Q E Z C = north-west, north-east, south-west, south-east (one turn, like the pathfinder's two-axis step)
        if (dead) return; // a corpse does not walk (nor put itself back into a tile's list)
        if (planning) { planMove(temp); return; }
        if (temp != 'x') movedTick = GameDriver.time;
        switch (temp) {
            case 'Q': movement('a'); movement('w'); return;
            case 'E': movement('d'); movement('w'); return;
            case 'Z': movement('a'); movement('s'); return;
            case 'C': movement('d'); movement('s'); return;
            default: break;
        }
        location.removeChar(this); // Removes This Character From The Character List of the "Old" Location
        switch (temp) /* Creates New Area and/or Moves This Character*/ {
            case 'd':
                location = (location.x >= range - 1)
                    ? ensureTile(0, location.y, 'e')
                    : ensureTile(location.x + 1, location.y, 'e');
                break;
            case 'a':
                location = (location.x <= 0)
                    ? ensureTile(range - 1, location.y, 'w')
                    : ensureTile(location.x - 1, location.y, 'w');
                break;
            case 'w':
                location = (location.y >= range - 1)
                    ? ensureTile(location.x, 0, 'n')
                    : ensureTile(location.x, location.y + 1, 'n');
                break;
            case 's':
                location = (location.y <= 0)
                    ? ensureTile(location.x, range - 1, 's')
                    : ensureTile(location.x, location.y - 1, 's');
                break;
            default:
                break;
        }
        if (trait != Traits.Beast && areaIdx < 0) {
            location.addChar(this); // Adds This Character to the Character List of the New Location
        }
        if (trait != Traits.Beast) learnPlace(location); // Adds New Location to knownLocations
    }

    public void practice() {
        cultivate();
        if (rand.nextInt(10) == 0) {
            if (martialTechnique != null) martialTechnique.upgrade(this);
            if (martialTechnique2 != null) martialTechnique2.upgrade(this);
            if (movementTechnique2 != null) movementTechnique2.upgrade(this);
        }
    }

    public void cultivate() { cultivate(1.0); }

    /** Meditation: one and a half cultivation ticks, and every one of the four equipped techniques gains a level (up to its potential). Returns a one-line account. */
    public String meditate() { return meditate(true); }

    /** `say`: print the account for a human who plays. */
    public String meditate(boolean say) {
        final boolean tell = player > 0; // only a person who plays reads the account: 15% of all NPC turns are meditation and none of them need the text
        double before = tell && cultivationBaseMax > 0 ? cultivationBase / cultivationBaseMax : 0;
        int lvl0 = lvl;
        cultivate(1.5);
        StringBuilder sb = tell ? new StringBuilder() : null;
        Technique[] four = {martialTechnique, martialTechnique2, movementTechnique, movementTechnique2};
        for (int i = 0; i < 4; i++) {
            Technique t = four[i];
            if (t == null) continue;
            boolean seen = false;
            for (int j = 0; j < i; j++) if (four[j] == t) seen = true;
            if (seen) continue;
            if (t.lvl < t.potential) { t.lvl++; if (tell) sb.append(sb.length() > 0 ? ", " : "").append(t.name).append(" ").append(t.lvl).append("/").append(t.potential); }
        }
        if (!tell) return "";
        String line = "You meditate: " + (lvl > lvl0 ? "you reach Lv." + lvl + "!" : "cultivation " + Math.round(before * 100) + "% -> " + Math.round(100 * cultivationBase / cultivationBaseMax) + "% of Lv." + lvl + ".")
                + (sb.length() > 0 ? " Techniques sharpen: " + sb + "." : " Your techniques are at their potential.");
        if (say && player > 0 && skipping <= 0) System.out.println(line);
        return line;
    }

    public void cultivate(double mult) {
        double spiritVeinBonus = (trait == Traits.SpiritVein) ? 1.5 : 1.0;
        if (trait == Traits.Lucky && rand.nextInt(10) == 0) spiritVeinBonus *= 2; // fortune: about +10% cultivation on average
        if (fx != null) spiritVeinBonus *= Life.cultMult(this); // food, wine, music, poison
        cultivationBase += cultivation * spiritVeinBonus * mult;
        if (cultivationBase >= cultivationBaseMax) {
            cultivationBase -= cultivationBaseMax;
            lvl++;
            minorLvl++;
            cultivationBaseMax = Math.pow(lvl, 3) * (((70.+1./3.) + ((11./6.) * lvl)) / 25.);
            cultivation = ((Math.pow(potential,2) * Math.sqrt(lvl) * 10.) / (52. * Math.sqrt(potential) * Math.pow(2,inhibitor)));
            if ((minorLvl >= 11 && majorLvl < 10)) {
                int preMajor = majorLvl;
                final int maxAgeBefore = maxAge, totalAgeBefore = totalAgeGained; // restored if the tribulation below fails
                majorLvl++;
                minorLvl = 1;

                switch (majorLvl) {
                    case 1:
                        if (potential < gatePotential(1)) {
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
                        if (potential < gatePotential(2)) {
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
                        if (potential < gatePotential(3)) {
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
                        if (potential < gatePotential(4)) {
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
                        if (potential < gatePotential(5)) {
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
                        if (potential < gatePotential(6)) {
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
                        if (potential < gatePotential(7)) {
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
                        if (potential < gatePotential(8)) {
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
                        if (potential < gatePotential(9)) {
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
                        if (potential < gatePotential(10) || rand.nextInt(5) != 0) { // 100 potential and a 1 in 5 chance
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
                if (majorLvl == preMajor + 1 && potential < 100 && rand.nextInt(100) == 0) givePotential(1, "Breakthrough insight!");
                // Breakthrough Tribulation at key realm transitions (3=Core, 5=Nascent, 7=Spirit, 9=Immortal)
                if (majorLvl == preMajor + 1 && (majorLvl == 3 || majorLvl == 5 || majorLvl == 7 || majorLvl == 9)) {
                    tempLvlCheck();
                    int tribChance = Math.max(10, Math.min(90, potential - (majorLvl - 3) * 5));
                    if (rand.nextInt(100) < tribChance) {
                        // Pass
                        if (rand.nextInt(10) == 0 && potential < 100) givePotential(1);
                        cultivationBase += cultivationBaseMax * 0.5;
                        if (printBreakthroughs || (player > 0 && skipping <= 0))
                            System.out.println(this + " survived the realm tribulation! (majorLvl " + majorLvl + ")");
                    } else {
                        // Fail — pushed back
                        majorLvl = preMajor;
                        minorLvl = 8;
                        lvl = preMajor * 10 + minorLvl; // lvl == 10*majorLvl + minorLvl everywhere else
                        maxAge = maxAgeBefore;          // the lifespan bonus of the failed realm is not kept (nor re-earned on each retry)
                        totalAgeGained = totalAgeBefore;
                        inhibitor++;
                        cultivationBaseMax = Math.pow(lvl, 3) * (((70.+1./3.) + ((11./6.) * lvl)) / 25.);
                        cultivation = ((Math.pow(potential,2) * Math.sqrt(lvl) * 10.) / (52. * Math.sqrt(potential) * Math.pow(2,inhibitor)));
                        if (printBreakthroughs || (player > 0 && skipping <= 0))
                            System.out.println(this + " failed the realm tribulation! Cultivation regressed.");
                    }
                }
                // Hero threshold: first cultivator of any kind to reach each apex realm
                if (majorLvl == preMajor + 1) {
                    Chronicle.breakthrough(this);
                    if (majorLvl == 5 && !heroEnsoulment) { synchronized(Map.class) { if (!heroEnsoulment) { heroEnsoulment = true; System.out.println("[HERO] " + this + " is the first to reach Ensoulment! The heavens take notice."); } } }
                    if (majorLvl == 7 && !heroAscension) { synchronized(Map.class) { if (!heroAscension) { heroAscension = true; System.out.println("[HERO] " + this + " is the first to reach Ascension! An era-defining cultivator has emerged."); } } }
                    if (majorLvl == 9 && !heroPseudoImmortal) { synchronized(Map.class) { if (!heroPseudoImmortal) { heroPseudoImmortal = true; System.out.println("[HERO] " + this + " is the first to reach Pseudo Immortal! Immortality approaches."); } } }
                }
            }
        }
    }

    /** Lucky cultivators stumble into fortune about once a decade: a flash of insight (+1 potential), a piece of gear one Major above them,
     *  a windfall of spiritual energy, or a technique breakthrough. */
    private void fortuneEvent() {
        int roll = rand.nextInt(100);
        String what;
        if (roll < 25 && potential < 100) {
            what = givePotential(1) > 0 ? "a sudden insight: potential is now " + potential : "a sudden insight: your potential stirs (" + Math.round(potentialProgress * 100) + "% of the next point)";
        } else if (roll < 55) {
            int gear = Math.max(1, Math.min(10, majorLvl + 1));
            Item found = rand.nextBoolean() ? new Item(this, Item.WeaponType.values()[rand.nextInt(Item.WeaponType.values().length)], gear) : new Item(this, gear, Item.ItemType.Clothing);
            backpack.add(found);
            equip();
            what = "a hidden cache: " + found.name + " (Lv." + gear + ")";
        } else if (roll < 85) {
            int weeks = 10 + rand.nextInt(21);
            for (int i = 0; i < weeks; i++) cultivate();
            what = "a windfall of spiritual energy (" + weeks + " weeks of cultivation in a moment)";
        } else {
            if (martialTechnique != null) martialTechnique.upgrade(this);
            what = "a martial insight that improves their technique";
        }
        if (player == 1 && skipping <= 0) {
            System.out.println("Fortune smiles on " + name + ": " + what + ".");
            Notifications.post("fortune", "Fortune smiles on you: " + what + ".", -1, id);
        }
    }

    public void time() {
        if (fx != null) Life.tick(this);
        if (trait == Traits.Lucky && born && !dead && rand.nextInt(520) == 0) fortuneEvent();
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
            Notifications.onBirth(this, mother, father);
        }
        if (age > maxAge) {
            deathCause = "old age (lived " + age + " years)";
            dead();
        }
    }

    /** Nobody under this age dies (a beast, a Monster, a cultivator, a trap, the epoch): the children of the world are safe until they come of age. */
    static final int ADULT_AGE = 18;

    boolean childSafe() { return age < ADULT_AGE && trait != Traits.Beast && trait != Traits.Monster && !reincarnating; }

    public void dead() {
        if (dead) return;
        if (wanderingImmortal) { return; }
        if (childSafe()) return; // the Wandering Immortals do not die
        if (fatedTo > 0) return; // a person of a story: only the human it belongs to ends them (dead(killer)); age, plague, traps and the epoch do not
        // (Map.later: what reaches beyond this tile - a party, a master, friends, a spouse, a sect, the tile's list of people - waits until the turns of a parallel phase are over; one thread runs it at once)
        Map.later(() -> { leaveParty(); dissolveParty(); });
        if (trait == Traits.Beast || trait == Traits.Monster) {
            dead = true;
            noteDeath();
            if (masterId >= 0 || waitingSoul >= 0) { Map.later(() -> { location.removeChar(this); Taming.onBeastDeath(this); }); return; } // a tamed (or waiting) beast: no corpse, no list entry left behind
            Map.later(() -> { location.removeChar(this); if (sect != null) sect.resign(this); }); // (a Monster may have joined a sect)
            location.addDead(this);
            addRem(this); // null out characters slot; GameDriver skips population-- for monsters
            return;
        }
        dead = true;
        noteDeath();
        if (player >= 1 && !reincarnating) { needsReincarnation = true; Map.needReincarnation(this); } // a Ghost only reaches this on a true death (the first death makes the ghost form and returns earlier)
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
        Map.later(() -> {
            Naming.releaseName(name, lastName);
            Taming.onMasterGone(this);
            if (!reincarnating) { Chronicle.death(this, null); Notifications.onDeath(this, null); }
            if (majorLvl >= 7) {
                String legend = name + " " + lastName;
                if (!legacyTitles.contains(legend)) { addLegacy(legend, lvl); }
            }
            if (spouse != null) {
                spouse.spouse = null;
                spouse = null;
            }
            if (pregnant) {
                for (Character child : new ArrayList<>(children)) killUnborn(child);
            }
            if (sect != null) {
                sect.resign(this);
            }
            // Recycling: remove this character from living friends' relationship lists
            for (Relationship rel : new ArrayList<>(relationshipList.relationships)) {
                if (rel.friendID < 0 || rel.friendID >= characters.size()) continue;
                Character friend = characters.get(rel.friendID);
                if (friend != null && !friend.dead) friend.relationshipList.remove(this.id);
            }
            // Notify children so they can find parent's body
            if (!reincarnating) for (Character child : children) {
                if (child != null && !child.dead && child.born) {
                    child.targetQuests.add(new Quest("Find Parent's Body", true, 25, 0, 45, location));
                }
            }
            location.removeChar(this);
        });
        addRem(this);
        location.addDead(this);

        if (purgedByHeaven()) { // the epoch: only the great names are announced one by one (thousands fall in a purge); the rest are counted in the [EPOCH] lines
            if (player > 0 || (printBreakthroughs && (majorLvl >= 3 || (sect != null && sect.master == this) || Game.Map.trackedCharacters.contains(id)))) System.out.println("Heaven has consumed " + this + " (Major Lvl " + majorLvl + ").");
        } else if (printBreakthroughs || player > 0) System.out.println(this + " has died");

    }

    transient String diedAt; // -Dtdf.verify=true only: where the death came from (Verify prints it)
    private void noteDeath() {
        if (!Verify.ON) return;
        StackTraceElement[] st = new Throwable().getStackTrace();
        StringBuilder sb = new StringBuilder();
        for (int k = 1; k < Math.min(st.length, 9); k++) sb.append(st[k].getMethodName()).append(':').append(st[k].getLineNumber()).append(' ');
        diedAt = sb.toString();
    }

    /** Whether this death is the epoch's (see GameDriver's purge). */
    boolean purgedByHeaven() { return GameDriver.PURGE_CAUSE.equals(deathCause); }

    // A mother's death takes her unborn child with her: mark it dead, clear its sect/list entries and queue its removal
    private void killUnborn(Character child) {
        if (child == null || child.born || child.dead) return;
        child.dead = true; child.noteDeath();
        if (child.sect != null) child.sect.resign(child);
        if (child.location != null) child.location.removeChar(child);
        addRem(child);
    }

    public void dead(Character killer) {
        if (dead) return; // already dead this tick; prevent duplicate remCharacters entry
        if (wanderingImmortal) { return; }
        if (childSafe()) return; // (a lost fight leaves a child hurt, see winCheck)
        if (fatedTo > 0 && (killer == null || killer.id != fatedTo - 1)) return; // (a person of a story: see winCheck)
        if (killer == null) { deathCause = "eliminated by an Array or Trap"; dead(); return; } // array/trap death: no killer to credit
        // True Immortal rebirth — cycles through death rather than truly dying
        if (majorLvl == 10 && trait != Traits.Beast && trait != Traits.Monster) {
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
        noteDeath();
        if (trait != Traits.Beast && trait != Traits.Monster) Map.later(() -> { Naming.releaseName(name, lastName); Taming.onMasterGone(this); Chronicle.death(this, killer); Notifications.onDeath(this, killer); });
        if (player >= 1) {
            needsReincarnation = true; Map.needReincarnation(this);
            reincarnationKiller = killer;
        }

        if (trait != Traits.Beast && trait != Traits.Monster) {
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
            Map.later(() -> {
                if (spouse != null) {
                    spouse.spouse = null;
                    spouse = null;
                }
                if (pregnant) {
                    for (Character child : new ArrayList<>(children)) killUnborn(child);
                }
                if (sect != null) {
                    sect.resign(this);
                }
                // Notify children so they can find parent's body
                for (Character child : children) {
                    if (child != null && !child.dead && child.born) {
                        child.targetQuests.add(new Quest("Find Parent's Body", true, 25, 0, 45, location));
                    }
                }
                location.removeChar(this); //Removes a cultivator from the location
            });
            addRem(this); //Removes a cultivator from the characters Array after the round is done
        }

        if (killer.trait == Traits.Beast || killer.trait == Traits.Monster) {
            if (killer.trait == Traits.Beast && !killer.ambusher) { // ambush beasts don't stay on as roaming predators
                addMonsterOnce(killer);
            }
            if (trait == Traits.Beast || trait == Traits.Monster) { // a beast or Monster killed by another: no corpse left in the tile's list, and out of the world's list after the round
                Map.later(() -> { location.removeChar(this); if (sect != null) sect.resign(this); });
                if (monstersList.contains(this)) addRem(this);
            }
            location.addDead(this);
            killer.kills++;
            for (int i = 0; i < lvl; i++) {
                killer.cultivate();
            }
            if (killer.trait == Traits.Beast && killer.masterId < 0 && trait != Traits.Beast && trait != Traits.Monster) killer.awakenMonster(); // every kill by a wild beast (not a tamed one) wakes a Monster
            return;
        }

        if (trait == Traits.Beast || trait == Traits.Monster) {
            killer.kills++;
            killer.ss += killer.trait == Traits.Lucky ? this.ss + this.ss / 4 : this.ss; // the Lucky find 25% more on every kill
            this.ss = 0;
            Map.later(() -> { location.removeChar(this); if (sect != null) sect.resign(this); }); // awakened Monsters sit in location.characters; without this a slain one lingered there as a corpse (and in a sect, if it had joined one)
            if (monstersList.contains(this)) {
                addRem(this); //Removes monster from monstersList after round is done
            }
            return;
        }

        if (trait != Traits.Beast && trait != Traits.Monster && killer.trait != Traits.Beast && killer.trait != Traits.Monster) {
            killer.kills++;
            Map.later(() -> { // (friends, their lists and cities' boards are other people's and other tiles' business: after a parallel phase)
                for (Relationship relationship : relationshipList.relationships) {
                    if (relationship.friendID < 0 || relationship.friendID >= characters.size()) continue;
                    Character friend = characters.get(relationship.friendID);
                    Integer value = relationship.value;
                    if (friend != null && !friend.dead && friend != killer && value >= 2) {
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
                    if (rel.friendID < 0 || rel.friendID >= characters.size()) continue;
                    Character friend = characters.get(rel.friendID);
                    if (friend != null && !friend.dead) friend.relationshipList.remove(this.id);
                }
                LoreEndings.onKill(killer, this); // the Usurper's hunger: each Good cultivator killed lifts the potential past 100
                // Legacy: record high-level cultivators who fall in battle
                if (majorLvl >= 7) {
                    String legend = name + " " + lastName;
                    if (!legacyTitles.contains(legend)) { addLegacy(legend, lvl); }
                }
            });
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
                if (rand.nextInt(20) == 0 && killer.potential < 100) killer.givePotential(1);
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
            FightReport.loot(this, killer);
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
    // ---------------------------------------------------------------- player control (see planning.md)

    /** Player-control state; created the first time a human-controlled character needs it. Its quest queues are swapped for
     *  muting-aware ones so a deleted quest stays deleted when its trigger fires again. */
    PlayerControl pc() {
        if (pc == null && player == 1) {
            pc = new PlayerControl();
            targetQuests = new QuestQueue(targetQuests, pc.muted);
            spotQuests = new QuestQueue(spotQuests, pc.muted);
        }
        return pc;
    }

    // A human character the player drives by hand (MANUAL, or HYBRID between script steps). Children stay on the NPC routine.
    /** Learns a place. An NPC keeps at most about KNOWN_CAP places: past KNOWN_CAP + KNOWN_SLACK the least interesting ones (plain wild tiles, the farthest first) are forgotten. */
    void learnPlace(Area a) {
        if (a == null || knownLocations == null || a == lastLearned) return;
        if (knownLocations.contains(a)) { lastLearned = a; return; }
        lastLearned = a;
        knownLocations.add(a);
        if (player == 0 && knownLocations.size() > KNOWN_CAP + KNOWN_SLACK) pruneKnownLocations();
    }

    private transient Area lastLearned;
    /** Forgets the far places of an NPC past the cap (once a year, see Slim.trim). */
    void trimKnown() { if (player == 0 && knownLocations != null && knownLocations.size() > KNOWN_CAP + KNOWN_SLACK) pruneKnownLocations(); }
    private void pruneKnownLocations() {
        final Area here = location;
        java.util.HashSet<Area> keep = new java.util.HashSet<>();
        if (here != null) keep.add(here);
        if (sect != null && sect.location != null) keep.add(sect.location);
        if (currentQuest != null && currentQuest.targetArea != null) keep.add(currentQuest.targetArea);
        for (Quest q : targetQuests) if (q != null && q.targetArea != null) keep.add(q.targetArea);
        final int n = knownLocations.size();
        final long[] score = new long[n];
        for (int i = 0; i < n; i++) {
            Area a = knownLocations.get(i);
            if (a == null) { score[i] = Long.MIN_VALUE; continue; }
            long kind = a.hasHeaven() ? 6 : (a.city || a.sect != null) ? 5 : (a.dungeonType != null || a.loreSite != null) ? 4 : a.monsters > 0 ? 2 : 0;
            long dist = 0;
            if (here != null) { int dx = Math.abs(a.x - here.x), dy = Math.abs(a.y - here.y); dist = Math.max(Math.min(dx, range - dx), Math.min(dy, range - dy)); }
            score[i] = keep.contains(a) ? Long.MAX_VALUE : kind * 1_000_000L - dist;
        }
        // the n - KNOWN_CAP lowest scores go; among equal scores the earliest learned go first (what the stable sort of the indices did)
        final int toDrop = n - KNOWN_CAP;
        long[] sorted = score.clone();
        java.util.Arrays.sort(sorted);
        final long limit = sorted[toDrop - 1];
        boolean[] drop = new boolean[n];
        int dropped = 0;
        for (int i = 0; i < n; i++) if (score[i] < limit) { drop[i] = true; dropped++; }
        for (int i = 0; i < n && dropped < toDrop; i++) if (!drop[i] && score[i] == limit) { drop[i] = true; dropped++; }
        ArrayList<Area> kept = new ArrayList<>(KNOWN_CAP + 8);
        for (int i = 0; i < n; i++) if (!drop[i]) kept.add(knownLocations.get(i)); // the order of the rest is unchanged
        knownLocations = kept;
    }

    /** Whether the Craft button runs a trade for this trait (every other trait crafts as an amateur, see AmateurCraft). */
    boolean hasTrade() { return Profession.hasTrade(trait); }

    boolean isManualPc() {
        return player == 1 && age >= 8 && pc().mode != PlayerControl.Mode.AUTO;
    }

    // Does the NPC-style routine for this duty run? Always for NPCs and AUTO characters; for MANUAL/HYBRID only when the duty toggle is on.
    boolean autoActs(int duty) {
        if (!isManualPc()) return true;
        return duty >= 0 && pc.duties[duty];
    }

    // A human takes over another character's seat (reincarnation, ghost rebirth): settings carry over, scripts and alerts do not
    void takeControlFrom(Character old) {
        player = old.player;
        old.player = 0;
        if (player == 1) {
            PlayerControl np = pc();
            if (old.pc != null) np.inheritFrom(old.pc);
        }
    }

    // Highest-priority queued quest this character may run by itself right now (NPCs and AUTO characters: the head of the queue)
    private Quest pollAllowed(PriorityQueue<Quest> queue) {
        if (!isManualPc()) return QuestLife.pick(this, queue); // the highest priority, bent by what this character feels like (see QuestLife)
        Quest best = null;
        for (Quest q : queue) if (pc.allowedToRun(this, q) && (best == null || q.priority > best.priority)) best = q;
        if (best != null) queue.remove(best);
        return best;
    }

    // Tile the character must reach to run this quest (null for spot quests)
    Area questDestination(Quest q) {
        if (q.category == Quest.QuestCategory.Character) {
            Character t = charById(q.targetCharacter);
            return (t != null && !t.dead) ? t.location : null;
        }
        return q.category == Quest.QuestCategory.Area ? q.targetArea : null;
    }

    Quest findQueuedQuest(String name) {
        name = Quest.canon(name);
        if (currentQuest != null && currentQuest.name.equalsIgnoreCase(name)) return currentQuest;
        Quest best = null;
        for (Quest q : targetQuests) if (q.name.equalsIgnoreCase(name) && (best == null || q.priority > best.priority)) best = q;
        for (Quest q : spotQuests) if (q.name.equalsIgnoreCase(name) && (best == null || q.priority > best.priority)) best = q;
        return best;
    }

    /** Do Quest: runs a queued quest right now if it can be done where the character stands, then applies the same result
     *  handling as quest(). Returns null when the quest ran, otherwise the reason it can't be done. */
    String doQuestNow(Quest q) {
        if (q == null) return "No such quest.";
        if (dead) return "You are dead.";
        boolean queued = q == currentQuest || targetQuests.contains(q) || spotQuests.contains(q);
        if (!queued) return "That quest is no longer in your queue.";
        if (q.category == Quest.QuestCategory.Spot) {
            spotQuests.remove(q);
            int result = spotQuests(q);
            if (dead) return null;
            if (result < 1 && q.oneTime) { // the handler refused (can't afford, wrong place, nothing to do): keep the quest so the player can retry
                spotQuests.add(q);
                return q.displayName() + " couldn't be done right now: " + whyNot(q) + ".";
            }
            finishSpot(q);
            return null;
        }
        Character target = null;
        if (q.category == Quest.QuestCategory.Character) {
            target = charById(q.targetCharacter);
            if (target == null || target.dead) return q.displayName() + ": the target is gone.";
            if (target.location != location)
                return q.displayName() + ": " + target.name + " is at " + PlayerControl.coord(target.location) + " — you are at " + PlayerControl.coord(location) + ". 'goquest #" + q.uid + "' walks there.";
            q.targetArea = target.location;
        } else if (q.targetArea != location) {
            return q.displayName() + ": go to " + (q.targetArea == null ? "its target" : q.targetArea.name + " " + PlayerControl.coord(q.targetArea))
                    + " first — you are at " + PlayerControl.coord(location) + ". 'goquest #" + q.uid + "' walks there.";
        }
        if (questViable(q) != 1)
            return q.displayName() + " can't be done right now (target gone, not affordable, or its requirements are no longer met).";
        boolean wasCurrent = q == currentQuest;
        if (wasCurrent) currentQuest = null; else targetQuests.remove(q);
        int result = targetQuests(q);
        if (dead) return null; // the quest killed the character (or reincarnated them)
        if (result == 0) { targetQuests.add(q); return null; } // in progress, nothing more to do this turn
        finishTarget(q, result, wasCurrent);
        return null;
    }

    // Post-execution bookkeeping for a target quest (shared by quest() and Do Quest): one-time quests are spent, repeating ones
    // go back to the queue at their base priority and the character is released so the queue decides the next trip
    private void finishTarget(Quest q, int result, boolean isCurrent) {
        if (q == null) return;
        if (result == 1 && q.oneTime) {
            final String doneName = q.name;
            final int doneTarget = q.targetCharacter;
            if ("Explore Dungeon".equals(doneName)) nextExploreWeek = GameDriver.time + 16;            // a rest between dungeon trips
            else if ("Hunt Beast".equals(doneName)) nextHuntWeek = GameDriver.time + 8; // and between hunts (bounties too: with 25 cities posting them everyone hunted all the time)
            q.questDeconstructor();
            if (isCurrent) currentQuest = null;
            bump(targetQuests);
            // A finished weak-relationship quest is replaced by one aimed at a different cultivator
            if ("Find Person".equals(doneName) || "Battle Dislike".equals(doneName)) refillWeakSocialQuests(doneTarget);
        }
        else if (result == -1 && "Find Person".equals(q.name)) {
            // The relationship this quest was built on no longer exists: drop it and look for someone new
            final int failedTarget = q.targetCharacter;
            q.questDeconstructor();
            if (isCurrent) currentQuest = null;
            bump(targetQuests);
            refillWeakSocialQuests(failedTarget);
        }
        else if (result == 1 || result == -1) {
            q.resetPriority();
            targetQuests.add(q);
            if (isCurrent) currentQuest = null; // release: the queue's priorities decide the next trip (previously the first repeating quest locked the character in forever)
            bump(targetQuests);
        }
    }

    // A spot quest that has run: one-off ones are spent whether they succeeded or failed (their trigger re-posts them when conditions
    // hold again); repeating ones return at base priority
    private void finishSpot(Quest q) {
        if (q == null) return;
        if (q.oneTime) {
            q.questDeconstructor();
        } else {
            q.resetPriority();
            spotQuests.add(q);
        }
    }

    // ---------------------------------------------------------------- quest-name bits (see quest())
    static final String[] GUARDED_QUESTS = {"Sect Mission", "Study Trip", "Accept Sect Mission", "Buy City Array", "Buy Gear", "Buy Sect Array", "Buy from Sect", "Demand Tribute", "Disperse", "Donate to Sect", "Entice Merchant", "Explore Dungeon", "Get Pregnant", "Gift Technique", "Go Sell", "Heaven Treasure", "Hold Tournament", "Hunt Beast", "Perform", "Join Sect", "Loot Corpse", "Possess Womb", "Propose Alliance", "Recruit Defector", "Recruit Member", "Refine Blood", "Register Tournament", "Restock Trip", "Return to Sect", "Sect Raid", "Seek Potential", "Seek Territory", "Sell Technique", "Spectate Tournament"};
    private static final java.util.HashMap<String, Long> QBIT = new java.util.HashMap<>();
    static { for (int i = 0; i < GUARDED_QUESTS.length; i++) QBIT.put(GUARDED_QUESTS[i], 1L << i); } // read-only after class load
    private long qMaskT, qMaskS;
    private static long Q(String name) { Long b = QBIT.get(name); return b == null ? 0L : b; }
    private static long Q(Quest q) { if (q.bitName != q.name) { q.bitVal = Q(q.name); q.bitName = q.name; } return q.bitVal; }
    // the guard bits of the quest names used in quest(), looked up once
    private static final long QB_GO_SELL = Q("Go Sell");
    private static final long QB_HUNT_BEAST = Q("Hunt Beast");
    private static final long QB_BUY_GEAR = Q("Buy Gear");
    private static final long QB_SEEK_POTENTIAL = Q("Seek Potential");
    private static final long QB_REFINE_BLOOD = Q("Refine Blood");
    private static final long QB_STUDY_TRIP = Q("Study Trip");
    private static final long QB_PERFORM = Q("Perform");
    private static final long QB_LOOT_CORPSE = Q("Loot Corpse");
    private static final long QB_SEEK_TERRITORY = Q("Seek Territory");
    private static final long QB_HEAVEN_TREASURE = Q("Heaven Treasure");
    private static final long QB_JOIN_SECT = Q("Join Sect");
    private static final long QB_RETURN_TO_SECT = Q("Return to Sect");
    private static final long QB_RECRUIT_MEMBER = Q("Recruit Member");
    private static final long QB_EXPLORE_DUNGEON = Q("Explore Dungeon");
    private static final long QB_DISPERSE = Q("Disperse");
    private static final long QB_SECT_RAID = Q("Sect Raid");
    private static final long QB_PROPOSE_ALLIANCE = Q("Propose Alliance");
    private static final long QB_DEMAND_TRIBUTE = Q("Demand Tribute");
    private static final long QB_RECRUIT_DEFECTOR = Q("Recruit Defector");
    private static final long QB_GIFT_TECHNIQUE = Q("Gift Technique");
    private static final long QB_RESTOCK_TRIP = Q("Restock Trip");
    private static final long QB_SELL_TECHNIQUE = Q("Sell Technique");
    private static final long QB_HOLD_TOURNAMENT = Q("Hold Tournament");
    private static final long QB_BUY_CITY_ARRAY = Q("Buy City Array");
    private static final long QB_BUY_SECT_ARRAY = Q("Buy Sect Array");
    private static final long QB_ENTICE_MERCHANT = Q("Entice Merchant");
    private static final long QB_DONATE_TO_SECT = Q("Donate to Sect");
    private static final long QB_BUY_FROM_SECT = Q("Buy from Sect");
    private static final long QB_POSSESS_WOMB = Q("Possess Womb");
    private static final long QB_ACCEPT_SECT_MISSION = Q("Accept Sect Mission");
    private static final long QB_REGISTER_TOURNAMENT = Q("Register Tournament");
    private static final long QB_SPECTATE_TOURNAMENT = Q("Spectate Tournament");
    private static final long QB_GET_PREGNANT = Q("Get Pregnant");
    private static long nameMask(java.util.Collection<Quest> qs) { long m = 0; for (Quest q : qs) if (q != null) m |= Q(q); return m; }
    private void addT(Quest q) { targetQuests.add(q); qMaskT |= Q(q); }
    private void addS(Quest q) { spotQuests.add(q); qMaskS |= Q(q); }
    void addTarget(Quest q) { addT(q); } // for QuestLife

    /** How many turns apart an NPC looks at the dynamic quest triggers (1 = every turn); -Dtdf.triggerEvery=1 switches the saving off. */
    static volatile int TRIGGER_EVERY = Math.max(1, Integer.getInteger("tdf.triggerEvery", 2)); // (volatile and not final so the probes that test one trigger can set it to 1)
    int trigK = 1; // the dice of the triggers are scaled by this (see quest())
    /** One in `n` per week, however often the triggers are looked at. */
    private boolean dice(int n) { return rand.nextInt(Math.max(1, n / trigK)) == 0; }

    public void quest () {
        final boolean manual = isManualPc();
        if (player == 1) syncMissionQuest(); // the accepted sect mission shows in the Quest Log
        QuestLife.tick(this); // the time on the current quest's family tires or feeds the interest in it
        qMaskT = nameMask(targetQuests); qMaskS = nameMask(spotQuests); // one pass over the queues: the guards below read these bits instead of streaming the queues 40 times
        if (manual) {
            // Whatever the player switched away from (an NPC-style trip in progress) goes back to the queue
            if (currentQuest != null && !pc.allowedToRun(this, currentQuest)) { addT(currentQuest); currentQuest = null; }
            pruneGoneQuests();
        }
        if (autoActs(PlayerControl.UPKEEP)) consume(); // Consume pills IMPORTANT!!!

        // Leaders attempt founding every turn (the Profession spot quest is picked too rarely): sect at Major Lvl 2+, city at Major Lvl 2+
        if (trait == Traits.Leader && age >= 8) {
            if (majorLvl >= 2 && professionLvl != 50 && autoActs(PlayerControl.CITY)) cityLeadership();
            if (majorLvl >= 2 && sect == null && autoActs(PlayerControl.SECT)) sectLeadership();
        }

        // Crafters work their trade regularly instead of relying on the rarely-picked Profession spot quest
        if (autoActs(PlayerControl.CRAFT) && age >= 8 && (trait == Traits.Blacksmith || trait == Traits.Alchemist
                || trait == Traits.Scribe || trait == Traits.Tailor || trait == Traits.Innovative || trait == Traits.Merchant)) {
            // Gather raw materials directly whenever short (the queued Forage/Mine spot quests are starved by target quests)
            boolean lowHerb = materialBag.stream().noneMatch(i -> i.itemType == Item.ItemType.Herb && i.quantity >= CRAFT_COST);
            boolean lowMetal = materialBag.stream().noneMatch(i -> i.itemType == Item.ItemType.Metal && i.quantity >= CRAFT_COST);
            if ((trait == Traits.Alchemist || trait == Traits.Tailor || trait == Traits.Scribe) && lowHerb && !(location.city && QuestLife.buyMaterials(this, Item.ItemType.Herb))) forage();
            if ((trait == Traits.Blacksmith || trait == Traits.Tailor) && lowMetal && !(location.city && QuestLife.buyMaterials(this, Item.ItemType.Metal))) mine();
            if (rand.nextInt(3) == 0) professionPicker();
        }

        // Gatherers and Miners live by their trade: they gather every turn (what they do not need goes to the cities on Trade Runs, where the craftsmen buy it)
        if ((trait == Traits.Gatherer || trait == Traits.Miner) && age >= 8 && autoActs(PlayerControl.CRAFT) && QuestLife.surplus(this) < 20000) { if (trait == Traits.Gatherer) forage(); else mine(); }

        // Wanderlust: cultivators periodically strike out in one direction for several tiles to discover new lands
        if (!manual && age >= 8 && exploreTicks <= 0 && partyLeaderId < 0 && trait != Traits.Beast && trait != Traits.Monster
                && (currentQuest == null || currentQuest.progress == 0) && rand.nextInt(60) == 0) {
            exploreDir = "wasd".charAt(rand.nextInt(4));
            exploreTicks = 6 + rand.nextInt(20);
        }

        // In a city with surplus goods: sell them right away (city pool / merchant shops)
        boolean crafterStock = (trait == Traits.Blacksmith || trait == Traits.Tailor) && age >= 8 && sellableCargo().size() >= 2; // a smith's finished pieces are for sale, not for the pack
        if (autoActs(PlayerControl.UPKEEP) && age >= 8 && location.city && (backpack.size() >= 5 || crafterStock) && trait != Traits.Beast && trait != Traits.Monster
                && rand.nextInt(crafterStock ? 2 : 5) == 0) {
            sell(location);
        }

        // Dynamic quest triggers — checked each turn by a person who plays, every TRIGGER_EVERY-th turn by everybody else (their dice are scaled so the chance per week is the same:
        // the triggers were most of the cost of a tick). Which turn is each character's own: (week + id) % TRIGGER_EVERY.
        trigK = (player == 0 && TRIGGER_EVERY > 1 && trait != Traits.Beast) ? TRIGGER_EVERY : 1;
        if (trigK == 1 || (GameDriver.time + id) % TRIGGER_EVERY == 0) {
        boolean luxury = false;
        for (Item it : backpack) if (it.heavenSent && it != equippedWeapon && it != equippedClothing) { luxury = true; break; }
        if (!luxury && potential >= 100) for (Item it : pillBag) if (it.pillType == Item.PillType.Potential) { luxury = true; break; }
        if ((backpack.size() > 8 || crafterStock || QuestLife.pillStock(this) >= 4 || (goods != null && Life.goodsCount(this) >= 20) || luxury) && (qMaskT & QB_GO_SELL) == 0
                && (currentQuest == null || !"Go Sell".equals(currentQuest.name))) {
            Area targetCity = null;
            int bestDist = Integer.MAX_VALUE;
            for (Area area : knownLocations) {
                if (area == null || !area.city || (luxury && !Auctions.hasHouse(area))) continue;
                int d = PlayerControl.distance(location, area) + (shopRoom(area) ? 0 : 8); // the nearest city, a shop with room on its shelves counts for eight tiles
                if (d < bestDist) { bestDist = d; targetCity = area; }
            }
            if (targetCity != null)
                addT(new Quest("Go Sell", true, 50, 0, 70, targetCity));
        }

        // Amateur gear: anyone without a weapon or clothes who has (or can gather) the materials makes a plain set (see AmateurCraft)
        if (autoActs(PlayerControl.CRAFT) && age >= 8 && trait != Traits.Beast && trait != Traits.Monster && trait != Traits.Ghost
                && (equippedWeapon == null || equippedClothing == null) && dice(8)) AmateurCraft.gearUp(this);

        // Buy Gear: in city with shops — buy immediately; otherwise queue travel to nearest known shop city
        // Monsters are predators and never buy gear
        boolean gearNeeded = trait != Traits.Monster && (equippedWeapon == null || equippedClothing == null
                || (equippedWeapon != null && equippedWeapon.lvl + 10 <= lvl)
                || (equippedClothing != null && equippedClothing.lvl + 10 <= lvl));
        if (gearNeeded) {
            if (canShopHere()) {
                if (autoActs(PlayerControl.UPKEEP)) buyGear();
                // If slot(s) still empty after visiting shop, can't afford — hunt beasts to earn ss
                boolean stillMissing = equippedWeapon == null || equippedClothing == null;
                if (stillMissing && (qMaskT & QB_HUNT_BEAST) == 0
                        && (currentQuest == null || !"Hunt Beast".equals(currentQuest.name))) {
                    for (Area area : knownLocations) {
                        if (area != null && area.monsters > 0
                                && area.highestLvl >= lvl - 10 && area.highestLvl <= lvl + 15) {
                            addT(new Quest("Hunt Beast", true, 40, 3, 65, area));
                            break;
                        }
                    }
                }
            } else if (dice(4) && (qMaskT & QB_BUY_GEAR) == 0
                    && (currentQuest == null || !"Buy Gear".equals(currentQuest.name))) {
                // Only travel to a city that actually has something I could buy — otherwise everyone gravitates to the shop city and stops exploring; the nearest one
                Area gearCity = null;
                int gearDist = Integer.MAX_VALUE;
                for (Area area : knownLocations) {
                    if (area != null && area.city && hasBuyableGear(area)) {
                        int d = PlayerControl.distance(location, area);
                        if (d < gearDist) { gearDist = d; gearCity = area; }
                    }
                }
                if (gearCity != null) addT(new Quest("Buy Gear", true, 45, 3, 62, gearCity)); // above a fresh hunt or exploration (30-35)
            }
        }

        // Seek Potential: travel to city with potential pills when potential is too low for next breakthrough
        if (trait != Traits.Beast && trait != Traits.Monster && majorLvl < 10) {
            int potThreshold = gatePotential(majorLvl + 1); // the potential the next breakthrough needs
            if (potential < potThreshold && (dice(6) || player == 1) // a look every few turns is plenty (the scan over every known place was the biggest single cost of a tick)
                    && (qMaskT & QB_SEEK_POTENTIAL) == 0
                    && (currentQuest == null || !"Seek Potential".equals(currentQuest.name))) {
                for (Area area : knownLocations) {
                    if (potentialPillsAt(area)) {
                        addT(new Quest("Seek Potential", true, 35, 2, 65, area));
                        break;
                    }
                }
            }
        }

        // Refine Blood: a cultivator who is not an alchemist takes the Blood of the fallen to one, who turns it into Potential Pills for a fee
        if (trait != Traits.Alchemist && trait != Traits.Beast && trait != Traits.Monster && age >= 8 && potential < 100 && dice(20) && bloodAmount() >= 1000
                && (qMaskT & QB_REFINE_BLOOD) == 0 && (currentQuest == null || !"Refine Blood".equals(currentQuest.name))) {
            Character alchemist = findAlchemist();
            if (alchemist != null) addT(new Quest("Refine Blood", true, 45, 3, 70, alchemist));
        }

        // Buy Cultivation Pills: pick up next-realm pills when in a city (10% chance; the Buy Pills quest brings the character here on purpose)
        if (trait != Traits.Monster && location.city && location.pillBag != null && autoActs(PlayerControl.UPKEEP) && dice(10)) buyNextRealmPill();

        // Buy a technique from the city library when a better one is for sale (about once a year in a city)
        if (trait != Traits.Monster && location.city && !location.libraries.isEmpty() && autoActs(PlayerControl.UPKEEP) && dice(52)) studyHere();

        // Study Trip: go to a library that sells a better technique than yours, if you can pay for it
        if (trait != Traits.Monster && trait != Traits.Beast && age >= 8 && QuestLife.roll(this, "Study Trip", 20) && (qMaskT & QB_STUDY_TRIP) == 0
                && (currentQuest == null || !"Study Trip".equals(currentQuest.name)) && autoActs(PlayerControl.UPKEEP)) {
            Area lib = studyTarget();
            if (lib == location) studyHere();
            else if (lib != null) addT(new Quest("Study Trip", true, 35, 2, 60, lib));
        }

        // Perform: a Musician now and then gives a concert in the best city nearby (big cities pay more)
        if (trait == Traits.Musician && age >= 12 && dice(20) && GameDriver.time >= nextPerformWeek && (qMaskT & QB_PERFORM) == 0
                && (currentQuest == null || !"Perform".equals(currentQuest.name)) && autoActs(PlayerControl.CRAFT)) {
            Area stage = Life.stage(this);
            if (stage == location) Life.concert(this);
            else if (stage != null) addT(new Quest("Perform", true, 40, 3, 62, stage));
        }

        // Bully: Evil characters proactively target significantly weaker characters in same location
        if (trait != Traits.Beast && "Evil".equals(affiliation) && QuestLife.roll(this, "Battle Dislike", 10)
                && !location.characters.isEmpty() && weakSocialCount("Battle Dislike") < WEAK_SOCIAL_CAP) {
            ArrayList<Character> tileB = location.characters;
            int nB = tileB.size(), lookB = Math.min(nB, 48), startB = nB > lookB ? rand.nextInt(nB) : 0;
            for (int kB = 0; kB < lookB; kB++) {
                int idxB = (startB + kB) % nB;
                if (idxB >= tileB.size()) break;
                Character c = tileB.get(idxB);
                if (c == null || c.dead || c == this || c.trait == Traits.Beast) continue;
                if (lvl - c.lvl >= 15
                        && !hasSocialQuestFor(c.id)) {
                    addT(relationshipQuest(-1, c));
                    break;
                }
            }
        }

        // Lucky: one-time high-priority loot when a fresh corpse within 30 levels is in this tile
        if (trait == Traits.Lucky && !location.deadCharacters.isEmpty()) {
            for (Character deceased : location.deadCharacters) {
                if (Math.abs(lvl - deceased.lvl) <= 30
                        && (qMaskS & QB_LOOT_CORPSE) == 0) {
                    addS(new Quest("Loot Corpse", true, 75, 0, 75));
                    break;
                }
            }
        }

        // Leaders without a throne keep gathering followers
        if (trait == Traits.Leader && partyLeaderId < 0 && age >= 14 && !isRuler() && dice(6)) tryRallyParty(true);

        // Hunt Beast: 20% chance per turn to seek combat in a known area with level-appropriate monsters
        if (QuestLife.roll(this, "Hunt Beast", 5) && GameDriver.time >= nextHuntWeek
                && (qMaskT & QB_HUNT_BEAST) == 0
                && (currentQuest == null || !"Hunt Beast".equals(currentQuest.name))) {
            // Targets beasts of my own major realm down to 2 realms below, so the economy keeps flowing at every tier.
            // One hunt per quest (oneTime) so spot quests (crafting, gathering) still get turns; the area is picked at random
            // among eligible known areas so hunters spread out instead of all returning to the first area they ever saw.
            Area pick = null;
            int pickScore = Integer.MAX_VALUE;
            for (Area area : knownLocations) {
                if (area == null || area.monsters <= 0) continue;
                int areaMajor = area.lvl / 10;
                if (areaMajor < majorLvl - 2 || areaMajor > majorLvl) continue;
                int score = PlayerControl.distance(location, area) + rand.nextInt(6); // near, with a little chance so hunters do not all pick the same ground
                if (score < pickScore) { pickScore = score; pick = area; }
            }
            if (pick != null) {
                addT(new Quest("Hunt Beast", true, 30, 2, 50, pick));
                // Same-realm beasts are dangerous: try to hunt in a party
                if (pick.lvl / 10 == majorLvl && partyLeaderId < 0) tryRallyParty(true);
            }
        }

        // Seek Territory: Monster migrates toward highest-level known area (20% chance per turn)
        if (trait == Traits.Monster && dice(5)
                && (qMaskT & QB_SEEK_TERRITORY) == 0
                && (currentQuest == null || !"Seek Territory".equals(currentQuest.name))) {
            Area bestArea = null;
            for (Area a : knownLocations) {
                if (a != null && a != location && a.lvl > location.lvl + 10) {
                    if (bestArea == null || a.lvl > bestArea.lvl) bestArea = a;
                }
            }
            if (bestArea != null) {
                addT(new Quest("Seek Territory", true, 45, 3, 70, bestArea));
            }
        }

        // Dual Cultivation: bonus cultivation with spouse or a best friend in the same tile (5%/turn)
        if (trait != Traits.Beast && trait != Traits.Monster && dice(location.spiritualVein ? 5 : 20)) {
            if (location.spiritualVein && (spouse != null || !relationshipList.relationships.isEmpty())) cultivate(); // on a spirit vein the pair gains more
            if (spouse != null && !spouse.dead && spouse.location == location) {
                for (int i = 0; i < 3; i++) cultivate();
                spouse.cultivationBase += spouse.cultivation * 3;
            } else {
                for (Relationship rel : relationshipList.copy()) {
                    if (rel.value >= 3) {
                        Character friend = charById(rel.friendID); // a newborn child (value-3 tie) may not be in the list yet
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
        if (!legacyTitles.isEmpty() && trait != Traits.Beast && trait != Traits.Monster && dice(50)) {
            int legIdx = rand.nextInt(legacyTitles.size());
            int legLvl = legacyLvls.get(legIdx);
            for (int i = 0; i < Math.min(legLvl / 10, 10); i++) cultivate();
            if (player > 0 && skipping <= 0)
                System.out.println(this + " meditates on the legend of " + legacyTitles.get(legIdx) + "!");
        }

        // Heaven Treasure: rush to claim a known heavenly technique (high priority)
        if (Map.heavenExists && trait != Traits.Beast && trait != Traits.Monster && QuestLife.pass(this, "Heaven Treasure") // Map.heavenExists is refreshed once a tick: without it everybody scanned every place they ever saw, every turn
                && (qMaskT & QB_HEAVEN_TREASURE) == 0
                && (currentQuest == null || !"Heaven Treasure".equals(currentQuest.name))) {
            for (Area a : knownLocations) {
                if (a != null && a.hasHeaven()) {
                    addT(new Quest("Heaven Treasure", true, 70, 5, 85, a));
                    break;
                }
            }
        }

        // Rob Caravan: Evil characters may target wealthy merchants passing through (5%/turn)
        if ("Evil".equals(affiliation) && trait != Traits.Beast && trait != Traits.Monster && QuestLife.roll(this, "Rob Caravan", 20)) {
            ArrayList<Character> tile = location.characters;
            for (int ti = 0; ti < tile.size(); ti++) {
                Character c = tile.get(ti);
                if (c == null || c.trait != Character.Traits.Merchant || c.dead || c == this) continue;
                if ((c.backpack.size() > 5 || c.ss > 100000L)
                        && targetQuests.stream().noneMatch(q -> "Rob Caravan".equals(q.name) && q.targetCharacter == c.id)) {
                    addT(new Quest("Rob Caravan", true, 30, 2, 55, c));
                    break;
                }
            }
        }

        // Join Sect: non-Leader adults with no sect seek a known sect (5% chance per turn, low priority)
        if (sect == null && trait != Traits.Leader && age >= 8 && QuestLife.roll(this, "Join Sect", 8)
                && (qMaskT & QB_JOIN_SECT) == 0
                && (currentQuest == null || !"Join Sect".equals(currentQuest.name))) {
            for (Area area : knownLocations) {
                if (area != null && area.sect != null) {
                    addT(new Quest("Join Sect", true, 20, 1, 40, area));
                    break;
                }
            }
        }

        // Return to Sect: sect members who drift away periodically head back (5% chance/turn)
        if (sect != null && !sect.isMaster(this) && location != sect.location && dice(6)
                && (qMaskT & QB_RETURN_TO_SECT) == 0
                && (currentQuest == null || !"Return to Sect".equals(currentQuest.name))) {
            addT(new Quest("Return to Sect", true, 25, 1, 40, sect.location)); // one trip per drift, not a permanent quest
        }

        // Recruit Member: sect masters actively seek eligible candidates in known locations
        if (sect != null && sect.isMaster(this) && trait == Traits.Leader && dice(15)
                && (qMaskT & QB_RECRUIT_MEMBER) == 0
                && (currentQuest == null || !"Recruit Member".equals(currentQuest.name))) {
            for (Character c : location.characters) {
                if (c != null && !c.dead && c != this && sect.canJoin(c)) {
                    addT(new Quest("Recruit Member", true, 25, 2, 45, c));
                    break;
                }
            }
        }

        // Bounty Hunt: pick up posted bounties at cities within 10 levels of the target
        long bountiesHeld = 2; // (counted below, only where a board can be read: this used to run a stream over the queue for every cultivator every turn)
        if (location.city && !location.bounties.isEmpty()) {
            bountiesHeld = currentQuest != null && "Hunt Beast".equals(currentQuest.name) && currentQuest.bounty ? 1 : 0;
            for (Quest hq : targetQuests) if (hq.bounty && "Hunt Beast".equals(hq.name)) bountiesHeld++;
        }
        if (location.city && !location.bounties.isEmpty() && bountiesHeld < 2 && GameDriver.time >= nextHuntWeek && QuestLife.pass(this, "Hunt Beast")) { // at most two beast bounties at a time: the board used to empty into one person's queue
            for (Quest bounty : new ArrayList<>(location.bounties)) {
                if (bountiesHeld >= 2) break;
                if ("Hunt Beast".equals(bounty.name)) {
                    Area ta = bounty.targetArea;
                    if (ta != null && ta.monsters > 0) {
                        int areaMajorLvl = Math.max(1, ta.lvl / 10);
                        boolean withinOneRealm = areaMajorLvl >= majorLvl - 2 && areaMajorLvl <= majorLvl + 1;
                        boolean notAlreadyQueued = true;
                        for (Quest hq : targetQuests) if (hq.targetArea == ta && "Hunt Beast".equals(hq.name)) { notAlreadyQueued = false; break; }
                        if (withinOneRealm && notAlreadyQueued) {
                            Quest bountyQuest = new Quest("Hunt Beast", true, 50, 5, 70, ta);
                            bountyQuest.bounty = true;
                            addT(bountyQuest);
                            bountiesHeld++;
                            location.bounties.remove(bounty);
                            // Same major realm — try to form a party before departing
                            if (areaMajorLvl == majorLvl) tryRallyParty(true);
                        }
                    }
                } else {
                    Character target = charById(bounty.targetCharacter);
                    if (target != null && !target.dead && Math.abs(lvl - target.lvl) <= 10
                            && targetQuests.stream().noneMatch(q -> "Bounty Hunt".equals(q.name)
                                    && q.targetCharacter == bounty.targetCharacter)) {
                        boolean contract = trait == Traits.Poisoner && Life.firstOf(this, Item.ItemType.Poison) != null;
                        addT(new Quest(contract ? "Poison Contract" : "Bounty Hunt", true, 40, 3, 70, target));
                        location.bounties.remove(bounty);
                    }
                }
            }
        }

        // Explore Dungeon: 16% chance per turn to seek a known dungeon area at appropriate level
        if (trait != Traits.Beast && age >= 8 && QuestLife.roll(this, "Explore Dungeon", 6) && GameDriver.time >= nextExploreWeek
                && (qMaskT & QB_EXPLORE_DUNGEON) == 0
                && (currentQuest == null || !"Explore Dungeon".equals(currentQuest.name))) {
            Area nearest = null;
            int nearestDist = Integer.MAX_VALUE;
            for (Area area : knownLocations) {
                if (area != null && area.dungeonType != null
                        && area.lvl >= lvl - 15 && area.lvl <= lvl + 20) {
                    int d = PlayerControl.distance(location, area);
                    if (d < nearestDist) { nearestDist = d; nearest = area; }
                }
            }
            if (nearest != null) addT(new Quest("Explore Dungeon", true, 35, 2, 60, nearest));
        }

        // Disperse: while beasts ravage an overcrowded hotspot, cultivators there head for a quieter tile nearby (10%/turn)
        if (location.crowdHotspot && trait != Traits.Beast && trait != Traits.Monster && dice(10)
                && (qMaskT & QB_DISPERSE) == 0
                && (currentQuest == null || !"Disperse".equals(currentQuest.name))) {
            Area quiet = findQuietTile();
            if (quiet != null) addT(new Quest("Disperse", true, 45, 3, 70, quiet));
        }

        // War skirmish: when two sects are at war their disciples fight whenever they meet, not only the raiders sent against the enemy's home
        if (sect != null && sect.warTarget != null && age >= 14 && trait != Traits.Beast && trait != Traits.Monster && location != null && dice(3) && !location.characters.isEmpty()) {
            Character foe = null;
            int n = location.characters.size();
            for (int i = 0; i < 12 && foe == null; i++) {
                Character c = location.characters.get(rand.nextInt(n));
                if (c != null && !c.dead && c != this && c.sect != null && c.sect != sect && (c.sect == sect.warTarget || c.sect.warTarget == sect) && c.age >= 14) foe = c;
            }
            if (foe != null) {
                Relationship wr = relationshipList.contains(foe.id);
                if (wr == null || wr.value > -2) changeRelationship(foe, -2); // enemies of a war are rivals: their fights can be fatal
                partyBattleCheck(foe);
                if (dead) return;
            }
        }

        // Sect Raid: masters and elders attack enemy sect location when at war; also opportunistic raids on crisis sects
        if (sect != null && (sect.isMaster(this) || sect.getRank(this) == Organization.SectRank.ELDER)
                && GameDriver.time >= sect.nextRaidWeek
                && (qMaskT & QB_SECT_RAID) == 0
                && (currentQuest == null || !"Sect Raid".equals(currentQuest.name))) {
            Area enemyLoc = null;
            if (sect.isAtWar() && sect.warTarget != null) {
                enemyLoc = sect.warTarget.location;
            } else {
                // Opportunistic: raid a known sect in succession crisis (1% chance/turn)
                if (dice(100)) {
                    for (Area area : knownLocations) {
                        if (area != null && area.sect != null && area.sect != sect && area.sect.inCrisis()) {
                            enemyLoc = area;
                            break;
                        }
                    }
                }
            }
            if (enemyLoc != null) {
                addT(new Quest("Sect Raid", true, 65, 5, 85, enemyLoc));
                sect.nextRaidWeek = GameDriver.time + Organization.RAID_COOLDOWN_WEEKS; // one raid per sect per season
            }
        }

        // Propose Alliance: sect masters seek alliances with other non-enemy sects (5% chance/turn)
        if (sect != null && sect.isMaster(this) && !sect.isAtWar() && dice(20)
                && (qMaskT & QB_PROPOSE_ALLIANCE) == 0) {
            for (Character c : new ArrayList<>(location.characters)) {
                if (c == null || c.dead || c.sect == null || c.sect == sect || sect.isAlly(c.sect)) continue;
                if (c.sect.isMaster(c)) {
                    addT(new Quest("Propose Alliance", true, 30, 5, 50, c));
                    break;
                }
            }
        }

        // Demand Tribute: powerful sect masters extract tribute from weaker rival sects (2% chance/turn)
        if (sect != null && sect.isMaster(this) && !sect.isAtWar() && dice(50)
                && (qMaskT & QB_DEMAND_TRIBUTE) == 0) {
            for (Character c : new ArrayList<>(location.characters)) {
                if (c == null || c.dead || c.sect == null || c.sect == sect || sect.isAlly(c.sect)) continue;
                if (c.sect.isMaster(c) && lvl - c.lvl >= 20) {
                    addT(new Quest("Demand Tribute", true, 40, 5, 60, c));
                    break;
                }
            }
        }

        // Recruit Defector: sect masters at war poach enemy Elders (1% chance/turn)
        if (sect != null && sect.isMaster(this) && sect.isAtWar() && dice(100)
                && (qMaskT & QB_RECRUIT_DEFECTOR) == 0) {
            for (Character c : new ArrayList<>(location.characters)) {
                if (c == null || c.dead || c.sect == null || c.sect == sect) continue;
                if (sect.warTarget != null && c.sect == sect.warTarget
                        && sect.warTarget.getRank(c) == Organization.SectRank.ELDER) {
                    addT(new Quest("Recruit Defector", true, 45, 5, 65, c));
                    break;
                }
            }
        }

        // Promote Disciples: sect masters check members for rank advancement (5% chance/turn)
        if (sect != null && sect.isMaster(this) && autoActs(PlayerControl.SECT) && dice(20)) {
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
        if (!techniqueList.isEmpty() && !children.isEmpty() && dice(20)
                && (qMaskT & QB_GIFT_TECHNIQUE) == 0) {
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
                        addT(new Quest("Gift Technique", true, 30, 2, 50, child));
                        break;
                    }
                }
            }
        }

        // Restock Trip: Merchant with shops seeks best city for arbitrage restocking (10% chance/turn)
        if (trait == Traits.Merchant && !ownedShops.isEmpty() && dice(10)
                && (qMaskT & QB_RESTOCK_TRIP) == 0
                && (currentQuest == null || !"Restock Trip".equals(currentQuest.name))) {
            Area bestCity = findBestRestockCity();
            if (bestCity != null) {
                addT(new Quest("Restock Trip", true, 30, 2, 60, bestCity));
            }
        }

        // Sell Technique: Innovative chars sell techniques to city libraries (10% chance/turn)
        if (trait == Traits.Innovative && dice(10) && !sellableTechniques().isEmpty()
                && (qMaskS & QB_SELL_TECHNIQUE) == 0
                && (qMaskT & QB_SELL_TECHNIQUE) == 0
                && (currentQuest == null || !"Sell Technique".equals(currentQuest.name))) {
            if (location.city && !location.libraries.isEmpty()) {
                addS(new Quest("Sell Technique", true, 40, 0, 60));
            } else {
                for (Area area : knownLocations) {
                    if (area != null && area.city && !area.libraries.isEmpty()) {
                        addT(new Quest("Sell Technique", true, 40, 0, 60, area));
                        break;
                    }
                }
            }
        }

        // Hold Tournament: city owners and sect masters hold periodic tournaments (2% chance/turn)
        if (dice(50) && (qMaskS & QB_HOLD_TOURNAMENT) == 0) {
            boolean isCityOwner = location.city && location.cityOwner == this;
            boolean isSectMasterAtHome = sect != null && sect.isMaster(this) && location == sect.location;
            if (isCityOwner || isSectMasterAtHome) {
                addS(new Quest("Hold Tournament", true, 35, 0, 55));
            }
        }

        // Buy City Array: city owners periodically invest in stronger defensive arrays (3% chance/turn)
        if (location.city && location.cityOwner == this && dice(33)
                && (qMaskS & QB_BUY_CITY_ARRAY) == 0) {
            int arrayLvl = Math.min(100, (majorLvl + 1) * 10);
            Item testArray = new Item(this, arrayLvl, Item.ItemType.Array);
            long cost = itemPrice(testArray);
            if (canAfford(cost) || location.cityTreasury >= cost) {
                addS(new Quest("Buy City Array", true, 40, 0, 60));
            }
        }

        // Buy Sect Array: sect masters invest in sect defensive arrays (3% chance/turn)
        if (sect != null && sect.isMaster(this) && location == sect.location && dice(33)
                && (qMaskS & QB_BUY_SECT_ARRAY) == 0) {
            int arrayLvl = Math.min(100, (majorLvl + 1) * 10);
            Item testArray = new Item(this, arrayLvl, Item.ItemType.Array);
            long cost = itemPrice(testArray);
            if (canAfford(cost) || sect.sectCash >= cost) {
                addS(new Quest("Buy Sect Array", true, 40, 0, 60));
            }
        }

        // Entice Merchant: city owners recruit merchants without a local shop (5% chance/turn)
        if (location.city && location.cityOwner == this && dice(20)
                && (qMaskS & QB_ENTICE_MERCHANT) == 0) {
            boolean missingMerchant = characters.stream().anyMatch(c -> c != null && !c.dead
                    && c.trait == Traits.Merchant
                    && c.ownedShops.stream().noneMatch(s -> s.location == location));
            if (missingMerchant) addS(new Quest("Entice Merchant", true, 45, 0, 65));
        }

        // Donate to Sect: members at sect home donate no-longer-useful items (10% chance/turn)
        if (sect != null && location == sect.location && dice(10)
                && (qMaskS & QB_DONATE_TO_SECT) == 0) {
            boolean hasExtras = backpack.stream().anyMatch(i -> i != equippedWeapon && i != equippedClothing);
            boolean usesHerbs  = trait == Traits.Alchemist || trait == Traits.Tailor || trait == Traits.Scribe;
            boolean usesMetals = trait == Traits.Blacksmith;
            if (!hasExtras) for (Item m : materialBag) {
                if ((m.itemType == Item.ItemType.Herb  && (!usesHerbs  || m.quantity > 5000))
                 || (m.itemType == Item.ItemType.Metal && (!usesMetals || m.quantity > 5000))) { hasExtras = true; break; }
            }
            if (!hasExtras) for (Item p : pillBag) {
                boolean stale = p.lvl <= majorLvl
                    || (p.pillType == Item.PillType.Potential && potential >= 100)
                    || (p.pillType == Item.PillType.Cultivation && p.quantity > 5 && p.lvl > majorLvl);
                if (stale) { hasExtras = true; break; }
            }
            if (hasExtras) addS(new Quest("Donate to Sect", true, 30, 0, 50));
        }

        // Buy from Sect: members buy needed materials or gear from treasury using sectPoints (10% chance/turn)
        if (sect != null && location == sect.location && sectPoints >= 5 && !sect.treasury.isEmpty()
                && dice(10)
                && (qMaskS & QB_BUY_FROM_SECT) == 0) {
            boolean needsHerb  = (trait == Traits.Alchemist || trait == Traits.Tailor || trait == Traits.Scribe)
                    && materialBag.stream().noneMatch(m -> m.itemType == Item.ItemType.Herb && m.quantity >= CRAFT_COST);
            boolean hasHerb    = sect.treasury.stream().anyMatch(t -> t.itemType == Item.ItemType.Herb && t.quantity > 0);
            boolean needsMetal = trait == Traits.Blacksmith
                    && materialBag.stream().noneMatch(m -> m.itemType == Item.ItemType.Metal && m.quantity >= CRAFT_COST);
            boolean hasMetal   = sect.treasury.stream().anyMatch(t -> t.itemType == Item.ItemType.Metal && t.quantity > 0);
            boolean wantsGear  = sect.treasury.stream().anyMatch(t ->
                (t.itemType == Item.ItemType.Weapon   && (equippedWeapon   == null || t.lvl > equippedWeapon.lvl   + 5))
             || (t.itemType == Item.ItemType.Clothing && (equippedClothing == null || t.lvl > equippedClothing.lvl + 5)));
            if ((needsHerb && hasHerb) || (needsMetal && hasMetal) || wantsGear) {
                addS(new Quest("Buy from Sect", true, 45, 0, 65));
            }
        }

        // Ghost form: seek pregnant women for possession (10% chance/turn)
        if (isGhostForm && dice(10)
                && (qMaskT & QB_POSSESS_WOMB) == 0) {
            for (Area area : knownLocations) {
                if (area == null) continue;
                for (Character c : new ArrayList<>(area.characters)) {
                    if (c.pregnant && !c.dead && lvl > c.lvl) {
                        addT(new Quest("Possess Womb", false, 80, 3, 100, c));
                        break;
                    }
                }
                if (targetQuests.stream().anyMatch(q -> "Possess Womb".equals(q.name))) break;
            }
        }

        // Accept Sect Mission: sect members pick up a board mission (15% chance/turn when at sect)
        if (sect != null && location == sect.location && sectMission == null
                && !sect.questBoard.isEmpty() && dice(7)
                && (qMaskS & QB_ACCEPT_SECT_MISSION) == 0) {
            addS(new Quest("Accept Sect Mission", true, 50, 0, 70));
        }

        // A tournament quest is for the tile it was posted in: once the window has closed here (or this character is entered) it is dropped, not left as a stale alert
        if ((qMaskS & (QB_REGISTER_TOURNAMENT | QB_SPECTATE_TOURNAMENT)) != 0) {
            final boolean open = location.tournamentTimer > 0, entered = location.tournamentRegistrants.contains(this);
            final boolean eligible = majorLvl >= location.tournamentMinMajor && majorLvl <= location.tournamentMaxMajor && !(sect != null && Lore.isEntryTournament(location)); // (a tournament to join a sect is not for someone who has one)
            spotQuests.removeIf(q -> ("Spectate Tournament".equals(q.name) && !open) || ("Register Tournament".equals(q.name) && (!open || entered || !eligible)));
            if (currentQuest != null && ("Spectate Tournament".equals(currentQuest.name) && !open || "Register Tournament".equals(currentQuest.name) && (!open || entered || !eligible))) currentQuest = null;
            qMaskS = nameMask(spotQuests);
        }

        // Tournament Registration: eligible cultivators sign up during registration window (5% chance)
        if (location.tournamentTimer > 0 && majorLvl >= location.tournamentMinMajor
                && majorLvl <= location.tournamentMaxMajor
                && !(sect != null && Lore.isEntryTournament(location))
                && !location.tournamentRegistrants.contains(this)
                && (player >= 1 || dice(20)) // (a person who plays is offered the sign-up the moment a tournament is open for them: the window is only a few weeks)
                && (qMaskS & QB_REGISTER_TOURNAMENT) == 0) {
            addS(new Quest("Register Tournament", true, 60, 0, 80));
        }

        // Spectate Tournament: any character nearby can watch (10% chance during registration)
        if (location.tournamentTimer > 0 && !location.tournamentRegistrants.contains(this)
                && dice(10)
                && (qMaskS & QB_SPECTATE_TOURNAMENT) == 0) {
            addS(new Quest("Spectate Tournament", true, 20, 0, 40));
        }

        // Get Pregnant: married females with a spouse present (2% chance/turn)
        if (gender.equals("Female") && spouse != null && !pregnant && dice(50)
                && (qMaskT & QB_GET_PREGNANT) == 0) {
            addT(new Quest("Get Pregnant", true, 25, 0, 45, spouse));
        }

        QuestLife.consider(this); // visits, courtship, teaching, trade runs, retreats, pilgrimages, taming, pills, auctions, founding
        Life.upkeep(this); // eat, drink, the reincarnation call, a taming attempt
        Life.pursueDelivery(this); // a sect delivery mission for food, wine or poison

        } // end of the dynamic triggers
        trigK = 1;

        // Player characters in MANUAL/HYBRID: announce what they won't run by themselves (once per quest)
        if (manual && pc.scanAlerts(this)) {
            pc.alertPending = true;
            if (skipping > 0 && Notifications.enabled("alert")) skipping = 0; // a new alert wakes a character that is walking on autopilot (unless alerts are muted: `settings notify alert off`)
        }

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
                Quest next = pollAllowed(targetQuests); // MANUAL/HYBRID: only quests whose duty is on
                if (next != null) {
                    currentQuest = next;
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

        // While exploring, target-quest travel is paused (spot quests still run en route)
        if (exploreTicks > 0) { exploreTicks--; questCode = -3; }

        // If currentQuest exists and is viable, do pathfinding
        if (questCode != -3) {
            if (currentQuest.category == Quest.QuestCategory.Character) {
                Character target = charById(currentQuest.targetCharacter);
                if (target != null && !target.dead) {
                    currentQuest.targetArea = target.location;
                }
            }
            questCode = pathfinderFor(currentQuest);
            if (questCode == 0 && partyLeaderId < 0 && rand.nextInt(4) == 0) {
                tryRallyParty(); // rally allies while traveling
            }
            if (questCode == 1) {
                // Do Target Actions
                questCode = targetQuests(currentQuest);
                // Hunting parties stay together across repeated hunts (small chance to disband each round)
                if (questCode == 1 && (currentQuest == null || !"Hunt Beast".equals(currentQuest.name) || rand.nextInt(20) == 0)) dissolveParty();
            }
        }

        // The target quest's outcome is kept separate from the spot quest's: -3 none, 0 travelling, 1 done, -1 failed
        final int targetResult = questCode;

        // If the target quest wasn't completed this turn, do a spot quest
        Quest tempQuest = null;
        if (targetResult != 1) {
            tempQuest = pollAllowed(spotQuests); // MANUAL/HYBRID: only spot quests whose duty is on
            if (tempQuest != null) spotQuests(player == 1 ? chosenSpot(tempQuest) : tempQuest);
        }

        if (dead) return; // died during quest execution; dead() already cleaned up quests

        if ((targetResult == 1 || targetResult == -1) && currentQuest != null) finishTarget(currentQuest, targetResult, true);
        else if (targetResult == 1 || targetResult == -1) bump(targetQuests);

        finishSpot(tempQuest);

        // Age all spot quests
        bump(spotQuests);

        // Once a year each character prunes stale/excess quests and tops up weak social quests
        if ((GameDriver.time + id) % 52 == 0) maintainQuests();
    }

    /** A person who plays decides what the plain spot slot does (PlayerControl.spotAction): when the routine picked Random, Profession, Hunt or Meditate, the chosen one runs instead
     *  (the quest picked is still the one spent or put back). Hunt with no beasts in the tile meditates. Everything else the routine picks (a Forage, a Mine...) runs as it was. */
    private Quest chosenSpot(Quest picked) {
        PlayerControl p = pc;
        if (p == null || picked == null) return picked;
        String n = picked.name;
        if (!("Random".equals(n) || "Profession".equals(n) || "Hunt".equals(n) || "Meditate".equals(n))) return picked;
        String want = PlayerControl.SPOT_ACTIONS[Math.max(0, Math.min(PlayerControl.SPOT_ACTIONS.length - 1, p.spotAction))];
        if ("Hunt".equals(want) && location.monsters <= 0) want = "Meditate";
        if (want.equals(n)) return picked;
        return new Quest(want, false, 30, 3, 50);
    }

    /** Whether a queued quest's target no longer exists (a person dead, a dungeon explored out or closed, an emptied hunting ground, a city razed, a treasure taken). */
    private boolean targetGone(Quest q) {
        if (q.userCreated || q.progress > 0 || q.category == Quest.QuestCategory.Spot) return false;
        if (q.category == Quest.QuestCategory.Character) {
            if (q.targetCharacter < 0 || q.targetCharacter >= characters.size()) return false; // born this tick, not in the list yet
            Character t = characters.get(q.targetCharacter);
            return t == null || t.dead;
        }
        Area a = q.targetArea;
        if (a == null) return true;
        switch (q.name) {
            case "Explore Dungeon": return a.dungeonType == null;
            case "Hunt Beast": return a.monsters <= 0;
            case "Heaven Treasure": return !a.hasHeaven();
            case "Go Sell": case "Festival Trade": case "Perform": case "Sell Technique": case "Sell Stock": case "Restock Trip": case "Buy Gear": case "Ghost City Rebirth":
                return !a.city;
            case "Join Sect": return a.sect == null || sect != null; // (joined a sect meanwhile, the Palace's included: nothing left to join)
            case "Sect Raid": return a.sect == null;
            case "Defend": return !Defense.stillOn(a);
            default: return false;
        }
    }

    /** Called when this character has just joined a sect: the queued "Join Sect" errands (to any sect) are finished. */
    void joinedASect() {
        targetQuests.removeIf(q -> "Join Sect".equals(q.name));
        if (currentQuest != null && "Join Sect".equals(currentQuest.name)) currentQuest = null;
    }

    /** A short reason a quest cannot be done where the character stands (the quest's own target if it has one). */
    String whyNot(Quest q) {
        Area a = q.targetArea != null ? q.targetArea : location;
        String n = q.name == null ? "" : q.name;
        switch (n) {
            case "Register Tournament": return location.tournamentTimer <= 0 ? "no tournament is open for registration here" : location.tournamentRegistrants.contains(this) ? "you are already entered" : (sect != null && Lore.isEntryTournament(location)) ? "this tournament is a way into a sect and you already belong to one" : "your realm is outside the tournament's band (Major " + location.tournamentMinMajor + "-" + location.tournamentMaxMajor + ")";
            case "Spectate Tournament": return "no tournament is open here";
            case "Restock Trip": return a == null || !a.city ? "it must be done in a city" : "the city's stock is empty and you already keep a shop there";
            case "Go Sell": case "Sell Stock": case "Festival Trade": case "Perform": return a == null || !a.city ? "it must be done in a city" : "you have nothing to sell there";
            case "Buy Gear": return a == null || !a.city ? "it must be done in a city with shops" : "no shop there has gear better than yours that you can afford";
            case "Sell Technique": return "it needs a city with a library, and a technique to sell";
            case "Seek Potential": return "no Potential Pill is for sale there";
            case "Join Sect": return sect != null ? "you already belong to a sect" : "there is no sect there";
            case "Explore Dungeon": case "Heaven Treasure": return "there is nothing left there to explore or claim";
            case "Hunt Beast": return "no monsters are left there";
            case "Donate Array": case "Sect Raid": case "Deliver Materials": case "Sect Mission": return "it needs your sect (and, for a raid, a war)";
            case "Found City": case "Found Sect": return "it needs Major Lvl 2 and a free place";
            default: return "its conditions are not met here";
        }
    }

    private String whyGone(Quest q) {
        if (q.category == Quest.QuestCategory.Character) { Character t = charById(q.targetCharacter); return (t == null ? "that person" : t.name) + " is gone"; }
        switch (q.name) {
            case "Explore Dungeon": return "the place was explored out, hunted out or has closed";
            case "Hunt Beast": return "no monsters are left there";
            case "Heaven Treasure": return "the treasure has been taken";
            default: return "its target is gone";
        }
    }

    /** A human's alert quests are never polled (nothing runs them by itself), so a target that has gone away is looked for here, every turn, and the quest is dropped with a line saying why. */
    private void pruneGoneQuests() {
        if (targetQuests.isEmpty()) return;
        ArrayList<Quest> gone = null;
        for (Quest q : targetQuests) if (targetGone(q)) { if (gone == null) gone = new ArrayList<>(); gone.add(q); }
        if (gone == null) return;
        for (Quest q : gone) {
            targetQuests.remove(q);
            if (pc != null) pc.inbox.remove(q);
            q.questDeconstructor();
            System.out.println("Quest dropped: " + q.displayName() + " (" + whyGone(q) + ").");
        }
        qMaskT = nameMask(targetQuests);
    }

    static final int TARGET_QUEUE_CAP = 24;      // hard cap on queued target quests per character
    static final int WEAK_SOCIAL_CAP = 2;        // "Like" / "Dislike" quests kept at once (replaced when completed)
    static final int QUEST_TTL_WEEKS = 520;      // one-time quests older than 10 years expire

    // Ages every quest by its priorityDiff (never past maxPriority) and re-sorts the heap; editing priorities in place
    // inside a PriorityQueue silently breaks its ordering, which used to make polling return the wrong quest.
    static void bump(java.util.PriorityQueue<Quest> queue) {
        if (queue.isEmpty()) return;
        ArrayList<Quest> all = new ArrayList<>(queue);
        for (Quest q : all) {
            if (!q.pinned && q.priority < q.maxPriority) q.priority = Math.min(q.maxPriority, q.priority + q.priorityDiff); // pinned (player-set) priorities never age
        }
        queue.clear();
        queue.addAll(all);
    }

    // Bounds-safe lookup: null for dead slots and for ids not yet merged into the list (births are appended at end of tick)
    static Character charById(int id) {
        return (id >= 0 && id < characters.size()) ? characters.get(id) : null;
    }

    static boolean isSocialQuest(String name) {
        return "Find Person".equals(name) || "Battle Dislike".equals(name)
                || "Battle Rival".equals(name) || "Battle Arch-Enemy".equals(name);
    }

    // Relationship value -> quest. Weak ties (Like/Dislike) are one-time and capped; strong ties (Friend, Close Friend, Rival,
    // Arch-Enemy) persist. All sit in the lowest priority band but climb to their cap, so they are still served eventually.
    private Quest relationshipQuest(int rel, Character other) {
        switch (rel) {
            case 1:  return new Quest("Find Person", true, 10, 1, 34, other);
            case 2:  return new Quest("Find Person", false, 14, 2, 40, other);
            case 3:  return new Quest("Find Person", false, 18, 2, 46, other);
            case -1: return new Quest("Battle Dislike", true, 10, 1, 34, other);
            case -2: return new Quest("Battle Rival", false, 18, 2, 42, other);
            case -3: return new Quest("Battle Arch-Enemy", false, 24, 3, 48, other);
            default: return null;
        }
    }

    private int weakSocialCount(String name) {
        int n = 0;
        for (Quest q : targetQuests) if (q.oneTime && name.equals(q.name)) n++;
        if (currentQuest != null && currentQuest.oneTime && name.equals(currentQuest.name)) n++;
        return n;
    }

    private boolean hasSocialQuestFor(int otherId) {
        if (currentQuest != null && currentQuest.targetCharacter == otherId && isSocialQuest(currentQuest.name)) return true;
        for (Quest q : targetQuests) if (q.targetCharacter == otherId && isSocialQuest(q.name)) return true;
        return false;
    }

    // Keeps this character's social quest toward `other` in line with the current relationship value.
    void syncSocialQuest(Character other, int rel) {
        if (other == null || other == this) return;
        if (currentQuest != null && currentQuest.targetCharacter == other.id && isSocialQuest(currentQuest.name)) return; // in flight
        Quest existing = null;
        for (Quest q : targetQuests) {
            if (q.targetCharacter == other.id && isSocialQuest(q.name)) { existing = q; break; }
        }
        Quest wanted = relationshipQuest(rel, other);
        if (existing != null && wanted != null && existing.name.equals(wanted.name)
                && existing.oneTime == wanted.oneTime && existing.maxPriority == wanted.maxPriority) return; // same tier: keep its accrued priority
        if (existing != null) targetQuests.remove(existing);
        if (wanted == null) return;
        if (wanted.oneTime && weakSocialCount(wanted.name) >= WEAK_SOCIAL_CAP) return; // at cap: refill picks it up later if a slot opens
        targetQuests.add(wanted);
    }

    // Tops weak-tie quests back up to the cap using other cultivators with the same relationship value.
    void refillWeakSocialQuests(int excludeId) {
        for (int sign = 1; sign >= -1; sign -= 2) {
            String name = sign > 0 ? "Find Person" : "Battle Dislike";
            int need = WEAK_SOCIAL_CAP - weakSocialCount(name);
            if (need <= 0) continue;
            ArrayList<Relationship> cands = new ArrayList<>();
            for (Relationship r : relationshipList.copy()) if (r.value == sign) cands.add(r);
            java.util.Collections.shuffle(cands, rand);
            for (Relationship r : cands) {
                if (need <= 0) break;
                if (r.friendID == excludeId || r.friendID < 0 || r.friendID >= characters.size()) continue;
                Character other = characters.get(r.friendID);
                if (other == null || other.dead || other == this || hasSocialQuestFor(other.id)) continue;
                targetQuests.add(relationshipQuest(sign, other));
                need--;
            }
        }
    }

    void maintainQuests() {
        final int now = GameDriver.time;
        // Clamp runaway priorities (old saves reached millions) and drop stale one-time quests
        ArrayList<Quest> all = new ArrayList<>(targetQuests);
        for (Quest q : all) if (!q.pinned && q.priority > q.maxPriority) q.priority = q.maxPriority;
        targetQuests.clear();
        for (Quest q : all) {
            boolean stale = q.createdAt > 0 && now - q.createdAt > QUEST_TTL_WEEKS
                    && (q.oneTime || "Find Parent's Body".equals(q.name)) && !q.pinned && !q.userCreated;
            if (!stale) targetQuests.add(q);
        }
        // Hard cap: shed the lowest-priority quests first (a player's pinned quests are never shed)
        int excess = targetQuests.size() - TARGET_QUEUE_CAP;
        if (excess > 0) {
            ArrayList<Quest> sorted = new ArrayList<>(targetQuests);
            sorted.removeIf(q -> q.pinned);
            sorted.sort((a, b) -> Integer.compare(a.priority, b.priority));
            for (int i = 0; i < excess && i < sorted.size(); i++) targetQuests.remove(sorted.get(i));
        }
        Slim.trim(this); // weak ties to the dead and past the cap, copies of one errand, far places (an old world stays small)
        refillWeakSocialQuests(-1);
    }



    public int questViable(Quest quest) {
        if (QuestLife.owns(quest.name)) return QuestLife.viable(this, quest);
        switch (quest.category) {
            case Spot:
                System.out.println("Error in questViable Spot");
                return 0;
            case Character:
                // A target born this tick sits in tempCharacters and isn't in the list yet — treat as not viable for now
                Character character = (quest.targetCharacter >= 0 && quest.targetCharacter < characters.size())
                        ? characters.get(quest.targetCharacter) : null;
                if (character == null || character.dead) return 0;
                if ("Refine Blood".equals(quest.name)) {
                    return (character.trait == Traits.Alchemist && bloodAmount() >= 1000 && potential < 100) ? 1 : 0;
                }
                if ("Possess Womb".equals(quest.name)) {
                    return (character.pregnant && lvl > character.lvl) ? 1 : 0;
                }
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
                if ("Deliver Goods".equals(quest.name)) {
                    return (sect != null && sectMission != null && sectMission.startsWith("Deliver|")) ? 1 : 0;
                }
                if ("Go Sell".equals(quest.name) || "Festival Trade".equals(quest.name) || "Perform".equals(quest.name)) {
                    return (quest.targetArea != null && quest.targetArea.city) ? 1 : 0;
                }
                if ("Hunt Beast".equals(quest.name)) {
                    return (quest.targetArea != null && quest.targetArea.monsters > 0) ? 1 : 0;
                }
                if ("Find Lost Beast".equals(quest.name)) {
                    return Taming.lostBeastAt(this, quest.targetArea) != null ? 1 : 0;
                }
                if ("Join Sect".equals(quest.name)) {
                    return (quest.targetArea != null && quest.targetArea.sect != null && sect == null) ? 1 : 0;
                }
                if ("Restock Trip".equals(quest.name)) {
                    // A merchant with no shop there yet can still go to open one even if the city pool is empty (Entice Merchant relies on this)
                    return (quest.targetArea != null && quest.targetArea.city
                            && quest.targetArea.backpack != null
                            && (!quest.targetArea.backpack.isEmpty()
                                || ownedShops.stream().noneMatch(sh -> sh.location == quest.targetArea))) ? 1 : 0;
                }
                if ("Sell Stock".equals(quest.name)) {
                    // Cancel if we have nothing left to sell (same definition the handler uses: spares are kept) or the destination city is gone
                    return (quest.targetArea != null && quest.targetArea.city && !sellableCargo().isEmpty()) ? 1 : 0;
                }
                if ("Buy Gear".equals(quest.name)) {
                    return (quest.targetArea != null && quest.targetArea.city && hasBuyableGear(quest.targetArea)) ? 1 : 0;
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
                    return potentialPillsAt(quest.targetArea) ? 1 : 0;
                }
                if ("Study Trip".equals(quest.name)) {
                    return bestStudy(quest.targetArea) != null ? 1 : 0;
                }
                if ("Return to Sect".equals(quest.name)) {
                    return (sect != null && quest.targetArea != null && quest.targetArea == sect.location) ? 1 : 0;
                }
                if ("Deliver Materials".equals(quest.name)) {
                    return (sect != null && sectMission != null && quest.targetArea != null && quest.targetArea == sect.location) ? 1 : 0;
                }
                if ("Sect Mission".equals(quest.name)) {
                    return (sect != null && sectMission != null && (quest.targetArea == sect.location || sectMission.startsWith("Hunt|"))) ? 1 : 0;
                }
                if ("Ghost City Rebirth".equals(quest.name)) {
                    return (quest.targetArea != null && quest.targetArea.city) ? 1 : 0; // the city may have been destroyed since
                }
                if ("Defend".equals(quest.name)) {
                    return Defense.stillOn(quest.targetArea) ? 1 : 0;
                }
                if ("Disperse".equals(quest.name)) {
                    return (quest.targetArea != null && !quest.targetArea.crowdHotspot
                            && quest.targetArea.characters.size() < quest.targetArea.crowdCapacity()) ? 1 : 0;
                }
                if ("Donate Array".equals(quest.name)) {
                    return (sect != null && quest.targetArea != null && quest.targetArea == sect.location && !arrayBag.isEmpty()) ? 1 : 0;
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
                boolean huntFought = beastBattleCheck(); // a mission kill only counts if a fight actually happened
                if (!huntFought && player > 0 && skipping <= 0) System.out.println(location.monsters <= 0 ? "Hunt: no beasts here." : "Hunt: the beasts here are beyond you (area Lv " + location.lvl + "-" + location.highestLvl + ", you are Lv " + lvl + ") or you chose not to risk it.");
                if (huntFought && sect != null) sectPoints += 2; // (a mission kill is counted by beastFought(), wherever the beast was fought)
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
                if (sectMission != null && sect != null) {
                    String[] mp = sectMission.split("\\|");
                    if (mp.length >= 4 && "Forage".equals(mp[0]) && "Herb".equals(mp[1])) {
                        int missionLv = Integer.parseInt(mp[2]);
                        int missionQty = Integer.parseInt(mp[3]);
                        if (findMissionStack(Item.ItemType.Herb, missionLv, missionQty) != null) {
                            if (location == sect.location) deliverMissionMaterials(Item.ItemType.Herb, missionLv, missionQty);
                            else if (targetQuests.stream().noneMatch(q -> "Deliver Materials".equals(q.name)))
                                targetQuests.add(new Quest("Deliver Materials", true, 65, 3, 80, sect.location));
                        }
                    }
                }
                if (player == 1 && sectMission != null) syncMissionQuest(); // the counter ("carrying 4/10") moves
                questSuccess = 2;
                break;

            case "Mine":
                mine();
                if (sect != null) sectPoints += 2;
                if (sectMission != null && sect != null) {
                    String[] mp = sectMission.split("\\|");
                    if (mp.length >= 4 && "Mine".equals(mp[0]) && "Metal".equals(mp[1])) {
                        int missionLv = Integer.parseInt(mp[2]);
                        int missionQty = Integer.parseInt(mp[3]);
                        if (findMissionStack(Item.ItemType.Metal, missionLv, missionQty) != null) {
                            if (location == sect.location) deliverMissionMaterials(Item.ItemType.Metal, missionLv, missionQty);
                            else if (targetQuests.stream().noneMatch(q -> "Deliver Materials".equals(q.name)))
                                targetQuests.add(new Quest("Deliver Materials", true, 65, 3, 80, sect.location));
                        }
                    }
                }
                if (player == 1 && sectMission != null) syncMissionQuest();
                questSuccess = 2;
                break;

            case "Reincarnate": {
                String why = Reincarnation.perform(this);
                if (why != null) { if (player > 0 && skipping <= 0) System.out.println(why); questSuccess = -2; }
                else questSuccess = 2;
                break;
            }

            case "Test":      // legacy name kept for old saves
            case "Meditate":
                meditate();
                questSuccess = 2;
                break;

            case "Sell Technique": {
                if (location.city && !location.libraries.isEmpty()) {
                    sellBestTechnique(location.libraries.get(0));
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
                location.tournamentName = Naming.tournament(location.name, GameDriver.time / 52);
                if (player > 0 && skipping <= 0) System.out.println("=== The " + location.tournamentName + " is announced at " + location.name + "! Realm " + minMajor + "-" + maxMajor + ", 8 weeks registration. Prize: " + prize + " ===");
                questSuccess = 2;
                break;
            }

            case "Register Tournament": {
                if (location.tournamentTimer <= 0 || location.tournamentRegistrants.contains(this)) { questSuccess = -2; break; }
                if (majorLvl < location.tournamentMinMajor || majorLvl > location.tournamentMaxMajor) { questSuccess = -2; break; }
                if (sect != null && Lore.isEntryTournament(location)) { if (player > 0 && skipping <= 0) System.out.println("The Convocation is a way into a sect, and you already belong to " + sect.name + "."); questSuccess = -2; break; }
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
                ArrayList<String> options = new ArrayList<>();
                for (String mm : sect.questBoard) if (!mm.startsWith("Deliver|") || Life.canMake(this, mm)) options.add(mm); // only those who can make (or, for a human, buy) the goods take a delivery
                if (options.isEmpty()) { questSuccess = -2; break; }
                String chosen;
                if (player == 1 && DecisionBroker.promptable(this)) {
                    String[] labels = new String[options.size() + 1];
                    for (int i = 0; i < options.size(); i++) labels[i] = missionText(options.get(i));
                    labels[options.size()] = "Not now";
                    int pick = DecisionBroker.ask(this, "Which sect mission do you take?", labels, options.size());
                    if (pick < 0 || pick >= options.size()) { questSuccess = -2; break; }
                    chosen = options.get(pick);
                } else chosen = options.get(rand.nextInt(options.size()));
                acceptMission(chosen);
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
                if (canAfford(arrayCost)) {
                    deductSs(arrayCost);
                } else if (location.cityTreasury >= arrayCost) {
                    location.cityTreasury -= arrayCost;
                } else { questSuccess = -2; break; }
                location.arrayBag.add(array);
                if (player > 0 && skipping <= 0) System.out.println("Installed " + array + " at " + location.name);
                questSuccess = 2;
                break;
            }

            case "Buy Sect Array": {
                if (sect == null || !sect.isMaster(this) || location != sect.location) { questSuccess = -2; break; }
                int arrayLvl = Math.min(100, (majorLvl + 1) * 10);
                Item array = new Item(this, arrayLvl, Item.ItemType.Array);
                long cost = itemPrice(array);
                if (canAfford(cost)) {
                    deductSs(cost);
                } else if (sect.sectCash >= cost) {
                    sect.sectCash -= cost;
                } else { questSuccess = -2; break; }
                location.arrayBag.add(array);
                if (player > 0 && skipping <= 0) System.out.println("Installed " + array + " at " + sect.name + "!");
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
                final Character hired = merchant; final Area where = location;
                Map.later(() -> { hired.credit(incentive); hired.targetQuests.add(new Quest("Restock Trip", true, 80, 5, 100, where)); }); // (the merchant may stand on another tile)
                if (player > 0 && skipping <= 0) System.out.println("Enticed " + merchant + " to open a shop in " + location.name);
                questSuccess = 2;
                break;
            }

            case "Donate to Sect": {
                if (sect == null || location != sect.location) { questSuccess = -2; break; }
                backpack.removeIf(i -> i.itemType == null);      // purge broken (deconstructed) gear
                sect.treasury.removeIf(t -> t.name == null);
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
                        Item copy = new Item(item.itemType, item.lvl, donate); // the constructor sets quantity exactly (the old "-1" lost a unit per new stack)
                        copy.name = item.name;
                        sect.treasury.add(copy);
                    }
                    earnedPoints += sect.donationPoints(item, donate);
                }
                // Pills: donate what's no longer useful
                for (Item pill : new ArrayList<>(pillBag)) {
                    int donate = 0;
                    if (pill.lvl <= majorLvl) {
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
                if (sect == null || location != sect.location || sect.treasury.isEmpty()) {
                    if (player > 0 && skipping <= 0) System.out.println("Buy from Sect: " + (sect == null ? "you have no sect." : location != sect.location ? "you must be at your sect's home, " + sect.location.name + "." : "the treasury is empty."));
                    questSuccess = -2; break;
                }
                boolean boughtAny = false;
                for (Item item : new ArrayList<>(sect.treasury)) {
                    if (item.quantity <= 0) { sect.treasury.remove(item); continue; }
                    boolean wantHerb  = item.itemType == Item.ItemType.Herb
                            && (trait == Traits.Alchemist || trait == Traits.Tailor || trait == Traits.Scribe)
                            && materialBag.stream().noneMatch(m -> m.itemType == Item.ItemType.Herb && m.quantity >= CRAFT_COST);
                    boolean wantMetal = item.itemType == Item.ItemType.Metal && trait == Traits.Blacksmith
                            && materialBag.stream().noneMatch(m -> m.itemType == Item.ItemType.Metal && m.quantity >= CRAFT_COST);
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
                                Item copy = new Item(item.itemType, item.lvl, buyQty);
                                copy.name = item.name;
                                materialBag.add(copy);
                            }
                            if (player > 0 && skipping <= 0) System.out.println("Bought " + buyQty + "x " + item.name + " from sect treasury for " + cost + " sect points.");
                            boughtAny = true;
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
                            boughtAny = true;
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
                            boughtAny = true;
                            break;
                        }
                    }
                }
                if (!boughtAny && player > 0 && skipping <= 0) System.out.println("Buy from Sect: nothing in the treasury suits you (herbs or metal for your craft, or gear better than yours), or you lack sect points (you have " + sectPoints + ").");
                questSuccess = boughtAny ? 2 : -2;
                break;
            }

        }


        return questSuccess;
    }

    public int targetQuests (Quest quest) {
        if (QuestLife.owns(quest.name)) return QuestLife.run(this, quest);
        switch (quest.name) {
            case "Find Lost Beast":
                return Taming.reunite(this) > 0 ? 1 : -1;
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

            case "Festival Trade":
                return Life.festivalTrade(this);

            case "Perform": {
                int pr = Life.concert(this);
                return pr > 0 ? 1 : -1;
            }

            case "Go Sell":
                if (location.city) {
                    sell(location);
                    location.population = (int) Math.min(Integer.MAX_VALUE, (long) location.population + Math.max(1, ss / 1000000)); // int += overflowed negative
                }
                return 1;

            case "Sell Technique": {
                if (location.city && !location.libraries.isEmpty()) {
                    sellBestTechnique(location.libraries.get(0));
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
            case "Revenge": {
                Character enemy = characters.get(quest.targetCharacter);
                if (enemy != null && !enemy.dead) {
                    partyBattleCheck(enemy);
                }
                return 1;
            }
            case "Poison Contract": {
                Character victim = characters.get(quest.targetCharacter);
                if (victim == null || victim.dead) return 1;
                if (victim.location != location) return 0;
                return Life.contract(this, victim);
            }
            case "Deliver Goods":
                if (location == null || sect == null || location != sect.location) return 0;
                Life.deliverMission(this);
                return 1;
            case "Bounty Hunt": {
                Character enemy = characters.get(quest.targetCharacter);
                if (enemy != null && !enemy.dead) {
                    partyBattleCheck(enemy);
                }
                return 1;
            }
            case "Propose Alliance": {
                Character target = characters.get(quest.targetCharacter);
                if (target != null && !target.dead && target.sect != null && sect != null && target.sect.isMaster(target)
                        && !sect.isAlly(target.sect) && !sect.isAtWar() && target.sect.allianceRefusal(sect) == null
                        && (target.player != 1 || DecisionBroker.askYesNo(target, this + " of " + sect + " proposes an alliance with " + target.sect + ". Accept?", true, this, "alliance"))) {
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
                    // A human target chooses; refusing is treated like being unable to pay (the demanding sect declares war)
                    boolean willPay = target.canAfford(tribute)
                            && (target.player != 1 || DecisionBroker.askYesNo(target, this + " of " + sect + " demands " + tribute + " ss in tribute. Pay? (refusing means war)", true, this, "tribute"));
                    if (willPay) {
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
                    if (accepts && target.player == 1) // a human elder decides for themselves
                        accepts = DecisionBroker.askYesNo(target, this + " of " + sect + " offers you a place in " + sect + ". Defect from " + oldSect + "?", false, this, "defect");
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
                partyBeastHunt();
                if (sect != null) sectPoints += 5;
                // Reward for clearing a monster bounty area
                if (quest.targetArea != null && quest.targetArea.monsters <= 0) {
                    long reward = (long) quest.targetArea.lvl * quest.targetArea.lvl * 50;
                    ss += reward;
                    convertCurrency();
                    if (player > 0 && skipping <= 0)
                        System.out.println(this + " cleared the monsters from " + quest.targetArea.name + " and claimed " + reward + " ss!");
                }
                return 1;
            // Player-picked targets ("seek <id> friend|fight|spar"): go to that cultivator and act on them
            case "Befriend": {
                Character t = charById(quest.targetCharacter);
                if (t != null && !t.dead && t != this) interact(t);
                return 1;
            }
            case "Talk To": { // a conversation, as the Talk button (a human); the same chat as a plain meeting for anyone else
                Character t = charById(quest.targetCharacter);
                if (t != null && !t.dead && t != this) {
                    if (player == 1 && t.age >= 8 && t.trait != Traits.Beast && t.trait != Traits.Monster && Dialogue.converse(this, t)) changeRelationship(t, 0);
                    else interact(t);
                }
                return 1;
            }
            case "Duel": {
                Character t = charById(quest.targetCharacter);
                if (t != null && !t.dead && t != this) partyBattleCheck(t);
                return 1;
            }
            case "Spar": {
                Character t = charById(quest.targetCharacter);
                if (t != null && !t.dead && t != this && t.age >= 8) {
                    sparring = true; t.sparring = true;
                    battleCheck(t);
                    sparring = false; t.sparring = false;
                }
                return 1;
            }
            case "Disperse":
                return 1; // arriving at the quieter tile is the whole quest
            case "Defend":
                Defense.arrive(this, quest.targetArea); // the attacker, if it is still there, is fought
                return 1;
            case "Donate Array":
                if (sect != null && location == sect.location && !arrayBag.isEmpty()) {
                    int donated = arrayBag.size();
                    while (!arrayBag.isEmpty()) installArray(location);
                    sectPoints += donated * 10;
                    if (player > 0 && skipping <= 0)
                        System.out.println(this + " donated " + donated + " array(s) to " + sect.name + "! (+" + (donated * 10) + " sect points)");
                }
                return 1;
            case "Seek Territory":
                beastBattleCheck();
                return 1;
            case "Heaven Treasure": {
                if (location.hasHeaven()) {
                    Treasure.claim(this, location);
                }
                return 1;
            }
            case "Study Trip":
                studyHere();
                return 1;
            case "Sect Mission": { // at the sect's home: hand in what the mission asks for, or say what is still missing
                if (sect == null || sectMission == null) return 1;
                String[] mp = sectMission.split("\\|");
                if (mp.length < 4) return 1;
                boolean done = false;
                try {
                    if ("Forage".equals(mp[0]) || "Mine".equals(mp[0])) {
                        done = deliverMissionMaterials("Forage".equals(mp[0]) ? Item.ItemType.Herb : Item.ItemType.Metal, Integer.parseInt(mp[2]), Integer.parseInt(mp[3]));
                        if (!done && player > 0 && skipping <= 0) System.out.println("Sect mission: you do not carry enough yet (" + missionText(sectMission) + "). '" + ("Forage".equals(mp[0]) ? "forage" : "mine") + "' first, then come back and 'qdo'.");
                    } else if ("Deliver".equals(mp[0])) {
                        done = Life.deliverMission(this);
                        if (!done && player > 0 && skipping <= 0) System.out.println("Sect mission: you do not carry the goods yet (" + missionText(sectMission) + "). 'make' them or 'buy market <#>', then 'qdo'.");
                    } else if ("Hunt".equals(mp[0])) { // the quest walks to beast ground (see syncMissionQuest); 'qdo' there fights one beast, and any beast fought anywhere counts
                        if (location.monsters <= 0) { if (player > 0 && skipping <= 0) System.out.println("Sect mission: " + missionLine() + ". No beasts live here; 'hunt' where they do."); }
                        else if (!partyBeastHunt() && player > 0 && skipping <= 0) System.out.println("Sect mission: the beasts here are too strong (or too weak to bother with) for you. " + missionLine() + ".");
                    } else if (player > 0 && skipping <= 0) System.out.println("Sect mission: " + missionLine() + ".");
                } catch (NumberFormatException e) { return 1; }
                return (done || sectMission == null) ? 1 : 0;
            }
            case "Refine Blood": {
                Character alchemist = charById(quest.targetCharacter);
                if (alchemist == null || alchemist.dead || alchemist.trait != Traits.Alchemist) return -1;
                return refineBloodWith(alchemist) ? 1 : -1;
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
                        Item.ItemType mType = "Herb".equals(mp[1]) ? Item.ItemType.Herb : Item.ItemType.Metal;
                        // Paid only if the full quantity is actually handed over; otherwise the mission is abandoned without reward
                        if (!deliverMissionMaterials(mType, Integer.parseInt(mp[2]), Integer.parseInt(mp[3]))) {
                            sectMission = null; sectMissionBonus = 0;
                        }
                    }
                }
                return 1;
            }
            case "Restock Trip": {
                if (location.city) {
                    boolean hasShopHere = ownedShops.stream().anyMatch(s -> s.location == location);
                    if (!hasShopHere && ownedShops.size() < maxShops() && location.shops.size() < location.maxShopSlots()) {
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
                if (!location.city) return 0;
                // Sell all non-equipped, non-spare backpack items into this city's pool at full price
                ArrayList<Item> cargo = sellableCargo();
                long earned = 0;
                for (Item item : cargo) {
                    long price = itemPrice(item);
                    earned += price;
                    boolean merged = false;
                    for (Item ci : location.backpack) {
                        if (item.stackable && ci.name.equals(item.name) && ci.lvl == item.lvl) { ci.quantity += item.quantity; merged = true; break; } // only stackables merge: two swords are not "a sword x2"
                    }
                    if (!merged) location.backpack.add(item);
                }
                backpack.removeAll(cargo);
                ss += earned;
                convertCurrency();
                location.population = (int) Math.min(Integer.MAX_VALUE, (long) location.population + Math.max(1L, earned / 1000000L));
                if (player > 0 && skipping <= 0) System.out.println(this + " sold cargo at " + location.name + " for " + earned + " ss");
                return 1;
            }
            case "Recruit Member": {
                Character recruit = characters.get(quest.targetCharacter);
                final Organization mine = sect; // (the talk may end with this person out of the sect, or dead: the sect named here is the one that invites)
                if (recruit != null && !recruit.dead && mine != null && mine.canJoin(recruit)) {
                    interact(recruit);
                    if (dead || sect != mine || recruit.dead) return 1;
                    if (recruit.player != 1 || DecisionBroker.askYesNo(recruit, this + " invites you to join " + mine + ". Join?", true, this, "invite"))
                        mine.join(recruit);
                }
                return 1;
            }
            case "Explore Dungeon": {
                Area dungeon = quest.targetArea;
                if (dungeon == null || dungeon.dungeonType == null) return 1;
                switch (dungeon.dungeonType) {
                    case LOST_RUINS:
                        if (dungeon.dungeonCharges == 0)
                            dungeon.dungeonCharges = Math.max(3, dungeon.lvl / 2 + rand.nextInt(Math.max(1, dungeon.lvl / 2 + 1)));
                        if (dungeon.monsters > 0) beastBattleCheck();
                        long treasure = (long)(dungeon.lvl) * dungeon.lvl * 100L;
                        ss += treasure; convertCurrency();
                        if (rand.nextInt(15) == 0) {
                            Technique.techniqueType tt = rand.nextInt(2) == 0 ? Technique.techniqueType.Movement : Technique.techniqueType.Martial;
                            techniqueList.add(new Technique(tt, this, Math.max(1, rand.nextInt(dungeon.lvl / 2 + 1))));
                            if (player > 0 && skipping <= 0) System.out.println(this + " found an ancient technique in the Lost Ruins!");
                        }
                        dungeon.dungeonCharges--;
                        if (dungeon.dungeonCharges <= 0) {
                            dungeon.dungeonType = null; dungeon.restoreName();
                            if (player > 0 && skipping <= 0)
                                System.out.println("The Lost Ruins of " + dungeon.name + " have been fully explored and fade into history.");
                        }
                        break;
                    case BEAST_LAIR:
                        for (int i = 0; i < 3 && dungeon.monsters > 0; i++) beastBattleCheck();
                        if (dungeon.monsters <= 0) {
                            dungeon.dungeonType = null; dungeon.restoreName();
                            if (player > 0 && skipping <= 0)
                                System.out.println("The Beast Lair at " + dungeon.name + " has been hunted out!");
                        }
                        break;
                    case MARTIAL_TRIAL:
                        tempLvlCheck();
                        int trialPower = Math.max(1, dungeon.lvl / 5);
                        int trialWin = Math.max(5, Math.min(95, 50 + 5 * (tempLvl - trialPower)));
                        if (rand.nextInt(100) < trialWin) {
                            if (martialTechnique != null) martialTechnique.upgrade(this);
                            if (martialTechnique2 != null) martialTechnique2.upgrade(this);
                            for (int i = 0; i < 3; i++) cultivate();
                            if (player > 0 && skipping <= 0) System.out.println(this + " passed the Martial Trial!");
                        }
                        break;
                    case HAUNTED_GROUNDS:
                        for (int i = 0; i < 5; i++) cultivate();
                        if (rand.nextInt(25) == 0 && potential < 100) givePotential(1, this + " found an insight in the Haunted Grounds!");
                        break;
                    case CELESTIAL_TRIAL:
                        for (int i = 0; i < 3 && dungeon.monsters > 0; i++) beastBattleCheck();
                        for (int i = 0; i < 20; i++) cultivate();
                        if (rand.nextInt(4) == 0 && potential < 100) givePotential(1, this + " found an insight at the Celestial Trial!");
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
                        if (dead || sect == null) return 1; // raider fell (or was expelled) in the fight
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
                if (canShopHere()) {
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
                    if (pill != null && pill.quantity > 0 && canAfford(itemPrice(pill))) {
                        // Buy ONE pill for its price (the whole stack used to transfer for a single unit's cost)
                        long potentialPrice = itemPrice(pill);
                        deductSs(potentialPrice);
                        pill.quantity--;
                        if (pill.quantity <= 0) location.pillBag.remove(pill);
                        addToBag(pillBag, new Item(Item.ItemType.Pill, Item.PillType.Potential, pill.lvl, 1));
                        location.receivePayment(potentialPrice);
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
                        addTemp(new Character(this));
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
                        givePotential(1, "Your grief has strengthened your resolve."); // (never lowers a potential above 100: the Usurper path)
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
                    if (player == 1 || player == 2) unborn.takeControlFrom(this);   // a human-controlled ghost carries on as the child
                    unborn.originalName = (originalName != null ? originalName + " → " : "") + name + " " + lastName;
                    if (player > 0 || mother.player > 0) System.out.println(this + " possesses " + mother + "'s unborn child! (was: " + unborn.originalName + ")");
                    // Ghost dies permanently
                    dead = true; noteDeath();
                    addRem(this);
                    Map.later(() -> { location.removeChar(this); if (sect != null) sect.resign(this); }); // (a ghost may have joined a sect again: it must not stay on its roll)
                    return 1;
                }
                return 0;
            }

            case "Ghost City Rebirth": {
                if (!location.city) return 0;
                Character rebirth = new Character(location);
                rebirth.age = GameDriver.START_AGE; // reborn at the age everyone starts at
                rebirth.trait = Traits.Ghost;
                rebirth.originalName = (originalName != null ? originalName + " → " : "") + name + " " + lastName;
                // Carry over technique mastery at half lvl so lifetimes of practice aren't lost
                rebirth.martialTechnique = new Technique(martialTechnique);
                rebirth.martialTechnique.lvl = Math.max(1, martialTechnique.lvl / 2);
                rebirth.movementTechnique = new Technique(movementTechnique);
                rebirth.movementTechnique.lvl = Math.max(1, movementTechnique.lvl / 2);
                // Profession accumulates across rebirths — Ghosts grow wiser each life
                rebirth.professionLvl = professionLvl + 1;
                if (player == 1 || player == 2) rebirth.takeControlFrom(this);
                addTemp(rebirth);
                if (player > 0 || rebirth.player > 0) System.out.println(this + " is reborn as " + rebirth + " in " + location.name + "! (was: " + rebirth.originalName + ")");
                dead = true; noteDeath();
                addRem(this);
                Map.later(() -> { location.removeChar(this); if (sect != null) sect.resign(this); }); // (the ghost's roll in a sect ends with it)
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
        if (quest.userCreated) { // player-authored waypoint: arriving completes it
            if (player > 0 && skipping <= 0) System.out.println("You reached " + quest.name + ".");
            return 1;
        }
        return -1;
    }

    /** Tiles between two places when every step may be diagonal (the world wraps). */
    static int cheb(Area a, Area b) {
        int dx = Math.abs(a.x - b.x), dy = Math.abs(a.y - b.y);
        return Math.max(Math.min(dx, range - dx), Math.min(dy, range - dy));
    }

    /** The tile a walker takes next, the way pathfindXY would take them (null when there is nowhere to go). */
    static int[] stepTile(int x, int y, int destX, int destY) {
        int xDiff = x - destX, yDiff = y - destY, divider = range / 2;
        int nx = x, ny = y;
        if (xDiff != 0) nx = (xDiff < -divider || (xDiff >= 1 && xDiff <= divider)) ? (x <= 0 ? range - 1 : x - 1) : (x >= range - 1 ? 0 : x + 1);
        if (yDiff != 0) ny = (yDiff < -divider || (yDiff >= 1 && yDiff <= divider)) ? (y <= 0 ? range - 1 : y - 1) : (y >= range - 1 ? 0 : y + 1);
        return (nx == x && ny == y) ? null : new int[]{nx, ny};
    }

    /** Where someone who is walking will be after this turn: along the way to their quest (or to the tile a player sent them to). Null for one who stands still. */
    static int[] nextStepOf(Character t) {
        if (t == null || t.location == null || t.dead || t.exploreTicks > 0) return null;
        if (t.player > 0 && t.autoX >= 0 && t.movement == 'c') return stepTile(t.location.x, t.location.y, t.autoX, t.autoY);
        if (t.isManualPc() || t.currentQuest == null) return null;
        Area dest = t.currentQuest.targetArea;
        if (t.currentQuest.category == Quest.QuestCategory.Character) { Character o = charById(t.currentQuest.targetCharacter); if (o != null && !o.dead) dest = o.location; }
        if (dest == null || dest == t.location) return null;
        return stepTile(t.location.x, t.location.y, dest.x, dest.y);
    }

    /** A target one or two tiles away is not chased to where it stands but to where it is going: both walk a tile a turn, so a chaser who aims at the old tile never catches a walker.
     *  Null when the target is farther away (or already here): then the plain walk to its tile does. */
    int[] interceptAim(Character target) {
        if (target == null || target.location == null || location == null) return null;
        int d = cheb(location, target.location);
        if (d == 0) { aimTid = -1; return null; }
        // The NPCs have decided and their steps are not taken yet (a person who plays acts in between): where the walker goes this week is known, not guessed
        if (GameDriver.movesPlanned && target.turnT == GameDriver.time && target.player == 0 && !target.dead) { aimTid = -1; return leadAim(target); }
        if (d > 2) { aimTid = -1; return null; }
        // someone who has not moved since the last look is standing still whatever their quest says (a pause, a fight, a frozen turn): go to them, not past them
        boolean sameTurn = aimTid == target.id && GameDriver.time == aimT;
        boolean still = !sameTurn && aimTid == target.id && aimX == target.location.x && aimY == target.location.y && GameDriver.time - aimT <= 2;
        if (!sameTurn) { aimTid = target.id; aimX = target.location.x; aimY = target.location.y; aimT = GameDriver.time; }
        return still ? null : nextStepOf(target);
    }
    private transient int aimTid = -1, aimX, aimY, aimT; // the last person aimed at, where they stood and when (interceptAim)

    /** Where to go to meet a walker whose step is planned: the tile he steps to this week, or, when he is too far ahead to be met there, the first tile on his way
     *  that can be reached no later than he reaches it (both walk a tile a week), or the place he is heading for. Null: he stays where he is, go to him. */
    private int[] leadAim(Character t) {
        if (t.pendTo == null) return null;
        Area dest = destinationOf(t);
        int x = t.pendTo.x, y = t.pendTo.y;
        for (int k = 1; k <= 40; k++) {
            if (chebXY(location.x, location.y, x, y) <= k) return new int[]{x, y};
            if (dest == null || (x == dest.x && y == dest.y)) break;
            int[] n = stepTile(x, y, dest.x, dest.y);
            if (n == null) break;
            x = n[0]; y = n[1];
        }
        return dest != null ? new int[]{dest.x, dest.y} : new int[]{t.pendTo.x, t.pendTo.y};
    }

    /** The place a walker's quest leads to (a person's quest: where that person stands now); null for one without a quest. */
    static Area destinationOf(Character t) {
        if (t == null || t.currentQuest == null) return null;
        Area dest = t.currentQuest.targetArea;
        if (t.currentQuest.category == Quest.QuestCategory.Character) { Character o = charById(t.currentQuest.targetCharacter); if (o != null && !o.dead) dest = o.location; }
        return dest;
    }

    static int chebXY(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x1 - x2), dy = Math.abs(y1 - y2);
        return Math.max(Math.min(dx, range - dx), Math.min(dy, range - dy));
    }

    /** One step toward a quest's target; a person who is close and walking is met at the tile they are heading for. 1 = standing with the target. */
    private int pathfinderFor(Quest q) {
        if (q.category == Quest.QuestCategory.Character) {
            Character target = charById(q.targetCharacter);
            int[] aim = (target != null && !target.dead && !(isManualPc() && pc.movedThisTick)) ? interceptAim(target) : null;
            if (aim != null) {
                pathfindXY(aim[0], aim[1]);
                return location == target.location ? 1 : 0;
            }
        }
        return pathfinder(q.targetArea);
    }

    public int pathfinder (Area destination) {
        // A player walking by hand moves themselves; a duty quest (toggled on) may only walk on ticks the player stood still
        if (isManualPc() && pc.movedThisTick) return (location == destination) ? 1 : 0;
        return pathfindXY(destination.x, destination.y);
    }

    // One step toward a tile coordinate (used by scripts); creates tiles on the way like any other move
    public void stepToward(int tx, int ty) {
        pathfindXY(tx, ty);
    }

    private int pathfindXY(int destX, int destY) {
        int xDiff = location.x - destX;
        int yDiff = location.y - destY;
        int divider = range / 2;

        if (planning) { // plan the one step (a diagonal when both axes differ); standing on the place already is arriving
            char mx = xDiff == 0 ? 'x' : (xDiff < -divider || (xDiff >= 1 && xDiff <= divider)) ? 'a' : 'd';
            char my = yDiff == 0 ? 'x' : (yDiff < -divider || (yDiff >= 1 && yDiff <= divider)) ? 's' : 'w';
            if (mx == 'x' && my == 'x') return 1;
            movement(mx == 'x' ? my : my == 'x' ? mx : mx == 'a' ? (my == 'w' ? 'Q' : 'Z') : (my == 'w' ? 'E' : 'C'));
            return 0;
        }
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

        if (location.x == destX && location.y == destY) {
            return 1;
        }
        else {
            return 0;
        }

    }

    public int event() {
        if (location.characters.isEmpty()) return -2; // nobody (not even this character) is listed in the tile: nothing to happen
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
                if (materialBag.stream().noneMatch(i -> i.itemType == Item.ItemType.Metal && i.quantity >= CRAFT_COST))
                    if (spotQuests.stream().noneMatch(q -> "Mine".equals(q.name)))
                        spotQuests.add(new Quest("Mine", false, 40, 3, 70));
                smithing();
                break;
            case Friendly:
                if (location.characters.isEmpty()) break;
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
                if (materialBag.stream().noneMatch(i -> i.itemType == Item.ItemType.Herb && i.quantity >= CRAFT_COST))
                    if (location.herbMaterials > 0 && spotQuests.stream().noneMatch(q -> "Forage".equals(q.name)))
                        spotQuests.add(new Quest("Forage", false, 40, 3, 70));
                if (materialBag.stream().noneMatch(i -> i.itemType == Item.ItemType.Metal && i.quantity >= CRAFT_COST))
                    if (location.metalMaterials > 0 && spotQuests.stream().noneMatch(q -> "Mine".equals(q.name)))
                        spotQuests.add(new Quest("Mine", false, 40, 3, 70));
                tailoring();
                break;
            case Scribe:
                if (materialBag.stream().noneMatch(i -> i.quantity >= ARRAY_MIN))
                    if (spotQuests.stream().noneMatch(q -> "Forage".equals(q.name)))
                        spotQuests.add(new Quest("Forage", false, 40, 3, 70));
                array();
                if (!arrayBag.isEmpty()) {
                    if (sect != null && location == sect.location) {
                        // Donate all crafted arrays to sect
                        while (!arrayBag.isEmpty()) installArray(location);
                    } else if (sect != null) {
                        // Queue a trip to deliver arrays to sect
                        if (targetQuests.stream().noneMatch(q -> "Donate Array".equals(q.name))
                                && (currentQuest == null || !"Donate Array".equals(currentQuest.name)))
                            targetQuests.add(new Quest("Donate Array", true, 50, 5, 65, sect.location));
                    } else {
                        installArray(location); // no sect, install locally
                    }
                }
                break;
            case Leader:
                if (professionLvl == 1) {
                    cityLeadership(); // try city founding every tick; sets professionLvl=50 on success
                }
                if (professionLvl == 50) {
                    sectLeadership();
                }
                break;
            case Innovative: // ideas come from fighting; a hundred of them make a technique
                if (inspiration >= INSPIRATION_NEEDED) inventTechnique();
                if (equippedWeapon != null && equippedWeapon.enchantment == null && rand.nextInt(20) == 0) enchantWeapon();
                break;
            case Merchant:
                if (location.city) {
                    boolean hasShopHere = ownedShops.stream().anyMatch(s -> s.location == location);
                    if (!hasShopHere && ownedShops.size() < maxShops() && location.shops.size() < location.maxShopSlots()) {
                        Shop shop = new Shop(this, location);
                        ownedShops.add(shop);
                        location.shops.add(shop);
                    }
                    Shop myShop = ownedShops.stream().filter(s -> s.location == location).findFirst().orElse(null);
                    if (myShop != null) {
                        myShop.restockFromCity(location);
                        Life.restockShopGoods(myShop);
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
                                    location.receivePayment(cost);
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
                                if (equippedWeapon != null) { // move the old one to the shelf for real (it used to end up on the shelf AND in the backpack)
                                    final Item oldW = equippedWeapon;
                                    equippedWeapon = null;
                                    backpack.removeIf(i -> i == oldW);
                                    myShop.inventory.add(oldW);
                                }
                                myShop.inventory.remove(item);
                                backpack.add(item);
                                equip();
                            } else if (item.itemType == Item.ItemType.Clothing
                                    && (equippedClothing == null || item.lvl > equippedClothing.lvl)) {
                                if (equippedClothing != null) {
                                    final Item oldC = equippedClothing;
                                    equippedClothing = null;
                                    backpack.removeIf(i -> i == oldC);
                                    myShop.inventory.add(oldC);
                                }
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
            case Tamer: case Poisoner: case Drunken: case Chef: case Musician:
                return Life.profession(this);
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
            int dropAmount = (rand.nextInt(10) + 1) * 10; // crafts cost 1000 materials, so gathering yields 10-100 per action
            if (dropAmount > location.herbMaterials) {
                dropAmount = location.herbMaterials;
            }
            // Cycling through the materialBag to see if they have the item. If they do, it adds to the quantity of the item. If not, the item is created and added to their inventory
            for (Item item : materialBag) {
                if (item.itemType == Item.ItemType.Herb && item.lvl == rewardLvl) {
                    hasItem = true;
                    item.quantity += dropAmount;
                    break; // one stack only (duplicate stacks each used to receive the full drop)
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
            int dropAmount = (rand.nextInt(10) + 1) * 10;
            if (dropAmount > location.metalMaterials) {
                dropAmount = location.metalMaterials;
            }
            // Cycling through the materialBag to see if they have the item. If they do, it adds to the quantity of the item. If not, the item is created and added to their inventory
            for (Item item : materialBag) {
                if (item.itemType == Item.ItemType.Metal && item.lvl == rewardLvl) {
                    hasItem = true;
                    item.quantity += dropAmount;
                    break;
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
    /** Techniques that may be sold: not one of the four equipped, and above the default potential 3. */
    ArrayList<Technique> sellableTechniques() {
        ArrayList<Technique> r = new ArrayList<>();
        if (techniqueList == null) return r;
        for (Technique t : techniqueList) if (Library.sellable(t) && t != martialTechnique && t != martialTechnique2 && t != movementTechnique && t != movementTechnique2) r.add(t);
        return r;
    }

    /** Sells the strongest sellable technique to a library (20% fee, the technique is gone from this character). Returns true if sold. */
    boolean sellBestTechnique(Library lib) {
        Technique best = null;
        for (Technique t : sellableTechniques()) if (best == null || t.potential > best.potential) best = t;
        if (best == null) {
            if (player > 0 && skipping <= 0) System.out.println("Nothing to sell: the library takes techniques of potential " + Library.MIN_SELL_POTENTIAL + " or more that you do not have equipped.");
            return false;
        }
        long fee = Library.sellPrice(best);
        String name = best.name;
        boolean ok = lib.sellTechnique(this, best);
        if (player > 0 && skipping <= 0) System.out.println(ok ? "Sold " + name + " (potential " + best.potential + ") to the library for " + fee + " ss (a fee of 20%; the book is theirs now)." : "The library cannot pay " + fee + " ss for " + name + " right now.");
        return ok;
    }

    // ---------------------------------------------------------------- sect missions
    /** A board entry ("Forage|Herb|2|500|15") in plain words. */
    static String missionText(String mm) {
        String[] p = mm == null ? new String[0] : mm.split("\\|");
        if (p.length < 4) return String.valueOf(mm);
        String bonus = p.length >= 5 ? ", +" + p[4] + " sect points" : "";
        switch (p[0]) {
            case "Forage": return "Forage " + p[3] + " herbs of level " + p[2] + " or better" + bonus;
            case "Mine": return "Mine " + p[3] + " metal of level " + p[2] + " or better" + bonus;
            case "Hunt": return "Hunt " + p[3] + " more beast(s) (level " + p[2] + " ground)" + bonus;
            case "Deliver": return "Deliver " + p[3] + " " + p[1].toLowerCase() + " of level " + p[2] + " or better" + bonus;
            default: return mm;
        }
    }

    /** Takes a mission from the board (the board's own string). Returns true if taken. */
    boolean acceptMission(String mm) {
        if (sect == null || sectMission != null || mm == null || !sect.questBoard.contains(mm)) return false;
        sectMission = mm;
        String[] parts = mm.split("\\|");
        sectMissionBonus = parts.length >= 5 ? Integer.parseInt(parts[4]) : 10;
        try { sectMissionTotal = parts.length >= 4 ? Integer.parseInt(parts[3]) : 0; } catch (NumberFormatException e) { sectMissionTotal = 0; }
        if (player > 0 && skipping <= 0) System.out.println("Accepted sect mission: " + missionText(mm) + ".");
        if (player == 1) syncMissionQuest();
        return true;
    }

    void abandonMission() { sectMission = null; sectMissionBonus = 0; if (player == 1) syncMissionQuest(); }

    /** A human's accepted mission is a pinned quest (it walks to the sect's home, and 'qdo' hands the work in); it goes away when the mission is done, abandoned, or the character runs on its own (AUTO). */
    void syncMissionQuest() {
        boolean want = sect != null && sectMission != null && player == 1 && pc().mode != PlayerControl.Mode.AUTO;
        Quest have = (currentQuest != null && "Sect Mission".equals(currentQuest.name)) ? currentQuest : null;
        if (have == null) for (Quest q : targetQuests) if ("Sect Mission".equals(q.name)) { have = q; break; }
        if (!want) {
            if (have != null) { if (have == currentQuest) currentQuest = null; targetQuests.remove(have); }
            return;
        }
        String label = "Sect Mission: " + missionLine();
        Area where = sectMission.startsWith("Hunt|") ? huntGround() : sect.location; // a hunt is done on beast ground, the rest is handed in at the sect's home
        if (where == null) where = sect.location;
        if (have == null) { Quest q = new Quest("Sect Mission", false, 75, 0, 75, where); q.pin(75); q.label = label; targetQuests.add(q); }
        else { have.label = label; have.targetArea = where; }
    }

    /** The nearest known tile with beasts this character can fight (here counts), or null. */
    Area huntGround() {
        Area best = null; int bd = Integer.MAX_VALUE;
        ArrayList<Area> options = new ArrayList<>(knownLocations);
        options.add(location);
        for (Area a : options) {
            if (a == null || a.monsters <= 0 || lvl < (a.lvl + a.highestLvl) / 2) continue;
            int d = PlayerControl.distance(location, a);
            if (d < bd) { bd = d; best = a; }
        }
        return best;
    }

    /** The best library technique worth buying in this city: better than the one worn of its type, not already known, affordable. Null if none. */
    Technique bestStudy(Area a) {
        if (a == null || !a.city || a.libraries == null) return null;
        Technique best = null;
        for (Library lib : a.libraries) for (Technique t : lib.techniques) {
            Technique worn = t.type == Technique.techniqueType.Martial ? martialTechnique : movementTechnique;
            if (worn == null || t.potential <= worn.potential || !canAfford(lib.buyPrice(t))) continue;
            final String n = t.name;
            if (techniqueList.stream().anyMatch(x -> x.name.equals(n))) continue;
            if (best == null || t.potential > best.potential) best = t;
        }
        return best;
    }

    /** The nearest known city whose library has something worth studying, or null. */
    Area studyTarget() {
        Area best = null; int bestD = Integer.MAX_VALUE;
        for (Area area : knownLocations) {
            if (area == null || !area.city || area.libraries.isEmpty() || bestStudy(area) == null) continue;
            int dx = Math.abs(area.x - location.x), dy = Math.abs(area.y - location.y), d = Math.min(dx, range - dx) + Math.min(dy, range - dy);
            if (d < bestD) { bestD = d; best = area; }
        }
        return best;
    }

    /** Buys the best technique worth having from the libraries of this city. Returns true if one was bought. */
    boolean studyHere() {
        Technique best = bestStudy(location);
        if (best == null) return false;
        for (Library lib : location.libraries) {
            if (!lib.techniques.contains(best)) continue;
            long price = lib.buyPrice(best);
            if (!lib.learnTechnique(this, best)) return false;
            if (player > 0 && skipping <= 0) System.out.println(this + " studied " + best + " at the library for " + price + " ss!");
            return true;
        }
        return false;
    }

    /** Potential Pills waiting for a buyer in this city: on the auction block, or (old saves) on a shelf. */
    static boolean potentialPillsAt(Area a) {
        if (a == null || !a.city) return false;
        if (a.pillBag != null) for (Item p : a.pillBag) if (p != null && p.itemType == Item.ItemType.Pill && p.pillType == Item.PillType.Potential) return true;
        if (a.auctionBag != null) for (Item p : a.auctionBag) if (p != null && p.itemType == Item.ItemType.Pill && p.pillType == Item.PillType.Potential) return true;
        return false;
    }

    /** How much Blood this character carries (1,000 make one Potential Pill). */
    int bloodAmount() {
        long n = 0;
        for (Item i : materialBag) if (i.itemType == Item.ItemType.Blood) n += Math.max(0, i.quantity);
        return (int) Math.min(Integer.MAX_VALUE, n);
    }

    /** A living alchemist who can refine Blood: here first, then in the cities this character knows. */
    Character findAlchemist() {
        for (Character c : new ArrayList<>(location.characters)) if (isRefiner(c)) return c;
        int checked = 0;
        for (Area area : knownLocations) {
            if (area == null || !area.city || area == location) continue;
            if (++checked > 40) break;
            for (Character c : area.characters) if (isRefiner(c)) return c;
        }
        return null;
    }

    private boolean isRefiner(Character c) { return c != null && c != this && !c.dead && c.born && c.trait == Traits.Alchemist && c.age >= 8 && c.professionLvl >= 10; }

    /** The alchemist turns this character's Blood into Potential Pills: one pill per 1,000 Blood (as many as the alchemist's skill allows), for a fee equal to the pill's price. Returns true if any were made. */
    static volatile int stRefined; // playtest counter: Potential Pills refined from Blood (not saved)
    boolean refineBloodWith(Character alchemist) {
        Item blood = null;
        for (Item i : materialBag) if (i.itemType == Item.ItemType.Blood && i.quantity >= 1000) { blood = i; break; }
        if (blood == null) return false;
        Item sample = new Item(Item.ItemType.Pill, Item.PillType.Potential, 11, 1);
        long fee = Math.max(1, itemPrice(sample));
        long wealth = ss + sc * 1_000_000L + dc * 1_000_000_000_000L;
        int n = (int) Math.min(Math.min(blood.quantity / 1000, alchemist.professionLvl / 10 + 1), wealth / fee);
        if (n <= 0) {
            if (player > 0 && skipping <= 0) System.out.println(alchemist.name + " " + alchemist.lastName + " would refine your Blood for " + fee + " ss a pill; you cannot pay that yet.");
            return false;
        }
        deductSs(fee * n);
        alchemist.ss += fee * n; alchemist.convertCurrency();
        blood.quantity -= 1000 * n;
        stRefined += n;
        if (blood.quantity <= 0) materialBag.remove(blood);
        addToBag(pillBag, new Item(Item.ItemType.Pill, Item.PillType.Potential, 11, n));
        if (player > 0 && skipping <= 0) System.out.println(alchemist.name + " " + alchemist.lastName + " refined your Blood into " + n + " Potential Pill(s) for " + fee * n + " ss.");
        consume();
        return true;
    }

    private int pillTotal() { int n = 0; for (Item i : pillBag) if (i != null) n += Math.max(0, i.quantity); return n; }

    public int alchemy() {
        int success = -2;
        final int pillsBefore = pillTotal();
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
                int quantity = material.quantity / 1000; // each Potential pill costs 1000 Blood (was /50, which drove the stack negative)
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
                    if (item.itemType == Item.ItemType.Pill && item.pillType == pill.pillType && item.lvl == pill.lvl) {
                        hasItem = true;
                        item.quantity += pill.quantity;
                        break;
                    }
                }
                if (!hasItem) {
                    pill.maker = (name + " " + lastName).intern(); pill.makerID = id; pill.brand = Naming.pillBrand(this).intern();
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
        materialBag.removeIf(m -> m.quantity <= 0); // the herbs and blood that were used up
        if (player > 0 && skipping <= 0 && age >= 8 && pillTotal() > pillsBefore) {
            System.out.println("You brew " + (pillTotal() - pillsBefore) + " pill(s) ('bag' shows them).");
        }
        return success;
    }
    /** Gives the potential that this many Potential Pills are worth and returns the whole points gained (0 at 100 or more). The band between two gates takes GATE_PILLS[gate] pills to cross,
     *  so a pill is worth 4 points below the first gate and a thirtieth of a point before the last; what is not a whole point yet is kept in `potentialProgress`.
     *  Every chance gain (insights, trials, the epoch) is given in pills too, so the same event is worth much less to someone who is already high. Exact rewards (story, reincarnation, edits) set `potential` directly. */
    int givePotential(double pills) {
        if (pills <= 0 || potential >= 100) return 0;
        int gained = 0;
        double left = pills, progress = potentialProgress;
        while (left > 1e-9 && potential < 100) {
            int k = 0;
            while (k < GATE_POTENTIAL.length - 1 && potential >= GATE_POTENTIAL[k]) k++;
            double perPill = (GATE_POTENTIAL[k] - (k == 0 ? 0 : GATE_POTENTIAL[k - 1])) / (double) GATE_PILLS[k]; // points of potential in one pill, in this band
            double need = (1 - progress) / perPill;                                                              // pills until the next whole point
            if (left + 1e-9 >= need) { left -= need; potential++; gained++; progress = 0; }
            else { progress += left * perPill; left = 0; }
        }
        potentialProgress = potential >= 100 ? 0 : progress;
        return gained;
    }

    /** givePotential, and a line for the person who plays: what happened, the points gained (or how far the next point has come). */
    int givePotential(double pills, String what) {
        int g = givePotential(pills);
        if (player > 0 && skipping <= 0) System.out.println(what + (g > 0 ? " Potential +" + g + " (now " + potential + ")." : potential >= 100 ? "" : " Your potential stirs (" + Math.round(potentialProgress * 100) + "% of the next point)."));
        return g;
    }

    public void consume(){
        try { consumePills(); } finally { pillBag.removeIf(p -> p.quantity <= 0); } // (a swallowed last pill leaves no empty stack)
    }
    private void consumePills() {
        for (Item item : pillBag) {
            if (item.itemType == Item.ItemType.Pill) {
                if (item.pillType == Item.PillType.Potential && item.quantity > 0 && potential < 100) {
                    givePotential(1);
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
            int odds = (player.player == 1 || this.player == 1) ? Math.max(30, 100 - 35 * GameDriver.difficulty) : 100; // a hostile world picks on the player more
            if (this.affiliation.equals("Evil")) {
                if (rand.nextInt(odds) == 0) {
                    battleCheck(player);
                }
            }
            else if ((this.affiliation.equals("Good") && player.affiliation.equals("Evil")) ) {
                if (rand.nextInt(odds) == 0) {
                    battleCheck(player);
                }
            }
            if (dead || player.dead) return; // a fight may have killed either party; don't rebuild relationships with the dead
            // Sparring: close friends spar together (10% chance when rel >= 1)
            Relationship relVal = relationshipList.contains(player.id);
            if (relVal != null && relVal.value >= 1 && !sparring && !player.sparring && rand.nextInt(10) == 0) {
                this.sparring = true;
                player.sparring = true;
                battleCheck(player);
                // battle() exits early (flee, array, etc.) without clearing the flag, which used to leave friends "sparring" forever
                this.sparring = false;
                player.sparring = false;
                if (dead || player.dead) return;
            }

            // Changes the Relationship Value of this Character and the targeted Player

            changeRelationship(player, 0);
            checkBirth(player);

            if (this.player == 0 && player.player == 1) {
                if (!Dialogue.approach(this, player) && skipping <= 0) System.out.println(this + " talked to " + player); // someone speaks to a human: words, a scene or a window (Dialogue)
            } else if ((this.player > 0 || player.player > 0) && skipping <= 0) {
                System.out.println(this + " talked to " + player);
            }
        }
    }
    public void changeRelationship (Character player, int change) {
        // Checks to see if the relationship doesn't exist. If it doesn't, it creates the relationship
        if (relationshipList.contains(player.id) == null) {
            relationshipList.add(new Relationship(player.id, 0));
        }
        if (player.relationshipList.contains(this.id) == null) {
            player.relationshipList.add(new Relationship(this.id, 0));
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
        // A human may refuse a friendship (the friend policy), and someone they avoid never grows closer
        Character humanSide = this.player == 1 ? this : player.player == 1 ? player : null;
        if (humanSide != null && change > value) {
            Character other = humanSide == this ? player : this;
            if (temp.refused || temp2.refused) change = value;
            else if (value < 1 && change >= 1 && other.born && other.trait != null && other.age >= 8 && !isKin(humanSide, other) && !friendshipAccepted(humanSide, other)) { temp.refused = true; temp2.refused = true; change = Math.min(value, 0); }
        }
        temp.changeValue(change);
        temp2.changeValue(change);

        checkRelationship(player);

        if (change == 0 && !temp.refused){
            relationshipList.remove(temp);
            player.relationshipList.remove(temp2);
            temp = null;
            temp2 = null;
        }
    }
    private static double socialBonus(Character c) { return c.trait == Traits.Friendly ? 2.0 : c.trait == Traits.Lucky ? 1.5 : 1.0; }

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

        if ((this.mother != null && this.mother == player.mother) || (this.father != null && this.father == player.father)) { // two founders with null parents are not siblings
            positive *= 2;
            negative /= 2;
        }

        // Charm: a Friendly person on either side doubles good feeling and halves bad feeling; a Lucky one does so by half
        double bond = Math.max(socialBonus(this), socialBonus(player));
        positive = (int) (positive * bond);
        negative = (int) (negative / bond);

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
            Notifications.onMarriage(this, player);
            if (gender.equals("Female")) {
                this.lastName = player.lastName;
            }
            else {
                player.lastName = this.lastName;
            }
        }
        // One social quest per person, matched to the relationship tier (Like/Dislike capped, stronger ties persist)
        this.syncSocialQuest(player, relationship);
        Relationship back = player.relationshipList.contains(this.id);
        player.syncSocialQuest(this, back != null ? back.value : 0);
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
                addTemp(new Character(temp));
            }
        }
    }
    public void smithing() {
        if (player == 0 && backpack.size() >= 16) return; // a full pack: sell first
        Item material = null;
        // Selects the metal to be used
        final int skillCap = professionLvl / 10 + 2;           // the material level the skill can make use of; richer metal works at that level
        for (Item item : materialBag) {
            if (item.itemType == Item.ItemType.Metal && item.quantity >= CRAFT_COST && item.lvl > 0) {
                if (material == null || Math.min(material.lvl, skillCap) < Math.min(item.lvl, skillCap) || (Math.min(material.lvl, skillCap) == Math.min(item.lvl, skillCap) && item.lvl < material.lvl)) material = item;
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
            int ml = Math.min(material.lvl, skillCap);
            int rawLvl = rand.nextInt(ml * 5) + (1 + ((ml * 10) / 2));
            weapon = new Item(this, temp, Math.min(rawLvl, Math.min((majorLvl + 1) * 10, 100)));
            material.quantity -= CRAFT_COST;
            if (material.quantity <= 0) materialBag.remove(material); // (a used-up stack is gone, not kept at 0)
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
        if (player == 0 && backpack.size() >= 16) return;
        Item material = null;
        // Selects the material to be used
        final int skillCap = professionLvl / 10 + 2;           // see smithing(): richer material works at the level the skill allows
        for (Item item : materialBag) {
            if (item.quantity >= CRAFT_COST && item.lvl > 0) {
                if (material == null || Math.min(material.lvl, skillCap) < Math.min(item.lvl, skillCap) || (Math.min(material.lvl, skillCap) == Math.min(item.lvl, skillCap) && item.lvl < material.lvl)) material = item;
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
            int ml = Math.min(material.lvl, skillCap);
            int rawLvl = rand.nextInt(ml * 5) + (1 + ((ml * 10) / 2));
            clothing = new Item(this, Math.min(rawLvl, Math.min((majorLvl + 1) * 10, 100)), Item.ItemType.Clothing);
            material.quantity -= CRAFT_COST;
            if (material.quantity <= 0) materialBag.remove(material); // (a used-up stack is gone, not kept at 0)
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
            if (item.quantity >= ARRAY_MIN) {
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
            material.quantity -= CRAFT_COST;
            if (material.quantity <= 0) materialBag.remove(material); // (a used-up stack is gone, not kept at 0)
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
    public synchronized void convertCurrency() {
        final long RATE = 1_000_000L;
        // Division instead of one loop pass per million (a huge balance used to spin billions of iterations)
        if (ss >= RATE) { sc += ss / RATE; ss %= RATE; }
        if (sc >= RATE) { dc += sc / RATE; sc %= RATE; }
        if (dc >= RATE) { ij += dc / RATE; dc %= RATE; }
    }

    // Adds an item to a bag, merging stackables into an existing stack of the same name and level (quantity clamped, never wrapped)
    static void addToBag(ArrayList<Item> bag, Item item) {
        if (item.stackable) {
            for (Item t : bag) {
                if (t.name != null && t.name.equals(item.name) && t.lvl == item.lvl) {
                    t.quantity = (int) Math.min(Integer.MAX_VALUE, (long) t.quantity + item.quantity);
                    return;
                }
            }
        }
        bag.add(item);
    }

    /** Money arriving from another tile's business (a sale at the shop, a gift): one thread at a time touches a purse. */
    public synchronized void credit(long amount) { ss += amount; convertCurrency(); }

    public synchronized boolean canAfford(long amount) {
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

    public synchronized void deductSs(long amount) {
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
            if (area == null || !area.city || area == location) continue;
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
            if (area == null || !area.city
                    || area.backpack == null || area.backpack.isEmpty()) continue;
            int score = area.backpack.size() * 10;
            for (Shop s : ownedShops) { if (s.location == area) { score += 50; break; } }
            int dist = Math.abs(area.x - location.x) + Math.abs(area.y - location.y);
            score += Math.max(0, 30 - dist);
            if (score > bestScore) { bestScore = score; best = area; }
        }
        return best;
    }

    // True if any shop in the city stocks a weapon/clothing upgrade this character could afford right now (mirrors buyGear's rules)
    // Backpack items a merchant may sell as cargo: everything except the equipped gear and the best spare weapon and spare clothing
    private ArrayList<Item> sellableCargo() {
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
        return cargo;
    }

    // A random nearby tile (within 3, else 6) that is under half its crowd capacity and not itself a hotspot
    private Area findQuietTile() {
        for (int radius = 3; radius <= 6; radius += 3) {
            ArrayList<Area> options = new ArrayList<>();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    int x = location.x + dx, y = location.y + dy;
                    if ((dx == 0 && dy == 0) || x < 0 || y < 0 || x >= range || y >= range) continue;
                    Area a = tiles[x][y];
                    if (a != null && !a.crowdHotspot && a.characters.size() < a.crowdCapacity() / 2) options.add(a);
                }
            }
            if (!options.isEmpty()) return options.get(rand.nextInt(options.size()));
        }
        return null;
    }

    /** The price of the cheapest piece of gear that would be an improvement and within a realm of my level in this city, by the per-turn index: {from a shop, from the pool}; Long.MAX_VALUE = none. */
    private long[] cheapestGear(Area city) {
        int curW = equippedWeapon != null ? equippedWeapon.lvl : -1, curC = equippedClothing != null ? equippedClothing.lvl : -1;
        int[][] g = city.gearIndex();
        long shop = Long.MAX_VALUE, pool = Long.MAX_VALUE;
        int fromW = Math.max(Math.max(curW + 1, lvl - 9), 1), fromC = Math.max(Math.max(curC + 1, lvl - 9), 1);
        for (int l = fromW; l < 102; l++) { if (g[0][l] > 0 && shop == Long.MAX_VALUE) shop = priceAtLevel(l); if (g[2][l] > 0 && pool == Long.MAX_VALUE) pool = priceAtLevel(l); if (shop != Long.MAX_VALUE && pool != Long.MAX_VALUE) break; }
        for (int l = fromC; l < 102; l++) { if (g[1][l] > 0) shop = Math.min(shop, priceAtLevel(l)); if (g[3][l] > 0) pool = Math.min(pool, priceAtLevel(l)); if (g[1][l] > 0 || g[3][l] > 0) break; }
        return new long[]{shop, pool};
    }

    static long priceAtLevel(int l) { return l <= 0 ? 0 : Math.max(1, (long) (Math.pow(l, 2.5) * 10)); }

    /** Is there something here I could buy and afford? Answered from the per-turn index (a shop asks 1.5x the price, the pool 1.25x), without reading the shelves: with thousands of pieces
     *  for sale every character asks about every city it knows. */
    /** Buys one Cultivation Pill of the next realm from this city's stock if there is one and it can be paid. */
    boolean buyNextRealmPill() {
        if (location == null || !location.city || location.pillBag == null) return false;
        for (int pi = 0; pi < location.pillBag.size(); pi++) {
            Item pill = location.pillBag.get(pi);
            if (pill.itemType == Item.ItemType.Pill && pill.pillType == Item.PillType.Cultivation && pill.lvl == majorLvl + 1 && pill.quantity > 0) {
                long price = itemPrice(pill);
                if (!canAfford(price)) return false;
                deductSs(price);
                location.receivePayment(price); // a dead lord used to receive (and swallow) this
                boolean found = false;
                for (Item p : pillBag) {
                    if (p.name.equals(pill.name)) { p.quantity++; found = true; break; }
                }
                if (!found) pillBag.add(new Item(Item.ItemType.Pill, pill.pillType, pill.lvl, 1));
                if (--pill.quantity <= 0) location.pillBag.remove(pill);
                return true;
            }
        }
        return false;
    }

    public boolean hasBuyableGear(Area city) {
        long[] cheap = cheapestGear(city);
        return (cheap[0] != Long.MAX_VALUE && canAfford(cheap[0] * 3 / 2)) || (cheap[1] != Long.MAX_VALUE && canAfford(cheap[1] * 5 / 4));
    }

    /** Is there anything to buy gear from here: a city with a shop, or goods in its pool. */
    boolean canShopHere() { return location != null && location.city && (!location.shops.isEmpty() || (location.backpack != null && !location.backpack.isEmpty())); }

    /** Does one of the city's shops have room on its shelves? */
    static boolean shopRoom(Area city) {
        for (Shop s : city.shops) if (s.inventory.size() < s.capacity()) return true;
        return false;
    }

    /** What the city asks for a piece from its pool (it paid half for it; a shop asks one and a half times the price). */
    static long poolPrice(Item it) { return Math.max(1, itemPrice(it) * 5 / 4); }

    /** The best piece of gear in the city's pool that is better than what is worn and affordable; false when there is none. */
    private boolean buyFromPool(boolean weapon, int curLvl) {
        if (location.backpack == null) return false;
        Item best = null;
        for (Item it : new ArrayList<>(location.backpack)) {
            if (it == null || it.itemType != (weapon ? Item.ItemType.Weapon : Item.ItemType.Clothing) || it.lvl < lvl - 9 || it.lvl <= curLvl || !canAfford(poolPrice(it))) continue;
            if (best == null || it.lvl > best.lvl) best = it;
        }
        if (best == null || !location.backpack.remove(best)) return false;
        long price = poolPrice(best);
        deductSs(price);
        location.receivePayment(price);
        backpack.add(best);
        equip();
        return true;
    }

    public void buyGear() {
        if (!canShopHere()) return;
        long[] cheap = cheapestGear(location);
        if ((cheap[0] == Long.MAX_VALUE || !canAfford(cheap[0] / 2)) && (cheap[1] == Long.MAX_VALUE || !canAfford(cheap[1]))) return; // nothing affordable on any shelf (a shop may ask less than 1.5x, so half the price is the bar)
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
                            && canAfford(price)) {            // (the old "ss <= price*4" clause blocked wealthy buyers and disagreed with hasBuyableGear)
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
                            && canAfford(price)) {
                        if (shop.sellToBuyer(this, item)) { equip(); boughtArmor = true; break; }
                    }
                }
            }
            if (boughtWeapon && boughtArmor) break;
        }
        // the merchants' shops come first; what they lack may be in the city's own pool
        if (!boughtWeapon && buyFromPool(true, currentWeaponLvl)) boughtWeapon = true;
        if (!boughtArmor && buyFromPool(false, currentArmorLvl)) boughtArmor = true;
    }

    public static long itemPrice(Item item) {
        if (item.lvl <= 0) return 0;
        long base = Math.max(1, (long)(Math.pow(item.lvl, 2.5) * 10));
        if (item.itemType == Item.ItemType.Pill && item.pillType == Item.PillType.Potential)
            return base > Long.MAX_VALUE / 5 ? Long.MAX_VALUE : base * 5;
        return base;
    }

    public void sell(Area city) {
        backpack.removeIf(i -> i.itemType == null); // purge broken (deconstructed) gear left over from older saves
        if (city.backpack != null) city.backpack.removeIf(i -> i.name == null);
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
        Item keepInstrument = Life.bestInstrument(this);
        // Luxuries: Heaven-Sent treasures are auctioned (never sold to a shop); a city too small for an auction house keeps them for you
        int consignedLots = 0;
        for (Item item : new ArrayList<>(backpack)) {
            if (!item.heavenSent || item == equippedWeapon || item == equippedClothing) continue;
            if (!Auctions.hasHouse(city)) continue;
            backpack.remove(item);
            Auctions.consign(city, item, this);
            consignedLots++;
        }
        ArrayList<Item> toSell = new ArrayList<>();
        for (Item item : backpack) {
            if (item == equippedWeapon || item == equippedClothing) continue;
            if (item == spareWeapon   || item == spareClothing)    continue;
            if (item == keepInstrument) continue;
            if (item.heavenSent) continue; // kept: this city has no auction house
            toSell.add(item);
        }
        // Find best-paying merchant shop in this city (beats city pool 0.50 rate)
        Shop bestShop = null;
        for (Shop shop : city.shops) {
            if (bestShop == null || shop.buyRate > bestShop.buyRate) bestShop = shop;
        }
        if (bestShop != null && bestShop.buyRate < 0.5) bestShop = null; // a shop paying less than the city pool (0.5x) is skipped; players can set low rates

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
                    if (item.stackable && cityItem.name.equals(item.name) && cityItem.lvl == item.lvl) {
                        cityItem.quantity += item.quantity;
                        merged = true;
                        break;
                    }
                }
                if (!merged && city.backpack.size() < POOL_CAP) city.backpack.add(item);
                else if (!merged) city.receivePayment(Life.refundShare(price)); // the pool is full: the item is scrapped, and the city is paid what it was worth
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
                if (item.lvl <= majorLvl) pillsToSell.add(item); // pill levels are 1-10 (consume() needs lvl == majorLvl + 1); the old *10 sold the pill needed next
            }
        }
        if (!Auctions.hasHouse(city)) pillsToSell.removeIf(p -> p.pillType == Item.PillType.Potential); // Potential Pills are only ever auctioned
        for (Item pill : new ArrayList<>(pillsToSell)) {
            if (pill.pillType == Item.PillType.Potential) { // put on the block: paid 90% when it sells
                pillBag.remove(pill); pillsToSell.remove(pill);
                Auctions.consign(city, pill, this);
                consignedLots++;
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
        Life.sellGoods(this); // food, wine and poison
        if (player > 0 && skipping <= 0 && age >= 8) {
            long totalEarned = merchantEarned + poolEarned + pillEarned;
            System.out.println("Sold " + (toSell.size() + pillsToSell.size()) + " items for " + totalEarned + " ss at " + city.name
                    + (consignedLots > 0 ? "; put " + consignedLots + " lot(s) on the auction block (held when " + Auctions.LOTS_TO_HOLD + " lots wait or " + Auctions.MAX_WAIT + " weeks pass; you get 90%)." : ""));
        }
    }

    static final int BEAST_KILL_ODDS = 100; // a wild beast that beats a cultivator kills them 1 time in this many (and then awakens as a Monster)
    static final int INSPIRATION_NEEDED = 100;
    int inspiration; // an Innovative character's ideas: fights give them, a hundred make a new technique

    /** An Innovative character takes inspiration from a fight (a hunt counts): more for a win, more from a stronger foe. */
    void gainInspiration(Character foe, boolean won) {
        if (trait != Traits.Innovative || foe == null) return;
        tempLvlCheck(); foe.tempLvlCheck();
        double ratio = Math.max(0.5, Math.min(2.0, (foe.tempLvl + 1.0) / (tempLvl + 1.0)));
        int before = inspiration;
        inspiration = Math.min(2 * INSPIRATION_NEEDED, inspiration + (int) Math.max(1, Math.round((won ? 5 : 2) * ratio)));
        if (player > 0 && skipping <= 0) {
            if (before < INSPIRATION_NEEDED && inspiration >= INSPIRATION_NEEDED)
                System.out.println("Your head is full of ideas from the fighting (Inspiration " + inspiration + "/" + INSPIRATION_NEEDED + "): use Profession to set down a new technique.");
            else if (inspiration > before) System.out.println("Inspiration +" + (inspiration - before) + " (" + inspiration + "/" + INSPIRATION_NEEDED + ").");
        }
    }

    /** Spends a hundred inspiration on a new technique, always kept (to use, teach or sell). */
    Technique inventTechnique() {
        inspiration = Math.max(0, inspiration - INSPIRATION_NEEDED);
        Technique.techniqueType type = rand.nextBoolean() ? Technique.techniqueType.Martial : Technique.techniqueType.Movement;
        int potential = Math.max(5, Math.min(100, (majorLvl + 1) * 8 + rand.nextInt(10) + professionLvl / 10));
        Technique technique = new Technique(type, this, potential);
        techniqueList.add(technique);
        equip();
        itemsCrafted++;
        //Leveling up your profession skill
        if (professionLvl < 100) {
            professionExp += (technique.lvl + 1) * 3;
            while (professionExp > professionExpMax && professionLvl < 100) {
                professionLvl++;
                professionExp -= professionExpMax;
                professionExpMax = Math.pow(10, professionLvl / 10.);
            }
        }
        // Donate a copy to sect library (earns sectPoints)
        if (sect != null && sect.sectLibrary != null) {
            final String tname = technique.name;
            boolean notYetIn = sect.sectLibrary.techniques.stream().noneMatch(t -> t.name.equals(tname));
            if (notYetIn && rand.nextInt(5) == 0) {
                sect.sectLibrary.techniques.add(new Technique(technique));
                sectPoints += 10;
            }
        }
        if (player > 0 && skipping <= 0 && age >= 8 && !(isManualPc())) System.out.println("Created " + technique);
        return technique;
    }

    void enchantWeapon() {
        String[] enchants = {"Sharp", "Spiritual", "Swift", "Heavy", "Fierce", "Wrathful", "Sundering"};
        equippedWeapon.enchantment = enchants[rand.nextInt(enchants.length)];
        equippedWeapon.enchantBonus = rand.nextInt(5) + 1;
        if (player > 0 && skipping <= 0 && !isManualPc()) System.out.println(this + " enchanted their weapon: " + equippedWeapon);
    }

    public void cityLeadership() {
        if (majorLvl >= 2 && !location.city && rand.nextInt(location.spiritualVein ? 20 : 100) == 0 && Settlements.cityRoom()) { // a spirit vein draws a city five times as often
            establishCity();
        } else if (majorLvl >= 2 && location.city && location.cityOwner == null && rand.nextInt(50) == 0) {
            claimCity();
        }
    }
    private void establishCity() {
        Settlements.cityCount.incrementAndGet();
        location.city = true;
        location.cityOwner = this;
        location.population = 20_000 + rand.nextInt(180_000); // (the old formula overflowed int to ~1.4 billion)
        location.backpack = new SafeList<>();
        location.materialBag = new SafeList<>();
        location.pillBag = new SafeList<>();
        location.arrayBag = new SafeList<>();
        location.libraries.add(new Library(location));
        professionLvl = 50;
        if (location.geoName == null) location.geoName = location.name;
        if (!location.isOriginis()) location.name = Naming.city(this, location.population); // the city gets its own name; the tile keeps geoName for its picture
        Chronicle.founded("city", "the city of " + location.name + " (population " + location.population + ")", this, location);
        if (player > 0 && skipping <= 0) System.out.println(this + " has founded the city of " + location.name + "!");
    }
    private void claimCity() { // a leaderless city
        location.cityOwner = this;
        if (professionLvl < 50) professionLvl = 50;
        if (player > 0 && skipping <= 0) System.out.println(this + " has claimed lordship of " + location.name + "!");
    }
    // Manual founding (the "found city" command): same rules as cityLeadership() without the dice. Returns null on success, else why not.
    String foundCityNow() {
        if (majorLvl < 2) return "Founding or claiming a city needs Major Lvl 2 (you are Major Lvl " + majorLvl + ").";
        if (!location.city) { establishCity(); return null; }
        if (location.cityOwner == null) { claimCity(); return null; }
        return location.name + " is already a city" + (location.cityOwner == this ? " you rule." : " ruled by " + location.cityOwner + ".");
    }
    public void sectLeadership(){
        if (location.sect == null && sect == null && majorLvl >= 2) {
            if ((location.city || rand.nextInt(location.spiritualVein ? 20 : 100) == 0) && Settlements.sectRoom()) { // ...and a sect too
                establishSect();
            }
        }
    }
    private void establishSect() {
        Settlements.sectCount.incrementAndGet();
        location.sect = new Organization(this);
        if (professionLvl != 50) professionLvl = 100;
        Chronicle.founded("sect", location.sect.name, this, location);
        if (player > 0 && skipping <= 0) System.out.println(this + " has founded " + location.sect.name + "!");
    }
    // Manual founding (the "found sect" command). Returns null on success, else why not.
    String foundSectNow() {
        if (majorLvl < 2) return "Founding a sect needs Major Lvl 2 (you are Major Lvl " + majorLvl + ").";
        if (sect != null) return "You already belong to " + sect.name + ". Resign first.";
        if (location.sect != null) return location.name + " already holds " + location.sect.name + ".";
        establishSect();
        return null;
    }
    public void equip () {
        // a human in MANUAL mode picks their own gear (Settings.autoEquipManual brings the automatic upgrade back); empty slots are still filled
        final boolean keepMine = player == 1 && pc != null && pc.mode == PlayerControl.Mode.MANUAL && !Settings.autoEquipManual && !equipNow;
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
                else if (!keepMine && item.itemType == Item.ItemType.Weapon && equippedWeapon.lvl < item.lvl) {
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
                } else if (!keepMine && item.itemType == Item.ItemType.Clothing && equippedClothing.lvl < item.lvl) {
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
    // Finds the lowest-level stack of `type` with level >= minLvl holding at least qty units (null if none)
    private Item findMissionStack(Item.ItemType type, int minLvl, int qty) {
        Item best = null;
        for (Item m : materialBag) {
            if (m.itemType == type && m.lvl >= minLvl && m.quantity >= qty && (best == null || m.lvl < best.lvl)) best = m;
        }
        return best;
    }

    // Hands the mission quantity to the sect treasury and pays the bonus; false (nothing moved, nothing paid) if the goods aren't there
    private boolean deliverMissionMaterials(Item.ItemType type, int minLvl, int qty) {
        if (sect == null) return false;
        Item stack = findMissionStack(type, minLvl, qty);
        if (stack == null) return false;
        final String itemName = stack.name;
        final int stackLvl = stack.lvl;
        int removed = removeItems(materialBag, itemName, qty);
        boolean merged = false;
        for (Item t : sect.treasury) { if (t.name.equals(itemName)) { t.quantity += removed; merged = true; break; } }
        if (!merged) sect.treasury.add(new Item(type, stackLvl, removed));
        sectPoints += sectMissionBonus;
        if (player > 0 && skipping <= 0) System.out.println("Sect mission complete! +" + sectMissionBonus + " SP");
        sectMission = null; sectMissionBonus = 0;
        return true;
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
        if (receiver == this) return;
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
        // All currency tiers change hands (only ss did before, so a dead tycoon's crystals vanished)
        receiver.ss += this.ss; this.ss = 0;
        receiver.sc += this.sc; this.sc = 0;
        receiver.dc += this.dc; this.dc = 0;
        receiver.ij += this.ij; this.ij = 0;
        receiver.convertCurrency();
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
            if (opponent.player == 1) opponent.pc().attackedThisTick = true; // interrupts a running hybrid script
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
        // First-year truce: the whole starting population shares Originis, so nobody may start a fight with another cultivator there until the year ends
        if (!sparring && GameDriver.time < GameDriver.TRUCE_WEEKS && location != null && location.isStartZone()) {
            if (player == 1 && skipping <= 0) System.out.println("A truce holds in Originis until the end of the first year: no fighting here yet.");
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

        // Array protection: attacker must break the strongest array before engaging (friendly spars don't break arrays)
        if (!sparring && opponent.location.arrayBag != null && !opponent.location.arrayBag.isEmpty()) {
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

        // A sect's seat or a city with a lord rises against an attacker it has reason to fear: the alarm, and the people on the spot fight at once (see Defense)
        if (!sparring) { Defense.onAttack(this, opponent); if (dead || opponent.dead) return; }

        // A human being challenged may decline (MANUAL asks; AUTO/HYBRID use the duel policy)
        if (!sparring && !opponent.sparring && opponent.player == 1 && opponent != this && !opponent.dead && !opponent.acceptChallenge(this)) return;

        // Decreases Relationship
        changeRelationship(opponent, 0);

        // Sect squabble escalation: repeated cross-sect fights lead to war
        if (!sparring && sect != null && opponent.sect != null && sect != opponent.sect && !sect.isAtWar()) {
            sect.squabbles++;
            if (sect.squabbles >= 5) {
                sect.declareWar(opponent.sect);
                if (player > 0 || opponent.player > 0) System.out.println(sect + " has declared war on " + opponent.sect + "!");
            }
        }

        if (this != opponent && !opponent.dead && opponent.time >= 0) {
            if (opponent.age < 8 && opponent.mother == this) {
                return; // a parent never duels their own child (the mother redirect below would make them fight themselves)
            }
            if (opponent.age < 8 && opponent.mother != null && !opponent.mother.dead) {
                battle(opponent.mother);
            } else {
                battle(opponent);
            }
        }
    }

    ArrayList<Integer> followerIds; // cultivators that joined this character's squad (pruned when counted; companion beasts are not listed)

    /** How many cultivators follow this leader right now, wherever they are (beasts do not count). */
    int squadSize() {
        if (followerIds == null) return 0;
        for (int i = followerIds.size() - 1; i >= 0; i--) {
            int fid = followerIds.get(i);
            Character f = fid >= 0 && fid < Map.characters.size() ? Map.characters.get(fid) : null;
            if (f == null || f.dead || f.partyLeaderId != id) followerIds.remove(i);
        }
        return followerIds.size();
    }

    public void joinParty(Character leader) {
        if (leader == null || leader.dead || leader == this) return;
        partyLeaderId = leader.id;
        if (trait != Traits.Beast && trait != Traits.Monster) {
            if (leader.followerIds == null) leader.followerIds = new ArrayList<>();
            if (!leader.followerIds.contains(id)) leader.followerIds.add(id);
        }
    }

    public void leaveParty() {
        partyLeaderId = -1;
    }

    public void tryRallyParty() { tryRallyParty(false); }

    // Friends (relationship >= 1) join 1/3 of the time. With openInvite, unaffiliated adventurers of a similar realm
    // and no ill will also sign on (1/4) — this is how hunting parties form among strangers.
    /** Rulers (sect masters, city lords) rarely lead squads of their own. */
    private boolean isRuler() { return (sect != null && sect.isMaster(this)) || (location != null && location.cityOwner == this); }

    /** How many cultivators a squad of this leader may hold (companion beasts do not count): a Leader without a throne gathers up to 12, everybody else up to 4. */
    int partyCap() { return trait == Traits.Leader && !isRuler() ? 12 : 4; }

    /** Whether `c` may sign on to this leader's squad. A human leader's policy decides: ask (a MANUAL player is asked each time; AUTO and scripts accept), auto, or never. */
    private int lastSquadAsk = -1; // the week this human was last asked to take someone into the squad
    private boolean recruitAccepted(Character c) {
        if (player != 1) return true;
        PlayerControl p = pc();
        if (p.squadPolicy == 2 || p.squadDeclined.contains(c.id)) return false;
        if (p.squadPolicy == 0 && DecisionBroker.promptable(this)) {
            if (lastSquadAsk == GameDriver.time) return false; // one question a turn: a crowded tile used to put thirty to you in a single hunt
            lastSquadAsk = GameDriver.time;
            int a = DecisionBroker.ask(this, c.name + " " + c.lastName + " (Lv." + c.lvl + ", " + c.getTrait() + ") wants to join your squad. Accept?", new String[]{"Accept", "Decline"}, 0, c, "squad");
            if (a != 0) { p.squadDeclined.add(c.id); return false; }
        }
        return true;
    }

    /** A friendship a human is offered (or that grows into one): the policy decides; a refusal is remembered and costs nothing. */
    /** Family bonds (parents, children, spouse, siblings) are never offered or refused as friendships. */
    private static boolean isKin(Character a, Character b) {
        return a.spouse == b || a.mother == b || a.father == b || b.mother == a || b.father == a
                || (a.mother != null && a.mother == b.mother) || (a.father != null && a.father == b.father);
    }

    private static boolean friendshipAccepted(Character human, Character other) {
        PlayerControl p = human.pc();
        if (p.friendPolicy == 2) return false;
        if (p.friendPolicy == 0 && DecisionBroker.promptable(human)) {
            return DecisionBroker.ask(human, other.name + " " + other.lastName + " (Lv." + other.lvl + ", " + other.getTrait() + ") would like to be your friend. Accept?", new String[]{"Accept", "Decline"}, 0, other, "friend") == 0;
        }
        return true;
    }

    /** `avoid`: this person will never become your friend (and a friendship you have is set back to nothing). `avoid off` lifts it. Costs nothing. */
    void avoid(Character other, boolean on) {
        if (other == null || other == this) return;
        if (relationshipList.contains(other.id) == null) relationshipList.add(new Relationship(other.id, 0));
        if (other.relationshipList.contains(id) == null) other.relationshipList.add(new Relationship(id, 0));
        Relationship a = relationshipList.contains(other.id), b = other.relationshipList.contains(id);
        a.refused = on; b.refused = on;
        if (on) { if (a.value > 0) a.changeValue(0); if (b.value > 0) b.changeValue(0); }
        checkRelationship(other);
    }

    public void tryRallyParty(boolean openInvite) {
        if (partyLeaderId >= 0) return;
        int have = squadSize(), cap = partyCap();
        boolean leader = trait == Traits.Leader && !isRuler();
        // Friends first: walk the (short) friend list rather than copying a tile that may hold thousands (this used to be the second-biggest cost of a tick).
        if (relationshipList.relationships != null) {
            final ArrayList<Relationship> friendsNow = relationshipList.copy();
            for (int i = 0; i < friendsNow.size() && have < cap; i++) {
                Relationship r = friendsNow.get(i);
                if (r == null || r.value < 1) continue;
                Character c = charById(r.friendID);
                if (c == null || c.dead || c == this || c.location != location || c.partyLeaderId >= 0 || c.player >= 1 || c.age < 8) continue;
                if (c.trait == Traits.Beast || c.trait == Traits.Monster) continue;
                if (rand.nextInt(3) == 0 && recruitAccepted(c)) { c.joinParty(this); have++; }
            }
        }
        if (!openInvite || have >= cap) return;
        // Strangers of a similar realm sign on too: look at a bounded stretch of the tile, from a random start, not at everybody.
        ArrayList<Character> here = location.characters;
        int n = here.size(), look = Math.min(n, 48), start = n > look ? rand.nextInt(n) : 0;
        for (int k = 0; k < look && have < cap; k++) {
            int idx = (start + k) % n;
            if (idx >= here.size()) break; // the tile changed under us
            Character c = here.get(idx);
            if (c == null || c.dead || c == this || c.partyLeaderId >= 0 || c.player >= 1 || c.age < 8) continue;
            if (c.trait == Traits.Beast || c.trait == Traits.Monster) continue;
            Relationship r = relationshipList.contains(c.id);
            if (r != null && r.value >= 1) continue; // friends were handled above
            if ((r == null || r.value >= 0) && Math.abs(c.majorLvl - majorLvl) <= (leader ? 2 : 1) && rand.nextInt(leader ? 2 : 4) == 0 && recruitAccepted(c)) { c.joinParty(this); have++; }
        }
    }

    // Leader plus every co-located follower attack one beast in turn; the beast tires after each fight.
    public boolean partyBeastHunt() {
        ArrayList<Character> party = new ArrayList<>();
        party.add(this);
        if (followerIds != null) for (int i = 0; i < followerIds.size(); i++) {
            int fid = followerIds.get(i);
            Character c = fid >= 0 && fid < Map.characters.size() ? Map.characters.get(fid) : null;
            if (c != null && !c.dead && c != this && c.location == location && c.partyLeaderId == this.id && c.age >= 8) party.add(c);
        }
        if (party.size() == 1) return beastBattleCheck();
        if (location.monsters <= 0) return false;
        for (Character p : party) p.tempLvlCheck();
        party.sort((a, b) -> Integer.compare(b.tempLvl, a.tempLvl));
        // The party may take on beasts its strongest member could handle alone
        if (party.get(0).lvl < (location.lvl + location.highestLvl) / 2) return false;
        location.monsters--;
        Character beast = new Character(location, party.get(0));
        for (int i = 0; i < party.size() && !beast.dead; i++) {
            Character fighter = party.get(i);
            if (fighter.dead) continue;
            beast.exhaustionPenalty = i * 2;
            FightReport.partyRound = i + 1; FightReport.partySize = party.size();
            try { fighter.battle(beast); } finally { FightReport.partyRound = 0; FightReport.partySize = 0; }
            fighter.beastFought();
        }
        beast.exhaustionPenalty = 0;
        return true;
    }

    public void dissolveParty() {
        if (followerIds == null || followerIds.isEmpty()) return; // nobody follows: nothing to dissolve (this used to copy the whole tile on every success)
        for (int i = 0; i < followerIds.size(); i++) {
            int fid = followerIds.get(i);
            Character c = fid >= 0 && fid < Map.characters.size() ? Map.characters.get(fid) : null;
            if (c != null && !c.dead && c.location == location && c.partyLeaderId == this.id)
                c.leaveParty();
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

        FightReport.partyStart(party, opponent); // a human in either party hears who is ganging up on whom
        for (int i = 0; i < party.size(); i++) {
            if (opponent.dead) break;
            Character fighter = party.get(i);
            if (fighter.dead || (fighter.age < 8 && !fighter.sparring)) continue;
            opponent.exhaustionPenalty = i * 2; // enemy tires from consecutive fights
            FightReport.partyRound = i + 1; FightReport.partySize = party.size();
            try { fighter.battleCheck(opponent); } finally { FightReport.partyRound = 0; FightReport.partySize = 0; }
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
        beastFought();
        return true;
    }

    /** A beast was fought (any hunt: the Hunt button, a Hunt Monster trip, a dungeon, a party): it counts toward a sect mission to hunt beasts. */
    void beastFought() {
        if (dead || sectMission == null || sect == null) return;
        String[] mp = sectMission.split("\\|");
        if (mp.length < 4 || !"Hunt".equals(mp[0]) || !"Beast".equals(mp[1])) return;
        int remaining;
        try { remaining = Integer.parseInt(mp[3]) - 1; } catch (NumberFormatException e) { return; }
        if (remaining <= 0) {
            sectPoints += sectMissionBonus;
            if (player > 0 && skipping <= 0) System.out.println("Hunt mission complete! +" + sectMissionBonus + " SP");
            sectMission = null; sectMissionBonus = 0; sectMissionTotal = 0;
        } else {
            sectMission = mp[0] + "|" + mp[1] + "|" + mp[2] + "|" + remaining + (mp.length > 4 ? "|" + mp[4] : "");
            if (player > 0 && skipping <= 0) System.out.println("Sect mission: " + missionLine() + ".");
        }
        if (player == 1) syncMissionQuest();
    }

    /** The mission in words with how far it has got: "Hunt 5 more beast(s) ... (2/5 done)", "Forage 10 herbs ... (carrying 4/10)". */
    String missionLine() {
        String text = missionText(sectMission);
        if (sectMission == null) return text;
        String[] mp = sectMission.split("\\|");
        if (mp.length < 4) return text;
        try {
            int need = Integer.parseInt(mp[3]);
            if ("Hunt".equals(mp[0]) && sectMissionTotal >= need && sectMissionTotal > 0) return text + " (" + (sectMissionTotal - need) + "/" + sectMissionTotal + " done)";
            if ("Forage".equals(mp[0]) || "Mine".equals(mp[0])) {
                Item.ItemType ty = "Forage".equals(mp[0]) ? Item.ItemType.Herb : Item.ItemType.Metal;
                int lv = Integer.parseInt(mp[2]), have = 0;
                for (Item m : materialBag) if (m.itemType == ty && m.lvl >= lv && m.quantity > have) have = m.quantity; // (one stack of the level must hold it all)
                return text + " (carrying " + Math.min(have, need) + "/" + need + ")";
            }
        } catch (NumberFormatException e) { /* the plain text */ }
        return text;
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
            FightReport.arrayAttempt(this, area, strongest.lvl, true, false);
            return true;
        }
        boolean backlash = rand.nextInt(20) == 0;
        if (backlash) {
            dead(null);
        }
        FightReport.arrayAttempt(this, area, strongest.lvl, false, backlash);
        return false;
    }

    public void installArray(Area area) {
        if (!arrayBag.isEmpty()) {
            area.arrayBag.add(arrayBag.remove(0));
        }
    }

    public void battle(Character opponent) {
        FightReport rep = FightReport.begin(this, opponent); // the human's account of the fight (null when no human or followed character is in it)
        try { battleCore(opponent); } finally { FightReport.end(rep); }
    }

    private void battleCore(Character opponent) {
        // (MESSAGE) System.out.println(this + " has challenged " + opponent + " to a fight!");


        // (NOTE) Optimizes Equipment and Techniques and Updates Corresponding TempLvl
        this.tempLvlCheck();
        opponent.tempLvlCheck();
        if (fx != null) Life.onBattle(this, opponent); // a poisoned blade

        // Defender gets a boost from arrays protecting their area (arrays shield people, not wild beasts: a hunter standing in an
        // array-protected tile used to make the beast it was hunting stronger)
        if (opponent.trait != Traits.Beast && opponent.trait != Traits.Monster
                && opponent.location.arrayBag != null && !opponent.location.arrayBag.isEmpty()) {
            int bestArrayLvl = 0;
            for (Item a : opponent.location.arrayBag) {
                if (a.lvl > bestArrayLvl) bestArrayLvl = a.lvl;
            }
            opponent.tempLvl += bestArrayLvl / 10 + 1;
            FightReport.arrayBoost(bestArrayLvl / 10 + 1);
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
        FightReport.odds(this, opponent, winChance, deathChance);

        if (winChance < caution) {
            if (fleeCheck(opponent)) {
                return;
            }
            else if (deathChance) {
                if (forbiddenCheck()) { this.dead(opponent); return; }
                forbiddenUsed = !forbiddenDeclined;
            }
        }
        else if (winChance > opponent.caution) {
            if(opponent.fleeCheck(this)) {
                return;
            }
            else if (deathChance) {
                if (opponent.forbiddenCheck()) { opponent.dead(this); return; }
                oppForbiddenUsed = !opponent.forbiddenDeclined;
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
                    if (forbiddenCheck()) { this.dead(opponent); return; }
                }
            } else if (winChance > opponent.caution && forbiddenUsed) {
                if (opponent.fleeCheck(this)) {
                    return;
                } else if (deathChance) {
                    if (opponent.forbiddenCheck()) { opponent.dead(this); return; }
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

        FightReport.odds(this, opponent, winChance, deathChance); // the figures the roll really uses (after any forbidden technique)
        durabilityCheck(opponent);

        if ((rand.nextInt(100) + 1) <= winChance) {
            FightReport.won(this, opponent); // the report states the winner itself
            if (!FightReport.active() && (player > 0 || opponent.player > 0)) System.out.println(this + " won the fight against " + opponent + "!");
            winCheck(opponent, deathChance);
        }
        else {
            FightReport.won(opponent, this);
            if (!FightReport.active() && (player > 0 || opponent.player > 0)) System.out.println(opponent + " won the fight against " + this + "!");
            opponent.winCheck(this, deathChance);
        }

    }

    // Returns true if a deathmatch, returns false if not
    public boolean deathCheck(Character opponent) {
        int deathChance = 1;

        if (opponent.trait == Traits.Beast || this.trait == Traits.Beast) {
            // Only a deathmatch if the beast meaningfully outlevels the cultivator
            Character _beast   = (this.trait == Traits.Beast) ? this : opponent;
            Character _fighter = (this.trait == Traits.Beast) ? opponent : this;
            if (_beast.trait != Traits.Beast) return true; // both beasts — always lethal
            return _beast.tempLvl > _fighter.tempLvl + 5;
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
        if (this.player == 1 || opponent.player == 1) deathChance = Math.min(100, deathChance + 12 * GameDriver.difficulty); // Hard / Brutal: lost fights involving the player are deadlier
        if (deathChance >= (rand.nextInt(100) + 1)){
            return true;
        }
        else {
            return false;
        }
    }

    public void winCheck(Character loser) { winCheck(loser, true); }

    /** `lethal` = the fight's death roll (deathCheck: by relationship, 1% strangers .. 100% wanted; beasts by level gap). A cultivator who loses a
     *  non-lethal fight with another cultivator survives, hurt; a human winner can still choose to kill. */
    public void winCheck(Character loser, boolean lethal) {
        gainInspiration(loser, true); loser.gainInspiration(this, false); // the Innovative learn from every fight
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
            // Non-lethal beast encounter: loser escapes injured if beast didn't meaningfully outlevel them
            // ...except that a wild beast now and then (1 in BEAST_KILL_ODDS) finishes the cultivator it beat: that is how Monsters are born (a measured 3,000-person decade had 9,000 such wins and no kill at all)
            if ((trait == Traits.Beast || trait == Traits.Monster) && !ambusher && tempLvl <= loser.tempLvl + 5
                    && !(trait == Traits.Beast && masterId < 0 && loser.trait != Traits.Beast && loser.trait != Traits.Monster && rand.nextInt(BEAST_KILL_ODDS) == 0)) {
                loser.cultivation = Math.max(1, (long)(loser.cultivation * 0.7));
                FightReport.beastEscape(loser);
                return;
            }
            // A human player decides whether to finish a defeated cultivator (spare = simply don't kill; nothing else changes)
            boolean humanLoser = loser.trait != Traits.Beast && loser.trait != Traits.Monster;
            if (humanLoser && loser.childSafe()) { // nobody under 18 dies: the beaten child is hurt and left
                loser.cultivation = Math.max(1, (long) (loser.cultivation * 0.7));
                if (loser.player == 1 && skipping <= 0 && !FightReport.active()) System.out.println("You were beaten by " + this + " but left alive, injured.");
                FightReport.injured(loser);
                return;
            }
            if (humanLoser && loser.fatedTo > 0 && id != loser.fatedTo - 1) { // a person of a story, beaten by somebody else than the human it belongs to: hurt, never killed
                loser.cultivation = Math.max(1, (long) (loser.cultivation * 0.7));
                FightReport.injured(loser);
                return;
            }
            if (humanLoser && fatedTo > 0 && loser.id == fatedTo - 1 && loser.story != null && loser.story.difficulty == 0) { // the foe of a hero on the Story difficulty leaves them alive
                loser.cultivation = Math.max(1, (long) (loser.cultivation * 0.7));
                if (loser.player == 1 && skipping <= 0 && !FightReport.active()) System.out.println("You were beaten by " + this + " but left alive, injured: this was not the end of your story.");
                FightReport.injured(loser);
                return;
            }
            if (player == 1 && humanLoser && loser.fatedTo == id + 1) Chapters.beaten(this, loser);
            boolean chosenKill = false;
            if (player == 1 && humanLoser && !loser.dead) {
                if (!decideKill(loser)) {
                    if (!FightReport.active()) System.out.println("You spared " + loser + ".");
                    FightReport.spared(this, loser);
                    FightReport.penalty(this, loser); // a spared enemy still pays for the loss when there is bad blood between you
                    return;
                }
                chosenKill = true;
            }
            // The death roll decides whether a lost fight between cultivators is fatal (it used to be ignored: every lost fight killed)
            if (!lethal && !chosenKill && humanLoser && trait != Traits.Beast && trait != Traits.Monster) {
                loser.cultivation = Math.max(1, (long) (loser.cultivation * 0.7)); // injured until the next level-up recalculates it
                if (loser.player == 1 && skipping <= 0 && !FightReport.active()) System.out.println("You were beaten by " + this + " but left alive, injured.");
                FightReport.injured(loser);
                FightReport.penalty(this, loser); // feud fights cost the loser 10% of their spirit stones
                return;
            }
            loser.dead(this);
            // (a Beast that kills a cultivator awakens as a Monster inside dead(), whatever the way it killed)
            // Evil or Beast destroys undefended city/sect (20% chance per eligible kill)
            if ((affiliationNum == -1 || trait == Traits.Beast) && !ambusher
                    && lostAt != null && !"Originis".equals(lostAt.name)
                    && (lostAt.city || lostAt.sect != null)
                    && (lostAt.arrayBag == null || lostAt.arrayBag.isEmpty())
                    && Settlements.mayDestroy(lostAt) // the world keeps at least 25 cities and 25 sects
                    && rand.nextInt(lostAt.city ? Settlements.RAZE_CITY_ODDS : Settlements.RAZE_SECT_ODDS) == 0) {
                destroyCity(lostAt);
            }
        }
    }
    /** Estimate of a fight against `opp` (no side effects beyond the usual gear optimisation a fight would do anyway). */
    static class FightPreview {
        Character me, opp;
        int myPower, oppPower, winChance, deathChance, myFlee, oppFlee;
        boolean deathmatch, arrowBoost;
        String oneLine() {
            return "Win chance ~" + winChance + "% (power " + myPower + " vs " + oppPower + "), lethal " + (deathChance >= 100 ? "for sure" : "~" + deathChance + "%")
                    + ", your escape odds ~" + myFlee + "%";
        }
        String detail() {
            StringBuilder sb = new StringBuilder("=== Fight preview: " + me.name + " vs " + opp.name + " " + opp.lastName + " ===\n");
            sb.append(String.format("%-18s power %3d   Lv.%d (Major %d)  %s%n", "You", myPower, me.lvl, me.majorLvl, me.affiliation));
            sb.append(String.format("%-18s power %3d   Lv.%d (Major %d)  %s  %s%n", opp.name, oppPower, opp.lvl, opp.majorLvl, opp.affiliation, opp.getTrait()));
            sb.append("Power = (level + armour + weapon + both techniques) / 5, plus trait bonuses").append(arrowBoost ? "; the defender's array adds a bonus here" : "").append(".\n\n");
            sb.append("Your chance to win:        ~").append(winChance).append("%\n");
            sb.append("Chance the fight is lethal: ").append(deathChance >= 100 ? "certain" : "~" + deathChance + "%").append(deathmatch ? "  (a beast this strong kills)" : "").append("\n");
            sb.append("Your chance to escape:     ~").append(myFlee).append("%\n");
            sb.append("Their chance to escape if they are losing: ~").append(oppFlee).append("%\n");
            Relationship r = me.relationshipList.contains(opp.id);
            sb.append("Relationship: ").append(r == null ? "none" : String.valueOf(r.value)).append("   Their caution: ").append(opp.caution).append("\n");
            sb.append("If you win you choose to kill or spare.\n");
            return sb.toString();
        }
    }

    public FightPreview previewFight(Character opp, boolean iAmDefender) {
        FightPreview p = new FightPreview();
        p.me = this; p.opp = opp;
        tempLvlCheck(); opp.tempLvlCheck();
        int mine = tempLvl, theirs = opp.tempLvl;
        Character defender = iAmDefender ? this : opp;
        if (defender.trait != Traits.Beast && defender.trait != Traits.Monster && defender.location != null && defender.location.arrayBag != null && !defender.location.arrayBag.isEmpty()) {
            int best = 0;
            for (Item a : defender.location.arrayBag) best = Math.max(best, a.lvl);
            if (iAmDefender) mine += best / 10 + 1; else theirs += best / 10 + 1;
            p.arrowBoost = true;
        }
        p.myPower = mine; p.oppPower = theirs;
        int win = 50 - 15 * (theirs - mine);
        if (trait == Traits.Lucky) win += 45;
        if (opp.trait == Traits.Lucky) win -= 45;
        p.winChance = Math.max(0, Math.min(100, win));
        if (trait == Traits.Beast || opp.trait == Traits.Beast) {
            Character beast = trait == Traits.Beast ? this : opp, fighter = trait == Traits.Beast ? opp : this;
            p.deathmatch = beast.trait != Traits.Beast || beast.tempLvl > fighter.tempLvl + 5;
            p.deathChance = beast.trait != Traits.Beast ? 100 : (beast.tempLvl > fighter.tempLvl + 5 ? 100 : 0);
        } else {
            Relationship r = relationshipList.contains(opp.id);
            int v = r == null ? 0 : r.value;
            p.deathChance = v <= -4 ? 100 : v == -3 ? 75 : v == -2 ? 25 : v == -1 ? 5 : v == 0 ? 1 : 0;
        }
        int n = 300, mf = 0, of = 0;
        for (int i = 0; i < n; i++) { if (trait != Traits.Beast && fleeRoll(opp)) mf++; if (opp.trait != Traits.Beast && opp.fleeRoll(this)) of++; }
        p.myFlee = mf * 100 / n; p.oppFlee = of * 100 / n;
        return p;
    }

    // Kill or spare a defeated cultivator: a MANUAL player is asked; AUTO/HYBRID follow the kill policy. True = kill.
    private boolean decideKill(Character loser) {
        PlayerControl p = pc();
        boolean byPolicy;
        switch (p.killPolicy) {
            case 0:  byPolicy = false; break;
            case 1:  byPolicy = "Evil".equals(loser.affiliation); break;
            case 2:  { Relationship r = relationshipList.contains(loser.id); byPolicy = r != null && r.value <= -1; break; }
            default: byPolicy = true; break;
        }
        int a = DecisionBroker.ask(this, "You defeated " + loser + " (Lv." + loser.lvl + ", " + loser.affiliation + "). Kill or spare?",
                new String[]{"Kill", "Spare"}, byPolicy ? 0 : 1);
        return a == 0 && !loser.dead;
    }

    // A player being challenged may slip away instead: MANUAL is asked, otherwise the duel policy decides. True = the fight goes ahead.
    boolean acceptChallenge(Character challenger) { // package-private: SectPolitics asks a human target before an intrigue turns into a fight
        PlayerControl p = pc();
        // Strangers, passing evildoers and people who merely dislike you may only challenge a few times a turn (Settings.maxRandomChallenges);
        // rivals, arch-enemies and anything between parties or sects at war always get through.
        if (!mustFace(challenger)) {
            if (p.limitedWeek != GameDriver.time) { p.limitedWeek = GameDriver.time; p.limitedCount = 0; }
            if (p.limitedCount >= Settings.maxRandomChallenges) {
                if (p.limitedNoticeWeek != GameDriver.time) { p.limitedNoticeWeek = GameDriver.time; System.out.println("Other strangers eye you, but keep their distance this week."); }
                return false;
            }
            p.limitedCount++;
        }
        p.attackedThisTick = true;
        int a = DecisionBroker.ask(this, challenger + " (Lv." + challenger.lvl + ", " + challenger.affiliation + ") challenges you!\n" + previewFight(challenger, true).oneLine(),
                new String[]{"Fight", "Slip away"}, p.duelPolicy ? 0 : 1, challenger, "challenge");
        if (a == 0) return true;
        if (fleeRoll(challenger)) {
            System.out.println("You slipped away from " + challenger + ".");
            return false;
        }
        System.out.println("You couldn't get away from " + challenger + "!");
        return true;
    }

    /** A challenge that is never turned away: from a rival or arch-enemy, from the same party's quarrel, or between sects at war. */
    private boolean mustFace(Character challenger) {
        Relationship r = relationshipList.contains(challenger.id);
        if (r != null && r.value <= -2) return true;
        Relationship back = challenger.relationshipList.contains(id);
        if (back != null && back.value <= -2) return true;
        if (partyLeaderId >= 0 && challenger.partyLeaderId == partyLeaderId) return true;
        if (sect != null && challenger.sect != null && sect != challenger.sect && (sect.warTarget == challenger.sect || challenger.sect.warTarget == sect)) return true;
        if (challenger.partyLeaderId >= 0 && (challenger.partyLeaderId == id || partyLeaderId == challenger.partyLeaderId)) return true;
        return false;
    }

    //If true, player will flee
    public boolean fleeCheck(Character opponent) {
        if (this.trait == Traits.Beast) {
            return false;
        }
        if (player == 1) { // a human decides (MANUAL) or their flee policy does (AUTO/HYBRID); fleeing can still fail
            PlayerControl p = pc();
            int a = DecisionBroker.ask(this, "You are outmatched by " + opponent + " (Lv." + opponent.lvl + "). Try to flee?\n" + previewFight(opponent, false).oneLine(),
                    new String[]{"Flee", "Fight"}, p.fleePolicy ? 0 : 1);
            if (a != 0) { FightReport.fledNot(this); return false; }
            if (fleeRoll(opponent)) { if (!FightReport.active()) System.out.println("You escaped from " + opponent + "."); FightReport.fleeAttempt(this, opponent, true); return true; }
            if (!FightReport.active()) System.out.println("You failed to escape from " + opponent + "!");
            FightReport.fleeAttempt(this, opponent, false);
            return false;
        }
        boolean got = fleeRoll(opponent);
        FightReport.fleeAttempt(this, opponent, got);
        return got;
    }

    private boolean fleeRoll(Character opponent) {
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

    /** What the sheets show as the profession level: a Leader's 50 only means that the founding is done. */
    String professionShown() { return trait == Traits.Leader && professionLvl >= 50 ? "-" : String.valueOf(professionLvl); }
    boolean leaderFounded() { return trait == Traits.Leader && professionLvl >= 50; }

    /** Tells the humans it concerns (the lord, the sect, shop owners, anyone standing there) that a city or sect was destroyed, and records it. */
    private void announceDestruction(Area area) {
        final boolean wasCity = area.city;
        final Organization wasSect = area.sect;
        final Character lord = area.cityOwner;
        final String what = (wasCity ? "The city of " : "The sect at ") + area.name;
        final String by = (trait == Traits.Beast ? "a beast, " : trait == Traits.Monster ? "a Monster, " : "") + name + " " + lastName;
        Chronicle.add(wasCity ? "city" : "sect", what + " was destroyed by " + by + ".", id);
        for (Character h : new ArrayList<>(Notifications.humans())) {
            if (h == null || h.dead || h == this) continue;
            boolean mine = h == lord || (wasSect != null && h.sect == wasSect);
            if (!mine && h.ownedShops != null) for (Shop s : h.ownedShops) if (s.location == area) mine = true;
            if (mine || h.location == area) Notifications.post("danger", what + " has been destroyed by " + by + (h == lord ? " - your city is gone." : mine ? " - it was yours or your sect's." : "."), id, h.id);
        }
    }

    public void destroyCity(Area area) {
        announceDestruction(area);
        if (area.city) Settlements.cityCount.decrementAndGet();
        if (area.sect != null && !area.sect.palace) Settlements.sectCount.decrementAndGet();
        // Plunder all area bags
        // (stackables merge into existing stacks — unmerged duplicates made gathering/crafting credit every copy)
        if (area.backpack != null)    { for (Item i : area.backpack)    addToBag(backpack, i);    area.backpack.clear();    }
        if (area.materialBag != null) { for (Item i : area.materialBag) addToBag(materialBag, i); area.materialBag.clear(); }
        if (area.pillBag != null)     { for (Item i : area.pillBag)     addToBag(pillBag, i);     area.pillBag.clear();     }
        if (area.arrayBag != null)    { arrayBag.addAll(area.arrayBag);                           area.arrayBag.clear();    }
        // Blood from population massacre — proportional to city size
        int bloodQty = Math.max(1, (int)(area.population / 100));
        addToBag(materialBag, new Item(Item.ItemType.Blood, bloodQty));
        // Cultivation bonus — 10 extra cultivate() calls from the battle aura
        for (int i = 0; i < 10; i++) cultivate();
        // Potential gain
        if (potential < 100 && rand.nextInt(10) == 0) givePotential(1, "The carnage expanded your limits!");
        // Plunder coin from city coffers
        ss += area.population;
        convertCurrency();
        // Population devastated — 90% wiped out
        area.population = Math.max(1, (int)(area.population * 0.1));
        // Dissolve sect if present
        if (area.sect != null && !area.sect.palace) { // the Palace of the Oldest Order cannot be razed
            final Organization destroyedSect = area.sect;
            area.sect = null; // (the tile loses its sect now; the members, far and wide, are told when a parallel phase is over)
            Map.later(() -> {
            // The founder/master isn't always in `members`; without this they kept a pointer to a sect that no longer exists
            ArrayList<Character> everyone = new ArrayList<>(destroyedSect.members);
            if (destroyedSect.master != null) everyone.add(destroyedSect.master);
            if (destroyedSect.founder != null) everyone.add(destroyedSect.founder);
            for (Character member : everyone) {
                if (member != null && !member.dead && member.sect == destroyedSect) {
                    member.sect = null;
                    member.sectMission = null;
                    member.sectMissionBonus = 0;
                }
            }
            destroyedSect.members.clear();
            destroyedSect.memberRanks.clear();
            destroyedSect.master = null;
            if (destroyedSect.warTarget != null) destroyedSect.endWar(destroyedSect.warTarget);
            for (Organization ally : new ArrayList<>(destroyedSect.allies)) ally.allies.remove(destroyedSect);
            destroyedSect.allies.clear();
            });
        }
        // Destroy city — remove infrastructure
        if (area.city) {
            area.city = false;
            area.cityOwner = null;
            area.restoreName();
            for (Shop lostShop : new ArrayList<>(area.shops)) {
                final Shop lost = lostShop;
                if (lost.owner != null) Map.later(() -> lost.owner.ownedShops.remove(lost)); // otherwise the merchant's slot stayed taken by a shop that no longer exists (the merchant may be far away)
            }
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
        tempLvl += blessing; // a soldier's blessing: +5 to the fighting level per soldier ancestor

        if (this.trait == Traits.Cheater) {
            tempLvl += 5;
        }

        if (this.trait == Traits.IronBody) {
            tempLvl += majorLvl / 2 + 1;
        }

        if (fx != null || beastIds != null) tempLvl = Math.max(1, tempLvl + Life.powerMod(this) + Taming.bonus(this)); // wine, poison, companion beasts

        if (exhaustionPenalty > 0) {
            tempLvl = Math.max(1, tempLvl - exhaustionPenalty);
        }

    }

    // Returns true if the technique burned out this character's lifespan. The caller then records the kill via dead(opponent);
    // calling dead() here made that follow-up a no-op (no kill credit, loot, revenge quests).
    public boolean forbiddenCheck() {
        forbiddenDeclined = false;
        if (trait == Traits.Beast) {
            return false;
        }
        if (player == 1) { // a human opts in (MANUAL), or their forbidden-technique policy decides
            int a = DecisionBroker.ask(this, "The fight is deadly. Use the Forbidden Technique? (burns " + totalAgeGained / 10
                    + " years of lifespan and can kill you)", new String[]{"Use it", "Don't"}, pc().forbiddenPolicy ? 0 : 1);
            if (a != 0) { forbiddenDeclined = true; FightReport.forbidden(this, 0); return false; }
        }
        if (age + totalAgeGained/10 > maxAge) {
            tempLvl += Math.sqrt(potential);
            maxAge -= totalAgeGained / 10;
            deathCause = "lifespan burned out by Forbidden Technique";
            FightReport.forbidden(this, 2);
            return true;
        }
        else {
            tempLvl += Math.sqrt(potential);
            maxAge -= totalAgeGained / 10;
            FightReport.forbidden(this, 1);
            // Rare potential gain for surviving a forbidden technique (2% chance)
            if (potential < 100 && rand.nextInt(50) == 0) givePotential(1, "Surviving the forbidden technique expanded your limits!");
            return false;
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
            if (player > 0 || opponent.player > 0) System.out.println("" + equippedWeapon + " Weapon has broken!");
            final Item brokenW = equippedWeapon;
            backpack.removeIf(i -> i == brokenW); // a deconstructed item must not linger in the backpack (null name breaks sell/merge code)
            equippedWeapon.deconstructor();
            this.equippedWeapon = null;
        }
        if (breakArmor) {
            if (player > 0 || opponent.player > 0) System.out.println("" + equippedClothing + " Armor has broken!");
            final Item brokenC = equippedClothing;
            backpack.removeIf(i -> i == brokenC);
            equippedClothing.deconstructor();
            this.equippedClothing = null;
        }
        if (oppBreakWeapon) {
            if (player > 0 || opponent.player > 0) System.out.println("" + opponent.equippedWeapon + " Weapon has broken!");
            final Item oppBrokenW = opponent.equippedWeapon;
            opponent.backpack.removeIf(i -> i == oppBrokenW);
            opponent.equippedWeapon.deconstructor();
            opponent.equippedWeapon = null;
        }
        if (oppBreakArmor) {
            if (player > 0 || opponent.player > 0) System.out.println("" + opponent.equippedClothing + " Armor has broken!");
            final Item oppBrokenC = opponent.equippedClothing;
            opponent.backpack.removeIf(i -> i == oppBrokenC);
            opponent.equippedClothing.deconstructor();
            opponent.equippedClothing = null;
        }

    }

    public boolean durabilityCheckUtil(int toBreak, int breaker) {
        int intBreaker = (int)(breaker/10);
        int intToBreak = (int)(toBreak/10);
        int breakChance = (int)(Math.pow(2, (intBreaker - intToBreak)) * 5000); // half the old chance: gear lasts, so better gear is worth making

        if (rand.nextInt(100000) + 1 <= breakChance - (450 * intToBreak)) {
            return true;
        }
        return false;
    }

    /** The display name of a trait (same words as getTrait for the character's own). */
    static String traitName(Traits t) {
        switch (t) {
            case IronBody: return "Iron Body"; case SpiritVein: return "Spirit Vein"; case HeavenlyRoot: return "Heavenly Root"; case Tamer: return "Beast Tamer";
            default: return t.name();
        }
    }
    String getTrait(Traits t) { return traitName(t); }

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
            case Tamer:
                return "Beast Tamer";
            case Poisoner:
                return "Poisoner";
            case Drunken:
                return "Drunken";
            case Chef:
                return "Chef";
            case Musician:
                return "Musician";
            case Beast:
                return "Beast";
            case Monster:
                return "Monster";
            default:
                return "Unknown Trait";
        }
    }
    public void updateRepTitle() {
        if (wanderingImmortal) { repTitle = "Wandering Immortal"; return; }
        repTitle = Naming.title(this); // (the yearly Naming.assignTitles limits each kind to its ten best)
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
