import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;

/**
 * Created by hpro1 on 11/22/16.
 */
public class InventoryWindow implements Common {
    // width of white border
    private static final int EDGE_WIDTH = 2;

    private static final int INV_START = 60;
    private static final int INV_SPACE = 32;
    private static final int INV_COL = 8;
    private static final int INV_ROW = 9;

    // outer frame
    private Rectangle rect;
    // inner frame
    private Rectangle innerRect;
    // item frame column
    private Rectangle invRectCol;
    // item frame row
    private Rectangle invRectRow;
    // cursor
    private Rectangle cursorRect;

    // message window is visible ?
    private boolean isVisible = false;

    // cursor animation gif
    private Image cursorImage;

    // cursor movement board
    private int[][] invBoard = new int[7][8];
    private int invBoardX = 0;
    private int invBoardY = 0;

    private boolean isMoving;
    private int moveLength;

    private InventoryEngine inventoryEngine;
    private MessageEngine messageEngine;

    private Thread threadAnime;

    public InventoryWindow(Rectangle rect) {
        this.rect = rect;
        innerRect = new Rectangle(
                rect.x + EDGE_WIDTH,
                rect.y + EDGE_WIDTH,
                rect.width - EDGE_WIDTH * 2,
                rect.height - EDGE_WIDTH * 2);

        invRectCol = new Rectangle(
                innerRect.x - 2,
                innerRect.y + INV_START,
                EDGE_WIDTH,
                rect.height - INV_START - 4);

        invRectRow = new Rectangle(
                innerRect.x,
                innerRect.y + INV_START,
                innerRect.width - 286,
                EDGE_WIDTH);

        cursorRect = new Rectangle(
                innerRect.x,
                innerRect.y + INV_START + 2,
                30,
                30);

        inventoryEngine = new InventoryEngine();
        messageEngine = new MessageEngine();

        // load cursor image
        ImageIcon icon = new ImageIcon(getClass().getResource("image/cursor.gif"));
        cursorImage = icon.getImage();

        // run thread
        threadAnime = new Thread(new InventoryWindow.AnimationThread());
        threadAnime.start();
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
        g.fillRect(innerRect.x, innerRect.y,
                innerRect.width, innerRect.height);

        // draw inventory columns
        g.setColor(Color.WHITE);
        for (int i = 0; i < INV_COL; i++) {
            g.fillRect(invRectCol.x + (i * INV_SPACE), invRectCol.y, invRectCol.width, invRectCol.height);
        }

        // draw inventory rows
        for (int i = 0; i < INV_ROW; i++) {
            g.fillRect(invRectRow.x, invRectRow.y + (i * INV_SPACE), invRectRow.width, invRectRow.height);
        }

        messageEngine.drawMessage(74, 134, "INVENTORY", g);

        // draw cursor
        g.setColor(Color.YELLOW);
        g.fillRect(cursorRect.x + INV_SPACE * invBoardX, cursorRect.y + INV_SPACE * invBoardY, cursorRect.width, cursorRect.height);

    }

    public void show() {
        isVisible = true;
    }

    public void hide() {
        isVisible = false;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setDirection(int direction) {
        switch (direction) {
            case LEFT:
                if (!isMoving) {
                    if (moveLeft()) {
                        invBoardX--;
                        isMoving = true;
                    }
                }
                break;
            case RIGHT:
                if (!isMoving) {
                    if (moveRight()) {
                        invBoardX++;
                        isMoving = true;
                    }
                }
                break;
            case DOWN:
                if (!isMoving) {
                    if (moveDown()) {
                        invBoardY++;
                        isMoving = true;
                    }
                }
                break;
            case UP:
                if (!isMoving) {
                    if (moveUp()) {
                        invBoardY--;
                        isMoving = true;
                    }
                }
                break;
        }
    }

    private boolean moveLeft() {
        if ((invBoardX - 1 >= 0)) {
            return true;
        }
        return false;
    }

    private boolean moveRight() {
        if ((invBoardX + 1 <= 6)) {
            return true;
        }
        return false;
    }

    private boolean moveUp() {
        if ((invBoardY - 1 >= 0)) {
            return true;
        }
        return false;
    }

    private boolean moveDown() {
        if ((invBoardY + 1 <= 7)) {
            return true;
        }
        return false;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public void setMoving(boolean flag) {
        isMoving = flag;
        moveLength = 0;
    }

    public int getInvBoardPos() { return invBoard[invBoardX][invBoardY];}

    public void add(int i) {
        for (int k = 0; k < invBoard.length; k++) {
            for (int j = 0; j < invBoard[k].length; j++) {
                if (invBoard[k][j] == -1) {
                    invBoard[k][j] = i;
                }
            }
        }
    }

    public boolean isFull() {
        for (int i = 0; i < invBoard.length; i++) {
            for (int j = 0; j < invBoard[i].length; j++) {
                if (invBoard[i][j] == -1) {
                    return true;
                }
            }
        }
        return false;
    }

    private class AnimationThread extends Thread {
        public void run() {
            while (true) {
                isMoving = false;
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
