package com.nhulston.essentials.util;

import com.hypixel.hytale.server.core.Message;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for parsing color codes in messages.
 */
public final class ColorUtil {
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("&([0-9a-fA-F])");
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("&#([0-9a-fA-Fa-f]{6})");

    // Standard Minecraft color codes mapped to hex
    private static final String[] COLOR_MAP = {
            "#000000", // &0 - Black
            "#0000AA", // &1 - Dark Blue
            "#00AA00", // &2 - Dark Green
            "#00AAAA", // &3 - Dark Aqua
            "#AA0000", // &4 - Dark Red
            "#AA00AA", // &5 - Dark Purple
            "#FFAA00", // &6 - Gold
            "#AAAAAA", // &7 - Gray
            "#555555", // &8 - Dark Gray
            "#5555FF", // &9 - Blue
            "#55FF55", // &a - Green
            "#55FFFF", // &b - Aqua
            "#FF5555", // &c - Red
            "#FF55FF", // &d - Light Purple
            "#FFFF55", // &e - Yellow
            "#FFFFFF"  // &f - White
    };

    private ColorUtil() {}

    /**
     * Parses color codes (&0-&f and &#RRGGBB) and returns a colored Message.
     */
    @Nonnull
    public static Message colorize(@Nonnull String text) {
        // First, convert all color codes to a normalized format
        String normalized = text;

        // Replace standard color codes (&0-&f) with hex equivalents
        Matcher colorMatcher = COLOR_CODE_PATTERN.matcher(normalized);
        StringBuilder sb = new StringBuilder();
        while (colorMatcher.find()) {
            String code = colorMatcher.group(1).toLowerCase();
            int index = Character.digit(code.charAt(0), 16);
            String hex = COLOR_MAP[index];
            colorMatcher.appendReplacement(sb, "&#" + hex.substring(1));
        }
        colorMatcher.appendTail(sb);
        normalized = sb.toString();

        // Now parse the string with hex color codes
        List<Message> parts = new ArrayList<>();
        Matcher hexMatcher = HEX_COLOR_PATTERN.matcher(normalized);
        int lastEnd = 0;
        String currentColor = "#FFFFFF";

        while (hexMatcher.find()) {
            // Add text before this color code
            if (hexMatcher.start() > lastEnd) {
                String segment = normalized.substring(lastEnd, hexMatcher.start());
                if (!segment.isEmpty()) {
                    parts.add(Message.raw(segment).color(currentColor));
                }
            }

            // Update current color
            currentColor = "#" + hexMatcher.group(1).toUpperCase();
            lastEnd = hexMatcher.end();
        }

        // Add remaining text
        if (lastEnd < normalized.length()) {
            String segment = normalized.substring(lastEnd);
            if (!segment.isEmpty()) {
                parts.add(Message.raw(segment).color(currentColor));
            }
        }

        if (parts.isEmpty()) {
            return Message.raw(text);
        } else if (parts.size() == 1) {
            return parts.getFirst();
        } else {
            return Message.join(parts.toArray(new Message[0]));
        }
    }
}
