

/**
 *
 * @author Ahmad El-baba & Noah Curran
 */
public abstract class Weapon implements Common {
    protected int tileRange;
    protected int damage;
    protected int id;
    protected String name;
     
    public Weapon(String name, int damage, int id, int tileRange){
        this.name = name;
     
    }
     
    public int getDamage() {
        return this.damage;
    }
    
    public int getRange() {
        return this.tileRange;
    }
      
}
