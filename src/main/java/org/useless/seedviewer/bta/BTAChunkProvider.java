package org.useless.seedviewer.bta;

import net.minecraft.core.Global;
import net.minecraft.core.block.Block;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.item.Item;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.world.SpawnerMobs;
import net.minecraft.core.world.World;
import net.minecraft.core.world.biome.Biomes;
import net.minecraft.core.world.biome.provider.BiomeProvider;
import net.minecraft.core.world.biome.provider.BiomeProviderOverworld;
import net.minecraft.core.world.save.ISaveFormat;
import net.minecraft.core.world.save.LevelData;
import net.minecraft.core.world.save.LevelStorage;
import net.minecraft.core.world.save.SaveFormatBase;
import net.minecraft.core.world.save.mcregion.SaveFormat19134;
import net.minecraft.core.world.type.WorldTypes;
import org.useless.seedviewer.bta.worldloader.ChunkLoader;
import org.useless.seedviewer.gui.ChunkProvider;
import org.useless.seedviewer.collections.ChunkLocation;
import org.useless.seedviewer.data.Chunk;

import java.io.File;
import java.io.IOException;

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

    ChunkLoader chunkLoader = null;
    private final BiomeProvider biomeProvider;
    public BTAChunkProvider(long seed) {
        biomeProvider = new BiomeProviderOverworld(seed, WorldTypes.OVERWORLD_EXTENDED);
    }

    public BTAChunkProvider(File worldFolder, LevelData data) {
        biomeProvider = new BiomeProviderOverworld(data.getRandomSeed(), WorldTypes.OVERWORLD_EXTENDED);
        chunkLoader = new ChunkLoader(worldFolder, 0);
    }
    @Override
    public Chunk getChunk(ChunkLocation location) {
        if (chunkLoader != null) {
            try {
                net.minecraft.core.world.chunk.Chunk c = chunkLoader.loadChunk(location);
                if (c != null) {
                    return new BTAComplexChunk(c);
                }
            } catch (IOException e) {
                org.useless.seedviewer.Global.LOGGER.error("Failed to load chunk data for chunk {}, {}", location.x, location.z, e);
            }
        }
        return new BTASimpleChunk(location, biomeProvider);
    }
}
