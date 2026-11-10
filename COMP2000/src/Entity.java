import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;
import java.net.URL;
import javax.swing.ImageIcon;

public abstract class Entity {

    private double x;
    private double y;
    private boolean active;

    protected Entity(double x, double y) {
        this.x = x;
        this.y = y;
        this.active = true;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    protected void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }

    public double distanceTo(Entity other) {
        double differenceX = other.x - x;
        double differenceY = other.y - y;

        return Math.sqrt(
                differenceX * differenceX
                        + differenceY * differenceY
        );
    }

    public abstract void update(World world);

    public abstract void draw(Graphics2D graphics);

    /**
     * Loads an image from the classpath, falling back to a src-relative file
     * when only compiled classes are on the classpath (e.g. when Java is run
     * with just the generated out directory on it). Shared here because
     * {@link Human} and {@link Food} previously each kept an almost
     * identical private copy of this method.
     */
    protected static Image loadImage(String imagePath) {
        URL imageUrl = Entity.class.getResource(imagePath);
        if (imageUrl != null) {
            return new ImageIcon(imageUrl).getImage();
        }

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
}
