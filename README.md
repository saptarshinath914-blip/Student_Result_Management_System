# Semester-wise Student Result Management System (Java)

Mini project for **Object Oriented Programming (PCC-CS503)**, B.Tech CSE, Budge Budge Institute of Technology (MAKAUT).

A pure-Java application (no frameworks, no external libraries) that manages semester-wise student results:

- Choose a semester (1 to 8) and the subjects of that semester (MAKAUT CSE syllabus 2023-24) are shown
- Store subject-wise marks for each student
- Calculate total, percentage, grade (O, E, A, B, C, D, F) and PASS/FAIL (pass mark 40 in every subject)
- Search by roll number, display all results, overall class result per semester
- Input validation for roll number, name, semester and marks

It comes in **two versions that share the same classes**:

| Version | Start class | How to run |
|---|---|---|
| Console (menu-driven) | `Main` | `java Main` |
| Web (browser) | `WebServer` | `java WebServer` then open http://localhost:8080 |

## OOP concepts used
Classes and objects, constructors, methods, encapsulation (private data + getters), arrays, `ArrayList`,
conditional statements (`if-else`, `switch-case`), association (a `Student` has a `Semester`), static members, exception handling.

## Classes
| File | Purpose |
|---|---|
| `Student.java` | One student's result for one semester; calculates total, percentage, grade, status |
| `Semester.java` | Subject names and codes of a semester (`switch-case` returns the right semester) |
| `ResultManager.java` | Stores all results in an `ArrayList`; add, search, display, summary |
| `Summary.java` | Overall result of one semester (used by console and web) |
| `Main.java` | Console menu program |
| `Html.java` | Page layout and HTML helpers for the web version |
| `WebServer.java` | Web version using the HTTP server built into the JDK |

## Run on your computer
Requires JDK 8 or newer.

```
javac *.java
java Main          # console version
java WebServer     # web version -> http://localhost:8080
```

## Host online (Docker)
The included `Dockerfile` builds and starts the web version. The server reads the `PORT` environment variable that hosting sites provide.
Any host that can run a Dockerfile from a GitHub repository works (for example Render). Typical steps:

1. Push this folder to a GitHub repository (files at the root of the repository).
2. On the hosting site create a new **Web Service** from that repository and choose **Docker** as the runtime.
3. Deploy, then open the public URL it gives you. `/health` returns `OK`.

Note: data is kept in memory, so it is cleared when the server restarts or sleeps. Use the **Load sample data** button on the home page to add demo students.

## Team
Group members: *(add names and roll numbers here)*
