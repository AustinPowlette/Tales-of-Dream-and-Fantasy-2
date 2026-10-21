package Game;

import java.util.*;

/** A consistency check of the whole world, for the long parallel soak runs (-Dtdf.verify=true: run at the end of every tick, the kinds of violation counted with two examples each). Slow (it walks every person and tile): never on in play. */
final class Verify {
    private Verify() { }
    static final boolean ON = Boolean.getBoolean("tdf.verify");
    static long ticksChecked = 0, violations = 0;

    /** kind -> {count, first examples} */
    private static void note(TreeMap<String, Object[]> out, String kind, String example) {
        Object[] o = out.get(kind);
        if (o == null) out.put(kind, o = new Object[]{0, new ArrayList<String>()});
        o[0] = (Integer) o[0] + 1;
        @SuppressWarnings("unchecked") List<String> ex = (List<String>) o[1];
        if (ex.size() < 2) ex.add(example);
    }

    /** Walks the world and returns what is wrong: kind -> {count, examples}, empty when all is well. */
    static TreeMap<String, Object[]> check() {
        TreeMap<String, Object[]> bad = new TreeMap<>();
        ArrayList<Character> all = Map.characters;
        int n = all.size();
        IdentityHashMap<Character, Boolean> seen = new IdentityHashMap<>(n * 2);
        for (int i = 0; i < n; i++) {
            Character c = all.get(i);
            if (c == null) continue;
            if (c.id != i) note(bad, "id is not the position", "id " + c.id + " at index " + i + " (" + c.name + ")");
            if (seen.put(c, Boolean.TRUE) != null) note(bad, "listed twice", c.name + " #" + c.id);
            if (c.dead || c.location == null) continue;
            if (c.trait == Character.Traits.Beast || !c.born) continue;
            ArrayList<Character> here = c.location.characters;
            if (c.areaIdx < 0 || c.areaIdx >= here.size() || here.get(c.areaIdx) != c)
                note(bad, "a living person his tile does not list at his index", c.name + " #" + c.id + " (" + c.trait + ") tile " + c.location.x + "," + c.location.y + ", areaIdx " + c.areaIdx + ", tile holds " + here.size());
            if (c.sect != null && c.sect.founder != c && c.sect.master != c && !c.sect.members.contains(c)) note(bad, "in a sect that does not list him", c.name + " #" + c.id + " in " + c.sect.name);
        }
        for (Area a : new ArrayList<>(Map.createdAreas)) {
            if (a == null) continue;
            List<Character> here = new ArrayList<>(a.characters);
            for (int i = 0; i < here.size(); i++) {
                Character c = here.get(i);
                if (c == null) { note(bad, "a null in a tile's people", a.name); continue; }
                if (c.dead) note(bad, "dead but still listed (" + c.trait + ")", c.name + " #" + c.id + " cause " + c.deathCause + " at " + a.name + ", areaIdx " + c.areaIdx + " position " + i);
                else if (c.location != a) note(bad, "listed at a tile he does not stand on", c.name + " #" + c.id + " listed at " + a.name + " but at " + (c.location == null ? "nowhere" : c.location.name));
                else if (c.areaIdx != i) note(bad, "areaIdx is not the position", c.name + " #" + c.id + " areaIdx " + c.areaIdx + " position " + i + " at " + a.name);
            }
            if (a.sect != null) {
                for (Character m : a.sect.members) if (m != null && (m.dead || m.sect != a.sect)) note(bad, m.dead ? "a dead sect member" : "a sect member who belongs elsewhere", a.sect.name + ": " + m.name + " #" + m.id + " (cause " + m.deathCause + ", age " + m.age + ", own sect " + (m.sect == null ? "none" : m.sect.name) + ", master " + (a.sect.master == m) + ", founder " + (a.sect.founder == m) + ", died at " + m.diedAt + ", born " + m.born + ", trait " + m.trait + ", time " + m.time + ")");
            }
        }
        return bad;
    }

    /** End of a tick: check and report. */
    static void tick() {
        if (!ON) return;
        ticksChecked++;
        TreeMap<String, Object[]> bad = check();
        StringBuilder sb = new StringBuilder();
        for (java.util.Map.Entry<String, Object[]> e : bad.entrySet()) {
            violations += (Integer) e.getValue()[0];
            sb.append("\n    ").append(e.getValue()[0]).append("x ").append(e.getKey()).append(" e.g. ").append(e.getValue()[1]);
        }
        if (sb.length() > 0) System.err.println("[VERIFY] week " + GameDriver.time + ":" + sb);
        else if (ticksChecked % 100 == 0) System.err.println("[VERIFY] " + ticksChecked + " ticks checked, all well");
    }
}
