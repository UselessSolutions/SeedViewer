package org.uselesssolutions;

import org.uselesssolutions.collections.ChunkLocation;
import org.uselesssolutions.data.Chunk;

public interface ChunkProvider {
    Chunk getChunk(ChunkLocation location);
}
