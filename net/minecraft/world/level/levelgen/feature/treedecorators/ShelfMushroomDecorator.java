/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShelfMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class ShelfMushroomDecorator
extends TreeDecorator {
    public static final MapCodec<ShelfMushroomDecorator> CODEC = Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("probability").xmap(ShelfMushroomDecorator::new, d -> Float.valueOf(d.placementProbability));
    private static final int MIN_HEIGHT_OFFSET = 1;
    private static final int MAX_HEIGHT_OFFSET = 4;
    private static final float PER_SIDE_PLACEMENT_CHANCE = 0.25f;
    private static final int MAX_AGE_EXCLUSIVE = 2;
    private final float placementProbability;

    public ShelfMushroomDecorator(float probability) {
        this.placementProbability = probability;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.SHELF_MUSHROOM;
    }

    @Override
    public void place(TreeDecorator.Context context) {
        RandomSource random = context.random();
        if (random.nextFloat() >= this.placementProbability) {
            return;
        }
        ObjectArrayList<BlockPos> logs = context.logs();
        if (logs.isEmpty()) {
            return;
        }
        if (ShelfMushroomDecorator.isFallenLog(logs)) {
            ShelfMushroomDecorator.placeOnFallenLog(context, logs, random);
        } else {
            ShelfMushroomDecorator.placeOnStandingTree(context, logs, random);
        }
    }

    private static void placeOnStandingTree(TreeDecorator.Context context, List<BlockPos> logs, RandomSource random) {
        Direction[] directions = ShelfMushroomDecorator.pickTwoPerpendicularDirections(random);
        int treeBaseY = logs.getFirst().getY();
        block0: for (BlockPos logPos : logs) {
            if (!ShelfMushroomDecorator.isWithinDecoratableHeight(logPos, treeBaseY)) continue;
            for (Direction facing : directions) {
                if (!(random.nextFloat() > 0.25f) && ShelfMushroomDecorator.tryPlaceMushroomOnStandingTree(context, logPos, facing, random)) continue block0;
            }
        }
    }

    private static void placeOnFallenLog(TreeDecorator.Context context, List<BlockPos> logs, RandomSource random) {
        Direction[] directions = ShelfMushroomDecorator.perpendicularToFallenLog(logs);
        for (BlockPos logPos : logs) {
            for (Direction facing : directions) {
                if (random.nextFloat() > 0.25f) continue;
                ShelfMushroomDecorator.tryPlaceMushroomOnFallenTree(context, logPos, facing, random);
            }
        }
    }

    private static boolean tryPlaceMushroomOnStandingTree(TreeDecorator.Context context, BlockPos logPos, Direction facing, RandomSource random) {
        BlockPos mushroomPos = ShelfMushroomDecorator.mushroomPosFor(logPos, facing);
        if (!ShelfMushroomDecorator.isBlockReplaceableWithShelfMushroom(context, mushroomPos)) {
            return false;
        }
        if (ShelfMushroomDecorator.hasShelfMushroomAt(context, mushroomPos.below())) {
            return false;
        }
        ShelfMushroomDecorator.placeMushroom(context, mushroomPos, facing, random);
        return true;
    }

    private static void tryPlaceMushroomOnFallenTree(TreeDecorator.Context context, BlockPos logPos, Direction facing, RandomSource random) {
        BlockPos mushroomPos = ShelfMushroomDecorator.mushroomPosFor(logPos, facing);
        if (!ShelfMushroomDecorator.isBlockReplaceableWithShelfMushroom(context, mushroomPos)) {
            return;
        }
        if (ShelfMushroomDecorator.hasHorizontallyAdjacentShelfMushroom(context, mushroomPos) || ShelfMushroomDecorator.hasHorizontallyAdjacentShelfMushroom(context, logPos)) {
            return;
        }
        ShelfMushroomDecorator.placeMushroom(context, mushroomPos, facing, random);
    }

    private static boolean isFallenLog(List<BlockPos> logs) {
        return logs.getFirst().getY() == logs.getLast().getY();
    }

    private static Direction[] pickTwoPerpendicularDirections(RandomSource random) {
        Direction first = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        return new Direction[]{first, first.getClockWise()};
    }

    private static Direction[] perpendicularToFallenLog(List<BlockPos> logs) {
        Direction[] directionArray;
        Direction.Axis logAxis;
        BlockPos first = logs.getFirst();
        BlockPos last = logs.getLast();
        Direction.Axis axis = logAxis = first.getX() != last.getX() ? Direction.Axis.X : Direction.Axis.Z;
        if (logAxis == Direction.Axis.X) {
            Direction[] directionArray2 = new Direction[2];
            directionArray2[0] = Direction.NORTH;
            directionArray = directionArray2;
            directionArray2[1] = Direction.SOUTH;
        } else {
            Direction[] directionArray3 = new Direction[2];
            directionArray3[0] = Direction.EAST;
            directionArray = directionArray3;
            directionArray3[1] = Direction.WEST;
        }
        return directionArray;
    }

    private static boolean isWithinDecoratableHeight(BlockPos pos, int treeBaseY) {
        int dy = pos.getY() - treeBaseY;
        return dy >= 1 && dy <= 4;
    }

    private static BlockPos mushroomPosFor(BlockPos logPos, Direction facing) {
        return logPos.offset(facing.getStepX(), 0, facing.getStepZ());
    }

    private static void placeMushroom(TreeDecorator.Context context, BlockPos pos, Direction facing, RandomSource random) {
        context.setBlock(pos, (BlockState)((BlockState)Blocks.SHELF_MUSHROOM.defaultBlockState().setValue(ShelfMushroomBlock.AGE, random.nextInt(2))).setValue(ShelfMushroomBlock.FACING, facing));
    }

    private static boolean hasShelfMushroomAt(TreeDecorator.Context context, BlockPos pos) {
        return context.checkBlock(pos, state -> state.is(Blocks.SHELF_MUSHROOM));
    }

    private static boolean hasHorizontallyAdjacentShelfMushroom(TreeDecorator.Context context, BlockPos pos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (!ShelfMushroomDecorator.hasShelfMushroomAt(context, pos.relative(dir))) continue;
            return true;
        }
        return false;
    }

    public static boolean isBlockReplaceableWithShelfMushroom(TreeDecorator.Context context, BlockPos pos) {
        return context.isReplaceable(pos) && !context.isWaterOrWaterNearby(pos);
    }
}

