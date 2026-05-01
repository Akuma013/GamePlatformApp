package com.gameplatform.ui.panels;

import com.gameplatform.db.DBConnection;
import com.gameplatform.db.LibraryDAO;
import com.gameplatform.db.OrderDAO;
import com.gameplatform.db.WishlistDAO;
import com.gameplatform.model.Game;
import com.gameplatform.ui.theme.GameForgeTheme;
import com.gameplatform.ui.util.ImageLoader;
import com.gameplatform.db.ReviewDAO;
import com.gameplatform.model.Review;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Unified game detail view. The same dialog opens from Store, Library,
 * and Wishlist; the action buttons it shows depend on the user's
 * relationship with the game:
 *
 *   - Doesn't own AND not on wishlist:  [Buy Now] [Add to Wishlist]
 *   - Doesn't own AND on wishlist:      [Buy Now] [Remove from Wishlist]
 *   - Owns:                              [Remove from Library] (+ "In your library")
 *
 * After any successful action the dialog calls onChange.run() so the
 * panel that opened it can refresh.
 */
public class GameDetailDialog extends JDialog {

    private static final int IMG_W = 460;
    private static final int IMG_H = 215;

    private final Game game;
    private final Runnable onChange;

    public GameDetailDialog(Window owner, Game game, Runnable onChange) {
        super(owner, game.getGameName(), ModalityType.APPLICATION_MODAL);
        this.game = game;
        this.onChange = onChange;

        setSize(640, 700);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(GameForgeTheme.BG_DARK);
        setContentPane(root);

        root.add(buildHero(),    BorderLayout.NORTH);
        root.add(buildBody(),    BorderLayout.CENTER);
        root.add(buildActions(), BorderLayout.SOUTH);
    }

    /** Big banner image at the top. */
    private JComponent buildHero() {
        ImageIcon icon = ImageLoader.load(game.getImagePath(), IMG_W, IMG_H);
        JLabel hero = new JLabel(icon, SwingConstants.CENTER);
        hero.setOpaque(true);
        hero.setBackground(GameForgeTheme.BG_CARD);
        hero.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                GameForgeTheme.BG_PANEL));
        return hero;
    }

    /** Title, publisher, version, size, rating, price. */
    private JComponent buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(GameForgeTheme.BG_DARK);
        body.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel title = headingLabel(game.getGameName(), GameForgeTheme.HEADER,
                GameForgeTheme.TEXT_BRIGHT);
        body.add(title);
        body.add(Box.createVerticalStrut(4));

        JLabel pub = headingLabel(
                game.getPublisher() == null ? "Unknown publisher" : game.getPublisher(),
                GameForgeTheme.BODY, GameForgeTheme.TEXT_MUTED);
        body.add(pub);
        body.add(Box.createVerticalStrut(18));

        body.add(buildRatingRow(game.getAvgRating()));
        body.add(infoRow("Version",  game.getVersion() == null ? "—" : game.getVersion()));
        body.add(infoRow("Size",     game.getGameSize() + " MB"));
        body.add(Box.createVerticalStrut(20));

        JLabel price = headingLabel(
                String.format("$%.2f", game.getPrice()),
                GameForgeTheme.HEADER, GameForgeTheme.ACCENT);
        body.add(price);
        body.add(Box.createVerticalStrut(20));

        body.add(headingLabel("Reviews", GameForgeTheme.TITLE,
                GameForgeTheme.TEXT_BRIGHT));
        body.add(Box.createVerticalStrut(8));
        body.add(buildReviewsList());

        return body;
    }
    private JComponent buildRatingRow(double avg) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel labelText = new JLabel("Rating:  ");
        labelText.setFont(GameForgeTheme.BODY);
        labelText.setForeground(GameForgeTheme.TEXT_MUTED);

        JLabel star = new JLabel("★ ");
        star.setFont(GameForgeTheme.SYMBOL_LARGE);     // ← symbol font here
        star.setForeground(GameForgeTheme.ACCENT);

        JLabel value = new JLabel(String.format("%.2f / 10", avg));
        value.setFont(GameForgeTheme.BODY);
        value.setForeground(GameForgeTheme.TEXT_BRIGHT);

        row.add(labelText);
        row.add(star);
        row.add(value);
        return row;
    }

    private JComponent buildReviewsList() {
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(GameForgeTheme.BG_DARK);
        list.setAlignmentX(LEFT_ALIGNMENT);

        try {
            List<Review> reviews = ReviewDAO.listForGame(game.getGameID());
            if (reviews.isEmpty()) {
                JLabel empty = headingLabel("No reviews yet.",
                        GameForgeTheme.SMALL, GameForgeTheme.TEXT_MUTED);
                list.add(empty);
            } else {
                for (Review r : reviews) {
                    list.add(buildReviewRow(r));
                    list.add(Box.createVerticalStrut(6));
                }
            }
        } catch (Exception ex) {
            JLabel err = headingLabel("Could not load reviews: " + ex.getMessage(),
                    GameForgeTheme.SMALL, new Color(0xff6b6b));
            list.add(err);
        }

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setPreferredSize(new Dimension(0, 140));
        scroll.setAlignmentX(LEFT_ALIGNMENT);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JComponent buildReviewRow(Review r) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(GameForgeTheme.BG_CARD);
        row.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        row.setAlignmentX(LEFT_ALIGNMENT);

        int safeRating = Math.max(0, Math.min(10, r.getRating()));
        String stars = "★".repeat(safeRating) + "☆".repeat(10 - safeRating);
        String date = r.getReviewDate() == null ? "" :
                r.getReviewDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy"));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        header.setOpaque(false);

        JLabel starsLabel = new JLabel(stars);
        starsLabel.setFont(GameForgeTheme.SYMBOL);            // ← symbol font here
        starsLabel.setForeground(GameForgeTheme.ACCENT);

        JLabel meta = new JLabel(String.format("%s   •   %s", r.getUserID(), date));
        meta.setFont(GameForgeTheme.BODY);
        meta.setForeground(GameForgeTheme.ACCENT);

        header.add(starsLabel);
        header.add(meta);

        JLabel body = new JLabel(
                "<html><div style='width:540px;'>" + escape(r.getDescription()) + "</div></html>");
        body.setFont(GameForgeTheme.BODY);
        body.setForeground(GameForgeTheme.TEXT);
        body.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        row.add(header, BorderLayout.NORTH);
        row.add(body,   BorderLayout.CENTER);
        return row;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** Bottom button bar. Decides which buttons to show based on DB state. */
    private JComponent buildActions() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 14));
        bar.setBackground(GameForgeTheme.BG_PANEL);
        bar.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));

        try {
            String me = DBConnection.getAppUsername();
            boolean owns      = LibraryDAO.owns(me, game.getGameID());
            boolean wishlisted = WishlistDAO.contains(me, game.getGameID());

            if (owns) {
                JLabel ownedTag = headingLabel("In your library",
                        GameForgeTheme.BODY, new Color(0x86b418));
                bar.add(ownedTag);

                if (!ReviewDAO.hasReviewed(me, game.getGameID())) {
                    bar.add(styled("Write a review", GameForgeTheme.ACCENT,
                            Color.WHITE, e -> openWriteReview()));
                }

                bar.add(styled("Remove from Library", GameForgeTheme.BG_CARD,
                        GameForgeTheme.TEXT_BRIGHT, e -> doRemoveFromLibrary()));
            } else {
                bar.add(styled("Buy Now", GameForgeTheme.BTN_GREEN,
                        Color.WHITE, e -> doPurchase()));
                if (wishlisted) {
                    bar.add(styled("Remove from Wishlist", GameForgeTheme.BG_CARD,
                            GameForgeTheme.TEXT_BRIGHT, e -> doRemoveFromWishlist()));
                } else {
                    bar.add(styled("Add to Wishlist", GameForgeTheme.ACCENT,
                            Color.WHITE, e -> doAddToWishlist()));
                }
            }

            bar.add(styled("Close", GameForgeTheme.BG_CARD,
                    GameForgeTheme.TEXT_BRIGHT, e -> dispose()));

        } catch (SQLException ex) {
            JLabel err = headingLabel("Error: " + ex.getMessage(),
                    GameForgeTheme.SMALL, new Color(0xff6b6b));
            bar.add(err);
        }
        return bar;
    }

    private void openWriteReview() {
        new WriteReviewDialog(
                SwingUtilities.getWindowAncestor(this),
                game.getGameName(),
                game.getGameID(),
                () -> {
                    // After a successful review, close the detail dialog and ask
                    // the caller to refresh — the game's avg rating may have changed.
                    fireChange();
                    dispose();
                }
        ).setVisible(true);
    }

    // ---------------------------------------------------------------- //
    //  Action handlers                                                   //
    // ---------------------------------------------------------------- //

    private void doPurchase() {
        try {
            String me = DBConnection.getAppUsername();
            OrderDAO.purchase(me, game.getGameID());
            // After purchase, also clean up wishlist if present
            if (WishlistDAO.contains(me, game.getGameID())) {
                WishlistDAO.remove(me, game.getGameID());
            }
            JOptionPane.showMessageDialog(this,
                    "Purchased! Added to your library.");
            fireChange();
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not complete purchase:\n" + ex.getMessage(),
                    "Purchase failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doAddToWishlist() {
        try {
            WishlistDAO.add(DBConnection.getAppUsername(), game.getGameID());
            fireChange();
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doRemoveFromWishlist() {
        try {
            WishlistDAO.remove(DBConnection.getAppUsername(), game.getGameID());
            fireChange();
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doRemoveFromLibrary() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Remove this game from your library?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;
        try {
            LibraryDAO.remove(DBConnection.getAppUsername(), game.getGameID());
            fireChange();
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fireChange() {
        if (onChange != null) onChange.run();
    }

    // ---------------------------------------------------------------- //
    //  UI helpers                                                        //
    // ---------------------------------------------------------------- //

    private JLabel headingLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JComponent infoRow(String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel l = new JLabel(label + ":  ");
        l.setFont(GameForgeTheme.BODY);
        l.setForeground(GameForgeTheme.TEXT_MUTED);

        JLabel v = new JLabel(value);
        v.setFont(GameForgeTheme.BODY);
        v.setForeground(GameForgeTheme.TEXT_BRIGHT);

        row.add(l);
        row.add(v);
        return row;
    }

    private JButton styled(String text, Color bg, Color fg,
                           java.awt.event.ActionListener handler) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(GameForgeTheme.TITLE);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(handler);
        return b;
    }
}