package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Mydb {
    private final String URL = "jdbc:mysql://localhost:3306/blindhire";
    private final String USER = "root";
    private final String PASSWORD = "";
    private Connection connection;
    private static Mydb instance;

    private Mydb() {
        try {
            this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/blindhire", "root", "");
            System.out.println("Connected to the database");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

    }

    public static Mydb getInstance() {
        if (instance == null) {
            instance = new Mydb();
        }

        return instance;
    }

    public Connection getConnection() {
        return this.connection;
    }
}
