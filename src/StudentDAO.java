
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentDAO {

    public void displayStudents() {

        String query = "SELECT roll_no, student_name, dept_code, current_semester FROM students";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                System.out.println("Database connection unavailable.");
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {

                System.out.println("\n--- Student Details ---");

                while (rs.next()) {
                    System.out.println("Roll No: " + rs.getString("roll_no"));
                    System.out.println("Name: " + rs.getString("student_name"));
                    System.out.println("Department: " + rs.getString("dept_code"));
                    System.out.println("Semester: " + rs.getInt("current_semester"));
                    System.out.println("-----------------------");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving students: " + e.getMessage());
        }
    }
}
