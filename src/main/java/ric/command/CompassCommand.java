package ric.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import ric.PluginMain;
import ric.compass.TrackingCompass;

public class CompassCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {return true;}

        Player player = (Player) sender;

        if (args.length > 1) {
            player.sendMessage("§cUsage: /compass [<player>]");
            return true;
        }

        Inventory inventory = player.getInventory();
        if (inventory.firstEmpty() == -1) {
            player.sendMessage("§cYour inventory is full!");
            return true;
        }

        TrackingCompass compass = PluginMain.compassManager.createTrackingCompass();
        inventory.addItem(compass.getItem());

        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                player.sendMessage("§cPlayer not found.");
                return true;
            }
            compass.track(target);
        }

        return true;
    }
}