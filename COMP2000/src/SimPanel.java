import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.List;

public class SimPanel extends JPanel {
    private final List<Building> buildings;
    private final Image backgroundImage;

    public SimPanel(List<Building> buildings) {
        this.buildings = buildings;
        this.setPreferredSize(new Dimension(1000, 750));
        URL backgroundURL = SimPanel.class.getResource("/Background.png");
        if (backgroundURL == null) {
            throw new IllegalStateException("background.png was not found");
        } else {
            this.backgroundImage = (new ImageIcon(backgroundURL)).getImage();
        }
    }

    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D graphics2D = (Graphics2D)graphics.create();

        try {
            graphics2D.drawImage(this.backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);

            for(Building building : this.buildings) {
                building.draw(graphics2D);
            }
        } finally {
            graphics2D.dispose();
        }

    }
}