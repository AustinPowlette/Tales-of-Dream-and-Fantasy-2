package Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import java.util.Random;
import java.util.Set;

/** One place for every generated name: cultivators (no two living namesakes where avoidable), places (biome-aware adjectives, colours, numbers,
 *  possessive owners), cities, sects, dungeons, shops, libraries, techniques, reputation titles, beasts (species tiered by level) and items (grade
 *  follows the item's Major level). Everything is seeded from `GameDriver.rand`; unique names are tracked in registries that `rebuild()` refills
 *  after a load. */
public final class Naming {
    private Naming() { }

    private static Random rand() { return GameDriver.rand; }
    private static <T> T pick(List<T> l) { return l.get(rand().nextInt(l.size())); }
    private static String pick(String... a) { return a[rand().nextInt(a.length)]; }
    private static String cap(String s) { return s.isEmpty() ? s : s.substring(0, 1).toUpperCase() + s.substring(1); }

    // ------------------------------------------------------------------ registries
    private static final Set<String> livingNames = new HashSet<>();
    private static final Set<String> usedPlaces = new HashSet<>(), usedSects = new HashSet<>();

    static synchronized void reset() { livingNames.clear(); usedPlaces.clear(); usedSects.clear(); }

    /** Refills the registries from the live world (after a load or when a world is created). */
    static synchronized void rebuild() {
        reset();
        for (Character c : new ArrayList<>(Map.characters)) if (c != null && !c.dead && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster) livingNames.add(c.name + " " + c.lastName);
        for (Area a : new ArrayList<>(Map.createdAreas)) { usedPlaces.add(a.name); if (a.sect != null) usedSects.add(a.sect.name); }
    }

    // ------------------------------------------------------------------ cultivators
    /** {first name, surname} for a new cultivator: re-rolls a few times so two living people rarely share a full name. */
    static synchronized String[] person(boolean female) {
        String[] firsts = female ? Dictionary.femaleNames : Dictionary.maleNames;
        String first = "", last = "";
        for (int i = 0; i < 10; i++) {
            first = firsts[rand().nextInt(firsts.length)];
            last = Dictionary.lastNames[rand().nextInt(Dictionary.lastNames.length)];
            if (!livingNames.contains(first + " " + last)) break;
        }
        livingNames.add(first + " " + last);
        return new String[]{first, last};
    }

    private static int[] eastRange(String[] arr, String from, String to) {
        int a = Arrays.asList(arr).indexOf(from), b = Arrays.asList(arr).lastIndexOf(to);
        return a < 0 || b < a ? new int[]{0, 0} : new int[]{a, b + 1};
    }

    /** A new cultivator born (or arriving) in `where`: in a region of pinyin-like culture (see Regions) about 45% take East Asian names, elsewhere about 12% do;
     *  everyone else draws from the whole dictionary as before. */
    static synchronized String[] person(boolean female, Area where) {
        int culture = where == null ? 0 : Regions.culture(where);
        if (culture == 0) return person(female);
        String[] firsts = female ? Dictionary.femaleNames : Dictionary.maleNames;
        int[] fr = female ? eastRange(firsts, "Aiko", "Zhu") : eastRange(firsts, "Akira", "Zhen"), lr = eastRange(Dictionary.lastNames, "Zhao", "Jang");
        int chance = culture == 1 ? 45 : 12;
        String first = "", last = "";
        for (int i = 0; i < 10; i++) {
            boolean east = fr[1] > fr[0] && lr[1] > lr[0] && rand().nextInt(100) < chance;
            first = east ? firsts[fr[0] + rand().nextInt(fr[1] - fr[0])] : firsts[rand().nextInt(firsts.length)];
            last = east ? Dictionary.lastNames[lr[0] + rand().nextInt(lr[1] - lr[0])] : Dictionary.lastNames[rand().nextInt(Dictionary.lastNames.length)];
            if (!livingNames.contains(first + " " + last)) break;
        }
        livingNames.add(first + " " + last);
        return new String[]{first, last};
    }

    /** The label on a pill: a house of the alchemist's sect when it is a pill-making one, otherwise their own cauldron. */
    static String pillBrand(Character maker) {
        if (maker.sect != null && maker.sect.name != null && (maker.sect.name.contains("Pill") || maker.sect.name.contains("Cauldron") || maker.sect.name.contains("Elixir"))) return maker.sect.name;
        String last = maker.lastName == null ? "Wandering" : maker.lastName;
        String[] kinds = {"Pill House", "Cauldron", "Dan Hall", "Elixir Studio", "Medicine Hall"};
        return last + " " + kinds[(int) Math.floorMod(Naming.mix64(maker.id * 31L + last.hashCode()), (long) kinds.length)];
    }

    /** A first name for a newborn who takes `last` from the father: avoids names already used with that surname. */
    static synchronized String firstFor(boolean female, String last) {
        String[] firsts = female ? Dictionary.femaleNames : Dictionary.maleNames;
        String first = firsts[rand().nextInt(firsts.length)];
        for (int i = 0; i < 10 && livingNames.contains(first + " " + last); i++) first = firsts[rand().nextInt(firsts.length)];
        livingNames.add(first + " " + last);
        return first;
    }

    static synchronized void claimName(String first, String last) { livingNames.add(first + " " + last); }
    static synchronized void releaseName(String first, String last) { livingNames.remove(first + " " + last); }

    // ------------------------------------------------------------------ beasts
    static String beastAdjective() {
        String[] pool = rand().nextInt(100) < 75 ? Dictionary.beastAdjectives : Dictionary.adjectivesEntities; // mostly fantasy words, sometimes a plain one
        return pool[rand().nextInt(pool.length)];
    }

    /** Species by level tier: 1-20 common animals, 21-50 spirit beasts, 51-80 high beasts, 81+ mythic; high levels sometimes a named legend. */
    static String beastSpecies(int lvl) {
        if (lvl >= 40 && rand().nextInt(25) == 0) return Dictionary.legendaryMonsters[rand().nextInt(Dictionary.legendaryMonsters.length)];
        String[] t = lvl <= 20 ? Dictionary.monstersLow : lvl <= 50 ? Dictionary.monstersMid : lvl <= 80 ? Dictionary.monstersHigh : Dictionary.monstersMythic;
        return t[rand().nextInt(t.length)];
    }

    // ------------------------------------------------------------------ places
    static final String[] POSSESSIVES = {"Dragon's", "Phoenix's", "Tiger's", "Crane's", "Hermit's", "Immortal's", "Sage's", "Elder's", "Ancestor's", "Wanderer's", "Giant's", "Fox's", "Turtle's"};
    private static final Set<String> NOT_OCEAN = new HashSet<>(Arrays.asList("Landlocked", "Sandy", "Dusty", "Arid", "Grassy", "Bald", "Rocky", "Thorny", "Shady", "Windswept", "Parched", "Barren", "Terraced", "Overgrown", "Highland", "Lowland", "Fragrant"));
    private static final Set<String> NOT_DRY = new HashSet<>(Arrays.asList("Flooded", "Snowy", "Frozen", "Frostbitten", "Glacial", "Muddy", "Drowned", "Lakeside", "Riverside", "Weeping", "Misty", "Cloudy", "Lush", "Verdant", "Blooming", "Grassy", "Bamboo", "Willow", "Lotus"));
    private static final Set<String> NOT_COLD = new HashSet<>(Arrays.asList("Sandy", "Dusty", "Scorching", "Burning", "Sweltering", "Sun-Scorched", "Boiling", "Hot", "Fiery", "Molten", "Parched", "Sultry", "Jungle"));

    static String placeAdjective(String biomem) {
        String b = biomem == null ? "" : biomem;
        boolean ocean = b.equals("Ocean"), dry = b.equals("Desert") || b.equals("Frozen Desert") || b.equals("Savanna"), cold = b.equals("Tundra") || b.startsWith("Frozen") || b.equals("Taiga");
        for (int i = 0; i < 12; i++) {
            String w = rand().nextInt(100) < 16 ? Dictionary.colors[rand().nextInt(Dictionary.colors.length)] : Dictionary.adjectivesPlaces[rand().nextInt(Dictionary.adjectivesPlaces.length)];
            if (ocean && NOT_OCEAN.contains(w)) continue;
            if (dry && NOT_DRY.contains(w)) continue;
            if (cold && NOT_COLD.contains(w)) continue;
            return w;
        }
        return "Hidden";
    }

    /** [Number] [Adjective] Noun [of X] — a tile's geographic name. A number is followed by a plural noun so "Eight Giant Footprints" reads right. */
    static String place(String biomem) { return place(biomem, 0, 0.5, false); }

    /** A place name whose noun fits the terrain (see BiomeGen.nounFor). */
    static String place(String biomem, double elev, double moist, boolean river) {
        String[] nouns = nounsFor(biomem);
        StringBuilder sb = new StringBuilder();
        boolean number = rand().nextInt(5) == 0;
        if (number) sb.append(Dictionary.numbers[rand().nextInt(Dictionary.numbers.length)]).append(' ');
        else if (rand().nextInt(100) < 9) sb.append(POSSESSIVES[rand().nextInt(POSSESSIVES.length)]).append(' ');
        if (rand().nextInt(100) < 80) sb.append(placeAdjective(biomem)).append(' ');
        sb.append(BiomeGen.nounFor(nouns, elev, moist, river, rand()));
        if (rand().nextInt(5) == 0) sb.append(" of ").append(Dictionary.adjectiveOf[rand().nextInt(Dictionary.adjectiveOf.length)]);
        return sb.toString();
    }

    static String[] nounsFor(String biomem) {
        switch (biomem == null ? "" : biomem) {
            case "Ocean": return Dictionary.ocean;
            case "Grassland": return Dictionary.grassland;
            case "Forest": return Dictionary.forest;
            case "Desert": return Dictionary.desert;
            case "Tundra": return Dictionary.tundra;
            case "Beach": return Dictionary.beach;
            case "Frozen Grassland": return Dictionary.frozenGrassland;
            case "Frozen Desert": return Dictionary.frozenDesert;
            case "Taiga": return Dictionary.taiga;
            case "Savanna": return Dictionary.savanna;
            default: return Dictionary.grassland;
        }
    }

    // ------------------------------------------------------------------ syllables, cities
    private static final String[] PIN_ON = {"q", "y", "l", "h", "f", "x", "z", "j", "m", "w", "ch", "sh", "b", "d", "g", "t", "k", "s", "n", "zh"};
    private static final String[] PIN_FIN = {"ing", "un", "ian", "ang", "ai", "ou", "ei", "ao", "en", "iu", "an", "ong", "uo", "in", "ua", "ie", "ui"};
    private static final String[] FANT_ON = {"Al", "Bel", "Cor", "Dun", "Eld", "Fen", "Gal", "Hal", "Ire", "Kes", "Lor", "Mar", "Nor", "Ol", "Pen", "Quen", "Ral", "Sil", "Tor", "Ul", "Val", "Wyn", "Ash", "Bran", "Cas", "Dor", "Elm", "Fal"};
    private static final String[] FANT_END = {"ford", "haven", "mere", "ton", "wick", "gard", "dale", "stead", "moor", "crest", "fall", "vale", "port", "holt"};
    private static final String[] CITY_RANK_SMALL = {"Town", "Market", "Landing", "Crossing"}, CITY_RANK_BIG = {"City", "Metropolis", "Capital", "Great City"};
    private static final String[] LANDMARKS = {"Rest", "Hold", "Crossing", "Landing", "Reach", "Watch", "Haven", "Gate", "Keep", "Ford", "Hollow", "Rise"};
    private static final String[] CITY_COUNTS = {"Three", "Five", "Seven", "Nine", "Twelve", "Thirty-Six", "Hundred", "Thousand"};
    private static final String[] CITY_THINGS = {"Pines", "Willows", "Springs", "Bridges", "Peaks", "Lanterns", "Cranes", "Pagodas", "Bells", "Lotuses", "Rivers", "Gates", "Winds", "Moons", "Pillars", "Stones"};
    private static final String[] CITY_ADJ = {"Azure", "Jade", "Golden", "Silver", "Crimson", "White", "Black", "Eternal", "Gleaming", "Mist", "Cloud", "Thunder", "Moon", "Sun", "Star", "Lotus", "Phoenix", "Dragon", "Crane", "Tiger"};
    private static final String[] CITY_NOUN = {"Harbor", "Terrace", "Market", "Garden", "Hall", "Lantern", "Spring", "Bridge", "Pavilion", "Crossing", "Hill", "Bay"};

    static String syllables() { return syllables(rand(), 0); }

    /** A made-up place name. style 1 = pinyin-like, 2 = western-fantasy, 0 = either. */
    static String syllables(Random r, int style) {
        boolean pin = style == 1 || style == 0 && r.nextBoolean();
        if (pin) return cap(PIN_ON[r.nextInt(PIN_ON.length)] + PIN_FIN[r.nextInt(PIN_FIN.length)]) + (r.nextBoolean() ? PIN_ON[r.nextInt(PIN_ON.length)] + PIN_FIN[r.nextInt(PIN_FIN.length)] : "");
        return FANT_ON[r.nextInt(FANT_ON.length)] + FANT_END[r.nextInt(FANT_END.length)];
    }

    /** A new name for a city; `population` picks the rank word. Unique among places. */
    static synchronized String city(Character founder, long population) {
        for (int tries = 0; tries < 30; tries++) {
            String n;
            int roll = rand().nextInt(100);
            String rank = population > 120_000 ? pick(CITY_RANK_BIG) : population > 60_000 ? "City" : pick(CITY_RANK_SMALL);
            int style = founder == null || founder.location == null ? 0 : Regions.culture(founder.location);
            if (roll < 35) n = syllables(rand(), style) + " " + rank;
            else if (roll < 60 && founder != null) n = founder.lastName + "'s " + pick(LANDMARKS);
            else if (roll < 80) n = pick(CITY_COUNTS) + " " + pick(CITY_THINGS);
            else n = pick(CITY_ADJ) + " " + pick(CITY_NOUN) + " " + rank;
            if (usedPlaces.add(n)) return n;
        }
        return unique(usedPlaces, syllables() + " " + pick(CITY_RANK_BIG));
    }

    static synchronized void claimPlace(String n) { usedPlaces.add(n); }

    private static final String[] ROMAN = {"", "", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX"};
    /** `base`, or `base II`, `base III`... until the name is free in `used`. */
    private static String unique(Set<String> used, String base) {
        if (used.add(base)) return base;
        for (int i = 2; ; i++) {
            String n = base + " " + (i < ROMAN.length ? ROMAN[i] : String.valueOf(i));
            if (used.add(n)) return n;
        }
    }

    // ------------------------------------------------------------------ sects
    private static final String[] SECT_EVIL = {"Blood Moon", "Black Lotus", "Ninefold Hell", "Crimson Shadow", "Soul Devouring", "Netherworld", "Ashen Skull", "Withered Bone", "Void Reaper", "Plague Wind", "Nightfall", "Bleeding Sun", "Thousand Ghosts", "Iron Fang"};
    private static final String[] SECT_GOOD = {"Radiant Sun", "Heavenly Dao", "Compassionate Lotus", "Azure Dragon", "Eternal Spring", "White Crane", "Jade Phoenix", "Golden Bell", "Celestial Harmony", "Righteous Sword", "Dawn Lantern", "Nine Virtues"};
    private static final String[] SECT_NEUTRAL = {"Wandering Cloud", "Hidden Valley", "Silent Mountain", "Drifting Leaf", "Twin Rivers", "Misty Peak", "Nine Winds", "Hollow Bamboo", "Iron Mountain", "Endless Sea", "Sleeping Dragon", "Three Pines"};

    static synchronized String sect(Character founder) {
        String[] pool = founder.affiliationNum < 0 ? SECT_EVIL : founder.affiliationNum > 0 ? SECT_GOOD : SECT_NEUTRAL;
        String[] kinds;
        switch (founder.trait) {
            case Alchemist: kinds = new String[]{"Pill Pavilion", "Cauldron Hall", "Elixir Temple"}; break;
            case Blacksmith: kinds = new String[]{"Forge", "Anvil Hall", "Weapon Hall"}; break;
            case Scribe: kinds = new String[]{"Scroll Hall", "Library Pavilion", "Inscription Temple"}; break;
            case Merchant: kinds = new String[]{"Trading Guild", "Merchant Alliance", "Treasure Pavilion"}; break;
            case Leader: kinds = new String[]{"Dynasty", "Alliance", "Palace", "Throne Hall"}; break;
            case Runner: kinds = new String[]{"Swift Gate", "Cloud Path Pavilion", "Wind Courier Hall"}; break;
            case Gatherer: case Miner: kinds = new String[]{"Herb Valley", "Stone Hall", "Harvest Temple"}; break;
            default: kinds = new String[]{"Sect", "Temple", "Valley", "Peak", "Gate", "Hall", "Palace", "Pavilion"};
        }
        for (int tries = 0; tries < 30; tries++) {
            String n = pick(pool) + " " + pick(kinds);
            int roll = rand().nextInt(100);
            if (roll < 18 && founder.lastName != null && founder.trait != Character.Traits.Beast) n = founder.lastName + " " + pick("Clan", "Family Hall", "Ancestral Hall", "House");
            else if (roll < 36) { String geo = geoWord(founder.location); if (geo != null) n = geo + " " + pick(kinds); }
            if (usedSects.add(n)) return n;
        }
        return unique(usedSects, pick(pool) + " " + pick(kinds)); // every combination is taken: a numbered successor
    }

    /** "Misty Peak" from a tile called "Misty Peaks" (the sect takes its name from the land); null when the tile name does not suit. */
    private static String geoWord(Area a) {
        if (a == null) return null;
        String g = a.geoName != null ? a.geoName : a.name;
        if (g == null || g.contains(" of ") || g.contains("'") || g.split(" ").length > 3) return null;
        for (String w : Dictionary.numbers) if (g.startsWith(w + " ")) return null;
        return g.endsWith("ies") ? g.substring(0, g.length() - 3) + "y" : g.endsWith("s") && !g.endsWith("ss") ? g.substring(0, g.length() - 1) : g;
    }

    // ------------------------------------------------------------------ dungeons, shops, libraries
    private static final String[] D_ADJ = {"Jade", "Crimson", "Withered", "Nine-Fold", "Silent", "Ashen", "Weeping", "Iron", "Sunken", "Forgotten", "Bone", "Moonlit", "Thousand-Eyed", "Sleeping", "Black", "Frozen", "Hollow", "Gilded"};
    private static final String[] D_THING = {"Dragon", "Phoenix", "Tiger", "Heron", "Bell", "Lantern", "Lotus", "Serpent", "Crow", "Lion", "Tortoise", "Crane", "Wolf", "Mirror", "Throne", "Sword"};
    private static final String[] D_RULER = {"Jade King", "Sorrowful Queen", "Nameless Emperor", "Sleeping Sage", "Blood Duke", "Hundred-Eyed Lord", "Pale Empress", "Iron General"};

    static synchronized String dungeon(Area.DungeonType type, int lvl) {
        String adj = pick(D_ADJ), thing = pick(D_THING);
        String n;
        switch (type) {
            case LOST_RUINS: n = rand().nextBoolean() ? "Ruins of the " + pick(D_RULER) : "Sunken " + thing + " Citadel"; break;
            case BEAST_LAIR: n = rand().nextBoolean() ? adj + " " + thing + " Den" : "Lair of the " + adj + " " + thing; break;
            case MARTIAL_TRIAL: n = rand().nextBoolean() ? "Gauntlet of the " + adj + " " + thing : adj + " " + thing + " Trial Peak"; break;
            case HAUNTED_GROUNDS: n = rand().nextBoolean() ? adj + " " + thing + " Graveyard" : "Weeping " + thing + " Barrows"; break;
            default: n = "Heavenly Trial Grounds of the " + adj + " " + thing;
        }
        return unique(usedPlaces, n);
    }

    private static final String[] SHOP_KIND = {"Emporium", "Pavilion", "Exchange", "Bazaar", "Trading House", "Counter", "Treasury"};
    static String shop(Character owner) {
        int r = rand().nextInt(3);
        if (r == 0 && owner != null) return owner.lastName + "'s " + pick(SHOP_KIND);
        if (r == 1) return pick(CITY_COUNTS) + " Treasures " + pick("Pavilion", "Hall", "House");
        return pick(CITY_ADJ) + " " + pick("Lantern", "Abacus", "Scale", "Coin", "Bridge") + " " + pick(SHOP_KIND);
    }

    static String library(String owner) {
        return pick("Hall of Whispered Scrolls", "Pavilion of Quiet Pages", "Archive of the " + pick(CITY_ADJ) + " " + pick("Crane", "Lotus", "Brush"), "Library of Nine Winds", "House of Unfinished Books", "Scroll Tower of " + (owner == null ? "the Ancients" : owner));
    }

    // ------------------------------------------------------------------ techniques
    private static final String[] T_NUM = {"Nine", "Seven", "Three", "Thousand", "Hundred", "Ten Thousand", "Eight", "Twelve", "Single", "Twin"};
    private static final String[] T_ELEM = {"Thunder", "Frost", "Flame", "Wind", "Stone", "River", "Shadow", "Moon", "Sun", "Star", "Iron", "Jade", "Void", "Storm", "Mist", "Lightning", "Blood", "Spirit"};
    private static final String[] T_CREATURE = {"Dragon", "Phoenix", "Tiger", "Crane", "Serpent", "Tortoise", "Wolf", "Lion", "Eagle", "Mantis", "Bear", "Monkey", "Qilin", "Heron"};
    private static final String[] T_FORM = {"Palm", "Fist", "Sword Art", "Saber Technique", "Spear Art", "Staff Method", "Fan Dance", "Scripture", "Strike", "Claw", "Kick", "Finger", "Seal"};
    private static final String[] T_MOVE = {"Step", "Leap", "Flight", "Shadow Walk", "Stride", "Dash", "Glide", "Footwork", "Ascent", "Vanishing"};
    private static final String[] T_HEAVEN = {"Heaven", "Earth", "Nine Heavens", "Ten Directions", "Eight Trigrams", "Five Elements", "Two Principles"};

    static String technique(Technique.techniqueType type, Character maker) {
        if (type == Technique.techniqueType.Movement) {
            int r = rand().nextInt(3);
            if (r == 0) return pick(T_ELEM) + "-" + pick("Treading", "Riding", "Crossing", "Chasing") + " " + pick(T_MOVE);
            if (r == 1) return pick(T_CREATURE) + " " + pick(T_MOVE);
            return pick(T_NUM) + " " + pick("Cloud", "Wind", "Shadow", "Moon", "Mist") + " " + pick(T_MOVE);
        }
        int r = rand().nextInt(4);
        if (r == 0) return pick(T_NUM) + " " + pick(T_HEAVEN.length > 0 ? new String[]{"Heavens", "Peaks", "Rivers", "Thunders", "Dragons", "Suns"} : T_ELEM) + " " + pick(T_FORM);
        if (r == 1) return pick(T_ELEM) + " " + pick(T_CREATURE) + " " + pick(T_FORM);
        if (r == 2) return pick(T_HEAVEN) + " " + pick(T_ELEM) + " " + pick(T_FORM);
        return pick(T_CREATURE) + "-Subduing " + pick(T_FORM);
    }

    // ------------------------------------------------------------------ reputation titles
    /** The title a cultivator has earned, or null. Deterministic (same input, same title) so it does not flicker from year to year. */
    static String title(Character c) {
        boolean f = "Female".equals(c.gender);
        if (c.majorLvl >= 10) return "True Immortal";
        if (c.kills >= 500) return "God Slayer";
        if (c.majorLvl >= 9) return "Pseudo Immortal Venerable";
        if (c.majorLvl >= 8) return f ? "Heaven-Ascending Matriarch" : "Heaven-Ascending Patriarch";
        if (c.majorLvl >= 7) return c.id % 2 == 0 ? "Ascendant Venerable" : "Sovereign of the Ascension";
        if (c.sect != null && c.sect.master == c && c.majorLvl >= 3) return "Master of the " + c.sect.name;
        if (c.kills >= 300) return f ? "Blood-Soaked Empress" : "Blood-Soaked Tyrant";
        if (c.kills >= 100) return f ? "Demoness Butcher" : "Demon Butcher";
        if (c.majorLvl >= 5) return f ? "Nascent Soul Lady" : "Nascent Soul Elder";
        switch (c.trait) {
            case Alchemist: if (c.itemsCrafted >= 400 && c.majorLvl >= 2) return "Pill Sage"; break;
            case Blacksmith: if (c.itemsCrafted >= 400 && c.majorLvl >= 2) return "Forge Master"; break;
            case Scribe: if (c.itemsCrafted >= 300 && c.majorLvl >= 2) return "Keeper of Scrolls"; break;
            case Merchant: if (PlayerLists.wealth(c) >= 1_000_000_000L) return "Golden Abacus"; break;
            case Runner: if (c.age >= 40 && c.majorLvl >= 2) return "Thousand-Mile Wanderer"; break;
            case Friendly: if (c.relationshipList != null && c.relationshipList.relationships.size() >= 25) return "Friend of All"; break;
            case Lucky: if (c.majorLvl >= 2 && c.age >= 40) return "The Fortunate"; break;
            case Ghost: if (c.originalName != null || c.isGhostForm) return "The Twice-Born"; break;
            default: break;
        }
        if (c.kills >= 50) return f ? "Slaughter Maiden" : "Slaughter King";
        if (c.professionLvl >= 100 && c.trait != null) {
            switch (c.trait) {
                case Alchemist: return f ? "Pill Queen" : "Pill King";
                case Blacksmith: return f ? "Forge Lady" : "Forge Saint";
                case Tailor: return "Robe Saint";
                case Scribe: return "Array Sage";
                case Innovative: return "Technique Ancestor";
                case Merchant: return f ? "Trade Queen" : "Trade King";
                default: break;
            }
        }
        if (c.majorLvl >= 3 && c.kills >= 30) return f ? "Ironhand Lady" : "Ironhand";
        return null;
    }

    // ------------------------------------------------------------------ items
    static String grade(int major) { return major <= 2 ? "Mortal" : major <= 4 ? "Spirit" : major <= 6 ? "Earth" : major <= 8 ? "Heaven" : "Divine"; }

    private static final String[][] WEAPON_ADJ = {
        {"Iron", "Bronze", "Steel", "Tempered", "Plain", "Cold-Forged", "Silver", "Ashen", "Worn", "Sturdy"},
        {"Jade", "Blazing", "Frostbitten", "Moonlit", "Storm", "Thunder", "Crimson", "Azure", "Verdant", "Whispering", "Obsidian", "Wind"},
        {"Dragon", "Phoenix", "Tiger", "Ghost", "Phantom", "Shadow", "Spirit", "Soul", "Star", "Lightning", "Sunforged", "Cloud-Splitting"},
        {"Void", "Immortal", "Heaven-Cleaving", "Earth-Rending", "Sky-Piercing", "Eternal", "Devouring", "Primordial", "Nine-Dragon", "Celestial", "Abyssal", "Wrathful"}};
    private static final String[][] CLOTH_ADJ = {
        {"Plain", "Coarse", "Hempen", "Quilted", "Simple", "Mended", "Sturdy", "Padded"},
        {"Silk", "Jade-Thread", "Cloud", "Wind", "Frost", "Lotus-Embroidered", "Moonlit", "Azure", "Crimson", "Gilded"},
        {"Dragon-Scale", "Phoenix-Feather", "Crane-Feather", "Ghost", "Shadow", "Star", "Phantom", "Iron-Thread", "Thunder", "Ember"},
        {"Celestial", "Divine", "Ethereal", "Void", "Immortal", "Sacred", "Heaven-Woven", "Eternal", "Starlit", "Gossamer"}};
    private static final String[][] ARRAY_ADJ = {
        {"Binding", "Concealing", "Defensive", "Gathering", "Mist", "Trapping", "Warding", "Obstructing"},
        {"Repelling", "Sealing", "Mountain", "Thunder", "Spirit-Gathering", "Wind-Gathering", "Illusory", "Frost-Binding"},
        {"Elemental", "Five-Element", "Four-Directional", "Killing", "Life-Protecting", "Crushing", "Seven-Star", "Bewildering"},
        {"Heaven-Sealing", "Void", "Heaven-Earth", "Yin-Yang", "Eight-Trigram", "Nine-Palace", "Azure-Dragon", "Soul-Binding"}};
    private static int band(int major) { return major <= 2 ? 0 : major <= 5 ? 1 : major <= 8 ? 2 : 3; }

    private static String adj(String[][] t, String[] dict, int major) { return rand().nextInt(100) < 25 ? dict[rand().nextInt(dict.length)] : pickBand(t, major); }
    static String weapon(int major, String type) { return grade(major) + "-Grade " + adj(WEAPON_ADJ, Dictionary.weaponAdjectives, major) + " " + type; }
    static String clothing(int major, String type) { return grade(major) + "-Grade " + adj(CLOTH_ADJ, Dictionary.clothingAdjectives, major) + " " + type; }
    static String array(int major, String type) { return grade(major) + "-Grade " + adj(ARRAY_ADJ, Dictionary.arrayAdjectives, major) + " " + type; }
    private static String pickBand(String[][] t, int major) { String[] a = t[band(major)]; return a[rand().nextInt(a.length)]; }

    private static final String[] HERBS = {"Spirit Grass", "Foundation Fern", "Core-Warming Ginseng", "Soul Lotus", "Nascent Moss", "Ensoul Blossom", "Nihility Orchid", "Ascension Lingzhi", "Immortal Peach", "Pseudo-Immortal Dew"};
    private static final String[] METALS = {"Black Iron", "Cold Steel", "Spirit Silver", "Meteoric Iron", "Jade Copper", "Star Metal", "Void Bronze", "Dragon-Bone Alloy", "Heavenly Gold", "Primordial Ore"};
    static String herbBase(int lvl) { return HERBS[Math.max(0, Math.min(HERBS.length - 1, lvl - 1))]; }

    private static final String[] DISHES = {"Congee", "Dumplings", "Soup", "Noodles", "Stew", "Steamed Buns", "Porridge", "Hotpot", "Rice Cakes", "Roast"};
    private static final String[] WINES = {"Wine", "Liquor", "Rice Wine", "Spirits", "Mead", "Nectar", "Plum Wine", "Baijiu"};
    private static final String[] POISONS = {"Venom", "Toxin", "Bane", "Draught", "Powder", "Extract", "Serpent Oil", "Grave Dust"};
    private static final String[] POISON_ADJ = {"Creeping", "Silent", "Weeping", "Withering", "Crimson", "Pale", "Black", "Hollow"};
    private static final String[][] INSTR_ADJ = {
        {"Bamboo", "Pine", "Plain", "Sandalwood", "Willow", "Simple"},
        {"Jade", "Moonlit", "Silk-Strung", "Crane", "Rosewood", "Lotus"},
        {"Phoenix", "Dragon-Tendon", "Cloud", "Frost", "Thunderwood", "Starlit"},
        {"Heaven-Echo", "Void", "Nine-Heaven", "Immortal", "Primordial", "Celestial"}};
    private static final String[] INSTRUMENTS = {"Guqin", "Pipa", "Erhu", "Dizi", "Xiao", "Guzheng", "Sheng", "Konghou", "Bell Chime", "War Drum"};
    private static String pickOf(String[] a) { return a[rand().nextInt(a.length)]; }
    private static final String[][] METHODS = {
        {"Steamed", "Boiled", "Plain", "Home-Style", "Roasted", "Fried"},
        {"Braised", "Twice-Cooked", "Honey-Glazed", "Slow-Simmered", "Clear-Broth", "Smoked"},
        {"Fire-Roasted", "Moonlit", "Hundred-Flavour", "Dragon-Whisker", "Jade-Steamed", "Spirit-Simmered"},
        {"Phoenix-Feast", "Immortal's", "Ninefold-Simmered", "Heaven-Banquet", "Celestial", "Star-Dew"}};
    private static final String[] WINE_NAMES = {"Drunken Moon", "Nine-Year Sorrow", "Parting Cup", "Spring Thaw", "Courtyard Plum", "Midnight Lotus", "Old Friend", "Cloud-Treading", "Heron's Rest", "Sword-Washing", "Thousand-Cup", "First Frost"};
    private static final String[] POISON_NAMES = {"Widow's Kiss", "Seven-Step Dust", "Silent Moth", "Weeping Willow", "Last Supper", "Midnight Orchid", "Ninefold Sorrow", "Pale Whisper", "Bitter Plum", "Courtesan's Breath"};
    private static final String[] INSTR_NAMES = {"Autumn Rain", "Frozen Dragon", "Orphan Wutong", "Nine Cranes", "Moon Over Water", "Whispering Pine", "Lonely Phoenix", "River of Stars", "Spring Silk", "Echo of Bells", "Wandering Cloud", "Jade Dew"};
    private static String maybeSig(Character maker) { return maker != null && maker.lastName != null && rand().nextInt(100) < 22 ? maker.lastName + "'s " : ""; }
    static String dish(int lvl) { return dish(lvl, null); }
    static String wine(int lvl) { return wine(lvl, null); }
    static String poison(int lvl) { return poison(lvl, null); }
    static String instrument(int lvl) { return instrument(lvl, null); }
    /** "Mortal-Grade Lin's Steamed Spirit Grass Dumplings": the cook's signature sometimes, a method that follows the grade. */
    static String dish(int lvl, Character maker) { return grade(lvl) + "-Grade " + maybeSig(maker) + pickBand(METHODS, lvl) + " " + herbBase(lvl) + " " + pickOf(DISHES); }
    /** A vintage wine carries the year it was brewed, and sometimes a name of its own. */
    static String wine(int lvl, Character maker) {
        int year = GameDriver.time / 52;
        String base = rand().nextInt(100) < 30 ? pickOf(WINE_NAMES) + " " + pickOf(WINES) + " (" + herbBase(lvl) + ")" : maybeSig(maker) + herbBase(lvl) + " " + pickOf(WINES);
        return grade(lvl) + "-Grade " + base + ", Year " + year;
    }
    static String poison(int lvl, Character maker) {
        if (rand().nextInt(100) < 35) return grade(lvl) + "-Grade " + pickOf(POISON_NAMES) + " (" + herbBase(lvl) + " " + pickOf(POISONS) + ")";
        return grade(lvl) + "-Grade " + pickOf(POISON_ADJ) + " " + herbBase(lvl) + " " + pickOf(POISONS);
    }
    static String instrument(int lvl, Character maker) {
        String base = grade(lvl) + "-Grade " + pickBand(INSTR_ADJ, lvl) + " " + pickOf(INSTRUMENTS);
        return lvl >= 3 && rand().nextInt(100) < 35 ? base + " '" + pickOf(INSTR_NAMES) + "'" : base;
    }

    // ------------------------------------------------------------------ companions
    private static final String[] PET_SMALL = {"Biscuit", "Dumpling", "Mochi", "Pebble", "Sprout", "Button", "Noodle", "Bean", "Pip", "Tofu", "Peach", "Cricket", "Sesame", "Whisker", "Cloud"};
    private static final String[] PET_MID = {"Ember", "Thunderpaw", "Shadowtail", "Moonshard", "Ironhide", "Frostfang", "Windrunner", "Ashen", "Stormcaller", "Redclaw", "Duskwing", "Briar"};
    private static final String[] PET_HIGH = {"Skybreaker", "Voidwalker", "Dawnspear", "Starfall", "Worldeater", "Nine-Tails", "Heavenpiercer", "Everflame", "Soulrender", "Abyssal King"};
    /** The name a tamed beast is given: cute while it is small, grand when it is mighty. The species stays as its surname. */
    static String petName(Character beast) {
        String[] pool = beast.lvl <= 20 ? PET_SMALL : beast.lvl <= 60 ? PET_MID : PET_HIGH;
        return pool[rand().nextInt(pool.length)];
    }

    // ------------------------------------------------------------------ songs, festivals, tournaments, wars, tides, eras
    /** A song title for a piece of music (also used by Ambient); the mood picks the flavour. */
    static String songTitle(Random r, int mood) {
        String[] adj = {"Distant", "Moonlit", "Quiet", "Jade", "Autumn", "Misty", "Wandering", "Drifting", "Frosted", "Ancient", "Lonely", "Golden", "Hidden", "Nine-Fold", "Silent", "Evening"};
        String[] noun = {"Cranes", "Lanterns", "Pine Wind", "River", "Mountain Bells", "Plum Blossoms", "Immortals", "Tea House", "Mist", "Bamboo", "Clouds", "Orchids", "Boats", "Temple", "Spring", "Rain"};
        String[] place = {"the Jade River", "the Nine Peaks", "the Old Gate", "the Cloud Sea", "the Sleeping Valley", "Heaven's Ladder", "the Lotus Pond", "the Mirror Lake"};
        String[] dark = {"Ash on the Wind", "Blades in the Fog", "The Black Pass", "A Cold Road", "Where the Beasts Wait", "Embers and Bone", "The Last Lantern", "Thunder Over the Ridge"};
        String[] war = {"March of the Broken Banners", "Ten Thousand Swords", "The Drums of Dawn", "Blood on the Snow", "A Vow Before Battle"};
        String[] city = {"Market of a Thousand Lamps", "Rooftops at Noon", "The Bell Street", "Tea and Rumours", "Lanterns on the Canal"};
        String[] sect = {"Hymn of the Cloud Peak", "Morning Bell, Mountain Hall", "The Sword Garden", "Incense and Pine", "Disciples' Stair"};
        String[] cap = {"Hall of Ten Thousand Years", "The Nine-Tier Pagoda", "Dawn Over Originis", "Where All Roads Begin"};
        switch (mood) {
            case 2: return dark[r.nextInt(dark.length)];
            case 4: return war[r.nextInt(war.length)];
            case 1: return city[r.nextInt(city.length)];
            case 5: return sect[r.nextInt(sect.length)];
            case 6: return cap[r.nextInt(cap.length)];
            default:
                switch (r.nextInt(4)) {
                    case 0: return adj[r.nextInt(adj.length)] + " " + noun[r.nextInt(noun.length)];
                    case 1: return noun[r.nextInt(noun.length)] + " over " + place[r.nextInt(place.length)];
                    case 2: return "Song of the " + adj[r.nextInt(adj.length)] + " " + noun[r.nextInt(noun.length)];
                    default: return adj[r.nextInt(adj.length)] + " " + noun[r.nextInt(noun.length)] + " at Dusk";
                }
        }
    }

    /** A well-mixed 64-bit hash (neighbouring inputs give unrelated outputs; java.util.Random seeded with near-equal numbers would not). */
    static long mix64(long z) { z += 0x9E3779B97F4A7C15L; z = (z ^ (z >>> 30)) * 0xBF58476D1CE5E5B9L; z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL; return z ^ (z >>> 31); }
    private static Random seeded(String key, int n) { return new Random(mix64(mix64(Map.worldSeed ^ key.hashCode()) + n)); }

    private static final String[][] FEST_THEME = {
        {"Blossom", "Peach-Blossom", "Willow", "Swallow", "First-Rain", "Kite"},
        {"Lotus", "Dragon-Boat", "Firefly", "Sword-Cooling", "Cicada", "Lantern-River"},
        {"Harvest", "Mid-Autumn Moon", "Chrysanthemum", "Wine-Pressing", "Wild-Goose", "Mountain-Climbing"},
        {"Lantern", "Plum-Frost", "Hearth", "Long-Night", "Snow-Viewing", "Ancestor"}};
    private static final String[] FEST_ADJ = {"Grand", "Merry", "Golden", "Jade", "Evening", "Thousand-Lamp", "Old Style", "Great"};
    /** "Merry Peach-Blossom Festival" for the city's spring; the same city celebrates the same festival every year. */
    static String festival(int season, String city) {
        Random r = seeded(city + "|fest", season);
        String[] t = FEST_THEME[Math.max(0, Math.min(3, season))];
        return FEST_ADJ[r.nextInt(FEST_ADJ.length)] + " " + t[r.nextInt(t.length)] + " " + (r.nextInt(4) == 0 ? "Fair" : "Festival");
    }

    private static final String[] CONTEST_THING = {"Dragon", "Phoenix", "Crane", "Tiger", "Jade", "Thunder", "Lotus", "Sword", "Heavenly", "Nine-Peak", "Golden", "Iron"};
    private static final String[] CONTEST_KIND = {"Tournament", "Contest", "Trial of Arms", "Martial Games", "Grand Match", "Challenge"};
    /** The name of a tournament held in a place in a year ("Jade Dragon Tournament of Year 214"). */
    static String tournament(String place, int year) {
        Random r = seeded(place + "|tournament", year);
        return CONTEST_THING[r.nextInt(CONTEST_THING.length)] + " " + CONTEST_KIND[r.nextInt(CONTEST_KIND.length)] + " of Year " + year;
    }

    private static final String[] WAR_ADJ = {"Crimson", "Ashen", "Broken", "Thousand-Day", "Iron", "Winter", "Silent", "Burning", "Hollow", "Ninefold"};
    private static final String[] WAR_THING = {"Banner", "Lotus", "Mountain", "Sword", "Gate", "River", "Moon", "Seal", "Lantern", "Crown"};
    /** The war between two sects has one name whoever tells it; sides in the same order or not. */
    static String war(String a, String b) {
        String x = a.compareTo(b) <= 0 ? a + "|" + b : b + "|" + a;
        Random r = seeded(x, 7);
        return r.nextBoolean() ? "War of the " + WAR_ADJ[r.nextInt(WAR_ADJ.length)] + " " + WAR_THING[r.nextInt(WAR_THING.length)] : WAR_ADJ[r.nextInt(WAR_ADJ.length)] + " " + WAR_THING[r.nextInt(WAR_THING.length)] + " War";
    }

    /** A name for a beast tide, celestial trial or similar event at a place in a year. */
    static String event(String kind, String place, int year) {
        Random r = seeded(place + "|" + kind, year);
        String[] adj = {"Crimson", "Howling", "Black", "Endless", "Thousand-Fang", "Ashen", "Roaring", "Hungry", "Pale", "Thunder"};
        String[] heav = {"Nine-Heaven", "Azure Sky", "Starlit", "Thunder-Gate", "Jade Sky", "Golden Dawn", "Eight-Trigram", "Void-Gate"};
        if (kind.equals("trial")) return "The " + heav[r.nextInt(heav.length)] + " Trial";
        return "The " + adj[r.nextInt(adj.length)] + " Tide of Year " + year;
    }

    private static final String[] ERA_THING = {"Jade Sparrow", "Silent Moon", "Nine Rivers", "Iron Lotus", "Crimson Dawn", "Wandering Star", "Sleeping Dragon", "Thousand Bells", "White Crane", "Broken Sword", "Golden Peach", "Black Tortoise"};
    /** "Age of the Jade Sparrow": one name for each century of the world. */
    static String era(int century) { Random r = seeded("era", century); return "Age of the " + ERA_THING[r.nextInt(ERA_THING.length)]; }

    /** Region words shared with Regions. */
    static final String[] REG_ADJ = {"Misty", "Silent", "Azure", "Jade", "Thundering", "Forgotten", "Whispering", "Crimson", "Golden", "Endless", "Frozen", "Sunken", "Verdant", "Ashen", "Heavenly", "Hollow", "Ancient", "Weeping", "Nine-Cloud", "Moonlit"};
    static final String[] REG_THING = {"Dragons", "Phoenixes", "Cranes", "Immortals", "Ghosts", "Sages", "Tigers", "Stars", "Sorrows", "Winds", "Lanterns", "Fallen Gods", "Whales", "Ancestors"};

    static String herb(int lvl) { return HERBS[Math.max(0, Math.min(HERBS.length - 1, lvl - 1))] + " (Lv." + lvl + ")"; }
    static String metal(int lvl) { return METALS[Math.max(0, Math.min(METALS.length - 1, lvl - 1))] + " (Lv." + lvl + ")"; }
}
