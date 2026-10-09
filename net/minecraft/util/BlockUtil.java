/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 */
package net.minecraft.util;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockUtil {
    public static final int MAX_POSITION_DIFFERENCE_PACKING_RADIUS = 127;

    public static FoundRectangle getLargestRectangleAround(BlockPos center, Direction.Axis axis1, int limit1, Direction.Axis axis2, int limit2, Predicate<BlockPos> test) {
        IntBounds lastBounds;
        int i;
        BlockPos.MutableBlockPos pos = center.mutable();
        Direction negativeDirection1 = Direction.get(Direction.AxisDirection.NEGATIVE, axis1);
        Direction positiveDirection1 = negativeDirection1.getOpposite();
        Direction negativeDirection2 = Direction.get(Direction.AxisDirection.NEGATIVE, axis2);
        Direction positiveDirection2 = negativeDirection2.getOpposite();
        int negativeDelta1 = BlockUtil.getLimit(test, pos.set(center), negativeDirection1, limit1);
        int positiveDelta1 = BlockUtil.getLimit(test, pos.set(center), positiveDirection1, limit1);
        int centerIndex1 = negativeDelta1;
        IntBounds[] boundsByAxis1 = new IntBounds[centerIndex1 + 1 + positiveDelta1];
        boundsByAxis1[centerIndex1] = new IntBounds(BlockUtil.getLimit(test, pos.set(center), negativeDirection2, limit2), BlockUtil.getLimit(test, pos.set(center), positiveDirection2, limit2));
        int centerIndex2 = boundsByAxis1[centerIndex1].min;
        for (i = 1; i <= negativeDelta1; ++i) {
            lastBounds = boundsByAxis1[centerIndex1 - (i - 1)];
            boundsByAxis1[centerIndex1 - i] = new IntBounds(BlockUtil.getLimit(test, pos.set(center).move(negativeDirection1, i), negativeDirection2, lastBounds.min), BlockUtil.getLimit(test, pos.set(center).move(negativeDirection1, i), positiveDirection2, lastBounds.max));
        }
        for (i = 1; i <= positiveDelta1; ++i) {
            lastBounds = boundsByAxis1[centerIndex1 + i - 1];
            boundsByAxis1[centerIndex1 + i] = new IntBounds(BlockUtil.getLimit(test, pos.set(center).move(positiveDirection1, i), negativeDirection2, lastBounds.min), BlockUtil.getLimit(test, pos.set(center).move(positiveDirection1, i), positiveDirection2, lastBounds.max));
        }
        int minAxis1 = 0;
        int minAxis2 = 0;
        int sizeAxis1 = 0;
        int sizeAxis2 = 0;
        int[] columns = new int[boundsByAxis1.length];
        for (int i2 = centerIndex2; i2 >= 0; --i2) {
            for (int i1 = 0; i1 < boundsByAxis1.length; ++i1) {
                IntBounds bounds2 = boundsByAxis1[i1];
                int min2 = centerIndex2 - bounds2.min;
                int max2 = centerIndex2 + bounds2.max;
                columns[i1] = i2 >= min2 && i2 <= max2 ? max2 + 1 - i2 : 0;
            }
            Pair<IntBounds, Integer> rectangle = BlockUtil.getMaxRectangleLocation(columns);
            IntBounds boundsAxis1 = (IntBounds)rectangle.getFirst();
            int newSizeAxis1 = 1 + boundsAxis1.max - boundsAxis1.min;
            int newSizeAxis2 = (Integer)rectangle.getSecond();
            if (newSizeAxis1 * newSizeAxis2 <= sizeAxis1 * sizeAxis2) continue;
            minAxis1 = boundsAxis1.min;
            minAxis2 = i2;
            sizeAxis1 = newSizeAxis1;
            sizeAxis2 = newSizeAxis2;
        }
        return new FoundRectangle(center.relative(axis1, minAxis1 - centerIndex1).relative(axis2, minAxis2 - centerIndex2), sizeAxis1, sizeAxis2);
    }

    private static int getLimit(Predicate<BlockPos> test, BlockPos.MutableBlockPos pos, Direction direction, int limit) {
        int max;
        for (max = 0; max < limit && test.test(pos.move(direction)); ++max) {
        }
        return max;
    }

    @VisibleForTesting
    static Pair<IntBounds, Integer> getMaxRectangleLocation(int[] columns) {
        int maxStart = 0;
        int maxEnd = 0;
        int maxHeight = 0;
        IntArrayList stack = new IntArrayList();
        stack.push(0);
        for (int column = 1; column <= columns.length; ++column) {
            int height;
            int n = height = column == columns.length ? 0 : columns[column];
            while (!stack.isEmpty()) {
                int stackHeight = columns[stack.topInt()];
                if (height >= stackHeight) {
                    stack.push(column);
                    break;
                }
                stack.popInt();
                int start = stack.isEmpty() ? 0 : stack.topInt() + 1;
                if (stackHeight * (column - start) <= maxHeight * (maxEnd - maxStart)) continue;
                maxEnd = column;
                maxStart = start;
                maxHeight = stackHeight;
            }
            if (!stack.isEmpty()) continue;
            stack.push(column);
        }
        return new Pair((Object)new IntBounds(maxStart, maxEnd - 1), (Object)maxHeight);
    }

    public static Optional<BlockPos> getTopConnectedBlock(BlockGetter level, BlockPos pos, Block bodyBlock, Direction growthDirection, Block headBlock) {
        BlockState forwardState;
        BlockPos.MutableBlockPos forwardPos = pos.mutable();
        do {
            forwardPos.move(growthDirection);
        } while ((forwardState = level.getBlockState(forwardPos)).is(bodyBlock));
        if (forwardState.is(headBlock)) {
            return Optional.of(forwardPos);
        }
        return Optional.empty();
    }

    public static int clampedPackDifferenceInPosition(BlockPos pos, BlockPos testPos, int xRadius, int yRadius, int zRadius) {
        int clampedXRadius = Mth.clamp(xRadius, 0, 127);
        int clampedYRadius = Mth.clamp(yRadius, 0, 127);
        int clampedZRadius = Mth.clamp(zRadius, 0, 127);
        int xDiff = Mth.clamp(testPos.getX() - pos.getX(), -127, 127);
        int yDiff = Mth.clamp(testPos.getY() - pos.getY(), -127, 127);
        int zDiff = Mth.clamp(testPos.getZ() - pos.getZ(), -127, 127);
        return BlockUtil.packDifferenceInPositionInternal(clampedXRadius, clampedYRadius, clampedZRadius, xDiff, yDiff, zDiff);
    }

    public static int packDifferenceInPosition(BlockPos pos, BlockPos testPos, int xRadius, int yRadius, int zRadius) {
        if (xRadius > 127 || yRadius > 127 || zRadius > 127) {
            throw new IllegalArgumentException("The radius cannot be larger than 127 when packing the difference between two positions");
        }
        int xDiff = BlockUtil.checkDifferenceInBounds(testPos.getX(), pos.getX());
        int yDiff = BlockUtil.checkDifferenceInBounds(testPos.getY(), pos.getY());
        int zDiff = BlockUtil.checkDifferenceInBounds(testPos.getZ(), pos.getZ());
        return BlockUtil.packDifferenceInPositionInternal(xRadius, yRadius, zRadius, xDiff, yDiff, zDiff);
    }

    private static int packDifferenceInPositionInternal(int xRadius, int yRadius, int zRadius, int xDiff, int yDiff, int zDiff) {
        return (xDiff + xRadius & 0xFF) << 16 | (yDiff + yRadius & 0xFF) << 8 | zDiff + zRadius & 0xFF;
    }

    private static int checkDifferenceInBounds(int from, int to) {
        int difference = from - to;
        if (difference > 127 || difference < -127) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "The difference must be within [%s,%s]", 127, -127));
        }
        return difference;
    }

    public static BlockPos unpackDifferenceInPosition(BlockPos pos, int packedDiff, int xRadius, int yRadius, int zRadius) {
        if (xRadius > 127 || yRadius > 127 || zRadius > 127) {
            throw new IllegalArgumentException("The radius cannot be larger than 127 when unpacking the difference between two positions.");
        }
        int xDiff = (packedDiff >> 16 & 0xFF) - xRadius;
        int yDiff = (packedDiff >> 8 & 0xFF) - yRadius;
        int zDiff = (packedDiff & 0xFF) - zRadius;
        return pos.offset(xDiff, yDiff, zDiff);
    }

    public static class IntBounds {
        public final int min;
        public final int max;

        public IntBounds(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public String toString() {
            return "IntBounds{min=" + this.min + ", max=" + this.max + "}";
        }
    }

    public static class FoundRectangle {
        public final BlockPos minCorner;
        public final int axis1Size;
        public final int axis2Size;

        public FoundRectangle(BlockPos minCorner, int axis1Size, int axis2Size) {
            this.minCorner = minCorner;
            this.axis1Size = axis1Size;
            this.axis2Size = axis2Size;
        }
    }
}

