import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Vector;
import javax.imageio.ImageIO;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hpro1
 */
class Attack implements Common {
    
    private final int PROJECTILE_SPEED = 16; // must be divisable by 32
    
    // attack's position (unit: tile)
    private int x, y;
    // attack's position (unit: pixel)
    private int px, py;
    // attack's direction (LEFT, RIGHT, UP, or DOWN)
    private int direction;
    // attack's movement position
    private int my, mx;
    
    // the attack's weapon
    private Weapon weapon;
    
    // the attack's map
    private Map map;
    
    // weapon's chipset id
    private int id = 5;
    // the weapon chipset's image
    private static BufferedImage image;
    
    // thread for attack animation
    private Thread threadAnimation;
    
    public Attack(int x, int y, int direction, Weapon weapon, Map map) {
        
        this.x = x;
        this.y = y;
        px = x * CS;
        py = y * CS;
        this.direction = direction;
        this.weapon = weapon;
        this.map = map;
        
        // the weaponchip is loaded if it has not already
        if (image == null) {
            loadImage("image/mapchip.gif");
        }
        
        // run thread
        threadAnimation = new Thread(new Attack.AttackAnimationThread());
        threadAnimation.start();
    }
    
    public void draw(Graphics g, int offsetX, int offsetY) {
        // calculate the x and y values of the chipset
        int cx = (id % 8) * (CS);
        int cy = (id / 8) * (CS * 4);
        
        // move attack projectile based on animation
        g.drawImage(image,
                    px - offsetX,
                    py - offsetY,
                    px + CS - offsetX,
                    py + CS - offsetY,
                    cx,
                    cy,//+ direction * CS,
                    cx + CS,
                    cy + CS,// + direction * CS,
                    null);
    }
    
    public void setDirection(int direction) {
        this.direction = direction;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getPX() {
        return px;
    }
    
    public int getPY() {
        return py;
    }
    
    public Weapon getWeapon() {
        return weapon;
    }

    public Rectangle getAttackBox() {
        Rectangle attackBox = new Rectangle(px, py, CS, CS);
        return attackBox;
    }
    
    private void removeAttack() {
        map.removeAttack(this);
    }
    
    private void loadImage(String filename) {
        try {
            image = ImageIO.read(getClass().getResource(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private class AttackAnimationThread extends Thread {
        public void run() {
            for (int i = 0; i < 2; i++) { //weapon.getRange() * (CS/PROJECTILE_SPEED
                switch (direction) {
                    case UP:
                        py-=PROJECTILE_SPEED;
                        break;
                    case DOWN:
                        py+=PROJECTILE_SPEED;
                        break;
                    case LEFT:
                        px-=PROJECTILE_SPEED;
                        break;
                    case RIGHT:
                        px+=PROJECTILE_SPEED;
                        break;
                }
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            removeAttack();
        }
    }
}
