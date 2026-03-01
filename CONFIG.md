# config.yml

- `default-interval`: The default time interval (in seconds) between item distributions. Defaults to 30.
- `prepare-time`: The time (in seconds) for the preparation phase before the game starts. Defaults to 15.
- `border-size`: The size of the world border during the game. Defaults to 400.
- `game-spacing`: How far apart the game locations are (in blocks). Defaults to 6000.
- `avoided-biomes`: List of biomes to avoid when teleporting players. If the biome in the future game location contains any of these strings, it will be skipped and a new location will be chosen. (for example: `ocean` will avoid all ocean biomes)
- `food`: The food item given to players at the start of the game (supports short notation, e.g., `BAKED_POTATO*64`).
- `effects`: Potion effects given to players during the game. Each effect can have `duration` (in ticks, -1 for infinite) and `amplifier` (0 = level 1, 1 = level 2, etc.).
- `compass-info`: The information displayed on the tracking compass. Supports placeholders: `{TARGET}`, `{OWNER}`, `{DISTANCE}`, `{X}`, `{Y}`, `{Z}`.
- `default-probability`: The default weight for items that don't specify a probability. Defaults to 24.

# items.yml

List of items that can be given to players. Items can be specified in short or detailed notation.

**Short Notation** (for simple items):
- `'<id>': <item_name>` - Gives one of the specified item.
- `'<id>': <item_name>*<count>` - Gives a specified number of the item.

Example:
```yaml
items:
  '001': iron_sword
  '002': diamond*16
  '003': oak_log*64
```

**Detailed Notation** (for items with special properties):
- `id`: The item type (required).
- `count`: Number of items to give. Defaults to 1.
- `name`: Custom display name for the item (colored text supported with color codes).
- `lore`: List of lore lines displayed on the item tooltip.
- `probability`: Weight for random selection. Higher values = more common. Defaults to the value set in `default-probability` (24).
- `enchantments`: List of enchantments with their levels (can exceed normal limits).
- `durability`: Set item durability:
  - `0` = Makes the item unbreakable.
  - Otherwise it sets the item's remaining durability.
- `trim-material`: Armor trim material (e.g., `gold`, `diamond`, `netherite`, etc.).
- `trim-pattern`: Armor trim pattern (e.g., `sentry`, `vex`, `wild`, `coast`, `dune`, etc.).
- `effects`: Potion effects (for potions/arrows). Each effect has:
  - `duration`: Duration in ticks (20 ticks = 1 second, -1 for infinite).
  - `amplifier`: Effect level (0 = level 1, 1 = level 2, etc.). Defaults to 0.
  - `hide`: Whether to hide the effect particles. Defaults to false.
- `potion-color`: Hex color code for potions (e.g., `'#FF0000'`). Must be enclosed in quotes.

Examples:
```yaml
items:
  '100':
    id: diamond_sword
    count: 1
    name: "§6§lEpic Sword"
    lore:
      - "§7A legendary blade"
      - "§7forged in dragon fire"
    enchantments:
      sharpness: 5
      unbreaking: 3
    durability: 0  # Unbreakable
    probability: 5  # Rare item (lower probability)
  
  '101':
    id: splash_potion
    effects:
      strength:
        duration: 3600
        amplifier: 1
        hide: false
    potion-color: '#e2c112'
    name: "Potion of Strength II"
    probability: 15
  
  '102':
    id: diamond_chestplate
    name: "§b§lFrosted Armor"
    enchantments:
      protection: 4
    trim-material: diamond
    trim-pattern: vex
    probability: 8
  
  '103':
    id: iron_pickaxe
    durability: 50  # Only 50 uses remaining
    enchantments:
      efficiency: 3
```
