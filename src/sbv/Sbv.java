package sbv;

//Leave these methods where they are for testing purposes//////////////
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

//Query.Console();
//    Oberflaeche ob = new Oberflaeche();
//PDF_Export.bestandPDF(ob);
   /*System.out.println(Copies.SingleCopyCountTotal("1"));
 System.out.println(Copies.boughtCopyCount("1"));
 System.out.println(Copies.borrowedCopyCount("1"));
 System.out.println(Copies.CopiesInStock("1"));*/
public class Sbv {

    public static final Logger logger = Logger.getLogger("Sbv-Log");

    /**
     * Alte Login Maske Code private JLabel jLabel1 = new JLabel(); private
     * JLabel jLabel2 = new JLabel(); private JTextField username = new
     * JTextField(); private JPasswordField passwort = new JPasswordField();
     * private JTextField password = new JTextField(); private JTextField erfolg
     * = new JTextField(); private JButton login = new JButton(); String user;
     * String pw; private String[] args;
     *     
*  * Ende Attribute
     *     
* public Sbv(String title) { * Frame-Initialisierung super(title);
     * setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); int
     * frameWidth = 300; int frameHeight = 300; setSize(frameWidth,
     * frameHeight); Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
     * int x = (d.width - getSize().width) / 2; int y = (d.height -
     * getSize().height) / 2; setLocation(x, y); setResizable(false); Container
     * cp = getContentPane(); cp.setLayout(null); * Anfang Komponenten
     *     
* jLabel1.setBounds(8, 40, 131, 25); jLabel1.setText("Benutzername");
     * cp.add(jLabel1); jLabel2.setBounds(8, 96, 131, 25);
     * jLabel2.setText("Passwort"); cp.add(jLabel2); username.setBounds(176, 48,
     * 97, 17); cp.add(username); passwort.setBounds(176, 96, 97, 17);
     * cp.add(passwort); passwort.setColumns(9); passwort.setEchoChar('*');
     * passwort.addActionListener(new java.awt.event.ActionListener() { public
     * void actionPerformed(java.awt.event.ActionEvent evt) {
     * jButton1_ActionPerformed(evt); } });
     *     
* erfolg.setBounds(48, 192, 201, 25); cp.add(erfolg); login.setBounds(104,
     * 152, 89, 17); login.setText("Einloggen"); login.setMargin(new Insets(2,
     * 2, 2, 2)); login.addActionListener(new ActionListener() {
     *
     * @param args
     * @throws java.lang.Exception
     * @Override public void actionPerformed(ActionEvent evt) {
     * jButton1_ActionPerformed(evt); } }); cp.add(login); * Ende Komponenten
     *     
* setVisible(true); } * end of public LoginMaske
     *     
*  * Anfang Methoden public void jButton1_ActionPerformed(ActionEvent evt)
     * { user = username.getText(); pw = passwort.getText();
     *     
*
     * if (user.equalsIgnoreCase("Admin") && pw.equals("1234")) {
     * erfolg.setText("Erfolgreich"); Oberflaeche.main(args); setVisible(false);
     * } else { erfolg.setText("Invalid User Data"); } * end of if-else } * end
     * of jButton1_ActionPerformed
     *     
*  * Ende Methoden
     */
    public static void main(String[] args) throws Exception {
        FileHandler fh;

        DateFormat dateForm = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Date date = new Date();
        fh = new FileHandler("C:/Users/Falko/Desktop/test/Sbv-Verlauf-" + dateForm.format(date) + ".log");

        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();

        fh.setFormatter(formatter);

        logger.log(Level.SEVERE, "created Log");
//        logger.log(Level.INFO, "test");
//        logger.log(Level.OFF, "test");
//        logger.log(Level.SEVERE, "test");
//        logger.log(Level.WARNING, "test");
        
        //UpdateDb.massAddClasses(328, 350, 14);
        //Students.newStudent("max", "mustermann", "1999-10-05");
        // new LoginMaske("Login");
        loginMaske.main(args);
    }
}
