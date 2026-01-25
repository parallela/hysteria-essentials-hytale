# Iron Tools Added to Vote Chest!

## ✅ New Legendary Rewards

I've added **Iron Pickaxe** and **Iron Shovel** to the legendary tier with special properties:

### Configuration
```toml
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
```

## 🎯 How It Works

### Very Low Chance
- **10% within legendary tier** (which is already 1% chance)
- **Effective chance: 0.1%** (1 in 1000 vote chests!)
- Much rarer than handgun (40%) or katana (40%)

### Can Give 0 Items
- `min-quantity = 0` means sometimes you get nothing
- `max-quantity = 1` means max 1 tool
- **50% chance to get 0 tools** (if this reward is selected)
- **50% chance to get 1 tool** (if this reward is selected)

### Updated Legendary Percentages
```
Handgun: 40%
Katana: 40%
Iron Pickaxe: 10%
Iron Shovel: 10%
Total: 100%
```

## 📊 Probability Breakdown

### Overall Chances
- **Legendary tier:** 1% of all vote chests
- **Getting iron pickaxe reward:** 10% of legendary = 0.1% overall
- **Actually receiving 1 pickaxe:** 50% of 0.1% = 0.05% overall
- **Getting nothing from pickaxe reward:** 50% of 0.1% = 0.05% overall

### Example: 1000 Vote Chests
- **Legendary tier:** ~10 chests
- **Handgun/Katana:** ~8 chests (80% of legendary)
- **Tools:** ~2 chests (20% of legendary)
- **Actually get tool:** ~1 chest (50% of tool rewards give 0)

## 🎮 Player Experience

### Scenario 1: Rolls Legendary - Tool Reward (0.1% chance)
```
[VoteChest] You rolled a LEGENDARY reward!
[Opening UI...]
[Selects Iron Pickaxe]
[VoteChest] You received 1x Pickaxe Iron!  (if lucky)
OR
[VoteChest] You received 0x Pickaxe Iron!  (if unlucky - 50% chance)
```

### Scenario 2: Most Common (99% chance)
```
[VoteChest] You rolled a Common reward!
[Gets ores/materials]
```

## ⚙️ Customization

### Make Tools More Common
```toml
[rewards.legendary.iron_pickaxe]
percentage = 20.0  # Increase to 20%
```

### Guarantee Tool Drop
```toml
[rewards.legendary.iron_pickaxe]
min-quantity = 1  # Always get 1 (no more 0)
max-quantity = 1
```

### Add to Rare Tier Instead
```toml
[rewards.rare.iron_pickaxe]
item = "Tool_Pickaxe_Iron"
min-quantity = 0
max-quantity = 1
percentage = 5.0  # 5% of rare tier (10% overall chance)
```

## 💡 Why min-quantity = 0?

This creates **extra excitement and rarity**:
- Even if you're lucky enough to roll legendary AND get tool reward
- You still have a 50/50 chance of getting nothing
- Makes actually receiving a tool even more special!

## 🔧 Testing

### Test Legendary Tier (Change temporarily)
```toml
[tiers]
legendary-chance = 100.0  # Force legendary every time
```

Then use multiple vote chests to see the 10% tool chance in action.

**Don't forget to change it back to 1.0 after testing!**

### Test Tool Rewards
1. Force legendary tier (100%)
2. Use 10+ vote chests
3. Should see tools appear ~1-2 times out of 10
4. Of those, ~50% will give 0 items

## 📝 Summary

### What Changed:
- ✅ Added `Tool_Pickaxe_Iron` to legendary rewards
- ✅ Added `Tool_Shovel_Iron` to legendary rewards
- ✅ Set to 10% chance each within legendary tier
- ✅ Can give 0 or 1 tool (50/50 split)
- ✅ Updated documentation

### New Legendary Tier Distribution:
| Reward | Percentage | Effective Chance |
|--------|------------|------------------|
| Handgun | 40% | 0.4% |
| Katana | 40% | 0.4% |
| Iron Pickaxe | 10% | 0.1% |
| Iron Shovel | 10% | 0.1% |

### How to Use:
1. Delete `plugins/Essentials/votechest.toml`
2. Reload plugin
3. New config generates with iron tools
4. Tools are extremely rare!

---

**JAR Location:** `/Users/lubomirstankov/Development/me/Essentials/build/libs/Essentials-1.5.5.jar`

**Status:** ✅ Iron tools added with very low chance and 0-1 quantity!

**Perfect for making tools extra special!** 🛠️

