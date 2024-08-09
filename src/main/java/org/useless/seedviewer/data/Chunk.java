package org.useless.seedviewer.data;

import org.useless.seedviewer.collections.ChunkPos2D;
import org.useless.seedviewer.collections.ChunkPos3D;

public interface Chunk {
    int CHUNK_SIZE_X = 16;
    int CHUNK_SIZE_Y = 256;
    int CHUNK_SIZE_Z = 16;
    Biome getBiome(ChunkPos3D pos);
    int getHeight(ChunkPos2D pos);
}
