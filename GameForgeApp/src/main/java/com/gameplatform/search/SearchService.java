package com.gameplatform.search;

import com.gamedb.trie.FuzzySearch;
import com.gamedb.trie.Trie;
import com.gameplatform.db.GameDAO;
import com.gameplatform.model.Game;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bridge between the Game database and the Trie-based search engine.
 *
 * - On startup, buildIndex() loads every game (with its average rating)
 *   and inserts each one into the Trie. Average rating is normalised to
 *   the 1..1000 frequency scale used by the Trie project.
 *
 * - autocomplete(prefix) is what the search bar calls on every keystroke.
 *   It first tries an exact prefix match; if that returns nothing, it
 *   falls back to fuzzy search (edit-distance ≤ 1).
 *
 * - onSelect(name) gives that name a +200 frequency boost so it floats
 *   to the top of future suggestions for the same prefix (the "recently
 *   searched" feature from the Trie spec).
 */
public class SearchService {

    private static final int RECENT_SEARCH_BOOST = 200;
    private static final int MAX_RATING = 10;     // Review.rating is 1..5

    private final Trie trie = new Trie();
    private final FuzzySearch fuzzy = new FuzzySearch(trie);
    private final Map<String, Integer> nameToId = new HashMap<>();


    /**
     * Load all games from the database and insert them into the Trie.
     * Call this once after the user logs in.
     */
    public void buildIndex() throws SQLException {
        for (Game g : GameDAO.loadAllGamesWithRatings()) {
            trie.insert(g.getGameName(), normalize(g.getAvgRating()));
            nameToId.put(g.getGameName().toLowerCase().trim(), g.getGameID());
        }
    }

    /**
     * Map a 0..5 rating to a 1..1000 frequency. Unrated games still get
     * frequency 1 so they remain searchable, just at the bottom.
     */
    private int normalize(double rating) {
        if (rating <= 0) return 1;
        return (int) Math.round((rating / MAX_RATING) * 1000);
    }

    /**
     * Top-5 completions for the given prefix.
     * Falls back to fuzzy search (1-edit tolerance) if exact match
     * yields no results.
     */
    public List<String> autocomplete(String prefix) {
        if (prefix == null || prefix.isBlank()) return List.of();
        List<String> exact = trie.topKCompletions(prefix, 10);
        return exact.isEmpty() ? fuzzy.fuzzyTopK(prefix, 10) : exact;
    }

    /**
     * Called when the user clicks a suggestion.
     * Returns the gameID for the UI, and boosts the term's frequency
     * so it ranks higher next time.
     */
    public Integer onSelect(String name) {
        if (name == null) return null;
        trie.insert(name, RECENT_SEARCH_BOOST);
        return nameToId.get(name.toLowerCase().trim());
    }

    /** How many games are currently indexed. Useful for diagnostics. */
    public int size() {
        return nameToId.size();
    }
}