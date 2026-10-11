package Game;

public class Quest implements Comparable<Quest>, java.io.Serializable {
    static final long serialVersionUID = 1L;
    enum QuestCategory {
    Area, Character, Spot
}
String name = "";
QuestCategory category;
boolean oneTime = false;
int priority;
int priorityDiff;
int maxPriority;
Area targetArea;
int targetCharacter;
int createdAt = GameDriver.time; // game week the quest was posted (0 on old saves = unknown, never expires)
boolean pinned = false;      // player-set priority: never ages, is never clamped, expires or gets shed
int pinnedPriority = 0;      // the priority a pinned quest holds (and returns to after a repeating quest runs)
boolean userCreated = false; // a player-authored waypoint quest (never muted, never expires)
int uid = ++nextUid;         // stable id so the GUI and `qp #uid` / `qd #uid` / `qdo #uid` can name a quest (list numbers shift)
static int nextUid = 0;
transient String bitName; transient long bitVal; // the guard bit of the name (see Character.Q)
String label = null;        // a text that replaces the name in lists (the Sect Mission quest shows what the mission asks)
int progress = 0;          // turns spent so far on a quest that stays a while (a retreat, a pilgrimage, an auction wait: see QuestLife)
boolean bounty = false;      // Hunt Beast posted on a city bounty board (a "bounty" duty for player characters)


    public Quest (String name, boolean oneTime, int priority, int priorityDiff, int maxPriority) {
        this.name = name;
        this.category = QuestCategory.Spot;
        this.oneTime = oneTime;
        this.priority = priority;
        this.priorityDiff = priorityDiff;
        this.maxPriority = maxPriority;
        this.targetArea = targetArea;
    }

public Quest (String name, boolean oneTime, int priority, int priorityDiff, int maxPriority, Area targetArea) {
    this.name = name;
    this.category = QuestCategory.Area;
    this.oneTime = oneTime;
    this.priority = priority;
    this.priorityDiff = priorityDiff;
    this.maxPriority = maxPriority;
    this.targetArea = targetArea;
}

public Quest (String name, boolean oneTime, int priority, int priorityDiff, int maxPriority, Character targetCharacter) {
    this.name = name;
    this.category = QuestCategory.Character;
    this.oneTime = oneTime;
    this.priority = priority;
    this.priorityDiff = priorityDiff;
    this.maxPriority = maxPriority;
    this.targetCharacter = targetCharacter.id;
    this.targetArea = targetCharacter.location;
}

public void questDeconstructor () {
    targetArea = null;
}

    // Player-set priority: holds the value forever (priorityDiff 0, maxPriority = value)
    public void pin(int value) {
        pinned = true;
        pinnedPriority = value;
        priority = value;
        priorityDiff = 0;
        maxPriority = value;
    }

    // Priority a repeating quest drops to after it runs (a pinned quest keeps its player-set value)
    public void resetPriority() {
        priority = pinned ? pinnedPriority : 0;
    }

    // Name shown to the player (the internal quest name stays stable)
    public String displayName() {
        if (label != null) return label;
        return shown(name);
    }

    /** The name the player sees for an internal quest name ("Hunt Beast" is shown as "Hunt Monster", "Get Pregnant" as "Have Child"). */
    static String shown(String n) { return "Get Pregnant".equals(n) ? "Have Child" : "Hunt Beast".equals(n) ? "Hunt Monster" : n; }

    /** The internal name for a name a player typed ("Hunt Monster" is shown for the quest saved and coded as "Hunt Beast"). */
    static String canon(String n) {
        return n != null && n.equalsIgnoreCase("Hunt Monster") ? "Hunt Beast" : n;
    }

    @Override
    public int compareTo(Quest o) {
        // Testing System.out.println("Quests being compared");
        return Integer.compare(o.priority, this.priority);
    }

    public String toString() {
        String pin = pinned ? " [pinned]" : "";
        if (category == QuestCategory.Character){
            return "" + displayName() + " [" + Character.charById(targetCharacter) + "] " + priority + pin;
        }
        return "" + displayName() + " " + priority + pin;
    }


}
