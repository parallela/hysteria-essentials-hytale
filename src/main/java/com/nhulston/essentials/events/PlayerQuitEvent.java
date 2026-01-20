package com.nhulston.essentials.events;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.nhulston.essentials.commands.freecam.FreecamCommand;
import com.nhulston.essentials.commands.msg.MsgCommand;
import com.nhulston.essentials.managers.AntiSpamManager;
import com.nhulston.essentials.managers.BackManager;
import com.nhulston.essentials.managers.TeleportManager;
import com.nhulston.essentials.managers.TpaManager;
import com.nhulston.essentials.util.Log;
import com.nhulston.essentials.util.StorageManager;
import javax.annotation.Nonnull;
import java.util.UUID;
public class PlayerQuitEvent {
    private final StorageManager storageManager;
    private final TpaManager tpaManager;
    private final TeleportManager teleportManager;
    private final BackManager backManager;
    private final AntiSpamManager antiSpamManager;
    public PlayerQuitEvent(@Nonnull StorageManager storageManager,
                           @Nonnull TpaManager tpaManager,
                           @Nonnull TeleportManager teleportManager,
                           @Nonnull BackManager backManager,
                           @Nonnull AntiSpamManager antiSpamManager) {
        this.storageManager = storageManager;
        this.tpaManager = tpaManager;
        this.teleportManager = teleportManager;
        this.backManager = backManager;
        this.antiSpamManager = antiSpamManager;
    }
    public void register(@Nonnull EventRegistry eventRegistry) {
        eventRegistry.registerGlobal(PlayerDisconnectEvent.class, event -> {
            UUID playerUuid = event.getPlayerRef().getUuid();
            storageManager.savePlayerData(playerUuid);
            storageManager.unloadPlayer(playerUuid);
            tpaManager.onPlayerQuit(playerUuid);
            teleportManager.onPlayerQuit(playerUuid);
            backManager.onPlayerQuit(playerUuid);
            antiSpamManager.clearPlayerData(playerUuid);
            MsgCommand.onPlayerQuit(playerUuid);
            FreecamCommand.onPlayerQuit(playerUuid);
            SpawnRegionTitleEvent.onPlayerQuit(playerUuid);
        });
        Log.info("Player disconnect cleanup registered.");
    }
}
