package com.gameplatform.ui.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PaymentsPanel extends BasePanel {

    public PaymentsPanel() {
        buildUI();
        refresh();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG_DARK);
        top.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel title = new JLabel("Payments  (read-only)");
        title.setFont(new Font("Dialog", Font.BOLD, 18));
        title.setForeground(TEXT_MAIN);

        JButton refreshBtn = makeBtn("Refresh", new Color(60,70,100));
        refreshBtn.setForeground(TEXT_MUTED);
        refreshBtn.addActionListener(e -> refresh());

        top.add(title,      BorderLayout.WEST);
        top.add(refreshBtn, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Payment: paymentID, orderID, amount, paymentMethod
        JScrollPane sp = buildTable(new String[]{
            "paymentID", "orderID", "Amount", "Payment Method"
        });
        add(sp, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    private void refresh() {
        loadTable("SELECT paymentID, orderID, amount, paymentMethod FROM Payment ORDER BY paymentID DESC");
    }
}
