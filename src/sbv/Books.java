package sbv;

import java.util.ArrayList;
import java.util.logging.Level;
import static sbv.Sbv.logger;

public class Books {

    //spits out an arraylist of all books
    public static ArrayList<String> BookList() {
        try {
            return Query.anyQuery("SELECT label, isbn, price, buy "
                    + "FROM sbm_books "
                    + "ORDER BY label");
        } catch (Exception e) {
            System.out.println(e + " => BookList");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        return null;
    }

    //edits book
    public static void editBook(String ID, String label, String isbn, String price, String buy) {
        try {
            ArrayList<String> oldLabel = Query.anyQuery("SELECT label, isbn, price, buy "
                    + "FROM `sbm_books` "
                    + "WHERE ID = " + ID);
            Query.anyUpdate("UPDATE `sbm_books` "
                    + "SET label ='" + label + "', "
                    + "isbn = " + isbn + ", "
                    + "price = " + price + ", "
                    + "buy = " + buy + " "
                    + "WHERE ID = " + ID + ";");
            logger.log(Level.INFO, "edit book with ID {0} label: {1} to {2} isbn: {3} to {4} price: {5} to {6} buy: {7} to {8}",
                    new Object[]{ID, oldLabel.get(0), label, oldLabel.get(1), isbn, oldLabel.get(2), price, oldLabel.get(3), buy});
        } catch (Exception e) {
            System.out.println(e + " => editBook");
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}'' and ''{2}'' and ''{3}'' and {4}'' and {5}''", new Object[]{e, ID, label, isbn, price, buy});
        }
    }

    //creates new book
    public static void newBook(String label, String isbn, String price, String buy) {
        try {
            Query.anyUpdate("INSERT INTO `sbm_books` "
                    + "SET label ='" + label + "', "
                    + "isbn = " + isbn + ", "
                    + "price = " + price + ", "
                    + "buy = " + buy);
            logger.log(Level.INFO, "created new book ''{0}''", label);
        } catch (Exception e) {
            System.out.println(e + " => newBook");
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}'' and ''{2}'' and ''{3}'' and {4}''", new Object[]{e, label, isbn, price, buy});
        }
    }

    //gives information on one book
    public static ArrayList<String> singleBook(String sterm, int i) {
        if (i == 0) {
            try {
                return Query.anyQuery("SELECT label, isbn, price, buy, ID "
                        + "FROM sbm_books "
                        + "WHERE isbn Like '" + sterm + "'");
            } catch (Exception e) {
                System.out.println(e + " => singleBook");
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}'' and ''{2}''", new Object[]{e, sterm, i});
            }
        } else {
            try {
                return Query.anyQuery("SELECT label, isbn, price, buy, ID "
                        + "FROM sbm_books "
                        + "WHERE label Like '" + sterm + "'");
            } catch (Exception e) {
                System.out.println(e + " => singleBook");
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}'' and ''{2}''", new Object[]{e, sterm, i});
            }
        }
        return null;
    }

    //deletes book
    public static void delBook(String label) {
        try {
            Query.anyUpdate("DELETE FROM sbm_books"
                    + " WHERE label LIKE '" + label + "';");
            logger.log(Level.INFO, "deleted book ''{0}''", label);
        } catch (Exception e) {
            System.out.println(e + " => delBook");
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}''", new Object[]{e, label});
        }
    }
}

//    public static ArrayList<String> BookIDList() {
//        try {
//            return Query.anyQuery("SELECT ID "
//                    + "FROM sbm_books "
//                    + "ORDER BY ID");
//        } catch (Exception e) {
//            System.out.println(e + " => BookIDList");
//        }
//        return null;
//    }
//
//
//    public static String singleBookName(String bookID) {
//
//        try {
//            return Query.getString("SELECT label "
//                    + "FROM sbm_books "
//                    + "WHERE ID Like '" + bookID + "'", 
//                    "label");
//        } catch (Exception e) {
//            System.out.println(e + " => singleBook");
//        }
//        return null;
//
//    }
