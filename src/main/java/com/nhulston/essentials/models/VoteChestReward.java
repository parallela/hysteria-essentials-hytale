package com.nhulston.essentials.models;

import javax.annotation.Nonnull;

/**
 * Represents a possible reward from a vote chest.
 */
public class VoteChestReward {
    private final String itemId;
    private final int minQuantity;
    private final int maxQuantity;
    private final double percentage;
    private final RewardTier tier;

    public enum RewardTier {
        COMMON,
        RARE,
        LEGENDARY
    }

    public VoteChestReward(@Nonnull String itemId, int minQuantity, int maxQuantity,
                          double percentage, @Nonnull RewardTier tier) {
        this.itemId = itemId;
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
        this.percentage = percentage;
        this.tier = tier;
    }

    @Nonnull
    public String getItemId() {
        return itemId;
    }

    public int getMinQuantity() {
        return minQuantity;
    }

    public int getMaxQuantity() {
        return maxQuantity;
    }

    public double getPercentage() {
        return percentage;
    }

    @Nonnull
    public RewardTier getTier() {
        return tier;
    }
}

