package Game;

/** Optional per-phase tick timer (measurement only). Enabled with -Dtdf.perf=true; otherwise every call is a single boolean test.
 *  mark(phase) adds the time since the previous mark to that phase; tickEnd() rolls the per-tick numbers into running totals. */
final class Perf {
    private Perf() { }
    static final boolean ON = Boolean.getBoolean("tdf.perf");
    static final String[] NAMES = {"wait/save", "yearly", "areaScan", "epoch", "time()", "cultivate", "phase2A", "phase2B", "tame+reinc", "area.age", "monsters", "cleanup", "guiHandoff", "steps", "companions"};
    static final int WAIT = 0, YEARLY = 1, AREASCAN = 2, EPOCH = 3, TIME = 4, CULT = 5, P2A = 6, P2B = 7, TAME = 8, AGE = 9, MON = 10, CLEAN = 11, GUI = 12, STEPS = 13, FOLLOW = 14;
    static final long[] ns = new long[NAMES.length];       // since the last reset
    static final long[] total = new long[NAMES.length];    // whole run
    static long last = 0, ticks = 0, tickStart = 0, tickNsTotal = 0, ticksSince = 0, maxTick = 0;

    static void begin() { if (ON) { last = System.nanoTime(); tickStart = last; } }
    static void mark(int phase) { if (ON) { long n = System.nanoTime(); ns[phase] += n - last; total[phase] += n - last; last = n; } }
    static void tickEnd() { if (ON) { long n = System.nanoTime(); long d = n - tickStart; tickNsTotal += d; maxTick = Math.max(maxTick, d); ticks++; ticksSince++; } }
    /** Per-tick averages (ms) since the last call, as "name=ms name=ms ... tick=ms n=ticks"; resets the window. */
    static synchronized String window() {
        StringBuilder sb = new StringBuilder();
        long n = Math.max(1, ticksSince);
        for (int i = 0; i < NAMES.length; i++) { sb.append(NAMES[i].replace(' ', '_')).append('=').append(String.format("%.1f", ns[i] / 1e6 / n)).append(' '); ns[i] = 0; }
        sb.append("max=").append(String.format("%.0f", maxTick / 1e6)).append(" n=").append(ticksSince);
        ticksSince = 0; maxTick = 0;
        return sb.toString();
    }
}
