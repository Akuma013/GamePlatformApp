package com.gameplatform.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**

 Reads the [User] table for the currently logged-in customer.
 Most of what the app needs is balance + nickname for display.*/
public class UserDAO {

    public static double getBalance(String username) throws SQLException {
        String sql = "SELECT balance FROM [User] WHERE username = ?";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("balance");
            }
        }
        return 0.0;
    }

    public static String getNickname(String username) throws SQLException {
        String sql = "SELECT nickname FROM [User] WHERE username = ?";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("nickname");
            }
        }
        return username;
    }
    /* True if this username is a real customer in the [User] table. */
    public static boolean exists(String username) throws SQLException {
        String sql = "SELECT 1 FROM [User] WHERE username = ?";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

/* All distinct usernames, alphabetical. Used to populate the user search Trie. */
    public static java.util.List<String> listAllUsernames() throws SQLException {
        java.util.List<String> out = new java.util.ArrayList<>();
        String sql = "SELECT username FROM [User] ORDER BY username";
        try (java.sql.Statement st = DBConnection.get().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) out.add(rs.getString(1));
        }
        return out;
    }
}