package Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/** Cities and sects scale with the number of cultivators instead of depending on rare Leader rolls: once a year the world compares how many
 *  settlements it has with how many its population supports (one city per 2,000 cultivators and one sect per 2,500, but never fewer than 25 of each
 *  once the world is big enough: the floor is scaled down in small test worlds) and lets suitable
 *  cultivators found the missing ones. Leaders and merchants are the most likely founders, but anyone of the right realm can do it. */
public final class Settlements {
    private Settlements() { }

    static final int CITY_PER = 2000, SECT_PER = 2500;     // cultivators supported per city / per sect
    static final int FLOOR = 25, FLOOR_PER = 150;          // at least 25 of each (one per 150 cultivators in a small world)
    static final int RAZE_CITY_ODDS = 250, RAZE_SECT_ODDS = 100; // one in this many eligible kills by Evil or a beast razes an unguarded city / sect (it was one in 5)
    static final int PROTECT = 25;                          // Evil and beasts only go after cities / sects when the world has more than this many of them
    static final int MAX_CITIES_PER_YEAR = 10, MAX_SECTS_PER_YEAR = 10;

    static volatile int cityTarget = Integer.MAX_VALUE / 2, sectTarget = Integer.MAX_VALUE / 2;  // set every year; wide open before the first count
    static final AtomicInteger cityCount = new AtomicInteger(), sectCount = new AtomicInteger();  // kept up to date as places are founded and razed, recounted every year

    /** Natural founding (a Leader's own city or sect) stops while the world already has more than it supports. */
    static boolean cityRoom() { return cityCount.get() < cityTarget + 5; }
    static boolean sectRoom() { return sectCount.get() < sectTarget + 5; }
    /** A third of the sect slots are kept for founders who are not Leaders: Leaders found on their own only while the world has fewer than the other two thirds. */
    static int reservedSlots() { int cap = (int) Math.min(Integer.MAX_VALUE / 2, (long) sectTarget + 5); return Math.max(1, cap / 3); }
    static boolean sectRoomForLeader() { return sectCount.get() < (long) sectTarget + 5 - reservedSlots(); }

    /** A tile's city or sect may be destroyed only while the world has more than PROTECT of that kind, so the world is never left without them. */
    static boolean mayDestroy(Area a) {
        return (!a.city || cityCount.get() > PROTECT) && (a.sect == null || a.sect.palace || sectCount.get() > PROTECT);
    }

    static class Candidate { Character c; double w; Candidate(Character c, double w) { this.c = c; this.w = w; } }

    /** Called once a year by the game loop. Returns {citiesFounded, sectsFounded} (for logs and tests). */
    static int[] yearly() {
        Random rand = GameDriver.rand;
        int cultivators = 0;
        List<Candidate> cityCand = new ArrayList<>(), sectCand = new ArrayList<>();
        int n = Map.characters.size();
        for (int i = 0; i < n; i++) {
            Character c = Map.characters.get(i);
            if (c == null || c.dead || !c.born || c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) continue;
            cultivators++;
            if (c.player >= 1 || c.location == null || c.age < 18) continue; // humans found their own with the `found` command
            double base = c.trait == Character.Traits.Leader ? 8 : c.trait == Character.Traits.Merchant ? 3 : c.trait == Character.Traits.Friendly ? 2 : 1;
            if (c.majorLvl >= 2 && !c.location.city && c.location.cityOwner == null && !"Ocean".equals(c.location.biomem))
                cityCand.add(new Candidate(c, base * (1 + Math.min(40, c.location.characters.size()) / 8.0) * (c.location.spiritualVein ? 5 : 1))); // spirit veins draw cities
            if (c.majorLvl >= 2 && c.sect == null && c.location.sect == null && !"Ocean".equals(c.location.biomem))
                sectCand.add(new Candidate(c, base * (1 + Math.min(40, c.location.characters.size()) / 8.0) * (c.location.city ? 2 : 1) * (c.location.spiritualVein ? 5 : 1))); // ...and sects
        }
        int cities = 0, sects = 0;
        for (Area a : new ArrayList<>(Map.createdAreas)) { if (a.city) cities++; if (a.sect != null && !a.sect.palace) sects++; }
        int floor = Math.min(FLOOR, cultivators / FLOOR_PER);
        cityTarget = Math.max(floor, cultivators / CITY_PER); sectTarget = Math.max(floor, cultivators / SECT_PER);
        cityCount.set(cities); sectCount.set(sects);
        int cityGap = Math.min(MAX_CITIES_PER_YEAR, cityTarget - cities);
        int sectGap = Math.min(MAX_SECTS_PER_YEAR, sectTarget - sects);
        int foundedCities = 0, foundedSects = 0;
        for (int k = 0; k < cityGap && !cityCand.isEmpty(); k++) {
            Candidate pick = draw(cityCand, rand);
            cityCand.remove(pick);
            if (pick.c.dead || pick.c.location.city) continue;
            if (pick.c.foundCityNow() == null) { foundedCities++; Say.world(pick.c + " has founded the city of " + pick.c.location.name + "!"); }
        }
        int nonLeader = 0;
        for (Area a : new ArrayList<>(Map.createdAreas)) if (a.sect != null && !a.sect.palace && a.sect.founder != null && a.sect.founder.trait != Character.Traits.Leader) nonLeader++;
        int reserved = Math.max(0, Math.min(sectGap, sectTarget / 3 - nonLeader)); // the first draws go to founders who are not Leaders until a third of the sects are theirs
        for (int k = 0; k < sectGap && !sectCand.isEmpty(); k++) {
            List<Candidate> from = sectCand;
            if (k < reserved) { from = new ArrayList<>(); for (Candidate cd : sectCand) if (cd.c.trait != Character.Traits.Leader) from.add(cd); if (from.isEmpty()) from = sectCand; }
            Candidate pick = draw(from, rand);
            sectCand.remove(pick);
            if (pick.c.dead || pick.c.sect != null || pick.c.location.sect != null) continue;
            if (pick.c.foundSectNow() == null) foundedSects++;
        }
        return new int[]{foundedCities, foundedSects};
    }

    private static Candidate draw(List<Candidate> list, Random rand) {
        double total = 0;
        for (Candidate c : list) total += c.w;
        double x = rand.nextDouble() * total;
        for (Candidate c : list) { x -= c.w; if (x <= 0) return c; }
        return list.get(list.size() - 1);
    }
}
