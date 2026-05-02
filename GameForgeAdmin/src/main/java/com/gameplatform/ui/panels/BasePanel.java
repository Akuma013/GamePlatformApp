package com.gameplatform.ui.panels;

import com.gameplatform.db.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public abstract class BasePanel extends JPanel {

    protected static final Color BG_DARK    = new Color(13, 17, 28);
    protected static final Color BG_CARD    = new Color(22, 28, 45);
    protected static final Color BG_ROW_ALT = new Color(18, 23, 38);
    protected static final Color ACCENT     = new Color(99, 179, 237);
    protected static final Color ACCENT2    = new Color(154, 117, 244);
    protected static final Color TEXT_MAIN  = new Color(220, 225, 240);
    protected static final Color TEXT_MUTED = new Color(120, 130, 160);
    protected static final Color BORDER_CLR = new Color(40, 50, 75);
    protected static final Color SUCCESS    = new Color(80, 220, 140);
    protected static final Color ERROR_CLR  = new Color(252, 100, 100);
    protected static final Color WARN       = new Color(250, 190, 80);

    protected JTable table;
    protected DefaultTableModel tableModel;
    protected JLabel statusLabel;

    public BasePanel() {
        setBackground(BG_DARK);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
    }

    // ── Table helpers ────────────────────────────────────────────────────────

    protected JScrollPane buildTable(String[] columns) {
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(new Color(40, 55, 90));
                    c.setForeground(TEXT_MAIN);
                } else {
                    c.setBackground(row % 2 == 0 ? BG_CARD : BG_ROW_ALT);
                    c.setForeground(TEXT_MAIN);
                }
                return c;
            }
        };
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_MAIN);
        table.setGridColor(BORDER_CLR);
        table.setFont(new Font("Dialog", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setSelectionBackground(new Color(40, 55, 90));
        table.setSelectionForeground(TEXT_MAIN);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.getTableHeader().setBackground(new Color(18, 23, 38));
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 12));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDER_CLR));

        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(BG_DARK);
        sp.getViewport().setBackground(BG_CARD);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_CLR, 1));
        return sp;
    }

    protected void loadTable(String sql) {
        tableModel.setRowCount(0);
        try (Statement st = DBConnection.get().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            while (rs.next()) {
                Object[] row = new Object[cols];
                for (int i = 0; i < cols; i++) row[i] = rs.getObject(i + 1);
                tableModel.addRow(row);
            }
            setStatus("Loaded " + tableModel.getRowCount() + " rows.", SUCCESS);
        } catch (SQLException e) {
            setStatus("Error: " + e.getMessage(), ERROR_CLR);
        }
    }

    // ── Form helpers ─────────────────────────────────────────────────────────

    protected JTextField makeField(int width) {
        JTextField f = new JTextField();
        f.setFont(new Font("Dialog", Font.PLAIN, 13));
        f.setBackground(new Color(30, 38, 60));
        f.setForeground(TEXT_MAIN);
        f.setCaretColor(ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CLR, 1),
            new EmptyBorder(5, 10, 5, 10)
        ));
        f.setPreferredSize(new Dimension(width, 32));
        return f;
    }

    protected JButton makeBtn(String label, Color bg) {
        JButton b = new JButton(label);
        b.setFont(new Font("Dialog", Font.BOLD, 12));
        b.setBackground(bg);
        b.setForeground(new Color(10, 15, 30));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(7, 16, 7, 16));
        Color hover = bg.brighter();
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(hover); }
            public void mouseExited(java.awt.event.MouseEvent e)  { b.setBackground(bg); }
        });
        return b;
    }

    protected JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Dialog", Font.PLAIN, 12));
        l.setForeground(TEXT_MUTED);
        return l;
    }

    protected JPanel buildFormRow(Component... components) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row.setBackground(BG_DARK);
        for (Component c : components) row.add(c);
        return row;
    }

    // ── Status bar ───────────────────────────────────────────────────────────

    protected JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0,BORDER_CLR),
            new EmptyBorder(6, 14, 6, 14)
        ));
        statusLabel = new JLabel("Ready.");
        statusLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_MUTED);
        bar.add(statusLabel, BorderLayout.WEST);
        return bar;
    }

    protected void setStatus(String msg, Color color) {
        if (statusLabel != null) {
            statusLabel.setText(msg);
            statusLabel.setForeground(color);
        }
    }

    protected int getSelectedId(int colIndex) {
        int row = table.getSelectedRow();
        if (row < 0) return -1;
        Object val = tableModel.getValueAt(row, colIndex);
        return val == null ? -1 : Integer.parseInt(val.toString());
    }

    protected String getSelectedValue(int colIndex) {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Object val = tableModel.getValueAt(row, colIndex);
        return val == null ? null : val.toString();
    }
}
