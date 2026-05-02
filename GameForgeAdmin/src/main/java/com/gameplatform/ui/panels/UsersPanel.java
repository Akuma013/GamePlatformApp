package com.gameplatform.ui.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class UsersPanel extends BasePanel {

    public UsersPanel() {
        buildUI();
        refresh();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG_DARK);
        top.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel title = new JLabel("Users  (read-only)");
        title.setFont(new Font("Dialog", Font.BOLD, 18));
        title.setForeground(TEXT_MAIN);

        JButton refreshBtn = makeBtn("Refresh", new Color(60,70,100));
        refreshBtn.setForeground(TEXT_MUTED);
        refreshBtn.addActionListener(e -> refresh());

        top.add(title,      BorderLayout.WEST);
        top.add(refreshBtn, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // User table: username, nickname, email
        JScrollPane sp = buildTable(new String[]{
            "username", "nickname", "email","balance"
        });
        add(sp, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    private void refresh() {
        loadTable("SELECT username, nickname, email,balance FROM [User] ORDER BY username");
    }
}
