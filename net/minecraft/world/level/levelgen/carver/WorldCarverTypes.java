/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.carver.CanyonWorldCarver;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

public interface WorldCarverTypes {
    public static MapCodec<? extends WorldCarver> bootstrap(Registry<MapCodec<? extends WorldCarver>> registry) {
        Registry.register(registry, "cave", CaveWorldCarver.MAP_CODEC);
        return Registry.register(registry, "canyon", CanyonWorldCarver.MAP_CODEC);
    }
}

