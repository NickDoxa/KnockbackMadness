package net.oasisgames.km.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class CommandTabs implements TabCompleter {

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command,
                                      @NonNull String label, @NonNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length > 1) {
            return completions;
        }
        if (!(sender instanceof Player player)) return completions;
        if (player.hasPermission("knockback.admin")) {
            completions.add("start");
            completions.add("end");
            completions.add("addpoint");
            completions.add("setspawn");
            completions.add("reload");
            completions.add("config");
        }
        completions.add("join");
        completions.add("leave");
        return completions;
    }

}
