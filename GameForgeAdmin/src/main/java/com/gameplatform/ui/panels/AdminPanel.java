package com.gameplatform.ui.panels;

import com.gameplatform.db.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class AdminPanel extends BasePanel {

    private JTextArea sqlInput;
    private JTextArea sqlOutput;

    public AdminPanel() {
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 12));

        // Header
        JLabel title = new JLabel("Admin Tools  —  Full DB Access");
        title.setFont(new Font("Dialog", Font.BOLD, 18));
        title.setForeground(new Color(252, 100, 100));
        title.setBorder(new EmptyBorder(0, 0, 6, 0));
        add(title, BorderLayout.NORTH);

        // Split pane: SQL editor top, results bottom
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setBackground(BG_DARK);
        split.setBorder(null);
        split.setDividerSize(6);
        split.setResizeWeight(0.4);

        // SQL editor
        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.setBackground(BG_CARD);
        editorPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CLR, 1),
            new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel editorTitle = new JLabel("SQL Console");
        editorTitle.setFont(new Font("Dialog", Font.BOLD, 13));
        editorTitle.setForeground(TEXT_MUTED);
        editorTitle.setBorder(new EmptyBorder(0,0,8,0));

        sqlInput = new JTextArea(8, 60);
        sqlInput.setFont(new Font("Monospaced", Font.PLAIN, 13));
        sqlInput.setBackground(new Color(18, 23, 38));
        sqlInput.setForeground(new Color(180, 220, 255));
        sqlInput.setCaretColor(ACCENT);
        sqlInput.setBorder(new EmptyBorder(8, 10, 8, 10));
        sqlInput.setText("SELECT TOP 10 * FROM Game;");

        JScrollPane inputScroll = new JScrollPane(sqlInput);
        inputScroll.setBorder(BorderFactory.createLineBorder(BORDER_CLR, 1));

        JButton runBtn   = makeBtn("▶  Execute", ACCENT);
        JButton clearBtn = makeBtn("Clear",       new Color(60,70,100));
        clearBtn.setForeground(TEXT_MUTED);
        runBtn.addActionListener(e -> runSQL());
        clearBtn.addActionListener(e -> { sqlInput.setText(""); sqlOutput.setText(""); });

        JPanel btnRow = buildFormRow(runBtn, clearBtn);
        btnRow.setBorder(new EmptyBorder(8,0,0,0));

        editorPanel.add(editorTitle,  BorderLayout.NORTH);
        editorPanel.add(inputScroll,  BorderLayout.CENTER);
        editorPanel.add(btnRow,       BorderLayout.SOUTH);

        // Output area
        JPanel outPanel = new JPanel(new BorderLayout());
        outPanel.setBackground(BG_CARD);
        outPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CLR, 1),
            new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel outTitle = new JLabel("Output");
        outTitle.setFont(new Font("Dialog", Font.BOLD, 13));
        outTitle.setForeground(TEXT_MUTED);
        outTitle.setBorder(new EmptyBorder(0,0,8,0));

        sqlOutput = new JTextArea();
        sqlOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));
        sqlOutput.setBackground(new Color(13, 17, 28));
        sqlOutput.setForeground(new Color(80, 220, 140));
        sqlOutput.setEditable(false);
        sqlOutput.setBorder(new EmptyBorder(8, 10, 8, 10));

        JScrollPane outScroll = new JScrollPane(sqlOutput);
        outScroll.setBorder(BorderFactory.createLineBorder(BORDER_CLR, 1));

        outPanel.add(outTitle,  BorderLayout.NORTH);
        outPanel.add(outScroll, BorderLayout.CENTER);

        split.setTopComponent(editorPanel);
        split.setBottomComponent(outPanel);

        add(split, BorderLayout.CENTER);

        // Quick-action buttons
        JPanel quickPanel = new JPanel(new BorderLayout());
        quickPanel.setBackground(BG_DARK);
        quickPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel qTitle = new JLabel("Quick Queries");
        qTitle.setFont(new Font("Dialog", Font.BOLD, 12));
        qTitle.setForeground(TEXT_MUTED);

        JPanel qButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        qButtons.setBackground(BG_DARK);

        String[][] queries = {
            {"All Games",    "SELECT * FROM Game ORDER BY gameName;"},
            {"All Users",    "SELECT * FROM [User] ORDER BY username;"},
            {"All Orders",   "SELECT * FROM [Order] ORDER BY OrderID DESC;"},
            {"All Payments", "SELECT * FROM Payment ORDER BY PaymentID DESC;"},
            {"Role Members", "SELECT dp.name AS role, mp.name AS member FROM sys.database_role_members rm JOIN sys.database_principals dp ON rm.role_principal_id = dp.principal_id JOIN sys.database_principals mp ON rm.member_principal_id = mp.principal_id;"}
        };

        for (String[] q : queries) {
            JButton b = makeBtn(q[0], new Color(30, 38, 60));
            b.setForeground(ACCENT);
            b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1),
                new EmptyBorder(5, 10, 5, 10)
            ));
            final String sql = q[1];
            b.addActionListener(e -> { sqlInput.setText(sql); runSQL(); });
            qButtons.add(b);
        }

        quickPanel.add(qTitle,   BorderLayout.NORTH);
        quickPanel.add(qButtons, BorderLayout.CENTER);
        add(quickPanel, BorderLayout.SOUTH);
    }

    private void runSQL() {
        String sql = sqlInput.getText().trim();
        if (sql.isEmpty()) return;

        sqlOutput.setForeground(new Color(80, 220, 140));
        sqlOutput.setText("Running...\n");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            protected String doInBackground() {
                StringBuilder sb = new StringBuilder();
                try (Statement st = DBConnection.get().createStatement()) {
                    boolean isQuery = sql.trim().toUpperCase().startsWith("SELECT");
                    if (isQuery) {
                        ResultSet rs = st.executeQuery(sql);
                        ResultSetMetaData meta = rs.getMetaData();
                        int cols = meta.getColumnCount();
                        // Header
                        for (int i = 1; i <= cols; i++) {
                            sb.append(String.format("%-20s", meta.getColumnName(i)));
                        }
                        sb.append("\n").append("─".repeat(cols * 20)).append("\n");
                        int rows = 0;
                        while (rs.next()) {
                            for (int i = 1; i <= cols; i++) {
                                Object val = rs.getObject(i);
                                sb.append(String.format("%-20s", val == null ? "NULL" : val.toString()));
                            }
                            sb.append("\n");
                            rows++;
                        }
                        sb.append("\n[").append(rows).append(" row(s) returned]");
                    } else {
                        int affected = st.executeUpdate(sql);
                        sb.append("[").append(affected).append(" row(s) affected]");
                    }
                } catch (SQLException e) {
                    return "ERROR: " + e.getMessage();
                }
                return sb.toString();
            }

            protected void done() {
                try {
                    String result = get();
                    if (result.startsWith("ERROR")) {
                        sqlOutput.setForeground(new Color(252, 100, 100));
                    }
                    sqlOutput.setText(result);
                } catch (Exception ex) {
                    sqlOutput.setForeground(new Color(252, 100, 100));
                    sqlOutput.setText("Exception: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}
