package com.gameplatform.db;

import com.gameplatform.model.Review;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Reviews are gated by the prevent_review_for_unowned_game trigger:
 * only customers who own a game can post a review for it.
 * The trigger raises an error which our DAO surfaces as SQLException.
 */
public class ReviewDAO {

    public static List<Review> listForGame(int gameID) throws SQLException {
        String sql =
                "SELECT reviewID, rating, description, reviewDate, userID " +
                        "FROM Review WHERE gameID = ? " +
                        "ORDER BY reviewDate DESC";
        List<Review> out = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setInt(1, gameID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date sqlDate = rs.getDate("reviewDate");
                    LocalDate date = sqlDate == null ? null : sqlDate.toLocalDate();
                    out.add(new Review(
                            rs.getInt("reviewID"),
                            rs.getInt("rating"),
                            rs.getString("description"),
                            date,
                            rs.getString("userID")
                    ));
                }
            }
        }
        return out;
    }

    /**
     * Insert a new review. The trigger will reject it with
     * "You can only review games you own." if the user doesn't own this game.
     */
    public static void create(String username, int gameID, int rating,
                              String description) throws SQLException {
        String sql = "INSERT INTO Review (rating, description, reviewDate, userID, gameID) " +
                "VALUES (?, ?, CAST(GETDATE() AS DATE), ?, ?)";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setInt(1, rating);
            ps.setString(2, description);
            ps.setString(3, username);
            ps.setInt(4, gameID);
            ps.executeUpdate();
        }
    }

    /** Has this customer already posted a review for this game? */
    public static boolean hasReviewed(String username, int gameID) throws SQLException {
        String sql = "SELECT 1 FROM Review WHERE userID = ? AND gameID = ?";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, gameID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}