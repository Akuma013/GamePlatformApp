package com.gameplatform.ui.panels;

import com.gameplatform.db.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class ReviewsPanel extends BasePanel {

    private JTextField userIdField, gameIdField, ratingField, dateField;
    private JTextArea  descArea;

    public ReviewsPanel() {
        buildUI();
        refresh();
    }

    private void buildUI() {
        JLabel title = new JLabel("Reviews");
        title.setFont(new Font("Dialog", Font.BOLD, 18));
        title.setForeground(TEXT_MAIN);
        title.setBorder(new EmptyBorder(0, 0, 14, 0));
        add(title, BorderLayout.NORTH);

        // reviewID, userID, gameID, gameName, rating, description, reviewDate
        JScrollPane sp = buildTable(new String[]{
            "reviewID", "userID", "gameID", "Game Name", "Rating", "Description", "Date"
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

        JLabel formTitle = new JLabel("Write / Edit Review  (Rating: 1–10)");
        formTitle.setFont(new Font("Dialog", Font.BOLD, 13));
        formTitle.setForeground(TEXT_MUTED);
        formTitle.setBorder(new EmptyBorder(0,0,8,0));

        userIdField = makeField(100);
        gameIdField = makeField(70);
        ratingField = makeField(50);
        dateField   = makeField(100);
        dateField.setText("2024-01-01");

        descArea = new JTextArea(2, 30);
        descArea.setFont(new Font("Dialog", Font.PLAIN, 13));
        descArea.setBackground(new Color(30, 38, 60));
        descArea.setForeground(TEXT_MAIN);
        descArea.setCaretColor(ACCENT);
        descArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CLR, 1),
            new EmptyBorder(5,10,5,10)
        ));
        descArea.setLineWrap(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(280, 56));
        descScroll.setBorder(null);

        JPanel fields = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fields.setBackground(BG_CARD);
        fields.add(makeLabel("User ID:"));    fields.add(userIdField);
        fields.add(makeLabel("Game ID:"));    fields.add(gameIdField);
        fields.add(makeLabel("Rating:"));     fields.add(ratingField);
        fields.add(makeLabel("Date (YYYY-MM-DD):")); fields.add(dateField);
        fields.add(makeLabel("Description:")); fields.add(descScroll);

        JButton addBtn     = makeBtn("Submit Review", SUCCESS);
        JButton updateBtn  = makeBtn("Update Review", WARN);
        JButton refreshBtn = makeBtn("Refresh",       new Color(60,70,100));
        refreshBtn.setForeground(TEXT_MUTED);

        addBtn.addActionListener(e -> addReview());
        updateBtn.addActionListener(e -> updateReview());
        refreshBtn.addActionListener(e -> refresh());

        table.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    userIdField.setText(safe(tableModel.getValueAt(row, 1)));
                    gameIdField.setText(safe(tableModel.getValueAt(row, 2)));
                    ratingField.setText(safe(tableModel.getValueAt(row, 4)));
                    descArea.setText(safe(tableModel.getValueAt(row, 5)));
                    dateField.setText(safe(tableModel.getValueAt(row, 6)));
                }
            }
        });

        JPanel buttons = buildFormRow(addBtn, updateBtn, refreshBtn);

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
            SELECT r.reviewID, r.userID, r.gameID, g.gameName,
                   r.rating, r.description, r.reviewDate
            FROM Review r
            JOIN Game g ON r.gameID = g.gameID
            ORDER BY r.reviewID DESC
        """);
    }

    private void addReview() {
        String uid  = userIdField.getText().trim();
        String gid  = gameIdField.getText().trim();
        String rat  = ratingField.getText().trim();
        String desc = descArea.getText().trim();
        String date = dateField.getText().trim();
        if (uid.isEmpty() || gid.isEmpty() || rat.isEmpty()) {
            setStatus("User ID, Game ID and Rating are required.", ERROR_CLR); return;
        }

        String sql = "INSERT INTO Review (rating, description, reviewDate, userID, gameID) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(rat));
            ps.setString(2, desc.isEmpty() ? null : desc);
            ps.setString(3, date.isEmpty() ? null : date);
            ps.setString(4, uid);
            ps.setInt(5, Integer.parseInt(gid));
            ps.executeUpdate();
            setStatus("Review submitted.", SUCCESS);
            refresh();
        } catch (Exception e) {
            setStatus("Error: " + e.getMessage(), ERROR_CLR);
        }
    }

    private void updateReview() {
        int id = getSelectedId(0);
        if (id < 0) { setStatus("Select a review to update.", WARN); return; }
        String rat  = ratingField.getText().trim();
        String desc = descArea.getText().trim();

        String sql = "UPDATE Review SET rating = ?, description = ? WHERE reviewID = ?";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(rat));
            ps.setString(2, desc.isEmpty() ? null : desc);
            ps.setInt(3, id);
            ps.executeUpdate();
            setStatus("Review updated.", SUCCESS);
            refresh();
        } catch (Exception e) {
            setStatus("Error: " + e.getMessage(), ERROR_CLR);
        }
    }
}
