package com.nhulston.essentials.commands.freecam;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.camera.SetFlyCameraMode;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nhulston.essentials.util.Msg;
import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
public class FreecamCommand extends AbstractPlayerCommand {
    private static final Set<UUID> freecamPlayers = new HashSet<>();
    public FreecamCommand() {
        super("freecam", "Toggle freecam mode");
        requirePermission("essentials.freecam");
    }
    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        UUID uuid = playerRef.getUuid();
        boolean enabling = !freecamPlayers.contains(uuid);
        SetFlyCameraMode packet = new SetFlyCameraMode(enabling);
        playerRef.getPacketHandler().write(packet);
        if (enabling) {
            freecamPlayers.add(uuid);
            Msg.success(context, "Freecam enabled. Type /freecam again to return.");
        } else {
            freecamPlayers.remove(uuid);
            Msg.success(context, "Freecam disabled.");
        }
    }
    public static void onPlayerQuit(UUID uuid) {
        freecamPlayers.remove(uuid);
    }
}
