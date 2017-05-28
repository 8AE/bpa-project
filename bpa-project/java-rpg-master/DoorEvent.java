
import java.io.Serializable;
import java.util.logging.Logger;

public class DoorEvent extends Event implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(MainPanel.class.getName());

    // Creates a door in the game.
    public DoorEvent(int x, int y) {
        super(x, y, 18, true);
    }

    public String toString() {
        return "DOOR:" + super.toString();
    }
}
