
import java.io.Serializable;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Ahmad El-baba & Noah Curran
 */
public class Weapon extends Item implements Common, Serializable {

    private static final Logger LOGGER = Logger.getLogger(MainPanel.class.getName());

    protected int tileRange;
    protected int damage;

    public Weapon(String name, String description, int damage, int id, int tileRange) {
        super(name, description, id);
        this.damage = damage;
        this.id = id;
        this.tileRange = tileRange;
    }

    public int getDamage() {
        return this.damage;
    }

    public int getRange() {
        return this.tileRange;
    }

}
