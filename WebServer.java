import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * WebServer.java
 * Web version of the Student Result Management System, written in pure Java.
 * It uses the HTTP server that is built into the JDK (no framework, no library)
 * and re-uses the same classes as the console version:
 * Student, Semester, ResultManager and Summary.
 *
 * Run:  java WebServer      then open  http://localhost:8080
 * The port can be changed with the PORT environment variable (used by hosting sites).
 */
public class WebServer {

    // One manager object keeps all results in memory (same as the console program)
    private static final ResultManager manager = new ResultManager();

    // What we send back to the browser: a page, a redirect or plain text
    private static class Response {
        int status = 200;
        String contentType = "text/html; charset=UTF-8";
        String body = "";
        String location = null;

        static Response html(String body) {
            Response r = new Response();
            r.body = body;
            return r;
        }

        static Response text(String body) {
            Response r = new Response();
            r.contentType = "text/plain; charset=UTF-8";
            r.body = body;
            return r;
        }

        static Response redirect(String location) {
            Response r = new Response();
            r.status = 303;
            r.location = location;
            return r;
        }

        static Response error(String message, String backHref) {
            return html(Html.error(message, backHref));
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 8080;
        String env = System.getenv("PORT");               // hosting sites give the port here
        if (env != null && !env.trim().isEmpty()) {
            try {
                port = Integer.parseInt(env.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid PORT value, using 8080.");
            }
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", exchange -> handle(exchange));
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        System.out.println("Student Result Management System is running on port " + port);
        System.out.println("Open http://localhost:" + port + " in your browser.");
    }

    // ---------------------------------------------------------------- request handling
    private static void handle(HttpExchange ex) throws IOException {
        Response res;
        try {
            String path = ex.getRequestURI().getPath();
            boolean post = ex.getRequestMethod().equalsIgnoreCase("POST");
            Map<String, String> query = parseParams(ex.getRequestURI().getRawQuery());
            Map<String, String> form = post ? parseParams(readBody(ex)) : new HashMap<String, String>();
            synchronized (manager) {                      // only one request changes data at a time
                res = route(path, post, query, form);
            }
        } catch (Exception e) {
            e.printStackTrace();
            res = Response.error("Something went wrong. Please go back and try again.", "/");
            res.status = 500;
        }
        send(ex, res);
    }

    // switch-case on the page address
    private static Response route(String path, boolean post, Map<String, String> query, Map<String, String> form) {
        switch (path) {
            case "/":          return homePage();
            case "/add":       return addForm();
            case "/add/marks": return post ? marksForm(form) : Response.redirect("/add");
            case "/add/save":  return post ? saveResult(form) : Response.redirect("/add");
            case "/search":    return searchPage(query);
            case "/all":       return allPage();
            case "/summary":   return summaryPage(query);
            case "/sample":
                if (post) {
                    loadSampleData();
                    return Response.redirect("/all");
                }
                return Response.redirect("/");
            case "/health":    return Response.text("OK");
            default:
                Response r = Response.error("Page not found.", "/");
                r.status = 404;
                return r;
        }
    }

    // ---------------------------------------------------------------- pages
    private static Response homePage() {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='card'><h2>Welcome</h2>");
        sb.append("<p>Add semester-wise marks of students and get the total, percentage, grade and pass/fail result ");
        sb.append("automatically. Choose a semester and the subjects of that semester (as per the MAKAUT B.Tech CSE ");
        sb.append("syllabus) are shown for entering marks.</p>");
        sb.append("<div class='stats'><div class='stat'><b>").append(manager.getCount())
          .append("</b>results stored</div>");
        sb.append("<div class='stat'><b>8</b>semesters</div>");
        sb.append("<div class='stat'><b>O to F</b>grade scale</div></div>");
        sb.append("<a class='btn' href='/add'>Add a Result</a> <a class='btn ghost' href='/all'>View All Results</a>");
        sb.append("<form method='post' action='/sample'><button class='ghost' type='submit'>Load sample data</button>");
        sb.append("<p class='hint'>Adds a few demo students so you can try Search and Class Summary quickly.</p></form>");
        sb.append("</div>");
        return Response.html(Html.page("Home", sb.toString()));
    }

    private static Response addForm() {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='card'><h2>Add a New Student Result</h2>");
        sb.append("<form method='post' action='/add/marks'>");
        sb.append("<label>Roll Number (1-99999)</label>");
        sb.append("<input type='number' name='roll' min='1' max='99999' required>");
        sb.append("<label>Student Name</label>");
        sb.append("<input type='text' name='name' pattern='[A-Za-z ]{2,}' placeholder='Letters and spaces only'>");
        sb.append("<p class='hint'>If this roll number is already registered, the saved name is used automatically.</p>");
        sb.append("<label>Semester</label><select name='semester'>");
        sb.append(semesterOptions(0));
        sb.append("</select>");
        sb.append("<button type='submit'>Next: Enter Marks</button></form></div>");
        return Response.html(Html.page("Add Result", sb.toString()));
    }

    // Step 2: validates roll/name/semester and shows the subjects of that semester
    private static Response marksForm(Map<String, String> form) {
        Integer roll = toInt(form.get("roll"));
        Integer sem = toInt(form.get("semester"));
        if (roll == null || !Student.isValidRollNo(roll)) {
            return Response.error("Roll number must be a whole number between 1 and 99999.", "/add");
        }
        if (sem == null || sem < 1 || sem > 8) {
            return Response.error("Please choose a semester between 1 and 8.", "/add");
        }
        String name = resolveName(roll, form.get("name"));
        if (name == null) {
            return Response.error("Invalid name! Use only letters and spaces (minimum 2 characters).", "/add");
        }
        if (manager.searchByRollAndSemester(roll, sem) != null) {
            return Response.error("Roll number " + roll + " already has a result for Semester " + sem + ".", "/add");
        }

        Semester semester = Semester.getSemester(sem);
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='card'><h2>Enter Marks - Semester ").append(sem).append("</h2>");
        sb.append("<p><b>Roll No:</b> ").append(roll).append(" &nbsp; <b>Name:</b> ").append(Html.esc(name)).append("</p>");
        sb.append("<form method='post' action='/add/save'>");
        sb.append("<input type='hidden' name='roll' value='").append(roll).append("'>");
        sb.append("<input type='hidden' name='name' value='").append(Html.esc(name)).append("'>");
        sb.append("<input type='hidden' name='semester' value='").append(sem).append("'>");
        sb.append("<div class='wrap'><table><tr><th>Code</th><th>Subject</th><th>Marks (0-100)</th></tr>");
        for (int i = 0; i < semester.getSubjectCount(); i++) {
            sb.append("<tr><td>").append(Html.esc(semester.getSubjectCode(i))).append("</td><td>")
              .append(Html.esc(semester.getSubjectName(i))).append("</td>");
            sb.append("<td><input type='number' name='m").append(i).append("' min='0' max='100' required></td></tr>");
        }
        sb.append("</table></div><button type='submit'>Save Result</button></form></div>");
        return Response.html(Html.page("Enter Marks", sb.toString()));
    }

    // Step 3: validates the marks, creates the Student object and stores it
    private static Response saveResult(Map<String, String> form) {
        Integer roll = toInt(form.get("roll"));
        Integer sem = toInt(form.get("semester"));
        if (roll == null || !Student.isValidRollNo(roll) || sem == null || sem < 1 || sem > 8) {
            return Response.error("Invalid roll number or semester.", "/add");
        }
        String name = resolveName(roll, form.get("name"));
        if (name == null) {
            return Response.error("Invalid name! Use only letters and spaces (minimum 2 characters).", "/add");
        }
        Semester semester = Semester.getSemester(sem);
        int[] marks = new int[semester.getSubjectCount()];
        for (int i = 0; i < marks.length; i++) {
            Integer m = toInt(form.get("m" + i));
            if (m == null || !Student.isValidMark(m)) {
                return Response.error("Marks of " + semester.getSubjectName(i)
                        + " must be a whole number between 0 and 100.", "/add");
            }
            marks[i] = m;
        }
        Student s = new Student(roll, name, semester, marks);
        if (!manager.addStudent(s)) {
            return Response.error("Roll number " + roll + " already has a result for Semester " + sem + ".", "/add");
        }
        return Response.redirect("/search?roll=" + roll);
    }

    private static Response searchPage(Map<String, String> query) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='card'><h2>Search Student by Roll Number</h2>");
        sb.append("<form method='get' action='/search'><label>Roll Number</label>");
        sb.append("<input type='number' name='roll' min='1' max='99999' required>");
        sb.append("<button type='submit'>Search</button></form></div>");

        String rollText = query.get("roll");
        if (rollText != null) {
            Integer roll = toInt(rollText);
            if (roll == null || !Student.isValidRollNo(roll)) {
                sb.append("<div class='card err'><p>Roll number must be a whole number between 1 and 99999.</p></div>");
            } else {
                ArrayList<Student> results = manager.searchByRoll(roll);
                if (results.isEmpty()) {
                    sb.append("<div class='card err'><p>No student found with roll number ").append(roll).append(".</p></div>");
                } else {
                    for (Student s : results) {
                        sb.append(resultCard(s));
                    }
                }
            }
        }
        return Response.html(Html.page("Search", sb.toString()));
    }

    private static Response allPage() {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='card'><h2>All Student Results</h2>");
        ArrayList<Student> all = manager.getAllStudents();
        if (all.isEmpty()) {
            sb.append("<p>No student records found.</p><a class='btn' href='/add'>Add a Result</a>");
        } else {
            sb.append("<div class='wrap'><table><tr><th class='n'>Roll No</th><th>Name</th><th class='n'>Sem</th>");
            sb.append("<th class='n'>Total</th><th class='n'>Percentage</th><th class='n'>Grade</th><th class='n'>Result</th><th></th></tr>");
            for (Student s : all) {
                sb.append("<tr><td class='n'>").append(s.getRollNo()).append("</td><td>").append(Html.esc(s.getName()))
                  .append("</td><td class='n'>").append(s.getSemesterNo()).append("</td><td class='n'>")
                  .append(s.getTotal()).append("/").append(s.getMaxTotal()).append("</td><td class='n'>")
                  .append(String.format("%.2f", s.getPercentage())).append("</td><td class='n'>").append(s.getGrade())
                  .append("</td><td class='n ").append(s.isPassed() ? "pass" : "fail").append("'>").append(s.getStatus())
                  .append("</td><td><a href='/search?roll=").append(s.getRollNo()).append("'>View</a></td></tr>");
            }
            sb.append("</table></div>");
        }
        sb.append("</div>");
        return Response.html(Html.page("All Results", sb.toString()));
    }

    private static Response summaryPage(Map<String, String> query) {
        String semText = query.get("sem");
        int chosen = 0;
        if (semText != null && toInt(semText) != null) {
            chosen = toInt(semText);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='card'><h2>Overall Class Result (Semester-wise)</h2>");
        sb.append("<form method='get' action='/summary'><label>Semester</label><select name='sem'>");
        sb.append(semesterOptions(chosen));
        sb.append("</select><button type='submit'>Show Summary</button></form></div>");

        if (semText != null) {
            if (chosen < 1 || chosen > 8) {
                sb.append("<div class='card err'><p>Please choose a semester between 1 and 8.</p></div>");
            } else {
                Summary sm = manager.getSummary(chosen);
                if (sm == null) {
                    sb.append("<div class='card err'><p>No records found for Semester ").append(chosen).append(".</p></div>");
                } else {
                    sb.append("<div class='card'><h3>Semester ").append(chosen).append(" - Overall Result</h3>");
                    sb.append("<div class='stats'>");
                    sb.append("<div class='stat'><b>").append(sm.getCount()).append("</b>students</div>");
                    sb.append("<div class='stat'><b class='pass'>").append(sm.getPassCount()).append("</b>passed</div>");
                    sb.append("<div class='stat'><b class='fail'>").append(sm.getFailCount()).append("</b>failed</div>");
                    sb.append("<div class='stat'><b>").append(String.format("%.2f", sm.getPassPercentage())).append(" %</b>pass percentage</div>");
                    sb.append("<div class='stat'><b>").append(String.format("%.2f", sm.getAverage())).append(" %</b>class average</div></div>");
                    sb.append("<p><b>Topper:</b> ").append(Html.esc(sm.getTopper().getName())).append(" (Roll ")
                      .append(sm.getTopper().getRollNo()).append(") - ")
                      .append(String.format("%.2f", sm.getTopper().getPercentage())).append(" %<br>");
                    sb.append("<b>Lowest scorer:</b> ").append(Html.esc(sm.getLowest().getName())).append(" (Roll ")
                      .append(sm.getLowest().getRollNo()).append(") - ")
                      .append(String.format("%.2f", sm.getLowest().getPercentage())).append(" %</p></div>");
                }
            }
        }
        return Response.html(Html.page("Class Summary", sb.toString()));
    }

    // ---------------------------------------------------------------- helpers
    // One result card (same information as the console result card)
    private static String resultCard(Student s) {
        Semester sem = s.getSemester();
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='card'><h3>Result Card - Semester ").append(s.getSemesterNo()).append("</h3>");
        sb.append("<p><b>Roll No:</b> ").append(s.getRollNo()).append(" &nbsp; <b>Name:</b> ")
          .append(Html.esc(s.getName())).append("</p>");
        sb.append("<div class='wrap'><table><tr><th>Code</th><th>Subject</th><th class='n'>Marks</th></tr>");
        for (int i = 0; i < sem.getSubjectCount(); i++) {
            sb.append("<tr><td>").append(Html.esc(sem.getSubjectCode(i))).append("</td><td>")
              .append(Html.esc(sem.getSubjectName(i))).append("</td><td class='n'>").append(s.getMark(i))
              .append("/").append(Student.MAX_MARKS).append("</td></tr>");
        }
        sb.append("</table></div>");
        sb.append("<p><b>Total:</b> ").append(s.getTotal()).append(" / ").append(s.getMaxTotal())
          .append(" &nbsp; <b>Percentage:</b> ").append(String.format("%.2f", s.getPercentage())).append(" %")
          .append(" &nbsp; <b>Grade:</b> ").append(s.getGrade())
          .append(" &nbsp; <b>Result:</b> <span class='").append(s.isPassed() ? "pass" : "fail").append("'>")
          .append(s.getStatus()).append("</span></p></div>");
        return sb.toString();
    }

    // <option> list for semesters 1..8 (selected one marked)
    private static String semesterOptions(int selected) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 8; i++) {
            sb.append("<option value='").append(i).append("'").append(i == selected ? " selected" : "")
              .append(">Semester ").append(i).append("</option>");
        }
        return sb.toString();
    }

    // Uses the name already saved for this roll number; otherwise validates the typed name
    private static String resolveName(int roll, String typed) {
        String saved = manager.findName(roll);
        if (saved != null) {
            return saved;
        }
        String name = typed == null ? "" : typed.trim();
        return Student.isValidName(name) ? name : null;
    }

    // Demo data (the same students as in the report)
    private static void loadSampleData() {
        addSample(101, "Rahul Das", 5, 78, 82, 74, 69, 88, 71, 76, 80, 85, 90);
        addSample(102, "Priya Sen", 5, 92, 95, 89, 91, 96, 88, 93, 94, 97, 90);
        addSample(103, "Amit Roy", 5, 35, 60, 55, 48, 52, 58, 62, 45, 50, 57);
        addSample(101, "Rahul Das", 3, 70, 75, 68, 72, 66, 80, 78, 74, 82);
    }

    private static void addSample(int roll, String name, int sem, int... marks) {
        String saved = manager.findName(roll);
        if (saved != null && !saved.equals(name)) {
            return;                                       // do not mix up with a real student
        }
        manager.addStudent(new Student(roll, name, Semester.getSemester(sem), marks));   // duplicates are ignored
    }

    private static Integer toInt(String s) {
        if (s == null) return null;
        try {
            return Integer.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Splits "a=1&b=2" into a map and decodes %xx and + characters
    private static Map<String, String> parseParams(String raw) {
        Map<String, String> map = new HashMap<String, String>();
        if (raw == null || raw.isEmpty()) return map;
        for (String pair : raw.split("&")) {
            int eq = pair.indexOf('=');
            String key = eq >= 0 ? pair.substring(0, eq) : pair;
            String value = eq >= 0 ? pair.substring(eq + 1) : "";
            map.put(decode(key), decode(value));
        }
        return map;
    }

    private static String decode(String s) {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            return s;
        }
    }

    private static String readBody(HttpExchange ex) throws IOException {
        InputStream in = ex.getRequestBody();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n, total = 0;
        while ((n = in.read(buf)) != -1) {
            total += n;
            if (total > 20000) break;                     // ignore very large requests
            out.write(buf, 0, n);
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    private static void send(HttpExchange ex, Response res) throws IOException {
        ex.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        if (res.location != null) {
            ex.getResponseHeaders().set("Location", res.location);
            ex.sendResponseHeaders(res.status, -1);
            ex.close();
            return;
        }
        byte[] bytes = res.body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", res.contentType);
        ex.sendResponseHeaders(res.status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
