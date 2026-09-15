import java.awt.geom.Point2D;
import java.util.List;

/**
 * Walks a fixed loop of waypoints. Once the unit reaches the current
 * waypoint it advances to the next one, wrapping back to the start at the
 * end of the route. This is the {@link Military} unit's behaviour whenever
 * no zombie is within detection range.
 */
public class PatrolBehaviour implements MovementBehaviour {

    /** How close counts as "reached this waypoint". */
    private static final double ARRIVAL_DISTANCE = 6.0;

    private final List<Point2D> route;   // the fixed loop of points to walk
    private int waypointIndex;           // which point we are currently heading to

    public PatrolBehaviour(List<Point2D> route) {
        // Guard: a patrol with no waypoints makes no sense - fail fast.
        if (route == null || route.isEmpty()) {
            throw new IllegalArgumentException(
                    "Patrol route must contain at least one waypoint");
        }
        this.route = route;
        this.waypointIndex = 0;
    }

    @Override
    public void move(Military unit, World world) {
        // Head towards the current waypoint (Military does the actual stepping).
        Point2D waypoint = route.get(waypointIndex);
        unit.moveTowards(waypoint.getX(), waypoint.getY(), world);

        // If we have arrived, advance to the next point; wrap round at the end
        // with modulo so the patrol loops forever.
        double distance = Math.hypot(
                waypoint.getX() - unit.getX(),
                waypoint.getY() - unit.getY());
        if (distance <= ARRIVAL_DISTANCE) {
            waypointIndex = (waypointIndex + 1) % route.size();
        }
    }

    @Override
    public String describe() {
        return "Patrolling";   // shown in the HUD
    }
}
