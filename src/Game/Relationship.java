package Game;

public class Relationship {
    int friendID;
    int value;

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
