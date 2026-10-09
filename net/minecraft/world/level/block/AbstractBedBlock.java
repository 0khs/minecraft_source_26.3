/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.ArrayUtils
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.Nullable;

public abstract class AbstractBedBlock
extends HorizontalDirectionalBlock {
    public static final EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;
    public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;

    public AbstractBedBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(PART, BedPart.FOOT)).setValue(OCCUPIED, false));
    }

    public static @Nullable Direction getBedOrientation(BlockGetter level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);
        return blockState.getBlock() instanceof AbstractBedBlock ? (Direction)blockState.getValue(FACING) : null;
    }

    protected abstract EnvironmentAttribute<BedRule> getBedEnvironmentAttribute();

    protected abstract InteractionResult destroyOnUse(BlockState var1, Level var2, BlockPos var3, Player var4);

    protected abstract void destroyOnLeave(Level var1, BlockPos var2);

    public BedRule getBedRule(Level level, BlockPos pos) {
        return level.environmentAttributes().getValue(this.getBedEnvironmentAttribute(), pos);
    }

    public Identifier getSleptInBedStatType() {
        return Stats.SLEEP_IN_BED;
    }

    public OptionalDouble getSleepHeight(BlockState state, Level level, BlockPos pos) {
        if (!state.is(this)) {
            return OptionalDouble.empty();
        }
        VoxelShape shape = state.getShape(level, pos);
        return shape.isEmpty() ? OptionalDouble.empty() : OptionalDouble.of(shape.max(Direction.Axis.Y));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BedRule bedRule;
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS_SERVER;
        }
        BedPart part = state.getValue(PART);
        BlockState otherState = level.getBlockState(pos.relative(AbstractBedBlock.getNeighbourDirection(part, (Direction)state.getValue(FACING))));
        if (!otherState.is(this) || otherState.getValue(PART) == part) {
            return InteractionResult.CONSUME;
        }
        if (part != BedPart.HEAD) {
            pos = pos.relative((Direction)state.getValue(FACING));
            state = level.getBlockState(pos);
        }
        if ((bedRule = this.getBedRule(level, pos)).destroyOnUse()) {
            bedRule.errorMessage().ifPresent(player::sendOverlayMessage);
            return this.destroyOnUse(state, level, pos, player);
        }
        if (state.getValue(OCCUPIED).booleanValue()) {
            if (!this.kickVillagerOutOfBed(level, pos)) {
                player.sendOverlayMessage(Component.translatable("block.minecraft.bed.occupied"));
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        player.startSleepInBed(this, state, bedRule, pos).ifLeft(problem -> {
            if (problem.message() != null) {
                player.sendOverlayMessage(problem.message());
            }
        });
        return InteractionResult.SUCCESS_SERVER;
    }

    public void onStopSleeping(Level level, BlockPos pos) {
        BedRule bedRule = this.getBedRule(level, pos);
        if (bedRule.destroyOnLeave()) {
            this.destroyOnLeave(level, pos);
        }
    }

    private boolean kickVillagerOutOfBed(Level level, BlockPos pos) {
        List<Villager> villagers = level.getEntitiesOfClass(Villager.class, new AABB(pos), LivingEntity::isSleeping);
        if (villagers.isEmpty()) {
            return false;
        }
        villagers.get(0).stopSleeping();
        return true;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (directionToNeighbour == AbstractBedBlock.getNeighbourDirection(state.getValue(PART), (Direction)state.getValue(FACING))) {
            if (neighbourState.is(this) && neighbourState.getValue(PART) != state.getValue(PART)) {
                return (BlockState)state.setValue(OCCUPIED, neighbourState.getValue(OCCUPIED));
            }
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    private static Direction getNeighbourDirection(BedPart part, Direction facing) {
        return part == BedPart.FOOT ? facing : facing.getOpposite();
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockPos headPos;
        BlockState headState;
        BedPart part;
        if (!level.isClientSide() && player.preventsBlockDrops() && (part = state.getValue(PART)) == BedPart.FOOT && (headState = level.getBlockState(headPos = pos.relative(AbstractBedBlock.getNeighbourDirection(part, (Direction)state.getValue(FACING))))).is(this) && headState.getValue(PART) == BedPart.HEAD) {
            level.setBlock(headPos, Blocks.AIR.defaultBlockState(), 35);
            level.levelEvent(player, 2001, headPos, Block.getId(headState));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        BlockPos pos = context.getClickedPos();
        BlockPos relative = pos.relative(facing);
        Level level = context.getLevel();
        if (level.getBlockState(relative).canBeReplaced(context) && level.getWorldBorder().isWithinBounds(relative)) {
            return (BlockState)this.defaultBlockState().setValue(FACING, facing);
        }
        return null;
    }

    public static Direction getConnectedDirection(BlockState state) {
        Direction facing = (Direction)state.getValue(FACING);
        return state.getValue(PART) == BedPart.HEAD ? facing.getOpposite() : facing;
    }

    public static DoubleBlockCombiner.BlockType getBlockType(BlockState state) {
        BedPart part = state.getValue(PART);
        if (part == BedPart.HEAD) {
            return DoubleBlockCombiner.BlockType.FIRST;
        }
        return DoubleBlockCombiner.BlockType.SECOND;
    }

    private static boolean isBunkBed(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos.below()).getBlock() instanceof AbstractBedBlock;
    }

    public static Optional<Vec3> findStandUpPosition(EntityType<?> type, CollisionGetter level, BlockPos pos, Direction forward, float yaw) {
        Direction side;
        Direction right = forward.getClockWise();
        Direction direction = side = right.isFacingAngle(yaw) ? right.getOpposite() : right;
        if (AbstractBedBlock.isBunkBed(level, pos)) {
            return AbstractBedBlock.findBunkBedStandUpPosition(type, level, pos, forward, side);
        }
        int[][] offsets = AbstractBedBlock.bedStandUpOffsets(forward, side);
        Optional<Vec3> safePosition = AbstractBedBlock.findStandUpPositionAtOffset(type, level, pos, offsets, true);
        if (safePosition.isPresent()) {
            return safePosition;
        }
        return AbstractBedBlock.findStandUpPositionAtOffset(type, level, pos, offsets, false);
    }

    private static Optional<Vec3> findBunkBedStandUpPosition(EntityType<?> type, CollisionGetter level, BlockPos pos, Direction forward, Direction side) {
        int[][] offsets = AbstractBedBlock.bedSurroundStandUpOffsets(forward, side);
        Optional<Vec3> safePosition = AbstractBedBlock.findStandUpPositionAtOffset(type, level, pos, offsets, true);
        if (safePosition.isPresent()) {
            return safePosition;
        }
        BlockPos below = pos.below();
        Optional<Vec3> belowSafePosition = AbstractBedBlock.findStandUpPositionAtOffset(type, level, below, offsets, true);
        if (belowSafePosition.isPresent()) {
            return belowSafePosition;
        }
        int[][] aboveOffsets = AbstractBedBlock.bedAboveStandUpOffsets(forward);
        Optional<Vec3> aboveSafePosition = AbstractBedBlock.findStandUpPositionAtOffset(type, level, pos, aboveOffsets, true);
        if (aboveSafePosition.isPresent()) {
            return aboveSafePosition;
        }
        Optional<Vec3> unsafePosition = AbstractBedBlock.findStandUpPositionAtOffset(type, level, pos, offsets, false);
        if (unsafePosition.isPresent()) {
            return unsafePosition;
        }
        Optional<Vec3> belowUnsafePosition = AbstractBedBlock.findStandUpPositionAtOffset(type, level, below, offsets, false);
        if (belowUnsafePosition.isPresent()) {
            return belowUnsafePosition;
        }
        return AbstractBedBlock.findStandUpPositionAtOffset(type, level, pos, aboveOffsets, false);
    }

    private static Optional<Vec3> findStandUpPositionAtOffset(EntityType<?> type, CollisionGetter level, BlockPos pos, int[][] offsets, boolean checkDangerous) {
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        for (int[] offset : offsets) {
            blockPos.set(pos.getX() + offset[0], pos.getY(), pos.getZ() + offset[1]);
            Vec3 position = DismountHelper.findSafeDismountLocation(type, level, blockPos, checkDangerous);
            if (position == null) continue;
            return Optional.of(position);
        }
        return Optional.empty();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, OCCUPIED);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack itemStack) {
        super.setPlacedBy(level, pos, state, by, itemStack);
        BlockPos otherPos = pos.relative((Direction)state.getValue(FACING));
        level.setBlockAndUpdate(otherPos, (BlockState)state.setValue(PART, BedPart.HEAD));
    }

    @Override
    protected long getSeed(BlockState state, BlockPos pos) {
        BlockPos sourcePos = pos.relative((Direction)state.getValue(FACING), state.getValue(PART) == BedPart.HEAD ? 0 : 1);
        return Mth.getSeed(sourcePos.getX(), pos.getY(), sourcePos.getZ());
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    private static int[][] bedStandUpOffsets(Direction forward, Direction side) {
        return (int[][])ArrayUtils.addAll((Object[])AbstractBedBlock.bedSurroundStandUpOffsets(forward, side), (Object[])AbstractBedBlock.bedAboveStandUpOffsets(forward));
    }

    private static int[][] bedSurroundStandUpOffsets(Direction forward, Direction side) {
        return new int[][]{{side.getStepX(), side.getStepZ()}, {side.getStepX() - forward.getStepX(), side.getStepZ() - forward.getStepZ()}, {side.getStepX() - forward.getStepX() * 2, side.getStepZ() - forward.getStepZ() * 2}, {-forward.getStepX() * 2, -forward.getStepZ() * 2}, {-side.getStepX() - forward.getStepX() * 2, -side.getStepZ() - forward.getStepZ() * 2}, {-side.getStepX() - forward.getStepX(), -side.getStepZ() - forward.getStepZ()}, {-side.getStepX(), -side.getStepZ()}, {-side.getStepX() + forward.getStepX(), -side.getStepZ() + forward.getStepZ()}, {forward.getStepX(), forward.getStepZ()}, {side.getStepX() + forward.getStepX(), side.getStepZ() + forward.getStepZ()}};
    }

    private static int[][] bedAboveStandUpOffsets(Direction forward) {
        return new int[][]{{0, 0}, {-forward.getStepX(), -forward.getStepZ()}};
    }
}

