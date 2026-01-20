package com.nhulston.essentials.commands.antispam;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nhulston.essentials.util.ConfigManager;
import com.nhulston.essentials.util.Msg;

import javax.annotation.Nonnull;

/**
 * Command to toggle anti-spam protection on/off.
 * Usage: /antispam [on|off]
 * Permission: essentials.antispam
 */
public class AntiSpamCommand extends AbstractPlayerCommand {
    private final ConfigManager configManager;

    public AntiSpamCommand(@Nonnull ConfigManager configManager) {
        super("antispam", "Toggle anti-spam protection");
        this.configManager = configManager;

        // Allow extra arguments
        setAllowsExtraArguments(true);

        // Require permission
        requirePermission("essentials.antispam");
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        String[] args = context.getInputString().trim().split("\\s+");

        // If no arguments, show current status
        if (args.length == 1) {
            boolean enabled = configManager.isAntiSpamEnabled();
            String status = enabled ? "&aenabled" : "&cdisabled";
            Msg.info(context, "Anti-spam is currently " + status);
            Msg.info(context, "Usage: /antispam <on|off>");
            return;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "on":
            case "enable":
            case "true":
                if (configManager.isAntiSpamEnabled()) {
                    Msg.info(context, "Anti-spam is already enabled!");
                } else {
                    configManager.setAntiSpamEnabled(true);
                    Msg.success(context, "Anti-spam enabled!");
                    Msg.info(context, "Players must wait " + (configManager.getAntiSpamDelay() / 1000.0) + " seconds between messages.");
                }
                break;

            case "off":
            case "disable":
            case "false":
                if (!configManager.isAntiSpamEnabled()) {
                    Msg.info(context, "Anti-spam is already disabled!");
                } else {
                    configManager.setAntiSpamEnabled(false);
                    Msg.success(context, "Anti-spam disabled!");
                    Msg.info(context, "Players can now send messages without delay.");
                }
                break;

            default:
                Msg.fail(context, "Invalid argument: " + args[1]);
                Msg.info(context, "Usage: /antispam <on|off>");
                break;
        }
    }
}

