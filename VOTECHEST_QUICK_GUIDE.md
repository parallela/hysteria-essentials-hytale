# Vote Chest Quick Setup Guide

## 🚀 Quick Start

### 1. Install
Place `Essentials-1.5.5.jar` in `plugins/` folder

### 2. Delete Old Config (Important!)
```bash
rm plugins/Essentials/votechest.toml
```

### 3. Start/Reload Server
Config will auto-generate with correct settings

### 4. Choose Your Mode

**Option A: UI Mode (Default)**
```toml
[ui]
enabled = true  # Players see all rewards and choose
```

**Option B: Auto-Reward Mode**
```toml
[ui]
enabled = false  # Auto-gives random reward (faster)
```

## 🎮 Usage

### Give Vote Chest
```
/votechest give PlayerName 1
```

### Player Uses It
- Right-click on ground with chest
- **If UI enabled:** UI opens, player selects reward
- **If UI disabled:** Random reward given instantly

## ⚙️ Essential Config Options

### votechest.toml Location
`plugins/Essentials/votechest.toml`

### Key Settings

```toml
[ui]
enabled = true  # Toggle UI on/off

[tiers]
legendary-chance = 1.0   # 1% legendary
rare-chance = 10.0       # 10% rare
# 89% common

[messages]
common-roll = "&7You rolled Common!"
rare-roll = "&9You rolled Rare!"
legendary-roll = "&6LEGENDARY!"
reward-received = "&aYou got %quantity%x %item%!"

[rewards.common.iron]
item = "Ore_Iron"
min-quantity = 3
max-quantity = 8
percentage = 30.0
```

## 🎨 Quick Message Customization

### Change Roll Messages
```toml
[messages]
common-roll = "&8[Reward] &7Common tier"
rare-roll = "&8[Reward] &9Rare tier"
legendary-roll = "&8[Reward] &6&lLEGENDARY"
```

### Change Reward Message
```toml
[messages]
reward-received = "&a✓ Received %quantity%x %item%"
```

**Placeholders:**
- `%quantity%` → Number received
- `%item%` → Item name

## 🔧 Common Changes

### Add New Reward
```toml
[rewards.rare.emerald]
item = "Rock_Gem_Emerald"
min-quantity = 1
max-quantity = 3
percentage = 20.0
```

### Change Tier Chances
```toml
[tiers]
legendary-chance = 5.0   # Increase to 5%
rare-chance = 20.0       # Increase to 20%
```

### Disable UI (Auto-Reward)
```toml
[ui]
enabled = false
```

## 📋 Available Items

Use these exact IDs in config:

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

**Tools:**
- Tool_Pickaxe_Iron
- Tool_Shovel_Iron

**Others:**
- Ingredient_Charcoal
- Furniture_Crude_Torch

## 🐛 Troubleshooting

### UI Not Working?
Set `ui.enabled = false` for auto-reward mode

### Wrong Items?
1. Delete `votechest.toml`
2. Reload plugin
3. Uses correct IDs now

### Messages Not Updating?
```
/essentials reload
```

## 💬 Permissions

```yaml
essentials.votechest        # Use /votechest
essentials.votechest.give   # Give to others
```

## 📝 Reload Config
After editing `votechest.toml`:
```
/essentials reload
```

---

**That's it!** Delete old config, reload, and you're ready to go! 🎉

**UI Mode:** Players choose their reward  
**Auto Mode:** Instant random reward

**All messages customizable!**

