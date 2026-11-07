import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class ShelterSummaryTest {
    public static void main(String[] args) {
        World world = new World(1000, 750);
        check(world.getStatistics().getShelterSummary(), 0, 0, 0, 0);
        Building open = new Building(100, 200, 150, 100, 2);
        Building emergency = new Building(400, 200, 150, 100, 3,
                new EmergencyAdmissionPolicy(2, 60));
        Building inactive = new Building(700, 200, 150, 100, 99);
        inactive.deactivate();
        world.addEntity(open);
        world.addEntity(emergency);
        world.addEntity(inactive);
        world.addEntity(new Food(100, 100, 45));
        open.enter();
        open.enter();
        new Human(475, 300).enterBuilding(emergency);

        AtomicReference<WorldStatistics> received = new AtomicReference<>();
        world.addObserver(received::set);
        world.publishStatistics();
        ShelterSummary snapshot = received.get().getShelterSummary();
        check(snapshot, 2, 5, 3, 1);
        if (snapshot.getAvailablePlaces() != 2) throw new AssertionError("Vacancies include policy-reserved places");
        open.leave();
        world.publishStatistics();
        check(received.get().getShelterSummary(), 2, 5, 2, 0);
        check(snapshot, 2, 5, 3, 1); // Earlier snapshot must not change.

        // Test the combiner independently on stable buildings, not a running world.
        ShelterSummary combined = List.of(open, emergency).parallelStream()
                .collect(ShelterSummary.collector());
        check(combined, 2, 5, 2, 0);
        world.reset();
        world.publishStatistics();
        check(received.get().getShelterSummary(), 0, 0, 0, 0);
        System.out.println("ShelterSummaryTest passed");
    }

    private static void check(ShelterSummary summary, long buildings, long capacity,
                              long occupied, long full) {
        if (summary.getBuildingCount() != buildings || summary.getCapacity() != capacity
                || summary.getOccupiedPlaces() != occupied || summary.getFullBuildings() != full
                || summary.getAvailablePlaces() != capacity - occupied) {
            throw new AssertionError("Incorrect shelter summary");
        }
    }
}