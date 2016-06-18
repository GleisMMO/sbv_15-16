package sbv;

import java.util.logging.Level;
import static sbv.Sbv.logger;

public class Home {

    //Cureent Students count
    public static String StudentsCount() {
        try {
            return Query.getString("SELECT COUNT(ID) "
                    + "FROM sbm_students", 
                    "COUNT(ID)");
        } catch (Exception e) {
            System.out.println(e + " => StudentsCount");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        return null;
    }

    //returns number of copies in Stock
    public static String CauchtCopyCount() {
        try {
            final int history = Integer.parseInt(Query.getString("SELECT COUNT(ID) "
                    + "FROM sbm_copieshistory", 
                    "COUNT(ID)"));
            final int all = Integer.parseInt(Home.AllCopyCount());
            final int catchedhistory = Integer.parseInt(Query.getString("SELECT COUNT(sbm_copies.ID) "
                    + "FROM sbm_copieshistory , sbm_copies, sbm_books "
                    + "WHERE sbm_copies.ID = sbm_copieshistory.copy_id "
                    + "AND sbm_copies.book_id = sbm_books.ID "
                    + "AND buy = 0 AND collected LIKE '%' "
                    + "AND copy_id LIKE sbm_copies.ID", 
                    "COUNT(sbm_copies.ID)"));
            final int result = catchedhistory + (all - history);
            return Integer.toString(result);
        } catch (Exception e) {
            System.out.println(e + " => CauchtCopyCount");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        return null;
    }

    private static String AllCopyCount() {
        try {
            return Query.getString("SELECT COUNT(ID) "
                    + "FROM sbm_copies", 
                    "COUNT(ID)");
        } catch (Exception e) {
            System.out.println(e + " => WildCopyCount");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        return null;
    }
}


//    public static String boughtCopyCount() {
//        try {
//            return Query.getString("SELECT COUNT(ID) "
//                    + "FROM sbm_copieshistory, sbm_books, sbm_copies "
//                    + "WHERE buy = 1 "
//                    + "AND sbm_copies.ID = sbm_copieshistory.copy_id "
//                    + "AND sbm_copies.book_id = sbm_books.ID "
//                    + "AND collected LIKE ''", 
//                    "COUNT(ID)");
//        } catch (Exception e) {
//            System.out.println(e + " => CauchtCopyCount");
//        }
//        return null;
//    }

    //current count of borrowed copys
//    public static String WildCopyCount() {
//        try {
//            return Query.getString("SELECT COUNT(sbm_copieshistory.ID) "
//                    + "FROM sbm_copieshistory, sbm_books, sbm_copies "
//                    + "WHERE sbm_copies.ID = sbm_copieshistory.copy_id "
//                    + "AND sbm_copies.book_id = sbm_books.ID "
//                    + "AND collected LIKE '' "
//                    + "AND buy = 0", 
//                    "COUNT(sbm_copieshistory.ID)");
//        } catch (Exception e) {
//            System.out.println(e + " => WildCopyCount");
//        }
//        return null;
//    }