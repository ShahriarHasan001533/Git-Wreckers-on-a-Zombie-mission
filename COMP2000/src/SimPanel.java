import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class SimPanel extends JPanel {

    private final Building building;

    public SimPanel(Building building) {
        this.building = building;

        setPreferredSize(new Dimension(800, 600));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D graphics2D =
                (Graphics2D) graphics;

        building.draw(graphics2D);
    }
}