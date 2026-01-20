package com.nhulston.essentials.managers;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.nhulston.essentials.util.ColorUtil;
import com.nhulston.essentials.util.ConfigManager;
import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
public class ChatManager {
    private static final String COLOR_PERMISSION = "essentials.chat.color";
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("&[0-9a-fA-F]|&#[0-9a-fA-F]{6}");
    private final ConfigManager configManager;
    public ChatManager(@Nonnull ConfigManager configManager) {
        this.configManager = configManager;
    }
    @Nonnull
    public PlayerChatEvent.Formatter createFormatter() {
        return this::formatMessage;
    }
    @Nonnull
    public Message formatMessage(@Nonnull PlayerRef sender, @Nonnull String content) {
        String format = getFormatForPlayer(sender.getUuid());
        String sanitizedContent = content;
        if (!PermissionsModule.get().hasPermission(sender.getUuid(), COLOR_PERMISSION)) {
            sanitizedContent = stripColorCodes(content);
        }
        String formatted = format
                .replace("%player%", sender.getUsername())
                .replace("%message%", sanitizedContent);
        return ColorUtil.colorize(formatted);
    }
    @Nonnull
    private String stripColorCodes(@Nonnull String text) {
        return COLOR_CODE_PATTERN.matcher(text).replaceAll("");
    }
    @Nonnull
    private String getFormatForPlayer(@Nonnull UUID playerUuid) {
        Map<String, String> formats = configManager.getChatFormats();
        if (formats.isEmpty()) {
            return configManager.getChatFallbackFormat();
        }
        Set<String> playerGroups = PermissionsModule.get().getGroupsForUser(playerUuid);
        for (Map.Entry<String, String> entry : formats.entrySet()) {
            String groupName = entry.getKey();
            for (String playerGroup : playerGroups) {
                if (playerGroup.equalsIgnoreCase(groupName)) {
                    return entry.getValue();
                }
            }
        }
        return configManager.getChatFallbackFormat();
    }
    public boolean isEnabled() {
        return configManager.isChatEnabled();
    }
}
