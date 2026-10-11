package Game;

import java.util.ArrayList;
import java.util.HashMap;

public class Organization implements java.io.Serializable {
    static final long serialVersionUID = 1L;
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
    String familyName = null;       // only members of this family (last name) may join

    // Rank tracking: member id → rank
    HashMap<Integer, SectRank> memberRanks = new HashMap<>();

    // Inter-sect politics
    int squabbles = 0;              // accumulated cross-sect fight count
    Organization warTarget = null;
    static int warsDeclared = 0, warsEnded = 0;   // counters for the playtest tools (not saved): the chronicle is a ring and forgets old wars
    int peaceUntil = 0;              // game week before which this sect's own masters (NPC) will not start a new war: a truce lasts a few years
    int warSince = 0;                // game week the current war was declared (0 = unknown: an old save; diplomacy dates it from the next yearly check)
    static final int RAID_COOLDOWN_WEEKS = 13;  // a sect launches at most one raid per season
    int nextRaidWeek = 0;                       // game week before which no new raid may be issued
    ArrayList<Organization> allies = new ArrayList<>();
    int crisisTimer = 0;            // succession crisis ticks remaining (inCrisis when > 0)
    SectPolitics.Plot plot = null;  // a conspiracy of elders against the master (see SectPolitics); null in old saves and when there is none

    // Sect treasury and advancement
    ArrayList<Item> treasury = new ArrayList<>();
    long sectCash = 0;
    // Quest board: each entry is "type|itemType|minLvl|qty|bonusPoints"
    ArrayList<String> questBoard = new ArrayList<>();

    // Non-cultivating population under sect protection; grows 1%/year, taxed to master
    long sectPopulation = 1000;
    int territoryBonus = 0; // computed yearly: +1 when members occupy 3+ distinct tiles near sect

    boolean palace = false;         // the Palace of the Oldest Order (stays true when a human becomes its master and `virtual` ends)
    boolean virtual = false;        // the Palace of the Oldest Order (see Lore): its master "???" is no Character (master == null for ever), so every master duty is done by Lore

    /** The Palace: a sect with a virtual master. No founder, no master, never at war, never dissolved. */
    Organization(String name, Area location) {
        this.virtual = true; this.palace = true;
        this.name = name;
        this.location = location;
        members = new ArrayList<>();
        sectLibrary = new Library(this);
    }

    /** One lock for every change of a sect's people, ranks and alliances (turns of different tiles may run together; sects touch each other, so a lock per sect could deadlock). */
    private static final Object LOCK = new Object();
    boolean isVirtual() { return virtual; }

    public Organization(Character founder) {
        this.founder = founder;
        this.master = founder;
        name = Naming.sect(founder);
        members = new ArrayList<>();
        location = founder.location;
        founder.sect = this;
        memberRanks.put(founder.id, SectRank.FOUNDER);
        sectLibrary = new Library(this);
        if (founder.player == 0) rollRules(founder); // a human founder sets their own with `sect rules`
    }

    /** An NPC-founded sect asks for something of its recruits: at most two of a craft (profession), a potential floor, a level floor, an alignment,
     *  a gender or a family name, all modest relative to the founder so the sect can still grow. */
    void rollRules(Character f) {
        java.util.Random r = GameDriver.rand;
        java.util.List<Integer> kinds = new java.util.ArrayList<>();
        switch (f.trait) {
            case Leader: case Ghost: case Cheater: case Lucky: case IronBody: case SpiritVein: case HeavenlyRoot: case Beast: case Monster: break;
            default: if (r.nextInt(100) < 45) kinds.add(0); // a craft guild
        }
        if (r.nextInt(100) < 35) kinds.add(1);
        if (r.nextInt(100) < 30) kinds.add(2);
        if (r.nextInt(100) < 25) kinds.add(3);
        if (r.nextInt(100) < 12) kinds.add(4);
        if (r.nextInt(100) < 10) kinds.add(5);
        java.util.Collections.shuffle(kinds, r);
        for (int i = 0; i < Math.min(2, kinds.size()); i++) {
            switch (kinds.get(i)) {
                case 0: traitRequired = f.trait; break;
                case 1: minPotential = Math.max(10, (int) (f.potential * (0.4 + r.nextDouble() * 0.3))); break;
                case 2: minLevel = Math.max(5, f.lvl / 4 + r.nextInt(Math.max(1, f.lvl / 6))); break;
                case 3: affiliationMatch = f.affiliation; break;
                case 4: genderPref = f.gender; break;
                default: familyName = f.lastName;
            }
        }
    }

    /** How many disciples the sect can hold: a young sect is small, and it grows with its wealth (herbs and metals in the treasury) and the realm of its master. */
    public int memberCap() {
        if (virtual) return Math.max(60, Map.population / 80); // the Palace is the capital's school: a share of the whole world, with no master to set its size
        return 20 + sectLvl() * 8 + (master != null ? master.majorLvl * 15 : 0);
    }

    /** Why this character cannot join (null if they can). */
    public String whyNot(Character c) {
        if (c.dead) return "dead";
        if (c.sect != null) return "already in " + c.sect.name;
        if (virtual || palace) return Lore.whyNotEnter(c); // the Palace admits only those the Convocation (or its notice) let in
        if (members != null && members.size() >= memberCap()) return "the sect is full (" + members.size() + "/" + memberCap() + ")";
        if (c.potential < minPotential) return "potential below " + minPotential;
        if (c.lvl < minLevel) return "level below " + minLevel;
        if (genderPref != null && !genderPref.equals(c.gender)) return genderPref.toLowerCase() + "s only";
        if (affiliationMatch != null && !affiliationMatch.equals(c.affiliation)) return affiliationMatch + " alignment only";
        if (traitRequired != null && c.trait != traitRequired) return "only " + c.getTrait(traitRequired) + "s";
        if (familyName != null && !familyName.equalsIgnoreCase(c.lastName)) return "only the " + familyName + " family";
        return null;
    }

    public boolean canJoin(Character c) { return whyNot(c) == null; }

    /** One line: what the sect asks of its recruits. */
    public String rulesText() {
        StringBuilder sb = new StringBuilder();
        if (traitRequired != null) sb.append("craft: ").append(Character.traitName(traitRequired)).append("s only; ");
        if (minPotential > 0) sb.append("potential ").append(minPotential).append("+; ");
        if (minLevel > 0) sb.append("level ").append(minLevel).append("+; ");
        if (affiliationMatch != null) sb.append(affiliationMatch).append(" alignment; ");
        if (genderPref != null) sb.append(genderPref.toLowerCase()).append("s only; ");
        if (familyName != null) sb.append("the ").append(familyName).append(" family only; ");
        return sb.length() == 0 ? "open to anyone" : sb.substring(0, sb.length() - 2);
    }

    /** `sect rules ...` for the master: key/value pairs, "any" or "open" clears. Returns a message. */
    public String setRules(String[] t, int from) {
        for (int i = from; i + 1 < t.length || (i < t.length && t[i].equalsIgnoreCase("open")); i += 2) {
            String k = t[i].toLowerCase();
            if (k.equals("open")) { minPotential = 0; minLevel = 0; genderPref = null; affiliationMatch = null; traitRequired = null; familyName = null; i--; continue; }
            String v = t[i + 1].replace('_', ' ');
            boolean any = v.equalsIgnoreCase("any") || v.equals("0");
            switch (k) {
                case "potential": try { minPotential = any ? 0 : Math.max(0, Math.min(100, Integer.parseInt(v))); } catch (NumberFormatException e) { return "potential needs a number"; } break;
                case "level": try { minLevel = any ? 0 : Math.max(0, Math.min(100, Integer.parseInt(v))); } catch (NumberFormatException e) { return "level needs a number"; } break;
                case "gender": genderPref = any ? null : v.toLowerCase().startsWith("f") ? "Female" : v.toLowerCase().startsWith("m") ? "Male" : genderPref; break;
                case "affiliation": case "alignment": affiliationMatch = any ? null : v.toLowerCase().startsWith("g") ? "Good" : v.toLowerCase().startsWith("e") ? "Evil" : v.toLowerCase().startsWith("n") ? "Neutral" : affiliationMatch; break;
                case "trait": case "craft": case "profession": {
                    if (any) { traitRequired = null; break; }
                    Character.Traits found = null;
                    for (Character.Traits tr : Character.Traits.values()) if (tr != Character.Traits.Beast && tr != Character.Traits.Monster && (tr.name().equalsIgnoreCase(v) || Character.traitName(tr).equalsIgnoreCase(v))) found = tr;
                    if (found == null) return "unknown trait '" + v + "'";
                    traitRequired = found; break;
                }
                case "family": case "clan": case "name": familyName = any ? null : v; break;
                default: return "unknown rule '" + k + "' (potential, level, gender, affiliation, trait, family, or 'open')";
            }
        }
        return "Recruitment rules: " + rulesText() + ".";
    }

    public void join(Character member) { synchronized (LOCK) {
        if (!canJoin(member)) return;
        members.add(member);
        member.sect = this;
        memberRanks.put(member.id, SectRank.OUTER_DISCIPLE);
        member.joinedASect(); // a "Join Sect" errand is done, whichever door they came through (the Palace's pass, a recruiter, the command)
    } }

    public void resign(Character member) { synchronized (LOCK) {
        members.remove(member);
        memberRanks.remove(member.id);
        member.sect = null;
        member.sectMission = null;      // a defector must not deliver the old sect's mission to a new one
        member.sectMissionBonus = 0;
        if (palace) Lore.onResign(member); // a human who leaves the Palace is not pulled straight back in (`lore join` later)
        if (isMaster(member)) {
            master = null;
            promote();
        }
    } }

    public void promote() { synchronized (LOCK) {
        if (virtual) { master = null; return; } // ??? is always the master; nobody is crowned in his place
        if (members != null && !members.isEmpty()) {
            Character temp = null;
            boolean hasElder = false;
            for (Character character : members) {
                if (character == null || character.dead) continue; // never crown a corpse
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
            if (!hasElder) crisisTimer = 1; // no proper heir — succession crisis; the timer counts down once a YEAR (52 made it last 52 years)
        } else {
            master = null;
            if (warTarget != null) endWar(warTarget);
            if (location.sect == this) location.sect = null; // don't wipe a newer sect founded on the same tile
        }
    } }

    public void promoteRank(Character c) { synchronized (LOCK) {
        SectRank current = getRank(c);
        switch (current) {
            case OUTER_DISCIPLE: memberRanks.put(c.id, SectRank.INNER_DISCIPLE); break;
            case INNER_DISCIPLE: memberRanks.put(c.id, SectRank.ELDER); break;
            default: break;
        }
    } }

    public SectRank getRank(Character c) { synchronized (LOCK) {
        if (isMaster(c)) return SectRank.MASTER;
        if (c == founder) return SectRank.FOUNDER;
        return memberRanks.getOrDefault(c.id, SectRank.OUTER_DISCIPLE);
    } }

    public boolean isMaster(Character character) {
        return character == master;
    }

    public boolean isAtWar() { return warTarget != null; }

    public boolean inCrisis() { return crisisTimer > 0; }

    public boolean isAlly(Organization other) { synchronized (LOCK) { return other != null && allies.contains(other); } }

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
        // Gathering only yields herb/metal levels 1-10, so mission levels are capped there (higher ones could never be filled)
        if (herbWeight < targetWeight) {
            int neededLvl = Math.min(10, Math.max(1, sl));
            int neededQty = (int) Math.min(5000, (targetWeight - herbWeight) / Math.max(1, neededLvl));
            int bonus = Math.max(10, neededQty * neededLvl / 500);
            questBoard.add("Forage|Herb|" + neededLvl + "|" + neededQty + "|" + bonus);
        }
        if (metalWeight < targetWeight / 2) {
            int neededLvl = Math.min(10, Math.max(1, sl));
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
        postGoodsMissions();
        // Check for low-level resource gaps (useful for crafters)
        for (int lvlBracket = 10; lvlBracket <= 10; lvlBracket += 10) { // only the 1-10 bracket exists for gathered materials
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

    /** Food, wine and poison for the pantry: "Deliver|<kind>|<min level>|<qty>|<bonus>" (see Life.deliverMission). Called from postMissionsYearly. */
    void postGoodsMissions() {
        int n = 0;
        for (Character m : members) if (m != null && !m.dead) n++;
        if (n < 3 || questBoard.size() >= 7) return;
        int lvl = Math.max(1, Math.min(10, sectLvl() / 10 + 1));
        int food = Life.countIn(treasury, Item.ItemType.Food, 1), wine = Life.countIn(treasury, Item.ItemType.Wine, 1), poison = Life.countIn(treasury, Item.ItemType.Poison, 1);
        if (food < n * 3) { int qty = Math.min(300, n * 5); questBoard.add("Deliver|Food|" + lvl + "|" + qty + "|" + Math.max(10, qty * lvl / 12)); }
        if (n >= 6 && wine < n && questBoard.size() < 7) { int qty = Math.min(100, Math.max(5, n)); questBoard.add("Deliver|Wine|" + lvl + "|" + qty + "|" + Math.max(10, qty * lvl / 8)); }
        if (master != null && "Evil".equals(master.affiliation) && poison < 5 && questBoard.size() < 7) questBoard.add("Deliver|Poison|" + lvl + "|10|" + Math.max(15, 10 * lvl / 3));
    }

    /** Why the master of THIS sect would not ally with `from`, or null when they would (a human master is asked by the caller instead). An alliance used to be made at once, without the
     *  other sect agreeing. They refuse when the masters are Good and Evil, when they dislike each other, or when `from` is allied with a sect this one is at war with. */
    public String allianceRefusal(Organization from) {
        if (virtual || (from != null && from.virtual)) return "The Palace of the Oldest Order keeps out of the sects' alliances.";
        if (from == null || master == null || master.dead || master.player > 0 || from.master == null) return null;
        if (warTarget == from || from.warTarget == this) return name + " is at war with " + from.name + ".";
        String a = master.affiliation, b = from.master.affiliation;
        if (a != null && b != null && !a.equals(b) && !"Neutral".equals(a) && !"Neutral".equals(b)) return master.name + " will not ally with " + (b.startsWith("E") ? "an " : "a ") + b + " sect.";
        Relationship r = master.relationshipList.contains(from.master.id);
        if (r != null && r.value < 0) return master.name + " does not trust " + from.master.name + ".";
        for (Organization ally : from.allies) if (ally != null && (warTarget == ally || ally.warTarget == this)) return name + " is at war with " + ally.name + ", an ally of " + from.name + ".";
        return null;
    }

    public void proposeAlliance(Organization other) { synchronized (LOCK) {
        if (other == null || other == this || isAlly(other)) return;
        allies.add(other);
        other.allies.add(this);
        // End any existing war between them (either side's pointer)
        endWar(other);
    } }

    public void dissolveAlliance(Organization other) { synchronized (LOCK) {
        if (other == null) return;
        allies.remove(other);
        other.allies.remove(this);
    } }

    public void declareWar(Organization enemy) { synchronized (LOCK) {
        if (enemy == null || enemy == this || virtual || enemy.virtual) return; // the Palace keeps out of the sects' wars
        if (GameDriver.time < peaceUntil && (master == null || master.player == 0)) return; // an NPC sect rests after a war
        if (warTarget != null && warTarget != enemy && warTarget.warTarget == this) warTarget.warTarget = null; // release the previous enemy so wars stay symmetric
        this.warTarget = enemy;
        warsDeclared++;
        this.warSince = GameDriver.time;
        if (enemy.warTarget == null) { enemy.warTarget = this; enemy.warSince = GameDriver.time; }
        squabbles = 0;
        dissolveAlliance(enemy); // can't be both allied and at war
        // Allies of the defender automatically oppose the attacker
        for (Organization ally : new ArrayList<>(enemy.allies)) {
            if (ally != this && ally.warTarget == null) ally.warTarget = this;
        }
        Chronicle.add("war", this.name + " declared war on " + enemy.name + " (the " + Naming.war(this.name, enemy.name) + ").", this.master != null ? new int[]{this.master.id} : new int[0]);
        Notifications.onWar(this, enemy);
    } }

    public void endWar(Organization enemy) { synchronized (LOCK) {
        if (enemy != null && this.warTarget == enemy) Chronicle.add("war", this.name + " made peace with " + enemy.name + " (end of the " + Naming.war(this.name, enemy.name) + ").");
        if (this.warTarget == enemy) warsEnded++;
        if (this.warTarget == enemy) { this.warTarget = null; this.warSince = 0; this.peaceUntil = GameDriver.time + 52 * 7; } // seven years of rest: with four, 3 in 4 sects were at war at any time in a 75-sect world
        if (enemy != null && enemy.warTarget == this) { enemy.warTarget = null; enemy.warSince = 0; enemy.peaceUntil = GameDriver.time + 52 * 7; }
    } }

    public String toString() {
        return name;
    }
}
