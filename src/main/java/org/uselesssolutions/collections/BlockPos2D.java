package org.uselesssolutions.collections;

import java.util.Objects;

public class BlockPos2D {
    public final int x;
    public final int z;

    public BlockPos2D(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockPos2D blockPos2D = (BlockPos2D) o;
        return x == blockPos2D.x && z == blockPos2D.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

    @Override
    public String toString() {
        return "BlockPos3D{" +
            "x=" + x +
            ", z=" + z +
            '}';
    }
}
