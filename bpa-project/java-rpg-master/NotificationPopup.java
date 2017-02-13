
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Ahmad 2/9/2017
 */
public class NotificationPopup implements Common {
       MessageEngine messageEngine;
         private static final int EDGE_WIDTH = 2;
    private boolean isVisible = false;
    String message = "";
    
          // outer frame
    private Rectangle boarder;
    // inner frame
    private Rectangle innerRect;
    public NotificationPopup(Rectangle rect){
           messageEngine = new MessageEngine();
           this.boarder = rect;
        innerRect = new Rectangle(
                rect.x + EDGE_WIDTH,
                rect.y + EDGE_WIDTH,
                rect.width - EDGE_WIDTH * 2,
                rect.height - EDGE_WIDTH * 2);

           
    }
    public void draw(Graphics g){
    if (isVisible == false) {
            return;
        }
        g.setColor(Color.WHITE);
        g.fillRect(boarder.x, boarder.y, boarder.width, boarder.height);

        g.setColor(Color.BLACK);
        g.fillRect(innerRect.x, innerRect.y, innerRect.width, innerRect.height);
           g.setColor(Color.WHITE);
            messageEngine.drawMessage(innerRect.x+10, innerRect.y+7, message, g);

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
}
