
import java.io.Serializable;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author hpro1
 */
public class Item implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(MainPanel.class.getName());

    String name;
    String description;
    int id;

    public Item(String name, String description, int id) {
        this.name = name;
        this.description = description;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

}
