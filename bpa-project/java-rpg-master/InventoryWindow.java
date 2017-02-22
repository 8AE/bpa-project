
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 * Created by hpro1 on 11/22/16.
 */
public class InventoryWindow implements Common, Serializable {

    private static final Logger LOGGER = Logger.getLogger(MainPanel.class.getName());

    // constants of inventory
    private static final int EDGE_WIDTH = 2;
    private static final int INV_START = 65;
    private static final int INV_SPACE = 48;
    public static final int INV_COL = 5;
    public static final int INV_ROW = 5;

    // constants of special commands for opening inventory
    public static final int TRASH_ITEM = 0;

    // constants for focuses
    private final int MAIN_FOCUS = 0;
    private final int TRASH_FOCUS = 1;

    private static BufferedImage trashIcon;
    
    // outer frame
    private static Rectangle rect;
    // inner frame
    private static Rectangle invInnerRect;
    // outer item frame
    private static Rectangle invSquare;
    // inner item frame
    private static Rectangle invInnerSquare;
    // cursor
    private static Rectangle cursorRect;
    // trash
    private static Rectangle trashRect;

    // is message window visible ?
    private boolean isVisible = false;

    // cursor movement board
    private Item[][] invBoard = new Item[INV_ROW][INV_COL];
    private int invBoardX = 0;
    private int invBoardY = 0;
    private int currentFocus = 0;
    private boolean isMoving;
    private static Color cursorColor;

    // trashing an item
    private boolean isTrashing = false;
    private static Item itemOnStandBy = null;

    // the engines needed in this class are declared
    private static InventoryEngine inventoryEngine;
    private static MessageEngine messageEngine;
    private static WaveEngine waveEngine;

    // Sound Clips needed in the inventory window
    private static final String[] soundNames = {"beep", "boop"};

    private static MainPanel panel;
    
    // the thread for the cursor animation
    private static Thread threadAnimation;

    public InventoryWindow(Rectangle rect, MainPanel panel) {
        // The base dimensions of the inventory window
        this.rect = rect;
        
        try {
            trashIcon = ImageIO.read(getClass().getResource("image/trashcan.png"));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);

                    try {
                        CrashReport cr = new CrashReport(e);
                        cr.show();
                    } catch (Exception n) {
                        // do nothing
                    }
        }
        
        // The inner dimensions so that there is a boarder
        invInnerRect = new Rectangle(
                rect.x + EDGE_WIDTH,
                rect.y + EDGE_WIDTH,
                rect.width - EDGE_WIDTH * 2,
                rect.height - EDGE_WIDTH * 2);

        // One square of the inventory. A spot for one item.
        invSquare = new Rectangle(
                rect.x + EDGE_WIDTH,
                rect.y + INV_START + EDGE_WIDTH,
                INV_SPACE - EDGE_WIDTH,
                INV_SPACE - EDGE_WIDTH);

        // Inner part of an inventory space to create a boarder effect.
        invInnerSquare = new Rectangle(
                rect.x,
                rect.y + INV_START,
                INV_SPACE + EDGE_WIDTH,
                INV_SPACE + EDGE_WIDTH);

        // The rectangle for the cursor
        cursorRect = new Rectangle(
                invInnerRect.x,
                invInnerRect.y + INV_START,
                INV_SPACE - EDGE_WIDTH,
                INV_SPACE - EDGE_WIDTH);

        // The rectangle for the trash space
        trashRect = new Rectangle(
                rect.x + INV_SPACE * 6,
                rect.y + INV_SPACE * 6,
                INV_SPACE,
                INV_SPACE
        );

        // The engines needed are initialized
        inventoryEngine = new InventoryEngine();
        messageEngine = new MessageEngine();
        waveEngine = new WaveEngine();

        // load sound clips
        loadSound();

        // The inventory board is created
        for (int i = 0; i < invBoard.length; i++) {
            for (int j = 0; j < invBoard[i].length; j++) {
                invBoard[i][j] = null;
            }
        }

        // run thread
        threadAnimation = new Thread(new InventoryWindow.AnimationThread());
        threadAnimation.start();
    }

    public void draw(Graphics g) {
        if (isVisible == false) {
            return;
        }

        // draw outer rect
        g.setColor(Color.WHITE);
        g.fillRect(rect.x, rect.y, rect.width, rect.height);

        // draw inner rect
        g.setColor(Color.BLACK);
        g.fillRect(invInnerRect.x, invInnerRect.y,
                invInnerRect.width, invInnerRect.height);

        // draw inventory rows
        g.setColor(Color.WHITE);
        for (int i = 0; i < INV_COL; i++) {
            for (int j = 0; j < INV_ROW; j++) {
                g.fillRect(invInnerSquare.x + (i * INV_SPACE), invInnerSquare.y + (j * INV_SPACE), invInnerSquare.width, invInnerSquare.height);
            }
        }

        // draw inventory columns
        g.setColor(Color.BLACK);
        for (int i = 0; i < INV_COL; i++) {
            for (int j = 0; j < INV_ROW; j++) {
                g.fillRect(invSquare.x + (i * INV_SPACE), invSquare.y + (j * INV_SPACE), invSquare.width, invSquare.height);
            }
        }

        // draw trash square
        g.drawImage(trashIcon, trashRect.x, trashRect.y, panel);

        
        // draw the actual items
        drawItems(g);
        
        // draw title for the inventory
        messageEngine.drawMessage(74, 134, "INVENTORY", g);

        // draw the cursor depending on the focus it is in
        if (currentFocus == MAIN_FOCUS) {
            // draw cursor
            cursorColor = Color.YELLOW;

            if (isTrashing) {
                cursorColor = Color.RED;
            }

            g.setColor(cursorColor);
            g.drawRect(cursorRect.x + INV_SPACE * invBoardX, cursorRect.y + INV_SPACE * invBoardY, cursorRect.width, cursorRect.height);

        } else if (currentFocus == TRASH_FOCUS) {
            //draw cursor
            cursorColor = Color.YELLOW;
            g.setColor(cursorColor);
            g.drawRect(trashRect.x, trashRect.y, trashRect.width, trashRect.height);

        }

        // draw the item name and description
        messageEngine.drawMessage(328, 134, "ITEM", g);
        if (invBoard[getInvBoardXPos()][getInvBoardYPos()] != null) {
            messageEngine.drawMessage(328, 160, invBoard[getInvBoardXPos()][getInvBoardYPos()].getName(), g);
        }

        messageEngine.drawMessage(328, 196, "DESCRIPTION", g);
        if (invBoard[getInvBoardXPos()][getInvBoardYPos()] != null) {
            messageEngine.drawMessage(328, 222, invBoard[getInvBoardXPos()][getInvBoardYPos()].getDescription(), g);
        }

    }

    public void runThread() {
        // run thread
        threadAnimation = new Thread(new InventoryWindow.AnimationThread());
        threadAnimation.start();
    }

    /**
     * The inventory window appears.
     */
    public void show() {
        isVisible = true;
    }

    /**
     * The inventory window appears with a special command.
     *
     * @param specialCommand The command you want the window to open with.
     * @param item The item that is brought into the inventory with the command.
     */
    public void show(int specialCommand, Item item) {
        isVisible = true;
        switch (specialCommand) {
            case TRASH_ITEM:
                isTrashing = true;
                itemOnStandBy = item;
                break;
        }
    }

    /**
     * The inventory window disappears.
     */
    public void hide() {
        isVisible = false;
        isTrashing = false;
    }

    /**
     * Is the inventory visible?
     *
     * @return if the window is visible.
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * Move the character in the declared direction.
     *
     * @param direction The direction that the cursor is moving.
     */
    public void setDirection(int direction) {
        switch (direction) {
            case LEFT:
                if (!isMoving) { // Check if the cursor is already moving.
                    isMoving = true; // Set that the cursor is now moving.
                    if (canMoveLeft()) { // Check if the cursor can move left
                        waveEngine.play("beep"); // Make a "beep" if the cursor moves into a new space.
                        invBoardX--; // Move the cursor a space to the left.
                    } else {
                        waveEngine.play("boop"); // Make a "boop" if the cursor cannot move left.
                    }
                }
                isMoving = false;
                break;
            // The following cases follow the same comments as above except in the other three directions.
            case RIGHT:
                if (!isMoving) {
                    isMoving = true;
                    if (canMoveRight()) {
                        waveEngine.play("beep");
                        invBoardX++;
                    } else {
                        waveEngine.play("boop");
                    }
                }
                isMoving = false;
                break;
            case DOWN:
                if (!isMoving) {
                    isMoving = true;
                    if (canMoveDown()) {
                        waveEngine.play("beep");
                        invBoardY++;
                    } else {
                        waveEngine.play("boop");
                    }
                }
                isMoving = false;
                break;
            case UP:
                if (!isMoving) {
                    isMoving = true;
                    if (canMoveUp()) {
                        waveEngine.play("beep");
                        invBoardY--;
                    } else {
                        waveEngine.play("boop");
                    }
                }
                isMoving = false;
                break;
        }
    }

    /**
     * Checks if the cursor can move left by checking if the position is above
     * zero.
     *
     * @return whether or not the cursor can move left.
     */
    private boolean canMoveLeft() {
        return (invBoardX > 0);
    }

    /**
     * Checks if the cursor can move right by checking if the position is below
     * row count.
     *
     * @return whether or not the cursor can move right.
     */
    private boolean canMoveRight() {
        return (invBoardX < INV_ROW - 1);
    }

    /**
     * Checks if the cursor can move up by checking if the position is above
     * zero.
     *
     * @return whether or not the cursor can move up.
     */
    private boolean canMoveUp() {
        return (invBoardY > 0);
    }

    /**
     * Checks if the cursor can move down by checking if the position is below
     * the column count.
     *
     * @return whether or not the cursor can move down.
     */
    private boolean canMoveDown() {
        return (invBoardY < INV_COL - 1);
    }

    /**
     * Move the cursor to the next focused section. When it reaches the max
     * number of focuses, it returns back to the beginning to recycle.
     */
    public void nextFocus() {
        if (currentFocus < 1) {
            currentFocus++;
        } else {
            currentFocus = 0;
        }
    }

    /**
     * Check if the cursor is currently moving.
     *
     * @return whether or not the cursor is moving.
     */
    public boolean isMoving() {
        return isMoving;
    }

    /**
     * Set whether or not the cursor is moving.
     *
     * @param isMoving true for if it is moving and false for if it is not
     * moving.
     */
    public void setMoving(boolean isMoving) {
        this.isMoving = isMoving;
    }

    /**
     * Draw the items in the inventory to the screen.
     *
     * @param g get the graphics class that is printing everything to the
     * screen.
     */
    private void drawItems(Graphics g) {
        for (int i = 0; i < invBoard.length; i++) {
            for (int j = 0; j < invBoard[i].length; j++) {
                inventoryEngine.drawItem(invSquare.x + 48 * j, invSquare.y + 48 * i, invBoard[i][j], g);
            }
        }
    }

    /**
     * Get the Item the cursor is currently on.
     *
     * @return the current Item the cursor is on.
     */
    public Item getInvBoardPosItem() {
        return invBoard[invBoardX][invBoardY];
    }

    /**
     * Get the X position of the inventory board that the cursor is on.
     *
     * @return The x-value in terms of a coordinate grind.
     */
    public int getInvBoardXPos() {
        return invBoardY;
    } // These are opposite on purpose.
    // The items' positions are backwards from

    public int getInvBoardYPos() {
        return invBoardX;
    } // the traditional cartesian coordinate plain.

    /**
     * Add an item to the inventory in the first free space available and then
     * exit the method.
     *
     * @param item the Item to be added into the inventory.
     */
    public void add(Item item) {
        for (int k = 0; k < invBoard.length; k++) {
            for (int j = 0; j < invBoard[k].length; j++) {
                if (invBoard[k][j] == null) {
                    invBoard[k][j] = item;
                    return;
                }
            }
        }
    }

    /**
     * Delete an item in the selected position.
     *
     * @param posX the x position of the board the item in question is located
     * in.
     * @param posY the y position of the board the item in question is located
     * in.
     */
    public void delete(int posX, int posY) {
        // Set the item in question to whatever the temporary item is.
        // Normally, this remains null, but if the inventory opens up
        // asking for the user to delete an item because it is full,
        // then the item that is replacing the deleted item is put in its place.
        invBoard[posX][posY] = itemOnStandBy;
        itemOnStandBy = null; // Set the temporary item back to null.
    }

    /**
     * Perform an action depending on the cursor's position and current focus.
     */
    public void select() {
        if (isTrashing) {
            // If the cursor is in trash mode, it will delete the item it is currently on.
            delete(getInvBoardXPos(), getInvBoardYPos());
            isTrashing = false;
            return;
        } else if (currentFocus == TRASH_FOCUS) {
            isTrashing = true;
            currentFocus = 0; // Send focus back to the main focus.
            return;
        }
    }

    public Item getItem(){
        return invBoard[getInvBoardXPos()][getInvBoardYPos()];
        
    }
    
    /**
     * Check if the inventory is full.
     *
     * @return whether or not it is full. True means it is, false means it is
     * now.
     */
    public boolean isFull() {
        for (int i = 0; i < invBoard.length; i++) {
            for (int j = 0; j < invBoard[i].length; j++) {
                if (invBoard[i][j] == null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Load the sounds.
     */
    private void loadSound() {
        // load sound clip files
        for (String soundName : soundNames) {
            waveEngine.load(soundName, "sound/" + soundName + ".wav");
        }
    }

    /**
     * This thread manages the animation of the cursor so that it moves a normal
     * speed.
     */
    private class AnimationThread extends Thread implements Serializable {

        public void run() {
            while (true) {
                isMoving = false; // Set movement to false
                try {
                    Thread.sleep(300); // Wait 0.3 seconds.
                } catch (InterruptedException e) {
                    LOGGER.log(Level.SEVERE, e.toString(), e);

                    try {
                        CrashReport cr = new CrashReport(e);
                        cr.show();
                    } catch (Exception n) {
                        // do nothing
                    }
                }
            }
        }
    }
}
