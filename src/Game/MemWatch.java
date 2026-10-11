package Game;

/** The memory of the game, for the epoch: a world that fills the heap is thinned out before it runs out of memory (an old world used to take 7-8 GB and then fall over on a machine that
 *  had less). Once a year, when the heap looks more than half full, one full collection tells how much of it is alive; the epoch comes when that is more than `Settings.epochMemoryPercent`
 *  of the most the heap may grow to (`-Xmx`; the default is a quarter of the machine's memory). The collection also lets the JVM give back the memory it does not use. */
final class MemWatch {
    private MemWatch() { }

    static volatile long liveMb = 0, maxMb = 0;

    /** At the start of the program: asks the JVM to give back the memory it does not use. A JVM keeps its heap as big as it once grew (up to a quarter of the machine's memory by default, 7-8 GB on
     *  a 32 GB machine), so a world that really needs 1.5 GB showed 7 GB in the task manager. The two "heap free ratio" switches can be changed while the program runs: after a collection the
     *  heap shrinks until at most this percent of it is free (a quarter by default is 70). Harmless where the JVM does not know them (it is then left as it is). */
    static void init() {
        try {
            com.sun.management.HotSpotDiagnosticMXBean b = java.lang.management.ManagementFactory.getPlatformMXBean(com.sun.management.HotSpotDiagnosticMXBean.class);
            b.setVMOption("MaxHeapFreeRatio", "35");
            b.setVMOption("MinHeapFreeRatio", "15");
        } catch (Throwable ignored) { }
    }

    /** Whether the live heap is over the limit. `note` (if given) receives the live and the most MB. */
    static boolean full(long[] note) {
        Runtime rt = Runtime.getRuntime();
        long max = rt.maxMemory();
        long used = rt.totalMemory() - rt.freeMemory();
        if (used > max / 2) { // it looks full: how much of it is alive?
            System.gc();
            used = rt.totalMemory() - rt.freeMemory();
        }
        liveMb = used >> 20; maxMb = max >> 20;
        if (note != null && note.length >= 2) { note[0] = liveMb; note[1] = maxMb; }
        int pct = Settings.epochMemoryPercent;
        return pct > 0 && used > max / 100 * pct;
    }

    /** One line about the memory, for `settings` and the log. */
    static String line() {
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) >> 20, max = rt.maxMemory() >> 20;
        return "memory: " + used + " MB in use (" + (liveMb > 0 ? liveMb + " MB alive at the last yearly check" : "no yearly check yet") + ") of " + max + " MB the game may use";
    }
}
