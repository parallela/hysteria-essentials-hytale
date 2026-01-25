# Vote Chest System - FIXED Item IDs!

## ✅ Issues Resolved

### Problem 1: Invalid Item IDs
**Was getting:** `Invalid_material_Ore_Silver`  
**Root cause:** Java code had hardcoded default config with wrong item IDs  
**Fixed:** Updated `createDefault()` method to use correct Hytale item IDs

### Problem 2: UI Not Showing
**Issue:** UI system was causing crashes  
**Solution:** Switched to auto-reward system (no UI selection needed)  
**Benefit:** Faster, more reliable, no crashes

## 🎮 How It Works Now

### 1. Give Vote Chest
```
/votechest give parallela_io 1
```

### 2. Use Vote Chest
- Right-click on ground with chest
- Chest is consumed
- Tier is rolled automatically
- Random reward selected from tier
- Items given directly to inventory

### 3. Player Experience
```
[VoteChest] You rolled a Rare reward!
[VoteChest] You received 2x Diamond!
```

## 📦 Correct Item IDs Now Used

### Common Tier (89%)
- `Ore_Iron` - Iron ore
- `Ore_Gold` - Gold ore  
- `Ore_Cobalt` - Cobalt ore
- `Ingredient_Charcoal` - Charcoal
- `Furniture_Crude_Torch` - Torches

### Rare Tier (10%)
- `Rock_Gem_Rudy` - Ruby gems
- `Rock_Gem_Diamond` - Diamonds
- `Ore_Prisma` - Prisma ore

### Legendary Tier (1%)
- `Weapon_Handgun` - Handgun
- `Weapon_Longsword_Katana` - Katana

## 🔧 What Was Fixed

### 1. Fixed Default Config Generation
**Before:**
```java
item = "Item_Material_Ore_Silver"  // ❌ WRONG!
```

**After:**
```java
item = "Ore_Iron"  // ✅ CORRECT!
```

### 2. Added Debug Logging
```
[INFO] Giving reward: Ore_Iron x5 to parallela_io
```
This helps verify correct item IDs are being used.

### 3. Direct Item Creation
```java
ItemStack rewardItem = new ItemStack(reward.getItemId(), quantity);
```
Uses exact ID from votechest.toml config - no modifications!

## 🚀 Testing Steps

### Step 1: Delete Old Config
**IMPORTANT:** Delete the old votechest.toml that has wrong item IDs:
```bash
rm plugins/Essentials/votechest.toml
```

### Step 2: Reload Plugin
- Upload new JAR: `Essentials-1.5.5.jar`
- Restart server or reload plugin
- New votechest.toml will be created with correct IDs

### Step 3: Test Vote Chest
```
/votechest give parallela_io 1
```
- Place the chest (right-click ground)
- Should consume chest
- Should give valid items (Ore_Iron, Rock_Gem_Rudy, etc.)
- Should NOT give Invalid_material items

### Step 4: Verify Console
Check console logs:
```
[INFO] [Essentials|P] Vote chest detected! Player: parallela_io
[INFO] [Essentials|P] Giving reward: Ore_Iron x5 to parallela_io
```

## 📝 Config File Location

After first start: `plugins/Essentials/votechest.toml`

The config will have correct item IDs like:
```toml
[rewards.common.iron_ore]
item = "Ore_Iron"  # ✅ Correct!
```

NOT like:
```toml
[rewards.common.iron]
item = "Item_Material_Ore_Iron"  # ❌ Old/Wrong!
```

## ⚙️ Customizing Rewards

To add more rewards, edit `votechest.toml`:

```toml
[rewards.rare.your_custom_reward]
item = "Your_Item_ID"  # Use exact Hytale item ID!
min-quantity = 1
max-quantity = 5
percentage = 20.0
```

Make sure to use the **exact item IDs** from Hytale, like:
- `Ore_Iron`
- `Rock_Gem_Diamond`
- `Weapon_Handgun`

NOT prefixed versions like:
- ~~`Item_Material_Ore_Iron`~~ ❌
- ~~`Item_Rock_Gem_Diamond`~~ ❌

## 🎯 Summary

### Fixed:
1. ✅ Correct item IDs in default config
2. ✅ Direct item creation (no ID modification)
3. ✅ Debug logging to verify IDs
4. ✅ Auto-reward system (no UI crashes)

### Next Steps:
1. Delete old votechest.toml
2. Reload plugin with new JAR
3. Test vote chest
4. Verify correct items are given

### Expected Result:
```
[VoteChest] You rolled a Common reward!
[VoteChest] You received 5x Iron!
```

Items like `Ore_Iron`, `Rock_Gem_Rudy`, `Weapon_Handgun` should work perfectly now!

---

**New JAR:** `/Users/lubomirstankov/Development/me/Essentials/build/libs/Essentials-1.5.5.jar`

**Status:** ✅ READY TO TEST - Item IDs fixed!

