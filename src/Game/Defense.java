package Game;

import java.util.ArrayList;
import java.util.List;

/** Defending a sect or a city. When a fight starts in a sect's seat or in a city with a lord, against someone the place has reason to fear (a raider of a sect at war with it or in
 *  crisis, an Evil cultivator who has killed {@link #MIN_KILLS} or more, an awakened Monster that has killed), the place raises the alarm: up to {@link #MAX_FIGHT} of the people who stand
 *  there fight the attacker at once, and the members of the sect (for a city: the lord, the shop owners, the lord's sect and close friends) within a few tiles are sent a "Defend"
 *  quest to go and drive the attacker off. A person who plays hears of it as a notice (the master or the lord wherever they are, anyone else only when near and once a season) and
 *  finds the quest in the Quest Log (an alert in MANUAL; in AUTO the sect / city duty does it). A place is still razed if the attacker beats the people who stand against it
 *  (blocking that stopped the world growing: ruined cities keep producing city-born cultivators). See `Character.battleCheck` (the hook), the "Defend" quest in `Character` and
 *  `Area.siegeUntil`. */
final class Defense {
    private Defense() { }

    static final int SIEGE_WEEKS = Integer.getInteger("tdf.defSiege", 13);   // how long a place stays "under attack" after the alarm (the Defend quests lapse then; no second alarm before)
    static final int CALL_RADIUS = 8;                                         // tiles from which members are called
    static final int MAX_CALLED = Integer.getInteger("tdf.defCalled", 8);     // people called by one alarm
    static final int MAX_FIGHT = Integer.getInteger("tdf.defFight", 3);       // people on the spot who fight the attacker at once (the strongest first)
    static final int MIN_KILLS = Integer.getInteger("tdf.defKills", 3);       // an attacker with fewer kills is a brawler, not a threat to the place (a raid of a sect at war always counts)
    static final int MIN_POWER_PCT = Integer.getInteger("tdf.defPower", 60);  // a defender fights an attacker only if it is at least this percent of its fighting level

    /** Counts for the long-run measurements (`MemProbe`): alarms raised, errands sent, fights fought, attackers killed, defenders killed. */
    static volatile int alarms, called, fights, attackersKilled, defendersKilled;
    static String counts() { return "alarms " + alarms + ", errands " + called + ", fights " + fights + ", attackers killed " + attackersKilled + ", defenders killed " + defendersKilled; }

    private static final java.util.concurrent.ConcurrentHashMap<Integer, Integer> lastNotice = new java.util.concurrent.ConcurrentHashMap<>(); // person who plays -> week of the last "under attack" notice
    static final int NOTICE_EVERY = 13;

    private static boolean person(Character x) {
        return x != null && !x.dead && x.born && x.trait != Character.Traits.Beast && x.trait != Character.Traits.Monster && x.age >= 14;
    }

    /** Whether a person who plays is at the keys (they are told, not dragged into the fight). */
    private static boolean atTheKeys(Character x) {
        return x.player == 1 && x.pc != null && x.pc.mode == PlayerControl.Mode.MANUAL;
    }

    /** Is the place one that defends itself (a sect's seat, or a city with a lord)? */
    static boolean defended(Area a) { return a != null && ((a.sect != null && !a.sect.palace) || (a.city && a.cityOwner != null && !a.cityOwner.dead)); } // (the Palace of the Oldest Order keeps its own counsel)

    /** The hook in a fight: `attacker` is about to fight `victim`, who stands in the same tile. Cheap when the tile is not a seat or a city. */
    static void onAttack(Character attacker, Character victim) {
        Area a = victim.location;
        if (a == null || attacker.location != a || !defended(a) || a.siegeUntil >= GameDriver.time) return;
        if (attacker.trait == Character.Traits.Beast || attacker.trait == Character.Traits.Monster) {
            if (victim.trait == Character.Traits.Beast || victim.trait == Character.Traits.Monster) return;
            if (attacker.trait == Character.Traits.Beast || attacker.kills < MIN_KILLS) return; // a wild beast wandering in is hunted as always; an awakened Monster that has killed is a threat
        } else {
            boolean raid = attacker.sect != null && a.sect != null && attacker.sect != a.sect
                    && (attacker.sect.warTarget == a.sect || a.sect.warTarget == attacker.sect || a.sect.inCrisis());
            boolean ravager = attacker.affiliationNum < 0 && victim.affiliationNum >= 0 && victim.trait != Character.Traits.Beast && victim.trait != Character.Traits.Monster
                    && attacker.kills >= MIN_KILLS;
            if (!raid && !ravager) return;
        }
        if (a.sect != null && a.sect == attacker.sect) return;      // a quarrel inside a sect
        if (a.city && a.cityOwner == attacker) return;              // the lord of the city
        final Area place = a;
        a.siegeUntil = GameDriver.time + SIEGE_WEEKS; a.siegeBy = attacker.id;
        Map.later(() -> alarm(place, attacker));
    }

    /** Raises the alarm: told to the humans who care, a Defend quest to the people who can come, and the people on the spot fight at once. */
    static void alarm(Area a, Character attacker) {
        if (attacker == null || attacker.dead || !defended(a)) return;
        alarms++;
        String place = a.sect != null ? "The seat of " + a.sect.name : a.name;
        String who = attacker.name + " " + attacker.lastName + " (Lv." + attacker.lvl + ")";
        List<Character> circle = circle(a, attacker);
        // the humans of the place are told
        for (Character h : new ArrayList<>(Notifications.humans())) {
            if (h == null || h.dead || h == attacker) continue;
            boolean leader = (a.sect != null && a.sect.master == h) || a.cityOwner == h;
            boolean mine = leader || (circle.contains(h) && (h.location == a || PlayerControl.distance(h.location, a) <= CALL_RADIUS));
            if (!mine) continue;
            Integer last = lastNotice.get(h.id);
            if (last != null && GameDriver.time - last < NOTICE_EVERY && !leader) continue; // one notice a season for a place far from the person, however many attackers come
            lastNotice.put(h.id, GameDriver.time);
            if (mine) Notifications.post("war", place + " is under attack by " + who + " at " + PlayerControl.coord(a) + "!" + (h.location == a ? "" : " Defend it (Quest Log: Defend)."), attacker.id, h.id);
        }
        List<Character> present = new ArrayList<>();
        for (Character d : circle) {
            if (d.location == a) { if (!atTheKeys(d)) present.add(d); }
            else if (!hasDefend(d, a)) { d.addTarget(new Quest("Defend", true, 90, 0, 95, a)); called++; }
        }
        // Good-hearted bystanders on the spot lend a hand (a few of them)
        if (a.characters != null) {
            int lent = 0;
            for (Character x : new ArrayList<>(a.characters)) {
                if (lent >= 3 || present.size() >= MAX_FIGHT) break;
                if (!person(x) || x == attacker || atTheKeys(x) || present.contains(x) || x.affiliationNum <= 0 || x.lvl < attacker.lvl / 2) continue;
                if (x.sect != null && attacker.sect != null && x.sect == attacker.sect) continue;
                present.add(x); lent++;
            }
        }
        fightOff(present, attacker);
        if (attacker.dead && attacker.lvl >= 15) Chronicle.add("war", place + " drove off " + who + ".", attacker.id);
    }

    private static boolean hasDefend(Character d, Area a) {
        if (d.currentQuest != null && "Defend".equals(d.currentQuest.name) && d.currentQuest.targetArea == a) return true;
        for (Quest q : d.targetQuests) if ("Defend".equals(q.name) && q.targetArea == a) return true;
        return false;
    }

    /** Who the place can call: the members of a sect, or the circle of a city's lord, near enough to come; those on the spot first, then the strongest. */
    static List<Character> circle(Area a, Character attacker) {
        ArrayList<Character> pool = new ArrayList<>();
        if (a.sect != null && a.sect.members != null) {
            for (Character m : new ArrayList<>(a.sect.members)) if (person(m) && m != attacker && m.sect == a.sect) pool.add(m);
        }
        if (a.city && a.cityOwner != null) {
            Character lord = a.cityOwner;
            if (person(lord) && lord != attacker && !pool.contains(lord)) pool.add(lord);
            if (lord.sect != null && lord.sect != a.sect && lord.sect.members != null) for (Character m : new ArrayList<>(lord.sect.members)) if (person(m) && m != attacker && !pool.contains(m)) pool.add(m);
            for (Shop sh : new ArrayList<>(a.shops)) if (sh.owner != null && person(sh.owner) && sh.owner != attacker && !pool.contains(sh.owner)) pool.add(sh.owner);
            if (lord.relationshipList != null) {
                for (Relationship r : new ArrayList<>(lord.relationshipList.relationships)) {
                    if (r.value < 3) continue;
                    Character f = Character.charById(r.friendID);
                    if (person(f) && f != attacker && !pool.contains(f)) pool.add(f);
                }
            }
        }
        ArrayList<Character> out = new ArrayList<>();
        for (Character x : pool) {
            if (x.location == null) continue;
            boolean leader = x.player == 1 && ((a.sect != null && a.sect.master == x) || a.cityOwner == x);
            if (x.location == a || leader || PlayerControl.distance(x.location, a) <= CALL_RADIUS) out.add(x);  // (the master or the lord who plays hears of it wherever they are)
        }
        out.sort((p, q) -> { boolean pa = p.location == a, qa = q.location == a; if (pa != qa) return pa ? -1 : 1; if ((p.player == 1) != (q.player == 1)) return p.player == 1 ? -1 : 1; return Integer.compare(q.lvl, p.lvl); });
        return out.size() > MAX_CALLED ? new ArrayList<>(out.subList(0, MAX_CALLED)) : out;
    }

    /** The defenders who stand in the tile fight the attacker one after the other, the strongest first (it tires: the same rule as a party's fight). */
    static void fightOff(List<Character> present, Character attacker) {
        if (present.isEmpty()) return;
        for (Character d : present) d.tempLvlCheck();
        attacker.tempLvlCheck();
        ArrayList<Character> order = new ArrayList<>(present);
        order.sort((p, q) -> Integer.compare(q.tempLvl, p.tempLvl));
        int fought = 0;
        for (int i = 0; i < order.size() && !attacker.dead && fought < MAX_FIGHT; i++) {
            Character d = order.get(i);
            if (d.dead || d.location != attacker.location) continue;
            if (d.tempLvl * 100L < (long) attacker.tempLvl * MIN_POWER_PCT) continue; // too weak to stand against it: they do not throw themselves away
            fought++;
            Relationship r = d.relationshipList.contains(attacker.id);
            if (attacker.affiliationNum >= 0 && (r == null || r.value > -3)) d.changeRelationship(attacker, -3); // a raider who is not Evil is now an enemy: the Good fight only those they hate (an Evil attacker is fought as it is)
            attacker.exhaustionPenalty = i * 2;
            fights++;
            try { d.battleCheck(attacker); } finally { attacker.exhaustionPenalty = 0; }
            if (attacker.dead) attackersKilled++;
            if (d.dead) defendersKilled++;
        }
    }

    /** A defender who has come to the place (the Defend quest): the attacker, if it is still there, is fought; otherwise the errand is done. */
    static void arrive(Character d, Area a) {
        Character att = a.siegeBy >= 0 ? Character.charById(a.siegeBy) : null;
        if (att == null || att.dead || att.location != a) return;
        ArrayList<Character> one = new ArrayList<>();
        one.add(d);
        fightOff(one, att);
        if (att.dead && att.lvl >= 15) Chronicle.add("war", (a.sect != null ? "The seat of " + a.sect.name : a.name) + " drove off " + att.name + " " + att.lastName + ".", att.id);
    }

    /** Whether the Defend errand is still worth the trip. */
    static boolean stillOn(Area a) { return a != null && defended(a) && a.siegeUntil >= GameDriver.time; }
}
