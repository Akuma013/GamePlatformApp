package com.gameplatform.ui.panels;

import com.gameplatform.db.DBConnection;
import com.gameplatform.db.GameDAO;
import com.gameplatform.db.GenreDAO;
import com.gameplatform.model.Game;
import com.gameplatform.search.SearchService;
import com.gameplatform.search.StoreSectionsService;
import com.gameplatform.ui.MainFrame;
import com.gameplatform.ui.theme.GameForgeTheme;
import com.gameplatform.ui.util.DarkScrollBarUI;
import com.gameplatform.ui.util.WrapLayout;

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

    private static final String ALL_GENRES = "All genres";

    private final SearchService search;
    private final StoreSectionsService sections = new StoreSectionsService();

    // Cached game catalog — used by the flat-grid search/filter mode.
    private final Map<String, Game> gamesByLowerName = new HashMap<>();

    // Top bar controls
    private final JTextField searchField = new JTextField();
    private final JComboBox<String> genreCombo = new JComboBox<>();
    private final JComboBox<SortOption> sortCombo = new JComboBox<>(SortOption.values());

    // Body — we swap this between browse and results modes
    private final JPanel body = new JPanel(new BorderLayout());
    private final JPanel browseContent = new JPanel();
    private final JPanel resultsGrid = new JPanel(new WrapLayout(FlowLayout.LEFT, 16, 16));

    public StorePanel(SearchService search) {
        this.search = search;

        setLayout(new BorderLayout());
        setBackground(GameForgeTheme.BG_DARK);

        add(buildSearchBar(), BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);

        body.setBackground(GameForgeTheme.BG_DARK);
        browseContent.setLayout(new BoxLayout(browseContent, BoxLayout.Y_AXIS));
        browseContent.setBackground(GameForgeTheme.BG_DARK);
        resultsGrid.setBackground(GameForgeTheme.BG_DARK);
        resultsGrid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        loadCatalog();
    }

    // =================================================================== //
    //  TOP BAR                                                             //
    // =================================================================== //

    private JPanel buildSearchBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(GameForgeTheme.BG_PANEL);
        bar.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        // ----- search field (centre) -----
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

        // ----- genre + sort (right) -----
        JLabel genreLabel = new JLabel("Genre ");
        genreLabel.setForeground(GameForgeTheme.TEXT_BRIGHT);
        genreLabel.setFont(GameForgeTheme.TITLE);

        genreCombo.setFont(GameForgeTheme.BODY);
        genreCombo.setBackground(GameForgeTheme.BG_CARD);
        genreCombo.setForeground(GameForgeTheme.TEXT_BRIGHT);
        genreCombo.setPreferredSize(new Dimension(180, 32));
        genreCombo.addActionListener(e -> refresh());

        JLabel sortLabel = new JLabel("Sort ");
        sortLabel.setForeground(GameForgeTheme.TEXT_BRIGHT);
        sortLabel.setFont(GameForgeTheme.TITLE);

        sortCombo.setFont(GameForgeTheme.BODY);
        sortCombo.setBackground(GameForgeTheme.BG_CARD);
        sortCombo.setForeground(GameForgeTheme.TEXT_BRIGHT);
        sortCombo.setPreferredSize(new Dimension(170, 32));
        sortCombo.addActionListener(e -> {
            // Sort only matters in results mode; refresh the grid.
            if (isResultsMode()) refresh();
        });

        JPanel filtersWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filtersWrap.setOpaque(false);
        filtersWrap.add(genreLabel);
        filtersWrap.add(genreCombo);
        filtersWrap.add(Box.createHorizontalStrut(8));
        filtersWrap.add(sortLabel);
        filtersWrap.add(sortCombo);

        bar.add(searchWrap, BorderLayout.CENTER);
        bar.add(filtersWrap, BorderLayout.EAST);
        return bar;
    }

    // =================================================================== //
    //  CATALOG LOADING                                                     //
    // =================================================================== //

    private void loadCatalog() {
        try {
            for (Game g : GameDAO.loadAllGamesWithRatings()) {
                gamesByLowerName.put(g.getGameName().toLowerCase().trim(), g);
            }
            populateGenres();
            renderBrowseMode();   // initial view
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
        if (selected != null) genreCombo.setSelectedItem(selected);
        else genreCombo.setSelectedItem(ALL_GENRES);
    }

    // =================================================================== //
    //  MODE SWITCHING                                                      //
    // =================================================================== //

    /** True when the user has typed something OR picked a non-default genre. */
    private boolean isResultsMode() {
        String prefix = searchField.getText();
        String genre  = (String) genreCombo.getSelectedItem();
        return (prefix != null && !prefix.isBlank())
                || (genre != null && !genre.equals(ALL_GENRES));
    }

    /** Called whenever search text, genre, or sort changes. */
    private void refresh() {
        if (isResultsMode()) {
            renderResultsMode();
        } else {
            renderBrowseMode();
        }
    }

    // =================================================================== //
    //  BROWSE MODE — curated sections                                      //
    // =================================================================== //

    private void renderBrowseMode() {
        body.removeAll();
        browseContent.removeAll();

        try {
            String me = DBConnection.getAppUsername();

            // 1. Top Rated
            browseContent.add(new SectionRow(
                    "Top Rated",
                    "Critics' favourites",
                    sections.topRated(),
                    this::openGame));

            // 2. Recommended (only if non-empty — handled inside SectionRow)
            List<Game> recs = sections.recommended(me);
            if (!recs.isEmpty()) {
                browseContent.add(new SectionRow(
                        "Recommended for You",
                        "Based on what's in your library",
                        recs,
                        this::openGame));
            }

            // 3. New Arrivals
            browseContent.add(new SectionRow(
                    "New Arrivals",
                    "Latest additions to the catalog",
                    sections.newArrivals(),
                    this::openGame));

            // 4. Budget Picks
            browseContent.add(new SectionRow(
                    "Budget Picks",
                    "Top-rated games under $20",
                    sections.budgetPicks(),
                    this::openGame));

            browseContent.add(Box.createVerticalStrut(20));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load store sections: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Wrap in a NORTH-anchored container + scroll pane
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(GameForgeTheme.BG_DARK);
        wrap.add(browseContent, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrap);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(GameForgeTheme.BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());

        body.add(scroll, BorderLayout.CENTER);
        body.revalidate();
        body.repaint();
    }

    // =================================================================== //
    //  RESULTS MODE — flat grid                                            //
    // =================================================================== //

    private void renderResultsMode() {
        body.removeAll();
        resultsGrid.removeAll();

        String prefix = searchField.getText();
        String genre  = (String) genreCombo.getSelectedItem();
        SortOption sort = (SortOption) sortCombo.getSelectedItem();

        boolean searchActive = prefix != null && !prefix.isBlank();
        boolean genreActive  = genre != null && !genre.equals(ALL_GENRES);

        Stream<Game> stream = gamesByLowerName.values().stream();

        if (searchActive) {
            List<String> hitNames = search.autocomplete(prefix);
            Set<String> hitSet = hitNames.stream()
                    .map(s -> s.toLowerCase().trim())
                    .collect(Collectors.toSet());
            stream = stream.filter(g -> hitSet.contains(
                    g.getGameName().toLowerCase().trim()));
        }
        if (genreActive) {
            stream = stream.filter(g -> g.getGenres().contains(genre));
        }

        List<Game> filtered = stream
                .sorted(sort == null ? SortOption.RATING_DESC.comparator()
                        : sort.comparator())
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            JLabel empty = new JLabel("No matches");
            empty.setFont(GameForgeTheme.BODY);
            empty.setForeground(GameForgeTheme.TEXT_MUTED);
            resultsGrid.add(empty);
        } else {
            for (Game g : filtered) {
                resultsGrid.add(new GameCard(g, this::openGame));
            }
        }

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(GameForgeTheme.BG_DARK);
        wrap.add(resultsGrid, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrap);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(GameForgeTheme.BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());

        body.add(scroll, BorderLayout.CENTER);
        body.revalidate();
        body.repaint();
    }

    // =================================================================== //
    //  CARD CLICK                                                          //
    // =================================================================== //

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
                                // After purchase/wishlist/review, refresh:
                                // - reload the catalog (ratings may have changed)
                                // - re-render the current mode (sections or results)
                                // - bump the header balance
                                loadCatalog();
                                if (SwingUtilities.getWindowAncestor(this) instanceof MainFrame mf) {
                                    mf.refreshBalance();
                                }
                            }
                    ).setVisible(true);
                });
    }
}