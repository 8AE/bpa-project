
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.Serializable;

/**
 * Created by Ahmad El-baba on 1/3/2017.
 */
public class PauseWindow implements Common, Serializable {

    MessageEngine messageEngine;

    // width of white border
    private static final int EDGE_WIDTH = 2;
    private boolean isVisible = false;

    private int pauseBoardSpot = 0;
    private boolean isMoving;

    private final int PAUSE_SLOT_SPACING = 40;

    private Thread threadAnimation;

    private WaveEngine waveEngine;
    private static final String[] soundNames = {"beep", "boop"};

    // outer frame
    private Rectangle boarder;
    // inner frame
    private Rectangle innerRect;

    //CCurson
    private Rectangle cursor;

    public PauseWindow(Rectangle rect) {
        messageEngine = new MessageEngine();

        waveEngine = new WaveEngine();
        loadSound();

        cursor = new Rectangle(295, 244, 100, 28);
        this.boarder = rect;
        innerRect = new Rectangle(
                rect.x + EDGE_WIDTH,
                rect.y + EDGE_WIDTH,
                rect.width - EDGE_WIDTH * 2,
                rect.height - EDGE_WIDTH * 2);

        // run thread
        threadAnimation = new Thread(new PauseWindow.AnimationThread());
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
        messageEngine.drawMessage(300, 200, "PAUSE", g);

        messageEngine.drawMessage(295, 244, "RESUME", g);
        messageEngine.drawMessage(295, 284, "SAVE", g);
        messageEngine.drawMessage(295, 324, "QUIT", g);

        g.drawRect(cursor.x, cursor.y + PAUSE_SLOT_SPACING * pauseBoardSpot, cursor.width, cursor.height);

    }

    public void runThread() {
        // run thread
        threadAnimation = new Thread(new PauseWindow.AnimationThread());
        threadAnimation.start();
    }

    public void show() {
        isVisible = true;
    }

    public void setDirection(int direction) {

        switch (direction) {

            case DOWN:
                if (!isMoving) {
                    isMoving = true;
                    if (canMoveDown()) {
                        waveEngine.play("beep");
                        pauseBoardSpot++;
                    } else {
                        pauseBoardSpot = 0;
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
                        pauseBoardSpot--;
                    } else {
                        pauseBoardSpot = 2;
                        waveEngine.play("boop");
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
     * Checks if the cursor can move down by checking if the position is below
     * the column count.
     *
     * @return whether or not the cursor can move down.
     */
    private boolean canMoveDown() {
        return (pauseBoardSpot <= 1);
    }

    /**
     * Checks if the cursor can move down by checking if the position is below
     * the column count.
     *
     * @return whether or not the cursor can move down.
     */
    private boolean canMoveUp() {
        return (pauseBoardSpot >= 1);
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

    public int getPauseBoardPos() {
        return pauseBoardSpot;
    }

    private void loadSound() {

        // load sound clip files
        for (String soundName : soundNames) {
            waveEngine.load(soundName, "sound/" + soundName + ".wav");
        }
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
