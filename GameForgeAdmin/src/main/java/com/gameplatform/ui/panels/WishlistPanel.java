package com.gameplatform.ui.panels;

import com.gameplatform.db.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class WishlistPanel extends BasePanel {

    // Wishlist PK is composite (userID, gameID) - no single WishlistID
    private JTextField userIdField, gameIdField;

    public WishlistPanel() {
        buildUI();
        refresh();
    }

    private void buildUI() {
        JLabel title = new JLabel("Wishlist");
        title.setFont(new Font("Dialog", Font.BOLD, 18));
        title.setForeground(TEXT_MAIN);
        title.setBorder(new EmptyBorder(0, 0, 14, 0));
        add(title, BorderLayout.NORTH);

        JScrollPane sp = buildTable(new String[]{
            "userID", "gameID", "Game Name", "Price"
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

        JLabel formTitle = new JLabel("Add / Remove Wishlist Item");
        formTitle.setFont(new Font("Dialog", Font.BOLD, 13));
        formTitle.setForeground(TEXT_MUTED);
        formTitle.setBorder(new EmptyBorder(0,0,8,0));

        userIdField = makeField(120);
        gameIdField = makeField(80);

        JPanel fields = buildFormRow(
            makeLabel("User ID:"), userIdField,
            makeLabel("Game ID:"), gameIdField
        );

        JButton addBtn     = makeBtn("Add to Wishlist", SUCCESS);
        JButton removeBtn  = makeBtn("Remove",          ERROR_CLR);
        JButton refreshBtn = makeBtn("Refresh",         new Color(60,70,100));
        refreshBtn.setForeground(TEXT_MUTED);

        addBtn.addActionListener(e -> addItem());
        removeBtn.addActionListener(e -> removeItem());
        refreshBtn.addActionListener(e -> refresh());

        table.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    userIdField.setText(safe(tableModel.getValueAt(row, 0)));
                    gameIdField.setText(safe(tableModel.getValueAt(row, 1)));
                }
            }
        });

        JPanel buttons = buildFormRow(addBtn, removeBtn, refreshBtn);

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
            SELECT w.userID, w.gameID, g.gameName, g.gamePrice
            FROM Wishlist w
            JOIN Game g ON w.gameID = g.gameID
            ORDER BY w.userID, g.gameName
        """);
    }

    private void addItem() {
        String uid = userIdField.getText().trim();
        String gid = gameIdField.getText().trim();
        if (uid.isEmpty() || gid.isEmpty()) { setStatus("User ID and Game ID are required.", ERROR_CLR); return; }

        String sql = "INSERT INTO Wishlist (userID, gameID) VALUES (?, ?)";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setString(1, uid);
            ps.setInt(2, Integer.parseInt(gid));
            ps.executeUpdate();
            setStatus("Added to wishlist.", SUCCESS);
            refresh();
        } catch (Exception e) {
            setStatus("Error: " + e.getMessage(), ERROR_CLR);
        }
    }

    private void removeItem() {
        String uid = userIdField.getText().trim();
        String gid = gameIdField.getText().trim();
        if (uid.isEmpty() || gid.isEmpty()) { setStatus("Select a row to remove.", WARN); return; }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Remove game #" + gid + " from " + uid + "'s wishlist?", "Confirm",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (PreparedStatement ps = DBConnection.get().prepareStatement(
                "DELETE FROM Wishlist WHERE userID = ? AND gameID = ?")) {
            ps.setString(1, uid);
            ps.setInt(2, Integer.parseInt(gid));
            ps.executeUpdate();
            setStatus("Removed from wishlist.", SUCCESS);
            refresh();
        } catch (Exception e) {
            setStatus("Error: " + e.getMessage(), ERROR_CLR);
        }
    }
}
