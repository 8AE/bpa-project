import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

import javax.swing.ImageIcon;

/**
 * Created by hpro1 on 11/22/16.
 */
public class InventoryWindow implements Common {
    // constants of inventory
    private static final int EDGE_WIDTH = 2;
    private static final int INV_START = 65;
    private static final int INV_SPACE = 48;
    private static final int INV_COL = 5;
    private static final int INV_ROW = 5;

    // outer frame
    private Rectangle rect;
    // inner frame
    private Rectangle invInnerRect;
    // outer item frame
    private Rectangle invSquare;
    // inner item frame
    private Rectangle invInnerSquare;
    // cursor
    private Rectangle cursorRect;

    // is message window visible ?
    private boolean isVisible = false;

    // cursor animation gif
    private Image cursorImage;

    // cursor movement board
    private int[][] invBoard = new int[INV_ROW][INV_COL];
    private int invBoardX = 0;
    private int invBoardY = 0;
    private int currentFocus = 0;
    
    private boolean isMoving;
    private int moveLength;

    private InventoryEngine inventoryEngine;
    private MessageEngine messageEngine;

    private Thread threadAnime;

    public InventoryWindow(Rectangle rect) {
        this.rect = rect;
        invInnerRect = new Rectangle(
                rect.x + EDGE_WIDTH,
                rect.y + EDGE_WIDTH,
                rect.width - EDGE_WIDTH * 2,
                rect.height - EDGE_WIDTH * 2);

        invSquare = new Rectangle(
                rect.x + EDGE_WIDTH,
                rect.y + INV_START + 2,
                INV_SPACE - EDGE_WIDTH,
                INV_SPACE - EDGE_WIDTH);

        invInnerSquare = new Rectangle(
                rect.x,
                rect.y + INV_START,
                INV_SPACE + EDGE_WIDTH,
                INV_SPACE + EDGE_WIDTH);

        cursorRect = new Rectangle(
                invInnerRect.x,
                invInnerRect.y + INV_START,
                INV_SPACE - EDGE_WIDTH,
                INV_SPACE - EDGE_WIDTH);

        inventoryEngine = new InventoryEngine();
        messageEngine = new MessageEngine();

        for (int i = 0; i < invBoard.length; i++) {
            for (int j = 0; j < invBoard[i].length; j++) {
                invBoard[i][j] = -1;
            }
        }

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

        messageEngine.drawMessage(74, 134, "INVENTORY", g);

        // draw cursor
        g.setColor(Color.YELLOW);
        g.fillRect(cursorRect.x + INV_SPACE * invBoardX, cursorRect.y + INV_SPACE * invBoardY, cursorRect.width, cursorRect.height);

        drawItems(g);

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
        isMoving = false;
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
        if ((invBoardX > 0)) {
            return true;
        }
        return false;
    }

    private boolean moveRight() {
        if ((invBoardX < INV_ROW - 1)) {
            return true;
        }
        return false;
    }

    private boolean moveUp() {
        if ((invBoardY > 0)) {
            return true;
        }
        return false;
    }

    private boolean moveDown() {
        if ((invBoardY < INV_COL - 1)) {
            return true;
        }
        return false;
    }

    public void nextFocus() {
        if (currentFocus <= 3) {
            currentFocus++;
        } else {
            currentFocus = 0;
        }
    }
    
    public boolean isMoving() {
        return isMoving;
    }

    public void setMoving(boolean flag) {
        isMoving = flag;
        moveLength = 0;
    }

    private void drawItems(Graphics g) {
        for (int i = 0; i < invBoard.length; i++) {
            for (int j = 0; j < invBoard[i].length; j++) {
                inventoryEngine.drawItem(invInnerSquare.x + 48 * j, invInnerSquare.y + 48 * i, invBoard[i][j], g);
            }
        }
    }

    public int getInvBoardPos() { return invBoard[invBoardX][invBoardY];}

    public void add(int i) {
        findOpen:
        for (int k = 0; k < invBoard.length; k++) {
            for (int j = 0; j < invBoard[k].length; j++) {
                if (invBoard[k][j] == -1) {
                    invBoard[k][j] = i;
                    break findOpen;
                }
            }
        }
    }

    public boolean isFull() {
        for (int i = 0; i < invBoard.length; i++) {
            for (int j = 0; j < invBoard[i].length; j++) {
                if (invBoard[i][j] == -1) {
                    return false;
                }
            }
        }
        return true;
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
