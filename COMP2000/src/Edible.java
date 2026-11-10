/**
 * A contract for anything a human can consume for energy. Pulling this out
 * as an interface (rather than piling more behaviour onto {@link Entity})
 * keeps "can be eaten" separate from "exists in the world": a future item
 * that is not an {@link Entity} at all could still be Edible.
 */
public interface Edible {

    /** How much energy a human gains from consuming this. */
    double getEnergyValue();

    /** Whether this has already been eaten. */
    boolean isConsumed();

    /** Marks this as eaten. */
    void consume();

    /** Default method (Java 8+): a human-readable summary, free for every implementer. */
    default String describeEnergy() {
        return isConsumed() ? "already consumed" : "worth " + getEnergyValue() + " energy";
    }
}
