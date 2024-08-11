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
import org.useless.seedviewer.gui.ChunkProvider;
import org.useless.seedviewer.collections.ChunkLocation;
import org.useless.seedviewer.data.Chunk;

import java.io.File;

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
    public static File worldDir = new File(Global.accessor.getMinecraftDir(), "saves");
    World world = null;
    BiomeProvider biomeProvider = null;
    public BTAChunkProvider(long seed) {
        biomeProvider = new BiomeProviderOverworld(seed, WorldTypes.OVERWORLD_EXTENDED);
    }

    public BTAChunkProvider(File worldFolder, LevelData data) {
        SaveFormatBase saveFormat = new SaveFormat19134(worldFolder.getParentFile());
        LevelStorage saveHandler = saveFormat.getSaveHandler(worldFolder.getName(), false);
        World world = new World(saveHandler, saveHandler.getLevelData().getWorldName(), saveHandler.getLevelData().getRandomSeed(), null, null);
        biomeProvider = world.worldType.createBiomeProvider(world);
    }
    @Override
    public Chunk getChunk(ChunkLocation location) {
        return new BTASimpleChunk(location, biomeProvider);
    }
}
