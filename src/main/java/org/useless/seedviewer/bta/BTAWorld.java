package org.useless.seedviewer.bta;

import net.minecraft.core.world.save.LevelData;
import org.jetbrains.annotations.NotNull;
import org.useless.seedviewer.bta.worldloader.LevelDataLoader;
import org.useless.seedviewer.data.World;
import org.useless.seedviewer.gui.ChunkProvider;

import java.io.File;
import java.util.Objects;

public class BTAWorld implements World {
    private final @NotNull File worldFolder;
    private final LevelData data;
    public BTAWorld(@NotNull File worldFolder) {
        this.worldFolder = worldFolder;
        this.data = Objects.requireNonNull(LevelDataLoader.getLevelData(worldFolder), "Failed to get level data from world '" + worldFolder + "'");
    }
    @Override
    public File getFolder() {
        return worldFolder;
    }

    @Override
    public long getSeed() {
        return data.getRandomSeed();
    }

    @Override
    public String getName() {
        return data.getWorldName();
    }

    @Override
    public long getLastPlayed() {
        return data.getLastTimePlayed();
    }

    @Override
    public ChunkProvider getChunkProvider() {
        return new BTAChunkProvider(worldFolder, data);
    }
}
