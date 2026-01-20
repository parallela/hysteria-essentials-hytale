package com.nhulston.essentials.commands.essentials;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nhulston.essentials.Essentials;
import com.nhulston.essentials.util.Msg;
import javax.annotation.Nonnull;
public class EssentialsReloadCommand extends AbstractPlayerCommand {
    public EssentialsReloadCommand() {
        super("reload", "Reload EssentialsCore configuration");
        requirePermission("essentials.reload");
    }
    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Essentials.getInstance().reloadConfigs();
        Msg.success(context, "Configuration reloaded.");
    }
}
