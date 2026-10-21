package Game;

import java.util.ArrayList;
import java.util.List;

/** One-time contextual hints (each id is shown once, ever, and can be switched off in Settings) and the step-by-step tutorial text that the
 *  console `tutorial` command prints and the GUI welcome window pages through. */
public class Hints {
    private Hints() { }

    /** Shows `text` the first time `id` fires for a human character. */
    static void trigger(Character h, String id, String text) {
        if (h == null || h.player != 1) return;
        if (!Settings.firstTime(id)) return;
        Notifications.post("hint", "Hint: " + text, -1, h.id);
    }

    /** One page of the welcome tour. `region` names the part of the main window the page lights up (see GuiHelp.regionRect), `helpId` the Help page it links to. */
    static class Page {
        final String title, body, region, helpId;
        Page(String title, String body) { this(title, body, null, null); }
        Page(String title, String body, String region, String helpId) { this.title = title; this.body = body; this.region = region; this.helpId = helpId; }
    }

    static List<Page> tutorial() {
        List<Page> p = new ArrayList<>();
        p.add(new Page("Welcome to the cultivation world",
                "Time passes in weeks. Each turn you choose one action (move, gather, fight...), then the whole world moves: 100,000 cultivators, "
              + "beasts, cities and sects, all living by the same rules as you.\n\n"
              + "You cultivate a little every week no matter what. Grow your realm, make friends and enemies, build a sect or a city, "
              + "or just survive: nothing is scripted.\n\n"
              + "A short tour lights up the parts of the window as it explains them. The Help explains every button: open it any time with F1 or the ? in the status bar.",
                null, "start"));
        p.add(new Page("The map",
                "This is the whole world: 31 x 31 tiles, north is up. Click a tile to read about it; double-click for a large picture. "
              + "Your own position is the yellow dot.\n\n"
              + "The bar above the map changes what the colours mean (terrain, beasts, population, level) and switches layers on: crowd outlines, fog of war, quest pins, a path line.",
                "map", "map"));
        p.add(new Page("Tile Info",
                "Everything about the tile you clicked: its name and picture, level, who lives there, beasts, bounties and the strongest cultivators (click a name to open them).\n\n"
              + "The button Move Here walks you to that tile, one step a week.",
                "tile", "tile-info"));
        p.add(new Page("Your actions: four tabs",
                "Move: walk, wait and type commands. Me: your character, backpack, family, quests and places. Auto: let the game play for you, quests, scripts. "
              + "World: browse everyone, manage shops and sects, rankings, saving and settings.\n\n"
              + "Every button has a typed command behind it, and every window has a ? button that opens its page of the Help.",
                "dock", "screen"));
        p.add(new Page("The status bar",
                "Your name, realm, cultivation progress (it fills, then you gain a level), age, wealth in spirit stones and your control mode. "
              + "The badge with a flag counts quest alerts that wait for you, the bell counts notices, and Go walks to your top quest. The ? opens the Help.",
                "status", "status-bar"));
        p.add(new Page("Time and the event log",
                "Each turn is a week. In MANUAL mode the world waits for you; the speed buttons and Pause matter when the game plays by itself (Auto, scripts, Watch). "
              + "Below the map the event log tells what happens in the world, with filters.",
                "speed", "speed"));
        p.add(new Page("Where to go next",
                "Open the Help (F1) for pages about every button and every feature, from your first steps to fighting, sects and the terminal commands. "
              + "The search box finds any word.\n\n"
              + "Press Done and click around: nothing you do can break the game, and you can save at any time.",
                null, "first-steps"));
        return p;
    }

    static String tutorialText() {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (Page pg : tutorial()) sb.append(i++).append(". ").append(pg.title).append("\n").append(pg.body).append("\n\n");
        return sb.toString();
    }
}
