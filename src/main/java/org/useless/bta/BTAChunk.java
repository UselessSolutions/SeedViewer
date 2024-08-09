package org.useless.bta;

import net.minecraft.core.world.biome.provider.BiomeProvider;
import org.useless.collections.ChunkLocation;
import org.useless.collections.ChunkPos2D;
import org.useless.collections.ChunkPos3D;
import org.useless.data.Biome;
import org.useless.data.Chunk;

public class BTAChunk implements Chunk {
    private final ChunkLocation location;
    private final BiomeProvider provider;
    public BTAChunk(ChunkLocation location, BiomeProvider biomeProvider) {
        this.location = location;
        provider = biomeProvider;
    }
    @Override
    public Biome getBiome(ChunkPos3D pos) {
        return new BTABiome(provider.getBiome(location.x * Chunk.CHUNK_SIZE_X + pos.x, pos.y, location.z * Chunk.CHUNK_SIZE_Z + pos.z));
    }

    @Override
    public int getHeight(ChunkPos2D pos) {
        return 128;
    }
}
