import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class main {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/BlindHire";
        String username = "root";
        String password = ""; // replace with your MySQL root password

        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // ensures driver is loaded
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("✅ Connection successful!");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Connection failed.");
            e.printStackTrace();
        }
    }
}