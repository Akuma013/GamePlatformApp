package com.gameplatform.ui.panels;

import com.gameplatform.db.DBConnection;

import com.gameplatform.db.WishlistDAO;
import com.gameplatform.model.Game;
import com.gameplatform.ui.theme.GameForgeTheme;
import com.gameplatform.ui.MainFrame;


import javax.swing.*;
import java.awt.*;
import java.util.List;

public class WishlistPanel extends JPanel {

    private final JPanel grid = new JPanel(new GridLayout(0, 4, 16, 16));

    public WishlistPanel() {
        setLayout(new BorderLayout());
        setBackground(GameForgeTheme.BG_DARK);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildGridScroller(), BorderLayout.CENTER);

        reload();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(GameForgeTheme.BG_PANEL);
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel title = new JLabel("My Wishlist");
        title.setFont(GameForgeTheme.HEADER);
        title.setForeground(GameForgeTheme.TEXT_BRIGHT);

        JButton refresh = new JButton("Refresh");
        refresh.setBackground(GameForgeTheme.BG_CARD);
        refresh.setForeground(GameForgeTheme.TEXT_BRIGHT);
        refresh.setFocusPainted(false);
        refresh.setOpaque(true);
        refresh.setContentAreaFilled(true);
        refresh.setBorderPainted(false);
        refresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refresh.addActionListener(e -> reload());

        header.add(title, BorderLayout.WEST);
        header.add(refresh, BorderLayout.EAST);
        return header;
    }

    private JScrollPane buildGridScroller() {
        grid.setBackground(GameForgeTheme.BG_DARK);
        grid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(GameForgeTheme.BG_DARK);
        wrap.add(grid, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrap);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(GameForgeTheme.BG_DARK);
        return scroll;
    }

    private void reload() {
        grid.removeAll();
        try {
            List<Game> games = WishlistDAO.listForUser(DBConnection.getAppUsername());
            if (games.isEmpty()) {
                showEmptyState();
            } else {
                for (Game g : games) {
                    grid.add(new GameCard(g, this::handleClick));
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load wishlist: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        grid.revalidate();
        grid.repaint();
    }

    private void showEmptyState() {
        JLabel empty = new JLabel(
                "<html><div style='text-align:center;'>"
                        + "Your wishlist is empty.<br/>"
                        + "<span style='color:#8f98a0;'>"
                        + "Save games from the Store to come back to them later.</span>"
                        + "</div></html>",
                SwingConstants.CENTER);
        empty.setForeground(GameForgeTheme.TEXT_BRIGHT);
        empty.setFont(GameForgeTheme.TITLE);
        grid.add(empty);
    }

    /**
     * Click on a wishlist card → offer two actions:
     *   - Move to Library (purchases the game)
     *   - Remove (just deletes the wishlist row)
     * Step 13 replaces this with the real detail dialog.
     */
    private void handleClick(int gameID) {
        try {
            var game = com.gameplatform.db.GameDAO.findById(gameID);
            if (game == null) return;
            new GameDetailDialog(
                    SwingUtilities.getWindowAncestor(this),
                    game,
                    () -> {
                        reload();
                        if (SwingUtilities.getWindowAncestor(this) instanceof MainFrame mf) {
                            mf.refreshBalance();
                        }
                    }
            ).setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}