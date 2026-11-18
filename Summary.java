/**
 * Summary.java
 * Holds the overall (class-level) result of one semester.
 * Used by both the console program and the web pages.
 * Demonstrates: encapsulation (private data, public getters).
 */
public class Summary {

    private int semesterNo;
    private int count;
    private int passCount;
    private double average;
    private Student topper;
    private Student lowest;

    public Summary(int semesterNo, int count, int passCount, double average,
                   Student topper, Student lowest) {
        this.semesterNo = semesterNo;
        this.count = count;
        this.passCount = passCount;
        this.average = average;
        this.topper = topper;
        this.lowest = lowest;
    }

    public int getSemesterNo()        { return semesterNo; }
    public int getCount()             { return count; }
    public int getPassCount()         { return passCount; }
    public int getFailCount()         { return count - passCount; }
    public double getPassPercentage() { return passCount * 100.0 / count; }
    public double getAverage()        { return average; }
    public Student getTopper()        { return topper; }
    public Student getLowest()        { return lowest; }
}
