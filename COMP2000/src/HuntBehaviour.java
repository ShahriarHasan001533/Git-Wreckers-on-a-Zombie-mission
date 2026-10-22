/**
 * Charges straight at the unit's current target zombie so that it can be
 * brought within firing range. This is the {@link Military} unit's
 * behaviour whenever a zombie has been detected.
 */
public class HuntBehaviour implements MovementBehaviour {

    @Override
    public void move(Military unit, World world) {
        // The target was already chosen in Military.update(); this strategy
        // only does the chasing.
        Zombie target = unit.getTarget();
        if (target == null || !target.isActive()) {
            return;   // nothing to chase (e.g. it was just shot)
        }
        unit.moveTowards(target.getX(), target.getY(), world);
    }

    @Override
    public String describe() {
        return "Engaging";   // shown in the HUD
    }
}
