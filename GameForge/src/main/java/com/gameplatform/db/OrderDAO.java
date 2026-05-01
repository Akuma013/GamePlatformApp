package com.gameplatform.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Purchases. Goes through the proper Order → Order_Game chain so the
 * database triggers fire:
 *   - trg_CheckUserBalance  (INSTEAD OF on Order_Game): validates balance
 *                                                       and deducts it
 *   - add_game_to_library   (AFTER on Order_Game):     inserts Library row
 *
 * The application MUST NOT insert into Library directly — doing so
 * bypasses the balance check.
 */
public class OrderDAO {

    /**
     * Buy one game for the given customer, atomically.
     *
     * The whole flow runs in a transaction:
     *   1. Read the current gamePrice (so we can record priceAtPurchase)
     *   2. INSERT INTO [Order] and capture the generated orderID
     *   3. INSERT INTO Order_Game — this fires the triggers
     *
     * @return the new orderID
     * @throws SQLException if the game doesn't exist, the balance is
     *                      insufficient (raised by the trigger), or any
     *                      other DB error. On any failure the transaction
     *                      is rolled back.
     */
    public static int purchase(String username, int gameID) throws SQLException {
        Connection conn = DBConnection.get();
        boolean prevAuto = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            // 1. Look up the price so we can record priceAtPurchase.
            double price;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT gamePrice FROM Game WHERE gameID = ?")) {
                ps.setInt(1, gameID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Game not found.");
                    price = rs.getDouble(1);
                }
            }

            // 2. Create the Order row, capture the new orderID.
            int orderID;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO [Order](quantity, orderDate, userID) " +
                            "VALUES (1, CAST(GETDATE() AS DATE), ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException(
                            "Could not retrieve generated orderID.");
                    orderID = keys.getInt(1);
                }
            }

            // 3. Create the Order_Game row. Triggers fire here:
            //    - balance is verified and deducted
            //    - the game is auto-added to Library
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Order_Game(gameID, orderID, priceAtPurchase) " +
                            "VALUES (?, ?, ?)")) {
                ps.setInt(1, gameID);
                ps.setInt(2, orderID);
                ps.setDouble(3, price);
                ps.executeUpdate();
            }

            conn.commit();
            return orderID;

        } catch (SQLException ex) {
            // The trigger may have already issued ROLLBACK TRANSACTION on
            // insufficient balance. Calling rollback() again is a no-op
            // and safe — but wrap in try/catch just in case.
            try { conn.rollback(); } catch (SQLException ignored) {}
            throw ex;
        } finally {
            try { conn.setAutoCommit(prevAuto); } catch (SQLException ignored) {}
        }
    }
}