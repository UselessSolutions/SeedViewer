package org.useless.seedviewer.bta;

import net.minecraft.core.world.biome.provider.BiomeProvider;
import org.useless.seedviewer.collections.ChunkLocation;
import org.useless.seedviewer.collections.ChunkPos2D;
import org.useless.seedviewer.collections.ChunkPos3D;
import org.useless.seedviewer.data.Biome;
import org.useless.seedviewer.data.Chunk;

import java.awt.*;

public class BTASimpleChunk implements Chunk {
    private final ChunkLocation location;
    private final BiomeProvider provider;
    public BTASimpleChunk(ChunkLocation location, BiomeProvider biomeProvider) {
        this.location = location;
        provider = biomeProvider;
    }
    @Override
    public Biome getBiome(ChunkPos3D pos) {
        return new BTABiome(provider.getBiome(location.x * Chunk.CHUNK_SIZE_X + pos.x, pos.y, location.z * Chunk.CHUNK_SIZE_Z + pos.z));
    }

    @Override
    public int getBlockColor(ChunkPos3D pos) {
        return 0;
    }

    @Override
    public int getHeight(ChunkPos2D pos) {
        return 128;
    }

    @Override
    public int getWaterDepth(ChunkPos2D pos) {
        return 0;
    }
}
