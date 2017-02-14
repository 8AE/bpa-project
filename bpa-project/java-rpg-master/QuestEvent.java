import java.awt.Point;
import java.io.Serializable;
import java.util.*;

public class QuestEvent extends Event implements Serializable {
      private String questType;
    private String questName;
    private String questDisctription;
    private int expGained;
    private String reward;
private Point DXY;

    public QuestEvent(int x, int y,String questType, String questName, String questDisctription,
            int expGained, String reward,int DX,int DY) {
        super(x, y, 24, false);
        this.questType = questType;
        this.questName = questName;
         this.questDisctription = questDisctription;
          this.expGained = expGained;
           this.reward = reward;
           this.DXY=new Point(DX,DY);
           
        
    }

    public String getQuestName() {
        return questName;
    }

    public Point getDXY(){
        return DXY;
    }

    public String getQuestDisctription() {
        return questDisctription;
    }

    public int getExp() {
        return expGained;
    }

    public String getQuestType() {
        return questType;
    }

    public String getReward() {
        return reward;
    }

    public String toString() {
        return "QUEST:" + super.toString() + ":" + questName;
    }

  
}
