package com.gameplatform.db;

import com.gameplatform.model.Game;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reads and writes the Wishlist table for the currently logged-in customer.
 * Mirrors LibraryDAO in shape: list / add / remove / contains.
 */
public class WishlistDAO {

    public static List<Game> listForUser(String username) throws SQLException {
        Map<Integer, List<String>> genresByGame = GenreDAO.loadGenresByGameId();
        String sql =
                "SELECT g.gameID, g.gameName, g.gamePrice, g.version, g.gameSize, " +
                        "       g.imagePath, p.publisherName, " +
                        "       COALESCE(AVG(CAST(r.rating AS FLOAT)), 0) AS avgRating " +
                        "FROM Wishlist w " +
                        "JOIN Game g ON g.gameID = w.gameID " +
                        "LEFT JOIN Publisher p ON p.publisherID = g.publisherID " +
                        "LEFT JOIN Review r ON r.gameID = g.gameID " +
                        "WHERE w.userID = ? " +
                        "GROUP BY g.gameID, g.gameName, g.gamePrice, g.version, " +
                        "         g.gameSize, g.imagePath, p.publisherName";

        List<Game> games = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    games.add(new Game(
                            rs.getInt("gameID"),
                            rs.getString("gameName"),
                            rs.getDouble("gamePrice"),
                            rs.getString("version"),
                            rs.getInt("gameSize"),
                            rs.getString("publisherName"),
                            rs.getDouble("avgRating"),
                            rs.getString("imagePath"),
                            genresByGame.getOrDefault(rs.getInt("gameID"), List.of())
                    ));
                }
            }
        }
        return games;
    }

    public static void add(String username, int gameID) throws SQLException {
        String sql = "INSERT INTO Wishlist(userID, gameID) VALUES (?, ?)";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, gameID);
            ps.executeUpdate();
        }
    }

    public static void remove(String username, int gameID) throws SQLException {
        String sql = "DELETE FROM Wishlist WHERE userID = ? AND gameID = ?";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, gameID);
            ps.executeUpdate();
        }
    }

    public static boolean contains(String username, int gameID) throws SQLException {
        String sql = "SELECT 1 FROM Wishlist WHERE userID = ? AND gameID = ?";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, gameID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}