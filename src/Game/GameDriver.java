package Game;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import javax.swing.SwingUtilities;
import static Game.Map.*;

public class GameDriver {

    static Scanner scan = new Scanner(System.in);
    static Random rand = new Random();
    static final boolean printBreakthroughs = false;
    static final boolean changeMc = true;
    public static int UID = 0;
    static boolean guiEnabled = false;
    static MapViewer mapViewer = null;
    static LinkedBlockingQueue<String> guiInputQueue = new LinkedBlockingQueue<>();
    static List<String> eventLog = Collections.synchronizedList(new ArrayList<>());
    static List<String> historyLog = Collections.synchronizedList(new ArrayList<>());
    static int currentSeason = 0; // 0=Spring 1=Summer 2=Autumn 3=Winter
    static volatile int tickDelayMs = 500;  // adjustable tick speed
    static volatile boolean paused = false; // pause flag for GUI speed controls

    static String readInput() {
        if (guiEnabled) {
            try { return guiInputQueue.take(); } catch (InterruptedException e) { return "x"; }
        }
        return scan.next();
    }
    static int readInt() {
        if (guiEnabled) {
            try { return Integer.parseInt(guiInputQueue.take().trim()); } catch (Exception e) { return 0; }
        }
        return scan.nextInt();
    }
    static void logEvent(String s) {
        if (s == null) return;
        eventLog.add(s);
        while (eventLog.size() > 30) eventLog.remove(0);
        // Push tagged world events to the persistent history log
        if (s.startsWith("[HERO]") || s.startsWith("[SUCCESSION]") || s.startsWith("[WORLD]")
                || s.contains("Beast Tide") || s.contains("Celestial Trial")
                || s.contains("Heaven's Will") || s.contains("spiritual storm")
                || s.contains("plague strikes") || s.contains("Heaven fire")
                || s.contains("Tournament at") && s.contains("wins")) {
            historyLog.add(s);
            if (historyLog.size() > 1000) historyLog.remove(0);
        }
    }

    // Intercepts System.out to also populate the GUI event log
    private static class TeeStream extends PrintStream {
        TeeStream(PrintStream wrapped) { super(wrapped, true); }
        @Override public void println(String s) { super.println(s); logEvent(s); }
        @Override public void println(Object o) { super.println(o); logEvent(o == null ? "null" : o.toString()); }
    }

    /** Interactive character editing; used at startup and in-game via 'e'. */
    static void editCharacterInteractive(Character c) {
        boolean editing = true;
        while (editing) {
            System.out.println("Edit: (N)ame (T)rait (P)otential (A)ffiliation (G)ender (D)one");
            System.out.println("  Current: " + c.name + " " + c.lastName
                    + "  Trait:" + c.getTrait() + "  Pot:" + c.potential
                    + "  Aff:" + c.affiliation + "  Gender:" + c.gender);
            char opt = readInput().charAt(0);
            switch (opt) {
                case 'N': case 'n':
                    System.out.println("First name:"); c.name = readInput();
                    System.out.println("Last name:");  c.lastName = readInput();
                    break;
                case 'T': case 't':
                    System.out.println("Trait: A=Alchemist B=Blacksmith T=Tailor S=Scribe I=Innovative L=Leader M=Merchant R=Runner G=Ghost Ga=Gatherer Mi=Miner Fr=Friendly Ck=Cheater Lk=Lucky Ib=IronBody Sv=SpiritVein Hr=HeavenlyRoot");
                    String tc = readInput().toLowerCase();
                    switch (tc) {
                        case "a":  c.trait = Character.Traits.Alchemist;    break;
                        case "b":  c.trait = Character.Traits.Blacksmith;   break;
                        case "t":  c.trait = Character.Traits.Tailor;       break;
                        case "s":  c.trait = Character.Traits.Scribe;       break;
                        case "i":  c.trait = Character.Traits.Innovative;   break;
                        case "l":  c.trait = Character.Traits.Leader;       break;
                        case "m":  c.trait = Character.Traits.Merchant;     break;
                        case "r":  c.trait = Character.Traits.Runner;       break;
                        case "g":  c.trait = Character.Traits.Ghost;        break;
                        case "ga": c.trait = Character.Traits.Gatherer;     break;
                        case "mi": c.trait = Character.Traits.Miner;        break;
                        case "fr": c.trait = Character.Traits.Friendly;     break;
                        case "ck": c.trait = Character.Traits.Cheater;      break;
                        case "lk": c.trait = Character.Traits.Lucky;        break;
                        case "ib": c.trait = Character.Traits.IronBody;     break;
                        case "sv": c.trait = Character.Traits.SpiritVein;   break;
                        case "hr": c.trait = Character.Traits.HeavenlyRoot; break;
                    }
                    break;
                case 'P': case 'p':
                    System.out.println("Potential (1-100):");
                    try { c.potential = Math.min(100, Math.max(1, Integer.parseInt(readInput()))); } catch (Exception ignored) {}
                    break;
                case 'A': case 'a':
                    System.out.println("Affiliation: G=Good N=Neutral E=Evil");
                    char ac = readInput().charAt(0);
                    if (ac == 'G' || ac == 'g') c.affiliation = "Good";
                    else if (ac == 'E' || ac == 'e') c.affiliation = "Evil";
                    else c.affiliation = "Neutral";
                    break;
                case 'G': case 'g':
                    System.out.println("Gender: M=Male F=Female");
                    char gc = readInput().charAt(0);
                    c.gender = (gc == 'M' || gc == 'm') ? "Male" : "Female";
                    break;
                case 'D': case 'd': editing = false; break;
            }
        }
    }

    /** Cheat menu: pick any character and modify stats/items/wealth. */
    static void handleCheatMenu(Character self) {
        System.out.println("=== CHEAT MENU ===  Target: (S)elf (I)D (N)earby");
        char targetOpt = readInput().charAt(0);
        Character target = null;
        if (targetOpt == 'S' || targetOpt == 's') {
            target = self;
        } else if (targetOpt == 'I' || targetOpt == 'i') {
            System.out.println("Enter character ID:");
            try {
                int cid = Integer.parseInt(readInput());
                if (cid >= 0 && cid < characters.size()) target = characters.get(cid);
            } catch (Exception ignored) {}
        } else if (targetOpt == 'N' || targetOpt == 'n') {
            System.out.println("Nearby characters:");
            int shown = 0;
            for (Character c : self.location.characters) {
                if (c != null && !c.dead && c != self && shown < 10) {
                    System.out.println("  ID:" + c.id + " " + c.name + " " + c.lastName + " Lv." + c.lvl);
                    shown++;
                }
            }
            System.out.println("Enter ID:");
            try {
                int cid = Integer.parseInt(readInput());
                if (cid >= 0 && cid < characters.size()) target = characters.get(cid);
            } catch (Exception ignored) {}
        }
        if (target == null || target.dead) { System.out.println("Target not found."); return; }
        final Character t = target;
        System.out.println("Target: " + t.name + " " + t.lastName + " Lv." + t.lvl);
        System.out.println("Cheat: (S)S wealth (L)evel (P)otential (H)eal (W)eapon (R)eset quests (K)ills (T)rack");
        char cheatOpt = readInput().charAt(0);
        switch (cheatOpt) {
            case 'S': case 's':
                System.out.println("Add how many Spirit Stones?");
                try { t.ss += Long.parseLong(readInput()); } catch (Exception ignored) {}
                System.out.println(t.name + " now has " + t.ss + " SS.");
                break;
            case 'L': case 'l':
                System.out.println("Set major level (0-10):");
                try {
                    int ml = Math.min(10, Math.max(0, Integer.parseInt(readInput())));
                    t.majorLvl = ml;
                    t.minorLvl = 1;
                    t.lvl = ml * 10 + 1;
                    t.tempLvl = t.lvl;
                } catch (Exception ignored) {}
                System.out.println(t.name + " is now major level " + t.majorLvl + ".");
                break;
            case 'P': case 'p':
                System.out.println("Set potential (1-100):");
                try { t.potential = Math.min(100, Math.max(1, Integer.parseInt(readInput()))); } catch (Exception ignored) {}
                break;
            case 'H': case 'h':
                t.cultivation = t.cultivationBaseMax;
                System.out.println(t.name + " cultivation restored to max.");
                break;
            case 'W': case 'w':
                System.out.println("Create weapon at level (1-10):");
                try {
                    int wlvl = Math.min(10, Math.max(1, Integer.parseInt(readInput())));
                    Item weapon = new Item(t, Item.WeaponType.values()[rand.nextInt(Item.WeaponType.values().length)], wlvl);
                    t.backpack.add(weapon);
                    t.equip();
                    System.out.println("Gave " + weapon + " to " + t.name + ".");
                } catch (Exception ignored) { System.out.println("Failed to create weapon."); }
                break;
            case 'R': case 'r':
                t.targetQuests.clear();
                t.spotQuests.clear();
                t.currentQuest = null;
                System.out.println(t.name + " quests cleared.");
                break;
            case 'K': case 'k':
                System.out.println("Set kill count:");
                try { t.kills = Integer.parseInt(readInput()); } catch (Exception ignored) {}
                break;
            case 'T': case 't':
                if (!trackedCharacters.contains(t.id)) {
                    trackedCharacters.add(t.id);
                    System.out.println(t.name + " " + t.lastName + " is now tracked.");
                } else {
                    System.out.println(t.name + " is already tracked.");
                }
                break;
        }
    }

    /** Reincarnation dialog: shown when a player character dies. */
    static void handleReincarnation(Character dead) {
        System.out.println("=== " + dead.name + " " + dead.lastName + " has died! Age " + dead.age + ", Realm " + dead.majorLvl + " ===");
        // Collect options
        Character killer = dead.reincarnationKiller;
        List<Character> aliveChildren = new ArrayList<>();
        for (Character child : dead.children) {
            if (child != null && !child.dead && child.born && child.trait != Character.Traits.Ghost) aliveChildren.add(child);
        }
        aliveChildren.sort((a, b) -> Integer.compare(b.lvl, a.lvl));

        System.out.println("Reincarnation options:");
        int opt = 1;
        if (killer != null && !killer.dead && killer.trait != Character.Traits.Ghost
                && killer.trait != Character.Traits.Beast && killer.trait != Character.Traits.Monster) {
            System.out.println("  " + opt + ". Take over killer: " + killer.name + " " + killer.lastName + " Lv." + killer.lvl);
        } else {
            killer = null; // invalid for takeover
        }
        int killerOpt = killer != null ? opt++ : 0;
        if (killerOpt > 0) {} // used above

        int childOpt = 0;
        if (!aliveChildren.isEmpty()) {
            opt = (killerOpt > 0) ? 2 : 1;
            System.out.println("  " + opt + ". Take over child: " + aliveChildren.get(0).name + " " + aliveChildren.get(0).lastName + " Lv." + aliveChildren.get(0).lvl);
            childOpt = opt;
            opt++;
        }
        int npcsOpt = opt;
        System.out.println("  " + opt + ". Choose any living NPC (enter ID)");
        opt++;
        int spectateOpt = opt;
        System.out.println("  " + opt + ". Spectate (follow mode on nearby NPC)");

        System.out.println("Choice:");
        int choice;
        try { choice = Integer.parseInt(readInput()); } catch (Exception e) { choice = spectateOpt; }

        Character newChar = null;
        if (choice == killerOpt && killerOpt > 0 && killer != null) {
            newChar = killer;
        } else if (choice == childOpt && childOpt > 0 && !aliveChildren.isEmpty()) {
            newChar = aliveChildren.get(0);
        } else if (choice == npcsOpt) {
            System.out.println("Enter character ID:");
            try {
                int cid = Integer.parseInt(readInput());
                if (cid >= 0 && cid < characters.size()) {
                    Character c = characters.get(cid);
                    if (c != null && !c.dead && c.trait != Character.Traits.Ghost) newChar = c;
                }
            } catch (Exception ignored) {}
            if (newChar == null) System.out.println("Invalid ID, switching to spectate.");
        }

        if (newChar != null) {
            newChar.player = 1;
            System.out.println("You are now " + newChar.name + " " + newChar.lastName + "!");
        } else {
            // Spectate: find any living NPC near old location
            for (Character c : dead.location.characters) {
                if (c != null && !c.dead && c.player == 0) { c.player = 2; break; }
            }
            System.out.println("Spectating. Press 'e' to take control of a tracked NPC or use the GUI Control button.");
        }
        dead.player = 0;
        dead.needsReincarnation = false;
        dead.reincarnationKiller = null;
    }


    public static void main(String[] args) throws InterruptedException, IOException {
        System.setOut(new TeeStream(System.out));
        Map map = new Map();
        int time = 0; // In Weeks

        System.out.println("Enable map viewer? Y/n");
        char guiInput = scan.next().charAt(0);
        if (guiInput == 'Y' || guiInput == 'y') {
            guiEnabled = true;
            try {
                SwingUtilities.invokeAndWait(() -> mapViewer = new MapViewer());
            } catch (Exception e) {
                System.out.println("Map viewer unavailable: " + e.getMessage());
                guiEnabled = false;
            }
        }

        // ── Startup flow ───────────────────────────────────────────────────────
        System.out.println("Mode: (S)ingle Player | (M)ultiplayer | (W)atch / Spectate");
        char modeInput = scan.next().charAt(0);
        int playerCount = 0;
        if (modeInput == 'S' || modeInput == 's') {
            playerCount = 1;
        } else if (modeInput == 'M' || modeInput == 'm') {
            System.out.println("How many players? (2-4)");
            try { playerCount = Math.min(4, Math.max(2, Integer.parseInt(scan.next()))); } catch (Exception e) { playerCount = 2; }
        } // else spectate: playerCount = 0

        for (int pi = 0; pi < playerCount; pi++) {
            if (playerCount > 1) System.out.println("\n=== Player " + (pi + 1) + " Setup ===");
            Character bete = new Character(tiles[range / 2][range / 2]);
            currentCharacter = bete;
            characters.add(bete);
            population++;
            bete.player = 1;
            bete.potential = 100;
            bete.trait = Character.Traits.Alchemist;
            System.out.println("Generated: " + bete.name + " " + bete.lastName + "  Trait:" + bete.getTrait() + "  Potential:" + bete.potential);
            System.out.println("Modify character? Y/n");
            char editChoice = scan.next().charAt(0);
            if (editChoice == 'Y' || editChoice == 'y') editCharacterInteractive(bete);
            System.out.println("" + bete + bete.location + "  Trait:" + bete.getTrait() + "  Potential:" + bete.potential);
        }
        if (playerCount == 0) {
            System.out.println("Spectating. Use 'Control' button in GUI or set player=2 follow-mode NPC.");
        }

        // NPC population
        for (int i = 0; i < startingPopulation; i++) {
            characters.add(new Character(tiles[range / 2][range / 2]));
        }


        while (true) {
            // Season update (Spring/Summer/Autumn/Winter change every 13 weeks)
            int newSeason = (time % 52) / 13;
            if (newSeason != currentSeason) {
                currentSeason = newSeason;
                String[] SEASON_NAMES = {"Spring", "Summer", "Autumn", "Winter"};
                System.out.println("=== " + SEASON_NAMES[currentSeason] + " begins ===");
            }
            if ((time) % 52 == 0) {
                int _year = (time / 52) + 1;
                System.out.println("It's year: " + _year + " Population: " + population + " " + monstersList);
                // World-state snapshot for verifying system activity
                long _sectCount = createdAreas.stream().filter(a -> a.sect != null).count();
                long _cityCount = createdAreas.stream().filter(a -> a.city).count();
                System.out.println("[WORLD] Areas: " + createdAreas.size() + " | Sects: " + _sectCount + " | Cities: " + _cityCount + " | Legacies: " + legacyTitles.size());
                // Snapshot city owners for succession detection this year
                java.util.HashMap<Integer, Integer> _cityOwnerSnap = new java.util.HashMap<>();
                for (Area area : createdAreas) {
                    if (area.city && area.cityOwner != null && !area.cityOwner.dead)
                        _cityOwnerSnap.put(area.index, area.cityOwner.id);
                }
                // Reset previous hotspots
                for (Area area : hotspotAreas) { area.lvl = area.baseLvl; area.hotspot = false; }
                hotspotAreas.clear();
                // Pick 3-5 new hotspot areas (non-city, not Originis)
                ArrayList<Area> eligible = new ArrayList<>();
                for (Area area : createdAreas) { if (!area.city && area.lvl > 0) eligible.add(area); }
                int hotCount = rand.nextInt(3) + 3;
                for (int _h = 0; _h < hotCount && !eligible.isEmpty(); _h++) {
                    Area pick = eligible.remove(rand.nextInt(eligible.size()));
                    pick.baseLvl = pick.lvl;
                    pick.lvl += rand.nextInt(20) + 10;
                    pick.hotspot = true;
                    hotspotAreas.add(pick);
                }
                // Resource disputes: sects competing for the same dungeon/hotspot areas escalate squabbles
                ArrayList<Organization> sectList = new ArrayList<>();
                for (Area area : createdAreas) {
                    if (area.sect != null && !sectList.contains(area.sect)) sectList.add(area.sect);
                }
                for (int _s1 = 0; _s1 < sectList.size(); _s1++) {
                    for (int _s2 = _s1 + 1; _s2 < sectList.size(); _s2++) {
                        Organization s1 = sectList.get(_s1);
                        Organization s2 = sectList.get(_s2);
                        if (s1.isAlly(s2) || s1.isAtWar() || s2.isAtWar()) continue;
                        if (s1.master == null || s2.master == null) continue;
                        for (Area a : s1.master.knownLocations) {
                            if (a == null || (!a.hotspot && a.dungeonType == null)) continue;
                            if (s2.master.knownLocations.contains(a)) {
                                s1.squabbles += a.hotspot ? 2 : 1; // hotspot disputes escalate faster
                                if (s1.squabbles >= 4) s1.declareWar(s2);
                                break;
                            }
                        }
                    }
                }
                // Succession crisis: tick down crisis timers
                for (Organization s : sectList) {
                    if (s.crisisTimer > 0) s.crisisTimer--;
                }
                // Sect quest boards refresh yearly
                for (Organization s : sectList) {
                    s.postMissionsYearly();
                }
                // City taxation: owners collect 1% of population as yearly ss income
                for (Area area : createdAreas) {
                    if (area.city) {
                        if (area.cityOwner != null && area.cityOwner.dead) area.cityOwner = null; // clear dead owner ref
                        if (area.cityOwner != null) {
                            area.cityOwner.ss += area.population / 100;
                            area.cityOwner.convertCurrency();
                        }
                    }
                }
                // City succession: detect and announce ownership changes
                for (Area area : createdAreas) {
                    if (!area.city) continue;
                    Integer prevId = _cityOwnerSnap.get(area.index);
                    Integer currId = (area.cityOwner != null) ? area.cityOwner.id : null;
                    if (prevId != null && !prevId.equals(currId)) {
                        String newName = (currId != null) ? (area.cityOwner.name + " " + area.cityOwner.lastName) : "no one";
                        System.out.println("[SUCCESSION] " + area.name + " has changed hands! New lord: " + newName);
                    } else if (prevId == null && currId != null) {
                        System.out.println("[SUCCESSION] " + area.name + " has a new founding lord: " + area.cityOwner.name + " " + area.cityOwner.lastName);
                    }
                }
                // Beast Tide: overflow Beast Lairs spread monsters to neighbors and threaten nearby cities
                int[] btDx = {-1, 1, 0, 0};
                int[] btDy = {0, 0, -1, 1};
                for (Area area : new ArrayList<>(createdAreas)) {
                    if (area.dungeonType == Area.DungeonType.BEAST_LAIR && area.monsters > area.lvl * 2000) {
                        int spread = area.monsters / 4;
                        for (int _d = 0; _d < 4; _d++) {
                            int nx = area.x + btDx[_d], ny = area.y + btDy[_d];
                            if (nx >= 0 && nx < range && ny >= 0 && ny < range && tiles[nx][ny] != null) {
                                Area nbr = tiles[nx][ny];
                                nbr.monsters += spread / 4;
                                if (nbr.city && nbr.population > 0) {
                                    long casualties = Math.min(nbr.population / 10, spread * 100L);
                                    // Defender bonus: cultivators in the city reduce casualties
                                    int defPower = 0;
                                    for (Character _def : new ArrayList<>(nbr.characters)) {
                                        if (_def != null && !_def.dead && _def.trait != Character.Traits.Beast && _def.trait != Character.Traits.Monster)
                                            defPower += _def.majorLvl + 1;
                                    }
                                    long saved = Math.min(casualties, defPower * 50L);
                                    casualties -= saved;
                                    nbr.population = (int) Math.max(1, nbr.population - casualties);
                                    String defMsg = (saved > 0) ? " (" + saved + " saved by " + defPower + " power of defenders)" : "";
                                    System.out.println("Beast Tide erupts from " + area.name + " and strikes " + nbr.name + "! -" + casualties + " population" + defMsg);
                                }
                            }
                        }
                        area.monsters = area.lvl * 1000;
                    }
                }
                // Celestial Trial: once per year, spawn a temporary high-level trial site
                if (!createdAreas.isEmpty()) {
                    ArrayList<Area> trialElig = new ArrayList<>();
                    for (Area area : createdAreas) {
                        if (!area.city && area.dungeonType == null && area.celestialTrialTimer == 0 && area.lvl >= 20) trialElig.add(area);
                    }
                    if (!trialElig.isEmpty()) {
                        Area trialArea = trialElig.get(rand.nextInt(trialElig.size()));
                        trialArea.dungeonType = Area.DungeonType.CELESTIAL_TRIAL;
                        trialArea.celestialTrialTimer = 10;
                        // Notify characters within 15 tiles
                        for (Character cc : characters) {
                            if (cc == null || cc.dead || !cc.born) continue;
                            if (Math.abs(cc.location.x - trialArea.x) + Math.abs(cc.location.y - trialArea.y) <= 15) {
                                if (!cc.knownLocations.contains(trialArea)) cc.knownLocations.add(trialArea);
                            }
                        }
                        System.out.println("A Celestial Trial has opened at " + trialArea.name + " [" + trialArea.x + "," + trialArea.y + "]! It will close in 10 ticks.");
                    }
                }
                // Heaven-Sent Treasure: every 3 years, a powerful technique descends into the world
                if ((time / 52) % 3 == 2) {
                    ArrayList<Area> htElig = new ArrayList<>();
                    for (Area area : createdAreas) {
                        if (!area.city && area.heavenlyTechnique == null) htElig.add(area);
                    }
                    if (!htElig.isEmpty()) {
                        Area htArea = htElig.get(rand.nextInt(htElig.size()));
                        Character phantom = null;
                        for (Character _ph : characters) { if (_ph != null && !_ph.dead) { phantom = _ph; break; } }
                        if (phantom == null) continue;
                        Technique heavenTech = new Technique(
                            rand.nextInt(2) == 0 ? Technique.techniqueType.Martial : Technique.techniqueType.Movement,
                            phantom, Math.max(5, rand.nextInt(10) + 5));
                        heavenTech.name = "Heaven-Sent " + heavenTech.name;
                        htArea.heavenlyTechnique = heavenTech;
                        // Broadcast to all characters within 20 tiles
                        for (Character cc : characters) {
                            if (cc == null || cc.dead || !cc.born) continue;
                            if (Math.abs(cc.location.x - htArea.x) + Math.abs(cc.location.y - htArea.y) <= 20) {
                                if (!cc.knownLocations.contains(htArea)) cc.knownLocations.add(htArea);
                            }
                        }
                        System.out.println("Heaven's Will descends! A heavenly technique has appeared at " + htArea.name + " [" + htArea.x + "," + htArea.y + "]!");
                    }
                }
                // Heaven Calamities (10% chance per year — storm, plague, or heaven fire)
                if (rand.nextInt(10) == 0 && !createdAreas.isEmpty()) {
                    switch (rand.nextInt(3)) {
                        case 0: { // Spiritual Storm — random area level surge
                            Area stormArea = createdAreas.get(rand.nextInt(createdAreas.size()));
                            stormArea.baseLvl = stormArea.lvl;
                            stormArea.lvl += rand.nextInt(20) + 10;
                            stormArea.hotspot = true;
                            hotspotAreas.add(stormArea);
                            System.out.println("A spiritual storm ravages " + stormArea.name + "! Level surges to " + stormArea.lvl + "!");
                            break;
                        }
                        case 1: { // Plague — kills ~30% of cultivators in a random populated area
                            ArrayList<Area> _popAreas = new ArrayList<>();
                            for (Area _a : createdAreas) { if (!_a.characters.isEmpty()) _popAreas.add(_a); }
                            if (!_popAreas.isEmpty()) {
                                Area plagueArea = _popAreas.get(rand.nextInt(_popAreas.size()));
                                int killed = 0;
                                for (Character _c : new ArrayList<>(plagueArea.characters)) {
                                    if (_c != null && !_c.dead && _c.trait != Character.Traits.Beast && _c.trait != Character.Traits.Monster && rand.nextInt(3) == 0) {
                                        _c.dead();
                                        killed++;
                                    }
                                }
                                if (killed > 0)
                                    System.out.println("A terrible plague strikes " + plagueArea.name + "! " + killed + " cultivators perish.");
                            }
                            break;
                        }
                        case 2: { // Heaven Fire — destroys arrays or culls beasts
                            Area fireArea = createdAreas.get(rand.nextInt(createdAreas.size()));
                            if (fireArea.arrayBag != null && !fireArea.arrayBag.isEmpty()) {
                                int destroyed = fireArea.arrayBag.size();
                                fireArea.arrayBag.clear();
                                System.out.println("Heaven fire descends on " + fireArea.name + "! " + destroyed + " array(s) destroyed!");
                            } else {
                                fireArea.monsters = (int)(fireArea.monsters * 0.7);
                                System.out.println("Heaven fire scorches " + fireArea.name + "! Beasts are culled.");
                            }
                            break;
                        }
                    }
                }
                // Auction House: cities with population >= 500 hold yearly auctions
                for (Area _aArea : new ArrayList<>(createdAreas)) {
                    if (!_aArea.city || _aArea.population < 500 || _aArea.backpack == null || _aArea.backpack.isEmpty()) continue;
                    _aArea.backpack.sort((a, b) -> Integer.compare(b.lvl, a.lvl));
                    int auctionCount = Math.min(3, _aArea.backpack.size());
                    for (int _ac = 0; _ac < auctionCount; _ac++) {
                        if (_aArea.backpack.isEmpty()) break;
                        Item aItem = _aArea.backpack.get(0);
                        long basePrice = (long)(Math.pow(aItem.lvl, 2.5) * 1000);
                        Character buyer = null;
                        long maxBid = basePrice;
                        for (Character _cc : characters) {
                            if (_cc == null || _cc.dead || !_cc.born) continue;
                            if (!_cc.knownLocations.contains(_aArea)) continue;
                            long wealth = _cc.ss + _cc.sc * 1000000L;
                            if (wealth > maxBid) { maxBid = wealth; buyer = _cc; }
                        }
                        if (buyer != null) {
                            long payment = Math.min(buyer.ss, maxBid);
                            buyer.ss -= payment;
                            buyer.backpack.add(aItem);
                            buyer.equip();
                            _aArea.backpack.remove(0);
                            if (_aArea.cityOwner != null) _aArea.cityOwner.ss += payment;
                            System.out.println("Auction at " + _aArea.name + ": " + buyer.name + " " + buyer.lastName + " wins " + aItem + " for " + payment + " SS!");
                        }
                    }
                }
                // Sect territory bonus: members spread across 3+ distinct tiles near sect location
                for (Organization _s : sectList) {
                    if (_s.location == null || _s.members == null) { _s.territoryBonus = 0; continue; }
                    java.util.Set<Area> controlled = new java.util.HashSet<>();
                    for (Character _m : _s.members) {
                        if (_m == null || _m.dead || _m.location == null) continue;
                        int dist = Math.abs(_m.location.x - _s.location.x) + Math.abs(_m.location.y - _s.location.y);
                        if (dist <= 5) controlled.add(_m.location);
                    }
                    _s.territoryBonus = controlled.size() >= 3 ? 1 : 0;
                }
                // Update reputation titles for all living characters once per year
                for (Character _cc : characters) {
                    if (_cc != null && !_cc.dead && _cc.born) _cc.updateRepTitle();
                }
            }
            // Tournament timer countdown and resolution (every tick)
            for (Area area : createdAreas) {
                if (area.tournamentTimer > 0) {
                    area.tournamentTimer--;
                    if (area.tournamentTimer == 0) {
                        area.runTournament();
                    }
                }
                // Celestial Trial despawn
                if (area.celestialTrialTimer > 0) {
                    area.celestialTrialTimer--;
                    if (area.celestialTrialTimer == 0) {
                        area.dungeonType = null;
                        System.out.println("The Celestial Trial at " + area.name + " has closed.");
                    }
                }
            }

            // Ages and Cultivates Players
            for (Character character : new ArrayList<>(characters)) {
                if (character == null || character.dead) continue;
                if (!character.dead) {
                    character.time();
                    if (character.born) {
                        character.cultivate();
                        if (character.sect != null && character.sect.founder.lvl > character.lvl) {
                            int bonus = Math.max(character.sect.founder.majorLvl, character.sect.sectLvl() / 10) + character.sect.territoryBonus;
                            for (int i = 0; i < bonus; i++) {
                                character.cultivate();
                            }
                        }
                        // Spiritual vein bonus (+1 cultivate/tick while occupying a vein tile)
                        if (character.location != null && character.location.spiritualVein) {
                            character.cultivate();
                        }
                        // Bloodline bonus (inherited from high-realm parents)
                        for (int _bl = 0; _bl < character.bloodlineBonus; _bl++) {
                            character.cultivate();
                        }
                    }
                }
            }

            // Causes Players to Move and Go Through Events

            for (Character character : new ArrayList<>(characters)) {
                if (character == null) continue;
                currentCharacter = character;
                if (!character.dead && character.born) {
                    if (character.player >= 1 && character.age >= 8) {
                        if (character.player == 2) {
                            character.movement();
                            System.out.println("[Follow] " + character + character.location);
                        } else {
                        if (character.skipping <= 0) {
                            System.out.println(character.backpack + "\n" + character.materialBag + "\n" + character.pillBag);
                            if (character.spouse != null) {
                                System.out.println("" + character.spouse + character.spouse.location);
                            }

                            switch (readInput().charAt(0)) {
                                case 'w':
                                    character.movement('w');
                                    break;
                                case 'a':
                                    character.movement('a');
                                    break;
                                case 's':
                                    character.movement('s');
                                    break;
                                case 'd':
                                    character.movement('d');
                                    break;
                                case 'i':
                                    System.out.println("=== " + character.name + " " + character.lastName + " ===");
                                    System.out.println("Level: " + character.lvl + "  (Major: " + character.majorLvl + "  Minor: " + character.minorLvl + ")");
                                    System.out.println("Cultivation: " + (int)character.cultivation + " / " + (int)character.cultivationBaseMax);
                                    System.out.println("Potential: " + character.potential + "  Age: " + character.age + " / " + character.maxAge);
                                    System.out.println("Affiliation: " + character.affiliation + "  Trait: " + character.getTrait());
                                    System.out.println("Profession Lvl: " + character.professionLvl);
                                    System.out.println("Wealth: " + character.ss + " SS  " + character.sc + " SC  " + character.dc + " DC  " + character.ij + " IJ");
                                    System.out.println("Weapon: " + (character.equippedWeapon != null ? character.equippedWeapon : "None"));
                                    System.out.println("Armor:  " + (character.equippedClothing != null ? character.equippedClothing : "None"));
                                    System.out.println("Martial Technique: " + character.martialTechnique + "  Movement Technique: " + character.movementTechnique);
                                    System.out.println("Kills: " + character.kills + "  Items Crafted: " + character.itemsCrafted + "  Sect Points: " + character.sectPoints);
                                    if (character.sect != null) System.out.println("Sect: " + character.sect + " [" + character.sect.getRank(character) + "]  Sect Lvl: " + character.sect.sectLvl() + "  Treasury: " + character.sect.treasury.size() + " items");
                                    character.movement('x');
                                    break;
                                case 'l':
                                    System.out.println("=== Known Locations (" + character.knownLocations.size() + ") ===");
                                    for (Area area : character.knownLocations) {
                                        if (area != null) {
                                            String info = area.name + " [" + area.x * 100 + ", " + area.y * 100 + "]";
                                            if (area.city) info += " (City)";
                                            if (area.sect != null) info += " [Sect: " + area.sect.name + "]";
                                            if (area.cityOwner != null) info += " [Owner: " + area.cityOwner.name + "]";
                                            System.out.println(info);
                                        }
                                    }
                                    character.movement('x');
                                    break;
                                case 'r':
                                    System.out.println("=== Relationships ===");
                                    for (Relationship rel : character.relationshipList.relationships) {
                                        Character friend = characters.get(rel.friendID);
                                        if (friend != null && !friend.dead) {
                                            String label = rel.value >= 3 ? "Best Friend" :
                                                           rel.value == 2 ? "Close Friend" :
                                                           rel.value == 1 ? "Friend" :
                                                           rel.value == 0 ? "Acquaintance" :
                                                           rel.value == -1 ? "Dislike" :
                                                           rel.value == -2 ? "Rival" : "Arch-Enemy";
                                            System.out.println(friend.name + " " + friend.lastName + " — " + label + " (" + rel.value + ")");
                                        }
                                    }
                                    if (character.spouse != null) System.out.println("Spouse: " + character.spouse.name + " " + character.spouse.lastName);
                                    character.movement('x');
                                    break;
                                case 'q':
                                    System.out.println("=== Quests ===");
                                    if (character.currentQuest != null) System.out.println("Active: " + character.currentQuest);
                                    System.out.println("Target quests (" + character.targetQuests.size() + "):");
                                    for (Quest q : character.targetQuests) System.out.println("  " + q);
                                    System.out.println("Spot quests (" + character.spotQuests.size() + "):");
                                    for (Quest q : character.spotQuests) System.out.println("  " + q);
                                    character.movement('x');
                                    break;
                                case 'e':
                                    System.out.println("=== Character Editor ===");
                                    editCharacterInteractive(character);
                                    character.movement('x');
                                    break;
                                case 'z':
                                    handleCheatMenu(character);
                                    character.movement('x');
                                    break;
                                case 'p':
                                    paused = !paused;
                                    System.out.println(paused ? "--- PAUSED --- (press 'p' again to resume)" : "--- RESUMED ---");
                                    character.movement('x');
                                    break;
                                case 'o':
                                    System.out.println("Auto-move options:");
                                    System.out.println("  x — Wait N turns (quest auto-runs)");
                                    System.out.println("  w/a/s/d — Move in direction N turns");
                                    System.out.println("  c — Move toward coordinate N turns");
                                    System.out.println("  m — Random movement N turns");
                                    char moveOpt = readInput().charAt(0);
                                    switch (moveOpt) {
                                        case 'x':
                                            character.movement = 'x';
                                            System.out.println("How many turns?");
                                            character.skipping = readInt() - 1;
                                            character.movement('x');
                                            break;
                                        case 'w': case 'a': case 's': case 'd':
                                            character.movement = moveOpt;
                                            System.out.println("How many turns?");
                                            character.skipping = readInt() - 1;
                                            character.movement(moveOpt);
                                            break;
                                        case 'c':
                                            System.out.println("Target tile X Y (0-" + (Map.range-1) + ", e.g. '15 15'):");
                                            character.autoX = readInt();
                                            character.autoY = readInt();
                                            character.movement = 'c';
                                            System.out.println("How many turns?");
                                            character.skipping = readInt() - 1;
                                            // first step toward coord
                                            int dx0 = character.autoX - character.location.x;
                                            int dy0 = character.autoY - character.location.y;
                                            if (dx0 == 0 && dy0 == 0) { character.autoX = -1; character.autoY = -1; character.movement = 'x'; character.movement('x'); }
                                            else if (Math.abs(dx0) >= Math.abs(dy0)) character.movement(dx0 > 0 ? 'd' : 'a');
                                            else character.movement(dy0 > 0 ? 's' : 'w');
                                            break;
                                        case 'm':
                                            character.movement = 'm';
                                            System.out.println("How many turns?");
                                            character.skipping = readInt() - 1;
                                            character.movement();
                                            break;
                                        default:
                                            character.movement('x');
                                    }
                                    break;
                                default:
                                    character.movement('x');
                                    break;
                            }
                        } else {
                            character.skipping--;
                            if (character.movement == 'm') {
                                character.movement();
                            } else if (character.movement == 'c' && character.autoX >= 0) {
                                int dxc = character.autoX - character.location.x;
                                int dyc = character.autoY - character.location.y;
                                if (dxc == 0 && dyc == 0) {
                                    character.autoX = -1; character.autoY = -1;
                                    character.movement = 'x';
                                    character.movement('x');
                                } else if (Math.abs(dxc) >= Math.abs(dyc)) {
                                    character.movement(dxc > 0 ? 'd' : 'a');
                                } else {
                                    character.movement(dyc > 0 ? 's' : 'w');
                                }
                            } else {
                                character.movement(character.movement);
                            }
                        }
                            if (character.skipping <= 0) {
                                System.out.println("" + character + character.location);
                            }
                        } // end else (non-followMode)
                        character.quest();
                    }
                    else if (character.age >= 8) {
                        character.movement();
                        character.quest();
                    }
                    else {
                        character.quest();
                    }

                    // For Mothers
                    if (character.gender.equals("Female") && !character.children.isEmpty()) {
                        for (Character child : character.children) {
                            if (child.age < 8) {
                                child.location.characters.remove(child);
                                child.location = child.mother.location;
                                if (!child.location.characters.contains(child)) {
                                    child.location.characters.add(child);
                                }
                                if (!child.knownLocations.contains(child.location)) {
                                    child.knownLocations.add(child.location);
                                }
                            }
                        }
                    }
                }
            }

            // Handle reincarnation for any player character that died this tick
            for (Character character : new ArrayList<>(characters)) {
                if (character != null && character.dead && character.needsReincarnation) {
                    handleReincarnation(character);
                }
            }

            // Ages the areas and adds the tempCharacters list to the characters list.

            for (Area area : createdAreas) {
                area.age();
            }
            for (Character monster : monstersList) {
                if (!monster.location.characters.isEmpty()) {
                    Character target = monster.location.characters.get(rand.nextInt(monster.location.characters.size()));
                    monster.battleCheck(target);
                }
                monster.movement();
            }
            time++;

            characters.addAll(tempCharacters);
            population += tempCharacters.size();

            if (!remCharacters.isEmpty()) {
                for (Character character : remCharacters) {
                    if (character.trait != Character.Traits.Beast && character.trait != Character.Traits.Monster) {
                        characters.set(character.id, null);
                        population--;
                    }
                }
            }
            monstersList.removeAll(remCharacters);
            monstersList.removeAll(awakenedMonsters);
            awakenedMonsters.clear();
            tempCharacters.clear();
            remCharacters.clear();
            if (guiEnabled && mapViewer != null) mapViewer.refresh();
            // Pause support: spin until unpaused, then sleep the configured delay
            while (paused) Thread.sleep(50);
            Thread.sleep(tickDelayMs);
        }
    }
}
