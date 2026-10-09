/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.CuboidPlacement;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountOnEveryLayerPlacement;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
import net.minecraft.world.level.levelgen.placement.FixedPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.NoiseBasedCountPlacement;
import net.minecraft.world.level.levelgen.placement.NoiseThresholdCountPlacement;
import net.minecraft.world.level.levelgen.placement.OffsetPlacement;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RandomChancePlacement;
import net.minecraft.world.level.levelgen.placement.RandomlySelectedPlacement;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceRelativeThresholdFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter;

public interface PlacementModifierTypes {
    public static MapCodec<? extends PlacementModifier> bootstrap(Registry<MapCodec<? extends PlacementModifier>> registry) {
        Registry.register(registry, "block_predicate_filter", BlockPredicateFilter.CODEC);
        Registry.register(registry, "rarity_filter", RarityFilter.CODEC);
        Registry.register(registry, "random_chance", RandomChancePlacement.CODEC);
        Registry.register(registry, "surface_relative_threshold_filter", SurfaceRelativeThresholdFilter.CODEC);
        Registry.register(registry, "surface_water_depth_filter", SurfaceWaterDepthFilter.CODEC);
        Registry.register(registry, "biome", BiomeFilter.CODEC);
        Registry.register(registry, "count", CountPlacement.CODEC);
        Registry.register(registry, "noise_based_count", NoiseBasedCountPlacement.CODEC);
        Registry.register(registry, "noise_threshold_count", NoiseThresholdCountPlacement.CODEC);
        Registry.register(registry, "count_on_every_layer", CountOnEveryLayerPlacement.CODEC);
        Registry.register(registry, "cuboid", CuboidPlacement.CODEC);
        Registry.register(registry, "environment_scan", EnvironmentScanPlacement.CODEC);
        Registry.register(registry, "heightmap", HeightmapPlacement.CODEC);
        Registry.register(registry, "height_range", HeightRangePlacement.CODEC);
        Registry.register(registry, "in_square", InSquarePlacement.CODEC);
        Registry.register(registry, "offset", OffsetPlacement.CODEC);
        Registry.register(registry, "randomly_selected", RandomlySelectedPlacement.CODEC);
        return Registry.register(registry, "fixed_placement", FixedPlacement.CODEC);
    }
}

