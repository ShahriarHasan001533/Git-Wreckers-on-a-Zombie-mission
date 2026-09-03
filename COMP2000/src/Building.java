<<<<<<< Updated upstream
import javax.swing.*;
=======
import javax.imageio.ImageIO;
>>>>>>> Stashed changes
import java.awt.*;
import java.io.IOException;
import java.net.URL;

public class Building extends Entity {

    private final Image image;
    private final int width;
    private final int height;
<<<<<<< Updated upstream
    public Building (double x, double y, int width, int height) {
        super(x, y);
      //  this.image = image;
        this.width = width;
        this.height = height;
        URL imageURL = Building.class.getResource("/building.png");
        this.image = new ImageIcon(imageURL).getImage();
=======
    private final int capacity;

    private int occupantCount;

    public Building(
            double x,
            double y,
            int width,
            int height
    ) {
        this(x, y, width, height, 10);
>>>>>>> Stashed changes
    }

    public Building(
            double x,
            double y,
            int width,
            int height,
            int capacity
    ) {
        super(x, y);

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                    "Building dimensions must be positive"
            );
        }

        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "Building capacity must be positive"
            );
        }

        this.width = width;
        this.height = height;
        this.capacity = capacity;
        this.occupantCount = 0;

        Image loadedImage;

        try {
            URL imageURL =
                    Building.class.getResource("/Building.png");

            if (imageURL == null) {
                throw new IOException(
                        "Building.png could not be found"
                );
            }

            loadedImage = ImageIO.read(imageURL);

            if (loadedImage == null) {
                throw new IOException(
                        "Building.png is corrupted or unsupported"
                );
            }

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to load the building image",
                    exception
            );
        }

        this.image = loadedImage;
    }

    public boolean isFull() {
        return occupantCount >= capacity;
    }

    public boolean canEnter() {
        return !isFull();
    }

    public boolean enter() {
        if (isFull()) {
            return false;
        }

        occupantCount++;
        return true;
    }

    public boolean leave() {
        if (occupantCount == 0) {
            return false;
        }

        occupantCount--;
        return true;
    }

    public boolean containsPoint(
            double pointX,
            double pointY
    ) {
        return pointX >= getX()
                && pointX < getX() + width
                && pointY >= getY()
                && pointY < getY() + height;
    }

    public double getEntranceX() {
        return getX() + width / 2.0;
    }

    public double getEntranceY() {
        return getY() + height;
    }

    public int getOccupantCount() {
        return occupantCount;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
<<<<<<< Updated upstream
}

=======

    @Override
    public void update(World world) {
        // Buildings are stationary, so there is
        // nothing to update each simulation tick.
    }
>>>>>>> Stashed changes

    @Override
    public void draw(Graphics2D graphics) {
        graphics.drawImage(
                image,
                (int) getX(),
                (int) getY(),
                width,
                height,
                null
        );
    }
}