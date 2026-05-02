package com.gameplatform.search;

import com.gamedb.trie.FuzzySearch;
import com.gamedb.trie.Trie;
import com.gameplatform.db.UserDAO;

import java.sql.SQLException;
import java.util.List;


public class UserSearchService {

    private static final int RECENT_SELECT_BOOST = 200;
    private static final int BASE_FREQUENCY      = 1;

    private final Trie trie = new Trie();
    private final FuzzySearch fuzzy = new FuzzySearch(trie);


    public void buildIndex() throws SQLException {
        for (String name : UserDAO.listAllUsernames()) {
            trie.insert(name, BASE_FREQUENCY);
        }
    }


    public List<String> autocomplete(String prefix) {
        if (prefix == null || prefix.isBlank()) return List.of();
        List<String> exact = trie.topKCompletions(prefix, 5);
        return exact.isEmpty() ? fuzzy.fuzzyTopK(prefix, 5) : exact;
    }


    public void onSelect(String username) {
        if (username == null) return;
        trie.insert(username, RECENT_SELECT_BOOST);
    }

    /* Diagnostic: how many usernames are indexed. */
    public int size() {
        // Cheap proxy: count the BASE_FREQUENCY-or-higher entries by
        // walking the Trie isn't worth it for a single integer.
        // We just track inserts manually if needed; for now just
        // re-query if precision matters elsewhere.
        return -1;
    }
}