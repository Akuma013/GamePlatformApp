package com.gameplatform.ui;

import com.gameplatform.db.DBConnection;
import com.gameplatform.ui.panels.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class DashboardFrame extends JFrame {

    private static final Color BG_DARK    = new Color(13, 17, 28);
    private static final Color BG_CARD    = new Color(22, 28, 45);
    private static final Color ACCENT     = new Color(99, 179, 237);
    private static final Color ACCENT2    = new Color(154, 117, 244);
    private static final Color TEXT_MAIN  = new Color(220, 225, 240);
    private static final Color TEXT_MUTED = new Color(120, 130, 160);
    private static final Color BORDER_CLR = new Color(40, 50, 75);

    private JTabbedPane tabs;

    public DashboardFrame() {
        String role = DBConnection.getCurrentRole();
        String user = DBConnection.getCurrentUser();

        setTitle("GamePlatform DB — " + user + " (" + role + ")");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 720);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        add(buildTopBar(user, role), BorderLayout.NORTH);
        add(buildTabs(role),         BorderLayout.CENTER);
    }

    private JPanel buildTopBar(String user, String role) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_CLR),
            new EmptyBorder(12, 20, 12, 20)
        ));

        // Left: title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setBackground(BG_CARD);
        JLabel logo = new JLabel("◈");
        logo.setFont(new Font("Dialog", Font.PLAIN, 20));
        logo.setForeground(ACCENT);
        JLabel title = new JLabel("GamePlatform DB");
        title.setFont(new Font("Dialog", Font.BOLD, 16));
        title.setForeground(TEXT_MAIN);
        left.add(logo);
        left.add(title);

        // Right: user badge + logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(BG_CARD);

        Color roleColor = switch (role) {
            case "ADMIN"   -> new Color(252, 100, 100);
            case "USER"    -> ACCENT;
            case "ANALYST" -> new Color(80, 220, 140);
            default        -> TEXT_MUTED;
        };

        JLabel badge = new JLabel(" " + role + " ");
        badge.setFont(new Font("Dialog", Font.BOLD, 10));
        badge.setForeground(roleColor);
        badge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(roleColor.darker(), 1),
            new EmptyBorder(2, 6, 2, 6)
        ));

        JLabel userLbl = new JLabel(user);
        userLbl.setFont(new Font("Dialog", Font.PLAIN, 13));
        userLbl.setForeground(TEXT_MAIN);

        JButton logout = new JButton("Logout");
        logout.setFont(new Font("Dialog", Font.PLAIN, 12));
        logout.setBackground(new Color(30, 38, 60));
        logout.setForeground(TEXT_MUTED);
        logout.setFocusPainted(false);
        logout.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CLR, 1),
            new EmptyBorder(4, 12, 4, 12)
        ));
        logout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> {
            DBConnection.disconnect();
            new LoginFrame().setVisible(true);
            dispose();
        });

        right.add(badge);
        right.add(userLbl);
        right.add(logout);

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JTabbedPane buildTabs(String role) {
        tabs = new JTabbedPane(JTabbedPane.LEFT);
        tabs.setBackground(BG_DARK);
        tabs.setForeground(TEXT_MAIN);
        tabs.setFont(new Font("Dialog", Font.PLAIN, 13));

        // ── Tabs available to ALL roles ──
        tabs.addTab("  Games      ", new GamesPanel());
        tabs.addTab("  Library    ", new LibraryPanel());
        tabs.addTab("  Wishlist   ", new WishlistPanel());
        tabs.addTab("  Reviews    ", new ReviewsPanel());
        tabs.addTab("  Orders     ", new OrdersPanel());

        // ── Analyst + Admin only ──
        if (role.equals("ANALYST") || role.equals("ADMIN")) {
            tabs.addTab("  Users      ", new UsersPanel());
            tabs.addTab("  Payments   ", new PaymentsPanel());
            tabs.addTab("  Order Items", new OrderItemsPanel());
        }

        // ── Admin only ──
        if (role.equals("ADMIN")) {
            tabs.addTab("  Admin Tools", new AdminPanel());
        }

        // Style the tab area
        UIManager.put("TabbedPane.selected",    BG_CARD);
        UIManager.put("TabbedPane.background",  BG_DARK);
        UIManager.put("TabbedPane.foreground",  TEXT_MUTED);
        UIManager.put("TabbedPane.focus",       BG_DARK);
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0,0,0,0));

        return tabs;
    }
}
