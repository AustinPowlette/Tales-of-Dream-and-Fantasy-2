package Game;

import java.util.ArrayList;
import java.util.List;

/** How a city or a sect is destroyed: the whole chain, in one place.
 *  1. WHO: an Evil cultivator or a wild beast (not an ambusher) that kills someone in a city or a sect's seat. Only while the world holds more than 25 cities (25 sects): `Settlements.mayDestroy`.
 *  2. THE ARRAYS: every array of the place must be gone. An array is only broken by an attacker who beats it (`Character.arrayBreakAttempt`: the odds fall 15 points for every level it is above the attacker, and the backlash of
 *     a failure can kill); a failed attempt leaves the array standing.
 *  3. THE WISH: one kill in {@link Settlements#RAZE_CITY_ODDS} (city) or {@link Settlements#RAZE_SECT_ODDS} (sect) turns into an attempt to raze the place (`Character.winCheck`).
 *  4. THE STORM (this class): the attacker must fight everyone who stands for the place, one after the other, strongest first, tiring by 2 points of fighting level with each bout: the members of a sect
 *     who are in the seat, the lord of a city, the lord's sect, the shop owners and the lord's close friends who are in the city (at most {@link #MAX_REAL}; a person far weaker than the attacker keeps out of it, as in `Defense`).
 *     Their fights are real ones (`Character.battleCheck`: reports, deaths, injuries). After them come the place's standing guards, who are always there however far its people have wandered: a sect keeps two and one more
 *     for every six of its members (2 to 10), a city two and one more for every 20,000 of its people (2 to 10). Guards are as strong as the best of the place (a sect's five strongest members, the lord of a city, a fifth of the
 *     level of the tile with none) and their bouts are rolled with the same odds as a fight. The place falls only if the attacker beats every one of them and every fight is won: one defeat ends the attack
 *     (the attacker is hurt, or dead if a real defender killed it) and the place holds.
 *  So a place that keeps its people at home and its arrays up is hard to destroy, and an attacker that does it has fought a small war. A person who plays is told what happened (the notice of `Defense.alarm`, or of the
 *  destruction), and the Chronicle records a razing and a repelled attacker of the level 15 and above. */
final class Sieges {
    private Sieges() { }

    static final int MAX_REAL = Integer.getInteger("tdf.stormReal", 12);        // people of the place who fight the attacker in a row
    static final int MIN_POWER_PCT = Defense.MIN_POWER_PCT;                      // a person weaker than this percent of the attacker keeps out
    static final int TIRE = 2;                                                  // points of fighting level the attacker loses with each bout
    static final int SECT_PER_GUARD = 6, CITY_PER_GUARD = 20000, MAX_GUARDS = 10, MIN_GUARDS = 2;

    /** Counts for the probes and the long runs. */
    static volatile int storms, razed, repelled, realFights, guardFights;

    static String counts() { return "storms " + storms + ", razed " + razed + ", turned back " + repelled + ", real fights " + realFights + ", guard bouts " + guardFights; }

    private static boolean person(Character x) {
        return x != null && !x.dead && x.born && x.trait != Character.Traits.Beast && x.trait != Character.Traits.Monster && x.age >= 14;
    }

    /** Who stands for the place and is in it now: the people the attacker will have to fight, strongest first. */
    static List<Character> defenders(Area a, Character attacker) {
        List<Character> out = new ArrayList<>();
        if (a == null || a.characters == null) return out;
        Character lord = a.city ? a.cityOwner : null;
        List<Character> circle = new ArrayList<>();
        if (lord != null && !lord.dead && lord.relationshipList != null) {
            for (Relationship r : new ArrayList<>(lord.relationshipList.relationships)) if (r.value >= 3) { Character f = Character.charById(r.friendID); if (f != null) circle.add(f); }
        }
        for (Character x : new ArrayList<>(a.characters)) {
            if (!person(x) || x == attacker) continue;
            boolean stands = false;
            if (a.sect != null && !a.sect.palace && x.sect == a.sect && !(attacker.sect != null && attacker.sect == a.sect)) stands = true;
            if (!stands && lord != null) {
                if (x == lord) stands = true;
                else if (x.sect != null && lord.sect != null && x.sect == lord.sect && x.sect != attacker.sect) stands = true;
                else if (x.ownedShops != null) for (Shop s : x.ownedShops) if (s.location == a) { stands = true; break; }
                if (!stands && circle.contains(x)) stands = true;
            }
            if (stands) out.add(x);
        }
        for (Character x : out) x.tempLvlCheck();
        out.sort((p, q) -> Integer.compare(q.tempLvl, p.tempLvl));
        return out.size() > MAX_REAL ? new ArrayList<>(out.subList(0, MAX_REAL)) : out;
    }

    /** The standing guards of a place (always there, whoever has wandered off). */
    static int guards(Area a) {
        if (a == null) return 0;
        int g = MIN_GUARDS;
        if (a.sect != null && a.sect.members != null) g = MIN_GUARDS + a.sect.members.size() / SECT_PER_GUARD;
        else if (a.city) g = MIN_GUARDS + Math.max(0, a.population / CITY_PER_GUARD);
        return Math.min(MAX_GUARDS, g);
    }

    /** How strong a guard is (the same scale as `Character.tempLvl`): the best of the place. */
    static int guardPower(Area a) {
        if (a == null) return 1;
        if (a.sect != null && a.sect.members != null && !a.sect.members.isEmpty()) {
            List<Character> m = new ArrayList<>(a.sect.members);
            m.removeIf(x -> x == null || x.dead);
            m.sort((p, q) -> Integer.compare(q.lvl, p.lvl));
            int n = Math.min(5, m.size()), sum = 0;
            for (int i = 0; i < n; i++) sum += m.get(i).lvl;
            if (n > 0) return Math.max(1, sum / n / 5 + 1);
        }
        if (a.city && a.cityOwner != null && !a.cityOwner.dead) return Math.max(1, a.cityOwner.lvl / 5 + 1);
        return Math.max(1, a.lvl / 5);
    }

    /** The attacker has the wish to raze `a` (it killed somebody there, the arrays are gone, the roll came up): it must now fight through the people and the guards. Returns true if the place was destroyed. */
    static boolean storm(Character att, Area a) {
        if (att == null || a == null || a.storming) return false;
        a.storming = true;
        try {
            storms++;
            att.tempLvlCheck();
            int bout = 0;
            for (Character d : defenders(a, att)) {
                if (att.dead || att.location != a) break;
                if (d.dead || d.location != a) continue;
                d.tempLvlCheck(); att.tempLvlCheck();
                if (d.tempLvl * 100L < (long) att.tempLvl * MIN_POWER_PCT) continue;       // too weak to throw themselves at it
                att.exhaustionPenalty = bout * TIRE; bout++;
                realFights++;
                try { d.battleCheck(att); } finally { att.exhaustionPenalty = 0; }
                if (att.dead || att.location != a || (att.lostAt == GameDriver.time && att.lostToId == d.id)) return turnedBack(att, a, d);
            }
            int n = guards(a), power = guardPower(a);
            for (int k = 0; k < n; k++) {
                att.tempLvlCheck();
                int mine = att.tempLvl - bout * TIRE;
                bout++;
                guardFights++;
                int wc = Math.max(5, Math.min(95, 50 - 15 * (power - mine)));
                if (GameDriver.rand.nextInt(100) >= wc) return turnedBack(att, a, null);
            }
            razed++;
            att.destroyCity(a);
            return true;
        } finally { a.storming = false; }
    }

    /** The place held: the attacker is hurt (and the place is "under attack" for a season, so it is not tried again at once). */
    private static boolean turnedBack(Character att, Area a, Character by) {
        repelled++;
        if (!att.dead) att.cultivation = Math.max(1, (long) (att.cultivation * 0.7));
        a.siegeUntil = Math.max(a.siegeUntil, GameDriver.time + Defense.SIEGE_WEEKS);
        if (att.lvl >= 15) Chronicle.add("war", (a.sect != null ? "The seat of " + a.sect.name : a.name) + " turned back " + att.name + " " + att.lastName + " (Lv." + att.lvl + ")" + (att.dead ? ", who died there." : "."), att.id);
        return false;
    }
}
