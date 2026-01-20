package com.nhulston.essentials.managers;

import com.nhulston.essentials.util.ConfigManager;
import com.nhulston.essentials.util.Log;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages anti-spam functionality for chat messages.
 * Prevents players from spamming chat and sending duplicate messages.
 *
 * Memory optimized:
 * - Uses ConcurrentHashMap for thread-safe access with minimal locking
 * - Stores only hash of last message instead of full string (saves memory)
 * - Automatically cleans up on player disconnect
 * - Uses primitive long for timestamps (8 bytes vs object overhead)
 */
public class AntiSpamManager {
    private final ConfigManager configManager;

    // Only stores data for online players - cleaned up on disconnect
    private final Map<UUID, PlayerChatData> playerChatData = new ConcurrentHashMap<>();

    public AntiSpamManager(@Nonnull ConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * Checks if a player's message should be blocked due to spam.
     *
     * @param playerUuid The player's UUID
     * @param message The message content
     * @return true if message should be blocked, false if allowed
     */
    public boolean isSpam(@Nonnull UUID playerUuid, @Nonnull String message) {
        if (!configManager.isAntiSpamEnabled()) {
            return false; // Anti-spam disabled
        }

        // Use computeIfAbsent for atomic creation - memory efficient
        PlayerChatData data = playerChatData.computeIfAbsent(playerUuid, k -> new PlayerChatData());
        long currentTime = System.currentTimeMillis();

        // Check delay between messages (1 second = 1000ms)
        long timeSinceLastMessage = currentTime - data.lastMessageTime;
        if (timeSinceLastMessage < configManager.getAntiSpamDelay()) {
            return true; // Sending messages too fast
        }

        // Check for duplicate messages using hash comparison (memory efficient)
        if (configManager.isAntiSpamBlockDuplicates()) {
            int messageHash = message.hashCode();
            if (messageHash == data.lastMessageHash && !message.isEmpty()) {
                return true; // Same message as last one
            }
            data.lastMessageHash = messageHash;
        }

        // Update timestamp
        data.lastMessageTime = currentTime;

        return false; // Message is allowed
    }

    /**
     * Gets the remaining cooldown time for a player in milliseconds.
     *
     * @param playerUuid The player's UUID
     * @return Remaining cooldown in milliseconds, or 0 if no cooldown
     */
    public long getRemainingCooldown(@Nonnull UUID playerUuid) {
        PlayerChatData data = playerChatData.get(playerUuid);
        if (data == null) {
            return 0;
        }

        long timeSinceLastMessage = System.currentTimeMillis() - data.lastMessageTime;
        long cooldown = configManager.getAntiSpamDelay() - timeSinceLastMessage;

        return Math.max(0, cooldown);
    }

    /**
     * Gets the last message sent by a player.
     * Note: For memory efficiency, this returns empty string as we only store hashes.
     *
     * @param playerUuid The player's UUID
     * @return Empty string (we don't store full messages for memory efficiency)
     */
    @Nonnull
    public String getLastMessage(@Nonnull UUID playerUuid) {
        // We don't store the actual message text to save memory
        // We only store the hash for duplicate detection
        return "";
    }

    /**
     * Clears chat data for a player (e.g., when they disconnect).
     * This is important for memory management - prevents memory leaks.
     *
     * @param playerUuid The player's UUID
     */
    public void clearPlayerData(@Nonnull UUID playerUuid) {
        playerChatData.remove(playerUuid);
    }

    /**
     * Gets the number of players currently being tracked.
     * Useful for monitoring memory usage.
     *
     * @return Number of players in the cache
     */
    public int getTrackedPlayerCount() {
        return playerChatData.size();
    }

    /**
     * Stores per-player chat data for spam detection.
     * Memory optimized:
     * - Uses primitive int for hash (4 bytes) instead of storing full String
     * - Uses primitive long for timestamp (8 bytes)
     * - Total: 12 bytes per player + object overhead (~24 bytes) = ~36 bytes per player
     * - Compare to storing String: could be hundreds of bytes per player
     */
    private static class PlayerChatData {
        int lastMessageHash = 0;      // Hash of last message (4 bytes)
        long lastMessageTime = 0;     // Timestamp in milliseconds (8 bytes)
    }
}

