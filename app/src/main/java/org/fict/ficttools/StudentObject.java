package org.fict.ficttools;

import java.util.Comparator;

public class StudentObject {
    private String name;
    private String course;
    private String code;
    private int id;

    public StudentObject(String name, String course, String code, int id) {
        this.name = name;
        this.course = course;
        this.code = code;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getCourse() {
        return course;
    }

    public String getCode() {
        return code;
    }

    public int getId() {
        return id;
    }
    public static Comparator<StudentObject> bySection = new Comparator<StudentObject>() {

        public int compare(StudentObject s1, StudentObject s2) {
            String StudentName1 = s1.getCourse().toUpperCase();
            String StudentName2 = s2.getCourse().toUpperCase();

            //ascending order
            return StudentName1.compareTo(StudentName2);

            //descending order
            //return StudentName2.compareTo(StudentName1);
        }};
}




