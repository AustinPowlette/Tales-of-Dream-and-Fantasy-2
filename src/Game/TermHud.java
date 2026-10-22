package Game;

import java.util.ArrayList;
import java.util.List;

/** The terminal's per-turn status block: who/where/when, the mode, how many quest alerts wait, the most pressing one ("next: ...") and a
 *  rotating one-line "try:" tip for beginners. Console only (the GUI has its own status bar and panels); `settings hud off` hides it and
 *  `settings hints off` hides just the tip. Reads game state on the game thread only, and never rebuilds the quest numbering. */
final class TermHud {
    private TermHud() { }

    private static int tipCounter = 0;

    static boolean enabled() { return !GameDriver.guiEnabled && Settings.hud; }

    /** Prints the full block at the start of a manual turn (console only, unless switched off). */
    static boolean show(Character c) {
        if (!enabled() || c == null || c.location == null) return false;
        System.out.println(status(c));
        System.out.println(next(c));
        if (Settings.hints) System.out.println("try: " + tip(c));
        return true;
    }

    /** Printed before every read of a command; the first one of a turn follows the full block, the later ones (after an info command) get a short line. */
    static void prompt(Character c, boolean first) {
        if (!enabled() || c == null || c.location == null) return;
        if (!first) System.out.println(mini(c));
        System.out.print("> ");
        System.out.flush();
    }

    static String when() {
        int wk = GameDriver.time;
        return TermFormat.season(wk) + " yr " + wk / 52 + " wk " + (wk % 52 + 1);
    }

    static String status(Character c) {
        PlayerControl pc = c.pc();
        StringBuilder sb = new StringBuilder();
        sb.append(c.name).append(' ').append(c.lastName).append(" - ").append(TermFormat.realm(c.majorLvl)).append(" Lv.").append(c.lvl);
        if (c.cultivationBaseMax > 0) sb.append(" (cult ").append(TermFormat.pct(Math.min(1.0, c.cultivationBase / c.cultivationBaseMax))).append(')');
        sb.append(" | ").append(c.location.name).append(' ').append(PlayerControl.coord(c.location));
        sb.append(" | ").append(when());
        sb.append(" | ").append(pc.mode);
        int alerts = pc.inbox.size();
        sb.append(" | ").append(alerts == 0 ? "no alerts" : alerts + (alerts == 1 ? " alert" : " alerts"));
        return sb.toString();
    }

    static String mini(Character c) {
        PlayerControl pc = c.pc();
        int alerts = pc.inbox.size();
        return "[" + c.location.name + " " + PlayerControl.coord(c.location) + " | Lv." + c.lvl + " | " + pc.mode + " | " + (alerts == 0 ? "no alerts" : alerts + (alerts == 1 ? " alert" : " alerts")) + "]";
    }

    /** The most pressing announced quest (highest priority in the inbox), described briefly, with what to type. */
    static String next(Character c) {
        PlayerControl pc = c.pc();
        String poisoned = Life.poisonHint(c);
        if (poisoned != null) return poisoned;
        String reg = Lore.registerHint(c);
        if (reg != null) return reg;
        Quest best = null;
        for (Quest q : new ArrayList<>(pc.inbox)) {
            if (q == null) continue;
            if (q != c.currentQuest && !c.targetQuests.contains(q) && !c.spotQuests.contains(q)) continue;
            if (best == null || q.priority > best.priority) best = q;
        }
        if (best == null) return "next: nothing urgent. 'meditate' grows you, 'q' lists your tasks, 'help' lists the commands.";
        StringBuilder sb = new StringBuilder("next: ").append(best.displayName());
        boolean here = true;
        if (best.category == Quest.QuestCategory.Character) {
            Character t = Character.charById(best.targetCharacter);
            if (t != null && t.location != null) {
                sb.append(" -> ").append(t.name).append(' ').append(t.lastName).append(" at ").append(PlayerControl.coord(t.location));
                int d = PlayerControl.distance(c.location, t.location);
                if (d > 0) { sb.append(", ").append(d).append(d == 1 ? " tile" : " tiles"); here = false; }
            }
        } else if (best.category == Quest.QuestCategory.Area && best.targetArea != null) {
            sb.append(" -> ").append(best.targetArea.name).append(' ').append(PlayerControl.coord(best.targetArea));
            int d = PlayerControl.distance(c.location, best.targetArea);
            if (d > 0) { sb.append(", ").append(d).append(d == 1 ? " tile" : " tiles"); here = false; }
        }
        // #<uid> is stable (it works without listing 'q' first and never goes stale), so the hint can be typed as it stands
        String ref = best.uid > 0 ? " #" + best.uid : " <n>";
        sb.append("   -> ").append(here ? "qdo" : "goquest").append(ref).append(best.uid > 0 ? "" : "  ('q' numbers your quests)");
        return sb.toString();
    }

    /** One short, context-aware tip; rotates so each turn shows a different one. */
    static String tip(Character c) {
        Area a = c.location;
        PlayerControl pc = c.pc();
        List<String> tips = new ArrayList<>();
        if (!pc.inbox.isEmpty()) tips.add("'inbox' lists every quest alert, 'q' numbers your quests, then 'qdo <n>' (here) or 'goquest <n>' (walk there)");
        if (a.herbMaterials > 0) tips.add("'forage' gathers herbs here");
        if (a.metalMaterials > 0) tips.add("'mine' digs metal here");
        if (a.monsters > 0) tips.add("'hunt' fights the beasts of this tile");
        if (a.city) tips.add("'list shop' shows what is for sale, 'market' the goods market, 'library' techniques to learn");
        if (a.sect != null && c.sect == null) tips.add("'sect' shows the sect here, 'join' asks to enter it");
        if (a.characters.size() > 1) tips.add("'people' lists who is here, then 'talk 1', 'spar 1' or 'preview 1'");
        tips.add("'meditate' cultivates: it is how you grow stronger");
        tips.add("'map 6' draws your surroundings, 'l' lists places you know, 'goto <x> <y>' walks there");
        tips.add("'watch 20' lets time pass until something happens to you");
        tips.add("'i' is your sheet, 'bag' your backpack, 'techniques' your skills");
        tips.add("'auto 50' lets the character run itself for 50 turns ('stop' takes control back)");
        tips.add("'last' repeats your previous command, 'cmds' lists what you typed");
        tips.add("'help' groups every command; 'help <command>' explains one; 'settings hints off' hides this line");
        return tips.get(Math.floorMod(tipCounter++, tips.size()));
    }
}
