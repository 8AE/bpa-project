
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class DoorEvent extends Event implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(MainPanel.class.getName());

    public DoorEvent(int x, int y) {
        super(x, y, 18, true);
    }

    public String toString() {
        return "DOOR:" + super.toString();
    }
}
