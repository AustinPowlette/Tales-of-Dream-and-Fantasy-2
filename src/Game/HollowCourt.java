package Game;

import java.util.ArrayList;
import java.util.List;

/** The Hollow Court felt in the world (see "Lore Design.md"). Three things: the omens grow stranger with the years (`LoreText.OMENS_LATE`), the Erased (`Lore.erasedYearly`: once a
 *  century a seeker is put out and forgotten by everybody but the humans who knew them), and the Dimming: from year 150, once in about 110-130 years, the sky over one city goes out for an
 *  hour and the people there wake with gaps in what they knew, each losing a few friendships. The humans in the Garden remember everything: it is the one thing the Court cannot take
 *  from them. Nothing here is a threat that can be fought: it is only a thing that happens. */
final class HollowCourt {
    private HollowCourt() { }

    static void yearly(int year) {
        Lore.State s = Lore.state;
        if (s == null || s.silenced || year < 150) return;
        if (s.dimNext == 0) s.dimNext = year + GameDriver.rand.nextInt(60);
        if (year < s.dimNext) return;
        dim(s, year);
        s.dimNext = year + 110 + GameDriver.rand.nextInt(21);
    }

    /** The sky over a city goes out. Returns the city (null when the world has none). */
    static Area dim(Lore.State s, int year) {
        List<Area> cities = new ArrayList<>();
        for (Area a : Map.createdAreas) if (a != null && a.city && a != Lore.palaceArea() && !a.characters.isEmpty()) cities.add(a);
        if (cities.isEmpty()) return null;
        Area city = cities.get(0);
        for (Area a : cities) if (a.characters.size() > city.characters.size()) city = a; // the largest city: that is where it is felt most
        int forgot = 0;
        for (Character r : new ArrayList<>(city.characters)) {
            if (r == null || r.dead || r.player != 0 || r.trait == Character.Traits.Beast || r.trait == Character.Traits.Monster || r.relationshipList == null) continue;
            int drop = 1 + GameDriver.rand.nextInt(3);
            for (int i = 0; i < drop && !r.relationshipList.relationships.isEmpty(); i++) {
                Relationship rel = r.relationshipList.relationships.get(GameDriver.rand.nextInt(r.relationshipList.relationships.size()));
                if (rel != null) { r.relationshipList.remove(rel.friendID); forgot++; }
            }
        }
        s.dimmings++;
        String text = "The sky over " + city.name + " went out for an hour. When it came back, the people there could not say who half their friends were.";
        Chronicle.add("world", "Omen: " + text);
        System.out.println("[Omen] " + text);
        for (Character h : Notifications.humans()) {
            if (h == null || h.dead || h.player != 1) continue;
            boolean there = h.location == city;
            Notifications.post("lore", there ? "The sky over " + city.name + " went out for an hour. Everyone around you forgot a friend. You forgot nothing." : "Word comes from " + city.name + ": the sky went out for an hour, and the people woke with gaps in what they knew.", -1, h.id);
            Lore.PState p = Lore.pstate(h);
            if (there && p != null && !p.ended) { p.scenes.remove("dimming"); Lore.queueScene(p, "dimming", true); }
        }
        return city;
    }
}
