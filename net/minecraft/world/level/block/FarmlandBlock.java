/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class FarmlandBlock
extends Block {
    private final Block baseBlock;
    public static final IntegerProperty MOISTURE = BlockStateProperties.MOISTURE;
    private static final VoxelShape SHAPE = Block.column(16.0, 0.0, 15.0);
    public static final int MAX_MOISTURE = 7;

    protected FarmlandBlock(Block baseBlock, BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(MOISTURE, 0));
        this.baseBlock = baseBlock;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (directionToNeighbour == Direction.UP && !state.canSurvive(level, pos)) {
            ticks.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState aboveState = level.getBlockState(pos.above());
        return !aboveState.isSolid() || FarmlandBlock.shouldMaintainFarmland(level, pos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (!this.defaultBlockState().canSurvive(context.getLevel(), context.getClickedPos())) {
            return this.baseBlock.defaultBlockState();
        }
        return super.getStateForPlacement(context);
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            this.turnToBaseBlock(null, state, level, pos);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int moisture = state.getValue(MOISTURE);
        if (FarmlandBlock.isNearWater(level, pos) || level.isRainingAt(pos.above())) {
            if (moisture < 7) {
                level.setBlock(pos, (BlockState)state.setValue(MOISTURE, 7), 2);
            }
        } else if (moisture > 0) {
            level.setBlock(pos, (BlockState)state.setValue(MOISTURE, moisture - 1), 2);
        } else if (!FarmlandBlock.shouldMaintainFarmland(level, pos)) {
            this.turnToBaseBlock(null, state, level, pos);
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if ((double)level.getRandom().nextFloat() < fallDistance - 0.5 && entity instanceof LivingEntity) {
                boolean canTrample;
                if (entity instanceof Player) {
                    Player player = (Player)entity;
                    v0 = !level.getServer().isUnderSpawnProtection(serverLevel, pos, player);
                } else {
                    v0 = canTrample = serverLevel.getGameRules().get(GameRules.MOB_GRIEFING).booleanValue();
                }
                if (canTrample && entity.getBbWidth() * entity.getBbWidth() * entity.getBbHeight() > 0.512f) {
                    this.turnToBaseBlock(entity, state, level, pos);
                }
            }
        }
        super.fallOn(level, state, pos, entity, fallDistance);
    }

    public void turnToBaseBlock(@Nullable Entity sourceEntity, BlockState state, Level level, BlockPos pos) {
        BlockState newState = FarmlandBlock.pushEntitiesUp(state, this.baseBlock.defaultBlockState(), level, pos);
        level.setBlockAndUpdate(pos, newState);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(sourceEntity, newState));
    }

    private static boolean shouldMaintainFarmland(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos.above()).is(BlockTags.MAINTAINS_FARMLAND);
    }

    private static boolean isNearWater(LevelReader level, BlockPos pos) {
        return level.findBlocksIn(pos.offset(-4, 0, -4), pos.offset(4, 1, 4)).filterState(state -> state.getFluidState().is(FluidTags.WATER)).anyMatched();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MOISTURE);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }
}

