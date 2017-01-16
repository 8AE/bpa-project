/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Ahmad El-baba
 */
public class Quest{
    String questName;
    String questDisctription;
    int expGained;
    String reward;
    boolean questFinished;
    
    public Quest(){
        this.questName = "Test";
        this.questDisctription = "This is a test";
        this.expGained = 0;
        this.reward = "Test Item";
        this.questFinished = false;
    }

    // We should remove all the setter methods aside from setQuestFinished()
    // Also, the constructor should not have quest finished since we will never create a quest finished.. they must complete it
    public Quest(String questName, String questDescription, int expGained, String reward){
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

    public int getExpGained() {
        return expGained;
    }

    public String getReward() {
        return reward;
    }
    
    // What is this??
    public void createQuest(String QN, String desc, int exp, String reward){
        this.questName = QN;
        this.questDisctription = desc;
        this.expGained = exp;
        this.reward = reward;
    }
}
