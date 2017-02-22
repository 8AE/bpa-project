
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Noah
 */
public class LogReport {
    
    FileWriter fw;
    PrintWriter pw;
    String log;
    
    public LogReport(String log) throws IOException {
        this.log = log;
        
        fw = new FileWriter("systemlogs.txt", true);
        pw = new PrintWriter(fw);
    }
    
    public void saveLog() throws IOException {
        pw.println(new SimpleDateFormat("[yyyy.MM.dd.HH:mm:ss] ").format(new Date()) + log);
        pw.close();
        fw.close();
    }
    
}
