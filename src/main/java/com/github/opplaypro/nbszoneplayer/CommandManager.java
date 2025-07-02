package com.github.opplaypro.nbszoneplayer;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandManager implements CommandExecutor {

    private final NBSZonePlayer plugin;

    public CommandManager(NBSZonePlayer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Use /" + label + " reload to reload!");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("NBSZonePlayer.command.reload")) {
                    sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to use this command!");
                    return true;
                }

                plugin.reloadConfig();
                plugin.loadPlaylists();

                sender.sendMessage(ChatColor.GREEN + "Config Reloaded!");
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Use /" + label + " reload to reload!");
                break;
        }
        return true;
    }
}

/*

if (args.length >0) {
            if (args[0].equalsIgnoreCase("reload")) {

                plugin.reloadConfig();
                plugin.loadPlaylists();

                sender.sendMessage("§aPlugin successfully reloaded!");
                return true;
            }
        }

        sender.sendMessage("§cUse /" + label + " reload to reload!");
        return true;
 */