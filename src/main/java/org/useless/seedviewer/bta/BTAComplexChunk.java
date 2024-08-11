package org.useless.seedviewer.bta;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.material.MaterialColor;
import org.jetbrains.annotations.NotNull;
import org.useless.seedviewer.collections.ChunkPos2D;
import org.useless.seedviewer.collections.ChunkPos3D;
import org.useless.seedviewer.data.Biome;
import org.useless.seedviewer.data.Chunk;

import java.awt.*;

public class BTAComplexChunk implements Chunk {
    private final net.minecraft.core.world.chunk.Chunk chunk;
    public BTAComplexChunk(@NotNull net.minecraft.core.world.chunk.Chunk chunk) {
        this.chunk = chunk;
    }
    @Override
    public Biome getBiome(ChunkPos3D pos) {
        return new BTABiome(chunk.getBlockBiome(pos.x, pos.y, pos.z));
    }

    @Override
    public Color getBlockColor(ChunkPos3D pos) {
        Block b = Block.getBlock(chunk.getBlockID(pos.x, pos.y, pos.z));
        if (b == null) return null;
        int meta = chunk.getBlockMetadata(pos.x, pos.y, pos.z);
        return new Color(MaterialColor.getColorFromIndex(MaterialColor.getColorIndexFromBlock(b, meta)));
    }

    @Override
    public int getHeight(ChunkPos2D pos) {
        return chunk.getHeightValue(pos.x, pos.z);
    }
}
