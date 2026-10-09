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
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ClampedNormalFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviders;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SpeleothemUtils;

public record SpeleothemClusterFeature(BlockState baseBlock, BlockState pointedBlock, HolderSet<Block> replaceableBlocks, int floorToCeilingSearchRange, IntProvider height, IntProvider radius, int maxStalagmiteStalactiteHeightDiff, int heightDeviation, IntProvider speleothemBlockLayerThickness, FloatProvider density, FloatProvider wetness, float chanceOfSpeleothemAtMaxDistanceFromCenter, int maxDistanceFromEdgeAffectingChanceOfSpeleothem, int maxDistanceFromCenterAffectingHeightBias) implements Feature
{
    public static final MapCodec<SpeleothemClusterFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)BlockState.CODEC.fieldOf("base_block").forGetter(SpeleothemClusterFeature::baseBlock), (App)BlockState.CODEC.fieldOf("pointed_block").forGetter(SpeleothemClusterFeature::pointedBlock), (App)RegistryCodecs.holderSet(Registries.BLOCK).fieldOf("replaceable_blocks").forGetter(SpeleothemClusterFeature::replaceableBlocks), (App)Codec.intRange((int)1, (int)512).fieldOf("floor_to_ceiling_search_range").forGetter(SpeleothemClusterFeature::floorToCeilingSearchRange), (App)IntProviders.codec(1, 128).fieldOf("height").forGetter(SpeleothemClusterFeature::height), (App)IntProviders.codec(1, 128).fieldOf("radius").forGetter(SpeleothemClusterFeature::radius), (App)Codec.intRange((int)0, (int)64).fieldOf("max_stalagmite_stalactite_height_diff").forGetter(SpeleothemClusterFeature::maxStalagmiteStalactiteHeightDiff), (App)Codec.intRange((int)1, (int)64).fieldOf("height_deviation").forGetter(SpeleothemClusterFeature::heightDeviation), (App)IntProviders.codec(0, 128).fieldOf("speleothem_block_layer_thickness").forGetter(SpeleothemClusterFeature::speleothemBlockLayerThickness), (App)FloatProviders.codec(0.0f, 2.0f).fieldOf("density").forGetter(SpeleothemClusterFeature::density), (App)FloatProviders.codec(0.0f, 2.0f).fieldOf("wetness").forGetter(SpeleothemClusterFeature::wetness), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("chance_of_speleothem_at_max_distance_from_center").forGetter(SpeleothemClusterFeature::chanceOfSpeleothemAtMaxDistanceFromCenter), (App)Codec.intRange((int)1, (int)64).fieldOf("max_distance_from_edge_affecting_chance_of_speleothem").forGetter(SpeleothemClusterFeature::maxDistanceFromEdgeAffectingChanceOfSpeleothem), (App)Codec.intRange((int)1, (int)64).fieldOf("max_distance_from_center_affecting_height_bias").forGetter(SpeleothemClusterFeature::maxDistanceFromCenterAffectingHeightBias)).apply((Applicative)i, SpeleothemClusterFeature::new));

    public MapCodec<SpeleothemClusterFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        if (!SpeleothemUtils.isEmptyOrWater(level, origin)) {
            return false;
        }
        int height = this.height.sample(random);
        float wetness = this.wetness.sample(random);
        float density = this.density.sample(random);
        int xRadius = this.radius.sample(random);
        int zRadius = this.radius.sample(random);
        for (int dx = -xRadius; dx <= xRadius; ++dx) {
            for (int dz = -zRadius; dz <= zRadius; ++dz) {
                double chanceOfStalagmiteOrStalactite = this.getChanceOfStalagmiteOrStalactite(xRadius, zRadius, dx, dz);
                BlockPos pos = origin.offset(dx, 0, dz);
                this.placeColumn(level, random, pos, dx, dz, wetness, chanceOfStalagmiteOrStalactite, height, density);
            }
        }
        return true;
    }

    private void placeColumn(WorldGenLevel level, RandomSource random, BlockPos pos, int dx, int dz, float chanceOfWater, double chanceOfStalagmiteOrStalactite, int clusterHeight, float density) {
        boolean mergeTips;
        int actualStalagmiteHeight;
        int actualStalactiteHeight;
        int stalagmiteHeight;
        boolean wantStalagmite;
        int stalactiteHeight;
        boolean wantStalactite;
        Column column;
        boolean wantPool;
        Optional<Column> baseColumn = Column.scan(level, pos, this.floorToCeilingSearchRange, SpeleothemUtils::isEmptyOrWater, SpeleothemUtils::isNeitherEmptyNorWater);
        if (baseColumn.isEmpty()) {
            return;
        }
        OptionalInt ceiling = baseColumn.get().getCeiling();
        OptionalInt baseFloor = baseColumn.get().getFloor();
        if (ceiling.isEmpty() && baseFloor.isEmpty()) {
            return;
        }
        boolean bl = wantPool = random.nextFloat() < chanceOfWater;
        if (wantPool && baseFloor.isPresent() && this.canPlacePool(level, pos.atY(baseFloor.getAsInt()))) {
            int baseFloorY = baseFloor.getAsInt();
            column = baseColumn.get().withFloor(OptionalInt.of(baseFloorY - 1));
            level.setBlock(pos.atY(baseFloorY), Blocks.WATER.defaultBlockState(), 2);
        } else {
            column = baseColumn.get();
        }
        OptionalInt floor = column.getFloor();
        boolean bl2 = wantStalactite = random.nextDouble() < chanceOfStalagmiteOrStalactite;
        if (ceiling.isPresent() && wantStalactite && !this.isLava(level, pos.atY(ceiling.getAsInt()))) {
            int ceilingThickness = this.speleothemBlockLayerThickness.sample(random);
            this.replaceBlocksWithBaseBlocks(level, pos.atY(ceiling.getAsInt()), ceilingThickness, Direction.UP);
            int maxHeightForThisColumn = floor.isPresent() ? Math.min(clusterHeight, ceiling.getAsInt() - floor.getAsInt()) : clusterHeight;
            stalactiteHeight = this.getSpeleothemHeight(random, dx, dz, density, maxHeightForThisColumn);
        } else {
            stalactiteHeight = 0;
        }
        boolean bl3 = wantStalagmite = random.nextDouble() < chanceOfStalagmiteOrStalactite;
        if (floor.isPresent() && wantStalagmite && !this.isLava(level, pos.atY(floor.getAsInt()))) {
            int floorThickness = this.speleothemBlockLayerThickness.sample(random);
            this.replaceBlocksWithBaseBlocks(level, pos.atY(floor.getAsInt()), floorThickness, Direction.DOWN);
            stalagmiteHeight = ceiling.isPresent() ? Math.max(0, stalactiteHeight + Mth.randomBetweenInclusive(random, -this.maxStalagmiteStalactiteHeightDiff, this.maxStalagmiteStalactiteHeightDiff)) : this.getSpeleothemHeight(random, dx, dz, density, clusterHeight);
        } else {
            stalagmiteHeight = 0;
        }
        if (ceiling.isPresent() && floor.isPresent() && ceiling.getAsInt() - stalactiteHeight <= floor.getAsInt() + stalagmiteHeight) {
            int floorY = floor.getAsInt();
            int ceilingY = ceiling.getAsInt();
            int lowestStalactiteBottom = Math.max(ceilingY - stalactiteHeight, floorY + 1);
            int highestStalagmiteTop = Math.min(floorY + stalagmiteHeight, ceilingY - 1);
            int actualStalactiteBottom = Mth.randomBetweenInclusive(random, lowestStalactiteBottom, highestStalagmiteTop + 1);
            int actualStalagmiteTop = actualStalactiteBottom - 1;
            actualStalactiteHeight = ceilingY - actualStalactiteBottom;
            actualStalagmiteHeight = actualStalagmiteTop - floorY;
        } else {
            actualStalactiteHeight = stalactiteHeight;
            actualStalagmiteHeight = stalagmiteHeight;
        }
        boolean bl4 = mergeTips = random.nextBoolean() && actualStalactiteHeight > 0 && actualStalagmiteHeight > 0 && column.getHeight().isPresent() && actualStalactiteHeight + actualStalagmiteHeight == column.getHeight().getAsInt();
        if (ceiling.isPresent()) {
            SpeleothemUtils.growSpeleothem(level, pos.atY(ceiling.getAsInt() - 1), Direction.DOWN, actualStalactiteHeight, mergeTips, this.baseBlock.getBlock(), this.pointedBlock.getBlock(), this.replaceableBlocks);
        }
        if (floor.isPresent()) {
            SpeleothemUtils.growSpeleothem(level, pos.atY(floor.getAsInt() + 1), Direction.UP, actualStalagmiteHeight, mergeTips, this.baseBlock.getBlock(), this.pointedBlock.getBlock(), this.replaceableBlocks);
        }
    }

    private boolean isLava(LevelReader level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.LAVA);
    }

    private int getSpeleothemHeight(RandomSource random, int dx, int dz, float density, int maxHeight) {
        if (random.nextFloat() > density) {
            return 0;
        }
        int distanceFromCenter = Math.abs(dx) + Math.abs(dz);
        float heightMean = (float)Mth.clampedMap((double)distanceFromCenter, 0.0, (double)this.maxDistanceFromCenterAffectingHeightBias, (double)maxHeight / 2.0, 0.0);
        return (int)SpeleothemClusterFeature.randomBetweenBiased(random, 0.0f, maxHeight, heightMean, this.heightDeviation);
    }

    private boolean canPlacePool(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.WATER) || state.is(this.baseBlock.getBlock()) || state.is(this.pointedBlock.getBlock())) {
            return false;
        }
        if (level.getBlockState(pos.above()).getFluidState().is(FluidTags.WATER)) {
            return false;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (this.canBeAdjacentToWater(level, pos.relative(direction))) continue;
            return false;
        }
        return this.canBeAdjacentToWater(level, pos.below());
    }

    private boolean canBeAdjacentToWater(LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(BlockTags.BASE_STONE_OVERWORLD) || state.getFluidState().is(FluidTags.WATER);
    }

    private void replaceBlocksWithBaseBlocks(WorldGenLevel level, BlockPos firstPos, int maxCount, Direction direction) {
        BlockPos.MutableBlockPos pos = firstPos.mutable();
        for (int i = 0; i < maxCount; ++i) {
            if (!SpeleothemUtils.placeBaseBlockIfPossible(level, pos, this.baseBlock.getBlock(), this.replaceableBlocks)) {
                return;
            }
            pos.move(direction);
        }
    }

    private double getChanceOfStalagmiteOrStalactite(int xRadius, int zRadius, int dx, int dz) {
        int xDistanceFromEdge = xRadius - Math.abs(dx);
        int zDistanceFromEdge = zRadius - Math.abs(dz);
        int distanceFromEdge = Math.min(xDistanceFromEdge, zDistanceFromEdge);
        return Mth.clampedMap(distanceFromEdge, 0.0f, this.maxDistanceFromEdgeAffectingChanceOfSpeleothem, this.chanceOfSpeleothemAtMaxDistanceFromCenter, 1.0f);
    }

    private static float randomBetweenBiased(RandomSource random, float min, float maxExclusive, float mean, float deviation) {
        return ClampedNormalFloat.sample(random, mean, deviation, min, maxExclusive);
    }
}

