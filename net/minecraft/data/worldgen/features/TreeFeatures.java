/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.worldgen.features;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BlockStateProviders;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.util.valueproviders.WeightedListInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.FallenTreeFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.HugeBrownMushroomFeature;
import net.minecraft.world.level.levelgen.feature.HugeFungusFeature;
import net.minecraft.world.level.levelgen.feature.HugeRedMushroomFeature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.featuresize.ThreeLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.AcaciaFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BushFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.CherryFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.DarkOakFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FancyFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaJungleFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaPineFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.PineFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.PoplarFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.RandomSpreadFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.SpruceFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.rootplacers.AboveRootPlacement;
import net.minecraft.world.level.levelgen.feature.rootplacers.MangroveRootPlacement;
import net.minecraft.world.level.levelgen.feature.rootplacers.MangroveRootPlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.AlterGroundDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.AttachedToLeavesDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.AttachedToLogsDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.BeehiveDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.CocoaDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.CreakingHeartDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.LeaveVineDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.PaleMossDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.PlaceOnGroundDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.ShelfMushroomDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TrunkVineDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.BendingTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.CherryTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.DarkOakTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.FancyTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.ForkingTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.GiantTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.MegaJungleTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.PoplarTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.UpwardsBranchingTrunkPlacer;

public class TreeFeatures {
    public static final ResourceKey<Feature> CRIMSON_FUNGUS = FeatureUtils.createKey("crimson_fungus");
    public static final ResourceKey<Feature> CRIMSON_FUNGUS_PLANTED = FeatureUtils.createKey("crimson_fungus_planted");
    public static final ResourceKey<Feature> WARPED_FUNGUS = FeatureUtils.createKey("warped_fungus");
    public static final ResourceKey<Feature> WARPED_FUNGUS_PLANTED = FeatureUtils.createKey("warped_fungus_planted");
    public static final ResourceKey<Feature> HUGE_BROWN_MUSHROOM = FeatureUtils.createKey("huge_brown_mushroom");
    public static final ResourceKey<Feature> HUGE_RED_MUSHROOM = FeatureUtils.createKey("huge_red_mushroom");
    public static final ResourceKey<Feature> OAK = FeatureUtils.createKey("oak");
    public static final ResourceKey<Feature> DARK_OAK = FeatureUtils.createKey("dark_oak");
    public static final ResourceKey<Feature> PALE_OAK = FeatureUtils.createKey("pale_oak");
    public static final ResourceKey<Feature> PALE_OAK_BONEMEAL = FeatureUtils.createKey("pale_oak_bonemeal");
    public static final ResourceKey<Feature> PALE_OAK_CREAKING = FeatureUtils.createKey("pale_oak_creaking");
    public static final ResourceKey<Feature> BIRCH = FeatureUtils.createKey("birch");
    public static final ResourceKey<Feature> ACACIA = FeatureUtils.createKey("acacia");
    public static final ResourceKey<Feature> SPRUCE = FeatureUtils.createKey("spruce");
    public static final ResourceKey<Feature> PINE = FeatureUtils.createKey("pine");
    public static final ResourceKey<Feature> JUNGLE_TREE = FeatureUtils.createKey("jungle_tree");
    public static final ResourceKey<Feature> FANCY_OAK = FeatureUtils.createKey("fancy_oak");
    public static final ResourceKey<Feature> JUNGLE_TREE_NO_VINE = FeatureUtils.createKey("jungle_tree_no_vine");
    public static final ResourceKey<Feature> MEGA_JUNGLE_TREE = FeatureUtils.createKey("mega_jungle_tree");
    public static final ResourceKey<Feature> MEGA_SPRUCE = FeatureUtils.createKey("mega_spruce");
    public static final ResourceKey<Feature> MEGA_PINE = FeatureUtils.createKey("mega_pine");
    public static final ResourceKey<Feature> SUPER_BIRCH_BEES_0002 = FeatureUtils.createKey("super_birch_bees_0002");
    public static final ResourceKey<Feature> SUPER_BIRCH_BEES = FeatureUtils.createKey("super_birch_bees");
    public static final ResourceKey<Feature> SWAMP_OAK = FeatureUtils.createKey("swamp_oak");
    public static final ResourceKey<Feature> JUNGLE_BUSH = FeatureUtils.createKey("jungle_bush");
    public static final ResourceKey<Feature> AZALEA_TREE = FeatureUtils.createKey("azalea_tree");
    public static final ResourceKey<Feature> MANGROVE = FeatureUtils.createKey("mangrove");
    public static final ResourceKey<Feature> TALL_MANGROVE = FeatureUtils.createKey("tall_mangrove");
    public static final ResourceKey<Feature> CHERRY = FeatureUtils.createKey("cherry");
    public static final ResourceKey<Feature> OAK_BEES_0002_LEAF_LITTER = FeatureUtils.createKey("oak_bees_0002_leaf_litter");
    public static final ResourceKey<Feature> OAK_BEES_002 = FeatureUtils.createKey("oak_bees_002");
    public static final ResourceKey<Feature> OAK_BEES_005 = FeatureUtils.createKey("oak_bees_005");
    public static final ResourceKey<Feature> BIRCH_BEES_0002 = FeatureUtils.createKey("birch_bees_0002");
    public static final ResourceKey<Feature> BIRCH_BEES_0002_LEAF_LITTER = FeatureUtils.createKey("birch_bees_0002_leaf_litter");
    public static final ResourceKey<Feature> BIRCH_BEES_002 = FeatureUtils.createKey("birch_bees_002");
    public static final ResourceKey<Feature> BIRCH_BEES_005 = FeatureUtils.createKey("birch_bees_005");
    public static final ResourceKey<Feature> FANCY_OAK_BEES_0002_LEAF_LITTER = FeatureUtils.createKey("fancy_oak_bees_0002_leaf_litter");
    public static final ResourceKey<Feature> FANCY_OAK_BEES_002 = FeatureUtils.createKey("fancy_oak_bees_002");
    public static final ResourceKey<Feature> FANCY_OAK_BEES_005 = FeatureUtils.createKey("fancy_oak_bees_005");
    public static final ResourceKey<Feature> FANCY_OAK_BEES = FeatureUtils.createKey("fancy_oak_bees");
    public static final ResourceKey<Feature> CHERRY_BEES_005 = FeatureUtils.createKey("cherry_bees_005");
    public static final ResourceKey<Feature> OAK_LEAF_LITTER = FeatureUtils.createKey("oak_leaf_litter");
    public static final ResourceKey<Feature> DARK_OAK_LEAF_LITTER = FeatureUtils.createKey("dark_oak_leaf_litter");
    public static final ResourceKey<Feature> BIRCH_LEAF_LITTER = FeatureUtils.createKey("birch_leaf_litter");
    public static final ResourceKey<Feature> FANCY_OAK_LEAF_LITTER = FeatureUtils.createKey("fancy_oak_leaf_litter");
    public static final ResourceKey<Feature> RED_POPLAR = FeatureUtils.createKey("red_poplar");
    public static final ResourceKey<Feature> ORANGE_POPLAR = FeatureUtils.createKey("orange_poplar");
    public static final ResourceKey<Feature> YELLOW_POPLAR = FeatureUtils.createKey("yellow_poplar");
    public static final ResourceKey<Feature> RED_POPLAR_LEAF_LITTER = FeatureUtils.createKey("red_poplar_leaf_litter");
    public static final ResourceKey<Feature> ORANGE_POPLAR_LEAF_LITTER = FeatureUtils.createKey("orange_poplar_leaf_litter");
    public static final ResourceKey<Feature> YELLOW_POPLAR_LEAF_LITTER = FeatureUtils.createKey("yellow_poplar_leaf_litter");
    public static final ResourceKey<Feature> FALLEN_OAK_TREE = FeatureUtils.createKey("fallen_oak_tree");
    public static final ResourceKey<Feature> FALLEN_JUNGLE_TREE = FeatureUtils.createKey("fallen_jungle_tree");
    public static final ResourceKey<Feature> FALLEN_SPRUCE_TREE = FeatureUtils.createKey("fallen_spruce_tree");
    public static final ResourceKey<Feature> FALLEN_BIRCH_TREE = FeatureUtils.createKey("fallen_birch_tree");
    public static final ResourceKey<Feature> FALLEN_SUPER_BIRCH_TREE = FeatureUtils.createKey("fallen_super_birch_tree");
    public static final ResourceKey<Feature> FALLEN_POPLAR_TREE = FeatureUtils.createKey("fallen_poplar_tree");

    private static TreeFeature.Builder createStraightBlobTree(Block oakLog, Block oakLeaves, int baseHeight, int heightRandA, int heightRandB, int blobRadius, Holder<BlockStateProvider> belowTrunkProvider) {
        return new TreeFeature.Builder(BlockStateProvider.of(oakLog), new StraightTrunkPlacer(baseHeight, heightRandA, heightRandB), BlockStateProvider.of(oakLeaves), new BlobFoliagePlacer(ConstantInt.of(blobRadius), ConstantInt.of(0), 3), new TwoLayersFeatureSize(1, 0, 1), belowTrunkProvider);
    }

    private static TreeFeature.Builder createOak(Holder<BlockStateProvider> belowTrunkProvider) {
        return TreeFeatures.createStraightBlobTree(Blocks.OAK_LOG, Blocks.OAK_LEAVES, 4, 2, 0, 2, belowTrunkProvider).ignoreVines();
    }

    private static TreeFeature.Builder createDarkOak(Holder<BlockStateProvider> belowTrunkProvider) {
        return new TreeFeature.Builder(BlockStateProvider.of(Blocks.DARK_OAK_LOG), new DarkOakTrunkPlacer(6, 2, 1), BlockStateProvider.of(Blocks.DARK_OAK_LEAVES), new DarkOakFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0)), new ThreeLayersFeatureSize(1, 1, 0, 1, 2, OptionalInt.empty()), belowTrunkProvider);
    }

    private static FallenTreeFeature.Builder createFallenOak() {
        return TreeFeatures.createFallenTrees(Blocks.OAK_LOG, 4, 7).stumpDecorator(TrunkVineDecorator.INSTANCE);
    }

    private static FallenTreeFeature.Builder createFallenBirch(int maxHeight) {
        return TreeFeatures.createFallenTrees(Blocks.BIRCH_LOG, 5, maxHeight);
    }

    private static FallenTreeFeature.Builder createFallenJungle() {
        return TreeFeatures.createFallenTrees(Blocks.JUNGLE_LOG, 4, 11).stumpDecorator(TrunkVineDecorator.INSTANCE);
    }

    private static FallenTreeFeature.Builder createFallenSpruce() {
        return TreeFeatures.createFallenTrees(Blocks.SPRUCE_LOG, 6, 10);
    }

    private static FallenTreeFeature.Builder createFallenPoplar() {
        return FallenTreeFeature.builder(BlockStateProvider.of(Blocks.POPLAR_LOG), UniformInt.of(4, 7)).logDecorator(new AttachedToLogsDecorator(0.1f, BlockStateProvider.holderOf(Blocks.BROWN_MUSHROOM), List.of(Direction.UP))).logDecorator(new ShelfMushroomDecorator(0.8f));
    }

    private static FallenTreeFeature.Builder createFallenTrees(Block logBlock, int minLength, int maxLength) {
        return FallenTreeFeature.builder(BlockStateProvider.of(logBlock), UniformInt.of(minLength, maxLength)).logDecorator(new AttachedToLogsDecorator(0.1f, Holder.direct(new WeightedStateProvider(WeightedList.builder().add(Blocks.RED_MUSHROOM.defaultBlockState(), 2).add(Blocks.BROWN_MUSHROOM.defaultBlockState(), 1))), List.of(Direction.UP)));
    }

    private static TreeFeature.Builder createBirch(Holder<BlockStateProvider> belowTrunkProvider) {
        return TreeFeatures.createStraightBlobTree(Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES, 5, 2, 0, 2, belowTrunkProvider).ignoreVines();
    }

    private static TreeFeature.Builder createSuperBirch(Holder<BlockStateProvider> belowTrunkProvider) {
        return TreeFeatures.createStraightBlobTree(Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES, 5, 2, 6, 2, belowTrunkProvider).ignoreVines();
    }

    private static TreeFeature.Builder createJungleTree(Holder<BlockStateProvider> belowTrunkProvider) {
        return TreeFeatures.createStraightBlobTree(Blocks.JUNGLE_LOG, Blocks.JUNGLE_LEAVES, 4, 8, 0, 2, belowTrunkProvider);
    }

    private static TreeFeature.Builder createFancyOak(Holder<BlockStateProvider> belowTrunkProvider) {
        return new TreeFeature.Builder(BlockStateProvider.of(Blocks.OAK_LOG), new FancyTrunkPlacer(3, 11, 0), BlockStateProvider.of(Blocks.OAK_LEAVES), new FancyFoliagePlacer(ConstantInt.of(2), ConstantInt.of(4), 4), new TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(4)), belowTrunkProvider).ignoreVines();
    }

    private static TreeFeature.Builder cherry(Holder<BlockStateProvider> belowTrunkProvider) {
        return new TreeFeature.Builder(BlockStateProvider.of(Blocks.CHERRY_LOG), new CherryTrunkPlacer(7, 1, 0, new WeightedListInt(WeightedList.builder().add(ConstantInt.of(1), 1).add(ConstantInt.of(2), 1).add(ConstantInt.of(3), 1).build()), UniformInt.of(2, 4), UniformInt.of(-4, -3), UniformInt.of(-1, 0)), BlockStateProvider.of(Blocks.CHERRY_LEAVES), new CherryFoliagePlacer(ConstantInt.of(4), ConstantInt.of(0), ConstantInt.of(5), 0.25f, 0.5f, 0.16666667f, 0.33333334f), new TwoLayersFeatureSize(1, 0, 2), belowTrunkProvider).ignoreVines();
    }

    private static TreeFeature.Builder createPoplar(Block leafBlock, Holder<BlockStateProvider> belowTrunkProvider) {
        return new TreeFeature.Builder(BlockStateProvider.of(Blocks.POPLAR_LOG), new PoplarTrunkPlacer(7, 4, 0, ConstantInt.of(4), UniformInt.of(1, 4)), BlockStateProvider.of(leafBlock), new PoplarFoliagePlacer(new WeightedListInt(WeightedList.builder().add(ConstantInt.of(5), 5).add(ConstantInt.of(6), 5).add(ConstantInt.of(7), 1).add(ConstantInt.of(8), 1).build()), ConstantInt.of(0), UniformInt.of(5, 6), 0.15f), new TwoLayersFeatureSize(1, 0, 2), belowTrunkProvider).ignoreVines();
    }

    public static void bootstrap(BootstrapContext<Feature> context) {
        HolderGetter<Block> blocks = context.lookup(Registries.BLOCK);
        HolderGetter<BlockStateProvider> blockStateProviders = context.lookup(Registries.BLOCK_STATE_PROVIDER);
        BlockPredicate stemReplaceableBlocks = BlockPredicate.matchesBlocks(Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.BIRCH_SAPLING, Blocks.JUNGLE_SAPLING, Blocks.ACACIA_SAPLING, Blocks.CHERRY_SAPLING, Blocks.DARK_OAK_SAPLING, Blocks.PALE_OAK_SAPLING, Blocks.MANGROVE_PROPAGULE, Blocks.DANDELION, Blocks.TORCHFLOWER, Blocks.POPPY, Blocks.BLUE_ORCHID, Blocks.ALLIUM, Blocks.AZURE_BLUET, Blocks.RED_TULIP, Blocks.ORANGE_TULIP, Blocks.WHITE_TULIP, Blocks.PINK_TULIP, Blocks.OXEYE_DAISY, Blocks.CORNFLOWER, Blocks.WITHER_ROSE, Blocks.LILY_OF_THE_VALLEY, Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM, Blocks.WHEAT, Blocks.SUGAR_CANE, Blocks.ATTACHED_PUMPKIN_STEM, Blocks.ATTACHED_MELON_STEM, Blocks.PUMPKIN_STEM, Blocks.MELON_STEM, Blocks.LILY_PAD, Blocks.NETHER_WART, Blocks.COCOA, Blocks.CARROTS, Blocks.POTATOES, Blocks.CHORUS_PLANT, Blocks.CHORUS_FLOWER, Blocks.TORCHFLOWER_CROP, Blocks.PITCHER_CROP, Blocks.BEETROOTS, Blocks.SWEET_BERRY_BUSH, Blocks.WARPED_FUNGUS, Blocks.CRIMSON_FUNGUS, Blocks.WEEPING_VINES, Blocks.WEEPING_VINES_PLANT, Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, Blocks.CAVE_VINES, Blocks.CAVE_VINES_PLANT, Blocks.SPORE_BLOSSOM, Blocks.AZALEA, Blocks.FLOWERING_AZALEA, Blocks.MOSS_CARPET, Blocks.PINK_PETALS, Blocks.WILDFLOWERS, Blocks.BIG_DRIPLEAF, Blocks.BIG_DRIPLEAF_STEM, Blocks.SMALL_DRIPLEAF);
        context.register(CRIMSON_FUNGUS, new HugeFungusFeature(Blocks.CRIMSON_NYLIUM.defaultBlockState(), Blocks.CRIMSON_STEM.defaultBlockState(), Blocks.NETHER_WART_BLOCK.defaultBlockState(), Blocks.SHROOMLIGHT.defaultBlockState(), stemReplaceableBlocks, false));
        context.register(CRIMSON_FUNGUS_PLANTED, new HugeFungusFeature(Blocks.CRIMSON_NYLIUM.defaultBlockState(), Blocks.CRIMSON_STEM.defaultBlockState(), Blocks.NETHER_WART_BLOCK.defaultBlockState(), Blocks.SHROOMLIGHT.defaultBlockState(), stemReplaceableBlocks, true));
        context.register(WARPED_FUNGUS, new HugeFungusFeature(Blocks.WARPED_NYLIUM.defaultBlockState(), Blocks.WARPED_STEM.defaultBlockState(), Blocks.WARPED_WART_BLOCK.defaultBlockState(), Blocks.SHROOMLIGHT.defaultBlockState(), stemReplaceableBlocks, false));
        context.register(WARPED_FUNGUS_PLANTED, new HugeFungusFeature(Blocks.WARPED_NYLIUM.defaultBlockState(), Blocks.WARPED_STEM.defaultBlockState(), Blocks.WARPED_WART_BLOCK.defaultBlockState(), Blocks.SHROOMLIGHT.defaultBlockState(), stemReplaceableBlocks, true));
        context.register(HUGE_BROWN_MUSHROOM, new HugeBrownMushroomFeature(BlockStateProvider.holderOf((BlockState)((BlockState)Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState().setValue(HugeMushroomBlock.UP, true)).setValue(HugeMushroomBlock.DOWN, false)), BlockStateProvider.holderOf((BlockState)((BlockState)Blocks.MUSHROOM_STEM.defaultBlockState().setValue(HugeMushroomBlock.UP, false)).setValue(HugeMushroomBlock.DOWN, false)), 3, BlockPredicate.matchesTag(BlockTags.HUGE_BROWN_MUSHROOM_CAN_PLACE_ON)));
        context.register(HUGE_RED_MUSHROOM, new HugeRedMushroomFeature(BlockStateProvider.holderOf((BlockState)Blocks.RED_MUSHROOM_BLOCK.defaultBlockState().setValue(HugeMushroomBlock.DOWN, false)), BlockStateProvider.holderOf((BlockState)((BlockState)Blocks.MUSHROOM_STEM.defaultBlockState().setValue(HugeMushroomBlock.UP, false)).setValue(HugeMushroomBlock.DOWN, false)), 2, BlockPredicate.matchesTag(BlockTags.HUGE_RED_MUSHROOM_CAN_PLACE_ON)));
        BeehiveDecorator beehive0002 = new BeehiveDecorator(0.002f);
        BeehiveDecorator beehive001 = new BeehiveDecorator(0.01f);
        BeehiveDecorator beehive002 = new BeehiveDecorator(0.02f);
        BeehiveDecorator beehive005 = new BeehiveDecorator(0.05f);
        BeehiveDecorator beehive = new BeehiveDecorator(1.0f);
        PlaceOnGroundDecorator sparseLeafLitter = new PlaceOnGroundDecorator(96, 4, 2, Holder.direct(new WeightedStateProvider(VegetationFeatures.leafLitterPatchBuilder(1, 3))));
        PlaceOnGroundDecorator thickLeafLitter = new PlaceOnGroundDecorator(150, 2, 2, Holder.direct(new WeightedStateProvider(VegetationFeatures.leafLitterPatchBuilder(1, 4))));
        Holder.Reference<BlockStateProvider> belowTrunkProvider = blockStateProviders.getOrThrow(BlockStateProviders.SOIL_BENEATH_TREE);
        context.register(OAK, TreeFeatures.createOak(belowTrunkProvider).build());
        context.register(DARK_OAK, TreeFeatures.createDarkOak(belowTrunkProvider).ignoreVines().build());
        context.register(PALE_OAK, new TreeFeature.Builder(BlockStateProvider.of(Blocks.PALE_OAK_LOG), new DarkOakTrunkPlacer(6, 2, 1), BlockStateProvider.of(Blocks.PALE_OAK_LEAVES), new DarkOakFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0)), new ThreeLayersFeatureSize(1, 1, 0, 1, 2, OptionalInt.empty()), belowTrunkProvider).decorators(List.of(new PaleMossDecorator(0.15f, 0.4f, 0.8f))).ignoreVines().build());
        context.register(PALE_OAK_BONEMEAL, new TreeFeature.Builder(BlockStateProvider.of(Blocks.PALE_OAK_LOG), new DarkOakTrunkPlacer(6, 2, 1), BlockStateProvider.of(Blocks.PALE_OAK_LEAVES), new DarkOakFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0)), new ThreeLayersFeatureSize(1, 1, 0, 1, 2, OptionalInt.empty()), belowTrunkProvider).ignoreVines().build());
        context.register(PALE_OAK_CREAKING, new TreeFeature.Builder(BlockStateProvider.of(Blocks.PALE_OAK_LOG), new DarkOakTrunkPlacer(6, 2, 1), BlockStateProvider.of(Blocks.PALE_OAK_LEAVES), new DarkOakFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0)), new ThreeLayersFeatureSize(1, 1, 0, 1, 2, OptionalInt.empty()), belowTrunkProvider).decorators(List.of(new PaleMossDecorator(0.15f, 0.4f, 0.8f), new CreakingHeartDecorator(1.0f))).ignoreVines().build());
        context.register(BIRCH, TreeFeatures.createBirch(belowTrunkProvider).build());
        context.register(ACACIA, new TreeFeature.Builder(BlockStateProvider.of(Blocks.ACACIA_LOG), new ForkingTrunkPlacer(5, 2, 2), BlockStateProvider.of(Blocks.ACACIA_LEAVES), new AcaciaFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0)), new TwoLayersFeatureSize(1, 0, 2), belowTrunkProvider).ignoreVines().build());
        context.register(CHERRY, TreeFeatures.cherry(belowTrunkProvider).build());
        context.register(CHERRY_BEES_005, TreeFeatures.cherry(belowTrunkProvider).decorators(List.of(beehive005)).build());
        context.register(SPRUCE, new TreeFeature.Builder(BlockStateProvider.of(Blocks.SPRUCE_LOG), new StraightTrunkPlacer(5, 2, 1), BlockStateProvider.of(Blocks.SPRUCE_LEAVES), new SpruceFoliagePlacer(UniformInt.of(2, 3), UniformInt.of(0, 2), UniformInt.of(1, 2)), new TwoLayersFeatureSize(2, 0, 2), belowTrunkProvider).ignoreVines().build());
        context.register(PINE, new TreeFeature.Builder(BlockStateProvider.of(Blocks.SPRUCE_LOG), new StraightTrunkPlacer(6, 4, 0), BlockStateProvider.of(Blocks.SPRUCE_LEAVES), new PineFoliagePlacer(ConstantInt.of(1), ConstantInt.of(1), UniformInt.of(3, 4)), new TwoLayersFeatureSize(2, 0, 2), belowTrunkProvider).ignoreVines().build());
        context.register(JUNGLE_TREE, TreeFeatures.createJungleTree(belowTrunkProvider).decorators(List.of(new CocoaDecorator(0.2f), TrunkVineDecorator.INSTANCE, new LeaveVineDecorator(0.25f))).ignoreVines().build());
        context.register(FANCY_OAK, TreeFeatures.createFancyOak(belowTrunkProvider).build());
        context.register(JUNGLE_TREE_NO_VINE, TreeFeatures.createJungleTree(belowTrunkProvider).ignoreVines().build());
        context.register(MEGA_JUNGLE_TREE, new TreeFeature.Builder(BlockStateProvider.of(Blocks.JUNGLE_LOG), new MegaJungleTrunkPlacer(10, 2, 19), BlockStateProvider.of(Blocks.JUNGLE_LEAVES), new MegaJungleFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 2), new TwoLayersFeatureSize(1, 1, 2), belowTrunkProvider).decorators(List.of(TrunkVineDecorator.INSTANCE, new LeaveVineDecorator(0.25f))).build());
        Holder.Reference<BlockStateProvider> podzol = blockStateProviders.getOrThrow(BlockStateProviders.PODZOL_BENEATH_TREE);
        context.register(MEGA_SPRUCE, new TreeFeature.Builder(BlockStateProvider.of(Blocks.SPRUCE_LOG), new GiantTrunkPlacer(13, 2, 14), BlockStateProvider.of(Blocks.SPRUCE_LEAVES), new MegaPineFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0), UniformInt.of(13, 17)), new TwoLayersFeatureSize(1, 1, 2), belowTrunkProvider).decorators(List.of(new AlterGroundDecorator(podzol))).build());
        context.register(MEGA_PINE, new TreeFeature.Builder(BlockStateProvider.of(Blocks.SPRUCE_LOG), new GiantTrunkPlacer(13, 2, 14), BlockStateProvider.of(Blocks.SPRUCE_LEAVES), new MegaPineFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0), UniformInt.of(3, 7)), new TwoLayersFeatureSize(1, 1, 2), belowTrunkProvider).decorators(List.of(new AlterGroundDecorator(podzol))).build());
        context.register(SUPER_BIRCH_BEES_0002, TreeFeatures.createSuperBirch(belowTrunkProvider).decorators(List.of(beehive0002)).build());
        context.register(SUPER_BIRCH_BEES, TreeFeatures.createSuperBirch(belowTrunkProvider).decorators(List.of(beehive)).build());
        context.register(SWAMP_OAK, TreeFeatures.createStraightBlobTree(Blocks.OAK_LOG, Blocks.OAK_LEAVES, 5, 3, 0, 3, belowTrunkProvider).decorators(List.of(new LeaveVineDecorator(0.25f))).build());
        context.register(JUNGLE_BUSH, new TreeFeature.Builder(BlockStateProvider.of(Blocks.JUNGLE_LOG), new StraightTrunkPlacer(1, 0, 0), BlockStateProvider.of(Blocks.OAK_LEAVES), new BushFoliagePlacer(ConstantInt.of(2), ConstantInt.of(1), 2), new TwoLayersFeatureSize(0, 0, 0), belowTrunkProvider).build());
        context.register(AZALEA_TREE, new TreeFeature.Builder(BlockStateProvider.of(Blocks.OAK_LOG), new BendingTrunkPlacer(4, 2, 0, 3, UniformInt.of(1, 2)), new WeightedStateProvider(WeightedList.builder().add(Blocks.AZALEA_LEAVES.defaultBlockState(), 3).add(Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState(), 1)), new RandomSpreadFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0), ConstantInt.of(2), 50), new TwoLayersFeatureSize(1, 0, 1), belowTrunkProvider).belowTrunkProvider(BlockStateProvider.holderOf(Blocks.ROOTED_DIRT)).build());
        context.register(MANGROVE, new TreeFeature.Builder(BlockStateProvider.of(Blocks.MANGROVE_LOG), (TrunkPlacer)new UpwardsBranchingTrunkPlacer(2, 1, 4, UniformInt.of(1, 4), 0.5f, UniformInt.of(0, 1), blocks.getOrThrow(BlockTags.MANGROVE_LOGS_CAN_GROW_THROUGH)), BlockStateProvider.of(Blocks.MANGROVE_LEAVES), (FoliagePlacer)new RandomSpreadFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0), ConstantInt.of(2), 70), Optional.of(new MangroveRootPlacer(UniformInt.of(1, 3), BlockStateProvider.holderOf(Blocks.MANGROVE_ROOTS), Optional.of(new AboveRootPlacement(BlockStateProvider.holderOf(Blocks.MOSS_CARPET), 0.5f)), new MangroveRootPlacement(blocks.getOrThrow(BlockTags.MANGROVE_ROOTS_CAN_GROW_THROUGH), HolderSet.direct(Block::builtInRegistryHolder, Blocks.MUD, Blocks.MUDDY_MANGROVE_ROOTS), BlockStateProvider.holderOf(Blocks.MUDDY_MANGROVE_ROOTS), 8, 15, 0.2f))), (FeatureSize)new TwoLayersFeatureSize(2, 0, 2), belowTrunkProvider).decorators(List.of(new LeaveVineDecorator(0.125f), new AttachedToLeavesDecorator(0.14f, 1, 0, blockStateProviders.getOrThrow(BlockStateProviders.MANGROVE_PROPAGULE), 2, List.of(Direction.DOWN)), beehive001)).ignoreVines().build());
        context.register(TALL_MANGROVE, new TreeFeature.Builder(BlockStateProvider.of(Blocks.MANGROVE_LOG), (TrunkPlacer)new UpwardsBranchingTrunkPlacer(4, 1, 9, UniformInt.of(1, 6), 0.5f, UniformInt.of(0, 1), blocks.getOrThrow(BlockTags.MANGROVE_LOGS_CAN_GROW_THROUGH)), BlockStateProvider.of(Blocks.MANGROVE_LEAVES), (FoliagePlacer)new RandomSpreadFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0), ConstantInt.of(2), 70), Optional.of(new MangroveRootPlacer(UniformInt.of(3, 7), BlockStateProvider.holderOf(Blocks.MANGROVE_ROOTS), Optional.of(new AboveRootPlacement(BlockStateProvider.holderOf(Blocks.MOSS_CARPET), 0.5f)), new MangroveRootPlacement(blocks.getOrThrow(BlockTags.MANGROVE_ROOTS_CAN_GROW_THROUGH), HolderSet.direct(Block::builtInRegistryHolder, Blocks.MUD, Blocks.MUDDY_MANGROVE_ROOTS), BlockStateProvider.holderOf(Blocks.MUDDY_MANGROVE_ROOTS), 8, 15, 0.2f))), (FeatureSize)new TwoLayersFeatureSize(3, 0, 2), belowTrunkProvider).decorators(List.of(new LeaveVineDecorator(0.125f), new AttachedToLeavesDecorator(0.14f, 1, 0, blockStateProviders.getOrThrow(BlockStateProviders.MANGROVE_PROPAGULE), 2, List.of(Direction.DOWN)), beehive001)).ignoreVines().build());
        context.register(OAK_BEES_0002_LEAF_LITTER, TreeFeatures.createOak(belowTrunkProvider).decorators(List.of(beehive0002, sparseLeafLitter, thickLeafLitter)).build());
        context.register(OAK_BEES_002, TreeFeatures.createOak(belowTrunkProvider).decorators(List.of(beehive002)).build());
        context.register(OAK_BEES_005, TreeFeatures.createOak(belowTrunkProvider).decorators(List.of(beehive005)).build());
        context.register(BIRCH_BEES_0002, TreeFeatures.createBirch(belowTrunkProvider).decorators(List.of(beehive0002)).build());
        context.register(BIRCH_BEES_0002_LEAF_LITTER, TreeFeatures.createBirch(belowTrunkProvider).decorators(List.of(beehive0002, sparseLeafLitter, thickLeafLitter)).build());
        context.register(BIRCH_BEES_002, TreeFeatures.createBirch(belowTrunkProvider).decorators(List.of(beehive002)).build());
        context.register(BIRCH_BEES_005, TreeFeatures.createBirch(belowTrunkProvider).decorators(List.of(beehive005)).build());
        context.register(FANCY_OAK_BEES_0002_LEAF_LITTER, TreeFeatures.createFancyOak(belowTrunkProvider).decorators(List.of(beehive0002, sparseLeafLitter, thickLeafLitter)).build());
        context.register(FANCY_OAK_BEES_002, TreeFeatures.createFancyOak(belowTrunkProvider).decorators(List.of(beehive002)).build());
        context.register(FANCY_OAK_BEES_005, TreeFeatures.createFancyOak(belowTrunkProvider).decorators(List.of(beehive005)).build());
        context.register(FANCY_OAK_BEES, TreeFeatures.createFancyOak(belowTrunkProvider).decorators(List.of(beehive)).build());
        context.register(OAK_LEAF_LITTER, TreeFeatures.createOak(belowTrunkProvider).decorators(List.of(sparseLeafLitter, thickLeafLitter)).build());
        context.register(DARK_OAK_LEAF_LITTER, TreeFeatures.createDarkOak(belowTrunkProvider).ignoreVines().decorators(List.of(sparseLeafLitter, thickLeafLitter)).build());
        context.register(BIRCH_LEAF_LITTER, TreeFeatures.createBirch(belowTrunkProvider).decorators(List.of(sparseLeafLitter, thickLeafLitter)).build());
        context.register(FANCY_OAK_LEAF_LITTER, TreeFeatures.createFancyOak(belowTrunkProvider).decorators(List.of(sparseLeafLitter, thickLeafLitter)).build());
        context.register(RED_POPLAR, TreeFeatures.createPoplar(Blocks.RED_POPLAR_LEAVES, belowTrunkProvider).decorators(List.of(new ShelfMushroomDecorator(0.4f))).build());
        context.register(ORANGE_POPLAR, TreeFeatures.createPoplar(Blocks.ORANGE_POPLAR_LEAVES, belowTrunkProvider).decorators(List.of(new ShelfMushroomDecorator(0.4f))).build());
        context.register(YELLOW_POPLAR, TreeFeatures.createPoplar(Blocks.YELLOW_POPLAR_LEAVES, belowTrunkProvider).decorators(List.of(new ShelfMushroomDecorator(0.4f))).build());
        context.register(RED_POPLAR_LEAF_LITTER, TreeFeatures.createPoplar(Blocks.RED_POPLAR_LEAVES, belowTrunkProvider).decorators(List.of(sparseLeafLitter, thickLeafLitter, new ShelfMushroomDecorator(0.4f))).build());
        context.register(ORANGE_POPLAR_LEAF_LITTER, TreeFeatures.createPoplar(Blocks.ORANGE_POPLAR_LEAVES, belowTrunkProvider).decorators(List.of(sparseLeafLitter, thickLeafLitter, new ShelfMushroomDecorator(0.4f))).build());
        context.register(YELLOW_POPLAR_LEAF_LITTER, TreeFeatures.createPoplar(Blocks.YELLOW_POPLAR_LEAVES, belowTrunkProvider).decorators(List.of(sparseLeafLitter, thickLeafLitter, new ShelfMushroomDecorator(0.4f))).build());
        context.register(FALLEN_OAK_TREE, TreeFeatures.createFallenOak().build());
        context.register(FALLEN_BIRCH_TREE, TreeFeatures.createFallenBirch(8).build());
        context.register(FALLEN_SUPER_BIRCH_TREE, TreeFeatures.createFallenBirch(15).build());
        context.register(FALLEN_JUNGLE_TREE, TreeFeatures.createFallenJungle().build());
        context.register(FALLEN_SPRUCE_TREE, TreeFeatures.createFallenSpruce().build());
        context.register(FALLEN_POPLAR_TREE, TreeFeatures.createFallenPoplar().build());
    }
}

