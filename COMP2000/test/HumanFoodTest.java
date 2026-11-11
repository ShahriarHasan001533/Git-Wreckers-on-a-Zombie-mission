public final class HumanFoodTest {

    private HumanFoodTest() {
    }

    /**
     * A non-Entity Edible: nothing to walk towards, no position, no sprite -
     * proof that Edible's "future item that isn't an Entity" claim is real,
     * not just a comment (see testEatDirectlyWorksForNonFoodEdible below).
     */
    private static final class CarePackage implements Edible {
        private boolean consumed;

        @Override
        public double getEnergyValue() {
            return 40;
        }

        @Override
        public boolean isConsumed() {
            return consumed;
        }

        @Override
        public void consume() {
            consumed = true;
        }
    }

    public static void main(String[] args) {
        testEnergyDecreasesAfterUpdate();
        testInvalidFoodEnergyIsRejected();
        testEatingIncreasesEnergyAndConsumesFood();
        testDistantFoodCannotBeEaten();
        testFoodExpiresAfter300Updates();
        testHumanEntersAndLeavesBuilding();
        testStarvationDeactivatesHuman();
        testSimulationAssetsLoadWithoutSourceClasspath();
        testFoodIsEdible();
        testFoodConstructorOverloadUsesDefaultEnergy();
        testFoodStrategiesPickDifferentFood();
        testHumanFoodStatisticsCollectors();
        testAverageEnergyByStrategy();
        testEatDirectlyWorksForNonFoodEdible();
        testParallelStatisticsMatchSequential();
        testRandomStrategyProducesVariety();
        testEnergyRangeCustomCollector();
        System.out.println("HumanFoodTest passed");
    }

    /** Custom Collector.of(...): min/max/count of food energy in one pass. */
    private static void testEnergyRangeCustomCollector() {
        java.util.List<Food> food =
                java.util.List.of(new Food(0, 0, 10), new Food(0, 0, 45), new Food(0, 0, 20));
        EnergyRange range = HumanFoodStatistics.energyRange(food);
        assert range.getMin() == 10;
        assert range.getMax() == 45;
        assert range.getCount() == 3;

        EnergyRange empty = HumanFoodStatistics.energyRange(java.util.List.of());
        assert empty.getMin() == 0;
        assert empty.getMax() == 0;
        assert empty.getCount() == 0;
    }

    /** World/Main rely on this producing a genuine mix, not always the same strategy. */
    private static void testRandomStrategyProducesVariety() {
        java.util.Random random = new java.util.Random(7);
        java.util.Set<Class<?>> seen = new java.util.HashSet<>();
        for (int i = 0; i < 20; i++) {
            seen.add(FoodSelectionStrategy.random(random).getClass());
        }
        assert seen.size() > 1;
    }

    /** Parallel and sequential aggregation should agree on the same data. */
    private static void testParallelStatisticsMatchSequential() {
        java.util.List<Human> humans =
                java.util.List.of(new Human(100, 100), new Human(500, 500), new Human(800, 200));
        double sequentialAverage = HumanFoodStatistics.summarizeEnergy(humans).getAverage();
        assert HumanFoodStatistics.averageEnergyParallel(humans) == sequentialAverage;

        java.util.List<Food> food =
                java.util.List.of(new Food(0, 0, 10), new Food(0, 0, 5), new Food(400, 400, 20));
        assert HumanFoodStatistics.countByFoodTypeParallel(food)
                .equals(HumanFoodStatistics.countByFoodType(food));
    }

    /** Week 11 collectors: groupingBy, partitioningBy, summarizingDouble, mapping. */
    private static void testHumanFoodStatisticsCollectors() {
        Human sheltered = new Human(120, 140);
        Human outInTheOpen = new Human(500, 500);
        Building building = new Building(100, 100, 40, 40, 1);
        assert sheltered.enterBuilding(building);
        java.util.List<Human> humans = java.util.List.of(sheltered, outInTheOpen);

        java.util.Map<Boolean, Long> byShelter = HumanFoodStatistics.countByShelterStatus(humans);
        assert byShelter.get(true) == 1;
        assert byShelter.get(false) == 1;

        java.util.DoubleSummaryStatistics energyStats =
                HumanFoodStatistics.summarizeEnergy(humans);
        assert energyStats.getCount() == 2;

        Food apple = new Food(0, 0, 10);
        Food sameTypeApple = new Food(0, 0, 15);
        java.util.List<Food> food = java.util.List.of(apple, sameTypeApple);
        java.util.Map<FoodType, Long> byType = HumanFoodStatistics.countByFoodType(food);
        assert byType.get(apple.getType()) == 2;

        java.util.Map<FoodType, Double> energyByType =
                HumanFoodStatistics.totalEnergyByFoodType(food);
        assert energyByType.get(apple.getType()) == 25.0;
    }

    /** averageEnergyByStrategy should report each strategy's own humans' average energy. */
    private static void testAverageEnergyByStrategy() {
        Human nearestA = new Human(0, 0, new NearestFoodStrategy());
        Human nearestB = new Human(0, 0, new NearestFoodStrategy());
        Human highestValue = new Human(0, 0, new HighestValueFoodStrategy());
        nearestB.restoreEnergy(5); // pushes against MAX_ENERGY, so the average stays exactly 100
        java.util.List<Human> humans = java.util.List.of(nearestA, nearestB, highestValue);

        java.util.Map<String, Double> averages = HumanFoodStatistics.averageEnergyByStrategy(humans);

        assert averages.size() == 2;
        assert averages.get("NearestFoodStrategy") == 100.0;
        assert averages.get("HighestValueFoodStrategy") == 100.0;
    }

    /**
     * Strategy pattern: swapping a human's FoodSelectionStrategy at runtime
     * changes which food it picks, without changing Human itself - mirrors
     * the course's RubberDuck.setFlyBehavior() example.
     */
    private static void testFoodStrategiesPickDifferentFood() {
        World world = new World(1000, 1000);
        Human human = new Human(100, 100);
        Food closerLowValue = new Food(110, 100, 5);
        Food fartherHighValue = new Food(200, 100, 90);
        world.addEntity(human);
        world.addEntity(closerLowValue);
        world.addEntity(fartherHighValue);

        assert new NearestFoodStrategy().selectFood(world, human, 250).get() == closerLowValue;
        assert new HighestValueFoodStrategy().selectFood(world, human, 250).get()
                == fartherHighValue;

        human.setFoodStrategy(new HighestValueFoodStrategy());
        assert human.getFoodStrategy().selectFood(world, human, 250).get() == fartherHighValue;
    }

    /** The two-argument constructor overload should default to DEFAULT_ENERGY_VALUE. */
    private static void testFoodConstructorOverloadUsesDefaultEnergy() {
        Food defaultEnergyFood = new Food(10, 10);
        Food customEnergyFood = new Food(10, 10, 99);

        assert defaultEnergyFood.getEnergyValue() == Food.DEFAULT_ENERGY_VALUE;
        assert customEnergyFood.getEnergyValue() == 99;
    }

    /** Food implements the Edible contract independently of its Entity behaviour. */
    private static void testFoodIsEdible() {
        Edible food = new Food(0, 0, 20);

        assert food.getEnergyValue() == 20;
        assert !food.isConsumed();
        assert food.describeEnergy().contains("20.0");

        food.consume();
        assert food.isConsumed();
        assert food.describeEnergy().equals("already consumed");
    }

    /**
     * eatDirectly works for a non-Entity, non-Food Edible, with no distance
     * check and no position - the thing Human.eat(Food) alone could never do.
     */
    private static void testEatDirectlyWorksForNonFoodEdible() {
        Human human = new Human(0, 0);
        double energyBeforeEating = human.getEnergy();
        CarePackage carePackage = new CarePackage();

        assert human.eatDirectly(carePackage);
        assert human.getEnergy() >= energyBeforeEating;
        assert carePackage.isConsumed();
        assert !human.eatDirectly(carePackage); // already consumed
    }

    private static void testEnergyDecreasesAfterUpdate() {
        Human human = new Human(500, 500);
        double initialEnergy = human.getEnergy();

        human.update(new World(1000, 1000));

        assert human.getEnergy() < initialEnergy;
    }

    private static void testInvalidFoodEnergyIsRejected() {
        boolean rejected = false;
        try {
            new Food(0, 0, 0);
        } catch (IllegalArgumentException exception) {
            rejected = true;
        }

        assert rejected;
    }

    private static void testEatingIncreasesEnergyAndConsumesFood() {
        Human human = new Human(100, 100);
        human.update(new World(1000, 1000));
        double energyBeforeEating = human.getEnergy();
        Food food = new Food(100, 100, 20);

        assert human.eat(food);
        assert human.getEnergy() > energyBeforeEating;
        assert !food.isActive();
    }

    private static void testDistantFoodCannotBeEaten() {
        Human human = new Human(100, 100);
        Food food = new Food(200, 100, 20);

        assert !human.eat(food);
        assert food.isActive();
    }

    private static void testFoodExpiresAfter300Updates() {
        Food food = new Food(0, 0, 20);
        World world = new World(1000, 1000);

        for (int tick = 0; tick < 299; tick++) {
            food.update(world);
        }
        assert food.isActive();

        food.update(world);
        assert !food.isActive();
    }

    private static void testHumanEntersAndLeavesBuilding() {
        Human human = new Human(120, 140);
        Building building = new Building(100, 100, 40, 40, 1);

        assert human.enterBuilding(building);
        assert human.isHidden();
        assert human.getShelter() == building;
        assert building.getOccupantCount() == 1;

        human.leaveBuilding();
        assert !human.isHidden();
        assert human.getShelter() == null;
        assert building.getOccupantCount() == 0;
        assert human.getX() == building.getEntranceX();
        assert human.getY() == building.getEntranceY();
    }

    private static void testStarvationDeactivatesHuman() {
        Human human = new Human(500, 500);
        World world = new World(1000, 1000);

        for (int tick = 0; tick < 600 && human.isActive(); tick++) {
            human.update(world);
        }

        assert !human.isActive();
    }

    private static void testSimulationAssetsLoadWithoutSourceClasspath() {
        World world = new World(1000, 750);

        new Building(155, 125, 150, 100);
        new SimPanel(world);
    }
}
