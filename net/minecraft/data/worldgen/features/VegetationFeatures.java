/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.worldgen.features;

import java.util.List;
import net.minecraft.core.CompositeDirection;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BlockStateProviders;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.TreePlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.BiasedToBottomInt;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.util.valueproviders.WeightedListInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBedBlock;
import net.minecraft.world.level.block.LeafLitterBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.BambooFeature;
import net.minecraft.world.level.levelgen.feature.BlockColumnFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.RandomBooleanSelectorFeature;
import net.minecraft.world.level.levelgen.feature.RandomSelectorFeature;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import net.minecraft.world.level.levelgen.feature.SimpleRandomSelectorFeature;
import net.minecraft.world.level.levelgen.feature.VegetationPatchFeature;
import net.minecraft.world.level.levelgen.feature.VinesFeature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.WeightedRandomSelectorFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.OffsetPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.material.Fluids;

public class VegetationFeatures {
    public static final ResourceKey<Feature> BAMBOO_NO_PODZOL = FeatureUtils.createKey("bamboo_no_podzol");
    public static final ResourceKey<Feature> BAMBOO_SOME_PODZOL = FeatureUtils.createKey("bamboo_some_podzol");
    public static final ResourceKey<Feature> VINES = FeatureUtils.createKey("vines");
    public static final ResourceKey<Feature> BROWN_MUSHROOM = FeatureUtils.createKey("brown_mushroom");
    public static final ResourceKey<Feature> RED_MUSHROOM = FeatureUtils.createKey("red_mushroom");
    public static final ResourceKey<Feature> SUNFLOWER = FeatureUtils.createKey("sunflower");
    public static final ResourceKey<Feature> PUMPKIN = FeatureUtils.createKey("pumpkin");
    public static final ResourceKey<Feature> BERRY_BUSH = FeatureUtils.createKey("berry_bush");
    public static final ResourceKey<Feature> TAIGA_GRASS = FeatureUtils.createKey("taiga_grass");
    public static final ResourceKey<Feature> GRASS = FeatureUtils.createKey("grass");
    public static final ResourceKey<Feature> GRASS_JUNGLE = FeatureUtils.createKey("grass_jungle");
    public static final ResourceKey<Feature> DEAD_BUSH = FeatureUtils.createKey("dead_bush");
    public static final ResourceKey<Feature> DRY_GRASS = FeatureUtils.createKey("dry_grass");
    public static final ResourceKey<Feature> MELON = FeatureUtils.createKey("melon");
    public static final ResourceKey<Feature> WATERLILY = FeatureUtils.createKey("waterlily");
    public static final ResourceKey<Feature> TALL_GRASS = FeatureUtils.createKey("tall_grass");
    public static final ResourceKey<Feature> LARGE_FERN = FeatureUtils.createKey("large_fern");
    public static final ResourceKey<Feature> BUSH = FeatureUtils.createKey("bush");
    public static final ResourceKey<Feature> RED_SHRUB = FeatureUtils.createKey("red_shrub");
    public static final ResourceKey<Feature> LEAF_LITTER = FeatureUtils.createKey("leaf_litter");
    public static final ResourceKey<Feature> FIREFLY_BUSH = FeatureUtils.createKey("firefly_bush");
    public static final ResourceKey<Feature> CACTUS = FeatureUtils.createKey("cactus");
    public static final ResourceKey<Feature> SUGAR_CANE = FeatureUtils.createKey("sugar_cane");
    public static final ResourceKey<Feature> FLOWER_DEFAULT = FeatureUtils.createKey("flower_default");
    public static final ResourceKey<Feature> FLOWER_FLOWER_FOREST = FeatureUtils.createKey("flower_flower_forest");
    public static final ResourceKey<Feature> FLOWER_SWAMP = FeatureUtils.createKey("flower_swamp");
    public static final ResourceKey<Feature> FLOWER_PLAIN = FeatureUtils.createKey("flower_plain");
    public static final ResourceKey<Feature> FLOWER_MEADOW = FeatureUtils.createKey("flower_meadow");
    public static final ResourceKey<Feature> FLOWER_CHERRY = FeatureUtils.createKey("flower_cherry");
    public static final ResourceKey<Feature> FLOWER_PALE_GARDEN = FeatureUtils.createKey("flower_pale_garden");
    public static final ResourceKey<Feature> WILDFLOWER = FeatureUtils.createKey("wildflower");
    public static final ResourceKey<Feature> FOREST_FLOWERS = FeatureUtils.createKey("forest_flowers");
    public static final ResourceKey<Feature> PALE_FOREST_FLOWER = FeatureUtils.createKey("pale_forest_flower");
    public static final ResourceKey<Feature> DARK_FOREST_VEGETATION = FeatureUtils.createKey("dark_forest_vegetation");
    public static final ResourceKey<Feature> PALE_GARDEN_VEGETATION = FeatureUtils.createKey("pale_garden_vegetation");
    public static final ResourceKey<Feature> PALE_MOSS_VEGETATION = FeatureUtils.createKey("pale_moss_vegetation");
    public static final ResourceKey<Feature> PALE_MOSS_PATCH = FeatureUtils.createKey("pale_moss_patch");
    public static final ResourceKey<Feature> PALE_MOSS_PATCH_BONEMEAL = FeatureUtils.createKey("pale_moss_patch_bonemeal");
    public static final ResourceKey<Feature> TREES_FLOWER_FOREST = FeatureUtils.createKey("trees_flower_forest");
    public static final ResourceKey<Feature> MEADOW_TREES = FeatureUtils.createKey("meadow_trees");
    public static final ResourceKey<Feature> TREES_TAIGA = FeatureUtils.createKey("trees_taiga");
    public static final ResourceKey<Feature> TREES_BADLANDS = FeatureUtils.createKey("trees_badlands");
    public static final ResourceKey<Feature> TREES_GROVE = FeatureUtils.createKey("trees_grove");
    public static final ResourceKey<Feature> TREES_SAVANNA = FeatureUtils.createKey("trees_savanna");
    public static final ResourceKey<Feature> TREES_SNOWY = FeatureUtils.createKey("trees_snowy");
    public static final ResourceKey<Feature> TREES_BIRCH = FeatureUtils.createKey("trees_birch");
    public static final ResourceKey<Feature> BIRCH_TALL = FeatureUtils.createKey("birch_tall");
    public static final ResourceKey<Feature> TREES_WINDSWEPT_HILLS = FeatureUtils.createKey("trees_windswept_hills");
    public static final ResourceKey<Feature> TREES_WATER = FeatureUtils.createKey("trees_water");
    public static final ResourceKey<Feature> TREES_BIRCH_AND_OAK_LEAF_LITTER = FeatureUtils.createKey("trees_birch_and_oak_leaf_litter");
    public static final ResourceKey<Feature> TREES_PLAINS = FeatureUtils.createKey("trees_plains");
    public static final ResourceKey<Feature> TREES_SPARSE_JUNGLE = FeatureUtils.createKey("trees_sparse_jungle");
    public static final ResourceKey<Feature> TREES_OLD_GROWTH_SPRUCE_TAIGA = FeatureUtils.createKey("trees_old_growth_spruce_taiga");
    public static final ResourceKey<Feature> TREES_OLD_GROWTH_PINE_TAIGA = FeatureUtils.createKey("trees_old_growth_pine_taiga");
    public static final ResourceKey<Feature> TREES_JUNGLE = FeatureUtils.createKey("trees_jungle");
    public static final ResourceKey<Feature> TREES_DAPPLED_FOREST = FeatureUtils.createKey("trees_dappled_forest");
    public static final ResourceKey<Feature> BAMBOO_VEGETATION = FeatureUtils.createKey("bamboo_vegetation");
    public static final ResourceKey<Feature> MUSHROOM_ISLAND_VEGETATION = FeatureUtils.createKey("mushroom_island_vegetation");
    public static final ResourceKey<Feature> MANGROVE_VEGETATION = FeatureUtils.createKey("mangrove_vegetation");
    private static final float FALLEN_TREE_ONE_IN_CHANCE = 80.0f;

    public static void bootstrap(BootstrapContext<Feature> context) {
        HolderGetter<Block> blocks = context.lookup(Registries.BLOCK);
        HolderGetter<Feature> configuredFeatures = context.lookup(Registries.FEATURE);
        HolderGetter<BlockStateProvider> blockStateProviders = context.lookup(Registries.BLOCK_STATE_PROVIDER);
        Holder.Reference<Feature> hugeBrownMushroom = configuredFeatures.getOrThrow(TreeFeatures.HUGE_BROWN_MUSHROOM);
        Holder.Reference<Feature> hugeRedMushroom = configuredFeatures.getOrThrow(TreeFeatures.HUGE_RED_MUSHROOM);
        Holder.Reference<Feature> fancyOakBees005 = configuredFeatures.getOrThrow(TreeFeatures.FANCY_OAK_BEES_005);
        Holder.Reference<Feature> oakBees005 = configuredFeatures.getOrThrow(TreeFeatures.OAK_BEES_005);
        Holder.Reference<Feature> grassJungle = configuredFeatures.getOrThrow(GRASS_JUNGLE);
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        Holder.Reference<PlacedFeature> paleOakChecked = placedFeatures.getOrThrow(TreePlacements.PALE_OAK_CHECKED);
        Holder.Reference<PlacedFeature> paleOakCreakingChecked = placedFeatures.getOrThrow(TreePlacements.PALE_OAK_CREAKING_CHECKED);
        Holder.Reference<PlacedFeature> fancyOakChecked = placedFeatures.getOrThrow(TreePlacements.FANCY_OAK_CHECKED);
        Holder.Reference<PlacedFeature> birchBees002 = placedFeatures.getOrThrow(TreePlacements.BIRCH_BEES_002);
        Holder.Reference<PlacedFeature> fancyOakBees002 = placedFeatures.getOrThrow(TreePlacements.FANCY_OAK_BEES_002);
        Holder.Reference<PlacedFeature> fancyOakBees = placedFeatures.getOrThrow(TreePlacements.FANCY_OAK_BEES);
        Holder.Reference<PlacedFeature> pineChecked = placedFeatures.getOrThrow(TreePlacements.PINE_CHECKED);
        Holder.Reference<PlacedFeature> spruceChecked = placedFeatures.getOrThrow(TreePlacements.SPRUCE_CHECKED);
        Holder.Reference<PlacedFeature> pineOnSnow = placedFeatures.getOrThrow(TreePlacements.PINE_ON_SNOW);
        Holder.Reference<PlacedFeature> acaciaChecked = placedFeatures.getOrThrow(TreePlacements.ACACIA_CHECKED);
        Holder.Reference<PlacedFeature> superBirchBees0002 = placedFeatures.getOrThrow(TreePlacements.SUPER_BIRCH_BEES_0002);
        Holder.Reference<PlacedFeature> birchBees0002Placed = placedFeatures.getOrThrow(TreePlacements.BIRCH_BEES_0002_PLACED);
        Holder.Reference<PlacedFeature> birchBees0002LeafLitter = placedFeatures.getOrThrow(TreePlacements.BIRCH_BEES_0002_LEAF_LITTER);
        Holder.Reference<PlacedFeature> fancyOakBees0002LeafLitter = placedFeatures.getOrThrow(TreePlacements.FANCY_OAK_BEES_0002_LEAF_LITTER);
        Holder.Reference<PlacedFeature> jungleBush = placedFeatures.getOrThrow(TreePlacements.JUNGLE_BUSH);
        Holder.Reference<PlacedFeature> megaSpruceChecked = placedFeatures.getOrThrow(TreePlacements.MEGA_SPRUCE_CHECKED);
        Holder.Reference<PlacedFeature> megaPineChecked = placedFeatures.getOrThrow(TreePlacements.MEGA_PINE_CHECKED);
        Holder.Reference<PlacedFeature> megaJungleTreeChecked = placedFeatures.getOrThrow(TreePlacements.MEGA_JUNGLE_TREE_CHECKED);
        Holder.Reference<PlacedFeature> tallMangroveChecked = placedFeatures.getOrThrow(TreePlacements.TALL_MANGROVE_CHECKED);
        Holder.Reference<PlacedFeature> oakChecked = placedFeatures.getOrThrow(TreePlacements.OAK_CHECKED);
        Holder.Reference<PlacedFeature> oakBees002 = placedFeatures.getOrThrow(TreePlacements.OAK_BEES_002);
        Holder.Reference<PlacedFeature> superBirchBees = placedFeatures.getOrThrow(TreePlacements.SUPER_BIRCH_BEES);
        Holder.Reference<PlacedFeature> spruceOnSnow = placedFeatures.getOrThrow(TreePlacements.SPRUCE_ON_SNOW);
        Holder.Reference<PlacedFeature> oakBees0002LeafLitter = placedFeatures.getOrThrow(TreePlacements.OAK_BEES_0002_LEAF_LITTER);
        Holder.Reference<PlacedFeature> jungleTreeChecked = placedFeatures.getOrThrow(TreePlacements.JUNGLE_TREE_CHECKED);
        Holder.Reference<PlacedFeature> mangroveChecked = placedFeatures.getOrThrow(TreePlacements.MANGROVE_CHECKED);
        Holder.Reference<PlacedFeature> oakLeafLitter = placedFeatures.getOrThrow(TreePlacements.OAK_LEAF_LITTER);
        Holder.Reference<PlacedFeature> darkOakLeafLitter = placedFeatures.getOrThrow(TreePlacements.DARK_OAK_LEAF_LITTER);
        Holder.Reference<PlacedFeature> birchLeafLitter = placedFeatures.getOrThrow(TreePlacements.BIRCH_LEAF_LITTER);
        Holder.Reference<PlacedFeature> fancyOakLeafLitter = placedFeatures.getOrThrow(TreePlacements.FANCY_OAK_LEAF_LITTER);
        Holder.Reference<PlacedFeature> redPoplarLeafLitter = placedFeatures.getOrThrow(TreePlacements.RED_POPLAR_LEAF_LITTER);
        Holder.Reference<PlacedFeature> orangePoplarLeafLitter = placedFeatures.getOrThrow(TreePlacements.ORANGE_POPLAR_LEAF_LITTER);
        Holder.Reference<PlacedFeature> yellowPoplarLeafLitter = placedFeatures.getOrThrow(TreePlacements.YELLOW_POPLAR_LEAF_LITTER);
        Holder.Reference<PlacedFeature> fallenOak = placedFeatures.getOrThrow(TreePlacements.FALLEN_OAK_TREE);
        Holder.Reference<PlacedFeature> fallenBirch = placedFeatures.getOrThrow(TreePlacements.FALLEN_BIRCH_TREE);
        Holder.Reference<PlacedFeature> fallenSuperBirch = placedFeatures.getOrThrow(TreePlacements.FALLEN_SUPER_BIRCH_TREE);
        Holder.Reference<PlacedFeature> fallenJungle = placedFeatures.getOrThrow(TreePlacements.FALLEN_JUNGLE_TREE);
        Holder.Reference<PlacedFeature> fallenSpruce = placedFeatures.getOrThrow(TreePlacements.FALLEN_SPRUCE_TREE);
        Holder.Reference<PlacedFeature> fallenPoplar = placedFeatures.getOrThrow(TreePlacements.FALLEN_POPLAR_TREE);
        context.register(BAMBOO_NO_PODZOL, new BambooFeature(0.0f));
        context.register(BAMBOO_SOME_PODZOL, new BambooFeature(0.2f));
        context.register(VINES, new VinesFeature());
        context.register(BROWN_MUSHROOM, new SimpleBlockFeature(BlockStateProvider.of(Blocks.BROWN_MUSHROOM)));
        context.register(RED_MUSHROOM, new SimpleBlockFeature(BlockStateProvider.of(Blocks.RED_MUSHROOM)));
        context.register(SUNFLOWER, new SimpleBlockFeature(BlockStateProvider.of(Blocks.SUNFLOWER)));
        context.register(PUMPKIN, new SimpleBlockFeature(BlockStateProvider.of(Blocks.PUMPKIN)));
        context.register(BERRY_BUSH, new SimpleBlockFeature(BlockStateProvider.of((BlockState)Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 3))));
        context.register(TAIGA_GRASS, new SimpleBlockFeature(new WeightedStateProvider(WeightedList.builder().add(Blocks.SHORT_GRASS.defaultBlockState(), 1).add(Blocks.FERN.defaultBlockState(), 4))));
        context.register(GRASS, new SimpleBlockFeature(BlockStateProvider.of(Blocks.SHORT_GRASS)));
        context.register(LEAF_LITTER, new SimpleBlockFeature(new WeightedStateProvider(VegetationFeatures.leafLitterPatchBuilder(1, 3))));
        context.register(GRASS_JUNGLE, new SimpleBlockFeature(new WeightedStateProvider(WeightedList.builder().add(Blocks.SHORT_GRASS.defaultBlockState(), 3).add(Blocks.FERN.defaultBlockState(), 1))));
        context.register(DEAD_BUSH, new SimpleBlockFeature(BlockStateProvider.of(Blocks.DEAD_BUSH)));
        context.register(DRY_GRASS, new SimpleBlockFeature(new WeightedStateProvider(WeightedList.builder().add(Blocks.SHORT_DRY_GRASS.defaultBlockState(), 1).add(Blocks.TALL_DRY_GRASS.defaultBlockState(), 1))));
        context.register(MELON, new SimpleBlockFeature(BlockStateProvider.of(Blocks.MELON)));
        context.register(WATERLILY, new SimpleBlockFeature(BlockStateProvider.of(Blocks.LILY_PAD)));
        context.register(TALL_GRASS, new SimpleBlockFeature(BlockStateProvider.of(Blocks.TALL_GRASS)));
        context.register(LARGE_FERN, new SimpleBlockFeature(BlockStateProvider.of(Blocks.LARGE_FERN)));
        context.register(BUSH, new SimpleBlockFeature(BlockStateProvider.of(Blocks.BUSH)));
        context.register(RED_SHRUB, new SimpleBlockFeature(BlockStateProvider.of(Blocks.RED_SHRUB)));
        context.register(CACTUS, new BlockColumnFeature(List.of(BlockColumnFeature.layer((IntProvider)BiasedToBottomInt.of(1, 3), BlockStateProvider.of(Blocks.CACTUS)), BlockColumnFeature.layer((IntProvider)new WeightedListInt(WeightedList.builder().add(ConstantInt.of(0), 3).add(ConstantInt.of(1), 1).build()), BlockStateProvider.of(Blocks.CACTUS_FLOWER))), Direction.UP, BlockPredicate.ONLY_IN_AIR_PREDICATE, false));
        context.register(SUGAR_CANE, BlockColumnFeature.simple(BiasedToBottomInt.of(2, 4), BlockStateProvider.of(Blocks.SUGAR_CANE)));
        context.register(FIREFLY_BUSH, new SimpleBlockFeature(BlockStateProvider.of(Blocks.FIREFLY_BUSH)));
        WeightedStateProvider provider = new WeightedStateProvider(WeightedList.builder().add(Blocks.POPPY.defaultBlockState(), 2).add(Blocks.DANDELION.defaultBlockState(), 1));
        context.register(FLOWER_DEFAULT, new SimpleBlockFeature(provider));
        context.register(FLOWER_FLOWER_FOREST, new SimpleBlockFeature(blockStateProviders.getOrThrow(BlockStateProviders.FLOWER_FLOWER_FOREST)));
        context.register(FLOWER_SWAMP, new SimpleBlockFeature(BlockStateProvider.of(Blocks.BLUE_ORCHID)));
        context.register(FLOWER_PLAIN, new SimpleBlockFeature(blockStateProviders.getOrThrow(BlockStateProviders.FLOWER_PLAIN)));
        context.register(FLOWER_MEADOW, new SimpleBlockFeature(blockStateProviders.getOrThrow(BlockStateProviders.FLOWER_MEADOW)));
        context.register(FLOWER_CHERRY, new SimpleBlockFeature(new WeightedStateProvider(VegetationFeatures.flowerBedPatchBuilder(Blocks.PINK_PETALS))));
        context.register(WILDFLOWER, new SimpleBlockFeature(new WeightedStateProvider(VegetationFeatures.flowerBedPatchBuilder(Blocks.WILDFLOWERS))));
        context.register(FLOWER_PALE_GARDEN, new SimpleBlockFeature(BlockStateProvider.holderOf(Blocks.CLOSED_EYEBLOSSOM), true));
        context.register(FOREST_FLOWERS, new SimpleRandomSelectorFeature(HolderSet.direct(PlacementUtils.inlinePlaced((Feature)new SimpleBlockFeature(BlockStateProvider.of(Blocks.LILAC)), CountPlacement.of(96), OffsetPlacement.ofTriangle(7, 3), BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE)), PlacementUtils.inlinePlaced((Feature)new SimpleBlockFeature(BlockStateProvider.of(Blocks.ROSE_BUSH)), CountPlacement.of(96), OffsetPlacement.ofTriangle(7, 3), BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE)), PlacementUtils.inlinePlaced((Feature)new SimpleBlockFeature(BlockStateProvider.of(Blocks.PEONY)), CountPlacement.of(96), OffsetPlacement.ofTriangle(7, 3), BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE)), PlacementUtils.inlinePlaced((Feature)new SimpleBlockFeature(BlockStateProvider.of(Blocks.LILY_OF_THE_VALLEY)), CountPlacement.of(96), OffsetPlacement.ofTriangle(7, 3), BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE)))));
        context.register(PALE_FOREST_FLOWER, new SimpleBlockFeature(BlockStateProvider.holderOf(Blocks.CLOSED_EYEBLOSSOM), true));
        context.register(DARK_FOREST_VEGETATION, new RandomSelectorFeature(List.of(new WeightedPlacedFeature(PlacementUtils.inlinePlaced(hugeBrownMushroom, new PlacementModifier[0]), 0.025f), new WeightedPlacedFeature(PlacementUtils.inlinePlaced(hugeRedMushroom, new PlacementModifier[0]), 0.05f), new WeightedPlacedFeature(darkOakLeafLitter, 0.6666667f), new WeightedPlacedFeature(fallenBirch, 0.0025f), new WeightedPlacedFeature(birchLeafLitter, 0.2f), new WeightedPlacedFeature(fallenOak, 0.0125f), new WeightedPlacedFeature(fancyOakLeafLitter, 0.1f)), oakLeafLitter));
        context.register(TREES_DAPPLED_FOREST, new WeightedRandomSelectorFeature(WeightedList.of(new Weighted<Holder.Reference<PlacedFeature>>(redPoplarLeafLitter, 200), new Weighted<Holder.Reference<PlacedFeature>>(orangePoplarLeafLitter, 240), new Weighted<Holder.Reference<PlacedFeature>>(yellowPoplarLeafLitter, 90), new Weighted<Holder.Reference<PlacedFeature>>(spruceChecked, 27), new Weighted<Holder.Reference<PlacedFeature>>(fallenPoplar, 120))));
        context.register(PALE_GARDEN_VEGETATION, new RandomSelectorFeature(List.of(new WeightedPlacedFeature(paleOakCreakingChecked, 0.1f), new WeightedPlacedFeature(paleOakChecked, 0.9f)), paleOakChecked));
        context.register(PALE_MOSS_VEGETATION, new SimpleBlockFeature(new WeightedStateProvider(WeightedList.builder().add(Blocks.PALE_MOSS_CARPET.defaultBlockState(), 25).add(Blocks.SHORT_GRASS.defaultBlockState(), 25).add(Blocks.TALL_GRASS.defaultBlockState(), 10))));
        context.register(PALE_MOSS_PATCH, new VegetationPatchFeature(blocks.getOrThrow(BlockTags.MOSS_REPLACEABLE), BlockStateProvider.holderOf(Blocks.PALE_MOSS_BLOCK), PlacementUtils.inlinePlaced(configuredFeatures.getOrThrow(PALE_MOSS_VEGETATION), new PlacementModifier[0]), CaveSurface.FLOOR, ConstantInt.of(1), 0.0f, 5, 0.3f, UniformInt.of(2, 4), 0.75f));
        context.register(PALE_MOSS_PATCH_BONEMEAL, new VegetationPatchFeature(blocks.getOrThrow(BlockTags.MOSS_REPLACEABLE), BlockStateProvider.holderOf(Blocks.PALE_MOSS_BLOCK), PlacementUtils.inlinePlaced(configuredFeatures.getOrThrow(PALE_MOSS_VEGETATION), new PlacementModifier[0]), CaveSurface.FLOOR, ConstantInt.of(1), 0.0f, 5, 0.6f, UniformInt.of(1, 2), 0.75f));
        context.register(TREES_FLOWER_FOREST, new RandomSelectorFeature(List.of(new WeightedPlacedFeature(fallenBirch, 0.0025f), new WeightedPlacedFeature(birchBees002, 0.2f), new WeightedPlacedFeature(fancyOakBees002, 0.1f)), oakBees002));
        context.register(MEADOW_TREES, new RandomSelectorFeature(List.of(new WeightedPlacedFeature(fancyOakBees, 0.5f)), superBirchBees));
        context.register(TREES_TAIGA, new RandomSelectorFeature(List.of(new WeightedPlacedFeature(pineChecked, 0.33333334f), new WeightedPlacedFeature(fallenSpruce, 0.0125f)), spruceChecked));
        context.register(TREES_BADLANDS, new RandomSelectorFeature(List.of(new WeightedPlacedFeature(fallenOak, 0.0125f)), oakLeafLitter));
        context.register(TREES_GROVE, new RandomSelectorFeature(List.of(new WeightedPlacedFeature(pineOnSnow, 0.33333334f)), spruceOnSnow));
        context.register(TREES_SAVANNA, new RandomSelectorFeature(List.of(new WeightedPlacedFeature(acaciaChecked, 0.8f), new WeightedPlacedFeature(fallenOak, 0.0125f)), oakChecked));
        context.register(TREES_SNOWY, new RandomSelectorFeature(List.of(new WeightedPlacedFeature(fallenSpruce, 0.0125f)), spruceChecked));
        context.register(TREES_BIRCH, new RandomSelectorFeature(List.of(new WeightedPlacedFeature(fallenBirch, 0.0125f)), birchBees0002Placed));
        context.register(BIRCH_TALL, new RandomSelectorFeature(List.of(new WeightedPlacedFeature(fallenSuperBirch, 0.00625f), new WeightedPlacedFeature(superBirchBees0002, 0.5f), new WeightedPlacedFeature(fallenBirch, 0.0125f)), birchBees0002Placed));
        context.register(TREES_WINDSWEPT_HILLS, new RandomSelectorFeature(List.of(new WeightedPlacedFeature(fallenSpruce, 0.008325f), new WeightedPlacedFeature(spruceChecked, 0.666f), new WeightedPlacedFeature(fancyOakChecked, 0.1f), new WeightedPlacedFeature(fallenOak, 0.0125f)), oakChecked));
        context.register(TREES_WATER, new RandomSelectorFeature(List.of(new WeightedPlacedFeature(fancyOakChecked, 0.1f)), oakChecked));
        context.register(TREES_BIRCH_AND_OAK_LEAF_LITTER, new RandomSelectorFeature(List.of(new WeightedPlacedFeature(fallenBirch, 0.0025f), new WeightedPlacedFeature(birchBees0002LeafLitter, 0.2f), new WeightedPlacedFeature(fancyOakBees0002LeafLitter, 0.1f), new WeightedPlacedFeature(fallenOak, 0.0125f)), oakBees0002LeafLitter));
        context.register(TREES_PLAINS, new RandomSelectorFeature(List.of(new WeightedPlacedFeature(PlacementUtils.inlinePlaced(fancyOakBees005, new PlacementModifier[0]), 0.33333334f), new WeightedPlacedFeature(fallenOak, 0.0125f)), PlacementUtils.inlinePlaced(oakBees005, new PlacementModifier[0])));
        context.register(TREES_SPARSE_JUNGLE, new RandomSelectorFeature(List.of(new WeightedPlacedFeature(fancyOakChecked, 0.1f), new WeightedPlacedFeature(jungleBush, 0.5f), new WeightedPlacedFeature(fallenJungle, 0.0125f)), jungleTreeChecked));
        context.register(TREES_OLD_GROWTH_SPRUCE_TAIGA, new RandomSelectorFeature(List.of(new WeightedPlacedFeature(megaSpruceChecked, 0.33333334f), new WeightedPlacedFeature(pineChecked, 0.33333334f), new WeightedPlacedFeature(fallenSpruce, 0.0125f)), spruceChecked));
        context.register(TREES_OLD_GROWTH_PINE_TAIGA, new RandomSelectorFeature(List.of(new WeightedPlacedFeature(megaSpruceChecked, 0.025641026f), new WeightedPlacedFeature(megaPineChecked, 0.30769232f), new WeightedPlacedFeature(pineChecked, 0.33333334f), new WeightedPlacedFeature(fallenSpruce, 0.0125f)), spruceChecked));
        context.register(TREES_JUNGLE, new RandomSelectorFeature(List.of(new WeightedPlacedFeature(fancyOakChecked, 0.1f), new WeightedPlacedFeature(jungleBush, 0.5f), new WeightedPlacedFeature(megaJungleTreeChecked, 0.33333334f), new WeightedPlacedFeature(fallenJungle, 0.0125f)), jungleTreeChecked));
        context.register(BAMBOO_VEGETATION, new RandomSelectorFeature(List.of(new WeightedPlacedFeature(fancyOakChecked, 0.05f), new WeightedPlacedFeature(jungleBush, 0.15f), new WeightedPlacedFeature(megaJungleTreeChecked, 0.7f)), PlacementUtils.inlinePlaced(grassJungle, CountPlacement.of(32), OffsetPlacement.ofTriangle(7, 3), BlockPredicateFilter.forPredicate(BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, BlockPredicate.not(BlockPredicate.matchesBlocks(Direction.DOWN, Blocks.PODZOL)))))));
        context.register(MUSHROOM_ISLAND_VEGETATION, new RandomBooleanSelectorFeature(PlacementUtils.inlinePlaced(hugeRedMushroom, new PlacementModifier[0]), PlacementUtils.inlinePlaced(hugeBrownMushroom, new PlacementModifier[0])));
        context.register(MANGROVE_VEGETATION, new RandomSelectorFeature(List.of(new WeightedPlacedFeature(tallMangroveChecked, 0.85f)), mangroveChecked));
    }

    private static WeightedList.Builder<BlockState> flowerBedPatchBuilder(Block flowerBedBlock) {
        return VegetationFeatures.segmentedBlockPatchBuilder(flowerBedBlock, 1, 4, FlowerBedBlock.AMOUNT, FlowerBedBlock.FACING);
    }

    public static WeightedList.Builder<BlockState> leafLitterPatchBuilder(int minState, int maxState) {
        return VegetationFeatures.segmentedBlockPatchBuilder(Blocks.LEAF_LITTER, minState, maxState, LeafLitterBlock.AMOUNT, LeafLitterBlock.FACING);
    }

    private static WeightedList.Builder<BlockState> segmentedBlockPatchBuilder(Block block, int minState, int maxState, IntegerProperty amountProperty, EnumProperty<Direction> directionProperty) {
        WeightedList.Builder<BlockState> segmentedBlockBuild = WeightedList.builder();
        for (int amount = minState; amount <= maxState; ++amount) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                segmentedBlockBuild.add((BlockState)((BlockState)block.defaultBlockState().setValue(amountProperty, amount)).setValue(directionProperty, direction), 1);
            }
        }
        return segmentedBlockBuild;
    }

    public static BlockPredicateFilter nearWaterPredicate(Block block) {
        return BlockPredicateFilter.forPredicate(BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, BlockPredicate.wouldSurvive(block), BlockPredicate.anyOf(BlockPredicate.matchesFluids(CompositeDirection.EAST_DOWN, Fluids.WATER, Fluids.FLOWING_WATER), BlockPredicate.matchesFluids(CompositeDirection.WEST_DOWN, Fluids.WATER, Fluids.FLOWING_WATER), BlockPredicate.matchesFluids(CompositeDirection.SOUTH_DOWN, Fluids.WATER, Fluids.FLOWING_WATER), BlockPredicate.matchesFluids(CompositeDirection.NORTH_DOWN, Fluids.WATER, Fluids.FLOWING_WATER))));
    }
}

