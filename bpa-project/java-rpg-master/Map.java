
import java.awt.*;
import java.io.*;
import java.util.*;
import java.awt.image.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.*;

public class Map implements Common, Serializable {

    private static final Logger LOGGER = Logger.getLogger(MainPanel.class.getName());

    // map data
    private int[][] map;

    // map size (tile)
    private int row;
    private int col;

    // map size (pixel)
    private int width;
    private int height;

    // chip set for tiles
    private static BufferedImage colorChipSet;
    // greyscale chip set
    private static BufferedImage greyscaleChipSet;
    // temporary image to differentiate between to the two above
    private static BufferedImage currentChipSet;

    // characters in this map
    private ArrayList<Character> characters = new ArrayList<Character>();
    // events in this map
    private ArrayList<Event> events = new ArrayList<Event>();
    // attacks in this map
    private ArrayList<Attack> attacks = new ArrayList<Attack>();

    // reference to MainPanel
    private transient MainPanel panel;

    // the thread to monitor the attacks on characters
    private transient Thread threadDamage;

    // the map file location and the background music location
    private String mapFile;
    private String bgmName;

    // the back of the health bar for when it depletes
    private Rectangle healthBarBackround;
    // the health bar amount
    private Rectangle healthBar;
    // health bar boarder
    private Rectangle outerHealthBar;

    /**
     * The map that is created for the user to view and interact with.
     * Everything that the user sees from the actual map is painted and loaded
     * here.
     *
     * @param mapFile the root location of this map's chipset pattern file
     * @param eventFile the root location of this map's events
     * @param bgmName the root location of this map's background music
     * @param panel the panel this map is painting onto
     */
    public Map(String mapFile, String eventFile, String bgmName, MainPanel panel) {
        // initialize the variables
        this.mapFile = mapFile;
        this.bgmName = bgmName;
        this.panel = panel;

        // load the chip sets and make the greyscaled one in greyscale
        loadMap(mapFile);
        loadEvent(eventFile);
        if (colorChipSet == null || greyscaleChipSet == null) {
            loadImage("image/mapchip.png");
            makeGreyScale(greyscaleChipSet);
        }
        currentChipSet = colorChipSet;

        // initialize the health bar rectangles.
        healthBarBackround = new Rectangle(2, -10, 30, 4);
        healthBar = new Rectangle(2, -10, 30, 4);
        outerHealthBar = new Rectangle(0, -6, 34, 14);

        // run damage thread
        threadDamage = new Thread(new Map.AttackDamageThread());
        threadDamage.start();
    }

    /**
     * Draw the map to the screen.
     *
     * @param g the Graphics the map is being with.
     * @param offsetX the offset X value of the hero.
     * @param offsetY the offset Y value of the hero.
     */
    public void draw(Graphics g, int offsetX, int offsetY) {

        // display xrange of map (unit:pixel)
        int firstTileX = pixelsToTiles(offsetX);
        int lastTileX = firstTileX + pixelsToTiles(MainPanel.WIDTH) + 1;

        // display yrange of map (unit: pixel)
        int firstTileY = pixelsToTiles(offsetY);
        int lastTileY = firstTileY + pixelsToTiles(MainPanel.HEIGHT) + 1;

        // clipping
        lastTileX = Math.min(lastTileX, col);
        lastTileY = Math.min(lastTileY, row);

        // the tiles are painted to the screen
        for (int i = firstTileY; i < lastTileY; i++) {
            for (int j = firstTileX; j < lastTileX; j++) {
                int cx = (map[i][j] % 8) * CS;
                int cy = (map[i][j] / 8) * CS;
                g.drawImage(currentChipSet,
                        tilesToPixels(j) - offsetX,
                        tilesToPixels(i) - offsetY,
                        tilesToPixels(j) - offsetX + CS,
                        tilesToPixels(i) - offsetY + CS,
                        cx, cy, cx + CS, cy + CS, panel);

                // draw tile associated with the event on (i, j)
                for (int n = 0; n < events.size(); n++) {
                    Event event = events.get(n);
                    if (event.x == j && event.y == i) {
                        cx = (event.id % 8) * CS;
                        cy = (event.id / 8) * CS;
                        g.drawImage(currentChipSet,
                                tilesToPixels(j) - offsetX,
                                tilesToPixels(i) - offsetY,
                                tilesToPixels(j) - offsetX + CS,
                                tilesToPixels(i) - offsetY + CS,
                                cx, cy, cx + CS, cy + CS, panel);
                    }
                }
            }
        }

        // check character health in this map for zero
        for (int i = 0; i < characters.size(); i++) {
            Character c = characters.get(i);
            if (c.getHealth() <= 0) {
                panel.checkifQuestCharacter(c);
                removeCharacter(c);
            }
        }

        // draw characters in this map
        for (int i = 0; i < characters.size(); i++) {
            Character c = characters.get(i);
            c.draw(g, offsetX, offsetY);
        }

        // draw attacks in this map
        for (int i = 0; i < attacks.size(); i++) {
            Attack a = attacks.get(i);
            a.draw(g, offsetX, offsetY);
        }

        //draw health bar for characters
        for (int i = 0; i < characters.size(); i++) {
            Character c = characters.get(i);
            drawHealthBar(c, g, offsetX, offsetY);
        }
    }

    /**
     * Check the tile the hero wants to move to next if it can be moved on.
     * Various tiles other Characters, and certain Events cannot be moved on.
     *
     * @param x the x position the hero wants to be moved to.
     * @param y the y position the hero wants to be moved to.
     * @return if the hero can move there (false) or not (true)
     */
    public boolean isHit(int x, int y) {
        // Check if there are tiles here that cannot be passed.
        if (map[y][x] == 1
                || // wall
                map[y][x] == 2
                || // throne
                map[y][x] == 5
                || // sea
                map[y][x] == 6
                || // lava
                map[y][x] == 7
                || // tree
                map[y][x] == 8
                || // rock1
                map[y][x] == 9
                || // rock2
                map[y][x] == 19
                || // brick
                map[y][x] == 20
                || // wall1
                map[y][x] == 21
                || // wall2
                map[y][x] == 22
                || // wall3
                map[y][x] == 23) {    // wall4
            return true;
        }

        // Are there other characters?
        for (int i = 0; i < characters.size(); i++) {
            Character c = characters.get(i);
            if (c.getX() == x && c.getY() == y) {
                return true;
            }
        }

        // Are there events that cannot be walked on?
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            if (event.x == x && event.y == y) {
                return event.isHit;
            }
        }

        return false;
    }

    public void runThread() {
        // run damage thread
        threadDamage = new Thread(new Map.AttackDamageThread());
        threadDamage.start();
    }

    public void runAttackThread() {
        for (Attack attack : attacks) {
            attack.runThread();
        }
    }

    public void runCharacterThread() {
        for (Character character : characters) {
            character.runThread();
        }
    }

    /**
     * Add a Character to this map.
     *
     * @param c a Character to add to this map.
     */
    public void addCharacter(Character c) {
        characters.add(c);
    }

    /**
     * Remove a Character from this map.
     *
     * @param c a Character to remove from this map.
     */
    public void removeCharacter(Character c) {
        characters.remove(c);
    }

    /**
     * Add an Attack to this map.
     *
     * @param a an Attack to add to this map.
     */
    public void addAttack(Attack a) {
        attacks.add(a);
    }

    /**
     * Remove a Character from this map.
     *
     * @param a an Attack to remove from this map.
     */
    public void removeAttack(Attack a) {
        attacks.remove(a);
    }

    /**
     * Remove an event from this map.
     *
     * @param event an Event to remove from this map.
     */
    public void removeEvent(Event event) {
        events.remove(event);
    }

    /**
     * Check if there is a Character in the (x,y) coordinate on this map.
     *
     * @param x the x coordinate of this map to check.
     * @param y the y coordinate of this map to check.
     * @return the Character in the spot.
     */
    public Character checkCharacter(int x, int y) {
        for (int i = 0; i < characters.size(); i++) {
            Character c = characters.get(i);
            if (c.getX() == x && c.getY() == y) {
                return c;
            }
        }
        return null;
    }

    /**
     * Check if there is an Attack in the (x,y) coordinate on this map.
     *
     * @param x the x coordinate of this map to check.
     * @param y the y coordinate of this map to check.
     * @return the Attack in the spot.
     */
    public Attack checkAttack(int x, int y) {
        for (int i = 0; i < attacks.size(); i++) {
            Attack a = attacks.get(i);
            if (a.getX() == x && a.getY() == y) {
                return a;
            }
        }
        return null;
    }

    /**
     * Check if there is an Event in the (x,y) coordinate on this map.
     *
     * @param x the x coordinate of this map to check.
     * @param y the y coordinate of this map to check.
     * @return the Event in the spot.
     */
    public Event checkEvent(int x, int y) {
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            if (event.x == x && event.y == y) {
                return event;
            }
        }
        return null;
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
     * Set the map to greyscale.
     *
     * @param bool true = turn on greyscale; false = turn off greyscale
     */
    public void setGreyScale(boolean bool) {
        if (bool) {
            currentChipSet = greyscaleChipSet;
        } else {
            currentChipSet = colorChipSet;
        }

        // Check each Character in this map and make each one of them greyscaled.
        for (int i = 0; i < characters.size(); i++) {
            characters.get(i).setGreyScale(bool);
        }
    }

    /**
     * Convert from pixel to tile. Divides the pixel input by the constant CS,
     * which is the number of pixels per tile.
     *
     * @param pixels the pixel location to find the tile it is on.
     * @return the number tile that the pixel is from, starting from 0 and
     * moving up.
     */
    public static int pixelsToTiles(double pixels) {
        return (int) Math.floor(pixels / CS);
    }

    /**
     * Convert from tile to pixel. Multiplies the tile input by the CS, which is
     * the number of pixels per tile.
     *
     * @param tiles the pixel location to find the tile it is on.
     * @return the number pixel that the starts from, starting from 0 and moving
     * up in intervals of CS.
     */
    public static int tilesToPixels(int tiles) {
        return tiles * CS;
    }

    /**
     * The number of rows of tiles this map has.
     *
     * @return the number of rows.
     */
    public int getRow() {
        return row;
    }

    /**
     * The number of columns this map has.
     *
     * @return the number of columns.
     */
    public int getCol() {
        return col;
    }

    /**
     * The number of pixels that make up this Map's width.
     *
     * @return the number of pixels.
     */
    public int getWidth() {
        return width;
    }

    /**
     * The number of pixels that make up this Map's height.
     *
     * @return the number of pixels.
     */
    public int getHeight() {
        return height;
    }

    /**
     * The Characters on this map.
     *
     * @return the ArrayList of Characters.
     */
    public ArrayList<Character> getCharacters() {
        return characters;
    }

    /**
     * The Attacks on this map.
     *
     * @return the ArrayList of Attacks.
     */
    public ArrayList<Attack> getAttacks() {
        return attacks;
    }

    /**
     * The Background music file of this map.
     *
     * @return the background music file location of this Map.
     */
    public String getBgmName() {
        return bgmName;
    }

    /**
     * The tile file of this map.
     *
     * @return the map tile file location location of this Map.
     */
    public String getMapName() {
        return mapFile;
    }

    /**
     * Check the ArrayList of characters to find the one that is the hero.
     *
     * @return the hero when it is found.
     */
    public Character getHero() {
        for (int i = 0; i < characters.size(); i++) {
            Character c = characters.get(i);

            if (c.getIsHero()) {
                return c;
            }
        }

        return null;
    }

    /**
     * Load the tile map file.
     *
     * @param filename the location of the tiles to load.
     */
    private void loadMap(String filename) {
        try {
            BufferedReader e = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(filename)));
            String line = e.readLine();
            this.row = Integer.parseInt(line);
            line = e.readLine();
            this.col = Integer.parseInt(line);
            this.width = this.col * 32;
            this.height = this.row * 32;
            this.map = new int[this.row][this.col];

            for (int i = 0; i < this.row; ++i) {
                line = e.readLine();
                StringTokenizer st = new StringTokenizer(line, ",");

                for (int j = 0; j < this.col; ++j) {
                    this.map[i][j] = Integer.parseInt(st.nextToken());
                }
            }
        } catch (IOException | NumberFormatException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);

            try {
                CrashReport cr = new CrashReport(e);
                cr.show();
            } catch (Exception n) {
                // do nothing
            }
        }
    }

    /**
     * Load the events file for this map.
     *
     * @param filename the location of the event file to load.
     */
    private void loadEvent(String filename) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    getClass().getResourceAsStream(filename), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                // skip null lines
                if (line.equals("")) {
                    continue;
                }
                // skip comment lines
                if (line.startsWith("#")) {
                    continue;
                }
                StringTokenizer st = new StringTokenizer(line, ",");
                String eventType = st.nextToken();
                if (eventType.equals("CHARACTER")) {
                    // create a character
                    makeCharacterEvent(st);
                } else if (eventType.equals("TREASURE")) {
                    // create a treasure chest
                    makeTreasureEvent(st);
                } else if (eventType.equals("DOOR")) {
                    // create a door
                    makeDoorEvent(st);
                } else if (eventType.equals("MOVE")) {
                    // create a warp location
                    makeMoveEvent(st);
                } else if (eventType.equals("QUEST")) {
                    makeQuestEvent(st);
                } else if (eventType.equals("TRIGGER")) {
                    makeTriggerEvent(st);
                } else if (eventType.equals("SAVE")) {
                    makeSaveEvent(st);
                } else if (eventType.equals("SHOP")) {
                    // create a treasure chest
                    makeShopEvent(st);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);

            try {
                CrashReport cr = new CrashReport(e);
                cr.show();
            } catch (Exception n) {
                // do nothing
            }
        }
    }

    /**
     * Load an image from the image folder for the chipset.
     *
     * @param filename the location of the chipset to load.
     */
    private void loadImage(String filename) {
        try {
            colorChipSet = ImageIO.read(getClass().getResource(filename));
            greyscaleChipSet = ImageIO.read(getClass().getResource(filename));
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

    private void makeCharacterEvent(StringTokenizer st) {
        int x = Integer.parseInt(st.nextToken());
        int y = Integer.parseInt(st.nextToken());
        int id = Integer.parseInt(st.nextToken());
        int direction = Integer.parseInt(st.nextToken());
        int moveType = Integer.parseInt(st.nextToken());
        int damagable = Integer.parseInt(st.nextToken());
        int attack = Integer.parseInt(st.nextToken());
        String message = st.nextToken();
        Character c = new Character(x, y, id, direction, moveType, damagable, attack, this);
        c.setMessage(message);
        characters.add(c);
    }

    private void makeTreasureEvent(StringTokenizer st) {
        int x = Integer.parseInt(st.nextToken());
        int y = Integer.parseInt(st.nextToken());
        String itemName = st.nextToken();
        String itemType = st.nextToken();
        TreasureEvent t = new TreasureEvent(x, y, itemName, itemType);
        events.add(t);
    }

    private void makeShopEvent(StringTokenizer st) {
        int x = Integer.parseInt(st.nextToken());
        int y = Integer.parseInt(st.nextToken());
        ShopEvent s = new ShopEvent(x, y);
        events.add(s);
    }

    private void makeQuestEvent(StringTokenizer st) {
        int x = Integer.parseInt(st.nextToken());
        int y = Integer.parseInt(st.nextToken());
        String questType = st.nextToken();
        String questName = st.nextToken();
        String questDisctription = st.nextToken();
        int expGained = Integer.parseInt(st.nextToken());
        String reward = st.nextToken();
        int DX = Integer.parseInt(st.nextToken());
        int DY = Integer.parseInt(st.nextToken());
        int target = Integer.parseInt(st.nextToken());
        QuestEvent s = new QuestEvent(x, y, questType, questName, questDisctription, expGained, reward, DX, DY, target);
        events.add(s);
    }

    private void makeDoorEvent(StringTokenizer st) {
        int x = Integer.parseInt(st.nextToken());
        int y = Integer.parseInt(st.nextToken());
        DoorEvent d = new DoorEvent(x, y);
        events.add(d);
    }

    private void makeTriggerEvent(StringTokenizer st) {
        int x = Integer.parseInt(st.nextToken());
        int y = Integer.parseInt(st.nextToken());
        TriggerEvent tr = new TriggerEvent(x, y);
        events.add(tr);
    }

    private void makeMoveEvent(StringTokenizer st) {
        int x = Integer.parseInt(st.nextToken());
        int y = Integer.parseInt(st.nextToken());
        int chipNo = Integer.parseInt(st.nextToken());
        int destMapNo = Integer.parseInt(st.nextToken());
        int destX = Integer.parseInt(st.nextToken());
        int destY = Integer.parseInt(st.nextToken());
        MoveEvent m = new MoveEvent(x, y, chipNo, destMapNo, destX, destY);
        events.add(m);
    }

    private void makeSaveEvent(StringTokenizer st) {
        int x = Integer.parseInt(st.nextToken());
        int y = Integer.parseInt(st.nextToken());
        SaveEvent s = new SaveEvent(x, y);
        events.add(s);
    }

    public void show() {
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                System.out.print(map[i][j]);
            }
            System.out.println();
        }
    }

    public void setPanel(MainPanel panel) {
        this.panel = panel;
    }

    private void drawHealthBar(Character c, Graphics g, int offsetX, int offsetY) {
        if (c.isAttacked()) {
            g.setColor(Color.BLACK);
            g.fillRect(healthBarBackround.x + c.getPX() - offsetX, healthBarBackround.y + c.getPY() - offsetY, healthBarBackround.width, healthBarBackround.height);
            g.setColor(Color.RED);
            g.fillRect(healthBar.x + c.getPX() - offsetX, healthBar.y + c.getPY() - offsetY, (int) (healthBar.width * c.getHealthProportions()), healthBar.height);
        }
    }

    private class AttackDamageThread extends Thread implements Serializable {

        public void run() {
            while (true) {
                attack:
                for (int i = 0; i < characters.size(); i++) {
                    for (int j = 0; j < attacks.size(); j++) {
                        Character c = characters.get(i);
                        // This try-catch ensures that if an attack removes itself while in the middle of checking its location with the Characters, it is not game breaking.
                        // In the case that one does disappear mid-check, then this thread would crash and characters would stop taking damage.
                        // The try-catch prevents the thread from crashing and therefore continuing to work.
                        try {
                            Attack a = attacks.get(j);

                            if (!a.getCharacter().getIsHero()) {
                                // When the character is not the hero, they can only attack the hero.
                                if (c.getHitbox().contains(new Point(a.getPX(), a.getPY())) && c.isDamageable() && a.getCharacter() != c && c.getIsHero()) {
                                    c.damage(a.getWeapon().getDamage());
                                    c.setAttacked(true);
                                    a.removeAttack();
                                    break attack;
                                }
                            } else // When the character is the hero, they can attack all attackable characters.
                             if (c.getHitbox().contains(new Point(a.getPX(), a.getPY())) && c.isDamageable() && a.getCharacter() != c) {
                                    c.damage(a.getWeapon().getDamage());
                                    c.setAttacked(true);
                                    a.removeAttack();
                                    break attack;
                                }
                        } catch (Exception e) {
                            LOGGER.log(Level.INFO, e.toString(), e);
                            try {
                                LogReport lr = new LogReport("Attack disappeared while checking:\n\t" + e);
                                lr.saveLog();
                            } catch (IOException ex) {
                                // do nothing
                            }
                        }
                    }
                }

                try {
                    Thread.sleep(20);
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
