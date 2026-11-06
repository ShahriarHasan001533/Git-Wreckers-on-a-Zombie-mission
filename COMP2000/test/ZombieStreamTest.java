import java.util.List;
import java.util.stream.Collectors;

public final class ZombieStreamTest {

    private ZombieStreamTest() {
    }

    public static void main(String[] args) {
        testVisibleCiviliansSkipsSoldiersHiddenAndFarHumans();
        testFindTargetPicksClosestCivilian();
        testFindTargetThrowsWhenNobodyIsInView();
        testPreyScoreRewardsCloserCivilians();
        testTotalPreyScoreMatchesSequentialSum();
        testInfectedHumanBecomesZombie();
        testZombieGathersOutsideOccupiedBuilding();
        testNearestSoldierIgnoresCiviliansAndFarSoldiers();
        testZombieJoinsNeighboursHunt();
        testWeakHumanIsPreferredOverCloserHealthyOne();
        testZombieStarvesWithoutInfecting();
        testRetreatingZombieSlidesAlongWall();
        System.out.println("ZombieStreamTest passed");
    }

    private static void testZombieJoinsNeighboursHunt() {
        World world = new World(1000, 750);
        Zombie hunter = new Zombie(300, 400);
        Zombie follower = new Zombie(300, 560);
        world.addEntity(hunter);
        world.addEntity(follower);
        world.addEntity(new Human(560, 380));

        assert follower.visibleCivilians(world).count() == 0;
        assert follower.alertedTarget(world).isEmpty();

        hunter.update(world);

        assert follower.alertedTarget(world).isPresent();
    }

    private static void testWeakHumanIsPreferredOverCloserHealthyOne()
            throws AssertionError {
        World world = new World(1000, 750);
        Zombie zombie = new Zombie(400, 400);
        Human healthy = new Human(470, 400);
        Human weak = new Human(520, 400);
        world.addEntity(zombie);
        world.addEntity(healthy);
        world.addEntity(weak);

        try {
            assert zombie.findTarget(world) == healthy;

            World empty = new World(1000, 750);
            for (int tick = 0; tick < 450; tick++) {
                weak.update(empty);
            }
            weak.setPosition(520, 400);

            assert weak.getEnergy() < 20;
            assert zombie.findTarget(world) == weak;
        } catch (TargetNotFoundException exception) {
            throw new AssertionError("a target was in view", exception);
        }
    }

    private static void testZombieStarvesWithoutInfecting() {
        World world = new World(1000, 750);
        Zombie zombie = new Zombie(500, 375);
        world.addEntity(zombie);

        for (int tick = 0; tick < 2400; tick++) {
            zombie.update(world);
        }
        assert zombie.isActive();

        for (int tick = 0; tick < 200; tick++) {
            zombie.update(world);
        }
        assert !zombie.isActive();
    }

    private static void testRetreatingZombieSlidesAlongWall() {
        World world = new World(1000, 750);
        Zombie zombie = new Zombie(20, 400);
        world.addEntity(zombie);
        world.addEntity(new Military(160, 400));

        double startY = zombie.getY();
        for (int tick = 0; tick < 30; tick++) {
            zombie.update(world);
        }

        assert zombie.getX() >= 19;
        assert Math.abs(zombie.getY() - startY) > 20;
    }

    private static void testZombieGathersOutsideOccupiedBuilding() {
        World world = new World(1000, 750);
        Building building = new Building(500, 300, 150, 100);
        Human sheltering = new Human(560, 380);
        Zombie zombie = new Zombie(300, 500);
        world.addEntity(building);
        world.addEntity(sheltering);
        world.addEntity(zombie);
        sheltering.enterBuilding(building);

        assert zombie.occupiedBuildingNearby(world).isPresent();

        for (int tick = 0; tick < 400; tick++) {
            zombie.update(world);
        }

        double distance = Math.hypot(
                building.getEntranceX() - zombie.getX(),
                building.getEntranceY() - zombie.getY());
        assert distance < 40;
    }

    private static void testNearestSoldierIgnoresCiviliansAndFarSoldiers() {
        World world = new World(1000, 750);
        Zombie zombie = new Zombie(400, 400);
        Military close = new Military(500, 400);
        world.addEntity(zombie);
        world.addEntity(new Human(410, 400));
        world.addEntity(new Military(650, 400));
        world.addEntity(close);
        assert zombie.nearestSoldier(world).get() == close;

        World faraway = new World(1000, 750);
        Zombie alone = new Zombie(100, 100);
        faraway.addEntity(alone);
        faraway.addEntity(new Military(900, 700));
        assert alone.nearestSoldier(faraway).isEmpty();
    }

    private static void testVisibleCiviliansSkipsSoldiersHiddenAndFarHumans() {
        World world = new World(1000, 750);
        Zombie zombie = new Zombie(400, 400);
        Human visible = new Human(450, 400);
        Human hidden = new Human(420, 400);
        Human far = new Human(900, 700);
        hidden.enterBuilding(new Building(0, 0, 100, 100));
        world.addEntity(zombie);
        world.addEntity(visible);
        world.addEntity(hidden);
        world.addEntity(far);
        world.addEntity(new Military(410, 400));

        List<Human> seen = zombie.visibleCivilians(world)
                .collect(Collectors.toList());

        assert seen.size() == 1;
        assert seen.get(0) == visible;
    }

    private static void testFindTargetPicksClosestCivilian() throws AssertionError {
        World world = new World(1000, 750);
        Zombie zombie = new Zombie(400, 400);
        Human near = new Human(430, 400);
        world.addEntity(zombie);
        world.addEntity(new Human(600, 400));
        world.addEntity(near);

        try {
            assert zombie.findTarget(world) == near;
        } catch (TargetNotFoundException exception) {
            throw new AssertionError("a target was in view", exception);
        }
    }

    private static void testFindTargetThrowsWhenNobodyIsInView() {
        World world = new World(1000, 750);
        Zombie zombie = new Zombie(400, 400);
        world.addEntity(zombie);

        boolean thrown = false;
        try {
            zombie.findTarget(world);
        } catch (TargetNotFoundException exception) {
            thrown = true;
        }

        assert thrown;
    }

    private static void testPreyScoreRewardsCloserCivilians() {
        World crowd = new World(1000, 750);
        Zombie zombie = new Zombie(400, 400);
        crowd.addEntity(zombie);
        crowd.addEntity(new Human(430, 400));
        crowd.addEntity(new Human(550, 400));

        World single = new World(1000, 750);
        Zombie other = new Zombie(400, 400);
        single.addEntity(other);
        single.addEntity(new Human(550, 400));

        // distances 30 and 150 over radius 300: (1 - 0.1) + (1 - 0.5) = 1.4
        assert Math.abs(zombie.preyScore(crowd) - 1.4) < 1e-9;
        assert zombie.preyScore(crowd) > other.preyScore(single);
    }

    private static void testTotalPreyScoreMatchesSequentialSum() {
        World world = new World(1000, 750);
        for (int i = 0; i < 20; i++) {
            world.addEntity(new Zombie(100 + i * 30, 300));
        }
        for (int i = 0; i < 15; i++) {
            world.addEntity(new Human(150 + i * 40, 340));
        }

        double sequential = world.getEntities().stream()
                .filter(entity -> entity instanceof Zombie)
                .mapToDouble(entity -> ((Zombie) entity).preyScore(world))
                .sum();

        assert Math.abs(Zombie.totalPreyScore(world) - sequential) < 1e-6;
    }

    private static void testInfectedHumanBecomesZombie() {
        World world = new World(1000, 750);
        Zombie zombie = new Zombie(400, 400);
        Human human = new Human(450, 400);
        world.addEntity(zombie);
        world.addEntity(human);

        for (int tick = 0; tick < 200 && human.isActive(); tick++) {
            zombie.update(world);
        }

        long zombies = world.getEntities().stream()
                .filter(entity -> entity instanceof Zombie)
                .count();

        assert !human.isActive();
        assert zombies == 2;
    }
}
