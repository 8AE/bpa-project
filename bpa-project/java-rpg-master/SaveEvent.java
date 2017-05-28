
import java.io.Serializable;
import java.util.logging.Logger;

public class SaveEvent extends Event implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(MainPanel.class.getName());

    public SaveEvent(int x, int y) {
        super(x, y, 10, false);
    }

}
