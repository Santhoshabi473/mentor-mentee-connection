package com.messaging.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtil {
  private static final String DB_URL = "jdbc:sqlite:C:/messaging.db";

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() throws SQLException, ClassNotFoundException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "username TEXT UNIQUE, " +
                         "password TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS groups (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "name TEXT, " +
                         "creator_id INTEGER)");
            stmt.execute("CREATE TABLE IF NOT EXISTS group_members (" +
                         "group_id INTEGER, " +
                         "user_id INTEGER)");
            stmt.execute("CREATE TABLE IF NOT EXISTS group_messages (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "group_id INTEGER, " +
                         "sender_id INTEGER, " +
                         "message TEXT, " +
                         "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS private_messages (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "sender_id INTEGER, " +
                         "receiver_id INTEGER, " +
                         "message TEXT, " +
                         "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
        }
    }
}