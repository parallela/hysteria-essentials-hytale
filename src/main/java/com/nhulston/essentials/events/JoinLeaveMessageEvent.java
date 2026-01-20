package com.nhulston.essentials.events;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.nhulston.essentials.util.ColorUtil;
import com.nhulston.essentials.util.ConfigManager;
import com.nhulston.essentials.util.Log;
import javax.annotation.Nonnull;
public class JoinLeaveMessageEvent {
    private final ConfigManager configManager;
    public JoinLeaveMessageEvent(@Nonnull ConfigManager configManager) {
        this.configManager = configManager;
    }
    public void register(@Nonnull EventRegistry eventRegistry) {
        if (!configManager.isJoinLeaveMessagesEnabled()) {
            Log.info("Join/Leave messages are disabled in config.");
            return;
        }
        eventRegistry.registerGlobal(PlayerConnectEvent.class, event -> {
            PlayerRef playerRef = event.getPlayerRef();
            String message = configManager.getJoinMessage()
                    .replace("%player%", playerRef.getUsername());
            Universe.get().sendMessage(ColorUtil.colorize(message));
        });
        eventRegistry.registerGlobal(PlayerDisconnectEvent.class, event -> {
            PlayerRef playerRef = event.getPlayerRef();
            String message = configManager.getLeaveMessage()
                    .replace("%player%", playerRef.getUsername());
            Universe.get().sendMessage(ColorUtil.colorize(message));
        });
        Log.info("Custom join/leave messages enabled.");
    }
}
