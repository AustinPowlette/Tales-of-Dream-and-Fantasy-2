package Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/** The boards of the world, recalculated once a year: the hundred with the Highest Level, the hundred with the Highest Fighting Level, the hundred Richest, the hundred with Most Kills, the hundred Most Famous (`Fame`), and the thousand of each level board.
 *  Rank 1-3 of a board earn its grandest title, 4-10 a lesser one, 11-100 the plainest; a cultivator holds the best title they have earned.
 *  Everybody on a board is spared by the epoch (so is every child, see Character.childSafe, and everyone on the road of the Palace's story). */
final class Rankings {
    private Rankings() { }

    static final int BOARD = 100;          // places with a title on each board
    static final int STRONG_SAFE = 1000;   // the strongest this many are spared by the epoch (no title)
    static final String[] BOARDS = {"Highest Level", "Richest", "Most Kills", "Highest Fighting Level", "Most Famous"}; // (0 was "Strongest": the same board, renamed)

    // [board][tier] -> {man's title, woman's title}; tier 0 = rank 1-3, 1 = rank 4-10, 2 = rank 11-100
    private static final String[][][] TITLES = {
        {{"Sovereign of the Nine Heavens", "Empress of the Nine Heavens"}, {"Pillar of Heaven", "Pillar of Heaven"}, {"One of the Divine Hundred", "One of the Divine Hundred"}},
        {{"Lord of Ten Thousand Vaults", "Lady of Ten Thousand Vaults"}, {"Master of the Golden Vault", "Mistress of the Golden Vault"}, {"Jade Vault Magnate", "Jade Vault Magnate"}},
        {{"Crimson Calamity", "Crimson Calamity"}, {"Nine-Hell Reaper", "Nine-Hell Reaper"}, {"Butcher of the Hundred", "Butcher of the Hundred"}},
        {{"War God of the Nine Heavens", "War Goddess of the Nine Heavens"}, {"Peerless Warrior", "Peerless Warrior"}, {"One of the Hundred Blades", "One of the Hundred Blades"}},
        {{"Name Known in All the Realms", "Name Known in All the Realms"}, {"Household Name of the Garden", "Household Name of the Garden"}, {"One of the Hundred Names", "One of the Hundred Names"}}};

    static int tier(int rank) { return rank <= 3 ? 0 : rank <= 10 ? 1 : 2; }

    static boolean eligible(Character c) {
        return c != null && !c.dead && c.born && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster && !c.wanderingImmortal;
    }

    /** Who may be on a board: cultivators on all four; a Monster (a beast that woke) on the boards of level, kills and fighting level (never the Richest), so the legends of the wilds stand beside the cultivators. */
    static boolean eligible(Character c, int board) {
        if (eligible(c)) return true;
        return board != 1 && c != null && !c.dead && c.born && c.trait == Character.Traits.Monster && c.masterId < 0;
    }

    /** How strong a cultivator is for the board (realm, then level, then kills). */
    static long strength(Character c) { return c.majorLvl * 100000L + c.lvl * 1000L + Math.min(999, c.kills); }

    /** The fighting rating of a cultivator: the number a fight is decided on before the division by five (level, worn gear and enchantment, the two chosen techniques) plus five for every point of the lasting bonuses
     *  (a soldier's blessing, the Cheater's and the Iron Body's, the companion beasts). Read from what is worn and held: nothing is equipped, nothing changes (`tempLvlCheck` re-equips, so it cannot be called 100,000 times). Wine and poison are not counted: they pass. */
    static long fightRating(Character c) {
        long raw = c.lvl;
        if (c.equippedClothing != null) raw += c.equippedClothing.lvl;
        if (c.equippedWeapon != null) raw += c.equippedWeapon.lvl + c.equippedWeapon.enchantBonus;
        if (c.martialTechnique != null) raw += c.martialTechnique.lvl;
        if (c.movementTechnique != null) raw += c.movementTechnique.lvl;
        long bonus = c.blessing;
        if (c.trait == Character.Traits.Cheater) bonus += 5;
        if (c.trait == Character.Traits.IronBody) bonus += c.majorLvl / 2 + 1;
        if (c.beastIds != null && !c.beastIds.isEmpty()) bonus += Taming.bonus(c);
        return raw + 5 * bonus;
    }

    /** The fighting level as a fight uses it (the rating divided by five). */
    static int fightLevel(Character c) { return (int) (fightRating(c) / 5); }

    static long metric(Character c, int board) {
        switch (board) {
            case 0: return strength(c);
            case 1: return PlayerLists.wealth(c);
            case 3: return fightRating(c) * 1000L + Math.min(999, c.kills);
            case 4: return c.fame;
            default: return c.kills;
        }
    }

    static int rankOf(Character c, int board) { return board == 0 ? c.rankStrong : board == 1 ? c.rankRich : board == 2 ? c.rankKills : board == 4 ? c.rankFame : c.rankFight; }

    /** Best first: the larger metric, then the lower id (the older soul). */
    private static Comparator<Character> best(final int board) {
        return (x, y) -> { int d = Long.compare(metric(y, board), metric(x, board)); return d != 0 ? d : Integer.compare(x.id, y.id); };
    }

    /** The `n` best of the list on a board, best first. */
    static List<Character> top(List<Character> all, int board, int n) {
        final Comparator<Character> better = best(board);
        PriorityQueue<Character> heap = new PriorityQueue<Character>(n + 1, Collections.reverseOrder(better)); // the worst of the kept at the head
        for (Character c : all) {
            if (!eligible(c, board)) continue;
            if (board == 2 && c.kills <= 0) continue;                       // nobody ranks for killing nothing
            if (board == 1 && PlayerLists.wealth(c) <= 0) continue;
            if (board == 4 && c.fame <= 0) continue;                       // nobody ranks for being unknown
            if (heap.size() < n) heap.add(c);
            else if (better.compare(c, heap.peek()) < 0) { heap.poll(); heap.add(c); }
        }
        ArrayList<Character> out = new ArrayList<Character>(heap);
        Collections.sort(out, better);
        return out;
    }

    /** Once a year: sets every cultivator's places and clears the old ones. */
    static void yearly(List<Character> all) {
        Monsters.yearly(all); // the cap on a Monster's level (the twentieth-strongest cultivator), counted before the boards
        Fame.yearly(all);     // the year's crafting becomes fame, the old fame fades
        for (Character c : all) if (c != null) { c.rankStrong = 0; c.rankRich = 0; c.rankKills = 0; c.rankFight = 0; c.rankFame = 0; }
        List<Character> strong = top(all, 0, STRONG_SAFE);
        for (int i = 0; i < strong.size(); i++) strong.get(i).rankStrong = i + 1;
        List<Character> rich = top(all, 1, BOARD);
        for (int i = 0; i < rich.size(); i++) rich.get(i).rankRich = i + 1;
        List<Character> killers = top(all, 2, BOARD);
        for (int i = 0; i < killers.size(); i++) killers.get(i).rankKills = i + 1;
        List<Character> fighters = top(all, 3, STRONG_SAFE);
        for (int i = 0; i < fighters.size(); i++) fighters.get(i).rankFight = i + 1;
        List<Character> famous = top(all, 4, BOARD);
        for (int i = 0; i < famous.size(); i++) famous.get(i).rankFame = i + 1;
    }

    static String titleOf(Character c, int board, int rank) {
        if (c.trait == Character.Traits.Monster) return new String[]{"Sovereign of the Wilds", "Lord of the Wilds", "Legend of the Wilds"}[tier(rank)];
        boolean f = "Female".equals(c.gender);
        return TITLES[board][tier(rank)][f ? 1 : 0];
    }

    /** The best board title this cultivator holds (grandest tier first, then the order Strongest, Richest, Most Kills); null when on no board's hundred. */
    static String title(Character c) {
        int bestTier = 9, bestBoard = -1, bestRank = 0;
        int[] ranks = {c.rankStrong, c.rankRich, c.rankKills, c.rankFight, c.rankFame};
        for (int b = 0; b < 5; b++) {
            if (ranks[b] <= 0 || ranks[b] > BOARD) continue;
            if (tier(ranks[b]) < bestTier) { bestTier = tier(ranks[b]); bestBoard = b; bestRank = ranks[b]; }
        }
        return bestBoard < 0 ? null : titleOf(c, bestBoard, bestRank);
    }

    /** Spared by the epoch: a child, anyone on a board (the thousand strongest included), anyone on the road of the Palace's story. */
    static boolean epochSafe(Character c) {
        return c.childSafe() || c.noDeath || c.rankStrong > 0 || c.rankFight > 0 || c.rankRich > 0 || c.rankKills > 0 || c.rankFame > 0 || Lore.onStoryPath(c) || c.fatedTo > 0;
    }

    /** One line about where a cultivator stands ("Strongest 2, Richest 40"), empty when nowhere. */
    static String places(Character c) {
        StringBuilder sb = new StringBuilder();
        if (c.rankStrong > 0) sb.append("Highest Level ").append(c.rankStrong);
        if (c.rankFight > 0) sb.append(sb.length() > 0 ? ", " : "").append("Fighting Level ").append(c.rankFight);
        if (c.rankRich > 0) sb.append(sb.length() > 0 ? ", " : "").append("Richest ").append(c.rankRich);
        if (c.rankKills > 0) sb.append(sb.length() > 0 ? ", " : "").append("Most Kills ").append(c.rankKills);
        if (c.rankFame > 0) sb.append(sb.length() > 0 ? ", " : "").append("Most Famous ").append(c.rankFame);
        return sb.toString();
    }

    /** The text of a board (or of all three, the first ten of each): who holds each place now (as of the last yearly count). */
    static String board(int board, int limit) {
        List<Character> all = Map.characters;
        List<Character> list = new ArrayList<Character>();
        for (Character c : all) {
            if (c == null || c.dead) continue;
            int r = rankOf(c, board);
            if (r > 0 && r <= BOARD) list.add(c);
        }
        final int b = board;
        Collections.sort(list, (x, y) -> Integer.compare(rankOf(x, b), rankOf(y, b)));
        StringBuilder sb = new StringBuilder("=== " + BOARDS[board] + " (the hundred, recounted every New Year) ===\n");
        if (list.isEmpty()) sb.append("  (no ranking yet: the first count comes at the end of the first year)\n");
        int shown = 0;
        for (Character c : list) {
            if (shown++ >= limit) break;
            int rank = rankOf(c, board);
            String stat = board == 0 ? "Major " + c.majorLvl + " Lv." + c.lvl : board == 1 ? TermFormat.ss(PlayerLists.wealth(c)) : board == 3 ? "fighting level " + fightLevel(c) : board == 4 ? c.fame + " fame" : c.kills + " kills";
            sb.append(String.format("%3d. %s %s%s  [#%d]  %s  - %s%n", rank, c.name, c.lastName, c.trait == Character.Traits.Monster ? " (Monster)" : "", c.id, stat, titleOf(c, board, rank)));
        }
        return sb.toString();
    }

    static String text(String which, Character me) {
        StringBuilder sb = new StringBuilder();
        String w = which == null ? "" : which.toLowerCase();
        final boolean level = w.startsWith("st") || w.startsWith("le") || w.startsWith("hi") && !w.startsWith("hif") && !w.contains("fight"), fight = w.startsWith("fi") || w.startsWith("hif") || w.contains("fight");
        final boolean fame = w.startsWith("fa");
        boolean one = level || fight || fame || w.startsWith("ri") || w.startsWith("ki") || w.startsWith("mo");
        int limit = one ? BOARD : 10;
        if (!one || level) sb.append(board(0, limit)).append('\n');
        if (!one || fight) sb.append(board(3, limit)).append('\n');
        if (!one || w.startsWith("ri")) sb.append(board(1, limit)).append('\n');
        if (!one || w.startsWith("ki") || w.startsWith("mo")) sb.append(board(2, limit)).append('\n');
        if (!one || fame) sb.append(board(4, limit)).append('\n');
        if (me != null) {
            String p = places(me);
            sb.append(p.isEmpty() ? "You are on no board yet." : "You stand: " + p + (me.repTitle != null ? "  ('" + me.repTitle + "')" : "")).append('\n');
        }
        sb.append("Rank 1-3 of a board earns its grandest title, 4-10 a lesser one, 11-100 the plainest. The thousand of each level board (and all on a board, all children and all on the Palace's road) are spared by the epoch.\n");
        sb.append("ranking [level|fighting|richest|kills|fame] shows a board in full.");
        return sb.toString();
    }
}
