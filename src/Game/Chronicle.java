package Game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The world's memory: a structured chronicle of notable events (deaths of the mighty, breakthroughs to Core Formation and beyond, new cities and
 *  sects, wars, tournaments, calamities), the causes of every cultivator's death, yearly life samples of the human characters (for the
 *  Biography chart) and the player's bookmarks/notes. All of it is saved with the world (see SaveData). Recording is cheap and thread-safe
 *  (cultivation runs on worker threads). */
final class Chronicle {
    private Chronicle() { }

    static final int MAX_ENTRIES = 6000;
    static final String[] KINDS = {"death", "breakthrough", "city", "sect", "war", "world", "legend", "reincarnation", "fight"};

    static class Entry implements Serializable {
        static final long serialVersionUID = 1L;
        final int week;
        final String kind, text;
        final int[] ids;   // cultivators involved (the first is the subject)
        Entry(int week, String kind, String text, int[] ids) { this.week = week; this.kind = kind; this.text = text; this.ids = ids; }
        int year() { return week / 52; }
        String when() { return "Year " + year() + ", wk " + (week % 52 + 1); }
        @Override public String toString() { return when() + "  [" + kind + "] " + text; }
    }

    static final List<Entry> log = Collections.synchronizedList(new ArrayList<>());
    static final Map<String, Integer> deathCauses = Collections.synchronizedMap(new LinkedHashMap<>()); // cause class -> count (cultivators only)
    static final Map<Integer, ArrayList<int[]>> bio = Collections.synchronizedMap(new HashMap<>());      // human id -> {year, lvl, log10 wealth *100, friends, kills}
    static final Map<String, String> notes = Collections.synchronizedMap(new LinkedHashMap<>());          // "t:x,y" or "c:id" -> note text

    static void reset() { log.clear(); deathCauses.clear(); bio.clear(); notes.clear(); }

    // ------------------------------------------------------------------ recording
    static void add(String kind, String text, int... ids) {
        if (text == null) return;
        log.add(new Entry(GameDriver.time, kind, text, ids == null ? new int[0] : ids));
        synchronized (log) {
            while (log.size() > MAX_ENTRIES) { // a flood of small world lines must not push out deaths, cities and wars: they go first
                int victim = 0;
                for (int i = 0; i < log.size() && i < 2000; i++) if ("world".equals(log.get(i).kind)) { victim = i; break; }
                log.remove(victim);
            }
        }
    }

    private static String who(Character c) { return c.name + " " + c.lastName + " (#" + c.id + ")"; }

    /** A cultivator died. Counts the cause; the chronicle keeps only the deaths that mattered. */
    static void death(Character dead, Character killer) {
        if (dead == null || dead.trait == Character.Traits.Beast || dead.trait == Character.Traits.Monster) return;
        String cls;
        if (killer == null) {
            String dc = dead.deathCause == null ? "" : dead.deathCause;
            cls = dc.startsWith("old age") ? "old age" : dc.contains("Array") ? "arrays & traps" : dc.contains("Forbidden") ? "forbidden technique" : dead.purgedByHeaven() ? "the epoch" : "other";
        } else cls = (killer.trait == Character.Traits.Beast || killer.trait == Character.Traits.Monster) ? "beasts" : "battle";
        synchronized (deathCauses) { deathCauses.merge(cls, 1, Integer::sum); }
        boolean notable = dead.majorLvl >= 3 || dead.player > 0 || Game.Map.trackedCharacters.contains(dead.id)
                || (dead.sect != null && dead.sect.master == dead) || dead.storyNpc;
        if (!notable) return;
        String how = killer != null ? "was slain by " + who(killer) : dead.purgedByHeaven() ? "was consumed by Heaven in the epoch" : dead.deathCause != null && dead.deathCause.startsWith("old age") ? "died of old age at " + dead.age : "died (" + dead.deathCause + ")";
        add("death", who(dead) + ", Major Lvl " + dead.majorLvl + (dead.sect != null ? " of " + dead.sect.name : "") + ", " + how + " at " + (dead.location == null ? "?" : dead.location.name) + ".",
                killer != null ? new int[]{dead.id, killer.id} : new int[]{dead.id});
    }

    static void breakthrough(Character c) {
        if (c.majorLvl < 3 || c.trait == Character.Traits.Beast) return;
        String realm = c.majorLvl >= 10 ? "True Immortal" : "Major Lvl " + c.majorLvl;
        add("breakthrough", who(c) + " broke through to " + realm + " at age " + c.age + ".", c.id);
    }

    static void founded(String kind, String what, Character founder, Area at) {
        if (founder.player > 0) RunStats.onFounded();
        add(kind, who(founder) + " founded " + what + " at " + (at == null ? "?" : at.name) + ".", founder.id);
    }

    // ------------------------------------------------------------------ queries
    /** Newest first. `q` matches text (a "#123" token matches a cultivator id); `kind` null/"all" = every kind. */
    static List<Entry> search(String q, String kind, int limit) {
        String needle = q == null ? "" : q.trim().toLowerCase();
        int id = -1;
        if (needle.matches("#\\d+")) { try { id = Integer.parseInt(needle.substring(1)); } catch (NumberFormatException ignored) { } }
        List<Entry> out = new ArrayList<>();
        synchronized (log) {
            for (int i = log.size() - 1; i >= 0 && out.size() < limit; i--) {
                Entry e = log.get(i);
                if (kind != null && !kind.equals("all") && !e.kind.equals(kind)) continue;
                if (id >= 0) { boolean hit = false; for (int x : e.ids) if (x == id) hit = true; if (!hit) continue; }
                else if (!needle.isEmpty() && !e.text.toLowerCase().contains(needle)) continue;
                out.add(e);
            }
        }
        return out;
    }

    /** Everything that involves a cultivator, oldest first (the text for `history <name>`). */
    static List<Entry> about(Character c) {
        List<Entry> r = search("#" + c.id, "all", 2000);
        Collections.reverse(r);
        return r;
    }

    static String render(List<Entry> list) {
        StringBuilder sb = new StringBuilder();
        for (Entry e : list) sb.append(e).append('\n');
        return sb.toString();
    }

    // ------------------------------------------------------------------ yearly life samples of human characters
    static void yearly() {
        int year = GameDriver.time / 52;
        List<Character> who = new ArrayList<>(Notifications.humans());
        for (int id : new ArrayList<>(Game.Map.trackedCharacters)) { Character c = Character.charById(id); if (c != null && !who.contains(c)) who.add(c); }
        for (Character h : who) {
            if (h == null || h.dead) continue;
            ArrayList<int[]> list;
            synchronized (bio) { list = bio.computeIfAbsent(h.id, k -> new ArrayList<>()); }
            double wealth = h.ss + h.sc * 1e6 + h.dc * 1e12;
            int friends = 0;
            for (Relationship r : h.relationshipList.relationships) if (r.value >= 1) friends++;
            synchronized (list) { list.add(new int[]{year, h.lvl, (int) (Math.log10(wealth + 1) * 100), friends, h.kills}); }
        }
    }

    static List<int[]> bioOf(int id) {
        ArrayList<int[]> list = bio.get(id);
        if (list == null) return new ArrayList<>();
        synchronized (list) { return new ArrayList<>(list); }
    }

    // ------------------------------------------------------------------ bookmarks and notes
    static String tileKey(Area a) { return "t:" + a.x + "," + a.y; }
    static String charKey(Character c) { return "c:" + c.id; }

    static String describeKey(String k) {
        if (k.startsWith("t:")) {
            String[] p = k.substring(2).split(",");
            try {
                Area a = Game.Map.tiles[Integer.parseInt(p[0])][Integer.parseInt(p[1])];
                return (a == null ? "unexplored tile" : a.name) + " " + "[" + Integer.parseInt(p[0]) * 100 + ", " + Integer.parseInt(p[1]) * 100 + "]";
            } catch (RuntimeException e) { return k; }
        }
        if (k.startsWith("c:")) {
            try { Character c = Character.charById(Integer.parseInt(k.substring(2))); return c == null ? "(gone) #" + k.substring(2) : c.name + " " + c.lastName + " #" + c.id; }
            catch (RuntimeException e) { return k; }
        }
        return k;
    }

    // ------------------------------------------------------------------ save / load
    static void snapshotInto(SaveData d) {
        synchronized (log) { d.chronicle = new ArrayList<>(log); }
        synchronized (deathCauses) { d.deathCauses = new HashMap<>(deathCauses); }
        synchronized (bio) {
            HashMap<Integer, ArrayList<int[]>> copy = new HashMap<>();
            for (Map.Entry<Integer, ArrayList<int[]>> e : bio.entrySet()) synchronized (e.getValue()) { copy.put(e.getKey(), new ArrayList<>(e.getValue())); }
            d.bio = copy;
        }
        synchronized (notes) { d.notes = new HashMap<>(notes); }
    }

    static void restoreFrom(SaveData d) {
        reset();
        if (d.chronicle != null) log.addAll(d.chronicle);
        if (d.deathCauses != null) deathCauses.putAll(d.deathCauses);
        if (d.bio != null) bio.putAll(d.bio);
        if (d.notes != null) notes.putAll(d.notes);
    }
}
