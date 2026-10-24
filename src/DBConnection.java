import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Central MariaDB connection settings. Edit these for your local installation. */
public final class DBConnection {
    private static final String URL = System.getenv().getOrDefault(
        "KEC_DB_URL", "jdbc:mariadb://localhost:3306/kec_academic_system");
    private static final String USER = System.getenv().getOrDefault("KEC_DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("KEC_DB_PASSWORD", "");

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
