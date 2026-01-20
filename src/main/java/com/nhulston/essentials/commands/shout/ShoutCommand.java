package com.nhulston.essentials.commands.shout;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nhulston.essentials.util.ColorUtil;
import com.nhulston.essentials.util.ConfigManager;
import com.nhulston.essentials.util.Msg;
import javax.annotation.Nonnull;
public class ShoutCommand extends AbstractPlayerCommand {
    private final ConfigManager configManager;
    public ShoutCommand(@Nonnull ConfigManager configManager) {
        super("shout", "Broadcast a message to all players");
        this.configManager = configManager;
        addAliases("broadcast");
        requirePermission("essentials.shout");
        setAllowsExtraArguments(true);
    }
    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        String rawInput = context.getInputString();
        String[] parts = rawInput.split("\\s+", 2);  
        if (parts.length < 2) {
            Msg.fail(context, "Usage: /shout <message>");
            return;
        }
        String message = configManager.getShoutPrefix() + parts[1];
        Universe.get().sendMessage(ColorUtil.colorize(message));
    }
}
