package com.gameplatform.ui.util;

import com.gameplatform.ui.theme.GameForgeTheme;

import javax.swing.*;
import java.awt.*;

/**

 A small rounded "tag" component — a pill with text inside.
 Used for displaying genres on game cards and in the detail dialog.*
 Two preset styles are provided:
 DEFAULT  : muted grey pill with bright text  (good for cards)
 ACCENT   : Steam-blue pill with white text   (good for the dialog)
 *
 The chip paints its own rounded background so it works on any
 parent regardless of that parent's background colour.*/
public class Chip extends JLabel {


    public enum Style { DEFAULT, ACCENT }

    private final Color bgColor;
    private final int arc;

    public Chip(String text) {
        this(text, Style.DEFAULT);
    }

    public Chip(String text, Style style) {
        super(text);
        setOpaque(false);
        setHorizontalAlignment(CENTER);
        setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        switch (style) {
            case ACCENT -> {
                bgColor = GameForgeTheme.ACCENT;
                setForeground(Color.WHITE);
                setFont(GameForgeTheme.SMALL.deriveFont(Font.BOLD));
            }
            default -> {
                bgColor = new Color(0x3a4d63);            // muted slate
                setForeground(GameForgeTheme.TEXT_BRIGHT);
                setFont(GameForgeTheme.SMALL);
            }
        }

        arc = 14;   // pill corner radius
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }
}