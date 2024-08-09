package org.useless.collections;

import org.useless.data.Chunk;

import java.util.Objects;

/**
 * Block coordinates of a position relative to a chunk
 */
public final class ChunkPos3D extends ChunkPos2D {
    public final int y;

    public ChunkPos3D(int x, int y, int z) {
        super(x, z);
        if (y < 0) {
            throw new IllegalArgumentException("Y must be between in range [0, " + Chunk.CHUNK_SIZE_Y + "]!");
        }
        if (y > Chunk.CHUNK_SIZE_Y) {
            throw new IllegalArgumentException("Y must be between in range [0, " + Chunk.CHUNK_SIZE_Y + "]!");
        }
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkPos3D that = (ChunkPos3D) o;
        return x == that.x && y == that.y && z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "ChunkPos3D{" +
            "x=" + x +
            ", y=" + y +
            ", z=" + z +
            '}';
    }
}
