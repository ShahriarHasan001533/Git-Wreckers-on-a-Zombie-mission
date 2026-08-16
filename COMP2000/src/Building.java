import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class Building extends Entity {
    private final Image image;
    private final int width;
    private final int height;
    public Building (double x, double y, int width, int height) {
        super(x, y);
      //  this.image = image;
        this.width = width;
        this.height = height;
        URL imageURL = Building.class.getResource("/building.png");
        this.image = new ImageIcon(imageURL).getImage();
    }


    @Override
    public void draw(Graphics2D graphics) {
graphics.drawImage(image,
        (int) getX(),
        (int) getY(),
        width,
        height,
        null);
    }
}


