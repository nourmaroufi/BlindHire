package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDB {
    private final String URL = "jdbc:mysql://localhost:3306/blindhire?autoReconnect=true&useSSL=false";
    private final String USERNAME = "root";
    private final String PASSWORD = "";

    private Connection connection;
    private static MyDB instance;

    private MyDB() {
        connect();
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("DB connected.");
        } catch (SQLException e) {
            System.err.println("DB connection failed: " + e.getMessage());
        }
    }

    public static MyDB getInstance() {
        if (instance == null) {
            instance = new MyDB();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            // If connection is closed or invalid, reconnect
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                System.out.println("Connection lost, reconnecting...");
                connect();
            }
        } catch (SQLException e) {
            System.err.println("Error checking connection: " + e.getMessage());
            connect();
        }
        return connection;
    }
}