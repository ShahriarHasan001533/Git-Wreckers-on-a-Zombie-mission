import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.ImageIcon;

/**
 * A soldier that patrols the world and shoots zombies on sight.
 *
 * <p>A military unit is still a {@link Human} as far as the outbreak is
 * concerned, so a {@link Zombie} treats it as a person. Unlike an ordinary
 * human it does not starve, flee, or shelter in buildings. Its movement each
 * tick is delegated to a {@link MovementBehaviour} strategy that is swapped
 * between {@link PatrolBehaviour} and {@link HuntBehaviour} depending on
 * whether a zombie is within {@link #DETECTION_RADIUS}. The supplied front,
 * side, and back PNGs are chosen from the unit's current facing.
 */
public class Military extends Human {

    // --- Tuning constants (all distances in pixels, all times in ticks) ---
    private static final double DETECTION_RADIUS = 260.0;   // how far the unit can "see" zombies
    private static final double FIRING_RANGE = 140.0;        // how close a zombie must be to be shot
    private static final double SPEED = 2.4;                 // pixels moved per tick (faster than a Human)
    private static final int FIRE_INTERVAL_TICKS = 12;       // reload time between shots
    private static final int MUZZLE_FLASH_TICKS = 3;         // how long the tracer line stays drawn
    private static final int CHARACTER_WIDTH = 38;
    private static final int CHARACTER_HEIGHT = 54;
    private static final int TICKS_PER_WALK_FRAME = 8;

    // --- Strategy pattern: two movement behaviours, created once, swapped at runtime ---
    private final MovementBehaviour patrolBehaviour;   // used when no zombie is nearby
    private final MovementBehaviour huntBehaviour;     // used when a zombie has been detected
    private MovementBehaviour currentBehaviour;        // points at whichever one applies this tick

    // --- Combat state ---
    private Zombie target;         // the zombie currently being hunted/shot, or null
    private int fireCooldown;      // ticks left before the unit can fire again
    private int muzzleFlashTicks;  // ticks left to draw the tracer line
    private int kills;             // zombies this unit has shot (shown for debugging/HUD)

    // --- Sprite + walk-animation state (same scheme as Human/Zombie) ---
    private final Image[] frontWalkFrames;
    private final Image[] sideWalkFrames;
    private final Image frontIdleFrame;
    private final Image sideIdleFrame;
    private final Image backFrame;
    private int walkFrame;
    private int ticksOnCurrentFrame;
    private boolean facingLeft;
    private Facing facing = Facing.FRONT;
    private boolean moving;

    /** Creates a unit that patrols the supplied route. */
    public Military(double x, double y, List<Point2D> patrolRoute) {
        super(x, y);   // reuse Human's construction (identity, position); we override its behaviour below

        // Build both strategy objects up front. update() only ever switches the
        // pointer between them - it never creates new ones.
        this.patrolBehaviour = new PatrolBehaviour(patrolRoute);
        this.huntBehaviour = new HuntBehaviour();
        this.currentBehaviour = patrolBehaviour;

        frontWalkFrames = new Image[]{
                loadImage("/MilitaryFrontWalk1.png"),
                loadImage("/MilitaryFrontWalk2.png"),
                loadImage("/MilitaryFrontWalk3.png"),
                loadImage("/MilitaryFrontWalk4.png")
        };
        sideWalkFrames = new Image[]{
                loadImage("/MilitaryWalk1.png"),
                loadImage("/MilitaryWalk2.png"),
                loadImage("/MilitaryWalk3.png")
        };
        frontIdleFrame = loadImage("/MilitaryFrontIdle.png");
        sideIdleFrame = loadImage("/MilitarySideIdle.png");
        backFrame = loadImage("/MilitaryBack.png");
    }

    /**
     * Convenience constructor: patrols a default diamond of 4 waypoints around
     * its spawn point. Delegates to the main constructor above.
     */
    public Military(double x, double y) {
        this(x, y, defaultRoute(x, y));
    }

    /** Four waypoints (left, up, right, down) forming a diamond around (x, y). */
    private static List<Point2D> defaultRoute(double x, double y) {
        List<Point2D> route = new ArrayList<>();
        route.add(new Point2D.Double(x - 160, y));
        route.add(new Point2D.Double(x, y - 110));
        route.add(new Point2D.Double(x + 160, y));
        route.add(new Point2D.Double(x, y + 110));
        return route;
    }

    /** The zombie this unit is currently hunting (used by {@link HuntBehaviour}). */
    public Zombie getTarget() {
        return target;
    }

    public int getKills() {
        return kills;
    }

    /** "Patrolling" or "Engaging" - the label of the active strategy. */
    public String getBehaviourName() {
        return currentBehaviour.describe();
    }

    /**
     * One tick of soldier AI. Called by {@link World#update()} through the
     * polymorphic {@code Entity.update(World)} contract.
     */
    @Override
    public void update(World world) {
        if (!isActive()) {
            return;
        }

        moving = false;

        // Step 1: sense - find the closest zombie within DETECTION_RADIUS (null if none).
        target = findNearestZombie(world);

        // Step 2: decide - swap the movement strategy based on the situation.
        //         This one line is the Strategy pattern in action.
        currentBehaviour = (target == null) ? patrolBehaviour : huntBehaviour;

        // Step 3: act - let the chosen strategy do this tick's movement.
        currentBehaviour.move(this, world);

        // Step 4: tick down the reload timer and the tracer-flash timer.
        if (fireCooldown > 0) {
            fireCooldown--;
        }
        if (muzzleFlashTicks > 0) {
            muzzleFlashTicks--;
        }

        // Step 5: try to shoot. shoot() throws if there is nothing valid in
        //         range; that is the normal "still patrolling" case, so we
        //         swallow the checked exception here.
        try {
            shoot();
        } catch (NoTargetException noTarget) {
            // Nothing to shoot this tick; the unit simply keeps patrolling.
        }
    }

    /**
     * Fires at the current target when it is alive and inside
     * {@link #FIRING_RANGE}. A hit deactivates the zombie so {@link World}
     * removes it after the tick.
     *
     * @throws NoTargetException if there is no live zombie in firing range
     */
    public void shoot() throws NoTargetException {
        // No target, target already dead, or target out of range -> not a shot.
        // Signalled with a custom checked exception rather than a silent return.
        if (target == null
                || !target.isActive()
                || distanceTo(target) > FIRING_RANGE) {
            throw new NoTargetException(
                    "No zombie within firing range of the military unit at ("
                            + (int) getX() + ", " + (int) getY() + ")");
        }

        // In range but still reloading - valid target, just no shot yet.
        if (fireCooldown > 0) {
            return;
        }

        // Hit: kill the zombie (World sweeps it out after the tick), then reload.
        target.deactivate();
        kills++;
        fireCooldown = FIRE_INTERVAL_TICKS;
        muzzleFlashTicks = MUZZLE_FLASH_TICKS;
    }

    /**
     * Moves one {@link #SPEED} step towards a point, clamped to the world
     * bounds, and updates the facing used for sprite selection. Called by
     * the {@link MovementBehaviour} strategies.
     */
    public void moveTowards(double targetX, double targetY, World world) {
        // Vector from here to the target point.
        double xDifference = targetX - getX();
        double yDifference = targetY - getY();
        double distance = Math.hypot(xDifference, yDifference);
        if (distance < 1.0e-6) {
            return;   // already there - nothing to do
        }

        // Normalise to a unit direction (length 1).
        double directionX = xDifference / distance;
        double directionY = yDifference / distance;

        // Pick which sprite set to show from the dominant axis of travel.
        if (Math.abs(directionY) > Math.abs(directionX)) {
            facing = directionY < 0 ? Facing.BACK : Facing.FRONT;
        } else if (Math.abs(directionX) > 0.01) {
            facing = Facing.SIDE;
            facingLeft = directionX < 0;   // side sprite is drawn mirrored when true
        }

        // Step one SPEED increment, then clamp so the unit stays on screen.
        double nextX = getX() + directionX * SPEED;
        double nextY = getY() + directionY * SPEED;
        nextX = Math.max(CHARACTER_WIDTH / 2.0,
                Math.min(world.getWidth() - CHARACTER_WIDTH / 2.0, nextX));
        nextY = Math.max(CHARACTER_HEIGHT / 2.0,
                Math.min(world.getHeight() - CHARACTER_HEIGHT / 2.0, nextY));
        setPosition(nextX, nextY);   // inherited from Entity; only the entity moves itself

        moving = true;
        advanceWalkAnimation();
    }

    /**
     * The unit's "eyes": asks the World for everything within DETECTION_RADIUS,
     * keeps the live zombies, and returns the closest one (or null).
     */
    private Zombie findNearestZombie(World world) {
        return world.getNearby(this, DETECTION_RADIUS).stream()
                .filter(entity -> entity instanceof Zombie)   // zombies only
                .map(entity -> (Zombie) entity)
                .filter(Entity::isActive)                      // not already dead
                .min(Comparator.comparingDouble(this::distanceTo))  // nearest wins
                .orElse(null);
    }

    private void advanceWalkAnimation() {
        ticksOnCurrentFrame++;
        if (ticksOnCurrentFrame >= TICKS_PER_WALK_FRAME) {
            // Twelve is a shared cycle for four front frames and three side frames.
            walkFrame = (walkFrame + 1) % 12;
            ticksOnCurrentFrame = 0;
        }
    }

    /**
     * Draws the soldier sprite, then the combat overlay on top. Called by
     * {@link SimPanel} through the polymorphic {@code Entity.draw(Graphics2D)}
     * contract.
     */
    @Override
    public void draw(Graphics2D graphics) {
        if (!isActive()) {
            return;
        }

        Image frame = getCurrentFrame();
        // Position is the unit's centre, so offset by half the sprite size.
        // The extra -2 on odd frames is a small vertical bob while walking.
        int left = (int) getX() - CHARACTER_WIDTH / 2;
        int top = (int) getY() - CHARACTER_HEIGHT / 2
                - (moving && walkFrame % 2 == 1 ? 2 : 0);

        if (facing == Facing.SIDE && facingLeft) {
            // A negative width mirrors the current frame horizontally.
            graphics.drawImage(frame, left + CHARACTER_WIDTH, top,
                    -CHARACTER_WIDTH, CHARACTER_HEIGHT, null);
        } else {
            graphics.drawImage(frame, left, top,
                    CHARACTER_WIDTH, CHARACTER_HEIGHT, null);
        }

        drawCombatOverlay(graphics);
    }

    /** Draws the firing-range ring while engaging and a tracer on firing. */
    private void drawCombatOverlay(Graphics2D graphics) {
        int centreX = (int) getX();
        int centreY = (int) getY();

        if (currentBehaviour == huntBehaviour) {
            graphics.setColor(new Color(200, 40, 40, 110));
            graphics.setStroke(new BasicStroke(1.0f));
            graphics.drawOval(
                    centreX - (int) FIRING_RANGE,
                    centreY - (int) FIRING_RANGE,
                    (int) FIRING_RANGE * 2,
                    (int) FIRING_RANGE * 2);
        }

        if (muzzleFlashTicks > 0 && target != null) {
            graphics.setColor(new Color(255, 220, 90));
            graphics.setStroke(new BasicStroke(2.0f));
            graphics.drawLine(centreX, centreY - CHARACTER_HEIGHT / 4,
                    (int) target.getX(), (int) target.getY());
        }
    }

    /** Picks the sprite for the current facing + moving/idle + walk-cycle frame. */
    private Image getCurrentFrame() {
        if (!moving) {
            if (facing == Facing.SIDE) {
                return sideIdleFrame;
            }
            return facing == Facing.BACK ? backFrame : frontIdleFrame;
        }

        if (facing == Facing.SIDE) {
            return sideWalkFrames[walkFrame % sideWalkFrames.length];
        }
        if (facing == Facing.BACK) {
            return backFrame;
        }
        return frontWalkFrames[walkFrame % frontWalkFrames.length];
    }

    /** Loads a bundled PNG by resource path, with a fallback to the src folder. */
    private Image loadImage(String imagePath) {
        URL imageUrl = Military.class.getResource(imagePath);
        if (imageUrl != null) {
            return new ImageIcon(imageUrl).getImage();
        }

        // When Java is run with only the generated out directory on its
        // classpath, image assets remain in src rather than being duplicated.
        String fileName = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
        File sourceAsset = new File("src", fileName);
        if (!sourceAsset.isFile()) {
            sourceAsset = new File("COMP2000/src", fileName);
        }
        if (!sourceAsset.isFile()) {
            throw new IllegalStateException(imagePath + " was not found");
        }
        return new ImageIcon(sourceAsset.getAbsolutePath()).getImage();
    }

    /** Which set of sprites to show, chosen from the direction of travel. */
    private enum Facing {
        FRONT,
        SIDE,
        BACK
    }
}
