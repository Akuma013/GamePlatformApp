package com.gameplatform.ui.panels;

import com.gameplatform.db.DBConnection;
import com.gameplatform.db.FriendDAO;
import com.gameplatform.db.UserDAO;
import com.gameplatform.search.UserSearchService;
import com.gameplatform.ui.theme.GameForgeTheme;
import com.gameplatform.ui.util.DarkScrollBarUI;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Two-pane friends manager. Left: current friends list with Remove buttons.
 * Right: Trie-backed username search with Add button on each suggestion.
 *
 * The dialog calls onChange.run() whenever a friendship is created or
 * removed, so the header (or whatever opened us) can refresh as needed.
 */
public class FriendsDialog extends JDialog {

    private static final int DIALOG_W = 800;
    private static final int DIALOG_H = 540;

    private final UserSearchService userSearch;
    private final Runnable onChange;

    private final DefaultListModel<String> friendsModel    = new DefaultListModel<>();
    private final DefaultListModel<String> suggestionModel = new DefaultListModel<>();
    private final JList<String> friendsList    = new JList<>(friendsModel);
    private final JList<String> suggestionList = new JList<>(suggestionModel);
    private final JTextField searchField       = new JTextField();
    private final JLabel statusLabel           = new JLabel(" ");

    public FriendsDialog(Window owner, UserSearchService userSearch, Runnable onChange) {
        super(owner, "Friends", ModalityType.APPLICATION_MODAL);
        this.userSearch = userSearch;
        this.onChange = onChange;

        setSize(DIALOG_W, DIALOG_H);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(GameForgeTheme.BG_DARK);
        setContentPane(root);

        root.add(buildHeader(),     BorderLayout.NORTH);
        root.add(buildBody(),       BorderLayout.CENTER);
        root.add(buildFooter(),     BorderLayout.SOUTH);

        reloadFriends();
    }

    // ============================================================ //
    //  HEADER                                                      //
    // ============================================================ //

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(GameForgeTheme.BG_PANEL);
        header.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        JLabel title = new JLabel("Friends");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(GameForgeTheme.TEXT_BRIGHT);
        header.add(title, BorderLayout.WEST);

        JLabel subtitle = new JLabel("Signed in as " + DBConnection.getAppUsername());
        subtitle.setFont(GameForgeTheme.SMALL);
        subtitle.setForeground(GameForgeTheme.TEXT_MUTED);
        header.add(subtitle, BorderLayout.EAST);

        return header;
    }

    // ============================================================ //
    //  BODY (two panes)                                            //
    // ============================================================ //

    private JComponent buildBody() {
        JPanel body = new JPanel(new GridLayout(1, 2, 16, 0));
        body.setBackground(GameForgeTheme.BG_DARK);
        body.setBorder(BorderFactory.createEmptyBorder(18, 22, 10, 22));

        body.add(buildLeftPane());
        body.add(buildRightPane());
        return body;
    }

    /** Left pane: current friends list with Remove buttons. */
    private JComponent buildLeftPane() {
        JPanel pane = new JPanel(new BorderLayout(0, 8));
        pane.setOpaque(false);

        JLabel heading = new JLabel("Your friends");
        heading.setFont(GameForgeTheme.TITLE);
        heading.setForeground(GameForgeTheme.TEXT_BRIGHT);
        pane.add(heading, BorderLayout.NORTH);

        friendsList.setBackground(GameForgeTheme.BG_CARD);
        friendsList.setForeground(GameForgeTheme.TEXT_BRIGHT);
        friendsList.setFont(GameForgeTheme.BODY);
        friendsList.setCellRenderer(new FriendCellRenderer());
        friendsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        friendsList.setSelectionBackground(GameForgeTheme.BG_PANEL);
        friendsList.setSelectionForeground(GameForgeTheme.TEXT_BRIGHT);
        friendsList.setFixedCellHeight(36);

        JScrollPane scroll = new JScrollPane(friendsList);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(GameForgeTheme.BG_CARD);
        scroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        pane.add(scroll, BorderLayout.CENTER);

        JButton removeBtn = styled("Remove selected", GameForgeTheme.BG_CARD,
                GameForgeTheme.TEXT_BRIGHT, e -> doRemove());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(removeBtn);
        pane.add(btnRow, BorderLayout.SOUTH);

        return pane;
    }

    /** Right pane: search bar + suggestion list + Add button. */
    private JComponent buildRightPane() {
        JPanel pane = new JPanel(new BorderLayout(0, 8));
        pane.setOpaque(false);

        JLabel heading = new JLabel("Find friends");
        heading.setFont(GameForgeTheme.TITLE);
        heading.setForeground(GameForgeTheme.TEXT_BRIGHT);
        pane.add(heading, BorderLayout.NORTH);

        // ---- search field ----
        searchField.setFont(GameForgeTheme.BODY);
        searchField.setBackground(GameForgeTheme.BG_CARD);
        searchField.setForeground(GameForgeTheme.TEXT_BRIGHT);
        searchField.setCaretColor(GameForgeTheme.TEXT_BRIGHT);
        searchField.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { refreshSuggestions(); }
            public void removeUpdate(DocumentEvent e)  { refreshSuggestions(); }
            public void changedUpdate(DocumentEvent e) { refreshSuggestions(); }
        });

        // ---- suggestion list ----
        suggestionList.setBackground(GameForgeTheme.BG_CARD);
        suggestionList.setForeground(GameForgeTheme.TEXT_BRIGHT);
        suggestionList.setFont(GameForgeTheme.BODY);
        suggestionList.setCellRenderer(new SuggestionCellRenderer());
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setSelectionBackground(GameForgeTheme.BG_PANEL);
        suggestionList.setSelectionForeground(GameForgeTheme.TEXT_BRIGHT);
        suggestionList.setFixedCellHeight(36);

        JScrollPane sugScroll = new JScrollPane(suggestionList);
        sugScroll.setBorder(null);
        sugScroll.getViewport().setBackground(GameForgeTheme.BG_CARD);
        sugScroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(searchField, BorderLayout.NORTH);
        center.add(sugScroll,   BorderLayout.CENTER);
        pane.add(center, BorderLayout.CENTER);

        JButton addBtn = styled("Add as friend", GameForgeTheme.BTN_GREEN,
                Color.WHITE, e -> doAdd());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(addBtn);
        pane.add(btnRow, BorderLayout.SOUTH);

        return pane;
    }

    // ============================================================ //
    //  FOOTER                                                      //
    // ============================================================ //

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(GameForgeTheme.BG_PANEL);
        footer.setBorder(BorderFactory.createEmptyBorder(8, 22, 8, 22));

        statusLabel.setFont(GameForgeTheme.SMALL);
        statusLabel.setForeground(GameForgeTheme.TEXT_MUTED);
        footer.add(statusLabel, BorderLayout.WEST);

        JButton close = styled("Close", GameForgeTheme.BG_CARD,
                GameForgeTheme.TEXT_BRIGHT, e -> dispose());
        footer.add(close, BorderLayout.EAST);

        return footer;
    }

    // ============================================================ //
    //  ACTIONS                                                     //
    // ============================================================ //

    private void refreshSuggestions() {
        suggestionModel.clear();
        String prefix = searchField.getText();
        if (prefix == null || prefix.isBlank()) return;

        String me = DBConnection.getAppUsername();
        List<String> hits = userSearch.autocomplete(prefix);
        for (String name : hits) {
            // Filter out the current user — can't befriend yourself
            if (!name.equalsIgnoreCase(me)) {
                suggestionModel.addElement(name);
            }
        }
    }

    private void reloadFriends() {
        friendsModel.clear();
        try {
            for (String name : FriendDAO.listFriends(DBConnection.getAppUsername())) {
                friendsModel.addElement(name);
            }
            setStatus(friendsModel.size() + " friend"
                    + (friendsModel.size() == 1 ? "" : "s"), false);
        } catch (SQLException ex) {
            setStatus("Could not load friends: " + ex.getMessage(), true);
        }
    }

    private void doAdd() {
        String picked = suggestionList.getSelectedValue();
        if (picked == null) {
            setStatus("Pick a username from the list first.", true);
            return;
        }
        String me = DBConnection.getAppUsername();
        try {
            // Defence in depth — we already filter in refreshSuggestions
            if (!UserDAO.exists(picked)) {
                setStatus("That user doesn't exist.", true);
                return;
            }
            if (FriendDAO.areFriends(me, picked)) {
                setStatus("You're already friends with " + picked + ".", true);
                return;
            }
            FriendDAO.add(me, picked);
            userSearch.onSelect(picked);    // boost in the Trie
            setStatus("Added " + picked + " as a friend.", false);

            reloadFriends();
            searchField.setText("");
            suggestionModel.clear();

            if (onChange != null) onChange.run();
        } catch (SQLException ex) {
            setStatus("Could not add: " + ex.getMessage(), true);
        }
    }

    private void doRemove() {
        String picked = friendsList.getSelectedValue();
        if (picked == null) {
            setStatus("Pick a friend to remove first.", true);
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Remove " + picked + " from your friends?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;

        try {
            FriendDAO.remove(DBConnection.getAppUsername(), picked);
            setStatus("Removed " + picked + ".", false);
            reloadFriends();
            if (onChange != null) onChange.run();
        } catch (SQLException ex) {
            setStatus("Could not remove: " + ex.getMessage(), true);
        }
    }

    private void setStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setForeground(isError
                ? new Color(0xff6b6b)
                : GameForgeTheme.TEXT_MUTED);
    }

    // ============================================================ //
    //  CELL RENDERERS                                              //
    // ============================================================ //

    /** Renders friends as a row with a small icon-style prefix. */
    private static class FriendCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JLabel l = (JLabel) super.getListCellRendererComponent(
                    list, "  ●  " + value, index, isSelected, cellHasFocus);
            l.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            if (!isSelected) {
                l.setBackground(GameForgeTheme.BG_CARD);
                l.setForeground(GameForgeTheme.TEXT_BRIGHT);
            }
            return l;
        }
    }

    private static class SuggestionCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JLabel l = (JLabel) super.getListCellRendererComponent(
                    list, "  " + value, index, isSelected, cellHasFocus);
            l.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            if (!isSelected) {
                l.setBackground(GameForgeTheme.BG_CARD);
                l.setForeground(GameForgeTheme.TEXT_BRIGHT);
            }
            return l;
        }
    }

    private JButton styled(String text, Color bg, Color fg,
                           java.awt.event.ActionListener handler) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(fg);
        b.setFont(GameForgeTheme.TITLE);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(handler);
        return b;
    }
}