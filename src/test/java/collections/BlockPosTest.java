package collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.uselesssolutions.collections.BlockPos2D;
import org.uselesssolutions.collections.BlockPos3D;

public class BlockPosTest {
    @Test
    @DisplayName("Test BlockPos2D Creation")
    void blockPos2DCreate() {
        // Test valid inputs
        final short range = 8192;
        final short targetAttempts = 128;
        final byte scalar = range/targetAttempts;
        for (int x = -range; x <= range; x += scalar) {
            for (int z = -range; z <= range; z += scalar) {
                BlockPos2D blockPos2D_1 = new BlockPos2D(x, z);
                Assertions.assertEquals(blockPos2D_1.x, x);
                Assertions.assertEquals(blockPos2D_1.z, z);

                BlockPos2D blockPos2D_2 = new BlockPos2D(x, z);
                Assertions.assertEquals(blockPos2D_2.x, x);
                Assertions.assertEquals(blockPos2D_2.z, z);

                Assertions.assertEquals(blockPos2D_1.x, blockPos2D_2.x);
                Assertions.assertEquals(blockPos2D_1.z, blockPos2D_2.z);
                Assertions.assertEquals(blockPos2D_1, blockPos2D_2);

                BlockPos2D blockPos2D_3 = new BlockPos2D(x + 1, z + 17);
                BlockPos3D blockPos3D_3 = new BlockPos3D(x + 1, 128, z + 17);
                Assertions.assertNotEquals(blockPos2D_3, blockPos2D_1);
                Assertions.assertNotEquals(blockPos3D_3, blockPos2D_1);
            }
        }
    }

    @Test
    @DisplayName("Test BlockPos3D Creation")
    void blockPos3DCreate() {
        // Test valid inputs
        final short range = 8192;
        final short targetAttempts = 128;
        final byte scalar = range/targetAttempts;
        for (int x = -range; x <= range; x += scalar) {
            for (int y = -range; y < range; y += scalar) {
                for (int z = -range; z <= range; z += scalar) {
                    BlockPos3D blockPos3D_1 = new BlockPos3D(x, y, z);
                    Assertions.assertEquals(blockPos3D_1.x, x);
                    Assertions.assertEquals(blockPos3D_1.y, y);
                    Assertions.assertEquals(blockPos3D_1.z, z);

                    BlockPos3D blockPos3D_2 = new BlockPos3D(x, y, z);
                    Assertions.assertEquals(blockPos3D_2.x, x);
                    Assertions.assertEquals(blockPos3D_2.y, y);
                    Assertions.assertEquals(blockPos3D_2.z, z);

                    Assertions.assertEquals(blockPos3D_1.x, blockPos3D_2.x);
                    Assertions.assertEquals(blockPos3D_1.y, blockPos3D_2.y);
                    Assertions.assertEquals(blockPos3D_1.z, blockPos3D_2.z);
                    Assertions.assertEquals(blockPos3D_1, blockPos3D_2);

                    BlockPos2D blockPos2D_3 = new BlockPos2D(x + 1, z + 17);
                    BlockPos3D blockPos3D_3 = new BlockPos3D(x + 1, y - 2, z + 17);
                    Assertions.assertNotEquals(blockPos3D_3, blockPos3D_1);
                    Assertions.assertNotEquals(blockPos2D_3, blockPos3D_1);
                }
            }
        }
    }
}
