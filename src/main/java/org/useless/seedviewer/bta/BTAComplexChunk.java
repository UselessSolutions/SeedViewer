package org.useless.seedviewer.bta;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.material.Material;
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
    public int getBlockColor(ChunkPos3D pos) {
        Block b = Block.getBlock(chunk.getBlockID(pos.x, pos.y, pos.z));
        if (b == null) return 0;
        int meta = chunk.getBlockMetadata(pos.x, pos.y, pos.z);
        return MaterialColor.getColorFromIndex(MaterialColor.getColorIndexFromBlock(b, meta));
    }

    @Override
    public int getHeight(ChunkPos2D pos) {
        return chunk.getHeightValue(pos.x, pos.z);
    }

    @Override
    public int getWaterDepth(ChunkPos2D pos) {
        int start = getHeight(pos) - 1;
        int depth = 0;
        while (true) {
            int y = start - depth;
            if (y < 0) break;
            Block b = Block.getBlock(chunk.getBlockID(pos.x, y, pos.z));
            if (b == null) break;
            if (b.blockMaterial != Material.water) break;
            depth++;
        }
        return depth;
    }
}
