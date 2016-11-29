import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.lang.*;
import java.lang.Character;
import java.util.HashMap;

import javax.swing.ImageIcon;

import static javax.swing.UIManager.put;

/**
 * Created by hpro1 on 11/29/16.
 */
public class InventoryEngine {
    // font size
    public static final int ITEM_WIDTH = 16;
    public static final int ITEM_HEIGHT = 22;

    // item image
    private Image itemsImage;
    // items position
    private HashMap<Integer, Point> itemPos;

    // current color
    private int color;

    public InventoryEngine() {
        // load item images
        ImageIcon icon = new ImageIcon(getClass().getResource("image/font.gif"));
        itemsImage = icon.getImage();

        itemPos = new HashMap<Integer, Point>();
        createHash();
    }

    public void drawItem(int x, int y, int i, Graphics g) {
        Point pos = itemPos.get(new Integer(i));
        if (pos == null) {
            return;
        }
        g.drawImage(itemsImage,
                x,
                y,
                x + ITEM_WIDTH,
                y + ITEM_HEIGHT,
                pos.x + color,
                pos.y,
                pos.x + color + ITEM_WIDTH,
                pos.y + ITEM_HEIGHT, null);
    }

    private void createHash() {
        // item sprite positions
        itemPos.put(new Integer(0), new Point(0, 0)); //torch
        //TODO: Set sprite positions
    }
}
