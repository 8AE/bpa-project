import java.awt.*;
import javax.swing.*;

public class RPG extends JFrame {
    public RPG() {
        // Set the title of the game.
        setTitle("RPG");
        
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
     * @param args never used.
     */
    public static void main(String[] args) {
        // Create a new RPG frame.
        RPG frame = new RPG();
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Display the game.
        frame.setVisible(true);
    }
}
