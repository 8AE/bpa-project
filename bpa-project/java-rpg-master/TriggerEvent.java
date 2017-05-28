
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Ahmad 2/8/17
 */
public class TriggerEvent extends Event implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(MainPanel.class.getName());

    private List<Point> tLocation;

    public TriggerEvent(int x, int y) {
        super(x, y, 50, false);
        tLocation = new ArrayList();
        tLocation.add(new Point(x, y));

    }

    public Point getPoint(int x) {
        return tLocation.get(x);
    }

    public List<Point> gettLocation() {
        return tLocation;
    }

}
