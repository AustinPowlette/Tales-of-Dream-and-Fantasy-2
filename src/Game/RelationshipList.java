package Game;

import java.util.ArrayList;

public class RelationshipList implements java.io.Serializable {
    static final long serialVersionUID = 1L;
    ArrayList<Relationship> relationships = null;

    public RelationshipList() {
        relationships = new ArrayList<Relationship>();
    }

    public void add(Relationship relationship) {
        relationships.add(relationship);
    }

    public void remove(Relationship relationship) {
        relationships.remove(relationship);
    }

    public void removeAll(Relationship relationship) {
        relationships.removeAll(null);
    }
    public Relationship contains(int friendID) {
        for (Relationship relationship : relationships) {
            if (relationship.friendID == friendID) {
                return relationship;
            }
        }
        return null;
    }

    public void remove(int friendID) {
        relationships.remove(contains(friendID));
    }

    public String toString () {
        String temp = "";
        for (Relationship relationship : relationships) {
            temp += "[" + relationship + "] ";
        }
        return temp;
    }



}
