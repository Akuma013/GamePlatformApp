package com.gameplatform.ui.panels;

import com.gameplatform.db.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class OrdersPanel extends BasePanel {

    // Order has: orderID, quantity, orderDate, userID
    private JTextField userIdField, quantityField, dateField;

    public OrdersPanel() {
        buildUI();
        refresh();
    }

    private void buildUI() {
        JLabel title = new JLabel("Orders");
        title.setFont(new Font("Dialog", Font.BOLD, 18));
        title.setForeground(TEXT_MAIN);
        title.setBorder(new EmptyBorder(0, 0, 14, 0));
        add(title, BorderLayout.NORTH);

        JScrollPane sp = buildTable(new String[]{
            "orderID", "userID", "Quantity", "Order Date"
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

        JLabel formTitle = new JLabel("Place New Order");
        formTitle.setFont(new Font("Dialog", Font.BOLD, 13));
        formTitle.setForeground(TEXT_MUTED);
        formTitle.setBorder(new EmptyBorder(0,0,8,0));

        userIdField  = makeField(120);
        quantityField= makeField(70);
        dateField    = makeField(110);
        dateField.setText("2024-01-01");

        JPanel fields = buildFormRow(
            makeLabel("User ID:"),              userIdField,
            makeLabel("Quantity:"),             quantityField,
            makeLabel("Date (YYYY-MM-DD):"),    dateField
        );

        JButton placeBtn   = makeBtn("Place Order", SUCCESS);
        JButton refreshBtn = makeBtn("Refresh",     new Color(60,70,100));
        refreshBtn.setForeground(TEXT_MUTED);

        placeBtn.addActionListener(e -> placeOrder());
        refreshBtn.addActionListener(e -> refresh());

        table.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    userIdField.setText(safe(tableModel.getValueAt(row, 1)));
                    quantityField.setText(safe(tableModel.getValueAt(row, 2)));
                    dateField.setText(safe(tableModel.getValueAt(row, 3)));
                }
            }
        });

        JPanel buttons = buildFormRow(placeBtn, refreshBtn);

        form.add(formTitle, BorderLayout.NORTH);
        form.add(fields,    BorderLayout.CENTER);
        form.add(buttons,   BorderLayout.SOUTH);

        south.add(form, BorderLayout.NORTH);
        south.add(buildStatusBar(), BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);
    }

    private String safe(Object o) { return o == null ? "" : o.toString(); }

    private void refresh() {
        loadTable("SELECT orderID, userID, quantity, orderDate FROM [Order] ORDER BY orderID DESC");
    }

    private void placeOrder() {
        String uid = userIdField.getText().trim();
        String qty = quantityField.getText().trim();
        String dt  = dateField.getText().trim();

        if (uid.isEmpty() || qty.isEmpty()) {
            setStatus("User ID and Quantity are required.", ERROR_CLR); return;
        }

        String sql = "INSERT INTO [Order] (quantity, orderDate, userID) VALUES (?, ?, ?)";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(qty));
            ps.setString(2, dt.isEmpty() ? null : dt);
            ps.setString(3, uid);
            ps.executeUpdate();
            setStatus("Order placed.", SUCCESS);
            refresh();
        } catch (Exception e) {
            setStatus("Error: " + e.getMessage(), ERROR_CLR);
        }
    }
}
