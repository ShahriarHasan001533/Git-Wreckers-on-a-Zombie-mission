import javax.swing.*;


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