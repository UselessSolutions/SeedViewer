package org.useless;

import org.useless.collections.ChunkLocation;
import org.useless.data.Chunk;

public interface ChunkProvider {
    Chunk getChunk(ChunkLocation location);
}
