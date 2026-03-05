package org.example;

import Utils.Mydb;

import java.sql.Connection;

public class testDB {
    public static void main(String[] args) {

        Connection conn = Mydb.getInstance().getConnection();

        if (conn != null) {
            System.out.println("Connection test successful ");
        } else {
            System.out.println("Connection test failed ");
        }
    }
}
