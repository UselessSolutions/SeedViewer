package collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.useless.seedviewer.collections.ChunkPos2D;
import org.useless.seedviewer.collections.ChunkPos3D;
import org.useless.seedviewer.data.Chunk;

public class ChunkPosTest {
    @Test
    @DisplayName("Test Valid ChunkPos2D")
    void validChunkPos2D() {
        for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                ChunkPos2D chunkPos2D_1 = new ChunkPos2D(x, z);
                Assertions.assertEquals(chunkPos2D_1.x, x);
                Assertions.assertEquals(chunkPos2D_1.z, z);

                ChunkPos2D chunkPos2D_2 = new ChunkPos2D(x, z);
                Assertions.assertEquals(chunkPos2D_2.x, x);
                Assertions.assertEquals(chunkPos2D_2.z, z);

                Assertions.assertEquals(chunkPos2D_1.x, chunkPos2D_2.x);
                Assertions.assertEquals(chunkPos2D_1.z, chunkPos2D_2.z);
                Assertions.assertEquals(chunkPos2D_1, chunkPos2D_2);
            }
        }
    }

    @Test
    @DisplayName("Test Invalid ChunkPos2D")
    void invalidChunkPos2D() {
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> new ChunkPos2D(-10, -10));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> new ChunkPos2D(0, -10));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> new ChunkPos2D(-10, 0));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> new ChunkPos2D(20, 0));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> new ChunkPos2D(0, 20));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> new ChunkPos2D(20, 20));
    }

    @Test
    @DisplayName("Test Valid ChunkPos3D")
    void validChunkPos3D() {
        for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
            for (int y = 0; y < Chunk.CHUNK_SIZE_Y; y++) {
                for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                    ChunkPos3D chunkPos3D_1 = new ChunkPos3D(x, y, z);
                    Assertions.assertEquals(chunkPos3D_1.x, x);
                    Assertions.assertEquals(chunkPos3D_1.y, y);
                    Assertions.assertEquals(chunkPos3D_1.z, z);

                    ChunkPos3D chunkPos3D_2 = new ChunkPos3D(x, y, z);
                    Assertions.assertEquals(chunkPos3D_2.x, x);
                    Assertions.assertEquals(chunkPos3D_2.y, y);
                    Assertions.assertEquals(chunkPos3D_2.z, z);

                    Assertions.assertEquals(chunkPos3D_1.x, chunkPos3D_2.x);
                    Assertions.assertEquals(chunkPos3D_1.y, chunkPos3D_2.y);
                    Assertions.assertEquals(chunkPos3D_1.z, chunkPos3D_2.z);
                    Assertions.assertEquals(chunkPos3D_1, chunkPos3D_2);
                }
            }
        }
    }

    @Test
    @DisplayName("Test Invalid ChunkPos3D")
    void invalidChunkPos3D() {
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> new ChunkPos3D(-10, -10, -10));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> new ChunkPos3D(0, -10, -10));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> new ChunkPos3D(0, 0, -10));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> new ChunkPos3D(-10, 0, -10));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> new ChunkPos3D(-10, 0, 0));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> new ChunkPos3D(20, 20, 20));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> new ChunkPos3D(0, 20, 20));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> new ChunkPos3D(0, 0, 20));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> new ChunkPos3D(20, 0, 20));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> new ChunkPos3D(20, 0, 0));
    }
}
