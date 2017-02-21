
import java.awt.*;
import java.io.*;
import java.awt.image.*;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.*;

public class Character implements Common, Serializable {

    private static final Logger LOGGER = Logger.getLogger(MainPanel.class.getName());

    private static final int SPEED = 4;
    public static final double PROB_MOVE = 0.02;
    public static final double PROB_ATTACK = 0.04;
    public static final double ATTACK_INTERVAL = 50.0;

    private static BufferedImage colorImage;
    private static BufferedImage greyscaleImage;
    private static BufferedImage currentImage;
    private int id;

    // character's position (unit: tile)
    private int x, y;
    // character's position (unit: pixel)
    private int px, py;

    // character's direction (LEFT, RIGHT, UP or DOWN)
    private int direction;
    // character's animation counter
    private int count;
    // can this character be damaged?
    private int damageable;

    private boolean isMoving;
    private int moveLength;

    private int moveType;

    // All of the stored messages to be obtained later if one needs to change.
    private String message0 = "";
    private String message1 = "";
    private String message2 = "";
    private String message3 = "";
    private String message4 = "";
    private String message5 = "";
    private String message6 = "";
    private String message7 = "";
    private String message8 = "";
    private String message9 = "";

    private int currentMessage = 0; // The default message is zero.

    // If the character had been attacked
    private boolean isAttacked = false;

    // thread for character animation
    private transient Thread threadAnimation;

    // reference to Map
    private Map map;

    // this is the hero
    private boolean isHero;

    // the hero's quest list
    private Vector<Quest> questList = new Vector();

    // what is this character's weapon
    private Weapon weapon = new Weapon("Long Sword", "This is a sword", 20, 4, 1);

    //Character stats
    private int health = 100;
    private double maxHealth = 100.0;

    //this is the health for the bar
    private double pHealth;
    private int power = 5;
    private int defence = 1;

    // Does this character shoot attacks (0) and if it does, do they attack randomly (1) or normal intervals (2)
    private int attackType;

    // If the character has a normal interval, this is where the counter is stored to ensure the character attacks at the interval
    private int attackCounter;

    /**
     * A character in the game. This can be the main hero or an NPC. The
     * characters can attack, move, and talk.
     *
     * @param x the X coordinate on the map
     * @param y the Y coordinate on the map
     * @param id the character number in order from the sprite map. Starts from
     * 0.
     * @param direction the default direction that the character faces.
     * @param moveType the way the character moves. Can either be 0 or 1. 0
     * means it will not move and 1 means it will move randomly.
     * @param damageable is this character damageable? 0 means no and 1 means
     * yes.
     * @param attackType the character's attack style. 0 = no attack, 1 = random
     * attack, = interval attack.
     * @param map the map the character is a part of
     */
    public Character(int x, int y, int id, int direction,
            int moveType, int damageable, int attackType, Map map) {
        // initialize the character
        this.x = x;
        this.y = y;
        px = x * CS;
        py = y * CS;
        this.id = id;
        this.direction = direction;
        this.moveType = moveType;
        this.damageable = damageable;
        this.attackType = attackType;
        this.map = map;

        // a counter for the animation to swich between the sprites on the character sprite map.
        count = 0;

        // load the sprite map
        if (colorImage == null || greyscaleImage == null) {
            loadImage("image/characters_1-4.png");
        }

        // making a greyscale copy of the sprites for the game over animation
        makeGreyScale(greyscaleImage);
        currentImage = colorImage;

        // run character animation thread
        threadAnimation = new Thread(new AnimationThread());
        threadAnimation.start();
    }

    /**
     * Draw the character.
     *
     * @param g the graphics that the character is drawn with.
     * @param offsetX the offset X coordinate from the main character
     * @param offsetY the offset Y coordinate from the main character
     */
    public void draw(Graphics g, int offsetX, int offsetY) {
        //character sprite map x and y.
        int cx = (id % 4) * (CS * 3);
        int cy = (id / 4) * (CS * 4);

        // switch image based on animation counter.
        // draw the character.
        // the offset ensures that the character will move away as the screen remains centered on the main character when it moves off screen.
        g.drawImage(currentImage,
                px - offsetX,
                py - offsetY,
                px - offsetX + CS,
                py - offsetY + CS,
                cx + count * CS,
                cy + direction * CS,
                cx + CS + count * CS,
                cy + direction * CS + CS,
                null);
    }

    public void runThread() {
        // run character animation thread
        threadAnimation = new Thread(new AnimationThread());
        threadAnimation.start();
    }

    /**
     * Makes a BufferedImage greyscaled.
     *
     * @param img the BufferedImage you want to make greyscale.
     */
    public void makeGreyScale(BufferedImage img) {
        // Get image width and height
        int width = img.getWidth();
        int height = img.getHeight();

        // Convert to grayscale
        for (int pixelY = 0; pixelY < height; pixelY++) {
            for (int pixelX = 0; pixelX < width; pixelX++) {

                // Pull out the pixel we want to manipulate
                int pixel = img.getRGB(pixelX, pixelY);

                // Obtain the ARGB of the pixel.
                // We do not need the alpha value, but we must take it out to access the other three values.
                int alpha = (pixel >> 24) & 0xff;
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;

                // Calculate the average color.
                int average = (red + green + blue) / 3;

                // Replace ARGB value with avg, making it greyscaled.
                pixel = (alpha << 24) | (average << 16) | (average << 8) | average;

                // Set the pixel to the new greyscaled pixel.
                img.setRGB(pixelX, pixelY, pixel);
            }
        }
    }

    /**
     * Set the character to greyscale.
     *
     * @param bool true = turn on greyscale; false = turn off greyscale
     */
    public void setGreyScale(boolean bool) {
        if (bool) {
            currentImage = greyscaleImage;
        } else {
            currentImage = colorImage;
        }
    }

    /**
     * Get the health of this Character.
     *
     * @return the current health value
     */
    public int getHealth() {
        return health;
    }

    /**
     * Change the health of this Character and then update the percentage of max
     * for health bar use.
     *
     * @param health the health to change to
     */
    public void setHealth(int health) {
        this.health = health;
        updateHealthProportions();
    }

    /**
     * Get the power this Character has. Stat is used in calculating attack
     * damage.
     *
     * @return the power of this Character
     */
    public int getPower() {
        return power;
    }

    /**
     * Set the power of this Character.
     *
     * @param power the new power of this Character.
     */
    public void setPower(int power) {
        this.power = power;
    }

    /**
     * Send if this Character has been attacked.
     *
     * @return boolean value of whether or not this Character has been attacked.
     */
    public boolean isAttacked() {
        return isAttacked;
    }

    /**
     * Set whether or not this Character has been attacked. This is used to
     * display the health bar. Eventually this will automatically return to
     * false if it has been awhile since Character has been last attacked.
     *
     * @param isAttacked boolean value of whether or not the Character has been
     * attacked.
     */
    public void setAttacked(boolean isAttacked) {
        this.isAttacked = isAttacked;
    }

    /**
     * Send this Character's defence stat.
     *
     * @return the defence stat of this Character.
     */
    public int getDefence() {
        return defence;
    }

    /**
     * Set the defence of this Character.
     *
     * @param defence the new defence of this Character.
     */
    public void setDefence(int defence) {
        this.defence = defence;
    }

    /**
     * Get the currently wielded weapon of this Character. Used to calculate the
     * Character's Attack.
     *
     * @return the Weapon of this Character.
     */
    public Weapon getWeapon() {
        return weapon;
    }

    /**
     * Change the Weapon of this Character.
     *
     * @param weapon the new Weapon.
     */
    public void setWeapon(Weapon weapon) {
        this.weapon = weapon;
    }

    public int getId() {
        return id;
    }

    /**
     * Move the Character and continue to return true that they are moving until
     * the moving is completed. The Character moves progressively, pixel by
     * pixel. Checks if it can move in the set direction before actually moving.
     * If it cannot move, it won't.
     *
     * @return whether or not it is currently moving.
     */
    public boolean move() {
        switch (direction) {
            case LEFT:
                if (moveLeft()) {
                    // return true if pixel-based scrolling is completed.
                    return true;
                }
                break;
            case RIGHT:
                if (moveRight()) {
                    return true;
                }
                break;
            case UP:
                if (moveUp()) {
                    return true;
                }
                break;
            case DOWN:
                if (moveDown()) {
                    return true;
                }
                break;
        }
        // return false if no movement is made
        return false;
    }

    /**
     * Calculates if the Character can move left. If it can, it does move the
     * Character left.
     *
     * @return if the Character can move left.
     */
    private boolean moveLeft() {
        int nextX = x - 1;
        int nextY = y;
        if (nextX < 0) {
            nextX = 0;
        }
        if (!map.isHit(nextX, nextY)) {
            px -= Character.SPEED;
            if (px < 0) {
                px = 0;
            }
            moveLength += Character.SPEED;
            if (moveLength >= CS) {
                // pixel-based scrolling is completed
                // hero moves to left tile
                x--;
                px = x * CS;
                isMoving = false;
                return true;
            }
        } else {
            isMoving = false;
            px = x * CS;
            py = y * CS;
        }
        return false;
    }

    /**
     * Calculates if the Character can move right. If it can, it does move the
     * Character right.
     *
     * @return if the Character can move right.
     */
    private boolean moveRight() {
        int nextX = x + 1;
        int nextY = y;
        if (nextX > map.getCol() - 1) {
            nextX = map.getCol() - 1;
        }
        if (!map.isHit(nextX, nextY)) {
            px += Character.SPEED;
            if (px > map.getWidth() - CS) {
                px = map.getWidth() - CS;
            }
            moveLength += Character.SPEED;
            if (moveLength >= CS) {
                // pixel-based scrolling is completed
                // hero moves to right tile
                x++;
                px = x * CS;
                isMoving = false;
                return true;
            }
        } else {
            isMoving = false;
            px = x * CS;
            py = y * CS;
        }

        return false;
    }

    /**
     * Calculates if the Character can move up. If it can, it does move the
     * Character up.
     *
     * @return if the Character can move up.
     */
    private boolean moveUp() {
        int nextX = x;
        int nextY = y - 1;
        if (nextY < 0) {
            nextY = 0;
        }
        if (!map.isHit(nextX, nextY)) {
            py -= Character.SPEED;
            if (py < 0) {
                py = 0;
            }
            moveLength += Character.SPEED;
            if (moveLength >= CS) {
                y--;
                py = y * CS;
                isMoving = false;
                return true;
            }
        } else {
            isMoving = false;
            px = x * CS;
            py = y * CS;
        }
        return false;
    }

    /**
     * Calculates if the Character can move down. If it can, it does move the
     * Character down.
     *
     * @return if the Character can move down.
     */
    private boolean moveDown() {
        int nextX = x;
        int nextY = y + 1;
        if (nextY > map.getRow() - 1) {
            nextY = map.getRow() - 1;
        }
        if (!map.isHit(nextX, nextY)) {
            py += Character.SPEED;
            if (py > map.getHeight() - CS) {
                py = map.getHeight() - CS;
            }
            moveLength += Character.SPEED;
            if (moveLength >= CS) {
                y++;
                py = y * CS;
                isMoving = false;
                return true;
            }
        } else {
            isMoving = false;
            px = x * CS;
            py = y * CS;
        }
        return false;
    }

    /**
     * Checks if there is a Character in front of the hero and then make them
     * face the hero.
     *
     * @return the Character in front of the hero.
     */
    public Character talkWith() {
        // The X and Y coordinates of the direction in front of the hero.
        int nextX = 0;
        int nextY = 0;
        switch (direction) {
            case LEFT:
                nextX = x - 1;
                nextY = y;
                break;
            case RIGHT:
                nextX = x + 1;
                nextY = y;
                break;
            case UP:
                nextX = x;
                nextY = y - 1;
                break;
            case DOWN:
                nextX = x;
                nextY = y + 1;
                break;
        }

        // is there a character in front of the hero.
        Character c = map.checkCharacter(nextX, nextY);

        // If there is a Character, set that Character to face the hero.
        if (c != null) {
            switch (direction) {
                case LEFT:
                    c.setDirection(RIGHT);
                    break;
                case RIGHT:
                    c.setDirection(LEFT);
                    break;
                case UP:
                    c.setDirection(DOWN);
                    break;
                case DOWN:
                    c.setDirection(UP);
                    break;
            }
        }
        return c;
    }

    /**
     * Check Search below the hero to see if there is treasure below them.
     *
     * @return the treasure below the hero.
     */
    public TreasureEvent searchForTreasure() {
        // The check the tile below the Character.
        Event event = map.checkEvent(x, y);
        if (event instanceof TreasureEvent) {
            return (TreasureEvent) event;
        }
        return null;
    }

    public TriggerEvent touch() {

        Event event = map.checkEvent(x, y);
        if (event instanceof TriggerEvent) {
            return (TriggerEvent) event;
        }
        return null;
    }

    public QuestEvent questSearch() {
        int nextX = 0;
        int nextY = 0;
        switch (direction) {
            case LEFT:
                nextX = x - 1;
                nextY = y;
                break;
            case RIGHT:
                nextX = x + 1;
                nextY = y;
                break;
            case UP:
                nextX = x;
                nextY = y - 1;
                break;
            case DOWN:
                nextX = x;
                nextY = y + 1;
                break;
        }
        Event event = map.checkEvent(nextX, nextY);
        if (event instanceof QuestEvent) {
            return (QuestEvent) event;
        }
        return null;
    }

    public ShopEvent shopSearch() {
        int nextX = 0;
        int nextY = 0;
        switch (direction) {
            case LEFT:
                nextX = x - 1;
                nextY = y;
                break;
            case RIGHT:
                nextX = x + 1;
                nextY = y;
                break;
            case UP:
                nextX = x;
                nextY = y - 1;
                break;
            case DOWN:
                nextX = x;
                nextY = y + 1;
                break;
        }
        Event event = map.checkEvent(nextX, nextY);
        if (event instanceof ShopEvent) {
            return (ShopEvent) event;
        }
        return null;
    }
    
    /**
     * Check if there is a door in front of the hero.
     *
     * @return the door in front of the hero if there is one.
     */
    public DoorEvent open() {
        // Calculate the coordinates of the tile in front of the character.
        int nextX = 0;
        int nextY = 0;
        switch (direction) {
            case LEFT:
                nextX = x - 1;
                nextY = y;
                break;
            case RIGHT:
                nextX = x + 1;
                nextY = y;
                break;
            case UP:
                nextX = x;
                nextY = y - 1;
                break;
            case DOWN:
                nextX = x;
                nextY = y + 1;
                break;
        }

        // See if there is a door in front of the character.
        Event event = map.checkEvent(nextX, nextY);
        if (event instanceof DoorEvent) {
            return (DoorEvent) event;
        }
        return null;
    }

    /**
     * Set if this is the hero.
     *
     * @param isHero make this Character the hero.
     */
    public void setIsHero(boolean isHero) {
        this.isHero = isHero;
    }

    /**
     * Check if this Character is the hero.
     *
     * @return whether or not this is the hero.
     */
    public boolean getIsHero() {
        return this.isHero;
    }

    /**
     * Get the x coordinate of this Character.
     *
     * @return the x coordinate of this Character.
     */
    public int getX() {
        return x;
    }

    /**
     * Get the y coordinate of this Character.
     *
     * @return the y coordinate of this Character.
     */
    public int getY() {
        return y;
    }

    /**
     * Get the x pixel of this Character.
     *
     * @return the x pixel of this Character.
     */
    public int getPX() {
        return px;
    }

    /**
     * Get the y pixel of this Character.
     *
     * @return the y pixel of this Character.
     */
    public int getPY() {
        return py;
    }

    /**
     * Set the direction this Character is facing.
     *
     * @param direction the new direction the Character should be facing,
     */
    public void setDirection(int direction) {
        this.direction = direction;
    }

    /**
     * Get the direction this Character is facing.
     *
     * @return the direction this Character is facing.
     */
    public int getDirection() {
        return direction;
    }

    /**
     * Check if this Character is moving
     *
     * @return whether or not this Character is moving.
     */
    public boolean isMoving() {
        return isMoving;
    }

    /**
     * Change if this Character is moving or not and reset the length it has
     * moved since last change to zero.
     *
     * @param isMoving set if this Character is moving or not.
     */
    public void setMoving(boolean isMoving) {
        this.isMoving = isMoving;
        moveLength = 0;
    }

    /**
     * Get the currently set message of this Character. Each Character can have
     * up to 10 preset messages, so the one it is currently set to will return.
     *
     * @return the currently selected message.
     */
    public String getMessage() {
        switch (currentMessage) {
            case 0:
                return message0;
            case 1:
                return message1;
            case 2:
                return message2;
            case 3:
                return message3;
            case 4:
                return message4;
            case 5:
                return message5;
            case 6:
                return message6;
            case 7:
                return message7;
            case 8:
                return message8;
            case 9:
                return message9;
            default:
                return "";
        }
    }

    /**
     * Set the current message the Character should display.
     *
     * @param currentMessage the number 0-9, that indicates which message to
     * display.
     */
    public void setCurrentMessage(int currentMessage) {
        this.currentMessage = currentMessage;
    }

    /**
     * Set the message of this character. Change the specified one by putting
     * a-j in front of it (IN LOWERCASE). The message should be in all caps. If
     * no lowercase letter is put in front of it, then it defaults to message0.
     * Can change multiple messages at once by separating the messages with each
     * lowercase letter.
     *
     * @param message the new message to have the character say. Should be all
     * caps. Can include lowercase a-j to indicate certain message to change.
     */
    public void setMessage(String message) {
        int thisMessage = 0; // Current message creating.
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            // Check which message is being changed.
            switch (c) {
                case 'a':
                    thisMessage = 0;
                    break;
                case 'b':
                    thisMessage = 1;
                    break;
                case 'c':
                    thisMessage = 2;
                    break;
                case 'd':
                    thisMessage = 3;
                    break;
                case 'e':
                    thisMessage = 4;
                    break;
                case 'f':
                    thisMessage = 5;
                    break;
                case 'g':
                    thisMessage = 6;
                    break;
                case 'h':
                    thisMessage = 7;
                    break;
                case 'i':
                    thisMessage = 8;
                    break;
                case 'j':
                    thisMessage = 9;
                    break;
                default:
                    // Add each character of the message to the new message.
                    switch (thisMessage) {
                        case 0:
                            message0 += c;
                            break;
                        case 1:
                            message1 += c;
                            break;
                        case 2:
                            message2 += c;
                            break;
                        case 3:
                            message3 += c;
                            break;
                        case 4:
                            message4 += c;
                            break;
                        case 5:
                            message5 += c;
                            break;
                        case 6:
                            message6 += c;
                            break;
                        case 7:
                            message7 += c;
                            break;
                        case 8:
                            message8 += c;
                            break;
                        case 9:
                            message9 += c;
                            break;
                    }
                    break;
            }
        }
    }

    /**
     * Get the way this Character moves.
     *
     * @return the move type of this Character
     */
    public int getMoveType() {
        return moveType;
    }

    /**
     * Get the way this Character attacks.
     *
     * @return the attack type of this Character.
     */
    public int getAttackType() {
        return attackType;
    }

    /**
     * Is this Character damageable? If it is, the damagable will equal 1.
     *
     * @return whether or not this Character is damagable.
     */
    public boolean isDamageable() {
        return damageable == 1;
    }

    /**
     * Damage this Character by reducing its health by the amount put into the
     * parameter. Updates the percentage of the Character's health remaining.
     *
     * @param dmg The amount to damage the Character's health by.
     */
    public void damage(int dmg) {
        this.health -= dmg;
        updateHealthProportions();
    }

    /**
     * Heal this Character by increasing its health by the amount put into the
     * parameter. Updates the percentage of the Character's health remaining.
     *
     * @param heal The amount to heal the Character's health by.
     */
    public void heal(int heal) {
        this.health += heal;
        updateHealthProportions();
    }

    /**
     * Updates the percentage of the Character's health remaining by taking its
     * current health and dividing it by the max health possible.
     */
    public void updateHealthProportions() {
        this.pHealth = health / maxHealth;
    }

    /**
     * Obtain the proportion of this Character's health remaining.
     *
     * @return the percentage of health remaining.
     */
    public double getHealthProportions() {
        return pHealth;
    }

    /**
     * A rectangle of the Character's currently occupied pixel range.
     *
     * @return the rectangle of pixels that this Character currently lies in.
     */
    public Rectangle getHitbox() {
        Rectangle hitbox = new Rectangle(px, py, CS, CS);
        return hitbox;
    }

    /**
     * How long has this Character been consecutively attacking for.
     *
     * @return the counter of how long this Character has been consecutively
     * attacking for.
     */
    public int getAttackCount() {
        return attackCounter;
    }

    /**
     * Increases the attack counter by one to indicate that one game tick has
     * occurred.
     */
    public void increaseAttackCount() {
        attackCounter++;
    }

    /**
     * Loads an image from the resources.
     *
     * @param filename the name of the file to load
     */
    private void loadImage(String filename) {
        try {
            colorImage = ImageIO.read(getClass().getResource(filename));
            greyscaleImage = ImageIO.read(getClass().getResource(filename));
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

    // Animation Class
    private class AnimationThread extends Thread implements Serializable {

        public void run() {
            while (true) {
                // The count changes ever 0.3 seconds to shift between the Character animations.
                if (count == 0) {
                    count = 1;
                } else if (count == 1) {
                    count = 2;
                } else if (count == 2) {
                    count = 0;
                }
                try {
                    Thread.sleep(300);
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
        }
    }
}
