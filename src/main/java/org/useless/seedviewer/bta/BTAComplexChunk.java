package org.useless.seedviewer.bta;

import org.useless.seedviewer.collections.ChunkPos2D;
import org.useless.seedviewer.collections.ChunkPos3D;
import org.useless.seedviewer.data.Biome;
import org.useless.seedviewer.data.Chunk;

public class BTAComplexChunk implements Chunk {
    @Override
    public Biome getBiome(ChunkPos3D pos) {
        return null;
    }

    @Override
    public int getHeight(ChunkPos2D pos) {
        return 0;
    }
}
