import java.util.ArrayList;

/**
 * ResultManager.java
 * Manages the collection of Student objects (add, search, display, summary).
 * Demonstrates: collections (ArrayList of objects), methods, loops.
 */
public class ResultManager {

    private ArrayList<Student> students = new ArrayList<>();

    // Adds a result; returns false if this roll number already has a result for this semester
    public boolean addStudent(Student s) {
        if (searchByRollAndSemester(s.getRollNo(), s.getSemesterNo()) != null) {
            return false;
        }
        students.add(s);
        return true;
    }

    // Finds the result of one roll number in one semester (null if not found)
    public Student searchByRollAndSemester(int roll, int semesterNo) {
        for (Student s : students) {
            if (s.getRollNo() == roll && s.getSemesterNo() == semesterNo) {
                return s;
            }
        }
        return null;
    }

    // Returns the results of a roll number in ALL semesters (empty list if none)
    public ArrayList<Student> searchByRoll(int roll) {
        ArrayList<Student> found = new ArrayList<>();
        for (Student s : students) {
            if (s.getRollNo() == roll) {
                found.add(s);
            }
        }
        return found;
    }

    // Returns the name already registered for a roll number (null if new student)
    public String findName(int roll) {
        for (Student s : students) {
            if (s.getRollNo() == roll) {
                return s.getName();
            }
        }
        return null;
    }

    public int getCount() {
        return students.size();
    }

    // Displays all results (all semesters) in a table
    public void displayAll() {
        if (students.isEmpty()) {
            System.out.println("No student records found.");
            return;
        }
        System.out.println("\n----------------------- ALL STUDENT RESULTS -----------------------");
        System.out.printf("%-8s %-18s %-4s %-10s %-11s %-6s %-6s%n",
                "Roll No", "Name", "Sem", "Total", "Percentage", "Grade", "Result");
        System.out.println("-------------------------------------------------------------------");
        for (Student s : students) {
            s.displayRow();
        }
        System.out.println("-------------------------------------------------------------------");
    }

    // Returns a copy of the list of all results (used by the web pages)
    public ArrayList<Student> getAllStudents() {
        return new ArrayList<>(students);
    }

    // Calculates the overall (class-level) result of ONE semester; null if no results
    public Summary getSummary(int semesterNo) {
        int count = 0, passCount = 0;
        double sumPercentage = 0;
        Student topper = null;
        Student lowest = null;

        for (Student s : students) {
            if (s.getSemesterNo() != semesterNo) continue;   // skip other semesters
            count++;
            if (s.isPassed()) passCount++;
            sumPercentage += s.getPercentage();
            if (topper == null || s.getPercentage() > topper.getPercentage()) topper = s;
            if (lowest == null || s.getPercentage() < lowest.getPercentage()) lowest = s;
        }
        if (count == 0) {
            return null;
        }
        return new Summary(semesterNo, count, passCount, sumPercentage / count, topper, lowest);
    }

    // Prints the overall result of ONE semester on the console
    public void displaySummary(int semesterNo) {
        Summary sm = getSummary(semesterNo);
        if (sm == null) {
            System.out.println("No records found for Semester " + semesterNo + ".");
            return;
        }

        System.out.println("\n======= OVERALL CLASS RESULT - SEMESTER " + semesterNo + " =======");
        System.out.println("Total students     : " + sm.getCount());
        System.out.println("Passed             : " + sm.getPassCount());
        System.out.println("Failed             : " + sm.getFailCount());
        System.out.printf("Pass percentage    : %.2f %%%n", sm.getPassPercentage());
        System.out.printf("Class average      : %.2f %%%n", sm.getAverage());
        System.out.printf("Topper             : %s (Roll %d) - %.2f %%%n",
                sm.getTopper().getName(), sm.getTopper().getRollNo(), sm.getTopper().getPercentage());
        System.out.printf("Lowest scorer      : %s (Roll %d) - %.2f %%%n",
                sm.getLowest().getName(), sm.getLowest().getRollNo(), sm.getLowest().getPercentage());
        System.out.println("===========================================");
    }
}
