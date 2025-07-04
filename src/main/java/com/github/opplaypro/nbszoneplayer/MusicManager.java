package com.github.opplaypro.nbszoneplayer;

import com.xxmicloxx.NoteBlockAPI.event.SongEndEvent;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.model.playmode.MonoMode;
import com.xxmicloxx.NoteBlockAPI.model.RepeatMode;
import com.xxmicloxx.NoteBlockAPI.songplayer.SongPlayer;
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Random;

public class MusicManager implements Listener {

    private final NBSZonePlayer plugin;
    private final Map<String, Song> loadedSongs = new HashMap<>();
    private final Map<UUID, SongPlayer> activeSongPlayers = new HashMap<>();
    private final Map<UUID, RegionPlaylist> activePlaylists = new HashMap<>();
    private final Map<UUID, PlaybackSource> playbackSources = new HashMap<>();

    public MusicManager(NBSZonePlayer plugin) {
        this.plugin = plugin;
    }

    // waht is the source of playback
    public enum PlaybackSource {
        REGION,
        COMMAND
    }

    // start playlist (region playlist)
    public void startPlaylist(Player player, RegionPlaylist playlist) {

        // stop previous playback
        stopMusic(player);

        // list of all songs in region playlist
        List<String> songFileNames = playlist.songs();
        // if no songs in region do not do anything
        if (songFileNames.isEmpty()) {
            return;
        }

        // add player to active players
        playbackSources.put(player.getUniqueId(), PlaybackSource.REGION);
        activePlaylists.put(player.getUniqueId(), playlist);

        // play song
        playNextSong(player);
    }

    // plays next song in playlsit
    private void playNextSong(Player player) {
        UUID playerUUID = player.getUniqueId();
        // works ONLY if currently is playing from region
        if (playbackSources.get(playerUUID) != PlaybackSource.REGION) {
            return;
        }
        // get playlist
        RegionPlaylist playlist = activePlaylists.get(playerUUID);

        // if no playlist found
        if (playlist == null) {
            return;
        }

        // get list of songs
        List<String> songFileNames = playlist.songs();
        String songToPlay;

        if (playlist.shuffle()) {
            // random song
            songToPlay = songFileNames.get(new Random().nextInt(songFileNames.size()));
        }  else {
            // find next song in playlist
            SongPlayer currentSp = activeSongPlayers.get(playerUUID);
            if (currentSp == null) {
                songToPlay = songFileNames.getFirst();
            } else {
                String currentSongName = currentSp.getSong().getPath().getName();
                int currentIndex = songFileNames.indexOf(currentSongName);
                int nextIndex = currentIndex + 1;
                if (nextIndex >= songFileNames.size()) {
                    // at the end of playlist
                    if (playlist.loop()) {
                        nextIndex = 0;
                    } else {
                        stopMusic(player);
                        return;
                    }
                }
                songToPlay = songFileNames.get(nextIndex);
            }
        }
        // play music
        Song song = loadSong(songToPlay);
        if (song == null) {
            stopMusic(player);
            return;
        }

        RadioSongPlayer songPlayer = new RadioSongPlayer(song);
        songPlayer.setChannelMode(new MonoMode());
        songPlayer.addPlayer(player);
        songPlayer.setRepeatMode(RepeatMode.NO);
        songPlayer.setVolume(playlist.volume());

        activeSongPlayers.put(playerUUID, songPlayer);
        playbackSources.put(player.getUniqueId(), PlaybackSource.REGION);

        songPlayer.setPlaying(true);
    }

    // handle playing next song from playlist
    @EventHandler
    public void onSongEnd(SongEndEvent event) {
        SongPlayer songPlayer = event.getSongPlayer();
        if (activeSongPlayers.containsValue(songPlayer)) {
            for (Map.Entry<UUID, SongPlayer> entry : activeSongPlayers.entrySet()) {
                if (entry.getValue().equals(songPlayer)) {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player != null) {
                        if (!playbackSources.get(player.getUniqueId()).equals(PlaybackSource.REGION)) {
                            return;
                        }
                        playNextSong(player);
                    }
                    break;
                }
            }
        }
    }

    // stop playback
    public void stopMusic(Player player) {

        UUID playerUUID = player.getUniqueId();
        SongPlayer songPlayer = activeSongPlayers.get(playerUUID);

        if  (songPlayer != null) {
            songPlayer.setPlaying(false);
            songPlayer.destroy();
            activeSongPlayers.remove(playerUUID);
        }
        activePlaylists.remove(playerUUID);
    }

    private Song loadSong(String songFileName) {

        if (loadedSongs.containsKey(songFileName)) {
            return loadedSongs.get(songFileName);
        }

        File songsFolder = new File(plugin.getDataFolder(), "songs");
        File songFile = new File(songsFolder, songFileName);

        if (!songFile.exists()) {
            plugin.getLogger().warning("Could not find song file " + songFileName);
            return null;
        }

        Song song = NBSDecoder.parse(songFile);

        if (song == null) {
            plugin.getLogger().warning("Could not parse song file " + songFileName);
            plugin.getLogger().warning("file can be corrupted!");
            return null;
        }

        loadedSongs.put(songFileName, song);
        plugin.getLogger().info("Loaded song " + songFileName);

        return song;
    }

    // handles playing one song from /playsong command
    public boolean playSingleSong(Player player, String songFileName, byte volume) {
        UUID playerUUID = player.getUniqueId();

        stopMusic(player);

        Song song = loadSong(songFileName);
        if (song == null) {
            plugin.getLogger().warning("Could not find song file " + songFileName);
            return false;
        }

        RadioSongPlayer songPlayer = new RadioSongPlayer(song);
        songPlayer.setChannelMode(new MonoMode());
        songPlayer.addPlayer(player);
        songPlayer.setRepeatMode(RepeatMode.NO);
        songPlayer.setVolume(volume);

        activeSongPlayers.put(playerUUID, songPlayer);
        playbackSources.put(playerUUID, PlaybackSource.COMMAND);

        songPlayer.setPlaying(true);
        plugin.getLogger().info("Forced playing song " + songFileName + " for " + player.getName());
        return true;
    }

    public SongPlayer getActiveSongPlayer(Player player) {
        return activeSongPlayers.get(player.getUniqueId());
    }
    public PlaybackSource getPlaybackSource(Player player) {
        return playbackSources.get(player.getUniqueId());
    }
}

