package com.gameplatform.ui.theme;

import java.awt.Color;
import java.awt.Font;

/**
 * Centralised colours and fonts for a Steam-like dark theme.
 * Every panel pulls its styling from here so the look stays consistent.
 */
public class GameForgeTheme {
    public static final Font SYMBOL       = new Font("Segoe UI Symbol", Font.PLAIN, 13);
    public static final Font SYMBOL_LARGE = new Font("Segoe UI Symbol", Font.PLAIN, 16);
    // ---- backgrounds ----
    public static final Color BG_DARK   = new Color(0x1b2838); // page background
    public static final Color BG_PANEL  = new Color(0x2a475e); // header / panel
    public static final Color BG_CARD   = new Color(0x16202d); // game card

    // ---- accents ----
    public static final Color ACCENT     = new Color(0x66c0f4); // Steam blue
    public static final Color BTN_GREEN  = new Color(0x5c7e10); // "Buy" green
    public static final Color BTN_GREEN_HOVER = new Color(0x86b418);

    // ---- text ----
    public static final Color TEXT        = new Color(0xc7d5e0);
    public static final Color TEXT_BRIGHT = new Color(0xffffff);
    public static final Color TEXT_MUTED  = new Color(0x8f98a0);

    // ---- fonts ----
    public static final Font HEADER = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font TITLE  = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font SMALL  = new Font("Segoe UI", Font.PLAIN, 11);

    private GameForgeTheme() {}  // utility class — no instances
}