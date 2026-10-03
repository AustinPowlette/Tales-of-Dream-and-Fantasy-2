package Game;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/** Deadlock-free coordination between the game thread (the only writer of world state) and GUI threads (readers).
 *
 *  Rules that make a deadlock impossible:
 *  1. The game thread holds the lock while it simulates and RELEASES it whenever it waits for anything (player input, the pause loop, the
 *     tick delay, a modal dialog it asked the GUI to show). While it is released the world is frozen, so readers see a consistent state.
 *  2. Readers (the Swing thread, script/browser timers) only ever `tryLock` with a short timeout and never wait for the game thread; a read
 *     that cannot get the lock is skipped and simply tried again on the next refresh (the last picture stays on screen).
 *  3. The game thread never waits for a reader without a bound: at the end of each tick it hands the lock over, waits at most ~200 ms for the
 *     GUI refresh it posted, and takes the lock back.
 *  A fair lock guarantees a waiting reader gets in at the handoff even at 0 ms tick delay. */
final class World {
    private World() { }

    private static final ReentrantLock lock = new ReentrantLock(true);
    private static final AtomicInteger waiters = new AtomicInteger();
    static volatile boolean enabled = true;   // tests / headless runs can switch the machinery off

    interface Blocking<T> { T run() throws Exception; }

    /** Game thread, once at start-up. */
    static void gameStart() { if (enabled && !lock.isHeldByCurrentThread()) lock.lock(); }

    static boolean heldByCurrentThread() { return lock.isHeldByCurrentThread(); }

    /** Game thread: run a blocking wait (input, sleep, dialog) with the lock released, then take it back. */
    static <T> T whileBlocked(Blocking<T> b) throws Exception {
        int holds = enabled && lock.isHeldByCurrentThread() ? lock.getHoldCount() : 0;
        for (int i = 0; i < holds; i++) lock.unlock();
        try {
            return b.run();
        } finally {
            for (int i = 0; i < holds; i++) lock.lock();
        }
    }

    /** Game thread, end of a tick: let the GUI read a consistent world. Posts `refresh` to the Swing thread, waits at most `maxMs` for it. */
    static void handoff(Runnable refresh, long maxMs) {
        if (!enabled || !lock.isHeldByCurrentThread()) { javax.swing.SwingUtilities.invokeLater(refresh); return; }
        int holds = lock.getHoldCount();
        for (int i = 0; i < holds; i++) lock.unlock();
        try {
            CountDownLatch done = new CountDownLatch(1);
            javax.swing.SwingUtilities.invokeLater(() -> { try { refresh.run(); } finally { done.countDown(); } });
            try { done.await(maxMs, TimeUnit.MILLISECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        } finally {
            for (int i = 0; i < holds; i++) lock.lock();
        }
    }

    /** Reader threads: run `r` while the world is stable. Returns false (and does nothing) if the game thread is mid-tick for longer than `ms`. */
    static boolean read(long ms, Runnable r) {
        if (!enabled || lock.isHeldByCurrentThread()) { r.run(); return true; }
        waiters.incrementAndGet();
        boolean got;
        try {
            got = lock.tryLock(ms, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            waiters.decrementAndGet();
        }
        if (!got) return false;
        try { r.run(); return true; } finally { lock.unlock(); }
    }

    /** Like read() but retries for up to `totalMs` (for one-off user actions such as opening a dialog, where waiting for the next tick boundary is fine). */
    static boolean readWait(long totalMs, Runnable r) {
        long end = System.currentTimeMillis() + totalMs;
        do {
            if (read(50, r)) return true;
        } while (System.currentTimeMillis() < end);
        return false;
    }

    static int waitingReaders() { return waiters.get(); }
}
