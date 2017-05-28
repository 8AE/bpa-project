
import java.io.Serializable;

public abstract class Event implements Serializable {

    protected int x;
    protected int y;
    protected int id;
    protected boolean isHit;

    // Basic bones of an event.
    public Event(int x, int y, int id, boolean isHit) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.isHit = isHit;
    }

    public String toString() {
        return x + ":" + y + ":" + id + ":" + isHit;
    }
}
