package sbv;

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
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}''", new Object[]{e, timestamp});
        }
        return timestamp;
    }

    public static void errorWin(String text) {
        JOptionPane.showMessageDialog(new JFrame(), text, "Fehler", JOptionPane.ERROR_MESSAGE);
        logger.log(Level.OFF, "printed Error: {0}", new Object[]{text});
    }

    public static void connectionErrorWin() {
        final Object[] ops = {"erneut Versuchen", "Programm schließen"};
        int ret = JOptionPane.showOptionDialog(new JFrame(),
                "Es konnte keine Verbindung zur Datenbank hergestellt werden.\n"
                + "Für die Benutzung des Programmes muss eine Verbindung mit dem Datenbankserver aufgebaut werden können.\n"
                + "Bei weiteren Problemen wenden Sie sich bitte an Hr. Würz oder Hr. Hirzler.\n"
                + "",
                "Verbindungsfehler",
                JOptionPane.WARNING_MESSAGE,
                JOptionPane.YES_NO_OPTION,
                null,
                ops, ops[0]);
        logger.log(Level.OFF, "Error while trying to connect to DB");
        if (ret == 1) {
            logger.log(Level.INFO, "Closed programm by exit(0), cause of Error while trying to connect to DB");
            System.exit(0);
        }
    }

}
