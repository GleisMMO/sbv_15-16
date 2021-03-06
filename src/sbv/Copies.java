package sbv;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import static sbv.Sbv.logger;

public class Copies {

    public static String SingleCopyCountTotal(String BookID) { //Takes super long
        try {
            return Query.getString("SELECT COUNT(ID) "
                    + "FROM sbm_copies "
                    + "WHERE book_id LIKE " + BookID,
                    "COUNT(ID)");
        } catch (Exception e) {
            System.out.println(e + " => SingleCopyCountTotal");
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}''", new Object[]{e, BookID});
        }
        return null;
    }

    public static String boughtCopyCount(String book_id) {
        try {
            return Query.getString("SELECT COUNT(sbm_copies.ID) "
                    + "FROM sbm_copieshistory , sbm_copies, sbm_books "
                    + "WHERE buy = 1 "
                    + "AND sbm_copies.book_id LIKE sbm_books.ID"
                    + "AND sbm_copieshistory.copy_id LIKE sbm_copies.ID "
                    + "AND book_id LIKE " + book_id,
                    "COUNT(sbm_copies.ID)");
        } catch (Exception e) {
            System.out.println(e + " => CauchtCopyCount");
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}''", new Object[]{e, book_id});
        }
        return null;
    }

    public static String copiesInStock(String book_id) {
        try {
            final int history = Integer.parseInt(Query.getString("SELECT COUNT(sbm_copieshistory.ID) "
                    + "FROM sbm_copieshistory , sbm_copies "
                    + "WHERE copy_id LIKE sbm_copies.ID "
                    + "AND book_id LIKE " + book_id,
                    "COUNT(sbm_copieshistory.ID)"));
            final int all = Integer.parseInt(SingleCopyCountTotal(book_id));
            final int caughthistory = Integer.parseInt(Query.getString("SELECT COUNT(sbm_copieshistory.ID) "
                    + "FROM sbm_copieshistory , sbm_copies "
                    + "WHERE collected LIKE '1%' "
                    + "AND copy_id LIKE sbm_copies.ID "
                    + "AND book_id LIKE " + book_id,
                    "COUNT(sbm_copieshistory.ID)"));
            return Integer.toString(caughthistory + (all - history));
        } catch (Exception e) {
            System.out.println(e + " => copiesInStock");
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}''", new Object[]{e, book_id});
        }
        return null;
    }

    public static String borrowedCopyCount(String book_id) {
        try {
            return Query.getString("SELECT COUNT(sbm_copies.ID) "
                    + "FROM sbm_copieshistory , sbm_copies, sbm_books "
                    + "WHERE buy = 0 "
                    + "AND collected LIKE '' "
                    + "AND sbm_copies.book_id LIKE sbm_books.ID "
                    + "AND sbm_copieshistory.copy_id LIKE sbm_copies.ID "
                    + "AND book_id LIKE " + book_id,
                    "COUNT(sbm_copies.ID)");
        } catch (Exception e) {
            System.out.println(e + " => borrowedCopyCount");
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}''", new Object[]{e, book_id});
        }
        return null;
    }

    public static ArrayList<String> copyBill(String student_id) {
        try {
            return Query.anyQuery("SELECT sbm_books.price "
                    + "FROM sbm_copieshistory, sbm_books, sbm_copies "
                    + "WHERE sbm_copies.book_id LIKE sbm_books.ID "
                    + "AND sbm_copieshistory.copy_id LIKE sbm_copies.ID "
                    + "AND student_id LIKE '" + student_id + "'");
        } catch (Exception e) {
            System.out.println(e + " => copyBill");
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}''", new Object[]{e, student_id});
        }
        return null;
    }

    public static ArrayList<String> Singlecopy(String copyId) {
        try {
            ArrayList<String> result = Query.anyQuery("SELECT label, sbm_students.ID, distributed, collected, buy, notice, paid, forename, surname, sbm_books.ID "
                    + "FROM sbm_copieshistory, sbm_copies, sbm_books, sbm_students "
                    + "WHERE sbm_books.ID = sbm_copies.book_id "
                    + "AND sbm_copieshistory.copy_id = sbm_copies.ID  "
                    + "AND sbm_copieshistory.student_id = sbm_students.ID "
                    + "AND sbm_copieshistory.copy_id LIKE " + copyId);
            if (result.isEmpty() == true) {
                result.add(0, Query.getString("SELECT label "
                        + "FROM sbm_copies, sbm_books "
                        + "WHERE sbm_books.ID = sbm_copies.book_id "
                        + "AND sbm_copies.ID LIKE " + copyId, "label"));
                result.add(1, "");
                result.add(2, "");
                result.add(3, "");
                result.add(4, "im lager");
                result.add(5, "");
                result.add(6, "im lager");
                result.add(7, "nicht ausgeliehen");
                result.add(8, "");
                result.add(9, Query.getString("SELECT sbm_books.ID "
                        + "FROM sbm_copies, sbm_books "
                        + "WHERE sbm_books.ID = sbm_copies.book_id "
                        + "AND sbm_copies.ID LIKE " + copyId, "sbm_books.ID"));
                return result;
            } else {
                /*result = Query.anyQuery("SELECT label, sbm_copieshistory.ID, distributed, collected, buy, notice, paid, forename, surname 
                 FROM sbm_copieshistory, sbm_copies, sbm_books, sbm_students 
                 WHERE sbm_books.ID = sbm_copies.book_id 
                 AND sbm_copieshistory.copy_id = sbm_copies.ID 
                 AND sbm_copieshistory.student_id = sbm_students.ID 
                 AND sbm_copieshistory.copy_id LIKE "+copyId); */
                return result;
            }
        } catch (Exception e) {
            System.out.println(e + " => Singlecopy");
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}''", new Object[]{e, copyId});
        }
        return null;
    }

    // eine buch ausleihen (mit copy id)
    public static void distributeCopy(String copy_id, String student_id, String buy) {
        final Date now = new Date();
        Long longTime = now.getTime() / 1000;
        longTime.intValue();
        final ArrayList<String> check = Singlecopy(copy_id);
        try {
            if ("".equals(check.get(1))) {
                Query.anyUpdate("INSERT INTO sbm_copieshistory "
                        + "SET paid = '0', notice = '', "
                        + "distributed = " + longTime + ", "
                        + "collected = '', "
                        + "student_id = " + student_id + ", "
                        + "copy_id = " + copy_id);
            logger.log(Level.INFO, "distributed copy {0} to {1}", new Object[]{copy_id, student_id});
            } else {
                Other.errorWin("Das Buch\n"
                        + "          " + check.get(0) + "\n"
                        + "ist bereits an den Schüler\n"
                        + "          " + check.get(7) + " " + check.get(8) + "\n"
                        + "ausgegeben!");
            }
        } catch (Exception e) {
            System.out.println(e + " => distributeCopy");
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}'' and ''{2}'' and ''{3}''", new Object[]{e, copy_id, student_id, buy});
        }
    }

    //buch einsammeln (mit copy id)
    public static void collectCopy(String copy_id) {
        final Date now = new Date();
        Long longTime = now.getTime() / 1000;
        longTime.intValue();

        final ArrayList<String> check = Singlecopy(copy_id);
        try {
            if ("".equals(check.get(1))) {
                Other.errorWin("Das Buch\n"
                        + "          " + check.get(0) + "\n"
                        + "mit dem Barcode\n"
                        + "          " + copy_id + "\n"
                        + "ist nicht ausgeliehen!");
            } else {
                Query.anyUpdate("DELETE FROM sbm_copieshistory "
                        + "WHERE copy_id "
                        + "LIKE " + copy_id);
            logger.log(Level.INFO, "collected copy {0} from {1}", new Object[]{copy_id, check.get(1)});
            }

        } catch (Exception e) {
            System.out.println(e + " => collectCopy");
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}''", new Object[]{e, copy_id});
        }
    }

    //copy erzeugen
    public static void addCopy(String book_id, int ID) {
        try {
            Query.anyUpdate("INSERT INTO sbm_copies "
                    + "SET book_id = " + book_id + ", "
                    + "ID = " + ID);
            logger.log(Level.INFO, "created new copy {0} from Book {1}", new Object[]{ID, book_id});
        } catch (Exception e) {
            System.out.println(e + " => addCopy");
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}''", new Object[]{e, book_id, ID});
        }
    }

    //copy löschen
    public static void deleteCopy(String ID) {
        try {
            Query.anyUpdate("DELETE FROM sbm_copies "
                    + "WHERE ID = " + ID);
            logger.log(Level.INFO, "deleted copy {0}", new Object[]{ID});
        } catch (Exception e) {
            System.out.println(e + " => addCopy");
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}''", new Object[]{e, ID});
        }
    }

    //nächste freie ID abrufen
    public static int newID() {
        try {
            return Integer.parseInt(Query.anyQuery("SELECT ID "
                    + "FROM sbm_copies "
                    + "Order BY ID DESC").get(0)) + 1;
        } catch (Exception e) {
            System.out.println(e + " => newID");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        return 0;
    }
    
}
//Copy kaufen
//    public static void copyBought(String copy_id) {
//        try {
//            Query.anyUpdate("UPDATE sbm_copieshistory, sbm_copies, sbm_books"
//                    + "SET buy = '1' "
//                    + "WHERE copy_id LIKE "
//                    + "AND sbm_copies.book_id LIKE sbm_books.ID "
//                    + "AND sbm_copieshistory.copy_id LIKE sbm_copies.ID "
//                    + copy_id);
//        } catch (Exception e) {
//            System.out.println(e + " => copyBought");
//        }
//    }
// how many copies peer label 
//    public static ArrayList<String> CopyCount(String BookID) { //Takes super long
//        try {
//            return Query.anyQuery("SELECT sbm_books.ID, sbm_books.label, COUNT(sbm_copieshistory.ID) FROM sbm_copieshistory, sbm_books, sbm_copies WHERE sbm_copies.book_id LIKE sbm_books.ID AND sbm_copieshistory.copy_id LIKE sbm_copies.ID GROUP BY sbm_books.label");
//        } catch (Exception e) {
//            System.out.println(e + "CopyCount");
//        }
//        return null;
//    }
//    public static String CopiesInStock(String book_id) {
//        try {
//            int history = Integer.parseInt(Query.getString("SELECT COUNT(sbm_copieshistory.ID) "
//                    + "FROM sbm_copieshistory , sbm_copies "
//                    + "WHERE copy_id LIKE sbm_copies.ID "
//                    + "AND book_id LIKE " + book_id,
//                    "COUNT(sbm_copieshistory.ID)"));
//            int all = Integer.parseInt(SingleCopyCountTotal(book_id));
//            int catchedhistory = Integer.parseInt(Query.getString("SELECT COUNT(sbm_copieshistory.ID) "
//                    + "FROM sbm_copieshistory , sbm_copies, sbm_books "
//                    + "WHERE buy = 0 "
//                    + "AND collected LIKE '%' "
//                    + "AND sbm_copies.book_id LIKE sbm_books.ID"
//                    + "AND copy_id LIKE sbm_copies.ID "
//                    + "AND book_id LIKE " + book_id,
//                    "COUNT(sbm_copieshistory.ID)"));
//            int result = catchedhistory + (all - history);
//            return Integer.toString(result);
//        } catch (Exception e) {
//            System.out.println(e + " => CauchtCopyCount");
//        }
//        return null;
//    }
//Buch informationen abhängig vom der copyID und einem index zum durchschalten der einzelnen infos 
//    public static String Singlecopy(String copyId, int index) {
//        try {
//            ArrayList<String> result = Query.anyQuery("SELECT label, sbm_copieshistory.ID, distributed, collected, buy, notice, paid "
//                    + "FROM sbm_copieshistory, sbm_copies, sbm_books, sbm_students "
//                    + "WHERE sbm_books.ID = sbm_copies.book_id "
//                    + "AND sbm_copieshistory.copy_id = sbm_copies.ID  "
//                    + "AND sbm_copieshistory.student_id = sbm_students.ID "
//                    + "AND sbm_copieshistory.copy_id LIKE " + copyId);
//            return result.get(index);
//        } catch (Exception e) {
//            System.out.println(e + " => Singlecopy");
//        }
//        return null;
//    }
