package com.gameplatform.ui;

import com.gameplatform.ui.theme.GameForgeTheme;
import com.gameplatform.db.DBConnection;
import com.gameplatform.search.SearchService;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JLabel statusLabel = new JLabel(" ");
    private final JButton signInButton = new JButton("Sign in");

    public LoginFrame() {
        setTitle("Game Platform — Sign in");
        setSize(440, 320);
        setLocationRelativeTo(null);          // centre on screen
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // Outer container with the dark background
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(GameForgeTheme.BG_DARK);
        root.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        setContentPane(root);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        // ---- header ----
        JLabel header = new JLabel("Sign in to your account");
        header.setFont(GameForgeTheme.HEADER);
        header.setForeground(GameForgeTheme.TEXT_BRIGHT);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        g.insets = new Insets(4, 6, 18, 6);
        root.add(header, g);

        // ---- username field ----
        g.gridwidth = 1;
        g.insets = new Insets(6, 6, 6, 6);
        addLabelled(root, "Username", usernameField, g, 1);

        // ---- password field ----
        addLabelled(root, "Password", passwordField, g, 2);

        // ---- sign-in button ----
        styleButton(signInButton);
        g.gridx = 1; g.gridy = 3;
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.LINE_END;
        g.insets = new Insets(14, 6, 6, 6);
        signInButton.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());
        root.add(signInButton, g);

        // ---- status line (errors / progress messages go here later) ----
        statusLabel.setForeground(GameForgeTheme.TEXT_MUTED);
        statusLabel.setFont(GameForgeTheme.SMALL);
        g.gridx = 0; g.gridy = 4; g.gridwidth = 2;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.CENTER;
        root.add(statusLabel, g);
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        // Disable the button and show progress while we work
        signInButton.setEnabled(false);
        statusLabel.setForeground(GameForgeTheme.TEXT_MUTED);
        statusLabel.setText("Connecting...");

        // Run DB work off the EDT so the UI doesn't freeze
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            SearchService search;
            String errorMessage;

            @Override
            protected Boolean doInBackground() {
                if (!DBConnection.loginCustomer(username, password)) {  // ← NEW
                    errorMessage = "Invalid username or password.";
                    return false;
                }
                try {
                    search = new SearchService();
                    search.buildIndex();
                    return true;
                } catch (Exception ex) {
                    errorMessage = "Could not load game index: " + ex.getMessage();
                    DBConnection.disconnect();
                    return false;
                }
            }

            @Override
            protected void done() {
                signInButton.setEnabled(true);
                try {
                    if (get()) {
                        // Success: open MainFrame and close the login window
                        new MainFrame(search).setVisible(true);
                        dispose();
                    } else {
                        showError(errorMessage);
                    }
                } catch (Exception ex) {
                    showError("Unexpected error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void showError(String msg) {
        statusLabel.setForeground(new Color(0xff6b6b));   // soft red
        statusLabel.setText(msg);
    }

    /** Helper to add a "Label : Field" row into the GridBag. */
    private void addLabelled(JPanel parent, String text, JComponent field,
                             GridBagConstraints g, int row) {
        JLabel l = new JLabel(text);
        l.setForeground(GameForgeTheme.TEXT);
        l.setFont(GameForgeTheme.BODY);

        g.gridx = 0; g.gridy = row;
        g.weightx = 0;
        parent.add(l, g);

        field.setFont(GameForgeTheme.BODY);
        g.gridx = 1; g.weightx = 1;
        parent.add(field, g);
        g.weightx = 0;
    }

    private void styleButton(JButton b) {
        b.setBackground(GameForgeTheme.BTN_GREEN_HOVER);
        b.setForeground(Color.WHITE);
        b.setFont(GameForgeTheme.TITLE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 22, 8, 22));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);                 // ← add
        b.setContentAreaFilled(true);      // ← add
        b.setBorderPainted(false);         // ← add
    }

    /** Temporary entry point so we can preview the window. */
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}