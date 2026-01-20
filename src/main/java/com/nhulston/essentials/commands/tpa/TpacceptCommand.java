package com.nhulston.essentials.commands.tpa;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nhulston.essentials.managers.TeleportManager;
import com.nhulston.essentials.managers.TpaManager;
import com.nhulston.essentials.util.Msg;
import javax.annotation.Nonnull;
public class TpacceptCommand extends AbstractPlayerCommand {
    private final TpaManager tpaManager;
    private final TeleportManager teleportManager;
    private final RequiredArg<String> playerArg;
    public TpacceptCommand(@Nonnull TpaManager tpaManager, @Nonnull TeleportManager teleportManager) {
        super("tpaccept", "Accept a teleport request");
        this.tpaManager = tpaManager;
        this.teleportManager = teleportManager;
        this.playerArg = withRequiredArg("player", "Player whose request to accept", ArgTypes.STRING);
        requirePermission("essentials.tpaccept");
    }
    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        String requesterName = context.get(playerArg);
        TpaManager.TpaRequest request = tpaManager.acceptRequest(playerRef, requesterName);
        if (request == null) {
            Msg.fail(context, "No pending teleport request from " + requesterName + ".");
            return;
        }
        PlayerRef requester = Universe.get().getPlayer(request.getRequesterUuid());
        if (requester == null) {
            Msg.fail(context, requesterName + " is no longer online.");
            return;
        }
        Ref<EntityStore> requesterRef = requester.getReference();
        if (requesterRef == null || !requesterRef.isValid()) {
            Msg.fail(context, requesterName + " is no longer available.");
            return;
        }
        Store<EntityStore> requesterStore = requesterRef.getStore();
        Msg.success(context, "Teleport request from " + requesterName + " accepted.");
        Vector3d startPosition = requester.getTransform().getPosition();
        teleportManager.queueTeleportToPlayer(
            requester, requesterRef, requesterStore, startPosition,
            playerRef,   
            "Teleported to " + playerRef.getUsername() + "."
        );
    }
}
