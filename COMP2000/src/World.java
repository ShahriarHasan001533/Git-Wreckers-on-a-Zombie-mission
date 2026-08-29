import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class World {

    private final List<Entity> entities;
    private final int width;
    private final int height;
    private long tick;

    public World(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                    "World dimensions must be positive"
            );
        }

        this.width = width;
        this.height = height;
        this.entities = new ArrayList<>();
        this.tick = 0;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getTick() {
        return tick;
    }

    public void addEntity(Entity entity) {
        entities.add(Objects.requireNonNull(entity));
    }

    public List<Entity> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    public List<Entity> getNearby(Entity source, double radius) {
        Objects.requireNonNull(source);

        if (radius < 0) {
            throw new IllegalArgumentException(
                    "Radius cannot be negative"
            );
        }

        List<Entity> nearbyEntities = new ArrayList<>();

        for (Entity entity : entities) {
            if (entity == source) {
                continue;
            }

            if (!entity.isActive()) {
                continue;
            }

            if (source.distanceTo(entity) <= radius) {
                nearbyEntities.add(entity);
            }
        }

        return nearbyEntities;
    }

    public void update() {
        for(Entity entity : List.copyOf(entities)) {
            if(entity.isActive()) {
                entity.update(this);
            }
        }

        removeInactiveEntities();
        tick++;
    }

    public void removeInactiveEntities() {
        entities.removeIf(entity -> !entity.isActive());
    }

    
}