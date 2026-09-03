import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.net.URL;

public class SimPanel extends JPanel {

    private final World world;
    private final Image backgroundImage;

    public SimPanel(World world) {
        this.world = world;

        setPreferredSize(
                new Dimension(
                        world.getWidth(),
                        world.getHeight()
                )
        );

        URL backgroundURL =
                SimPanel.class.getResource("/Background.png");

        if (backgroundURL == null) {
            throw new IllegalStateException(
                    "Background.png was not found"
            );
        }

        this.backgroundImage =
                new ImageIcon(backgroundURL).getImage();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D graphics2D = (Graphics2D)graphics.create();

        try {
            graphics2D.drawImage(this.backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);

            for (Entity entity : world.getEntities()) {
                if (entity.isActive()) {
                    entity.draw(graphics2D);
                }
            }
        } finally {
            graphics2D.dispose();
        }

    }
}
