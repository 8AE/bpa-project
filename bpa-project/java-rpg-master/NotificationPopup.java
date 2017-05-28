
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Ahmad 2/9/2017
 */
public class NotificationPopup implements Common, Serializable {

    private static final Logger LOGGER = Logger.getLogger(MainPanel.class.getName());

    MessageEngine messageEngine;
    private static final int EDGE_WIDTH = 2;
    private boolean isVisible = false;
    String message = "";

    // outer frame
    private Rectangle boarder;
    // inner frame
    private Rectangle innerRect;

    private static Thread threadAnimation;

    public NotificationPopup(Rectangle rect) {
        messageEngine = new MessageEngine();
        this.boarder = rect;
        innerRect = new Rectangle(
                rect.x + EDGE_WIDTH,
                rect.y + EDGE_WIDTH,
                rect.width - EDGE_WIDTH * 2,
                rect.height - EDGE_WIDTH * 2);

        threadAnimation = new Thread(new NotificationPopup.AnimationThread());
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
        g.setColor(Color.WHITE);
        messageEngine.drawMessage(innerRect.x + 10, innerRect.y + 7, message, g);

    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void show() {
        isVisible = true;
    }

    public void hide() {
        isVisible = false;
    }

    private class AnimationThread extends Thread implements Serializable {

        public void run() {

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            hide();
        }
    }

}
