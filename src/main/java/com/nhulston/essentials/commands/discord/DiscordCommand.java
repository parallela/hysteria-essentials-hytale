package com.nhulston.essentials.commands.discord;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nhulston.essentials.util.ColorUtil;
import com.nhulston.essentials.util.ConfigManager;

import javax.annotation.Nonnull;

/**
 * Command to display the server's Discord invite link.
 * Usage: /discord
 */
public class DiscordCommand extends AbstractPlayerCommand {
    private final ConfigManager configManager;

    public DiscordCommand(@Nonnull ConfigManager configManager) {
        super("discord", "Get the Discord server invite link");
        this.configManager = configManager;

        requirePermission("essentials.discord");
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {

        if (!configManager.isDiscordEnabled()) {
            return;
        }

        String title = configManager.getDiscordTitle();
        String url = configManager.getDiscordUrl();
        String message = configManager.getDiscordMessage();

        // Send title if configured
        if (!title.isEmpty()) {
            playerRef.sendMessage(ColorUtil.colorize(title));
        }

        // Send message with clickable link
        if (!message.isEmpty()) {
            // Replace %url% placeholder in message
            String formattedMessage = message.replace("%url%", url);
            playerRef.sendMessage(ColorUtil.colorize(formattedMessage));
        }

        // Send clickable link
        if (!url.isEmpty()) {
            playerRef.sendMessage(Message.join(
                    Message.raw("Click here to join: ").color("#AAAAAA"),
                    Message.raw(url).color("#55FFFF").link(url)
            ));
        }
    }
}

