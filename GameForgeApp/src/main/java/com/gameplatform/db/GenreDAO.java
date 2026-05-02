package com.gameplatform.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Genre lookups. Most callers use loadGenresByGameId() which returns
 * a map keyed by gameID, so a Store render of 50 games costs one query
 * instead of 50.
 */
public class GenreDAO {

    /** All distinct genre names, alphabetical. Used to populate the
     *  genre filter dropdown in the Store. */
    public static List<String> listAll() throws SQLException {
        List<String> out = new ArrayList<>();
        String sql = "SELECT genreName FROM Genre ORDER BY genreName";
        try (Statement st = DBConnection.get().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) out.add(rs.getString(1));
        }
        return out;
    }

    /** Genres for a single game (used by the detail dialog). */
    public static List<String> listForGame(int gameID) throws SQLException {
        List<String> out = new ArrayList<>();
        String sql =
                "SELECT gen.genreName " +
                        "FROM Game_Genre gg JOIN Genre gen ON gen.genreID = gg.genreID " +
                        "WHERE gg.gameID = ? " +
                        "ORDER BY gen.genreName";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setInt(1, gameID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getString(1));
            }
        }
        return out;
    }

    /**
     * Bulk lookup: gameID → list of genre names.
     * Use this in the Store's loadAllGames so we don't issue 50 round-trips.
     */
    public static Map<Integer, List<String>> loadGenresByGameId() throws SQLException {
        Map<Integer, List<String>> out = new HashMap<>();
        String sql =
                "SELECT gg.gameID, gen.genreName " +
                        "FROM Game_Genre gg JOIN Genre gen ON gen.genreID = gg.genreID " +
                        "ORDER BY gg.gameID, gen.genreName";
        try (Statement st = DBConnection.get().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                out.computeIfAbsent(id, k -> new ArrayList<>()).add(name);
            }
        }
        return out;
    }
}
