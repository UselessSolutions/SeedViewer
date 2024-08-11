package org.useless.seedviewer.bta.worldloader;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.NbtIo;
import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.Chunk;
import net.minecraft.core.world.chunk.ChunkLoaderLegacy;
import net.minecraft.core.world.chunk.reader.ChunkReader;
import net.minecraft.core.world.chunk.reader.ChunkReaderLegacy;
import net.minecraft.core.world.chunk.reader.ChunkReaderVersion1;
import net.minecraft.core.world.chunk.reader.ChunkReaderVersion2;
import net.minecraft.core.world.save.mcregion.RegionFileCache;
import org.useless.seedviewer.Global;
import org.useless.seedviewer.collections.ChunkLocation;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class ChunkLoader {
    private static final World dummyWorld = new World();
    private final File dimensionDir;
    public ChunkLoader(File worldFolder, int dimension) {
        this.dimensionDir = new File(worldFolder, "dimensions/" + dimension);
    }

    public boolean chunkExists(ChunkLocation location) {
        DataInputStream regionStream = RegionFileCache.getChunkInputStream(dimensionDir, location.x, location.z);
        CompoundTag tag;
        if(regionStream != null)
        {
            try {
                tag = NbtIo.read(regionStream);
            } catch (IOException e) {
                Global.LOGGER.error("Failed to read tag for location {}, {}", location.x, location.z, e);
                return false;
            }
        }
        else
        {
            return false;
        }
        return tag.containsKey("Level");
    }

    public Chunk loadChunk(ChunkLocation location) throws IOException {
        DataInputStream regionStream = RegionFileCache.getChunkInputStream(dimensionDir, location.x, location.z);
        CompoundTag tag;
        if(regionStream != null)
        {
            tag = NbtIo.read(regionStream);
        }
        else
        {
            return null;
        }
        if(!tag.containsKey("Level"))
        {
            Global.LOGGER.error("Chunk file at {},{} is missing level data, skipping", location.x, location.z);
            return null;
        }
        return loadChunkFromCompound(tag.getCompound("Level"));
    }

    public static Chunk loadChunkFromCompound(CompoundTag tag) {
        int version = tag.getIntegerOrDefault("Version", -1);
        ChunkReader reader = getChunkReaderByVersion(tag, version);
        int x = reader.getX();
        int z = reader.getZ();
        Chunk chunk = new Chunk(dummyWorld, x, z);
        chunk.heightMap = reader.getHeightMap();
        chunk.averageBlockHeight = reader.getAverageBlockHeight();
        chunk.isTerrainPopulated = reader.getIsTerrainPopulated();
        chunk.temperature = reader.getTemperatureMap();
        chunk.humidity = reader.getHumidityMap();
        Map<Integer, String> biomeRegistry = reader.getBiomeRegistry();

        for(int i = 0; i < Chunk.CHUNK_SECTIONS; ++i) {
            ChunkLoaderLegacy.loadChunkSectionFromCompound(chunk.getSection(i), reader, biomeRegistry);
        }

        if (chunk.heightMap == null) {
            chunk.heightMap = new short[256];
            chunk.recalcHeightmap();
        }

        if (chunk.temperature == null || chunk.temperature.length == 0) {
            chunk.temperature = new double[256];
            Arrays.fill(chunk.temperature, Double.NEGATIVE_INFINITY);
        }

        if (chunk.humidity == null || chunk.humidity.length == 0) {
            chunk.humidity = new double[256];
            Arrays.fill(chunk.humidity, Double.NEGATIVE_INFINITY);
        }

        return chunk;
    }
    private static ChunkReader getChunkReaderByVersion(CompoundTag tag, int version) {
        switch (version) {
            case 1:
                return new ChunkReaderVersion1(null, tag);
            case 2:
                return new ChunkReaderVersion2(null, tag);
            default:
                return new ChunkReaderLegacy(null, tag);
        }
    }

}
