# Vote Chest System - Final Configuration

## ✅ Complete Setup with Real Items

### Updated Reward Configuration

The `votechest.toml` now includes the actual in-game items:

#### Common Tier (89% chance)
- **Iron Ore** (Ore_Iron): 3-8 pieces, 30% of common rewards
- **Gold Ore** (Ore_Gold): 2-5 pieces, 25% of common rewards
- **Cobalt Ore** (Ore_Cobalt): 2-4 pieces, 20% of common rewards
- **Charcoal** (Ingredient_Charcoal): 5-15 pieces, 15% of common rewards
- **Crude Torch** (Furniture_Crude_Torch): 8-16 pieces, 10% of common rewards

#### Rare Tier (10% chance)
- **Ruby** (Rock_Gem_Rudy): 1-3 gems, 40% of rare rewards
- **Diamond** (Rock_Gem_Diamond): 1-2 gems, 30% of rare rewards
- **Prisma Ore** (Ore_Prisma): 2-5 pieces, 30% of rare rewards

#### Legendary Tier (1% chance)
- **Handgun** (Weapon_Handgun): 1 weapon, 50% of legendary rewards
- **Katana** (Weapon_Longsword_Katana): 1 weapon, 50% of legendary rewards

## 🎮 How It Works

### 1. Give Vote Chest
```
/votechest give parallela_io 1
```

### 2. Use Vote Chest
- Player receives `Furniture_Goblin_Chest_Small` with metadata tag
- Right-click on ground to "place" it
- System intercepts placement and consumes chest

### 3. Tier Roll
- 89% chance: Common tier (basic ores)
- 10% chance: Rare tier (gems & prisma)
- 1% chance: Legendary tier (weapons!)

### 4. Reward Selection
- UI opens showing available rewards for rolled tier
- Rewards display as:
  - **Iron** (instead of Ore_Iron)
  - **Ruby** (instead of Rock_Gem_Rudy)
  - **Handgun** (instead of Weapon_Handgun)
- Click to select and claim reward

### 5. Receive Items
- Items added to inventory automatically
- Overflow items drop on ground
- Success message with tier coloring

## 📁 Files in Build

```
Essentials-1.5.5.jar contains:
├── Common/UI/Custom/Pages/
│   ├── Essentials_VoteChestPage.ui
│   └── Essentials_VoteChestRewardEntry.ui
├── votechest.toml (default config)
└── All Java classes
```

## ⚙️ Configuration File

Location after first start: `plugins/Essentials/votechest.toml`

You can edit this file to:
- Change tier percentages
- Add more rewards
- Adjust quantity ranges
- Modify drop chances

Example customization:
```toml
[rewards.common.custom_item]
item = "Your_Item_ID"
min-quantity = 1
max-quantity = 5
percentage = 15.0
```

## 🎯 Display Names

The system automatically cleans up item IDs for display:
- `Ore_Iron` → "Iron"
- `Rock_Gem_Rudy` → "Rudy"
- `Weapon_Handgun` → "Handgun"
- `Ingredient_Charcoal` → "Charcoal"

## 🐛 Known Issues & Solutions

### Issue: UI Still Crashing with "Failed to load CustomUI documents"

This could be due to UI syntax errors. The current UI structure matches KitPage exactly:

**If UI continues to fail:**
1. Check server console for specific UI errors
2. Verify UI files are in correct location in JAR
3. Try testing the `/kit` command to confirm UI system works

### Alternative: Disable UI Temporarily

If UI keeps failing, you could modify the system to:
1. Auto-claim a random reward (no UI selection)
2. Just give the rolled reward directly
3. Send chat message showing what was received

Let me know if you need this simpler version!

## 📊 Expected Behavior

### Console Output:
```
[INFO] [Essentials|P] Vote chest detected! Player: parallela_io
[INFO] [Essentials|P] Rolling tier...
[INFO] [Essentials|P] Rolled: COMMON
```

### Player Messages:
```
[VoteChest] &7You rolled a Common reward!
[VoteChest] &aYou received 5x Iron!
```

## 🚀 Testing Checklist

1. ✅ Load plugin with new JAR
2. ✅ Run `/votechest give parallela_io 1`
3. ✅ Verify chest appears in inventory
4. ✅ Right-click on ground to place
5. ✅ Chest should be consumed
6. ✅ UI should open (if working)
7. ✅ Rewards should be given

## 💡 Tips

- **Test tier rolls**: Use multiple chests to test different tiers
- **Check percentages**: Ensure reward percentages in each tier add up to 100%
- **Backup config**: Keep a copy of votechest.toml before editing
- **Reload command**: Use `/essentials reload` after config changes

## 📝 Item List Reference

For adding more items to votechest.toml:

**Ores:**
- Ore_Iron
- Ore_Gold
- Ore_Cobalt
- Ore_Prisma

**Gems:**
- Rock_Gem_Rudy
- Rock_Gem_Diamond

**Weapons:**
- Weapon_Handgun
- Weapon_Longsword_Katana

**Ingredients:**
- Ingredient_Charcoal

**Furniture:**
- Furniture_Crude_Torch

Add any other valid Hytale item IDs following the same pattern!

---

**New JAR Location:** `/Users/lubomirstankov/Development/me/Essentials/build/libs/Essentials-1.5.5.jar`

**Ready to test!** 🎉

