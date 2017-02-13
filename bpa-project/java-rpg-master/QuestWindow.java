import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import java.util.List;
/**
 * Created by Ahmad El-baba on 1/3/2017.
 */
public class QuestWindow implements Common {

    MessageEngine messageEngine;
    int currentQuest;
  List<Quest> questList = new ArrayList();

    // width of white border
    private static final int EDGE_WIDTH = 2;
    private boolean isVisible = false;

    private int questBoardSpot = 0;
    private boolean isMoving;

    private int moveLength;
    private int QuestSlotSpacing = 40;

    private Thread threadAnime;

    // outer frame
    private Rectangle boarder;
    // inner frame
    private Rectangle innerRect;

    //CCurson
    private Rectangle cursor;

    public QuestWindow(Rectangle rect) {
        messageEngine = new MessageEngine();
        cursor = new Rectangle(70, 138, 214, 28);
        this.boarder = rect;
        innerRect = new Rectangle(
                rect.x + EDGE_WIDTH,
                rect.y + EDGE_WIDTH,
                rect.width - EDGE_WIDTH * 2,
                rect.height - EDGE_WIDTH * 2);

        // run thread
        threadAnime = new Thread(new QuestWindow.AnimationThread());
        threadAnime.start();
    }

    public void draw(Graphics g) {
        if (isVisible == false) {
            return;
        }
        g.setColor(Color.WHITE);
        g.fillRect(boarder.x, boarder.y, boarder.width, boarder.height);

        g.setColor(Color.BLACK);
        g.fillRect(innerRect.x, innerRect.y, innerRect.width, innerRect.height);
  messageEngine.setColor(0);
        g.setColor(Color.WHITE);
        messageEngine.drawMessage(125, 96, "QUESTS", g);
        messageEngine.drawMessage(380, 96, "DESCRIPTION", g);
        //basicly the line dividng quests and discriptions
        g.fillRect(288, 98, 2, 350);

        g.drawRect(cursor.x, cursor.y + QuestSlotSpacing * questBoardSpot, cursor.width, cursor.height);

        //drawing quest titles
     if (!questList.isEmpty()) {
            for (int i = 0; i < questList.size(); i++) {
                try{
                  for (int c = 0; c <13; c++) {
               if (questList.get(i).questFinished) {
                  
               
                   messageEngine.setColor(320);
                         messageEngine.drawMessage(c*15+70, 140 + i * 40, String.valueOf(questList.get(i).getQuestName().charAt(c)), g);
                } else {
                    messageEngine.setColor(0);
                         messageEngine.drawMessage(c*15+70, 140 + i * 40,String.valueOf(questList.get(i).getQuestName().charAt(c)), g);
                }
           
      
               
                
         
                if (cursor.contains(70, 140 + i * 40)) {
                    
                    g.setColor(Color.white);
                      messageEngine.setColor(0);
                    messageEngine.drawMessage(300, 140, questList.get(questBoardSpot).getQuestDisctription(), g);
                    messageEngine.drawMessage(300, 410, "REWARD " + String.valueOf(questList.get(questBoardSpot).getReward()), g);
                }
           
                  }    
                }catch(Exception e){}
        
            }
        }

    

    }

    public void addQuest(Quest newQuest) {
        questList.add(newQuest);
    }
     public void sendQuestList(List<Quest> quests, int currentQuestTransfer) {
        this.questList = quests;
        this.currentQuest = currentQuestTransfer;
    }

    public void show() {
        isVisible = true;
    }

    public void setDirection(int direction) {

        switch (direction) {

            case DOWN:
                if (!isMoving) {
                    isMoving = true;
                    if (moveDown()) {
                        questBoardSpot++;
                    }
                }
                isMoving = false;
                break;
            case UP:
                if (!isMoving) {
                    isMoving = true;
                    if (moveUp()) {
                        questBoardSpot--;
                    }
                }
                isMoving = false;
                break;
        }
    }

    public void hide() {
        isVisible = false;
    }

    private boolean moveUp() {
        
         if(questBoardSpot==0){
         questBoardSpot = currentQuest;
        }else{
               return (questBoardSpot > 0);
        }
       return(questBoardSpot == currentQuest+1);  
   
        
        
    }

    private boolean moveDown() {
        if(questBoardSpot==currentQuest){
         questBoardSpot = 0;
        }else{
           return (questBoardSpot < currentQuest);  
        }
       return(questBoardSpot == -1);  
    }

    public boolean isMoving() {
        return isMoving;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setMoving(boolean flag) {
        isMoving = flag;
        moveLength = 0;
    }

    public int getQuestBoardPos() {
        return questBoardSpot;
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
