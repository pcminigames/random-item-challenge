package com.pythoncraft.ric.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class RICTabCompleter implements TabCompleter {
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!(sender instanceof Player)) {return completions;}

        if (args.length == 1) {
            completions.add("30");
            completions.add("60");
            completions.add("90");
            completions.add("stop");
        }

        return completions;
    }
}