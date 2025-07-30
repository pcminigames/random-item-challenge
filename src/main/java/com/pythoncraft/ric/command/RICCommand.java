package com.pythoncraft.ric.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.pythoncraft.gamelib.Chat;
import com.pythoncraft.ric.PluginMain;

public class RICCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender == null || !(sender instanceof Player)) {return false;}

        Player player = (Player) sender;

        if (args.length > 1) {
            player.sendMessage("Usage: /ric <stop|[time]>");
            return true;
        }

        int time = PluginMain.defaultTime;

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("stop")) {
                if (!PluginMain.gameRunning) {player.sendMessage(Chat.c(" §c§lNo game is currently running.")); return true;}

                PluginMain.getInstance().stopGame();
                return true;
            }

            if (!args[0].matches("\\d+")) {
                player.sendMessage(Chat.c(" §c§lInvalid time format. Please enter a number."));
                return true;
            }

            time = Integer.parseInt(args[0]);
        }

        if (PluginMain.preparing) {player.sendMessage(Chat.c(" §c§lA game is already being prepared.")); return true;}
        if (PluginMain.gameRunning) {PluginMain.getInstance().stopGame();}
        PluginMain.getInstance().startGame(time);

        return true;
    }
}