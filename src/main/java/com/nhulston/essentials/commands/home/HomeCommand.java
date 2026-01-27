package com.nhulston.essentials.commands.home;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nhulston.essentials.Essentials;
import com.nhulston.essentials.managers.BackManager;
import com.nhulston.essentials.managers.HomeManager;
import com.nhulston.essentials.managers.TeleportManager;
import com.nhulston.essentials.models.Home;
import com.nhulston.essentials.util.Msg;
import com.nhulston.essentials.util.StorageManager;
import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;

public class HomeCommand extends AbstractPlayerCommand {
    private final HomeManager homeManager;
    private final TeleportManager teleportManager;
    private final BackManager backManager;

    public HomeCommand(@Nonnull HomeManager homeManager, @Nonnull TeleportManager teleportManager,
                       @Nonnull BackManager backManager) {
        super("home", "Teleport to your home");
        this.homeManager = homeManager;
        this.teleportManager = teleportManager;
        this.backManager = backManager;

        addAliases("homes");
        requirePermission("essentials.home");
        addUsageVariant(new HomeNamedCommand(homeManager, teleportManager, backManager));
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World currentWorld) {
        UUID playerUuid = playerRef.getUuid();
        Map<String, Home> homes = homeManager.getHomes(playerUuid);

        if (homes.isEmpty()) {
            Msg.fail(context, "You don't have any homes set. Use /sethome to set one.");
            return;
        }

        if (homes.size() == 1) {
            String homeName = homes.keySet().iterator().next();
            doTeleportToHome(context, store, ref, playerRef, currentWorld, homeName, homeManager, teleportManager, backManager);
        } else {
            Msg.prefix(context, "Homes", String.join(", ", homes.keySet()));
        }
    }

    static void doTeleportToHome(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                                 @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef,
                                 @Nonnull World currentWorld, @Nonnull String homeName,
                                 @Nonnull HomeManager homeManager, @Nonnull TeleportManager teleportManager,
                                 @Nonnull BackManager backManager) {
        Home home = homeManager.getHome(playerRef.getUuid(), homeName);
        if (home == null) {
            Msg.fail(context, "Home '" + homeName + "' not found.");
            return;
        }

        // Save current location before teleporting
        Vector3d currentPos = playerRef.getTransform().getPosition();
        Vector3f currentRot = playerRef.getTransform().getRotation();
        backManager.setTeleportLocation(playerRef.getUuid(), currentWorld.getName(),
            currentPos.getX(), currentPos.getY(), currentPos.getZ(),
            currentRot.getY(), currentRot.getX());

        Vector3d startPosition = playerRef.getTransform().getPosition();

        teleportManager.queueTeleport(
            playerRef, ref, store, startPosition,
            home.getWorld(), home.getX(), home.getY(), home.getZ(), home.getYaw(), home.getPitch(),
            "Teleported to home '" + homeName + "'."
        );
    }

    /**
     * Handles /home <name> and /home <player>:<home> syntax.
     * Also handles /home <player> to list another player's homes.
     */
    private static class HomeNamedCommand extends AbstractPlayerCommand {
        private static final String OTHERS_PERMISSION = "essentials.home.others";

        private final HomeManager homeManager;
        private final TeleportManager teleportManager;
        private final BackManager backManager;
        private final RequiredArg<String> nameArg;

        HomeNamedCommand(@Nonnull HomeManager homeManager, @Nonnull TeleportManager teleportManager,
                        @Nonnull BackManager backManager) {
            super("Teleport to a specific home or view another player's homes");
            this.homeManager = homeManager;
            this.teleportManager = teleportManager;
            this.backManager = backManager;
            this.nameArg = withRequiredArg("name", "Home name or player:home", ArgTypes.STRING);
        }

        @Override
        protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                               @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
            StorageManager storageManager = Essentials.getInstance().getStorageManager();
            String arg = context.get(nameArg);

            // Check for player:home syntax
            if (arg.contains(":")) {
                handleOtherPlayerHome(context, store, ref, playerRef, world, arg, storageManager);
                return;
            }

            // Check if this could be a player name (user has permission to view others' homes)
            if (PermissionsModule.get().hasPermission(playerRef.getUuid(), OTHERS_PERMISSION)) {
                UUID targetUuid = storageManager.getUuidByUsername(arg);
                if (targetUuid != null && !targetUuid.equals(playerRef.getUuid())) {
                    // It's a valid player name, list their homes
                    listOtherPlayerHomes(context, targetUuid, arg);
                    return;
                }
            }

            // Default: treat as own home name
            doTeleportToHome(context, store, ref, playerRef, world, arg, homeManager, teleportManager, backManager);
        }

        private void handleOtherPlayerHome(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                                           @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef,
                                           @Nonnull World world, @Nonnull String arg,
                                           @Nonnull StorageManager storageManager) {
            // Check permission
            if (!PermissionsModule.get().hasPermission(playerRef.getUuid(), OTHERS_PERMISSION)) {
                Msg.fail(context, "You don't have permission to view other players' homes.");
                return;
            }

            // Split player:home
            String[] parts = arg.split(":", 2);
            String targetName = parts[0];
            String homeName = parts.length > 1 ? parts[1] : "";

            if (targetName.isEmpty()) {
                Msg.fail(context, "Player not found.");
                return;
            }

            // Look up target player UUID
            UUID targetUuid = storageManager.getUuidByUsername(targetName);
            if (targetUuid == null) {
                Msg.fail(context, "Player '" + targetName + "' not found or has never joined.");
                return;
            }

            // If no home specified (e.g., "player:"), list their homes
            if (homeName.isEmpty()) {
                listOtherPlayerHomes(context, targetUuid, targetName);
                return;
            }

            // Get the home
            Home home = homeManager.getHome(targetUuid, homeName);
            if (home == null) {
                Msg.fail(context, "Home '" + homeName + "' not found for " + targetName + ".");
                return;
            }

            // Save current location before teleporting
            Vector3d currentPos = playerRef.getTransform().getPosition();
            Vector3f currentRot = playerRef.getTransform().getRotation();
            backManager.setTeleportLocation(playerRef.getUuid(), world.getName(),
                currentPos.getX(), currentPos.getY(), currentPos.getZ(),
                currentRot.getY(), currentRot.getX());
            Vector3d startPosition = playerRef.getTransform().getPosition();

            teleportManager.queueTeleport(
                playerRef, ref, store, startPosition,
                home.getWorld(), home.getX(), home.getY(), home.getZ(), home.getYaw(), home.getPitch(),
                "Teleported to " + targetName + "'s home '" + homeName + "'."
            );
        }

        private void listOtherPlayerHomes(@Nonnull CommandContext context, @Nonnull UUID targetUuid,
                                          @Nonnull String targetName) {
            Map<String, Home> homes = homeManager.getHomes(targetUuid);

            if (homes.isEmpty()) {
                Msg.fail(context, targetName + " doesn't have any homes set.");
                return;
            }

            Msg.prefix(context, targetName + "'s homes", String.join(", ", homes.keySet()));
        }
    }
}
