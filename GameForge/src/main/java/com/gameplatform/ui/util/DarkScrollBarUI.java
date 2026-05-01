package com.gameplatform.ui.util;

import com.gameplatform.ui.theme.GameForgeTheme;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

/**
 * A flat, thin scroll-bar UI that matches the dark theme.
 * - Track is the panel background (effectively invisible).
 * - Thumb is a slim rounded pill in muted grey.
 * - Up/Down arrow buttons are removed (zero-size).
 *
 * Usage:
 *   scrollPane.getVerticalScrollBar().setUI(new DarkScrollBarUI());
 *   scrollPane.getHorizontalScrollBar().setUI(new DarkScrollBarUI());
 */
public class DarkScrollBarUI extends BasicScrollBarUI {

    private static final int THUMB_W = 8;
    private static final int ARC     = 8;

    @Override
    protected void configureScrollBarColors() {
        thumbColor          = new Color(0x4f6275);   // muted slate
        thumbHighlightColor = new Color(0x66c0f4);   // accent on hover
        thumbLightShadowColor = thumbColor;
        thumbDarkShadowColor  = thumbColor;
        trackColor = GameForgeTheme.BG_DARK;
        trackHighlightColor = GameForgeTheme.BG_DARK;
    }

    @Override
    protected JButton createDecreaseButton(int orientation) { return zeroButton(); }

    @Override
    protected JButton createIncreaseButton(int orientation) { return zeroButton(); }

    private JButton zeroButton() {
        JButton b = new JButton();
        b.setPreferredSize(new Dimension(0, 0));
        b.setMinimumSize(new Dimension(0, 0));
        b.setMaximumSize(new Dimension(0, 0));
        return b;
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        g.setColor(GameForgeTheme.BG_DARK);
        g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        boolean hover = isThumbRollover() || isDragging;
        g2.setColor(hover ? new Color(0x66c0f4) : new Color(0x4f6275));

        // Center the thumb horizontally (or vertically for horizontal scroll bars)
        int x, y, w, h;
        if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
            w = THUMB_W;
            h = thumbBounds.height - 4;
            x = thumbBounds.x + (thumbBounds.width - w) / 2;
            y = thumbBounds.y + 2;
        } else {
            w = thumbBounds.width - 4;
            h = THUMB_W;
            x = thumbBounds.x + 2;
            y = thumbBounds.y + (thumbBounds.height - h) / 2;
        }

        g2.fillRoundRect(x, y, w, h, ARC, ARC);
        g2.dispose();
    }

    @Override
    protected Dimension getMinimumThumbSize() {
        return new Dimension(THUMB_W, 30);
    }
}