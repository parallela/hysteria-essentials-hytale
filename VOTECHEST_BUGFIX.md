# Vote Chest System - Bug Fix Summary

## Issues Fixed

### 1. Command Structure Issue
**Problem:** Command was showing "wrong number of arguments expected 0 given 3" error.

**Root Cause:** The GiveCommand subcommand was not properly structured:
- Used `addUsageVariant()` instead of `addSubCommand()`
- Constructor was missing the command name parameter

**Fix Applied:**
- Changed from `addUsageVariant(new GiveCommand(...))` to `addSubCommand(new GiveCommand(...))`
- Updated GiveCommand constructor from `super("Give vote chests...")` to `super("give", "Give vote chests...")`

### 2. Invalid Item ID
**Problem:** Item was showing as invalid with "???" name

**Root Cause:** Item ID was incorrect:
- Used: `Furniture_Goblin_Chest` (lowercase 'small')
- Correct: `Furniture_Goblin_Chest_Small` (capital 'S')

**Fix Applied:**
- Updated `VOTE_CHEST_ITEM` constant in both `VoteChestCommand.java` and `VoteChestEvent.java`
- Changed to: `"Furniture_Goblin_Chest_Small"`

## Files Modified

1. **VoteChestCommand.java**
   - Line 31: Fixed item ID constant
   - Line 40: Changed to `addSubCommand()`
   - Line 59: Added command name "give" to constructor

2. **VoteChestEvent.java**
   - Line 27: Fixed item ID constant

## Testing the Fix

After reloading the plugin, test with:
```
/votechest give <player> 1
```

The player should receive a valid Furniture_Goblin_Chest_Small item that:
- Shows the correct item model
- Can be right-clicked to open the vote chest UI
- Consumes the item and gives random rewards

## Command Usage

### Give Vote Chests
```bash
# Give 1 vote chest (default)
/votechest give PlayerName

# Give multiple vote chests
/votechest give PlayerName 5

# Works from console too
votechest give PlayerName 10
```

### Permissions
- `essentials.votechest` - Access to main command
- `essentials.votechest.give` - Permission to give vote chests

## Notes

- The "???" display name is expected behavior if the item doesn't have a translation key in Hytale's language files
- The item will still function correctly and show its model in-game
- When placed in inventory, it should show as "Furniture Goblin Chest Small" or similar
- Custom display names via NBT metadata could be added in a future enhancement if needed

