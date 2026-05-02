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

    /**
     * Library stats for the profile-style header on the Library tab.
     * Computed in one round-trip:
     *   - gamesOwned    : COUNT(*) from Library
     *   - favoritesCount: COUNT(*) where favorite = 1
     *   - totalPlayTime : SUM(playTime), in minutes (0 if user has no games)
     *   - topGenre      : the genre that appears most often across the user's
     *                     library, or null if user has no games or no genres
     */
    public static LibraryStats getStats(String username) throws SQLException {
        LibraryStats stats = new LibraryStats();

        // Counts and sums in one query
        String aggSql =
                "SELECT " +
                        "  COUNT(*)                            AS owned, " +
                        "  SUM(CASE WHEN favorite = 1 THEN 1 ELSE 0 END) AS favs, " +
                        "  COALESCE(SUM(playTime), 0)          AS total_minutes " +
                        "FROM Library WHERE userID = ?";

        try (PreparedStatement ps = DBConnection.get().prepareStatement(aggSql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.gamesOwned     = rs.getInt("owned");
                    stats.favoritesCount = rs.getInt("favs");
                    stats.totalPlayTime  = rs.getInt("total_minutes");
                }
            }
        }

        // Top genre — separate query because it joins three tables
        String genreSql =
                "SELECT TOP 1 gen.genreName, COUNT(*) AS n " +
                        "FROM Library l " +
                        "JOIN Game_Genre gg ON gg.gameID = l.gameID " +
                        "JOIN Genre gen ON gen.genreID = gg.genreID " +
                        "WHERE l.userID = ? " +
                        "GROUP BY gen.genreName " +
                        "ORDER BY n DESC, gen.genreName";

        try (PreparedStatement ps = DBConnection.get().prepareStatement(genreSql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.topGenre = rs.getString("genreName");
                }
            }
        }

        return stats;
    }

    /** Simple holder for the four library stats. */
    public static class LibraryStats {
        public int gamesOwned;
        public int favoritesCount;
        public int totalPlayTime;       // in minutes
        public String topGenre;         // null if no genre data
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