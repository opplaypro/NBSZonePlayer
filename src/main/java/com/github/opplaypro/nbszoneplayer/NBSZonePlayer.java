package com.github.opplaypro.nbszoneplayer;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.session.SessionManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public final class NBSZonePlayer extends JavaPlugin {

    private final Map<String, RegionPlaylist> regionPlaylists = new HashMap<>();
    private MusicManager musicManager;

    @Override
    public void onEnable() {
        getLogger().info("Enabling NoteblockMusicPlayer");

        if (getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            getLogger().severe("WorldGuard not installed!");
            getLogger().severe("Disabling NoteblockMusicPlayer");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (getServer().getPluginManager().getPlugin("NoteBlockAPI") == null) {
            getLogger().severe("NoteBlockAPI not installed!");
            getLogger().severe("Disabling NoteblockMusicPlayer");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("All dependencies found!");

        this.musicManager = new MusicManager(this);
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(this.musicManager, this);

        try {
            SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
            sessionManager.registerHandler(new RegionEnterHandler.Factory(this), null);            getLogger().info("Region handler enabled!");
        }
        catch (Exception e) {
            getLogger().severe("SEVERE error, disabling NoteblockMusicPlayer");
            getLogger().log(Level.SEVERE, "Encountered error [ERR:NP51]!", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        PluginCommand command = getCommand("nbszoneplayer");
        if (command != null) {
            command.setExecutor(new CommandManager(this));
        } else {
            getLogger().severe("cannot find command 'nbszoneplayer', check config.yml.");
        }

        loadPlaylists();
        getLogger().info("NoteblockMusicPlayer successfully enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling NoteblockMusicPlayer");
    }

    public void loadPlaylists() {
        regionPlaylists.clear();

        File songsFolder = new File(getDataFolder(), "songs");

        if  (!songsFolder.exists()) {
            boolean created = songsFolder.mkdirs();
            if (created) {
                getLogger().info("songs folder created!");
            } else {
                getLogger().warning("Could not create songs folder!");
            }
        }

        ConfigurationSection regionSelection = getConfig().getConfigurationSection("regions");

        if  (regionSelection == null) {
            getLogger().warning("No regions found in config.yml!");
            return;
        }

        for (String regionName : regionSelection.getKeys(false)) {

            String path = regionName + ".";

            List<String> songFileNames = regionSelection.getStringList(path + "songs");

            boolean shuffle = regionSelection.getBoolean(path + "shuffle", true);
            boolean loop = regionSelection.getBoolean(path + "loop", true);
            int volumeInt = regionSelection.getInt(path + "volume", 100);

            if (volumeInt < 0) volumeInt = 0;
            if (volumeInt > 100) volumeInt = 100;

            if (songFileNames.isEmpty()) {
                getLogger().warning("No songs specified in region: "  + regionName);
                continue;
            }

            List<String> foundSongs = new ArrayList<>();
            for (String songFileName : songFileNames) {
                File  songFile = new File(songsFolder, songFileName);
                if (songFile.exists()) {
                    foundSongs.add(songFileName);
                } else {
                    getLogger().warning("Can't find song file: " + songFileName);
                }
            }

            if (foundSongs.isEmpty()) {
                getLogger().warning("No songs found in region: " + regionName);
                continue;
            }

            RegionPlaylist playlist = new RegionPlaylist(foundSongs, shuffle, loop, (byte) volumeInt);
            this.regionPlaylists.put(regionName, playlist);

            getLogger().info(String.format("Loaded region '%s': %d/%d songs, shuffle: %b, loop: %b, volume: %d%%",
                    regionName, foundSongs.size(), songFileNames.size(), shuffle, loop, volumeInt));
        }
        getLogger().info("Finished loading playlists");
    }
    public Map<String, RegionPlaylist> getRegionPlaylists() {
        return regionPlaylists;
    }

    public MusicManager getMusicManager() {
        return musicManager;
    }
}
