package com.nhulston.essentials.commands.rtp;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nhulston.essentials.managers.TeleportManager;
import com.nhulston.essentials.models.PlayerData;
import com.nhulston.essentials.util.ConfigManager;
import com.nhulston.essentials.util.CooldownUtil;
import com.nhulston.essentials.util.Msg;
import com.nhulston.essentials.util.StorageManager;
import com.nhulston.essentials.util.TeleportUtil;
import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
public class RtpCommand extends AbstractPlayerCommand {
    private static final int MAX_ATTEMPTS = 5;
    private static final String COOLDOWN_BYPASS_PERMISSION = "essentials.rtp.cooldown.bypass";
    private final ConfigManager configManager;
    private final StorageManager storageManager;
    private final TeleportManager teleportManager;
    public RtpCommand(@Nonnull ConfigManager configManager, @Nonnull StorageManager storageManager,
                      @Nonnull TeleportManager teleportManager) {
        super("rtp", "Randomly teleport to a location");
        this.configManager = configManager;
        this.storageManager = storageManager;
        this.teleportManager = teleportManager;
        requirePermission("essentials.rtp");
    }
    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        UUID playerUuid = playerRef.getUuid();
        PlayerData data = storageManager.getPlayerData(playerUuid);
        int cooldownSeconds = configManager.getRtpCooldown();
        boolean bypassCooldown = PermissionsModule.get().hasPermission(playerUuid, COOLDOWN_BYPASS_PERMISSION);
        if (cooldownSeconds > 0 && !bypassCooldown) {
            Long lastUse = data.getLastRtpTime();
            if (lastUse != null) {
                long elapsed = (System.currentTimeMillis() - lastUse) / 1000;
                long remaining = cooldownSeconds - elapsed;
                if (remaining > 0) {
                    Msg.fail(context, "RTP is on cooldown. " + CooldownUtil.formatCooldown(remaining) + " remaining.");
                    return;
                }
            }
        }
        String currentWorldName = world.getName();
        String rtpWorldName;
        Integer radius = configManager.getRtpRadius(currentWorldName);
        if (radius != null) {
            rtpWorldName = currentWorldName;
        } else {
            rtpWorldName = configManager.getRtpDefaultWorld();
            radius = configManager.getRtpRadius(rtpWorldName);
            if (radius == null) {
                Msg.fail(context, "RTP is not enabled in this world.");
                return;
            }
        }
        World rtpWorld = Universe.get().getWorld(rtpWorldName);
        if (rtpWorld == null) {
            Msg.fail(context, "RTP world '" + rtpWorldName + "' is not loaded.");
            return;
        }
        boolean isCrossWorld = !rtpWorldName.equals(currentWorldName);
        if (isCrossWorld) {
            Vector3d startPosition = playerRef.getTransform().getPosition().clone();
            findSafeLocationAsync(rtpWorld, radius, 0)
                .thenAccept(result -> {
                    if (result == null) {
                        Msg.fail(playerRef, "Could not find a safe location after " + MAX_ATTEMPTS + " attempts. Try again.");
                        return;
                    }
                    world.execute(() -> {
                        teleportManager.queueTeleport(
                            playerRef, ref, store, startPosition,
                                rtpWorldName, result.x, result.y, result.z,
                            0.0f, 0.0f,
                            "Randomly teleported!",
                            () -> {
                                data.setLastRtpTime(System.currentTimeMillis());
                                storageManager.savePlayerData(playerUuid);
                            }
                        );
                    });
                })
                .exceptionally(ex -> {
                    Msg.fail(playerRef, "RTP failed. Please try again.");
                    return null;
                });
        } else {
            findSafeLocationSync(rtpWorld, radius, playerRef, ref, store, rtpWorldName, data, playerUuid);
        }
    }
    private void findSafeLocationSync(World rtpWorld, int radius, PlayerRef playerRef, 
                                       Ref<EntityStore> ref, Store<EntityStore> store,
                                       String rtpWorldName, PlayerData data, UUID playerUuid) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            double x = random.nextDouble(-radius, radius);
            double z = random.nextDouble(-radius, radius);
            Double safeY = TeleportUtil.findSafeRtpY(rtpWorld, x, z);
            if (safeY != null) {
                Vector3d startPosition = playerRef.getTransform().getPosition();
                teleportManager.queueTeleport(
                    playerRef, ref, store, startPosition,
                    rtpWorldName, x, safeY, z,
                    0.0f, 0.0f,
                    "Randomly teleported!",
                    () -> {
                        data.setLastRtpTime(System.currentTimeMillis());
                        storageManager.savePlayerData(playerUuid);
                    }
                );
                return;
            }
        }
        Msg.fail(playerRef, "Could not find a safe location after " + MAX_ATTEMPTS + " attempts. Try again.");
    }
    private CompletableFuture<RtpLocation> findSafeLocationAsync(World rtpWorld, int radius, int attempt) {
        if (attempt >= MAX_ATTEMPTS) {
            return CompletableFuture.completedFuture(null);
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double x = random.nextDouble(-radius, radius);
        double z = random.nextDouble(-radius, radius);
        return TeleportUtil.findSafeRtpYAsync(rtpWorld, x, z)
            .thenCompose(safeY -> {
                if (safeY != null) {
                    return CompletableFuture.completedFuture(new RtpLocation(x, safeY, z));
                }
                return findSafeLocationAsync(rtpWorld, radius, attempt + 1);
            });
    }
    private static class RtpLocation {
        final double x, y, z;
        RtpLocation(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
