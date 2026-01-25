# Vote Chest System - Complete & Final!

## ✅ All Features Implemented

### 1. Configurable Item ID
The vote chest item can now be any Hytale item:
```toml
[item]
id = "Furniture_Goblin_Chest_Small"  # Change to any item ID!
```

### 2. Configurable Messages
All messages are customizable with placeholders:
```toml
[messages]
common-roll = "&8[&eVoteChest&8] &7You rolled a Common reward!"
rare-roll = "&8[&eVoteChest&8] &9You rolled a Rare reward!"
legendary-roll = "&8[&eVoteChest&8] &6You rolled a LEGENDARY reward!"
reward-received = "&8[&eVoteChest&8] &aYou received %quantity%x %item%!"
```

### 3. UI Toggle
Choose between UI selection or auto-reward:
```toml
[ui]
enabled = true  # true = UI, false = instant reward
```

### 4. Iron Tools Added
Ultra-rare tools with 0-1 quantity range:
```toml
[rewards.legendary.iron_pickaxe]
item = "Tool_Pickaxe_Iron"
min-quantity = 0  # Can give nothing!
max-quantity = 1
percentage = 10.0
```

### 5. All Correct Item IDs
- Ore_Iron, Ore_Gold, Ore_Cobalt, Ore_Prisma
- Rock_Gem_Rudy, Rock_Gem_Diamond
- Weapon_Handgun, Weapon_Longsword_Katana
- Tool_Pickaxe_Iron, Tool_Shovel_Iron
- Ingredient_Charcoal, Furniture_Crude_Torch

## 📋 Complete Configuration

The `votechest.toml` now includes:

### [tiers]
- legendary-chance = 1.0
- rare-chance = 10.0

### [ui]
- enabled = true

### [item]
- id = "Furniture_Goblin_Chest_Small"

### [messages]
- common-roll
- rare-roll
- legendary-roll
- reward-received
- no-rewards
- ui-hint

### [rewards]
**Common (89%):**
- Iron Ore (30%)
- Gold Ore (25%)
- Cobalt Ore (20%)
- Charcoal (15%)
- Torches (10%)

**Rare (10%):**
- Ruby (40%)
- Diamond (30%)
- Prisma (30%)

**Legendary (1%):**
- Handgun (40%)
- Katana (40%)
- Iron Pickaxe (10%) - can give 0!
- Iron Shovel (10%) - can give 0!

## 🎮 How to Use Different Items

### Example 1: Use Paper Instead
```toml
[item]
id = "Item_Paper"
```
Now players use paper to open vote chests!

### Example 2: Use Custom Item
```toml
[item]
id = "Your_Custom_Item_ID"
```

## 🎨 Customize Everything

### Change Vote Chest Item
Edit `votechest.toml`:
```toml
[item]
id = "Weapon_Handgun"  # Now handguns open vote chests!
```

### Customize Messages
```toml
[messages]
legendary-roll = "&6&l✦ JACKPOT! ✦"
reward-received = "&a✓ Got %quantity%x %item%!"
```

### Toggle UI
```toml
[ui]
enabled = false  # Quick auto-rewards
```

### Add More Rewards
```toml
[rewards.rare.emerald]
item = "Rock_Gem_Emerald"
min-quantity = 1
max-quantity = 3
percentage = 20.0
```

## 📦 What's in the JAR

The new JAR includes:
- ✅ Updated votechest.toml resource
- ✅ Configurable item ID system
- ✅ Configurable messages with placeholders
- ✅ UI toggle
- ✅ Iron tools in legendary tier
- ✅ All correct item IDs

## 🚀 Deployment Steps

### 1. Delete Old Config
```bash
rm plugins/Essentials/votechest.toml
```

### 2. Upload New JAR
```
/Users/lubomirstankov/Development/me/Essentials/build/libs/Essentials-1.5.5.jar
```

### 3. Restart/Reload
New config will be generated with all features!

### 4. Test
```
/votechest give YourName 1
```

## 🎯 Key Features Summary

| Feature | Configurable | Default Value |
|---------|--------------|---------------|
| **Item ID** | ✅ Yes | Furniture_Goblin_Chest_Small |
| **UI Mode** | ✅ Yes | Enabled (true) |
| **Messages** | ✅ Yes | All 6 messages |
| **Tier Chances** | ✅ Yes | 1% / 10% / 89% |
| **Rewards** | ✅ Yes | Ores, Gems, Weapons, Tools |
| **Tool Drop** | ✅ Yes | 0-1 quantity |

## 💡 Advanced Usage

### Scenario 1: Voting Server
```toml
[item]
id = "Item_Vote_Ticket"  # Custom voting ticket

[ui]
enabled = false  # Auto-reward, no clicking needed

[messages]
reward-received = "&6Thank you for voting! Got %quantity%x %item%!"
```

### Scenario 2: Premium Crate
```toml
[item]
id = "Furniture_Ornate_Chest"  # Fancy chest

[ui]
enabled = true  # Let them choose

[tiers]
legendary-chance = 5.0  # More generous
```

### Scenario 3: Mystery Box
```toml
[item]
id = "Item_Mystery_Box"

[messages]
common-roll = "&7Opened: Common loot"
rare-roll = "&9Opened: Rare loot!"
legendary-roll = "&6&lOPENED: LEGENDARY!"
```

## 📝 Configuration Reference

### Full votechest.toml Structure
```toml
[tiers]          # Tier roll chances
[ui]             # UI on/off
[item]           # What item opens chest
[messages]       # All text messages
[rewards.common]      # 89% rewards
[rewards.rare]        # 10% rewards
[rewards.legendary]   # 1% rewards
```

### Message Placeholders
- `%quantity%` - Number of items
- `%item%` - Item name (cleaned)

### Reward Structure
```toml
[rewards.TIER.NAME]
item = "Item_ID"
min-quantity = X
max-quantity = Y
percentage = Z
```

## 🎊 Everything is Ready!

**The vote chest system is now:**
- ✅ Fully configurable
- ✅ Uses correct item IDs
- ✅ Has UI support
- ✅ Customizable messages
- ✅ Supports any item as crate
- ✅ Includes iron tools
- ✅ Production ready

**JAR Location:**
`/Users/lubomirstankov/Development/me/Essentials/build/libs/Essentials-1.5.5.jar`

**Config will auto-generate at:**
`plugins/Essentials/votechest.toml`

**Ready to deploy!** 🎉

---

**Last Updated:** January 25, 2026
**Status:** ✅ COMPLETE - All features implemented
**Version:** Essentials 1.5.5

