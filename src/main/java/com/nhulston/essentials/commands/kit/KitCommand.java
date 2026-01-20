package com.nhulston.essentials.commands.kit;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nhulston.essentials.gui.KitPage;
import com.nhulston.essentials.managers.KitManager;
import com.nhulston.essentials.util.Msg;
import javax.annotation.Nonnull;
public class KitCommand extends AbstractPlayerCommand {
    private final KitManager kitManager;
    public KitCommand(@Nonnull KitManager kitManager) {
        super("kit", "Open the kit selection menu");
        this.kitManager = kitManager;
        requirePermission("essentials.kit");
        addSubCommand(new KitCreateCommand(kitManager));
        addSubCommand(new KitDeleteCommand(kitManager));
    }
    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            Msg.fail(context, "Could not get player component.");
            return;
        }
        KitPage kitPage = new KitPage(playerRef, kitManager);
        player.getPageManager().openCustomPage(ref, store, kitPage);
    }
}
