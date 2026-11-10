import java.util.Comparator;
import java.util.Optional;

/**
 * Efficiency strategy: picks the food with the best energy-per-distance
 * payoff, trading off how far away it is against how much it is worth. A
 * compromise between {@link NearestFoodStrategy} and
 * {@link HighestValueFoodStrategy}.
 */
public class BalancedFoodStrategy implements FoodSelectionStrategy {

    @Override
    public Optional<Food> selectFood(World world, Human human, double searchRadius) {
        return world.getNearby(human, searchRadius).stream()
                .filter(Food.class::isInstance)
                .map(Food.class::cast)
                .max(Comparator.comparingDouble(food ->
                        food.getEnergyValue() / Math.max(1.0, human.distanceTo(food))));
    }
}
