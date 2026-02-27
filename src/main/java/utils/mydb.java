package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class mydb {
    private final String URL ="jdbc:mysql://localhost:3306/quizzing";
    private final String USERNAME = "root";
    private final String PASSWORD ="";


    private Connection connection;
    private static mydb instance;

    private mydb(){
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("connected");
        }catch(SQLException e){
            System.err.println(e.getMessage());
        }
    }

    public static mydb getInstance(){
        if(instance==null){
            instance = new mydb();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

}
