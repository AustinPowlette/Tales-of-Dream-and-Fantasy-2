package Game;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/** A Random for turns that run on several cores: every thread draws from its own generator (java.util.Random's one shared seed is a bottleneck when many threads ask at once). Not reproducible from a seed. */
final class ParallelRandom extends Random {
    private static final long serialVersionUID = 1L;
    ParallelRandom() { super(0); }
    @Override protected int next(int bits) { return (int) (ThreadLocalRandom.current().nextLong() >>> (64 - bits)); }
}
