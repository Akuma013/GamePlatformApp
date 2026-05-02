package com.gameplatform.ui;

import com.gameplatform.db.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {

    private JTextField userField;
    private JPasswordField passField;
    private JLabel statusLabel;
    private JButton loginBtn;

    // Color palette
    private static final Color BG_DARK    = new Color(13, 17, 28);
    private static final Color BG_CARD    = new Color(22, 28, 45);
    private static final Color ACCENT     = new Color(99, 179, 237);
    private static final Color ACCENT2    = new Color(154, 117, 244);
    private static final Color TEXT_MAIN  = new Color(220, 225, 240);
    private static final Color TEXT_MUTED = new Color(120, 130, 160);
    private static final Color BORDER_CLR = new Color(40, 50, 75);
    private static final Color ERROR_CLR  = new Color(252, 100, 100);
    private static final Color SUCCESS_CLR= new Color(80, 220, 140);

    public LoginFrame() {
        setTitle("GamePlatform DB — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setSize(460, 600);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildForm(),   BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel();
        p.setBackground(BG_DARK);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(40, 40, 10, 40));

        // Icon / logo area
        JLabel icon = new JLabel("◈", SwingConstants.CENTER);
        icon.setFont(new Font("Dialog", Font.PLAIN, 42));
        icon.setForeground(ACCENT);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("GamePlatform DB", SwingConstants.CENTER);
        title.setFont(new Font("Dialog", Font.BOLD, 22));
        title.setForeground(TEXT_MAIN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Sign in to your account", SwingConstants.CENTER);
        sub.setFont(new Font("Dialog", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(icon);
        p.add(Box.createVerticalStrut(8));
        p.add(title);
        p.add(Box.createVerticalStrut(4));
        p.add(sub);
        return p;
    }

    private JPanel buildForm() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BG_DARK);
        outer.setBorder(new EmptyBorder(20, 40, 10, 40));

        JPanel card = new JPanel();
        card.setBackground(BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CLR, 1),
            new EmptyBorder(28, 28, 28, 28)
        ));

        // Username
        card.add(makeLabel("Username"));
        card.add(Box.createVerticalStrut(6));
        userField = makeField("admin1");
        card.add(userField);
        card.add(Box.createVerticalStrut(18));

        // Password
        card.add(makeLabel("Password"));
        card.add(Box.createVerticalStrut(6));
        passField = new JPasswordField("Admin123!");
        styleField(passField);
        card.add(passField);
        card.add(Box.createVerticalStrut(24));

        // Quick-fill buttons
        card.add(makeLabel("Quick fill"));
        card.add(Box.createVerticalStrut(6));
        card.add(buildQuickFill());
        card.add(Box.createVerticalStrut(24));

        // Login button
        loginBtn = new JButton("Connect");
        loginBtn.setFont(new Font("Dialog", Font.BOLD, 14));
        loginBtn.setBackground(ACCENT);
        loginBtn.setForeground(new Color(10, 15, 30));
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginBtn.addActionListener(e -> doLogin());
        loginBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { loginBtn.setBackground(ACCENT2); }
            public void mouseExited(MouseEvent e)  { loginBtn.setBackground(ACCENT); }
        });
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(12));

        // Status
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_MUTED);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(statusLabel);

        // Enter key on password
        passField.addActionListener(e -> doLogin());

        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildQuickFill() {
        JPanel p = new JPanel(new GridLayout(1, 3, 8, 0));
        p.setBackground(BG_CARD);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        String[][] users = {
            {"admin1",   "Admin123!",   "Admin"},
            {"user1",    "User123!",    "User"},
            {"analyst1", "Analyst123!", "Analyst"}
        };

        Color[] colors = { new Color(252, 100, 100), ACCENT, new Color(80, 220, 140) };

        for (int i = 0; i < users.length; i++) {
            String[] u = users[i];
            JButton b = new JButton(u[2]);
            b.setFont(new Font("Dialog", Font.PLAIN, 11));
            b.setBackground(new Color(30, 38, 60));
            b.setForeground(colors[i]);
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createLineBorder(colors[i].darker(), 1));
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            final int idx = i;
            b.addActionListener(e -> {
                userField.setText(u[0]);
                passField.setText(u[1]);
                statusLabel.setText(" ");
            });
            Color hoverBg = colors[idx].darker().darker();
            b.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { b.setBackground(hoverBg); }
                public void mouseExited(MouseEvent e)  { b.setBackground(new Color(30, 38, 60)); }
            });
            p.add(b);
        }
        return p;
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel();
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(0, 0, 20, 0));
        JLabel l = new JLabel("GamePlatformDB · SQL Server", SwingConstants.CENTER);
        l.setFont(new Font("Dialog", Font.PLAIN, 11));
        l.setForeground(new Color(60, 70, 100));
        p.add(l);
        return p;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Dialog", Font.PLAIN, 12));
        l.setForeground(TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField makeField(String placeholder) {
        JTextField f = new JTextField(placeholder);
        styleField(f);
        return f;
    }

    private void styleField(JTextField f) {
        f.setFont(new Font("Dialog", Font.PLAIN, 14));
        f.setBackground(new Color(30, 38, 60));
        f.setForeground(TEXT_MAIN);
        f.setCaretColor(ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CLR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void doLogin() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Please fill in both fields.");
            statusLabel.setForeground(ERROR_CLR);
            return;
        }

        loginBtn.setEnabled(false);
        loginBtn.setText("Connecting...");
        statusLabel.setText("Connecting to SQL Server...");
        statusLabel.setForeground(TEXT_MUTED);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            protected Boolean doInBackground() {
                return DBConnection.connect(user, pass);
            }
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        statusLabel.setText("Connected!");
                        statusLabel.setForeground(SUCCESS_CLR);
                        Timer t = new Timer(500, e -> {
                            new DashboardFrame().setVisible(true);
                            LoginFrame.this.dispose();
                        });
                        t.setRepeats(false);
                        t.start();
                    } else {
                        statusLabel.setText("Login failed. Check credentials.");
                        statusLabel.setForeground(ERROR_CLR);
                        loginBtn.setEnabled(true);
                        loginBtn.setText("Connect");
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                    statusLabel.setForeground(ERROR_CLR);
                    loginBtn.setEnabled(true);
                    loginBtn.setText("Connect");
                }
            }
        };
        worker.execute();
    }
}
