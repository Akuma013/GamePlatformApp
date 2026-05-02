package com.gameplatform.ui.panels;

import com.gameplatform.db.DBConnection;
import com.gameplatform.db.LibraryDAO;
import com.gameplatform.db.UserDAO;
import com.gameplatform.ui.theme.GameForgeTheme;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * The profile-style stats strip at the top of the Library tab.
 *
 * Layout:
 *   [Welcome row, two lines]                              [4 stat tiles]
 *
 * The tiles show: Games owned | Favorites | Total playtime | Top genre
 */
public class LibraryStatsPanel extends JPanel {

    public LibraryStatsPanel() {
        setLayout(new BorderLayout());
        setBackground(GameForgeTheme.BG_PANEL);
        setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        try {
            LibraryDAO.LibraryStats stats =
                    LibraryDAO.getStats(DBConnection.getAppUsername());
            String nickname = UserDAO.getNickname(DBConnection.getAppUsername());

            add(buildWelcome(nickname), BorderLayout.WEST);
            add(buildTiles(stats), BorderLayout.EAST);
        } catch (SQLException ex) {
            JLabel err = new JLabel("Could not load stats: " + ex.getMessage());
            err.setForeground(new Color(0xff6b6b));
            add(err, BorderLayout.CENTER);
        }
    }

    private JComponent buildWelcome(String nickname) {
        JPanel welcome = new JPanel();
        welcome.setLayout(new BoxLayout(welcome, BoxLayout.Y_AXIS));
        welcome.setOpaque(false);

        JLabel hi = new JLabel("Welcome back, " + nickname);
        hi.setFont(new Font("Segoe UI", Font.BOLD, 24));
        hi.setForeground(GameForgeTheme.TEXT_BRIGHT);
        hi.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Your gaming profile at a glance");
        sub.setFont(GameForgeTheme.SMALL);
        sub.setForeground(GameForgeTheme.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        welcome.add(hi);
        welcome.add(sub);
        return welcome;
    }

    private JComponent buildTiles(LibraryDAO.LibraryStats stats) {
        JPanel tiles = new JPanel(new GridLayout(1, 4, 16, 0));
        tiles.setOpaque(false);

        tiles.add(buildTile("Games owned",
                String.valueOf(stats.gamesOwned),
                GameForgeTheme.ACCENT));
        tiles.add(buildTile("Favorites",
                String.valueOf(stats.favoritesCount),
                new Color(0xffd24d)));    // gold, matches the favorite star
        tiles.add(buildTile("Total playtime",
                formatPlayTime(stats.totalPlayTime),
                new Color(0x86b418)));    // money green
        tiles.add(buildTile("Top genre",
                stats.topGenre == null ? "—" : stats.topGenre,
                GameForgeTheme.TEXT_BRIGHT));

        return tiles;
    }

    private JComponent buildTile(String label, String value, Color valueColor) {
        JPanel tile = new JPanel();
        tile.setLayout(new BoxLayout(tile, BoxLayout.Y_AXIS));
        tile.setBackground(GameForgeTheme.BG_CARD);
        tile.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JLabel labelText = new JLabel(label);
        labelText.setFont(GameForgeTheme.SMALL);
        labelText.setForeground(GameForgeTheme.TEXT_MUTED);
        labelText.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueText = new JLabel(value);
        valueText.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueText.setForeground(valueColor);
        valueText.setAlignmentX(Component.LEFT_ALIGNMENT);

        tile.add(labelText);
        tile.add(Box.createVerticalStrut(4));
        tile.add(valueText);
        return tile;
    }

    /** Format minutes as "Xh Ym" for the "total playtime" tile. */
    private static String formatPlayTime(int minutes) {
        if (minutes <= 0) return "0 h";
        int hours = minutes / 60;
        int mins  = minutes % 60;
        if (hours == 0) return mins + " min";
        if (mins  == 0) return hours + " h";
        return hours + " h " + mins + " min";
    }
}