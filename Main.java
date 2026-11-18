import java.util.ArrayList;
import java.util.Scanner;

/**
 * Main.java
 * Menu-driven console program: takes user input and calls the other classes.
 * Demonstrates: objects, conditional statements (switch / if), loops, validation.
 */
public class Main {

    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        ResultManager manager = new ResultManager();
        int choice;

        System.out.println("*****************************************");
        System.out.println("     STUDENT RESULT MANAGEMENT SYSTEM    ");
        System.out.println("*****************************************");

        do {
            showMenu();
            choice = readInt("Enter your choice (1-5): ", 1, 5);

            switch (choice) {
                case 1: addStudent(manager);      break;
                case 2: searchStudent(manager);   break;
                case 3: manager.displayAll();     break;
                case 4: showSummary(manager);     break;
                case 5: System.out.println("Thank you for using the system. Goodbye!"); break;
            }
        } while (choice != 5);
    }

    private static void showMenu() {
        System.out.println("\n----------------- MENU -----------------");
        System.out.println("1. Add a new student record");
        System.out.println("2. Search student by roll number");
        System.out.println("3. Display all student results");
        System.out.println("4. Show overall class result (semester-wise)");
        System.out.println("5. Exit");
        System.out.println("----------------------------------------");
    }

    // Asks the user to choose a semester and returns the matching Semester object
    private static Semester chooseSemester() {
        System.out.println("\n--------- SELECT SEMESTER ---------");
        for (int i = 1; i <= 8; i++) {
            System.out.println(i + ". Semester " + i);
        }
        System.out.println("-----------------------------------");
        int choice = readInt("Choose semester (1-8): ", 1, 8);
        return Semester.getSemester(choice);      // uses switch-case inside Semester
    }

    // Reads and validates one student's data for one semester, then stores the object
    private static void addStudent(ResultManager manager) {
        int roll = readInt("Enter Roll Number (1-99999): ", 1, 99999);

        // If this roll number is already registered, reuse the same name
        String name = manager.findName(roll);
        if (name != null) {
            System.out.println("Student already registered: " + name);
        } else {
            while (true) {
                System.out.print("Enter Student Name: ");
                name = sc.nextLine().trim();
                if (Student.isValidName(name)) break;
                System.out.println("Invalid name! Use only letters and spaces (min 2 characters).");
            }
        }

        // Semester selection BEFORE entering marks
        Semester sem = chooseSemester();

        if (manager.searchByRollAndSemester(roll, sem.getSemesterNo()) != null) {
            System.out.println("Error: Roll number " + roll + " already has a result for Semester "
                    + sem.getSemesterNo() + ".");
            return;
        }

        // Show the subjects of the chosen semester, then read marks one by one
        sem.displaySubjects();
        System.out.println("\nEnter marks (0-100) for each subject:");
        int[] marks = new int[sem.getSubjectCount()];
        for (int i = 0; i < marks.length; i++) {
            marks[i] = readInt("  " + sem.getSubjectCode(i) + " " + sem.getSubjectName(i) + ": ", 0, 100);
        }

        Student s = new Student(roll, name, sem, marks);
        manager.addStudent(s);
        System.out.println("Student record added successfully!");
    }

    // Shows the result card of a roll number for every semester it has
    private static void searchStudent(ResultManager manager) {
        int roll = readInt("Enter Roll Number to search: ", 1, 99999);
        ArrayList<Student> results = manager.searchByRoll(roll);
        if (results.isEmpty()) {
            System.out.println("No student found with roll number " + roll + ".");
        } else {
            for (Student s : results) {
                s.displayResult();
            }
        }
    }

    // Overall class result of one chosen semester
    private static void showSummary(ResultManager manager) {
        Semester sem = chooseSemester();
        manager.displaySummary(sem.getSemesterNo());
    }

    // Keeps asking until the user enters a whole number within [min, max]
    private static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Invalid input! Enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a whole number.");
            }
        }
    }
}
