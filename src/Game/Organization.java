package Game;

import java.util.ArrayList;

public class Organization {
    String name = "";
    Character founder;
    Character master;
    ArrayList<Character> members;
    Area location;

    // Recruitment criteria — null means no restriction
    int minPotential = 0;          // minimum potential to join
    int minLevel = 0;              // minimum lvl to join
    String genderPref = null;      // "Male", "Female", or null (any)
    String affiliationMatch = null; // "Good", "Neutral", "Evil", or null (any)
    Character.Traits traitRequired = null; // specific trait required, or null

    public Organization(Character founder) {
        this.founder = founder;
        this.master = founder;
        name = (founder + "'s Sect");
        members  = new ArrayList<>();
        location = founder.location;
        founder.sect = this;
    }

    public boolean canJoin(Character c) {
        if (c.dead) return false;
        if (c.sect != null) return false;
        if (c.potential < minPotential) return false;
        if (c.lvl < minLevel) return false;
        if (genderPref != null && !genderPref.equals(c.gender)) return false;
        if (affiliationMatch != null && !affiliationMatch.equals(c.affiliation)) return false;
        if (traitRequired != null && c.trait != traitRequired) return false;
        return true;
    }

    public void join(Character member) {
        if (!canJoin(member)) return;
        members.add(member);
        member.sect = this;
    }
    public void resign(Character member) {
        members.remove(member);
        member.sect = null;
        if (isMaster(member)) {
            master = null;
            promote();
        }
    }
    public void promote() {
        if (members != null && !members.isEmpty()) {
            Character temp = null;
            for (Character character : members) {
                if (temp == null) {
                    temp = character;
                }
                else if (temp.lvl < character.lvl) {
                    temp = character;
                }
                else if (temp.lvl == character.lvl && temp.tempLvl < character.tempLvl) {
                    temp = character;
                }
            }
            master = temp;
        }
        else if (master == null) {
            location.sect = null;
        }
    }

    public boolean isMaster(Character character) {
        if (character == master) {
            return true;
        }
        return false;
    }

    public String toString() {
        return name;
    }
}
