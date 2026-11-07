import java.util.Optional;

/**
 * Default strategy: always go for whichever active food is physically
 * closest, ignoring its energy value. Delegates to Human's bounded generic
 * entity finder, so the same generic method backs both this strategy and
 * any future nearest-of-type lookup Human might need.
 */
public class NearestFoodStrategy implements FoodSelectionStrategy {

    @Override
    public Optional<Food> selectFood(World world, Human human, double searchRadius) {
        try {
            return Optional.of(
                    human.findNearestEntity(world, searchRadius, Food.class, Food::isActive));
        } catch (TargetNotFoundException exception) {
            return Optional.empty();
        }
    }
}
