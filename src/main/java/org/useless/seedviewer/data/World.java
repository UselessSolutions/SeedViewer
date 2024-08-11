package org.useless.seedviewer.data;

import org.useless.seedviewer.gui.ChunkProvider;

import java.io.File;

public interface World {
    File getFolder();
    long getSeed();
    String getName();
    long getLastPlayed();
    ChunkProvider getChunkProvider();
}
