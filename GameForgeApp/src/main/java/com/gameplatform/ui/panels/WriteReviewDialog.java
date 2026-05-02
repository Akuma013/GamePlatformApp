package com.gameplatform.ui.panels;

import com.gameplatform.db.DBConnection;
import com.gameplatform.db.ReviewDAO;
import com.gameplatform.ui.theme.GameForgeTheme;
import com.gameplatform.ui.util.DarkScrollBarUI;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class WriteReviewDialog extends JDialog {

    private final int gameID;
    private final Runnable onSubmitted;

    private final JComboBox<Integer> ratingCombo =
            new JComboBox<>(new Integer[] {10,9,8,7,6,5, 4, 3, 2, 1});
    private final JTextArea descriptionArea = new JTextArea(6, 36);

    public WriteReviewDialog(Window owner, String gameName, int gameID,
                             Runnable onSubmitted) {
        super(owner, "Review: " + gameName, ModalityType.APPLICATION_MODAL);
        this.gameID = gameID;
        this.onSubmitted = onSubmitted;

        setSize(500, 360);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(GameForgeTheme.BG_DARK);
        root.setBorder(BorderFactory.createEmptyBorder(20, 22, 16, 22));
        setContentPane(root);

        // ---- form ----
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        form.add(label("Your rating"));
        ratingCombo.setMaximumSize(new Dimension(80, 30));
        ratingCombo.setAlignmentX(LEFT_ALIGNMENT);
        form.add(ratingCombo);
        form.add(Box.createVerticalStrut(14));

        form.add(label("Your review"));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBackground(GameForgeTheme.BG_CARD);
        descriptionArea.setForeground(GameForgeTheme.TEXT_BRIGHT);
        descriptionArea.setCaretColor(GameForgeTheme.TEXT_BRIGHT);
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JScrollPane scroll = new JScrollPane(descriptionArea);
        scroll.setAlignmentX(LEFT_ALIGNMENT);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        form.add(scroll);
        root.add(form, BorderLayout.CENTER);


        // ---- buttons ----
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(styled("Cancel", GameForgeTheme.BG_CARD,
                GameForgeTheme.TEXT_BRIGHT, e -> dispose()));
        buttons.add(styled("Submit Review", GameForgeTheme.BTN_GREEN,
                Color.WHITE, e -> submit()));
        root.add(buttons, BorderLayout.SOUTH);
    }

    private void submit() {
        int rating = (Integer) ratingCombo.getSelectedItem();
        String desc = descriptionArea.getText().trim();
        if (desc.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please write something in your review.",
                    "Empty review", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            ReviewDAO.create(DBConnection.getAppUsername(), gameID, rating, desc);
            if (onSubmitted != null) onSubmitted.run();
            dispose();
        } catch (SQLException ex) {
            // The trigger raises if the user doesn't own the game,
            // but we shouldn't reach this dialog in that case anyway.
            JOptionPane.showMessageDialog(this,
                    "Could not submit review:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------------- helpers ----------------
    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(GameForgeTheme.BODY);
        l.setForeground(GameForgeTheme.TEXT);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
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