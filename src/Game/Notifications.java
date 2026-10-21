package Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Things the human player should hear about even while doing something else: breakthroughs, deaths of family and friends, wars involving
 *  their sect, births and marriages, a crowd turning dangerous, new quest alerts, the yearly auction. Each becomes a `[Notice]` line in the
 *  log, an entry in the notification history, and (GUI) a toast; a category can be muted in Settings. Also home of the one-time hints and
 *  the periodic recap text. Posting is cheap and safe from the game thread. */
public class Notifications {
    static final String[] CATEGORIES = {"breakthrough", "level", "death", "war", "family", "alert", "danger", "auction", "hint", "recap", "fortune", "reincarnation", "fight", "sect", "music"};
    static final String[] LABELS = {"Breakthroughs", "Level-ups", "Deaths of people you know", "War & peace", "Births & marriages", "New quest alerts",
            "Danger (crowds, lifespan)", "Auctions", "Hints", "Recaps", "Good fortune", "Reincarnation", "Fight reports", "Sect intrigue"};

    static class Note {
        final long at = System.currentTimeMillis();
        final int week = GameDriver.time;
        final String cat, text;
        final int charId;   // a cultivator the note is about (opens their dialog), or -1
        final int forId;    // the human character it is for, or -1 for everyone
        volatile boolean read = false;
        Note(String cat, String text, int charId, int forId) { this.cat = cat; this.text = text; this.charId = charId; this.forId = forId; }
    }

    static final List<Note> history = Collections.synchronizedList(new ArrayList<>());
    static volatile Consumer<Note> listener = null;         // the GUI's toast hook
    private static volatile List<Character> humans = new ArrayList<>();
    private static final Map<Integer, int[]> last = new HashMap<>(); // per human: {majorLvl, lvl, inCrowdHotspot, lifespanWarned, lastAuctionYear}

    /** The humans the death/war/birth hooks tell (refreshed every tick by the game loop). */
    static void setHumans(List<Character> l) { humans = l; }
    static List<Character> humans() { return humans; }

    static void reset() { history.clear(); synchronized (last) { last.clear(); } Recap.reset(); }

    static boolean enabled(String cat) { return !Settings.mutedNotifications.contains(cat); }

    /** Posts a notice. `forId` = the human it is for (or -1). Returns null when the category is muted. */
    static Note post(String cat, String text, int charId, int forId) { return post(cat, text, charId, forId, true); }

    /** Same, without the `[Notice]` echo in the log (the caller already printed the full text, e.g. a fight report). */
    static Note postQuiet(String cat, String text, int charId, int forId) { return post(cat, text, charId, forId, false); }

    private static Note post(String cat, String text, int charId, int forId, boolean echo) {
        if (!enabled(cat)) return null;
        Note n = new Note(cat, text, charId, forId);
        history.add(n);
        while (history.size() > 500) history.remove(0);
        if (echo) System.out.println("[Notice] " + text);
        wake(cat, forId);
        Consumer<Note> l = listener;
        if (l != null && Settings.toasts) {
            try { l.accept(n); } catch (RuntimeException ignored) { }
        }
        if (Settings.sound && GameDriver.guiEnabled) { try { java.awt.Toolkit.getDefaultToolkit().beep(); } catch (Throwable ignored) { } }
        return n;
    }

    /** A notice reaches the humans it is for: ends a `watch` fast-forward and, per the auto-pause rules, pauses the game. */
    private static void wake(String cat, int forId) {
        for (Character h : humans) {
            if (h == null || h.dead || h.player != 1 || (forId >= 0 && forId != h.id)) continue;
            PlayerControl p = h.pc;
            if (p != null && p.watching && (p.watchCats == null ? !cat.equals("hint") && !cat.equals("recap") : p.watchCats.contains(cat))) { p.watching = false; h.skipping = 0; }
            if (Settings.pauseOn.contains(cat) && GameDriver.guiEnabled) GameDriver.paused = true;
        }
    }

    static int unread() { int n = 0; synchronized (history) { for (Note x : history) if (!x.read) n++; } return n; }

    // ------------------------------------------------------------------ hooks called from the game code
    private static String relationLabel(Character h, Character other) {
        if (h.spouse == other) return "spouse";
        if (h.mother == other) return "mother";
        if (h.father == other) return "father";
        if (h.children.contains(other)) return "child";
        if (h.siblings.contains(other)) return "sibling";
        Relationship r = h.relationshipList.contains(other.id);
        if (r == null) return null;
        if (r.value >= 3) return "best friend";
        if (r.value == 2) return "close friend";
        if (r.value == 1) return "friend";
        if (r.value <= -2) return "rival";
        return null;
    }

    /** A cultivator died (call before their spouse/relationship links are cleared). */
    static void onDeath(Character dead, Character killer) {
        if (dead == null || dead.player == 1) return; // a human's own death has its own screen
        for (Character h : humans) {
            if (h == null || h.dead || h == dead || h.player != 1) continue;
            String rel = relationLabel(h, dead);
            if (rel == null) continue;
            String how = killer != null ? ", killed by " + killer.name + " " + killer.lastName : "";
            post("death", dead.purgedByHeaven() ? "Heaven has consumed your " + rel + " " + dead.name + " " + dead.lastName + " in the epoch."
                    : "Your " + rel + " " + dead.name + " " + dead.lastName + " has died" + how + ".", dead.id, h.id);
            RunStats.onLoved();
        }
    }

    static void onWar(Organization a, Organization b) {
        for (Character h : humans) {
            if (h == null || h.dead || h.player != 1 || h.sect == null) continue;
            if (h.sect == a || h.sect == b) post("war", a.name + " is now at war with " + b.name + (h.sect == a ? "" : " — your sect is under attack") + ".", -1, h.id);
        }
    }

    static void onBirth(Character child, Character mother, Character father) {
        for (Character h : humans) {
            if (h == null || h.dead || h.player != 1) continue;
            if (h == mother || h == father) RunStats.onChild();
            if (h == mother || h == father) post("family", "Your child " + child.name + " " + child.lastName + " was born.", child.id, h.id);
        }
    }

    static void onMarriage(Character a, Character b) {
        for (Character h : humans) {
            if (h == null || h.dead || h.player != 1) continue;
            if (h == a) post("family", "You married " + b.name + " " + b.lastName + ". (To try for a child, stand in one tile and press 'Try for a child' in the Me tab, or type 'child'.)", b.id, h.id);
            else if (h == b) post("family", "You married " + a.name + " " + a.lastName + ". (To try for a child, stand in one tile and press 'Try for a child' in the Me tab, or type 'child'.)", a.id, h.id);
        }
    }

    /** A best friend or a relative brought a gift (the `Give Gift` quest, see Gifts). */
    static void onGift(Character h, Character giver, String line) {
        if (h == null || h.player != 1) return;
        post("family", line, giver == null ? -1 : giver.id, h.id);
    }

    static void onAlerts(Character h, int count, String first) {
        if (count <= 0 || h.player != 1) return;
        postQuiet("alert", count == 1 ? "New quest alert: " + first : count + " new quest alerts (latest: " + first + ").", -1, h.id); // (the [Quest Alert] line already says it)
        Hints.trigger(h, "first-alert", "Alerts are quests you have to do yourself: open the quest panel (Q or the orange flag badge), travel to the target, then Do Quest.");
    }

    /** Once a tick for each human: level changes, crowd danger, lifespan. */
    static void observe(Character c) {
        if (c.player != 1) return;
        int[] st;
        synchronized (last) { st = last.computeIfAbsent(c.id, k -> new int[]{c.majorLvl, c.lvl, 0, 0, -1}); }
        if (c.potentialHidden && c.majorLvl >= 1) {
            c.potentialHidden = false;
            post("breakthrough", "Your talent is revealed: potential " + c.potential + ".", -1, c.id);
        }
        if (c.majorLvl > st[0]) {
            post("breakthrough", "You broke through to Major Lvl " + c.majorLvl + "!", -1, c.id);
            Hints.trigger(c, "first-breakthrough", "Breakthroughs unlock bigger things: Major 2 can found a sect or a city. Check the Manage window.");
        } else if (c.lvl > st[1] && c.majorLvl == st[0]) post("level", "You reached level " + c.lvl + ".", -1, c.id);
        st[0] = c.majorLvl; st[1] = c.lvl;
        boolean hot = c.location != null && c.location.crowdHotspot;
        if (hot && st[2] == 0) {
            post("danger", c.location.name + " is overcrowded: beasts are ambushing everyone here. Leave soon.", -1, c.id);
            Hints.trigger(c, "first-hotspot", "Crowded tiles turn into hotspots. Use the map's Crowd layer to see them and move to a quieter tile.");
        }
        st[2] = hot ? 1 : 0;
        if (st[3] == 0 && c.age >= c.maxAge - 5) { st[3] = 1; post("danger", "Your lifespan is nearly spent (age " + c.age + " of " + c.maxAge + ").", -1, c.id); }
        if (st[3] == 1 && c.age < c.maxAge - 10) st[3] = 0;
    }

    /** The yearly auction is near and the human is standing in the auctioning city (once per year). */
    static void onAuctionSoon(Character c, Area city, int weeks) {
        int year = GameDriver.time / 52;
        int[] st;
        synchronized (last) { st = last.computeIfAbsent(c.id, k -> new int[]{c.majorLvl, c.lvl, 0, 0, -1}); }
        if (st[4] == GameDriver.time / 4) return; // once a month at most
        st[4] = GameDriver.time / 4;
        post("auction", city.name + " holds an auction in about " + weeks + " week(s). Stay in the city to bid ('auction' lists the lots).", -1, c.id);
        Hints.trigger(c, "first-auction", "At an auction you set a maximum bid per lot; the winner pays the runner-up's bid, so bidding your true value is safe.");
    }
}
