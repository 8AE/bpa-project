import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.util.Timer;

public class MessageWindow {
    // width of white border
    private static final int EDGE_WIDTH = 2;

    protected static final int LINE_HEIGHT = 8;
    private static final int MAX_CHAR_PER_LINE = 20;
    private static final int MAX_LINE_PER_PAGE = 3;
    private static final int MAX_CHAR_PER_PAGE = MAX_CHAR_PER_LINE * MAX_LINE_PER_PAGE;
    private static final int CHAR_FOR_TAB = 5;

    // outer frame
    private Rectangle rect;
    // inner frame
    private Rectangle innerRect;
    // text frame
    private Rectangle textRect;

    // message window is visible ?
    private boolean isVisible = false;

    // cursor animation gif
    private Image cursorImage;

    // message array
    private char[] text = new char[128 * MAX_CHAR_PER_LINE];
    private int maxPage;
    private int curPage = 0;
    private int curPos;
    private boolean nextFlag = false;
    public static boolean selectFlag = false;
    private int selectLine = 0;
    public static int selectOption = 0;
    public static String commandOption1 = "";
    public static String commandOption2 = "";
    public static String commandVariable = "";
    
    private MessageEngine messageEngine;

    private Timer timer;
    private TimerTask task;

    public MessageWindow(Rectangle rect) {
        this.rect = rect;
        innerRect = new Rectangle(
                rect.x + EDGE_WIDTH,
                rect.y + EDGE_WIDTH,
                rect.width - EDGE_WIDTH * 2,
                rect.height - EDGE_WIDTH * 2);

        textRect = new Rectangle(
                innerRect.x + 16,
                innerRect.y + 16,
                320,
                120);

        messageEngine = new MessageEngine();

        // load cursor image
        ImageIcon icon = new ImageIcon(getClass().getResource("image/cursor.gif"));
        cursorImage = icon.getImage();

        timer = new Timer();
    }

    public void draw(Graphics g) {
        if (isVisible == false) {
            return;
        }

        // draw outer rect
        g.setColor(Color.WHITE);
        g.fillRect(rect.x, rect.y, rect.width, rect.height);

        // draw inner rect
        g.setColor(Color.BLACK);
        g.fillRect(innerRect.x, innerRect.y,
                   innerRect.width, innerRect.height);

        // draw a current page
        for (int i = 0; i < curPos; i++) {
            char c = text[curPage * MAX_CHAR_PER_PAGE + i];
            int dx = textRect.x + MessageEngine.FONT_WIDTH * (i % MAX_CHAR_PER_LINE);
            int dy = textRect.y + (LINE_HEIGHT + MessageEngine.FONT_HEIGHT) * (i / MAX_CHAR_PER_LINE);
            messageEngine.drawCharacter(dx, dy, c, g);
        }
        
        // draw a cursor if the current page is not the last page
        if (curPage < maxPage && nextFlag) {
            int dx = textRect.x + (MAX_CHAR_PER_LINE / 2) * MessageEngine.FONT_WIDTH - 8;
            int dy = textRect.y + (LINE_HEIGHT + MessageEngine.FONT_HEIGHT) * 3;
            g.drawImage(cursorImage, dx, dy, null);
        }
        
        // draw a cursor to select an option if one is given
        if (selectFlag) {
            int dx = textRect.x + selectOption*MessageEngine.FONT_WIDTH * 9;
            int dy = textRect.y + MessageEngine.FONT_HEIGHT + LINE_HEIGHT;
            g.drawImage(cursorImage, dx, dy, null);
        }
    }

    public void setMessage(String msg) {
        curPos = 0;
        curPage = 0;
        nextFlag = false;
        selectFlag = false;
        selectLine = 0;
        selectOption = 0;

        // initialize
        for (int i=0; i<text.length; i++) {
            text[i] = ' ';
        }
        
        boolean isText = true;
        boolean isCommand = false;
        String selection = "";
        int firstSelectionWordLength = 0;
        
        int p = 0;  // current position
        for (int i = 0; i < msg.length(); i++) {
            char c = msg.charAt(i);
            if (c == '/') {         // new line
                p += MAX_CHAR_PER_LINE;
                p = (p / MAX_CHAR_PER_LINE) * MAX_CHAR_PER_LINE;
            } else if (c == '|') {  // new page
                p += MAX_CHAR_PER_PAGE;
                p = (p / MAX_CHAR_PER_PAGE) * MAX_CHAR_PER_PAGE;
            } else if (c == 't') {
                p += CHAR_FOR_TAB;
            } else if (c == '(') {
                text[p++] = ' ';
                isText = false;
            } else if (c == ';') {
                for (int j = 0; j < selection.length(); j++) {
                    text[p++] = selection.charAt(j);
                }
                for (int j = 0; j < 9 - firstSelectionWordLength; j++) {
                    text[p++] = ' ';
                }
                selection = "";
            } else if (c == ')') {
                for (int j = 0; j < selection.length(); j++) {
                    text[p++] = selection.charAt(j);
                }
                selection = "";
                selectFlag = true;
                isText = true;
            } else if (c == '[') {
                isText = false;
                isCommand = true;
            } else if (c == ']') { 
                commandOption1 = selection;
                selection = "";
                isText = true;
                isCommand = false;
            } else if (c == '<') {
                isText = false;
                isCommand = true;
            } else if (c == '>') { 
                commandOption2 = selection;
                selection = "";
                isText = true;
                isCommand = false;
            } else if (c == ':') { 
                i++;
                while (msg.charAt(i) != '>' && msg.charAt(i) != ']') {
                    commandVariable+=msg.charAt(i);
                    i++;
                }
                i--;
            } else if (isText) {
                text[p++] = c;
            } else {
                firstSelectionWordLength++;
                selection+=c;
            }
        }
        
        System.out.println(commandVariable);
        
        maxPage = p / MAX_CHAR_PER_PAGE;

        task = new FlowingMessageTask();
        timer.schedule(task, 0L, 20L);
    }

    public boolean isCommand() {
        return selectFlag;
    }
    
    public void checkCommand(Character c) {
        System.out.println(c.getMessage());
        if (commandString().equalsIgnoreCase("hi")) {
            c.setMessage(commandVariable);
            System.out.println(c.getMessage());
        } else if (commandString().equalsIgnoreCase("bye")) {
            System.out.println("bye");
        }
    }
    
    public String commandString() {
        if (selectOption == 0) {
            return commandOption1;
        } else if (selectOption == 1) {
            return commandOption2;
        }
        return null;
    }
    
    public boolean nextPage() {
        if (curPage == maxPage) {
            task.cancel();
            task = null;
            return true;
        }
        if (nextFlag) {
            curPage++;
            curPos = 0;
            nextFlag = false;
        }
        return false;
    }

    public void show() {
        isVisible = true;
    }

    public void hide() {
        isVisible = false;
    }

    public boolean isVisible() {
        return isVisible;
    }
    
    public void cursorLeft() {
        selectOption = 0;
    }
    
    public void cursorRight() {
        selectOption = 1;
    }

    class FlowingMessageTask extends TimerTask {
        public void run() {
            if (!nextFlag) {
                curPos++;
                if (curPos % MAX_CHAR_PER_PAGE == 0) {
                    nextFlag = true;
                }
            }
        }
    }

}
