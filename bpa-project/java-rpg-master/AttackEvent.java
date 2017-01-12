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
class AttackEvent extends Event implements Common {
    int direction;
    int attackX;
    int attackY;
    int id;
    
    private static BufferedImage image;
    
    public AttackEvent(int x, int y, int chipNo) {
        super(x, y, chipNo, false);
        
        this.attackX = x;
        this.attackY = y;
        this.id = chipNo;
        
        if (image == null) {
            loadImage("image/mapchip.gif");
        }
    }

    public String toString() {
        return "ATTACK:" + super.toString();
    }
    
    public void draw(Graphics g) {
        int cx = (id % 8) * (CS * 2);
        int cy = (id / 8) * (CS * 4);
        // switch image based on animation counter
        g.drawImage(image,
                    x * CS,
                    y * CS,
                    x * CS,
                    y * CS,
                    cx, // + count * CS,
                    cy + direction * CS,
                    cx + CS, // + count * CS,
                    cy + direction * CS + CS,
                    null);
    }
    
    public void setDirection(int direction) {
        this.direction = direction;
    }
    
    public void performAttack(Vector<Character> characters) {
        
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
            
        }
    }
}
