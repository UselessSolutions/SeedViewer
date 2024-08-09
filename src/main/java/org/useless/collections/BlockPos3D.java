package org.useless.collections;

import java.util.Objects;

/**
 * Block coordinates of a position relative to the world
 */
public final class BlockPos3D extends BlockPos2D {
    public final int y;

    public BlockPos3D(int x, int y, int z) {
        super(x, z);
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockPos3D blockPos3D = (BlockPos3D) o;
        return x == blockPos3D.x && y == blockPos3D.y && z == blockPos3D.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "BlockPos3D{" +
            "x=" + x +
            ", y=" + y +
            ", z=" + z +
            '}';
    }
}
