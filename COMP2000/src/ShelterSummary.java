import java.util.List;
import java.util.stream.Collector;

/** Immutable shelter totals captured on the simulation thread. */
public final class ShelterSummary {
    private final long buildingCount;
    private final long capacity;
    private final long occupiedPlaces;
    private final long fullBuildings;

    private ShelterSummary(long[] totals) {
        buildingCount = totals[0];
        capacity = totals[1];
        occupiedPlaces = totals[2];
        fullBuildings = totals[3];
    }

    static ShelterSummary capture(List<Entity> entities) {
        // Keep active buildings, then combine their shelter data in one pass.
        return entities.stream()
                .filter(entity -> entity.isActive() && entity instanceof Building)
                .map(entity -> (Building) entity)
                .collect(collector());
    }

    /** Each accumulator owns its totals; the finisher copies them into immutable fields. */
    static Collector<Building, long[], ShelterSummary> collector() {
        return Collector.of(
                () -> new long[4],
                (totals, building) -> {
                    totals[0]++;
                    totals[1] += building.getCapacity();
                    totals[2] += building.getOccupantCount();
                    if (building.isFull()) totals[3]++;
                },
                (left, right) -> {
                    for (int index = 0; index < left.length; index++) {
                        left[index] += right[index];
                    }
                    return left;
                },
                ShelterSummary::new
        );
    }

    public long getBuildingCount() { return buildingCount; }
    public long getCapacity() { return capacity; }
    public long getOccupiedPlaces() { return occupiedPlaces; }
    /** Physical vacancies; an admission policy may restrict who can use them. */
    public long getAvailablePlaces() { return capacity - occupiedPlaces; }
    public long getFullBuildings() { return fullBuildings; }
}