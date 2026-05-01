package com.gameplatform.ui.util;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/*

Loads and caches game images from the classpath.*
Image paths in the DB are stored as classpath-relative strings like
"images/half_life_2.jpg". This class resolves them via the classloader
        so they work the same way in IDE runs and in a packaged JAR.*
Loaded icons are cached by (path, width, height) so repeated calls
for the same card size are free after the first paint.*/
public class ImageLoader {

    private static final String PLACEHOLDER = "images/placeholder.jpg";
    private static final Map<String, ImageIcon> cache = new HashMap<>();

    /*

    Load an image scaled to fit the requested width/height.
    Falls back to the placeholder if the path can't be resolved.*/
    public static ImageIcon load(String path, int width, int height) {
        String key = path + "@" + width + "x" + height;
        ImageIcon cached = cache.get(key);
        if (cached != null) return cached;

        ImageIcon icon = loadRaw(path);
        if (icon == null) icon = loadRaw(PLACEHOLDER);
        if (icon == null) return null;        // not even the placeholder is there

        Image scaled = icon.getImage().getScaledInstance(
                width, height, Image.SCALE_SMOOTH);
        ImageIcon result = new ImageIcon(scaled);
        cache.put(key, result);
        return result;
    }

    private static ImageIcon loadRaw(String classpathRelative) {
        if (classpathRelative == null || classpathRelative.isBlank()) return null;
        URL url = ImageLoader.class.getClassLoader().getResource(classpathRelative);
        return url == null ? null : new ImageIcon(url);
    }
}