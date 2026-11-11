import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Collector-based analytics over the humans and food in a world. Kept
 * separate from {@link World} / {@code WorldStatistics} (the team's own
 * observer-driven reporting) so this stays a self-contained demonstration of
 * the Week 11 Collectors covered in the study guide: groupingBy,
 * partitioningBy, summarizingDouble, and downstream collectors such as
 * counting, summingDouble and averagingDouble.
 */
public final class HumanFoodStatistics {

    private HumanFoodStatistics() {
    }

    /** How many humans are currently sheltering vs out in the open. */
    public static Map<Boolean, Long> countByShelterStatus(List<Human> humans) {
        return humans.stream()
                .collect(Collectors.partitioningBy(Human::isHidden, Collectors.counting()));
    }

    /** Min / max / average / total energy across all humans, in one pass. */
    public static DoubleSummaryStatistics summarizeEnergy(List<Human> humans) {
        return humans.stream()
                .collect(Collectors.summarizingDouble(Human::getEnergy));
    }

    /** How many food items of each type are currently active, e.g. {APPLE=3, PIZZA=1}. */
    public static Map<FoodType, Long> countByFoodType(List<Food> food) {
        return food.stream()
                .collect(Collectors.groupingBy(Food::getType, Collectors.counting()));
    }

    /** Total energy value available, grouped by food type. */
    public static Map<FoodType, Double> totalEnergyByFoodType(List<Food> food) {
        return food.stream()
                .collect(Collectors.groupingBy(Food::getType,
                        Collectors.summingDouble(Food::getEnergyValue)));
    }

    /**
     * Average energy of the humans using each food-selection strategy, e.g.
     * {"NearestFoodStrategy"=62.4, "HighestValueFoodStrategy"=48.1}. This is
     * the figure that actually answers "which food strategy is working out
     * best right now" - higher average energy means that strategy's humans
     * are feeding themselves more effectively.
     */
    public static Map<String, Double> averageEnergyByStrategy(List<Human> humans) {
        return humans.stream()
                .collect(Collectors.groupingBy(
                        human -> human.getFoodStrategy().getClass().getSimpleName(),
                        Collectors.averagingDouble(Human::getEnergy)));
    }

    /**
     * Average energy across a (potentially large) population, computed with
     * a parallel stream. Safe because {@code average()} reduces each chunk
     * separately and combines the results - no shared mutable state.
     */
    public static double averageEnergyParallel(List<Human> humans) {
        return humans.parallelStream()
                .mapToDouble(Human::getEnergy)
                .average()
                .orElse(0.0);
    }

    /**
     * Per-type food counts computed with a parallel stream. Uses
     * groupingByConcurrent rather than plain groupingBy, so every thread
     * inserts straight into one shared ConcurrentHashMap instead of each
     * thread building its own private map to be merged afterwards - the
     * "ALSO RIGHT" thread-safe-totals-per-category approach from the course,
     * as opposed to the race-condition-prone plain HashMap + forEach version.
     */
    public static Map<FoodType, Long> countByFoodTypeParallel(List<Food> food) {
        return food.parallelStream()
                .collect(Collectors.groupingByConcurrent(Food::getType, Collectors.counting()));
    }

    /**
     * Min/max/count of food energy values in one pass, built with
     * {@code Collector.of(...)} - a genuine custom collector (its own
     * supplier, accumulator and combiner in {@link EnergyRange}), rather than
     * composing built-in collectors the way every other method here does.
     */
    public static EnergyRange energyRange(List<Food> food) {
        return food.stream()
                .collect(Collector.of(EnergyRange::new, EnergyRange::accept, EnergyRange::combine));
    }
}
