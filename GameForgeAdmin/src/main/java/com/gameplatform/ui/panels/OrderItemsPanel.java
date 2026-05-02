package com.gameplatform.ui.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class OrderItemsPanel extends BasePanel {

    public OrderItemsPanel() {
        buildUI();
        refresh();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG_DARK);
        top.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel title = new JLabel("Order Items  (read-only)");
        title.setFont(new Font("Dialog", Font.BOLD, 18));
        title.setForeground(TEXT_MAIN);

        JButton refreshBtn = makeBtn("Refresh", new Color(60,70,100));
        refreshBtn.setForeground(TEXT_MUTED);
        refreshBtn.addActionListener(e -> refresh());

        top.add(title,      BorderLayout.WEST);
        top.add(refreshBtn, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Order_Game: gameID, orderID, priceAtPurchase + gameName from join
        JScrollPane sp = buildTable(new String[]{
            "gameID", "orderID", "Game Name", "Price at Purchase"
        });
        add(sp, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    private void refresh() {
        loadTable("""
            SELECT og.gameID, og.orderID, g.gameName, og.priceAtPurchase
            FROM Order_Game og
            JOIN Game g ON og.gameID = g.gameID
            ORDER BY og.orderID DESC
        """);
    }
}
