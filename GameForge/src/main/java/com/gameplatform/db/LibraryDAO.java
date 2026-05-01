package com.gameplatform.db;

import com.gameplatform.model.Game;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reads and writes the Library table for the currently logged-in customer.
 *
 * Library rows store playTime and favorite alongside the FK to Game,
 * but for the panel we only need the joined Game record itself —
 * playTime/favorite will surface in Step 13's detail dialog.
 */
public class LibraryDAO {

    /**
     * Every game owned by the given customer, joined with Publisher and
     * average review rating (so we can reuse GameCard unchanged).
     */
    public static List<Game> listForUser(String username) throws SQLException {
        Map<Integer, List<String>> genresByGame = GenreDAO.loadGenresByGameId();

        String sql =
                "SELECT g.gameID, g.gameName, g.gamePrice, g.version, g.gameSize, " +
                        "       g.imagePath, p.publisherName, " +
                        "       l.playTime, l.favorite, " +
                        "       COALESCE(AVG(CAST(r.rating AS FLOAT)), 0) AS avgRating " +
                        "FROM Library l " +
                        "JOIN Game g ON g.gameID = l.gameID " +
                        "LEFT JOIN Publisher p ON p.publisherID = g.publisherID " +
                        "LEFT JOIN Review r ON r.gameID = g.gameID " +
                        "WHERE l.userID = ? " +
                        "GROUP BY g.gameID, g.gameName, g.gamePrice, g.version, " +
                        "         g.gameSize, g.imagePath, p.publisherName, " +
                        "         l.playTime, l.favorite";

        List<Game> out = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("gameID");
                    Game g = new Game(
                            id,
                            rs.getString("gameName"),
                            rs.getDouble("gamePrice"),
                            rs.getString("version"),
                            rs.getInt("gameSize"),
                            rs.getString("publisherName"),
                            rs.getDouble("avgRating"),
                            rs.getString("imagePath"),
                            genresByGame.getOrDefault(id, List.of())
                    );
                    g.setPlayTime(rs.getInt("playTime"));
                    g.setFavorite(rs.getInt("favorite") == 1);
                    out.add(g);
                }
            }
        }
        return out;
    }


    /** Remove a game from the customer's library. */
    public static void remove(String username, int gameID) throws SQLException {
        String sql = "DELETE FROM Library WHERE userID = ? AND gameID = ?";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, gameID);
            ps.executeUpdate();
        }
    }

    /** Has this customer got this game already? Used to disable "Buy" buttons. */
    public static boolean owns(String username, int gameID) throws SQLException {
        String sql = "SELECT 1 FROM Library WHERE userID = ? AND gameID = ?";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, gameID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    /** Toggle the favorite flag for a game in the user's library. */
    public static void setFavorite(String username, int gameID, boolean favorite)
            throws SQLException {
        String sql = "UPDATE Library SET favorite = ? " +
                "WHERE userID = ? AND gameID = ?";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setInt(1, favorite ? 1 : 0);
            ps.setString(2, username);
            ps.setInt(3, gameID);
            ps.executeUpdate();
        }
    }

    /* Read the current favorite flag. */
    public static boolean isFavorite(String username, int gameID) throws SQLException {
        String sql = "SELECT favorite FROM Library WHERE userID = ? AND gameID = ?";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, gameID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("favorite") == 1;
            }
        }
    }

    /** Read the current playtime in minutes. Returns 0 if not in library.**/
     public static int getPlayTime(String username, int gameID) throws SQLException {
     String sql = "SELECT playTime FROM Library WHERE userID = ? AND gameID = ?";
     try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
     ps.setString(1, username);
     ps.setInt(2, gameID);
     try (ResultSet rs = ps.executeQuery()) {
     return rs.next() ? rs.getInt("playTime") : 0;
     }
     }
     }

     /** Increment playtime by N minutes. Used by the "Simulate Playing" button. **/
    public static void incrementPlayTime(String username, int gameID, int minutes)
            throws SQLException {
        String sql = "UPDATE Library SET playTime = playTime + ? " +
                "WHERE userID = ? AND gameID = ?";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setInt(1, minutes);
            ps.setString(2, username);
            ps.setInt(3, gameID);
            ps.executeUpdate();
        }
    }


}