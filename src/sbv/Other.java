package sbv;

import java.text.SimpleDateFormat;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

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
//        JDialog meinJDialog = new JDialog();
//        meinJDialog.setTitle("Fehler");     
//        meinJDialog.setBounds(300, 250, 0, 0);
//        meinJDialog.setSize(750, 100);
//        meinJDialog.setModal(true);
//        JLabel textoben = new JLabel(text);
//
//        textoben.setForeground(Color.red);
//        meinJDialog.add(textoben);
//
//        Container contentpane = meinJDialog.getContentPane();
//        contentpane.setBackground(Color.black);
//
//        meinJDialog.setVisible(true);
        JOptionPane.showMessageDialog(new JFrame(), text, "Fehler", JOptionPane.ERROR_MESSAGE);
    }

}
