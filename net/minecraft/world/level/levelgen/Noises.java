/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.synth.Noise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class Noises {
    public static final ResourceKey<NormalNoise> TEMPERATURE = Noises.createKey("temperature");
    public static final ResourceKey<NormalNoise> VEGETATION = Noises.createKey("vegetation");
    public static final ResourceKey<NormalNoise> CONTINENTALNESS = Noises.createKey("continentalness");
    public static final ResourceKey<NormalNoise> EROSION = Noises.createKey("erosion");
    public static final ResourceKey<NormalNoise> TEMPERATURE_LARGE = Noises.createKey("temperature_large");
    public static final ResourceKey<NormalNoise> VEGETATION_LARGE = Noises.createKey("vegetation_large");
    public static final ResourceKey<NormalNoise> CONTINENTALNESS_LARGE = Noises.createKey("continentalness_large");
    public static final ResourceKey<NormalNoise> EROSION_LARGE = Noises.createKey("erosion_large");
    public static final ResourceKey<NormalNoise> RIDGE = Noises.createKey("ridge");
    public static final ResourceKey<NormalNoise> SHIFT = Noises.createKey("offset");
    public static final ResourceKey<NormalNoise> TEMPERATURE_NETHER = Noises.createKey("nether/temperature");
    public static final ResourceKey<NormalNoise> VEGETATION_NETHER = Noises.createKey("nether/vegetation");
    public static final ResourceKey<NormalNoise> AQUIFER_BARRIER = Noises.createKey("aquifer_barrier");
    public static final ResourceKey<NormalNoise> AQUIFER_FLUID_LEVEL_FLOODEDNESS = Noises.createKey("aquifer_fluid_level_floodedness");
    public static final ResourceKey<NormalNoise> AQUIFER_LAVA = Noises.createKey("aquifer_lava");
    public static final ResourceKey<NormalNoise> AQUIFER_FLUID_LEVEL_SPREAD = Noises.createKey("aquifer_fluid_level_spread");
    public static final ResourceKey<NormalNoise> PILLAR = Noises.createKey("pillar");
    public static final ResourceKey<NormalNoise> PILLAR_RARENESS = Noises.createKey("pillar_rareness");
    public static final ResourceKey<NormalNoise> PILLAR_THICKNESS = Noises.createKey("pillar_thickness");
    public static final ResourceKey<NormalNoise> SPAGHETTI_2D = Noises.createKey("spaghetti_2d");
    public static final ResourceKey<NormalNoise> SPAGHETTI_2D_ELEVATION = Noises.createKey("spaghetti_2d_elevation");
    public static final ResourceKey<NormalNoise> SPAGHETTI_2D_MODULATOR = Noises.createKey("spaghetti_2d_modulator");
    public static final ResourceKey<NormalNoise> SPAGHETTI_2D_THICKNESS = Noises.createKey("spaghetti_2d_thickness");
    public static final ResourceKey<NormalNoise> SPAGHETTI_3D_1 = Noises.createKey("spaghetti_3d_1");
    public static final ResourceKey<NormalNoise> SPAGHETTI_3D_2 = Noises.createKey("spaghetti_3d_2");
    public static final ResourceKey<NormalNoise> SPAGHETTI_3D_RARITY = Noises.createKey("spaghetti_3d_rarity");
    public static final ResourceKey<NormalNoise> SPAGHETTI_3D_THICKNESS = Noises.createKey("spaghetti_3d_thickness");
    public static final ResourceKey<NormalNoise> SPAGHETTI_ROUGHNESS = Noises.createKey("spaghetti_roughness");
    public static final ResourceKey<NormalNoise> SPAGHETTI_ROUGHNESS_MODULATOR = Noises.createKey("spaghetti_roughness_modulator");
    public static final ResourceKey<NormalNoise> CAVE_ENTRANCE = Noises.createKey("cave_entrance");
    public static final ResourceKey<NormalNoise> CAVE_LAYER = Noises.createKey("cave_layer");
    public static final ResourceKey<NormalNoise> CAVE_CHEESE = Noises.createKey("cave_cheese");
    public static final ResourceKey<NormalNoise> ORE_VEININESS = Noises.createKey("ore_veininess");
    public static final ResourceKey<NormalNoise> ORE_VEIN_A = Noises.createKey("ore_vein_a");
    public static final ResourceKey<NormalNoise> ORE_VEIN_B = Noises.createKey("ore_vein_b");
    public static final ResourceKey<NormalNoise> ORE_GAP = Noises.createKey("ore_gap");
    public static final ResourceKey<NormalNoise> NOODLE = Noises.createKey("noodle");
    public static final ResourceKey<NormalNoise> NOODLE_THICKNESS = Noises.createKey("noodle_thickness");
    public static final ResourceKey<NormalNoise> NOODLE_RIDGE_A = Noises.createKey("noodle_ridge_a");
    public static final ResourceKey<NormalNoise> NOODLE_RIDGE_B = Noises.createKey("noodle_ridge_b");
    public static final ResourceKey<NormalNoise> JAGGED = Noises.createKey("jagged");
    public static final ResourceKey<NormalNoise> SURFACE = Noises.createKey("surface");
    public static final ResourceKey<NormalNoise> SURFACE_SECONDARY = Noises.createKey("surface_secondary");
    public static final ResourceKey<NormalNoise> CLAY_BANDS_OFFSET = Noises.createKey("clay_bands_offset");
    public static final ResourceKey<NormalNoise> BADLANDS_PILLAR = Noises.createKey("badlands_pillar");
    public static final ResourceKey<NormalNoise> BADLANDS_PILLAR_ROOF = Noises.createKey("badlands_pillar_roof");
    public static final ResourceKey<NormalNoise> BADLANDS_SURFACE = Noises.createKey("badlands_surface");
    public static final ResourceKey<NormalNoise> ICEBERG_PILLAR = Noises.createKey("iceberg_pillar");
    public static final ResourceKey<NormalNoise> ICEBERG_PILLAR_ROOF = Noises.createKey("iceberg_pillar_roof");
    public static final ResourceKey<NormalNoise> ICEBERG_SURFACE = Noises.createKey("iceberg_surface");
    public static final ResourceKey<NormalNoise> SULFUR_CAVE_GRADIENT = Noises.createKey("sulfur_cave_gradient");
    public static final ResourceKey<NormalNoise> SWAMP = Noises.createKey("surface_swamp");
    public static final ResourceKey<NormalNoise> CALCITE = Noises.createKey("calcite");
    public static final ResourceKey<NormalNoise> GRAVEL = Noises.createKey("gravel");
    public static final ResourceKey<NormalNoise> POWDER_SNOW = Noises.createKey("powder_snow");
    public static final ResourceKey<NormalNoise> PACKED_ICE = Noises.createKey("packed_ice");
    public static final ResourceKey<NormalNoise> ICE = Noises.createKey("ice");
    public static final ResourceKey<NormalNoise> SOUL_SAND_LAYER = Noises.createKey("soul_sand_layer");
    public static final ResourceKey<NormalNoise> GRAVEL_LAYER = Noises.createKey("gravel_layer");
    public static final ResourceKey<NormalNoise> PATCH = Noises.createKey("patch");
    public static final ResourceKey<NormalNoise> SMALL_PATCH = Noises.createKey("small_patch");
    public static final ResourceKey<NormalNoise> NETHERRACK = Noises.createKey("netherrack");
    public static final ResourceKey<NormalNoise> NETHER_WART = Noises.createKey("nether_wart");
    public static final ResourceKey<NormalNoise> NETHER_STATE_SELECTOR = Noises.createKey("nether_state_selector");

    private static ResourceKey<NormalNoise> createKey(String name) {
        return ResourceKey.create(Registries.NOISE, Identifier.withDefaultNamespace(name));
    }

    public static Noise instantiate(HolderGetter<NormalNoise> noises, PositionalRandomFactory context, ResourceKey<NormalNoise> name) {
        Holder.Reference<NormalNoise> holder = noises.getOrThrow(name);
        return ((NormalNoise)holder.value()).create(context.fromHashOf(name.identifier()));
    }
}

