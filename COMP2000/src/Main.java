import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            Building building =
                    new Building(100, 200, 150, 100);

            SimPanel simPanel =
                    new SimPanel(building);

            JFrame frame =
                    new JFrame("Zombie Simulation");

            frame.setDefaultCloseOperation(
                    JFrame.EXIT_ON_CLOSE
            );

            frame.add(simPanel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
        });
    }
}