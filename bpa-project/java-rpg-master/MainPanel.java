import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
import javax.swing.Timer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

class MainPanel extends JPanel implements KeyListener, Runnable, Common, ActionListener {
    
    // The dimension of the game's panel.
    public static final int WIDTH = 640;
    public static final int HEIGHT = 640;

    // 20ms/frame = 50fps
    private static final int PERIOD = 20;

    // debug mode
    private static final boolean DEBUG_MODE = true;

    // map list
    private Map[] maps;
    // current map number
    private int mapNo;

    // our hero!
    private Character hero;
    
    // action keys
    private ActionKey leftKey;
    private ActionKey rightKey;
    private ActionKey upKey;
    private ActionKey downKey;
    private ActionKey enterKey;
    private ActionKey inventoryKey;
    private ActionKey questKey;
    private ActionKey attackKey;
    private ActionKey tabKey;

    // thread that loops the painting of images on screen
    private Thread gameLoop;
    
    // random number initialized
    private Random rand = new Random();

    /* START: declaration of window variables.
       The variables are followed by a rectangle with the dimensions of the base of the window */
    
    // The window for the text box.
    private MessageWindow messageWindow;
    private static Rectangle WND_RECT = new Rectangle(142, 480, 356, 140);

    // The window for the inventory.
    private InventoryWindow inventoryWindow;
    private static Rectangle INV_RECT = new Rectangle(64, 96, 512, 352);
    
    // The window for the quest list.
    private QuestWindow questWindow;
    private static Rectangle QUE_RECT = new Rectangle(64, 96, 512, 352);
    
    // The window for the Heads Up Display at the top of the screen.
    // This is refered to as the HUD throughout the code.
    private HudWindow hudWindow;

    // The engines for the sounds are declared and initialized.
    private MidiEngine midiEngine = new MidiEngine();
    private WaveEngine waveEngine = new WaveEngine();

    // Quests
    int currentQuest = 0;
    // creating quest sample  createQuest(questList[currentQuest], "TEST", "TEST DISCRIPTON", 10, "HOLY SWORD");
    
    // BGM
    // from TAM Music Factory http://www.tam-music.com/
    private static final String[] bgmNames = {"castle", "field"};
    // Sound Clip
    private static final String[] soundNames = {"treasure", "door", "step", "beep"};

    // Double buffering for the graphics displayed on screen to eliminate flickering.
    private Graphics dbg; // dbg = double buffer graphic
    private Image dbImage = null; // dbImage = double buffer Image

    // Timer for the game over animation
    private Timer timer;
    // Keeps track of the alpha value of the fade in of the game over animation.
    private float alpha = 0f;
    // The game over image.
    private BufferedImage gameOver;
    // Whether there is a game over or not.
    private boolean isGameOver = false;
    
    public MainPanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        
        setFocusable(true);
        addKeyListener(this);

        // Create action keys.
        leftKey = new ActionKey();
        rightKey = new ActionKey();
        upKey = new ActionKey();
        downKey = new ActionKey();
        tabKey = new ActionKey(ActionKey.DETECT_INITIAL_PRESS_ONLY);
        enterKey = new ActionKey(ActionKey.DETECT_INITIAL_PRESS_ONLY);
        inventoryKey = new ActionKey(ActionKey.DETECT_INITIAL_PRESS_ONLY);
        questKey = new ActionKey(ActionKey.DETECT_INITIAL_PRESS_ONLY);
        attackKey = new ActionKey(ActionKey.SLOWER_INPUT);

        // Create maps.
        maps = new Map[3];
        maps[0] = new Map("map/castle.map", "event/castle.evt", "castle", this);
        maps[1] = new Map("map/field.map", "event/field.evt", "field", this);
        maps[2] = new Map("map/map.map", "event/map.evt", "field", this);
        mapNo = 0;  // initial map
        
        // Create the main character, our hero.
        // This is also the start point of the game.
        hero = new Character(6, 6, 0, DOWN, 0, 1, 0, maps[mapNo]);
        hero.setIsHero(true);

        // Add characters to the map.
        maps[mapNo].addCharacter(hero);

        // Create message window.
        messageWindow = new MessageWindow(WND_RECT);

        // Create inventory window.
        inventoryWindow = new InventoryWindow(INV_RECT);

        // Create quest window.
        questWindow = new QuestWindow(QUE_RECT);

        // Create HUD.
        hudWindow = new HudWindow();
        
        // Load Backgound Music (BGM) and sound clips.
        loadSound();

        // The background music of the initial map plays.
        midiEngine.play(maps[mapNo].getBgmName());

        // Start game loop.
        gameLoop = new Thread(this);
        gameLoop.start();
        
        timer = new Timer (20, this);
    }

    @Override
    public void run() {
        long beforeTime, timeDiff, sleepTime;

        beforeTime = System.currentTimeMillis();
        while (true) {
            checkInput();
            gameUpdate();
            gameRender();
            printScreen();
            heroAlive();

            timeDiff = System.currentTimeMillis() - beforeTime;
            sleepTime = PERIOD - timeDiff;
            // sleep at least 5ms
            if (sleepTime <= 0) {
                sleepTime = 5;
            }

            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            beforeTime = System.currentTimeMillis();
        }
    }

    private void checkInput() {
        
        /* Checks if any of the windows are visable.
           If they are not visable, the main window is useable.
           If any are visable, their input is checked.
           They are organized in a hierarchy of priority with the message window on top. */
        if (messageWindow.isVisible()) {
            messageWindowCheckInput();
        } else if (inventoryWindow.isVisible()) {
            inventoryWindowCheckInput();
        } else if (questWindow.isVisible()) {
            questWindowCheckInput();
        } else {
            mainWindowCheckInput();
        }
    }

    private void gameUpdate() {
        // If only the main panel is visable, then the activities that reside within it function.
        if (!messageWindow.isVisible() && !inventoryWindow.isVisible() && !questWindow.isVisible()) {
            heroMove();
            characterMove();
            characterAttack();
            updateStats();
        }
    }
    public void updateStats() {
        // update health, stats, items,
        hudWindow.updateHealth(hero.getHealth());
    }

    private void gameRender() {
        if (dbImage == null) {
            // Buffer image to prevent flickering
            dbImage = createImage(WIDTH, HEIGHT);
            if (dbImage == null) {
                return;
            } else {
                // device context of buffer image
                dbg = dbImage.getGraphics();
            }
        }

        // The default background is set to black.
        dbg.setColor(Color.BLACK);
        dbg.fillRect(0, 0, WIDTH, HEIGHT);

        // Calculate offset so that the hero is in the center of a screen.
        int offsetX = hero.getPX() - MainPanel.WIDTH / 2;
        // Character does not scroll at the edge of the map on the X-axis.
        if (offsetX < 0) {
            offsetX = 0;
        } else if (offsetX > maps[mapNo].getWidth() - MainPanel.WIDTH) {
            offsetX = maps[mapNo].getWidth() - MainPanel.WIDTH;
        }

        int offsetY = hero.getPY() - MainPanel.HEIGHT / 2;
        // Character does not scroll at the edge of the map on the Y-axis.
        if (offsetY < 0) {
            offsetY = 0;
        } else if (offsetY > maps[mapNo].getHeight() - MainPanel.HEIGHT) {
            offsetY = maps[mapNo].getHeight() - MainPanel.HEIGHT;
        }
        
        // Draw current map.
        maps[mapNo].draw(dbg, offsetX, offsetY);
        
        // Draw HUD window.
        hudWindow.draw(dbg);
        
        // Draw message window.
        messageWindow.draw(dbg);
        
        // Draw message window.
        inventoryWindow.draw(dbg);
        
        // Draw quest window.
        questWindow.draw(dbg);
        
        if (isGameOver) {

            try {
                gameOver = ImageIO.read(getClass().getResource("image/gameover.png"));
            } catch (IOException ex) {
                Logger.getLogger(MainPanel.class.getName()).log(Level.SEVERE, null, ex);
            }

            Graphics2D g2d = (Graphics2D) dbg;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            dbg.drawImage(gameOver, 64, 200, null);

            maps[mapNo].setGreyScale(true);
        }
        
        // Display debug information if the mode is enabled (accessible through the boolean at the top of this Class).
        // This helps with figuring out what tiles need specific entities and where issues occur.
        if (DEBUG_MODE) {
            Font font = new Font("SansSerif", Font.BOLD, 16);
            dbg.setFont(font);
            dbg.setColor(Color.YELLOW);
            dbg.drawString(maps[mapNo].getMapName() + " (" + maps[mapNo].getCol() + "," + maps[mapNo].getRow() + ")", 4, 16);
            dbg.drawString("(" + hero.getX() + "," + hero.getY() + ") ", 4, 32);
            dbg.drawString("(" + hero.getPX() + "," + hero.getPY() + ")", 4, 48);
            dbg.drawString(maps[mapNo].getBgmName(), 4, 64);
        }
    }

    private void printScreen() {
        Graphics g = getGraphics();
        // When both the graphics and double buffer are not null, then the image is drawn to the screen.
        if ((g != null) && (dbImage != null)) {
            g.drawImage(dbImage, 0, 0, null);
        }
        Toolkit.getDefaultToolkit().sync(); // The graphics state is syncronized and up to date.
        // Disposes of the  temporary graphic now that it has been drawn to the screen.
        if (g != null) {
            g.dispose();
        }
    }
    
    private void mainWindowCheckInput() {
        // The hero is moved left if there are no obsticles in the way.
        if (leftKey.isPressed()) {
            if (!hero.isMoving()) {
                hero.setDirection(LEFT);
                hero.setMoving(true);
            }
        }
        // The hero is moved right if there are no obsticles in the way.
        if (rightKey.isPressed()) {
            if (!hero.isMoving()) {
                hero.setDirection(RIGHT);
                hero.setMoving(true);
            }
        }
        // The hero is moved up if there are no obsticles in the way.
        if (upKey.isPressed()) {
            if (!hero.isMoving()) {
                hero.setDirection(UP);
                hero.setMoving(true);
            }
        }
        // The hero is moved down if there are no obsticles in the way.
        if (downKey.isPressed()) {
            if (!hero.isMoving()) {
                hero.setDirection(DOWN);
                hero.setMoving(true);
            }
        }
        // An action is performed if there are object events in front of or under the hero, depending on the case.
        if (enterKey.isPressed()) {
            
            // Door is opened if there is one in front of the hero.
            DoorEvent door = hero.open();
            if (door != null) {
                waveEngine.play("door"); // A sound plays in the game that indicates a door opening.
                maps[mapNo].removeEvent(door); // The door is removed from the map.
                return;
            }

            // Cannot open text window if hero is moving, but can open doors while moving (see above code).
            // This allows for clean gameplay.
            if (hero.isMoving()) {
                return; // Exits this method.
            }

            // Search underneath the character:
            // Looking for a treasure chest.
            TreasureEvent treasure = hero.searchForTreasure();
            if (treasure != null) {
                waveEngine.play("treasure"); // Sound of chest opening.
                messageWindow.show(); // The message window appears to display to the user what the item in the chest is.
                if (!inventoryWindow.isFull()) {
                    inventoryWindow.add(treasure.toItem()); // Item is added based on positioning on item chip image.
                    messageWindow.setMessage("HERO DISCOVERED/" + treasure.getItemName());
                } else { // When the inventory of the hero is full, the user must choose where to make room.
                    messageWindow.setMessage("HERO DISCOVERED/" + treasure.getItemName() + "|YOUR INVENTORY IS/FULL! YOU NEED TO/MAKE SPACE!");
                    // Action key movement types are changed to work with the inventory.
                    leftKey = new ActionKey(ActionKey.SLOWER_INPUT);
                    rightKey = new ActionKey(ActionKey.SLOWER_INPUT);
                    upKey = new ActionKey(ActionKey.SLOWER_INPUT);
                    downKey = new ActionKey(ActionKey.SLOWER_INPUT);
                    
                    // Inventory window is opened with the "TRASH_ITEM" mode enabled to make space for new item.
                    inventoryWindow.show(InventoryWindow.TRASH_ITEM, treasure.toItem());
                }
                // The treasure chest is removed from the map.
                maps[mapNo].removeEvent(treasure);
                return;
            }

            // Hero talks with character in front of him.
            if (!messageWindow.isVisible()) {
                Character c = hero.talkWith();
                if (c != null) {
                    messageWindow.setMessage(c.getMessage()); // The message of the character appears.
                    messageWindow.show();
                } else { // When there is no one in front of the hero, a default message appears.
                    messageWindow.setMessage("THERE IS NO ONE/IN THAT DIRECTION");
                    messageWindow.show();
                }
            }
        }

        // The inventory window appears.
        if (inventoryKey.isPressed()) {
            // Set movement keys to take input slower.
            leftKey = new ActionKey(ActionKey.SLOWER_INPUT);
            rightKey = new ActionKey(ActionKey.SLOWER_INPUT);
            upKey = new ActionKey(ActionKey.SLOWER_INPUT);
            downKey = new ActionKey(ActionKey.SLOWER_INPUT);
            inventoryWindow.show();
        }
        
        // The quest window appears.
        if (questKey.isPressed()) {
            // Set movement keys to take input slower.
            leftKey = new ActionKey(ActionKey.SLOWER_INPUT);
            rightKey = new ActionKey(ActionKey.SLOWER_INPUT);
            upKey = new ActionKey(ActionKey.SLOWER_INPUT);
            downKey = new ActionKey(ActionKey.SLOWER_INPUT);
            questWindow.show();
        }

        // The hero attacks.
        if (attackKey.isPressed()) {
            // An attack is created on the tile the hero is on.
            Attack attack = new Attack(hero.getX(), hero.getY(), hero.getDirection(), hero.getWeapon(), hero, maps[mapNo]);
            // The attack is added to the map.
            maps[mapNo].addAttack(attack);
        }
    }

    private void messageWindowCheckInput() {
        // Moves to the next page or exits the text box if there is no text left.
        if (enterKey.isPressed()) {
            if (messageWindow.nextPage()) {
                messageWindow.hide();
            }
        }
    }

    private void inventoryWindowCheckInput() {
        // The inventory cursor is moved left if there is not a border.
        if (leftKey.isPressed()) {
            inventoryWindow.setDirection(LEFT);
        }
        // The inventory cursor is moved right if there is not a border.
        if (rightKey.isPressed()) {
            inventoryWindow.setDirection(RIGHT);
        }
        // The inventory cursor is moved up if there is not a border.
        if (upKey.isPressed()) {
            inventoryWindow.setDirection(UP);
        }
        // The inventory cursor is moved down if there is not a border.
        if (downKey.isPressed()) {
            inventoryWindow.setDirection(DOWN);
        }
        // When tab is pressed, the cursor moves to the next box on the inventory screen.
        if (tabKey.isPressed()) {
            inventoryWindow.nextFocus();
        }
        // When the inventory key is pressed, it closes the inventory while in the inventory.
        if (inventoryKey.isPressed()) {
            // set movement keys back to constant input
            leftKey = new ActionKey();
            rightKey = new ActionKey();
            upKey = new ActionKey();
            downKey = new ActionKey();
            inventoryWindow.hide(); // Hides the inventory window from view.
        }
        // When the quest key is pressed, the inventory is closed and the quest window is opened.
        if (questKey.isPressed()) {
            inventoryWindow.hide();
            //questWindow.setQuestList(hero.getQuestList());
            questWindow.show();
        }
        // The enter key selects what ever the cursor is on.
        if (enterKey.isPressed()) {
            inventoryWindow.select();
        }
    }
    
    private void questWindowCheckInput() {
        // When the quest key is pressed again, the quest window is closed.
        if (questKey.isPressed()) {
            // set movement keys back to constant input
            leftKey = new ActionKey();
            rightKey = new ActionKey();
            upKey = new ActionKey();
            downKey = new ActionKey();
            questWindow.hide();
        }
        // The cursor moves up and down the list of quests to see their description, reward, and requirements.
        if (upKey.isPressed()) {
            questWindow.setDirection(UP);
        }
        if (downKey.isPressed()) {
            questWindow.setDirection(DOWN);
        }
        // When the inventory key is pressed, the quest window is closed and the inventory is opened.
        if (inventoryKey.isPressed()) {
            questWindow.hide();
            inventoryWindow.show();
        }
    }
    
    private void heroMove() {
        if (hero.isMoving()) {
            if (hero.move()) {
                Event event = maps[mapNo].checkEvent(hero.getX(), hero.getY());
                if (event instanceof MoveEvent) {
                    waveEngine.play("step");
                    // Move to new map.
                    MoveEvent m = (MoveEvent)event;
                    maps[mapNo].removeCharacter(hero);
                    mapNo = m.destMapNo;
                    hero = new Character(m.destX, m.destY, 0, DOWN, 0, 1, 0, maps[mapNo]);
                    maps[mapNo].addCharacter(hero);
                    midiEngine.play(maps[mapNo].getBgmName());
                }
            }
        }
    }
    
    private void heroAlive() {
        Vector<Character> characters = maps[mapNo].getCharacters();
        for (int i = 0; i < maps[mapNo].getCharacters().size(); i++) {
            if (characters.get(i) == hero) {
                return;
            }
        }
        if (!isGameOver) {
            // The game over image is loaded from memory
            try {
                gameOver = ImageIO.read(getClass().getResource("image/gameover.png"));
            } catch (IOException ex) {
                Logger.getLogger(MainPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            leftKey = new ActionKey(ActionKey.DEAD_INPUT);
            rightKey = new ActionKey(ActionKey.DEAD_INPUT);
            upKey = new ActionKey(ActionKey.DEAD_INPUT);
            downKey = new ActionKey(ActionKey.DEAD_INPUT);
            tabKey = new ActionKey(ActionKey.DEAD_INPUT);
            enterKey = new ActionKey(ActionKey.DEAD_INPUT);
            inventoryKey = new ActionKey(ActionKey.DEAD_INPUT);
            questKey = new ActionKey(ActionKey.DEAD_INPUT);
            attackKey = new ActionKey(ActionKey.DEAD_INPUT);
            isGameOver = true;
            timer.start(); 
        }
    }

    private void characterMove() {
        // get characters in the current map
        Vector<Character> characters = maps[mapNo].getCharacters();
        // move each character
        for (int i = 0; i < characters.size(); i++) {
            Character c = characters.get(i);
            if (c.getMoveType() == 1) {
                if (c.isMoving()) {
                    c.move();
                } else if (rand.nextDouble() < Character.PROB_MOVE) {
                    c.setDirection(rand.nextInt(4));
                    c.setMoving(true);
                }
            }
        }
    }
    
    private void characterAttack() {
        // get characters in the current map
        Vector<Character> characters = maps[mapNo].getCharacters();
        for (int i = 0; i < characters.size(); i++) {
            Character c = characters.get(i);
            if (c.getAttackType() == 1) {
                if (rand.nextDouble() < Character.PROB_ATTACK) {
                    // An attack is created on the tile the hero is on.
                    Attack attack = new Attack(c.getX(), c.getY(), c.getDirection(), c.getWeapon(), c, maps[mapNo]);
                    // The attack is added to the map.
                    maps[mapNo].addAttack(attack);
                }
            } else if (c.getAttackType() == 2) {
                if ((c.getAttackCount() % c.ATTACK_INTERVAL) == 0) {
                    // An attack is created on the tile the hero is on.
                    Attack attack = new Attack(c.getX(), c.getY(), c.getDirection(), c.getWeapon(), c, maps[mapNo]);
                    // The attack is added to the map.
                    maps[mapNo].addAttack(attack);
                }
                c.increaseAttackCount();
            }
        }
    }

    //ActionKeys are bound to keyboard keys. When pressed it notifies the system.
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode(); // The key on the keyboard pressed is obtained.

        // If the key equals this value, it indicates that the action below should occur (press left key in this case).
        if (keyCode == KeyEvent.VK_A) {
            leftKey.press();
        }
        if (keyCode == KeyEvent.VK_D) {
            rightKey.press();
        }
        if (keyCode == KeyEvent.VK_W) {
            upKey.press();
        }
        if (keyCode == KeyEvent.VK_S) {
            downKey.press();
        }
        if (keyCode == KeyEvent.VK_ENTER) {
            enterKey.press();
        }
        if (keyCode == KeyEvent.VK_I) {
            inventoryKey.press();
        }
        if (keyCode == KeyEvent.VK_Q) {
            questKey.press();
        }
        if (keyCode == KeyEvent.VK_SPACE) {
            attackKey.press();
        }
        if (keyCode == KeyEvent.VK_P) {
            tabKey.press();
        }
    }

    //ActionKeys are bound to keyboard keys. When released it notifies the system.
    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode(); // The key on the keyboard released is obtained.

        // If the key equals this value, it indicates that the action below should occur (release left key in this case).
        if (keyCode == KeyEvent.VK_A) {
            leftKey.release();
        }
        if (keyCode == KeyEvent.VK_D) {
            rightKey.release();
        }
        if (keyCode == KeyEvent.VK_W) {
            upKey.release();
        }
        if (keyCode == KeyEvent.VK_S) {
            downKey.release();
        }
        if (keyCode == KeyEvent.VK_ENTER) {
            enterKey.release();
        }
        if (keyCode == KeyEvent.VK_I) {
            inventoryKey.release();
        }
        if (keyCode == KeyEvent.VK_Q) {
            questKey.release();
        }
        if (keyCode == KeyEvent.VK_SPACE) {
            attackKey.release();
        }
        if (keyCode == KeyEvent.VK_P) {
            tabKey.release();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    private void loadSound() {
        // load midi files
        for (String bgmName : bgmNames) {
            midiEngine.load(bgmName, "bgm/" + bgmName + ".mid");
        }

        // load sound clip files
        for (String soundName : soundNames) {
            waveEngine.load(soundName, "sound/" + soundName + ".wav");
        }
    }

    // Timer for the game over animation. 
    // When it is enabled, the tranparency value (alpha) increases to create a fade in effect.
    @Override
    public void actionPerformed(ActionEvent ae) {
        alpha += 0.01f;
        if (alpha >= 0.90) {
            timer.stop();
        }
    }
}
