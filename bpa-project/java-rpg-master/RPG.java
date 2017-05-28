
import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;

public class RPG extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(MainPanel.class.getName());

    public static final String VERSION = "1.0";

    public RPG() {
        // Set the title of the game.
        setTitle("Argentium Unbound");

        // Create a MainPanel.
        MainPanel panel = new MainPanel();
        // Create a container.
        Container contentPane = getContentPane();
        // Put the main panel into the container.
        contentPane.add(panel);

        pack();
    }

    /**
     * Begin the program.
     *
     * @param args never used.
     */
    public static void main(String[] args) {
        // Create a new RPG frame.
        RPG frame = new RPG();
        // Centers the game panel
        frame.setLocationRelativeTo(null);

        // the panel cannot be resized.
        frame.setResizable(false);

        // When the panel closes, it will exit the application.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Display the game.
        frame.setVisible(true);
    }

}
