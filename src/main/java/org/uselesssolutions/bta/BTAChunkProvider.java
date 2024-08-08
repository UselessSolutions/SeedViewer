package org.uselesssolutions.bta;

import net.minecraft.core.block.Block;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.item.Item;
import net.minecraft.core.world.biome.Biomes;
import net.minecraft.core.world.biome.provider.BiomeProvider;
import net.minecraft.core.world.biome.provider.BiomeProviderOverworld;
import net.minecraft.core.world.type.WorldTypes;
import org.uselesssolutions.ChunkProvider;
import org.uselesssolutions.collections.ChunkLocation;
import org.uselesssolutions.data.Chunk;

public class BTAChunkProvider implements ChunkProvider {
    static {
        Block.initializeBlocks();
        Item.initializeItems();
        new Registries();
        Biomes.init();
        WorldTypes.init();
        BiomeProviderOverworld.init();
    }
    BiomeProvider biomeProvider;
    public BTAChunkProvider(long seed) {
        biomeProvider = new BiomeProviderOverworld(seed, WorldTypes.OVERWORLD_EXTENDED);
    }
    @Override
    public Chunk getChunk(ChunkLocation location) {
        return new BTAChunk(location, biomeProvider);
    }
}
