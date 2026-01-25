package com.nhulston.essentials.commands.votechest;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nhulston.essentials.managers.VoteChestManager;
import com.nhulston.essentials.util.ColorUtil;
import com.nhulston.essentials.util.Msg;
import org.bson.BsonDocument;
import org.bson.BsonString;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * Command to give vote chests to players.
 * Usage: /votechest give <player> [amount]
 */
public class VoteChestCommand extends AbstractPlayerCommand {
    private static final String GIVE_PERMISSION = "essentials.votechest.give";

    private final VoteChestManager voteChestManager;

    public VoteChestCommand(@Nonnull VoteChestManager voteChestManager) {
        super("votechest", "Vote chest commands");
        this.voteChestManager = voteChestManager;

        addSubCommand(new GiveCommand(voteChestManager));
        requirePermission("essentials.votechest");
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        playerRef.sendMessage(ColorUtil.colorize(voteChestManager.getMessageGiveUsage()));
    }

    /**
     * Subcommand: /votechest give <player> [amount]
     * Can be used from console
     */
    private static class GiveCommand extends AbstractCommand {
        private final VoteChestManager voteChestManager;
        private final RequiredArg<String> playerArg;
        private final OptionalArg<Integer> amountArg;

        GiveCommand(@Nonnull VoteChestManager voteChestManager) {
            super("give", "Give vote chests to a player");
            this.voteChestManager = voteChestManager;
            this.playerArg = withRequiredArg("player", "Player name", ArgTypes.STRING);
            this.amountArg = withOptionalArg("amount", "Amount to give (default: 1)", ArgTypes.INTEGER);

            requirePermission(GIVE_PERMISSION);
        }

        @Override
        protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
            String targetPlayerName = context.get(playerArg);
            Integer amount = context.get(amountArg);

            if (amount == null) {
                amount = 1;
            }

            if (amount <= 0 || amount > 64) {
                Msg.fail(context, "Amount must be between 1 and 64.");
                return CompletableFuture.completedFuture(null);
            }

            // Find target player
            PlayerRef targetPlayer = findPlayer(targetPlayerName);
            if (targetPlayer == null) {
                Msg.fail(context, "Player '" + targetPlayerName + "' not found or not online.");
                return CompletableFuture.completedFuture(null);
            }

            Ref<EntityStore> targetRef = targetPlayer.getReference();
            if (targetRef == null || !targetRef.isValid()) {
                Msg.fail(context, "Player '" + targetPlayerName + "' is not available.");
                return CompletableFuture.completedFuture(null);
            }

            Store<EntityStore> targetStore = targetRef.getStore();
            EntityStore entityStore = targetStore.getExternalData();
            World targetWorld = entityStore.getWorld();

            if (targetWorld == null) {
                Msg.fail(context, "Player's world is not available.");
                return CompletableFuture.completedFuture(null);
            }

            int finalAmount = amount;

            // Execute on target player's world thread
            targetWorld.execute(() -> {
                if (!targetRef.isValid()) {
                    Msg.fail(context, "Player is no longer available.");
                    return;
                }

                Player targetPlayerEntity = targetStore.getComponent(targetRef, Player.getComponentType());
                if (targetPlayerEntity == null) {
                    Msg.fail(context, "Could not access player's inventory.");
                    return;
                }

                Inventory inventory = targetPlayerEntity.getInventory();
                if (inventory == null) {
                    Msg.fail(context, "Could not access player's inventory.");
                    return;
                }

                // Create vote chest item with custom metadata tag to mark it as special
                BsonDocument metadata = new BsonDocument();
                metadata.put("VoteChest", new BsonString("true"));
                // Note: Hytale doesn't support custom display names via metadata
                // The item will show default name, but functionality works

                String itemId = voteChestManager.getVoteChestItemId();
                ItemStack voteChest = new ItemStack(itemId, finalAmount, metadata);

                // Try to add to inventory
                var transaction = inventory.getCombinedHotbarFirst().addItemStack(voteChest);
                ItemStack remainder = transaction.getRemainder();

                // Drop overflow items
                if (remainder != null && !remainder.isEmpty()) {
                    ItemUtils.dropItem(targetRef, remainder, targetStore);
                }

                // Sync inventory
                targetPlayerEntity.sendInventory();

                // Send messages
                String successMsg = voteChestManager.getMessageGiveSuccess()
                    .replace("%amount%", String.valueOf(finalAmount));
                targetPlayer.sendMessage(ColorUtil.colorize(successMsg));

                String adminMsg = voteChestManager.getMessageGiveSuccessAdmin()
                    .replace("%amount%", String.valueOf(finalAmount))
                    .replace("%player%", targetPlayerName);
                context.sendMessage(ColorUtil.colorize(adminMsg));
            });

            return CompletableFuture.completedFuture(null);
        }

        /**
         * Finds a player by name in the universe
         */
        @javax.annotation.Nullable
        private PlayerRef findPlayer(@Nonnull String playerName) {
            Universe universe = Universe.get();
            if (universe == null) {
                return null;
            }

            for (PlayerRef player : universe.getPlayers()) {
                if (player.getUsername().equalsIgnoreCase(playerName)) {
                    return player;
                }
            }
            return null;
        }
    }
}

