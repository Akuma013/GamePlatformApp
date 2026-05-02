package com.gameplatform.ui.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GamesPanel extends BasePanel {

    private JTextField searchField;

    public GamesPanel() {
        buildUI();
        refresh();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG_DARK);
        top.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel title = new JLabel("Game Catalog");
        title.setFont(new Font("Dialog", Font.BOLD, 18));
        title.setForeground(TEXT_MAIN);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setBackground(BG_DARK);
        searchField = makeField(200);
        JButton searchBtn  = makeBtn("Search",  ACCENT);
        JButton refreshBtn = makeBtn("Refresh", new Color(60, 70, 100));
        refreshBtn.setForeground(TEXT_MUTED);

        searchBtn.addActionListener(e -> search());
        refreshBtn.addActionListener(e -> refresh());

        controls.add(makeLabel("Search:"));
        controls.add(searchField);
        controls.add(searchBtn);
        controls.add(refreshBtn);

        top.add(title,    BorderLayout.WEST);
        top.add(controls, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        JScrollPane sp = buildTable(new String[]{
            "gameID", "Name", "Price", "Size (MB)", "Version", "Download URL", "Publisher"
        });
        add(sp, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    private void refresh() {
        loadTable("""
            SELECT g.gameID, g.gameName, g.gamePrice, g.gameSize, g.version,
                   g.downloadURL, p.publisherName
            FROM Game g
            LEFT JOIN Publisher p ON g.publisherID = p.publisherID
            ORDER BY g.gameName
        """);
    }

    private void search() {
        String q = searchField.getText().trim().replace("'", "''");
        if (q.isEmpty()) { refresh(); return; }
        loadTable("""
            SELECT g.gameID, g.gameName, g.gamePrice, g.gameSize, g.version,
                   g.downloadURL, p.publisherName
            FROM Game g
            LEFT JOIN Publisher p ON g.publisherID = p.publisherID
            WHERE g.gameName LIKE '%""" + q + "%' ORDER BY g.gameName");
    }
}
