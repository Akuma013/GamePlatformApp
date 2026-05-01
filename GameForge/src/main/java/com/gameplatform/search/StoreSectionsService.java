package com.gameplatform.search;

import com.gameplatform.db.DBConnection;
import com.gameplatform.db.GenreDAO;
import com.gameplatform.model.Game;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Computes the curated rows shown on the Store landing.
 *
 * Each method runs a focused SQL query and returns a list of Game
 * objects ready to render as cards. All methods accept the username
 * because some sections (recommendations) are personalised.
 *
 * Sections:
 *   - topRated()       : games with the highest avgRating
 *   - newArrivals()    : highest gameIDs (proxy for recently added)
 *   - budgetPicks()    : top-rated games priced under $20
 *   - recommended()    : games sharing genres with the user's library,
 *                        excluding games they already own
 */
public class StoreSectionsService {

    private static final int LIMIT = 10;   // 10 cards per row, 2 rows of 5 in WrapLayout

    public List<Game> topRated() throws SQLException {
        String sql =
                "SELECT TOP (" + LIMIT + ") " +
                        "       g.gameID, g.gameName, g.gamePrice, g.version, g.gameSize, " +
                        "       g.imagePath, p.publisherName, " +
                        "       COALESCE(AVG(CAST(r.rating AS FLOAT)), 0) AS avgRating " +
                        "FROM Game g " +
                        "LEFT JOIN Publisher p ON p.publisherID = g.publisherID " +
                        "LEFT JOIN Review r ON r.gameID = g.gameID " +
                        "GROUP BY g.gameID, g.gameName, g.gamePrice, g.version, " +
                        "         g.gameSize, g.imagePath, p.publisherName " +
                        "HAVING COALESCE(AVG(CAST(r.rating AS FLOAT)), 0) > 0 " +
                        "ORDER BY avgRating DESC, g.gameID";
        return runQuery(sql, null);
    }

    public List<Game> newArrivals() throws SQLException {
        String sql =
                "SELECT TOP (" + LIMIT + ") " +
                        "       g.gameID, g.gameName, g.gamePrice, g.version, g.gameSize, " +
                        "       g.imagePath, p.publisherName, " +
                        "       COALESCE(AVG(CAST(r.rating AS FLOAT)), 0) AS avgRating " +
                        "FROM Game g " +
                        "LEFT JOIN Publisher p ON p.publisherID = g.publisherID " +
                        "LEFT JOIN Review r ON r.gameID = g.gameID " +
                        "GROUP BY g.gameID, g.gameName, g.gamePrice, g.version, " +
                        "         g.gameSize, g.imagePath, p.publisherName " +
                        "ORDER BY g.gameID DESC";
        return runQuery(sql, null);
    }

    public List<Game> budgetPicks() throws SQLException {
        String sql =
                "SELECT TOP (" + LIMIT + ") " +
                        "       g.gameID, g.gameName, g.gamePrice, g.version, g.gameSize, " +
                        "       g.imagePath, p.publisherName, " +
                        "       COALESCE(AVG(CAST(r.rating AS FLOAT)), 0) AS avgRating " +
                        "FROM Game g " +
                        "LEFT JOIN Publisher p ON p.publisherID = g.publisherID " +
                        "LEFT JOIN Review r ON r.gameID = g.gameID " +
                        "WHERE g.gamePrice < 20 " +
                        "GROUP BY g.gameID, g.gameName, g.gamePrice, g.version, " +
                        "         g.gameSize, g.imagePath, p.publisherName " +
                        "ORDER BY avgRating DESC, g.gamePrice ASC";
        return runQuery(sql, null);
    }

    /**
     * Games whose genres overlap with the user's library, excluding
     * games they already own. Ranked by (overlap_count, avgRating).
     *
     * The query joins:
     *   user's library → Game_Genre → other games in those genres
     * grouped by gameID with a count of how many shared genres.
     */
    public List<Game> recommended(String username) throws SQLException {
        String sql =
                "SELECT TOP (" + LIMIT + ") " +
                        "       g.gameID, g.gameName, g.gamePrice, g.version, g.gameSize, " +
                        "       g.imagePath, p.publisherName, " +
                        "       COALESCE(AVG(CAST(r.rating AS FLOAT)), 0) AS avgRating, " +
                        "       COUNT(DISTINCT my_gg.genreID) AS shared_genres " +
                        "FROM Game g " +
                        "JOIN Game_Genre gg ON gg.gameID = g.gameID " +
                        "JOIN ( " +
                        "    SELECT DISTINCT gg2.genreID " +
                        "    FROM Library l2 " +
                        "    JOIN Game_Genre gg2 ON gg2.gameID = l2.gameID " +
                        "    WHERE l2.userID = ? " +
                        ") AS my_gg ON my_gg.genreID = gg.genreID " +
                        "LEFT JOIN Publisher p ON p.publisherID = g.publisherID " +
                        "LEFT JOIN Review r ON r.gameID = g.gameID " +
                        "WHERE g.gameID NOT IN ( " +
                        "    SELECT gameID FROM Library WHERE userID = ? " +
                        ") " +
                        "GROUP BY g.gameID, g.gameName, g.gamePrice, g.version, " +
                        "         g.gameSize, g.imagePath, p.publisherName " +
                        "ORDER BY shared_genres DESC, avgRating DESC";
        return runQuery(sql, username);
    }

    /**
     * Run a SELECT and build a list of Game objects.
     * @param username for queries that need a parameter; null for unparameterised queries.
     */
    private List<Game> runQuery(String sql, String username) throws SQLException {
        Map<Integer, List<String>> genres = GenreDAO.loadGenresByGameId();
        List<Game> out = new ArrayList<>();

        if (username == null) {
            try (Statement st = DBConnection.get().createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                readGames(rs, genres, out);
            }
        } else {
            try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, username);   // recommended() uses it twice
                try (ResultSet rs = ps.executeQuery()) {
                    readGames(rs, genres, out);
                }
            }
        }
        return out;
    }

    private void readGames(ResultSet rs, Map<Integer, List<String>> genres,
                           List<Game> out) throws SQLException {
        while (rs.next()) {
            int id = rs.getInt("gameID");
            out.add(new Game(
                    id,
                    rs.getString("gameName"),
                    rs.getDouble("gamePrice"),
                    rs.getString("version"),
                    rs.getInt("gameSize"),
                    rs.getString("publisherName"),
                    rs.getDouble("avgRating"),
                    rs.getString("imagePath"),
                    genres.getOrDefault(id, List.of())
            ));
        }
    }
}