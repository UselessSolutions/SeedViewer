package org.uselesssolutions.data;

import org.uselesssolutions.collections.ChunkPos2D;
import org.uselesssolutions.collections.ChunkPos3D;

public interface Chunk {
    int CHUNK_SIZE_X = 16;
    int CHUNK_SIZE_Y = 256;
    int CHUNK_SIZE_Z = 16;
    Biome getBiome(ChunkPos3D pos);
    int getHeight(ChunkPos2D pos);
}
