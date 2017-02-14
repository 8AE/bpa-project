import java.io.Serializable;

public class SaveEvent extends Event implements Serializable {

    public SaveEvent(int x, int y) {
        super(x, y, 10, false);
    }
}
