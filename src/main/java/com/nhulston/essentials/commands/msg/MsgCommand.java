package com.nhulston.essentials.commands.msg;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nhulston.essentials.util.Msg;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
public class MsgCommand extends AbstractPlayerCommand {
    private static final Map<UUID, UUID> lastMessagePartner = new ConcurrentHashMap<>();
    public MsgCommand() {
        super("msg", "Send a private message to a player");
        setAllowsExtraArguments(true);
        addAliases("m", "message", "whisper", "pm");
        requirePermission("essentials.msg");
    }
    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        String rawInput = context.getInputString();
        String[] parts = rawInput.split("\\s+", 3);  
        if (parts.length < 3) {
            Msg.fail(context, "Usage: /msg <player> <message>");
            return;
        }
        String targetName = parts[1];
        String message = parts[2];
        PlayerRef target = findPlayer(targetName);
        if (target == null) {
            Msg.fail(context, "Player '" + targetName + "' not found.");
            return;
        }
        if (target.getUuid().equals(playerRef.getUuid())) {
            Msg.fail(context, "You cannot message yourself.");
            return;
        }
        sendMessage(playerRef, target, message, context);
    }
    public static void sendMessage(@Nonnull PlayerRef sender, @Nonnull PlayerRef target, 
                                   @Nonnull String message, @Nullable CommandContext context) {
        Msg.info(target, "[From " + sender.getUsername() + "] " + message);
        if (context != null) {
            Msg.info(context, "[To " + target.getUsername() + "] " + message);
        } else {
            Msg.info(sender, "[To " + target.getUsername() + "] " + message);
        }
        lastMessagePartner.put(sender.getUuid(), target.getUuid());
        lastMessagePartner.put(target.getUuid(), sender.getUuid());
    }
    @Nullable
    public static UUID getLastMessagePartner(@Nonnull UUID playerUuid) {
        return lastMessagePartner.get(playerUuid);
    }
    @Nullable
    public static PlayerRef findPlayer(String name) {
        List<PlayerRef> players = Universe.get().getPlayers();
        for (PlayerRef player : players) {
            if (player.getUsername().equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null;
    }
    public static void onPlayerQuit(UUID uuid) {
        lastMessagePartner.remove(uuid);
    }
}
