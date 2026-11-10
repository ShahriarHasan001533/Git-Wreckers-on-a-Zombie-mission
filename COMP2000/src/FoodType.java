/**
 * The three kinds of food dropped into the simulation. Each carries its own
 * sprite path and is picked deterministically from a drop's position, so
 * reloading the same world looks the same. Having this as a real enum field
 * on {@link Food} (rather than only choosing an image path inline) is what
 * lets {@link HumanFoodStatistics} group and count food by kind.
 */
public enum FoodType {
    APPLE("/HumanFoodAssets/FoodApple.png"),
    PIZZA("/HumanFoodAssets/FoodPizza.png"),
    BURGER("/HumanFoodAssets/FoodBurger.png");

    private final String spritePath;

    FoodType(String spritePath) {
        this.spritePath = spritePath;
    }

    String getSpritePath() {
        return spritePath;
    }

    /** Deterministic pick from a drop's position. */
    static FoodType fromPosition(double x, double y) {
        FoodType[] types = values();
        int index = Math.floorMod((int) Math.round(x * 31 + y * 17), types.length);
        return types[index];
    }
}
