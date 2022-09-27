package Game;

import java.util.ArrayList;

public class Organization {
    String name = "";
    Character founder;
    Character master;
    ArrayList<Character> members;
    Area location;

    public Organization(Character founder) {
        this.founder = founder;
        this.master = founder;
        name = (founder + "'s Sect");
        members  = new ArrayList<>();
        location = founder.location;
        founder.sect = this;
    }

    public void join(Character member) {
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
