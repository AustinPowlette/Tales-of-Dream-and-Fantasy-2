package Game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Small arcs for the NPC seekers of the Palace (see "Lore Design.md"): up to six at a time, each a few beats a few years apart, told as chronicle lines and notices (to the humans who are
 *  members of the Palace or who know the people in it) and each with one thing that really happens.
 *  The Rival races a human to the Door (a note at a place, a meeting). The Doubter reads the Cistern, leaves the Palace and preaches (Wicks follow, a price is put on them).
 *  The Pair are two friends who study together. The Defector gathers others who mean to strike at the Gardener (they cross together, see `defectorAllies`).
 *  The Teacher takes students and hands the lamp to the best of them. (The Erased is in `Lore.erasedYearly`.) */
final class SeekerArcs {
    private SeekerArcs() { }

    static final int MAX_ACTIVE = 6;

    static class Arc implements Serializable {
        static final long serialVersionUID = 1L;
        String kind;
        int a = -1, b = -1;            // the main person, and the human (Rival) or the second friend (Pair)
        int[] others = new int[0];     // the followers, students or companions
        int stage, nextYear, startYear;
    }

    private static final int ADVANCE = 0, FINISH = 1, HOLD = 2;

    static Character alive(int id) { Character c = id < 0 ? null : Character.charById(id); return c == null || c.dead ? null : c; }
    static String nm(Character c) { return c.name + " " + c.lastName; }

    // ------------------------------------------------------------------ the yearly step
    static void yearly(Lore.State s, int year) {
        if (s == null || s.silenced || year < 12) return;
        if (s.arcs == null) s.arcs = new ArrayList<>();
        for (Arc ar : new ArrayList<>(s.arcs)) {
            if (year < ar.nextYear) continue;
            int r = beat(s, ar, year);
            if (r == FINISH) s.arcs.remove(ar);
            else if (r == HOLD) ar.nextYear = year + 5;
            else { ar.stage++; ar.nextYear = year + 3 + GameDriver.rand.nextInt(5); }
        }
        if (s.arcs.size() < MAX_ACTIVE && GameDriver.rand.nextInt(100) < 35) begin(s, year);
    }

    static void begin(Lore.State s, int year) {
        List<String> kinds = new ArrayList<>(Arrays.asList("rival", "doubter", "pair", "defector", "teacher"));
        Collections.shuffle(kinds, GameDriver.rand);
        for (String k : kinds) { Arc ar = start(s, k, year); if (ar != null) { s.arcs.add(ar); return; } }
    }

    // ------------------------------------------------------------------ telling
    /** A line of the chronicle, and a notice for the humans who are members of the Palace or know any of the people in it. */
    static void tell(String text, Character... who) {
        int[] ids = new int[who.length];
        for (int i = 0; i < who.length; i++) ids[i] = who[i].id;
        Chronicle.add("world", text, ids);
        for (Character h : Notifications.humans()) {
            if (h == null || h.dead || h.player != 1) continue;
            boolean knows = false;
            for (Character w : who) if (h.relationshipList.contains(w.id) != null) knows = true;
            if (knows || Lore.isMember(h)) Notifications.post("lore", text, who.length > 0 ? who[0].id : -1, h.id);
        }
    }

    private static void tellTo(Character h, String text, Character about) {
        Chronicle.add("world", text, about == null ? new int[0] : new int[]{about.id});
        if (h != null && !h.dead) Notifications.post("lore", text, about == null ? -1 : about.id, h.id);
    }

    // ------------------------------------------------------------------ who can be in an arc
    static boolean busy(Lore.State s, int id) {
        if (s.arcs == null) return false;
        for (Arc ar : s.arcs) {
            if (ar.a == id || (!ar.kind.equals("rival") && ar.b == id)) return true;
            for (int o : ar.others) if (o == id) return true;
        }
        return false;
    }

    static List<Character> freeSeekers(Lore.State s) {
        List<Character> out = new ArrayList<>();
        if (s.seekers == null) return out;
        for (Integer id : new ArrayList<>(s.seekers.keySet())) {
            Character c = alive(id);
            if (c == null || c.player != 0 || c.wanderingImmortal || c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster || busy(s, id)) continue;
            if (c.sect == null || !(c.sect.palace || c.sect.virtual)) continue;
            out.add(c);
        }
        return out;
    }

    private static Character pick(List<Character> l) { return l.isEmpty() ? null : l.get(GameDriver.rand.nextInt(l.size())); }

    // ------------------------------------------------------------------ starting an arc
    static Arc start(Lore.State s, String kind, int year) {
        List<Character> seekers = freeSeekers(s);
        Arc ar = new Arc();
        ar.kind = kind; ar.startYear = year; ar.stage = 1; ar.nextYear = year + 3 + GameDriver.rand.nextInt(5);
        switch (kind) {
            case "rival": {
                List<Character> hs = new ArrayList<>();
                for (Character h : Notifications.humans()) {
                    Lore.PState p = Lore.pstate(h);
                    if (h != null && !h.dead && h.player == 1 && p != null && !p.ended && (Lore.isMember(h) || !p.found.isEmpty())) hs.add(h);
                }
                Character h = pick(hs), r = pick(seekers);
                if (h == null || r == null) return null;
                ar.a = r.id; ar.b = h.id;
                tellTo(h, nm(r) + ", a seeker of the Oldest Order, tells the Wicks that " + nm(h) + " will not reach the Door first.", r);
                return ar;
            }
            case "doubter": {
                List<Character> cs = new ArrayList<>();
                for (Character c : seekers) if (c.majorLvl >= 2) cs.add(c);
                Character d = pick(cs);
                if (d == null) return null;
                ar.a = d.id;
                if (d.sect != null) d.sect.resign(d);
                s.seekers.remove(d.id);
                d.repTitle = "the Doubter"; d.affiliationNum = 0; d.affiliation = "Neutral";
                tell(nm(d) + " read the Cistern of Returning and put down the lamp. " + nm(d) + " has left the Palace.", d);
                return ar;
            }
            case "pair": {
                Character x = pick(seekers);
                if (x == null) return null;
                List<Character> rest = new ArrayList<>(seekers);
                rest.remove(x);
                Character y = null;
                for (Character c : rest) if (x.relationshipList.contains(c.id) != null && c.id != x.id) { y = c; break; }
                if (y == null) y = pick(rest);
                if (y == null) return null;
                x.changeRelationship(y, 3); y.changeRelationship(x, 3);
                ar.a = x.id; ar.b = y.id;
                tell(nm(x) + " and " + nm(y) + " came into the Palace as friends and share a room in the Outer Court. They mean to reach the Door together.", x, y);
                return ar;
            }
            case "defector": {
                List<Character> cs = new ArrayList<>();
                for (Character c : seekers) if (c.affiliationNum != null && c.affiliationNum < 0 && c.majorLvl >= 2) cs.add(c);
                Character d = pick(cs);
                if (d == null) return null;
                ar.a = d.id;
                tell(nm(d) + ", a seeker with a hard name, has asked ??? what lies behind the Seam. ??? did not answer.", d);
                return ar;
            }
            case "teacher": {
                Organization pal = Lore.palace();
                if (pal == null) return null;
                Character t = null; int lamp = -1;
                for (int i = 0; i < s.lamps.length; i++) { Character h = alive(s.lamps[i]); if (h != null && h.player == 0 && !busy(s, h.id)) { t = h; lamp = i; break; } }
                if (t == null) return null;
                List<Character> studs = new ArrayList<>();
                for (Character c : seekers) if (c != t && c.majorLvl >= 1 && c.majorLvl <= 5 && pal.getRank(c) != Organization.SectRank.ELDER) studs.add(c);
                Collections.shuffle(studs, GameDriver.rand);
                if (studs.size() < 2) return null;
                int n = Math.min(3, studs.size());
                ar.a = t.id; ar.others = new int[n];
                for (int i = 0; i < n; i++) { Character st = studs.get(i); ar.others[i] = st.id; st.changeRelationship(t, 2); t.changeRelationship(st, 2); }
                tell("Elder " + nm(t) + ", the Lamp of " + LoreText.LAMPS[lamp] + ", has taken " + n + " students.", t);
                return ar;
            }
            default: return null;
        }
    }

    // ------------------------------------------------------------------ the beats
    static int beat(Lore.State s, Arc ar, int year) {
        switch (ar.kind) {
            case "rival": return rival(s, ar);
            case "doubter": return doubter(s, ar);
            case "pair": return pair(s, ar);
            case "defector": return defector(s, ar);
            case "teacher": return teacher(s, ar);
            default: return FINISH;
        }
    }

    private static int rival(Lore.State s, Arc ar) {
        Character r = alive(ar.a), h = alive(ar.b);
        if (h == null || Lore.pstate(h) == null || Lore.pstate(h).ended) return FINISH;
        if (r == null) { tellTo(h, "Word comes that " + "your rival on the road to the Door has fallen out of the race for good.", null); return FINISH; }
        int mask = s.seekers != null && s.seekers.containsKey(r.id) ? s.seekers.get(r.id) : 0;
        switch (ar.stage) {
            case 1: {
                int top = -1;
                for (int i = 0; i < LoreSites.ALL.length; i++) if ((mask & (1 << i)) != 0) top = i;
                String where = top < 0 ? LoreSites.ALL[0].title : LoreSites.ALL[top].title;
                tellTo(h, "At " + where + ", someone has scratched a name into the stone: '" + nm(r) + " was here first.'", r);
                return ADVANCE;
            }
            case 2: {
                if (h.location != null && r.location != h.location) {
                    if (r.location != null) r.location.removeChar(r);
                    r.location = h.location; h.location.addChar(r);
                }
                r.changeRelationship(h, -1); h.changeRelationship(r, -1);
                tellTo(h, nm(r) + " was waiting for you at " + (h.location == null ? "the road" : h.location.name) + ". 'I'll be at the Seam before you,' " + r.name + " said, and walked on.", r);
                return ADVANCE;
            }
            default: {
                int count = Integer.bitCount(mask);
                tellTo(h, count >= LoreSites.ALL.length ? nm(r) + " has read all nine places and waits for the last lock. The race is close." : nm(r) + " has fallen behind, and sends word: next time.", r);
                return FINISH;
            }
        }
    }

    private static int doubter(Lore.State s, Arc ar) {
        Character d = alive(ar.a);
        if (d == null) { Chronicle.add("world", "The Doubter is dead. People still say the line: ask who is pouring."); return FINISH; }
        switch (ar.stage) {
            case 1: {
                List<Area> cities = new ArrayList<>();
                for (Area a : Map.createdAreas) if (a != null && a.city && a != Lore.palaceArea()) cities.add(a);
                Area to = cities.isEmpty() ? null : cities.get(GameDriver.rand.nextInt(cities.size()));
                if (to != null && d.location != to) { if (d.location != null) d.location.removeChar(d); d.location = to; to.addChar(d); }
                int followers = 0;
                Organization pal = Lore.palace();
                if (pal != null) for (Character m : new ArrayList<>(pal.members)) {
                    if (followers >= 3) break;
                    if (m == null || m.dead || m.player != 0 || pal.getRank(m) != Organization.SectRank.OUTER_DISCIPLE) continue;
                    if (GameDriver.rand.nextInt(4) != 0) continue;
                    pal.resign(m); if (s.seekers != null) s.seekers.remove(m.id); followers++;
                }
                tell(nm(d) + " preached in " + (to == null ? "the market" : to.name) + ": 'Nothing leaves. Ask who is pouring.' " + (followers == 0 ? "Few listened." : followers + " Wick" + (followers > 1 ? "s" : "") + " left the Palace to follow."), d);
                return ADVANCE;
            }
            case 2: {
                Area city = d.location != null && d.location.city ? d.location : Life.nearestCity(d.location);
                if (city != null && city.bounties.stream().noneMatch(b -> b.targetCharacter == d.id)) city.bounties.add(new Quest("Bounty Hunt", true, 55, 4, 80, d));
                tell("The Elders of the Palace called " + nm(d) + " a liar. A price was posted on the boards. ??? said nothing.", d);
                return ADVANCE;
            }
            default:
                tell(nm(d) + ", the Doubter, was last seen walking away from the Palace with a lantern.", d);
                return FINISH;
        }
    }

    private static int pair(Lore.State s, Arc ar) {
        Character x = alive(ar.a), y = alive(ar.b);
        if (x == null && y == null) return FINISH;
        if (x == null || y == null) {
            Character left = x != null ? x : y;
            Chronicle.add("world", nm(left) + " goes on alone. The letters to a friend who is gone pile up on the Empty Seat, and ??? reads them.", left.id);
            return FINISH;
        }
        switch (ar.stage) {
            case 1:
                x.potential = Math.min(100, x.potential + 1); y.potential = Math.min(100, y.potential + 1);
                tell(nm(x) + " and " + nm(y) + " read the Weaver's Wall together. Each says the other made it bearable.", x, y);
                return ADVANCE;
            case 2:
                for (int i = 0; i < 5; i++) { x.cultivate(); y.cultivate(); }
                tell(nm(x) + " and " + nm(y) + " study through the winter. They argue about whether to cross together.", x, y);
                return ADVANCE;
            default:
                tell(nm(x) + " and " + nm(y) + " have made a promise at the Empty Seat: whichever goes through first will leave the lamp on.", x, y);
                return FINISH;
        }
    }

    private static int defector(Lore.State s, Arc ar) {
        Character d = alive(ar.a);
        if (d == null || s.seekers == null || !s.seekers.containsKey(d.id)) {
            int n = 0; for (int o : ar.others) if (alive(o) != null) n++;
            Chronicle.add("world", "The seekers who meant to strike at the Gardener " + (n > 0 ? "have gone their own ways." : "are gone."));
            return FINISH;
        }
        switch (ar.stage) {
            case 1: {
                List<Character> cs = new ArrayList<>();
                for (Character c : freeSeekers(s)) if (c.id != d.id && c.affiliationNum != null && c.affiliationNum <= 0) cs.add(c);
                Collections.shuffle(cs, GameDriver.rand);
                int n = Math.min(3, cs.size());
                ar.others = new int[n];
                for (int i = 0; i < n; i++) ar.others[i] = cs.get(i).id;
                tell(nm(d) + " has found " + (n == 0 ? "no one" : n + " other seeker" + (n > 1 ? "s" : "")) + " who think the Gardener should be made to answer.", d);
                return ADVANCE;
            }
            case 2: {
                for (int i = 0; i < 5; i++) d.cultivate();
                for (int o : ar.others) { Character c = alive(o); if (c != null) for (int i = 0; i < 5; i++) c.cultivate(); }
                tell("The seekers around " + nm(d) + " meet at night beside the Empty Seat. The Seat is warm. ??? does not say who is listening.", d);
                return ADVANCE;
            }
            default: return HOLD; // they wait for the Door; the arc ends when they cross or die
        }
    }

    private static int teacher(Lore.State s, Arc ar) {
        Character t = alive(ar.a);
        Organization pal = Lore.palace();
        int lamp = -1;
        if (t != null) for (int i = 0; i < s.lamps.length; i++) if (s.lamps[i] == t.id) lamp = i;
        if (t == null || lamp < 0 || pal == null) { Chronicle.add("world", "The students of a Teacher of the Palace go on without them."); return FINISH; }
        switch (ar.stage) {
            case 1:
                for (int o : ar.others) { Character c = alive(o); if (c != null) for (int i = 0; i < 10; i++) c.cultivate(); }
                tell("Elder " + nm(t) + " teaches the Wicks in the Hall of the Nine Lamps at dawn. The students are tired, and better.", t);
                return ADVANCE;
            case 2:
                tell("Elder " + nm(t) + " has begun to give things away: a coat, a sword, a seat by the fire.", t);
                return ADVANCE;
            default: {
                Character best = null;
                for (int o : ar.others) { Character c = alive(o); if (c != null && c.sect == pal && c.majorLvl >= 3 && (best == null || c.lvl > best.lvl)) best = c; }
                if (best == null) { tell("Elder " + nm(t) + " waited for a student who was ready. None was. The lamp stays where it is.", t); return FINISH; }
                s.lamps[lamp] = -1;
                pal.memberRanks.put(t.id, Organization.SectRank.INNER_DISCIPLE);
                Lore.seatLamp(s, pal, best, lamp);
                tell("Elder " + nm(t) + " gave the Lamp of " + LoreText.LAMPS[lamp] + " to " + nm(best) + ", who was a Wick when the lessons began.", t, best);
                return FINISH;
            }
        }
    }

    // ------------------------------------------------------------------ the Defector's strength at the Door
    /** How many of the others who joined this seeker's cause are still alive (a seeker who is a Defector crosses with them: see LoreEndings.npcCross). */
    static int defectorAllies(Lore.State s, Character c) {
        if (s == null || s.arcs == null || c == null) return 0;
        for (Arc ar : s.arcs) {
            if (!ar.kind.equals("defector") || ar.a != c.id) continue;
            int n = 0; for (int o : ar.others) if (alive(o) != null) n++;
            return n;
        }
        return 0;
    }
}
