package com.nhulston.essentials.commands.rtp;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nhulston.essentials.managers.BackManager;
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

/**
 * RTP Command - Randomly teleport players
 * Usage: /rtp - Teleport yourself (with cooldown)
 * Usage: /rtp <player> - Teleport another player (no cooldown, requires essentials.rtp.others)
 */
public class RtpCommand extends AbstractPlayerCommand {
    private static final int MAX_ATTEMPTS = 5;
    private static final String COOLDOWN_BYPASS_PERMISSION = "essentials.rtp.cooldown.bypass";
    private static final String RTP_OTHERS_PERMISSION = "essentials.rtp.others";

    private final ConfigManager configManager;
    private final StorageManager storageManager;
    private final TeleportManager teleportManager;
    private final BackManager backManager;

    public RtpCommand(@Nonnull ConfigManager configManager, @Nonnull StorageManager storageManager,
                      @Nonnull TeleportManager teleportManager, @Nonnull BackManager backManager) {
        super("rtp", "Randomly teleport to a location");
        this.configManager = configManager;
        this.storageManager = storageManager;
        this.teleportManager = teleportManager;
        this.backManager = backManager;

        requirePermission("essentials.rtp");

        // Add variant for teleporting others (works from console too)
        addUsageVariant(new RtpOthersCommand(configManager, storageManager, teleportManager, backManager));
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

        // Save current location before teleporting
        Vector3d currentPos = playerRef.getTransform().getPosition();
        Vector3f currentRot = playerRef.getTransform().getRotation();
        backManager.setTeleportLocation(playerUuid, currentWorldName,
            currentPos.getX(), currentPos.getY(), currentPos.getZ(),
            currentRot.getY(), currentRot.getX());

        int rtpDelay = configManager.getRtpTeleportDelay();

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
                            rtpDelay,
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
            findSafeLocationSync(rtpWorld, radius, playerRef, ref, store, rtpWorldName, data, playerUuid, rtpDelay);
        }
    }

    private void findSafeLocationSync(World rtpWorld, int radius, PlayerRef playerRef,
                                       Ref<EntityStore> ref, Store<EntityStore> store,
                                       String rtpWorldName, PlayerData data, UUID playerUuid, int rtpDelay) {
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
                    rtpDelay,
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

    /**
     * Nested command for /rtp <player> - Can be used from console
     * Teleports another player without cooldown
     */
    private static class RtpOthersCommand extends AbstractCommand {
        private final ConfigManager configManager;
        private final StorageManager storageManager;
        private final TeleportManager teleportManager;
        private final BackManager backManager;
        private final RequiredArg<String> playerArg;

        RtpOthersCommand(@Nonnull ConfigManager configManager, @Nonnull StorageManager storageManager,
                        @Nonnull TeleportManager teleportManager, @Nonnull BackManager backManager) {
            super("Teleport another player to a random location");
            this.configManager = configManager;
            this.storageManager = storageManager;
            this.teleportManager = teleportManager;
            this.backManager = backManager;
            this.playerArg = withRequiredArg("player", "Player to teleport", ArgTypes.STRING);

            requirePermission(RTP_OTHERS_PERMISSION);
        }

        @Override
        protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
            String targetPlayerName = context.get(playerArg);

            // Find the target player
            PlayerRef targetPlayer = findPlayer(targetPlayerName);
            if (targetPlayer == null) {
                Msg.fail(context, "Player '" + targetPlayerName + "' not found or not online.");
                return CompletableFuture.completedFuture(null);
            }

            Ref<EntityStore> targetRef = targetPlayer.getReference();
            if (targetRef == null || !targetRef.isValid()) {
                Msg.fail(context, "Player '" + targetPlayerName + "' is not available.");
                return CompletableFuture.completedFuture(null);
            }

            Store<EntityStore> targetStore = targetRef.getStore();
            EntityStore entityStore = targetStore.getExternalData();
            World currentWorld = entityStore.getWorld();

            if (currentWorld == null) {
                Msg.fail(context, "Player '" + targetPlayerName + "' is not in a valid world.");
                return CompletableFuture.completedFuture(null);
            }

            // Get RTP world configuration
            String currentWorldName = currentWorld.getName();
            String rtpWorldName;
            Integer radius = configManager.getRtpRadius(currentWorldName);

            if (radius != null) {
                rtpWorldName = currentWorldName;
            } else {
                rtpWorldName = configManager.getRtpDefaultWorld();
                radius = configManager.getRtpRadius(rtpWorldName);
                if (radius == null) {
                    Msg.fail(context, "RTP is not enabled in this world.");
                    return CompletableFuture.completedFuture(null);
                }
            }

            World rtpWorld = Universe.get().getWorld(rtpWorldName);
            if (rtpWorld == null) {
                Msg.fail(context, "RTP world '" + rtpWorldName + "' is not loaded.");
                return CompletableFuture.completedFuture(null);
            }

            UUID targetUuid = targetPlayer.getUuid();
            PlayerData data = storageManager.getPlayerData(targetUuid);
            boolean isCrossWorld = !rtpWorldName.equals(currentWorldName);

            // Save target's current location before teleporting
            Vector3d currentPos = targetPlayer.getTransform().getPosition();
            Vector3f currentRot = targetPlayer.getTransform().getRotation();
            backManager.setTeleportLocation(targetUuid, currentWorldName,
                currentPos.getX(), currentPos.getY(), currentPos.getZ(),
                currentRot.getY(), currentRot.getX());

            // Perform RTP (no cooldown for admin command)
            Msg.success(context, "Teleporting " + targetPlayerName + " to a random location...");

            if (isCrossWorld) {
                Vector3d startPosition = targetPlayer.getTransform().getPosition().clone();
                findSafeLocationAsync(rtpWorld, radius, 0)
                    .thenAccept(result -> {
                        if (result == null) {
                            Msg.fail(context, "Could not find a safe location for " + targetPlayerName + " after " + MAX_ATTEMPTS + " attempts.");
                            return;
                        }
                        currentWorld.execute(() -> {
                            teleportManager.queueTeleport(
                                targetPlayer, targetRef, targetStore, startPosition,
                                rtpWorldName, result.x, result.y, result.z,
                                0.0f, 0.0f,
                                "Randomly teleported by admin!",
                                () -> {
                                    data.setLastRtpTime(System.currentTimeMillis());
                                    storageManager.savePlayerData(targetUuid);
                                }
                            );
                            Msg.success(context, "Successfully teleported " + targetPlayerName + "!");
                        });
                    })
                    .exceptionally(ex -> {
                        Msg.fail(context, "RTP failed for " + targetPlayerName + ". Please try again.");
                        return null;
                    });
            } else {
                performSyncRtp(rtpWorld, radius, targetPlayer, targetRef, targetStore, rtpWorldName, data, targetUuid, context, targetPlayerName);
            }

            return CompletableFuture.completedFuture(null);
        }

        private void performSyncRtp(World rtpWorld, int radius, PlayerRef targetPlayer,
                                    Ref<EntityStore> targetRef, Store<EntityStore> targetStore,
                                    String rtpWorldName, PlayerData data, UUID targetUuid,
                                    CommandContext context, String targetPlayerName) {
            ThreadLocalRandom random = ThreadLocalRandom.current();

            for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
                double x = random.nextDouble(-radius, radius);
                double z = random.nextDouble(-radius, radius);

                Double safeY = TeleportUtil.findSafeRtpY(rtpWorld, x, z);
                if (safeY != null) {
                    Vector3d startPosition = targetPlayer.getTransform().getPosition();
                    teleportManager.queueTeleport(
                        targetPlayer, targetRef, targetStore, startPosition,
                        rtpWorldName, x, safeY, z,
                        0.0f, 0.0f,
                        "Randomly teleported by admin!",
                        () -> {
                            data.setLastRtpTime(System.currentTimeMillis());
                            storageManager.savePlayerData(targetUuid);
                        }
                    );
                    Msg.success(context, "Successfully teleported " + targetPlayerName + "!");
                    return;
                }
            }

            Msg.fail(context, "Could not find a safe location for " + targetPlayerName + " after " + MAX_ATTEMPTS + " attempts.");
        }

        private PlayerRef findPlayer(String name) {
            for (PlayerRef player : Universe.get().getPlayers()) {
                if (player.getUsername().equalsIgnoreCase(name)) {
                    return player;
                }
            }
            return null;
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
    }
}
