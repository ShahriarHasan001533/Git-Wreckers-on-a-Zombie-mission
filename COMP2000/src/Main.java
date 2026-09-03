import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;


public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            World world = new World(1000,750);

            Building leftBuilding = new Building(155, 125, 150, 100);
            Building rightBuilding = new Building(580, 490,150, 100);
            
            world.addEntity(leftBuilding);
            world.addEntity(rightBuilding);


            SimPanel simPanel =
                    new SimPanel(world);

            // Part B: people wander until they find and consume nearby food.
            world.addEntity(new Human(100, 400));
            world.addEntity(new Human(450, 200));
            world.addEntity(new Human(800, 350));

            world.addEntity(new Food(250, 300, 45));
            world.addEntity(new Food(500, 600, 45));
            world.addEntity(new Food(850, 150, 45));

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

            Timer timer = new Timer(100, event -> {
                world.update();
                simPanel.repaint();
            });

            timer.start();
        });
    }
}
