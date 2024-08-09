package collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.useless.collections.ChunkLocation;

public class ChunkLocationTest {
    @Test
    @DisplayName("Test ChunkLocation Creation")
    void blockPos2DCreate() {
        // Test valid inputs
        final short range = 8192;
        final short targetAttempts = 128;
        final byte scalar = range/targetAttempts;
        for (int x = -range; x <= range; x += scalar) {
            for (int z = -range; z <= range; z += scalar) {
                ChunkLocation chunkLocation_1 = new ChunkLocation(x, z);
                Assertions.assertEquals(chunkLocation_1.x, x);
                Assertions.assertEquals(chunkLocation_1.z, z);

                ChunkLocation chunkLocation_2 = new ChunkLocation(x, z);
                Assertions.assertEquals(chunkLocation_2.x, x);
                Assertions.assertEquals(chunkLocation_2.z, z);

                Assertions.assertEquals(chunkLocation_1.x, chunkLocation_2.x);
                Assertions.assertEquals(chunkLocation_1.z, chunkLocation_2.z);
                Assertions.assertEquals(chunkLocation_1, chunkLocation_2);

                ChunkLocation chunkLocation_3 = new ChunkLocation(x + 1, z + 17);
                Assertions.assertNotEquals(chunkLocation_3, chunkLocation_1);
            }
        }
    }
}
