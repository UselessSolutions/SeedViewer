package org.useless.seedviewer.gui;

import org.useless.seedviewer.collections.ChunkLocation;
import org.useless.seedviewer.data.Chunk;

public interface ChunkProvider {
    Chunk getChunk(ChunkLocation location);
}
