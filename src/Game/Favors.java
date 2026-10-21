package Game;

import java.util.ArrayList;
import java.util.List;

import Game.Dialogue.Ctx;

/** A favor: a talk in the portrait window can end with a request that becomes a real quest. Someone whose place is troubled by beasts asks the person who plays to thin them out; the request goes into the Quest Log
 *  (a Hunt Beast quest at that place, marked with who asked: `Quest.giver`) and when it is done the one who asked pays what they promised and thinks better of the hero. At most two favors are open at once and the
 *  next is not asked for at least `COOLDOWN` weeks after the last one. */
final class Favors {
    private Favors() { }

    static final int COOLDOWN = 8, MAX_OPEN = 2;

    static int open(Character me) {
        int n = 0;
        if (me.currentQuest != null && me.currentQuest.giver > 0) n++;
        for (Quest q : new ArrayList<>(me.targetQuests)) if (q != null && q.giver > 0) n++;
        return n;
    }

    /** The troubled place near the speaker: a place the speaker knows (or stands in) with beasts, not too far from the listener and not far above his strength. null: none. */
    static Area troubledPlace(Character npc, Character me) {
        List<Area> pool = new ArrayList<>();
        if (npc.location != null) pool.add(npc.location);
        try { for (Area a : new ArrayList<>(npc.knownLocations)) if (a != null && !pool.contains(a)) pool.add(a); } catch (RuntimeException e) { }
        Area best = null;
        for (Area a : pool) {
            if (a.monsters <= 0 || a.city || a.lvl > me.lvl + 8) continue;
            int d = PlayerControl.distance(me.location, a);
            if (d > 14) continue;
            if (best == null || RNDpick()) best = a;
        }
        return best;
    }

    private static boolean RNDpick() { return GameDriver.rand.nextInt(3) == 0; }

    /** The scene's weight: 0 when no favor can be asked (cooldown, too many open, nobody who can pay, no troubled place). */
    static double weight(Ctx c) {
        PlayerControl pc = c.me.pc();
        if (pc == null || GameDriver.time < pc.nextFavorWeek || open(c.me) >= MAX_OPEN) return 0;
        if (c.npc.age < 14 || c.npc.ss < 60 || c.npc.affiliationNum < 0 && c.relValue() <= 0) return 0;
        if (c.relValue() < 0) return 0;
        return troubledPlace(c.npc, c.me) == null ? 0 : 1.4;
    }

    static long rewardFor(Ctx c, Area a) {
        return Math.max(30, Math.min(c.npc.ss / 2, 30 + 8L * a.lvl));
    }

    /** The scene: the request, the reward, and the answer. */
    static void scene(Ctx c) {
        Area a = troubledPlace(c.npc, c.me);
        PlayerControl pc = c.me.pc();
        if (a == null || pc == null) { c.say(c.line("greet")); return; }
        long reward = rewardFor(c, a);
        c.thing = a.name; c.price = reward + " ss";
        String where = a == c.npc.location ? "here" : "at " + a.name + " (" + PlayerControl.coord(a) + ")";
        c.say(c.open() + c.any(
                "Could I ask a favor? Beasts have been getting bold " + where + ". They've scared off every honest worker in the area.",
                "You look like someone who can handle a fight. There are beasts " + where + " that need thinning out. Could you do it?",
                "Forgive me for asking a stranger's help. Beasts are ruining the work " + where + ". I'd feel safer if someone strong cleared them."));
        c.say("I can pay " + reward + " ss when it's done.");
        String[] o = {"I'll do it.", "I can't, sorry.", "Not now."};
        int[] t = {Dialogue.BOLD, Dialogue.CAREFUL, Dialogue.COLD};
        int p = c.pick(o, t);
        if (p == 0) {
            Quest q = new Quest("Hunt Beast", true, 60, 0, 60, a);
            q.giver = c.npc.id + 1; q.reward = reward;
            q.label = "Thin out the beasts for " + c.npc.name + " " + c.npc.lastName;
            q.pin(60);
            Quest kept = c.me.addPlayerQuest(q);
            if (c.me.questAddWasDuplicate && kept != q) { kept.giver = q.giver; kept.reward = Math.max(kept.reward, reward); }
            pc.nextFavorWeek = GameDriver.time + COOLDOWN;
            c.say(c.any("Thank you. Truly. Come find me when it's done.", "You have my thanks. Be careful out there.", "Heaven watch over you. And thank you."));
            c.note("A new quest is in your Quest Log: " + q.label + " (" + PlayerControl.coord(a) + ").");
            c.rel(1);
        } else if (p == 1) {
            c.say(c.any("I understand. It was a lot to ask.", "No harm in asking. Another time, perhaps."));
            pc.nextFavorWeek = GameDriver.time + COOLDOWN / 2;
        } else {
            c.say("Of course. If you change your mind, I'll still be here.");
            pc.nextFavorWeek = GameDriver.time + COOLDOWN / 2;
        }
    }

    /** The quest is done: the one who asked pays what they promised (if they can and live) and likes the hero better. */
    static void thank(Character me, Quest q) {
        if (q == null || q.giver <= 0 || me == null) return;
        Character who = Character.charById(q.giver - 1);
        if (who == null || who.dead) { if (me.player > 0 && me.skipping <= 0) System.out.println("The one who asked is gone; the beasts are thinned all the same."); return; }
        long pay = Math.max(0, Math.min(q.reward, who.ss));
        if (pay > 0) { who.ss -= pay; me.ss += pay; me.convertCurrency(); }
        who.changeRelationship(me, 2);
        if (me.player > 0 && me.skipping <= 0)
            System.out.println(who.name + " " + who.lastName + " thanks you for the favor" + (pay > 0 ? " and pays " + pay + " ss" : "") + ".");
    }
}
