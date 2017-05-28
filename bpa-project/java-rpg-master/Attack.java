
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
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
class Attack implements Common, Serializable {

    private static final Logger LOGGER = Logger.getLogger(MainPanel.class.getName());

    private final int PROJECTILE_SPEED = 16; // must be divisable by 32

    // attack's position (unit: tile)
    private int x, y;
    // attack's position (unit: pixel)
    private int px, py;
    // attack's direction (LEFT, RIGHT, UP, or DOWN)
    private int direction;

    // the attack's weapon
    private Weapon weapon;

    // the attack's map
    private Map map;

    // the character the attack is from
    private Character character;

    // weapon's chipset id
    private int id;
    // the weapon chipset's image
    private static BufferedImage image;

    private WaveEngine waveEngine;

    // Sound Clips needed in the attack
    private static final String[] soundNames = {"boop"};

    // thread for attack animation
    private transient Thread threadAnimation;

    // Creates an attack that will draw on screen. If it touches the hero or enemies, it will damage them.
    public Attack(int x, int y, int direction, Weapon weapon, Character character, Map map) {

        this.x = x;
        this.y = y;
        px = x * CS;
        py = y * CS;
        this.direction = direction;
        this.weapon = weapon;
        this.map = map;
        this.character = character;
        this.id = weapon.getId();

        // the weaponchip is loaded if it has not already
        if (image == null) {
            loadImage("image/weapontier1.png");
        }

        waveEngine = new WaveEngine();
        loadSound();

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
                cy + direction * CS,
                cx + CS,
                cy + CS + direction * CS,
                null);
    }

    public void runThread() {
        // run thread
        threadAnimation = new Thread(new Attack.AttackAnimationThread());
        threadAnimation.start();
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

    public Character getCharacter() {
        return character;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    // The area that the attack currently occupies.
    public Rectangle getAttackBox() {
        Rectangle attackBox = new Rectangle(px, py, CS, CS);
        return attackBox;
    }

    public void removeAttack() {
        map.removeAttack(this);
    }

    /**
     * Load the sounds.
     */
    private void loadSound() {
        // load sound clip files
        for (String soundName : soundNames) {
            waveEngine.load(soundName, "sound/" + soundName + ".wav");
        }
    }

    private void loadImage(String filename) {
        try {
            image = ImageIO.read(getClass().getResource(filename));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);

            try {
                CrashReport cr = new CrashReport(e);
                cr.show();
            } catch (Exception n) {
                // do nothing
            }
        }
    }

    // The attack moves across the screen.
    private class AttackAnimationThread extends Thread implements Serializable {

        public void run() {
            waveEngine.play("boop");

            for (int i = 0; i < weapon.getRange() * (CS / PROJECTILE_SPEED); i++) {
                switch (direction) {
                    case UP:
                        py -= PROJECTILE_SPEED;
                        break;
                    case DOWN:
                        py += PROJECTILE_SPEED;
                        break;
                    case LEFT:
                        px -= PROJECTILE_SPEED;
                        break;
                    case RIGHT:
                        px += PROJECTILE_SPEED;
                        break;
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    LOGGER.log(Level.SEVERE, e.toString(), e);

                    try {
                        CrashReport cr = new CrashReport(e);
                        cr.show();
                    } catch (Exception n) {
                        // do nothing
                    }
                }
            }

            removeAttack();
        }
    }
}
