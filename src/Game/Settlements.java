package Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Cities and sects scale with the number of cultivators instead of depending on rare Leader rolls: once a year the world compares how many
 *  settlements it has with how many its population supports (about one city per 1,000 cultivators and one sect per 300) and lets suitable
 *  cultivators found the missing ones. Leaders and merchants are the most likely founders, but anyone of the right realm can do it. */
public final class Settlements {
    private Settlements() { }

    static final int CITY_PER = 1000, SECT_PER = 300;      // cultivators supported per city / per sect
    static final int MAX_CITIES_PER_YEAR = 6, MAX_SECTS_PER_YEAR = 15;

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
                cityCand.add(new Candidate(c, base * (1 + Math.min(40, c.location.characters.size()) / 8.0)));
            if (c.majorLvl >= 2 && c.sect == null && c.location.sect == null && !"Ocean".equals(c.location.biomem))
                sectCand.add(new Candidate(c, base * (1 + Math.min(40, c.location.characters.size()) / 8.0) * (c.location.city ? 2 : 1)));
        }
        int cities = 0, sects = 0;
        for (Area a : new ArrayList<>(Map.createdAreas)) { if (a.city) cities++; if (a.sect != null) sects++; }
        int cityGap = Math.min(MAX_CITIES_PER_YEAR, cultivators / CITY_PER - cities);
        int sectGap = Math.min(MAX_SECTS_PER_YEAR, cultivators / SECT_PER - sects);
        int foundedCities = 0, foundedSects = 0;
        for (int k = 0; k < cityGap && !cityCand.isEmpty(); k++) {
            Candidate pick = draw(cityCand, rand);
            cityCand.remove(pick);
            if (pick.c.dead || pick.c.location.city) continue;
            if (pick.c.foundCityNow() == null) { foundedCities++; System.out.println(pick.c + " has founded the city of " + pick.c.location.name + "!"); }
        }
        for (int k = 0; k < sectGap && !sectCand.isEmpty(); k++) {
            Candidate pick = draw(sectCand, rand);
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
