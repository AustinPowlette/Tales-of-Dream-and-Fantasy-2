package Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** The world's calendar and politics beyond fighting: a festival in every large city when a season begins, and once a year the sects talk to each
 *  other (truces, joint gatherings, border incidents). Both run on the game thread between turns. */
final class Seasonal {
    private Seasonal() { }

    static final String[] FESTIVALS = {"Blossom Festival", "Martial Fair", "Harvest Fair", "Lantern Night"};

    // ------------------------------------------------------------------ festivals
    /** Called when a new season begins (0 spring, 1 summer, 2 autumn, 3 winter). Cities of 500+ people celebrate. */
    static void festivals(int season) {
        Random r = GameDriver.rand;
        inviteTraders(season);
        Life.sectFeast(season);
        SectPolitics.seasonal(season); // inner disciples bully outer ones
        int held = 0;
        for (Area city : new ArrayList<>(Map.createdAreas)) {
            if (city == null || !city.city || city.population < 500) continue;
            List<Character> crowd = new ArrayList<>();
            for (Character c : new ArrayList<>(city.characters)) if (c != null && !c.dead && c.age >= 8 && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster) crowd.add(c);
            held++;
            final String fest = Naming.festival(season, city.name);
            city.cityTreasury += city.population / 200; // visitors spend
            switch (season) {
                case 0: // spring: everyone is in a good mood, and people meet
                    for (Character c : crowd) {
                        Life.Fx f = Life.fx(c); f.inspiredWeeks = Math.max(f.inspiredWeeks, 4); f.inspiredPow = Math.max(f.inspiredPow, 0.08);
                        if (crowd.size() > 1 && r.nextInt(4) == 0) { Character o = crowd.get(r.nextInt(crowd.size())); if (o != c) c.changeRelationship(o, 1); }
                    }
                    break;
                case 1: // summer: the lord holds a tournament
                    if (city.cityOwner != null && !city.cityOwner.dead && city.tournamentTimer <= 0 && city.cityOwner.player == 0
                            && city.cityOwner.spotQuests.stream().noneMatch(q -> "Hold Tournament".equals(q.name)))
                        city.cityOwner.spotQuests.add(new Quest("Hold Tournament", true, 70, 0, 70));
                    break;
                case 2: // autumn: shops restock and the cooks and brewers sell twice over
                    for (Shop s : new ArrayList<>(city.shops)) { try { s.restockFromCity(city); } catch (RuntimeException ignored) { } }
                    for (Character c : crowd) if (c.trait == Character.Traits.Chef || c.trait == Character.Traits.Drunken) { Life.sellGoods(c); Life.sellGoods(c); }
                    break;
                default: // winter: warm food and music
                    for (Character c : crowd) {
                        Life.Fx f = Life.fx(c); f.foodWeeks = Math.max(f.foodWeeks, 4); f.foodPow = Math.max(f.foodPow, 0.05);
                        if (c.trait == Character.Traits.Musician && Life.bestInstrument(c) != null) Life.play(c);
                    }
            }
            for (Character h : Notifications.humans()) {
                if (h != null && !h.dead && h.player == 1 && h.location == city) Notifications.post("fortune", city.name + " holds its " + fest + " (" + crowd.size() + " here).", -1, h.id);
            }
            if (crowd.size() >= 10 && held <= 3) Chronicle.add("world", city.name + " held its " + fest + " (" + crowd.size() + " cultivators present).");
        }
    }

    /** The cooks, brewers and musicians of the neighbourhood hear about the festival: they get a "Festival Trade" quest to the nearest festival city. */
    private static void inviteTraders(int season) {
        List<Area> cities = new ArrayList<>();
        for (Area a : new ArrayList<>(Map.createdAreas)) if (a != null && a.city && a.population >= 500) cities.add(a);
        if (cities.isEmpty()) return;
        int n = Map.characters.size();
        for (int i = 0; i < n; i++) {
            Character c = Map.characters.get(i);
            if (c == null || c.dead || !c.born || c.age < 8 || c.location == null) continue;
            if (c.trait != Character.Traits.Chef && c.trait != Character.Traits.Drunken && c.trait != Character.Traits.Musician) continue;
            Area best = null; int bd = 9;
            for (Area a : cities) { int d = PlayerControl.distance(c.location, a); if (d <= bd) { bd = d; best = a; } }
            if (best == null || c.targetQuests.stream().anyMatch(q -> "Festival Trade".equals(q.name))) continue;
            c.targetQuests.add(new Quest("Festival Trade", true, 45, 0, 45, best));
        }
    }

    // ------------------------------------------------------------------ diplomacy between sects
    private static List<Organization> sects() {
        List<Organization> out = new ArrayList<>();
        for (Area a : new ArrayList<>(Map.createdAreas)) if (a != null && a.sect != null && a.sect.members != null && !a.sect.members.isEmpty()) out.add(a.sect);
        return out;
    }

    private static void tell(Organization a, Organization b, String text) {
        Chronicle.add("war", text);
        for (Character h : Notifications.humans()) if (h != null && !h.dead && h.player == 1 && h.sect != null && (h.sect == a || h.sect == b)) Notifications.post("war", text, -1, h.id);
    }

    /** Once a year: exhausted enemies make peace, allies gather, and neighbours quarrel. */
    static void diplomacy() {
        Random r = GameDriver.rand;
        List<Organization> all = sects();
        // Wars wear themselves out: none ends in its first two years, then every year the chance of a truce grows (15%, 30%, ...);
        // after five years the declaring master must choose to renew the war (40%) or it ends by itself.
        for (Organization s : all) {
            Organization t = s.warTarget;
            if (t == null) { s.warSince = 0; continue; }
            if (s.warSince <= 0) s.warSince = GameDriver.time; // an old save: date it from now
            boolean mutual = t.warTarget == s;
            if (mutual && all.contains(t) && all.indexOf(s) > all.indexOf(t)) continue; // each war is judged once
            int years = (GameDriver.time - s.warSince) / 52;
            if (years < 2) continue;
            int chance = Math.min(90, 15 * (years - 1));
            boolean over = r.nextInt(100) < chance;
            if (!over && years >= 5 && years % 5 == 0) { // the war's term is up: renewed, or let go
                if (r.nextInt(100) < 40) { s.warSince = GameDriver.time; if (mutual) t.warSince = GameDriver.time; tell(s, t, s.name + " renewed its war against " + t.name + " (the " + Naming.war(s.name, t.name) + ", year " + years + ")."); continue; }
                over = true;
            }
            if (!over) continue;
            s.endWar(t); if (mutual) t.endWar(s); else t.warTarget = t.warTarget == s ? null : t.warTarget;
            s.squabbles = 0; t.squabbles = 0;
            tell(s, t, s.name + " and " + t.name + " signed a truce and laid down their arms after " + years + " years of the " + Naming.war(s.name, t.name) + ".");
        }
        // Internal politics (SectPolitics): treachery against the master or a fellow disciple, murder for loot, theft from the treasury, poison, slander and
        // coups by Evil (and, less so, Neutral) elders. Good-aligned members never take part. The old treachery event lives there now.
        SectPolitics.yearly(all);
        for (Organization s : all) { // a war sometimes puts a price on the enemy master's head (Poisoners and bounty hunters take it up)
            Organization t = s.warTarget;
            if (t != null && t.master != null && !t.master.dead && s.location != null && s.location.city && r.nextInt(100) < 30) {
                final int id = t.master.id;
                if (s.location.bounties.stream().noneMatch(b -> b.targetCharacter == id)) {
                    s.location.bounties.add(new Quest("Bounty Hunt", true, 55, 4, 80, t.master));
                    tell(s, t, s.name + " has put a price on the head of " + t.master.name + " " + t.master.lastName + " of " + t.name + ".");
                }
            }
        }
        for (Organization s : all) {
            for (Organization ally : new ArrayList<>(s.allies)) {
                if (all.indexOf(s) < all.indexOf(ally) && r.nextInt(100) < 20 && s.location != null) { // allies hold a joint gathering at the first one's home
                    int guests = 0;
                    for (Character c : new ArrayList<>(s.members)) if (c != null && !c.dead) { Life.Fx f = Life.fx(c); f.inspiredWeeks = Math.max(f.inspiredWeeks, 8); f.inspiredPow = Math.max(f.inspiredPow, 0.08); guests++; }
                    for (Character c : new ArrayList<>(ally.members)) if (c != null && !c.dead) { Life.Fx f = Life.fx(c); f.inspiredWeeks = Math.max(f.inspiredWeeks, 8); f.inspiredPow = Math.max(f.inspiredPow, 0.08); guests++; }
                    tell(s, ally, s.name + " and its ally " + ally.name + " held a joint gathering (" + guests + " disciples shared their insights).");
                }
            }
        }
        for (int i = 0; i < all.size() && all.size() > 1; i++) { // a border incident between two nearby sects at peace
            Organization a = all.get(i), b = all.get(r.nextInt(all.size()));
            if (a == b || a.isAtWar() || b.isAtWar() || a.isAlly(b) || a.location == null || b.location == null) continue;
            if (PlayerControl.distance(a.location, b.location) > 6 || r.nextInt(100) >= 4) continue;
            a.squabbles += 2; b.squabbles += 2;
            tell(a, b, "A quarrel over the border between " + a.name + " and " + b.name + " soured their relations.");
        }
    }
}
