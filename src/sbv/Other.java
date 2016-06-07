package sbv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import static sbv.Sbv.logger;

public class Other {

    public static String dateToNormal(String timestamp) {
        try {
            String s;
            long stuff = Long.parseLong(timestamp);
            java.util.Date time = new java.util.Date((long) stuff * 1000);
//            s = new SimpleDateFormat("MM/dd/yyyy").format(time);
            s = new SimpleDateFormat("dd.MM.yyyy").format(time);
            return s;
        } catch (Exception e) {
            System.out.println(e + " => date");
        }
        return timestamp;
    }

    public static void errorWin(String text) {
        JOptionPane.showMessageDialog(new JFrame(), text, "Fehler", JOptionPane.ERROR_MESSAGE);
    }

    public static Connection getConnection() {
        while (true) {
            try {
                final String driver = "com.mysql.jdbc.Driver";                //chosing driver
                final String url = "jdbc:mysql://localhost:3307/sbv_aes_2013";//choosing mySQL server
                final String username = "root";                               //DB ussername and password
                final String password = "usbw";
                Class.forName(driver);
                Connection con = DriverManager.getConnection(url, username, password); //Connecting
                System.out.println("Connected");                                     //conectian establischt notification
            logger.log(Level.INFO, "connected to Server {0}", new Object[]{url});
                return con;
            } catch (ClassNotFoundException | SQLException e) {
                System.out.println(e + " => getConnection");
                Other.errorWin("Verbindung zum Server fehlgeschlagen");
            }
        }
    }
}
