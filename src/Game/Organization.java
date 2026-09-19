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
    int crisisTimer = 0;            // succession crisis ticks remaining (inCrisis when > 0)

    // Sect treasury and advancement
    ArrayList<Item> treasury = new ArrayList<>();
    // Quest board: each entry is "type|itemType|minLvl|qty|bonusPoints"
    ArrayList<String> questBoard = new ArrayList<>();

    // Non-cultivating population under sect protection; grows 1%/year, taxed to master
    long sectPopulation = 1000;
    int territoryBonus = 0; // computed yearly: +1 when members occupy 3+ distinct tiles near sect

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
            boolean hasElder = false;
            for (Character character : members) {
                SectRank r = memberRanks.getOrDefault(character.id, SectRank.OUTER_DISCIPLE);
                if (r == SectRank.ELDER || r == SectRank.FOUNDER) hasElder = true;
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
            if (!hasElder) crisisTimer = 52; // no proper heir — succession crisis for 1 year
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

    public boolean inCrisis() { return crisisTimer > 0; }

    public boolean isAlly(Organization other) { return other != null && allies.contains(other); }

    public int sectLvl() {
        long weight = 0;
        for (Item item : treasury) {
            if (item.itemType == Item.ItemType.Herb || item.itemType == Item.ItemType.Metal) {
                weight += (long) item.quantity * item.lvl;
            }
        }
        return (int) Math.min(100, weight / 10000);
    }

    public int donationPoints(Item item, int qty) {
        switch (item.itemType) {
            case Herb: case Metal: return Math.max(1, qty * item.lvl / 1000);
            case Pill:             return Math.max(1, qty * item.lvl * 2);
            default:               return Math.max(1, item.lvl);
        }
    }

    public int buyingCost(Item item, int qty) {
        return donationPoints(item, qty);
    }

    public void growPopulation() {
        sectPopulation = (long)(sectPopulation * 1.01);
        if (master != null) master.ss += sectPopulation / 100;
    }

    public void postMissionsYearly() {
        questBoard.clear();
        growPopulation();
        int sl = sectLvl();
        long herbWeight = 0, metalWeight = 0;
        int[] herbByLvl = new int[101], metalByLvl = new int[101];
        for (Item item : treasury) {
            if (item.itemType == Item.ItemType.Herb) {
                herbWeight += (long) item.quantity * item.lvl;
                if (item.lvl <= 100) herbByLvl[item.lvl] += item.quantity;
            } else if (item.itemType == Item.ItemType.Metal) {
                metalWeight += (long) item.quantity * item.lvl;
                if (item.lvl <= 100) metalByLvl[item.lvl] += item.quantity;
            }
        }
        long targetWeight = (long)(sl + 1) * 10000;
        if (herbWeight < targetWeight) {
            int neededLvl = Math.max(1, sl);
            int neededQty = (int) Math.min(5000, (targetWeight - herbWeight) / Math.max(1, neededLvl));
            int bonus = Math.max(10, neededQty * neededLvl / 500);
            questBoard.add("Forage|Herb|" + neededLvl + "|" + neededQty + "|" + bonus);
        }
        if (metalWeight < targetWeight / 2) {
            int neededLvl = Math.max(1, sl);
            int neededQty = (int) Math.min(2000, (targetWeight / 2 - metalWeight) / Math.max(1, neededLvl));
            int bonus = Math.max(10, neededQty * neededLvl / 200);
            questBoard.add("Mine|Metal|" + neededLvl + "|" + neededQty + "|" + bonus);
        }
        // Hunt Beast mission for established sects
        if (sl >= 5 && questBoard.size() < 5) {
            int huntLvl = Math.max(1, sl / 2);
            int huntQty = Math.max(3, sl / 3);
            int bonus = Math.max(15, huntQty * huntLvl * 2);
            questBoard.add("Hunt|Beast|" + huntLvl + "|" + huntQty + "|" + bonus);
        }
        // Check for low-level resource gaps (useful for crafters)
        for (int lvlBracket = 10; lvlBracket <= 100; lvlBracket += 10) {
            int lo = lvlBracket - 9;
            int herbSum = 0, metalSum = 0;
            for (int lv = lo; lv <= lvlBracket; lv++) { herbSum += herbByLvl[lv]; metalSum += metalByLvl[lv]; }
            if (herbSum < 500 && questBoard.size() < 5) {
                questBoard.add("Forage|Herb|" + lo + "|500|" + (lvlBracket / 2));
            }
            if (metalSum < 200 && questBoard.size() < 5) {
                questBoard.add("Mine|Metal|" + lo + "|200|" + (lvlBracket));
            }
        }
    }

    public void proposeAlliance(Organization other) {
        if (other == null || other == this || isAlly(other)) return;
        allies.add(other);
        other.allies.add(this);
        // End any existing war between them
        if (warTarget == other) endWar(other);
    }

    public void dissolveAlliance(Organization other) {
        if (other == null) return;
        allies.remove(other);
        other.allies.remove(this);
    }

    public void declareWar(Organization enemy) {
        if (enemy == null || enemy == this) return;
        this.warTarget = enemy;
        if (enemy.warTarget == null) enemy.warTarget = this;
        squabbles = 0;
        dissolveAlliance(enemy); // can't be both allied and at war
        // Allies of the defender automatically oppose the attacker
        for (Organization ally : new ArrayList<>(enemy.allies)) {
            if (ally != this && ally.warTarget == null) ally.warTarget = this;
        }
    }

    public void endWar(Organization enemy) {
        if (this.warTarget == enemy) this.warTarget = null;
        if (enemy != null && enemy.warTarget == this) enemy.warTarget = null;
    }

    public String toString() {
        return name;
    }
}
