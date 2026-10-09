/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 */
package net.minecraft.world.level.biome;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;

@FunctionalInterface
public interface BiomeResolver {
    public Holder<Biome> getNoiseBiome(int var1, int var2, int var3);

    default public Set<Holder<Biome>> getBiomesWithin(int x, int y, int z, int radius) {
        int x0 = QuartPos.fromBlock(x - radius);
        int y0 = QuartPos.fromBlock(y - radius);
        int z0 = QuartPos.fromBlock(z - radius);
        int x1 = QuartPos.fromBlock(x + radius);
        int y1 = QuartPos.fromBlock(y + radius);
        int z1 = QuartPos.fromBlock(z + radius);
        ObjectOpenHashSet biomeSet = new ObjectOpenHashSet();
        for (int noiseZ = z0; noiseZ <= z1; ++noiseZ) {
            for (int noiseX = x0; noiseX <= x1; ++noiseX) {
                for (int noiseY = y0; noiseY <= y1; ++noiseY) {
                    biomeSet.add(this.getNoiseBiome(noiseX, noiseY, noiseZ));
                }
            }
        }
        return biomeSet;
    }
}

