
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Ahmad El-baba 2/7/2017
 */
public class QuestEngine {
 

 
public QuestEngine(){

        }
    //this method below essentaly discards any uneed data depending on the type
    public void chooseType(Quest quest,QuestEvent questEvent){
        switch(quest.getQuestType()){
            case "OBJECTIVE":
                quest.setDXY(questEvent.getDXY());
               
                
                
                break;
            case "Collective":
                
                
                
                break;
            case "Elimination":
                
                
                
                break;
        }
        
    }
    
}
