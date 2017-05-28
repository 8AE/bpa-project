
import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

public class TreasureEvent extends Event implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(MainPanel.class.getName());

    private String itemName;
    private String itemType;

    public TreasureEvent(int x, int y, String itemName, String itemType) {
        super(x, y, 17, false);
        this.itemName = itemName;
        this.itemType = itemType;
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

    public String getItemType() {
        return itemType;
    }

    public int getId() {
        switch (itemName) {
            case "SWORD":

                return 0;
            case "KEY":

                return 1;
            case "SPEAR":

                return 2;
            case "WOODEN STAFF":

                return 3;

        }
        return 1;
    }

    public int getTile() {
        switch (itemName) {
            case "SWORD":

                return 1;
            case "SPEAR":

                return 3;
            case "WOODEN STAFF":

                return 5;

        }
        return 1;
    }

    public int getDamage() {
        switch (itemName) {
            case "SWORD":

                return 25;
            case "SPEAR":

                return 15;
            case "WOODEN STAFF":

                return 10;

        }
        return 1;
    }

    public String getDesc() {
        switch (itemName) {
            case "SWORD":

                return "A SHARP SWORD";
            case "KEY":

                return "A COOL KEY";
            case "SPEAR":

                return "A POINTY SPEAR";
            case "WOODEN STAFF":

                return "A MAGICAL STAFF";

        }
        return "";
    }

    public Item toItem() {
        return new Item(itemName, getDesc(), getId());
    }

    public Weapon toWeapon() {
        return new Weapon(itemName, getDesc(), getDamage(), getId(), getTile());
    }

}
