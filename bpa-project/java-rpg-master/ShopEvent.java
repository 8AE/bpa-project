
import java.io.Serializable;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Ahmad 2/16/2017
 */
public class ShopEvent extends Event implements Serializable {

    public ShopEvent(int x, int y) {
        super(x, y, 13, true);
    }

}
