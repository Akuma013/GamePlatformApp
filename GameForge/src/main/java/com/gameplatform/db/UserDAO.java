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
}