package org.uselesssolutions.bta;

import net.minecraft.core.data.registry.Registries;
import org.uselesssolutions.collections.NamespaceID;
import org.uselesssolutions.data.Biome;

public class BTABiome implements Biome {
    private final net.minecraft.core.world.biome.Biome biome;
    public BTABiome(net.minecraft.core.world.biome.Biome biome) {
        this.biome = biome;
    }
    @Override
    public String getName() {
        return biome.translationKey;
    }

    @Override
    public NamespaceID getID() {
        return new NamespaceID(Registries.BIOMES.getKey(biome));
    }

    @Override
    public int getColor() {
        return biome.color;
    }
}
