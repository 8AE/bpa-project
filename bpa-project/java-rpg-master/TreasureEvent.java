
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class TreasureEvent extends Event implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(MainPanel.class.getName());

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

        return (Integer) dict.get(itemName);
    }

    public Item toItem() {
        return new Item("ITEM", "ITEM DESC", 2);
    }

}
