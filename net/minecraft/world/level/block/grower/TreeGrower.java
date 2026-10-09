/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block.grower;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import org.jspecify.annotations.Nullable;

public final class TreeGrower {
    private static final Map<String, TreeGrower> GROWERS = new Object2ObjectArrayMap();
    public static final Codec<TreeGrower> CODEC = Codec.stringResolver(g -> g.name, GROWERS::get);
    public static final TreeGrower OAK = new TreeGrower("oak", WeightedList.of(new Weighted<ResourceKey<Feature>>(TreeFeatures.OAK, 9), new Weighted<ResourceKey<Feature>>(TreeFeatures.FANCY_OAK, 1)), WeightedList.of(), WeightedList.of(new Weighted<ResourceKey<Feature>>(TreeFeatures.OAK_BEES_005, 9), new Weighted<ResourceKey<Feature>>(TreeFeatures.FANCY_OAK_BEES_005, 1)), TreeFeatures.OAK);
    public static final TreeGrower SPRUCE = new TreeGrower("spruce", WeightedList.of(TreeFeatures.SPRUCE), WeightedList.of(new Weighted<ResourceKey<Feature>>(TreeFeatures.MEGA_SPRUCE, 1), new Weighted<ResourceKey<Feature>>(TreeFeatures.MEGA_PINE, 1)), WeightedList.of(), TreeFeatures.SPRUCE);
    public static final TreeGrower MANGROVE = new TreeGrower("mangrove", WeightedList.of(new Weighted<ResourceKey<Feature>>(TreeFeatures.MANGROVE, 15), new Weighted<ResourceKey<Feature>>(TreeFeatures.TALL_MANGROVE, 85)), WeightedList.of(), WeightedList.of(), TreeFeatures.MANGROVE);
    public static final TreeGrower AZALEA = new TreeGrower("azalea", WeightedList.of(TreeFeatures.AZALEA_TREE), WeightedList.of(), WeightedList.of(), TreeFeatures.AZALEA_TREE);
    public static final TreeGrower BIRCH = new TreeGrower("birch", WeightedList.of(TreeFeatures.BIRCH), WeightedList.of(), WeightedList.of(TreeFeatures.BIRCH_BEES_005), TreeFeatures.BIRCH);
    public static final TreeGrower JUNGLE = new TreeGrower("jungle", WeightedList.of(TreeFeatures.JUNGLE_TREE_NO_VINE), WeightedList.of(TreeFeatures.MEGA_JUNGLE_TREE), WeightedList.of(), TreeFeatures.JUNGLE_TREE_NO_VINE);
    public static final TreeGrower ACACIA = new TreeGrower("acacia", WeightedList.of(TreeFeatures.ACACIA), WeightedList.of(), WeightedList.of(), TreeFeatures.ACACIA);
    public static final TreeGrower CHERRY = new TreeGrower("cherry", WeightedList.of(TreeFeatures.CHERRY), WeightedList.of(), WeightedList.of(TreeFeatures.CHERRY_BEES_005), TreeFeatures.CHERRY);
    public static final TreeGrower DARK_OAK = new TreeGrower("dark_oak", WeightedList.of(), WeightedList.of(TreeFeatures.DARK_OAK), WeightedList.of(), null);
    public static final TreeGrower PALE_OAK = new TreeGrower("pale_oak", WeightedList.of(), WeightedList.of(TreeFeatures.PALE_OAK_BONEMEAL), WeightedList.of(), null);
    public static final TreeGrower POPLAR = new TreeGrower("poplar", WeightedList.of(new Weighted<ResourceKey<Feature>>(TreeFeatures.RED_POPLAR, 1), new Weighted<ResourceKey<Feature>>(TreeFeatures.ORANGE_POPLAR, 1), new Weighted<ResourceKey<Feature>>(TreeFeatures.YELLOW_POPLAR, 1)), WeightedList.of(), WeightedList.of(), TreeFeatures.RED_POPLAR);
    private final String name;
    private final WeightedList<ResourceKey<Feature>> trees;
    private final WeightedList<ResourceKey<Feature>> megaTrees;
    private final WeightedList<ResourceKey<Feature>> flowerTrees;
    private final @Nullable ResourceKey<Feature> shortestTreeType;

    public TreeGrower(String name, WeightedList<ResourceKey<Feature>> trees, WeightedList<ResourceKey<Feature>> megaTrees, WeightedList<ResourceKey<Feature>> flowerTrees, @Nullable ResourceKey<Feature> shortestTreeType) {
        this.name = name;
        this.trees = trees;
        this.megaTrees = megaTrees;
        this.flowerTrees = flowerTrees;
        this.shortestTreeType = shortestTreeType;
        GROWERS.put(name, this);
    }

    private @Nullable ResourceKey<Feature> getConfiguredFeature(RandomSource random, boolean hasFlowers) {
        if (hasFlowers && !this.flowerTrees.isEmpty()) {
            return this.flowerTrees.getRandom(random).orElse(null);
        }
        return this.trees.getRandom(random).orElse(null);
    }

    private @Nullable ResourceKey<Feature> getConfiguredMegaFeature(RandomSource random) {
        return this.megaTrees.getRandom(random).orElse(null);
    }

    public boolean growTree(ServerLevel level, ChunkGenerator generator, BlockPos pos, BlockState state, RandomSource random) {
        Optional<TwoByTwoSaplingPos> twoByTwoSaplingPos;
        Holder featureHolder;
        ResourceKey<Feature> megaFeatureKey = this.getConfiguredMegaFeature(random);
        if (megaFeatureKey != null && (featureHolder = (Holder)level.registryAccess().lookupOrThrow(Registries.FEATURE).get(megaFeatureKey).orElse(null)) != null && (twoByTwoSaplingPos = TreeGrower.findTwoByTwoSaplingPos(level, state, pos)).isPresent()) {
            int dx = twoByTwoSaplingPos.get().offsetX();
            int dz = twoByTwoSaplingPos.get().offsetZ();
            List<Pair<BlockState, BlockPos>> groundLevelSurroundingBlocks = twoByTwoSaplingPos.get().groundLevelSurroundingBlocks();
            Feature feature = (Feature)featureHolder.value();
            TreeGrower.removeSaplings(level, groundLevelSurroundingBlocks);
            if (feature.place(level, generator, random, pos.offset(dx, 0, dz))) {
                return true;
            }
            TreeGrower.resetSaplings(level, groundLevelSurroundingBlocks);
            return false;
        }
        ResourceKey<Feature> featureKey = this.getConfiguredFeature(random, this.hasFlowers(level, pos));
        if (featureKey == null) {
            return false;
        }
        Holder featureHolder2 = level.registryAccess().lookupOrThrow(Registries.FEATURE).get(featureKey).orElse(null);
        if (featureHolder2 == null) {
            return false;
        }
        Feature feature = (Feature)featureHolder2.value();
        TreeGrower.removeSapling(level, pos);
        if (feature.place(level, generator, random, pos)) {
            return true;
        }
        TreeGrower.resetSaplings(level, List.of(Pair.of((Object)state, (Object)pos)));
        return false;
    }

    private static List<Pair<BlockState, BlockPos>> getSurroundingBlockStates(ServerLevel level, BlockPos pos, int dx, int dz) {
        return List.of(Pair.of((Object)level.getBlockState(pos.offset(dx, 0, dz)), (Object)pos.offset(dx, 0, dz)), Pair.of((Object)level.getBlockState(pos.offset(dx + 1, 0, dz)), (Object)pos.offset(dx + 1, 0, dz)), Pair.of((Object)level.getBlockState(pos.offset(dx, 0, dz + 1)), (Object)pos.offset(dx, 0, dz + 1)), Pair.of((Object)level.getBlockState(pos.offset(dx + 1, 0, dz + 1)), (Object)pos.offset(dx + 1, 0, dz + 1)));
    }

    private static void removeSaplings(ServerLevel level, List<Pair<BlockState, BlockPos>> saplingBlocks) {
        for (Pair<BlockState, BlockPos> saplingBlock : saplingBlocks) {
            BlockPos saplingPosition = (BlockPos)saplingBlock.getSecond();
            TreeGrower.removeSapling(level, saplingPosition);
        }
    }

    private static void removeSapling(ServerLevel level, BlockPos saplingPosition) {
        BlockState emptyBlock = level.getFluidState(saplingPosition).createLegacyBlock();
        level.setBlock(saplingPosition, emptyBlock, 818);
    }

    private static void resetSaplings(ServerLevel level, List<Pair<BlockState, BlockPos>> saplingBlocks) {
        for (Pair<BlockState, BlockPos> saplingBlock : saplingBlocks) {
            level.setBlock((BlockPos)saplingBlock.getSecond(), (BlockState)saplingBlock.getFirst(), 260);
        }
    }

    private static boolean isTwoByTwoSapling(BlockState state, List<Pair<BlockState, BlockPos>> surroundingBlocks) {
        Block block = state.getBlock();
        for (Pair<BlockState, BlockPos> surroundingBlock : surroundingBlocks) {
            BlockState surroundingBlockState = (BlockState)surroundingBlock.getFirst();
            if (surroundingBlockState.is(block)) continue;
            return false;
        }
        return true;
    }

    private boolean hasFlowers(LevelAccessor level, BlockPos pos) {
        return level.findBlocksIn(pos.offset(-2, -1, -2), pos.offset(2, 1, 2)).filterState(state -> state.is(BlockTags.FLOWERS)).anyMatched();
    }

    public OptionalInt getMinimumHeight(ServerLevel level) {
        Object t;
        ResourceKey<Feature> featureKey = this.shortestTreeType;
        if (featureKey == null) {
            return OptionalInt.empty();
        }
        Holder featureHolder = level.registryAccess().lookupOrThrow(Registries.FEATURE).get(featureKey).orElse(null);
        if (featureHolder != null && (t = featureHolder.value()) instanceof TreeFeature) {
            TreeFeature treeFeature = (TreeFeature)t;
            return OptionalInt.of(treeFeature.trunkPlacer().getBaseHeight());
        }
        return OptionalInt.empty();
    }

    public boolean canGrow(ServerLevel level, BlockPos pos, BlockState state) {
        ResourceKey<Feature> featureKey = this.getConfiguredFeature(level.getRandom(), this.hasFlowers(level, pos));
        ResourceKey<Feature> megaFeatureKey = this.getConfiguredMegaFeature(level.getRandom());
        if (featureKey == null && megaFeatureKey != null) {
            return TreeGrower.findTwoByTwoSaplingPos(level, state, pos).isPresent();
        }
        return true;
    }

    private static Optional<TwoByTwoSaplingPos> findTwoByTwoSaplingPos(ServerLevel level, BlockState state, BlockPos pos) {
        for (int dx = 0; dx >= -1; --dx) {
            for (int dz = 0; dz >= -1; --dz) {
                List<Pair<BlockState, BlockPos>> groundLevelSurroundingBlocks = TreeGrower.getSurroundingBlockStates(level, pos, dx, dz);
                if (!TreeGrower.isTwoByTwoSapling(state, groundLevelSurroundingBlocks)) continue;
                return Optional.of(new TwoByTwoSaplingPos(dx, dz, groundLevelSurroundingBlocks));
            }
        }
        return Optional.empty();
    }

    private record TwoByTwoSaplingPos(int offsetX, int offsetZ, List<Pair<BlockState, BlockPos>> groundLevelSurroundingBlocks) {
    }
}

