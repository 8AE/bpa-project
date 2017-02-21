
import java.awt.Point;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class QuestEvent extends Event implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(MainPanel.class.getName());

    private String questType;
    private String questName;
    private String questDisctription;
    private int expGained;
    private int target;
    private String reward;
    private Point DXY;

    public QuestEvent(int x, int y, String questType, String questName, String questDisctription,
            int expGained, String reward, int DX, int DY, int target) {
        super(x, y, 24, false);
        this.questType = questType;
        this.questName = questName;
        this.questDisctription = questDisctription;
        this.expGained = expGained;
        this.reward = reward;
        this.DXY = new Point(DX, DY);
        this.target = target;

    }

    public String getQuestName() {
        return questName;
    }

    public Point getDXY() {
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
    
    public int getTarget() {
        return target;
    }

    public String toString() {
        return "QUEST:" + super.toString() + ":" + questName;
    }

}
