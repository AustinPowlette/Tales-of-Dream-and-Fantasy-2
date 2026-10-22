package Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** The Garden's Variety Tournament. At year 10, at year 100 and every 100 years after, the sky opens and almost a thousand stone platforms fall out of it ({@link #PLATFORMS}). Every living person is entered, whether they
 *  wish it or not (beasts are not people; a Monster of the wilds is). Nobody dies: a bout only decides who goes on. Each round the people still standing are paired anew at random, anyone against anyone, with no limit of
 *  level or fighting level, and the loser is out; the odds are the ones of every duel (fighting level, elements, the Lucky), so a child can beat an immortal 5 times in 100. When the people are an odd number one has no
 *  opponent and goes on. The rounds go on until one is left, and a person's score is how many rounds they SURVIVED out of how many there were: strength and luck together. The score is kept on the person
 *  ({@link Character#gardenLog}) and shown on the profile; the best of the field are rewarded (the table below), the rest only have the score. A person who plays is told in a scene, may watch their bouts, and gets a result window.
 *  The tournament is settled in one pass of the year change (about a second at 100,000 people); that tick is not counted toward the epoch's test of slow ticks (see GameDriver.tickPurging), and
 *  `settings garden off` skips it altogether. */
final class GardenTournament {
    private GardenTournament() { }

    static final int FIRST_YEAR = 10, EVERY_YEARS = 100, PLATFORMS = 999;
    static volatile boolean heldThisTick = false;   // the tick that held it is left out of the epoch's measure of slow ticks

    // ------------------------------------------------------------------ the prizes by how far a person got (d = rounds short of the champion's)
    static final String[] TIER = {"Champion of the Garden", "Finalist", "Semifinalist", "Quarterfinalist", "Last Sixteen", "Last Sixty-Four", "Platform Survivor", "Entrant"};
    static final double[] PILLS = {3, 2, 1, 1, 0.5, 0, 0, 0};                 // Potential Pills' worth (Character.givePotential)
    static final int[] FAME = {5000, 2500, 1500, 800, 400, 150, 40, 0};
    static final int[] STONES = {2000, 1000, 500, 250, 125, 50, 10, 0};        // times (Major Lvl + 1) squared, in spirit stones
    static final int LUCK_PILLS = 1, LUCK_FAME = 150;                         // the Favoured of the Garden: the weakest person who got through half the rounds

    static boolean isGardenYear(int year) { return year == FIRST_YEAR || (year >= EVERY_YEARS && year % EVERY_YEARS == 0); }

    /** The tier of a person who survived `s` of `r` rounds. */
    static int tierOf(int s, int r) {
        int d = r - s;
        if (d <= 4) return Math.max(0, d);
        if (d <= 6) return 5;
        return s * 2 >= r ? 6 : 7;
    }

    // ------------------------------------------------------------------ the record on a person
    static int pack(int year, int survived, int rounds, boolean lucky) { return (lucky ? 1 << 30 : 0) | (year & 0x3FFFF) << 12 | (survived & 63) << 6 | (rounds & 63); }
    static int yearOf(int e) { return (e >>> 12) & 0x3FFFF; }
    static int survivedOf(int e) { return (e >>> 6) & 63; }
    static int roundsOf(int e) { return e & 63; }
    static boolean luckyOf(int e) { return (e & (1 << 30)) != 0; }

    private static void record(Character c, int e) {
        int[] old = c.gardenLog;
        int[] now = old == null ? new int[1] : java.util.Arrays.copyOf(old, old.length + 1);
        now[now.length - 1] = e;
        c.gardenLog = now;
    }

    /** "Year 10: survived 7 of 17 rounds" for each tournament the person was in, empty when none. */
    static String profileLine(Character c) {
        if (c == null || c.gardenLog == null || c.gardenLog.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int e : c.gardenLog) {
            if (sb.length() > 0) sb.append("; ");
            sb.append("Year ").append(yearOf(e)).append(": survived ").append(survivedOf(e)).append(" of ").append(roundsOf(e)).append(" rounds");
            int t = tierOf(survivedOf(e), roundsOf(e));
            if (t <= 4) sb.append(" (").append(TIER[t]).append(')');
            else if (luckyOf(e)) sb.append(" (Favoured of the Garden)");
        }
        return sb.toString();
    }

    /** The title the last tournament gave, or null. `grand` titles (champion, finalist) outrank the titles of the boards. */
    static String title(Character c) {
        if (c == null || c.gardenLog == null || c.gardenLog.length == 0) return null;
        int e = c.gardenLog[c.gardenLog.length - 1];
        int t = tierOf(survivedOf(e), roundsOf(e));
        if (t == 0) return "Champion of the Garden";
        if (t == 1) return "Finalist of the Garden";
        if (luckyOf(e)) return "Favoured of the Garden";
        return null;
    }
    static boolean grand(Character c) {
        if (c == null || c.gardenLog == null || c.gardenLog.length == 0) return false;
        int e = c.gardenLog[c.gardenLog.length - 1];
        return tierOf(survivedOf(e), roundsOf(e)) <= 1;
    }

    // ------------------------------------------------------------------ the year
    /** Called in the year change, before the epoch's test. */
    static void yearly(int year) {
        if (!Settings.gardenGames || !isGardenYear(year) || held(year)) return;
        run(year);
    }

    private static final String HEAD = "The Garden's Variety Tournament of Year ";

    static boolean held(int year) {
        synchronized (Chronicle.log) {
            for (Chronicle.Entry e : Chronicle.log) if ("legend".equals(e.kind) && e.text.startsWith(HEAD + year + ":")) return true;
        }
        return false;
    }

    /** What a person who plays keeps of the tournament while it is settled. */
    private static final class Human {
        final Character c; final int index;
        final StringBuilder story = new StringBuilder();
        final List<FightReplay> pics = new ArrayList<>();
        final int platform = 1 + GameDriver.rand.nextInt(PLATFORMS);
        Human(Character c, int index) { this.c = c; this.index = index; }
    }

    static boolean eligible(Character c) {
        return c != null && !c.dead && c.born && c.trait != Character.Traits.Beast && !(c.trait == Character.Traits.Monster && c.masterId >= 0);
    }

    /** Settles the tournament. Returns the champion (null when there were fewer than two people). */
    static Character run(int year) {
        final long t0 = System.nanoTime();
        final java.util.Random rand = GameDriver.rand;
        final List<Character> field = new ArrayList<>();
        for (Character c : Map.characters) if (eligible(c)) field.add(c);
        final int n = field.size();
        if (n < 2) return null;
        heldThisTick = true;
        int rounds = 0;
        for (int m = n; m > 1; m = (m + 1) / 2) rounds++;
        final int R = rounds;
        final int[] pw = new int[n];
        for (int i = 0; i < n; i++) pw[i] = Rankings.fightLevel(field.get(i));
        final int[] survived = new int[n];
        final HashMap<Integer, Human> humans = new HashMap<>();
        for (int i = 0; i < n; i++) if (field.get(i).player == 1) humans.put(i, new Human(field.get(i), i));
        int[] pool = new int[n];
        for (int i = 0; i < n; i++) pool[i] = i;
        int size = n;
        for (int round = 1; round <= R && size > 1; round++) {
            for (int i = size - 1; i > 0; i--) { int j = rand.nextInt(i + 1); int t = pool[i]; pool[i] = pool[j]; pool[j] = t; }
            int[] next = new int[(size + 1) / 2];
            int k = 0;
            for (int i = 0; i + 1 < size; i += 2) {
                int a = pool[i], b = pool[i + 1];
                Character ca = field.get(a), cb = field.get(b);
                int wc = 50 - 15 * (pw[b] - pw[a]) + Elements.swing(ca, cb);
                if (ca.trait == Character.Traits.Lucky) wc += 45;
                if (cb.trait == Character.Traits.Lucky) wc -= 45;
                wc = Math.max(5, Math.min(95, wc));
                boolean aWins = rand.nextInt(100) < wc;
                int w = aWins ? a : b;
                next[k++] = w;
                survived[w] = round;
                if (!humans.isEmpty() && (humans.containsKey(a) || humans.containsKey(b))) humanBout(humans, field, a, b, wc, aWins, round);
            }
            if (size % 2 == 1) {
                int bye = pool[size - 1];
                next[k++] = bye;
                survived[bye] = round;
                Human h = humans.get(bye);
                if (h != null) h.story.append("  Round ").append(round).append(": no opponent fell to your platform, so you go on (luck).\n");
            }
            pool = next;
            size = k;
        }
        final int champIdx = size == 1 ? pool[0] : -1;
        final Character champion = champIdx < 0 ? null : field.get(champIdx);

        // how many people ended each round (for the shares and the places)
        final int[] cnt = new int[R + 2];
        for (int i = 0; i < n; i++) cnt[survived[i]]++;
        final int[] below = new int[R + 2];          // people who fell in an earlier round than someone who survived s rounds
        for (int s = 1; s <= R + 1; s++) below[s] = below[s - 1] + cnt[s - 1];

        // the Favoured of the Garden: the weakest person who got through at least half the rounds (not already a placer)
        int lucky = -1;
        for (int i = 0; i < n; i++) {
            if (survived[i] * 2 < R || tierOf(survived[i], R) <= 4) continue;
            if (lucky < 0 || pw[i] < pw[lucky] || (pw[i] == pw[lucky] && rand.nextInt(2) == 0)) lucky = i;
        }

        // the record, the prizes
        int top = 1;
        for (Character c : field) if (c.majorLvl > top) top = c.majorLvl;
        final int topMajor = top;
        final List<Object[]> placed = new ArrayList<>();     // {tier, text} of the best sixteen
        final HashMap<Integer, String> prizeText = new HashMap<>();
        for (int i = 0; i < n; i++) {
            Character c = field.get(i);
            int s = survived[i], t = tierOf(s, R);
            record(c, pack(year, s, R, i == lucky));
            String prize = reward(c, t, i == lucky, champion, topMajor);
            if (t <= 4 && placed.size() < 16) placed.add(new Object[]{t, TIER[t] + ": " + c.name + " " + c.lastName + " (Lv." + c.lvl + ", Major " + c.majorLvl + ")"});
            if (humans.containsKey(i)) prizeText.put(i, prize);
        }
        placed.sort((x, y) -> Integer.compare((Integer) x[0], (Integer) y[0]));
        final List<String> placers = new ArrayList<>();
        for (Object[] o : placed) placers.add((String) o[1]);
        if (lucky >= 0) { Character l = field.get(lucky); placers.add("Favoured of the Garden: " + l.name + " " + l.lastName + " (Lv." + l.lvl + ") got through " + survived[lucky] + " of " + R + " rounds"); }

        final String summary = HEAD + year + ": " + String.format("%,d", n) + " people fought on " + PLATFORMS + " platforms in " + R + " rounds. The champion is " + (champion == null ? "no one" : champion.name + " " + champion.lastName + " (Lv." + champion.lvl + ")") + ".";
        Chronicle.add("legend", summary, champion == null ? new int[0] : new int[]{champion.id});
        System.out.println("=== " + summary + " ===");
        long ms = (System.nanoTime() - t0) / 1_000_000L;
        lastRun = summary + " (settled in " + ms + " ms)";

        // the people who play
        List<Human> order = new ArrayList<>(humans.values());
        order.sort((x, y) -> Integer.compare(x.index, y.index));
        for (Human h : order) {
            int s = survived[h.index], t = tierOf(s, R);
            try { tellHuman(h, year, n, R, s, t, below[s], cnt[s], champion, placers, prizeText.get(h.index), h.index == lucky); }
            catch (RuntimeException e) { System.out.println("(the tournament's report failed: " + e + ")"); }
        }
        return champion;
    }

    static volatile String lastRun = "";

    private static void humanBout(HashMap<Integer, Human> humans, List<Character> field, int a, int b, int wc, boolean aWins, int round) {
        Character ca = field.get(a), cb = field.get(b);
        Character w = aWins ? ca : cb, l = aWins ? cb : ca;
        for (int side = 0; side < 2; side++) {
            Human h = humans.get(side == 0 ? a : b);
            if (h == null) continue;
            Character me = h.c, other = side == 0 ? cb : ca;
            boolean won = w == me;
            int mine = side == 0 ? wc : 100 - wc;
            h.story.append("  Round ").append(round).append(" (platform ").append(1 + (h.platform - 1 + (round - 1) * 211) % PLATFORMS).append("): ").append(won ? "you beat " : "you lost to ").append(other.name).append(" ").append(other.lastName)
                    .append(" (Lv.").append(other.lvl).append(", ").append(mine).append("% to win).\n");
            try {
                FightReplay pic = FightReplay.bout(ca, cb, wc, w, "Round " + round + " of the Garden's Variety Tournament", "the platform");
                pic.tourney = 2;
                h.pics.add(pic);
            } catch (RuntimeException e) { }
        }
    }

    // ------------------------------------------------------------------ prizes
    /** Gives a person what their tier earns; returns a line about it (for the person who plays). */
    private static String reward(Character c, int tier, boolean lucky, Character champion, int topMajor) {
        StringBuilder sb = new StringBuilder();
        if (tier <= 4) {
            Fame.add(c, FAME[tier]);
        } else if (tier <= 6) Fame.add(c, FAME[tier]);
        double pills = PILLS[tier];
        long stones = (long) STONES[tier] * (c.majorLvl + 1) * (c.majorLvl + 1);
        if (lucky) { pills += LUCK_PILLS; Fame.add(c, LUCK_FAME); }
        if (FAME[tier] > 0) sb.append(FAME[tier]).append(" fame");
        if (pills > 0) {
            int g = c.givePotential(pills);
            sb.append(sb.length() > 0 ? ", " : "").append("potential worth ").append(pills == Math.floor(pills) ? String.valueOf((int) pills) : String.valueOf(pills)).append(pills == 1 ? " Potential Pill" : " Potential Pills");
            if (g > 0) sb.append(" (+").append(g).append(")");
        }
        if (stones > 0) { c.ss += stones; c.convertCurrency(); sb.append(sb.length() > 0 ? ", " : "").append(String.format("%,d", stones)).append(" spirit stones"); }
        if (tier <= 1) {
            Treasure.Kind kind = Treasure.Kind.values()[GameDriver.rand.nextInt(Treasure.Kind.values().length)];
            Object t = Treasure.create(kind, Math.max(topMajor, c.majorLvl), c);
            if (t instanceof Technique) { c.techniqueList.add((Technique) t); c.equip(); sb.append(", the technique ").append(((Technique) t).name); }
            else if (t instanceof Item) { Character.addToBag(((Item) t).itemType == Item.ItemType.Array ? c.arrayBag : c.backpack, (Item) t); sb.append(", ").append(t); }
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ the person who plays
    private static List<String> introLines(int platform, int field, int rounds) {
        List<String> l = new ArrayList<>();
        l.add("* The sky opens without a sound.");
        l.add("* Nearly a thousand platforms of pale stone come down out of it. They settle on hills, rooftops, rivers and market squares, as light as leaves. Every platform is wide enough for two people to fight on.");
        l.add("* Everyone is entered. The farmer and the immortal, the child and the old master, the cat-keeper and the sect elder. Nobody signed a name. Nobody is asked.");
        l.add("* A platform stands in front of you. A number is cut into its edge: " + platform + ".");
        l.add("* " + String.format("%,d", field) + " people will fight today. Winners go on, losers go home, and no one dies. After " + rounds + " rounds one person will be left.");
        l.add("* The tournament counts rounds survived, not wins. Skill gets you far. Luck gets you the rest of the way.");
        return l;
    }

    private static void tellHuman(Human h, int year, int field, int R, int s, int tier, int beaten, int same, Character champion, List<String> placers, String prize, boolean lucky) {
        final Character me = h.c;
        final boolean gui = GameDriver.guiEnabled && GameDriver.mapViewer != null && DecisionBroker.scripted == null;
        boolean watch = true;
        if (gui && me.isManualPc()) {
            StoryArt.next("platforms", null, null);
            int pick = GuiScenes.ask(me, "The Garden", "The platforms fall", introLines(h.platform, field, R), "What do you do?", new String[]{"Step onto the platform", "Let the bouts pass: show me the result"}, 0);
            watch = pick == 0;
        }
        StringBuilder sb = new StringBuilder("=== The Garden's Variety Tournament, Year " + year + " ===\n");
        sb.append(String.format("%,d", field)).append(" people fought on ").append(PLATFORMS).append(" platforms in ").append(R).append(" rounds.\n\n");
        sb.append("Your bouts:\n").append(h.story.length() == 0 ? "  (you had no bout)\n" : h.story.toString()).append("\n");
        double pct = field <= 0 ? 0 : 100.0 * beaten / field;
        if (me == champion) sb.append("YOU WON THE GARDEN'S VARIETY TOURNAMENT!\n");
        else sb.append("You survived ").append(s).append(" of ").append(R).append(" rounds.\n");
        sb.append("You went further than ").append(String.format("%.1f", pct)).append("% of the field; ").append(String.format("%,d", same)).append(" people fell in the same round as you.\n");
        sb.append("Standing: ").append(TIER[tier]).append(lucky ? " and Favoured of the Garden (the weakest to get through half the rounds)" : "").append(".\n");
        sb.append(prize == null || prize.isEmpty() ? "Prize: none. The score is on your profile.\n" : "Prize: " + prize + ".\n");
        sb.append("\nThe field's best:\n");
        for (String p : placers) sb.append("  ").append(p).append('\n');
        Notifications.post("fight", "Garden's Variety Tournament, Year " + year + ": you survived " + s + " of " + R + " rounds" + (me == champion ? " and won it all" : "") + ".", -1, me.id);
        if (gui) {
            if (watch && Settings.fightView && me.isManualPc()) {
                EventMusic.hold(EventMusic.GARDEN);
                try {
                    int total = h.pics.size();
                    for (int i = 0; i < total; i++) if (i == 0 || i >= total - 3) GuiFightView.live(h.pics.get(i)); // the first bout and the last three, the ones that decided it
                } finally { EventMusic.release(); }
            }
            GuiFightView.tournamentResult("Garden's Variety Tournament", sb.toString(), h.pics, EventMusic.GARDEN);
        } else System.out.println(sb);
    }
}
