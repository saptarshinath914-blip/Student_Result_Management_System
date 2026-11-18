/**
 * Student.java
 * Represents one student's result for ONE semester: personal details,
 * subject-wise marks and the calculated result (total, percentage,
 * grade, pass/fail status).
 * Demonstrates: class, object, constructor, encapsulation, methods,
 * arrays, conditional statements and association (Student has a Semester).
 */
public class Student {

    // Constants (same for every student)
    public static final int MAX_MARKS = 100;   // maximum marks per subject
    public static final int PASS_MARKS = 40;   // minimum marks to pass a subject

    // Encapsulation: all data members are private
    private int rollNo;
    private String name;
    private Semester semester;     // the semester whose subjects are used
    private int[] marks;           // marks[i] belongs to subject i of the semester
    private int total;
    private double percentage;
    private String grade;
    private String status;

    // Constructor: creates a student result and calculates it immediately
    public Student(int rollNo, String name, Semester semester, int[] marks) {
        this.rollNo = rollNo;
        this.name = name;
        this.semester = semester;
        this.marks = marks;
        calculateResult();
    }

    // Calculates total, percentage, status and grade
    private void calculateResult() {
        total = 0;
        boolean failedAnySubject = false;

        for (int i = 0; i < marks.length; i++) {
            total += marks[i];
            if (marks[i] < PASS_MARKS) {
                failedAnySubject = true;
            }
        }

        percentage = (total * 100.0) / getMaxTotal();
        status = failedAnySubject ? "FAIL" : "PASS";
        grade = failedAnySubject ? "F" : findGrade(percentage);
    }

    // Assigns grade from percentage using conditional statements
    private String findGrade(double p) {
        if (p >= 90)      return "O";
        else if (p >= 80) return "E";
        else if (p >= 70) return "A";
        else if (p >= 60) return "B";
        else if (p >= 50) return "C";
        else if (p >= 40) return "D";
        else              return "F";
    }

    // ---------- Validation methods (static: no object needed) ----------
    public static boolean isValidRollNo(int roll) {
        return roll > 0 && roll <= 99999;
    }

    public static boolean isValidName(String name) {
        // only letters and spaces, at least 2 characters
        return name != null && name.trim().length() >= 2
                && name.matches("[A-Za-z ]+");
    }

    public static boolean isValidMark(int mark) {
        return mark >= 0 && mark <= MAX_MARKS;
    }

    // ---------- Getters (read-only access to private data) ----------
    public int getRollNo()          { return rollNo; }
    public String getName()         { return name; }
    public int getSemesterNo()      { return semester.getSemesterNo(); }
    public Semester getSemester()   { return semester; }
    public int getMark(int i)       { return marks[i]; }
    public int getTotal()           { return total; }
    public int getMaxTotal()        { return semester.getSubjectCount() * MAX_MARKS; }
    public double getPercentage()   { return percentage; }
    public String getGrade()        { return grade; }
    public String getStatus()       { return status; }
    public boolean isPassed()       { return status.equals("PASS"); }

    // Prints the full individual result card (semester-wise)
    public void displayResult() {
        System.out.println("\n=================== RESULT CARD - SEMESTER " + semester.getSemesterNo() + " ===================");
        System.out.println("Roll No    : " + rollNo);
        System.out.println("Name       : " + name);
        System.out.println("Semester   : " + semester.getSemesterNo());
        System.out.println("--------------------------------------------------------------");
        System.out.printf("%-11s %-42s %7s%n", "Code", "Subject", "Marks");
        for (int i = 0; i < marks.length; i++) {
            System.out.printf("%-11s %-42s %4d/%d%n",
                    semester.getSubjectCode(i), semester.getSubjectName(i), marks[i], MAX_MARKS);
        }
        System.out.println("--------------------------------------------------------------");
        System.out.println("Total      : " + total + " / " + getMaxTotal());
        System.out.printf("Percentage : %.2f %%%n", percentage);
        System.out.println("Grade      : " + grade);
        System.out.println("Result     : " + status);
        System.out.println("==============================================================");
    }

    // Prints one row for the "all students" table
    public void displayRow() {
        System.out.printf("%-8d %-18s %-4d %-10s %-11.2f %-6s %-6s%n",
                rollNo, name, semester.getSemesterNo(), total + "/" + getMaxTotal(),
                percentage, grade, status);
    }
}
