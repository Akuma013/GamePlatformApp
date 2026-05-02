package com.gameplatform.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Manages the database connection and the current customer's identity.
 *
 * Two identities are tracked here:
 *   - SQL identity:  always "user1" (constants below).  Determines what
 *                    DCL grants apply.  Hidden from the customer.
 *   - App identity:  the row in [User] that the customer logged in as,
 *                    e.g. "alex_k".  This is what gets written into
 *                    Library, Wishlist, Review, [Order], etc.
 */
public class DBConnection {

    // Internal SQL credentials — never shown to the customer
    private static final String SQL_LOGIN    = "user1";
    private static final String SQL_PASSWORD = "User123!";

    private static final String BASE_URL =
            "jdbc:sqlserver://DESKTOP-7GN2E9Q\\SQLEXPRESS;databaseName=GamePlatformDB;encrypt=false";

    private static Connection connection;
    private static String appUsername;

    /**
     * Open a SQL connection and authenticate the customer.
     * @return true if both the SQL connection succeeded AND the customer's
     *         credentials match a row in [User].
     */
    public static boolean loginCustomer(String username, String password) {
        try {
            // 1. Open the underlying SQL connection (always as user1)
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            connection = DriverManager.getConnection(
                    BASE_URL, SQL_LOGIN, SQL_PASSWORD);

            // 2. Validate the customer against the [User] table
            String sql = "SELECT 1 FROM [User] WHERE username = ? AND password = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, password);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        // Bad customer credentials — close the connection again
                        connection.close();
                        connection = null;
                        return false;
                    }
                }
            }

            appUsername = username;
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try { if (connection != null) connection.close(); }
            catch (SQLException ignored) {}
            connection = null;
            return false;
        }
    }

    public static Connection get()           { return connection; }
    public static String getAppUsername()    { return appUsername; }

    public static void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException ignored) {}
        connection = null;
        appUsername = null;
    }
}