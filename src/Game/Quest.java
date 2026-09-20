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



    @Override
    public int compareTo(Quest o) {
        // Testing System.out.println("Quests being compared");
        return Integer.compare(o.priority, this.priority);
    }

    public String toString() {
        if (category == QuestCategory.Character){
            return "" + name + " [" + Map.characters.get(targetCharacter) + "] / " + priorityDiff;
        }
        return "" + name + " " + priority;
    }


}
