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
        image.setPreferredSize(new Dimension(IMG_W, IMG_H));
        add(image, BorderLayout.NORTH);

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
        text.add(price);
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