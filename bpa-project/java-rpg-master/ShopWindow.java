import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import java.util.List;
/**
 * Created by Ahmad El-baba on 2/15/2017.
 */
public class ShopWindow implements Common {

    MessageEngine messageEngine;



    // width of white border
    private static final int EDGE_WIDTH = 2;
    private boolean isVisible = false;

    private int ShopBoardSpot = 0;
    private boolean isMoving;

    private final int QUEST_SLOT_SPACING = 150;

    private Thread threadAnimation;

    // outer frame
    private Rectangle boarder;
    // inner frame
    private Rectangle innerRect;

    //CCurson
    private Rectangle cursor;

    public ShopWindow(Rectangle rect) {
        messageEngine = new MessageEngine();
      
        cursor = new Rectangle(150, 300, 64, 64 );
        this.boarder = rect;
        innerRect = new Rectangle(
                rect.x + EDGE_WIDTH,
                rect.y + EDGE_WIDTH,
                rect.width - EDGE_WIDTH * 2,
                rect.height - EDGE_WIDTH * 2);

        // run thread
        threadAnimation = new Thread(new ShopWindow.AnimationThread());
        threadAnimation.start();
    }

    public void draw(Graphics g) {
        if (isVisible == false) {
            return;
        }
        g.setColor(Color.WHITE);
        g.fillRect(boarder.x, boarder.y, boarder.width, boarder.height);

        g.setColor(Color.BLACK);
        g.fillRect(innerRect.x, innerRect.y, innerRect.width, innerRect.height);
        messageEngine.setColor(messageEngine.WHITE);
        g.setColor(Color.WHITE);
        messageEngine.drawMessage(256, 96, "SHOP", g);
      
      
      

        g.drawRect(cursor.x+ QUEST_SLOT_SPACING * ShopBoardSpot, cursor.y , cursor.width, cursor.height);

       
        
    }
    
    public void runThread() {
        // run thread
        threadAnimation = new Thread(new ShopWindow.AnimationThread());
        threadAnimation.start();
    }

    

    public void show() {
        isVisible = true;
    }

    public void setDirection(int direction) {

        switch (direction) {

            case LEFT:
                if (!isMoving) {
                    isMoving = true;
                    if (canMoveRight()) {
                        //waveEngine.play("beep");
                        ShopBoardSpot--;
                    } else {
                    ShopBoardSpot=2;
                        //waveEngine.play("boop");
                    }
                }
                isMoving = false;
                break;
            case RIGHT:
                if (!isMoving) {
                    isMoving = true;
                    if (canMoveLeft()) {
                        //waveEngine.play("beep");
                        ShopBoardSpot++;
                    } else {
                      ShopBoardSpot=0;
                        //waveEngine.play("boop");
                    }
                }
                isMoving = false;
                break;
                
        }
    }

    public void hide() {
        isVisible = false;
    }
    
    /**
     * Checks if the cursor can move right by checking if the position is below the column count.
     * @return whether or not the cursor can move down.
     */
    private boolean canMoveRight() {
        return (ShopBoardSpot >=1);
    }
    
    /**
     * Checks if the cursor can move left by checking if the position is below the column count.
     * @return whether or not the cursor can move down.
     */
    private boolean canMoveLeft() {
        return (ShopBoardSpot <= 1);
    }

    public boolean isMoving() {
        return isMoving;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setMoving(boolean flag) {
        isMoving = flag;
    }

    public int getShopBoardPos() {
        return ShopBoardSpot;
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
