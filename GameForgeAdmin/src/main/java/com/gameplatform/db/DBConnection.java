package com.gameplatform.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String BASE_URL =
        "jdbc:sqlserver://DESKTOP-7OVINNS\\SQLEXPRESS;databaseName=GamePlatformDB;encrypt=false";

    private static Connection connection;
    private static String currentUser;
    private static String currentRole;

    public static boolean connect(String username, String password) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            connection = DriverManager.getConnection(BASE_URL, username, password);
            currentUser = username;
            currentRole = detectRole(username);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String detectRole(String username) {
        return switch (username.toLowerCase()) {
            case "admin1"   -> "ADMIN";
            case "user1"    -> "USER";
            case "analyst1" -> "ANALYST";
            default         -> "UNKNOWN";
        };
    }

    public static Connection get() { return connection; }
    public static String getCurrentUser() { return currentUser; }
    public static String getCurrentRole() { return currentRole; }

    public static void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException ignored) {}
        connection = null;
        currentUser = null;
        currentRole = null;
    }
}
