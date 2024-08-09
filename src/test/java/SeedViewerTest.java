import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.useless.SeedViewer;
import org.useless.collections.ChunkLocation;

public class SeedViewerTest {

    @Test
    @DisplayName("Test for Slime Chunks")
    void slimeChunkTest() {
        {
            long seed = 100;
            Assertions.assertAll(
                () -> Assertions.assertTrue(SeedViewer.isSlimeChunk(seed, new ChunkLocation(3, -4))),
                () -> Assertions.assertTrue(SeedViewer.isSlimeChunk(seed, new ChunkLocation(8, -7))),
                () -> Assertions.assertTrue(SeedViewer.isSlimeChunk(seed, new ChunkLocation(15, 0))));
            Assertions.assertAll(
                () -> Assertions.assertFalse(SeedViewer.isSlimeChunk(seed, new ChunkLocation(11, -5))),
                () -> Assertions.assertFalse(SeedViewer.isSlimeChunk(seed, new ChunkLocation(12, -8))),
                () -> Assertions.assertFalse(SeedViewer.isSlimeChunk(seed, new ChunkLocation(12, -11))),
                () -> Assertions.assertFalse(SeedViewer.isSlimeChunk(seed, new ChunkLocation(0, -17))),
                () -> Assertions.assertFalse(SeedViewer.isSlimeChunk(seed, new ChunkLocation(-4, -11))));
        }
        {
            long seed = 10000;
            Assertions.assertAll(
                () -> Assertions.assertTrue(SeedViewer.isSlimeChunk(seed, new ChunkLocation(2, 0))),
                () -> Assertions.assertTrue(SeedViewer.isSlimeChunk(seed, new ChunkLocation(6, -4))),
                () -> Assertions.assertTrue(SeedViewer.isSlimeChunk(seed, new ChunkLocation(0, -6))),
                () -> Assertions.assertTrue(SeedViewer.isSlimeChunk(seed, new ChunkLocation(5, 3))),
                () -> Assertions.assertTrue(SeedViewer.isSlimeChunk(seed, new ChunkLocation(7, 7))),
                () -> Assertions.assertTrue(SeedViewer.isSlimeChunk(seed, new ChunkLocation(6, 7))),
                () -> Assertions.assertTrue(SeedViewer.isSlimeChunk(seed, new ChunkLocation(3, 11))),
                () -> Assertions.assertTrue(SeedViewer.isSlimeChunk(seed, new ChunkLocation(8, 13))),
                () -> Assertions.assertTrue(SeedViewer.isSlimeChunk(seed, new ChunkLocation(10, 3))));
            Assertions.assertAll(
                () -> Assertions.assertFalse(SeedViewer.isSlimeChunk(seed, new ChunkLocation(10, -1))),
                () -> Assertions.assertFalse(SeedViewer.isSlimeChunk(seed, new ChunkLocation(12, -4))),
                () -> Assertions.assertFalse(SeedViewer.isSlimeChunk(seed, new ChunkLocation(8, -10))),
                () -> Assertions.assertFalse(SeedViewer.isSlimeChunk(seed, new ChunkLocation(5, -14))),
                () -> Assertions.assertFalse(SeedViewer.isSlimeChunk(seed, new ChunkLocation(0, -11))));
        }

    }
}
