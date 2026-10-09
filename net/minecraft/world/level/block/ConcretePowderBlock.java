/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ConcretePowderBlock
extends FallingBlock {
    private final Block concrete;

    public ConcretePowderBlock(Block concrete, BlockBehaviour.Properties properties) {
        super(properties);
        this.concrete = concrete;
    }

    @Override
    public void onLand(Level level, BlockPos pos, BlockState state, BlockState replacedBlock, FallingBlockEntity entity) {
        if (ConcretePowderBlock.shouldSolidify(level, pos, replacedBlock)) {
            level.setBlockAndUpdate(pos, this.concrete.defaultBlockState());
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState replacedBlock;
        BlockPos pos;
        Level level = context.getLevel();
        if (ConcretePowderBlock.shouldSolidify(level, pos = context.getClickedPos(), replacedBlock = level.getBlockState(pos))) {
            return this.concrete.defaultBlockState();
        }
        return super.getStateForPlacement(context);
    }

    private static boolean shouldSolidify(BlockGetter level, BlockPos pos, BlockState replacedBlock) {
        return ConcretePowderBlock.canSolidify(replacedBlock) || ConcretePowderBlock.touchesLiquid(level, pos);
    }

    private static boolean touchesLiquid(BlockGetter level, BlockPos pos) {
        boolean touchesLiquid = false;
        BlockPos.MutableBlockPos testPos = pos.mutable();
        for (Direction direction : Direction.values()) {
            BlockState blockState = level.getBlockState(testPos);
            if (direction == Direction.DOWN && !ConcretePowderBlock.canSolidify(blockState)) continue;
            testPos.setWithOffset((Vec3i)pos, direction);
            blockState = level.getBlockState(testPos);
            if (!ConcretePowderBlock.canSolidify(blockState) || blockState.isFaceSturdy(level, pos, direction.getOpposite())) continue;
            touchesLiquid = true;
            break;
        }
        return touchesLiquid;
    }

    private static boolean canSolidify(BlockState state) {
        return state.getFluidState().is(FluidTags.WATER);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (ConcretePowderBlock.touchesLiquid(level, pos)) {
            return this.concrete.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    public int getDustColor(BlockState blockState, BlockGetter level, BlockPos pos) {
        return blockState.getMapColor((BlockGetter)level, (BlockPos)pos).col;
    }
}

