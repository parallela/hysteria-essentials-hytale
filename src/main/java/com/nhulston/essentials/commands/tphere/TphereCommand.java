package com.nhulston.essentials.commands.tphere;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nhulston.essentials.util.Msg;
import com.nhulston.essentials.util.TeleportUtil;

import javax.annotation.Nonnull;

/**
 * Command to teleport another player to yourself.
 * Usage: /tphere <player>
 */
public class TphereCommand extends AbstractPlayerCommand {
    private final RequiredArg<PlayerRef> targetArg;

    public TphereCommand() {
        super("tphere", "Teleport a player to you");
        this.targetArg = withRequiredArg("player", "Player to teleport", ArgTypes.PLAYER_REF);
        requirePermission("essentials.tphere");
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        PlayerRef target = context.get(targetArg);

        if (target == null) {
            Msg.fail(context, "Player not found.");
            return;
        }

        if (target.getUuid().equals(playerRef.getUuid())) {
            Msg.fail(context, "You cannot teleport yourself to yourself.");
            return;
        }

        // Teleport target to the command sender
        TeleportUtil.teleportToPlayer(target, playerRef);

        Msg.success(context, "Teleported " + target.getUsername() + " to you.");
        Msg.info(target, "You have been teleported to " + playerRef.getUsername() + ".");
    }
}
