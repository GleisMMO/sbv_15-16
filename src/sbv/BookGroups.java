package sbv;

import java.util.ArrayList;

public class BookGroups {

    //shows books inside a group
    public static ArrayList<String> BooksList(String class_id) {
        try {
            return Query.anyQuery("SELECT label, isbn "
                    + "FROM `sbm_classes-books`, `sbm_books` "
                    + "WHERE sbm_books.ID LIKE book_id "
                    + "AND class_id LIKE '" + class_id + "'");
        } catch (Exception e) {
            System.out.println(e + " => AddRecord");
        }
        return null;
    }

    public static ArrayList<String> getBookIds(String class_id) {
        try {
            return Query.anyQuery("SELECT sbm_books.ID "
                    + "FROM `sbm_classes-books`, `sbm_books` "
                    + "WHERE sbm_books.ID LIKE book_id "
                    + "AND class_id LIKE " + class_id);
        } catch (Exception e) {
            System.out.println(e + " => BooksList");
        }
        return null;
    }

    public static ArrayList<String> getStudentIds(String class_id) {
        try {
            return Query.anyQuery("SELECT sbm_students.ID "
                    + "FROM `sbm_students`, `sbm_students-classes`, `sbm_classes` "
                    + "WHERE sbm_classes.class_id LIKE '" + class_id + "' "
                    + "AND student_ID lIKE sbm_students.ID");
        } catch (Exception e) {
            System.out.println(e + " => getStudentIds");
        }
        return null;
    }
}


    //adds record
//    public static void AddRecord(String book_id, String class_id) {
//        try {
//            Query.anyUpdate("INSERT INTO `sbm_classes-books` "
//                    + "SET book_id= " + book_id + " , "
//                    + "class_id = " + class_id);
//        } catch (Exception e) {
//            System.out.println(e + " => AddRecord");
//        }
//    }

    //deletes record
//    public static void DeleteRecord(String ID) {
//        try {
//            Query.anyUpdate("DELETE FROM `sbm_classes-books` "
//                    + "WHERE ID LIKE " + ID);
//        } catch (Exception e) {
//            System.out.println(e + " => AddRecord");
//        }
//    }