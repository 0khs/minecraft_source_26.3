/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.floats.FloatList
 */
package net.minecraft.world.level.levelgen;

import it.unimi.dsi.fastutil.floats.FloatList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.TerrainProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.OverworldFunctionSet;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctions;
import net.minecraft.world.level.levelgen.densityfunction.DistanceMetric;
import net.minecraft.world.level.levelgen.densityfunction.op.SplineFunction;
import net.minecraft.world.level.levelgen.material.rule.OreVeinRule;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseRouterData {
    public static final float GLOBAL_OFFSET = -0.50375f;
    private static final float ORE_THICKNESS = 0.08f;
    private static final float VEININESS_FREQUENCY = 1.5f;
    private static final float NOODLE_SPACING_AND_STRAIGHTNESS = 1.5f;
    private static final float SURFACE_DENSITY_THRESHOLD = 1.5625f;
    private static final float CHEESE_NOISE_TARGET = -0.703125f;
    public static final float NOISE_ZERO = 0.390625f;
    public static final int ISLAND_CHUNK_DISTANCE = 64;
    public static final long ISLAND_CHUNK_DISTANCE_SQR = 4096L;
    private static final int DENSITY_Y_ANCHOR_BOTTOM = -64;
    private static final int DENSITY_Y_ANCHOR_TOP = 320;
    private static final float DENSITY_Y_BOTTOM = 1.5f;
    private static final float DENSITY_Y_TOP = -1.5f;
    private static final int OVERWORLD_BOTTOM_SLIDE_HEIGHT = 24;
    private static final float BASE_DENSITY_MULTIPLIER = 4.0f;
    private static final DensityFunction BLENDING_FACTOR = DensityFunctions.constant(10.0f);
    private static final DensityFunction BLENDING_JAGGEDNESS = DensityFunctions.zero();
    private static final ResourceKey<DensityFunction> ZERO = NoiseRouterData.createKey("zero");
    private static final ResourceKey<DensityFunction> Y = NoiseRouterData.createKey("y");
    private static final ResourceKey<DensityFunction> SHIFT_X = NoiseRouterData.createKey("shift_x");
    private static final ResourceKey<DensityFunction> SHIFT_Z = NoiseRouterData.createKey("shift_z");
    private static final ResourceKey<DensityFunction> BASE_3D_NOISE_OVERWORLD = NoiseRouterData.createKey("overworld/base_3d_noise");
    private static final ResourceKey<DensityFunction> BASE_3D_NOISE_NETHER = NoiseRouterData.createKey("nether/base_3d_noise");
    private static final ResourceKey<DensityFunction> BASE_3D_NOISE_END = NoiseRouterData.createKey("end/base_3d_noise");
    public static final ResourceKey<DensityFunction> RIDGES = NoiseRouterData.createKey("overworld/ridges");
    public static final ResourceKey<DensityFunction> RIDGES_FOLDED = NoiseRouterData.createKey("overworld/ridges_folded");
    public static final OverworldFunctionSet<ResourceKey<DensityFunction>> OVERWORLD_FUNCTIONS = new OverworldFunctionSet<ResourceKey<DensityFunction>>(NoiseRouterData.createKey("overworld/temperature"), NoiseRouterData.createKey("overworld/vegetation"), NoiseRouterData.createKey("overworld/continents"), NoiseRouterData.createKey("overworld/erosion"), NoiseRouterData.createKey("overworld/offset"), NoiseRouterData.createKey("overworld/factor"), NoiseRouterData.createKey("overworld/jaggedness"), NoiseRouterData.createKey("overworld/depth"), NoiseRouterData.createKey("overworld/sloped_cheese"), NoiseRouterData.createKey("overworld/preliminary_surface_level"), NoiseRouterData.createKey("overworld/chunk_surface_level"), NoiseRouterData.createKey("overworld/final_density"));
    public static final OverworldFunctionSet<ResourceKey<DensityFunction>> AMPLIFIED_OVERWORLD_FUNCTIONS = new OverworldFunctionSet<ResourceKey<DensityFunction>>(OVERWORLD_FUNCTIONS.temperature(), OVERWORLD_FUNCTIONS.vegetation(), OVERWORLD_FUNCTIONS.continents(), OVERWORLD_FUNCTIONS.erosion(), NoiseRouterData.createKey("overworld_amplified/offset"), NoiseRouterData.createKey("overworld_amplified/factor"), NoiseRouterData.createKey("overworld_amplified/jaggedness"), NoiseRouterData.createKey("overworld_amplified/depth"), NoiseRouterData.createKey("overworld_amplified/sloped_cheese"), NoiseRouterData.createKey("overworld_amplified/preliminary_surface_level"), NoiseRouterData.createKey("overworld_amplified/chunk_surface_level"), NoiseRouterData.createKey("overworld_amplified/final_density"));
    public static final OverworldFunctionSet<ResourceKey<DensityFunction>> LARGE_OVERWORLD_FUNCTIONS = new OverworldFunctionSet<ResourceKey<DensityFunction>>(NoiseRouterData.createKey("overworld_large_biomes/temperature"), NoiseRouterData.createKey("overworld_large_biomes/vegetation"), NoiseRouterData.createKey("overworld_large_biomes/continents"), NoiseRouterData.createKey("overworld_large_biomes/erosion"), NoiseRouterData.createKey("overworld_large_biomes/offset"), NoiseRouterData.createKey("overworld_large_biomes/factor"), NoiseRouterData.createKey("overworld_large_biomes/jaggedness"), NoiseRouterData.createKey("overworld_large_biomes/depth"), NoiseRouterData.createKey("overworld_large_biomes/sloped_cheese"), NoiseRouterData.createKey("overworld_large_biomes/preliminary_surface_level"), NoiseRouterData.createKey("overworld_large_biomes/chunk_surface_level"), NoiseRouterData.createKey("overworld_large_biomes/final_density"));
    private static final ResourceKey<DensityFunction> END_ISLANDS = NoiseRouterData.createKey("end/islands");
    private static final ResourceKey<DensityFunction> SLOPED_CHEESE_END = NoiseRouterData.createKey("end/sloped_cheese");
    private static final ResourceKey<DensityFunction> SPAGHETTI_ROUGHNESS_FUNCTION = NoiseRouterData.createKey("overworld/caves/spaghetti_roughness_function");
    private static final ResourceKey<DensityFunction> ENTRANCES = NoiseRouterData.createKey("overworld/caves/entrances");
    private static final ResourceKey<DensityFunction> NOODLE = NoiseRouterData.createKey("overworld/caves/noodle");
    private static final ResourceKey<DensityFunction> PILLARS = NoiseRouterData.createKey("overworld/caves/pillars");
    private static final ResourceKey<DensityFunction> SPAGHETTI_2D_THICKNESS_MODULATOR = NoiseRouterData.createKey("overworld/caves/spaghetti_2d_thickness_modulator");
    private static final ResourceKey<DensityFunction> SPAGHETTI_2D = NoiseRouterData.createKey("overworld/caves/spaghetti_2d");
    private static final ResourceKey<DensityFunction> ORE_VEIN_MASK = NoiseRouterData.createKey("overworld/ore_vein/mask");
    private static final ResourceKey<DensityFunction> ORE_VEIN_TOGGLE = NoiseRouterData.createKey("overworld/ore_vein/toggle");
    public static final ResourceKey<DensityFunction> ORE_VEIN_RICHNESS = NoiseRouterData.createKey("overworld/ore_vein/richness");
    public static final ResourceKey<DensityFunction> ORE_VEIN_COPPER_DENSITY = NoiseRouterData.createKey("overworld/ore_vein/copper_density");
    public static final ResourceKey<DensityFunction> ORE_VEIN_IRON_DENSITY = NoiseRouterData.createKey("overworld/ore_vein/iron_density");
    public static final ResourceKey<DensityFunction> ORE_VEIN_GAP = NoiseRouterData.createKey("overworld/ore_vein/gap");

    private static ResourceKey<DensityFunction> createKey(String name) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, Identifier.withDefaultNamespace(name));
    }

    public static void bootstrap(BootstrapContext<DensityFunction> context) {
        HolderGetter<NormalNoise> noises = context.lookup(Registries.NOISE);
        HolderGetter<DensityFunction> functions = context.lookup(Registries.DENSITY_FUNCTION);
        context.register(ZERO, DensityFunctions.zero());
        int belowBottom = DimensionType.MIN_Y * 2;
        int aboveTop = DimensionType.MAX_Y * 2;
        context.register(Y, DensityFunctions.yClampedGradient(belowBottom, aboveTop, belowBottom, aboveTop));
        NoiseRouterData.registerOreVeins(context);
        DensityFunction shiftX = NoiseRouterData.registerAndWrap(context, SHIFT_X, DensityFunctions.cache(DensityFunctions.shiftA(noises.getOrThrow(Noises.SHIFT))));
        DensityFunction shiftZ = NoiseRouterData.registerAndWrap(context, SHIFT_Z, DensityFunctions.cache(DensityFunctions.shiftB(noises.getOrThrow(Noises.SHIFT))));
        context.register(BASE_3D_NOISE_OVERWORLD, new BlendedNoise(0.25, 0.125, 80.0, 160.0, 8.0));
        context.register(BASE_3D_NOISE_NETHER, new BlendedNoise(0.25, 0.375, 80.0, 60.0, 8.0));
        context.register(BASE_3D_NOISE_END, new BlendedNoise(0.25, 0.25, 80.0, 160.0, 4.0));
        NoiseRouterData.registerAndWrap(context, OVERWORLD_FUNCTIONS.temperature(), DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, noises.getOrThrow(Noises.TEMPERATURE)));
        NoiseRouterData.registerAndWrap(context, LARGE_OVERWORLD_FUNCTIONS.temperature(), DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, noises.getOrThrow(Noises.TEMPERATURE_LARGE)));
        NoiseRouterData.registerAndWrap(context, OVERWORLD_FUNCTIONS.vegetation(), DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, noises.getOrThrow(Noises.VEGETATION)));
        NoiseRouterData.registerAndWrap(context, LARGE_OVERWORLD_FUNCTIONS.vegetation(), DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, noises.getOrThrow(Noises.VEGETATION_LARGE)));
        DensityFunction continents = NoiseRouterData.registerAndWrap(context, OVERWORLD_FUNCTIONS.continents(), DensityFunctions.cache(DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, noises.getOrThrow(Noises.CONTINENTALNESS))));
        DensityFunction erosion = NoiseRouterData.registerAndWrap(context, OVERWORLD_FUNCTIONS.erosion(), DensityFunctions.cache(DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, noises.getOrThrow(Noises.EROSION))));
        DensityFunction ridge = NoiseRouterData.registerAndWrap(context, RIDGES, DensityFunctions.cache(DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, noises.getOrThrow(Noises.RIDGE))));
        context.register(RIDGES_FOLDED, NoiseRouterData.peaksAndValleys(ridge));
        DensityFunction jaggedNoise = DensityFunctions.noise(noises.getOrThrow(Noises.JAGGED), 1500.0, 0.0);
        NoiseRouterData.registerTerrainNoises(context, functions, noises, jaggedNoise, continents, erosion, OVERWORLD_FUNCTIONS, false);
        DensityFunction continentsLarge = NoiseRouterData.registerAndWrap(context, LARGE_OVERWORLD_FUNCTIONS.continents(), DensityFunctions.cache(DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, noises.getOrThrow(Noises.CONTINENTALNESS_LARGE))));
        DensityFunction erosionLarge = NoiseRouterData.registerAndWrap(context, LARGE_OVERWORLD_FUNCTIONS.erosion(), DensityFunctions.cache(DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, noises.getOrThrow(Noises.EROSION_LARGE))));
        NoiseRouterData.registerTerrainNoises(context, functions, noises, jaggedNoise, continentsLarge, erosionLarge, LARGE_OVERWORLD_FUNCTIONS, false);
        NoiseRouterData.registerTerrainNoises(context, functions, noises, jaggedNoise, continents, erosion, AMPLIFIED_OVERWORLD_FUNCTIONS, true);
        DensityFunction endIslands = NoiseRouterData.registerAndWrap(context, END_ISLANDS, NoiseRouterData.createEndIslands());
        context.register(SLOPED_CHEESE_END, DensityFunctions.add(endIslands, NoiseRouterData.getFunction(functions, BASE_3D_NOISE_END)));
        context.register(SPAGHETTI_ROUGHNESS_FUNCTION, NoiseRouterData.spaghettiRoughnessFunction(noises));
        context.register(SPAGHETTI_2D_THICKNESS_MODULATOR, DensityFunctions.cache(DensityFunctions.mappedNoise(noises.getOrThrow(Noises.SPAGHETTI_2D_THICKNESS), 2.0, 1.0, -0.6f, -1.3f)));
        context.register(SPAGHETTI_2D, NoiseRouterData.spaghetti2D(functions, noises));
        context.register(ENTRANCES, NoiseRouterData.entrances(functions, noises));
        context.register(NOODLE, NoiseRouterData.noodle(functions, noises));
        context.register(PILLARS, NoiseRouterData.pillars(noises));
    }

    private static DensityFunction createEndIslands() {
        DensityFunction distanceToMainIsland = DensityFunctions.distanceToPoint(Vec3i.ZERO, DistanceMetric.EUCLIDEAN);
        DensityFunction mainIsland = DensityFunctions.sliceY(DensityFunctions.constant(100.0f).sub(distanceToMainIsland).clamp(-100.0f, 80.0f).sub(8.0f).mul(0.0078125f), 0);
        DensityFunction outerIslands = DensityFunctions.endOuterIslands();
        return DensityFunctions.cache(DensityFunctions.max(mainIsland, outerIslands));
    }

    private static void registerTerrainNoises(BootstrapContext<DensityFunction> context, HolderGetter<DensityFunction> functions, HolderGetter<NormalNoise> noises, DensityFunction jaggedNoise, DensityFunction continentsFunction, DensityFunction erosionFunction, OverworldFunctionSet<ResourceKey<DensityFunction>> names, boolean amplified) {
        SplineFunction.Coordinate continents = new SplineFunction.Coordinate(continentsFunction);
        SplineFunction.Coordinate erosion = new SplineFunction.Coordinate(erosionFunction);
        SplineFunction.Coordinate weirdness = new SplineFunction.Coordinate(NoiseRouterData.getFunction(functions, RIDGES));
        SplineFunction.Coordinate ridges = new SplineFunction.Coordinate(NoiseRouterData.getFunction(functions, RIDGES_FOLDED));
        DensityFunction offset = NoiseRouterData.registerAndWrap(context, names.offset(), NoiseRouterData.splineWithBlending(DensityFunctions.add(DensityFunctions.constant(-0.50375f), DensityFunctions.spline(TerrainProvider.overworldOffset(continents, erosion, ridges, amplified))), DensityFunctions.blendOffset()));
        DensityFunction factor = NoiseRouterData.registerAndWrap(context, names.factor(), NoiseRouterData.splineWithBlending(DensityFunctions.spline(TerrainProvider.overworldFactor(continents, erosion, weirdness, ridges, amplified)), BLENDING_FACTOR));
        DensityFunction depth = NoiseRouterData.registerAndWrap(context, names.depth(), NoiseRouterData.offsetToDepth(offset));
        DensityFunction unscaledJaggedness = NoiseRouterData.registerAndWrap(context, names.jaggedness(), NoiseRouterData.splineWithBlending(DensityFunctions.spline(TerrainProvider.overworldJaggedness(continents, erosion, weirdness, ridges, amplified)), BLENDING_JAGGEDNESS));
        DensityFunction jaggedness = DensityFunctions.cache(DensityFunctions.mul(unscaledJaggedness, jaggedNoise.halfNegative()));
        DensityFunction initialDensity = NoiseRouterData.noiseGradientDensity(factor, DensityFunctions.add(depth, jaggedness));
        DensityFunction slopedCheese = NoiseRouterData.registerAndWrap(context, names.slopedCheese(), DensityFunctions.cache(DensityFunctions.add(initialDensity, NoiseRouterData.getFunction(functions, BASE_3D_NOISE_OVERWORLD))));
        DensityFunction surfaceLevel = NoiseRouterData.registerAndWrap(context, names.preliminarySurfaceLevel(), NoiseRouterData.preliminarySurfaceLevel(offset, factor, amplified));
        context.register(names.chunkSurfaceLevel(), DensityFunctions.interpolated(surfaceLevel, 16, 1));
        DensityFunction surfaceWithEntrances = DensityFunctions.min(slopedCheese, NoiseRouterData.getFunction(functions, ENTRANCES).mul(5.0f));
        DensityFunction caves = DensityFunctions.rangeChoice(slopedCheese, -1000000.0f, 1.5625f, surfaceWithEntrances, NoiseRouterData.underground(functions, noises, slopedCheese));
        context.register(names.finalDensity(), DensityFunctions.add(DensityFunctions.min(NoiseRouterData.postProcess(NoiseRouterData.slideOverworld(amplified, caves), 4, 8), NoiseRouterData.getFunction(functions, NOODLE)), DensityFunctions.beardifier()));
    }

    private static DensityFunction offsetToDepth(DensityFunction offset) {
        return DensityFunctions.add(DensityFunctions.yClampedGradient(-64, 320, 1.5f, -1.5f), offset);
    }

    private static DensityFunction registerAndWrap(BootstrapContext<DensityFunction> context, ResourceKey<DensityFunction> name, DensityFunction value) {
        return new DensityFunctions.HolderHolder(context.register(name, value));
    }

    public static DensityFunction getFunction(HolderGetter<DensityFunction> functions, ResourceKey<DensityFunction> name) {
        return new DensityFunctions.HolderHolder(functions.getOrThrow(name));
    }

    public static DensityFunction peaksAndValleys(DensityFunction weirdness) {
        return DensityFunctions.mul(DensityFunctions.add(DensityFunctions.add(weirdness.abs(), DensityFunctions.constant(-0.6666667f)).abs(), DensityFunctions.constant(-0.33333334f)), DensityFunctions.constant(-3.0f));
    }

    public static float peaksAndValleys(float weirdness) {
        return TerrainProvider.peaksAndValleys(weirdness);
    }

    private static DensityFunction spaghettiRoughnessFunction(HolderGetter<NormalNoise> noises) {
        DensityFunction spaghettiRoughnessNoise = DensityFunctions.noise(noises.getOrThrow(Noises.SPAGHETTI_ROUGHNESS));
        DensityFunction spaghettiRoughnessModulator = DensityFunctions.mappedNoise(noises.getOrThrow(Noises.SPAGHETTI_ROUGHNESS_MODULATOR), 0.0f, -0.1f);
        return DensityFunctions.cache(DensityFunctions.mul(spaghettiRoughnessModulator, DensityFunctions.add(spaghettiRoughnessNoise.abs(), DensityFunctions.constant(-0.4f))));
    }

    private static DensityFunction entrances(HolderGetter<DensityFunction> functions, HolderGetter<NormalNoise> noises) {
        DensityFunction spaghetti3DRarityModulator = DensityFunctions.cache(DensityFunctions.noise(noises.getOrThrow(Noises.SPAGHETTI_3D_RARITY), 2.0, 1.0));
        DensityFunction spaghetti3DThicknessModulator = DensityFunctions.mappedNoise(noises.getOrThrow(Noises.SPAGHETTI_3D_THICKNESS), -0.065f, -0.088f);
        DensityFunction spaghetti3DCave1 = QuantizedSpaghettiRarity.wrapRarity3d(spaghetti3DRarityModulator, noises.getOrThrow(Noises.SPAGHETTI_3D_1));
        DensityFunction spaghetti3DCave2 = QuantizedSpaghettiRarity.wrapRarity3d(spaghetti3DRarityModulator, noises.getOrThrow(Noises.SPAGHETTI_3D_2));
        DensityFunction spaghetti3DFunction = DensityFunctions.add(DensityFunctions.max(spaghetti3DCave1, spaghetti3DCave2), spaghetti3DThicknessModulator).clamp(-1.0f, 1.0f);
        DensityFunction spaghettiRoughnessFunction = NoiseRouterData.getFunction(functions, SPAGHETTI_ROUGHNESS_FUNCTION);
        DensityFunction bigEntranceNoiseSource = DensityFunctions.noise(noises.getOrThrow(Noises.CAVE_ENTRANCE), 0.75, 0.5);
        DensityFunction bigEntrancesFunction = DensityFunctions.add(bigEntranceNoiseSource.add(0.37f), DensityFunctions.yClampedGradient(-10, 30, 0.3f, 0.0f));
        return DensityFunctions.cache(DensityFunctions.min(bigEntrancesFunction, DensityFunctions.add(spaghettiRoughnessFunction, spaghetti3DFunction)));
    }

    private static DensityFunction noodle(HolderGetter<DensityFunction> functions, HolderGetter<NormalNoise> noises) {
        DensityFunction y = NoiseRouterData.getFunction(functions, Y);
        int minBlockY = -64;
        int noodleMinY = -60;
        int noodleMaxY = 320;
        int cellSizeXz = 4;
        int cellSizeY = 8;
        DensityFunction noodleToggle = NoiseRouterData.yLimitedInterpolatable(y, DensityFunctions.noise(noises.getOrThrow(Noises.NOODLE), 1.0, 1.0), -60, 320, -1, 4, 8);
        DensityFunction noodleThickness = NoiseRouterData.yLimitedInterpolatable(y, DensityFunctions.mappedNoise(noises.getOrThrow(Noises.NOODLE_THICKNESS), 1.0, 1.0, -0.05f, -0.1f), -60, 320, 0, 4, 8);
        double noodleRidgeFrequency = 2.6666666666666665;
        DensityFunction noodleRidgeA = NoiseRouterData.yLimitedInterpolatable(y, DensityFunctions.noise(noises.getOrThrow(Noises.NOODLE_RIDGE_A), 2.6666666666666665, 2.6666666666666665), -60, 320, 0, 4, 8);
        DensityFunction noodleRidgeB = NoiseRouterData.yLimitedInterpolatable(y, DensityFunctions.noise(noises.getOrThrow(Noises.NOODLE_RIDGE_B), 2.6666666666666665, 2.6666666666666665), -60, 320, 0, 4, 8);
        DensityFunction noodleRidged = DensityFunctions.max(noodleRidgeA.abs(), noodleRidgeB.abs()).mul(1.5f);
        return DensityFunctions.rangeChoice(noodleToggle, -1000000.0f, 0.0f, DensityFunctions.constant(64.0f), DensityFunctions.add(noodleThickness, noodleRidged));
    }

    private static DensityFunction pillars(HolderGetter<NormalNoise> noises) {
        double xzFrequency = 25.0;
        double yFrequency = 0.3;
        DensityFunction pillarNoiseSource = DensityFunctions.noise(noises.getOrThrow(Noises.PILLAR), 25.0, 0.3);
        DensityFunction pillarRarenessModulator = DensityFunctions.mappedNoise(noises.getOrThrow(Noises.PILLAR_RARENESS), 0.0f, -2.0f);
        DensityFunction pillarThicknessModulator = DensityFunctions.mappedNoise(noises.getOrThrow(Noises.PILLAR_THICKNESS), 0.0f, 1.1f);
        DensityFunction pillarsWithRareness = DensityFunctions.add(pillarNoiseSource.mul(2.0f), pillarRarenessModulator);
        return DensityFunctions.cache(DensityFunctions.mul(pillarsWithRareness, pillarThicknessModulator.cube()));
    }

    private static DensityFunction spaghetti2D(HolderGetter<DensityFunction> functions, HolderGetter<NormalNoise> noises) {
        DensityFunction spaghetti2DRarityModulator = DensityFunctions.noise(noises.getOrThrow(Noises.SPAGHETTI_2D_MODULATOR), 2.0, 1.0);
        DensityFunction spaghetti2DCave = QuantizedSpaghettiRarity.wrapRarity2d(spaghetti2DRarityModulator, noises.getOrThrow(Noises.SPAGHETTI_2D));
        DensityFunction spaghetti2DElevationModulator = DensityFunctions.mappedNoise(noises.getOrThrow(Noises.SPAGHETTI_2D_ELEVATION), 0.0, Math.floorDiv(-64, 8), 8.0f);
        DensityFunction spaghetti2DThicknessModulator = NoiseRouterData.getFunction(functions, SPAGHETTI_2D_THICKNESS_MODULATOR);
        DensityFunction slopedSpaghetti = DensityFunctions.add(DensityFunctions.cache(spaghetti2DElevationModulator), DensityFunctions.yClampedGradient(-64, 320, 8.0f, -40.0f)).abs();
        DensityFunction layerRidged = DensityFunctions.add(slopedSpaghetti, spaghetti2DThicknessModulator).cube();
        float ridgeOffset = 0.083f;
        DensityFunction caveNoise = DensityFunctions.add(spaghetti2DCave, spaghetti2DThicknessModulator.mul(0.083f));
        return DensityFunctions.max(caveNoise, layerRidged).clamp(-1.0f, 1.0f);
    }

    private static DensityFunction underground(HolderGetter<DensityFunction> functions, HolderGetter<NormalNoise> noises, DensityFunction slopedCheese) {
        DensityFunction spaghetti2DFunction = NoiseRouterData.getFunction(functions, SPAGHETTI_2D);
        DensityFunction spaghettiRoughnessFunction = NoiseRouterData.getFunction(functions, SPAGHETTI_ROUGHNESS_FUNCTION);
        DensityFunction layerNoiseSource = DensityFunctions.noise(noises.getOrThrow(Noises.CAVE_LAYER), 8.0);
        DensityFunction layerizedCavernsFunction = layerNoiseSource.square().mul(4.0f);
        DensityFunction cheese = DensityFunctions.noise(noises.getOrThrow(Noises.CAVE_CHEESE), 0.6666666666666666);
        DensityFunction solidifedCheeseWithTopSlide = DensityFunctions.add(cheese.add(0.27f).clamp(-1.0f, 1.0f), slopedCheese.mul(-0.64f).add(1.5f).clamp(0.0f, 0.5f));
        DensityFunction baseCaveDensity = DensityFunctions.add(layerizedCavernsFunction, solidifedCheeseWithTopSlide);
        DensityFunction undergroundSubtractions = DensityFunctions.min(DensityFunctions.min(baseCaveDensity, NoiseRouterData.getFunction(functions, ENTRANCES)), DensityFunctions.add(spaghetti2DFunction, spaghettiRoughnessFunction));
        DensityFunction pillarsWithoutCutoff = NoiseRouterData.getFunction(functions, PILLARS);
        DensityFunction pillars = DensityFunctions.rangeChoice(pillarsWithoutCutoff, -1000000.0f, 0.03f, DensityFunctions.constant(-1000000.0f), pillarsWithoutCutoff);
        return DensityFunctions.max(undergroundSubtractions, pillars);
    }

    private static DensityFunction postProcess(DensityFunction slide, int cellSizeXz, int cellSizeY) {
        DensityFunction blended = DensityFunctions.blendDensity(slide);
        return DensityFunctions.interpolated(DensityFunctions.mul(blended, DensityFunctions.constant(0.64f)), cellSizeXz, cellSizeY).squeeze();
    }

    protected static NoiseRouter overworld(HolderGetter<DensityFunction> functions, OverworldFunctionSet<ResourceKey<DensityFunction>> functionNames) {
        OverworldFunctionSet<DensityFunction> functionSet = functionNames.map(key -> NoiseRouterData.getFunction(functions, key));
        return new NoiseRouter(functionSet.temperature(), functionSet.vegetation(), functionSet.continents(), functionSet.erosion(), functionSet.depth(), NoiseRouterData.getFunction(functions, RIDGES), functionSet.chunkSurfaceLevel(), functionSet.finalDensity());
    }

    protected static Aquifer.Config overworldAquifers(HolderGetter<DensityFunction> functions, HolderGetter<NormalNoise> noises, OverworldFunctionSet<ResourceKey<DensityFunction>> names) {
        DensityFunction barrierNoise = DensityFunctions.noise(noises.getOrThrow(Noises.AQUIFER_BARRIER), 0.5);
        DensityFunction fluidLevelFloodednessNoise = DensityFunctions.noise(noises.getOrThrow(Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS), 0.67);
        DensityFunction fluidLevelSpreadNoise = DensityFunctions.noise(noises.getOrThrow(Noises.AQUIFER_FLUID_LEVEL_SPREAD), 0.7142857142857143);
        DensityFunction lavaNoise = DensityFunctions.noise(noises.getOrThrow(Noises.AQUIFER_LAVA));
        DensityFunction exclusion = OverworldBiomeBuilder.deepDarkRegion(NoiseRouterData.getFunction(functions, names.erosion()), NoiseRouterData.getFunction(functions, names.depth()));
        return new Aquifer.Config(barrierNoise, fluidLevelFloodednessNoise, fluidLevelSpreadNoise, lavaNoise, exclusion, NoiseRouterData.getFunction(functions, names.preliminarySurfaceLevel()));
    }

    private static void registerOreVeins(BootstrapContext<DensityFunction> context) {
        HolderGetter<DensityFunction> functions = context.lookup(Registries.DENSITY_FUNCTION);
        HolderGetter<NormalNoise> noises = context.lookup(Registries.NOISE);
        DensityFunction y = NoiseRouterData.getFunction(functions, Y);
        int veinMinY = Stream.of(OreVeinRule.VeinType.values()).mapToInt(t -> t.minY).min().orElseThrow();
        int veinMaxY = Stream.of(OreVeinRule.VeinType.values()).mapToInt(t -> t.maxY).max().orElseThrow();
        int interpolatedCellHeight = 8;
        int interpolatedVeinMinY = Mth.floorDiv(veinMinY, 8) * 8;
        int interpolatedVeinMaxY = (Mth.floorDiv(veinMaxY, 8) + 1) * 8;
        DensityFunction veinToggle = NoiseRouterData.registerAndWrap(context, ORE_VEIN_TOGGLE, DensityFunctions.cache(NoiseRouterData.yLimitedInterpolatable(y, DensityFunctions.noise(noises.getOrThrow(Noises.ORE_VEININESS), 1.5, 1.5), interpolatedVeinMinY, interpolatedVeinMaxY, 0, 4, 8)));
        context.register(ORE_VEIN_RICHNESS, DensityFunctions.clampedMap(veinToggle.abs(), 0.4f, 0.6f, 0.1f, 0.3f));
        DensityFunction veinMask = NoiseRouterData.registerAndWrap(context, ORE_VEIN_MASK, DensityFunctions.cache(NoiseRouterData.createBaseOreVeinMask(noises, y, veinToggle, interpolatedVeinMinY, interpolatedVeinMaxY)));
        context.register(ORE_VEIN_COPPER_DENSITY, NoiseRouterData.createOreVeinDensity(OreVeinRule.VeinType.COPPER, y, veinToggle, veinMask, true));
        context.register(ORE_VEIN_IRON_DENSITY, NoiseRouterData.createOreVeinDensity(OreVeinRule.VeinType.IRON, y, veinToggle, veinMask, false));
        context.register(ORE_VEIN_GAP, DensityFunctions.constant(-0.3f).sub(DensityFunctions.noise(noises.getOrThrow(Noises.ORE_GAP))));
    }

    private static DensityFunction createBaseOreVeinMask(HolderGetter<NormalNoise> noises, DensityFunction y, DensityFunction toggle, int minY, int maxY) {
        float oreRidgeFrequency = 4.0f;
        boolean noVein = true;
        int cellSizeXz = 4;
        int cellSizeY = 8;
        DensityFunction veinA = NoiseRouterData.yLimitedInterpolatable(y, DensityFunctions.noise(noises.getOrThrow(Noises.ORE_VEIN_A), 4.0, 4.0), minY, maxY, 1, 4, 8).abs();
        DensityFunction veinB = NoiseRouterData.yLimitedInterpolatable(y, DensityFunctions.noise(noises.getOrThrow(Noises.ORE_VEIN_B), 4.0, 4.0), minY, maxY, 1, 4, 8).abs();
        return DensityFunctions.rangeChoice(toggle, -0.4f, 0.4f, DensityFunctions.constant(-1.0f), DensityFunctions.constant(0.08f).sub(DensityFunctions.max(veinA, veinB)));
    }

    private static DensityFunction createOreVeinDensity(OreVeinRule.VeinType type, DensityFunction y, DensityFunction toggle, DensityFunction baseVeinMask, boolean whenTogglePositive) {
        DensityFunction noVein = DensityFunctions.constant(-1.0f);
        DensityFunction distanceFromEdge = DensityFunctions.min(DensityFunctions.constant(type.maxY).sub(y), y.sub(type.minY));
        DensityFunction edgeRoundoff = DensityFunctions.clampedMap(distanceFromEdge, 0.0f, 20.0f, -0.2f, 0.0f);
        DensityFunction veininess = whenTogglePositive ? toggle : toggle.negate();
        return DensityFunctions.rangeChoice(y, type.minY, type.maxY, DensityFunctions.rangeChoice(baseVeinMask, 0.0f, 1000000.0f, DensityFunctions.rangeChoice(veininess.sub(0.4f).add(edgeRoundoff), 0.0f, 1000000.0f, DensityFunctions.constant(0.7f), noVein), noVein), noVein);
    }

    private static DensityFunction slideOverworld(boolean isAmplified, DensityFunction caves) {
        return NoiseRouterData.slide(caves, -64, 384, isAmplified ? 16 : 80, isAmplified ? 0 : 64, -0.078125f, 0, 24, isAmplified ? 0.4f : 0.1171875f);
    }

    private static DensityFunction slideNetherLike(HolderGetter<DensityFunction> functions, int minY, int height) {
        return NoiseRouterData.slide(NoiseRouterData.getFunction(functions, BASE_3D_NOISE_NETHER), minY, height, 24, 0, 0.9375f, -8, 24, 2.5f);
    }

    private static DensityFunction slideEndLike(DensityFunction caves, int minY, int height) {
        return NoiseRouterData.slide(caves, minY, height, 72, -184, -23.4375f, 4, 32, -0.234375f);
    }

    protected static NoiseRouter nether(HolderGetter<DensityFunction> functions, HolderGetter<NormalNoise> noises) {
        DensityFunction temperature = DensityFunctions.shiftedNoise2d(DensityFunctions.zero(), DensityFunctions.zero(), 0.25, noises.getOrThrow(Noises.TEMPERATURE_NETHER));
        DensityFunction vegetation = DensityFunctions.shiftedNoise2d(DensityFunctions.zero(), DensityFunctions.zero(), 0.25, noises.getOrThrow(Noises.VEGETATION_NETHER));
        DensityFunction slide = NoiseRouterData.slideNetherLike(functions, 0, 128);
        DensityFunction fullNoise = DensityFunctions.add(NoiseRouterData.postProcess(slide, 4, 8), DensityFunctions.beardifier());
        return new NoiseRouter(temperature, vegetation, DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), fullNoise);
    }

    protected static NoiseRouter caves(HolderGetter<DensityFunction> functions) {
        DensityFunction slide = NoiseRouterData.slideNetherLike(functions, -64, 192);
        return NoiseRouterData.simpleRouter(DensityFunctions.add(NoiseRouterData.postProcess(slide, 4, 8), DensityFunctions.beardifier()));
    }

    protected static NoiseRouter floatingIslands(HolderGetter<DensityFunction> functions, HolderGetter<NormalNoise> noises) {
        DensityFunction slide = NoiseRouterData.slideEndLike(NoiseRouterData.getFunction(functions, BASE_3D_NOISE_END), 0, 256);
        return NoiseRouterData.simpleRouter(DensityFunctions.add(NoiseRouterData.postProcess(slide, 8, 4), DensityFunctions.beardifier()));
    }

    private static DensityFunction slideEnd(DensityFunction caves) {
        return NoiseRouterData.slideEndLike(caves, 0, 128);
    }

    protected static NoiseRouter end(HolderGetter<DensityFunction> functions) {
        DensityFunction islands = NoiseRouterData.getFunction(functions, END_ISLANDS);
        DensityFunction fullNoise = DensityFunctions.add(NoiseRouterData.postProcess(NoiseRouterData.slideEnd(NoiseRouterData.getFunction(functions, SLOPED_CHEESE_END)), 8, 4), DensityFunctions.beardifier());
        return new NoiseRouter(DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), islands, DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), fullNoise);
    }

    private static NoiseRouter simpleRouter(DensityFunction fullNoise) {
        return new NoiseRouter(DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), fullNoise);
    }

    public static NoiseRouter none() {
        return NoiseRouterData.simpleRouter(DensityFunctions.zero());
    }

    private static DensityFunction splineWithBlending(DensityFunction spline, DensityFunction blendingTarget) {
        return DensityFunctions.cache(DensityFunctions.lerp(DensityFunctions.blendAlpha(), blendingTarget, spline));
    }

    private static DensityFunction noiseGradientDensity(DensityFunction factor, DensityFunction depthWithJaggedness) {
        DensityFunction gradientUnscaled = DensityFunctions.mul(depthWithJaggedness, factor);
        return gradientUnscaled.quarterNegative().mul(4.0f);
    }

    private static DensityFunction preliminarySurfaceLevel(DensityFunction offset, DensityFunction factor, boolean amplified) {
        DensityFunction upperBound = DensityFunctions.remap(DensityFunctions.sub(DensityFunctions.div(DensityFunctions.constant(0.2734375f), factor), offset), 1.5f, -1.5f, -64.0f, 320.0f);
        upperBound = upperBound.clamp(-40.0f, 320.0f);
        DensityFunction density = DensityFunctions.add(NoiseRouterData.slideOverworld(amplified, DensityFunctions.add(NoiseRouterData.noiseGradientDensity(factor, NoiseRouterData.offsetToDepth(offset)), DensityFunctions.constant(-0.703125f)).clamp(-64.0f, 64.0f)), DensityFunctions.constant(-0.390625f));
        return DensityFunctions.findTopSurface(density, upperBound, -64, 8);
    }

    private static DensityFunction yLimitedInterpolatable(DensityFunction y, DensityFunction whenInRange, int minYInclusive, int maxYInclusive, int whenOutOfRange, int cellSizeXz, int cellSizeY) {
        return DensityFunctions.interpolated(DensityFunctions.rangeChoice(y, minYInclusive, maxYInclusive + 1, whenInRange, DensityFunctions.constant(whenOutOfRange)), cellSizeXz, cellSizeY);
    }

    private static DensityFunction slide(DensityFunction caves, int minY, int height, int topStartY, int topEndY, float topTarget, int bottomStartY, int bottomEndY, float bottomTarget) {
        DensityFunction noiseValue = caves;
        DensityFunction topFactor = DensityFunctions.yClampedGradient(minY + height - topStartY, minY + height - topEndY, 1.0f, 0.0f);
        noiseValue = DensityFunctions.lerp(topFactor, topTarget, noiseValue);
        DensityFunction bottomFactor = DensityFunctions.yClampedGradient(minY + bottomStartY, minY + bottomEndY, 0.0f, 1.0f);
        noiseValue = DensityFunctions.lerp(bottomFactor, bottomTarget, noiseValue);
        return noiseValue;
    }

    protected static final class QuantizedSpaghettiRarity {
        protected QuantizedSpaghettiRarity() {
        }

        public static DensityFunction wrapRarity2d(DensityFunction input, Holder<NormalNoise> noise) {
            return DensityFunctions.intervalSelect(input, FloatList.of((float[])new float[]{-0.75f, -0.5f, 0.5f, 0.75f}), List.of(QuantizedSpaghettiRarity.noiseFunctionForRarity(noise, 0.5f), QuantizedSpaghettiRarity.noiseFunctionForRarity(noise, 0.75f), QuantizedSpaghettiRarity.noiseFunctionForRarity(noise, 1.0f), QuantizedSpaghettiRarity.noiseFunctionForRarity(noise, 2.0f), QuantizedSpaghettiRarity.noiseFunctionForRarity(noise, 3.0f))).abs();
        }

        public static DensityFunction wrapRarity3d(DensityFunction input, Holder<NormalNoise> noise) {
            return DensityFunctions.intervalSelect(input, FloatList.of((float)-0.5f, (float)0.0f, (float)0.5f), List.of(QuantizedSpaghettiRarity.noiseFunctionForRarity(noise, 0.75f), QuantizedSpaghettiRarity.noiseFunctionForRarity(noise, 1.0f), QuantizedSpaghettiRarity.noiseFunctionForRarity(noise, 1.5f), QuantizedSpaghettiRarity.noiseFunctionForRarity(noise, 2.0f))).abs();
        }

        private static DensityFunction noiseFunctionForRarity(Holder<NormalNoise> noise, float rarity) {
            return DensityFunctions.noise(noise, 1.0 / (double)rarity, 1.0 / (double)rarity).mul(rarity);
        }
    }
}

