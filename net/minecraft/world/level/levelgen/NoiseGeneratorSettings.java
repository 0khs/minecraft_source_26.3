/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.material.EndMaterialRules;
import net.minecraft.data.worldgen.material.NetherMaterialRules;
import net.minecraft.data.worldgen.material.OverworldMaterialRules;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.OverworldFunctionSet;
import net.minecraft.world.level.levelgen.SpawnTargetPoint;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public record NoiseGeneratorSettings(NoiseSettings noiseSettings, BlockState defaultBlock, BlockState defaultFluid, NoiseRouter noiseRouter, Holder<MaterialRule> materialRule, List<SpawnTargetPoint> spawnTarget, int seaLevel, boolean disableMobGeneration, Optional<Aquifer.Config> aquifers, boolean useLegacyRandomSource, DebugFunctions debugFunctions) {
    public static final Codec<NoiseGeneratorSettings> DIRECT_CODEC = RecordCodecBuilder.create(i -> i.group((App)NoiseSettings.CODEC.fieldOf("noise").forGetter(NoiseGeneratorSettings::noiseSettings), (App)BlockState.CODEC.fieldOf("default_block").forGetter(NoiseGeneratorSettings::defaultBlock), (App)BlockState.CODEC.fieldOf("default_fluid").forGetter(NoiseGeneratorSettings::defaultFluid), (App)NoiseRouter.CODEC.fieldOf("noise_router").forGetter(NoiseGeneratorSettings::noiseRouter), (App)MaterialRule.HOLDER_CODEC.fieldOf("material_rule").forGetter(NoiseGeneratorSettings::materialRule), (App)SpawnTargetPoint.CODEC.listOf().fieldOf("spawn_target").forGetter(NoiseGeneratorSettings::spawnTarget), (App)Codec.INT.fieldOf("sea_level").forGetter(NoiseGeneratorSettings::seaLevel), (App)Codec.BOOL.fieldOf("disable_mob_generation").forGetter(NoiseGeneratorSettings::disableMobGeneration), (App)Aquifer.Config.CODEC.optionalFieldOf("aquifers").forGetter(NoiseGeneratorSettings::aquifers), (App)Codec.BOOL.fieldOf("legacy_random_source").forGetter(NoiseGeneratorSettings::useLegacyRandomSource), (App)DebugFunctions.CODEC.optionalFieldOf("debug_functions", (Object)DebugFunctions.EMPTY).forGetter(NoiseGeneratorSettings::debugFunctions)).apply((Applicative)i, NoiseGeneratorSettings::new));
    public static final Codec<Holder<NoiseGeneratorSettings>> CODEC = RegistryCodecs.holder(Registries.NOISE_SETTINGS, DIRECT_CODEC);
    public static final ResourceKey<NoiseGeneratorSettings> OVERWORLD = ResourceKey.create(Registries.NOISE_SETTINGS, Identifier.withDefaultNamespace("overworld"));
    public static final ResourceKey<NoiseGeneratorSettings> LARGE_BIOMES = ResourceKey.create(Registries.NOISE_SETTINGS, Identifier.withDefaultNamespace("large_biomes"));
    public static final ResourceKey<NoiseGeneratorSettings> AMPLIFIED = ResourceKey.create(Registries.NOISE_SETTINGS, Identifier.withDefaultNamespace("amplified"));
    public static final ResourceKey<NoiseGeneratorSettings> NETHER = ResourceKey.create(Registries.NOISE_SETTINGS, Identifier.withDefaultNamespace("nether"));
    public static final ResourceKey<NoiseGeneratorSettings> END = ResourceKey.create(Registries.NOISE_SETTINGS, Identifier.withDefaultNamespace("end"));
    public static final ResourceKey<NoiseGeneratorSettings> CAVES = ResourceKey.create(Registries.NOISE_SETTINGS, Identifier.withDefaultNamespace("caves"));
    public static final ResourceKey<NoiseGeneratorSettings> FLOATING_ISLANDS = ResourceKey.create(Registries.NOISE_SETTINGS, Identifier.withDefaultNamespace("floating_islands"));

    public WorldgenRandom.Algorithm getRandomSource() {
        return this.useLegacyRandomSource ? WorldgenRandom.Algorithm.LEGACY : WorldgenRandom.Algorithm.XOROSHIRO;
    }

    public static void bootstrap(BootstrapContext<NoiseGeneratorSettings> context) {
        context.register(OVERWORLD, NoiseGeneratorSettings.overworld(context, false, false));
        context.register(LARGE_BIOMES, NoiseGeneratorSettings.overworld(context, false, true));
        context.register(AMPLIFIED, NoiseGeneratorSettings.overworld(context, true, false));
        context.register(NETHER, NoiseGeneratorSettings.nether(context));
        context.register(END, NoiseGeneratorSettings.end(context));
        context.register(CAVES, NoiseGeneratorSettings.caves(context));
        context.register(FLOATING_ISLANDS, NoiseGeneratorSettings.floatingIslands(context));
    }

    private static NoiseGeneratorSettings end(BootstrapContext<?> context) {
        NoiseRouter router = NoiseRouterData.end(context.lookup(Registries.DENSITY_FUNCTION));
        return new NoiseGeneratorSettings(NoiseSettings.END_NOISE_SETTINGS, Blocks.END_STONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), router, context.lookup(Registries.MATERIAL_RULE).getOrThrow(EndMaterialRules.END), List.of(), 0, true, Optional.empty(), true, new DebugFunctions(List.of(new DebugFunctionEntry("N", router.finalDensity()), new DebugFunctionEntry("IS", router.erosion()))));
    }

    private static NoiseGeneratorSettings nether(BootstrapContext<?> context) {
        NoiseRouter router = NoiseRouterData.nether(context.lookup(Registries.DENSITY_FUNCTION), context.lookup(Registries.NOISE));
        return new NoiseGeneratorSettings(NoiseSettings.NETHER_NOISE_SETTINGS, Blocks.NETHERRACK.defaultBlockState(), Blocks.LAVA.defaultBlockState(), router, context.lookup(Registries.MATERIAL_RULE).getOrThrow(NetherMaterialRules.NETHER), List.of(), 32, false, Optional.empty(), true, new DebugFunctions(List.of(new DebugFunctionEntry("N", router.finalDensity()), new DebugFunctionEntry("T", router.temperature()), new DebugFunctionEntry("V", router.vegetation()))));
    }

    private static NoiseGeneratorSettings overworld(BootstrapContext<?> context, boolean isAmplified, boolean largeBiomes) {
        HolderGetter<DensityFunction> functions = context.lookup(Registries.DENSITY_FUNCTION);
        HolderGetter<NormalNoise> noises = context.lookup(Registries.NOISE);
        OverworldFunctionSet<ResourceKey<DensityFunction>> functionNames = isAmplified ? NoiseRouterData.AMPLIFIED_OVERWORLD_FUNCTIONS : (largeBiomes ? NoiseRouterData.LARGE_OVERWORLD_FUNCTIONS : NoiseRouterData.OVERWORLD_FUNCTIONS);
        Holder.Reference<DensityFunction> weirdness = functions.getOrThrow(NoiseRouterData.RIDGES);
        List<SpawnTargetPoint> spawnTarget = new OverworldBiomeBuilder().spawnTarget(functionNames.map(functions::getOrThrow), weirdness);
        NoiseRouter router = NoiseRouterData.overworld(functions, functionNames);
        return new NoiseGeneratorSettings(NoiseSettings.OVERWORLD_NOISE_SETTINGS, Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), router, context.lookup(Registries.MATERIAL_RULE).getOrThrow(OverworldMaterialRules.OVERWORLD), spawnTarget, 63, false, Optional.of(NoiseRouterData.overworldAquifers(functions, noises, functionNames)), false, new DebugFunctions(List.of(new DebugFunctionEntry("N", router.finalDensity()), new DebugFunctionEntry("T", router.temperature()), new DebugFunctionEntry("V", router.vegetation()), new DebugFunctionEntry("C", router.continents()), new DebugFunctionEntry("E", router.erosion()), new DebugFunctionEntry("D", router.depth()), new DebugFunctionEntry("W", router.ridges()), new DebugFunctionEntry("PV", NoiseRouterData.peaksAndValleys(router.ridges())), new DebugFunctionEntry("PS", NoiseRouterData.getFunction(functions, functionNames.preliminarySurfaceLevel())))));
    }

    private static NoiseGeneratorSettings caves(BootstrapContext<?> context) {
        NoiseRouter router = NoiseRouterData.caves(context.lookup(Registries.DENSITY_FUNCTION));
        return new NoiseGeneratorSettings(NoiseSettings.CAVES_NOISE_SETTINGS, Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), router, context.lookup(Registries.MATERIAL_RULE).getOrThrow(OverworldMaterialRules.OVERWORLD_CAVES), List.of(), 32, false, Optional.empty(), true, new DebugFunctions(List.of(new DebugFunctionEntry("N", router.finalDensity()))));
    }

    private static NoiseGeneratorSettings floatingIslands(BootstrapContext<?> context) {
        NoiseRouter router = NoiseRouterData.floatingIslands(context.lookup(Registries.DENSITY_FUNCTION), context.lookup(Registries.NOISE));
        return new NoiseGeneratorSettings(NoiseSettings.FLOATING_ISLANDS_NOISE_SETTINGS, Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), router, context.lookup(Registries.MATERIAL_RULE).getOrThrow(OverworldMaterialRules.OVERWORLD_FLOATING_ISLANDS), List.of(), -64, false, Optional.empty(), true, new DebugFunctions(List.of(new DebugFunctionEntry("N", router.finalDensity()))));
    }

    public record DebugFunctions(List<DebugFunctionEntry> functions) {
        public static final DebugFunctions EMPTY = new DebugFunctions(List.of());
        public static final Codec<DebugFunctions> CODEC = DebugFunctionEntry.CODEC.listOf().xmap(DebugFunctions::new, DebugFunctions::functions);
    }

    public record DebugFunctionEntry(String label, DensityFunction function) {
        public static final Codec<DebugFunctionEntry> CODEC = RecordCodecBuilder.create(i -> i.group((App)Codec.STRING.fieldOf("label").forGetter(DebugFunctionEntry::label), (App)DensityFunction.CODEC.fieldOf("function").forGetter(DebugFunctionEntry::function)).apply((Applicative)i, DebugFunctionEntry::new));
    }
}

