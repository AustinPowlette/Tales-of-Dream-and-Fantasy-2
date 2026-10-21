package Game;

public class Relationship implements java.io.Serializable {
    static final long serialVersionUID = 1L;
    int friendID;
    int value;
    int talks = 0;             // conversations held with this person (Dialogue)
    int lastTalk = -1000;      // the week of the last one
    int lastAdvice = -1000;    // the week someone last gave you a lesson (a lesson is offered once in half a year)
    int topics = 0;            // bit mask of the topics already raised this week-long visit (reset when a week passes since lastTalk)
    boolean refused = false;   // a human declined (or avoids) this friendship: it never grows past 0, and the entry is kept so the question is not asked again

    public Relationship (int friendID, int value) {
        this.friendID = friendID;
        this.value = value;
    }

    public void changeValue (int value) {
        this.value = value;
    }

    public String toString (){
        return (Map.characters.get(friendID) + " / " + value);
    }
}
