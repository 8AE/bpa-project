
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JOptionPane;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Noah
 */
public class CrashReport implements Serializable {

    FileWriter fw;
    PrintWriter pw;
    Exception error;

    // A popup appears on the screen saying that the program has crashed and asks
    // if the user wishes to send a crash report.
    public CrashReport(Exception error) throws IOException {

        this.error = error;

        fw = new FileWriter("errors.txt", true);
        pw = new PrintWriter(fw);

    }

    public void show() throws IOException {

        // Error saved to a text file.
        pw.print(new SimpleDateFormat("[yyyy.MM.dd.HH:mm:ss] ").format(new Date()));
        error.printStackTrace(pw);
        pw.close();
        fw.close();

        // Dialog box created.
        int selection = JOptionPane.showConfirmDialog(
                null,
                error + " Please restart game."
                + "\nAll progress since last save has been lost."
                + "\n\n\tWould you like to email the crash report?",
                "ERROR",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE);

        if (selection == 0) {
            sendMail(error);
            System.exit(0);
        } else if (selection == 1) {
            System.exit(0);
        }
    }

    // An email is sent to our presupplied email. It helps us to see where the crashes occur and which version of
    // the game is being used.
    private void sendMail(Exception report) {
        // NOT SECURE!
        final String username = "bpajavagame@gmail.com";
        final String password = "BPAruleZ";

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        report.printStackTrace(pw);
        String reportString = sw.toString();

        // Following code obtained mostly from https://www.tutorialspoint.com/javamail_api/javamail_api_gmail_smtp_server.htm
        // Has been motified to work with our case.
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("bpajavagame@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("bpajavagame@gmail.com"));
            message.setSubject("Crash Report [VERSION: " + RPG.VERSION + "]");
            message.setText("Here is a reported crash!"
                    + "\n\nVersion: " + RPG.VERSION
                    + "\n\n" + reportString);

            Transport.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
