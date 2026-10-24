import java.sql.*;
import java.util.Scanner;
import java.util.Locale;

public class MainApp {
    private static final Scanner SC = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("KEC STUDENT ACADEMIC & CONTINUOUS EVALUATION SYSTEM");
        try (Connection ignored = DBConnection.getConnection()) {
            System.out.println("Database connected successfully.");
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            System.out.println("Check MariaDB, schema import, and DB settings in README.md.");
            return;
        }

        int choice;
        do {
            menu();
            choice = readInt("Enter choice: ", 0, 12);
            try {
                switch (choice) {
                    case 1 -> listStudents();
                    case 2 -> addStudent();
                    case 3 -> updateStudent();
                    case 4 -> deleteStudent();
                    case 5 -> listCourses();
                    case 6 -> addCourse();
                    case 7 -> enrollStudent();
                    case 8 -> updateAttendance();
                    case 9 -> enterMarks();
                    case 10 -> attendanceReport();
                    case 11 -> semesterMarkSheet();
                    case 12 -> departmentSummary();
                    case 0 -> System.out.println("Exiting. Thank you.");
                    default -> System.out.println("Invalid choice.");
                }
            } catch (SQLException e) {
                System.out.println("Database operation failed: " + e.getMessage());
            }
        } while (choice != 0);
    }

    private static void menu() {
        System.out.println("\n========== MAIN MENU ==========");
        System.out.println("1. Display all students");
        System.out.println("2. Register student");
        System.out.println("3. Update student");
        System.out.println("4. Delete student");
        System.out.println("5. Display courses");
        System.out.println("6. Add course");
        System.out.println("7. Enroll student in course");
        System.out.println("8. Update attendance / medical approval");
        System.out.println("9. Enter/update CIA, assignment, ESE marks");
        System.out.println("10. Attendance report / defaulters");
        System.out.println("11. Student semester mark sheet / SGPA");
        System.out.println("12. Department-wise performance summary");
        System.out.println("0. Exit");
    }

    private static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String s = SC.nextLine().trim();
            try {
                int v = Integer.parseInt(s);
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException ignored) {}
            System.out.println("Enter a whole number from " + min + " to " + max + ".");
        }
    }
    private static double readDouble(String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            try {
                double v = Double.parseDouble(SC.nextLine().trim());
                if (Double.isFinite(v) && v >= min && v <= max) return v;
            } catch (NumberFormatException ignored) {}
            System.out.println("Enter a number from " + min + " to " + max + ".");
        }
    }
    private static String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = SC.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println("This field cannot be empty.");
        }
    }
    private static boolean readYesNo(String prompt) {
        while (true) {
            String s = readText(prompt + " (y/n): ").toLowerCase(Locale.ROOT);
            if (s.equals("y") || s.equals("yes")) return true;
            if (s.equals("n") || s.equals("no")) return false;
            System.out.println("Enter y or n.");
        }
    }

    private static void listStudents() throws SQLException {
        String sql = "SELECT s.roll_no,s.student_name,d.dept_name,s.current_semester,s.email,s.phone " +
                     "FROM students s JOIN department d ON d.dept_code=s.dept_code ORDER BY s.roll_no";
        try (Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql);
             ResultSet r=p.executeQuery()) {
            System.out.println("\\nROLL NO | NAME | DEPARTMENT | SEM | EMAIL | PHONE");
            while(r.next()) System.out.printf("%s | %s | %s | %d | %s | %s%n",
                r.getString(1),r.getString(2),r.getString(3),r.getInt(4),nvl(r.getString(5)),nvl(r.getString(6)));
        }
    }
    private static void addStudent() throws SQLException {
        String roll=readText("Roll number: ").toUpperCase(Locale.ROOT);
        String name=readText("Student name: ");
        String dept=readText("Department code (CSE/IT/ECE): ").toUpperCase(Locale.ROOT);
        int sem=readInt("Current semester (1-8): ",1,8);
        System.out.print("Email (optional): "); String email=SC.nextLine().trim();
        System.out.print("Phone (optional): "); String phone=SC.nextLine().trim();
        String sql="INSERT INTO students(roll_no,student_name,dept_code,current_semester,email,phone) VALUES(?,?,?,?,?,?)";
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,roll);p.setString(2,name);p.setString(3,dept);p.setInt(4,sem);
            p.setString(5,emptyNull(email));p.setString(6,emptyNull(phone));p.executeUpdate();
            System.out.println("Student registered.");
        }
    }
    private static void updateStudent() throws SQLException {
        String roll=readText("Roll number to update: ").toUpperCase(Locale.ROOT);
        String name=readText("New student name: ");
        String dept=readText("New department code: ").toUpperCase(Locale.ROOT);
        int sem=readInt("New semester (1-8): ",1,8);
        System.out.print("New email (optional): ");String email=SC.nextLine().trim();
        System.out.print("New phone (optional): ");String phone=SC.nextLine().trim();
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(
            "UPDATE students SET student_name=?,dept_code=?,current_semester=?,email=?,phone=? WHERE roll_no=?")){
            p.setString(1,name);p.setString(2,dept);p.setInt(3,sem);p.setString(4,emptyNull(email));p.setString(5,emptyNull(phone));p.setString(6,roll);
            System.out.println(p.executeUpdate()>0?"Student updated.":"No student found.");
        }
    }
    private static void deleteStudent() throws SQLException {
        String roll=readText("Roll number to delete: ").toUpperCase(Locale.ROOT);
        if(!readYesNo("Delete "+roll+"? Related enrollments will also be deleted")) return;
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("DELETE FROM students WHERE roll_no=?")){
            p.setString(1,roll);System.out.println(p.executeUpdate()>0?"Student deleted.":"No student found.");
        }
    }
    private static void listCourses() throws SQLException {
        String sql="SELECT course_code,title,credits,course_type,dept_code FROM courses ORDER BY course_code";
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){
            System.out.println("\\nCOURSE CODE | TITLE | CREDITS | TYPE | DEPT");
            while(r.next())System.out.printf("%s | %s | %.1f | %s | %s%n",r.getString(1),r.getString(2),r.getDouble(3),r.getString(4),r.getString(5));
        }
    }
    private static void addCourse() throws SQLException {
        String code=readText("Course code: ").toUpperCase(Locale.ROOT);
        String title=readText("Course title: ");
        double credits=readDouble("Credits (0.5-10): ",0.5,10);
        String type;
        while(true){type=readText("Type THEORY / PRACTICAL / EMBEDDED: ").toUpperCase(Locale.ROOT);
            if(type.equals("THEORY")||type.equals("PRACTICAL")||type.equals("EMBEDDED"))break;
            System.out.println("Invalid course type.");}
        String dept=readText("Department code: ").toUpperCase(Locale.ROOT);
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(
            "INSERT INTO courses(course_code,title,credits,course_type,dept_code) VALUES(?,?,?,?,?)")){
            p.setString(1,code);p.setString(2,title);p.setDouble(3,credits);p.setString(4,type);p.setString(5,dept);p.executeUpdate();
            System.out.println("Course added.");
        }
    }
    private static void enrollStudent() throws SQLException {
        String roll=readText("Student roll number: ").toUpperCase(Locale.ROOT);
        String code=readText("Course code: ").toUpperCase(Locale.ROOT);
        int sem=readInt("Semester (1-8): ",1,8);
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try(PreparedStatement p=c.prepareStatement("INSERT INTO enrollments(roll_no,course_code,semester) VALUES(?,?,?)",Statement.RETURN_GENERATED_KEYS)){
                p.setString(1,roll);p.setString(2,code);p.setInt(3,sem);p.executeUpdate();
                try(ResultSet keys=p.getGeneratedKeys()){if(keys.next())try(PreparedStatement m=c.prepareStatement("INSERT INTO assessment_marks(enrollment_id) VALUES(?)")){m.setInt(1,keys.getInt(1));m.executeUpdate();}}
                c.commit();System.out.println("Enrollment created.");
            }catch(SQLException e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
        }
    }
    private static void updateAttendance() throws SQLException {
        int id=readInt("Enrollment ID (see attendance report): ",1,Integer.MAX_VALUE);
        int held=readInt("Total classes held: ",0,10000);
        int attended=readInt("Classes attended: ",0,held);
        boolean medical=readYesNo("Medical condonation approved");
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(
            "UPDATE enrollments SET classes_held=?,classes_attended=?,medical_approval=? WHERE enrollment_id=?")){
            p.setInt(1,held);p.setInt(2,attended);p.setBoolean(3,medical);p.setInt(4,id);
            System.out.println(p.executeUpdate()>0?"Attendance updated.":"Enrollment ID not found.");
        }
    }
    private static void enterMarks() throws SQLException {
        int id=readInt("Enrollment ID (see attendance report): ",1,Integer.MAX_VALUE);
        double cia1=readDouble("CIA-1 (0-100): ",0,100);
        double cia2=readDouble("CIA-2 (0-100): ",0,100);
        double assignment=readDouble("Assignment (0-100): ",0,100);
        double quiz=readDouble("Quiz (0-100): ",0,100);
        double lab=readDouble("Lab evaluation (0-100): ",0,100);
        double ese=readDouble("ESE (0-100; enter 0 if absent): ",0,100);
        boolean absent=readYesNo("Student absent for ESE");
        String sql="UPDATE assessment_marks SET cia1=?,cia2=?,assignment_mark=?,quiz_mark=?,lab_evaluation=?,ese_mark=?,ese_absent=? WHERE enrollment_id=?";
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setDouble(1,cia1);p.setDouble(2,cia2);p.setDouble(3,assignment);p.setDouble(4,quiz);p.setDouble(5,lab);p.setDouble(6,ese);p.setBoolean(7,absent);p.setInt(8,id);
            System.out.println(p.executeUpdate()>0?"Marks saved.":"No marks record. Enroll the student first.");
        }
    }
    private static void attendanceReport() throws SQLException {
        String sql="SELECT e.enrollment_id,s.roll_no,s.student_name,c.course_code,c.title,e.classes_held,e.classes_attended,e.medical_approval " +
          "FROM enrollments e JOIN students s ON s.roll_no=e.roll_no JOIN courses c ON c.course_code=e.course_code ORDER BY s.roll_no,c.course_code";
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){
            System.out.println("\\nID | ROLL NO | STUDENT | COURSE | ATTENDANCE | STATUS");
            while(r.next()){
                int held=r.getInt("classes_held"),att=r.getInt("classes_attended");
                double pct=held==0?0:100.0*att/held;
                boolean medical=r.getBoolean("medical_approval");
                String status=pct>=75?"Eligible for ESE":pct>=65?(medical?"Medical condonation approved":"Medical approval required"):"SA - Not eligible";
                System.out.printf("%d | %s | %s | %s | %.2f%% (%d/%d) | %s%n",r.getInt(1),r.getString(2),r.getString(3),r.getString(4),pct,att,held,status);
            }
        }
    }
    private static void semesterMarkSheet() throws SQLException {
        String roll=readText("Student roll number: ").toUpperCase(Locale.ROOT);
        int sem=readInt("Semester: ",1,8);
        String sql="SELECT s.student_name,s.roll_no,c.course_code,c.title,c.credits,e.classes_held,e.classes_attended,e.medical_approval,"+
            "m.cia1,m.cia2,m.assignment_mark,m.quiz_mark,m.lab_evaluation,m.ese_mark,m.ese_absent "+
            "FROM enrollments e JOIN students s ON s.roll_no=e.roll_no JOIN courses c ON c.course_code=e.course_code "+
            "LEFT JOIN assessment_marks m ON m.enrollment_id=e.enrollment_id WHERE e.roll_no=? AND e.semester=? ORDER BY c.course_code";
        double weighted=0,totalCredits=0;
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,roll);p.setInt(2,sem);
            try(ResultSet r=p.executeQuery()){
                boolean any=false;
                while(r.next()){
                    any=true;
                    double credits=r.getDouble("credits");
                    int held=r.getInt("classes_held"),att=r.getInt("classes_attended");
                    double pct=held==0?0:100.0*att/held;
                    String eligibility=pct>=75?"Eligible":pct>=65?(r.getBoolean("medical_approval")?"Condonation approved":"Medical approval required"):"SA - Not eligible";
                    double internal=(r.getDouble("cia1")+r.getDouble("cia2"))/2.0*0.4+r.getDouble("assignment_mark")*0.2+r.getDouble("quiz_mark")*0.1+r.getDouble("lab_evaluation")*0.3;
                    double total=internal*0.4+r.getDouble("ese_mark")*0.6;
                    boolean absent=r.getBoolean("ese_absent");
                    String grade=absent||pct<65?"SA":grade(total);
                    int gp=gradePoint(grade);
                    System.out.printf("%s - %s | Credits %.1f | Attendance %.1f%% (%s) | Internal %.2f | ESE %.2f | Total %.2f | Grade %s%n",
                        r.getString("course_code"),r.getString("title"),credits,pct,eligibility,internal,r.getDouble("ese_mark"),total,grade);
                    if(!grade.equals("SA")){weighted+=gp*credits;totalCredits+=credits;}
                }
                if(!any){System.out.println("No enrollments found for this student and semester.");return;}
            }
        }
        System.out.printf("SGPA (excluding SA courses): %.2f%n",totalCredits==0?0:weighted/totalCredits);
        System.out.println("Note: The grade thresholds and component weights are configurable academic assumptions; confirm your department's official regulation before submission.");
    }
    private static void departmentSummary() throws SQLException {
        String sql="SELECT d.dept_code,d.dept_name,COUNT(DISTINCT s.roll_no) students,COUNT(e.enrollment_id) enrollments,"+
            "AVG(CASE WHEN e.classes_held>0 THEN 100.0*e.classes_attended/e.classes_held END) avg_attendance,"+
            "AVG(CASE WHEN m.ese_absent=0 THEN ( ((m.cia1+m.cia2)/2*0.4+m.assignment_mark*0.2+m.quiz_mark*0.1+m.lab_evaluation*0.3)*0.4+m.ese_mark*0.6) END) avg_total "+
            "FROM department d LEFT JOIN students s ON s.dept_code=d.dept_code LEFT JOIN enrollments e ON e.roll_no=s.roll_no "+
            "LEFT JOIN courses c ON c.course_code=e.course_code LEFT JOIN assessment_marks m ON m.enrollment_id=e.enrollment_id GROUP BY d.dept_code,d.dept_name ORDER BY d.dept_code";
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){
            System.out.println("\\nDEPARTMENT | STUDENTS | ENROLLMENTS | AVG ATTENDANCE | AVG MARK TOTAL");
            while(r.next())System.out.printf("%s - %s | %d | %d | %s | %s%n",r.getString(1),r.getString(2),r.getInt(3),r.getInt(4),
                r.getObject(5)==null?"N/A":String.format("%.2f%%",r.getDouble(5)),r.getObject(6)==null?"N/A":String.format("%.2f",r.getDouble(6)));
        }
    }
    private static String grade(double mark) {
        if(mark>=90)return "O"; if(mark>=80)return "A+"; if(mark>=70)return "A";
        if(mark>=60)return "B+"; if(mark>=50)return "B"; if(mark>=40)return "C"; return "F";
    }
    private static int gradePoint(String g) {
        return switch(g){case "O"->10;case "A+"->9;case "A"->8;case "B+"->7;case "B"->6;case "C"->5;default->0;};
    }
    private static String nvl(String s){return s==null?"-":s;}
    private static String emptyNull(String s){return s==null||s.isBlank()?null:s;}
}
