import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

/**
 * Shows the race condition that {@link Experiment} is designed to avoid.
 *
 * <p>The experiment runs simulations in parallel, and every run counts its
 * trap catches with a {@link TrapReport}. A tempting shortcut would be one
 * shared report for all runs. This demo tries three ways of counting the same
 * number of catches from parallel runs:
 * <ul>
 *   <li><b>One shared report</b>: wrong. {@code catches++} is really three
 *       steps (read, add one, write back). Two threads can read the same
 *       value, both add one, and both write it back, so one catch is lost.</li>
 *   <li><b>A shared atomic counter</b>: correct, because each increment
 *       happens as one indivisible step, but every thread competes for the
 *       same counter.</li>
 *   <li><b>One report per run</b>: correct with no locking at all, because
 *       nothing is shared while counting. This is what the experiment does.</li>
 * </ul>
 *
 * <p>Run it with {@code java -cp out:src RaceConditionDemo}. The lost count
 * changes every time, which is what makes race conditions hard to spot.
 */
public final class RaceConditionDemo {

    static final int RUNS = 1_000;
    static final int CATCHES_PER_RUN = 1_000;

    private static final Trap TRAP = new Trap(0, 0, 1);
    private static final Zombie ZOMBIE = new Zombie(0, 0);

    private RaceConditionDemo() {
    }

    /** Wrong: every parallel run reports into the same, unsynchronised report. */
    static long countWithSharedReport() {
        TrapReport shared = new TrapReport();
        IntStream.range(0, RUNS).parallel().forEach(run -> {
            for (int catchNumber = 0; catchNumber < CATCHES_PER_RUN; catchNumber++) {
                shared.onZombieCaught(TRAP, ZOMBIE);
            }
        });
        return shared.getCatches();
    }

    /** Correct but contended: every run adds to one atomic counter. */
    static long countWithAtomicCounter() {
        AtomicLong shared = new AtomicLong();
        TrapListener atomicCounter = (trap, zombie) -> shared.incrementAndGet();
        IntStream.range(0, RUNS).parallel().forEach(run -> {
            for (int catchNumber = 0; catchNumber < CATCHES_PER_RUN; catchNumber++) {
                atomicCounter.onZombieCaught(TRAP, ZOMBIE);
            }
        });
        return shared.get();
    }

    /** Correct with no locking: each run has its own report, added up at the end. */
    static long countWithReportPerRun() {
        return IntStream.range(0, RUNS).parallel()
                .mapToLong(run -> {
                    TrapReport own = new TrapReport();
                    for (int catchNumber = 0; catchNumber < CATCHES_PER_RUN; catchNumber++) {
                        own.onZombieCaught(TRAP, ZOMBIE);
                    }
                    return own.getCatches();
                })
                .sum();
    }

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");
        long expected = (long) RUNS * CATCHES_PER_RUN;
        System.out.printf("%,d parallel runs x %,d catches = %,d catches, %d CPU cores%n%n",
                RUNS, CATCHES_PER_RUN, expected, Runtime.getRuntime().availableProcessors());

        for (int attempt = 1; attempt <= 3; attempt++) {
            System.out.println("Attempt " + attempt);
            report("One shared report", expected, RaceConditionDemo::countWithSharedReport);
            report("Shared atomic counter", expected, RaceConditionDemo::countWithAtomicCounter);
            report("One report per run", expected, RaceConditionDemo::countWithReportPerRun);
            System.out.println();
        }
    }

    private static void report(String label, long expected,
                               java.util.function.LongSupplier counting) {
        long start = System.nanoTime();
        long counted = counting.getAsLong();
        double millis = (System.nanoTime() - start) / 1e6;
        String verdict = counted == expected
                ? "correct"
                : String.format("WRONG, %,d catches lost", expected - counted);
        System.out.printf("  %-22s %,11d  %-28s %6.1f ms%n", label, counted, verdict, millis);
    }
}
