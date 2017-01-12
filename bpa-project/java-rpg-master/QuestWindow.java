import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.ImageIcon;

/**
 * Created by Ahmad El-baba on 1/3/2017.
 */

public class QuestWindow implements Common {
    MessageEngine messageEngine;
    
    // width of white border
    private static final int EDGE_WIDTH = 2;
    private boolean isVisible = false;
    
    // outer frame
    private Rectangle boarder;
    // inner frame
    private Rectangle innerRect;
    
    public QuestWindow(Rectangle rect){
        messageEngine=new MessageEngine();
        this.boarder = rect;
        innerRect = new Rectangle(
                rect.x + EDGE_WIDTH,
                rect.y + EDGE_WIDTH,
                rect.width - EDGE_WIDTH * 2,
                rect.height - EDGE_WIDTH * 2);
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
        messageEngine.drawMessage(66, 96, "QUESTS", g);
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
}