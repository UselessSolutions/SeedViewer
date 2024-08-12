package org.useless.seedviewer;

import org.useless.seedviewer.collections.ChunkLocation;
import org.useless.seedviewer.collections.ChunkPos2D;
import org.useless.seedviewer.collections.ChunkPos3D;
import org.useless.seedviewer.collections.NamespaceID;
import org.useless.seedviewer.data.Biome;
import org.useless.seedviewer.data.Chunk;
import org.useless.seedviewer.gui.ChunkProvider;

import java.awt.*;

public class TestChunkProvider implements ChunkProvider {
    @Override
    public Chunk getChunk(ChunkLocation location) {
        return new Chunk() {
            @Override
            public Biome getBiome(ChunkPos3D pos) {
                return new Biome() {
                    @Override
                    public String getName() {
                        return "Test Biome";
                    }

                    @Override
                    public NamespaceID getID() {
                        return new NamespaceID("viewer:test");
                    }

                    @Override
                    public int getColor() {
                        int r = (pos.x << 3) & 0xFF;
                        int g = (pos.y) & 0xFF;
                        int b = (pos.z << 3) & 0xFF;
                        return r << 16 | g << 8 | b;
                    }
                };
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
        };
    }
}
