package net.oasisgames.km.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Tab Complete Class for the Knockback Madness commands
 */
public class CommandTabs implements TabCompleter {

    /**
     * Tab completes the knockback command
     * @param sender Source of the command.  For players tab-completing a
     *     command inside a command block, this will be the player, not
     *     the command block.
     * @param command Command which was executed
     * @param label Alias of the command which was used
     * @param args The arguments passed to the command, including final
     *     partial argument to be completed
     * @return List of potential command completions
     */
    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command,
                                      @NonNull String label, @NonNull String[] args) {
        if (!label.equalsIgnoreCase("knockback")) return null;
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
