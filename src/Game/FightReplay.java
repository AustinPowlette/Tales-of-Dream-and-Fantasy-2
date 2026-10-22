package Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** The picture of a fight: a short bout of exchanges for the stick-figure window (`GuiFightView`). A fight is settled by one roll, so the exchanges are a dramatisation that is built to agree with what happened: the winner
 *  of the roll wins the bout, the loser ends at the health the real result gives (dead, spared, injured, fled), a favourite wins with health to spare and an underdog barely, the techniques are the fighters' own martial
 *  techniques, and the elements show where they changed the odds. It is built from a `FightReport` (a fight a person who plays was in) or from the two fighters of a tournament bout, kept in memory only,
 *  and costs nothing for the NPC fights nobody sees. */
final class FightReplay {
    static final int TALK = 0, STRIKE = 1, TECH = 2, BLOCK = 3, FORBID = 4, ARRAY = 5, FLEE = 6, FINISH = 7;

    static final class Beat {
        final int actor, kind, hpA, hpB;   // actor: 0 = the first fighter, 1 = the second (-1: nobody); health (0-100) after the beat
        final String text;
        final boolean boost;               // an element gave the actor the upper hand in this exchange
        final String tech;                 // the technique this beat shows (null: a plain blow): drawn from its name and type by FightFx
        final boolean movement;            // the technique is a movement technique (a dodge, an escape), shown as the fighter's own motion
        final int techElem;                // the technique's element (the colour of its effect)
        Beat(int actor, int kind, String text, int hpA, int hpB, boolean boost) { this(actor, kind, text, hpA, hpB, boost, null, false, 0); }
        Beat(int actor, int kind, String text, int hpA, int hpB, boolean boost, String tech, boolean movement, int techElem) { this.actor = actor; this.kind = kind; this.text = text; this.hpA = hpA; this.hpB = hpB; this.boost = boost; this.tech = tech; this.movement = movement; this.techElem = techElem; }
    }

    int tourney = 0;                   // 0 an ordinary fight, 1 a tournament bout (a crowd, banners, drums), 2 a bout of the Garden's Variety Tournament (a night sky, falling platforms, the Garden's own music): set by the caller; not saved
    final String title, place, nameA, nameB, techA, techB, moveA, moveB, resultLine, reportText;
    final int tElemA, tElemB, mElemA, mElemB;   // the elements of the martial and the movement techniques (the colour of their effects)
    final int elemA, elemB, lvlA, lvlB, powA, powB, winA, swing, week;
    final int kindA, kindB;            // 0 a person (a stick man), 1 a wild beast (a stick animal), 2 a Monster (a bigger one with spikes)
    Area area;                         // where it was fought (the window's background picture); not saved
    final boolean aWon, bWon, killed, spared, injured, fledA, fledB, spar;
    final List<Beat> beats = new ArrayList<>();

    private FightReplay(String title, String place, Character a, Character b, int powA, int powB, int winA, Character winner, Character fleer, boolean killed, boolean spared, boolean injured, boolean spar,
                        boolean forbiddenByA, boolean forbiddenByB, int array, String resultLine, String reportText) {
        this.title = title; this.place = place; this.week = GameDriver.time;
        nameA = a.name + " " + a.lastName; nameB = b.name + " " + b.lastName;
        elemA = Elements.of(a); elemB = Elements.of(b);
        kindA = kindOf(a); kindB = kindOf(b);
        lvlA = a.lvl; lvlB = b.lvl; this.powA = powA; this.powB = powB; this.winA = winA;
        techA = techOf(a); techB = techOf(b);
        moveA = moveOf(a); moveB = moveOf(b);
        tElemA = techElemOf(a.martialTechnique, elemA); tElemB = techElemOf(b.martialTechnique, elemB);
        mElemA = techElemOf(a.movementTechnique, elemA); mElemB = techElemOf(b.movementTechnique, elemB);
        swing = Elements.swing(a, b);
        aWon = winner == a; bWon = winner == b;
        this.killed = killed; this.spared = spared; this.injured = injured; this.spar = spar;
        fledA = fleer == a; fledB = fleer == b;
        this.resultLine = resultLine; this.reportText = reportText;
        build(a, b, forbiddenByA, forbiddenByB, array);
    }

    private static int kindOf(Character c) { return c.trait == Character.Traits.Beast ? 1 : c.trait == Character.Traits.Monster ? 2 : 0; }

    private static String techOf(Character c) {
        Technique t = c.martialTechnique;
        if (t != null && t.element() >= 0 && !"Default Technique".equals(t.name)) return t.name;
        return Elements.pathName(c).replaceFirst("^(Path|Way|Dao|Art) of the ", "") + " Strike";
    }

    /** The name of the fighter's movement technique, or a plain one by its path when it has none. */
    private static String moveOf(Character c) {
        Technique t = c.movementTechnique;
        if (t != null && !"Default Technique".equals(t.name)) return t.name;
        return Elements.pathName(c).replaceFirst("^(Path|Way|Dao|Art) of the ", "") + " Step";
    }

    private static int techElemOf(Technique t, int fallback) { return t != null && t.element() >= 0 ? t.element() : fallback; }

    /** The picture of a fight a person who plays was in (called when its report is done). */
    static FightReplay of(FightReport r) {
        boolean wild = r.a.trait == Character.Traits.Beast || r.a.trait == Character.Traits.Monster || r.b.trait == Character.Traits.Beast || r.b.trait == Character.Traits.Monster;
        Character winner = r.winner != null ? r.winner : (r.b.dead && !r.a.dead ? r.a : r.a.dead && !r.b.dead ? r.b : null);
        Character loser = winner == null ? null : winner == r.a ? r.b : r.a;
        boolean killed = loser != null && loser.dead;
        String title = (r.spar ? "Friendly spar" : wild ? "Hunt" : "Fight") + " at " + r.place;
        String res = winner == null ? (r.fleer != null ? who(r.fleer, r) + " escaped." : "No clear winner.") : who(winner, r) + " won" + (killed ? ", and " + who(loser, r) + " " + (loser == r.hero ? "were" : "was") + " killed." : r.spared ? " and spared " + who(loser, r) + "." : r.injured ? "; " + who(loser, r) + " left injured." : ".");
        boolean fa = false, fb = false;
        for (String e : r.events) if (e.contains("used the Forbidden Technique")) { if (e.startsWith(r.a.name) || (r.hero == r.a && e.startsWith("You"))) fa = true; else fb = true; }
        int pa = r.powA >= 0 ? r.powA : r.a.tempLvl, pb = r.powB >= 0 ? r.powB : r.b.tempLvl, win = r.winA >= 0 ? r.winA : 50;
        FightReplay f = new FightReplay(title, r.place, r.a, r.b, pa, pb, win, winner, r.fleer, killed, r.spared, r.injured, r.spar, fa, fb, r.arrayB, res, r.text());
        f.area = r.a.location != null ? r.a.location : r.b.location;
        return f;
    }

    /** One bout of a tournament (nobody dies in a bout). */
    static FightReplay bout(Character a, Character b, int winA, Character winner, String title, Area where) {
        FightReplay f = bout(a, b, winA, winner, title, where == null ? "?" : where.name);
        f.area = where;
        return f;
    }

    static FightReplay bout(Character a, Character b, int winA, Character winner, String title, String place) {
        a.tempLvlCheck(); b.tempLvlCheck();
        String res = winner.name + " " + winner.lastName + " won the bout.";
        return new FightReplay(title, place, a, b, a.tempLvl, b.tempLvl, winA, winner, null, false, false, false, false, false, false, 0, res,
                title + "\n" + a.name + " " + a.lastName + " (Lv." + a.lvl + ") against " + b.name + " " + b.lastName + " (Lv." + b.lvl + "): " + res);
    }

    private static String who(Character c, FightReport r) { return c == r.hero ? "you" : c.name + " " + c.lastName; }

    // ---------------------------------------------------------------- the choreography
    private static final String[] VERBS = {"lunges in", "drives a palm forward", "feints and cuts low", "sweeps a heavy strike", "spins and kicks", "thrusts at the ribs", "presses the attack", "hammers a fist down"};
    private static final String[] BLOCKS = {"turns the blow aside", "slips back out of reach", "blocks with a forearm", "rolls away from it"};

    private void build(Character a, Character b, boolean forbA, boolean forbB, int array) {
        Random r = new Random(week * 31L + a.id * 131L + b.id * 7L + 11);
        int winnerSide = (fledA || fledB) ? (fledA ? 1 : 0) : aWon ? 0 : bWon ? 1 : -1;
        double pw = winnerSide < 0 ? 0.5 : winnerSide == 0 ? winA / 100.0 : 1 - winA / 100.0;
        pw = Math.max(0.05, Math.min(0.95, pw));
        int winnerEnd = winnerSide < 0 ? 40 + r.nextInt(30) : (int) Math.max(8, Math.min(92, 18 + 72 * pw + r.nextInt(25) - 12));
        int loserEnd = winnerSide < 0 ? 40 + r.nextInt(30) : killed ? 0 : (fledA || fledB) ? 30 + r.nextInt(25) : (spared || injured) ? 6 + r.nextInt(10) : 4 + r.nextInt(8);
        int n = 6 + r.nextInt(4);
        // who strikes in each exchange (the favourite more often; the last blow is the winner's)
        int[] actor = new int[n];
        int wSide = winnerSide < 0 ? r.nextInt(2) : winnerSide;
        double pWinnerActs = 0.5 + (pw - 0.5) * 0.5;
        int wCount = 0, lCount = 0;
        for (int i = 0; i < n; i++) { actor[i] = r.nextDouble() < pWinnerActs ? wSide : 1 - wSide; if (actor[i] == wSide) wCount++; else lCount++; }
        if (wCount < 2) { for (int i = 0; i < n && wCount < 2; i++) if (actor[i] != wSide) { actor[i] = wSide; wCount++; lCount--; } }
        if (lCount < 2) { for (int i = n - 2; i >= 0 && lCount < 2; i--) if (actor[i] == wSide) { actor[i] = 1 - wSide; lCount++; wCount--; } }
        actor[n - 1] = wSide;
        // the damage each side takes, spread over the other side's blows (the last blow lands hardest)
        int dmgToLoser = 100 - loserEnd, dmgToWinner = 100 - winnerEnd;
        int[] dmg = new int[n];
        int wBlows = 0, lBlows = 0; for (int i = 0; i < n; i++) if (actor[i] == wSide) wBlows++; else lBlows++;
        double[] wt = new double[n]; double wSum = 0, lSum = 0;
        for (int i = 0; i < n; i++) { wt[i] = 0.5 + r.nextDouble() + (i == n - 1 ? 1.2 : 0); if (actor[i] == wSide) wSum += wt[i]; else lSum += wt[i]; }
        int giveW = 0, giveL = 0;
        for (int i = 0; i < n; i++) {
            if (actor[i] == wSide) { dmg[i] = (int) Math.round(dmgToLoser * wt[i] / wSum); giveW += dmg[i]; }
            else { dmg[i] = (int) Math.round(dmgToWinner * wt[i] / lSum); giveL += dmg[i]; }
        }
        // rounding: put what is left on the last blow of each side
        for (int i = n - 1; i >= 0; i--) if (actor[i] == wSide) { dmg[i] += dmgToLoser - giveW; break; }
        for (int i = n - 1; i >= 0; i--) if (actor[i] != wSide) { dmg[i] += dmgToWinner - giveL; break; }
        if (winnerSide < 0) { for (int i = 0; i < n; i++) actor[i] = i % 2; }

        int hpA = 100, hpB = 100;
        StringBuilder intro = new StringBuilder(nameA + " (Lv." + lvlA + ", " + Elements.name(elemA) + ") faces " + nameB + " (Lv." + lvlB + ", " + Elements.name(elemB) + "). Power " + powA + " against " + powB + ", " + winA + "% for " + nameA.split(" ")[0] + ".");
        beats.add(new Beat(-1, TALK, intro.toString(), 100, 100, false));
        if (swing != 0) {
            String rel = Elements.relation(elemA, elemB);
            beats.add(new Beat(-1, TALK, "The elements: " + Elements.name(elemA) + " against " + Elements.name(elemB) + (rel.isEmpty() ? "" : " (" + rel + ")") + ": " + (swing > 0 ? nameA.split(" ")[0] + " gains " : nameB.split(" ")[0] + " gains ") + Math.abs(swing) + " points.", 100, 100, false));
        }
        if (array > 0) beats.add(new Beat(-1, ARRAY, "An array guards this place. It lends the defender +" + array + " power.", 100, 100, false));
        int forbAt = (forbA || forbB) ? n / 2 : -1;
        for (int i = 0; i < n; i++) {
            int act = winnerSide < 0 ? i % 2 : actor[i];
            int d = Math.max(0, dmg[i]);
            String an = act == 0 ? nameA.split(" ")[0] : nameB.split(" ")[0], dn = act == 0 ? nameB.split(" ")[0] : nameA.split(" ")[0];
            String tech = act == 0 ? techA : techB;
            int ae = act == 0 ? elemA : elemB, de = act == 0 ? elemB : elemA;
            boolean advantage = Elements.overcomes(ae, de);
            if (act == 0) hpB = Math.max(0, hpB - d); else hpA = Math.max(0, hpA - d);
            String moveTech = act == 0 ? moveB : moveA;     // the one who is struck slips away with their movement technique
            int moveElem = act == 0 ? mElemB : mElemA;
            if (i == forbAt && (forbA || forbB)) {
                String fn = (forbA ? nameA : nameB).split(" ")[0];
                int fa = forbA ? 0 : 1;
                beats.add(new Beat(fa, FORBID, fn + " reaches for the Forbidden Technique, burning years of life for one terrible blow!", hpA, hpB, false));
            }
            if (i == n - 1) {
                String end = killed ? "The last blow ends it. " + dn + " falls." : (spared || injured) ? "The last blow lands. " + dn + " goes down and stays down." : (fledA || fledB) ? "" : dn + " is beaten.";
                if (fledA || fledB) {
                    beats.add(new Beat(act, STRIKE, an + " drives " + dn + " back with a hard blow.", hpA, hpB, advantage));
                    String fn = fledA ? nameA.split(" ")[0] : nameB.split(" ")[0];
                    beats.add(new Beat(fledA ? 0 : 1, FLEE, fn + " breaks away and runs! The fight is over.", hpA, hpB, false, fledA ? moveA : moveB, true, fledA ? mElemA : mElemB));
                } else if (winnerSide < 0) {
                    beats.add(new Beat(act, STRIKE, an + " " + VERBS[r.nextInt(VERBS.length)] + ", but nothing settles it.", hpA, hpB, false));
                } else {
                    beats.add(new Beat(act, FINISH, an + " uses " + tech + ". " + end, hpA, hpB, advantage, tech, false, act == 0 ? tElemA : tElemB));
                }
                break;
            }
            if (r.nextInt(100) < 55) {
                String line = an + " uses " + tech + "!" + (advantage ? " " + Elements.name(ae) + " overpowers " + Elements.name(de) + "!" : Elements.overcomes(de, ae) ? " " + Elements.name(de) + " holds " + Elements.name(ae) + " back." : "");
                beats.add(new Beat(act, TECH, line, hpA, hpB, advantage, tech, false, act == 0 ? tElemA : tElemB));
            } else if (d <= 3 && r.nextInt(100) < 70) {
                beats.add(new Beat(act, BLOCK, an + " strikes, and " + dn + " " + BLOCKS[r.nextInt(BLOCKS.length)] + " (" + moveTech + ").", hpA, hpB, false, moveTech, true, moveElem));
            } else {
                beats.add(new Beat(act, STRIKE, an + " " + VERBS[r.nextInt(VERBS.length)] + ".", hpA, hpB, false));
            }
        }
        // the last beat's health is exactly the result's
        Beat last = beats.get(beats.size() - 1);
        if (winnerSide >= 0 && !(fledA || fledB)) {
            int fa = winnerSide == 0 ? winnerEnd : loserEnd, fb = winnerSide == 0 ? loserEnd : winnerEnd;
            beats.set(beats.size() - 1, new Beat(last.actor, last.kind, last.text, fa, fb, last.boost));
        }
    }

    /** The health of the fighters (0-100) at the end. */
    int endA() { return beats.get(beats.size() - 1).hpA; }
    int endB() { return beats.get(beats.size() - 1).hpB; }
}
