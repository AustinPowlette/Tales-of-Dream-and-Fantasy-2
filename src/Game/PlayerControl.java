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
    /** What a spare moment of a turn can be used for. "Surprise me" is the old "Random" (index 3, so saved choices still mean the same) without its fights and corpse-looting; the last five are new. */
    static final String[] SPOT_ACTIONS = {"Meditate", "Hunt", "Profession", "Surprise me", "Talk", "Spar", "Forage", "Mine", "Explore"};
    /** The spot quest each action runs as (index = SPOT_ACTIONS). */
    static final String[] SPOT_QUESTS = {"Meditate", "Hunt", "Profession", "Random", "Talk", "Spar", "Forage", "Mine", "Explore"};
    int spotAction = 0;   // index into SPOT_ACTIONS (Meditate by default)

    Mode mode = Mode.MANUAL;
    boolean[] duties = new boolean[DUTY_NAMES.length];

    // Decision policies: answer the prompts when the character is not in MANUAL (AUTO/HYBRID) or no human is attached
    static final String[] SQUAD_FIGHT = {"join", "ask", "alone"};          // when you attack or hunt with a squad beside you: it joins, you are asked ("Call the squad" / "Go alone"), or you go alone
    int squadFight = 0;
    static final String[] SQUAD_POLICIES = {"ask", "auto", "never"};      // who may join your squad: MANUAL asks you each time, auto accepts, never refuses everyone
    static final String[] FEED_POLICIES = {"leftovers", "always", "never"}; // AUTO / HYBRID: do the companion beasts get fed? leftovers = food beyond 3 portions, always = the best food whenever there is any, never
    static final String[] FRIEND_POLICIES = {"ask", "accept", "decline"};  // a friendship someone offers: ask, accept it, or decline (nothing is lost by declining)
    int squadPolicy = 0, friendPolicy = 0;
    int feedPolicy = 0;      // index into FEED_POLICIES (0 leftovers, 1 always, 2 never): used while AUTO or a script runs; in MANUAL you `feed <n>` yourself
    java.util.HashSet<Integer> squadDeclined = new java.util.HashSet<>();  // cultivators already turned away (not asked again)
    java.util.HashMap<Integer, Integer> inviteRefused;  // person id -> the week they turned an `invite` down (null in old saves): the same person is not asked again for INVITE_WAIT weeks
    static final int INVITE_WAIT = 4;
    java.util.HashMap<Integer, Integer> giftWeek;        // person id -> the week a gift from you last warmed them (null in old saves): one friendship point a week per person, more gifts are still handed over
    transient boolean dedupedAtLoad = false;             // the queue was cleaned of identical quests since this game was loaded
    int killPolicy = 0;      // 0 always spare, 1 kill evil only, 2 kill if wronged, 3 always kill
    boolean fleePolicy = true;      // try to flee when outmatched
    boolean forbiddenPolicy = false; // use the Forbidden Technique when a fight turns deadly
    boolean duelPolicy = true;      // accept a challenge (false = try to slip away)
    int listLimit = 20;             // default number of rows a `list` shows (`limit <n>`, 1-500)
    int decisionTimeout = 0;        // seconds a prompt waits before the policy answer is used (0 = wait forever)

    HashSet<String> muted = new HashSet<>();      // quest names the player deleted for good
    static final int BT_ASK = 0, BT_AUTO = 1, BT_BUTTON = 2;
    static final String[] BREAKTHROUGH_POLICIES = {"ask", "auto", "button"};
    int breakthroughPolicy = BT_ASK; // a full bar at the end of a realm: ask me (the prompt), break through by itself (as an NPC does), or wait for the Breakthrough button / `breakthrough`
    java.util.HashSet<String> lessonsSeen;           // the lessons (Lessons) this person has been told (null in old saves)
    int nextLessonWeek = 0;
    int nextFavorWeek = 0;
    java.util.HashSet<String> vignettesSeen;         // the little stories (Vignettes) this person has lived (null in old saves)
    int nextVignetteWeek = 0;                        // the week the next little story may begin                           // the week someone may next ask this person for a favor (Favors)                          // the week the next lesson may be told
    boolean evadeDeath = false;      // a cheat policy: when death comes the person goes on living (an AUTO character too)
    boolean walkStopFight = false;   // a fight that finds you on a walk (Go There, goto, chase) stops the walk (off: the fight is fought and the walk goes on)
    boolean walkStopNotice = false;  // a new quest alert stops a walk (off: the alert waits for the next prompt)
    String attackedBy;               // who attacked this tick (for the 'walk stopped' line); not needed in old saves
    int nextGiftCheck = 0;                          // the week a best friend or a relative is next considered for bringing a gift (Gifts.weekly)
    java.util.HashMap<Integer, Integer> giftsFrom;  // giver id -> the week a gift from them was last sent for (null in old saves): the cooldown
    java.util.LinkedHashMap<String, Integer> prioRules; // the priority this player gives a kind of quest (null in old saves): key = type ("*" = any) + "|" + scope ("" = every one, "kind:family", "id:123", "tile:x,y")
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
        REASONS.put("Refine Blood", "an Evil alchemist can turn your Blood (two pills' worth or more) into Potential Pills and keeps half for the work");
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
        breakthroughPolicy = o.breakthroughPolicy;
        walkStopFight = o.walkStopFight; walkStopNotice = o.walkStopNotice; evadeDeath = o.evadeDeath;
        if (o.lessonsSeen != null) lessonsSeen = new java.util.HashSet<>(o.lessonsSeen);
        nextLessonWeek = o.nextLessonWeek; nextFavorWeek = o.nextFavorWeek;
        if (o.vignettesSeen != null) vignettesSeen = new java.util.HashSet<>(o.vignettesSeen);
        nextVignetteWeek = o.nextVignetteWeek;
        if (o.prioRules != null) prioRules = new java.util.LinkedHashMap<>(o.prioRules);
        mode = Mode.MANUAL;
    }

    // ---------------------------------------------------------------- priority rules (Control > Priorities, `qprio`)
    /** Every quest type a person can meet, with the priority it starts at (the first one in the code; the same type may start higher or lower in other places). */
    static final String[][] QUEST_TYPES = {
            {"Accept Sect Mission", "50"}, {"Attend Auction", "42"}, {"Battle Arch-Enemy", "24"}, {"Battle Dislike", "10"}, {"Battle Rival", "18"}, {"Bounty Hunt", "40"}, {"Buy City Array", "40"},
            {"Buy Gear", "45"}, {"Buy Pills", "38"}, {"Buy Sect Array", "40"}, {"Buy from Sect", "45"}, {"Courier Run", "34"}, {"Courtship", "32"}, {"Defend", "90"}, {"Deliver Goods", "65"},
            {"Deliver Materials", "65"}, {"Demand Tribute", "40"}, {"Disperse", "45"}, {"Donate Array", "50"}, {"Donate to Sect", "30"}, {"Entice Merchant", "45"}, {"Explore Dungeon", "35"},
            {"Festival Trade", "45"}, {"Find Parent's Body", "25"}, {"Find Person", "14"}, {"Forage", "40"}, {"Gathering Trip", "32"}, {"Get Pregnant", "25"}, {"Ghost City Rebirth", "50"},
            {"Gift Technique", "30"}, {"Give Gift", "30"}, {"Go Sell", "50"}, {"Heaven Treasure", "70"}, {"Hold Tournament", "35"}, {"Hunt Beast", "30"}, {"Hunt Cultivator", "55"}, {"Hunt", "30"}, {"Join Sect", "20"},
            {"Loot Corpse", "75"}, {"Market Trip", "30"}, {"Meditate", "15"}, {"Meditation Retreat", "40"}, {"Mine", "40"}, {"Perform", "40"}, {"Pilgrimage", "30"}, {"Possess Womb", "80"},
            {"Profession", "30"}, {"Propose Alliance", "30"}, {"Random", "25"}, {"Recruit Defector", "45"}, {"Recruit Member", "25"}, {"Refine Blood", "45"}, {"Register Tournament", "60"},
            {"Reincarnate", "95"}, {"Restock Trip", "30"}, {"Return to Sect", "25"}, {"Revenge", "60"}, {"Rob Caravan", "30"}, {"Sect Mission", "75"}, {"Sect Raid", "65"}, {"Seek Potential", "35"},
            {"Seek Teacher", "33"}, {"Seek Territory", "45"}, {"Sell Stock", "40"}, {"Sell Technique", "40"}, {"Sightseeing", "26"}, {"Spectate Tournament", "20"}, {"Study Trip", "35"},
            {"Tame Beast", "33"}, {"Teach Disciple", "30"}, {"Trade Run", "36"}, {"Visit Family", "28"}};
    /** Quest types that are about a person; the spot types are about nothing (done where you stand); every other type is about a place. */
    static final java.util.Set<String> PERSON_TYPES = new java.util.HashSet<>(java.util.Arrays.asList("Find Person", "Battle Dislike", "Battle Rival", "Battle Arch-Enemy", "Courtship", "Revenge", "Gift Technique", "Visit Family",
            "Teach Disciple", "Give Gift", "Propose Alliance", "Recruit Defector", "Recruit Member", "Seek Teacher", "Get Pregnant", "Demand Tribute", "Refine Blood", "Rob Caravan", "Possess Womb", "Bounty Hunt"));
    static final java.util.Set<String> SPOT_TYPES = new java.util.HashSet<>(java.util.Arrays.asList("Meditate", "Forage", "Mine", "Profession", "Hunt", "Hunt Cultivator", "Loot Corpse", "Random", "Reincarnate", "Accept Sect Mission",
            "Buy City Array", "Buy Sect Array", "Buy from Sect", "Donate to Sect", "Entice Merchant", "Hold Tournament", "Register Tournament", "Spectate Tournament")); // (done where you stand: their code is in Character.spotQuests; made as place quests they were only waypoints)
    /** Types the game manages itself (listed in the New quest window, but not created by hand). */
    static final java.util.Set<String> MANAGED_TYPES = new java.util.HashSet<>(java.util.Arrays.asList("Sect Mission"));

    /** What a quest type is about: "person", "place" or "spot" (nothing: done where you stand). */
    static String aboutOf(String type) { return PERSON_TYPES.contains(type) ? "person" : SPOT_TYPES.contains(type) ? "spot" : "place"; }

    /** Makes a quest by hand: any type, a person or a place where it needs one, a priority, pinned (it never ages and the priority rules leave it alone), repeating if asked. It is written through the one door
     *  every quest of a person who plays goes through (`Character.addPlayerQuest`: no second copy of the same errand). Returns what to tell him; a message that begins with "!" is a refusal. */
    transient Quest lastCreated = null; // the quest the last createQuest made or found
    String createQuest(Character c, String typed, Character person, Area area, int priority, boolean repeat) {
        String type = typeName(typed);
        if (type == null || type.equals("*")) return "!No quest type begins with '" + typed + "' (or it matches several). The New quest window lists them all.";
        if (MANAGED_TYPES.contains(type)) return "!" + type + " is given by the game (your sect's board: 'mission'); it cannot be made by hand.";
        String about = aboutOf(type);
        if (about.equals("person") && (person == null || person.dead || person == c)) return "!" + type + " needs a person other than you: '#<id>' or part of a name.";
        if (about.equals("person") && person.age < 8 && !type.equals("Visit Family") && !type.equals("Find Person")) return "!" + person.name + " is a child.";
        if (about.equals("place") && area == null) area = c.location;
        priority = Math.max(0, Math.min(100, priority));
        Quest q = about.equals("person") ? new Quest(type, !repeat, priority, 0, priority, person) : about.equals("place") ? new Quest(type, !repeat, priority, 0, priority, area) : new Quest(type, !repeat, priority, 0, priority);
        q.pin(priority);
        q.userCreated = true;
        Quest kept = about.equals("spot") ? c.addPlayerSpotQuest(q) : c.addPlayerQuest(q);
        if (kept == null) return "!Nothing was made.";
        lastCreated = kept;
        if (c.questAddWasDuplicate) return "Already in your Quest Log: " + describe(c, kept) + " (priority " + kept.priority + "). No second copy was made.";
        return "Created " + describe(c, kept) + (repeat ? " (repeating)" : "") + ". AUTO mode and scripts do it when it can be done; in MANUAL you are alerted, and 'qdo' / 'goquest' do it or walk there.";
    }

    static final String[] KINDS_OF_PERSON = {"family", "friend", "enemy", "sect", "stranger"};
    static final String[] KINDS_OF_PLACE = {"city", "sect", "dungeon", "wild"};

    /** The type as the game spells it ("hunt monster" -> "Hunt Beast"), or null when nothing matches. `*` stands for every type. */
    static String typeName(String typed) {
        if (typed == null) return null;
        String t = Quest.canon(typed.trim());
        if (t.equals("*") || t.equalsIgnoreCase("all") || t.equalsIgnoreCase("any")) return "*";
        for (String[] q : QUEST_TYPES) if (q[0].equalsIgnoreCase(t)) return q[0];
        String found = null;
        for (String[] q : QUEST_TYPES) if (q[0].toLowerCase().startsWith(t.toLowerCase())) { if (found != null) return null; found = q[0]; } // a unique beginning is enough
        return found;
    }

    /** Whom or what a quest is about, in the words a rule uses: a person is family, a friend, an enemy, a sect mate or a stranger; a place is a city, a sect, a dungeon or wild. null: it is about nothing. */
    static String kindOf(Character me, Quest q) {
        if (q.category == Quest.QuestCategory.Character) {
            Character t = Character.charById(q.targetCharacter);
            if (t == null) return null;
            if (PlayerLists.isKin(me, t)) return "family";
            Relationship r = me.relationshipList.contains(t.id);
            if (r != null && r.value >= 1) return "friend";
            if (r != null && r.value <= -1) return "enemy";
            if (me.sect != null && me.sect == t.sect) return "sect";
            return "stranger";
        }
        if (q.category == Quest.QuestCategory.Area && q.targetArea != null) return PlayerLists.placeType(q.targetArea);
        return null;
    }

    /** The rule key for a quest's own target: "id:<person>" or "tile:<x>,<y>" (null for a quest about nothing). */
    static String scopeOf(Quest q) {
        if (q.category == Quest.QuestCategory.Character && q.targetCharacter >= 0) return "id:" + q.targetCharacter;
        if (q.category == Quest.QuestCategory.Area && q.targetArea != null) return "tile:" + q.targetArea.x + "," + q.targetArea.y;
        return null;
    }

    /** The priority this player has chosen for a quest like `q`, or null: the most specific rule wins (this type and this target, any type and this target, this type and this kind of target, any type and this kind, this type). */
    Integer priorityRule(Character me, Quest q) {
        java.util.LinkedHashMap<String, Integer> r = prioRules;
        if (r == null || r.isEmpty()) return null;
        String scope = scopeOf(q), kind = kindOf(me, q);
        Integer v;
        if (scope != null) { if ((v = r.get(q.name + "|" + scope)) != null) return v; if ((v = r.get("*|" + scope)) != null) return v; }
        if (kind != null) { if ((v = r.get(q.name + "|kind:" + kind)) != null) return v; if ((v = r.get("*|kind:" + kind)) != null) return v; }
        return r.get(q.name + "|");
    }

    void setRule(String key, Integer value) {
        if (prioRules == null) prioRules = new java.util.LinkedHashMap<>();
        if (value == null) prioRules.remove(key); else prioRules.put(key, Math.max(0, Math.min(100, value)));
    }

    /** `qprio` words after the verb: `<type words> [@kind | #id | at:x,y] <n>` sets a rule, `... off` removes it. Returns the answer text. */
    String qprio(Character c, String[] w) {
        if (w.length == 0) return rulesText();
        if (w[0].equalsIgnoreCase("reset") || w[0].equalsIgnoreCase("clear")) {
            if (w.length == 1) { int n = prioRules == null ? 0 : prioRules.size(); prioRules = null; c.applyPriorityRules(); return "All " + n + " priority rules removed; quests rank as the game ranks them."; }
            w = java.util.Arrays.copyOfRange(w, 1, w.length); // `qprio reset <type> ...` = the same as `<type> ... off`
            w = java.util.Arrays.copyOf(w, w.length + 1); w[w.length - 1] = "off";
        }
        String last = w[w.length - 1];
        Integer value = null;
        boolean remove = last.equalsIgnoreCase("off") || last.equalsIgnoreCase("reset");
        if (!remove) {
            try { value = Integer.parseInt(last); } catch (NumberFormatException e) { return "Usage: qprio <type> [@family|@friend|@enemy|@sect|@stranger|@city|@dungeon|@wild | #<person id> | at:<x>,<y>] <0-100|off>   (qprio lists the rules, 'qprio reset' removes them all)"; }
            if (value < 0 || value > 100) return "A priority is 0-100.";
        }
        String scope = "";
        StringBuilder typeWords = new StringBuilder();
        for (int i = 0; i < w.length - 1; i++) {
            String x = w[i];
            if (x.startsWith("@") && x.length() > 1) {
                String k = x.substring(1).toLowerCase();
                boolean ok = false;
                for (String s : KINDS_OF_PERSON) if (s.equals(k)) ok = true;
                for (String s : KINDS_OF_PLACE) if (s.equals(k)) ok = true;
                if (!ok) return "Kinds of target: " + String.join(", ", KINDS_OF_PERSON) + " (people); " + String.join(", ", KINDS_OF_PLACE) + " (places).";
                scope = "kind:" + k;
            } else if (x.startsWith("#") && x.substring(1).matches("\\d+")) scope = "id:" + x.substring(1);
            else if (x.toLowerCase().startsWith("at:") && x.substring(3).matches("\\d+,\\d+")) scope = "tile:" + x.substring(3);
            else typeWords.append(typeWords.length() > 0 ? " " : "").append(x);
        }
        String type = typeWords.length() == 0 ? "*" : typeName(typeWords.toString());
        if (type == null) return "No quest type begins with '" + typeWords + "' (or it matches several). 'qprio' lists the rules; the types are in the Control window, Priorities tab.";
        if (type.equals("*") && scope.isEmpty()) return "Name a type, or a kind or a person: 'qprio Find Person 60', 'qprio @family 80', 'qprio #123 90'.";
        String key = type + "|" + scope;
        setRule(key, remove ? null : value);
        c.applyPriorityRules();
        return (remove ? "Rule removed: " : "Rule set: ") + ruleText(key, value) + (remove ? "" : "   (it replaces the game's own number for those quests; pinned quests keep theirs)");
    }

    static String ruleText(String key, Integer value) {
        int bar = key.indexOf('|');
        String type = key.substring(0, bar), scope = key.substring(bar + 1);
        String what = type.equals("*") ? "every quest" : type;
        String where = scope.isEmpty() ? "" : scope.startsWith("kind:") ? " about " + scope.substring(5) : scope.startsWith("id:") ? " about #" + scope.substring(3) : " at tile " + scope.substring(5);
        return what + where + (value == null ? "" : " = " + value);
    }

    String rulesText() {
        if (prioRules == null || prioRules.isEmpty()) return "No priority rules: quests rank as the game ranks them. 'qprio Find Person 60', 'qprio @family 80', 'qprio #123 90', 'qprio Forage off', 'qprio reset'.";
        StringBuilder sb = new StringBuilder("Priority rules (most specific wins; a pinned quest keeps its own):\n");
        for (java.util.Map.Entry<String, Integer> e : prioRules.entrySet()) sb.append("  ").append(ruleText(e.getKey(), e.getValue())).append("\n");
        return sb.toString();
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
        return c.addPlayerQuest(q); // an identical waypoint already in the log is raised, not copied
    }
}
