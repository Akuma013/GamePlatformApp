package com.gameplatform.ui.panels;

import com.gameplatform.db.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class LibraryPanel extends BasePanel {

    // Library PK is composite (userID, gameID) - no single LibraryID
    private JTextField userIdField, gameIdField, playTimeField;
    private JCheckBox favoriteBox;

    public LibraryPanel() {
        buildUI();
        refresh();
    }

    private void buildUI() {
        JLabel title = new JLabel("Library");
        title.setFont(new Font("Dialog", Font.BOLD, 18));
        title.setForeground(TEXT_MAIN);
        title.setBorder(new EmptyBorder(0, 0, 14, 0));
        add(title, BorderLayout.NORTH);

        // userID, gameID, gameName, playTime, favorite
        JScrollPane sp = buildTable(new String[]{
            "userID", "gameID", "Game Name", "Play Time (min)", "Favorite"
        });
        add(sp, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(BG_DARK);

        JPanel form = new JPanel(new BorderLayout());
        form.setBackground(BG_CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0,BORDER_CLR),
            new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel formTitle = new JLabel("Manage Library Entry");
        formTitle.setFont(new Font("Dialog", Font.BOLD, 13));
        formTitle.setForeground(TEXT_MUTED);
        formTitle.setBorder(new EmptyBorder(0,0,8,0));

        userIdField  = makeField(120);
        gameIdField  = makeField(80);
        playTimeField= makeField(80);
        favoriteBox  = new JCheckBox("Favorite");
        favoriteBox.setBackground(BG_CARD);
        favoriteBox.setForeground(TEXT_MAIN);
        favoriteBox.setFont(new Font("Dialog", Font.PLAIN, 13));

        JPanel fields = buildFormRow(
            makeLabel("User ID:"),    userIdField,
            makeLabel("Game ID:"),    gameIdField,
            makeLabel("Play Time:"),  playTimeField,
            favoriteBox
        );

        JButton addBtn     = makeBtn("Add to Library",   SUCCESS);
        JButton updateBtn  = makeBtn("Update Entry",     WARN);
        JButton removeBtn  = makeBtn("Remove",           ERROR_CLR);
        JButton refreshBtn = makeBtn("Refresh",          new Color(60,70,100));
        refreshBtn.setForeground(TEXT_MUTED);

        addBtn.addActionListener(e -> addEntry());
        updateBtn.addActionListener(e -> updateEntry());
        removeBtn.addActionListener(e -> removeEntry());
        refreshBtn.addActionListener(e -> refresh());

        table.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    userIdField.setText(safe(tableModel.getValueAt(row, 0)));
                    gameIdField.setText(safe(tableModel.getValueAt(row, 1)));
                    playTimeField.setText(safe(tableModel.getValueAt(row, 3)));
                    Object fav = tableModel.getValueAt(row, 4);
                    favoriteBox.setSelected(fav != null && (fav.toString().equals("true") || fav.toString().equals("1")));
                }
            }
        });

        JPanel buttons = buildFormRow(addBtn, updateBtn, removeBtn, refreshBtn);

        form.add(formTitle, BorderLayout.NORTH);
        form.add(fields,    BorderLayout.CENTER);
        form.add(buttons,   BorderLayout.SOUTH);

        south.add(form, BorderLayout.NORTH);
        south.add(buildStatusBar(), BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);
    }

    private String safe(Object o) { return o == null ? "" : o.toString(); }

    private void refresh() {
        loadTable("""
            SELECT l.userID, l.gameID, g.gameName, l.playTime, l.favorite
            FROM Library l
            JOIN Game g ON l.gameID = g.gameID
            ORDER BY l.userID, g.gameName
        """);
    }

    private void addEntry() {
        String uid = userIdField.getText().trim();
        String gid = gameIdField.getText().trim();
        String pt  = playTimeField.getText().trim();
        if (uid.isEmpty() || gid.isEmpty()) { setStatus("User ID and Game ID are required.", ERROR_CLR); return; }

        String sql = "INSERT INTO Library (userID, gameID, playTime, favorite) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setString(1, uid);
            ps.setInt(2, Integer.parseInt(gid));
            ps.setObject(3, pt.isEmpty() ? 0 : Integer.parseInt(pt));
            ps.setBoolean(4, favoriteBox.isSelected());
            ps.executeUpdate();
            setStatus("Entry added successfully.", SUCCESS);
            refresh();
        } catch (Exception e) {
            setStatus("Error: " + e.getMessage(), ERROR_CLR);
        }
    }

    private void updateEntry() {
        String uid = userIdField.getText().trim();
        String gid = gameIdField.getText().trim();
        String pt  = playTimeField.getText().trim();
        if (uid.isEmpty() || gid.isEmpty()) { setStatus("Select a row to update.", WARN); return; }

        String sql = "UPDATE Library SET playTime = ?, favorite = ? WHERE userID = ? AND gameID = ?";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setObject(1, pt.isEmpty() ? 0 : Integer.parseInt(pt));
            ps.setBoolean(2, favoriteBox.isSelected());
            ps.setString(3, uid);
            ps.setInt(4, Integer.parseInt(gid));
            ps.executeUpdate();
            setStatus("Entry updated.", SUCCESS);
            refresh();
        } catch (Exception e) {
            setStatus("Error: " + e.getMessage(), ERROR_CLR);
        }
    }

    private void removeEntry() {
        String uid = userIdField.getText().trim();
        String gid = gameIdField.getText().trim();
        if (uid.isEmpty() || gid.isEmpty()) { setStatus("Select a row to remove.", WARN); return; }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Remove " + uid + "'s entry for game #" + gid + "?", "Confirm Delete",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (PreparedStatement ps = DBConnection.get().prepareStatement(
                "DELETE FROM Library WHERE userID = ? AND gameID = ?")) {
            ps.setString(1, uid);
            ps.setInt(2, Integer.parseInt(gid));
            ps.executeUpdate();
            setStatus("Entry removed.", SUCCESS);
            refresh();
        } catch (Exception e) {
            setStatus("Error: " + e.getMessage(), ERROR_CLR);
        }
    }
}
