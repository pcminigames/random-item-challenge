package com.pythoncraft.ric;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.pythoncraft.ric.command.RICCommand;
import com.pythoncraft.ric.command.RICTabCompleter;
import com.pythoncraft.gamelib.compass.CompassCommand;
import com.pythoncraft.gamelib.compass.CompassTabCompleter;
// import com.pythoncraft.gamelib.compass.CompassManager;
// import com.pythoncraft.gamelib.compass.ShowCoords;
// import com.pythoncraft.gamelib.compass.ShowWhen;
import com.pythoncraft.gamelib.Logger;
import com.pythoncraft.gamelib.Chat;
import com.pythoncraft.gamelib.GameLib;
import com.pythoncraft.gamelib.inventory.ItemLoader;
import com.pythoncraft.gamelib.Timer;


public class PluginMain extends JavaPlugin implements Listener {

    static PluginMain instance;
    public static PluginMain getInstance() { return instance; }

    public static int defaultTime = 60;
    public static int interval;

    public static Random random = new Random();

    public static HashMap<Attribute, Double> attributes = new HashMap<>();

    private File configFile;
    private FileConfiguration config;
    private File itemsFile;
    private FileConfiguration itemsConfig;

    public static int currentGame = -1;
    public static int nextGame = 0;
    public static int borderSize = 400;
    public static int gap = 6000;
    public static int prepareTime = 5;
    public static HashSet<String> avoidedBiomes = new HashSet<>();

    // public static CompassManager compassManager;
    public static BossBar bossBar;

    public static World world;
    public static boolean gameRunning = false;
    public static boolean preparing = false;
    public static HashSet<Player> playersInGame = new HashSet<>();

    public static List<ItemStack> items = new ArrayList<>();

    public Timer timer;

    @Override
    public void onEnable() {
        instance = this;

        Bukkit.getPluginManager().registerEvents(this, this);

        this.configFile = new File(getDataFolder(), "config.yml");
        this.config = YamlConfiguration.loadConfiguration(this.configFile);
 
        this.itemsFile = new File(getDataFolder(), "items.yml");
        this.itemsConfig = YamlConfiguration.loadConfiguration(this.itemsFile);

        world = Bukkit.getWorld("world");

        this.loadConfig();

        Logger.info("Items loaded: {0}", items.size());
        Logger.info("Random Item Challenge plugin enabled!");

        this.getCommand("ric").setExecutor(new RICCommand());
        this.getCommand("ric").setTabCompleter(new RICTabCompleter());
        this.getCommand("compass").setExecutor(new CompassCommand());
        this.getCommand("compass").setTabCompleter(new CompassTabCompleter());

        attributes.put(Attribute.MOVEMENT_SPEED, 0.1);
        attributes.put(Attribute.ATTACK_DAMAGE, 1.0);
        attributes.put(Attribute.JUMP_STRENGTH, 0.42);

        // compassManager = new CompassManager(ShowCoords.ALL, ShowWhen.IN_INVENTORY);

        // Timer.loop(5, (timeLeft) -> {compassManager.update();}).start();
    }

    @Override
    public void onDisable() {
        stopGame();
    }

    public void startGame(int time) {
        Logger.info("Starting Random Item Challenge with interval {0} seconds.", time);
        if (world == null) {return;}

        currentGame = nextGame;

        nextGame = findNextGame(currentGame + 1);
        int x = nextGame * gap;
        int z = 0;
        int y = world.getHighestBlockYAt(x, z);

        config.set("last-game", currentGame);
        try {config.save(configFile);} catch (IOException e) {e.printStackTrace();}

        if (world.getBlockAt(x, y, z).getType().equals(Material.WATER) && world.getBlockAt(x, y - 1, z).getType().equals(Material.WATER)) {
            world.getBlockAt(x, y + 1, z).setType(Material.LILY_PAD);
        }

        WorldBorder worldBorder = world.getWorldBorder();
        worldBorder.setCenter(x, z);
        worldBorder.setSize(borderSize);
        worldBorder.setDamageAmount(100);
        worldBorder.setWarningDistance(0);

        playersInGame.clear();

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * prepareTime, 0, false, false));
            p.setGameMode(GameMode.ADVENTURE);
            for (Attribute attribute : attributes.keySet()) {p.getAttribute(attribute).setBaseValue(0);}
            
            p.teleport(new Location(world, x + 0.5, y + 1.09375, z + 0.5));
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 0, false, false));

            Inventory i = p.getInventory();
            i.clear();
            // i.setItem(0, compassManager.createTrackingCompass().getItem());
            i.addItem(getItemStack(Material.COOKED_PORKCHOP, 64));

            playersInGame.add(p);
        }

        this.timer = new Timer(prepareTime * 20, 20, (i) -> {
            for (Player p : playersInGame) {
                p.sendActionBar(Chat.c("§a§l" + i));
                if (i <= 3) {p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);}
            }
        }, () -> {
            preparing = false;
            gameRunning = true;

            PluginMain.instance.timer = Timer.loop(20, (i1) -> {
                int q = i1 % time;
                PluginMain.bossBar.setProgress(1 - (q / (double) time));

                if (q == 0) {
                    for (Player p : playersInGame) {
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                        giveRandomItem(p);
                    }
                }
            });

            PluginMain.instance.timer.start();

            for (Player p : playersInGame) {
                p.sendActionBar(Chat.c("§c§lGO!"));
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
                for (Attribute attribute : attributes.keySet()) {
                    p.getAttribute(attribute).setBaseValue(attributes.get(attribute));
                }
                p.setGameMode(GameMode.SURVIVAL);
            }
        });

        Timer.after(prepareTime * 20 + 200, () -> {
            GameLib.forceLoadChunkStop(world, currentGame * gap, 0, 2);
            GameLib.forceLoadChunk(world, nextGame * gap, 0, 2);
        });

        preparing = true;
        gameRunning = false;
        bossBar = setupBossbar();
        bossBar.setVisible(true);
        // playersInGame.clear();
        // compassManager.getActiveCompasses().clear();
        this.timer.start();
    }

    public int findNextGame(int start) {
        int g = start;
        while (!isSafe(g * gap, world.getHighestBlockYAt(0, g * gap), 0)) {g++;}
        return g;
    }

    public static ItemStack getItemStack(Material material, int amount) {
        ItemStack item = new ItemStack(material, amount);
        item.setAmount(amount);
        return item;
    }

    public void stopGame() {
        Logger.info("Stopping Random Item Challenge.");
        gameRunning = false;
        preparing = false;

        if (this.timer != null) {this.timer.cancel();}
        if (bossBar != null) {bossBar.removeAll();}
        if (world == null) {return;}

        WorldBorder worldBorder = world.getWorldBorder();
        worldBorder.setSize(60_000_000);
        worldBorder.setCenter(0, 0);
    }

    public static boolean isSafe(int x, int y, int z) {
        if (avoidedBiomes.isEmpty()) {return true;}

        String b = world.getBiome(x, y, z).toString().toUpperCase();

        Logger.info("Checking safe location ({0}).", b);

        for (String biome : avoidedBiomes) {
            if (b.contains(biome)) {
                return false;
                // return true;
            }
        }

        Logger.info("Location is safe.");

        return true;
        // return false;
    }

    public static BossBar setupBossbar() {
        BossBar bar = Bukkit.createBossBar(Chat.c("§a§lRandom Item Challenge"), BarColor.GREEN, BarStyle.SOLID);
        bar.setVisible(false);
        bar.setProgress(1);
        for (Player p : Bukkit.getOnlinePlayers()) {bar.addPlayer(p);}

        return bar;
    }

    public static void giveRandomItem(Player player) {
        int i = random.nextInt(items.size());
        ItemStack item = items.get(i).clone();
        player.getInventory().addItem(item);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!playersInGame.contains(event.getPlayer())) {return;}
        if (preparing) {
            World w = event.getTo().getWorld();
            double x = Math.round(event.getFrom().getX() - 0.5) + 0.5;
            double y = event.getTo().getY();
            double z = Math.round(event.getFrom().getZ() - 0.5) + 0.5;
            float yaw = event.getTo().getYaw();
            float pitch = event.getTo().getPitch();
            event.setTo(new Location(w, x, y, z, yaw, pitch));
        }
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player p)) {return;}

        if (!playersInGame.contains(p)) {return;}
        if (preparing) {event.setCancelled(true);}
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!playersInGame.contains(event.getPlayer())) {return;}
        if (preparing) {event.setCancelled(true);}
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (bossBar != null) {bossBar.addPlayer(player);}

        if (playersInGame.contains(player)) {return;}
        
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(new Location(world, 0.5, 100, currentGame * gap + 0.5));
        player.clearActivePotionEffects();
        for (Attribute attribute : attributes.keySet()) {player.getAttribute(attribute).setBaseValue(attributes.get(attribute));}
        player.getInventory().clear();

        if (gameRunning) {
            player.sendMessage(Chat.c("§c§lA game is currently running. You are in spectator mode."));
            player.sendMessage("You will be teleported to the next game when it starts.");
        } else if (preparing) {
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
        // playersInGame.remove(player);
        bossBar.removePlayer(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!playersInGame.contains(player)) {return;}

        player.getInventory().clear();
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(new Location(world, currentGame * gap + 0.5, 100, 0.5));
        player.clearActivePotionEffects();
        for (Attribute attribute : attributes.keySet()) {player.getAttribute(attribute).setBaseValue(attributes.get(attribute));}

        Logger.info("{0} has died. {1} players remaining.", player.getName(), playersInGame.size());

        playersInGame.remove(player);

        if (playersInGame.size() == 1) {
            Player winner = playersInGame.iterator().next();
            // winner.sendMessage(Chat.c("§a§lYou are the last player standing! You win!"));
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(Chat.c("§a§l" + winner.getName() + " is the last player standing and wins the game!"));
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
            }
            stopGame();
        } else if (playersInGame.size() == 0) {
            Logger.info("All players have died or left. Ending game.");
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(Chat.c("§a§lAll players have died or left. Ending game."));
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
            }
            stopGame();
        }
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player p)) {return;}
        if (!playersInGame.contains(p)) {return;}

        event.setCancelled(true);
    }

    private void loadConfig() {
        gap = this.config.getInt("gap", gap);
        borderSize = this.config.getInt("border-size", borderSize);
        defaultTime = this.config.getInt("default-time", defaultTime);
        prepareTime = this.config.getInt("prepare-time", prepareTime);
        currentGame = this.config.getInt("last-game", currentGame);
        Logger.info("Last game was {0}.", currentGame);
        
        currentGame = findNextGame(currentGame + 1);
        Logger.info("Next game will be {0}.", currentGame);

        avoidedBiomes.clear();
        for (String biome : this.config.getStringList("avoided-biomes")) {
            avoidedBiomes.add(biome.toUpperCase());
        }
        Logger.info("Avoided biomes: {0}", String.join(", ", avoidedBiomes));

        items.clear();
        items = ItemLoader.loadItems(itemsConfig.getConfigurationSection("items"));
    }
}