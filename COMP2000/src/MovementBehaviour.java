/**
 * Strategy for how a {@link Military} unit moves during a single simulation
 * tick. Concrete implementations are swapped at runtime by
 * {@link Military#update(World)} depending on the tactical situation, for
 * example patrolling a route versus hunting a nearby zombie.
 *
 * <p>This is the "strategy" interface of the Strategy design pattern:
 * {@link PatrolBehaviour} and {@link HuntBehaviour} are the interchangeable
 * implementations, and {@code Military} is the context that holds one and
 * calls into it. Adding a new behaviour (e.g. retreat) means writing one new
 * class - {@code Military.update()} does not change.
 */
public interface MovementBehaviour {

    /** Moves the given unit one step for the current tick. */
    void move(Military unit, World world);

    /** Short human-readable name for this behaviour, shown in the HUD. */
    String describe();
}
