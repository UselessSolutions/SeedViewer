package org.useless.seedviewer.bta;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.chunk.provider.ChunkProviderStatic;
import net.minecraft.core.MinecraftAccessor;
import net.minecraft.core.entity.SkinVariantList;
import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.IChunkLoader;
import net.minecraft.core.world.chunk.provider.IChunkProvider;

import java.io.File;

public class DummyMinecraft implements MinecraftAccessor {
    public File getMinecraftDir() {
        return Minecraft.getAppDir("minecraft-bta");
    }

    public IChunkProvider createChunkProvider(World world, IChunkLoader chunkLoader) {
        return new ChunkProviderStatic(world, chunkLoader, world.getWorldType().createChunkGenerator(world));
    }

    public int getAutosaveTimer() {
        return 0;
    }

    public SkinVariantList getSkinVariantList() {
        return null;
    }

    public String getMinecraftVersion() {
        return null;
    }
}
