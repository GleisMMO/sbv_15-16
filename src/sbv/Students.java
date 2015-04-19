package sbv;

import java.util.ArrayList;
//import java.sql.Date;

public class Students {
    
    //TODO edit SQL statements
    public static ArrayList<String> StudentsList( String StudentId, int index){
        try{
            return Query.anyQuery("SELECT forename, surname, birth, name FROM sbm_students, sbm_classes, `sbm_students-classes` WHERE sbm_students.ID LIKE student_ID AND class_ID LIKE sbm_classes.ID");   
        }catch(Exception e){System.out.println(e);}
        return null;
    }
    
   //Schüler informationen abhängig vom der schülerID und einem index 
    public static String SingelStudent( String StudentId, int index){
        try{
            ArrayList<String> result = Query.anyQuery("SELECT ID, forename, surname, birth, FROM sbm_students WHERE ID LIKE " + StudentId); 
            return result.get(index);     
        }catch(Exception e){System.out.println(e);}
        return null;
    }
        
    //edits Student 
    public static void editStudent(int ID, String forename, String surename,String birth){ //birth might bug needs testing
        try{
            Query.anyUpdate("UPDATE `sbm_students` SET forename =" + forename + ", surename = "+ surename + ", birth = "+birth + "WHERE ID LIKE"+ ID );
        }catch(Exception e){System.out.println(e);}
    }
    
    //creates new student
    public static void newStudent(String forename, String surename,String birth){
        try{
            Query.anyUpdate("INSERT INTO `sbm_students` SET forename =" + forename + ", surename = "+ surename + ", birth = "+birth );
        }catch(Exception e){System.out.println(e);}       
    }    
}
