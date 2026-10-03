package Game;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The account of a fight. `Character.battle` opens one (only when a human-controlled or followed character is in the fight, so the 100,000 NPCs pay
 *  nothing), the hooks in flee/forbidden/winCheck/dead fill it while the fight runs, and `end` prints it as a short block (terminal and GUI log both read
 *  System.out), posts a "fight" notice and files it in the history and the chronicle. Easy wins over beasts collapse to one line.
 *  Also home of the non-lethal loss penalty: the loser of a feud fight that is not fatal pays 10% of their spirit stones to the winner.
 *  Everything runs on the game thread (battles are never fought on the worker threads). */
final class FightReport {
    static FightReport cur;                      // the fight being fought right now (null for NPC-only fights)
    static final List<String> history = Collections.synchronizedList(new ArrayList<>()); // the last reports, newest last (see `last(n)`)
    static final int MAX_HISTORY = 80;
    static int made = 0;                         // reports produced (tests and the playtest tool)
    static volatile String last = "";            // the newest report as text
    static int partyRound = 0, partySize = 0;    // set by partyBattleCheck / partyBeastHunt so each report says which round of a tag-team it is
    static final int PENALTY_PERCENT = 10;       // share of the loser's spirit stones paid after a non-lethal feud fight
    private static final BigInteger E6 = BigInteger.valueOf(1_000_000L), E12 = E6.multiply(E6), E18 = E12.multiply(E6);

    final Character a, b, hero;                  // a = the one who started it (this), b = the opponent; hero = the human among them (or null)
    FightReport prev;
    final int week;
    final String place;
    final boolean spar;
    final int round, party;
    int lvlA, lvlB, majA, majB, powA = -1, powB = -1, winA = -1, arrayB = 0, enchA = 0, enchB = 0;
    boolean deadly, oddsKnown;
    int relA, relB, killsA, killsB;
    boolean ghostA, ghostB;
    final ArrayList<String> events = new ArrayList<>();
    Character winner, fleer;
    boolean spared, injured, beastEscape;
    String lootText, penaltyText;
    long penaltyAmount = 0;

    private FightReport(Character a, Character b) {
        this.a = a; this.b = b;
        hero = a.player == 1 ? a : b.player == 1 ? b : null;
        week = GameDriver.time;
        place = (a.location != null ? a.location.name : b.location != null ? b.location.name : "?");
        spar = a.sparring || b.sparring;
        round = partyRound; party = partySize;
        lvlA = a.lvl; lvlB = b.lvl; majA = a.majorLvl; majB = b.majorLvl;
        killsA = a.kills; killsB = b.kills;
        ghostA = a.isGhostForm; ghostB = b.isGhostForm;
        relA = relOf(a, b); relB = relOf(b, a);
    }

    private static int relOf(Character x, Character y) { Relationship r = x.relationshipList == null ? null : x.relationshipList.contains(y.id); return r == null ? 0 : r.value; }
    private static boolean wild(Character c) { return c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster; }

    // ------------------------------------------------------------------ lifecycle (called from Character.battle)
    static FightReport begin(Character a, Character b) {
        if (a == null || b == null || (a.player <= 0 && b.player <= 0)) { return null; }
        FightReport r = new FightReport(a, b);
        r.prev = cur;
        cur = r;
        return r;
    }

    static void end(FightReport r) {
        if (r == null) return;
        cur = r.prev;
        try { r.finish(); } catch (RuntimeException e) { System.out.println("[Fight] (report failed: " + e + ")"); }
    }

    // ------------------------------------------------------------------ hooks
    static void odds(Character self, Character opp, int winChance, boolean lethal) {
        FightReport r = cur; if (r == null || r.a != self) return;
        r.powA = self.tempLvl; r.powB = opp.tempLvl; r.winA = Math.max(0, Math.min(100, winChance)); r.deadly = lethal; r.oddsKnown = true;
        r.enchA = self.equippedWeapon != null ? self.equippedWeapon.enchantBonus : 0;
        r.enchB = opp.equippedWeapon != null ? opp.equippedWeapon.enchantBonus : 0;
    }
    static void arrayBoost(int boost) { FightReport r = cur; if (r != null) r.arrayB = boost; }
    static void note(String text) { FightReport r = cur; if (r != null) r.events.add(text); }

    /** `who` tried (or, for a human, chose not) to flee `from`. */
    static Character lastFleer;                  // whoever last got away from a fight (even in NPC-only fights: SectPolitics reads it to tell who fled an ambush)
    static void fleeAttempt(Character who, Character from, boolean ok) {
        if (ok) lastFleer = who;
        FightReport r = cur; if (r == null) return;
        if (ok) { r.fleer = who; r.events.add(r.cap(who) + " fled from " + r.who(from) + ": the escape worked, the fight is over."); }
        else r.events.add(r.cap(who) + " tried to flee from " + r.who(from) + " but " + (r.hero == who ? "were" : "was") + " caught.");
    }
    static void fledNot(Character who) { FightReport r = cur; if (r != null) r.events.add(r.cap(who) + " chose to stand and fight rather than flee."); }

    /** kind: 0 declined by a human, 1 used and survived, 2 used and burned out the lifespan. */
    static void forbidden(Character who, int kind) {
        FightReport r = cur; if (r == null) return;
        if (kind == 0) r.events.add(r.cap(who) + " declined the Forbidden Technique.");
        else if (kind == 1) r.events.add(r.cap(who) + " used the Forbidden Technique (power +" + (int) Math.sqrt(who.potential) + ", lifespan burned).");
        else r.events.add(r.cap(who) + " used the Forbidden Technique and burned out " + (r.hero == who ? "your" : "their") + " lifespan.");
    }

    static void won(Character w, Character l) { FightReport r = cur; if (r != null) r.winner = w; }
    static boolean active() { return cur != null; }
    static void spared(Character winnerC, Character loser) { FightReport r = cur; if (r != null) r.spared = true; }
    static void injured(Character loser) { FightReport r = cur; if (r != null) r.injured = true; }
    static void beastEscape(Character loser) { FightReport r = cur; if (r != null) { r.beastEscape = true; r.injured = true; } }

    /** Called by dead(killer) just before the loser's belongings change hands. */
    static void loot(Character dead, Character killer) {
        FightReport r = cur; if (r == null || (dead != r.a && dead != r.b)) return;
        int items = dead.backpack.size() + dead.pillBag.size() + dead.materialBag.size() + dead.arrayBag.size();
        Item best = null;
        for (Item it : dead.backpack) if (it != null && it.itemType != null && (best == null || Character.itemPrice(it) > Character.itemPrice(best))) best = it;
        StringBuilder sb = new StringBuilder();
        if (items > 0) sb.append(items).append(items == 1 ? " item stack" : " item stacks").append(best != null ? " (best: " + best.name + ", Lv." + best.lvl + ")" : "");
        String m = money(dead.ss, dead.sc, dead.dc, dead.ij);
        if (!m.isEmpty()) sb.append(sb.length() > 0 ? ", " : "").append(m);
        r.lootText = sb.length() == 0 ? "nothing of value" : sb.toString();
    }

    /** A fight between two cultivators was won on a non-lethal result (the loser lives, hurt or spared): feuds cost the loser 10% of their wealth. */
    static void penalty(Character w, Character l) {
        if (w == null || l == null || w == l || w.dead || l.dead || wild(w) || wild(l) || w.age < 8 || l.age < 8 || w.sparring || l.sparring) return;
        if (!feud(w, l)) return;
        long moved = moveWealth(l, w, PENALTY_PERCENT);
        FightReport r = cur;
        if (r != null) {
            r.penaltyAmount = moved;
            r.penaltyText = moved > 0 ? r.cap(l) + " paid " + moneyOf(moved) + " to " + r.who(w) + " (the loser of a feud fight forfeits " + PENALTY_PERCENT + "% of their spirit stones)."
                    : r.cap(l) + (r.hero == l ? " have" : " has") + " nothing worth taking.";
        } else if (moved > 0 && l.player == 1) System.out.println("You paid " + moneyOf(moved) + " to " + w.name + " " + w.lastName + " for losing the fight.");
    }

    /** Negative relationship either way, or a Battle Rival / Arch-Enemy / Dislike / Revenge / Bounty Hunt quest aimed at the other. */
    static boolean feud(Character w, Character l) {
        if (relOf(w, l) <= -1 || relOf(l, w) <= -1) return true;
        return questAgainst(w, l) || questAgainst(l, w);
    }
    private static boolean questAgainst(Character c, Character t) {
        Quest q = c.currentQuest;
        if (q == null || q.targetCharacter != t.id || q.category != Quest.QuestCategory.Character) return false;
        switch (q.name) { case "Battle Rival": case "Battle Arch-Enemy": case "Battle Dislike": case "Revenge": case "Bounty Hunt": return true; default: return false; }
    }

    /** A tag-team assault begins (several fighters take turns against one opponent): one line for the humans involved; each round then gets its own report. */
    static void partyStart(List<Character> party, Character opponent) {
        if (party == null || party.size() < 2 || opponent == null) return;
        boolean human = opponent.player == 1;
        for (Character c : party) if (c != null && c.player == 1) human = true;
        if (!human) return;
        StringBuilder sb = new StringBuilder("[Fight] Tag-team assault: ");
        for (int i = 0; i < party.size(); i++) { Character c = party.get(i); sb.append(i > 0 ? (i == party.size() - 1 ? " and " : ", ") : "").append(c.player == 1 ? "you" : c.name + " " + c.lastName); }
        sb.append(opponent.player == 1 ? " take on you" : " take on " + opponent.name + " " + opponent.lastName).append(", one after the other (the target tires each round).");
        System.out.println(sb);
        remember(sb.toString());
    }

    /** One step of an arrays-versus-attacker clash; humans standing in the tile and the attacker (if human) hear of it. */
    static void arrayAttempt(Character attacker, Area area, int arrayLvl, boolean broken, boolean died) {
        if (attacker == null || area == null) return;
        boolean mine = attacker.player > 0;
        boolean here = false;
        for (Character c : area.characters) if (c != null && c.player == 1 && c != attacker) { here = true; break; }
        if (!mine && !here) return;
        String line = "[Fight] " + (attacker.player == 1 ? "You" : attacker.name + " " + attacker.lastName) + (broken ? " broke" : " failed to break") + " the Lv." + arrayLvl + " array guarding " + area.name
                + (died ? " and was killed by the backlash." : broken ? "." : ".");
        if (attacker.player == 1 || attacker.skipping <= 0) System.out.println(line);
        remember(line);
    }

    // ------------------------------------------------------------------ text
    private String who(Character c) { return c == hero ? "you" : c.name + " " + c.lastName; }
    private String cap(Character c) { return c == hero ? "You" : c.name + " " + c.lastName; }
    private String tag(Character c, boolean first) {
        return (c == hero ? (first ? "You" : "you") : c.name + " " + c.lastName) + " (" + (wild(c) ? (c.trait == Character.Traits.Beast ? "beast" : "monster") + " Lv." + c.lvl : "Lv." + c.lvl + ", Major " + c.majorLvl + ", " + c.affiliation) + ")";
    }

    static String money(long ss, long sc, long dc, long ij) {
        StringBuilder sb = new StringBuilder();
        if (ij > 0) sb.append(ij).append(" ij ");
        if (dc > 0) sb.append(dc).append(" dc ");
        if (sc > 0) sb.append(sc).append(" sc ");
        if (ss > 0) sb.append(ss).append(" ss ");
        return sb.toString().trim();
    }
    static String moneyOf(long ssEquivalent) {
        if (ssEquivalent >= Long.MAX_VALUE) return "a fortune";
        long ij = ssEquivalent / 1_000_000_000_000_000_000L, r1 = ssEquivalent % 1_000_000_000_000_000_000L;
        long dc = r1 / 1_000_000_000_000L, r2 = r1 % 1_000_000_000_000L, sc = r2 / 1_000_000L, ss = r2 % 1_000_000L;
        String m = money(ss, sc, dc, ij);
        return m.isEmpty() ? "0 ss" : m;
    }
    private static String relName(int v) {
        switch (v) {
            case -4: return "Wanted"; case -3: return "Arch-enemy"; case -2: return "Rival"; case -1: return "Dislike";
            case 0: return "stranger"; case 1: return "Friend"; case 2: return "Close friend"; default: return "Best friend";
        }
    }

    private boolean compact() {
        Character foe = hero == a ? b : a;
        if (hero == null || !wild(foe) || fleer != null || !events.isEmpty() || hero.dead) return false; // the foe dying is the ordinary end of a hunt: one line; the hero dying deserves the full report
        return !injured && !spared;
    }

    private String headline() {
        Character foe = hero != null ? (hero == a ? b : a) : b;
        String fn = foe.name + " " + foe.lastName;
        String res;
        if (fleer != null) res = fleer == hero ? "you escaped" : hero != null ? fn + " escaped" : who(fleer) + " escaped";
        else if (winner == null) res = "no winner";
        else if (hero == null) res = who(winner) + " won";
        else res = winner == hero ? "you won" : "you lost";
        Character loser = winner == null ? null : winner == a ? b : a;
        if (loser != null) {
            if (loser.dead) res += loser == hero ? ", you were killed" : ", " + (hero == null ? "the loser" : "they") + " died";
            else if (spared) res += ", spared";
            else if (injured) res += ", left alive and injured";
        }
        if (penaltyAmount > 0) res += (winner == hero ? ", +" : ", -") + moneyOf(penaltyAmount);
        return "Fight " + (spar ? "(spar) " : "") + "vs " + fn + ": " + res + ".";
    }

    /** The multi-line account (also what the history keeps). */
    String text() {
        Character loser = winner == null ? null : winner == a ? b : a;
        StringBuilder sb = new StringBuilder();
        sb.append("==== FIGHT REPORT ====  Year ").append(week / 52).append(", wk ").append(week % 52 + 1).append(", ").append(place);
        if (party > 1) sb.append("  (round ").append(round).append(" of ").append(party).append(")");
        sb.append('\n');
        sb.append(spar ? "Friendly spar: " : "").append(tag(a, true)).append(spar ? " and " : " attacked ").append(tag(b, false)).append('\n');
        if (oddsKnown) {
            int mine = hero == b ? 100 - winA : winA;
            Character me = hero != null ? hero : a, other = me == a ? b : a;
            int pm = me == a ? powA : powB, po = me == a ? powB : powA;
            sb.append("  Power ").append(me == hero ? "you " : me.name + " ").append(pm).append(" vs ").append(other.name).append(' ').append(po);
            if (arrayB > 0) sb.append(" (the defender's array adds +").append(arrayB).append(")");
            if (enchA > 0) sb.append(" (").append(a == hero ? "your" : a.name + "'s").append(" weapon enchant +").append(enchA).append(")");
            if (enchB > 0) sb.append(" (").append(b == hero ? "your" : b.name + "'s").append(" weapon enchant +").append(enchB).append(")");
            sb.append(".  ").append(me == hero ? "Your" : me.name + "'s").append(" chance to win ~").append(mine).append("%.  Lethal: ").append(deadly ? "yes" : "no")
                    .append(spar ? "" : " (relationship " + relName(relA) + "/" + relName(relB) + ")").append('\n');
        }
        for (String e : events) sb.append("  - ").append(e).append('\n');
        if (fleer != null) sb.append("  RESULT: no winner; ").append(fleer == hero ? "you" : who(fleer)).append(" escaped, nobody was hurt.\n");
        else if (winner == null && !(a.dead || b.dead)) sb.append("  RESULT: the fight ended with no clear winner.\n");
        else {
            Character w = winner != null ? winner : (b.dead && !a.dead ? a : a.dead && !b.dead ? b : null);
            Character l = w == null ? null : w == a ? b : a;
            if (w == null) sb.append("  RESULT: both fighters fell.\n");
            else sb.append("  RESULT: ").append(hero == null ? who(w) + " won" : w == hero ? "YOU WON" : "YOU LOST (" + who(w) + " won)").append(".\n");
            if (l != null) sb.append("  Fate: ").append(fate(w, l)).append('\n');
        }
        if (lootText != null) {
            Character w = winner != null ? winner : (b.dead && !a.dead ? a : b);
            sb.append("  Loot: ").append(cap(w)).append(w == hero ? " take " : " took ").append(lootText).append(".\n");
        }
        if (penaltyText != null) sb.append("  Penalty: ").append(penaltyText).append('\n');
        Character winN = winner;
        if (winN != null && winN == hero && (a.dead || b.dead) && (hero.kills > (hero == a ? killsA : killsB))) sb.append("  Kills: ").append(hero.kills).append(" in total.\n");
        if (!a.dead && !b.dead && !spar && fleer == null) {
            int ra = relOf(a, b);
            if (ra != relA) sb.append("  Relationship: ").append(relName(relA)).append(" -> ").append(relName(ra)).append(".\n");
        }
        if (spar && winner != null) sb.append("  Both of you practised and grew closer.\n");
        sb.append("========================");
        return sb.toString();
    }

    private String fate(Character w, Character l) {
        String ln = cap(l);
        if (l.dead) {
            String how = l.deathCause != null && l.deathCause.contains("Forbidden") ? " (their lifespan burned out in the Forbidden Technique)" : "";
            if (wild(l)) return ln + " " + (l == hero ? "were" : "was") + " slain" + (w == hero ? " by you" : "") + ".";
            return l == hero ? "You were KILLED by " + who(w) + how + "." : (w == hero ? ln + " was KILLED by you" : ln + " was killed") + how + ".";
        }
        if (spared) return w == hero ? "You spared " + who(l) + "." : ln + " was spared.";
        if (beastEscape) return l == hero ? "You got away from the beast, injured (cultivation down 30%)." : ln + " got away injured.";
        if (injured) return (l == hero ? "You were left alive but injured (cultivation down 30% until your next level-up)." : ln + " was left alive, injured (cultivation down 30%).");
        if (l.isGhostForm && !(l == a ? ghostA : ghostB)) return ln + " slipped into ghost form instead of dying.";
        return ln + " survived (a rebirth or ghost form saved " + (l == hero ? "you" : "them") + ").";
    }

    private void finish() {
        // spirit-stone loot recorded by penalty(); winner may still be unset on early kills: loot() text is built by dead(killer)
        made++;
        String full = compact() ? compactLine() : text();
        last = full;
        history.add(full);
        synchronized (history) { while (history.size() > MAX_HISTORY) history.remove(0); }
        boolean show = hero != null || (a.player == 2 && a.skipping <= 0) || (b.player == 2 && b.skipping <= 0);
        if (show) for (String line : full.split("\n")) System.out.println(line);
        if (hero != null && !compact()) {
            Character foe = hero == a ? b : a;
            Notifications.postQuiet("fight", headline(), foe.id, hero.id);
            if (!spar && !wild(foe)) Chronicle.add("fight", (hero.name + " " + hero.lastName) + ": " + headline(), hero.id, foe.id);
        }
    }

    private String compactLine() {
        Character foe = hero == a ? b : a;
        int mine = !oddsKnown ? -1 : hero == b ? 100 - winA : winA;
        int pm = hero == a ? powA : powB, po = hero == a ? powB : powA;
        return "[Fight] " + (winner == hero ? "You defeated " : "You were beaten by ") + (foe.trait == Character.Traits.Beast ? "the beast " : "the monster ") + foe.name + " " + foe.lastName
                + " (Lv." + foe.lvl + "), power " + pm + " vs " + po + (mine >= 0 ? ", " + mine + "% to win" : "") + (lootText != null ? ", loot: " + lootText : "") + ".";
    }

    /** The last `n` reports (oldest first) as one text. */
    static String lastReports(int n) {
        StringBuilder sb = new StringBuilder();
        synchronized (history) { for (int i = Math.max(0, history.size() - n); i < history.size(); i++) sb.append(history.get(i)).append('\n'); }
        return sb.length() == 0 ? "No fights yet.\n" : sb.toString();
    }

    private static void remember(String line) { history.add(line); synchronized (history) { while (history.size() > MAX_HISTORY) history.remove(0); } last = line; }

    // ------------------------------------------------------------------ wealth moves (exact, overflow-safe)
    private static BigInteger big(long v) { return BigInteger.valueOf(Math.max(0L, v)); }
    static BigInteger totalWealth(Character c) { return big(c.ss).add(big(c.sc).multiply(E6)).add(big(c.dc).multiply(E12)).add(big(c.ij).multiply(E18)); }

    private static long sat(long x, long y) { long s = x + y; return s < 0 ? Long.MAX_VALUE : s; }

    private static void setWealth(Character c, BigInteger v) {
        BigInteger[] q = v.divideAndRemainder(E18);
        c.ij = q[0].min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
        q = q[1].divideAndRemainder(E12); c.dc = q[0].longValue();
        q = q[1].divideAndRemainder(E6); c.sc = q[0].longValue(); c.ss = q[1].longValue();
    }

    private static void addWealth(Character c, BigInteger v) {
        BigInteger[] q = v.divideAndRemainder(E18);
        long ij = q[0].min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
        q = q[1].divideAndRemainder(E12); long dc = q[0].longValue();
        q = q[1].divideAndRemainder(E6);
        c.ss = sat(c.ss, q[1].longValue()); c.sc = sat(c.sc, q[0].longValue()); c.dc = sat(c.dc, dc); c.ij = sat(c.ij, ij);
        c.convertCurrency();
    }

    /** Moves `percent` of `from`'s total wealth (all four currency tiers, counted in spirit stones) to `to`; the tiers of both are renormalised.
     *  Returns what moved, in spirit stones (saturated at Long.MAX_VALUE). Never takes more than they own, never goes negative. */
    static long moveWealth(Character from, Character to, int percent) {
        if (from == null || to == null || from == to || percent <= 0) return 0;
        BigInteger total = totalWealth(from);
        BigInteger pay = total.multiply(BigInteger.valueOf(Math.min(100, percent))).divide(BigInteger.valueOf(100));
        if (pay.signum() <= 0) return 0;
        setWealth(from, total.subtract(pay));
        addWealth(to, pay);
        return pay.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0 ? Long.MAX_VALUE : pay.longValue();
    }
}
