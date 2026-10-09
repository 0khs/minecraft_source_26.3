/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.worldgen;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.CaveVinesBlock;
import net.minecraft.world.level.block.MangrovePropaguleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.DualNoiseProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseThresholdProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RandomizedIntStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public interface BlockStateProviders {
    public static final ResourceKey<BlockStateProvider> CAVE_VINES_BODY = BlockStateProviders.key("cave_vines_body");
    public static final ResourceKey<BlockStateProvider> CAVE_VINES_HEAD = BlockStateProviders.key("cave_vines_head");
    public static final ResourceKey<BlockStateProvider> FLOWER_FLOWER_FOREST = BlockStateProviders.key("flower_flower_forest");
    public static final ResourceKey<BlockStateProvider> FLOWER_MEADOW = BlockStateProviders.key("flower_meadow");
    public static final ResourceKey<BlockStateProvider> FLOWER_PLAIN = BlockStateProviders.key("flower_plain");
    public static final ResourceKey<BlockStateProvider> MANGROVE_PROPAGULE = BlockStateProviders.key("mangrove_propagule");
    public static final ResourceKey<BlockStateProvider> PODZOL_BENEATH_TREE = BlockStateProviders.key("podzol_beneath_tree");
    public static final ResourceKey<BlockStateProvider> SOIL_BENEATH_TREE = BlockStateProviders.key("soil_beneath_tree");

    public static void bootstrap(BootstrapContext<BlockStateProvider> context) {
        context.register(CAVE_VINES_BODY, new WeightedStateProvider(WeightedList.builder().add(Blocks.CAVE_VINES_PLANT.defaultBlockState(), 4).add((BlockState)Blocks.CAVE_VINES_PLANT.defaultBlockState().setValue(CaveVines.BERRIES, true), 1)));
        context.register(CAVE_VINES_HEAD, new RandomizedIntStateProvider(new WeightedStateProvider(WeightedList.builder().add(Blocks.CAVE_VINES.defaultBlockState(), 4).add((BlockState)Blocks.CAVE_VINES.defaultBlockState().setValue(CaveVines.BERRIES, true), 1)), CaveVinesBlock.AGE, (IntProvider)UniformInt.of(23, 25)));
        context.register(FLOWER_FLOWER_FOREST, new NoiseProvider(2345L, NormalNoise.createParity(0, 1.0), 0.020833334f, List.of(Blocks.DANDELION.defaultBlockState(), Blocks.POPPY.defaultBlockState(), Blocks.ALLIUM.defaultBlockState(), Blocks.AZURE_BLUET.defaultBlockState(), Blocks.RED_TULIP.defaultBlockState(), Blocks.ORANGE_TULIP.defaultBlockState(), Blocks.WHITE_TULIP.defaultBlockState(), Blocks.PINK_TULIP.defaultBlockState(), Blocks.OXEYE_DAISY.defaultBlockState(), Blocks.CORNFLOWER.defaultBlockState(), Blocks.LILY_OF_THE_VALLEY.defaultBlockState())));
        context.register(FLOWER_MEADOW, new DualNoiseProvider(new InclusiveRange<Integer>(1, 3), NormalNoise.createParity(-10, 1.0), 1.0f, 2345L, NormalNoise.createParity(-3, 1.0), 1.0f, List.of(Blocks.TALL_GRASS.defaultBlockState(), Blocks.ALLIUM.defaultBlockState(), Blocks.POPPY.defaultBlockState(), Blocks.AZURE_BLUET.defaultBlockState(), Blocks.DANDELION.defaultBlockState(), Blocks.CORNFLOWER.defaultBlockState(), Blocks.OXEYE_DAISY.defaultBlockState(), Blocks.SHORT_GRASS.defaultBlockState())));
        context.register(FLOWER_PLAIN, new NoiseThresholdProvider(2345L, NormalNoise.createParity(0, 1.0), 0.005f, -0.8f, 0.33333334f, Blocks.DANDELION.defaultBlockState(), List.of(Blocks.ORANGE_TULIP.defaultBlockState(), Blocks.RED_TULIP.defaultBlockState(), Blocks.PINK_TULIP.defaultBlockState(), Blocks.WHITE_TULIP.defaultBlockState()), List.of(Blocks.POPPY.defaultBlockState(), Blocks.AZURE_BLUET.defaultBlockState(), Blocks.OXEYE_DAISY.defaultBlockState(), Blocks.CORNFLOWER.defaultBlockState())));
        context.register(MANGROVE_PROPAGULE, new RandomizedIntStateProvider(BlockStateProvider.of((BlockState)Blocks.MANGROVE_PROPAGULE.defaultBlockState().setValue(MangrovePropaguleBlock.HANGING, true)), MangrovePropaguleBlock.AGE, (IntProvider)UniformInt.of(0, 4)));
        context.register(PODZOL_BENEATH_TREE, RuleBasedStateProvider.ifTrueThenProvide(BlockPredicate.matchesTag(BlockTags.BENEATH_TREE_PODZOL_REPLACEABLE), Blocks.PODZOL));
        context.register(SOIL_BENEATH_TREE, RuleBasedStateProvider.ifTrueThenProvide(BlockPredicate.not(BlockPredicate.matchesTag(BlockTags.CANNOT_REPLACE_BELOW_TREE_TRUNK)), Blocks.DIRT));
    }

    private static ResourceKey<BlockStateProvider> key(String id) {
        return ResourceKey.create(Registries.BLOCK_STATE_PROVIDER, Identifier.withDefaultNamespace(id));
    }
}

