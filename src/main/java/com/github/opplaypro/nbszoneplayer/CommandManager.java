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
    // /reloadnzp command handler
    private void handleReloadPlugin(CommandSender sender) {
        // do not run if no permission
        if (!sender.hasPermission("nbszoneplayer.command.reload")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }
        // reload plugin and load playlists again
        plugin.reloadConfig();
        plugin.loadPlaylists();

        sender.sendMessage(ChatColor.GREEN + "Successfully reloaded!");
    }

    // /playsong <song_name.nbs> [volume] [player] command handler
    private void handlePlaysong(CommandSender sender, String label, String[] args) {
        // do not run if no permission
        if (!sender.hasPermission("nbszoneplayer.command.playsong")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }
        // get musicManager
        MusicManager musicManager = plugin.getMusicManager();

        // none or too many arguments specified
        if (args.length == 0 || args.length > 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <song_name.nbs> [volume] [player]");
            return;
        }

        // specified just song, play for sender if sender is player, volume = 100
        if (args.length == 1) {
            if (sender instanceof Player) {
                //play song
                boolean played = musicManager.playSingleSong((Player) sender, args[0],(byte) 100);
                if  (played) {
                    sender.sendMessage(ChatColor.GREEN + "Playing song");
                }  else {
                    sender.sendMessage(ChatColor.RED + "Failed to play song");

                }
            } else { // if sender is not player
                sender.sendMessage(ChatColor.RED + "You must be or specify a player to use this command.");
            }
            return;
        }
        // specified song and volume, play for sender if sender is player
        if (args.length == 2) {
            if (sender instanceof Player) {
                int volume;
                // try to get volume
                try {
                    volume = Integer.parseInt(args[1]);
                }
                catch (NumberFormatException e) {
                    // if volume is not an int
                    sender.sendMessage(ChatColor.RED + "Please enter a valid volume number.");
                    return;
                }
                // set volume to 0-100 range
                if (volume < 0) {
                    volume = 0;
                }
                if  (volume > 100) {
                    volume = 100;
                }
                musicManager.playSingleSong((Player) sender, args[0], (byte) volume);
            } else {
                // if sender is not player
                sender.sendMessage(ChatColor.RED + "You must be or specify a player to use this command.");
            }
            return;
        }

        // if all arguments were specified

         int volume;
        // check if specified player is online
         Player player =  Bukkit.getPlayer(args[2]);
         if  (player == null) {
             sender.sendMessage(ChatColor.RED + "That player is not online.");
             return;
         }
         // try to get volume
         try {
            volume = Integer.parseInt(args[1]);
         }
         catch (NumberFormatException e) {
             // if volume is not an int
             sender.sendMessage(ChatColor.RED + "Please enter a valid volume number.");
             return;
         }

         // set volume to 0-100 range
         if (volume < 0) volume = 0;

         if (volume > 100) volume = 100;

         //play song
         boolean played = musicManager.playSingleSong(player, args[0],(byte) volume);
         if  (played) {
             sender.sendMessage(ChatColor.GREEN + "Playing song");
         }  else {
             sender.sendMessage(ChatColor.RED + "Failed to play song");

        }
    }

    // /stopsong [player] command handler
    private void handleStopSong(CommandSender sender, String label, String[] args) {
        // do not run if no permission
        MusicManager musicManager = plugin.getMusicManager();
        if (!sender.hasPermission("nbszoneplayer.command.play")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }
        // if too many arguments present
        if (args.length > 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " [player]");
            return;
        }
        //if none arguments, stop playback for sender only if sender is player
        if (args.length == 0) {
            if (sender instanceof Player) {
                // get current song player
                SongPlayer currentSp = musicManager.getActiveSongPlayer((Player) sender);
                if (currentSp == null) {
                    // if player is not listening to any music
                    sender.sendMessage(ChatColor.RED + "That player is not listening to any music.");
                } else {
                    // stop music for sender
                    musicManager.stopMusic((Player) sender);
                    sender.sendMessage(ChatColor.GREEN + "Stopping song");
                }
            } else {
                // sender is not player
                sender.sendMessage(ChatColor.RED + "You must be or specify a player to use this command.");
            }
            return;
        }

        // if one argument is present
        Player player =  Bukkit.getPlayer(args[0]);
        if (player == null) {
            // if player is not online
            sender.sendMessage(ChatColor.RED + "That player is not online.");
        } else {
            // get current song player
            SongPlayer currentSp = musicManager.getActiveSongPlayer((Player) sender);
            if (currentSp == null) {
                // if player is not listening to any music
                sender.sendMessage(ChatColor.RED + "That player is not listening to any music.");
            } else {
                // stop music for sender
                musicManager.stopMusic((Player) sender);
                sender.sendMessage(ChatColor.GREEN + "Stopping song");
            }
        }
    }
}
