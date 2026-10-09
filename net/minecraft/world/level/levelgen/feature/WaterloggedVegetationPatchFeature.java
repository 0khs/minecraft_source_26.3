/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.MapCodec;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.VegetationPatchFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class WaterloggedVegetationPatchFeature
extends VegetationPatchFeature {
    public static final MapCodec<WaterloggedVegetationPatchFeature> CODEC = WaterloggedVegetationPatchFeature.makeCodec(WaterloggedVegetationPatchFeature::new);

    public WaterloggedVegetationPatchFeature(HolderSet<Block> replaceable, Holder<BlockStateProvider> groundState, Holder<PlacedFeature> vegetationFeature, CaveSurface surface, IntProvider depth, float extraBottomBlockChance, int verticalRange, float vegetationChance, IntProvider xzRadius, float extraEdgeColumnChance) {
        super(replaceable, groundState, vegetationFeature, surface, depth, extraBottomBlockChance, verticalRange, vegetationChance, xzRadius, extraEdgeColumnChance);
    }

    public MapCodec<WaterloggedVegetationPatchFeature> codec() {
        return CODEC;
    }

    @Override
    public Set<BlockPos> placeGroundPatch(WorldGenLevel level, RandomSource random, BlockPos origin, Predicate<BlockState> replaceable, int xRadius, int zRadius) {
        Set<BlockPos> surface = super.placeGroundPatch(level, random, origin, replaceable, xRadius, zRadius);
        HashSet<BlockPos> waterSurface = new HashSet<BlockPos>();
        BlockPos.MutableBlockPos testPos = new BlockPos.MutableBlockPos();
        for (BlockPos surfacePos : surface) {
            if (WaterloggedVegetationPatchFeature.isExposed(level, surface, surfacePos, testPos)) continue;
            waterSurface.add(surfacePos);
        }
        for (BlockPos surfacePos : waterSurface) {
            level.setBlock(surfacePos, Blocks.WATER.defaultBlockState(), 2);
        }
        return waterSurface;
    }

    private static boolean isExposed(WorldGenLevel level, Set<BlockPos> surface, BlockPos pos, BlockPos.MutableBlockPos testPos) {
        return WaterloggedVegetationPatchFeature.isExposedDirection(level, pos, testPos, Direction.NORTH) || WaterloggedVegetationPatchFeature.isExposedDirection(level, pos, testPos, Direction.EAST) || WaterloggedVegetationPatchFeature.isExposedDirection(level, pos, testPos, Direction.SOUTH) || WaterloggedVegetationPatchFeature.isExposedDirection(level, pos, testPos, Direction.WEST) || WaterloggedVegetationPatchFeature.isExposedDirection(level, pos, testPos, Direction.DOWN);
    }

    private static boolean isExposedDirection(WorldGenLevel level, BlockPos pos, BlockPos.MutableBlockPos testPos, Direction direction) {
        testPos.setWithOffset((Vec3i)pos, direction);
        return !level.getBlockState(testPos).isFaceSturdy(level, testPos, direction.getOpposite());
    }

    @Override
    protected boolean placeVegetation(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos placementPos) {
        if (super.placeVegetation(level, generator, random, placementPos.below())) {
            BlockState placed = level.getBlockState(placementPos);
            if (placed.hasProperty(BlockStateProperties.WATERLOGGED) && !placed.getValue(BlockStateProperties.WATERLOGGED).booleanValue()) {
                level.setBlock(placementPos, (BlockState)placed.setValue(BlockStateProperties.WATERLOGGED, true), 2);
            }
            return true;
        }
        return false;
    }
}

