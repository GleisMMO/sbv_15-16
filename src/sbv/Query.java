package sbv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static sbv.Sbv.logger;

public class Query {

    private static Connection con;

    public static void getConnection() {
        final boolean server = true;
        if (con == null) {
            final String driver;
            final String url;
            if (server) {
                driver = "org.mariadb.jdbc.Driver";                  //chosing driver
                url = "jdbc:mariadb://db-server:3306/sbv_aes_2013";  //choosing mySQL server  alt:jdbc:mysql://localhost:3307/sbv_aes_2013
            } else {
                driver = "com.mysql.jdbc.Driver";                  //chosing driver
                url = "jdbc:mysql://localhost:3307/sbv_aes_2013";  //choosing mySQL server
            }
            boolean loop = true;
            while (loop) {
                try {
                    final String username;
                    final String password;
                    if (server) {
                        username = "buchscanner"; 
                        password = "Buchscaner"; 
                    } else {
                        username = "root"; 
                        password = "usbw";
                    }

                    Class.forName(driver);
                    con = DriverManager.getConnection(url, username, password);     //Connecting
                    loop = false;
                } catch (ClassNotFoundException | SQLException e) {
                    System.out.println(e + " => getConnection");
                    Other.connectionErrorWin();
                }
            }
            logger.log(Level.INFO, "connected to Server {0}", new Object[]{url});
        }
    }

    public static void disconnect() {
        con = null;
        logger.log(Level.INFO, "disconnected from Server");
    }

    public static String getString(String Statement, String label) {
        try {
            ResultSet result = con.prepareStatement(Statement).executeQuery();
            result.next();
            return result.getString(label);
        } catch (Exception e) {
            System.out.println(e + " => getString");
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}''", new Object[]{e, Statement});
        }
        return null;
    }

    public static ArrayList<String> anyQuery(String input) throws Exception {
        try {
            ResultSet result = con.prepareStatement(input).executeQuery();        // gets results
            ArrayList<String> array = new ArrayList();          //Arraylist for Results
            final String[] collum = TableNames(input);                //gets collum names
            //42 <3
            while (result.next()) {                              //Saves results
                for (int i = 0; i < collum.length; i++) {
                    array.add(result.getString(collum[i]));
                }
            }
            //con.close();
            return array;
        } catch (Exception e) {
            System.out.println(e + " => anyQuery");
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}''", new Object[]{e, input});
        }
        return null;
    }

    public static void anyUpdate(String input) throws Exception {
        try {
            con.prepareStatement(input).executeUpdate(input);                         //updates DB gets results 
            //con.close();
            logger.log(Level.INFO, "updated Database with command ''{0}''", new Object[]{input});
        } catch (Exception e) {
            System.out.println(e + " => anyUpdate");
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}''", new Object[]{e, input});
        }
    }

    public static String[] TableNames(String statement) {

        Matcher rawMatcher = Pattern.compile("SELECT.*FROM").matcher(statement);
        rawMatcher.find();

        Matcher selectMatcher = Pattern.compile("SELECT ").matcher(statement);
        selectMatcher.find();

        Matcher fromMatcher = Pattern.compile("FROM").matcher(statement);
        fromMatcher.find();                                               //for removing the "," of the SQL statement

        StringBuilder raw = new StringBuilder(" ");                                    // SELECT and FROM gets cut out from Statement Stringbuffer 
        String tableLong = rawMatcher.group();                                       //and splited to an array of words (the collum names)
        raw.insert(0, tableLong);
        raw.delete(fromMatcher.start(), fromMatcher.end());
        raw.delete(selectMatcher.start(), selectMatcher.end());
        tableLong = raw.toString();
        tableLong = tableLong.replaceAll(",", "");
        return tableLong.split(" ");
    }
}

//
//
//    //primitiv method for SystemPrinting SQL Results
//    public static void output(ArrayList<String> result, String[] collums) {
//        int rows = collums.length;
//
//        for (int i = 0; i < result.size(); i = i + rows) {
//            for (int o = i; o < (rows + i); o++) {
//                System.out.print(result.get(o));
//                System.out.print(" ");
//            }
//            System.out.println(" ");
//        }
//
//    }
//SQL Console
//    public static void Console() throws Exception {
//        InputStreamReader Input = new InputStreamReader(System.in);
//        BufferedReader DataIn = new BufferedReader(Input);
//        ArrayList<String> result = new ArrayList();
//        //Connection con = DbConnector.getConnection(); //tests Connection
//
//        while (con != null) {
//            System.out.println("Enter SQL Statement");
//            String statement = DataIn.readLine();
//            String[] collums = Query.TableNames(statement);
//            result = Query.anyQuery(statement);
//            Query.output(result, collums);
//        }
//    }
