/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public record RootSystemFeature(Holder<PlacedFeature> treeFeature, int requiredVerticalSpaceForTree, int levelTestDistance, int maxLevelDeviation, int rootRadius, HolderSet<Block> rootReplaceable, Holder<BlockStateProvider> rootStateProvider, int rootPlacementAttempts, int rootColumnMaxHeight, int hangingRootRadius, int hangingRootsVerticalSpan, Holder<BlockStateProvider> hangingRootStateProvider, int hangingRootPlacementAttempts, int allowedVerticalWaterForTree, BlockPredicate allowedTreePosition) implements Feature
{
    public static final MapCodec<RootSystemFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)PlacedFeature.CODEC.fieldOf("feature").forGetter(RootSystemFeature::treeFeature), (App)Codec.intRange((int)1, (int)64).fieldOf("required_vertical_space_for_tree").forGetter(RootSystemFeature::requiredVerticalSpaceForTree), (App)Codec.intRange((int)0, (int)16).fieldOf("level_test_distance").forGetter(RootSystemFeature::levelTestDistance), (App)Codec.intRange((int)0, (int)64).fieldOf("max_level_deviation").forGetter(RootSystemFeature::maxLevelDeviation), (App)Codec.intRange((int)1, (int)64).fieldOf("root_radius").forGetter(RootSystemFeature::rootRadius), (App)RegistryCodecs.holderSet(Registries.BLOCK).fieldOf("root_replaceable").forGetter(RootSystemFeature::rootReplaceable), (App)BlockStateProvider.CODEC.fieldOf("root_state_provider").forGetter(RootSystemFeature::rootStateProvider), (App)Codec.intRange((int)1, (int)256).fieldOf("root_placement_attempts").forGetter(RootSystemFeature::rootPlacementAttempts), (App)Codec.intRange((int)1, (int)4096).fieldOf("root_column_max_height").forGetter(RootSystemFeature::rootColumnMaxHeight), (App)Codec.intRange((int)1, (int)64).fieldOf("hanging_root_radius").forGetter(RootSystemFeature::hangingRootRadius), (App)Codec.intRange((int)1, (int)16).fieldOf("hanging_roots_vertical_span").forGetter(RootSystemFeature::hangingRootsVerticalSpan), (App)BlockStateProvider.CODEC.fieldOf("hanging_root_state_provider").forGetter(RootSystemFeature::hangingRootStateProvider), (App)Codec.intRange((int)1, (int)256).fieldOf("hanging_root_placement_attempts").forGetter(RootSystemFeature::hangingRootPlacementAttempts), (App)Codec.intRange((int)1, (int)64).fieldOf("allowed_vertical_water_for_tree").forGetter(RootSystemFeature::allowedVerticalWaterForTree), (App)BlockPredicate.CODEC.fieldOf("allowed_tree_position").forGetter(RootSystemFeature::allowedTreePosition)).apply((Applicative)i, RootSystemFeature::new));

    public MapCodec<RootSystemFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        if (!level.getBlockState(origin).isAir()) {
            return false;
        }
        BlockPos.MutableBlockPos workingPos = origin.mutable();
        if (this.placeDirtAndTree(level, chunkGenerator, random, workingPos, origin)) {
            this.placeRoots(level, random, origin, workingPos);
        }
        return true;
    }

    private boolean spaceForTree(WorldGenLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos columnUpPos = pos.mutable();
        for (int i = 1; i <= this.requiredVerticalSpaceForTree; ++i) {
            columnUpPos.move(Direction.UP);
            BlockState state = level.getBlockState(columnUpPos);
            if (RootSystemFeature.isAllowedTreeSpace(state, i, this.allowedVerticalWaterForTree)) continue;
            return false;
        }
        if (this.levelTestDistance > 0) {
            BlockPos.MutableBlockPos cornerPos = pos.mutable();
            for (int i = 0; i < 4; ++i) {
                cornerPos.move(Direction.from2DDataValue(i), this.levelTestDistance);
                BlockState below = level.getBlockState((BlockPos)cornerPos.below(this.maxLevelDeviation));
                BlockState above = level.getBlockState((BlockPos)cornerPos.above(this.maxLevelDeviation));
                if (below.isAir() || !above.isAir()) {
                    return false;
                }
                cornerPos.set(pos);
            }
        }
        return true;
    }

    private static boolean isAllowedTreeSpace(BlockState state, int blocksAboveOrigin, int allowedVerticalWaterHeight) {
        if (state.isAir()) {
            return true;
        }
        int blocksAboveGround = blocksAboveOrigin + 1;
        return blocksAboveGround <= allowedVerticalWaterHeight && state.getFluidState().is(FluidTags.WATER);
    }

    private boolean placeDirtAndTree(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos.MutableBlockPos workingPos, BlockPos pos) {
        for (int y = 0; y < this.rootColumnMaxHeight; ++y) {
            workingPos.move(Direction.UP);
            if (level.getHeight(Heightmap.Types.WORLD_SURFACE, workingPos) < workingPos.getY()) {
                return false;
            }
            if (!this.allowedTreePosition.test(level, workingPos) || !this.spaceForTree(level, workingPos)) continue;
            Vec3i belowPos = workingPos.below();
            if (level.getFluidState((BlockPos)belowPos).is(FluidTags.LAVA) || !level.getBlockState((BlockPos)belowPos).isSolid()) {
                return false;
            }
            if (!this.treeFeature.value().place(level, generator, random, workingPos)) continue;
            this.placeDirt(pos, pos.getY() + y, level, random);
            return true;
        }
        return false;
    }

    private void placeDirt(BlockPos origin, int targetHeight, WorldGenLevel level, RandomSource random) {
        int originX = origin.getX();
        int originZ = origin.getZ();
        BlockPos.MutableBlockPos workingPos = origin.mutable();
        for (int y = origin.getY(); y < targetHeight; ++y) {
            this.placeRootedDirt(level, random, originX, originZ, workingPos.set(originX, y, originZ));
        }
    }

    private void placeRootedDirt(WorldGenLevel level, RandomSource random, int originX, int originZ, BlockPos.MutableBlockPos workingPos) {
        for (int i = 0; i < this.rootPlacementAttempts; ++i) {
            workingPos.setWithOffset(workingPos, random.nextInt(this.rootRadius) - random.nextInt(this.rootRadius), 0, random.nextInt(this.rootRadius) - random.nextInt(this.rootRadius));
            if (level.getBlockState(workingPos).is(this.rootReplaceable)) {
                level.setBlock(workingPos, this.rootStateProvider.value().getState(level, random, workingPos), 2);
            }
            workingPos.setX(originX);
            workingPos.setZ(originZ);
        }
    }

    private void placeRoots(WorldGenLevel level, RandomSource random, BlockPos pos, BlockPos.MutableBlockPos workingPos) {
        for (int i = 0; i < this.hangingRootPlacementAttempts; ++i) {
            BlockState targetState;
            workingPos.setWithOffset(pos, random.nextInt(this.hangingRootRadius) - random.nextInt(this.hangingRootRadius), random.nextInt(this.hangingRootsVerticalSpan) - random.nextInt(this.hangingRootsVerticalSpan), random.nextInt(this.hangingRootRadius) - random.nextInt(this.hangingRootRadius));
            if (!level.isEmptyBlock(workingPos) || !(targetState = this.hangingRootStateProvider.value().getState(level, random, workingPos)).canSurvive(level, workingPos) || !level.getBlockState((BlockPos)workingPos.above()).isFaceSturdy(level, workingPos, Direction.DOWN)) continue;
            level.setBlock(workingPos, targetState, 2);
        }
    }
}

