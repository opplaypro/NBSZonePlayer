package com.github.opplaypro.nbszoneplayer;

import java.util.List;

public record RegionPlaylist(List<String> songs, boolean shuffle, boolean loop, byte volume) {}
