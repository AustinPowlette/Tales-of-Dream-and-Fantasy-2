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
    static Random rand = Long.getLong("tdf.seed") != null ? new Random(Long.getLong("tdf.seed")) : new Random(); // -Dtdf.seed=N (+ -Dtdf.threads=1): a reproducible world for before/after comparisons
    static final boolean printBreakthroughs = false;
    static final boolean changeMc = true;
    public static int UID = 0;
    static boolean guiEnabled = false;
    static MapViewer mapViewer = null;
    static LinkedBlockingQueue<String> guiInputQueue = new LinkedBlockingQueue<>();
    static final int EVENT_LOG_MAX = 400; // the GUI log filters over this many recent lines
    static List<String> eventLog = Collections.synchronizedList(new ArrayList<>());
    /** One row per game year for the GUI charts: {year, population, cities, sects, shops}. Not saved; it restarts from the loaded year. */
    static List<int[]> statHistory = Collections.synchronizedList(new ArrayList<>());
    static List<String> historyLog = Collections.synchronizedList(new ArrayList<>());
    static int currentSeason = 0; // 0=Spring 1=Summer 2=Autumn 3=Winter
    static volatile int tickDelayMs = 0;  // adjustable tick speed (ms per tick)
    static volatile boolean paused = false; // pause flag for GUI speed controls
    static volatile boolean waitingForInput = false; // true while blocked on player input
    static int time = 0;                    // global week counter (static so save/load can access it)

    /** Tests only: leave NPCs and roaming beasts where they are so a scripted test isn't disturbed by random world events between its steps. */
    static volatile boolean frozenNpcs = false;

    /** Runs a blocking wait with the world lock released (see World): the GUI can read the frozen world meanwhile. */
    static <T> T blocked(World.Blocking<T> b) {
        try { return World.whileBlocked(b); }
        catch (RuntimeException e) { throw e; }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    /** Shows something on the Swing thread and waits for it (dialogs) with the world lock released. */
    static void edtWait(Runnable r) throws Exception {
        World.whileBlocked(() -> { SwingUtilities.invokeAndWait(r); return null; });
    }

    static String readInput() {
        if (guiEnabled) {
            try { waitingForInput = true; return blocked(() -> guiInputQueue.take()); }
            catch (RuntimeException e) { return "x"; }
            finally { waitingForInput = false; }
        }
        return blocked(() -> scan.next());
    }
    static int readInt() {
        if (guiEnabled) {
            try { waitingForInput = true; return Integer.parseInt(blocked(() -> guiInputQueue.take()).trim()); }
            catch (Exception e) { return 0; }
            finally { waitingForInput = false; }
        }
        return blocked(() -> scan.nextInt());
    }
    /** Like readInput() but gives up after `seconds` (0 = wait forever) and returns null. Console: waits for typed input via
     *  System.in.available(), so it suits interactive play (piped multi-line input already buffered by the Scanner is not seen). */
    static String readInputTimed(int seconds) {
        if (seconds <= 0) return readInput();
        if (guiEnabled) {
            try { waitingForInput = true; return blocked(() -> guiInputQueue.poll(seconds, java.util.concurrent.TimeUnit.SECONDS)); }
            catch (RuntimeException e) { return null; }
            finally { waitingForInput = false; }
        }
        return blocked(() -> {
            long end = System.currentTimeMillis() + seconds * 1000L;
            while (System.currentTimeMillis() < end) {
                if (System.in.available() > 0) return scan.next();
                Thread.sleep(50);
            }
            return null;
        });
    }

    /** Reads a whole command line (GUI: whatever the buttons/command box sent; console: the next non-empty line). */
    /** True until the GUI has been shown the world once (start-up, and after a load): the first prompt refreshes it so the map and every panel are filled before the player has to act. */
    static volatile boolean guiStale = true;
    static volatile boolean digestPending = false;

    static String readLine() {
        if (guiEnabled) {
            if (guiStale && mapViewer != null) { guiStale = false; World.handoff(mapViewer::refreshNow, 400); }
            if (digestPending && mapViewer != null) { digestPending = false; if (Settings.digestOnLoad && time >= 52) javax.swing.SwingUtilities.invokeLater(() -> GuiMore.digestWindow(mapViewer, 52)); }
            try { waitingForInput = true; return blocked(() -> guiInputQueue.take()).trim(); }
            catch (RuntimeException e) { return "x"; }
            finally { waitingForInput = false; }
        }
        return blocked(() -> {
            while (true) {
                try {
                    String s = scan.nextLine().trim(); // the tail of a previous token-read line arrives empty: skip it
                    if (!s.isEmpty()) return s;
                } catch (NoSuchElementException e) {
                    return "x"; // input closed: stand still rather than crash
                }
            }
        });
    }

    /** Non-blocking read for characters running on their own (AUTO / scripts): a typed command such as 'stop', or null. */
    static String pollInput() {
        if (guiEnabled) return guiInputQueue.poll();
        try {
            if (System.in.available() > 0) return scan.nextLine().trim();
        } catch (Exception ignored) {}
        return null;
    }

    static void logEvent(String s) {
        if (s == null) return;
        eventLog.add(s);
        while (eventLog.size() > EVENT_LOG_MAX) eventLog.remove(0);
        // Push tagged world events to the persistent history log
        if (s.indexOf('\n') < 0 && (s.startsWith("[HERO]") || s.startsWith("[SUCCESSION]") || s.startsWith("[WORLD]")
                || s.contains("Beast Tide") || s.contains("Celestial Trial")
                || s.contains("Heaven's Will") || s.contains("spiritual storm")
                || s.contains("plague strikes") || s.contains("Heaven fire")
                || s.contains("Tournament at") && s.contains("wins"))) { // one-line events only: a multi-line info sheet that mentions a Celestial Trial is not a world event
            historyLog.add(s);
            if (historyLog.size() > 1000) historyLog.remove(0);
            Chronicle.add(s.startsWith("[HERO]") ? "legend" : "world", s.replaceFirst("^\\[[A-Z]+\\] ?", ""));
        }
    }

    // Intercepts System.out to also populate the GUI event log
    private static class TeeStream extends PrintStream {
        TeeStream(PrintStream wrapped) { super(wrapped, true); }
        @Override public void println(String s) { super.println(s); logEvent(s); }
        @Override public void println(Object o) { super.println(o); logEvent(o == null ? "null" : o.toString()); }
    }

    /** Show an info popup in GUI mode; fall back to stdout in terminal mode. */
    static void showGuiInfo(String title, String content) {
        if (!guiEnabled || mapViewer == null) { System.out.println(content); return; }
        GuiSheets.infoWindow(title, content); // non-modal and themed: nothing blocks the game or the other windows
    }

    /** Interactive character editing; used at startup and in-game via 'e'. */
    static void editCharacterInteractive(Character c) {
        if (guiEnabled && mapViewer != null) {
            boolean editing = true;
            while (editing) {
                String info = "Editing: " + c.name + " " + c.lastName
                        + "\nTrait: " + c.getTrait() + "  Potential: " + c.potential
                        + "\nAffiliation: " + c.affiliation + "  Gender: " + c.gender;
                String[] topOpts = {"Name", "Trait", "Potential", "Affiliation", "Gender", "Done"};
                final int[] sel = {5};
                try {
                    edtWait(() ->
                        sel[0] = javax.swing.JOptionPane.showOptionDialog(mapViewer, info, "Edit Character",
                            javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE,
                            null, topOpts, topOpts[5]));
                } catch (Exception ignored) {}
                switch (sel[0]) {
                    case 0: { // Name
                        final String[] nm = {c.name, c.lastName};
                        try {
                            edtWait(() -> {
                                String fn = javax.swing.JOptionPane.showInputDialog(mapViewer, "First name:", c.name);
                                if (fn != null && !fn.trim().isEmpty()) nm[0] = fn.trim();
                                String ln = javax.swing.JOptionPane.showInputDialog(mapViewer, "Last name:", c.lastName);
                                if (ln != null && !ln.trim().isEmpty()) nm[1] = ln.trim();
                            });
                        } catch (Exception ignored) {}
                        c.name = nm[0]; c.lastName = nm[1];
                        break;
                    }
                    case 1: { // Trait
                        Character.Traits[] allTraits = Character.Traits.values();
                        String[] traitNames = new String[allTraits.length];
                        for (int _i = 0; _i < allTraits.length; _i++) traitNames[_i] = allTraits[_i].toString();
                        final int[] tr = {-1};
                        try {
                            edtWait(() ->
                                tr[0] = javax.swing.JOptionPane.showOptionDialog(mapViewer, "Choose trait:", "Trait",
                                    javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE,
                                    null, traitNames, c.getTrait()));
                        } catch (Exception ignored) {}
                        if (tr[0] >= 0 && tr[0] < allTraits.length) c.trait = allTraits[tr[0]];
                        break;
                    }
                    case 2: { // Potential
                        final String[] potStr = {String.valueOf(c.potential)};
                        try {
                            edtWait(() -> {
                                String s = javax.swing.JOptionPane.showInputDialog(mapViewer, "Potential (1-100):", c.potential);
                                if (s != null && !s.trim().isEmpty()) potStr[0] = s.trim();
                            });
                        } catch (Exception ignored) {}
                        try { c.potential = Math.min(100, Math.max(1, Integer.parseInt(potStr[0]))); } catch (Exception ignored) {}
                        break;
                    }
                    case 3: { // Affiliation
                        String[] affOpts = {"Good", "Neutral", "Evil"};
                        final int[] aff = {1};
                        try {
                            edtWait(() ->
                                aff[0] = javax.swing.JOptionPane.showOptionDialog(mapViewer, "Affiliation:", "Affiliation",
                                    javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE,
                                    null, affOpts, c.affiliation));
                        } catch (Exception ignored) {}
                        if (aff[0] == 0) c.affiliation = "Good";
                        else if (aff[0] == 2) c.affiliation = "Evil";
                        else c.affiliation = "Neutral";
                        break;
                    }
                    case 4: { // Gender
                        String[] genOpts = {"Male", "Female"};
                        final int[] gen = {0};
                        try {
                            edtWait(() ->
                                gen[0] = javax.swing.JOptionPane.showOptionDialog(mapViewer, "Gender:", "Gender",
                                    javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE,
                                    null, genOpts, c.gender));
                        } catch (Exception ignored) {}
                        if (gen[0] == 0) c.gender = "Male";
                        else if (gen[0] == 1) c.gender = "Female";
                        break;
                    }
                    default: editing = false; break;
                }
            }
            return;
        }
        // Terminal mode
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
                    System.out.println("Trait: A=Alchemist B=Blacksmith T=Tailor S=Scribe I=Innovative L=Leader M=Merchant R=Runner G=Ghost Ga=Gatherer Mi=Miner Tm=Tamer Po=Poisoner Dr=Drunken Ch=Chef Mu=Musician Fr=Friendly Ck=Cheater Lk=Lucky Ib=IronBody Sv=SpiritVein Hr=HeavenlyRoot");
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
                        case "tm": c.trait = Character.Traits.Tamer;        break;
                        case "po": c.trait = Character.Traits.Poisoner;     break;
                        case "dr": c.trait = Character.Traits.Drunken;      break;
                        case "ch": c.trait = Character.Traits.Chef;         break;
                        case "mu": c.trait = Character.Traits.Musician;     break;
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

    // ------------------------------------------------------------------ per-character turns

    /** One NPC turn (also used for AUTO player characters and tracked NPCs): party logic, movement, quest routine. */
    static void npcTurn(Character npc) {
        if (npc.wanderingImmortal) { LoreEndings.wanderTurn(npc); return; }
        if (npc.age >= 8) {
            if (npc.partyLeaderId >= 0) {
                Character leader = Character.charById(npc.partyLeaderId);
                if (leader == null || leader.dead) {
                    npc.leaveParty();
                    npc.movement();
                    npc.quest();
                } else {
                    // Abandon party only if follower's top quest outprioritises the party's purpose
                    Quest topQ = npc.targetQuests.peek();
                    int leaderPriority = (leader.currentQuest != null) ? leader.currentQuest.priority : 0;
                    if (topQ != null && topQ.priority > leaderPriority
                            && topQ.targetArea != null && topQ.targetArea != leader.location) {
                        npc.leaveParty();
                        npc.movement();
                        npc.quest();
                    } else {
                        // Move toward leader; quest only when co-located
                        npc.pathfinder(leader.location);
                        if (npc.location == leader.location) npc.quest();
                    }
                }
            } else {
                if (npc.exploreTicks > 0) npc.movement(npc.exploreDir);
                else npc.movement();
                npc.quest();
            }
        } else {
            npc.quest();
        }
    }

    /** Children follow their mother's tile until they are old enough to walk on their own. */
    static void syncMotherChildren(Character mother) {
        if (mother.gender.equals("Female") && !mother.children.isEmpty()) {
            for (Character child : mother.children) {
                if (child.age < 8 && !child.dead) {
                    child.location.removeChar(child);
                    child.location = child.mother.location;
                    if (child.areaIdx < 0) {
                        child.location.addChar(child);
                    }
                    if (!child.knownLocations.contains(child.location)) {
                        child.knownLocations.add(child.location);
                    }
                }
            }
        }
    }

    /** One turn of a human-controlled adult: MANUAL prompts for commands; AUTO runs the NPC pipeline; HYBRID runs the script. */
    static void playerTurn(Character c) {
        PlayerControl pc = c.pc();
        pc.movedThisTick = false;
        pc.assignUids(c);
        Notifications.observe(c);
        Recap.ensureBase(c);
        Starts.tick(c); // objectives of a Story start
        Lore.tick(c);   // the Palace's story: objectives, ??? speaks
        if (pc.mode != PlayerControl.Mode.MANUAL) {
            // 'stop', 'manual', info and setup commands still work while the character runs on its own
            String cmd = pollInput();
            if (cmd != null && !cmd.isEmpty()) PlayerCommands.handle(c, cmd);
        }
        if (c.dead) return;
        if (pc.mode == PlayerControl.Mode.MANUAL) manualTurn(c, pc);
        else runOnItsOwn(c, pc);
    }

    static void manualTurn(Character character, PlayerControl pc) {
        Area before = character.location;
        if (pc.attackedThisTick) { // a fight found the character: stop any autopilot walk/wait so the player can react
            pc.attackedThisTick = false;
            pc.watching = false;
            if (Settings.pauseOn.contains("attack") && guiEnabled) paused = true;
            if (character.skipping > 0) {
                character.skipping = 0;
                System.out.println("You were attacked — autopilot cancelled.");
            }
        }
        final boolean prompted = character.skipping <= 0;
        if (prompted) {
            if (!TermHud.enabled()) { // the terminal shows a status block (TermHud, after any notices) instead of these plain lines; GUI / 'settings hud off' keep them
                System.out.println(character.backpack + "\n" + character.materialBag + "\n" + character.pillBag);
                if (character.spouse != null) {
                    System.out.println("" + character.spouse + character.spouse.location);
                }
                System.out.println("" + character + character.location);
            }
            if (Auctions.hasHouse(character.location) && character.location.auctionBag != null
                    && Auctions.weeksToNext(character.location) > 0 && Auctions.weeksToNext(character.location) <= 3)
                Notifications.onAuctionSoon(character, character.location, Auctions.weeksToNext(character.location));
            TermHud.show(character);
            boolean firstPrompt = true;
            while (true) { // info/setup commands don't use up the turn: keep asking until an action does
                TermHud.prompt(character, firstPrompt); firstPrompt = false;
                if (PlayerCommands.handle(character, readLine())) break;
                if (character.dead || pc.mode != PlayerControl.Mode.MANUAL) break;
            }
        } else {
            character.skipping--; // autopilot walk/wait chosen earlier
            if (character.movement == 'm') {
                character.movement();
            } else if (character.movement == 'c' && character.autoX >= 0) {
                PlayerCommands.stepAuto(character);
            } else {
                character.movement(character.movement);
            }
        }
        // Duty quests may walk the player only on a `watch` tick (the game fast-forwards while the duties work). A tick the player chose (an action, `stay`, `wait`, a walk, the arrival of a walk) is the player's own.
        pc.movedThisTick = character.location != before || prompted || !pc.watching;
        if (character.dead) return;
        if (pc.mode == PlayerControl.Mode.MANUAL) character.quest(); // duties that are toggled on, plus quest alerts
        else runOnItsOwn(character, pc);                            // the command switched modes this turn
    }

    static void runOnItsOwn(Character c, PlayerControl pc) {
        if (pc.mode == PlayerControl.Mode.AUTO) {
            c.skipping = 1; // routine gathering/crafting lines are silenced while running on its own (fights, deaths and breakthroughs still print)
            npcTurn(c);
            c.skipping = 0;
            if (c.dead) return;
            if (pc.autoTurns > 0 && --pc.autoTurns == 0) {
                pc.mode = PlayerControl.Mode.MANUAL;
                System.out.println("AUTO run finished — you have control again.");
            } else if (time % 13 == 0) {
                System.out.println("[Auto] " + c + c.location);
            }
        } else if (pc.mode == PlayerControl.Mode.HYBRID) {
            if (pc.script == null || !pc.script.running) { // nothing to run: hand control back
                pc.mode = pc.returnToAuto ? PlayerControl.Mode.AUTO : PlayerControl.Mode.MANUAL;
                return;
            }
            Area before = c.location;
            pc.script.tick(c);
            pc.movedThisTick = c.location != before;
            if (!c.dead) c.quest(); // duties that are toggled on, plus quest alerts
        }
    }

    /** Reincarnation dialog: shown when a player character dies. */
    static void handleReincarnation(Character dead) {
        String _cause;
        if (dead.reincarnationKiller != null) {
            Character k = dead.reincarnationKiller;
            String kName = k.name + " " + k.lastName;
            if (k.repTitle != null) kName += " '" + k.repTitle + "'";
            _cause = "Killed by " + kName + " (Lv." + k.lvl + ", " + k.getTrait() + ")";
        } else {
            _cause = dead.deathCause;
        }
        boolean leftWorld = _cause != null && (_cause.startsWith("left the Garden") || _cause.startsWith("became a"));
        if (leftWorld) System.out.println("=== " + dead.name + " " + dead.lastName + " is gone from the Garden. Age " + dead.age + ", Realm " + dead.majorLvl + " ===");
        else {
            System.out.println("=== " + dead.name + " " + dead.lastName + " has died! Age " + dead.age + ", Realm " + dead.majorLvl + " ===");
            System.out.println("    Cause: " + _cause);
        }
        RunStats.endLife(dead, difficulty >= 2 ? "died: " + _cause : _cause);
        if (difficulty >= 2) { // Brutal: death is the end of this life, with no way back
            RunStats.finish(dead, "died: " + _cause);
            String end = "Brutal: there is no rebirth and no taking over another body. " + dead.name + " " + dead.lastName + " lived " + dead.age + " years and reached Major Lvl " + dead.majorLvl + ".";
            System.out.println(end);
            if (guiEnabled && mapViewer != null) { try { edtWait(() -> javax.swing.JOptionPane.showMessageDialog(mapViewer, dead.name + " " + dead.lastName + " has died.\nCause: " + _cause + "\n\n" + end, "The end of this life", javax.swing.JOptionPane.PLAIN_MESSAGE)); } catch (Exception ignored) { } }
            for (Character c : dead.location.characters) { if (c != null && !c.dead && c.player == 0) { c.player = 2; break; } }
            dead.player = 0;
            dead.needsReincarnation = false;
            dead.reincarnationKiller = null;
            return;
        }
        // Collect options
        Character killer = dead.reincarnationKiller;
        List<Character> aliveChildren = new ArrayList<>();
        for (Character child : dead.children) {
            if (child != null && !child.dead && child.born && child.trait != Character.Traits.Ghost) aliveChildren.add(child);
        }
        aliveChildren.sort((a, b) -> Integer.compare(b.lvl, a.lvl));

        System.out.println("Your life has ended. What next?");
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

        // Flush stale GUI inputs so movement button presses don't pollute this prompt
        if (guiEnabled) guiInputQueue.clear();

        int choice;
        if (guiEnabled && mapViewer != null) {
            // Build option labels for the GUI dialog
            java.util.List<String> btnLabels = new java.util.ArrayList<>();
            if (killerOpt > 0 && killer != null)
                btnLabels.add(killerOpt + ". Take over killer: " + killer.name + " " + killer.lastName + " Lv." + killer.lvl);
            if (childOpt > 0 && !aliveChildren.isEmpty())
                btnLabels.add(childOpt + ". Take over child: " + aliveChildren.get(0).name + " " + aliveChildren.get(0).lastName + " Lv." + aliveChildren.get(0).lvl);
            btnLabels.add(npcsOpt + ". Choose any living NPC (enter ID)");
            btnLabels.add(spectateOpt + ". Spectate");
            final int[] result = {spectateOpt};
            try {
                edtWait(() -> {
                    int idx = javax.swing.JOptionPane.showOptionDialog(mapViewer,
                        dead.name + " " + dead.lastName + " has died!\nAge " + dead.age + ", Realm " + dead.majorLvl
                        + "\nCause: " + _cause
                        + "\n\nChoose what happens next:",
                        "After death",
                        javax.swing.JOptionPane.DEFAULT_OPTION,
                        javax.swing.JOptionPane.QUESTION_MESSAGE,
                        null,
                        btnLabels.toArray(),
                        btnLabels.get(btnLabels.size() - 1));
                    if (idx >= 0) {
                        // Map button index back to option number
                        String label = btnLabels.get(idx);
                        try { result[0] = Integer.parseInt(label.substring(0, label.indexOf('.'))); }
                        catch (Exception ignored) { result[0] = spectateOpt; }
                    }
                });
            } catch (Exception ignored) {}
            choice = result[0];
        } else {
            System.out.println("Choice:");
            try { choice = Integer.parseInt(readInput()); } catch (Exception e) { choice = spectateOpt; }
        }

        Character newChar = null;
        if (choice == killerOpt && killerOpt > 0 && killer != null) {
            newChar = killer;
        } else if (choice == childOpt && childOpt > 0 && !aliveChildren.isEmpty()) {
            newChar = aliveChildren.get(0);
        } else if (choice == npcsOpt) {
            if (guiEnabled && mapViewer != null) {
                // Swing dialogs must be created on the event thread (this method runs on the game thread)
                final String[] idHolder = {null};
                final String idPrompt = "Enter character ID (0 – " + (characters.size() - 1) + "):";
                try {
                    edtWait(() -> idHolder[0] = javax.swing.JOptionPane.showInputDialog(mapViewer,
                        idPrompt, "Choose NPC", javax.swing.JOptionPane.PLAIN_MESSAGE));
                } catch (Exception ignored) {}
                String idStr = idHolder[0];
                try {
                    int cid = Integer.parseInt(idStr == null ? "" : idStr.trim());
                    if (cid >= 0 && cid < characters.size()) {
                        Character c = characters.get(cid);
                        if (c != null && !c.dead && c.trait != Character.Traits.Ghost) newChar = c;
                    }
                } catch (Exception ignored) {}
            } else {
                System.out.println("Enter character ID:");
                try {
                    int cid = Integer.parseInt(readInput());
                    if (cid >= 0 && cid < characters.size()) {
                        Character c = characters.get(cid);
                        if (c != null && !c.dead && c.trait != Character.Traits.Ghost) newChar = c;
                    }
                } catch (Exception ignored) {}
            }
            if (newChar == null) System.out.println("Invalid ID, switching to spectate.");
        }

        if (newChar != null) {
            newChar.player = 1;
            RunStats.track(newChar);
            if (dead.pc != null) newChar.pc().inheritFrom(dead.pc); // duty toggles, policies and muted quests carry over; scripts and alerts do not
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


    // Population control
    // A tile's capacity is a fixed share of the STARTING population (never the current one, so a booming world can't raise its own ceiling),
    // scaled by map area: CROWD_PERCENT of the starting population on the reference 31x31 map, proportionally less on a bigger map
    // (people spread over more tiles) and more on a smaller one. Equivalent to CROWD_PERCENT * 961 times the even-spread density.
    static final double CROWD_PERCENT = 0.02;
    static final int CROWD_REFERENCE_TILES = 31 * 31;
    static int difficulty = 0;                    // world difficulty chosen at the start: 0 Story/Standard, 1 Hard, 2 Brutal (see Starts)
    static final int START_AGE = 18;              // age of the first generation (players and NPCs); cultivators born later start at 8
    static final int TRUCE_WEEKS = 52;            // nobody may start a fight with another cultivator in Originis during the first game year
    static final int CROWD_MIN_CAPACITY = 20;     // floor so tiny test populations don't turn every tile into a hotspot
    static final int CROWD_CAPACITY = Math.max(CROWD_MIN_CAPACITY,
            (int) Math.round(startingPopulation * CROWD_PERCENT * CROWD_REFERENCE_TILES / (double) (range * range)));  // wild tile
    static final int CROWD_CAPACITY_CITY = CROWD_CAPACITY * 4;   // cities support more (Originis is exempt entirely)
    static final int CROWD_GRACE_YEARS = 10;      // the founding crowd at Originis is given time to disperse first
    static final double CROWD_CLEAR_RATIO = 0.8;  // the hotspot ends once the tile is back under 80% of capacity
    static final double CROWD_ATTACK_RATE = 0.02; // beast ambushes per tick = 2% of the excess cultivators
    static final int CROWD_MAX_ATTACKS = 40;      // cap on ambushes per tile per tick
    static final int CROWD_SURGE = 15;            // level surge while a tile is a crowd hotspot
    static final int EPOCH_POPULATION = 200000;   // living cultivators that trigger the epoch purge
    static final int EPOCH_BATCH = 3000;          // purge deaths processed per tick
    static ArrayList<Character> epochVictims = new ArrayList<>();

    // Java serialization recurses through the character/area object graph (knownLocations <-> Area.characters),
    // which gets very deep once the world is explored; run it on a thread with a large stack.
    static final long BIG_STACK = 1L << 30;

    /** Copies the static world state into a SaveData (game thread, between ticks). */
    static SaveData snapshot() {
        SaveData d = new SaveData();
        d.population      = population;
        d.tiles           = tiles;
        d.characters      = characters;
        d.createdAreas    = createdAreas;
        d.cityAreas       = cityAreas;
        d.monstersList    = monstersList;
        d.hotspotAreas    = hotspotAreas;
        d.legacyTitles    = legacyTitles;
        d.legacyLvls      = legacyLvls;
        d.heroEnsoulment  = heroEnsoulment;
        d.heroAscension   = heroAscension;
        d.heroPseudoImmortal = heroPseudoImmortal;
        d.trackedCharacters  = trackedCharacters;
        d.areaUindex      = Area.uindex;
        d.time            = time;
        d.currentSeason   = currentSeason;
        d.uid             = UID;
        d.historyLog      = new ArrayList<>(historyLog);
        d.tickDelayMs     = tickDelayMs;
        d.yearlyDone      = yearlyDone;
        d.difficulty      = difficulty;
        Chronicle.snapshotInto(d);
        RunStats.snapshotInto(d);
        Lore.snapshotInto(d);
        d.worldSeed       = Map.worldSeed;
        d.savedAt         = System.currentTimeMillis();
        StringBuilder who = new StringBuilder();
        for (Character c : characters) {
            if (c != null && c.player == 1 && !c.dead) { if (who.length() > 0) who.append(", "); who.append(c.name).append(' ').append(c.lastName).append(" (Major ").append(c.majorLvl).append(')'); }
        }
        d.playerSummary = who.toString();
        return d;
    }

    // Saves are only written between ticks (temp/removal queues are empty then). Anything else - the GUI button, the 'f' key -
    // just raises this flag and the game loop performs the save at the top of the next tick.
    static volatile boolean saveRequested = false;
    static volatile boolean quitRequested = false;   // `quit` / the Quit button: the game saves (unless told not to) at the end of the turn and exits
    static volatile boolean quitSaves = true;
    static boolean yearlyDone = false;      // true once this tick's yearly block has run
    static boolean skipYearlyOnce = false;  // set after loading a save whose yearly block had already run

    /** Puts a loaded SaveData into the static world state (game thread, between ticks, or at start-up). */
    static void applySnapshot(SaveData d) {
        population       = d.population;
        tiles            = d.tiles;
        characters       = d.characters;
        createdAreas     = d.createdAreas;
        cityAreas        = d.cityAreas;
        monstersList     = d.monstersList;
        hotspotAreas     = d.hotspotAreas;
        legacyTitles     = d.legacyTitles;
        legacyLvls       = d.legacyLvls;
        heroEnsoulment   = d.heroEnsoulment;
        heroAscension    = d.heroAscension;
        heroPseudoImmortal = d.heroPseudoImmortal;
        trackedCharacters  = d.trackedCharacters;
        Area.uindex      = d.areaUindex;
        time             = d.time;
        currentSeason    = d.currentSeason;
        UID              = d.uid;
        historyLog.clear(); historyLog.addAll(d.historyLog);
        tickDelayMs      = d.tickDelayMs;
        skipYearlyOnce   = d.yearlyDone && time % 52 == 0; // otherwise taxes, calamities, hotspots and the auction all fired twice for the loaded year
        // transient per-run state that must not leak from the world that was replaced
        tempCharacters.clear(); remCharacters.clear(); awakenedMonsters.clear(); epochVictims.clear();
        statHistory.clear();
        difficulty = d.difficulty;
        Chronicle.restoreFrom(d);
        RunStats.restoreFrom(d);
        Lore.restoreFrom(d);
        Map.worldSeed     = d.worldSeed != 0 ? d.worldSeed : (Map.worldSeed | 1L);
        Naming.rebuild(); // unique-name registries follow the loaded world
        // Rebuild areaIdx for all characters (not serialized; handles old saves)
        for (Area a : createdAreas) {
            for (int _i = 0; _i < a.characters.size(); _i++) {
                Character _c = a.characters.get(_i);
                if (_c != null) _c.areaIdx = _i;
            }
        }
        // quest uids are per-quest serialized values but the counter is static: continue after the highest one in the save (old saves have 0)
        int _maxUid = 0;
        for (Character _c : characters) {
            if (_c == null) continue;
            for (Quest _q : _c.targetQuests) _maxUid = Math.max(_maxUid, _q.uid);
            for (Quest _q : _c.spotQuests) _maxUid = Math.max(_maxUid, _q.uid);
            if (_c.currentQuest != null) _maxUid = Math.max(_maxUid, _c.currentQuest.uid);
        }
        Quest.nextUid = Math.max(Quest.nextUid, _maxUid);
        for (Character _c : characters) {
            if (_c == null || _c.player < 1) continue; // only human characters' quests are addressed by uid
            for (Quest _q : _c.targetQuests) if (_q.uid == 0) _q.uid = ++Quest.nextUid;
            for (Quest _q : _c.spotQuests) if (_q.uid == 0) _q.uid = ++Quest.nextUid;
            if (_c.currentQuest != null && _c.currentQuest.uid == 0) _c.currentQuest.uid = ++Quest.nextUid;
        }
    }

    /** Pending requests, honoured at the top of the next tick (the world is consistent then). */
    static volatile String saveRequestName = null;     // named slot save
    static volatile String loadRequest = null;         // slot to load

    public static void main(String[] args) throws InterruptedException, IOException {
        System.setOut(new TeeStream(System.out));
        World.gameStart(); // the game thread owns the world; it lets go whenever it waits (see World)
        Settings.load();
        Ambient.apply();
        SaveManager.installCrashHandler();

        System.out.println("Enable map viewer? Y/n");
        char guiInput = scan.next().charAt(0);
        if (guiInput == 'Y' || guiInput == 'y') {
            guiEnabled = true;
            try {
                edtWait(Theme::install);
                edtWait(() -> mapViewer = new MapViewer());
            } catch (Exception e) {
                System.out.println("Map viewer unavailable: " + e.getMessage());
                guiEnabled = false;
            }
        }

        // ── Load existing save or start fresh ─────────────────────────────────
        boolean loaded = false;
        String slotChoice = SaveManager.chooseAtStartup();
        if (slotChoice != null) loaded = SaveManager.load(slotChoice);
        if (loaded) {
            System.out.println("Resuming. Player characters in world: "
                    + characters.stream().filter(c -> c != null && c.player >= 1 && !c.dead).count());
        } else {
            new Map(); // initialize the world
        }
        if (loaded) { while (true) { /* jump straight to main loop below */ break; } }

        // ── Startup flow (new game only) ───────────────────────────────────────
        if (!loaded) {
        char modeInput = StartMenu.askMode();
        int playerCount = 0;
        if (modeInput == 'S' || modeInput == 's') {
            playerCount = 1;
        } else if (modeInput == 'M' || modeInput == 'm') {
            playerCount = StartMenu.askPlayerCount();
        } // else spectate: playerCount = 0

        for (int pi = 0; pi < playerCount; pi++) {
            if (playerCount > 1) System.out.println("\n=== Player " + (pi + 1) + " Setup ===");
            Character bete = new Character(tiles[range / 2][range / 2]);
            bete.age = START_AGE;
            currentCharacter = bete;
            characters.add(bete);
            population++;
            bete.player = 1;
            Starts.Choice start = StartMenu.choose(pi, playerCount);
            Starts.apply(bete, start);
            System.out.println("Generated: " + bete.name + " " + bete.lastName + "  Trait:" + bete.getTrait() + "  Potential:" + bete.potentialLabel());
            if (start.kind == Starts.Kind.STANDARD_CUSTOM) editCharacterInteractive(bete);
            else {
                if (StartMenu.askYesNo("Your character", bete.name + " " + bete.lastName + " (" + bete.getTrait() + ", potential " + bete.potentialLabel() + "). Modify the character?", false)) editCharacterInteractive(bete);
            }
            if (start.kind == Starts.Kind.STANDARD_CUSTOM) bete.potentialHidden = false;
            Starts.apply2(bete);
            if (Settings.matureStart && start.kind != Starts.Kind.STORY) Starts.prehistoryPlayer(bete); // the same ten years of cultivation everybody else had
            System.out.println("" + bete + bete.location + "  Trait:" + bete.getTrait() + "  Potential:" + bete.potentialLabel());
            if (bete.story != null) System.out.println(Starts.describe(bete));
            if (pi == 0) RunStats.begin(bete, start.kind == Starts.Kind.STORY ? "story: " + start.story : start.kind.name().toLowerCase().replace('_', ' '), difficulty); else RunStats.track(bete);
        }
        if (playerCount == 0) {
            System.out.println("Spectating. Use 'Control' button in GUI or set player=2 follow-mode NPC.");
        }

        // NPC population
        List<Character> firstGeneration = new ArrayList<>(startingPopulation);
        // The crowd is spread over Originis and the eight tiles around it (about a ninth each) so the first turns are not one tile with everybody in it
        Area[] zone = new Area[9];
        {
            int cx = range / 2, cy = range / 2;
            if (currentCharacter == null) { // the tile constructor reads currentCharacter (a spectator start has none yet): a stand-in that leaves no trace (it used to take id 0 and shift every id by one)
                Character stand = new Character(tiles[cx][cy]);
                stand.location.removeChar(stand);
                Naming.releaseName(stand.name, stand.lastName);
                UID--;
                currentCharacter = stand;
            }
            // {dx, dy, parent dx, parent dy, direction from the parent}: the four sides first, then the corners from the north and south tiles
            int[][] steps = {{1, 0, 0, 0, 'e'}, {-1, 0, 0, 0, 'w'}, {0, 1, 0, 0, 'n'}, {0, -1, 0, 0, 's'}, {1, 1, 0, 1, 'e'}, {-1, 1, 0, 1, 'w'}, {1, -1, 0, -1, 'e'}, {-1, -1, 0, -1, 'w'}};
            for (int[] st : steps) {
                if (tiles[cx + st[0]][cy + st[1]] == null) tiles[cx + st[0]][cy + st[1]] = new Area(tiles[cx + st[2]][cy + st[3]], (char) st[4]);
            }
            int k = 0;
            for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) zone[k++] = tiles[cx + dx][cy + dy];
        }
        Lore.state = null; Lore.newWorld(); // the Palace of the Oldest Order stands at Originis (no-op with `settings lore off`)
        Area[] wide = null; // the first generation is spread uniformly over the N x N tiles around Originis (N odd; Settings.startSpread, default 15, only in big worlds where the crowd matters: nine tiles holding 11,000 each made the first ticks take 10 s; 0 = the old 3 x 3 start zone; -Dtdf.spread=N overrides)
        int spreadN = Integer.getInteger("tdf.spread", startingPopulation >= 20000 ? Settings.startSpread : 0);
        if (spreadN > 3) {
            int h = Math.min(spreadN, range) / 2;
            List<int[]> cells = new ArrayList<>();
            for (int dx = -h; dx <= h; dx++) for (int dy = -h; dy <= h; dy++) cells.add(new int[]{dx, dy});
            cells.sort((p, q) -> { int rp = Math.max(Math.abs(p[0]), Math.abs(p[1])), rq = Math.max(Math.abs(q[0]), Math.abs(q[1])); return rp != rq ? rp - rq : Math.abs(p[0]) != Math.abs(q[0]) ? Math.abs(p[0]) - Math.abs(q[0]) : Math.abs(p[1]) - Math.abs(q[1]); });
            int cx = range / 2, cy = range / 2;
            wide = new Area[cells.size()];
            for (int k = 0; k < cells.size(); k++) {
                int dx = cells.get(k)[0], dy = cells.get(k)[1];
                if (tiles[cx + dx][cy + dy] == null) {
                    int px = dx != 0 ? dx - Integer.signum(dx) : dx, py = dx != 0 ? dy : dy - Integer.signum(dy);
                    char dir = dx != 0 ? (dx > 0 ? 'e' : 'w') : (dy > 0 ? 'n' : 's');
                    tiles[cx + dx][cy + dy] = new Area(tiles[cx + px][cy + py], dir);
                }
                wide[k] = tiles[cx + dx][cy + dy];
            }
        }
        for (int i = 0; i < startingPopulation; i++) {
            Character starter = new Character(wide != null ? wide[rand.nextInt(wide.length)] : Boolean.getBoolean("tdf.nospread") ? zone[4] : zone[rand.nextInt(9)]); // -Dtdf.nospread=true: everybody in Originis (for timing comparisons)
            starter.age = START_AGE; // the first generation begins as young adults so they can marry and have children straight away
            characters.add(starter);
            firstGeneration.add(starter);
        }
        Starts.prehistory(firstGeneration); // ...and with the cultivation they did between 8 and 18 (the player starts at level 1)
        } // end if (!loaded)
        digestPending = loaded; // a loaded game opens with a digest of the last year


        while (true) {
            if (loadRequest != null) { // load a slot into the running game; everything below then continues from the loaded world
                String slot = loadRequest; loadRequest = null;
                if (SaveManager.load(slot)) {
                    Notifications.reset();
                    guiStale = true; digestPending = true;
                    if (guiEnabled && mapViewer != null) World.handoff(mapViewer::refreshNow, 250);
                }
            }
            Recap.check();
            if (time >= LoreEndings.worldEndWeek && !LoreEndings.worldEnded) LoreEndings.worldEnd(); // the last week the counter can hold: ??? goes out and the game ends instead of overflowing
            if (saveRequested) { saveRequested = false; SaveManager.save(SaveManager.QUICK, SaveManager.Kind.QUICK); } // between ticks: nothing is pending in tempCharacters/remCharacters
            if (saveRequestName != null) { String n = saveRequestName; saveRequestName = null; SaveManager.save(n, SaveManager.Kind.MANUAL); }
            if (quitRequested && !(LoreEndings.worldEnded && GuiLore.busy())) { // saved (or not) above: leave (after the Garden's end the ending and credits windows are read first)
                try { Settings.save(); } catch (RuntimeException ignored) { }
                System.out.println("Goodbye.");
                System.exit(0);
            }
            Perf.begin();
            boolean heavenNow = false;
            for (int hi = 0, hn = Map.createdAreas.size(); hi < hn && !heavenNow; hi++) { Area ha = Map.createdAreas.get(hi); if (ha != null && ha.hasHeaven()) heavenNow = true; }
            Map.heavenExists = heavenNow;
            final boolean skipYearly = skipYearlyOnce;
            skipYearlyOnce = false;
            // Season update (Spring/Summer/Autumn/Winter change every 13 weeks)
            int newSeason = (time % 52) / 13;
            if (newSeason != currentSeason) {
                currentSeason = newSeason;
                String[] SEASON_NAMES = {"Spring", "Summer", "Autumn", "Winter"};
                System.out.println("=== " + SEASON_NAMES[currentSeason] + " begins ===");
                Seasonal.festivals(currentSeason);
            }
            if ((time) % 52 == 0 && !skipYearly) {
                int _year = (time / 52) + 1;
                System.out.println("It's year: " + _year + " Population: " + population);
                // World-state snapshot for verifying system activity
                long _sectCount = createdAreas.stream().filter(a -> a.sect != null).count();
                long _cityCount = createdAreas.stream().filter(a -> a.city).count();
                {
                    int _shops = 0;
                    for (Area _a : createdAreas) _shops += _a.shops.size();
                    statHistory.add(new int[]{_year, population, (int) _cityCount, (int) _sectCount, _shops});
                    while (statHistory.size() > 2000) statHistory.remove(0);
                }
                System.out.println("[WORLD] Areas: " + createdAreas.size() + " | Sects: " + _sectCount + " | Cities: " + _cityCount + " | Legacies: " + legacyTitles.size());
                // Snapshot city owners for succession detection this year
                java.util.HashMap<Integer, Integer> _cityOwnerSnap = new java.util.HashMap<>();
                for (Area area : createdAreas) {
                    if (area.city && area.cityOwner != null && !area.cityOwner.dead)
                        _cityOwnerSnap.put(area.index, area.cityOwner.id);
                }
                // Reset previous hotspots
                for (Area area : hotspotAreas) { area.lvl = area.baseLvl + area.crowdSurge; area.hotspot = area.crowdHotspot; } // keep a live crowd surge/flag intact
                hotspotAreas.clear();
                // Pick 3-5 new hotspot areas (non-city, not Originis)
                ArrayList<Area> eligible = new ArrayList<>();
                for (Area area : createdAreas) { if (!area.city && area.lvl > 0 && !area.hotspot) eligible.add(area); } // never re-pick a tile that is already surged
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
                // City taxation: owners collect half of 1% of population; other half goes to city treasury
                for (Area area : createdAreas) {
                    if (area.city) {
                        if (area.cityOwner != null && area.cityOwner.dead) area.cityOwner = null; // clear dead owner ref
                        long tax = Math.max(0, area.population) / 100;
                        area.cityTreasury += tax / 2;
                        if (area.cityOwner != null) {
                            area.cityOwner.ss += tax - tax / 2;
                            area.cityOwner.convertCurrency();
                        }
                    }
                }
                // Sect cash income: sect population feeds sect treasury (1% of sectPopulation/year)
                for (Organization s : sectList) {
                    s.sectCash += s.sectPopulation / 100;
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
                    if (area.dungeonType == Area.DungeonType.BEAST_LAIR && area.monsters > area.lvl * 2000 && rand.nextInt(5) == 0) {
                        System.out.println("A Beast Tide erupts from " + area.name + "! Monsters spill into the surrounding lands (" + Naming.event("tide", area.name, time / 52) + (Regions.regionOf(area) == null ? "" : ", " + Regions.regionOf(area)) + ").");
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
                                    System.out.println("The tide strikes " + nbr.name + "! -" + casualties + " population" + defMsg);
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
                // Heaven-Sent Treasure: every year one treasure (a technique, instrument, poison, pill, clothes, weapon or array) descends on a wild tile
                if (!Treasure.yearly()) { time++; continue; } // nobody alive: still advance time or this loop spins forever
                for (Area _la : createdAreas) if (_la != null && _la.libraries != null) for (Library _lb : _la.libraries) _lb.compact(); // one book for each type and potential
                // Heaven Calamities (10% chance per year — storm, plague, or heaven fire; grace period first 18 years)
                if (_year > 18 && rand.nextInt(10) == 0 && !createdAreas.isEmpty()) {
                    switch (rand.nextInt(3)) {
                        case 0: { // Spiritual Storm — random area level surge
                            Area stormArea = createdAreas.get(rand.nextInt(createdAreas.size()));
                            if (stormArea.hotspot) break; // already surged: a second surge would bake the first one into baseLvl permanently
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
                // Epoch purge: when the living population reaches the threshold, 10% of each major level (levels with < 10 cultivators are spared) are marked to die
                if (epochVictims.isEmpty()) {
                    int living = 0;
                    ArrayList<ArrayList<Character>> byLevel = new ArrayList<>();
                    for (int _l = 0; _l <= 10; _l++) byLevel.add(new ArrayList<>());
                    for (Character _c : characters) {
                        if (_c == null || _c.dead || !_c.born || _c.trait == Character.Traits.Beast || _c.trait == Character.Traits.Monster) continue;
                        living++;
                        if (_c.player < 1) byLevel.get(Math.max(0, Math.min(10, _c.majorLvl))).add(_c);
                    }
                    if (living >= EPOCH_POPULATION) {
                        for (ArrayList<Character> tier : byLevel) {
                            if (tier.size() < 10) continue;
                            java.util.Collections.shuffle(tier, rand);
                            epochVictims.addAll(tier.subList(0, tier.size() / 10));
                        }
                        System.out.println("[EPOCH] The Heavens grow weary of the crowded world. " + epochVictims.size() + " cultivators are marked for the great purge!");
                    }
                }
                // Purge broken (deconstructed) items from every city pool and shop shelf
                for (Area _pa : createdAreas) {
                    if (_pa.backpack != null) _pa.backpack.removeIf(i -> i == null || i.itemType == null || i.name == null);
                    for (Shop _ps : _pa.shops) _ps.inventory.removeIf(i -> i == null || i.itemType == null || i.name == null);
                }
                // Free shop slots held by dead merchants (their unsold stock goes back to the city pool)
                for (Area _sa : createdAreas) {
                    if (_sa.shops.isEmpty()) continue;
                    for (Shop _sh : new ArrayList<>(_sa.shops)) {
                        if (_sh.owner == null || _sh.owner.dead) {
                            if (_sa.backpack != null) _sa.backpack.addAll(_sh.inventory);
                            _sa.shops.remove(_sh);
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
                Settlements.yearly(); // cities and sects scale with the number of cultivators
                Seasonal.diplomacy(); // truces, gatherings and quarrels between sects
                Lore.yearly();        // the Palace: next Convocation, ranks, omens, the crossing
                Life.marketYearly();  // spoilage on the city markets' shelves
                Chronicle.yearly();   // a life sample for the Biography chart
                if (time / 52 > 0 && (time / 52) % 100 == 0) Chronicle.add("world", "The " + Naming.era(time / 5200 - 1) + " ends. A new age begins: the " + Naming.era(time / 5200) + ".");
                RunStats.yearly();
                // Auto-save every 10 years
                yearlyDone = true; // everything above this point has run for this year
                if (Settings.autosaveYears > 0 && _year % Settings.autosaveYears == 0) SaveManager.autosave();
            }
            Perf.mark(Perf.YEARLY);
            // Tournament timer countdown and resolution (every tick)
            for (Area area : createdAreas) {
                if (area.city) Auctions.tick(area); // auction houses: lots are put up, and held when five wait or six weeks have passed
                area.crowdCheck(time / 52 + 1);
                if (area.tournamentTimer > 0) {
                    area.tournamentTimer--;
                    if (area.tournamentTimer == 0) {
                        area.runTournament();
                    }
                }
                if (area.celestialTrialTimer > 0) { // Celestial Trial despawn
                    area.celestialTrialTimer--;
                    if (area.celestialTrialTimer == 0) {
                        area.dungeonType = null;
                        System.out.println("The Celestial Trial at " + area.name + " has closed.");
                    }
                }
            }
            Lore.worldTick(); // opens a waiting Convocation once the courtyard is free

            // Monster bounties: post Hunt Beast quests on city boards for all nearby monster areas (2-tile radius)
            for (Area city : createdAreas) {
                if (!city.city) continue;
                for (Area nearby : createdAreas) {
                    if (Math.abs(nearby.x - city.x) > 2 || Math.abs(nearby.y - city.y) > 2) continue;
                    if (nearby == city || nearby.monsters <= 0) continue;
                    boolean alreadyPosted = city.bounties.stream()
                            .anyMatch(q -> "Hunt Beast".equals(q.name) && q.targetArea == nearby);
                    if (!alreadyPosted)
                        city.bounties.add(new Quest("Hunt Beast", true, 45, 5, 65, nearby));
                }
            }

            Perf.mark(Perf.AREASCAN);
            // Epoch purge in progress: the marked die in batches so the world doesn't stall on one huge tick
            if (!epochVictims.isEmpty()) {
                int batch = Math.min(EPOCH_BATCH, epochVictims.size());
                for (int _e = 0; _e < batch; _e++) {
                    Character v = epochVictims.remove(epochVictims.size() - 1);
                    if (v != null && !v.dead && v.player < 1) { v.deathCause = "the Heavenly purge"; v.dead(); } // a character a human has since taken over (or follows) is spared too
                }
                if (epochVictims.isEmpty()) System.out.println("[EPOCH] The purge has ended. The world falls quiet.");
            }

            Perf.mark(Perf.EPOCH);
            // Pass 1a: age every character — serial; needs full list including unborn
            final java.util.List<Character> fullSnap = new ArrayList<>(characters);
            for (Character character : fullSnap) {
                if (character == null || character.dead) continue;
                character.time();
            }
            // Live snapshot taken AFTER time() so newly-born chars are included
            final java.util.List<Character> tickSnap = new ArrayList<>();
            for (Character c : fullSnap) {
                if (c != null && !c.dead && c.born) tickSnap.add(c);
            }

            Perf.mark(Perf.TIME);
            // Pass 1b: cultivate — parallel (pure per-character math; hero flags use double-checked sync)
            try {
                tickPool.submit(() ->
                    tickSnap.parallelStream()  // tickSnap already filtered: no null/dead/unborn
                        .forEach(character -> {
                            character.cultivate();
                            if (character.sect != null && character.sect.founder != null
                                    && character.sect.founder.lvl > character.lvl) {
                                int bonus = Math.max(character.sect.founder.majorLvl,
                                        character.sect.sectLvl() / 10) + character.sect.territoryBonus;
                                for (int i = 0; i < bonus; i++) character.cultivate();
                            }
                            if (character.location != null && character.location.spiritualVein) {
                                character.cultivate();
                            }
                            for (int _bl = 0; _bl < character.bloodlineBonus; _bl++) {
                                character.cultivate();
                            }
                        })
                ).get();
            } catch (Exception _ex) {
                System.err.println("[WARN] Parallel cultivate error: " + _ex);
                _ex.printStackTrace(); // the message alone hid the cause and the whole batch silently aborted every tick
            }

            Perf.mark(Perf.CULT);
            // Phase 2A: serial — player characters (interactive I/O must stay single-threaded)
            {   // who the death / war / birth notices go to
                List<Character> hs = new ArrayList<>();
                for (Character c : tickSnap) if (c.player == 1 && !c.dead) hs.add(c);
                Notifications.setHumans(hs);
            }
            for (Character character : tickSnap) { // tickSnap: only live born chars
                if (character.player < 1 || character.dead) continue; // died earlier this tick (reincarnation is handled below)
                currentCharacter = character;
                if (character.age < 8) {
                    character.quest();
                } else if (character.player == 2) {
                    npcTurn(character); // a tracked NPC runs on the same pipeline as an AUTO player character
                    System.out.println("[Follow] " + character + character.location);
                } else {
                    playerTurn(character);
                }
                syncMotherChildren(character);
            }

            Perf.mark(Perf.P2A);
            // Phase 2B: serial NPC processing (tickSnap: only live born chars)
            for (Character npc : tickSnap) {
                if (frozenNpcs) break; // tests only
                if (npc.player >= 1 || npc.dead) continue; // killed earlier this tick: must not move (re-adding itself to a tile) or quest
                if (npc.masterId >= 0 || npc.waitingSoul >= 0) continue; // a tamed beast has no turn: it follows its master (below); a waiting one stays put
                currentCharacter = npc;
                npcTurn(npc);
                syncMotherChildren(npc);
            }

            Perf.mark(Perf.P2B);
            for (Character beast : tickSnap) if (beast.masterId >= 0 && !beast.dead) Taming.follow(beast); // companions stand where their masters ended the turn

            LoreEndings.drainReleases(); // humans who left the world as soldiers choose their next character
            // Handle reincarnation for any player character that died this tick
            for (Character character : new ArrayList<>(characters)) {
                if (character != null && character.dead && character.needsReincarnation) {
                    handleReincarnation(character);
                }
            }

            Perf.mark(Perf.TAME);
            // Ages the areas and adds the tempCharacters list to the characters list.

            for (Area area : createdAreas) {
                area.age();
            }
            Perf.mark(Perf.AGE);
            for (Character monster : frozenNpcs ? new ArrayList<Character>() : new ArrayList<>(monstersList)) { // copy: a kill inside the loop can add a new roaming beast to the list
                if (monster.dead) continue;
                if (!monster.location.characters.isEmpty()) {
                    Character target = monster.location.characters.get(rand.nextInt(monster.location.characters.size()));
                    monster.battleCheck(target);
                }
                monster.movement();
            }
            Perf.mark(Perf.MON);
            time++;
            yearlyDone = false;

            characters.addAll(tempCharacters);
            population += tempCharacters.size();

            if (!remCharacters.isEmpty()) {
                for (Character character : remCharacters) {
                    if (character.trait != Character.Traits.Beast && character.trait != Character.Traits.Monster) {
                        characters.set(character.id, null);
                        population--;
                    } else if (character.wasTamed && character.id >= 0 && character.id < characters.size() && characters.get(character.id) == character) {
                        characters.set(character.id, null); // a tamed beast freed or killed in the tick it was tamed (it was never counted in the population)
                    }
                }
            }
            {   // hash lookup instead of removeAll's O(monsters x removals) scan (matters with thousands of purge deaths per tick)
                java.util.Set<Character> gone = new java.util.HashSet<>(remCharacters);
                gone.addAll(awakenedMonsters);
                monstersList.removeIf(gone::contains);
            }
            awakenedMonsters.clear();
            tempCharacters.clear();
            remCharacters.clear();
            Perf.mark(Perf.CLEAN); Perf.tickEnd();
            // End of tick: let the GUI read a consistent world (bounded wait), then pause / sleep with the world lock released
            if (guiEnabled && mapViewer != null) World.handoff(mapViewer::refreshNow, 250);
            Perf.mark(Perf.GUI);
            blocked(() -> {
                while (paused) Thread.sleep(50); // pause support: spin until unpaused, then sleep the configured delay
                Thread.sleep(tickDelayMs);
                return null;
            });
        }
    }
}
