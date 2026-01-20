package com.nhulston.essentials.util;
import com.hypixel.hytale.server.core.Message;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public final class ColorUtil {
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("&([0-9a-fA-F])");
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("&#([0-9a-fA-Fa-f]{6})");
    private static final String[] COLOR_MAP = {
            "#000000",  
            "#0000AA",  
            "#00AA00",  
            "#00AAAA",  
            "#AA0000",  
            "#AA00AA",  
            "#FFAA00",  
            "#AAAAAA",  
            "#555555",  
            "#5555FF",  
            "#55FF55",  
            "#55FFFF",  
            "#FF5555",  
            "#FF55FF",  
            "#FFFF55",  
            "#FFFFFF"   
    };
    private ColorUtil() {}
    @Nonnull
    public static Message colorize(@Nonnull String text) {
        String normalized = text;
        Matcher colorMatcher = COLOR_CODE_PATTERN.matcher(normalized);
        StringBuilder sb = new StringBuilder();
        while (colorMatcher.find()) {
            String code = colorMatcher.group(1).toLowerCase();
            int index = Character.digit(code.charAt(0), 16);
            String hex = COLOR_MAP[index];
            colorMatcher.appendReplacement(sb, Matcher.quoteReplacement("&#" + hex.substring(1)));
        }
        colorMatcher.appendTail(sb);
        normalized = sb.toString();
        List<Message> parts = new ArrayList<>();
        Matcher hexMatcher = HEX_COLOR_PATTERN.matcher(normalized);
        int lastEnd = 0;
        String currentColor = "#FFFFFF";
        while (hexMatcher.find()) {
            if (hexMatcher.start() > lastEnd) {
                String segment = normalized.substring(lastEnd, hexMatcher.start());
                if (!segment.isEmpty()) {
                    parts.add(Message.raw(segment).color(currentColor));
                }
            }
            currentColor = "#" + hexMatcher.group(1).toUpperCase();
            lastEnd = hexMatcher.end();
        }
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
