import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Strategy for choosing which nearby food a human goes for.
 *
 * <p>Different humans can be given different strategies, and a human's
 * strategy can be swapped at runtime without changing {@link Human} itself
 * - the Strategy pattern, applied the same way the course's FlyBehavior
 * example separates "a duck" from "how a duck flies".
 */
public interface FoodSelectionStrategy {

    /**
     * Picks a food item (if any) within {@code searchRadius} of {@code human}.
     * Returns {@link Optional#empty()} rather than throwing when nothing
     * suitable is in range, leaving the caller to decide what "no food" means.
     */
    Optional<Food> selectFood(World world, Human human, double searchRadius);

    /**
     * Picks one of the three built-in strategies at random. World and Main
     * use this so a spawned population is a genuine mix of food-seeking
     * behaviours, rather than every human defaulting to
     * {@link NearestFoodStrategy} - which food strategy actually survives
     * longest becomes a real, measurable question (see HumanFoodStatistics).
     */
    static FoodSelectionStrategy random(Random random) {
        List<Supplier<FoodSelectionStrategy>> options = List.of(
                NearestFoodStrategy::new,
                HighestValueFoodStrategy::new,
                BalancedFoodStrategy::new);
        return options.get(random.nextInt(options.size())).get();
    }

    /**
     * Picks one of the three built-in strategies at random, but never the
     * same kind as {@code current}. Used when a human adapts away from a
     * strategy that has left it struggling (see Human.adaptFoodStrategyIfStruggling).
     */
    static FoodSelectionStrategy randomOtherThan(Random random, FoodSelectionStrategy current) {
        FoodSelectionStrategy candidate;
        do {
            candidate = random(random);
        } while (candidate.getClass() == current.getClass());
        return candidate;
    }
}
