import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.lang.Character;
import java.util.HashMap;

import javax.swing.ImageIcon;

// in this class, Character does not mean Player!

public class MessageEngine {
    // font size
    public static final int FONT_WIDTH = 16;
    public static final int FONT_HEIGHT = 22;

    // font colors
    public static final int WHITE = 0;
    public static final int RED = 160;
    public static final int GREEN = 320;
    public static final int BLUE = 480;

    // font image
    private Image fontImage;
    // character's position
    private HashMap<Character, Point> characterPos;

    // current color
    private int color;

    public MessageEngine() {
        // load font image
        ImageIcon icon = new ImageIcon(getClass().getResource("image/font.gif"));
        fontImage = icon.getImage();

        color = WHITE;

        // calculate each character's position
        characterPos = new HashMap<Character, Point>();
        createHash();
    }

    public void setColor(int c) {
        if (color == WHITE || color == RED || color == GREEN || color == BLUE) {
            this.color = c;
        } else {
            this.color = WHITE;
        }
    }

    public void drawMessage(int x, int y, String message, Graphics g) {
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            int dx = x + FONT_WIDTH * i;
            drawCharacter(dx, y, c, g);
        }
    }

    public void drawCharacter(int x, int y, char c, Graphics g) {
        Point pos = characterPos.get(new Character(c));
        if (pos == null) {
            return;
        }
        g.drawImage(fontImage,
                    x,
                    y,
                    x + FONT_WIDTH,
                    y + FONT_HEIGHT,
                    pos.x + color,
                    pos.y,
                    pos.x + color + FONT_WIDTH,
                    pos.y + FONT_HEIGHT, null);
    }

    private void createHash() {

        // Numbers
        characterPos.put(new Character('0'), new Point(0, 352));
        characterPos.put(new Character('1'), new Point(16, 352));
        characterPos.put(new Character('2'), new Point(32, 352));
        characterPos.put(new Character('3'), new Point(48, 352));
        characterPos.put(new Character('4'), new Point(64, 352));
        characterPos.put(new Character('5'), new Point(80, 352));
        characterPos.put(new Character('6'), new Point(96, 352));
        characterPos.put(new Character('7'), new Point(112, 352));
        characterPos.put(new Character('8'), new Point(128, 352));
        characterPos.put(new Character('9'), new Point(144, 352));

        // Capital Letters
        characterPos.put(new Character('A'), new Point(0, 374));
        characterPos.put(new Character('B'), new Point(16, 374));
        characterPos.put(new Character('C'), new Point(32, 374));
        characterPos.put(new Character('D'), new Point(48, 374));
        characterPos.put(new Character('E'), new Point(64, 374));
        characterPos.put(new Character('F'), new Point(80, 374));
        characterPos.put(new Character('G'), new Point(96, 374));
        characterPos.put(new Character('H'), new Point(112, 374));
        characterPos.put(new Character('I'), new Point(128, 374));
        characterPos.put(new Character('J'), new Point(144, 374));

        characterPos.put(new Character('K'), new Point(0, 396));
        characterPos.put(new Character('L'), new Point(16, 396));
        characterPos.put(new Character('M'), new Point(32, 396));
        characterPos.put(new Character('N'), new Point(48, 396));
        characterPos.put(new Character('O'), new Point(64, 396));
        characterPos.put(new Character('P'), new Point(80, 396));
        characterPos.put(new Character('Q'), new Point(96, 396));
        characterPos.put(new Character('R'), new Point(112, 396));
        characterPos.put(new Character('S'), new Point(128, 396));
        characterPos.put(new Character('T'), new Point(144, 396));

        characterPos.put(new Character('U'), new Point(0, 418));
        characterPos.put(new Character('V'), new Point(16, 418));
        characterPos.put(new Character('W'), new Point(32, 418));
        characterPos.put(new Character('X'), new Point(48, 418));
        characterPos.put(new Character('Y'), new Point(64, 418));
        characterPos.put(new Character('Z'), new Point(80, 418));
        characterPos.put(new Character('!'), new Point(96, 418));
        characterPos.put(new Character('?'), new Point(112, 418));
        characterPos.put(new Character(' '),  new Point(64, 198));
    }
}
