package org.uselesssolutions.collections;

import org.uselesssolutions.data.Chunk;

import java.util.Objects;

public class ChunkPos2D {
    public final int x;
    public final int z;

    public ChunkPos2D(int x, int z) {
        if (x < 0) {
            throw new IllegalArgumentException("X must be between in range [0, " + Chunk.CHUNK_SIZE_X + "]!");
        }
        if (x > Chunk.CHUNK_SIZE_X) {
            throw new IllegalArgumentException("X must be between in range [0, " + Chunk.CHUNK_SIZE_X + "]!");
        }
        if (z < 0) {
            throw new IllegalArgumentException("Z must be between in range [0, " + Chunk.CHUNK_SIZE_Z + "]!");
        }
        if (z > Chunk.CHUNK_SIZE_Z) {
            throw new IllegalArgumentException("Z must be between in range [0, " + Chunk.CHUNK_SIZE_Z + "]!");
        }
        this.x = x;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkPos2D that = (ChunkPos2D) o;
        return x == that.x && z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

    @Override
    public String toString() {
        return "ChunkPos3D{" +
            "x=" + x +
            ", z=" + z +
            '}';
    }
}
