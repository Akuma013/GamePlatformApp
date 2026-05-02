package com.gameplatform.ui.panels;

import com.gameplatform.model.Game;
import com.gameplatform.ui.theme.GameForgeTheme;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * One curated row on the Store landing — heading + a sliding window of
 * game cards with left/right arrow navigation. No native scroll bar:
 * the user steps through the row by clicking the arrows.
 *
 * Renders nothing if the section is empty.
 */
public class SectionRow extends JPanel {

    private static final int VISIBLE_CARDS = 5;
    private static final int ROW_HEIGHT    = 250;
    private static final int ARROW_WIDTH   = 44;

    private final List<Game> games;
    private final Consumer<Integer> onCardClick;
    private final JPanel cardStrip = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
    private final JButton leftBtn  = buildArrowButton("‹", -1);
    private final JButton rightBtn = buildArrowButton("›", +1);

    private int windowStart = 0;     // index of first visible card

    public SectionRow(String title, String subtitle, List<Game> games,
                      Consumer<Integer> onCardClick) {
        this.games = games;
        this.onCardClick = onCardClick;

        setLayout(new BorderLayout());
        setBackground(GameForgeTheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        if (games == null || games.isEmpty()) {
            setPreferredSize(new Dimension(0, 0));
            setMaximumSize(new Dimension(0, 0));
            return;
        }

        add(buildHeading(title, subtitle), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        // Hide arrows if the entire section fits in one window.
        if (games.size() <= 1) {
            leftBtn.setVisible(false);
            rightBtn.setVisible(false);
        }
        renderWindow();
        cardStrip.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                renderWindow();
            }
        });

    }

    /** Centered heading: title and subtitle stacked above the strip. */
    private JComponent buildHeading(String title, String subtitle) {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 24, 8, 24));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(GameForgeTheme.TEXT_BRIGHT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(titleLabel);

        if (subtitle != null && !subtitle.isBlank()) {
            JLabel sub = new JLabel(subtitle);
            sub.setFont(GameForgeTheme.SMALL);
            sub.setForeground(GameForgeTheme.TEXT_MUTED);
            sub.setAlignmentX(Component.CENTER_ALIGNMENT);
            sub.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
            header.add(sub);
        }
        return header;
    }

    /** Body row: [ ‹ ] [ ...cards... ] [ › ] */
    private JComponent buildBody() {
        cardStrip.setBackground(GameForgeTheme.BG_DARK);
        cardStrip.setPreferredSize(new Dimension(0, ROW_HEIGHT));

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(GameForgeTheme.BG_DARK);
        body.add(leftBtn, BorderLayout.WEST);
        body.add(cardStrip, BorderLayout.CENTER);
        body.add(rightBtn, BorderLayout.EAST);
        return body;
    }


    /** Show the current window of cards. */
    private void renderWindow() {
        cardStrip.removeAll();

        // How many cards fit at this width?
        int stripWidth = cardStrip.getWidth();
        int visible;
        if (stripWidth <= 0) {
            // First render before layout has happened — pick a sensible default
            visible = 4;
        } else {
            // each card = 230 wide + 14 gap; subtract one gap because there's
            // no gap after the last card
            visible = Math.max(1, (stripWidth + 14) / (230 + 14));
            visible = Math.min(visible, 6);  // cap at 6 even on huge displays
        }

        // Clamp the window so we never go past the end
        int maxStart = Math.max(0, games.size() - visible);
        if (windowStart > maxStart) windowStart = maxStart;

        int end = Math.min(windowStart + visible, games.size());
        for (int i = windowStart; i < end; i++) {
            cardStrip.add(new GameCard(games.get(i), onCardClick));
        }
        leftBtn.setEnabled(windowStart > 0);
        rightBtn.setEnabled(end < games.size());
        cardStrip.revalidate();
        cardStrip.repaint();
        // Re-render when the section's width changes so we adapt visible-count.

    }




    private void scrollBy(int direction) {
        int newStart = windowStart + direction;
        // Clamp so we don't shift past either end.
        newStart = Math.max(0, Math.min(newStart, games.size() - VISIBLE_CARDS));
        if (newStart != windowStart) {
            windowStart = newStart;
            renderWindow();
        }
    }

    private JButton buildArrowButton(String glyph, int direction) {
        JButton b = new JButton(glyph) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = isEnabled()
                        ? (getModel().isRollover()
                        ? GameForgeTheme.ACCENT
                        : new Color(0x3a4d63))
                        : new Color(0x2a3540);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 22));
        b.setForeground(GameForgeTheme.TEXT_BRIGHT);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(ARROW_WIDTH, ROW_HEIGHT - 20));
        b.setMinimumSize(new Dimension(ARROW_WIDTH, ROW_HEIGHT - 20));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> scrollBy(direction));
        return b;
    }

}