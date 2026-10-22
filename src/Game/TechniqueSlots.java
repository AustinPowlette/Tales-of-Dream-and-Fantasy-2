package Game;

import java.util.ArrayList;

/** The hand of a person who plays on the four technique slots (Martial, Martial 2, Movement, Movement 2): swap the two of a kind, wear a known technique, forget one. In MANUAL mode the game no longer sorts the
 *  slots behind the person's back (see `Character.equip`); `equip` and the Equip best button still sort them. Every method returns the sentence to say; `null` for a kind or number it did not understand is never returned. */
final class TechniqueSlots {
    private TechniqueSlots() { }

    /** Martial or Movement from a word ("martial", "mov", "m2"...); null if it is neither. */
    static Technique.techniqueType kindOf(String s) {
        if (s == null) return null;
        String l = s.toLowerCase();
        if (l.startsWith("mar")) return Technique.techniqueType.Martial;
        if (l.startsWith("mov")) return Technique.techniqueType.Movement;
        return null;
    }

    static Technique slot(Character c, Technique.techniqueType k, int n) {
        if (k == Technique.techniqueType.Martial) return n == 1 ? c.martialTechnique : c.martialTechnique2;
        return n == 1 ? c.movementTechnique : c.movementTechnique2;
    }

    private static void put(Character c, Technique.techniqueType k, int n, Technique t) {
        if (k == Technique.techniqueType.Martial) { if (n == 1) c.martialTechnique = t; else c.martialTechnique2 = t; }
        else { if (n == 1) c.movementTechnique = t; else c.movementTechnique2 = t; }
    }

    private static boolean isDefault(Technique t) { return t == null || "Default Technique".equals(t.name); }

    private static String show(Technique t) { return t.name + " (lvl " + t.lvl + " / potential " + t.potential + ")"; }

    private static String autoNote(Character c) {
        return c.player == 1 && c.pc != null && c.pc.mode == PlayerControl.Mode.MANUAL ? "" : " (Outside Manual mode the game sorts the slots itself, so this may be undone.)";
    }

    private static void refresh(Character c) { c.tempLvlCheck(); }

    /** `swaptech martial|movement`: slot 1 and slot 2 of that kind change places. */
    static String swap(Character c, String kindWord) {
        Technique.techniqueType k = kindOf(kindWord);
        if (k == null) return "Usage: swaptech martial|movement   (the first and the second technique of that kind change places)";
        Technique a = slot(c, k, 1), b = slot(c, k, 2);
        if (b == null) return "You have no second " + k.name().toLowerCase() + " technique to swap with.";
        put(c, k, 1, b); put(c, k, 2, a);
        refresh(c);
        return "Your " + k.name().toLowerCase() + " technique is now " + show(b) + "; " + (a == null ? "the second slot is empty" : show(a) + " is the second") + "." + autoNote(c);
    }

    /** `usetech <known#> [1|2]`: wears a known technique in slot 2 (or slot 1) of its kind; the one it replaces goes back to the known list. */
    static String use(Character c, int number, int slotNo) {
        if (c.techniqueList == null || number < 1 || number > c.techniqueList.size()) return "Usage: usetech <n> [1|2]   (n is a number from the Known list of 'techniques', 1-" + (c.techniqueList == null ? 0 : c.techniqueList.size()) + ")";
        if (slotNo != 1 && slotNo != 2) slotNo = 2;
        Technique t = c.techniqueList.get(number - 1);
        Technique old = slot(c, t.type, slotNo);
        c.techniqueList.remove(number - 1);
        put(c, t.type, slotNo, t);
        if (old != null && !isDefault(old)) c.techniqueList.add(old);
        refresh(c);
        return "You wear " + show(t) + " as your " + (slotNo == 1 ? "main " : "second ") + t.type.name().toLowerCase() + " technique." + (old != null && !isDefault(old) ? " " + old.name + " is back among the ones you know." : "") + autoNote(c);
    }

    /** `forget martial|martial2|movement|movement2|known N`: the technique is gone for good (a library pays for a book you have not worn: 'Sell my best technique'). The first slot is never left empty. */
    static String forget(Character c, String what, int number) {
        if (what == null) return "Usage: forget martial|martial2|movement|movement2|known <n>";
        String w = what.toLowerCase();
        if (w.startsWith("kno")) {
            if (c.techniqueList == null || number < 1 || number > c.techniqueList.size()) return "No such known technique. 'techniques' numbers them 1-" + (c.techniqueList == null ? 0 : c.techniqueList.size()) + ".";
            Technique t = c.techniqueList.remove(number - 1);
            return "You forget " + show(t) + ".";
        }
        Technique.techniqueType k = kindOf(w);
        if (k == null) return "Usage: forget martial|martial2|movement|movement2|known <n>";
        boolean second = w.endsWith("2");
        Technique t = slot(c, k, second ? 2 : 1);
        if (t == null) return "That slot is empty.";
        if (!second && isDefault(t) && slot(c, k, 2) == null && best(c, k) == null) return "That is the Default Technique, and you know nothing else of that kind: there is nothing to forget.";
        String filled;
        if (second) { put(c, k, 2, null); filled = "the second slot is empty now"; }
        else if (slot(c, k, 2) != null) { put(c, k, 1, slot(c, k, 2)); put(c, k, 2, null); filled = show(slot(c, k, 1)) + " takes the first place"; }
        else {
            Technique b = best(c, k);
            if (b != null) { c.techniqueList.remove(b); put(c, k, 1, b); filled = show(b) + " takes the first place"; }
            else { put(c, k, 1, new Technique(k)); filled = "you fall back on the Default Technique"; }
        }
        refresh(c);
        return "You forget " + show(t) + "; " + filled + "." + autoNote(c);
    }

    /** The known technique of a kind with the highest level (then the highest potential), or null. */
    private static Technique best(Character c, Technique.techniqueType k) {
        Technique b = null;
        if (c.techniqueList != null) for (Technique t : new ArrayList<>(c.techniqueList)) if (t.type == k && (b == null || t.lvl > b.lvl || (t.lvl == b.lvl && t.potential > b.potential))) b = t;
        return b;
    }
}
