package Game;

import java.util.ArrayList;
import java.util.HashMap;

public class Organization {

    enum SectRank { OUTER_DISCIPLE, INNER_DISCIPLE, ELDER, MASTER, FOUNDER }

    String name = "";
    Character founder;
    Character master;
    ArrayList<Character> members;
    Area location;
    Library sectLibrary;

    // Recruitment criteria — null/0 means no restriction
    int minPotential = 0;
    int minLevel = 0;
    String genderPref = null;       // "Male", "Female", or null (any)
    String affiliationMatch = null; // "Good", "Neutral", "Evil", or null (any)
    Character.Traits traitRequired = null;

    // Rank tracking: member id → rank
    HashMap<Integer, SectRank> memberRanks = new HashMap<>();

    // Inter-sect politics
    int squabbles = 0;              // accumulated cross-sect fight count
    Organization warTarget = null;
    ArrayList<Organization> allies = new ArrayList<>();

    public Organization(Character founder) {
        this.founder = founder;
        this.master = founder;
        name = (founder + "'s Sect");
        members = new ArrayList<>();
        location = founder.location;
        founder.sect = this;
        memberRanks.put(founder.id, SectRank.FOUNDER);
        sectLibrary = new Library(this);
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
        memberRanks.put(member.id, SectRank.OUTER_DISCIPLE);
    }

    public void resign(Character member) {
        members.remove(member);
        memberRanks.remove(member.id);
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
                } else if (temp.lvl < character.lvl) {
                    temp = character;
                } else if (temp.lvl == character.lvl && temp.tempLvl < character.tempLvl) {
                    temp = character;
                }
            }
            master = temp;
            if (master != null) memberRanks.put(master.id, SectRank.MASTER);
        } else {
            master = null;
            if (warTarget != null) endWar(warTarget);
            location.sect = null;
        }
    }

    public void promoteRank(Character c) {
        SectRank current = getRank(c);
        switch (current) {
            case OUTER_DISCIPLE: memberRanks.put(c.id, SectRank.INNER_DISCIPLE); break;
            case INNER_DISCIPLE: memberRanks.put(c.id, SectRank.ELDER); break;
            default: break;
        }
    }

    public SectRank getRank(Character c) {
        if (isMaster(c)) return SectRank.MASTER;
        if (c == founder) return SectRank.FOUNDER;
        return memberRanks.getOrDefault(c.id, SectRank.OUTER_DISCIPLE);
    }

    public boolean isMaster(Character character) {
        return character == master;
    }

    public boolean isAtWar() { return warTarget != null; }

    public void declareWar(Organization enemy) {
        this.warTarget = enemy;
        if (enemy.warTarget == null) enemy.warTarget = this;
        squabbles = 0; // reset after war declared
    }

    public void endWar(Organization enemy) {
        if (this.warTarget == enemy) this.warTarget = null;
        if (enemy != null && enemy.warTarget == this) enemy.warTarget = null;
    }

    public String toString() {
        return name;
    }
}
