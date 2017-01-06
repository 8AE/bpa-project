import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;
import java.util.Vector;

import javax.swing.JPanel;

class MainPanel extends JPanel implements KeyListener, Runnable, Common {
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

    private Thread gameLoop;
    private Random rand = new Random();

    private MessageWindow messageWindow;
    private static Rectangle WND_RECT = new Rectangle(142, 480, 356, 140);

    private InventoryWindow inventoryWindow;
    private static Rectangle INV_RECT = new Rectangle(64, 96, 512, 352);
    
    private QuestWindow questWindow;
    private static Rectangle QUE_RECT = new Rectangle(64, 96, 512, 352);
    
    private HudWindow hudWindow;

    private MidiEngine midiEngine = new MidiEngine();
    private WaveEngine waveEngine = new WaveEngine();

    // BGM
    // from TAM Music Factory http://www.tam-music.com/
    private static final String[] bgmNames = {"castle", "field"};
    // Sound Clip
    private static final String[] soundNames = {"treasure", "door", "step"};

    // double buffering
    private Graphics dbg;
    private Image dbImage = null;

    public MainPanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        setFocusable(true);
        addKeyListener(this);

        // create action keys
        leftKey = new ActionKey();
        rightKey = new ActionKey();
        upKey = new ActionKey();
        downKey = new ActionKey();
        tabKey = new ActionKey(ActionKey.DETECT_INITIAL_PRESS_ONLY);
        enterKey = new ActionKey(ActionKey.DETECT_INITIAL_PRESS_ONLY);
        inventoryKey = new ActionKey(ActionKey.DETECT_INITIAL_PRESS_ONLY);
        questKey = new ActionKey(ActionKey.DETECT_INITIAL_PRESS_ONLY);
        attackKey = new ActionKey(ActionKey.DETECT_INITIAL_PRESS_ONLY);

        // create map
        maps = new Map[3];
        maps[0] = new Map("map/castle.map", "event/castle.evt", "castle", this);
        maps[1] = new Map("map/field.map", "event/field.evt", "field", this);
        maps[2] = new Map("map/map.map", "event/map.evt", "field", this);
        mapNo = 0;  // initial map

        // create character
        hero = new Character(6, 6, 0, DOWN, 0, maps[mapNo]);

        // add characters to the map
        maps[mapNo].addCharacter(hero);

        // create message window
        messageWindow = new MessageWindow(WND_RECT);

        // create inventory window
        inventoryWindow = new InventoryWindow(INV_RECT);

        // create quest window
        questWindow = new QuestWindow(QUE_RECT);

        // create hud window
        hudWindow = new HudWindow();
        
        
        // load BGM and sound clips
        loadSound();

        midiEngine.play(maps[mapNo].getBgmName());

        // start game loop
        gameLoop = new Thread(this);
        gameLoop.start();
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
        if (!messageWindow.isVisible() && !inventoryWindow.isVisible() && !questWindow.isVisible()) {
            heroMove();
            characterMove();
        }
    }

    private void gameRender() {
        if (dbImage == null) {
            // buffer image
            dbImage = createImage(WIDTH, HEIGHT);
            if (dbImage == null) {
                return;
            } else {
                // device context of buffer image
                dbg = dbImage.getGraphics();
            }
        }

        dbg.setColor(Color.WHITE);
        dbg.fillRect(0, 0, WIDTH, HEIGHT);

        // calculate offset so that the hero is in the center of a screen.
        int offsetX = hero.getPX() - MainPanel.WIDTH / 2;
        // do not scroll at the edge of the map
        if (offsetX < 0) {
            offsetX = 0;
        } else if (offsetX > maps[mapNo].getWidth() - MainPanel.WIDTH) {
            offsetX = maps[mapNo].getWidth() - MainPanel.WIDTH;
        }

        int offsetY = hero.getPY() - MainPanel.HEIGHT / 2;
        // do not scroll at the edge of the map
        if (offsetY < 0) {
            offsetY = 0;
        } else if (offsetY > maps[mapNo].getHeight() - MainPanel.HEIGHT) {
            offsetY = maps[mapNo].getHeight() - MainPanel.HEIGHT;
        }
        
        // draw map
        maps[mapNo].draw(dbg, offsetX, offsetY);
        
        // draw hud window
        hudWindow.draw(dbg);
        
        // draw message window
        messageWindow.draw(dbg);
        
        inventoryWindow.draw(dbg);
        
        // draw quest window
        questWindow.draw(dbg);
        
        // display debug information
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
        if ((g != null) && (dbImage != null)) {
            g.drawImage(dbImage, 0, 0, null);
        }
        Toolkit.getDefaultToolkit().sync();
        if (g != null) {
            g.dispose();
        }
    }

    private void mainWindowCheckInput() {
        if (leftKey.isPressed()) {
            if (!hero.isMoving()) {
                hero.setDirection(LEFT);
                hero.setMoving(true);
            }
        }

        if (rightKey.isPressed()) {
            if (!hero.isMoving()) {
                hero.setDirection(RIGHT);
                hero.setMoving(true);
            }
        }

        if (upKey.isPressed()) {
            if (!hero.isMoving()) {
                hero.setDirection(UP);
                hero.setMoving(true);
            }
        }

        if (downKey.isPressed()) {
            if (!hero.isMoving()) {
                hero.setDirection(DOWN);
                hero.setMoving(true);
            }
        }

        if (enterKey.isPressed()) {

            // door open event
            DoorEvent door = hero.open();
            if (door != null) {
                waveEngine.play("door");
                maps[mapNo].removeEvent(door);
                return;
            }            

            // cannot open window if hero is moving, but can open doors while moving (see above code)
            if (hero.isMoving()) {
                return;
            }

            // search
            TreasureEvent treasure = hero.search();
            if (treasure != null) {
                waveEngine.play("treasure");
                messageWindow.setMessage("HERO DISCOVERED/" + treasure.getItemName());
                messageWindow.show();
                if (!inventoryWindow.isFull()) {
                    //inventoryWindow.add(treasure.toInt());
                    inventoryWindow.add(12);
                    messageWindow.setMessage("HERO DISCOVERED/" + treasure.getItemName());
                } else {
                    messageWindow.setMessage("HERO DISCOVERED/" + treasure.getItemName() + "|YOUR INVENTORY IS/FULL! YOU NEED TO/MAKE SPACE!");
                    leftKey = new ActionKey(ActionKey.SLOWER_INPUT);
                    rightKey = new ActionKey(ActionKey.SLOWER_INPUT);
                    upKey = new ActionKey(ActionKey.SLOWER_INPUT);
                    downKey = new ActionKey(ActionKey.SLOWER_INPUT);
                    inventoryWindow.show(InventoryWindow.TRASH_ITEM, 11);
                    //TODO: Force player to trash an item.
                }
                maps[mapNo].removeEvent(treasure);
                return;
            }

            // talk
            if (!messageWindow.isVisible()) {
                Character c = hero.talkWith();
                if (c != null) {
                    messageWindow.setMessage(c.getMessage());
                    messageWindow.show();
                } else {
                    messageWindow.setMessage("THERE IS NO ONE/IN THAT DIRECTION");
                    messageWindow.show();
                }
            }
        }

        if (inventoryKey.isPressed()) {
            // set movement keys to only take initial press
            leftKey = new ActionKey(ActionKey.SLOWER_INPUT);
            rightKey = new ActionKey(ActionKey.SLOWER_INPUT);
            upKey = new ActionKey(ActionKey.SLOWER_INPUT);
            downKey = new ActionKey(ActionKey.SLOWER_INPUT);
            inventoryWindow.show();
        }
        if (questKey.isPressed()) {
            // set movement keys to only take initial press
            leftKey = new ActionKey(ActionKey.SLOWER_INPUT);
            rightKey = new ActionKey(ActionKey.SLOWER_INPUT);
            upKey = new ActionKey(ActionKey.SLOWER_INPUT);
            downKey = new ActionKey(ActionKey.SLOWER_INPUT);
            questWindow.show();
        }

        if (attackKey.isPressed()) {
            //TODO: initiate attack animation and shoot projectile
        }
    }

    private void messageWindowCheckInput() {
        if (enterKey.isPressed()) {
            if (messageWindow.nextPage()) {
                messageWindow.hide();
            }
        }
    }

    private void inventoryWindowCheckInput() {
        if (leftKey.isPressed()) {
            inventoryWindow.setDirection(LEFT);
        }

        if (rightKey.isPressed()) {
            inventoryWindow.setDirection(RIGHT);
        }

        if (upKey.isPressed()) {
            inventoryWindow.setDirection(UP);
        }

        if (downKey.isPressed()) {
            inventoryWindow.setDirection(DOWN);
        }

        if (tabKey.isPressed()) {
            inventoryWindow.nextFocus();
        }
        
        if (inventoryKey.isPressed()) {
            // set movement keys back to constant input
            leftKey = new ActionKey();
            rightKey = new ActionKey();
            upKey = new ActionKey();
            downKey = new ActionKey();
            inventoryWindow.hide();
        }
        
        if (questKey.isPressed()) {
            inventoryWindow.hide();
            questWindow.show();
        }
        
        if (enterKey.isPressed()) {
            inventoryWindow.select(inventoryWindow.getInvBoardXPos(), inventoryWindow.getInvBoardYPos());
        }
    }
    
    private void questWindowCheckInput() {
        if (questKey.isPressed()) {
            // set movement keys back to constant input
            leftKey = new ActionKey();
            rightKey = new ActionKey();
            upKey = new ActionKey();
            downKey = new ActionKey();
            questWindow.hide();
        }
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
                    // move to another map
                    MoveEvent m = (MoveEvent)event;
                    maps[mapNo].removeCharacter(hero);
                    mapNo = m.destMapNo;
                    hero = new Character(m.destX, m.destY, 0, DOWN, 0, maps[mapNo]);
                    maps[mapNo].addCharacter(hero);
                    midiEngine.play(maps[mapNo].getBgmName());
                }
            }
        }
    }

    private void characterMove() {
        // get characters in the map
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

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

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
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();

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
}