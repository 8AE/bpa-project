
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

/**
 * Created by hpro1 on 11/29/16.
 */
public class InventoryEngine implements Common, Serializable {

    private static final Logger LOGGER = Logger.getLogger(MainPanel.class.getName());

    // chipset dimensions of items
    public static final int ITEM_WIDTH = CS;
    public static final int ITEM_HEIGHT = CS;

    // item scaling for inventory
    public static final double ITEM_SCALE = 1.5;

    // item image
    private Image itemsImage;
    // items position
    private HashMap<Integer, Point> itemPos;

    public InventoryEngine() {
        // load item images
        ImageIcon icon = new ImageIcon(getClass().getResource("image/mapchip.png"));
        itemsImage = icon.getImage();

        itemPos = new HashMap<Integer, Point>();
        createHash();
    }

    public void drawItem(int x, int y, Item item, Graphics g) {
        if (item != null) {
            Point pos = itemPos.get(new Integer(item.getId()));
            if (pos == null) {
                return;
            }
            g.drawImage(itemsImage,
                    x,
                    y,
                    (int) ((x + ITEM_WIDTH * ITEM_SCALE) - 2),
                    (int) ((y + ITEM_HEIGHT * ITEM_SCALE) - 2),
                    pos.x,
                    pos.y,
                    pos.x + ITEM_WIDTH,
                    pos.y + ITEM_HEIGHT, null);
        }
    }

    // The locations of item sprites
    private void createHash() {
        // item sprite positions
        itemPos.put(new Integer(0), new Point(0, 0)); //torch
        itemPos.put(new Integer(1), new Point(32, 0));
        itemPos.put(new Integer(2), new Point(64, 0));
        itemPos.put(new Integer(3), new Point(96, 0));
        itemPos.put(new Integer(4), new Point(128, 0));
        itemPos.put(new Integer(5), new Point(160, 0));
        itemPos.put(new Integer(6), new Point(192, 0));
        itemPos.put(new Integer(7), new Point(224, 0));
        itemPos.put(new Integer(8), new Point(0, 32));
        itemPos.put(new Integer(9), new Point(32, 32));
        itemPos.put(new Integer(10), new Point(64, 32));
        itemPos.put(new Integer(11), new Point(96, 32));
        itemPos.put(new Integer(12), new Point(128, 32));
        //TODO: Set sprite positions and the points... need to know how many sprite per row
    }

}
