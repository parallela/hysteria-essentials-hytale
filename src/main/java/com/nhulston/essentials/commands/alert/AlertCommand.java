package com.nhulston.essentials.commands.alert;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.nhulston.essentials.util.ColorUtil;
import com.nhulston.essentials.util.Msg;
import javax.annotation.Nonnull;
import java.util.List;
public class AlertCommand extends AbstractPlayerCommand {
    public AlertCommand() {
        super("alert");
        setAllowsExtraArguments(true);
        requirePermission("essentials.alert");
    }
    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        String rawInput = context.getInputString();
        String[] parts = rawInput.split("\\s+", 2);  
        if (parts.length < 2) {
            Msg.fail(context, "Usage: /alert <title> | <subtitle>");
            Msg.info(context, "Example: /alert &cWarning | &eServer restarting soon");
            Msg.info(context, "Use | to separate title and subtitle");
            return;
        }
        String message = parts[1];
        String[] titleParts = message.split("\\|", 2);
        String title = titleParts[0].trim();
        String subtitle = titleParts.length > 1 ? titleParts[1].trim() : "";
        List<PlayerRef> players = Universe.get().getPlayers();
        for (PlayerRef player : players) {
            EventTitleUtil.hideEventTitleFromPlayer(player, 0);
            EventTitleUtil.showEventTitleToPlayer(
                    player,
                    ColorUtil.colorize(title),
                    ColorUtil.colorize(subtitle),
                    false  
            );
        }
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                for (PlayerRef player : Universe.get().getPlayers()) {
                    EventTitleUtil.hideEventTitleFromPlayer(player, 0);
                }
            }
        }, 10000);  
        Msg.success(context, "Alert sent to " + players.size() + " player(s)!");
        Msg.info(context, "Title: " + title);
        if (!subtitle.isEmpty()) {
            Msg.info(context, "Subtitle: " + subtitle);
        }
    }
}
