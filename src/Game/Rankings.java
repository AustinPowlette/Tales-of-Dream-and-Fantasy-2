package Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/** The boards of the world, recalculated once a year: the hundred Strongest, the hundred Richest, the hundred with Most Kills, and the thousand Strongest.
 *  Rank 1-3 of a board earn its grandest title, 4-10 a lesser one, 11-100 the plainest; a cultivator holds the best title they have earned.
 *  Everybody on a board is spared by the epoch (so is every child, see Character.childSafe, and everyone on the road of the Palace's story). */
final class Rankings {
    private Rankings() { }

    static final int BOARD = 100;          // places with a title on each board
    static final int STRONG_SAFE = 1000;   // the strongest this many are spared by the epoch (no title)
    static final String[] BOARDS = {"Strongest", "Richest", "Most Kills"};

    // [board][tier] -> {man's title, woman's title}; tier 0 = rank 1-3, 1 = rank 4-10, 2 = rank 11-100
    private static final String[][][] TITLES = {
        {{"Sovereign of the Nine Heavens", "Empress of the Nine Heavens"}, {"Pillar of Heaven", "Pillar of Heaven"}, {"One of the Divine Hundred", "One of the Divine Hundred"}},
        {{"Lord of Ten Thousand Vaults", "Lady of Ten Thousand Vaults"}, {"Master of the Golden Vault", "Mistress of the Golden Vault"}, {"Jade Vault Magnate", "Jade Vault Magnate"}},
        {{"Crimson Calamity", "Crimson Calamity"}, {"Nine-Hell Reaper", "Nine-Hell Reaper"}, {"Butcher of the Hundred", "Butcher of the Hundred"}}};

    static int tier(int rank) { return rank <= 3 ? 0 : rank <= 10 ? 1 : 2; }

    static boolean eligible(Character c) {
        return c != null && !c.dead && c.born && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster && !c.wanderingImmortal;
    }

    /** How strong a cultivator is for the board (realm, then level, then kills). */
    static long strength(Character c) { return c.majorLvl * 100000L + c.lvl * 1000L + Math.min(999, c.kills); }

    static long metric(Character c, int board) {
        switch (board) {
            case 0: return strength(c);
            case 1: return PlayerLists.wealth(c);
            default: return c.kills;
        }
    }

    /** Best first: the larger metric, then the lower id (the older soul). */
    private static Comparator<Character> best(final int board) {
        return (x, y) -> { int d = Long.compare(metric(y, board), metric(x, board)); return d != 0 ? d : Integer.compare(x.id, y.id); };
    }

    /** The `n` best of the list on a board, best first. */
    static List<Character> top(List<Character> all, int board, int n) {
        final Comparator<Character> better = best(board);
        PriorityQueue<Character> heap = new PriorityQueue<Character>(n + 1, Collections.reverseOrder(better)); // the worst of the kept at the head
        for (Character c : all) {
            if (!eligible(c)) continue;
            if (board == 2 && c.kills <= 0) continue;                       // nobody ranks for killing nothing
            if (board == 1 && PlayerLists.wealth(c) <= 0) continue;
            if (heap.size() < n) heap.add(c);
            else if (better.compare(c, heap.peek()) < 0) { heap.poll(); heap.add(c); }
        }
        ArrayList<Character> out = new ArrayList<Character>(heap);
        Collections.sort(out, better);
        return out;
    }

    /** Once a year: sets every cultivator's places and clears the old ones. */
    static void yearly(List<Character> all) {
        for (Character c : all) if (c != null) { c.rankStrong = 0; c.rankRich = 0; c.rankKills = 0; }
        List<Character> strong = top(all, 0, STRONG_SAFE);
        for (int i = 0; i < strong.size(); i++) strong.get(i).rankStrong = i + 1;
        List<Character> rich = top(all, 1, BOARD);
        for (int i = 0; i < rich.size(); i++) rich.get(i).rankRich = i + 1;
        List<Character> killers = top(all, 2, BOARD);
        for (int i = 0; i < killers.size(); i++) killers.get(i).rankKills = i + 1;
    }

    static String titleOf(Character c, int board, int rank) {
        boolean f = "Female".equals(c.gender);
        return TITLES[board][tier(rank)][f ? 1 : 0];
    }

    /** The best board title this cultivator holds (grandest tier first, then the order Strongest, Richest, Most Kills); null when on no board's hundred. */
    static String title(Character c) {
        int bestTier = 9, bestBoard = -1, bestRank = 0;
        int[] ranks = {c.rankStrong, c.rankRich, c.rankKills};
        for (int b = 0; b < 3; b++) {
            if (ranks[b] <= 0 || ranks[b] > BOARD) continue;
            if (tier(ranks[b]) < bestTier) { bestTier = tier(ranks[b]); bestBoard = b; bestRank = ranks[b]; }
        }
        return bestBoard < 0 ? null : titleOf(c, bestBoard, bestRank);
    }

    /** Spared by the epoch: a child, anyone on a board (the thousand strongest included), anyone on the road of the Palace's story. */
    static boolean epochSafe(Character c) {
        return c.childSafe() || c.rankStrong > 0 || c.rankRich > 0 || c.rankKills > 0 || Lore.onStoryPath(c) || c.fatedTo > 0;
    }

    /** One line about where a cultivator stands ("Strongest 2, Richest 40"), empty when nowhere. */
    static String places(Character c) {
        StringBuilder sb = new StringBuilder();
        if (c.rankStrong > 0) sb.append("Strongest ").append(c.rankStrong);
        if (c.rankRich > 0) sb.append(sb.length() > 0 ? ", " : "").append("Richest ").append(c.rankRich);
        if (c.rankKills > 0) sb.append(sb.length() > 0 ? ", " : "").append("Most Kills ").append(c.rankKills);
        return sb.toString();
    }

    /** The text of a board (or of all three, the first ten of each): who holds each place now (as of the last yearly count). */
    static String board(int board, int limit) {
        List<Character> all = Map.characters;
        List<Character> list = new ArrayList<Character>();
        for (Character c : all) {
            if (c == null || c.dead) continue;
            int r = board == 0 ? c.rankStrong : board == 1 ? c.rankRich : c.rankKills;
            if (r > 0 && r <= BOARD) list.add(c);
        }
        final int b = board;
        Collections.sort(list, (x, y) -> Integer.compare(b == 0 ? x.rankStrong : b == 1 ? x.rankRich : x.rankKills, b == 0 ? y.rankStrong : b == 1 ? y.rankRich : y.rankKills));
        StringBuilder sb = new StringBuilder("=== " + BOARDS[board] + " (the hundred, recounted every New Year) ===\n");
        if (list.isEmpty()) sb.append("  (no ranking yet: the first count comes at the end of the first year)\n");
        int shown = 0;
        for (Character c : list) {
            if (shown++ >= limit) break;
            int rank = board == 0 ? c.rankStrong : board == 1 ? c.rankRich : c.rankKills;
            String stat = board == 0 ? "Major " + c.majorLvl + " Lv." + c.lvl : board == 1 ? TermFormat.ss(PlayerLists.wealth(c)) : c.kills + " kills";
            sb.append(String.format("%3d. %s %s  [#%d]  %s  - %s%n", rank, c.name, c.lastName, c.id, stat, titleOf(c, board, rank)));
        }
        return sb.toString();
    }

    static String text(String which, Character me) {
        StringBuilder sb = new StringBuilder();
        String w = which == null ? "" : which.toLowerCase();
        boolean one = w.startsWith("st") || w.startsWith("ri") || w.startsWith("ki") || w.startsWith("mo");
        int limit = one ? BOARD : 10;
        if (!one || w.startsWith("st")) sb.append(board(0, limit)).append('\n');
        if (!one || w.startsWith("ri")) sb.append(board(1, limit)).append('\n');
        if (!one || w.startsWith("ki") || w.startsWith("mo")) sb.append(board(2, limit)).append('\n');
        if (me != null) {
            String p = places(me);
            sb.append(p.isEmpty() ? "You are on no board yet." : "You stand: " + p + (me.repTitle != null ? "  ('" + me.repTitle + "')" : "")).append('\n');
        }
        sb.append("Rank 1-3 of a board earns its grandest title, 4-10 a lesser one, 11-100 the plainest. The thousand strongest (and all on a board, all children and all on the Palace's road) are spared by the epoch.\n");
        sb.append("ranking [strongest|richest|kills] shows a board in full.");
        return sb.toString();
    }
}
