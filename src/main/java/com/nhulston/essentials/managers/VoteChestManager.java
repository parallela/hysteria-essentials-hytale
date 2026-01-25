package com.nhulston.essentials.managers;

import com.nhulston.essentials.models.VoteChestReward;
import com.nhulston.essentials.models.VoteChestReward.RewardTier;
import com.nhulston.essentials.util.Log;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages vote chest rewards and selection logic.
 */
public class VoteChestManager {
    private final Path voteChestPath;
    private final List<VoteChestReward> commonRewards = new ArrayList<>();
    private final List<VoteChestReward> rareRewards = new ArrayList<>();
    private final List<VoteChestReward> legendaryRewards = new ArrayList<>();

    private double legendaryChance = 1.0; // 1% chance
    private double rareChance = 10.0; // 10% chance

    // UI settings
    private boolean enableUI = true; // true = show UI, false = auto-give reward

    // Item settings
    private String voteChestItemId = "VoteRewardItem"; // Default item ID

    // Configurable messages
    private String messageCommonRoll = "&4Hysteria &7> &7You rolled a Common reward!";
    private String messageRareRoll = "&4Hysteria &7> &9You rolled a Rare reward!";
    private String messageLegendaryRoll = "&4Hysteria &7> &6You rolled a LEGENDARY reward!";
    private String messageRewardReceived = "&4Hysteria &7> &aYou received %quantity%x %item%!";
    private String messageNoRewards = "&4Hysteria &7> &cNo rewards available for this tier!";
    private String messageUIHint = "&4Hysteria &7> &7Sneak + right-click to see reward options!";
    private String messageGiveUsage = "&4Hysteria &7> &7Usage: /votechest give <player> [amount]";
    private String messageGiveSuccess = "&4Hysteria &7> &aYou received %amount% vote chest(s)!";
    private String messageGiveSuccessAdmin = "&4Hysteria &7> &aGave %amount% vote chest(s) to %player%";

    public VoteChestManager(@Nonnull Path dataFolder) {
        this.voteChestPath = dataFolder.resolve("votechest.toml");
        load();
    }

    /**
     * Loads vote chest configuration from votechest.toml
     */
    private void load() {
        if (!Files.exists(voteChestPath)) {
            createDefault();
        }

        try {
            TomlParseResult config = Toml.parse(voteChestPath);

            // Load tier chances
            legendaryChance = config.getDouble("tiers.legendary-chance", () -> 1.0);
            rareChance = config.getDouble("tiers.rare-chance", () -> 10.0);

            // Load UI settings
            enableUI = config.getBoolean("ui.enabled", () -> true);

            // Load item settings
            voteChestItemId = config.getString("item.id", () -> "VoteRewardItem");

            // Load messages
            messageCommonRoll = config.getString("messages.common-roll", () -> "&4Hysteria &7> &7You rolled a Common reward!");
            messageRareRoll = config.getString("messages.rare-roll", () -> "&4Hysteria &7> &9You rolled a Rare reward!");
            messageLegendaryRoll = config.getString("messages.legendary-roll", () -> "&4Hysteria &7> &6You rolled a LEGENDARY reward!");
            messageRewardReceived = config.getString("messages.reward-received", () -> "&4Hysteria &7> &aYou received %quantity%x %item%!");
            messageNoRewards = config.getString("messages.no-rewards", () -> "&4Hysteria &7> &cNo rewards available for this tier!");
            messageUIHint = config.getString("messages.ui-hint", () -> "&4Hysteria &7> &7Sneak + right-click to see reward options!");
            messageGiveUsage = config.getString("messages.give-usage", () -> "&4Hysteria &7> &7Usage: /votechest give <player> [amount]");
            messageGiveSuccess = config.getString("messages.give-success", () -> "&4Hysteria &7> &aYou received %amount% vote chest(s)!");
            messageGiveSuccessAdmin = config.getString("messages.give-success-admin", () -> "&4Hysteria &7> &aGave %amount% vote chest(s) to %player%");

            // Load common rewards
            commonRewards.clear();
            loadRewardsFromTable(config, "rewards.common", RewardTier.COMMON, commonRewards);

            // Load rare rewards
            rareRewards.clear();
            loadRewardsFromTable(config, "rewards.rare", RewardTier.RARE, rareRewards);

            // Load legendary rewards
            legendaryRewards.clear();
            loadRewardsFromTable(config, "rewards.legendary", RewardTier.LEGENDARY, legendaryRewards);

            Log.info("Loaded vote chest rewards: " +
                     commonRewards.size() + " common, " +
                     rareRewards.size() + " rare, " +
                     legendaryRewards.size() + " legendary");

        } catch (Exception e) {
            Log.error("Failed to load vote chest config: " + e.getMessage());
        }
    }

    /**
     * Loads rewards from a TOML table section
     */
    private void loadRewardsFromTable(@Nonnull TomlParseResult config, @Nonnull String path,
                                     @Nonnull RewardTier tier, @Nonnull List<VoteChestReward> targetList) {
        TomlTable table = config.getTable(path);
        if (table == null) {
            return;
        }

        for (String key : table.keySet()) {
            TomlTable rewardTable = table.getTable(key);
            if (rewardTable == null) {
                continue;
            }

            String itemId = rewardTable.getString("item");
            Long minQty = rewardTable.getLong("min-quantity");
            Long maxQty = rewardTable.getLong("max-quantity");
            Double percentage = rewardTable.getDouble("percentage");

            if (itemId != null && minQty != null && maxQty != null && percentage != null) {
                targetList.add(new VoteChestReward(
                    itemId,
                    minQty.intValue(),
                    maxQty.intValue(),
                    percentage,
                    tier
                ));
            }
        }
    }

    /**
     * Creates default votechest.toml configuration
     */
    private void createDefault() {
        try {
            String defaultConfig = """
                # Vote Chest Configuration
                # Configure rewards that players can receive from vote chests
                
                [tiers]
                # Chance percentages for each tier (legendary is checked first, then rare, then common)
                legendary-chance = 1.0    # 1% chance for legendary rewards
                rare-chance = 10.0         # 10% chance for rare rewards
                # Remaining chance (89%) goes to common rewards
                
                [ui]
                # UI settings for reward selection
                enabled = true  # true = show UI to select reward, false = auto-give random reward
                
                [item]
                # Vote chest item configuration
                id = "VoteRewardItem"  # Item ID that will open the vote chest UI
                # Examples: "Furniture_Goblin_Chest_Small", "Item_Paper", "Weapon_Handgun", etc.
                
                [messages]
                # Customizable messages (use & for color codes)
                common-roll = "&4Hysteria &7> &7You rolled a Common reward!"
                rare-roll = "&4Hysteria &7> &9You rolled a Rare reward!"
                legendary-roll = "&4Hysteria &7> &6You rolled a LEGENDARY reward!"
                reward-received = "&4Hysteria &7> &aYou received %quantity%x %item%!"
                no-rewards = "&4Hysteria &7> &cNo rewards available for this tier!"
                ui-hint = "&4Hysteria &7> &7Sneak + right-click to see reward options!"
                give-usage = "&4Hysteria &7> &7Usage: /votechest give <player> [amount]"
                give-success = "&4Hysteria &7> &aYou received %amount% vote chest(s)!"
                give-success-admin = "&4Hysteria &7> &aGave %amount% vote chest(s) to %player%"
                
                [rewards.common]
                # Common tier rewards - Basic ores and materials
                
                [rewards.common.iron_ore]
                item = "Ore_Iron"
                min-quantity = 3
                max-quantity = 8
                percentage = 30.0
                
                [rewards.common.gold_ore]
                item = "Ore_Gold"
                min-quantity = 2
                max-quantity = 5
                percentage = 25.0
                
                [rewards.common.cobalt_ore]
                item = "Ore_Cobalt"
                min-quantity = 2
                max-quantity = 4
                percentage = 20.0
                
                [rewards.common.charcoal]
                item = "Ingredient_Charcoal"
                min-quantity = 5
                max-quantity = 15
                percentage = 15.0
                
                [rewards.common.torch]
                item = "Furniture_Crude_Torch"
                min-quantity = 8
                max-quantity = 16
                percentage = 10.0
                
                [rewards.rare]
                # Rare tier rewards - Gems and prisma
                
                [rewards.rare.ruby]
                item = "Rock_Gem_Rudy"
                min-quantity = 1
                max-quantity = 3
                percentage = 40.0
                
                [rewards.rare.diamond]
                item = "Rock_Gem_Diamond"
                min-quantity = 1
                max-quantity = 2
                percentage = 30.0
                
                [rewards.rare.prisma]
                item = "Ore_Prisma"
                min-quantity = 2
                max-quantity = 5
                percentage = 30.0
                
                [rewards.legendary]
                # Legendary tier rewards - Weapons (very rare!)
                
                [rewards.legendary.handgun]
                item = "Weapon_Handgun"
                min-quantity = 1
                max-quantity = 1
                percentage = 40.0
                
                [rewards.legendary.katana]
                item = "Weapon_Longsword_Katana"
                min-quantity = 1
                max-quantity = 1
                percentage = 40.0
                
                [rewards.legendary.iron_pickaxe]
                item = "Tool_Pickaxe_Iron"
                min-quantity = 0
                max-quantity = 1
                percentage = 10.0
                
                [rewards.legendary.iron_shovel]
                item = "Tool_Shovel_Iron"
                min-quantity = 0
                max-quantity = 1
                percentage = 10.0
                """;

            Files.writeString(voteChestPath, defaultConfig);
            Log.info("Created default votechest.toml");

        } catch (Exception e) {
            Log.error("Failed to create default vote chest config: " + e.getMessage());
        }
    }

    /**
     * Reloads the vote chest configuration
     */
    public void reload() {
        load();
    }

    /**
     * Rolls for a reward tier based on configured chances
     */
    @Nonnull
    public RewardTier rollTier() {
        double roll = ThreadLocalRandom.current().nextDouble(100.0);

        if (roll < legendaryChance) {
            return RewardTier.LEGENDARY;
        } else if (roll < legendaryChance + rareChance) {
            return RewardTier.RARE;
        } else {
            return RewardTier.COMMON;
        }
    }

    /**
     * Gets available rewards for a specific tier
     */
    @Nonnull
    public List<VoteChestReward> getRewardsForTier(@Nonnull RewardTier tier) {
        List<VoteChestReward> result = switch (tier) {
            case COMMON -> new ArrayList<>(commonRewards);
            case RARE -> new ArrayList<>(rareRewards);
            case LEGENDARY -> new ArrayList<>(legendaryRewards);
        };


        return result;
    }

    /**
     * Selects a random reward from a tier based on percentages
     */
    @Nullable
    public VoteChestReward selectRandomReward(@Nonnull RewardTier tier) {
        List<VoteChestReward> rewards = getRewardsForTier(tier);
        if (rewards.isEmpty()) {
            return null;
        }

        // Calculate total percentage
        double totalPercentage = rewards.stream()
            .mapToDouble(VoteChestReward::getPercentage)
            .sum();

        // Roll for reward
        double roll = ThreadLocalRandom.current().nextDouble(totalPercentage);
        double cumulative = 0.0;

        for (VoteChestReward reward : rewards) {
            cumulative += reward.getPercentage();
            if (roll < cumulative) {
                return reward;
            }
        }

        // Fallback to first reward
        return rewards.get(0);
    }

    /**
     * Gets a random quantity between min and max for a reward
     */
    public int rollQuantity(@Nonnull VoteChestReward reward) {
        if (reward.getMinQuantity() == reward.getMaxQuantity()) {
            return reward.getMinQuantity();
        }
        return ThreadLocalRandom.current().nextInt(
            reward.getMinQuantity(),
            reward.getMaxQuantity() + 1
        );
    }

    public double getLegendaryChance() {
        return legendaryChance;
    }

    public double getRareChance() {
        return rareChance;
    }

    public boolean isUIEnabled() {
        return enableUI;
    }

    @Nonnull
    public String getVoteChestItemId() {
        return voteChestItemId;
    }

    @Nonnull
    public String getMessageForTier(@Nonnull RewardTier tier) {
        return switch (tier) {
            case COMMON -> messageCommonRoll;
            case RARE -> messageRareRoll;
            case LEGENDARY -> messageLegendaryRoll;
        };
    }

    @Nonnull
    public String getMessageRewardReceived() {
        return messageRewardReceived;
    }

    @Nonnull
    public String getMessageNoRewards() {
        return messageNoRewards;
    }

    @Nonnull
    public String getMessageUIHint() {
        return messageUIHint;
    }

    @Nonnull
    public String getMessageGiveUsage() {
        return messageGiveUsage;
    }

    @Nonnull
    public String getMessageGiveSuccess() {
        return messageGiveSuccess;
    }

    @Nonnull
    public String getMessageGiveSuccessAdmin() {
        return messageGiveSuccessAdmin;
    }
}

