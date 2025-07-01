package com.github.opplaypro.nbszoneplayer;

import java.util.List;


public final class RegionPlaylist {

    private final List<String> songs;
    private final boolean shuffle;
    private final boolean loop;

    public RegionPlaylist(List<String> songs, boolean shuffle, boolean loop) {
        this.songs = songs;
        this.shuffle = shuffle;
        this.loop = loop;
    }

    public List<String> getSongs() {
        return songs;
    }

    public boolean isShuffle() {
        return shuffle;
    }

    public boolean isLoop() {
        return loop;
    }
}
