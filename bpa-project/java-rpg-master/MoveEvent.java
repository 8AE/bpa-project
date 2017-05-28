
import java.io.Serializable;
import java.util.logging.Logger;

public class MoveEvent extends Event implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(MainPanel.class.getName());

    public int destMapNo;
    public int destX;
    public int destY;

    public MoveEvent(int x, int y, int chipNo, int destMapNo, int destX, int destY) {
        super(x, y, chipNo, false);
        this.destMapNo = destMapNo;
        this.destX = destX;
        this.destY = destY;
    }

    public String toString() {
        return "MOVE:" + super.toString() + ":" + destMapNo + ":" + destX + ":" + destY;
    }

}
