
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class SaveEvent extends Event implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(MainPanel.class.getName());

    public SaveEvent(int x, int y) {
        super(x, y, 10, false);
    }

}
