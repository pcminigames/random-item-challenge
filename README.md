# Random Item Challenge

A Minecraft minigame where players receive random items at timed intervals and must use them creatively to survive and thrive. Inspired by the popular *"Random Item Challenge"* concept popularized by Dream and other Minecraft content creators, this plugin features a free-for-all battle royale format where the last player standing wins.

**Requires [GameLib](https://github.com/tmg-minigames/gamelib) plugin to run.**

This is one of the many high-quality Minecraft minigames in the [TMG Minigames Collection](https://github.com/orgs/tmg-minigames/repositories). Make sure to check them out as well!

## Usage

### Starting a Game

1. When all the players are online and ready, run a command to start the game:
   - Use `/ric` to start the game with the default timer value specified in `config.yml` (defaults to 30 seconds).
   - Use `/ric <seconds>` to start the game with a custom timer value.
  
  **Note:** You don't have to create a new world every time you want to play. The plugin will automatically teleport players to different locations in the same world for each game.

2. Players will be teleported to a new location. The preparation phase will begin. During that, players aren't able to do anything, it just gives them time to load the chunks around them. The duration of this phase is specified by the `prepare-time` value in `config.yml` (defaults to 15 seconds).
3. After the preparation phase, the game will start. Players will receive a random item every time the timer runs out. The timer is shown on the bossbar at the top of the screen. You can add or remove items from the pool by editing the `items.yml` file.
4. When a player dies, they will be put into spectator mode and will no longer receive items.
5. The game ends when only one player is left alive.

### Tracking Compass

Players can use the `/compass [player]` command to get a tracking compass that points to another player:
- Use `/compass` to cycle through all alive players.
- Use `/compass <player>` to track a specific player.
- The compass only works during an active game and only tracks alive players.

### Stopping a Game

- A game can be forcefully ended by running `/ric stop`.

## Configuration

You can customize various aspects of the game by editing the `config.yml` and `items.yml` files generated in the `plugins/ric/` folder. See [CONFIG.md](CONFIG.md) for detailed information on configuration options.

## Issues

If you find any issues or have suggestions for improvements, please report them on the [issues page](https://github.com/tmg-minigames/random-item-challenge/issues).

