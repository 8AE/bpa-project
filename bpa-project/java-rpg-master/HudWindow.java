import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.ImageIcon;

/**
 * Created by Ahmad El-baba on 11/29/2016.
 */

public class HudWindow implements Common {

    //healthBar
    private Rectangle healthBarBackround;
    private Rectangle healthBar;
    //healthBar outline
    private Rectangle outerHealthBar;
    //Ultimate Bar
    private Rectangle ultamateBar;
    //Ultimate Bar outline
    private Rectangle outerUltamateBar;
    //Left Hand Box
    private Rectangle leftBox;
    //Left Hand Box outline
    private Rectangle outerLeftBox;
    //right Hand Box
    private Rectangle rightBox;
    //right Hand Box outline
    private Rectangle outerRightBox;
    //Item Box
    private Rectangle itemBox;
    //Item Box outline
    private Rectangle outerItemBox;

    private Rectangle backround;

    //Message Engine
    private MessageEngine messageEngine;
    
    private double health;
    
    
    

    public HudWindow() {
        messageEngine = new MessageEngine();
        backround = new Rectangle(0,0,640,128);

        healthBarBackround = new Rectangle(34 , 34 , 220, 28 );
         healthBar = new Rectangle(34 , 34 , 220, 28 );
        outerHealthBar = new Rectangle(32 , 32 , 224, 32 );

        /*ultamateBar = new Rectangle(34 , 34 , 220, 28 );
        outerUltamateBar = new Rectangle(32 , 32 , 224, 32 );*/

        itemBox = new Rectangle(290 , 34 , 60, 60 );
        outerItemBox = new Rectangle(288 , 32 , 64, 64 );

        rightBox = new Rectangle(482 , 34 , 60, 60 );
        outerRightBox = new Rectangle(480 , 32 , 64, 64 );

        leftBox = new Rectangle(418 , 34 , 60, 60 );
        outerLeftBox = new Rectangle(416 , 32 , 64, 64 );

    }
public void updateHealth(int health){
   this.health = health/100.0;
   
   
    
    
}
    public void draw(Graphics g) {
        // draw outer rect
        g.setColor(Color.BLACK);
        g.fillRect(backround.x, backround.y, backround.width, backround.height);
        
        g.setColor(Color.WHITE);
        g.fillRect(outerHealthBar.x, outerHealthBar.y, outerHealthBar.width, outerHealthBar.height);
        //g.fillRect(outerUltamateBar.x, outerUltamateBar.y, outerUltamateBar.width, outerUltamateBar.height);
        g.fillRect(outerItemBox.x, outerItemBox.y, outerItemBox.width, outerItemBox.height);
        g.fillRect(outerRightBox.x, outerRightBox.y, outerRightBox.width, outerRightBox.height);
        g.fillRect(outerLeftBox.x, outerLeftBox.y, outerLeftBox.width, outerLeftBox.height);

        messageEngine.drawMessage(32, 0, "HEALTH", g);
        messageEngine.drawMessage(288, 0, "ITEM", g);
        messageEngine.drawMessage(440, 0, "Z", g);
        messageEngine.drawMessage(504, 0, "X", g);

        g.setColor(Color.BLACK);
        g.fillRect(healthBarBackround.x, healthBarBackround.y, healthBarBackround.width, healthBarBackround.height);
        //g.fillRect(ultamateBar.x, ultamateBar.y, ultamateBar.width, ultamateBar.height);
        g.fillRect(itemBox.x, itemBox.y, itemBox.width, itemBox.height);
        g.fillRect(rightBox.x, rightBox.y, rightBox.width, rightBox.height);
        g.fillRect(leftBox.x, leftBox.y, leftBox.width, leftBox.height);

         g.setColor(Color.RED);
                    g.fillRect(healthBar.x, healthBar.y, (int)(healthBar.width *health), healthBar.height);
    }
}
