package com.nhulston.essentials.commands.essentials;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.MaybeBool;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nhulston.essentials.Essentials;
import javax.annotation.Nonnull;
public class EssentialsCommand extends AbstractPlayerCommand {
    private static final String CURSEFORGE_URL = "https://www.curseforge.com/hytale/mods/essentials-core";
    private static final String GREEN = "#55FF55";
    private static final String GRAY = "#AAAAAA";
    public EssentialsCommand() {
        super("essentials", "Show EssentialsCore version information");
        addAliases("ess");
        addSubCommand(new EssentialsReloadCommand());
    }
    @Override
    protected boolean canGeneratePermission() {
        return false;
    }
    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Message prefix = Message.raw("Running ").color(GRAY);
        Message versionText = Message.raw("EssentialsCore v" + Essentials.VERSION)
                .color(GREEN)
                .link(CURSEFORGE_URL);
        versionText.getFormattedMessage().underlined = MaybeBool.True;
        Message period = Message.raw(".").color(GRAY);
        context.sendMessage(Message.join(prefix, versionText, period));
    }
}
