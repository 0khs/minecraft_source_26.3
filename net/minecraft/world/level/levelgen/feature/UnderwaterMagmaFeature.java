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
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public record UnderwaterMagmaFeature(int floorSearchRange, int placementRadiusAroundFloor, float placementProbabilityPerValidPosition) implements Feature
{
    public static final MapCodec<UnderwaterMagmaFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Codec.intRange((int)0, (int)512).fieldOf("floor_search_range").forGetter(UnderwaterMagmaFeature::floorSearchRange), (App)Codec.intRange((int)0, (int)64).fieldOf("placement_radius_around_floor").forGetter(UnderwaterMagmaFeature::placementRadiusAroundFloor), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("placement_probability_per_valid_position").forGetter(UnderwaterMagmaFeature::placementProbabilityPerValidPosition)).apply((Applicative)i, UnderwaterMagmaFeature::new));

    public MapCodec<UnderwaterMagmaFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        Vec3i radius;
        OptionalInt floorY = this.getFloorY(level, origin);
        if (floorY.isEmpty()) {
            return false;
        }
        BlockPos floorPos = origin.atY(floorY.getAsInt());
        BoundingBox bounds = BoundingBox.fromCorners(floorPos.subtract(radius = new Vec3i(this.placementRadiusAroundFloor, this.placementRadiusAroundFloor, this.placementRadiusAroundFloor)), floorPos.offset(radius));
        return BlockPos.betweenClosedStream(bounds).filter(pos -> random.nextFloat() < this.placementProbabilityPerValidPosition).filter(pos -> this.isValidPlacement(level, (BlockPos)pos)).mapToInt(pos -> {
            level.setBlock((BlockPos)pos, Blocks.MAGMA_BLOCK.defaultBlockState(), 2);
            return 1;
        }).sum() > 0;
    }

    private OptionalInt getFloorY(WorldGenLevel level, BlockPos origin) {
        Predicate<BlockState> insideColumn = state -> state.is(Blocks.WATER);
        Predicate<BlockState> validEdge = state -> !state.is(Blocks.WATER);
        Optional<Column> waterColumn = Column.scan(level, origin, this.floorSearchRange, insideColumn, validEdge);
        return waterColumn.map(Column::getFloor).orElseGet(OptionalInt::empty);
    }

    private boolean isValidPlacement(WorldGenLevel level, BlockPos pos) {
        if (UnderwaterMagmaFeature.isWaterOrAir(level.getBlockState(pos)) || this.isVisibleFromOutside(level, pos.below(), Direction.UP)) {
            return false;
        }
        for (Direction neighbourDir : Direction.Plane.HORIZONTAL) {
            if (!this.isVisibleFromOutside(level, pos.relative(neighbourDir), neighbourDir.getOpposite())) continue;
            return false;
        }
        return true;
    }

    private static boolean isWaterOrAir(BlockState state) {
        return state.is(Blocks.WATER) || state.isAir();
    }

    private boolean isVisibleFromOutside(LevelAccessor level, BlockPos pos, Direction coveredDirection) {
        BlockState state = level.getBlockState(pos);
        VoxelShape faceOcclusionShape = state.getFaceOcclusionShape(coveredDirection);
        return faceOcclusionShape == Shapes.empty() || !Block.isShapeFullBlock(faceOcclusionShape);
    }
}

