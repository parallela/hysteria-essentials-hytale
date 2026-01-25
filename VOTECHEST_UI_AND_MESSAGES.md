# Vote Chest System - Complete with UI & Configurable Messages!

## ✅ What's New

### 1. Configurable Messages
All vote chest messages can now be customized in `votechest.toml`!

### 2. UI Support
- **UI Mode (default):** Players see all reward options and can choose
- **Auto-Reward Mode:** Automatically gives a random reward (no UI)
- Toggle between modes in config!

### 3. Fixed Item IDs
All items use correct Hytale IDs (Ore_Iron, Rock_Gem_Rudy, etc.)

## 🎮 How It Works

### With UI Enabled (Default)
```
1. Player uses vote chest
2. Tier is rolled
3. UI opens showing all possible rewards for that tier
4. Player clicks to select desired reward
5. Items are given
```

### With UI Disabled
```
1. Player uses vote chest
2. Tier is rolled
3. Random reward selected automatically
4. Items are given directly
```

## ⚙️ Configuration (votechest.toml)

### UI Settings
```toml
[ui]
enabled = true  # true = show UI, false = auto-give reward
```

### Tier Chances
```toml
[tiers]
legendary-chance = 1.0   # 1%
rare-chance = 10.0       # 10%
# Remaining 89% = common
```

### Customizable Messages
```toml
[messages]
common-roll = "&8[&eVoteChest&8] &7You rolled a Common reward!"
rare-roll = "&8[&eVoteChest&8] &9You rolled a Rare reward!"
legendary-roll = "&8[&eVoteChest&8] &6You rolled a LEGENDARY reward!"
reward-received = "&8[&eVoteChest&8] &aYou received %quantity%x %item%!"
no-rewards = "&8[&eVoteChest&8] &cNo rewards available for this tier!"
ui-hint = "&8[&eVoteChest&8] &7Sneak + right-click to see reward options!"
```

**Message Placeholders:**
- `%quantity%` - Number of items received
- `%item%` - Item name (auto-cleaned, e.g., "Iron" instead of "Ore_Iron")

### Reward Configuration
```toml
[rewards.common.iron_ore]
item = "Ore_Iron"
min-quantity = 3
max-quantity = 8
percentage = 30.0
```

## 🎨 Customizing Messages

### Example: Change Colors
```toml
[messages]
# Make legendary more exciting
legendary-roll = "&6✨ &l&6LEGENDARY REWARD! &6✨"

# Add emojis or symbols
reward-received = "&a► You got %quantity%x %item%! ◄"

# Custom prefix
common-roll = "&b[Rewards] &7Common tier unlocked!"
```

### Color Codes
- `&0-&9, &a-&f` - Standard colors
- `&l` - Bold
- `&o` - Italic
- `&n` - Underline
- `&m` - Strikethrough

## 🎁 Configured Rewards

### Common Tier (89%)
- **Ore_Iron** (3-8) - 30%
- **Ore_Gold** (2-5) - 25%
- **Ore_Cobalt** (2-4) - 20%
- **Ingredient_Charcoal** (5-15) - 15%
- **Furniture_Crude_Torch** (8-16) - 10%

### Rare Tier (10%)
- **Rock_Gem_Rudy** (1-3) - 40%
- **Rock_Gem_Diamond** (1-2) - 30%
- **Ore_Prisma** (2-5) - 30%

### Legendary Tier (1%)
- **Weapon_Handgun** (1) - 50%
- **Weapon_Longsword_Katana** (1) - 50%

## 🧪 Testing

### Step 1: Delete Old Config
```bash
rm plugins/Essentials/votechest.toml
```

### Step 2: Reload Plugin
Upload new JAR and restart

### Step 3: Test with UI
```
/votechest give parallela_io 1
```
- Place chest
- UI should open (if enabled)
- See all rewards for rolled tier
- Click to select

### Step 4: Test Auto-Reward
Edit `votechest.toml`:
```toml
[ui]
enabled = false
```
Reload and test - should auto-give reward

## 📊 UI vs Auto-Reward Comparison

| Feature | UI Mode | Auto-Reward Mode |
|---------|---------|------------------|
| **Player Choice** | ✅ Yes | ❌ No |
| **Speed** | Slower (selection) | ⚡ Instant |
| **UI Crashes** | Possible | ❌ Never |
| **Best For** | Player engagement | Automation/speed |

## 🐛 Troubleshooting

### UI Not Opening
1. Check console for errors
2. Verify `ui.enabled = true` in config
3. Check if UI files are in JAR
4. Try setting `ui.enabled = false` for auto-reward mode

### Wrong Items Given
1. Delete `plugins/Essentials/votechest.toml`
2. Reload plugin (regenerates with correct IDs)
3. Verify item IDs match Hytale items exactly

### Messages Not Showing Correctly
1. Check color codes (use `&` not `§`)
2. Verify placeholders: `%quantity%` and `%item%`
3. Reload config with `/essentials reload`

## 💡 Pro Tips

### Tip 1: Disable UI for Voting Rewards
If you're using this for automated voting rewards:
```toml
[ui]
enabled = false  # Auto-give, no player interaction needed
```

### Tip 2: Custom Messages Per Language
You could create multiple configs for different languages:
- `votechest.toml` (English)
- `votechest_es.toml` (Spanish)
- `votechest_fr.toml` (French)

### Tip 3: Seasonal Messages
Update messages for events:
```toml
[messages]
legendary-roll = "&6🎃 LEGENDARY HALLOWEEN REWARD! 🎃"
```

### Tip 4: Add Reward Hints
```toml
[messages]
common-roll = "&7Common reward! &8(Basic ores and materials)"
rare-roll = "&9Rare reward! &8(Gems and special ores)"
legendary-roll = "&6LEGENDARY! &8(Powerful weapons!)"
```

## 📝 Configuration Examples

### Example 1: Minimal Messages
```toml
[messages]
common-roll = "&7Common"
rare-roll = "&9Rare"
legendary-roll = "&6Legendary"
reward-received = "&a+%quantity% %item%"
```

### Example 2: Detailed Messages
```toml
[messages]
common-roll = "&8[&eVoteChest&8] &7You have been granted a &fCommon &7reward tier! Opening selection..."
rare-roll = "&8[&eVoteChest&8] &9Congratulations! You rolled a &b&lRare &9reward!"
legendary-roll = "&8[&eVoteChest&8] &6&l✦ LEGENDARY ✦ &eAn extremely rare reward awaits!"
reward-received = "&8[&eVoteChest&8] &aSuccess! &7You received &f%quantity%x &e%item%&7!"
```

### Example 3: Fun/Playful Messages
```toml
[messages]
common-roll = "&7Meh, just common stuff..."
rare-roll = "&9Ooh, shiny! A rare reward!"
legendary-roll = "&6&lHOLY COW! LEGENDARY!!!"
reward-received = "&aYay! Got %quantity%x %item%! :D"
```

## 🎯 Summary

### What You Can Do Now:
1. ✅ Toggle UI on/off via config
2. ✅ Customize all messages
3. ✅ Use placeholders for dynamic text
4. ✅ Change colors and formatting
5. ✅ Choose between UI selection or auto-reward

### Config File Location:
`plugins/Essentials/votechest.toml`

### Commands:
- `/votechest give <player> [amount]` - Give vote chests
- `/essentials reload` - Reload configs (including messages)

### Permissions:
- `essentials.votechest` - Use command
- `essentials.votechest.give` - Give to others

---

**JAR Location:** `/Users/lubomirstankov/Development/me/Essentials/build/libs/Essentials-1.5.5.jar`

**Status:** ✅ COMPLETE - UI + Configurable Messages

**Test it now!** Delete old config, reload plugin, and enjoy fully customizable vote chests! 🎉

