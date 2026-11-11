/**
 * Mutable accumulator for {@link HumanFoodStatistics#energyRange}: the min,
 * max and count of a set of food energy values, gathered in one pass.
 *
 * <p>This exists only to back a custom {@code Collector.of(...)} rather than
 * a built-in one like {@code summarizingDouble} - {@code accept} and
 * {@code combine} are its accumulator and combiner.
 */
public final class EnergyRange {

    private double min = Double.POSITIVE_INFINITY;
    private double max = Double.NEGATIVE_INFINITY;
    private long count;

    /** Folds one more food item's energy value into this range. */
    void accept(Food food) {
        double value = food.getEnergyValue();
        min = Math.min(min, value);
        max = Math.max(max, value);
        count++;
    }

    /** Merges another range accumulated independently (e.g. on another thread) into this one. */
    EnergyRange combine(EnergyRange other) {
        min = Math.min(min, other.min);
        max = Math.max(max, other.max);
        count += other.count;
        return this;
    }

    /** Lowest energy value seen, or 0 if nothing was collected. */
    public double getMin() {
        return count == 0 ? 0 : min;
    }

    /** Highest energy value seen, or 0 if nothing was collected. */
    public double getMax() {
        return count == 0 ? 0 : max;
    }

    /** How many food items contributed to this range. */
    public long getCount() {
        return count;
    }
}
