
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AcademicDAO {

    // Display attendance details and examination eligibility
    public void displayAttendanceReport() throws SQLException {
        String sql = """
            SELECT e.enrollment_id, s.roll_no, s.student_name,
                   c.course_code, c.title,
                   e.classes_held, e.classes_attended,
                   e.medical_approval
            FROM enrollments e
            JOIN students s ON s.roll_no = e.roll_no
            JOIN courses c ON c.course_code = e.course_code
            ORDER BY s.roll_no, c.course_code
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\nID | ROLL NO | STUDENT | COURSE | ATTENDANCE | STATUS");

            while (rs.next()) {
                int held = rs.getInt("classes_held");
                int attended = rs.getInt("classes_attended");

                double percentage = held == 0
                        ? 0 : attended * 100.0 / held;

                boolean medical = rs.getBoolean("medical_approval");

                String status;
                if (percentage >= 75) {
                    status = "Eligible for ESE";
                } else if (percentage >= 65) {
                    status = medical
                            ? "Medical condonation approved"
                            : "Medical approval required";
                } else {
                    status = "SA - Not eligible";
                }

                System.out.printf(
                    "%d | %s | %s | %s - %s | %.2f%% | %s%n",
                    rs.getInt("enrollment_id"),
                    rs.getString("roll_no"),
                    rs.getString("student_name"),
                    rs.getString("course_code"),
                    rs.getString("title"),
                    percentage,
                    status
                );
            }
        }
    }

    // Find enrollment ID using student roll number and course code
    public int findEnrollmentId(String rollNo, String courseCode)
            throws SQLException {

        String sql = """
            SELECT enrollment_id
            FROM enrollments
            WHERE roll_no = ? AND course_code = ?
            ORDER BY semester DESC
            LIMIT 1
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, rollNo);
            ps.setString(2, courseCode);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("enrollment_id");
                }
            }
        }

        return -1;
    }
}
