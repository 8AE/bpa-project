
import java.awt.Point;
import java.io.Serializable;


/**
 *
 * @author Ahmad El-baba
 */
public class Quest implements Serializable {

    String questName;
    String questDisctription;
    Point DXY;
  
    String reward;
    String questType;
    int expGained;
    boolean questFinished;

    public Quest() {
        this.questName = "Test";
        this.questDisctription = "This is a test";
        this.expGained = 0;
        this.reward = "Test Item";
        this.questFinished = false;
    }

    public Quest(String questType,String questName, String questDescription, int expGained, String reward) {
        this.questType = questType;
        this.questName = questName;
        this.questDisctription = questDescription;
        this.expGained = expGained;
        this.reward = reward;
        this.questFinished = false;
    }

    public boolean isQuestFinished() {
        return questFinished;
    }

    public void setQuestFinished(boolean questFinished) {
        this.questFinished = questFinished;
    }

    public String getQuestName() {
        return questName;
    }

    public String getQuestDisctription() {
        return questDisctription;
    }

    public String getQuestType() {
        return questType;
    }

    public Point getDXY() {
        return DXY;
    }

    public void setDXY(Point DXY) {
        this.DXY = DXY;
    }

   

  

 

    public int getExpGained() {
        return expGained;
    }

    public String getReward() {
        return reward;
    }

    // What is this??
    public void createQuest(String QT,String QN, String desc, int exp, String reward) {
        this.questType =QT;
        this.questName = QN;
        this.questDisctription = desc;
        this.expGained = exp;
        this.reward = reward;
    }
}
