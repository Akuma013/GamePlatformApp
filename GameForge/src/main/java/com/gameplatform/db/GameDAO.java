package com.gameplatform.db;

import com.gameplatform.model.Game;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GameDAO {

    /**
     * Load every game in the database together with its average review rating.
     * Used by SearchService.buildIndex() to populate the Trie.
     *
     * LEFT JOIN ensures unrated games are still returned (with avgRating = 0).
     */
    public static List<Game> loadAllGamesWithRatings() throws SQLException {
        String sql =
                "SELECT g.gameID, g.gameName, g.gamePrice, g.version, g.gameSize, " +
                        "       g.imagePath, " +                                          // ← add
                        "       p.publisherName, " +
                        "       COALESCE(AVG(CAST(r.rating AS FLOAT)), 0) AS avgRating " +
                        "FROM Game g " +
                        "LEFT JOIN Publisher p ON p.publisherID = g.publisherID " +
                        "LEFT JOIN Review r ON r.gameID = g.gameID " +
                        "GROUP BY g.gameID, g.gameName, g.gamePrice, g.version, " +
                        "         g.gameSize, g.imagePath, p.publisherName";              // ← add
        List<Game> games = new ArrayList<>();
        try (Statement st = DBConnection.get().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                games.add(new Game(
                        rs.getInt("gameID"),
                        rs.getString("gameName"),
                        rs.getDouble("gamePrice"),
                        rs.getString("version"),
                        rs.getInt("gameSize"),
                        rs.getString("publisherName"),
                        rs.getDouble("avgRating"),
                        rs.getString("imagePath")             // ← add
                ));
            }
        }
        return games;
    }

    /**
     * Fetch a single game by its primary key. Used by the UI when the user
     * clicks a search suggestion or a store card.
     */
    public static Game findById(int gameID) throws SQLException {
        String sql =
                "SELECT g.gameID, g.gameName, g.gamePrice, g.version, g.gameSize, " +
                        "       g.imagePath, " +
                        "       p.publisherName, " +
                        "       COALESCE(AVG(CAST(r.rating AS FLOAT)), 0) AS avgRating " +
                        "FROM Game g " +
                        "LEFT JOIN Publisher p ON p.publisherID = g.publisherID " +
                        "LEFT JOIN Review r ON r.gameID = g.gameID " +
                        "WHERE g.gameID = ? " +                                       // ← add this
                        "GROUP BY g.gameID, g.gameName, g.gamePrice, g.version, " +
                        "         g.gameSize, g.imagePath, p.publisherName";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setInt(1, gameID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Game(
                            rs.getInt("gameID"),
                            rs.getString("gameName"),
                            rs.getDouble("gamePrice"),
                            rs.getString("version"),
                            rs.getInt("gameSize"),
                            rs.getString("publisherName"),
                            rs.getDouble("avgRating"),
                            rs.getString("imagePath")
                    );
                }
            }
        }
        return null;
    }
}