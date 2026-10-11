package Game;

import java.util.ArrayList;
import java.util.HashSet;

/** Per-character player-control state: mode, managed-duty toggles, decision policies, muted quests, quest alerts and the
 *  hybrid script. Only human-controlled characters (player == 1) carry one; NPCs never have it. See planning.md. */
public class PlayerControl implements java.io.Serializable {
    static final long serialVersionUID = 1L;

    enum Mode { MANUAL, AUTO, HYBRID }

    // Managed duties: each is a toggle. ON = the character does that work by itself the way an NPC would; OFF = the player does it.
    static final String[] DUTY_NAMES = {"city", "sect", "shop", "craft", "upkeep", "bounty", "tournament", "social", "family", "loot"};
    static final int CITY = 0, SECT = 1, SHOP = 2, CRAFT = 3, UPKEEP = 4, BOUNTY = 5, TOURNAMENT = 6, SOCIAL = 7, FAMILY = 8, LOOT = 9;
    static final int MUNDANE = -1; // classification: silently ignored in MANUAL/HYBRID
    static final int ALERT = -2;   // classification: announced with target + coordinates; the player goes there and uses Do Quest

    static final int MAX_ALERTS_PER_TURN = 3; // more new alerts than this in one turn are summarised
    static final int ALERT_TTL_WEEKS = 26; // an unanswered one-time spot quest (no location, nothing to walk to) lapses after half a year
    static final String[] KILL_POLICIES = {"always spare", "kill evil only", "kill if wronged", "always kill"};

    /** What the character does by itself in the spot slot of a turn (travelling, waiting) when its routine would pick one of the four plain spot quests; set in the Quest Log or with `spotaction`. */
    static final String[] SPOT_ACTIONS = {"Meditate", "Hunt", "Profession", "Random"};
    int spotAction = 0;   // index into SPOT_ACTIONS (Meditate by default)

    Mode mode = Mode.MANUAL;
    boolean[] duties = new boolean[DUTY_NAMES.length];

    // Decision policies: answer the prompts when the character is not in MANUAL (AUTO/HYBRID) or no human is attached
    static final String[] SQUAD_POLICIES = {"ask", "auto", "never"};      // who may join your squad: MANUAL asks you each time, auto accepts, never refuses everyone
    static final String[] FEED_POLICIES = {"leftovers", "always", "never"}; // AUTO / HYBRID: do the companion beasts get fed? leftovers = food beyond 3 portions, always = the best food whenever there is any, never
    static final String[] FRIEND_POLICIES = {"ask", "accept", "decline"};  // a friendship someone offers: ask, accept it, or decline (nothing is lost by declining)
    int squadPolicy = 0, friendPolicy = 0;
    int feedPolicy = 0;      // index into FEED_POLICIES (0 leftovers, 1 always, 2 never): used while AUTO or a script runs; in MANUAL you `feed <n>` yourself
    java.util.HashSet<Integer> squadDeclined = new java.util.HashSet<>();  // cultivators already turned away (not asked again)
    int killPolicy = 0;      // 0 always spare, 1 kill evil only, 2 kill if wronged, 3 always kill
    boolean fleePolicy = true;      // try to flee when outmatched
    boolean forbiddenPolicy = false; // use the Forbidden Technique when a fight turns deadly
    boolean duelPolicy = true;      // accept a challenge (false = try to slip away)
    int listLimit = 20;             // default number of rows a `list` shows (`limit <n>`, 1-500)
    int decisionTimeout = 0;        // seconds a prompt waits before the policy answer is used (0 = wait forever)

    HashSet<String> muted = new HashSet<>();      // quest names the player deleted for good
    ArrayList<Quest> inbox = new ArrayList<>();   // announced quests still waiting in a queue
    ArrayList<Quest> view = new ArrayList<>();    // the numbering shown by the last quest listing
    PlayerScript script = null;
    boolean returnToAuto = false;   // when a script ends or is interrupted: false = MANUAL, true = AUTO
    boolean interruptOnAlert = false; // a new quest alert pauses the running script
    int autoTurns = 0;              // AUTO: turns left before dropping back to the prompt (0 = until stopped)
    boolean movedThisTick = false;  // the player walked this tick; a duty quest must not also path-find
    volatile java.util.Set<String> watchCats = null; // `watch ... for a,b`: only these notice categories end it (null = any except hints and recaps)
    volatile boolean watching = false;   // `watch`: fast-forward until the next notice or attack
    int limitedWeek = -1, limitedCount = 0, limitedNoticeWeek = -1; // challenges from strangers / dislikers this turn (see Character.acceptChallenge)
    boolean attackedThisTick = false; // a hostile fight touched the character this tick (script interrupt)
    int interruptMajor = -1;         // major level when the script started (level-up interrupt)
    boolean alertPending = false;    // a new quest alert arrived since the script last looked
    transient ArrayList<PlayerLists.ShopItem> itemView = new ArrayList<>(); // numbering shown by the last shop listing
    transient ArrayList<Character> peopleView = new ArrayList<>(); // numbering shown by the last 'people' listing

    // ---------------------------------------------------------------- classification
    // Which quests each mode treats as mundane / alert / duty. Anything unlisted is announced.
    static int classify(Character c, Quest q) {
        if (q.userCreated) return ALERT;
        switch (q.name) {
            case "Random": case "Profession": case "Hunt": case "Meditate": case "Forage": case "Mine": case "Interact":
            case "Sell Technique": case "Find Person": case "Buy Gear": case "Go Sell": case "Return to Sect":
            case "Seek Territory": case "Hunt Cultivator": case "Perform": case "Visit Family": case "Courtship": case "Trade Run": case "Pilgrimage":
            case "Meditation Retreat": case "Tame Beast": case "Buy Pills": case "Attend Auction": case "Market Trip": case "Gathering Trip": case "Seek Teacher": case "Sightseeing": case "Courier Run":
                return MUNDANE;
            case "Hunt Beast":
                return q.bounty ? BOUNTY : MUNDANE;
            case "Bounty Hunt": return BOUNTY;
            case "Loot Corpse": return LOOT;
            case "Register Tournament": return TOURNAMENT;
            case "Battle Dislike": return SOCIAL;
            case "Seek Potential": case "Refine Blood": case "Study Trip": return UPKEEP;
            case "Get Pregnant": return FAMILY;
            case "Accept Sect Mission": case "Donate to Sect": case "Buy from Sect": case "Buy Sect Array":
            case "Recruit Member": case "Recruit Defector": case "Propose Alliance": case "Demand Tribute":
            case "Deliver Materials": case "Donate Array": case "Sect Raid": case "Teach Disciple": case "Found Sect":
                return SECT;
            case "Buy City Array": case "Entice Merchant": case "Found City":
                return CITY;
            case "Defend":
                return q.targetArea != null && q.targetArea.sect != null ? SECT : CITY;
            case "Hold Tournament":
                return (c != null && c.location != null && c.location.cityOwner == c) ? CITY : SECT;
            case "Restock Trip": case "Sell Stock":
                return SHOP;
            default:
                return ALERT;
        }
    }

    static String classText(int cls) {
        if (cls == MUNDANE) return "routine";
        if (cls == ALERT) return "alert";
        return "duty:" + DUTY_NAMES[cls];
    }

    static final java.util.HashMap<String, String> REASONS = new java.util.HashMap<>();
    static {
        REASONS.put("Revenge", "a friend of yours was killed");
        REASONS.put("Bounty Hunt", "a bounty was posted in a city");
        REASONS.put("Battle Rival", "you have a rival");
        REASONS.put("Battle Arch-Enemy", "you have an arch-enemy");
        REASONS.put("Explore Dungeon", "a dungeon you know of matches your level");
        REASONS.put("Heaven Treasure", "a heavenly technique lies unclaimed");
        REASONS.put("Rob Caravan", "a wealthy merchant is nearby");
        REASONS.put("Join Sect", "a known sect could take you in");
        REASONS.put("Gift Technique", "a child of yours lacks your best technique");
        REASONS.put("Find Parent's Body", "your parent's resting place");
        REASONS.put("Ghost City Rebirth", "a ghost's chance at rebirth");
        REASONS.put("Possess Womb", "a pregnant woman weaker than you");
        REASONS.put("Disperse", "your tile is an overcrowded hotspot");
        REASONS.put("Defend", "your sect or city is under attack");
        REASONS.put("Spectate Tournament", "a tournament is open here");
        REASONS.put("Register Tournament", "a tournament in your realm is open here");
        REASONS.put("Sect Raid", "your sect is at war or an enemy sect is in crisis");
        REASONS.put("Hunt Beast", "a city bounty on monsters");
        REASONS.put("Battle Dislike", "you dislike someone");
        REASONS.put("Seek Potential", "a Potential Pill is on the auction block and your potential is low");
        REASONS.put("Study Trip", "a library sells a better technique than yours and you can pay for it");
        REASONS.put("Refine Blood", "an alchemist can turn your Blood into Potential Pills for a fee");
        REASONS.put("Perform", "a city nearby would pay for a concert");
        REASONS.put("Visit Family", "a relative lives some way off");
        REASONS.put("Market Trip", "you have no meal and a city market sells one");
        REASONS.put("Gathering Trip", "richer herbs or ore lie some way off");
        REASONS.put("Seek Teacher", "someone far stronger might teach you");
        REASONS.put("Sightseeing", "a notable place lies some way off");
        REASONS.put("Courier Run", "a city would pay for word from afar");
        REASONS.put("Teach Disciple", "a disciple or child could learn from you");
        REASONS.put("Courtship", "someone free and suitable is near");
        REASONS.put("Trade Run", "you carry herbs and metal you do not need and a city can pay");
        REASONS.put("Pilgrimage", "a famous place is within reach");
        REASONS.put("Meditation Retreat", "a spirit vein is near (or a partner invited you to one)");
        REASONS.put("Tame Beast", "a beast you could tame lives nearby");
        REASONS.put("Buy Pills", "a city sells the pill for your next realm and you can pay");
        REASONS.put("Attend Auction", "an auction is coming with a lot you want");
        REASONS.put("Found City", "a good place for a city is near (a spirit vein is best)");
        REASONS.put("Found Sect", "a good place for a sect is near (a spirit vein is best)");
        REASONS.put("Get Pregnant", "your spouse is at hand");
        REASONS.put("Loot Corpse", "a fresh corpse lies here");
    }

    // A new body keeps the player's settings (duty toggles, policies, muted quests) but starts MANUAL with no script or alerts
    void inheritFrom(PlayerControl o) {
        duties = o.duties.clone();
        killPolicy = o.killPolicy;
        squadPolicy = o.squadPolicy; friendPolicy = o.friendPolicy; feedPolicy = o.feedPolicy;
        fleePolicy = o.fleePolicy;
        forbiddenPolicy = o.forbiddenPolicy;
        duelPolicy = o.duelPolicy;
        decisionTimeout = o.decisionTimeout;
        listLimit = o.listLimit;
        returnToAuto = o.returnToAuto;
        interruptOnAlert = o.interruptOnAlert;
        muted.addAll(o.muted);
        mode = Mode.MANUAL;
    }

    // ---------------------------------------------------------------- duties
    int dutiesOn() { int n = 0; for (boolean b : duties) if (b) n++; return n; }

    String dutySummary() {
        int n = dutiesOn();
        return n == 0 ? "all off" : n == duties.length ? "all on" : "mixed (" + n + "/" + duties.length + " on)";
    }

    void setAllDuties(boolean on) { java.util.Arrays.fill(duties, on); }

    static int dutyIndex(String name) {
        if (name == null) return -1;
        for (int i = 0; i < DUTY_NAMES.length; i++) if (DUTY_NAMES[i].equalsIgnoreCase(name)) return i;
        return -1;
    }

    // ---------------------------------------------------------------- quest filtering (MANUAL / HYBRID)
    // A quest runs by itself only in AUTO mode or when its duty toggle is on
    boolean allowedToRun(Character c, Quest q) {
        if (mode == Mode.AUTO) return true;
        int cls = classify(c, q);
        return cls >= 0 && duties[cls];
    }

    // ---------------------------------------------------------------- alerts
    static String coord(Area a) {
        return a == null ? "unknown" : "[" + (a.x * 100) + ", " + (a.y * 100) + "]";
    }

    static int distance(Area from, Area to) {
        if (from == null || to == null) return 0;
        int dx = Math.abs(from.x - to.x), dy = Math.abs(from.y - to.y);
        return Math.min(dx, Map.range - dx) + Math.min(dy, Map.range - dy);
    }

    // One line describing a quest: name, target, coordinates and distance, priority, classification
    String describe(Character c, Quest q) {
        StringBuilder sb = new StringBuilder(q.displayName());
        if (q.category == Quest.QuestCategory.Character) {
            Character t = Character.charById(q.targetCharacter);
            if (t != null) {
                sb.append(" -> ").append(t.name).append(" ").append(t.lastName).append(" (Lv.").append(t.lvl).append(")");
                if (t.location != null) sb.append(" at ").append(coord(t.location)).append(", ").append(distance(c.location, t.location)).append(" tiles away");
            }
        } else if (q.category == Quest.QuestCategory.Area && q.targetArea != null) {
            sb.append(" -> ").append(q.targetArea.name).append(" at ").append(coord(q.targetArea)).append(", ")
              .append(distance(c.location, q.targetArea)).append(" tiles away");
        } else {
            sb.append(" (here, no travel)");
        }
        sb.append("  pri ").append(q.priority);
        if (q.pinned) sb.append(" pinned");
        return sb.toString();
    }

    // Announces quests the mode won't run by itself; returns true if something new was announced
    boolean scanAlerts(Character c) {
        if (mode == Mode.AUTO) { inbox.clear(); return false; }
        final int now = GameDriver.time;
        // Unanswered one-time spot quests lapse (their trigger re-posts them when the situation still holds)
        for (Quest q : new ArrayList<>(c.spotQuests)) {
            if (q.oneTime && !q.pinned && !q.userCreated && q.createdAt > 0 && now - q.createdAt > ALERT_TTL_WEEKS
                    && classify(c, q) != MUNDANE) c.spotQuests.remove(q);
        }
        inbox.removeIf(q -> !c.targetQuests.contains(q) && !c.spotQuests.contains(q) && q != c.currentQuest);
        boolean fresh = false;
        int shown = 0, hidden = 0, fresh2 = 0;
        String firstText = null;
        ArrayList<Quest> all = new ArrayList<>(c.targetQuests);
        all.addAll(c.spotQuests);
        if (c.currentQuest != null) all.add(c.currentQuest);
        all.sort((a, b) -> Integer.compare(b.priority, a.priority)); // the most pressing alerts are the ones shown
        for (Quest q : all) {
            int cls = classify(c, q);
            if (cls == MUNDANE || (cls >= 0 && duties[cls])) continue;
            if (inbox.contains(q)) continue;
            inbox.add(q);
            fresh = true;
            fresh2++;
            if (firstText == null) firstText = q.displayName();
            if (++shown > MAX_ALERTS_PER_TURN) { hidden++; continue; }
            String why = REASONS.get(q.name);
            System.out.println("[Quest Alert] " + describe(c, q) + (why != null ? "  (" + why + ")" : "")
                    + (q.category == Quest.QuestCategory.Spot ? "  -> use 'qdo' here" : "  -> travel there, then 'qdo'"));
        }
        if (fresh2 > 0) Notifications.onAlerts(c, fresh2, firstText);
        if (hidden > 0) System.out.println("[Quest Alert] ...and " + hidden + " more (type 'inbox' to list them all).");
        return fresh;
    }

    // ---------------------------------------------------------------- quest queue editing
    ArrayList<Quest> buildView(Character c) {
        ArrayList<Quest> rows = new ArrayList<>();
        if (c.currentQuest != null) rows.add(c.currentQuest);
        ArrayList<Quest> t = new ArrayList<>(c.targetQuests);
        t.sort((a, b) -> Integer.compare(b.priority, a.priority));
        rows.addAll(t);
        ArrayList<Quest> s = new ArrayList<>(c.spotQuests);
        s.sort((a, b) -> Integer.compare(b.priority, a.priority));
        rows.addAll(s);
        view = rows;
        return rows;
    }

    String listQuests(Character c) {
        ArrayList<Quest> rows = buildView(c);
        StringBuilder sb = new StringBuilder("=== Quests (mode: " + mode + ") ===\n");
        if (rows.isEmpty()) sb.append("  (none)\n");
        else {
            TermFormat.Table tb = new TermFormat.Table("#", "State", "Kind", "Quest", "Where", "Dist", "Pri", "Notes").raw(0, 5, 6).max(3, 28).max(4, 44);
            int firstAlert = -1;
            for (int i = 0; i < rows.size(); i++) {
                Quest q = rows.get(i);
                int cls = classify(c, q);
                if (firstAlert < 0 && cls == ALERT) firstAlert = i + 1;
                String where = "here";
                int dist = 0;
                if (q.category == Quest.QuestCategory.Character) {
                    Character t = Character.charById(q.targetCharacter);
                    if (t != null) { where = t.name + " " + t.lastName + " (Lv." + t.lvl + ") " + (t.location != null ? coord(t.location) : ""); dist = t.location != null ? distance(c.location, t.location) : 0; }
                    else where = "(gone)";
                } else if (q.category == Quest.QuestCategory.Area && q.targetArea != null) { where = q.targetArea.name + " " + coord(q.targetArea); dist = distance(c.location, q.targetArea); }
                String notes = (q.pinned ? "pinned " : "") + (q.userCreated ? "yours" : "");
                tb.row(i + 1, q == c.currentQuest ? "ACTIVE" : c.spotQuests.contains(q) ? "spot" : "target", classText(cls), q.displayName(), where, dist, q.priority, notes.trim());
            }
            sb.append(tb.render());
            sb.append("Kind: alert = yours to do (you are told when one appears); routine = runs by itself in AUTO or when its duty is on; duty:x = runs when that duty is on.\n");
            if (firstAlert > 0) {
                Quest fa = rows.get(firstAlert - 1);
                boolean here = fa.category == Quest.QuestCategory.Spot || (fa.targetArea != null && fa.targetArea == c.location);
                sb.append(TermFormat.hint(here ? "qdo " + firstAlert + "   (do " + fa.displayName() + " now)" : "goquest " + firstAlert + "   (walk to " + fa.displayName() + "), then  qdo " + firstAlert));
            }
        }
        if (!muted.isEmpty()) sb.append("Muted: ").append(muted).append("\n");
        sb.append("qp <n> <pri> pin priority | qd <n> delete | qdo <n> do it | qu <n> unpin | mute/unmute <name> | qnew <label> <x> <y> <pri> [repeat]\n");
        return sb.toString();
    }

    // Quests loaded from an old save (or inherited from an NPC body) may carry uid 0: number them so the GUI can address them
    void assignUids(Character c) {
        for (Quest q : c.targetQuests) if (q.uid == 0) q.uid = ++Quest.nextUid;
        for (Quest q : c.spotQuests) if (q.uid == 0) q.uid = ++Quest.nextUid;
        if (c.currentQuest != null && c.currentQuest.uid == 0) c.currentQuest.uid = ++Quest.nextUid;
    }

    // A quest named by `#<uid>` (stable) or by its 1-based number in the last listing
    Quest rowRef(Character c, String ref) {
        if (ref != null && ref.startsWith("#")) {
            int u;
            try { u = Integer.parseInt(ref.substring(1)); } catch (NumberFormatException e) { System.out.println("Use #<uid> or a list number."); return null; }
            if (c.currentQuest != null && c.currentQuest.uid == u) return c.currentQuest;
            for (Quest q : c.targetQuests) if (q.uid == u) return q;
            for (Quest q : c.spotQuests) if (q.uid == u) return q;
            System.out.println("That quest is gone (finished or replaced).");
            return null;
        }
        int n;
        try { n = Integer.parseInt(ref); } catch (Exception e) { System.out.println("Give a quest number from 'q', or #<uid>."); return null; }
        return row(c, n);
    }

    // Row n (1-based) of the last listing, still present in a queue; null with a printed reason otherwise
    Quest row(Character c, int n) {
        if (n < 1 || n > view.size()) { System.out.println("No quest #" + n + ". List quests with 'q' first."); return null; }
        Quest q = view.get(n - 1);
        if (q != c.currentQuest && !c.targetQuests.contains(q) && !c.spotQuests.contains(q)) {
            System.out.println("That quest is gone (finished or replaced). List quests with 'q' again.");
            return null;
        }
        return q;
    }

    boolean setPriority(Character c, Quest q, int value) {
        value = Math.max(0, Math.min(1000, value));
        boolean inTarget = c.targetQuests.remove(q);
        boolean inSpot = !inTarget && c.spotQuests.remove(q);
        q.pin(value);
        if (inTarget) c.targetQuests.add(q);
        else if (inSpot) c.spotQuests.add(q);
        return true;
    }

    boolean unpin(Character c, Quest q) {
        if (!q.pinned) return false;
        boolean inTarget = c.targetQuests.remove(q);
        boolean inSpot = !inTarget && c.spotQuests.remove(q);
        q.pinned = false;
        q.priorityDiff = 1;
        q.maxPriority = Math.max(q.priority, 60);
        if (inTarget) c.targetQuests.add(q);
        else if (inSpot) c.spotQuests.add(q);
        return true;
    }

    void delete(Character c, Quest q) {
        c.targetQuests.remove(q);
        c.spotQuests.remove(q);
        if (q == c.currentQuest) c.currentQuest = null;
        inbox.remove(q);
    }

    // Deletes every quest of this name and stops the triggers re-posting it
    int mute(Character c, String name) {
        muted.add(name);
        int removed = 0;
        for (Quest q : new ArrayList<>(c.targetQuests)) if (q.name.equalsIgnoreCase(name) && !q.userCreated) { delete(c, q); removed++; }
        for (Quest q : new ArrayList<>(c.spotQuests)) if (q.name.equalsIgnoreCase(name) && !q.userCreated) { delete(c, q); removed++; }
        if (c.currentQuest != null && c.currentQuest.name.equalsIgnoreCase(name) && !c.currentQuest.userCreated) { delete(c, c.currentQuest); removed++; }
        return removed;
    }

    // Player-authored waypoint: reaching the tile completes it (repeat = it returns to the queue); pinned at the given priority
    Quest createWaypoint(Character c, String label, int tx, int ty, int priority, boolean repeat) {
        Area a = (tx >= 0 && tx < Map.range && ty >= 0 && ty < Map.range) ? Map.tiles[tx][ty] : null;
        if (a == null) { // a quest needs a tile object to path to; tiles only exist once someone has been there
            System.out.println("That tile hasn't been discovered yet — you can only mark places that have been visited.");
            return null;
        }
        Quest q = new Quest("Go: " + label, !repeat, priority, 0, priority, a); // the prefix keeps it from colliding with built-in quest names
        q.userCreated = true;
        q.pin(priority);
        c.targetQuests.add(q);
        return q;
    }
}
