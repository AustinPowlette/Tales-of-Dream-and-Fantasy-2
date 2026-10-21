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
    static synchronized int nextUid() { return UID++; } // (turns of different tiles may run together)
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
        return blocked(() -> nextToken());
    }
    /** The next word typed; the rest of that line is dropped (a reply like "no thanks" used to leave "thanks" to run as a command). */
    private static String nextToken() {
        String w = scan.next();
        try { scan.nextLine(); } catch (NoSuchElementException e) { }
        return w;
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
                if (System.in.available() > 0) return nextToken();
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
            try {
                waitingForInput = true;
                while (true) { // poll, so a Save clicked while the game waits for the player's command is done now, not after the next move
                    if (mapViewer != null) mapViewer.refreshLogIfNew(); // what the last command printed is shown now, not at the end of the next week
                    String s = blocked(() -> guiInputQueue.poll(150, java.util.concurrent.TimeUnit.MILLISECONDS));
                    if (s != null) return s.trim();
                    servePendingSaves(true); // (the game waits for a command in the middle of a week)
                }
            }
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

    /** While the game is paused (and not waiting at a prompt) every command that takes no time (looking, settings, trading, gifts, Edit Self, the rankings...) is run now, so its pop-up opens and
     *  it works; a command that spends the week waits for the game to resume, in order, and a window says so once per pause. A Save is done at once. */
    static void servePausedInfo() {
        servePendingSaves();
        if (!guiEnabled || guiInputQueue.isEmpty()) return;
        Character who = mapViewer == null ? null : mapViewer.findPlayer();
        java.util.List<String> kept = servePaused(who, guiInputQueue);
        if (!kept.isEmpty() && !pausedNoticeShown) { pausedNoticeShown = true; showPausedNotice(kept); }
        servePendingSaves();
    }

    /** Takes every command off the queue: the ones that take no time run now (for `who`), the rest are put back in their order and returned. */
    static java.util.List<String> servePaused(Character who, java.util.Queue<String> queue) {
        java.util.ArrayList<String> keep = new java.util.ArrayList<>();
        String cmd;
        while ((cmd = queue.poll()) != null) {
            if (who != null && !who.dead && PlayerCommands.runsWhilePaused(cmd)) { try { PlayerCommands.handle(who, cmd); } catch (RuntimeException e) { System.out.println("(that command failed while paused: " + e + ")"); } }
            else keep.add(cmd);
        }
        queue.addAll(keep);
        return keep;
    }

    static volatile boolean pauseWaiting = false;   // the game is standing still in the pause loop (the end of a tick), answering free commands
    private static boolean pausedNoticeShown = false; // the "you are paused" window was shown in this pause

    /** A window that tells the player the game is paused and that the command they gave is queued (non-modal; the pause goes on). */
    private static void showPausedNotice(java.util.List<String> queued) {
        final String first = queued.get(0);
        final int more = queued.size() - 1;
        System.out.println("The game is paused: '" + first + "'" + (more > 0 ? " and " + more + " more" : "") + " will run when you resume.");
        if (guiEnabled && mapViewer != null) {
            final String text = "The game is paused.\n\n'" + first + "'" + (more > 0 ? " and " + more + " other command(s) use" : " uses") + " up a week, so " + (more > 0 ? "they wait" : "it waits") + " in line and will run when you resume (the Pause button or P).\n\nEverything that takes no time still works while paused: looking at things, buying and selling, gifts, equipping, settings, Edit Self, the rankings and saving.";
            javax.swing.SwingUtilities.invokeLater(() -> GuiSheets.infoWindow("Paused", text));
        }
    }

    /** Between steps (the game waits for the player, or is paused, or a tick begins) a Save is done now. The world is brought to a consistent state first: births of this week join the list and the
     *  people who died this week leave it (the end of the tick does the same), so a save made while the game waits for a command is complete. Loading it repeats the week. */
    static void servePendingSaves() { servePendingSaves(false); }

    /** `midWeek`: the game is waiting for the player's command in the middle of a week (the slot is marked, and loading it plays that week over once). */
    static void servePendingSaves(boolean midWeek) {
        if (!saveRequested && saveRequestName == null) return;
        flushPendingLists();
        SaveManager.savingMidWeek = midWeek;
        try {
            if (saveRequested) { saveRequested = false; SaveManager.save(SaveManager.QUICK, SaveManager.Kind.QUICK); }
            if (saveRequestName != null) { String n = saveRequestName; saveRequestName = null; SaveManager.save(n, SaveManager.Kind.MANUAL); }
        } finally { SaveManager.savingMidWeek = false; }
    }

    /** The end of a tick's bookkeeping, usable in the middle of a tick: births join the list, the dead leave it, awakened monsters leave the beasts' list. */
    static void flushPendingLists() {
        mergeBirths();
        for (Character character : new ArrayList<>(remCharacters)) {
            if (character.trait != Character.Traits.Beast && character.trait != Character.Traits.Monster) {
                if (character.id >= 0 && character.id < characters.size() && characters.get(character.id) == character) { characters.set(character.id, null); population--; }
            } else if (character.wasTamed && character.id >= 0 && character.id < characters.size() && characters.get(character.id) == character) {
                characters.set(character.id, null);
            }
        }
        java.util.Set<Character> gone = new java.util.HashSet<>(remCharacters);
        gone.addAll(awakenedMonsters);
        if (!gone.isEmpty()) monstersList.removeIf(gone::contains);
        awakenedMonsters.clear();
        remCharacters.clear();
    }

    /** Non-blocking read for characters running on their own (AUTO / scripts): a typed command such as 'stop', or null. */
    static String pollInput() {
        if (guiEnabled) return guiInputQueue.poll();
        try {
            if (System.in.available() > 0) return scan.nextLine().trim();
        } catch (Exception ignored) {}
        return null;
    }

    /** Counts the lines added to the event log: the GUI redraws its log pane when this changes (a command that costs no turn must show its answer at once, not after the next tick). */
    static volatile int logVersion = 0;

    static void logEvent(String s) {
        if (s == null) return;
        eventLog.add(s);
        logVersion++;
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
    static void editCharacterInteractive(Character c) { editCharacterInteractive(c, false); }

    /** `creation`: the editor offered while the character is being made (the potential may be set); in a running game only a Cheater may change it. */
    static void editCharacterInteractive(Character c, boolean creation) {
        if (guiEnabled && mapViewer != null) {
            try { edtWait(() -> GuiEditSelf.show(mapViewer, c, creation)); } catch (Exception ignored) { }
            return;
        }
        // Terminal mode
        boolean editing = true;
        while (editing) {
            System.out.println("Edit: (N)ame (T)rait (P)otential (A)ffiliation (G)ender (S)kin (R)andomize (D)one");
            System.out.println("  Current: " + c.name + " " + c.lastName
                    + "  Trait:" + c.getTrait() + "  Pot:" + c.potential
                    + "  Aff:" + c.affiliation + "  Gender:" + c.gender + "  Skin:" + ArtMore.skinName(c.skinTone));
            String optIn = readInput();
            char opt = optIn.isEmpty() ? '?' : optIn.charAt(0);
            switch (opt) {
                case 'N': case 'n':
                    System.out.println("First name:"); c.name = readInput();
                    System.out.println("Last name:");  c.lastName = readInput();
                    break;
                case 'T': case 't':
                    System.out.println("Trait: A=Alchemist B=Blacksmith T=Tailor S=Scribe I=Innovative L=Leader M=Merchant R=Runner G=Ghost Ga=Gatherer Mi=Miner Tm=Tamer Po=Poisoner Dr=Drunken Ch=Chef Mu=Musician Fr=Friendly Ck=Cheater Lk=Lucky Ib=IronBody Sv=SpiritVein Hr=HeavenlyRoot");
                    String tc = readInput().toLowerCase();
                    boolean known = true;
                    if (tc.length() > 2) { // the trait's name works as well as its code
                        known = false;
                        for (Character.Traits tr : Character.Traits.values()) if (tr != Character.Traits.Monster && tr != Character.Traits.Beast && tr.name().toLowerCase().startsWith(tc)) { c.trait = tr; known = true; break; }
                        if (!known && tc.equals("innovator")) { c.trait = Character.Traits.Innovative; known = true; }
                        tc = "";
                    }
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
                        case "":  break;
                        default:   known = false;
                    }
                    if (!known) System.out.println("Not a trait: type its code (A, B, I, Ga, ...) or its name (Innovative, Tamer, ...).");
                    break;
                case 'P': case 'p':
                    if (!creation && c.trait != Character.Traits.Cheater) { System.out.println("Potential can only be changed by a Cheater (pick the Cheater trait first)."); break; }
                    System.out.println("Potential (1-100):");
                    try { c.potential = Math.min(100, Math.max(1, Integer.parseInt(readInput()))); } catch (Exception ignored) {}
                    break;
                case 'A': case 'a':
                    System.out.println("Affiliation: G=Good N=Neutral E=Evil");
                    String acIn = readInput();
                    char ac = acIn.isEmpty() ? 'n' : acIn.charAt(0);
                    if (ac == 'G' || ac == 'g') c.affiliation = "Good";
                    else if (ac == 'E' || ac == 'e') c.affiliation = "Evil";
                    else c.affiliation = "Neutral";
                    break;
                case 'G': case 'g':
                    System.out.println("Gender: M=Male F=Female");
                    String gcIn = readInput();
                    char gc = gcIn.isEmpty() ? 'm' : gcIn.charAt(0);
                    c.gender = (gc == 'M' || gc == 'm') ? "Male" : "Female";
                    break;
                case 'S': case 's': {
                    StringBuilder sk = new StringBuilder("Skin tone (the portrait): 0 = by the portrait's seed");
                    for (int i = 0; i < ArtMore.SKIN_NAMES.length; i++) sk.append(", ").append(i + 1).append(" = ").append(ArtMore.SKIN_NAMES[i]);
                    System.out.println(sk);
                    try { c.skinTone = Math.max(0, Math.min(ArtMore.SKINS.length, Integer.parseInt(readInput().trim()))); } catch (Exception ignored) { }
                    break;
                }
                case 'R': case 'r': { // a random name, skin, trait, alignment and (when it may change) potential; the gender stays
                    String[] nm = EditRandom.name("Female".equalsIgnoreCase(c.gender), c.location); c.name = nm[0]; c.lastName = nm[1];
                    c.skinTone = EditRandom.skin(); c.trait = EditRandom.trait(); c.affiliation = EditRandom.alignment();
                    if (creation || c.trait == Character.Traits.Cheater) c.potential = EditRandom.potential();
                    System.out.println("Randomized: " + c.name + " " + c.lastName + ", " + c.getTrait() + ", " + c.affiliation + ", potential " + c.potential + ".");
                    break;
                }
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
                System.out.println("Create weapon at level (1-100):");
                try {
                    int wlvl = Math.min(100, Math.max(1, Integer.parseInt(readInput())));
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
                    actThenWander(npc);
                } else if (leader.player == 1 && leader.pc != null && leader.pc.mode != PlayerControl.Mode.AUTO) {
                    // The leader is a person who plays: the member stays in the squad (it leaves only when dismissed) and its errands wait; it works on the spot, then rides with the leader
                    // wherever his step takes him, so that it stands in his tile when his turn comes. On Hold it stays where it is.
                    if (npc.squadOrder == Character.ORDER_WORK) { actThenWander(npc); return; } // goes about its own errands; still in the squad
                    npc.squadBound = true;
                    try { npc.quest(); } finally { npc.squadBound = false; }
                    if (npc.squadOrder == Character.ORDER_HOLD) { /* stays put */ }
                    else if (npc.location == leader.location) npc.ride(leader);
                    else npc.pathfinder(leader.location);
                } else {
                    // Abandon party only if follower's top quest outprioritises the party's purpose
                    Quest topQ = npc.targetQuests.peek();
                    int leaderPriority = (leader.currentQuest != null) ? leader.currentQuest.priority : 0;
                    if (topQ != null && topQ.priority > leaderPriority
                            && topQ.targetArea != null && topQ.targetArea != leader.location) {
                        npc.leaveParty();
                        actThenWander(npc);
                    } else if (npc.location == leader.location) {
                        npc.quest();           // standing with the leader: the quest routine here, then on with him wherever his step takes him
                        npc.ride(leader);
                    } else {
                        npc.pathfinder(leader.location); // not with him yet: a step toward him
                    }
                }
            } else {
                actThenWander(npc);
            }
        } else {
            npc.quest();
        }
    }

    /** Act on the tile you stand in (the quest routine: spot quests, the target quest's work, its step), and only then, with no step planned by a quest, wander one step. A step is the last thing of a turn. */
    static void actThenWander(Character npc) {
        final boolean exploring = npc.exploreTicks > 0;
        npc.quest();
        if (npc.dead) return;
        if (npc.movedTick != time) { // no step yet this week
            if (exploring) npc.movement(npc.exploreDir);
            else npc.movement();
        }
    }

    /** The steps of a big crowd taken on several cores: first everybody leaves the tile he stood on (one core per tile left), then everybody arrives (one core per tile entered), so no two cores ever touch the same tile's list. */
    static void applyStepsInParallel(List<Character> moved) {
        final ArrayList<Character> steppers = new ArrayList<>(moved.size());
        for (Character c : moved) {
            if (c.pendTo != null && !c.dead && c.location == c.pendFrom && c.pendTo != c.location) steppers.add(c);
            else { c.pendTo = null; if (c.rideId < 0) c.pendFrom = null; }
        }
        forEachTile(steppers, c -> c.location, Character::leaveTile);
        forEachTile(steppers, c -> c.pendTo, Character::arriveTile);
    }

    /** Groups people by a tile and plays each group on one core (the biggest first, each core taking the next group when it is free). */
    static void forEachTile(List<Character> people, java.util.function.Function<Character, Area> tileOf, java.util.function.Consumer<Character> action) {
        java.util.HashMap<Area, ArrayList<Character>> byTile = new java.util.HashMap<>(2048);
        for (Character c : people) { Area t = tileOf.apply(c); ArrayList<Character> g = byTile.get(t); if (g == null) byTile.put(t, g = new ArrayList<>()); g.add(c); }
        final ArrayList<ArrayList<Character>> groups = new ArrayList<>(byTile.values());
        groups.sort((u, v) -> Integer.compare(v.size(), u.size()));
        final java.util.concurrent.atomic.AtomicInteger next = new java.util.concurrent.atomic.AtomicInteger();
        final List<java.util.concurrent.Future<?>> futures = new ArrayList<>();
        for (int w = Math.max(1, Math.min(tickPool.getParallelism(), groups.size())); w > 0; w--) futures.add(tickPool.submit(() -> {
            int gi;
            while ((gi = next.getAndIncrement()) < groups.size()) for (Character c : groups.get(gi)) { try { action.accept(c); } catch (Throwable t) { parallelError(t); } }
        }));
        for (java.util.concurrent.Future<?> f : futures) { try { f.get(); } catch (Exception e) { parallelError(e); } }
    }

    /** Every tamed companion goes to the tile its master ended the turn on (a pure update of its own place, so the beasts are played together on all cores); one whose master is gone goes back to the wild, one after the other. */
    static void followAllCompanions(List<Character> snap) {
        final java.util.concurrent.ConcurrentLinkedQueue<Character> orphans = new java.util.concurrent.ConcurrentLinkedQueue<>();
        try {
            tickPool.submit(() -> snap.parallelStream().forEach(b -> {
                if (b.masterId < 0 || b.dead) return;
                Character m = Taming.byId(b.masterId);
                if (m == null || m.dead) { orphans.add(b); return; }
                if (m.location != null) b.location = m.location;
                b.partyLeaderId = m.id;
            })).get();
        } catch (Exception e) { for (Character b : snap) if (b.masterId >= 0 && !b.dead) Taming.follow(b); return; }
        for (Character b : orphans) Taming.follow(b);
    }

    /** Phase 2A: the people who play (and the tracked NPCs), serial: interactive I/O must stay single-threaded. */
    static void peoplePhase(List<Character> tickSnap) {
        for (Character character : tickSnap) { // tickSnap: only live born chars
            if (character.player < 1 || character.dead) continue; // died earlier this tick (reincarnation is handled below)
            currentCharacter = character;
            if (character.age < 8) {
                character.quest();
            } else if (character.player == 2) {
                character.planning = true;
                npcTurn(character); // a tracked NPC runs on the same pipeline as an AUTO player character
                character.planning = false; character.rideId = -1; character.applyMove();
                Say.status("[Follow] " + character + character.location);
            } else {
                playerTurn(character);
            }
            syncMotherChildren(character);
        }
    }

    /** Whether the NPC steps have been decided and are waiting to be taken (the person who plays acts in between; see Character.interceptAim). */
    static volatile boolean movesPlanned = false;

    /** The NPC phase: every NPC acts on the tile it stands in and plans its step. One after the other, or (Settings.parallelNpcs) the turns of different tiles together on several cores. */
    static void npcPhase(List<Character> tickSnap, ArrayList<Character> moved) {
        if (frozenNpcs) return; // tests only
        if (Settings.parallelNpcs && tickPool.getParallelism() > 1) { parallelNpcPhase(tickSnap, moved); return; }
        if (rand instanceof ParallelRandom) rand = new Random();
        for (Character npc : tickSnap) {
            if (!npcCanAct(npc)) continue;
            currentCharacter = npc;
            runNpcTurn(npc, moved);
        }
    }

    /** Killed earlier this tick: must not move (re-adding itself to a tile) or quest; a tamed beast has no turn (it follows its master); a waiting one stays put. */
    static boolean npcCanAct(Character npc) { return npc.player < 1 && !npc.dead && npc.masterId < 0 && npc.waitingSoul < 0; }

    static void runNpcTurn(Character npc, List<Character> moved) {
        npc.pendTo = null; npc.pendFrom = null; npc.rideId = -1; // (a turn that failed last week must not leave a step planned that blocks every step after it)
        npc.planning = true;
        try { npcTurn(npc); } finally { npc.planning = false; }
        npc.turnT = time;
        if (npc.pendTo != null || npc.rideId >= 0) moved.add(npc);
    }

    /** How many NPC turns failed in a parallel phase (each loses the rest of its turn), and where: the first Game frame of the stack, counted (printed now and then, so the sites can be fixed one by one). */
    static final java.util.concurrent.atomic.AtomicLong parallelErrors = new java.util.concurrent.atomic.AtomicLong();
    static final java.util.concurrent.ConcurrentHashMap<String, java.util.concurrent.atomic.AtomicLong> parallelSites = new java.util.concurrent.ConcurrentHashMap<>();
    static void parallelError(Throwable t) {
        long n = parallelErrors.incrementAndGet();
        String site = t.getClass().getSimpleName();
        StackTraceElement[] st = t.getStackTrace();
        for (StackTraceElement e : st) if (e.getClassName().startsWith("Game.") && !e.getClassName().startsWith("Game.GameDriver")) { site += " @ " + e.getClassName().substring(5) + "." + e.getMethodName() + ":" + e.getLineNumber(); break; }
        parallelSites.computeIfAbsent(site, k -> new java.util.concurrent.atomic.AtomicLong()).incrementAndGet();
        if (n <= 3) { System.err.println("[PARALLEL] an NPC turn failed (" + n + "): " + t); t.printStackTrace(); }
        if (n % 1000 == 0) System.err.println("[PARALLEL] " + n + " turns failed so far; " + parallelSummary());
    }
    static String parallelSummary() {
        StringBuilder sb = new StringBuilder();
        parallelSites.entrySet().stream().sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get())).limit(12).forEach(e -> sb.append(e.getValue().get()).append("x ").append(e.getKey()).append("; "));
        return sb.toString();
    }

    /** Experimental: the NPCs are grouped by the tile they stand on and the groups are shared out over the cores (the biggest first, each core taking the next group when it is free). Everything a turn
     *  does that reaches beyond its tile has to be safe for that (see Parallel Turns in Game Info.md); the steps are only planned, so nobody changes tile meanwhile. */
    static void parallelNpcPhase(List<Character> tickSnap, ArrayList<Character> moved) {
        if (!(rand instanceof ParallelRandom)) rand = new ParallelRandom();
        // A turn may have to ask a person who plays (a friendship, a challenge, a squad): that waits for the console on the game thread. The tiles where such a person stands are
        // played one NPC after the other on the game thread, after the rest has been played together.
        final java.util.HashSet<Area> peopleTiles = new java.util.HashSet<>();
        for (Character c : tickSnap) if (c.player >= 1 && !c.dead && c.location != null) peopleTiles.add(c.location);
        java.util.HashMap<Area, ArrayList<Character>> byTile = new java.util.HashMap<>(2048);
        final ArrayList<Character> onGameThread = new ArrayList<>();
        for (Character npc : tickSnap) {
            if (!npcCanAct(npc)) continue;
            if (peopleTiles.contains(npc.location)) { onGameThread.add(npc); continue; }
            ArrayList<Character> g = byTile.get(npc.location);
            if (g == null) byTile.put(npc.location, g = new ArrayList<>());
            g.add(npc);
        }
        final ArrayList<ArrayList<Character>> groups = new ArrayList<>(byTile.values());
        groups.sort((x, y) -> Integer.compare(y.size(), x.size()));
        final int workers = Math.max(1, Math.min(tickPool.getParallelism(), groups.size()));
        final java.util.concurrent.atomic.AtomicInteger next = new java.util.concurrent.atomic.AtomicInteger();
        final List<List<Character>> movedBy = new ArrayList<>();
        final List<java.util.concurrent.Future<?>> futures = new ArrayList<>();
        Map.parallelPhase = true;
        for (int w = 0; w < workers; w++) {
            final List<Character> mine = new ArrayList<>();
            movedBy.add(mine);
            futures.add(tickPool.submit(() -> {
                int gi;
                while ((gi = next.getAndIncrement()) < groups.size()) {
                    for (Character npc : groups.get(gi)) {
                        if (!npcCanAct(npc)) continue;
                        try { runNpcTurn(npc, mine); } catch (Throwable t) { parallelError(t); }
                    }
                }
            }));
        }
        for (java.util.concurrent.Future<?> f : futures) {
            try { f.get(); } catch (Exception e) { parallelError(e); }
        }
        Map.parallelPhase = false;
        Map.runDeferred(); // what reached beyond a tile (friends' lists, sects, distant boards, payments) happens now, on the game thread
        for (List<Character> m : movedBy) moved.addAll(m);
        for (Character npc : onGameThread) {
            if (!npcCanAct(npc)) continue;
            currentCharacter = npc;
            runNpcTurn(npc, moved);
        }
    }

    /** Takes the planned steps of the NPC phase together, then the party members who stood with their leaders, then the children who follow their mothers. */
    static void applyMoves(List<Character> moved, List<Character> everyone) {
        if (Settings.parallelNpcs && moved.size() >= 4000 && tickPool.getParallelism() > 1) applyStepsInParallel(moved);
        else for (Character c : moved) c.applyMove();
        for (Character c : moved) if (c.rideId >= 0) c.applyRide();
        for (Character c : everyone) if (c.player == 0 && !c.dead && c.children != null && !c.children.isEmpty() && c.gender.equals("Female")) syncMotherChildren(c); // (the NPC mothers: a human mother brings her children along in her own turn)
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
                    child.learnPlace(child.location);
                }
            }
        }
    }

    /** One turn of a human-controlled adult: MANUAL prompts for commands; AUTO runs the NPC pipeline; HYBRID runs the script. */
    static void playerTurn(Character c) {
        PlayerControl pc = c.pc();
        pc.movedThisTick = false;
        pc.assignUids(c);
        if (!pc.dedupedAtLoad) { pc.dedupedAtLoad = true; c.dedupePlayerQueue(); } // old saves (and a tester's ten identical Find Person quests) lose their copies once
        Notifications.observe(c);
        Recap.ensureBase(c);
        Starts.tick(c); // objectives of a Story start
        Lore.tick(c);   // the Palace's story: objectives, ??? speaks
        CheatsMore.refill(c); // cheat freecraft: the bag is full of herbs, metal and Blood
        Vignettes.tick(c); // now and then a little story on the road (its own switch, `settings vignettes`)
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
            boolean walking = character.skipping > 0 && character.movement != 'x';
            String by = pc.attackedBy == null ? "" : " by " + pc.attackedBy;
            pc.attackedBy = null;
            if (character.skipping > 0 && walking && !pc.walkStopFight) {
                System.out.println("You were attacked" + by + " on the road; you keep walking. ('policy walkfight on' stops a walk at every fight.)");
            } else if (character.skipping > 0) {
                character.skipping = 0;
                System.out.println("You were attacked" + by + " — autopilot cancelled.");
            }
        }
        boolean prompted = character.skipping <= 0;
        if (!prompted) {
            character.skipping--; // autopilot walk/wait chosen earlier
            if (character.movement == 'm') {
                character.movement();
            } else if (character.movement == 'c' && character.autoX >= 0) {
                // the walk ended in this very turn (arrived, caught up with the one chased): the player acts now, before a chased walker takes its planned step
                if (PlayerCommands.stepAuto(character)) prompted = true;
            } else {
                character.movement(character.movement);
            }
        }
        if (prompted && !character.dead) promptLoop(character, pc);
        // Duty quests may walk the player only on a `watch` tick (the game fast-forwards while the duties work). A tick the player chose (an action, `stay`, `wait`, a walk, the arrival of a walk) is the player's own.
        pc.movedThisTick = character.location != before || prompted || !pc.watching;
        if (character.dead) return;
        if (pc.mode == PlayerControl.Mode.MANUAL) character.quest(); // duties that are toggled on, plus quest alerts
        else runOnItsOwn(character, pc);                            // the command switched modes this turn
    }

    /** The prompt of a MANUAL turn: status, then commands until one uses the turn (info, setup and free actions keep asking). */
    private static void promptLoop(Character character, PlayerControl pc) {
        if (!TermHud.enabled()) { // the terminal shows a status block (TermHud, after any notices); the GUI and 'settings hud off' get one line (the bags, the herbs and metals, the pills and the spouse are in 'bag', the Backpack window and the Me tab, not repeated every week)
            System.out.println("" + character + character.location);
        }
        if (Auctions.hasHouse(character.location) && character.location.auctionBag != null
                && Auctions.weeksToNext(character.location) > 0 && Auctions.weeksToNext(character.location) <= 3)
            Notifications.onAuctionSoon(character, character.location, Auctions.weeksToNext(character.location));
        character.offerBreakthrough(); // a full bar at the end of a realm waits for the person who plays
        TermHud.show(character);
        boolean firstPrompt = true;
        while (true) { // info/setup commands don't use up the turn: keep asking until an action does
            TermHud.prompt(character, firstPrompt); firstPrompt = false;
            boolean used = PlayerCommands.handle(character, readLine());
            servePendingSaves(true); // a `save` / Save button at the prompt is done before the next question (mid-week: the slot is marked)
            if (used) break;
            if (character.dead || pc.mode != PlayerControl.Mode.MANUAL) break;
        }
    }

    static void runOnItsOwn(Character c, PlayerControl pc) {
        if (pc.mode == PlayerControl.Mode.AUTO) {
            c.skipping = 1; // routine gathering/crafting lines are silenced while running on its own (fights, deaths and breakthroughs still print)
            c.planning = true;
            npcTurn(c); // (acts where it stands, then takes its step)
            c.planning = false; c.rideId = -1; c.applyMove();
            c.skipping = 0;
            if (c.dead) return;
            if (pc.autoTurns > 0 && --pc.autoTurns == 0) {
                pc.mode = PlayerControl.Mode.MANUAL;
                System.out.println("AUTO run finished — you have control again.");
            } else if (time % 13 == 0) {
                Say.status("[Auto] " + c + c.location);
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
    /** The after-death window: a modal dialog with one button per label that the X, Escape and a click outside cannot close (closing it used to count as "Spectate" and left the world racing at full speed).
     *  Returns the index of the button pressed. Runs on the game thread (the world lock is released while it waits). */
    static int deathDialog(String message, java.util.List<String> labels, int dflt) {
        final int[] pick = {dflt};
        try {
            edtWait(() -> {
                final javax.swing.JDialog d = new javax.swing.JDialog(mapViewer, "After death", true);
                d.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
                javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.BorderLayout(8, 8));
                p.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 16, 14, 16));
                javax.swing.JLabel text = new javax.swing.JLabel("<html><body style='width:380px'>" + GuiSheets.esc(message).replace("\n", "<br>") + "</body></html>");
                p.add(text, java.awt.BorderLayout.NORTH);
                javax.swing.JPanel buttons = new javax.swing.JPanel(new java.awt.GridLayout(0, 1, 0, 6));
                for (int i = 0; i < labels.size(); i++) {
                    final int k = i;
                    javax.swing.JButton b = new javax.swing.JButton(labels.get(i));
                    b.addActionListener(e -> { pick[0] = k; d.dispose(); });
                    buttons.add(b);
                    if (i == dflt) d.getRootPane().setDefaultButton(b);
                }
                p.add(buttons, java.awt.BorderLayout.CENTER);
                d.setContentPane(p);
                d.pack();
                d.setLocationRelativeTo(mapViewer);
                d.setVisible(true);
            });
        } catch (Exception ignored) { }
        return pick[0];
    }

    /** Nobody plays any more: in the windowed game the world slows to two seconds a week (the player chose to watch, or the life was the last), unless it already is slower (the speed buttons exist only there). */
    static void slowForSpectating() {
        if (guiEnabled && tickDelayMs < 2000) { tickDelayMs = 2000; System.out.println("Speed set to 2 seconds a week while you spectate (the Speed buttons change it)."); }
    }

    /** "Save and quit" / "Quit" chosen in the after-death window or menu. */
    static void quitFromDeath(boolean save) {
        quitSaves = save; quitRequested = true; paused = false;
        if (save) saveRequested = true;
        else { try { Settings.save(); } catch (RuntimeException ignored) { } System.exit(0); }
    }

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
            int brutal = 0;
            if (guiEnabled && mapViewer != null) brutal = deathDialog(dead.name + " " + dead.lastName + " has died.\nCause: " + _cause + "\n\n" + end, java.util.Arrays.asList("Spectate the world", "Save and quit the game", "Quit the game (without saving)"), 0);
            for (Character c : dead.location.characters) { if (c != null && !c.dead && c.player == 0) { c.player = 2; break; } }
            slowForSpectating();
            if (brutal == 1) quitFromDeath(true); else if (brutal == 2) quitFromDeath(false);
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
        final int saveQuitOpt = opt + 1, quitOpt = opt + 2;
        System.out.println("  " + saveQuitOpt + ". Save and quit the game");
        System.out.println("  " + quitOpt + ". Quit the game (without saving)");

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
            btnLabels.add(spectateOpt + ". Spectate the world (it slows to 2 seconds a week)");
            btnLabels.add(saveQuitOpt + ". Save and quit the game");
            btnLabels.add(quitOpt + ". Quit the game (without saving)");
            final int[] result = {spectateOpt};
            int idx = deathDialog(dead.name + " " + dead.lastName + " has died!\nAge " + dead.age + ", Realm " + dead.majorLvl + "\nCause: " + _cause + "\n\nChoose what happens next:", btnLabels, btnLabels.size() - 3);
            if (idx >= 0) {
                String label = btnLabels.get(idx);
                try { result[0] = Integer.parseInt(label.substring(0, label.indexOf('.'))); }
                catch (Exception ignored) { result[0] = spectateOpt; }
            }
            choice = result[0];
        } else {
            System.out.println("Choice:");
            try { choice = Integer.parseInt(readInput()); } catch (Exception e) { choice = spectateOpt; }
        }

        Character newChar = null;
        boolean quitAfter = choice == saveQuitOpt || choice == quitOpt, quitSave = choice == saveQuitOpt;
        if (quitAfter) choice = spectateOpt; // (the world is left as it is: nothing is taken over)
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
            slowForSpectating();
        }
        if (quitAfter) quitFromDeath(quitSave);
        dead.player = 0;
        dead.needsReincarnation = false;
        dead.reincarnationKiller = null;
    }


    // Population control
    // A tile's capacity is a fixed share of the STARTING population (never the current one, so a booming world can't raise its own ceiling),
    // a multiple (Settings.crowdFactor, 3.5) of the tile's fair share, the starting population spread evenly over the map's tiles: 365 on a wild tile of the usual 31x31 map at 100,000 people,
    // proportionally less on a bigger map and more on a smaller one.
    static int difficulty = 0;                    // world difficulty chosen at the start: 0 Story/Standard, 1 Hard, 2 Brutal (see Starts)
    static final int START_AGE = 18;              // age of the first generation (players and NPCs); cultivators born later start at 8
    static final int TRUCE_WEEKS = 52;            // nobody may start a fight with another cultivator in Originis during the first game year
    static final int CROWD_MIN_CAPACITY = 20;     // floor so tiny test populations don't turn every tile into a hotspot
    static int CROWD_CAPACITY = crowdFor(startingPopulation);       // wild tile
    static int CROWD_CAPACITY_CITY = CROWD_CAPACITY * 4;           // cities support more (Originis is exempt entirely)
    static int crowdFor(int start) { return Math.max(CROWD_MIN_CAPACITY, (int) Math.round(Settings.crowdFactor * start / (double) (range * range))); }
    /** The size of the world was chosen (or loaded): the limits of a tile follow it. */
    static void rescaleCrowd() { CROWD_CAPACITY = crowdFor(startingPopulation); CROWD_CAPACITY_CITY = CROWD_CAPACITY * 4; }
    static final int CROWD_GRACE_YEARS = 10;      // the founding crowd at Originis is given time to disperse first
    static final double CROWD_CLEAR_RATIO = 0.8;  // the hotspot ends once the tile is back under 80% of capacity
    static final double CROWD_ATTACK_RATE = 0.02; // beast ambushes per tick = 2% of the excess cultivators
    static final int CROWD_MAX_ATTACKS = 40;      // cap on ambushes per tile per tick
    static final int CROWD_SURGE = 15;            // level surge while a tile is a crowd hotspot
    // The epoch's three limits are settings (Settings.epochPopulation: the population target, whatever the speed of the computer; Settings.epochTickSeconds: the slower the computer the sooner it is reached;
    // Settings.epochCullPercent / epochCullNumber: how many fall); only the size of a world where a slow tick counts is fixed here
    static volatile int EPOCH_MIN_LIVING = Integer.getInteger("tdf.epochMin", 10000); // a slow tick in a small world is not a crowded world (tests may lower it)
    static volatile boolean epochNow = false;      // cheat epoch: the next tick purges whatever the crowd
    static final String PURGE_CAUSE = "the Heavenly purge"; // the death cause of the epoch's victims (Character.purgedByHeaven)
    // The last 26 ticks' own computing time (waiting for input or the window excluded); the median is the typical tick, so one slow yearly tick or a hiccup does not count
    static final long[] tickNs = new long[26];
    static int tickCount = 0;
    static long tickT0 = 0, tickBlocked0 = 0;
    static boolean tickPurging = false;
    static long medianTickMs() {
        int n = Math.min(tickCount, tickNs.length);
        if (n < 20) return 0;
        long[] c = java.util.Arrays.copyOf(tickNs, n);
        java.util.Arrays.sort(c);
        return c[n / 2] / 1_000_000L;
    }
    /** The realms (Major Lvl 0..10) the last purge took people from; only the survivors of those realms are blessed. */
    static final boolean[] epochTierCulled = new boolean[11];
    static { java.util.Arrays.fill(epochTierCulled, true); } // (a purge that was loaded halfway has no record: every realm up to the limit)
    static final double EPOCH_BLESSING = 0.5;     // the blessing is worth this many Potential Pills (Character.givePotential)
    /** The epoch's test (once a year, and at once after `cheat epoch`): too many living, memory full, or ticks too slow; then a share of each realm up to `epochrealm` is marked to die. */
    static void epochCheck() {
        if (!epochVictims.isEmpty()) return;
        int living = 0;
        ArrayList<ArrayList<Character>> byLevel = new ArrayList<>();
        for (int _l = 0; _l <= 10; _l++) byLevel.add(new ArrayList<>());
        for (Character _c : characters) {
            if (_c == null || _c.dead || !_c.born || _c.trait == Character.Traits.Beast || _c.trait == Character.Traits.Monster) continue;
            living++;
            if (_c.player < 1 && !Rankings.epochSafe(_c)) byLevel.get(Math.max(0, Math.min(10, _c.majorLvl))).add(_c); // (children, the ranked and the Palace's road are spared)
        }
        long typicalMs = medianTickMs();
        final boolean tooMany = living >= Settings.epochLimit();
        final long[] memNote = new long[2];
        final boolean memFull = !tooMany && living >= EPOCH_MIN_LIVING && MemWatch.full(memNote); // the heap is nearly full of living things (checked once a year)
        if (tooMany || memFull || epochNow || (living >= EPOCH_MIN_LIVING && typicalMs > Math.round(Settings.epochTickSeconds * 1000))) {
            int purgeable = 0;
            for (int _t = 0; _t < byLevel.size(); _t++) if (_t <= Settings.epochMaxRealm && byLevel.get(_t).size() >= 10) purgeable += byLevel.get(_t).size();
            final double share = Settings.epochCullNumber > 0 ? Math.min(1.0, Settings.epochCullNumber / (double) Math.max(1, purgeable)) : Settings.epochCullPercent / 100.0; // a number is shared out over the realms by their size
            for (int _t = 0; _t < byLevel.size(); _t++) {
                ArrayList<Character> tier = byLevel.get(_t);
                epochTierCulled[_t] = _t <= Settings.epochMaxRealm && tier.size() >= 10; // (a realm of fewer than 10, or above the limit, is left alone and gets no blessing)
                if (!epochTierCulled[_t]) continue;
                java.util.Collections.shuffle(tier, rand);
                epochVictims.addAll(tier.subList(0, (int) Math.min(tier.size(), Math.round(tier.size() * share))));
            }
            tickCount = 0; // the next epoch is judged on ticks of the thinned world
            Say.world("[EPOCH] The Heavens grow weary of the crowded world (" + (epochNow ? "the Heavens are called down by a Cheater" : tooMany ? living + " cultivators live" : memFull ? "the memory is nearly full: " + memNote[0] + " of " + memNote[1] + " MB alive" : "a turn now takes " + typicalMs + " ms") + "). " + epochVictims.size() + " cultivators are marked for the great purge!");
            Chronicle.add("world", "An epoch begins: the Heavens grow weary of the crowded world and " + epochVictims.size() + " cultivators are marked to fall.");
            epochNow = false;
        }
    }

    /** After an epoch the survivors of the realms it culled (up to Settings.epochMaxRealm) gain the potential of half a Potential Pill: a whole point for the youngest, a trickle for the highest. */
    static void epochBlessing() {
        int n = 0, points = 0;
        for (Character c : characters) {
            if (c == null || c.dead || !c.born || c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) continue;
            int realm = Math.max(0, Math.min(10, c.majorLvl));
            if (realm > Settings.epochMaxRealm || !epochTierCulled[realm] || c.potential >= 100) continue;
            points += c.givePotential(EPOCH_BLESSING); n++;
        }
        Say.world("[EPOCH] The Heavens bless those who remain in the realms they thinned (Major Lvl 0-" + Settings.epochMaxRealm + "): the potential of half a Potential Pill (" + n + " cultivators, " + points + " points in all).");
        Chronicle.add("world", "The epoch ends. The survivors of the lower realms are blessed: their potential grows.");
    }
    static final int EPOCH_BATCH = 3000;          // purge deaths processed per tick
    static ArrayList<Character> epochVictims = new ArrayList<>();

    // Java serialization recurses through the character/area object graph (knownLocations <-> Area.characters),
    // which gets very deep once the world is explored; run it on a thread with a large stack.
    static final long BIG_STACK = 1L << 30;

    /** Copies the static world state into a SaveData (game thread, between ticks). */
    static SaveData snapshot() {
        SaveData d = new SaveData();
        d.population      = population;
        d.startPop        = startingPopulation;
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
        Map.setStartingPopulation(d.startPop > 0 ? d.startPop : Map.DEFAULT_START); // the size the world began with (its crowd limits, its monsters); older saves began at 100,000
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
        GearNames.fixAll(); // gear named before 2026-10-18 read its level (1-100) as a realm number: the names follow the real realm now
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
        rescaleCrowd(); // (the factor may have been changed in the settings file)
        MemWatch.init(); // the heap gives back what it does not use
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
        while (true) { // a save that cannot be loaded is told about and the saved-games menu comes back (it used to start a new world without a word)
            String slotChoice = SaveManager.chooseAtStartup();
            if (slotChoice == null) break;
            loaded = SaveManager.load(slotChoice);
            if (loaded) break;
            final String failed = slotChoice;
            if (guiEnabled && mapViewer != null) { try { edtWait(() -> javax.swing.JOptionPane.showMessageDialog(mapViewer, "'" + failed + "' could not be loaded: the file is damaged or unreadable.\nNothing on disk was changed. Pick another save, or start a new game.", "Saved games", javax.swing.JOptionPane.WARNING_MESSAGE)); } catch (Exception ignored) { } }
            else System.out.println("Pick another save, or N for a new game.");
        }
        if (loaded) {
            System.out.println("Resuming. Player characters in world: "
                    + characters.stream().filter(c -> c != null && c.player >= 1 && !c.dead).count());
        } else {
            StartMenu.askPopulation(); // how many people the world begins with (before the first tile is made: its monsters follow the size)
            Map.population = Map.startingPopulation;
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
            if (start.kind == Starts.Kind.STANDARD_CUSTOM) editCharacterInteractive(bete, true);
            else {
                if (StartMenu.askYesNo("Your character", bete.name + " " + bete.lastName + " (" + bete.getTrait() + ", potential " + bete.potentialLabel() + "). Modify the character?", false)) editCharacterInteractive(bete, true);
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
            servePendingSaves(); // between ticks nothing is pending in tempCharacters/remCharacters (a Save asked while the game waits for the player or is paused is served there, at once)
            if (quitRequested && !(LoreEndings.worldEnded && GuiLore.busy())) { // saved (or not) above: leave (after the Garden's end the ending and credits windows are read first)
                try { Settings.save(); } catch (RuntimeException ignored) { }
                System.out.println("Goodbye.");
                System.exit(0);
            }
            Perf.begin();
            tickT0 = System.nanoTime(); tickBlocked0 = World.blockedNs; tickPurging = !epochVictims.isEmpty();
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
                Say.debug("[WORLD] Areas: " + createdAreas.size() + " | Sects: " + _sectCount + " | Cities: " + _cityCount + " | Legacies: " + legacyTitles.size());
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
                        Say.world("[SUCCESSION] " + area.name + " has changed hands! New lord: " + newName);
                    } else if (prevId == null && currId != null) {
                        Say.world("[SUCCESSION] " + area.name + " has a new founding lord: " + area.cityOwner.name + " " + area.cityOwner.lastName);
                    }
                }
                // Beast Tide: overflow Beast Lairs spread monsters to neighbors and threaten nearby cities
                int[] btDx = {-1, 1, 0, 0};
                int[] btDy = {0, 0, -1, 1};
                for (Area area : new ArrayList<>(createdAreas)) {
                    if (area.dungeonType == Area.DungeonType.BEAST_LAIR && area.monsters > area.lvl * 2000 && rand.nextInt(5) == 0) {
                        Say.world("A Beast Tide erupts from " + area.name + "! Monsters spill into the surrounding lands (" + Naming.event("tide", area.name, time / 52) + (Regions.regionOf(area) == null ? "" : ", " + Regions.regionOf(area)) + ").");
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
                                    Say.world("The tide strikes " + nbr.name + "! -" + casualties + " population" + defMsg);
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
                                cc.learnPlace(trialArea);
                            }
                        }
                        Say.world("A Celestial Trial has opened at " + trialArea.name + " [" + trialArea.x + "," + trialArea.y + "]! It will close in 10 ticks.");
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
                            Say.world("A spiritual storm ravages " + stormArea.name + "! Level surges to " + stormArea.lvl + "!");
                            break;
                        }
                        case 1: { // Plague — kills ~30% of cultivators in a random populated area
                            ArrayList<Area> _popAreas = new ArrayList<>();
                            for (Area _a : createdAreas) { if (!_a.characters.isEmpty()) _popAreas.add(_a); }
                            if (!_popAreas.isEmpty()) {
                                Area plagueArea = _popAreas.get(rand.nextInt(_popAreas.size()));
                                int killed = 0;
                                for (Character _c : new ArrayList<>(plagueArea.characters)) {
                                    if (_c != null && !_c.dead && _c.trait != Character.Traits.Beast && _c.trait != Character.Traits.Monster && !_c.childSafe() && rand.nextInt(3) == 0) {
                                        _c.dead();
                                        killed++;
                                    }
                                }
                                if (killed > 0)
                                    Say.world("A terrible plague strikes " + plagueArea.name + "! " + killed + " cultivators perish.");
                            }
                            break;
                        }
                        case 2: { // Heaven Fire — destroys arrays or culls beasts
                            Area fireArea = createdAreas.get(rand.nextInt(createdAreas.size()));
                            if (fireArea.arrayBag != null && !fireArea.arrayBag.isEmpty()) {
                                int destroyed = fireArea.arrayBag.size();
                                fireArea.arrayBag.clear();
                                Say.world("Heaven fire descends on " + fireArea.name + "! " + destroyed + " array(s) destroyed!");
                            } else {
                                fireArea.monsters = (int)(fireArea.monsters * 0.7);
                                Say.world("Heaven fire scorches " + fireArea.name + "! Beasts are culled.");
                            }
                            break;
                        }
                    }
                }
                // The Garden's Variety Tournament (year 10, year 100 and every 100 years after): everyone fights, nobody dies; before the epoch, so every living person is in it (`settings garden off` skips it)
                GardenTournament.yearly(_year);
                // Epoch purge: when the ticks have grown slow (the world is too crowded for this computer), 20% of each major level (levels with < 10 cultivators are spared) are marked to die
                epochCheck();
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
                // Shops that have run dry get a few plain wares again (a new player must have something to buy)
                for (Area _sa : createdAreas) for (Shop _sh : _sa.shops) if (_sh.owner != null && !_sh.owner.dead && _sh.inventory.size() < 5) _sh.seedStock();
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
                Naming.assignTitles(characters); // a kind of title is held by its ten best only
                Slim.yearly(); // the dead still held by living kin let go of their lists; the beasts people keep are counted
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
                        area.dungeonType = null; area.restoreName();
                        Say.world("The Celestial Trial at " + area.name + " has closed.");
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
            if (epochNow) epochCheck(); // cheat epoch: at once, not at the next year

            // Epoch purge in progress: the marked die in batches so the world doesn't stall on one huge tick
            if (!epochVictims.isEmpty()) {
                int batch = Math.min(EPOCH_BATCH, epochVictims.size());
                for (int _e = 0; _e < batch; _e++) {
                    Character v = epochVictims.remove(epochVictims.size() - 1);
                    if (v != null && !v.dead && v.player < 1 && !Rankings.epochSafe(v)) { v.deathCause = PURGE_CAUSE; v.dead(); } // a character a human has since taken over (or follows) is spared too
                }
                if (epochVictims.isEmpty()) { Say.world("[EPOCH] The purge has ended. The world falls quiet."); epochBlessing(); }
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
                Say.debug("[WARN] Parallel cultivate error: " + _ex);
                _ex.printStackTrace(); // the message alone hid the cause and the whole batch silently aborted every tick
            }

            Perf.mark(Perf.CULT);
            // Phase 2A: serial — player characters (interactive I/O must stay single-threaded)
            {   // who the death / war / birth notices go to
                List<Character> hs = new ArrayList<>();
                for (Character c : tickSnap) if (c.player == 1 && !c.dead) hs.add(c);
                Notifications.setHumans(hs);
            }
            // Phase 2B first: the NPCs act on the tile they stand in and only PLAN one step each (nobody leaves or enters a tile while the turns run: a mover is still there to be met,
            // fought or robbed). Then the people who play act, knowing where every walker is going; then all the steps are taken together.
            final ArrayList<Character> moved = new ArrayList<>(); // the NPCs with a planned step (or a ride with their leader)
            // The very first week of a world: the character speaks first (no NPC can ask it a question before it has even seen the screen); every other week the NPCs go first
            final boolean peopleFirst = time == 0;
            if (peopleFirst) peoplePhase(tickSnap);
            npcPhase(tickSnap, moved);
            // The births of the NPC phase join the list now, so the people who play (and anything that adds a character by hand) find ids and list positions equal, as they always did
            mergeBirths();
            movesPlanned = true;

            Perf.mark(Perf.P2B);
            if (!peopleFirst) peoplePhase(tickSnap);

            Perf.mark(Perf.P2A);
            applyMoves(moved, tickSnap);
            Perf.mark(Perf.STEPS);
            movesPlanned = false;
            followAllCompanions(tickSnap); // companions stand where their masters ended the turn
            Perf.mark(Perf.FOLLOW);

            LoreEndings.drainLeavers(); // ordinary people who rolled a leave on reaching Ascension or above go through the Seam
            LoreEndings.drainReleases(); // humans who left the world as soldiers choose their next character
            // Handle reincarnation for any player character that died this tick
            for (Character character : time % 52 == 0 ? new ArrayList<>(characters) : Map.takeReincarnations()) { // (the whole list once a year as a net)
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
                if (monster.dead) continue; // it lost the fight: a corpse does not walk on (and put itself back in a tile's list)
                monster.movement();
            }
            Perf.mark(Perf.MON);
            time++;
            yearlyDone = false;

            mergeBirths();

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
            Verify.tick(); // (only with -Dtdf.verify=true)
            Perf.mark(Perf.CLEAN); Perf.tickEnd();
            if (!tickPurging && !GardenTournament.heldThisTick) tickNs[tickCount++ % tickNs.length] = System.nanoTime() - tickT0 - (World.blockedNs - tickBlocked0); // this tick's own computing time (the tick of the Garden's Variety Tournament is left out: it is slow by design)
            GardenTournament.heldThisTick = false;
            // End of tick: let the GUI read a consistent world (bounded wait), then pause / sleep with the world lock released
            if (guiEnabled && mapViewer != null) World.handoff(mapViewer::refreshNow, 250);
            Perf.mark(Perf.GUI);
            pauseWaiting = paused;
            while (paused) { // pause support: wait until unpaused, answering the commands that take no time (buttons that open pop-ups, settings, trading, saving) while we wait
                servePausedInfo();
                if (mapViewer != null) mapViewer.refreshLogIfNew();
                blocked(() -> { Thread.sleep(50); return null; });
            }
            pauseWaiting = false;
            pausedNoticeShown = false;
            blocked(() -> {
                Thread.sleep(tickDelayMs);
                return null;
            });
        }
    }
}
