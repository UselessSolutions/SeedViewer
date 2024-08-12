package org.useless.seedviewer.data;

import net.minecraft.core.world.chunk.ChunkSection;
import org.useless.seedviewer.collections.ChunkPos2D;
import org.useless.seedviewer.collections.ChunkPos3D;

import java.awt.*;

public interface Chunk {
    int CHUNK_SIZE_X = net.minecraft.core.world.chunk.Chunk.CHUNK_SIZE_X;
    int CHUNK_SIZE_Y = net.minecraft.core.world.chunk.Chunk.CHUNK_SECTIONS * ChunkSection.SECTION_SIZE_Y;
    int CHUNK_SIZE_Z = net.minecraft.core.world.chunk.Chunk.CHUNK_SIZE_Z;
    Biome getBiome(ChunkPos3D pos);
    Color getBlockColor(ChunkPos3D pos);
    int getHeight(ChunkPos2D pos);
    int getWaterDepth(ChunkPos2D pos);
}
