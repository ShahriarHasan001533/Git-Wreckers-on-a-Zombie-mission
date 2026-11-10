import java.util.Comparator;
import java.util.Optional;

/**
 * Greedy strategy: ignores distance entirely and goes for whichever food in
 * range has the highest energy value, even if a closer, lower-value item
 * was available. Useful for a "reckless" human variant.
 */
public class HighestValueFoodStrategy implements FoodSelectionStrategy {

    @Override
    public Optional<Food> selectFood(World world, Human human, double searchRadius) {
        return world.getNearby(human, searchRadius).stream()
                .filter(Food.class::isInstance)
                .map(Food.class::cast)
                .max(Comparator.comparingDouble(Food::getEnergyValue));
    }
}
