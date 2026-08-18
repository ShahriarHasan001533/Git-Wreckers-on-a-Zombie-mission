import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            Building leftBuilding =
                    new Building(
                            155,  // x
                            125,  // y
                            150,  // width
                            100   // height
                    );

            Building rightBuilding =
                    new Building(
                            580,  // x
                            490,  // y
                            150,  // width
                            100   // height
                    );

            List<Building> buildings =
                    List.of(
                            leftBuilding,
                            rightBuilding
                    );

            SimPanel simPanel =
                    new SimPanel(buildings);

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