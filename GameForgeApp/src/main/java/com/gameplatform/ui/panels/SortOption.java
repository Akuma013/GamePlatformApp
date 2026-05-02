package com.gameplatform.ui.panels;

import com.gameplatform.model.Game;
import java.util.Comparator;

/**

 Sort options for the Store results grid.
 The display name is shown in the dropdown; the comparator drives the sort.*/
public enum SortOption {
    RATING_DESC ("Highest rated",    Comparator.comparingDouble(Game::getAvgRating).reversed()),
    PRICE_ASC   ("Price (low → high)", Comparator.comparingDouble(Game::getPrice)),
    PRICE_DESC  ("Price (high → low)", Comparator.comparingDouble(Game::getPrice).reversed()),
    NAME_ASC    ("Name (A → Z)",      Comparator.comparing(Game::getGameName,
            String.CASE_INSENSITIVE_ORDER));

    private final String label;
    private final Comparator<Game> comparator;

    SortOption(String label, Comparator<Game> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public Comparator<Game> comparator() { return comparator; }

    @Override
    public String toString() { return label; }   // the dropdown shows this
}