import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SimPanel extends JPanel {

    private final World world;
    private final Image backgroundImage;

    private boolean started;
    private boolean paused;

    public SimPanel(World world) {
        this.world = world;
        started = false;
        paused = false;

        setPreferredSize(
                new Dimension(world.getWidth(), world.getHeight())
        );

        URL backgroundURL =
                SimPanel.class.getResource("/Background.png");

        if (backgroundURL == null) {
            throw new IllegalStateException(
                    "Background.png could not be found"
            );
        }

        backgroundImage = new ImageIcon(backgroundURL).getImage();
        setupKeyBindings();
    }
    private void setupKeyBindings() {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("pressed R"),
                "start"
        );

        getActionMap().put("start", new AbstractAction() {
            @Override
            public void actionPerformed(
                    java.awt.event.ActionEvent event
            ) {
                started = true;
                paused = false;
                repaint();
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("pressed P"),
                "pause"
        );

        getActionMap().put("pause", new AbstractAction() {
            @Override
            public void actionPerformed(
                    java.awt.event.ActionEvent event
            ) {
                if (started) {
                    paused = !paused;
                    repaint();
                }
            }
        });
    }

    public boolean isRunning() {
        return started && !paused;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D graphics2D =
                (Graphics2D) graphics.create();

        try {
            // Draw the background
            graphics2D.drawImage(
                    backgroundImage,
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    this
            );
            if (!started) {
                drawStartScreen(graphics2D);
                return;
            }



            // Draw every active entity
            for (Entity entity : world.getEntities()) {
                if (entity.isActive()) {
                    entity.draw(graphics2D);
                }
            }

            // Draw each building's occupancy on top
            graphics2D.setFont(
                    new Font("Arial", Font.BOLD, 14)
            );


            for (Building building : findEntities(Building.class)) {
                String occupancyText =
                        "Occupancy: "
                                + building.getOccupantCount()
                                + "/"
                                + building.getCapacity();

                int textX = (int) building.getX();
                int textY = (int) building.getY() - 10;

                graphics2D.setColor(Color.BLACK);
                graphics2D.drawString(
                        occupancyText,
                        textX + 1,
                        textY + 1
                );

                graphics2D.setColor(Color.WHITE);
                graphics2D.drawString(
                        occupancyText,
                        textX,
                        textY
                );
            }
            int humanCount = findEntities(Human.class).size();

            graphics2D.setColor(new Color(0, 0, 0, 170));
            graphics2D.fillRoundRect(15, 15, 130, 35, 10, 10);

            graphics2D.setColor(Color.WHITE);
            graphics2D.drawString(
                    "Humans: " + humanCount,
                    25,
                    38
            );

        } finally {
            graphics2D.dispose();
        }
    }
    private void drawStartScreen(Graphics2D graphics2D) {
        graphics2D.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        graphics2D.setColor(new Color(0, 0, 0, 165));
        graphics2D.fillRect(
                0,
                0,
                getWidth(),
                getHeight()
        );

        graphics2D.setFont(
                new Font(
                        "Serif",
                        Font.BOLD | Font.ITALIC,
                        60
                )
        );

        graphics2D.setColor(new Color(190, 30, 30));
        drawCenteredText(
                graphics2D,
                "ZOMBIE SIMULATION",
                getHeight() / 2 - 50
        );

        graphics2D.setFont(
                new Font("Serif", Font.BOLD, 28)
        );

        graphics2D.setColor(Color.WHITE);
        drawCenteredText(
                graphics2D,
                "PRESS R TO START",
                getHeight() / 2 + 30
        );

        graphics2D.setFont(
                new Font("Serif", Font.ITALIC, 18)
        );

        graphics2D.setColor(Color.LIGHT_GRAY);
        drawCenteredText(
                graphics2D,
                "Press P to pause or resume",
                getHeight() / 2 + 70
        );
    }

    private void drawCenteredText(
            Graphics2D graphics2D,
            String text,
            int y
    ) {
        FontMetrics fontMetrics =
                graphics2D.getFontMetrics();

        int x = (
                getWidth()
                        - fontMetrics.stringWidth(text)
        ) / 2;

        graphics2D.drawString(text, x, y);
    }
    private <T extends Entity> List<T> findEntities(
            Class<T> entityType
    ) {
        List<T> matchingEntities = new ArrayList<>();

        for (Entity entity : world.getEntities()) {
            if (entityType.isInstance(entity)) {
                matchingEntities.add(entityType.cast(entity));
            }
        }

        return matchingEntities;
    }
}