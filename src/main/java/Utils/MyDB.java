package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton database connection utility.
 * Place this file at: src/main/java/Utils/Mydb.java
 *
 * Usage: Connection cnx = Mydb.getInstance().getConnection();
 */
public class Mydb {

    // ── Configure your database here ─────────────────────────────────────────
    private static final String URL      = "jdbc:mysql://localhost:3306/blindhire";
    private static final String USER     = "root";
    private static final String PASSWORD = "";
    // ─────────────────────────────────────────────────────────────────────────

    private static Mydb instance;
    private Connection connection;

    /** Private constructor — loads the JDBC driver and opens the connection. */
    private Mydb() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Database connected successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the singleton instance.
     * Re-opens the connection automatically if it was closed or lost.
     */
    public static Mydb getInstance() {
        try {
            if (instance == null || instance.connection == null || instance.connection.isClosed()) {
                instance = new Mydb();
            }
        } catch (SQLException e) {
            instance = new Mydb();
        }
        return instance;
    }

    /** Returns the active JDBC connection. */
    public Connection getConnection() {
        return connection;
    }
}