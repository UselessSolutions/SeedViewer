package org.uselesssolutions.bta;

import com.sun.imageio.plugins.common.I18N;
import net.minecraft.core.Global;
import net.minecraft.core.block.Block;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.item.Item;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.world.biome.Biomes;
import net.minecraft.core.world.biome.provider.BiomeProvider;
import net.minecraft.core.world.biome.provider.BiomeProviderOverworld;
import net.minecraft.core.world.type.WorldTypes;
import org.uselesssolutions.ChunkProvider;
import org.uselesssolutions.collections.ChunkLocation;
import org.uselesssolutions.data.Chunk;

public class BTAChunkProvider implements ChunkProvider {
    static {
        Global.accessor = new DummyMinecraft();
        Block.initializeBlocks();
        Item.initializeItems();
        new Registries();
        Biomes.init();
        WorldTypes.init();
        BiomeProviderOverworld.init();
        I18n.initialize("en_US");
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
