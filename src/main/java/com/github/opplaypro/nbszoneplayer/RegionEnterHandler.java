package com.github.opplaypro.nbszoneplayer;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import com.xxmicloxx.NoteBlockAPI.songplayer.SongPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;


public class RegionEnterHandler extends Handler {

    private final NBSZonePlayer plugin;

    public RegionEnterHandler(Session session, NBSZonePlayer plugin) {
        super(session);
        this.plugin = plugin;
    }

    public static class Factory extends Handler.Factory<RegionEnterHandler> {
        private final NBSZonePlayer plugin;

        public Factory(NBSZonePlayer plugin) {
            this.plugin = plugin;
        }

        @Override
        public RegionEnterHandler create(Session session) {
            return new RegionEnterHandler(session, this.plugin);
        }
    }

    @Override
    public boolean onCrossBoundary(LocalPlayer localPlayer, Location from, Location to, ApplicableRegionSet toSet,
                                   Set<ProtectedRegion> entered, Set<ProtectedRegion> left, MoveType moveType) {

        Player bukkitPlayer = Bukkit.getPlayer(localPlayer.getUniqueId());
        if (bukkitPlayer == null) {
            return true;
        }

        MusicManager musicManager = plugin.getMusicManager();
        Map<String, RegionPlaylist> playlists = plugin.getRegionPlaylists();

        RegionPlaylist highestPriorityPlaylist = null;

        for (ProtectedRegion region : toSet.getRegions()) {
            RegionPlaylist foundPlaylist = playlists.get(region.getId());
            if (foundPlaylist != null) {
                highestPriorityPlaylist = foundPlaylist;
                break;
            }
        }

        SongPlayer currentSongPlayer = musicManager.getActiveSongPlayer(bukkitPlayer);

        if (highestPriorityPlaylist != null) {

            if (currentSongPlayer != null) {

                MusicManager.PlaybackSource source = musicManager.getPlaybackSource(bukkitPlayer);

                if (source == MusicManager.PlaybackSource.COMMAND) {
                    return true;
                }
                String currentSongFileName = currentSongPlayer.getSong().getPath().getName();

                if (!highestPriorityPlaylist.songs().contains(currentSongFileName)) {
                    musicManager.startPlaylist(bukkitPlayer, highestPriorityPlaylist);
                }

            } else {
                musicManager.startPlaylist(bukkitPlayer, highestPriorityPlaylist);
            }
        } else {
            if (currentSongPlayer != null) {
                MusicManager.PlaybackSource source = musicManager.getPlaybackSource(bukkitPlayer);
                    if (source == MusicManager.PlaybackSource.REGION) {
                        musicManager.stopMusic(bukkitPlayer);
                    }
            }
        }
        return true;
    }
}
