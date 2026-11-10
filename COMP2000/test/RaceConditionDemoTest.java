public final class RaceConditionDemoTest {

    private static final long EXPECTED =
            (long) RaceConditionDemo.RUNS * RaceConditionDemo.CATCHES_PER_RUN;

    public static void main(String[] args) {
        // Repeat, because a race only shows up some of the time.
        for (int attempt = 0; attempt < 20; attempt++) {
            check(RaceConditionDemo.countWithReportPerRun() == EXPECTED,
                    "one report per run never loses a catch");
            check(RaceConditionDemo.countWithAtomicCounter() == EXPECTED,
                    "an atomic counter never loses a catch");
            // The shared report may or may not lose catches on a given run,
            // so the only thing that is always true is that it can't gain any.
            check(RaceConditionDemo.countWithSharedReport() <= EXPECTED,
                    "a shared report can lose catches but never invent them");
        }
        System.out.println("RaceConditionDemoTest passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
