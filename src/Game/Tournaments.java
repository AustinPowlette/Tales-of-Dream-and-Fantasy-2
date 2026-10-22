package Game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** What a person who plays sees of a tournament (and of the Convocation of the First Gate). A tournament is settled in one go, bout by bout, when its timer runs out; this keeps the bouts a person has a reason to
 *  see and shows them: an entrant gets his own bouts through the fight window and then the result; a spectator (a person who plays and stands in the tile without having entered) gets the last rounds (all of the bouts
 *  when there are eight or fewer), and may bet on each before it is shown. The bets are paid out at the odds the two fighters' chances make, less 5% for the house. */
final class Tournaments {
    private Tournaments() { }

    static final int HOUSE_PERCENT = 5;
    static final int[] STAKE_PERCENT = {2, 10, 25};       // small, medium, large: a share of the spectator's wealth

    /** One settled bout. `wcA` is the first fighter's chance to win (percent). */
    static final class Bout {
        final Character a, b, winner; final int round, wcA;
        Bout(Character a, Character b, int wcA, Character winner, int round) { this.a = a; this.b = b; this.wcA = wcA; this.winner = winner; this.round = round; }
    }

    /** The bouts of a tournament, kept only when somebody who plays could want to see them. */
    static final class Record {
        final List<Bout> all = new ArrayList<>();
        final boolean keep;
        Record(boolean keep) { this.keep = keep; }
        void add(Character a, Character b, int wcA, Character winner, int round) { if (keep) all.add(new Bout(a, b, wcA, winner, round)); }
    }

    /** True when a person who plays by hand will want the bouts: an entrant, or a spectator in the tile. */
    static boolean interested(Area where, Collection<Character> entrants) {
        for (Character c : entrants) if (c != null && c.player == 1) return true;
        return !spectators(where, entrants).isEmpty();
    }

    /** Who has taken a seat (done the Spectate Tournament quest) and where; kept in memory only, a loaded game asks to spectate again. */
    private static final java.util.Map<Character, Area> seated = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<Character, Area>());

    /** The quest Spectate Tournament: this person watches the tournament held in the tile. */
    static void takeSeat(Character c, Area where) { if (c != null && where != null) seated.put(c, where); }

    /** The people who play by hand, stand in the tile, have not entered and have taken a seat (the Spectate Tournament quest). */
    static List<Character> spectators(Area where, Collection<Character> entrants) {
        List<Character> out = new ArrayList<>();
        if (where == null) return out;
        try { for (Character c : new ArrayList<>(where.characters)) if (c != null && !c.dead && c.player == 1 && c.born && !entrants.contains(c) && c.isManualPc() && seated.get(c) == where) out.add(c); } catch (RuntimeException e) { }
        return out;
    }

    /** The tournament is over: the seats are free again. */
    static void clearSeats(Area where) {
        synchronized (seated) { seated.values().removeIf(a -> a == where); }
    }

    /** An entrant's own bouts through the fight window (when the live window is on), then the result window with a Watch button for each. */
    static void showToEntrant(Character h, String windowTitle, String text, List<FightReplay> pics) {
        if (pics == null) pics = new ArrayList<>();
        if (GameDriver.guiEnabled && GameDriver.mapViewer != null && DecisionBroker.scripted == null) {
            if (Settings.fightView && h.isManualPc()) for (FightReplay pic : pics) GuiFightView.live(pic);
            GuiFightView.tournamentResult(windowTitle, text, pics, EventMusic.ARENA);
        } else GameDriver.showGuiInfo(windowTitle, text);
    }

    // ------------------------------------------------------------------ spectators
    /** Every spectator sees the last rounds (all the bouts if there are eight or fewer), bets if he likes, and is told how it went. */
    static void spectate(Area where, String title, Collection<Character> entrants, Record rec, int rounds, Character champion) {
        if (rec != null && rec.keep && !rec.all.isEmpty()) {
            for (Character h : spectators(where, entrants)) {
                try { spectateOne(h, where, title, rec, rounds, champion); } catch (RuntimeException e) { System.out.println("(spectating failed: " + e + ")"); }
            }
        }
        clearSeats(where);
    }

    private static void spectateOne(Character h, Area where, String title, Record rec, int rounds, Character champion) {
        List<Bout> shown = new ArrayList<>();
        for (Bout b : rec.all) if (rec.all.size() <= 8 || b.round >= rounds - 1) shown.add(b);
        if (shown.isEmpty()) return;
        StringBuilder text = new StringBuilder("=== You watched the tournament at " + title + " ===\n");
        text.append(rec.all.size() + 1).append(" fighters; ").append(rounds).append(rounds == 1 ? " round" : " rounds").append(shown.size() < rec.all.size() ? "; you saw the last " + shown.size() + " bouts.\n\n" : ".\n\n");
        List<FightReplay> pics = new ArrayList<>();
        long net = 0;
        int n = 0;
        for (Bout b : shown) {
            n++;
            long stake = 0; boolean onA = false;
            long wealth = PlayerLists.wealth(h);
            if (wealth >= 50) {
                int pickSide = bet(h, b, wealth);
                if (pickSide >= 0) { onA = pickSide == 0; stake = stakeFor(h, wealth); }
            }
            boolean aWon = b.winner == b.a;
            FightReplay pic = FightReplay.bout(b.a, b.b, b.wcA, b.winner, "Round " + b.round + " of the tournament at " + title, where);
            pic.tourney = 1;
            pics.add(pic);
            if (GameDriver.guiEnabled && GameDriver.mapViewer != null && DecisionBroker.scripted == null && Settings.fightView && h.isManualPc()) GuiFightView.live(pic);
            String line = "Bout " + n + " (round " + b.round + "): " + name(b.a) + " against " + name(b.b) + ": " + name(b.winner) + " won (" + (aWon ? b.wcA : 100 - b.wcA) + "% to win).";
            text.append(line);
            if (stake > 0) {
                boolean won = onA == aWon;
                if (won) { long pay = Math.round(stake * oddsFor(onA ? b.wcA : 100 - b.wcA)); h.ss += pay; h.convertCurrency(); net += pay - stake; text.append("  You bet ").append(stake).append(" ss on ").append(name(onA ? b.a : b.b)).append(" and won ").append(pay).append(" ss."); }
                else { net -= stake; text.append("  You bet ").append(stake).append(" ss on ").append(name(onA ? b.a : b.b)).append(" and lost it."); }
            }
            text.append('\n');
        }
        if (champion != null) text.append("\nThe champion: ").append(name(champion)).append(" (Lv.").append(champion.lvl).append(").\n");
        if (net != 0 || rec.keep) text.append(net > 0 ? "Your bets: +" + net + " ss." : net < 0 ? "Your bets: " + net + " ss." : "").append('\n');
        Notifications.post("fight", "You watched the tournament at " + title + (net != 0 ? " (bets " + (net > 0 ? "+" : "") + net + " ss)" : "") + ".", -1, h.id);
        if (GameDriver.guiEnabled && GameDriver.mapViewer != null && DecisionBroker.scripted == null) GuiFightView.tournamentResult("Tournament result (you watched)", text.toString(), pics, EventMusic.ARENA);
        else System.out.println(text);
    }

    private static String name(Character c) { return c.name + " " + c.lastName; }

    /** The payout multiplier for a side with this chance to win: fair odds less the house's share, between 1.05 and 15. */
    static double oddsFor(int percent) {
        double fair = 100.0 / Math.max(1, Math.min(99, percent));
        return Math.max(1.05, Math.min(15.0, fair * (100 - HOUSE_PERCENT) / 100.0));
    }

    private static long stakeFor(Character h, long wealth) {
        int idx = lastStake;
        return Math.max(1, wealth * STAKE_PERCENT[Math.max(0, Math.min(2, idx))] / 100);
    }

    private static int lastStake = 0;

    /** Asks whom to bet on and how much. Returns 0 (the first fighter), 1 (the second) or -1 (no bet). The stake is chosen in the same breath (`lastStake`). */
    private static int bet(Character h, Bout b, long wealth) {
        String an = name(b.a), bn = name(b.b);
        String prompt = "Bout: " + an + " (Lv." + b.a.lvl + ", " + Elements.name(Elements.of(b.a)) + ") against " + bn + " (Lv." + b.b.lvl + ", " + Elements.name(Elements.of(b.b)) + ").\nBet on the winner? Payout x" + String.format("%.2f", oddsFor(b.wcA)) + " on "
                + b.a.name + ", x" + String.format("%.2f", oddsFor(100 - b.wcA)) + " on " + b.b.name + " (the house keeps " + HOUSE_PERCENT + "%).";
        String[] sides = {"Bet on " + b.a.name, "Bet on " + b.b.name, "No bet"};
        int side = DecisionBroker.ask(h, prompt, sides, 2);
        if (side < 0 || side > 1) return -1;
        String[] stakes = new String[3];
        for (int i = 0; i < 3; i++) stakes[i] = STAKE_PERCENT[i] + "% of your wealth (" + Math.max(1, wealth * STAKE_PERCENT[i] / 100) + " ss)";
        int st = DecisionBroker.ask(h, "How much on " + (side == 0 ? b.a.name : b.b.name) + "?", stakes, 0);
        lastStake = st < 0 || st > 2 ? 0 : st;
        long stake = stakeFor(h, wealth);
        if (!h.canAfford(stake)) return -1;
        h.deductSs(stake);
        return side;
    }
}
