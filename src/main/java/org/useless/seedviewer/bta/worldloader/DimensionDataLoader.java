package org.useless.seedviewer.bta.worldloader;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.NbtIo;
import net.minecraft.core.world.save.DimensionData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.useless.seedviewer.Global;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;

public class DimensionDataLoader {
    @Nullable
    public static DimensionData getDimensionData(@NotNull File worldDir, int dimensionId) {
        Objects.requireNonNull(worldDir, "WorldDir must not be null!");
        CompoundTag tag = getDimensionDataRaw(worldDir, dimensionId);
        if (tag == null) return null;
        return new DimensionData(tag);
    }

    @Nullable
    public static CompoundTag getDimensionDataRaw(@NotNull File worldDir, int dimensionId)
    {
        if(!worldDir.exists())
        {
            return null;
        }
        File dimensionsDir = new File(worldDir, "dimensions");
        File dimensionDir = new File(dimensionsDir, "" + dimensionId);
        File dimensionDat = new File(dimensionDir, "dimension.dat");
        if (dimensionDat.exists())
        {
            try
            {
                CompoundTag nbtRoot = NbtIo.readCompressed(Files.newInputStream(dimensionDat.toPath()));
                return nbtRoot.getCompound("Data");
            }
            catch (Exception ex)
            {
                Global.LOGGER.error("Error parsing 'dimension.dat' in '{}' dimension {}", worldDir, dimensionId, ex);
            }
        }
        dimensionDat = new File(dimensionDir, "dimension.dat_old");
        if (dimensionDat.exists())
        {
            try
            {
                CompoundTag oldNbtRoot = NbtIo.readCompressed(Files.newInputStream(dimensionDat.toPath()));
                return oldNbtRoot.getCompound("Data");
            }
            catch (Exception ex)
            {
                Global.LOGGER.error("Error parsing 'dimension.dat_old' in '{}' dimension {}", worldDir, dimensionId, ex);
            }
        }

        return null;
    }
}
