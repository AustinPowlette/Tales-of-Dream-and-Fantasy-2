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
    ArrayList<Item> goodsBag = new SafeList<>(); // the city market's food, wine and poison (see Life)
    ArrayList<Item> arrayBag;
    ArrayList<Library> libraries = new SafeList<>();
    Organization sect;
    boolean hasHeaven() { return heavenlyTechnique != null || heavenItem != null; }
    String heavenLabel() { return heavenlyTechnique != null ? heavenlyTechnique.name : heavenItem != null ? heavenItem.name + (heavenItem.quantity > 1 ? " x" + heavenItem.quantity : "") : ""; }
    ArrayList<Item> auction() { if (auctionBag == null) auctionBag = new SafeList<>(); return auctionBag; }
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

    ArrayList<Shop> shops = new SafeList<>();
    ArrayList<Quest> bounties = new SafeList<>();
    ArrayList<Character> characters = new SafeList<>();
    transient ArrayList<Character> deadCharacters = new SafeList<>();

    // City level 1-10, scaling with the coffers exactly like item prices (price = itemLvl^2.5 * 10, one city level per 10 item levels).
    // Level 1 needs no money; level 2 ~18k, 3 ~49k, 5 ~177k, 10 ~1M.
    public int cityLevel() {
        if (!city) return 0;
        double itemLvl = Math.pow(Math.max(0L, cityTreasury) / 10.0, 0.4);
        return Math.max(1, Math.min(10, (int) (itemLvl / 10)));
    }

    int perfBy = -1, perfStart, perfUntil, perfGrade;   // a musician playing here now: who, from which week, until which week, the grade of the instrument (Concerts.announce; the piece is worked out when somebody asks)
    String perfInst; boolean perfHeaven;                // the name of the instrument, and whether it is a Heaven-Sent one
    transient boolean storming;          // the attacker is fighting through the people of this place now (Sieges.storm): one storm at a time
    int siegeUntil = 0, siegeBy = -1;   // the week until which this city or sect seat counts as under attack, and who attacks it (see Defense)
    boolean crowdHotspot = false;   // overcrowded: beasts ambush the cultivators here until the tile thins out
    int crowdSurge = 0;             // level surge applied while crowdHotspot is set

    public int crowdCapacity() { return city ? CROWD_CAPACITY_CITY : CROWD_CAPACITY; }

    // Called every tick. An over-capacity tile becomes a hotspot (level surge) and beasts start ambushing its cultivators;
    // it returns to normal once the crowd drops under 80% of capacity.
    public boolean isOriginis() { return "Originis".equals(name); }

    // A per-turn index of the weapons and clothes for sale here, counted by item level: {shop weapons, shop clothes, pool weapons, pool clothes}. It is only a hint (a list may change under it);
    // the real purchase still reads the shelves. Lets a character tell in a few steps whether a city has anything worth a look.
    private transient volatile int[][] gearIdx;
    private transient volatile int gearIdxTime = -1, gearIdxSig = 0;

    int[][] gearIndex() {
        int sig = gearSignature();
        int[][] g = gearIdx;
        if (g != null && gearIdxTime == GameDriver.time && gearIdxSig == sig) return g;
        synchronized (this) {
            if (gearIdx != null && gearIdxTime == GameDriver.time && gearIdxSig == sig) return gearIdx;
            int[][] n = new int[4][102];
            try {
                for (Shop sh : shops) for (Item it : sh.inventory) countGear(n, it, 0);
                if (backpack != null) for (Item it : backpack) countGear(n, it, 2);
            } catch (RuntimeException e) { /* the shelves changed while counting: the index is only a hint */ }
            gearIdx = n; gearIdxTime = GameDriver.time; gearIdxSig = sig;
            return n;
        }
    }

    /** How many things are on the shelves and in the pool (a purchase or a sale inside the turn changes it, so the index is rebuilt). */
    private int gearSignature() {
        int sig = backpack == null ? 0 : backpack.size();
        try { for (Shop sh : shops) sig = sig * 31 + sh.inventory.size(); } catch (RuntimeException e) { sig = -1; }
        return sig;
    }

    private static void countGear(int[][] n, Item it, int base) {
        if (it == null || it.lvl < 0) return;
        int l = Math.min(101, it.lvl);
        if (it.itemType == Item.ItemType.Weapon) n[base][l]++; else if (it.itemType == Item.ItemType.Clothing) n[base + 1][l]++;
    }

    /** A city or dungeon that renamed the tile is gone: the land has its own name again (lore sites and Originis keep theirs; `geoName` stays for the pictures). */
    void restoreName() {
        if (geoName != null && loreSite == null && !city && dungeonType == null && !isOriginis() && !geoName.equals(name)) name = geoName;
    }

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
                Say.world("Overcrowding turns " + name + " into a hotspot! Beasts stir among its " + n + " cultivators.");
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
    static final int MATERIAL_FLOOR = 5_000, MATERIAL_TRICKLE = 50;
    static final int MAX_POPULATION = 100_000_000;

    // Non-cultivating population ceiling: 100k at city level 1, 400k at level 2 ... 10M at level 10 (wild tiles use level 1)
    public long populationCap() {
        long l = city ? cityLevel() : 1;
        return Math.min(MAX_POPULATION, 100_000L * l * l);
    }

    // Money paid to "the city": goes to the living lord, otherwise into the city treasury (never to a dead owner, never lost)
    public void receivePayment(long amount) {
        if (amount <= 0) return;
        long tenth = salesTax(amount);
        amount -= tenth;
        if (cityOwner != null && !cityOwner.dead) { cityOwner.credit(amount); }
        else cityTreasury += amount;
    }

    /** The city's cut of a sale: a tenth goes straight to the treasury (which sets the city level); the amount taken is returned. */
    public long salesTax(long amount) {
        if (!city || amount < 10) return 0;
        long tenth = amount / 10;
        cityTreasury += tenth;
        return tenth;
    }

    // Shops a city can host: 3 per city level
    public int maxShopSlots() { return city ? cityLevel() * 3 : 0; }

    // Recent corpses only (Lucky characters loot them); unbounded growth was a memory leak and made the loot scan O(n)
    public void addDead(Character c) {
        if (deadCharacters == null) deadCharacters = new SafeList<>();
        deadCharacters.add(c);
        if (deadCharacters.size() > 12) Slim.husk(deadCharacters.remove(0)); // the corpses of the last few are lootable; an older one lets go of its lists
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
    ArrayList<Character> tournamentRegistrants = new SafeList<>();
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
        backpack = new SafeList<>();
        materialBag = new SafeList<>();
        pillBag = new SafeList<>();
        arrayBag = new SafeList<>();
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
        backpack = new SafeList<>();
        materialBag = new SafeList<>();
        pillBag = new SafeList<>();
        arrayBag = new SafeList<>();
        addCreatedArea(this);
        Lore.onTileCreated(this); // a tile that holds one of the lore's places takes its name
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (deadCharacters == null) deadCharacters = new SafeList<>();
        if (characters == null) characters = new SafeList<>();
        // Fields added after older saves were written deserialize as null
        if (shops == null) shops = new SafeList<>();
        if (bounties == null) bounties = new SafeList<>();
        if (goodsBag == null) goodsBag = new SafeList<>();
        if (libraries == null) libraries = new SafeList<>();
        if (tournamentRegistrants == null) tournamentRegistrants = new SafeList<>();
        // lists of a saved game are plain ArrayLists: the lists people on other tiles read are made safe for that (see SafeList)
        characters = SafeList.of(characters); deadCharacters = SafeList.of(deadCharacters); shops = SafeList.of(shops); bounties = SafeList.of(bounties); goodsBag = SafeList.of(goodsBag);
        libraries = SafeList.of(libraries); tournamentRegistrants = SafeList.of(tournamentRegistrants); backpack = SafeList.of(backpack); materialBag = SafeList.of(materialBag); pillBag = SafeList.of(pillBag);
        arrayBag = SafeList.of(arrayBag); auctionBag = SafeList.of(auctionBag);
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
        // gathering must never leave a tile empty for good: a trickle (about 2,600 a year) comes back under the floor
        if (herbMaterials < MATERIAL_FLOOR) herbMaterials += MATERIAL_TRICKLE;
        if (metalMaterials < MATERIAL_FLOOR) metalMaterials += MATERIAL_TRICKLE;
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
            Tournaments.clearSeats(this);
            return;
        }
        java.util.Collections.shuffle(tournamentRegistrants, rand);
        java.util.ArrayList<Character> pool = new java.util.ArrayList<>(tournamentRegistrants);
        final java.util.ArrayList<Character> entrants = new java.util.ArrayList<>(tournamentRegistrants);
        final Tournaments.Record rec = new Tournaments.Record(Tournaments.interested(this, entrants)); // the bouts, kept for a person who plays: an entrant, or a spectator in the tile
        final java.util.IdentityHashMap<Character, StringBuilder> story = new java.util.IdentityHashMap<>(); // each human's bouts, in their own words
        final java.util.IdentityHashMap<Character, Integer> out = new java.util.IdentityHashMap<>();       // the round a human fell in
        final java.util.IdentityHashMap<Character, java.util.List<FightReplay>> pics = new java.util.IdentityHashMap<>(); // each human's bouts as pictures for the fight window
        int round = 1;
        while (pool.size() > 1) {
            java.util.ArrayList<Character> next = new java.util.ArrayList<>();
            for (int _i = 0; _i + 1 < pool.size(); _i += 2) {
                Character a = pool.get(_i), b = pool.get(_i + 1);
                if (a.dead) { next.add(b); noteBout(story, b, "Round " + round + ": your opponent had died, so you move on."); continue; }
                if (b.dead) { next.add(a); noteBout(story, a, "Round " + round + ": your opponent had died, so you move on."); continue; }
                a.tempLvlCheck(); b.tempLvlCheck();
                int wc = Math.max(5, Math.min(95, 50 - 15 * (b.tempLvl - a.tempLvl) + Elements.swing(a, b)));
                if (a.trait == Character.Traits.Lucky) wc = Math.min(95, wc + 45);
                if (b.trait == Character.Traits.Lucky) wc = Math.max(5, wc - 45);
                boolean aWins = rand.nextInt(100) < wc;
                Character w = aWins ? a : b, l = aWins ? b : a;
                next.add(w);
                out.put(l, round);
                rec.add(a, b, wc, w, round);
                if (a.player == 1 || b.player == 1) {
                    try {
                        FightReplay pic = FightReplay.bout(a, b, wc, w, "Round " + round + " of the tournament at " + name, name);
                        pic.tourney = 1;
                        Character human = a.player == 1 ? a : b;
                        java.util.List<FightReplay> list = pics.get(human);
                        if (list == null) { list = new java.util.ArrayList<>(); pics.put(human, list); }
                        list.add(pic);
                        if (a.player == 1 && b.player == 1) { java.util.List<FightReplay> l2 = pics.get(b); if (l2 == null) { l2 = new java.util.ArrayList<>(); pics.put(b, l2); } l2.add(pic); }
                    } catch (RuntimeException e) { }
                }
                noteBout(story, w, "Round " + round + ": you beat " + l.name + " " + l.lastName + " (Lv." + l.lvl + ", " + (aWins ? wc : 100 - wc) + "% to win).");
                noteBout(story, l, "Round " + round + ": you lost to " + w.name + " " + w.lastName + " (Lv." + w.lvl + ", " + (aWins ? 100 - wc : wc) + "% to win).");
            }
            if (pool.size() % 2 == 1) { Character bye = pool.get(pool.size() - 1); next.add(bye); noteBout(story, bye, "Round " + round + ": no opponent this round (a bye)."); }
            pool = next;
            round++;
        }
        final int rounds = round - 1;
        Character winner = pool.isEmpty() ? null : pool.get(0);
        if (winner != null && !winner.dead && tournamentPrize != null) {
            winner.backpack.add(tournamentPrize);
            winner.equip();
        }
        if (winner != null && !winner.dead) Fame.tournament(winner, rounds, tournamentMaxMajor, true); // a name made on the sand
        boolean hasPlayer = Map.characters.stream().anyMatch(c -> c != null && c.player > 0 && c.skipping <= 0);
        final String title = name + (tournamentName == null ? "" : " (" + tournamentName + ")");
        if (hasPlayer && winner != null)
            Say.world("=== Tournament at " + title + ": " + winner + " wins " + tournamentPrize + "! ===");
        if (winner != null) Chronicle.add("world", winner.name + " " + winner.lastName + " won the tournament at " + title + (tournamentPrize != null ? " and took " + tournamentPrize : "") + ".", winner.id);
        for (Character h : entrants) if (h != null && h.player == 1) reportToHuman(h, title, entrants.size(), rounds, winner, tournamentPrize, story.get(h), out.get(h), pics.get(h));
        Tournaments.spectate(this, title, entrants, rec, rounds, winner); // a person who plays and only watched: the last rounds, and bets
        tournamentRegistrants.clear(); tournamentPrize = null; tournamentOrganizer = null;
    }

    private static void noteBout(java.util.IdentityHashMap<Character, StringBuilder> story, Character c, String what) {
        if (c == null || c.player != 1) return;
        StringBuilder sb = story.get(c);
        if (sb == null) { sb = new StringBuilder(); story.put(c, sb); }
        sb.append("  ").append(what).append("\n");
    }

    /** Tells a human who entered what happened: a notice, and a window with each bout and the result. */
    static void reportToHuman(Character h, String title, int field, int rounds, Character winner, Item prize, StringBuilder bouts, Integer fellIn, java.util.List<FightReplay> pics) {
        boolean won = winner == h;
        StringBuilder sb = new StringBuilder("=== Tournament at " + title + " ===\n");
        sb.append(field).append(" fighters entered; ").append(rounds).append(rounds == 1 ? " round.\n\n" : " rounds.\n\n");
        sb.append("Your bouts:\n").append(bouts == null ? "  (you were not matched)\n" : bouts.toString()).append("\n");
        String head;
        if (won) {
            head = "You won the tournament at " + title + "!";
            sb.append("YOU WON THE TOURNAMENT!").append(prize != null ? "\nPrize: " + prize + " (in your backpack)." : "").append("\n");
        } else {
            head = "You lost the tournament at " + title + (fellIn != null ? " in round " + fellIn + " of " + rounds : "") + ".";
            sb.append(fellIn != null ? "You were knocked out in round " + fellIn + " of " + rounds + ".\n" : "");
            if (winner != null) sb.append("Champion: ").append(winner.name).append(" ").append(winner.lastName).append(" (Lv.").append(winner.lvl).append(").\n");
        }
        Notifications.post("fight", head, -1, h.id);
        Tournaments.showToEntrant(h, "Tournament result", sb.toString(), pics); // the bouts through the fight window, then the result with a Watch button for each
    }

    public String toString() {
        return (" is in the " + name + " at x: " + x * 100 + ", y: " + y * 100);
        //return (currentCharacter + " is in the " + name + " at x: " + x * 100 + ", y: " + y * 100 + " Lvl: " + lvl + " animalCount: " + animals + " monsterCount: " + monsters + " spiritStoneCount: " + spiritStoneMaterials + " herbMaterialCount: " + herbMaterials + " metalMaterialCount: " + metalMaterials);
    }
}

