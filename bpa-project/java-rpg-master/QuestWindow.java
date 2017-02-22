
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.ImageIcon;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * Created by Ahmad El-baba on 1/3/2017.
 */
public class QuestWindow implements Common,Serializable {

    private static final Logger LOGGER = Logger.getLogger(MainPanel.class.getName());

    MessageEngine messageEngine;
    QuestEngine questEngine;

    List<Quest> questList = new ArrayList();

    // width of white border
    private static final int EDGE_WIDTH = 2;
    private boolean isVisible = false;

    private int questBoardSpot = 0;
    private boolean isMoving;

    private final int QUEST_SLOT_SPACING = 40;

    private static Thread threadAnimation;

    
    
    // outer frame
    private Rectangle boarder;
    // inner frame
    private Rectangle innerRect;

    //CCurson
    private Rectangle cursor;
    
    private WaveEngine waveEngine;
    private static final String[] soundNames = {"beep", "boop"};

    public QuestWindow(Rectangle rect) {
        messageEngine = new MessageEngine();
        questEngine = new QuestEngine();
        waveEngine = new WaveEngine();
        
        cursor = new Rectangle(70, 138, 214, 28);
        this.boarder = rect;
        innerRect = new Rectangle(
                rect.x + EDGE_WIDTH,
                rect.y + EDGE_WIDTH,
                rect.width - EDGE_WIDTH * 2,
                rect.height - EDGE_WIDTH * 2);

        loadSound();
        
        // run thread
        threadAnimation = new Thread(new QuestWindow.AnimationThread());
        threadAnimation.start();
    }

    public void draw(Graphics g) {
        if (isVisible == false) {
            return;
        }
        g.setColor(Color.WHITE);
        g.fillRect(boarder.x, boarder.y, boarder.width, boarder.height);

        g.setColor(Color.BLACK);
        g.fillRect(innerRect.x, innerRect.y, innerRect.width, innerRect.height);
        messageEngine.setColor(messageEngine.WHITE);
        g.setColor(Color.WHITE);
        messageEngine.drawMessage(125, 96, "QUESTS", g);
        messageEngine.drawMessage(380, 96, "DESCRIPTION", g);
        // the line dividng quests and discriptions
        g.fillRect(288, 98, 2, 350);

        g.drawRect(cursor.x, cursor.y + QUEST_SLOT_SPACING * questBoardSpot, cursor.width, cursor.height);

        // drawing quest titles
        if (!questList.isEmpty()) {
            for (int i = 0; i < questList.size(); i++) {
                try {
                    for (int c = 0; c < 13; c++) {
                        if (questList.get(i).questFinished) {

                            messageEngine.setColor(messageEngine.GREEN);
                            messageEngine.drawMessage(c * 15 + 70, 140 + i * 40, String.valueOf(questList.get(i).getQuestName().charAt(c)), g);
                        } else {
                            messageEngine.setColor(messageEngine.WHITE);
                            messageEngine.drawMessage(c * 15 + 70, 140 + i * 40, String.valueOf(questList.get(i).getQuestName().charAt(c)), g);
                        }

                        if (cursor.contains(70, 140 + i * 40)) {

                            g.setColor(Color.white);
                            messageEngine.setColor(messageEngine.WHITE);
                            messageEngine.drawMessage(300, 140, questList.get(questBoardSpot).getQuestDisctription(), g);
                            messageEngine.drawMessage(300, 410, "REWARD " + String.valueOf(questList.get(questBoardSpot).getReward()), g);
                        }
                    }
                } catch (Exception e) {

                }
            }
        }
    }

    public void runThread() {
        // run thread
        threadAnimation = new Thread(new QuestWindow.AnimationThread());
        threadAnimation.start();
    }

    public void addQuest(QuestEvent questEvent) {
        questList.add(new Quest(questEvent.getQuestType(), questEvent.getQuestName(), questEvent.getQuestDisctription(), questEvent.getExp(), questEvent.getReward()));
        questEngine.chooseType(questList.get(questList.size() - 1), questEvent);
    }

    public List<Quest> getQuests() {
        return questList;
    }

    public void sendQuestList(List<Quest> quests) {
        this.questList = quests;
    }

    public void show() {
        isVisible = true;
    }

    public void setDirection(int direction) {

        switch (direction) {

            case DOWN:
                if (!isMoving) {
                    isMoving = true;
                    if (canMoveDown()) {
                        waveEngine.play("beep");
                        questBoardSpot++;
                    } else {
                        questBoardSpot = 0;
                        waveEngine.play("boop");
                    }
                }
                isMoving = false;
                break;
            case UP:
                if (!isMoving) {
                    isMoving = true;
                    if (canMoveUp()) {
                        waveEngine.play("beep");
                        questBoardSpot--;
                    } else if (questList.size() == 0) {
                        questBoardSpot = questList.size();
                    } else {
                        questBoardSpot = questList.size() - 1;
                    }   waveEngine.play("boop");
                }
                isMoving = false;
                break;
        }
    }

    public void hide() {
        isVisible = false;
    }

    /**
     * Checks if the cursor can move down by checking if the position is below
     * the column count.
     *
     * @return whether or not the cursor can move down.
     */
    private boolean canMoveDown() {
        return (questBoardSpot < questList.size() - 1);
    }

    /**
     * Checks if the cursor can move down by checking if the position is below
     * the column count.
     *
     * @return whether or not the cursor can move down.
     */
    private boolean canMoveUp() {
        return (questBoardSpot > 0);
    }

    public boolean isMoving() {
        return isMoving;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setMoving(boolean flag) {
        isMoving = flag;
    }

    public int getQuestBoardPos() {
        return questBoardSpot;
    }

    private void loadSound() {
        
        // load sound clip files
        for (String soundName : soundNames) {
            waveEngine.load(soundName, "sound/" + soundName + ".wav");
        }
    }
    
    private class AnimationThread extends Thread {

        public void run() {
            while (true) {
                isMoving = false;
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
