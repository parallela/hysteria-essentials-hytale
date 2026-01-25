# Vote Chest System - Final Implementation Guide

## ✅ What's Been Fixed

### 1. Item ID Corrected
- **Fixed:** `Furniture_Goblin_Chest_Small` (with capital S)

### 2. Event System Changed
- **Changed from:** PlayerInteractEvent (wasn't firing)
- **Changed to:** PlaceBlockEvent via EntityEventSystem
- **Why:** Furniture items trigger block placement events, not interaction events

### 3. Metadata Implementation
- **VoteChest Tag:** `metadata.put("VoteChest", "true")` - marks as special vote chest
- **Display Name:** `metadata.put("DisplayName", "§6§lVote Chest")` - custom colored name

## 📦 How It Works Now

### Giving Vote Chests
```bash
/votechest give PlayerName 1
```

**What happens:**
1. Creates `Furniture_Goblin_Chest_Small` item
2. Adds NBT metadata: `VoteChest=true` and `DisplayName=§6§lVote Chest`
3. Places in player inventory

### Using Vote Chests
1. Player tries to **place** the chest (right-click on ground)
2. `VoteChestPlaceSystem` intercepts the `PlaceBlockEvent`
3. Checks if item has `VoteChest` metadata tag
4. If YES: Cancels placement, consumes item, opens reward UI
5. If NO: Allows normal chest placement (regular furniture chest)

### Custom Display Name
The display name is set via metadata: `§6§lVote Chest`
- `§6` = Gold color
- `§l` = Bold text
- Result: **Bold Gold "Vote Chest"**

**IMPORTANT:** Hytale's engine may or may not render custom display names from metadata:
- If it works: You'll see "§6§lVote Chest" in inventory
- If it doesn't: You'll see default item name or "???"
- **The item will still function correctly either way**

## 🔍 Testing Checklist

1. **Give yourself a vote chest:**
   ```
   /votechest give YourName 1
   ```

2. **Check the item:**
   - Item should appear in inventory
   - Item model should show (small goblin chest)
   - Name might show as custom or default (depends on engine)

3. **Try to place it:**
   - Right-click on ground to place
   - **Should see:** Log message "Vote chest detected! Player: YourName"
   - **Should happen:** Block placement cancelled, item consumed
   - **Should open:** Reward selection UI
   - **Should receive:** Tier announcement message

4. **Test regular chest:**
   - Give yourself regular `Furniture_Goblin_Chest_Small` (without command)
   - Try to place it
   - **Should:** Place normally as furniture

## 🐛 Troubleshooting

### If vote chest still places as normal chest:

**Check console for:**
```
"Vote chest detected! Player: <name>"
```

**If you DON'T see this message:**
- Metadata isn't persisting on the item
- PlaceBlockEvent isn't firing
- Item doesn't have VoteChest tag

**If you DO see this message but UI doesn't open:**
- Check for errors in console
- Verify VoteChestPage UI files exist
- Check player permissions

### If display name shows as "???"
This is expected if:
- Hytale doesn't support custom item display names via metadata
- The item doesn't have a translation key in language files

**This is cosmetic only - the item will still work!**

## 🎯 Why This Approach Works

1. **PlaceBlockEvent catches furniture placement** - This is the key! Furniture items trigger block placement, not item use
2. **Metadata distinguishes special items** - Regular chests can still be placed normally
3. **Entity Event System handles it properly** - Gives us access to player ref and can cancel events

## 📝 Alternative Solutions (If Current Approach Fails)

### Option 1: Use Different Item Type
Instead of furniture chest, use a non-placeable item:
- `Item_Paper` or similar document item
- Can't be placed as block
- Would need different visual representation

### Option 2: Custom Item Asset
- Create custom Hytale item asset
- Would show proper name and icon
- Requires modding game files

### Option 3: Remove Metadata Check
If metadata doesn't persist:
- Remove metadata requirement
- ALL `Furniture_Goblin_Chest_Small` items become vote chests
- Regular chests can't be placed anymore (breaking change)

## 📂 Files Modified

1. **VoteChestCommand.java** - Creates items with metadata
2. **VoteChestPlaceSystem.java** - NEW: Handles placement interception
3. **VoteChestPage.java** - UI for reward selection
4. **VoteChestManager.java** - Manages rewards and tiers
5. **Essentials.java** - Registers the system

## 🎮 Commands & Permissions

### Commands
- `/votechest give <player> [amount]` - Give vote chests (console compatible)

### Permissions
- `essentials.votechest` - Access to command
- `essentials.votechest.give` - Give vote chests to others

## ⚙️ Configuration

File: `plugins/Essentials/votechest.toml`

```toml
[tiers]
legendary-chance = 1.0    # 1%
rare-chance = 10.0        # 10%

[rewards.common]
[rewards.common.copper]
item = "Item_Material_Ore_Copper"
min-quantity = 5
max-quantity = 15
percentage = 25.0

[rewards.legendary]
[rewards.legendary.handgun]
item = "Weapon_Handgun"
min-quantity = 1
max-quantity = 1
percentage = 100.0
```

## 🚀 Next Steps

1. **Test on server:**
   - Reload/restart with new JAR
   - Run `/votechest give YourName 1`
   - Try to place the chest
   - Watch console for "Vote chest detected!"

2. **If it works:**
   - Configure rewards in votechest.toml
   - Set up permissions
   - Enjoy!

3. **If it doesn't work:**
   - Check console logs
   - Verify metadata persists
   - Consider alternative item types

## 📊 Expected Console Output

```
[INFO] [Essentials] Vote chest event system registered.
[INFO] [Essentials] Vote chest detected! Player: parallela_io
[INFO] [VoteChestPage] Opening reward UI for tier: COMMON
```

## ✨ Display Name Format Codes

Current setting: `§6§lVote Chest`

Available codes:
- `§0` - Black
- `§1` - Dark Blue  
- `§2` - Dark Green
- `§3` - Dark Aqua
- `§4` - Dark Red
- `§5` - Dark Purple
- `§6` - Gold (current)
- `§7` - Gray
- `§8` - Dark Gray
- `§9` - Blue
- `§a` - Green
- `§b` - Aqua
- `§c` - Red
- `§d` - Light Purple
- `§e` - Yellow
- `§f` - White
- `§l` - Bold (current)
- `§o` - Italic
- `§n` - Underline
- `§m` - Strikethrough
- `§r` - Reset

You can change the display name in VoteChestCommand.java line ~130:
```java
metadata.put("DisplayName", new BsonString("§6§lVote Chest"));
```

