package com.gameplatform.ui;


import com.gameplatform.db.DBConnection;
import com.gameplatform.search.SearchService;
import com.gameplatform.ui.panels.LibraryPanel;
import com.gameplatform.ui.panels.StorePanel;
import com.gameplatform.ui.panels.WishlistPanel;
import com.gameplatform.ui.theme.GameForgeTheme;
import com.gameplatform.db.UserDAO;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private JLabel balanceLabel = new JLabel();      // ← add field at top of class

    public MainFrame(SearchService search) {
        setTitle("GameForge — " + DBConnection.getAppUsername());
        setSize(1200, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Root container
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(GameForgeTheme.BG_DARK);
        setContentPane(root);

        // Header strip showing the logged-in customer
        root.add(buildHeader(), BorderLayout.NORTH);

        // Tabbed content
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(GameForgeTheme.BG_DARK);
        tabs.setForeground(GameForgeTheme.TEXT_BRIGHT);
        tabs.setFont(GameForgeTheme.TITLE);
        tabs.setFocusable(false);

        tabs.addTab("Store",    new StorePanel(search));
        tabs.addTab("Library",  new LibraryPanel());
        tabs.addTab("Wishlist", new WishlistPanel());

        root.add(tabs, BorderLayout.CENTER);

        // Disconnect cleanly when closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                DBConnection.disconnect();
            }
        });
    }

    /** Top header with app name on the left, current user + sign-out on the right. */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(GameForgeTheme.BG_PANEL);
        header.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        JLabel brand = new JLabel("GameForge");
        brand.setFont(GameForgeTheme.HEADER);
        brand.setForeground(GameForgeTheme.ACCENT);

        JLabel user = new JLabel("Signed in as " + DBConnection.getAppUsername());
        user.setFont(GameForgeTheme.BODY);
        user.setForeground(GameForgeTheme.TEXT);

        balanceLabel.setFont(GameForgeTheme.TITLE);
        balanceLabel.setForeground(new Color(0x86b418));   // money green
        refreshBalance();

        JButton signOut = new JButton("Sign out");
        signOut.setBackground(GameForgeTheme.BG_CARD);
        signOut.setForeground(GameForgeTheme.TEXT_BRIGHT);
        signOut.setFocusPainted(false);
        signOut.setOpaque(true);
        signOut.setContentAreaFilled(true);
        signOut.setBorderPainted(false);
        signOut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signOut.addActionListener(e -> handleSignOut());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);
        right.add(user);
        right.add(balanceLabel);
        right.add(signOut);

        header.add(brand, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }


    /** Re-read the balance from the DB and update the label. */
    public void refreshBalance() {
        try {
            double bal = com.gameplatform.db.UserDAO.getBalance(
                    DBConnection.getAppUsername());
            balanceLabel.setText(String.format("$%.2f", bal));
        } catch (Exception ex) {
            balanceLabel.setText("—");
        }
    }

    private void handleSignOut() {
        DBConnection.disconnect();
        dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}