package com.gameplatform.ui.panels;

import com.gameplatform.db.GameDAO;
import com.gameplatform.db.GenreDAO;
import com.gameplatform.model.Game;
import com.gameplatform.search.SearchService;
import com.gameplatform.ui.theme.GameForgeTheme;
import com.gameplatform.ui.MainFrame;
import com.gameplatform.ui.util.DarkScrollBarUI;


import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StorePanel extends JPanel {

    private final SearchService search;
    private final Map<String, Game> gamesByLowerName = new HashMap<>();

    private final JTextField searchField = new JTextField();
    private final JPanel grid = new JPanel(new GridLayout(0, 4, 16, 16));

    private final JComboBox<String> genreCombo = new JComboBox<>();
    private static final String ALL_GENRES = "All genres";

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
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(GameForgeTheme.BG_PANEL);
        bar.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        // ----- search field (centre, takes remaining width) -----
        JLabel searchLabel = new JLabel("Search ");
        searchLabel.setForeground(GameForgeTheme.TEXT_BRIGHT);
        searchLabel.setFont(GameForgeTheme.TITLE);

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

        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setOpaque(false);
        searchWrap.add(searchLabel, BorderLayout.WEST);
        searchWrap.add(searchField, BorderLayout.CENTER);

        // ----- genre dropdown (right) -----
        JLabel genreLabel = new JLabel("Genre ");
        genreLabel.setForeground(GameForgeTheme.TEXT_BRIGHT);
        genreLabel.setFont(GameForgeTheme.TITLE);

        genreCombo.setFont(GameForgeTheme.BODY);
        genreCombo.setBackground(GameForgeTheme.BG_CARD);
        genreCombo.setForeground(GameForgeTheme.TEXT_BRIGHT);
        genreCombo.setPreferredSize(new Dimension(200, 32));
        genreCombo.addActionListener(e -> refresh());

        JPanel genreWrap = new JPanel(new BorderLayout(6, 0));
        genreWrap.setOpaque(false);
        genreWrap.add(genreLabel, BorderLayout.WEST);
        genreWrap.add(genreCombo, BorderLayout.CENTER);

        bar.add(searchWrap, BorderLayout.CENTER);
        bar.add(genreWrap,  BorderLayout.EAST);
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
        scroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        scroll.getHorizontalScrollBar().setUI(new DarkScrollBarUI());
        scroll.setBackground(GameForgeTheme.BG_DARK);

        return scroll;
    }

    /** One-time load of every game so we can render any subset on demand. */
    private void loadAllGames() {
        try {
            for (Game g : GameDAO.loadAllGamesWithRatings()) {
                gamesByLowerName.put(g.getGameName().toLowerCase().trim(), g);
            }
            populateGenres();
            renderAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load games: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateGenres() throws SQLException {
        String selected = (String) genreCombo.getSelectedItem();
        genreCombo.removeAllItems();
        genreCombo.addItem(ALL_GENRES);
        for (String name : GenreDAO.listAll()) {
            genreCombo.addItem(name);
        }
        // Restore previous selection if still valid (e.g. on a reload)
        if (selected != null) {
            genreCombo.setSelectedItem(selected);
        } else {
            genreCombo.setSelectedItem(ALL_GENRES);
        }
    }

    /** Called on every keystroke. Empty input → show everything. */
    /** Called whenever search text or genre selection changes. */
    private void refresh() {
        String prefix = searchField.getText();
        String genre  = (String) genreCombo.getSelectedItem();
        boolean genreActive = genre != null && !genre.equals(ALL_GENRES);
        boolean searchActive = prefix != null && !prefix.isBlank();

        if (!searchActive && !genreActive) {
            renderAll();
            return;
        }

        // Start with full game set, then apply each active filter.
        Stream<Game> stream = gamesByLowerName.values().stream();

        if (searchActive) {
            // Use the Trie for the prefix narrowing.
            List<String> hitNames = search.autocomplete(prefix);
            Set<String> hitSet = hitNames.stream()
                    .map(s -> s.toLowerCase().trim())
                    .collect(Collectors.toSet());
            stream = stream.filter(g ->
                    hitSet.contains(g.getGameName().toLowerCase().trim()));
        }

        if (genreActive) {
            stream = stream.filter(g -> g.getGenres().contains(genre));
        }

        List<Game> filtered = stream
                .sorted((a, b) -> Double.compare(b.getAvgRating(), a.getAvgRating()))
                .collect(Collectors.toList());

        renderGames(filtered);
    }

    private void renderAll() {
        List<Game> all = gamesByLowerName.values().stream()
                .sorted((a, b) -> Double.compare(b.getAvgRating(), a.getAvgRating()))
                .collect(Collectors.toList());
        renderGames(all);
    }

    private void renderGames(List<Game> games) {
        grid.removeAll();
        if (games.isEmpty()) {
            JLabel empty = new JLabel("No matches");
            empty.setForeground(GameForgeTheme.TEXT_MUTED);
            empty.setFont(GameForgeTheme.BODY);
            grid.add(empty);
        } else {
            for (Game g : games) {
                grid.add(new GameCard(g, this::openGame));
            }
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