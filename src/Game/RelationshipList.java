package Game;

import java.util.ArrayList;

public class RelationshipList implements java.io.Serializable {
    static final long serialVersionUID = 1L;
    ArrayList<Relationship> relationships = null;

    public RelationshipList() {
        relationships = new ArrayList<Relationship>();
    }

    static final int CAP = Integer.getInteger("tdf.relCap", 40); // acquaintances kept per person
    public synchronized void add(Relationship relationship) {
        relationships.add(relationship);
        if (relationships.size() > CAP) evictOne();
    }
    /** The oldest acquaintance of no weight (|value| <= 1, not refused, never talked to) is forgotten; a bond of 2 or 3 stays. */
    /** Once a year: faint ties (|value| <= 2, never talked to, not refused) to people who are dead are forgotten, and the list is brought down to the cap (the weakest oldest first). */
    synchronized void trim() {
        for (int i = relationships.size() - 1; i >= 0; i--) {
            Relationship r = relationships.get(i);
            if (Math.abs(r.value) > 2 || r.refused || r.talks > 0) continue;
            if (r.friendID >= 0 && r.friendID < Map.characters.size()) {
                Character f = Map.characters.get(r.friendID);
                if (f == null || f.dead) relationships.remove(i);
            }
        }
        while (relationships.size() > CAP && evictOne()) { }
        relationships.trimToSize();
    }
    private synchronized boolean evictOne() {
        for (int i = 0; i < relationships.size() - 1; i++) {
            Relationship r = relationships.get(i);
            if (Math.abs(r.value) > 1 || r.refused || r.talks > 0) continue;
            Character other = r.friendID >= 0 && r.friendID < Map.characters.size() ? Map.characters.get(r.friendID) : null;
            if (other != null && other.player > 0) continue; // a person you play is never forgotten
            relationships.remove(i); return true;
        }
        return false;
    }

    public synchronized void remove(Relationship relationship) {
        relationships.remove(relationship);
    }

    public void removeAll(Relationship relationship) {
        relationships.removeAll(null);
    }
    /** Synchronized (the lists of turns on different tiles are touched by one thread at a time) and walked by index, so nothing is allocated for the commonest lookup. */
    public synchronized Relationship contains(int friendID) {
        final ArrayList<Relationship> l = relationships;
        for (int i = 0, n = l.size(); i < n; i++) {
            Relationship relationship = l.get(i);
            if (relationship.friendID == friendID) return relationship;
        }
        return null;
    }

    /** A copy to walk while the list may change (another turn, another thread). */
    public synchronized ArrayList<Relationship> copy() { return new ArrayList<Relationship>(relationships); }

    public synchronized void remove(int friendID) {
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
