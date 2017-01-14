/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Save The Holy Lands
 */
public class QuestSystem{
        String questName;
     String questDisctription;
     int expGained;
     String reward;
     boolean questFinished;
public QuestSystem(){
questName="test";
questDisctription = "moreTest";
expGained = 100;
reward = "Holy Sword";
questFinished = false;
}
public QuestSystem(String QN, String desc, int exp, String newReward, boolean questComplete){
    QN= questName;
    desc = questDisctription;
    exp = expGained;
    newReward = reward;
    questComplete = questFinished;
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

    public void setQuestName(String questName) {
        this.questName = questName;
    }

    public String getQuestDisctription() {
        return questDisctription;
    }

    public void setQuestDisctription(String questDisctription) {
        this.questDisctription = questDisctription;
    }

    public int getExpGained() {
        return expGained;
    }

    public void setExpGained(int expGained) {
        this.expGained = expGained;
    }

    public String getReward() {
        return reward;
    }

    public void setReward(String reward) {
        this.reward = reward;
    }
   
    
    public void createQuest(String QN, String desc, int exp, String reward){
           this.questName = QN;
             this.questDisctription = desc;
                  this.expGained = exp;
                      this.reward = reward;
    }
}
