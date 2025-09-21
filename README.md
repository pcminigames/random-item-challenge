# Random Item Challenge

A Minecraft minigame where players receive random items and must use them creatively to survive and thrive. Inspired by the popular *"Random Item Challenge"* concept, probably first seen on Dream's YouTube channel, but with some changes, like that it's free-for-all instead of teams.

**Requires [pythoncraft's GameLib](https://github.com/pcminigames/gamelib) plugin to run.**

## Installation

1. Make sure you are running the correct version of Minecraft server (for Random Item Challenge v2.0 it's Paper 1.21.8)
2. Download the latest release from the [releases page](https://github.com/pcminigames/random-item-challenge/releases). You should get a `.jar` file and two `.yml` files.
3. Download the [GameLib](https://github.com/pcminigames/gamelib/releases) plugin. There should be just one `.jar` file.
4. Put both `.jar` files in your server's `plugins` folder.
5. Make a folder named `ric` in your server's `plugins` folder.
6. Put the `config.yml` and `items.yml` files in the `ric` folder.
7. Start/restart your server.

## Usage

1. When all the players are online and ready, run a command to start the game:
   - Use `/ric` to start the game, the timer will be set to the `default-timer` value specified in `config.yml` (defaults to 60 seconds).
   - Use `/ric <seconds>` to start the game with a custom timer value.
2. Players will be teleported to a new location. The preparation phase will begin. During that, players aren't able to do anything, it just gives them time to load the chunks around them. The duration of this phase is specified by the `prepare-time` value in `config.yml` (defaults to 10 seconds).
3. After the preparation phase, the game will start. Players will receive a random item every time the timer runs out. The timer is shown on the bossbar at the top of the screen. You can add or remove items from the pool by editing the `items.yml` file.
4. When a player dies, they will be put into spectator mode and will no longer receive items.
5. The game ends when only one player is left alive.

- A game can be forcefully ended by running `/ric stop`.

## Notes

<!-- - Compatible with [TrackingCompass](...) plugin by ... -->
- Make sure to try out other [pythoncraft's minigames](https://github.com/orgs/pcminigames/repositories)
- Feel free to suggest improvements or report issues ([here](https://github.com/pcminigames/random-item-challenge/issues)).
- Originally made for me and my friends, so don't expect too much polish. However, if you behave how you are expected to behave, everything should work fine.

## Configuration

### config.yml

- `default-timer`: The default time interval (in seconds) between item distributions.
- `prepare-time`: The time (in seconds) for the preparation phase before the game starts.
- `border-size`: The size of the world border during the game.
- `gap`: How far apart are the game locations.
- `avoided-biomes`: List of biomes to avoid when teleporting players. If the biome in the future game location contains any of these strings, it will be skipped and a new location will be chosen. (for example: `ocean` will avoid all ocean biomes)
### items.yml
- List of items that can be given to players. You can add or remove items from this list.
- Each item has to have an id, which is a unique identifier for that item. It can be anything, as long as it's unique. In the default file, items are named `001`, `002`, etc. They cannot contain special characters like `'` or `:`.
- There are two ways to specify an item:

1. Short notation
    - Is used for simple items without any special properties.
    - `'<id>': <item_name>` - Gives one of the specified item.
    - `'<id>': <item_name>*<count>` - Gives a specified number of the item. `<count>` must be a positive integer, but can be greater 64.
    - Example:

      ```yaml
      items:
        '001': dirt
        '002': oak_log*16
        '003': diamond_sword
      ```

2. Detailed notation
    - Is used for items that require special properties, like custom names, lore, or enchantments.
    - Has multiple properties. The only one required is `id`.
    - `'<id>':` - Starts the detailed item definition.
      - `id: <item_name>` - Specifies the item name.
      - `count: <count>` - Specifies the number of the item. Must be a positive integer, but can be greater 64. Defaults to 1 if not specified.
      - `custom-name: <custom_name>` - Specifies a custom name for the item.
      - `enchantments:` - Starts the enchantments definition.
        - `<enchantment_1>: <level>` - Specifies an enchantment and its level. `<level>` must be a positive integer.
        - `<enchantment_2>: <level>`
      - `effects:` - Starts the effects definition.
        - `<effect_1>:` - Specifies an effect.
          - `duration: <duration>` - Duration of the effect in ticks (1 tick = 1/20 seconds). Must be a positive integer.
          - `amplifier: <amplifier>` - Amplifier of the effect. Must be a non-negative integer (0 = level 1, 1 = level 2, etc.). Defaults to 0 if not specified.
        - `<effect_2>: ...`
      - `potion-color: '<color>'` - Specifies the color of the potion. Must be a valid hex color code (e.g. `#FF0000` for red). Must be enclosed in single quotes.
    - Example:

      ```yaml
      items:
        '003':
          id: diamond_sword
          count: 1
          custom-name: "Epic Sword"
          enchantments:
            sharpness: 5
            unbreaking: 3
        '004':
          id: potion
          count: 2
          custom-name: "Speed Potion"
          effects:
            speed:
              duration: 600
              amplifier: 1
          potion-color: '#FF0000'
      ```

## Issues

If you find any issues or have suggestions for improvements, please report them on the [issues page](https://github.com/pcminigames/random-item-challenge/issues).
