# Vote Chest System - Implementation Summary

## Overview
A complete vote chest reward system has been implemented for the Essentials plugin. This system allows administrators to give players special chests that, when opened, grant random rewards from configurable tiers.

## Features

### 1. **Tiered Reward System**
- **Common Tier** (89% chance by default): Basic ore rewards
- **Rare Tier** (10% chance by default): Ingot rewards  
- **Legendary Tier** (1% chance by default): Weapon rewards (Weapon_Handgun)

### 2. **Configurable Rewards**
All rewards are configurable via `votechest.toml`:
- Item IDs
- Quantity ranges (min/max)
- Drop percentages within each tier
- Tier roll chances

### 3. **Interactive UI Selection**
- Players see a GUI when opening the chest
- Shows available rewards based on rolled tier
- Displays quantity ranges and drop chances
- Click to select preferred reward
- Auto-claim for legendary tier with single option

### 4. **Console & Player Commands**
- `/votechest give <player> [amount]` - Give vote chests (console compatible)
- Permission: `essentials.votechest.give`

## Files Created

### Core Classes
1. **VoteChestReward.java** - Model for reward data
2. **VoteChestManager.java** - Handles reward loading, tier rolling, and selection
3. **VoteChestPage.java** - UI page for reward selection
4. **VoteChestCommand.java** - Command for giving vote chests
5. **VoteChestEvent.java** - Handles chest opening interaction

### UI Files
1. **Essentials_VoteChestPage.ui** - Main reward selection UI
2. **Essentials_VoteChestRewardEntry.ui** - Individual reward card UI

### Configuration
1. **votechest.toml** - Auto-generated config with default rewards

## Configuration Example

```toml
[tiers]
legendary-chance = 1.0    # 1% chance
rare-chance = 10.0        # 10% chance
# Remaining 89% = common

[rewards.common]
[rewards.common.copper]
item = "Item_Material_Ore_Copper"
min-quantity = 5
max-quantity = 15
percentage = 25.0

[rewards.rare]
[rewards.rare.steel]
item = "Item_Material_Ingot_Steel"
min-quantity = 3
max-quantity = 8
percentage = 30.0

[rewards.legendary]
[rewards.legendary.handgun]
item = "Weapon_Handgun"
min-quantity = 1
max-quantity = 1
percentage = 100.0
```

## Usage

### For Administrators
1. Give vote chests to players:
   ```
   /votechest give PlayerName 5
   ```

2. Configure rewards by editing `plugins/Essentials/votechest.toml`

3. Reload configuration:
   ```
   /essentials reload
   ```

### For Players
1. Receive a vote chest (Furniture_Goblin_Chest with small size)
2. Right-click the chest in hand
3. See which tier was rolled
4. Select preferred reward from the UI
5. Receive items directly to inventory (overflow drops on ground)

## Technical Details

### Reward Selection Algorithm
1. Roll for tier based on configured percentages (legendary → rare → common)
2. Within the tier, roll for specific reward based on item percentages
3. Roll quantity between min-max range
4. Present options to player via UI
5. Award selected reward

### Thread Safety
- Uses registerGlobal for deprecated PlayerInteractEvent
- Inventory operations performed on player's world thread
- Reward rolls use ThreadLocalRandom for thread safety

### Item Detection
- Detects `Furniture_Goblin_Chest` in player's hand
- Consumes one chest per use
- Syncs inventory after consumption
- Cancels normal chest placement

## Permissions
- `essentials.votechest` - Access to votechest command
- `essentials.votechest.give` - Give vote chests to players

## Integration
- Fully integrated with Essentials plugin
- Uses existing managers and utilities
- Follows plugin's coding patterns
- Supports hot-reload via `/essentials reload`

## Future Enhancements (Optional)
- Particle effects when opening chests
- Sound effects for tier rolls
- Stat tracking (how many chests opened, rewards received)
- Broadcast legendary rewards to server
- Custom chest models/names via NBT data
- Multiple chest types with different reward pools

