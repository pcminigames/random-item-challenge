package ric.compass;

import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import net.kyori.adventure.text.Component;
import ric.PluginMain;

public class CompassManager implements Listener {
    private static CompassManager instance;
    private final HashMap<String, TrackingCompass> activeCompasses = new HashMap<>();
    private final ShowCoords showCoords;
    private final ShowWhen showWhen;

    public CompassManager(ShowCoords showCoords, ShowWhen showWhen) {
        this.showCoords = showCoords;
        this.showWhen = showWhen;
        PluginMain.getInstance().getServer().getPluginManager().registerEvents(this, PluginMain.getInstance());
        instance = this;
    }

    public CompassManager(ShowCoords showCoords) {this(showCoords, ShowWhen.IN_INVENTORY);}

    public CompassManager() {this(ShowCoords.ALL, ShowWhen.IN_INVENTORY);}

    public static CompassManager getInstance() {
        if (instance == null) {
            instance = new CompassManager(ShowCoords.ALL, ShowWhen.IN_INVENTORY);
        }

        return instance;
    }

    public TrackingCompass createTrackingCompass() {
        TrackingCompass compass = new TrackingCompass();
        this.addCompass(compass);
        return compass;
    }

    public void addCompass(TrackingCompass compass) {
        this.activeCompasses.put(compass.getCompassUUID(), compass);
    }

    public void removeCompass(String uuid) {
        TrackingCompass compass = this.activeCompasses.get(uuid);
        if (compass != null) {compass.destroy();}

        this.activeCompasses.remove(uuid);
    }

    public void removeCompass(TrackingCompass compass) {
        if (compass == null) {return;}
        this.removeCompass(compass.getCompassUUID());
    }

    public HashMap<String, TrackingCompass> getActiveCompasses() {
        return new HashMap<>(this.activeCompasses);
    }

    private boolean coords(Player player, ItemStack compass) {
        if (!TrackingCompass.isTrackingCompass(compass)) {return false;}

        ItemMeta itemMeta = compass.getItemMeta();
        String uuid = itemMeta.getPersistentDataContainer().get(TrackingCompass.compassUUIDKey, PersistentDataType.STRING);
        TrackingCompass trackingCompass = activeCompasses.get(uuid);

        if (trackingCompass == null) {return false;}

        Player trackedPlayer = trackingCompass.getTrackedPlayer();
        if (trackedPlayer == null || !trackedPlayer.isOnline()) {return false;}

        if (!(itemMeta instanceof CompassMeta)) {return false;}
        CompassMeta compassMeta = (CompassMeta) itemMeta;

        compassMeta.setLodestoneTracked(false);
        compassMeta.setLodestone(trackedPlayer.getLocation());

        Location loc = trackedPlayer.getLocation();

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        switch (showCoords) {
            case ALL ->  player.sendActionBar(Component.text(PluginMain.c(" §7Tracking §a" + trackedPlayer.getName() + " §7at §f" + x + " " + y + " " + z)));
            case Y ->    player.sendActionBar(Component.text(PluginMain.c(" §7Tracking §a" + trackedPlayer.getName() + " §7at §fy " + y)));
            case NONE -> {}
        }

        return true;
    }
    
    public void update() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory inventory = player.getInventory();

            switch (showWhen) {
                case IN_INVENTORY -> {
                    for (ItemStack compass : inventory.all(Material.COMPASS).values()) {
                        if (coords(player, compass)) {break;}
                    }
                }
                case IN_HOTBAR -> {
                    for (int i = 0; i < 9; i++) {
                        ItemStack compass = inventory.getItem(i);
                        if (compass != null && coords(player, compass)) {break;}
                    }
                }
                case IN_HAND -> {
                    ItemStack mainHand = player.getInventory().getItemInMainHand();
                    if (coords(player, mainHand)) {break;}
                    ItemStack offHand = player.getInventory().getItemInOffHand();
                    coords(player, offHand);
                }
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        ItemStack itemStack = item.getItemStack();
        Player player = event.getPlayer();

        PluginMain.getInstance().getLogger().log(Level.INFO, "Player {0} dropped an item.", new Object[]{player.getName()});

        if (TrackingCompass.isTrackingCompass(itemStack)) {
            String uuid = itemStack.getItemMeta().getPersistentDataContainer().get(TrackingCompass.compassUUIDKey, PersistentDataType.STRING);
            PluginMain.getInstance().getLogger().log(Level.INFO, "Player {0} dropped a tracking compass ({1}).", new Object[]{player.getName(), uuid});
            item.remove();

            removeCompass(uuid);
        }
    }
}