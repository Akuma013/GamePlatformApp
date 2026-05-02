package com.gameplatform.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Friend operations.
 *
 * INVARIANT: every friendship is stored as two rows — one in each direction.
 * Both add() and remove() operate on both rows in a single transaction so
 * the table is always symmetric. Application code MUST NOT insert into
 * Friend directly; always go through this DAO.
 */
public class FriendDAO {

    /**
     * List the usernames of the given user's friends, alphabetical.
     * Reads from userID_1 = me — but because of the symmetric invariant,
     * this returns the full friend set even though we only look at one column.
     */
    public static List<String> listFriends(String username) throws SQLException {
        String sql =
                "SELECT userID_2 AS friend FROM Friend WHERE userID_1 = ? " +
                        "ORDER BY userID_2";
        List<String> out = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getString("friend"));
            }
        }
        return out;
    }

    /** Are these two users already friends? */
    public static boolean areFriends(String a, String b) throws SQLException {
        String sql = "SELECT 1 FROM Friend WHERE userID_1 = ? AND userID_2 = ?";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setString(1, a);
            ps.setString(2, b);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Add a mutual friendship. Inserts BOTH directions atomically.
     * Throws if either direction already exists, if a == b, or if either
     * username doesn't exist (FK violation).
     */
    public static void add(String a, String b) throws SQLException {
        if (a == null || b == null) throw new SQLException("Usernames cannot be null.");
        if (a.equalsIgnoreCase(b))  throw new SQLException("You cannot befriend yourself.");

        Connection conn = DBConnection.get();
        boolean prevAuto = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Friend(userID_1, userID_2) VALUES (?, ?)")) {
                ps.setString(1, a); ps.setString(2, b); ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Friend(userID_1, userID_2) VALUES (?, ?)")) {
                ps.setString(1, b); ps.setString(2, a); ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException ex) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            throw ex;
        } finally {
            try { conn.setAutoCommit(prevAuto); } catch (SQLException ignored) {}
        }
    }

    /**
     * Remove a mutual friendship. Deletes BOTH directions atomically.
     * Silent no-op if the friendship doesn't exist.
     */
    public static void remove(String a, String b) throws SQLException {
        Connection conn = DBConnection.get();
        boolean prevAuto = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM Friend WHERE userID_1 = ? AND userID_2 = ?")) {
                ps.setString(1, a); ps.setString(2, b); ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM Friend WHERE userID_1 = ? AND userID_2 = ?")) {
                ps.setString(1, b); ps.setString(2, a); ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException ex) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            throw ex;
        } finally {
            try { conn.setAutoCommit(prevAuto); } catch (SQLException ignored) {}
        }
    }
}