package org.useless.seedviewer.bta;

import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.lang.I18n;
import org.useless.seedviewer.collections.NamespaceID;
import org.useless.seedviewer.data.Biome;

public class BTABiome implements Biome {
    private final net.minecraft.core.world.biome.Biome biome;
    public BTABiome(net.minecraft.core.world.biome.Biome biome) {
        this.biome = biome;
    }
    @Override
    public String getName() {
        if (biome == null) return "Unknown";
        return I18n.getInstance().translateKey(biome.translationKey);
    }

    @Override
    public NamespaceID getID() {
        return new NamespaceID(Registries.BIOMES.getKey(biome));
    }

    @Override
    public int getColor() {
        if (biome == null) return 0x00000000;
        return biome.color;
    }
}
