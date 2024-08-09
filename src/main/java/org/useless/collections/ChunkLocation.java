package org.useless.collections;

import java.util.Objects;

/**
 * Chunk coordinates of a chunk in the world
 */
public final class ChunkLocation {
    public final int x;
    public final int z;

    public ChunkLocation(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkLocation that = (ChunkLocation) o;
        return x == that.x && z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

    @Override
    public String toString() {
        return "ChunkLocation{" +
            "x=" + x +
            ", z=" + z +
            '}';
    }
}
