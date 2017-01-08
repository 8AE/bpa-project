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

    // constants of special commands for opening inventory
    public static final int TRASH_ITEM = 0;
    
    // constants for focuses
    private final int MAIN_FOCUS = 0;
    private final int TRASH_FOCUS = 1;
    
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
    // trash
    private Rectangle trashRect;
    
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
    private Color cursorColor;
    
    // trashing an item
    private boolean isTrashing = false;
    private int itemOnStandBy = -1;
    
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
                rect.y + INV_START + EDGE_WIDTH,
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

        trashRect = new Rectangle(
                rect.x + INV_SPACE*6,
                rect.y + INV_SPACE*6,
                INV_SPACE,
                INV_SPACE
                
        );
        
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
        
        // draw trash square
        g.setColor(Color.GREEN);
        g.fillRect(trashRect.x, trashRect.y, trashRect.width, trashRect.height);

        messageEngine.drawMessage(74, 134, "INVENTORY", g);
        if (currentFocus == MAIN_FOCUS) {
            // draw cursor
            cursorColor = Color.YELLOW;
        
            if (isTrashing) {
                cursorColor = Color.RED;
            }
        
            g.setColor(cursorColor);
            g.fillRect(cursorRect.x + INV_SPACE * invBoardX, cursorRect.y + INV_SPACE * invBoardY, cursorRect.width, cursorRect.height);
            
        } else if (currentFocus == TRASH_FOCUS) {
            //draw cursor
            cursorColor = Color.YELLOW;
            g.setColor(cursorColor);
            g.fillRect(trashRect.x, trashRect.y, trashRect.width, trashRect.height);
            
        }

        drawItems(g);

    }

    public void show() {
        isVisible = true;
    }
    
    public void show(int specialCommand, int num) {
        isVisible = true;
        switch (specialCommand) {
            case TRASH_ITEM:
                isTrashing = true;
                itemOnStandBy = num;
                break;
        }
    }

    public void hide() {
        isVisible = false;
        isTrashing = false;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setDirection(int direction) {
        switch (direction) {
            case LEFT:
                if (!isMoving) {
                    isMoving = true;
                    if (moveLeft()) {
                        invBoardX--;
                    }
                }
                isMoving = false;
                break;
            case RIGHT:
                if (!isMoving) {
                    isMoving = true;
                    if (moveRight()) {
                        invBoardX++;
                    }
                }
                isMoving = false;
                break;
            case DOWN:
                if (!isMoving) {
                    isMoving = true;
                    if (moveDown()) {
                        invBoardY++;
                    }
                }
                isMoving = false;
                break;
            case UP:
                if (!isMoving) {
                    isMoving = true;
                    if (moveUp()) {
                        invBoardY--;
                    }
                }
                isMoving = false;
                break;
        }
    }

    private boolean moveLeft() {
        return (invBoardX > 0);
    }

    private boolean moveRight() {
        return (invBoardX < INV_ROW - 1);
    }

    private boolean moveUp() {
        return (invBoardY > 0);
    }

    private boolean moveDown() {
        return (invBoardY < INV_COL - 1);
    }

    public void nextFocus() {
        if (currentFocus <= 1) {
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
                inventoryEngine.drawItem(invSquare.x + 48 * j, invSquare.y + 48 * i, invBoard[i][j], g);
            }
        }
    }

    public int getInvBoardPos() { return invBoard[invBoardX][invBoardY];}

    public int getInvBoardXPos() { return invBoardY; } // These are opposite on purpose
                                                       // since they are opposite for
    public int getInvBoardYPos() { return invBoardX; } // the items' positions on GUI
    
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
    
    public void delete(int posX, int posY) {
        invBoard[posX][posY] = itemOnStandBy;
        itemOnStandBy = -1;
    }
    
    public void select() {
        if (isTrashing) {
            delete(getInvBoardXPos(), getInvBoardYPos());
            isTrashing = false;
            return;
        } else if (currentFocus == TRASH_FOCUS) {
            isTrashing = true;
            currentFocus = 0;
            return;
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
