package sbv;

import java.util.ArrayList;
import java.util.logging.Level;
import static sbv.Sbv.logger;
//import java.sql.Date;

public class Students {

    //Schüler informationen abhängig vom der schülerID und einem index 
    public static String SingelStudent(String StudentId, int index) {
        try {
            final ArrayList<String> result = Query.anyQuery("SELECT ID, forename, surname, birth "
                    + "FROM `sbm_students` "
                    + "WHERE ID = '" + StudentId + "'");
            return result.get(index);
        } catch (Exception e) {
            System.out.println(e + " => SingelStudent");
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}'' and ''{2}''", new Object[]{e, StudentId, index});
        }
        return null;
    }

    //booklist of a student
    public static ArrayList<String> BookList(String student_id) {
        try {
            return Query.anyQuery("SELECT label, buy, distributed, paid, sbm_copies.ID "
                    + "FROM sbm_copieshistory , sbm_books, sbm_copies "
                    + "WHERE sbm_books.ID LIKE book_id "
                    + "AND sbm_copies.ID LIKE copy_ID "
                    + "AND student_ID LIKE " + student_id);
        } catch (Exception e) {
            System.out.println(e + " => BookList");
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}''", new Object[]{e, student_id});
        }
        return null;
    }

    //copies total
    public static String copiesCount(String StudentId) {
        try {
            final String result = Query.getString("SELECT COUNT(sbm_copieshistory.ID) "
                    + "FROM `sbm_copieshistory`, `sbm_copies`, `sbm_books` "
                    + "WHERE student_id LIKE '" + StudentId + "' "
                    + "AND sbm_copies.book_id LIKE sbm_books.ID "
                    + "AND sbm_copieshistory.copy_id LIKE sbm_copies.ID "
                    + "GROUP BY student_id", 
                    "COUNT(sbm_copieshistory.ID)");
            if (result.isEmpty()) {
                return "0";
            }
            return result;
        } catch (Exception e) {
            System.out.println(e + " => CopiesToReturn");
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}''", new Object[]{e, StudentId});
        }
        return "0";
    }
    
    //copies to return
    public static String CopiesToReturn(String StudentId) {
        try {
            final String result = Query.getString("SELECT COUNT(sbm_copieshistory.ID) "
                    + "FROM `sbm_copieshistory`, `sbm_copies`, `sbm_books` "
                    + "WHERE buy LIKE '0' "
                    + "AND student_id LIKE '" + StudentId + "' "
                    + "AND sbm_copies.book_id LIKE sbm_books.ID "
                    + "AND sbm_copieshistory.copy_id LIKE sbm_copies.ID "
                    + "GROUP BY student_id", 
                    "COUNT(sbm_copieshistory.ID)");
            if (result.isEmpty()) {
                return "0";
            }
            return result;
            //cut
            /*return Query.getString("SELECT COUNT(sbm_copieshistory.ID) "
                    + "FROM sbm_copieshistory, sbm_books, sbm_copies "
                    + "WHERE sbm_copies.ID = sbm_copieshistory.copy_id "
                    + "AND sbm_copies.book_id = sbm_books.ID "
                    + "AND collected LIKE '' "
                    + "AND buy = 0", 
                    "COUNT(sbm_copieshistory.ID)");*/
            //
        } catch (Exception e) {
            System.out.println(e + " => CopiesToReturn");
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}''", new Object[]{e, StudentId});
        }
        return "0";
    }

    public static ArrayList<String> SingelStudentClasses(String StudentId) {
        try {
            return Query.anyQuery("SELECT sbm_classes.name "
                    + "FROM sbm_classes, `sbm_students-classes` "
                    + "WHERE `sbm_students-classes`.student_ID = " + StudentId + " "
                    + "AND class_ID LIKE sbm_classes.ID");
        } catch (Exception e) {
            System.out.println(e + " => SingelStudentClasses");
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}''", new Object[]{e, StudentId});
        }
        return null;
    }

    public static void addToClass(String student_id, String className) {
        try {
            String class_id = Query.getString("SELECT ID "
                    + "FROM sbm_classes "
                    + "WHERE name LIKE '" + className + "'"
                    , "ID");
            Query.anyUpdate("INSERT INTO `sbm_students-classes` "
                    + "SET class_id =" + class_id + ", "
                    + "student_id = " + student_id);
            logger.log(Level.INFO, "added student {0} to class {1}", new Object[]{student_id, class_id});
        } catch (Exception e) {
            System.out.println(e + " => addToClass");
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}'' and ''{2}''", new Object[]{e, student_id, className});
        }
    }
    
    public static void removeFromClass(String student_id, String className) {
        try {
            String class_id = Query.getString("SELECT ID "
                    + "FROM sbm_classes "
                    + "WHERE name LIKE '" + className + "'"
                    , "ID");
            Query.anyUpdate("DELETE FROM `sbm_students-classes` "
                    + "WHERE class_id = " + class_id + " "
                    + "AND student_id = " + student_id);
            logger.log(Level.INFO, "remove student {0} from class {1}", new Object[]{student_id, class_id});
        } catch (Exception e) {
            System.out.println(e + " => removeFromClass");
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}'' and ''{2}''", new Object[]{e, student_id, className});
        }
    }
    
    public static String StudentSearch(String Student) {
        final String names[] = Student.split(" ");
        try {
            return Query.anyQuery("SELECT ID "
                    + "FROM `sbm_students` "
                    + "WHERE forename LIKE '" + names[0] + "%'"
                    + "AND surname LIKE '%" + names[names.length-1] + "'").get(0);
        } catch (Exception e) {
            System.out.println(e + " => StudentSearch");
            logger.log(Level.WARNING, "Exception ''{0}'' from ''{1}''", new Object[]{e, Student});
        }
        return null;
    }
}


    //All students
//    public static ArrayList<String> StudentsList() {
//        try {
//            return Query.anyQuery("SELECT forename, surname, birth "
//                    + "FROM sbm_students");
//        } catch (Exception e) {
//            System.out.println(e + " => StudentsList");
//        }
//        return null;
//    }

    //edits Student 
//    public static void editStudent(int ID, String forename, String surename, String birth) { //birth might bug needs testing
//        try {
//            Query.anyUpdate("UPDATE `sbm_students` "
//                    + "SET forename =" + forename + ", "
//                    + "surename = " + surename + ", "
//                    + "birth = " + birth + ""
//                    + "WHERE ID LIKE" + ID);
//        } catch (Exception e) {
//            System.out.println(e + " => editStudent");
//        }
//    }

    //creates new student
//    public static void newStudent(String forename, String surename, String birth) {
//        try {
//            Query.anyUpdate("INSERT INTO `sbm_students` "
//                    + "SET forename = '" + forename + "', "
//                    + "surname = '" + surename + "', "
//                    + "birth = '" + birth + "', "
//                    + "class = 'nope', "
//                    + "img = 'nope'");
//        } catch (Exception e) {
//            System.out.println(e + " => newStudent");
//        }
//    }

    //checks if student has a copy of a book
//    public static boolean[] BookGroupListCheck(String class_id, String student_id) {
//        boolean results[] = null;
//        ArrayList<String> books = BookGroups.getBookIds(class_id);
//        ArrayList<String> students = BookGroups.getStudentIds(class_id);
//        boolean stahp = true;
//
//        try {
//            for (int i = 0; i < students.size(); i++) {
//                ArrayList<String> studentsBooks = Students.BookList(students.get(i));
//                for (int o = 0; o < books.size(); o++) {
//                    for (int k = 0; k < studentsBooks.size(); k++) {
//                        do {
//                            if (books.get(o).equals(studentsBooks.get(k))) {
//                                results[o] = true;
//                            } else {
//                                results[o] = false;
//                            }
//                            if (k == studentsBooks.size()) {
//                                stahp = false;
//                            }
//                        } while (results[o] || stahp == false);
//                    }
//                }
//            }
//            return results;
//        } catch (Exception e) {
//            System.out.println(e + " => BookGroupListCheck");
//        }
//        return null;
//    }

//    public static void moveToClass(String student_id, String class_id, String new_class_id) {
//        try {
//            Query.anyUpdate("UPDATE `sbm_students-clases` "
//                    + "SET class_id =" + new_class_id + ", "
//                    + "student_id = " + student_id + ""
//                    + "WHERE student_id LIKE " + student_id + ""
//                    + "AND class_id LIKE " + class_id);
//        } catch (Exception e) {
//            System.out.println(e + " => newStudent");
//        }
//    }