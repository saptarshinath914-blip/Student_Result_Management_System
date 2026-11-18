/**
 * Semester.java
 * Stores the subjects (name and code) of ONE semester of B.Tech CSE
 * (MAKAUT syllabus, effective 2023-24).
 * The static method getSemester() uses a switch-case to return the
 * correct semester, so the program can show the right subjects.
 */
public class Semester {

    private int semesterNo;
    private String[] subjectNames;
    private String[] subjectCodes;

    // Constructor
    public Semester(int semesterNo, String[] subjectNames, String[] subjectCodes) {
        this.semesterNo = semesterNo;
        this.subjectNames = subjectNames;
        this.subjectCodes = subjectCodes;
    }

    // switch-case: returns the Semester object for the chosen semester number
    public static Semester getSemester(int no) {
        switch (no) {
            case 1:
                return new Semester(1,
                    new String[] {
                        "Mathematics - IA",
                        "Physics-I",
                        "Basic Electrical Engineering",
                        "Physics-I Laboratory",
                        "Basic Electrical Engineering Lab",
                        "Workshop"},
                    new String[] {"BS-M101", "BS-PH101", "ES-EE101", "BS-PH191", "ES-EE191", "ES-ME192"});
            case 2:
                return new Semester(2,
                    new String[] {
                        "Chemistry-I",
                        "Mathematics - IIA",
                        "Programming for Problem Solving",
                        "English",
                        "Chemistry-I Laboratory",
                        "Programming for Problem Solving (Practical)",
                        "Engineering Graphics & Design",
                        "Language Laboratory"},
                    new String[] {"BS-CH201", "BS-M201", "ES-CS201", "HM-HU201", "BS-CH291", "ES-CS291", "ES-ME291", "HM-HU291"});
            case 3:
                return new Semester(3,
                    new String[] {
                        "Analog and Digital Electronics",
                        "Data Structure & Algorithms",
                        "Computer Organisation",
                        "Mathematics-III",
                        "Economics for Engineers (Humanities-II)",
                        "Analog and Digital Electronics Lab",
                        "Data Structure & Algorithm Lab",
                        "Computer Organization Lab",
                        "IT Workshop"},
                    new String[] {"ESC301", "PCC-CS301", "PCC-CS302", "BSC301", "HSMC301", "ESC391", "PCC-CS391", "PCC-CS392", "PCC-CS393"});
            case 4:
                return new Semester(4,
                    new String[] {
                        "Discrete Mathematics",
                        "Computer Architecture",
                        "Formal Language & Automata Theory",
                        "Design and Analysis of Algorithms",
                        "Biology",
                        "Environmental Sciences",
                        "Computer Architecture Lab",
                        "Design & Analysis Algorithm Lab"},
                    new String[] {"PCC-CS401", "PCC-CS402", "PCC-CS403", "PCC-CS404", "BSC401", "MC-401", "PCC-CS492", "PCC-CS494"});
            case 5:
                return new Semester(5,
                    new String[] {
                        "Software Engineering",
                        "Compiler Design",
                        "Operating Systems",
                        "Object Oriented Programming",
                        "Introduction to Industrial Management",
                        "Artificial Intelligence",
                        "Constitution of India",
                        "Software Engineering Lab",
                        "Operating System Lab",
                        "Object Oriented Programming Lab"},
                    new String[] {"ESC501", "PCC-CS501", "PCC-CS502", "PCC-CS503", "HSMC-501", "PEC-IT501B", "MC-CS501", "ESC591", "PCC-CS592", "PCC-CS593"});
            case 6:
                return new Semester(6,
                    new String[] {
                        "Database Management Systems",
                        "Computer Networks",
                        "Distributed Systems",
                        "Image Processing",
                        "Pattern Recognition",
                        "Numerical Methods",
                        "Research Methodology",
                        "Database Management System Lab",
                        "Computer Networks Lab"},
                    new String[] {"PCC-CS601", "PCC-CS602", "PEC-IT601B", "PEC-IT601D", "PEC-IT602D", "OEC-IT601A", "PROJ-CS601", "PCC-CS691", "PCC-CS692"});
            case 7:
                return new Semester(7,
                    new String[] {
                        "Machine Learning",
                        "Soft Computing",
                        "Adhoc - Sensor Network",
                        "Operation Research",
                        "Multimedia Technology",
                        "Project Management and Entrepreneurship",
                        "Project-II"},
                    new String[] {"PEC-CS701E", "PEC-CS702B", "PEC-CS702C", "OEC-CS701A", "OEC-CS701B", "HSMC701", "PROJ-CS781"});
            case 8:
                return new Semester(8,
                    new String[] {
                        "Cryptography and Network Security",
                        "Internet of Things",
                        "Big Data Analytics",
                        "Mobile Computing",
                        "E-Commerce & ERP",
                        "Project-III"},
                    new String[] {"PEC-CS801B", "PEC-CS801E", "OEC-CS801A", "OEC-CS801C", "OEC-CS802A", "PROJ-CS881"});
            default:
                return null;
        }
    }

    // ---------- Getters ----------
    public int getSemesterNo()          { return semesterNo; }
    public int getSubjectCount()        { return subjectNames.length; }
    public String getSubjectName(int i) { return subjectNames[i]; }
    public String getSubjectCode(int i) { return subjectCodes[i]; }

    // Prints the list of subjects of this semester
    public void displaySubjects() {
        System.out.println("\nSemester " + semesterNo + " - " + subjectNames.length + " subjects:");
        for (int i = 0; i < subjectNames.length; i++) {
            System.out.printf("  %2d. %-11s %s%n", (i + 1), subjectCodes[i], subjectNames[i]);
        }
    }
}
