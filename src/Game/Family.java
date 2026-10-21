package Game;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Data for the family tree and the relationship graph (shared by the console `family` / `relations` commands and the GUI windows). */
public class Family {
    private Family() { }

    static class Person {
        int id, gen; String name, gender, label; int lvl; boolean dead; int age;
        int x, y; // layout, filled by the GUI
    }

    static class Tree {
        final List<List<Person>> rows = new ArrayList<>();   // index 0 = oldest generation shown
        final List<Person[]> edges = new ArrayList<>();      // {parent, child}
        int selfGen;                                        // which row is the player's generation
        Person self;
    }

    private static Person person(Character c, int gen, String label) {
        Person p = new Person();
        p.id = c.id; p.gen = gen; p.name = c.name + " " + c.lastName; p.gender = c.gender; p.lvl = c.lvl; p.dead = c.dead; p.age = c.age;
        p.label = c.lives > 0 ? label + " (reborn x" + c.lives + ")" : label;
        return p;
    }

    /** Ancestors up to `up` generations, the player's generation (spouse, siblings) and descendants down `down` generations. */
    static Tree build(Character me, int up, int down) {
        Map<Integer, Person> byId = new LinkedHashMap<>();
        Tree t = new Tree();
        t.self = person(me, 0, "you");
        byId.put(me.id, t.self);
        // ancestors
        List<Character> layer = new ArrayList<>(); layer.add(me);
        for (int g = 1; g <= up; g++) {
            List<Character> next = new ArrayList<>();
            for (Character c : layer) {
                for (Character par : new Character[]{c.mother, c.father}) {
                    if (par == null) continue;
                    String label = g == 1 ? (par == c.mother ? "mother" : "father") : g == 2 ? "grandparent" : "ancestor";
                    if (!byId.containsKey(par.id)) { Person p = person(par, -g, label); byId.put(par.id, p); next.add(par); }
                    Person pp = byId.get(par.id), cp = byId.get(c.id);
                    if (cp != null) t.edges.add(new Person[]{pp, cp});
                }
            }
            layer = next;
        }
        // own generation
        if (me.spouse != null) byId.put(me.spouse.id, person(me.spouse, 0, "spouse"));
        for (Character s : me.siblings) if (s != null && !byId.containsKey(s.id)) {
            Person p = person(s, 0, "sibling"); byId.put(s.id, p);
            for (Character par : new Character[]{s.mother, s.father}) if (par != null && byId.containsKey(par.id)) t.edges.add(new Person[]{byId.get(par.id), p});
        }
        // descendants
        layer = new ArrayList<>(); layer.add(me);
        for (int g = 1; g <= down; g++) {
            List<Character> next = new ArrayList<>();
            for (Character c : layer) {
                for (Character ch : c.children) {
                    if (ch == null) continue;
                    if (!byId.containsKey(ch.id)) { byId.put(ch.id, person(ch, g, g == 1 ? "child" : g == 2 ? "grandchild" : "descendant")); next.add(ch); }
                    t.edges.add(new Person[]{byId.get(c.id), byId.get(ch.id)});
                }
            }
            layer = next;
        }
        int min = 0, max = 0;
        for (Person p : byId.values()) { min = Math.min(min, p.gen); max = Math.max(max, p.gen); }
        for (int g = min; g <= max; g++) t.rows.add(new ArrayList<>());
        for (Person p : byId.values()) t.rows.get(p.gen - min).add(p);
        t.selfGen = -min;
        return t;
    }

    static String text(Character me) {
        Tree t = build(me, 3, 3);
        StringBuilder sb = new StringBuilder("=== Family of " + me.name + " " + me.lastName + " ===\n");
        for (int r = 0; r < t.rows.size(); r++) {
            int g = r - t.selfGen;
            String title = g <= -3 ? "Ancestors" : g == -2 ? "Grandparents" : g == -1 ? "Parents" : g == 0 ? "You, spouse & siblings" : g == 1 ? "Children" : g == 2 ? "Grandchildren" : "Descendants";
            if (t.rows.get(r).isEmpty()) continue;
            sb.append(title).append(":\n");
            for (Person p : t.rows.get(r)) sb.append(String.format("  #%-6d %-28s Lv.%-4d age %-4d %s%s%n", p.id, p.name, p.lvl, p.age, p.label, p.dead ? "  (dead)" : ""));
        }
        if (t.rows.size() == 1 && t.rows.get(0).size() == 1) sb.append("  (no known family yet)\n");
        return sb.toString();
    }

    // ------------------------------------------------------------------ relationships
    static class Tie {
        Character other; int value; String label;
    }

    static String label(int v) {
        return v >= 3 ? "best friend" : v == 2 ? "close friend" : v == 1 ? "friend" : v == 0 ? "acquaintance" : v == -1 ? "dislike" : v == -2 ? "rival" : "arch-enemy";
    }

    /** The player's strongest ties (by |value|, then level), alive only, at most n. */
    static List<Tie> ties(Character me, int n) {
        List<Tie> out = new ArrayList<>();
        for (Relationship r : new ArrayList<>(me.relationshipList.relationships)) {
            Character o = Character.charById(r.friendID);
            if (o == null || o.dead || o == me) continue;
            Tie t = new Tie(); t.other = o; t.value = r.value; t.label = label(r.value);
            if (me.spouse == o) t.label = "spouse";
            else if (me.mother == o) t.label = "mother"; else if (me.father == o) t.label = "father"; else if (me.children.contains(o)) t.label = "child"; else if (me.siblings.contains(o)) t.label = "sibling";
            out.add(t);
        }
        if (me.spouse != null && !me.spouse.dead && out.stream().noneMatch(t -> t.other == me.spouse)) { Tie t = new Tie(); t.other = me.spouse; t.value = 3; t.label = "spouse"; out.add(t); }
        out.sort(Comparator.<Tie>comparingInt(t -> -Math.abs(t.value)).thenComparingInt(t -> -t.other.lvl));
        return out.size() > n ? new ArrayList<>(out.subList(0, n)) : out;
    }

    static String relationsText(Character me, int n) {
        StringBuilder sb = new StringBuilder("=== Relationships of " + me.name + " (strongest " + n + ") ===\n");
        for (Tie t : ties(me, n)) sb.append(String.format("  #%-6d %-28s Lv.%-4d %-13s (%+d)%n", t.other.id, t.other.name + " " + t.other.lastName, t.other.lvl, t.label, t.value));
        return sb.toString();
    }
}
