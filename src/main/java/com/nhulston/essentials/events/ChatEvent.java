package com.nhulston.essentials.events;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.nhulston.essentials.managers.AntiSpamManager;
import com.nhulston.essentials.managers.ChatManager;
import javax.annotation.Nonnull;
public class ChatEvent {
    private final ChatManager chatManager;
    private final AntiSpamManager antiSpamManager;
    public ChatEvent(@Nonnull ChatManager chatManager, @Nonnull AntiSpamManager antiSpamManager) {
        this.chatManager = chatManager;
        this.antiSpamManager = antiSpamManager;
    }
    public void register(@Nonnull EventRegistry eventRegistry) {
        eventRegistry.<String, PlayerChatEvent>registerAsyncGlobal(PlayerChatEvent.class, future ->
                future.thenApply(event -> {
                    // Check for spam (optimized - returns true for both rate limit and duplicates)
                    if (antiSpamManager.isSpam(event.getSender().getUuid(), event.getContent())) {
                        long cooldown = antiSpamManager.getRemainingCooldown(event.getSender().getUuid());

                        // If cooldown is very small, it's likely a duplicate message
                        if (cooldown < 100) {
                            event.getSender().sendMessage(Message.raw("Please don't spam the same message!").color("#FF5555"));
                        } else {
                            double secondsRemaining = cooldown / 1000.0;
                            event.getSender().sendMessage(Message.raw(String.format("Slow down! Please wait %.1f seconds.", secondsRemaining)).color("#FF5555"));
                        }
                        event.setCancelled(true);
                        return event;
                    }

                    // Apply custom chat formatting
                    if (chatManager.isEnabled()) {
                        event.setFormatter(chatManager.createFormatter());
                    }
                    return event;
                })
        );
    }
}
