package com.pythoncraft.ric;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import com.pythoncraft.ric.command.RICCommand;
import com.pythoncraft.ric.command.RICTabCompleter;
import com.pythoncraft.gamelib.compass.CompassCommand;
import com.pythoncraft.gamelib.compass.CompassTabCompleter;
import com.pythoncraft.gamelib.compass.CompassManager;
import com.pythoncraft.gamelib.compass.ShowWhen;
import com.pythoncraft.gamelib.game.GameManager;
import com.pythoncraft.gamelib.game.event.GameEndEvent;
import com.pythoncraft.gamelib.game.event.GamePrepareEvent;
import com.pythoncraft.gamelib.game.event.GameStartEvent;
import com.pythoncraft.gamelib.game.event.TickEvent;
import com.pythoncraft.gamelib.Logger;
import com.pythoncraft.gamelib.PlayerActions;
import com.pythoncraft.gamelib.Chat;
import com.pythoncraft.gamelib.inventory.ItemLoader;
import com.pythoncraft.gamelib.Timer;


public class PluginMain extends JavaPlugin implements Listener {

    static PluginMain instance;
    public static PluginMain getInstance() { return instance; }

    private File configFile;
    private FileConfiguration config;
    private File itemsFile;
    private FileConfiguration itemsConfig;

    public ItemStack food;
    public List<PotionEffect> effects = new ArrayList<>();
    public int borderSize = 400;
    public int gap = 6000;
    public int prepareTime = 5;
    public HashSet<String> avoidedBiomes = new HashSet<>();
    public int defaultInterval = 60;
    public int interval;

    public CompassManager compassManager;
    public GameManager gameManager;
    public World world;
    public Timer timer;
    public Random random = new Random();

    public List<ItemStack> items = new ArrayList<>();

    @Override
    public void onEnable() {
        instance = this;

        Bukkit.getPluginManager().registerEvents(this, this);

        this.configFile = new File(getDataFolder(), "config.yml");
        this.config = YamlConfiguration.loadConfiguration(this.configFile);
        this.itemsFile = new File(getDataFolder(), "items.yml");
        this.itemsConfig = YamlConfiguration.loadConfiguration(this.itemsFile);

        this.world = Bukkit.getWorld("world");
        this.loadConfig();

        Logger.info("Random Item Challenge plugin enabled!");
        Logger.info("Items loaded: {0}", this.items.size());

        this.getCommand("ric").setExecutor(new RICCommand());
        this.getCommand("ric").setTabCompleter(new RICTabCompleter());
        this.getCommand("compass").setExecutor(new CompassCommand());
        this.getCommand("compass").setTabCompleter(new CompassTabCompleter());

        this.compassManager = new CompassManager("§7Tracking §a{TARGET} §7at §f{X} {Y} {Z}", ShowWhen.IN_HAND);
        this.gameManager = new GameManager();
        this.gameManager.setAvoidedBiomes(this.avoidedBiomes);
        this.gameManager.setBorder(this.borderSize);
        this.gameManager.setGap(this.gap);
        this.gameManager.setWorld(this.world);
        this.gameManager.setBossbar(BarColor.GREEN, BarStyle.SOLID);
        this.gameManager.setConfig(configFile, config, "last-game");
        this.gameManager.setPlayerSetupMethod(
            PlayerActions.setupPlayerPrepare(GameMode.ADVENTURE, false, this.food, true, null),
            PlayerActions.setupPlayerReset(this.effects));
        Logger.info("Food item: {0}", this.food);

        Timer.loop(5, (timeLeft) -> {compassManager.update();}).start();
    }

    @Override
    public void onDisable() {
        stopGame();
    }

    public void startGame(int time) {
        Logger.info("Starting Random Item Challenge with interval {0} seconds.", time);

        this.interval = time;

        this.world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        this.world.setTime(1000); // Set time to morning

        this.gameManager.setGameTime(0, this.prepareTime, 0);
        this.gameManager.startGame(this.world);
    }

    @EventHandler
    public void onGamePrepare(GamePrepareEvent event) {
    }

    @EventHandler
    public void onGameStart(GameStartEvent event) {
        Logger.info("Game start event triggered.");
    }

    @EventHandler
    public void onGameTick(TickEvent event) {
        int timeLeft = event.getTimeRemaining();

        if (this.gameManager.isPreparing) {
            this.gameManager.bossbar.setTitle(Chat.c("§lPrepare: §c§l" + timeLeft + "§r§l seconds remaining"));
            this.gameManager.bossbar.setProgress((double) timeLeft / this.gameManager.prepareTimeSec);
            
            Chat.actionBar(this.gameManager.playersInGame, "§a§l" + timeLeft);
            for (Player p : this.gameManager.playersInGame) {
                if (timeLeft == 0) {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
                    Chat.actionBar(p, "§c§lGO!");
                } else if (timeLeft <= 3) {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                }
            }
        } else if (this.gameManager.isGame) {
            int timeElapsed = event.getElapsedSeconds() % this.interval;

            this.gameManager.bossbar.setTitle(Chat.c("§lNext item in §a§l" + (this.interval - timeElapsed) + "§r§l seconds"));
            this.gameManager.bossbar.setProgress(1 - ((double) timeElapsed / this.interval));

            if (timeElapsed == 0) {
                for (Player p : this.gameManager.playersInGame) {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                    giveRandomItem(p);
                }
            }
        }
    }

    @EventHandler
    public void onGameEnd(GameEndEvent event) {
    }

    public void stopGame() {
        Logger.info("Stopping Random Item Challenge.");
        
        this.gameManager.stopGame();

        if (this.world == null) {return;}

        WorldBorder worldBorder = this.world.getWorldBorder();
        worldBorder.reset();
    }

    public void giveRandomItem(Player player) {
        int i = random.nextInt(this.items.size());
        ItemStack item = this.items.get(i).clone();
        player.getInventory().addItem(item);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (this.gameManager.bossbar != null) {this.gameManager.bossbar.addPlayer(player);}

        if (this.gameManager.playersInGame != null && this.gameManager.playersInGame.contains(player)) {return;}
        
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(this.gameManager.getSpectatorLocation());
        player.clearActivePotionEffects();
        player.getInventory().clear();

        if (this.gameManager.isGame) {
            player.sendMessage(Chat.c("§c§lA game is currently running. You are in spectator mode."));
            player.sendMessage("You will be teleported to the next game when it starts.");
        } else if (this.gameManager.isPreparing) {
            player.sendMessage(Chat.c("§c§lA game is currently being prepared. You are in spectator mode."));
            player.sendMessage("You will be teleported to the next game when it starts.");
        } else {
            player.sendMessage(Chat.c("§a§lNo game is currently running."));
            player.sendMessage("You can start a new game with /ric or /ric <time>.");
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (this.gameManager.playersInGame != null) {
            PlayerActions.setupPlayerReset(effects).accept(player, this.gameManager.playersInGame);
        }

        if (this.gameManager.bossbar != null) {this.gameManager.bossbar.removePlayer(player);}
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!this.gameManager.playersInGame.contains(player)) {return;}

        player.getInventory().clear();
        PlayerActions.setupPlayerReset(effects).accept(player, this.gameManager.playersInGame);
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(this.gameManager.getSpectatorLocation());
        player.clearActivePotionEffects();

        Logger.info("{0} has died. {1} players remaining.", player.getName(), this.gameManager.playersInGame.size());

        this.gameManager.playersInGame.remove(player);

        if (this.gameManager.playersInGame.size() == 1) {
            Player winner = this.gameManager.playersInGame.iterator().next();
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(Chat.c("§a§l" + winner.getName() + " is the last player standing and wins the game!"));
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
            }
            stopGame();
        } else if (this.gameManager.playersInGame.size() == 0) {
            Logger.info("All players have died or left. Ending game.");
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(Chat.c("§c§lAll players have died or left. Ending game."));
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
            }
            stopGame();
        }
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player p)) {return;}
        if (!this.gameManager.playersInGame.contains(p)) {return;}

        event.setCancelled(true);
    }

    private void loadConfig() {
        this.food = ItemLoader.loadShortItemStack(this.config.getString("food", ""));
        this.effects = ItemLoader.loadPotionEffects(this.config.getConfigurationSection("effects"));
        this.gap = this.config.getInt("game-spacing", this.gap);
        this.borderSize = this.config.getInt("border-size", this.borderSize);
        this.defaultInterval = this.config.getInt("default-game-interval", this.defaultInterval);
        this.prepareTime = this.config.getInt("prepare-time", this.prepareTime);
        
        this.avoidedBiomes.clear();
        for (String biome : this.config.getStringList("avoided-biomes")) {
            this.avoidedBiomes.add(biome.toUpperCase());
        }
        Logger.info("Avoided biomes: {0}", String.join(", ", this.avoidedBiomes));

        this.items.clear();
        this.items = ItemLoader.loadItems(this.itemsConfig.getConfigurationSection("items"));
    }
}