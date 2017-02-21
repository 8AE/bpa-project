
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javax.swing.Timer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

class MainPanel extends JPanel implements KeyListener, Runnable, Common, ActionListener, Serializable {

    private static final Logger LOGGER = Logger.getLogger(MainPanel.class.getName());

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

    // is this the main menu?
    private boolean isMainMenu = false;
    // which selection of the main menu the cursor is on
    private int mainMenuSelection = 0;

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
    private ActionKey escKey;

    // thread that loops the painting of images on screen
    private static Thread gameLoop;

    // random number initialized
    private static Random rand = new Random();

    /* START: declaration of window variables.
       The variables are followed by a rectangle with the dimensions of the base of the window */
    // The window for the text box.
    private MessageWindow messageWindow;
    private static Rectangle WND_RECT = new Rectangle(142, 480, 356, 140);

    // The window for the inventory.
    private InventoryWindow inventoryWindow;
    // The window for the quest list.
    private QuestWindow questWindow;
    // The window for the Shop list.
    private ShopWindow shopWindow;
    // The basic window Rectangle
    private static Rectangle MENU_RECT = new Rectangle(64, 96, 512, 352);

    // The window for the pause menu
    private PauseWindow pauseWindow;
    private static Rectangle PAUSE_RECT = new Rectangle(290, 200, 110, 155);
    
    //Draws the popup
    private NotificationPopup popup;
    private static Rectangle POP_RECT = new Rectangle(300, 96, 250, 45);

    // The window for the Heads Up Display at the top of the screen.
    // This is refered to as the HUD throughout the code.
    private HudWindow hudWindow;

    // The engines for the sounds are declared and initialized.
    private WaveEngine bgm = new WaveEngine();
    private WaveEngine waveEngine = new WaveEngine();

    // BGM
    // from TAM Music Factory http://www.tam-music.com/
    private static final String[] bgmNames = {"castle", "field"};
    // Sound Clip
    private static final String[] soundNames = {"treasure", "door", "step", "beep", "boop"};

    // Double buffering for the graphics displayed on screen to eliminate flickering.
    private Graphics dbg; // dbg = double buffer graphic
    private Image dbImage = null; // dbImage = double buffer Image

    // Timer for the game over animation
    private static Timer gameoverTimer;
    // Keeps track of the alpha value of the fade in of the game over animation.
    private float alpha = 1f;
    // The game over image.
    private static BufferedImage gameOver;
    // Whether there is a game over or not.
    private boolean isGameOver = false;
    // which selection of the main menu the cursor is on
    private int gameOverSelection = 0;

    // The images for the main menu
    private static BufferedImage credits;
    private static BufferedImage mainMenuScreen;
    private boolean isStartUp = true;
    private boolean isStart = false;

    public MainPanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        setFocusable(true);
        addKeyListener(this);

        // Create action keys.
        leftKey = new ActionKey(ActionKey.SLOWER_INPUT);
        rightKey = new ActionKey(ActionKey.SLOWER_INPUT);
        upKey = new ActionKey(ActionKey.SLOWER_INPUT);
        downKey = new ActionKey(ActionKey.SLOWER_INPUT);
        tabKey = new ActionKey(ActionKey.DETECT_INITIAL_PRESS_ONLY);
        enterKey = new ActionKey(ActionKey.DETECT_INITIAL_PRESS_ONLY);
        inventoryKey = new ActionKey(ActionKey.DETECT_INITIAL_PRESS_ONLY);
        questKey = new ActionKey(ActionKey.DETECT_INITIAL_PRESS_ONLY);
        attackKey = new ActionKey(ActionKey.SLOWER_INPUT);
        escKey = new ActionKey(ActionKey.DETECT_INITIAL_PRESS_ONLY);

        // Create maps.
        maps = new Map[6];
        maps[0] = new Map("map/castle.map", "event/castle.evt", "theme", this);
        maps[1] = new Map("map/field.map", "event/field.evt", "field", this);
        maps[2] = new Map("map/map.map", "event/map.evt", "field", this);
        maps[3] = new Map("map/level2.map", "event/level2.evt", "field", this);
        maps[4] = new Map("map/level1_1.map", "event/level1_1.evt", "field", this);
        maps[5] = new Map("map/level1_2.map", "event/level1_2.evt", "field", this);
        mapNo = 4;  // initial map

        // Create the main character, our hero.
        // This is also the start point of the game.
        hero = new Character(15, 21, 0, DOWN, 0, 1, 0, maps[mapNo]);
        hero.setIsHero(true);

        // Add characters to the map.
        maps[mapNo].addCharacter(hero);

        // Create Shop Window
        shopWindow = new ShopWindow(MENU_RECT);
        
        // Create message window.
        messageWindow = new MessageWindow(WND_RECT);
        
        // Create inventory window.
        inventoryWindow = new InventoryWindow(MENU_RECT);
        
        // Create quest window.
        questWindow = new QuestWindow(MENU_RECT);
        
        // Create Shop Window
        pauseWindow = new PauseWindow(PAUSE_RECT);
        
        // Create HUD.
        hudWindow = new HudWindow();
        
        // Create the popup window.
        popup = new NotificationPopup(POP_RECT);

        // Load Backgound Music (BGM) and sound clips.
        loadSound();
        // The background music of the main menu plays.
        bgm.play("main");
        bgm.isLoop(true);


        // Start game loop.
        gameLoop = new Thread(this);
        gameLoop.start();

        gameoverTimer = new Timer(20, this);
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
                LOGGER.log(Level.SEVERE, e.toString(), e);

                try {
                    CrashReport cr = new CrashReport(e);
                    cr.show();
                } catch (Exception n) {
                    // do nothing
                }
            }

            beforeTime = System.currentTimeMillis();
        }
    }

    private void checkInput() {
        // Do not check the input when the game is starting up.
        if (!isStart) {

            /* Checks if any of the windows are visable.
           If they are not visable, the main window is useable.
           If any are visable, their input is checked.
           They are organized in a hierarchy of priority with the message window on top. */
            if (messageWindow.isVisible()) {
                messageWindowCheckInput();
            } else if (isMainMenu) {
                mainMenuCheckInput();
            } else if (isGameOver) {
                gameOverCheckInput();
            } else if (inventoryWindow.isVisible()) {
                inventoryWindowCheckInput();
            } else if (shopWindow.isVisible()) {
                shopWindowCheckInput();
            } else if (pauseWindow.isVisible()) {
                pauseWindowCheckInput();
            } else if (questWindow.isVisible()) {
                questWindowCheckInput();
            } else {
                mainWindowCheckInput();
            }
        }
    }

    private void gameUpdate() {
        // If only the main panel is visable, then the activities that reside within it function.
        if (!messageWindow.isVisible() && !inventoryWindow.isVisible() && !questWindow.isVisible() && !isMainMenu && !isStartUp && !isStart && !pauseWindow.isVisible()) {
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

        if (isStartUp) {
            mainMenuStartUp();
            isStartUp = false;
            isStart = true;
            return;
        }

        if (isStart) {
            
            try {
                credits = ImageIO.read(getClass().getResource("image/credits.png"));
            } catch (IOException | IllegalArgumentException e) {
                LOGGER.log(Level.SEVERE, e.toString(), e);

                try {
                    CrashReport cr = new CrashReport(e);
                    cr.show();
                } catch (Exception n) {
                    // do nothing
                }
            }

            Graphics2D g2d = (Graphics2D) dbg;
            if (alpha >= .01) {

                alpha += -0.01f;
                try {
                    Thread.sleep(20); // Wait 1 seconds.
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                dbg.drawImage(credits, 0, 0, this);
                return;
            }

            alpha = 1f;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            alpha = 0f;
            isStart = false;
            isMainMenu = true;
            return;
        }

        if (isMainMenu) {

            try {
                mainMenuScreen = ImageIO.read(getClass().getResource("image/mainmenu.png"));
            } catch (IOException | IllegalArgumentException e) {
                LOGGER.log(Level.SEVERE, e.toString(), e);

                try {
                    CrashReport cr = new CrashReport(e);
                    cr.show();
                } catch (Exception n) {
                    // do nothing
                }
            }

            dbg.drawImage(mainMenuScreen, 0, 0, this);
            Font font = new Font("SansSerif", Font.BOLD, 16);
            dbg.setFont(font);
            dbg.setColor(Color.YELLOW);
            dbg.drawRect(120, 340 + 75 * mainMenuSelection, 400, 45);
        } else {
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

            //Draws Shop Winodw
            shopWindow.draw(dbg);
            
            // Draw popup message window.
            popup.draw(dbg);

            // Draw inventory window
            inventoryWindow.draw(dbg);

            // Draw quest window.
            questWindow.draw(dbg);

            // Draw pause Menu
            pauseWindow.draw(dbg);
            
            if (isGameOver) {
                try {
                    gameOver = ImageIO.read(getClass().getResource("image/gameover.png"));
                } catch (IOException | IllegalArgumentException e) {
                    LOGGER.log(Level.SEVERE, e.toString(), e);

                    try {
                        CrashReport cr = new CrashReport(e);
                        cr.show();
                    } catch (Exception n) {
                        // do nothing
                    }
                }

                dbg.drawRect(90 + 200 * gameOverSelection, 485, 100, 20);
                dbg.drawString("Main Menu", 100, 500);
                dbg.drawString("Last Save", 300, 500);

                Graphics2D g2d = (Graphics2D) dbg;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                dbg.drawImage(gameOver, 64, 200, null);

                maps[mapNo].setGreyScale(true);
            }
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
        
        // Draw message window last so it always pop ups on top.
        messageWindow.draw(dbg);
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

    private void mainMenuStartUp() {
        try {
            credits = ImageIO.read(getClass().getResource("image/credits.png"));
            //mainMenuScreen = ImageIO.read(getClass().getResource("image/mainMenu.png"));
        } catch (IOException | IllegalArgumentException e) {
            //LOGGER.log(Level.SEVERE, e.toString(), e);

            try {
                //CrashReport cr = new CrashReport(e);
                //cr.show();
            } catch (Exception n) {
                // do nothing
            }
        }

        dbg.drawImage(credits, 0, 0, this);

    }

    private void gameOverCheckInput() {
        if (leftKey.isPressed()) {
            if (gameOverSelection > 0) {
                gameOverSelection--;
            }
        }

        if (rightKey.isPressed()) {
            if (gameOverSelection < 1) {
                gameOverSelection++;
            }
        }

        if (enterKey.isPressed()) {
            //stop the fade-in animation of game over and continue with the selection.
            gameoverTimer.stop();
            alpha = 1f;
            Graphics2D g2d = (Graphics2D) dbg;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            if (gameOverSelection == 0) {
                isMainMenu = true;
                isGameOver = false;

                bgm.play("main");
bgm.isLoop(true);
                leftKey = new ActionKey();
                rightKey = new ActionKey();
                upKey = new ActionKey(ActionKey.SLOWER_INPUT);
                downKey = new ActionKey(ActionKey.SLOWER_INPUT);
            } else if (gameOverSelection == 1) {
                try {
                    loadGame();

                // The background music of the loaded map plays.
                bgm.play(maps[mapNo].getBgmName());
                bgm.isLoop(true);

                isGameOver = false;

                leftKey = new ActionKey();
                rightKey = new ActionKey();
                upKey = new ActionKey();
                downKey = new ActionKey();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, e.toString(), e);
                    messageWindow.setMessage("THERE IS NO/LOAD DATA");
                    messageWindow.show();
                }
            }
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
        
        //The pause menu opens
        if (escKey.isPressed()) {
            enterKey = new ActionKey(ActionKey.DETECT_INITIAL_PRESS_ONLY);
            leftKey = new ActionKey(ActionKey.SLOWER_INPUT);
            rightKey = new ActionKey(ActionKey.SLOWER_INPUT);
            upKey = new ActionKey(ActionKey.SLOWER_INPUT);
            downKey = new ActionKey(ActionKey.SLOWER_INPUT);
            
            pauseWindow.show();
            
        }
        
        // An action is performed if there are object events in front of or under the hero, depending on the case.
        if (enterKey.isPressed()) {

            ShopEvent shopEvent = hero.shopSearch();
            if (shopEvent != null) {
                // waveEngine.play("treasure");
                System.out.print("Hello");
                leftKey = new ActionKey(ActionKey.SLOWER_INPUT);
                rightKey = new ActionKey(ActionKey.SLOWER_INPUT);
                upKey = new ActionKey(ActionKey.SLOWER_INPUT);
                downKey = new ActionKey(ActionKey.SLOWER_INPUT);
                shopWindow.show();
                
                return;
            }

            QuestEvent questEvent = hero.questSearch();
            if (questEvent != null) {
                // waveEngine.play("treasure");
                messageWindow.setMessage("HERO FOUND/" + questEvent.getQuestName());
                messageWindow.show();

                questWindow.addQuest(questEvent);
                maps[mapNo].removeEvent(questEvent);
                return;
            }

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
                    if (treasure.getItemType().equals("WEAPON")) {
                        inventoryWindow.add(treasure.toWeapon()); // Item is added based on positioning on item chip image.
                    } else {
                        inventoryWindow.add(treasure.toItem()); // Item is added based on positioning on item chip image.
                        
                    }
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
            if (hudWindow.getSelectedItem() != null) {
                // An attack is created on the tile the hero is on.
                Attack attack = new Attack(hero.getX(), hero.getY(), hero.getDirection(), hero.getWeapon(), hero, maps[mapNo]);
                // The attack is added to the map.
                maps[mapNo].addAttack(attack);
            }
        }
    }

    private void checkTrigger() {

        TriggerEvent trigger = hero.touch();
        try {

            for (int t = 0; t <= trigger.gettLocation().size(); t++) {

                for (int i = 0; i <= questWindow.getQuests().size(); i++) {
                    if (trigger.getPoint(t).equals(questWindow.getQuests().get(i).getDXY())) {
                        popup.setMessage("QUEST COMPLETE");
                        popup.show();
                        questWindow.getQuests().get(i).setQuestFinished(true);
                        maps[mapNo].removeEvent(trigger);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINER, e.toString(), e);
        }
    }

    private void messageWindowCheckInput() {
        // Moves to the next page or exits the text box if there is no text left.
        if (enterKey.isPressed()) {
            if (messageWindow.nextPage()) {
                waveEngine.play("boop");
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
        if (inventoryKey.isPressed() || escKey.isPressed()) {
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
            if (inventoryWindow.getItem() == null) {
                hudWindow.selectItem(inventoryWindow.getItem());
                return;
            }
            
            if (inventoryWindow.getItem() instanceof Weapon) {
                hudWindow.selectItem(inventoryWindow.getItem());
                hero.setWeapon((Weapon)hudWindow.getSelectedItem());
            } else {
                messageWindow.setMessage("YOU CANT EQUIPT THAT");
                messageWindow.show();
            }
        }
    }

    private void questWindowCheckInput() {
        // When the quest key is pressed again, the quest window is closed.
        if (questKey.isPressed() || escKey.isPressed()) {
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
    
    private void shopWindowCheckInput() {
        // When the escape key is pressed again, the shop window is closed.
        if (escKey.isPressed()) {
            // set movement keys back to constant input
            leftKey = new ActionKey();
            rightKey = new ActionKey();
            upKey = new ActionKey();
            downKey = new ActionKey();
            shopWindow.hide();
            
        }
        // The cursor moves left and right to see the items
        if (leftKey.isPressed()) {
            shopWindow.setDirection(LEFT);
        }
        if (rightKey.isPressed()) {
            shopWindow.setDirection(RIGHT);
        }
        
    }
    
    private void pauseWindowCheckInput() {
        // When the escape key is pressed again, the shop window is closed.
        if (escKey.isPressed()) {
            // set movement keys back to constant input
            enterKey = new ActionKey(ActionKey.DETECT_INITIAL_PRESS_ONLY);
            leftKey = new ActionKey();
            rightKey = new ActionKey();
            upKey = new ActionKey();
            downKey = new ActionKey();
            
            pauseWindow.hide();
            
        }
        // The cursor moves left and right to see the items
        if (upKey.isPressed()) {
            pauseWindow.setDirection(UP);
        }
        if (downKey.isPressed()) {
            pauseWindow.setDirection(DOWN);
        }
        if (enterKey.isPressed()) {
            switch (pauseWindow.getPauseBoardPos()) {
                case 0:
                    enterKey = new ActionKey(ActionKey.DETECT_INITIAL_PRESS_ONLY);
                    leftKey = new ActionKey();
                    rightKey = new ActionKey();
                    upKey = new ActionKey();
                    downKey = new ActionKey();
                    
                    pauseWindow.hide();
                    break;
                case 1:
                    saveGame();
                    enterKey = new ActionKey(ActionKey.DETECT_INITIAL_PRESS_ONLY);
                    leftKey = new ActionKey();
                    rightKey = new ActionKey();
                    upKey = new ActionKey();
                    downKey = new ActionKey();
                    
                    pauseWindow.hide();
                    break;
                case 2:
                    
                    System.exit(0);
                    break;
            }
        }
        
    }

    private void mainMenuCheckInput() {
        // The cursor moves up and down the list of quests to see their description, reward, and requirements.
        if (upKey.isPressed()) {
            if (mainMenuSelection <= 0) {
                mainMenuSelection = 0;
            } else {
                waveEngine.play("boop");
                mainMenuSelection--;
            }
        }
        if (downKey.isPressed()) {
            if (mainMenuSelection >= 2) {
                mainMenuSelection = 2;
            } else {
                waveEngine.play("boop");
                mainMenuSelection++;
            }
        }
        if (enterKey.isPressed()) {
            if (mainMenuSelection == 0) {
                waveEngine.play("beep");
                isMainMenu = false;

                newGame();

                // The background music of the initial map plays.
                bgm.play(maps[mapNo].getBgmName());
bgm.isLoop(true);
                leftKey = new ActionKey();
                rightKey = new ActionKey();
                upKey = new ActionKey();
                downKey = new ActionKey();
            } else if (mainMenuSelection == 1) {
                waveEngine.play("beep");

                try {
                    loadGame();

                    // The background music of the loaded map plays.
                    bgm.play(maps[mapNo].getBgmName());
bgm.isLoop(true);
                    
                    isMainMenu = false;
                    leftKey = new ActionKey();
                    rightKey = new ActionKey();
                    upKey = new ActionKey();
                    downKey = new ActionKey();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, e.toString(), e);
                    messageWindow.setMessage("THERE IS NO/LOAD DATA");
                    messageWindow.show();
                }
            } else if (mainMenuSelection == 2) {
                waveEngine.play("beep");
                System.exit(0);
            }
        }
    }

    private void heroMove() {
        if (hero.isMoving()) {
            if (hero.move()) {
                Event event = maps[mapNo].checkEvent(hero.getX(), hero.getY());
                if (event instanceof MoveEvent) {
                    waveEngine.play("step");
                    // Move to new map.
                    MoveEvent m = (MoveEvent) event;
                    maps[mapNo].removeCharacter(hero);
                    mapNo = m.destMapNo;
                    hero = new Character(m.destX, m.destY, 0, DOWN, 0, 1, 0, maps[mapNo]);
                    hero.setIsHero(true);
                    maps[mapNo].addCharacter(hero);
                    bgm.play(maps[mapNo].getBgmName());
                    bgm.isLoop(true);
                    maps[mapNo].runThread();
                }

                if (event instanceof TriggerEvent) {
                    checkTrigger();
                }

                if (event instanceof SaveEvent) {
                    saveGame();
                }
            }
        }
    }

    private void heroAlive() {
        Vector<Character> characters = maps[mapNo].getCharacters();
        for (int i = 0; i < maps[mapNo].getCharacters().size(); i++) {
            if (characters.get(i).getIsHero()) {
                return;
            }
        }
        if (!isMainMenu) {
            if (!isGameOver) {
                // The game over image is loaded from memory
                try {
                    gameOver = ImageIO.read(getClass().getResource("image/gameover.png"));
                } catch (IOException | IllegalArgumentException e) {
                    LOGGER.log(Level.SEVERE, e.toString(), e);

                    try {
                        CrashReport cr = new CrashReport(e);
                        cr.show();
                    } catch (Exception n) {
                        // do nothing
                    }
                }

                leftKey = new ActionKey(ActionKey.SLOWER_INPUT);
                rightKey = new ActionKey(ActionKey.SLOWER_INPUT);

                isGameOver = true;
                gameoverTimer.start();
            }
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
    
    public void CheckifQuestCharacter(Character c) {
        try {
            for (int i = 0; i < questWindow.getQuests().size(); i++) {
                if (c.getId() == questWindow.getQuests().get(i).getTarget()) {
                    popup.setMessage("QUEST COMPLETE");
                    popup.show();
                    questWindow.getQuests().get(i).setQuestFinished(true);
                    
                }
            }
        } catch (Exception e) {
            
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

    private void saveGame() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("savegame.txt"));
            out.writeObject(hero);
            out.writeObject(maps);
            out.writeInt(mapNo);
            out.writeObject(questWindow);
            out.writeObject(inventoryWindow);
            out.writeObject(hudWindow);
            out.flush();
            out.close();

        } catch (IOException | IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);

            try {
                CrashReport cr = new CrashReport(e);
                cr.show();
            } catch (Exception n) {
                // do nothing
            }
        }
    }

    private void loadGame() throws IOException, ClassNotFoundException {
        // remove any greyscale if there is any.
        maps[mapNo].setGreyScale(false);
        // reset the alpha to 0
        alpha = 0f;

        ObjectInputStream in = new ObjectInputStream(new FileInputStream("savegame.txt"));

        hero = (Character) in.readObject();

        maps = (Map[]) in.readObject();
        mapNo = (Integer) in.readInt();
        for (Map map : maps) {
            map.runThread();
            map.runAttackThread();
            map.runCharacterThread();
            map.setPanel(this);
        }

        questWindow = ((QuestWindow) in.readObject());
        inventoryWindow = ((InventoryWindow) in.readObject());
        hudWindow = ((HudWindow) in.readObject());

        questWindow.runThread();
        inventoryWindow.runThread();

        in.close();

        gameoverTimer = new Timer(20, this);
    }

    private void newGame() {
        // remove any greyscale if there is any.
        maps[mapNo].setGreyScale(false);

        alpha = 0f;

        // Create action keys.
        leftKey = new ActionKey(ActionKey.SLOWER_INPUT);
        rightKey = new ActionKey(ActionKey.SLOWER_INPUT);
        upKey = new ActionKey(ActionKey.SLOWER_INPUT);
        downKey = new ActionKey(ActionKey.SLOWER_INPUT);
        tabKey = new ActionKey(ActionKey.DETECT_INITIAL_PRESS_ONLY);
        enterKey = new ActionKey(ActionKey.DETECT_INITIAL_PRESS_ONLY);
        inventoryKey = new ActionKey(ActionKey.DETECT_INITIAL_PRESS_ONLY);
        questKey = new ActionKey(ActionKey.DETECT_INITIAL_PRESS_ONLY);
        attackKey = new ActionKey(ActionKey.SLOWER_INPUT);

        // Create maps.
         maps = new Map[6];
        maps[0] = new Map("map/castle.map", "event/castle.evt", "theme", this);
        maps[1] = new Map("map/field.map", "event/field.evt", "field", this);
        maps[2] = new Map("map/map.map", "event/map.evt", "field", this);
        maps[3] = new Map("map/level2.map", "event/level2.evt", "field", this);
        maps[4] = new Map("map/level1_1.map", "event/level1_1.evt", "field", this);
        maps[5] = new Map("map/level1_2.map", "event/level1_2.evt", "field", this);
        mapNo = 4;  // initial map

        // Create the main character, our hero.
        // This is also the start point of the game.
        hero = new Character(15, 21, 0, DOWN, 0, 1, 0, maps[mapNo]);
        hero.setIsHero(true);


        // Add characters to the map.
        maps[mapNo].addCharacter(hero);

        // Create message window.
        messageWindow = new MessageWindow(WND_RECT);

        // Create inventory window.
        inventoryWindow = new InventoryWindow(MENU_RECT);

        // Create quest window.
        questWindow = new QuestWindow(MENU_RECT);

        // Create HUD.
        hudWindow = new HudWindow();

        // Create the popup window.
        popup = new NotificationPopup(POP_RECT);

        gameoverTimer = new Timer(20, this);
    }

    //ActionKeys are bound to keyboard keys. When pressed it notifies the system.
    @Override
    public void keyPressed(KeyEvent e) {
        // Do not allow key input when the game is starting.
        if (!isStart) {
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
            if (keyCode == KeyEvent.VK_ESCAPE) {
                escKey.press();
            }
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
        if (keyCode == KeyEvent.VK_ESCAPE) {
            escKey.release();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    private void loadSound() {
      
 // load bgm files
        for (String bgmName : bgmNames) {
            bgm.load(bgmName, "bgm/" + bgmName + ".wav");
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
            gameoverTimer.stop();
        }
    }
}
