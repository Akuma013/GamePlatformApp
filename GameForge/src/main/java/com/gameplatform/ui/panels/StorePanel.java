package com.gameplatform.ui.panels;

import com.gameplatform.db.GameDAO;
import com.gameplatform.model.Game;
import com.gameplatform.search.SearchService;
import com.gameplatform.ui.theme.GameForgeTheme;
import com.gameplatform.ui.MainFrame;


import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorePanel extends JPanel {

    private final SearchService search;
    private final Map<String, Game> gamesByLowerName = new HashMap<>();

    private final JTextField searchField = new JTextField();
    private final JPanel grid = new JPanel(new GridLayout(0, 4, 16, 16));

    public StorePanel(SearchService search) {
        this.search = search;
        setLayout(new BorderLayout());
        setBackground(GameForgeTheme.BG_DARK);

        add(buildSearchBar(), BorderLayout.NORTH);
        add(buildGridScroller(), BorderLayout.CENTER);

        loadAllGames();
    }

    /** Top strip with the search field. */
    private JPanel buildSearchBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(GameForgeTheme.BG_PANEL);
        bar.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel label = new JLabel("Search ");
        label.setForeground(GameForgeTheme.TEXT_BRIGHT);
        label.setFont(GameForgeTheme.TITLE);

        searchField.setFont(GameForgeTheme.BODY);
        searchField.setBackground(GameForgeTheme.BG_CARD);
        searchField.setForeground(GameForgeTheme.TEXT_BRIGHT);
        searchField.setCaretColor(GameForgeTheme.TEXT_BRIGHT);
        searchField.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { refresh(); }
            public void removeUpdate(DocumentEvent e)  { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }
        });

        bar.add(label, BorderLayout.WEST);
        bar.add(searchField, BorderLayout.CENTER);
        return bar;
    }

    /** Scrollable grid of game cards. */
    private JScrollPane buildGridScroller() {
        grid.setBackground(GameForgeTheme.BG_DARK);
        grid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Wrap in a NORTH-anchored container so the grid doesn't stretch
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(GameForgeTheme.BG_DARK);
        wrap.add(grid, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrap);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(GameForgeTheme.BG_DARK);
        return scroll;
    }

    /** One-time load of every game so we can render any subset on demand. */
    private void loadAllGames() {
        try {
            for (Game g : GameDAO.loadAllGamesWithRatings()) {
                gamesByLowerName.put(g.getGameName().toLowerCase().trim(), g);
            }
            // Initial render: show everything sorted by rating
            renderAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load games: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Called on every keystroke. Empty input → show everything. */
    private void refresh() {
        String prefix = searchField.getText();
        if (prefix == null || prefix.isBlank()) {
            renderAll();
            return;
        }
        List<String> hits = search.autocomplete(prefix);
        renderByNames(hits);
    }

    private void renderAll() {
        grid.removeAll();
        gamesByLowerName.values().stream()
                .sorted((a, b) -> Double.compare(b.getAvgRating(), a.getAvgRating()))
                .forEach(g -> grid.add(new GameCard(g, this::openGame)));
        grid.revalidate();
        grid.repaint();
    }

    private void renderByNames(List<String> names) {
        grid.removeAll();
        for (String n : names) {
            Game g = gamesByLowerName.get(n.toLowerCase().trim());
            if (g != null) grid.add(new GameCard(g, this::openGame));
        }
        if (names.isEmpty()) {
            JLabel empty = new JLabel("No matches");
            empty.setForeground(GameForgeTheme.TEXT_MUTED);
            empty.setFont(GameForgeTheme.BODY);
            grid.add(empty);
        }
        grid.revalidate();
        grid.repaint();
    }

    private void openGame(int gameID) {
        gamesByLowerName.values().stream()
                .filter(g -> g.getGameID() == gameID)
                .findFirst()
                .ifPresent(g -> {
                    search.onSelect(g.getGameName());
                    new GameDetailDialog(
                            SwingUtilities.getWindowAncestor(this),
                            g,
                            () -> {
                                loadAllGames();
                                if (SwingUtilities.getWindowAncestor(this) instanceof MainFrame mf) {
                                    mf.refreshBalance();
                                }
                            }
                    ).setVisible(true);
                });
    }
}