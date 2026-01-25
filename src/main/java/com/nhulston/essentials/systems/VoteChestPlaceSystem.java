package com.nhulston.essentials.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nhulston.essentials.gui.VoteChestPage;
import com.nhulston.essentials.managers.VoteChestManager;
import com.nhulston.essentials.models.VoteChestReward;
import com.nhulston.essentials.models.VoteChestReward.RewardTier;
import com.nhulston.essentials.util.ColorUtil;
import com.nhulston.essentials.util.Log;
import org.bson.BsonDocument;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

/**
 * System that handles vote chest block placement events.
 * When a player tries to place the configured vote chest item with VoteChest metadata,
 * it cancels the placement and opens the reward selection UI instead.
 */
public class VoteChestPlaceSystem extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
    private final VoteChestManager voteChestManager;

    public VoteChestPlaceSystem(@Nonnull VoteChestManager voteChestManager) {
        super(PlaceBlockEvent.class);
        this.voteChestManager = voteChestManager;
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                      @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer,
                      @Nonnull PlaceBlockEvent event) {
        // Get the item being placed
        ItemStack itemStack = event.getItemInHand();
        if (itemStack == null || itemStack.isEmpty()) {
            return;
        }

        // Check if it's a vote chest item (using configurable item ID)
        String voteChestItemId = voteChestManager.getVoteChestItemId();
        if (!voteChestItemId.equals(itemStack.getItemId())) {
            return;
        }

        // Check if it has the VoteChest metadata tag
        BsonDocument metadata = itemStack.getMetadata();
        if (metadata == null || !metadata.containsKey("VoteChest")) {
            // This is a regular chest being placed, not a vote chest
            return;
        }

        // Get the player reference
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

        if (player == null || playerRef == null) {
            return;
        }

        Log.info("Vote chest detected! Player: " + playerRef.getUsername());

        // Cancel the block placement
        event.setCancelled(true);

        Inventory inventory = player.getInventory();
        if (inventory == null) {
            return;
        }

        // Find which slot has the vote chest and consume it
        boolean consumed = false;
        for (short slot = 0; slot < inventory.getHotbar().getCapacity(); slot++) {
            ItemStack slotItem = inventory.getHotbar().getItemStack(slot);
            if (slotItem != null && voteChestItemId.equals(slotItem.getItemId())) {
                // Check if this specific item has the VoteChest metadata
                BsonDocument slotMetadata = slotItem.getMetadata();
                if (slotMetadata != null && slotMetadata.containsKey("VoteChest")) {
                    int newQuantity = slotItem.getQuantity() - 1;
                    if (newQuantity <= 0) {
                        inventory.getHotbar().setItemStackForSlot(slot, ItemStack.EMPTY);
                    } else {
                        // Preserve metadata when reducing quantity
                        inventory.getHotbar().setItemStackForSlot(slot,
                            new ItemStack(voteChestItemId, newQuantity, slotMetadata));
                    }
                    consumed = true;
                    break;
                }
            }
        }

        if (!consumed) {
            return;
        }

        // Sync inventory
        player.sendInventory();

        // Roll for reward tier
        RewardTier tier = voteChestManager.rollTier();
        Log.info("Rolled tier: " + tier + " for player " + playerRef.getUsername());

        // Send tier announcement using configurable message
        String tierMessage = voteChestManager.getMessageForTier(tier);
        playerRef.sendMessage(ColorUtil.colorize(tierMessage));

        // Check if UI is enabled in config
        if (voteChestManager.isUIEnabled()) {
            // Show UI with all reward options
            Log.info("Opening UI for player: " + playerRef.getUsername() + " with tier: " + tier);
            VoteChestPage rewardPage = new VoteChestPage(playerRef, voteChestManager, tier);
            player.getPageManager().openCustomPage(ref, store, rewardPage);
        } else {
            // Auto-select and give random reward
            giveRandomReward(player, playerRef, ref, store, tier);
        }
    }

    /**
     * Auto-gives a random reward from the tier (no UI)
     */
    private void giveRandomReward(@Nonnull Player player, @Nonnull PlayerRef playerRef,
                                  @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                  @Nonnull RewardTier tier) {
        // Auto-select a random reward from the tier
        VoteChestReward reward = voteChestManager.selectRandomReward(tier);
        if (reward == null) {
            String noRewardsMsg = voteChestManager.getMessageNoRewards();
            playerRef.sendMessage(ColorUtil.colorize(noRewardsMsg));
            Log.info("No reward selected for tier: " + tier);
            return;
        }

        // Roll quantity
        int quantity = voteChestManager.rollQuantity(reward);

        // Log what we're giving for debugging
        Log.info("Giving reward: " + reward.getItemId() + " x" + quantity + " to " + playerRef.getUsername());

        // Create and give the reward item - use exact ID from config
        ItemStack rewardItem = new ItemStack(reward.getItemId(), quantity);

        Inventory rewardInventory = player.getInventory();
        if (rewardInventory != null) {
            var transaction = rewardInventory.getCombinedHotbarFirst().addItemStack(rewardItem);
            ItemStack remainder = transaction.getRemainder();

            // Drop overflow items
            if (remainder != null && !remainder.isEmpty()) {
                ItemUtils.dropItem(ref, remainder, store);
            }
        }

        // Send success message using configurable message
        String itemName = reward.getItemId()
            .replaceAll("^(Ore_|Rock_Gem_|Weapon_|Ingredient_|Furniture_)", "")
            .replace("_", " ");

        String rewardMsg = voteChestManager.getMessageRewardReceived()
            .replace("%quantity%", String.valueOf(quantity))
            .replace("%item%", itemName);

        playerRef.sendMessage(ColorUtil.colorize(rewardMsg));
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return Collections.singleton(RootDependency.first());
    }
}

