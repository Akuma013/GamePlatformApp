package com.gameplatform.ui.panels;

import com.gameplatform.model.Game;
import com.gameplatform.ui.theme.GameForgeTheme;
import com.gameplatform.ui.util.ImageLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * A Steam-style game card: image on top, name + meta below.
 * Clicking the card invokes the supplied callback with the game's ID.
 */
public class GameCard extends JPanel {

    private static final int CARD_W = 230;
    private static final int CARD_H = 230;
    private static final int IMG_W  = 230;
    private static final int IMG_H  = 107;     // ≈ Steam capsule aspect

    public GameCard(Game game, Consumer<Integer> onClick) {
        this(game, onClick, false, null, null);
    }

    public GameCard(Game game, Consumer<Integer> onClick,
                    boolean showFavoriteStar,
                    java.util.function.BiConsumer<Integer, Boolean> onFavoriteToggle) {
        this(game, onClick, showFavoriteStar, onFavoriteToggle, null);
    }

    public GameCard(Game game, Consumer<Integer> onClick,
                    boolean showFavoriteStar,
                    java.util.function.BiConsumer<Integer, Boolean> onFavoriteToggle,
                    Consumer<Integer> onPlay){
        setLayout(new BorderLayout());
        setBackground(GameForgeTheme.BG_CARD);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        setMinimumSize(new Dimension(CARD_W, CARD_H));
        setMaximumSize(new Dimension(CARD_W, CARD_H));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // ---- image ----
        ImageIcon icon = ImageLoader.load(game.getImagePath(), IMG_W, IMG_H);
        JLabel image = new JLabel(icon);
        image.setHorizontalAlignment(SwingConstants.CENTER);
        image.setBounds(0, 0, IMG_W, IMG_H);

        JLayeredPane imageLayer = new JLayeredPane();
        imageLayer.setPreferredSize(new Dimension(IMG_W, IMG_H));
        imageLayer.add(image, JLayeredPane.DEFAULT_LAYER);

        if (showFavoriteStar) {
            JLabel star = buildFavoriteStar(game, onFavoriteToggle);
            star.setBounds(IMG_W - 32, 6, 28, 28);
            imageLayer.add(star, JLayeredPane.PALETTE_LAYER);
        }

        add(imageLayer, BorderLayout.NORTH);

        // ---- text block ----
        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        text.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JLabel name = new JLabel(game.getGameName());
        name.setFont(GameForgeTheme.TITLE);
        name.setForeground(GameForgeTheme.TEXT_BRIGHT);
        name.setAlignmentX(LEFT_ALIGNMENT);

        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        metaRow.setOpaque(false);
        metaRow.setAlignmentX(LEFT_ALIGNMENT);

        JLabel starLabel = new JLabel(String.format("★ %.2f", game.getAvgRating()));
        starLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 11));   // small symbol size
        starLabel.setForeground(GameForgeTheme.TEXT_MUTED);

        JLabel pubLabel = new JLabel(String.format("  •  %s",
                game.getPublisher() == null ? "—" : game.getPublisher()));
        pubLabel.setFont(GameForgeTheme.SMALL);
        pubLabel.setForeground(GameForgeTheme.TEXT_MUTED);

        metaRow.add(starLabel);
        metaRow.add(pubLabel);

        JLabel price = new JLabel(String.format("$%.2f", game.getPrice()));
        price.setFont(GameForgeTheme.TITLE);
        price.setForeground(GameForgeTheme.ACCENT);
        price.setAlignmentX(LEFT_ALIGNMENT);
        price.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        text.add(name);
        text.add(Box.createVerticalStrut(2));
        text.add(metaRow);
        text.add(Box.createVerticalStrut(6));
        text.add(buildPrimaryGenreChip(game));
        text.add(Box.createVerticalStrut(6));

        if (onPlay != null) {
            // Library context: show price + Play button on one row
            JPanel priceRow = new JPanel(new BorderLayout());
            priceRow.setOpaque(false);
            priceRow.setAlignmentX(LEFT_ALIGNMENT);
            priceRow.add(price, BorderLayout.WEST);
            priceRow.add(buildPlayButton(game, onPlay), BorderLayout.EAST);
            text.add(priceRow);
        } else {
            // Default: price alone
            text.add(price);
        }

        add(text, BorderLayout.CENTER);

        // ---- hover and click ----
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                setBackground(GameForgeTheme.BG_PANEL);
            }
            @Override public void mouseExited(MouseEvent e) {
                setBackground(GameForgeTheme.BG_CARD);
            }
            @Override public void mouseClicked(MouseEvent e) {
                if (onClick != null) onClick.accept(game.getGameID());
            }
        });
    }

    private static JButton buildPlayButton(Game game, Consumer<Integer> onPlay) {
        JButton btn = new JButton("Play") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover()
                        ? new Color(0x86b418)        // brighter green on hover
                        : GameForgeTheme.BTN_GREEN;  // standard green
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(70, 26));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                onPlay.accept(game.getGameID());
                e.consume();   // don't propagate to the card's open-detail click
            }
        });
        return btn;
    }

    private static JLabel buildFavoriteStar(Game game,
                                            java.util.function.BiConsumer<Integer, Boolean> onToggle) {
        JLabel star = new JLabel();
        star.setHorizontalAlignment(SwingConstants.CENTER);
        star.setVerticalAlignment(SwingConstants.CENTER);
        star.setFont(new Font("Segoe UI Symbol", Font.BOLD, 22));
        star.setOpaque(false);
        star.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        star.setToolTipText("Toggle favorite");

        paintStar(star, game.isFavorite());

        star.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                boolean nowFav = !game.isFavorite();
                game.setFavorite(nowFav);
                paintStar(star, nowFav);
                if (onToggle != null) onToggle.accept(game.getGameID(), nowFav);
                e.consume();   // don't propagate to the card's open-detail click
            }
        });
        return star;
    }

    private static void paintStar(JLabel star, boolean filled) {
        if (filled) {
            star.setText("★");
            star.setForeground(new Color(0xffd24d));   // gold
        } else {
            star.setText("☆");
            star.setForeground(new Color(0xc7d5e0));   // muted
        }
    }

    private JComponent buildPrimaryGenreChip(com.gameplatform.model.Game game) {
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrap.setOpaque(false);
        wrap.setAlignmentX(LEFT_ALIGNMENT);

        if (game.getGenres().isEmpty()) {
            // Render an invisible spacer so the layout doesn't shift
            // between cards with and without genres.
            JLabel spacer = new JLabel(" ");
            spacer.setFont(GameForgeTheme.SMALL);
            wrap.add(spacer);
        } else {
            wrap.add(new com.gameplatform.ui.util.Chip(game.getGenres().get(0)));
        }
        return wrap;
    }
}