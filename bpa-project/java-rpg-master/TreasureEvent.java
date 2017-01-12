import java.util.*;

public class TreasureEvent extends Event {
    private String itemName;

    public TreasureEvent(int x, int y, String itemName) {
        super(x, y, 17, false);
        this.itemName = itemName;
    }

    public String getItemName() {
        return itemName;
    }

    public String toString() {
        return "TREASURE:" + super.toString() + ":" + itemName;
    }

    public int toInt() {

        Dictionary dict = new Hashtable();
        //put(key, value)
        dict.put("ITEM", 0);
        dict.put("THIS", 1);
        dict.put("SOMETHING", 2);
        dict.put("YEAH", 3);

        return (Integer) dict.get(super.toString());
    }
}
