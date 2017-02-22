
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 * Created by Ahmad El-baba on 11/29/2016.
 */
public class HudWindow implements Common, Serializable {

    private static final Logger LOGGER = Logger.getLogger(MainPanel.class.getName());

    //healthBar
    private static Rectangle healthBarBackround;
    private static Rectangle healthBar;
    //healthBar outline
    private static Rectangle outerHealthBar;
    
    
    
    //Item Box
    private static Rectangle itemBox;
    //Item Box outline
    private static Rectangle outerItemBox;
    
    private static Rectangle backround;
    
    //Message Engine
    private static MessageEngine messageEngine;
    private static Item selectedItem;
    private static InventoryEngine inventory;
    
    private double health;
    
    private static Image itemsImage;
    
    
    public HudWindow() {
        messageEngine = new MessageEngine();
        
        inventory = new InventoryEngine();
        backround = new Rectangle(0,0,640,128);
        
        healthBarBackround = new Rectangle(34 , 34 , 220, 28 );
        healthBar = new Rectangle(34 , 34 , 220, 28 );
        outerHealthBar = new Rectangle(32 , 32 , 224, 32 );
        
        /*ultamateBar = new Rectangle(34 , 34 , 220, 28 );
         outerUltamateBar = new Rectangle(32 , 32 , 224, 32 );*/
        
        itemBox = new Rectangle(290 , 34 , 60, 60 );
        outerItemBox = new Rectangle(288 , 32 , 64, 64 );
        
        ImageIcon icon = new ImageIcon(getClass().getResource("image/mapchip.png"));
        itemsImage = icon.getImage();
        
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
        
        
        messageEngine.drawMessage(32, 0, "HEALTH", g);
        messageEngine.drawMessage(288, 0, "ITEM", g);
        
        
        g.setColor(Color.BLACK);
        g.fillRect(healthBarBackround.x, healthBarBackround.y, healthBarBackround.width, healthBarBackround.height);
        //g.fillRect(ultamateBar.x, ultamateBar.y, ultamateBar.width, ultamateBar.height);
        g.fillRect(itemBox.x, itemBox.y, itemBox.width, itemBox.height);
        
        
        g.setColor(Color.RED);
        g.fillRect(healthBar.x, healthBar.y, (int)(healthBar.width *health), healthBar.height);
        if(selectedItem!=null){
            try{
                inventory.drawItem(itemBox.x, itemBox.y, selectedItem, g);
            }catch(Exception e){
                System.out.println(e);
            }
        }
        
        
    }
    
    public Item getSelectedItem() {
        return selectedItem;
    }
    
    
    public void selectItem(Item item){
        selectedItem = item;
    }

}
