package org.useless.seedviewer.bta.worldloader;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.NbtIo;
import net.minecraft.core.world.save.LevelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.useless.seedviewer.Global;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;

public class LevelDataLoader {

    @Nullable
    public static LevelData getLevelData(@NotNull File worldDir)
    {
        Objects.requireNonNull(worldDir, "WorldDir must not be null!");
        CompoundTag tag = getLevelDataRaw(worldDir);
        if (tag == null) return null;
        return new LevelData(tag);
    }

    @Nullable
    public static CompoundTag getLevelDataRaw(@NotNull File worldDir)
    {
        Objects.requireNonNull(worldDir, "WorldDir must not be null!");
        if(!worldDir.exists())
        {
            return null;
        }
        File worldLevelDat = new File(worldDir, "level.dat");
        if(worldLevelDat.exists())
        {
            try
            {
                CompoundTag nbtRoot = NbtIo.readCompressed(Files.newInputStream(worldLevelDat.toPath()));
                return nbtRoot.getCompound("Data");
            }
            catch(Exception ex)
            {
                Global.LOGGER.error("Error parsing 'level.dat' in '{}'", worldDir, ex);
            }
        }
        worldLevelDat = new File(worldDir, "level.dat_old");
        if(worldLevelDat.exists())
        {
            try
            {
                CompoundTag oldNbtRoot = NbtIo.readCompressed(Files.newInputStream(worldLevelDat.toPath()));
                return oldNbtRoot.getCompound("Data");
            }
            catch(Exception ex)
            {
                Global.LOGGER.error("Error parsing 'level.dat_old' in '{}'", worldDir, ex);
            }
        }
        return null;
    }
}
