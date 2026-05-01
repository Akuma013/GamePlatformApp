package com.gameplatform.ui.panels;

import com.gameplatform.db.DBConnection;
import com.gameplatform.db.LibraryDAO;
import com.gameplatform.db.OrderDAO;
import com.gameplatform.db.ReviewDAO;
import com.gameplatform.db.WishlistDAO;
import com.gameplatform.model.Game;
import com.gameplatform.model.Review;
import com.gameplatform.ui.theme.GameForgeTheme;
import com.gameplatform.ui.util.Chip;
import com.gameplatform.ui.util.DarkScrollBarUI;
import com.gameplatform.ui.util.ImageLoader;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GameDetailDialog extends JDialog {

    private static final int DIALOG_W = 800;
    private static final int DIALOG_H = 860;
    private static final int HERO_W   = 760;
    private static final int HERO_H   = 280;

    // The reviews list inner labels need a fixed wrap width so the
    // text wraps but never triggers horizontal scrolling.
    private static final int REVIEW_TEXT_WIDTH = 690;

    private final Game game;
    private final Runnable onChange;

    public GameDetailDialog(Window owner, Game game, Runnable onChange) {
        super(owner, game.getGameName(), ModalityType.APPLICATION_MODAL);
        this.game = game;
        this.onChange = onChange;

        setSize(DIALOG_W, DIALOG_H);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(GameForgeTheme.BG_DARK);
        setContentPane(root);

        root.add(buildHero(),    BorderLayout.NORTH);
        root.add(buildBody(),    BorderLayout.CENTER);
        root.add(buildActions(), BorderLayout.SOUTH);
    }

    // ============================================================== //
    //  HERO                                                           //
    // ============================================================== //

    private JComponent buildHero() {
        ImageIcon icon = ImageLoader.load(game.getImagePath(), HERO_W, HERO_H);
        JLabel hero = new JLabel(icon, SwingConstants.CENTER);
        hero.setOpaque(true);
        hero.setBackground(GameForgeTheme.BG_CARD);
        hero.setPreferredSize(new Dimension(HERO_W, HERO_H));
        hero.setBorder(new MatteBorder(0, 0, 2, 0, GameForgeTheme.ACCENT));
        return hero;
    }

    // ============================================================== //
    //  BODY                                                           //
    // ============================================================== //

    private JComponent buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(GameForgeTheme.BG_DARK);
        body.setBorder(BorderFactory.createEmptyBorder(20, 28, 12, 28));

        // ---- title + publisher ----
        JLabel title = new JLabel(game.getGameName());
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(GameForgeTheme.TEXT_BRIGHT);
        title.setAlignmentX(LEFT_ALIGNMENT);
        body.add(title);

        body.add(Box.createVerticalStrut(2));
        JLabel publisher = new JLabel(
                game.getPublisher() == null ? "Unknown publisher" : game.getPublisher());
        publisher.setFont(GameForgeTheme.BODY);
        publisher.setForeground(GameForgeTheme.TEXT_MUTED);
        publisher.setAlignmentX(LEFT_ALIGNMENT);
        body.add(publisher);

        // ---- genre chips row ----
        body.add(Box.createVerticalStrut(14));
        body.add(buildChipsRow());

        // ---- separator ----
        body.add(Box.createVerticalStrut(18));
        body.add(buildSeparator());
        body.add(Box.createVerticalStrut(14));

        // ---- 2x2 stats grid ----
        body.add(buildStatsGrid());

        // ---- separator ----
        body.add(Box.createVerticalStrut(18));
        body.add(buildSeparator());
        body.add(Box.createVerticalStrut(14));

        // ---- reviews ----
        body.add(buildReviewsSection());

        return body;
    }

    private JComponent buildChipsRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        if (game.getGenres().isEmpty()) {
            JLabel none = new JLabel("No genre tags");
            none.setFont(GameForgeTheme.SMALL);
            none.setForeground(GameForgeTheme.TEXT_MUTED);
            row.add(none);
        } else {
            for (String genre : game.getGenres()) {
                row.add(new Chip(genre, Chip.Style.ACCENT));
            }
        }
        return row;
    }

    private JComponent buildSeparator() {
        JPanel sep = new JPanel();
        sep.setBackground(new Color(0x2a3f54));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setPreferredSize(new Dimension(0, 1));
        sep.setAlignmentX(LEFT_ALIGNMENT);
        return sep;
    }

    private JComponent buildStatsGrid() {
        JPanel stats = new JPanel(new GridLayout(2, 2, 24, 8));
        stats.setOpaque(false);
        stats.setAlignmentX(LEFT_ALIGNMENT);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        stats.add(buildStat("Rating",
                String.format("%.2f / 10", game.getAvgRating()),
                GameForgeTheme.ACCENT, true));
        stats.add(buildStat("Price",
                String.format("$%.2f", game.getPrice()),
                GameForgeTheme.ACCENT, true));
        stats.add(buildStat("Version",
                game.getVersion() == null ? "—" : game.getVersion(),
                GameForgeTheme.TEXT_BRIGHT, false));
        stats.add(buildStat("Size",
                game.getGameSize() + " MB",
                GameForgeTheme.TEXT_BRIGHT, false));
        return stats;
    }

    private JComponent buildStat(String label, String value, Color valueColor, boolean big) {
        JPanel cell = new JPanel();
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        cell.setOpaque(false);

        JLabel l = new JLabel(label);
        l.setFont(GameForgeTheme.SMALL);
        l.setForeground(GameForgeTheme.TEXT_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);

        JLabel v = new JLabel(value);
        v.setFont(big
                ? new Font("Segoe UI", Font.BOLD, 22)
                : new Font("Segoe UI", Font.PLAIN, 15));
        v.setForeground(valueColor);
        v.setAlignmentX(LEFT_ALIGNMENT);

        cell.add(l);
        cell.add(Box.createVerticalStrut(2));
        cell.add(v);
        return cell;
    }

    // ============================================================== //
    //  REVIEWS                                                        //
    // ============================================================== //

    private JComponent buildReviewsSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(LEFT_ALIGNMENT);

        try {
            List<Review> reviews = ReviewDAO.listForGame(game.getGameID());

            JLabel header = new JLabel(
                    "Reviews  " + (reviews.isEmpty() ? "" : "(" + reviews.size() + ")"));
            header.setFont(new Font("Segoe UI", Font.BOLD, 18));
            header.setForeground(GameForgeTheme.TEXT_BRIGHT);
            header.setAlignmentX(LEFT_ALIGNMENT);
            section.add(header);
            section.add(Box.createVerticalStrut(10));

            if (reviews.isEmpty()) {
                JLabel empty = new JLabel("No reviews yet. Be the first.");
                empty.setFont(GameForgeTheme.BODY);
                empty.setForeground(GameForgeTheme.TEXT_MUTED);
                empty.setAlignmentX(LEFT_ALIGNMENT);
                section.add(empty);
            } else {
                section.add(buildReviewsList(reviews));
            }
        } catch (SQLException ex) {
            JLabel err = new JLabel("Could not load reviews: " + ex.getMessage());
            err.setForeground(new Color(0xff6b6b));
            err.setAlignmentX(LEFT_ALIGNMENT);
            section.add(err);
        }

        return section;
    }

    private JComponent buildReviewsList(List<Review> reviews) {
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(GameForgeTheme.BG_DARK);

        for (Review r : reviews) {
            list.add(buildReviewRow(r));
            list.add(Box.createVerticalStrut(8));
        }

        JScrollPane scroll = new JScrollPane(list,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);    // ← never horizontal
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setPreferredSize(new Dimension(0, 240));     // ← much taller
        scroll.setAlignmentX(LEFT_ALIGNMENT);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        return scroll;
    }

    private JComponent buildReviewRow(Review r) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setBackground(GameForgeTheme.BG_CARD);
        row.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // ----- header line: stars + user + date -----
        int safeRating = Math.max(0, Math.min(10, r.getRating()));
        String stars = "★".repeat(safeRating) + "☆".repeat(10 - safeRating);
        String date = r.getReviewDate() == null ? "" :
                r.getReviewDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy"));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        header.setOpaque(false);
        header.setAlignmentX(LEFT_ALIGNMENT);

        JLabel starsLabel = new JLabel(stars);
        starsLabel.setFont(GameForgeTheme.SYMBOL);
        starsLabel.setForeground(GameForgeTheme.ACCENT);

        JLabel meta = new JLabel(String.format("%s   •   %s", r.getUserID(), date));
        meta.setFont(GameForgeTheme.BODY);
        meta.setForeground(GameForgeTheme.ACCENT);

        header.add(starsLabel);
        header.add(meta);
        row.add(header);

        // ----- description (plain text wrapping, no HTML scroll quirks) -----
        JLabel description = new JLabel(toWrappingHtml(r.getDescription()));
        description.setFont(GameForgeTheme.BODY);
        description.setForeground(GameForgeTheme.TEXT);
        description.setAlignmentX(LEFT_ALIGNMENT);
        description.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        row.add(description);

        return row;
    }

    /**
     * Wrap text to a known width so JLabel renders it on multiple lines
     * without ever needing a horizontal scrollbar.
     */
    private static String toWrappingHtml(String text) {
        if (text == null) return "";
        String escaped = text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
        return "<html><div style='width:" + REVIEW_TEXT_WIDTH + "px;'>"
                + escaped + "</div></html>";
    }

    // ============================================================== //
    //  ACTIONS                                                        //
    // ============================================================== //

    private JComponent buildActions() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 14));
        bar.setBackground(GameForgeTheme.BG_PANEL);
        bar.setBorder(BorderFactory.createEmptyBorder(8, 22, 8, 22));

        try {
            String me = DBConnection.getAppUsername();
            boolean owns       = LibraryDAO.owns(me, game.getGameID());
            boolean wishlisted = WishlistDAO.contains(me, game.getGameID());

            if (owns) {
                JLabel ownedTag = new JLabel("✓  In your library");
                ownedTag.setFont(GameForgeTheme.BODY);
                ownedTag.setForeground(new Color(0x86b418));
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
            JLabel err = new JLabel("Error: " + ex.getMessage());
            err.setForeground(new Color(0xff6b6b));
            bar.add(err);
        }
        return bar;
    }

    // ============================================================== //
    //  ACTION HANDLERS  (unchanged from previous version)             //
    // ============================================================== //

    private void doPurchase() {
        try {
            String me = DBConnection.getAppUsername();
            OrderDAO.purchase(me, game.getGameID());
            if (WishlistDAO.contains(me, game.getGameID())) {
                WishlistDAO.remove(me, game.getGameID());
            }
            JOptionPane.showMessageDialog(this, "Purchased! Added to your library.");
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
            fireChange(); dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doRemoveFromWishlist() {
        try {
            WishlistDAO.remove(DBConnection.getAppUsername(), game.getGameID());
            fireChange(); dispose();
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
            fireChange(); dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openWriteReview() {
        new WriteReviewDialog(
                SwingUtilities.getWindowAncestor(this),
                game.getGameName(),
                game.getGameID(),
                () -> { fireChange(); dispose(); }
        ).setVisible(true);
    }

    private void fireChange() {
        if (onChange != null) onChange.run();
    }

    // ============================================================== //
    //  UI HELPERS                                                     //
    // ============================================================== //

    private JButton styled(String text, Color bg, Color fg,
                           java.awt.event.ActionListener handler) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(fg);
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