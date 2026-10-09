/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.CopyPropertiesProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.DualNoiseProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseThresholdProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RandomBlockProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RandomizedIntStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RotatedBlockProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;

public interface BlockStateProviderTypes {
    public static MapCodec<? extends BlockStateProvider> bootstrap(Registry<MapCodec<? extends BlockStateProvider>> registry) {
        Registry.register(registry, "copy_properties", CopyPropertiesProvider.CODEC);
        Registry.register(registry, "dual_noise", DualNoiseProvider.CODEC);
        Registry.register(registry, "noise", NoiseProvider.CODEC);
        Registry.register(registry, "noise_threshold", NoiseThresholdProvider.CODEC);
        Registry.register(registry, "random_block", RandomBlockProvider.CODEC);
        Registry.register(registry, "randomized_int", RandomizedIntStateProvider.CODEC);
        Registry.register(registry, "rotated", RotatedBlockProvider.CODEC);
        Registry.register(registry, "rule_based", RuleBasedStateProvider.CODEC);
        Registry.register(registry, "simple", SimpleStateProvider.CODEC);
        return Registry.register(registry, "weighted", WeightedStateProvider.CODEC);
    }
}

