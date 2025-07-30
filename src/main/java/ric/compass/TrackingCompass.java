package ric.compass;

import java.util.UUID;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import ric.PluginMain;

public class TrackingCompass {    
    private Player trackedPlayer;
    private final ItemStack compassItem;
    private final String compassUUID;
    public static final NamespacedKey isCompassKey = new NamespacedKey(PluginMain.getInstance(), "tracking-compass");
    public static final NamespacedKey compassUUIDKey = new NamespacedKey(PluginMain.getInstance(), "compass-uuid");

    public TrackingCompass(Player trackedPlayer) {
        this.compassUUID = UUID.randomUUID().toString();
        this.compassItem = createTrackingCompass(this.compassUUID);
        this.trackedPlayer = trackedPlayer;
    }

    public TrackingCompass() {this(null);}

    public void track(Player player) {
        this.trackedPlayer = player;
    }

    public void destroy() {
        this.trackedPlayer = null;
        
        if (this.compassItem != null) {
            ItemMeta meta = this.compassItem.getItemMeta();
            meta.getPersistentDataContainer().remove(isCompassKey);
            meta.getPersistentDataContainer().remove(compassUUIDKey);
            this.compassItem.setItemMeta(meta);
        }
        this.compassItem.setType(org.bukkit.Material.AIR);
    }

    public ItemStack getItem() {
        return this.compassItem;
    }

    public Player getTrackedPlayer() {
        return this.trackedPlayer;
    }

    public String getCompassUUID() {
        return this.compassUUID;
    }

    

    public static ItemStack createTrackingCompass(String uuid) {
        ItemStack compass = new ItemStack(org.bukkit.Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        
        meta.setDisplayName(PluginMain.c("§d§lTracking Compass"));
        meta.getPersistentDataContainer().set(isCompassKey, PersistentDataType.BOOLEAN, true);
        meta.getPersistentDataContainer().set(compassUUIDKey, PersistentDataType.STRING, uuid);

        compass.setItemMeta(meta);

        return compass;
    }

    public static boolean isTrackingCompass(ItemStack item) {
        if (item == null || item.getType() != org.bukkit.Material.COMPASS) {return false;}

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {return false;}

        return meta.getPersistentDataContainer().has(isCompassKey, PersistentDataType.BOOLEAN);
    }
}