package com.nhulston.essentials.managers;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nhulston.essentials.util.ConfigManager;
import com.nhulston.essentials.util.Log;
import com.nhulston.essentials.util.Msg;
import com.nhulston.essentials.util.TeleportUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
public class TeleportManager {
    private static final String BYPASS_PERMISSION = "essentials.teleport.bypass";
    private static final double CANCEL_DISTANCE = 2.0;
    private final ConfigManager configManager;
    private final ConcurrentHashMap<UUID, PendingTeleport> pendingTeleports = new ConcurrentHashMap<>();
    public TeleportManager(@Nonnull ConfigManager configManager) {
        this.configManager = configManager;
    }
    public void queueTeleport(@Nonnull PlayerRef playerRef, @Nonnull Ref<EntityStore> entityRef,
                              @Nonnull Store<EntityStore> store, @Nonnull Vector3d startPosition,
                              @Nonnull String worldName, double x, double y, double z,
                              float yaw, float pitch, @Nullable String successMessage) {
        queueTeleport(playerRef, entityRef, store, startPosition, worldName, x, y, z, yaw, pitch, successMessage, null);
    }
    public void queueTeleport(@Nonnull PlayerRef playerRef, @Nonnull Ref<EntityStore> entityRef,
                              @Nonnull Store<EntityStore> store, @Nonnull Vector3d startPosition,
                              @Nonnull String worldName, double x, double y, double z,
                              float yaw, float pitch, @Nullable String successMessage,
                              @Nullable Runnable onSuccess) {
        UUID playerUuid = playerRef.getUuid();
        int delay = configManager.getTeleportDelay();
        if (delay <= 0 || PermissionsModule.get().hasPermission(playerUuid, BYPASS_PERMISSION)) {
            String error = TeleportUtil.teleportSafe(store, entityRef, worldName, x, y, z, yaw, pitch);
            if (error != null) {
                Msg.fail(playerRef, error);
            } else {
                if (successMessage != null) {
                    Msg.success(playerRef, successMessage);
                }
                if (onSuccess != null) {
                    onSuccess.run();
                }
            }
            return;
        }
        if (pendingTeleports.containsKey(playerUuid)) {
            Msg.fail(playerRef, "You already have a pending teleport. Please wait.");
            return;
        }
        TeleportDestination destination = new TeleportDestination(worldName, x, y, z, yaw, pitch);
        PendingTeleport pending = new PendingTeleport(playerRef, startPosition, destination, successMessage, delay, onSuccess);
        pendingTeleports.put(playerUuid, pending);
        Msg.info(playerRef, "Teleporting in " + delay + " seconds. Don't move!");
    }
    public void queueTeleportToPlayer(@Nonnull PlayerRef playerRef, @Nonnull Ref<EntityStore> entityRef,
                                      @Nonnull Store<EntityStore> store, @Nonnull Vector3d startPosition,
                                      @Nonnull PlayerRef targetPlayer, @Nullable String successMessage) {
        UUID playerUuid = playerRef.getUuid();
        int delay = configManager.getTeleportDelay();
        if (delay <= 0 || PermissionsModule.get().hasPermission(playerUuid, BYPASS_PERMISSION)) {
            TeleportUtil.teleportToPlayer(playerRef, targetPlayer);
            if (successMessage != null) {
                Msg.success(playerRef, successMessage);
            }
            return;
        }
        if (pendingTeleports.containsKey(playerUuid)) {
            Msg.fail(playerRef, "You already have a pending teleport. Please wait.");
            return;
        }
        PendingTeleport pending = new PendingTeleport(playerRef, startPosition, targetPlayer.getUuid(), 
                                                       targetPlayer.getUsername(), successMessage, delay);
        pendingTeleports.put(playerUuid, pending);
        Msg.info(playerRef, "Teleporting in " + delay + " seconds. Don't move!");
    }
    public void tick(@Nonnull UUID playerUuid, @Nonnull Ref<EntityStore> currentRef,
                     @Nonnull Vector3d currentPosition, float deltaTime,
                     @Nonnull CommandBuffer<EntityStore> buffer) {
        PendingTeleport pending = pendingTeleports.get(playerUuid);
        if (pending == null) {
            return;
        }
        double distanceSquared = pending.getStartPosition().distanceSquaredTo(currentPosition);
        double maxDistanceSquared = CANCEL_DISTANCE * CANCEL_DISTANCE;
        if (distanceSquared > maxDistanceSquared) {
            cancelTeleport(playerUuid, "Teleport canceled because you moved.");
            return;
        }
        pending.addElapsedTime(deltaTime);
        if (pending.isReady()) {
            executeTeleport(playerUuid, currentRef, buffer);
        }
    }
    private void executeTeleport(@Nonnull UUID playerUuid, @Nonnull Ref<EntityStore> currentRef,
                                 @Nonnull CommandBuffer<EntityStore> buffer) {
        PendingTeleport pending = pendingTeleports.remove(playerUuid);
        if (pending == null) {
            return;
        }
        buffer.run(store -> {
            try {
                if (!currentRef.isValid()) {
                    Msg.fail(pending.getPlayerRef(), "Teleport failed - player reference invalid.");
                    return;
                }
                String error = null;
                if (pending.isPlayerTeleport()) {
                    error = TeleportUtil.teleportToPlayerByUuid(store, currentRef, pending.getTargetPlayerUuid());
                } else {
                    TeleportDestination dest = pending.getDestination();
                    error = TeleportUtil.teleportSafe(store, currentRef, dest.worldName,
                            dest.x, dest.y, dest.z, dest.yaw, dest.pitch);
                }
                if (error != null) {
                    Msg.fail(pending.getPlayerRef(), error);
                } else {
                    if (pending.getSuccessMessage() != null) {
                        Msg.success(pending.getPlayerRef(), pending.getSuccessMessage());
                    }
                    if (pending.getOnSuccess() != null) {
                        pending.getOnSuccess().run();
                    }
                }
            } catch (Exception e) {
                Log.error("Failed to execute teleport for " + playerUuid + ": " + e.getMessage());
                Msg.fail(pending.getPlayerRef(), "Teleport failed.");
            }
        });
    }
    public void cancelTeleport(@Nonnull UUID playerUuid, @Nullable String reason) {
        PendingTeleport pending = pendingTeleports.remove(playerUuid);
        if (pending != null && reason != null) {
            Msg.fail(pending.getPlayerRef(), reason);
        }
    }
    public boolean hasPendingTeleport(@Nonnull UUID playerUuid) {
        return pendingTeleports.containsKey(playerUuid);
    }
    public void onPlayerQuit(@Nonnull UUID playerUuid) {
        pendingTeleports.remove(playerUuid);
    }
    public void shutdown() {
        pendingTeleports.clear();
    }
    private static class TeleportDestination {
        final String worldName;
        final double x, y, z;
        final float yaw, pitch;
        TeleportDestination(String worldName, double x, double y, double z, float yaw, float pitch) {
            this.worldName = worldName;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }
    private static class PendingTeleport {
        private final PlayerRef playerRef;
        private final Vector3d startPosition;
        private final TeleportDestination destination;  
        private final UUID targetPlayerUuid;            
        private final String targetPlayerName;          
        private final String successMessage;
        private final Runnable onSuccess;
        private final float delaySeconds;
        private float elapsedTime;
        PendingTeleport(@Nonnull PlayerRef playerRef, @Nonnull Vector3d startPosition,
                        @Nonnull TeleportDestination destination, @Nullable String successMessage, 
                        int delaySeconds, @Nullable Runnable onSuccess) {
            this.playerRef = playerRef;
            this.startPosition = startPosition.clone();  
            this.destination = destination;
            this.targetPlayerUuid = null;
            this.targetPlayerName = null;
            this.successMessage = successMessage;
            this.onSuccess = onSuccess;
            this.delaySeconds = delaySeconds;
            this.elapsedTime = 0f;
        }
        PendingTeleport(@Nonnull PlayerRef playerRef, @Nonnull Vector3d startPosition,
                        @Nonnull UUID targetPlayerUuid, @Nonnull String targetPlayerName,
                        @Nullable String successMessage, int delaySeconds) {
            this.playerRef = playerRef;
            this.startPosition = startPosition.clone();  
            this.destination = null;
            this.targetPlayerUuid = targetPlayerUuid;
            this.targetPlayerName = targetPlayerName;
            this.successMessage = successMessage;
            this.onSuccess = null;
            this.delaySeconds = delaySeconds;
            this.elapsedTime = 0f;
        }
        PlayerRef getPlayerRef() {
            return playerRef;
        }
        Vector3d getStartPosition() {
            return startPosition;
        }
        TeleportDestination getDestination() {
            return destination;
        }
        UUID getTargetPlayerUuid() {
            return targetPlayerUuid;
        }
        boolean isPlayerTeleport() {
            return targetPlayerUuid != null;
        }
        String getSuccessMessage() {
            return successMessage;
        }
        Runnable getOnSuccess() {
            return onSuccess;
        }
        void addElapsedTime(float deltaTime) {
            this.elapsedTime += deltaTime;
        }
        boolean isReady() {
            return elapsedTime >= delaySeconds;
        }
    }
}
