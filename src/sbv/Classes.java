package sbv;

import java.util.ArrayList;
import java.util.logging.Level;
import static sbv.Sbv.logger;

public class Classes {

    public static ArrayList<String> studentIDList(String ID) {
        try {
            return Query.anyQuery("SELECT student_ID "
                    + "FROM  `sbm_students`, `sbm_students-classes`, `sbm_classes` "
                    + "WHERE sbm_classes.ID LIKE class_ID "
                    + "AND student_ID lIKE sbm_students.ID "
                    + "AND name lIKE '" + ID + "'");
        } catch (Exception e) {
            System.out.println(e + " => studentIDList");
        }
        return null;
    }

    //gets Class name
    public static String getClassName(String ID) {
        try {
            return Query.getString("SELECT name "
                    + "FROM sbm_classes "
                    + "WHERE ID LIKE " + ID, 
                    "name");
        } catch (Exception e) {
            System.out.println(e + " => getClassName");
        }
        return null;
    }
    
    //gets name of all Classes
    public static ArrayList<String> getClassNameList() {
        try {
            return Query.anyQuery("SELECT name "
                    + "FROM sbm_classes "
                    + "ORDER BY name");
        } catch (Exception e) {
            System.out.println(e + " => getClassNameList");
        }
        return null;
    }

    public static ArrayList<String> getClassIDs() {
        try {
            return Query.anyQuery("SELECT ID "
                    + "FROM sbm_classes");
        } catch (Exception e) {
            System.out.println(e + " => getClassIDs");
        }
        return null;

    }

    //all students in a class
    public static ArrayList<String> classList(String name) {
        try {
            return Query.anyQuery("SELECT forename, surname, birth, student_ID "
                    + "FROM  `sbm_students`, `sbm_students-classes`, `sbm_classes` "
                    + "WHERE sbm_classes.ID LIKE class_ID "
                    + "AND student_ID lIKE sbm_students.ID "
                    + "AND name LIKE '" + name + "' "
                    + "ORDER BY surname");
        } catch (Exception e) {
            System.out.println(e + " => classList");
        }
        return null;
    }

    //all studentsnames in a class
    public static ArrayList<String> classListNames(String name) {
        try {
            return Query.anyQuery("SELECT forename, surname "
                    + "FROM  `sbm_students`, `sbm_students-classes`, `sbm_classes` "
                    + "WHERE sbm_classes.ID LIKE class_ID "
                    + "AND student_ID lIKE sbm_students.ID "
                    + "AND name LIKE '" + name + "' "
                    + "ORDER BY surname");
        } catch (Exception e) {
            System.out.println(e + " => classListNames");
        }
        return null;
    }
    
    //adds new class
    public static void newClass(String name) {
        try {
            Query.anyUpdate("INSERT INTO `sbm_classes` "
                    + "SET name = '" + name + "'");
            logger.log(Level.INFO, "created new class ''{0}''", name);
        } catch (Exception e) {
            System.out.println(e + " => newClass");
        }
    }
    
}
//
//    public static String classSearch(String classe) {
//        try {
//            return Query.anyQuery("SELECT ID "
//                    + "FROM `sbm_classes` "
//                    + "WHERE name LIKE '" + classe + "'").get(0);
//        } catch (Exception e) {
//            System.out.println(e + " => classSearch");
//        }
//        return null;
//    }

    //arrayList of all classes with student count
//    public static ArrayList<String> classesList() {
//        try {
//            return Query.anyQuery("SELECT name, COUNT(student_ID) "
//                    + "FROM `sbm_classes`, `sbm_students-classes` "
//                    + "WHERE sbm_classes.ID LIKE class_ID "
//                    + "GROUP BY class_ID");
//        } catch (Exception e) {
//            System.out.println(e + " => classesList");
//        }
//        return null;
//    }

    //edits class
//    public static void editClass(String ID, String name) {
//        try {
//            Query.anyUpdate("UPDATE `sbm_classes` "
//                    + "SET name = " + name + " "
//                    + "WHERE ID LIKE " + ID);
//        } catch (Exception e) {
//            System.out.println(e + " => editClass");
//        }
//    }