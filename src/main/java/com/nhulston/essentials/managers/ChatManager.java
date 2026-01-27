package com.nhulston.essentials.managers;
import com.buuz135.simpleclaims.claim.ClaimManager;
import com.buuz135.simpleclaims.claim.party.PartyInfo;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.nhulston.essentials.util.ColorUtil;
import com.nhulston.essentials.util.ConfigManager;
import javax.annotation.Nonnull;
import java.util.List;
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

        // Get party name from SimpleClaims
        String partyName = getPartyName(sender.getUuid());

        String formatted = format
                .replace("%player%", sender.getUsername())
                .replace("%party%", partyName)
                .replace("%message%", sanitizedContent);
        return ColorUtil.colorize(formatted);
    }

    /**
     * Gets the party name for a player from HysteriaClaims (SimpleClaims).
     * Returns empty string if player is not in a party.
     */
    @Nonnull
    private String getPartyName(@Nonnull UUID playerUuid) {
        try {
            ClaimManager claimManager = ClaimManager.getInstance();
            PartyInfo party = claimManager.getPartyFromPlayer(playerUuid);

            if (party != null) {
                return party.getName();
            }
        } catch (Exception e) {
            // Silently handle any errors (e.g., if HysteriaClaims is not loaded)
        }

        return "";
    }
    @Nonnull
    private String stripColorCodes(@Nonnull String text) {
        return COLOR_CODE_PATTERN.matcher(text).replaceAll("");
    }
    @Nonnull
    private String getFormatForPlayer(@Nonnull UUID playerUuid) {
        List<ConfigManager.ChatFormat> formats = configManager.getChatFormats();

        if (formats.isEmpty()) {
            return configManager.getChatFallbackFormat();
        }

        Set<String> playerGroups = PermissionsModule.get().getGroupsForUser(playerUuid);

        // Check each configured format in order (List preserves insertion order)
        for (ConfigManager.ChatFormat chatFormat : formats) {
            // Check if player is in this group (case-insensitive)
            for (String playerGroup : playerGroups) {
                if (playerGroup.equalsIgnoreCase(chatFormat.group())) {
                    return chatFormat.format();
                }
            }
        }

        return configManager.getChatFallbackFormat();
    }
    public boolean isEnabled() {
        return configManager.isChatEnabled();
    }
}
