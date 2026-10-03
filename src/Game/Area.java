package Game;

import java.util.ArrayList;
import java.util.List;

import static Game.GameDriver.*;
import static Game.Dictionary.*;
import static Game.Map.*;

public class Area implements java.io.Serializable {
    static final long serialVersionUID = 1L;
    enum DungeonType { LOST_RUINS, BEAST_LAIR, MARTIAL_TRIAL, HAUNTED_GROUNDS, CELESTIAL_TRIAL }

    static int uindex = 1;
    int index;
    String name = "";
    String geoName = null; // the tile's geographic name once it has been renamed (a city or a dungeon); pictures are chosen from this one
    int x;
    int y;
    boolean city = false;
    Character cityOwner;
    ArrayList<Item> backpack;
    ArrayList<Item> materialBag;
    ArrayList<Item> pillBag;
    ArrayList<Item> goodsBag = new ArrayList<>(); // the city market's food, wine and poison (see Life)
    ArrayList<Item> arrayBag;
    ArrayList<Library> libraries = new ArrayList<>();
    Organization sect;
    boolean hasHeaven() { return heavenlyTechnique != null || heavenItem != null; }
    String heavenLabel() { return heavenlyTechnique != null ? heavenlyTechnique.name : heavenItem != null ? heavenItem.name + (heavenItem.quantity > 1 ? " x" + heavenItem.quantity : "") : ""; }
    ArrayList<Item> auction() { if (auctionBag == null) auctionBag = new ArrayList<>(); return auctionBag; }
    String biomeM = "";
    String biomem = "";
    float elev = 0.5f, moist = 0.5f, temp = 0.5f; // terrain fields of the tile (BiomeGen): elevation, moisture, temperature, 0-1
    boolean river = false;
    int lvl;
    int baseLvl = 0;
    boolean hotspot = false;
    DungeonType dungeonType = null;
    int highestLvl = 1;
    int population;
    int monsters;
    int herbMaterials;
    int metalMaterials;
    int celestialTrialTimer = 0;
    Technique heavenlyTechnique = null;   // a Heaven-Sent technique lying here (see Treasure)
    Item heavenItem = null;               // a Heaven-Sent item lying here
    ArrayList<Item> auctionBag;           // the lots on this city's auction block (null until the first; see Auctions)
    int auctionSince = 0;                 // the week the first waiting lot arrived (0 = nothing waits)
    int dungeonCharges = 0;
    long cityTreasury = 0;
    boolean spiritualVein = false;

    ArrayList<Shop> shops = new ArrayList<>();
    ArrayList<Quest> bounties = new ArrayList<>();
    ArrayList<Character> characters = new ArrayList<>();
    transient ArrayList<Character> deadCharacters = new ArrayList<>();

    // City level 1-10, scaling with the coffers exactly like item prices (price = itemLvl^2.5 * 10, one city level per 10 item levels).
    // Level 1 needs no money; level 2 ~18k, 3 ~49k, 5 ~177k, 10 ~1M.
    public int cityLevel() {
        if (!city) return 0;
        double itemLvl = Math.pow(Math.max(0L, cityTreasury) / 10.0, 0.4);
        return Math.max(1, Math.min(10, (int) (itemLvl / 10)));
    }

    boolean crowdHotspot = false;   // overcrowded: beasts ambush the cultivators here until the tile thins out
    int crowdSurge = 0;             // level surge applied while crowdHotspot is set

    public int crowdCapacity() { return city ? CROWD_CAPACITY_CITY : CROWD_CAPACITY; }

    // Called every tick. An over-capacity tile becomes a hotspot (level surge) and beasts start ambushing its cultivators;
    // it returns to normal once the crowd drops under 80% of capacity.
    public boolean isOriginis() { return "Originis".equals(name); }

    /** Originis and the eight tiles around it, where the first generation is spread out at the start. */
    public boolean isStartZone() { return Math.abs(x - range / 2) <= 1 && Math.abs(y - range / 2) <= 1; }

    private void endCrowdHotspot() {
        crowdHotspot = false;
        lvl = Math.max(1, lvl - crowdSurge);
        crowdSurge = 0;
        if (!hotspotAreas.contains(this)) hotspot = false;
    }

    public void crowdCheck(int year) {
        // The founding city can never be a crowd hotspot (also lifts one an older save may already carry)
        if (isOriginis()) { if (crowdHotspot) endCrowdHotspot(); return; }
        if (year <= CROWD_GRACE_YEARS) return;
        int n = characters.size();
        int cap = crowdCapacity();
        if (n > cap) {
            if (!crowdHotspot) {
                crowdHotspot = true;
                hotspot = true;
                crowdSurge = CROWD_SURGE;
                lvl += crowdSurge;
                System.out.println("Overcrowding turns " + name + " into a hotspot! Beasts stir among its " + n + " cultivators.");
            }
            crowdAmbush(n - cap);
        } else if (crowdHotspot && n <= cap * CROWD_CLEAR_RATIO) {
            endCrowdHotspot();
        }
    }

    // Beasts strike immediately, ignoring level; any cultivator they defeat dies on the spot (see Character.winCheck).
    void crowdAmbush(int excess) {
        if (monsters <= 0 || characters.isEmpty()) return;
        if (arrayBag != null && !arrayBag.isEmpty()) return; // arrays keep beasts out
        int attacks = Math.max(1, Math.min(CROWD_MAX_ATTACKS, (int) (excess * CROWD_ATTACK_RATE + 0.5)));
        for (int i = 0; i < attacks && monsters > 0 && !characters.isEmpty(); i++) {
            Character victim = null;
            for (int tries = 0; tries < 8 && victim == null; tries++) {
                Character c = characters.get(rand.nextInt(characters.size()));
                if (c != null && !c.dead && c.born && c.player < 1
                        && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster) victim = c;
            }
            if (victim == null) return;
            monsters--;
            Character beast = new Character(this, victim);
            beast.ambusher = true;
            beast.battle(victim);
        }
    }

    static final int MAX_MONSTERS = 50_000_000;
    static final int MAX_MATERIALS = 1_000_000_000;
    static final int MAX_POPULATION = 100_000_000;

    // Non-cultivating population ceiling: 100k at city level 1, 400k at level 2 ... 10M at level 10 (wild tiles use level 1)
    public long populationCap() {
        long l = city ? cityLevel() : 1;
        return Math.min(MAX_POPULATION, 100_000L * l * l);
    }

    // Money paid to "the city": goes to the living lord, otherwise into the city treasury (never to a dead owner, never lost)
    public void receivePayment(long amount) {
        if (amount <= 0) return;
        if (cityOwner != null && !cityOwner.dead) { cityOwner.ss += amount; cityOwner.convertCurrency(); }
        else cityTreasury += amount;
    }

    // Shops a city can host: 3 per city level
    public int maxShopSlots() { return city ? cityLevel() * 3 : 0; }

    // Recent corpses only (Lucky characters loot them); unbounded growth was a memory leak and made the loot scan O(n)
    public void addDead(Character c) {
        if (deadCharacters == null) deadCharacters = new ArrayList<>();
        deadCharacters.add(c);
        if (deadCharacters.size() > 50) deadCharacters.remove(0);
    }

    public void addChar(Character c) {
        c.areaIdx = characters.size();
        characters.add(c);
    }
    public void removeChar(Character c) {
        int idx = c.areaIdx;
        if (idx < 0 || idx >= characters.size()) return;
        int last = characters.size() - 1;
        if (idx != last) {
            Character swap = characters.get(last);
            characters.set(idx, swap);
            swap.areaIdx = idx;
        }
        characters.remove(last);
        c.areaIdx = -1;
    }

    // Tournament registration
    ArrayList<Character> tournamentRegistrants = new ArrayList<>();
    int tournamentTimer = 0;
    String tournamentName; // e.g. "Jade Dragon Tournament of Year 214"
    int tournamentMinMajor = 0;
    int tournamentMaxMajor = 0;
    Item tournamentPrize = null;
    Character tournamentOrganizer = null;

    String loreSite = null; // id of the lore place this tile is (see LoreSites); null for ordinary tiles and in old saves

    /** An unregistered stand-in at (gx, gy): only its position is used, to build the tile next to it with Area(Area, char) (see LoreSites.ensureTile). */
    private Area(int gx, int gy, boolean stand) { x = gx; y = gy; }
    static Area ghost(int gx, int gy) { return new Area(gx, gy, true); }

    public Area() {
        name = "Originis";
        index = uindex++;
        x = range / 2;
        y = range / 2;
        city = true;
        biomeM = "Forest";
        biomem = "Forest";
        elev = 0.56f; moist = 0.74f; temp = 0.5f;
        lvl = 1;
        baseLvl = lvl;
        monsters = rand.nextInt(10000000);
        herbMaterials = rand.nextInt(1000000000);
        metalMaterials = rand.nextInt(1000000000);
        population = 100_000 + rand.nextInt(900_000); // the capital starts as a large but bounded city
        backpack = new ArrayList<>();
        materialBag = new ArrayList<>();
        pillBag = new ArrayList<>();
        arrayBag = new ArrayList<>();
        libraries.add(new Library(this));
        addCreatedArea(this);
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

        // Sets the Biome from the world's coherent noise fields (see BiomeGen): it depends only on this tile's position and the world seed
        BiomeGen.apply(this);

        // Indexing the Area
        index = uindex++;

        // Naming The Area (see Naming.place: number / possessive, biome-aware adjective, plural noun, "of X")
        name = Naming.place(biomem, elev, moist, river);
        int _cx = range / 2, _cy = range / 2;
        int _dx = x - _cx, _dy = y - _cy;
        // Each quadrant has a different level ceiling (NW=20, NE=40, SW=60, SE=80)
        float _qMax = (_dx < 0 && _dy < 0) ? 20f :
                      (_dx >= 0 && _dy < 0) ? 40f :
                      (_dx < 0  && _dy >= 0) ? 60f : 80f;
        float _xNorm = (_cx > 0) ? Math.abs(_dx) / (float)_cx : 0f;
        float _yNorm = (_cy > 0) ? Math.abs(_dy) / (float)_cy : 0f;
        int _distLvl = (int)(_qMax * (_xNorm + _yNorm) / 2f);
        lvl = Math.min(100, Math.max(currentCharacter.lvl, 1 + _distLvl + rand.nextInt(5)));
        baseLvl = lvl;
        if (rand.nextInt(20) == 0) {
            // CELESTIAL_TRIAL is the last enum value and only exists as a temporary yearly event; picked here it never despawned
            dungeonType = DungeonType.values()[rand.nextInt(DungeonType.values().length - 1)];
            if (dungeonType == DungeonType.LOST_RUINS) {
                dungeonCharges = Math.max(3, lvl / 2 + rand.nextInt(Math.max(1, lvl / 2 + 1)));
            }
            geoName = name;
            name = Naming.dungeon(dungeonType, lvl);
        }
        monsters = rand.nextInt((startingPopulation * 10) - startingPopulation + 1) + startingPopulation;
        herbMaterials = rand.nextInt((startingPopulation * 10000) - startingPopulation + 1) + startingPopulation;
        metalMaterials = rand.nextInt((startingPopulation * 1000) - startingPopulation + 1) + startingPopulation;
        // resources follow the terrain: herbs grow where it is wet, metal sits in high ground, spirit veins run through the mountains
        herbMaterials = (int) Math.min((long) Integer.MAX_VALUE / 4, Math.max(1L, (long) (herbMaterials * (0.4 + 1.2 * moist))));
        metalMaterials = (int) Math.min((long) Integer.MAX_VALUE / 4, Math.max(1L, (long) (metalMaterials * (0.3 + 1.6 * elev))));
        if (rand.nextInt(100) == 0 || (elev > 0.74 && rand.nextInt(30) == 0)) spiritualVein = true;
        backpack = new ArrayList<>();
        materialBag = new ArrayList<>();
        pillBag = new ArrayList<>();
        arrayBag = new ArrayList<>();
        addCreatedArea(this);
        Lore.onTileCreated(this); // a tile that holds one of the lore's places takes its name
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (deadCharacters == null) deadCharacters = new ArrayList<>();
        if (characters == null) characters = new ArrayList<>();
        // Fields added after older saves were written deserialize as null
        if (shops == null) shops = new ArrayList<>();
        if (bounties == null) bounties = new ArrayList<>();
        if (goodsBag == null) goodsBag = new ArrayList<>();
        if (libraries == null) libraries = new ArrayList<>();
        if (tournamentRegistrants == null) tournamentRegistrants = new ArrayList<>();
    }

    // Multiplicative growth capped at `cap`. A count of 0 stays 0 (emptied tiles/lairs stay empty on purpose); small positive counts
    // always gain at least 1 when growing (plain truncation froze anything under ~100 in place).
    private static int grow(int v, double mult, long cap) {
        if (v <= 0) return 0;
        long g = (long) (v * mult);
        if (mult > 1 && g <= v) g = (long) v + 1;
        return (int) Math.min(cap, Math.max(0L, g));
    }

    public void age() {
        double herbMult = (currentSeason == 1) ? 1.03 : (currentSeason == 3) ? 0.98 : 1.01;
        double monsterMult = (currentSeason == 3) ? 0.995 : 1.01;
        // Growth is capped well below Integer.MAX_VALUE so counters can never saturate or wrap
        monsters = grow(monsters, monsterMult, MAX_MONSTERS);
        herbMaterials = grow(herbMaterials, herbMult, MAX_MATERIALS);
        metalMaterials = grow(metalMaterials, 1.01, MAX_MATERIALS);
        if (population > 0) {
            // Logistic-style growth: 0.1%/tick (min +1) toward a ceiling that rises with city level
            long grown = Math.max((long) population + 1, (long) (population * 1.001));
            population = (int) Math.min(populationCap(), grown);
        }
        if (population > 0 && rand.nextInt(10) == 0) {
            Character temp = new Character(this);
            Starts.prehistoryOne(temp); // city-born cultivators arrive at 18 with the level their potential earned since 8
            if (rand.nextInt(2) == 0 && sect != null) {
                sect.join(temp);
            }
            tempCharacters.add(temp);
        }
    }

    public void runTournament() {
        if (Lore.runConvocation(this)) return; // the Convocation of the First Gate at Originis is judged by the Palace
        if (tournamentRegistrants.size() < 2) {
            if (tournamentPrize != null && tournamentOrganizer != null)
                tournamentOrganizer.backpack.add(tournamentPrize);
            tournamentPrize = null; tournamentOrganizer = null;
            tournamentRegistrants.clear();
            return;
        }
        java.util.Collections.shuffle(tournamentRegistrants, rand);
        java.util.ArrayList<Character> pool = new java.util.ArrayList<>(tournamentRegistrants);
        while (pool.size() > 1) {
            java.util.ArrayList<Character> next = new java.util.ArrayList<>();
            for (int _i = 0; _i + 1 < pool.size(); _i += 2) {
                Character a = pool.get(_i), b = pool.get(_i + 1);
                if (a.dead) { next.add(b); continue; }
                if (b.dead) { next.add(a); continue; }
                a.tempLvlCheck(); b.tempLvlCheck();
                int wc = Math.max(5, Math.min(95, 50 - 15 * (b.tempLvl - a.tempLvl)));
                if (a.trait == Character.Traits.Lucky) wc = Math.min(95, wc + 45);
                if (b.trait == Character.Traits.Lucky) wc = Math.max(5, wc - 45);
                next.add(rand.nextInt(100) < wc ? a : b);
            }
            if (pool.size() % 2 == 1) next.add(pool.get(pool.size() - 1));
            pool = next;
        }
        Character winner = pool.isEmpty() ? null : pool.get(0);
        if (winner != null && !winner.dead && tournamentPrize != null) {
            winner.backpack.add(tournamentPrize);
            winner.equip();
        }
        boolean hasPlayer = Map.characters.stream().anyMatch(c -> c != null && c.player > 0 && c.skipping <= 0);
        if (hasPlayer && winner != null)
            System.out.println("=== Tournament at " + name + (tournamentName == null ? "" : " (" + tournamentName + ")") + ": " + winner + " wins " + tournamentPrize + "! ===");
        tournamentRegistrants.clear(); tournamentPrize = null; tournamentOrganizer = null;
    }

    public String toString() {
        return (" is in the " + name + " at x: " + x * 100 + ", y: " + y * 100);
        //return (currentCharacter + " is in the " + name + " at x: " + x * 100 + ", y: " + y * 100 + " Lvl: " + lvl + " animalCount: " + animals + " monsterCount: " + monsters + " spiritStoneCount: " + spiritStoneMaterials + " herbMaterialCount: " + herbMaterials + " metalMaterialCount: " + metalMaterials);
    }
}

