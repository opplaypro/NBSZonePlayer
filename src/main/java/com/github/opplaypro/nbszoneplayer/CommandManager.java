package com.github.opplaypro.nbszoneplayer;

import com.xxmicloxx.NoteBlockAPI.songplayer.SongPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
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

        switch(cmd.getName().toLowerCase()) {

            case "reloadnbszoneplayer":
                handleReloadPlugin(sender);
                break;

            case "playsong":
                handlePlaysong(sender, label, args);
                break;
            case "stopsong":
                handleStopSong(sender, label, args);
                break;
        }
        return true;
    }

    private void handleReloadPlugin(CommandSender sender) {
        if (!sender.hasPermission("nbsoneplayer.command.reload")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }
        plugin.reloadConfig();
        plugin.loadPlaylists();

        sender.sendMessage(ChatColor.GREEN + "Successfully reloaded!");
    }

    private void handlePlaysong(CommandSender sender, String label, String[] args) {
        MusicManager musicManager = plugin.getMusicManager();
        if (!sender.hasPermission("nbsoneplayer.command.playsong")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }

        if (args.length == 0 || args.length > 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <song_name.nbs> [volume] [player]");
            return;
        }
        if (args.length == 1) {
            if (sender instanceof Player) {
                musicManager.playSingleSong((Player) sender, args[0], (byte) 100);
            } else {
                sender.sendMessage(ChatColor.RED + "You must be specify a player to use this command.");
            }
            return;
        }
        if (args.length == 2) {
            if (sender instanceof Player) {
                int volume;
                try {
                    volume = Integer.parseInt(args[1]);
                }
                catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Please enter a valid volume number.");
                    return;
                }
                if (volume < 0) {
                    volume = 0;
                }
                if  (volume > 100) {
                    volume = 100;
                }
                musicManager.playSingleSong((Player) sender, args[0], (byte) volume);
            } else {
                sender.sendMessage(ChatColor.RED + "You must be or specify a player to use this command.");
            }
            return;
        }

         int volume;
         Player player =  Bukkit.getPlayer(args[2]);
         if  (player == null) {
             sender.sendMessage(ChatColor.RED + "That player is not online.");
             return;
         }
         try {
            volume = Integer.parseInt(args[1]);
         }
         catch (NumberFormatException e) {
             sender.sendMessage(ChatColor.RED + "Please enter a valid volume number.");
             return;
         }
         if (volume < 0) volume = 0;

         if (volume > 100) volume = 100;

         boolean played = musicManager.playSingleSong(player, args[0],(byte) volume);
         if  (played) {
             sender.sendMessage(ChatColor.GREEN + "Playing song");
         }  else {
             sender.sendMessage(ChatColor.RED + "Failed to play song");

        }
    }

    private void handleStopSong(CommandSender sender, String label, String[] args) {
        MusicManager musicManager = plugin.getMusicManager();
        if (!sender.hasPermission("nbsoneplayer.command.play")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }
        if (args.length > 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " [player]");
            return;
        }
        if (args.length == 0) {
            if (sender instanceof Player) {
                SongPlayer currentSp = musicManager.getActiveSongPlayer((Player) sender);
                if (currentSp == null) {
                    sender.sendMessage(ChatColor.RED + "That player is not listening to any music.");
                }
                musicManager.stopMusic((Player) sender);

            } else {
                sender.sendMessage(ChatColor.RED + "You must be or specify a player to use this command.");
            }
            return;
        }
        Player player =  Bukkit.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "That player is not online.");
            return;
        }
        musicManager.stopMusic(player);
        sender.sendMessage(ChatColor.GREEN + "Stopping song");


    }
}

/*



 */