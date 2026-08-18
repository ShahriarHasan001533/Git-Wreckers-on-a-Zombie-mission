import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.net.URL;
import java.util.List;

public class SimPanel extends JPanel {

    private final List<Building> buildings;
    private final Image backgroundImage;

    public SimPanel(List<Building> buildings) {
        this.buildings = buildings;

        setPreferredSize(
                new Dimension(1000, 750)
        );

        URL backgroundURL =
                SimPanel.class.getResource(
                        "/background.png"
                );

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
                (Graphics2D) graphics.create();

        try {
            // Draw background first
            graphics2D.drawImage(
                    backgroundImage,
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    this
            );

            // Draw every building
            for (Building building : buildings) {
                building.draw(graphics2D);
            }

        } finally {
            graphics2D.dispose();
        }
    }
}