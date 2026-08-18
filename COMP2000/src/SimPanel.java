import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.net.URL;

public class SimPanel extends JPanel {

    private final Building building;
    private final Image backgroundImage;

    public SimPanel(Building building) {
        this.building = building;

        setPreferredSize(new Dimension(800, 600));

        URL backgroundURL =
                SimPanel.class.getResource("/Background.png");

        if (backgroundURL == null) {
            throw new IllegalStateException(
                    "background.png was not found"
            );
        }

        backgroundImage =
                new ImageIcon(backgroundURL).getImage();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D graphics2D =
                (Graphics2D) graphics;

        // Draw the background first
        graphics2D.drawImage(
                backgroundImage,
                0,
                0,
                getWidth(),
                getHeight(),
                this
        );

        // Draw the building over the background
        building.draw(graphics2D);
    }
}