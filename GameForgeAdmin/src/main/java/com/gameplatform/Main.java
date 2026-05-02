package com.gameplatform;

import com.gameplatform.ui.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Use system look for native OS integration, then override colors in panels
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
