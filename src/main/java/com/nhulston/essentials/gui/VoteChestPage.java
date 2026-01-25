package com.nhulston.essentials.gui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nhulston.essentials.managers.VoteChestManager;
import com.nhulston.essentials.models.VoteChestReward;
import com.nhulston.essentials.models.VoteChestReward.RewardTier;
import com.nhulston.essentials.util.ColorUtil;
import com.nhulston.essentials.util.Msg;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * GUI page for selecting a vote chest reward.
 */
public class VoteChestPage extends InteractiveCustomUIPage<VoteChestPage.VoteChestPageData> {
    private final VoteChestManager voteChestManager;
    private final RewardTier tier;

    public VoteChestPage(@Nonnull PlayerRef playerRef, @Nonnull VoteChestManager voteChestManager,
                        @Nonnull RewardTier tier) {
        super(playerRef, CustomPageLifetime.CanDismiss, VoteChestPageData.CODEC);
        this.voteChestManager = voteChestManager;
        this.tier = tier;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder,
                      @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {
        commandBuilder.append("Pages/Essentials_VoteChestPage.ui");

        com.nhulston.essentials.util.Log.info("VoteChestPage.build() called with tier: " + tier);

        // Note: Title is static in UI - can't set dynamically with template structure
        // Get rewards for the rolled tier
        List<VoteChestReward> rewards = voteChestManager.getRewardsForTier(tier);

        com.nhulston.essentials.util.Log.info("Got " + rewards.size() + " rewards for tier " + tier);
        if (!rewards.isEmpty()) {
            com.nhulston.essentials.util.Log.info("First reward: " + rewards.get(0).getItemId() + " (tier: " + rewards.get(0).getTier() + ")");
        }

        if (rewards.isEmpty()) {
            // No rewards available - shouldn't happen but handle gracefully
            return;
        }


        // Add reward cards
        for (int i = 0; i < rewards.size(); i++) {
            VoteChestReward reward = rewards.get(i);
            String selector = "#RewardCards[" + i + "]";

            commandBuilder.append("#RewardCards", "Pages/Essentials_VoteChestRewardEntry.ui");

            String displayName = getRewardDisplayName(reward.getItemId());
            commandBuilder.set(selector + " #RewardName.Text", displayName);

            String quantityText = reward.getMinQuantity() == reward.getMaxQuantity()
                ? String.valueOf(reward.getMinQuantity())
                : reward.getMinQuantity() + "-" + reward.getMaxQuantity();

            commandBuilder.set(selector + " #Quantity.Text", "Amount: " + quantityText);
            commandBuilder.set(selector + " #Chance.Text",
                String.format("%.1f%% chance", reward.getPercentage()));

            // Bind click event
            eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                selector,
                EventData.of("RewardIndex", String.valueOf(i))
            );
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                @Nonnull VoteChestPageData data) {
        String rewardIndexStr = data.getRewardIndex();
        int rewardIndex;

        try {
            rewardIndex = Integer.parseInt(rewardIndexStr);
        } catch (NumberFormatException e) {
            Msg.fail(playerRef, "Invalid reward selection.");
            this.close();
            return;
        }

        List<VoteChestReward> rewards = voteChestManager.getRewardsForTier(tier);
        if (rewardIndex < 0 || rewardIndex >= rewards.size()) {
            Msg.fail(playerRef, "Invalid reward selection.");
            this.close();
            return;
        }

        VoteChestReward selectedReward = rewards.get(rewardIndex);

        // Get player inventory
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            Msg.fail(playerRef, "Could not access your inventory.");
            this.close();
            return;
        }

        Inventory inventory = player.getInventory();
        if (inventory == null) {
            Msg.fail(playerRef, "Could not access your inventory.");
            this.close();
            return;
        }

        // Roll quantity and give reward
        int quantity = voteChestManager.rollQuantity(selectedReward);
        ItemStack rewardItem = new ItemStack(selectedReward.getItemId(), quantity);

        // Try to add to inventory
        var transaction = inventory.getCombinedHotbarFirst().addItemStack(rewardItem);
        ItemStack remainder = transaction.getRemainder();

        // Drop overflow items
        if (remainder != null && !remainder.isEmpty()) {
            ItemUtils.dropItem(ref, remainder, store);
        }

        // Sync inventory
        player.sendInventory();

        // Send success message using configurable message
        String itemName = getRewardDisplayName(selectedReward.getItemId());
        String message = voteChestManager.getMessageRewardReceived()
            .replace("%quantity%", String.valueOf(quantity))
            .replace("%item%", itemName);

        playerRef.sendMessage(ColorUtil.colorize(message));

        this.close();
    }

    /**
     * Gets a display-friendly name from an item ID
     */
    @Nonnull
    private String getRewardDisplayName(@Nonnull String itemId) {
        // Remove prefix (Ore_, Rock_Gem_, Weapon_, etc.) and format nicely
        String name = itemId;

        // Remove common prefixes
        if (name.startsWith("Ore_")) {
            name = name.substring(4);
        } else if (name.startsWith("Rock_Gem_")) {
            name = name.substring(9);
        } else if (name.startsWith("Weapon_")) {
            name = name.substring(7);
        } else if (name.startsWith("Ingredient_")) {
            name = name.substring(11);
        } else if (name.startsWith("Furniture_")) {
            name = name.substring(10);
        }

        // Replace underscores with spaces
        name = name.replace("_", " ");

        return name;
    }

    /**
     * Event data for reward selection
     */
    public static class VoteChestPageData {
        public static final BuilderCodec<VoteChestPageData> CODEC =
            BuilderCodec.builder(VoteChestPageData.class, VoteChestPageData::new)
                .append(new KeyedCodec<>("RewardIndex", Codec.STRING),
                    (data, s) -> data.rewardIndex = s,
                    data -> data.rewardIndex)
                .add()
                .build();

        private String rewardIndex;

        public String getRewardIndex() {
            return rewardIndex;
        }
    }
}

