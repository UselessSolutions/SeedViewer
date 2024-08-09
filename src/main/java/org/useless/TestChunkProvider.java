package org.useless;

import org.useless.collections.ChunkLocation;
import org.useless.collections.ChunkPos2D;
import org.useless.collections.ChunkPos3D;
import org.useless.collections.NamespaceID;
import org.useless.data.Biome;
import org.useless.data.Chunk;

public class TestChunkProvider implements ChunkProvider{
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
            public int getHeight(ChunkPos2D pos) {
                return 128;
            }
        };
    }
}